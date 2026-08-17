package egovframework.com.uat.uia.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;

import egovframework.com.uat.uia.service.LoginVO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * egovframework.com.uat.uia.service.impl.EgovLoginManageServiceLogTest
 * <p>
 * Verifies that actionLogin does not expose the encrypted password to the logs (CWE-532).
 * <p>
 * encPassword is Base64(SHA-256(userId)(userPw)) -- the stored credential hash used to look
 * up the member. Logging it means every login (when DEBUG is enabled) writes a
 * password-derived hash to the log file, enabling offline dictionary attacks against it.
 * No log event may contain that value.
 *
 * @author EricSeokgon
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class EgovLoginManageServiceLogTest {

    @Mock
    private JPAQueryFactory queryFactory;

    @InjectMocks
    private EgovLoginManageServiceImpl service;

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(EgovLoginManageServiceImpl.class);
        logger.setLevel(Level.DEBUG); // ensure DEBUG events are emitted
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    private LoginVO loginVO(String userId, String userPw, String userSe) {
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setUserPw(userPw);
        vo.setUserSe(userSe);
        return vo;
    }

    @Test
    @DisplayName("actionLogin must not log the encrypted password (CWE-532)")
    void actionLogin_doesNotLogEncryptedPassword() {
        // given
        String userId = "tester";
        String userPw = "S3cr3t!Passw0rd";
        // the exact value the production code computes and would log
        String encPassword = ReflectionTestUtils.invokeMethod(service, "encryptPassword", userPw, userId);

        assertThat(encPassword)
                .as("precondition: encryptPassword should produce a non-trivial hash")
                .isNotBlank()
                .hasSizeGreaterThan(10);

        // when: invoke the login flow. Downstream QueryDSL access fails fast (mock queryFactory),
        // but the debug log at the top of actionLogin has already fired by then.
        try {
            service.actionLogin(loginVO(userId, userPw, "GNR"));
        } catch (Exception ignored) {
            // expected: NPE from the unstubbed query chain, after the log statement under test
        }

        // then
        boolean leaked = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains(encPassword));

        assertThat(leaked)
                .as("encrypted password must not appear in logs (CWE-532). Captured logs: %s",
                        appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList())
                .isFalse();
    }
}
