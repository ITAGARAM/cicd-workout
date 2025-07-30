package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestPriceDetail;
import com.agaramtech.qualis.testmanagement.model.TestPriceVersion;
import com.agaramtech.qualis.testmanagement.service.testprice.TestPriceService;
import com.agaramtech.qualis.testmanagement.service.testprice.TestPriceVersionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/testpricing")
public class TestPricingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestPricingController.class);

	private RequestContext requestContext;
	private final TestPriceVersionService testPriceVersionService;
	private final TestPriceService testPriceService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext          RequestContext to hold the request
	 * @param testPriceVersionService TestPriceVersionService
	 * @param testPriceService        TestPriceService
	 */
	public TestPricingController(RequestContext requestContext, TestPriceVersionService testPriceVersionService,
			TestPriceService testPriceService) {
		super();
		this.requestContext = requestContext;
		this.testPriceVersionService = testPriceVersionService;
		this.testPriceService = testPriceService;
	}

	@PostMapping(value = "/getTestPriceVersion")
	public ResponseEntity<Object> getTestPriceVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getTestPriceVersion");
		final ObjectMapper objectMapper = new ObjectMapper();
		Integer npriceVersionCode = null;
		if (inputMap.get("npriceversioncode") != null) {
			npriceVersionCode = (Integer) inputMap.get("npriceversioncode");
		}
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceVersionService.getTestPriceVersion(npriceVersionCode, userInfo);
	}

	@PostMapping(value = "/getActiveTestPriceVersionById")
	public ResponseEntity<Object> getActiveTestPriceVersionById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final int npriceVersionCode = (Integer) inputMap.get("npriceversioncode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceVersionService.getActiveTestPriceVersionById(npriceVersionCode, userInfo);
	}

	@PostMapping(value = "/createTestPriceVersion")
	public ResponseEntity<Object> createTestPriceVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final TestPriceVersion testPriceVersion = objectMapper.convertValue(inputMap.get("testpriceversion"),
				TestPriceVersion.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceVersionService.createTestPriceVersion(testPriceVersion, userInfo);
	}

	@PostMapping(value = "/updateTestPriceVersion")
	public ResponseEntity<Object> updateTestPriceVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final TestPriceVersion testPriceVersion = objectMapper.convertValue(inputMap.get("testpriceversion"),
				TestPriceVersion.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceVersionService.updateTestPriceVersion(testPriceVersion, userInfo);
	}

	@PostMapping(value = "/deleteTestPriceVersion")
	public ResponseEntity<Object> deleteTestPriceVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final TestPriceVersion testPriceVersion = objectMapper.convertValue(inputMap.get("testpriceversion"),
				TestPriceVersion.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceVersionService.deleteTestPriceVersion(testPriceVersion, userInfo);
	}

	@PostMapping(value = "/approveTestPriceVersion")
	public ResponseEntity<Object> approveTestPriceVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final TestPriceVersion testPriceVersion = objectMapper.convertValue(inputMap.get("testpriceversion"),
				TestPriceVersion.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceVersionService.approveTestPriceVersion(testPriceVersion, userInfo);
	}

	@PostMapping(value = "/copyTestPriceVersion")
	public ResponseEntity<Object> copyTestPriceVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final TestPriceVersion testPriceVersion = objectMapper.convertValue(inputMap.get("testpriceversion"),
				TestPriceVersion.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceVersionService.copyTestPriceVersion(testPriceVersion, userInfo);
	}

	@PostMapping(value = "/getTestPrice")
	public ResponseEntity<Object> getTestPrice(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final Integer npriceVersionCode = (Integer) inputMap.get("npriceversioncode");
		final Integer ntestPriceCode = (Integer) inputMap.get("ntestpricedetailcode");

		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceService.getTestPrice(npriceVersionCode, ntestPriceCode, userInfo);
	}

	@PostMapping(value = "/createTestPrice")
	public ResponseEntity<Object> createTestPrice(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final List<TestPriceDetail> testPriceList = objectMapper.convertValue(inputMap.get("testpricelist"),
				new TypeReference<List<TestPriceDetail>>() {
				});
		final Integer npriceVersionCode = (Integer) inputMap.get("npriceversioncode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceService.createTestPrice(testPriceList, npriceVersionCode, userInfo);
	}

	@PostMapping(value = "/getPriceUnmappedTest")
	public ResponseEntity<Object> getPriceUnmappedTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final Integer npriceVersionCode = (Integer) inputMap.get("npriceversioncode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceService.getPriceUnmappedTest(npriceVersionCode, userInfo);
	}

	@PostMapping(value = "/updateTestPrice")
	public ResponseEntity<Object> updateTestPrice(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final List<TestPriceDetail> testPriceList = objectMapper.convertValue(inputMap.get("testpricelist"),
				new TypeReference<List<TestPriceDetail>>() {
				});
		final Integer npriceVersionCode = (Integer) inputMap.get("npriceversioncode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceService.updateTestPrice(testPriceList, npriceVersionCode, userInfo);
	}

	@PostMapping(value = "/deleteTestPrice")
	public ResponseEntity<Object> deleteTestPrice(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final TestPriceDetail testPrice = objectMapper.convertValue(inputMap.get("testprice"), TestPriceDetail.class);
		final Integer npriceVersionCode = (Integer) inputMap.get("npriceversioncode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testPriceService.deleteTestPrice(testPrice, npriceVersionCode, userInfo);
	}
}