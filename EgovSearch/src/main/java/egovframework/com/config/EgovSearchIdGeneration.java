package egovframework.com.config;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class EgovSearchIdGeneration {

	@Bean(name="searchIdStrategy")
	EgovIdGnrStrategyImpl searchIdStrategy() {
		EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
		egovIdGnrStrategyImpl.setPrefix("SYNC_");
		egovIdGnrStrategyImpl.setCipers(15);
		egovIdGnrStrategyImpl.setFillChar('0');
		return egovIdGnrStrategyImpl;
	}

	@Bean(name="egovIdGnrService", destroyMethod="destroy")
	EgovTableIdGnrServiceImpl egovIdGnrService(DataSource dataSource) {
		EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
		egovTableIdGnrServiceImpl.setDataSource(dataSource);
		egovTableIdGnrServiceImpl.setStrategy(searchIdStrategy());
		egovTableIdGnrServiceImpl.setBlockSize(10);
		egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
		egovTableIdGnrServiceImpl.setTableName("SYNC_ID");
		return egovTableIdGnrServiceImpl;
	}

}
