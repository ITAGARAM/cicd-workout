package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCalibration;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentFile;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentMaintenance;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentSection;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentValidation;
import com.agaramtech.qualis.instrumentmanagement.service.instrument.InstrumentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the MaterialCategory Service methods.
 * 
 * @author ATE154
 * @version 9.0.0.1
 * @since 03- nov- 2020
 */

@RestController
@RequestMapping("/instrument")
public class InstrumentController {

	private final InstrumentService objInstrumentService;
	private RequestContext requestContext;

	public InstrumentController(RequestContext requestContext, InstrumentService objInstrumentService) {
		super();
		this.requestContext = requestContext;
		this.objInstrumentService = objInstrumentService;

	}

	/**
	 * This method is used to retrieve list of active instrument for the specified
	 * site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active instrument that are to be listed
	 *         for the specified site
	 */

	@SuppressWarnings("unused")
	@PostMapping(value = "/getInstrument")
	public ResponseEntity<Object> getInstrument(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer ninstCode = null;
		Integer ninstrumentcatcode = null;
		if (inputMap.get("ninstrumentcode") != null) {
			ninstCode = (Integer) inputMap.get("ninstrumentcode");
		}
		if (inputMap.get("ninstrumentcatcode") != null) {
			ninstrumentcatcode = (Integer) inputMap.get("ninstrumentcatcode");
		}

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		
		return objInstrumentService.getInstrument(ninstCode, userInfo);
	}

