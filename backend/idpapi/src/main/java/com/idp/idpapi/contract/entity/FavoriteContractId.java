package com.idp.idpapi.contract.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class FavoriteContractId implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "contract_id")
    private Integer contractId;

    public FavoriteContractId() {
    }

    public FavoriteContractId(Integer userId, Integer contractId) {
        this.userId = userId;
        this.contractId = contractId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getContractId() {
        return contractId;
    }

    public void setContractId(Integer contractId) {
        this.contractId = contractId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof FavoriteContractId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(contractId, that.contractId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, contractId);
    }
}
