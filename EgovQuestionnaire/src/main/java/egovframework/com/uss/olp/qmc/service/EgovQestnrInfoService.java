package egovframework.com.uss.olp.qmc.service;

import org.springframework.data.domain.Page;

import java.util.Map;

public interface EgovQestnrInfoService {

    Page<QestnrInfoDTO> list(QestnrInfoVO qestnrInfoVO);

    QestnrInfoDTO detail(QestnrInfoVO qestnrInfoVO);

    QestnrInfoVO insert(QestnrInfoVO qestnrInfoVO, Map<String, String> userInfo);

    QestnrInfoVO update(QestnrInfoVO qestnrInfoVO, Map<String, String> userInfo);

    boolean delete(QestnrInfoVO qestnrInfoVO, Map<String, String> userInfo);

}