	@PostMapping(value = "/getInsByInstrumentCat")
	public ResponseEntity<Object> getInsByInstrumentCat(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer ninstrumentcatcode = null;
		if (inputMap.get("ninstrumentcatcode") != null) {
			ninstrumentcatcode = (Integer) inputMap.get("ninstrumentcatcode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getInsByInstrumentCat(ninstrumentcatcode, userInfo);
	}

	@PostMapping(value = "/validateOpenDate")
	public ResponseEntity<Object> validateOpenDate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer ninstrumentcode = null;
		if (inputMap.get("ninstrumentcode") != null) {
			ninstrumentcode = (Integer) inputMap.get("ninstrumentcode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.validateOpenDate(ninstrumentcode, userInfo);
	}

	/**
	 * This method is used to delete instrument for the specified Site.
	 * 
	 * @param mapObject [Map] object with keys of instrument entity.
	 * @return response entity of deleted instrument entity
	 */
	@PostMapping(value = "/deleteInstrument")
	public ResponseEntity<Object> deleteInstrument(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Instrument inst = objmapper.convertValue(inputMap.get("instrument"), Instrument.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.deleteInstrument(inst, userInfo);
	}

	/**
	 * This method is used to create instrument for the specified Site.
	 * 
	 * @param mapObject [Map] object with keys of instrument entity.
	 * @return response entity of active instrument entity
	 */

	@PostMapping(value = "/createInstrument")
	public ResponseEntity<Object> createInstrument(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final List<InstrumentSection> instSect = objmapper.convertValue(inputMap.get("instrumentsection"),
				new TypeReference<List<InstrumentSection>>() {
				});
		requestContext.setUserInfo(userInfo);

		return objInstrumentService.createInstrument(inputMap, userInfo, instSect);
	}

	/**
	 * This method is used to update instrument for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of instrument entity.
	 * @return response entity of updated instrument entity
	 */
	@PostMapping(value = "/updateInstrument")
	public ResponseEntity<Object> updateInstrument(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Instrument inst = objmapper.convertValue(inputMap.get("instrument"), Instrument.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final InstrumentSection instSect = objmapper.convertValue(inputMap.get("instrumentsection"),
				InstrumentSection.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.updateInstrument(inst, userInfo, instSect);
	}

	/**
	 * This method is used to get transaction status for instrument.
	 * 
	 * @param mapObject [Map] object with keys of instrument entity.
	 * @return response entity of active instrument entity
	 */
	
	@PostMapping(value = "/getInstrumentStatus")
	public ResponseEntity<Object> getInstrumentStatus(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getInstrumentStatus(userInfo);
	}

//		/**
//		 * This method is used to get period for instrument.
//		 * @param mapObject [Map] object with keys of instrument entity.
//		 * @return response entity of active instrument entity
//		 */
//		@RequestMapping(value = "/getPeriod", method = RequestMethod.POST)
//		public ResponseEntity<Object> getPeriod(@RequestBody Map<String, Object> inputMap)  throws Exception {
//				ObjectMapper objMapper = new ObjectMapper();
//				Integer ncontrolCode=null;
//				UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
//				requestContext.setUserInfo(userInfo);
//				if(inputMap.get("ncontrolcode")!=null) {
//					ncontrolCode=(Integer) inputMap.get("ncontrolcode");
//				}
//				//final int siteCode = (Integer) inputMap.get("nsitecode");
//				return objInstrumentService.getPeriod(ncontrolCode,userInfo);
//		}
//		

	/**
	 * This method is used to get active record for instrument.
	 * 
	 * @param mapObject [Map] object with keys of instrument entity.
	 * @return response entity of active instrument entity
	 */
	
	@PostMapping(value = "/getActiveInstrumentById")
	public ResponseEntity<Object> getActiveInstrumentById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ninstCode = (Integer) inputMap.get("ninstrumentcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getActiveInstrumentById(ninstCode, userInfo);
	}

	@PostMapping(value = "/getSiteBasedSection")
	public ResponseEntity<Object> getSiteBasedSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final int nsitecode = (int) inputMap.get("nsitecode");

		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getSiteBasedSection(nsitecode, userInfo);
	}

	@PostMapping(value = "/getSectionBasedUser")
	public ResponseEntity<Object> getSectionBasedUser(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int sectionCode = (Integer) inputMap.get("nsectioncode");
		final int nregionalsitecode = (Integer) inputMap.get("nregionalsitecode");
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getSectionBasedUser(sectionCode, nregionalsitecode, userInfo);
	}

	@PostMapping(value = "/getUsers")
	public ResponseEntity<Object> getUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getUsers(inputMap, userInfo);
	}

	@PostMapping(value = "/createSection")
	public ResponseEntity<Object> createSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentSection inst = objmapper.convertValue(inputMap.get("instrumentsection"),
				InstrumentSection.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.createSection(inst, userInfo);
	}

	@PostMapping(value = "/deleteSection")
	public ResponseEntity<Object> deleteSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentSection instSec = objmapper.convertValue(inputMap.get("instrumentsection"),
				InstrumentSection.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.deleteSection(instSec, userInfo);
	}

	@PostMapping(value = "/setDefaultSection")
	public ResponseEntity<Object> setDefaultSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final InstrumentSection objSection = objMapper.convertValue(inputMap.get("Section"), InstrumentSection.class);
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(objUserInfo);
		return objInstrumentService.setDefaultSection(objSection, objUserInfo);
	}

	@PostMapping(value = "/getManufacturer")
	public ResponseEntity<Object> getManufacturer(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer ncontrolCode = null;
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		if (inputMap.get("ncontrolcode") != null) {
			ncontrolCode = (Integer) inputMap.get("ncontrolcode");
		}
		// final int siteCode = (Integer) inputMap.get("nsitecode");
		return objInstrumentService.getManufacturer(ncontrolCode, userInfo);
	}

	@PostMapping(value = "/getSupplier")
	public ResponseEntity<Object> getSupplier(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer ncontrolCode = null;
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		if (inputMap.get("ncontrolcode") != null) {
			ncontrolCode = (Integer) inputMap.get("ncontrolcode");
		}
		// final int siteCode = (Integer) inputMap.get("nsitecode");
		return objInstrumentService.getSupplier(ncontrolCode, userInfo);
	}

	@PostMapping(value = "/addInstrumentDate")
	public ResponseEntity<Object> addInstrmentDate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer ncontrolCode = null;
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		if (inputMap.get("ncontrolcode") != null) {
			ncontrolCode = (Integer) inputMap.get("ncontrolcode");
		}
		return objInstrumentService.addInstrmentDate(ncontrolCode, userInfo);
	}

	@PostMapping(value = "/getInstrumentValidationStatus")
	public ResponseEntity<Object> getInstrumentValidationStatus(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getInstrumentValidationStatus(inputMap, userInfo);
	}

	@PostMapping(value = "/createInstrumentValidation")
	public ResponseEntity<Object> createInstrumentValidation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentValidation inst = objmapper.convertValue(inputMap.get("instrumentvalidation"),
				InstrumentValidation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.createInstrumentValidation(inst, userInfo);
	}

	@PostMapping(value = "/getActiveInstrumentValidationById")
	public ResponseEntity<Object> getActiveInstrumentValidationById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final int ninstrumentvalidationcode = (Integer) inputMap.get("ninstrumentvalidationcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getActiveInstrumentValidationById(ninstrumentvalidationcode, userInfo);
	}

	
	@PostMapping(value = "/updateInstrumentValidation")
	public ResponseEntity<Object> updateInstrumentValidation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentValidation inst = objmapper.convertValue(inputMap.get("instrumentvalidation"),
				InstrumentValidation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.updateInstrumentValidation(inst, userInfo);
	}

	
	@PostMapping(value = "/deleteInstrumentValidation")
	public ResponseEntity<Object> deleteInstrumentValidation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentValidation inst = objmapper.convertValue(inputMap.get("InstrumentValidation"),
				InstrumentValidation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.deleteInstrumentValidation(inst, userInfo);
	}

	@PostMapping(value = "/getothertabdetails")
	public ResponseEntity<Object> getOtherTabDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nFlag = (int) inputMap.get("nFlag");
		final int ninstrumentcode = (int) inputMap.get("ninstrumentcode");
		int tabprimarycode = 0;
		if (inputMap.containsKey("ninstrumentvalidationcode")) {
			tabprimarycode = (int) inputMap.get("ninstrumentvalidationcode");
		} else if (inputMap.containsKey("ninstrumentcalibrationcode")) {
			tabprimarycode = (int) inputMap.get("ninstrumentcalibrationcode");
		} else if (inputMap.containsKey("ninstrumentmaintenancecode")) {
			tabprimarycode = (int) inputMap.get("ninstrumentmaintenancecode");
		}
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getOtherTabDetails(nFlag, ninstrumentcode, tabprimarycode, userInfo);

	}

	@PostMapping(value = "/createInstrumentFile")
	public ResponseEntity<Object> createInstrumentFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.createInstrumentFile(request, userInfo);
	}

