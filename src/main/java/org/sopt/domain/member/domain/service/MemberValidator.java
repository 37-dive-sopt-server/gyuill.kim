package org.sopt.domain.member.domain.service;

import java.time.LocalDate;

import org.sopt.domain.member.domain.entity.Gender;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.exception.MemberException;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class MemberValidator {

    public void validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new MemberException(ErrorCode.BIRTH_DATE_REQUIRED);
        }
        if (birthDate.isAfter(LocalDate.now())) {
            throw new MemberException(ErrorCode.BIRTH_DATE_FUTURE);
        }
        int age = LocalDate.now().getYear() - birthDate.getYear();
        if (age < 20) {
            throw new MemberException(ErrorCode.AGE_UNDER_20);
        }
    }

    public Member createValidatedMember(String name, LocalDate birthDate, String email, Gender gender) {
        validateBirthDate(birthDate);
        return Member.create(name, birthDate, email, gender);
    }
}
