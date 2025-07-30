package com.agaramtech.qualis.organization.service.sitedepartment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.credential.model.UsersSite;
import com.agaramtech.qualis.credential.service.users.UsersDAO;
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
import com.agaramtech.qualis.organization.model.DepartmentLab;
import com.agaramtech.qualis.organization.model.LabSection;
import com.agaramtech.qualis.organization.model.SectionUsers;
import com.agaramtech.qualis.organization.model.SiteDepartment;
import com.agaramtech.qualis.organization.model.TreeMenuFormat;
import com.agaramtech.qualis.organization.model.TreeNodeItem;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "SiteDepartment" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class SiteDepartmentDAOImpl implements SiteDepartmentDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SiteDepartmentDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final UsersDAO usersDAO;

	/**
	 * This method is used to retrieve list of all available SiteDepartments for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         SiteDepartments
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getOrganisation(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String nsitecode = "";
		if (inputMap.get("nsitecode") != null) {
			nsitecode = " and nsitecode=" + inputMap.get("nsitecode").toString();
		}
		
		final String siteQuery = "select nsitecode, ssitename, ssiteaddress from site where nsitecode > 0 and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + nsitecode;
		List<Site> siteList = (List<Site>) jdbcTemplate.query(siteQuery, new Site());
		LOGGER.info("Called getOrganisation....");
		outputMap.put("OrganisationSite", siteList);
		if (!siteList.isEmpty()) {
			final Site selectedSite = siteList.get(siteList.size() - 1);
			outputMap.putAll((Map<String, Object>) getSiteDepartment(selectedSite.getNsitecode(), userInfo, false,
					selectedSite.getSsitename(), true, 0, true).getBody());
		} else {
			outputMap.put("SiteDepartment", new ArrayList<>());
			outputMap.put("DepartmentLab", new ArrayList<>());
			outputMap.put("LabSection", new ArrayList<>());
			outputMap.put("SectionUsers", new ArrayList<>());
			outputMap.put("SectionUserRoleList", new ArrayList<>());
			outputMap.put("SelectedSectionUserCode", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private Map<String, Object> generateTreeData(final Map<String, Object> inputData, final Site site,
			final String selectedPath, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> initialOpenNodesArray = new ArrayList<String>();
		final List<SiteDepartment> siteDeptList = (List<SiteDepartment>) inputData.get("SiteDepartment");
		final List<DepartmentLab> deptLabList = (List<DepartmentLab>) inputData.get("DepartmentLab");
		final List<LabSection> labSectionList = (List<LabSection>) inputData.get("LabSection");
		final StringBuffer treePath = new StringBuffer();
		treePath.append(site.getSsitename());

		TreeNodeItem siteNodeItem = new TreeNodeItem();
		siteNodeItem.setPrimaryKeyField("nsitecode");
		siteNodeItem.setPrimaryKeyValue(site.getNsitecode());
		siteNodeItem.setSelectedNode("Site");
		siteNodeItem.setSelectedNodeDetail(site);
		siteNodeItem.setSelectedNodeName(site.getSsitename());

		TreeMenuFormat siteTreeMenuFormat = new TreeMenuFormat();
		siteTreeMenuFormat.setItem(siteNodeItem);
		siteTreeMenuFormat.setKey(site.getSsitename());
		siteTreeMenuFormat.setPrimaryKey(site.getNsitecode());
		siteTreeMenuFormat.setParentKey(0);
		siteTreeMenuFormat.setLabel(commonFunction.getMultilingualMessage("IDS_SITE", userInfo.getSlanguagefilename())
				+ ":" + site.getSsitename());
		siteTreeMenuFormat.setServiceUrl("organisation/getSiteDepartment");

		initialOpenNodesArray.add(site.getSsitename());
		final List<TreeMenuFormat> treeList = new ArrayList<TreeMenuFormat>();

		for (int i = 0; i < siteDeptList.size(); i++) {
			final SiteDepartment siteDepartment = siteDeptList.get(i);
			if (i == 0) {
				treePath.append("/" + siteDepartment.getSdeptname());
			}
			TreeNodeItem treeNodeItem = new TreeNodeItem();
			treeNodeItem.setPrimaryKeyField("nsitedeptcode");
			treeNodeItem.setPrimaryKeyValue(siteDepartment.getNsitedeptcode());
			treeNodeItem.setSelectedNode("Department");
			treeNodeItem.setSelectedNodeDetail(siteDepartment);
			treeNodeItem.setSelectedNodeName(siteDepartment.getSdeptname());

			TreeMenuFormat treeMenuFormat = new TreeMenuFormat();
			treeMenuFormat.setItem(treeNodeItem);
			treeMenuFormat.setKey(siteDepartment.getSdeptname());// +i);
			treeMenuFormat.setPrimaryKey(siteDepartment.getNsitedeptcode());
			treeMenuFormat.setParentKey(siteDepartment.getNsitecode());
			treeMenuFormat
					.setLabel(commonFunction.getMultilingualMessage("IDS_DEPTNAME", userInfo.getSlanguagefilename())
							+ ":" + siteDepartment.getSdeptname());
			treeMenuFormat.setServiceUrl("organisation/getDepartmentLab");

			initialOpenNodesArray.add(site.getSsitename() + "/" + siteDepartment.getSdeptname());
			List<TreeMenuFormat> deptLabTreeList = new ArrayList<TreeMenuFormat>();
			for (int j = 0; j < deptLabList.size(); j++) {
				DepartmentLab deptLab = deptLabList.get(j);
				if (deptLab.getNsitedeptcode() == siteDepartment.getNsitedeptcode()) {
					if (i == 0 && j == 0) {
						treePath.append("/" + deptLab.getSlabname());
					}
					TreeNodeItem deptNodeItem = new TreeNodeItem();
					deptNodeItem.setPrimaryKeyField("ndeptlabcode");
					deptNodeItem.setPrimaryKeyValue(deptLab.getNdeptlabcode());
					deptNodeItem.setSelectedNode("Lab");
					deptNodeItem.setSelectedNodeDetail(deptLab);
					deptNodeItem.setSelectedNodeName(deptLab.getSlabname());

					TreeMenuFormat deptLabTreeFormat = new TreeMenuFormat();
					deptLabTreeFormat.setItem(deptNodeItem);
					deptLabTreeFormat.setKey(deptLab.getSlabname());// +j);
					deptLabTreeFormat.setPrimaryKey(deptLab.getNdeptlabcode());
					deptLabTreeFormat.setParentKey(deptLab.getNsitedeptcode());
					deptLabTreeFormat
							.setLabel(commonFunction.getMultilingualMessage("IDS_LAB", userInfo.getSlanguagefilename())
									+ ":" + deptLab.getSlabname());
					deptLabTreeFormat.setServiceUrl("organisation/getLabSection");

					initialOpenNodesArray.add(
							site.getSsitename() + "/" + siteDepartment.getSdeptname() + "/" + deptLab.getSlabname());
					List<TreeMenuFormat> labSectionTreeList = new ArrayList<TreeMenuFormat>();
					for (int k = 0; k < labSectionList.size(); k++) {
						LabSection labSection = labSectionList.get(k);
						if (labSection.getNdeptlabcode() == deptLab.getNdeptlabcode()) {

							if (i == 0 && j == 0 && k == 0) {
								treePath.append("/" + labSection.getSsectionname());
							}

							TreeNodeItem labNodeItem = new TreeNodeItem();
							labNodeItem.setPrimaryKeyField("nlabsectioncode");
							labNodeItem.setPrimaryKeyValue(labSection.getNlabsectioncode());
							labNodeItem.setSelectedNode("Section");
							labNodeItem.setSelectedNodeDetail(labSection);
							labNodeItem.setSelectedNodeName(labSection.getSsectionname());

							TreeMenuFormat labSectionTreeFormat = new TreeMenuFormat();
							labSectionTreeFormat.setItem(labNodeItem);
							labSectionTreeFormat.setKey(labSection.getSsectionname());// +k);
							labSectionTreeFormat.setPrimaryKey(labSection.getNlabsectioncode());
							labSectionTreeFormat.setParentKey(labSection.getNdeptlabcode());
							labSectionTreeFormat.setLabel(commonFunction.getMultilingualMessage("IDS_SECTION",
									userInfo.getSlanguagefilename()) + ":" + labSection.getSsectionname());
							labSectionTreeFormat.setServiceUrl("organisation/getSectionUsers");

							// k++;
							labSectionTreeList.add(labSectionTreeFormat);
							initialOpenNodesArray.add(site.getSsitename() + "/" + siteDepartment.getSdeptname() + "/"
									+ deptLab.getSlabname() + "/" + labSection.getSsectionname());
						}

					}
					;

					// j++;
					deptLabTreeFormat.setNodes(labSectionTreeList);
					deptLabTreeList.add(deptLabTreeFormat);
				}

			}
			;

			// i++;
			treeMenuFormat.setNodes(deptLabTreeList);
			treeList.add(treeMenuFormat);

		}
		;

		siteTreeMenuFormat.setNodes(treeList);

		outputMap.put("TreeData", Arrays.asList(siteTreeMenuFormat));
		outputMap.put("TreeInitialOpenNodes", initialOpenNodesArray);
		if (selectedPath == null) {
			outputMap.put("CompleteTreePath", treePath);
		} else {
			outputMap.put("CompleteTreePath", selectedPath);
		}
		return outputMap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getSiteDepartment(final int nsiteCode, final UserInfo userInfo,
			final boolean graphView, String treePath, final boolean initialGet, final Integer primaryKey,
			final boolean fetchUser) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String siteQuery = "select * from site where nsitecode = " + nsiteCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Site site = (Site) jdbcTemplate.queryForObject(siteQuery, new Site());
		outputMap.put("SelectedOrgSite", site);

		final String deptQuery = "select sd.nsitedeptcode,sd.ndeptcode,sd.nsitecode,d.sdeptname,sd.nstatus from sitedepartment sd,"
				+ " department d where sd.nsitecode =" + nsiteCode + " and sd.ndeptcode=d.ndeptcode and  sd.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<SiteDepartment> siteDeptList = (List<SiteDepartment>) jdbcTemplate.query(deptQuery,
				new SiteDepartment());
		outputMap.put("SiteDepartment", siteDeptList);
		outputMap.put("GraphView", graphView);
		outputMap.put("SelectedNode", "Site");

		if (!siteDeptList.isEmpty()) {
			final String siteDeptKey = siteDeptList.stream()
					.map(siteDeptItem -> String.valueOf(siteDeptItem.getNsitedeptcode()))
					.collect(Collectors.joining(","));

			outputMap.put("AddedChildPrimaryKey", siteDeptList.get(0).getNsitedeptcode());
			outputMap.put("SelectedNodeName", siteDeptList.get(0).getSdeptname());

			final Map<String, Object> dataMap = (Map<String, Object>) getDepartmentLab(siteDeptKey, userInfo, graphView,
					fetchUser).getBody();
			outputMap.putAll(dataMap);
			if (initialGet) {
				treePath = (String) dataMap.get("CompleteTreePath");
			}
			outputMap.putAll(generateTreeData(outputMap, site, treePath, userInfo));
			if (initialGet == false) {
				outputMap.put("CompleteTreePath", treePath);
				outputMap.put("AddedChildPrimaryKey", primaryKey);
				if (treePath != null) {
					outputMap.put("SelectedNodeName",
							treePath.substring(treePath.lastIndexOf("/") + 1, treePath.length()));
				}
			}
		} else {
			outputMap.put("CompleteTreePath", treePath);
			outputMap.put("DepartmentLab", new ArrayList<>());
			outputMap.put("LabSection", new ArrayList<>());
			outputMap.put("SectionUsers", new ArrayList<>());
			outputMap.put("SectionUserRoleList", new ArrayList<>());
			outputMap.put("SelectedSectionUserCode", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getDepartmentLab(final String siteDeptCode, final UserInfo userInfo,
			final boolean graphView, final boolean fetchUser) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		if (siteDeptCode.split(",").length == 1) {
			final String query = "select sd.nsitedeptcode,sd.ndeptcode,sd.nsitecode,d.sdeptname,sd.nstatus from sitedepartment sd,"
					+ " department d where sd.nsitedeptcode =" + siteDeptCode
					+ " and sd.ndeptcode=d.ndeptcode and  sd.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final SiteDepartment siteDept = (SiteDepartment) jdbcTemplate.queryForObject(query, new SiteDepartment());
			outputMap.put("SelectedSiteDepartment", siteDept);
		}

		final String deptLabQuery = "select dl.ndeptlabcode,dl.nsitedeptcode,dl.nlabcode,l.slabname,dl.nstatus "
				+ " from departmentlab dl,lab l where dl.nlabcode=l.nlabcode and " + " dl.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and l.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dl.nsitedeptcode in ("
				+ siteDeptCode + ")";

		final List<DepartmentLab> deptLabList = (List<DepartmentLab>) jdbcTemplate.query(deptLabQuery,
				new DepartmentLab());

		outputMap.put("DepartmentLab", deptLabList);
		outputMap.put("GraphView", graphView);
		outputMap.put("SelectedNode", "Department");

		if (!deptLabList.isEmpty()) {
			final String deptLabKey = deptLabList.stream()
					.map(deptLabItem -> String.valueOf(deptLabItem.getNdeptlabcode())).collect(Collectors.joining(","));

			outputMap.put("AddedChildPrimaryKey", deptLabList.get(0).getNdeptlabcode());
			outputMap.put("SelectedNodeName", deptLabList.get(0).getSlabname());

			outputMap.putAll((Map<String, Object>) getLabSection(deptLabKey, userInfo, graphView, fetchUser).getBody());
		} else {
			outputMap.put("LabSection", new ArrayList<>());
			outputMap.put("SectionUsers", new ArrayList<>());
			outputMap.put("SectionUserRoleList", new ArrayList<>());
			outputMap.put("SelectedSectionUserCode", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getLabSection(final String deptLabCode, final UserInfo userInfo,
			final boolean graphView, final boolean fetchUser) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		if (deptLabCode.split(",").length == 1) {
			final String query = "select dl.ndeptlabcode,dl.nsitedeptcode,dl.nlabcode,l.slabname,dl.nstatus "
					+ " from departmentlab dl,lab l where dl.nlabcode=l.nlabcode and " + " dl.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and l.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dl.ndeptlabcode = "
					+ deptLabCode;
			final DepartmentLab siteDept = (DepartmentLab) jdbcTemplate.queryForObject(query, new DepartmentLab());
			outputMap.put("SelectedDepartmentLab", siteDept);
		}
		final String labSectionQuery = "select ls.nlabsectioncode,ls.nsectioncode,ls.ndeptlabcode,s.ssectionname,ls.nstatus from labsection ls,section s "
				+ "where ls.ndeptlabcode in (" + deptLabCode + ") and ls.nsectioncode=s.nsectioncode and "
				+ " ls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<LabSection> labSectionList = (List<LabSection>) jdbcTemplate.query(labSectionQuery,
				new LabSection());

		outputMap.put("LabSection", labSectionList);
		outputMap.put("GraphView", graphView);
		outputMap.put("SelectedNode", "Lab");

		if (!labSectionList.isEmpty()) {
			outputMap.put("AddedChildPrimaryKey", labSectionList.get(0).getNlabsectioncode());
			outputMap.put("SelectedNodeName", labSectionList.get(0).getSsectionname());
			String labSectionKey = "";
			if (graphView) {
				labSectionKey = labSectionList.stream()
						.map(labSectionItem -> String.valueOf(labSectionItem.getNlabsectioncode()))
						.collect(Collectors.joining(","));

			} else {
				if (fetchUser) {
					labSectionKey = Integer.toString(labSectionList.get(0).getNlabsectioncode());
				}
			}
			if (labSectionKey == "") {
				outputMap.put("SectionUsers", new ArrayList<>());
				outputMap.put("SectionUserRoleList", new ArrayList<>());
				outputMap.put("SelectedSectionUserCode", null);
			} else {
				outputMap.putAll((Map<String, Object>) getSectionUsers(labSectionKey, userInfo, graphView).getBody());
			}
		} else {
			outputMap.put("SectionUsers", new ArrayList<>());
			outputMap.put("SectionUserRoleList", new ArrayList<>());
			outputMap.put("SelectedSectionUserCode", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getSectionUsers(final String nlabSectionCode, final UserInfo userInfo,
			final boolean graphView) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.put("GraphView", graphView);
		outputMap.put("SelectedNode", "Section");
		if (nlabSectionCode.split(",").length == 1) {
			final String query = "select ls.nlabsectioncode,ls.nsectioncode,ls.ndeptlabcode,s.ssectionname,ls.nstatus from labsection ls,section s "
					+ "where ls.nlabsectioncode = " + nlabSectionCode + " and ls.nsectioncode=s.nsectioncode and "
					+ "ls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
					+ "s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final LabSection labSection = (LabSection) jdbcUtilityFunction.queryForObject(query, LabSection.class,
					jdbcTemplate);
			outputMap.put("SelectedLabSection", labSection);
		}
		final String sectionUserQuery = "select su.nsectionusercode,su.nusercode,u.sfirstname,"
				+ " CONCAT(u.sfirstname,' ',u.slastname) as susername, u.sempid,su.nstatus,s.ssectionname, "
				+ " su.nsitecode,su.nlabsectioncode,u.ndesignationcode,d.sdesignationname, dt.sdeptname, l.slabname, st.ssitename "
				+ " from sectionusers su,users u,designation d, section s , labsection ls, "
				+ " departmentlab dl, sitedepartment sd, department dt, lab l, site st "
				+ " where su.nlabsectioncode in (" + nlabSectionCode + ") "
				+ " and d.ndesignationcode = u.ndesignationcode " + " and su.nlabsectioncode =ls.nlabsectioncode "
				+ " and ls.ndeptlabcode = dl.ndeptlabcode " + " and dl.nsitedeptcode = sd.nsitedeptcode "
				+ " and ls.nsectioncode= s.nsectioncode " + " and dl.nlabcode = l.nlabcode "
				+ " and sd.ndeptcode =dt.ndeptcode " + " and sd.nsitecode = st.nsitecode"
				+ " and su.nusercode=u.nusercode and su.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nusercode<>-1 ";

		final List<SectionUsers> sectionUserList = (List<SectionUsers>) jdbcTemplate.query(sectionUserQuery,
				new SectionUsers());
		outputMap.put("SectionUsers", sectionUserList);

		if (!sectionUserList.isEmpty()) {
			final int nsiteCode = sectionUserList.get(sectionUserList.size() - 1).getNsitecode();
			final int nuserCode = sectionUserList.get(sectionUserList.size() - 1).getNusercode();

			final SectionUsers sectionUser = sectionUserList.get(0);
			final String path = sectionUser.getSsitename() + "/" + sectionUser.getSdeptname() + "/"
					+ sectionUser.getSlabname() + "/" + sectionUser.getSsectionname();
			outputMap.put("CompleteTreePath", path);
			outputMap.put("AddedChildPrimaryKey", sectionUser.getNlabsectioncode());
			outputMap.put("SelectedNodeName", sectionUser.getSsectionname());
			outputMap.putAll((Map<String, Object>) getSectionUserRole(nsiteCode, nuserCode, userInfo).getBody());
		} else {
			outputMap.put("SelectedSectionUserCode", null);
			outputMap.put("SectionUserRoleList", new ArrayList<>());
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getSectionUserRole(final int nsiteCode, final int nuserCode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.put("SelectedSectionUserCode", nuserCode);
		final String siteQuery = "select * from userssite where nusercode=" + nuserCode + " and nsitecode = "
				+ nsiteCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final UsersSite usersSite = (UsersSite) jdbcUtilityFunction.queryForObject(siteQuery, UsersSite.class,
				jdbcTemplate);
		if (usersSite != null) {
			final Map<String, Object> roleMap = (Map<String, Object>) usersDAO
					.getUserMultiRole(usersSite.getNusersitecode(), userInfo).getBody();
			outputMap.put("SectionUserRoleList", roleMap.get("UserMultiRole"));
			Map<Integer, Object> userRoleMap = new HashMap<Integer, Object>();
			userRoleMap.put(nuserCode, roleMap.get("UserMultiRole"));
			outputMap.put("SectionUserRoleMap", userRoleMap);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to SiteDepartment table.
	 * SiteDepartment Name is unique across the database. Need to check for
	 * duplicate entry of SiteDepartment name for the specified site before saving
	 * into database. * Need to check that there should be only one default
	 * SiteDepartment for a site.
	 * 
	 * @param objSiteDepartment [SiteDepartment] object holding details to be added
	 *                          in SiteDepartment table
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return saved SiteDepartment object with status code 200 if saved
	 *         successfully else if the SiteDepartment already exists, response will
	 *         be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSiteDepartment(List<SiteDepartment> siteDepartmentList, final UserInfo userInfo,
			String treePath) throws Exception {

		final String sQuery = " lock  table lockorganisation " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final int nsiteCode = siteDepartmentList.get(0).getNsitecode();
		final String deptKeyString = siteDepartmentList.stream()
				.map(siteDeptItem -> String.valueOf(siteDeptItem.getNdeptcode())).collect(Collectors.joining(","));

		final String deptQuery = "select sd.* from sitedepartment sd where sd.ndeptcode in (" + deptKeyString + ") "
				+ " and sd.nsitecode = " + nsiteCode + " and sd.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<SiteDepartment> availableList = (List<SiteDepartment>) jdbcTemplate.query(deptQuery,
				new SiteDepartment());
		if (availableList.isEmpty()) {
			final String sGetSeqNoQuery = "select nsequenceno from SeqNoOrganisation where stablename = 'sitedepartment';";
			int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);

			String addstring = "";
			String sInsertQuery = " insert into sitedepartment (nsitedeptcode,ndeptcode,dmodifieddate,nsitecode,nstatus) values ";

			for (int i = 0; i < siteDepartmentList.size(); i++) {

				String addstring1 = " ";
				nSeqNo++;
				final int ndeptcode = siteDepartmentList.get(i).getNdeptcode();
				if (i < siteDepartmentList.size() - 1) {
					addstring1 = ",";
				}

				addstring = "( " + nSeqNo + "," + ndeptcode + "," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + nsiteCode + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;

				siteDepartmentList.get(i).setNsitedeptcode(nSeqNo);
				sInsertQuery = sInsertQuery + addstring;
			}
			jdbcTemplate.execute(sInsertQuery);
			final String sUpdateSeqNoQuery = "update SeqNoOrganisation set nsequenceno =" + nSeqNo
					+ " where stablename = 'sitedepartment';";
			jdbcTemplate.execute(sUpdateSeqNoQuery);
			final String queryString = "select * from department where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndeptcode= "
					+ siteDepartmentList.get(0).getNdeptcode();
			final Department dept = (Department) jdbcTemplate.queryForObject(queryString, new Department());
			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(siteDepartmentList), 1, null,
					Arrays.asList("IDS_ADDSITEDEPARTMENT"), userInfo);
			return getSiteDepartment(nsiteCode, userInfo, false, treePath + "/" + dept.getSdeptname(), false,
					siteDepartmentList.get(0).getNsitedeptcode(), false);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> getSiteDepartmentComboData(final UserInfo userInfo, final int nsiteCode)
			throws Exception {
		final String siteQuery = "select * from site where nsitecode = " + nsiteCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Site site = (Site) jdbcUtilityFunction.queryForObject(siteQuery, Site.class, jdbcTemplate);
		if (site == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select d.* from department d where d.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.ndeptcode > 0"
					+ " and not exists (select 1 from sitedepartment sd " + " where sd.nsitecode = " + nsiteCode
					+ " and sd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and d.ndeptcode = sd.ndeptcode) and d.nsitecode=" + userInfo.getNmastersitecode();
			return new ResponseEntity<>(jdbcTemplate.query(queryString, new Department()), HttpStatus.OK);
		}
	}

	/**
	 * This method id used to delete an entry in SiteDepartment table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objSiteDepartment [SiteDepartment] an Object holds the record to be
	 *                          deleted
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return a response entity with list of available SiteDepartment objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSiteDepartment(final int siteDeptCode, final UserInfo userInfo, String treePath)
			throws Exception {
		final String query = "select * from sitedepartment where nsitedeptcode =" + siteDeptCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SiteDepartment deptById = (SiteDepartment) jdbcUtilityFunction.queryForObject(query, SiteDepartment.class,
				jdbcTemplate);

		if (deptById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// ALPD-3070 delete validation
			final String deptLabQry = "select dl.* from departmentlab dl where dl.nsitedeptcode=" + siteDeptCode
					+ " and dl.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<DepartmentLab> deptLabLst = (List<DepartmentLab>) jdbcTemplate.query(deptLabQry,
					new DepartmentLab());
			if (!deptLabLst.isEmpty()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DELETEDEPARTMENTLAB",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {

				final List<Object> auditList = new ArrayList<Object>();
				final List<String> multilingualList = new ArrayList<String>();
				final List<SiteDepartment> deletedSiteDeptList = new ArrayList<SiteDepartment>();
				final String updateQueryString = "update sitedepartment set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsitedeptcode="
						+ deptById.getNsitedeptcode();

				jdbcTemplate.execute(updateQueryString);
				deptById.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedSiteDeptList.add(deptById);
				auditList.add(deletedSiteDeptList);
				multilingualList.add("IDS_DELETESITEDEPARTMENT");

				// deleting departmentlab
				final String deptLabQuery = "select dl.* from departmentlab dl where  dl.nsitedeptcode ="
						+ deptById.getNsitedeptcode() + " and dl.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final List<DepartmentLab> deptLabList = (List<DepartmentLab>) jdbcTemplate.query(deptLabQuery,
						new DepartmentLab());
				if (!deptLabList.isEmpty()) {
					final String updateQueryString1 = "update departmentlab set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsitedeptcode="
							+ deptById.getNsitedeptcode();
					jdbcTemplate.execute(updateQueryString1);

					deptLabList.forEach(item -> item
							.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus()));
					auditList.add(deptLabList);
					multilingualList.add("IDS_DELETEDEPARTMENTLAB");

					// deleting labsection
					final String deptLabKey = deptLabList.stream()
							.map(deptLabItem -> String.valueOf(deptLabItem.getNdeptlabcode()))
							.collect(Collectors.joining(","));

					final String labSectionQuery = "select ls.* from labsection ls " + "where ls.ndeptlabcode in ("
							+ deptLabKey + ") and ls.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<LabSection> labSectionList = (List<LabSection>) jdbcTemplate.query(labSectionQuery,
							new LabSection());
					if (!labSectionList.isEmpty()) {
						final String updateQueryString2 = "update labsection set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ndeptlabcode in ("
								+ deptLabKey + ")";
						jdbcTemplate.execute(updateQueryString2);
						labSectionList.forEach(labSection -> labSection
								.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus()));
						auditList.add(labSectionList);
						multilingualList.add("IDS_DELETELABSECTION");

						// deleting sectionusers

						final String labSectionKey = labSectionList.stream()
								.map(labSectionItem -> String.valueOf(labSectionItem.getNlabsectioncode()))
								.collect(Collectors.joining(","));
						final String sectionUserQuery = "select su.* from sectionusers su where su.nlabsectioncode in ("
								+ labSectionKey + ") and su.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						final List<SectionUsers> sectionUserList = (List<SectionUsers>) jdbcTemplate
								.query(sectionUserQuery, new SectionUsers());
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
				}
				auditUtilityFunction.fnInsertListAuditAction(auditList, 1, null, multilingualList, userInfo);
				return getSiteDepartment((int) deptById.getNsitecode(), userInfo, false,
						treePath.substring(0, treePath.lastIndexOf("/")), false, (int) deptById.getNsitecode(), false);

			}
		}
	}

}
