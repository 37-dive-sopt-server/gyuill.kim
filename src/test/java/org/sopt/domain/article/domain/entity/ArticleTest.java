package org.sopt.domain.article.domain.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.fixture.MemberFixture;

class ArticleTest {

	@Test
	@DisplayName("게시글 생성 - create 팩토리 메서드")
	void create_Success() {
		// given
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		String title = "Test Article";
		String content = "Test content for article";
		Tag tag = Tag.CS;

		// when
		Article article = Article.create(author, title, content, tag);

		// then
		assertThat(article.getAuthor()).isEqualTo(author);
		assertThat(article.getTitle()).isEqualTo(title);
		assertThat(article.getContent()).isEqualTo(content);
		assertThat(article.getTag()).isEqualTo(tag);
	}

	@Test
	@DisplayName("다양한 Tag로 게시글 생성")
	void create_VariousTags() {
		// given
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");

		// when
		Article csArticle = Article.create(author, "CS Article", "Content", Tag.CS);
		Article dbArticle = Article.create(author, "DB Article", "Content", Tag.DB);
		Article springArticle = Article.create(author, "Spring Article", "Content", Tag.SPRING);
		Article etcArticle = Article.create(author, "ETC Article", "Content", Tag.ETC);

		// then
		assertThat(csArticle.getTag()).isEqualTo(Tag.CS);
		assertThat(dbArticle.getTag()).isEqualTo(Tag.DB);
		assertThat(springArticle.getTag()).isEqualTo(Tag.SPRING);
		assertThat(etcArticle.getTag()).isEqualTo(Tag.ETC);
	}

	@Test
	@DisplayName("content가 null인 게시글 생성 가능")
	void create_WithNullContent() {
		// given
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		String title = "Article Without Content";

		// when
		Article article = Article.create(author, title, null, Tag.CS);

		// then
		assertThat(article.getAuthor()).isEqualTo(author);
		assertThat(article.getTitle()).isEqualTo(title);
		assertThat(article.getContent()).isNull();
		assertThat(article.getTag()).isEqualTo(Tag.CS);
	}

	@Test
	@DisplayName("긴 content로 게시글 생성")
	void create_WithLongContent() {
		// given
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		String longContent = "A".repeat(1000); // TEXT 타입이므로 긴 내용 가능

		// when
		Article article = Article.create(author, "Long Article", longContent, Tag.CS);

		// then
		assertThat(article.getContent()).isEqualTo(longContent);
		assertThat(article.getContent()).hasSize(1000);
	}
}
