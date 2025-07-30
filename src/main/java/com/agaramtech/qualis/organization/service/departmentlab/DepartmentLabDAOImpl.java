package com.agaramtech.qualis.organization.service.departmentlab;

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
import com.agaramtech.qualis.organization.model.Lab;
import com.agaramtech.qualis.organization.model.LabSection;
import com.agaramtech.qualis.organization.model.SectionUsers;
import com.agaramtech.qualis.organization.model.SiteDepartment;
import com.agaramtech.qualis.organization.service.sitedepartment.*;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "DepartmentLab" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class DepartmentLabDAOImpl implements DepartmentLabDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentLabDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final SiteDepartmentDAO siteDepartmentDAO;

	/**
	 * This method is used to add a new entry to DepartmentLab table. DepartmentLab
	 * Name is unique across the database. Need to check for duplicate entry of
	 * DepartmentLab name for the specified site before saving into database. * Need
	 * to check that there should be only one default DepartmentLab for a site.
	 * 
	 * @param objDepartmentLab [DepartmentLab] object holding details to be added in
	 *                         DepartmentLab table
	 * @param userInfo         [UserInfo] holding logged in user details based on
	 *                         which the list is to be fetched
	 * @return saved DepartmentLab object with status code 200 if saved successfully
	 *         else if the DepartmentLab already exists, response will be returned
	 *         as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createDepartmentLab(List<DepartmentLab> deptLabList, final UserInfo userInfo,
			final int nsiteCode, final String treePath) throws Exception {

		final String sQuery = " lock  table lockorganisation " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final int nsiteDeptCode = deptLabList.get(0).getNsitedeptcode();
		final String labKeyString = deptLabList.stream().map(deptLabItem -> String.valueOf(deptLabItem.getNlabcode()))
				.collect(Collectors.joining(","));
		final String labQuery = "select * from departmentlab where nlabcode in (" + labKeyString + ") "
				+ " and nsitedeptcode = " + nsiteDeptCode + " and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<DepartmentLab> availableList = (List<DepartmentLab>) jdbcTemplate.query(labQuery,
				new DepartmentLab());
		if (availableList.isEmpty()) {
			final String sGetSeqNoQuery = "select nsequenceno from SeqNoOrganisation where stablename = 'departmentlab';";
			int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);
			String addstring = "";
			String sInsertQuery = " insert into departmentlab (ndeptlabcode,nsitedeptcode,nlabcode,dmodifieddate,nsitecode,nstatus) values ";
			for (int i = 0; i < deptLabList.size(); i++) {
				String addstring1 = " ";
				nSeqNo++;
				final int nsitedeptcode = deptLabList.get(i).getNsitedeptcode();
				final int nlabcode = deptLabList.get(i).getNlabcode();
				if (i < deptLabList.size() - 1) {
					addstring1 = ",";
				}
				addstring = "( " + nSeqNo + "," + nsitedeptcode + "," + nlabcode + "," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + nsiteCode + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;
				deptLabList.get(i).setNdeptlabcode(nSeqNo);
				sInsertQuery = sInsertQuery + addstring;
			}
			jdbcTemplate.execute(sInsertQuery);
			final String sUpdateSeqNoQuery = "update SeqNoOrganisation set nsequenceno =" + nSeqNo
					+ " where stablename = 'departmentlab';";
			jdbcTemplate.execute(sUpdateSeqNoQuery);
			final String queryString = "select * from lab where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nlabcode= "
					+ deptLabList.get(0).getNlabcode();
			final Lab lab = (Lab) jdbcTemplate.queryForObject(queryString, new Lab());
			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(deptLabList), 1, null,
					Arrays.asList("IDS_ADDDEPARTMENTLAB"), userInfo);
			return siteDepartmentDAO.getSiteDepartment(nsiteCode, userInfo, false, treePath + "/" + lab.getSlabname(),
					false, deptLabList.get(0).getNdeptlabcode(), false);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve list of all available DepartmentLabs for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         DepartmentLabs
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getDepartmentLabComboData(final UserInfo userInfo, final int nsiteDeptCode)
			throws Exception {
		final String siteQuery = "select * from sitedepartment where nsitedeptcode = " + nsiteDeptCode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SiteDepartment siteDepartment = (SiteDepartment) jdbcUtilityFunction.queryForObject(siteQuery,
				SiteDepartment.class, jdbcTemplate);
		if (siteDepartment == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select l.* from lab l where l.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nlabcode > 0"
					+ " and not exists (select 1 from departmentlab dl " + " where dl.nsitedeptcode = " + nsiteDeptCode
					+ " and dl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and l.nlabcode = dl.nlabcode ) and l.nsitecode=" + userInfo.getNmastersitecode();
			LOGGER.info("getDepartmentLabComboData-->" + siteQuery);
			return new ResponseEntity<>(jdbcTemplate.query(queryString, new Lab()), HttpStatus.OK);
		}
	}

	/**
	 * This method id used to delete an entry in DepartmentLab table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objDepartmentLab [DepartmentLab] an Object holds the record to be
	 *                         deleted
	 * @param userInfo         [UserInfo] holding logged in user details based on
	 *                         which the list is to be fetched
	 * @return a response entity with list of available DepartmentLab objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteDepartmentLab(final int deptLabCode, final UserInfo userInfo,
			final int nsiteCode, String treePath) throws Exception {
		final String query = "select * from departmentlab where ndeptlabcode =" + deptLabCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DepartmentLab labById = (DepartmentLab) jdbcUtilityFunction.queryForObject(query, DepartmentLab.class,
				jdbcTemplate);
		if (labById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String labSectionQuery1 = "select ls.* from labsection ls " + "where ls.ndeptlabcode = " + deptLabCode
					+ " and ls.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<LabSection> labSectionList1 = (List<LabSection>) jdbcTemplate.query(labSectionQuery1,
					new LabSection());
			if (!labSectionList1.isEmpty()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_DELETELABSECTION", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<Object> auditList = new ArrayList<Object>();
				final List<String> multilingualList = new ArrayList<String>();
				final List<DepartmentLab> deletedDeptLabList = new ArrayList<DepartmentLab>();
				final String updateQueryString = "update departmentlab set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ndeptlabcode="
						+ labById.getNdeptlabcode();
				jdbcTemplate.execute(updateQueryString);
				labById.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedDeptLabList.add(labById);
				auditList.add(deletedDeptLabList);
				multilingualList.add("IDS_DELETEDEPARTMENTLAB");
				final String labSectionQuery = "select ls.* from labsection ls " + "where ls.ndeptlabcode = "
						+ labById.getNdeptlabcode() + " and ls.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<LabSection> labSectionList = (List<LabSection>) jdbcTemplate.query(labSectionQuery,
						new LabSection());
				if (!labSectionList.isEmpty()) {
					final String updateQueryString2 = "update labsection set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ndeptlabcode ="
							+ labById.getNdeptlabcode();
					jdbcTemplate.execute(updateQueryString2);
					labSectionList.forEach(labSection -> labSection
							.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus()));
					auditList.add(labSectionList);
					multilingualList.add("IDS_DELETELABSECTION");
					// deleting sectionusers
					final String labSectionKey = labSectionList.stream()
							.map(labSectionItem -> String.valueOf(labSectionItem.getNlabsectioncode()))
							.collect(Collectors.joining(","));
					final String sectionUserQuery = "select su.* from sectionusers su where"
							+ " su.nlabsectioncode in (" + labSectionKey + ") and su.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<SectionUsers> sectionUserList = (List<SectionUsers>) jdbcTemplate.query(sectionUserQuery,
							new SectionUsers());
					if (!sectionUserList.isEmpty()) {
						final String updateQueryString3 = "update sectionusers set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+ " where nlabsectioncode in (" + labSectionKey + ")";
						jdbcTemplate.execute(updateQueryString3);
						sectionUserList.forEach(sectionUser -> sectionUser
								.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus()));
						auditList.add(sectionUserList);
						multilingualList.add("IDS_DELETESECTIONUSERS");
					}

				}
				auditUtilityFunction.fnInsertListAuditAction(auditList, 1, null, multilingualList, userInfo);
				return siteDepartmentDAO.getSiteDepartment(nsiteCode, userInfo, false,
						treePath.substring(0, treePath.lastIndexOf("/")), false, labById.getNsitedeptcode(), false);
			}
		}
	}
}
