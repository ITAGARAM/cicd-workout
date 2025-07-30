package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.audittrail.model.AuditActionFilter;
import com.agaramtech.qualis.basemaster.model.Gender;
import com.agaramtech.qualis.basemaster.model.Period;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.checklist.model.ChecklistComponent;
import com.agaramtech.qualis.configuration.model.ApprovalSubType;
import com.agaramtech.qualis.configuration.model.GenericLabel;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.service.languages.LanguagesService;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.testmanagement.model.InterfaceType;
import com.agaramtech.qualis.credential.model.QualisForms;
import com.agaramtech.qualis.credential.model.QualisMenu;
import com.agaramtech.qualis.credential.model.QualisModule;
import com.agaramtech.qualis.dashboard.model.ChartType;
import com.agaramtech.qualis.dashboard.model.DesignComponents;
import com.agaramtech.qualis.dashboard.model.QueryBuilderTables;
import com.agaramtech.qualis.dashboard.model.QueryBuilderViews;
import com.agaramtech.qualis.dashboard.model.QueryBuilderViewsColumns;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactComponents;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialType;
import com.agaramtech.qualis.multilingualmasters.model.MultilingualMasters;
import com.agaramtech.qualis.reports.model.COAReportType;
import com.agaramtech.qualis.reports.model.ReportType;
import com.agaramtech.qualis.scheduler.model.SchedulerType;
import com.agaramtech.qualis.testmanagement.model.DynamicFormulaFields;
import com.agaramtech.qualis.testmanagement.model.Functions;
import com.agaramtech.qualis.testmanagement.model.Grade;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Languages Service methods
 */

@RestController
@RequestMapping("/language")
public class LanguagesController {
	private static final Log LOGGER = LogFactory.getLog(LanguagesController.class);

	private  RequestContext requestContext;
	private final LanguagesService languagesService;

	public LanguagesController(RequestContext requestContext, LanguagesService languagesService) {
		super();
		this.requestContext = requestContext;
		this.languagesService = languagesService;
	}

