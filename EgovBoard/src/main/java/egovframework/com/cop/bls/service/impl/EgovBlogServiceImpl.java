package egovframework.com.cop.bls.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.cop.bls.entity.*;
import egovframework.com.cop.bls.repository.EgovBlogRepository;
import egovframework.com.cop.bls.repository.EgovBlogUserRepository;
import egovframework.com.cop.bls.service.BlogDTO;
import egovframework.com.cop.bls.service.BlogUserVO;
import egovframework.com.cop.bls.service.BlogVO;
import egovframework.com.cop.bls.service.EgovBlogService;
import egovframework.com.cop.bls.util.EgovBlogUtility;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service("blsEgovBlogService")
public class EgovBlogServiceImpl extends EgovAbstractServiceImpl implements EgovBlogService {

    private final EgovBlogRepository repository;
    private final EgovBlogUserRepository userRepository;
    private final EgovIdGnrService idgenService;
    private final JPAQueryFactory queryFactory;

    public EgovBlogServiceImpl(
            EgovBlogRepository repository,
            EgovBlogUserRepository userRepository,
            @Qualifier("egovBlogIdGnrService") EgovIdGnrService idgenService,
            JPAQueryFactory queryFactory
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.idgenService = idgenService;
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<BlogDTO> list(BlogVO blogVO) {
        Pageable pageable = PageRequest.of(blogVO.getFirstIndex(), blogVO.getRecordCountPerPage());
        String searchCondition = blogVO.getSearchCondition();
        String searchKeyword = blogVO.getSearchKeyword();

        QBlog blog = QBlog.blog;
        QUserMaster userMaster = QUserMaster.userMaster;
        QCmmnDetailCode cmmnDetailCode = QCmmnDetailCode.cmmnDetailCode;

        BooleanBuilder where = new BooleanBuilder();

        if ("1".equals(searchCondition)) {
            if (searchKeyword == null || searchKeyword.isEmpty()) {
            } else {
                where.and(blog.blogNm.contains(searchKeyword));;
            }
        }
        List<Tuple> results = queryFactory
                .select(blog, userMaster, cmmnDetailCode)
                .from(blog)
                .leftJoin(userMaster)
                .on(blog.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(cmmnDetailCode)
                .on(blog.registSeCode.eq(cmmnDetailCode.cmmnDetailCodeId.code)
                        .and(cmmnDetailCode.useAt.eq("Y"))
                        .and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM001")))
                .where(where)
                .orderBy(blog.frstRegistPnttm.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(blog.count())
                        .from(blog)
                        .leftJoin(userMaster)
                        .on(blog.frstRegisterId.eq(userMaster.esntlId))
                        .leftJoin(cmmnDetailCode)
                        .on(blog.registSeCode.eq(cmmnDetailCode.cmmnDetailCodeId.code)
                                .and(cmmnDetailCode.useAt.eq("Y"))
                                .and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM001")))
                        .where(where)
                        .fetchOne()
        ).orElse(0L);

        List<BlogDTO> content = results.stream().map(tuple -> {
            Blog b = tuple.get(blog);
            UserMaster user =tuple.get(userMaster);

            String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

            return new BlogDTO(
                Objects.requireNonNull(b).getBlogId(),
                b.getBlogNm(),
                b.getBlogIntrcn(),
                b.getUseAt(),
                b.getRegistSeCode(),
                b.getTmplatId(),
                b.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                b.getFrstRegisterId(),
                b.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                b.getLastUpdusrId(),
                userNm,
        ""
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(content,pageable,total);
    }

    @Override
    public BlogDTO detail(BlogVO blogVO) {
        QBlog blog =QBlog.blog;
        QUserMaster userMaster = QUserMaster.userMaster;
        QTmplatInfo tmplatInfo = QTmplatInfo.tmplatInfo;

        Tuple tuple = queryFactory
                .select(blog,userMaster,tmplatInfo)
                .from(blog)
                .leftJoin(userMaster)
                .on(blog.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(tmplatInfo)
                .on(blog.tmplatId.eq(tmplatInfo.tmplatId))
                .where(blog.blogId.eq(blogVO.getBlogId()))
                .fetchOne();

        Blog b = Objects.requireNonNull(tuple).get(blog);
        UserMaster user =tuple.get(userMaster);
        TmplatInfo tmplat = tuple.get(tmplatInfo);

        String tmplatNm = tmplat != null && tmplat.getTmplatNm() != null ? tmplat.getTmplatNm() : "";
        String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

        return new BlogDTO(
                Objects.requireNonNull(b).getBlogId(),
                b.getBlogNm(),
                b.getBlogIntrcn(),
                b.getUseAt(),
                b.getRegistSeCode(),
                b.getTmplatId(),
                b.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                b.getFrstRegisterId(),
                b.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                b.getLastUpdusrId(),
                userNm,
                tmplatNm
        );
    }

    @Transactional
    @Override
    public BlogVO insert(BlogVO blogVO, Map<String, String> userInfo) {
        try {
            String blogId = idgenService.getNextStringId();

            blogVO.setBlogId(blogId);
            blogVO.setRegistSeCode("REGC02");
            blogVO.setFrstRegistPnttm(LocalDateTime.now());
            blogVO.setFrstRegisterId(userInfo.get("uniqId"));
            blogVO.setLastUpdtPnttm(LocalDateTime.now());
            blogVO.setLastUpdusrId(userInfo.get("uniqId"));
            Blog blog = repository.save(EgovBlogUtility.blogVOToEntity(blogVO));

            BlogUserVO blogUserVO = getBlogUserVO(blogId, userInfo.get("uniqId"));
            userRepository.save(EgovBlogUtility.blogUserVOToEntity(blogUserVO));

            return EgovBlogUtility.blogEntityToVO(blog);
        //2026.02.28 KISA 보안취약점 조치
        } catch (FdlException ex) {
            leaveaTrace("fail.common.insert");
            return null;
        }
    }

    @Transactional
    @Override
    public BlogVO update(BlogVO blogVO, Map<String, String> userInfo) {
        String uniqId = userInfo != null ? userInfo.get("uniqId") : null;
        if (ObjectUtils.isEmpty(uniqId)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        return repository.findById(blogVO.getBlogId())
                .map(result -> {
                    // 2026.07.13 KISA 보안취약점 조치 - 소유자만 수정 가능
                    if (!Objects.equals(uniqId, result.getFrstRegisterId())) {
                        throw new IllegalStateException("수정 권한이 없습니다.");
                    }
                    result.setBlogNm(blogVO.getBlogNm());
                    result.setBlogIntrcn(blogVO.getBlogIntrcn());
                    result.setLastUpdtPnttm(LocalDateTime.now());
                    result.setLastUpdusrId(uniqId);
                    return repository.save(result);
                })
                .map(EgovBlogUtility::blogEntityToVO).orElse(null);
    }

    private BlogUserVO getBlogUserVO(String blogId, String uniqId) {
        BlogUserVO blogUserVO = new BlogUserVO();
        blogUserVO.setBlogId(blogId);
        blogUserVO.setEmplyrId(uniqId);
        blogUserVO.setMngrAt("Y");
        blogUserVO.setMberSttus("P");
        blogUserVO.setSbscrbDe(LocalDateTime.now());
        blogUserVO.setUseAt("Y");
        blogUserVO.setFrstRegistPnttm(LocalDateTime.now());
        blogUserVO.setFrstRegisterId(uniqId);
        blogUserVO.setLastUpdtPnttm(LocalDateTime.now());
        blogUserVO.setLastUpdusrId(uniqId);
        return blogUserVO;
    }

}
