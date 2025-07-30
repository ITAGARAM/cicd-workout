package com.agaramtech.qualis.stability.service.packagecategory;

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
import com.agaramtech.qualis.stability.model.PackageCategory;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to perform CRUD Operation on "packagecategory" table by
 * implementing methods from its interface.
 */

@AllArgsConstructor
@Repository
public class PackageCategoryDAOImpl implements PackageCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PackageCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active packagecategorys for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         packagecategorys
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getPackageCategory(UserInfo userInfo) throws Exception {
		final String strQuery = "select npackagecategorycode,spackagecategoryname,sdescription,nsitecode,nstatus"
				+ " from packagecategory p" + " where p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.npackagecategorycode>0 and p.nsitecode=" + userInfo.getNmastersitecode();
		LOGGER.info("getPackageCategory() called");
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new PackageCategory()), HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve active packagecategory object based on the
	 * specified npackagecategorycode.
	 * 
	 * @param npackagecategorycode [int] primary key of packagecategory object
	 * @return response entity object holding response status and data of
	 *         packagecategory object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public PackageCategory getActivePackageCategoryById(int npackagecategorycode, UserInfo userInfo) throws Exception {
		final String strQuery = "select npackagecategorycode,spackagecategoryname,sdescription,nsitecode,nstatus"
				+ " from packagecategory p " + " where p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.npackagecategorycode = "
				+ npackagecategorycode;
		return (PackageCategory) jdbcUtilityFunction.queryForObject(strQuery, PackageCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to packagecategory table. Need to
	 * check for duplicate entry of packagecategory name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default packagecategory for a site
	 * 
	 * @param objPackageCategory [PackageCategory] object holding details to be
	 *                           added in packagecategory table
	 * @return inserted packagecategory object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createPackageCategory(PackageCategory objPackageCategory, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedPackageCategoryList = new ArrayList<>();
		final PackageCategory packageCategoryByName = getPackageCategoryByName(
				objPackageCategory.getSpackagecategoryname(), objPackageCategory.getNsitecode());
		if (packageCategoryByName == null) {
			String seqquery = "select nsequenceno from SeqNoStability where stablename ='packagecategory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nsequenceno = jdbcTemplate.queryForObject(seqquery, Integer.class);
			nsequenceno++;
			String insertquery = "Insert into packagecategory (npackagecategorycode,spackagecategoryname,sdescription,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(objPackageCategory.getSpackagecategoryname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objPackageCategory.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update SeqNoStability set nsequenceno=" + nsequenceno
					+ " where stablename='packagecategory'";
			jdbcTemplate.execute(updatequery);
			objPackageCategory.setNpackagecategorycode(nsequenceno);
			savedPackageCategoryList.add(objPackageCategory);
			multilingualIDList.add("IDS_ADDPACKAGECATEGORY");
			auditUtilityFunction.fnInsertAuditAction(savedPackageCategoryList, 1, null, multilingualIDList, userInfo);
			return getPackageCategory(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update entry in packagecategory table. Need to
	 * validate that the packagecategory object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * packagecategory name for the specified site before saving into database. Need
	 * to check that there should be only one default packagecategory for a site
	 * 
	 * @param objPackageCategory [PackageCategory] object holding details to be
	 *                           updated in packagecategory table
	 * @return response entity object holding response status and data of updated
	 *         packagecategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updatePackageCategory(PackageCategory objPackageCategory, UserInfo userInfo)
			throws Exception {
		final PackageCategory packagecategory = getActivePackageCategoryById(
				objPackageCategory.getNpackagecategorycode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		if (packagecategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select npackagecategorycode from packagecategory where spackagecategoryname = '"
					+ stringUtilityFunction.replaceQuote(objPackageCategory.getSpackagecategoryname())
					+ "' and npackagecategorycode <> " + objPackageCategory.getNpackagecategorycode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNmastersitecode();
			final PackageCategory checkpackagecategory = (PackageCategory) jdbcUtilityFunction
					.queryForObject(queryString, PackageCategory.class, jdbcTemplate);
			if (checkpackagecategory == null) {
				final String updateQueryString = "update packagecategory set spackagecategoryname=N'"
						+ stringUtilityFunction.replaceQuote(objPackageCategory.getSpackagecategoryname())
						+ "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(objPackageCategory.getSdescription()) + "'"
						+ ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ " where npackagecategorycode=" + objPackageCategory.getNpackagecategorycode() + ";";
				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(objPackageCategory);
				listBeforeUpdate.add(packagecategory);
				multilingualIDList.add("IDS_EDITPACKAGECATEGORY");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getPackageCategory(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in PackageCategory table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as 'packagemaster'
	 * 
	 * @param objPackageCategory [PackageCategory] an Object holds the record to be
	 *                           deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         PackageCategory object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deletePackageCategory(PackageCategory objPackageCategory, UserInfo userInfo)
			throws Exception {
		final PackageCategory packageCategory = getActivePackageCategoryById(
				objPackageCategory.getNpackagecategorycode(), userInfo);
		if (packageCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_PACKAGEMASTER' as Msg from packagemaster where npackagecategorycode= "
					+ objPackageCategory.getNpackagecategorycode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objPackageCategory.getNpackagecategorycode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedPackageCategoryList = new ArrayList<>();
				final String updateQueryString = "update packagecategory set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where npackagecategorycode="
						+ objPackageCategory.getNpackagecategorycode();

				jdbcTemplate.execute(updateQueryString);
				objPackageCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedPackageCategoryList.add(objPackageCategory);
				multilingualIDList.add("IDS_DELETEPACKAGECATEGORY");
				auditUtilityFunction.fnInsertAuditAction(deletedPackageCategoryList, 1, null, multilingualIDList,
						userInfo);
				return getPackageCategory(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to fetch the active packagecategory objects for the
	 * specified packagecategory name and site.
	 * 
	 * @param spackagecategoryname [String] name of the packagecategory
	 * @param nmasterSiteCode      [int] site code of the packagecategory
	 * @return list of active packagecategory code(s) based on the specified
	 *         packagecategory name and site
	 * @throws Exception
	 */
	private PackageCategory getPackageCategoryByName(String spackagecategoryname, int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select npackagecategorycode from packagecategory where spackagecategoryname = N'"
				+ stringUtilityFunction.replaceQuote(spackagecategoryname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (PackageCategory) jdbcUtilityFunction.queryForObject(strQuery, PackageCategory.class, jdbcTemplate);
	}
}
