package com.idp.idpapi.contract.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.contract.dto.request.ContractCreateRequest;
import com.idp.idpapi.contract.dto.request.ContractUpdateRequest;
import com.idp.idpapi.contract.dto.request.ContractUploadRequest;
import com.idp.idpapi.contract.dto.response.ContractDetailResponse;
import com.idp.idpapi.contract.dto.response.ContractDownloadPayload;
import com.idp.idpapi.contract.dto.response.ContractSummaryResponse;
import com.idp.idpapi.contract.entity.Contract;
import com.idp.idpapi.contract.entity.ContractFile;
import com.idp.idpapi.contract.entity.ContractVersion;
import com.idp.idpapi.contract.entity.FavoriteContract;
import com.idp.idpapi.contract.entity.FavoriteContractId;
import com.idp.idpapi.contract.enums.ContractStatus;
import com.idp.idpapi.contract.enums.ContractVersionStatus;
import com.idp.idpapi.contract.mapper.ContractMapper;
import com.idp.idpapi.contract.repository.ContractFileRepository;
import com.idp.idpapi.contract.repository.ContractRepository;
import com.idp.idpapi.contract.repository.ContractSpecifications;
import com.idp.idpapi.contract.repository.ContractVersionRepository;
import com.idp.idpapi.contract.repository.FavoriteContractRepository;
import com.idp.idpapi.contract.service.ContractService;
import com.idp.idpapi.documenttype.entity.DocumentType;
import com.idp.idpapi.documenttype.repository.DocumentTypeRepository;
import com.idp.idpapi.partner.entity.Partner;
import com.idp.idpapi.partner.repository.PartnerRepository;
import com.idp.idpapi.storage.FileStorageService;
import com.idp.idpapi.storage.StoredFileResult;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.repository.UserRepository;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final ContractFileRepository contractFileRepository;
    private final FavoriteContractRepository favoriteContractRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final ContractMapper contractMapper;
    private final FileStorageService fileStorageService;
    private final Clock clock;

    public ContractServiceImpl(
            ContractRepository contractRepository,
            ContractVersionRepository contractVersionRepository,
            ContractFileRepository contractFileRepository,
            FavoriteContractRepository favoriteContractRepository,
            DocumentTypeRepository documentTypeRepository,
            PartnerRepository partnerRepository,
            UserRepository userRepository,
            ContractMapper contractMapper,
            FileStorageService fileStorageService,
            Clock clock) {
        this.contractRepository = contractRepository;
        this.contractVersionRepository = contractVersionRepository;
        this.contractFileRepository = contractFileRepository;
        this.favoriteContractRepository = favoriteContractRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.partnerRepository = partnerRepository;
        this.userRepository = userRepository;
        this.contractMapper = contractMapper;
        this.fileStorageService = fileStorageService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContractSummaryResponse> getContracts(
            String keyword,
            Integer partnerId,
            Integer documentTypeId,
            ContractStatus status,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            LocalDate expiredFrom,
            LocalDate expiredTo,
            Integer currentUserId,
            Pageable pageable) {
        Specification<Contract> specification = Specification.where(ContractSpecifications.keyword(keyword))
                .and(ContractSpecifications.partnerId(partnerId))
                .and(ContractSpecifications.documentTypeId(documentTypeId))
                .and(ContractSpecifications.status(status))
                .and(ContractSpecifications.effectiveDateFrom(effectiveFrom))
                .and(ContractSpecifications.effectiveDateTo(effectiveTo))
                .and(ContractSpecifications.expiredDateFrom(expiredFrom))
                .and(ContractSpecifications.expiredDateTo(expiredTo));

        Page<Contract> contracts = contractRepository.findAll(specification, pageable);
        return PageResponse.from(contracts.map(contract -> buildSummary(contract, currentUserId)));
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDetailResponse getById(Integer contractId, Integer currentUserId) {
        Contract contract = getContract(contractId);
        return buildDetail(contract, currentUserId);
    }

    @Override
    public ContractDetailResponse create(ContractCreateRequest request, Integer currentUserId) {
        validateContractNumberForCreate(request.contractNumber());
        User currentUser = getUser(currentUserId);
        Contract contract = new Contract();
        contract.setDocumentType(getDocumentType(request.documentTypeId()));
        contract.setPartner(getPartner(request.partnerId()));
        contract.setUploadedBy(currentUser);
        contractMapper.applyCreateRequest(contract, request);
        contract = contractRepository.save(contract);

        ContractVersion version = createVersion(contract, currentUser, request.changeNote(), ContractVersionStatus.DRAFT);
        contract.setCurrentVersionId(version.getVersionId());
        contractRepository.save(contract);

        return buildDetail(contract, currentUserId);
    }

    @Override
    public ContractDetailResponse upload(ContractUploadRequest request, MultipartFile file, Integer currentUserId) {
        validateContractNumberForCreate(request.getContractNumber());
        User currentUser = getUser(currentUserId);
        Contract contract = new Contract();
        contract.setDocumentType(getDocumentType(request.getDocumentTypeId()));
        contract.setPartner(getPartner(request.getPartnerId()));
        contract.setUploadedBy(currentUser);
        contractMapper.applyUploadRequest(contract, request);
        contract = contractRepository.save(contract);

        ContractVersion version = createVersion(contract, currentUser, request.getChangeNote(), ContractVersionStatus.PUBLISHED);
        contract.setCurrentVersionId(version.getVersionId());
        contractRepository.save(contract);

        StoredFileResult storedFile = fileStorageService.storeContractFile(file, contract.getContractNumber());
        ContractFile contractFile = new ContractFile();
        contractFile.setVersion(version);
        contractFile.setFileName(storedFile.originalFileName());
        contractFile.setFilePath(storedFile.storedPath());
        contractFile.setFileType(storedFile.contentType());
        contractFile.setFileSize(storedFile.fileSize());
        contractFile.setFileHash(storedFile.hash());
        contractFileRepository.save(contractFile);

        return buildDetail(contract, currentUserId);
    }

    @Override
    public ContractDetailResponse update(Integer contractId, ContractUpdateRequest request, Integer currentUserId) {
        Contract contract = getContract(contractId);
        validateContractNumberForUpdate(request.contractNumber(), contractId);
        User currentUser = getUser(currentUserId);

        contract.setDocumentType(getDocumentType(request.documentTypeId()));
        contract.setPartner(getPartner(request.partnerId()));
        contractMapper.applyUpdateRequest(contract, request);
        contract = contractRepository.save(contract);

        ContractVersion version = createVersion(contract, currentUser, request.changeNote(), ContractVersionStatus.PUBLISHED);
        contract.setCurrentVersionId(version.getVersionId());
        contractRepository.save(contract);

        return buildDetail(contract, currentUserId);
    }

    @Override
    public void delete(Integer contractId) {
        Contract contract = getContract(contractId);
        contractFileRepository.findByVersionContractContractId(contractId)
                .forEach(file -> fileStorageService.deleteIfExists(file.getFilePath()));
        contractRepository.delete(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContractSummaryResponse> getFavoriteContracts(Integer currentUserId, Pageable pageable) {
        Page<FavoriteContract> favorites = favoriteContractRepository.findByUserUserId(currentUserId, pageable);
        return PageResponse.from(favorites.map(favorite -> buildSummary(favorite.getContract(), currentUserId)));
    }

    @Override
    public void markFavorite(Integer contractId, Integer currentUserId) {
        if (favoriteContractRepository.existsByUserUserIdAndContractContractId(currentUserId, contractId)) {
            return;
        }
        FavoriteContract favorite = new FavoriteContract();
        favorite.setId(new FavoriteContractId(currentUserId, contractId));
        favorite.setUser(getUser(currentUserId));
        favorite.setContract(getContract(contractId));
        favoriteContractRepository.save(favorite);
    }

    @Override
    public void unmarkFavorite(Integer contractId, Integer currentUserId) {
        favoriteContractRepository.deleteByUserIdAndContractId(currentUserId, contractId);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDownloadPayload downloadLatestFile(Integer contractId) {
        Contract contract = getContract(contractId);
        if (contract.getCurrentVersionId() == null) {
            throw new ResourceNotFoundException("Hợp đồng chưa có file đính kèm.");
        }

        ContractFile latestFile = contractFileRepository.findTopByVersionVersionIdOrderByUploadedAtDesc(
                        contract.getCurrentVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy file hợp đồng."));

        return new ContractDownloadPayload(
                fileStorageService.loadAsResource(latestFile.getFilePath()),
                latestFile.getFileName(),
                latestFile.getFileType());
    }

    private ContractSummaryResponse buildSummary(Contract contract, Integer currentUserId) {
        ContractVersion currentVersion = getCurrentVersion(contract);
        ContractFile latestFile = currentVersion == null
                ? null
                : contractFileRepository.findTopByVersionVersionIdOrderByUploadedAtDesc(currentVersion.getVersionId()).orElse(null);
        boolean favorite = currentUserId != null
                && favoriteContractRepository.existsByUserUserIdAndContractContractId(currentUserId, contract.getContractId());
        return contractMapper.toSummaryResponse(contract, currentVersion, latestFile, favorite);
    }

    private ContractDetailResponse buildDetail(Contract contract, Integer currentUserId) {
        List<ContractVersion> versions = contractVersionRepository.findByContractContractIdOrderByVersionNumberDesc(contract.getContractId());
        ContractVersion currentVersion = versions.stream()
                .filter(version -> version.getVersionId().equals(contract.getCurrentVersionId()))
                .findFirst()
                .orElse(null);
        List<ContractFile> files = contractFileRepository.findByVersionContractContractId(contract.getContractId())
                .stream()
                .sorted(Comparator.comparing(ContractFile::getUploadedAt).reversed())
                .toList();
        boolean favorite = currentUserId != null
                && favoriteContractRepository.existsByUserUserIdAndContractContractId(currentUserId, contract.getContractId());
        return contractMapper.toDetailResponse(contract, currentVersion, versions, files, favorite);
    }

    private ContractVersion createVersion(
            Contract contract,
            User currentUser,
            String changeNote,
            ContractVersionStatus versionStatus) {
        contractVersionRepository.clearCurrentVersionFlag(contract.getContractId());
        Integer nextVersionNumber = contractVersionRepository.findMaxVersionNumberByContractId(contract.getContractId()) + 1;

        ContractVersion version = new ContractVersion();
        version.setContract(contract);
        version.setVersionNumber(nextVersionNumber);
        version.setEditedBy(currentUser);
        version.setChangeNote(changeNote);
        version.setStatus(versionStatus);
        version.setIsCurrent(true);
        return contractVersionRepository.save(version);
    }

    private Contract getContract(Integer contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng."));
    }

    private User getUser(Integer userId) {
        return userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));
    }

    private DocumentType getDocumentType(Integer documentTypeId) {
        return documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại tài liệu."));
    }

    private Partner getPartner(Integer partnerId) {
        return partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đối tác."));
    }

    private ContractVersion getCurrentVersion(Contract contract) {
        if (contract.getCurrentVersionId() == null) {
            return null;
        }
        return contractVersionRepository.findById(contract.getCurrentVersionId()).orElse(null);
    }

    private void validateContractNumberForCreate(String contractNumber) {
        if (contractRepository.existsByContractNumberIgnoreCase(contractNumber.trim())) {
            throw new BadRequestException("Số hợp đồng đã tồn tại.");
        }
    }

    private void validateContractNumberForUpdate(String contractNumber, Integer contractId) {
        if (contractRepository.existsByContractNumberIgnoreCaseAndContractIdNot(contractNumber.trim(), contractId)) {
            throw new BadRequestException("Số hợp đồng đã tồn tại.");
        }
    }
}
