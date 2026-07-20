package egovframework.com.uss.olp.qri.service;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface EgovQustnrRspnsResultService {

    Page<QustnrRspnsResultDTO> list(QustnrRspnsResultVO qustnrRspnsResultVO);

    boolean insert(QustnrRspnsResultVO qustnrRspnsResultVO, Map<String, String> userInfo);

    List<QustnrRspnsResultMCStatsDTO> qustnrRspnsResultMCStats(QustnrRspnsResultVO qustnrRspnsResultVO, Map<String, String> userInfo);

    List<QustnrRspnsResultESStatsDTO> qustnrRspnsResultESStats(QustnrRspnsResultVO qustnrRspnsResultVO, Map<String, String> userInfo);

}
