package com.agaramtech.qualis.barcode.service.collectiontubetype;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.barcode.model.CollectionTubeType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.project.service.projecttype.ProjectTypeDAO;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "collectiontubetype" table by
 * implementing methods from its interface. 
 */
@AllArgsConstructor
@Repository
public class CollectionTubeTypeDAOImpl implements CollectionTubeTypeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionTubeTypeDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectTypeDAO projectTypeDAO;

	/**
	 * This method is used to retrieve list of all active collectiontubetype for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         collectiontubetype
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCollectionTubeType(UserInfo userInfo) throws Exception {
		final String strQuery = "select c.ncollectiontubetypecode,c.nprojecttypecode, c.stubename, c.ncode,  "
								+ " c.nsitecode,c.nstatus, p.sprojecttypename,"
								+ " to_char(c.dmodifieddate, '" + userInfo.getSpgsitedatetime()
								+ "') as smodifieddate from collectiontubetype c, projecttype p"
								+ " where c.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and p.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and c.ncollectiontubetypecode >0  and c.nsitecode=" + userInfo.getNmastersitecode()
								+ " and p.nsitecode=" + userInfo.getNmastersitecode()
								+ " and c.nprojecttypecode= p.nprojecttypecode";
		LOGGER.info("getCollectionTubeType -->" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new CollectionTubeType()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active collectiontubetype object based on the
	 * specified nstudyidentitycode.
	 * 
	 * @param ncollectiontubetypecode [int] primary key of collectiontubetype object
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity object holding response status and data of
	 *         collectiontubetype object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public CollectionTubeType getActiveCollectionTubeTypeById(int ncollectiontubetypecode, UserInfo userInfo)
			throws Exception {
		final String strQuery = "select c.ncollectiontubetypecode,c.nprojecttypecode, c.stubename,c.ncode, "
								+ " c.nsitecode,c.nstatus, p.sprojecttypename from collectiontubetype c, projecttype p"
								+ " where c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ncollectiontubetypecode  = " + ncollectiontubetypecode
								+ " and c.nsitecode=" + userInfo.getNmastersitecode()
								+ " and p.nsitecode=" + userInfo.getNmastersitecode()
								+ " and c.nprojecttypecode= p.nprojecttypecode";
		return (CollectionTubeType) jdbcUtilityFunction.queryForObject(strQuery, CollectionTubeType.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to collectiontubetype table. Need to
	 * check for duplicate entry of Tube name and Code for the specified ProjectType
	 * before saving into database.
	 * 
	 * @param objCollectionTubeType [CollectionTubeType] object holding details to
	 *                              be added in collectiontubetypes table
	 * @param userInfo [UserInfo] holding logged in user details                             
	 * @return inserted collectiontubetype object with HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createCollectionTubeType(CollectionTubeType objCollectionTubeType, UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedStudyIdentity = new ArrayList<>();

		final ResponseEntity<Object> projectTypeResponse = projectTypeDAO
				.getActiveProjectTypeById(objCollectionTubeType.getNprojecttypecode(), userInfo);

		if (projectTypeResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String projecttype = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());

			return new ResponseEntity<>(projecttype + " "
					+ commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()).toLowerCase(),
					HttpStatus.CONFLICT);
		} else {

			final List<CollectionTubeType> collectiontubetypeList = getCollectionTubeTypeList(
					objCollectionTubeType.getStubename(), objCollectionTubeType.getNcode(),
					objCollectionTubeType.getNprojecttypecode(), userInfo,
					objCollectionTubeType.getNcollectiontubetypecode());

			if (collectiontubetypeList.get(0).isIstubename() == false
					&& collectiontubetypeList.get(0).isIscode() == false) {
				if (objCollectionTubeType.getNcode() > 0) {
					final String sQuery = " lock  table collectiontubetype "
							+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
					jdbcTemplate.execute(sQuery);

					final String sequencenoquery = "select nsequenceno from seqnobarcode where stablename ='collectiontubetype'"
												+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
					nsequenceno++;

					final String insertquery = "Insert into collectiontubetype (ncollectiontubetypecode,nprojecttypecode ,stubename ,"
											+ " ncode,dmodifieddate,nsitecode,nstatus) "
											+ "values(" + nsequenceno + "," + objCollectionTubeType.getNprojecttypecode() + ",N'"
											+ stringUtilityFunction.replaceQuote(objCollectionTubeType.getStubename()) + "',"
											+ objCollectionTubeType.getNcode() + "," + "'"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " "
											+ userInfo.getNmastersitecode() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);

					final String updatequery = "update seqnobarcode set nsequenceno =" + nsequenceno
										+ " where stablename='collectiontubetype'"
										+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();;
					jdbcTemplate.execute(updatequery);

					objCollectionTubeType.setNcollectiontubetypecode(nsequenceno);

					savedStudyIdentity.add(objCollectionTubeType);

					multilingualIDList.add("IDS_ADDCOLLECTIONTUBETYPE");

					auditUtilityFunction.fnInsertAuditAction(savedStudyIdentity, 1, null, multilingualIDList, userInfo);
					return getCollectionTubeType(userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				String alert = "";
				final boolean isTubename = collectiontubetypeList.get(0).isIstubename();
				final boolean isCode = collectiontubetypeList.get(0).isIscode();
				if (isTubename == true && isCode == true) {
					alert = commonFunction.getMultilingualMessage("IDS_TUBENAME", userInfo.getSlanguagefilename())
							+ " and "
							+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				} else if (isTubename == true) {
					alert = commonFunction.getMultilingualMessage("IDS_TUBENAME", userInfo.getSlanguagefilename());
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
	 * This method is used to update entry in collectiontubetype table. Need to
	 * validate that the collectiontubetype object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of tube name
	 * and code for the specified project type before saving into database.
	 * 
	 * @param objCollectionTubeType [CollectionTubeType] object holding details to
	 *                              be updated in collectiontubetype table
	 * @param userInfo [UserInfo] holding logged in user details                             
	 * @return response entity object holding response status and data of updated
	 *         collectiontubetype object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateCollectionTubeType(CollectionTubeType objCollectionTubeType, UserInfo userInfo)
			throws Exception {
		final CollectionTubeType objcollectiontubetype = getActiveCollectionTubeTypeById(
				objCollectionTubeType.getNcollectiontubetypecode(), userInfo);
		
		final ResponseEntity<Object> projectTypeResponse = projectTypeDAO
				.getActiveProjectTypeById(objCollectionTubeType.getNprojecttypecode(), userInfo);

		if (objcollectiontubetype == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (projectTypeResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
				
				final String projecttype = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE", userInfo.getSlanguagefilename());

				return new ResponseEntity<>(projecttype + " "
						+ commonFunction
								.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
										userInfo.getSlanguagefilename())
								.toLowerCase(),
						HttpStatus.CONFLICT);
			} else {

				final List<CollectionTubeType> collectionTubeTypeList = getCollectionTubeTypeList(
						objCollectionTubeType.getStubename(), objCollectionTubeType.getNcode(),
						objCollectionTubeType.getNprojecttypecode(), userInfo,
						objCollectionTubeType.getNcollectiontubetypecode());
				if (objCollectionTubeType.getNcode() > 0) {

					if (collectionTubeTypeList.get(0).isIscode() == false
							&& collectionTubeTypeList.get(0).isIstubename() == false) {
						
						final String updateQueryString = "update collectiontubetype set stubename='"
															+ stringUtilityFunction.replaceQuote(objCollectionTubeType.getStubename()) + "', "
															+ "ncode =" + objCollectionTubeType.getNcode() + ",dmodifieddate='"
															+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nprojecttypecode ="
															+ objCollectionTubeType.getNprojecttypecode() + " where ncollectiontubetypecode   ="
															+ objCollectionTubeType.getNcollectiontubetypecode()
															+ " and nsitecode=" + userInfo.getNmastersitecode();

						jdbcTemplate.execute(updateQueryString);

						final List<String> multilingualIDList = new ArrayList<>();

						multilingualIDList.add("IDS_EDITCOLLECTIONTUBETYPE");
						final List<Object> listAfterSave = new ArrayList<>();
						listAfterSave.add(objCollectionTubeType);

						final List<Object> listBeforeSave = new ArrayList<>();
						listBeforeSave.add(objcollectiontubetype);

						auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
								userInfo);
						return getCollectionTubeType(userInfo);

					} else {

						final boolean isTubename = collectionTubeTypeList.get(0).isIstubename();
						final boolean isCode = collectionTubeTypeList.get(0).isIscode();

						String alert = "";
						if (isTubename == true && isCode == true) {
							alert = commonFunction.getMultilingualMessage("IDS_TUBENAME",
									userInfo.getSlanguagefilename()) + " and "
									+ commonFunction.getMultilingualMessage("IDS_CODE",
											userInfo.getSlanguagefilename());
						} else if (isTubename == true) {
							alert = commonFunction.getMultilingualMessage("IDS_TUBENAME",
									userInfo.getSlanguagefilename());
						} else {
							alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
						}
						return new ResponseEntity<>(alert + " "
								+ commonFunction.getMultilingualMessage(
										Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
										userInfo.getSlanguagefilename()),
								HttpStatus.CONFLICT);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

				}
			}
		}
	}

	/**
	 * This method id used to delete an entry in collectiontubetype table.
	 * 
	 * @param objCollectionTubeType [CollectionTubeType] an Object holds the record
	 *                              to be deleted
	 * @param userInfo [UserInfo] holding logged in user details                             
	 * @return a response entity with corresponding HTTP status and an
	 *         collectiontubetype object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteCollectionTubeType(CollectionTubeType objCollectionTubeType, UserInfo userInfo)
			throws Exception {
		
		final CollectionTubeType collectionTubeType = getActiveCollectionTubeTypeById(
				objCollectionTubeType.getNcollectiontubetypecode(), userInfo);
		if (collectionTubeType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else { // added the screen used the record

			final String recordInProcessType = "select 'IDS_SAMPLEPROCESSMAPPING' as Msg from sampleprocesstype where "
											+ " ncollectiontubetypecode=" + objCollectionTubeType.getNcollectiontubetypecode() + "  "
											+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
											+ " and nsitecode=" + userInfo.getNmastersitecode() 
											+ " union all "
											+ " select 'IDS_ALIQUOTPLAN' as Msg from aliquotplan where  " 
											+ " ncollectiontubetypecode="+ objCollectionTubeType.getNcollectiontubetypecode() 
											+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and nsitecode="	+ userInfo.getNmastersitecode() 
											+ " union all "
											+ " select 'IDS_STORAGESAMPLEPROCESSING' as Msg from storagesampleprocessing where  "
											+ " ncollectiontubetypecode=" + objCollectionTubeType.getNcollectiontubetypecode() + " "
											+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

			valiDatorDel = projectDAOSupport.getTransactionInfo(recordInProcessType, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(objCollectionTubeType.getNcollectiontubetypecode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedStudyIdentity = new ArrayList<>();
				
				final String updateQueryString = "update collectiontubetype set nstatus = "
												+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
												+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where ncollectiontubetypecode  ="
												+ collectionTubeType.getNcollectiontubetypecode()
												+ " and nsitecode="	+ userInfo.getNmastersitecode() ;

				jdbcTemplate.execute(updateQueryString);

				objCollectionTubeType.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedStudyIdentity.add(objCollectionTubeType);

				multilingualIDList.add("IDS_DELETECOLLECTIONTUBETYPE");

				auditUtilityFunction.fnInsertAuditAction(savedStudyIdentity, 1, null, multilingualIDList, userInfo);

			} else {

				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);

			}

			return getCollectionTubeType(userInfo);

		}
	}
	/**
	 * This method id used to update or edit an entry in CollectionTubeType table.
	 * 
	 * @param stubename for get the tubename 
	 * @param userInfo [UserInfo] holding logged in user details  
	 * @param ncode for code for the particular Tube
	 * @param nprojecttypecode for foreignKey
	 * @param userInfo ncollectiontubetypecode for primary key
	 *                         
	 * @return a response entity with corresponding HTTP status and an CollectionTubeType
	 *         object
	 * @exception Exception that are thrown from this DAO layer
	 */	
	private List<CollectionTubeType> getCollectionTubeTypeList(String stubename, int ncode, int nprojecttypecode,
			UserInfo userInfo, int ncollectiontubetypecode) {
		String scollectiontubetype = "";
		if (ncollectiontubetypecode != 0) {
			scollectiontubetype = "and ncollectiontubetypecode<> " + ncollectiontubetypecode + "";
		}

		final String strQuery = "SELECT" + " CASE WHEN EXISTS (SELECT stubename  FROM collectiontubetype WHERE stubename=N'"
							+ stringUtilityFunction.replaceQuote(stubename) + "' and nprojecttypecode=" + nprojecttypecode
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNmastersitecode() + " " + scollectiontubetype + ")" 
							+ " THEN true "
							+ " ELSE false"
							+ " END AS istubename,"
							+ " CASE WHEN EXISTS (SELECT ncode  FROM collectiontubetype WHERE ncode=" + ncode
							+ " and nprojecttypecode=" + nprojecttypecode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNmastersitecode() + " " + scollectiontubetype + ")" 
							+ " THEN true" 
							+ " ELSE false"
							+ " END AS iscode";

		return (List<CollectionTubeType>) jdbcTemplate.query(strQuery, new CollectionTubeType());
	}
}
