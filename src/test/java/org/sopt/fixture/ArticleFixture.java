package org.sopt.fixture;

import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.entity.Tag;
import org.sopt.domain.member.domain.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class ArticleFixture {

	private static final String DEFAULT_CONTENT = "Test content for article";
	private static final Tag DEFAULT_TAG = Tag.CS;

	public static Article createArticle(Member author, String title) {
		return Article.create(author, title, DEFAULT_CONTENT, DEFAULT_TAG);
	}

	public static Article createArticleWithTag(Member author, String title, Tag tag) {
		return Article.create(author, title, DEFAULT_CONTENT, tag);
	}

	public static Article createArticleWithId(Long id, Member author, String title) {
		Article article = createArticle(author, title);
		ReflectionTestUtils.setField(article, "id", id);
		return article;
	}
}
