package egovframework.com.uss.olp.qrm.service;

import org.springframework.data.domain.Page;

import java.util.Map;

public interface EgovQustnrRespondInfoService {

    Page<QustnrRespondInfoDTO> list(QustnrRespondInfoVO qustnrRespondInfoVO);

    QustnrRespondInfoDTO detail(QustnrRespondInfoVO qustnrRespondInfoVO, Map<String, String> userInfo);

    QustnrRespondInfoVO insert(QustnrRespondInfoVO qustnrRespondInfoVO);

    QustnrRespondInfoVO update(QustnrRespondInfoVO qustnrRespondInfoVO, Map<String, String> userInfo);

    boolean delete(QustnrRespondInfoVO qustnrRespondInfoVO, Map<String, String> userInfo);

}
