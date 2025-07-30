package com.agaramtech.qualis.configuration.service.languages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
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
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialConfig;
import com.agaramtech.qualis.materialmanagement.model.MaterialType;
import com.agaramtech.qualis.multilingualmasters.model.MultilingualMasters;
import com.agaramtech.qualis.reports.model.COAReportType;
import com.agaramtech.qualis.reports.model.ReportType;
import com.agaramtech.qualis.scheduler.model.SchedulerType;
import com.agaramtech.qualis.testmanagement.model.DynamicFormulaFields;
import com.agaramtech.qualis.testmanagement.model.Functions;
import com.agaramtech.qualis.testmanagement.model.Grade;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "Language" table by
 * implementing methods from its interface.
 */
@RequiredArgsConstructor
@Repository
public class LanguagesDAOImpl implements LanguagesDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(LanguagesDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;

	/**
	 * This method is used to retrieve list of all active Menu jsonObject and
	 * MultilingualMasters jsonObject for the specified userInfo.
	 * 
	 * @param userInfo [UserInfo] ,the list is to be fetched by slanguagetypecode
	 * @return response entity object holding response status and list of all active
	 *         Menu jsonObject and MultilingualMasters jsonObject
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getLanguage(UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String querymultilingual = "select nsorter,nmultilingualmasterscode,coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', jsondata->'sdisplayname'->>'en-US') as sdisplayname , jsondata->'sdisplayname'->>'en-US' as displayname,jsondata->>'needheader' as sneedheader "
				+ " from multilingualmasters where nmultilingualmasterscode > 0   " + "   and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter  asc";
		List<MultilingualMasters> lstmultilingual = (List<MultilingualMasters>) jdbcTemplate.query(querymultilingual,
				new MultilingualMasters());
		LOGGER.info("getLanguage -->");
		outputMap.put("multilingualmasters", lstmultilingual);
		outputMap.put("selectedmultilingualmasters", lstmultilingual.get(0));

		final String strQuery = "select q.*,coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + "q.jsondata->'sdisplayname'->>'en-US') as sdisplayname ,"
				+ "(q.jsondata->'sdisplayname'->>'en-US') as sdefaultname ,"
				+ " q.smenuname from qualismenu q where q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and  q.nmenucode > 0";
		List<QualisMenu> lstMenu = (List<QualisMenu>) jdbcTemplate.query(strQuery, new QualisMenu());
		outputMap.put("listofItem", lstMenu);
		outputMap.put("headername", "Menu");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public QualisMenu getActiveMenuByID(int nmenucode, UserInfo userInfo) throws Exception {
		final String qualismenuQuery = "select q.*,coalesce(q.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',q.jsondata->'sdisplayname'->>'en-US') as sdisplayname ,"
				+ "(q.jsondata->'sdisplayname'->>'en-US') as sdefaultname ," + "q.smenuname from qualismenu q "
				+ "where q.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and q.nmenucode=q.nmenucode and q.nmenucode = " + nmenucode;
		return (QualisMenu) jdbcUtilityFunction.queryForObject(qualismenuQuery, QualisMenu.class, jdbcTemplate);
	}

	@Override
	public QualisModule getActiveModuleByID(int nmodulecode, UserInfo userInfo) throws Exception {
		final String qualismoduleQuery = "select qm.*,coalesce(qm.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',qm.jsondata->'sdisplayname'->>'en-US') as sdisplayname ,"
				+ "(qm.jsondata->'sdisplayname'->>'en-US') as sdefaultname ," + "qm.smodulename from qualismodule qm "
				+ "where qm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and qm.nmodulecode=" + nmodulecode;
		return (QualisModule) jdbcUtilityFunction.queryForObject(qualismoduleQuery, QualisModule.class, jdbcTemplate);
	}

	@Override
	public QualisForms getActiveFormByID(int nformcode, UserInfo userInfo) throws Exception {
		final String qualisformsQuery = "select qf.*,coalesce(qf.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',qf.jsondata->'sdisplayname'->>'en-US') as sdisplayname ,"
				+ "(qf.jsondata->'sdisplayname'->>'en-US') as sdefaultname ," + "qf.sformname from qualisforms qf "
				+ "where qf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and qf.nformcode=" + nformcode;
		return (QualisForms) jdbcUtilityFunction.queryForObject(qualisformsQuery, QualisForms.class, jdbcTemplate);
	}

	@Override
	public TransactionStatus getActiveTransactionStatusByID(int ntranscode, UserInfo userInfo) throws Exception {
		final String transactionstatusQuery = "select ts.*, "
				+ "(ts.jsondata->'sactiondisplaystatus'->>'en-US') as sdefaultname ,(ts.jsondata->'stransdisplaystatus'->>'en-US') as sdefaultname,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,"
				+ "coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'sactiondisplaystatus'->>'en-US') as sactiondisplaystatus,"
				+ "ts.stransstatus from transactionstatus ts " + "where ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ts.ntranscode=" + ntranscode;
		return (TransactionStatus) jdbcUtilityFunction.queryForObject(transactionstatusQuery, TransactionStatus.class,
				jdbcTemplate);
	}

	@Override
	public ControlMaster getActiveControlMasterByID(int ncontrolcode, UserInfo userInfo) throws Exception {
		final String controlmasterQuery = "select cm.*,cm.jsondata->'scontrolids'->>'en-US' as sdefaultname ,"
				+ "coalesce(cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
				+ "',cm.jsondata->'scontrolids'->>'en-US') as sdisplayname," + "cm.scontrolname from controlmaster cm "
				+ "where cm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and cm.ncontrolcode=" + ncontrolcode;
		return (ControlMaster) jdbcUtilityFunction.queryForObject(controlmasterQuery, ControlMaster.class,
				jdbcTemplate);
	}

	@Override
	public ApprovalSubType getActiveApprovalSubTypeByID(int napprovalsubtypecode, UserInfo userInfo) throws Exception {
		final String approvalsubtypeQuery = "select ast.*,ast.jsondata->'approvalsubtypename'->>'en-US' as sdefaultname ,"
				+ "coalesce(ast.jsondata->'approvalsubtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',ast.jsondata->'approvalsubtypename'->>'en-US') as sdisplayname"
				+ " from approvalsubtype ast where ast.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ast.napprovalsubtypecode="
				+ napprovalsubtypecode;
		return (ApprovalSubType) jdbcUtilityFunction.queryForObject(approvalsubtypeQuery, ApprovalSubType.class,
				jdbcTemplate);
	}

	@Override
	public SampleType getActiveSampleTypeByID(int nsampletypecode, UserInfo userInfo) throws Exception {
		final String sampletypeQuery = "select st.*,st.jsondata->'sampletypename'->>'en-US' as sdefaultname ,"
				+ "coalesce(st.jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',st.jsondata->'sampletypename'->>'en-US') as sdisplayname"
				+ " from sampletype st where st.nstatus in(-2, "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) and st.nsampletypecode="
				+ nsampletypecode;
		return (SampleType) jdbcUtilityFunction.queryForObject(sampletypeQuery, SampleType.class, jdbcTemplate);
	}

	@Override
	public Period getActivePeriodByID(int nperiodcode, UserInfo userInfo) throws Exception {
		final String periodQuery = "select p.jsondata,p.nperiodcode,p.nsitecode,p.nstatus,p.jsondata->'speriodname'->>'en-US' as sdefaultname ,"
				+ "coalesce(p.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "',p.jsondata->'speriodname'->>'en-US') as sdisplayname" + "  from period p where p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and p.nperiodcode=" + nperiodcode;
		return (Period) jdbcUtilityFunction.queryForObject(periodQuery, Period.class, jdbcTemplate);
	}

	@Override
	public Gender getActiveGenderByID(int ngendercode, UserInfo userInfo) throws Exception {
		final String genderQuery = "select g.*,g.jsondata->'sgendername'->>'en-US' as sdefaultname ,"
				+ "coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
				+ "',g.jsondata->'sgendername'->>'en-US') as sdisplayname" + " from gender g where g.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and g.ngendercode=" + ngendercode;
		return (Gender) jdbcUtilityFunction.queryForObject(genderQuery, Gender.class, jdbcTemplate);
	}

	@Override
	public Grade getActiveGradeByID(int ngradecode, UserInfo userInfo) throws Exception {
		final String gradeQuery = "select gr.*,gr.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(gr.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',gr.jsondata->'sdisplayname'->>'en-US') as sdisplayname" + " from grade gr where gr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and gr.ngradecode=" + ngradecode;
		return (Grade) jdbcUtilityFunction.queryForObject(gradeQuery, Grade.class, jdbcTemplate);
	}

	@Override
	public SchedulerType getActiveSchedulerTypeByID(int nschedulertypecode, UserInfo userInfo) throws Exception {
		final String schedulertypeQuery = "select st.*,st.jsondata->'sschedulertypename'->>'en-US' as sdefaultname ,"
				+ "coalesce(st.jsondata->'sschedulertypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',st.jsondata->'sschedulertypename'->>'en-US') as sdisplayname"
				+ " from schedulertype st where st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and st.nschedulertypecode="
				+ nschedulertypecode;
		return (SchedulerType) jdbcUtilityFunction.queryForObject(schedulertypeQuery, SchedulerType.class,
				jdbcTemplate);
	}

	@Override
	public MultilingualMasters getActiveMultilingualMastersByID(int nmultilingualmasterscode, UserInfo userInfo)
			throws Exception {
		final String multilingualMastersQuery = "select mm.nmultilingualmasterscode,mm.smultilingualmastername,mm.jsondata,mm.nsorter,mm.nsitecode,mm.nstatus,mm.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(mm.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',mm.jsondata->'sdisplayname'->>'en-US') as sdisplayname"
				+ " from multilingualmasters mm where mm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and mm.nmultilingualmasterscode="
				+ nmultilingualmasterscode;
		return (MultilingualMasters) jdbcUtilityFunction.queryForObject(multilingualMastersQuery,
				MultilingualMasters.class, jdbcTemplate);
	}

	@Override
	public QueryBuilderTables getActiveQueryBuilderTablesByID(int nquerybuildertablecode, UserInfo userInfo)
			throws Exception {
		final String sqlQueryBuilderQuery = "select qbt.*,qbt.jsondata->'tablename'->>'en-US' as sdefaultname ,"
				+ "coalesce(qbt.jsondata->'tablename'->>'" + userInfo.getSlanguagetypecode()
				+ "',qbt.jsondata->'tablename'->>'en-US') as sdisplayname"
				+ " from querybuildertables qbt where qbt .nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and qbt.nquerybuildertablecode="
				+ nquerybuildertablecode;
		return (QueryBuilderTables) jdbcUtilityFunction.queryForObject(sqlQueryBuilderQuery, QueryBuilderTables.class,
				jdbcTemplate);
	}

	@Override
	public QueryBuilderViews getActiveQueryBuilderViewsByID(int nquerybuilderviewscode, UserInfo userInfo)
			throws Exception {
		final String viewsQueryBuilderQuery = "select qbv.*,qbv.jsondata->'displayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(qbv.jsondata->'displayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',qbv.jsondata->'displayname'->>'en-US') as sdisplayname"
				+ " from querybuilderviews qbv where qbv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and qbv.nquerybuilderviewscode="
				+ nquerybuilderviewscode;
		return (QueryBuilderViews) jdbcUtilityFunction.queryForObject(viewsQueryBuilderQuery, QueryBuilderViews.class,
				jdbcTemplate);
	}

	@Override
	public QueryBuilderViewsColumns getActiveQueryBuilderViewsColumnsByID(int languagesparam, String sviewname,
			UserInfo userInfo, String keysvalue) throws Exception {
		final String testMasterQuery = "select qbvc.jsondata, qbvc.jsondata->'" + keysvalue + "'-> " + languagesparam
				+ " as sjsondata," + " case when (qbvc.jsondata->'" + keysvalue + "'->" + languagesparam
				+ "->'displayname'->>'" + userInfo.getSlanguagetypecode() + "' is null or qbvc.jsondata->'" + keysvalue
				+ "'->" + languagesparam + "->'displayname'->>'" + userInfo.getSlanguagetypecode()
				+ "'='') then qbvc.jsondata->'" + keysvalue + "'->" + languagesparam
				+ "->'displayname'->>'en-US' else qbvc.jsondata->'" + keysvalue + "'->" + languagesparam
				+ "->'displayname'->>'" + userInfo.getSlanguagetypecode() + "' end as sdefaultname,qbvc.sviewname,"
				+ "qbc.jsondata->'displayname'->>'" + userInfo.getSlanguagetypecode()
				+ "' as sdisplayname from querybuilderviewscolumns qbvc,querybuilderviews qbc where"
				+ " qbvc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qbc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qbvc.sviewname=qbc.sviewname and  qbvc.sviewname='" + sviewname + "";
		return (QueryBuilderViewsColumns) jdbcUtilityFunction.queryForObject(testMasterQuery,
				QueryBuilderViewsColumns.class, jdbcTemplate);
	}

	@Override
	public MaterialType getActiveMaterialTypeByID(int nmaterialtypecode, UserInfo userInfo) throws Exception {
		final String materialTypeQuery = "select mt.*,mt.jsondata->'smaterialtypename'->>'en-US' as sdefaultname ,"
				+ "coalesce(mt.jsondata->'smaterialtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',mt.jsondata->'smaterialtypename'->>'en-US') as smaterialtypename"
				+ " from materialtype mt where mt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and mt.nmaterialtypecode="
				+ nmaterialtypecode;
		return (MaterialType) jdbcUtilityFunction.queryForObject(materialTypeQuery, MaterialType.class, jdbcTemplate);
	}

	@Override
	public InterfaceType getActiveInterfaceTypeByID(int ninterfacetypecode, UserInfo userInfo) throws Exception {
		final String interfaceTypeQuery = "select mt.*,mt.jsondata->'sinterfacetypename'->>'en-US' as sdefaultname ,"
				+ "coalesce(mt.jsondata->'sinterfacetypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',mt.jsondata->'sinterfacetypename'->>'en-US') as sdisplayname"
				+ " from interfacetype mt where mt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and mt.ninterfacetypecode="
				+ ninterfacetypecode;
		return (InterfaceType) jdbcUtilityFunction.queryForObject(interfaceTypeQuery, InterfaceType.class,
				jdbcTemplate);
	}

	@Override
	public AuditActionFilter getActiveAuditActionFilterByID(int nauditactionfiltercode, UserInfo userInfo)
			throws Exception {
		final String auditActionFilterQuery = "select mt.*,mt.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(mt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',mt.jsondata->'sdisplayname'->>'en-US') as sdisplayname"
				+ " from auditactionfilter mt where mt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and mt.nauditactionfiltercode="
				+ nauditactionfiltercode;
		return (AuditActionFilter) jdbcUtilityFunction.queryForObject(auditActionFilterQuery, AuditActionFilter.class,
				jdbcTemplate);
	}

	@Override
	public MultilingualMasters getActiveAttachmentTypeByID(int nattachmenttypecode, UserInfo userInfo)
			throws Exception {
		final String attachmentTypeQuery = "select at.*,at.jsondata->'sattachmenttype'->>'en-US' as sdefaultname ,"
				+ "coalesce(at.jsondata->'sattachmenttype'->>'" + userInfo.getSlanguagetypecode()
				+ "',at.jsondata->'sattachmenttype'->>'en-US') as sdisplayname"
				+ " from attachmenttype at where at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and at.nattachmenttypecode="
				+ nattachmenttypecode;
		return (MultilingualMasters) jdbcUtilityFunction.queryForObject(attachmentTypeQuery, MultilingualMasters.class,
				jdbcTemplate);
	}

	@Override
	public MultilingualMasters getActiveFTPTypeByID(int nftptypecode, UserInfo userInfo) throws Exception {
		final String ftpTypeQuery = "select ft.nftptypecode, ft.jsondata, ft.ndefaultstatus, ft.nstatus,ft.sftptypename,ft.jsondata->'sftptypename'->>'en-US' as sdefaultname ,"
				+ "coalesce(ft.jsondata->'sftptypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',ft.jsondata->'sftptypename'->>'en-US') as sdisplayname" + " from ftptype ft where ft.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ft.nftptypecode=" + nftptypecode;
		return (MultilingualMasters) jdbcUtilityFunction.queryForObject(ftpTypeQuery, MultilingualMasters.class,
				jdbcTemplate);
	}

	@Override
	public ReportType getActiveReportTypeByID(int nreporttypecode, UserInfo userInfo) throws Exception {
		final String reportTypeQuery = "select rt.*,rt.sreporttypename ,rt.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(rt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sdisplayname'->>'en-US') as sdisplayname" + " from reporttype rt where rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and rt.nreporttypecode="
				+ nreporttypecode;
		return (ReportType) jdbcUtilityFunction.queryForObject(reportTypeQuery, ReportType.class, jdbcTemplate);
	}

	@Override
	public COAReportType getActiveCOAReportTypeByID(int ncoareporttypecode, UserInfo userInfo) throws Exception {
		final String CoareportTypeQuery = "select crt.*,crt.scoareporttypename ,crt.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(crt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',crt.jsondata->'sdisplayname'->>'en-US') as sdisplayname"
				+ " from coareporttype crt where crt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and crt.ncoareporttypecode="
				+ ncoareporttypecode;
		return (COAReportType) jdbcUtilityFunction.queryForObject(CoareportTypeQuery, COAReportType.class,
				jdbcTemplate);
	}

	@Override
	public ReactComponents getActiveReactComponentByID(int nreactcomponentcode, UserInfo userInfo) throws Exception {
		final String CoareportTypeQuery = "select rc.*,rc.jsondata->'componentname'->>'en-US' as sdefaultname ,"
				+ "coalesce(rc.jsondata->'componentname'->>'" + userInfo.getSlanguagetypecode()
				+ "',rc.jsondata->'componentname'->>'en-US') as sdisplayname"
				+ " from reactcomponents rc where rc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and rc.nreactcomponentcode="
				+ nreactcomponentcode;
		return (ReactComponents) jdbcUtilityFunction.queryForObject(CoareportTypeQuery, ReactComponents.class,
				jdbcTemplate);
	}

	@Override
	public Functions getActiveFunctionsByID(int nfunctioncode, UserInfo userInfo) throws Exception {
		final String functionsQuery = "select f.*,f.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(f.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',f.jsondata->'sdisplayname'->>'en-US') as sdisplayname" + " from functions f where f.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and f.nfunctioncode="
				+ nfunctioncode;
		return (Functions) jdbcUtilityFunction.queryForObject(functionsQuery, Functions.class, jdbcTemplate);
	}

	@Override
	public DynamicFormulaFields getActiveDynamicFormulaFieldByID(int ndynamicformulafieldcode, UserInfo userInfo)
			throws Exception {
		final String dynamicformulafieldQuery = "select df.ndynamicformulafieldcode,df.jsondata,df.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(df.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',df.jsondata->'sdisplayname'->>'en-US') as sdisplayname"
				+ " from dynamicformulafields df where df.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and df.ndynamicformulafieldcode="
				+ ndynamicformulafieldcode;
		return (DynamicFormulaFields) jdbcUtilityFunction.queryForObject(dynamicformulafieldQuery,
				DynamicFormulaFields.class, jdbcTemplate);
	}

	@Override
	public ChartType getActiveChartTypeByID(int ncharttypecode, UserInfo userInfo) throws Exception {
		final String charttypeQuery = "select ct.*,ct.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(ct.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',ct.jsondata->'sdisplayname'->>'en-US') as sdisplayname" + " from charttype ct where ct.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ct.ncharttypecode="
				+ ncharttypecode;
		return (ChartType) jdbcUtilityFunction.queryForObject(charttypeQuery, ChartType.class, jdbcTemplate);
	}

	@Override
	public DesignComponents getActiveDesignComponentByID(int ndesigncomponentcode, UserInfo userInfo) throws Exception {
		final String DesignComponentsQuery = "select dc.*,dc.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(dc.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',dc.jsondata->'sdisplayname'->>'en-US') as sdisplayname"
				+ " from designcomponents dc where dc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and dc.ndesigncomponentcode="
				+ ndesigncomponentcode;
		return (DesignComponents) jdbcUtilityFunction.queryForObject(DesignComponentsQuery, DesignComponents.class,
				jdbcTemplate);
	}

	@Override
	public ChecklistComponent getActiveCheckListComponentByID(int nchecklistcomponentcode, UserInfo userInfo)
			throws Exception {
		final String ChecklistComponentQuery = "select clc.*,clc.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(clc.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',clc.jsondata->'sdisplayname'->>'en-US') as sdisplayname"
				+ " from checklistcomponent clc where clc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and clc.nchecklistcomponentcode="
				+ nchecklistcomponentcode;
		return (ChecklistComponent) jdbcUtilityFunction.queryForObject(ChecklistComponentQuery,
				ChecklistComponent.class, jdbcTemplate);
	}

	@Override
	public GenericLabel getActiveGenericLabelByID(int ngenericlabelcode, UserInfo userInfo) throws Exception {
		final String GenericLabelQuery = "select gl.*,gl.jsondata->'sdisplayname'->>'en-US' as sdefaultname ,"
				+ "coalesce(gl.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',gl.jsondata->'sdisplayname'->>'en-US') as sdisplayname"
				+ " from genericlabel gl where gl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and gl.ngenericlabelcode="
				+ ngenericlabelcode;
		return (GenericLabel) jdbcUtilityFunction.queryForObject(GenericLabelQuery, GenericLabel.class, jdbcTemplate);
	}

	@Override
	public QueryBuilderTableColumns getActiveQueryBuilderTableColumnsByID(UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception {
		String ColumnName = inputMap.get("scolumnname").toString().replace("\"", "").trim();
		final String QueryBuilderTableColumnsQuery = "select ordinality-1 as index, coalesce(arr."
				+ inputMap.get("tablecolumnname") + "->'displayname'->>'" + userInfo.getSlanguagetypecode() + "', arr."
				+ inputMap.get("tablecolumnname") + "->'displayname'->>'en-US') as sdisplayname," + " arr."
				+ inputMap.get("tablecolumnname") + "->'displayname'->>'en-US' as sdefaultname, " + "arr."
				+ inputMap.get("tablecolumnname") + " as sjsondata,"
				+ "qbtc.* from querybuildertablecolumns qbtc, jsonb_array_elements(qbtc."
				+ inputMap.get("tablecolumnname") + ")" + " with ORDINALITY arr(" + inputMap.get("tablecolumnname")
				+ ") where qbtc.nquerybuildertablecode=" + inputMap.get("nquerybuildertablecode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and" + " arr."
				+ inputMap.get("tablecolumnname") + "->>'columnname'='" + ColumnName + "'";
		return (QueryBuilderTableColumns) jdbcUtilityFunction.queryForObject(QueryBuilderTableColumnsQuery,
				QueryBuilderTableColumns.class, jdbcTemplate);
	}

	@Override
	public DynamicAuditTable getActiveDynamicAuditTableByID(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {

		final String QueryDynamicAuditTable = "select ordinality-1 as index, '" + inputMap.get("subsampleenabledisable")
				+ "' as sfieldname, coalesce(arr.jsondata->'" + inputMap.get("keyname") + "'->>'"
				+ userInfo.getSlanguagetypecode() + "', arr.jsondata->'" + inputMap.get("keyname") + "'"
				+ "->>'en-US') as sdisplayname, arr.jsondata->'" + inputMap.get("keyname")
				+ "'->>'en-US' as sdefaultname, arr.jsondata"
				+ " as sjsondata, dat.* from dynamicaudittable dat, jsonb_array_elements(dat.jsondata->'"
				+ inputMap.get("subsampleenabledisable")
				+ "'->'multilingualfields') with ordinality arr(jsondata) where dat.ndynamicaudittablecode="
				+ inputMap.get("ndynamicaudittablecode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and arr.jsondata='"
				+ stringUtilityFunction.replaceQuote(inputMap.get("conditioncheck").toString()) + "'";
		return (DynamicAuditTable) jdbcUtilityFunction.queryForObject(QueryDynamicAuditTable, DynamicAuditTable.class,
				jdbcTemplate);
	}

	@Override
	public Map<String, Object> getActiveMappedTemplateFieldPropsByID(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		String QueryMappedTemplateFieldProps = "";
		List<String> lstSubKeyValues = Arrays.asList("conditionfields", "selectfields",
				"querybuildertablecolumnsfordynamicview");
		String displayName = "";
		String columnName = "";
		String strValue = "";
		if (inputMap.get("indexFieldValue") != null) {
			if (lstSubKeyValues.contains(inputMap.get("indexFieldValue"))) {
				displayName = "displayname";
				columnName = "columnname";
				strValue = inputMap.get("nformcode") + "'->'" + inputMap.get("indexPropertiesValue") + "'->'"
						+ inputMap.get("indexFieldValue");
			} else {
				displayName = "1";
				columnName = "2";
				strValue = inputMap.get("nformcode") + "'->'" + inputMap.get("indexPropertiesValue") + "'->'"
						+ inputMap.get("indexFieldValue");
			}
		} else if (inputMap.get("nformcode") == null) {
			displayName = "1";
			columnName = "2";
			strValue = "" + inputMap.get("sformname");
		} else {
			displayName = "1";
			columnName = "2";
			strValue = inputMap.get("nformcode") + "'->'" + inputMap.get("indexPropertiesValue");
		}
		QueryMappedTemplateFieldProps = "select ordinality-1 as index, coalesce(arr.jsondata->'" + displayName + "'->>'"
				+ userInfo.getSlanguagetypecode() + "', arr.jsondata->'" + displayName
				+ "'->>'en-US') as sdisplayname, arr.jsondata->'" + displayName
				+ "'->'en-US' as sdefaultname, arr.jsondata as sjsondata," + " arr.jsondata->'" + columnName
				+ "' as scolumnname, mtfp.* from mappedtemplatefieldprops mtfp, jsonb_array_elements(mtfp.jsondata->'"
				+ strValue + "') with ordinality arr(jsondata) where mtfp.nmappedtemplatefieldpropcode="
				+ inputMap.get("nmappedtemplatefieldpropcode") + " and mtfp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ordinality-1="
				+ inputMap.get("index");
		return (Map<String, Object>) jdbcTemplate.queryForList(QueryMappedTemplateFieldProps).get(0);
	}

	private QualisMenu getMenuLanguageByName(String smenuname, UserInfo userInfo) throws Exception {
		final String getQualisMenuType = "select nmenucode from qualismenu" + " where jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "' ='" + stringUtilityFunction.replaceQuote(smenuname) + "'"
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (QualisMenu) jdbcUtilityFunction.queryForObject(getQualisMenuType, QualisMenu.class, jdbcTemplate);
	}

	private QualisModule getModuleLanguageByName(String smodulename, UserInfo userInfo) throws Exception {
		final String getQualisModuleType = "select nmodulecode from qualismodule"
				+ " where jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' ='"
				+ stringUtilityFunction.replaceQuote(smodulename) + "'" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (QualisModule) jdbcUtilityFunction.queryForObject(getQualisModuleType, QualisModule.class, jdbcTemplate);
	}

	private QualisForms getFormLanguageByName(String sformname, UserInfo userInfo) throws Exception {
		final String getQualisFormsType = "select nformcode from qualisforms" + " where jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "' ='" + stringUtilityFunction.replaceQuote(sformname) + "'"
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (QualisForms) jdbcUtilityFunction.queryForObject(getQualisFormsType, QualisForms.class, jdbcTemplate);
	}

	private TransactionStatus getTransactionStatusLanguageByName(String stransstatus, UserInfo userInfo)
			throws Exception {
		final String getTransactionStatusType = "select ntranscode from transactionstatus"
				+ " where jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' ='"
				+ stringUtilityFunction.replaceQuote(stransstatus) + "'" + " and jsondata->'sactiondisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' ='" + stringUtilityFunction.replaceQuote(stransstatus) + "'"
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (TransactionStatus) jdbcUtilityFunction.queryForObject(getTransactionStatusType, TransactionStatus.class,
				jdbcTemplate);
	}

	private ApprovalSubType getApprovalSubTypeLanguageByName(String ssubtype, UserInfo userInfo) throws Exception {
		final String getApprovalSubType = "select napprovalsubtypecode from approvalSubtype"
				+ " where jsondata->'approvalsubtypename'->>'" + userInfo.getSlanguagetypecode() + "' ='"
				+ stringUtilityFunction.replaceQuote(ssubtype) + "'" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (ApprovalSubType) jdbcUtilityFunction.queryForObject(getApprovalSubType, ApprovalSubType.class,
				jdbcTemplate);
	}

	private SampleType getSampleTypeLanguageByName(String ssampletype, UserInfo userInfo) throws Exception {
		final String getSampleType = "select nsampletypecode from sampletype" + " where jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "' ='" + stringUtilityFunction.replaceQuote(ssampletype) + "'"
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (SampleType) jdbcUtilityFunction.queryForObject(getSampleType, SampleType.class, jdbcTemplate);
	}

	private Period getPeriodLanguageByName(String speriodname, UserInfo userInfo) throws Exception {
		final String getGenderType = "select nperiodcode from period" + " where jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "' ='" + stringUtilityFunction.replaceQuote(speriodname) + "'"
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (Period) jdbcUtilityFunction.queryForObject(getGenderType, Period.class, jdbcTemplate);
	}

	private Gender getGenderLanguageByName(String sgendername, UserInfo userInfo) throws Exception {
		final String getGenderType = "select ngendercode from gender" + " where jsondata->'sgendername'->>'"
				+ userInfo.getSlanguagetypecode() + "' ='" + stringUtilityFunction.replaceQuote(sgendername) + "'"
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (Gender) jdbcUtilityFunction.queryForObject(getGenderType, Gender.class, jdbcTemplate);
	}

	private Grade getGradeLanguageByName(String sgradename, UserInfo userInfo) throws Exception {
		final String getGradeType = "select ngradecode from grade" + " where jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "' ='" + stringUtilityFunction.replaceQuote(sgradename) + "'"
				+ " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (Grade) jdbcUtilityFunction.queryForObject(getGradeType, Grade.class, jdbcTemplate);
	}

	private SchedulerType getSchedulerTypeLanguageByName(String sschedulertypename, UserInfo userInfo)
			throws Exception {
		final String getSchedulerType = "select nschedulertypecode from schedulertype"
				+ " where jsondata->'sschedulertypename'->>'" + userInfo.getSlanguagetypecode() + "' ='"
				+ stringUtilityFunction.replaceQuote(sschedulertypename) + "'" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (SchedulerType) jdbcUtilityFunction.queryForObject(getSchedulerType, SchedulerType.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateMenuLanguage(QualisMenu objQualisMenu, UserInfo userInfo) throws Exception {
		final QualisMenu getQualisMenu = getActiveMenuByID(objQualisMenu.getNmenucode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getQualisMenu != null) {
			final Map<String, Object> smenuname = (Map<String, Object>) objQualisMenu.getJsondata().get("sdisplayname");
			final QualisMenu regType = getMenuLanguageByName((String) smenuname.get(userInfo.getSlanguagetypecode()),
					userInfo);
			if (regType == null || regType.getNmenucode() == objQualisMenu.getNmenucode()) {

				final String updateQuery = "update qualismenu set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', jsondata = jsondata||'"
						+ stringUtilityFunction
								.replaceQuote((mapper.writeValueAsString(objQualisMenu.getJsondata())).toString())
						+ "'::jsonb " + " where nmenucode = " + objQualisMenu.getNmenucode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updateQuery);
				return getMenuComboService(userInfo, objQualisMenu.getNmenucode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateModuleLanguage(QualisModule objQualisModule, UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final QualisModule getQualismodule = getActiveModuleByID(objQualisModule.getNmodulecode(), userInfo);
		if (getQualismodule != null) {
			final Map<String, Object> smodulename = (Map<String, Object>) objQualisModule.getJsondata()
					.get("sdisplayname");
			final QualisModule regType = getModuleLanguageByName(
					(String) smodulename.get(userInfo.getSlanguagetypecode()), userInfo);
			if (regType == null || regType.getNmodulecode() == objQualisModule.getNmodulecode()) {
				final String updateQuery = "update qualismodule set jsondata = jsondata||'"
						+ stringUtilityFunction
								.replaceQuote((mapper.writeValueAsString(objQualisModule.getJsondata())).toString())
						+ "'::jsonb " + " where nmodulecode = " + objQualisModule.getNmodulecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updateQuery);
				return getModuleComboService(userInfo, objQualisModule.getNmodulecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateFormLanguage(QualisForms objQualisForms, UserInfo userInfo) throws Exception {
		final QualisForms getQualisForms = getActiveFormByID(objQualisForms.getNformcode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getQualisForms != null) {
			final Map<String, Object> sformname = (Map<String, Object>) objQualisForms.getJsondata()
					.get("sdisplayname");
			final QualisForms regType = getFormLanguageByName((String) sformname.get(userInfo.getSlanguagetypecode()),
					userInfo);
			if (regType == null || regType.getNformcode() == objQualisForms.getNformcode()) {
				final String updateQuery = "update qualisforms set jsondata = jsondata||'"
						+ stringUtilityFunction
								.replaceQuote((mapper.writeValueAsString(objQualisForms.getJsondata())).toString())
						+ "'::jsonb, dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nformcode = " + objQualisForms.getNformcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updateQuery);
				return getFormComboService(userInfo, objQualisForms.getNformcode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateTransactionStatusLanguage(TransactionStatus objTransactionStatus,
			UserInfo userInfo) throws Exception {
		final TransactionStatus getTransactionStatus = getActiveTransactionStatusByID(
				objTransactionStatus.getNtranscode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getTransactionStatus != null) {
			final Map<String, Object> stransstatus = (Map<String, Object>) objTransactionStatus.getJsondata();
			Map<String, Object> stransdisplaystatus = (Map<String, Object>) stransstatus.get("stransdisplaystatus");
			Map<String, Object> sactiondisplaystatus = (Map<String, Object>) stransstatus.get("sactiondisplaystatus");
			final TransactionStatus regType1 = getTransactionStatusLanguageByName(
					(String) stransdisplaystatus.get(userInfo.getSlanguagetypecode()), userInfo);
			final TransactionStatus regType = getTransactionStatusLanguageByName(
					(String) sactiondisplaystatus.get(userInfo.getSlanguagetypecode()), userInfo);
			if ((regType == null || regType1 == null)
					|| (regType.getNtranscode() == objTransactionStatus.getNtranscode()
							|| regType1.getNtranscode() == objTransactionStatus.getNtranscode())) {
				final String updateQuery = "update transactionstatus set jsondata = jsondata||'"
						+ stringUtilityFunction.replaceQuote(
								(mapper.writeValueAsString(objTransactionStatus.getJsondata())).toString())
						+ "'::jsonb, dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ntranscode = " + objTransactionStatus.getNtranscode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updateQuery);
				return getTransactionStatusComboService(userInfo, objTransactionStatus.getNtranscode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateControlMasterLanguage(ControlMaster objControlMaster, UserInfo userInfo)
			throws Exception {
		final ControlMaster getControlMaster = getActiveControlMasterByID(objControlMaster.getNcontrolcode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getControlMaster != null) {
			final String updateQuery = "update controlmaster set jsondata = jsondata||'"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(objControlMaster.getJsondata())).toString())
					+ "'::jsonb, dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' where ncontrolcode = " + objControlMaster.getNcontrolcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getControlMasterComboService(userInfo, objControlMaster.getNcontrolcode());
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateApprovalSubTypeLanguage(ApprovalSubType objapprovalsubtype, UserInfo userInfo)
			throws Exception {
		final ApprovalSubType getApprovalSubType = getActiveApprovalSubTypeByID(
				objapprovalsubtype.getNapprovalsubtypecode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getApprovalSubType != null) {
			final Map<String, Object> ssubtypename = (Map<String, Object>) objapprovalsubtype.getJsondata()
					.get("approvalsubtypename");
			final ApprovalSubType regType = getApprovalSubTypeLanguageByName(
					(String) ssubtypename.get(userInfo.getSlanguagetypecode()), userInfo);
			if (regType == null || regType.getNapprovalsubtypecode() == objapprovalsubtype.getNapprovalsubtypecode()) {
				final String updateQuery = "update approvalSubtype set jsondata = jsondata||'"
						+ stringUtilityFunction
								.replaceQuote((mapper.writeValueAsString(objapprovalsubtype.getJsondata())).toString())
						+ "'::jsonb " + " where napprovalsubtypecode = " + objapprovalsubtype.getNapprovalsubtypecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updateQuery);
				return getApprovalSubTypeComboService(userInfo, objapprovalsubtype.getNapprovalsubtypecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateSampleTypeLanguage(SampleType objSampleType, UserInfo userInfo)
			throws Exception {
		final SampleType getSampleType = getActiveSampleTypeByID(objSampleType.getNsampletypecode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getSampleType != null) {
			final Map<String, Object> ssampletypename = (Map<String, Object>) objSampleType.getJsondata()
					.get("sampletypename");
			final SampleType regType = getSampleTypeLanguageByName(
					(String) ssampletypename.get(userInfo.getSlanguagetypecode()), userInfo);
			if (regType == null || regType.getNsampletypecode() == objSampleType.getNsampletypecode()) {
				final String updateQuery = "update sampletype set jsondata = jsondata||'"
						+ stringUtilityFunction
								.replaceQuote((mapper.writeValueAsString(objSampleType.getJsondata())).toString())
						+ "'::jsonb " + ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nsampletypecode = " + objSampleType.getNsampletypecode() + " and nstatus in(-2, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				jdbcTemplate.execute(updateQuery);
				return getSampleTypeComboService(userInfo, objSampleType.getNsampletypecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updatePeriodLanguage(Period period, UserInfo userInfo) throws Exception {
		final Period getPeriod = getActivePeriodByID(period.getNperiodcode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getPeriod != null) {
			final Map<String, Object> smenuname = (Map<String, Object>) period.getJsondata().get("speriodname");
			final Period regType = getPeriodLanguageByName((String) smenuname.get(userInfo.getSlanguagetypecode()),
					userInfo);
			if (regType == null || regType.getNperiodcode() == period.getNperiodcode()) {
				final String updateQuery = "update period set jsondata = jsondata||'"
						+ stringUtilityFunction
								.replaceQuote((mapper.writeValueAsString(period.getJsondata())).toString())
						+ "'::jsonb " + " where nperiodcode = " + period.getNperiodcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updateQuery);
				return getPeriodComboService(userInfo, period.getNperiodcode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateGenderLanguage(Gender objGender, UserInfo userInfo) throws Exception {
		final Gender getGender = getActiveGenderByID(objGender.getNgendercode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getGender != null) {
			final Map<String, Object> sgendername = (Map<String, Object>) objGender.getJsondata().get("sgendername");
			final Gender regType = getGenderLanguageByName((String) sgendername.get(userInfo.getSlanguagetypecode()),
					userInfo);
			if (regType == null || regType.getNgendercode() == objGender.getNgendercode()) {
				final String updateQuery = "update gender set jsondata = jsondata||'"
						+ stringUtilityFunction
								.replaceQuote((mapper.writeValueAsString(objGender.getJsondata())).toString())
						+ "'::jsonb " + " where ngendercode = " + objGender.getNgendercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updateQuery);
				return getGenderComboService(userInfo, objGender.getNgendercode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateGradeLanguage(Grade objGrade, UserInfo userInfo) throws Exception {
		final Grade getGrade = getActiveGradeByID(objGrade.getNgradecode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getGrade != null) {
			final Map<String, Object> sgradename = (Map<String, Object>) objGrade.getJsondata().get("sdisplayname");
			final Grade regType = getGradeLanguageByName((String) sgradename.get(userInfo.getSlanguagetypecode()),
					userInfo);
			if (regType == null || regType.getNgradecode() == objGrade.getNgradecode()) {
				final String updateQuery = "update grade set jsondata = jsondata||'"
						+ stringUtilityFunction
								.replaceQuote((mapper.writeValueAsString(objGrade.getJsondata())).toString())
						+ "'::jsonb " + " where ngradecode = " + objGrade.getNgradecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updateQuery);
				return getGradeComboService(userInfo, objGrade.getNgradecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateSchedulerTypeLanguage(SchedulerType objSchedulerType, UserInfo userInfo)
			throws Exception {
		final SchedulerType getSchedulerType = getActiveSchedulerTypeByID(objSchedulerType.getNschedulertypecode(),
				userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getSchedulerType != null) {
			final Map<String, Object> sschedulertypename = (Map<String, Object>) objSchedulerType.getJsondata()
					.get("sschedulertypename");
			final SchedulerType regType = getSchedulerTypeLanguageByName(
					(String) sschedulertypename.get(userInfo.getSlanguagetypecode()), userInfo);
			if (regType == null || regType.getNschedulertypecode() == objSchedulerType.getNschedulertypecode()) {
				final String updateQuery = "update schedulertype set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', jsondata = jsondata||'"
						+ stringUtilityFunction
								.replaceQuote((mapper.writeValueAsString(objSchedulerType.getJsondata())).toString())
						+ "'::jsonb " + " where nschedulertypecode = " + objSchedulerType.getNschedulertypecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updateQuery);
				return getSchedulerTypeComboService(userInfo, objSchedulerType.getNschedulertypecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateQueryBuilderTablesLanguage(QueryBuilderTables objQueryBuilderTables,
			UserInfo userInfo) throws Exception {
		final QueryBuilderTables getSqlQueryBuilder = getActiveQueryBuilderTablesByID(
				objQueryBuilderTables.getNquerybuildertablecode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getSqlQueryBuilder != null) {
			final String updateQuery = "update querybuildertables set jsondata = jsondata||'"
					+ stringUtilityFunction.replaceQuote(
							(mapper.writeValueAsString(objQueryBuilderTables.getJsondata())).toString())
					+ "'::jsonb " + " where nquerybuildertablecode = "
					+ objQueryBuilderTables.getNquerybuildertablecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getQueryBuilderTablesComboService(userInfo, objQueryBuilderTables.getNquerybuildertablecode());
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateQueryBuilderViewsLanguage(QueryBuilderViews queryBuilderViews,
			UserInfo userInfo) throws Exception {
		final QueryBuilderViews getViewsQueryBuilder = getActiveQueryBuilderViewsByID(
				queryBuilderViews.getNquerybuilderviewscode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getViewsQueryBuilder != null) {
			final String updateQuery = "update querybuilderviews set dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,  jsondata = jsondata||'"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(queryBuilderViews.getJsondata())).toString())
					+ "'::jsonb " + " where nquerybuilderviewscode = " + queryBuilderViews.getNquerybuilderviewscode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getQueryBuilderViewsComboService(userInfo, queryBuilderViews.getNquerybuilderviewscode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateMultilingualMastersLanguage(MultilingualMasters objMultilingualMasters,
			UserInfo userInfo) throws Exception {
		final MultilingualMasters getMultilingualMasters = getActiveMultilingualMastersByID(
				objMultilingualMasters.getNmultilingualmasterscode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getMultilingualMasters != null) {

			final String updateQuery = "update multilingualmasters set jsondata = jsondata||'"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(objMultilingualMasters.getJsondata())).toString())
					+ "'::jsonb, dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' where nmultilingualmasterscode = " + objMultilingualMasters.getNmultilingualmasterscode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getMultilingualMastersComboService(userInfo, objMultilingualMasters.getNmultilingualmasterscode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateQueryBuilderViewsColumnsLanguage(
			QueryBuilderViewsColumns queryBuilderViewsColumns, UserInfo userInfo, int nindexoff, String keyvalue)
			throws Exception {
		final QueryBuilderViewsColumns getqueryBuilderViewsColumns = getActiveQueryBuilderViewsColumnsByID(nindexoff,
				queryBuilderViewsColumns.getSviewname(), userInfo, keyvalue);
		final ObjectMapper mapper = new ObjectMapper();
		if (getqueryBuilderViewsColumns != null) {
			final String updateQuery = "update querybuilderviewscolumns set dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' , jsondata = '"
					+ stringUtilityFunction.replaceQuote(
							(mapper.writeValueAsString(queryBuilderViewsColumns.getSjsondata())).toString())
					+ "'::jsonb " + " where sviewname = '" + queryBuilderViewsColumns.getSviewname()
					+ "' and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getQueryBuilderViewsColumnsComboService(userInfo, queryBuilderViewsColumns.getSviewname());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateMaterialTypeLanguage(MaterialType materialtype, UserInfo userInfo)
			throws Exception {
		final MaterialType getMaterialTYpe = getActiveMaterialTypeByID(materialtype.getNmaterialtypecode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getMaterialTYpe != null) {

			final String updateQuery = "update materialtype set jsondata = jsondata||'"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(materialtype.getJsondata())).toString())
					+ "'::jsonb " + ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' where nmaterialtypecode= " + materialtype.getNmaterialtypecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getMaterialTypeComboService(userInfo, materialtype.getNmaterialtypecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateInterfaceTypeLanguage(InterfaceType objInterfaceType, UserInfo userInfo)
			throws Exception {
		final InterfaceType getInterfaceType = getActiveInterfaceTypeByID(objInterfaceType.getNinterfacetypecode(),
				userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getInterfaceType != null) {

			final String updateQuery = "update interfacetype set jsondata = jsondata||'"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(objInterfaceType.getJsondata())).toString())
					+ "'::jsonb " + ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' where ninterfacetypecode= " + objInterfaceType.getNinterfacetypecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getInterfaceTypeComboService(userInfo, objInterfaceType.getNinterfacetypecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateAuditActionFilterLanguage(AuditActionFilter auditactionfilter,
			UserInfo userInfo) throws Exception {
		final AuditActionFilter getauditactionfilter = getActiveAuditActionFilterByID(
				auditactionfilter.getNauditactionfiltercode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getauditactionfilter != null) {

			final String updateQuery = "update auditactionfilter set jsondata = jsondata||'"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(auditactionfilter.getJsondata())).toString())
					+ "'::jsonb, dauditdate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' where nauditactionfiltercode= " + auditactionfilter.getNauditactionfiltercode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getAuditActionFilterComboService(userInfo, auditactionfilter.getNauditactionfiltercode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateAttachmentTypeLanguage(MultilingualMasters attachmentType, UserInfo userInfo)
			throws Exception {
		final MultilingualMasters getAttachmentType = getActiveAttachmentTypeByID(
				attachmentType.getNattachmenttypecode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getAttachmentType != null) {

			final String updateQuery = "update attachmenttype set jsondata = jsondata||'"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(attachmentType.getJsondata())).toString())
					+ "'::jsonb " + " where nattachmenttypecode= " + attachmentType.getNattachmenttypecode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getAttachmenttypeComboService(userInfo, attachmentType.getNattachmenttypecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateFTPTypeLanguage(MultilingualMasters FTPType, UserInfo userInfo)
			throws Exception {
		final MultilingualMasters getFTPType = getActiveFTPTypeByID(FTPType.getNftptypecode(), userInfo);

		final ObjectMapper mapper = new ObjectMapper();
		if (getFTPType != null) {

			final String updateQuery = "update ftptype set jsondata = jsondata||'"
					+ stringUtilityFunction.replaceQuote((mapper.writeValueAsString(FTPType.getJsondata())).toString())
					+ "'::jsonb, dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' where nftptypecode= " + FTPType.getNftptypecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getFTPtypeComboService(userInfo, FTPType.getNftptypecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateReportTypeLanguage(ReportType reportType, UserInfo userInfo) throws Exception {
		final ReportType getReportType = getActiveReportTypeByID(reportType.getNreporttypecode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getReportType != null) {

			final String updateQuery = "update reporttype set jsondata = jsondata||'"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(reportType.getJsondata())).toString())
					+ "'::jsonb, dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' where nreporttypecode= " + reportType.getNreporttypecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getReportTypeComboService(userInfo, reportType.getNreporttypecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateCOAReportTypeLanguage(COAReportType COAReportType, UserInfo userInfo)
			throws Exception {
		final COAReportType getCOAReportType = getActiveCOAReportTypeByID(COAReportType.getNcoareporttypecode(),
				userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getCOAReportType != null) {

			final String updateQuery = "update coareporttype set jsondata = jsondata||'"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(COAReportType.getJsondata())).toString())
					+ "'::jsonb, dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' where ncoareporttypecode= " + COAReportType.getNcoareporttypecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getCOAReportTypeComboService(userInfo, COAReportType.getNcoareporttypecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateReactComponentsLanguage(ReactComponents reactComponents, UserInfo userInfo)
			throws Exception {
		final ReactComponents getreactComponents = getActiveReactComponentByID(reactComponents.getNreactcomponentcode(),
				userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getreactComponents != null) {

			final String updateQuery = "update reactcomponents set jsondata = jsondata||'"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(reactComponents.getJsondata())).toString())
					+ "'::jsonb " + " where nreactcomponentcode= " + reactComponents.getNreactcomponentcode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getReactComponentsComboService(userInfo, reactComponents.getNreactcomponentcode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateFunctionsLanguage(Functions functions, UserInfo userInfo) throws Exception {
		final Functions getFunctions = getActiveFunctionsByID(functions.getNfunctioncode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getFunctions != null) {

			final String updateQuery = "update functions set jsondata = '"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(functions.getJsondata())).toString())
					+ "'::jsonb " + " where nfunctioncode= " + functions.getNfunctioncode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getFunctionsComboService(userInfo, functions.getNfunctioncode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateDynamicFormulaFieldLanguage(DynamicFormulaFields dynamicFormulaFields,
			UserInfo userInfo) throws Exception {
		final DynamicFormulaFields getDynamicFormulFields = getActiveDynamicFormulaFieldByID(
				dynamicFormulaFields.getNdynamicformulafieldcode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getDynamicFormulFields != null) {

			final String updateQuery = "update dynamicformulafields set jsondata = '"
					+ stringUtilityFunction.replaceQuote(
							(mapper.writeValueAsString(dynamicFormulaFields.getJsondata())).toString())
					+ "'::jsonb " + " where ndynamicformulafieldcode= "
					+ dynamicFormulaFields.getNdynamicformulafieldcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getDynamicFormulaFieldsComboService(userInfo, dynamicFormulaFields.getNdynamicformulafieldcode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateChartTypeLanguage(ChartType chartType, UserInfo userInfo) throws Exception {
		final ChartType getChartType = getActiveChartTypeByID(chartType.getNcharttypecode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getChartType != null) {

			final String updateQuery = "update charttype set jsondata = '"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(chartType.getJsondata())).toString())
					+ "'::jsonb " + " where ncharttypecode= " + chartType.getNcharttypecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getChartTypeComboService(userInfo, chartType.getNcharttypecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateDesignComponentLanguage(DesignComponents designComponent, UserInfo userInfo)
			throws Exception {
		final DesignComponents getDesignComponents = getActiveDesignComponentByID(
				designComponent.getNdesigncomponentcode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getDesignComponents != null) {

			final String updateQuery = "update designcomponents set jsondata = '"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(designComponent.getJsondata())).toString())
					+ "'::jsonb " + " where ndesigncomponentcode= " + designComponent.getNdesigncomponentcode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getDesignComponentsComboService(userInfo, designComponent.getNdesigncomponentcode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateCheckListComponentLanguage(ChecklistComponent checklistComponents,
			UserInfo userInfo) throws Exception {
		final ChecklistComponent getChecklistComponent = getActiveCheckListComponentByID(
				checklistComponents.getNchecklistcomponentcode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getChecklistComponent != null) {

			final String updateQuery = "update checklistcomponent set jsondata = '"
					+ stringUtilityFunction.replaceQuote(
							(mapper.writeValueAsString(checklistComponents.getJsondata())).toString())
					+ "'::jsonb " + " where nchecklistcomponentcode= "
					+ checklistComponents.getNchecklistcomponentcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getCheckListComponentService(userInfo, checklistComponents.getNchecklistcomponentcode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateGenericLabelLanguage(GenericLabel genericLabels, UserInfo userInfo)
			throws Exception {
		final GenericLabel getGenericLabel = getActiveGenericLabelByID(genericLabels.getNgenericlabelcode(), userInfo);
		final ObjectMapper mapper = new ObjectMapper();
		if (getGenericLabel != null) {

			final String updateQuery = "update genericlabel set jsondata = '"
					+ stringUtilityFunction
							.replaceQuote((mapper.writeValueAsString(genericLabels.getJsondata())).toString())
					+ "'::jsonb " + " where ngenericlabelcode= " + genericLabels.getNgenericlabelcode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
			return getGenericLabelService(userInfo, genericLabels.getNgenericlabelcode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateQueryBuilderTableColumnsLanguage(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {

		Map<String, Object> queryBuilderTableColumns = (Map<String, Object>) inputMap.get("language");

		final QueryBuilderTableColumns getQueryBuilderTableColumns = getActiveQueryBuilderTableColumnsByID(userInfo,
				queryBuilderTableColumns);
		if (getQueryBuilderTableColumns != null) {

			JSONArray arrayColumnData = new JSONArray(queryBuilderTableColumns.get("jsondata").toString().trim());
			final String updateQuery = "update querybuildertablecolumns set "
					+ queryBuilderTableColumns.get("tablecolumnname") + "='"
					+ stringUtilityFunction.replaceQuote(arrayColumnData.toString()) + "' where nquerybuildertablecode="
					+ queryBuilderTableColumns.get("nquerybuildertablecode") + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updateQuery);
			return getQueryBuilderTableColumnsService(userInfo, queryBuilderTableColumns);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateDynamicAuditTableLanguage(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> dynamicAuditTable = (Map<String, Object>) inputMap.get("language");
		final DynamicAuditTable getDynamicAuditTable = getActiveDynamicAuditTableByID(userInfo, dynamicAuditTable);
		if (getDynamicAuditTable != null) {
			final String updateQuery = "update dynamicaudittable set jsondata='"
					+ stringUtilityFunction.replaceQuote(dynamicAuditTable.get("jsondata").toString()) + "' where"
					+ " ndynamicaudittablecode=" + dynamicAuditTable.get("ndynamicaudittablecode") + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			jdbcTemplate.execute(updateQuery);
			return getDynamicAuditTableService(userInfo, dynamicAuditTable);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateMappedTemplateFieldPropsLanguage(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		Map<String, Object> mappedTemplateFieldProps = (Map<String, Object>) inputMap.get("language");
		final Map<String, Object> getMappedTemplateFieldProps = getActiveMappedTemplateFieldPropsByID(userInfo,
				mappedTemplateFieldProps);
		if (getMappedTemplateFieldProps != null) {
			final String updateQuery = "update mappedtemplatefieldprops set jsondata='"
					+ stringUtilityFunction.replaceQuote(mappedTemplateFieldProps.get("jsondata").toString())
					+ "' where" + " nmappedtemplatefieldpropcode="
					+ mappedTemplateFieldProps.get("nmappedtemplatefieldpropcode") + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			jdbcTemplate.execute(updateQuery);
			return getMappedTemplateFieldPropsService(userInfo, mappedTemplateFieldProps);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getMenuComboService(UserInfo userInfo, int nmenucode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String menuquery = "select q.*,coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',"
				+ "q.jsondata->'sdisplayname'->>'en-US') as sdisplayname , q.smenuname from qualismenu q where q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and  q.nmenucode > 0 order by nmenucode";
		List<QualisMenu> lstmenuquery = (List<QualisMenu>) jdbcTemplate.query(menuquery, new QualisMenu());
		outputMap.put("listofItem", lstmenuquery);
		outputMap.put("headername", "Menu");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getModuleComboService(UserInfo userInfo, int nmodulecode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String modulequery = "select qm.*,coalesce(qm.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', qm.jsondata->'sdisplayname'->>'en-US') as sdisplayname "
				+ ",qm.smodulename from qualismodule qm where qm.nmodulecode > 0  and qm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nmodulecode  ";
		List<QualisModule> lstmodulequery = (List<QualisModule>) jdbcTemplate.query(modulequery, new QualisModule());
		outputMap.put("listofItem", lstmodulequery);
		outputMap.put("headername", "Module");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getFormComboService(UserInfo userInfo, int nformcode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String formquery = "select qf.*,coalesce(qf.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', qf.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ "qf.sformname from qualisforms qf where   " + " qf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nformcode";
		List<QualisForms> lstformquery = (List<QualisForms>) jdbcTemplate.query(formquery, new QualisForms());
		outputMap.put("listofItem", lstformquery);
		outputMap.put("headername", "Forms");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTransactionStatusComboService(UserInfo userInfo, int ntranscode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String transstatusquery = "select ts.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,"
				+ "coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'sactiondisplaystatus'->>'en-US') as sactiondisplaystatus,"
				+ "ts.stransstatus from transactionstatus ts where ts.ntranscode > 0  and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ntranscode";
		List<TransactionStatus> lsttransstatusquery = (List<TransactionStatus>) jdbcTemplate.query(transstatusquery,
				new TransactionStatus());
		outputMap.put("listofItem", lsttransstatusquery);
		outputMap.put("headername", "Transaction Status");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getControlMasterComboService(UserInfo userInfo, int ncontrolcode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String controlmasterquery = "select cm.*,coalesce(cm.jsondata->'scontrolids'->>'"
				+ userInfo.getSlanguagetypecode() + "', cm.jsondata->'scontrolids'->>'en-US') as sdisplayname, "
				+ "cm.scontrolname, coalesce(qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "', qf.jsondata->'sdisplayname'->>'en-US') as sformname from controlmaster cm, qualisforms qf where cm.ncontrolcode > 0 and cm.nformcode=qf.nformcode and qf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ncontrolcode";
		List<ControlMaster> lstcontrolmasterquery = (List<ControlMaster>) jdbcTemplate.query(controlmasterquery,
				new ControlMaster());
		outputMap.put("listofItem", lstcontrolmasterquery);
		outputMap.put("headername", "Control Master");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getApprovalSubTypeComboService(UserInfo userInfo, int napprovalsubtypecode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String AppSubTypequery = "select ast.*,coalesce(ast.jsondata->'approvalsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ast.jsondata->'approvalsubtypename'->>'en-US') as sapprovalsubtypename, "
				+ "ast.jsondata->'approvalsubtypename'->>'en-US' as sdisplayname "
				+ " from approvalsubtype ast where ast.napprovalsubtypecode > 0   " + "   and ast.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by napprovalsubtypecode";
		List<ApprovalSubType> lstAppSubTypequery = (List<ApprovalSubType>) jdbcTemplate.query(AppSubTypequery,
				new ApprovalSubType());
		outputMap.put("listofItem", lstAppSubTypequery);
		outputMap.put("headername", "Approval Sub Type");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSampleTypeComboService(UserInfo userInfo, int nsampletypecode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String sampleTypequery = "select st.*,coalesce(st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "', st.jsondata->'sampletypename'->>'en-US') as ssampletypename, "
				+ " st.jsondata->'sampletypename'->>'en-US' as sdisplayname "
				+ " from sampletype st where st.nsampletypecode > 0   " + "   and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsampletypecode  ";
		List<SampleType> lstsampleTypequery = (List<SampleType>) jdbcTemplate.query(sampleTypequery, new SampleType());
		outputMap.put("listofItem", lstsampleTypequery);
		outputMap.put("headername", "Sample Type");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getPeriodComboService(UserInfo userInfo, int nperiodcode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String periodquery = "select p.nperiodcode,p.nsitecode,p.nstatus,coalesce(p.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "', p.jsondata->'speriodname'->>'en-US') as speriodname, "
				+ " p.jsondata->'speriodname'->>'en-US' as sdisplayname " + " from period p where p.nperiodcode > 0   "
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nperiodcode";
		List<Period> lstperiodquery = (List<Period>) jdbcTemplate.query(periodquery, new Period());
		outputMap.put("listofItem", lstperiodquery);
		outputMap.put("headername", "Period");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getGenderComboService(UserInfo userInfo, int ngendercode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String genderquery = "select g.*,coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
				+ "', g.jsondata->'sgendername'->>'en-US') as sgendername, "
				+ " g.jsondata->'sgendername'->>'en-US' as sdisplayname " + " from gender g where g.ngendercode > 0   "
				+ "  and g.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by ngendercode  ";
		List<Gender> lstgenderquery = (List<Gender>) jdbcTemplate.query(genderquery, new Gender());
		outputMap.put("listofItem", lstgenderquery);
		outputMap.put("headername", "Gender");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getGradeComboService(UserInfo userInfo, int ngradecode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String gradequery = "select gr.*,coalesce(gr.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', gr.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ "gr.sgradename  from grade gr where gr.ngradecode > 0   " + "   and gr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ngradecode  ";
		List<Grade> lstgradequery = (List<Grade>) jdbcTemplate.query(gradequery, new Grade());
		outputMap.put("listofItem", lstgradequery);
		outputMap.put("headername", "Grade");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSchedulerTypeComboService(UserInfo userInfo, int nschedulertypecode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String schedulerquery = "select st.*,coalesce(st.jsondata->'sschedulertypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', st.jsondata->'sschedulertypename'->>'en-US') as sschedulertypename, "
				+ " st.jsondata->'sschedulertypename'->>'en-US' as sdisplayname "
				+ " from schedulertype st where st.nschedulertypecode > 0   " + "   and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nschedulertypecode  ";
		List<SchedulerType> lstschedulerquery = (List<SchedulerType>) jdbcTemplate.query(schedulerquery,
				new SchedulerType());
		outputMap.put("listofItem", lstschedulerquery);
		outputMap.put("headername", "Scheduler Type");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMultilingualMastersComboService(UserInfo userInfo, int nmultilingualmasterscode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String multilingualMastersQuery = "select mm.nmultilingualmasterscode,mm.jsondata,mm.nsorter,mm.nsitecode,mm.nstatus,mm.smultilingualmastername as sdisplayname,coalesce(mm.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', mm.jsondata->'sdisplayname'->>'en-US') as sdefaultname "
				+ " from multilingualmasters mm where mm.nmultilingualmasterscode > 0   " + "   and mm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nmultilingualmasterscode";
		List<MultilingualMasters> lstmultilingualMastersQuery = (List<MultilingualMasters>) jdbcTemplate
				.query(multilingualMastersQuery, new MultilingualMasters());
		outputMap.put("listofItem", lstmultilingualMastersQuery);
		outputMap.put("headername", "Multilingual Masters");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getQueryBuilderTablesComboService(UserInfo userInfo, int nquerybuildertablecode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String queryBuilderTablesQuery = "select qbs.*,qf.sformname,coalesce(qbs.jsondata->'tablename'->>'"
				+ userInfo.getSlanguagetypecode() + "', qbs.jsondata->'tablename'->>'en-US') as sdisplayname, "
				+ " qbs.stablename from querybuildertables qbs ,qualisforms qf where qbs.nquerybuildertablecode > 0   "
				+ " and qbs.nformcode=qf.nformcode  and qbs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nquerybuildertablecode";
		List<QueryBuilderTables> lstqueryBuilderTablesQuery = (List<QueryBuilderTables>) jdbcTemplate
				.query(queryBuilderTablesQuery, new QueryBuilderTables());
		outputMap.put("listofItem", lstqueryBuilderTablesQuery);
		outputMap.put("headername", "Query Builder Tables");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getQueryBuilderViewsComboService(UserInfo userInfo, int nquerybuilderviewscode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String queryBuilderViewsQuery = "select qbv.*,coalesce(qbv.jsondata->'displayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', qbv.jsondata->'displayname'->>'en-US') as sdisplayname, "
				+ " qbv.sviewname from querybuilderviews qbv where qbv.nquerybuilderviewscode > 0   "
				+ " and qbv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nquerybuilderviewscode";
		List<QueryBuilderViews> lstqueryBuilderViewsQuery = (List<QueryBuilderViews>) jdbcTemplate
				.query(queryBuilderViewsQuery, new QueryBuilderViews());
		outputMap.put("listofItem", lstqueryBuilderViewsQuery);
		outputMap.put("headername", "Query Builder Views");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@SuppressWarnings("unused")
	private MultilingualMasters getMultilingual(UserInfo userInfo, String sviewname) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String querymultilingual = "select nmultilingualmasterscode, smultilingualmastername,"
				+ " smultilingualmastername as sdisplayname ,jsondata->>'needheader' as sneedheader "
				+ " from multilingualmasters where smultilingualmastername='" + sviewname + "'" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (MultilingualMasters) jdbcUtilityFunction.queryForObject(querymultilingual, MultilingualMasters.class,
				jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getQueryBuilderViewsColumnsComboService(UserInfo userInfo, String sviewname)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String filterquery = "select  q.* ,coalesce(q.jsondata->'displayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', q.jsondata->'displayname'->>'en-US') as sdisplayname"
				+ " from querybuilderviews q  where nquerybuilderviewscode > 0 " + "   and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nquerybuilderviewscode desc";
		List<QueryBuilderViews> lstfilterquery = (List<QueryBuilderViews>) jdbcTemplate.query(filterquery,
				new QueryBuilderViews());

		if (sviewname.equals("Query Builder Views Columns")) {
			sviewname = lstfilterquery.get(0).getSviewname();
		}
		final String queryBuilderViewsColumnsQuery = ""
				+ " select b.* from (select jsonb_object_keys(jsondata) as keys, sviewname,case when (arr.jsonbc->'displayname'->>'"
				+ userInfo.getSlanguagetypecode() + "' is null or arr.jsonbc->'displayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "'='') then arr.jsonbc->'displayname'->>'en-US' else arr.jsonbc->'displayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "' end as sdisplayname,jsondata as jsondata, arr.jsonbc->'displayname'->>'en-US' as sdefaultname"
				+ " from (select nstatus,sviewname,jsondata, jsondata->'selectfields' as jsonbc from querybuilderviewscolumns)a ,"
				+ " jsonb_array_elements(a.jsonbc) with ordinality arr(jsonbc)  where  nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and sviewname='" + sviewname + "')b"
				+ "  where  b.keys = 'selectfields' union all"
				+ " select b.* from (select jsonb_object_keys(jsondata) as keys, sviewname,case when (arr.jsonbc->'displayname'->>'"
				+ userInfo.getSlanguagetypecode() + "' is null or arr.jsonbc->'displayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "'='') then arr.jsonbc->'displayname'->>'en-US' else arr.jsonbc->'displayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "' end as sdisplayname,jsondata as jsondata,  arr.jsonbc->'displayname'->>'en-US' as sdefaultname "
				+ " from (select nstatus,sviewname,jsondata,  jsondata->'conditionfields' as jsonbc from querybuilderviewscolumns)a ,"
				+ " jsonb_array_elements(a.jsonbc) with ordinality arr(jsonbc)  where  nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sviewname='" + sviewname + "')b  "
				+ " where  b.keys = 'conditionfields'";
		List<QueryBuilderViewsColumns> lstqueryBuilderViewsColumnsQuery = (List<QueryBuilderViewsColumns>) jdbcTemplate
				.query(queryBuilderViewsColumnsQuery, new QueryBuilderViewsColumns());
		for (int i = 0; i < lstqueryBuilderViewsColumnsQuery.size(); i++) {
			lstqueryBuilderViewsColumnsQuery.get(i).setIndex(i);
		}
		outputMap.put("headername", "ColumnsFilter");
		outputMap.put("QueryBuilderViewsName", lstfilterquery);
		outputMap.put("selectedQueryBuilderViewsName", lstfilterquery.get(0));
		outputMap.put("listofItem", lstqueryBuilderViewsColumnsQuery);
		outputMap.put("headername", "Query Builder Views Columns");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	private ResponseEntity<Object> getFormName(UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String getFormName = "select  qf.sformname,mc.nformcode "
				+ " from materialconfig mc,qualisforms qf where qf.nformcode=mc.nformcode and mc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by qf.sformname,mc.nformcode";
		List<MaterialConfig> lstGetFormName = (List<MaterialConfig>) jdbcTemplate.query(getFormName,
				new MaterialConfig());
		outputMap.put("MaterialConfig", lstGetFormName);
		outputMap.put("selectedMaterialConfig", lstGetFormName.get(0));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterialConfigComboService(UserInfo userInfo, int nmaterialconfigcode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ResponseEntity<Object> getFormName = getFormName(userInfo);
		outputMap.put("needHeader1", getFormName);
		final String materialConfigQuery = "select mc.*,mc.nsorter,jsondata as sdisplayname "
				+ "  from materialconfig mc where mc.nmaterialconfigcode > 0   " + "  and mc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter asc";
		List<MaterialConfig> lstmaterialConfigQuery = (List<MaterialConfig>) jdbcTemplate.query(materialConfigQuery,
				new MaterialConfig());
		outputMap.put("listofItem", lstmaterialConfigQuery);
		outputMap.put("headername", "Material Config");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterialTypeComboService(UserInfo userInfo, int nmaterialtypecode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String materialTypequery = "select mt.*,coalesce(mt.jsondata->'smaterialtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', mt.jsondata->'smaterialtypename'->>'en-US') as smaterialtypename, "
				+ "mt.jsondata->'smaterialtypename'->>'en-US' as sdisplayname  from materialtype mt where mt.nmaterialtypecode > 0   "
				+ "   and mt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nmaterialtypecode";
		List<MaterialType> lstmaterialTypequery = (List<MaterialType>) jdbcTemplate.query(materialTypequery,
				new MaterialType());
		outputMap.put("listofItem", lstmaterialTypequery);
		outputMap.put("headername", "Material Type");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInterfaceTypeComboService(UserInfo userInfo, int ninterfacetypecode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String interfaceTypequery = "select mt.*,coalesce(mt.jsondata->'sinterfacetypename'->>'"
				+ userInfo.getSlanguagetypecode() + "', mt.jsondata->'sinterfacetypename'->>'en-US') as sdisplayname, "
				+ "mt.jsondata->'sinterfacetypename'->>'en-US' as sinterfacetypename from interfacetype mt where mt.ninterfacetypecode > -1   "
				+ "   and mt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " order by ninterfacetypecode";
		List<InterfaceType> lstinterfaceTypequery = (List<InterfaceType>) jdbcTemplate.query(interfaceTypequery,
				new InterfaceType());
		outputMap.put("listofItem", lstinterfaceTypequery);
		outputMap.put("headername", "Interface Type");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getAuditActionFilterComboService(UserInfo userInfo, int nauditactionfiltercode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String auditActionFilterQuery = "select mt.*,coalesce(mt.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', mt.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " mt.jsondata->'sdisplayname'->>'en-US' as sauditactionfiltername from auditactionfilter mt where mt.nauditactionfiltercode > 0   "
				+ " and mt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nauditactionfiltercode";
		final List<AuditActionFilter> lstAuditctionFilterQuery = jdbcTemplate.query(auditActionFilterQuery,
				new AuditActionFilter());
		outputMap.put("listofItem", lstAuditctionFilterQuery);
		outputMap.put("headername", "Audit Action Filter");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getAttachmenttypeComboService(UserInfo userInfo, int nattachmenttypecode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String attachmentTypeQuery = "select at.*,coalesce(at.jsondata->'sattachmenttype'->>'"
				+ userInfo.getSlanguagetypecode() + "', at.jsondata->'sattachmenttype'->>'en-US') as sdisplayname, "
				+ " at.jsondata->'sattachmenttype'->>'en-US' as sdefaultname from attachmenttype at where at.nattachmenttypecode > 0   "
				+ " and at.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nattachmenttypecode";
		final List<MultilingualMasters> lstAuditctionFilterQuery = jdbcTemplate.query(attachmentTypeQuery,
				new MultilingualMasters());
		outputMap.put("listofItem", lstAuditctionFilterQuery);
		outputMap.put("headername", "Attachment Type");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getFTPtypeComboService(UserInfo userInfo, int nftptypecode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String ftpTypeQuery = "select ft.nftptypecode, ft.sftptypename, ft.jsondata, ft.ndefaultstatus, ft.nstatus,coalesce(ft.jsondata->'sftptypename'->>'"
				+ userInfo.getSlanguagetypecode() + "', ft.jsondata->'sftptypename'->>'en-US') as sdisplayname, "
				+ " ft.sftptypename as sdefaultname from ftptype ft where ft.nftptypecode > 0   " + " and ft.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nftptypecode";
		final List<MultilingualMasters> lstAuditctionFilterQuery = jdbcTemplate.query(ftpTypeQuery,
				new MultilingualMasters());
		outputMap.put("listofItem", lstAuditctionFilterQuery);
		outputMap.put("headername", "FTP Type");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReportTypeComboService(UserInfo userInfo, int nreporttypecode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String reportTypeQuery = "select rt.*,coalesce(rt.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', rt.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ "rt.sreporttypename  from reporttype rt where rt.nreporttypecode > 0   " + "   and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nreporttypecode  ";
		final List<ReportType> lstAuditctionFilterQuery = jdbcTemplate.query(reportTypeQuery, new ReportType());
		outputMap.put("listofItem", lstAuditctionFilterQuery);
		outputMap.put("headername", "Report Type");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getCOAReportTypeComboService(UserInfo userInfo, int ncoareporttypecode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String COAReportTypeQuery = "select crt.*,coalesce(crt.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', crt.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " crt.scoareporttypename  from coareporttype crt where crt.ncoareporttypecode > 0   "
				+ " and crt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by ncoareporttypecode";
		final List<COAReportType> lstAuditctionFilterQuery = jdbcTemplate.query(COAReportTypeQuery,
				new COAReportType());
		outputMap.put("listofItem", lstAuditctionFilterQuery);
		outputMap.put("headername", "COAReport Type");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReactComponentsComboService(UserInfo userInfo, int nreactcomponentcode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String reactComponentsQuery = "select rc.*,coalesce(rc.jsondata->'componentname'->>'"
				+ userInfo.getSlanguagetypecode() + "', rc.jsondata->'componentname'->>'en-US') as sdisplayname, "
				+ " rc.jsondata->'componentname'->>'en-US' as componentname  from reactcomponents rc where rc.nreactcomponentcode > 0   "
				+ " and rc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nreactcomponentcode";
		final List<ReactComponents> lstAuditctionFilterQuery = jdbcTemplate.query(reactComponentsQuery,
				new ReactComponents());
		outputMap.put("listofItem", lstAuditctionFilterQuery);
		outputMap.put("headername", "React Components");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getFunctionsComboService(UserInfo userInfo, int nfunctioncode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String functionsQuery = "select f.*,coalesce(f.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', f.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " f.jsondata->'sdisplayname'->>'en-US' as sfunctionname  from functions f where f.nfunctioncode > 0   "
				+ " and f.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nfunctioncode";
		final List<Functions> lstfunctionsQuery = jdbcTemplate.query(functionsQuery, new Functions());
		outputMap.put("listofItem", lstfunctionsQuery);
		outputMap.put("headername", "Functions");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDynamicFormulaFieldsComboService(UserInfo userInfo, int ndynamicformulafieldcode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String dynamicFormulafieldsQuery = "select df.*,coalesce(df.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', df.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " df.jsondata->'sdisplayname'->>'en-US' as sdefaultname  from dynamicformulafields df where df.ndynamicformulafieldcode > 0   "
				+ " and df.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by ndynamicformulafieldcode";
		final List<DynamicFormulaFields> lstdynamicFormulafieldsQuery = jdbcTemplate.query(dynamicFormulafieldsQuery,
				new DynamicFormulaFields());
		outputMap.put("listofItem", lstdynamicFormulafieldsQuery);
		outputMap.put("headername", "Dynamic Formula Fields");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getChartTypeComboService(UserInfo userInfo, int ncharttypecode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String charTypeQuery = "select ct.*,coalesce(ct.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', ct.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " ct.jsondata->'sdisplayname'->>'en-US' as sdefaultname  from charttype ct where ct.ncharttypecode > 0   "
				+ " and ct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by ncharttypecode";
		final List<ChartType> lstcharTypeQuery = jdbcTemplate.query(charTypeQuery, new ChartType());
		outputMap.put("listofItem", lstcharTypeQuery);
		outputMap.put("headername", "Chart Type");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDesignComponentsComboService(UserInfo userInfo, int ndesigncomponentcode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String designcomponentsQuery = "select dc.*,coalesce(dc.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', dc.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " dc.jsondata->'sdisplayname'->>'en-US' as sdefaultname  from designcomponents dc where dc.ndesigncomponentcode > 0   "
				+ " and dc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by ndesigncomponentcode";
		final List<DesignComponents> lstdesigncomponentsQuery = jdbcTemplate.query(designcomponentsQuery,
				new DesignComponents());
		outputMap.put("listofItem", lstdesigncomponentsQuery);
		outputMap.put("headername", "Design Components");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getCheckListComponentService(UserInfo userInfo, int nchecklistcomponentcode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String clcQuery = "select clc.*,coalesce(clc.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', clc.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " clc.jsondata->'sdisplayname'->>'en-US' as scomponentname  from checklistcomponent clc where clc.nchecklistcomponentcode > 0   "
				+ " and clc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nchecklistcomponentcode";
		final List<ChecklistComponent> lstclcQuery = jdbcTemplate.query(clcQuery, new ChecklistComponent());
		outputMap.put("listofItem", lstclcQuery);
		outputMap.put("headername", "CheckList Component");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getGenericLabelService(UserInfo userInfo, int ngenericlabelcode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String gerenicLabelQuery = "select gl.*,coalesce(gl.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', gl.jsondata->'sdisplayname'->>'en-US') as sdisplayname "
				+ " from genericlabel gl where gl.ngenericlabelcode > 0   " + "   and gl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ngenericlabelcode";
		final List<GenericLabel> lstglQuery = jdbcTemplate.query(gerenicLabelQuery, new GenericLabel());
		outputMap.put("listofItem", lstglQuery);
		outputMap.put("headername", "Generic Label");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getQueryBuilderTableColumnsService(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		String conditionQueryBuilderTableColumns = "";
		String conditionInformationSchema = "";
		int nquerybuildertablecode = -1;
		String tablecolumnname = "";

		List<Map<String, Object>> lstqbtcQuery = new ArrayList<>();
		List<Map<String, Object>> lstColumnNames = new ArrayList<>();
		List<Map<String, Object>> selectedLstColumnNames = new ArrayList<>();

		if (inputMap.get("nquerybuildertablecode") == null) {

			conditionQueryBuilderTableColumns = "> 0";
			lstqbtcQuery = getQueryBuilderTableColumnsByCondition(userInfo, conditionQueryBuilderTableColumns);

			nquerybuildertablecode = (int) lstqbtcQuery.get(lstqbtcQuery.size() - 1).get("nquerybuildertablecode");
			outputMap.put("QueryBuilderStableName", lstqbtcQuery);

		} else {
			nquerybuildertablecode = (int) inputMap.get("nquerybuildertablecode");
			conditionQueryBuilderTableColumns = "=" + nquerybuildertablecode;
			lstqbtcQuery = getQueryBuilderTableColumnsByCondition(userInfo, conditionQueryBuilderTableColumns);
		}

		if (inputMap.get("tablecolumnname") == null) {

			conditionInformationSchema = " order by ordinal_position desc";
			lstColumnNames = getInformationSchemaByCondition(userInfo, conditionInformationSchema);

			tablecolumnname = (String) lstColumnNames.get(0).get("scolumnname");
			outputMap.put("QueryBuilderScolumnList", lstColumnNames);
			outputMap.put("SelectedQueryBuilderStableName", Arrays.asList(lstqbtcQuery.get(lstqbtcQuery.size() - 1)));
			outputMap.put("selectedQueryBuilderScolumnList", Arrays.asList(lstColumnNames.get(0)));

		} else {
			tablecolumnname = (String) inputMap.get("tablecolumnname");

			conditionInformationSchema = " and COLUMN_NAME='" + tablecolumnname + "' order by ordinal_position desc";
			selectedLstColumnNames = getInformationSchemaByCondition(userInfo, conditionInformationSchema);

			outputMap.put("selectedQueryBuilderScolumnList", selectedLstColumnNames);

		}

		String listItems = "select ordinality-1 as index, qbtc.*, coalesce(arr." + tablecolumnname
				+ "->'displayname'->>'" + userInfo.getSlanguagetypecode() + "', arr." + tablecolumnname
				+ "->'displayname'->>'en-US') as sdisplayname, arr." + tablecolumnname
				+ "->'columnname' as scolumnname " + "from querybuildertablecolumns qbtc, jsonb_array_elements(qbtc."
				+ tablecolumnname + ") with ordinality" + " arr(" + tablecolumnname
				+ ") where qbtc.nquerybuildertablecode=" + nquerybuildertablecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<Map<String, Object>> lstOfItems = jdbcTemplate.queryForList(listItems);

		outputMap.put("listofItem", lstOfItems);
		outputMap.put("headername", "Query Builder Table Columns");

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private List<Map<String, Object>> getQueryBuilderTableColumnsByCondition(UserInfo userInfo, String conditionValue)
			throws Exception {
		final String queryBuilderTableColumnsQuery = "select qbtc.nquerybuildertablecode, qbtc.nformcode, qbtc.sprimarykeyname, "
				+ "qbtc.jstaticcolumns,qbtc.jdynamiccolumns,qbtc.jmultilingualcolumn, qbtc.jnumericcolumns, qbtc.jsqlquerycolumns,  qbtc.nstatus, "
				+ "coalesce(qbt.jsondata->'tablename'->>'" + userInfo.getSlanguagetypecode()
				+ "',qbt.jsondata->'tablename'->>'en-US') as stablename "
				+ " from querybuildertablecolumns qbtc, querybuildertables qbt where qbtc.nquerybuildertablecode"
				+ conditionValue + " and qbtc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qbt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qbtc.nquerybuildertablecode=qbt.nquerybuildertablecode order by qbtc.nquerybuildertablecode desc";
		return (List<Map<String, Object>>) jdbcTemplate.queryForList(queryBuilderTableColumnsQuery);
	}

	private List<Map<String, Object>> getInformationSchemaByCondition(UserInfo userInfo, String conditionValue)
			throws Exception {
		String Query = "SELECT COLUMN_NAME as scolumnname, ordinal_position FROM INFORMATION_SCHEMA.COLUMNS  "
				+ " WHERE TABLE_NAME = 'querybuildertablecolumns' and " + "data_type='jsonb' " + conditionValue + "";

		return (List<Map<String, Object>>) jdbcTemplate.queryForList(Query);
	}

	@Override
	public ResponseEntity<Object> getDynamicAuditTableService(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		int ndynamicaudittablecode = -1;
		int nformcode = -1;
		String conditionFormcode = "";
		String conditionDynamicAuditTable = "";

		List<Map<String, Object>> lststrDynamicAuditTable = new ArrayList<>();
		List<Map<String, Object>> lstQualisForms = new ArrayList<>();

		if (inputMap.get("nformcode") == null) {

			conditionFormcode = "select nformcode from dynamicaudittable where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by nformcode";
			lstQualisForms = getQualisForms(userInfo, conditionFormcode);

			nformcode = (int) lstQualisForms.get(0).get("nformcode");
			outputMap.put("lstFormName", lstQualisForms);

		} else {
			nformcode = (int) inputMap.get("nformcode");

			conditionFormcode = "" + nformcode + "";
			lstQualisForms = getQualisForms(userInfo, conditionFormcode);
		}
		if (inputMap.get("ndynamicaudittablecode") == null) {

			conditionDynamicAuditTable = " dat.nformcode=" + nformcode + " ";
			lststrDynamicAuditTable = getDynamicAuditTable(userInfo, conditionDynamicAuditTable);

			outputMap.put("lstDynamicAuditTable", lststrDynamicAuditTable);
			ndynamicaudittablecode = (int) lststrDynamicAuditTable.get(lststrDynamicAuditTable.size() - 1)
					.get("ndynamicaudittablecode");
			outputMap.put("selectedDynamicAuditTable",
					Arrays.asList(lststrDynamicAuditTable.get(lststrDynamicAuditTable.size() - 1)));
			outputMap.put("SelectedFormName", Arrays.asList(lstQualisForms.get(0)));
		} else {
			ndynamicaudittablecode = (int) inputMap.get("ndynamicaudittablecode");

			conditionDynamicAuditTable = " dat.nformcode=" + nformcode + " and dat.ndynamicaudittablecode="
					+ ndynamicaudittablecode + " ";
			lststrDynamicAuditTable = getDynamicAuditTable(userInfo, conditionDynamicAuditTable);

			outputMap.put("selectedDynamicAuditTable", lststrDynamicAuditTable);
		}

		List<Map<String, Object>> lstOfItemsEnabled = getMethodBasedOnSubSampleEnabledDisabled(userInfo,
				commonFunction.getMultilingualMessage("IDS_SUBSAMPLEENABLED", userInfo.getSlanguagefilename()),
				"subsampleenabled", ndynamicaudittablecode);

		List<Map<String, Object>> lstOfItemsDisabled = getMethodBasedOnSubSampleEnabledDisabled(userInfo,
				commonFunction.getMultilingualMessage("IDS_SUBSAMPLEDISABLED", userInfo.getSlanguagefilename()),
				"subsampledisabled", ndynamicaudittablecode);

		List<Map<String, Object>> lstOfItems = new ArrayList<>();

		if (!lstOfItemsEnabled.isEmpty() && !lstOfItemsDisabled.isEmpty()) {
			String lstObjEnabled = (String) lstOfItemsEnabled.get(0).get("skeyvalue");
			String lstObjDisabled = (String) lstOfItemsDisabled.get(0).get("skeyvalue");
			String keyEnabled[] = lstObjEnabled.replace("[", "").replace("]", "").replace("\"", "").split(",");
			String keyDisabled[] = lstObjDisabled.replace("[", "").replace("]", "").replace("\"", "").split(",");

			int indexEnabled = -1;
			int indexDisabled = -1;
			int iterationLength = (keyEnabled.length == keyDisabled.length ? keyEnabled.length
					: keyEnabled.length > keyDisabled.length ? keyEnabled.length : keyDisabled.length);
			for (int i = 0; i < iterationLength; i++) {
				if (i < keyEnabled.length && !keyEnabled[i].isEmpty()) {
					for (int j = 0; j < lstOfItemsEnabled.size(); j++) {
						if (new JSONObject(((PGobject) lstOfItemsEnabled.get(j).get("jsondata")).getValue()).keySet()
								.contains(keyEnabled[i].trim())) {
							indexEnabled = j;
						}
					}
					JSONObject bEnabled = (JSONObject) new JSONObject(
							((PGobject) lstOfItemsEnabled.get(indexEnabled).get("jsondata")).getValue())
							.get(keyEnabled[i].trim());
					lstOfItemsEnabled.get(indexEnabled).put("sdisplayname",
							bEnabled.get(userInfo.getSlanguagetypecode()));
					lstOfItemsEnabled.get(indexEnabled).put("index", i);
				}
				if (i < keyDisabled.length && !keyDisabled[i].isEmpty()) {
					for (int j = 0; j < lstOfItemsDisabled.size(); j++) {
						if (new JSONObject(((PGobject) lstOfItemsDisabled.get(j).get("jsondata")).getValue()).keySet()
								.contains(keyDisabled[i].trim())) {
							indexDisabled = j;
						}
					}
					JSONObject bDisabled = (JSONObject) new JSONObject(
							((PGobject) lstOfItemsDisabled.get(indexDisabled).get("jsondata")).getValue())
							.get(keyDisabled[i].trim());
					lstOfItemsDisabled.get(indexDisabled).put("sdisplayname",
							bDisabled.get(userInfo.getSlanguagetypecode()));
					lstOfItemsDisabled.get(indexDisabled).put("index", i + keyEnabled.length);
				}
			}

			lstOfItems.addAll(lstOfItemsEnabled);
			lstOfItems.addAll(lstOfItemsDisabled);
		}
		outputMap.put("listofItem", lstOfItems);
		outputMap.put("headername", "Dynamic Audit Table");

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private List<Map<String, Object>> getQualisForms(UserInfo userInfo, String conditionValue) throws Exception {
		final String qualisFormsQuery = "select nformcode, coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', jsondata->'sdisplayname'->>"
				+ "'en-US') as sformname, nmenucode, nmodulecode, jsondata, sclassname, surl, nsorter,"
				+ "nstatus from qualisforms where nformcode in (" + conditionValue + ")" + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (List<Map<String, Object>>) jdbcTemplate.queryForList(qualisFormsQuery);
	}

	private List<Map<String, Object>> getDynamicAuditTable(UserInfo userInfo, String conditionValue) throws Exception {
		final String dynamicAuditTableQuery = "select dat.ndynamicaudittablecode, dat.nformcode, dat.nissubsampletable, dat.jsondata, dat.nsorter, "
				+ "dat.nsitecode, dat.nstatus, dat.stablename from dynamicaudittable dat where " + conditionValue
				+ " and dat.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by dat.ndynamicaudittablecode";
		return (List<Map<String, Object>>) jdbcTemplate.queryForList(dynamicAuditTableQuery);
	}

	private List<Map<String, Object>> getMethodBasedOnSubSampleEnabledDisabled(UserInfo userInfo, String idsValue,
			String conditionValue, int ndynamicaudittablecode) throws Exception {
		String listOfItems = "select '" + idsValue + "' as sfieldname, '" + conditionValue
				+ "' as fieldname, dat.jsondata->'" + conditionValue + "'->>'auditcapturefields' as skeyvalue,"
				+ " arr.jsondata,dat.* from dynamicaudittable dat," + " jsonb_array_elements(dat.jsondata->'"
				+ conditionValue + "'->'multilingualfields') "
				+ " with ORDINALITY arr(jsondata) where dat.ndynamicaudittablecode=" + ndynamicaudittablecode
				+ " and dat.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (List<Map<String, Object>>) jdbcTemplate.queryForList(listOfItems);
	}

	@Override
	public ResponseEntity<Object> getMappedTemplateFieldPropsService(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstOfItems = new ArrayList<>();
		int nmappedtemplatefieldpropcode = -1;
		List<String> lstKeyValues = Arrays.asList("samplegriditem", "samplelistitem", "testListFields",
				"subsamplelistitem", "samplegridmoreitem", "sampledisplayfields");
		List<String> lstSubKeyValues = Arrays.asList("conditionfields", "selectfields",
				"querybuildertablecolumnsfordynamicview");
		List<Map<String, Object>> lstMappedTemplateFieldProps = new ArrayList<>();
		List<Map<String, Object>> lstQualisForms = new ArrayList<>();
		int index = 0;
		String conditionValue = "";
		String fieldName;
		String displayField;
		String selectedField;
		String ordinalityFields;
		if (inputMap.get("nmappedtemplatefieldpropcode") == null) {
			lstMappedTemplateFieldProps = getMappedTemplateFieldPropsByCode(userInfo, "");
			if (!lstMappedTemplateFieldProps.isEmpty()) {
				outputMap.put("lstMappedTemplateFieldProps", lstMappedTemplateFieldProps);
				outputMap.put("selectedLstMappedTemplateFieldProps", Arrays.asList(lstMappedTemplateFieldProps.get(0)));
				nmappedtemplatefieldpropcode = (int) lstMappedTemplateFieldProps.get(0)
						.get("nmappedtemplatefieldpropcode");
			} else {
				outputMap.put("lstMappedTemplateFieldProps", null);
				outputMap.put("selectedLstMappedTemplateFieldProps", null);
				outputMap.put("lstQualisforms", null);
				outputMap.put("selectedLstQualisforms", null);
				outputMap.put("lstSampleItems", null);
				outputMap.put("headername", "Mapped Template Field Props");
				outputMap.put("listofItem", null);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			}
		} else {
			nmappedtemplatefieldpropcode = (int) inputMap.get("nmappedtemplatefieldpropcode");
			conditionValue = " and mtfp.nmappedtemplatefieldpropcode=" + nmappedtemplatefieldpropcode + " ";
			lstMappedTemplateFieldProps = getMappedTemplateFieldPropsByCode(userInfo, conditionValue);
			outputMap.put("selectedLstMappedTemplateFieldProps", Arrays.asList(lstMappedTemplateFieldProps.get(0)));
		}
		JSONObject jsondata = new JSONObject();
		Set<String> keyValues = new HashSet<>();

		jsondata = (JSONObject) new JSONObject(
				((PGobject) lstMappedTemplateFieldProps.get(0).get("jsondata")).getValue());
		keyValues = jsondata.keySet();

		List<Integer> lstInt = new ArrayList<>();
		List<Map<String, Object>> lstStr = new ArrayList<>();

		for (String s : keyValues) {
			Map<String, Object> mapObj = new HashMap<>();
			if ((s.replaceAll("[0-9]", "")).isEmpty()) {
				lstInt.add(Integer.parseInt(s));
			} else {
				if (!s.equals("deleteValidation") && !s.equals("samplecombinationunique")
						&& !s.equals("subsamplecombinationunique")) {
					mapObj.put("sformname", s);
					lstStr.add(index, mapObj);
					index++;
				}
			}
		}
		lstQualisForms = getQualisForms(userInfo, (lstInt.toString()).replace("[", "").replace("]", "").trim());
		lstQualisForms.addAll(lstStr);
		for (int i = 0; i < lstQualisForms.size(); i++) {
			Map<String, Object> indexPropertiesValue = new HashMap<>();
			indexPropertiesValue.put("indexQualisforms", i);
			lstQualisForms.get(i).putAll(indexPropertiesValue);
		}
		outputMap.put("lstQualisforms", lstQualisForms);
		if (inputMap.get("indexQualisforms") == null) {
			outputMap.put("selectedLstQualisforms", Arrays.asList(lstQualisForms.get(0)));
		} else {
			outputMap.put("selectedLstQualisforms",
					Arrays.asList(lstQualisForms.get((int) inputMap.get("indexQualisforms"))));
		}
		int indexQualisforms = -1;
		if (inputMap.get("indexQualisforms") == null) {
			indexQualisforms = 0;
		} else {
			indexQualisforms = (int) inputMap.get("indexQualisforms");
		}
		if (lstQualisForms.get(indexQualisforms).get("nformcode") != null) {
			JSONObject jsonObject = (JSONObject) jsondata
					.get(lstQualisForms.get(indexQualisforms).get("nformcode").toString());
			List<String> keyValueForFormCode = new ArrayList<>();

			jsonObject.keySet().stream().forEach(jsb -> {
				lstKeyValues.stream().forEach(lkv -> {
					if (lkv.equals(jsb)) {
						keyValueForFormCode.add(jsb);
					}
				});
			});
			List<Map<String, Object>> lstMapObj = new ArrayList<>();
			index = 0;
			for (String s : keyValueForFormCode) {
				Map<String, Object> mapObj = new HashMap<>();
				mapObj.put("indexPropertiesKey", index);
				mapObj.put("indexPropertiesValue", s);
				lstMapObj.add(index, mapObj);
				index++;
			}
			int indexPropertiesKey = -1;
			if (inputMap.get("indexPropertiesKey") != null) {
				indexPropertiesKey = (int) inputMap.get("indexPropertiesKey");
			} else {
				indexPropertiesKey = 0;
			}
			if (lstMapObj.get(indexPropertiesKey).get("indexPropertiesValue").equals("testListFields")) {
				JSONObject jsonSample = (JSONObject) jsonObject
						.get(lstMapObj.get(indexPropertiesKey).get("indexPropertiesValue").toString());
				Set<String> lstSample = jsonSample.keySet();
				List<Map<String, Object>> lstSampleItems = new ArrayList<>();
				index = 0;
				for (String s : lstSample) {
					Map<String, Object> mapObj = new HashMap<>();
					if (!s.equals("testsearchfields")) {
						mapObj.put("indexFieldKey", index);
						mapObj.put("indexFieldValue", s);
						lstSampleItems.add(index, mapObj);
						index++;
					}
				}
				outputMap.put("lstSampleField", lstSampleItems);
				int indexValueSample = -1;
				if (inputMap.get("indexFieldKey") == null) {
					indexValueSample = 0;
				} else {
					indexValueSample = (int) inputMap.get("indexFieldKey");
				}
				outputMap.put("selectedLstSampleField", Arrays.asList(lstSampleItems.get(indexValueSample)));

				if (lstSubKeyValues.contains(lstSampleItems.get(indexValueSample).get("indexFieldValue"))) {
					fieldName = "" + lstQualisForms.get(indexQualisforms).get("nformcode");
					displayField = "displayname";
					selectedField = "columnname";
					ordinalityFields = lstQualisForms.get(indexQualisforms).get("nformcode") + "'->'"
							+ lstMapObj.get(indexPropertiesKey).get("indexPropertiesValue") + "'->'"
							+ lstSampleItems.get(indexValueSample).get("indexFieldValue");
				} else {
					fieldName = "" + lstQualisForms.get(indexQualisforms).get("nformcode");
					displayField = "1";
					selectedField = "2";
					ordinalityFields = lstQualisForms.get(indexQualisforms).get("nformcode") + "'->'"
							+ lstMapObj.get(indexPropertiesKey).get("indexPropertiesValue") + "'->'"
							+ lstSampleItems.get(indexValueSample).get("indexFieldValue");
				}
				outputMap.put("lstSampleField", lstSampleItems);
				outputMap.put("selectedLstSampleField", Arrays.asList(lstSampleItems.get(indexValueSample)));
			} else {
				fieldName = "" + lstQualisForms.get(indexQualisforms).get("nformcode");
				displayField = "1";
				selectedField = "2";
				ordinalityFields = lstQualisForms.get(indexQualisforms).get("nformcode") + "'->'"
						+ lstMapObj.get(indexPropertiesKey).get("indexPropertiesValue");
			}
			outputMap.put("lstSampleItems", lstMapObj);
			outputMap.put("selectedLstSampleItems", Arrays.asList(lstMapObj.get(indexPropertiesKey)));
		} else {
			fieldName = "" + lstQualisForms.get(indexQualisforms).get("sformname");
			displayField = "1";
			selectedField = "2";
			ordinalityFields = "" + lstQualisForms.get(indexQualisforms).get("sformname");
		}

		String strMappedTemplateFieldProps = "select ordinality-1 as index, '" + fieldName
				+ "' as sfieldname, coalesce(arr.jsondata->'" + displayField + "'->>'" + userInfo.getSlanguagetypecode()
				+ "', arr.jsondata->'" + displayField + "'->>'en-US') as sdisplayname, arr.jsondata->>'" + selectedField
				+ "' as selectedField, mtfp.* from "
				+ " mappedtemplatefieldprops mtfp, jsonb_array_elements(mtfp.jsondata" + "->'" + ordinalityFields
				+ "') with ORDINALITY  arr(jsondata) where nmappedtemplatefieldpropcode=" + nmappedtemplatefieldpropcode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		lstOfItems = jdbcTemplate.queryForList(strMappedTemplateFieldProps);
		outputMap.put("headername", "Mapped Template Field Props");
		outputMap.put("listofItem", lstOfItems);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private List<Map<String, Object>> getMappedTemplateFieldPropsByCode(UserInfo userInfo, String conditionValue)
			throws Exception {
		String strMappedTemplateFieldProps = "select mtfp.*, dtm.nversionno, rrt.sregtemplatename || '(' || dtm.nversionno || ')'"
				+ " as sregtemplatename from mappedtemplatefieldprops mtfp, designtemplatemapping dtm, reactregistrationtemplate rrt where "
				+ " mtfp.ndesigntemplatemappingcode=dtm.ndesigntemplatemappingcode and dtm.nreactregtemplatecode="
				+ " rrt.nreactregtemplatecode and mtfp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dtm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rrt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditionValue + " order by mtfp.nmappedtemplatefieldpropcode";
		return jdbcTemplate.queryForList(strMappedTemplateFieldProps);
	}
}
