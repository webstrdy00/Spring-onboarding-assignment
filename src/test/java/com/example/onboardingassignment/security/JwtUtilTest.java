package com.example.onboardingassignment.security;

import com.example.onboardingassignment.common.exception.GlobalException;
import com.example.onboardingassignment.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private JwtUtil jwtUtil;

    private final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000L; // 7일

    private final String TEST_SECRET_KEY = "c3ByaW5nLWJvb3Qtc2VjdXJpdHktand0LXR1dG9yaWFsLWppd29vbi1zcHJpbmctYm9vdC1zZWN1cml0eS1qd3QtdHV0b3JpYWwK";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(redisTemplate);
        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET_KEY);
        jwtUtil.init();
    }

    @Nested
    @DisplayName("AccessToken 테스트")
    class AccessTokenTest{
        @Test
        void Access_Token_생성_성공() {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;

            // when
            String token = jwtUtil.createAccessToken(username, role);

            // then
            assertThat(token).isNotNull();
            Claims claims = jwtUtil.getUserInfoFromToken(token);
            assertThat(claims.getSubject()).isEqualTo(username);
            assertThat(claims.get(JwtUtil.AUTHORIZATION_KEY)).isEqualTo(role.getAuthority());
        }

        @Test
        void Access_Token_검증_성공() {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;
            String token = jwtUtil.createAccessToken(username, role);

            // when
            boolean isValid = jwtUtil.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        void 만료된_Access_Token_검증() {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;
            // 만료 시간을 -1ms로 설정하여 즉시 만료되는 토큰 생성
            String token = jwtUtil.createToken(username, role, -1);

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateToken(token))
                    .isInstanceOf(GlobalException.class);
        }

        @Test
        void 잘못된_서명의_Access_Token_검증() {
            // given
            String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciJ9.invalidSignature";

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateToken(invalidToken))
                    .isInstanceOf(GlobalException.class);
        }

        @Test
        void 변조된_Access_Token_검증() {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;
            String token = jwtUtil.createAccessToken(username, role) + "tampered";

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateToken(token))
                    .isInstanceOf(GlobalException.class);
        }
    }

    @Nested
    @DisplayName("RefreshToken Redis 관련 테스트")
    class RefreshTokenRedisTest{
        @BeforeEach
        void setUpRedis() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void Refresh_Token_생성_성공() {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;

            // when
            String token = jwtUtil.createRefreshToken(username, role);

            // then
            assertThat(token).isNotNull();
            verify(valueOperations).set(
                    eq(username),
                    anyString(),
                    eq(REFRESH_TOKEN_TIME),
                    any()
            );
        }

        @Test
        void Refresh_Token_Redis_저장_및_조회() {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;
            String token = jwtUtil.createRefreshToken(username, role);
            when(valueOperations.get(username)).thenReturn(token);

            // when
            Optional<String> retrievedToken = jwtUtil.getRefreshTokenFromRedis(username);

            // then
            assertThat(retrievedToken).isPresent();
            assertThat(retrievedToken.get()).isEqualTo(token);
        }

        @Test
        void Refresh_Token_업데이트_성공() {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;

            // when
            String newToken = jwtUtil.updateRefreshToken(username, role);

            // then
            assertThat(newToken).isNotNull();
            verify(redisTemplate).delete(username);
            verify(valueOperations).set(
                    eq(username),
                    anyString(),
                    eq(REFRESH_TOKEN_TIME),
                    any()
            );
        }

        @Test
        void Redis에_저장된_Refresh_Token_없음() {
            // given
            String username = "testUser";
            when(valueOperations.get(username)).thenReturn(null);

            // when
            Optional<String> result = jwtUtil.getRefreshTokenFromRedis(username);

            // then
            assertThat(result).isEmpty();
        }
        @Test
        void Refresh_Token_검증_성공() {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;
            String token = jwtUtil.createRefreshToken(username, role);

            // when
            boolean isValid = jwtUtil.validateRefreshToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        void 변조된_Refresh_Token_검증() {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;
            String token = jwtUtil.createRefreshToken(username, role) + "tampered";

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateRefreshToken(token))
                    .isInstanceOf(GlobalException.class);
        }
        @Test
        void Redis에_저장된_Token과_일치하지_않는_Refresh_Token_검증() throws InterruptedException {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;
            String originalToken = jwtUtil.createRefreshToken(username, role);

            Thread.sleep(1000);
            String differentToken = jwtUtil.createRefreshToken(username, role);
            when(valueOperations.get(username)).thenReturn(differentToken);

            // when
            Optional<String> storedToken = jwtUtil.getRefreshTokenFromRedis(username);

            // then
            assertThat(storedToken).isPresent();
            assertThat(storedToken.get()).isNotEqualTo(originalToken);
        }

        @Test
        void Refresh_Token_재발급_후_이전_토큰_검증() throws InterruptedException {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;
            String oldToken = jwtUtil.createRefreshToken(username, role);

            // when
            Thread.sleep(1000);
            String newToken = jwtUtil.updateRefreshToken(username, role);
            when(valueOperations.get(username)).thenReturn(newToken);

            // then
            Optional<String> storedToken = jwtUtil.getRefreshTokenFromRedis(username);
            assertThat(storedToken).isPresent();
            assertThat(storedToken.get()).isEqualTo(newToken);
            assertThat(storedToken.get()).isNotEqualTo(oldToken);
        }

    }

    @Nested
    @DisplayName("RefreshToken 검증 테스트")
    class RefreshTokenValidationTest{
        @Test
        void 잘못된_서명의_Refresh_Token_검증() {
            // given
            String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImF1dGgiOiJST0xFX1VTRVIifQ.invalidSignature";

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateRefreshToken(invalidToken))
                    .isInstanceOf(GlobalException.class);
        }
    }

    @Nested
    @DisplayName("토큰 예외 케이스 테스트")
    class TokenExceptionTest {
        private final Key testKey = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(TEST_SECRET_KEY)
        );

        @Test
        void 지원되지_않는_JWT_토큰_검증() {
            // given
            // JWT 형식이 아닌 토큰
            String unsupportedToken = "not.a.jwt.token.format";

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateToken(unsupportedToken))
                    .isInstanceOf(GlobalException.class)
                    .hasMessageContaining("UNAUTHORIZED");
        }

        @Test
        void 빈_Claims를_가진_JWT_토큰_검증() {
            // given
            // Claims가 비어있는 토큰 생성
            String emptyClaimsToken = Jwts.builder()
                    .signWith(testKey, SignatureAlgorithm.HS256)
                    .compact();

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateToken(emptyClaimsToken))
                    .isInstanceOf(GlobalException.class)
                    .hasMessageContaining("UNAUTHORIZED");
        }

        @Test
        void 유효하지_않은_형식의_JWT_토큰_검증() {
            // given
            // 완전히 잘못된 형식의 토큰
            String malformedToken = "this.is.not.even.close.to.jwt";

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateToken(malformedToken))
                    .isInstanceOf(GlobalException.class)
                    .hasMessageContaining("UNAUTHORIZED");
        }

        @Test
        void Null_JWT_토큰_검증() {
            // given
            String nullToken = null;

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateToken(nullToken))
                    .isInstanceOf(GlobalException.class)
                    .hasMessageContaining("UNAUTHORIZED");
        }

        @Test
        void 비어있는_JWT_토큰_검증() {
            // given
            String emptyToken = "";

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateToken(emptyToken))
                    .isInstanceOf(GlobalException.class)
                    .hasMessageContaining("UNAUTHORIZED");
        }

        @Test
        void 알고리즘이_다른_JWT_토큰_검증() {
            // given
            // 다른 알고리즘으로 서명된 토큰 생성 (예: HS512)
            Key differentKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            String token = Jwts.builder()
                    .setSubject("testUser")
                    .signWith(differentKey, SignatureAlgorithm.HS512)
                    .compact();

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateToken(token))
                    .isInstanceOf(GlobalException.class)
                    .hasMessageContaining("UNAUTHORIZED");
        }

        @Test
        void Header가_없는_JWT_토큰_검증() {
            // given
            // 헤더 없이 페이로드와 서명만 있는 토큰
            String invalidToken = ".eyJzdWIiOiJ0ZXN0VXNlciJ9.signature";

            // when & then
            assertThatThrownBy(() -> jwtUtil.validateToken(invalidToken))
                    .isInstanceOf(GlobalException.class)
                    .hasMessageContaining("UNAUTHORIZED");
        }
    }

    @Nested
    @DisplayName("토큰 공통 기능 테스트")
    class CommonTokenTest {
        @Test
        void 토큰에서_사용자_정보_추출() {
            // given
            String username = "testUser";
            UserRole role = UserRole.ROLE_USER;
            String token = jwtUtil.createAccessToken(username, role);

            // when
            Claims claims = jwtUtil.getUserInfoFromToken(token);

            // then
            assertThat(claims.getSubject()).isEqualTo(username);
            assertThat(claims.get(JwtUtil.AUTHORIZATION_KEY)).isEqualTo(role.getAuthority());
        }
    }
}