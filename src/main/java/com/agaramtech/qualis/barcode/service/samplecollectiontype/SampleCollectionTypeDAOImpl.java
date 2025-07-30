package com.agaramtech.qualis.barcode.service.samplecollectiontype;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.barcode.model.SampleCollectionType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.service.product.ProductDAO;
import com.agaramtech.qualis.project.service.projecttype.ProjectTypeDAO;
import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "samplecollectiontype" table
 * by implementing methods from its interface.
 */

@RequiredArgsConstructor
@Repository
public class SampleCollectionTypeDAOImpl implements SampleCollectionTypeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleCollectionTypeDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectTypeDAO projectTypeDAO;
	private final ProductDAO productDAO;

	/**
	 * This method is used to retrieve list of all active samplecollectiontype for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         samplecollectiontype
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleCollectionType(UserInfo userInfo) throws Exception {
		final String strQuery = "select sct.nsamplecollectiontypecode  ,sct.nprojecttypecode , sct.nproductcode  ,sct.scode,  sct.nsitecode,sct.nstatus, pt.sprojecttypename,p.sproductname,"
				+ " to_char(sct.dmodifieddate, '" + userInfo.getSpgsitedatetime()
				+ "') as smodifieddate from samplecollectiontype sct, projecttype pt,product p"
				+ " where sct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  pt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsamplecollectiontypecode >0  and sct.nsitecode=" + userInfo.getNmastersitecode()
				+ " and sct.nprojecttypecode= pt.nprojecttypecode and sct.nproductcode=p.nproductcode";
		LOGGER.info("getSamplecollectiontype -->" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SampleCollectionType()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active samplecollectiontype object based on
	 * the specified nsamplecollectiontypecode.
	 * 
	 * @param nsamplecollectiontypecode [int] primary key of samplecollectiontype object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         samplecollectiontype object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public SampleCollectionType getActiveSampleCollectionTypeById(int nsamplecollectiontypecode, UserInfo userInfo)
			throws Exception {
		final String strQuery = "select sct.nsamplecollectiontypecode  ,sct.nprojecttypecode , sct.nproductcode  ,sct.scode,  sct.nsitecode,sct.nstatus, pt.sprojecttypename,p.sproductname from samplecollectiontype sct, projecttype pt,product p"
				+ " where sct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  pt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsamplecollectiontypecode  = " + nsamplecollectiontypecode
				+ " and sct.nprojecttypecode= pt.nprojecttypecode and sct.nproductcode=p.nproductcode ";
		return (SampleCollectionType) jdbcUtilityFunction.queryForObject(strQuery, SampleCollectionType.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to samplecollectiontype table. Need to
	 * check for duplicate entry of Sample Type Name and Code for the specified
	 * ProjectType before saving into database.
	 * 
	 * @param objSampleCollectionType [SampleCollectionType] object holding details to
	 *                              be added in samplecollectiontype table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return inserted samplecollectionType object with HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createSampleCollectionType(SampleCollectionType objSampleCollectionType,
			UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedStudyIdentity = new ArrayList<>();
		final ResponseEntity<Object> projectTypeResponse = projectTypeDAO
				.getActiveProjectTypeById(objSampleCollectionType.getNprojecttypecode(), userInfo);
		if (projectTypeResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String projecttype = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(projecttype + " "
					+ commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()).toLowerCase(),
					HttpStatus.CONFLICT);
		} else {
			final Product productResponse = productDAO.getActiveProductById(objSampleCollectionType.getNproductcode(),
					userInfo);
			if (productResponse == null) {
				final String projecttype = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE",
						userInfo.getSlanguagefilename());

				return new ResponseEntity<>(projecttype + " "
						+ commonFunction
								.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
										userInfo.getSlanguagefilename())
								.toLowerCase(),
						HttpStatus.CONFLICT);
			} else {
				final SampleCollectionType sampleCollectionType = getSampleCollectionType(
						objSampleCollectionType.getNprojecttypecode(), objSampleCollectionType.getNproductcode(),
						objSampleCollectionType.getScode(), userInfo.getNmastersitecode(),
						objSampleCollectionType.getNsamplecollectiontypecode());
				if (sampleCollectionType != null && sampleCollectionType.isCodeExists() == false
						&& sampleCollectionType.isSampletypeExists() == false) {
					final String sQuery = " lock  table samplecollectiontype "
							+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
					jdbcTemplate.execute(sQuery);
					final String sequencenoquery = "select nsequenceno from seqnobarcode where stablename ='samplecollectiontype' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
					nsequenceno++;
					String insertquery = "Insert into samplecollectiontype (nsamplecollectiontypecode  ,nprojecttypecode ,nproductcode ,scode ,dmodifieddate,nsitecode,nstatus) "
							+ "values(" + nsequenceno + "," + objSampleCollectionType.getNprojecttypecode() + ","
							+ objSampleCollectionType.getNproductcode() + " ," + "N'"
							+ stringUtilityFunction.replaceQuote(objSampleCollectionType.getScode()) + "'," + "'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " "
							+ userInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);
					String updatequery = "update seqnobarcode set nsequenceno =" + nsequenceno
							+ " where stablename='samplecollectiontype'";
					jdbcTemplate.execute(updatequery);
					objSampleCollectionType.setNsamplecollectiontypecode(nsequenceno);
					savedStudyIdentity.add(objSampleCollectionType);
					multilingualIDList.add("IDS_ADDSAMPLECOLLECTIONTYPE");
					auditUtilityFunction.fnInsertAuditAction(savedStudyIdentity, 1, null, multilingualIDList, userInfo);
					return getSampleCollectionType(userInfo);

				} else {
					String alert = "";
					final boolean isSampleTypeExists = sampleCollectionType.isSampletypeExists();
					final boolean isCodeExists = sampleCollectionType.isCodeExists();
					if (isSampleTypeExists == true && isCodeExists == true) {
						alert = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE", userInfo.getSlanguagefilename())
								+ " and "
								+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
					} else if (isSampleTypeExists == true) {
						alert = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE",
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
	}

	/**
	 * This method is used to update entry in samplecollectiontype table. Need to
	 * validate that the samplecollectiontype object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of sample
	 * type and code for the specified project type before saving into database.
	 * 
	 * @param objSampleCollectionType [SampleCollectionType] object holding details
	 *                                to be updated in samplecollectiontype table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         samplecollectiontype object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSampleCollectionType(SampleCollectionType objSampleCollectionType,
			UserInfo userInfo) throws Exception {
		final SampleCollectionType objsamplecollectiontype = getActiveSampleCollectionTypeById(
				objSampleCollectionType.getNsamplecollectiontypecode(), userInfo);
		final ResponseEntity<Object> projectTypeResponse = projectTypeDAO
				.getActiveProjectTypeById(objSampleCollectionType.getNprojecttypecode(), userInfo);
		if (objsamplecollectiontype == null) {
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
				final Product productResponse = productDAO
						.getActiveProductById(objSampleCollectionType.getNproductcode(), userInfo);
				if (productResponse == null) {
					final String projecttype = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE",
							userInfo.getSlanguagefilename());
					return new ResponseEntity<>(projecttype + " "
							+ commonFunction
									.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
											userInfo.getSlanguagefilename())
									.toLowerCase(),
							HttpStatus.CONFLICT);
				} else {
					final SampleCollectionType sampleCollectionType = getSampleCollectionType(
							objSampleCollectionType.getNprojecttypecode(), objSampleCollectionType.getNproductcode(),
							objSampleCollectionType.getScode(), userInfo.getNmastersitecode(),
							objSampleCollectionType.getNsamplecollectiontypecode());
					if (sampleCollectionType != null && sampleCollectionType.isCodeExists() == false
							&& sampleCollectionType.isSampletypeExists() == false) {
						final String updateQueryString = "update samplecollectiontype set nprojecttypecode="
								+ objSampleCollectionType.getNprojecttypecode() + "," + " nproductcode="
								+ objSampleCollectionType.getNproductcode() + ",scode =N'"
								+ stringUtilityFunction.replaceQuote(objSampleCollectionType.getScode())
								+ "',dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' where nsamplecollectiontypecode   ="
								+ objSampleCollectionType.getNsamplecollectiontypecode();

						jdbcTemplate.execute(updateQueryString);
						final List<String> multilingualIDList = new ArrayList<>();
						multilingualIDList.add("IDS_EDITSAMPLECOLLECTIONTYPE");
						final List<Object> listAfterSave = new ArrayList<>();
						listAfterSave.add(objSampleCollectionType);
						final List<Object> listBeforeSave = new ArrayList<>();
						listBeforeSave.add(objsamplecollectiontype);
						auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
								userInfo);
						return getSampleCollectionType(userInfo);

					} else {
						final boolean isSampletypeExists = sampleCollectionType.isSampletypeExists();
						final boolean isCodeExists = sampleCollectionType.isCodeExists();
						String alert = "";
						if (isSampletypeExists == true && isCodeExists == true) {
							alert = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE",
									userInfo.getSlanguagefilename()) + " and "
									+ commonFunction.getMultilingualMessage("IDS_CODE",
											userInfo.getSlanguagefilename());
						} else if (isSampletypeExists == true) {
							alert = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE",
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
				}
			}
		}
	}

	/**
	 * This method id used to delete an entry in samplecollectiontype table.
	 * 
	 * @param objCollectionTubeType [SampleCollectionType] an Object holds the
	 *                              record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and an
	 *         samplecollectiontype object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteSampleCollectionType(SampleCollectionType objCollectionTubeType,
			UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSampleCollectionType = new ArrayList<>();
		final SampleCollectionType sampleCollectionType = getActiveSampleCollectionTypeById(
				objCollectionTubeType.getNsamplecollectiontypecode(), userInfo);
		boolean validRecord = true;
		if (sampleCollectionType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_SAMPLEPUNCHSITE' as Msg from samplepunchsite where nprojecttypecode= "
					+ objCollectionTubeType.getNsamplecollectiontypecode() + " and " + " nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all  "
					+ "select 'IDS_SAMPLEPROCESSMAPPING' as Msg from sampleprocesstype where  "
					+ "nsamplecollectiontypecode=" + objCollectionTubeType.getNsamplecollectiontypecode() + " "
					+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and nsitecode=" + userInfo.getNmastersitecode() + " union all "
					+ "select 'IDS_ALIQUOTPLAN' as Msg from aliquotplan where  " + "nsamplecollectiontypecode="
					+ objCollectionTubeType.getNsamplecollectiontypecode() + " " + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(sampleCollectionType.getNsamplecollectiontypecode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = "update samplecollectiontype set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where nsamplecollectiontypecode  ="
						+ sampleCollectionType.getNsamplecollectiontypecode();
				jdbcTemplate.execute(updateQueryString);
				objCollectionTubeType.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedSampleCollectionType.add(objCollectionTubeType);
				multilingualIDList.add("IDS_DELETESAMPLECOLLECTIONTYPE");
				auditUtilityFunction.fnInsertAuditAction(savedSampleCollectionType, 1, null, multilingualIDList,
						userInfo);
				return getSampleCollectionType(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to retrieve active samplecollectiontype object based on
	 * the specified projecttype, sampletype, code, nsamplecollectiontypecode and site.
	 * 
	 * @param nprojecttypecode [int] holding the primary key of the project type of the sample collection type.
	 * @param nproductcode [int] holding the primary key of sample type 
	 * @param scode [string] holding the sample collection type code
	 * @param nmastersitecode [int] primary key of site
	 * @param ncollectiontubetypecode [int] primary key of collectiontubetype object
	 * @return response entity object holding response status and data of
	 *         collectiontubetype object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private SampleCollectionType getSampleCollectionType(int projectypecode, int productcode, String scode,
			int nmastersitecode, int nsamplecollectiontypecode) throws Exception {
		String ssamplecollectiontypeQuery = "";
		if (nsamplecollectiontypecode != 0) {
			ssamplecollectiontypeQuery = " and nsamplecollectiontypecode<> " + nsamplecollectiontypecode + "";
		}
		String strQuery = "SELECT"
				+ " CASE WHEN EXISTS (SELECT nproductcode  FROM samplecollectiontype WHERE nproductcode=" + productcode
				+ " and nprojecttypecode=" + projectypecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode=" + nmastersitecode
				+ " " + ssamplecollectiontypeQuery + ")" + " THEN true " + " ELSE false"
				+ " END AS isSampleTypeExists,CASE WHEN EXISTS (SELECT scode  FROM samplecollectiontype WHERE scode=N'"
				+ stringUtilityFunction.replaceQuote(scode) + "' and nprojecttypecode=" + projectypecode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ nmastersitecode + "" + ssamplecollectiontypeQuery + ")" + " THEN true" + " ELSE false"
				+ " END AS isCodeExists";
		return (SampleCollectionType) jdbcUtilityFunction.queryForObject(strQuery, SampleCollectionType.class,
				jdbcTemplate);
	}
}
