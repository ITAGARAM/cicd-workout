package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.service.testgrouprulesengine.TestGroupRulesEngineService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/testgrouprulesengine")

public class TestGroupRulesEngineController {

	
	private RequestContext requestContext;
	private final TestGroupRulesEngineService testGroupRulesEngineService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param resultEntryService ResultEntryService
	 */
	public TestGroupRulesEngineController(RequestContext requestContext, TestGroupRulesEngineService testGroupRulesEngineService) {
		super();
		this.requestContext = requestContext;
		this.testGroupRulesEngineService = testGroupRulesEngineService;
	}

	@PostMapping(value = "/getTestGroupRulesEngine")
	public ResponseEntity<Object> getTestGroupRulesEngine(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
//		int nproductcatcode = 0;
//		if (inputMap.containsKey("nproductcatcode") == true) {
//			nproductcatcode = (Integer) inputMap.get("nproductcatcode");
//		}
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.getTestGroupRulesEngine(inputMap, userInfo);
	}

	@PostMapping("/getSelectedTestGroupRulesEngine")
	public ResponseEntity<Object> getSelectedTestGroupRulesEngine(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		//final int ntestgrouprulesenginecode = (Integer) inputMap.get("ntestgrouprulesenginecode"); 

		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.getSelectedTestGroupRulesEngine(inputMap , userInfo);
	}

	@PostMapping(value = "/getTestGroupRulesEngineAdd")
	public ResponseEntity<Object> getTestGroupRulesEngineAdd(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"),  new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.getTestGroupRulesEngineAdd(inputMap,userInfo);

	}

	@PostMapping(value = "/getEditTestGroupRulesEngine")
	public ResponseEntity<Object> getEditTestGroupRulesEngine(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.getEditTestGroupRulesEngine(inputMap, userInfo);
	}

	@PostMapping(value = "/createTestGroupRulesEngine")
	public ResponseEntity<Object> createTestGroupRulesEngine(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"),  new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.createTestGroupRulesEngine(inputMap, userInfo);

	}

	@PostMapping(value = "/updateTestGroupRulesEngine")
	public ResponseEntity<Object> updateTestGroupRulesEngine(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"),  new TypeReference<UserInfo>() {});

		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.updateTestGroupRulesEngine(inputMap, userInfo);

	}

	@PostMapping(value = "/approveTestGroupRulesEngine")
	public ResponseEntity<Object> approveTestGroupRulesEngine(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"),  new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.approveTestGroupRulesEngine(inputMap, userInfo);

	}

	@PostMapping(value = "/deleteTestGroupRulesEngine")
	public ResponseEntity<Object> deleteTestGroupRulesEngine(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"),  new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.deleteTestGroupRulesEngine(inputMap, userInfo);

	}
	
	@PostMapping(value = "/getParameterResultValue")
	public ResponseEntity<Object> getSpecificationTestGroupRulesEngine(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"),  new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo); 
		return testGroupRulesEngineService.getParameterResultValue( inputMap, userInfo);

	}
	
	@PostMapping(value = "/getParameterRulesEngine")
	public ResponseEntity<Object> getParameterRulesEngine(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"),  new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo); 
		return testGroupRulesEngineService.getParameterRulesEngine(inputMap, userInfo);

	}
	
	@PostMapping(value = "/updateExecutionOrder")
	public ResponseEntity<Object> updateExecutionOrder(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"),  new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo); 
		return testGroupRulesEngineService.updateExecutionOrder(inputMap, userInfo);
	}
	
	@PostMapping(value = "/getProfileRoot")
	public ResponseEntity<Object> getProfileRoot(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		int nproductcatcode = 0;
		int nproductcode=0;
		if (inputMap.containsKey("nproductcatcode") == true) {
			nproductcatcode = (Integer) inputMap.get("nproductcatcode");
		}
		if (inputMap.containsKey("nproductcode") == true) {
			nproductcode = (Integer) inputMap.get("nproductcode");
		}
		int ntestcode=0;
		if (inputMap.containsKey("ntestcode") == true) {
			ntestcode = (Integer) inputMap.get("ntestcode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.getProfileRoot(userInfo,nproductcatcode,nproductcode,ntestcode);
	}
	 
	@PostMapping(value = "/getSpecification")
	public ResponseEntity<Object> getSpecification(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		int ntemplatemanipulationcode = 0;
		int ntestcode=0;
		if (inputMap.containsKey("ntemplatemanipulationcode") == true) {
			ntemplatemanipulationcode = (Integer) inputMap.get("ntemplatemanipulationcode");
		}
		if (inputMap.containsKey("ntestcode") == true) {
			ntestcode = (Integer) inputMap.get("ntestcode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.getSpecification(userInfo,ntemplatemanipulationcode,ntestcode);
	}
	
	@PostMapping(value = "/getComponent")
	public ResponseEntity<Object> getComponent(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		int nallottedspeccode = 0;
		int ncomponentcode=0;
		int ntestcode=0;
		if (inputMap.containsKey("nallottedspeccode") == true) {
			nallottedspeccode = (Integer) inputMap.get("nallottedspeccode");
			ncomponentcode = (Integer) inputMap.get("ncomponentcode");
		}
		if (inputMap.containsKey("ntestcode") == true) {
			ntestcode = (Integer) inputMap.get("ntestcode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.getComponent(userInfo,nallottedspeccode,ncomponentcode, ntestcode);
	}
	
	@PostMapping(value = "/getTestBasedOnRules")
	public ResponseEntity<Object> getTestBasedOnRules(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.getTestBasedOnRules(inputMap,userInfo);
	}	
	
	@PostMapping(value = "/copyTestGroupRulesEngine")
	public ResponseEntity<Object> copyTestGroupRulesEngine(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.copyTestGroupRulesEngine(inputMap,userInfo);
	}	
	
	@PostMapping(value = "/getProductCategory")
	public ResponseEntity<Object> getProductCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		int ntestcode=0;
		if (inputMap.containsKey("ntestcode") == true) {
			ntestcode = (Integer) inputMap.get("ntestcode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		 int ncategorybasedflow=Enumeration.TransactionStatus.NO.gettransactionstatus();
		 if (inputMap.containsKey("ncategorybasedflow")) {
			 ncategorybasedflow = (Integer) inputMap.get("ncategorybasedflow");
			}
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.getProductCategory(userInfo, ntestcode,ncategorybasedflow);
	}
	
	
	@PostMapping(value = "/getProductByProductCat")
	public ResponseEntity<Object> getProductByProductCat(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		int nproductcatcode = 0;
		if (inputMap.containsKey("nproductcatcode") == true) {
			nproductcatcode = (Integer) inputMap.get("nproductcatcode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return testGroupRulesEngineService.getProductByProductCat(userInfo,nproductcatcode);
	}
	
}