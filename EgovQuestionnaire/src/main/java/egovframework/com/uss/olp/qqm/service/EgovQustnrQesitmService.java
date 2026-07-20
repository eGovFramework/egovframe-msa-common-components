package egovframework.com.uss.olp.qqm.service;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface EgovQustnrQesitmService {

    Page<QustnrQesitmDTO> list(QustnrQesitmVO qustnrQesitmVO);

    QustnrQesitmDTO detail(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo);

    QustnrQesitmVO insert(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo);

    QustnrQesitmVO update(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo);

    boolean delete(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo);

    List<QustnrQesitmDTO> qustnrQusitmList(QustnrQesitmVO qustnrQesitmVO);

}
