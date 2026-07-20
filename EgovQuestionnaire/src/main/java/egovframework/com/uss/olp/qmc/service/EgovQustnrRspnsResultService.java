package egovframework.com.uss.olp.qmc.service;

import java.util.List;
import java.util.Map;

public interface EgovQustnrRspnsResultService {

    List<QustnrRspnsResultMCStatsDTO> qustnrRspnsResultMCStats(QestnrInfoVO qestnrInfoVO, Map<String, String> userInfo);

    List<QustnrRspnsResultESStatsDTO> qustnrRspnsResultESStats(QestnrInfoVO qestnrInfoVO, Map<String, String> userInfo);

}
