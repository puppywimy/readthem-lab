-- 초기 스키마. 엔티티(ddl-auto)가 만들던 구조에 도서 검색용 FULLTEXT 인덱스를 더했다.
-- 적용된 마이그레이션 파일은 고치지 않는다. 스키마 변경은 V2부터 새 파일로 한다.

CREATE TABLE `member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `github_id` varchar(255) DEFAULT NULL,
  `github_link` varchar(255) DEFAULT NULL,
  `widget_link` varchar(255) DEFAULT NULL,
  `img_url` varchar(255) DEFAULT NULL,
  `refresh_token` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','USER') DEFAULT NULL,
  `deleted_date` datetime(6) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6p926dfj54l7npjasf3kx2uxm` (`github_id`),
  UNIQUE KEY `UKm9ixjkf3qqgk04x8jayvlq0ww` (`github_link`),
  UNIQUE KEY `UK56ymicgdb3uysana9eq1qmyjf` (`refresh_token`),
  UNIQUE KEY `UKgc3jmn7c2abyo3wf6syln5t2i` (`username`),
  UNIQUE KEY `UKte6n2ys4g9xeswd0a5b7w7v6h` (`widget_link`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `isbn` varchar(255) DEFAULT NULL,
  `title` longtext NOT NULL,
  `authors` longtext,
  `publisher` varchar(255) DEFAULT NULL,
  `published_date` datetime(6) DEFAULT NULL,
  `description` longtext,
  `img_url` varchar(255) DEFAULT NULL,
  `img_url_fetched_at` datetime(6) DEFAULT NULL,
  `fetched_page` int DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKehpdfjpu1jm3hijhj4mm0hx9h` (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 도서 검색(BookRepository.searchByKeyword)용. 엔티티로 표현할 수 없어 여기서만 관리한다.
-- 불용어 설정은 현재 세션에만 적용되며, 인덱스를 만드는 시점에 적용되므로 인덱스를 만들기 전에 끈다.
SET SESSION innodb_ft_enable_stopword = OFF;

CREATE FULLTEXT INDEX ft_book_search
  ON book (title, authors, publisher)
  WITH PARSER ngram;

CREATE TABLE `book_operational` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `isbn` varchar(255) DEFAULT NULL,
  `view_count` int NOT NULL,
  `average_rating` double NOT NULL,
  `review_count` int NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5vjy3jwukhrrj7xtk80dt2bic` (`isbn`),
  KEY `idx_book_average_rating_id` (`average_rating` DESC,`id` DESC),
  KEY `idx_book_review_count_id` (`review_count` DESC,`id` DESC),
  KEY `idx_book_view_count_id` (`view_count` DESC,`id` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `book_fetch_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `current_page` int NOT NULL,
  `current_api_key_index` int NOT NULL,
  `daily_call_count` int NOT NULL,
  `last_call_date` date DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `book_last_fetched_page` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `number` int NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1wdpsed5kna2y38hnbgrnhi5b` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `book_id` bigint DEFAULT NULL,
  `reviewer_id` bigint DEFAULT NULL,
  `rating` float NOT NULL,
  `content` varchar(500) DEFAULT NULL,
  `like_count` int NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_like_count_id` (`like_count` DESC,`id` DESC),
  KEY `FK70yrt09r4r54tcgkrwbeqenbs` (`book_id`),
  KEY `FKr0jxjlw2wqvxuy2ntlsuvkydi` (`reviewer_id`),
  CONSTRAINT `FK70yrt09r4r54tcgkrwbeqenbs` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
  CONSTRAINT `FKr0jxjlw2wqvxuy2ntlsuvkydi` FOREIGN KEY (`reviewer_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `review_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `review_id` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqhbo1hs2awkmdp901kgb2ixm3` (`review_id`,`member_id`),
  KEY `FKf19ep4u0vm5vietilw2kp9jo2` (`member_id`),
  CONSTRAINT `FK68am9vk1s1e8n1v873meqkk0k` FOREIGN KEY (`review_id`) REFERENCES `review` (`id`),
  CONSTRAINT `FKf19ep4u0vm5vietilw2kp9jo2` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `review_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `review_id` bigint DEFAULT NULL,
  `tag_id` bigint DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKea2voymuynf2rmdx7ph30cwoe` (`review_id`),
  KEY `FKktgdj6yskxf37alvmhehas7fu` (`tag_id`),
  CONSTRAINT `FKea2voymuynf2rmdx7ph30cwoe` FOREIGN KEY (`review_id`) REFERENCES `review` (`id`),
  CONSTRAINT `FKktgdj6yskxf37alvmhehas7fu` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `wish` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint DEFAULT NULL,
  `book_id` bigint DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlmo6ovc1hi9y3w97u1h4gfpw8` (`book_id`),
  KEY `FK70nrc4a6uvljrtemsn80eq1gd` (`member_id`),
  CONSTRAINT `FK70nrc4a6uvljrtemsn80eq1gd` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `FKlmo6ovc1hi9y3w97u1h4gfpw8` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
