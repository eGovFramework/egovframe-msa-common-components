package egovframework.com.filter;

import egovframework.com.config.GatewayJwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuthGatewayFilterFactoryTest {

    @Test
    void getParamFromPathReturnsLoginCategoryForMobileIdPath() {
        AuthGatewayFilterFactory filterFactory = new AuthGatewayFilterFactory(
                WebClient.builder(), mock(GatewayJwtProvider.class));

        String pathCode = ReflectionTestUtils.invokeMethod(
                filterFactory, "getParamFromPath", "/mip/main");

        assertThat(pathCode).isEqualTo("1");
    }
}
