package com.agaramtech.qualis.reports.service.reportconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.SiteControlMaster;
import com.agaramtech.qualis.dashboard.model.DesignComponents;
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CloudStorageUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.reports.model.COAReportType;
import com.agaramtech.qualis.reports.model.CertificateType;
import com.agaramtech.qualis.reports.model.ControlbasedReportvalidation;
import com.agaramtech.qualis.reports.model.ParameterConfiguration;
import com.agaramtech.qualis.reports.model.ReportDecisionType;
import com.agaramtech.qualis.reports.model.ReportDesignConfig;
import com.agaramtech.qualis.reports.model.ReportDetails;
import com.agaramtech.qualis.reports.model.ReportImages;
import com.agaramtech.qualis.reports.model.ReportMaster;
import com.agaramtech.qualis.reports.model.ReportModule;
import com.agaramtech.qualis.reports.model.ReportParameter;
import com.agaramtech.qualis.reports.model.ReportParameterAction;
import com.agaramtech.qualis.reports.model.ReportParameterMapping;
import com.agaramtech.qualis.reports.model.ReportType;
import com.agaramtech.qualis.reports.model.SeqNoReportManagement;
import com.agaramtech.qualis.reports.model.SeqNoReportVersion;
import com.agaramtech.qualis.reports.model.SubReportDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ReportMasterDAOImpl implements ReportMasterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportMasterDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final CommonFunction commonFunction;
	private final CloudStorageUtilityFunction cloudStorageUtilityFunction;

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getReportMaster(Integer nreportCode, String nreportTypeCode, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		ReportMaster selectedReport = null;

		String query = " select rm.* ,ret.isneedregtype,crt.isneedsection, ret.sreporttypename, "
				+ " coalesce(ret.jsondata ->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',ret.jsondata ->'sdisplayname'->>'en-US')  as sreportdisplayname, "
				+ " coalesce(st.jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',st.jsondata->'sampletypename'->>'en-US') as ssampletypename, "
				+ " coalesce(rt.jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename, "
				+ " coalesce(rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,"
				+ " coalesce(crt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',crt.jsondata->'sdisplayname'->>'en-US') as scoareporttypename,"
				+ " coalesce(rmd.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',rmd.jsondata->'sdisplayname'->>'en-US') as sreportmodulename,"
				+ " s.ssectionname,cm.scolorhexcode ,    " + " coalesce(rmd.jsondata ->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rmd.jsondata ->'sdisplayname'->>'en-US')  as smoduledisplayname, "
				+ " coalesce(ts.jsondata ->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata ->'stransdisplaystatus'->>'en-US')  as sactivestatus, "
				+ " q.nformcode, coalesce(c.jsondata ->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
				+ "',c.jsondata ->'scontrolids'->>'en-US')  as scontrolids,  "
				+ " coalesce(q.jsondata ->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',q.jsondata ->'sdisplayname'->>'en-US')  as sdisplayname,aca.sversionname sapproveversionname, "
				+ " coalesce(rtm.jsondata->'stemplatename'->>'" + userInfo.getSlanguagetypecode()
				+ "', rtm.jsondata->'stemplatename'->>'en-US') as sreporttemplatename "
				+ " from reportmaster rm join reporttype ret on ret.nreporttypecode=rm.nreporttypecode "
				+ " join reportmodule rmd on rmd.nreportmodulecode = rm.nreportmodulecode "
				+ " join sampletype st on  st.nsampletypecode =rm.nsampletypecode  "
				+ " join registrationtype rt on  rt.nregtypecode =rm.nregtypecode  "
				+ " join registrationsubtype rst on rst.nregsubtypecode =rm.nregsubtypecode "
				+ " join approvalconfigautoapproval aca on aca.napprovalconfigversioncode = rm.napproveconfversioncode "
				+ " join coareporttype crt on crt.ncoareporttypecode = rm.ncoareporttypecode "
				+ " join transactionstatus ts  on ts.ntranscode = rm.ntransactionstatus "
				+ " join section s on  s.nsectioncode=rm.nsectioncode "
				+ " join controlmaster c on c.ncontrolcode =rm.ncontrolcode "
				+ " join qualisforms q on q.nformcode =c.nformcode "
				+ " join formwisestatuscolor fsc on fsc.ntranscode = rm.ntransactionstatus  "
				+ " join colormaster cm on  cm.ncolorcode = fsc.ncolorcode  "
				+ " left outer join reporttemplate rtm on rm.nreporttemplatecode=rtm.nreporttemplatecode  and rtm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where  " + " rm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rmd.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and crt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and q.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and  fsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and fsc.nformcode = "
				+ Enumeration.FormCode.REPORTCONFIG.getFormCode() + "   ";

		if (nreportTypeCode != null && Integer.parseInt(nreportTypeCode) != 0) {

			query = query + " and rm.nreporttypecode = " + nreportTypeCode;
		}
		query = query + " and rm.nsitecode = " + userInfo.getNmastersitecode() + " order by nreportcode desc";

		final List<ReportMaster> reportList = jdbcTemplate.query(query, new ReportMaster());
		if (nreportCode == null) {
			final String reportTypeString = " select ntranscode as nreporttypecode,coalesce(jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "', "
					+ "jsondata->'stransdisplaystatus'->>'en-US') as sdisplayname, " + " 0 as isneedregtype "
					+ " from transactionstatus where ntranscode=0  and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all"
					+ " select nreporttypecode, coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "', " + " jsondata->'sdisplayname'->>'en-US')sdisplayname, isneedregtype  from reporttype where "
					+ " nreporttypecode > 0 and  nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<ReportType> typeList = jdbcTemplate.query(reportTypeString, new ReportType());
			outputMap.put("ReportTypeList", typeList);

			if (nreportTypeCode == null || Integer.parseInt(nreportTypeCode) == 0) {
				outputMap.put("SelectedFilterReportType", typeList.get(0));
			} else {
				final int typeCode = Integer.parseInt(nreportTypeCode);
				final List<ReportType> typeList1 = typeList.stream()
						.filter(item -> item.getNreporttypecode() == typeCode).collect(Collectors.toList());
				outputMap.put("SelectedFilterReportType", typeList1.get(0));
			}

			if (reportList.isEmpty()) {
				outputMap.put("ReportMaster", reportList);
				outputMap.put("SelectedReportMaster", null);
				outputMap.put("ReportDetails", Arrays.asList());
				outputMap.put("SelectedReportDetail", null);
				outputMap.put("ReportParameter", Arrays.asList());
				outputMap.put("ReportDesignConfig", Arrays.asList());
				outputMap.put("ReportParameterMapping", Arrays.asList());
				outputMap.put("ReportParameterAction", Arrays.asList());
				outputMap.put("SubReportDetails", Arrays.asList());
				outputMap.put("ReportImages", Arrays.asList());
				outputMap.put("ReportValidation", Arrays.asList());

				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {

				outputMap.put("ReportMaster", reportList);

				selectedReport = reportList.get(0);
				nreportCode = selectedReport.getNreportcode();

			}
		} else {
			outputMap.put("ReportMaster", reportList);
			selectedReport = getActiveReportMasterById(nreportCode, userInfo);
		}

		if (selectedReport == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedReportMaster", selectedReport);

			final String detailQueryString = " select rd.*, case when rd.nversionno> 0 then cast(rd.nversionno as text) else '-' end as sversionno,"
					+ "	 coalesce (ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US')as sdisplaystatus, "
					+ " coalesce (ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts2.jsondata->'stransdisplaystatus'->>'en-US')as splsqlquery, cm.scolorhexcode"
					+ "  from reportdetails rd,transactionstatus ts1, transactionstatus ts2,reportmaster rm,"
					+ "formwisestatuscolor fsc,colormaster cm  where rd.nreportcode=rm.nreportcode"
					+ "	 and rd.ntransactionstatus = ts1.ntranscode and rd.nisplsqlquery = ts2.ntranscode"
					+ "	 and rm.nstatus=  rd.nstatus and rd.nstatus=  ts1.nstatus and ts1.nstatus = ts2.nstatus"
					+ " and ts2.nstatus  = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "	 and rd.nreportcode =" + selectedReport.getNreportcode()
					+ " and cm.ncolorcode = fsc.ncolorcode and fsc.nformcode = "
					+ Enumeration.FormCode.REPORTCONFIG.getFormCode() + " and fsc.ntranscode = rd.ntransactionstatus"
					+ " and cm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and fsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by rd.nreportdetailcode  asc ";

			final List<ReportDetails> reportDetailsList = jdbcTemplate.query(detailQueryString, new ReportDetails());
			if (reportDetailsList.isEmpty()) {
				outputMap.put("ReportDetails", Arrays.asList());
				outputMap.put("SelectedReportDetail", null);
				outputMap.put("ReportParameter", Arrays.asList());
				outputMap.put("ReportDesignConfig", Arrays.asList());
				outputMap.put("ReportParameterMapping", Arrays.asList());
				outputMap.put("ReportParameterAction", Arrays.asList());
				outputMap.put("SubReportDetails", Arrays.asList());
				outputMap.put("ReportImages", Arrays.asList());
				outputMap.put("ReportValidation", Arrays.asList());

			} else {

				outputMap.put("ReportDetails", reportDetailsList);
				final ReportDetails reportDetail = reportDetailsList.get(reportDetailsList.size() - 1);
				outputMap.put("SelectedReportDetail", reportDetail);

				outputMap.putAll((Map<String, Object>) getReportParameter(reportDetail.getNreportdetailcode(), userInfo)
						.getBody());
				outputMap.putAll(
						(Map<String, Object>) getReportDesignConfig(reportDetail.getNreportdetailcode(), userInfo)
								.getBody());
				outputMap.putAll(
						(Map<String, Object>) getReportParameterMapping(reportDetail.getNreportdetailcode(), userInfo)
								.getBody());
				outputMap.putAll(
						(Map<String, Object>) getReportParameterAction(reportDetail.getNreportdetailcode()).getBody());
				outputMap.putAll(
						(Map<String, Object>) getSubReportDetails(reportDetail.getNreportdetailcode()).getBody());
				outputMap.putAll((Map<String, Object>) getReportImages(reportDetail.getNreportdetailcode()).getBody());
				outputMap.putAll(
						(Map<String, Object>) getControlbasedReportvalidation(reportDetail.getNreportdetailcode(),
								userInfo).getBody());
				outputMap.putAll(
						(Map<String, Object>) getControlbasedReportParameter(reportDetail.getNreportdetailcode(),
								userInfo).getBody());

			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getReportDetail(final int nreportDetailCode, final UserInfo userInfo)
			throws Exception {
		final ReportDetails reportDetail = getActiveReportDetailById(nreportDetailCode, userInfo);
		if (reportDetail == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else {
			final Map<String, Object> outputMap = new LinkedHashMap<>();

			outputMap.put("SelectedReportDetail", reportDetail);
			outputMap.putAll(
					(Map<String, Object>) getReportParameter(reportDetail.getNreportdetailcode(), userInfo).getBody());
			outputMap.putAll((Map<String, Object>) getReportDesignConfig(reportDetail.getNreportdetailcode(), userInfo)
					.getBody());
			outputMap.putAll(
					(Map<String, Object>) getReportParameterMapping(reportDetail.getNreportdetailcode(), userInfo)
							.getBody());
			outputMap.putAll(
					(Map<String, Object>) getReportParameterAction(reportDetail.getNreportdetailcode()).getBody());
			outputMap.putAll((Map<String, Object>) getSubReportDetails(reportDetail.getNreportdetailcode()).getBody());
			outputMap.putAll((Map<String, Object>) getReportImages(reportDetail.getNreportdetailcode()).getBody());
			outputMap.putAll(
					(Map<String, Object>) getControlbasedReportvalidation(reportDetail.getNreportdetailcode(), userInfo)
							.getBody());
			outputMap.putAll(
					(Map<String, Object>) getControlbasedReportParameter(reportDetail.getNreportdetailcode(), userInfo)
							.getBody());

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

	}

	public ResponseEntity<Object> getControlbasedReportvalidation(int nreportvalidationcode, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final String queryString = "select ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransactionstatus,cbr.* from controlbasedreportvalidation cbr,transactionstatus ts where cbr.ntranscode=ts.ntranscode "
				+ "and cbr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cbr.nreportdetailcode=" + nreportvalidationcode + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		final List<ControlbasedReportvalidation> controlbasedreportvalidation = jdbcTemplate.query(queryString,
				new ControlbasedReportvalidation());
		outputMap.put("ReportValidation", controlbasedreportvalidation);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getControlbasedReportParameter(int nreportvalidationcode, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final String queryString = "select pc.*,pc.smappedcolumnname as stablecolumn, "
				+ "rp.sreportparametername as scontrolBasedparameter,rp.nreportparametercode as ncontrolBasedparameter  "
				+ "from   parameterconfiguration pc,reportparameter rp,reportdetails rd  "
				+ "where pc.nreportparametercode=rp.nreportparametercode "
				+ "and pc.nreportdetailcode=rd.nreportdetailcode " + "and pc.nreportdetailcode=" + nreportvalidationcode
				+ " " + "and pc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "order by 1 desc;";

		final List<ParameterConfiguration> controlbasedreportParameterMapping = jdbcTemplate.query(queryString,
				new ParameterConfiguration());
		outputMap.put("ParameterMapping", controlbasedreportParameterMapping);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReportParameter(final int nreportDetailCode, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final String queryString = "select rp.* from reportparameter rp where rp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nreportdetailcode="
				+ nreportDetailCode;

		final List<ReportParameter> parameterList = jdbcTemplate.query(queryString, new ReportParameter());
		for (ReportParameter ParameterList : parameterList) {
			ParameterList.setSdisplaydatatype(commonFunction.getMultilingualMessage(ParameterList.getSdatatype(),
					userInfo.getSlanguagefilename()));
		}
		outputMap.put("ReportParameter", parameterList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReportDesignConfig(final int nreportDetailCode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final String queryString = "select rdc.*, rp.sreportparametername as sreportparametername, coalesce(dc.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',dc.jsondata->'sdisplayname'->>'en-US') as sdesigncomponentname, "
				+ " sq.ssqlqueryname as ssqlqueryname, sq.ssqlquery as ssqlquery,"
				+ " sq.svaluemember as svaluemember, sq.sdisplaymember as sdisplaymember,"
				+ " coalesce (ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as smandatory from "
				+ " reportdesignconfig rdc, reportparameter rp, designcomponents dc, "
				+ " sqlquery sq, transactionstatus ts where  rdc.ndesigncomponentcode = dc.ndesigncomponentcode "
				+ " and rdc.nreportparametercode = rp.nreportparametercode "
				+ " and rdc.nsqlquerycode = sq.nsqlquerycode " + " and rdc.nmandatory = ts.ntranscode and "
				+ " rdc.nstatus=rp.nstatus and rp.nstatus = dc.nstatus and dc.nstatus= sq.nstatus and sq.nstatus = ts.nstatus"
				+ " and ts.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rdc.nreportdetailcode=" + nreportDetailCode;

		final List<ReportDesignConfig> configList = jdbcTemplate.query(queryString, new ReportDesignConfig());
		outputMap.put("ReportDesignConfig", configList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getReportParameterMapping(final int nreportDetailCode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final String queryString = "select rpm.*, rdc1.sdisplayname as sparentparametername,"
				+ " rdc2.sdisplayname as schildparametername,coalesce (ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US')  as sisactionparent "
				+ " from reportparametermapping rpm, "
				+ " reportdesignconfig rdc1 , reportdesignconfig rdc2, transactionstatus ts where "
				+ " rdc1.nreportdesigncode = rpm.nparentreportdesigncode "
				+ " and rdc2.nreportdesigncode = rpm.nchildreportdesigncode " + " and rpm.nstatus = rdc1.nstatus "
				+ " and rpm.nisactionparent = ts.ntranscode" + " and rpm.nreportdetailcode=" + nreportDetailCode
				+ " and rpm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<ReportParameterMapping> mappingList = jdbcTemplate.query(queryString, new ReportParameterMapping());
		outputMap.put("ReportParameterMapping", mappingList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getReportParameterAction(final int nreportDetailCode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final String queryString = "select rpa.*,  rdc.sdisplayname as sparentparametername, "
				+ " rdc2.sdisplayname as schildparametername "
				+ " from reportparameteraction rpa,reportdesignconfig rdc, reportdesignconfig rdc2"
				+ " where rpa.nparentreportdesigncode = rdc.nreportdesigncode "
				+ " and rpa.nreportdesigncode = rdc2.nreportdesigncode " + " and rdc.nstatus = rdc2.nstatus "
				+ " and rdc2.nstatus = rpa.nstatus " + " and rdc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rpa.nreportdetailcode="
				+ nreportDetailCode;
		final List<ReportParameterAction> mappingList = jdbcTemplate.query(queryString, new ReportParameterAction());
		outputMap.put("ReportParameterAction", mappingList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSubReportDetails(final int nreportDetailCode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final String queryString = "select rp.* from subreportdetails rp where rp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nreportdetailcode="
				+ nreportDetailCode;

		final List<SubReportDetails> subReportList = jdbcTemplate.query(queryString, new SubReportDetails());
		outputMap.put("SubReportDetails", subReportList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getReportImages(final int nreportDetailCode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final String queryString = "select ri.* from reportimages ri where ri.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ri.nreportdetailcode="
				+ nreportDetailCode;
		final List<ReportImages> reportImageList = jdbcTemplate.query(queryString, new ReportImages());
		outputMap.put("ReportImages", reportImageList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ReportMaster getActiveReportMasterById(final int nreportCode, UserInfo userInfo) throws Exception {
		final String strQuery = "select rm.* ,ret.isneedregtype,crt.isneedsection, ret.sreporttypename, "
				+ "coalesce(ret.jsondata ->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',ret.jsondata ->'sdisplayname'->>'en-US')  as sreportdisplayname, "
				+ "coalesce(st.jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',st.jsondata->'sampletypename'->>'en-US') as ssampletypename, "
				+ "coalesce(rt.jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename,"
				+ "coalesce(rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,"
				+ "coalesce(crt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "', crt.jsondata->'sdisplayname'->>'en-US') as scoareporttypename,"
				+ "coalesce(rmd.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',rmd.jsondata->'sdisplayname'->>'en-US') as sreportmodulename,"
				+ "s.ssectionname,cm.scolorhexcode ,    " + "coalesce(rmd.jsondata ->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rmd.jsondata ->'sdisplayname'->>'en-US')  as smoduledisplayname, "
				+ "coalesce(ts.jsondata ->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata ->'stransdisplaystatus'->>'en-US')  as sactivestatus, "
				+ "q.nformcode, coalesce(c.jsondata ->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
				+ "',c.jsondata ->'scontrolids'->>'en-US')  as scontrolids,  "
				+ "coalesce(q.jsondata ->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',q.jsondata ->'sdisplayname'->>'en-US')  as sdisplayname,aca.sversionname sapproveversionname,"
				+ "coalesce(rtm.jsondata->'stemplatename'->>'" + userInfo.getSlanguagetypecode()
				+ "', rtm.jsondata->'stemplatename'->>'en-US') as sreporttemplatename "
				+ "from reportmaster rm join reporttype ret on ret.nreporttypecode=rm.nreporttypecode "
				+ "join reportmodule rmd on rmd.nreportmodulecode = rm.nreportmodulecode "
				+ "join sampletype st on  st.nsampletypecode =rm.nsampletypecode  "
				+ "join registrationtype rt on  rt.nregtypecode =rm.nregtypecode  "
				+ "join registrationsubtype rst on rst.nregsubtypecode =rm.nregsubtypecode "
				+ "join approvalconfigautoapproval aca on aca.napprovalconfigversioncode = rm.napproveconfversioncode "
				+ "join coareporttype crt on crt.ncoareporttypecode = rm.ncoareporttypecode "
				+ "join transactionstatus ts  on ts.ntranscode = rm.ntransactionstatus "
				+ "join section s on  s.nsectioncode=rm.nsectioncode "
				+ "join controlmaster c on c.ncontrolcode =rm.ncontrolcode "
				+ "join qualisforms q on q.nformcode =c.nformcode "
				+ "join formwisestatuscolor fsc on fsc.ntranscode = rm.ntransactionstatus  "
				+ "join colormaster cm on  cm.ncolorcode = fsc.ncolorcode  "
				+ "left outer join reporttemplate rtm on rm.nreporttemplatecode = rtm.nreporttemplatecode  and  rtm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " where  " + " rm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rmd.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and rst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and crt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  fsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.nreportcode =" + nreportCode
				+ " and fsc.nformcode = " + Enumeration.FormCode.REPORTCONFIG.getFormCode() + "   ";

		return (ReportMaster) jdbcUtilityFunction.queryForObject(strQuery, ReportMaster.class, jdbcTemplate);

	}

	@Override
	public ReportDetails getActiveReportDetailById(final int reportDetailCode, UserInfo userInfo) throws Exception {
		final String queryString = " select rd.*, case when rd.nversionno> 0 then cast(rd.nversionno as text) else '-' end as sversionno, "
				+ "	 coalesce (ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US')as sdisplaystatus, "
				+ "	 coalesce (ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts2.jsondata->'stransdisplaystatus'->>'en-US')as splsqlquery,cm.scolorhexcode from reportdetails rd, "
				+ "	 transactionstatus ts1, transactionstatus ts2, reportmaster rm,formwisestatuscolor fsc,colormaster cm  "
				+ "	 where rd.nreportcode=rm.nreportcode "
				+ "	 and rd.ntransactionstatus = ts1.ntranscode and rd.nisplsqlquery = ts2.ntranscode  "
				+ "	 and rm.nstatus=  rd.nstatus " + "	 and rd.nstatus=  ts1.nstatus "
				+ "	 and ts1.nstatus = ts2.nstatus " + "	 and ts2.nstatus  = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rd.nreportdetailcode in ("
				+ reportDetailCode + ") " + " and cm.ncolorcode = fsc.ncolorcode " + " and fsc.nformcode =  "
				+ Enumeration.FormCode.REPORTCONFIG.getFormCode() + " and fsc.ntranscode = rm.ntransactionstatus "
				+ " and cm.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and fsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (ReportDetails) jdbcUtilityFunction.queryForObject(queryString, ReportDetails.class, jdbcTemplate);
	}

	private ReportDetails getApprovedReportDetail(final ReportMaster reportMaster) throws Exception {
		final String detailQuery = "select * from reportdetails where nreportcode=" + reportMaster.getNreportcode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
		return (ReportDetails) jdbcUtilityFunction.queryForObject(detailQuery, ReportDetails.class, jdbcTemplate);
	}

	@Override
	@SuppressWarnings({ "unchecked", "unused" })
	public ResponseEntity<Object> getReportMasterComboData(final Integer nreportCode, final UserInfo userInfo,
			final ReportType reportType) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		ReportMaster reportMaster = null;
		if (nreportCode == null) {
			final List<ReportType> reportTypeList = (List<ReportType>) getReportType(userInfo).getBody();

			int reportTypeCode = reportTypeList.get(0).getNreporttypecode();
			ReportType reportTypeObj = reportTypeList.get(0);
			if (reportType.getNreporttypecode() != 0) {
				reportTypeCode = reportType.getNreporttypecode();
				reportTypeObj = reportType;
			}
			int regTypeCode = -1;
			if (reportTypeCode == Enumeration.ReportType.SAMPLE.getReporttype()
					&& reportTypeObj.getIsneedregtype() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				regTypeCode = Enumeration.RegistrationType.PLASMAPOOL.getregtype();
			}

			outputMap.putAll(getComboData(reportTypeObj, nreportCode, -1, -1, -1, reportTypeList, userInfo));

			if (reportType.getNreporttypecode() != 0) {
				outputMap.put("SelectedReportType", reportType);
			}

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			reportMaster = getActiveReportMasterById(nreportCode, userInfo);
			if (reportMaster == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REPORTALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				ReportDetails reportDetails = getApprovedReportDetail(reportMaster);

				final List<ReportType> reportTypeList = (List<ReportType>) getReportType(userInfo).getBody();
				outputMap.put("SelectedReportDesigner", reportMaster);

				final int reportTypeCode = reportMaster.getNreporttypecode();

				int nreportTypeCode = ((reportTypeCode == Enumeration.ReportType.COAPREVIEW.getReporttype())
						|| (reportTypeCode == Enumeration.ReportType.COAPRELIMINARY.getReporttype())
								? Enumeration.ReportType.COA.getReporttype()
								: reportMaster.getNreporttypecode());
				String reportquery = "select * from reporttype where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nreporttypecode="
						+ nreportTypeCode;
				final ReportType reportTypeObj = (ReportType) jdbcUtilityFunction.queryForObject(reportquery,
						ReportType.class, jdbcTemplate);
				final int nregTypeCode = reportMaster.getNregtypecode();
				final int nregsubTypeCode = reportMaster.getNregsubtypecode();
				final int nsampleTypeCode = reportMaster.getNsampletypecode();
				outputMap.putAll(getComboData(reportTypeObj, nreportCode, nregTypeCode, nregsubTypeCode,
						nsampleTypeCode, reportTypeList, userInfo));

				if (reportDetails == null) {
					return new ResponseEntity<>(outputMap, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(outputMap, HttpStatus.ACCEPTED);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getComboData(final ReportType reportType, final Integer reportCode, int regTypeCode,
			int nregsubTypeCode, int nsampleTypeCode, final List<ReportType> reportTypeList, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		final List<ReportDecisionType> decisionTypeList = (List<ReportDecisionType>) getReportDecisionType(userInfo)
				.getBody();

		final List<ReportModule> moduleList = (List<ReportModule>) getReportModule(userInfo).getBody();

		final List<SiteControlMaster> ControlmasterList = (List<SiteControlMaster>) getControlMasterScreen(userInfo)
				.getBody();

		outputMap.put("SectionList", getSectionListBySite(userInfo).getBody());
		outputMap.put("ReportType", reportTypeList);
		outputMap.put("SelectedReportType", reportTypeList.get(0));
		outputMap.put("ReportDecisionType", decisionTypeList);
		outputMap.put("ReportModule", moduleList);
		outputMap.put("ControlScreen", ControlmasterList);

		if (reportType.getNreporttypecode() == Enumeration.ReportType.SAMPLE.getReporttype()
				|| reportType.getNreporttypecode() == Enumeration.ReportType.COA.getReporttype()
				|| reportType.getNreporttypecode() == Enumeration.ReportType.COAPREVIEW.getReporttype()
				|| reportType.getNreporttypecode() == Enumeration.ReportType.COAPRELIMINARY.getReporttype()) {
			if (reportType.getIsneedregtype() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				List<SampleType> sampleTypeList = (List<SampleType>) getSampleTypeList(userInfo).getBody();
				outputMap.put("SampleType", sampleTypeList);
				if (!sampleTypeList.isEmpty()) {
					int nsampletypecode = nsampleTypeCode == -1 ? sampleTypeList.get(0).getNsampletypecode()
							: nsampleTypeCode;
					outputMap.put("SelectedSampleType", sampleTypeList.get(0));
					final Map<String, Object> coaReportTypeList = (Map<String, Object>) getCOAReportType(
							reportType.getNreporttypecode(), nsampletypecode, userInfo).getBody();
					outputMap.put("COAReportType", coaReportTypeList.get("COAReportType"));
					final List<Map<String, Object>> reportTemplateList = (List<Map<String, Object>>) getReportTemplate(
							userInfo).getBody();
					outputMap.put("ReportTemplate", reportTemplateList);
					outputMap.put("CertificateType", coaReportTypeList.get("CertificateType"));
					if (regTypeCode != -1) {
						List<RegistrationType> regTypeList = (List<RegistrationType>) getRegistrationTypeList(userInfo,
								nsampletypecode, regTypeCode).getBody();
						outputMap.put("RegistrationType", regTypeList);

						if (!regTypeList.isEmpty()) {
							RegistrationType regType = null;

							if (regTypeCode == -1
									|| regTypeCode == Enumeration.RegistrationType.PLASMAPOOL.getregtype()) {
								regType = regTypeList.get(0);
							} else {
								regType = regTypeList.stream().filter(item -> item.getNregtypecode() == regTypeCode)
										.collect(Collectors.toList()).get(0);
							}
							outputMap.put("SelectedRegType", regType);
							List<RegistrationSubType> regSubTypeList = (List<RegistrationSubType>) getRegistrationSubTypeByRegType(
									regType.getNregtypecode(), userInfo).getBody();
							outputMap.put("RegistrationSubType", regSubTypeList);
							if (nregsubTypeCode != -1) {
								RegistrationSubType regSubType = regSubTypeList.stream()
										.filter(item -> item.getNregsubtypecode() == nregsubTypeCode)
										.collect(Collectors.toList()).get(0);
								outputMap.put("ApprovalConfigVersion",
										getReportRegSubTypeApproveConfigVersion(regType.getNregtypecode(),
												regSubType.getNregsubtypecode(), userInfo).getBody());
							}

						}
					}
				}
			}
		}

		return outputMap;
	}

	public ResponseEntity<Object> getControlMasterScreen(UserInfo userinfo) throws Exception {
		String queryString = " select  coalesce (q.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
				+ "',q.jsondata->'sdisplayname'->>'en-US')as sdisplayname,"
				+ " q.nformcode  from controlmaster c,qualisforms q,sitecontrolmaster sc" + " where nisreportcontrol="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "  and "
				+ " sc.nformcode=c.nformcode and  sc.nsitecode=" + userinfo.getNmastersitecode()
				+ " and c.nformcode=q.nformcode  and sc.ncontrolcode=c.ncontrolcode" + " and sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by q.nformcode  ";

		return new ResponseEntity<>(jdbcTemplate.query(queryString, new SiteControlMaster()), HttpStatus.OK);

	}

	public ResponseEntity<Object> getReportType(UserInfo userInfo) throws Exception {
		final String queryString = "select rt.*,coalesce (rt.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rt.jsondata->'sdisplayname'->>'en-US')as sdisplayname from reporttype rt where rt.nreporttypecode > 0 "
				+ "and rt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by rt.nreporttypecode";
		return new ResponseEntity<>(jdbcTemplate.query(queryString, new ReportType()), HttpStatus.OK);
	}

	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> getCOAReportType(final int nreportTypeCode, final int nsampletypecode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		String queryString = "select ct.isneedsection,ct.ncoareporttypecode, ct.nreporttypecode,coalesce (ct.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ct.jsondata->'sdisplayname'->>'en-US') as scoareporttypename ,coalesce (ct.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ct.jsondata->'sdisplayname'->>'en-US') as sdisplayname from coareporttype ct,samplecoareporttype sct"
				+ " where ct.ncoareporttypecode > 0 and sct.ncoareporttypecode=ct.ncoareporttypecode and ct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ct.nreporttypecode="
				+ nreportTypeCode + " " + " and sct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sct.nsampletypecode="
				+ nsampletypecode;
		String strQuery = "";
		if (nreportTypeCode == Enumeration.ReportType.SAMPLE.getReporttype()) {

			strQuery = " select ct.*,coalesce (rbt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "',rbt.jsondata->'sdisplayname'->>'en-US') as sbatchdisplayname  from certificatetype ct, "
					+ " certificatereporttype rbt where "
					+ " ct.ncertificatereporttypecode = rbt.ncertificatereporttypecode "
					+ " and ct.nstatus = rbt.nstatus " + " and ct.ncertificatetypecode >0" + " and rbt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ct.ncertificatereporttypecode in  " + "("
					+ Enumeration.CertificateReportType.PLASMAPOOL_PASS.getNCertificateReportType() + "" + ","
					+ Enumeration.CertificateReportType.PLASMAPOOL_FAIL.getNCertificateReportType() + "" + ","
					+ Enumeration.CertificateReportType.PLASMAPOOL_WITHDRAWN.getNCertificateReportType() + ")";
		} else {
			strQuery = " select ct.*, coalesce (rbt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "',rbt.jsondata->'sdisplayname'->>'en-US') as sbatchdisplayname  from certificatetype ct, "
					+ " certificatereporttype rbt where "
					+ " ct.ncertificatereporttypecode = rbt.ncertificatereporttypecode "
					+ " and ct.nstatus = rbt.nstatus " + " and rbt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.ncertificatetypecode >0"
					+ " and ct.ncertificatereporttypecode not in  " + "("
					+ Enumeration.CertificateReportType.PLASMAPOOL_PASS.getNCertificateReportType() + "" + ","
					+ Enumeration.CertificateReportType.PLASMAPOOL_FAIL.getNCertificateReportType() + "" + ","
					+ Enumeration.CertificateReportType.PLASMAPOOL_WITHDRAWN.getNCertificateReportType() + ")";
		}
		final List<CertificateType> certificateTypeList = null; // jdbcTemplate.query(strQuery, new CertificateType());
		outputMap.put("CertificateType", certificateTypeList);
		final List<COAReportType> cc = jdbcTemplate.query(queryString, new COAReportType());

		outputMap.put("COAReportType", jdbcTemplate.query(queryString, new COAReportType()));

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getReportDecisionType(UserInfo userInfo) throws Exception {
		final String queryString = "select *,coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as sdisplayname from reportdecisiontype where nreportdecisiontypecode > 0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<>(jdbcTemplate.query(queryString, new ReportDecisionType()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReportModule(UserInfo userInfo) throws Exception {
		String queryString = "select *,coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as sdisplayname from reportmodule rm where rm.nreportmodulecode > 0  and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<>(jdbcTemplate.query(queryString, new ReportModule()), HttpStatus.OK);

	}

	public ResponseEntity<Object> getRegistrationTypeList(final UserInfo userInfo, final int nsampleTypeCode,
			final int nregTypeCode) throws Exception {

		List<RegistrationType> regTypeList = null;
		String queryString = "";
		if (nregTypeCode == Enumeration.RegistrationType.PLASMAPOOL.getregtype()) {
			queryString = "select *,coalesce(jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
					+ "',jsondata->'sregtypename'->>'en-US') as sregtypename "
					+ " from registrationtype where nregtypecode = " + nregTypeCode + " and nsitecode = "
					+ userInfo.getNmastersitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		} else {
			queryString = "select *,coalesce(jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
					+ "',jsondata->'sregtypename'->>'en-US') as sregtypename"
					+ "  from registrationtype where nregtypecode > 0 and nsampletypecode=" + nsampleTypeCode + " "
					+ " and nsitecode = " + userInfo.getNmastersitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		regTypeList = jdbcTemplate.query(queryString, new RegistrationType());
		return new ResponseEntity<>(regTypeList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegistrationSubTypeByRegType(final int nregTypeCode, final UserInfo userInfo)
			throws Exception {
		final String queryString = "select *,coalesce(jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sregsubtypename'->>'en-US') as sregsubtypename"
				+ " from registrationsubtype where nregtypecode= " + nregTypeCode + " and nsitecode = "
				+ +userInfo.getNmastersitecode() + " and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<RegistrationSubType> regSubTypeList = jdbcTemplate.query(queryString, new RegistrationSubType());
		return new ResponseEntity<>(regSubTypeList, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSectionListBySite(final UserInfo userInfo) throws Exception {

		final String strQuery = "select s.* from section s where s.nsectioncode>0 and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode = "
				+ userInfo.getNmastersitecode();

		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new Section()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createReportDesigner(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {

		final String sQueryLock = " lock  table lockreportdesigner "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final ObjectMapper objectMapper = new ObjectMapper();

		ReportMaster reportMaster = objectMapper.readValue(request.getParameter("reportmaster"),
				new TypeReference<ReportMaster>() {
				});
		ReportDetails reportDetails = objectMapper.readValue(request.getParameter("reportdetails"),
				new TypeReference<ReportDetails>() {
				});

		final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);

		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {

			final ReportMaster reportByName = getReportMasterByReportName(reportMaster, userInfo.getNmastersitecode());

			if (reportByName == null) {
				if (reportMaster.getSmoduledisplayname() != null) {

					final String nameQuery = "select * from reportmodule where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sreportmodulename=N'"
							+ stringUtilityFunction.replaceQuote(reportMaster.getSmoduledisplayname()) + "'";
					final ReportModule moduleByName = (ReportModule) jdbcUtilityFunction.queryForObject(nameQuery,
							ReportModule.class, jdbcTemplate);

					if (moduleByName == null) {

						final String moduleQuery = "select max(nsorter) from reportmodule where nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final int nsorter = jdbcTemplate.queryForObject(moduleQuery, Integer.class);

						ReportModule reportModule = new ReportModule();
						reportModule.setSreportmodulename(reportMaster.getSmoduledisplayname());
						reportModule.setSdisplayname(reportMaster.getSmoduledisplayname());
						reportModule.setNsorter((short) (nsorter + 1));
						reportModule.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
						reportModule.setJsondata(reportMaster.getJsondata());

						String sequencequery = "select nsequenceno from seqnoreportmanagement where stablename ='reportmodule'";
						int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
						nsequenceno++;

						String insertquery = "Insert into reportmodule (nreportmodulecode, sreportmodulename, jsondata, nsorter, dmodifieddate, nsitecode, nstatus) values"
								+ "(" + nsequenceno + ", N'"
								+ stringUtilityFunction.replaceQuote(reportModule.getSreportmodulename()) + "', '"
								+ stringUtilityFunction.replaceQuote(
										objectMapper.writeValueAsString(reportModule.getJsondata()))
								+ "', " + reportModule.getNsorter() + ", '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNmastersitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
						jdbcTemplate.execute(insertquery);

						String updatequery = "update seqnoreportmanagement set nsequenceno =" + nsequenceno
								+ " where stablename='reportmodule'";
						jdbcTemplate.execute(updatequery);

						reportMaster.setNreportmodulecode(nsequenceno);

					} else {
						reportMaster.setNreportmodulecode(moduleByName.getNreportmodulecode());
					}
				}
				if (reportMaster.getNtransactionstatus() == Enumeration.TransactionStatus.ACTIVE
						.gettransactionstatus()) {
					if (reportMaster.getNreporttypecode() != Enumeration.ReportType.MIS.getReporttype()) {
						final ReportMaster activeStatusReport = getReportByActiveStatus(reportMaster);
						if (activeStatusReport != null) {

							ReportMaster deactivatedReportMaster = SerializationUtils.clone(activeStatusReport);
							deactivatedReportMaster.setNtransactionstatus(
									Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus());

							final String updateQueryString = "update reportmaster set " + " ntransactionstatus="
									+ deactivatedReportMaster.getNtransactionstatus() + ", dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportcode = "
									+ deactivatedReportMaster.getNreportcode();

							jdbcTemplate.execute(updateQueryString);
							auditUtilityFunction.fnInsertAuditAction(Arrays.asList(deactivatedReportMaster), 2,
									Arrays.asList(activeStatusReport), Arrays.asList("IDS_EDITREPORTMASTER"), userInfo);
						}
					}
				} else {
					final String query = "select * from reportmaster where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nreporttypecode="
							+ reportMaster.getNreporttypecode() + " and ncoareporttypecode="
							+ reportMaster.getNcoareporttypecode() + " and ncertificatetypecode="
							+ reportMaster.getNcertificatetypecode() + " and ncontrolcode="
							+ reportMaster.getNcontrolcode();
					final List<ReportMaster> list = jdbcTemplate.query(query, new ReportMaster());
					if (list.isEmpty()) {
						reportMaster.setNtransactionstatus(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
					}
				}
				String seqreportmasterquery = "select nsequenceno from SeqNoReportManagement where stablename='reportmaster'";
				int nsequencereportmastercount = jdbcTemplate.queryForObject(seqreportmasterquery, Integer.class);
				nsequencereportmastercount++;
				String insertreportmasterquery = "Insert into reportmaster (nreportcode,ncertificatetypecode,ncoareporttypecode,ncontrolcode,nsampletypecode,nregsubtypecode,nregtypecode,napproveconfversioncode,nreportdecisiontypecode,"
						+ "nreportmodulecode,nreporttypecode,nsectioncode,nsitecode,nstatus,ntransactionstatus,sdescription,sreportname,dmodifieddate,nreporttemplatecode)"
						+ "values(" + nsequencereportmastercount + "," + reportMaster.getNcertificatetypecode() + ","
						+ reportMaster.getNcoareporttypecode() + "," + reportMaster.getNcontrolcode() + ","
						+ reportMaster.getNsampletypecode() + "," + reportMaster.getNregsubtypecode() + ","
						+ reportMaster.getNregtypecode() + "," + reportMaster.getNapproveconfversioncode() + ","
						+ reportMaster.getNreportdecisiontypecode() + "," + " " + reportMaster.getNreportmodulecode()
						+ "," + reportMaster.getNreporttypecode() + "," + reportMaster.getNsectioncode() + ","
						+ userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ reportMaster.getNtransactionstatus() + ",N'"
						+ stringUtilityFunction.replaceQuote(reportMaster.getSdescription()) + "',N'"
						+ stringUtilityFunction.replaceQuote(reportMaster.getSreportname()) + "','"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ reportMaster.getNreporttemplatecode() + ")";
				jdbcTemplate.execute(insertreportmasterquery);

				String updatereportmasterquery = "update SeqNoReportManagement set nsequenceno="
						+ nsequencereportmastercount + " where stablename='reportmaster'";
				jdbcTemplate.execute(updatereportmasterquery);
				reportMaster.setNreportcode(nsequencereportmastercount);

				auditUtilityFunction.fnInsertAuditAction(Arrays.asList(reportMaster), 1, null,
						Arrays.asList("IDS_ADDREPORTMASTER"), userInfo);

				reportDetails.setNreportcode(reportMaster.getNreportcode());

				String seqreportdetailquery = "select nsequenceno from SeqNoReportManagement where stablename='reportdetails'";
				int nsequencereportdetailcount = jdbcTemplate.queryForObject(seqreportdetailquery, Integer.class);
				nsequencereportdetailcount++;
				String insertreportdetailquery = "Insert into reportdetails (nreportdetailcode,nisplsqlquery,nreportcode,nstatus,ntransactionstatus,nversionno,sfilename,ssystemfilename,dmodifieddate,nsitecode,sreportformatdetail)"
						+ "values(" + nsequencereportdetailcount + "," + reportDetails.getNisplsqlquery() + ","
						+ reportDetails.getNreportcode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ reportDetails.getNtransactionstatus() + "," + reportDetails.getNversionno() + ", " + " N'"
						+ stringUtilityFunction.replaceQuote(reportDetails.getSfilename()) + "',N'"
						+ stringUtilityFunction.replaceQuote(reportDetails.getSsystemfilename()) + "', '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
						+ ",'" + reportDetails.getSreportformatdetail() + "')";
				jdbcTemplate.execute(insertreportdetailquery);

				String updatereportdetailquery = "update SeqNoReportManagement set nsequenceno="
						+ nsequencereportdetailcount + " where stablename='reportdetails'";
				jdbcTemplate.execute(updatereportdetailquery);

				reportDetails.setNreportdetailcode(nsequencereportdetailcount);

				createReportParameter(reportDetails, userInfo);

				return getReportMaster(null, Integer.toString(reportMaster.getNreporttypecode()), userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	private ReportMaster getReportByActiveStatus(final ReportMaster reportMaster) throws Exception {

		String query = "";

		if (reportMaster.getNreportcode() == 0) {
			query = "select * from reportmaster where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nreporttypecode="
					+ reportMaster.getNreporttypecode() + " and ncoareporttypecode="
					+ reportMaster.getNcoareporttypecode() + " and ncertificatetypecode="
					+ reportMaster.getNcertificatetypecode() + " and ncontrolcode=" + reportMaster.getNcontrolcode()
					+ " and ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nregsubtypecode=" + reportMaster.getNregsubtypecode() + " and nregtypecode="
					+ reportMaster.getNregtypecode() + " and ncoareporttypecode=" + reportMaster.getNcoareporttypecode()
					+ " " + " and nreporttemplatecode=" + reportMaster.getNreporttemplatecode() + " and nsectioncode="
					+ reportMaster.getNsectioncode() + " and napproveconfversioncode="
					+ reportMaster.getNapproveconfversioncode() + " and nsampletypecode="
					+ reportMaster.getNsampletypecode();

		}

		else {
			query = "select * from reportmaster where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nreporttypecode="
					+ reportMaster.getNreporttypecode() + " and ncoareporttypecode="
					+ reportMaster.getNcoareporttypecode() + " and ncertificatetypecode="
					+ reportMaster.getNcertificatetypecode() + " and ncontrolcode=" + reportMaster.getNcontrolcode()
					+ " and nreportcode <> " + reportMaster.getNreportcode() + " and ntransactionstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregsubtypecode="
					+ reportMaster.getNregsubtypecode() + " and nregtypecode=" + reportMaster.getNregtypecode()
					+ " and ncoareporttypecode=" + reportMaster.getNcoareporttypecode() + " "
					+ " and nreporttemplatecode=" + reportMaster.getNreporttemplatecode() + " and nsectioncode="
					+ reportMaster.getNsectioncode() + " and napproveconfversioncode="
					+ reportMaster.getNapproveconfversioncode() + " and nsampletypecode="
					+ reportMaster.getNsampletypecode();
		}
		return (ReportMaster) jdbcUtilityFunction.queryForObject(query, ReportMaster.class, jdbcTemplate);
	}

	public ReportMaster getReportMasterByReportName(final ReportMaster reportMaster, final int nsiteCode)
			throws Exception {
		final String queryString = "select * from reportmaster where sreportname=N'"
				+ stringUtilityFunction.replaceQuote(reportMaster.getSreportname()) + "' and  nreporttypecode="
				+ reportMaster.getNreporttypecode() + " and nsitecode = " + nsiteCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (ReportMaster) jdbcUtilityFunction.queryForObject(queryString, ReportMaster.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> createReportDetails(final MultipartHttpServletRequest request,
			ReportDetails reportDetails, final ReportMaster reportMaster, final Integer getId, final UserInfo userInfo)
			throws Exception {

		final String sQueryLock = " lock  table lockreportdesigner "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);

		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {
			reportDetails.setNreportcode(reportMaster.getNreportcode());

			String seqreportdetailquery = "select nsequenceno from SeqNoReportManagement where stablename='reportdetails'";
			int nsequencereportdetailcount = jdbcTemplate.queryForObject(seqreportdetailquery, Integer.class);
			nsequencereportdetailcount++;
			String insertreportdetailquery = "Insert  into reportdetails (nreportdetailcode,nisplsqlquery,nreportcode,nstatus,ntransactionstatus,nversionno,sfilename,ssystemfilename,dmodifieddate,nsitecode,sreportformatdetail)"
					+ "values(" + nsequencereportdetailcount + "," + reportDetails.getNisplsqlquery() + ","
					+ reportDetails.getNreportcode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "," + reportDetails.getNtransactionstatus() + "," + reportDetails.getNversionno() + ", " + " N'"
					+ stringUtilityFunction.replaceQuote(reportDetails.getSfilename()) + "',N'"
					+ stringUtilityFunction.replaceQuote(reportDetails.getSsystemfilename()) + "', '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ",'"
					+ reportDetails.getSreportformatdetail() + "'); ";
			jdbcTemplate.execute(insertreportdetailquery);

			String updatereportdetailquery = "update SeqNoReportManagement set nsequenceno="
					+ nsequencereportdetailcount + " where stablename='reportdetails'";
			jdbcTemplate.execute(updatereportdetailquery);

			reportDetails.setNreportdetailcode(nsequencereportdetailcount);

			createReportParameter(reportDetails, userInfo);

			return getReportMaster(getId, reportMaster.getSfilterreporttypecode(), userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateReportDetails(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		ReportMaster reportMaster = objMapper.readValue(request.getParameter("reportmaster"),
				new TypeReference<ReportMaster>() {
				});
		ReportDetails reportDetails = objMapper.readValue(request.getParameter("reportdetails"),
				new TypeReference<ReportDetails>() {
				});

		final ReportMaster reportById = getActiveReportMasterById(reportMaster.getNreportcode(), userInfo);
		if (reportById == null) {
			// status code= 417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REPORTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ReportDetails detailById = getActiveReportDetailById(reportDetails.getNreportdetailcode(), userInfo);

			if (detailById == null) {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REPORTDETAILALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {

				if (detailById.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
						.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTEDITAPPROVEDREPORT",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				} else if (detailById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
						.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTEDITRETIREDREPORT",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				} else {
					String uploadStatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

					final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
					if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						uploadStatus = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
					}
					if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {

						List<String> deleteList = new ArrayList<String>();
						if (reportDetails.getSsystemfilename() == null
								|| !reportDetails.getSsystemfilename().equals(detailById.getSsystemfilename())) {
							deleteList.add(detailById.getSsystemfilename());
						}

						deleteList.remove(null);
						if (!deleteList.isEmpty()) {
								ftpUtilityFunction.deleteFTPFile(deleteList, "reports", userInfo);
						}
						reportDetails.setNreportcode(reportMaster.getNreportcode());
						reportDetails.setNstatus(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

						final String query = "update reportdetails set sfilename ='" + reportDetails.getSfilename()
								+ "' , ssystemfilename= '" + reportDetails.getSsystemfilename() + "' ,"
								+ "nisplsqlquery=" + reportDetails.getNisplsqlquery() + ", dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',sreportformatdetail='"
								+ reportDetails.getSreportformatdetail() + "'  where nreportdetailcode = "
								+ reportDetails.getNreportdetailcode() + " ";

						jdbcTemplate.execute(query);

						auditUtilityFunction.fnInsertAuditAction(Arrays.asList(reportDetails), 2,
								Arrays.asList(detailById), Arrays.asList("IDS_EDITREPORTDETAILS"), userInfo);

						final Map<String, Object> mapObject = (Map<String, Object>) getReportParameter(
								reportDetails.getNreportdetailcode(), userInfo).getBody();
						final List<ReportParameter> parameterList = (List<ReportParameter>) mapObject
								.get("ReportParameter");
						if (parameterList.isEmpty()) {
							createReportParameter(reportDetails, userInfo);
						} else {
							final String paramDelQuery = "delete from reportparameter where nreportdetailcode ="
									+ reportDetails.getNreportdetailcode() + " and nstatus ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							jdbcTemplate.execute(paramDelQuery);

							auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(parameterList), 1, null,
									Arrays.asList("IDS_DELETEREPORTPARAMETER"), userInfo);

							createReportParameter(reportDetails, userInfo);
						}
						final Map<String, Object> outputMap = new HashMap<String, Object>();
						outputMap.putAll(
								(Map<String, Object>) getReportDetail(reportDetails.getNreportdetailcode(), userInfo)
										.getBody());
						return new ResponseEntity<>(outputMap, HttpStatus.OK);

					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				}

			}
		}
	}

	@Override
	public ResponseEntity<Object> getReportDetailComboData(final Integer nreportDetailCode,
			final ReportMaster reportMaster, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		ReportDetails reportDetail = null;
		if (nreportDetailCode == null) {
			// Add
			final int nreportTypeCode = reportMaster.getNreporttypecode();

			String ReportTypeQuery = "select * from reporttype where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nreporttypecode="
					+ nreportTypeCode;
			final ReportType reportType = jdbcTemplate.queryForObject(ReportTypeQuery, new ReportType());
			outputMap.put("SelectedReportType", reportType);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			// Edit
			reportDetail = getActiveReportDetailById(nreportDetailCode, userInfo);
			if (reportDetail == null) {
				// status code= 417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				if (reportDetail.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					outputMap.put("SelectedReportDetail", reportDetail);

					final int nreportTypeCode = reportMaster.getNreporttypecode();
					String ReportTypQuery = "select * from reporttype where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nreporttypecode="
							+ nreportTypeCode;
					final ReportType reportType = jdbcTemplate.queryForObject(ReportTypQuery, new ReportType());
					outputMap.put("SelectedReportType", reportType);
					return new ResponseEntity<>(outputMap, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTEDITAPPROVEDREPORT",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
	}

	@Override
	public ResponseEntity<Object> updateReportMaster(final ReportMaster reportMaster, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final ReportMaster reportById = getActiveReportMasterById(reportMaster.getNreportcode(), userInfo);
		if (reportById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REPORTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ReportDetails reportDetails = getApprovedReportDetail(reportById);
			if (reportDetails == null) {
				final String queryString = "select * from reportmaster where sreportname=N'"
						+ stringUtilityFunction.replaceQuote(reportMaster.getSreportname()) + "' and nreporttypecode="
						+ reportMaster.getNreporttypecode() + " and nreportcode <>" + reportMaster.getNreportcode()
						+ " and nsitecode = " + userInfo.getNmastersitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final ReportMaster reportByName = (ReportMaster) jdbcUtilityFunction.queryForObject(queryString,
						ReportMaster.class, jdbcTemplate);

				if (reportByName == null) {
					if (reportMaster.getNreporttypecode() == Enumeration.ReportType.MIS.getReporttype()
							&& reportMaster.getNreportmodulecode() == 0) {

						final String nameQuery = "select * from reportmodule where nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and sreportmodulename=N'"
								+ stringUtilityFunction.replaceQuote(reportMaster.getSmoduledisplayname()) + "'";
						final ReportModule moduleByName = (ReportModule) jdbcUtilityFunction.queryForObject(nameQuery,
								ReportModule.class, jdbcTemplate);

						if (moduleByName == null) {

							final String moduleQuery = "select max(nsorter) from reportmodule where nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							final int nsorter = jdbcTemplate.queryForObject(moduleQuery, Integer.class);

							ReportModule reportModule = new ReportModule();
							reportModule.setSreportmodulename(reportMaster.getSmoduledisplayname());
							reportModule.setSdisplayname(reportMaster.getSmoduledisplayname());
							reportModule.setNsorter((short) (nsorter + 1));
							reportModule
									.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
							reportModule.setJsondata(reportMaster.getJsondata());

							String sequencequery = "select nsequenceno from seqnoreportmanagement where stablename ='reportmodule'";
							int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
							nsequenceno++;

							String insertquery = "Insert into reportmodule (nreportmodulecode, sreportmodulename, jsondata, nsorter, dmodifieddate, nsitecode, nstatus) values"
									+ "(" + nsequenceno + ", N'"
									+ stringUtilityFunction.replaceQuote(reportModule.getSreportmodulename()) + "', '"
									+ stringUtilityFunction
											.replaceQuote(objMapper.writeValueAsString(reportModule.getJsondata()))
									+ "', " + reportModule.getNsorter() + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
									+ userInfo.getNmastersitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
							jdbcTemplate.execute(insertquery);

							String updatequery = "update seqnoreportmanagement set nsequenceno =" + nsequenceno
									+ " where stablename='reportmodule'";
							jdbcTemplate.execute(updatequery);

							reportMaster.setNreportmodulecode(nsequenceno);
						} else {
							reportMaster.setNreportmodulecode(moduleByName.getNreportmodulecode());
						}
					}

					if (reportMaster.getNtransactionstatus() == Enumeration.TransactionStatus.ACTIVE
							.gettransactionstatus()) {
						if (reportMaster.getNreporttypecode() != Enumeration.ReportType.MIS.getReporttype()) {

							final ReportMaster activeStatusReport = getReportByActiveStatus(reportMaster);
							if (activeStatusReport != null) {

								ReportMaster deactivatedReportMaster = SerializationUtils.clone(activeStatusReport);
								deactivatedReportMaster.setNtransactionstatus(
										Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus());

								final String updateQueryString = "update reportmaster set " + " ntransactionstatus="
										+ deactivatedReportMaster.getNtransactionstatus() + ", dmodifieddate ='"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportcode = "
										+ deactivatedReportMaster.getNreportcode();

								jdbcTemplate.execute(updateQueryString);
								auditUtilityFunction.fnInsertAuditAction(Arrays.asList(deactivatedReportMaster), 2,
										Arrays.asList(activeStatusReport), Arrays.asList("IDS_EDITREPORTMASTER"),
										userInfo);
							}
						}
					}

					final String updateQueryString = "update reportmaster set " + " sreportname= N'"
							+ stringUtilityFunction.replaceQuote(reportMaster.getSreportname()) + "', sdescription = N'"
							+ stringUtilityFunction.replaceQuote(reportMaster.getSdescription()) + "',nsampletypecode="
							+ reportMaster.getNsampletypecode() + ", nregtypecode=" + reportMaster.getNregtypecode()
							+ ", nregsubtypecode=" + reportMaster.getNregsubtypecode() + ",napproveconfversioncode="
							+ reportMaster.getNapproveconfversioncode() + ", nreportmodulecode="
							+ reportMaster.getNreportmodulecode() + ", ntransactionstatus="
							+ reportMaster.getNtransactionstatus() + ", ncoareporttypecode = "
							+ reportMaster.getNcoareporttypecode() + ", nsectioncode=" + reportMaster.getNsectioncode()
							+ ", nreportdecisiontypecode=" + reportMaster.getNreportdecisiontypecode()
							+ ", ncertificatetypecode=" + reportMaster.getNcertificatetypecode() + ", ncontrolcode="
							+ reportMaster.getNcontrolcode() + "," + " dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " nreporttemplatecode="
							+ reportMaster.getNreporttemplatecode() + " where nreportcode = "
							+ reportMaster.getNreportcode();

					jdbcTemplate.execute(updateQueryString);

					auditUtilityFunction.fnInsertAuditAction(Arrays.asList(reportMaster), 2, Arrays.asList(reportById),
							Arrays.asList("IDS_EDITREPORTMASTER"), userInfo);

					return getReportMaster(reportMaster.getNreportcode(), reportMaster.getSfilterreporttypecode(),
							userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				if (reportMaster.getNtransactionstatus() == reportById.getNtransactionstatus()) {

					String msg = "";
					if (reportMaster.getNtransactionstatus() == Enumeration.TransactionStatus.ACTIVE
							.gettransactionstatus()) {
						msg = "IDS_REPORTALREADYACTIVATED";
					} else {
						msg = "IDS_REPORTALREADYDEACTIVATED";
					}
					// status code= 417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(msg, userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					if (reportMaster.getNtransactionstatus() == Enumeration.TransactionStatus.ACTIVE
							.gettransactionstatus()) {
						if (reportMaster.getNreporttypecode() != Enumeration.ReportType.MIS.getReporttype()) {

							final ReportMaster activeStatusReport = getReportByActiveStatus(reportMaster);
							if (activeStatusReport != null) {

								ReportMaster deactivatedReportMaster = SerializationUtils.clone(activeStatusReport);
								deactivatedReportMaster.setNtransactionstatus(
										Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus());

								final String updateQueryString = "update reportmaster set " + " ntransactionstatus="
										+ deactivatedReportMaster.getNtransactionstatus() + ", dmodifieddate ='"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportcode = "
										+ deactivatedReportMaster.getNreportcode();

								jdbcTemplate.execute(updateQueryString);
								auditUtilityFunction.fnInsertAuditAction(Arrays.asList(deactivatedReportMaster), 2,
										Arrays.asList(activeStatusReport), Arrays.asList("IDS_EDITREPORTMASTER"),
										userInfo);
							}
						}
					}
					final ReportMaster masterBeforeUpdate =SerializationUtils.clone(reportById);
					reportById.setNtransactionstatus(reportMaster.getNtransactionstatus());

					final String updateQueryString = "update reportmaster set ntransactionstatus="
							+ reportById.getNtransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportcode = "
							+ reportMaster.getNreportcode();

					jdbcTemplate.execute(updateQueryString);

					auditUtilityFunction.fnInsertAuditAction(Arrays.asList(reportById), 2,
							Arrays.asList(masterBeforeUpdate), Arrays.asList("IDS_EDITREPORTMASTER"), userInfo);
					return getReportMaster(reportMaster.getNreportcode(), reportMaster.getSfilterreporttypecode(),
							userInfo);

				}
			}
		}
	}

	@SuppressWarnings({ "unused" })
	private void createReportParameter(ReportDetails reportDetail, final UserInfo userInfo) throws Exception {

		final String sQueryLock = " lock  table lockreportdesigner "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);
		//need to change JRProperties,JRQueryExecuterFactory
//		JRProperties.setProperty(JRQueryExecuterFactory.QUERY_EXECUTER_FACTORY_PREFIX + "plsql",
//				"com.jaspersoft.jrx.query.PlSqlQueryExecuterFactory");
		final String absolutePath1 = ftpUtilityFunction.getFTPFileWritingPath("", null);

		String sFileType = "";
		String filename = "";
		Settings objSettingsS3 = cloudStorageUtilityFunction.getSetting(userInfo);
		if(Integer.valueOf(objSettingsS3.getSsettingvalue()) == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

			filename = reportDetail.getSsystemfilename();
			sFileType = filename.substring(filename.indexOf("."));
			
		} else {
			final Map<String, Object> signMap = ftpUtilityFunction.FileViewUsingFtp(reportDetail.getSsystemfilename(), -1,
					userInfo, "", "");
			filename = (String) signMap.get("AttachFile");

			sFileType = filename.substring(filename.indexOf("."));
		}

		List<ReportParameter> parameterList = new ArrayList<ReportParameter>();
		SeqNoReportManagement objSequence = (SeqNoReportManagement) jdbcUtilityFunction.queryForObject(
				"select * from SeqNoReportManagement where stablename='reportparameter'",
				SeqNoReportManagement.class, jdbcTemplate);
		int sequence = objSequence.getNsequenceno();
		String strReportParamter = "";

		if (sFileType.compareTo(".jrxml") == 0) {
			//need to change JasperDesign,JRXmlLoader,JasperReport,JasperCompileManager,JRParameter
//			final JasperDesign jasperDesign = JRXmlLoader.load(absolutePath1 + filename);
//			final JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
//			final List<JRParameter> jasperParameterList = Arrays.asList(jasperReport.getParameters());
//			parameterList = new ArrayList<ReportParameter>();
//			for (JRParameter jasperParameter : jasperParameterList) {
//				if (jasperParameter.isSystemDefined() != true) {
//
//					if (!jasperParameter.getName().toString().toString().equals("SUBREPORT_DIR")) {
//
//						final ReportParameter reportParameter = new ReportParameter();
//						reportParameter.setNreportdetailcode(reportDetail.getNreportdetailcode());
//						reportParameter.setNstatus(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
//						reportParameter.setSdatatype(jasperParameter.getValueClassName());
//						reportParameter.setSreportparametername(jasperParameter.getName());
//						reportParameter.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
//						parameterList.add(reportParameter);
//					} else {
//						System.out.println("subreport:" + jasperParameter);
//					}
//				}
//			}
			
		} else {
			JSONObject jsobObj = JSONParsing(absolutePath1 + filename);
			if (jsobObj != null) {
				Iterator<String> keys = jsobObj.keys();

				while (keys.hasNext()) {
					String key = keys.next();
					if (jsobObj.get(key) instanceof JSONObject) {
						JSONObject jsobObjVariable = (JSONObject) jsobObj.get(key);
						String sType = jsobObjVariable.get("Type").toString();
						String sName = jsobObjVariable.get("Name").toString();

						final ReportParameter reportParameter = new ReportParameter();
						reportParameter.setNreportdetailcode(reportDetail.getNreportdetailcode());
						reportParameter.setNstatus(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
						reportParameter.setSdatatype(jsobObjVariable.get("Type").toString());
						reportParameter.setSreportparametername(jsobObjVariable.get("Name").toString());
						reportParameter.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						sequence++;
						strReportParamter += " (" + sequence + ", " + reportParameter.getNreportdetailcode() + ", '"
								+ reportParameter.getSdatatype() + "', '"+reportParameter.getSreportparametername()+"','"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ reportParameter.getNsitecode() + ", " + reportParameter.getNstatus() + "),";
						
						parameterList.add(reportParameter);
						System.out.println(sName);
					}
				}
			}
		}
		if (!parameterList.isEmpty()) {
			String strReportParamterInsertQry = "insert into reportparameter "
					+ "(nreportparametercode, nreportdetailcode, sdatatype, sreportparametername, dmodifieddate, nsitecode, nstatus) values "
					+ strReportParamter.subSequence(0, strReportParamter.length() - 1) + ";";
			strReportParamterInsertQry += "update SeqNoReportManagement set nsequenceno=" + sequence
					+ " where stablename='reportparameter';";
			jdbcTemplate.execute(strReportParamterInsertQry);
//			parameterList = (List<ReportParameter>) insertBatch(parameterList, SeqNoReportManagement.class,
//					"nsequenceno");

			for (ReportParameter ParameterList : parameterList) {
				ParameterList.setSdisplaydatatype(commonFunction.getMultilingualMessage(ParameterList.getSdatatype(),
						userInfo.getSlanguagefilename()));
			}
			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(parameterList), 1, null,
					Arrays.asList("IDS_ADDREPORTPARAMETER"), userInfo);

		}
	}

	@SuppressWarnings("unused")
	private JSONObject JSONParsing(String FilePath) throws IOException {
		String a = "", b = "";
		File f = new File(FilePath);
		JSONObject jsonVariables = null;
		if (f.exists()) {

			InputStream is = new FileInputStream(FilePath);
			String jsonTxt = IOUtils.toString(is, "UTF-8");
			JSONObject json = new JSONObject(jsonTxt);
			JSONObject jsonDictionary = new JSONObject(json.get("Dictionary").toString());
			if (jsonDictionary.has("Variables"))
				jsonVariables = new JSONObject(jsonDictionary.get("Variables").toString());
		}
		return jsonVariables;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getReportAddDesignComboData(final int nreportDetailCode, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		outputMap.putAll((Map<String, Object>) getReportParameter(nreportDetailCode, userInfo).getBody());

		String strQuery = "select ndesigncomponentcode, coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as sdesigncomponentname, nsitecode, nstatus from designcomponents where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<DesignComponents> designComponentList = jdbcTemplate.query(strQuery, new DesignComponents());
		outputMap.put("DesignComponents", designComponentList);

		strQuery = " select * from sqlquery where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nquerytypecode = "
				+ Enumeration.QueryType.FILTER.getQuerytype();

		final List<SQLQuery> sqlQueryList = jdbcTemplate.query(strQuery, new SQLQuery());
		;

		outputMap.put("SQLQuery", sqlQueryList);

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createReportDesignParameter(final List<ReportDesignConfig> designConfigList,
			final UserInfo userInfo) throws Exception {

		final String sQueryLock = " lock  table lockreportdesigner "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final String configQuery = "select rdc.* from reportdesignconfig rdc where nreportdetailcode="
				+ designConfigList.get(0).getNreportdetailcode() + " and rdc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<ReportDesignConfig> configList = jdbcTemplate.query(configQuery, new ReportDesignConfig());

		if (configList.isEmpty()) {

			String sequencequery = "select nsequenceno from seqnoreportmanagement where stablename ='reportdesignconfig'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);

			String addstring = "";
			String sInsertQuery = " insert into reportdesignconfig (nreportdesigncode, ndays, ndesigncomponentcode, nmandatory, nreportdetailcode, nreportparametercode, nsqlquerycode, sdisplayname, dmodifieddate, nsitecode, nstatus) values";

			for (int i = 0; i < designConfigList.size(); i++) {

				String addstring1 = " ";
				nsequenceno++;
				if (i < designConfigList.size() - 1) {
					addstring1 = ",";
				}

				addstring = "(" + nsequenceno + ", " + designConfigList.get(i).getNdays() + ", "
						+ designConfigList.get(i).getNdesigncomponentcode() + ", "
						+ designConfigList.get(i).getNmandatory() + ", "
						+ designConfigList.get(i).getNreportdetailcode() + ", "
						+ designConfigList.get(i).getNreportparametercode() + ", "
						+ designConfigList.get(i).getNsqlquerycode() + ", N'"
						+ stringUtilityFunction.replaceQuote(designConfigList.get(i).getSdisplayname()) + "', '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
						+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;

				designConfigList.get(i).setNreportdesigncode(nsequenceno);
				sInsertQuery = sInsertQuery + addstring;
			}
			jdbcTemplate.execute(sInsertQuery);

			String updatequery = "update seqnoreportmanagement set nsequenceno =" + nsequenceno
					+ " where stablename='reportdesignconfig'";
			jdbcTemplate.execute(updatequery);

			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(designConfigList), 1, null,
					Arrays.asList("IDS_ADDREPORTDESIGNCONFIG"), userInfo);

			return getReportDesignConfig(designConfigList.get(0).getNreportdetailcode(), userInfo);
		} else {
			final ReportDetails reportDetail = getActiveReportDetailById(designConfigList.get(0).getNreportdetailcode(),
					userInfo);

			if (reportDetail == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				if (reportDetail.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					final String delQuery = "delete from reportdesignconfig where nreportdetailcode="
							+ designConfigList.get(0).getNreportdetailcode() + " and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(delQuery);

					auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(configList), 1, null,
							Arrays.asList("IDS_DELETEREPORTDESIGNCONFIG"), userInfo);

					String sequencequery = "select nsequenceno from seqnoreportmanagement where stablename ='reportdesignconfig'";
					int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);

					String addstring = "";
					String sInsertQuery = " insert into reportdesignconfig (nreportdesigncode, ndays, ndesigncomponentcode, nmandatory, nreportdetailcode, nreportparametercode, nsqlquerycode, sdisplayname, dmodifieddate, nsitecode, nstatus) values";

					for (int i = 0; i < designConfigList.size(); i++) {

						String addstring1 = " ";
						nsequenceno++;
						if (i < designConfigList.size() - 1) {
							addstring1 = ",";
						}

						addstring = "(" + nsequenceno + ", " + designConfigList.get(i).getNdays() + ", "
								+ designConfigList.get(i).getNdesigncomponentcode() + ", "
								+ designConfigList.get(i).getNmandatory() + ", "
								+ designConfigList.get(i).getNreportdetailcode() + ", "
								+ designConfigList.get(i).getNreportparametercode() + ", "
								+ designConfigList.get(i).getNsqlquerycode() + ", N'"
								+ stringUtilityFunction.replaceQuote(designConfigList.get(i).getSdisplayname()) + "', '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNmastersitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;

						designConfigList.get(i).setNreportdesigncode(nsequenceno);
						sInsertQuery = sInsertQuery + addstring;
					}
					jdbcTemplate.execute(sInsertQuery);

					String updatequery = "update seqnoreportmanagement set nsequenceno =" + nsequenceno
							+ " where stablename='reportdesignconfig'";
					jdbcTemplate.execute(updatequery);

					auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(designConfigList), 1, null,
							Arrays.asList("IDS_ADDREPORTDESIGNCONFIG"), userInfo);
					return getReportDesignConfig(designConfigList.get(0).getNreportdetailcode(), userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTEDITAPPROVEDREPORT",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}

	}

	@Override
	@SuppressWarnings({"unused" })
	public ResponseEntity<Object> approveReportVersion(final ReportDetails reportDetail, final UserInfo userInfo)
			throws Exception {

		final ReportMaster reportById = getActiveReportMasterById(reportDetail.getNreportcode(), userInfo);
		if (reportById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REPORTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ReportDetails detailById = getActiveReportDetailById(reportDetail.getNreportdetailcode(), userInfo);

			if (detailById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				if (detailById.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
					boolean isValidForApprove = true;
					if (reportById.getNreporttypecode() == Enumeration.ReportType.MIS.getReporttype()) {

						final String parameterQuery = "select * from reportparameter where nreportdetailcode="
								+ detailById.getNreportdetailcode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final List<ReportParameter> reportParameterList = jdbcTemplate.query(parameterQuery,
								new ReportParameter());
						for (ReportParameter ParameterList : reportParameterList) {
							ParameterList.setSdisplaydatatype(commonFunction.getMultilingualMessage(
									ParameterList.getSdatatype(), userInfo.getSlanguagefilename()));
						}
						if (reportParameterList.isEmpty()) {
							isValidForApprove = true;
						} else {
							final String configQuery = "select rdg.*, sq.ssqlquery from reportdesignconfig rdg, sqlquery sq where rdg.nreportdetailcode="
									+ detailById.getNreportdetailcode() + " and rdg.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rdg.nsqlquerycode = sq.nsqlquerycode and sq.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
							List<ReportDesignConfig> lstReportDesignConfig = jdbcTemplate.query(configQuery,
									new ReportDesignConfig());

							final String mappingQuery = "select * from reportparametermapping where nreportdetailcode="
									+ detailById.getNreportdetailcode() + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							List<ReportParameterMapping> lstReportParameterMapping = jdbcTemplate.query(configQuery,
									new ReportParameterMapping());

							final List<ReportDesignConfig> reportConfigList = lstReportDesignConfig;
							final List<ReportParameterMapping> reportParamList = lstReportParameterMapping;

							if (!reportConfigList.isEmpty()) {
								final List<ReportDesignConfig> comboFieldList = reportConfigList.stream().filter(
										item -> item.getNdesigncomponentcode() == Enumeration.DesignComponent.COMBOBOX
												.gettype()
												&& (item.getSsqlquery().contains("<@")
														|| item.getSsqlquery().contains("<#")))
										.collect(Collectors.toList());
								if (comboFieldList.isEmpty()) {
									isValidForApprove = true;
								} else {

									if (!reportParamList.isEmpty()) {
										isValidForApprove = true;
									}
								}
							}
						}
					} else {// Batch/Sample

					}
					if (reportById.getNreporttypecode() == Enumeration.ReportType.CONTROLBASED.getReporttype()) {

						isValidForApprove = false;

						final String parameterQuery = "select * from reportparameter where nreportdetailcode="
								+ detailById.getNreportdetailcode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final List<ReportParameter> reportParameterList = jdbcTemplate.query(parameterQuery,
								new ReportParameter());
						for (ReportParameter ParameterList : reportParameterList) {
							ParameterList.setSdisplaydatatype(commonFunction.getMultilingualMessage(
									ParameterList.getSdatatype(), userInfo.getSlanguagefilename()));
						}
						if (reportParameterList.isEmpty()) {
							isValidForApprove = true;
						} else {

							final String mappingQuery = "select * from parameterconfiguration where nreportdetailcode="
									+ detailById.getNreportdetailcode() + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							List<ParameterConfiguration> parameterMapping = jdbcTemplate.query(mappingQuery,
									new ParameterConfiguration());

							if (parameterMapping.isEmpty() || reportParameterList.size() != parameterMapping.size()) {
								isValidForApprove = false;
							} else {
								isValidForApprove = true;
							}
						}

					}
					if (isValidForApprove) {

						final ReportDetails approvedDetail = getApprovedReportDetail(reportById);

						if (approvedDetail != null) {
							// Copy of object before update
							final ReportDetails detailBeforeSave = SerializationUtils.clone(approvedDetail);
							approvedDetail.setNtransactionstatus(
									Enumeration.TransactionStatus.RETIRED.gettransactionstatus());

							final String query = "update reportdetails set ntransactionstatus="
									+ approvedDetail.getNtransactionstatus() + ", dmodifieddate='"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportdetailcode = "
									+ approvedDetail.getNreportdetailcode();
							jdbcTemplate.execute(query);

							auditUtilityFunction.fnInsertAuditAction(Arrays.asList(approvedDetail), 2,
									Arrays.asList(detailBeforeSave), Arrays.asList("IDS_RETIREREPORTVERSION"),
									userInfo);

						}
						int nversionNo = -1;
						Object versionObject = projectDAOSupport.fnGetVersion(userInfo.getNformcode(),
								reportById.getNreportcode(), null, SeqNoReportVersion.class,
								userInfo.getNmastersitecode(), userInfo);
						if (versionObject != null) {
							nversionNo = Integer.parseInt(BeanUtils.getProperty(versionObject, "versionno"));
						}
						reportDetail.setNversionno(nversionNo);
						reportDetail
								.setNtransactionstatus(Enumeration.TransactionStatus.APPROVED.gettransactionstatus());

						final String query = "update reportdetails set ntransactionstatus="
								+ reportDetail.getNtransactionstatus() + ", nversionno=" + reportDetail.getNversionno()
								+ ", dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' where nreportdetailcode = " + reportDetail.getNreportdetailcode();
						jdbcTemplate.execute(query);
						int a = reportDetail.getNversionno();
						String sversionno = Integer.toString(a);
						reportDetail.setSversionno(sversionno);
						auditUtilityFunction.fnInsertAuditAction(Arrays.asList(reportDetail), 2,
								Arrays.asList(detailById), Arrays.asList("IDS_APPROVEREPORTVERSION"), userInfo);
						return getReportMaster(reportDetail.getNreportcode(), reportDetail.getSfilterreporttypecode(),
								userInfo);

					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_VERSIONNOTYETCONFIGUREDORMAPPED",
										userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_VERSIONALREADYAPPROVED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

				}

			}
		}

	}

	@Override
	public ResponseEntity<Object> deleteReportMaster(final ReportMaster reportMaster, final UserInfo userInfo)
			throws Exception {

		final ReportMaster reportById = getActiveReportMasterById(reportMaster.getNreportcode(), userInfo);
		if (reportById == null) {
			// status code= 417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REPORTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ReportDetails reportDetails = getApprovedReportDetail(reportMaster);
			if (reportDetails == null) {
				List<String> lstFiles = new ArrayList<>();
				
				final String reportDetailQuery = "select * from reportdetails where nreportcode="
						+ reportMaster.getNreportcode() + " and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final List<ReportDetails> reportDetailList = jdbcTemplate.query(reportDetailQuery, new ReportDetails());

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> auditActionList = new ArrayList<>();

				String updateQuery = "";

				if (!reportDetailList.isEmpty()) {
					final String detailKey = reportDetailList.stream()
							.map(item -> String.valueOf(item.getNreportdetailcode())).collect(Collectors.joining(","));
					String queryString = "";
					queryString = "select * from reportparameter where nreportdetailcode in (" + detailKey
							+ ") and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					List<ReportParameter> lstrp = jdbcTemplate.query(queryString, new ReportParameter());

					queryString = "select * from reportdesignconfig where nreportdetailcode in (" + detailKey
							+ ") and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					List<ReportDesignConfig> lstrdc = jdbcTemplate.query(queryString, new ReportDesignConfig());

					queryString = "select * from reportparametermapping where nreportdetailcode in (" + detailKey
							+ ") and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					List<ReportParameterMapping> lstrpm = jdbcTemplate.query(queryString, new ReportParameterMapping());

					queryString = "select * from subreportdetails where nreportdetailcode in (" + detailKey
							+ ") and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					List<SubReportDetails> lstsd = jdbcTemplate.query(queryString, new SubReportDetails());

					queryString = "select * from reportimages where nreportdetailcode in (" + detailKey
							+ ") and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					List<ReportImages> lstrm = jdbcTemplate.query(queryString, new ReportImages());

					final List<ReportParameter> parameterList = lstrp;
					final List<ReportDesignConfig> configList = lstrdc;
					final List<ReportParameterMapping> mappingList = lstrpm;
					final List<SubReportDetails> subReportList = lstsd;
					final List<ReportImages> imageList = lstrm;

					for (ReportParameter ParameterList : parameterList) {
						ParameterList.setSdisplaydatatype(commonFunction
								.getMultilingualMessage(ParameterList.getSdatatype(), userInfo.getSlanguagefilename()));
					}

					for(ReportDetails reportDetail: reportDetailList) {
						lstFiles.add(reportDetail.getSsystemfilename());
					}
					
					for(ReportImages reportImage : lstrm) {
						lstFiles.add(reportImage.getSsystemfilename());
					}
					
					auditActionList.add(imageList);
					auditActionList.add(subReportList);
					auditActionList.add(mappingList);
					auditActionList.add(configList);
					auditActionList.add(parameterList);
					auditActionList.add(reportDetailList);

					multilingualIDList.add("IDS_DELETEREPORTIMAGE");
					multilingualIDList.add("IDS_DELETESUBREPORTDETAILS");
					multilingualIDList.add("IDS_DELETEREPORTPARAMETERMAPPING");
					multilingualIDList.add("IDS_DELETEREPORTCONFIG");
					multilingualIDList.add("IDS_DELETEREPORTPARAMETER");
					multilingualIDList.add("IDS_DELETEREPORTDETAILS");

					updateQuery = updateQuery + " update reportdesignconfig set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where  nreportdetailcode in ("
							+ detailKey + ");" + " update reportparameter set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where  nreportdetailcode in ("
							+ detailKey + ");" + " update reportparametermapping set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportdetailcode in ("
							+ detailKey + ");" + " update subreportdetails set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportdetailcode  in ("
							+ detailKey + ") ;" + " update reportimages set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportdetailcode  in ("
							+ detailKey + "); " + " update reportdetails set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where  nreportdetailcode in ("
							+ detailKey + ");";

				}

				updateQuery = updateQuery + " ;update reportmaster set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where  nreportcode = "
						+ reportMaster.getNreportcode();
				jdbcTemplate.execute(updateQuery);

				
				ftpUtilityFunction.deleteFTPFile(lstFiles, "reports", userInfo);
				
				auditActionList.add(Arrays.asList(reportMaster));
				multilingualIDList.add("IDS_DELETEREPORTMASTER");

				auditUtilityFunction.fnInsertListAuditAction(auditActionList, 1, null, multilingualIDList, userInfo);

				return getReportMaster(null, null, userInfo);

			} else {
				// status code= 417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTDELETEAPPROVEDREPORT",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteReportDetails(final ReportDetails reportDetails, final UserInfo userInfo)
			throws Exception {

		final ReportMaster reportById = getActiveReportMasterById(reportDetails.getNreportcode(), userInfo);
		if (reportById == null) {
			// status code= 417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REPORTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ReportDetails detailById = getActiveReportDetailById(reportDetails.getNreportdetailcode(), userInfo);

			if (detailById == null) {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {

				if (detailById.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
						.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTDELETEAPPROVEDREPORT",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

				} else if (detailById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
						.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTDELETERETIREDREPORT",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

				} else {
					String queryString = "";
					List<String> lstFiles = new ArrayList<>();

					queryString = "select * from reportparameter where nreportdetailcode = "
							+ detailById.getNreportdetailcode() + " and  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					List<ReportParameter> lstReportParameter = jdbcTemplate.query(queryString, new ReportParameter());

					queryString = "select * from reportdesignconfig where nreportdetailcode = "
							+ detailById.getNreportdetailcode() + " and  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					List<ReportDesignConfig> lstReportDesignConfig = jdbcTemplate.query(queryString,
							new ReportDesignConfig());

					queryString = "select * from reportparametermapping where nreportdetailcode = "
							+ detailById.getNreportdetailcode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					List<ReportParameterMapping> lstReportParameterMapping = jdbcTemplate.query(queryString,
							new ReportParameterMapping());

					queryString = "select * from subreportdetails where nreportdetailcode = "
							+ detailById.getNreportdetailcode() + " and  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					List<SubReportDetails> lstSubReportDetails = jdbcTemplate.query(queryString,
							new SubReportDetails());

					queryString = "select * from reportimages where nreportdetailcode = "
							+ detailById.getNreportdetailcode() + " and  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					List<ReportImages> lstReportImages = jdbcTemplate.query(queryString, new ReportImages());

					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> auditActionList = new ArrayList<>();

					final List<ReportParameter> parameterList = lstReportParameter;
					final List<ReportDesignConfig> configList = lstReportDesignConfig;
					final List<ReportParameterMapping> mappingList = lstReportParameterMapping;
					final List<SubReportDetails> subReportList = lstSubReportDetails;
					final List<ReportImages> imageList = lstReportImages;

					for (ReportParameter ParameterList : parameterList) {
						ParameterList.setSdisplaydatatype(commonFunction
								.getMultilingualMessage(ParameterList.getSdatatype(), userInfo.getSlanguagefilename()));
					}

					lstFiles.add(detailById.getSsystemfilename());
					
					for(ReportImages reportImage : lstReportImages) {
						lstFiles.add(reportImage.getSsystemfilename());
					}
					
					auditActionList.add(imageList);
					auditActionList.add(subReportList);
					auditActionList.add(mappingList);
					auditActionList.add(configList);
					auditActionList.add(parameterList);

					multilingualIDList.add("IDS_DELETEREPORTIMAGE");
					multilingualIDList.add("IDS_DELETESUBREPORTDETAILS");
					multilingualIDList.add("IDS_DELETEREPORTPARAMETERACTION");
					multilingualIDList.add("IDS_DELETEREPORTPARAMETERMAPPING");
					multilingualIDList.add("IDS_DELETEREPORTCONFIG");
					multilingualIDList.add("IDS_DELETEREPORTPARAMETER");

					final String updateQuery = " update reportdetails set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where  nreportdetailcode = "
							+ detailById.getNreportdetailcode() + ";" + " update reportparameter set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportdetailcode = "
							+ detailById.getNreportdetailcode() + ";" + " update reportdesignconfig set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportdetailcode = "
							+ detailById.getNreportdetailcode() + ";" + " update reportparametermapping set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportdetailcode = "
							+ detailById.getNreportdetailcode() + ";" + " update subreportdetails set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportdetailcode = "
							+ detailById.getNreportdetailcode() + ";" + " update reportimages set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportdetailcode = "
							+ detailById.getNreportdetailcode() + ";";

					jdbcTemplate.execute(updateQuery);

					ftpUtilityFunction.deleteFTPFile(lstFiles, "reports", userInfo);
					
					auditActionList.add(Arrays.asList(detailById));
					multilingualIDList.add("IDS_DELETEREPORTDETAILS");

					auditUtilityFunction.fnInsertListAuditAction(auditActionList, 1, null, multilingualIDList,
							userInfo);
					return getReportMaster(reportDetails.getNreportcode(), reportDetails.getSfilterreporttypecode(),
							userInfo);
				}
			}
		}
	}

	@Override
	public ResponseEntity<Object> createReportParameterMapping(final ReportDetails reportDetail,
			final List<ReportParameterMapping> mappingList, final UserInfo userInfo) throws Exception {

		final String sQueryLock = " lock  table lockreportdesigner "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final String queryString = "select rpm.* from reportparametermapping rpm where rpm.nreportdetailcode="
				+ reportDetail.getNreportdetailcode() + " and rpm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<ReportParameterMapping> configList = jdbcTemplate.query(queryString, new ReportParameterMapping());

		if (configList.isEmpty()) {
			if (!mappingList.isEmpty()) {

				String sequencequery = "select nsequenceno from seqnoreportmanagement where stablename ='reportparametermapping'";
				int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);

				String addstring = "";
				String sInsertQuery = " Insert into reportparametermapping (nreportparametermappingcode, nchildreportdesigncode, nisactionparent, nparentreportdesigncode,  nreportdetailcode, sfieldname, dmodifieddate, nsitecode, nstatus) values";

				for (int i = 0; i < mappingList.size(); i++) {

					String addstring1 = " ";
					nsequenceno++;
					if (i < mappingList.size() - 1) {
						addstring1 = ",";
					}

					addstring = "(" + nsequenceno + ", " + mappingList.get(i).getNchildreportdesigncode() + ", "
							+ mappingList.get(i).getNisactionparent() + ", "
							+ mappingList.get(i).getNparentreportdesigncode() + ", "
							+ mappingList.get(i).getNreportdetailcode() + ", N'" + mappingList.get(i).getSfieldname()
							+ "', '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ userInfo.getNmastersitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;

					mappingList.get(i).setNreportparametermappingcode(nsequenceno);
					sInsertQuery = sInsertQuery + addstring;
				}
				jdbcTemplate.execute(sInsertQuery);
				String updatequery = "update seqnoreportmanagement set nsequenceno =" + nsequenceno
						+ " where stablename='reportparametermapping'";
				jdbcTemplate.execute(updatequery);

				auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(mappingList), 1, null,
						Arrays.asList("IDS_ADDREPORTPARAMETERMAPPING"), userInfo);
			}
			return getReportParameterMapping(reportDetail.getNreportdetailcode(), userInfo);

		} else {
			final ReportDetails activeReportDetail = getActiveReportDetailById(reportDetail.getNreportdetailcode(),
					userInfo);

			if (activeReportDetail == null) {
				// status code= 417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				if (activeReportDetail.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					final String delQuery = "delete from reportparametermapping where nreportdetailcode="
							+ reportDetail.getNreportdetailcode() + " and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(delQuery);

					auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(configList), 1, null,
							Arrays.asList("IDS_DELETEREPORTPARAMETERMAPPING"), userInfo);

					if (!mappingList.isEmpty()) {

						String sequencequery = "select nsequenceno from seqnoreportmanagement where stablename ='reportparametermapping'";
						int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
						String addstring = "";
						String sInsertQuery = " Insert into reportparametermapping (nreportparametermappingcode, nchildreportdesigncode, nisactionparent, nparentreportdesigncode,  nreportdetailcode, sfieldname, dmodifieddate, nsitecode, nstatus) values";

						for (int i = 0; i < mappingList.size(); i++) {

							String addstring1 = " ";
							nsequenceno++;
							if (i < mappingList.size() - 1) {
								addstring1 = ",";
							}

							addstring = "(" + nsequenceno + ", " + mappingList.get(i).getNchildreportdesigncode() + ", "
									+ mappingList.get(i).getNisactionparent() + ", "
									+ mappingList.get(i).getNparentreportdesigncode() + ", "
									+ mappingList.get(i).getNreportdetailcode() + ", N'"
									+ mappingList.get(i).getSfieldname() + "', '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
									+ userInfo.getNmastersitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;

							mappingList.get(i).setNreportparametermappingcode(nsequenceno);
							sInsertQuery = sInsertQuery + addstring;
						}
						jdbcTemplate.execute(sInsertQuery);

						String updatequery = "update seqnoreportmanagement set nsequenceno =" + nsequenceno
								+ " where stablename='reportparametermapping'";
						jdbcTemplate.execute(updatequery);

						auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(mappingList), 1, null,
								Arrays.asList("IDS_ADDREPORTPARAMETERMAPPING"), userInfo);
					}
					return getReportParameterMapping(reportDetail.getNreportdetailcode(), userInfo);
				} else {
					// status code= 417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTEDITAPPROVEDREPORT",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}

	}

	@Override
	public ResponseEntity<Object> getReportParameterMappingComboData(final int nreportDetailCode, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strQuery = "select rdc.*, sq.ssqlqueryname as ssqlqueryname, sq.ssqlquery as ssqlquery, sq.svaluemember as svaluemember from "
				+ " reportdesignconfig rdc, sqlquery sq where rdc.nsqlquerycode = sq.nsqlquerycode "
				+ " and rdc.nstatus = sq.nstatus and rdc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nreportdetailcode= "
				+ nreportDetailCode;

		final List<ReportDesignConfig> designComponentList = jdbcTemplate.query(strQuery, new ReportDesignConfig());
		outputMap.put("ParentComponentList", designComponentList);

		final List<ReportDesignConfig> parameterList = designComponentList.stream()
				.filter(item -> item.getSsqlquery().contains("<@") || item.getSsqlquery().contains("<#"))
				.collect(Collectors.toList());
		outputMap.put("ChildComponentList", parameterList);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateReportStatus(final ReportMaster reportMaster, final UserInfo userInfo)
			throws Exception {
		final ReportMaster reportById = getActiveReportMasterById(reportMaster.getNreportcode(), userInfo);
		if (reportById == null) {
			// status code= 417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REPORTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (reportMaster.getNtransactionstatus() == reportById.getNtransactionstatus()) {

				String msg = "";
				if (reportMaster.getNtransactionstatus() == Enumeration.TransactionStatus.ACTIVE
						.gettransactionstatus()) {
					msg = "IDS_REPORTALREADYACTIVATED";
				} else {
					msg = "IDS_REPORTALREADYDEACTIVATED";
				}
				// status code= 417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(msg, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final ReportMaster masterBeforeUpdate = SerializationUtils.clone(reportById);
				reportById.setNtransactionstatus(reportMaster.getNtransactionstatus());

				final String updateQueryString = "update reportmaster set ntransactionstatus="
						+ reportById.getNtransactionstatus() + ", dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportcode = "
						+ reportMaster.getNreportcode();
				jdbcTemplate.execute(updateQueryString);

				auditUtilityFunction.fnInsertAuditAction(Arrays.asList(reportById), 2,
						Arrays.asList(masterBeforeUpdate), Arrays.asList("IDS_EDITREPORTMASTER"), userInfo);
				return getReportMaster(reportMaster.getNreportcode(),
						Integer.toString(reportMaster.getNreporttypecode()), userInfo);
			}

		}
	}

	@Override
	public ResponseEntity<Object> createSubReportDetails(final MultipartHttpServletRequest request,
			SubReportDetails subReport, final UserInfo userInfo) throws Exception {

		final String sQueryLock = " lock  table subreportdetails "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final ReportMaster activeMaster = getActiveReportMasterById(subReport.getNreportcode(), userInfo);
		final ReportDetails activeDetail = getActiveReportDetailById(subReport.getNreportdetailcode(), userInfo);
		if (activeMaster == null || activeDetail == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);

			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {

				String seqquery = "select nsequenceno from SeqNoReportManagement where stablename ='subreportdetails'";
				int nsequenceno = jdbcTemplate.queryForObject(seqquery, Integer.class);
				nsequenceno++;

				String insertquery = "Insert into subreportdetails (nsubreportdetailcode,nreportcode,nreportdetailcode,nstatus,sfilename,ssystemfilename,dmodifieddate,nsitecode)"
						+ "values(" + nsequenceno + "," + subReport.getNreportcode() + ","
						+ subReport.getNreportdetailcode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",N'"
						+ stringUtilityFunction.replaceQuote(subReport.getSfilename()) + "',N'"
						+ stringUtilityFunction.replaceQuote(subReport.getSsystemfilename()) + "', '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
						+ ")";

				jdbcTemplate.execute(insertquery);

				String updatequery = "update SeqNoReportManagement set nsequenceno =" + nsequenceno
						+ " where stablename ='subreportdetails'";
				jdbcTemplate.execute(updatequery);
				subReport.setNsubreportdetailcode(nsequenceno);
				auditUtilityFunction.fnInsertAuditAction(Arrays.asList(subReport), 1, null,
						Arrays.asList("IDS_ADDSUBREPORTDETAILS"), userInfo);

				return getSubReportDetails(subReport.getNreportdetailcode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	public SubReportDetails getActiveSubReportDetailsById(final int subReportDetailCode) throws Exception {
		final String queryString = " select srd.* from subreportdetails srd where " + " srd.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and srd.nsubreportdetailcode = "
				+ subReportDetailCode;
		return jdbcTemplate.queryForObject(queryString, new SubReportDetails());

	}

	@Override
	public ResponseEntity<Object> deleteSubReportDetails(final SubReportDetails subReportDetails,
			final UserInfo userInfo) throws Exception {

		final ReportMaster activeMaster = getActiveReportMasterById(subReportDetails.getNreportcode(), userInfo);
		final ReportDetails activeDetail = getActiveReportDetailById(subReportDetails.getNreportdetailcode(), userInfo);
		if (activeMaster == null || activeDetail == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final SubReportDetails detailById = getActiveSubReportDetailsById(
					subReportDetails.getNsubreportdetailcode());

			if (detailById == null) {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUBREPORTALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				if (activeDetail.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					// valid for delete
					final String updateQuery = "update subreportdetails set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where  nsubreportdetailcode = "
							+ detailById.getNsubreportdetailcode();

					jdbcTemplate.execute(updateQuery);
					detailById.setNstatus(Enumeration.TransactionStatus.DELETED.gettransactionstatus());

					auditUtilityFunction.fnInsertAuditAction(Arrays.asList(detailById), 1, null,
							Arrays.asList("IDS_DELETESUBREPORTDETAILS"), userInfo);
					return getSubReportDetails(activeDetail.getNreportdetailcode());
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_CANNOTDELETEFORAPPROVEDREPORT",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				}
			}
		}
	}

	@Override
	public ResponseEntity<Object> createReportImages(final MultipartHttpServletRequest request,
			ReportImages reportImages, final UserInfo userInfo) throws Exception {

		final String sQueryLock = " lock  table reportimages "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final ReportMaster activeMaster = getActiveReportMasterById(reportImages.getNreportcode(), userInfo);
		final ReportDetails activeDetail = getActiveReportDetailById(reportImages.getNreportdetailcode(), userInfo);
		if (activeMaster == null || activeDetail == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);

			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {

				String seqquery = "select nsequenceno from SeqNoReportManagement where stablename ='reportimages'";
				int nsequenceno = jdbcTemplate.queryForObject(seqquery, Integer.class);
				nsequenceno++;

				String insertquery = "Insert into reportimages (nreportimagecode,nreportdetailcode,nreportcode,sfilename,ssystemfilename,nstatus,dmodifieddate,nsitecode)"
						+ "values(" + nsequenceno + "," + reportImages.getNreportdetailcode() + ","
						+ reportImages.getNreportcode() + ",N'"
						+ stringUtilityFunction.replaceQuote(reportImages.getSfilename()) + "',N'"
						+ stringUtilityFunction.replaceQuote(reportImages.getSsystemfilename()) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ")";

				jdbcTemplate.execute(insertquery);

				String updatequery = "update SeqNoReportManagement set nsequenceno =" + nsequenceno
						+ " where stablename ='reportimages'";
				jdbcTemplate.execute(updatequery);

				reportImages.setNreportimagecode(nsequenceno);
				auditUtilityFunction.fnInsertAuditAction(Arrays.asList(reportImages), 1, null,
						Arrays.asList("IDS_ADDREPORTIMAGES"), userInfo);

				return getReportImages(reportImages.getNreportdetailcode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	public ReportImages getActiveReportImagesById(final int reportImageId) throws Exception {
		final String queryString = " select ri.* from reportimages ri where " + " ri.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ri.nreportimagecode = "
				+ reportImageId;
		return jdbcTemplate.queryForObject(queryString, new ReportImages());
	}

	@Override
	public ResponseEntity<Object> deleteReportImages(final ReportImages reportImages, final UserInfo userInfo)
			throws Exception {

		final ReportMaster activeMaster = getActiveReportMasterById(reportImages.getNreportcode(), userInfo);
		final ReportDetails activeDetail = getActiveReportDetailById(reportImages.getNreportdetailcode(), userInfo);
		if (activeMaster == null || activeDetail == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ReportImages detailById = getActiveReportImagesById(reportImages.getNreportimagecode());

			if (detailById == null) {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REPORTIMAGEALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				if (activeDetail.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					
					ftpUtilityFunction.deleteFTPFile(Arrays.asList(detailById.getSsystemfilename()), "", userInfo);
					// valid for delete
					final String updateQuery = "update reportimages set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where  nreportimagecode = "
							+ detailById.getNreportimagecode();

					jdbcTemplate.execute(updateQuery);
					detailById.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

					auditUtilityFunction.fnInsertAuditAction(Arrays.asList(detailById), 1, null,
							Arrays.asList("IDS_DELETEREPORTIMAGES"), userInfo);
					return getReportImages(activeDetail.getNreportdetailcode());
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_CANNOTDELETEFORAPPROVEDREPORT",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				}
			}
		}
	}

	@Override
	public ResponseEntity<Object> getControlButton(int nformcode, UserInfo userinfo) throws Exception {

		final String query = " select coalesce (q.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
				+ "',q.jsondata->'sdisplayname'->>'en-US') as  sdisplayname ,q.nformcode nformcode,"
				+ " coalesce (c.jsondata->'scontrolids'->>'" + userinfo.getSlanguagetypecode()
				+ "',c.jsondata->'sdisplayname'->>'en-US') as scontrolids,c.ncontrolcode  "
				+ " from controlmaster c,qualisforms q,sitecontrolmaster sc  where nisreportcontrol="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "  and "
				+ " sc.nformcode=c.nformcode and  sc.nsitecode=" + userinfo.getNmastersitecode()
				+ " and c.nformcode=q.nformcode" + "  and sc.ncontrolcode=c.ncontrolcode  and q.nformcode=" + nformcode
				+ " and sc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<SiteControlMaster> lst = jdbcTemplate.query(query, new SiteControlMaster());
		return new ResponseEntity<>(lst, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings({ "unchecked", "unlikely-arg-type" })
	public ResponseEntity<Object> getReportSampleType(int nreporttypecode, UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		String queryString = "select *,coalesce(jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sampletypename'->>'en-US') as ssampletypename "
				+ " from sampletype where nsampletypecode > 0 " + " and nsitecode = " + userInfo.getNmastersitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<SampleType> sampleTypeList = jdbcTemplate.query(queryString, new SampleType());
		final Map<String, Object> coaReportTypeList = (Map<String, Object>) getCOAReportType(nreporttypecode,
				sampleTypeList.get(0).getNsampletypecode(), userInfo).getBody();

		final List<RegistrationType> regTypeList = (List<RegistrationType>) getRegistrationtypeForSample(
				sampleTypeList.get(0).getNsampletypecode(), userInfo, nreporttypecode).getBody();
		outputMap.put("COAReportType", coaReportTypeList.get("CoaReportType"));
		outputMap.put("SelectedCOAReportType", coaReportTypeList.get(0));
		outputMap.put("CertificateType", coaReportTypeList.get("CertificateType"));
		outputMap.put("SampleType", sampleTypeList);
		outputMap.put("SelectedSampleType", sampleTypeList.get(0));
		outputMap.put("RegistrationType", regTypeList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getRegistrationtypeForSample(int nsampletypecode, UserInfo userInfo,
			int nreporttypecode) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		String queryString = "select *,coalesce(jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sregtypename'->>'en-US') as sregtypename "
				+ " from registrationtype where  nsitecode = " + userInfo.getNmastersitecode() + " and nsampletypecode="
				+ nsampletypecode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<RegistrationType> regTypeList = jdbcTemplate.query(queryString, new RegistrationType());
		if (!regTypeList.isEmpty()) {
			final Map<String, Object> coaReportTypeList = (Map<String, Object>) getCOAReportType(nreporttypecode,
					nsampletypecode, userInfo).getBody();
			final List<RegistrationSubType> regSubTypeList = (List<RegistrationSubType>) getRegistrationSubTypeByRegType(
					regTypeList.get(0).getNregtypecode(), userInfo).getBody();
			outputMap.put("COAReportType", coaReportTypeList.get("COAReportType"));
			outputMap.put("CertificateType", coaReportTypeList.get("CertificateType"));
			outputMap.put("RegistrationType", regTypeList);
			outputMap.put("SelectedRegType", regTypeList.get(0));
			outputMap.put("RegistrationSubType", regSubTypeList);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REGISTRATIONTYPENOTAVAILABLE",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

		}
	}

	public ResponseEntity<Object> getReportRegSubTypeApproveConfigVersion(int nregtypecode, int nregsubtypecode,
			UserInfo userInfo) throws Exception {

		final String getApprovalConfigVersion = "select aca.sversionname,aca.napprovalconfigcode,"
				+ " aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode,acv.napproveconfversioncode "
				+ " from   approvalconfigautoapproval aca," + " approvalconfig ac,approvalconfigversion acv "
				+ "    where   acv.napproveconfversioncode=aca.napprovalconfigversioncode and ac.napprovalconfigcode=aca.napprovalconfigcode and ac.napprovalconfigcode=acv.napprovalconfigcode "
				+ " and ac.nregtypecode =" + nregtypecode + " and ac.nregsubtypecode =" + nregsubtypecode
				+ " and acv.ntransactionstatus not in ( " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ ") and acv.nsitecode =" + userInfo.getNmastersitecode() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,"
				+ "acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname,acv.napproveconfversioncode order by aca.napprovalconfigversioncode desc";

		final List<ApprovalConfigAutoapproval> approvalVersion = jdbcTemplate.query(getApprovalConfigVersion,
				new ApprovalConfigAutoapproval());

		return new ResponseEntity<>(approvalVersion, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSampleTypeList(final UserInfo userInfo) throws Exception {

		List<SampleType> sampleTypeList = null;
		String queryString = "";

		queryString = "select *,coalesce(jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sampletypename'->>'en-US') as ssampletypename"
				+ "  from sampletype where nsampletypecode > 0 " + " and nsitecode = " + userInfo.getNmastersitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		sampleTypeList = jdbcTemplate.query(queryString, new SampleType());
		return new ResponseEntity<>(sampleTypeList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReportTemplate(UserInfo userinfo) throws Exception {
		String queryString = "select *, coalesce(jsondata->'stemplatename'->>'" + userinfo.getSlanguagetypecode()
				+ "', jsondata->'stemplatename'->>'en-US') as sreporttemplatename"
				+ " from reporttemplate where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userinfo.getNmastersitecode();
		List<Map<String, Object>> reportTemplateList = jdbcTemplate.queryForList(queryString);
		return new ResponseEntity<>(reportTemplateList, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unused")
	public ResponseEntity<Object> createReportValidation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final List<ControlbasedReportvalidation> controlBasedList = reportvalidationByID(inputMap, userInfo);

		if (controlBasedList.isEmpty()) { // lock the table

			String ntranscode = "ntranscode";
			String str = "transactionstatus where ntranscode in(" + inputMap.get("ntransctionstatus") + ")";

			final String sequenceQueryString = "select nsequenceno from seqnoreportmanagement where stablename ='controlbasedreportvalidation'";
			int nsequenceno = jdbcTemplate.queryForObject(sequenceQueryString, Integer.class);

			String queryString = "INSERT INTO public.controlbasedreportvalidation(nreportvalidationcode, nreportdetailcode, nformcode,ncontrolcode, ntranscode, dmodifieddate, nsitecode, nstatus)"
					+ "(Select " + nsequenceno + " + Rank()over(order by ntranscode) as nreportvalidationcode,"
					+ inputMap.get("nreportdetailcode") + "," + inputMap.get("nformcode") + ","
					+ inputMap.get("ncontrolcode") + ", " + "ntranscode as ntransctionstatus,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "from " + str + ") ;";

			queryString = queryString
					+ "update seqnoreportmanagement set nsequenceno =(select max(nreportvalidationcode) from controlbasedreportvalidation)  where stablename='controlbasedreportvalidation'";
			jdbcTemplate.execute(queryString);

			final String strAudit = "SELECT STRING_AGG(ts.stransstatus, ', ') AS transstatus "
					+ "FROM transactionstatus ts where ts.ntranscode in (" + inputMap.get("ntransctionstatus") + ") "
					+ "and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

			final ControlbasedReportvalidation reportValidation = new ControlbasedReportvalidation();

			final String auditStatus = jdbcTemplate.queryForObject(strAudit, String.class);

			reportValidation.setNreportdetailcode((int) inputMap.get("nreportdetailcode"));
			reportValidation.setStransactionstatus(auditStatus);
			reportValidation.setNformcode((int) inputMap.get("nformcode"));
			reportValidation.setNcontrolcode((int) inputMap.get("ncontrolcode"));
			;
			final List<Object> savedTestList = new ArrayList<>();
			final List<String> multilingualIDList = new ArrayList<>();

			savedTestList.add(reportValidation);
			multilingualIDList.add("IDS_ADDREPORTVALIDATION");

			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
			return getControlbasedReportvalidation((int) inputMap.get("nreportdetailcode"), userInfo);
		} else {

			final String stransactionStatus = controlBasedList.stream()
					.map(objreport -> String.valueOf(objreport.getStransactionstatus()))
					.collect(Collectors.joining(","));

			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_DUPLICATEFORM", userInfo.getSlanguagefilename()) + " "
							+ stransactionStatus,
					HttpStatus.EXPECTATION_FAILED);

		}

	}

	private List<ControlbasedReportvalidation> reportvalidationByID(Map<String, Object> inputMap, UserInfo userInfo) {
		String queryString = "select cbr.*,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransactionstatus" + " from controlbasedreportvalidation cbr,transactionstatus ts "
				+ " where cbr.ntranscode=ts.ntranscode and cbr.nreportdetailcode=" + inputMap.get("nreportdetailcode")
				+ " and cbr.ntranscode in (" + inputMap.get("ntransctionstatus") + ") and cbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc;";

		return (List<ControlbasedReportvalidation>) jdbcTemplate.query(queryString, new ControlbasedReportvalidation());
	}

	@Override
	public ResponseEntity<Object> getReportValidation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		String passStr = (String) inputMap.get("ntranscode");
		if (passStr.isEmpty()) {
			passStr = "-1";
		}

		String queryString = "select distinct coalesce(ts.jsondata -> 'stransdisplaystatus' ->> 'en-US', ts.jsondata -> 'stransdisplaystatus' ->> 'en-US') "
				+ "as stransstatus,rvs.*, ts.ntranscode from transactionstatus ts, reportvalidationstatus rvs "
				+ "where ts.ntranscode not in(select rvs.ntranscode from controlbasedreportvalidation rvs "
				+ "where rvs.ntranscode in (" + passStr + ") " + "and rvs.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
				+ "and rvs.ntranscode = ts.ntranscode " + "and rvs.nformcode =" + inputMap.get("nformcode") + " "
				+ "and rvs.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and ts.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "order by 1 desc;";

		List<Map<String, Object>> reportTemplateList = jdbcTemplate.queryForList(queryString);
		return new ResponseEntity<>(reportTemplateList, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteReportValidation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final ControlbasedReportvalidation controlbasedreportvalidation = objMapper
				.convertValue(inputMap.get("reportvalidation"), ControlbasedReportvalidation.class);

		final String deleteCheck = "select * from controlbasedreportvalidation where  " + "nreportvalidationcode="
				+ controlbasedreportvalidation.getNreportvalidationcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";  ";

		final ControlbasedReportvalidation objReport = (ControlbasedReportvalidation) jdbcUtilityFunction
				.queryForObject(deleteCheck, ControlbasedReportvalidation.class, jdbcTemplate);

		if (objReport == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else {

			final String queryString = "UPDATE public.controlbasedreportvalidation SET  " + "dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " "
					+ "WHERE nreportvalidationcode="
					+ ((Map<String, Object>) inputMap.get("reportvalidation")).get("nreportvalidationcode") + "; ";

			jdbcTemplate.execute(queryString);

			final List<Object> savedTestList = new ArrayList<>();
			final List<String> multilingualIDList = new ArrayList<>();

			savedTestList.add(controlbasedreportvalidation);
			multilingualIDList.add("IDS_DELETEREPORTVALIDATION");
			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
			return getControlbasedReportvalidation(
					(int) ((Map<String, Object>) inputMap.get("reportvalidation")).get("nreportdetailcode"), userInfo);
		}
	}

}
