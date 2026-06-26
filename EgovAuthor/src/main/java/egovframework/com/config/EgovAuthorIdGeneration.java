package egovframework.com.config;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class EgovAuthorIdGeneration {

    @Bean(name="groupIdStrategy")
    EgovIdGnrStrategyImpl groupIdStrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("GROUP_");
        egovIdGnrStrategyImpl.setCipers(14);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="egovIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl egovIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(groupIdStrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("GROUP_ID");
        return egovTableIdGnrServiceImpl;
    }

    @Bean(name="roleIdStrategy")
    EgovIdGnrStrategyImpl roleIdStrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("");
        egovIdGnrStrategyImpl.setCipers(6);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="egovRoleIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl egovRoleIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(roleIdStrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("ROLE_ID");
        return egovTableIdGnrServiceImpl;
    }

}
