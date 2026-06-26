package egovframework.com.config;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class EgovBoardIdGeneration {

    @Bean(name="bbsMstrStrategy")
    EgovIdGnrStrategyImpl bbsMstrStrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("BBSMSTR_");
        egovIdGnrStrategyImpl.setCipers(12);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="egovBBSMstrIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl egovBBSMstrIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(bbsMstrStrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("BBS_ID");
        return egovTableIdGnrServiceImpl;
    }

    @Bean(name="blogIdStrategy")
    EgovIdGnrStrategyImpl blogIdStrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("BLOG_");
        egovIdGnrStrategyImpl.setCipers(15);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="egovBlogIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl egovBlogIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(blogIdStrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("BLOG_ID");
        return egovTableIdGnrServiceImpl;
    }

    @Bean(name="cmmntyStrategy")
    EgovIdGnrStrategyImpl cmmntyStrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("CMMNTY_");
        egovIdGnrStrategyImpl.setCipers(13);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name="egovCmmntyIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl egovCmmntyIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(cmmntyStrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("CMMNTY_ID");
        return egovTableIdGnrServiceImpl;
    }

    @Bean(name = "nttIdStrategy")
    EgovIdGnrStrategyImpl nttIdStrategy() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setCipers(20);
        return strategy;
    }

    @Bean(name="egovBoardIdGnrService")
    EgovTableIdGnrServiceImpl egovNttIdGnrService(DataSource dataSource){
        EgovTableIdGnrServiceImpl idGnrService = new EgovTableIdGnrServiceImpl();
        idGnrService.setDataSource(dataSource);
        idGnrService.setStrategy(nttIdStrategy());
        idGnrService.setBlockSize(10);
        idGnrService.setTable("COMTECOPSEQ");
        idGnrService.setTableName("NTT_ID");
        return idGnrService;
    }

    @Bean
    EgovIdGnrStrategyImpl fileStrategy(){
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setPrefix("FILE_");
        strategy.setCipers(15);
        strategy.setFillChar('0');
        return strategy;
    }

    @Bean(name = "egovFileIdGnrService")
    EgovTableIdGnrServiceImpl egovFileIdGnrService(DataSource dataSource){
        EgovTableIdGnrServiceImpl egovTableIdGnrService = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrService.setDataSource(dataSource);
        egovTableIdGnrService.setStrategy(fileStrategy());
        egovTableIdGnrService.setBlockSize(10);
        egovTableIdGnrService.setTable("COMTECOPSEQ");
        egovTableIdGnrService.setTableName("FILE_ID");
        return egovTableIdGnrService;
    }

    @Bean(name = "bbsSyncLogStrategy")
    EgovIdGnrStrategyImpl bbsSyncLogStrategy() {
        EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
        egovIdGnrStrategyImpl.setPrefix("SYNC_");
        egovIdGnrStrategyImpl.setCipers(15);
        egovIdGnrStrategyImpl.setFillChar('0');
        return egovIdGnrStrategyImpl;
    }

    @Bean(name = "egovBbsSyncLogIdGnrService", destroyMethod="destroy")
    EgovTableIdGnrServiceImpl egovBbsSyncLogIdGnrService(DataSource dataSource) {
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(bbsSyncLogStrategy());
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("SYNC_ID");
        return egovTableIdGnrServiceImpl;
    }

    @Bean
    EgovIdGnrStrategyImpl answerNoStrategy(){
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setCipers(20);
        return strategy;
    }

    @Bean(name = "egovAnswerNoGnrService")
    EgovTableIdGnrServiceImpl egovAnswerNoGnrService(@Qualifier("answerNoStrategy")EgovIdGnrStrategyImpl answerNoStrategy, DataSource dataSource){
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(answerNoStrategy);
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("ANSWER_NO");
        return egovTableIdGnrServiceImpl;
    }

    @Bean
    EgovIdGnrStrategyImpl stsfdgNoStrategy(){
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setCipers(20);
        return strategy;
    }

    @Bean(name = "egovStsfdgNoGnrService")
    EgovTableIdGnrServiceImpl egovStsfdgNoGnrService(@Qualifier("stsfdgNoStrategy")EgovIdGnrStrategyImpl stsfdgNoStrategy, DataSource dataSource){
        EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
        egovTableIdGnrServiceImpl.setDataSource(dataSource);
        egovTableIdGnrServiceImpl.setStrategy(stsfdgNoStrategy);
        egovTableIdGnrServiceImpl.setBlockSize(10);
        egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
        egovTableIdGnrServiceImpl.setTableName("STSFDG_NO");
        return egovTableIdGnrServiceImpl;
    }

}
