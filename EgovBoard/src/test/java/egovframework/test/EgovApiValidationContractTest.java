package egovframework.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * BindingResult 를 받는 핸들러는 검증 대상 파라미터에 검증 애노테이션을 선언해야 한다.
 *
 * 검증 애노테이션이 없으면 BindingResult 가 항상 비어 hasErrors 분기가 실행되지 않는다.
 *
 * @author 최완택
 * @since 2026-08-30
 */
@DisplayName("API 컨트롤러 검증 계약")
class EgovApiValidationContractTest {

	private static final Pattern METHOD = Pattern.compile("public\\s+\\S+\\s+(\\w+)\\s*\\(");

	@Test
	@DisplayName("BindingResult 를 받으면 검증 애노테이션도 함께 선언한다")
	void handlersWithBindingResultDeclareValidation() throws IOException {
		List<String> dead = new ArrayList<>();

		try (Stream<Path> paths = Files.walk(Paths.get("src/main/java"))) {
			for (Path path : paths.filter(Files::isRegularFile).toList()) {
				if (!path.getFileName().toString().endsWith("Controller.java")) {
					continue;
				}
				String source = Files.readString(path, StandardCharsets.UTF_8);
				Matcher matcher = METHOD.matcher(source);
				while (matcher.find()) {
					String parameters = parameterList(source, matcher.end() - 1);
					if (!parameters.contains("BindingResult")) {
						continue;
					}
					if (parameters.contains("@Valid") || parameters.contains("@Validated")) {
						continue;
					}
					dead.add(path.getFileName() + "." + matcher.group(1));
				}
			}
		}

		assertTrue(dead.isEmpty(), "검증 애노테이션 없이 BindingResult 를 받는 핸들러: " + dead);
	}

	private String parameterList(String source, int openParen) {
		int depth = 0;
		for (int i = openParen; i < source.length(); i++) {
			char c = source.charAt(i);
			if (c == '(') {
				depth++;
			} else if (c == ')') {
				depth--;
				if (depth == 0) {
					return source.substring(openParen + 1, i);
				}
			}
		}
		return "";
	}

}
