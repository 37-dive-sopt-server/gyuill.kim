package org.sopt.fixture;

import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.entity.Tag;
import org.sopt.domain.member.domain.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public class ArticleFixture {

	public static Article createArticle(Member author, String title) {
		return Article.create(author, title, "Test content for article", Tag.CS);
	}

	public static Article createArticleWithTag(Member author, String title, Tag tag) {
		return Article.create(author, title, "Test content for article", tag);
	}

	public static Article createArticleWithContent(Member author, String title, String content) {
		return Article.create(author, title, content, Tag.CS);
	}

	public static Article createArticleWithId(Long id, Member author, String title) {
		Article article = createArticle(author, title);
		ReflectionTestUtils.setField(article, "id", id);
		return article;
	}

	public static Article createArticleWithIdAndTag(Long id, Member author, String title, Tag tag) {
		Article article = createArticleWithTag(author, title, tag);
		ReflectionTestUtils.setField(article, "id", id);
		return article;
	}
}
