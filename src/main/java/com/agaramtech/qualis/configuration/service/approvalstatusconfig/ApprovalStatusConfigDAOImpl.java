package com.agaramtech.qualis.configuration.service.approvalstatusconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.configuration.model.ApprovalStatusConfig;
import com.agaramtech.qualis.configuration.model.ApprovalSubType;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.credential.model.QualisForms;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ApprovalStatusConfigDAOImpl implements ApprovalStatusConfigDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalStatusConfigDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getApprovalStatusConfig(final UserInfo userinfo) throws Exception {
		LOGGER.info("getApprovalStatusConfig");
		final Map<String, Object> objmap = new HashMap<>();
		int nregtypecode = -1;
		int nregsubtypecode = -1;
		int nformcode = -1;
		int napprovalsubtypecode = -1;
		String query = "";
		List<RegistrationType> registrationTypes = null;
		List<QualisForms> qualisforms = null;
		List<ApprovalSubType> approvalsubtype = null;
		Map<String, Object> defaultsubTypeValue = null;
		Map<String, Object> defaultform = null;
		Map<String, Object> defaultregtype = null;
		List<RegistrationSubType> regSubTypeList = null;
		Map<String, Object> defaultRegSubType = null;
		String addString = "";
		String approvlsubtypeQuery = "";
		String forms = "";

		final String getSampleTypeQry = "select nsampletypecode,ncategorybasedflowrequired,coalesce(jsondata->'sampletypename'->>'"
				+ userinfo.getSlanguagetypecode()
				+ "	',jsondata->'sampletypename'->>'en-US') as ssampletypename,nsorter "
				+ " from sampletype where nsampletypecode > 0  and nstatus in( -1,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and nsitecode="
				+ userinfo.getNmastersitecode() + " order by nsorter";

		final List<SampleType> sampleTypes = jdbcTemplate.query(getSampleTypeQry, new SampleType());

		final Map<String, Object> defaultsamples = new HashMap<>();
		defaultsamples.put("label", sampleTypes.get(sampleTypes.size() - 1).getSsampletypename());
		defaultsamples.put("value", sampleTypes.get(sampleTypes.size() - 1).getNsampletypecode());
		defaultsamples.put("item", sampleTypes.get(sampleTypes.size() - 1));

		final int sampleTypeValue = sampleTypes.get(sampleTypes.size() - 1).getNsampletypecode();

		if (sampleTypeValue != Enumeration.SampleType.MASTERS.getType()) {
			final String getRegistrationQry = getRegistrationType(sampleTypeValue, userinfo);
			registrationTypes = jdbcTemplate.query(getRegistrationQry, new RegistrationType());
			if (!registrationTypes.isEmpty()) {

				defaultregtype = new HashMap<>();
				defaultregtype.put("label", registrationTypes.get(registrationTypes.size() - 1).getSregtypename());
				defaultregtype.put("value", registrationTypes.get(registrationTypes.size() - 1).getNregtypecode());
				defaultregtype.put("item", registrationTypes.get(registrationTypes.size() - 1));

				nregtypecode = registrationTypes.get(registrationTypes.size() - 1).getNregtypecode();

				final String getRegistrationSubQry = regSubtypeQuery(nregtypecode, userinfo);

				regSubTypeList = jdbcTemplate.query(getRegistrationSubQry, new RegistrationSubType());

				defaultRegSubType = new HashMap<>();
				if (!regSubTypeList.isEmpty()) {
					defaultRegSubType.put("value", regSubTypeList.get(regSubTypeList.size() - 1).getNregsubtypecode());
					defaultRegSubType.put("label", regSubTypeList.get(regSubTypeList.size() - 1).getSregsubtypename());
					defaultRegSubType.put("item", regSubTypeList.get(regSubTypeList.size() - 1));

					nregsubtypecode = regSubTypeList.get(registrationTypes.size() - 1).getNregsubtypecode();

					if (!defaultRegSubType.isEmpty()) {

						forms = getTransQualisForms(nregtypecode, nregsubtypecode, userinfo);

						qualisforms = jdbcTemplate.query(forms, new QualisForms());

						defaultform = new HashMap<>();
						if (!qualisforms.isEmpty()) {
							defaultform.put("value", qualisforms.get(qualisforms.size() - 1).getNformcode());
							defaultform.put("item", qualisforms.get(qualisforms.size() - 1));
							defaultform.put("label", qualisforms.get(qualisforms.size() - 1).getSdisplayname());

							nformcode = qualisforms.get(qualisforms.size() - 1).getNformcode();
						} else {
							objmap.put("qualisForms", null);
							objmap.put("defaultForms", null);
						}

					}
				}
			} else {
				objmap.put("qualisForms", null);
				objmap.put("defaultForms", null);
			}

			addString = " asf.nstatusfunctioncode=ac.nstatusfunctioncode and qf.nformcode=ac.nformcode and "
					+ " ac.nregtypecode=rt.nregtypecode  and  rst.nregsubtypecode=ac.nregsubtypecode and  ac.nformcode="
					+ nformcode + "  and ac.nregtypecode=" + nregtypecode + " and  ac.nregsubtypecode="
					+ nregsubtypecode + " and  ac.ntranscode=ts.ntranscode and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and asf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  qf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ac.nstatusconfigcode>0  and ac.nsitecode=" + userinfo.getNmastersitecode()
					+ " order by 1 desc";

		} else {
			approvlsubtypeQuery = ",approvalsubtype ast";

			forms = getMasterQualisForms(userinfo);

			qualisforms = jdbcTemplate.query(forms, new QualisForms());
			if (!qualisforms.isEmpty()) {
				defaultform = new HashMap<>();
				defaultform.put("label", qualisforms.get(qualisforms.size() - 1).getSdisplayname());
				defaultform.put("value", qualisforms.get(qualisforms.size() - 1).getNformcode());
				defaultform.put("item", qualisforms.get(qualisforms.size() - 1));
				nformcode = qualisforms.get(qualisforms.size() - 1).getNformcode();
			} else {
				objmap.put("qualisForms", null);
				objmap.put("defaultForms", null);
			}

			final String strQuery = getApprovalSubType(userinfo);

			approvalsubtype = jdbcTemplate.query(strQuery, new ApprovalSubType());

			if (!approvalsubtype.isEmpty()) {
				defaultsubTypeValue = new HashMap<>();
				defaultsubTypeValue.put("label", approvalsubtype.get(approvalsubtype.size() - 1).getSsubtypename());
				defaultsubTypeValue.put("value",
						approvalsubtype.get(approvalsubtype.size() - 1).getNapprovalsubtypecode());
				defaultsubTypeValue.put("item", approvalsubtype.get(approvalsubtype.size() - 1));
				napprovalsubtypecode = approvalsubtype.get(approvalsubtype.size() - 1).getNapprovalsubtypecode();
			}

			addString = " asf.nstatusfunctioncode=ac.nstatusfunctioncode and qf.nformcode=ac.nformcode and "
					+ "  ac.nregtypecode=rt.nregtypecode and ac.napprovalsubtypecode=ast.napprovalsubtypecode  and  rst.nregsubtypecode=ac.nregsubtypecode and  ac.nformcode="
					+ nformcode + " and   ac.nregtypecode=" + nregtypecode + " and  ac.nregsubtypecode="
					+ nregsubtypecode + "  and  ast.napprovalsubtypecode=" + napprovalsubtypecode
					+ " and ac.napprovalsubtypecode=" + napprovalsubtypecode
					+ " and ac.ntranscode=ts.ntranscode and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and asf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  ast.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and ac.nstatusconfigcode>0  and ac.nsitecode=" + userinfo.getNmastersitecode() + ""
					+ "  order by 1 desc";

		}

		query = " select ac.*,COALESCE(asf.jsondata->'sapprovalstatusfunctions'->>'en-US',asf.jsondata->'sapprovalstatusfunctions'->>'en-US') as"
				+ " sapprovalstatusfunctions,coalesce(qf.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
				+ "',qf.jsondata->'sdisplayname'->>'en-US') as sdisplayname,coalesce(ts.jsondata->'salertdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'salertdisplaystatus'->>'en-US') as sdisplaystatus  from registrationsubtype rst,approvalstatusfunctions asf,registrationtype rt,"
				+ " qualisforms qf,approvalstatusconfig ac,transactionstatus ts " + approvlsubtypeQuery + " where "
				+ addString + "";

		objmap.put("defaultSample", defaultsamples);
		objmap.put("realSampleType", defaultsamples);
		objmap.put("SampleTypes", sampleTypes);

		objmap.put("qualisForms", qualisforms);
		objmap.put("defaultForms", defaultform);
		objmap.put("realdefaultForms", defaultform);

		objmap.put("regSubTypeList", regSubTypeList);
		objmap.put("defaultRegSubType", defaultRegSubType);
		objmap.put("realRegSubType", defaultRegSubType);

		objmap.put("approvalSubType", approvalsubtype);
		objmap.put("defaultApprovalSubType", defaultsubTypeValue);
		objmap.put("realApprovalSubType", defaultsubTypeValue);

		objmap.put("registrationTypes", registrationTypes);
		objmap.put("defaultRegType", defaultregtype);
		objmap.put("realRegType", defaultregtype);

		final List<ApprovalStatusConfig> approvalstatusList = jdbcTemplate.query(query, new ApprovalStatusConfig());

		objmap.put("approvalstatsusconfig", approvalstatusList);
		return new ResponseEntity<>(objmap, HttpStatus.OK);

	}

	private String getApprovalSubType(final UserInfo userInfo) {
		return "select ntemplatecode, napprovalsubtypecode, coalesce(jsondata->'approvalsubtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'approvalsubtypename'->>'en-US') as ssubtypename, nisregsubtypeconfigneed"
				+ " from approvalsubtype where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNmastersitecode() + " order by ntemplatecode asc;";
	}

	@Override
	public ResponseEntity<Object> getTransactionStatus(final UserInfo userinfo) {
		final String query = "select ts.*,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as label,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as value , COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from transactionstatus ts"
				+ " where ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and  ts.ntranscode>0 ";
		return new ResponseEntity<>(jdbcTemplate.query(query, new ApprovalStatusConfig()), HttpStatus.OK);
	}

	private String getTransQualisForms(final int nregtypecode, final int nregsubtypecode, final UserInfo userInfo) {
		return " select qf.nformcode,coalesce(qf.jsondata->'sdisplayname'->>'en-US',qf.jsondata->'sdisplayname'->>'en-US') as sdisplayname "
				+ "	from qualisforms qf,approvalstatusconfig ac  where qf.nformcode=ac.nformcode and  "
				+ "	ac.nregtypecode=" + nregtypecode + " and ac.nregsubtypecode= " + nregsubtypecode + " and  "
				+ " qf.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by qf.nformcode";
	}

	private String getMasterQualisForms(final UserInfo userInfo) {
		return " select qf.nformcode,coalesce(qf.jsondata->'sdisplayname'->>'en-US',qf.jsondata->'sdisplayname'->>'en-US') as sdisplayname "
				+ "	from qualisforms qf,approvalstatusconfig ac  where qf.nformcode=ac.nformcode and "
				+ "	ac.nregtypecode=" + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " and ac.nregsubtypecode= " + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " and qf.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nsitecode="
				+ userInfo.getNmastersitecode() + " group by qf.nformcode";
	}

	private String getRegistrationType(final int nsampletypecode, final UserInfo userInfo) {
		return "select  rt.nregtypecode, coalesce(rt.jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "', rt.jsondata->'sregtypename'->>'en-US') as sregtypename "
				+ "	from registrationsubtype rs,registrationtype rt, regsubtypeconfigversion rsv,approvalconfig ac,transactionstatus ts,approvalconfigversion acv "
				+ "	where rt.nsampletypecode = " + nsampletypecode
				+ " and ac.nregsubtypecode = rs.nregsubtypecode and ac.napprovalconfigcode = rsv.napprovalconfigcode and acv.ntransactionstatus=ts.ntranscode "
				+ " and rsv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rt.nregtypecode=rs.nregtypecode and ts.ntranscode="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rsv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsv.nsitecode="
				+ userInfo.getNmastersitecode() + " and rs.nsitecode=" + userInfo.getNmastersitecode()
				+ " and rt.nregtypecode > 0  group by rt.nregtypecode ";
	}

	private String regSubtypeQuery(final int nregtypecode, final UserInfo userInfo) {
		return "select rs.nregsubtypecode,coalesce(rs.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "', rs.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,"
				+ " coalesce(rsv.jsondata->>'nneedsubsample','false')::boolean as nneedsubsample, "
				+ " coalesce(rsv.jsondata->>'nneedjoballocation','false')::boolean as nneedjoballocation, "
				+ " coalesce(rsv.jsondata->>'nneedmyjob','false')::boolean as nneedmyjob,"
				+ " coalesce(ts.jsondata->'salertdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'salertdisplaystatus'->>'en-US') as sdisplaystatus,"
				+ " coalesce(rsv.jsondata->>'nneedtestinitiate','false')::boolean as nneedtestinitiate, "
				+ " rsv.napprovalconfigcode, rsv.ntransactionstatus"
				+ " from registrationsubtype rs, regsubtypeconfigversion rsv,approvalconfig ac,transactionstatus ts"
				+ " where ac.nregtypecode = " + nregtypecode
				+ " and ac.nregsubtypecode = rs.nregsubtypecode and ac.napprovalconfigcode = rsv.napprovalconfigcode"
				+ " and rs.nregsubtypecode >0  and rsv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and ts.ntranscode="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rs.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rs.nsitecode="
				+ userInfo.getNmastersitecode();
	}

	@Override
	public ResponseEntity<Object> getTransactionForms(final int nregtypecode, final int nregsubtypecode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> objmap = new HashMap<>();

		final String forms = getTransQualisForms(nregtypecode, nregsubtypecode, userInfo);

		final List<QualisForms> qualisforms = jdbcTemplate.query(forms, new QualisForms());

		final Map<String, Object> defaultform = new HashMap<>();
		if (!qualisforms.isEmpty()) {
			defaultform.put("label", qualisforms.get(qualisforms.size() - 1).getSdisplayname());
			defaultform.put("value", qualisforms.get(qualisforms.size() - 1).getNformcode());
			defaultform.put("item", qualisforms.get(qualisforms.size() - 1));

			objmap.put("qualisForms", qualisforms);
			objmap.put("defaultForms", defaultform);
		} else {
			objmap.put("qualisForms", null);
			objmap.put("defaultForms", null);
		}

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegTypeBySampleType(final int nsampletypecode, final UserInfo userinfo)
			throws Exception {
		final Map<String, Object> objmap = new HashMap<>();
		List<RegistrationType> registrationTypes = null;
		List<QualisForms> qualisforms;
		List<RegistrationSubType> regSubTypeList = new ArrayList<>();
		List<ApprovalSubType> approvalSubTypeList = new ArrayList<>();
		Map<String, Object> defaultregtype = null;
		Map<String, Object> defaultApprovalSubType = null;
		Map<String, Object> defaultforms = null;
		Map<String, Object> defaultRegSubType = null;
		String approvalsubtype = "";
		String query = "";
		String query1 = "";
		if (nsampletypecode == Enumeration.SampleType.MASTERS.getType()) {
			final String getMasterForms = getMasterQualisForms(userinfo);
			qualisforms = jdbcTemplate.query(getMasterForms, new QualisForms());
			if (!qualisforms.isEmpty()) {
				defaultforms = new HashMap<>();
				defaultforms.put("label", qualisforms.get(qualisforms.size() - 1).getSdisplayname());
				defaultforms.put("value", qualisforms.get(qualisforms.size() - 1).getNformcode());
				defaultforms.put("item", qualisforms.get(qualisforms.size() - 1));

				objmap.put("qualisForms", qualisforms);
				objmap.put("defaultForms", defaultforms);
			} else {
				objmap.put("qualisForms", null);
				objmap.put("defaultForms", null);
			}
			approvalsubtype = getApprovalSubType(userinfo);

			approvalSubTypeList = jdbcTemplate.query(approvalsubtype, new ApprovalSubType());

			if (!approvalSubTypeList.isEmpty()) {
				defaultApprovalSubType = new HashMap<>();
				defaultApprovalSubType.put("label",
						approvalSubTypeList.get(approvalSubTypeList.size() - 1).getSsubtypename());
				defaultApprovalSubType.put("value",
						approvalSubTypeList.get(approvalSubTypeList.size() - 1).getNapprovalsubtypecode());
				defaultApprovalSubType.put("item", approvalSubTypeList.get(approvalSubTypeList.size() - 1));
			}
			objmap.put("defaultApprovalSubType", defaultApprovalSubType);
			objmap.put("approvalSubType", approvalSubTypeList);

		} else {
			query = getRegistrationType(nsampletypecode, userinfo);
			registrationTypes = jdbcTemplate.query(query, new RegistrationType());

			final int nregtypecode = !registrationTypes.isEmpty()
					? registrationTypes.get(registrationTypes.size() - 1).getNregtypecode()
					: -1;

			if (!registrationTypes.isEmpty()) {
				defaultregtype = new HashMap<>();
				defaultregtype.put("label", registrationTypes.get(registrationTypes.size() - 1).getSregtypename());
				defaultregtype.put("value", registrationTypes.get(registrationTypes.size() - 1).getNregtypecode());
				defaultregtype.put("item", registrationTypes.get(registrationTypes.size() - 1));

				objmap.put("registrationTypes", registrationTypes);
				objmap.put("defaultRegType", defaultregtype);

				query1 = regSubtypeQuery(nregtypecode, userinfo);
				regSubTypeList = jdbcTemplate.query(query1, new RegistrationSubType());
				int nregsubtypecode = !regSubTypeList.isEmpty()
						? regSubTypeList.get(regSubTypeList.size() - 1).getNregsubtypecode()
						: -1;
				if (!regSubTypeList.isEmpty()) {
					defaultRegSubType = new HashMap<>();
					defaultRegSubType.put("label", regSubTypeList.get(regSubTypeList.size() - 1).getSregsubtypename());
					defaultRegSubType.put("value", regSubTypeList.get(regSubTypeList.size() - 1).getNregsubtypecode());
					defaultRegSubType.put("item", regSubTypeList.get(regSubTypeList.size() - 1));

					nregsubtypecode = regSubTypeList.get(regSubTypeList.size() - 1).getNregsubtypecode();

					String transqualis = "";
					transqualis = getTransQualisForms(nregtypecode, nregsubtypecode, userinfo);
					qualisforms = jdbcTemplate.query(transqualis, new QualisForms());
					final Map<String, Object> defaultTransForm = new HashMap<>();
					if (!qualisforms.isEmpty()) {
						defaultTransForm.put("value", qualisforms.get(qualisforms.size() - 1).getNformcode());
						defaultTransForm.put("label", qualisforms.get(qualisforms.size() - 1).getSdisplayname());
						defaultTransForm.put("item", qualisforms.get(qualisforms.size() - 1));

						objmap.put("defaultForms", defaultTransForm);
						objmap.put("qualisForms", qualisforms);
					} else {
						objmap.put("defaultForms", null);
						objmap.put("qualisForms", null);
					}
				}
			}

			if (!registrationTypes.isEmpty()) {
				objmap.put("registrationTypes", registrationTypes);
				objmap.put("defaultRegType", defaultregtype);
			} else {

				objmap.put("defaultForms", null);
				objmap.put("qualisForms", null);

				objmap.put("registrationTypes", null);
				objmap.put("defaultRegType", null);
			}
			if (!regSubTypeList.isEmpty()) {

				objmap.put("regSubTypeList", regSubTypeList);
				objmap.put("defaultRegSubType", defaultRegSubType);
			} else {

				objmap.put("defaultForms", null);
				objmap.put("qualisForms", null);

				objmap.put("regSubTypeList", null);
				objmap.put("defaultRegSubType", null);
			}
		}

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getRegistrationSubType(final int nregtypecode, final int nsampletypecode,
			final UserInfo userinfo) throws Exception {
		final Map<String, Object> objmap2 = new HashMap<>();
		final String getRegistrationSubQry = regSubtypeQuery(nregtypecode, userinfo);

		final List<RegistrationSubType> regSubTypeList = jdbcTemplate.query(getRegistrationSubQry,
				new RegistrationSubType());

		final Map<String, Object> defaultRegSubType = new HashMap<>();
		if (!regSubTypeList.isEmpty()) {
			defaultRegSubType.put("key", regSubTypeList.get(regSubTypeList.size() - 1).getNregsubtypecode());
			defaultRegSubType.put("label", regSubTypeList.get(regSubTypeList.size() - 1).getSregsubtypename());
			defaultRegSubType.put("item", regSubTypeList.get(regSubTypeList.size() - 1));
			objmap2.put("regSubTypeList", regSubTypeList);
			objmap2.put("defaultRegSubType", defaultRegSubType);
			objmap2.put("realRegSubType", defaultRegSubType);
		} else {
			objmap2.put("regSubTypeList", null);
			objmap2.put("defaultRegSubType", null);
		}

		return new ResponseEntity<>(objmap2, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> deleteApprovalStatusConfig(final ApprovalStatusConfig objappprovalstatus,
			final int nsampletypecode, final UserInfo userinfo) throws Exception {

		final ApprovalStatusConfig isActiveStatus = getActiveApprovalStatusById(
				objappprovalstatus.getNstatusconfigcode(), userinfo);
		if (isActiveStatus == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userinfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final String delete = "update approvalstatusconfig set nstatus="
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nstatusconfigcode = "
				+ objappprovalstatus.getNstatusconfigcode();

		jdbcTemplate.execute(delete);
		objappprovalstatus.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
		final List<Object> deleteList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		deleteList.add(objappprovalstatus);
		multilingualIDList.add("IDS_DELETEAPPROVALSTATUSCONFIG");
		auditUtilityFunction.fnInsertAuditAction(deleteList, 1, null, multilingualIDList, userinfo);

		return getFilterSubmit(nsampletypecode, objappprovalstatus.getNapprovalsubtypecode(),
				objappprovalstatus.getNregtypecode(), objappprovalstatus.getNregsubtypecode(),
				objappprovalstatus.getNformcode(), userinfo);
	}

	@Override
	public ResponseEntity<Object> getRegSubTypeByRegtype(final int nregtypecode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> objmap = new HashMap<>();
		List<RegistrationSubType> regSubTypeList;
		List<QualisForms> qualisforms = null;
		Map<String, Object> defaultform = null;
		Map<String, Object> defaultRegSubType = null;
		String strQuery = "";
		String forms = "";

		strQuery = regSubtypeQuery(nregtypecode, userInfo);
		regSubTypeList = jdbcTemplate.query(strQuery, new RegistrationSubType());
		final int nregsubtypecode = !regSubTypeList.isEmpty()
				? regSubTypeList.get(regSubTypeList.size() - 1).getNregsubtypecode()
				: -1;
		if (!regSubTypeList.isEmpty()) {
			defaultRegSubType = new HashMap<>();
			defaultRegSubType.put("label", regSubTypeList.get(regSubTypeList.size() - 1).getSregsubtypename());
			defaultRegSubType.put("value", regSubTypeList.get(regSubTypeList.size() - 1).getNregsubtypecode());
			defaultRegSubType.put("item", regSubTypeList.get(regSubTypeList.size() - 1));

			forms = getTransQualisForms(nregtypecode, nregsubtypecode, userInfo);
			qualisforms = jdbcTemplate.query(forms, new QualisForms());

			defaultform = new HashMap<>();
			if (!qualisforms.isEmpty()) {
				defaultform.put("value", qualisforms.get(qualisforms.size() - 1).getNformcode());
				defaultform.put("item", qualisforms.get(qualisforms.size() - 1));
				defaultform.put("label", qualisforms.get(qualisforms.size() - 1).getSdisplayname());

				objmap.put("qualisForms", qualisforms);
				objmap.put("defaultForms", defaultform);
			} else {
				objmap.put("qualisForms", null);
				objmap.put("defaultForms", null);
			}

		}

		objmap.put("regSubTypeList", regSubTypeList);
		objmap.put("defaultRegSubType", defaultRegSubType);

		return new ResponseEntity<>(objmap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getFilterSubmit(final int nsampletypecode, final int napprovalsubtypecode,
			final int nregtypecode, final int nregsubtypecode, final int nformCode, final UserInfo userinfo)
			throws Exception {
		final Map<String, Object> objmap = new HashMap<>();
		String approvlsubtypeQuery = "";
		String query = "";
		String addString = "";
		if (nsampletypecode == Enumeration.SampleType.MASTERS.getType()
				&& nformCode == Enumeration.QualisForms.APPROVALCONFIGURATION.getqualisforms()) {
			approvlsubtypeQuery = ",approvalsubtype ast";
			addString = " asf.nstatusfunctioncode=ac.nstatusfunctioncode and qf.nformcode=ac.nformcode and "
					+ " ac.nregtypecode=rt.nregtypecode and ac.napprovalsubtypecode=ast.napprovalsubtypecode  and  rst.nregsubtypecode=ac.nregsubtypecode and  ac.nformcode="
					+ nformCode + " and  ac.nregtypecode=" + nregtypecode + " and ac.nregsubtypecode=" + nregsubtypecode
					+ " and ast.napprovalsubtypecode=ac.napprovalsubtypecode and ast.napprovalsubtypecode="
					+ napprovalsubtypecode + " and ac.napprovalsubtypecode=" + napprovalsubtypecode
					+ " and ac.ntranscode=ts.ntranscode and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and asf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ast.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ac.nstatusconfigcode>0  and ac.nsitecode=" + userinfo.getNmastersitecode() + ""
					+ " order by 1 desc";
		}

		else if (nsampletypecode == Enumeration.SampleType.MASTERS.getType()
				&& nformCode != Enumeration.QualisForms.APPROVALCONFIGURATION.getqualisforms()) {
			addString = "asf.nstatusfunctioncode=ac.nstatusfunctioncode and qf.nformcode=ac.nformcode and "
					+ "  ac.nregtypecode=rt.nregtypecode and  rst.nregsubtypecode=ac.nregsubtypecode and  ac.nformcode="
					+ nformCode + " and   ac.nregtypecode=" + nregtypecode + " and  ac.nregsubtypecode="
					+ nregsubtypecode + " and ac.ntranscode=ts.ntranscode and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and asf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "and qf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "	and ac.nstatusconfigcode>0  and ac.nsitecode=" + userinfo.getNmastersitecode()
					+ "	order by 1 desc";

		}

		else {
			addString = " asf.nstatusfunctioncode=ac.nstatusfunctioncode and qf.nformcode=ac.nformcode and "
					+ " ac.nregtypecode=rt.nregtypecode  and   rst.nregsubtypecode=ac.nregsubtypecode and  ac.nformcode="
					+ nformCode + "  and ac.nregtypecode=" + nregtypecode + " and  ac.nregsubtypecode="
					+ nregsubtypecode + " and  ac.ntranscode=ts.ntranscode and ac.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and asf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  qf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ac.nstatusconfigcode>0  and ac.nsitecode=" + userinfo.getNmastersitecode()
					+ " order by 1 desc";

		}

		query = " select ac.*,COALESCE(asf.jsondata->'sapprovalstatusfunctions'->>'" + userinfo.getSlanguagetypecode()
				+ "',asf.jsondata->'sapprovalstatusfunctions'->>'en-US') as"
				+ " sapprovalstatusfunctions,coalesce(qf.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
				+ "',qf.jsondata->'sdisplayname'->>'en-US') as sdisplayname,coalesce(ts.jsondata->'salertdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ "	ts.jsondata->'salertdisplaystatus'->>'en-US') as sdisplaystatus  from registrationsubtype rst,approvalstatusfunctions asf,registrationtype rt,"
				+ "	qualisforms qf,approvalstatusconfig ac,transactionstatus ts " + approvlsubtypeQuery + " where "
				+ addString + "";

		final List<ApprovalStatusConfig> approvalstatusList = jdbcTemplate.query(query, new ApprovalStatusConfig());

		objmap.put("approvalstatsusconfig", approvalstatusList);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getStatusFunction(final int nformcode, final UserInfo userinfo) {
		final String query = "select  asf.nstatusfunctioncode,COALESCE(asf.jsondata->'sapprovalstatusfunctions'->>'en-US',asf.jsondata->'sapprovalstatusfunctions'->>'en-US') as"
				+ " sapprovalstatusfunctions "
				+ " from approvalstatusfunctions asf,formwisestatusfunction fws ,qualisforms qf where fws.nformcode="
				+ nformcode + " and fws.nformcode=qf.nformcode and fws.nstatusfunctioncode = asf.nstatusfunctioncode"
				+ " and asf.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and asf.nsitecode= " + userinfo.getNmastersitecode() + " and fws.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by asf.nstatusfunctioncode,asf.jsondata;";
		return new ResponseEntity<>(jdbcTemplate.query(query, new ApprovalStatusConfig()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createApprovalStatusConfig(final List<ApprovalStatusConfig> inputMap,
			final UserInfo userinfo) throws Exception {
		final String sQuery = " lock  table approvalstatusconfig "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		List<ApprovalStatusConfig> listSave;
		final int nregsubtypecode = inputMap.get(0).getNregsubtypecode();
		final int nsampletypecode = inputMap.get(0).getNsampletypecode();
		final int nformcode = inputMap.get(0).getNformcode();
		final int nstatusfunctioncode = inputMap.get(0).getNstatusfunctioncode();
		final int nregtypecode = inputMap.get(0).getNregtypecode();
		final int napprovalsubtypecode = inputMap.get(0).getNapprovalsubtypecode();
		int ntranscode = -1;
		String query = "";
		final String transcodeList = inputMap.stream().map(transItem -> String.valueOf(transItem.getNtranscode()))
				.collect(Collectors.joining(","));

		query = "select * from approvalstatusconfig where nformcode= " + nformcode + " and ntranscode  in ("
				+ transcodeList + ") and  nstatusfunctioncode=" + nstatusfunctioncode + " and   nregsubtypecode = "
				+ nregsubtypecode + " and napprovalsubtypecode=" + napprovalsubtypecode + "  and  nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		listSave = jdbcTemplate.query(query, new ApprovalStatusConfig());

		if (listSave.size() == inputMap.size()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userinfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		final List<ApprovalStatusConfig> lstOldAvailableData = inputMap.stream()
				.filter(listAvalData -> listSave.stream()
						.anyMatch(lstTrans -> lstTrans.getNtranscode() == listAvalData.getNtranscode()))
				.collect(Collectors.toList());

		int i = 0;
		final String sGetSeqNoQuery = "select nsequenceno from seqnoconfigurationmaster where stablename = 'approvalstatusconfig' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		short nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Short.class);

		String addstring = "";
		String insertQuery = " insert into approvalstatusconfig (nstatusconfigcode,nstatusfunctioncode,nformcode,napprovalsubtypecode,nregtypecode,nregsubtypecode,ntranscode,nsorter,dmodifieddate,nsitecode,nstatus) values ";
		inputMap.removeAll(lstOldAvailableData);
		for (i = 0; i < inputMap.size(); i++) {

			String addstring1 = " ";
			nSeqNo++;
			ntranscode = inputMap.get(i).getNtranscode();

			if (i < inputMap.size() - 1) {
				addstring1 = ",";
			}
			addstring = "( " + nSeqNo + "," + nstatusfunctioncode + "," + nformcode + "," + napprovalsubtypecode + ","
					+ nregtypecode + "," + nregsubtypecode + "," + ntranscode + ", 1 ,'"
					+ dateUtilityFunction.getCurrentDateTime(userinfo) + "'," + userinfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;

			inputMap.get(i).setNstatusconfigcode(nSeqNo);

			insertQuery = insertQuery + addstring;
		}

		jdbcTemplate.execute(insertQuery);

		final String sUpdateSeqNoQuery = "update seqnoconfigurationmaster set nsequenceno =" + nSeqNo
				+ " where stablename = 'approvalstatusconfig';";

		jdbcTemplate.execute(sUpdateSeqNoQuery);

		auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(inputMap), 1, null,
				Arrays.asList("IDS_ADDAPPROVALSTATUSCONFIG"), userinfo);

		return getFilterSubmit(nsampletypecode, napprovalsubtypecode, nregtypecode, nregsubtypecode, nformcode,
				userinfo);
	}

	public ApprovalStatusConfig getActiveApprovalStatusById(final int nstatusconfigcode, final UserInfo userinfo)
			throws Exception {
		final String activeQuery = "select ac.nstatusconfigcode,ac.nstatusfunctioncode,ac.nformcode,ac.napprovalsubtypecode,ac.nregtypecode,ac.nregsubtypecode,ac.ntranscode,ac.nsitecode,ac.nstatus,"
				+ " COALESCE(ts.jsondata->'salertdisplaystatus'->>'" + userinfo.getSlanguagetypecode()
				+ "',ts.jsondata->'salertdisplaystatus'->>'en-US') as sdisplaystatus from approvalstatusconfig ac, transactionstatus ts "
				+ " where ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.ntranscode =ac.ntranscode and ac.nstatusconfigcode = " + nstatusconfigcode
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ac.nsitecode = " + userinfo.getNmastersitecode();
		return (ApprovalStatusConfig) jdbcUtilityFunction.queryForObject(activeQuery, ApprovalStatusConfig.class,
				jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> closeFilterService(final int nsampletypecode, final int nformcode,
			final int nregsubtypecode, final int nregtypecode, final int napprovalsubtypecode, final UserInfo userinfo)
			throws Exception {
		final Map<String, Object> objmap = new HashMap<>();
		List<QualisForms> qualisforms = null;
		List<RegistrationSubType> regSubTypeList = null;
		List<ApprovalSubType> approvalsubtype = null;
		List<RegistrationType> registrationTypes = null;
		String forms = "";
		if (nsampletypecode == 4) {
			forms = getMasterQualisForms(userinfo);
			qualisforms = jdbcTemplate.query(forms, new QualisForms());
			final String strQuery = getApprovalSubType(userinfo);
			approvalsubtype = jdbcTemplate.query(strQuery, new ApprovalSubType());
			objmap.put("qualisForms", qualisforms);
			objmap.put("approvalSubType", approvalsubtype);

			return new ResponseEntity<>(objmap, HttpStatus.OK);

		} else {
			final String getRegistrationQry = getRegistrationType(nsampletypecode, userinfo);
			registrationTypes = jdbcTemplate.query(getRegistrationQry, new RegistrationType());

			final String getRegistrationSubQry = regSubtypeQuery(nregtypecode, userinfo);
			regSubTypeList = jdbcTemplate.query(getRegistrationSubQry, new RegistrationSubType());
			forms = getTransQualisForms(nregtypecode, nregsubtypecode, userinfo);
			qualisforms = jdbcTemplate.query(forms, new QualisForms());
			objmap.put("qualisForms", qualisforms);
			objmap.put("regSubTypeList", regSubTypeList);
			objmap.put("approvalSubType", approvalsubtype);
			objmap.put("registrationTypes", registrationTypes);

			return new ResponseEntity<>(objmap, HttpStatus.OK);
		}
	}

}