package org.sopt.domain.member.domain.entity;

import java.time.LocalDate;

import org.sopt.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", indexes = {
	@Index(name = "idx_member_name", columnList = "name")
})
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Member(String password, String name, LocalDate birthDate, String email, Gender gender) {
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.email = email;
        this.gender = gender;
    }

    public static Member create(String password, String name, LocalDate birthDate, String email, Gender gender) {
        return new Member(password, name, birthDate, email, gender);
    }
}
