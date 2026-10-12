package egovframework.com.cop.brd.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardEvent {

    private BoardEventType eventType;
    private Long nttId;
    private String bbsId;
    private String nttSj;
    private String nttCn;
    private LocalDateTime eventDateTime;

}
