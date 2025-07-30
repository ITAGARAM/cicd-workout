package com.agaramtech.qualis.configuration.service.languages;

import java.util.Map;
import org.springframework.http.ResponseEntity;
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
import com.agaramtech.qualis.testmanagement.model.InterfaceType;
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
 * This interface holds declarations to perform CRUD operation
 * 
 * @author ATE224
 * @version 10.0.0.2
 * @since 10 - June -2022
 */
public interface LanguagesDAO {
	/**
	 * This interface declaration is used to get the over all Menu jsonObject and
	 * MultilingualMasters jsonObject with respect to userInfo
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Menu jsonObject and
	 *         MultilingualMasters jsonObject with respect to slanguagetypecode and
	 *         also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getLanguage(final UserInfo userInfo) throws Exception;

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
	public ResponseEntity<Object> getMenuComboService(final UserInfo userInfo, final int nmenucode) throws Exception;

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
	public ResponseEntity<Object> getModuleComboService(final UserInfo userInfo, final int nmodulecode)
			throws Exception;

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
	public ResponseEntity<Object> getFormComboService(final UserInfo userInfo, final int nformcode) throws Exception;

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
	public ResponseEntity<Object> getTransactionStatusComboService(final UserInfo userInfo, final int ntranscode)
			throws Exception;

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
	public ResponseEntity<Object> getControlMasterComboService(final UserInfo userInfo, final int ncontrolcode)
			throws Exception;

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
	public ResponseEntity<Object> getApprovalSubTypeComboService(final UserInfo userInfo,
			final int napprovalsubtypecode) throws Exception;

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
	public ResponseEntity<Object> getSampleTypeComboService(final UserInfo userInfo, final int nsampletypecode)
			throws Exception;

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
	public ResponseEntity<Object> getPeriodComboService(final UserInfo userInfo, final int nperiodcode)
			throws Exception;

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
	public ResponseEntity<Object> getGenderComboService(final UserInfo userInfo, final int ngendercode)
			throws Exception;

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
	public ResponseEntity<Object> getGradeComboService(final UserInfo userInfo, final int ngradecode) throws Exception;

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
	public ResponseEntity<Object> getSchedulerTypeComboService(final UserInfo userInfo, final int nschedulertypecode)
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
	public ResponseEntity<Object> getMultilingualMastersComboService(final UserInfo userInfo,
			final int nmultilingualmasterscode) throws Exception;

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
	public ResponseEntity<Object> getQueryBuilderTablesComboService(final UserInfo userInfo,
			final int nquerybuildertablecode) throws Exception;

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
	public ResponseEntity<Object> getQueryBuilderViewsComboService(final UserInfo userInfo,
			final int nquerybuilderviewscode) throws Exception;

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
	public ResponseEntity<Object> getQueryBuilderViewsColumnsComboService(final UserInfo userInfo,
			final String sviewname) throws Exception;

	public ResponseEntity<Object> getMaterialConfigComboService(final UserInfo userInfo, final int nmaterialconfigcode)
			throws Exception;

	public ResponseEntity<Object> getMaterialTypeComboService(final UserInfo userInfo, final int nmaterialtypecode)
			throws Exception;

	public ResponseEntity<Object> getInterfaceTypeComboService(final UserInfo userInfo, final int ninterfacetypecode)
			throws Exception;

	public ResponseEntity<Object> getAuditActionFilterComboService(final UserInfo userInfo,
			final int nauditactionfiltercode) throws Exception;

	public ResponseEntity<Object> getAttachmenttypeComboService(final UserInfo userInfo, final int nattachmenttypecode)
			throws Exception;

	public ResponseEntity<Object> getFTPtypeComboService(final UserInfo userInfo, final int nftptypecode)
			throws Exception;

	public ResponseEntity<Object> getReportTypeComboService(final UserInfo userInfo, final int nreporttypecode)
			throws Exception;

	public ResponseEntity<Object> getCOAReportTypeComboService(final UserInfo userInfo, final int ncoareporttypecode)
			throws Exception;

	public ResponseEntity<Object> getReactComponentsComboService(final UserInfo userInfo, final int nreactcomponentcode)
			throws Exception;

	public ResponseEntity<Object> getFunctionsComboService(final UserInfo userInfo, final int nfunctioncode)
			throws Exception;

	public ResponseEntity<Object> getDynamicFormulaFieldsComboService(final UserInfo userInfo,
			final int ndynamicformulafieldcode) throws Exception;

	public ResponseEntity<Object> getChartTypeComboService(final UserInfo userInfo, final int ncharttypecode)
			throws Exception;

	public ResponseEntity<Object> getDesignComponentsComboService(final UserInfo userInfo,
			final int ndesigncomponentcode) throws Exception;

	public ResponseEntity<Object> getCheckListComponentService(final UserInfo userInfo,
			final int nchecklistcomponentcode) throws Exception;

	public ResponseEntity<Object> getGenericLabelService(final UserInfo userInfo, final int ngenericlabelcode)
			throws Exception;

	public ResponseEntity<Object> getQueryBuilderTableColumnsService(final UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getDynamicAuditTableService(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> getMappedTemplateFieldPropsService(final UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception;

	/**
	 * This interface declaration is used to retrieve active menu jsonobject based
	 * on the specified nmenuCode.
	 * 
	 * @param nmenuCode [int] primary key of menu jsonobject
	 * @return response entity object holding response status and data of menu json
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public QualisMenu getActiveMenuByID(final int nmenucode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active module jsonobject based
	 * on the specified nmoduleCode.
	 * 
	 * @param nmoduleCode [int] primary key of module jsonobject
	 * @return response entity object holding response status and data of module
	 *         json object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public QualisModule getActiveModuleByID(final int nmodulecode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active forms jsonobject based
	 * on the specified nformCode.
	 * 
	 * @param nformCode [int] primary key of form jsonobject
	 * @return response entity object holding response status and data of form json
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public QualisForms getActiveFormByID(final int nformcode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active transactionstatus
	 * jsonobject based on the specified ntransCode.
	 * 
	 * @param ntransCode [int] primary key of transactionstatus jsonobject
	 * @return response entity object holding response status and data of
	 *         transactionstatus json object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public TransactionStatus getActiveTransactionStatusByID(final int ntranscode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active controlmaster
	 * jsonobject based on the specified ncontrolCode.
	 * 
	 * @param ncontrolCode [int] primary key of controlmaster jsonobject
	 * @return response entity object holding response status and data of
	 *         controlmaster json object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ControlMaster getActiveControlMasterByID(final int ncontrolcode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active approvalsubtype
	 * jsonobject based on the specified napprovalsubtypecode.
	 * 
	 * @param napprovalsubtypecode [int] primary key of approvalsubtype jsonobject
	 * @return response entity object holding response status and data of
	 *         approvalsubtype jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ApprovalSubType getActiveApprovalSubTypeByID(final int napprovalsubtypecode, final UserInfo userInfo)
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
	public SampleType getActiveSampleTypeByID(final int nsampletypecode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active period jsonobject based
	 * on the specified nperiodcode.
	 * 
	 * @param nperiodcode [int] primary key of period jsonobject
	 * @return response entity object holding response status and data of period
	 *         jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Period getActivePeriodByID(final int nperiodcode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active gender jsonobject based
	 * on the specified ngendercode.
	 * 
	 * @param ngendercode [int] primary key of gender jsonobject
	 * @return response entity object holding response status and data of gender
	 *         jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Gender getActiveGenderByID(final int ngendercode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active grade jsonobject based
	 * on the specified ngradecode.
	 * 
	 * @param ngradecode [int] primary key of grade jsonobject
	 * @return response entity object holding response status and data of grade
	 *         jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Grade getActiveGradeByID(final int ngradecode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active schedulertype
	 * jsonobject based on the specified nschedulertypecode.
	 * 
	 * @param nschedulertypecode [int] primary key of schedulertype jsonobject
	 * @return response entity object holding response status and data of
	 *         schedulertype jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public SchedulerType getActiveSchedulerTypeByID(final int nschedulertypecode, final UserInfo userInfo)
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
	public MultilingualMasters getActiveMultilingualMastersByID(final int nmultilingualmasterscode,
			final UserInfo userInfo) throws Exception;

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
	public QueryBuilderTables getActiveQueryBuilderTablesByID(final int nquerybuildertablecode, final UserInfo userInfo)
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
	public QueryBuilderViews getActiveQueryBuilderViewsByID(final int nquerybuilderviewscode, final UserInfo userInfo)
			throws Exception;

	public QueryBuilderViewsColumns getActiveQueryBuilderViewsColumnsByID(final int languagesparam,
			final String sviewname, final UserInfo userInfo, final String keysvalue) throws Exception;

	public MaterialType getActiveMaterialTypeByID(final int nmaterialtypecode, final UserInfo userInfo)
			throws Exception;

	public InterfaceType getActiveInterfaceTypeByID(final int ninterfacetypecode, final UserInfo userInfo)
			throws Exception;

	public AuditActionFilter getActiveAuditActionFilterByID(final int nauditactionfiltercode, final UserInfo userInfo)
			throws Exception;

	public MultilingualMasters getActiveAttachmentTypeByID(final int nattachmenttypecode, final UserInfo userInfo)
			throws Exception;

	public MultilingualMasters getActiveFTPTypeByID(final int nftptypecode, final UserInfo userInfo) throws Exception;

	public ReportType getActiveReportTypeByID(final int nreporttypecode, final UserInfo userInfo) throws Exception;

	public COAReportType getActiveCOAReportTypeByID(final int ncoareporttypecode, final UserInfo userInfo)
			throws Exception;

	public ReactComponents getActiveReactComponentByID(final int nreactcomponentcode, final UserInfo userInfo)
			throws Exception;

	public Functions getActiveFunctionsByID(final int nfunctioncode, final UserInfo userInfo) throws Exception;

	public DynamicFormulaFields getActiveDynamicFormulaFieldByID(final int ndynamicformulafieldcode,
			final UserInfo userInfo) throws Exception;

	public ChartType getActiveChartTypeByID(final int ncharttypecode, final UserInfo userInfo) throws Exception;

	public DesignComponents getActiveDesignComponentByID(final int ndesigncomponentcode, final UserInfo userInfo)
			throws Exception;

	public ChecklistComponent getActiveCheckListComponentByID(final int nchecklistcomponentcode,
			final UserInfo userInfo) throws Exception;

	public GenericLabel getActiveGenericLabelByID(final int ngenericlabelcode, final UserInfo userInfo)
			throws Exception;

	public QueryBuilderTableColumns getActiveQueryBuilderTableColumnsByID(final UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception;

	public DynamicAuditTable getActiveDynamicAuditTableByID(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	public Map<String, Object> getActiveMappedTemplateFieldPropsByID(final UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception;

	/**
	 * This interface declaration is used to update entry in QualisMenu table.
	 * 
	 * @param objQualisMenu [QualisMenu] object holding details to be updated in
	 *                      QualisMenu table
	 * @return response entity object holding response status and data of updated
	 *         Menu jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateMenuLanguage(final QualisMenu objQualisMenu, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in QualisModule table.
	 * 
	 * @param objQualisModule [QualisModule] object holding details to be updated in
	 *                        QualisModule table
	 * @return response entity object holding response status and data of updated
	 *         Module jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateModuleLanguage(final QualisModule objQualisModule, final UserInfo userInfo)
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
	public ResponseEntity<Object> updateFormLanguage(final QualisForms objQualisForms, final UserInfo userInfo)
			throws Exception;

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
	public ResponseEntity<Object> updateTransactionStatusLanguage(final TransactionStatus objTransactionStatus,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in ControlMaster table.
	 * 
	 * @param objControlMaster [ControlMaster] object holding details to be updated
	 *                         in ControlMaster table
	 * @return response entity object holding response status and data of updated
	 *         ControlMaster jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateControlMasterLanguage(final ControlMaster objControlMaster,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in ApprovalSubType table.
	 * 
	 * @param objApprovalSubType [ApprovalSubType] object holding details to be
	 *                           updated in ApprovalSubType table
	 * @return response entity object holding response status and data of updated
	 *         ApprovalSubType jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateApprovalSubTypeLanguage(final ApprovalSubType objapprovalsubtype,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in SampleType table.
	 * 
	 * @param objSampleType [SampleType] object holding details to be updated in
	 *                      SampleType table
	 * @return response entity object holding response status and data of updated
	 *         SampleType jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSampleTypeLanguage(final SampleType objSampleType, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in Period table.
	 * 
	 * @param period [Period] object holding details to be updated in Period table
	 * @return response entity object holding response status and data of updated
	 *         Period jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updatePeriodLanguage(final Period period, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in Gender table.
	 * 
	 * @param objGender [Gender] object holding details to be updated in Gender
	 *                  table
	 * @return response entity object holding response status and data of updated
	 *         Gender jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateGenderLanguage(final Gender objGender, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in Grade table.
	 * 
	 * @param objGrade [Grade] object holding details to be updated in Grade table
	 * @return response entity object holding response status and data of updated
	 *         Grade jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateGradeLanguage(final Grade objGrade, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in SchedulerType table.
	 * 
	 * @param objSchedulerType [SchedulerType] object holding details to be updated
	 *                         in SchedulerType table
	 * @return response entity object holding response status and data of updated
	 *         SchedulerType jsonobject
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSchedulerTypeLanguage(final SchedulerType objSchedulerType,
			final UserInfo userInfo) throws Exception;

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
	public ResponseEntity<Object> updateMultilingualMastersLanguage(final MultilingualMasters objMultilingualMasters,
			final UserInfo userInfo) throws Exception;

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
	public ResponseEntity<Object> updateQueryBuilderTablesLanguage(final QueryBuilderTables objQueryBuilderTables,
			final UserInfo userInfo) throws Exception;

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
	public ResponseEntity<Object> updateQueryBuilderViewsLanguage(final QueryBuilderViews queryBuilderViews,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateQueryBuilderViewsColumnsLanguage(
			final QueryBuilderViewsColumns queryBuilderViewsColumns, final UserInfo userInfo, final int nindexoff,
			final String keyvalue) throws Exception;

	public ResponseEntity<Object> updateMaterialTypeLanguage(final MaterialType materialtype, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateInterfaceTypeLanguage(final InterfaceType objInterfacetype,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateAuditActionFilterLanguage(final AuditActionFilter auditactionfilter,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateAttachmentTypeLanguage(final MultilingualMasters attachmentType,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateFTPTypeLanguage(final MultilingualMasters FTPType, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateReportTypeLanguage(final ReportType reportType, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateCOAReportTypeLanguage(final COAReportType COAReportType,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateReactComponentsLanguage(final ReactComponents reactComponents,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateFunctionsLanguage(final Functions functions, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateDynamicFormulaFieldLanguage(final DynamicFormulaFields dynamicFormulaFields,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateChartTypeLanguage(final ChartType chartType, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateDesignComponentLanguage(final DesignComponents designComponent,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateCheckListComponentLanguage(final ChecklistComponent checklistComponents,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateGenericLabelLanguage(final GenericLabel genericLists, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateQueryBuilderTableColumnsLanguage(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateDynamicAuditTableLanguage(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateMappedTemplateFieldPropsLanguage(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

}
