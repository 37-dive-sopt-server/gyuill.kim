package org.sopt.domain.article.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.domain.article.application.dto.ArticleCreateRequest;
import org.sopt.domain.article.application.dto.ArticleResponse;
import org.sopt.domain.article.application.service.ArticleService;
import org.sopt.global.annotation.ApiExceptions;
import org.sopt.global.annotation.AutoApiResponse;
import org.sopt.global.annotation.SuccessCodeAnnotation;
import org.sopt.global.response.error.ErrorCode;
import org.sopt.global.response.success.SuccessCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
@AutoApiResponse
@Tag(name = "Article", description = "게시글 관리 API")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    @SuccessCodeAnnotation(SuccessCode.ARTICLE_CREATED)
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @ApiExceptions({ErrorCode.MEMBER_NOT_FOUND, ErrorCode.INVALID_INPUT, ErrorCode.INVALID_FORMAT})
    public ArticleResponse createArticle(
            @Parameter(description = "게시글 작성 정보", required = true)
            @Valid @RequestBody ArticleCreateRequest request
    ) {
        return articleService.create(request);
    }

    @GetMapping("/{id}")
    @SuccessCodeAnnotation(SuccessCode.ARTICLE_VIEW)
    @Operation(summary = "게시글 조회", description = "ID로 특정 게시글의 정보를 조회합니다.")
    @ApiExceptions({ErrorCode.ARTICLE_NOT_FOUND})
    public ArticleResponse getArticleById(
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        return articleService.getArticleById(id);
    }

    @GetMapping
    @SuccessCodeAnnotation(SuccessCode.ARTICLE_VIEW)
    @Operation(summary = "전체 게시글 조회", description = "등록된 모든 게시글을 페이징하여 조회합니다.")
    public Page<ArticleResponse> getAllArticles(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return articleService.findAllArticles(pageable);
    }
}
