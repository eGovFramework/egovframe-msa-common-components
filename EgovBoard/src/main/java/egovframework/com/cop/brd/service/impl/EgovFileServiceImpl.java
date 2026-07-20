package egovframework.com.cop.brd.service.impl;

import egovframework.com.cop.brd.entity.File;
import egovframework.com.cop.brd.entity.FileDetail;
import egovframework.com.cop.brd.entity.FileDetailId;
import egovframework.com.cop.brd.repository.EgovFileDetailRepository;
import egovframework.com.cop.brd.repository.EgovFileRepository;
import egovframework.com.cop.brd.service.EgovFileService;
import egovframework.com.cop.brd.service.FileVO;
import egovframework.com.cop.brd.util.EgovBoardUtility;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("brdEgovFileService")
@Slf4j
public class EgovFileServiceImpl extends EgovAbstractServiceImpl implements EgovFileService {

    private final EgovFileRepository fileRepository;
    private final EgovFileDetailRepository fileDetailRepository;
    private final EgovIdGnrService idGnrService;

    public EgovFileServiceImpl(
            EgovFileRepository fileRepository,
            EgovFileDetailRepository fileDetailRepository,
            @Qualifier("egovFileIdGnrService") EgovIdGnrService idGnrService
    ) {
        this.fileRepository = fileRepository;
        this.fileDetailRepository = fileDetailRepository;
        this.idGnrService = idGnrService;
    }

    @Override
    public List<FileVO> selectFileInfs(String atchFileId) {
        return fileDetailRepository.findAllByFileDetailId_AtchFileId(atchFileId)
                .stream()
                .map(EgovBoardUtility::fileDeatailEntityToVO)
                .collect(Collectors.toList());
    }

    // 2026.07.13 KISA 보안취약점 조치: fileSn 산정(count) 시점과 저장 시점 사이의 TOCTOU 경쟁 조건으로
    // 인해 동시 요청이 동일한 fileSn을 계산해 서로 덮어쓸 수 있는 문제를 방지하기 위해
    // 메서드 전체를 동기화하고, 실제 저장 직전에 해당 PK(atchFileId+fileSn)가 이미 존재하는지 재확인하여
    // 존재하면 사용 가능한 다음 순번을 찾을 때까지 재시도한다.
    @Transactional
    @Override
    public synchronized String insertFileInf(FileVO fvo) {
        int fileCount = fileDetailRepository.findAllByFileDetailId_AtchFileId(fvo.getAtchFileId()).size();

        if (fileCount > 0) {
            fvo.setFileSn(String.valueOf(fileCount));
        }

        File file = new File();
        FileDetail filedetail = new FileDetail();
        BeanUtils.copyProperties(fvo, file);
        BeanUtils.copyProperties(fvo, filedetail);

        file.setAtchFileId(fvo.getAtchFileId());
        file.setUseAt("Y");
        file.setCreatDt(LocalDateTime.now());

        String candidateFileSn = fvo.getFileSn().isEmpty() ? "0" : fvo.getFileSn();
        FileDetailId filedetailId = new FileDetailId();
        filedetailId.setAtchFileId(fvo.getAtchFileId());
        filedetailId.setFileSn(candidateFileSn);

        // 저장 직전 재확인: 계산된 fileSn이 이미 사용 중이면(경쟁 조건 발생) 사용 가능한 다음 값으로 재시도
        int candidateSn;
        try {
            candidateSn = Integer.parseInt(candidateFileSn);
        } catch (NumberFormatException e) {
            candidateSn = 0;
        }
        while (fileDetailRepository.findById(filedetailId).isPresent()) {
            candidateSn++;
            filedetailId = new FileDetailId();
            filedetailId.setAtchFileId(fvo.getAtchFileId());
            filedetailId.setFileSn(String.valueOf(candidateSn));
        }
        fvo.setFileSn(filedetailId.getFileSn());
        filedetail.setFileDetailId(filedetailId);

        file.setCreatDt(LocalDateTime.now());

        fileRepository.save(file);
        fileDetailRepository.save(filedetail);

        return fvo.getAtchFileId();
    }

    @Transactional
    @Override
    public String insertFiles(List<FileVO> fileList) throws FdlException {
        String attachFileId = idGnrService.getNextStringId();
        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).getAtchFileId() != null) {
                // 추가등록
                attachFileId = fileList.get(i).getAtchFileId();
                fileList.get(i).setAtchFileId(attachFileId);
                fileList.get(i).setFileSn(String.valueOf(i));
            } else {
                // 신규등록
                fileList.get(i).setAtchFileId(attachFileId);
                fileList.get(i).setFileSn(String.valueOf(i));
            }
            attachFileId = insertFileInf(fileList.get(i));
        }
        return attachFileId;
    }

    @Override
    public void deleteFileInfs(FileVO fileVO) {
        if (ObjectUtils.isEmpty(fileVO.getDeleteFileSn()) || ObjectUtils.isEmpty(fileVO.getFileSn())) {
            return;
        }

        for (String num : fileVO.getDeleteFileSn()) {
            // 토큰으로 검증된 단일 fileSn과 일치하는 값만 삭제한다.
            if (!fileVO.getFileSn().equals(num)) {
                continue;
            }
            FileDetailId filedetailId = new FileDetailId();
            filedetailId.setAtchFileId(fileVO.getAtchFileId());
            filedetailId.setFileSn(num);
            fileDetailRepository.deleteById(filedetailId);
        }

        List<FileVO> flist = selectFileInfs(fileVO.getAtchFileId());
        if (!flist.isEmpty()) {
            for (int i = 0; i < flist.size(); i++) {
                FileVO fvo = flist.get(i);
                FileDetailId fileDetailId = new FileDetailId();
                fileDetailId.setAtchFileId(fvo.getAtchFileId());
                fileDetailId.setFileSn(fvo.getFileSn());
                Optional<FileDetail> optionalFileDetail = fileDetailRepository.findById(fileDetailId);

                if (optionalFileDetail.isPresent()) {
                    FileDetail fileDetail = optionalFileDetail.get();

                    // 기존 엔티티 삭제
                    fileDetailRepository.delete(fileDetail);

                    String fileSn = String.valueOf(i);

                    // 새로운 엔티티 생성
                    FileDetail newFileDetail = new FileDetail();
                    BeanUtils.copyProperties(fvo, newFileDetail);

                    FileDetailId newFileDetailId = new FileDetailId();
                    newFileDetailId.setAtchFileId(fvo.getAtchFileId());
                    newFileDetailId.setFileSn(fileSn);

                    newFileDetail.setFileDetailId(newFileDetailId);

                    // 저장
                    fileDetailRepository.save(newFileDetail);
                }
            }
        }
    }

    @Override
    public FileVO detailFileInf(FileVO fileVO) {
        FileDetailId fileDetailId = new FileDetailId();
        fileDetailId.setAtchFileId(fileVO.getAtchFileId());
        fileDetailId.setFileSn(fileVO.getFileSn());
        fileVO = EgovBoardUtility.fileDeatailEntityToVO(fileDetailRepository.findById(fileDetailId).get());
        return fileVO;
    }
}
