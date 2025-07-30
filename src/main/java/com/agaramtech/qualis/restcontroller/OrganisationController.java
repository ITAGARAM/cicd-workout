package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.DepartmentLab;
import com.agaramtech.qualis.organization.model.LabSection;
import com.agaramtech.qualis.organization.model.SectionUsers;
import com.agaramtech.qualis.organization.model.SiteDepartment;
import com.agaramtech.qualis.organization.service.departmentlab.DepartmentLabService;
import com.agaramtech.qualis.organization.service.labsection.LabSectionService;
import com.agaramtech.qualis.organization.service.sectionusers.SectionUsersService;
import com.agaramtech.qualis.organization.service.sitedepartment.SiteDepartmentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/organisation")
@AllArgsConstructor
public class OrganisationController {
	
	private RequestContext requestContext;	
	private final SiteDepartmentService siteService;	
	private final DepartmentLabService deptLabService;	
	private final LabSectionService labSectionService;	
	private final SectionUsersService sectionUsersService;
	
@PostMapping(value = "/getOrganisation")
public ResponseEntity<Object> getSite(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper mapper = new ObjectMapper();
	final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
	requestContext.setUserInfo(userInfo);
	return siteService.getOrganisation(userInfo, inputMap);
}

	
@PostMapping(value = "/getSiteDepartment")
public ResponseEntity<Object> getSiteDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper mapper = new ObjectMapper();
	final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	requestContext.setUserInfo(userInfo);
	final int nsiteCode = (Integer) inputMap.get("nsitecode");
	final boolean graphView = (Boolean) inputMap.get("graphview");
	final String treePath = (String) inputMap.get("completetreepath");
	final Integer primaryKey = (Integer) inputMap.get("primarykey");
	return siteService.getSiteDepartment(nsiteCode, userInfo, graphView, treePath, primaryKey, false);
}

	
@PostMapping(value = "/getSiteDepartmentComboData")
public ResponseEntity<Object> getSiteDepartmentComboData(@RequestBody Map<String, Object> inputMap)	throws Exception {
	final ObjectMapper mapper = new ObjectMapper();
	final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	requestContext.setUserInfo(userInfo);
	final int nsiteCode = (Integer) inputMap.get("nsitecode");
	return siteService.getSiteDepartmentComboData(userInfo, nsiteCode);
}

	
@PostMapping(value = "/createSiteDepartment")
public ResponseEntity<Object> createSiteDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final List<SiteDepartment> siteDepartmentList = objMapper.convertValue(inputMap.get("sitedepartmentlist"),new TypeReference<List<SiteDepartment>>() {});
	final String treePath = (String) inputMap.get("completetreepath");
	requestContext.setUserInfo(userInfo);
	return siteService.createSiteDepartment(siteDepartmentList, userInfo, treePath);
}

	
@PostMapping(value = "/deleteSiteDepartment")
public ResponseEntity<Object> deleteSiteDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final int siteDeptCode = (Integer) inputMap.get("nsitedeptcode");
	final String treePath = (String) inputMap.get("completetreepath");
	requestContext.setUserInfo(userInfo);
	return siteService.deleteSiteDepartment(siteDeptCode, userInfo, treePath);
}

	
@PostMapping(value = "/getDepartmentLabComboData")
public ResponseEntity<Object> getDepartmentLabComboData(@RequestBody Map<String, Object> inputMap)throws Exception {
	final ObjectMapper mapper = new ObjectMapper();
	final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final int nsiteDeptCode = (Integer) inputMap.get("nsitedeptcode");
	requestContext.setUserInfo(userInfo);
	return deptLabService.getDepartmentLabComboData(userInfo, nsiteDeptCode);
}

	
@PostMapping(value = "/createDepartmentLab")
public ResponseEntity<Object> createDepartmentLab(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final List<DepartmentLab> departmentLabList = objMapper.convertValue(inputMap.get("departmentlablist"),new TypeReference<List<DepartmentLab>>() {});
	final int nsiteCode = (Integer) inputMap.get("nsitecode");
	requestContext.setUserInfo(userInfo);
	final String treePath = (String) inputMap.get("completetreepath");
	return deptLabService.createDepartmentLab(departmentLabList, userInfo, nsiteCode, treePath);
}

	
@PostMapping(value = "/deleteDepartmentLab")
public ResponseEntity<Object> deleteDepartmentLab(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final int deptLabCode = (Integer) inputMap.get("ndeptlabcode");
	final int nsiteCode = (Integer) inputMap.get("nsitecode");
	final String treePath = (String) inputMap.get("completetreepath");
	requestContext.setUserInfo(userInfo);
	return deptLabService.deleteDepartmentLab(deptLabCode, userInfo, nsiteCode, treePath);
}

	
@PostMapping(value = "/getLabSectionComboData")
public ResponseEntity<Object> getLabSectionComboData(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper mapper = new ObjectMapper();
	final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final int ndeptLabCode = (Integer) inputMap.get("ndeptlabcode");
	requestContext.setUserInfo(userInfo);
	return labSectionService.getLabSectionComboData(userInfo, ndeptLabCode);
}

