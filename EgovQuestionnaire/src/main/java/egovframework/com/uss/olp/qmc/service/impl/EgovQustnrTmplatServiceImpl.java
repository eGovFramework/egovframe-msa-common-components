package egovframework.com.uss.olp.qmc.service.impl;

import egovframework.com.uss.olp.qmc.repository.EgovQustnrTmplatRepository;
import egovframework.com.uss.olp.qmc.service.EgovQustnrTmplatService;
import egovframework.com.uss.olp.qmc.service.QustnrTmplatVO;
import egovframework.com.uss.olp.qmc.util.EgovQestnrInfoUtility;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("qmcEgovQustnrTmplatService")
@RequiredArgsConstructor
public class EgovQustnrTmplatServiceImpl extends EgovAbstractServiceImpl implements EgovQustnrTmplatService {

    private final EgovQustnrTmplatRepository repository;

    @Override
    public List<QustnrTmplatVO> list() {
        return repository.findAll().stream().map(EgovQestnrInfoUtility::qustnrTmplatEntityToVO).collect(Collectors.toList());
    }

    @Override
    public byte[] getImage(String qustnrTmplatId, Map<String, String> userInfo) {
        return repository.findById(qustnrTmplatId)
                .map(template -> {
                    String uniqId = userInfo != null ? userInfo.get("uniqId") : null;
                    if (ObjectUtils.isEmpty(uniqId)) {
                        throw new IllegalStateException("인증 정보가 없습니다.");
                    }
                    // 2026.07.13 KISA 보안취약점 조치
                    if (!Objects.equals(uniqId, template.getFrstRegisterId())) {
                        throw new IllegalStateException("권한이 없습니다.");
                    }
                    return template.getQustnrTmplatImageInfo();
                })
                .orElseThrow(() -> new IllegalStateException("권한이 없습니다."));
    }

}
