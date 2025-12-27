package org.sopt.fixture;

import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.comment.domain.entity.Comment;
import org.sopt.domain.member.domain.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class CommentFixture {

	private static final String DEFAULT_CONTENT = "Test comment content";

	/**
	 * 기본 댓글 생성 (기본 content 사용)
	 */
	public static Comment createComment(Article article, Member author) {
		return createCommentWithContent(article, author, DEFAULT_CONTENT);
	}

	/**
	 * 커스텀 content로 댓글 생성
	 */
	public static Comment createCommentWithContent(Article article, Member author, String content) {
		return Comment.create(article, author, content);
	}

	/**
	 * ID를 가진 댓글 생성 (기본 content 사용)
	 */
	public static Comment createCommentWithId(Long id, Article article, Member author) {
		return createCommentWithIdAndContent(id, article, author, DEFAULT_CONTENT);
	}

	/**
	 * ID와 커스텀 content로 댓글 생성
	 */
	public static Comment createCommentWithIdAndContent(Long id, Article article, Member author, String content) {
		Comment comment = createCommentWithContent(article, author, content);
		ReflectionTestUtils.setField(comment, "id", id);
		return comment;
	}
}
