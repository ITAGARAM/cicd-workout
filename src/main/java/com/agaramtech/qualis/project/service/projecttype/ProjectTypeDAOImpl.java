package com.agaramtech.qualis.project.service.projecttype;

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
import com.agaramtech.qualis.project.model.ProjectType;

import lombok.AllArgsConstructor;
/**
 * This class is used to perform CRUD Operation on "ProjectType" table by 
 * implementing methods from its interface. 
 */
@AllArgsConstructor
@Repository
public class ProjectTypeDAOImpl implements ProjectTypeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectTypeDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available ProjectType for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         projectType
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<List<ProjectType>> getProjectType(final UserInfo userInfo) throws Exception {
		final String strQuery = "select pt.* from ProjectType pt where pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nprojecttypecode>0 and pt.nsitecode=" + userInfo.getNmastersitecode();
		LOGGER.info("getProjectType-->" + strQuery);
		return new ResponseEntity<List<ProjectType>>(jdbcTemplate.query(strQuery, new ProjectType()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to ProjectType table. ProjectType Name
	 * is unique across the database. Need to check for duplicate entry of
	 * ProjectType name for the specified site before saving into database. * Need
	 * to check that there should be only one default ProjectType for a site.
	 * 
	 * @param objProjectType [ProjectType] object holding details to be added in
	 *                       ProjectType table
	 * @param userInfo       [UserInfo] holding logged in user details based on
	 *                       which the list is to be fetched
	 * @return saved projectType object with status code 200 if saved successfully
	 *         else if the ProjectType already exists, response will be returned as
	 *         'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createProjectType(final ProjectType projecttype, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedProjectTypeList = new ArrayList<>();
		final ProjectType projectTypeByName = getProjectTypeByName(projecttype.getSprojecttypename(),
				userInfo.getNmastersitecode());
		if (projectTypeByName == null) {
			int nSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoprojectmanagement where stablename='projecttype'", Integer.class);
			nSeqNo++;
			final String projecttypeInsert = "insert into projecttype(nprojecttypecode,sprojecttypename,sdescription,dmodifieddate,nsitecode,nstatus)"
					+ " values(" + nSeqNo + ",N'"
					+ stringUtilityFunction.replaceQuote(projecttype.getSprojecttypename()) + "',N'"
					+ stringUtilityFunction.replaceQuote(projecttype.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(projecttypeInsert);
			jdbcTemplate.execute(
					"update seqnoprojectmanagement set nsequenceno = " + nSeqNo + " where stablename='projecttype'");
			projecttype.setNprojecttypecode(nSeqNo);
			savedProjectTypeList.add(projecttype);
			multilingualIDList.add("IDS_ADDPROJECTTYPE");
			auditUtilityFunction.fnInsertAuditAction(savedProjectTypeList, 1, null, multilingualIDList, userInfo);
			return getProjectType(projecttype.getNsitecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method was common usage in the THIST Project example :- Aliquotplan.java
	 * ,goodin.java ect.
	 * @param nmasterSiteCode master site code
     * @return response entity  object holding response status and data of projectType object
	 * @throws Exception that are thrown in the DAO layer 
	 */
	@Override
	public ResponseEntity<Object> getProjectType(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from projecttype pt where pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojecttypecode > 0 "
				+ " and pt.nsitecode = " + nmasterSiteCode;
		return new ResponseEntity<>((List<ProjectType>) jdbcTemplate.query(strQuery, new ProjectType()), HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the projectType object for the specified
	 * ProjectType name and site.
	 * 
	 * @param sprojecttypename [String] name of the ProjectType
	 * @param nmasterSiteCode  [int] site code of the ProjectType
	 * @return projectType object based on the specified ProjectType name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private ProjectType getProjectTypeByName(final String sprojecttypename, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select nprojecttypecode from projecttype where sprojecttypename = N'"
				+ stringUtilityFunction.replaceQuote(sprojecttypename) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (ProjectType) jdbcUtilityFunction.queryForObject(strQuery, ProjectType.class, jdbcTemplate);
	}

	/**
	 * This method is used to retrieve active projectType object based on the
	 * specified nprojecttypeCode.
	 * 
	 * @param nprojecttypeCode [int] primary key of projectType object
	 * @param userInfo         [UserInfo] holding logged in user details based on
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         projectType object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveProjectTypeById(final int nprojecttypecode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select * from projecttype pt where pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nprojecttypecode = "
				+ nprojecttypecode;
		final ProjectType projecttype = (ProjectType) jdbcUtilityFunction.queryForObject(strQuery, ProjectType.class,
				jdbcTemplate);
		if (projecttype == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(projecttype, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to update entry in ProjectType table. Need to validate
	 * that the projectType object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of ProjectType name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default ProjectType for a site
	 * 
	 * @param objProjectType [ProjectType] object holding details to be updated in
	 *                       ProjectType table
	 * @param userInfo       [UserInfo] holding logged in user details based on
	 *                       which the list is to be fetched
	 * @return saved projectType object with status code 200 if saved successfully
	 *         else if the ProjectType already exists, response will be returned as
	 *         'Already Exists' with status code 409 else if the ProjectType to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateProjectType(final ProjectType ProjectType, final UserInfo userInfo)
			throws Exception {
		final ResponseEntity<Object> projectTypeResponse = getActiveProjectTypeById(ProjectType.getNprojecttypecode(),
				userInfo);
		if (projectTypeResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return projectTypeResponse;
		} else {
			final String queryString = "select nprojecttypecode from ProjectType where sprojecttypename = N'"
					+ stringUtilityFunction.replaceQuote(ProjectType.getSprojecttypename())
					+ "' and nprojecttypecode <> " + ProjectType.getNprojecttypecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();
			final List<ProjectType> projecttypeList = (List<ProjectType>) jdbcTemplate.query(queryString,
					new ProjectType());
			if (projecttypeList.isEmpty()) {
				final String updateQueryString = "update ProjectType set sprojecttypename=N'"
						+ stringUtilityFunction.replaceQuote(ProjectType.getSprojecttypename()) + "', sdescription =N'"
						+ stringUtilityFunction.replaceQuote(ProjectType.getSdescription()) + "',dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nprojecttypecode="
						+ ProjectType.getNprojecttypecode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITPROJECTTYPE");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(ProjectType);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(projectTypeResponse.getBody());
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getProjectType(ProjectType.getNsitecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in ProjectType table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as 'projectmaster','samplecycle','studyidentity' and few
	 * more tables
	 * 
	 * @param objProjectType [ProjectType] an Object holds the record to be deleted
	 * @param userInfo       [UserInfo] holding logged in user details based on
	 *                       which the list is to be fetched
	 * @return a response entity with list of available projectType objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteProjectType(final ProjectType ProjectType, final UserInfo userInfo)
			throws Exception {
		final ResponseEntity<Object> projecttypeResponseEntity = getActiveProjectTypeById(
				ProjectType.getNprojecttypecode(), userInfo);
		boolean validRecord = true;
		if (projecttypeResponseEntity.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return projecttypeResponseEntity;
		} else {
			final String query = "select 'IDS_PROJECTMASTER' as Msg from projectmaster where nprojecttypecode= "
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_SAMPLECYCLE' as Msg from samplecycle where nprojecttypecode= "
					+ ProjectType.getNprojecttypecode() + " and nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_STUDYIDENTITY' as Msg from studyidentity where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_COLLECTIONTUBETYPE' as Msg from collectiontubetype where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_VISITNUMBER' as Msg from visitnumber where nprojecttypecode= "
					+ ProjectType.getNprojecttypecode() + " and nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_COLLECTIONSITE' as Msg from collectionsite where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_SAMPLEPUNCHSITE' as Msg from samplepunchsite where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_SAMPLECOLLECTIONTYPE' as Msg from samplecollectiontype where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_GOODSIN' as Msg from goodsin where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_SAMPLESTORAGEMAPPING' as Msg from samplestoragemapping where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_SAMPLESTORAGESTRUCTURE' as Msg from samplestoragelocation where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_RETRIEVALCERTIFICATE' as Msg from retrievalcertificate where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_PATIENTCATEGORY' as Msg from patientcategory where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_SAMPLEPROCESSMAPPING' as Msg from sampleprocesstype where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_ALIQUOTPLAN' as Msg from aliquotplan where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_STORAGESAMPLECOLLECTION' as Msg from storagesamplecollection where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_STORAGESAMPLEPROCESSING' as Msg from storagesampleprocessing where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_TEMPORARYSTORAGE' as Msg from temporarystorage where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_BULKBARCODEGENERATION' as Msg from bulkbarcodegeneration where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_PATIENTCATEGORY' as Msg from patientcategory where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " union all select 'IDS_BARCODEFORMATTING' as Msg from bulkbarcodeconfigdetails where nprojecttypecode="
					+ ProjectType.getNprojecttypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(ProjectType.getNprojecttypecode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedProjectTypeList = new ArrayList<>();
				final String updateQueryString = "update ProjectType set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nprojecttypecode="
						+ ProjectType.getNprojecttypecode();
				jdbcTemplate.execute(updateQueryString);
				ProjectType.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedProjectTypeList.add(ProjectType);
				multilingualIDList.add("IDS_DELETEPROJECTTYPE");
				auditUtilityFunction.fnInsertAuditAction(deletedProjectTypeList, 1, null, multilingualIDList, userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
		return getProjectType(ProjectType.getNsitecode());
	}

	/**
	 * if (objSampleType.getNformcode() ==
	 * Enumeration.FormCode.PRODUCTCATEGORY.getFormCode()) { } TestGroupDAOImpl.java
	 *@param userInfo [UserInfo] holding logged in user details
     * @return response entity  object holding response status and data of projectType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getApprovedProjectType(final UserInfo userInfo) throws Exception {

		final String strQuery = "select pt.nprojecttypecode, pt.sprojecttypename " + " from   "
				+ " projecttype pt,projectmaster pm, projectmasterhistory ph " + " where  "
				+ " pt.nstatus = pm.nstatus and pm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ph.nsitecode = pm.nsitecode and  ph.nsitecode =" + userInfo.getNtranssitecode()
				+ " and  pm.nsitecode =" + userInfo.getNtranssitecode() + " and pt.nsitecode="
				+ userInfo.getNmastersitecode() + ""
				+ " and pt.nprojecttypecode = pm.nprojecttypecode and ph.ntransactionstatus ="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ " and ph.nprojectmasterhistorycode = any(select max(nprojectmasterhistorycode) from  projectmasterhistory ph "
				+ " where ph.nprojectmastercode = pm.nprojectmastercode and ph.nsitecode="
				+ userInfo.getNtranssitecode() + "" + " group  by ph.nprojectmastercode) and ph.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " group  by pt.nprojecttypecode  "
				+ " union " + " select pt.nprojecttypecode, pt.sprojecttypename "
				+ " from   projecttype pt,projectmaster pm,testgroupspecification tgs where  pt.nstatus = pm.nstatus "
				+ " and pm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and   pt.nsitecode=" + userInfo.getNmastersitecode()
				+ " and   pt.nprojecttypecode = pm.nprojecttypecode and  pt.nprojecttypecode>0  and pm.nprojectmastercode=tgs.nprojectmastercode "
				+ " and tgs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group  by pt.nprojecttypecode";

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ProjectType()), HttpStatus.OK);
	}

}