	/**
	 * This Method is used to get the over all Menu jsonObject and
	 * MultilingualMasters jsonObject with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key slanguagetypecode which holds the value of
	 *                 respective LanguageTypeCode
	 * @return a response entity which holds the Active List of Menu jsonObject and
	 *         MultilingualMasters jsonObject with respect to slanguagetypecode in
	 *         userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getLanguage")
	public ResponseEntity<Object> getLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getLanguage called");
		return languagesService.getLanguage(userInfo);
	}

	/**
	 * This method is used to get the single record in qualismenu table
	 * 
	 * @param inputMap [Map] holds the nmenucode to get
	 * @return response entity object holding response status and data of single
	 *         Menu jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveMenuByID")
	public ResponseEntity<Object> getActiveMenuByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nmenucode = (int) inputMap.get("nmenucode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveMenuByID(nmenucode, userInfo);
	}

	/**
	 * This method is used to get the single record in qualismodule table
	 * 
	 * @param inputMap [Map] holds the nmodulecode to get
	 * @return response entity object holding response status and data of single
	 *         Module jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveModuleByID")
	public ResponseEntity<Object> getActiveModuleByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nmodulecode = (int) inputMap.get("nmodulecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveModuleByID(nmodulecode, userInfo);
	}

	/**
	 * This method is used to get the single record in qualisforms table
	 * 
	 * @param inputMap [Map] holds the nformcode to get
	 * @return response entity object holding response status and data of single
	 *         form jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveFormByID")
	public ResponseEntity<Object> getActiveFormByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nformcode = (int) inputMap.get("nformcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveFormByID(nformcode, userInfo);
	}

	/**
	 * This method is used to get the single record in transactionstatus table
	 * 
	 * @param inputMap [Map] holds the ntranscode to get
	 * @return response entity object holding response status and data of
	 *         transactionstatus jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveTransactionStatusByID")
	public ResponseEntity<Object> getActiveTransactionStatusByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ntranscode = (int) inputMap.get("ntranscode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveTransactionStatusByID(ntranscode, userInfo);
	}

	/**
	 * This method is used to get the single record in controlmaster table
	 * 
	 * @param inputMap [Map] holds the ncontrolcode to get
	 * @return response entity object holding response status and data of single
	 *         controlmaster jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveControlMasterByID")
	public ResponseEntity<Object> getActiveControlMasterByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ncontrolcode = (int) inputMap.get("ncontrolcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveControlMasterByID(ncontrolcode, userInfo);
	}

	/**
	 * This method is used to get the single record in approvalsubtype table
	 * 
	 * @param inputMap [Map] holds the napprovalsubtypecode to get
	 * @return response entity object holding response status and data of single
	 *         approvalsubtype jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveApprovalSubTypeByID")
	public ResponseEntity<Object> getActiveApprovalSubTypeByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveApprovalSubTypeByID(napprovalsubtypecode, userInfo);
	}

	/**
	 * This method is used to get the single record in sampletype table
	 * 
	 * @param inputMap [Map] holds the nsampletypecode to get
	 * @return response entity object holding response status and data of single
	 *         sampletype jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveSampleTypeByID")
	public ResponseEntity<Object> getActiveSampleTypeByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveSampleTypeByID(nsampletypecode, userInfo);
	}

	/**
	 * This method is used to get the single record in period table
	 * 
	 * @param inputMap [Map] holds the nperiodcode to get
	 * @return response entity object holding response status and data of single
	 *         period jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActivePeriodByID")
	public ResponseEntity<Object> getActivePeriodByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nperiodcode = (int) inputMap.get("nperiodcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActivePeriodByID(nperiodcode, userInfo);
	}

	/**
	 * This method is used to get the single record in gender table
	 * 
	 * @param inputMap [Map] holds the nperiodcode to get
	 * @return response entity object holding response status and data of single
	 *         period jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveGenderByID")
	public ResponseEntity<Object> getActiveGenderByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ngendercode = (int) inputMap.get("ngendercode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveGenderByID(ngendercode, userInfo);
	}

	/**
	 * This method is used to get the single record in grade table
	 * 
	 * @param inputMap [Map] holds the ngradecode to get
	 * @return response entity object holding response status and data of single
	 *         grade jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveGradeByID")
	public ResponseEntity<Object> getActiveGradeByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ngradecode = (int) inputMap.get("ngradecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveGradeByID(ngradecode, userInfo);
	}

	/**
	 * This method is used to get the single record in schedulertype table
	 * 
	 * @param inputMap [Map] holds the nschedulertypecode to get
	 * @return response entity object holding response status and data of single
	 *         schedulertype jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveSchedulerTypeByID")
	public ResponseEntity<Object> getActiveSchedulerTypeByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nschedulertypecode = (int) inputMap.get("nschedulertypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveSchedulerTypeByID(nschedulertypecode, userInfo);
	}

	/**
	 * This method is used to get the single record in multilingualmasters table
	 * 
	 * @param inputMap [Map] holds the nmultilingualmasterscode to get
	 * @return response entity object holding response status and data of single
	 *         MultilingualMasters jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveMultilingualMastersByID")
	public ResponseEntity<Object> getActiveMultilingualMastersByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nmultilingualmasterscode = (int) inputMap.get("nmultilingualmasterscode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveMultilingualMastersByID(nmultilingualmasterscode, userInfo);
	}

	/**
	 * This method is used to get the single record in querybuildertables table
	 * 
	 * @param inputMap [Map] holds the nquerybuildertablecode to get
	 * @return response entity object holding response status and data of single
	 *         querybuildertables jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveQueryBuilderTablesByID")
	public ResponseEntity<Object> getActiveQueryBuilderTablesByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nquerybuildertablecode = (int) inputMap.get("nquerybuildertablecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveQueryBuilderTablesByID(nquerybuildertablecode, userInfo);
	}

	/**
	 * This method is used to get the single record in querybuilderviews table
	 * 
	 * @param inputMap [Map] holds the nquerybuilderviewscode to get
	 * @return response entity object holding response status and data of single
	 *         querybuilderviews jsonObject
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveQueryBuilderViewsByID")
	public ResponseEntity<Object> getActiveQueryBuilderViewsByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nquerybuilderviewscode = (int) inputMap.get("nquerybuilderviewscode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveQueryBuilderViewsByID(nquerybuilderviewscode, userInfo);
	}

	@PostMapping(value = "/getActiveQueryBuilderViewsColumnsByID")
	public ResponseEntity<Object> getActiveQueryBuilderViewsColumnsByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int languagesparam = (int) inputMap.get("languagesParam");
		final String sviewname = (String) inputMap.get("sviewname");
		final String keysvalue = (String) inputMap.get("keysvalue");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveQueryBuilderViewsColumnsByID(languagesparam, sviewname, userInfo, keysvalue);
	}

	@PostMapping(value = "/getActiveMaterialTypeByID")
	public ResponseEntity<Object> getActiveMaterialTypeByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nmaterialtypecode = (int) inputMap.get("nmaterialtypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveMaterialTypeByID(nmaterialtypecode, userInfo);
	}

	@PostMapping(value = "/getActiveInterfaceTypeByID")
	public ResponseEntity<Object> getActiveInterfaceTypeByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ninterfacetypecode = (int) inputMap.get("ninterfacetypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveInterfaceTypeByID(ninterfacetypecode, userInfo);
	}

	@PostMapping(value = "/getActiveAuditActionFilterByID")
	public ResponseEntity<Object> getActiveAuditActionFilterByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nauditactionfiltercode = (int) inputMap.get("nauditactionfiltercode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveAuditActionFilterByID(nauditactionfiltercode, userInfo);
	}

	@PostMapping(value = "/getActiveAttachmentTypeByID")
	public ResponseEntity<Object> getActiveAttachmentTypeByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nattachmenttypecode = (int) inputMap.get("nattachmenttypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveAttachmentTypeByID(nattachmenttypecode, userInfo);
	}

	@PostMapping(value = "/getActiveFTPTypeByID")
	public ResponseEntity<Object> getActiveFTPTypeByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nftptypecode = (int) inputMap.get("nftptypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveFTPTypeByID(nftptypecode, userInfo);
	}

	@PostMapping(value = "/getActiveReportTypeByID")
	public ResponseEntity<Object> getActiveReportTypeByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nreporttypecode = (int) inputMap.get("nreporttypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveReportTypeByID(nreporttypecode, userInfo);
	}

	@PostMapping(value = "/getActiveCOAReportTypeByID")
	public ResponseEntity<Object> getActiveCOAReportTypeByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ncoareporttypecode = (int) inputMap.get("ncoareporttypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveCOAReportTypeByID(ncoareporttypecode, userInfo);
	}

	@PostMapping(value = "/getActiveReactComponentByID")
	public ResponseEntity<Object> getActiveReactComponentByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nreactcomponentcode = (int) inputMap.get("nreactcomponentcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveReactComponentByID(nreactcomponentcode, userInfo);
	}

	@PostMapping(value = "/getActiveFunctionsByID")
	public ResponseEntity<Object> getActiveFunctionsByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nfunctioncode = (int) inputMap.get("nfunctioncode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveFunctionsByID(nfunctioncode, userInfo);
	}

	@PostMapping(value = "/getActiveDynamicFormulaFieldByID")
	public ResponseEntity<Object> getActiveDynamicFormulaFieldByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndynamicformulafieldcode = (int) inputMap.get("ndynamicformulafieldcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveDynamicFormulaFieldByID(ndynamicformulafieldcode, userInfo);
	}

	@PostMapping(value = "/getActiveChartTypeByID")
	public ResponseEntity<Object> getActiveChartTypeByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ncharttypecode = (int) inputMap.get("ncharttypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveChartTypeByID(ncharttypecode, userInfo);
	}

	@PostMapping(value = "/getActiveDesignComponentByID")
	public ResponseEntity<Object> getActiveDesignComponentByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndesigncomponentcode = (int) inputMap.get("ndesigncomponentcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveDesignComponentByID(ndesigncomponentcode, userInfo);
	}

	@PostMapping(value = "/getActiveCheckListComponentByID")
	public ResponseEntity<Object> getActiveCheckListComponentByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nchecklistcomponentcode = (int) inputMap.get("nchecklistcomponentcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveCheckListComponentByID(nchecklistcomponentcode, userInfo);
	}

	@PostMapping(value = "/getActiveGenericLabelByID")
	public ResponseEntity<Object> getActiveGenericLabelByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ngenericlabelcode = (int) inputMap.get("ngenericlabelcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveGenericLabelByID(ngenericlabelcode, userInfo);
	}

	@PostMapping(value = "/getActiveQueryBuilderTableColumnsByID")
	public ResponseEntity<Object> getActiveQueryBuilderTableColumnsByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveQueryBuilderTableColumnsByID(userInfo, inputMap);
	}

	@PostMapping(value = "/getActiveDynamicAuditTableByID")
	public ResponseEntity<Object> getActiveDynamicAuditTableByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveDynamicAuditTableByID(userInfo, inputMap);
	}

	@PostMapping(value = "/getActiveMappedTemplateFieldPropsByID")
	public ResponseEntity<Object> getActiveMappedTemplateFieldPropsByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getActiveMappedTemplateFieldPropsByID(userInfo, inputMap);
	}

	/**
	 * This method is used to update entry in qualismenu table.
	 * 
	 * @param inputMap [Map] holds the Menu jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         menu jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateMenuLanguage")
	public ResponseEntity<Object> updateMenuLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final QualisMenu objQualisMenu = objmapper.convertValue(inputMap.get("language"), QualisMenu.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateMenuLanguage(objQualisMenu, userInfo);
	}

	/**
	 * This method is used to update entry in qualismodule table.
	 * 
	 * @param inputMap [Map] holds the Module jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         Module jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateModuleLanguage")
	public ResponseEntity<Object> updateModuleLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final QualisModule objQualisModule = objmapper.convertValue(inputMap.get("language"), QualisModule.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateModuleLanguage(objQualisModule, userInfo);
	}

	/**
	 * This method is used to update entry in qualisforms table.
	 * 
	 * @param inputMap [Map] holds the form jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         form jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateFormLanguage")
	public ResponseEntity<Object> updateFormLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final QualisForms objQualisForms = objmapper.convertValue(inputMap.get("language"), QualisForms.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateFormLanguage(objQualisForms, userInfo);
	}

	/**
	 * This method is used to update entry in transactionstatus table.
	 * 
	 * @param inputMap [Map] holds the transactionstatus jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         transactionstatus jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateTransactionStatusLanguage")
	public ResponseEntity<Object> updateTransactionStatusLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final TransactionStatus objTransactionStatus = objmapper.convertValue(inputMap.get("language"),
				TransactionStatus.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateTransactionStatusLanguage(objTransactionStatus, userInfo);
	}

	/**
	 * This method is used to update entry in controlmaster table.
	 * 
	 * @param inputMap [Map] holds the controlmaster jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         controlmaster jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateControlMasterLanguage")
	public ResponseEntity<Object> updateControlMasterLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ControlMaster objControlMaster = objmapper.convertValue(inputMap.get("language"), ControlMaster.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateControlMasterLanguage(objControlMaster, userInfo);
	}

	/**
	 * This method is used to update entry in approvalsubtype table.
	 * 
	 * @param inputMap [Map] holds the approvalsubtype jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         approvalsubtype jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateApprovalSubTypeLanguage")
	public ResponseEntity<Object> updateApprovalSubTypeLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ApprovalSubType objapprovalsubtype = objmapper.convertValue(inputMap.get("language"),
				ApprovalSubType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateApprovalSubTypeLanguage(objapprovalsubtype, userInfo);
	}

	/**
	 * This method is used to update entry in sampletype table.
	 * 
	 * @param inputMap [Map] holds the sampletype jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         sampletype jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateSampleTypeLanguage")
	public ResponseEntity<Object> updateSampleTypeLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SampleType objSampleType = objmapper.convertValue(inputMap.get("language"), SampleType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateSampleTypeLanguage(objSampleType, userInfo);
	}

	/**
	 * This method is used to update entry in period table.
	 * 
	 * @param inputMap [Map] holds the period jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         period jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updatePeriodLanguage")
	public ResponseEntity<Object> updatePeriodLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Period period = objmapper.convertValue(inputMap.get("language"), Period.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updatePeriodLanguage(period, userInfo);
	}

	/**
	 * This method is used to update entry in gender table.
	 * 
	 * @param inputMap [Map] holds the gender jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         gender jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateGenderLanguage")
	public ResponseEntity<Object> updateGenderLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Gender objGender = objmapper.convertValue(inputMap.get("language"), Gender.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateGenderLanguage(objGender, userInfo);
	}

	/**
	 * This method is used to update entry in grade table.
	 * 
	 * @param inputMap [Map] holds the grade jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         grade jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateGradeLanguage")
	public ResponseEntity<Object> updateGradeLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Grade objGrade = objmapper.convertValue(inputMap.get("language"), Grade.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateGradeLanguage(objGrade, userInfo);
	}

	/**
	 * This method is used to update entry in schedulertype table.
	 * 
	 * @param inputMap [Map] holds the schedulertype jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         schedulertype jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateSchedulerTypeLanguage")
	public ResponseEntity<Object> updateSchedulerTypeLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SchedulerType objSchedulerType = objmapper.convertValue(inputMap.get("language"), SchedulerType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateSchedulerTypeLanguage(objSchedulerType, userInfo);
	}

	/**
	 * This method is used to update entry in multilingualmasters table.
	 * 
	 * @param inputMap [Map] holds the multilingualmasters jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         multilingualmasters jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateMultilingualMastersLanguage")
	public ResponseEntity<Object> updateMultilingualMastersLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final MultilingualMasters objmultilingualmasters = objmapper.convertValue(inputMap.get("language"),
				MultilingualMasters.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateMultilingualMastersLanguage(objmultilingualmasters, userInfo);
	}

	/**
	 * This method is used to update entry in querybuildertables table.
	 * 
	 * @param inputMap [Map] holds the querybuildertables jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         querybuildertables jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateQueryBuilderTablesLanguage")
	public ResponseEntity<Object> updateQueryBuilderTablesLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final QueryBuilderTables objQueryBuilderTables = objmapper.convertValue(inputMap.get("language"),
				QueryBuilderTables.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateQueryBuilderTablesLanguage(objQueryBuilderTables, userInfo);
	}

	/**
	 * This method is used to update entry in querybuilderviews table.
	 * 
	 * @param inputMap [Map] holds the querybuilderviews jsonobject to be updated
	 * @return response entity object holding response status and data of updated
	 *         querybuilderviews jsonobject
	 * @throws Exception
	 */
	@PostMapping(value = "/updateQueryBuilderViewsLanguage")
	public ResponseEntity<Object> updateQueryBuilderViewsLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final QueryBuilderViews queryBuilderViews = objmapper.convertValue(inputMap.get("language"),
				QueryBuilderViews.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateQueryBuilderViewsLanguage(queryBuilderViews, userInfo);
	}

