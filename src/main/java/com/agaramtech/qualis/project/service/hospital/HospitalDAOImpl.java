package com.agaramtech.qualis.project.service.hospital;

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
import com.agaramtech.qualis.project.model.Hospital;

import lombok.AllArgsConstructor;

/*
* 
* @author Mullai Balaji.V BGSI-4 30/06/2025
* @version
*/
@AllArgsConstructor
@Repository
public class HospitalDAOImpl implements HospitalDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(HospitalDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available hospitals for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         hospitals
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getHospital(final UserInfo userInfo) throws Exception {

		final String strQuery = "select h.nhospitalcode, h.shospitalname,h.sdescription, h.dmodifieddate, h.nsitecode, h.nstatus from hospital h"
				+ " where nhospitalcode > 0" + " and h.nsitecode= " + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		LOGGER.info("getHospital() called " + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Hospital()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active hospital object based on the specified
	 * nhospitalcode.
	 * 
	 * @param nhospitalcode [int] primary key of hospital object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of hospital
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Hospital getActiveHospitalById(final int nhospitalcode, final UserInfo userInfo) throws Exception {

		final String strQuery = "select h.nhospitalcode, h.shospitalname, h.sdescription, h.dmodifieddate, h.nsitecode, h.nstatus from hospital h"
				+ " where h.nhospitalcode= " + nhospitalcode + " and h.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (Hospital) jdbcUtilityFunction.queryForObject(strQuery, Hospital.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to hospital table. Hospital Name is
	 * unique across the database. Need to check for duplicate entry of hospital
	 * name for the specified site before saving into database. *
	 * 
	 * @param objHospital [Hospital] object holding details to be added in hospital
	 *                    table
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return saved hospital object with status code 200 if saved successfully else
	 *         if the hospital already exists, response will be returned as 'Already
	 *         Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createHospital(final Hospital objHospital, final UserInfo userInfo) throws Exception {
		final Hospital hospitalByName = getHospitalByName(objHospital.getShospitalname(),
				userInfo.getNmastersitecode());

	//	final Hospital hospitalCode = getHospitalCode(objHospital.getShospitalcode(), userInfo.getNmastersitecode());
		//if (hospitalByName == null && hospitalCode == null) {
		if (hospitalByName == null) {
			final String sQuery = " lock table hospital " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final List<String> multilingualIdList = new ArrayList<>();
			final List<Object> saveHospitalList = new ArrayList<>();

			String sequenceNoQuery = " select nsequenceno from seqnoprojectmanagement where stablename ='hospital'";
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;
			final String insertQuery = " insert into hospital (nhospitalcode, shospitalname,sdescription, dmodifieddate, nsitecode, nstatus )"
					+ " values(" + nsequenceNo + ",N'"
					+ stringUtilityFunction.replaceQuote(objHospital.getShospitalname()) + "' ,N'"
					+ stringUtilityFunction.replaceQuote(objHospital.getSdescription()) + "' ,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";
			jdbcTemplate.execute(insertQuery);

			final String updateQuery = " update seqnoprojectmanagement set nsequenceno= " + nsequenceNo
					+ " where stablename='hospital'";
			jdbcTemplate.execute(updateQuery);

			objHospital.setNhospitalcode(nsequenceNo);
			saveHospitalList.add(objHospital);

			multilingualIdList.add("IDS_ADDHOSPITAL");

			auditUtilityFunction.fnInsertAuditAction(saveHospitalList, 1, null, multilingualIdList, userInfo);
			return getHospital(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	/**
	 * This method is used to fetch the hospital object for the specified hospital
	 * name and site.
	 * 
	 * @param shospitalname   [String] name of the hospital
	 * @param nmasterSiteCode [int] site code of the hospital
	 * @return hospital object based on the specified hospital name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Hospital getHospitalByName(final String shospitalname, int nmasterSiteCode) throws Exception {
		final String strQuery = "select nhospitalcode from hospital h where h.shospitalname= N'"
				+ stringUtilityFunction.replaceQuote(shospitalname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and h.nsitecode= " + nmasterSiteCode;
		return (Hospital) jdbcUtilityFunction.queryForObject(strQuery, Hospital.class, jdbcTemplate);
	}

	//private Hospital getHospitalCode(final String shospitalcode, int nmasterSiteCode) throws Exception {
		//final String strQuery = "select nhospitalcode from hospital h where h.shospitalcode= N'"
			//	+ stringUtilityFunction.replaceQuote(shospitalcode) + "' and nstatus = "
				//+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and h.nsitecode= " + nmasterSiteCode;
		//return (Hospital) jdbcUtilityFunction.queryForObject(strQuery, Hospital.class, jdbcTemplate);
	//}

	/**
	 * This method is used to update entry in hospital table. Need to validate that
	 * the hospital object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of hospital name for the
	 * specified site before saving into database.
	 * 
	 * @param objHospital [Hospital] object holding details to be updated in
	 *                    hospital table
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return saved hospital object with status code 200 if saved successfully else
	 *         if the hospital already exists, response will be returned as 'Already
	 *         Exists' with status code 409 else if the unit to be updated is not
	 *         available, response will be returned as 'Already Deleted' with status
	 *         code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateHospital(final Hospital objHospital, final UserInfo userInfo) throws Exception {

		final Hospital hospital = getActiveHospitalById(objHospital.getNhospitalcode(), userInfo);
		if (hospital == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();

			final String queryString = " select h.nhospitalcode from hospital h where h.shospitalname=N'"
					+ stringUtilityFunction.replaceQuote(objHospital.getShospitalname()) + "' and h.nhospitalcode<> "
					+ objHospital.getNhospitalcode() + " and nsitecode= " + objHospital.getNsitecode()
					+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			//final String qryString = "select h.nhospitalcode from hospital h where h.shospitalcode=N'"
				//	+ stringUtilityFunction.replaceQuote(objHospital.getShospitalcode()) + "' and h.nhospitalcode<> "
					//+ objHospital.getNhospitalcode() + " and nsitecode=" + objHospital.getNsitecode() + " and nstatus= "
					//+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final Hospital availableHospital = (Hospital) jdbcUtilityFunction.queryForObject(queryString,
					Hospital.class, jdbcTemplate);
			//final Hospital availableHospitalCode = (Hospital) jdbcUtilityFunction.queryForObject(qryString,
					//Hospital.class, jdbcTemplate);

			//if (availableHospital == null && availableHospitalCode == null)
			if (availableHospital == null)
			{
				final String updateQuery = " update hospital set shospitalname=N'"
						+ stringUtilityFunction.replaceQuote(objHospital.getShospitalname()) + "', sdescription=N'"
						+ stringUtilityFunction.replaceQuote(objHospital.getSdescription()) + "', dmodifieddate= '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nhospitalcode= "
						+ objHospital.getNhospitalcode();

				jdbcTemplate.execute(updateQuery);
				listAfterUpdate.add(objHospital);
				listBeforeUpdate.add(hospital);

				multilingualIDList.add("IDS_EDITHOSPITAL");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getHospital(userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in Hospital table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as 'hospital'
	 * 
	 * @param objHospital [Hospital] an Object holds the record to be deleted
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return a response entity with list of available hospital objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteHospital(final Hospital objHospital, final UserInfo userInfo) throws Exception {

		final Hospital hospital = getActiveHospitalById(objHospital.getNhospitalcode(), userInfo);
		if (hospital == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> deletedHospitalList = new ArrayList<>();

			final String query = " Select 'IDS_SITEHOSPITALMAPPING' as Msg from sitehospitalmapping where nhospitalcode= "
					+ objHospital.getNhospitalcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objHospital.getNhospitalcode()),
						userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQuery = " update hospital set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' , nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nhospitalcode="
						+ objHospital.getNhospitalcode();

				jdbcTemplate.execute(updateQuery);
				objHospital.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedHospitalList.add(objHospital);
				multilingualIDList.add("IDS_DELETEHOSPITAL");

				auditUtilityFunction.fnInsertAuditAction(deletedHospitalList, 1, null, multilingualIDList, userInfo);
				return getHospital(userInfo);

			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

}
