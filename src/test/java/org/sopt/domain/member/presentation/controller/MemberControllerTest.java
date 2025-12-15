package org.sopt.domain.member.presentation.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.config.MockArgumentResolver;
import org.sopt.domain.member.application.dto.MemberCreateRequest;
import org.sopt.domain.member.application.dto.MemberResponse;
import org.sopt.domain.member.application.service.MemberService;
import org.sopt.domain.member.domain.entity.Gender;
import org.sopt.domain.member.domain.entity.Member;
import org.sopt.domain.member.exception.MemberException;
import org.sopt.fixture.MemberFixture;
import org.sopt.global.exception.GlobalExceptionHandler;
import org.sopt.global.response.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@InjectMocks
	private MemberController memberController;

	@Mock
	private MemberService memberService;

	@BeforeEach
	void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		mockMvc = MockMvcBuilders
			.standaloneSetup(memberController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.setCustomArgumentResolvers(
				new MockArgumentResolver(),
				new PageableHandlerMethodArgumentResolver()
			)
			.build();
	}

	@Test
	@DisplayName("회원 가입 성공")
	void createMember_Success() throws Exception {
		// given
		MemberCreateRequest request = new MemberCreateRequest(
			"Test User",
			"password123!",
			LocalDate.of(2000, 1, 1),
			"test@example.com",
			Gender.MALE
		);

		Member member = MemberFixture.createMemberWithId(1L, "test@example.com", "Test User");
		MemberResponse response = MemberResponse.fromEntity(member);

		given(memberService.create(any(MemberCreateRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("test@example.com"))
			.andExpect(jsonPath("$.name").value("Test User"));

		verify(memberService).create(any(MemberCreateRequest.class));
	}

	@Test
	@DisplayName("회원 가입 실패 - 잘못된 요청 (이메일 형식)")
	void createMember_InvalidEmail() throws Exception {
		// given
		MemberCreateRequest request = new MemberCreateRequest(
			"Test User",
			"password123!",
			LocalDate.of(2000, 1, 1),
			"invalid-email",  // 잘못된 이메일
			Gender.MALE
		);

		// when & then
		mockMvc.perform(post("/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(memberService, never()).create(any());
	}

	@Test
	@DisplayName("회원 가입 실패 - 이메일 중복")
	void createMember_DuplicateEmail() throws Exception {
		// given
		MemberCreateRequest request = new MemberCreateRequest(
			"Test User",
			"password123!",
			LocalDate.of(2000, 1, 1),
			"duplicate@example.com",
			Gender.MALE
		);

		given(memberService.create(any(MemberCreateRequest.class)))
			.willThrow(new MemberException(ErrorCode.DUPLICATE_EMAIL));

		// when & then
		mockMvc.perform(post("/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(memberService).create(any(MemberCreateRequest.class));
	}

	@Test
	@DisplayName("회원 가입 실패 - 20세 미만")
	void createMember_Under20() throws Exception {
		// given
		MemberCreateRequest request = new MemberCreateRequest(
			"Young User",
			"password123!",
			LocalDate.now().minusYears(19),
			"young@example.com",
			Gender.MALE
		);

		given(memberService.create(any(MemberCreateRequest.class)))
			.willThrow(new MemberException(ErrorCode.AGE_UNDER_20));

		// when & then
		mockMvc.perform(post("/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(memberService).create(any(MemberCreateRequest.class));
	}

	@Test
	@DisplayName("전체 회원 조회 - 페이징")
	void getAllMembers_Success() throws Exception {
		// given
		Member member1 = MemberFixture.createMemberWithId(1L, "user1@example.com", "User1");
		Member member2 = MemberFixture.createMemberWithId(2L, "user2@example.com", "User2", Gender.FEMALE);

		MemberResponse response1 = MemberResponse.fromEntity(member1);
		MemberResponse response2 = MemberResponse.fromEntity(member2);

		Page<MemberResponse> page = new PageImpl<>(List.of(response1, response2), PageRequest.of(0, 20), 2);

		given(memberService.findAllMembers(any())).willReturn(page);

		// when & then
		mockMvc.perform(get("/members")
				.param("page", "0")
				.param("size", "20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.content[0].email").value("user1@example.com"))
			.andExpect(jsonPath("$.content[1].email").value("user2@example.com"))
			.andExpect(jsonPath("$.totalElements").value(2));

		verify(memberService).findAllMembers(any());
	}

	@Test
	@DisplayName("전체 회원 조회 - 결과 없음")
	void getAllMembers_Empty() throws Exception {
		// given
		Page<MemberResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

		given(memberService.findAllMembers(any())).willReturn(emptyPage);

		// when & then
		mockMvc.perform(get("/members"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(0))
			.andExpect(jsonPath("$.totalElements").value(0));

		verify(memberService).findAllMembers(any());
	}

	@Test
	@DisplayName("내 정보 조회 성공")
	void getMyInfo_Success() throws Exception {
		// given - MockArgumentResolver가 memberId=1L인 CustomUserDetails 주입
		Member member = MemberFixture.createMemberWithId(1L, "test@example.com", "Test User");
		MemberResponse response = MemberResponse.fromEntity(member);

		given(memberService.getMemberById(1L)).willReturn(response);

		// when & then
		mockMvc.perform(get("/members/me"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("test@example.com"))
			.andExpect(jsonPath("$.name").value("Test User"));

		verify(memberService).getMemberById(1L);
	}

	@Test
	@DisplayName("회원 탈퇴 성공")
	void deleteMyAccount_Success() throws Exception {
		// given - MockArgumentResolver가 memberId=1L인 CustomUserDetails 주입
		willDoNothing().given(memberService).deleteMember(1L);

		// when & then
		mockMvc.perform(delete("/members/me"))
			.andExpect(status().isOk());

		verify(memberService).deleteMember(1L);
	}
}
