package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import com.agaramtech.qualis.basemaster.service.unit.UnitService;
import com.agaramtech.qualis.configuration.model.TransactionFilterTypeConfig;
import com.agaramtech.qualis.configuration.service.transactionfilterconfiguration.TransactionFilterConfigrationService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
@RestController
@RequestMapping("/registrationsubtypeconfigration")
public class TransactionFilterConfigrationController {
	
	private RequestContext requestContext;
	private final TransactionFilterConfigrationService objTransactionFilterConfigrationService;
	
	public TransactionFilterConfigrationController(RequestContext requestContext, TransactionFilterConfigrationService objTransactionFilterConfigrationService) {
		super();
		this.requestContext = requestContext;
		this.objTransactionFilterConfigrationService = objTransactionFilterConfigrationService;
	}
	
	
	@PostMapping(value = "/getRegistrationSubtypeConfigration")
	public ResponseEntity<Object> getRegistrationSubtypeConfigration(@RequestBody Map<String, Object> inputMap) throws Exception {
			ObjectMapper objMapper = new ObjectMapper();	
			Integer ninstCode = null;					
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			
			return objTransactionFilterConfigrationService.getRegistrationSubtypeConfigration(ninstCode,userInfo);
	}
	
	
	
	@PostMapping(value = "/getBySampleType")
	public ResponseEntity<Object> getBySampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
			ObjectMapper objMapper = new ObjectMapper();	
			Integer nsampletypecode = null;
			if (inputMap.get("nsampletypecode") != null) {
				nsampletypecode = (Integer) inputMap.get("nsampletypecode");	
			}
			Integer nregtypecode = null;
			if (inputMap.get("nregtypecode") != null) {
				nregtypecode = (Integer) inputMap.get("nregtypecode");	
			}
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return objTransactionFilterConfigrationService.getBySampleType(nsampletypecode,nregtypecode,userInfo);
	}
	
	
	@PostMapping(value = "/getDepartment")
	public ResponseEntity<Object> getDepartment(@RequestBody Map<String, Object> inputMap)  throws Exception {
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return objTransactionFilterConfigrationService.getDepartment(inputMap,userInfo); 
	}
	
	

	
	@PostMapping(value = "/getDepartmentBasedUser")
	public ResponseEntity<Object> getDepartmentBasedUser(@RequestBody Map<String, Object> inputMap)  throws Exception {
			ObjectMapper objMapper = new ObjectMapper();	
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			
			final int sectionCode = (Integer) inputMap.get("ndeptcode");
			final int nregsubtypecode = (Integer) inputMap.get("nregsubtypecode");
			requestContext.setUserInfo(userInfo);
			return objTransactionFilterConfigrationService.getDepartmentBasedUser(nregsubtypecode,sectionCode,userInfo);
	}
	
	@PostMapping(value = "/getUserRole")
	public ResponseEntity<Object> getUserRole(@RequestBody Map<String, Object> inputMap)  throws Exception {
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
//			int ninstrumentcode=(int) (inputMap.get("ninstrumentcode"));
			requestContext.setUserInfo(userInfo);
			//final int siteCode = (Integer) inputMap.get("nsitecode");
			return objTransactionFilterConfigrationService.getUserRole(inputMap,userInfo); 
	}
	
	
	
	@PostMapping(value = "/getUserRoleBasedUser")
	public ResponseEntity<Object> getUserRoleBasedUser(@RequestBody Map<String, Object> inputMap)  throws Exception {
			ObjectMapper objMapper = new ObjectMapper();	
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			final int nregsubtypecode = (Integer) inputMap.get("nregsubtypecode");
			final int nuserrolecode = (Integer) inputMap.get("nuserrolecode");
			requestContext.setUserInfo(userInfo);
			return objTransactionFilterConfigrationService.getUserRoleBasedUser(nregsubtypecode,nuserrolecode,userInfo);
	}
	
	
	@PostMapping(value = "/createDepartment")
	public ResponseEntity<Object> createDepartment(@RequestBody Map<String, Object> inputMap)  throws Exception {
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return objTransactionFilterConfigrationService.createDepartment(inputMap,userInfo); 
	}
	
	@PostMapping(value = "/getRegtypeBasedSampleType")
	public ResponseEntity<Object> getRegtypeBasedSampleType(@RequestBody Map<String, Object> inputMap)  throws Exception {
			ObjectMapper objMapper = new ObjectMapper();	
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			
			final int nsampletype = (Integer) inputMap.get("nsampletype");
			requestContext.setUserInfo(userInfo);
			return objTransactionFilterConfigrationService.getRegtypeBasedSampleType(nsampletype,userInfo);
	}
	
	
	 @PostMapping(value = "/deleteDepartmentAndUserRole")
		public ResponseEntity<Object> deleteSection(@RequestBody Map<String, Object> inputMap)  throws Exception {
				final ObjectMapper objmapper = new ObjectMapper();
				objmapper.registerModule(new JavaTimeModule());
				final TransactionFilterTypeConfig instSec = objmapper.convertValue(inputMap.get("tabdetails"), TransactionFilterTypeConfig.class);
				UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
				requestContext.setUserInfo(userInfo);
				return objTransactionFilterConfigrationService.deleteDepartment(instSec, userInfo);
		}
	 
	 @PostMapping(value = "/getTabdetails")
		public ResponseEntity<Object> getTabdetails(@RequestBody Map<String, Object> inputMap)  throws Exception {
				final ObjectMapper objmapper = new ObjectMapper();
				objmapper.registerModule(new JavaTimeModule());
				int nregsubtypecode=objmapper.convertValue(inputMap.get("nregsubtypecode"), Integer.class);
				//final TransactionFilterTypeConfig instSec = objmapper.convertValue(inputMap.get("tabdetails"), TransactionFilterTypeConfig.class);
				 TransactionFilterTypeConfig instSec = new TransactionFilterTypeConfig();
				 instSec.setNregsubtypecode(nregsubtypecode);
				 UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
				requestContext.setUserInfo(userInfo);
				return objTransactionFilterConfigrationService.getTabdetails(instSec, userInfo);
		}
	
		@PostMapping(value = "/getListofUsers")
		public ResponseEntity<Object> getListofUsers(@RequestBody Map<String, Object> inputMap)  throws Exception {
				ObjectMapper objMapper = new ObjectMapper();	
				UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
				final int nregsubtypecode = (Integer) inputMap.get("nregsubtypecode");
				requestContext.setUserInfo(userInfo);
				return objTransactionFilterConfigrationService.getListofUsers(nregsubtypecode,userInfo);
		}
}