	@PostMapping(value = "/updateQueryBuilderViewsColumnsLanguage")
	public ResponseEntity<Object> updateQueryBuilderViewsColumnsLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nindexoff = (int) inputMap.get("findIndex");
		final String keyvalue = (String) inputMap.get("keyvalue");
		final QueryBuilderViewsColumns queryBuilderViewscolumns = objmapper.convertValue(inputMap.get("jsondata"),
				QueryBuilderViewsColumns.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateQueryBuilderViewsColumnsLanguage(queryBuilderViewscolumns, userInfo, nindexoff,
				keyvalue);
	}

	@PostMapping(value = "/updateMaterialTypeLanguage")
	public ResponseEntity<Object> updateMaterialTypeLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final MaterialType materialtype = objmapper.convertValue(inputMap.get("language"), MaterialType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateMaterialTypeLanguage(materialtype, userInfo);
	}

	@PostMapping(value = "/updateInterfaceTypeLanguage")
	public ResponseEntity<Object> updateInterfaceTypeLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InterfaceType objInterfaceType = objmapper.convertValue(inputMap.get("language"), InterfaceType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateInterfaceTypeLanguage(objInterfaceType, userInfo);
	}

	@PostMapping(value = "/updateAuditActionFilterLanguage")
	public ResponseEntity<Object> updateAuditActionFilterLagetactibguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final AuditActionFilter auditActionFilter = objmapper.convertValue(inputMap.get("language"),
				AuditActionFilter.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateAuditActionFilterLanguage(auditActionFilter, userInfo);
	}

