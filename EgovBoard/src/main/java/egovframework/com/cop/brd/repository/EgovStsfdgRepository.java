package egovframework.com.cop.brd.repository;

import egovframework.com.cop.brd.entity.Stsfdg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("brdEgovStsfdgRepository")
public interface EgovStsfdgRepository extends JpaRepository<Stsfdg, String> {

    Page<Stsfdg> findAllByBbsIdAndNttIdAndUseAt(String bbsId, Long nttId, String useAt, Pageable pageable);

    List<Stsfdg> findAllByBbsIdAndNttIdAndUseAt(String bbsId, Long nttId, String useAt);

}
