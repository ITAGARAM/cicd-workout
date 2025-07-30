package com.agaramtech.qualis.organization.service.labsection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.DepartmentLab;
import com.agaramtech.qualis.organization.model.LabSection;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.organization.model.SectionUsers;
import com.agaramtech.qualis.organization.service.sitedepartment.SiteDepartmentDAO;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "LabSection" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class LabSectionDAOImpl implements LabSectionDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(LabSectionDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final SiteDepartmentDAO siteDepartmentDAO;

	/**
	 * This method is used to add a new entry to LabSection table. LabSection Name
	 * is unique across the database. Need to check for duplicate entry of
	 * LabSection name for the specified site before saving into database. * Need to
	 * check that there should be only one default LabSection for a site.
	 *
	 * @param objLabSection [LabSection] object holding details to be added in
	 *                      LabSection table
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return saved LabSection object with status code 200 if saved successfully
	 *         else if the LabSection already exists, response will be returned as
	 *         'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createLabSection(List<LabSection> labSectionList, final UserInfo userInfo,
			final int nsiteCode, final String treePath) throws Exception {
		final String sQuery = " lock  table lockorganisation " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final int ndeptLabCode = labSectionList.get(0).getNdeptlabcode();
		final String sectionKeyString = labSectionList.stream()
				.map(labSectionItem -> String.valueOf(labSectionItem.getNsectioncode()))
				.collect(Collectors.joining(","));
		final String labQuery = "select * from labsection where nsectioncode in (" + sectionKeyString + ") "
				+ " and ndeptlabcode = " + ndeptLabCode + " and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode="+userInfo.getNmastersitecode()+" ;";

		final List<LabSection> availableList = jdbcTemplate.query(labQuery, new LabSection());
		if (availableList.isEmpty()) {
			final String sGetSeqNoQuery = "select nsequenceno from SeqNoOrganisation where stablename = 'labsection';";
			int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);
			String addstring = "";
			String sInsertQuery = " insert into labsection (nlabsectioncode,ndeptlabcode,nsectioncode,dmodifieddate,nsitecode,nstatus) values ";
			for (int i = 0; i < labSectionList.size(); i++) {
				String addstring1 = " ";
				nSeqNo++;
				final int ndeptlabcode = labSectionList.get(i).getNdeptlabcode();
				final int nsectioncode = labSectionList.get(i).getNsectioncode();
				if (i < labSectionList.size() - 1) {
					addstring1 = ",";
				}
				addstring = "( " + nSeqNo + "," + ndeptlabcode + "," + nsectioncode + "," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + nsiteCode + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;
				labSectionList.get(i).setNlabsectioncode(nSeqNo);
				sInsertQuery = sInsertQuery + addstring;
			}
			jdbcTemplate.execute(sInsertQuery);
			final String sUpdateSeqNoQuery = "update SeqNoOrganisation set nsequenceno =" + nSeqNo
					+ " where stablename = 'labsection';";
			jdbcTemplate.execute(sUpdateSeqNoQuery);
			final String queryString = "select * from section where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsectioncode= "
					+ labSectionList.get(0).getNsectioncode();
			final Section section = jdbcTemplate.queryForObject(queryString, new Section());
			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(labSectionList), 1, null,
					Arrays.asList("IDS_ADDLABSECTION"), userInfo);
			return siteDepartmentDAO.getSiteDepartment(nsiteCode, userInfo, false,
					treePath + "/" + section.getSsectionname(), false, labSectionList.get(0).getNlabsectioncode(),
					false);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve list of all available LabSections for the
	 * specified site.
	 *
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         LabSections
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getLabSectionComboData(final UserInfo userInfo, final int ndeptLabCode)
			throws Exception {
		final String siteQuery = "select * from departmentlab where ndeptlabcode = " + ndeptLabCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DepartmentLab departmentLab = (DepartmentLab) jdbcUtilityFunction.queryForObject(siteQuery,
				DepartmentLab.class, jdbcTemplate);
		if (departmentLab == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select s.* from section s where s.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsectioncode > 0"
					+ " and not exists (select 1 from labsection ls " + " where ls.ndeptlabcode = " + ndeptLabCode
					+ " and ls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nsectioncode = ls.nsectioncode) and s.nsitecode=" + userInfo.getNmastersitecode();
			LOGGER.info("getLabSectionComboData:" + siteQuery);
			return new ResponseEntity<>(jdbcTemplate.query(queryString, new Section()), HttpStatus.OK);
		}
	}

	/**
	 * This method id used to delete an entry in LabSection table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 *
	 * @param objLabSection [LabSection] an Object holds the record to be deleted
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return a response entity with list of available LabSection objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteLabSection(final int labSectionKey, final UserInfo userInfo,
			final int nsiteCode, String treePath) throws Exception {
		String query = "select * from labsection where nlabsectioncode =" + labSectionKey + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final LabSection sectionById = (LabSection) jdbcUtilityFunction.queryForObject(query, LabSection.class,
				jdbcTemplate);
		if (sectionById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// ALPD-3070 delete validation
			final String sectionUserQuery1 = "select su.* from sectionusers su where" + " su.nlabsectioncode ="
					+ labSectionKey + " and su.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<SectionUsers> sectionUserList1 = jdbcTemplate.query(sectionUserQuery1,
					new SectionUsers());
			if (!sectionUserList1.isEmpty()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DELETESECTIONUSERS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<Object> auditList = new ArrayList<Object>();
				final List<String> multilingualList = new ArrayList<String>();
				final List<LabSection> deletedLabSectionList = new ArrayList<LabSection>();
				final String updateQueryString = "update labsection set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nlabsectioncode="
						+ labSectionKey;
				jdbcTemplate.execute(updateQueryString);
				sectionById.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedLabSectionList.add(sectionById);
				auditList.add(deletedLabSectionList);
				multilingualList.add("IDS_DELETELABSECTION");
				// deleting sectionusers
				final String sectionUserQuery = "select su.* from sectionusers su where" + " su.nlabsectioncode ="
						+ labSectionKey + " and su.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<SectionUsers> sectionUserList = jdbcTemplate.query(sectionUserQuery,
						new SectionUsers());
				if (!sectionUserList.isEmpty()) {
					final String updateQueryString3 = "update sectionusers set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " "
							+ "where nlabsectioncode =" + labSectionKey;
					jdbcTemplate.execute(updateQueryString3);
					sectionUserList.forEach(sectionUser -> sectionUser
							.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus()));
					auditList.add(sectionUserList);
					multilingualList.add("IDS_DELETESECTIONUSERS");
				}
				auditUtilityFunction.fnInsertListAuditAction(auditList, 1, null, multilingualList, userInfo);
				return siteDepartmentDAO.getSiteDepartment(nsiteCode, userInfo, false,
						treePath.substring(0, treePath.lastIndexOf("/")), false, sectionById.getNdeptlabcode(), false);
			}
		}
	}
}