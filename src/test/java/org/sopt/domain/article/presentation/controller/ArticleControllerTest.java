package org.sopt.domain.article.presentation.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.config.MockArgumentResolver;
import org.sopt.domain.article.application.dto.ArticleCreateRequest;
import org.sopt.domain.article.application.dto.ArticleResponse;
import org.sopt.domain.article.application.service.ArticleService;
import org.sopt.domain.article.domain.entity.Article;
import org.sopt.domain.article.domain.entity.Tag;
import org.sopt.domain.article.exception.ArticleException;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.fixture.ArticleFixture;
import org.sopt.fixture.MemberFixture;
import org.sopt.global.exception.GlobalExceptionHandler;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class ArticleControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@InjectMocks
	private ArticleController articleController;

	@Mock
	private ArticleService articleService;

	@BeforeEach
	void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		mockMvc = MockMvcBuilders
			.standaloneSetup(articleController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.setCustomArgumentResolvers(
				new MockArgumentResolver(),
				new PageableHandlerMethodArgumentResolver()
			)
			.build();
	}

	@Test
	@DisplayName("게시글 작성 성공")
	void createArticle_Success() throws Exception {
		// given - MockArgumentResolver가 memberId=1L인 CustomUserDetails 주입
		ArticleCreateRequest request = new ArticleCreateRequest(
			"Test Article",
			"Test content for article",
			Tag.CS
		);

		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(100L, author, request.title());
		ArticleResponse response = ArticleResponse.fromEntity(article);

		given(articleService.create(eq(1L), any(ArticleCreateRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/articles")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Test Article"))
			.andExpect(jsonPath("$.authorName").value("Author"));

		verify(articleService).create(eq(1L), any(ArticleCreateRequest.class));
	}

	@Test
	@DisplayName("게시글 작성 실패 - 잘못된 요청 (빈 제목)")
	void createArticle_InvalidTitle() throws Exception {
		// given
		ArticleCreateRequest request = new ArticleCreateRequest(
			"",  // 빈 제목
			"Test content",
			Tag.CS
		);

		// when & then
		mockMvc.perform(post("/articles")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(articleService, never()).create(anyLong(), any());
	}

	@Test
	@DisplayName("게시글 작성 실패 - 제목 중복")
	void createArticle_DuplicateTitle() throws Exception {
		// given
		ArticleCreateRequest request = new ArticleCreateRequest(
			"Duplicate Title",
			"Test content",
			Tag.CS
		);

		given(articleService.create(eq(1L), any(ArticleCreateRequest.class)))
			.willThrow(new ArticleException(ErrorCode.DUPLICATE_ARTICLE_TITLE));

		// when & then
		mockMvc.perform(post("/articles")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(articleService).create(eq(1L), any(ArticleCreateRequest.class));
	}

	@Test
	@DisplayName("게시글 조회 성공")
	void getArticleById_Success() throws Exception {
		// given
		Long articleId = 1L;
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(articleId, author, "Test Article");
		ArticleResponse response = ArticleResponse.fromEntity(article);

		given(articleService.getArticleById(articleId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/articles/{id}", articleId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(articleId))
			.andExpect(jsonPath("$.title").value("Test Article"))
			.andExpect(jsonPath("$.authorName").value("Author"));

		verify(articleService).getArticleById(articleId);
	}

	@Test
	@DisplayName("게시글 조회 실패 - 존재하지 않는 게시글")
	void getArticleById_NotFound() throws Exception {
		// given
		Long articleId = 999L;
		given(articleService.getArticleById(articleId))
			.willThrow(new ArticleException(ErrorCode.ARTICLE_NOT_FOUND));

		// when & then
		mockMvc.perform(get("/articles/{id}", articleId))
			.andExpect(status().isNotFound());

		verify(articleService).getArticleById(articleId);
	}

	@Test
	@DisplayName("게시글 목록 조회 - 검색어 없음")
	void getAllArticles_WithoutKeyword() throws Exception {
		// given
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article1 = ArticleFixture.createArticleWithId(1L, author, "Article 1");
		Article article2 = ArticleFixture.createArticleWithId(2L, author, "Article 2");

		ArticleResponse response1 = ArticleResponse.fromEntity(article1);
		ArticleResponse response2 = ArticleResponse.fromEntity(article2);

		Pageable pageable = PageRequest.of(0, 20);
		Page<ArticleResponse> page = new PageImpl<>(List.of(response1, response2), pageable, 2);

		given(articleService.getArticles(isNull(), any(Pageable.class))).willReturn(page);

		// when & then
		mockMvc.perform(get("/articles")
				.param("page", "0")
				.param("size", "20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.content[0].title").value("Article 1"))
			.andExpect(jsonPath("$.content[1].title").value("Article 2"))
			.andExpect(jsonPath("$.totalElements").value(2));

		verify(articleService).getArticles(isNull(), any(Pageable.class));
	}

	@Test
	@DisplayName("게시글 검색 - 키워드로 검색")
	void getAllArticles_WithKeyword() throws Exception {
		// given
		String keyword = "Spring";
		Member author = MemberFixture.createMemberWithId(1L, "author@example.com", "Author");
		Article article = ArticleFixture.createArticleWithId(1L, author, "Spring Boot Tutorial");

		ArticleResponse response = ArticleResponse.fromEntity(article);

		Pageable pageable = PageRequest.of(0, 20);
		Page<ArticleResponse> page = new PageImpl<>(List.of(response), pageable, 1);

		given(articleService.getArticles(eq(keyword), any(Pageable.class))).willReturn(page);

		// when & then
		mockMvc.perform(get("/articles")
				.param("keyword", keyword)
				.param("page", "0")
				.param("size", "20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].title").value("Spring Boot Tutorial"))
			.andExpect(jsonPath("$.totalElements").value(1));

		verify(articleService).getArticles(eq(keyword), any(Pageable.class));
	}
}
