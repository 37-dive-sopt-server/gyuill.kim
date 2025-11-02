package org.sopt.domain.member.domain.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Member(String name, LocalDate birthDate, String email, Gender gender) {
        validateBirthDate(birthDate);
        this.name = name;
        this.birthDate = birthDate;
        this.email = email;
        this.gender = gender;
    }

    public static Member create(String name, LocalDate birthDate, String email, Gender gender) {
        return new Member(name, birthDate, email, gender);
    }

    private void validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("생년월일을 입력해주세요");
        }
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("생년월일은 과거 날짜여야 합니다");
        }
        int age = LocalDate.now().getYear() - birthDate.getYear();
        if (age < 20) {
            throw new IllegalArgumentException("20세 미만은 회원 가입이 불가능합니다");
        }
    }
}
