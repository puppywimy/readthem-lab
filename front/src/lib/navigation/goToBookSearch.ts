const BOOK_SEARCH_PATH = "/books/search";

/**
 * 도서 검색 페이지로 이동한다.
 *
 * 정적 빌드에서는 Next 라우터로 이동하면 검색어가 처음 방문한 검색어로 바뀌므로
 * 라우터를 거치지 않는다. docs/other_considerations.md 참고
 */
export function goToBookSearch(currentPathname: string, searchTerm = "") {
  const url =
    searchTerm.length > 0
      ? `${BOOK_SEARCH_PATH}/?searchTerm=${encodeURIComponent(searchTerm)}`
      : `${BOOK_SEARCH_PATH}/`;

  if (currentPathname !== BOOK_SEARCH_PATH) {
    window.location.assign(url);
    return;
  }

  // 검색 페이지는 주소창의 검색어를 읽어 검색하므로 주소만 바꾼다.
  // 같은 주소면 기록을 쌓지 않는다.
  if (window.location.pathname + window.location.search === url) {
    window.history.replaceState(null, "", url);
  } else {
    window.history.pushState(null, "", url);
  }
  // router.push처럼 새 결과의 맨 위에서 시작하게 한다.
  window.scrollTo({ top: 0 });
}
