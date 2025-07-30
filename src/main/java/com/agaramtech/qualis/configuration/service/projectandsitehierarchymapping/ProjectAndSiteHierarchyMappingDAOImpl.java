package com.agaramtech.qualis.configuration.service.projectandsitehierarchymapping;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.configuration.model.ProjectAndSiteHierarchyMapping;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "projectsitehierarchymapping" table by
 * implementing methods from its interface.
 * @author Mullai Balaji.V 
 * BGSI-7
 * 1/07/2025 
 */
@AllArgsConstructor
@Repository
public class ProjectAndSiteHierarchyMappingDAOImpl  implements  ProjectAndSiteHierarchyMappingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectAndSiteHierarchyMappingDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available projectsitehierarchymapping for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         projectsitehierarchymapping
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProjectAndSiteHierarchyMapping(final UserInfo userInfo) throws Exception 
	{
		final String strQuery="SELECT p.nprojectsitehierarchymapcode ,p.nbioprojectcode ,p.nsitehierarchyconfigcode ,"
				+ "p.nsitecode ,p.nstatus ,b.sprojecttitle ,s.sconfigname ,"
				+ "COALESCE(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') AS sdisplaystatus "
				+ "FROM projectsitehierarchymapping p, bioproject b, sitehierarchyconfig s , transactionstatus ts"
				+ "  WHERE p.nprojectsitehierarchymapcode > 0 "
				+ "AND p.nbioprojectcode = b.nbioprojectcode  AND p.nsitehierarchyconfigcode = s.nsitehierarchyconfigcode AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND p.nsitecode = "
				+ userInfo.getNmastersitecode()+ " AND b.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND b.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND s.nsitecode = "
				+ userInfo.getNmastersitecode()  + " "+ " AND ts.ntranscode = s.ntransactionstatus "
				+ "ORDER BY p.nprojectsitehierarchymapcode DESC";
				
		LOGGER.info("Get Method" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ProjectAndSiteHierarchyMapping()), HttpStatus.OK);
	}
	
	/**
	 * This method is used to retrieve list of all available sitehierarchymapping for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         projectsitehierarchymapping
	 * @throws Exception that are thrown from this DAO layer
	 */
    @Override
	public ResponseEntity<Object> getSitemap(final UserInfo userInfo) throws Exception 
	{
		final String strquery="SELECT nsitehierarchyconfigcode,sconfigname,ntransactionstatus,nsitecode,nstatus"+ "  FROM sitehierarchyconfig " + " WHERE ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " AND nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND nsitecode = "
								+ userInfo.getNmastersitecode() + " ORDER BY nsitehierarchyconfigcode DESC";
		return new ResponseEntity<Object>(jdbcTemplate.query(strquery, new ProjectAndSiteHierarchyMapping()), HttpStatus.OK);
		
	}
	/**
	 * This method is used to retrieve list of all available bioproject for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         projectsitehierarchymapping
	 * @throws Exception that are thrown from this DAO layer
	 */
    @Override
	public ResponseEntity<Object> getBioProject(final UserInfo userInfo) throws Exception 
	{
		final String strquery = "SELECT b.nbioprojectcode, b.sprojecttitle, b.nsitecode, b.nstatus " +
	               "FROM bioproject b " +
	               "WHERE b.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	               "AND b.nsitecode = " + userInfo.getNmastersitecode() + " " +
	               "AND NOT EXISTS ( " +
	               "    SELECT 1 FROM projectsitehierarchymapping p " +
	               "    WHERE p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	               "    AND p.nsitecode = " + userInfo.getNmastersitecode() + " " +
	               "    AND p.nbioprojectcode = b.nbioprojectcode " +
	               ") " +
	               "ORDER BY b.nbioprojectcode DESC";

		return new ResponseEntity<Object>(jdbcTemplate.query(strquery, new ProjectAndSiteHierarchyMapping()), HttpStatus.OK);

	}	
	
	
	/**
	 * This method is used to add a new entry to projectsitehierarchymapping table. projectsitehierarchymapping Name is
	 * unique across the database. Need to check for duplicate entry of disease name 
	 * for the specified site before saving into database. * Need to check that
	 * there should be only one default disease for a site.
	 * 
	 * @param objProjectAndSiteHierarchyMapping [Disease] object holding details to be added in disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return saved disease object with status code 200 if saved successfully else
	 *         if the disease already exists, response will be returned as 'Already
	 *         Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */

    
	@Override
	public ResponseEntity<Object> createProjectAndSiteHierarchyMapping(ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping, UserInfo userInfo) throws Exception 
	{
		final ProjectAndSiteHierarchyMapping projectAndSiteHierarchyMappingByName = getBioTitleById(objProjectAndSiteHierarchyMapping, userInfo);

		if(projectAndSiteHierarchyMappingByName==null)
		{
		final String sQuery = "lock table projectsitehierarchymapping " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedDiseaseList = new ArrayList<>();

		final String sequenceNoQuery = "select nsequenceno from seqnoconfigurationmaster where stablename ='projectsitehierarchymapping'";
		int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
		nsequenceNo++;

		final String insertQuery="insert into projectsitehierarchymapping (nprojectsitehierarchymapcode,nbioprojectcode,nsitehierarchyconfigcode,dmodifieddate,nsitecode,nstatus)"
				+ "values(" + nsequenceNo + ", " +objProjectAndSiteHierarchyMapping.getNbioprojectcode()
				 +", " + objProjectAndSiteHierarchyMapping.getNsitehierarchyconfigcode() +", '"
				 + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		jdbcTemplate.execute(insertQuery);

		final String updateQuery = "update seqnoconfigurationmaster set nsequenceno = " + nsequenceNo
				+ " where stablename = 'projectsitehierarchymapping'";
		jdbcTemplate.execute(updateQuery);
		
		objProjectAndSiteHierarchyMapping.setNprojectsitehierarchymapcode(nsequenceNo);
		savedDiseaseList.add(objProjectAndSiteHierarchyMapping);
		multilingualIDList.add("IDS_ADDPROJECTANDSITEHIERARCHYMAPPING");
		
		auditUtilityFunction.fnInsertAuditAction(savedDiseaseList, 1, null, multilingualIDList, userInfo);

		return getProjectAndSiteHierarchyMapping(userInfo);
		}
		else  {
			// Conflict = 409 - Duplicate entry --getSlanguagetypecode
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}
	
	/**
	 * This method is used to retrieve active bioproject object based on the specified nprojectsitehierarchymapCode.
	 * @param nprojectsitehierarchymapCode [int] primary key of projectsitehierarchymapping object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of projectsitehierarchymapping object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ProjectAndSiteHierarchyMapping getBioTitleById(ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping,UserInfo userinfo)throws Exception
	{
	final String strQuery="SELECT nbioprojectcode FROM projectsitehierarchymapping WHERE nbioprojectcode = "
			+ objProjectAndSiteHierarchyMapping.getNbioprojectcode() + " and nstatus = "
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + userinfo.getNmastersitecode();

			return (ProjectAndSiteHierarchyMapping) jdbcUtilityFunction.queryForObject(strQuery, ProjectAndSiteHierarchyMapping.class, jdbcTemplate);

	}
	
	/**
	 * This method is used to retrieve active projectsitehierarchymapping object based on the specified nprojectsitehierarchymapCode.
	 * @param nprojectsitehierarchymapCode [int] primary key of projectsitehierarchymapping object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of projectsitehierarchymapping object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ProjectAndSiteHierarchyMapping getActiveProjectAndSiteHierarchyMappingById(final int nprojectsitehierarchymapcode, UserInfo userInfo) throws Exception {
				final String strQuery="SELECT p.nprojectsitehierarchymapcode ,p.nbioprojectcode,"
						+ " p.nsitecode ,p.nstatus ,b.sprojecttitle ,s.sconfigname "
						+ "FROM projectsitehierarchymapping p, bioproject b ,sitehierarchyconfig s "
						+ "WHERE p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "AND b.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
	                     + "AND p.nbioprojectcode = b.nbioprojectcode " + "AND p.nsitehierarchyconfigcode = s.nsitehierarchyconfigcode "+ "AND p.nprojectsitehierarchymapcode = " + nprojectsitehierarchymapcode; 
	
				return (ProjectAndSiteHierarchyMapping) jdbcUtilityFunction.queryForObject(strQuery, ProjectAndSiteHierarchyMapping.class, jdbcTemplate);
	}


	/**
	 * This method id used to delete an entry in projectsitehierarchymapping table Need to check the
	 * 
	 * @param objDisease [projectsitehierarchymapping] an Object holds the record to be deleted
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return a response entity with list of available projectsitehierarchymapping objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> deleteProjectAndSiteHierarchyMapping(ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping, UserInfo userInfo) throws Exception
	{
		
		final ProjectAndSiteHierarchyMapping projectAndSiteHierarchyMapping =getActiveProjectAndSiteHierarchyMappingById(objProjectAndSiteHierarchyMapping.getNprojectsitehierarchymapcode(), userInfo);
                   if(projectAndSiteHierarchyMapping==null)
                   {

           			return new ResponseEntity<>(
           					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
           							userInfo.getSlanguagefilename()),
           					HttpStatus.EXPECTATION_FAILED);
           		}else
           		{
           			final List<String> multilingualIDList = new ArrayList<>();
    				final List<Object> deletedProjectAndSiteHierarchyMappingList = new ArrayList<>();
    				
    				final String updateQueryString = "update projectsitehierarchymapping set dmodifieddate='"
    						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
    						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprojectsitehierarchymapcode="
    						+ objProjectAndSiteHierarchyMapping.getNprojectsitehierarchymapcode() + ";";
    				
    				jdbcTemplate.execute(updateQueryString);
    				objProjectAndSiteHierarchyMapping.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
    				deletedProjectAndSiteHierarchyMappingList.add(objProjectAndSiteHierarchyMapping);
    				multilingualIDList.add("IDS_DELETEPROJECTANDSITEHIERARCHYMAPPING");

    				auditUtilityFunction.fnInsertAuditAction(deletedProjectAndSiteHierarchyMappingList, 1, null, multilingualIDList, userInfo);

    				return getProjectAndSiteHierarchyMapping(userInfo);
           		}
	}
		
}	


	
	

