package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.model.BioParentSampleReceiving;
import com.agaramtech.qualis.biobank.service.bioparentsamplereceiving.BioParentSampleReceivingService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the bioparentsamplereceiving Service methods.
 */

@RestController
@RequestMapping("/bioparentsamplereceiving")
public class BioParentSampleReceivingController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BioParentSampleReceivingController.class);

	private RequestContext requestContext;
	private final BioParentSampleReceivingService bioParentSampleReceivingService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param bioParentSampleReceivingService BioParentSampleReceivingService
	 */
	
	public BioParentSampleReceivingController(RequestContext requestContext, BioParentSampleReceivingService bioParentSampleReceivingService) {
		this.requestContext = requestContext;
		this.bioParentSampleReceivingService = bioParentSampleReceivingService;
	 }	
	
	@PostMapping(value = "/getBioParentSampleReceiving")
	public ResponseEntity<Object> getBioParentSampleReceiving(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioParentSampleReceivingService.getBioParentSampleReceiving(userInfo);
	}
	
	@PostMapping(value = "/getActiveBioParentSampleReceiving")
	public ResponseEntity<Object> getActiveBioParentSampleReceiving(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioparentsamplecode = Integer.valueOf(inputMap.get("nbioparentsamplecode").toString());
		return bioParentSampleReceivingService.getActiveBioParentSampleReceiving(nbioparentsamplecode, userInfo);
	}	
	
	@PostMapping(value = "/getDiseaseforLoggedInSite")
	public ResponseEntity<Object> getDiseaseforLoggedInSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioParentSampleReceivingService.getDiseaseforLoggedInSite(userInfo);
	}
	
	@PostMapping(value = "/getBioProjectforLoggedInSite")
	public ResponseEntity<Object> getBioProjectforLoggedInSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int ndiseasecode = Integer.valueOf(inputMap.get("ndiseasecode").toString());
		return bioParentSampleReceivingService.getBioProjectforLoggedInSite(ndiseasecode, userInfo);
	}
	
	@PostMapping(value = "/getCollectionSiteBasedonProject")
	public ResponseEntity<Object> getCollectionSiteBasedonProject(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioprojectcode = Integer.valueOf(inputMap.get("nbioprojectcode").toString());
		return bioParentSampleReceivingService.getCollectionSiteBasedonProject(nbioprojectcode, userInfo);
	}
	

	@PostMapping(value = "/getHospitalBasedonSite")
	public ResponseEntity<Object> getHospitalBasedonSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int ncollectionsitecode = Integer.valueOf(inputMap.get("ncollectionsitecode").toString());
		return bioParentSampleReceivingService.getHospitalBasedonSite(ncollectionsitecode, userInfo);
	}
	
	@PostMapping(value = "/getStorageStructureBasedonSite")
	public ResponseEntity<Object> getStorageStructureBasedonSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioParentSampleReceivingService.getStorageStructureBasedonSite(userInfo);
	}
	
	@PostMapping(value = "/validateSubjectID")
	public ResponseEntity<Object> validateSubjectID(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String ssubjectid = (String) inputMap.get("ssubjectid");
		return bioParentSampleReceivingService.validateSubjectID(ssubjectid, userInfo);
	}
	
	@PostMapping(value = "/createBioParentSampleReceiving")
	public ResponseEntity<Object> createBioParentSampleReceiving(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioParentSampleReceivingService.createBioParentSampleReceiving(inputMap, userInfo);
	}
	
	@PostMapping(value = "/updateBioParentSampleReceiving")
	public ResponseEntity<Object> updateBioParentSampleReceiving(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioParentSampleReceivingService.updateBioParentSampleReceiving(inputMap, userInfo);
	}
	
	@PostMapping(value = "/getActiveBioParentSampleCollection")
	public ResponseEntity<Object> getActiveBioParentSampleCollection(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioparentsamplecollectioncode = Integer.parseInt(inputMap.get("nbioparentsamplecollectioncode").toString());
		return bioParentSampleReceivingService.getActiveBioParentSampleCollection(nbioparentsamplecollectioncode, userInfo);
	}

	@PostMapping(value = "/createParentSampleCollection")
	public ResponseEntity<Object> createParentSampleCollection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioParentSampleReceivingService.createParentSampleCollection(inputMap, userInfo);
	}
	
	@PostMapping(value = "/updateParentSampleCollection")
	public ResponseEntity<Object> updateParentSampleCollection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioParentSampleReceivingService.updateParentSampleCollection(inputMap, userInfo);
	}
	
	@PostMapping(value = "/deleteParentSampleCollection")
	public ResponseEntity<Object> deleteParentSampleCollection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioparentsamplecollectioncode = Integer.parseInt(inputMap.get("nbioparentsamplecollectioncode").toString());
		final int nbioparentsamplecode = Integer.parseInt(inputMap.get("nbioparentsamplecode").toString());
		return bioParentSampleReceivingService.deleteParentSampleCollection(nbioparentsamplecollectioncode, nbioparentsamplecode, userInfo);
	}
	
}
