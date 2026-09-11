package com.idp.idpapi.comment.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idp.idpapi.comment.dto.request.CommentCreateRequest;
import com.idp.idpapi.comment.dto.request.CommentUpdateRequest;
import com.idp.idpapi.comment.dto.response.CommentResponse;
import com.idp.idpapi.comment.entity.Comment;
import com.idp.idpapi.comment.entity.CommentMention;
import com.idp.idpapi.comment.entity.CommentMentionId;
import com.idp.idpapi.comment.mapper.CommentMapper;
import com.idp.idpapi.comment.repository.CommentMentionRepository;
import com.idp.idpapi.comment.repository.CommentRepository;
import com.idp.idpapi.comment.service.CommentService;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.contract.entity.Contract;
import com.idp.idpapi.contract.repository.ContractRepository;
import com.idp.idpapi.notification.service.NotificationService;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.repository.UserRepository;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CommentMapper commentMapper;

    public CommentServiceImpl(
            CommentRepository commentRepository,
            CommentMentionRepository commentMentionRepository,
            ContractRepository contractRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.commentMentionRepository = commentMentionRepository;
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getByContractId(Integer contractId) {
        getContract(contractId);
        return commentRepository.findByContractContractIdOrderByCreatedAtAsc(contractId)
                .stream()
                .map(comment -> commentMapper.toResponse(
                        comment,
                        commentMentionRepository.findMentionedUserIdsByCommentId(comment.getCommentId())))
                .toList();
    }

    @Override
    public CommentResponse create(Integer contractId, CommentCreateRequest request, Integer currentUserId) {
        Contract contract = getContract(contractId);
        User currentUser = getUser(currentUserId);
        Comment comment = new Comment();
        comment.setContract(contract);
        comment.setMetadataId(request.metadataId());
        comment.setParentComment(resolveParent(request.parentCommentId(), contractId));
        comment.setUser(currentUser);
        comment.setContent(request.content().trim());
        comment.setPageNumber(request.pageNumber());
        comment = commentRepository.save(comment);
        syncMentions(comment, request.mentionedUserIds());
        return mapAndNotify(comment);
    }

    @Override
    public CommentResponse update(Integer commentId, CommentUpdateRequest request, Integer currentUserId) {
        Comment comment = getComment(commentId);
        ensureOwner(comment, currentUserId);
        comment.setContent(request.content().trim());
        commentMentionRepository.deleteByCommentCommentId(commentId);
        syncMentions(comment, request.mentionedUserIds());
        return mapAndNotify(commentRepository.save(comment));
    }

    @Override
    public void delete(Integer commentId, Integer currentUserId) {
        Comment comment = getComment(commentId);
        ensureOwner(comment, currentUserId);
        commentRepository.delete(comment);
    }

    @Override
    public CommentResponse reply(Integer commentId, CommentCreateRequest request, Integer currentUserId) {
        Comment parentComment = getComment(commentId);
        CommentCreateRequest replyRequest = new CommentCreateRequest(
                request.metadataId(),
                parentComment.getCommentId(),
                request.pageNumber(),
                request.content(),
                request.mentionedUserIds());
        return create(parentComment.getContract().getContractId(), replyRequest, currentUserId);
    }

    private CommentResponse mapAndNotify(Comment comment) {
        List<Integer> mentionedUserIds = commentMentionRepository.findMentionedUserIdsByCommentId(comment.getCommentId());
        for (Integer mentionedUserId : mentionedUserIds) {
            if (mentionedUserId.equals(comment.getUser().getUserId())) {
                continue;
            }
            notificationService.createNotification(
                    mentionedUserId,
                    "Ban duoc nhac trong binh luan",
                    comment.getUser().getFullName() + " da nhac den ban trong hop dong " + comment.getContract().getContractNumber(),
                    "COMMENT_MENTION",
                    "/contracts/" + comment.getContract().getContractId());
        }
        return commentMapper.toResponse(comment, mentionedUserIds);
    }

    private void syncMentions(Comment comment, Set<Integer> mentionedUserIds) {
        if (mentionedUserIds == null || mentionedUserIds.isEmpty()) {
            return;
        }
        Map<Integer, User> users = userRepository.findAllById(mentionedUserIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
        if (users.size() != mentionedUserIds.size()) {
            throw new ResourceNotFoundException("Co nguoi duoc mention khong ton tai.");
        }
        for (Integer mentionedUserId : mentionedUserIds) {
            CommentMention mention = new CommentMention();
            mention.setId(new CommentMentionId(comment.getCommentId(), mentionedUserId));
            mention.setComment(comment);
            mention.setMentionedUser(users.get(mentionedUserId));
            commentMentionRepository.save(mention);
        }
    }

    private Comment resolveParent(Integer parentCommentId, Integer contractId) {
        if (parentCommentId == null) {
            return null;
        }
        Comment parent = getComment(parentCommentId);
        if (!parent.getContract().getContractId().equals(contractId)) {
            throw new ResourceNotFoundException("Khong tim thay comment cha hop le.");
        }
        return parent;
    }

    private Comment getComment(Integer commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay comment."));
    }

    private Contract getContract(Integer contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay hop dong."));
    }

    private User getUser(Integer userId) {
        return userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung."));
    }

    private void ensureOwner(Comment comment, Integer currentUserId) {
        if (!comment.getUser().getUserId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Khong tim thay comment.");
        }
    }
}