	@PostMapping(value = "/editInstrumentFile")
	public ResponseEntity<Object> editInstrumentFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final InstrumentFile objInstrumentFile = objMapper.convertValue(inputMap.get("Instrumentfile"),
				InstrumentFile.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.editInstrumentFile(objInstrumentFile, userInfo);

	}

	@PostMapping(value = "/updateInstrumentFile")
	public ResponseEntity<Object> updateInstrumentFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.updateInstrumentFile(request, userInfo);

	}

	@PostMapping(value = "/deleteInstrumentValidationFile")
	public ResponseEntity<Object> deleteInstrumentValidationFile(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final InstrumentFile objInstrumentFile = objMapper.convertValue(inputMap.get("InstrumentFile"),
				InstrumentFile.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.deleteInstrumentValidationFile(objInstrumentFile, userInfo);
	}

	@PostMapping(value = "/getInstrumentCalibrationStatus")
	public ResponseEntity<Object> getInstrumentCalibrationStatus(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getInstrumentCalibrationStatus(userInfo);
	}

	@PostMapping(value = "/getInstrumentLastCalibrationDate")
	public ResponseEntity<Object> getInstrumentLastCalibrationDate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getInstrumentLastCalibrationDate(userInfo, inputMap);
	}

	@PostMapping(value = "/createInstrumentCalibration")
	public ResponseEntity<Object> createInstrumentCalibration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentCalibration inst = objmapper.convertValue(inputMap.get("instrumentcalibration"),
				InstrumentCalibration.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.createInstrumentCalibration(inst, userInfo);
	}

	@PostMapping(value = "/getActiveInstrumentCalibrationById")
	public ResponseEntity<Object> getActiveInstrumentCalibrationById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final int ninstrumentcalibrationcode = (Integer) inputMap.get("ninstrumentcalibrationcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getActiveInstrumentCalibrationById(ninstrumentcalibrationcode, userInfo);
	}

	@PostMapping(value = "/updateInstrumentCalibration")
	public ResponseEntity<Object> updateInstrumentCalibration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentCalibration inst = objmapper.convertValue(inputMap.get("instrumentcalibration"),
				InstrumentCalibration.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.updateInstrumentCalibration(inst, userInfo);
	}

	@PostMapping(value = "/deleteInstrumentCalibration")
	public ResponseEntity<Object> deleteInstrumentCalibration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentCalibration inst = objmapper.convertValue(inputMap.get("InstrumentCalibration"),
				InstrumentCalibration.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.deleteInstrumentCalibration(inputMap, inst, userInfo);
	}

	@PostMapping(value = "/getInstrumentCalibrationOpenDateStatus")
	public ResponseEntity<Object> getInstrumentCalibrationOpenDateStatus(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getInstrumentCalibrationOpenDateStatus(userInfo, inputMap);
	}

	@PostMapping(value = "/createInstrumentCalibrationFile")
	public ResponseEntity<Object> createInstrumentCalibrationFile(MultipartHttpServletRequest request)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.createInstrumentCalibrationFile(request, userInfo);

	}

	@PostMapping(value = "/updateInstrumentCalibrationFile")
	public ResponseEntity<Object> updateInstrumentCalibrationFile(MultipartHttpServletRequest request)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.updateInstrumentCalibrationFile(request, userInfo);
	}

	@PostMapping(value = "/deleteInstrumentCalibrationFile")
	public ResponseEntity<Object> deleteInstrumentCalibartionFile(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final InstrumentFile objInstrumentFile = objMapper.convertValue(inputMap.get("InstrumentFile"),
				InstrumentFile.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.deleteInstrumentCalibrationFile(objInstrumentFile, userInfo);
	}

	@PostMapping(value = "/getInstrumentCalibrationValidation")
	public ResponseEntity<Object> getInstrumentCalibrationValidation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int nFlag = (Integer) inputMap.get("nFlag");
		int ninstrumentcalibrationcode = 0;
		int ninstrumentcode = (Integer) inputMap.get("ninstrumentcode");

		if (inputMap.containsKey("ninstrumentcalibrationcode")) {
			ninstrumentcalibrationcode = (Integer) inputMap.get("ninstrumentcalibrationcode");
		}

		return objInstrumentService.getInstrumentCalibrationValidation(userInfo, nFlag, ninstrumentcode,
				ninstrumentcalibrationcode);
	}

	@PostMapping(value = "/getInstrumentMaintenanceStatus")
	public ResponseEntity<Object> getInstrumentMaintenanceStatus(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getInstrumentMaintenanceStatus(userInfo);
	}

	@PostMapping(value = "/getInstrumentLastMaintenanceDate")
	public ResponseEntity<Object> getInstrumentLastMaintenanceDate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getInstrumentLastMaintenanceDate(userInfo, inputMap);
	}
	
	@PostMapping(value = "/createInstrumentMaintenance")
	public ResponseEntity<Object> createInstrumentMaintenance(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentMaintenance inst = objmapper.convertValue(inputMap.get("instrumentmaintenance"),
				InstrumentMaintenance.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.createInstrumentMaintenance(inst, userInfo);
	}

	@PostMapping(value = "/getActiveInstrumentMaintenanceById")
	public ResponseEntity<Object> getActiveInstrumentMaintenanceById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final int ninstrumentmaintenancecode = (Integer) inputMap.get("ninstrumentmaintenancecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getActiveInstrumentMaintenanceById(ninstrumentmaintenancecode, userInfo);
	}

	@PostMapping(value = "/updateInstrumentMaintenance")
	public ResponseEntity<Object> updateInstrumentMaintenance(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentMaintenance inst = objmapper.convertValue(inputMap.get("instrumentmaintenance"),
				InstrumentMaintenance.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.updateInstrumentMaintenance(inst, userInfo);
	}

	@PostMapping(value = "/deleteInstrumentMaintenance")
	public ResponseEntity<Object> deleteInstrumentMaintenance(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentMaintenance inst = objmapper.convertValue(inputMap.get("InstrumentMaintenance"),
				InstrumentMaintenance.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.deleteInstrumentMaintenance(inst, userInfo);
	}

	@PostMapping(value = "/getInstrumentMaintenanceOpenCloseDateStatus")
	public ResponseEntity<Object> getInstrumentMaintenanceOpenCloseDateStatus(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getInstrumentMaintenanceOpenCloseDateStatus(userInfo, inputMap);
	}

	@PostMapping(value = "/createInstrumentMaintenanceFile")
	public ResponseEntity<Object> createInstrumentMaintenanceFile(MultipartHttpServletRequest request)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.createInstrumentMaintenanceFile(request, userInfo);
	}

	@PostMapping(value = "/updateInstrumentMaintenanceFile")
	public ResponseEntity<Object> updateInstrumentMaintenanceFile(MultipartHttpServletRequest request)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return objInstrumentService.updateInstrumentMaintenanceFile(request, userInfo);
	}

	@PostMapping(value = "/deleteInstrumentMaintenanceFile")
	public ResponseEntity<Object> deleteInstrumentMaintenanceFile(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final InstrumentFile objInstrumentFile = objMapper.convertValue(inputMap.get("InstrumentFile"),
				InstrumentFile.class);

		requestContext.setUserInfo(userInfo);
		return objInstrumentService.deleteInstrumentMaintenanceFile(objInstrumentFile, userInfo);
	}

	@PostMapping(value = "/viewAttachedInstrumentFile")
	public ResponseEntity<Object> viewAttachedInstrumentFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final InstrumentFile objInstrumentFile = objMapper.convertValue(inputMap.get("instrumentfile"),
				InstrumentFile.class);
		Map<String, Object> outputMap = objInstrumentService.viewAttachedInstrumentFile(objInstrumentFile, userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@PostMapping(value = "/getInstrumentMaintenanceValidation")
	public ResponseEntity<Object> getInstrumentMaintenanceValidation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int nFlag = (Integer) inputMap.get("nFlag");
		int ninstrumentmaintenancecode = 0;
		int ninstrumentcode = (Integer) inputMap.get("ninstrumentcode");
		if (inputMap.containsKey("ninstrumentmaintenancecode")) {
			ninstrumentmaintenancecode = (Integer) inputMap.get("ninstrumentmaintenancecode");
		}

		return objInstrumentService.getInstrumentMaintenanceValidation(userInfo, nFlag, ninstrumentcode,
				ninstrumentmaintenancecode);
	}

	@PostMapping(value = "/getCalibrationRequired")
	public ResponseEntity<Object> getCalibrationRequired(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer ninstrumentCatCode = null;
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		if (inputMap.get("ninstrumentcatcode") != null) {
			ninstrumentCatCode = (Integer) inputMap.get("ninstrumentcatcode");
		}
		return objInstrumentService.getCalibrationRequired(ninstrumentCatCode, userInfo);
	}

	@PostMapping(value = "/getInstrumentName")
	public ResponseEntity<Object> getInstrumentName(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getInstrumentName(userInfo);
	}

	@PostMapping(value = "/getInstrumentLocation")
	public ResponseEntity<Object> getInstrumentLocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		// final int siteCode = (Integer) inputMap.get("nsitecode");
		return objInstrumentService.getInstrumentLocation(userInfo);
	}

	// Added by sonia on 30th Sept 2024 for Jira idL:ALPD-4940
	@PostMapping(value = "/getInstrumentBySchedulerDetail")
	public ResponseEntity<Object> getInstrumentBySchedulerDetail(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer instrumentCode = null;

		if (inputMap.get("ninstrumentcode") != null) {
			instrumentCode = (Integer) inputMap.get("ninstrumentcode");
		}

		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(objUserInfo);
		return objInstrumentService.getInstrumentBySchedulerDetail(instrumentCode, objUserInfo);
	}

	// Added by sonia on 30th Sept 2024 for Jira idL:ALPD-4940
	@PostMapping(value = "/updateAutoCalibrationInstrument")
	public ResponseEntity<Object> updateAutoCalibrationInstrument(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer instrumentCode = null;
		Integer autoCalibration = null;
		if (inputMap.get("ninstrumentcode") != null) {
			instrumentCode = (Integer) inputMap.get("ninstrumentcode");
		}
		if (inputMap.get("nautocalibration") != null) {
			autoCalibration = (Integer) inputMap.get("nautocalibration");
		}

		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(objUserInfo);
		return objInstrumentService.updateAutoCalibrationInstrument(instrumentCode, autoCalibration, objUserInfo);
	}

	// ALPD-5330 - Gowtham R - Section not loaded initially - 08-02-2025
	@PostMapping(value = "/getSection")
	public ResponseEntity<Object> getSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objInstrumentService.getSection(userInfo);
	}
}
