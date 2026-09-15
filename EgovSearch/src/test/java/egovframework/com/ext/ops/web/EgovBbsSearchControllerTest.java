package egovframework.com.ext.ops.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import egovframework.com.ext.ops.service.BoardVO;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class EgovBbsSearchControllerTest {

    @Test
    void searchPageHasOnlyOneSubmittedKeywordField() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(
                "/templates/egovframework/com/ext/ops/bbsSearch.html")) {
            assertThat(stream).isNotNull();
            String html = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(Pattern.compile("name=\"searchWrd\"").matcher(html).results().count())
                    .isEqualTo(1);
            assertThat(html).contains("document.getElementById(\"searchWrd\").value = "
                    + "document.getElementById(\"textSearchKeyword\").value;");
            assertThat(html).contains("document.getElementById(\"searchWrd\").value = "
                    + "document.getElementById(\"vectorSearchKeyword\").value;");
        }
    }

    @Test
    void vectorSearchUsesTheEnteredKeyword() throws Exception {
        MvcResult result = MockMvcBuilders.standaloneSetup(new EgovBbsSearchController())
                .build()
                .perform(post("/ext/ops/vectorSearchResultView")
                        .param("searchWrd", "flower"))
                .andReturn();

        BoardVO boardVO = (BoardVO) result.getModelAndView().getModel().get("BoardVO");
        assertThat(boardVO.getSearchWrd()).isEqualTo("flower");
    }

    @Test
    void resultPagesDisplayTheFullKeyword() throws Exception {
        for (String template : List.of("textSearchResult.html", "vectorSearchResult.html")) {
            try (InputStream stream = getClass().getResourceAsStream(
                    "/templates/egovframework/com/ext/ops/" + template)) {
                assertThat(stream).isNotNull();
                String html = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                assertThat(html).contains("BoardVO.searchWrd+'`'}");
                assertThat(html).doesNotContain("BoardVO.searchWrd).split(',')");
            }
        }
    }
}
