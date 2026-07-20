package egovframework.com.uss.olp.qtm.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.uss.olp.qtm.entity.QQustnrTmplat;
import egovframework.com.uss.olp.qtm.entity.QUserMaster;
import egovframework.com.uss.olp.qtm.entity.QustnrTmplat;
import egovframework.com.uss.olp.qtm.entity.UserMaster;
import egovframework.com.uss.olp.qtm.repository.*;
import egovframework.com.uss.olp.qtm.service.EgovQustnrTmplatService;
import egovframework.com.uss.olp.qtm.service.QustnrTmplatDTO;
import egovframework.com.uss.olp.qtm.service.QustnrTmplatVO;
import egovframework.com.uss.olp.qtm.util.EgovQustnrTmplatUtility;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("qtmEgovQustnrTmplatService")
public class EgovQustnrTmplatServiceImpl extends EgovAbstractServiceImpl implements EgovQustnrTmplatService {

    private final EgovQustnrTmplatRepository repository;
    private final EgovQestnrInfoRepository egovQestnrInfoRepository;
    private final EgovQustnrQesitmRepository egovQustnrQesitmRepository;
    private final EgovQustnrIemRepository egovQustnrIemRepository;
    private final EgovQustnrRespondInfoRepository egovQustnrRespondInfoRepository;
    private final EgovQustnrRspnsResultRepository egovQustnrRspnsResultRepository;
    private final EgovIdGnrService idgenService;
    private final JPAQueryFactory queryFactory;

