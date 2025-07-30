package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.basemaster.model.ContainerStructure;
import com.agaramtech.qualis.basemaster.service.containerstructure.ContainerStructureService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 

/**
 * This class is used to perform CRUD Operation on "containerstructure" table by
 * implementing methods from its interface.
 */

@RestController
@RequestMapping("/containerstructure")
public class ContainerStructureController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContainerStructureController.class); 
	
	private RequestContext requestContext;
	private final ContainerStructureService containerstructureService;
	

	public ContainerStructureController(RequestContext requestContext,
			ContainerStructureService containerstructureService) {
		super();
		this.requestContext = requestContext;
		this.containerstructureService = containerstructureService;
	}

	

	/**
	 * This Method is used to get the over all containerstructure with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of containerstructure with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getContainerStructure")
	public ResponseEntity<Object> getContainerStructure(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getContainerStructure() called");
		requestContext.setUserInfo(userInfo);
		return containerstructureService.getContainerStructure(userInfo);
	}

	/**
	 * This method is used to add a new entry to containerstructure table.
	 * 
	 * @param inputMap [Map] holds the containerstructure object to be inserted
	 * @return inserted containerstructure object with HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createContainerStructure")
	public ResponseEntity<Object> createContainerStructure(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final ContainerStructure objContainerStructure = objMapper.convertValue(inputMap.get("containerstructure"),
				new TypeReference<ContainerStructure>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return containerstructureService.createContainerStructure(objContainerStructure, userInfo);
	}

	/**
	 * This method is used to update entry in containerstructure table.
	 * 
	 * @param inputMap [Map] holds the containerstructure object to be updated
	 * @return response entity object holding response status and data of updated
	 *         containerstructure object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateContainerStructure")
	public ResponseEntity<Object> updateContainerStructure(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ContainerStructure objContainerStructure = objMapper.convertValue(inputMap.get("containerstructure"),
				new TypeReference<ContainerStructure>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return containerstructureService.updateContainerStructure(objContainerStructure, userInfo);
	}

	/**
	 * This method id used to delete an entry in containerstructure table
	 * 
	 * @param inputMap [Map] holds the containerstructure object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         containerstructure object
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteContainerStructure")
	public ResponseEntity<Object> deleteContainerStructure(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ContainerStructure objContainerStructure = objMapper.convertValue(inputMap.get("containerstructure"),
				ContainerStructure.class);

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return containerstructureService.deleteContainerStructure(objContainerStructure, userInfo);
	}

	/**
	 * This method is used to get the single record in containerstructure table
	 * 
	 * @param inputMap [Map] holds the containerstructure code to get
	 * @return response entity object holding response status and data of single
	 *         containerstructure object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveContainerStructureById")
	public ResponseEntity<Object> getActiveContainerStructureById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int ncontainerstructurecode = 0;
		if (inputMap.get("ncontainerstructurecode") != null) {
			ncontainerstructurecode = (int) inputMap.get("ncontainerstructurecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return containerstructureService.getActiveContainerStructureById(ncontainerstructurecode, userInfo);
	}
}
