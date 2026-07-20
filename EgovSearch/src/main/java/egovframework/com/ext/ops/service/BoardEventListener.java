package egovframework.com.ext.ops.service;

import egovframework.com.ext.ops.entity.BbsSyncLog;
import egovframework.com.ext.ops.event.BoardEvent;
import egovframework.com.ext.ops.repository.EgovBbsRepository;
import egovframework.com.ext.ops.repository.EgovBbsSyncLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardEventListener {
	private final EgovBbsSyncLogRepository comtnbbssynclogRepository;
    private final EgovBbsRepository comtnbbsRepository;
    private final EgovOpenSearchService egovOpenSearchService;
    
    @Bean
    public Consumer<BoardEvent> searchConsumer() {
        return this::handleBoardEvent;
    }
    
    public void handleBoardEvent(BoardEvent event) {
    	try {
    		// Event의 nttId로 COMTNBBSSYNCLOG의 가장 최근 데이터가 Pending 상태인지 확인
    		Optional<BbsSyncLog> syncLogOpt = comtnbbssynclogRepository.findTopByNttIdOrderByRegistPnttmDesc(event.getNttId());
            if (syncLogOpt.isEmpty() || !"P".equals(syncLogOpt.get().getSyncSttusCode())) {
                return;
            }
    		
    		// 게시글 데이터 조회
            Optional<BoardVO> boardVOOptional = comtnbbsRepository.findBBSDTOByNttId(event.getNttId())
                    .map(dto -> {
                        BoardVO vo = new BoardVO();
                        BeanUtils.copyProperties(dto, vo);
                        return vo;
                    });
    		
    		if (boardVOOptional.isPresent()) {
    			// OpenSearch 처리
    			egovOpenSearchService.processOpenSearchOperations(event.getNttId(), boardVOOptional.get());
    			
    			Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MILLISECOND, 0);
                Date dateWithoutMillis = calendar.getTime();
                
                List<BbsSyncLog> allPendingLogs = comtnbbssynclogRepository.findByNttIdAndSyncSttusCode(event.getNttId(), "P");
                
                // 성공 시 상태 업데이트
                for (BbsSyncLog log : allPendingLogs) {
                    log.setSyncSttusCode("C");  // Completed
                    log.setSyncPnttm(dateWithoutMillis);
                    comtnbbssynclogRepository.save(log);
                }
                
                log.info("Successfully processed nttId: {} and updated {} logs", event.getNttId(), allPendingLogs.size());
            } else {
            	 // 게시글을 찾을 수 없는 경우
            	Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MILLISECOND, 0);
                Date dateWithoutMillis = calendar.getTime();
                
                List<BbsSyncLog> allPendingLogs = comtnbbssynclogRepository.findByNttIdAndSyncSttusCode(event.getNttId(), "P");

                for (BbsSyncLog log : allPendingLogs) {
                    log.setSyncSttusCode("F");  // Failed
                    log.setErrorPnttm(dateWithoutMillis);
                    comtnbbssynclogRepository.save(log);
                }
                
                log.error("Board not found with id: {}, marked {} logs as failed", event.getNttId(), allPendingLogs.size());
            }
        //2026.02.28 KISA 보안취약점 조치
    	} catch (DataAccessException | BeansException e) {
    		log.error("Failed to process board event: " + event.getNttId(), e);
    		
    		Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MILLISECOND, 0);
            Date dateWithoutMillis = calendar.getTime();
    		
            List<BbsSyncLog> allPendingLogs = comtnbbssynclogRepository.findByNttIdAndSyncSttusCode(event.getNttId(), "P");
    		
            for (BbsSyncLog log : allPendingLogs) {
                log.setSyncSttusCode("F");  // Failed
                log.setErrorPnttm(dateWithoutMillis);
                comtnbbssynclogRepository.save(log);
            }
    	}
    }
}
