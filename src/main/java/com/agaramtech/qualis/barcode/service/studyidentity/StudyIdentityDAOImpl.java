package com.agaramtech.qualis.barcode.service.studyidentity;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.barcode.model.StudyIdentity;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.service.projecttype.ProjectTypeDAO;
import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "StudyIdentity" table by
 * implementing methods from its interface.
 */

@RequiredArgsConstructor
@Repository
public class StudyIdentityDAOImpl implements StudyIdentityDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(StudyIdentityDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectTypeDAO projectTypeDAO;

	/**
	 * This method is used to retrieve list of all active StudyIdentity for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         StudyIdentity
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getStudyIdentity(UserInfo userInfo) throws Exception {
		final String strQuery = "select s.nstudyidentitycode , s.nprojecttypecode , s.sidentificationname ,s.scode,  s.nsitecode,s.nstatus, p.sprojecttypename,"
				+ " to_char(s.dmodifieddate, '" + userInfo.getSpgsitedatetime()
				+ "') as smodifieddate from studyidentity s, projecttype p" + " where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nstudyidentitycode>0  and s.nsitecode=" + userInfo.getNmastersitecode()
				+ " and s.nprojecttypecode= p.nprojecttypecode";
		LOGGER.info("getStudyIdentity:" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new StudyIdentity()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active studyidentity object based on the
	 * specified nstudyidentitycode.
	 * 
	 * @param nstudyidentitycode [int] primary key of studyidentity object
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity object holding response status and data of
	 *         studyidentity object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public StudyIdentity getActiveStudyIdentityById(int nstudyidentitycode, UserInfo userInfo) throws Exception {
		final String strQuery = "select s.nstudyidentitycode , s.nprojecttypecode , s.sidentificationname ,s.scode , s.nsitecode,s.nstatus, p.sprojecttypename from studyidentity s, projecttype p"
				+ " where s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nstudyidentitycode = " + nstudyidentitycode + " and s.nprojecttypecode= p.nprojecttypecode";
		return (StudyIdentity) jdbcUtilityFunction.queryForObject(strQuery, StudyIdentity.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to studyidentity table. Need to check
	 * for duplicate entry of Identification name and Code name for the specified
	 * ProjectType before saving into database.
	 * 
	 * @param objStudyIdentity [Studyidentity] object holding details to be added in
	 *                         StudyIdentity table
	 *@param userInfo [UserInfo] holding logged in user details
	 * @return inserted StudyIdentity object with HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createStudyIdentity(StudyIdentity objStudyIdentity, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedStudyIdentity = new ArrayList<>();
		final ResponseEntity<Object> projectTypeResponse = projectTypeDAO
				.getActiveProjectTypeById(objStudyIdentity.getNprojecttypecode(), userInfo);
		if (projectTypeResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String projecttype = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(projecttype + " "
					+ commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()).toLowerCase(),
					HttpStatus.CONFLICT);
		} else {
			final StudyIdentity existingStudyidentity = getStudyIdentity(objStudyIdentity.getNprojecttypecode(),
					objStudyIdentity.getSidentificationname(), objStudyIdentity.getScode(),
					userInfo.getNmastersitecode(), objStudyIdentity.getNstudyidentitycode());
			if (existingStudyidentity != null && existingStudyidentity.isCodeExists() == false
					&& existingStudyidentity.isStudyidentyNameExists() == false) {
				final String sQuery = " lock  table studyidentity "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(sQuery);
				final String sequencenoquery = "select nsequenceno from seqnobarcode where stablename ='studyidentity' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
				nsequenceno++;
				String insertquery = "Insert into studyidentity (nstudyidentitycode ,nprojecttypecode ,sidentificationname,scode,dmodifieddate,nsitecode,nstatus) "
						+ "values(" + nsequenceno + "," + objStudyIdentity.getNprojecttypecode() + ",N'"
						+ stringUtilityFunction.replaceQuote(objStudyIdentity.getSidentificationname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objStudyIdentity.getScode()) + "'," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " " + userInfo.getNmastersitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);
				String updatequery = "update seqnobarcode set nsequenceno =" + nsequenceno
						+ " where stablename='studyidentity'";
				jdbcTemplate.execute(updatequery);
				objStudyIdentity.setNstudyidentitycode(nsequenceno);
				savedStudyIdentity.add(objStudyIdentity);
				multilingualIDList.add("IDS_ADDSTUDYIDENTITY");
				auditUtilityFunction.fnInsertAuditAction(savedStudyIdentity, 1, null, multilingualIDList, userInfo);
				return getStudyIdentity(userInfo);
			} else {
				String alert = "";
				final boolean isCodeExists = existingStudyidentity.isCodeExists();
				final boolean isStudyidentyNameExists = existingStudyidentity.isStudyidentyNameExists();
				if (isStudyidentyNameExists == true && isCodeExists == true) {
					alert = commonFunction.getMultilingualMessage("IDS_IDENTIFICATIONNAME",
							userInfo.getSlanguagefilename()) + " and "
							+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				} else if (isStudyidentyNameExists == true) {
					alert = commonFunction.getMultilingualMessage("IDS_IDENTIFICATIONNAME",
							userInfo.getSlanguagefilename());
				} else {
					alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " "
						+ commonFunction
								.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
										userInfo.getSlanguagefilename())
								.toLowerCase(),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to update entry in StudyIdentity table. Need to validate
	 * that the StudyIdentity object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of sidentificationname name
	 * and code Name for the specified project type before saving into database.
	 * 
	 * @param objStudyIdentity [studyidentity] object holding details to be updated
	 *                         in StudyIdentity table
	 *@param userInfo [UserInfo] holding logged in user details                         
	 * @return response entity object holding response status and data of updated
	 *         StudyIdentity object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateStudyIdentity(StudyIdentity objStudyIdentity, UserInfo userInfo)
			throws Exception {
		final StudyIdentity objstudyidentity = getActiveStudyIdentityById(objStudyIdentity.getNstudyidentitycode(),
				userInfo);
		final ResponseEntity<Object> projectTypeResponse = projectTypeDAO
				.getActiveProjectTypeById(objStudyIdentity.getNprojecttypecode(), userInfo);
		if (objstudyidentity == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (projectTypeResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
				String projecttype = "";
				projecttype = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE", userInfo.getSlanguagefilename());
				return new ResponseEntity<>(projecttype + " "
						+ commonFunction
								.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
										userInfo.getSlanguagefilename())
								.toLowerCase(),
						HttpStatus.CONFLICT);
			} else {
				final StudyIdentity existingStudyidentity = getStudyIdentity(objStudyIdentity.getNprojecttypecode(),
						objStudyIdentity.getSidentificationname(), objStudyIdentity.getScode(),
						userInfo.getNmastersitecode(), objStudyIdentity.getNstudyidentitycode());
				if (existingStudyidentity != null && existingStudyidentity.isStudyidentyNameExists() == false
						&& existingStudyidentity.isCodeExists() == false) {
					final String updateQueryString = "update studyidentity set sidentificationname='"
							+ stringUtilityFunction.replaceQuote(objStudyIdentity.getSidentificationname()) + "', "
							+ "scode ='" + stringUtilityFunction.replaceQuote(objStudyIdentity.getScode())
							+ "',dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "', nprojecttypecode =" + objStudyIdentity.getNprojecttypecode()
							+ " where nstudyidentitycode  =" + objStudyIdentity.getNstudyidentitycode();
					jdbcTemplate.execute(updateQueryString);
					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add("IDS_EDITSTUDYIDENTITY");
					final List<Object> listAfterSave = new ArrayList<>();
					listAfterSave.add(objStudyIdentity);
					final List<Object> listBeforeSave = new ArrayList<>();
					listBeforeSave.add(objstudyidentity);
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					return getStudyIdentity(userInfo);
				} else {
					final boolean isIdentificationnameExists = existingStudyidentity.isStudyidentyNameExists();
					final boolean iscodenameExists = existingStudyidentity.isCodeExists();
					String alert = "";
					if (isIdentificationnameExists == true && iscodenameExists == true) {
						alert = commonFunction.getMultilingualMessage("IDS_IDENTIFICATIONNAME",
								userInfo.getSlanguagefilename()) + " and "
								+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
					} else if (isIdentificationnameExists == true) {
						alert = commonFunction.getMultilingualMessage("IDS_IDENTIFICATIONNAME",
								userInfo.getSlanguagefilename());
					} else {
						alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
					}
					return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
	}

	/**
	 * This method id used to delete an entry in StudyIdentity table.
	 * 
	 * @param objStudyIdentity [studyidentity] an Object holds the record to be
	 *                         deleted
	 * @param userInfo [UserInfo] holding logged in user details                        
	 * @return a response entity with corresponding HTTP status and an StudyIdentity
	 *         object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteStudyIdentity(StudyIdentity objStudyIdentity, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedStudyIdentity = new ArrayList<>();
		final StudyIdentity studyidentity = getActiveStudyIdentityById(objStudyIdentity.getNstudyidentitycode(),
				userInfo);
		if (studyidentity == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String updateQueryString = "update studyidentity set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where nstudyidentitycode ="
					+ studyidentity.getNstudyidentitycode();
			jdbcTemplate.execute(updateQueryString);
			objStudyIdentity.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			savedStudyIdentity.add(objStudyIdentity);
			multilingualIDList.add("IDS_DELETESTUDYIDENTITY");
			auditUtilityFunction.fnInsertAuditAction(savedStudyIdentity, 1, null, multilingualIDList, userInfo);
			return getStudyIdentity(userInfo);
		}
	}

	/**
	 * This method is used when the time of adding or editing the value at the time this method called to check if the previous value and given new value is same or not.
	 * 
	 * @param nprojectycode for foreignKey 
	 *@param sidentificationname for identificationname
	 *@param scode for get a code for barcodeFormatting 
	 *@param nmastersitecode for masterScreensite code
	 *@param nstudyidentitycode for primary key
	 *                         
	 * @return a response entity with corresponding HTTP status and an StudyIdentity
	 *         object
	 * @exception Exception that are thrown from this DAO layer
	 */	
	private StudyIdentity getStudyIdentity(int nprojectypecode, String sidentificationname, String scode,
			int nmasterSitecode, int nstudyidentitycode) throws Exception {
		String sstudyIdentityQuery = "";
		if (nstudyidentitycode != 0) {
			sstudyIdentityQuery = " and nstudyidentitycode <> " + nstudyidentitycode + "";
		}
		String strQuery = "SELECT"
				+ " CASE WHEN EXISTS (SELECT sidentificationname  FROM studyidentity WHERE sidentificationname=N'"
				+ stringUtilityFunction.replaceQuote(sidentificationname) + "' and nprojecttypecode=" + nprojectypecode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ nmasterSitecode + " " + sstudyIdentityQuery + ")" + " THEN true " + " ELSE false"
				+ " END AS isStudyidentyNameExists,CASE WHEN EXISTS (SELECT scode  FROM studyidentity WHERE scode=N'"
				+ scode + "' and nprojecttypecode=" + nprojectypecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode=" + nmasterSitecode
				+ " " + sstudyIdentityQuery + ")" + " THEN true" + " ELSE false" + " END AS isCodeExists";
		return (StudyIdentity) jdbcUtilityFunction.queryForObject(strQuery, StudyIdentity.class, jdbcTemplate);
	}
}
