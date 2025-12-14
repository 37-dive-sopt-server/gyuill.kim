package org.sopt.fixture;

import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.comment.domain.entity.Comment;
import org.sopt.domain.member.domain.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class CommentFixture {

	public static Comment createComment(Article article, Member author, String content) {
		return Comment.create(article, author, content);
	}

	public static Comment createComment(Article article, Member author) {
		return Comment.create(article, author, "Test comment content");
	}

	public static Comment createCommentWithId(Long id, Article article, Member author) {
		Comment comment = createComment(article, author);
		ReflectionTestUtils.setField(comment, "id", id);
		return comment;
	}

	public static Comment createCommentWithId(Long id, Article article, Member author, String content) {
		Comment comment = createComment(article, author, content);
		ReflectionTestUtils.setField(comment, "id", id);
		return comment;
	}
}
