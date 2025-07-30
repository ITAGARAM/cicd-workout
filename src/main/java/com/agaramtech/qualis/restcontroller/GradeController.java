package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.service.grade.GradeService;
import com.fasterxml.jackson.databind.ObjectMapper;



@RestController
@RequestMapping("/grade")
public class GradeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(GradeController.class);

	private RequestContext requestContext;
	private final GradeService gradeService;
	
	public GradeController(RequestContext requestContext, GradeService gradeService) {
		this.requestContext = requestContext;
		this.gradeService = gradeService;
	}
	
	@RequestMapping(value = "/getGrade", method = RequestMethod.POST)
	public ResponseEntity<Object> getParamterType(@RequestBody Map<String, Object> inputMap) throws Exception{
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(objUserInfo);
			LOGGER.info("UserInfo:"+ objUserInfo);
			return gradeService.getGrade(objUserInfo);
	
	}
}
