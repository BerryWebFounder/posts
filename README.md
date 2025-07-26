# Posts API Server (Backend)

Spring Boot 3 + MariaDB + JPA로 구축된 게시판 백엔드 API 서버입니다.

## 🚀 주요 기능

### 📝 게시글 관리
- 게시글 CRUD (생성, 조회, 수정, 삭제)
- 페이지네이션 지원
- 제목/내용/작성자별 검색
- 게시글 통계 조회

### 💬 댓글 시스템
- 댓글 CRUD 기능
- 게시글별 댓글 조회
- 작성자별 댓글 관리
- 댓글 내용 검색

### 📎 파일 관리
- 다중 파일 업로드/다운로드
- 파일 메타데이터 관리
- 이미지 파일 필터링
- 파일 크기 제한 및 검증
- 안전한 파일 저장 (UUID 기반)

### 🔐 보안 및 설정
- CORS 설정
- Spring Security 기본 설정
- 파일 업로드 보안
- SQL Injection 방지 (JPA 사용)

## 🛠 기술 스택

### Core Framework
- **Spring Boot 3.5.3** - 메인 프레임워크
- **Java 17** - 개발 언어
- **Gradle 8.14.3** - 빌드 도구

### Database
- **MariaDB** - 메인 데이터베이스
- **Spring Data JPA** - ORM
- **Hibernate** - JPA 구현체

### Libraries
- **Lombok** - 보일러플레이트 코드 제거
- **Spring Security** - 보안 프레임워크
- **Spring Boot Actuator** - 모니터링
- **Spring Boot DevTools** - 개발 도구

## 📁 프로젝트 구조

```
src/main/java/com/berryweb/shop/posts/
├── PostsApplication.java                    # 메인 애플리케이션 클래스
├── config/
│   ├── SecurityConfig.java                 # Spring Security 설정
│   └── WebConfig.java                      # CORS 및 웹 설정
├── controller/
│   ├── PostController.java                 # 게시글 REST API
│   ├── CommentController.java              # 댓글 REST API
│   └── FileController.java                 # 파일 업로드/다운로드 API
├── dto/
│   ├── PostCreateReq.java                  # 게시글 생성 요청 DTO
│   ├── PostUpdateReq.java                  # 게시글 수정 요청 DTO
│   ├── CommentCreateReq.java               # 댓글 생성 요청 DTO
│   └── CommentUpdateReq.java               # 댓글 수정 요청 DTO
├── entity/
│   ├── Post.java                           # 게시글 엔티티
│   ├── Comment.java                        # 댓글 엔티티
│   └── PostFile.java                       # 파일 엔티티
├── repository/
│   ├── PostRepository.java                 # 게시글 데이터 접근
│   ├── CommentRepository.java              # 댓글 데이터 접근
│   └── PostFileRepository.java             # 파일 데이터 접근
└── service/
    ├── PostService.java                    # 게시글 비즈니스 로직
    ├── CommentService.java                 # 댓글 비즈니스 로직
    └── PostFileService.java                # 파일 비즈니스 로직

src/main/resources/
├── application.yml                          # 애플리케이션 설정
└── static/                                  # 정적 리소스

uploads/                                     # 파일 업로드 디렉토리
```

## 🗄 데이터베이스 스키마

### posts 테이블
```sql
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### comments 테이블
```sql
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
```

### files 테이블
```sql
CREATE TABLE files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
```

## 🚀 시작하기

### 필수 요구사항
- **Java 17+**
- **MariaDB 10.3+**
- **Gradle 7.0+** (또는 Gradle Wrapper 사용)

### 설치 및 실행

1. **저장소 클론**
   ```bash
   git clone <repository-url>
   cd posts-backend
   ```

2. **데이터베이스 설정**
   ```sql
   -- MariaDB에서 데이터베이스 생성
   CREATE DATABASE shop_posts CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'posts_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON shop_posts.* TO 'posts_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **환경 설정**
   
   `src/main/resources/application.yml` 수정:
   ```yaml
   spring:
     datasource:
       url: jdbc:mariadb://localhost:3306/shop_posts?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul
       username: posts_user
       password: your_password
   ```

4. **애플리케이션 실행**
   ```bash
   # Gradle Wrapper 사용 (권장)
   ./gradlew bootRun
   
   # 또는 Gradle이 설치된 경우
   gradle bootRun
   ```

5. **서버 확인**
   ```bash
   curl http://localhost:8081/api/posts
   ```

### 개발 모드 실행

```bash
# 개발 프로필로 실행 (DDL 자동 생성)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 📊 API 엔드포인트

### 게시글 API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/posts` | 게시글 목록 조회 (페이징) |
| GET | `/api/posts/{id}` | 게시글 상세 조회 |
| POST | `/api/posts` | 게시글 생성 |
| PUT | `/api/posts/{id}` | 게시글 수정 |
| DELETE | `/api/posts/{id}` | 게시글 삭제 |
| GET | `/api/posts/search` | 게시글 검색 |
| GET | `/api/posts/with-files` | 파일이 첨부된 게시글 조회 |
| GET | `/api/posts/stats` | 게시판 통계 |

