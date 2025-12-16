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
}
