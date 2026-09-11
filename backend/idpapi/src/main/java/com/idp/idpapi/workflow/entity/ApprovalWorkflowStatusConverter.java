package com.idp.idpapi.workflow.entity;

import com.idp.idpapi.workflow.enums.ApprovalWorkflowStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ApprovalWorkflowStatusConverter implements AttributeConverter<ApprovalWorkflowStatus, String> {

    @Override
    public String convertToDatabaseColumn(ApprovalWorkflowStatus attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public ApprovalWorkflowStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? ApprovalWorkflowStatus.fromValue(dbData) : null;
    }
}
