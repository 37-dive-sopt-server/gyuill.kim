package org.sopt.domain.article.application.service;

import java.util.List;

import org.sopt.domain.article.application.dto.ArticleCreateRequest;
import org.sopt.domain.article.application.dto.ArticleResponse;
import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.repository.ArticleRepository;
import org.sopt.domain.article.exception.ArticleException;
import org.sopt.domain.comment.domain.entity.Comment;
import org.sopt.domain.comment.domain.repository.CommentRepository;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.sopt.domain.member.exception.MemberException;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleService {

	private final ArticleRepository articleRepository;
	private final MemberRepository memberRepository;
	private final CommentRepository commentRepository;

	@Transactional
	public ArticleResponse create(Long authorId, ArticleCreateRequest request) {

		if (articleRepository.existsByTitle(request.title())) {
			throw new ArticleException(ErrorCode.DUPLICATE_ARTICLE_TITLE);
		}

		Member author = memberRepository.findById(authorId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		Article article = Article.create(
			author,
			request.title(),
			request.content(),
			request.tag()
		);

		articleRepository.save(article);

		return ArticleResponse.fromEntity(article);
	}

	public ArticleResponse getArticleById(Long articleId) {
		Article article = articleRepository.findByIdWithAuthor(articleId)
			.orElseThrow(() -> new ArticleException(ErrorCode.ARTICLE_NOT_FOUND));

		// 댓글 목록 조회 (JOIN FETCH로 작성자 정보 포함)
		List<Comment> comments = commentRepository.findByArticleIdWithAuthor(articleId);

		return ArticleResponse.fromEntityWithComments(article, comments);
	}

	private Page<ArticleResponse> findAllArticles(Pageable pageable) {
		return articleRepository.findAllWithAuthor(pageable)
			.map(ArticleResponse::fromEntity);
	}

	private Page<ArticleResponse> searchArticles(String keyword, Pageable pageable) {
		return articleRepository.findByTitleOrAuthorNameContaining(keyword, pageable)
			.map(ArticleResponse::fromEntity);
	}

	public Page<ArticleResponse> getArticles(String keyword, Pageable pageable) {
		if (keyword != null && !keyword.trim().isEmpty()) {
			return searchArticles(keyword, pageable);
		}
		return findAllArticles(pageable);
	}
}
