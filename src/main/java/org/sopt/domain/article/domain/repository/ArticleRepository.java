package org.sopt.domain.article.domain.repository;

import org.sopt.domain.article.domain.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
