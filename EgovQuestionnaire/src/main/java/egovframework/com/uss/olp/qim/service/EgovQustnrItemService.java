package egovframework.com.uss.olp.qim.service;

import org.springframework.data.domain.Page;

import java.util.Map;

public interface EgovQustnrItemService {

    Page<QustnrIemDTO> list(QustnrIemVO qustnrIemVO);

    QustnrIemDTO detail(QustnrIemVO qustnrIemVO, Map<String, String> userInfo);

    QustnrIemVO insert(QustnrIemVO qustnrIemVO, Map<String, String> userInfo);

    QustnrIemVO update(QustnrIemVO qustnrIemVO, Map<String, String> userInfo);

    boolean delete(QustnrIemVO qustnrIemVO, Map<String, String> userInfo);

}
