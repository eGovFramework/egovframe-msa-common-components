package egovframework.com.sec.rgm.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.sec.rgm.service.AuthorGroupDTO;
import egovframework.com.sec.rgm.service.AuthorGroupVO;
import egovframework.com.sec.rgm.service.AuthorInfoVO;
import egovframework.com.sec.rgm.service.EgovAuthorGroupService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("rgmEgovAuthorGroupAPIController")
@RequestMapping("/sec/rgm")
@RequiredArgsConstructor
public class EgovAuthorGroupAPIController {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovAuthorGroupService service;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;

    @PostMapping(value="/authorGroupList")
    public ResponseEntity<?> authorGroupList(@ModelAttribute AuthorGroupVO authorGroupVO) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(authorGroupVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        authorGroupVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        authorGroupVO.setLastIndex(paginationInfo.getLastRecordIndex());
        authorGroupVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<AuthorGroupDTO> list = service.list(authorGroupVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        List<AuthorInfoVO> authorInfoList = service.authorInfoList();

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("authorGroupList", list.getContent());
        response.put("authorInfoList", authorInfoList);
        response.put("pagination", pagination);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value="/authorGroupInsert")
    public ResponseEntity<?> authorGroupInsert(
            @RequestParam String userIds,
            @RequestParam String mberTyCode,
            @RequestParam String authorCode,
            @RequestParam String regYns,
            @ModelAttribute AuthorGroupVO authorGroupVO)
    {
        String[] strUserIds = userIds.split(";");
        String[] strMberTyCodes = mberTyCode.split(";");
        String[] strAuthorCodes = authorCode.split(";");
        String[] strRegYns = regYns.split(";");

        for (int i = 0; i < strUserIds.length; i++) {
            authorGroupVO.setScrtyDtrmnTrgetId(strUserIds[i]);
            authorGroupVO.setMberTyCode(strMberTyCodes[i]);
            authorGroupVO.setAuthorCode(strAuthorCodes[i]);

            if ("N".equals(strRegYns[i])) {
                service.insert(authorGroupVO);
            } else {
                service.update(authorGroupVO);
            }
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value="/authorGroupDelete")
    public ResponseEntity<?> authorGroupDelete(@RequestParam String userIds, @ModelAttribute AuthorGroupVO authorGroupVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        String[] strUserIds = userIds.split(";");
        for (String strUserId : strUserIds) {
            authorGroupVO.setScrtyDtrmnTrgetId(strUserId);
            service.delete(authorGroupVO, userInfo);
        }

        return ResponseEntity.ok().build();
    }

    private Map<String, String> extracted(HttpServletRequest request) {
        Map<String, String> userInfo = new HashMap<>();
        String encryptUserId = request.getHeader("X-USER-ID");
        String encryptUserNm = request.getHeader("X-USER-NM");
        String encryptUniqId = request.getHeader("X-UNIQ-ID");
        userInfo.put("userId", egovEnvCryptoService.decrypt(encryptUserId));
        userInfo.put("userName", egovEnvCryptoService.decrypt(encryptUserNm));
        userInfo.put("uniqId", egovEnvCryptoService.decrypt(encryptUniqId));
        return userInfo;
    }

}
