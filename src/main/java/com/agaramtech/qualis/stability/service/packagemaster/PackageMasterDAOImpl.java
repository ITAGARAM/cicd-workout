package com.agaramtech.qualis.stability.service.packagemaster;

import java.util.ArrayList;
import java.util.List;
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
import com.agaramtech.qualis.stability.model.PackageMaster;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to perform CRUD Operation on "packagemaster" table by
 * implementing methods from its interface.
 * 
 */
@AllArgsConstructor
@Repository
public class PackageMasterDAOImpl implements PackageMasterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PackageMasterDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active packagemaster for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         packagemaster * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getPackageMaster(UserInfo userInfo) throws Exception {

		final String strQuery = "select pm.npackagemastercode,pm.npackagecategorycode,pm.spackagename,pm.sdescription,pc.spackagecategoryname,pm.nsitecode,pm.nstatus,"
				+ "to_char(pm.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate" + " from packagemaster pm,packagecategory pc " + " where pm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pm.npackagecategorycode = pc.npackagecategorycode "
				+ " and pm.npackagemastercode>0 and pm.nsitecode=" + userInfo.getNmastersitecode();
		LOGGER.info("getPackageMaster() called");
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new PackageMaster()), HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve active packagemaster object based on the
	 * specified npackagemastercode.
	 * 
	 * @param npackagemastercode [int] primary key of packagemaster object
	 * @return response entity object holding response status and data of
	 *         packagemaster object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public PackageMaster getActivePackageMasterById(int npackagemastercode, UserInfo userInfo) throws Exception {
		final String strQuery = "select pm.npackagemastercode,pm.npackagecategorycode,pm.spackagename,pm.sdescription,pc.spackagecategoryname,pm.nsitecode,pm.nstatus"
				+ " from packagemaster pm,packagecategory pc " + " where pm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pm.npackagecategorycode = pc.npackagecategorycode " + " and pm.npackagemastercode = "
				+ npackagemastercode;
		return (PackageMaster) jdbcUtilityFunction.queryForObject(strQuery, PackageMaster.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to packagemaster table. Need to check
	 * for duplicate entry of package name for the specified site before saving into
	 * database. Need to check that there should be only one default packagemaster
	 * for a site
	 * 
	 * @param objPackageMaster [PackageMaster] object holding details to be added in
	 *                         packagemaster table
	 * @return inserted packagemaster object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createPackageMaster(PackageMaster objPackageMaster, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedPackageMasterList = new ArrayList<>();
		final PackageMaster packageMasterByName = getPackageMasterByName(objPackageMaster.getSpackagename(),
				objPackageMaster.getNsitecode());
		if (packageMasterByName == null) {
			String seqquery = "select nsequenceno from SeqNoStability where stablename ='packagemaster' where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nsequenceno = jdbcTemplate.queryForObject(seqquery, Integer.class);
			nsequenceno++;
			String insertquery = "Insert into packagemaster (npackagemastercode,npackagecategorycode,spackagename,sdescription,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + "," + objPackageMaster.getNpackagecategorycode() + ",N'"
					+ stringUtilityFunction.replaceQuote(objPackageMaster.getSpackagename()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objPackageMaster.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update SeqNoStability set nsequenceno=" + nsequenceno
					+ " where stablename='packagemaster'";
			jdbcTemplate.execute(updatequery);
			objPackageMaster.setNpackagemastercode(nsequenceno);
			savedPackageMasterList.add(objPackageMaster);
			multilingualIDList.add("IDS_ADDPACKAGEMASTER");
			auditUtilityFunction.fnInsertAuditAction(savedPackageMasterList, 1, null, multilingualIDList, userInfo);
			return getPackageMaster(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to get a default unit object with respect to the site
	 * code
	 * 
	 * @param nmasterSiteCode [int] Site code
	 * @param nunitCode       [int] primary key of unit table nunitcode
	 * @return a Unit Object
	 * @throws Exception that are from DAO layer
	 */

	/**
	 * This method is used to update entry in packagemaster table. Need to validate
	 * that the packagemaster object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of package name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default packagemaster for a site
	 * 
	 * @param objPackageMaster [PackageMaster] object holding details to be updated
	 *                         in packagemaster table
	 * @return response entity object holding response status and data of updated
	 *         packagemaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updatePackageMaster(PackageMaster objPackageMaster, UserInfo userInfo)
			throws Exception {
		final PackageMaster packagemaster = getActivePackageMasterById(objPackageMaster.getNpackagemastercode(),
				userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		if (packagemaster == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select npackagemastercode from packagemaster where spackagename = '"
					+ stringUtilityFunction.replaceQuote(objPackageMaster.getSpackagename())
					+ "' and npackagemastercode <> " + objPackageMaster.getNpackagemastercode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final List<PackageMaster> packagemasterList = (List<PackageMaster>) jdbcTemplate.query(queryString,
					new PackageMaster());
			if (packagemasterList.isEmpty()) {
				final String updateQueryString = "update packagemaster set spackagename=N'"
						+ stringUtilityFunction.replaceQuote(objPackageMaster.getSpackagename()) + "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(objPackageMaster.getSdescription()) + "'"
						+ " ,npackagecategorycode = " + objPackageMaster.getNpackagecategorycode() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where npackagemastercode="
						+ objPackageMaster.getNpackagemastercode() + ";";
				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(objPackageMaster);
				listBeforeUpdate.add(packagemaster);
				multilingualIDList.add("IDS_EDITPACKAGEMASTER");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getPackageMaster(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in PackageMaster table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as 'packagemaster'
	 * 
	 * @param objPackageMaster[PackageMaster] an Object holds the record to be
	 *                                        deleted
	 * @return a response entity with corresponding HTTP status and an PackageMaster
	 *         object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deletePackageMaster(PackageMaster objPackageMaster, UserInfo userInfo)
			throws Exception {
		final PackageMaster packageMaster = getActivePackageMasterById(objPackageMaster.getNpackagemastercode(),
				userInfo);
		if (packageMaster == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			boolean validRecord = false;
			validRecord = true;
			valiDatorDel = projectDAOSupport
					.validateDeleteRecord(Integer.toString(objPackageMaster.getNpackagemastercode()), userInfo);
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedPackageMasterList = new ArrayList<>();
				final String updateQueryString = "update packagemaster set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where npackagemastercode="
						+ objPackageMaster.getNpackagemastercode();
				jdbcTemplate.execute(updateQueryString);
				objPackageMaster.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedPackageMasterList.add(objPackageMaster);
				multilingualIDList.add("IDS_DELETEPACKAGEMASTER");
				auditUtilityFunction.fnInsertAuditAction(deletedPackageMasterList, 1, null, multilingualIDList,
						userInfo);
				return getPackageMaster(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to fetch the active packagemaster objects for the
	 * specified package name and site.
	 * 
	 * @param spackagename    [String] name of the packagemaster
	 * @param nmasterSiteCode [int] site code of the packagemaster
	 * @return list of active packagemaster code(s) based on the specified package
	 *         name and site
	 * @throws Exception
	 */
	private PackageMaster getPackageMasterByName(String spackagename, int nmasterSiteCode) throws Exception {
		final String strQuery = "select npackagemastercode from packagemaster where spackagename = N'"
				+ stringUtilityFunction.replaceQuote(spackagename) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (PackageMaster) jdbcUtilityFunction.queryForObject(strQuery, PackageMaster.class, jdbcTemplate);
	}
}
