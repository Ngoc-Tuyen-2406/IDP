package com.idp.idpapi.comment.service;

import java.util.List;

import com.idp.idpapi.comment.dto.request.CommentCreateRequest;
import com.idp.idpapi.comment.dto.request.CommentUpdateRequest;
import com.idp.idpapi.comment.dto.response.CommentResponse;

public interface CommentService {

    List<CommentResponse> getByContractId(Integer contractId);

    CommentResponse create(Integer contractId, CommentCreateRequest request, Integer currentUserId);

    CommentResponse update(Integer commentId, CommentUpdateRequest request, Integer currentUserId);

    void delete(Integer commentId, Integer currentUserId);

    CommentResponse reply(Integer commentId, CommentCreateRequest request, Integer currentUserId);
}
