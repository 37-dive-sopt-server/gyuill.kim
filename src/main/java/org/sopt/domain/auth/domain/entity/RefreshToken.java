package org.sopt.domain.auth.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "refresh_tokens",
	indexes = {
		@Index(name = "idx_token", columnList = "token"),
		@Index(name = "idx_member_id", columnList = "memberId"),
		@Index(name = "idx_expiry_date", columnList = "expiryDate")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long memberId;

	@Column(nullable = false, unique = true, length = 512)
	private String token;

	@Column(nullable = false)
	private LocalDateTime expiryDate;

	@Column(nullable = false)
	private boolean isBlacklisted = false;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private RefreshToken(Long memberId, String token, LocalDateTime expiryDate) {
		this.memberId = memberId;
		this.token = token;
		this.expiryDate = expiryDate;
		this.isBlacklisted = false;
	}

	public static RefreshToken create(Long memberId, String token, LocalDateTime expiryDate) {
		return new RefreshToken(memberId, token, expiryDate);
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public void markAsBlacklisted() {
		this.isBlacklisted = true;
	}
}
