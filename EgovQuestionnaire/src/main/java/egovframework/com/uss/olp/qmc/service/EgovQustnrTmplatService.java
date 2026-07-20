package egovframework.com.uss.olp.qmc.service;

import java.util.List;
import java.util.Map;

public interface EgovQustnrTmplatService {

    List<QustnrTmplatVO> list();

    byte[] getImage(String qustnrTmplatId, Map<String, String> userInfo);

}
