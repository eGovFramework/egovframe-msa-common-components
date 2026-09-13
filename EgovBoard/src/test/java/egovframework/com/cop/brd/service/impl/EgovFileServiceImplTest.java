package egovframework.com.cop.brd.service.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import egovframework.com.cop.brd.entity.FileDetailId;
import egovframework.com.cop.brd.repository.EgovFileDetailRepository;
import egovframework.com.cop.brd.repository.EgovFileRepository;
import egovframework.com.cop.brd.service.FileVO;

@ExtendWith(MockitoExtension.class)
class EgovFileServiceImplTest {

    @Mock
    EgovFileRepository fileRepository;

    @Mock
    EgovFileDetailRepository fileDetailRepository;

    @Mock
    EgovIdGnrService idGnrService;

    @InjectMocks
    EgovFileServiceImpl service;

    @Test
    void deleteFileInfsDeletesValidatedFileNumbers() {
        FileVO request = new FileVO();
        request.setAtchFileId("FILE_TEST");
        request.setDeleteFileSn(new String[] {"0", "2"});
        when(fileDetailRepository.findAllByFileDetailId_AtchFileId("FILE_TEST"))
                .thenReturn(Collections.emptyList());

        service.deleteFileInfs(request);

        verify(fileDetailRepository).deleteById(fileId("FILE_TEST", "0"));
        verify(fileDetailRepository).deleteById(fileId("FILE_TEST", "2"));
    }

    private FileDetailId fileId(String attachmentId, String fileNumber) {
        FileDetailId id = new FileDetailId();
        id.setAtchFileId(attachmentId);
        id.setFileSn(fileNumber);
        return id;
    }
}
