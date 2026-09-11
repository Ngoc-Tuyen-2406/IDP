package com.idp.idpapi.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String contractDir = "storage/contracts";

    public String getContractDir() {
        return contractDir;
    }

    public void setContractDir(String contractDir) {
        this.contractDir = contractDir;
    }
}
