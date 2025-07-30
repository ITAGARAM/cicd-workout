package com.agaramtech.qualis.organization.service.lab;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.organization.model.Lab;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "Lab" table by implementing
 * methods from its interface.
 */
@AllArgsConstructor
@Repository
public class LabDAOImpl implements LabDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LabDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available Labs for the specified
	 * site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         Labs
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getLab(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from lab l where l.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nlabcode > 0  and l.nsitecode = "
				+ nmasterSiteCode;
		LOGGER.info("Get Lab Query :-------->" + strQuery);
		return new ResponseEntity<>((List<Lab>) jdbcTemplate.query(strQuery, new Lab()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createLab(Lab lab, UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table lab " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedLabList = new ArrayList<>();
		final Lab labByName = getLabByName(lab.getSlabname(), lab.getNsitecode());
		if (labByName == null) {
			String sequencequery = "select nsequenceno from SeqNoOrganisation where stablename ='lab'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;
			String insertquery = "Insert into lab (nlabcode,slabname,sdescription,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(lab.getSlabname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(lab.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update SeqNoOrganisation set nsequenceno =" + nsequenceno
					+ " where stablename ='lab'";
			jdbcTemplate.execute(updatequery);
			lab.setNlabcode(nsequenceno);
			savedLabList.add(lab);
			multilingualIDList.add("IDS_ADDLAB");
			auditUtilityFunction.fnInsertAuditAction(savedLabList, 1, null, multilingualIDList, userInfo);
			return getLab(userInfo.getNmastersitecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the Lab object for the specified Lab name and
	 * site.
	 * 
	 * @param slabname        [String] name of the Lab
	 * @param nmasterSiteCode [int] site code of the Lab
	 * @return Lab object based on the specified Lab name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Lab getLabByName(final String labName, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nlabcode from lab where slabname = N'"
				+ stringUtilityFunction.replaceQuote(labName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (Lab) jdbcUtilityFunction.queryForObject(strQuery, Lab.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateLab(Lab lab, UserInfo userInfo) throws Exception {
		final Lab objLab = getActiveLabById(lab.getNlabcode());
		if (objLab == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nlabcode from lab where slabname = '"
					+ stringUtilityFunction.replaceQuote(lab.getSlabname()) + "' and nlabcode <> " + lab.getNlabcode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode =" + userInfo.getNmastersitecode();
			final List<Lab> labList = (List<Lab>) jdbcTemplate.query(queryString, new Lab());
			if (labList.isEmpty()) {
				final String updateQueryString = "update lab set slabname='"
						+ stringUtilityFunction.replaceQuote(lab.getSlabname()) + "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(lab.getSdescription()) + "',dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nlabcode=" + lab.getNlabcode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITLAB");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(lab);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(objLab);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getLab(userInfo.getNmastersitecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to retrieve active Lab object based on the specified
	 * nlabCode.
	 * 
	 * @param nlabCode [int] primary key of Lab object
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and data of Lab object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Lab getActiveLabById(final int nlabcode) throws Exception {
		final String strQuery = "select * from lab l where l.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and l.nlabcode = " + nlabcode;
		try {
			return (Lab) jdbcTemplate.queryForObject(strQuery, new Lab());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * This method id used to delete an entry in Lab table Need to check the record
	 * is already deleted or not Need to check whether the record is used in other
	 * tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objLab   [Lab] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return a response entity with list of available Lab objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteLab(Lab lab, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedLabList = new ArrayList<>();
		final Lab labByID = (Lab) getActiveLabById(lab.getNlabcode());
		if (labByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_ORGANISATION' as Msg from departmentlab where nlabcode= "
					+ labByID.getNlabcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = "update lab set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nlabcode="
						+ lab.getNlabcode();
				jdbcTemplate.execute(updateQueryString);
				lab.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedLabList.add(lab);
				multilingualIDList.add("IDS_DELETELAB");
				auditUtilityFunction.fnInsertAuditAction(savedLabList, 1, null, multilingualIDList, userInfo);
				return getLab(lab.getNsitecode());
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);

			}
		}
	}
}
