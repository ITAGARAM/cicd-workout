package com.agaramtech.qualis.submitter.service.district;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.submitter.model.District;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "district" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class DistrictDAOImpl implements DistrictDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available districts for the
	 * specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         districts
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDistrict(UserInfo userInfo) throws Exception {
		String strQuery = "select d.ndistrictcode, d.sdistrictname, d.sdistrictcode, d.nsitecode,d.nstatus, d.nregioncode, r.sregionname,"
				+ " to_char(d.dmodifieddate, '" + userInfo.getSpgsitedatetime()
				+ "') as smodifieddate from district d, region r" + " where d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ndistrictcode>0 and d.nsitecode=r.nsitecode and d.nsitecode=" + userInfo.getNmastersitecode()
				+ " and d.nregioncode = r.nregioncode";
		LOGGER.info("Get Method:"+ strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new District()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active district object based on the specified
	 * ndistrictcode.
	 * @param ndistrictcode [int] primary key of unit object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of district
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public District getActiveDistrictById(int ndistrictcode, UserInfo userInfo) throws Exception {
		final String strQuery = "select d.ndistrictcode, d.sdistrictname, d.sdistrictcode, d.nsitecode,d.nstatus, d.nregioncode, r.sregionname from district d, region r"
				+ " where d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and r.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and d.nsitecode=r.nsitecode and   d.nsitecode="+userInfo.getNmastersitecode()
				+ " and ndistrictcode = " + ndistrictcode + " and d.nregioncode=r.nregioncode";
		return (District) jdbcUtilityFunction.queryForObject(strQuery, District.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to district table. District Name is
	 * unique across the database. Need to check for duplicate entry of district
	 * name for the specified site before saving into database. * Need to check that
	 * there should be only one default district for a site.
	 * @param objDistrict [District] object holding details to be added in district
	 *                    table
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return saved district object with status code 200 if saved successfully else
	 *         if the district already exists, response will be returned as 'Already
	 *         Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createDistrict(District objDistrict, UserInfo userInfo) throws Exception {

		final String districtLockQuery = " lock  table district "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(districtLockQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedDistrictList = new ArrayList<>();
		final District districtListByName = getDistrictListByName(objDistrict.getSdistrictname(),
				userInfo.getNmastersitecode());
		final District districtListByCode = getDistrictListByCode(objDistrict.getSdistrictcode(),
				userInfo.getNmastersitecode());
		String alert = "";
		if (districtListByName== null && districtListByCode== null) {
			final String sequencenoquery = "select nsequenceno from seqnosubmittermanagement where stablename ='district'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert into district (ndistrictcode,sdistrictname,sdistrictcode,nregioncode,dmodifieddate,nsitecode,nstatus) "
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(objDistrict.getSdistrictname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objDistrict.getSdistrictcode()) + "',"
					+ objDistrict.getNregioncode() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " " + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);

			final String updatequery = "update seqnosubmittermanagement set nsequenceno =" + nsequenceno
					+ " where stablename='district'";
			jdbcTemplate.execute(updatequery);
			objDistrict.setNdistrictcode(nsequenceno);
			savedDistrictList.add(objDistrict);
			multilingualIDList.add("IDS_ADDDISTRICT");
			auditUtilityFunction.fnInsertAuditAction(savedDistrictList, 1, null, multilingualIDList, userInfo);
			return getDistrict(userInfo);
		} else {
			if (districtListByName!=null && districtListByCode!=null) {
				alert = commonFunction.getMultilingualMessage("IDS_DISTRICTNAME", userInfo.getSlanguagefilename())
						+ " and "
						+ commonFunction.getMultilingualMessage("IDS_DISTRICTCODE", userInfo.getSlanguagefilename());
			} else if (districtListByName!=null) {
				alert = commonFunction.getMultilingualMessage("IDS_DISTRICTNAME", userInfo.getSlanguagefilename());
			} else {
				alert = commonFunction.getMultilingualMessage("IDS_DISTRICTCODE", userInfo.getSlanguagefilename());
			}
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the district object for the specified district
	 * name and site.
	 * @param sdistrictname   [String] name of the district
	 * @param nmasterSiteCode [int] site code of the district
	 * @return district object based on the specified district name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private District getDistrictListByName(String sdistrictname, int nmasterSiteCode) throws Exception {
		final String strQuery = "select sdistrictname,sdistrictcode from district where " + "  nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode
				+ " and sdistrictname = N'" + stringUtilityFunction.replaceQuote(sdistrictname) + "'";
		return (District) jdbcUtilityFunction.queryForObject(strQuery, District.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the district object for the specified district
	 * code and site.
	 * @param sdistrictcode   [String] code of the district
	 * @param nmasterSiteCode [int] site code of the district
	 * @return district object based on the specified district code and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private District getDistrictListByCode(String sdistrictcode, int nmasterSiteCode) throws Exception {
		final String strQuery = "select sdistrictname,sdistrictcode from district where " + "  nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode
				+ " and sdistrictcode = N'" + stringUtilityFunction.replaceQuote(sdistrictcode) + "'";
		return (District) jdbcUtilityFunction.queryForObject(strQuery, District.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in district table. Need to validate that
	 * the district object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of district name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default district for a site
	 * @param objDistrict [District] object holding details to be updated in
	 *                    district table
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return saved district object with status code 200 if saved successfully else
	 *         if the district already exists, response will be returned as 'Already
	 *         Exists' with status code 409 else if the district to be updated is
	 *         not available, response will be returned as 'Already Deleted' with
	 *         status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateDistrict(District objDistrict, UserInfo userInfo) throws Exception {
		final District objdistrict = getActiveDistrictById(objDistrict.getNdistrictcode(), userInfo);
		String alert = "";
		if (objdistrict == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryName = "select sdistrictcode,sdistrictname from district where sdistrictname = '"
					+ stringUtilityFunction.replaceQuote(objDistrict.getSdistrictname()) + "'  and ndistrictcode <> "
					+ objDistrict.getNdistrictcode() + " and nsitecode ="+userInfo.getNmastersitecode()+" and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<District> districtNameList = jdbcTemplate.query(queryName, new District());
			final String queryCode = "select sdistrictcode,sdistrictname from district where sdistrictcode='"
					+ stringUtilityFunction.replaceQuote(objDistrict.getSdistrictcode()) + "' and ndistrictcode <> "
					+ objDistrict.getNdistrictcode() + " and nsitecode ="+userInfo.getNmastersitecode()+" and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<District> districtCodeList = jdbcTemplate.query(queryCode, new District());
			if (districtNameList.isEmpty() && districtCodeList.isEmpty()) {
				final String updateQueryString = "update district set sdistrictname='"
						+ stringUtilityFunction.replaceQuote(objDistrict.getSdistrictname()) + "', "
						+ "sdistrictcode ='" + stringUtilityFunction.replaceQuote(objDistrict.getSdistrictcode())
						+ "',dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', nregioncode="
						+ objDistrict.getNregioncode() + " where ndistrictcode =" + objDistrict.getNdistrictcode()+" and nsitecode="+userInfo.getNmastersitecode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITDISTRICT");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objDistrict);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(objdistrict);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getDistrict(userInfo);

			} else {
				if (!districtNameList.isEmpty() && !districtCodeList.isEmpty()) {
					alert = commonFunction.getMultilingualMessage("IDS_DISTRICTNAME", userInfo.getSlanguagefilename())
							+ " and " + commonFunction.getMultilingualMessage("IDS_DISTRICTCODE",
									userInfo.getSlanguagefilename());
				} else if (!districtNameList.isEmpty()) {
					alert = commonFunction.getMultilingualMessage("IDS_DISTRICTNAME", userInfo.getSlanguagefilename());
				} else {
					alert = commonFunction.getMultilingualMessage("IDS_DISTRICTCODE", userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in District table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as 'PatientMaster','InstitutionSite','City','Site'
	 * @param objDistrict [District] an Object holds the record to be deleted
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return a response entity with list of available unit objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteDistrict(District objDistrict, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedDistrictList = new ArrayList<>();
		final District district = getActiveDistrictById(objDistrict.getNdistrictcode(), userInfo);
		if (district == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_CITY' as Msg from city where ndistrictcode= "
					+ objDistrict.getNdistrictcode() +" and nsitecode="+userInfo.getNmastersitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ "select 'IDS_INSTITUTION' as Msg from institutionsite where ndistrictcode= "
					+ objDistrict.getNdistrictcode() +" and nsitecode="+userInfo.getNmastersitecode()+"  and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ "select 'IDS_SITE' as Msg from site where ndistrictcode= " + objDistrict.getNdistrictcode()
					+ "  and nsitecode="+userInfo.getNmastersitecode()+"  and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all"//2580.99
					+ " select 'IDS_PATIENTMASTER' as Msg from patientmaster where ndistrictcode= "
					+ objDistrict.getNdistrictcode() + "  and nsitecode="+userInfo.getNmastersitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objDistrict.getNdistrictcode()),
						userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final String updateQueryString = "update district set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where ndistrictcode="
						+ district.getNdistrictcode()+" and nsitecode="+userInfo.getNmastersitecode();
				jdbcTemplate.execute(updateQueryString);
				objDistrict.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedDistrictList.add(objDistrict);
				multilingualIDList.add("IDS_DELETEDISTRICT");
				auditUtilityFunction.fnInsertAuditAction(savedDistrictList, 1, null, multilingualIDList, userInfo);
				return getDistrict(userInfo);
			} else {
				// status code:417
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
