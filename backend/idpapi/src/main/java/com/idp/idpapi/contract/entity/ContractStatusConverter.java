package com.idp.idpapi.contract.entity;

import com.idp.idpapi.contract.enums.ContractStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ContractStatusConverter implements AttributeConverter<ContractStatus, String> {

    @Override
    public String convertToDatabaseColumn(ContractStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ContractStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ContractStatus.fromValue(dbData);
    }
}