	@PostMapping(value = "/updateAttachmentTypeLanguage")
	public ResponseEntity<Object> updateAttachmentTypeLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final MultilingualMasters attachmentType = objmapper.convertValue(inputMap.get("language"),
				MultilingualMasters.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateAttachmentTypeLanguage(attachmentType, userInfo);
	}

	@PostMapping(value = "/updateFTPTypeLanguage")
	public ResponseEntity<Object> updateFTPTypeLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final MultilingualMasters FTPType = objmapper.convertValue(inputMap.get("language"), MultilingualMasters.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateFTPTypeLanguage(FTPType, userInfo);
	}

	@PostMapping(value = "/updateReportTypeLanguage")
	public ResponseEntity<Object> updateReportTypeLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ReportType reportType = objmapper.convertValue(inputMap.get("language"), ReportType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateReportTypeLanguage(reportType, userInfo);
	}

	@PostMapping(value = "/updateCOAReportTypeLanguage")
	public ResponseEntity<Object> updateCOAReportTypeLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final COAReportType COAReportType = objmapper.convertValue(inputMap.get("language"), COAReportType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateCOAReportTypeLanguage(COAReportType, userInfo);
	}

	@PostMapping(value = "/updateReactComponentsLanguage")
	public ResponseEntity<Object> updateReactComponentsLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ReactComponents reactComponents = objmapper.convertValue(inputMap.get("language"), ReactComponents.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateReactComponentsLanguage(reactComponents, userInfo);
	}

