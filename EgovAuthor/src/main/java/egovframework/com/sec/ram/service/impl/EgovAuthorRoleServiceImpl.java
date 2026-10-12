package egovframework.com.sec.ram.service.impl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.sec.ram.entity.AuthorRoleRelated;
import egovframework.com.sec.ram.entity.QAuthorRoleRelated;
import egovframework.com.sec.ram.entity.QRoleInfo;
import egovframework.com.sec.ram.entity.RoleInfo;
import egovframework.com.sec.ram.repository.EgovAuthorRoleRepository;
import egovframework.com.sec.ram.service.AuthorRoleRelatedVO;
import egovframework.com.sec.ram.service.EgovAuthorRoleService;
import egovframework.com.sec.ram.service.RoleInfoDTO;
import egovframework.com.sec.ram.util.EgovAuthorInfoUtility;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("ramEgovAuthorRoleService")
@RequiredArgsConstructor
public class EgovAuthorRoleServiceImpl extends EgovAbstractServiceImpl implements EgovAuthorRoleService {

    private final EgovAuthorRoleRepository repository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<RoleInfoDTO> list(AuthorRoleRelatedVO authorRoleRelatedVO) {
        Pageable pageable = PageRequest.of(authorRoleRelatedVO.getFirstIndex(), authorRoleRelatedVO.getRecordCountPerPage());
        String searchKeyword = authorRoleRelatedVO.getSearchKeyword();

        QRoleInfo roleInfo = QRoleInfo.roleInfo;
        QAuthorRoleRelated authorRoleRelated = QAuthorRoleRelated.authorRoleRelated;

        JPAQuery<Tuple> query = queryFactory
                .select(roleInfo, authorRoleRelated)
                .from(roleInfo)
                .leftJoin(authorRoleRelated)
                .on(roleInfo.roleCode.eq(authorRoleRelated.authorRoleRelatedId.roleCode)
                        .and(authorRoleRelated.authorRoleRelatedId.authorCode.eq(searchKeyword)))
                .orderBy(roleInfo.roleCode.asc());

        List<Tuple> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<RoleInfoDTO> content = results.stream().map(tuple -> {
            RoleInfo r = tuple.get(roleInfo);
            AuthorRoleRelated ar = tuple.get(authorRoleRelated);

            String authorRelated = "N";
            String creatDt = null;

            if (ar != null && ar.getAuthorRoleRelatedId() != null) {
                authorRelated = "Y";
                if (ar.getCreatDt() != null) {
                    creatDt = ar.getCreatDt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }else{
                    creatDt = "''";
                }
            }

            return new RoleInfoDTO(
                    Objects.requireNonNull(r).getRoleCode(),
                    r.getRoleNm(),
                    r.getRolePttrn(),
                    r.getRoleDc(),
                    r.getRoleTy(),
                    r.getRoleSort(),
                    authorRelated,
                    creatDt
            );
        }).collect(Collectors.toList());

        // count 쿼리
        long total = Optional.ofNullable(
                queryFactory
                        .select(roleInfo.count())
                        .from(roleInfo)
                        .leftJoin(authorRoleRelated)
                        .on(roleInfo.roleCode.eq(authorRoleRelated.authorRoleRelatedId.roleCode)
                                .and(authorRoleRelated.authorRoleRelatedId.authorCode.eq(searchKeyword)))
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Transactional
    @Override
    public AuthorRoleRelatedVO insert(AuthorRoleRelatedVO authorRoleRelatedVO) {
        AuthorRoleRelated authorRoleRelated = EgovAuthorInfoUtility.authorRoleRelatedVOToEntity(authorRoleRelatedVO);
        authorRoleRelated.setCreatDt(LocalDateTime.now());
        return EgovAuthorInfoUtility.authorRoleRelatedEntityToVO(repository.save(authorRoleRelated));
    }

    @Transactional
    @Override
    public boolean delete(AuthorRoleRelatedVO authorRoleRelatedVO) {
        AuthorRoleRelated authorRoleRelated = EgovAuthorInfoUtility.authorRoleRelatedVOToEntity(authorRoleRelatedVO);
        repository.delete(authorRoleRelated);
        return !repository.existsById(authorRoleRelated.getAuthorRoleRelatedId());
    }

    @Transactional
    @Override
    public boolean updateRoleInfoList(String[] authorCodes, String[] roleCodes, String[] regYns, AuthorRoleRelatedVO authorRoleRelatedVO) {
        boolean result = false;
        for (int i = 0; i < authorCodes.length; i++) {
            authorRoleRelatedVO.setAuthorCode(authorCodes[i]);
            authorRoleRelatedVO.setRoleCode(roleCodes[i]);

            if ("Y".equals(regYns[i])) {
                result = this.delete(authorRoleRelatedVO);
                this.insert(authorRoleRelatedVO);
            } else {
                result = this.delete(authorRoleRelatedVO);
            }
        }
        return result;
    }

}
