package com.agaramtech.qualis.barcode.service.sampledonor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.barcode.model.SampleDonor;
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
 * This class is used to perform CRUD Operation on "sampledonor" table by
 * implementing methods from its interface.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@AllArgsConstructor
@Repository
public class SampleDonorDAOImpl implements SampleDonorDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleDonorDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available sampledonor's for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and list of all active sampledonor's
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleDonor(final UserInfo userInfo) throws Exception {

		final String strQuery = "select s.nsampledonorcode,s.nprojecttypecode,s.ssampledonor,s.ncode,s.nsitecode,s.nstatus,p.sprojecttypename"
				+ " from sampledonor s,projecttype p" + " where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nsampledonorcode>0 and   s.nprojecttypecode= p.nprojecttypecode and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and s.nsitecode = "
				+ userInfo.getNmastersitecode() + " and p.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " order by s.nsampledonorcode desc";
		       LOGGER.info("getSampleDonor-->" + strQuery);

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SampleDonor()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to sampledonor table.
	 * Sample Donor Name is unique across the database. 
	 * Need to check for duplicate entry of sampleDonor name for the specified site before saving into database.
	 * @param objSampleDonor [SampleDonor] object holding details to be added in sampledonor table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved sampledonor object with status code 200 if saved successfully else if the sampledonor already exists, 
	 * 			response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSampleDonor(final SampleDonor objSampleDonor,final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSampleDonorList = new ArrayList<>();
		final List<SampleDonor> sampleDonorList = getSampleDonorName(objSampleDonor.getSsampledonor(),
				objSampleDonor.getNcode(), userInfo.getNmastersitecode(), objSampleDonor.getNsampledonorcode(),
				objSampleDonor.getNprojecttypecode());

		if (sampleDonorList.get(0).isIscode() == false && sampleDonorList.get(0).isIssampledonor() == false) {
			if (objSampleDonor.getNcode() > 0) {
				final String sQuery = " lock  table sampledonor "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(sQuery);

				String sequencenoquery = "select nsequenceno from seqnobarcode where stablename ='sampledonor'";

				int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
				nsequenceno++;

				final String insertquery = "Insert into sampledonor (nsampledonorcode ,nprojecttypecode,ssampledonor,ncode,dmodifieddate,nsitecode,nstatus) "
						+ "values(" + nsequenceno + "," + objSampleDonor.getNprojecttypecode() + ",N'"
						+ stringUtilityFunction.replaceQuote(objSampleDonor.getSsampledonor()) + "',"
						+ objSampleDonor.getNcode() + "," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "'," + " " + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);

				String updatequery = "update seqnobarcode set nsequenceno =" + nsequenceno
						+ " where stablename='sampledonor'";
				jdbcTemplate.execute(updatequery);

				objSampleDonor.setNsampledonorcode(nsequenceno);

				savedSampleDonorList.add(objSampleDonor);

				multilingualIDList.add("IDS_ADDSAMPLEDONOR");

				auditUtilityFunction.fnInsertAuditAction(savedSampleDonorList, 1, null, multilingualIDList, userInfo);
				return getSampleDonor(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			String alert = "";

			boolean sampledonor = sampleDonorList.get(0).isIssampledonor();
			boolean code = sampleDonorList.get(0).isIscode();

			if (sampledonor == true && code == true) {
				alert = commonFunction.getMultilingualMessage("IDS_SAMPLEDONOR", userInfo.getSlanguagefilename())
						+ " and " + commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
			} else if (sampledonor == true) {
				alert = commonFunction.getMultilingualMessage("IDS_SAMPLEDONOR", userInfo.getSlanguagefilename());
			} else {
				alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
			}
			return new ResponseEntity<>(alert + " "
					+ commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()).toLowerCase(),
					HttpStatus.CONFLICT);
		}

	}

	/**
	 * This method is used to retrieve active sampledonor object based on the specified nsampledonorcode.
	 * @param nsampledonorcode [int] primary key of sampledonor object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of sampledonor object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getActiveSampleDonorById(final int nsampledonorcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select s.nsampledonorcode,s.ssampledonor,s.ncode,s.dmodifieddate,s.nsitecode,s.nstatus,p.sprojecttypename,p.nprojecttypecode from sampledonor s,projecttype p where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nprojecttypecode= p.nprojecttypecode  and  s.nsampledonorcode = " + nsampledonorcode;

		final SampleDonor sampledonor = (SampleDonor) jdbcUtilityFunction.queryForObject(strQuery, SampleDonor.class,jdbcTemplate);

		if (sampledonor == null) {
			// status code: 417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// status code: 200
			return new ResponseEntity<>(sampledonor, HttpStatus.OK);
		}

	}

	/**
	 * This method is used to update entry in sampledonor table.
	 * Need to validate that the sampledonor object to be updated is active before updating 
	 * details in database.
	 * Need to check for duplicate entry of sampleDonor name for the specified site before saving into database.
	 * @param objSampleDonor [SampleDonor] object holding details to be updated in sampledonor table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved sampledonor object with status code 200 if saved successfully 
	 * 			else if the sampledonor already exists, response will be returned as 'Already Exists' with status code 409
	 *          else if the sampledonor to be updated is not available, response will be returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSampleDonor(final SampleDonor objSampleDonor,final UserInfo userInfo) throws Exception {
		final ResponseEntity<Object> sampledonorsResponse = getActiveSampleDonorById(
				objSampleDonor.getNsampledonorcode(), userInfo);

		if (sampledonorsResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return sampledonorsResponse;
		}

		else {
			final List<SampleDonor> sampledonorList = getSampleDonorName(objSampleDonor.getSsampledonor(),
					objSampleDonor.getNcode(), userInfo.getNmastersitecode(), objSampleDonor.getNsampledonorcode(),
					objSampleDonor.getNprojecttypecode());

			if (sampledonorList.get(0).isIscode() == false && sampledonorList.get(0).isIssampledonor() == false) {
				if (objSampleDonor.getNcode() > 0) {
					final String updateQueryString = "update sampledonor set ssampledonor='"
							+ stringUtilityFunction.replaceQuote(objSampleDonor.getSsampledonor())
							+ "',nprojecttypecode=" + objSampleDonor.getNprojecttypecode() + ", " + "ncode ="
							+ objSampleDonor.getNcode() + ",dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nsampledonorcode  ="
							+ objSampleDonor.getNsampledonorcode();
					jdbcTemplate.execute(updateQueryString);

					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add("IDS_EDITSAMPLEDONOR");

					final List<Object> listAfterSave = new ArrayList<>();
					listAfterSave.add(objSampleDonor);

					final List<Object> listBeforeSave = new ArrayList<>();
					listBeforeSave.add(sampledonorsResponse.getBody());

					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
					return getSampleDonor(userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				String alert = "";

				boolean isSampleDonor = sampledonorList.get(0).isIssampledonor();
				boolean isCode = sampledonorList.get(0).isIscode();
				if (isSampleDonor == true && isCode == true) {

					alert = commonFunction.getMultilingualMessage("IDS_SAMPLEDONOR", userInfo.getSlanguagefilename())
							+ " and "
							+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				}

				else if (isSampleDonor == true) {
					alert = commonFunction.getMultilingualMessage("IDS_SAMPLEDONOR", userInfo.getSlanguagefilename());
				} else {
					alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

		}
	}

	/**
	 * This method id used to delete an entry in sampledonor table
	 * Need to check the record is already deleted or not
	 * Need to check whether the record is used in aliquotplan table
	 * @param objSampleDonor [SampleDonor] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available sampledonor objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSampleDonor(SampleDonor objSampleDonor, UserInfo userInfo) throws Exception {

		final ResponseEntity<Object> sampledonorsResponse = getActiveSampleDonorById(
				objSampleDonor.getNsampledonorcode(), userInfo);

		if (sampledonorsResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return sampledonorsResponse;
		} else {
			// ALPD-5513 - added by Gowtham R on 10/3/25 - added validation to check this
			// record is exist in aliquotPlan or not
			final String query = "select 'IDS_ALIQUOTPLAN' as Msg from aliquotplan where nsampledonorcode = "
					+ objSampleDonor.getNsampledonorcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel  = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objSampleDonor.getNsampledonorcode()),
						userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<Object> deletedSampleDonorList = new ArrayList<>();
				final String updateQueryString = "update sampledonor set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nsampledonorcode="
						+ objSampleDonor.getNsampledonorcode();
				jdbcTemplate.execute(updateQueryString);

				objSampleDonor.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedSampleDonorList.add(objSampleDonor);
				auditUtilityFunction.fnInsertAuditAction(deletedSampleDonorList, 1, null,
						Arrays.asList("IDS_DELETESAMPLEDONOR"), userInfo);

				return getSampleDonor(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to fetch the sampledonor object for the specified sampleDonor name and site.
	 * @param ssampledonor [String] name of the sampleDonor
	 * @param nmasterSiteCode [int] site code of the sampledonor
	 * @return sampledonor object based on the specified sampleDonor name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private List<SampleDonor> getSampleDonorName(String ssampledonor, short code, int nmasterSiteCode,
			int nsampledonorcode, int nprojecttypecode) throws Exception {
		String ssampleDonorQuery = "";
		if (nsampledonorcode != 0) {
			ssampleDonorQuery = "and nsampledonorcode<>" + nsampledonorcode + "";
		}

		String strQuery = "SELECT" + " CASE WHEN EXISTS (SELECT ssampledonor  FROM sampledonor WHERE ssampledonor=N'"
				+ stringUtilityFunction.replaceQuote(ssampledonor) + "' and nprojecttypecode=" + nprojecttypecode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ nmasterSiteCode + " " + ssampleDonorQuery + ")" + " THEN true " + " ELSE false"
				+ " END AS issampledonor,CASE WHEN EXISTS (SELECT ncode  FROM sampledonor WHERE ncode=" + code
				+ " and nprojecttypecode=" + nprojecttypecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode=" + nmasterSiteCode
				+ " " + ssampleDonorQuery + ")" + " THEN true" + " ELSE false" + " END AS iscode";

		return (List<SampleDonor>) jdbcTemplate.query(strQuery, new SampleDonor());
	}

}
