package com.agaramtech.qualis.organization.service.department;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
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
import com.agaramtech.qualis.organization.model.Department;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "department" table by
 * implementing methods from its interface.
 */

@AllArgsConstructor
@Repository
public class DepartmentDAOImpl implements DepartmentDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available department for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         department
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDepartment(final UserInfo userInfo) throws Exception {
		final String strQuery = "select d.ndeptcode, d.sdeptname, d.sdescription, d.ndefaultstatus, d.nsitecode, d.nstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
				+ " from department d,transactionstatus ts " + " where d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndeptcode > 0 "
				+ " and ts.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and d.ndefaultstatus=ts.ntranscode" + " and d.nsitecode = " + userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new Department()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active department object based on the
	 * specified ndeptcode.
	 * 
	 * @param ndeptcode [int] primary key of department object
	 * @param userInfo  [UserInfo] holding logged in user details based on which the
	 *                  list is to be fetched
	 * @return response entity object holding response status and data of department
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Department getActiveDepartmentById(final int ndeptcode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select d.ndeptcode, d.sdeptname, d.sdescription, d.ndefaultstatus, d.nsitecode, d.nstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
				+ " from department d,transactionstatus ts" + " where d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.ndefaultstatus=ts.ntranscode"
				+ " and d.ndeptcode = " + ndeptcode;
		final Department objDepartment = (Department) jdbcUtilityFunction.queryForObject(strQuery, Department.class,
				jdbcTemplate);
		LOGGER.info("getActiveDepartmentById-->" + strQuery);
		return objDepartment;
	}

	/**
	 * This method is used to add a new entry to department table. Department Name
	 * is unique across the database. Need to check for duplicate entry of
	 * department name for the specified site before saving into database. * Need to
	 * check that there should be only one default department for a site.
	 * 
	 * @param objDepartment [Department] object holding details to be added in
	 *                      department table
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return saved department object with status code 200 if saved successfully
	 *         else if the department already exists, response will be returned as
	 *         'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createDepartment(final Department organisationDepartment, final UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock table department " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedDepartmentList = new ArrayList<>();
		final Department departmentByName = getDepartmentByName(organisationDepartment.getSdeptname(),
				organisationDepartment.getNsitecode());
		if (departmentByName == null) {
			if (organisationDepartment.getNdefaultstatus() == Enumeration.TransactionStatus.YES
					.gettransactionstatus()) {
				final Department defaultDepartment = getDepartmentByDefaultStatus(
						organisationDepartment.getNsitecode());
				if (defaultDepartment != null) {
					final Department departmentBeforeSave = SerializationUtils.clone(defaultDepartment);
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(departmentBeforeSave);
					defaultDepartment
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					final String updateQueryString = " update department set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ndeptcode ="
							+ defaultDepartment.getNdeptcode();
					jdbcTemplate.execute(updateQueryString);
					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultDepartment);
					multilingualIDList.add("IDS_EDITDEPARTMENT");
					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}
			}
			final String sequencequery = "select nsequenceno from SeqNoOrganisation where stablename ='department'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert into department (ndeptcode,sdeptname,sdescription,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(organisationDepartment.getSdeptname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(organisationDepartment.getSdescription()) + "',"
					+ organisationDepartment.getNdefaultstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update SeqNoOrganisation set nsequenceno =" + nsequenceno
					+ " where stablename ='department'";
			jdbcTemplate.execute(updatequery);
			organisationDepartment.setNdeptcode(nsequenceno);
			savedDepartmentList.add(organisationDepartment);
			multilingualIDList.add("IDS_ADDDEPARTMENT");
			auditUtilityFunction.fnInsertAuditAction(savedDepartmentList, 1, null, multilingualIDList, userInfo);
			return getDepartment(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to get the default department object with respect to the
	 * site.
	 * 
	 * @param nmasterSiteCode [int] Site code
	 * @return Department Object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Department getDepartmentByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ndeptcode,sdeptname,sdescription,ndefaultstatus,nsitecode,nstatus from department d where d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and d.nsitecode = " + nmasterSiteCode;
		return (Department) jdbcUtilityFunction.queryForObject(strQuery, Department.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the department object for the specified
	 * department name and site.
	 * 
	 * @param sdeptname       [String] name of the department
	 * @param nmasterSiteCode [int] site code of the department
	 * @return department object based on the specified department name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Department getDepartmentByName(final String departmentName, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ndeptcode from department where sdeptname = N'"
				+ stringUtilityFunction.replaceQuote(departmentName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (Department) jdbcUtilityFunction.queryForObject(strQuery, Department.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in department table. Need to validate
	 * that the department object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of department name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default department for a site
	 * 
	 * @param objDepartment [Department] object holding details to be updated in
	 *                      department table
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return saved department object with status code 200 if saved successfully
	 *         else if the department already exists, response will be returned as
	 *         'Already Exists' with status code 409 else if the department to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateDepartment(final Department organisationDepartment, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final Department department = (Department) getActiveDepartmentById(organisationDepartment.getNdeptcode(),
				userInfo);
		if (department == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ndeptcode from department where sdeptname = '"
					+ stringUtilityFunction.replaceQuote(organisationDepartment.getSdeptname()) + "' and ndeptcode <> "
					+ organisationDepartment.getNdeptcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();
			final Department departmentExists = (Department) jdbcUtilityFunction.queryForObject(queryString,
					Department.class, jdbcTemplate);
			if (departmentExists == null) {
				if (organisationDepartment.getNdefaultstatus() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {
					final Department defaultDepartment = getDepartmentByDefaultStatus(
							organisationDepartment.getNsitecode());
					if (defaultDepartment != null
							&& defaultDepartment.getNdeptcode() != organisationDepartment.getNdeptcode()) {
						final Department departmentBeforeSave = SerializationUtils.clone(defaultDepartment);
						listBeforeUpdate.add(departmentBeforeSave);
						defaultDepartment
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = " update department set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ndeptcode="
								+ defaultDepartment.getNdeptcode();
						jdbcTemplate.execute(updateQueryString);
						listAfterUpdate.add(defaultDepartment);
					}
				}
				final String updateQueryString = "update department set sdeptname='"
						+ stringUtilityFunction.replaceQuote(organisationDepartment.getSdeptname())
						+ "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(organisationDepartment.getSdescription())
						+ "',ndefaultstatus=" + organisationDepartment.getNdefaultstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ndeptcode="
						+ organisationDepartment.getNdeptcode();
				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(organisationDepartment);
				listBeforeUpdate.add(department);
				multilingualIDList.add("IDS_EDITDEPARTMENT");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getDepartment(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in Department table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as 'users','sitedepartment'
	 * 
	 * @param objDepartment [Department] an Object holds the record to be deleted
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return a response entity with list of available department objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteDepartment(final Department organisationDepartment, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedDepartmentList = new ArrayList<>();
		final Department department = (Department) getActiveDepartmentById(organisationDepartment.getNdeptcode(),
				userInfo);
		if (department == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select 'IDS_USERS' as Msg from users where ndeptcode= " + department.getNdeptcode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNmastersitecode() + " union all "
					+ " select 'IDS_ORGANISATION' as Msg from sitedepartment where ndeptcode= "
					+ department.getNdeptcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(organisationDepartment.getNdeptcode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = "update department set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ndeptcode="
						+ organisationDepartment.getNdeptcode();
				jdbcTemplate.execute(updateQueryString);
				organisationDepartment.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedDepartmentList.add(organisationDepartment);
				multilingualIDList.add("IDS_DELETEDEPARTMENT");
				auditUtilityFunction.fnInsertAuditAction(deletedDepartmentList, 1, null, multilingualIDList, userInfo);
				return getDepartment(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
