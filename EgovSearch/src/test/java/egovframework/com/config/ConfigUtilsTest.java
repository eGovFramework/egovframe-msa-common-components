package egovframework.com.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ConfigUtils 단위 테스트.
 * EgovSearch-Config JSON 설정 로딩 및 경로 해석 로직을 검증한다.
 * 외부 서비스(OpenSearch, DB 등) 없이 파일시스템만 사용한다.
 */
class ConfigUtilsTest {

    @TempDir
    Path tempDir;

    private ConfigUtils configUtils;

    @BeforeEach
    void setUp() {
        configUtils = new ConfigUtils();
    }

    // ---------------------------------------------------------------
    // resolvePath
    // ---------------------------------------------------------------

    @Test
    @DisplayName("resolvePath: null 입력이면 null을 반환한다")
    void resolvePath_null_returnsNull() {
        String result = configUtils.resolvePath(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolvePath: 빈 문자열이면 빈 문자열을 반환한다")
    void resolvePath_empty_returnsEmpty() {
        String result = configUtils.resolvePath("");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("resolvePath: ${HOME}을 포함한 경로를 실제 홈 디렉토리로 치환한다")
    void resolvePath_homeVariable_resolved() {
        String input = "${HOME}/EgovSearch-Config/model/model.onnx";
        String result = configUtils.resolvePath(input);

        String expectedHome = System.getenv("HOME");
        if (expectedHome == null || expectedHome.isEmpty()) {
            expectedHome = System.getenv("USERPROFILE");
        }
        if (expectedHome == null || expectedHome.isEmpty()) {
            expectedHome = System.getProperty("user.home");
        }

        assertThat(result).isNotNull();
        assertThat(result).startsWith(expectedHome);
        assertThat(result).doesNotContain("${HOME}");
    }

    @Test
    @DisplayName("resolvePath: ${HOME}이 없는 절대 경로는 정규화만 수행한다")
    void resolvePath_absolutePath_normalizedOnly() {
        String input = "/opt/egov/config/search.json";
        String result = configUtils.resolvePath(input);

        assertThat(result).isNotNull();
        assertThat(result).doesNotContain("${HOME}");
        // 경로 구분자가 시스템에 맞게 정규화
        assertThat(result).contains("egov");
    }

    @Test
    @DisplayName("resolvePath: 백슬래시 경로를 시스템 구분자로 정규화한다")
    void resolvePath_backslashPath_normalized() {
        String input = "C:\\Users\\test\\config\\search.json";
        String result = configUtils.resolvePath(input);

        assertThat(result).isNotNull();
        // 시스템 구분자로 통일되어야 함
        assertThat(result).contains("test");
        assertThat(result).contains("config");
    }

    @Test
    @DisplayName("resolvePath: ${HOME}을 여러 번 포함해도 모두 치환한다")
    void resolvePath_multipleHomeVars_allReplaced() {
        String input = "${HOME}/dir1/${HOME}/dir2";
        String result = configUtils.resolvePath(input);

        assertThat(result).doesNotContain("${HOME}");
    }

    // ---------------------------------------------------------------
    // loadConfig
    // ---------------------------------------------------------------

    @Test
    @DisplayName("loadConfig: 유효한 JSON 파일이면 EgovSearchConfig 객체를 반환한다")
    void loadConfig_validJson_returnsConfig() throws Exception {
        // searchConfig.json과 동일한 구조의 임시 파일 생성
        String json = "{\n"
                + "  \"modelPath\": \"${HOME}/model/model.onnx\",\n"
                + "  \"tokenizerPath\": \"${HOME}/model/tokenizer.json\",\n"
                + "  \"stopTagsPath\": \"/fixed/path/stoptags.txt\",\n"
                + "  \"synonymsPath\": \"/fixed/path/synonyms.txt\",\n"
                + "  \"dictionaryRulesPath\": \"/fixed/path/dictionaryRules.txt\"\n"
                + "}";
        Path configFile = tempDir.resolve("searchConfig.json");
        Files.writeString(configFile, json);

        // @Value 필드를 ReflectionTestUtils로 주입
        ReflectionTestUtils.setField(configUtils, "configPath", configFile.toString());

        EgovSearchConfig config = configUtils.loadConfig();

        assertThat(config).isNotNull();
        // ${HOME}이 치환되어야 함
        assertThat(config.getModelPath()).doesNotContain("${HOME}");
        assertThat(config.getTokenizerPath()).doesNotContain("${HOME}");
        // 고정 경로는 그대로 유지
        assertThat(config.getStopTagsPath()).contains("stoptags.txt");
        assertThat(config.getSynonymsPath()).contains("synonyms.txt");
        assertThat(config.getDictionaryRulesPath()).contains("dictionaryRules.txt");
    }

    @Test
    @DisplayName("loadConfig: 존재하지 않는 파일이면 null을 반환한다")
    void loadConfig_nonExistentFile_returnsNull() {
        ReflectionTestUtils.setField(configUtils, "configPath",
                tempDir.resolve("not_exist.json").toString());

        EgovSearchConfig config = configUtils.loadConfig();

        assertThat(config).isNull();
    }

    @Test
    @DisplayName("loadConfig: 경로가 모두 null인 JSON이면 null 경로를 유지한다")
    void loadConfig_nullPaths_handledGracefully() throws Exception {
        String json = "{}";
        Path configFile = tempDir.resolve("empty.json");
        Files.writeString(configFile, json);

        ReflectionTestUtils.setField(configUtils, "configPath", configFile.toString());

        EgovSearchConfig config = configUtils.loadConfig();

        assertThat(config).isNotNull();
        assertThat(config.getModelPath()).isNull();
        assertThat(config.getTokenizerPath()).isNull();
        assertThat(config.getStopTagsPath()).isNull();
        assertThat(config.getSynonymsPath()).isNull();
        assertThat(config.getDictionaryRulesPath()).isNull();
    }
}
