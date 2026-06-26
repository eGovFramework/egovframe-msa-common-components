package egovframework.com.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class EgovLoginDataSource {

    @Value("${datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Value("${datasource.url:jdbc:mysql://localhost:3306/com}")
    private String url;

    @Value("${datasource.username:com}")
    private String username;

    @Value("${datasource.password:com01}")
    private String password;

    @Value("${datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    @Value("${datasource.hikari.idle-timeout:30000}")
    private long idleTimeout;

    @Value("${datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${datasource.hikari.max-lifetime:180000}")
    private long maxLifetime;

    @Bean(name="dataSource")
    DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMinimumIdle(minimumIdle);
        config.setMaxLifetime(maxLifetime);

        return new HikariDataSource(config);
    }

}
