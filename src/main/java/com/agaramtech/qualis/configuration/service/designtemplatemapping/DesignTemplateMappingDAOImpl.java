package com.agaramtech.qualis.configuration.service.designtemplatemapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.audittrail.model.DynamicAuditTable;
import com.agaramtech.qualis.checklist.model.ChecklistVersion;
import com.agaramtech.qualis.checklist.model.ChecklistVersionQB;
import com.agaramtech.qualis.configuration.model.DeleteValidation;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.QualisForms;
import com.agaramtech.qualis.credential.model.QualisModule;
import com.agaramtech.qualis.credential.model.SeqNoCredentialManagement;
import com.agaramtech.qualis.dashboard.model.QueryBuilderTables;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegSubTypeConfigVersion;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class DesignTemplateMappingDAOImpl implements DesignTemplateMappingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(DesignTemplateMappingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getDesignTemplateMapping(int nsampletypecode, int nregtypecode, int nregsubtypecode,
			int ndesigntemplatemappingcode, short formCode, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		if (nsampletypecode == -1 && nregtypecode == -1 && nregsubtypecode == -1) {
			final String getSampleTypeQry = "select nsampletypecode,ncategorybasedflowrequired,coalesce(jsondata->'sampletypename'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',jsondata->'sampletypename'->>'en-US') as ssampletypename,nsorter "
					+ " from sampletype where nsampletypecode > 0  and nstatus in( -2,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") order by nsorter;";
			final List<SampleType> sampleTypes = jdbcTemplate.query(getSampleTypeQry, new SampleType());

			List<RegistrationType> registrationTypes = new ArrayList<>();
			List<RegistrationSubType> registrationSubTypes = new ArrayList<>();

			Map<String, Object> subTypeValue = null;
			Map<String, Object> regTypeValue = null;
			Map<String, Object> regsubTypeValue = null;
			if (!sampleTypes.isEmpty()) {
				nsampletypecode = sampleTypes.get(0).getNsampletypecode();

				final String getRegistrationQry = "select nregtypecode,coalesce(jsondata->'sregtypename'->>'"
						+ userInfo.getSlanguagetypecode() + "',"
						+ " jsondata->'sregtypename'->>'en-US') as sregtypename "
						+ " from registrationtype where nregtypecode >0" + " and nsitecode="+userInfo.getNmastersitecode()+" and nsampletypecode = " + nsampletypecode
						+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				registrationTypes = jdbcTemplate.query(getRegistrationQry, new RegistrationType());
				subTypeValue = new HashMap<>();
				subTypeValue.put("label", sampleTypes.get(0).getSsampletypename());
				subTypeValue.put("value", sampleTypes.get(0).getNsampletypecode());
				subTypeValue.put("item", sampleTypes.get(0));

				if (!registrationTypes.isEmpty()) {
					nregtypecode = registrationTypes.get(0).getNregtypecode();

					final String getRegistrationSubQry = "select rs.nregsubtypecode,coalesce(rs.jsondata->'sregsubtypename'->>'"
							+ userInfo.getSlanguagetypecode() + "',"
							+ " rs.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,"
							+ " coalesce(rsv.jsondata->>'nneedsubsample','false')::boolean as nneedsubsample, "
							+ " coalesce(rsv.jsondata->>'nneedjoballocation','false')::boolean as nneedjoballocation, "
							+ " coalesce(rsv.jsondata->>'nneedmyjob','false')::boolean as nneedmyjob, "
							+ " coalesce(rsv.jsondata->>'nneedtestinitiate','false')::boolean as nneedtestinitiate, "
							+ " rsv.napprovalconfigcode"
							+ " from registrationsubtype rs, regsubtypeconfigversion rsv,approvalconfig ac"
							+ " where ac.nregtypecode = " + nregtypecode
							+ " and ac.nregsubtypecode = rs.nregsubtypecode and ac.napprovalconfigcode = rsv.napprovalconfigcode"
							+ " and rs.nregsubtypecode >0  and rs.nsitecode="+userInfo.getNmastersitecode()+"  "
							+ " and rsv.nsitecode="+userInfo.getNmastersitecode()+" and  ac.nsitecode="+userInfo.getNmastersitecode()+" and rsv.ntransactionstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rs.nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					registrationSubTypes = jdbcTemplate.query(getRegistrationSubQry, new RegistrationSubType());
					regTypeValue = new HashMap<>();
					regTypeValue.put("label", registrationTypes.get(0).getSregtypename());
					regTypeValue.put("value", registrationTypes.get(0).getNregtypecode());
					regTypeValue.put("item", registrationTypes.get(0));

					if (!registrationSubTypes.isEmpty()) {
						nregsubtypecode = registrationSubTypes.get(0).getNregsubtypecode();
						regsubTypeValue = new HashMap<>();
						regsubTypeValue.put("label", registrationSubTypes.get(0).getSregsubtypename());
						regsubTypeValue.put("value", registrationSubTypes.get(0).getNregsubtypecode());
						regsubTypeValue.put("item", registrationSubTypes.get(0));

					}
				}

			}
			returnMap.put("SampleTypes", sampleTypes);
			returnMap.put("registrationTypes", registrationTypes);
			returnMap.put("registrationSubTypes", registrationSubTypes);

			returnMap.put("defaultsampletype", subTypeValue);
			returnMap.put("defaultregtype", regTypeValue);
			returnMap.put("defaultregsubtype", regsubTypeValue);

			returnMap.put("realSampleValue", subTypeValue);
			returnMap.put("realRegTypeValue", regTypeValue);
			returnMap.put("realRegSubTypeValue", regsubTypeValue);

		}

		final String getRegistrationTemplate = "select dt.ndesigntemplatemappingcode, dt.nsampletypecode, dt.nformcode, dt.nregtypecode, dt.nregsubtypecode, "
				+ "dt.nregsubtypeversioncode, dt.nformwisetypecode,dt.nreactregtemplatecode, dt.nsubsampletemplatecode, dt.ntransactionstatus, "
				+ "dt.nsitecode, dt.nstatus,rt.nreactregtemplatecode, rt.nsampletypecode, rt.ntransactionstatus, rt.sregtemplatename, rt.stemplatetypesname,"
				+ "rt.jsondata, rt.nsitecode, rt.nstatus ," + " sst.jsondata subsamplejsondata, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "') as stransdisplaystatus,"
				+ " case when dt.nversionno >0 then dt.nversionno::character varying else '-' end as sversionno, "
				+ " qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' as sformname, "
				+ " qm.nmodulecode,qm.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "' as smodulename "
				+ " from reactregistrationtemplate rt, reactregistrationtemplate sst,transactionstatus ts,designtemplatemapping dt,"
				+ " qualisforms qf, qualismodule qm " + " where ts.ntranscode= dt.ntransactionstatus "
				+ " and dt.nsampletypecode = " + nsampletypecode + " and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nregtypecode = " + nregtypecode
				+ " and dt.nregsubtypecode = " + nregsubtypecode + " and dt.nformcode = " + formCode
				+ " and dt.nreactregtemplatecode = rt.nreactregtemplatecode" + " and sst.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and dt.nsubsampletemplatecode = sst.nreactregtemplatecode " + " and qf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qf.nformcode = dt.nformcode "
				+ " and qm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nsitecode="+userInfo.getNmastersitecode()+"  and sst.nsitecode="+userInfo.getNmastersitecode()+"  and dt.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and qf.nmodulecode = qm.nmodulecode " + " order by ndesigntemplatemappingcode;";
		final List<DesignTemplateMapping> designTemplateMapping = jdbcTemplate.query(getRegistrationTemplate,
				new DesignTemplateMapping());
		returnMap.put("DesignTemplateMapping", designTemplateMapping);
		if (!designTemplateMapping.isEmpty()) {
			if (nsampletypecode == 4) {
				Map<String, Object> formValue = new HashMap<>();
				formValue.put("label", designTemplateMapping.get(0).getSformname());
				formValue.put("value", designTemplateMapping.get(0).getNformcode());
				formValue.put("item", designTemplateMapping.get(0));
				returnMap.put("realFormValue", formValue);
			}

		}
		if (designTemplateMapping.isEmpty()) {
			returnMap.put("selectedDesignTemplateMapping", null);
		} else {
			if (ndesigntemplatemappingcode != -1) {

				List<DesignTemplateMapping> lstSelectedTemplate = designTemplateMapping.stream()
						.filter(x -> x.getNdesigntemplatemappingcode() == (ndesigntemplatemappingcode))
						.collect(Collectors.toList());

				if (!lstSelectedTemplate.isEmpty()) {
					returnMap.put("selectedDesignTemplateMapping", lstSelectedTemplate.get(0));
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYDELETED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

			} else {
				returnMap.put("selectedDesignTemplateMapping",
						designTemplateMapping.get(designTemplateMapping.size() - 1));
			}
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDynamicPreRegDesign(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		short sampleType;
		if (inputMap.containsKey("nsampletypecode")) {

			sampleType = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));

		} else {

			final String getSampleTypeQry = "select nsampletypecode,jsondata->'sampletypename'->>'"
					+ userInfo.getSlanguagetypecode() + "' as ssampletypename "
					+ " from sampletype where nsampletypecode >0" + " and napprovalconfigview = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			List<SampleType> sampleTypes = jdbcTemplate.query(getSampleTypeQry, new SampleType());
			sampleType = sampleTypes.get(0).getNsampletypecode();

			returnMap.put("SampleTypes", sampleTypes);
		}
		final String getRegistrationTemplate = "select *  from reactregistrationtemplate " + " where nsampletypecode = "
				+ sampleType + " and " + " ntransactionstatus= "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode()+";";

		final List<ReactRegistrationTemplate> designTemplateMapping = jdbcTemplate.query(getRegistrationTemplate,
				new ReactRegistrationTemplate());

		if (sampleType != Enumeration.SampleType.MASTERS.getType()
				&& sampleType != Enumeration.SampleType.GOODSIN.getType()
				&& sampleType != Enumeration.SampleType.PROTOCOL.getType()
				// && sampleType != Enumeration.SampleType.STABILITY.getType()
				) {

			final String subsampleQuery = "select cast(rscv.jsondata->>'nneedsubsample' as boolean) from "
					+ " approvalconfig ac, regsubtypeconfigversion rscv where "
					+ " ac.napprovalconfigcode=rscv.napprovalconfigcode " + " and ac.nregsubtypecode="
					+ inputMap.get("nregsubtypecode") + "  and ac.nsitecode="+userInfo.getNmastersitecode()+"  and rscv.nsitecode="+userInfo.getNmastersitecode()+" and rscv.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
			// final Boolean subsampleQueryData = (Boolean)
			// jdbcQueryForObject(subsampleQuery, Boolean.class);
			final Boolean subsampleQueryData = jdbcTemplate.queryForObject(subsampleQuery, Boolean.class);

			int nissubsampletest = Enumeration.TransactionStatus.YES.gettransactionstatus();
			if (subsampleQueryData == null || subsampleQueryData == false) {
				nissubsampletest = Enumeration.TransactionStatus.NO.gettransactionstatus();
			}

			final String getTestListFields = "select jsondata from testlistfields where " + " nissubsampletest="
					+ nissubsampletest// Enumeration.TransactionStatus.YES.gettransactionstatus()
					// + " ntestlistfieldscode = 1 "
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";
			final SampleType testListFields = (SampleType) jdbcUtilityFunction.queryForObject(getTestListFields,
					SampleType.class, jdbcTemplate);

			final String getSubSampleTemplate = "select * from reactregistrationtemplate " + " where nsampletypecode = "
					+ Enumeration.SampleType.SUBSAMPLE.getType() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsubsampletypecode="
					+ sampleType + " and  ntransactionstatus= "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode()+";";

			final List<ReactRegistrationTemplate> subSampleTemplate = jdbcTemplate.query(getSubSampleTemplate,
					new ReactRegistrationTemplate());
			returnMap.put("SubSampleTemplate", subSampleTemplate);

			returnMap.put("TestListFields", testListFields);
		} else {
			if (sampleType == Enumeration.SampleType.MASTERS.getType()) {
				final String moduleQuery = " select nmodulecode,coalesce(jsondata->'sdisplayname'->>'"
						+ userInfo.getSlanguagetypecode() + "',jsondata->'sdisplayname'->>'en-US') as "
						+ " sdisplayname from qualismodule  where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nmodulecode <>-1 and nmenucode = " + Enumeration.QualisMenu.MASTER.getQualismenu()
						+ " order by 1 desc ";
				final List<QualisModule> moduleList = jdbcTemplate.query(moduleQuery, new QualisModule());
				returnMap.put("DT_QualisModule", moduleList);
			}

		}
		returnMap.put("DesignTemplateMapping", designTemplateMapping);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTemplateRegType(int nregtypecode, final int nsampleTypeCode,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> objmap = new HashMap<>();
		Map<String, Object> gettemplate = new HashMap<>();
		List<RegistrationType> lstregistrationtype;
		List<QualisForms> lstForms;
		List<RegistrationSubType> lstregistrationsubtype = new ArrayList<>();
		Map<String, Object> regTypeValue = null;
		Map<String, Object> regSubTypeValue = null;
		int nregsubtypecode = 0;
		String strQuery = "";

		if (nsampleTypeCode == 4) {
			final String getForms =
					" select nformcode,coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "',jsondata->'sdisplayname'->>'en-US') as sdisplayname from qualisforms"
					+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nformcode <>-1" + " and nmenucode = "
					+ Enumeration.QualisMenu.MASTER.getQualismenu() + "";
			// + " order by 1 desc ";
			lstForms = jdbcTemplate.query(getForms, new QualisForms());
			if (!lstForms.isEmpty()) {
				regSubTypeValue = new HashMap<>();
				regSubTypeValue.put("label", lstForms.get(0).getSdisplayname());
				regSubTypeValue.put("value", lstForms.get(0).getNformcode());
				regSubTypeValue.put("item", lstForms.get(0));
			}
			objmap.put("qualisforms", lstForms);
			objmap.put("defaultform", regSubTypeValue);
			objmap.put("nregtypecode", -1);
			objmap.put("nregsubtypecode", -1);
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		} else {
			strQuery = regtypeQuery(nsampleTypeCode, userInfo);
			lstregistrationtype = jdbcTemplate.query(strQuery, new RegistrationType());
			nregtypecode = !lstregistrationtype.isEmpty() ? lstregistrationtype.get(0).getNregtypecode() : -1;
			if (!lstregistrationtype.isEmpty()) {
				regTypeValue = new HashMap<>();
				regTypeValue.put("label", lstregistrationtype.get(0).getSregtypename());
				regTypeValue.put("value", lstregistrationtype.get(0).getNregtypecode());
				regTypeValue.put("item", lstregistrationtype.get(0));

				strQuery = regSubtypeQuery(nregtypecode, userInfo);
				lstregistrationsubtype = jdbcTemplate.query(strQuery, new RegistrationSubType());
				nregsubtypecode = !lstregistrationsubtype.isEmpty() ? lstregistrationsubtype.get(0).getNregsubtypecode()
						: -1;
				if (!lstregistrationsubtype.isEmpty()) {
					regSubTypeValue = new HashMap<>();
					regSubTypeValue.put("label", lstregistrationsubtype.get(0).getSregsubtypename());
					regSubTypeValue.put("value", lstregistrationsubtype.get(0).getNregsubtypecode());
					regSubTypeValue.put("item", lstregistrationsubtype.get(0));
				}
			}

			gettemplate.put("nsampletypecode", nsampleTypeCode);
			gettemplate.put("nregtypecode", nregtypecode);
			gettemplate.put("nregsubtypecode", nregsubtypecode);

			objmap.put("registrationTypes", lstregistrationtype);

			objmap.put("registrationSubTypes", lstregistrationsubtype);
			objmap.put("defaultregtype", regTypeValue);
			objmap.put("defaultregsubtype", regSubTypeValue);

			return new ResponseEntity<>(objmap, HttpStatus.OK);
		}

	}

	@Override
	public ResponseEntity<Object> getTemplateRegSubType(final int nregtypecode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> objmap = new HashMap<>();
		Map<String, Object> gettemplate = new HashMap<>();
		List<RegistrationSubType> lstregistrationsubtype;
		Map<String, Object> regSubTypeValue = null;
		int nregsubtypecode = 0;
		String strQuery = "";

		strQuery = regSubtypeQuery(nregtypecode, userInfo);
		lstregistrationsubtype = jdbcTemplate.query(strQuery, new RegistrationSubType());
		nregsubtypecode = !lstregistrationsubtype.isEmpty() ? lstregistrationsubtype.get(0).getNregsubtypecode() : -1;
		if (!lstregistrationsubtype.isEmpty()) {
			regSubTypeValue = new HashMap<>();
			regSubTypeValue.put("label", lstregistrationsubtype.get(0).getSregsubtypename());
			regSubTypeValue.put("value", lstregistrationsubtype.get(0).getNregsubtypecode());
			regSubTypeValue.put("item", lstregistrationsubtype.get(0));
		}
		gettemplate.put("nregtypecode", nregtypecode);
		gettemplate.put("nregsubtypecode", nregsubtypecode);

		objmap.put("registrationSubTypes", lstregistrationsubtype);
		objmap.put("defaultregsubtype", regSubTypeValue);

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	private String regSubtypeQuery(final int nregtypecode, final UserInfo userInfo) {
		return "select rs.nregsubtypecode,coalesce(rs.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
		+ "',rs.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,"
		+ " coalesce(rsv.jsondata->>'nneedsubsample','false')::boolean as nneedsubsample, "
		+ " rsv.napprovalconfigcode "
		+ " from registrationsubtype rs, regsubtypeconfigversion rsv,approvalconfig ac"
		+ " where ac.nregtypecode = " + nregtypecode
		+ " and ac.nregsubtypecode = rs.nregsubtypecode and ac.napprovalconfigcode = rsv.napprovalconfigcode"
		+ " and rs.nregsubtypecode >0  and rs.nsitecode="+userInfo.getNmastersitecode()+" and rsv.nsitecode="+userInfo.getNmastersitecode()+""
		+ " and ac.nsitecode="+userInfo.getNmastersitecode()+" and rsv.ntransactionstatus = "
		+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rs.nstatus ="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	}

	private String regtypeQuery(final int nsampletypecode, final UserInfo userInfo) {
		return "select *,coalesce(jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
		+ "',jsondata->'sregtypename'->>'en-US') as sregtypename from registrationtype rt where rt.nstatus = "
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nsampletypecode = "
		+ nsampletypecode + " and rt.nsitecode = " + userInfo.getNmastersitecode() + ";";
	}

	public void createView(String viewName, String templateName, int ndesigntemplatemappingcode, int formcode,
			boolean isMaster, boolean isGoodsIn, boolean isDropView, String ssubSampletemplatename) {
		String createView = "";
		if (isMaster) {
			if (isDropView) {
				createView = "drop view if exists view_" + viewName + ";";
			}
			createView = createView + "CREATE OR REPLACE VIEW public.view_" + viewName
					+ " AS SELECT d.ndynamicmastercode, d.nformcode, d.ndesigntemplatemappingcode, d.jsondata, d.jsonuidata, d.nsitecode, d.nstatus,r.* FROM   dynamicmaster d,"
					+ "   jsonb_populate_record(NULL::type_" + templateName + ", d.jsonuidata) r where d.nformcode="
					+ formcode + " and d.ndesigntemplatemappingcode=" + ndesigntemplatemappingcode + "";
			// ALPD-4283 commented by rukshana on June 01 2024 to avoid OWNER issue in NFC
			// + "ALTER TABLE public.view_" + viewName + " OWNER TO postgres;";
		} else if (isGoodsIn) {
			if (isDropView) {
				createView = "drop view if exists view_" + viewName + ";";
			}
			createView = createView + "CREATE OR REPLACE VIEW public.view_" + viewName
					+ " AS SELECT  d.jsonuidata, d.ngoodsinsamplecode,d.ngoodsincode,d.ndesigntemplatemappingcode,d.nstatus ,r.* FROM   view_goodsinsample d,"
					+ "   jsonb_populate_record(NULL::type_" + templateName
					+ ", d.jsonuidata) r where d.ndesigntemplatemappingcode=" + ndesigntemplatemappingcode + "";
			// ALPD-4283 commented by rukshana on June 01 2024 to avoid OWNER issue in NFC
			// + "ALTER TABLE public.view_" + viewName + " OWNER TO postgres;";
		} else {
			if (isDropView) {
				createView = "drop view if exists view_" + viewName + ";";
			}

			if (ssubSampletemplatename == null) {

				createView = createView + "CREATE OR REPLACE VIEW public.view_" + viewName
						+ " AS SELECT d.*,r.* FROM   registration d," + "   jsonb_populate_record(NULL::type_"
						+ templateName + ", d.jsonuidata) r where  d.ndesigntemplatemappingcode="
						+ ndesigntemplatemappingcode + "";

			} else {

				createView = createView + "CREATE OR REPLACE VIEW public.view_" + viewName + "report"
						+ " AS SELECT d.*,r.*,rs.* FROM   view_registrationreport d,"
						+ "   jsonb_populate_record(NULL::type_" + templateName + ", d.jsonuidata) r,  "
						+ "   jsonb_populate_record(NULL::type_" + ssubSampletemplatename
						+ ", d.samplejsonuidata) rs where " + " d.ndesigntemplatemappingcode="
						+ ndesigntemplatemappingcode + "";

			}

		}

		jdbcTemplate.execute(createView);
	}

	@Override
	public ResponseEntity<Object> createDesignTemplateMapping(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		short sampleType;
		short registrationType;
		short registrationSubType;
		short reactregTemplate;
		short formCode = -1;
		int nsubsampletemplatecode = -1;
		int nregsubtypeversioncode = -1;

		if (inputMap.containsKey("nsampletypecode") && inputMap.containsKey("nregtypecode")
				&& inputMap.containsKey("nregsubtypecode")) {

			sampleType = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
			registrationType = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
			registrationSubType = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));

			// Added by sonia on 11th NOV 2024 for jira id:ALPD-5025
			if (sampleType != Enumeration.SampleType.MASTERS.getType()
					&& sampleType != Enumeration.SampleType.GOODSIN.getType()
					&& sampleType != Enumeration.SampleType.PROTOCOL.getType()
					// && sampleType != Enumeration.SampleType.STABILITY.getType()
					) {
				final String query = "select * from regsubtypeconfigversion rc,approvalconfig ac "
						+ " where ac.nstatus=1 and rc.napprovalconfigcode=ac.napprovalconfigcode and ac.nregsubtypecode="
						+ registrationSubType + "  and rc.nsitecode="+userInfo.getNmastersitecode()+"  and ac.nsitecode="+userInfo.getNmastersitecode()+" and rc.ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
				RegSubTypeConfigVersion reg = (RegSubTypeConfigVersion) jdbcUtilityFunction.queryForObject(query,
						RegSubTypeConfigVersion.class, jdbcTemplate);
				if (reg == null) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDVERSIONREGSUBTYPE",
									userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				} else {
					nregsubtypeversioncode = reg.getNregsubtypeversioncode();
				}
			}

			reactregTemplate = Short.parseShort(String.valueOf(inputMap.get("nreactregtemplatecode")));
			// nregsubtypeversioncode=(int) inputMap.get("nregsubtypeversioncode");
			formCode = Short.parseShort(String.valueOf(inputMap.get("nformcode")));
			nsubsampletemplatecode = inputMap.containsKey("nsubsampletemplatecode")
					? (int) inputMap.get("nsubsampletemplatecode")
							: -1;

			final String sGetSeqNoQuery = "select nsequenceno from seqnoregtemplateversion where stablename = 'designtemplatemapping';";
			int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);
			nSeqNo++;

			final String sGetSeqNoQuery01 = "select nsequenceno from seqnoregtemplateversion where stablename = 'mappedtemplatefieldprops';";
			int nSeqNo01 = jdbcTemplate.queryForObject(sGetSeqNoQuery01, Integer.class);
			nSeqNo01++;

			int seqNoQualisForms = -1;
			//int seqNoTable = -1;

			boolean valid = true;
			if (sampleType == Enumeration.SampleType.MASTERS.getType()) {

				if (formCode == -2) {

					final String formQuery = "select * from qualisforms where nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sformname = N'"
							+ stringUtilityFunction.replaceQuote((String) inputMap.get("sformname")) + "'";
					final QualisForms form = (QualisForms) jdbcUtilityFunction.queryForObject(formQuery,
							QualisForms.class, jdbcTemplate);
					if (form == null) {
						final String getSeqNoForm = "select stablename,nsequenceno from seqnocredentialmanagement "
								+ " where stablename in('qualisforms','querybuildertables');";
						List<SeqNoCredentialManagement> seqList = jdbcTemplate.query(getSeqNoForm,
								new SeqNoCredentialManagement());

						Map<String, Integer> seqMap = seqList.stream()
								.collect(Collectors.toMap(SeqNoCredentialManagement::getStablename,
										seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));

						// ALPD-5084 L.Subashini --New Master, New Module has to be created with form
						// code greater than Enumeration value

						seqNoQualisForms = (seqMap.get("qualisforms") < Enumeration.DynamicInitialValue.DYNAMICFORMS
								.getInitialValue() ? Enumeration.DynamicInitialValue.DYNAMICFORMS.getInitialValue()
										: seqMap.get("qualisforms"))
								+ 1;

						//						seqNoTable = (seqMap.get("querybuildertables") < Enumeration.DynamicInitialValue.QUERYBUILDERTABLES
						//								.getInitialValue() ? Enumeration.DynamicInitialValue.QUERYBUILDERTABLES.getInitialValue()
						//										: seqMap.get("querybuildertables"))
						//								+ 1;

						//seqNoTable = seqMap.get("querybuildertables") + 1;

						////////////////////
						int nmodulecode = (int) inputMap.get("nmodulecode");
						if (inputMap.get("smoduledisplayname") != null
								&& ((String) inputMap.get("smoduledisplayname")).trim().length() > 0) {

							final String nameQuery = "select * from qualismodule where nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and smodulename=N'" + inputMap.get("smoduledisplayname") + "'";
							// final QualisModule moduleByName =(QualisModule)
							// jdbcTemplate.queryForObject(nameQuery, new QualisModule());

							final QualisModule moduleByName = (QualisModule) jdbcUtilityFunction
									.queryForObject(nameQuery, QualisModule.class, jdbcTemplate);

							if (moduleByName == null) {

								final String moduleQuery = "select max(nsorter) from qualismodule where nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and nmenucode = " + Enumeration.QualisMenu.MASTER.getQualismenu();
								final int nsorter = jdbcTemplate.queryForObject(moduleQuery, Integer.class);

								final String seqModuleQuery = "select nsequenceno from seqnocredentialmanagement "
										+ " where stablename in('qualismodule');";

								int seqNoQualisModule = jdbcTemplate.queryForObject(seqModuleQuery, Integer.class);

								// ALPD-5084 L.Subashini --New Master, New Module has to be created with form
								// code greater than Enumeration value
								seqNoQualisModule = (seqNoQualisModule < Enumeration.DynamicInitialValue.DYNAMICMODULE
										.getInitialValue()
										? Enumeration.DynamicInitialValue.DYNAMICMODULE.getInitialValue()
												: seqNoQualisModule)
										+ 1;

								// ALPD-5084 L.Subashini --New Master, New Module has to be created with form
								// code greater than Enumeration value

								// ALPD-662_fix
								String insertModule = "INSERT INTO qualismodule(nmodulecode,nmenucode,smodulename,jsondata,"
										+ " nsorter,dmodifieddate, nstatus) values" + " (" + seqNoQualisModule + ","
										+ Enumeration.QualisMenu.MASTER.getQualismenu() + ",'"
										+ inputMap.get("smoduledisplayname") + "','"
										+ mapper.writeValueAsString(inputMap.get("qm_jsonData")) + "'," + nsorter + ",'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',1);"
										+ "update seqnocredentialmanagement set nsequenceno = " + seqNoQualisModule
										+ " where  stablename = 'qualismodule'";

								jdbcTemplate.execute(insertModule);
								nmodulecode = seqNoQualisModule;

							} else {
								nmodulecode = moduleByName.getNmodulecode();
							}
						}

						///////////////////
						String insertForms = "INSERT INTO qualisforms (nformcode,nmenucode,nmodulecode,sformname,jsondata,sclassname,surl,nsorter,nstatus,dmodifieddate) values"
								+ " (" + seqNoQualisForms + "," + Enumeration.QualisMenu.MASTER.getQualismenu() + ","
								+ nmodulecode + ",'" + inputMap.get("sformname") + "','"
								+ mapper.writeValueAsString(inputMap.get("qf_jsonData"))
								+ "','dynamicmaster','DynamicMaster',1,1,'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

						insertForms += "insert into sitequalisforms (nsiteformscode, nsitecode, nformcode, "
								+ " nsorter, nstatus, dmodifieddate) values (" + seqNoQualisForms + ", -1, "
								+ seqNoQualisForms + ", 1, 1,'" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "');";


						jdbcTemplate.execute(insertForms + " update seqnocredentialmanagement set nsequenceno = "
								+ seqNoQualisForms + " where stablename ='qualisforms';");
						//								+ " update seqnocredentialmanagement set nsequenceno = " + (seqNoTable )
						//								+ " where stablename='querybuildertables';");

						formCode = Short.valueOf(String.valueOf(seqNoQualisForms));
						registrationType = -1;
						registrationSubType = -1;
					} else {
						valid = false;
					}

				}
			}
			if (valid) {
				final String getRegistrationTemplate = "insert into designtemplatemapping(ndesigntemplatemappingcode,nsampletypecode,nformcode,nregtypecode,nregsubtypecode,nregsubtypeversioncode,"
						+ "nformwisetypecode,nreactregtemplatecode,nsubsampletemplatecode,ntransactionstatus,nversionno,nstatus,nsitecode,dmodifieddate) values ("
						+ nSeqNo + " , " + sampleType + " , " + formCode + " , " + registrationType + " , "
						+ registrationSubType + "," + nregsubtypeversioncode + ", -1 " + " ," + reactregTemplate + " ,"
						+ nsubsampletemplatecode + "," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						+ " , -1 ," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ userInfo.getNmastersitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "');";
				jdbcTemplate.execute(getRegistrationTemplate);

				final JSONObject jsonData = new JSONObject((Map<String, Object>) inputMap.get("jsondataobj"));

				final String updateQuery = "insert into mappedtemplatefieldprops(nmappedtemplatefieldpropcode, ndesigntemplatemappingcode, jsondata, nstatus,nsitecode,dmodifieddate)"
						+ "values (" + nSeqNo01 + " ," + nSeqNo + " , '"
						+ stringUtilityFunction.replaceQuote(jsonData.toString()) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ userInfo.getNmastersitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "');";
				jdbcTemplate.execute(updateQuery);

				final ObjectMapper objMapper = new ObjectMapper();

				Map<String, Object> auditData = objMapper.convertValue(inputMap.get("auditdata"), Map.class);

				boolean needsubsample = false;
				String fieldName = "subsampledisabled";
				if (inputMap.containsKey("needsubsample") && (Boolean) inputMap.get("needsubsample") == true) {
					needsubsample = true;
					fieldName = "subsampleenabled";
				}

				// Added by sonia on 11th NOV 2024 for jira id:ALPD-5025
				List<DynamicAuditTable> filteredTableList = new ArrayList<DynamicAuditTable>();
				if (sampleType != Enumeration.SampleType.MASTERS.getType()
						&& sampleType != Enumeration.SampleType.GOODSIN.getType()
						&& sampleType != Enumeration.SampleType.PROTOCOL.getType()) {
					final String auditTableQuery = "select ndynamicaudittablecode,nformcode,stablename,nissubsampletable,stableprimarykey, "
							+ " jsondata->'" + fieldName + "' as jsondata " + " from dynamicaudittable where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					final List<DynamicAuditTable> tableList = jdbcTemplate.query(auditTableQuery,
							new DynamicAuditTable());

					filteredTableList = tableList.stream().filter(item -> item.getJsondata() != null)
							.collect(Collectors.toList());
				}
				final String seqAuditQuery = "select nsequenceno from seqnoaudittrail "
						+ " where stablename = 'dynamicauditrecordtable';";
				// ALPD-5084 L.Subashini seqno changed
				int seqNoAuditTable = (int) jdbcUtilityFunction.queryForObject(seqAuditQuery, Integer.class,
						jdbcTemplate);

				final List<String> queryList = new ArrayList<String>();

				for (Map.Entry<String, Object> entry : auditData.entrySet()) {
					// System.out.println(key + ":" + value);
					final JSONObject sampleAuditData = new JSONObject((Map<String, Object>) entry.getValue());
					if (needsubsample) {
						if (entry.getKey().equalsIgnoreCase("registrationsample")) {
							//							final JSONObject subSampleAuditData = new JSONObject(
							//									(Map<String, Object>) entry.getValue());
							// ALPD-5084 Seqno changed
							queryList.add(" (" + ++seqNoAuditTable + "," + registrationType + "," + registrationSubType
									+ "," + nSeqNo
									// + "," +auditTableRecord.getNformcode()
									+ "," + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()
									+ ",'registrationsample','"
									+ stringUtilityFunction.replaceQuote(sampleAuditData.toString()) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "',(select COLUMN_name from information_schema.\"columns\" where table_name='registrationsample' and ordinal_position=1)"
									+ ", " + userInfo.getNmastersitecode() + ")");
						} else if (entry.getKey().equalsIgnoreCase("schedulersubsampledetail")) {

							queryList.add(" (" + ++seqNoAuditTable + "," + registrationType + "," + registrationSubType
									+ "," + nSeqNo
									// + "," +auditTableRecord.getNformcode()
									+ "," + Enumeration.QualisForms.SCHEDULERCONFIGURATION.getqualisforms()
									+ ",'schedulersubsampledetail','"
									+ stringUtilityFunction.replaceQuote(sampleAuditData.toString()) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "',(select COLUMN_name from information_schema.\"columns\" where table_name='schedulersubsampledetail' and ordinal_position=1)"
									+ ", " + userInfo.getNmastersitecode() + ")");

						}

						else if (entry.getKey().equalsIgnoreCase("schedulersampledetail")) {
							queryList.add(" (" + ++seqNoAuditTable + "," + registrationType + "," + registrationSubType
									+ "," + nSeqNo + ","
									+ Enumeration.QualisForms.SCHEDULERCONFIGURATION.getqualisforms() + ",'"
									+ entry.getKey() + "','"
									+ stringUtilityFunction.replaceQuote(sampleAuditData.toString()) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "',(select COLUMN_name from information_schema.\"columns\" where table_name='"
									+ entry.getKey() + "' and ordinal_position=1)" + ", "
									+ userInfo.getNmastersitecode() + ")");
						} else {
							queryList.add(" (" + ++seqNoAuditTable + "," + registrationType + "," + registrationSubType
									+ "," + nSeqNo
									// + "," +formCode
									+ "," + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() + ",'"
									+ entry.getKey() + "','"
									+ stringUtilityFunction.replaceQuote(sampleAuditData.toString()) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "',(select COLUMN_name from information_schema.\"columns\" where table_name='"
									+ entry.getKey() + "' and ordinal_position=1)" + ", "
									+ userInfo.getNmastersitecode() + ")");
						}
					} else {
						// final JSONObject sampleAuditData = new JSONObject((Map<String, Object>)
						// entry.getValue());

						if (entry.getKey().equalsIgnoreCase("registration")) {
							// formCode =
							// (short)Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms();

							// ALPD-5084 L.Subashini seqno changed
							queryList.add(" (" + ++seqNoAuditTable + "," + registrationType + "," + registrationSubType
									+ "," + nSeqNo
									// + "," +formCode
									+ "," + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() + ",'"
									+ entry.getKey() + "','"
									+ stringUtilityFunction.replaceQuote(sampleAuditData.toString()) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "',(select COLUMN_name from information_schema.\"columns\" where table_name='"
									+ entry.getKey() + "' and ordinal_position=1)" + ", "
									+ userInfo.getNmastersitecode() + ")");

						}

						else if (entry.getKey().equalsIgnoreCase("schedulersampledetail")) {
							// ALPD-5084 L.Subashini seqno changed
							queryList.add(" (" + ++seqNoAuditTable + "," + registrationType + "," + registrationSubType
									+ "," + nSeqNo + ","
									+ Enumeration.QualisForms.SCHEDULERCONFIGURATION.getqualisforms() + ",'"
									+ entry.getKey() + "','"
									+ stringUtilityFunction.replaceQuote(sampleAuditData.toString()) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "',(select COLUMN_name from information_schema.\"columns\" where table_name='"
									+ entry.getKey() + "' and ordinal_position=1)" + ", "
									+ userInfo.getNmastersitecode() + ")");
						}

						else if (entry.getKey().equalsIgnoreCase("goodsinsample")) {
							queryList.add(" (" + ++seqNoAuditTable + "," + registrationType + "," + registrationSubType
									+ "," + nSeqNo + "," + Enumeration.QualisForms.GOODSIN.getqualisforms() + ",'"
									+ entry.getKey() + "','"
									+ stringUtilityFunction.replaceQuote(sampleAuditData.toString()) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "',(select COLUMN_name from information_schema.\"columns\" where table_name='"
									+ entry.getKey() + "' and ordinal_position=1)" + ", "
									+ userInfo.getNmastersitecode() + ")");
							// Added by sonia on 11th NOV 2024 for jira id:ALPD-5025
						} else if (entry.getKey().equalsIgnoreCase("protocol")) {
							queryList.add(" (" + ++seqNoAuditTable + "," + registrationType + "," + registrationSubType
									+ "," + nSeqNo + "," + Enumeration.QualisForms.PROTOCOL.getqualisforms() + ",'"
									+ entry.getKey() + "','"
									+ stringUtilityFunction.replaceQuote(sampleAuditData.toString()) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "',(select COLUMN_name from information_schema.\"columns\" where table_name='"
									+ entry.getKey() + "' and ordinal_position=1)" + ", "
									+ userInfo.getNmastersitecode() + ")");
						} else {

							// ALPD-5084 L.Subashini seqno changed
							queryList.add(" (" + ++seqNoAuditTable + "," + registrationType + "," + registrationSubType
									+ "," + nSeqNo + "," + formCode
									// + "," + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()
									+ ",'" + entry.getKey() + "','"
									+ stringUtilityFunction.replaceQuote(sampleAuditData.toString()) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "',(select COLUMN_name from information_schema.\"columns\" where table_name='"
									+ entry.getKey() + "' and ordinal_position=1)" + ", "
									+ userInfo.getNmastersitecode() + ")");
						}
					}

				}

				for (DynamicAuditTable auditTable : filteredTableList) {

					boolean validAudit = true;
					if (auditTable.getNissubsampletable() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						if (needsubsample) {
							validAudit = true;

						} else {
							validAudit = false;
						}
					}

					if (validAudit) {

						final JSONObject auditJSonData = new JSONObject(auditTable.getJsondata());

						// ALPD-5084 L.Subashini seqno changed
						queryList.add(" (" + ++seqNoAuditTable + "," + registrationType + "," + registrationSubType
								+ "," + nSeqNo + "," + auditTable.getNformcode() + ",'" + auditTable.getStablename()
								+ "','" + stringUtilityFunction.replaceQuote(auditJSonData.toString()) + "',"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "','"
								+ auditTable.getStableprimarykey() + "'," + userInfo.getNmastersitecode() + ")");
					}
				}

				if (queryList.size() > 0) {
					final String insertAuditQuery = "insert into dynamicauditrecordtable "
							+ " (nauditrecordcode, nregtypecode, nregsubtypecode, ndesigntemplatemappingcode, "
							+ " nformcode, stablename, jsondata, nstatus, dauditdate,stableprimarykey, nsitecode) VALUES "
							+ String.join(",", queryList) + ";update seqnoaudittrail set nsequenceno = "
							+ seqNoAuditTable + " where stablename = 'dynamicauditrecordtable';";

					jdbcTemplate.execute(insertAuditQuery);
				}

				final String sUpdateSeqNoQuery = "update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename "
						+ "in ('designtemplatemapping','mappedtemplatefieldprops');";
				jdbcTemplate.execute(sUpdateSeqNoQuery);

				DesignTemplateMapping desinTemMap = new DesignTemplateMapping();
				desinTemMap.setNdesigntemplatemappingcode(nSeqNo);
				desinTemMap.setNsampletypecode(sampleType);
				desinTemMap.setNregtypecode(registrationType);
				desinTemMap.setNregsubtypecode(registrationSubType);
				desinTemMap.setNreactregtemplatecode(reactregTemplate);
				desinTemMap.setNsubsampletemplatecode(nsubsampletemplatecode);
				desinTemMap.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
				desinTemMap.setNformcode(formCode == -1 ? -2 : formCode);

				final List<Object> savedList = new ArrayList<>();
				savedList.add(desinTemMap);

				auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, Arrays.asList("IDS_ADDTEMPLATEMAPPING"),
						userInfo);

				return getDesignTemplateMapping(sampleType, registrationType, registrationSubType, nSeqNo, formCode,
						userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_DUPLICATEFORM", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_CHOOSETHEFILTER", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getDesignTemplateMappingById(int ndesigntemplatemappingcode, UserInfo userInfo)
			throws Exception {

		final String getDesignTemplate = "select dt.ndesigntemplatemappingcode, dt.nsampletypecode, dt.nformcode, dt.nregtypecode, dt.nregsubtypecode, "
				+ "dt.nregsubtypeversioncode, dt.nformwisetypecode,dt.nreactregtemplatecode, dt.nsubsampletemplatecode, dt.ntransactionstatus, "
				+ "dt.nversionno,dt.nsitecode, dt.nstatus,ts.jsondata->'" + userInfo.getSlanguagetypecode()
				+ "'->>'key' stransdisplaystatus " + " from designtemplatemapping dt,transactionstatus ts"
				+ " where ts.ntranscode= dt.ntransactionstatus " + " and ndesigntemplatemappingcode = "
				+ ndesigntemplatemappingcode + " and dt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and dt.nsitecode="+userInfo.getNmastersitecode()+" and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DesignTemplateMapping designTemplate = (DesignTemplateMapping) jdbcUtilityFunction
				.queryForObject(getDesignTemplate, DesignTemplateMapping.class, jdbcTemplate);
		return new ResponseEntity<>(designTemplate, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> approveDesignTemplateMapping(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		int seqNoTable1 = -1;
		int seqNoTableView = -1;
		int seqNoTable = -1;

		String updateQuery1 = "";
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final DesignTemplateMapping designtemplatemapping = objMapper
				.convertValue(inputMap.get("designtemplatemapping"), DesignTemplateMapping.class);
		int nversionno;
		DesignTemplateMapping oldTemplate = (DesignTemplateMapping) getDesignTemplateMappingById(
				designtemplatemapping.getNdesigntemplatemappingcode(), userInfo).getBody();
		if (oldTemplate != null) {
			if ((oldTemplate.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
					.gettransactionstatus())) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				String getSeqNoForm = "select stablename,nsequenceno from seqnocredentialmanagement "
						+ " where stablename in('querybuildertables','querytabledetails','querybuilderviews');";
				List<SeqNoCredentialManagement> seqList1 = jdbcTemplate.query(getSeqNoForm,
						new SeqNoCredentialManagement());

				Map<String, Integer> seqMap1 = seqList1.stream()
						.collect(Collectors.toMap(SeqNoCredentialManagement::getStablename,
								seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));
				//							int seqNoTable = seqMap1.get("querybuildertables") + 1;
				//							int seqNoTable1 = seqMap1.get("querytabledetails") + 1;
				//							int seqNoTableView = seqMap1.get("querybuilderviews") + 1;

				seqNoTable1 = (seqMap1.get("querytabledetails") < Enumeration.DynamicInitialValue.QUERYTABLEDETAILS
						.getInitialValue() ? Enumeration.DynamicInitialValue.QUERYTABLEDETAILS.getInitialValue()
								: seqMap1.get("querytabledetails"))
						+ 1;

				seqNoTableView = (seqMap1.get("querybuilderviews") < Enumeration.DynamicInitialValue.QUERYBUILDERVIEWS
						.getInitialValue() ? Enumeration.DynamicInitialValue.QUERYBUILDERVIEWS.getInitialValue()
								: seqMap1.get("querybuilderviews"))
						+ 1;

				seqNoTable = (seqMap1.get("querybuildertables") < Enumeration.DynamicInitialValue.QUERYBUILDERTABLES
						.getInitialValue() ? Enumeration.DynamicInitialValue.QUERYBUILDERTABLES.getInitialValue()
								: seqMap1.get("querybuildertables"))
						+ 1;


				if (designtemplatemapping.getNsampletypecode() == Enumeration.SampleType.GOODSIN.getType()) {
					String getQuery = " select ndesigntemplatemappingcode,nversionno  from designtemplatemapping where "
							+ "	nsampletypecode = " + designtemplatemapping.getNsampletypecode()
							+ "  and nsitecode="+userInfo.getNmastersitecode()+"	and ntransactionstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

					final List<DesignTemplateMapping> designtemplateList = jdbcTemplate.query(getQuery,
							new DesignTemplateMapping());
					if (!designtemplateList.isEmpty()) {

						updateQuery1 = "update designtemplatemapping set dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = "
								+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
								+ " where ndesigntemplatemappingcode = "
								+ designtemplateList.get(0).getNdesigntemplatemappingcode();
						nversionno = designtemplateList.get(0).getNversionno() + 1;
					} else {
						nversionno = 1;
					}

					final String updateQuery = "update designtemplatemapping set dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nversionno =" + nversionno
							+ ", ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ " where ndesigntemplatemappingcode = "
							+ designtemplatemapping.getNdesigntemplatemappingcode();

					List<DeleteValidation> deleteValidationList = objMapper.convertValue(
							inputMap.get("deletevalidationlist"), new TypeReference<List<DeleteValidation>>() {
							});
					designtemplatemapping.setNformcode((short) Enumeration.QualisForms.GOODSIN.getqualisforms());
					int ntransFormCode = designtemplatemapping.getNformcode();

					List<String> queryList = new ArrayList<String>();
					for (DeleteValidation deleteValidation : deleteValidationList) {
						queryList.add("(" + " (select nformcode from querybuildertables where nquerybuildertablecode="
								+ deleteValidation.getNquerybuildertablecode() + ")," + "'"
								+ deleteValidation.getSmastertablename() + "'," + "'"
								+ deleteValidation.getSmasterprimarykeyname() + "'," + "" + ntransFormCode + "," + "'"
								+ deleteValidation.getStranstablename() + "'," + "'"
								+ deleteValidation.getStranstableforeignkeyname() + "'," + "'"
								+ deleteValidation.getSjsonfieldname() + "'," + deleteValidation.getNisdynamicmaster()
								+ "," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNmastersitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
					}
					String queryString = "";
					if (queryList.size() > 0) {
						queryString = "INSERT INTO deletevalidation (nformcode, smastertablename,"
								+ " smasterprimarykeyname,ntransformcode, stranstablename, stranstableforeignkeyname,"
								+ "sjsonfieldname, nisdynamicmaster, dmodifieddate, nsitecode, nstatus) values "
								+ String.join(",", queryList)
								+ " on conflict on constraint unique_deletevalidation do nothing";
						jdbcTemplate.execute(queryString);
					}

					String viewName = ((String) inputMap.get("sviewname")).replaceAll(" ", "")
							.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
					String templateName = designtemplatemapping.getStemplatetypesname().replaceAll(" ", "")
							.replaceAll("[^a-zA-Z0-9]", "");

					String queryViewCheck = "select  * from  information_schema.views  where table_name='view_"
							+ viewName + "'";
					List<?> lst = jdbcTemplate.queryForList(queryViewCheck);

					if (!lst.isEmpty()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					} else {

						if (lst.isEmpty()) {

							createView(viewName, templateName, designtemplatemapping.getNdesigntemplatemappingcode(),
									designtemplatemapping.getNformcode(), false, true, false, null);

							final String query = "select * from qualisforms where nformcode="
									+ designtemplatemapping.getNformcode();
							QualisForms qualisForm = (QualisForms) jdbcUtilityFunction.queryForObject(query,
									QualisForms.class, jdbcTemplate);

							final JSONObject editObject = new JSONObject();
							editObject.put("tablename", new JSONObject() {
								{
									put("en-US", viewName);
								}
							});

							// editObject.put("tablename",qualisForm.getJsondata().get("sdisplayname"));
							List<Map<String, Object>> obj2 = (List<Map<String, Object>>) inputMap
									.get("jsqlquerycolumns");
							final JSONArray editObject1 = new JSONArray(obj2);
							JSONArray editObject2 = new JSONArray();
							editObject1.forEach(x -> {
								JSONObject obj1 = (JSONObject) x;
								obj1.put("tablename", "view_" + viewName);
								editObject2.put(obj1);

							});

							final String insertTable = "INSERT INTO querybuildertables (nquerybuildertablecode,nformcode,stablename,jsondata,nismastertable,nstatus,nsitecode,dmodifieddate) values"
									+ " (" + seqNoTable + ",-3,'view_" + viewName + "','" + editObject + "',3,1,"
									+ userInfo.getNmastersitecode() + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

							final String insertColumns = "INSERT INTO querybuildertablecolumns (nquerybuildertablecode,nformcode,stablename,sprimarykeyname"
									+ ",jstaticcolumns,jmultilingualcolumn,jdynamiccolumns,jnumericcolumns,jsqlquerycolumns,nstatus,nsitecode,dmodifieddate) values"
									+ " (" + seqNoTable + ",-3,'view_" + viewName + "','"
									+ inputMap.get("sprimarykeyname") + "','"
									+ stringUtilityFunction
									.replaceQuote(objMapper.writeValueAsString(inputMap.get("jdynamiccolumns")))
									+ "',null,null,'"
									+ stringUtilityFunction
									.replaceQuote(objMapper.writeValueAsString(inputMap.get("jnumericcolumns")))
									+ "'," + "'" + stringUtilityFunction.replaceQuote(editObject2.toString()) + "',1,"
									+ userInfo.getNmastersitecode() + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

							final String insertTablequerytabledetails = "INSERT INTO querytabledetails (nquerytabledetailcode,nquerybuildertablecode,nmodulecode,nformcode,nsitecode,nstatus,dmodifieddate) values"
									+ " (" + seqNoTable1 + "," + seqNoTable + ","
									+ designtemplatemapping.getNmodulecode() + ","
									+ designtemplatemapping.getNformcode() + "," + userInfo.getNmastersitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

							final JSONObject editObjectView = new JSONObject();
							editObjectView.put("displayname", qualisForm.getJsondata().get("sdisplayname"));

							// Map<String, Object> obj6 = null;
							List<Map<String, Object>> objView2 = (List<Map<String, Object>>) inputMap
									.get("sampleQuerybuilderViewCondition");
							List<Map<String, Object>> objView3 = (List<Map<String, Object>>) inputMap
									.get("sampleQuerybuilderViewSelect");

							Map<String, Object> obj5 = new HashMap<>();
							obj5.put("conditionfields", objView2);
							obj5.put("selectfields", objView3);

							JSONArray js = new JSONArray();
							js.put(obj5);

							final String insertviewTable = "INSERT INTO querybuilderviews (nquerybuilderviewscode,sviewname,jsondata,nstatus,dmodifieddate,nsitecode) values"
									+ " (" + seqNoTableView + ",'view_" + viewName + "','"
									+ stringUtilityFunction.replaceQuote(editObjectView.toString()) + "',1,'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNmastersitecode() + ");";

							final String insertviewColumns = "INSERT INTO querybuilderviewscolumns (sviewname,jsondata,nstatus,dmodifieddate,nsitecode) values"
									+ " ('view_" + viewName + "','"
									+ stringUtilityFunction.replaceQuote(js.get(0).toString()) + "',1,'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNmastersitecode() + " );";

							jdbcTemplate.execute(insertTable + insertColumns + insertTablequerytabledetails
									+ insertviewTable + insertviewColumns);

							jdbcTemplate.execute(" update seqnocredentialmanagement set nsequenceno = " + seqNoTable
									+ " where stablename='querybuildertables';"
									+ " update seqnocredentialmanagement set nsequenceno = " + seqNoTable1
									+ " where stablename='querytabledetails';"
									+ " update seqnocredentialmanagement set nsequenceno = " + seqNoTableView
									+ " where stablename='querybuilderviews';");

						}

					}
					if (!designtemplateList.isEmpty()) {
						jdbcTemplate.execute(updateQuery1);
					}
					jdbcTemplate.execute(updateQuery);

					final List<Object> savedListbefore1 = new ArrayList<>();


					savedListbefore1.add(oldTemplate);

					final List<Object> savedList = new ArrayList<>();

					DesignTemplateMapping NewTemplate = SerializationUtils.clone(oldTemplate);
					NewTemplate.setNtransactionstatus(
							(short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
					savedList.add(NewTemplate);

					auditUtilityFunction.fnInsertAuditAction(savedList, 2, savedListbefore1,
							Arrays.asList("IDS_APPROVEDTEMPLATEMAPPING"), userInfo);

					return getDesignTemplateMapping(designtemplatemapping.getNsampletypecode(),
							designtemplatemapping.getNregtypecode(), designtemplatemapping.getNregsubtypecode(),
							designtemplatemapping.getNdesigntemplatemappingcode(), designtemplatemapping.getNformcode(),
							userInfo);

				}else {
					String getQuery = "";
					// Added by sonia on 11th NOV 2024 for jira id:ALPD-5025
					if (designtemplatemapping.getNsampletypecode() == Enumeration.SampleType.MASTERS.getType()
							|| designtemplatemapping.getNsampletypecode() == Enumeration.SampleType.PROTOCOL
							.getType()) {
						getQuery = "select ndesigntemplatemappingcode,nversionno  from designtemplatemapping where "
								+ " nsampletypecode = " + designtemplatemapping.getNsampletypecode()
								+ " and nformcode = " + designtemplatemapping.getNformcode()
								+ "  and nsitecode="+userInfo.getNmastersitecode()+" and ntransactionstatus = "
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
					} else {
						getQuery = "select ndesigntemplatemappingcode,nversionno  from designtemplatemapping where "
								+ " nsampletypecode = " + designtemplatemapping.getNsampletypecode()
								+ " and nregtypecode = " + designtemplatemapping.getNregtypecode()
								+ " and nregsubtypecode = " + designtemplatemapping.getNregsubtypecode()
								+ "  and nsitecode="+userInfo.getNmastersitecode()+" and ntransactionstatus = "
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
					}

					final List<DesignTemplateMapping> designtemplateList = jdbcTemplate.query(getQuery,
							new DesignTemplateMapping());
					if (!designtemplateList.isEmpty()) {

						updateQuery1 = "update designtemplatemapping set dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = "
								+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
								+ " where ndesigntemplatemappingcode = "
								+ designtemplateList.get(0).getNdesigntemplatemappingcode();
						// jdbcTemplate.execute(updateQuery1);

						nversionno = designtemplateList.get(0).getNversionno() + 1;
					} else {
						nversionno = 1;
					}

					final String updateQuery = "update designtemplatemapping set dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nversionno =" + nversionno
							+ ", ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ " where ndesigntemplatemappingcode = "
							+ designtemplatemapping.getNdesigntemplatemappingcode();
					if (designtemplatemapping.getNsampletypecode() != Enumeration.SampleType.MASTERS.getType()) {
						jdbcTemplate.execute(updateQuery);
					}

					List<DeleteValidation> deleteValidationList = objMapper.convertValue(
							inputMap.get("deletevalidationlist"), new TypeReference<List<DeleteValidation>>() {
							});

					int ntransFormCode = designtemplatemapping.getNformcode();
					if (ntransFormCode == -1) {
						ntransFormCode = Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms();
					}
					List<String> queryList = new ArrayList<String>();
					for (DeleteValidation deleteValidation : deleteValidationList) {
						queryList.add("(" + " (select nformcode from querybuildertables where nquerybuildertablecode="
								+ deleteValidation.getNquerybuildertablecode() + ")," + "'"
								+ deleteValidation.getSmastertablename() + "'," + "'"
								+ deleteValidation.getSmasterprimarykeyname() + "'," + "" + ntransFormCode + "," + "'"
								+ deleteValidation.getStranstablename() + "'," + "'"
								+ deleteValidation.getStranstableforeignkeyname() + "'," + "'"
								+ deleteValidation.getSjsonfieldname() + "'," + deleteValidation.getNisdynamicmaster()
								+ "," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNmastersitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
					}
					String queryString = "";
					if (queryList.size() > 0) {
						queryString = "INSERT INTO deletevalidation (nformcode, smastertablename,"
								+ " smasterprimarykeyname,ntransformcode, stranstablename, stranstableforeignkeyname,"
								+ "sjsonfieldname, nisdynamicmaster, dmodifieddate, nsitecode, nstatus) values "
								+ String.join(",", queryList)
								+ " on conflict on constraint unique_deletevalidation do nothing";
						jdbcTemplate.execute(queryString);
					}

					if (designtemplatemapping.getNsampletypecode() == Enumeration.SampleType.MASTERS.getType()) {
						String viewName = ((String) inputMap.get("sviewname")).replaceAll(" ", "")
								.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
						String templateName = designtemplatemapping.getStemplatetypesname().replaceAll(" ", "")
								.replaceAll("[^a-zA-Z0-9]", "");

						String queryViewCheck = "select  * from  information_schema.views  where table_name='view_"
								+ viewName + "'";
						List<?> lst = jdbcTemplate.queryForList(queryViewCheck);
						if (!lst.isEmpty()) {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
									userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

						} else {
							String getSeqNoForm1 = "select stablename,nsequenceno from seqnocredentialmanagement "
									+ " where stablename in('usersrolescreen','controlmaster', 'sitecontrolmaster','userrolescreencontrol');";
							final List<SeqNoCredentialManagement> seqList = jdbcTemplate.query(getSeqNoForm1,
									new SeqNoCredentialManagement());

							final Map<String, Integer> seqMapMaster = seqList.stream()
									.collect(Collectors.toMap(SeqNoCredentialManagement::getStablename,
											seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));

							final int seqNoScreen = seqMapMaster.get("usersrolescreen") + 1;
							final int seqNoControlMaster = seqMapMaster.get("controlmaster") + 1;
							final int seqNoSiteControlMaster = seqMapMaster.get("sitecontrolmaster") + 1;
							final int seqNoScreenControl = seqMapMaster.get("userrolescreencontrol") + 1;

							int querybuildertablecode = -1;
							if (lst.isEmpty() && designtemplateList.isEmpty()) {

								querybuildertablecode = seqNoTable;
								// create View
								createView(viewName, templateName,
										designtemplatemapping.getNdesigntemplatemappingcode(),
										designtemplatemapping.getNformcode(), true, false, false, null);

								final String query = "select * from qualisforms where nformcode="
										+ designtemplatemapping.getNformcode();
								QualisForms qualisForm = (QualisForms) jdbcUtilityFunction.queryForObject(query,
										QualisForms.class, jdbcTemplate);

								final JSONObject editObject = new JSONObject();
								editObject.put("tablename", qualisForm.getJsondata().get("sdisplayname"));
								editObject.put("classUrl", "dynamicmaster");
								editObject.put("component", "Dynamic");
								editObject.put("methodUrl", "DynamicMaster");
								editObject.put("isMasterAdd", true);
								editObject.put("addControlCode", seqNoControlMaster);
								editObject.put("editControlCode", seqNoControlMaster + 1);

								List<Map<String, Object>> obj2 = (List<Map<String, Object>>) inputMap
										.get("jsqlquerycolumns");
								final JSONArray editObject1 = new JSONArray(obj2);
								JSONArray editObject2 = new JSONArray();
								editObject1.forEach(x -> {
									JSONObject obj1 = (JSONObject) x;
									obj1.put("tablename", "view_" + viewName);
									editObject2.put(obj1);

								});

								final String insertTable = "INSERT INTO querybuildertables (nquerybuildertablecode,nformcode,stablename,jsondata,nismastertable,nstatus,nsitecode,dmodifieddate) values"
										+ " (" + seqNoTable + "," + designtemplatemapping.getNformcode() + ",'view_"
										+ viewName + "','" + editObject + "',3,1," + userInfo.getNmastersitecode()
										+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

								final String insertColumns = "INSERT INTO querybuildertablecolumns (nquerybuildertablecode,nformcode,stablename,sprimarykeyname"
										+ ",jstaticcolumns,jmultilingualcolumn,jdynamiccolumns,jnumericcolumns,jsqlquerycolumns,nstatus,nsitecode,dmodifieddate) values"
										+ " (" + seqNoTable + "," + designtemplatemapping.getNformcode() + ",'view_"
										+ viewName + "','" + inputMap.get("sprimarykeyname") + "','"
										+ stringUtilityFunction.replaceQuote(
												objMapper.writeValueAsString(inputMap.get("jdynamiccolumns")))
										+ "',null,null,'"
										+ stringUtilityFunction.replaceQuote(
												objMapper.writeValueAsString(inputMap.get("jnumericcolumns")))
										+ "'," + "'" + stringUtilityFunction.replaceQuote(editObject2.toString())
										+ "',1," + userInfo.getNmastersitecode() + ",'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

								final String insertTablequerytabledetails = "INSERT INTO querytabledetails (nquerytabledetailcode,nquerybuildertablecode,nmodulecode,nformcode,nsitecode,nstatus,dmodifieddate) values"
										+ " (" + seqNoTable1 + "," + seqNoTable + ","
										+ designtemplatemapping.getNmodulecode() + ","
										+ designtemplatemapping.getNformcode() + "," + userInfo.getNmastersitecode()
										+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

								final JSONObject editObjectView = new JSONObject();
								editObjectView.put("displayname", qualisForm.getJsondata().get("sdisplayname"));

								// Map<String, Object> obj6 = null;
								List<Map<String, Object>> objView2 = (List<Map<String, Object>>) inputMap
										.get("sampleQuerybuilderViewCondition");
								List<Map<String, Object>> objView3 = (List<Map<String, Object>>) inputMap
										.get("sampleQuerybuilderViewSelect");

								Map<String, Object> obj5 = new HashMap<>();
								obj5.put("conditionfields", objView2);
								obj5.put("selectfields", objView3);
								JSONArray js = new JSONArray();
								js.put(obj5);

								final String insertviewTable = "INSERT INTO querybuilderviews (nquerybuilderviewscode,sviewname,jsondata,nstatus,dmodifieddate,nsitecode) values"
										+ " (" + seqNoTableView + ",'view_" + viewName + "','"
										+ stringUtilityFunction.replaceQuote(editObjectView.toString()) + "',1,'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
										+ userInfo.getNmastersitecode() + ");";

								final String insertviewColumns = "INSERT INTO querybuilderviewscolumns (sviewname,jsondata,nstatus,dmodifieddate,nsitecode) values"
										+ " ('view_" + viewName + "','"
										+ stringUtilityFunction.replaceQuote(js.get(0).toString()) + "',1,'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
										+ userInfo.getNmastersitecode() + " );";

								jdbcTemplate.execute(insertTable + insertColumns + insertTablequerytabledetails
										+ insertviewTable + insertviewColumns);

								jdbcTemplate.execute(" update seqnocredentialmanagement set nsequenceno = " + seqNoTable
										+ " where stablename='querybuildertables';"
										+ " update seqnocredentialmanagement set nsequenceno = " + seqNoTable1
										+ " where stablename='querytabledetails';"
										+ " update seqnocredentialmanagement set nsequenceno = " + seqNoTableView
										+ " where stablename='querybuilderviews';");
							} else {

								createView(viewName, templateName,
										designtemplatemapping.getNdesigntemplatemappingcode(),
										designtemplatemapping.getNformcode(), true, false, true, null);

								List<Map<String, Object>> obj2 = (List<Map<String, Object>>) inputMap
										.get("jsqlquerycolumns");
								final JSONArray editObject1 = new JSONArray(obj2);
								JSONArray editObject2 = new JSONArray();
								editObject1.forEach(x -> {
									JSONObject obj1 = (JSONObject) x;
									obj1.put("tablename", "view_" + viewName);
									editObject2.put(obj1);

								});

								final String queryForm = "select * from qualisforms where nformcode="
										+ designtemplatemapping.getNformcode();
								QualisForms qualisForm = (QualisForms) jdbcUtilityFunction.queryForObject(queryForm,
										QualisForms.class, jdbcTemplate);

								final JSONObject editObject = new JSONObject();
								editObject.put("tablename", qualisForm.getJsondata().get("sdisplayname"));
								editObject.put("classUrl", "dynamicmaster");
								editObject.put("component", "Dynamic");
								editObject.put("methodUrl", "DynamicMaster");
								editObject.put("isMasterAdd", true);
								editObject.put("addControlCode", seqNoControlMaster);
								editObject.put("editControlCode", seqNoControlMaster + 1);

								String query = "select stablename  from querybuildertables where nformcode="
										+ designtemplatemapping.getNformcode();
								final QueryBuilderTables stablename = (QueryBuilderTables) jdbcUtilityFunction
										.queryForObject(query, QueryBuilderTables.class, jdbcTemplate);
								final String insertTable = "update  querybuildertablecolumns set stablename='view_"
										+ viewName + "' ,dmodifieddate='"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', sprimarykeyname='"
										+ inputMap.get("sprimarykeyname") + "' ,jstaticcolumns='"
										+ stringUtilityFunction.replaceQuote(
												objMapper.writeValueAsString(inputMap.get("jdynamiccolumns")))
										+ "'," + " jnumericcolumns='"
										+ stringUtilityFunction.replaceQuote(
												objMapper.writeValueAsString(inputMap.get("jnumericcolumns")))
										+ "',jsqlquerycolumns='"
										+ stringUtilityFunction.replaceQuote(editObject2.toString())
										+ "' where nformcode=" + designtemplatemapping.getNformcode() + ";";

								final String insertTables = "update  querybuildertables set stablename='view_"
										+ viewName + "' ,dmodifieddate='"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,jsondata='"
										+ editObject + "'" + " where nformcode=" + designtemplatemapping.getNformcode()
										+ ";";

								List<Map<String, Object>> objView2 = (List<Map<String, Object>>) inputMap
										.get("sampleQuerybuilderViewCondition");
								List<Map<String, Object>> objView3 = (List<Map<String, Object>>) inputMap
										.get("sampleQuerybuilderViewSelect");

								Map<String, Object> obj5 = new HashMap<>();

								obj5.put("conditionfields", objView2);
								obj5.put("selectfields", objView3);

								JSONArray js1 = new JSONArray();
								js1.put(obj5);

								final String insertTableView = "update  querybuilderviewscolumns set sviewname='view_"
										+ viewName + "' , dmodifieddate='"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',jsondata='"
										+ stringUtilityFunction.replaceQuote(js1.get(0).toString())
										+ "' where sviewname='" + stablename.getStablename() + "';";

								final String insertTableViewColumns = "update  querybuilderviews set sviewname='view_"
										+ viewName + "' , dmodifieddate='"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',jsondata='"
										+ stringUtilityFunction.replaceQuote(js1.get(0).toString())
										+ "' where sviewname='" + stablename.getStablename() + "';";

								final String updateDesignTemplate = "delete from dynamicmaster where  nformcode="
										+ designtemplatemapping.getNformcode() + ";";

								jdbcTemplate.execute(insertTables + insertTable + insertTableView + updateDesignTemplate
										+ insertTableViewColumns);

							}

							///////////////////
							String insertForms = "insert into usersrolescreen (nuserrolescreencode, nformcode, nuserrolecode,dmodifieddate, "
									+ " nstatus) values (" + seqNoScreen + ", " + designtemplatemapping.getNformcode()
									+ ", -1,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', 1); ";

							final String controlMasterQry = "select ncontrolcode from controlmaster where nformcode = "
									+ designtemplatemapping.getNformcode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

							final List<ControlMaster> cmList = jdbcTemplate.query(controlMasterQry,
									new ControlMaster());
							if (cmList.isEmpty()) {
								final JSONObject addObject = new JSONObject();
								addObject.put("scontrolids", new JSONObject() {
									{
										put("en-US", "Add");
									}
								});
								final JSONObject editObject = new JSONObject();
								editObject.put("scontrolids", new JSONObject() {
									{
										put("en-US", "Edit");
									}
								});
								final JSONObject deleteObject = new JSONObject();
								deleteObject.put("scontrolids", new JSONObject() {
									{
										put("en-US", "Delete");
									}
								});

								final JSONObject barcodeObject = new JSONObject();
								barcodeObject.put("scontrolids", new JSONObject() {
									{
										put("en-US", "Barcode");
									}
								});

								insertForms += "insert into controlmaster "
										+ " (ncontrolcode, nformcode, scontrolname,jsondata,nisesigncontrol, nstatus,dmodifieddate) values ("
										+ seqNoControlMaster + ", " + designtemplatemapping.getNformcode()
										+ ", N'AddDynamicMaster', '" + addObject + "', 4, 1, '"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'),("
										+ (seqNoControlMaster + 1) + ", " + designtemplatemapping.getNformcode()
										+ ", N'EditDynamicMaster', '" + editObject + "', 4, 1,'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'),("
										+ (seqNoControlMaster + 2) + ", " + designtemplatemapping.getNformcode()
										+ ", N'DeleteDynamicMaster', '" + deleteObject + "', 4, 1,'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "')," + "("
										+ (seqNoControlMaster + 3) + ", " + designtemplatemapping.getNformcode()
										+ ", N'BarcodeDynamicMaster', '" + barcodeObject + "', 4, 1,'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "');";

								insertForms += "insert into sitecontrolmaster "
										+ " (nsitecontrolcode, nsitecode,nformcode, ncontrolcode,nneedesign,nisbarcodecontrol,nisreportcontrol,nisemailrequired,dmodifieddate,nstatus) values ("
										+ seqNoSiteControlMaster + ",-1, " + designtemplatemapping.getNformcode() + ", "
										+ seqNoControlMaster + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", '"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),("
										+ (seqNoSiteControlMaster + 1) + ",-1, " + designtemplatemapping.getNformcode()
										+ ", " + (seqNoControlMaster + 1) + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", '"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + ",("
										+ (seqNoSiteControlMaster + 2) + ",-1, " + designtemplatemapping.getNformcode()
										+ ", " + (seqNoControlMaster + 2) + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", '"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + ",("
										+ (seqNoSiteControlMaster + 3) + ",-1, " + designtemplatemapping.getNformcode()
										+ ", " + (seqNoControlMaster + 3) + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", '"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

								insertForms += "insert into userrolescreencontrol "
										+ " (nuserrolecontrolcode,ncontrolcode, nformcode, nuserrolecode, nneedrights,dmodifieddate) values ("
										+ seqNoScreenControl + "," + seqNoControlMaster + ","
										+ designtemplatemapping.getNformcode() + ",-1, 3,'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "),("
										+ (seqNoScreenControl + 1) + "," + (seqNoControlMaster + 1) + ","
										+ designtemplatemapping.getNformcode() + ",-1, 3,'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ")" + ",("
										+ (seqNoScreenControl + 2) + "," + (seqNoControlMaster + 2) + ","
										+ designtemplatemapping.getNformcode() + ",-1, 3,'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ")" + ",("
										+ (seqNoScreenControl + 3) + "," + (seqNoControlMaster + 3) + ","
										+ designtemplatemapping.getNformcode() + ",-1, 3,'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ");";

								////////////////////////////////////////

								final String sUpdateSeqNoQuery = "update seqnocredentialmanagement set nsequenceno = nsequenceno+1 where stablename = 'usersrolescreen';";

								final String barcodecontrol = "insert into barcodecontrolconfig(nbarcodecontrolconfigcode,nformcode,ncontrolcode,nquerybuildertablecode,nisdynamic,nissubsample,nstatus)"
										+ " values((select max(nbarcodecontrolconfigcode) from barcodecontrolconfig)+1,"
										+ designtemplatemapping.getNformcode() + "," + (seqNoControlMaster + 3) + ","
										+ querybuildertablecode + ","
										+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

								jdbcTemplate.execute(barcodecontrol + insertForms + sUpdateSeqNoQuery
										+ " update seqnocredentialmanagement set nsequenceno = "
										+ (seqNoControlMaster + 3) + " where stablename='controlmaster';"
										+ " update seqnocredentialmanagement set nsequenceno = "
										+ (seqNoSiteControlMaster + 3) + " where stablename='sitecontrolmaster';"
										+ " update seqnocredentialmanagement set nsequenceno = "
										+ (seqNoScreenControl + 3) + " where stablename='userrolescreencontrol';"
										+ " update querybuildertables set  dmodifieddate='"
										+ dateUtilityFunction.getCurrentDateTime(userInfo)
										+ "', nismastertable=3 where nformcode=" + designtemplatemapping.getNformcode()
										+ ";" + "");

							}
						}
						// End of Masters///////

					}
					if (!designtemplateList.isEmpty()) {
						jdbcTemplate.execute(updateQuery1);
					}
					jdbcTemplate.execute(updateQuery);

					final List<Object> savedListbefore1 = new ArrayList<>();

					if (oldTemplate.getNformcode() == -1) {
						oldTemplate.setNformcode((short) -2);
					}

					savedListbefore1.add(oldTemplate);

					final List<Object> savedList = new ArrayList<>();

					DesignTemplateMapping NewTemplate = SerializationUtils.clone(oldTemplate);
					NewTemplate.setNtransactionstatus(
							(short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
					savedList.add(NewTemplate);

					auditUtilityFunction.fnInsertAuditAction(savedList, 2, savedListbefore1,
							Arrays.asList("IDS_APPROVEDTEMPLATEMAPPING"), userInfo);

					return getDesignTemplateMapping(designtemplatemapping.getNsampletypecode(),
							designtemplatemapping.getNregtypecode(), designtemplatemapping.getNregsubtypecode(),
							designtemplatemapping.getNdesigntemplatemappingcode(), designtemplatemapping.getNformcode(),
							userInfo);
				}

			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}


	@Override
	public ResponseEntity<Object> deleteDesignTemplateMapping(DesignTemplateMapping designtemplatemapping,
			UserInfo userInfo) throws Exception {

		DesignTemplateMapping oldTemplate = (DesignTemplateMapping) getDesignTemplateMappingById(
				designtemplatemapping.getNdesigntemplatemappingcode(), userInfo).getBody();

		if (oldTemplate == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if ((oldTemplate.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus())) {

				final String updateQuery = "update designtemplatemapping set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = -1"
						+ " where ndesigntemplatemappingcode = "
						+ designtemplatemapping.getNdesigntemplatemappingcode();
				jdbcTemplate.execute(updateQuery);

				final String updateQuery1 = "update mappedtemplatefieldprops set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = -1"
						+ " where ndesigntemplatemappingcode = "
						+ designtemplatemapping.getNdesigntemplatemappingcode();
				jdbcTemplate.execute(updateQuery1);

				final List<Object> savedList = new ArrayList<>();
				oldTemplate.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				if (oldTemplate.getNformcode() == -1) {
					oldTemplate.setNformcode((short) -2);
				}
				savedList.add(oldTemplate);

				auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, Arrays.asList("IDS_DELETETEMPLATEMAPPING"),
						userInfo);

			} else

			{
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDDELETE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

			}

			return getDesignTemplateMapping(designtemplatemapping.getNsampletypecode(),
					designtemplatemapping.getNregtypecode(), designtemplatemapping.getNregsubtypecode(), -1,
					designtemplatemapping.getNformcode(), userInfo);
		}
	}

	@Override
	public ResponseEntity<Object> getMappedFieldProps(DesignTemplateMapping designtemplatemapping, UserInfo userInfo)
			throws Exception {
		final String getTemplate = " select ndesigntemplatemappingcode, nsampletypecode, nformcode, nregtypecode, nregsubtypecode, "
				+ "nregsubtypeversioncode, nformwisetypecode,nreactregtemplatecode, nsubsampletemplatecode, ntransactionstatus, "
				+ "nversionno,nsitecode, nstatus from designtemplatemapping" + " where ndesigntemplatemappingcode = "
				+ designtemplatemapping.getNdesigntemplatemappingcode() + "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DesignTemplateMapping designTemplateMapping = (DesignTemplateMapping) jdbcUtilityFunction
				.queryForObject(getTemplate, DesignTemplateMapping.class, jdbcTemplate);
		if (designTemplateMapping == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final String getMappedProps = " select jsondata as jsondataobj from mappedtemplatefieldprops"
				+ " where ndesigntemplatemappingcode = " + designtemplatemapping.getNdesigntemplatemappingcode()
				+ "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		DesignTemplateMapping mappedTemplate = jdbcTemplate.queryForObject(getMappedProps, new DesignTemplateMapping());
		return new ResponseEntity<>(mappedTemplate, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> configureDesignTemplateMapping(DesignTemplateMapping designTemplateMapping,
			UserInfo userInfo) throws Exception {
		final String getTemplate = " select * from designtemplatemapping" + " where ndesigntemplatemappingcode = "
				+ designTemplateMapping.getNdesigntemplatemappingcode() + "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DesignTemplateMapping oldDesignTemplateMapping = jdbcTemplate.queryForObject(getTemplate,
				new DesignTemplateMapping());
		if (oldDesignTemplateMapping == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		// final ObjectMapper mapper =new ObjectMapper();
		final String updateQuery = " update mappedtemplatefieldprops set dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', jsondata = '"// +mapper.writeValueAsString(designTemplateMapping.getJsondataobj())+"'::jsonb
				// "
				+ stringUtilityFunction.replaceQuote(new JSONObject(designTemplateMapping.getJsondataobj()).toString())
				+ "'::jsonb  " + " where ndesigntemplatemappingcode = "
				+ designTemplateMapping.getNdesigntemplatemappingcode();

		jdbcTemplate.execute(updateQuery);
		return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_FIELDSCONFIGURED", userInfo.getSlanguagefilename()),
				HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<Object> getAuditMappedFieldProps(DesignTemplateMapping designtemplatemapping,
			UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		final String getTemplate = " select * from designtemplatemapping" + " where ndesigntemplatemappingcode = "
				+ designtemplatemapping.getNdesigntemplatemappingcode() + "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DesignTemplateMapping designTemplateMapping = (DesignTemplateMapping) jdbcUtilityFunction
				.queryForObject(getTemplate, DesignTemplateMapping.class, jdbcTemplate);
		if (designTemplateMapping == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final String getMappedProps = " select jsondata as jsondataobj from mappedtemplatefieldprops"
				+ " where ndesigntemplatemappingcode = " + designtemplatemapping.getNdesigntemplatemappingcode()
				+ "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DesignTemplateMapping mappedTemplate = jdbcTemplate.queryForObject(getMappedProps,
				new DesignTemplateMapping());

		String auditQuery = "";
		if (designTemplateMapping.getNsampletypecode() == Enumeration.SampleType.MASTERS.getType()) {
			auditQuery = " select dart.nformcode, dart.stablename, dart.jsondata as jsondataobj,"
					+ " qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' as sformname "
					+ " from dynamicauditrecordtable dart, qualisforms qf "
					+ " where dart.ndesigntemplatemappingcode = "
					+ designtemplatemapping.getNdesigntemplatemappingcode() + " and dart.nformcode =qf.nformcode "
					+ " and dart.stablename in ('dynamicmaster')" + " and dart.nstatus = qf.nstatus and qf.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		} else if (designTemplateMapping.getNsampletypecode() == Enumeration.SampleType.GOODSIN.getType()) {
			auditQuery = " select dart.nformcode, dart.stablename, dart.jsondata as jsondataobj,"
					+ " qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' as sformname "
					+ " from  dynamicauditrecordtable dart, qualisforms qf "
					+ " where  dart.ndesigntemplatemappingcode = "
					+ designtemplatemapping.getNdesigntemplatemappingcode() + " and dart.nformcode =qf.nformcode "
					+ " and dart.stablename in ('goodsinsample')" + " and dart.nstatus = qf.nstatus and qf.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			// Added by sonia on 11th NOV 2024 for jira id:ALPD-5025
		} else if (designTemplateMapping.getNsampletypecode() == Enumeration.SampleType.PROTOCOL.getType()) {
			auditQuery = " select dart.nformcode, dart.stablename, dart.jsondata as jsondataobj,"
					+ " qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' as sformname "
					+ " from  dynamicauditrecordtable dart, qualisforms qf "
					+ " where  dart.ndesigntemplatemappingcode = "
					+ designtemplatemapping.getNdesigntemplatemappingcode() + " and dart.nformcode =qf.nformcode "
					+ " and dart.stablename in ('protocol')" + " and dart.nstatus = qf.nstatus and qf.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		} else {
			auditQuery = " select dart.nformcode, dart.stablename, dart.jsondata as jsondataobj,"
					+ " qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' as sformname "
					+ " from dynamicauditrecordtable dart, qualisforms qf " + " where dart.nregtypecode="
					+ inputMap.get("nregtypecode") + " and dart.nregsubtypecode=" + inputMap.get("nregsubtypecode")
					+ "  and dart.ndesigntemplatemappingcode = " + designtemplatemapping.getNdesigntemplatemappingcode()
					+ " and dart.nformcode =qf.nformcode "
					+ " and dart.stablename in ('registration','registrationsample')"
					+ " and dart.nstatus = qf.nstatus and qf.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}

		final List<DesignTemplateMapping> auditTemplateList = jdbcTemplate.query(auditQuery,
				new DesignTemplateMapping());


		final Map<Short, Map<String, Object>> auditTemplateMap = auditTemplateList.stream()
				.collect(Collectors.groupingBy(e -> e.getNformcode(),
						Collectors.toMap(DesignTemplateMapping::getStablename, templateMapping -> templateMapping)));
		final Map<String, Object> returnMap = new HashMap<String, Object>() {
			{
				put("MappedTemplateFieldProps", mappedTemplate);
				put("DynamicAuditRecordTable", auditTemplateMap);
			}
		};

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> configureDynamicAudit(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String getTemplate = " select * from designtemplatemapping" + " where ndesigntemplatemappingcode = "
				+ inputMap.get("ndesigntemplatemappingcode") + "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final DesignTemplateMapping oldDesignTemplateMapping = jdbcTemplate.queryForObject(getTemplate,
				new DesignTemplateMapping());
		if (oldDesignTemplateMapping == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> auditData = objMapper.convertValue(inputMap.get("auditdata"), Map.class);

		final List<String> auditList = new ArrayList<String>();
		auditData.entrySet().stream().forEach(e -> {// System.out.println(e.getKey() + ":" + e.getValue())

			Map<String, Object> tableData = objMapper.convertValue(e.getValue(), Map.class);

			tableData.entrySet().stream().forEach(e1 -> {// System.out.println(e.getKey() + ":" + e.getValue())
				final Map<String, Object> jsonObject = (Map<String, Object>) e1.getValue();
				final JSONObject sampleAuditData = new JSONObject((Map<String, Object>) jsonObject.get("jsondataobj"));
				try {
					auditList.add("update dynamicauditrecordtable " + " set jsondata = '"
							+ stringUtilityFunction.replaceQuote(sampleAuditData.toString()) + "', dauditdate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nformcode=" + e.getKey()
							+ " and nregtypecode=" + oldDesignTemplateMapping.getNregtypecode()
							+ " and nregsubtypecode =" + oldDesignTemplateMapping.getNregsubtypecode()
							+ " and ndesigntemplatemappingcode="
							+ oldDesignTemplateMapping.getNdesigntemplatemappingcode() + " and stablename='"
							+ e1.getKey() + "'");
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			});

		});

		jdbcTemplate.execute(String.join(";", auditList));
		return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_FIELDSCONFIGURED", userInfo.getSlanguagefilename()),
				HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<Object> updateConfigureSendToStore(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		String updateQuery = "";
		String str = "select ndesigntemplatemappingcode from mappedtemplatefieldprops where"
				+ " nsitecode="+userInfo.getNmastersitecode()+" and ndesigntemplatemappingcode=" + inputMap.get("ndesigntemplatemappingcode");
		final List<DesignTemplateMapping> mappedTemplate = jdbcTemplate.query(str, new DesignTemplateMapping());
		if (mappedTemplate.size() > 0) {
			String query = "select b.* from (select jsonb_object_keys(jsondata) as keys from mappedtemplatefieldprops "
					+ " where ndesigntemplatemappingcode=" + inputMap.get("ndesigntemplatemappingcode")
					+ " and nstatus = 1)b " + " where b.keys='senttostoragefields'";
			final List<DesignTemplateMapping> templatename = jdbcTemplate.query(query, new DesignTemplateMapping());
			if (templatename.size() > 0) {
				updateQuery = "update mappedtemplatefieldprops set jsondata = jsondata ||    '"
						+ stringUtilityFunction.replaceQuote(
								(mapper.writeValueAsString(inputMap.get("senttostoragefields"))).toString())
						+ "'::jsonb " + " where ndesigntemplatemappingcode="
						+ inputMap.get("ndesigntemplatemappingcode") + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			} else {
				updateQuery = "update mappedtemplatefieldprops set jsondata = jsondata || jsonb_build_object ('senttostoragefields', '"
						+ stringUtilityFunction
						.replaceQuote((mapper.writeValueAsString(inputMap.get("updatesendtostore"))).toString())
						+ "'::jsonb )" + " where ndesigntemplatemappingcode="
						+ inputMap.get("ndesigntemplatemappingcode") + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			}
			jdbcTemplate.execute(updateQuery);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_FIELDSCONFIGURED", userInfo.getSlanguagefilename()),
				HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<Object> getMappedfield(final int ndesigntemplatemappingcode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String query = "select ndesigntemplatemappingcode,jsondata->'senttostoragefields' as jsondataobj from mappedtemplatefieldprops where "
				+ "nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and nsitecode="+userInfo.getNmastersitecode()+" and  ndesigntemplatemappingcode=" + ndesigntemplatemappingcode;
		final List<DesignTemplateMapping> mappedTemplate = jdbcTemplate.query(query, new DesignTemplateMapping());
		outputMap.put("mappedFields", mappedTemplate);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getExportMappedFieldProps(DesignTemplateMapping designtemplatemapping,
			UserInfo userInfo) throws Exception {
		final String getTemplate = " select * from designtemplatemapping where ndesigntemplatemappingcode = "
				+ designtemplatemapping.getNdesigntemplatemappingcode() + "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DesignTemplateMapping designTemplateMapping = (DesignTemplateMapping) jdbcUtilityFunction
				.queryForObject(getTemplate, DesignTemplateMapping.class, jdbcTemplate);
		if (designTemplateMapping == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final String getMappedProps = " select jsondata as jsondataobj from mappedtemplatefieldprops"
				+ " where ndesigntemplatemappingcode = " + designtemplatemapping.getNdesigntemplatemappingcode()
				+ "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DesignTemplateMapping mappedTemplate = jdbcTemplate.queryForObject(getMappedProps,
				new DesignTemplateMapping());

		return new ResponseEntity<>(mappedTemplate, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getConfigureCheckList(int ndesigntemplatemappingcode, UserInfo userInfo)
			throws Exception {
		String getTemplate = " select * from designtemplatemapping where ndesigntemplatemappingcode = "
				+ ndesigntemplatemappingcode + "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DesignTemplateMapping designTemplateMapping = (DesignTemplateMapping) jdbcUtilityFunction
				.queryForObject(getTemplate, DesignTemplateMapping.class, jdbcTemplate);
		if (designTemplateMapping == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		getTemplate = " select jsondata as jsondataobj from mappedtemplatefieldprops"
				+ " where ndesigntemplatemappingcode = " + ndesigntemplatemappingcode + "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DesignTemplateMapping mappedTemplate = jdbcTemplate.queryForObject(getTemplate,
				new DesignTemplateMapping());

		final Map<String, Object> rturnMap = new HashMap<String, Object>();

		Map<String, Object> map = mappedTemplate.getJsondataobj();

		if (map.containsKey("checklist")) {

			Map<String, Object> map1 =  (Map<String, Object>) map.get("checklist");

			int nchecklistversioncode = (int) map1.get("nchecklistversioncode");

			getTemplate = " select cv.*,cl.*,t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
			+ "' stransdisplaystatus from checklistversion cv,checklist cl,transactionstatus t "
			+ " where cv.ntransactionstatus=t.ntranscode and cv.nchecklistcode=cl.nchecklistcode and cv.nchecklistversioncode="
			+ nchecklistversioncode + "  and cv.nsitecode="+userInfo.getNmastersitecode()+"  and cl.nsitecode="+userInfo.getNmastersitecode()+" and cv.nstatus="
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cl.nstatus="
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cl.nsitecode="
			+ userInfo.getNmastersitecode();

			ChecklistVersion cv = (ChecklistVersion) jdbcUtilityFunction.queryForObject(getTemplate,
					ChecklistVersion.class, jdbcTemplate);
			rturnMap.put("ChecklistVersion", cv);
			if (cv != null) {

				final String strQuery2 = " select vb.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "',"
						+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as smandatory,"
						+ " vb.nchecklistqbcode,cq.squestion,coalesce(cc.jsondata->'sdisplayname'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "', cc.jsondata->'sdisplayname'->>'en-US') as scomponentname,qc.schecklistqbcategoryname"
						+ " from checklistversionqb vb,checklistqb cq,checklistcomponent cc,transactionstatus ts,checklistqbcategory qc"
						+ " where vb.nchecklistqbcategorycode=qc.nchecklistqbcategorycode "
						+ " and vb.nmandatoryfield=ts.ntranscode and vb.nchecklistqbcode=cq.nchecklistqbcode "
						+ " and cq.nchecklistcomponentcode=cc.nchecklistcomponentcode" + " "
						+ " and vb.nsitecode="+userInfo.getNmastersitecode()+ "  and cq.nsitecode="+userInfo.getNmastersitecode()+" "
						+ " and cc.nsitecode="+userInfo.getNmastersitecode()+"  and qc.nsitecode="+userInfo.getNmastersitecode()+" and vb.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cq.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vb.nchecklistversioncode="
						+ nchecklistversioncode;
				List<ChecklistVersionQB> lstVersionQB = jdbcTemplate.query(strQuery2,
						new ChecklistVersionQB());

				rturnMap.put("ChecklistVersionQB", lstVersionQB);
			}

		} else {
			rturnMap.put("ChecklistVersion", new ArrayList<>());

		}
		return new ResponseEntity<>(rturnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getConfigureCheckListLatestVersion(Map<String, Object> inputMap, UserInfo userInfo) {
		final String query = "select cv.*,cl.schecklistname||'-'||cv.schecklistversionname||'-'||cv.schecklistversiondesc as schecklistname "
				+ " from checklistversion cv,checklist cl "
				+ " where cv.nchecklistcode=cl.nchecklistcode" + "  and cl.nsitecode="+userInfo.getNmastersitecode()+"  and cv.nsitecode="+userInfo.getNmastersitecode()+" and cv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and cv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cl.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cl.nsitecode="
				+ userInfo.getNmastersitecode();

		List<ChecklistVersion> cv = jdbcTemplate.query(query, new ChecklistVersion());
		return new ResponseEntity<>(cv, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReleaseSampleFilterFields(UserInfo userInfo) throws Exception {
		final String query = "select jsondata from releasesamplefilterfields " + " where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<Map<String, Object>> listDataList = jdbcTemplate.queryForList(query);
		return new ResponseEntity<>(listDataList, HttpStatus.OK);
	}

}
