# Software Architecture

`../back/` 애플리케이션의 소프트웨어 아키텍처와 팀 컨벤션입니다.

구현 또는 코드 리뷰 시 이 문서의 규칙을 우선으로 합니다. 규칙을 따르지 않는 부분을 발견하면 수정안을 제안합니다.

## 1. 아키텍처 개요

애플리케이션은 다음 3계층으로 구성합니다.

| 계층              | 주요 구성 요소 | 책임                                                     |
| ----------------- | -------------- | -------------------------------------------------------- |
| Presentation Tier | Controller     | 요청 검증, 하나의 Service 호출, HTTP 응답 생성           |
| Logic Tier        | Service        | 유스케이스 구현, 트랜잭션 경계 관리, 엔티티를 DTO로 변환 |
| Data Tier         | Repository     | DB 관련 작업, DB 행을 엔티티로 변환                      |

```text
Client → Controller → Service → Repository → Database
                     ↓
                    DTO
```

## 2. 용어와 API 명명 규칙

### 도메인 용어

| 도메인 | 함께 사용하는 표현                              |
| ------ | ----------------------------------------------- |
| Member | 회원, Me(내 정보), Mine(내), Username           |
| Widget | 위젯                                            |
| Review | 리뷰                                            |
| Book   | 도서, Recommend(추천), Rank(순위), Search(검색) |
| Wish   | 찜                                              |

`Username`은 번역하지 않고 그대로 사용합니다.

### API 이름

API 이름은 기본적으로 **`${도메인 명} ${메서드 명}`** 형식을 사용합니다.

| HTTP 메서드  | API 이름  |
| ------------ | --------- |
| `POST`       | 생성      |
| `GET` (다건) | 다건 조회 |
| `GET` (단건) | 단건 조회 |
| `PUT`        | 수정      |
| `DELETE`     | 삭제      |

특정 도메인 기준으로 조회할 때는 API 이름 앞에 **`${도메인 명}별`**을 붙입니다.

```text
GET /reviews/member/{memberId} → 회원별 리뷰 다건 조회
```

다음 API는 예외로 사용합니다.

| Endpoint                 | API 이름  |
| ------------------------ | --------- |
| `POST /members`          | 회원가입  |
| `POST /members/login`    | 로그인    |
| `DELETE /members/logout` | 로그아웃  |
| `DELETE /members`        | 회원 탈퇴 |

### 응답 메시지

| 상황  | 메시지 형식                         | 예시                         |
| ----- | ----------------------------------- | ---------------------------- |
| 성공  | `${API 명}을 성공했습니다.`         | `도서 생성을 성공했습니다.`  |
| `403` | `${API 명} 권한이 없습니다.`        | `리뷰 수정 권한이 없습니다.` |
| `404` | `존재하지 않는 ${도메인 명}입니다.` | `존재하지 않는 도서입니다.`  |
| `409` | `이미 존재하는 ${도메인 명}입니다.` | `이미 존재하는 회원입니다.`  |

## 3. Controller

### 책임

- 요청을 검증한다.
- 적절한 하나의 Service를 호출한다.
- Service의 결과를 바탕으로 HTTP 응답 DTO를 생성한다.

### 규칙

- Controller 클래스명 뒤에 버전을 명시한다.

  ```kotlin
  ApiMemberControllerV1
  ```

- 액션 메서드명은 호출하는 Service 메서드명과 동일하게 작성한다.
- Controller에는 `@Transactional`을 선언하지 않는다.
- Swagger 문서 정보가 실제 API와 일치하는지 확인한다. Tag(name = "...")은 클래스명으로, Operation(summary = "...")는 API 명으로 작성한다.

## 4. Service

### 책임

- 유스케이스를 구현한다.
- 트랜잭션 경계를 관리한다.
- 엔티티를 DTO로 변환한다.

여러 도메인을 함께 다뤄야 한다면 필요한 Repository를 Service에 직접 주입할 수 있습니다.

### 트랜잭션 규칙

클래스에는 읽기 전용 트랜잭션을 선언하고, 생성·수정·삭제 메서드에서만 일반 트랜잭션으로 덮어씁니다.

```kotlin
@Transactional(readOnly = true)
class BookService {

    @Transactional
    fun createBook(...) { ... }

    fun getBook(...) { ... }
}
```

> 엔티티를 DTO로 변환하는 작업은 트랜잭션 경계 안에서 수행합니다. 경계 밖에서 지연 로딩 필드에 접근하면 `LazyInitializationException`이 발생할 수 있습니다.

### 메서드 이름

아래 가이드를 기본으로 하되, 복잡한 유스케이스는 동작이 명확하게 드러나는 이름을 우선합니다.

| 동작              | 기본 형식                                    | 예시                            |
| ----------------- | -------------------------------------------- | ------------------------------- |
| 생성              | `create{Entity}`                             | `createReview`                  |
| 단건 조회         | `get{Entity}`                                | `getReview`                     |
| 다건 조회         | `get{Entities}`                              | `getReviews`                    |
| 수정              | `update{Entity}`                             | `updateReview`                  |
| 삭제              | `delete{Entity}`                             | `deleteReview`                  |
| 조건 1개 조회     | `get{Entities}By{Condition}`                 | `getReviewsByMemberId`          |
| 조건 여러 개 조회 | `get{Entities}By{Condition1}And{Condition2}` | `getReviewsByMemberIdAndRating` |

- 엔티티명은 단건·다건을 구분해 작성합니다.
- 조건이 둘 이상이면 조건 사이를 `And`로 연결합니다.
- 단순 조회 메서드에는 Repository 스타일의 `find`를 사용하지 않습니다.

## 5. DTO

### 요청 DTO

| 항목 | 규칙                                                    |
| ---- | ------------------------------------------------------- |
| 위치 | `controller` 패키지 하위의 `request` 패키지             |
| 이름 | 액션 메서드명에서 도메인명을 앞에 두고 `Request`를 붙임 |

```text
ReviewCreateRequest
```

### Service 반환 DTO

| 항목      | 규칙                                                        |
| --------- | ----------------------------------------------------------- |
| 위치      | 도메인 패키지 하위의 `dto` 패키지 (예: `domain/member/dto`) |
| 타입      | 상속 가능하도록 `data class` 대신 `class` 사용              |
| 기본 이름 | `{Entity}Dto`                                               |
| 확장 이름 | 기본 DTO에 추가한 정보를 이름으로 명시                      |

```kotlin
open class MemberDto(...)

class MemberWithUsernameAndGithubIdDto(...) : MemberDto(...)
```

반환 정보가 예외적인 경우에도, DTO 이름만 보고 담긴 정보를 알 수 있도록 명확하게 작성합니다.
