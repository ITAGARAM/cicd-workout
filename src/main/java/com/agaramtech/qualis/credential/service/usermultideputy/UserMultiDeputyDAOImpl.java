package com.agaramtech.qualis.credential.service.usermultideputy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.credential.model.UserMultiDeputy;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.credential.model.UsersSite;
import com.agaramtech.qualis.credential.service.users.UsersDAO;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "usermultideputy" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class UserMultiDeputyDAOImpl implements UserMultiDeputyDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserMultiDeputyDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final UsersDAO usersDAO;

	/**
	 * This method is used to add a new entry to usermultideputy table. Need to
	 * check whether the deputy user already exists Need to check whether the user
	 * is not retired
	 * 
	 * @param objUserMultiDeputy [usermultideputy] object holding details to be
	 *                           added in usermultideputy table
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @param objUsers           [Users] object holding details of users object with
	 *                           nusercode [int] primary key of users object.
	 * @return saved usermultideputy object with status code 200 if saved
	 *         successfully else if the usermultideputy already exists, response
	 *         will be returned as 'Already Exists' with status code 409 or if the
	 *         user already retired, response will be returned as "User Already
	 *         Retired " with statuscode 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createUserMultiDeputy(UserMultiDeputy objUserMultiDeputy, UserInfo userInfo,
			final Users objUsers) throws Exception {

		final String sQueryLock = " lock  table usermultideputy "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);
		final Users userById = usersDAO.getActiveUserById(objUsers.getNusercode(), userInfo);
		if (userById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (userById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final UserMultiDeputy multiDeputy = getUserMultiDeputyByRoleAndDeputy(
						objUserMultiDeputy.getNusersitecode(), objUserMultiDeputy.getNuserrolecode(),
						objUserMultiDeputy.getNdeputyusersitecode());
				if (multiDeputy == null) {

					final String sequenceNoQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='usermultideputy' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
					nsequenceNo++;

					final String insertQuery = "insert into usermultideputy (nusermultideputycode, nusersitecode, ndeputyusersitecode, nuserrolecode, ntransactionstatus, dmodifieddate, nsitecode, nstatus) "
							+ " values(" + nsequenceNo + ", " + objUserMultiDeputy.getNusersitecode() + ", "
							+ objUserMultiDeputy.getNdeputyusersitecode() + ", " + objUserMultiDeputy.getNuserrolecode()
							+ ", " + objUserMultiDeputy.getNtransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertQuery);

					final String updateQuery = "update seqnocredentialmanagement set nsequenceno =" + nsequenceNo
							+ " where stablename='usermultideputy' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(updateQuery);

					objUserMultiDeputy.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
					objUserMultiDeputy.setNsitecode(userInfo.getNmastersitecode());
					objUserMultiDeputy.setNusermultideputycode(nsequenceNo);

					final List<Object> savedUserList = new ArrayList<>();
					savedUserList.add(objUserMultiDeputy);
					auditUtilityFunction.fnInsertAuditAction(savedUserList, 1, null, Arrays.asList("IDS_ADDDEPUTYUSER"),
							userInfo);
					return usersDAO.getUserMultiDeputy(objUserMultiDeputy.getNusersitecode(), userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
	}

	/**
	 * This method is used to fetch the usermultideputy object for the specified
	 * ndeputyusersitecode, nusersitecode and nuserrolecode.
	 * 
	 * @param nusersitecode       [int] user site code of the usermultideputy
	 * @param ndeputyusersitecode [int] deputy user site code of the usermultideputy
	 * @param nuserrolecode       [int] user role code of the usermultideputy
	 * @return usermultideputy object based on the specified ndeputyusersitecode,
	 *         nusersitecode and nuserrolecode.
	 * @throws Exception that are thrown from this DAO layer
	 */
	private UserMultiDeputy getUserMultiDeputyByRoleAndDeputy(final int userSiteCode, final int userRoleCode,
			final int deputyUserSiteCode) throws Exception {
		final String strQuery = "select nusermultideputycode,nusersitecode,ndeputyusersitecode,nuserrolecode, ntransactionstatus,dmodifieddate, nsitecode, nstatus from usermultideputy where nusersitecode = "
				+ userSiteCode + " and" + " ndeputyusersitecode = " + deputyUserSiteCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nuserrolecode = "
				+ userRoleCode;
		LOGGER.info("at getUserMultiDeputyByRoleAndDeputy");
		return (UserMultiDeputy) jdbcUtilityFunction.queryForObject(strQuery, UserMultiDeputy.class, jdbcTemplate);
	}

	/**
	 * This method is used to retrieve active usermultideputy object based on the
	 * specified nusermultideputycode.
	 * 
	 * @param nusermultideputycode [int] primary key of usermultideputy object
	 * @param userInfo             [UserInfo] holding logged in user details based
	 *                             on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         usermultideputy object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public UserMultiDeputy getActiveUserMultiDeputyById(final int nuserMultiDeputyCode) throws Exception {
		final String strQuery = "select nusermultideputycode,nusersitecode,ndeputyusersitecode,nuserrolecode, ntransactionstatus,dmodifieddate, nsitecode, nstatus from usermultideputy umd where umd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umd.nusermultideputycode = "
				+ nuserMultiDeputyCode;
		return (UserMultiDeputy) jdbcUtilityFunction.queryForObject(strQuery, UserMultiDeputy.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in usermultideputy table. Need to
	 * validate that the usermultideputy object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * usermultideputy name for the specified site before saving into database.
	 * 
	 * @param objUserMultiDeputy [UserMultiDeputy] object holding detail to be
	 *                           deleted from usermultideputy table
	 * @param objUsers           [Users] object holding details of users for which
	 *                           the usermultideputy is to be deleted
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @return saved usermultideputy object with status code 200 if saved
	 *         successfully else if the usermultideputy already exists, response
	 *         will be returned as 'Already Exists' with status code 409 else if the
	 *         user already retired, response will be returned as "User Already
	 *         Retired " with statuscode 417 else if the usermultideputy to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateUserMultiDeputy(UserMultiDeputy objUserMultiDeputy, UserInfo userInfo,
			final Users objUsers) throws Exception {
		final UserMultiDeputy deputyById = getActiveUserMultiDeputyById(objUserMultiDeputy.getNusermultideputycode());
		if (deputyById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final Users userById = usersDAO.getActiveUserById(objUsers.getNusercode(), userInfo);
			if (userById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				if (userById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					final String queryString = "select nusermultideputycode,nusersitecode,ndeputyusersitecode,nuserrolecode, ntransactionstatus,dmodifieddate, nsitecode, nstatus from usermultideputy where  nusermultideputycode <> "
							+ objUserMultiDeputy.getNusermultideputycode() + " and nuserrolecode = "
							+ objUserMultiDeputy.getNuserrolecode() + " and nusersitecode = "
							+ objUserMultiDeputy.getNusersitecode() + " and ndeputyusersitecode = "
							+ objUserMultiDeputy.getNdeputyusersitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					final UserMultiDeputy duplicateMultiDeputy = (UserMultiDeputy) jdbcUtilityFunction
							.queryForObject(queryString, UserMultiDeputy.class, jdbcTemplate);
					if (duplicateMultiDeputy == null) {
						final String updateQueryString = "Update usermultideputy set dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nusersitecode = "
								+ objUserMultiDeputy.getNusersitecode() + ",ndeputyusersitecode = "
								+ objUserMultiDeputy.getNdeputyusersitecode() + ",nuserrolecode = "
								+ objUserMultiDeputy.getNuserrolecode() + ",ntransactionstatus = "
								+ objUserMultiDeputy.getNtransactionstatus() + " where nusermultideputycode = "
								+ objUserMultiDeputy.getNusermultideputycode();

						jdbcTemplate.execute(updateQueryString);

						final List<Object> listAfterSave = new ArrayList<>();
						listAfterSave.add(objUserMultiDeputy);
						final List<Object> listBeforeSave = new ArrayList<>();
						listBeforeSave.add(deputyById);
						auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave,
								Arrays.asList("IDS_EDITDEPUTYUSER"), userInfo);
						return usersDAO.getUserMultiDeputy(objUserMultiDeputy.getNusersitecode(), userInfo);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				}
			}
		}
	}

	/**
	 * This method id used to delete an entry in usermultideputy table Need to check
	 * the record is already deleted or not
	 * 
	 * @param objUserMultiDeputy [usermultideputy] an Object holds the record to be
	 *                           deleted
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return a response entity with list of available usermultideputy objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteUserMultiDeputy(UserMultiDeputy objUserMultiDeputy, UserInfo userInfo,
			Users objUsers) throws Exception {
		final UserMultiDeputy activeUserMultiDeputy = (UserMultiDeputy) getActiveUserMultiDeputyById(
				objUserMultiDeputy.getNusermultideputycode());
		if (activeUserMultiDeputy == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final Users userById = usersDAO.getActiveUserById(objUsers.getNusercode(), userInfo);
			if (userById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				if (userById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					final List<Object> savedList = new ArrayList<>();
					final String deleteQuery = "update usermultideputy set dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ " where nusermultideputycode = " + objUserMultiDeputy.getNusermultideputycode();
					jdbcTemplate.execute(deleteQuery);
					objUserMultiDeputy.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
					savedList.add(objUserMultiDeputy);
					auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, Arrays.asList("IDS_DELETEDEPUTY"),
							userInfo);
					return usersDAO.getUserMultiDeputy(objUserMultiDeputy.getNusersitecode(), userInfo);
				}
			}
		}
	}

	/**
	 * This method is used to retrieve list of all get all the UsersSite, Userrole
	 * Data with respect to given nuserSiteCode, nsiteCode,nuserCode,userInfo.
	 * 
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @param nuserSiteCode [int] primary key of userssite object
	 * @param nsiteCode     [int] primary key of site object
	 * @param nuserCode     [int] primary key of users object
	 * @return response entity object holding response status and list of all active
	 *         UsersSite, Userrole Data
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getComboDataforUserMultiDeputy(final int nuserSiteCode, final int nmasterSiteCode,
			final int nuserCode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final UsersSite userSite = usersDAO.getActiveUsersSiteById(nuserSiteCode, userInfo);
		final Users userById = usersDAO.getActiveUserById(nuserCode, userInfo);
		if (userById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (userById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (userSite == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_USERSITEALREADYDELETED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				} else {
					final String strQuery = "select uss.nusersitecode,uss.nusercode,uss.ndefaultsite,uss.dmodifieddate,uss.nsitecode,uss.nstatus , us.sfirstname, us.slastname, us.sloginid from users us, "
							+ "	userssite uss where us.nusercode = uss.nusercode and us.ntransactionstatus =  "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and us.nusercode not in(-1," + nuserCode + ") and uss.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uss.nsitecode = "
							+ userSite.getNsitecode();
					final List<UsersSite> deputyUsersList = (List<UsersSite>) jdbcTemplate.query(strQuery,
							new UsersSite());
					outputMap.put("DeputyUsersList", deputyUsersList);
					final String strUserRole = "select ur.nuserrolecode,ur.suserrolename,ur.sdescription,ur.dmodifieddate,ur.nsitecode,ur.nstatus from usermultirole umr, userrole ur "
							+ " where ur.nuserrolecode = umr.nuserrolecode and " + " umr.nstatus =  "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.nusersitecode = "
							+ nuserSiteCode;
					final List<UserRole> lstUserRole = (List<UserRole>) jdbcTemplate.query(strUserRole, new UserRole());
					outputMap.put("UserRoleToAssignForDeputyUser", lstUserRole);
					return new ResponseEntity<>(outputMap, HttpStatus.OK);
				}
			}
		}
	}

}