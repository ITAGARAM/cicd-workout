package com.agaramtech.qualis.configuration.service.languages;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.audittrail.model.AuditActionFilter;
import com.agaramtech.qualis.audittrail.model.DynamicAuditTable;
import com.agaramtech.qualis.basemaster.model.Gender;
import com.agaramtech.qualis.basemaster.model.Period;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.checklist.model.ChecklistComponent;
import com.agaramtech.qualis.configuration.model.ApprovalSubType;
import com.agaramtech.qualis.configuration.model.GenericLabel;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.QualisForms;
import com.agaramtech.qualis.credential.model.QualisMenu;
import com.agaramtech.qualis.credential.model.QualisModule;
import com.agaramtech.qualis.dashboard.model.ChartType;
import com.agaramtech.qualis.dashboard.model.DesignComponents;
import com.agaramtech.qualis.dashboard.model.QueryBuilderTableColumns;
import com.agaramtech.qualis.dashboard.model.QueryBuilderTables;
import com.agaramtech.qualis.dashboard.model.QueryBuilderViews;
import com.agaramtech.qualis.dashboard.model.QueryBuilderViewsColumns;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactComponents;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialType;
import com.agaramtech.qualis.multilingualmasters.model.MultilingualMasters;
import com.agaramtech.qualis.reports.model.COAReportType;
import com.agaramtech.qualis.reports.model.ReportType;
import com.agaramtech.qualis.scheduler.model.SchedulerType;
import com.agaramtech.qualis.testmanagement.model.DynamicFormulaFields;
import com.agaramtech.qualis.testmanagement.model.Functions;
import com.agaramtech.qualis.testmanagement.model.Grade;
import com.agaramtech.qualis.testmanagement.model.InterfaceType;

