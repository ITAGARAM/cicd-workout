package com.agaramtech.qualis.contactmaster.service.manufacturer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.contactmaster.model.Manufacturer;
import com.agaramtech.qualis.contactmaster.model.ManufacturerContactInfo;
import com.agaramtech.qualis.contactmaster.model.ManufacturerFile;
import com.agaramtech.qualis.contactmaster.model.ManufacturerSiteAddress;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "manufacturer" table by
 * implementing methods from its interface.
 * 
 * @author ATE090
 * @version
 * @since 21- nov- 2020
 */
@AllArgsConstructor
@Repository
public class ManufacturerDAOImpl implements ManufacturerDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManufacturerDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	/**
	 * This method is used to retrieve list of all active manufacturer(s) and their
	 * associated details (Manufacturer Site details and Manufacturer Contacts
	 * details) on the specified site through its DAO layer
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         Manufaturer, ManufacturerSite and Contact detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getManufacturer(final UserInfo userInfo, int nmanufcode) throws Exception {

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		List<String> lscolunms = new ArrayList<>();
		List<Object> getAllList = new ArrayList<>();

		final String strQuery = " select a.nmanufcode, a.smanufname, a.sdescription, a.ntransactionstatus, a.nsitecode, a.nstatus,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as stransdisplaystatus"
				+ " from manufacturer a, transactionstatus ts where ts.ntranscode = a.ntransactionstatus "
				+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.nmanufcode > 0  and a.nsitecode = " + userInfo.getNmastersitecode()
				+ " order by a.nmanufcode";
		LOGGER.info("getManufacturer -->" + strQuery);
		List<Manufacturer> lstManufacturerGet = (List<Manufacturer>) jdbcTemplate.query(strQuery, new Manufacturer());
		if (lstManufacturerGet.size() > 0) {
			getAllList.add(lstManufacturerGet);
			if (nmanufcode == 0 && lstManufacturerGet.size() > 0) {
				nmanufcode = lstManufacturerGet.get(lstManufacturerGet.size() - 1).getNmanufcode();
			}
			List<ManufacturerSiteAddress> lstSiteManufacturerGet = getManufacturerSiteDetails(userInfo, nmanufcode);
			getAllList.add(lstSiteManufacturerGet);
			int nManufacturerSiteCode = 0;
			ManufacturerSiteAddress selectedSiteAddress = null;
			if (lstSiteManufacturerGet.size() > 0) {
				nManufacturerSiteCode = lstSiteManufacturerGet.get(lstSiteManufacturerGet.size() - 1)
						.getNmanufsitecode();
				selectedSiteAddress = lstSiteManufacturerGet.get(lstSiteManufacturerGet.size() - 1);
			}
			List<ManufacturerContactInfo> lstContactManufacturerGet = getContactManufacturerBySite(nmanufcode,
					nManufacturerSiteCode, userInfo);
			getAllList.add(lstContactManufacturerGet);
			List<Manufacturer> lstManufacturerByID = new ArrayList<Manufacturer>();
			lstManufacturerByID.add(getManufacturerByIdForInsert(nmanufcode, userInfo));
			getAllList.add(lstManufacturerByID);

			ObjectMapper mapper = new ObjectMapper();
			lscolunms.add("stransdisplaystatus");
			lscolunms.add("stransdisplaystatus,defaultstatus");
			lscolunms.add("sdefaultContact");
			lscolunms.add("stransdisplaystatus");

			final List<Object> finalList = mapper.convertValue(commonFunction.getMultilingualMessageMultiList(
					getAllList, lscolunms, userInfo.getSlanguagefilename(), 2), new TypeReference<List<Object>>() {
					});
			lstManufacturerByID = mapper.convertValue(finalList.get(3), new TypeReference<List<Manufacturer>>() {
			});

			objMap.putAll((Map<String, Object>) getManufacturerFile(nmanufcode, userInfo).getBody());

			objMap.put("Manufacturer", mapper.convertValue(finalList.get(0), new TypeReference<List<Manufacturer>>() {
			}));
			objMap.put("ManufacturerSiteAddress",
					mapper.convertValue(finalList.get(1), new TypeReference<List<ManufacturerSiteAddress>>() {
					}));
			objMap.put("ManufacturerContactInfo",
					mapper.convertValue(finalList.get(2), new TypeReference<List<ManufacturerContactInfo>>() {
					}));
			objMap.put("selectedManufacturer",
					mapper.convertValue(lstManufacturerByID.get(0), new TypeReference<Manufacturer>() {
					}));
			objMap.put("SiteCode", nManufacturerSiteCode);
			objMap.put("selectedSite", selectedSiteAddress);
			return new ResponseEntity<Object>(objMap, HttpStatus.OK);
		} else {
			objMap.put("Manufacturer", new ArrayList<>());
			objMap.put("ManufacturerSiteAddress", new ArrayList<>());
			objMap.put("ManufacturerContactInfo", new ArrayList<>());
			return new ResponseEntity<Object>(objMap, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active manufacturer object based on the
	 * specified nmanufcode through its DAO layer.
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and data of
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	private Manufacturer getManufacturerByIdForInsert(final int nmanufCode, final UserInfo userInfo) throws Exception {
		final String strQuery = " select a.nmanufcode, a.smanufname, a.sdescription, a.ntransactionstatus, a.nsitecode, a.nstatus,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as stransdisplaystatus"
				+ " from manufacturer a,transactionstatus ts where ts.ntranscode = a.ntransactionstatus "
				+ " and a.nstatus = ts.nstatus and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nmanufcode = " + nmanufCode;
		return (Manufacturer) jdbcUtilityFunction.queryForObject(strQuery, Manufacturer.class, jdbcTemplate);
	}

	/**
	 * This method is used to retrieve active manufacturer object based on the
	 * specified nmanufcode through its DAO layer.
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and data of
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getManufacturerById(final int nmanufCode, final UserInfo userInfo) throws Exception {
		final String strQuery = " select a.nmanufcode, a.smanufname, a.sdescription, a.ntransactionstatus, a.nsitecode, a.nstatus,"
				+ "coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'sactiondisplaystatus'->>'en-US') as stransdisplaystatus"
				+ " from manufacturer a,transactionstatus ts where ts.ntranscode = a.ntransactionstatus "
				+ " and a.nstatus = ts.nstatus and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nmanufcode = " + nmanufCode;
		final Manufacturer objManufacturer = (Manufacturer) jdbcUtilityFunction.queryForObject(strQuery,
				Manufacturer.class, jdbcTemplate);
		if (objManufacturer != null) {
			return new ResponseEntity<Object>(objManufacturer, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to retrieve active manufacturersite object based on the
	 * specified nmanufsitecode through its DAO layer.
	 * 
	 * @param nmanufcode     [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @param nmanufSiteCode [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @return response entity object holding response status and data of
	 *         manufacturersite object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ManufacturerSiteAddress getSiteManufacturerById1(final int nmanufCode, final int nmanufSiteCode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = " select a.nmanufsitecode,a.nmanufcode,a.ncountrycode,a.smanufsitename,a.saddress1,"
				+ " a.saddress2,a.saddress3,a.ndefaultstatus,a.nsitecode,a.nstatus, b.scountryname,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as defaultstatus"
				+ " from manufacturersiteaddress a, country b, transactionstatus ts  "
				+ " where ts.ntranscode = a.ndefaultstatus and a.ncountrycode = b.ncountrycode and a.nstatus = b.nstatus and"
				+ " a.nstatus = ts.nstatus and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nmanufcode = " + nmanufCode
				+ " and a.nmanufsitecode = " + nmanufSiteCode;
		ManufacturerSiteAddress manusite = (ManufacturerSiteAddress) jdbcUtilityFunction.queryForObject(strQuery,
				ManufacturerSiteAddress.class, jdbcTemplate);
		return manusite;
	}

	@Override
	public ResponseEntity<Object> getSiteManufacturerById(final int nmanufCode, final int nmanufSiteCode,
			UserInfo userInfo) throws Exception {

		final String strQuery = " select a.nmanufsitecode,a.nmanufcode,a.ncountrycode,a.smanufsitename,"
				+ " a.saddress1,a.saddress2,a.saddress3,a.ndefaultstatus,a.nsitecode,a.nstatus, b.scountryname,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as defaultstatus"
				+ " from manufacturersiteaddress a, country b, transactionstatus ts "
				+ " where ts.ntranscode = a.ndefaultstatus and a.ncountrycode = b.ncountrycode and a.nstatus = b.nstatus and a.nstatus = ts.nstatus and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nmanufcode = " + nmanufCode
				+ " and a.nmanufsitecode = " + nmanufSiteCode;
		ManufacturerSiteAddress manusite = (ManufacturerSiteAddress) jdbcUtilityFunction.queryForObject(strQuery,
				ManufacturerSiteAddress.class, jdbcTemplate);
		if (manusite != null) {
			return new ResponseEntity<Object>(manusite, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	/**
	 * This method is used to retrieve active manufacturercontact object based on
	 * the specified nmanufContactCode through its DAO layer.
	 * 
	 * @param nmanufcode        [int] primary key of site object for which the list
	 *                          is to be fetched
	 * @param nmanufSiteCode    [int] primary key of site object for which the list
	 *                          is to be fetched
	 * @param nmanufContactCode [int] primary key of site object for which the list
	 *                          is to be fetched
	 * @return response entity object holding response status and data of
	 *         manufacturercontact object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ManufacturerContactInfo getContactManufacturerById(final int nmanufCode, final int nmanufSiteCode,
			int nmanufContactCode) throws Exception {
		final String strQuery = " select nmanufcontactcode,nmanufsitecode,nmanufcode,scontactname,sphoneno,smobileno,semail,sfaxno,scomments,"
				+ " ndefaultstatus,nsitecode,nstatus from manufacturercontactinfo where  nmanufsitecode = "
				+ nmanufSiteCode + " and nmanufcontactcode = " + nmanufContactCode + " " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmanufcode = " + nmanufCode;
		return (ManufacturerContactInfo) jdbcUtilityFunction.queryForObject(strQuery, ManufacturerContactInfo.class,
				jdbcTemplate);

	}

	/**
	 * This method is used to retrieve list of all active manufacturer(s) and their
	 * associated details (Manufacturer Site details and Manufacturer Contacts
	 * details) on the specified site through its DAO layer
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         Manufaturer, ManufacturerSite and Contact detail objects
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getManufacturerWithSiteAndContactDetails(final UserInfo userInfo,
			final int nmanufcode) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		List<String> lscolunms = new ArrayList<>();
		List<Object> getAllList = new ArrayList<>();
		List<ManufacturerSiteAddress> lstSiteManufacturerGet = getManufacturerSiteDetails(userInfo, nmanufcode);
		getAllList.add(lstSiteManufacturerGet);
		int nManufacturerSiteCode = 0;
		ManufacturerSiteAddress selectedSiteAddress = null;
		if (lstSiteManufacturerGet.size() > 0) {
			nManufacturerSiteCode = lstSiteManufacturerGet.get(lstSiteManufacturerGet.size() - 1).getNmanufsitecode();
			selectedSiteAddress = lstSiteManufacturerGet.get(lstSiteManufacturerGet.size() - 1);
		}
		List<ManufacturerContactInfo> lstContactManufacturerGet = getContactManufacturerBySite(nmanufcode,
				nManufacturerSiteCode, userInfo);
		getAllList.add(lstContactManufacturerGet);
		List<Manufacturer> lstManufacturerByID = new ArrayList<Manufacturer>();
		Manufacturer checkManufacturerById = getManufacturerByIdForInsert(nmanufcode, userInfo);
		lstManufacturerByID.add(checkManufacturerById);
		getAllList.add(lstManufacturerByID);
		if (checkManufacturerById != null) {
			ObjectMapper mapper = new ObjectMapper();
			lscolunms.add("stransdisplaystatus,defaultstatus");
			lscolunms.add("sdefaultContact");
			lscolunms.add("stransdisplaystatus");
			final List<Object> finalList = mapper.convertValue(commonFunction.getMultilingualMessageMultiList(
					getAllList, lscolunms, userInfo.getSlanguagefilename(), 2), new TypeReference<List<Object>>() {
					});
			lstManufacturerByID = mapper.convertValue(finalList.get(2), new TypeReference<List<Manufacturer>>() {
			});
			objMap.putAll((Map<String, Object>) getManufacturerFile(nmanufcode, userInfo).getBody());
			objMap.put("ManufacturerSiteAddress",
					mapper.convertValue(finalList.get(0), new TypeReference<List<ManufacturerSiteAddress>>() {
					}));
			objMap.put("ManufacturerContactInfo",
					mapper.convertValue(finalList.get(1), new TypeReference<List<ManufacturerContactInfo>>() {
					}));
			objMap.put("selectedManufacturer",
					mapper.convertValue(lstManufacturerByID.get(0), new TypeReference<Manufacturer>() {
					}));
			objMap.put("SiteCode", nManufacturerSiteCode);
			objMap.put("selectedSite", selectedSiteAddress);
			return new ResponseEntity<Object>(objMap, HttpStatus.OK);
		} else {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		}

	}

	/**
	 * This method is used to retrieve list of all active manufacturer site details
	 * through its DAO layer
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         ManufacturerSite details
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public List<ManufacturerSiteAddress> getManufacturerSiteDetails(final UserInfo userInfo, final int nmanufcode)
			throws Exception {
		final String strSiteQuery = " select a.nmanufsitecode,a.nmanufcode,a.ncountrycode,a.smanufsitename,a.saddress1,a.saddress2,"
				+ " a.saddress3,a.ndefaultstatus,a.nsitecode,a.nstatus,b.scountryname,"
				+ "coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'sactiondisplaystatus'->>'en-US') as defaultstatus"
				+ " from manufacturersiteaddress a,country b, transactionstatus ts where ts.ntranscode = a.ndefaultstatus "
				+ " and a.ncountrycode = b.ncountrycode and a.nstatus = b.nstatus and a.nstatus = ts.nstatus and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and a.nmanufcode = "
				+ nmanufcode + " and a.nsitecode =" + userInfo.getNmastersitecode() + " order by a.nmanufsitecode asc";
		List<ManufacturerSiteAddress> lstManufacturerSiteGet = (List<ManufacturerSiteAddress>) jdbcTemplate
				.query(strSiteQuery, new ManufacturerSiteAddress());
		return lstManufacturerSiteGet;
	}

	@Override
	public List<ManufacturerSiteAddress> getManufacturerSiteDetailsById(final UserInfo userInfo, final int nmanufcode,
			final int nmanufsitecode) throws Exception {

		final String strSiteQuery = " select a.nmanufsitecode,a.nmanufcode,a.ncountrycode,a.smanufsitename,"
				+ "a.saddress1,a.saddress2,a.saddress3,a.ndefaultstatus,a.nsitecode,a.nstatus,b.scountryname,"
				+ "coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'sactiondisplaystatus'->>'en-US') as defaultstatus"
				+ " from manufacturersiteaddress a,country b, transactionstatus ts where ts.ntranscode = a.ndefaultstatus "
				+ " and a.ncountrycode = b.ncountrycode and a.nstatus = b.nstatus and a.nstatus = ts.nstatus and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and a.nmanufcode = "
				+ nmanufcode + " and a.nmanufsitecode =" + nmanufsitecode;

		List<ManufacturerSiteAddress> lstManufacturerSiteGet = (List<ManufacturerSiteAddress>) jdbcTemplate
				.query(strSiteQuery, new ManufacturerSiteAddress());
		return lstManufacturerSiteGet;
	}

	/**
	 * This method is used to retrieve list of all active manufacturer contact
	 * details through its DAO layer
	 * 
	 * @param nmanufcode [int] primary key of site object for which the list is to
	 *                   be fetched
	 * @param userInfo   [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         ManufacturerContact details
	 * @throws Exception that are thrown in the DAO layer
	 */

	/**
	 * This method is used to retrieve list of all active manufacturer contact
	 * details
	 * 
	 * @param nmanufcode     [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @param nmanufSiteCode [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @param userInfo       [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         ManufacturerContact details
	 * @throws Exception that are thrown in the DAO layer
	 */
	private ResponseEntity<Object> callContactDetails(final int nmanufcode, final int nmanufSiteCode,
			final int nmanufContactCode, final UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final ManufacturerContactInfo selectedManufacturerContact = getContactManufacturerById(nmanufcode,
				nmanufSiteCode, nmanufContactCode);
		List<ManufacturerContactInfo> lstContactManufacturerGet = getContactManufacturerBySite(nmanufcode,
				nmanufSiteCode, userInfo);
		objMap.put("selectedContact", selectedManufacturerContact);
		objMap.put("ManufacturerContactInfo", lstContactManufacturerGet);
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active manufacturer contact
	 * details based on the site through its DAO layer
	 * 
	 * @param nmanufcode     [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @param nmanufSiteCode [int] primary key of site object for which the list is
	 *                       to be fetched
	 * @param userInfo
	 * @return response entity object holding response status and list of
	 *         ManufacturerContact details
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public List<ManufacturerContactInfo> getContactManufacturerBySite(final int nmanufcode, final int nmanufSiteCode,
			UserInfo userInfo) throws Exception {
		final String strContactQuery = " select a.nmanufcontactcode,a.nmanufsitecode,a.nmanufcode,a.scontactname,a.sphoneno,"
				+ " a.smobileno,a.semail,a.sfaxno,a.scomments,a.ndefaultstatus,a.nsitecode,a.nstatus,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as sdefaultContact"
				+ " from manufacturercontactinfo a, manufacturersiteaddress b, transactionstatus ts where a.ndefaultstatus = ts.ntranscode and  a.nmanufsitecode = b.nmanufsitecode and "
				+ "  a.nstatus = b.nstatus  and a.nstatus = ts.nstatus and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nmanufcode = " + nmanufcode
				+ " and a.nmanufsitecode = " + nmanufSiteCode + " and a.nsitecode = " + userInfo.getNmastersitecode();
		List<ManufacturerContactInfo> lstManufacturerContactGet = (List<ManufacturerContactInfo>) jdbcTemplate
				.query(strContactQuery, new ManufacturerContactInfo());
		return lstManufacturerContactGet;
	}

	/**
	 * This method is used to fetch the active manufacturer objects for the
	 * specified manufacturer name and site.
	 * 
	 * @param manufacturerName [String] manufacturer name for which the records are
	 *                         to be fetched
	 * @param nmasterSiteCode  [int] primary key of site object
	 * @return list of active manufacturer based on the specified manufacturer name
	 *         and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Manufacturer getManufacturerByName(final String manufacturerName, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select  smanufname from manufacturer where smanufname = N'"
				+ stringUtilityFunction.replaceQuote(manufacturerName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (Manufacturer) jdbcUtilityFunction.queryForObject(strQuery, Manufacturer.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active manufacturer site objects for the
	 * specified manufacturer site name and site.
	 * 
	 * @param siteManufacturerName [String] manufacturer site name for which the
	 *                             records are to be fetched
	 * @param nmanufCode           [iint] manufacturer site name for which the
	 *                             records are to be fetched
	 * @return list of active manufacturer site based on the specified manufacturer
	 *         site name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private ManufacturerSiteAddress getSiteManufacturerByName(final String siteManufacturerName, final int nmanufCode,
			final int nmastersitecode) throws Exception {
		final String strQuery = "select  smanufsitename from manufacturersiteaddress where smanufsitename = N'"
				+ stringUtilityFunction.replaceQuote(siteManufacturerName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmanufcode = " + nmanufCode
				+ " and nsitecode = " + nmastersitecode;
		return (ManufacturerSiteAddress) jdbcUtilityFunction.queryForObject(strQuery, ManufacturerSiteAddress.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active manufacturer contact objects for the
	 * specified manufacturer contact name and site.
	 * 
	 * @param contactManufacturerName [String] manufacturer contact name for which
	 *                                the records are to be fetched
	 * @param nmanufCode              [iint] manufacturer contact name for which the
	 *                                records are to be fetched
	 * @param nmanufSiteCode          [int] primary key of site object for which the
	 *                                list is to be fetched
	 * @return list of active manufacturer contact based on the specified
	 *         manufacturer contact name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private ManufacturerContactInfo getContactManufacturerByName(final int nmanufCode,
			final String contactManufacturerName, final int nmanufSiteCode, final int nmastersitecode)
			throws Exception {
		final String strQuery = "select  scontactname from manufacturercontactinfo where scontactname = N'"
				+ stringUtilityFunction.replaceQuote(contactManufacturerName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmanufsitecode =" + nmanufSiteCode
				+ " and nmanufcode = " + nmanufCode + " and nsitecode = " + nmastersitecode;
		return (ManufacturerContactInfo) jdbcUtilityFunction.queryForObject(strQuery, ManufacturerContactInfo.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to manufacturer table through its DAO
	 * layer.
	 * 
	 * @param manufacturer [Manufacturer] object holding details to be added in
	 *                     manufacturer table
	 * @return response entity object holding response status and data of added
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> createManufacturer(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table lockmanufacturer " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final ObjectMapper objmapper = new ObjectMapper();
		Manufacturer manufacturer = objmapper.convertValue(inputMap.get("manufacturer"), Manufacturer.class);
		ManufacturerSiteAddress manufacturerSiteAddress = objmapper
				.convertValue(inputMap.get("manufacturersiteaddress"), ManufacturerSiteAddress.class);
		manufacturerSiteAddress.setIsreadonly(true);
		final Manufacturer maufacturerByName = getManufacturerByName(manufacturer.getSmanufname(),
				manufacturer.getNsitecode());
		if (maufacturerByName == null) {
			String sequencenoquery = "select nsequenceno from seqnocontactmaster where stablename ='manufacturer'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			String sdescription = manufacturer.getSdescription() == null ? "" : manufacturer.getSdescription();
			final String addManufacturer = "Insert into manufacturer (nmanufcode, smanufname, sdescription, ntransactionstatus, dmodifieddate, "
					+ " nsitecode, nstatus) values (" + nsequenceno + ", N'"
					+ stringUtilityFunction.replaceQuote(manufacturer.getSmanufname()) + "', N'"
					+ stringUtilityFunction.replaceQuote(sdescription) + "', " + manufacturer.getNtransactionstatus()
					+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + manufacturer.getNsitecode()
					+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(addManufacturer);
			String updatequery = "update seqnocontactmaster set nsequenceno =" + nsequenceno
					+ " where stablename='manufacturer'";
			jdbcTemplate.execute(updatequery);
			manufacturerSiteAddress.setNmanufcode(nsequenceno);
			manufacturer.setNmanufcode(nsequenceno);
			final List<String> multilingualIDList = new ArrayList<>();
			multilingualIDList.add("IDS_ADDMANUFACTURER");
			final List<Object> savedManufacturerList = new ArrayList<>();
			manufacturer.setNmanufcode(nsequenceno);
			savedManufacturerList.add(manufacturer);
			auditUtilityFunction.fnInsertAuditAction(savedManufacturerList, 1, null, multilingualIDList, userInfo);
			return createSiteAddress(manufacturerSiteAddress, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update entry in manufacturer table through its DAO
	 * layer.
	 * 
	 * @param manufacturer [Manufacturer] object holding details to be added in
	 *                     manufacturer table
	 * @return response entity object holding response status and data of added
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateManufacturer(Manufacturer manufacturer, final UserInfo userInfo)
			throws Exception {
		final Manufacturer manufacturerByID = (Manufacturer) getManufacturerByIdForInsert(manufacturer.getNmanufcode(),
				userInfo);
		if (manufacturerByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nmanufcode from manufacturer where smanufname = N'"
					+ stringUtilityFunction.replaceQuote(manufacturer.getSmanufname()) + "' and nmanufcode <> "
					+ manufacturer.getNmanufcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + "";
			final List<Manufacturer> manufacturerObj = (List<Manufacturer>) jdbcTemplate.query(queryString,
					new Manufacturer());

			if (manufacturerObj.isEmpty()) {
				final String updateQueryString = "update manufacturer set smanufname=N'"
						+ stringUtilityFunction.replaceQuote(manufacturer.getSmanufname()) + "', sdescription =N'"
						+ stringUtilityFunction.replaceQuote(manufacturer.getSdescription()) + "', ntransactionstatus="
						+ manufacturer.getNtransactionstatus() + ", dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmanufcode="
						+ manufacturer.getNmanufcode();

				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITMANUFACTURER");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(manufacturer);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(manufacturerByID);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getManufacturer(userInfo, manufacturer.getNmanufcode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);

			}
		}
	}

	/**
	 * This method is used to delete entry in manufacturer table through its DAO
	 * layer.
	 * 
	 * @param manufacturer [Manufacturer] object holding details to be added in
	 *                     manufacturer table
	 * @return response entity object holding response status and data of added
	 *         manufacturer object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteManufacturer(Manufacturer manufacturer, final UserInfo userInfo)
			throws Exception {

		final Manufacturer manufacturerByID = (Manufacturer) getManufacturerByIdForInsert(manufacturer.getNmanufcode(),
				userInfo);
		if (manufacturerByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select " + "'IDS_INSTRUMENT' as Msg from instrument where nmanufcode = "
					+ manufacturer.getNmanufcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				Map<String, Object> objOneToManyValidation = new HashMap<String, Object>();
				objOneToManyValidation.put("primaryKeyValue", Integer.toString(manufacturer.getNmanufcode()));
				objOneToManyValidation.put("stablename", "manufacturer");
				valiDatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<Manufacturer> deletedManufacturerList = new ArrayList<>();
				final List<Object> AuditActionList = new ArrayList<>();
				List<ManufacturerSiteAddress> lstSiteDetails = new ArrayList<ManufacturerSiteAddress>();
				List<ManufacturerContactInfo> lstContactDetails = new ArrayList<ManufacturerContactInfo>();
				List<ManufacturerFile> lstManufFile = new ArrayList<ManufacturerFile>();
				final List<String> multilingualIDList = new ArrayList<>();

				String strSite = "select nmanufsitecode,nmanufcode,ncountrycode,smanufsitename,saddress1,saddress2,saddress3,ndefaultstatus,nsitecode,"
						+ " nstatus  from manufacturersiteaddress where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmanufcode = "
						+ manufacturer.getNmanufcode() + "";
				lstSiteDetails = (List<ManufacturerSiteAddress>) jdbcTemplate.query(strSite,
						new ManufacturerSiteAddress());

				String strContact = "select nmanufcontactcode,nmanufsitecode,nmanufcode,scontactname,sphoneno,smobileno,semail,sfaxno,"
						+ " scomments,ndefaultstatus,nsitecode,nstatus from manufacturercontactinfo where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmanufcode = "
						+ manufacturer.getNmanufcode() + " ";
				lstContactDetails = (List<ManufacturerContactInfo>) jdbcTemplate.query(strContact,
						new ManufacturerContactInfo());

				final String strFile = "select nmanufacturerfilecode, nmanufcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
						+ " from manufacturerfile where nmanufcode = " + manufacturer.getNmanufcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				lstManufFile = (List<ManufacturerFile>) jdbcTemplate.query(strFile, new ManufacturerFile());
				final String updateQueryString = "update manufacturer set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nmanufcode="
						+ manufacturer.getNmanufcode() + " ; update manufacturersiteaddress set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nmanufcode= "
						+ manufacturer.getNmanufcode() + " ; update manufacturercontactinfo set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nmanufcode= "
						+ manufacturer.getNmanufcode();

				jdbcTemplate.execute(updateQueryString);
				manufacturer.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedManufacturerList.add(manufacturer);
				AuditActionList.add(deletedManufacturerList);
				multilingualIDList.add("IDS_DELETEMANUFACTURER");
				AuditActionList.add(lstSiteDetails);
				multilingualIDList.add("IDS_DELETEMANUFACTURERSITE");
				AuditActionList.add(lstContactDetails);
				multilingualIDList.add("IDS_DELETEMANUFACTURERCONTACT");

				if (!lstManufFile.isEmpty()) {
					List<ManufacturerFile> listDeleteFile = lstManufFile.stream()
							.filter(b -> b.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype())
							.collect(Collectors.toList());
					List<ManufacturerFile> listDeleteLink = lstManufFile.stream()
							.filter(b -> b.getNattachmenttypecode() == Enumeration.AttachmentType.LINK.gettype())
							.collect(Collectors.toList());
					AuditActionList.add(listDeleteFile);
					multilingualIDList.add("IDS_DELETEMANUFACTURERFILE");
					AuditActionList.add(listDeleteLink);
					multilingualIDList.add("IDS_DELETEMANUFACTURERLINK");
				}

				auditUtilityFunction.fnInsertListAuditAction(AuditActionList, 1, null, multilingualIDList, userInfo);
				return getManufacturer(userInfo, 0);

			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to add a new entry to manufacturersiteaddress table
	 * through its DAO layer.
	 * 
	 * @param manufacturerSite [ManufacturerSite] object holding details to be added
	 *                         in manufacturersiteaddress table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSiteAddress(ManufacturerSiteAddress manufacturerSite, final UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table lockmanufacturer " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedManufacturerSiteList = new ArrayList<>();
		final Manufacturer manufacture = (Manufacturer) getManufacturerByIdForInsert(manufacturerSite.getNmanufcode(),
				userInfo);
		if (manufacture == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MANUFACUTRERALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final ManufacturerSiteAddress siteMaufacturerByName = getSiteManufacturerByName(
					manufacturerSite.getSmanufsitename(), manufacturerSite.getNmanufcode(),
					manufacturerSite.getNsitecode());
			if (siteMaufacturerByName == null) {
				if (manufacturerSite.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final ManufacturerSiteAddress defaultManufacturerSite = getManufacturerSiteAddressByDefault(
							manufacturerSite.getNmanufcode(), manufacturerSite.getNsitecode());
					if (defaultManufacturerSite != null) {
						final ManufacturerSiteAddress siteAddressBeforeSave = SerializationUtils
								.clone(defaultManufacturerSite);
						final List<Object> defaultListBeforeSave = new ArrayList<>();
						defaultListBeforeSave.add(siteAddressBeforeSave);
						defaultManufacturerSite
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = " update manufacturersiteaddress set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nmanufsitecode ="
								+ defaultManufacturerSite.getNmanufsitecode();
						jdbcTemplate.execute(updateQueryString);
						final List<Object> defaultListAfterSave = new ArrayList<>();
						defaultListAfterSave.add(defaultManufacturerSite);
						multilingualIDList.add("IDS_EDITMANUFACTURERSITE");
						auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
								multilingualIDList, userInfo);
						multilingualIDList.clear();
					}
				}
				String manufacSiteSeq = "";
				manufacSiteSeq = "select nsequenceno from seqnocontactmaster where stablename='manufacturersiteaddress'";
				int seqNo = jdbcTemplate.queryForObject(manufacSiteSeq, Integer.class);
				seqNo = seqNo + 1;
				String manufacSiteInsert = "";
				String address2 = manufacturerSite.getSaddress2() == null ? "" : manufacturerSite.getSaddress2();
				String address3 = manufacturerSite.getSaddress3() == null ? "" : manufacturerSite.getSaddress3();

				manufacSiteInsert = "insert into manufacturersiteaddress(nmanufsitecode,nmanufcode,ncountrycode,smanufsitename,saddress1,"
						+ "saddress2,saddress3,ndefaultstatus,dmodifieddate,nsitecode,nstatus) values(" + seqNo + ","
						+ manufacturerSite.getNmanufcode() + "," + "" + manufacturerSite.getNcountrycode() + ",'"
						+ stringUtilityFunction.replaceQuote(manufacturerSite.getSmanufsitename()) + "','"
						+ stringUtilityFunction.replaceQuote(manufacturerSite.getSaddress1()) + "'," + "'"
						+ stringUtilityFunction.replaceQuote(address2) + "','" + "a.nsitecode = "
						+ userInfo.getNmastersitecode() + stringUtilityFunction.replaceQuote(address3) + "',"
						+ manufacturerSite.getNdefaultstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(manufacSiteInsert);
				manufacSiteSeq = "update seqnocontactmaster set nsequenceno=" + seqNo
						+ " where stablename='manufacturersiteaddress'";
				jdbcTemplate.execute(manufacSiteSeq);
				multilingualIDList.add("IDS_ADDMANUFACTURERSITE");
				manufacturerSite.setNmanufsitecode(seqNo);
				savedManufacturerSiteList.add(manufacturerSite);
				auditUtilityFunction.fnInsertAuditAction(savedManufacturerSiteList, 1, null, multilingualIDList,
						userInfo);
				if (manufacturerSite.isIsreadonly() != true) {
					return getManufacturer(userInfo, manufacturerSite.getNmanufcode());
				} else {
					final ObjectMapper objmapper = new ObjectMapper();
					Map<String, Object> manufacContact = new HashMap<>();
					manufacContact.put("nmanufcode", manufacturerSite.getNmanufcode());
					manufacContact.put("nmanufsitecode", manufacturerSite.getNmanufsitecode());
					manufacContact.put("scontactname", manufacturerSite.getScontactname());
					manufacContact.put("ndefaultstatus", manufacturerSite.getNdefaultstatus());
					manufacContact.put("isreadonly", manufacturerSite.isIsreadonly());
					final ManufacturerContactInfo manufacturerContactInfo = objmapper.convertValue(manufacContact,
							ManufacturerContactInfo.class);
					manufacturerContactInfo.setNmanufsitecode(seqNo);
					return createContactInfo(manufacturerContactInfo, userInfo);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to get the default record in manufacturersiteaddress
	 * table through its DAO layer.
	 * 
	 * @param nmanuFCode [Manufacturer] object holding details to be added in
	 *                   manufacturersiteaddress table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	private ManufacturerSiteAddress getManufacturerSiteAddressByDefault(final int nmanuFCode, final int nmastersitecode)
			throws Exception {

		final String strQuery = "select nmanufsitecode,nmanufcode,ncountrycode,smanufsitename,saddress1,"
				+ " saddress2,saddress3,ndefaultstatus,nsitecode,nstatus from manufacturersiteaddress a"
				+ " where a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and a.nmanufcode = " + nmanuFCode + " and a.nsitecode = " + nmastersitecode;

		return (ManufacturerSiteAddress) jdbcUtilityFunction.queryForObject(strQuery, ManufacturerSiteAddress.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry in manufacturersiteaddress table through
	 * its DAO layer.
	 * 
	 * @param manufacturerSite [ManufacturerSite] object holding details to be added
	 *                         in manufacturersiteaddress table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSiteAddress(ManufacturerSiteAddress manufacturerSite, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final Manufacturer manufacture = (Manufacturer) getManufacturerByIdForInsert(manufacturerSite.getNmanufcode(),
				userInfo);
		if (manufacture == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MANUFACUTRERALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final ManufacturerSiteAddress siteManufacturerByID = (ManufacturerSiteAddress) getSiteManufacturerById1(
					manufacturerSite.getNmanufcode(), manufacturerSite.getNmanufsitecode(), userInfo);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			if (siteManufacturerByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final String queryString = "select smanufsitename from manufacturersiteaddress where smanufsitename = N'"
						+ stringUtilityFunction.replaceQuote(manufacturerSite.getSmanufsitename())
						+ "' and nmanufcode = " + manufacturerSite.getNmanufcode() + " and nmanufsitecode <> "
						+ manufacturerSite.getNmanufsitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<ManufacturerSiteAddress> siteManufacturerObj = (List<ManufacturerSiteAddress>) jdbcTemplate
						.query(queryString, new ManufacturerSiteAddress());
				if (siteManufacturerByID.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()
						&& siteManufacturerByID.getNdefaultstatus() != manufacturerSite.getNdefaultstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.DEFAULTCANNOTCHANGED.getreturnstatus(),
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				} else {
					if (siteManufacturerObj.isEmpty()) {
						final ManufacturerSiteAddress defaultManufacturerSite = getManufacturerSiteAddressByDefault(
								manufacturerSite.getNmanufcode(), manufacturerSite.getNsitecode());
						if (manufacturerSite.getNdefaultstatus() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
							if (defaultManufacturerSite != null && defaultManufacturerSite
									.getNmanufsitecode() != manufacturerSite.getNmanufsitecode()) {
								final ManufacturerSiteAddress siteAddressBeforeSave = SerializationUtils
										.clone(defaultManufacturerSite);
								listBeforeUpdate.add(siteAddressBeforeSave);
								defaultManufacturerSite.setNdefaultstatus(
										(short) Enumeration.TransactionStatus.NO.gettransactionstatus());
								final String updateQueryString = " update manufacturersiteaddress set ndefaultstatus="
										+ Enumeration.TransactionStatus.NO.gettransactionstatus()
										+ ", dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo)
										+ "' " + " where nmanufsitecode ="
										+ defaultManufacturerSite.getNmanufsitecode();
								jdbcTemplate.execute(updateQueryString);
								listAfterUpdate.add(defaultManufacturerSite);
								multilingualIDList.add("IDS_EDITMANUFACTURERSITE");
							}
						}

						final String updateQueryString = "update manufacturersiteaddress set smanufsitename=N'"
								+ stringUtilityFunction.replaceQuote(manufacturerSite.getSmanufsitename())
								+ "', saddress1 =N'"
								+ stringUtilityFunction.replaceQuote(manufacturerSite.getSaddress1())
								+ "',  saddress2 =N'"
								+ stringUtilityFunction.replaceQuote(manufacturerSite.getSaddress2())
								+ "',saddress3 = N'"
								+ stringUtilityFunction.replaceQuote(manufacturerSite.getSaddress3())
								+ "', nmanufcode= " + manufacturerSite.getNmanufcode() + ",ncountrycode= "
								+ manufacturerSite.getNcountrycode() + ", ndefaultstatus= "
								+ manufacturerSite.getNdefaultstatus() + ", dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nmanufcode= "
								+ manufacturerSite.getNmanufcode() + " and nmanufsitecode="
								+ manufacturerSite.getNmanufsitecode();
						jdbcTemplate.execute(updateQueryString);
						multilingualIDList.add("IDS_EDITMANUFACTURERSITE");
						listAfterUpdate.add(manufacturerSite);
						listBeforeUpdate.add(siteManufacturerByID);
						auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
								multilingualIDList, userInfo);

						final List<ManufacturerContactInfo> lstContactDetails = (List<ManufacturerContactInfo>) getContactManufacturerBySite(
								manufacturerSite.getNmanufcode(), manufacturerSite.getNmanufsitecode(), userInfo);
						final List<ManufacturerSiteAddress> manufacturerSiteAddress = (List<ManufacturerSiteAddress>) getManufacturerSiteDetails(
								userInfo, manufacturerSite.getNmanufcode());
						final List<ManufacturerSiteAddress> lstSiteAddress = (List<ManufacturerSiteAddress>) getManufacturerSiteDetailsById(
								userInfo, manufacturerSite.getNmanufcode(), manufacturerSite.getNmanufsitecode());
						outputMap.put("selectedSite", lstSiteAddress.get(0));
						outputMap.put("ManufacturerContactInfo", lstContactDetails);
						outputMap.put("ManufacturerSiteAddress", manufacturerSiteAddress);

					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				}
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to delete entry in manufacturersiteaddress table through
	 * its DAO layer.
	 * 
	 * @param manufacturerSite [ManufacturerSite] object holding details to be added
	 *                         in manufacturersiteaddress table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override

	public ResponseEntity<Object> deleteSiteAddress(ManufacturerSiteAddress manufacturerSite, final UserInfo userInfo)
			throws Exception {
		final Manufacturer manufacture = (Manufacturer) getManufacturerByIdForInsert(manufacturerSite.getNmanufcode(),
				userInfo);
		if (manufacture == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ManufacturerSiteAddress siteManufacturerByID = (ManufacturerSiteAddress) getSiteManufacturerById1(
					manufacturerSite.getNmanufcode(), manufacturerSite.getNmanufsitecode(), userInfo);
			if (siteManufacturerByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				valiDatorDel = null;
				boolean validRecord = false;
				if (manufacturerSite.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(),
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				} else {
					valiDatorDel = projectDAOSupport
							.validateDeleteRecord(Integer.toString(manufacturerSite.getNmanufsitecode()), userInfo);
					if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
						validRecord = true;
					} else {
						validRecord = false;
					}
				}
				if (validRecord) {
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> savedSiteManufacturerList = new ArrayList<>();
					String updateQueryString = "update manufacturersiteaddress set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmanufcode="
							+ manufacturerSite.getNmanufcode() + " and nmanufsitecode="
							+ manufacturerSite.getNmanufsitecode() + ";";
					manufacturerSite.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
					multilingualIDList.add("IDS_DELETEMANUFACTURERSITE");
					savedSiteManufacturerList.add(Arrays.asList(manufacturerSite));
					final String getContacts = "select nmanufcontactcode,nmanufsitecode,nmanufcode,scontactname,"
							+ " sphoneno,smobileno,semail,sfaxno,scomments,ndefaultstatus,nsitecode,nstatus from manufacturercontactinfo "
							+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nmanufsitecode = " + manufacturerSite.getNmanufsitecode();
					List<ManufacturerContactInfo> deletedContacts = (List<ManufacturerContactInfo>) jdbcTemplate
							.query(getContacts, new ManufacturerContactInfo());
					updateQueryString += " update manufacturercontactinfo set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmanufsitecode = "
							+ manufacturerSite.getNmanufsitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(updateQueryString);
					multilingualIDList.add("IDS_DELETEMANUFACTURERCONTACT");
					savedSiteManufacturerList.add(deletedContacts);
					auditUtilityFunction.fnInsertListAuditAction(savedSiteManufacturerList, 1, null, multilingualIDList,
							userInfo);
					return getManufacturer(userInfo, manufacturerSite.getNmanufcode());
				} else {
					return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
	}

	/**
	 * This method is used to add a new entry to manufacturercontactinfo table
	 * through its DAO layer.
	 * 
	 * @param manufacturerContact [ManufacturercontactInfo] object holding details
	 *                            to be added in manufacturercontactinfo table
	 * @return response entity object holding response status and data of added
	 *         manufacturercontactinfo object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> createContactInfo(ManufacturerContactInfo manufacturerContact,
			final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table lockmanufacturer " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedManufacturerContactInfoList = new ArrayList<>();
		final Manufacturer manufacture = (Manufacturer) getManufacturerByIdForInsert(
				manufacturerContact.getNmanufcode(), userInfo);
		if (manufacture == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MANUFACUTRERALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final ManufacturerSiteAddress siteManufacturerByID = (ManufacturerSiteAddress) getSiteManufacturerById1(
					manufacturerContact.getNmanufcode(), manufacturerContact.getNmanufsitecode(), userInfo);
			if (siteManufacturerByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SITEALREADYDELETE", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final ManufacturerContactInfo contactMaufacturerByName = getContactManufacturerByName(
						manufacturerContact.getNmanufcode(), manufacturerContact.getScontactname(),
						manufacturerContact.getNmanufsitecode(), manufacturerContact.getNsitecode());
				if (contactMaufacturerByName == null) {
					if (manufacturerContact.getNdefaultstatus() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						final ManufacturerContactInfo defaultManufacturerContact = getManufacturerContactInfoByDefault(
								manufacturerContact.getNmanufcode(), manufacturerContact.getNmanufsitecode(),
								manufacturerContact.getNsitecode());
						if (defaultManufacturerContact != null) {
							final ManufacturerContactInfo contactInfoBeforeSave = SerializationUtils
									.clone(defaultManufacturerContact);
							final List<Object> defaultListBeforeSave = new ArrayList<>();
							defaultListBeforeSave.add(contactInfoBeforeSave);
							defaultManufacturerContact
									.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							final String updateQueryString = " update manufacturercontactinfo set ndefaultstatus="
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nmanufcode ="
									+ defaultManufacturerContact.getNmanufcode() + " and nmanufsitecode = "
									+ defaultManufacturerContact.getNmanufsitecode();
							jdbcTemplate.execute(updateQueryString);
							final List<Object> defaultListAfterSave = new ArrayList<>();
							defaultListAfterSave.add(defaultManufacturerContact);
							multilingualIDList.add("IDS_EDITMANUFACTURERCONTACT");
							auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
									multilingualIDList, userInfo);
							multilingualIDList.clear();
						}

					}
					String manufacContactInfoSeq = "";
					manufacContactInfoSeq = "select nsequenceno from seqnocontactmaster where stablename='manufacturercontactinfo'";
					int seqNo = jdbcTemplate.queryForObject(manufacContactInfoSeq, Integer.class);
					seqNo = seqNo + 1;
					String manufacContactInfoInsert = "";
					String phoneno = manufacturerContact.getSphoneno() == null ? "" : manufacturerContact.getSphoneno();
					String mobileno = manufacturerContact.getSmobileno() == null ? ""
							: manufacturerContact.getSmobileno();
					String email = manufacturerContact.getSemail() == null ? "" : manufacturerContact.getSemail();
					String faxno = manufacturerContact.getSfaxno() == null ? "" : manufacturerContact.getSfaxno();
					String comments = manufacturerContact.getScomments() == null ? ""
							: manufacturerContact.getScomments();
					manufacContactInfoInsert = "insert into manufacturercontactinfo(nmanufcontactcode,nmanufsitecode,nmanufcode,scontactname,"
							+ "sphoneno,smobileno,semail,sfaxno,scomments,ndefaultstatus,dmodifieddate,nsitecode,nstatus)values ("
							+ seqNo + "," + manufacturerContact.getNmanufsitecode() + "," + ""
							+ manufacturerContact.getNmanufcode() + ",'"
							+ stringUtilityFunction.replaceQuote(manufacturerContact.getScontactname()) + "','"
							+ stringUtilityFunction.replaceQuote(phoneno) + "'," + "'"
							+ stringUtilityFunction.replaceQuote(mobileno) + "','"
							+ stringUtilityFunction.replaceQuote(email) + "','"
							+ stringUtilityFunction.replaceQuote(faxno) + "','"
							+ stringUtilityFunction.replaceQuote(comments) + "'," + ""
							+ manufacturerContact.getNdefaultstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
							+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(manufacContactInfoInsert);
					manufacContactInfoSeq = "update seqnocontactmaster set nsequenceno=" + seqNo
							+ " where stablename='manufacturercontactinfo'";
					jdbcTemplate.execute(manufacContactInfoSeq);
					multilingualIDList.add("IDS_ADDMANUFACTURERCONTACT");
					manufacturerContact.setNmanufcontactcode(seqNo);
					savedManufacturerContactInfoList.add(manufacturerContact);
					auditUtilityFunction.fnInsertAuditAction(savedManufacturerContactInfoList, 1, null,
							multilingualIDList, userInfo);
					if (manufacturerContact.isIsreadonly() != true) {
						return callContactDetails(manufacturerContact.getNmanufcode(),
								manufacturerContact.getNmanufsitecode(), seqNo, userInfo);
					} else {
						return getManufacturer(userInfo, manufacturerContact.getNmanufcode());
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
	}

	/**
	 * This method is used to get the default record in manufacturercontactinfo
	 * table .
	 * 
	 * @param nmanuFCode     [Manufacturer] object holding details to be added in
	 *                       manufacturercontactinfo table
	 * @param nmanuFSiteCode [ManufacturerSiteAddress] object holding details to be
	 *                       added in manufacturercontactinfo table
	 * @return response entity object holding response status and data of added
	 *         manufacturerSite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	private ManufacturerContactInfo getManufacturerContactInfoByDefault(final int nmanuFCode, final int nmanuFSiteCode,
			final int nmastersitecode) throws Exception {
		final String strQuery = "select a.nmanufcontactcode,a.nmanufsitecode,a.nmanufcode,a.scontactname,a.sphoneno,"
				+ " a.smobileno,a.semail,a.sfaxno,a.scomments,a.ndefaultstatus,a.nsitecode,a.nstatus from manufacturercontactinfo a"
				+ " where a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and a.nmanufsitecode = " + nmanuFSiteCode + " and a.nmanufcode = " + nmanuFCode
				+ " and a.nsitecode = " + nmastersitecode;
		return (ManufacturerContactInfo) jdbcUtilityFunction.queryForObject(strQuery, ManufacturerContactInfo.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry to manufacturercontactinfo table through
	 * its DAO layer.
	 * 
	 * @param manufacturerContact [ManufacturercontactInfo] object holding details
	 *                            to be added in manufacturercontactinfo table
	 * @return response entity object holding response status and data of added
	 *         manufacturercontactinfo object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateContactInfo(ManufacturerContactInfo manufacturerContact, UserInfo userInfo)
			throws Exception {

		final Manufacturer manufacture = (Manufacturer) getManufacturerByIdForInsert(
				manufacturerContact.getNmanufcode(), userInfo);
		if (manufacture == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MANUFACUTRERALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final ManufacturerSiteAddress siteManufacturerByID = (ManufacturerSiteAddress) getSiteManufacturerById1(
					manufacturerContact.getNmanufcode(), manufacturerContact.getNmanufsitecode(), userInfo);
			if (siteManufacturerByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SITEALREADYDELETE", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final ManufacturerContactInfo contactManufacturerByID = (ManufacturerContactInfo) getContactManufacturerById(
						manufacturerContact.getNmanufcode(), manufacturerContact.getNmanufsitecode(),
						manufacturerContact.getNmanufcontactcode());
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();
				final List<String> multilingualIDList = new ArrayList<>();
				if (contactManufacturerByID == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					final String queryString = "select scontactname from manufacturercontactinfo where scontactname = N'"
							+ stringUtilityFunction.replaceQuote(manufacturerContact.getScontactname())
							+ "' and nmanufcode = " + manufacturerContact.getNmanufcode() + " and nmanufsitecode = "
							+ manufacturerContact.getNmanufsitecode() + " and nmanufcontactcode <> "
							+ manufacturerContact.getNmanufcontactcode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<ManufacturerContactInfo> contactManufacturerObj = (List<ManufacturerContactInfo>) jdbcTemplate
							.query(queryString, new ManufacturerContactInfo());
					if (contactManufacturerByID.getNdefaultstatus() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()
							&& contactManufacturerByID.getNdefaultstatus() != manufacturerContact.getNdefaultstatus()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.DEFAULTCANNOTCHANGED.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else {
						if (contactManufacturerObj.isEmpty()) {
							if (manufacturerContact.getNdefaultstatus() == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
								final ManufacturerContactInfo defaultManufacturerContact = getManufacturerContactInfoByDefault(
										manufacturerContact.getNmanufcode(), manufacturerContact.getNmanufsitecode(),
										manufacturerContact.getNsitecode());
								if (defaultManufacturerContact != null && (defaultManufacturerContact
										.getNmanufcontactcode() != manufacturerContact.getNmanufcontactcode())) {
									final ManufacturerContactInfo contactInfoBeforeSave = SerializationUtils
											.clone(defaultManufacturerContact);
									listBeforeUpdate.add(contactInfoBeforeSave);
									defaultManufacturerContact.setNdefaultstatus(
											(short) Enumeration.TransactionStatus.NO.gettransactionstatus());
									final String updateQueryString = " update manufacturercontactinfo set ndefaultstatus="
											+ Enumeration.TransactionStatus.NO.gettransactionstatus()
											+ ", dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo)
											+ "' " + " where nmanufcontactcode ="
											+ defaultManufacturerContact.getNmanufcontactcode();
									jdbcTemplate.execute(updateQueryString);
									listAfterUpdate.add(defaultManufacturerContact);
									multilingualIDList.add("IDS_EDITMANUFACTURERCONTACT");
								}

							}

							final String updateQueryString = "update manufacturercontactinfo set scontactname=N'"
									+ stringUtilityFunction.replaceQuote(manufacturerContact.getScontactname())
									+ "', sphoneno =N'"
									+ stringUtilityFunction.replaceQuote(manufacturerContact.getSphoneno())
									+ "',  smobileno =N'"
									+ stringUtilityFunction.replaceQuote(manufacturerContact.getSmobileno())
									+ "',semail = N'"
									+ stringUtilityFunction.replaceQuote(manufacturerContact.getSemail())
									+ "', sfaxno = N'"
									+ stringUtilityFunction.replaceQuote(manufacturerContact.getSfaxno())
									+ "', scomments = N'"
									+ stringUtilityFunction.replaceQuote(manufacturerContact.getScomments())
									+ "', nmanufcode= " + manufacturerContact.getNmanufcode() + ",nmanufsitecode= "
									+ manufacturerContact.getNmanufsitecode() + ", ndefaultstatus= "
									+ manufacturerContact.getNdefaultstatus() + ", dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
									+ " where nmanufcontactcode=" + manufacturerContact.getNmanufcontactcode();
							jdbcTemplate.execute(updateQueryString);
							multilingualIDList.add("IDS_EDITMANUFACTURERCONTACT");
							listAfterUpdate.add(manufacturerContact);
							listBeforeUpdate.add(contactManufacturerByID);
							auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
									multilingualIDList, userInfo);
						} else {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
									userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
						}
					}
				}
			}
		}
		return callContactDetails(manufacturerContact.getNmanufcode(), manufacturerContact.getNmanufsitecode(),
				manufacturerContact.getNmanufcontactcode(), userInfo);
	}

	/**
	 * This method is used to delete entry to manufacturercontactinfo table through
	 * its DAO layer.
	 * 
	 * @param manufacturerContact [ManufacturercontactInfo] object holding details
	 *                            to be added in manufacturercontactinfo table
	 * @return response entity object holding response status and data of added
	 *         manufacturercontactinfo object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteContactInfo(ManufacturerContactInfo manufacturerContact,
			final UserInfo userInfo) throws Exception {
		final ManufacturerContactInfo contactManufacturerByID = (ManufacturerContactInfo) getContactManufacturerById(
				manufacturerContact.getNmanufcode(), manufacturerContact.getNmanufsitecode(),
				manufacturerContact.getNmanufcontactcode());
		if (contactManufacturerByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (contactManufacturerByID.getNdefaultstatus() == Enumeration.TransactionStatus.YES
					.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(),
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedContactManufacturerList = new ArrayList<>();
				final String updateQueryString = "update manufacturercontactinfo set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nmanufcontactcode="
						+ manufacturerContact.getNmanufcontactcode() + " and nmanufcode="
						+ manufacturerContact.getNmanufcode() + " and nmanufsitecode="
						+ manufacturerContact.getNmanufsitecode();

				jdbcTemplate.execute(updateQueryString);
				manufacturerContact.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedContactManufacturerList.add(manufacturerContact);
				multilingualIDList.add("IDS_DELETEMANUFACTURERCONTACT");
				auditUtilityFunction.fnInsertAuditAction(savedContactManufacturerList, 1, null, multilingualIDList,
						userInfo);
				return callContactDetails(manufacturerContact.getNmanufcode(), manufacturerContact.getNmanufsitecode(),
						manufacturerContact.getNmanufcontactcode(), userInfo);
			}
		}
	}

	/**
	 * This method is used to retrieve list of all active manufacturer and EDQM
	 * Manufacturer details through its DAO layer
	 * 
	 * @param userInfo [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         manufacturer and EDQM Manufacturer details
	 * @throws Exception that are thrown in the DAO layer
	 */

	/**
	 * This method is used to retrieve list of all active manufacturer details
	 * through its DAO layer
	 * 
	 * @param userInfo [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of
	 *         manufacturer details
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> createManufacturerFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {

		final String sTableLockQuery = " lock  table manufacturerfile "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sTableLockQuery);

		final ObjectMapper objMapper = new ObjectMapper();
		final List<ManufacturerFile> lstReqManufacturerFile = objMapper
				.readValue(request.getParameter("manufacturerfile"), new TypeReference<List<ManufacturerFile>>() {
				});

		if (lstReqManufacturerFile != null && lstReqManufacturerFile.size() > 0) {
			final Manufacturer objManufacturer = checkManufacturerIsPresent(
					lstReqManufacturerFile.get(0).getNmanufcode());

			if (objManufacturer != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqManufacturerFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
						.gettype()) {

					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo); // Folder Name -
																									// master
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select nmanufacturerfilecode, nmanufcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
							+ " from manufacturerfile where nmanufcode = "
							+ lstReqManufacturerFile.get(0).getNmanufcode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus();
					final ManufacturerFile lstManufacturerFiles = (ManufacturerFile) jdbcUtilityFunction
							.queryForObject(sQuery, ManufacturerFile.class, jdbcTemplate);

					ManufacturerFile objManufacturerFile = lstReqManufacturerFile.get(0);

//						
					if (lstManufacturerFiles != null) {

						if (objManufacturerFile.getNdefaultstatus() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {

							final ManufacturerFile ManufacturerFileBeforeSave = SerializationUtils
									.clone(lstManufacturerFiles);

							final List<Object> defaultListBeforeSave = new ArrayList<>();
							defaultListBeforeSave.add(ManufacturerFileBeforeSave);

							final String updateQueryString = " update manufacturerfile set ndefaultstatus=" + " "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo)
									+ "' where nmanufacturerfilecode ="
									+ lstManufacturerFiles.getNmanufacturerfilecode();
							jdbcTemplate.execute(updateQueryString);

							lstManufacturerFiles
									.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							final List<Object> defaultListAfterSave = new ArrayList<>();
							defaultListAfterSave.add(lstManufacturerFiles);

							auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
									Arrays.asList("IDS_EDITMANUFACTURERFILE"), objUserInfo);

						}

					} else {
						objManufacturerFile
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					}
					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo)
							.truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
					lstReqManufacturerFile.forEach(objtf -> {
						objtf.setDcreateddate(instantDate);
						if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(noffset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						}

					});

					String sequencequery = "select nsequenceno from seqnocontactmaster where stablename ='manufacturerfile'";
					int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
					nsequenceno++;
					String insertquery = "Insert into manufacturerfile(nmanufacturerfilecode,nmanufcode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,ntzcreateddate,noffsetdcreateddate,ssystemfilename,dmodifieddate,ndefaultstatus,nstatus)"
							+ "values (" + nsequenceno + "," + lstReqManufacturerFile.get(0).getNmanufcode() + ","
							+ lstReqManufacturerFile.get(0).getNlinkcode() + ","
							+ lstReqManufacturerFile.get(0).getNattachmenttypecode() + "," + " N'"
							+ stringUtilityFunction.replaceQuote(lstReqManufacturerFile.get(0).getSfilename()) + "',N'"
							+ stringUtilityFunction.replaceQuote(lstReqManufacturerFile.get(0).getSdescription()) + "',"
							+ lstReqManufacturerFile.get(0).getNfilesize() + "," + " '"
							+ lstReqManufacturerFile.get(0).getDcreateddate() + "'," + objUserInfo.getNtimezonecode()
							+ "," + lstReqManufacturerFile.get(0).getNoffsetdcreateddate() + ",N'"
							+ lstReqManufacturerFile.get(0).getSsystemfilename() + "','"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
							+ lstReqManufacturerFile.get(0).getNdefaultstatus() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);

					String updatequery = "update seqnocontactmaster set nsequenceno =" + nsequenceno
							+ " where stablename ='manufacturerfile'";
					jdbcTemplate.execute(updatequery);

					final List<String> multilingualIDList = new ArrayList<>();

					multilingualIDList.add(
							lstReqManufacturerFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
									.gettype() ? "IDS_ADDMANUFACTURERFILE" : "IDS_ADDMANUFACTURERLINK");
					final List<Object> listObject = new ArrayList<Object>();
					for (int i = 0; i < lstReqManufacturerFile.size(); i++) {
						lstReqManufacturerFile.get(i).setNmanufacturerfilecode(nsequenceno);
					}
					listObject.add(lstReqManufacturerFile);

					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MANUFACTURERALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return (getManufacturerFile(lstReqManufacturerFile.get(0).getNmanufcode(), objUserInfo));
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public Manufacturer checkManufacturerIsPresent(final int nmanufcode) throws Exception {
		String strQuery = "select nmanufcode from manufacturer where nmanufcode = " + nmanufcode + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		Manufacturer objTest = (Manufacturer) jdbcUtilityFunction.queryForObject(strQuery, Manufacturer.class,
				jdbcTemplate);
		return objTest;
	}

	public ResponseEntity<Object> getManufacturerFile(final int nmanufcode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String query = "select mf.noffsetdcreateddate,mf.nmanufacturerfilecode,"
				+ "(select  count(nmanufacturerfilecode) from manufacturerfile where nmanufacturerfilecode>0 and nmanufcode = "
				+ nmanufcode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ") as ncount,mf.sdescription,"
				+ " mf.nmanufacturerfilecode as nprimarycode,mf.sfilename,mf.nmanufcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,mf.ssystemfilename,"
				+ " mf.ndefaultstatus,mf.nattachmenttypecode,"
				+ "coalesce(at.jsondata->'sattachmenttype'->>'"+objUserInfo.getSlanguagetypecode()+"',at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when mf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname, mf.nfilesize, case when mf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(mf.dcreateddate,'"
				+ Enumeration.DatabaseDateFormat.FORMAT_1.getDateFormat() + "'),'-') end  as screateddate, "
				+ " mf.nlinkcode, case when mf.nlinkcode = -1 then mf.nfilesize::varchar(1000) else '-' end sfilesize"
				+ " from manufacturerfile mf,transactionstatus ts,attachmenttype at, linkmaster lm  "
				+ " where at.nattachmenttypecode = mf.nattachmenttypecode and at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and lm.nlinkcode = mf.nlinkcode and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and mf.ndefaultstatus=ts.ntranscode and ts.nstatus=" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +""
				+ " and mf.nmanufcode=" + nmanufcode + " order by mf.nmanufacturerfilecode;";
		final List<ManufacturerFile> manufacturerFileList = (List<ManufacturerFile>) jdbcTemplate.query(query,
				new ManufacturerFile());
		outputMap.put("manufacturerFile",
				dateUtilityFunction.getSiteLocalTimeFromUTC(manufacturerFileList, Arrays.asList("screateddate"),
						Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null, true));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> editManufacturerFile(final ManufacturerFile objManufacturerFile,
			final UserInfo objUserInfo) throws Exception {
		final String sEditQuery = "select mf.nmanufacturerfilecode, mf.nmanufcode, mf.nlinkcode, mf.nattachmenttypecode, mf.sfilename, mf.sdescription, mf.nfilesize,"
				+ " mf.ssystemfilename, mf.ndefaultstatus, lm.jsondata->>'slinkname' as slinkname"
				+ " from manufacturerfile mf, linkmaster lm where lm.nlinkcode = mf.nlinkcode" + " and mf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mf.nmanufacturerfilecode = "
				+ +objManufacturerFile.getNmanufacturerfilecode();
		final ManufacturerFile objTF = (ManufacturerFile) jdbcUtilityFunction.queryForObject(sEditQuery,
				ManufacturerFile.class, jdbcTemplate);
		if (objTF != null) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateManufacturerFile(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final List<ManufacturerFile> lstManufacturerFile = objMapper.readValue(request.getParameter("manufacturerfile"),
				new TypeReference<List<ManufacturerFile>>() {
				});
		if (lstManufacturerFile != null && lstManufacturerFile.size() > 0) {
			final ManufacturerFile objManufacturerFile = lstManufacturerFile.get(0);
			final Manufacturer objManufacturer = checkManufacturerIsPresent(objManufacturerFile.getNmanufcode());

			if (objManufacturer != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objManufacturerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select nmanufacturerfilecode, nmanufcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
							+ " from manufacturerfile where nmanufacturerfilecode = "
							+ objManufacturerFile.getNmanufacturerfilecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final ManufacturerFile objTF = (ManufacturerFile) jdbcUtilityFunction.queryForObject(sQuery,
							ManufacturerFile.class, jdbcTemplate);

					final String sCheckDefaultQuery = "select nmanufacturerfilecode, nmanufcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
							+ " from manufacturerfile where nmanufcode = " + objManufacturerFile.getNmanufcode()
							+ " and nmanufacturerfilecode!=" + objManufacturerFile.getNmanufacturerfilecode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<ManufacturerFile> lstDefManufacturerFiles = (List<ManufacturerFile>) jdbcTemplate
							.query(sCheckDefaultQuery, new ManufacturerFile());

					if (objTF != null) {
						String ssystemfilename = "";
						if (objManufacturerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							ssystemfilename = objManufacturerFile.getSsystemfilename();
						}

						if (lstManufacturerFile.get(0).getNdefaultstatus() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {

							final String sDefaultQuery = "select nmanufacturerfilecode, nmanufcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
									+ " from manufacturerfile where nmanufcode = " + objManufacturerFile.getNmanufcode()
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<ManufacturerFile> lstManufacturerFiles = (List<ManufacturerFile>) jdbcTemplate
									.query(sDefaultQuery, new ManufacturerFile());

							if (!lstManufacturerFiles.isEmpty()) {
								final String updateQueryString = " update manufacturerfile set ndefaultstatus=" + " "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus()
										+ ", dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
										+ "' where nmanufacturerfilecode ="
										+ lstManufacturerFiles.get(0).getNmanufacturerfilecode();
								jdbcTemplate.execute(updateQueryString);
							}
						} else {
							final String sDefaultQuery = "select nmanufacturerfilecode, nmanufcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
									+ " from manufacturerfile where nmanufcode = " + objManufacturerFile.getNmanufcode()
									+ " and nManufacturerfilecode=" + objManufacturerFile.getNmanufacturerfilecode()
									+ "" + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<ManufacturerFile> lstManufacturerFiles = (List<ManufacturerFile>) jdbcTemplate
									.query(sDefaultQuery, new ManufacturerFile());
							if (!lstManufacturerFiles.isEmpty()) {
								final String sEditDefaultQuery = "update manufacturerfile set " + " ndefaultstatus = "
										+ Enumeration.TransactionStatus.YES.gettransactionstatus()
										+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
										+ "' where nmanufacturerfilecode = "
										+ objManufacturerFile.getNmanufacturerfilecode();
								jdbcTemplate.execute(sEditDefaultQuery);
							}
						}
						final String sUpdateQuery = "update manufacturerfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objManufacturerFile.getSfilename()) + "',"
								+ " sdescription=N'"
								+ stringUtilityFunction.replaceQuote(objManufacturerFile.getSdescription())
								+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
								+ objManufacturerFile.getNattachmenttypecode() + ", nlinkcode="
								+ objManufacturerFile.getNlinkcode() + "," + " nfilesize = "
								+ objManufacturerFile.getNfilesize() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', " + "ndefaultstatus = "
								+ objManufacturerFile.getNdefaultstatus() + "" + " where nmanufacturerfilecode = "
								+ objManufacturerFile.getNmanufacturerfilecode();
						objManufacturerFile.setDcreateddate(objTF.getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);

						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList.add(
								objManufacturerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
										? "IDS_EDITMANUFACTURERFILE"
										: "IDS_EDITMANUFACTURERLINK");
						lstOldObject.add(objTF);

						auditUtilityFunction.fnInsertAuditAction(lstManufacturerFile, 2, lstOldObject,
								multilingualIDList, objUserInfo);
						return (getManufacturerFile(objManufacturerFile.getNmanufcode(), objUserInfo));
					} else {
						// status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MANUFACTURERALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteManufacturerFile(ManufacturerFile objManufacturerFile, UserInfo objUserInfo)
			throws Exception {
		final Manufacturer manufacturer = checkManufacturerIsPresent(objManufacturerFile.getNmanufcode());
		if (manufacturer != null) {
			if (objManufacturerFile != null) {
				final String sQuery = "select nmanufacturerfilecode, nmanufcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
						+ " from manufacturerfile where nmanufacturerfilecode = "
						+ objManufacturerFile.getNmanufacturerfilecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final ManufacturerFile objTF = (ManufacturerFile) jdbcUtilityFunction.queryForObject(sQuery,
						ManufacturerFile.class, jdbcTemplate);
				if (objTF != null) {
					if (objManufacturerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					} else {
						objManufacturerFile.setScreateddate(null);
					}

					if (objTF.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						final String sDeleteQuery = "select nmanufacturerfilecode, nmanufcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
								+ " from manufacturerfile where " + " nmanufacturerfilecode !="
								+ objManufacturerFile.getNmanufacturerfilecode() + "" + " and nmanufcode="
								+ objManufacturerFile.getNmanufcode() + "" + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						List<ManufacturerFile> lstManufacturerFiles = (List<ManufacturerFile>) jdbcTemplate
								.query(sDeleteQuery, new ManufacturerFile());
						String sDefaultQuery = "";
						if (lstManufacturerFiles.isEmpty()) {
							sDefaultQuery = " update manufacturerfile set  " + "  dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' ,nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									+ " where nmanufacturerfilecode = "
									+ objManufacturerFile.getNmanufacturerfilecode();
						} else {
							sDefaultQuery = "update manufacturerfile set " + "  dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus()
									+ " where nmanufacturerfilecode = " + lstManufacturerFiles
											.get(lstManufacturerFiles.size() - 1).getNmanufacturerfilecode();
						}
						jdbcTemplate.execute(sDefaultQuery);
					}
					final String sUpdateQuery = "update manufacturerfile set dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ " where nmanufacturerfilecode = " + objManufacturerFile.getNmanufacturerfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList.add(
							objManufacturerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_DELETEMANUFACTURERFILE"
									: "IDS_DELETEMANUFACTURERLINK");
					lstObject.add(objManufacturerFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return getManufacturerFile(objManufacturerFile.getNmanufcode(), objUserInfo);
		} else {
			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MANUFACTURERALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public Map<String, Object> viewAttachedManufacturerFile(ManufacturerFile objManufacturerFile, UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		final Manufacturer objManufacturer = checkManufacturerIsPresent(objManufacturerFile.getNmanufcode());
		if (objManufacturer != null) {
			String sQuery = "select * from manufacturerfile where nmanufacturerfilecode = "
					+ objManufacturerFile.getNmanufacturerfilecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final ManufacturerFile objTF = (ManufacturerFile) jdbcUtilityFunction.queryForObject(sQuery,
					ManufacturerFile.class, jdbcTemplate);
			if (objTF != null) {
				if (objTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(objTF.getSsystemfilename(), -1, objUserInfo, "", "");
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ objTF.getNlinkcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,
							jdbcTemplate);
					map.put("AttachLink", objlinkmaster.getSlinkname() + objTF.getSfilename());
					objManufacturerFile.setScreateddate(null);
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList
						.add(objManufacturerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWMANUFACTURERFILE"
								: "IDS_VIEWMANUFACTURERLINK");
				lstObject.add(objManufacturerFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);

			}
		}
		return map;
	}
}
