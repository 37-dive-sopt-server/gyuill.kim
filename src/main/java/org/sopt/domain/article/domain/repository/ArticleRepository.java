package org.sopt.domain.article.domain.repository;

import org.sopt.domain.article.domain.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {
	boolean existsByTitle(String title);

	@Query(value = "SELECT a FROM Article a JOIN FETCH a.author",
		   countQuery = "SELECT COUNT(a) FROM Article a")
	Page<Article> findAllWithAuthor(Pageable pageable);

	@Query(value = "SELECT a FROM Article a JOIN FETCH a.author WHERE a.title LIKE %:keyword% OR a.author.name LIKE %:keyword%",
		   countQuery = "SELECT COUNT(a) FROM Article a WHERE a.title LIKE %:keyword% OR a.author.name LIKE %:keyword%")
	Page<Article> findByTitleOrAuthorNameContaining(@Param("keyword") String keyword, Pageable pageable);
}
