package com.agaramtech.qualis.submitter.service.region;

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
import com.agaramtech.qualis.submitter.model.Region;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "region" table by
 * implementing methods from its interface.
 * 
 * @author ATE234
 * @version 10.0.0.2 ALPD-5645 Master screen -> Region
 */

@Repository
@AllArgsConstructor
public class RegionDAOImpl implements RegionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegionDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active Region for the specified
	 * site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Region
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getRegion(UserInfo userInfo) throws Exception {
//		String filterIt="";
//		if(inputMap.get("skip")!=null   ) {
//			filterIt="ORDER BY 1 DESC LIMIT "+inputMap.get("take")+" OFFSET "+inputMap.get("skip")+" ";
//		}
		String strQuery = "select *, to_char(dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate from region " + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregioncode>0  and nsitecode="
				+ userInfo.getNmastersitecode()+";";
		LOGGER.info("Get Method:" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Region()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active region object based on the specified
	 * nregioncode.
	 * 
	 * @param nregioncode [int] primary key of region object
	 * @return response entity object holding response status and data of region
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public Region getActiveRegionById(int nregioncode, UserInfo userInfo) throws Exception {
		final String strQuery = "select * from region  where nsitecode="+userInfo.getNmastersitecode()+" and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregioncode = " + nregioncode;
		return (Region) jdbcUtilityFunction.queryForObject(strQuery, Region.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to Region table. Need to check for
	 * duplicate entry of Region name for the specified site before saving into
	 * database.
	 * 
	 * @param objRegion [Region] object holding details to be added in Region table
	 * @return inserted Region object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createRegion(Region objRegion, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table region " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedRegionList = new ArrayList<>();

		final Region regionListByName = getRegionListByName(objRegion.getSregionname(),
				userInfo.getNmastersitecode());
		final Region regionListByCode = getRegionListByCode(objRegion.getSregioncode(),
				userInfo.getNmastersitecode());
		// if (regionListByName.isEmpty()) {
		if (regionListByName ==null && regionListByCode == null) {
			String sequencenoquery = "select nsequenceno from seqnosubmittermanagement where stablename ='region'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			String insertquery = "Insert into region (nregioncode,sregionname,sregioncode,dmodifieddate,nsitecode,nstatus) "
					+ "values(" + nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(objRegion.getSregionname())
					+ "',N'" + stringUtilityFunction.replaceQuote(objRegion.getSregioncode()) + "'," + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " " + userInfo.getNmastersitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);

			String updatequery = "update seqnosubmittermanagement set nsequenceno =" + nsequenceno
					+ " where stablename='region'";
			jdbcTemplate.execute(updatequery);
			objRegion.setNregioncode(nsequenceno);
			savedRegionList.add(objRegion);
			multilingualIDList.add("IDS_ADDREGION");
			auditUtilityFunction.fnInsertAuditAction(savedRegionList, 1, null, multilingualIDList, userInfo);
			return getRegion(userInfo);
		} else {
			// ALPD-1596(while adding duplicate record,alert thrown incorrectly)
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
//		if(!regionListByName.isEmpty() && !regionListByCode.isEmpty()) {
//			alert = commonFunction.getMultilingualMessage("IDS_REGIONNAME", userInfo.getSlanguagefilename())+ " and "
//					+commonFunction.getMultilingualMessage("IDS_REGIONCODE", userInfo.getSlanguagefilename());
//		}
//		else if(!regionListByName.isEmpty())
//		{
//			alert = commonFunction.getMultilingualMessage("IDS_REGIONNAME", userInfo.getSlanguagefilename());
//		}
//		else
//		{
//			alert = commonFunction.getMultilingualMessage("IDS_REGIONCODE", userInfo.getSlanguagefilename());
//		}
//		return new ResponseEntity<>(alert+" "+commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),HttpStatus.CONFLICT);
		}
	}

	private Region getRegionListByName(String sregionname, int nmasterSiteCode) throws Exception {
		final String strQuery = "select sregionname,sregioncode from region where " + "  nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode
				+ " and sregionname = N'" + stringUtilityFunction.replaceQuote(sregionname) + "'";
		//return (Region) jdbcTemplate.query(strQuery, new Region());
		return (Region)jdbcUtilityFunction.queryForObject(strQuery, Region.class, jdbcTemplate);
	}

	private Region getRegionListByCode(String sregioncode, int nmasterSiteCode) throws Exception {
		final String strQuery = "select sregionname,sregioncode from region where " + "  nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode
				+ " and sregioncode = N'" + stringUtilityFunction.replaceQuote(sregioncode) + "'";
		//return (Region) jdbcTemplate.query(strQuery, new Region());
		return (Region)jdbcUtilityFunction.queryForObject(strQuery, Region.class, jdbcTemplate);

	}

	/**
	 * This method is used to fetch the active region objects for the specified
	 * region name and site.
	 * 
	 * @param sregionname     [String] and sregioncode it to be checked tha
	 *                        duplicate in the region table.
	 * @param nmasterSiteCode [int] site code of the unit
	 * @return list of active region code(s) based on the specified region name and
	 *         site
	 * @throws Exception
	 */


//	private Region getRegionListByName(String sregionname, String sregioncode, int nmasterSiteCode)
//			throws Exception {
//		final String strQuery = "select sregionname,sregioncode from region where " + "  nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode
//				+ " and sregionname = N'" + stringUtilityFunction.replaceQuote(sregionname) + "' or  sregioncode = N'"
//				+ stringUtilityFunction.replaceQuote(sregioncode) + "'";
//		return (Region) jdbcTemplate.query(strQuery, new Region());
//	}

	/**
	 * This method is used to update entry in region table. Need to validate that
	 * the region object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of region name and region code
	 * for the specified site before saving into database. Need to check that there
	 * should be only one default region for a site
	 * 
	 * @param objRegion [Region] object holding details to be updated in region
	 *                  table
	 * @return response entity object holding response status and data of updated
	 *         region object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateRegion(Region objRegion, UserInfo userInfo) throws Exception {
		final Region objregion = getActiveRegionById(objRegion.getNregioncode(), userInfo);
		if (objregion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select sregioncode,sregionname from region where (sregionname = '"
					+ stringUtilityFunction.replaceQuote(objRegion.getSregionname()) + "' or sregioncode='"
					+ stringUtilityFunction.replaceQuote(objRegion.getSregioncode()) + "' ) and nregioncode <> "
					+ objRegion.getNregioncode() + " and nsitecode="+userInfo.getNmastersitecode()+" and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<Region> regionList = jdbcTemplate.query(queryString, new Region());
			if (regionList.isEmpty()) {
				final String updateQueryString = "update region set sregionname='"
						+ stringUtilityFunction.replaceQuote(objRegion.getSregionname()) + "', " + "sregioncode ='"
						+ stringUtilityFunction.replaceQuote(objRegion.getSregioncode()) + "',dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nsitecode="+userInfo.getNmastersitecode()+" where nregioncode ="
						+ objRegion.getNregioncode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITREGION");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objRegion);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(objregion);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getRegion(userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in region table Need to check the
	 * record is already deleted or not Need to check whether the record is used
	 * inside transaction screen.
	 * 
	 * @param objregion [region] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an region object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteRegion(Region objRegion, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedRegionList = new ArrayList<>();
		final Region region = getActiveRegionById(objRegion.getNregioncode(), userInfo);
		if (region == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_INSTITUTIONSITE' as Msg from institutionsite where nregioncode= "
					+ region.getNregioncode() + " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all"
					+ " select 'IDS_DISTRICT' as Msg from district where nregioncode= " + region.getNregioncode()
					+ " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all"
					+ " select 'IDS_PATIENTMASTER' as Msg from patientmaster where nregioncode="
					+ region.getNregioncode() + " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ " select 'IDS_SITE' as Msg from site where nregioncode=" + region.getNregioncode()
					+ " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objRegion.getNregioncode()),
						userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final String updateQueryString = "update region set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where nsitecode="+userInfo.getNmastersitecode()+" and nregioncode="
						+ region.getNregioncode();
				jdbcTemplate.execute(updateQueryString);
				objRegion.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedRegionList.add(objRegion);
				multilingualIDList.add("IDS_DELETEREGION");
				auditUtilityFunction.fnInsertAuditAction(savedRegionList, 1, null, multilingualIDList, userInfo);	
				return getRegion(userInfo);

			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
