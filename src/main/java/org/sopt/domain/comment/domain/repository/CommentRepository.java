package org.sopt.domain.comment.domain.repository;

import java.util.List;
import java.util.Optional;

import org.sopt.domain.comment.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	// 단일 댓글 조회 (작성자 정보 포함)
	@Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.id = :id")
	Optional<Comment> findByIdWithAuthor(@Param("id") Long id);

	// 단일 댓글 상세 조회 (게시글 + 작성자)
	@Query("SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.article WHERE c.id = :id")
	Optional<Comment> findByIdWithDetails(@Param("id") Long id);

	// 내가 작성한 댓글 목록 (페이지네이션)
	@Query(value = "SELECT c FROM Comment c JOIN FETCH c.article WHERE c.author.id = :authorId",
		countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.author.id = :authorId")
	Page<Comment> findByAuthorIdWithArticle(@Param("authorId") Long authorId, Pageable pageable);

	// 게시글별 댓글 목록 (ArticleResponse에 포함용)
	@Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.article.id = :articleId")
	List<Comment> findByArticleIdWithAuthor(@Param("articleId") Long articleId);
}