/**
 * This class holds methods to perform CRUD operation through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class LanguagesServiceImpl implements LanguagesService {

	private final LanguagesDAO languagesDAO;
	private final CommonFunction commonFunction;
	
	public LanguagesServiceImpl(LanguagesDAO languagesDAO, CommonFunction commonFunction) {
		super();
		this.languagesDAO = languagesDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active Menu jsonObject and
	 * MultilingualMasters jsonObject for the specified slanguagetypecode in
	 * userInfo.
	 * 
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and list of all active
	 *         Menu jsonObject and MultilingualMasters jsonObject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getLanguage(UserInfo userInfo) throws Exception {
		return languagesDAO.getLanguage(userInfo);
	}

	/**
	 * This method is used to retrieve active menu jsonobject based on the specified
	 * nmenuCode.
	 * 
	 * @param nmenuCode [int] primary key of menu jsonobject
	 * @return response entity object holding response status and data of menu
	 *         jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveMenuByID(int nmenucode, UserInfo userInfo) throws Exception {
		final QualisMenu objQualisMenu = languagesDAO.getActiveMenuByID(nmenucode, userInfo);
		if (objQualisMenu == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objQualisMenu, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active Module jsonobject based on the
	 * specified nmodulecode.
	 * 
	 * @param nmodulecode [int] primary key of Module jsonobject
	 * @return response entity object holding response status and data of menu
	 *         jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveModuleByID(int nmodulecode, UserInfo userInfo) throws Exception {
		final QualisModule objQualisModule = languagesDAO.getActiveModuleByID(nmodulecode, userInfo);
		if (objQualisModule == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objQualisModule, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active form jsonobject based on the specified
	 * nformcode.
	 * 
	 * @param nformcode [int] primary key of form jsonobject
	 * @return response entity object holding response status and data of form
	 *         jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveFormByID(int nformcode, UserInfo userInfo) throws Exception {
		final QualisForms objQualisForms = languagesDAO.getActiveFormByID(nformcode, userInfo);
		if (objQualisForms == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objQualisForms, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active TransactionStatus jsonobject based on
	 * the specified ntranscode.
	 * 
	 * @param ntranscode [int] primary key of TransactionStatus jsonobject
	 * @return response entity object holding response status and data of
	 *         TransactionStatus jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveTransactionStatusByID(int ntranscode, UserInfo userInfo) throws Exception {
		final TransactionStatus objTransactionStatus = languagesDAO.getActiveTransactionStatusByID(ntranscode,
				userInfo);
		if (objTransactionStatus == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objTransactionStatus, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active ControlMaster jsonobject based on the
	 * specified ncontrolcode.
	 * 
	 * @param ncontrolcode [int] primary key of ControlMaster jsonobject
	 * @return response entity object holding response status and data of
	 *         ControlMaster jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveControlMasterByID(int ncontrolcode, UserInfo userInfo) throws Exception {
		final ControlMaster objControlMaster = languagesDAO.getActiveControlMasterByID(ncontrolcode, userInfo);
		if (objControlMaster == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objControlMaster, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active ApprovalSubType jsonobject based on
	 * the specified napprovalsubtypecode.
	 * 
	 * @param napprovalsubtypecode [int] primary key of ApprovalSubType jsonobject
	 * @return response entity object holding response status and data of
	 *         ApprovalSubType jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveApprovalSubTypeByID(int napprovalsubtypecode, UserInfo userInfo)
			throws Exception {
		final ApprovalSubType objapprovalsubtype = languagesDAO.getActiveApprovalSubTypeByID(napprovalsubtypecode,
				userInfo);
		if (objapprovalsubtype == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objapprovalsubtype, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active SampleType jsonobject based on the
	 * specified nsampletypecode.
	 * 
	 * @param nsampletypecode [int] primary key of SampleType jsonobject
	 * @return response entity object holding response status and data of SampleType
	 *         jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleTypeByID(int nsampletypecode, UserInfo userInfo) throws Exception {
		final SampleType objSampleType = languagesDAO.getActiveSampleTypeByID(nsampletypecode, userInfo);
		if (objSampleType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objSampleType, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active Period jsonobject based on the
	 * specified nperiodcode.
	 * 
	 * @param nperiodcode [int] primary key of Period jsonobject
	 * @return response entity object holding response status and data of Period
	 *         jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActivePeriodByID(int nperiodcode, UserInfo userInfo) throws Exception {
		final Period period = languagesDAO.getActivePeriodByID(nperiodcode, userInfo);
		if (period == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(period, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active gender jsonobject based on the
	 * specified ngendercode.
	 * 
	 * @param ngendercode [int] primary key of gender jsonobject
	 * @return response entity object holding response status and data of gender
	 *         jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveGenderByID(int ngendercode, UserInfo userInfo) throws Exception {
		final Gender objGender = languagesDAO.getActiveGenderByID(ngendercode, userInfo);
		if (objGender == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objGender, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active grade jsonobject based on the
	 * specified ngradecode.
	 * 
	 * @param ngradecode [int] primary key of grade jsonobject
	 * @return response entity object holding response status and data of grade
	 *         jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveGradeByID(int ngradecode, UserInfo userInfo) throws Exception {
		final Grade objGrade = languagesDAO.getActiveGradeByID(ngradecode, userInfo);
		if (objGrade == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objGrade, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active SchedulerType jsonobject based on the
	 * specified nschedulertypecode.
	 * 
	 * @param nschedulertypecode [int] primary key of SchedulerType jsonobject
	 * @return response entity object holding response status and data of
	 *         SchedulerType jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSchedulerTypeByID(int nschedulertypecode, UserInfo userInfo)
			throws Exception {
		final SchedulerType objSchedulerType = languagesDAO.getActiveSchedulerTypeByID(nschedulertypecode, userInfo);
		if (objSchedulerType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objSchedulerType, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveMultilingualMastersByID(int nmultilingualmasterscode, UserInfo userInfo)
			throws Exception {
		final MultilingualMasters objMultilingualMasters = languagesDAO
				.getActiveMultilingualMastersByID(nmultilingualmasterscode, userInfo);
		if (objMultilingualMasters == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objMultilingualMasters, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active QueryBuilderTables jsonobject based on
	 * the specified nquerybuildertablecode.
	 * 
	 * @param nquerybuildertablecode [int] primary key of QueryBuilderTables
	 *                               jsonobject
	 * @return response entity object holding response status and data of
	 *         QueryBuilderTables jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveQueryBuilderTablesByID(int nquerybuildertablecode, UserInfo userInfo)
			throws Exception {
		final QueryBuilderTables objQueryBuilderTables = languagesDAO
				.getActiveQueryBuilderTablesByID(nquerybuildertablecode, userInfo);
		if (objQueryBuilderTables == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objQueryBuilderTables, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active QueryBuilderViews jsonobject based on
	 * the specified nquerybuilderviewscode.
	 * 
	 * @param nquerybuilderviewscode [int] primary key of QueryBuilderViews
	 *                               jsonobject
	 * @return response entity object holding response status and data of
	 *         QueryBuilderViews jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveQueryBuilderViewsByID(int nquerybuilderviewscode, UserInfo userInfo)
			throws Exception {
		final QueryBuilderViews queryBuilderViews = languagesDAO.getActiveQueryBuilderViewsByID(nquerybuilderviewscode,
				userInfo);
		if (queryBuilderViews == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(queryBuilderViews, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveQueryBuilderViewsColumnsByID(int languagesparam, String sviewname,
			UserInfo userInfo, String keysvalue) throws Exception {
		final QueryBuilderViewsColumns queryBuilderViewsColumns = languagesDAO
				.getActiveQueryBuilderViewsColumnsByID(languagesparam, sviewname, userInfo, keysvalue);
		if (queryBuilderViewsColumns == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(queryBuilderViewsColumns, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveMaterialTypeByID(int nmaterialtypecode, UserInfo userInfo) throws Exception {
		final MaterialType materialtype = languagesDAO.getActiveMaterialTypeByID(nmaterialtypecode, userInfo);
		if (materialtype == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(materialtype, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveInterfaceTypeByID(int ninterfacetypecode, UserInfo userInfo)
			throws Exception {
		final InterfaceType objInterfaceType = languagesDAO.getActiveInterfaceTypeByID(ninterfacetypecode, userInfo);
		if (objInterfaceType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objInterfaceType, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveAuditActionFilterByID(int nauditactionfiltercode, UserInfo userInfo)
			throws Exception {
		final AuditActionFilter auditactionfilter = languagesDAO.getActiveAuditActionFilterByID(nauditactionfiltercode,
				userInfo);
		if (auditactionfilter == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(auditactionfilter, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveAttachmentTypeByID(int nattachmenttypecode, UserInfo userInfo)
			throws Exception {
		final MultilingualMasters attactmentType = languagesDAO.getActiveAttachmentTypeByID(nattachmenttypecode,
				userInfo);
		if (attactmentType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(attactmentType, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveFTPTypeByID(int nftptypecode, UserInfo userInfo) throws Exception {
		final MultilingualMasters FTPType = languagesDAO.getActiveFTPTypeByID(nftptypecode, userInfo);
		if (FTPType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(FTPType, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveReportTypeByID(int nreporttypecode, UserInfo userInfo) throws Exception {
		final ReportType reportType = languagesDAO.getActiveReportTypeByID(nreporttypecode, userInfo);
		if (reportType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(reportType, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveCOAReportTypeByID(int ncoareporttypecode, UserInfo userInfo)
			throws Exception {
		final COAReportType COAReportType = languagesDAO.getActiveCOAReportTypeByID(ncoareporttypecode, userInfo);
		if (COAReportType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(COAReportType, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveReactComponentByID(int nreactcomponentcode, UserInfo userInfo)
			throws Exception {
		final ReactComponents reactComponents = languagesDAO.getActiveReactComponentByID(nreactcomponentcode, userInfo);
		if (reactComponents == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(reactComponents, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveFunctionsByID(int nfunctioncode, UserInfo userInfo) throws Exception {
		final Functions functions = languagesDAO.getActiveFunctionsByID(nfunctioncode, userInfo);
		if (functions == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(functions, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveDynamicFormulaFieldByID(int ndynamicformulafieldcode, UserInfo userInfo)
			throws Exception {
		final DynamicFormulaFields dynamicformulafields = languagesDAO
				.getActiveDynamicFormulaFieldByID(ndynamicformulafieldcode, userInfo);
		if (dynamicformulafields == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(dynamicformulafields, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveChartTypeByID(int ncharttypecode, UserInfo userInfo) throws Exception {
		final ChartType chartType = languagesDAO.getActiveChartTypeByID(ncharttypecode, userInfo);
		if (chartType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(chartType, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveDesignComponentByID(int ndesigncomponentcode, UserInfo userInfo)
			throws Exception {
		final DesignComponents designComponents = languagesDAO.getActiveDesignComponentByID(ndesigncomponentcode,
				userInfo);
		if (designComponents == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(designComponents, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveCheckListComponentByID(int nchecklistcomponentcode, UserInfo userInfo)
			throws Exception {
		final ChecklistComponent checklistComponent = languagesDAO
				.getActiveCheckListComponentByID(nchecklistcomponentcode, userInfo);
		if (checklistComponent == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(checklistComponent, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveGenericLabelByID(int ngenericlabelcode, UserInfo userInfo) throws Exception {
		final GenericLabel genericLabel = languagesDAO.getActiveGenericLabelByID(ngenericlabelcode, userInfo);
		if (genericLabel == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(genericLabel, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveQueryBuilderTableColumnsByID(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		final QueryBuilderTableColumns queryBuilderTableColumns = languagesDAO
				.getActiveQueryBuilderTableColumnsByID(userInfo, inputMap);
		if (queryBuilderTableColumns == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(queryBuilderTableColumns, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveDynamicAuditTableByID(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		final DynamicAuditTable dynamicAuditTable = languagesDAO.getActiveDynamicAuditTableByID(userInfo, inputMap);
		if (dynamicAuditTable == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(dynamicAuditTable, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveMappedTemplateFieldPropsByID(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		final Map<String, Object> mappedTemplateFieldProps = (Map<String, Object>) languagesDAO
				.getActiveMappedTemplateFieldPropsByID(userInfo, inputMap);
		if (mappedTemplateFieldProps == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(mappedTemplateFieldProps, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to update entry in qualismenu table.
	 * 
	 * @param objQualisMenu [QualisMenu] object holding details to be updated in
	 *                      qualismenu table
	 * @return response entity object holding response status and data of updated
	 *         menu jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateMenuLanguage(QualisMenu objQualisMenu, UserInfo userInfo) throws Exception {
		return languagesDAO.updateMenuLanguage(objQualisMenu, userInfo);
	}

	/**
	 * This method is used to update entry in qualismodule table.
	 * 
	 * @param objQualisModule [QualisModule] object holding details to be updated in
	 *                        qualismodule table
	 * @return response entity object holding response status and data of updated
	 *         Module jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateModuleLanguage(QualisModule objQualisModule, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateModuleLanguage(objQualisModule, userInfo);
	}

	/**
	 * This method is used to update entry in qualisforms table.
	 * 
	 * @param objQualisForms [QualisForms] object holding details to be updated in
	 *                       qualisforms table
	 * @return response entity object holding response status and data of updated
	 *         Form jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateFormLanguage(QualisForms objQualisForms, UserInfo userInfo) throws Exception {
		return languagesDAO.updateFormLanguage(objQualisForms, userInfo);
	}

	/**
	 * This method is used to update entry in TransactionStatus table.
	 * 
	 * @param objTransactionStatus [TransactionStatus] object holding details to be
	 *                             updated in TransactionStatus table
	 * @return response entity object holding response status and data of updated
	 *         TransactionStatus jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateTransactionStatusLanguage(TransactionStatus objTransactionStatus,
			UserInfo userInfo) throws Exception {
		return languagesDAO.updateTransactionStatusLanguage(objTransactionStatus, userInfo);
	}

	/**
	 * This method is used to update entry in ControlMaster table.
	 * 
	 * @param objControlMaster [ControlMaster] object holding details to be updated
	 *                         in ControlMaster table
	 * @return response entity object holding response status and data of updated
	 *         ControlMaster jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateControlMasterLanguage(ControlMaster objControlMaster, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateControlMasterLanguage(objControlMaster, userInfo);
	}

	/**
	 * This method is used to update entry in ApprovalSubType table.
	 * 
	 * @param objApprovalSubType [ApprovalSubType] object holding details to be
	 *                           updated in ApprovalSubType table
	 * @return response entity object holding response status and data of updated
	 *         ApprovalSubType jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateApprovalSubTypeLanguage(ApprovalSubType objapprovalsubtype, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateApprovalSubTypeLanguage(objapprovalsubtype, userInfo);
	}

	/**
	 * This method is used to update entry in SampleType table.
	 * 
	 * @param objSampleType [SampleType] object holding details to be updated in
	 *                      SampleType table
	 * @return response entity object holding response status and data of updated
	 *         SampleType jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleTypeLanguage(SampleType objSampleType, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateSampleTypeLanguage(objSampleType, userInfo);
	}

	/**
	 * This method is used to update entry in Period table.
	 * 
	 * @param period [Period] object holding details to be updated in Period table
	 * @return response entity object holding response status and data of updated
	 *         Period jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updatePeriodLanguage(Period period, UserInfo userInfo) throws Exception {
		return languagesDAO.updatePeriodLanguage(period, userInfo);
	}

	/**
	 * This method is used to update entry in Gender table.
	 * 
	 * @param objGender [Gender] object holding details to be updated in Gender
	 *                  table
	 * @return response entity object holding response status and data of updated
	 *         Gender jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateGenderLanguage(Gender objGender, UserInfo userInfo) throws Exception {
		return languagesDAO.updateGenderLanguage(objGender, userInfo);
	}

	/**
	 * This method is used to update entry in Grade table.
	 * 
	 * @param objGrade [Grade] object holding details to be updated in Grade table
	 * @return response entity object holding response status and data of updated
	 *         Grade jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateGradeLanguage(Grade objGrade, UserInfo userInfo) throws Exception {
		return languagesDAO.updateGradeLanguage(objGrade, userInfo);
	}

	/**
	 * This method is used to update entry in SchedulerType table.
	 * 
	 * @param objSchedulerType [SchedulerType] object holding details to be updated
	 *                         in SchedulerType table
	 * @return response entity object holding response status and data of updated
	 *         SchedulerType jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSchedulerTypeLanguage(SchedulerType objSchedulerType, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateSchedulerTypeLanguage(objSchedulerType, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateMultilingualMastersLanguage(MultilingualMasters objMultilingualMasters,
			UserInfo userInfo) throws Exception {
		return languagesDAO.updateMultilingualMastersLanguage(objMultilingualMasters, userInfo);
	}

	/**
	 * This method is used to update entry in QueryBuilderTables table.
	 * 
	 * @param objQueryBuilderTables [QueryBuilderTables] object holding details to
	 *                              be updated in QueryBuilderTables table
	 * @return response entity object holding response status and data of updated
	 *         QueryBuilderTables jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateQueryBuilderTablesLanguage(QueryBuilderTables objQueryBuilderTables,
			UserInfo userInfo) throws Exception {
		return languagesDAO.updateQueryBuilderTablesLanguage(objQueryBuilderTables, userInfo);
	}

	/**
	 * This method is used to update entry in QueryBuilderViews table.
	 * 
	 * @param objQueryBuilderViews [QueryBuilderViews] object holding details to be
	 *                             updated in QueryBuilderViews table
	 * @return response entity object holding response status and data of updated
	 *         QueryBuilderViews jsonobject
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateQueryBuilderViewsLanguage(QueryBuilderViews queryBuilderViews,
			UserInfo userInfo) throws Exception {
		return languagesDAO.updateQueryBuilderViewsLanguage(queryBuilderViews, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateQueryBuilderViewsColumnsLanguage(
			QueryBuilderViewsColumns queryBuilderViewsColumns, UserInfo userInfo, int nindexoff, String keyvalue)
			throws Exception {
		return languagesDAO.updateQueryBuilderViewsColumnsLanguage(queryBuilderViewsColumns, userInfo, nindexoff,
				keyvalue);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateMaterialTypeLanguage(MaterialType materialtype, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateMaterialTypeLanguage(materialtype, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateInterfaceTypeLanguage(InterfaceType objInterfaceType, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateInterfaceTypeLanguage(objInterfaceType, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateAuditActionFilterLanguage(AuditActionFilter actionauditfilter,
			UserInfo userInfo) throws Exception {
		return languagesDAO.updateAuditActionFilterLanguage(actionauditfilter, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateAttachmentTypeLanguage(MultilingualMasters attachmentType, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateAttachmentTypeLanguage(attachmentType, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateFTPTypeLanguage(MultilingualMasters FTPType, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateFTPTypeLanguage(FTPType, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateReportTypeLanguage(ReportType reportType, UserInfo userInfo) throws Exception {
		return languagesDAO.updateReportTypeLanguage(reportType, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateCOAReportTypeLanguage(COAReportType COAReportType, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateCOAReportTypeLanguage(COAReportType, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateReactComponentsLanguage(ReactComponents reactComponents, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateReactComponentsLanguage(reactComponents, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateFunctionsLanguage(Functions functions, UserInfo userInfo) throws Exception {
		return languagesDAO.updateFunctionsLanguage(functions, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateDynamicFormulaFieldLanguage(DynamicFormulaFields dynamicFormulaFields,
			UserInfo userInfo) throws Exception {
		return languagesDAO.updateDynamicFormulaFieldLanguage(dynamicFormulaFields, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateChartTypeLanguage(ChartType chartType, UserInfo userInfo) throws Exception {
		return languagesDAO.updateChartTypeLanguage(chartType, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateDesignComponentLanguage(DesignComponents designComponent, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateDesignComponentLanguage(designComponent, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateCheckListComponentLanguage(ChecklistComponent checklistComponents,
			UserInfo userInfo) throws Exception {
		return languagesDAO.updateCheckListComponentLanguage(checklistComponents, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateGenericLabelLanguage(GenericLabel genericLabels, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateGenericLabelLanguage(genericLabels, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateQueryBuilderTableColumnsLanguage(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		return languagesDAO.updateQueryBuilderTableColumnsLanguage(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateDynamicAuditTableLanguage(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return languagesDAO.updateDynamicAuditTableLanguage(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateMappedTemplateFieldPropsLanguage(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		return languagesDAO.updateMappedTemplateFieldPropsLanguage(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of active menu slanguagetypecode for the
	 * specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of menu jsonobject for which the list
	 *                 is to be fetched
	 * @return response entity object holding response status and list of active
	 *         menu slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getMenuComboService(UserInfo userInfo, int nmenucode) throws Exception {
		return languagesDAO.getMenuComboService(userInfo, nmenucode);
	}

	/**
	 * This method is used to retrieve list of active module slanguagetypecode for
	 * the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of module jsonobject for which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         module slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getModuleComboService(UserInfo userInfo, int nmodulecode) throws Exception {
		return languagesDAO.getModuleComboService(userInfo, nmodulecode);
	}

	/**
	 * This method is used to retrieve list of active Forms slanguagetypecode for
	 * the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of Forms jsonobject for which the list
	 *                 is to be fetched
	 * @return response entity object holding response status and list of active
	 *         Forms slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getFormComboService(UserInfo userInfo, int nformcode) throws Exception {
		return languagesDAO.getFormComboService(userInfo, nformcode);
	}

	/**
	 * This method is used to retrieve list of active TransactionStatus
	 * slanguagetypecode for the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of TransactionStatus jsonobject for
	 *                 which the list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         TransactionStatus slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getTransactionStatusComboService(UserInfo userInfo, int ntranscode) throws Exception {
		return languagesDAO.getTransactionStatusComboService(userInfo, ntranscode);
	}

	/**
	 * This method is used to retrieve list of active ControlMaster
	 * slanguagetypecode for the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of ControlMaster jsonobject for which
	 *                 the list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         ControlMaster slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getControlMasterComboService(UserInfo userInfo, int ncontrolcode) throws Exception {
		return languagesDAO.getControlMasterComboService(userInfo, ncontrolcode);
	}

	/**
	 * This method is used to retrieve list of active ApprovalSubType
	 * slanguagetypecode for the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of ApprovalSubType jsonobject for
	 *                 which the list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         ApprovalSubType slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getApprovalSubTypeComboService(UserInfo userInfo, int napprovalsubtypecode)
			throws Exception {
		return languagesDAO.getApprovalSubTypeComboService(userInfo, napprovalsubtypecode);
	}

	/**
	 * This method is used to retrieve list of active SampleType slanguagetypecode
	 * for the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of SampleType jsonobject for which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         SampleType slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getSampleTypeComboService(UserInfo userInfo, int nsampletypecode) throws Exception {
		return languagesDAO.getSampleTypeComboService(userInfo, nsampletypecode);
	}

	/**
	 * This method is used to retrieve list of active Period slanguagetypecode for
	 * the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of Period jsonobject for which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         Period slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getPeriodComboService(UserInfo userInfo, int nperiodcode) throws Exception {
		return languagesDAO.getPeriodComboService(userInfo, nperiodcode);
	}

	/**
	 * This method is used to retrieve list of active Gender slanguagetypecode for
	 * the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of Gender jsonobject for which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         Gender slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getGenderComboService(UserInfo userInfo, int ngendercode) throws Exception {
		return languagesDAO.getGenderComboService(userInfo, ngendercode);
	}

	/**
	 * This method is used to retrieve list of active Grade slanguagetypecode for
	 * the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of Grade jsonobject for which the list
	 *                 is to be fetched
	 * @return response entity object holding response status and list of active
	 *         Grade slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getGradeComboService(UserInfo userInfo, int ngradecode) throws Exception {
		return languagesDAO.getGradeComboService(userInfo, ngradecode);
	}

	/**
	 * This method is used to retrieve list of active SchedulerType
	 * slanguagetypecode for the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of SchedulerType jsonobject for which
	 *                 the list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         SchedulerType slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getSchedulerTypeComboService(UserInfo userInfo, int nschedulertypecode)
			throws Exception {
		return languagesDAO.getSchedulerTypeComboService(userInfo, nschedulertypecode);
	}

	@Override
	public ResponseEntity<Object> getMultilingualMastersComboService(UserInfo userInfo, int nmultilingualmasterscode)
			throws Exception {
		return languagesDAO.getMultilingualMastersComboService(userInfo, nmultilingualmasterscode);
	}

	/**
	 * This method is used to retrieve list of active QueryBuilderTables
	 * slanguagetypecode for the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of QueryBuilderTables jsonobject for
	 *                 which the list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         QueryBuilderTables slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getQueryBuilderTablesComboService(UserInfo userInfo, int nquerybuildertablecode)
			throws Exception {
		return languagesDAO.getQueryBuilderTablesComboService(userInfo, nquerybuildertablecode);
	}

	/**
	 * This method is used to retrieve list of active QueryBuilderViews
	 * slanguagetypecode for the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of QueryBuilderViews jsonobject for
	 *                 which the list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         QueryBuilderViews slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getQueryBuilderViewsComboService(UserInfo userInfo, int nquerybuilderviewscode)
			throws Exception {
		return languagesDAO.getQueryBuilderViewsComboService(userInfo, nquerybuilderviewscode);
	}

	/**
	 * This method is used to retrieve list of active QueryBuilderViewsColumns
	 * slanguagetypecode for the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] primary key of QueryBuilderViewsColumns jsonobject
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of active
	 *         QueryBuilderViewsColumns slanguagetypecode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getQueryBuilderViewsColumnsComboService(UserInfo userInfo, String sviewname)
			throws Exception {
		return languagesDAO.getQueryBuilderViewsColumnsComboService(userInfo, sviewname);
	}

	@Override
	public ResponseEntity<Object> getMaterialConfigComboService(UserInfo userInfo, int nmaterialconfigcode)
			throws Exception {
		return languagesDAO.getMaterialConfigComboService(userInfo, nmaterialconfigcode);
	}

	@Override
	public ResponseEntity<Object> getMaterialTypeComboService(UserInfo userInfo, int nmaterialtypecode)
			throws Exception {
		return languagesDAO.getMaterialTypeComboService(userInfo, nmaterialtypecode);
	}

	@Override
	public ResponseEntity<Object> getInterfaceTypeComboService(UserInfo userInfo, int ninterfacetypecode)
			throws Exception {
		return languagesDAO.getInterfaceTypeComboService(userInfo, ninterfacetypecode);
	}

	@Override
	public ResponseEntity<Object> getAuditActionFilterComboService(UserInfo userInfo, int nauditactionfiltercode)
			throws Exception {
		return languagesDAO.getAuditActionFilterComboService(userInfo, nauditactionfiltercode);
	}

	@Override
	public ResponseEntity<Object> getAttachmenttypeComboService(UserInfo userInfo, int nattachmenttypecode)
			throws Exception {
		return languagesDAO.getAttachmenttypeComboService(userInfo, nattachmenttypecode);
	}

	@Override
	public ResponseEntity<Object> getFTPtypeComboService(UserInfo userInfo, int nftptypecode) throws Exception {
		return languagesDAO.getFTPtypeComboService(userInfo, nftptypecode);
	}

	@Override
	public ResponseEntity<Object> getReportTypeComboService(UserInfo userInfo, int nreporttypecode) throws Exception {
		return languagesDAO.getReportTypeComboService(userInfo, nreporttypecode);
	}

	@Override
	public ResponseEntity<Object> getCOAReportTypeComboService(UserInfo userInfo, int ncoareporttypecode)
			throws Exception {
		return languagesDAO.getCOAReportTypeComboService(userInfo, ncoareporttypecode);
	}

	@Override
	public ResponseEntity<Object> getReactComponentsComboService(UserInfo userInfo, int nreactcomponentcode)
			throws Exception {
		return languagesDAO.getReactComponentsComboService(userInfo, nreactcomponentcode);
	}

	@Override
	public ResponseEntity<Object> getFunctionsComboService(UserInfo userInfo, int nfunctioncode) throws Exception {
		return languagesDAO.getFunctionsComboService(userInfo, nfunctioncode);
	}

	@Override
	public ResponseEntity<Object> getDynamicFormulaFieldsComboService(UserInfo userInfo, int ndynamicformulafieldcode)
			throws Exception {
		return languagesDAO.getDynamicFormulaFieldsComboService(userInfo, ndynamicformulafieldcode);
	}

	@Override
	public ResponseEntity<Object> getChartTypeComboService(UserInfo userInfo, int ncharttypecode) throws Exception {
		return languagesDAO.getChartTypeComboService(userInfo, ncharttypecode);
	}

	@Override
	public ResponseEntity<Object> getDesignComponentsComboService(UserInfo userInfo, int ndesigncomponentcode)
			throws Exception {
		return languagesDAO.getDesignComponentsComboService(userInfo, ndesigncomponentcode);
	}

	@Override
	public ResponseEntity<Object> getCheckListComponentService(UserInfo userInfo, int nchecklistcomponentcode)
			throws Exception {
		return languagesDAO.getCheckListComponentService(userInfo, nchecklistcomponentcode);
	}

	@Override
	public ResponseEntity<Object> getGenericLabelService(UserInfo userInfo, int ngenericlabelcode) throws Exception {
		return languagesDAO.getGenericLabelService(userInfo, ngenericlabelcode);
	}

	@Override
	public ResponseEntity<Object> getQueryBuilderTableColumnsService(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		return languagesDAO.getQueryBuilderTableColumnsService(userInfo, inputMap);
	}

	@Override
	public ResponseEntity<Object> getDynamicAuditTableService(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		return languagesDAO.getDynamicAuditTableService(userInfo, inputMap);
	}

	@Override
	public ResponseEntity<Object> getMappedTemplateFieldPropsService(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		return languagesDAO.getMappedTemplateFieldPropsService(userInfo, inputMap);
	}

}
