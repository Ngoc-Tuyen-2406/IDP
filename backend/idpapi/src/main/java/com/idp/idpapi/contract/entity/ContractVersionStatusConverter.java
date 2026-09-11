package com.idp.idpapi.contract.entity;

import com.idp.idpapi.contract.enums.ContractVersionStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ContractVersionStatusConverter implements AttributeConverter<ContractVersionStatus, String> {

    @Override
    public String convertToDatabaseColumn(ContractVersionStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ContractVersionStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ContractVersionStatus.fromValue(dbData);
    }
}
