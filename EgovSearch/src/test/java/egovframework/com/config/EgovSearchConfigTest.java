package egovframework.com.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EgovSearchConfig POJO 단위 테스트.
 * EgovSearch-Config 모듈의 설정 구조체(getter/setter) 동작을 검증한다.
 */
class EgovSearchConfigTest {

    @Test
    @DisplayName("기본 생성 시 모든 필드가 null이다")
    void newInstance_allFieldsNull() {
        EgovSearchConfig config = new EgovSearchConfig();

        assertThat(config.getModelPath()).isNull();
        assertThat(config.getTokenizerPath()).isNull();
        assertThat(config.getStopTagsPath()).isNull();
        assertThat(config.getSynonymsPath()).isNull();
        assertThat(config.getDictionaryRulesPath()).isNull();
    }

    @Test
    @DisplayName("setter로 설정한 값을 getter로 정확히 조회한다")
    void setAndGet_allFields_returnCorrectValues() {
        EgovSearchConfig config = new EgovSearchConfig();

        config.setModelPath("/home/user/model/model.onnx");
        config.setTokenizerPath("/home/user/model/tokenizer.json");
        config.setStopTagsPath("/home/user/example/stoptags.txt");
        config.setSynonymsPath("/home/user/example/synonyms.txt");
        config.setDictionaryRulesPath("/home/user/example/dictionaryRules.txt");

        assertThat(config.getModelPath()).isEqualTo("/home/user/model/model.onnx");
        assertThat(config.getTokenizerPath()).isEqualTo("/home/user/model/tokenizer.json");
        assertThat(config.getStopTagsPath()).isEqualTo("/home/user/example/stoptags.txt");
        assertThat(config.getSynonymsPath()).isEqualTo("/home/user/example/synonyms.txt");
        assertThat(config.getDictionaryRulesPath()).isEqualTo("/home/user/example/dictionaryRules.txt");
    }

    @Test
    @DisplayName("필드를 덮어쓰면 마지막 값이 반영된다")
    void overwriteField_returnsLastValue() {
        EgovSearchConfig config = new EgovSearchConfig();

        config.setModelPath("/first/path");
        config.setModelPath("/second/path");

        assertThat(config.getModelPath()).isEqualTo("/second/path");
    }
}
