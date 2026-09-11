package com.idp.idpapi.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.common.exception.ResourceNotFoundException;

@Service
public class FileStorageService {

    private final StorageProperties storageProperties;

    public FileStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public StoredFileResult storeContractFile(MultipartFile file, String contractNumber) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn file hợp đồng.");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = extractExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);

        Path contractDirectory = resolveContractDirectory(contractNumber);
        Path targetPath = contractDirectory.resolve(storedFileName).normalize();

        try {
            Files.createDirectories(contractDirectory);
            String hash = copyAndHash(file.getInputStream(), targetPath);
            return new StoredFileResult(
                    originalFileName,
                    storedFileName,
                    targetPath.toString(),
                    file.getContentType(),
                    file.getSize(),
                    hash);
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể lưu file hợp đồng.", exception);
        }
    }

    public Resource loadAsResource(String storedPath) {
        Path path = Path.of(storedPath).normalize();
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Không tìm thấy file hợp đồng.");
        }
        return new FileSystemResource(path);
    }

    public void deleteIfExists(String storedPath) {
        if (!StringUtils.hasText(storedPath)) {
            return;
        }

        try {
            Files.deleteIfExists(Path.of(storedPath).normalize());
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể xóa file hợp đồng đã lưu.", exception);
        }
    }

    private Path resolveContractDirectory(String contractNumber) {
        String safeFolder = contractNumber == null
                ? "general"
                : contractNumber.replaceAll("[^a-zA-Z0-9-_]", "_");
        return Path.of(storageProperties.getContractDir()).resolve(safeFolder).toAbsolutePath().normalize();
    }

    private String copyAndHash(InputStream inputStream, Path targetPath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest)) {
                Files.copy(digestInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
        }
    }

    private String extractExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
