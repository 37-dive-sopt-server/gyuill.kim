package org.sopt.domain.comment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.global.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment", indexes = {
	@Index(name = "idx_comment_article", columnList = "article_id"),
	@Index(name = "idx_comment_author", columnList = "author_id")
})
public class Comment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "article_id", nullable = false)
	private Article article;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private Member author;

	@Column(nullable = false, length = 300)
	private String content;

	private Comment(Article article, Member author, String content) {
		this.article = article;
		this.author = author;
		this.content = content;
	}

	public static Comment create(Article article, Member author, String content) {
		return new Comment(article, author, content);
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public boolean isAuthor(Long memberId) {
		return this.author.getId().equals(memberId);
	}
}
