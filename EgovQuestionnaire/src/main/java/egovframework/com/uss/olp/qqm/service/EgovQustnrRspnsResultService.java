package egovframework.com.uss.olp.qqm.service;

import java.util.List;
import java.util.Map;

public interface EgovQustnrRspnsResultService {

    List<QustnrRspnsResultMCStatsDTO> qustnrRspnsResultMCStats(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo);

    List<QustnrRspnsResultESStatsDTO> qustnrRspnsResultESStats(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo);

}
