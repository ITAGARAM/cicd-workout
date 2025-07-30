package com.agaramtech.qualis.contactmaster.service.country;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.contactmaster.model.Country;
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
 * This class is used to perform CRUD Operation on "country" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class CountryDAOImpl implements CountryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CountryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available countries for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         countries for given site.
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCountry(final UserInfo userInfo) throws Exception {
		final String strQuery = "select c.ncountrycode,c.scountryname,c.scountryshortname,c.stwocharcountry,c.sthreecharcountry,c.nsitecode,c.nstatus,"
				+ "to_char(c.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate " + " from Country c where c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.ncountrycode > 0 and "
				+ " c.nsitecode = " + userInfo.getNmastersitecode();
		LOGGER.info("getCountry  : "+strQuery);
		return new ResponseEntity<>((List<Country>) jdbcTemplate.query(strQuery, new Country()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active country object based on the specified
	 * ncountryCode.
	 * 
	 * @param ncountryCode [int] primary key of country object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of country
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Country getActiveCountryById(int ncountryCode, UserInfo userInfo) throws Exception {
		final String strQuery = "select a.dmodifieddate,a.ncountrycode,a.nsitecode,a.nstatus,a.scountryname, a.scountryshortname, to_char(a.dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate, a.sthreecharcountry,a.stwocharcountry from country a where a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.ncountrycode = " + ncountryCode;
		Country objCountry = (Country) jdbcUtilityFunction.queryForObject(strQuery, Country.class, jdbcTemplate);
		return objCountry;
	}

	/**
	 * This method is used to add a new entry to country table. Country Name is
	 * unique for each Site. Need to check for duplicate entry of country name for
	 * the specified site before saving into database. * Need to check that there
	 * should be only one default country for a site.
	 * 
	 * @param objCountry [Country] object holding details to be added in country
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return saved country object with status code 200 if saved successfully else
	 *         if the country already exists, response will be returned as 'Already
	 *         Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createCountry(Country objCountry, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table country " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedCountryList = new ArrayList<>();
		final Country countryObjByName = getCountryObjByName(objCountry.getScountryname(), objCountry.getNsitecode());
		if (countryObjByName == null) {
			String sthreeCountry = objCountry.getSthreecharcountry() != null
					? "N'" + stringUtilityFunction.replaceQuote(objCountry.getSthreecharcountry()) + "'"
					: "N'NA'";
			String stwoCountry = objCountry.getStwocharcountry() != null
					? "N'" + stringUtilityFunction.replaceQuote(objCountry.getStwocharcountry()) + "'"
					: "N'NA'";
			String sequencequery = "select nsequenceno from SeqNoContactMaster where stablename = 'country' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno = (Integer) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class, jdbcTemplate);
			nsequenceno++;

			String insertquery = "Insert into country (ncountrycode,scountryname,scountryshortname,stwocharcountry,sthreecharcountry,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(objCountry.getScountryname())
					+ "',N'" + stringUtilityFunction.replaceQuote(objCountry.getScountryshortname()) + "', "
					+ stwoCountry + ", " + sthreeCountry + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update SeqNoContactMaster set nsequenceno = " + nsequenceno
					+ " where stablename = 'country' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updatequery);
			objCountry.setNcountrycode(nsequenceno);
			savedCountryList.add(objCountry);
			multilingualIDList.add("IDS_ADDCOUNTRY");
			auditUtilityFunction.fnInsertAuditAction(savedCountryList, 1, null, multilingualIDList, userInfo);
			return getCountry(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private Country getCountryObjByName(final String scountryName, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select  scountryname from country where scountryname = N'"
				+ stringUtilityFunction.replaceQuote(scountryName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = " + nmasterSiteCode;
		return (Country) jdbcUtilityFunction.queryForObject(strQuery, Country.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in country table. Need to validate that
	 * the country object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of country name for the specified
	 * site before saving into database. Need to check that there should be only one
	 * default country for a site
	 * 
	 * @param objCountry [Country] object holding details to be updated in country
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return saved country object with status code 200 if saved successfully else
	 *         if the country already exists, response will be returned as 'Already
	 *         Exists' with status code 409 else if the country to be updated is not
	 *         available, response will be returned as 'Already Deleted' with status
	 *         code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateCountry(Country objCountry, UserInfo userInfo) throws Exception {
		final Country countryByID = (Country) getActiveCountryById(objCountry.getNcountrycode(), userInfo);
		if (countryByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ncountrycode from country where scountryname = N'"
					+ stringUtilityFunction.replaceQuote(objCountry.getScountryname()) + "' and ncountrycode <> "
					+ objCountry.getNcountrycode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode = "
					+ userInfo.getNmastersitecode();
			final Country availableCountry = (Country) jdbcUtilityFunction.queryForObject(queryString, Country.class,
					jdbcTemplate);
			if (availableCountry == null) {
				String sthreeCountry = objCountry.getSthreecharcountry() != null
						? "N'" + stringUtilityFunction.replaceQuote(objCountry.getSthreecharcountry()) + "'"
						: "N'NA'";
				String stwoCountry = objCountry.getStwocharcountry() != null
						? "N'" + stringUtilityFunction.replaceQuote(objCountry.getStwocharcountry()) + "'"
						: "N'NA'";
				final String updateQueryString = "update country set scountryname = N'"
						+ stringUtilityFunction.replaceQuote(objCountry.getScountryname()) + "', scountryshortname = N'"
						+ stringUtilityFunction.replaceQuote(objCountry.getScountryshortname())
						+ "', stwocharcountry = " + stwoCountry + "" + ", sthreecharcountry = " + sthreeCountry + ""
						+ ",dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ " where ncountrycode = " + objCountry.getNcountrycode();

				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITCOUNTRY");
				final List<Object> countryListAfterSave = new ArrayList<>();
				countryListAfterSave.add(objCountry);
				final List<Object> countryListBeforeSave = new ArrayList<>();
				countryListBeforeSave.add(countryByID);
				auditUtilityFunction.fnInsertAuditAction(countryListAfterSave, 2, countryListBeforeSave,
						multilingualIDList, userInfo);
				return getCountry(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in Country table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as
	 * 'users','institutionsite','patientmaster','supplier','client','manufacturersiteaddress','courier'
	 * 
	 * @param objCountry [Country] an Object holds the record to be deleted
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return a response entity with list of available country objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteCountry(Country objCountry, UserInfo userInfo) throws Exception {
		final Country countryByID = (Country) getActiveCountryById(objCountry.getNcountrycode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedCountryList = new ArrayList<>();
		if (countryByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select 'IDS_USERS' as Msg from users where ncountrycode = "
					+ objCountry.getNcountrycode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " union all"
					+ " select 'IDS_INSTITUTIONSITE' as Msg from institutionsite where ncountrycode = "
					+ objCountry.getNcountrycode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " union all"
					+ " select 'IDS_PATIENTMASTER' as Msg from patientmaster where ncountrycode = "
					+ objCountry.getNcountrycode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " union all"
					+ " select 'IDS_SUPPLIER' as Msg from supplier where ncountrycode = " + objCountry.getNcountrycode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all"
					+ " select 'IDS_CLIENT' as Msg from client where ncountrycode = " + objCountry.getNcountrycode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all"
					+ " select 'IDS_MANUFACTURERSITEADDRESS' as Msg from manufacturersiteaddress where ncountrycode = "
					+ objCountry.getNcountrycode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " union all"
					+ " select 'IDS_COURIER' as Msg from courier where ncountrycode = " + objCountry.getNcountrycode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objCountry.getNcountrycode()),
						userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final String updateQueryString = "update country set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncountrycode = "
						+ objCountry.getNcountrycode();
				jdbcTemplate.execute(updateQueryString);
				objCountry.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedCountryList.add(objCountry);
				multilingualIDList.add("IDS_DELETECOUNTRY");
				auditUtilityFunction.fnInsertAuditAction(savedCountryList, 1, null, multilingualIDList, userInfo);
				return getCountry(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to retrieve list of all available countries for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         countries for given site.
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBatchPoolCountry(final UserInfo userInfo) throws Exception {
		final String strQuery = "select c.ncountrycode,c.scountryname,c.scountryshortname,c.stwocharcountry,c.sthreecharcountry,c.nsitecode,c.nstatus "
				+ "  from Country c " + " where c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.ncountrycode > 0  "
				+ " and c.nsitecode = " + userInfo.getNmastersitecode();
		return new ResponseEntity<>((List<Country>) jdbcTemplate.query(strQuery, new Country()), HttpStatus.OK);
	}

}
