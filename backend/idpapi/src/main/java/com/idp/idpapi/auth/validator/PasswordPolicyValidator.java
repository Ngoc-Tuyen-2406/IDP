package com.idp.idpapi.auth.validator;

import org.springframework.stereotype.Component;

import com.idp.idpapi.common.exception.BadRequestException;

@Component
public class PasswordPolicyValidator {

    public void validate(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (!password.chars().anyMatch(Character::isUpperCase)) {
            throw new BadRequestException("Mật khẩu phải chứa ít nhất 1 ký tự in hoa.");
        }
        if (!password.chars().anyMatch(Character::isLowerCase)) {
            throw new BadRequestException("Mật khẩu phải chứa ít nhất 1 ký tự in thường.");
        }
        if (!password.chars().anyMatch(Character::isDigit)) {
            throw new BadRequestException("Mật khẩu phải chứa ít nhất 1 chữ số.");
        }
    }
}
