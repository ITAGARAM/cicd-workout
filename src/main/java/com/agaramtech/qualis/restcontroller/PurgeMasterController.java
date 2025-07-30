package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.archivalandpurging.purge.model.PurgeMaster;
import com.agaramtech.qualis.archivalandpurging.purge.service.PurgeMasterService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the LimsPurgMaster Service methods
 * 
 */
@RestController
@RequestMapping("/purge")
public class PurgeMasterController {

	private RequestContext requestContext;
	private final PurgeMasterService purgeMasterService;
	
	public PurgeMasterController(RequestContext requestContext, PurgeMasterService purgeMasterService) {
		super();
		this.requestContext = requestContext;
		this.purgeMasterService = purgeMasterService;
	}

	/**
	 * This Method is used to get the over all purgemaster with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of purgemaster with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getPurge")
	public ResponseEntity<Object> getPurgeMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

	
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return purgeMasterService.getPurge(userInfo);


	}

	/**
	 * This method is used to add a new entry to purgemaster table.
	 * 
	 * @param inputMap [Map] holds the purgemaster object to be inserted
	 * @return inserted purgemaster object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createPurge")
	public ResponseEntity<Object> createPurgeMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		PurgeMaster objPurgeMaster = objMapper.convertValue(inputMap.get("purge"), new TypeReference<PurgeMaster>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return purgeMasterService.createPurgeMaster(objPurgeMaster, userInfo);

	}

	/**
	 * This method is used to update entry in purgemaster table.
	 * 
	 * @param inputMap [Map] holds the purgemaster object to be updated
	 * @return response entity object holding response status and data of updated
	 *         purgemaster object
	 * @throws Exception
	 */
	@PostMapping(value = "/updatePurge")
	public ResponseEntity<Object> updatePurgeMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		PurgeMaster objPurgeMaster = objMapper.convertValue(inputMap.get("purge"), new TypeReference<PurgeMaster>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return purgeMasterService.updatePurgeMaster(objPurgeMaster, userInfo);


	}

	/**
	 * This method id used to delete an entry in PurgeMaster table
	 * 
	 * @param inputMap [Map] holds the purgemaster object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         purgemaster object
	 * @throws Exception
	 */
	@PostMapping(value = "/deletePurge")
	public ResponseEntity<Object> deletePurgeMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		PurgeMaster objPurgeMaster = objMapper.convertValue(inputMap.get("purge"), new TypeReference<PurgeMaster>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return purgeMasterService.deletePurgeMaster(objPurgeMaster, userInfo);

	}

	/**
	 * This method is used to get the single record in purgemaster table
	 * 
	 * @param inputMap [Map] holds the npurgemastercode to get
	 * @return response entity object holding response status and data of single
	 *         purgemaster object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActivePurgeById")
	public ResponseEntity<Object> getActivePurgeMasterById(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		int npurgemastercode = (int) inputMap.get("npurgemastercode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return purgeMasterService.getActivePurgeMasterById(npurgemastercode, userInfo);


	}
	
	@PostMapping(value = "/getSite")
	public ResponseEntity<Object> getSite(@RequestBody Map<String, Object> inputMap) throws Exception{
		
			final ObjectMapper mapper=new ObjectMapper();
			final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		
			requestContext.setUserInfo(userInfo);
			return purgeMasterService.getSite(userInfo);
		
	}
}

