package org.sopt.domain.comment.domain.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.comment.exception.CommentException;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.fixture.ArticleFixture;
import org.sopt.fixture.CommentFixture;
import org.sopt.fixture.MemberFixture;
import org.sopt.global.response.error.ErrorCode;

class CommentTest {

	private Member author;
	private Member otherUser;
	private Article article;

	@BeforeEach
	void setUp() {
		author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		otherUser = MemberFixture.createMemberWithId(2L, "other@example.com", "Other User");
		article = ArticleFixture.createArticleWithId(100L, author, "Test Article");
	}

	@Test
	@DisplayName("댓글 생성 성공")
	void create_Success() {
		// when
		Comment comment = Comment.create(article, author, "Test comment content");

		// then
		assertThat(comment.getArticle()).isEqualTo(article);
		assertThat(comment.getAuthor()).isEqualTo(author);
		assertThat(comment.getContent()).isEqualTo("Test comment content");
	}

	@Test
	@DisplayName("댓글 내용 수정 성공")
	void updateContent_Success() {
		// given
		Comment comment = CommentFixture.createCommentWithContent(article, author, "Original content");

		// when
		comment.updateContent("Updated content");

		// then
		assertThat(comment.getContent()).isEqualTo("Updated content");
	}

	@Test
	@DisplayName("작성자 본인 확인 - 본인인 경우 true")
	void isAuthor_SameUser_ReturnsTrue() {
		// given
		Comment comment = CommentFixture.createComment(article, author);

		// when
		boolean result = comment.isAuthor(author.getId());

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("작성자 본인 확인 - 타인인 경우 false")
	void isAuthor_DifferentUser_ReturnsFalse() {
		// given
		Comment comment = CommentFixture.createComment(article, author);

		// when
		boolean result = comment.isAuthor(otherUser.getId());

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("작성자가 아닌 사용자의 수정/삭제 시도 시 예외 발생")
	void validateAuthor_Unauthorized_ThrowsException() {
		// given
		Comment comment = CommentFixture.createComment(article, author);

		// when & then
		assertThatThrownBy(() -> comment.validateAuthor(otherUser.getId()))
			.isInstanceOf(CommentException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_UNAUTHORIZED);
	}

	@Test
	@DisplayName("작성자 본인의 validateAuthor 호출 시 예외 없음")
	void validateAuthor_Authorized_NoException() {
		// given
		Comment comment = CommentFixture.createComment(article, author);

		// when & then
		assertThatNoException().isThrownBy(() ->
			comment.validateAuthor(author.getId())
		);
	}

	@Test
	@DisplayName("댓글 내용을 빈 문자열로 수정")
	void updateContent_EmptyString() {
		// given
		Comment comment = CommentFixture.createCommentWithContent(article, author, "Original content");

		// when
		comment.updateContent("");

		// then
		assertThat(comment.getContent()).isEmpty();
	}

	@Test
	@DisplayName("같은 내용으로 댓글 수정")
	void updateContent_SameContent() {
		// given
		String originalContent = "Same content";
		Comment comment = CommentFixture.createCommentWithContent(article, author, originalContent);

		// when
		comment.updateContent("Same content");

		// then
		assertThat(comment.getContent()).isEqualTo(originalContent);
	}
}
