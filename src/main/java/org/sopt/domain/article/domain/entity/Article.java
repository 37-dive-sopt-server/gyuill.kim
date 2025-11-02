package org.sopt.domain.article.domain.entity;

import org.sopt.domain.member.domain.entity.Member;
import org.sopt.global.entity.BaseTimeEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "article", indexes = {
	@Index(name = "idx_article_title", columnList = "title")
})
public class Article extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Member author;

	private Tag tag;
	private String title;
	private String content;

	private Article(Member author, String title, String content, Tag tag) {
		this.author = author;
		this.title = title;
		this.content = content;
		this.tag = tag;
	}

	public static Article create(Member author, String title, String content, Tag tag) {
		return new Article(author, title, content, tag);
	}
}
