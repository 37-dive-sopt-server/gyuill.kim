package org.sopt.domain.article.application.service;

import org.sopt.domain.article.application.dto.ArticleCreateRequest;
import org.sopt.domain.article.application.dto.ArticleResponse;
import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.repository.ArticleRepository;
import org.sopt.domain.article.exception.ArticleException;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.domain.repository.MemberRepository;
import org.sopt.domain.member.exception.MemberException;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    public ArticleService(ArticleRepository articleRepository, MemberRepository memberRepository) {
        this.articleRepository = articleRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {

        if(articleRepository.existsByTitle(request.title())) {
            throw new ArticleException(ErrorCode.DUPLICATE_ARTICLE_TITLE);
        }

        Member author = memberRepository.findById(request.authorId())
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
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleException(ErrorCode.ARTICLE_NOT_FOUND));
        return ArticleResponse.fromEntity(article);
    }

    public Page<ArticleResponse> findAllArticles(Pageable pageable) {
        return articleRepository.findAllWithAuthor(pageable)
                .map(ArticleResponse::fromEntity);
    }

    public Page<ArticleResponse> searchArticles(String keyword, Pageable pageable) {
        return articleRepository.findByTitleOrAuthorNameContaining(keyword, pageable)
                .map(ArticleResponse::fromEntity);
    }
}
