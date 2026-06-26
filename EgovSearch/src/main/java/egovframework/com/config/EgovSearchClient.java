package egovframework.com.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.net.ssl.SSLContext;

/**
 * OpenSearch 클라이언트 설정
 */
@Configuration
@Slf4j
public class EgovSearchClient {

        @Value("${opensearch.protocol}")
        private String protocol;

        @Value("${opensearch.url}")
        private String url;

        @Value("${opensearch.port}")
        private int port;

        @Value("${opensearch.username}")
        private String username;

        @Value("${opensearch.password}")
        private String password;

        @Value("${opensearch.keystore.path:}")
        private String keystorePath;

        @Value("${opensearch.keystore.password:changeit}")
        private String keystorePassword;

        @Bean
        @Lazy
        OpenSearchClient openSearchClient() throws Exception {
                log.info("=== OpenSearch Client Bean 초기화 시작 ===");

                // 1. HttpHost 생성
                final HttpHost host = new HttpHost(protocol, url, port);
                log.info("HttpHost 생성 완료: {}://{}:{}", protocol, url, port);

                // 2. ObjectMapper 설정
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                JacksonJsonpMapper jacksonJsonpMapper = new JacksonJsonpMapper(objectMapper);

                // 3. HTTP 프로토콜인 경우 간단하게 생성
                if ("http".equalsIgnoreCase(protocol)) {
                        log.info("HTTP 프로토콜 사용 - 간단한 클라이언트 생성");
                        final OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
                                        .builder(host)
                                        .setMapper(jacksonJsonpMapper)
                                        .build();
                        log.info("=== OpenSearch Client Bean 초기화 완료 (HTTP) ===");
                        return new OpenSearchClient(transport);
                }

                // 4. HTTPS 프로토콜인 경우 인증 및 SSL 설정 (공식 문서 방식)
                log.info("HTTPS 프로토콜 사용 - SSL 및 인증 설정");

                // 4-1. Credentials 설정
                final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                                new AuthScope(host),
                                new UsernamePasswordCredentials(username, password.toCharArray()));

                // 4-2. SSLContext 생성 (키스토어 기반 인증서 검증)
                SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
                if (keystorePath != null && !keystorePath.isBlank()) {
                        sslContextBuilder.loadTrustMaterial(
                                new java.io.File(keystorePath),
                                keystorePassword.toCharArray());
                        log.info("SSLContext 생성 완료 (키스토어: {})", keystorePath);
                } else {
                        log.info("SSLContext 생성 완료 (JVM 기본 TrustStore 사용)");
                }
                final SSLContext sslContext = sslContextBuilder.build();

                // 4-3. Transport 빌드 (공식 문서 방식)
                final ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder.builder(host);

                builder.setHttpClientConfigCallback(httpClientBuilder -> {
                        // TLS 전략 설정 (호스트명 검증 활성화)
                        final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                                        .setSslContext(sslContext)
                                        .build();

                        // 연결 매니저 설정
                        final PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder
                                        .create()
                                        .setTlsStrategy(tlsStrategy)
                                        .build();

                        return httpClientBuilder
                                        .setDefaultCredentialsProvider(credentialsProvider)
                                        .setConnectionManager(connectionManager);
                });

                // 4-4. Transport 빌드
                final OpenSearchTransport transport = builder.setMapper(jacksonJsonpMapper).build();
                log.info("=== OpenSearch Client Bean 초기화 완료 (HTTPS with TLS verification) ===");

                return new OpenSearchClient(transport);
        }
}
