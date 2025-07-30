package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.configuration.model.ProjectAndSiteHierarchyMapping;
import com.agaramtech.qualis.configuration.service.projectandsitehierarchymapping.ProjectAndSiteHierarchyMappingService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class is used to perform CRUD Operation on "projectsitehierarchymapping" table by
 * implementing methods from its interface.
 * 
 * 
 * @author Mullai Balaji.V 
 * BGSI-7
 * 3/07/2025 
 * @version 
 */

@RestController
@RequestMapping("/projectandsitehierarchymapping")
public class ProjectAndSiteHierarchyMappingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectAndSiteHierarchyMappingController.class);

	private RequestContext requestContext;
    private final ProjectAndSiteHierarchyMappingService projectAndSiteHierarchyMappingService;
    
	public ProjectAndSiteHierarchyMappingController(RequestContext requestContext,
			ProjectAndSiteHierarchyMappingService projectAndSiteHierarchyMappingService) {
		
		this.requestContext = requestContext;
		this.projectAndSiteHierarchyMappingService = projectAndSiteHierarchyMappingService;
	}
    

	/**
	 * This Method is used to get the over all projectsitehierarchymapping with respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of projectsitehierarchymapping with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	
	@PostMapping(value = "/getProjectAndSiteHierarchyMapping")
	public ResponseEntity<Object> getProjectAndSiteHierarchyMapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getProjectAndSiteHierarchyMapping called");
		requestContext.setUserInfo(userInfo);

		return projectAndSiteHierarchyMappingService.getProjectAndSiteHierarchyMapping(userInfo);

	}
	/**
	 * This method will is used to make a new entry to projectsitehierarchymapping table.
	 * 
	  * @param inputMap [Map] object with keys of projectsitehierarchymapping entity and UserInfo object.
	 *                 Input:{ "projectsitehierarchymapping": {"nprojectsitehierarchymapcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 260,"nmastersitecode": -1, "nmodulecode": 67,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already Exists' if the projectsitehierarchymapping
	 *         already exists/ list of diseases along with the newly added projectsitehierarchymapping.
	 * @throws Exception exception
	 */
	
	
	@PostMapping(value = "/createProjectAndSiteHierarchyMapping")
	public ResponseEntity<Object> createProjectAndSiteHierarchyMapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping = objMapper.convertValue(inputMap.get("projectandsitehierarchymapping"), new TypeReference<ProjectAndSiteHierarchyMapping>() {
		});

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return projectAndSiteHierarchyMappingService.createProjectAndSiteHierarchyMapping(objProjectAndSiteHierarchyMapping, userInfo);
	}
	
	/**
	 * This method is used to delete an entry in projectsitehierarchymapping table
	 * 
	 * @param inputMap [Map] object with keys of projectsitehierarchymapping entity and UserInfo object.
	 *                 Input:{ "projectsitehierarchymapping": {"nprojectsitehierarchymapcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 260,"nmastersitecode": -1, "nmodulecode": 67,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         projectsitehierarchymapping record is not available/ string message as 'Record is used
	 *         in....' when the disease is associated in transaction / list of all
	 *         diseases excluding the deleted record
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/deleteProjectAndSiteHierarchyMapping")
	public ResponseEntity<Object> deleteProjectAndSiteHierarchyMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping = objMapper.convertValue(inputMap.get("projectandsitehierarchymapping"), new TypeReference<ProjectAndSiteHierarchyMapping>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return projectAndSiteHierarchyMappingService.deleteProjectAndSiteHierarchyMapping(objProjectAndSiteHierarchyMapping, userInfo);
	}
	

	/**
	 * This Method is used to get the over all sitehierarchconfig with respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] 
	 *                
	 * @return a response entity which holds the list of projectsitehierarchymapping with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSitemap")
	public ResponseEntity<Object> getSitemap(@RequestBody Map<String, Object> inputMap) throws Exception 

	{
		final ObjectMapper objMapper = new ObjectMapper();
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		
		requestContext.setUserInfo(userInfo);
		return projectAndSiteHierarchyMappingService.getSitemap(userInfo);

	}

	/**
	 * This Method is used to get the over all bioproject with respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] 
	 * @return a response entity which holds the list of projectsitehierarchymapping with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getBioproject")
	public ResponseEntity<Object> getBioproject(@RequestBody Map<String, Object> inputMap) throws Exception 
	{
final ObjectMapper objMapper = new ObjectMapper();
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		
		requestContext.setUserInfo(userInfo);
		return projectAndSiteHierarchyMappingService.getBioProject(userInfo);

	}
}
