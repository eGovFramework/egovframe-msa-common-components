package egovframework.com.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class JPAConfig {

    private final DataSource dataSource;

    @Value("${jpa.hibernate.ddl-auto:none}")
    private String ddlAuto;

    @Value("${jpa.show-sql:true}")
    private boolean showSql;

    @Value("${jpa.properties.hibernate.format_sql:true}")
    private boolean formatSql;

    /**
     * EntityManagerFactory 빈 생성
     * JPA 엔티티를 관리하기 위한 팩토리 설정
     */
    @Bean
    @Primary
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setPackagesToScan("egovframework.com");  // 엔티티 클래스가 위치한 패키지 지정
        factoryBean.setPersistenceUnitName("default");
        factoryBean.setJpaPropertyMap(properties);

        // JPA 구현체로 Hibernate 사용
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        factoryBean.setJpaVendorAdapter(vendorAdapter);

        return factoryBean;
    }

    /**
     * JPA TransactionManager 빈 생성
     * JPA 트랜잭션 관리
     */
    @Bean
    @Primary
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * JPAQueryFactory 빈 생성
     * QueryDSL 사용을 위한 팩토리 제공
     */
    @Bean
    JPAQueryFactory jpaQueryFactory(EntityManager entityManager){
        return new JPAQueryFactory(entityManager);
    }

}