    public EgovQustnrTmplatServiceImpl(
            EgovQustnrTmplatRepository repository,
            EgovQestnrInfoRepository egovQestnrInfoRepository,
            EgovQustnrQesitmRepository egovQustnrQesitmRepository,
            EgovQustnrIemRepository egovQustnrIemRepository,
            EgovQustnrRespondInfoRepository egovQustnrRespondInfoRepository,
            EgovQustnrRspnsResultRepository egovQustnrRspnsResultRepository,
            @Qualifier("egovQustnrTmplatManageIdGnrService") EgovIdGnrService idgenService,
            JPAQueryFactory queryFactory) {
        this.repository = repository;
        this.egovQestnrInfoRepository = egovQestnrInfoRepository;
        this.egovQustnrQesitmRepository = egovQustnrQesitmRepository;
        this.egovQustnrIemRepository = egovQustnrIemRepository;
        this.egovQustnrRespondInfoRepository = egovQustnrRespondInfoRepository;
        this.egovQustnrRspnsResultRepository = egovQustnrRspnsResultRepository;
        this.idgenService = idgenService;
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<QustnrTmplatDTO> list(QustnrTmplatVO qustnrTmplatVO) {
        Pageable pageable = PageRequest.of(qustnrTmplatVO.getFirstIndex(), qustnrTmplatVO.getRecordCountPerPage());
        String searchCondition = qustnrTmplatVO.getSearchCondition();
        String searchKeyword = qustnrTmplatVO.getSearchKeyword();

        QQustnrTmplat qustnrTmplat = QQustnrTmplat.qustnrTmplat;
        QUserMaster userMaster = QUserMaster.userMaster;

        BooleanBuilder where = new BooleanBuilder();
        if ("1".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(qustnrTmplat.qustnrTmplatDc.contains(searchKeyword));
        } else if ("2".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(qustnrTmplat.qustnrTmplatTy.contains(searchKeyword));
        }

        List<Tuple> results = qustnrTmplatQuery()
                .where(where)
                .orderBy(qustnrTmplat.frstRegistPnttm.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(qustnrTmplat.count())
                        .from(qustnrTmplat)
                        .leftJoin(userMaster)
                        .on(qustnrTmplat.frstRegisterId.eq(userMaster.esntlId))
                        .where(where)
                        .fetchOne()
        ).orElse(0L);

        List<QustnrTmplatDTO> content = results.stream().map(tuple -> {
            QustnrTmplat qtm = tuple.get(qustnrTmplat);
            UserMaster user = tuple.get(userMaster);
            String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

            return  new QustnrTmplatDTO(
                Objects.requireNonNull(qtm).getQustnrTmplatId(),
                qtm.getQustnrTmplatTy(),
                qtm.getQustnrTmplatDc(),
                qtm.getQustnrTmplatPathNm(),
                qtm.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qtm.getFrstRegisterId(),
                qtm.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qtm.getLastUpdusrId(),
                qtm.getQustnrTmplatImageInfo(),
                userNm
            );
        }).collect(Collectors.toList());
        return new PageImpl<>(content,pageable,total);
    }

    @Override
    public QustnrTmplatDTO detail(QustnrTmplatVO qustnrTmplatVO, Map<String, String> userInfo) {
        QustnrTmplat template = repository.findById(qustnrTmplatVO.getQustnrTmplatId()).orElse(null);
        if (template == null) {
            return null;
        }
        assertOwner(template.getFrstRegisterId(), userInfo);

        QQustnrTmplat qustnrTmplat = QQustnrTmplat.qustnrTmplat;
        QUserMaster userMaster = QUserMaster.userMaster;

        Tuple tuple = qustnrTmplatQuery().where(qustnrTmplat.qustnrTmplatId.eq(qustnrTmplatVO.getQustnrTmplatId())).fetchOne();

        QustnrTmplat qtm = tuple.get(qustnrTmplat);
        UserMaster user = tuple.get(userMaster);
        String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

        return  new QustnrTmplatDTO(
                Objects.requireNonNull(qtm).getQustnrTmplatId(),
                qtm.getQustnrTmplatTy(),
                qtm.getQustnrTmplatDc(),
                qtm.getQustnrTmplatPathNm(),
                qtm.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qtm.getFrstRegisterId(),
                qtm.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qtm.getLastUpdusrId(),
                qtm.getQustnrTmplatImageInfo(),
                userNm
        );
    }

    @Transactional
    @Override
    public QustnrTmplatVO insert(QustnrTmplatVO qustnrTmplatVO, Map<String, String> userInfo) {
        try {
            String qustnrTmplatId = idgenService.getNextStringId();
            qustnrTmplatVO.setQustnrTmplatId(qustnrTmplatId);

            QustnrTmplat qustnrTmplat = EgovQustnrTmplatUtility.qustnrTmplatVOToEntity(qustnrTmplatVO);
            qustnrTmplat.setQustnrTmplatImageInfo(validateAndGetImageBytes(qustnrTmplatVO.getQustnrTmplatImageInfo()));
            qustnrTmplat.setFrstRegistPnttm(LocalDateTime.now());
            qustnrTmplat.setFrstRegisterId(userInfo.get("uniqId"));
            qustnrTmplat.setLastUpdtPnttm(LocalDateTime.now());
            qustnrTmplat.setLastUpdusrId(userInfo.get("uniqId"));
            return EgovQustnrTmplatUtility.qustnrTmplatEntityToVO(repository.save(qustnrTmplat));
        //2026.02.28 KISA 보안취약점 조치
        } catch (FdlException | IOException ex) {
            leaveaTrace("fail.common.insert");
            return null;
        }
    }

    @Transactional
    @Override
    public QustnrTmplatVO update(QustnrTmplatVO qustnrTmplatVO, Map<String, String> userInfo) {
        String qustnrTmplatId = qustnrTmplatVO.getQustnrTmplatId();
        return repository.findById(qustnrTmplatId)
                .map(item -> {
                    assertOwner(item.getFrstRegisterId(), userInfo);
                    try {
                        return updateItem(item, qustnrTmplatVO, userInfo.get("uniqId"));
                    } catch (IOException e) {
                        return null;
                    }
                })
                .map(EgovQustnrTmplatUtility::qustnrTmplatEntityToVO)
                .orElse(null);
    }

    @Transactional
    @Override
    public boolean delete(QustnrTmplatVO qustnrTmplatVO, Map<String, String> userInfo) {
        String qustnrTmplatId = qustnrTmplatVO.getQustnrTmplatId();
        return repository.findById(qustnrTmplatId)
                .map(result -> {
                    assertOwner(result.getFrstRegisterId(), userInfo);
                    egovQustnrRspnsResultRepository.deleteByQustnrRspnsResultIdQustnrTmplatId(qustnrTmplatId);
                    egovQustnrRespondInfoRepository.deleteByQustnrRespondInfoIdQustnrTmplatId(qustnrTmplatId);
                    egovQustnrIemRepository.deleteByQustnrIemIdQustnrTmplatId(qustnrTmplatId);
                    egovQustnrQesitmRepository.deleteByQustnrQesitmIdQustnrTmplatId(qustnrTmplatId);
                    egovQestnrInfoRepository.deleteByQestnrInfoIdQustnrTmplatId(qustnrTmplatId);
                    repository.deleteById(qustnrTmplatId);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public byte[] getImage(String qustnrTmplatId, Map<String, String> userInfo) {
        return repository.findById(qustnrTmplatId)
                .map(template -> {
                    assertOwner(template.getFrstRegisterId(), userInfo);
                    return template.getQustnrTmplatImageInfo();
                })
                .orElseThrow(() -> new IllegalStateException("권한이 없습니다."));
    }

    private QustnrTmplat updateItem(QustnrTmplat qustnrTmplat, QustnrTmplatVO qustnrTmplatVO, String uniqId) throws IOException {
        if (!"update".equals(qustnrTmplatVO.getQustnrTmplatImageState())) {
            qustnrTmplat.setQustnrTmplatImageInfo(validateAndGetImageBytes(qustnrTmplatVO.getQustnrTmplatImageInfo()));
        }
        qustnrTmplat.setQustnrTmplatTy(qustnrTmplatVO.getQustnrTmplatTy());
        qustnrTmplat.setQustnrTmplatDc(qustnrTmplatVO.getQustnrTmplatDc());
        qustnrTmplat.setQustnrTmplatPathNm(qustnrTmplatVO.getQustnrTmplatPathNm());
        qustnrTmplat.setLastUpdtPnttm(LocalDateTime.now());
        qustnrTmplat.setLastUpdusrId(uniqId);
        return qustnrTmplat;
    }

    // 2026.07.13 KISA 보안취약점 조치: 업로드된 이미지의 매직바이트를 검증하여 위장된 파일(HTML/스크립트) 저장을 방지
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] GIF_MAGIC = {0x47, 0x49, 0x46, 0x38};

    private byte[] validateAndGetImageBytes(org.springframework.web.multipart.MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        if (!isValidImageContent(bytes)) {
            throw new IOException("업로드된 파일이 유효한 이미지(JPEG/PNG/GIF) 형식이 아닙니다.");
        }
        return bytes;
    }

    private boolean isValidImageContent(byte[] bytes) {
        return startsWith(bytes, JPEG_MAGIC) || startsWith(bytes, PNG_MAGIC) || startsWith(bytes, GIF_MAGIC);
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data == null || data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private JPAQuery<Tuple> qustnrTmplatQuery(){

        QQustnrTmplat qustnrTmplat = QQustnrTmplat.qustnrTmplat;
        QUserMaster userMaster = QUserMaster.userMaster;

        return queryFactory
                .select(qustnrTmplat,userMaster)
                .from(qustnrTmplat)
                .leftJoin(userMaster)
                .on(qustnrTmplat.frstRegisterId.eq(userMaster.esntlId));
    }

    private void assertOwner(String frstRegisterId, Map<String, String> userInfo) {
        String uniqId = userInfo != null ? userInfo.get("uniqId") : null;
        if (ObjectUtils.isEmpty(uniqId)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        // 2026.07.13 KISA 보안취약점 조치
        if (!Objects.equals(uniqId, frstRegisterId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }
    }

}
