package egovframework.com.ext.ops.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StrUtil 단위 테스트.
 * 외부 의존성 없이 순수 유틸리티 메서드를 검증한다.
 */
class StrUtilTest {

    @TempDir
    Path tempDir;

    // ---------------------------------------------------------------
    // cleanString
    // ---------------------------------------------------------------

    @Test
    @DisplayName("cleanString: null 또는 빈 문자열이면 null을 반환한다")
    void cleanString_nullOrEmpty_returnsNull() {
        assertThat(StrUtil.cleanString(null)).isNull();
        assertThat(StrUtil.cleanString("")).isNull();
        assertThat(StrUtil.cleanString("   ")).isNull();
    }

    @Test
    @DisplayName("cleanString: HTML 개행 태그를 실제 줄바꿈으로 변환한다")
    void cleanString_htmlLineBreaks_convertedToNewline() {
        String result = StrUtil.cleanString("line1<br>line2<br/>line3<br />line4");
        assertThat(result).isEqualTo("line1\nline2\nline3\nline4");
    }

    @Test
    @DisplayName("cleanString: <p> 태그를 제거한다")
    void cleanString_pTags_removed() {
        String result = StrUtil.cleanString("<p>단락 내용</p>");
        assertThat(result).isEqualTo("단락 내용");
    }

    @Test
    @DisplayName("cleanString: \\r 및 non-breaking space를 공백으로 치환한다")
    void cleanString_crAndNbsp_replacedWithSpace() {
        String input = "앞\r뒤 끝";
        String result = StrUtil.cleanString(input);
        assertThat(result).isEqualTo("앞 뒤 끝");
    }

    @Test
    @DisplayName("cleanString: HTML 엔티티(&amp; 등)를 디코딩한다")
    void cleanString_htmlEntities_decoded() {
        String result = StrUtil.cleanString("A &amp; B &lt;C&gt;");
        assertThat(result).isEqualTo("A & B <C>");
    }

    @Test
    @DisplayName("cleanString: 백슬래시를 공백으로 치환한다")
    void cleanString_backslash_replacedWithSpace() {
        String result = StrUtil.cleanString("path\\to\\file");
        assertThat(result).isEqualTo("path to file");
    }

    @Test
    @DisplayName("cleanString: 특수문자 없는 일반 문자열은 원본 그대로 반환한다")
    void cleanString_plainText_returnsSame() {
        String input = "일반 텍스트 내용";
        String result = StrUtil.cleanString(input);
        assertThat(result).isEqualTo(input);
    }

    // ---------------------------------------------------------------
    // readWordsFromFile
    // ---------------------------------------------------------------

    @Test
    @DisplayName("readWordsFromFile: 각 줄을 trim하여 리스트로 반환한다")
    void readWordsFromFile_normalFile_returnsTrimmedLines() throws IOException {
        Path file = tempDir.resolve("words.txt");
        Files.writeString(file, "apple\n  banana  \ncherry\n");

        List<String> words = StrUtil.readWordsFromFile(file.toString());

        assertThat(words).containsExactly("apple", "banana", "cherry");
    }

    @Test
    @DisplayName("readWordsFromFile: 빈 파일이면 빈 리스트를 반환한다")
    void readWordsFromFile_emptyFile_returnsEmptyList() throws IOException {
        Path file = tempDir.resolve("empty.txt");
        Files.createFile(file);

        List<String> words = StrUtil.readWordsFromFile(file.toString());

        assertThat(words).isEmpty();
    }

    @Test
    @DisplayName("readWordsFromFile: 존재하지 않는 파일이면 빈 리스트를 반환한다")
    void readWordsFromFile_nonExistentFile_returnsEmptyList() {
        List<String> words = StrUtil.readWordsFromFile("/nonexistent/path/words.txt");

        assertThat(words).isEmpty();
    }

    @Test
    @DisplayName("readWordsFromFile: 한글 단어도 올바르게 읽는다")
    void readWordsFromFile_koreanWords_returnsList() throws IOException {
        Path file = tempDir.resolve("korean.txt");
        Files.writeString(file, "전자정부\n표준프레임워크\n검색모듈\n");

        List<String> words = StrUtil.readWordsFromFile(file.toString());

        assertThat(words).containsExactly("전자정부", "표준프레임워크", "검색모듈");
    }
}
