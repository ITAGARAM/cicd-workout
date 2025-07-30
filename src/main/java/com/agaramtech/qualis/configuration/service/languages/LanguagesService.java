package com.agaramtech.qualis.configuration.service.languages;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.audittrail.model.AuditActionFilter;
import com.agaramtech.qualis.basemaster.model.Gender;
import com.agaramtech.qualis.basemaster.model.Period;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.checklist.model.ChecklistComponent;
import com.agaramtech.qualis.configuration.model.ApprovalSubType;
import com.agaramtech.qualis.configuration.model.GenericLabel;
import com.agaramtech.qualis.configuration.model.SampleType;
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

/**
 * This interface declaration holds methods to perform CRUD operation
 * 
 * @author ATE224
 * @version 10.0.0.2
 * @since 10- June- 2022
 */
public interface LanguagesService {
	/**
	 * This interface declaration is used to get the over all Menu jsonObject and
	 * MultilingualMasters jsonObject with respect to slanguagetypecode in userInfo
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the Active List of Menu jsonObject and
	 *         MultilingualMasters jsonObject with respect to slanguagetypecode in
	 *         userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getLanguage(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the Menu from MultilingualMasters
	 * table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key nmenucode which holds the value of
	 *                 respective menu slanguagetypecode
	 * @return a response entity which holds the Active Menu slanguagetypecode with
	 *         respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getMenuComboService(UserInfo userInfo, int nmenucode) throws Exception;

	/**
	 * This interface declaration is used to get the Module from MultilingualMasters
	 * table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key nmodulecode which holds the value of
	 *                 respective module slanguagetypecode
	 * @return a response entity which holds the Active module slanguagetypecode
	 *         with respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getModuleComboService(UserInfo userInfo, int nmodulecode) throws Exception;

	/**
	 * This interface declaration is used to get the form from MultilingualMasters
	 * table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key nformcode which holds the value of
	 *                 respective form slanguagetypecode
	 * @return a response entity which holds the Active form slanguagetypecode with
	 *         respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getFormComboService(UserInfo userInfo, int nformcode) throws Exception;

	/**
	 * This interface declaration is used to get the transactiostatus from
	 * MultilingualMasters table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key ntranscode which holds the value of
	 *                 respective transactionstatus slanguagetypecode
	 * @return a response entity which holds the Active transactionstatus
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTransactionStatusComboService(UserInfo userInfo, int ntranscode) throws Exception;

	/**
	 * This interface declaration is used to get the controlmaster from
	 * MultilingualMasters table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key ncontrolcode which holds the value of
	 *                 respective controlmaster slanguagetypecode
	 * @return a response entity which holds the Active controlmaster
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getControlMasterComboService(UserInfo userInfo, int ncontrolcode) throws Exception;

	/**
	 * This interface declaration is used to get the approvalsubtype from
	 * MultilingualMasters table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key napprovalsubtypecode which holds the value
	 *                 of respective approvalsubtype slanguagetypecode
	 * @return a response entity which holds the Active approvalsubtype
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getApprovalSubTypeComboService(UserInfo userInfo, int napprovalsubtypecode)
			throws Exception;

	/**
	 * This interface declaration is used to get the sampletype from
	 * MultilingualMasters table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key nsampletypecode which holds the value of
	 *                 respective sampletype slanguagetypecode
	 * @return a response entity which holds the Active sampletype slanguagetypecode
	 *         with respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSampleTypeComboService(UserInfo userInfo, int nsampletypecode) throws Exception;

//	/**
//	 * This interface declaration is used to get the templatetype from
//	 * MultilingualMasters table with respect to slanguagetypecode in userInfo
//	 * 
//	 * @param inputMap [Map] contains key ntemplatetypecode which holds the value of
//	 *                 respective templatetype slanguagetypecode
//	 * @return a response entity which holds the Active templatetype
//	 *         slanguagetypecode with respect to userInfo and also have the HTTP
//	 *         response code
//	 * @throws Exception
//	 */
//	public ResponseEntity<Object> getTemplateTypeComboService(UserInfo userInfo, int ntemplatetypecode)
//			throws Exception;

