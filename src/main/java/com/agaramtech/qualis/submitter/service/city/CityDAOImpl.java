package com.agaramtech.qualis.submitter.service.city;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.agaramtech.qualis.submitter.model.City;
import com.agaramtech.qualis.submitter.model.District;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "city" table by implementing
 * methods from its interface.
 */
@AllArgsConstructor
@Repository
public class CityDAOImpl implements CityDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CityDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available cities for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         cities
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCity(final UserInfo userInfo) throws Exception {
		LOGGER.info("getCity");
		final String strCity = "select c.ncitycode, c.scityname, c.scitycode, c.nsitecode, c.nstatus ,d.sdistrictname ,c.ndistrictcode,"
				+ "to_char(c.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate from city c,district d where  d.ndistrictcode=c.ndistrictcode and d.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.ncitycode>0 and "
				+ " c.nsitecode = " + userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(strCity, new City()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to city table. City Name is unique
	 * across the database. Need to check for duplicate entry of city name for the
	 * specified site before saving into database. * Need to check for duplicate
	 * entry of city code for the specified site before saving into database.
	 * 
	 * @param objCity  [City] object holding details to be added in city table
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return saved city object with status code 200 if saved successfully else if
	 *         the city already exists, response will be returned as 'Already
	 *         Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createCity(final City submitterCity, final UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table city " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedCityList = new ArrayList<>();
		final City cityByName = getCityByName(submitterCity.getScityname(), userInfo.getNmastersitecode());
		final City cityByCode = getCityByCode(submitterCity.getScitycode(), userInfo.getNmastersitecode());
		String alert = "";
		if (cityByName == null && cityByCode == null) {
			final String sequencenoquery = "select nsequenceno from seqnosubmittermanagement where stablename ='city' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert into city (ncitycode,scityname,scitycode,ndistrictcode,dmodifieddate,nsitecode,nstatus) "
					+ "values(" + nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(submitterCity.getScityname())
					+ "',N'" + stringUtilityFunction.replaceQuote(submitterCity.getScitycode()) + "',"
					+ submitterCity.getNdistrictcode() + "," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update seqnosubmittermanagement set nsequenceno =" + nsequenceno
					+ " where stablename='city'";
			jdbcTemplate.execute(updatequery);
			submitterCity.setNcitycode(nsequenceno);
			savedCityList.add(submitterCity);
			multilingualIDList.add("IDS_ADDCITY");
			auditUtilityFunction.fnInsertAuditAction(savedCityList, 1, null, multilingualIDList, userInfo);
			return getCity(userInfo);
		} else {
			if (cityByName != null && cityByCode != null) {
				alert = commonFunction.getMultilingualMessage("IDS_CITY", userInfo.getSlanguagefilename()) + " and "
						+ commonFunction.getMultilingualMessage("IDS_CITYCODE", userInfo.getSlanguagefilename());
			} else if (cityByName != null) {
				alert = commonFunction.getMultilingualMessage("IDS_CITY", userInfo.getSlanguagefilename());
			} else {
				alert = commonFunction.getMultilingualMessage("IDS_CITYCODE", userInfo.getSlanguagefilename());
			}
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the city object for the specified city name and
	 * site.
	 * 
	 * @param scityname       [String] name of the city
	 * @param nmasterSiteCode [int] site code of the city
	 * @return city object based on the specified city name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private City getCityByName(final String city, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ncitycode from city where scityname = N'"
				+ stringUtilityFunction.replaceQuote(city) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		final City objCity = (City) jdbcUtilityFunction.queryForObject(strQuery, City.class, jdbcTemplate);
		return objCity;
	}

	/**
	 * This method is used to fetch the city object for the specified city code and
	 * site.
	 * 
	 * @param scitycode       [String] code of the city
	 * @param nmasterSiteCode [int] site code of the city
	 * @return city object based on the specified city code and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private City getCityByCode(final String scitycode, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ncitycode from city where scitycode=N'"
				+ stringUtilityFunction.replaceQuote(scitycode) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		final City objCity = (City) jdbcUtilityFunction.queryForObject(strQuery, City.class, jdbcTemplate);
		return objCity;
	}

	/**
	 * This method is used to retrieve active city object based on the specified
	 * ncitycode.
	 * 
	 * @param ncitycode [int] primary key of city object
	 * @param userInfo  [UserInfo] holding logged in user details based on which the
	 *                  list is to be fetched
	 * @return response entity object holding response status and data of city
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public City getActiveCityById(final int ncitycode) throws Exception {
		final String strQuery = "select c.ncitycode, c.scityname, c.scitycode, c.nsitecode, c.nstatus ,d.sdistrictname ,c.ndistrictcode from city c,district d  "
				+ " where d.ndistrictcode=c.ndistrictcode and d.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.ncitycode = " + ncitycode;
		final City objCity = (City) jdbcUtilityFunction.queryForObject(strQuery, City.class, jdbcTemplate);
		return objCity;
	}

	/**
	 * This method is used to update entry in city table. Need to validate that the
	 * city object to be updated is active before updating details in database. Need
	 * to check for duplicate entry of city name for the specified site before
	 * saving into database. Need to check for duplicate entry of city code for the
	 * specified site before saving into database
	 * 
	 * @param objCity  [City] object holding details to be updated in city table
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return saved city object with status code 200 if saved successfully else if
	 *         the city already exists, response will be returned as 'Already
	 *         Exists' with status code 409 else if the city to be updated is not
	 *         available, response will be returned as 'Already Deleted' with status
	 *         code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateCity(final City submitterCity, final UserInfo userInfo) throws Exception {
		final City city = getActiveCityById(submitterCity.getNcitycode());
		String alert = "";
		if (city == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryCity = "select ncitycode from city where scityname = '"
					+ stringUtilityFunction.replaceQuote(submitterCity.getScityname()) + "' and ncitycode <> "
					+ submitterCity.getNcitycode() + " and nsitecode=" + userInfo.getNmastersitecode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<City> cityList = jdbcTemplate.query(queryCity, new City());
			final String queryCityCode = "select ncitycode from city where scitycode='"
					+ stringUtilityFunction.replaceQuote(submitterCity.getScitycode()) + "' and ncitycode <> "
					+ submitterCity.getNcitycode() + " and nsitecode=" + userInfo.getNmastersitecode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<City> cityCodeList = jdbcTemplate.query(queryCityCode, new City());
			if (cityList.isEmpty() && cityCodeList.isEmpty()) {
				final String updateQueryString = "update city set ndistrictcode=" + submitterCity.getNdistrictcode()
						+ " ,scityname='" + stringUtilityFunction.replaceQuote(submitterCity.getScityname()) + "', "
						+ "scitycode ='" + stringUtilityFunction.replaceQuote(submitterCity.getScitycode())
						+ "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ncitycode=" + submitterCity.getNcitycode() + " and nsitecode= "
						+ userInfo.getNmastersitecode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITCITY");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(submitterCity);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(city);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getCity(userInfo);
			} else {
				if (!cityList.isEmpty() && !cityCodeList.isEmpty()) {
					alert = commonFunction.getMultilingualMessage("IDS_CITY", userInfo.getSlanguagefilename()) + " and "
							+ commonFunction.getMultilingualMessage("IDS_CITYCODE", userInfo.getSlanguagefilename());
				} else if (!cityList.isEmpty()) {
					alert = commonFunction.getMultilingualMessage("IDS_CITY", userInfo.getSlanguagefilename());
				} else {
					alert = commonFunction.getMultilingualMessage("IDS_CITYCODE", userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in City table Need to check the record
	 * is already deleted or not Need to check whether the record is used in other
	 * tables such as 'institutionsite','patientmaster'
	 * 
	 * @param objCity  [City] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return a response entity with list of available city objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteCity(final City submitterCity, final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedCityList = new ArrayList<>();
		final City city = getActiveCityById(submitterCity.getNcitycode());
		if (city == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_INSTITUTIONSITE' as Msg from institutionsite where ncitycode= "
					+ submitterCity.getNcitycode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ " select 'IDS_PATIENTMASTER' as Msg from patientmaster where ncitycode="
					+ submitterCity.getNcitycode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(submitterCity.getNcitycode()),
						userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = "update city set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ncitycode= "
						+ submitterCity.getNcitycode() + " and nsitecode=" + userInfo.getNmastersitecode();
				jdbcTemplate.execute(updateQueryString);
				submitterCity.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedCityList.add(submitterCity);
				multilingualIDList.add("IDS_DELETECITY");
				auditUtilityFunction.fnInsertAuditAction(savedCityList, 1, null, multilingualIDList, userInfo);
				return getCity(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to retrieve list of all available districts for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         districts
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDistrict(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select * from district  where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndistrictcode > 0 and "
				+ " nsitecode= " + userInfo.getNmastersitecode();
		outputMap.put("districtList", jdbcTemplate.query(strQuery, new District()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

}
