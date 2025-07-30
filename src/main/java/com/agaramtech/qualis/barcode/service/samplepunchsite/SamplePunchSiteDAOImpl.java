package com.agaramtech.qualis.barcode.service.samplepunchsite;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.barcode.model.SampleCollectionType;
import com.agaramtech.qualis.barcode.model.SamplePunchSite;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectType;
import com.agaramtech.qualis.project.service.projecttype.ProjectTypeDAO;
import lombok.RequiredArgsConstructor;

/**
 * * This class is used to perform CRUD Operation on "SamplePunchSite" table by
 * implementing methods from its interface.
 * 
 */

@RequiredArgsConstructor
@Repository
public class SamplePunchSiteDAOImpl implements SamplePunchSiteDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamplePunchSiteDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectTypeDAO projectTypeDAO;

	/**
	 * This method is used to retrieve list of all available samplepunchsites for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active samplepunchsites
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSamplePunchSite(UserInfo userInfo) throws Exception {
		final String strQuery = "select sps.nsamplepunchsitecode,sct.nproductcode, p.sproductname,sps.spunchdescription, sps.ncode, sps.nsitecode, sps.nstatus, sps.nprojecttypecode, pt.sprojecttypename"
				+ " from samplepunchsite sps,samplecollectiontype sct, product p,projecttype pt "
				+ " where sps.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sps.nsamplepunchsitecode > 0 and sps.nsitecode = " + userInfo.getNmastersitecode()
				+ " and sps.nprojecttypecode = pt.nprojecttypecode and sps.nsamplecollectiontypecode=sct.nsamplecollectiontypecode and sct.nprojecttypecode=sps.nprojecttypecode and"
				+ " sct.nproductcode =p.nproductcode  and sct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		LOGGER.info("getSamplePunchSite:" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SamplePunchSite()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all available projecttypes for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active projecttypes
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProjecttype(UserInfo userinfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select pt.nprojecttypecode, pt.sprojecttypename from  projecttype pt where pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nprojecttypecode > 0 and pt.nsitecode = " + userinfo.getNmastersitecode();
		outputMap.put("projecttypeList", jdbcTemplate.query(strQuery, new ProjectType()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all available sampletypes for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active sampletypes
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleType(final int nprojecttypecode, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select ct.nsamplecollectiontypecode,ct.nproductcode, p.sproductname from samplecollectiontype ct, product p where ct.nprojecttypecode = "
				+ nprojecttypecode + " and ct.nproductcode = p.nproductcode and ct.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.nsitecode = "
				+ userInfo.getNmastersitecode();
		List<SampleCollectionType> objSampletype = jdbcTemplate.query(strQuery, new SampleCollectionType());
		if (objSampletype.size() > 0) {
			outputMap.put("sampletypeList", objSampletype);
			outputMap.put("sproductname", objSampletype.get(0).getSproductname());
		} else {
			outputMap.put("sampletypeList", "");
			outputMap.put("sproductname", "");
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active samplepunchsite objects for the
	 * specified spunchdescription name and code.
	 * 
	 * @param spunchdescription [String] and code it to be checked the duplicate in
	 *                          the samplepunchsite table.
	 * @param nmasterSiteCode   [int] site code of the samplepunchsite
	 * @return list of active samplepunchsite code(s) based on the specified
	 *         spunchdescription name and code
	 * @throws Exception
	 */
	private SamplePunchSite getExistingRecord(int nsamplepunchsitecode, SamplePunchSite samplePunchSite,
			UserInfo userInfo) throws Exception {
		String samplecode = "";
		if (nsamplepunchsitecode != 0) {
			samplecode = " and nsamplepunchsitecode <> " + samplePunchSite.getNsamplepunchsitecode() + "";
		}
		final String strQuery = "select "
				+ "CASE WHEN EXISTS(select spunchdescription from samplepunchsite where spunchdescription = N'"
				+ stringUtilityFunction.replaceQuote(samplePunchSite.getSpunchdescription()) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojecttypecode = "
				+ samplePunchSite.getNprojecttypecode() + "" + samplecode + ")" + " THEN TRUE" + " ELSE FALSE "
				+ " End AS ispunchdescription, CASE when exists (select ncode from samplepunchsite where ncode = "
				+ samplePunchSite.getNcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojecttypecode = "
				+ samplePunchSite.getNprojecttypecode() + " and nsamplecollectiontypecode="
				+ samplePunchSite.getNsamplecollectiontypecode() + " " + samplecode + ")" + " THEN TRUE "
				+ " ELSE FALSE " + " End AS iscode ";
		return (SamplePunchSite) jdbcUtilityFunction.queryForObject(strQuery, SamplePunchSite.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to SamplePunchSite table. Need to
	 * check for duplicate entry of SamplePunchSite name based on ProjectType and
	 * SampleType from samplecollectiontype for the specified site before saving
	 * into database.
	 * 
	 * @param samplePunchSite [SamplePunchSite] object holding details to be added
	 *                        in SamplePunchSite table
	 * @return inserted SamplePunchSite object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSamplePunchSite(SamplePunchSite samplePunchSite, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSampleCycleList = new ArrayList<>();
		final ResponseEntity<Object> activeProjectType = projectTypeDAO
				.getActiveProjectTypeById(samplePunchSite.getNprojecttypecode(), userInfo);
		if (activeProjectType.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String alert = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final ResponseEntity<Object> activesampletype = getSampleType(samplePunchSite.getNprojecttypecode(), userInfo);
		if (activesampletype != null) {
			final String query = getSampleCollectionType(samplePunchSite, userInfo);
			int nsamplecollectiontypecode = jdbcTemplate.queryForObject(query, Integer.class);
			samplePunchSite.setNsamplecollectiontypecode(nsamplecollectiontypecode);
			final SamplePunchSite punchedSample = getExistingRecord(samplePunchSite.getNsamplepunchsitecode(),
					samplePunchSite, userInfo);
			if (punchedSample != null && punchedSample.isIspunchdescription() == false
					&& punchedSample.isIscode() == false) {
				if (samplePunchSite.getNcode() > 0) {
					String sequencenoquery = "select nsequenceno from seqnobarcode where stablename = 'samplepunchsite' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
					nsequenceno++;
					String insertquery = "Insert into samplepunchsite (nsamplepunchsitecode,nsamplecollectiontypecode,spunchdescription,ncode,nprojecttypecode,dmodifieddate,nsitecode,nstatus)"
							+ "values(" + nsequenceno + "," + samplePunchSite.getNsamplecollectiontypecode() + ",N'"
							+ stringUtilityFunction.replaceQuote(samplePunchSite.getSpunchdescription()) + "',"
							+ samplePunchSite.getNcode() + "," + samplePunchSite.getNprojecttypecode() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " "
							+ userInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);
					String updatequery = "update seqnobarcode set nsequenceno =" + nsequenceno
							+ " where stablename='samplepunchsite'";
					jdbcTemplate.execute(updatequery);
					samplePunchSite.setNsamplepunchsitecode(nsequenceno);
					savedSampleCycleList.add(samplePunchSite);
					multilingualIDList.add("IDS_ADDSAMPLEPUNCHSITE");
					auditUtilityFunction.fnInsertAuditAction(savedSampleCycleList, 1, null, multilingualIDList,
							userInfo);
					return getSamplePunchSite(userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			} else {
				String alert = "";
				final Boolean punchDesc = punchedSample.isIspunchdescription();
				final Boolean punchCode = punchedSample.isIscode();
				if (punchDesc == true && punchCode == true) {
					alert = commonFunction.getMultilingualMessage("IDS_SAMPLEPUNCHSITEDESCRIPTION",
							userInfo.getSlanguagefilename()) + " and "
							+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				} else if (punchDesc == true) {
					alert = commonFunction.getMultilingualMessage("IDS_SAMPLEPUNCHSITEDESCRIPTION",
							userInfo.getSlanguagefilename());
				} else {
					alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
		final String alert = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE", userInfo.getSlanguagefilename());
		return new ResponseEntity<>(
				alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
				HttpStatus.EXPECTATION_FAILED);
	}

	/**
	 * This method is used to retrieve active samplepunchsite object based on the specified nsamplepunchsitecode.
	 * @param nsamplepunchsitecode [int] primary key of samplepunchsite object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of samplepunchsite object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	@Override
	public SamplePunchSite getActiveSamplePunchSiteById(int nsamplepunchsitecode, UserInfo userInfo) throws Exception {
		final String strQuery = "select sps.nsamplepunchsitecode,sps.nprojecttypecode,  p.nproductcode,sps.spunchdescription, sps.ncode, sps.nsitecode, sps.nstatus, pt.sprojecttypename,p.sproductname from samplepunchsite sps, projecttype pt,samplecollectiontype sct, product p"
				+ " where sps.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sct.nsamplecollectiontypecode=sps.nsamplecollectiontypecode and sct.nproductcode= p.nproductcode  and sps.nprojecttypecode=sct.nprojecttypecode and pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplepunchsitecode = "
				+ nsamplepunchsitecode + " and sps.nprojecttypecode = pt.nprojecttypecode  and sct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (SamplePunchSite) jdbcUtilityFunction.queryForObject(strQuery, SamplePunchSite.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in SamplePunchSite table. Need to
	 * validate that the samplePunchSite object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * SamplePunchSite name for the specified site before saving into database.
	 * 
	 * @param samplePunchSite [SamplePunchSite] object holding details to be updated
	 *                        in SamplePunchSite table
	 * @return response entity object holding response status and data of updated
	 *         SamplePunchSite object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSamplePunchSite(SamplePunchSite samplePunchSite, UserInfo userInfo)
			throws Exception {
		final SamplePunchSite objsamplepunch = getActiveSamplePunchSiteById(samplePunchSite.getNsamplepunchsitecode(),
				userInfo);
		final ResponseEntity<Object> activeProjectType = projectTypeDAO
				.getActiveProjectTypeById(samplePunchSite.getNprojecttypecode(), userInfo);
		if (activeProjectType.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String alert = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		if (objsamplepunch == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ResponseEntity<Object> activesampletype = getSampleType(samplePunchSite.getNprojecttypecode(),
					userInfo);
			if (activesampletype != null) {
				if (samplePunchSite.getNcode() > 0) {
					final String query = getSampleCollectionType(samplePunchSite, userInfo);
					int nsamplecollectiontypecode = jdbcTemplate.queryForObject(query, Integer.class);
					samplePunchSite.setNsamplecollectiontypecode(nsamplecollectiontypecode);
					final SamplePunchSite punchedSample = getExistingRecord(samplePunchSite.getNsamplepunchsitecode(),
							samplePunchSite, userInfo);
					if (punchedSample != null && punchedSample.isIspunchdescription() == false
							&& punchedSample.isIscode() == false && samplePunchSite.getNcode() > 0) {

						final String updateQueryString = "update samplepunchsite set spunchdescription='"
								+ stringUtilityFunction.replaceQuote(samplePunchSite.getSpunchdescription()) + "', "
								+ "ncode =" + samplePunchSite.getNcode() + " , nsamplecollectiontypecode ="
								+ samplePunchSite.getNsamplecollectiontypecode() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nprojecttypecode="
								+ samplePunchSite.getNprojecttypecode() + " where nsamplepunchsitecode ="
								+ samplePunchSite.getNsamplepunchsitecode();
						jdbcTemplate.execute(updateQueryString);
						final List<String> multilingualIDList = new ArrayList<>();
						multilingualIDList.add("IDS_EDITSAMPLEPUNCHSITE");
						final List<Object> listAfterSave = new ArrayList<>();
						listAfterSave.add(samplePunchSite);
						final List<Object> listBeforeSave = new ArrayList<>();
						listBeforeSave.add(objsamplepunch);
						auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
								userInfo);
						return getSamplePunchSite(userInfo);

					} else {
						String alert = "";
						final Boolean punchDesc = punchedSample.isIspunchdescription();
						final Boolean punchCode = punchedSample.isIscode();
						if (punchDesc == true && punchCode == true) {
							alert = commonFunction.getMultilingualMessage("IDS_SAMPLEPUNCHSITEDESCRIPTION",
									userInfo.getSlanguagefilename()) + " and "
									+ commonFunction.getMultilingualMessage("IDS_CODE",
											userInfo.getSlanguagefilename());
						} else if (punchDesc == true) {
							alert = commonFunction.getMultilingualMessage("IDS_SAMPLEPUNCHSITEDESCRIPTION",
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
		final String alert = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE", userInfo.getSlanguagefilename());
		return new ResponseEntity<>(
				alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
				HttpStatus.EXPECTATION_FAILED);

	}

	/**
	 * This method id used to delete an entry in samplepunchsite table
	 * Need to check the record is already deleted or not
//	 * Need to check whether the record is used in other tables  such as 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * @param samplePunchSite [SamplePunchSite] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available samplepunchsite objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSamplePunchSite(SamplePunchSite samplePunchSite, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSamplePunchList = new ArrayList<>();
		final SamplePunchSite objsamplepunch = getActiveSamplePunchSiteById(samplePunchSite.getNsamplepunchsitecode(),
				userInfo);
		if (objsamplepunch == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String updateQueryString = "update samplepunchsite set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where nsamplepunchsitecode="
					+ samplePunchSite.getNsamplepunchsitecode();
			jdbcTemplate.execute(updateQueryString);
			samplePunchSite.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			savedSamplePunchList.add(samplePunchSite);
			multilingualIDList.add("IDS_DELETESAMPLEPUNCHSITE");
			auditUtilityFunction.fnInsertAuditAction(savedSamplePunchList, 1, null, multilingualIDList, userInfo);
			return getSamplePunchSite(userInfo);
		}
	}

	/**
	 * This method is used to retrieve list of all available samplecollectiontypes for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active samplecollectiontypes
	 * @throws Exception that are thrown from this DAO layer
	 */
	public String getSampleCollectionType(SamplePunchSite objSamplePunchSite, UserInfo userInfo) {
		String sQuery = " select sct.nsamplecollectiontypecode from samplecollectiontype sct, product p,projecttype pro where "
				+ " sct.nprojecttypecode = " + objSamplePunchSite.getNprojecttypecode()
				+ " and pro.nprojecttypecode=sct.nprojecttypecode and  sct.nproductcode = p.nproductcode "
				+ " and sct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and sct.nproductcode=" + objSamplePunchSite.getNproductcode() + "  " + " and sct.nsitecode = "
				+ userInfo.getNmastersitecode() + "" + " and pro.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return sQuery;
	}
}