	@PostMapping(value = "/updateFunctionsLanguage")
	public ResponseEntity<Object> updateFunctionsLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Functions functions = objmapper.convertValue(inputMap.get("language"), Functions.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateFunctionsLanguage(functions, userInfo);
	}

	@PostMapping(value = "/updateDynamicFormulaFieldLanguage")
	public ResponseEntity<Object> updateDynamicFormulaFieldLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final DynamicFormulaFields dynamicFormulaFields = objmapper.convertValue(inputMap.get("language"),
				DynamicFormulaFields.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateDynamicFormulaFieldLanguage(dynamicFormulaFields, userInfo);
	}

	@PostMapping(value = "/updateChartTypeLanguage")
	public ResponseEntity<Object> updateChartTypeLanguage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ChartType chartType = objmapper.convertValue(inputMap.get("language"), ChartType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateChartTypeLanguage(chartType, userInfo);
	}

	@PostMapping(value = "/updateDesignComponentLanguage")
	public ResponseEntity<Object> updateDesignComponentLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final DesignComponents designComponent = objmapper.convertValue(inputMap.get("language"),
				DesignComponents.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateDesignComponentLanguage(designComponent, userInfo);
	}

	@PostMapping(value = "/updateCheckListComponentLanguage")
	public ResponseEntity<Object> updateCheckListComponentLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ChecklistComponent checklistComponents = objmapper.convertValue(inputMap.get("language"),
				ChecklistComponent.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateCheckListComponentLanguage(checklistComponents, userInfo);
	}