### 댓글 API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/comments/post/{postId}` | 특정 게시글의 댓글 조회 |
| GET | `/api/comments/{id}` | 댓글 상세 조회 |
| POST | `/api/comments` | 댓글 생성 |
| PUT | `/api/comments/{id}` | 댓글 수정 |
| DELETE | `/api/comments/{id}` | 댓글 삭제 |
| GET | `/api/comments/author/{author}` | 작성자별 댓글 조회 |
| GET | `/api/comments/search` | 댓글 검색 |

### 파일 API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/files/post/{postId}` | 특정 게시글의 파일 목록 |
| POST | `/api/files/upload/{postId}` | 파일 업로드 |
| GET | `/api/files/download/{storedName}` | 파일 다운로드 |
| DELETE | `/api/files/{id}` | 파일 삭제 |
| GET | `/api/files/{id}` | 파일 정보 조회 |
| GET | `/api/files/images` | 이미지 파일 목록 |

## 📝 API 사용 예시

### 게시글 생성
```bash
curl -X POST http://localhost:8081/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "테스트 게시글",
    "content": "게시글 내용입니다.",
    "author": "작성자"
  }'
```

### 파일 업로드
```bash
curl -X POST http://localhost:8081/api/files/upload/1 \
  -F "files=@/path/to/file1.jpg" \
  -F "files=@/path/to/file2.pdf"
```

### 게시글 검색
```bash
# 제목으로 검색
curl "http://localhost:8081/api/posts/search?title=테스트&page=0&size=10"

# 작성자로 검색
curl "http://localhost:8081/api/posts/search?author=홍길동&page=0&size=10"

# 키워드로 검색 (제목 + 내용)
curl "http://localhost:8081/api/posts/search?keyword=Spring&page=0&size=10"
```

## ⚙️ 설정 및 환경변수

### application.yml 주요 설정

```yaml
# 서버 설정
server:
  port: 8081

# 데이터베이스 설정
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/shop_posts
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:admin}
  
  # JPA 설정
  jpa:
    hibernate:
      ddl-auto: validate  # 운영: validate, 개발: create-drop
    show-sql: true
  
  # 파일 업로드 설정
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

# 파일 저장 경로
file:
  upload:
    directory: uploads
```

### 환경별 프로필

#### 개발 환경 (`dev`)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # 테이블 자동 생성/삭제
    show-sql: true
  
file:
  upload:
    directory: uploads/dev
```

#### 운영 환경 (`prod`)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 스키마 검증만
    show-sql: false

file:
  upload:
    directory: /var/uploads/prod
```

### 환경변수
- `DB_HOST`: 데이터베이스 호스트 (기본값: localhost)
- `DB_PORT`: 데이터베이스 포트 (기본값: 3306)
- `DB_NAME`: 데이터베이스 이름 (기본값: shop_posts)
- `DB_USERNAME`: 데이터베이스 사용자명
- `DB_PASSWORD`: 데이터베이스 비밀번호
- `SERVER_PORT`: 서버 포트 (기본값: 8081)

## 🔧 개발 가이드

### 코드 컨벤션
- **Java 17** 문법 사용
- **Lombok** 어노테이션 활용
- **RESTful API** 설계 원칙 준수
- **Controller-Service-Repository** 패턴

### 테스트
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests PostServiceTest
```

### 빌드
```bash
# JAR 파일 생성
./gradlew bootJar

# 생성된 JAR 실행
java -jar build/libs/posts-0.0.1-SNAPSHOT.jar
```

## 📦 배포

### Docker 배포 (예시)
```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app
COPY build/libs/posts-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
```

### 시스템 요구사항
- **메모리**: 최소 512MB, 권장 1GB+
- **디스크**: 최소 1GB (로그 및 업로드 파일 공간)
- **네트워크**: 8081 포트 개방

## 🔍 모니터링 및 로깅

### Actuator 엔드포인트
- `/actuator/health` - 헬스 체크
- `/actuator/info` - 애플리케이션 정보
- `/actuator/metrics` - 메트릭 정보

### 로그 설정
```yaml
logging:
  level:
    com.berryweb.shop.posts: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
  file:
    name: logs/posts-application.log
```

## 🐛 문제 해결

### 일반적인 문제들

1. **데이터베이스 연결 실패**
   ```bash
   # MariaDB 서비스 상태 확인
   systemctl status mariadb  # Linux
   brew services list | grep mariadb  # macOS
   ```

2. **파일 업로드 실패**
   ```bash
   # uploads 폴더 권한 확인
   ls -la uploads
   
   # 권한 설정
   chmod 755 uploads
   ```

3. **포트 충돌**
   ```bash
   # 8081 포트 사용 프로세스 확인
   netstat -tulpn | grep 8081
   lsof -i :8081
   ```

### 디버깅 팁
- 개발 모드에서 `spring.jpa.show-sql=true` 설정으로 SQL 쿼리 확인
- `spring.logging.level.com.berryweb.shop.posts=DEBUG` 설정으로 상세 로그 확인
- Actuator의 `/actuator/health` 엔드포인트로 시스템 상태 확인

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 📞 연락처

프로젝트 링크: [GitHub Repository URL]

---

**Built with ❤️ using Spring Boot 3 and MariaDB**
