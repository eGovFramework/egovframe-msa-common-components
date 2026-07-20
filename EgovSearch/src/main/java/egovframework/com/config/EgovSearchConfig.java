package egovframework.com.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EgovSearchConfig {
	private String modelPath;
    private String tokenizerPath;
    private String stopTagsPath;
    private String synonymsPath;
    private String dictionaryRulesPath;
}
