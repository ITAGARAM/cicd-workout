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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.Institution;
import com.agaramtech.qualis.submitter.model.InstitutionFile;
import com.agaramtech.qualis.submitter.model.InstitutionSite;
import com.agaramtech.qualis.submitter.service.institution.InstitutionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/institution")
public class InstitutionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CityController.class);

	private RequestContext requestContext;
	private final InstitutionService institutionService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext     RequestContext to hold the request
	 * @param institutionService InstitutionService
	 */
	public InstitutionController(RequestContext requestContext, InstitutionService institutionService) {
		super();
		this.requestContext = requestContext;
		this.institutionService = institutionService;
	}

	@PostMapping(value = "/getInstitution")
	public ResponseEntity<Object> getInstitution(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getInstitution");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitution(userInfo);

	}

	@PostMapping(value = "/getInstitutionCategory")
	public ResponseEntity<Object> getInstitutionCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionCategory(userInfo);
	}

	@PostMapping(value = "/getInstitutionByCategory")
	public ResponseEntity<Object> getInstitutionByCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ninstitutionCatCode = (int) inputMap.get("ninstitutioncatcode");
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionByCategory(ninstitutionCatCode, userInfo);
	}

	@PostMapping(value = "/createInstitution")
	public ResponseEntity<Object> createInstitution(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Institution objInstitution = objmapper.convertValue(inputMap.get("institution"), Institution.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.createInstitution(objInstitution, userInfo);
	}

	@PostMapping(value = "/updateInstitution")
	public ResponseEntity<Object> updateInstitution(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Institution institution = objmapper.convertValue(inputMap.get("institution"), Institution.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.updateInstitution(institution, userInfo);
	}

	@PostMapping(value = "/deleteInstitution")
	public ResponseEntity<Object> deleteInstitution(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Institution institution = objmapper.convertValue(inputMap.get("institution"), Institution.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.deleteInstitution(institution, userInfo);

	}

	@PostMapping(value = "/getActiveInstitutionById")
	public ResponseEntity<Object> getActiveInstitutionById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ninstitutioncode = (Integer) inputMap.get("ninstitutioncode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getActiveInstitutionById(ninstitutioncode, userInfo);
	}

	@PostMapping(value = "/getInstitutionSiteCombo")
	public ResponseEntity<Object> getInstitutionSiteCombo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ninstitutioncode = (int) inputMap.get("ninstitutioncode");
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionSiteCombo(ninstitutioncode, userInfo);
	}

	@PostMapping(value = "/createInstitutionSite")
	public ResponseEntity<Object> createClientSiteAddress(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InstitutionSite objInstitutionSite = objmapper.convertValue(inputMap.get("institutionsite"),
				InstitutionSite.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.createInstitutionSite(objInstitutionSite, userInfo);
	}

	@PostMapping(value = "/getActiveInstitutionSiteById")
	public ResponseEntity<Object> getActiveInstitutionSiteById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final int ninstitutioncode = (Integer) inputMap.get("ninstitutioncode");
		final int ninstitutionsitecode = (Integer) inputMap.get("ninstitutionsitecode");
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getActiveInstitutionSiteById(ninstitutioncode, ninstitutionsitecode, userInfo);

	}

	@PostMapping(value = "/updateInstitutionSite")
	public ResponseEntity<Object> updateInstitutionSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InstitutionSite objInstitutionSite = objmapper.convertValue(inputMap.get("institutionsite"),
				InstitutionSite.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.updateInstitutionSite(objInstitutionSite, userInfo);
	}

	@PostMapping(value = "/deleteInstitutionSite")
	public ResponseEntity<Object> deleteInstitutionSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InstitutionSite objInstitutionSite = objmapper.convertValue(inputMap.get("institutionsite"),
				InstitutionSite.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.deleteInstitutionSite(objInstitutionSite, userInfo);

	}

	@PostMapping(value = "/getInstitutionFile")
	public ResponseEntity<Map<String, Object>> getInstitutionFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int ninstitutionCode = (int) inputMap.get("ninstitutionCode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionFile(ninstitutionCode, userInfo);
	}

	@PostMapping(value = "/getInstitutionFileById")
	public ResponseEntity<Object> getInstitutionFileById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final InstitutionFile objInstitutionFile = objmapper.convertValue(inputMap.get("institutionfile"),
				InstitutionFile.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionFileById(objInstitutionFile, userInfo);

	}

	@PostMapping(value = "/createInstitutionFile")
	public ResponseEntity<? extends Object> createInstitutionFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.createInstitutionFile(request, userInfo);
	}

	@PostMapping(value = "/updateInstitutionFile")
	public ResponseEntity<? extends Object> updateInstitutionFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.updateInstitutionFile(request, userInfo);
	}

	@PostMapping(value = "/deleteInstitutionFile")
	public ResponseEntity<? extends Object> deleteInstitutionFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final InstitutionFile objInstitutionFile = objMapper.convertValue(inputMap.get("institutionfile"),
				InstitutionFile.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.deleteInstitutionFile(objInstitutionFile, userInfo);
	}

	@PostMapping(value = "/viewInstitutionFile")
	public ResponseEntity<Object> viewInstitutionFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final InstitutionFile objInstitutionFile = objMapper.convertValue(inputMap.get("institutionfile"),
				InstitutionFile.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.viewInstitutionFile(objInstitutionFile, userInfo);
	}

	@PostMapping(value = "/getSelectedInstitutionDetail")
	public ResponseEntity<Object> getSelectedInstitutionDetail(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ninstitutionCode = (Integer) inputMap.get("ninstitutioncode");
		requestContext.setUserInfo(userInfo);
		return institutionService.getSelectedInstitutionDetail(userInfo, ninstitutionCode);
	}

	@PostMapping(value = "/getDistrict")
	public ResponseEntity<Object> getDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nregioncode = (int) inputMap.get("nregioncode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getDistrict(nregioncode, userInfo);
	}

	@PostMapping(value = "/getCity")
	public ResponseEntity<Object> getCity(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndistrictcode = (int) inputMap.get("ndistrictcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getCity(ndistrictcode, userInfo);
	}

	@PostMapping(value = "/getInstitutionSitebyInstitution")
	public ResponseEntity<List<InstitutionSite>> getInstitutionSitebyInstitution(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ninstitutioncode = (int) inputMap.get("ninstitutioncode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionSitebyInstitution(ninstitutioncode, userInfo);
	}

	@PostMapping(value = "/getInstitutionSitebyAll")
	public ResponseEntity<Object> getInstitutionSitebyAll(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionSitebyAll(userInfo);
	}

	@PostMapping(value = "/getInstitutionValues")
	public ResponseEntity<Object> getInstitutionValues(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionValues(userInfo);
	}

	@PostMapping(value = "/getInstitutionDistrict")
	public ResponseEntity<Object> getInstitutionDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionDistrict(userInfo);
	}

	@PostMapping(value = "/getInstitutionCategoryByDistrict")
	public ResponseEntity<Object> getInstitutionCategoryByDistrict(@RequestBody Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int districtCode = (int) inputMap.get("ndistrictcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionCategoryByDistrict(districtCode, userInfo);
	}

	@PostMapping(value = "/getInstitutionByMappedCategory")
	public ResponseEntity<Object> getInstitutionByMappedCategory(@RequestBody Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int institutionCatCode = (int) inputMap.get("ninstitutioncatcode");
		final int ndistrictCode = (int) inputMap.get("ndistrictcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionByMappedCategory(institutionCatCode, userInfo, ndistrictCode);
	}

	@PostMapping(value = "/getInstitutionSiteByMappedInstitution")
	public ResponseEntity<Object> getInstitutionSiteByMappedInstitution(@RequestBody Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int institutionCode = (int) inputMap.get("ninstitutioncode");
		final int ndistrictCode = (int) inputMap.get("ndistrictcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return institutionService.getInstitutionSiteByMappedInstitution(institutionCode, userInfo, ndistrictCode);
	}
}
