package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.RetrievalCertificate;
import com.agaramtech.qualis.storagemanagement.service.retrievalcertificate.RetrievalCertificateService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/retrievalcertificate")
public class RetrievalCertificateController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RetrievalCertificateController.class);

	private final RetrievalCertificateService retrievalCertificateService;
	private RequestContext requestContext;

	public RetrievalCertificateController(RetrievalCertificateService retrievalCertificateService,
			RequestContext requestContext) {
		super();
		this.retrievalCertificateService = retrievalCertificateService;
		this.requestContext = requestContext;
	}

	@PostMapping(path = "/getRetrievalCertificate")
	public ResponseEntity<Object> getRetrievalCertificate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		Integer nretrievalcertificatecode = null;
		if (inputMap.get("nretrievalcertificatecode") != null) {
			nretrievalcertificatecode = (Integer) inputMap.get("nretrievalcertificatecode");
		}
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return retrievalCertificateService.getRetrievalCertificate(nretrievalcertificatecode, fromDate, toDate,
				userInfo, currentUIDate);

	}

	@PostMapping(value = "/RetrievalReportCertificate")
	public ResponseEntity<Object> retrievalreportcertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("retrievalreportcertificate called");
		return retrievalCertificateService.retrievalreportcertificate(inputMap, userInfo);
	}

	@PostMapping(value = "/getProjectUsers")
	public ResponseEntity<Object> getProjectUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int nprojectmastercode = 0;
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		if ((inputMap.get("nprojectmastercode")) != null) {
			nprojectmastercode = (Integer) inputMap.get("nprojectmastercode");
		}
		return retrievalCertificateService.getProjectUsers(nprojectmastercode, userInfo);
	}

	@PostMapping(value = "/getProject")
	public ResponseEntity<Object> getProject(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int nprojectTypeCode = -1;
		if (inputMap.containsKey("nprojecttypecode")) {
			nprojectTypeCode = (int) inputMap.get("nprojecttypecode");
		}
		requestContext.setUserInfo(userInfo);
		return retrievalCertificateService.getProject(nprojectTypeCode, userInfo);
	}

	@PostMapping(value = "/getSiteAddress")
	public ResponseEntity<Object> getSiteAddress(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return retrievalCertificateService.getSiteAddress(userInfo);
	}

	@PostMapping(path = "/createRetrievalCertificate")
	public ResponseEntity<Object> createRetrievalCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final String currentUIDate = (String) inputMap.get("currentdate");
		final RetrievalCertificate trainingcertificate = objMapper.convertValue(inputMap.get("retrievalcertificate"),
				RetrievalCertificate.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return retrievalCertificateService.createRetrievalCertificate(trainingcertificate, userInfo, fromDate, toDate,
				currentUIDate);

	}

	@PostMapping(path = "/getActiveRetrievalCertificateById")
	public ResponseEntity<Object> getActiveRetrievalCertificateById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nretrievalcertificatecode = (Integer) inputMap.get("nretrievalcertificatecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return retrievalCertificateService.getActiveRetrievalCertificateById(nretrievalcertificatecode, userInfo);
	}

	@PostMapping(path = "/deleteRetrievalCertificate")
	public ResponseEntity<Object> deleteRetrievalCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final RetrievalCertificate retrievalcertificate = objMapper.convertValue(inputMap.get("retrievalcertificate"),
				RetrievalCertificate.class);
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return retrievalCertificateService.deleteRetrievalCertificate(retrievalcertificate, fromDate, toDate, userInfo);

	}

	@PostMapping(path = "/updateRetrievalCertificate")
	public ResponseEntity<Object> updateRetrievalCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final RetrievalCertificate retrievalcertificate = objMapper.convertValue(inputMap.get("retrievalcertificate"),
				RetrievalCertificate.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		requestContext.setUserInfo(userInfo);
		return retrievalCertificateService.updateRetrievalCertificate(retrievalcertificate, fromDate, toDate, userInfo);
	}

	@PostMapping(path = "/completeRetrievalCertificate")
	public ResponseEntity<Object> completeRetrievalCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final RetrievalCertificate retrievalcertificate = objMapper.convertValue(inputMap.get("retrievalcertificate"),
				RetrievalCertificate.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return retrievalCertificateService.completeRetrievalCertificate(retrievalcertificate, fromDate, toDate,
				userInfo);
	}

	@PostMapping(path = "/checkRetrievalCertificate")
	public ResponseEntity<Object> checkRetrievalCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final RetrievalCertificate retrievalcertificate = objMapper.convertValue(inputMap.get("retrievalcertificate"),
				RetrievalCertificate.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return retrievalCertificateService.checkRetrievalCertificate(retrievalcertificate, fromDate, toDate, userInfo);
	}

	@PostMapping(path = "/approveRetrievalCertificate")
	public ResponseEntity<Object> approveRetrievalCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final RetrievalCertificate retrievalcertificate = objMapper.convertValue(inputMap.get("retrievalcertificate"),
				RetrievalCertificate.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return retrievalCertificateService.approveRetrievalCertificate(retrievalcertificate, fromDate, toDate,
				userInfo);
	}
}
