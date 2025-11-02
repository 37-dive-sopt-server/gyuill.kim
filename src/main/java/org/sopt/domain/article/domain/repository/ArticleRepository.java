package org.sopt.domain.article.domain.repository;

import org.sopt.domain.article.domain.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {
	boolean existsByTitle(String title);

	// 제목 또는 작성자 이름으로 검색 (하나의 키워드로 둘 다 검색)
	@Query("SELECT a FROM Article a WHERE a.title LIKE %:keyword% OR a.author.name LIKE %:keyword%")
	Page<Article> findByTitleOrAuthorNameContaining(@Param("keyword") String keyword, Pageable pageable);
}
