package egovframework.com.config;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class EgovQuestionnaireIdGeneration {

    @Bean(name="qustnrItemManageInfotrategy")
    EgovIdGnrStrategyImpl qustnrItemManageInfotrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("QESITM_");
        egovIdGnrStrategyImpl.setCipers(13);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="egovQustnrItemManageIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl egovQustnrItemManageIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(qustnrItemManageInfotrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("QESTNR_QESITM_ID");
        return egovTableIdGnrServiceImpl;
    }

    @Bean(name="qustnrManageInfotrategy")
    EgovIdGnrStrategyImpl qustnrManageInfotrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("QMANAGE_");
        egovIdGnrStrategyImpl.setCipers(12);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="egovQustnrManageIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl egovQustnrManageIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(qustnrManageInfotrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("QUSTNRTMPLA_ID");
        return egovTableIdGnrServiceImpl;
    }

    @Bean(name="qustnrQestnManageInfotrategy")
    EgovIdGnrStrategyImpl qustnrQestnManageInfotrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("QQESTN_");
        egovIdGnrStrategyImpl.setCipers(13);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="egovQustnrQestnManageIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl egovQustnrQestnManageIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(qustnrQestnManageInfotrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("QUSTNRQESTN_ID");
        return egovTableIdGnrServiceImpl;
    }

    @Bean(name="qustnrRespondInfotrategy")
    EgovIdGnrStrategyImpl qustnrRespondInfotrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("QRSPNS_");
        egovIdGnrStrategyImpl.setCipers(13);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="qustnrRespondInfoIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl qustnrRespondInfoIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(qustnrRespondInfotrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("QESRSPNS_ID");
        return egovTableIdGnrServiceImpl;
    }

    @Bean(name="qustnrRespondManageInfotrategy")
    EgovIdGnrStrategyImpl qustnrRespondManageInfotrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("QRPD_");
        egovIdGnrStrategyImpl.setCipers(15);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="qustnrRespondManageIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl qustnrRespondManageIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(qustnrRespondManageInfotrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("QESTNR_RPD_ID");
        return egovTableIdGnrServiceImpl;
    }

    @Bean(name="qustnrTmplatManageInfotrategy")
    EgovIdGnrStrategyImpl qustnrTmplatManageInfotrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("QTMPLA_");
        egovIdGnrStrategyImpl.setCipers(13);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="egovQustnrTmplatManageIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl egovQustnrTmplatManageIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(qustnrTmplatManageInfotrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("QUSTNRTMPLA_ID");
        return egovTableIdGnrServiceImpl;
    }

}
