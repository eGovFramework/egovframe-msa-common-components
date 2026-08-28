package egovframework.com.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.egovframe.rte.fdl.cmmn.exception.BaseRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

@SpringBootTest
@AutoConfigureMockMvc
class EgovMainManageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void indexGet() throws BaseRuntimeException, Exception {
		// given
		String uriTemplate = "/";

		// when
		MvcResult result = mockMvc.perform(get(uriTemplate)).andExpect(status().isOk()).andDo(print()).andReturn();

		// then
		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView).isNotNull();
		assertThat(modelAndView.getViewName()).isEqualTo("index");
	}

	@Test
	void indexPost() throws BaseRuntimeException, Exception {
		// given
		String uriTemplate = "/";

		// when
		MvcResult result = mockMvc.perform(post(uriTemplate)).andExpect(status().isOk()).andDo(print()).andReturn();

		// then
		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView).isNotNull();
		assertThat(modelAndView.getViewName()).isEqualTo("index");
	}
}
