package com.idp.idpapi.storage;

public record StoredFileResult(
        String originalFileName,
        String storedFileName,
        String storedPath,
        String contentType,
        long fileSize,
        String hash) {
}
