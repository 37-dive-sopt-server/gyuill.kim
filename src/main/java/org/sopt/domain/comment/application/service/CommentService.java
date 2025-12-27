package org.sopt.domain.comment.application.service;

import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.repository.ArticleRepository;
import org.sopt.domain.article.exception.ArticleException;
import org.sopt.domain.comment.application.dto.request.CommentCreateRequest;
import org.sopt.domain.comment.application.dto.request.CommentUpdateRequest;
import org.sopt.domain.comment.application.dto.response.CommentResponse;
import org.sopt.domain.comment.domain.entity.Comment;
import org.sopt.domain.comment.domain.repository.CommentRepository;
import org.sopt.domain.comment.exception.CommentException;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.sopt.domain.member.exception.MemberException;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final ArticleRepository articleRepository;
	private final MemberRepository memberRepository;

	@Transactional
	@CacheEvict(value = "articles", key = "#request.articleId")
	public CommentResponse create(Long authorId, CommentCreateRequest request) {
		Article article = articleRepository.findById(request.articleId())
			.orElseThrow(() -> new ArticleException(ErrorCode.ARTICLE_NOT_FOUND));

		Member author = memberRepository.findById(authorId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		Comment comment = Comment.create(article, author, request.content());
		commentRepository.save(comment);

		return CommentResponse.fromEntity(comment);
	}

	public CommentResponse getCommentById(Long commentId) {
		Comment comment = commentRepository.findByIdWithDetails(commentId)
			.orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND));
		return CommentResponse.fromEntity(comment);
	}

	public Page<CommentResponse> getMyComments(Long authorId, Pageable pageable) {
		return commentRepository.findByAuthorIdWithArticle(authorId, pageable)
			.map(CommentResponse::fromEntity);
	}

	@Transactional
	@CacheEvict(value = "articles", key = "#result.articleId")
	public CommentResponse update(Long commentId, Long requesterId, CommentUpdateRequest request) {
		Comment comment = commentRepository.findByIdWithDetails(commentId)
			.orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND));

		comment.validateAuthor(requesterId);

		comment.updateContent(request.content());

		return CommentResponse.fromEntity(comment);
	}

	@Transactional
	public void delete(Long commentId, Long requesterId) {
		Comment comment = commentRepository.findByIdWithAuthor(commentId)
			.orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND));

		comment.validateAuthor(requesterId);

		Long articleId = comment.getArticle().getId();
		commentRepository.delete(comment);

		// 캐시 수동 무효화 (void 메서드라 @CacheEvict 사용 불가)
		evictArticleCache(articleId);
	}

	@CacheEvict(value = "articles", key = "#articleId")
	public void evictArticleCache(Long articleId) {
		// 캐시 무효화만 수행
	}
}