	@PostMapping(value = "/updateGenericLabelLanguage")
	public ResponseEntity<Object> updateGenericLabelLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final GenericLabel genericLabels = objmapper.convertValue(inputMap.get("language"), GenericLabel.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateGenericLabelLanguage(genericLabels, userInfo);
	}

	@PostMapping(value = "/updateQueryBuilderTableColumnsLanguage")
	public ResponseEntity<Object> updateQueryBuilderTableColumnsLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateQueryBuilderTableColumnsLanguage(inputMap, userInfo);
	}

	@PostMapping(value = "/updateDynamicAuditTableLanguage")
	public ResponseEntity<Object> updateDynamicAuditTableLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateDynamicAuditTableLanguage(inputMap, userInfo);
	}

	@PostMapping(value = "/updateMappedTemplateFieldPropsLanguage")
	public ResponseEntity<Object> updateMappedTemplateFieldPropsLanguage(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.updateMappedTemplateFieldPropsLanguage(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the menu in multilingualmasters table with respect
	 * to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective menu slanguagetypecode
	 * @return a response entity which holds the menu slanguagetypecode with respect
	 *         to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getMenuComboService")
	public ResponseEntity<Object> getMenuComboService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nmenucode = (int) inputMap.get("nmenucode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getMenuComboService(userInfo, nmenucode);
	}

	/**
	 * This Method is used to get the module in multilingualmasters table with
	 * respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective module slanguagetypecode
	 * @return a response entity which holds the module slanguagetypecode with
	 *         respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getModuleComboService")
	public ResponseEntity<Object> getModuleComboService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nmodulecode = (int) inputMap.get("nmodulecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getModuleComboService(userInfo, nmodulecode);
	}

	/**
	 * This Method is used to get the Form in multilingualmasters table with respect
	 * to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective Form slanguagetypecode
	 * @return a response entity which holds the Form slanguagetypecode with respect
	 *         to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getFormComboService")
	public ResponseEntity<Object> getFormComboService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nformcode = (int) inputMap.get("nformcode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getFormComboService(userInfo, nformcode);
	}

	/**
	 * This Method is used to get the TransactionStatus in multilingualmasters table
	 * with respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective TransactionStatus slanguagetypecode
	 * @return a response entity which holds the TransactionStatus slanguagetypecode
	 *         with respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getTransactionStatusComboService")
	public ResponseEntity<Object> getTransactionStatusComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ntranscode = (int) inputMap.get("ntranscode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getTransactionStatusComboService(userInfo, ntranscode);
	}

	/**
	 * This Method is used to get the ControlMaster in multilingualmasters table
	 * with respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective ControlMaster slanguagetypecode
	 * @return a response entity which holds the ControlMaster slanguagetypecode
	 *         with respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getControlMasterComboService")
	public ResponseEntity<Object> getControlMasterComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ncontrolcode = (int) inputMap.get("ncontrolcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getControlMasterComboService(userInfo, ncontrolcode);
	}

	/**
	 * This Method is used to get the Approvalsubtype in multilingualmasters table
	 * with respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective Approvalsubtype slanguagetypecode
	 * @return a response entity which holds the Approvalsubtype slanguagetypecode
	 *         with respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getApprovalSubTypeComboService")
	public ResponseEntity<Object> getApprovalSubTypeComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int napprovalsubtypecode = (int) inputMap.get("napprovalsubtypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getApprovalSubTypeComboService(userInfo, napprovalsubtypecode);
	}

	/**
	 * This Method is used to get the SampleType in multilingualmasters table with
	 * respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective SampleType slanguagetypecode
	 * @return a response entity which holds the SampleType slanguagetypecode with
	 *         respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSampleTypeComboService")
	public ResponseEntity<Object> getSampleTypeComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getSampleTypeComboService(userInfo, nsampletypecode);
	}

	/**
	 * This Method is used to get the Period in multilingualmasters table with
	 * respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective Period slanguagetypecode
	 * @return a response entity which holds the Period slanguagetypecode with
	 *         respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getPeriodComboService")
	public ResponseEntity<Object> getPeriodComboService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nperiodcode = (int) inputMap.get("nperiodcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getPeriodComboService(userInfo, nperiodcode);
	}

	/**
	 * This Method is used to get the Gender in multilingualmasters table with
	 * respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective Gender slanguagetypecode
	 * @return a response entity which holds the Gender slanguagetypecode with
	 *         respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getGenderComboService")
	public ResponseEntity<Object> getGenderComboService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ngendercode = (int) inputMap.get("ngendercode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getGenderComboService(userInfo, ngendercode);
	}

	/**
	 * This Method is used to get the Grade in multilingualmasters table with
	 * respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective Grade slanguagetypecode
	 * @return a response entity which holds the Grade slanguagetypecode with
	 *         respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getGradeComboService")
	public ResponseEntity<Object> getGradeComboService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ngradecode = (int) inputMap.get("ngradecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getGradeComboService(userInfo, ngradecode);
	}

	/**
	 * This Method is used to get the SchedulerType in multilingualmasters table
	 * with respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective SchedulerType slanguagetypecode
	 * @return a response entity which holds the SchedulerType slanguagetypecode
	 *         with respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSchedulerTypeComboService")
	public ResponseEntity<Object> getSchedulerTypeComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nschedulertypecode = (int) inputMap.get("nschedulertypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getSchedulerTypeComboService(userInfo, nschedulertypecode);
	}

	/**
	 * This Method is used to get the DynamicRecordDetailType in multilingualmasters
	 * table with respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective DynamicRecordDetailType slanguagetypecode
	 * @return a response entity which holds the DynamicRecordDetailType
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getMultilingualMastersComboService")
	public ResponseEntity<Object> getMultilingualMastersComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nmultilingualmasterscode = (int) inputMap.get("nmultilingualmasterscode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getMultilingualMastersComboService(userInfo, nmultilingualmasterscode);
	}

	/**
	 * This Method is used to get the QueryBuilderTables in multilingualmasters
	 * table with respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective QueryBuilderTables slanguagetypecode
	 * @return a response entity which holds the QueryBuilderTables
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getQueryBuilderTablesComboService")
	public ResponseEntity<Object> getQueryBuilderTablesComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nquerybuildertablecode = (int) inputMap.get("nquerybuildertablecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getQueryBuilderTablesComboService(userInfo, nquerybuildertablecode);
	}

	/**
	 * This Method is used to get the QueryBuilderViews in multilingualmasters table
	 * with respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective QueryBuilderViews slanguagetypecode
	 * @return a response entity which holds the QueryBuilderViews slanguagetypecode
	 *         with respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getQueryBuilderViewsComboService")
	public ResponseEntity<Object> getQueryBuilderViewsComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nquerybuilderviewscode = (int) inputMap.get("nquerybuilderviewscode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getQueryBuilderViewsComboService(userInfo, nquerybuilderviewscode);
	}

	/**
	 * This Method is used to get the QueryBuilderViewsColumns in
	 * multilingualmasters table with respect to userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective QueryBuilderViewsColumns
	 *                 slanguagetypecode
	 * @return a response entity which holds the QueryBuilderViewsColumns
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getQueryBuilderViewsColumnsComboService")
	public ResponseEntity<Object> getQueryBuilderViewsColumnsComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final String sviewname = (String) inputMap.get("sviewname");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getQueryBuilderViewsColumnsComboService(userInfo, sviewname);
	}

	@PostMapping(value = "/getMaterialConfigComboService")
	public ResponseEntity<Object> getMaterialConfigComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nmaterialconfigcode = (int) inputMap.get("nmaterialconfigcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getMaterialConfigComboService(userInfo, nmaterialconfigcode);
	}

	@PostMapping(value = "/getMaterialTypeComboService")
	public ResponseEntity<Object> getMaterialTypeComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nmaterialtypecode = (int) inputMap.get("nmaterialtypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getMaterialTypeComboService(userInfo, nmaterialtypecode);
	}

	@PostMapping(value = "/getInterfaceTypeComboService")
	public ResponseEntity<Object> getInterfaceTypeComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ninterfacetypecode = (int) inputMap.get("ninterfacetypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getInterfaceTypeComboService(userInfo, ninterfacetypecode);
	}

	@PostMapping(value = "/getAuditActionFilterComboService")
	public ResponseEntity<Object> getAuditActionFilterComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nauditactionfiltercode = (int) inputMap.get("nauditactionfiltercode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getAuditActionFilterComboService(userInfo, nauditactionfiltercode);
	}

	@PostMapping(value = "/getAttachmenttypeComboService")
	public ResponseEntity<Object> getAttachmenttypeComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nattachmenttypecode = (int) inputMap.get("nattachmenttypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getAttachmenttypeComboService(userInfo, nattachmenttypecode);
	}

	@PostMapping(value = "/getFTPtypeComboService")
	public ResponseEntity<Object> getFTPtypeComboService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nftptypecode = (int) inputMap.get("nftptypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getFTPtypeComboService(userInfo, nftptypecode);
	}

	@PostMapping(value = "/getReportTypeComboService")
	public ResponseEntity<Object> getReportTypeComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nreporttypecode = (int) inputMap.get("nreporttypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getReportTypeComboService(userInfo, nreporttypecode);
	}

	@PostMapping(value = "/getCOAReportTypeComboService")
	public ResponseEntity<Object> getCOAReportTypeComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ncoareporttypecode = (int) inputMap.get("ncoareporttypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getCOAReportTypeComboService(userInfo, ncoareporttypecode);
	}

	@PostMapping(value = "/getReactComponentsComboService")
	public ResponseEntity<Object> getReactComponentsComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nreactcomponentcode = (int) inputMap.get("nreactcomponentcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getReactComponentsComboService(userInfo, nreactcomponentcode);
	}

	@PostMapping(value = "/getFunctionsComboService")
	public ResponseEntity<Object> getFunctionsComboService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nfunctioncode = (int) inputMap.get("nfunctioncode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getFunctionsComboService(userInfo, nfunctioncode);
	}

	@PostMapping(value = "/getDynamicFormulaFieldsComboService")
	public ResponseEntity<Object> getDynamicFormulaFieldsComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ndynamicformulafieldcode = (int) inputMap.get("ndynamicformulafieldcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getDynamicFormulaFieldsComboService(userInfo, ndynamicformulafieldcode);
	}

	@PostMapping(value = "/getChartTypeComboService")
	public ResponseEntity<Object> getChartTypeComboService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ncharttypecode = (int) inputMap.get("ncharttypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getChartTypeComboService(userInfo, ncharttypecode);
	}

	@PostMapping(value = "/getDesignComponentsComboService")
	public ResponseEntity<Object> getDesignComponentsComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ndesigncomponentcode = (int) inputMap.get("ndesigncomponentcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getDesignComponentsComboService(userInfo, ndesigncomponentcode);
	}

	@PostMapping(value = "/getCheckListComponentService")
	public ResponseEntity<Object> getCheckListComponentService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistcomponentcode = (int) inputMap.get("nchecklistcomponentcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getCheckListComponentService(userInfo, nchecklistcomponentcode);
	}

	@PostMapping(value = "/getGenericLabelService")
	public ResponseEntity<Object> getGenericLabelService(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ngenericLabelCode = (int) inputMap.get("ngenericlabelcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getGenericLabelService(userInfo, ngenericLabelCode);
	}

	@PostMapping(value = "/getQueryBuilderTableColumnsService")
	public ResponseEntity<Object> getQueryBuilderTableColumnsService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getQueryBuilderTableColumnsService(userInfo, inputMap);
	}

	@PostMapping(value = "/getDynamicAuditTableService")
	public ResponseEntity<Object> getDynamicAuditTableService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getDynamicAuditTableService(userInfo, inputMap);
	}

	@PostMapping(value = "/getMappedTemplateFieldPropsService")
	public ResponseEntity<Object> getMappedTemplateFieldPropsService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return languagesService.getMappedTemplateFieldPropsService(userInfo, inputMap);
	}

}
