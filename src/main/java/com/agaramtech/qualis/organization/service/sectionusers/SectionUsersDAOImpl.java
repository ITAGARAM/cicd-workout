package com.agaramtech.qualis.organization.service.sectionusers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.organization.model.LabSection;
import com.agaramtech.qualis.organization.model.SectionUsers;
import com.agaramtech.qualis.organization.service.sitedepartment.SiteDepartmentDAO;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "SectionUsers" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class SectionUsersDAOImpl implements SectionUsersDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SectionUsersDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final SiteDepartmentDAO siteDepartmentDAO;

	/**
	 * This method is used to add a new entry to SectionUsers table. SectionUsers
	 * Name is unique across the database. Need to check for duplicate entry of
	 * SectionUsers name for the specified site before saving into database. * Need
	 * to check that there should be only one default SectionUsers for a site.
	 *
	 * @param objSectionUsers [SectionUsers] object holding details to be added in
	 *                        SectionUsers table
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return saved SectionUsers object with status code 200 if saved successfully
	 *         else if the SectionUsers already exists, response will be returned as
	 *         'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSectionUsers(List<SectionUsers> sectionUsersList, final UserInfo userInfo,
			final int nsiteCode) throws Exception {
		final String sQuery = " lock  table lockorganisation " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final int nlabSectionCode = sectionUsersList.get(0).getNlabsectioncode();
		final String usersKeyString = sectionUsersList.stream()
				.map(sectionUserItem -> String.valueOf(sectionUserItem.getNusercode()))
				.collect(Collectors.joining(","));
		final String userQuery = "select * from sectionusers where nusercode in (" + usersKeyString + ") "
				+ " and nlabsectioncode = " + nlabSectionCode + " and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode="+userInfo.getNmastersitecode()+" ;";
		final List<SectionUsers> availableList = jdbcTemplate.query(userQuery, new SectionUsers());
		if (availableList.isEmpty()) {
			final String sGetSeqNoQuery = "select nsequenceno from SeqNoOrganisation where stablename = 'sectionusers';";
			int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);
			String addstring = "";
			String sInsertQuery = " insert into sectionusers (nsectionusercode,nlabsectioncode,nusercode,dmodifieddate,nsitecode,nstatus) values ";
			for (int i = 0; i < sectionUsersList.size(); i++) {
				String addstring1 = " ";
				nSeqNo++;
				final int nlabsectioncode = sectionUsersList.get(i).getNlabsectioncode();
				final int nusercode = sectionUsersList.get(i).getNusercode();
				if (i < sectionUsersList.size() - 1) {
					addstring1 = ",";
				}
				addstring = "( " + nSeqNo + "," + nlabsectioncode + "," + nusercode + "," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + nsiteCode + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;
				sectionUsersList.get(i).setNsectionusercode(nSeqNo);
				sInsertQuery = sInsertQuery + addstring;
			}
			jdbcTemplate.execute(sInsertQuery);
			final String sUpdateSeqNoQuery = "update SeqNoOrganisation set nsequenceno =" + nSeqNo
					+ " where stablename = 'sectionusers';";
			jdbcTemplate.execute(sUpdateSeqNoQuery);
			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(sectionUsersList), 1, null,
					Arrays.asList("IDS_ADDSECTIONUSERS"), userInfo);
			return siteDepartmentDAO.getSectionUsers(Integer.toString(nlabSectionCode), userInfo, false);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	/**
	 * This method is used to retrieve list of all available SectionUserss for the
	 * specified site.
	 *
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         SectionUserss
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSectionUsersComboData(final UserInfo userInfo, final int nlabSectionCode,
			final int nsiteCode) throws Exception {
		final String labSectionQuery = "select * from labsection where nlabsectioncode = " + nlabSectionCode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final LabSection labSection = (LabSection) jdbcUtilityFunction.queryForObject(labSectionQuery, LabSection.class,
				jdbcTemplate);
		if (labSection == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = " select u.*, case when COALESCE(u.sempid, '') = '' then CONCAT(u.sfirstname,' ',u.slastname) else CONCAT(u.sfirstname, ' ', u.slastname, ' ', '(', u.sempid, ')') end as susername from users u, userssite us "
					+ " where u.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and u.ntransactionstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and u.nusercode = us.nusercode and u.nusercode > 0 and us.nsitecode = " + nsiteCode
					+ " and not exists 	(select 1 from sectionusers su where " + " su.nlabsectioncode = "
					+ nlabSectionCode + " and su.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and us.nusercode = su.nusercode) and u.nsitecode=" + userInfo.getNmastersitecode();
			LOGGER.info("getSectionUsersComboData:" + queryString);
			return new ResponseEntity<>(jdbcTemplate.query(queryString, new Users()), HttpStatus.OK);
		}
	}

	/**
	 * This method id used to delete an entry in SectionUsers table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 *
	 * @param objSectionUsers [SectionUsers] an Object holds the record to be
	 *                        deleted
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return a response entity with list of available SectionUsers objects
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteSectionUsers(final int sectionUserCode, final UserInfo userInfo,
			final int nlabSectionCode) throws Exception {
		final String query = "select * from sectionusers where nsectionusercode =" + sectionUserCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SectionUsers userById = (SectionUsers) jdbcUtilityFunction.queryForObject(query, SectionUsers.class,
				jdbcTemplate);
		if (userById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// deleteValidation
			// ALPD-3070 delete validation
			// final String validationQuery = "select 'IDS_INSTRUMENT' as Msg from
			// instrumentsection where nusercode= "
			// + userById.getNusercode() + "and nsitecode= "+nsiteCode
			// +" and nstatus="+
			// Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			// ALPD-5471 - Gowtham R - 20/02/2025 - Organization-->cannot be able to delete
			// the section and user that are not being utilized in the instrument
			final String validationQuery = "select 'IDS_INSTRUMENT' as Msg from labsection ls, instrumentsection ins"
					+ " where ins.nusercode=" + userById.getNusercode() + " and ins.nsitecode=" + userInfo.getNmastersitecode()
					+ " and ls.nlabsectioncode=" + nlabSectionCode + " and ls.nsectioncode=ins.nsectioncode"
					+ " and ins.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ls.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(validationQuery, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(userById.getNusercode()),
						userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = "update sectionusers set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsectionusercode="
						+ userById.getNsectionusercode();

				jdbcTemplate.execute(updateQueryString);
				userById.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				auditUtilityFunction.fnInsertAuditAction(Arrays.asList(userById), 1, null,
						Arrays.asList("IDS_DELETESECTIONUSERS"), userInfo);
				return siteDepartmentDAO.getSectionUsers(Integer.toString(userById.getNlabsectioncode()), userInfo,
						false);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}

	}
}
