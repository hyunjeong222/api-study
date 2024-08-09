package com.spring.api.controller;

import com.spring.api.domain.Article;
import com.spring.api.dto.AddArticleRequest;
import com.spring.api.dto.UpdateArticleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.api.service.BlogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class) // JUniit5와 Mockito를 연동
class BlogApiControllerTest {
    @InjectMocks // 가짜 객체 주입
    private BlogApiController blogApiController;

    @Mock // 가짜 객체 생성
    private BlogService blogService;

    private MockMvc mockMvc; // 컨트롤러 테스트를 위해 HTTP 호출하기 위한 MockMvc

    private ObjectMapper objectMapper; // 직렬화, 역직렬화를 위한 클래스

    @BeforeEach // 각 테스트 실행 전 초기화 작업
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(blogApiController).build(); // Spring MVC 컨트롤러 테스트 환경 제공
        objectMapper = new ObjectMapper(); // JSON 데이터를 객체로 변환하거나, 객체를 JSON으로 변환
    }

    @DisplayName("addArticle : 블로그 글 추가 성공")
    @Test
    public void addArticle() throws Exception {
        // given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        // 객체 JSON 직렬화
        final String requestBody = objectMapper.writeValueAsString(userRequest);

        // when
        // save 메서드가 호출될 때, 특정 Article 객체 반환하도록 모킹
        given(blogService.save(any(AddArticleRequest.class))).willReturn(new Article(title, content));

        // 요청 전송
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());

        // save 메서드가 호출되었는지 검증
        verify(blogService).save(any(AddArticleRequest.class));
    }

    @DisplayName("findAllArticles : 블로그 글 전체 조회 성공")
    @Test
    public void findAllArticles() throws Exception {
        // given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";

        Article article = Article.builder()
                .title(title)
                .content(content)
                .build();

        // when, thenReturn
        // findAll 메소드가 호출될 때 데이터 반환 설정
        given(blogService.findAll()).willReturn(Collections.singletonList(article));
        // when
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(title))
                .andExpect(jsonPath("$[0].content").value(content));
    }

    @DisplayName("findArticle: 블로그 글 상세 조회 성공")
    @Test
    public void findArticle() throws Exception {
        // given
        final Long articleId = 1L; // 테스트에서 사용할 id 값
        final String title = "title";
        final String content = "content";

        // findById 메소드가 호출될 때 데이터 반환 설정
        when(blogService.findById(articleId)).thenReturn(new Article(title, content));

        final String url = "/api/articles/{id}";

        // when
        final ResultActions resultActions = mockMvc.perform(get(url, articleId));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.title").value(title));
    }

    // 블로그 글 삭제
    @DisplayName("deleteArticle : 블로그 글 삭제 성공")
    @Test
    public void deleteArticle() throws Exception {
        // given
        final Long articleId = 1L;
        final String url = "/api/articles/{id}";

        // 메소드가 void 타입일때, 외부 시스템에 실제로 영향 X
        doNothing().when(blogService).delete(articleId);

        // when
        final ResultActions resultActions = mockMvc.perform(delete(url, articleId));

        // then
        resultActions
                .andExpect(status().isOk());

        // delete 메서드가 몇 번 호출 되었는지 검증
        verify(blogService, times(1)).delete(articleId);
    }

    // 블로그 글 수정
    @DisplayName("updateArticle : 블로그 글 수정 성공")
    @Test
    public void updateArticle() throws Exception {
        // given
        final Long articleId = 1L;
        final String newTitle = "new title";
        final String newContent = "new content";

        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);
        Article updatedArticle = Article.builder()
                .title(newTitle)
                .content(newContent)
                .build();

        when(blogService.update(eq(articleId), any(UpdateArticleRequest.class))).thenReturn(updatedArticle);

        final String url = "/api/articles/{id}";

        // when
        ResultActions result = mockMvc.perform(put(url, articleId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        // title 필드의 값이 newTitle과 일치하는지 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(newTitle))
                .andExpect(jsonPath("$.content").value(newContent));

        // 메서드가 실제로 호출되었는지 검증
        verify(blogService, times(1)).update(eq(articleId), any(UpdateArticleRequest.class));
    }
}