package org.sopt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sopt.config.TestJpaConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository 계층 테스트를 위한 커스텀 메타 어노테이션
 *
 * 포함 기능:
 * - @DataJpaTest: JPA 컴포넌트만 로드하는 슬라이스 테스트
 * - @AutoConfigureTestDatabase(replace = NONE): application-test.yml의 H2 설정 사용
 * - @ActiveProfiles("test"): 테스트 프로파일 활성화
 * - @Import(TestJpaConfig.class): JPA Auditing 활성화 (BaseTimeEntity의 @CreatedDate/@LastModifiedDate 동작)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public @interface RepositoryTest {
}
