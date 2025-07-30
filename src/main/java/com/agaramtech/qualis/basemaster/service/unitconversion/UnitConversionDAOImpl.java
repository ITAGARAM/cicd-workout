package com.agaramtech.qualis.basemaster.service.unitconversion;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.basemaster.model.UnitConversion;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.testmanagement.model.Operators;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "unitconversion" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class UnitConversionDAOImpl implements UnitConversionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitConversionDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available unitconversions for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         unitconversions
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUnitConversion(UserInfo userInfo) throws Exception {
		String strQuery = "select uc.dmodifieddate, uc.nconversionfactor, uc.ndestinationunitcode, uc.noperatorcode, uc.nsitecode, uc.nsourceunitcode, uc.nstatus, uc.nunitconversioncode, "
				+ "to_char(uc.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate,"
				+ " u.sunitname as sunitname,u1.sunitname as sunitname1, o.soperator as soperator from unitconversion uc "
				+ "join unit u on u.nunitcode = uc.nsourceunitcode "
				+ "join unit u1 on u1.nunitcode = uc.ndestinationunitcode "
				+ "join operators o on o.noperatorcode = uc.noperatorcode " + "where uc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and u1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and o.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and uc.nsitecode = "
				+ userInfo.getNmastersitecode() + " and u.nsitecode = " + userInfo.getNmastersitecode()
				+ " and u1.nsitecode = " + userInfo.getNmastersitecode() + " and o.nsitecode = "
				+ userInfo.getNmastersitecode() + " and uc.nunitconversioncode > 0 ";
		LOGGER.info("getUnitConversion : " + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new UnitConversion()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all available units for the specified
	 * site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         units
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUnit(UserInfo userInfo) throws Exception {
		String strQuery = "select dmodifieddate, ndefaultstatus, nsitecode, nstatus, nunitcode, sdescription, sunitsynonym, "
				+ "to_char(dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate,"
				+ " sunitname as sunitname1, sunitname,nunitcode as nsourceunitcode,nunitcode as ndestinationunitcode from unit where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nsitecode = "
				+ userInfo.getNmastersitecode() + " and nunitcode > 0 ";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Unit()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active unitconversion object based on the
	 * specified nunitCoversionCode and nmastersitecode.
	 * 
	 * @param nunitCoversionCode [int] primary key of unitCoversion object
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         unitCoversion object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public UnitConversion getActiveUnitConversionById(int nunitConversionCode, UserInfo userInfo) throws Exception {
		final String strQuery = "select uc.dmodifieddate, uc.nconversionfactor, uc.ndestinationunitcode, uc.noperatorcode, uc.nsitecode, uc.nsourceunitcode, uc.nstatus, uc.nunitconversioncode,"
				+ "to_char(uc.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate,"
				+ "u.sunitname as sunitname,u1.sunitname as sunitname1, o.soperator as soperator from unitconversion uc "
				+ "join unit u on u.nunitcode = uc.nsourceunitcode "
				+ "join unit u1 on u1.nunitcode = uc.ndestinationunitcode "
				+ "join operators o on o.noperatorcode = uc.noperatorcode " + "where uc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and o.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uc.nunitconversioncode = "
				+ nunitConversionCode + "";
		return (UnitConversion) jdbcUtilityFunction.queryForObject(strQuery, UnitConversion.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to unitconversion table. Combination
	 * of nsourceunitcode and ndestinationunitcode is unique across the database.
	 * Need to check for duplicate entry of nsourceunitcode and ndestinationunitcode
	 * for the specified site before saving into database.
	 * 
	 * @param objUnitConversion [UnitConversion] object holding details to be added
	 *                          in unitconversion table
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return saved unitconversion object with status code 200 if saved
	 *         successfully else if the unitconversion already exists, response will
	 *         be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table unitconversion " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedUnitList = new ArrayList<>();

		final List<UnitConversion> unitConversionList = getUnitConversionListByName(
				objUnitConversion.getNsourceunitcode(), objUnitConversion.getNdestinationunitcode(),
				objUnitConversion.getNsitecode());
		if (unitConversionList.isEmpty()) {

			int sourceUnitCode = objUnitConversion.getNsourceunitcode();
			int destinationUnitCode = objUnitConversion.getNdestinationunitcode();

			if (sourceUnitCode != destinationUnitCode) {
				final String sequencenoquery = "select nsequenceno from seqnobasemaster where stablename = 'unitconversion' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int nsequenceno = (Integer) jdbcUtilityFunction.queryForObject(sequencenoquery, Integer.class,
						jdbcTemplate);
				nsequenceno++;

				final String insertquery = " Insert into unitconversion (nunitconversioncode,nsourceunitcode,ndestinationunitcode,noperatorcode,nconversionfactor,dmodifieddate,nsitecode,nstatus) "
						+ " values(" + nsequenceno + "," + objUnitConversion.getNsourceunitcode() + ","
						+ objUnitConversion.getNdestinationunitcode() + "," + objUnitConversion.getNoperatorcode() + ","
						+ objUnitConversion.getNconversionfactor() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);

				final String updatequery = "update seqnobasemaster set nsequenceno = " + nsequenceno
						+ " where stablename = 'unitconversion' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(updatequery);

				objUnitConversion.setNunitconversioncode(nsequenceno);
				savedUnitList.add(objUnitConversion);

				multilingualIDList.add("IDS_ADDUNITCONVERSION");
				auditUtilityFunction.fnInsertAuditAction(savedUnitList, 1, null, multilingualIDList, userInfo);
				return getUnitConversion(userInfo);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FROMUNITTOUNITNOTSAME",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}

	}

	/**
	 * This method is used to update entry in unitconversion table. Need to validate
	 * that the unitconversion object to be updated is active before updating
	 * details in database. Need to check for duplicate entry of Combination of
	 * nsourceunitcode and ndestinationunitcode for the specified site before saving
	 * into database.
	 * 
	 * @param objUnitConversion [UnitConversion] object holding details to be
	 *                          updated in unitconversion table
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return saved unitconversion object with status code 200 if saved
	 *         successfully else if the unitconversion already exists, response will
	 *         be returned as 'Already Exists' with status code 409 else if the
	 *         unitconversion to be updated is not available, response will be
	 *         returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception {

		final UnitConversion unitconversion = getActiveUnitConversionById(objUnitConversion.getNunitconversioncode(),
				userInfo);
		final List<String> multilingualIDList = new ArrayList<>();

		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		if (unitconversion == null) {
			// status code:205
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String strQuery = " select nunitconversioncode from unitconversion where nsourceunitcode = "
					+ objUnitConversion.getNsourceunitcode() + " " + " and ndestinationunitcode = "
					+ objUnitConversion.getNdestinationunitcode() + " and nunitconversioncode <> "
					+ objUnitConversion.getNunitconversioncode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();

			final List<UnitConversion> unitConversionList = jdbcTemplate.query(strQuery, new UnitConversion());
			if (unitConversionList.isEmpty()) {

				final int sourceUnitCode = objUnitConversion.getNsourceunitcode();
				final int destinationUnitCode = objUnitConversion.getNdestinationunitcode();

				if (sourceUnitCode != destinationUnitCode) {
					final String updateQueryString = "update unitconversion set nsourceunitcode="
							+ objUnitConversion.getNsourceunitcode() + ", ndestinationunitcode ="
							+ objUnitConversion.getNdestinationunitcode() + ", noperatorcode = "
							+ objUnitConversion.getNoperatorcode() + ", nconversionfactor="
							+ objUnitConversion.getNconversionfactor() + ",dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nunitconversioncode = "
							+ objUnitConversion.getNunitconversioncode() + ";";

					jdbcTemplate.execute(updateQueryString);

					listBeforeUpdate.add(unitconversion);
					listAfterUpdate.add(objUnitConversion);
					multilingualIDList.add("IDS_EDITUNITCONVERSION");
					auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
							userInfo);
					return getUnitConversion(userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FROMUNITTOUNITNOTSAME",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

				}

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

		}
	}

	/**
	 * This method id used to delete an entry in UnitConversion table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as 'testparameter','materialaccountingunit'
	 * 
	 * @param objUnitConversion [UnitConversion] an Object holds the record to be
	 *                          deleted
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return a response entity with list of available unitconversion objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteUnitConversion(UnitConversion objUnitConversion, UserInfo userInfo)
			throws Exception {
		final UnitConversion unitconversion = getActiveUnitConversionById(objUnitConversion.getNunitconversioncode(),
				userInfo);
		if (unitconversion == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_TESTMASTER' as Msg from testparameter where nunitcode = "
					+ objUnitConversion.getNsourceunitcode() + " and ndestinationunitcode = "
					+ objUnitConversion.getNdestinationunitcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ " select 'IDS_MATERIALACCOUNTINGUNIT' as Msg from materialaccountingunit where nunitconversioncode = "
					+ objUnitConversion.getNunitconversioncode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objUnitConversion.getNunitconversioncode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedUnitList = new ArrayList<>();
				final String updateQueryString = "update unitconversion set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nunitconversioncode = "
						+ objUnitConversion.getNunitconversioncode();

				jdbcTemplate.execute(updateQueryString);
				objUnitConversion.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedUnitList.add(objUnitConversion);
				multilingualIDList.add("IDS_DELETEUNITCONVERSION");
				auditUtilityFunction.fnInsertAuditAction(deletedUnitList, 1, null, multilingualIDList, userInfo);
				return getUnitConversion(userInfo);
			} else {
				// status code:417
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to fetch the unit conversion object for the specified
	 * nsourceunitcode and ndestinationunitcode and site.
	 * 
	 * @param nsourceunitcode      [int] nsourceunitcode of the unitconversion
	 * @param ndestinationunitcode [int] ndestinationunitcode of the unitconversion
	 * @param nmasterSiteCode      [int] site code of the unitconversion
	 * @return unitconversion object based on the specified nsourceunitcode and
	 *         ndestinationunitcode and site
	 * @throws Exception that are thrown from this DAO layer
	 */

	private List<UnitConversion> getUnitConversionListByName(int nsourceunitcode, int ndestinationunitcode,
			int nmasterSiteCode) throws Exception {
		final String strQuery = " select nunitconversioncode from unitconversion where nsourceunitcode = "
				+ nsourceunitcode + " " + " and ndestinationunitcode = " + ndestinationunitcode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = " + nmasterSiteCode;
		return (List<UnitConversion>) jdbcTemplate.query(strQuery, new UnitConversion());
	}

	/**
	 * This method is used to fetch the conversion operator object for the specified
	 * nisunitconversion.
	 * 
	 * @param nmasterSiteCode [int] site code of the unitconversion
	 * @return Operators object based on the specified site
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getConversionOperator(UserInfo userInfo) throws Exception {
		String strQuery = "select noperatorcode, soperator as soperator from operators where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nsitecode = "
				+ userInfo.getNmastersitecode() + " and nisunitconversion = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and noperatorcode > 0 ";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Operators()), HttpStatus.OK);
	}

}
