# Spring-onboarding-assignment
## 🏫 1. 프로젝트 소개
- Spring Security 이해와 JWT 활용 및 JUnit 활용

## ✅ 2. 프로젝트 요구 사항
- [x] Spring Security를 이용한 Filter에 대한 이해
  - [x] JwtAuthenticationFilter 구현
  - [x] JwtAuthorizationFilter 구현
  - [x] JwtExceptionFilter 구현
  - [x] SecurityConfig 설정
- [x] JWT와 구체적인 알고리즘의 이해
  - [x] AccessToken/RefreshToken 발급 로직 구현
  - [x] Redis를 활용한 RefreshToken 관리
  - [x] 토큰 검증 및 갱신 메커니즘 구현
- [x] PR 날려보기
- [x] JUnit를 이용한 테스트 코드 작성법 이해
  - [x] JWT 토큰 생성/검증 테스트
  - [x] 인증 필터 테스트
  - [x] 예외 처리 테스트
- [x] EC2에 배포해보기
- [x] 리뷰 바탕으로 개선하기

## 📌 3. 시나리오 설계
<details>
<summary><b>Spring Security 기본 이해</b></summary>
  
### Filter란 무엇인가? (with Interceptor, AOP)
- Filter는 HTTP 요청과 응답을 변경할 수 잇는 재사용 가능한 코드입니다.
- 디자인 패턴의 책임 연쇄 패턴의 대표적인 예시입니다.

1. **Filter**
   - J2EE 스펙의 서블릿 기능
   - request/response의 최전방에서 처리
   - web.xml에 설정
   - Web Container에서 관리
   - ServletRequest/ServletResponse 조작 가능
2. **Interceptor**
   - Spring MVC에서 제공하는 기능
   - Spring Container에서 관리
   - Controller 호출 전후에 처리
   - HandlerInterceptor를 구현하여 사용
3. **AOP**
   - Spring Framework에서 제공하는 기능
   - 메소드 전후 처리
   - 비즈니스 로직과 관련된 부분을 처리

### Spring Security란?
- Spring Security는 Spring 기반의 애플리케이션의 보안(인증과 권한, 인가 등)을 담당하는 스프링 하위 프레임워크입니다.

1. **보안 관련 주요 기능**
   - 인증(Authentication)
   - 권한/인가(Authorization)
   - 암호화
   - CSRF 공격 방지
   - Session 관리

2. **주요 용어**
   - Principal (접근 주체): 보호받는 리소스에 접근하는 대상
   - Authentication (인증): 사용자가 누구인지 확인하는 절차
   - Authorization (인가): 인증된 사용자가 리소스에 접근할 권한이 있는지 확인
   - Credential (비밀번호): 리소스에 접근하는 대상의 비밀번호

3. **기본 구조**
   - SecurityContextHolder: 보안 주체의 세부 정보를 포함하는 컨텍스트 정보 저장
   - SecurityContext: Authentication을 보관하는 역할
   - Authentication: 현재 접근하는 주체의 정보와 권한을 담는 인터페이스
   - UsernamePasswordAuthenticationToken: Authentication을 구현한 대표적인 클래스
</details>

<details>
<summary><b>JWT 기본 이해</b></summary>
  
### JWT란 무엇인가요?
- JWT는 당사자 간 정보를 JSON 객체로 안전하게 전송하기 위한 독립적인 방식을 정의하는 개방형 표준(RFC 7519)입니다.
- 이 정보는 디지털 서명이 되어있으므로 신뢰할 수 있습니다.

### JWT 구조
1. **Header (헤더)**
  ```json
  {
    "alg": "HS256",  // 서명 알고리즘
    "typ": "JWT"     // 토큰 유형
  }
```
2. **Payload (내용)**
  ```json
  {
    "sub": "1234567890",     // 사용자 식별자
    "name": "John Doe",      // 사용자 이름
    "auth": "ROLE_USER",     // 사용자 권한
    "iat": 1516239022,       // 토큰 발행 시간
    "exp": 1516242622       // 토큰 만료 시간
  }
```
3. **Signature (서명)**
  ```
  HMACSHA256(
    base64UrlEncode(header) + "." +
    base64UrlEncode(payload),
    secret
  )
```

### JWT  특징
1. **상태 비저장(Stateless)**
   - 서버에 별도의 저장소가 필요 없음
   - 서버 확장성이 좋음
2. **자가 수용적(Self-contained)**
   - 필요한 모든 정보를 토큰 자체에 포함
   - 별도의 조회 없이 인증 가능
3. **보안성**
   - 디지털 서명으로 변조 방지
   - HTTPS와 함께 사용 시 보안성 향상

### JWT 동작 방식
1. 클라이언트가 서버에 인증 정보(아이디/비밀번호)를 전송
2. 서버는 인증 정보를 확인하고 JWT 생성
3. 서버가 클라이언트에게 JWT 전달
4. 클라이언트는 이후 요청 시 JWT를 함께 전송
5. 서버는 JWT를 검증하고 요청 처리

### JWT 장단점
1. **장점**
   - 서버 부하 감소 (Stateless)
   - 확장성이 좋음
   - 클라이언트 독립적인 인증 방식
3. **단점**
   - 토큰 크기가 상대적으로 큼
   - 한번 발급된 토큰은 만료 전까지 취소 어려움
   - Payload 정보가 암호화되지 않음

### 토큰 종류
1. **Access Token**
   - 실제 인증에 사용되는 토큰
   - 비교적 짧은 유효기간 (예: 30분)
2. **Refresh Token**
   - Access Token 재발급용 토큰
   - 비교적 긴 유효기간 (예: 1주)
   - 보안을 위해 데이터베이스에 저장
</details>