@PostMapping(value = "/createLabSection")
public ResponseEntity<Object> createLabSection(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final List<LabSection> labSectionList = objMapper.convertValue(inputMap.get("labsectionlist"),new TypeReference<List<LabSection>>() {});
	final int nsiteCode = (Integer) inputMap.get("nsitecode");
	final String treePath = (String) inputMap.get("completetreepath");
	requestContext.setUserInfo(userInfo);
	return labSectionService.createLabSection(labSectionList, userInfo, nsiteCode, treePath);
}

	
@PostMapping(value = "/deleteLabSection")
public ResponseEntity<Object> deleteLabSection(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final int labSectionCode = (Integer) inputMap.get("nlabsectioncode");
	final int nsiteCode = (Integer) inputMap.get("nsitecode");
	final String treePath = (String) inputMap.get("completetreepath");
	requestContext.setUserInfo(userInfo);
	return labSectionService.deleteLabSection(labSectionCode, userInfo, nsiteCode, treePath);
}


@PostMapping(value = "/getSectionUsers")
public ResponseEntity<Object> getSectionUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper mapper = new ObjectMapper();
	final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final String nlabSectionCode = Integer.toString((Integer) inputMap.get("nlabsectioncode"));
	final boolean graphView = (Boolean) inputMap.get("graphview");
	requestContext.setUserInfo(userInfo);
	return siteService.getSectionUsers(nlabSectionCode, userInfo, graphView);
}

	
@PostMapping(value = "/getSectionUsersComboData")
public ResponseEntity<Object> getSectionUsersComboData(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper mapper = new ObjectMapper();
	final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final int nlabSectionCode = (Integer) inputMap.get("nlabsectioncode");
	final int nsiteCode = (Integer) inputMap.get("nsitecode");
	requestContext.setUserInfo(userInfo);
	return sectionUsersService.getSectionUsersComboData(userInfo, nlabSectionCode, nsiteCode);
}

@PostMapping(value = "/createSectionUsers")
public ResponseEntity<Object> createSectionUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final List<SectionUsers> sectionUsersList = objMapper.convertValue(inputMap.get("sectionuserslist"),new TypeReference<List<SectionUsers>>() {});
	final int nsiteCode = (Integer) inputMap.get("nsitecode");
	requestContext.setUserInfo(userInfo);
	return sectionUsersService.createSectionUsers(sectionUsersList, userInfo, nsiteCode);
}

	
@PostMapping(value = "/deleteSectionUsers")
public ResponseEntity<Object> deleteSectionUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final int sectionUserCode = (Integer) inputMap.get("deleteid");
	final int nsiteCode = (Integer) inputMap.get("nsitecode");
	requestContext.setUserInfo(userInfo);
	return sectionUsersService.deleteSectionUsers(sectionUserCode, userInfo, nsiteCode);
}

@PostMapping(value = "/getSectionUserRole")
public ResponseEntity<Object> getSectionUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final int nuserCode = (Integer) inputMap.get("nusercode");
	final int nsiteCode = (Integer) inputMap.get("nsitecode");
	requestContext.setUserInfo(userInfo);
	return siteService.getSectionUserRole(nsiteCode, nuserCode, userInfo);
}


@PostMapping(value = "/getDepartmentLab")
public ResponseEntity<Object> getDepartmentLab(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper mapper = new ObjectMapper();
	final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final int siteDeptCode = (Integer) inputMap.get("nsitedeptcode");
	final StringBuffer treePath = new StringBuffer();
	treePath.append((String) inputMap.get("selectedtreepath"));
	final boolean graphView = (Boolean) inputMap.get("graphview");
	requestContext.setUserInfo(userInfo);
	return siteService.getDepartmentLab(Integer.toString(siteDeptCode), userInfo, treePath, graphView, false);
}
	
@PostMapping(value = "/getLabSection")
public ResponseEntity<Object> getLabSection(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper mapper = new ObjectMapper();
	final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	final int deptLabCode = (Integer) inputMap.get("ndeptlabcode");
	final boolean graphView = (Boolean) inputMap.get("graphview");
	requestContext.setUserInfo(userInfo);
	return siteService.getLabSection(Integer.toString(deptLabCode), userInfo,graphView, false);
}

}
