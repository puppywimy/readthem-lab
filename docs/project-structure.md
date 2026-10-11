# 프로젝트 구조

## Frontend

```text
front
└── src
    ├── app
    │   ├── admin
    │   ├── books
    │   │   ├── detail
    │   │   └── search
    │   ├── members
    │   │   ├── detail
    │   │   ├── join
    │   │   └── login
    │   └── mypage
    └── lib
        ├── auth
        ├── backend
        │   └── apiV1
        └── theme
```

## Backend

```text
back
└── src/main/kotlin/com/back
    ├── domain
    │   ├── book
    │   ├── home
    │   ├── member
    │   ├── review
    │   ├── tag
    │   ├── widget
    │   └── wish
    ├── global
    │   ├── app
    │   ├── aspect
    │   ├── exception
    │   ├── globalExceptionHandler
    │   ├── initData
    │   ├── jpa
    │   ├── redis
    │   ├── rq
    │   ├── rsData
    │   ├── security
    │   └── springDoc
    └── standard
        ├── recommend
        │   ├── byContent
        │   ├── byRating
        │   └── util
        └── util
```
