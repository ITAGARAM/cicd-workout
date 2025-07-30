package com.agaramtech.qualis.checklist.service.checklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.checklist.model.Checklist;
import com.agaramtech.qualis.checklist.model.ChecklistQB;
import com.agaramtech.qualis.checklist.model.ChecklistVersion;
import com.agaramtech.qualis.checklist.model.ChecklistVersionNoGenerator;
import com.agaramtech.qualis.checklist.model.ChecklistVersionQB;
import com.agaramtech.qualis.checklist.model.ChecklistVersionTemplate;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@Repository
public class ChecklistDAOImpl implements ChecklistDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getChecklist(UserInfo userInfo) throws Exception {
		Map<String, Object> responseMap = new HashMap<String, Object>();

		final String strQuery = " select * from checklist where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nchecklistcode>0 and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<Checklist> lstChecklist = jdbcTemplate.query(strQuery, new Checklist());
		responseMap.put("checklist", lstChecklist);
		if (lstChecklist.size() > 0) {
			responseMap.put("selectedchecklist", lstChecklist.get(lstChecklist.size() - 1));
			int nchecklistCode = (lstChecklist.get(lstChecklist.size() - 1).getNchecklistcode());
			final String strQuery1 = "select vs.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from checklistversion vs,transactionstatus ts"
					+ " where vs.ntransactionstatus=ts.ntranscode " + "and vs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vs.nchecklistcode="
					+ nchecklistCode;
			LOGGER.info("Get Query --> "+strQuery1);
			final List<ChecklistVersion> lstVersion = jdbcTemplate.query(strQuery1,
					new ChecklistVersion());
			responseMap.put("checklistversion", lstVersion);
			if (lstVersion.size() > 0) {
				responseMap.put("selectedversion", lstVersion.get(0));
				final int nchecklistVersionCode = (lstVersion.get(0).getNchecklistversioncode());
				final String strQuery2 = " select vb.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "',"
						+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as smandatory,"
						+ " vb.nchecklistqbcode,cq.squestion,coalesce(cc.jsondata->'sdisplayname'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "', cc.jsondata->'sdisplayname'->>'en-US') as scomponentname,qc.schecklistqbcategoryname"
						+ " from checklistversionqb vb,checklistqb cq,checklistcomponent cc,transactionstatus ts,checklistqbcategory qc"
						+ " where vb.nchecklistqbcategorycode=qc.nchecklistqbcategorycode "
						+ " and vb.nmandatoryfield=ts.ntranscode and vb.nchecklistqbcode=cq.nchecklistqbcode "
						+ " and cq.nchecklistcomponentcode=cc.nchecklistcomponentcode" + " and vb.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cq.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vb.nchecklistversioncode="
						+ nchecklistVersionCode;
				LOGGER.info("Get Query --> "+strQuery2);
				List<ChecklistVersionQB> lstVersionQB = jdbcTemplate.query(strQuery2,
						new ChecklistVersionQB());
				responseMap.put("checklistversionqb", lstVersionQB);
			} else {
				responseMap.put("selectedversion", new HashMap<>());
			}
		} else {
			responseMap.put("selectedchecklist", new HashMap<>());
		}
		return new ResponseEntity<Object>(responseMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createChecklist(Checklist objChecklist, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table checklist " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final Checklist ChecklistListByName = getChecklistByName(objChecklist.getSchecklistname(),
				objChecklist.getNsitecode());
		if (ChecklistListByName == null) {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedChecklistList = new ArrayList<>();

			final String sequencequery = "select nsequenceno from SeqNoChecklist where stablename ='checklist'";
			int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class, jdbcTemplate);
			nsequenceno++;
			final String insertquery = "Insert into checklist(nchecklistcode,schecklistname,sdescription,dmodifieddate,nsitecode,nstatus) values("
					+ nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(objChecklist.getSchecklistname())
					+ "',N'" + stringUtilityFunction.replaceQuote(objChecklist.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);

			final String updatequery = "update SeqNoChecklist set  nsequenceno=" + nsequenceno
					+ " where stablename ='checklist'";
			jdbcTemplate.execute(updatequery);
			objChecklist.setNchecklistcode(nsequenceno);
			savedChecklistList.add(objChecklist);
			multilingualIDList.add("IDS_ADDCHECKLIST");
			auditUtilityFunction.fnInsertAuditAction(savedChecklistList, 1, null, multilingualIDList, userInfo);
			return getChecklist(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private Checklist getChecklistByName(String schecklistName, int nmasterSiteCode) throws Exception {
		String strQuery = "select nchecklistcode from checklist where schecklistname = N'"
				+ stringUtilityFunction.replaceQuote(schecklistName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode=" + nmasterSiteCode;
		return (Checklist) jdbcUtilityFunction.queryForObject(strQuery, Checklist.class, jdbcTemplate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateChecklist(Checklist objChecklist, UserInfo userInfo) throws Exception {
		final Checklist checklist = getActiveChecklistById(objChecklist.getNchecklistcode());
		final Map<String, Object> rtnMap = new HashMap<>();
		if (checklist == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedChecklistList = new ArrayList<>();
			final List<Object> editedChecklistList = new ArrayList<>();
			final String queryString = "select nchecklistcode from checklist where schecklistname = N'"
					+ stringUtilityFunction.replaceQuote(objChecklist.getSchecklistname()) + "'"
					+ " and nchecklistcode <> " + objChecklist.getNchecklistcode() + " "
					+ "and nsitecode="+userInfo.getNmastersitecode()+" "
					+ "and  nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			final Checklist ChecklistListByName = (Checklist) jdbcUtilityFunction.queryForObject(queryString,
					Checklist.class, jdbcTemplate);
			if (ChecklistListByName == null) {
				final String updateQueryString = " update checklist set schecklistname='"
						+ stringUtilityFunction.replaceQuote(objChecklist.getSchecklistname()) + "',"
						+ " sdescription ='" + stringUtilityFunction.replaceQuote(objChecklist.getSdescription()) + "'"
						+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nchecklistcode=" + objChecklist.getNchecklistcode();

				jdbcTemplate.execute(updateQueryString);
				savedChecklistList.add(objChecklist);
				editedChecklistList.add(checklist);
				multilingualIDList.add("IDS_EDITCHECKLIST");
				auditUtilityFunction.fnInsertAuditAction(savedChecklistList, 2, editedChecklistList, multilingualIDList,
						userInfo);
				rtnMap.put("selectedchecklist", getActiveChecklistById(objChecklist.getNchecklistcode()));
				rtnMap.putAll(
						(Map<String, Object>) getActiveChecklistVersionByChecklist(objChecklist.getNchecklistcode(),
								userInfo).getBody());
				return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteChecklist(Checklist objChecklist, UserInfo userInfo) throws Exception {
		final Checklist Checklist = getActiveChecklistById(objChecklist.getNchecklistcode());
		if (Checklist == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String validateChecklist = validateChecklist(objChecklist.getNchecklistcode());
			if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(validateChecklist)) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(validateChecklist, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

			final List<String> multilingualIDList = new ArrayList<>();

			String updateQueryString = " update checklist set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nchecklistcode="
					+ objChecklist.getNchecklistcode();
			jdbcTemplate.execute(updateQueryString);

			updateQueryString = " update checklistversion set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nchecklistcode="
					+ objChecklist.getNchecklistcode();
			jdbcTemplate.execute(updateQueryString);

			objChecklist.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

			String getQuery = " select * from checklist where nchecklistcode=" + objChecklist.getNchecklistcode() + ";";
			List<Checklist> lstChecklist = jdbcTemplate.query(getQuery, new Checklist());

			getQuery = " select * from checklistversion where nchecklistcode=" + objChecklist.getNchecklistcode() + ";";
			List<ChecklistVersion> lstChecklistVersion = jdbcTemplate.query(getQuery, new ChecklistVersion());

			getQuery = " select vq.* from checklistversionqb vq,checklistversion cv where cv.nchecklistversioncode=vq.nchecklistversioncode and cv.nchecklistcode="
					+ objChecklist.getNchecklistcode() + ";";
			List<ChecklistVersionQB> lstChecklistVersionQB = jdbcTemplate.query(getQuery, new ChecklistVersionQB());

			List<Object> deletedChecklistList = new ArrayList<Object>();
			deletedChecklistList.add(lstChecklist);
			deletedChecklistList.add(lstChecklistVersion);
			deletedChecklistList.add(lstChecklistVersionQB);

			multilingualIDList.add("IDS_DELETECHECKLIST");
			multilingualIDList.add("IDS_DELETECHECKLISTVERSION");
			multilingualIDList.add("IDS_DELETECHECKLISTVERSIONQB");
			auditUtilityFunction.fnInsertListAuditAction(deletedChecklistList, 1, null, multilingualIDList, userInfo);
			return getChecklist(userInfo);
		}
	}

	private String validateChecklist(int nchecklistcode) throws Exception {
		String str = "";

		String str1 = "select count(nchecklistversioncode) from checklistversion where nchecklistcode=" + nchecklistcode
				+ " and ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		int approvedVersion = (int) jdbcUtilityFunction.queryForObject(str1, Integer.class, jdbcTemplate);
		if (approvedVersion == 0) {
			str = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		} else {
			str = Enumeration.ReturnStatus.NOTPOSSIBLETODELETEAPPROVE.getreturnstatus();
		}
		return str;
	}

	@Override
	public Checklist getActiveChecklistById(int nchecklistCode) throws Exception {
		final String strQuery = " select * from checklist where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nchecklistcode=" + nchecklistCode;
		return (Checklist) jdbcUtilityFunction.queryForObject(strQuery, Checklist.class, jdbcTemplate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getActiveChecklistVersionByChecklist(int nchecklistCode, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> rtnMap = new LinkedHashMap<String, Object>();
		Checklist validateChecklist = getActiveChecklistById(nchecklistCode);
		if (validateChecklist != null) {
			final String strQuery1 = "select vs.nchecklistversioncode,vs.nchecklistcode,vs.schecklistversionname,vs.schecklistversiondesc,vs.ntransactionstatus,vs.nstatus,"
					+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus"
					+ " from checklistversion vs,transactionstatus ts "
					+ "where vs.ntransactionstatus=ts.ntranscode "
					+ "and vs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and ts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and vs.nchecklistcode="+ nchecklistCode;// +" order by vs.ntransactionstatus desc"
			List<ChecklistVersion> lstChecklistVersion = jdbcTemplate.query(strQuery1,
					new ChecklistVersion());
			rtnMap.put("checklistversion", lstChecklistVersion);
			rtnMap.put("selectedchecklist", getActiveChecklistById(nchecklistCode));
			if (lstChecklistVersion.size() > 0) {
				rtnMap.put("selectedversion", lstChecklistVersion.get(0));
				int versionCode = lstChecklistVersion.get(0).getNchecklistversioncode();
				rtnMap.putAll((Map<String, Object>) getChecklistVersionQB(versionCode, userInfo).getBody());
			}
			return new ResponseEntity<>(rtnMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getChecklistVersionAfterEdit(int nchecklistCode, int checklistVersionCode,
			UserInfo userinfo) throws Exception {
		final Map<String, Object> rtnMap = new LinkedHashMap<String, Object>();
		final String strQuery1 = "select vs.nchecklistversioncode,vs.nchecklistcode,vs.schecklistversionname,vs.schecklistversiondesc,vs.ntransactionstatus,vs.nstatus,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from checklistversion vs,transactionstatus ts where vs.ntransactionstatus=ts.ntranscode and vs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vs.nchecklistcode="
				+ nchecklistCode;// +" order by vs.ntransactionstatus desc"
		List<ChecklistVersion> lstChecklistVersion = jdbcTemplate.query(strQuery1,
				new ChecklistVersion());
		rtnMap.put("checklistversion", lstChecklistVersion);

		if (lstChecklistVersion.size() > 0) {
			rtnMap.put("selectedversion", getActiveChecklistVersionById(checklistVersionCode, userinfo));
			rtnMap.putAll((Map<String, Object>) getChecklistVersionQB(checklistVersionCode, userinfo).getBody());
		}
		return new ResponseEntity<>(rtnMap, HttpStatus.OK);
	}

	@Override
	public ChecklistVersion getActiveChecklistVersionById(int nchecklistVersionCode, UserInfo userInfo)
			throws Exception {
		final String strQuery1 = " select vs.nchecklistversioncode,vs.nchecklistcode,vs.schecklistversionname,vs.ntransactionstatus,vs.nstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from checklistversion vs,transactionstatus ts where "
				+ " vs.ntransactionstatus=ts.ntranscode and vs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vs.nchecklistversioncode="
				+ nchecklistVersionCode + "";
		List<ChecklistVersion> VersionList = jdbcTemplate.query(strQuery1,
				new ChecklistVersion());
		ChecklistVersion objChecklistVersion = null;
		if (VersionList.size() > 0) {
			ObjectMapper objMapper = new ObjectMapper();
			objChecklistVersion = objMapper.convertValue(VersionList.get(0), ChecklistVersion.class);
		}
		return objChecklistVersion;
	}

	@Override
	public ResponseEntity<Object> createChecklistVersion(ChecklistVersion objChecklistVersion, UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table checklistversion "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedChecklistVersionList = new ArrayList<>();
		final Checklist checklist = getActiveChecklistById(objChecklistVersion.getNchecklistcode());
		if (checklist != null) {
			String strQuery = " select nchecklistversioncode from checklistversion"
					+ " where schecklistversionname = N'"
					+ stringUtilityFunction.replaceQuote(objChecklistVersion.getSchecklistversionname())
					+ "' and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nchecklistcode=" + objChecklistVersion.getNchecklistcode();
			List<ChecklistVersion> lstCheck = jdbcTemplate.query(strQuery,
					new ChecklistVersion());
			if (lstCheck.size() == 0) {
				objChecklistVersion.setSchecklistversiondesc("-");

				final String sequencenoquery = "select nsequenceno from SeqNoChecklist where stablename ='checklistversion'";
				int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencenoquery, Integer.class,
						jdbcTemplate);
				nsequenceno++;
				final String insertquery = " Insert into checklistversion(nchecklistversioncode,nchecklistcode,schecklistversionname,schecklistversiondesc,ntransactionstatus,dmodifieddate,nsitecode,nstatus)"
						+ " values(" + nsequenceno + "," + objChecklistVersion.getNchecklistcode() + ",N'"
						+ stringUtilityFunction.replaceQuote(objChecklistVersion.getSchecklistversionname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objChecklistVersion.getSchecklistversiondesc()) + "',"
						+ objChecklistVersion.getNtransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);

				final String updatequery = "update SeqNoChecklist set nsequenceno =" + nsequenceno
						+ " where stablename ='checklistversion'";
				jdbcTemplate.execute(updatequery);
				objChecklistVersion.setNchecklistversioncode(nsequenceno);
				savedChecklistVersionList.add(objChecklistVersion);
				multilingualIDList.add("IDS_ADDCHECKLISTVERSION");
				auditUtilityFunction.fnInsertAuditAction(savedChecklistVersionList, 1, null, multilingualIDList,
						userInfo);
				return getActiveChecklistVersionByChecklist(objChecklistVersion.getNchecklistcode(), userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> updateChecklistVersion(ChecklistVersion objChecklistVersion, UserInfo userInfo)
			throws Exception {
		final Checklist checklist = getActiveChecklistById(objChecklistVersion.getNchecklistcode());
		if (checklist != null) {
			final ChecklistVersion checklistVersion = getActiveChecklistVersionById(
					objChecklistVersion.getNchecklistversioncode(), userInfo);
			if (checklistVersion == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (checklistVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					String strQuery = "select nchecklistversioncode from checklistversion where schecklistversionname = N'"
							+ stringUtilityFunction.replaceQuote(objChecklistVersion.getSchecklistversionname())
							+ "' and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nchecklistcode=" + objChecklistVersion.getNchecklistcode()
							+ " and nchecklistversioncode!=" + objChecklistVersion.getNchecklistversioncode();
					List<ChecklistVersion> lstCheck = jdbcTemplate.query(strQuery,
							new ChecklistVersion());
					if (lstCheck.size() == 0) {
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> newChecklistVersionList = new ArrayList<>();
						final List<Object> oldChecklistVersionList = new ArrayList<>();
						final String updateQueryString = "update checklistversion set schecklistversionname = N'"
								+ stringUtilityFunction.replaceQuote(objChecklistVersion.getSchecklistversionname())
								+ "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' where nchecklistversioncode=" + objChecklistVersion.getNchecklistversioncode();
						jdbcTemplate.execute(updateQueryString);
						newChecklistVersionList.add(objChecklistVersion);
						oldChecklistVersionList.add(checklistVersion);
						multilingualIDList.add("IDS_EDITCHECKLISTVERSION");
						auditUtilityFunction.fnInsertAuditAction(newChecklistVersionList, 2, oldChecklistVersionList,
								multilingualIDList, userInfo);
						return getChecklistVersionAfterEdit(objChecklistVersion.getNchecklistcode(),
								objChecklistVersion.getNchecklistversioncode(), userInfo);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(),
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> deleteChecklistVersion(ChecklistVersion objChecklistVersion, UserInfo userInfo)
			throws Exception {
		final Checklist objChecklist = getActiveChecklistById(objChecklistVersion.getNchecklistcode());
		if (objChecklist == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		final ChecklistVersion checklistVersion = getActiveChecklistVersionById(
				objChecklistVersion.getNchecklistversioncode(), userInfo);
		if (checklistVersion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (checklistVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				final List<String> multilingualIDList = new ArrayList<>();

				final String updateQueryString = "update checklistversion set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nchecklistversioncode="
						+ objChecklistVersion.getNchecklistversioncode() + ";"
						+ " update checklistversionqb set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nchecklistversioncode="
						+ objChecklistVersion.getNchecklistversioncode() + ";";
				jdbcTemplate.execute(updateQueryString);

				String getQuery = " select * from checklistversion where nchecklistversioncode="
						+ objChecklistVersion.getNchecklistversioncode() + ";";
				final List<ChecklistVersion> lstChecklistVersion = jdbcTemplate.query(getQuery, new ChecklistVersion());
				getQuery = " select * from checklistversionqb where nchecklistversioncode="
						+ objChecklistVersion.getNchecklistversioncode() + ";";
				final List<ChecklistVersionQB> lstChecklistVersionQB = jdbcTemplate.query(getQuery, new ChecklistVersionQB());

				final List<Object> deletedChecklistVersionList = new ArrayList<Object>();
				deletedChecklistVersionList.add(lstChecklistVersion);
				deletedChecklistVersionList.add(lstChecklistVersionQB);
				multilingualIDList.add("IDS_DELETECHECKLISTVERSION");
				multilingualIDList.add("IDS_DELETECHECKLISTVERSIONQB");

				auditUtilityFunction.fnInsertListAuditAction(deletedChecklistVersionList, 1, null, multilingualIDList,
						userInfo);
				return getActiveChecklistVersionByChecklist(objChecklistVersion.getNchecklistcode(), userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> approveChecklistVersion(ChecklistVersion objChecklistVersion, UserInfo userInfo)
			throws Exception {
		final Checklist objChecklist = getActiveChecklistById(objChecklistVersion.getNchecklistcode());
		if (objChecklist == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		final ChecklistVersion checklistVersion = getActiveChecklistVersionById(
				objChecklistVersion.getNchecklistversioncode(), userInfo);
		if (checklistVersion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (checklistVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				final Map<String, Object> mapVersionQB = (Map<String, Object>) getChecklistVersionQB(
						objChecklistVersion.getNchecklistversioncode(), userInfo).getBody();
				final List<ChecklistVersionQB> avilableVersionQB = (List<ChecklistVersionQB>) mapVersionQB
						.get("checklistversionqb");
				if (avilableVersionQB != null && avilableVersionQB.size() > 0) {
					List<String> multilingualIDList = new ArrayList<>();
					List<Object> approvedChecklistVersionList = new ArrayList<>();
					List<Object> retiredChecklistVersionList = new ArrayList<>();
					String updateQueryString = "";

					final String strQuery = " select * from checklistversion where nchecklistcode="
							+ objChecklistVersion.getNchecklistcode() + " and ntransactionstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					final ChecklistVersion oldApprovedVersion = (ChecklistVersion) jdbcUtilityFunction
							.queryForObject(strQuery, ChecklistVersion.class, jdbcTemplate);
					if (oldApprovedVersion != null) {

						updateQueryString += " update checklistversion set ntransactionstatus = "
								+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nchecklistversioncode="
								+ oldApprovedVersion.getNchecklistversioncode() + ";";

						final ChecklistVersion retiredChecklistVersion =  SerializationUtils.clone(oldApprovedVersion);
						//new ChecklistVersion(oldApprovedVersion);
						retiredChecklistVersion.setNtransactionstatus(
								(short) Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
						approvedChecklistVersionList.add(retiredChecklistVersion);
						retiredChecklistVersionList.add(oldApprovedVersion);
						multilingualIDList.add("IDS_RETIRECHECKLISTVERSION");
						auditUtilityFunction.fnInsertAuditAction(approvedChecklistVersionList, 2,
								retiredChecklistVersionList, multilingualIDList, userInfo);
						approvedChecklistVersionList = new ArrayList<>();
						retiredChecklistVersionList = new ArrayList<>();
						multilingualIDList = new ArrayList<>();
					}
					String strverdes = "-";
					final  Object objversion = projectDAOSupport.fnGetVersion(userInfo.getNformcode(),
							objChecklistVersion.getNchecklistcode(), null, ChecklistVersionNoGenerator.class,
							userInfo.getNmastersitecode(), userInfo);
					if (objversion != null) {
						strverdes = BeanUtils.getProperty(objversion, "versiondes");
					}
					objChecklistVersion.setSchecklistversiondesc(strverdes);
					updateQueryString += " update checklistversion set schecklistversiondesc="
							+ objChecklistVersion.getSchecklistversiondesc() + " ,ntransactionstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nchecklistversioncode="
							+ objChecklistVersion.getNchecklistversioncode();

					jdbcTemplate.execute(updateQueryString);

					objChecklistVersion.setNtransactionstatus(
							(short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
					approvedChecklistVersionList.add(objChecklistVersion);
					retiredChecklistVersionList.add(checklistVersion);
					multilingualIDList.add("IDS_APPROVECHECKLISTVERSION");
					auditUtilityFunction.fnInsertAuditAction(approvedChecklistVersionList, 2,
							retiredChecklistVersionList, multilingualIDList, userInfo);
					return getChecklistVersionAfterEdit(objChecklistVersion.getNchecklistcode(),
							objChecklistVersion.getNchecklistversioncode(), userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDCHECKLISTQBTOVERSION",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getChecklistVersionQB(int nchecklistVersionCode, UserInfo userInfo) throws Exception {
		final Map<String, Object> rtnMap = new HashMap<String, Object>();
		final String StringQuery = " select vb.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as smandatory,qc.schecklistqbcategoryname,cq.squestion,cq.squestiondata,coalesce(cc.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',cc.jsondata->'sdisplayname'->>'en-US') as scomponentname"
				+ " from checklistversionqb as vb,checklistqb cq,checklistcomponent cc,transactionstatus ts,checklistqbcategory qc "
				+ " where vb.nchecklistqbcategorycode=qc.nchecklistqbcategorycode and vb.nchecklistqbcode=cq.nchecklistqbcode"
				+ " and cq.nchecklistcomponentcode=cc.nchecklistcomponentcode and vb.nmandatoryfield=ts.ntranscode and vb.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and" + " cq.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vb.nchecklistversioncode="
				+ nchecklistVersionCode + "";
		rtnMap.put("checklistversionqb",
				jdbcTemplate.query(StringQuery, new ChecklistVersionQB()));
		return new ResponseEntity<>(rtnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createChecklistVersionQB(List<ChecklistVersionQB> lstChecklistVersionQB,
			UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table checklistversionqb "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		if (lstChecklistVersionQB != null && lstChecklistVersionQB.size() > 0) {
			final ChecklistVersion checklistVersion = getActiveChecklistVersionById(
					lstChecklistVersionQB.get(0).getNchecklistversioncode(), userInfo);
			if (checklistVersion != null) {
				final Checklist checklist = getActiveChecklistById(checklistVersion.getNchecklistcode());
				if (checklist != null) {
					if (checklistVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
							.gettransactionstatus()) {
						final String strchecklistqbcode = lstChecklistVersionQB.stream()
								.map(versionqb -> (String.valueOf(versionqb.getNchecklistqbcode())))
								.collect(Collectors.joining(","));

						final String strQuery = "select nchecklistversionqbcode from checklistversionqb "
								+ " where nchecklistversioncode="
								+ lstChecklistVersionQB.get(0).getNchecklistversioncode() + " and nchecklistqbcode in ("
								+ strchecklistqbcode + ")" + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						final List<ChecklistVersionQB> lstvalidate = jdbcTemplate
								.query(strQuery, new ChecklistVersionQB());
						if (lstvalidate.size() == 0) {

							final String sequencequery = "select nsequenceno from SeqNoChecklist where stablename ='checklistversionqb'";
							int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);

							final String insertquery = "Insert into checklistversionqb (nchecklistversionqbcode,nchecklistversioncode,nchecklistqbcategorycode,nchecklistqbcode,nmandatoryfield,dmodifieddate,nsitecode,nstatus)"
									+ "select " + nsequenceno
									+ " +rank() over (order by nchecklistqbcode)  as nchecklistversionqbcode ,"
									+ lstChecklistVersionQB.get(0).getNchecklistversioncode()
									+ "  as nchecklistversioncode  "
									+ ",cq.nchecklistqbcategorycode,cq.nchecklistqbcode,cq.nmandatory,'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNmastersitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
									+ "from checklistversion cv ,checklistqb cq,checklist cl "
									+ "where cl.nchecklistcode=cv.nchecklistcode and  cq.nchecklistqbcode in ("
									+ strchecklistqbcode + ") group by nchecklistqbcode";
							jdbcTemplate.execute(insertquery);

							final String updatequery = "update SeqNoChecklist set nsequenceno =(" + nsequenceno + ") +( "
									+ lstChecklistVersionQB.size() + ")  where stablename='checklistversionqb'";
							jdbcTemplate.execute(updatequery);

							multilingualIDList.add("IDS_ADDCHECKLISTVERSIONQB");
							List<Object> insertedQB = new ArrayList<>();

							final String auditqry = "select " + nsequenceno
									+ " +rank() over (order by nchecklistqbcode)  as nchecklistversionqbcode ,"
									+ lstChecklistVersionQB.get(0).getNchecklistversioncode()
									+ "  as nchecklistversioncode  "
									+ ",cq.nchecklistqbcategorycode,cq.nchecklistqbcode,cq.nmandatory, cq.nmandatory as nmandatoryfield"
									+ " from checklistversion cv ,checklistqb cq,checklist cl "
									+ "where cl.nchecklistcode=cv.nchecklistcode and  cq.nchecklistqbcode in ("
									+ strchecklistqbcode + ") group by nchecklistqbcode";
							final List<ChecklistVersionQB> lstvalidate1 = jdbcTemplate
									.query(auditqry, new ChecklistVersionQB());
							insertedQB.add(lstvalidate1);
							auditUtilityFunction.fnInsertListAuditAction(insertedQB, 1, null, multilingualIDList,
									userInfo);
							return getChecklistVersionQB(lstChecklistVersionQB.get(0).getNchecklistversioncode(),
									userInfo);

						} else {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
									userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
						}

					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTALREADYDELETED",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTVERSIONALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTQUESTIONS", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateChecklistVersionQB(ChecklistVersionQB objChecklistVersionQB, UserInfo userInfo)
			throws Exception {
		final ChecklistVersion checklistVersion = getActiveChecklistVersionById(
				objChecklistVersionQB.getNchecklistversioncode(), userInfo);
		if (checklistVersion == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTVERSIONALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

		}
		final Checklist checklist = getActiveChecklistById(checklistVersion.getNchecklistcode());
		if (checklist == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
		if (checklistVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			final ChecklistVersionQB checklistVersionQB = getActiveChecklistVersionQBById(
					objChecklistVersionQB.getNchecklistversionqbcode());
			if (checklistVersionQB == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> updatedChecklistVersionQBList = new ArrayList<>();
				final List<Object> editedChecklistVersionQBList = new ArrayList<>();
				final String updateQueryString = " update checklistversionqb set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nmandatoryfield = "
						+ objChecklistVersionQB.getNmandatoryfield() + " where nchecklistversionqbcode="
						+ objChecklistVersionQB.getNchecklistversionqbcode();
				jdbcTemplate.execute(updateQueryString);
				updatedChecklistVersionQBList.add(objChecklistVersionQB);
				editedChecklistVersionQBList.add(checklistVersionQB);
				multilingualIDList.add("IDS_UPDATECHECKLISTVERSIONQB");
				auditUtilityFunction.fnInsertAuditAction(updatedChecklistVersionQBList, 2, editedChecklistVersionQBList,
						multilingualIDList, userInfo);
				return getChecklistVersionQB(objChecklistVersionQB.getNchecklistversioncode(), userInfo);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteChecklistVersionQB(ChecklistVersionQB objChecklistVersionQB, UserInfo userInfo)
			throws Exception {
		final ChecklistVersion checklistVersion = getActiveChecklistVersionById(
				objChecklistVersionQB.getNchecklistversioncode(), userInfo);
		if (checklistVersion == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTVERSIONALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
		final Checklist checklist = getActiveChecklistById(checklistVersion.getNchecklistcode());
		if (checklist == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
		if (checklistVersion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			final ChecklistVersionQB checklistVersionQB = getActiveChecklistVersionQBById(
					objChecklistVersionQB.getNchecklistversionqbcode());
			if (checklistVersionQB == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedChecklistVersionQBList = new ArrayList<>();
				final String updateQueryString = " update checklistversionqb set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nchecklistversionqbcode=" + objChecklistVersionQB.getNchecklistversionqbcode();
				jdbcTemplate.execute(updateQueryString);
				objChecklistVersionQB.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedChecklistVersionQBList.add(objChecklistVersionQB);
				multilingualIDList.add("IDS_DELETECHECKLISTVERSIONQB");
				auditUtilityFunction.fnInsertAuditAction(deletedChecklistVersionQBList, 1, null, multilingualIDList,
						userInfo);
				return getChecklistVersionQB(objChecklistVersionQB.getNchecklistversioncode(), userInfo);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ChecklistVersionQB getActiveChecklistVersionQBById(int nchecklistversionqbcode) throws Exception {
		final String querystring = "select cvq.*,cq.squestion,cqc.schecklistqbcategoryname from checklistversionqb cvq,checklistqb cq,checklistqbcategory cqc "
				+ " where cvq.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cq.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cqc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cvq.nchecklistqbcategorycode=cqc.nchecklistqbcategorycode"
				+ " and cq.nchecklistqbcode=cvq.nchecklistqbcode" + " and cvq.nchecklistversionqbcode="
				+ nchecklistversionqbcode;
		return (ChecklistVersionQB) jdbcUtilityFunction.queryForObject(querystring, ChecklistVersionQB.class,
				jdbcTemplate);
	}

	@Override
	public String validateCheclistVersion(int nchecklistVersionCode) throws Exception {
		String str = "";
		final String str1 = "select ntransactionstatus from checklistversion where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nchecklistversioncode="
				+ nchecklistVersionCode;
		final ChecklistVersion checklistversion = (ChecklistVersion) jdbcUtilityFunction.queryForObject(str1,
				ChecklistVersion.class, jdbcTemplate);
		if (checklistversion != null) {
			if (checklistversion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				str = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			} else {
				str = Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus();
			}
		} else {
			str = "IDS_CHECKLISTVERSIONALREADYDELETED";
		}
		return str;
	}

	@Override
	public List<ChecklistQB> getAvailableChecklistQB(int nchecklistQBCategoryCode, int nchecklistVersionCode)
			throws Exception {
		final String strQuery = " select cq.nchecklistqbcode,cq.squestion,cq.nmandatory " + " from checklistqb cq"
				+ " where cq.nchecklistqbcategorycode=" + nchecklistQBCategoryCode + " and cq.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and cq.nchecklistqbcode not in "
				+ " (select nchecklistqbcode from checklistversionqb where nchecklistversioncode="
				+ nchecklistVersionCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ")";
		return jdbcTemplate.query(strQuery, new ChecklistQB());
	}

	@Override
	public ResponseEntity<Object> viewTemplate(int nchecklistVersionCode, int flag, int ntransactionResultCode,
			UserInfo userInfo) throws Exception {
		final ChecklistVersion checklistVersion = getActiveChecklistVersionById(nchecklistVersionCode, userInfo);
		if (checklistVersion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final Checklist checklist = getActiveChecklistById(checklistVersion.getNchecklistcode());
		if (checklist == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		String strQuery = "";
		if (flag == Enumeration.TransactionStatus.CHECKLIST_TEMPLATE_VIEW.gettransactionstatus()
				|| flag == Enumeration.TransactionStatus.CHECKLIST_TEST_GROUP_TEMPLATE_VIEW.gettransactionstatus()) {
			strQuery = " select v.ntransactionstatus,v.schecklistversionname,cqb.nchecklistqbcode,cqb.nchecklistcomponentcode,v.nchecklistversioncode,vqb.nchecklistversionqbcode,c.nchecklistcode,cqb.nmandatory,"
					+ " cqb.squestion,  case when cqb.nchecklistcomponentcode=7 and length(vct.sdefaultvalue) >0 then to_char((vct.sdefaultvalue)::timestamp without time zone,'"
					+ userInfo.getSdatetimeformat() + "'::text) else vct.sdefaultvalue end as sdefaultvalue, "
					+ " vqb.nmandatoryfield,cqb.squestiondata,coalesce(cc.jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',cc.jsondata->'sdisplayname'->>'en-US') as scomponentname ,c.schecklistname from checklistqb cqb,"
					+ " checklistversionqb vqb left outer join checklistversiontemplate vct on vqb.nchecklistversionqbcode=vct.nchecklistversionqbcode,checklistversion v,checklist c,checklistcomponent cc "
					+ " where cqb.nchecklistqbcode=vqb.nchecklistqbcode and vqb.nchecklistversioncode=v.nchecklistversioncode and cc.nchecklistcomponentcode=cqb.nchecklistcomponentcode and "
					+ " v.nchecklistcode=c.nchecklistcode and cqb.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vqb.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " cc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and v.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and v.nchecklistversioncode ="
					+ nchecklistVersionCode
					// ALPD-3556
					+ " order by vqb.nchecklistversionqbcode";
		} else if (flag == Enumeration.TransactionStatus.CHECKLIST_RESULTENTRY_VIEW.gettransactionstatus()) {
		}
		final List<Map<String, Object>> lstTemplate = jdbcTemplate.queryForList(strQuery);
		if (lstTemplate != null && lstTemplate.size() > 0) {
			return new ResponseEntity<Object>(lstTemplate, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDCHECKLISTQBTOVERSION",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> createUpdateChecklistVersionTemplate(
			List<ChecklistVersionTemplate> lstChecklistVersionTemplate, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table checklistversiontemplate "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> addMultilingualIDList = new ArrayList<>();
		final List<String> editMultilingualIDList = new ArrayList<>();
		final List<Object> savedDefaultValueList = new ArrayList<>();
		final List<Object> newEditedDefaultValueList = new ArrayList<>();
		final List<Object> oldDefaultValueList = new ArrayList<>();

		final List<ChecklistVersionTemplate> addTemplateList = new ArrayList<>();
		final StringBuffer sb = new StringBuffer();
		for (ChecklistVersionTemplate objCheckListTemp : lstChecklistVersionTemplate) {
			ChecklistVersionTemplate objTemplate = new ChecklistVersionTemplate();
			String strQuery = " select * from checklistversiontemplate where nchecklistqbcode = "
					+ objCheckListTemp.getNchecklistqbcode() + " and nchecklistversioncode="
					+ objCheckListTemp.getNchecklistversioncode() + " and nchecklistversionqbcode="
					+ objCheckListTemp.getNchecklistversionqbcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			final List<ChecklistVersionTemplate> lstCheck = jdbcTemplate.query(strQuery,
					new ChecklistVersionTemplate());
			if (lstCheck.size() == 0) {

				final String sequencequery = "select nsequenceno from SeqNoChecklist  where stablename ='checklistversiontemplate'";
				int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
				nsequenceno++;
				final String insertquery = "Insert into checklistversiontemplate (nchecklistversiontempcode,nchecklistversioncode,nchecklistversionqbcode,nchecklistqbcode,sdefaultvalue,dmodifieddate,nsitecode,nstatus) "
						+ "values(" + nsequenceno + "," + objCheckListTemp.getNchecklistversioncode() + ","
						+ objCheckListTemp.getNchecklistversionqbcode() + "," + objCheckListTemp.getNchecklistqbcode()
						+ ",'" + stringUtilityFunction.replaceQuote(objCheckListTemp.getSdefaultvalue()) + "','"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);

				final String updatequery = "update SeqNoChecklist set nsequenceno= " + nsequenceno
						+ " where stablename ='checklistversiontemplate'";
				jdbcTemplate.execute(updatequery);

				addTemplateList.add(objCheckListTemp);
				addMultilingualIDList.add("IDS_ADDTEMPLATEDEFAULTVALUE");
				savedDefaultValueList.add(objCheckListTemp);
			} else {
				objTemplate = lstCheck.get(0);
				sb.append("update CheckListVersionTemplate set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', sdefaultvalue="
						+ (objCheckListTemp.getSdefaultvalue() == null ? null
								: ("N'" + stringUtilityFunction.replaceQuote(objCheckListTemp.getSdefaultvalue())
								+ "'"))
						+ " where nchecklistqbcode=" + objTemplate.getNchecklistqbcode()
						+ " and nchecklistversionqbcode=" + objCheckListTemp.getNchecklistversionqbcode()
						+ " and nchecklistversioncode=" + objTemplate.getNchecklistversioncode() + "; ");
				editMultilingualIDList.add("IDS_EDITTEMPLATEDEFAULTVALUE");
				newEditedDefaultValueList.add(objCheckListTemp);
				oldDefaultValueList.add(objTemplate);
			}
		}
		if (addTemplateList.size() > 0) {
			// insertBatch(addTemplateList,SeqNoChecklist.class,"nsequenceno");
			auditUtilityFunction.fnInsertAuditAction(savedDefaultValueList, 1, null, addMultilingualIDList, userInfo);
		}
		if (sb.toString().length() > 0) {
			jdbcTemplate.execute(sb.toString());
			auditUtilityFunction.fnInsertAuditAction(newEditedDefaultValueList, 2, oldDefaultValueList,
					editMultilingualIDList, userInfo);
		}
		return new ResponseEntity<Object>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getApprovedChecklist(int nmasterSiteCode) throws Exception {
		final String sQuery = "select cl.nchecklistcode,v.nchecklistversioncode,cl.schecklistname,v.schecklistversionname "
				+ " from checklist cl,checklistversion v where cl.nchecklistcode=v.nchecklistcode and cl.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and v.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and v.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and cl.nsitecode = "
				+ nmasterSiteCode + " and v.nchecklistversioncode > 0;";
		return new ResponseEntity<Object>(jdbcTemplate.query(sQuery, new ChecklistVersion()), HttpStatus.OK);
	}

}
