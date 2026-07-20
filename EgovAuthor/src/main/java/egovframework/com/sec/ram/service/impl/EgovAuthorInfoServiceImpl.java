package egovframework.com.sec.ram.service.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.sec.ram.entity.AuthorInfo;
import egovframework.com.sec.ram.entity.QMenuCreateDetail;
import egovframework.com.sec.ram.repository.EgovAuthorInfoRepository;
import egovframework.com.sec.ram.service.AuthorInfoVO;
import egovframework.com.sec.ram.service.EgovAuthorManageService;
import egovframework.com.sec.ram.util.EgovAuthorInfoUtility;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service("ramEgovAuthorInfoService")
@RequiredArgsConstructor
public class EgovAuthorInfoServiceImpl extends EgovAbstractServiceImpl implements EgovAuthorManageService {

    private final EgovAuthorInfoRepository repository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AuthorInfoVO> list(AuthorInfoVO authorInfoVO) {
        Sort sort = Sort.by("authorCreatDe").descending();
        Pageable pageable = PageRequest.of(authorInfoVO.getFirstIndex(), authorInfoVO.getRecordCountPerPage(), sort);

        Specification<AuthorInfo> spec = (root, query, criteriaBuilder) -> null;
        if (!ObjectUtils.isEmpty(authorInfoVO.getSearchKeyword()) && "1".equals(authorInfoVO.getSearchCondition())) {
            spec = spec.and(EgovAuthorInfoSpecification.authorNmContains(authorInfoVO.getSearchKeyword()));
            return repository.findAll(spec, pageable).map(EgovAuthorInfoUtility::authorInfoEntityToVO);
        } else {
            return repository.findAll(pageable).map(EgovAuthorInfoUtility::authorInfoEntityToVO);
        }
    }

    @Override
    public AuthorInfoVO detail(AuthorInfoVO authorInfoVO, Map<String, String> userInfo) {
        requireAuthenticated(userInfo);
        String authorCode = authorInfoVO.getAuthorCode();
        return repository.findById(authorCode).map(EgovAuthorInfoUtility::authorInfoEntityToVO).orElse(null);
    }

    @Transactional
    @Override
    public AuthorInfoVO insert(AuthorInfoVO authorInfoVO) {
        AuthorInfo authorInfo = EgovAuthorInfoUtility.authorInfoVOToEntity(authorInfoVO);
        authorInfo.setAuthorCreatDe(LocalDateTime.now().format(formatter));
        return EgovAuthorInfoUtility.authorInfoEntityToVO(repository.save(authorInfo));
    }

    @Transactional
    @Override
    public AuthorInfoVO update(AuthorInfoVO authorInfoVO) {
        boolean result = doDelete(authorInfoVO);
        if (result) {
            return insert(authorInfoVO);
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public boolean delete(AuthorInfoVO authorInfoVO, Map<String, String> userInfo) {
        requireAuthenticated(userInfo);
        return doDelete(authorInfoVO);
    }

    private boolean doDelete(AuthorInfoVO authorInfoVO) {
        String authorCode = authorInfoVO.getOriginalAuthorCode();

        QMenuCreateDetail menuCreateDetail = QMenuCreateDetail.menuCreateDetail;

        long count = Optional.ofNullable(
                queryFactory
                        .select(menuCreateDetail.count())
                        .from(menuCreateDetail)
                        .where(menuCreateDetail.menuCreateDetailId.authorCode.eq(authorCode))
                        .fetchOne()
        ).orElse(0L);

        if (count > 0L) {
            return false;
        } else {
            repository.deleteById(authorCode);
            return true;
        }
    }

    private void requireAuthenticated(Map<String, String> userInfo) {
        if (userInfo == null || ObjectUtils.isEmpty(userInfo.get("uniqId"))) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        // 2026.07.13 KISA 보안취약점 조치 - 권한 마스터 데이터는 인증된 사용자만 접근
    }

}
