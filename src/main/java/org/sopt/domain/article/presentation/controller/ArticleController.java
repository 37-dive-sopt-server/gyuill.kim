package org.sopt.domain.article.presentation.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/articles")
@AutoApiResponse
@RequiredArgsConstructor
@Tag(name = "Article", description = "게시글 관리 API")
public class ArticleController {

	private final ArticleService articleService;

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
	@Operation(summary = "게시글 조회 및 검색", description = "등록된 게시글을 조회합니다. keyword를 입력하면 제목 또는 작성자 이름으로 검색합니다.")
	public Page<ArticleResponse> getAllArticles(
		@Parameter(description = "검색 키워드 (제목 또는 작성자 이름)", example = "Spring")
		@RequestParam(required = false) String keyword,
		@PageableDefault(size = 20) Pageable pageable
	) {
		return articleService.getArticles(keyword, pageable);
	}
}
