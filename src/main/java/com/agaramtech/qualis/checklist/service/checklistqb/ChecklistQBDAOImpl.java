package com.agaramtech.qualis.checklist.service.checklistqb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.checklist.model.ChecklistComponent;
import com.agaramtech.qualis.checklist.model.ChecklistQB;
import com.agaramtech.qualis.checklist.service.checklistqbcategory.QBCategoryDAO;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD operation on 'checklistqb' table
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 08- 04 -2025
 */

@AllArgsConstructor
@Repository
public class ChecklistQBDAOImpl implements ChecklistQBDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistQBDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final QBCategoryDAO objQBCategoryDAO;

	@Override
	public ResponseEntity<Object> getChecklistQB(UserInfo userInfo) throws Exception {
		final String strQuery = " select qb.*,coalesce(cn.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "', cn.jsondata->'sdisplayname'->>'en-US') as scomponentname,qbc.schecklistqbcategoryname,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as smandatory"
				+ " from checklistqb as qb,checklistcomponent as cn,"
				+ " checklistqbcategory qbc,transactionstatus ts where qb.nchecklistcomponentcode=cn.nchecklistcomponentcode and qb.nchecklistqbcategorycode=qbc.nchecklistqbcategorycode and "
				+ " qb.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cn.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ts.ntranscode=qb.nmandatory and ts.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qbc.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qb.nsitecode= "
				+ userInfo.getNmastersitecode() + " and cn.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ "and qbc.nsitecode=" + userInfo.getNmastersitecode() + "; ";
		LOGGER.info("Get Query -->"+strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ChecklistQB()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createChecklistQB(ChecklistQB objChecklistQB, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table checklistqb " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final ChecklistQB ChecklistQBListByName = getChecklistQBByName(objChecklistQB.getSquestion(),
				objChecklistQB.getNchecklistqbcategorycode(), objChecklistQB.getNsitecode());
		if (ChecklistQBListByName == null) {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedChecklistQBList = new ArrayList<>();
			final String sequencenquery = "select nsequenceno from SeqNoChecklist where stablename ='checklistqb'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenquery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert into checklistqb (nchecklistqbcode,nchecklistqbcategorycode,nchecklistcomponentcode,squestion,squestiondata,nmandatory,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + "," + objChecklistQB.getNchecklistqbcategorycode() + ","
					+ objChecklistQB.getNchecklistcomponentcode() + ",N'"
					+ stringUtilityFunction.replaceQuote(objChecklistQB.getSquestion()) + "'," + " N'"
					+ stringUtilityFunction.replaceQuote(objChecklistQB.getSquestiondata()) + "',"
					+ objChecklistQB.getNmandatory() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")";
			jdbcTemplate.execute(insertquery);

			final String updatequery = "update SeqNoChecklist set nsequenceno =" + nsequenceno
					+ " where stablename ='checklistqb'";
			jdbcTemplate.execute(updatequery);
			objChecklistQB.setNchecklistqbcode(nsequenceno);
			savedChecklistQBList.add(objChecklistQB);
			multilingualIDList.add("IDS_ADDCHECKLISTQB");
			auditUtilityFunction.fnInsertAuditAction(savedChecklistQBList, 1, null, multilingualIDList, userInfo);
			return getChecklistQB(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private ChecklistQB getChecklistQBByName(final String squestion, final int ncategoryCode, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select nchecklistqbcode from checklistqb cq join checklistqbcategory cqc on cq.nchecklistqbcategorycode =cqc.nchecklistqbcategorycode "
				+ "where cq.nchecklistqbcategorycode=" + ncategoryCode + " and cq.squestion = N'"
				+ stringUtilityFunction.replaceQuote(squestion) + "' and cq.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cqc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cq.nsitecode=" + nmasterSiteCode
				+ " " + "and cqc.nsitecode=" + nmasterSiteCode;

		return (ChecklistQB) jdbcUtilityFunction.queryForObject(strQuery, ChecklistQB.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateChecklistQB(ChecklistQB objChecklistQB, UserInfo userInfo) throws Exception {

		final ChecklistQB checklistQB = (ChecklistQB) getActiveChecklistQBById(objChecklistQB.getNchecklistqbcode(),
				userInfo);
		if (checklistQB == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nchecklistqbcode from checklistqb where nchecklistqbcategorycode="
					+ objChecklistQB.getNchecklistqbcategorycode() + " and squestion = N'"
					+ stringUtilityFunction.replaceQuote(objChecklistQB.getSquestion()) + "' and nchecklistqbcode <> "
					+ objChecklistQB.getNchecklistqbcode() + " and nsitecode=" + userInfo.getNmastersitecode() + " "
					+ "and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<ChecklistQB> checklistQBList = (List<ChecklistQB>) jdbcTemplate.query(queryString,
					new ChecklistQB());
			if (checklistQBList.isEmpty()) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedChecklistQBList = new ArrayList<>();
				final List<Object> editedChecklistQBList = new ArrayList<>();
				final String updateQueryString = "update checklistqb set squestion=N'"
						+ stringUtilityFunction.replaceQuote(objChecklistQB.getSquestion()) + "', squestiondata =N'"
						+ stringUtilityFunction.replaceQuote(objChecklistQB.getSquestiondata())
						+ "', nchecklistqbcategorycode=" + objChecklistQB.getNchecklistqbcategorycode()
						+ ", nchecklistcomponentcode=" + objChecklistQB.getNchecklistcomponentcode() + ", nmandatory="
						+ objChecklistQB.getNmandatory() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nchecklistqbcode="
						+ objChecklistQB.getNchecklistqbcode();
				jdbcTemplate.execute(updateQueryString);
				savedChecklistQBList.add(objChecklistQB);
				editedChecklistQBList.add(checklistQB);
				multilingualIDList.add("IDS_EDITCHECKLISTQB");
				auditUtilityFunction.fnInsertAuditAction(savedChecklistQBList, 2, editedChecklistQBList,
						multilingualIDList, userInfo);
				return getChecklistQB(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteChecklistQB(ChecklistQB objChecklistQB, UserInfo userInfo) throws Exception {
		final ChecklistQB ChecklistQB = (ChecklistQB) getActiveChecklistQBById(objChecklistQB.getNchecklistqbcode(),
				userInfo);
		if (ChecklistQB == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_CHECKLIST' as Msg from checklistversionqb where nchecklistqbcode= "
					+ objChecklistQB.getNchecklistqbcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objChecklistQB.getNchecklistqbcode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedChecklistQBList = new ArrayList<>();
				final String updateQueryString = " update checklistqb set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nchecklistqbcode="
						+ objChecklistQB.getNchecklistqbcode();

				jdbcTemplate.execute(updateQueryString);
				objChecklistQB.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedChecklistQBList.add(objChecklistQB);
				multilingualIDList.add("IDS_DELETECHECKLISTQB");
				auditUtilityFunction.fnInsertAuditAction(deletedChecklistQBList, 1, null, multilingualIDList, userInfo);
				return getChecklistQB(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ChecklistQB getActiveChecklistQBById(int nchecklistQBCode, UserInfo userInfo) throws Exception {
		final String strQuery = "select qb.nchecklistqbcode,qb.nchecklistqbcategorycode,qb.nchecklistcomponentcode, qb.squestion, qb.squestiondata,qb.nmandatory,qb.nsitecode,qb.nstatus,coalesce(cn.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "', cn.jsondata->'sdisplayname'->>'en-US') as scomponentname,qbc.schecklistqbcategoryname,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as smandatory"
				+ " from checklistqb as qb,checklistcomponent as cn,"
				+ " checklistqbcategory qbc,transactionstatus ts where "
				+ "qb.nchecklistcomponentcode=cn.nchecklistcomponentcode "
				+ "and qb.nchecklistqbcategorycode=qbc.nchecklistqbcategorycode " + "and qb.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode=qb.nmandatory "
				+ " and qb.nsitecode =" + userInfo.getNmastersitecode() + " and cn.nsitecode="
				+ userInfo.getNmastersitecode() + " and qbc.nsitecode= " + userInfo.getNmastersitecode()
				+ " and ts.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cn.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qbc.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qb.nchecklistqbcode= "
				+ nchecklistQBCode;

		return (ChecklistQB) jdbcUtilityFunction.queryForObject(strQuery, ChecklistQB.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getAddEditData(UserInfo userInfo) throws Exception {
		final Map<String, Object> rtnMap = new HashMap<String, Object>();
		rtnMap.put("qbcategory", objQBCategoryDAO.getQBCategory(userInfo.getNmastersitecode()).getBody());
		rtnMap.put("checklistcomponent", getChecklistComponent(userInfo));
		return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);
	}

	private List<ChecklistComponent> getChecklistComponent(UserInfo userInfo) throws Exception {
		final String getQuery = "select nchecklistcomponentcode, coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', jsondata->'sdisplayname'->>'en-US') as scomponentname, nsitecode, nstatus from "
				+ "checklistcomponent where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " " + "and nsitecode=" + userInfo.getNmastersitecode() + " " + "and nchecklistcomponentcode>0";

		return (List<ChecklistComponent>) jdbcTemplate.query(getQuery, new ChecklistComponent());
	}

}