	/**
	 * This interface declaration is used to get the period from MultilingualMasters
	 * table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key nperiodcode which holds the value of
	 *                 respective period slanguagetypecode
	 * @return a response entity which holds the Active period slanguagetypecode
	 *         with respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getPeriodComboService(UserInfo userInfo, int nperiodcode) throws Exception;

	/**
	 * This interface declaration is used to get the gender from MultilingualMasters
	 * table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key ngendercode which holds the value of
	 *                 respective gender slanguagetypecode
	 * @return a response entity which holds the Active gender slanguagetypecode
	 *         with respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getGenderComboService(UserInfo userInfo, int ngendercode) throws Exception;

	/**
	 * This interface declaration is used to get the grade from MultilingualMasters
	 * table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key ngradecode which holds the value of
	 *                 respective grade slanguagetypecode
	 * @return a response entity which holds the Active grade slanguagetypecode with
	 *         respect to userInfo and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getGradeComboService(UserInfo userInfo, int ngradecode) throws Exception;

	/**
	 * This interface declaration is used to get the schedulertype from
	 * MultilingualMasters table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key nschedulertypecode which holds the value
	 *                 of respective schedulertype slanguagetypecode
	 * @return a response entity which holds the Active schedulertype
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSchedulerTypeComboService(UserInfo userInfo, int nschedulertypecode)
			throws Exception;

	/**
	 * This interface declaration is used to get the multilingualmasters from
	 * MultilingualMasters table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key nmultilingualmasterscode which holds the
	 *                 value of respective multilingualmasters slanguagetypecode
	 * @return a response entity which holds the Active multilingualmasters
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getMultilingualMastersComboService(UserInfo userInfo, int nmultilingualmasterscode)
			throws Exception;

	/**
	 * This interface declaration is used to get the querybuildertables from
	 * MultilingualMasters table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key nquerybuildertablecode which holds the
	 *                 value of respective querybuildertables slanguagetypecode
	 * @return a response entity which holds the Active querybuildertables
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getQueryBuilderTablesComboService(UserInfo userInfo, int nquerybuildertablecode)
			throws Exception;

	/**
	 * This interface declaration is used to get the querybuilderviews from
	 * MultilingualMasters table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key nquerybuilderviewscode which holds the
	 *                 value of respective querybuilderviews slanguagetypecode
	 * @return a response entity which holds the Active querybuilderviews
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getQueryBuilderViewsComboService(UserInfo userInfo, int nquerybuilderviewscode)
			throws Exception;

	/**
	 * This interface declaration is used to get the querybuilderviewscolumns from
	 * MultilingualMasters table with respect to slanguagetypecode in userInfo
	 * 
	 * @param inputMap [Map] contains key sviewname which holds the value of
	 *                 respective querybuilderviewscolumns slanguagetypecode
	 * @return a response entity which holds the Active querybuilderviewscolumns
	 *         slanguagetypecode with respect to userInfo and also have the HTTP
	 *         response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getQueryBuilderViewsColumnsComboService(UserInfo userInfo, String sviewname)
			throws Exception;

	public ResponseEntity<Object> getMaterialConfigComboService(UserInfo userInfo, int nmaterialconfigcode)
			throws Exception;

	public ResponseEntity<Object> getMaterialTypeComboService(UserInfo userInfo, int nmaterialtypecode)
			throws Exception;

	public ResponseEntity<Object> getInterfaceTypeComboService(UserInfo userInfo, int ninterfacetypecode)
			throws Exception;
	
	public ResponseEntity<Object> getAuditActionFilterComboService(UserInfo userInfo, int nauditactionfiltercode)
			throws Exception;
	
	public ResponseEntity<Object> getAttachmenttypeComboService(UserInfo userInfo, int nattachmenttypecode)
			throws Exception;

	
	public ResponseEntity<Object> getFTPtypeComboService(UserInfo userInfo, int nftptypecode)
			throws Exception;
	
	public ResponseEntity<Object> getReportTypeComboService(UserInfo userInfo, int nreporttypecode)
			throws Exception;
	
	public ResponseEntity<Object> getCOAReportTypeComboService(UserInfo userInfo, int ncoareporttypecode)
			throws Exception;
	
	public ResponseEntity<Object> getReactComponentsComboService(UserInfo userInfo, int nreactcomponentcode)
			throws Exception;
	
	public ResponseEntity<Object> getFunctionsComboService(UserInfo userInfo, int nfunctioncode)
			throws Exception;
	
	public ResponseEntity<Object> getDynamicFormulaFieldsComboService(UserInfo userInfo, int ndynamicformulafieldcode)
			throws Exception;
	
	public ResponseEntity<Object> getChartTypeComboService(UserInfo userInfo, int ncharttypecode)
			throws Exception;
	
	public ResponseEntity<Object> getDesignComponentsComboService(UserInfo userInfo, int ndesigncomponentcode)
			throws Exception;
	
	public ResponseEntity<Object> getCheckListComponentService(UserInfo userInfo, int nchecklistcomponentcode)
			throws Exception;
	
	public ResponseEntity<Object> getGenericLabelService(UserInfo userInfo, int ngenericlabelcode)
			throws Exception;
	
	public ResponseEntity<Object> getQueryBuilderTableColumnsService(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;
	
	public ResponseEntity<Object> getDynamicAuditTableService(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> getMappedTemplateFieldPropsService(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active menu jsonobject based
	 * on the specified nmenuCode.
	 * 
	 * @param nmenuCode [int] primary key of menu jsonobject
	 * @return response entity object holding response status and data of menu json
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveMenuByID(int nmenucode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active module jsonobject based
	 * on the specified nmoduleCode.
	 * 
	 * @param nmoduleCode [int] primary key of module jsonobject
	 * @return response entity object holding response status and data of module
	 *         json object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveModuleByID(int nmodulecode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active forms jsonobject based
	 * on the specified nformCode.
	 * 
	 * @param nformCode [int] primary key of form jsonobject
	 * @return response entity object holding response status and data of form json
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveFormByID(int nformcode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active transactionstatus
	 * jsonobject based on the specified ntransCode.
	 * 
	 * @param ntransCode [int] primary key of transactionstatus jsonobject
	 * @return response entity object holding response status and data of
	 *         transactionstatus json object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveTransactionStatusByID(int ntranscode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active controlmaster
	 * jsonobject based on the specified ncontrolCode.
	 * 
	 * @param ncontrolCode [int] primary key of controlmaster jsonobject
	 * @return response entity object holding response status and data of
	 *         controlmaster json object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveControlMasterByID(int ncontrolcode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active approvalsubtype
	 * jsonobject based on the specified napprovalsubtypecode.
	 * 
	 * @param napprovalsubtypecode [int] primary key of approvalsubtype jsonobject
	 * @return response entity object holding response status and data of
	 *         approvalsubtype jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveApprovalSubTypeByID(int napprovalsubtypecode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active sampletype jsonobject
	 * based on the specified nsampletypecode.
	 * 
	 * @param nsampletypecode [int] primary key of sampletype jsonobject
	 * @return response entity object holding response status and data of sampletype
	 *         jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveSampleTypeByID(int nsampletypecode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active period jsonobject based
	 * on the specified nperiodcode.
	 * 
	 * @param nperiodcode [int] primary key of period jsonobject
	 * @return response entity object holding response status and data of period
	 *         jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActivePeriodByID(int nperiodcode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active gender jsonobject based
	 * on the specified ngendercode.
	 * 
	 * @param ngendercode [int] primary key of gender jsonobject
	 * @return response entity object holding response status and data of gender
	 *         jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveGenderByID(int ngendercode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active grade jsonobject based
	 * on the specified ngradecode.
	 * 
	 * @param ngradecode [int] primary key of grade jsonobject
	 * @return response entity object holding response status and data of grade
	 *         jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveGradeByID(int ngradecode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active schedulertype
	 * jsonobject based on the specified nschedulertypecode.
	 * 
	 * @param nschedulertypecode [int] primary key of schedulertype jsonobject
	 * @return response entity object holding response status and data of
	 *         schedulertype jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveSchedulerTypeByID(int nschedulertypecode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active MultilingualMasters
	 * jsonobject based on the specified nmultilingualmasterscode.
	 * 
	 * @param nrecorddetailtypecode [int] primary key of MultilingualMasters
	 *                              jsonobject
	 * @return response entity object holding response status and data of
	 *         MultilingualMasters jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveMultilingualMastersByID(int nmultilingualmasterscode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active querybuildertable
	 * jsonobject based on the specified nquerybuildertablecode.
	 * 
	 * @param nquerybuildertablecode [int] primary key of querybuildertable
	 *                               jsonobject
	 * @return response entity object holding response status and data of
	 *         querybuildertable jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveQueryBuilderTablesByID(int nquerybuildertablecode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active querybuilderviews
	 * jsonobject based on the specified nquerybuilderviewscode.
	 * 
	 * @param nquerybuilderviewscode [int] primary key of querybuilderviews
	 *                               jsonobject
	 * @return response entity object holding response status and data of
	 *         querybuilderviews jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveQueryBuilderViewsByID(int nquerybuilderviewscode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveQueryBuilderViewsColumnsByID(int languagesparam, String sviewname,
			UserInfo userInfo,String keysvalue) throws Exception;

	public ResponseEntity<Object> getActiveMaterialTypeByID(int nmaterialtypecode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveInterfaceTypeByID(int ninterfacetypecode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveAttachmentTypeByID(int nattachmenttypecode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveFTPTypeByID(int nftptypecode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveReportTypeByID(int nreporttypecode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveCOAReportTypeByID(int ncoareporttypecode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveAuditActionFilterByID(int nauditactionfiltercode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveReactComponentByID(int nreactcomponentcode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveFunctionsByID(int nfunctioncode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveDynamicFormulaFieldByID(int ndynamicformulafieldcode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveChartTypeByID(int ncharttypecode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveDesignComponentByID(int ndesigncomponentcode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveCheckListComponentByID(int nchecklistcomponentcode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveGenericLabelByID(int ngenericlabelcode, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getActiveQueryBuilderTableColumnsByID(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;
	
	public ResponseEntity<Object> getActiveDynamicAuditTableByID(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> getActiveMappedTemplateFieldPropsByID(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;
	
	/**
	 * This interface declaration is used to update entry in QualisMenu table.
	 * 
	 * @param objQualisMenu [QualisMenu] object holding details to be updated in
	 *                      QualisMenu table
	 * @return response entity object holding response status and data of updated
	 *         Menu jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateMenuLanguage(QualisMenu objQualisMenu, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in QualisModule table.
	 * 
	 * @param objQualisModule [QualisModule] object holding details to be updated in
	 *                        QualisModule table
	 * @return response entity object holding response status and data of updated
	 *         Module jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateModuleLanguage(QualisModule objQualisModule, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in QualisForms table.
	 * 
	 * @param objQualisForms [QualisForms] object holding details to be updated in
	 *                       QualisForms table
	 * @return response entity object holding response status and data of updated
	 *         Forms jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateFormLanguage(QualisForms objQualisForms, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in TransactionStatus
	 * table.
	 * 
	 * @param objTransactionStatus [TransactionStatus] object holding details to be
	 *                             updated in TransactionStatus table
	 * @return response entity object holding response status and data of updated
	 *         TransactionStatus jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateTransactionStatusLanguage(TransactionStatus objTransactionStatus,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in ControlMaster table.
	 * 
	 * @param objControlMaster [ControlMaster] object holding details to be updated
	 *                         in ControlMaster table
	 * @return response entity object holding response status and data of updated
	 *         ControlMaster jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateControlMasterLanguage(ControlMaster objControlMaster, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in ApprovalSubType table.
	 * 
	 * @param objApprovalSubType [ApprovalSubType] object holding details to be
	 *                           updated in ApprovalSubType table
	 * @return response entity object holding response status and data of updated
	 *         ApprovalSubType jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateApprovalSubTypeLanguage(ApprovalSubType objapprovalsubtype, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in SampleType table.
	 * 
	 * @param objSampleType [SampleType] object holding details to be updated in
	 *                      SampleType table
	 * @return response entity object holding response status and data of updated
	 *         SampleType jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSampleTypeLanguage(SampleType objSampleType, UserInfo userInfo)
			throws Exception;

/**
	 * This interface declaration is used to update entry in Period table.
	 * 
	 * @param period [Period] object holding details to be updated in Period table
	 * @return response entity object holding response status and data of updated
	 *         Period jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updatePeriodLanguage(Period period, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in Gender table.
	 * 
	 * @param objGender [Gender] object holding details to be updated in Gender
	 *                  table
	 * @return response entity object holding response status and data of updated
	 *         Gender jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateGenderLanguage(Gender objGender, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in Grade table.
	 * 
	 * @param objGrade [Grade] object holding details to be updated in Grade table
	 * @return response entity object holding response status and data of updated
	 *         Grade jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateGradeLanguage(Grade objGrade, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in SchedulerType table.
	 * 
	 * @param objSchedulerType [SchedulerType] object holding details to be updated
	 *                         in SchedulerType table
	 * @return response entity object holding response status and data of updated
	 *         SchedulerType jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSchedulerTypeLanguage(SchedulerType objSchedulerType, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in multilingualmasters
	 * table.
	 * 
	 * @param objMultilingualMasters [MultilingualMasters] object holding details to
	 *                               be updated in multilingualmasters table
	 * @return response entity object holding response status and data of updated
	 *         MultilingualMasters jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateMultilingualMastersLanguage(MultilingualMasters objMultilingualMasters,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in QueryBuilderTables
	 * table.
	 * 
	 * @param objQueryBuilderTables [QueryBuilderTables] object holding details to
	 *                              be updated in QueryBuilderTables table
	 * @return response entity object holding response status and data of updated
	 *         QueryBuilderTables jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateQueryBuilderTablesLanguage(QueryBuilderTables objQueryBuilderTables,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in QueryBuilderViews
	 * table.
	 * 
	 * @param queryBuilderViews [QueryBuilderViews] object holding details to be
	 *                          updated in QueryBuilderViews table
	 * @return response entity object holding response status and data of updated
	 *         QueryBuilderViews jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateQueryBuilderViewsLanguage(QueryBuilderViews queryBuilderViews,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateQueryBuilderViewsColumnsLanguage(
			QueryBuilderViewsColumns queryBuilderViewsColumns, UserInfo userInfo, int nindexoff,String keyvalue) throws Exception;

	public ResponseEntity<Object> updateMaterialTypeLanguage(MaterialType materialtype, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateInterfaceTypeLanguage(InterfaceType objInterfaceType, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateAuditActionFilterLanguage(AuditActionFilter auditActionFilter, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateAttachmentTypeLanguage(MultilingualMasters attachmentType, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateFTPTypeLanguage( MultilingualMasters FTPType, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateReportTypeLanguage( ReportType reportType, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateCOAReportTypeLanguage(COAReportType COAReportType, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateReactComponentsLanguage(ReactComponents reactComponents, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateFunctionsLanguage(Functions  functions, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateDynamicFormulaFieldLanguage(DynamicFormulaFields  dynamicFormulaFields, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateChartTypeLanguage(ChartType  chartType, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateDesignComponentLanguage(DesignComponents  designComponent, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateCheckListComponentLanguage(ChecklistComponent  checklistComponents, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateGenericLabelLanguage(GenericLabel  genericLabels, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateQueryBuilderTableColumnsLanguage(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateDynamicAuditTableLanguage(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateMappedTemplateFieldPropsLanguage(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;
	
}
