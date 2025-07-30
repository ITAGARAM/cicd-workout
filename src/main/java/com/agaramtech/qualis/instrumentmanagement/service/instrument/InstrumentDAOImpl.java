package com.agaramtech.qualis.instrumentmanagement.service.instrument;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.basemaster.service.timezone.TimeZoneDAO;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.contactmaster.model.Manufacturer;
import com.agaramtech.qualis.contactmaster.model.Supplier;
import com.agaramtech.qualis.credential.model.Users;
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
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCalibration;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCategory;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentFile;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentLocation;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentMaintenance;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentName;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentSection;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentValidation;
import com.agaramtech.qualis.instrumentmanagement.model.SeqNoInstrumentManagement;
import com.agaramtech.qualis.instrumentmanagement.service.instrumentcategory.InstrumentCategoryDAO;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.organization.model.SectionUsers;
import com.agaramtech.qualis.scheduler.model.SchedulerSampleDetail;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageStructure;
import com.agaramtech.qualis.storagemanagement.model.StorageCategory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "instrument and instrument
 * related" table by implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class InstrumentDAOImpl implements InstrumentDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final InstrumentCategoryDAO instrumentCategoryDAO;
	private final TimeZoneDAO objTimeZoneDAO;

	/**
	 * This method is used to retrieve list of all active instrument for the
	 * specified site.
	 * 
	 * @param userInfo object for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         instrument
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getInstrument(final Integer ninstCode, final UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<>();
		Map<String, Object> instrumentCatValue = new HashMap<>();
		Instrument selectedInstrument = null;
		if (ninstCode == null) {

			String strQuery = "select * from instrumentcategory where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ninstrumentcatcode<>"
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + " order by 1 asc";

			List<InstrumentCategory> lstInsCat = (List<InstrumentCategory>) jdbcTemplate.query(strQuery,
					new InstrumentCategory());
			// Exception thrown in console when the instrument category not available
			if (lstInsCat.isEmpty()) {
				return null;
			}
			instrumentCatValue.put("label", lstInsCat.get(lstInsCat.size() - 1).getSinstrumentcatname());
			instrumentCatValue.put("value", lstInsCat.get(lstInsCat.size() - 1).getNinstrumentcatcode());
			instrumentCatValue.put("item", lstInsCat.get(lstInsCat.size() - 1));

			objMap.put("filterInstrumentCategory", lstInsCat);
			objMap.put("defaultInstrumentCatValue", instrumentCatValue);

			if (!lstInsCat.isEmpty()) {

				int instrumentCatCode = -1;
				List<InstrumentCategory> defaultInstrumentCategory = lstInsCat.stream()
						.filter(x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
						.collect(Collectors.toList());

				if (defaultInstrumentCategory.size() > 0) {

					objMap.put("SelectedInsCat", defaultInstrumentCategory.get(0));
					instrumentCatCode = defaultInstrumentCategory.get(0).getNinstrumentcatcode();

				} else {
					objMap.put("SelectedInsCat", lstInsCat.get(lstInsCat.size() - 1));
					instrumentCatCode = lstInsCat.get(lstInsCat.size() - 1).getNinstrumentcatcode();
				}
				// ALPD-4517-Vignesh R(26-07-2024)-- Loader issue occurs.

//				 strQuery="SELECT i.ninstrumentcode, inn.*, COALESCE(ts2.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"', ts2.jsondata->'stransdisplaystatus'->>'en-US') AS sdefaultstatus, "
//			    +" CASE i.ninstrumentstatus WHEN -1 THEN '-' ELSE COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"', ts.jsondata->'stransdisplaystatus'->>'en-US') END AS sactivestatus, " 
//			    +" i.ninstrumentstatus,il.sinstrumentlocationname,il.ninstrumentlocationcode,ts.ntranscode, "
//			    +" ic.sinstrumentcatname,mf.smanufname,s.ssuppliername,s1.ssuppliername AS sserviceby,"
//			    +" si.ssitename AS sregionalsitename, "    
//			    +" CONCAT(u.sfirstname, ' ' ,u.slastname) AS susername, "
//			    +" COALESCE(p2.jsondata->'speriodname'->>'"+userInfo.getSlanguagetypecode()+"', p2.jsondata->'speriodname'->>'en-US')  AS swindowsperiodplusunit, "
//			    +" COALESCE(p1.jsondata->'speriodname'->>'"+userInfo.getSlanguagetypecode()+"', p1.jsondata->'speriodname'->>'en-US')  AS swindowsperiodminusunit,"
//			    +" COALESCE(p3.jsondata->'speriodname'->>'"+userInfo.getSlanguagetypecode()+"', p3.jsondata->'speriodname'->>'en-US')  AS snextcalibrationperiod, "
//			    +" tz1.stimezoneid AS stzmanufdate,tz2.stimezoneid AS stzpodate,ic.ncalibrationreq,"    
//			    +" tz3.stimezoneid AS stzreceivedate,tz4.stimezoneid AS stzinstallationdate,tz5.stimezoneid AS stzexpirydate, "
//			    +" tz6.stimezoneid AS stzservicedate,tz1.ntimezonecode AS ntzmanufdatecode, "
//			    +" tz2.ntimezonecode AS ntzpodatecode, tz3.ntimezonecode AS ntzreceivedatecode, "  
//			    +" tz4.ntimezonecode AS ntzinstallationdatecode,tz5.ntimezonecode AS ntzexpirydatecode,"
//			    +" tz6.ntimezonecode  AS ntzservicedate ,"    
//			    +" COALESCE(TO_CHAR(i.dreceiveddate,'"+userInfo.getSpgsitedatetime()+"'),'') AS sreceiveddate,"    
//			    +" COALESCE(TO_CHAR(i.dexpirydate,'"+userInfo.getSpgsitedatetime()+"'),'') AS sexpirydate,  "  
//			    +" COALESCE(TO_CHAR(i.dmanufacdate,'"+userInfo.getSpgsitedatetime()+"'),'') AS smanufacdate,"
//			    +" COALESCE(TO_CHAR(i.dpodate,'"+userInfo.getSpgsitedatetime()+"'),'') AS spodate,"
//			    +" COALESCE(TO_CHAR(i.dinstallationdate,'"+userInfo.getSpgsitedatetime()+"'),'') AS sinstallationdate,"
//			    +" COALESCE(TO_CHAR(i.dservicedate,'"+userInfo.getSpgsitedatetime()+"'),'') AS sservicedate, " 
//			    +" ic.ncalibrationreq,"
//			    +" i.nnextcalibration,"
//			    +" i.nnextcalibrationperiod  FROM  instrument i "
//			    +" JOIN instrumentname inn ON inn.ninstrumentnamecode = i.ninstrumentnamecode "
//			    +" JOIN transactionstatus ts ON ts.ntranscode = i.ninstrumentstatus "
//			    +" JOIN transactionstatus ts2 ON ts2.ntranscode = i.ndefaultstatus "
//			    +" JOIN instrumentcategory ic ON ic.ninstrumentcatcode = i.ninstrumentcatcode "
//			    +" JOIN manufacturer mf ON mf.nmanufcode = i.nmanufcode "
//			    +" JOIN supplier s ON s.nsuppliercode = i.nsuppliercode "
//			    +" JOIN supplier s1 ON s1.nsuppliercode = i.nservicecode "
//			    +" JOIN users u ON u.nusercode = i.nusercode "
//			    +" JOIN site si ON si.nsitecode = i.nregionalsitecode "
//			    +" JOIN instrumentlocation il ON il.ninstrumentlocationcode = i.ninstrumentlocationcode "
//			    +" JOIN period p1 ON p1.nperiodcode = i.nwindowsperiodminusunit "
//			    +" JOIN period p2 ON p2.nperiodcode = i.nwindowsperiodplusunit "
//			    +" JOIN period p3 ON p3.nperiodcode = i.nnextcalibrationperiod "
//			    +" JOIN timezone tz1 ON tz1.ntimezonecode = i.ntzmanufdate "
//			    +" JOIN timezone tz2 ON tz2.ntimezonecode = i.ntzpodate "
//			    +" JOIN timezone tz3 ON tz3.ntimezonecode = i.ntzreceivedate "
//			    +" JOIN timezone tz4 ON tz4.ntimezonecode = i.ntzinstallationdate "
//			    +" JOIN timezone tz5 ON tz5.ntimezonecode = i.ntzexpirydate "
//			    +" JOIN timezone tz6 ON tz6.ntimezonecode = i.ntzservicedate  "
//			    +" WHERE i.ninstrumentcode > 0 "
//			    +" AND ic.ninstrumentcatcode = "+instrumentCatCode+" "
//			    +" AND i.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""  
//			    +" AND ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"" 
//			    +" AND ic.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			    +" AND il.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			    +" AND mf.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			    +" AND s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			    +" AND p1.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			    +" AND p2.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			    +" AND p3.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			    +" AND tz1.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"" 
//			    +" AND tz2.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"" 
//			    +" AND tz3.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			    +" AND tz4.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			    +" AND tz5.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"" 
//			    +" AND tz6.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//			    +" AND u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ORDER BY i.ninstrumentcode";

//ALPD-5026 Saravanan - Instrument --> When initially navigate to the instrument screen some of the data is not displaying. Check Description.

				strQuery = "SELECT  i.*,inn.*," + " COALESCE(ts2.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "', "
						+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') AS sdefaultstatus, "
						+ " CASE WHEN i.ninstrumentstatus = -1 THEN '-' ELSE COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "', "
						+ " ts.jsondata->'stransdisplaystatus'->>'en-US') END AS sactivestatus, ts.ntranscode, "
						+ " ic.sinstrumentcatname, il.sinstrumentlocationname,mf.smanufname, "
						+ " s.ssuppliername,  s1.ssuppliername AS sserviceby,  si.ssitename AS sregionalsitename, "
						+ " COALESCE(p1.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode() + "', "
						+ " p1.jsondata->'speriodname'->>'en-US') AS swindowsperiodminusunit, "
						+ " COALESCE(p3.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
						+ "',  p3.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
						+ "') AS snextcalibrationperiod, " + " COALESCE(p4.jsondata->'speriodname'->>'"   //	//added by sujatha ALPDJ21-21 
						+ userInfo.getSlanguagetypecode() + "',  p4.jsondata->'speriodname'->>'"
						+ userInfo.getSlanguagetypecode() + "') AS snextmaintenanceperiod, "
						+ " CONCAT(u.sfirstname, ' ', u.slastname) AS susername, "
						+ " COALESCE(p2.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode() + "', "
						+ " p2.jsondata->'speriodname'->>'en-US') AS swindowsperiodplusunit, "
						+ " ic.ncalibrationreq,tz1.stimezoneid AS stzmanufdate, tz2.stimezoneid AS stzpodate, "
						+ " tz3.stimezoneid AS stzreceivedate, tz4.stimezoneid AS stzinstallationdate, "
						+ " tz5.stimezoneid AS stzexpirydate,tz6.stimezoneid AS stzservicedate, "
						+ " tz1.ntimezonecode AS ntzmanufdatecode,   tz2.ntimezonecode AS ntzpodatecode, "
						+ " tz3.ntimezonecode AS ntzreceivedatecode, "
						+ " tz4.ntimezonecode AS ntzinstallationdatecode,  tz5.ntimezonecode AS ntzexpirydatecode, "
						+ " tz6.ntimezonecode AS ntzservicedate, " + " COALESCE(TO_CHAR(i.dreceiveddate, '"
						+ userInfo.getSpgsitedatetime() + "'), '') AS sreceiveddate, "
						+ " COALESCE(TO_CHAR(i.dexpirydate, '" + userInfo.getSpgsitedatetime()
						+ "'), '') AS sexpirydate, " + " COALESCE(TO_CHAR(i.dmanufacdate, '"
						+ userInfo.getSpgsitedatetime() + "'), '') AS smanufacdate, "
						+ " COALESCE(TO_CHAR(i.dservicedate, '" + userInfo.getSpgsitedatetime()
						+ "'), '') AS sservicedate, " + " COALESCE(TO_CHAR(i.dpodate, '" + userInfo.getSpgsitedatetime()
						+ "'), '') AS spodate, " + " COALESCE(TO_CHAR(i.dinstallationdate, '"
						+ userInfo.getSpgsitedatetime() + "'), '') AS sinstallationdate, "
						+ " COALESCE(TO_CHAR(i.dservicedate,'" + userInfo.getSpgsitedatetime()
						+ "'),'') AS sservicedate, " + " ic.ncalibrationreq," + " i.nnextcalibration,"
						+ " i.nnextcalibrationperiod ," + "i.nnextmaintenance," + " i.nnextmaintenanceperiod,"  //	//added by sujatha ALPDJ21-21 
						+ "i.nautocalibration,COALESCE(ts3.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "', ts3.jsondata->'stransdisplaystatus'->>'en-US') AS sautocalibration " // Added by sonia on
																									// 27th Sept 2024
																									// for Jira
																									// idL:ALPD-4939
						+ " FROM instrument i"
						// Added by neeraj Kumar --ALPD-5237 Instrument category dropdown not loading
						// initial get problem.
						// +" JOIN instrumentname inn ON inn.ninstrumentnamecode = i.ninstrumentnamecode
						+ " JOIN transactionstatus ts ON ts.ntranscode = i.ninstrumentstatus "
						+ " JOIN transactionstatus ts2 ON ts2.ntranscode = i.ndefaultstatus "
						+ " JOIN instrumentcategory ic ON ic.ninstrumentcatcode = i.ninstrumentcatcode"
						+ " JOIN manufacturer mf ON mf.nmanufcode = i.nmanufcode"
						+ " JOIN supplier s ON s.nsuppliercode = i.nsuppliercode "
						+ " JOIN supplier s1 ON s1.nsuppliercode = i.nservicecode "
						+ " JOIN users u ON u.nusercode = i.nusercode "
						+ " JOIN site si ON si.nsitecode = i.nregionalsitecode "
						+ " JOIN instrumentlocation il ON il.ninstrumentlocationcode = i.ninstrumentlocationcode "
						+ " JOIN period p1 ON p1.nperiodcode = i.nwindowsperiodminusunit "
						+ " JOIN period p2 ON p2.nperiodcode = i.nwindowsperiodplusunit "
						+ " JOIN period p3 ON p3.nperiodcode = i.nnextcalibrationperiod "
						+ " JOIN period p4 ON p4.nperiodcode = i.nnextmaintenanceperiod "  	//added by sujatha ALPDJ21-21 
						+ " JOIN instrumentname inn ON inn.ninstrumentnamecode = i.ninstrumentnamecode "
						+ " JOIN timezone tz1 ON tz1.ntimezonecode = i.ntzmanufdate "
						+ " JOIN timezone tz2 ON tz2.ntimezonecode = i.ntzpodate "
						+ " JOIN timezone tz3 ON tz3.ntimezonecode = i.ntzreceivedate "
						+ " JOIN timezone tz4 ON tz4.ntimezonecode = i.ntzinstallationdate "
						+ " JOIN timezone tz5 ON tz5.ntimezonecode = i.ntzexpirydate "
						+ " JOIN timezone tz6 ON tz6.ntimezonecode = i.ntzservicedate "
						+ " JOIN transactionstatus ts3 ON ts3.ntranscode =i.nautocalibration " // Added by sonia on 27th
																								// Sept 2024 for Jira
																								// idL:ALPD-4939
						+ " WHERE i.ninstrumentcode > 0 " + " AND ic.ninstrumentcatcode = " + instrumentCatCode + " "
						+ " AND i.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " AND ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND il.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND mf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND p1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND p2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND p3.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND p4.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "      	//added by sujatha ALPDJ21-21 
						+ " AND tz1.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND tz2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND tz3.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND tz4.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND inn.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND tz5.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " AND i.nsitecode = " + userInfo.getNmastersitecode() + " " + " AND mf.nsitecode = "
						+ userInfo.getNmastersitecode() + " " + " AND ic.nsitecode = " + userInfo.getNmastersitecode()
						+ " " + " AND s.nsitecode = " + userInfo.getNmastersitecode() + " " + " AND s1.nsitecode = "
						+ userInfo.getNmastersitecode() + " " + " AND u.nsitecode = " + userInfo.getNmastersitecode()
						+ " AND il.nsitecode = " + userInfo.getNmastersitecode() + " " + " AND inn.nsitecode = "
						+ userInfo.getNmastersitecode() + "  ORDER BY  i.ninstrumentcode ";

				LOGGER.info(strQuery);
				// Don't remove the following comment in the query
				/*
				 * 
				 * strQuery = "select i.*,inn.*," +
				 * " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" +
				 * userInfo.getSlanguagetypecode() + "'," +
				 * " ts2.jsondata->'stransdisplaystatus'->>'en-US') as sdefaultstatus, " +
				 * "  case i.ninstrumentstatus when -1 then '-' else " +
				 * " coalesce(ts.jsondata->'stransdisplaystatus'->>'" +
				 * userInfo.getSlanguagetypecode() + "'," +
				 * " ts.jsondata->'stransdisplaystatus'->>'en-US') end as sactivestatus ,i.ninstrumentstatus,il.sinstrumentlocationname,il.ninstrumentlocationcode, "
				 * + "  ts.ntranscode,ic.sinstrumentcatname," +
				 * "	 mf.smanufname,s.ssuppliername,s1.ssuppliername as sserviceby,si.ssitename as sregionalsitename,"
				 * + "	 CONCAT(u.sfirstname, ' ' ,u.slastname) as susername," +
				 * " coalesce(p2.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				 * + "'," +
				 * " p2.jsondata->'speriodname'->>'en-US')  as swindowsperiodplusunit , " +
				 * " coalesce(p1.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				 * + "'," +
				 * " p1.jsondata->'speriodname'->>'en-US')  as swindowsperiodminusunit , " +
				 * " coalesce(p3.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				 * + "'," +
				 * " p3.jsondata->'speriodname'->>'en-US')  as snextcalibrationperiod , " +
				 * "	tz1.stimezoneid stzmanufdate ,tz2.stimezoneid stzpodate,ic.ncalibrationreq, "
				 * +
				 * "	 tz3.stimezoneid stzreceivedate,tz4.stimezoneid stzinstallationdate,tz5.stimezoneid stzexpirydate,tz6.stimezoneid stzservicedate,"
				 * +
				 * "	 tz1.ntimezonecode ntzmanufdatecode,tz2.ntimezonecode ntzpodatecode,tz3.ntimezonecode ntzreceivedatecode,"
				 * +
				 * "	 tz4.ntimezonecode ntzinstallationdatecode,tz5.ntimezonecode ntzexpirydatecode,tz6.ntimezonecode  ntzservicedate,"
				 * + "	 COALESCE(TO_CHAR(i.dreceiveddate,'" + userInfo.getSpgsitedatetime() +
				 * "'),'') as sreceiveddate," + "	 COALESCE(TO_CHAR(i.dexpirydate,'" +
				 * userInfo.getSpgsitedatetime() + "'),'') as sexpirydate," +
				 * "	 COALESCE(TO_CHAR(i.dmanufacdate,'" + userInfo.getSpgsitedatetime() +
				 * "'),'') as smanufacdate," + "	 COALESCE(TO_CHAR(i.dpodate,'" +
				 * userInfo.getSpgsitedatetime() + "'),'') as spodate," +
				 * "	 COALESCE(TO_CHAR(i.dinstallationdate,'" + userInfo.getSpgsitedatetime()
				 * + "'),'') as sinstallationdate,COALESCE(TO_CHAR(i.dservicedate,'" +
				 * userInfo.getSpgsitedatetime() +
				 * "'),'') as sservicedate,  ic.ncalibrationreq,i.nnextcalibration,i.nnextcalibrationperiod "
				 * +
				 * "	 from instrumentname inn,instrument i,transactionstatus ts ,transactionstatus ts2,"
				 * +
				 * "  instrumentcategory ic,manufacturer mf,supplier s,supplier s1,users u,site si,instrumentlocation il,"
				 * +
				 * "	 period p1,period p2,period p3 ,timezone tz1, timezone tz2, timezone tz3, timezone tz4, timezone tz5,timezone tz6 "
				 * +
				 * "	 where il.ninstrumentlocationcode=i.ninstrumentlocationcode and inn.ninstrumentnamecode=i.ninstrumentnamecode and i.ninstrumentcode>0 and si.nsitecode = i.nregionalsitecode and ts.ntranscode=i.ninstrumentstatus and ic.ninstrumentcatcode=i.ninstrumentcatcode "
				 * +
				 * "	 and i.nsuppliercode=s.nsuppliercode  and i.nservicecode=s1.nsuppliercode and  u.nusercode=i.nusercode and ts2.ntranscode=i.ndefaultstatus  "
				 * +
				 * "	 and p1.nperiodcode=i.nwindowsperiodminusunit and p2.nperiodcode=i.nwindowsperiodplusunit and p3.nperiodcode=i.nnextcalibrationperiod"
				 * +
				 * "	 and tz1.ntimezonecode=i.ntzmanufdate and tz2.ntimezonecode=i.ntzpodate and tz3.ntimezonecode=i.ntzreceivedate "
				 * +
				 * "	 and tz4.ntimezonecode=i.ntzinstallationdate and tz5.ntimezonecode=i.ntzexpirydate and tz6.ntimezonecode=i.ntzservicedate "
				 * + "	 and mf.nmanufcode=i.nmanufcode and  ic.ninstrumentcatcode=" +
				 * instrumentCatCode + " and i.nstatus= " + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and ts.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and ic.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and il.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and mf.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and s.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and p1.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and  p2.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and  p3.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and tz1.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and tz2.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and tz3.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and tz4.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and tz5.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				 * +" and tz6.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " and u.nstatus=" + "	" +
				 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
				 * " order by i.ninstrumentcode ";
				 */

				final ObjectMapper objMapper = new ObjectMapper();
				objMapper.registerModule(new JavaTimeModule());

				final List<Instrument> lstInstGet = objMapper.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new Instrument()),
								Arrays.asList("sreceiveddate", "sexpirydate", "smanufacdate", "spodate",
										"sinstallationdate"),
								Arrays.asList(userInfo.getStimezoneid()), userInfo, true,
								Arrays.asList("sactivestatus", "sdefaultstatus"), false),
						new TypeReference<List<Instrument>>() {
						});

				objMap.put("Instrument", lstInstGet);

				if (!lstInstGet.isEmpty()) {

					selectedInstrument = (Instrument) lstInstGet.get(lstInstGet.size() - 1);
					final int ninstcode = selectedInstrument.getNinstrumentcode();

					final List<InstrumentSection> selectedSection = getInstrumentSection(ninstcode, userInfo);

					objMap.put("selectedInstrument", selectedInstrument);
					objMap.put("selectedSection", selectedSection);

					List<InstrumentValidation> lstInstrumentValidation = getInstrumentValidation(ninstcode, 0,
							userInfo);

					if (!lstInstrumentValidation.isEmpty()) {

						objMap.put("selectedInstrumentValidation",
								lstInstrumentValidation.get(lstInstrumentValidation.size() - 1));
						objMap.put("InstrumentValidation", lstInstrumentValidation);

						final int ninstrumentlogcode = lstInstrumentValidation.get(lstInstrumentValidation.size() - 1)
								.getNinstrumentvalidationcode();
						objMap.putAll(getInstrumentValidationFile(ninstrumentlogcode, userInfo));

					} else {

						objMap.put("selectedInstrumentValidation", lstInstrumentValidation);
						objMap.put("InstrumentValidation", lstInstrumentValidation);

					}
					List<InstrumentCalibration> lstInstrumentCalibration = getInstrumentCalibration(ninstcode, 0,
							userInfo);

					if (!lstInstrumentCalibration.isEmpty()) {
						objMap.put("selectedInstrumentCalibration",
								lstInstrumentCalibration.get(lstInstrumentCalibration.size() - 1));
						objMap.put("InstrumentCalibration", lstInstrumentCalibration);

						final int ninstrumentlogcode = lstInstrumentCalibration.get(lstInstrumentCalibration.size() - 1)
								.getNinstrumentcalibrationcode();
						objMap.putAll(getInstrumentCalibrationFile(ninstrumentlogcode, userInfo));

					} else {
						objMap.put("selectedInstrumentCalibration", lstInstrumentCalibration);
						objMap.put("InstrumentCalibration", lstInstrumentCalibration);

					}
					List<InstrumentMaintenance> lstInstrumentMaintenance = getInstrumentMaintenance(ninstcode, 0,
							userInfo);

					if (!lstInstrumentMaintenance.isEmpty()) {
						objMap.put("selectedInstrumentMaintenance",
								lstInstrumentMaintenance.get(lstInstrumentMaintenance.size() - 1));
						objMap.put("InstrumentMaintenance", lstInstrumentMaintenance);

						final int ninstrumentlogcode = lstInstrumentMaintenance.get(lstInstrumentMaintenance.size() - 1)
								.getNinstrumentmaintenancecode();
						objMap.putAll(getInstrumentMaintenanceFile(ninstrumentlogcode, userInfo));

					} else {
						objMap.put("selectedInstrumentMaintenance", lstInstrumentMaintenance);
						objMap.put("InstrumentMaintenance", lstInstrumentMaintenance);

					}
				} else {
					objMap.put("selectedInstrument", selectedInstrument);

				}
			} else {
				objMap.put("Instrument", lstInsCat);
				objMap.put("SelectedInsCat", null);

			}
		} else {
			selectedInstrument = getActiveInstrumentById(ninstCode, userInfo);
			if (selectedInstrument != null) {
				final List<InstrumentSection> selectedSection = getInstrumentSection(ninstCode, userInfo);
				objMap.put("selectedInstrument", selectedInstrument);
				objMap.put("selectedSection", selectedSection);

				final List<InstrumentValidation> lstInstrumentValidation = getInstrumentValidation(ninstCode, 0,
						userInfo);
				if (!lstInstrumentValidation.isEmpty()) {
					objMap.put("selectedInstrumentValidation",
							lstInstrumentValidation.get(lstInstrumentValidation.size() - 1));
					objMap.put("InstrumentValidation", lstInstrumentValidation);
					final int ninstrumentlogcode = lstInstrumentValidation.get(lstInstrumentValidation.size() - 1)
							.getNinstrumentvalidationcode();

					objMap.putAll(getInstrumentValidationFile(ninstrumentlogcode, userInfo));
				} else {
					objMap.put("InstrumentValidation", lstInstrumentValidation);
					objMap.put("selectedInstrumentValidation", lstInstrumentValidation);
				}

				final List<InstrumentCalibration> lstInstrumentCalibration = getInstrumentCalibration(ninstCode, 0,
						userInfo);
				if (!lstInstrumentCalibration.isEmpty()) {
					objMap.put("selectedInstrumentCalibration",
							lstInstrumentCalibration.get(lstInstrumentCalibration.size() - 1));
					objMap.put("InstrumentCalibration", lstInstrumentCalibration);

					final int ninstrumentlogcode = lstInstrumentCalibration.get(lstInstrumentCalibration.size() - 1)
							.getNinstrumentcalibrationcode();

					objMap.putAll(getInstrumentCalibrationFile(ninstrumentlogcode, userInfo));
				} else {
					objMap.put("InstrumentCalibration", lstInstrumentCalibration);
					objMap.put("selectedInstrumentCalibration", lstInstrumentCalibration);
				}

				final List<InstrumentMaintenance> lstInstrumentMaintenance = getInstrumentMaintenance(ninstCode, 0,
						userInfo);
				if (!lstInstrumentMaintenance.isEmpty()) {
					objMap.put("selectedInstrumentMaintenance",
							lstInstrumentMaintenance.get(lstInstrumentMaintenance.size() - 1));
					objMap.put("InstrumentMaintenance", lstInstrumentMaintenance);

					final int ninstrumentlogcode = lstInstrumentMaintenance.get(lstInstrumentMaintenance.size() - 1)
							.getNinstrumentmaintenancecode();

					objMap.putAll(getInstrumentMaintenanceFile(ninstrumentlogcode, userInfo));
				} else {
					objMap.put("InstrumentMaintenance", lstInstrumentMaintenance);
					objMap.put("selectedInstrumentMaintenance", lstInstrumentMaintenance);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

		}

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> validateOpenDate(final Integer ninstCode, final UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<>();

		boolean validRecord = false;
		int validation1, validation2;

		validation1 = jdbcTemplate.queryForObject(
				" select count(*) from instrumentcalibration where dopendate is not null and ninstrumentcode="
						+ ninstCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNmastersitecode() + ";",
				Integer.class);

		validation2 = jdbcTemplate.queryForObject(
				" select count(*) from instrumentmaintenance where dopendate is not null and ninstrumentcode="
						+ ninstCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNmastersitecode() + ";",
				Integer.class);

		if (validation1 > 0 || validation2 > 0) {
			validRecord = true;
		}
		if (validation1 == 0 && validation2 == 0) {
			validRecord = false;
		}
		objMap.put("ValidateOpenDate", validRecord);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to retrieve list of all active Instrument
	 * based on Instrument Category for the specified site through its DAO layer
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         Instrument based on Instrument Category.
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getInsByInstrumentCat(Integer ninstcatCode, UserInfo userInfo) throws Exception {

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		Map<String, Object> instrumentCatValue = new HashMap<>();
		Instrument selectedInstrument = null;

		String strQuery = "select * from instrumentcategory where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ninstrumentcatcode="
				+ ninstcatCode;

		InstrumentCategory lstInsCat = (InstrumentCategory) jdbcUtilityFunction.queryForObject(strQuery,
				InstrumentCategory.class, jdbcTemplate);

		objMap.put("SelectedInsCat", lstInsCat);
		instrumentCatValue.put("label", lstInsCat.getSinstrumentcatname());
		instrumentCatValue.put("value", lstInsCat.getNinstrumentcatcode());
		instrumentCatValue.put("item", lstInsCat);
		objMap.put("defaultInstrumentCatValue", instrumentCatValue);

		// ALPD-4517-Vignesh R(26-07-2024)-- Loader issue occurs.
		strQuery = "SELECT  i.*,inn.*," + " COALESCE(ts2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') AS sdefaultstatus, "
				+ " CASE WHEN i.ninstrumentstatus = -1 THEN '-' ELSE COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') END AS sactivestatus, ts.ntranscode, "
				+ " ic.sinstrumentcatname, il.sinstrumentlocationname,mf.smanufname, "
				+ " s.ssuppliername,  s1.ssuppliername AS sserviceby,  si.ssitename AS sregionalsitename, "
				+ " COALESCE(p1.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ " p1.jsondata->'speriodname'->>'en-US') AS swindowsperiodminusunit, "
				+ " COALESCE(p3.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "',  p3.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "') AS snextcalibrationperiod, " + " COALESCE(p4.jsondata->'speriodname'->>'"     	//added by sujatha ALPDJ21-21 
				+ userInfo.getSlanguagetypecode() + "',  p4.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "') AS snextmaintenanceperiod, "
				+ " CONCAT(u.sfirstname, ' ', u.slastname) AS susername, " + " COALESCE(p2.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ " p2.jsondata->'speriodname'->>'en-US') AS swindowsperiodplusunit, "
				+ " ic.ncalibrationreq,tz1.stimezoneid AS stzmanufdate, tz2.stimezoneid AS stzpodate, "
				+ " tz3.stimezoneid AS stzreceivedate, tz4.stimezoneid AS stzinstallationdate, "
				+ " tz5.stimezoneid AS stzexpirydate,tz6.stimezoneid AS stzservicedate, "
				+ " tz1.ntimezonecode AS ntzmanufdatecode,   tz2.ntimezonecode AS ntzpodatecode, "
				+ " tz3.ntimezonecode AS ntzreceivedatecode, "
				+ " tz4.ntimezonecode AS ntzinstallationdatecode,  tz5.ntimezonecode AS ntzexpirydatecode, "
				+ " tz6.ntimezonecode AS ntzservicedate, " + " COALESCE(TO_CHAR(i.dreceiveddate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS sreceiveddate, " + " COALESCE(TO_CHAR(i.dexpirydate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS sexpirydate, " + " COALESCE(TO_CHAR(i.dmanufacdate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS smanufacdate, " + " COALESCE(TO_CHAR(i.dservicedate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS sservicedate, " + " COALESCE(TO_CHAR(i.dpodate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS spodate, " + " COALESCE(TO_CHAR(i.dinstallationdate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS sinstallationdate,"
				+ " i.nautocalibration,COALESCE(ts3.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', " // Added by sonia on 27th Sept 2024 for Jira idL:ALPD-4939
				+ " ts3.jsondata->'stransdisplaystatus'->>'en-US') AS sautocalibration " // Added by sonia on 27th Sept
																							// 2024 for Jira
																							// idL:ALPD-4939
				+ " FROM instrument i" + " JOIN transactionstatus ts ON ts.ntranscode = i.ninstrumentstatus "
				+ " JOIN transactionstatus ts2 ON ts2.ntranscode = i.ndefaultstatus "
				+ " JOIN instrumentcategory ic ON ic.ninstrumentcatcode = i.ninstrumentcatcode"
				+ " JOIN manufacturer mf ON mf.nmanufcode = i.nmanufcode"
				+ " JOIN supplier s ON s.nsuppliercode = i.nsuppliercode "
				+ " JOIN supplier s1 ON s1.nsuppliercode = i.nservicecode "
				+ " JOIN users u ON u.nusercode = i.nusercode " + " JOIN site si ON si.nsitecode = i.nregionalsitecode "
				+ " JOIN instrumentlocation il ON il.ninstrumentlocationcode = i.ninstrumentlocationcode "
				+ " JOIN period p1 ON p1.nperiodcode = i.nwindowsperiodminusunit "
				+ " JOIN period p2 ON p2.nperiodcode = i.nwindowsperiodplusunit "
				+ " JOIN period p3 ON p3.nperiodcode = i.nnextcalibrationperiod "
				+ " JOIN period p4 ON p4.nperiodcode = i.nnextmaintenanceperiod "    	//added by sujatha ALPDJ21-21 
				+ " JOIN instrumentname inn ON inn.ninstrumentnamecode = i.ninstrumentnamecode "
				+ " JOIN timezone tz1 ON tz1.ntimezonecode = i.ntzmanufdate "
				+ " JOIN timezone tz2 ON tz2.ntimezonecode = i.ntzpodate "
				+ " JOIN timezone tz3 ON tz3.ntimezonecode = i.ntzreceivedate "
				+ " JOIN timezone tz4 ON tz4.ntimezonecode = i.ntzinstallationdate "
				+ " JOIN timezone tz5 ON tz5.ntimezonecode = i.ntzexpirydate "
				+ " JOIN timezone tz6 ON tz6.ntimezonecode = i.ntzservicedate "
				+ " JOIN transactionstatus ts3 ON ts3.ntranscode = i.nautocalibration " // Added by sonia on 27th Sept
																						// 2024 for Jira idL:ALPD-4939
				+ " WHERE i.ninstrumentcode > 0 " + " AND ic.ninstrumentcatcode = " + lstInsCat.getNinstrumentcatcode()
				+ " AND i.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " AND ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND il.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND mf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND p1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND p2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND p3.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND p4.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "     	//added by sujatha ALPDJ21-21 
				+ " AND tz1.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND tz2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND tz3.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND tz4.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND inn.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND tz5.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND mf.nsitecode = " + userInfo.getNmastersitecode() + " " + " AND ic.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " AND s.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " AND s1.nsitecode = " + userInfo.getNmastersitecode() + " " + " AND u.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND il.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " AND inn.nsitecode = " + userInfo.getNmastersitecode() + "  ORDER BY  i.ninstrumentcode ";

		// Don't remove the following comment in the query
		/*
		 * strQuery = "select i.*,inn.*," +
		 * " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" +
		 * userInfo.getSlanguagetypecode() + "'," +
		 * " ts2.jsondata->'stransdisplaystatus'->>'en-US') as sdefaultstatus, " +
		 * "  case i.ninstrumentstatus when -1 then '-' else " +
		 * " coalesce(ts.jsondata->'stransdisplaystatus'->>'" +
		 * userInfo.getSlanguagetypecode() + "'," +
		 * " ts.jsondata->'stransdisplaystatus'->>'en-US') end as sactivestatus , " +
		 * "  ts.ntranscode,ic.sinstrumentcatname,il.sinstrumentlocationname," +
		 * "	 mf.smanufname,s.ssuppliername,s1.ssuppliername as sserviceby,si.ssitename as sregionalsitename,"
		 * + " coalesce(p1.jsondata->'speriodname'->>'" +
		 * userInfo.getSlanguagetypecode() + "'," +
		 * " p1.jsondata->'speriodname'->>'en-US')  as swindowsperiodminusunit , " +
		 * " coalesce(p3.jsondata->'speriodname'->>'"+userInfo.getSlanguagetypecode()
		 * +"', p3.jsondata->'speriodname'->>'"+userInfo.getSlanguagetypecode()
		 * +"')  as snextcalibrationperiod," +
		 * "	 CONCAT(u.sfirstname,' ',u.slastname) as susername," +
		 * " coalesce(p2.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
		 * + "'," +
		 * " p2.jsondata->'speriodname'->>'en-US')  as swindowsperiodplusunit ,ic.ncalibrationreq, "
		 * + " tz1.stimezoneid stzmanufdate ,tz2.stimezoneid stzpodate," +
		 * "	 tz3.stimezoneid stzreceivedate,tz4.stimezoneid stzinstallationdate,tz5.stimezoneid stzexpirydate,tz6.stimezoneid stzservicedate,"
		 * +
		 * "	 tz1.ntimezonecode ntzmanufdatecode,tz2.ntimezonecode ntzpodatecode,tz3.ntimezonecode ntzreceivedatecode,"
		 * +
		 * "	 tz4.ntimezonecode ntzinstallationdatecode,tz5.ntimezonecode ntzexpirydatecode,tz6.ntimezonecode ntzservicedate,"
		 * + "	 COALESCE(TO_CHAR(i.dreceiveddate,'" + userInfo.getSpgsitedatetime() +
		 * "'),'') as sreceiveddate," + "	 COALESCE(TO_CHAR(i.dexpirydate,'" +
		 * userInfo.getSpgsitedatetime() + "'),'') as sexpirydate," +
		 * "	 COALESCE(TO_CHAR(i.dmanufacdate,'" + userInfo.getSpgsitedatetime() +
		 * "'),'') as smanufacdate,COALESCE(TO_CHAR(i.dservicedate,'" +
		 * userInfo.getSpgsitedatetime() + "'),'') as sservicedate," +
		 * "	 COALESCE(TO_CHAR(i.dpodate,'" + userInfo.getSpgsitedatetime() +
		 * "'),'') as spodate," + "	 COALESCE(TO_CHAR(i.dinstallationdate,'" +
		 * userInfo.getSpgsitedatetime() + "'),'') as sinstallationdate " +
		 * "	 from instrument i,transactionstatus ts ,transactionstatus ts2," +
		 * "  instrumentcategory ic,manufacturer mf,supplier s,supplier s1,users u,site si,instrumentlocation il,"
		 * +
		 * "	 period p1,period p2 ,period p3,instrumentname inn, timezone tz1, timezone tz2, timezone tz3, timezone tz4, timezone tz5,timezone tz6 "
		 * +
		 * "	 where il.ninstrumentlocationcode=i.ninstrumentlocationcode and i.ninstrumentcode>0 and i.ninstrumentnamecode =inn.ninstrumentnamecode and  si.nsitecode = i.nregionalsitecode and ts.ntranscode=i.ninstrumentstatus and ic.ninstrumentcatcode=i.ninstrumentcatcode "
		 * +
		 * "	 and i.nsuppliercode=s.nsuppliercode and i.nservicecode=s1.nsuppliercode and u.nusercode=i.nusercode and ts2.ntranscode=i.ndefaultstatus  "
		 * +
		 * "	 and p1.nperiodcode=i.nwindowsperiodminusunit and p2.nperiodcode=i.nwindowsperiodplusunit and p3.nperiodcode=i.nnextcalibrationperiod"
		 * +
		 * "	 and tz1.ntimezonecode=i.ntzmanufdate and tz2.ntimezonecode=i.ntzpodate and tz3.ntimezonecode=i.ntzreceivedate "
		 * +
		 * "	 and tz4.ntimezonecode=i.ntzinstallationdate and tz5.ntimezonecode=i.ntzexpirydate and tz6.ntimezonecode=i.ntzservicedate "
		 * + "	 and mf.nmanufcode=i.nmanufcode and  ic.ninstrumentcatcode=" +
		 * lstInsCat.getNinstrumentcatcode() + " and i.nstatus= " + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and ts.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and ic.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and il.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and mf.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and s.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and p1.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and  p2.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and  p3.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and tz1.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and tz2.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and tz3.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and tz4.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and inn.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and tz5.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and u.nstatus=" + "	" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " order by i.ninstrumentcode";
		 */

		final ObjectMapper objMapper = new ObjectMapper();

		objMapper.registerModule(new JavaTimeModule());
		final List<Instrument> lstInstGet = objMapper
				.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new Instrument()),
								Arrays.asList("sreceiveddate", "sexpirydate", "smanufacdate", "spodate",
										"sinstallationdate"),
								null, userInfo, true, Arrays.asList("sactivestatus", "sdefaultstatus"), false),
						new TypeReference<List<Instrument>>() {
						});

		objMap.put("Instrument", lstInstGet);

		if (!lstInstGet.isEmpty()) {
			selectedInstrument = (Instrument) lstInstGet.get(lstInstGet.size() - 1);
			final int ninstcode = selectedInstrument.getNinstrumentcode();

			final List<InstrumentSection> selectedSection = getInstrumentSection(ninstcode, userInfo);
			final List<InstrumentValidation> lstinstvalget = getInstrumentValidation(ninstcode, 0, userInfo);
			final List<InstrumentCalibration> lstinstcalget = getInstrumentCalibration(ninstcode, 0, userInfo);
			final List<InstrumentMaintenance> lstinstmainget = getInstrumentMaintenance(ninstcode, 0, userInfo);

			objMap.put("selectedInstrument", selectedInstrument);
			objMap.put("selectedSection", selectedSection);

			if (!lstinstvalget.isEmpty()) {
				objMap.put("selectedInstrumentValidation", lstinstvalget.get(lstinstvalget.size() - 1));
				final int ninstrumentlogcode = lstinstvalget.get(lstinstvalget.size() - 1)
						.getNinstrumentvalidationcode();
				objMap.put("InstrumentValidation", lstinstvalget);

				objMap.putAll(getInstrumentValidationFile(ninstrumentlogcode, userInfo));
			} else {
				objMap.put("selectedInstrumentValidation", lstinstvalget);

				objMap.put("InstrumentValidation", lstinstvalget);

			}
			if (!lstinstcalget.isEmpty()) {
				objMap.put("selectedInstrumentCalibration", lstinstcalget.get(lstinstcalget.size() - 1));
				final int ninstrumentlogcode = lstinstcalget.get(lstinstcalget.size() - 1)
						.getNinstrumentcalibrationcode();
				objMap.put("InstrumentCalibration", lstinstcalget);

				objMap.putAll(getInstrumentCalibrationFile(ninstrumentlogcode, userInfo));
			} else {
				objMap.put("selectedInstrumentCalibration", lstinstcalget);

				objMap.put("InstrumentCalibration", lstinstcalget);
			}
			if (!lstinstmainget.isEmpty()) {
				objMap.put("selectedInstrumentMaintenance", lstinstmainget.get(lstinstmainget.size() - 1));
				final int ninstrumentlogcode = lstinstmainget.get(lstinstmainget.size() - 1)
						.getNinstrumentmaintenancecode();
				objMap.put("InstrumentMaintenance", lstinstmainget);

				objMap.putAll(getInstrumentMaintenanceFile(ninstrumentlogcode, userInfo));
			} else {
				objMap.put("InstrumentMaintenance", lstinstmainget);
				objMap.put("selectedInstrumentMaintenance", lstinstmainget);

			}

		} else {
			objMap.put("selectedInstrument", null);
			objMap.put("selectedSection", lstInstGet);
		}

		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}

	/**
	 * This method is used to delete entry in Instrument table. Need to validate
	 * that the specified Instrument object is active and is not associated with any
	 * of its Instrument tables before updating its nstatus to -1.
	 * 
	 * @param materialcategory [MaterialCategory] object holding detail to be
	 *                         deleted in barcode table
	 * @return response entity object holding response status and data of deleted
	 *         barcode object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteInstrument(Instrument inst, final UserInfo userInfo) throws Exception {

		final Instrument instByID = (Instrument) getActiveInstrumentById(inst.getNinstrumentcode(), userInfo);// .getBody();
		final InstrumentCategory instCatValidation = instrumentCategoryDAO
				.getActiveInstrumentCategoryById(inst.getNinstrumentcatcode(),userInfo);

		if (instByID == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else if (instCatValidation == null) {

			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTCATEGORYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			String strCalibrationreqQuery = "";
			if (instCatValidation.getNcalibrationreq() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				strCalibrationreqQuery = " union all"
						+ " select 'IDS_INSTRUMENTCALIBRATION' as Msg from instrumentcalibration where ninstrumentcode= "
						+ inst.getNinstrumentcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			}

			final String strQuery = "select 'IDS_INTRUMENTVALIDATION' as Msg from instrumentvalidation where ninstrumentcode= "
					+ inst.getNinstrumentcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + strCalibrationreqQuery + " "
					+ " union all"
					+ " select 'IDS_INSTRUMENTMAINTENANCE' as Msg from instrumentmaintenance where ninstrumentcode= "
					+ inst.getNinstrumentcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					//ALPDJ21-23
					//Storage Category, Instrument Category and Storage Structure Screens mapping enhancements
					+ " union all"
					+ " select 'IDS_SAMPLESTORAGESTRUCTURE ' as Msg from samplestoragelocation where ninstrumentcode= "
					+ inst.getNinstrumentcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			// + " SELECT 'IDS_REGISTRATION' as Msg FROM registration r "
			// + " JOIN jsonb_each(r.jsondata) d ON true where "
			// + " d.value->>'pkey' ='nunitcode' and
			// d.value->>'nquerybuildertablecode'='253' "
			// + " and d.value->>'value'='" + objUnit.getNunitcode() + "'"
			// + " union all "
			// + " SELECT 'IDS_REGISTRATIONSAMPLE' as Msg FROM registrationsample rs "
			// + " JOIN jsonb_each(rs.jsondata) d1 ON true where "
			// + " d1.value->>'pkey' ='nunitcode' and
			// d1.value->>'nquerybuildertablecode'='253' "
			// + " and d1.value->>'value'='" + objUnit.getNunitcode() + "'";

			validatorDel = projectDAOSupport.getTransactionInfo(strQuery, userInfo);

			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				// ALPD-4513--Vignesh R(05-09-2024)
				Map<String, Object> objOneToManyValidation = new HashMap<String, Object>();
				objOneToManyValidation.put("primaryKeyValue", Integer.toString(inst.getNinstrumentcode()));
				objOneToManyValidation.put("stablename", "instrument");
				validatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedMaterialCategoryList = new ArrayList<>();
				String updateQueryString = "update instrument set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcode = "
						+ inst.getNinstrumentcode() + ";";

				updateQueryString = updateQueryString + "update instrumentsection set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcode = "
						+ inst.getNinstrumentcode() + ";";

				updateQueryString = updateQueryString + "update instrumentvalidation set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcode = "
						+ inst.getNinstrumentcode() + ";";

				updateQueryString = updateQueryString + "update instrumentcalibration set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcode = "
						+ inst.getNinstrumentcode() + ";";

				updateQueryString = updateQueryString + "update instrumentmaintenance set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcode = "
						+ inst.getNinstrumentcode() + ";";

				updateQueryString = updateQueryString + "update instrumentfile set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ninstrumentlogcode in( select ninstrumentvalidationcode from  instrumentvalidation "
						+ "	 where ninstrumentcode=" + inst.getNinstrumentcode() + ") and ninstrumentlogtypecode = "
						+ Enumeration.InstrumentLogType.INSTRUMENT_VALIDATION.getinstrumentlogtype() + ";";

				updateQueryString = updateQueryString + "update instrumentfile set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ninstrumentlogcode in( select ninstrumentcalibrationcode from  instrumentcalibration "
						+ "	 where ninstrumentcode=" + inst.getNinstrumentcode() + ") and ninstrumentlogtypecode = "
						+ Enumeration.InstrumentLogType.INSTRUMENT_CALIBRATION.getinstrumentlogtype() + ";";

				updateQueryString = updateQueryString + "update instrumentfile set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ninstrumentlogcode in( select ninstrumentmaintenancecode from  instrumentmaintenance "
						+ "	 where ninstrumentcode=" + inst.getNinstrumentcode() + ") and ninstrumentlogtypecode = "
						+ Enumeration.InstrumentLogType.INSTRUMENT_MAINTENANCE.getinstrumentlogtype() + ";";

				String query = " select * from instrumentsection where  ninstrumentcode=" + inst.getNinstrumentcode()
						+ " " + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode() + "";
				List<InstrumentSection> lstSection = (List<InstrumentSection>) jdbcTemplate.query(query,
						new InstrumentSection());

				query = " select * from instrumentvalidation where  ninstrumentcode=" + inst.getNinstrumentcode() + " "
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode() + ";";
				List<InstrumentValidation> lstValidation = (List<InstrumentValidation>) jdbcTemplate.query(query,
						new InstrumentValidation());

				query = " select * from instrumentcalibration where  ninstrumentcode=" + inst.getNinstrumentcode() + " "
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode() + ";";
				List<InstrumentCalibration> lstCalibration = (List<InstrumentCalibration>) jdbcTemplate.query(query,
						new InstrumentCalibration());

				query = " select * from instrumentmaintenance where  ninstrumentcode=" + inst.getNinstrumentcode() + " "
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode() + ";";
				List<InstrumentMaintenance> lstMaintance = (List<InstrumentMaintenance>) jdbcTemplate.query(query,
						new InstrumentMaintenance());

				query = " select * from instrumentfile where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and  ninstrumentlogcode in ( select ninstrumentvalidationcode from  instrumentvalidation "
						+ "	 where ninstrumentcode=" + inst.getNinstrumentcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode() + ") and ninstrumentlogtypecode ="
						+ Enumeration.InstrumentLogType.INSTRUMENT_VALIDATION.getinstrumentlogtype() + " and nsitecode="
						+ userInfo.getNmastersitecode() + ";";
				List<InstrumentFile> lstvalidationFile = (List<InstrumentFile>) jdbcTemplate.query(query,
						new InstrumentFile());

				query = " select * from instrumentfile where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and  ninstrumentlogcode in ( select ninstrumentcalibrationcode from  instrumentcalibration "
						+ "	 where ninstrumentcode=" + inst.getNinstrumentcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode() + ")" + " and ninstrumentlogtypecode ="
						+ Enumeration.InstrumentLogType.INSTRUMENT_CALIBRATION.getinstrumentlogtype()
						+ " and nsitecode=" + userInfo.getNmastersitecode() + ";";
				List<InstrumentFile> lstcalibrationFile = (List<InstrumentFile>) jdbcTemplate.query(query,
						new InstrumentFile());

				query = " select * from instrumentfile where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and  ninstrumentlogcode in ( select ninstrumentmaintenancecode from  instrumentmaintenance "
						+ "	 where ninstrumentcode=" + inst.getNinstrumentcode() + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and ninstrumentlogtypecode ="
						+ Enumeration.InstrumentLogType.INSTRUMENT_MAINTENANCE.getinstrumentlogtype()
						+ " and nsitecode=" + userInfo.getNmastersitecode() + ";";
				List<InstrumentFile> lstmaintenanceFile = (List<InstrumentFile>) jdbcTemplate.query(query,
						new InstrumentFile());

				jdbcTemplate.execute(updateQueryString);

				if (inst.getDmanufacdate() != null) {
					Date date = Date.from(inst.getDmanufacdate());
					inst.setSmanufacdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (inst.getDpodate() != null) {
					Date date = Date.from(inst.getDpodate());
					inst.setSpodate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (inst.getDreceiveddate() != null) {
					Date date = Date.from(inst.getDreceiveddate());
					inst.setSreceiveddate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (inst.getDinstallationdate() != null) {
					Date date = Date.from(inst.getDinstallationdate());
					inst.setSinstallationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (inst.getDexpirydate() != null) {
					Date date = Date.from(inst.getDexpirydate());
					inst.setSexpirydate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (inst.getDservicedate() != null) {
					Date date = Date.from(inst.getDservicedate());
					inst.setSservicedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				inst.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				List<Instrument> lstIns = new ArrayList<Instrument>();
				lstIns.add(inst);
				savedMaterialCategoryList.add(lstIns);
				savedMaterialCategoryList.add(lstSection);
				savedMaterialCategoryList.add(lstValidation);
				savedMaterialCategoryList.add(lstCalibration);
				savedMaterialCategoryList.add(lstMaintance);
				savedMaterialCategoryList.add(lstvalidationFile);
				savedMaterialCategoryList.add(lstcalibrationFile);
				savedMaterialCategoryList.add(lstmaintenanceFile);

				multilingualIDList.add("IDS_DELETEINSTRUMENT");
				multilingualIDList.add("IDS_DELETEINSTRUMENTSECTION");
				multilingualIDList.add("IDS_DELETEINSTRUMENTVALIDATION");
				multilingualIDList.add("IDS_DELETEINSTRUMENTCALIBRATION");
				multilingualIDList.add("IDS_DELETEINSTRUMENTMAINTENANCE");
				multilingualIDList.add("IDS_DELETEINSTRUMENTVALIDATIONFILE/LINK");
				multilingualIDList.add("IDS_DELETEINSTRUMENTCALIBRATIONFILE/LINK");
				multilingualIDList.add("IDS_DELETEINSTRUMENTMAINTENANCEFILE/LINK");

				auditUtilityFunction.fnInsertListAuditAction(savedMaterialCategoryList, 1, null, multilingualIDList,
						userInfo);

				Map<String, Object> responseMap = new HashMap<String, Object>();
				responseMap.putAll(
						(Map<String, Object>) getInsByInstrumentCat(inst.getNinstrumentcatcode(), userInfo).getBody());
				InstrumentCategory instCat = instrumentCategoryDAO
						.getActiveInstrumentCategoryById(inst.getNinstrumentcatcode(),userInfo);
				responseMap.put("SelectedInsCat", instCat);
				return new ResponseEntity<>(responseMap, HttpStatus.OK);

			} else {
				// status code:417
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}

		}

	}

	/**
	 * This method is used to retrieve active instrument object based on the
	 * specified instrument.
	 * 
	 * @param ninstrumentcode [int] primary key of instrument object
	 * @return response entity object holding response status and data of instrument
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Instrument getActiveInstrumentById(final int ninstrumentcode, UserInfo userInfo) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();

		// ALPD-4517-Vignesh R(26-07-2024)-- Loader issue occurs.
		final String strQuery = "SELECT i.*,  inn.*," + " COALESCE(ts2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') AS sdefaultstatus, "
				+ " COALESCE(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') AS sactivestatus, "
				+ " i.ninstrumentstatus, ts.ntranscode, ic.sinstrumentcatname,  i.nmanufcode,"
				+ " i.nsuppliercode,  inl.sinstrumentlocationname,   mf.smanufname,s.ssuppliername, "
				+ " s1.ssuppliername AS sserviceby,  si.ssitename AS sregionalsitename,"
				+ " COALESCE(p2.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ " p2.jsondata->'speriodname'->>'en-US') AS swindowsperiodplusunit,"
				+ " COALESCE(p3.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " p3.jsondata->'speriodname'->>'en-US') AS snextcalibrationperiod, "
				+ " COALESCE(p4.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode() + "',"      	//added by sujatha ALPDJ21-21 
				+ " p4.jsondata->'speriodname'->>'en-US') AS snextmaintenanceperiod, "
				+ " CONCAT(u.sfirstname, ' ', u.slastname) AS susername, " + " COALESCE(p1.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " p1.jsondata->'speriodname'->>'en-US') AS swindowsperiodminusunit, "
				+ " tz1.stimezoneid AS stzmanufdate,  tz2.stimezoneid AS stzpodate, "
				+ " tz3.stimezoneid AS stzreceivedate,  tz4.stimezoneid AS stzinstallationdate, "
				+ " tz5.stimezoneid AS stzexpirydate, tz1.ntimezonecode AS ntzmanufdatecode, "
				+ " tz2.ntimezonecode AS ntzpodatecode,tz3.ntimezonecode AS ntzreceivedatecode, "
				+ " tz4.ntimezonecode AS ntzinstallationdatecode,tz5.ntimezonecode AS ntzexpirydatecode, "
				+ " COALESCE(TO_CHAR(i.dreceiveddate, '" + userInfo.getSpgsitedatetime() + "'), '') AS sreceiveddate, "
				+ " COALESCE(TO_CHAR(i.dexpirydate, '" + userInfo.getSpgsitedatetime() + "'), '') AS sexpirydate, "
				+ " COALESCE(TO_CHAR(i.dmanufacdate, '" + userInfo.getSpgsitedatetime() + "'), '') AS smanufacdate, "
				+ " COALESCE(TO_CHAR(i.dpodate, '" + userInfo.getSpgsitedatetime() + "'), '') AS spodate,"
				+ " COALESCE(TO_CHAR(i.dinstallationdate, '" + userInfo.getSpgsitedatetime()
				+ "'), '') AS sinstallationdate, " + " COALESCE(TO_CHAR(i.dservicedate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS sservicedate, "
				+ " ic.ncalibrationreq, i.nnextcalibration,   i.nnextcalibrationperiod, i.nnextmaintenance, i.nnextmaintenanceperiod, i.npurchasecost,"
				+ " i.noffsetdmanufacdate,i.noffsetdpodate,i.noffsetdreceiveddate,i.noffsetdinstallationdate,i.noffsetdexpirydate,"
				+ " i.nautocalibration, COALESCE(ts3.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', " // Added by sonia on 27th Sept 2024 for Jira idL:ALPD-4939
				+ " ts3.jsondata->'stransdisplaystatus'->>'en-US') AS sautocalibration " // Added by sonia on 27th Sept
																							// 2024 for Jira
																							// idL:ALPD-4939
				+ " FROM  instrument i JOIN "
				+ " instrumentname inn ON i.ninstrumentnamecode = inn.ninstrumentnamecode "
				+ " JOIN  transactionstatus ts ON ts.ntranscode = i.ninstrumentstatus "
				+ " JOIN  transactionstatus ts2 ON ts2.ntranscode = i.ndefaultstatus "
				+ " JOIN  instrumentcategory ic ON ic.ninstrumentcatcode = i.ninstrumentcatcode "
				+ " JOIN  manufacturer mf ON mf.nmanufcode = i.nmanufcode "
				+ " JOIN  supplier s ON i.nsuppliercode = s.nsuppliercode "
				+ " JOIN  supplier s1 ON i.nservicecode = s1.nsuppliercode "
				+ " JOIN  users u ON u.nusercode = i.nusercode "
				+ " JOIN  site si ON i.nregionalsitecode = si.nsitecode "
				+ " JOIN  instrumentlocation inl ON inl.ninstrumentlocationcode = i.ninstrumentlocationcode "
				+ " JOIN  period p1 ON p1.nperiodcode = i.nwindowsperiodminusunit "
				+ " JOIN  period p2 ON p2.nperiodcode = i.nwindowsperiodplusunit "
				+ " JOIN  period p3 ON p3.nperiodcode = i.nnextcalibrationperiod "
				+ " JOIN  period p4 ON p4.nperiodcode = i.nnextmaintenanceperiod "    	//added by sujatha ALPDJ21-21 
				+ " JOIN  timezone tz1 ON tz1.ntimezonecode = i.ntzmanufdate "
				+ " JOIN  timezone tz2 ON tz2.ntimezonecode = i.ntzpodate "
				+ " JOIN  timezone tz3 ON tz3.ntimezonecode = i.ntzreceivedate "
				+ " JOIN  timezone tz4 ON tz4.ntimezonecode = i.ntzinstallationdate "
				+ " JOIN  timezone tz5 ON tz5.ntimezonecode = i.ntzexpirydate "
				+ "	JOIN  transactionstatus ts3 ON ts3.ntranscode = i.nautocalibration " // Added by sonia on 27th Sept
																							// 2024 for Jira
																							// idL:ALPD-4939
				+ " WHERE i.ninstrumentcode = " + ninstrumentcode + " " + " AND i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND ic.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND mf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND p1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND p2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND p3.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND p4.nstatus = "    	//added by sujatha ALPDJ21-21 
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND inn.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND tz3.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND tz4.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND tz5.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND s1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND inl.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		// Don't remove the following comment in the query
		/*
		 * final String strQuery = "select i.*,inn.*," +
		 * " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" +
		 * userInfo.getSlanguagetypecode() + "'," +
		 * " ts2.jsondata->'stransdisplaystatus'->>'en-US') as sdefaultstatus, " +
		 * " coalesce(ts.jsondata->'stransdisplaystatus'->>'" +
		 * userInfo.getSlanguagetypecode() + "'," +
		 * " ts.jsondata->'stransdisplaystatus'->>'en-US') as sactivestatus,i.ninstrumentstatus, "
		 * + " ts.ntranscode,ic.sinstrumentcatname," //+
		 * "il.sinstrumentlocationname,i.ninstrumentlocationcode,
		 * +" i.nmanufcode,i.nsuppliercode,inl.sinstrumentlocationname," +
		 * " mf.smanufname,s.ssuppliername,s1.ssuppliername as sserviceby,si.ssitename as sregionalsitename,"
		 * + " coalesce(p2.jsondata->'speriodname'->>'" +
		 * userInfo.getSlanguagetypecode() + "'," +
		 * " p2.jsondata->'speriodname'->>'en-US')  as swindowsperiodplusunit , " + "" +
		 * " coalesce(p3.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
		 * + "'," +
		 * " p3.jsondata->'speriodname'->>'en-US')  as snextcalibrationperiod , " + "" +
		 * " CONCAT(u.sfirstname,' ',u.slastname) as susername," +
		 * " coalesce(p1.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
		 * + "'," +
		 * " p1.jsondata->'speriodname'->>'en-US')  as swindowsperiodminusunit , " +
		 * " tz1.stimezoneid stzmanufdate ,tz2.stimezoneid stzpodate," +
		 * " tz3.stimezoneid stzreceivedate,tz4.stimezoneid stzinstallationdate,tz5.stimezoneid stzexpirydate,"
		 * //+ "tz6.stimezoneid stzservicedate," +
		 * " tz1.ntimezonecode ntzmanufdatecode,tz2.ntimezonecode ntzpodatecode,tz3.ntimezonecode ntzreceivedatecode,"
		 * +
		 * " tz4.ntimezonecode ntzinstallationdatecode,tz5.ntimezonecode ntzexpirydatecode,"
		 * //+ "tz6.ntimezonecode ntzservicedate," +
		 * " COALESCE(TO_CHAR(i.dreceiveddate,'" + userInfo.getSpgsitedatetime() +
		 * "'),'') as sreceiveddate," + " COALESCE(TO_CHAR(i.dexpirydate,'" +
		 * userInfo.getSpgsitedatetime() + "'),'') as sexpirydate," +
		 * " COALESCE(TO_CHAR(i.dmanufacdate,'" + userInfo.getSpgsitedatetime() +
		 * "'),'') as smanufacdate," + " COALESCE(TO_CHAR(i.dpodate,'" +
		 * userInfo.getSpgsitedatetime() + "'),'') as spodate," +
		 * " COALESCE(TO_CHAR(i.dinstallationdate,'" + userInfo.getSpgsitedatetime()+
		 * "'),'') as sinstallationdate," + " COALESCE(TO_CHAR(i.dservicedate,'" +
		 * userInfo.getSpgsitedatetime()+ "'),'') as sservicedate," //+
		 * "COALESCE(TO_CHAR(i.dservicedate,'" + userInfo.getSpgsitedatetime() //+
		 * "'),'') as sservicedate,
		 * +" ic.ncalibrationreq,i.nnextcalibration,i.nnextcalibrationperiod,i.npurchasecost, i.noffsetdmanufacdate,i.noffsetdpodate, i.noffsetdreceiveddate, i.noffsetdinstallationdate, i.noffsetdexpirydate"
		 * //+ "i.noffsetdservicedate
		 * +" from instrument i,transactionstatus ts ,transactionstatus ts2," +
		 * " instrumentcategory ic,manufacturer mf,supplier s," +
		 * " users u,supplier s1,site si," //+ "instrumentlocation il," +
		 * " period p1,period p2,period p3 ,timezone tz1," +
		 * " timezone tz2, timezone tz3, timezone tz4, timezone tz5," //+ "timezone tz6,
		 * +" instrumentname inn, instrumentlocation inl" + " where" //+ "
		 * il.ninstrumentlocationcode=i.ninstrumentlocationcode and
		 * +" i.ninstrumentcode>0 and i.ninstrumentnamecode=inn.ninstrumentnamecode and ts.ntranscode=i.ninstrumentstatus and ic.ninstrumentcatcode=i.ninstrumentcatcode "
		 * +
		 * " and i.nsuppliercode=s.nsuppliercode and i.nservicecode=s1.nsuppliercode and  u.nusercode=i.nusercode and inl.ninstrumentlocationcode = i.ninstrumentlocationcode "
		 * +
		 * " and ts2.ntranscode=i.ndefaultstatus and p1.nperiodcode=i.nwindowsperiodminusunit and p2.nperiodcode=i.nwindowsperiodplusunit and p3.nperiodcode=i.nnextcalibrationperiod"
		 * +
		 * " and tz1.ntimezonecode=i.ntzmanufdate and tz2.ntimezonecode=i.ntzpodate and tz3.ntimezonecode=i.ntzreceivedate"
		 * +
		 * " and tz4.ntimezonecode=i.ntzinstallationdate and tz5.ntimezonecode=i.ntzexpirydate  "
		 * //+ "tz6.ntimezonecode=i.ntzservicedate" +
		 * " and mf.nmanufcode=i.nmanufcode and i.nregionalsitecode = si.nsitecode and i.nstatus= "
		 * + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
		 * " and ts.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and ic.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
		 * " and mf.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and s.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
		 * " and p1.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and  p2.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
		 * " and p3.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
		 * " and tz1.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and inn.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and tz2.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
		 * " and tz3.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and tz4.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
		 * " and tz5.nstatus=" //+
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
		 * " and tz6.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
		 * " and u.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
		 * +" and s1.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
		 * " and inl.nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
		 * +" and i.ninstrumentcode = " + ninstrumentcode;
		 */

		Instrument instGet = (Instrument) jdbcUtilityFunction.queryForObject(strQuery, Instrument.class, jdbcTemplate);
		if (instGet != null) {

			if (instGet.getDmanufacdate() != null || instGet.getDexpirydate() != null
					|| instGet.getDinstallationdate() != null || instGet.getDpodate() != null
					|| instGet.getDreceiveddate() != null) {

				List<String> lstDateField = new ArrayList<String>();
				if (instGet.getDmanufacdate() != null) {
					lstDateField.add("smanufacdate");
				}
				if (instGet.getDpodate() != null) {
					lstDateField.add("spodate");
				}
				if (instGet.getDreceiveddate() != null) {
					lstDateField.add("sreceiveddate");
				}
				if (instGet.getDinstallationdate() != null) {
					lstDateField.add("sinstallationdate");
				}
				if (instGet.getDexpirydate() != null) {
					lstDateField.add("sexpirydate");
				}
				if (instGet.getDservicedate() != null) {
					lstDateField.add("sservicedate");
				}

				objMapper.registerModule(new JavaTimeModule());
				final List<Instrument> lstSelInstGet = objMapper.convertValue(

						dateUtilityFunction.getSiteLocalTimeFromUTC(Arrays.asList(instGet), lstDateField,
								Arrays.asList(userInfo.getStimezoneid()), userInfo, true, Arrays.asList("sactivestatus",
										"sdefaultstatus", "swindowsperiodminusunit", "swindowsperiodplusunit"),
								false),
						new TypeReference<List<Instrument>>() {
						});
				return (Instrument) (lstSelInstGet.get(0));
			} else {
				instGet.setDmodifieddate(null);
				final List<Instrument> lstSelInstGet = objMapper
						.convertValue(
								commonFunction.getMultilingualMessageList(Arrays.asList(instGet),
										Arrays.asList("sactivestatus", "swindowsperiodminusunit",
												"swindowsperiodplusunit"),
										userInfo.getSlanguagefilename()),
								new TypeReference<List<Instrument>>() {
								});
				return (Instrument) (lstSelInstGet.get(0));
			}
		} else {
			return null;
		}

		// return (Instrument) jdbcUtilityFunction.queryForObject(lstInstGet,
		// Instrument.class);//,HttpStatus.OK);

	}

	/**
	 * This method declaration is used to retrieve list of all active Instrument
	 * Name for the specified site through its DAO layer
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response object holding response status and list of all active
	 *         Instrument Name
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Instrument getInstrumentNameByInstrument(final Instrument instrumentName, final int nsiteCode)
			throws Exception {
		final String queryString = "select * from instrumentname where sinstrumentname=N'"
				+ stringUtilityFunction.replaceQuote(instrumentName.getSinstrumentname()) + "' and ninstrumentnamecode="
				+ instrumentName.getNinstrumentnamecode() + " and nsitecode = " + nsiteCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (Instrument) jdbcUtilityFunction.queryForObject(queryString, Instrument.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to instrument table. Need to check for
	 * duplicate entry of sinstrumentname for the specified site before saving into
	 * database.
	 * 
	 * @param materialcategory [Instrument] object holding details to be added in
	 *                         instrument table
	 * @return response entity object holding response status and data of added
	 *         instrument object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createInstrument(Map<String, Object> inputMap, UserInfo userInfo,
			List<InstrumentSection> instSect) throws Exception {

		final String sQueryLock = " lock  table lockinstrument " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedMaterialCategoryList = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		Instrument inst = objMapper.convertValue(inputMap.get("instrument"), Instrument.class);
		final InstrumentValidation instVal = objMapper.convertValue(inputMap.get("instrumentvalidation"),
				InstrumentValidation.class);
		final InstrumentCalibration instCal = objMapper.convertValue(inputMap.get("instrumentcalibration"),
				InstrumentCalibration.class);
		final InstrumentMaintenance instMain = objMapper.convertValue(inputMap.get("instrumentmaintenance"),
				InstrumentMaintenance.class);

		if (inst.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			final Instrument defaultInst = getInstrumentByDefaultStatus(inst.getNinstrumentcatcode(),
					inst.getNsitecode());
			if (defaultInst != null) {

				final List<Object> defaultListBeforeSave = new ArrayList<>();
				defaultListBeforeSave.add(defaultInst);
				defaultInst.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

				final String updateQueryString = " update instrument set ndefaultstatus="
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcode ="
						+ defaultInst.getNinstrumentcode();
				jdbcTemplate.execute(updateQueryString);

				final List<Object> defaultListAfterSave = new ArrayList<>();
				defaultListAfterSave.add(defaultInst);

				multilingualIDList.add("IDS_ADDINSTRUMENT");
				auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
						multilingualIDList, userInfo);

			}
		}
		final Instrument instrumentName = getInstrumentNameByInstrument(inst, userInfo.getNmastersitecode());

		if (instrumentName == null) {
			final String nameQuery = "select * from instrumentname where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sinstrumentname=N'"
					+ stringUtilityFunction.replaceQuote(inst.getSinstrumentname()) + "' and nsitecode="
					+ userInfo.getNmastersitecode() + "";

			final InstrumentName instrumentByName = (InstrumentName) jdbcUtilityFunction.queryForObject(nameQuery,
					InstrumentName.class, jdbcTemplate);
			// ALPD-5330 - Gowtham R - Default Section not loaded initially (section present
			// in organization validation) - 08-02-2025

			final String validation = "select s.* from section s, labsection ls where s.nsectioncode=ls.nsectioncode "
					+ " and ls.nsitecode = " + inst.getNregionalsitecode() + " and s.nsitecode = "
					+ userInfo.getNmastersitecode() + " and ls.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsectioncode ="
					+ inst.getNsectioncode() + " and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by s.nsectioncode ";

			List<Section> sectionlst = (List<Section>) jdbcTemplate.query(validation, new Section());

			if (instrumentByName == null && (!sectionlst.isEmpty() || inst.getNsectioncode() == -1)) {
				InstrumentName instrumentNameList = new InstrumentName();
				instrumentNameList.setSinstrumentname(inst.getSinstrumentname());
				instrumentNameList.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

				String sequencequery = "select nsequenceno from seqnoinstrumentmanagement where stablename ='instrumentname'";
				int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
				nsequenceno++;

				String insertquery = "Insert into instrumentname (ninstrumentnamecode,sinstrumentname, dmodifieddate, nsitecode, nstatus) values"
						+ "(" + nsequenceno + ", N'"
						+ stringUtilityFunction.replaceQuote(instrumentNameList.getSinstrumentname()) + "', " + " '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
						+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);

				String updatequery = "update seqnoinstrumentmanagement set nsequenceno =" + nsequenceno
						+ " where stablename='instrumentname'";
				jdbcTemplate.execute(updatequery);

				inst.setNinstrumentnamecode(nsequenceno);
			} else if (instrumentByName != null) {
				inst.setNinstrumentnamecode(instrumentByName.getNinstrumentnamecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SECTIONNOTINSITE", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
		Double purchasecost;
		String modelNo, poNo, remarks, poDate, receivedDate, installationDate, expiryDate, servicedate,
				softwareinformation, performancecapabilities, acceptancecriteria, description, serialno,
				sassociateddocument, smovement, manufactDate;

		final JavaTimeModule javaTimeModule = new JavaTimeModule();
		objMapper.registerModule(javaTimeModule);
		final List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();

		if (inst.getDmanufacdate() != null) {
			inst.setSmanufacdate(
					dateUtilityFunction.instantDateToString(inst.getDmanufacdate()).replace("T", " ").replace("Z", ""));
			lstDateField.add("smanufacdate");
			lstDatecolumn.add("ntzmanufdate");
		}
		if (inst.getDpodate() != null) {
			inst.setSpodate(
					dateUtilityFunction.instantDateToString(inst.getDpodate()).replace("T", " ").replace("Z", ""));
			lstDateField.add("spodate");
			lstDatecolumn.add("ntzpodate");
		}
		if (inst.getDreceiveddate() != null) {
			inst.setSreceiveddate(dateUtilityFunction.instantDateToString(inst.getDreceiveddate()).replace("T", " ")
					.replace("Z", ""));
			lstDateField.add("sreceiveddate");
			lstDatecolumn.add("ntzreceivedate");
		}
		if (inst.getDinstallationdate() != null) {
			inst.setSinstallationdate(dateUtilityFunction.instantDateToString(inst.getDinstallationdate())
					.replace("T", " ").replace("Z", ""));
			lstDateField.add("sinstallationdate");
			lstDatecolumn.add("ntzinstallationdate");
		}
		if (inst.getDexpirydate() != null) {
			inst.setSexpirydate(
					dateUtilityFunction.instantDateToString(inst.getDexpirydate()).replace("T", " ").replace("Z", ""));
			lstDateField.add("sexpirydate");
			lstDatecolumn.add("ntzexpirydate");
		}

		if (inst.getDservicedate() != null) {
			inst.setSservicedate(
					dateUtilityFunction.instantDateToString(inst.getDservicedate()).replace("T", " ").replace("Z", ""));
			lstDateField.add("sservicedate");
			lstDatecolumn.add("ntzservicedate");
		}

		final Instrument convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(inst, lstDateField, lstDatecolumn, true, userInfo),
				new TypeReference<Instrument>() {
				});


		if (convertedObject.getSmodelnumber() == null) {
			modelNo = "";
		} else {
			modelNo = stringUtilityFunction.replaceQuote(inst.getSmodelnumber());
		}
		if (convertedObject.getSpono() == null) {
			poNo = "";
		} else {
			poNo = stringUtilityFunction.replaceQuote(inst.getSpono());
		}
		if (convertedObject.getSremarks() == null) {
			remarks = "";
		} else {
			remarks = stringUtilityFunction.replaceQuote(inst.getSremarks());
		}
		if (convertedObject.getDpodate() == null) {
			poDate = null;
		} else {
			poDate = "'" + convertedObject.getSpodate() + "'";
		}
		if (convertedObject.getDreceiveddate() == null) {
			receivedDate = null;
		} else {
			receivedDate = "'" + convertedObject.getSreceiveddate() + "'";
		}
		if (convertedObject.getDinstallationdate() == null) {
			installationDate = null;
		} else {
			installationDate = "'" + convertedObject.getSinstallationdate() + "'";
		}
		if (convertedObject.getDexpirydate() == null) {
			expiryDate = null;
		} else {
			expiryDate = "'" + convertedObject.getSexpirydate() + "'";
		}
		if (convertedObject.getDmanufacdate() == null) {
			manufactDate = null;
		} else {
			manufactDate = "'" + convertedObject.getSmanufacdate() + "'";
		}
		if (convertedObject.getDservicedate() == null) {
			servicedate = null;
		} else {
			servicedate = "'" + convertedObject.getSservicedate() + "'";
		}
		if (convertedObject.getSsoftwareinformation() == null) {
			softwareinformation = "";
		} else {
			softwareinformation = stringUtilityFunction.replaceQuote(inst.getSsoftwareinformation());
		}
		if (convertedObject.getSperformancecapabilities() == null) {
			performancecapabilities = "";
		} else {
			performancecapabilities = stringUtilityFunction.replaceQuote(inst.getSperformancecapabilities());
		}
		if (convertedObject.getSacceptancecriteria() == null) {
			acceptancecriteria = "";
		} else {
			acceptancecriteria = stringUtilityFunction.replaceQuote(inst.getSacceptancecriteria());
		}
		if (convertedObject.getSserialno() == null) {
			serialno = "";
		} else {
			serialno = stringUtilityFunction.replaceQuote(inst.getSserialno());
		}
		if (convertedObject.getSdescription() == null) {
			description = "";
		} else {
			description = stringUtilityFunction.replaceQuote(inst.getSdescription());
		}
		if (convertedObject.getNpurchasecost() == null) {
			purchasecost = (double) 0.00;
		} else {
			purchasecost = inst.getNpurchasecost();
		}
		if (convertedObject.getSassociateddocument() == null) {
			sassociateddocument = "";
		} else {
			sassociateddocument = stringUtilityFunction.replaceQuote(inst.getSassociateddocument());
		}
		if (convertedObject.getSmovement() == null) {
			smovement = "";
		} else {
			smovement = stringUtilityFunction.replaceQuote(inst.getSmovement());
		}

		// Added by sonia on 27th Sept 2024 for Jira idL:ALPD-4939
		final String settingsQuery = "select * from settings where nsettingcode ="
				+ Enumeration.Settings.AUTO_CALIBRATION.getNsettingcode() + " and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(settingsQuery, Settings.class,
				jdbcTemplate);
		Short autoCalibration = -1;
		if (objSettings != null) {
			autoCalibration = Short.parseShort(String.valueOf(objSettings.getSsettingvalue()));

		}

		// Added by sonia on 04th March 2025 for Jira idL:ALPD-5504
		if (inputMap.containsKey("nautogenerationid")) {
			int nautoGenerationID = (int) inputMap.get("nautogenerationid");
			if (nautoGenerationID == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				String strformat = projectDAOSupport.getSeqfnFormat("instrument", "seqnoformatgeneratorinstrument", 0,
						0, userInfo);
				inst.setSinstrumentid(strformat);
			}
		}

		final String sequencenoquery = "select nsequenceno from seqnoinstrumentmanagement where stablename ='instrument'";
		int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
		nsequenceno++;
		// Modified the insert query by sonia on 27th Sept 2024 for Jira idL:ALPD-4939
		final String insertquery = "Insert into instrument (ninstrumentcode, ninstrumentcatcode, sinstrumentid, ninstrumentnamecode,sinstrumentname, "
				+ "sdescription, smodelnumber, sserialno, nmanufcode, dmanufacdate, ntzmanufdate, noffsetdmanufacdate, "
				+ "nusercode, nsuppliercode,ninstrumentlocationcode, nservicecode, spono, dpodate, ntzpodate, noffsetdpodate, dreceiveddate, "
				+ "ntzreceivedate, noffsetdreceiveddate, dinstallationdate, ntzinstallationdate, noffsetdinstallationdate, "
				+ "dexpirydate, ntzexpirydate, noffsetdexpirydate,dservicedate,ntzservicedate,noffsetdservicedate, ninstrumentstatus, nwindowsperiodminus, nwindowsperiodminusunit, "
				+ "nwindowsperiodplus, nwindowsperiodplusunit,nnextcalibration,nnextcalibrationperiod,nnextmaintenance, nnextmaintenanceperiod, nregionalsitecode, sremarks,sassociateddocument,smovement,ssoftwareinformation,sperformancecapabilities,sacceptancecriteria,npurchasecost, ndefaultstatus, dmodifieddate, nsitecode, nstatus,nautocalibration) "
				+ " values(" + nsequenceno + ", " + inst.getNinstrumentcatcode() + ", N'"
				+ stringUtilityFunction.replaceQuote(inst.getSinstrumentid()) + "', " + inst.getNinstrumentnamecode()
				+ ",N'" + stringUtilityFunction.replaceQuote(inst.getSinstrumentname()) + "', N'" + description
				+ "', N'" + modelNo + "', N'" + serialno + "'," + inst.getNmanufcode() + ", " + manufactDate + ", "
				+ convertedObject.getNtzmanufdate() + ", " + convertedObject.getNoffsetdmanufacdate() + ", "
				+ inst.getNusercode() + ", " + inst.getNsuppliercode() + "," + inst.getNinstrumentlocationcode() + ", "
				+ inst.getNservicecode() + ", N'" + poNo + "'," + poDate + ", " + convertedObject.getNtzpodate() + ", "
				+ convertedObject.getNoffsetdpodate() + ", " + receivedDate + ", " + convertedObject.getNtzreceivedate()
				+ ", " + convertedObject.getNoffsetdreceiveddate() + ", " + installationDate + ", "
				+ convertedObject.getNtzinstallationdate() + ", " + convertedObject.getNoffsetdinstallationdate() + ", "
				+ expiryDate + ", " + convertedObject.getNtzexpirydate() + ", "
				+ convertedObject.getNoffsetdexpirydate() + "," + servicedate + ","
				+ convertedObject.getNtzservicedate() + "," + convertedObject.getNoffsetdservicedate() + ", "
				+ inst.getNinstrumentstatus() + ", " + inst.getNwindowsperiodminus() + ", "
				+ inst.getNwindowsperiodminusunit() + ", " + inst.getNwindowsperiodplus() + ", "
				+ inst.getNwindowsperiodplusunit() + "," + inst.getNnextcalibration() + ","
				+ inst.getNnextcalibrationperiod() + "," + inst.getNnextmaintenance() + ","   	//added by sujatha ALPDJ21-21 
				+ inst.getNnextmaintenanceperiod() + "," + inst.getNregionalsitecode() + ", N'" + remarks + "',N'"
				+ sassociateddocument + "',N'" + smovement + "',N'" + softwareinformation + "',N'"
				+ performancecapabilities + "',N'" + acceptancecriteria + "'," + purchasecost + ","
				+ inst.getNdefaultstatus() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				+ userInfo.getNmastersitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "," + autoCalibration + ")";
		jdbcTemplate.execute(insertquery);

		String updatequery = "update seqnoinstrumentmanagement set nsequenceno =" + nsequenceno
				+ " where stablename='instrument'";
		jdbcTemplate.execute(updatequery);

		// start
		// ALPD-4941-->Added by Vignesh R(11-02-2025)--Instrument scheduler
		if (inst.getNcalibrationreq() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			final ObjectMapper objectMapper = new ObjectMapper();

			final String sampleSchedulerConfigTypeCodeQry = "select nsampleschedulerconfigtypecode from sampleschedulerconfigtype where nsampletypecode="
					+ Enumeration.SampleType.INSTRUMENT.getType() + " and nschedulerconfigtypecode="
					+ Enumeration.SchedulerConfigType.INTERNAL.getSchedulerConfigType() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int nsampleschedulerconfigtypecode = jdbcTemplate.queryForObject(sampleSchedulerConfigTypeCodeQry,
					Integer.class);

			final String str = "select ssd.nschedulersamplecode,nschedulecode,jsondata,jsonuidata from schedulersampledetail ssd,"
					+ " schedulerconfigsite scs  where scs.nschedulersamplecode=ssd.nschedulersamplecode "
					+ " and ninstrumentcatcode = " + inst.getNinstrumentcatcode() + " and ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					// +" and
					// nactivestatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsampleschedulerconfigtypecode=" + nsampleschedulerconfigtypecode + " and scs.nsitecode="
					+ userInfo.getNtranssitecode()
					// +" and ssd.nsitecode="+userInfo.getNtranssitecode()
					+ " and ssd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and scs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<SchedulerSampleDetail> schedulerSampleDetailList = jdbcTemplate.query(str,
					new SchedulerSampleDetail());

			if (!schedulerSampleDetailList.isEmpty()) {
				JSONObject ssdjsondata = new JSONObject(
						objectMapper.writeValueAsString(schedulerSampleDetailList.get(0).getJsondata()));
				JSONObject ssdjsonuidata = new JSONObject(
						objectMapper.writeValueAsString(schedulerSampleDetailList.get(0).getJsonuidata()));
				final String qrybuilderstr = "select nquerybuildertablecode from querybuildertables where stablename='view_instrumentname' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final int nquerybuildertablecode = jdbcTemplate.queryForObject(qrybuilderstr, Integer.class);

				JSONObject newInstrument = new JSONObject();
				JSONObject newInstrumentID = new JSONObject();

				newInstrument.put("pkey", "ninstrumentnamecode");
				newInstrument.put("label", stringUtilityFunction.replaceQuote(inst.getSinstrumentname()));
				newInstrument.put("value", inst.getNinstrumentnamecode());
				newInstrument.put("source", "view_instrumentname");
				newInstrument.put("ninstrumentnamecode", inst.getNinstrumentnamecode());
				newInstrument.put("nquerybuildertablecode", nquerybuildertablecode);

				final String qrybuilderstrInstument = "select nquerybuildertablecode from querybuildertables where stablename='view_instrument_autocalibration' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final int nquerybuildertablecodeinstrumentid = jdbcTemplate.queryForObject(qrybuilderstrInstument,
						Integer.class);

				newInstrumentID.put("pkey", "ninstrumentcode");
				newInstrumentID.put("label", stringUtilityFunction.replaceQuote(inst.getSinstrumentid()));
				newInstrumentID.put("value", nsequenceno);
				newInstrumentID.put("source", "view_instrument_autocalibration");
				newInstrumentID.put("ninstrumentcode", nsequenceno);
				newInstrumentID.put("nquerybuildertablecode", nquerybuildertablecodeinstrumentid);

				final String regTemplateQry = "select jsondata from reactregistrationtemplate rrt join designtemplatemapping dtm on dtm.nreactregtemplatecode=rrt.nreactregtemplatecode and dtm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " join approvalconfigversion acv  on dtm.ndesigntemplatemappingcode = acv.ndesigntemplatemappingcode "
						+ " and acv.napproveconfversioncode=" + ssdjsonuidata.getInt("napproveconfversioncode");

				final String jsondatastr = jdbcTemplate.queryForObject(regTemplateQry, String.class);

				JSONArray rrtjsondata = new JSONArray(jsondatastr);

				final String instrumentLabel = findLabel(rrtjsondata, "Instrument Name");

				final String instrumentIDLabel = findLabel(rrtjsondata, "Instrument");

				ssdjsondata.put(instrumentLabel, newInstrument);
				ssdjsondata.put(instrumentIDLabel, newInstrumentID);

				ssdjsondata.put("ninstrumentcode", nsequenceno);
				ssdjsondata.put("ninstrumentnamecode", inst.getNinstrumentnamecode());

				ssdjsonuidata.put(instrumentLabel, stringUtilityFunction.replaceQuote(inst.getSinstrumentname()));
				ssdjsonuidata.put(instrumentIDLabel, stringUtilityFunction.replaceQuote(inst.getSinstrumentid()));

				final String seqnoschedulertransaction = "select nsequenceno from seqnoscheduler where stablename='schedulertransaction'";
				int seqno = jdbcTemplate.queryForObject(seqnoschedulertransaction, Integer.class);
				seqno = seqno + 1;

				String schedulerqry = " INSERT INTO public.schedulertransaction("
						+ "	nschedulertransactioncode, nschedulecode, nsampletypecode, jsondata, jsonuidata,"
						+ " nschedulersamplecode, npreregno, dscheduleoccurrencedate, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, ntransactionstatus, nsitecode, nstatus)"
						+ "	select " + seqno + ", " + schedulerSampleDetailList.get(0).getNschedulecode() + ", "
						+ Enumeration.SampleType.INSTRUMENT.getType() + "," + " '"
						+ stringUtilityFunction.replaceQuote(ssdjsondata.toString()) + "','"
						+ stringUtilityFunction.replaceQuote(ssdjsonuidata.toString()) + "', "
						+ schedulerSampleDetailList.get(0).getNschedulersamplecode() + "," + " "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,"
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
						+ userInfo.getNtimezonecode() + ", "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
						+ userInfo.getNtranssitecode() + "," + ""
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(schedulerqry);
				schedulerqry = "update seqnoscheduler set nsequenceno=" + seqno
						+ " where stablename='schedulertransaction' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(schedulerqry);

				jdbcTemplate.execute("call sp_instrumentscheduler(" + seqno + ","
						+ schedulerSampleDetailList.get(0).getNschedulersamplecode() + ","
						+ userInfo.getNtranssitecode() + ")");

			}
		}

		inst.setNinstrumentcode(nsequenceno);

		final String strQuery = "select * from instrument where ninstrumentcode>0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " order by 1 desc ";
		List<Instrument> lstInstCode = (List<Instrument>) jdbcTemplate.query(strQuery, new Instrument());
		instVal.setNinstrumentcode(lstInstCode.get(0).getNinstrumentcode());
		instCal.setNinstrumentcode(lstInstCode.get(0).getNinstrumentcode());
		instMain.setNinstrumentcode(lstInstCode.get(0).getNinstrumentcode());

		userInfo.setNsitecode(lstInstCode.get(0).getNregionalsitecode());
		userInfo.setNsitecode(lstInstCode.get(0).getNregionalsitecode());
		userInfo.setNsitecode(lstInstCode.get(0).getNregionalsitecode());

		instVal.setIsreadonly(true);
		instCal.setIsreadonly(true);
		instMain.setIsreadonly(true);
		createInstrumentValidation(instVal, userInfo);
		// Added by sonia on 27th Sept 2024 for Jira idL:ALPD-4939
		if (objSettings != null) {
			if (Integer.valueOf(objSettings.getSsettingvalue()) == Enumeration.TransactionStatus.NO
					.gettransactionstatus()) {
				createInstrumentCalibration(instCal, userInfo);

			}
		}
		createInstrumentMaintenance(instMain, userInfo);

		final Instrument objInstForAudit = (Instrument) getActiveInstrumentById(nsequenceno, userInfo);

		if (inst.getDmanufacdate() != null) {
			Date date = Date.from(inst.getDmanufacdate());
			objInstForAudit.setSmanufacdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		if (inst.getDpodate() != null) {
			Date date = Date.from(inst.getDpodate());
			objInstForAudit.setSpodate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		if (inst.getDreceiveddate() != null) {
			Date date = Date.from(inst.getDreceiveddate());
			objInstForAudit.setSreceiveddate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		if (inst.getDinstallationdate() != null) {
			Date date = Date.from(inst.getDinstallationdate());
			objInstForAudit.setSinstallationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		if (inst.getDexpirydate() != null) {
			Date date = Date.from(inst.getDexpirydate());
			objInstForAudit.setSexpirydate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		if (inst.getDservicedate() != null) {
			Date date = Date.from(inst.getDservicedate());
			objInstForAudit.setSservicedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		savedMaterialCategoryList.add(objInstForAudit);
		multilingualIDList.add("IDS_ADDINSTRUMENT");

		if (instSect != null) {

			final String strSecQuery = "select * from instrument where ninstrumentcode>0 and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + " order by 1 desc ";

			List<Instrument> lstInstSecGet = (List<Instrument>) jdbcTemplate.query(strSecQuery, new Instrument());

			if (lstInstSecGet != null) {

				final InstrumentSection defaultInst = getInstrumentSectionByDefaultStatus(userInfo.getNsitecode(),
						lstInstSecGet.get(0).getNinstrumentcode());
				if (defaultInst == null) {
					int lstSection = lstInstSecGet.get(0).getNinstrumentcode();
					String sectSeq = "select nsequenceno from seqnoinstrumentmanagement where stablename='instrumentsection'";
					SeqNoInstrumentManagement objseq = (SeqNoInstrumentManagement) jdbcUtilityFunction
							.queryForObject(sectSeq, SeqNoInstrumentManagement.class, jdbcTemplate);
					String instSection = "";
					int count = 0;
					String defaultStatus = "";
					int seqNo = objseq.getNsequenceno();
					for (int i = 0; i <= instSect.size() - 1; i++) {
						seqNo = seqNo + 1;
						count++;
						if (count == 1) {
							defaultStatus = "" + Enumeration.TransactionStatus.YES.gettransactionstatus() + "";
							instSection = " insert into instrumentsection(ninstrumentsectioncode,ninstrumentcode,"
									+ " nsectioncode,nusercode,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
									+ " values(" + seqNo + "," + lstSection + "," + inst.getNsectioncode() + "," + " "
									+ instSect.get(i).getNusercode() + "," + defaultStatus + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNsitecode()
									+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
							jdbcTemplate.execute(instSection);

						} else {
							defaultStatus = "" + Enumeration.TransactionStatus.NO.gettransactionstatus() + "";
							instSection = " insert into instrumentsection(ninstrumentsectioncode,ninstrumentcode,"
									+ " nsectioncode,nusercode,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
									+ " values(" + seqNo + "," + lstSection + "," + inst.getNsectioncode() + ","
									+ instSect.get(i).getNusercode() + "," + defaultStatus + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNsitecode()
									+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
							jdbcTemplate.execute(instSection);

						}
					}
					instSection = "update seqnoinstrumentmanagement set nsequenceno=" + seqNo
							+ " where stablename='instrumentsection'";

					jdbcTemplate.execute(instSection);

					String secQry = "select * from instrumentsection where ninstrumentcode = "
							+ inst.getNinstrumentcode() + " and nsectioncode = " + inst.getNsectioncode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "  and nsitecode=" + userInfo.getNtranssitecode() + ";";

					List<InstrumentSection> lstInstSection = (List<InstrumentSection>) jdbcTemplate.query(secQry,
							new InstrumentSection());

					auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(lstInstSection), 1, null,
							Arrays.asList("IDS_ADDINSTRUMENTSECTION"), userInfo);

				}
			}
		}

		auditUtilityFunction.fnInsertAuditAction(savedMaterialCategoryList, 1, null, multilingualIDList, userInfo);
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap
				.putAll((Map<String, Object>) getInsByInstrumentCat(inst.getNinstrumentcatcode(), userInfo).getBody());
		InstrumentCategory instCat = instrumentCategoryDAO
				.getActiveInstrumentCategoryById(inst.getNinstrumentcatcode(),userInfo);
		responseMap.put("SelectedInsCat", instCat);

		// return getInstrument(null, userInfo);
		return new ResponseEntity<>(responseMap, HttpStatus.OK);

	}

	/**
	 * This interface declaration is used to check Duplicate Record a new entry to
	 * instrument table.
	 * 
	 * @param materialcategory [Instrument] object holding details to be added in
	 *                         instrument table
	 * @return response entity object holding response status and data of active
	 *         instrument object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public Instrument getInstrumentByName(final String sinstrumentid, final int nsitecode) throws Exception {

		final String strQuery = "select ninstrumentcode ,ndefaultstatus from instrument where sinstrumentname = N'"
				+ stringUtilityFunction.replaceQuote(sinstrumentid) + "'" + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and  nsitecode =" + nsitecode;

		return (Instrument) jdbcUtilityFunction.queryForObject(strQuery, Instrument.class, jdbcTemplate);

	}

	/**
	 * This method is used to get a default unit object with respect to the site
	 * code
	 * 
	 * @param nSiteCode       [int] Site code
	 * @param ninstrumentcode [int] primary key of instrument table ninstrumentcode
	 * @return a inst Object
	 * @throws Exception that are from DAO layer
	 */
	private Instrument getInstrumentByDefaultStatus(int ninstrumentcatcode, int nsiteCode) throws Exception {
		final String strQuery = "select i.* from instrument i  where i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " ";
		// + "and i.ninstrumentcatcode="+ ninstrumentcatcode + "" + "and i.nsitecode = "
		// + nsiteCode + "";
		return (Instrument) jdbcUtilityFunction.queryForObject(strQuery, Instrument.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in instrument table. Need to validate
	 * that the instrument object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of sinstrumentname for the
	 * specified site before saving into database.
	 * 
	 * @param materialcategory [Instrument] object holding details to be updated in
	 *                         instrument table
	 * @return response entity object holding response status and data of updated
	 *         instrument object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateInstrument(Instrument inst, UserInfo userInfo, InstrumentSection instSect)
			throws Exception {

		final Instrument objInst = (Instrument) getActiveInstrumentById(inst.getNinstrumentcode(), userInfo);
		if (objInst == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			//ALPDJ21-23
			//Storage Category, Instrument Category and Storage Structure Screens mapping enhancements
			//start
			final String query="select ssl.* from samplestoragelocation ssl ,instrument i "
					+ " where i.ninstrumentcode=ssl.ninstrumentcode "
					+ " and i.ninstrumentcode>0 and i.ninstrumentcode="+inst.getNinstrumentcode()+" and ssl.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ssl.nmastersitecode="+userInfo.getNmastersitecode() 
					+ " and i.nsitecode="+userInfo.getNmastersitecode();
			final SampleStorageStructure objSampleStorageStructure=(SampleStorageStructure) 
					jdbcUtilityFunction.queryForObject(query, SampleStorageStructure.class, jdbcTemplate);
			if(objSampleStorageStructure!= null && objSampleStorageStructure.getNsitecode() != inst.getNregionalsitecode()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_STORAGECREATEDFORTHISINSTRUMENTSITE",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			//end
			if (objInst.getDmanufacdate() != null) {
				Date date = Date.from(objInst.getDmanufacdate());
				objInst.setSmanufacdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
			}
			if (objInst.getDpodate() != null) {
				Date date = Date.from(objInst.getDpodate());
				objInst.setSpodate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
			}
			if (objInst.getDreceiveddate() != null) {
				Date date = Date.from(objInst.getDreceiveddate());
				objInst.setSreceiveddate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
			}
			if (objInst.getDinstallationdate() != null) {
				Date date = Date.from(objInst.getDinstallationdate());
				objInst.setSinstallationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
			}
			if (objInst.getDexpirydate() != null) {
				Date date = Date.from(objInst.getDexpirydate());
				objInst.setSexpirydate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
			}
			if (objInst.getDservicedate() != null) {
				Date date = Date.from(objInst.getDservicedate());
				objInst.setSservicedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
			}
			final List<String> multilingualIDList = new ArrayList<>();

			final List<Object> listAfterUpdate = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			String updateQueryString = "";

			final String queryString = "select ninstrumentcode from instrument where sinstrumentid = N'"
					+ stringUtilityFunction.replaceQuote(inst.getSinstrumentid()) + "' " + " "
					+ " and ninstrumentcode <> " + inst.getNinstrumentcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			final List<Instrument> instList = (List<Instrument>) jdbcTemplate.query(queryString, new Instrument());

			if (instList.isEmpty()) {

				final String nameQuery = "select * from instrumentname where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sinstrumentname=N'"
						+ stringUtilityFunction.replaceQuote(inst.getSinstrumentname()) + "' and nsitecode="
						+ userInfo.getNmastersitecode() + "";
				final InstrumentName instrumentByName = (InstrumentName) jdbcUtilityFunction.queryForObject(nameQuery,
						InstrumentName.class, jdbcTemplate);
				if (instrumentByName == null) {
					InstrumentName instrumentNameList = new InstrumentName();
					instrumentNameList.setSinstrumentname(inst.getSinstrumentname());
					instrumentNameList.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

					final String sequencequery = "select nsequenceno from seqnoinstrumentmanagement where stablename ='instrumentname'";
					int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
					nsequenceno++;

					final String insertquery = "Insert into instrumentname (ninstrumentnamecode,sinstrumentname, dmodifieddate, nsitecode, nstatus) values"
							+ "(" + nsequenceno + ", N'"
							+ stringUtilityFunction.replaceQuote(instrumentNameList.getSinstrumentname()) + "', " + " '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
							+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);

					final String updatequery = "update seqnoinstrumentmanagement set nsequenceno =" + nsequenceno
							+ " where stablename='instrumentname'";
					jdbcTemplate.execute(updatequery);

					inst.setNinstrumentnamecode(nsequenceno);
				} else {
					inst.setNinstrumentnamecode(instrumentByName.getNinstrumentnamecode());
				}

				if (inst.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

					final Instrument defaultInst = getInstrumentByDefaultStatus(inst.getNinstrumentcatcode(),
							inst.getNsitecode());

					if (defaultInst != null && defaultInst.getNinstrumentcode() != inst.getNinstrumentcode()) {

						defaultInst.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						updateQueryString = " update instrument set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where ninstrumentcode="
								+ defaultInst.getNinstrumentcode() + ";";

					}

				}

				String instrquery = "select * from instrumentcalibration where ninstrumentcode = "
						+ inst.getNinstrumentcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "order by ninstrumentcalibrationcode desc LIMIT 1;";
				InstrumentCalibration objInstCalib = (InstrumentCalibration) jdbcUtilityFunction
						.queryForObject(instrquery, InstrumentCalibration.class, jdbcTemplate);

				instrquery = "select * from instrumentmaintenance where ninstrumentcode = " + inst.getNinstrumentcode()
						+ " " + "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "   order by ninstrumentmaintenancecode desc LIMIT 1;";
				InstrumentMaintenance objInstMain = (InstrumentMaintenance) jdbcUtilityFunction
						.queryForObject(instrquery, InstrumentMaintenance.class, jdbcTemplate);

				instrquery = "select * from instrumentvalidation where ninstrumentcode = " + inst.getNinstrumentcode()
						+ " and " + "nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "order by ninstrumentvalidationcode desc LIMIT 1;";
				InstrumentValidation objInstVal = (InstrumentValidation) jdbcUtilityFunction.queryForObject(instrquery,
						InstrumentValidation.class, jdbcTemplate);

				if (inst.getDmanufacdate() != null || inst.getDexpirydate() != null
						|| inst.getDinstallationdate() != null || inst.getDpodate() != null
						|| inst.getDreceiveddate() != null || inst.getDservicedate() != null) {

					final ObjectMapper objMapper = new ObjectMapper();
					objMapper.registerModule(new JavaTimeModule());
					List<String> lstDateField = new ArrayList<String>();
					List<String> lstDatecolumn = new ArrayList<String>();

					if (inst.getDmanufacdate() != null) {
						inst.setSmanufacdate(
								dateUtilityFunction.instantDateToString(inst.getDmanufacdate()).replace("T", " "));
						lstDateField.add("smanufacdate");
						lstDatecolumn.add("ntzmanufdate");
					}
					if (inst.getDpodate() != null) {
						inst.setSpodate(dateUtilityFunction.instantDateToString(inst.getDpodate()).replace("T", " "));
						lstDateField.add("spodate");
						lstDatecolumn.add("ntzpodate");
					}
					if (inst.getDreceiveddate() != null) {
						inst.setSreceiveddate(
								dateUtilityFunction.instantDateToString(inst.getDreceiveddate()).replace("T", " "));
						lstDateField.add("sreceiveddate");
						lstDatecolumn.add("ntzreceivedate");
					}
					if (inst.getDinstallationdate() != null) {
						inst.setSinstallationdate(
								dateUtilityFunction.instantDateToString(inst.getDinstallationdate()).replace("T", " "));
						lstDateField.add("sinstallationdate");
						lstDatecolumn.add("ntzinstallationdate");
					}
					if (inst.getDexpirydate() != null) {
						inst.setSexpirydate(
								dateUtilityFunction.instantDateToString(inst.getDexpirydate()).replace("T", " "));
						lstDateField.add("sexpirydate");
						lstDatecolumn.add("ntzexpirydate");
					}
					if (inst.getDservicedate() != null) {
						inst.setSservicedate(
								dateUtilityFunction.instantDateToString(inst.getDservicedate()).replace("T", " "));
						lstDateField.add("sservicedate");
						lstDatecolumn.add("ntzservicedate");
					}

					final Instrument convertedObject = objMapper.convertValue(dateUtilityFunction
							.convertInputDateToUTCByZone(inst, lstDateField, lstDatecolumn, true, userInfo),
							new TypeReference<Instrument>() {
							});
					if (inst.getDmanufacdate() == null || inst.getDexpirydate() == null
							|| convertedObject.getDmanufacdate().isBefore(convertedObject.getDexpirydate())) {

						String smanfdate = "";
						if (!convertedObject.getSmanufacdate().equals("-1")) {
							smanfdate = "dmanufacdate='" + convertedObject.getSmanufacdate() + "',";
						} else {
							smanfdate = "dmanufacdate=null,";
						}
						if (!convertedObject.getSpodate().equals("-1")) {
							smanfdate = smanfdate + "dpodate='" + convertedObject.getSpodate() + "',";
						} else {
							smanfdate = smanfdate + "dpodate=null,";
						}
						if (!convertedObject.getSreceiveddate().equals("-1")) {
							smanfdate = smanfdate + "dreceiveddate='" + convertedObject.getSreceiveddate() + "',";
						} else {
							smanfdate = smanfdate + "dreceiveddate=null,";
						}
						if (!convertedObject.getSinstallationdate().equals("-1")) {
							smanfdate = smanfdate + "dinstallationdate='" + convertedObject.getSinstallationdate()
									+ "',";
						} else {
							smanfdate = smanfdate + "dinstallationdate=null,";
						}
						if (!convertedObject.getSexpirydate().equals("-1")) {
							smanfdate = smanfdate + "dexpirydate='" + convertedObject.getSexpirydate() + "',";
						} else {
							smanfdate = smanfdate + "dexpirydate=null,";
						}

						if (!convertedObject.getSservicedate().equals("-1")) {
							smanfdate = smanfdate + "dservicedate='" + convertedObject.getSservicedate() + "',";
						} else {
							smanfdate = smanfdate + "dservicedate=null,";
						}

						if (objInst.getNregionalsitecode() != inst.getNregionalsitecode()) {
							final String validationStr = validateInstrumenttransction(inst.getNinstrumentcode(),
									objInst.getNregionalsitecode(), userInfo);
							if (!Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(validationStr)) {
								return new ResponseEntity<>(commonFunction.getMultilingualMessage(validationStr,
										userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
							}
						}

						updateQueryString = updateQueryString + " update instrument set ninstrumentcatcode="
								+ inst.getNinstrumentcatcode() + "," + "  sinstrumentid=N'"
								+ stringUtilityFunction.replaceQuote(inst.getSinstrumentid()) + "',"
								+ " sinstrumentname=N'" + stringUtilityFunction.replaceQuote(inst.getSinstrumentname())
								+ "', ninstrumentnamecode=" + inst.getNinstrumentnamecode() + " ,ndefaultstatus = "
								+ inst.getNdefaultstatus() + " ,sremarks=N'"
								+ stringUtilityFunction.replaceQuote(inst.getSremarks()) + "',nsuppliercode="
								+ inst.getNsuppliercode() + ",smodelnumber=N'"
								+ stringUtilityFunction.replaceQuote(inst.getSmodelnumber()) + "'," + smanfdate
								+ "ntzmanufdate=" + inst.getNtzmanufdate() + "" + " ,ntzpodate=" + inst.getNtzpodate()
								+ ",ntzreceivedate=" + inst.getNtzreceivedate() + "," + "  ntzinstallationdate="
								+ inst.getNtzinstallationdate() + ",ntzexpirydate=" + inst.getNtzexpirydate() + ""
								+ " ,ninstrumentstatus=" + inst.getNinstrumentstatus() + ",nwindowsperiodminus="
								+ inst.getNwindowsperiodminus() + "," + "  nwindowsperiodplus="
								+ inst.getNwindowsperiodplus() + ",nwindowsperiodminusunit="
								+ inst.getNwindowsperiodminusunit() + "," + "  nwindowsperiodplusunit="
								+ inst.getNwindowsperiodplusunit() + ",nservicecode=" + inst.getNservicecode() + ","
								+ "  sserialno=N'" + stringUtilityFunction.replaceQuote(inst.getSserialno())
								+ "',spono =N'" + stringUtilityFunction.replaceQuote(inst.getSpono()) + "',nmanufcode="
								+ inst.getNmanufcode() + ",nnextcalibration=" + inst.getNnextcalibration()
								+ ",nnextcalibrationperiod=" + inst.getNnextcalibrationperiod() + ",nnextmaintenance="      	//added by sujatha ALPDJ21-21 
								+ inst.getNnextmaintenance() + ",nnextmaintenanceperiod="
								+ inst.getNnextmaintenanceperiod() + ",nregionalsitecode=" + inst.getNregionalsitecode()
								+ ",sdescription=N'" + stringUtilityFunction.replaceQuote(inst.getSdescription())
								+ "',sassociateddocument=N'"
								+ stringUtilityFunction.replaceQuote(inst.getSassociateddocument()) + "',smovement=N'"
								+ stringUtilityFunction.replaceQuote(inst.getSmovement()) + "',ntzservicedate="
								+ inst.getNtzservicedate() + ",ssoftwareinformation=N'"
								+ stringUtilityFunction.replaceQuote(inst.getSsoftwareinformation())
								+ "',sperformancecapabilities=N'"
								+ stringUtilityFunction.replaceQuote(inst.getSperformancecapabilities())
								+ "',sacceptancecriteria=N'"
								+ stringUtilityFunction.replaceQuote(inst.getSacceptancecriteria())
								+ "',ninstrumentlocationcode=" + inst.getNinstrumentlocationcode() + ",npurchasecost="
								+ inst.getNpurchasecost() + "  where ninstrumentcode=" + inst.getNinstrumentcode()
								+ "  and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

						jdbcTemplate.execute(updateQueryString);
						if (objInst.getNregionalsitecode() != inst.getNregionalsitecode()) {

							userInfo.setNsitecode(inst.getNregionalsitecode());

							objInstVal.setIsreadonly(true);
							objInstCalib.setIsreadonly(true);
							objInstMain.setIsreadonly(true);

							createInstrumentValidation(objInstVal, userInfo);
							createInstrumentCalibration(objInstCalib, userInfo);
							createInstrumentMaintenance(objInstMain, userInfo);

						}

					}

					else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MANUFBEFOREXPIRE",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					updateQueryString = updateQueryString + " update instrument set ninstrumentcatcode="
							+ inst.getNinstrumentcatcode() + "," + "  sinstrumentid=N'"
							+ stringUtilityFunction.replaceQuote(inst.getSinstrumentid()) + "',"
							+ "  sinstrumentname=N'" + stringUtilityFunction.replaceQuote(inst.getSinstrumentname())
							+ "' ,ndefaultstatus = " + inst.getNdefaultstatus() + " ,sremarks=N'"
							+ stringUtilityFunction.replaceQuote(inst.getSremarks()) + "',nsuppliercode="
							+ inst.getNsuppliercode() + ",smodelnumber=N'"
							+ stringUtilityFunction.replaceQuote(inst.getSmodelnumber()) + "'," + "  dmanufacdate="
							+ inst.getDmanufacdate() + ",noffsetdmanufacdate=" + inst.getNoffsetdmanufacdate()
							+ ",dinstallationdate= " + inst.getDinstallationdate() + ",noffsetdinstallationdate="
							+ inst.getNoffsetdinstallationdate() + " ,dexpirydate=" + inst.getDexpirydate()
							+ ",noffsetdexpirydate=" + inst.getNoffsetdexpirydate() + ",dpodate=" + inst.getDpodate()
							+ ",noffsetdpodate=" + inst.getNoffsetdpodate() + " ,dreceiveddate="
							+ inst.getDreceiveddate() + ",noffsetdreceiveddate=" + inst.getNoffsetdreceiveddate()
							+ ",ntzmanufdate=" + inst.getNtzmanufdate() + " ,ntzpodate=" + inst.getNtzpodate()
							+ ",ntzreceivedate=" + inst.getNtzreceivedate() + "," + "  ntzinstallationdate="
							+ inst.getNtzinstallationdate() + ",ntzexpirydate=" + inst.getNtzexpirydate() + ""
							+ " ,ninstrumentstatus=" + inst.getNinstrumentstatus() + ",nwindowsperiodminus="
							+ inst.getNwindowsperiodminus() + "," + "  nwindowsperiodplus="
							+ inst.getNwindowsperiodplus() + ",nwindowsperiodminusunit="
							+ inst.getNwindowsperiodminusunit() + "," + "  nwindowsperiodplusunit="
							+ inst.getNwindowsperiodplusunit() + ",nservicecode=" + inst.getNservicecode() + ","
							+ "  sserialno=N'" + stringUtilityFunction.replaceQuote(inst.getSserialno()) + "',spono =N'"
							+ stringUtilityFunction.replaceQuote(inst.getSpono()) + "',nmanufcode="
							+ inst.getNmanufcode() + ",nnextcalibration=" + inst.getNnextcalibration()
							+ ",nnextcalibrationperiod=" + inst.getNnextcalibrationperiod() + ",nnextmaintenance="         	//added by sujatha ALPDJ21-21 
							+ inst.getNnextmaintenance() + ",nnextmaintenanceperiod=" + inst.getNnextmaintenanceperiod()
							+ ",nregionalsitecode=" + inst.getNregionalsitecode() + ",ninstrumentnamecode="
							+ inst.getNinstrumentnamecode() + ",sdescription=N'"
							+ stringUtilityFunction.replaceQuote(inst.getSdescription()) + "',sassociateddocument=N'"
							+ stringUtilityFunction.replaceQuote(inst.getSassociateddocument()) + "',smovement=N'"
							+ stringUtilityFunction.replaceQuote(inst.getSmovement()) + "'," + "dservicedate="
							+ inst.getDservicedate() + ",ntzservicedate=" + inst.getNtzservicedate()
							+ ",noffsetdservicedate=" + inst.getNoffsetdservicedate() + "" + ",ssoftwareinformation=N'"
							+ stringUtilityFunction.replaceQuote(inst.getSsoftwareinformation())
							+ "',sperformancecapabilities=N'"
							+ stringUtilityFunction.replaceQuote(inst.getSperformancecapabilities())
							+ "',sacceptancecriteria=N'"
							+ stringUtilityFunction.replaceQuote(inst.getSacceptancecriteria()) + "' "
							+ ",ninstrumentlocationcode=" + inst.getNinstrumentlocationcode() + ",npurchasecost="
							+ inst.getNpurchasecost() + " where ninstrumentcode=" + inst.getNinstrumentcode()
							+ "  and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				}

				if (instSect != null) {

					final String strQuery = " select nsectioncode from instrumentsection where ninstrumentcode = "
							+ inst.getNinstrumentcode() + " ";

					List<Instrument> lstInstSecGet = (List<Instrument>) jdbcTemplate.query(strQuery, new Instrument());
					if (lstInstSecGet != null) {
						String updateInstSection = " update instrumentsection set nsectioncode="
								+ instSect.getNsectioncode() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where  ninstrumentcode = "
								+ inst.getNinstrumentcode() + "";
						jdbcTemplate.execute(updateInstSection);

					}

				}
				jdbcTemplate.execute(updateQueryString);

				final Instrument updatedAuditInstrument = (Instrument) getActiveInstrumentById(
						inst.getNinstrumentcode(), userInfo);
				if (inst.getDmanufacdate() != null) {
					Date date = Date.from(updatedAuditInstrument.getDmanufacdate());
					updatedAuditInstrument.setSmanufacdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (inst.getDpodate() != null) {
					Date date = Date.from(updatedAuditInstrument.getDpodate());
					updatedAuditInstrument.setSpodate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (inst.getDreceiveddate() != null) {
					Date date = Date.from(updatedAuditInstrument.getDreceiveddate());
					updatedAuditInstrument.setSreceiveddate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (inst.getDinstallationdate() != null) {
					Date date = Date.from(updatedAuditInstrument.getDinstallationdate());
					updatedAuditInstrument
							.setSinstallationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (inst.getDexpirydate() != null) {
					Date date = Date.from(updatedAuditInstrument.getDexpirydate());
					updatedAuditInstrument.setSexpirydate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				listAfterUpdate.add(updatedAuditInstrument);
				listBeforeUpdate.add(objInst);

				multilingualIDList.add("IDS_EDITINSTRUMENT");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				if (objInst.getNinstrumentcatcode() == inst.getNinstrumentcatcode()) {
					return getInstrument(inst.getNinstrumentcode(), userInfo);
				} else {
					Map<String, Object> responseMap = new HashMap<String, Object>();
					responseMap.putAll((Map<String, Object>) getInsByInstrumentCatForEdit(inst.getNinstrumentcatcode(),
							inst.getNinstrumentcode(), userInfo));
					return new ResponseEntity<>(responseMap, HttpStatus.OK);
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
	 * This method is used to fetch the active Instrument objects for the
	 * InstrumentCategory name and site.
	 * 
	 * @param sunitname       [String] name of the unit
	 * @param nmasterSiteCode [int] site code of the unit
	 * @return list of active unit code(s) based on the specified unit name and site
	 * @throws Exception
	 */

	public Map<String, Object> getInsByInstrumentCatForEdit(Integer ninstcatCode, Integer instCode, UserInfo userInfo)
			throws Exception {

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		Map<String, Object> instrumentCatValue = new HashMap<>();
		Instrument selectedInstrument = null;
		String strQuery = "select * from instrumentcategory  where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ninstrumentcatcode="
				+ ninstcatCode + " and nsitecode=" + userInfo.getNmastersitecode() + "";

		InstrumentCategory lstInsCat = (InstrumentCategory) jdbcUtilityFunction.queryForObject(strQuery,
				InstrumentCategory.class, jdbcTemplate);
		objMap.put("SelectedInsCat", lstInsCat);
		instrumentCatValue.put("label", lstInsCat.getSinstrumentcatname());
		instrumentCatValue.put("value", lstInsCat.getNinstrumentcatcode());
		instrumentCatValue.put("item", lstInsCat);
		objMap.put("defaultInstrumentCatValue", instrumentCatValue);

		// ALPD-5961 - incorrect alias name
		strQuery = "select i.*,inn.sinstrumentname,inn.ninstrumentnamecode,"
				+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') as sdefaultstatus, "
				+ "  case i.ninstrumentstatus when -1 then '-' else "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') end as sactivestatus, "
				+ "  ts.ntranscode,ic.sinstrumentcatname," + "	 mf.smanufname,s.ssuppliername,"
				+ " coalesce(p2.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " p2.jsondata->'speriodname'->>'en-US')  as swindowsperiodplusunit , "
				+ " coalesce(p1.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " p1.jsondata->'speriodname'->>'en-US')  as swindowsperiodminusunit , "
				+ "	 CONCAT(u.sfirstname,' ',u.slastname) as susername,ic.ncalibrationreq, "
				+ "	 tz1.stimezoneid stzmanufdate ,tz2.stimezoneid stzpodate,"
				+ "	 tz3.stimezoneid stzreceivedate,tz4.stimezoneid stzinstallationdate,tz5.stimezoneid stzexpirydate,"
				+ "	 tz1.ntimezonecode ntzmanufdatecode,tz2.ntimezonecode ntzpodatecode,tz3.ntimezonecode ntzreceivedatecode,"
				+ "	 tz4.ntimezonecode ntzinstallationdatecode,tz5.ntimezonecode ntzexpirydatecode,"
				+ "	 COALESCE(TO_CHAR(i.dreceiveddate,'" + userInfo.getSpgsitedatetime() + "'),'') as sreceiveddate,"
				+ "	 COALESCE(TO_CHAR(i.dexpirydate,'" + userInfo.getSpgsitedatetime() + "'),'') as sexpirydate,"
				+ "	 COALESCE(TO_CHAR(i.dmanufacdate,'" + userInfo.getSpgsitedatetime() + "'),'') as smanufacdate,"
				+ "	 COALESCE(TO_CHAR(i.dpodate,'" + userInfo.getSpgsitedatetime() + "'),'') as spodate,"
				+ "	 COALESCE(TO_CHAR(i.dinstallationdate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sinstallationdate "
				+ "	 from instrument i,instrumentname inn,transactionstatus ts ,transactionstatus ts2,"
				+ "  instrumentcategory ic,manufacturer mf,supplier s,users u,"
				+ "	 period p1,period p2 ,timezone tz1, timezone tz2, timezone tz3, timezone tz4, timezone tz5 "
				+ "	 where i.ninstrumentcode>0 and i.ninstrumentnamecode=inn.ninstrumentnamecode and  ts.ntranscode=i.ninstrumentstatus and ic.ninstrumentcatcode=i.ninstrumentcatcode "
				+ "	 and i.nsuppliercode=s.nsuppliercode and  u.nusercode=i.nusercode and ts2.ntranscode=i.ndefaultstatus  "
				+ "	 and p1.nperiodcode=i.nwindowsperiodminusunit and p2.nperiodcode=i.nwindowsperiodplusunit"
				+ "	 and tz1.ntimezonecode=i.ntzmanufdate and tz2.ntimezonecode=i.ntzpodate and tz3.ntimezonecode=i.ntzreceivedate "
				+ "	 and tz4.ntimezonecode=i.ntzinstallationdate and tz5.ntimezonecode=i.ntzexpirydate "
				+ "	 and mf.nmanufcode=i.nmanufcode and  ic.ninstrumentcatcode=" + lstInsCat.getNinstrumentcatcode()
				+ " and i.nstatus= " + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ic.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and mf.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p1.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  p2.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz1.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz2.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz3.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz4.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz5.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and inn.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus=" + "	" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " AND i.nsitecode = " + userInfo.getNmastersitecode() + " " + " AND mf.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " AND ic.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " AND s.nsitecode = " + userInfo.getNmastersitecode() + " " + " AND u.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " AND i.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " AND inn.nsitecode = " + userInfo.getNmastersitecode() + " ";

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<Instrument> lstInstGet = objMapper
				.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new Instrument()),
								Arrays.asList("sreceiveddate", "sexpirydate", "smanufacdate", "spodate",
										"sinstallationdate"),
								Arrays.asList(userInfo.getStimezoneid()), userInfo, true,
								Arrays.asList("sactivestatus"), false),
						new TypeReference<List<Instrument>>() {
						});

		objMap.put("Instrument", lstInstGet);

		if (!lstInstGet.isEmpty()) {
			selectedInstrument = (Instrument) lstInstGet.stream().filter(x -> x.getNinstrumentcode() == instCode)
					.collect(Collectors.toList()).get(0);
			final int ninstcode = selectedInstrument.getNinstrumentcode();
			final List<InstrumentSection> selectedSection = getInstrumentSection(ninstcode, userInfo);
			objMap.put("selectedInstrument", selectedInstrument);
			objMap.put("selectedSection", selectedSection);
		} else {
			objMap.put("selectedInstrument", null);
			objMap.put("selectedSection", lstInstGet);
		}

		return objMap;
	}

	/**
	 * This interface declaration is used to retrieve list of all active Instrument
	 * Status for the specified site through its DAO layer
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         Instrument Status
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getInstrumentStatus(UserInfo userInfo) throws Exception {

		final String strQuery = " select ntranscode,coalesce(jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + "jsondata->'stransdisplaystatus'->>'en-US') as stransstatus "
				+ " from transactionstatus where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntranscode in (" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.DISPOSED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.OFFSITE.gettransactionstatus() + ")";

		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new TransactionStatus()), HttpStatus.OK);
	}

	// @Override
	// public ResponseEntity<Object> getPeriod(Integer ncontrolCode, UserInfo
	// userInfo) throws Exception {
	// //
	// // final String strQuery = "select p.*,p.nperiodcode as
	// // nwindowsperiodminusunit,nperiodcode as "
	// // + " nwindowsperiodplusunit from period p" + " where nstatus="
	// // + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and
	// // nperiodcode>0;";
	//
	// final String strQuery = " select p.nperiodcode," + "
	// coalesce(p.jsondata->'speriodname'->>'"
	// + userInfo.getSlanguagetypecode() + "'," + "
	// p.jsondata->'speriodname'->>'en-US') as speriodname"
	// + " from period p,periodconfig pc where p.nstatus = "
	// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	// + " and p.nperiodcode=pc.nperiodcode and pc.nformcode=" +
	// userInfo.getNformcode()
	// + " and pc.ncontrolcode=" + ncontrolCode + "" + " and p.nperiodcode in(2,3,4)
	// and pc.nstatus="
	// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and
	// nsitecode= "
	// + userInfo.getNmastersitecode();
	//
	// return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Period()),
	// HttpStatus.OK);
	//
	// }

	@Override
	public Map<String, Object> validateAndInsertSeqNoForInstrument(Map<String, Object> inputMap, UserInfo userInfo,
			List<InstrumentSection> instSect) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Instrument inst = objmapper.convertValue(inputMap.get("instrument"), Instrument.class);

		final String queryString = "select ninstrumentcode from instrument where sinstrumentid = N'"
				+ stringUtilityFunction.replaceQuote(inst.getSinstrumentid()) + "' " + " and ninstrumentcode <> "
				+ inst.getNinstrumentcode() + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + "";

		final List<Instrument> instList = (List<Instrument>) jdbcTemplate.query(queryString, new Instrument());
		if (instList.isEmpty()) {
			final String strSelectSeqno = "select stablename,nsequenceno from seqnoinstrumentmanagement where stablename "
					+ "in ('instrument','instrumentsection')";

			List<SeqNoInstrumentManagement> lstSeqNo = (List<SeqNoInstrumentManagement>) jdbcTemplate
					.query(strSelectSeqno, new SeqNoInstrumentManagement());

			returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoInstrumentManagement::getStablename,
					SeqNoInstrumentManagement -> SeqNoInstrumentManagement.getNsequenceno()));

			final String strSeqnoUpdate = "Update seqnoregistration set nsequenceno = "
					+ (lstSeqNo.get(0).getNsequenceno() + 1) + " where stablename = 'instrument' ;"
					+ "Update seqnoregistration set nsequenceno = " + (lstSeqNo.get(1).getNsequenceno() + 1)
					+ " where stablename = 'instrumentsection';";

			jdbcTemplate.execute(strSeqnoUpdate);

			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		} else {

			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()));
		}
		return returnMap;
	}

	/**
	 * This interface declaration is used to retrieve list of all active Users Based
	 * on Section for the specified site through its DAO layer
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         Users Based on Section
	 * 
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getSectionBasedUser(int sectionCode, int nregionalsitecode, UserInfo userInfo)
			throws Exception {
		final String strQuery = " select  u.nusercode,CONCAT(u.sfirstname,' ',u.slastname) as susername "
				+ " from sectionusers su,users u , labsection ls"
				+ " where u.nusercode=su.nusercode and su.nlabsectioncode=ls.nlabsectioncode and ls.nsitecode=su.nsitecode"
				+ " and su.nsitecode=" + nregionalsitecode + " and  ls.nsectioncode=" + sectionCode + ""
				+ " and su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by u.nusercode";

		return new ResponseEntity<>((List<SectionUsers>) jdbcTemplate.query(strQuery, new SectionUsers()),
				HttpStatus.OK);

	}

	/**
	 * This method declaration is used to retrieve list of all active
	 * InstrumentSection based on Defaultstatus for the specified site through its
	 * DAO layer
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         InstrumentSection based on Defaultstatus
	 * 
	 * @throws Exception that are thrown in the DAO layer
	 */

	private InstrumentSection getInstrumentSectionByDefaultStatus(int nsiteCode, int ninstrumentcode) throws Exception {

		final String instSecQuery = "select * from instrumentsection    where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode = " + nsiteCode
				+ " and ninstrumentcode=" + ninstrumentcode + "";

		return (InstrumentSection) jdbcUtilityFunction.queryForObject(instSecQuery, InstrumentSection.class,
				jdbcTemplate);

	}

	/**
	 * This method declaration is used to retrieve list of all active
	 * InstrumentSection for the specified site through its DAO layer
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         Instrument Section
	 * 
	 * @throws Exception that are thrown in the DAO layer
	 */

	public List<InstrumentSection> getInstrumentSection(final int instCode, UserInfo userInfo) throws Exception {

		final String query = " select i.ninstrumentcode,sc.ssectionname,ins.nsectioncode,ins.nusercode,ins.ndefaultstatus,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, "
				+ " CONCAT(u.sfirstname,' ',u.slastname) as susername,ins.ninstrumentsectioncode,ins.ndefaultstatus from instrument i,"
				+ " instrumentsection ins,section sc,users u,transactionstatus ts where ins.nsectioncode=sc.nsectioncode "
				+ " and ins.ninstrumentcode= " + instCode + "  and i.ninstrumentcode=ins.ninstrumentcode "
				+ " and ts.ntranscode=ins.ndefaultstatus and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and u.nusercode=ins.nusercode and ins.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ins.nsitecode = "
				+ userInfo.getNtranssitecode();

		return (List<InstrumentSection>) jdbcTemplate.query(query, new InstrumentSection());

	}

	/**
	 * This method declaration is used to retrieve list of all active Users for the
	 * specified site through its DAO layer
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         Users
	 * 
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getUsers(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> objMap = new HashMap<>();

		String strQuery = "select s.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
				+ "from section s,transactionstatus ts, labsection ls where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nsectioncode > 0 and s.nsectioncode=ls.nsectioncode " + " and ts.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ls.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.ndefaultstatus=ts.ntranscode"
				+ " and ls.nsitecode = " + (int) inputMap.get("nregionalsitecode")
				+ " group by s.nsectioncode, sdisplaystatus";

		int statusCheck = jdbcTemplate.queryForObject(
				"select ninstrumentstatus from instrument where ninstrumentcode=" + inputMap.get("ninstrumentcode"),
				Integer.class);
		if (statusCheck != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {
			final List<Section> lstSection = (List<Section>) jdbcTemplate.query(strQuery, new Section());

			objMap.put("Section", lstSection);
			if (!lstSection.isEmpty()) {
				final List<Section> lstSectionDefault = lstSection.stream()
						.filter(x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
						.collect(Collectors.toList());
				if (!lstSectionDefault.isEmpty()) {

					strQuery = " select  u.nusercode,CONCAT(u.sfirstname,' ',u.slastname) as susername "
							+ " from sectionusers su,users u , labsection ls"
							+ " where u.nusercode=su.nusercode and su.nlabsectioncode=ls.nlabsectioncode "
							+ " and  ls.nsectioncode=" + lstSectionDefault.get(0).getNsectioncode() + ""
							+ " and su.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and ls.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by u.nusercode ";

					final List<Users> lstUsers = (List<Users>) jdbcTemplate.query(strQuery, new Users());
					objMap.put("Users", lstUsers);
				} else {
					objMap.put("Users", lstSectionDefault);
				}
			} else {
				objMap.put("Users", lstSection);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to instrumentsection table. Need to
	 * check for duplicate entry of section name for the specified site before
	 * saving into database. Need to check that there should be only one default
	 * section for a site
	 * 
	 * @param inst [InstrumentSection] object holding details to be added in unit
	 *             table
	 * @return inserted unit object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSection(InstrumentSection inst, final UserInfo userInfo) throws Exception {

		final Instrument objInst = (Instrument) getActiveInstrumentById(inst.getNinstrumentcode(), userInfo);

		if (objInst == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String sQueryLock = " lock  table instrumentsection "
					+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQueryLock);

			Map<String, Object> objMap = new LinkedHashMap<String, Object>();
			final InstrumentSection instSecByName = getInstrumentSectionByName(inst.getNusercode(),
					inst.getNsectioncode(), inst.getNinstrumentcode());
			if (instSecByName == null) {
				String sectSeq = "select nsequenceno from seqnoinstrumentmanagement where stablename='instrumentsection'";
				SeqNoInstrumentManagement objseq = (SeqNoInstrumentManagement) jdbcUtilityFunction
						.queryForObject(sectSeq, SeqNoInstrumentManagement.class, jdbcTemplate);
				int seqNo = objseq.getNsequenceno();
				seqNo = seqNo + 1;
				String instSectionInst = "";

				final InstrumentSection defaultInst = getInstrumentSecionByDefaultStatus(userInfo.getNsitecode(),
						inst.getNinstrumentcode(), inst.getNinstrumentsectioncode());

				if (defaultInst != null) {

					if (seqNo != 0) {
						instSectionInst = " insert into instrumentsection(ninstrumentsectioncode,ninstrumentcode,"
								+ " nsectioncode,nusercode,ndefaultstatus,dmodifieddate,nsitecode,nstatus)" + " values("
								+ seqNo + "," + inst.getNinstrumentcode() + "," + inst.getNsectioncode() + "," + " "
								+ inst.getNusercode() + "," + Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNsitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
						jdbcTemplate.execute(instSectionInst);
					}

					instSectionInst = "update seqnoinstrumentmanagement set nsequenceno=" + seqNo
							+ " where stablename='instrumentsection'";
					jdbcTemplate.execute(instSectionInst);

					inst.setNinstrumentsectioncode(seqNo);

				} else {

					if (seqNo != 0) {
						instSectionInst = " insert into instrumentsection(ninstrumentsectioncode,ninstrumentcode,"
								+ " nsectioncode,nusercode,ndefaultstatus,dmodifieddate,nsitecode,nstatus)" + " values("
								+ seqNo + "," + inst.getNinstrumentcode() + "," + inst.getNsectioncode() + "," + " "
								+ inst.getNusercode() + "," + Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNsitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
						jdbcTemplate.execute(instSectionInst);
					}

					instSectionInst = "update seqnoinstrumentmanagement set nsequenceno=" + seqNo
							+ " where stablename='instrumentsection'";
					jdbcTemplate.execute(instSectionInst);

					inst.setNinstrumentsectioncode(seqNo);
				}

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedInstrumentSectionList = new ArrayList<>();
				final List<InstrumentSection> selectedSection = getInstrumentSection(inst.getNinstrumentcode(),
						userInfo);
				objMap.put("selectedSection", selectedSection);
				inst.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
				savedInstrumentSectionList.add(inst);
				multilingualIDList.add("IDS_ADDINSTRUMENTSECTION");
				auditUtilityFunction.fnInsertAuditAction(savedInstrumentSectionList, 1, null, multilingualIDList,
						userInfo);
				return new ResponseEntity<>(objMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}

	}

	/**
	 * This method id used to delete an entry in instrumentsection table Need to
	 * check the record is already deleted or not
	 * 
	 * @param instSec [InstrumentSection] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an unit object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteSection(InstrumentSection instSec, final UserInfo userInfo) throws Exception {

		final InstrumentSection instByID = (InstrumentSection) getActiveInstrumentSectionById(
				instSec.getNinstrumentsectioncode(), userInfo);

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		if (instByID == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			int statusCheck = 0;

			statusCheck = jdbcTemplate.queryForObject(
					"select ninstrumentstatus from instrument where ninstrumentcode=" + instSec.getNinstrumentcode(),
					Integer.class);

			if (statusCheck != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {

				boolean validRecord = false;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(instSec.getNinstrumentsectioncode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
				if (validRecord) {
					// ALPD-4469
					/*
					 * final InstrumentSection defaultInst =
					 * getInstrumentSecionByDefaultStatus(userInfo.getNsitecode(),
					 * instSec.getNinstrumentcode(), instSec.getNinstrumentsectioncode()); if
					 * (defaultInst != null) { return new
					 * ResponseEntity<>(commonFunction.getMultilingualMessage(
					 * Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(),
					 * userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED); } else {
					 */
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> savedInstrumentSectionList = new ArrayList<>();
					final String updateQueryString = "update instrumentsection set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentsectioncode="
							+ instSec.getNinstrumentsectioncode() + "";

					jdbcTemplate.execute(updateQueryString);

					savedInstrumentSectionList.add(instSec);
					multilingualIDList.add("IDS_DELETEINSTRUMENTSECTION");

					auditUtilityFunction.fnInsertAuditAction(savedInstrumentSectionList, 1, null, multilingualIDList,
							userInfo);
					final List<InstrumentSection> selectedSection = getInstrumentSection(instSec.getNinstrumentcode(),
							userInfo);
					objMap.put("selectedSection", selectedSection);
					return new ResponseEntity<>(objMap, HttpStatus.OK);

				} else {
					return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to fetch the active unit objects for the specified
	 * Instrument Section and site.
	 * 
	 * @param ninstrumentsectioncode [int] code of the instrumentsection
	 * @param userInfo               [UserInfo] site code of the instrumentsection
	 * @return list of active unit code(s) based on the specified Instrument Section
	 *         and site
	 * @throws Exception
	 */
	public InstrumentSection getActiveInstrumentSectionById(final int ninstrumentsectioncode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select ins.ninstrumentsectioncode,CONCAT(u.sfirstname,' ',u.slastname) as susername"
				+ " from instrument i,instrumentsection ins,users u  where ins.ninstrumentsectioncode="
				+ ninstrumentsectioncode + " and  "
				+ " ins.ninstrumentcode=i.ninstrumentcode and ins.nusercode=u.nusercode  and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ins.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		InstrumentSection objInst = (InstrumentSection) jdbcUtilityFunction.queryForObject(strQuery,
				InstrumentSection.class, jdbcTemplate);

		return objInst;
	}

	/**
	 * This method is used to fetch the active unit objects for the specified
	 * Instrument Section based on Name and site.
	 * 
	 * @param nusercode       [int] code of the users
	 * @param nsectioncode    [int] code of the section
	 * @param ninstrumentcode [int] code of the instrumentsection
	 * @return list of active nsectioncode code(s) based on the specified Instrument
	 *         Section based on Name and site
	 * @throws Exception
	 */

	public InstrumentSection getInstrumentSectionByName(final int nusercode, final int nsectioncode,
			final int ninstrumentcode) throws Exception {

		String strQuery = "select ins.ninstrumentsectioncode,ins.ninstrumentcode,se.ssectionname,CONCAT(u.sfirstname,' ',u.slastname) susername "
				+ " from instrumentsection ins,section se,users u " + " where ins.nsectioncode = " + nsectioncode
				+ " and u.nusercode=ins.nusercode and" + " ins.nusercode=" + nusercode
				+ " and se.nsectioncode=ins.nsectioncode and ins.ninstrumentcode=" + ninstrumentcode + ""
				+ " and ins.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		return (InstrumentSection) jdbcUtilityFunction.queryForObject(strQuery, InstrumentSection.class, jdbcTemplate);
	}

	/**
	 * This method is used to set DefaultSection of instrumentsection
	 * 
	 * @param objSection [InstrumentSection] object of the instrumentsection
	 * @return list of active nsectioncode code(s) based on the specified Instrument
	 *         Section based on Name and site
	 * @return response entity object holding response status and list of all active
	 *         DefaultSection
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> setDefaultSection(InstrumentSection objSection, final UserInfo objUserInfo)
			throws Exception {
		final InstrumentSection objLstSection = sectionDuplicateCheck(objSection.getNinstrumentsectioncode(),
				objSection.getNusercode());
		if (objLstSection != null) {

			String sQuery = "select ins.*,se.ssectionname,CONCAT(u.sfirstname,' ',u.slastname) susername,"
					+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + objUserInfo.getSlanguagetypecode() + "',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus "
					+ " from instrumentsection ins,section se,users u,transactionstatus ts  "
					+ " where ninstrumentsectioncode = " + objLstSection.getNinstrumentsectioncode()
					+ " and se.nsectioncode=ins.nsectioncode and u.nusercode=ins.nusercode and "
					+ " ts.ntranscode=ins.ndefaultstatus and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ins.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and se.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and se.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			final InstrumentSection objSectionOld = (InstrumentSection) jdbcUtilityFunction.queryForObject(sQuery,
					InstrumentSection.class, jdbcTemplate);

			if (objSectionOld != null) {
				if (objSectionOld.getNdefaultstatus() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstOldObject = new ArrayList<>();
					final List<Object> lstNewObject = new ArrayList<>();

					sQuery = "select * from instrumentsection  where " + " nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ninstrumentsectioncode <> " + objSectionOld.getNinstrumentsectioncode()
							+ " and ninstrumentcode=" + objSectionOld.getNinstrumentcode() + ""
							+ " and ndefaultstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and nsitecode=" + objUserInfo.getNtranssitecode() + "";

					final List<InstrumentSection> lstInstSection = (List<InstrumentSection>) jdbcTemplate.query(sQuery,
							new InstrumentSection());

					if (lstInstSection != null && lstInstSection.size() > 0) {
						InstrumentSection objNewTestSection = new InstrumentSection();
						objNewTestSection = lstInstSection.get(0);
						final String sUpdateQuery = "update instrumentsection set ndefaultstatus = "
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo)
								+ "' where ninstrumentsectioncode = " + objNewTestSection.getNinstrumentsectioncode()
								+ ";";
						objNewTestSection
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						jdbcTemplate.execute(sUpdateQuery);
						multilingualIDList.add("IDS_EDITINSTRUMENTSECTION");
					}
					final String sUpdateQuery1 = "update instrumentsection set ndefaultstatus ="
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ninstrumentsectioncode = "
							+ objSection.getNinstrumentsectioncode() + ";";
					jdbcTemplate.execute(sUpdateQuery1);
					multilingualIDList.add("IDS_EDITINSTRUMENTSECTION");
					lstNewObject.add(objSection);
					lstOldObject.add(objSectionOld);
					auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList,
							objUserInfo);

					return getInstrument(objSection.getNinstrumentcode(), objUserInfo);
				} else {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDEFAULT.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {

			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to the check the section Duplicate or not.
	 * 
	 * @param instrumentsectioncode [int] code of the instrumentsection
	 * @throws Exception
	 */

	public InstrumentSection sectionDuplicateCheck(final int instrumentsectioncode, final int nusercode)
			throws Exception {

		final String strQuery = "select ins.ninstrumentsectioncode from instrumentsection ins "
				+ " where ins.ninstrumentsectioncode=" + instrumentsectioncode + " and ins.nusercode=" + nusercode + ""
				+ " and ins.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		return (InstrumentSection) jdbcUtilityFunction.queryForObject(strQuery, InstrumentSection.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active InstrumentSecion objects for the
	 * specified InstrumentSecion based on DefaultStatus and site.
	 * 
	 * @param ninstrumentcode        [int] code of the instrumentsection
	 * @param ninstrumentcode        [int] instrumnet code of the instrumentsection
	 * @param ninstrumentsectioncode [int] code of the instrumentsection
	 * @return list of active nsectioncode code(s) based on the specified Instrument
	 *         Section based on DefaultStatus and site
	 * @throws Exception
	 */

	private InstrumentSection getInstrumentSecionByDefaultStatus(final int nsiteCode, final int ninstrumentcode,
			int ninstrumentsectioncode) throws Exception {
		final String strQuery = "select i.* from instrumentsection i where i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and i.nsitecode = " + nsiteCode
				+ " and  i.ninstrumentcode=" + ninstrumentcode + " and i.ninstrumentsectioncode="
				+ ninstrumentsectioncode + "";

		return (InstrumentSection) jdbcUtilityFunction.queryForObject(strQuery, InstrumentSection.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active Manufacturer objects for the
	 * specified Manufacturer and site.
	 * 
	 * @param userInfo [UserInfo] object of the users
	 * @return list of active records based on the specified Manufacturer and site
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> getManufacturer(final Integer ncontrolCode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();

		final String strQuery = " select a.*," + " coalesce(ts.jsondata->'sactiondisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as stransdisplaystatus "
				+ " from manufacturer a,transactionstatus ts where ts.ntranscode = a.ntransactionstatus "
				+ " and a.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.nsitecode = " + userInfo.getNmastersitecode() + " and a.nmanufcode > 0  and a.nsitecode = "
				+ userInfo.getNmastersitecode();

		final List<Manufacturer> lstManufacturerGet = (List<Manufacturer>) jdbcTemplate.query(strQuery,
				new Manufacturer());
		objMap.put("Manufacturer", lstManufacturerGet);
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);

	}

	/**
	 * This interface declaration is used to retrieve list of all active Supplier
	 * for the specified site through its DAO layer
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         Instrument Category
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getSupplier(final Integer ncontrolCode, final UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();

		final String strQuery = "select a.*, coalesce(ts.jsondata->'sactiondisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as stransdisplaystatus, "
				+ " c.scountryname scountryname from supplier a, transactionstatus ts,transactionstatus ts1,Country c where a.napprovalstatus = ts.ntranscode and nsuppliercode > 0 "
				+ " and c.nsitecode = a.nsitecode and c.nstatus = a.nstatus and c.ncountrycode = a.ncountrycode  and  a.ntransactionstatus = ts1.ntranscode "
				+ " and a.ntransactionstatus <> " + Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()
				+ " and a.napprovalstatus not in (" + Enumeration.TransactionStatus.BLACKLIST.gettransactionstatus()
				+ "," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ")  and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode = "
				+ userInfo.getNmastersitecode();

		final List<Supplier> lstSupplierGet = (List<Supplier>) jdbcTemplate.query(strQuery, new Supplier());
		objMap.put("Supplier", lstSupplierGet);
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> addInstrmentDate(Integer ncontrolCode, UserInfo userInfo) throws Exception {
		Map<String, Object> returnobj = new HashMap<>();
		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		final Map<String, Object> outputMap = projectDAOSupport.getDateFromControlProperties(userInfo, "", // currentUIDate,
				"", "ExpiryDate");
		final String ExpiryDateUI = LocalDateTime.parse((String) outputMap.get("ExpiryDate"), dbPattern)
				.format(uiPattern);
		returnobj.put("ExpiryDate", ExpiryDateUI);

		return new ResponseEntity<Object>(returnobj, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to retrieve list of all active
	 * InstrumentValidationStatus for the specified site through its DAO layer
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         Instrument ValidationStatus
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getInstrumentValidationStatus(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnobj = new HashMap<>();
		int statusCheck = 0;
		if (inputMap.get("ninstrumentcode") != null) {
			statusCheck = jdbcTemplate.queryForObject(
					"select ninstrumentstatus from instrument where ninstrumentcode=" + inputMap.get("ninstrumentcode"),
					Integer.class);
		}
		if (statusCheck != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {

			final String strQuery = " select ntranscode, coalesce(jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " jsondata->'stransdisplaystatus'->>'en-US') as stransstatus " + "  from transactionstatus where "
					+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntranscode in ("
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.UNDERVALIDATION.gettransactionstatus() + ");";

			returnobj.put("ValidationStatus", jdbcTemplate.query(strQuery, new TransactionStatus()));

			return new ResponseEntity<>(returnobj, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This interface declaration is used to retrieve list of all active Instrument
	 * Validation for the specified site through its DAO layer
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         Instrument Validation
	 * @throws Exception that are thrown in the DAO layer
	 */

	public List<InstrumentValidation> getInstrumentValidation(final int instCode, final int ninstrumentvalidationcode,
			final UserInfo userInfo) throws Exception {

		String query = "select iv.noffsetdvalidationdate,iv.ninstrumentvalidationcode,i.sinstrumentid,inn.sinstrumentname,inn.ninstrumentnamecode,iv.ninstrumentcode,iv.sremark,iv.nvalidationstatus ,"
				+ " iv.nstatus,tz.stimezoneid as stzvalidationdate, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, "
				+ " COALESCE(TO_CHAR(iv.dvalidationdate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as svalidationdate," + "COALESCE(TO_CHAR(iv.dvalidationdate,'" + userInfo.getSsitedate()
				+ "'),'')||'-'||" + "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + " ts.jsondata->'stransdisplaystatus'->>'en-US') as sheadername"
				+ " from instrumentvalidation iv,transactionstatus ts,instrument i,timezone tz,instrumentname inn  "
				+ " where "
				+ " i.ninstrumentcode=iv.ninstrumentcode and i.ninstrumentnamecode=inn.ninstrumentnamecode and iv.ntzvalidationdate=tz.ntimezonecode and "
				+ " iv.ninstrumentcode=" + instCode + " and iv.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nsitecode = "
				+ userInfo.getNtranssitecode() + " and iv.nvalidationstatus=ts.ntranscode";

		if (ninstrumentvalidationcode > 0) {
			query = query + " and iv.ninstrumentvalidationcode = " + ninstrumentvalidationcode;
		}
		query += " order by iv.ninstrumentvalidationcode asc;";

		final ObjectMapper objMapper = new ObjectMapper();

		objMapper.registerModule(new JavaTimeModule());

		final List<InstrumentValidation> lstInstValGet = objMapper
				.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(
								jdbcTemplate.query(query, new InstrumentValidation()), Arrays.asList("svalidationdate"),
								Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
						new TypeReference<List<InstrumentValidation>>() {
						});

		return lstInstValGet;
	}

	/**
	 * This method is used to add a new entry to instrumentvalidation table. Need to
	 * check for duplicate entry of Instrumentvalidation name for the specified site
	 * before saving into database.
	 * 
	 * @param instVal [InstrumentValidation] object holding details to be added in
	 *                instrumentvalidation table
	 * @return inserted instrumentvalidation object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createInstrumentValidation(InstrumentValidation instVal, final UserInfo userInfo)
			throws Exception {

		final String sQueryLock = " lock  table lockinstrument " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<Object> savedInstList = new ArrayList<>();
		if (instVal.getDvalidationdate() != null) {
			instVal.setSvalidationdate(
					dateUtilityFunction.instantDateToString(instVal.getDvalidationdate()).replace("T", " "));
		}

		final Instrument instrument = checkMainInstrumentIsPresent(instVal.getNinstrumentcode());
		if (instrument != null) {
			final String ValSeq = "select nsequenceno from seqnoinstrumentmanagement where stablename='instrumentvalidation'";
			final SeqNoInstrumentManagement objseq = (SeqNoInstrumentManagement) jdbcUtilityFunction
					.queryForObject(ValSeq, SeqNoInstrumentManagement.class, jdbcTemplate);
			int seqNo = objseq.getNsequenceno();
			seqNo = seqNo + 1;
			String instValidation = "";
			String remarks = instVal.getSremark() == null ? "" : instVal.getSremark();

			String svalidationdate = instVal.isIsreadonly()
					? "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
					: "'" + instVal.getSvalidationdate() + "'";

			int offset = instVal.isIsreadonly()
					? dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					: dateUtilityFunction.getCurrentDateTimeOffsetFromDate(
							instVal.getSvalidationdate().replace("T", " "), userInfo.getStimezoneid());

			instValidation = "insert into instrumentvalidation(ninstrumentvalidationcode,ninstrumentcode,"
					+ " nusercode,sremark,dvalidationdate,noffsetdvalidationdate,ntzvalidationdate,nvalidationstatus,dmodifieddate,nsitecode,nstatus)"
					+ " values(" + seqNo + "," + instVal.getNinstrumentcode() + "," + instVal.getNusercode() + ",'"
					+ stringUtilityFunction.replaceQuote(remarks) + "'," + svalidationdate + "," + offset + ","
					+ instVal.getNtzvalidationdate() + "," + instVal.getNvalidationstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNsitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(instValidation);

			instValidation = "update seqnoinstrumentmanagement set nsequenceno=" + seqNo
					+ " where stablename='instrumentvalidation'";
			jdbcTemplate.execute(instValidation);

			instVal.setNinstrumentvalidationcode(seqNo);

			final List<InstrumentValidation> lstInstVal = getInstrumentValidation(instVal.getNinstrumentcode(), 0,
					userInfo);
			if (!lstInstVal.isEmpty()) {
				objMap.put("selectedInstrumentValidation", lstInstVal.get(lstInstVal.size() - 1));
				objMap.putAll(getInstrumentValidationFile(
						lstInstVal.get(lstInstVal.size() - 1).getNinstrumentvalidationcode(), userInfo));
				objMap.put("InstrumentValidation", lstInstVal);
			} else {
				objMap.put("selectedInstrumentValidation", lstInstVal);
				objMap.put("InstrumentValidation", lstInstVal);

			}
			if (instVal.getDvalidationdate() != null) {
				Date date = Date.from(instVal.getDvalidationdate());
				instVal.setSvalidationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
			}
			savedInstList.add(instVal);
			auditUtilityFunction.fnInsertAuditAction(savedInstList, 1, null,
					Arrays.asList("IDS_ADDINSTRUMENTVALIDATION"), userInfo);

			return new ResponseEntity<>(objMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	/**
	 * This method is used to update entry in instrumentvalidation table. Need to
	 * validate that the instrumentvalidation object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * instrumentvalidation name for the specified site before saving into database.
	 * 
	 * @param instVal [InstrumentValidation] object holding details to be updated in
	 *                unit table
	 * @return response entity object holding response status and data of updated
	 *         unit object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateInstrumentValidation(InstrumentValidation instVal, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final List<Object> beforeSavedInstList = new ArrayList<>();
		final List<Object> savedInstList = new ArrayList<>();
		if (instVal.getDvalidationdate() != null) {
			instVal.setSvalidationdate(dateUtilityFunction.instantDateToString(instVal.getDvalidationdate())
					.replace("T", " ").replace("Z", ""));
		}
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final InstrumentValidation convertedObject = objMapper
				.convertValue(
						dateUtilityFunction.convertInputDateToUTCByZone(instVal, Arrays.asList("svalidationdate"),
								Arrays.asList("ntzvalidationdate"), true, userInfo),
						new TypeReference<InstrumentValidation>() {
						});
		final InstrumentValidation instval = (InstrumentValidation) getActiveInstrumentValidationById(
				instVal.getNinstrumentvalidationcode(), userInfo);
		if (instVal.getDvalidationdate() != null) {
			Date date = Date.from(instval.getDvalidationdate());
			instval.setSvalidationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		if (instval == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ninstrumentvalidationcode from instrumentvalidation where dvalidationdate = '"
					+ instVal.getSvalidationdate() + "' and nvalidationstatus=" + instVal.getNvalidationstatus()
					+ " and ninstrumentvalidationcode <> " + instVal.getNinstrumentvalidationcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<InstrumentValidation> instvalList = (List<InstrumentValidation>) jdbcTemplate.query(queryString,
					new InstrumentValidation());
			if (instvalList.isEmpty()) {
				String query = "update instrumentvalidation set " + "sremark=N'"
						+ stringUtilityFunction.replaceQuote(instVal.getSremark()) + "'," + "dvalidationdate='"
						+ convertedObject.getSvalidationdate() + "',noffsetdvalidationdate="
						+ convertedObject.getNoffsetdvalidationdate() + ",nvalidationstatus="
						+ instVal.getNvalidationstatus() + "," + "ntzvalidationdate="
						+ convertedObject.getNtzvalidationdate() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentvalidationcode="
						+ instVal.getNinstrumentvalidationcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(query);

				final List<InstrumentValidation> lstInstrumentValidation = getInstrumentValidation(
						instVal.getNinstrumentcode(), instVal.getNinstrumentvalidationcode(), userInfo);

				objMap.put("selectedInstrumentValidation",
						lstInstrumentValidation.get(lstInstrumentValidation.size() - 1));
				if (instVal.getDvalidationdate() != null) {
					Date date = Date.from(instVal.getDvalidationdate());
					instVal.setSvalidationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				savedInstList.add(instVal);
				beforeSavedInstList.add(instval);

				auditUtilityFunction.fnInsertAuditAction(savedInstList, 2, beforeSavedInstList,
						Arrays.asList("IDS_EDITINSTRUMENTVALIDATION"), userInfo);

				return new ResponseEntity<>(objMap, HttpStatus.OK);
			}

			else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("ALREADYEXIST", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in instrumentvalidation table Need to
	 * check the record is already deleted or not
	 * 
	 * @param objInstVal [InstrumentValidation] an Object holds the record to be
	 *                   deleted
	 * @return a response entity with corresponding HTTP status and an unit object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteInstrumentValidation(InstrumentValidation objInstVal, final UserInfo userInfo)
			throws Exception {
		final List<Object> deletedInstList = new ArrayList<>();

		final String sQuery = "select *,COALESCE(TO_CHAR(dvalidationdate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as svalidationdate from instrumentvalidation where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ninstrumentvalidationcode = "
				+ objInstVal.getNinstrumentvalidationcode() + " and nsitecode=" + userInfo.getNtranssitecode() + "";

		final InstrumentValidation oldInstVal = (InstrumentValidation) jdbcUtilityFunction.queryForObject(sQuery,
				InstrumentValidation.class, jdbcTemplate);
		if (oldInstVal != null) {

			int statusCheck = jdbcTemplate.queryForObject(
					"select ninstrumentstatus from instrument where ninstrumentcode=" + objInstVal.getNinstrumentcode(),
					Integer.class);
			if (statusCheck != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {

				boolean validRecord = false;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objInstVal.getNinstrumentvalidationcode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
				if (validRecord) {

					String updateQuery = "update instrumentvalidation set nstatus = "
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentvalidationcode = "
							+ objInstVal.getNinstrumentvalidationcode() + ";";

					final String qry = "select * from instrumentfile where ninstrumentlogcode = "
							+ objInstVal.getNinstrumentvalidationcode() + " and ninstrumentlogtypecode= "
							+ Enumeration.InstrumentLogType.INSTRUMENT_VALIDATION.getinstrumentlogtype()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";
					final List<InstrumentFile> oldInstrumentFile = (List<InstrumentFile>) jdbcTemplate.query(qry,
							new InstrumentFile());

					updateQuery = updateQuery + "update instrumentfile set nstatus = "
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentlogcode = "
							+ objInstVal.getNinstrumentvalidationcode() + " and ninstrumentlogtypecode= "
							+ Enumeration.InstrumentLogType.INSTRUMENT_VALIDATION.getinstrumentlogtype()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

					jdbcTemplate.execute(updateQuery);

					final List<String> multilingualIDList = new ArrayList<>();

					Map<String, Object> outMap = new HashMap<String, Object>();
					final List<InstrumentValidation> lstInstVal = getInstrumentValidation(
							objInstVal.getNinstrumentcode(), 0, userInfo);
					if (lstInstVal != null && lstInstVal.size() > 0) {
						outMap.put("selectedInstrumentValidation", lstInstVal.get(lstInstVal.size() - 1));
						outMap.putAll(getInstrumentValidationFile(
								lstInstVal.get(lstInstVal.size() - 1).getNinstrumentvalidationcode(), userInfo));
					}
					outMap.put("InstrumentValidation", lstInstVal);
					if (oldInstVal.getDvalidationdate() != null) {
						Date date = Date.from(oldInstVal.getDvalidationdate());
						oldInstVal.setSvalidationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}

					deletedInstList.add(oldInstrumentFile);
					final List<InstrumentValidation> lst = new ArrayList<>();
					lst.add(oldInstVal);
					deletedInstList.add(lst);

					multilingualIDList.add("IDS_DELETEINSTRUMENTVALIDATIONFILE/LINK");
					multilingualIDList.add("IDS_DELETEINSTRUMENTVALIDATION");
					auditUtilityFunction.fnInsertListAuditAction(deletedInstList, 1, null, multilingualIDList,
							userInfo);

					return new ResponseEntity<Object>(outMap, HttpStatus.OK);

				} else {

					return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to fetch the active InstrumentValidation objects for the
	 * specified InstrumentValidation name and site.
	 * 
	 * @param ninstrumentvalidationcode [int] code of the unit
	 * @param userInfo                  [UserInfo] Object of the userInfo
	 * @return list of active ninstrumentvalidationcode code(s) based on the
	 *         specified ninstrumentvalidationcode name and site
	 * @throws Exception
	 */

	@Override
	public InstrumentValidation getActiveInstrumentValidationById(final int ninstrumentvalidationcode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select  i.ninstrumentstatus,iv.dvalidationdate, iv.ninstrumentvalidationcode,i.sinstrumentid,iv.ninstrumentcode,iv.sremark,"
				+ " iv.nvalidationstatus,iv.ntzvalidationdate,tz.stimezoneid as stzvalidationdate, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, "
				+ " COALESCE(TO_CHAR(iv.dvalidationdate,'" + userInfo.getSpgsitedatetime() + "'),'') as svalidationdate"
				+ " from instrument i,instrumentvalidation iv ,transactionstatus ts ,timezone tz "
				+ " where iv.ninstrumentvalidationcode=" + ninstrumentvalidationcode + " and  "
				+ " iv.ninstrumentcode=i.ninstrumentcode " + "and iv.nvalidationstatus=ts.ntranscode "
				+ " and iv.ntzvalidationdate=tz.ntimezonecode" + " and iv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nsitecode = "
				+ userInfo.getNtranssitecode() + "";

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<InstrumentValidation> objInstVal = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new InstrumentValidation()),
						Arrays.asList("svalidationdate"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false,
						null, false),
				new TypeReference<List<InstrumentValidation>>() {
				});
		if (objInstVal.isEmpty()) {
			InstrumentValidation InstrumentValidation = null;
			return InstrumentValidation;
		} else {
			return (InstrumentValidation) objInstVal.get(0);

		}
	}

	/**
	 * This method is used to fetch the active InstrumentValidation based on date
	 * objects for the specified InstrumentValidation name and site.
	 * 
	 * @param nvalidationstatus [int] code of the instrumentvalidation
	 * @param validationdate    [String] name of the instrumentvalidation
	 * @param ninstrumentcode   [int] code of the instrument
	 * @return list of active nvalidationstatus code(s) based on the specified
	 *         InstrumentValidationListByDate and site
	 * @throws Exception
	 */

	public InstrumentValidation getInstrumentValidationListByDate(final int nvalidationstatus,
			final String validationdate, final int ninstrumentcode) throws Exception {

		final String query = "select * from instrumentvalidation " + "where dvalidationdate='" + validationdate
				+ "' and nvalidationstatus=" + nvalidationstatus + " and ninstrumentcode=" + ninstrumentcode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		return (InstrumentValidation) jdbcUtilityFunction.queryForObject(query, InstrumentValidation.class,
				jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getOtherTabDetails(final int nFlag, final int ninstrumentcode,
			final int tabprimarycode, final UserInfo objUserInfo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		final Instrument instrument = checkMainInstrumentIsPresent(ninstrumentcode);
		if (instrument != null) {
			switch (nFlag) {
			case 1:
				final List<InstrumentValidation> lstInstValDetails = getInstrumentValidation(ninstrumentcode,
						tabprimarycode, objUserInfo);
				map.put("selectedInstrumentValidation", lstInstValDetails.get(lstInstValDetails.size() - 1));
				map.putAll(getInstrumentValidationFile(tabprimarycode, objUserInfo));

				break;

			case 2:
				final List<InstrumentCalibration> lstInstCalDetails = getInstrumentCalibration(ninstrumentcode,
						tabprimarycode, objUserInfo);
				map.put("selectedInstrumentCalibration", lstInstCalDetails.get(lstInstCalDetails.size() - 1));
				map.putAll(getInstrumentCalibrationFile(tabprimarycode, objUserInfo));

				break;
			case 3:
				final List<InstrumentMaintenance> lstInstMainDetails = getInstrumentMaintenance(ninstrumentcode,
						tabprimarycode, objUserInfo);
				map.put("selectedInstrumentMaintenance", lstInstMainDetails.get(lstInstMainDetails.size() - 1));
				map.putAll(getInstrumentMaintenanceFile(tabprimarycode, objUserInfo));

				break;
			default:
				break;

			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	public Map<String, Object> getInstrumentValidationFile(final int ninstrumentlogcode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String queryformat = "COALESCE(TO_CHAR(dcreateddate,'" + objUserInfo.getSpgdatetimeformat() + "'),'-') ";

		final String query = "select insf.ninstrumentfilecode, insf.ninstrumentlogcode, insf.ninstrumentlogtypecode, "
				+ " insf.nattachmenttypecode, insf.sfilename, insf.sfiledesc, insf.ssystemfilename, insf.nfilesize, insf.nsitecode,"
				+ " insf.nstatus, case when insf.nlinkcode = -1 then cast(insf.nfilesize as text) else '-' end sfilesize,"
				+ " case when insf.nlinkcode = -1 then " + queryformat + " else '-' end  screateddate, "
				+ " case when insf.nlinkcode = -1 then '-' else lm.jsondata->>'slinkname' end slinkname, insf.noffsetdcreateddate "
				+ " from instrumentfile insf, linkmaster lm where insf.ninstrumentlogcode=" + ninstrumentlogcode
				+ " and insf.ninstrumentlogtypecode=1 " + " and insf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and insf.nlinkcode=lm.nlinkcode and "
				+ "insf.nstatus=lm.nstatus";

		outputMap.put("ValidationFileData",
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new InstrumentFile()),
						Arrays.asList("screateddate"), Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false,
						null, false));

		return outputMap;

	}

	/**
	 * This method is used to add a new entry to InstrumentFile table. Need to check
	 * for whether instrument file is available or not.
	 * 
	 * @param objUserInfo [UserInfo] object of the UserInfo
	 * @return inserted instrumentfile object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createInstrumentFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {

		final String sQueryLock = " lock  table instrumentfile "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final ObjectMapper objMapper = new ObjectMapper();
		final List<InstrumentFile> lstReqInstFile = objMapper.readValue(request.getParameter("instrumentFile"),
				new TypeReference<List<InstrumentFile>>() {
				});

		final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo);
		final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
		final String screatedDate = dateUtilityFunction.instantDateToString(instantDate);

		lstReqInstFile.forEach(objtf -> {
			objtf.setDcreateddate(instantDate);
			if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					|| objtf.getNattachmenttypecode() == Enumeration.AttachmentType.LINK.gettype()) {
				objtf.setDcreateddate(instantDate);
				objtf.setNoffsetdcreateddate(noffset);
				objtf.setScreateddate(screatedDate.replace("T", " "));
			}

		});

		int seqNo = 0;
		if (lstReqInstFile != null && lstReqInstFile.size() > 0) {
			final InstrumentValidation objInstVal = checkInstrumentValidationIsPresent(
					lstReqInstFile.get(0).getNinstrumentlogcode());

			if (objInstVal != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqInstFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {

					final String instValSeq = "select nsequenceno from seqnoinstrumentmanagement where stablename='instrumentfile'";
					SeqNoInstrumentManagement objseq = (SeqNoInstrumentManagement) jdbcUtilityFunction
							.queryForObject(instValSeq, SeqNoInstrumentManagement.class, jdbcTemplate);
					seqNo = objseq.getNsequenceno();
					seqNo = seqNo + 1;

					String instValidationFile = "";

					instValidationFile = "insert into instrumentfile(ninstrumentfilecode,ninstrumentlogcode,"
							+ " ninstrumentlogtypecode,nlinkcode,nattachmenttypecode,sfilename,sfiledesc,ssystemfilename,"
							+ "dcreateddate,ntzcreateddate,noffsetdcreateddate,nfilesize,dmodifieddate,nsitecode,nstatus)"
							+ " values(" + seqNo + "," + lstReqInstFile.get(0).getNinstrumentlogcode() + ","
							+ lstReqInstFile.get(0).getNinstrumentlogtypecode() + ","
							+ lstReqInstFile.get(0).getNlinkcode() + ","
							+ lstReqInstFile.get(0).getNattachmenttypecode() + ",'"
							+ stringUtilityFunction.replaceQuote(lstReqInstFile.get(0).getSfilename()) + "','"
							+ stringUtilityFunction.replaceQuote(lstReqInstFile.get(0).getSfiledesc()) + "','"
							+ stringUtilityFunction.replaceQuote(lstReqInstFile.get(0).getSsystemfilename()) + "','"
							+ lstReqInstFile.get(0).getDcreateddate() + "'," + objUserInfo.getNtimezonecode() + ", "
							+ lstReqInstFile.get(0).getNoffsetdcreateddate() + ", "
							+ lstReqInstFile.get(0).getNfilesize() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
							+ objUserInfo.getNmastersitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(instValidationFile);

					instValidationFile = "update seqnoinstrumentmanagement set nsequenceno=" + seqNo
							+ " where stablename='instrumentfile'";
					jdbcTemplate.execute(instValidationFile);

					lstReqInstFile.get(0).setNinstrumentfilecode(seqNo);

					final List<Object> listObject = new ArrayList<Object>();
					listObject.add(lstReqInstFile);
					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add(
							lstReqInstFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_ADDINSTRUMENTVALIDATIONFILE"
									: "IDS_ADDINSTRUMENTVALIDATIONLINK");

					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);

				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			return new ResponseEntity<Object>(
					getInstrumentValidationFile(lstReqInstFile.get(0).getNinstrumentlogcode(), objUserInfo),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> editInstrumentFile(final InstrumentFile objInstrumentFile, final UserInfo objUserInfo)
			throws Exception {

		final String sEditQuery = "select insf.*, coalesce(lm.jsondata->>'slinkname','') as slinkname from "
				+ " instrumentfile insf, linkmaster lm where insf.ninstrumentfilecode="
				+ objInstrumentFile.getNinstrumentfilecode() + " and insf.ninstrumentlogtypecode="
				+ objInstrumentFile.getNinstrumentlogtypecode() + " and insf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and insf.nlinkcode=lm.nlinkcode";

		final InstrumentFile objIF = (InstrumentFile) jdbcUtilityFunction.queryForObject(sEditQuery,
				InstrumentFile.class, jdbcTemplate);
		if (objIF != null) {
			return new ResponseEntity<Object>(objIF, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to update entry in instrumentfile table. Need to validate
	 * that the lstInstrumentValFile object to be updated is active before updating
	 * details in database. Need to check for duplicate entry of instrumentfile
	 * record for the specified site before saving into database.
	 * 
	 * @param objUserInfo [UserInfo] object holding details to be updated in
	 *                    instrumentfile table
	 * @return response entity object holding response status and data of updated
	 *         unit object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateInstrumentFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final List<InstrumentFile> lstInstrumentValFile = objMapper.readValue(request.getParameter("instrumentFile"),
				new TypeReference<List<InstrumentFile>>() {
				});
		if (lstInstrumentValFile != null && lstInstrumentValFile.size() > 0) {
			final InstrumentFile objInstrumentFile = lstInstrumentValFile.get(0);
			final InstrumentValidation objInstVal = checkInstrumentValidationIsPresent(
					objInstrumentFile.getNinstrumentlogcode());
			if (objInstVal != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objInstrumentFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select *, " + objInstrumentFile.getNinstrumentcatcode()
							+ " as ninstrumentcatcode" + " from instrumentfile insf where ninstrumentfilecode = "
							+ objInstrumentFile.getNinstrumentfilecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					final InstrumentFile objIF = (InstrumentFile) jdbcUtilityFunction.queryForObject(sQuery,
							InstrumentFile.class, jdbcTemplate);

					if (objIF != null) {
						String ssystemfilename = "";
						if (objInstrumentFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							ssystemfilename = objInstrumentFile.getSsystemfilename();
						}

						final String sUpdateQuery = "update instrumentfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objInstrumentFile.getSfilename()) + "',"
								+ " sfiledesc=N'" + stringUtilityFunction.replaceQuote(objInstrumentFile.getSfiledesc())
								+ "', ssystemfilename= N'" + ssystemfilename + "', nattachmenttypecode = "
								+ objInstrumentFile.getNattachmenttypecode() + ", nfilesize ="
								+ objInstrumentFile.getNfilesize() + ", nlinkcode =" + objInstrumentFile.getNlinkcode()
								+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
								+ "' where ninstrumentfilecode = " + objInstrumentFile.getNinstrumentfilecode();
						jdbcTemplate.execute(sUpdateQuery);
						final List<Object> lstOldObject = new ArrayList<Object>();
						objIF.setNinstrumentcode(lstInstrumentValFile.get(0).getNinstrumentcode());

						lstOldObject.add(objIF);

						auditUtilityFunction.fnInsertAuditAction(lstInstrumentValFile, 2, lstOldObject,
								Arrays.asList(lstInstrumentValFile.get(0)
										.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
												? "IDS_EDITINSTRUMENTVALIDATIONFILE"
												: "IDS_EDITINSTRUMENTVALIDATIONLINK"),
								objUserInfo);
						return new ResponseEntity<Object>(
								getInstrumentValidationFile(objInstrumentFile.getNinstrumentlogcode(), objUserInfo),
								HttpStatus.OK);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteInstrumentValidationFile(InstrumentFile objInstrumentFile,
			final UserInfo objUserInfo) throws Exception {
		final InstrumentValidation objInstVal = checkInstrumentValidationIsPresent(
				objInstrumentFile.getNinstrumentlogcode());
		if (objInstVal != null) {

			boolean validRecord = false;
			validatorDel = projectDAOSupport
					.validateDeleteRecord(Integer.toString(objInstrumentFile.getNinstrumentlogcode()), objUserInfo);
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}
			if (validRecord) {

				final String sQuery = "select * from instrumentfile where ninstrumentfilecode = "
						+ objInstrumentFile.getNinstrumentfilecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final InstrumentFile objIF = (InstrumentFile) jdbcUtilityFunction.queryForObject(sQuery,
						InstrumentFile.class, jdbcTemplate);

				if (objIF != null) {

					final String sUpdateQuery = "update instrumentfile set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ninstrumentfilecode = "
							+ objInstrumentFile.getNinstrumentfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<Object> lstObject = new ArrayList<>();

					lstObject.add(objInstrumentFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null,
							Arrays.asList(objInstrumentFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
									.gettype() ? "IDS_DELETEINSTRUMENTVALIDATIONFILE"
											: "IDS_DELETEINSTRUMENTVALIDATIONLINK"),
							objUserInfo);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				return new ResponseEntity<Object>(
						getInstrumentValidationFile(objInstrumentFile.getNinstrumentlogcode(), objUserInfo),
						HttpStatus.OK);

			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public Instrument checkMainInstrumentIsPresent(final int ninstrumentcode) throws Exception {

		final String strQuery = "select ninstrumentcode from instrument where ninstrumentcode = " + ninstrumentcode
				+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (Instrument) jdbcUtilityFunction.queryForObject(strQuery, Instrument.class, jdbcTemplate);

	}

	public InstrumentValidation checkInstrumentValidationIsPresent(final int ninstrumentvalidationcode)
			throws Exception {

		final String strQuery = "select ninstrumentvalidationcode from instrumentvalidation where ninstrumentvalidationcode = "
				+ ninstrumentvalidationcode + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (InstrumentValidation) jdbcUtilityFunction.queryForObject(strQuery, InstrumentValidation.class,
				jdbcTemplate);
	}

	public InstrumentCalibration checkInstrumentCalibrationIsPresent(final int ninstrumentcalibrationcode)
			throws Exception {

		final String strQuery = "select ninstrumentcalibrationcode from instrumentcalibration where ninstrumentcalibrationcode = "
				+ ninstrumentcalibrationcode + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (InstrumentCalibration) jdbcUtilityFunction.queryForObject(strQuery, InstrumentCalibration.class,
				jdbcTemplate);

	}

	public InstrumentMaintenance checkInstrumentMaintenanceIsPresent(final int ninstrumentmaintenancecode)
			throws Exception {
		final String strQuery = "select ninstrumentmaintenancecode from instrumentmaintenance where ninstrumentmaintenancecode = "
				+ ninstrumentmaintenancecode + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (InstrumentMaintenance) jdbcUtilityFunction.queryForObject(strQuery, InstrumentMaintenance.class,
				jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getInstrumentCalibrationStatus(UserInfo userInfo) throws Exception {
		Map<String, Object> returnobj = new HashMap<>();

		final String strQuery = " select ntranscode," + " coalesce(jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'stransdisplaystatus'->>'en-US') as stransstatus " + "  from transactionstatus where "
				+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntranscode in ("
				+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.UNDERCALIBIRATION.gettransactionstatus() + ");";
		returnobj.put("CalibrationStatus", jdbcTemplate.query(strQuery, new TransactionStatus()));

		return new ResponseEntity<>(returnobj, HttpStatus.OK);
	}

	public ResponseEntity<Object> getInstrumentLastCalibrationDate(final UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception {
		Map<String, Object> returnobj = new HashMap<>();
		final int ninstrumentcode = (Integer) inputMap.get("ninstrumentcode");

		final String checkInstCal = "select count(*) from instrumentcalibration where ninstrumentcode="
				+ ninstrumentcode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		int lstInstCal = jdbcTemplate.queryForObject(checkInstCal, Integer.class);

		if (lstInstCal > 0) {
//			final List<InstrumentCalibration> lstInstLastCalDate = (List<InstrumentCalibration>) jdbcTemplate.query(
//					"select  " + " COALESCE(TO_CHAR(dclosedate,'" + userInfo.getSsitedate()
//							+ "'),'') as slastcalibrationdate from instrumentcalibration" + " where "
//							+ " ninstrumentcode=" + ninstrumentcode + " and " + " nstatus="
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ",
//					new InstrumentCalibration());
	        //added by sujatha ALPDJ21-21 
			final List<InstrumentCalibration> lstInstLastCalDate = (List<InstrumentCalibration>) jdbcTemplate
					.query("SELECT COALESCE(TO_CHAR(dclosedate, '" + userInfo.getSsitedate()
							+ "'), '') AS slastcalibrationdate FROM instrumentcalibration "
							+ " WHERE ninstrumentcode = " + ninstrumentcode + "  AND nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " ORDER BY dclosedate ASC", new InstrumentCalibration());

			returnobj.put("date", lstInstLastCalDate.get(lstInstLastCalDate.size() - 1).getSlastcalibrationdate());
			return new ResponseEntity<Object>(returnobj, HttpStatus.OK);
		} else {
			returnobj.put("date", objTimeZoneDAO.getLocalTimeByZone(userInfo).getBody());
			return new ResponseEntity<Object>(returnobj, HttpStatus.OK);
		}
	}

	public List<InstrumentCalibration> getInstrumentCalibration(final int ninstrumentcode,
			final int ninstrumentcalibrationcode, final UserInfo userInfo) throws Exception {

		String query = "select ic.ninstrumentcalibrationcode,inn.ninstrumentnamecode,ic.ninstrumentcode,ic.dclosedate,ic.dopendate,ic.nopenusercode,ic.ncloseusercode,ic.npreregno,ic.sarno,ic.dduedate, "
				+ " ic.dlastcalibrationdate,i.sinstrumentid,inn.sinstrumentname,tz1.stimezoneid as stzopendate,tz2.stimezoneid as stzclosedate,"
				+ " tz3.stimezoneid as stzlastcalibrationdate,tz4.stimezoneid as stzduedate,"
				+ " CONCAT(u.sfirstname,' ',u.slastname) as sopenusername, "
				+ " CONCAT(u1.sfirstname,' ',u1.slastname) as scloseusername, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, "
				+ " COALESCE(TO_CHAR(ic.dlastcalibrationdate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as slastcalibrationdate," + " COALESCE(TO_CHAR(ic.dduedate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sduedate," + " COALESCE(TO_CHAR(ic.dclosedate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sclosedate," + " COALESCE(TO_CHAR(ic.dopendate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sopendate ," + "COALESCE(TO_CHAR(ic.dduedate,'" + userInfo.getSsitedate() + "'),'')||'-'||"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sheadername, case when  coalesce(ic.sclosereason,'') = '' then '-' else ic.sclosereason end sclosereason, "
				+ " case when  coalesce(ic.sopenreason,'') = '' then '-' else ic.sopenreason end sopenreason, "
				+ " COALESCE(TO_CHAR(ic.dduedate, '" + userInfo.getSsitedate() + "'),'') as snextcalibrationperiod"         	//added by sujatha ALPDJ21-21 
				+ " from instrumentcalibration ic,transactionstatus ts,instrument i ,users u,users u1,"
				+ " timezone tz1,timezone tz2,timezone tz3,timezone tz4,instrumentname inn where "
				+ " i.ninstrumentcode=ic.ninstrumentcode and i.ninstrumentnamecode=inn.ninstrumentnamecode and ic.ntzopendate=tz1.ntimezonecode "
				+ " and ic.ntzclosedate=tz2.ntimezonecode and ic.ntzlastcalibrationdate=tz3.ntimezonecode "
				+ " and ic.ntzduedate=tz4.ntimezonecode and ic.nopenusercode=u.nusercode and ic.ncloseusercode=u1.nusercode and"
				+ " ic.ninstrumentcode=" + ninstrumentcode + " and ic.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ic.nsitecode = "
				+ userInfo.getNtranssitecode() + "" + " and i.nsitecode = " + userInfo.getNmastersitecode() + ""
				+ " and u.nsitecode = " + userInfo.getNmastersitecode() + "" + " and u1.nsitecode = "
				+ userInfo.getNmastersitecode() + "" + " and ic.ncalibrationstatus=ts.ntranscode";
		if (ninstrumentcalibrationcode > 0) {
			query = query + " and ic.ninstrumentcalibrationcode = " + ninstrumentcalibrationcode;
		}
		query += " order by ic.ninstrumentcalibrationcode asc;";
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<InstrumentCalibration> lstInstCalGet = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new InstrumentCalibration()),
						Arrays.asList("slastcalibrationdate", "sduedate", "sopendate", "sclosedate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<InstrumentCalibration>>() {
				});

		return lstInstCalGet;
	}

	@Override
	public ResponseEntity<Object> createInstrumentCalibration(InstrumentCalibration instCal, final UserInfo userInfo)
			throws Exception {

		final String sQueryLock = " lock  table lockinstrument " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final List<Object> savedInstList = new ArrayList<>();

		List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		if (instCal.getDlastcalibrationdate() != null) {
			instCal.setSlastcalibrationdate(
					dateUtilityFunction.instantDateToString(instCal.getDlastcalibrationdate()).replace("T", " "));
			lstDateField.add("slastcalibrationdate");
			lstDatecolumn.add("ntzlastcalibrationdate");

		}
		if (instCal.getDopendate() != null) {
			instCal.setSopendate(dateUtilityFunction.instantDateToString(instCal.getDopendate()).replace("T", " "));
			lstDateField.add("sopendate");
			lstDatecolumn.add("ntzopendate");

		}
		if (instCal.getDclosedate() != null) {
			instCal.setSclosedate(dateUtilityFunction.instantDateToString(instCal.getDclosedate()).replace("T", " "));
			lstDateField.add("sclosedate");
			lstDatecolumn.add("ntzclosedate");

		}
		Date duedate = null;
		if (instCal.getDlastcalibrationdate() != null) {
			String qry = "select i.nnextcalibrationperiod,i.nnextcalibration from instrument i where ninstrumentcode ="
					+ instCal.getNinstrumentcode();
			Instrument objins = (Instrument) jdbcUtilityFunction.queryForObject(qry, Instrument.class, jdbcTemplate);
			Calendar calendar = Calendar.getInstance();
			duedate = Date.from(instCal.getDlastcalibrationdate());
			calendar.setTime(duedate);
			if (objins.getNnextcalibrationperiod() == Enumeration.Period.Days.getPeriod()) {
				calendar.add(Calendar.DAY_OF_MONTH, (int) objins.getNnextcalibration());
				duedate = calendar.getTime();
			} else if (objins.getNnextcalibrationperiod() == Enumeration.Period.Weeks.getPeriod()) {
				calendar.add(Calendar.WEEK_OF_MONTH, (int) objins.getNnextcalibration());
				duedate = calendar.getTime();
			} else if (objins.getNnextcalibrationperiod() == Enumeration.Period.Month.getPeriod()) {
				calendar.add(Calendar.MONTH, (int) objins.getNnextcalibration());
				duedate = calendar.getTime();
			}
			if (duedate != null) {
				instCal.setSduedate(dateUtilityFunction.instantDateToString(duedate.toInstant()).replace("T", " "));
				lstDateField.add("sduedate");
				lstDatecolumn.add("ntzduedate");

			}
		}

		InstrumentCalibration convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(instCal, lstDateField, lstDatecolumn, true, userInfo),
				new TypeReference<InstrumentCalibration>() {
				});

		final Instrument instrument = checkMainInstrumentIsPresent(instCal.getNinstrumentcode());
		if (instrument != null) {

			if (instCal.isIsreadonly() ? instCal.isIsreadonly()
					: instCal.getSopendate() == null && instCal.getSclosedate() == null) {

				String instCalSeq = "select nsequenceno from seqnoinstrumentmanagement where stablename='instrumentcalibration'";
				SeqNoInstrumentManagement objseq = (SeqNoInstrumentManagement) jdbcUtilityFunction
						.queryForObject(instCalSeq, SeqNoInstrumentManagement.class, jdbcTemplate);
				int seqNo = objseq.getNsequenceno();
				seqNo = seqNo + 1;

				String dopendate = instCal.getSopendate() == null ? null
						: "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'";
				String dclosedate = instCal.getSclosedate() == null ? null
						: "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'";

				int noffsetdopendate = instCal.getSopendate() == null ? 0
						: dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());
				int noffsetdclosedate = instCal.getSclosedate() == null ? 0
						: dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());

				final String instCalInsert = "insert into instrumentcalibration(ninstrumentcalibrationcode,ninstrumentcode,"
						+ " nopenusercode,ncloseusercode,ncalibrationstatus,npreregno,dduedate,noffsetdduedate,ntzduedate,"
						+ "dopendate,noffsetdopendate,ntzopendate,dlastcalibrationdate,noffsetdlastcalibrationdate,"
						+ "ntzlastcalibrationdate,sopenreason,sclosereason,dclosedate,noffsetdclosedate,ntzclosedate,sarno,dmodifieddate,nsitecode,nstatus)"
						+ " values(" + seqNo + "," + instCal.getNinstrumentcode() + "," + instCal.getNopenusercode()
						+ "," + instCal.getNcloseusercode() + "," + instCal.getNcalibrationstatus() + ","
						+ instCal.getNpreregno() + "" + ",'" + convertedObject.getSduedate() + "',"
						+ convertedObject.getNoffsetdduedate() + "," + instCal.getNtzduedate() + "," + dopendate + ","
						+ noffsetdopendate + "," + instCal.getNtzopendate() + ",'"
						+ convertedObject.getSlastcalibrationdate() + "',"
						+ convertedObject.getNoffsetdlastcalibrationdate() + "," + instCal.getNtzlastcalibrationdate()
						+ ",'" + stringUtilityFunction.replaceQuote(instCal.getSopenreason()) + "','"
						+ stringUtilityFunction.replaceQuote(instCal.getSclosereason()) + "'," + dclosedate + ","
						+ noffsetdclosedate + "," + instCal.getNtzclosedate() + ",'"
						+ stringUtilityFunction.replaceQuote(instCal.getSarno()) + "', '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNsitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(instCalInsert);

				final String instCalSeqUpdate = "update seqnoinstrumentmanagement set nsequenceno=" + seqNo
						+ " where stablename='instrumentcalibration'";
				jdbcTemplate.execute(instCalSeqUpdate);

				instCal.setNinstrumentcalibrationcode(seqNo);

				// String calibrationStatus = "update instrument set ncalibrationstatus=" +
				// instCal.getNcalibrationstatus()
				// + " where ninstrumentcode="+instCal.getNinstrumentcode()+"";
				// jdbcTemplate.execute(calibrationStatus);
				if (instCal.getDlastcalibrationdate() != null) {
					Date date = Date.from(instCal.getDlastcalibrationdate());
					instCal.setSlastcalibrationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}

				if (duedate != null) {
					instCal.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(duedate));
				}

				if (instCal.getNcalibrationstatus() == Enumeration.TransactionStatus.CALIBIRATION
						.gettransactionstatus()) {
					if (instCal.getDopendate() != null) {
						Date date = Date.from(instCal.getDopendate());
						instCal.setSopendate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					if (instCal.getDclosedate() != null) {
						Date date = Date.from(instCal.getDclosedate());
						instCal.setSclosedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
				}
				savedInstList.add(instCal);

				auditUtilityFunction.fnInsertAuditAction(savedInstList, 1, null,
						Arrays.asList("IDS_ADDINSTRUMENTCALIBRATION"), userInfo);

			} else {

				if (instCal.getSopendate() != null) {

					final String dopendate = convertedObject.getSopendate() == null ? null
							: "'" + convertedObject.getSopendate() + "'";

					final String openreason = instCal.getSopenreason() == null ? "" : instCal.getSopenreason();

					final String instOpenDate = "update instrumentcalibration set dopendate=" + dopendate
							+ ",noffsetdopendate=" + convertedObject.getNoffsetdopendate() + ",sopenreason='"
							+ stringUtilityFunction.replaceQuote(openreason) + "',ncalibrationstatus="
							+ instCal.getNcalibrationstatus() + ",nopenusercode=" + instCal.getNopenusercode()
							+ ",ntzopendate=" + instCal.getNtzopendate() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcalibrationcode="
							+ instCal.getNinstrumentcalibrationcode();
					jdbcTemplate.execute(instOpenDate);

					if (instCal.getDopendate() != null) {
//						Date date = Date.from(instCal.getDopendate());
//						instCal.setSopendate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						instCal.setSopendate((dateUtilityFunction.instantDateToStringWithFormat(instCal.getDopendate(),
								userInfo.getSsitedate())));	        //added by sujatha ALPDJ21-21 
					}

					savedInstList.add(instCal);

					auditUtilityFunction.fnInsertAuditAction(savedInstList, 1, null, Arrays.asList("IDS_OPENDATE"),
							userInfo);

				}
				if (instCal.getSclosedate() != null) {

	                 //added by sujatha ALPDJ21-21 

					Date duedateFromClose = null;
					String qry = "select i.nnextcalibrationperiod, i.nnextcalibration from instrument i where ninstrumentcode = "
							+ instCal.getNinstrumentcode();
					final Instrument objins = (Instrument) jdbcUtilityFunction.queryForObject(qry, Instrument.class,
							jdbcTemplate);

					Calendar calendar = Calendar.getInstance();
					duedateFromClose = Date.from(instCal.getDclosedate());
					calendar.setTime(duedateFromClose);

					if (objins.getNnextcalibrationperiod() == Enumeration.Period.Days.getPeriod()) {
						calendar.add(Calendar.DAY_OF_MONTH, (int) objins.getNnextcalibration());
						duedateFromClose = calendar.getTime();
					} else if (objins.getNnextcalibrationperiod() == Enumeration.Period.Weeks.getPeriod()) {
						calendar.add(Calendar.WEEK_OF_MONTH, (int) objins.getNnextcalibration());
						duedateFromClose = calendar.getTime();
					} else if (objins.getNnextcalibrationperiod() == Enumeration.Period.Month.getPeriod()) {
						calendar.add(Calendar.MONTH, (int) objins.getNnextcalibration());
						duedateFromClose = calendar.getTime();
					}

					if (duedateFromClose != null) {
						instCal.setSduedate(dateUtilityFunction.instantDateToString(duedateFromClose.toInstant())
								.replace("T", " "));
						lstDateField.add("sduedate");
						lstDatecolumn.add("ntzduedate");
					}

					convertedObject = objMapper.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(instCal,
							lstDateField, lstDatecolumn, true, userInfo), new TypeReference<InstrumentCalibration>() {
							});
					final String dclosedate = convertedObject.getSclosedate() == null ? null
							: "'" + convertedObject.getSclosedate() + "'";

					final String closereason = instCal.getSclosereason() == null ? "" : instCal.getSclosereason();
	                 //added by sujatha ALPDJ21-21 
					final String sduedate = convertedObject.getSduedate() == null ? null
							: "'" + convertedObject.getSduedate() + "'";
					final String instCloseDate = "update instrumentcalibration set dclosedate=" + dclosedate
							+ ",noffsetdclosedate=" + convertedObject.getNoffsetdclosedate() + ",sclosereason='"
							+ stringUtilityFunction.replaceQuote(closereason) + "' ,ncalibrationstatus="
							+ instCal.getNcalibrationstatus() + ",ncloseusercode=" + instCal.getNcloseusercode()
							+ ",ntzclosedate=" + instCal.getNtzclosedate() + ", dduedate=" + sduedate               	//added by sujatha ALPDJ21-21 
							+ ", noffsetdduedate=" + convertedObject.getNoffsetdduedate() + ", ntzduedate="
							+ instCal.getNtzduedate() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcalibrationcode="
							+ instCal.getNinstrumentcalibrationcode();
					jdbcTemplate.execute(instCloseDate);

					if (instCal.getDclosedate() != null) {
						Date date = Date.from(instCal.getDclosedate());
						instCal.setSclosedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
	                 //added by sujatha ALPDJ21-21 
					if (duedateFromClose != null) {
						instCal.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(duedateFromClose));
					}
					savedInstList.clear();
					savedInstList.add(instCal);

					auditUtilityFunction.fnInsertAuditAction(savedInstList, 1, null, Arrays.asList("IDS_CLOSEDATE"),
							userInfo);

				}
			}
			final List<InstrumentCalibration> lstInstCal = getInstrumentCalibration(instCal.getNinstrumentcode(), 0,
					userInfo);
			if (!lstInstCal.isEmpty()) {

				objMap.put("selectedInstrumentCalibration", lstInstCal.get(lstInstCal.size() - 1));

				objMap.put("InstrumentCalibration", lstInstCal);
				objMap.putAll(getInstrumentCalibrationFile(
						lstInstCal.get(lstInstCal.size() - 1).getNinstrumentcalibrationcode(), userInfo));
			} else {
				objMap.put("selectedInstrumentCalibration", lstInstCal);

				objMap.put("InstrumentCalibration", lstInstCal);
			}
			return new ResponseEntity<>(objMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public InstrumentCalibration getActiveInstrumentCalibrationById(final int ninstrumentcalibrationcode,
			final UserInfo userInfo) throws Exception {

		final String strQuery = "select ic.dduedate,ic.dlastcalibrationdate,ic.ninstrumentcalibrationcode,i.sinstrumentid,ic.ninstrumentcode,ic.ncalibrationstatus ,ic.nstatus,tz1.stimezoneid as stzopendate,tz2.stimezoneid as stzclosedate,tz3.stimezoneid as stzlastcalibrationdate,tz4.stimezoneid as stzduedate, tz1.ntimezonecode,tz2.ntimezonecode,tz3.ntimezonecode,tz4.ntimezonecode,ic.ntzduedate,ic.ntzlastcalibrationdate, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, "
				+ " COALESCE(TO_CHAR(ic.dlastcalibrationdate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as slastcalibrationdate," + " COALESCE(TO_CHAR(ic.dduedate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sduedate"
				+ " from instrument i,instrumentcalibration ic ,transactionstatus ts,timezone tz1,timezone tz2,timezone tz3,timezone tz4  "
				+ " where ic.ninstrumentcalibrationcode=" + ninstrumentcalibrationcode
				+ " and ic.ntzopendate=tz1.ntimezonecode and ic.ntzclosedate=tz2.ntimezonecode and ic.ntzlastcalibrationdate=tz3.ntimezonecode and ic.ntzduedate=tz4.ntimezonecode and "
				+ " ic.ninstrumentcode=i.ninstrumentcode " + "and ic.ncalibrationstatus=ts.ntranscode "
				+ " and ic.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ic.nsitecode = " + userInfo.getNtranssitecode() + "";

		final ObjectMapper objMapper = new ObjectMapper();

		objMapper.registerModule(new JavaTimeModule());

		final List<InstrumentCalibration> lstInstCalGet = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new InstrumentCalibration()),
						Arrays.asList("slastcalibrationdate", "sduedate"),
						Arrays.asList("ntzlastcalibrationdate", "ntzduedate"), userInfo, false, null, false),
				new TypeReference<List<InstrumentCalibration>>() {
				});

		if (lstInstCalGet.isEmpty()) {
			InstrumentCalibration InstrumentCalibration = null;
			return InstrumentCalibration;
		} else {
			return (InstrumentCalibration) lstInstCalGet.get(0);

		}
	}

	public InstrumentCalibration getInstrumentCalibrationListByDate(final int ncalibrationstatus,
			final String calibrationdate, final String duedate, final int ninstrumentcode) throws Exception {

		final String query = "select ninstrumentcalibrationcode, ninstrumentcode, nopenusercode, ncloseusercode,"
				+ " ncalibrationstatus, npreregno, dduedate, ntzduedate, noffsetdduedate,"
				+ " dopendate, ntzopendate, noffsetdopendate, dlastcalibrationdate,"
				+ " ntzlastcalibrationdate, noffsetdlastcalibrationdate, sopenreason,"
				+ " sclosereason, dclosedate, ntzclosedate, noffsetdclosedate, sarno,"
				+ " dmodifieddate, nsitecode, nstatus from instrumentcalibration " + " where dlastcalibrationdate='"
				+ calibrationdate + "'" + " and dduedate='" + duedate + "' and ncalibrationstatus=" + ncalibrationstatus
				+ " and ninstrumentcode=" + ninstrumentcode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		return (InstrumentCalibration) jdbcUtilityFunction.queryForObject(query, InstrumentCalibration.class,
				jdbcTemplate);

	}

	@Override
	public ResponseEntity<Object> updateInstrumentCalibration(final InstrumentCalibration instCal,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final List<Object> beforeSavedInstList = new ArrayList<>();
		final List<Object> savedInstList = new ArrayList<>();
		List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		if (instCal.getDlastcalibrationdate() != null) {
			instCal.setSlastcalibrationdate(dateUtilityFunction.instantDateToString(instCal.getDlastcalibrationdate())
					.replace("T", " ").replace("Z", ""));
			lstDateField.add("slastcalibrationdate");
			lstDatecolumn.add("ntzlastcalibrationdate");

		}
		if (instCal.getDduedate() != null) {
			instCal.setSduedate(
					dateUtilityFunction.instantDateToString(instCal.getDduedate()).replace("T", " ").replace("Z", ""));
			lstDateField.add("sduedate");
			lstDatecolumn.add("ntzduedate");

		}

		final InstrumentCalibration convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(instCal, lstDateField, lstDatecolumn, true, userInfo),
				new TypeReference<InstrumentCalibration>() {
				});

		final InstrumentCalibration instCalDetails = (InstrumentCalibration) getActiveInstrumentCalibrationById(
				instCal.getNinstrumentcalibrationcode(), userInfo);
		if (instCal.getDlastcalibrationdate() != null) {
			Date date = Date.from(instCalDetails.getDlastcalibrationdate());
			instCalDetails.setSlastcalibrationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		if (instCal.getDduedate() != null) {
			Date date = Date.from(instCalDetails.getDduedate());
			instCalDetails.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		if (instCalDetails == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ninstrumentcalibrationcode from instrumentcalibration where dlastcalibrationdate = '"
					+ instCal.getSlastcalibrationdate() + "' and ncalibrationstatus=" + instCal.getNcalibrationstatus()
					+ " and dduedate='" + instCal.getSduedate() + "' and ninstrumentcalibrationcode<> "
					+ instCal.getNinstrumentcalibrationcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<InstrumentCalibration> instCalList = (List<InstrumentCalibration>) jdbcTemplate
					.query(queryString, new InstrumentCalibration());

			if (instCalList.isEmpty()) {
				final String query = "update instrumentcalibration set dlastcalibrationdate='"
						+ convertedObject.getSlastcalibrationdate() + "',noffsetdlastcalibrationdate="
						+ convertedObject.getNoffsetdlastcalibrationdate() + ",dduedate='"
						+ convertedObject.getSduedate() + "',noffsetdduedate=" + convertedObject.getNoffsetdduedate()
						+ ",ncalibrationstatus=" + instCal.getNcalibrationstatus() + ",ntzduedate="
						+ instCal.getNtzduedate() + "," + "ntzlastcalibrationdate="
						+ instCal.getNtzlastcalibrationdate() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentcalibrationcode="
						+ instCal.getNinstrumentcalibrationcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);

				final List<InstrumentCalibration> lstInstrumentCalibration = getInstrumentCalibration(
						instCal.getNinstrumentcode(), instCal.getNinstrumentcalibrationcode(), userInfo);

				if (lstInstrumentCalibration.size() > 0) {
					objMap.put("selectedInstrumentCalibration",
							lstInstrumentCalibration.get(lstInstrumentCalibration.size() - 1));
					if (lstInstrumentCalibration.get(0).getDlastcalibrationdate() != null) {
						Date date = Date.from(lstInstrumentCalibration.get(0).getDlastcalibrationdate());
						instCal.setSlastcalibrationdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					if (lstInstrumentCalibration.get(0).getDduedate() != null) {
						Date date = Date.from(lstInstrumentCalibration.get(0).getDduedate());
						instCal.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
				}
				savedInstList.add(instCal);
				beforeSavedInstList.add(instCalDetails);

				auditUtilityFunction.fnInsertAuditAction(savedInstList, 2, beforeSavedInstList,
						Arrays.asList("IDS_EDITINSTRUMENTCALIBRATION"), userInfo);

				return new ResponseEntity<>(objMap, HttpStatus.OK);
			}

			else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("ALREADYEXIST", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteInstrumentCalibration(Map<String, Object> inputMap,
			InstrumentCalibration objInstCal, UserInfo userInfo) throws Exception {
		Map<String, Object> outMap = new HashMap<String, Object>();
		final List<Object> deletedInstList = new ArrayList<>();

		final InstrumentCalibration instCalDetails = getActiveInstrumentCalibrationById(
				objInstCal.getNinstrumentcalibrationcode(), userInfo);

		int checkCalValidation = jdbcTemplate.queryForObject(
				" select count(*) from instrumentcalibration where dopendate is not null and dclosedate is not null and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentcalibrationcode=" + objInstCal.getNinstrumentcalibrationcode() + "",
				Integer.class);

		if (checkCalValidation != 0) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYCLOSED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else {
			if (instCalDetails == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				int statusCheck = 0;

				statusCheck = jdbcTemplate
						.queryForObject("select ninstrumentstatus from instrument where ninstrumentcode="
								+ objInstCal.getNinstrumentcode(), Integer.class);

				if (statusCheck != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {

					boolean validRecord = false;
					validatorDel = projectDAOSupport.validateDeleteRecord(
							Integer.toString(objInstCal.getNinstrumentcalibrationcode()), userInfo);
					if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
						validRecord = true;
					} else {
						validRecord = false;
					}
					if (validRecord) {
						String updateQuery = "";

						updateQuery = "update instrumentcalibration set nstatus = "
								+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' where ninstrumentcalibrationcode = " + objInstCal.getNinstrumentcalibrationcode()
								+ ";";

						String qry = "select * from instrumentfile where ninstrumentlogcode = "
								+ objInstCal.getNinstrumentcalibrationcode() + " and ninstrumentlogtypecode = "
								+ Enumeration.InstrumentLogType.INSTRUMENT_CALIBRATION.getinstrumentlogtype()
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " ;";
						List<InstrumentFile> oldInstrumentFile = (List<InstrumentFile>) jdbcTemplate.query(qry,
								new InstrumentFile());

						updateQuery = updateQuery + "update instrumentfile set nstatus = "
								+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentlogcode = "
								+ objInstCal.getNinstrumentcalibrationcode() + " and ninstrumentlogtypecode = "
								+ Enumeration.InstrumentLogType.INSTRUMENT_CALIBRATION.getinstrumentlogtype()
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " ;";

						// updateQuery =updateQuery + "update instrument set ncalibrationstatus=" +
						// Enumeration.TransactionStatus.NA.gettransactionstatus()
						// + " where ninstrumentcode="+objInstCal.getNinstrumentcode()+";";
						jdbcTemplate.execute(updateQuery);

						final List<InstrumentCalibration> lstInstCal = getInstrumentCalibration(
								objInstCal.getNinstrumentcode(), 0, userInfo);
						if (lstInstCal != null && lstInstCal.size() > 0) {
							outMap.put("selectedInstrumentCalibration", lstInstCal.get(lstInstCal.size() - 1));
							outMap.putAll(getInstrumentCalibrationFile(
									lstInstCal.get(lstInstCal.size() - 1).getNinstrumentcalibrationcode(), userInfo));

						}
						outMap.put("InstrumentCalibration", lstInstCal);
						if (instCalDetails.getDlastcalibrationdate() != null) {
							Date date = Date.from(instCalDetails.getDlastcalibrationdate());
							instCalDetails.setSlastcalibrationdate(
									new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						}
						if (instCalDetails.getDduedate() != null) {
							Date date = Date.from(instCalDetails.getDduedate());
							instCalDetails.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						}
						if (instCalDetails.getDopendate() != null) {
							Date date = Date.from(instCalDetails.getDopendate());
							instCalDetails.setSopendate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						}
						if (instCalDetails.getDclosedate() != null) {
							Date date = Date.from(instCalDetails.getDclosedate());
							instCalDetails.setSclosedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						}

						final List<String> multilingualIDList = new ArrayList<>();

						// if (!oldInstrumentFile.isEmpty()) {
						//
						// List<InstrumentFile> listDeleteFile = oldInstrumentFile.stream()
						// .filter(b -> b.getNattachmenttypecode() ==
						// Enumeration.AttachmentType.FTP.gettype())
						// .collect(Collectors.toList());
						//
						// List<InstrumentFile> listDeleteLink = oldInstrumentFile.stream()
						// .filter(b -> b.getNattachmenttypecode() ==
						// Enumeration.AttachmentType.LINK.gettype())
						// .collect(Collectors.toList());
						// deletedInstList.add(listDeleteFile);
						// multilingualIDList.add("IDS_DELETEINSTRUMENTCALIBRATIONFILE");
						// deletedInstList.add(listDeleteLink);
						// multilingualIDList.add("IDS_DELETEINSTRUMENTCALIBRATIONLINK");
						// }

						deletedInstList.add(oldInstrumentFile);
						multilingualIDList.add("IDS_DELETEINSTRUMENTCALIBRATIONFILE/LINK");
						final List<InstrumentCalibration> lst = new ArrayList<>();
						lst.add(instCalDetails);
						deletedInstList.add(lst);
						multilingualIDList.add("IDS_DELETEINSTRUMENTCALIBRATION");
						auditUtilityFunction.fnInsertListAuditAction(deletedInstList, 1, null, multilingualIDList,
								userInfo);

						return new ResponseEntity<Object>(outMap, HttpStatus.OK);
					} else {
						return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

			}
		}

	}

	@Override
	public ResponseEntity<Object> getInstrumentCalibrationOpenDateStatus(final UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception {
		Map<String, Object> returnobj = new HashMap<>();

		int statusCheck = 0;
		if (inputMap.get("ninstrumentcode") != null) {
			statusCheck = jdbcTemplate.queryForObject(
					"select ninstrumentstatus from instrument where ninstrumentcode=" + inputMap.get("ninstrumentcode"),
					Integer.class);
		}
		if (statusCheck != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {

			final String query = "select  * from instrumentcalibration  where ninstrumentcalibrationcode= "
					+ (Integer) inputMap.get("ninstrumentcalibrationcode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "";

			List<InstrumentCalibration> checkCalIsPresent = (List<InstrumentCalibration>) jdbcTemplate.query(query,
					new InstrumentCalibration());
			if (checkCalIsPresent.size() != 0) {
				final String strQuery = " select ntranscode, coalesce(jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "',"
						+ " jsondata->'stransdisplaystatus'->>'en-US') as stransstatus "
						+ "  from transactionstatus where  nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntranscode in ("
						+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.UNDERCALIBIRATION.gettransactionstatus() + ");";

				returnobj.put("CalibrationStatus", jdbcTemplate.query(strQuery, new TransactionStatus()));

				return new ResponseEntity<>(returnobj, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	public Map<String, Object> getInstrumentCalibrationFile(final int ninstrumentlogcode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String queryformat = "COALESCE(TO_CHAR(dcreateddate,'" + objUserInfo.getSpgdatetimeformat() + "'),'-') ";

		final String query = "select insf.ninstrumentfilecode, insf.ninstrumentlogcode, insf.ninstrumentlogtypecode, "
				+ " insf.nattachmenttypecode, insf.sfilename, insf.sfiledesc, insf.ssystemfilename, insf.nfilesize, insf.nsitecode,"
				+ " insf.nstatus, case when insf.nlinkcode = -1 then cast(insf.nfilesize as text) else '-' end sfilesize,"
				+ " case when insf.nlinkcode = -1 then " + queryformat + " else '-' end  screateddate, "
				+ " case when insf.nlinkcode = -1 then '-' else lm.jsondata->>'slinkname' end slinkname, insf.noffsetdcreateddate "
				+ " from instrumentfile insf, linkmaster lm where insf.ninstrumentlogcode=" + ninstrumentlogcode
				+ " and insf.ninstrumentlogtypecode=2 " + "and insf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and insf.nlinkcode=lm.nlinkcode and "
				+ " insf.nstatus=lm.nstatus";

		outputMap.put("CalibrationFileData",
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new InstrumentFile()),
						Arrays.asList("screateddate"), Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false,
						null, false));

		return outputMap;

	}

	@Override
	public ResponseEntity<Object> createInstrumentCalibrationFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {

		final String sQueryLock = " lock  table instrumentfile "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final ObjectMapper objMapper = new ObjectMapper();
		final List<InstrumentFile> lstReqInstFile = objMapper.readValue(request.getParameter("instrumentFile"),
				new TypeReference<List<InstrumentFile>>() {
				});

		final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS);
		final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
		final String screatedDate = dateUtilityFunction.instantDateToString(instantDate);

		lstReqInstFile.forEach(objtf -> {
			objtf.setDcreateddate(instantDate);
			if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					|| objtf.getNattachmenttypecode() == Enumeration.AttachmentType.LINK.gettype()) {
				objtf.setDcreateddate(instantDate);
				objtf.setNoffsetdcreateddate(noffset);
				objtf.setScreateddate(screatedDate.replace("T", " "));
			}

		});

		int seqNo = 0;
		if (lstReqInstFile != null && lstReqInstFile.size() > 0) {
			final InstrumentCalibration objInstCal = checkInstrumentCalibrationIsPresent(
					lstReqInstFile.get(0).getNinstrumentlogcode());
			if (objInstCal != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqInstFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo); // Folder Name -
																									// master
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {

					final String sectSeq = "select nsequenceno from seqnoinstrumentmanagement where stablename='instrumentfile'";
					final SeqNoInstrumentManagement objseq = (SeqNoInstrumentManagement) jdbcUtilityFunction
							.queryForObject(sectSeq, SeqNoInstrumentManagement.class, jdbcTemplate);
					seqNo = objseq.getNsequenceno();
					seqNo = seqNo + 1;

					String instCalFile = "";
					instCalFile = "insert into instrumentfile(ninstrumentfilecode,ninstrumentlogcode,ninstrumentlogtypecode,nlinkcode,"
							+ "nattachmenttypecode,sfilename,sfiledesc,ssystemfilename,"
							+ "dcreateddate,ntzcreateddate,noffsetdcreateddate,nfilesize,dmodifieddate,nsitecode,nstatus)"
							+ " values(" + seqNo + "," + lstReqInstFile.get(0).getNinstrumentlogcode() + ","
							+ lstReqInstFile.get(0).getNinstrumentlogtypecode() + ","
							+ lstReqInstFile.get(0).getNlinkcode() + ","
							+ lstReqInstFile.get(0).getNattachmenttypecode() + ",'"
							+ stringUtilityFunction.replaceQuote(lstReqInstFile.get(0).getSfilename()) + "','"
							+ stringUtilityFunction.replaceQuote(lstReqInstFile.get(0).getSfiledesc()) + "','"
							+ stringUtilityFunction.replaceQuote(lstReqInstFile.get(0).getSsystemfilename()) + "','"
							+ lstReqInstFile.get(0).getDcreateddate() + "'," + objUserInfo.getNtimezonecode() + ","
							+ lstReqInstFile.get(0).getNoffsetdcreateddate() + ", "
							+ lstReqInstFile.get(0).getNfilesize() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
							+ objUserInfo.getNmastersitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(instCalFile);

					instCalFile = "update seqnoinstrumentmanagement set nsequenceno=" + seqNo
							+ " where stablename='instrumentfile'";
					jdbcTemplate.execute(instCalFile);

					lstReqInstFile.get(0).setNinstrumentfilecode(seqNo);

					final List<Object> listObject = new ArrayList<Object>();
					listObject.add(lstReqInstFile);

					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, Arrays.asList(
							lstReqInstFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_ADDINSTRUMENTCALIBRATIONFILE"
									: "IDS_ADDINSTRUMENTCALIBRATIONLINK"),
							objUserInfo);

				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return new ResponseEntity<Object>(
					getInstrumentCalibrationFile(lstReqInstFile.get(0).getNinstrumentlogcode(), objUserInfo),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateInstrumentCalibrationFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final List<InstrumentFile> lstInstrumentFile = objMapper.readValue(request.getParameter("instrumentFile"),
				new TypeReference<List<InstrumentFile>>() {
				});
		if (lstInstrumentFile != null && lstInstrumentFile.size() > 0) {
			final InstrumentFile objInstrumentFile = lstInstrumentFile.get(0);

			final InstrumentCalibration objInstCal = checkInstrumentCalibrationIsPresent(
					objInstrumentFile.getNinstrumentlogcode());

			if (objInstCal != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objInstrumentFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select * from instrumentfile where ninstrumentfilecode = "
							+ objInstrumentFile.getNinstrumentfilecode() + " and ninstrumentlogtypecode="
							+ objInstrumentFile.getNinstrumentlogtypecode() + "  and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final InstrumentFile objTF = (InstrumentFile) jdbcUtilityFunction.queryForObject(sQuery,
							InstrumentFile.class, jdbcTemplate);

					if (objTF != null) {
						String ssystemfilename = "";
						if (objInstrumentFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							ssystemfilename = objInstrumentFile.getSsystemfilename();
						}

						final String sUpdateQuery = "update instrumentfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objInstrumentFile.getSfilename()) + "',"
								+ " sfiledesc=N'" + stringUtilityFunction.replaceQuote(objInstrumentFile.getSfiledesc())
								+ "', ssystemfilename= N'" + stringUtilityFunction.replaceQuote(ssystemfilename)
								+ "', nattachmenttypecode = " + objInstrumentFile.getNattachmenttypecode()
								+ ", nfilesize =" + objInstrumentFile.getNfilesize() + ", nlinkcode ="
								+ objInstrumentFile.getNlinkcode() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ninstrumentfilecode = "
								+ objInstrumentFile.getNinstrumentfilecode();
						jdbcTemplate.execute(sUpdateQuery);
						final List<Object> lstOldObject = new ArrayList<Object>();

						objTF.setNinstrumentcode(lstInstrumentFile.get(0).getNinstrumentcode());

						lstOldObject.add(objTF);

						auditUtilityFunction.fnInsertAuditAction(lstInstrumentFile, 2, lstOldObject,
								Arrays.asList(lstInstrumentFile.get(0)
										.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
												? "IDS_EDITINSTRUMENTCALIBRATIONFILE"
												: "IDS_EDITINSTRUMENTCALIBRATIONLINK"),
								objUserInfo);
						return new ResponseEntity<Object>(
								getInstrumentCalibrationFile(objInstrumentFile.getNinstrumentlogcode(), objUserInfo),
								HttpStatus.OK);
					} else {

						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {

					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteInstrumentCalibrationFile(InstrumentFile objInstrumentFile,
			final UserInfo objUserInfo) throws Exception {
		final InstrumentCalibration objInstCal = checkInstrumentCalibrationIsPresent(
				objInstrumentFile.getNinstrumentlogcode());
		if (objInstCal != null) {

			boolean validRecord = false;
			validatorDel = projectDAOSupport
					.validateDeleteRecord(Integer.toString(objInstrumentFile.getNinstrumentlogcode()), objUserInfo);
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}
			if (validRecord) {
				final String sQuery = "select * from instrumentfile where ninstrumentfilecode = "
						+ objInstrumentFile.getNinstrumentfilecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final InstrumentFile objIF = (InstrumentFile) jdbcUtilityFunction.queryForObject(sQuery,
						InstrumentFile.class, jdbcTemplate);
				if (objIF != null) {

					final String sUpdateQuery = "update instrumentfile set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ninstrumentfilecode = "
							+ objInstrumentFile.getNinstrumentfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<Object> lstObject = new ArrayList<>();
					lstObject.add(objInstrumentFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null,
							Arrays.asList(objInstrumentFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
									.gettype() ? "IDS_DELETEINSTRUMENTCALIBRATIONFILE"
											: "IDS_DELETEINSTRUMENTCALIBRATIONLINK"),
							objUserInfo);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
				return new ResponseEntity<Object>(
						getInstrumentCalibrationFile(objInstrumentFile.getNinstrumentlogcode(), objUserInfo),
						HttpStatus.OK);
			} else {

				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getInstrumentCalibrationValidation(final UserInfo userInfo, final int nFlag,
			final int ninstrumentcode, final int ninstrumentcalibrationcode) throws Exception {
		int statusCheck = 0;
		Map<String, Object> returnobj = new HashMap<>();

		statusCheck = jdbcTemplate.queryForObject(
				"select ninstrumentstatus from instrument where ninstrumentcode=" + ninstrumentcode, Integer.class);

		if (statusCheck != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {

			String checkInstCalVal = "";
			String validation = "";
			if (nFlag == 1) {
				int validation1, validation2, validation3, nValue = 0;
				validation1 = jdbcTemplate
						.queryForObject(
								" select count(*) from instrumentcalibration where ninstrumentcode=" + ninstrumentcode
										+ " and nsitecode = " + userInfo.getNsitecode() + " and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
								Integer.class);
				validation2 = jdbcTemplate
						.queryForObject(
								" select count(*) from instrumentcalibration where ninstrumentcode=" + ninstrumentcode
										+ " and dopendate is null and dclosedate is null and nsitecode = "
										+ userInfo.getNsitecode() + " and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
								Integer.class);

				validation3 = jdbcTemplate.queryForObject(
						" select count(*) from instrumentcalibration where dopendate is not null and dclosedate is null and ninstrumentcode="
								+ ninstrumentcode + " and nsitecode = " + userInfo.getNsitecode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
						Integer.class);

				if (validation1 == 0) {
					nValue = 1;
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}
				if (validation1 > 0 && nValue != 1) {
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}
				if (validation2 != 0) {
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_SELECTOPENDATE";
					returnobj.put("Status", validation);
				}
				if (validation3 != 0) {
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_SELECTCLOSEDATE";
					returnobj.put("Status", validation);
				}
			} else if (nFlag == 2) {

				checkInstCalVal = " select * from instrumentcalibration where dopendate is null and dclosedate is null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentcalibrationcode=" + ninstrumentcalibrationcode + "";

				InstrumentCalibration objInstCalData1 = (InstrumentCalibration) jdbcUtilityFunction
						.queryForObject(checkInstCalVal, InstrumentCalibration.class, jdbcTemplate);

				checkInstCalVal = " select * from instrumentcalibration where dopendate is not null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentcalibrationcode=" + ninstrumentcalibrationcode + "";

				final InstrumentCalibration objInstCalData2 = (InstrumentCalibration) jdbcUtilityFunction
						.queryForObject(checkInstCalVal, InstrumentCalibration.class, jdbcTemplate);

				checkInstCalVal = " select * from instrumentcalibration where  dclosedate is not null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentcalibrationcode=" + ninstrumentcalibrationcode + "";

				InstrumentCalibration objInstCalData3 = (InstrumentCalibration) jdbcUtilityFunction
						.queryForObject(checkInstCalVal, InstrumentCalibration.class, jdbcTemplate);

				if (objInstCalData1 != null) {

					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}
				if (objInstCalData2 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_OPENCLOSEDCANNOTEDIT";
					returnobj.put("Status", validation);
				}
				if (objInstCalData3 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_OPENCLOSEDCANNOTEDIT";
					returnobj.put("Status", validation);
				}
			} else if (nFlag == 3) {

				checkInstCalVal = " select * from instrumentcalibration where dopendate is null and dclosedate is null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentcalibrationcode=" + ninstrumentcalibrationcode + "";

				InstrumentCalibration objInstCalData1 = (InstrumentCalibration) jdbcUtilityFunction
						.queryForObject(checkInstCalVal, InstrumentCalibration.class, jdbcTemplate);
				checkInstCalVal = " select * from instrumentcalibration where dopendate is not null and dclosedate is not null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentcalibrationcode=" + ninstrumentcalibrationcode + "";

				InstrumentCalibration objInstCalData2 = (InstrumentCalibration) jdbcUtilityFunction
						.queryForObject(checkInstCalVal, InstrumentCalibration.class, jdbcTemplate);

				checkInstCalVal = " select * from instrumentcalibration where dopendate is not null and dclosedate is null and ninstrumentcode="
						+ ninstrumentcode + "  and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentcalibrationcode=" + ninstrumentcalibrationcode + "";

				InstrumentCalibration objInstCalData3 = (InstrumentCalibration) jdbcUtilityFunction
						.queryForObject(checkInstCalVal, InstrumentCalibration.class, jdbcTemplate);

				if (objInstCalData1 != null) {
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}
				if (objInstCalData2 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_ALREADYOPENED";
					returnobj.put("Status", validation);
				}
				if (objInstCalData3 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_ALREADYOPENED";
					returnobj.put("Status", validation);
				}
			} else if (nFlag == 4) {
				checkInstCalVal = " select * from instrumentcalibration where dopendate is not null and dclosedate is null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentcalibrationcode=" + ninstrumentcalibrationcode + "";

				InstrumentCalibration objInstCalData1 = (InstrumentCalibration) jdbcUtilityFunction
						.queryForObject(checkInstCalVal, InstrumentCalibration.class, jdbcTemplate);

				checkInstCalVal = " select * from instrumentcalibration where dopendate is not null and dclosedate is not null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentcalibrationcode=" + ninstrumentcalibrationcode + "";

				InstrumentCalibration objInstCalData2 = (InstrumentCalibration) jdbcUtilityFunction
						.queryForObject(checkInstCalVal, InstrumentCalibration.class, jdbcTemplate);

				checkInstCalVal = " select * from instrumentcalibration where dopendate is null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentcalibrationcode=" + ninstrumentcalibrationcode + "";

				InstrumentCalibration objInstCalData3 = (InstrumentCalibration) jdbcUtilityFunction
						.queryForObject(checkInstCalVal, InstrumentCalibration.class, jdbcTemplate);

				if (objInstCalData1 != null) {
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}

				if (objInstCalData2 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_ALREADYCLOSED";
					returnobj.put("Status", validation);
				}

				if (objInstCalData3 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_SELECTOPENDATEBEFORECLOSE";
					returnobj.put("Status", validation);
				}
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(returnobj, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active
	 * InstrumentMaintenanceStatus for the specified site.
	 * 
	 * @param userInfo [UserInfo] object for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         units
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getInstrumentMaintenanceStatus(final UserInfo userInfo) throws Exception {
		Map<String, Object> returnobj = new HashMap<>();
		final String strQuery = " select ntranscode, coalesce(jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'stransdisplaystatus'->>'en-US') as stransstatus   from transactionstatus where "
				+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntranscode in ("
				+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.UNDERMAINTANENCE.gettransactionstatus() + ");";

		returnobj.put("MaintenanceStatus", jdbcTemplate.query(strQuery, new TransactionStatus()));
		return new ResponseEntity<>(returnobj, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active InstrumentMaintenance for
	 * the specified site.
	 * 
	 * @param userInfo                   [UserInfo] nmasterSiteCode [int] primary
	 *                                   key of site object for which the list is to
	 *                                   be fetched
	 * @param ninstrumentmaintenancecode [int]code of the instrumentmaintenance
	 * @return response entity object holding response status and list of all active
	 *         units
	 * @throws Exception that are thrown from this DAO layer
	 */

	public List<InstrumentMaintenance> getInstrumentMaintenance(final int ninstrumentcode,
			final int ninstrumentmaintenancecode, final UserInfo userInfo) throws Exception {

		String query = "select im.ninstrumentmaintenancecode,im.ninstrumentcode,im.dduedate,im.dopendate,im.dclosedate,im.dlastmaintenancedate, "
				+ " im.nopenusercode,im.ncloseusercode,im.nmaintenancestatus,im.nstatus,i.sinstrumentid,inn.ninstrumentnamecode,inn.sinstrumentname, "
				+ " tz1.stimezoneid as stzopendate,tz2.stimezoneid as stzclosedate,"
				+ " tz3.stimezoneid as stzlastmaintenancedate,tz4.stimezoneid as stzduedate,"
				+ " CONCAT(u.sfirstname,' ',u.slastname) as sopenusername, "
				+ " CONCAT(u1.sfirstname,' ',u1.slastname) as scloseusername, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, "
				+ " COALESCE(TO_CHAR(im.dlastmaintenancedate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as slastmaintenancedate," + " COALESCE(TO_CHAR(im.dduedate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sduedate," + " COALESCE(TO_CHAR(im.dclosedate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sclosedate," + " COALESCE(TO_CHAR(im.dopendate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sopendate, " + " COALESCE(TO_CHAR(im.dduedate,'" + userInfo.getSsitedate()
				+ "'),'')||'-'||" + " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + " ts.jsondata->'stransdisplaystatus'->>'en-US') as sheadername, "
				+ " case when  coalesce(im.sclosereason,'') = '' then '-' else im.sclosereason end sclosereason,"
				+ " case when  coalesce(im.sopenreason,'') = '' then '-' else im.sopenreason end sopenreason, "
				+ " COALESCE(TO_CHAR(im.dduedate, '" + userInfo.getSsitedate() + "'),'') as snextmaintenanceperiod "            	//added by sujatha ALPDJ21-21 
				+ " from instrumentname inn,instrumentmaintenance im,transactionstatus ts,instrument i ,timezone tz1,timezone tz2,timezone tz3,timezone tz4, "
				+ " users u,users u1 where "
				+ " i.ninstrumentcode=im.ninstrumentcode and i.ninstrumentnamecode=inn.ninstrumentnamecode and im.ntzopendate=tz1.ntimezonecode "
				+ " and im.ntzclosedate=tz2.ntimezonecode and im.ntzlastmaintenancedate=tz3.ntimezonecode"
				+ " and im.ntzduedate=tz4.ntimezonecode and im.nopenusercode=u.nusercode and im.ncloseusercode=u1.nusercode and"
				+ " im.ninstrumentcode=" + ninstrumentcode + " and im.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and im.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "and i.nsitecode = "              	//added by sujatha ALPDJ21-21 
				+ userInfo.getNmastersitecode() + " " + "and u.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ "and u1.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and im.nmaintenancestatus=ts.ntranscode";
		if (ninstrumentmaintenancecode > 0) {
			query = query + " and im.ninstrumentmaintenancecode = " + ninstrumentmaintenancecode;
		}
		query += " order by im.ninstrumentmaintenancecode asc;";
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<InstrumentMaintenance> lstInstMainGet = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new InstrumentMaintenance()),
						Arrays.asList("slastmaintenancedate", "sduedate", "sopendate", "sclosedate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<InstrumentMaintenance>>() {
				});

		return lstInstMainGet;

	}

	/**
	 * This method is used to retrieve list of all active InstrumentMaintenanceDate
	 * for the specified instrument.
	 * 
	 * @param userInfo [UserInfo] object for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         units
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getInstrumentLastMaintenanceDate(final UserInfo userInfo,
			final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> returnobj = new HashMap<>();
		int ninstrumentcode = (Integer) inputMap.get("ninstrumentcode");
		String checkMainIsPresent = "select count(*) from instrumentmaintenance where ninstrumentcode="
				+ ninstrumentcode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		int lstInstMain = jdbcTemplate.queryForObject(checkMainIsPresent, Integer.class);
		if (lstInstMain > 0) {
	           //added by sujatha ALPDJ21-21 
//			final List<InstrumentMaintenance> lstInstLastMainDate = (List<InstrumentMaintenance>) jdbcTemplate.query(
//					"select  " + " COALESCE(TO_CHAR(dclosedate,'" + userInfo.getSsitedate()
//							+ "'),'') as slastmaintenancedate from instrumentmaintenance where " + " ninstrumentcode="
//							+ ninstrumentcode + " and  nstatus="
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ",
//					new InstrumentMaintenance());
				//added by sujatha ALPDJ21-21 
			final List<InstrumentMaintenance> lstInstLastMainDate = (List<InstrumentMaintenance>) jdbcTemplate
					.query("select COALESCE(TO_CHAR(dclosedate, '" + userInfo.getSsitedate()
							+ "'), '') AS slastmaintenancedate " + "from instrumentmaintenance "
							+ "where ninstrumentcode = " + ninstrumentcode + " " + "AND nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "ORDER BY dclosedate ASC", new InstrumentMaintenance());

			returnobj.put("date", lstInstLastMainDate.get(lstInstLastMainDate.size() - 1).getSlastmaintenancedate());
			return new ResponseEntity<Object>(returnobj, HttpStatus.OK);

		} else {
			returnobj.put("date", objTimeZoneDAO.getLocalTimeByZone(userInfo).getBody());
			return new ResponseEntity<Object>(returnobj, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to unit table. Need to check for
	 * duplicate entry of InstrumentMaintenance name for the specified site before
	 * saving into database. Need to check whether instrument delete or not
	 * 
	 * @param instMain [InstrumentMaintenance] object holding details to be added in
	 *                 instrumentmaintenance table
	 * @return inserted instMain object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createInstrumentMaintenance(InstrumentMaintenance instMain, final UserInfo userInfo)
			throws Exception {

		final String sQueryLock = " lock table lockinstrument " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final List<Object> savedInstList = new ArrayList<>();

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();
		if (instMain.getDlastmaintenancedate() != null) {
			instMain.setSlastmaintenancedate(
					dateUtilityFunction.instantDateToString(instMain.getDlastmaintenancedate()).replace("T", " "));
			lstDateField.add("slastmaintenancedate");
			lstDatecolumn.add("ntzlastmaintenancedate");
		}
		if (instMain.getDopendate() != null) {
			instMain.setSopendate(dateUtilityFunction.instantDateToString(instMain.getDopendate()).replace("T", " "));
			lstDateField.add("sopendate");
			lstDatecolumn.add("ntzopendate");
		}
		if (instMain.getDclosedate() != null) {
			instMain.setSclosedate(dateUtilityFunction.instantDateToString(instMain.getDclosedate()).replace("T", " "));
			lstDateField.add("sclosedate");
			lstDatecolumn.add("ntzclosedate");
		}
	//added by sujatha ALPDJ21-21 
//		if (instMain.getDduedate() != null) {
//			instMain.setSduedate(dateUtilityFunction.instantDateToString(instMain.getDduedate()).replace("T", " "));
//			lstDateField.add("sduedate");
//			lstDatecolumn.add("ntzduedate");
//
//		}
		Date duedate = null;
		if (instMain.getDlastmaintenancedate() != null) {
			String qry = "select i.nnextmaintenanceperiod, i.nnextmaintenance from instrument i where ninstrumentcode = "
					+ instMain.getNinstrumentcode();
			final Instrument objins = (Instrument) jdbcUtilityFunction.queryForObject(qry, Instrument.class,
					jdbcTemplate);

			Calendar calendar = Calendar.getInstance();
			duedate = Date.from(instMain.getDlastmaintenancedate());
			calendar.setTime(duedate);
			if (objins.getNnextmaintenanceperiod() == Enumeration.Period.Days.getPeriod()) {
				calendar.add(Calendar.DAY_OF_MONTH, objins.getNnextmaintenance());
			} else if (objins.getNnextmaintenanceperiod() == Enumeration.Period.Weeks.getPeriod()) {
				calendar.add(Calendar.WEEK_OF_MONTH, objins.getNnextmaintenance());
			} else if (objins.getNnextmaintenanceperiod() == Enumeration.Period.Month.getPeriod()) {
				calendar.add(Calendar.MONTH, objins.getNnextmaintenance());
			}
			duedate = calendar.getTime();
			instMain.setSduedate(dateUtilityFunction.instantDateToString(duedate.toInstant()).replace("T", " "));
			lstDateField.add("sduedate");
			lstDatecolumn.add("ntzduedate");
		}

		 InstrumentMaintenance convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(instMain, lstDateField, lstDatecolumn, true, userInfo),
				new TypeReference<InstrumentMaintenance>() {
				});

		final Instrument instrument = checkMainInstrumentIsPresent(instMain.getNinstrumentcode());
		if (instrument != null) {

			if (instMain.isIsreadonly() ? instMain.isIsreadonly()
					: instMain.getSopendate() == null && instMain.getSclosedate() == null) {

				String instMainSeq = "select nsequenceno from seqnoinstrumentmanagement where stablename='instrumentmaintenance'";
				SeqNoInstrumentManagement objseq = (SeqNoInstrumentManagement) jdbcUtilityFunction
						.queryForObject(instMainSeq, SeqNoInstrumentManagement.class, jdbcTemplate);
				int seqNo = objseq.getNsequenceno();
				seqNo = seqNo + 1;
				String instMainInsert = "";
				final String dopendate = instMain.getSopendate() == null ? null
						: "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'";
				final String dclosedate = instMain.getSclosedate() == null ? null
						: "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'";

				final int noffsetdopendate = instMain.getSopendate() == null ? 0
						: dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());
				final int noffsetdclosedate = instMain.getSclosedate() == null ? 0
						: dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());

				instMainInsert = "insert into instrumentmaintenance(ninstrumentmaintenancecode,ninstrumentcode,"
						+ " dduedate,noffsetdduedate,ntzduedate,dopendate,noffsetdopendate,ntzopendate,dlastmaintenancedate,noffsetdlastmaintenancedate,ntzlastmaintenancedate,"
						+ "nopenusercode,sopenreason,ncloseusercode,sclosereason,dclosedate,noffsetdclosedate,ntzclosedate,nmaintenancestatus,dmodifieddate,nsitecode,nstatus)"
						+ " values(" + seqNo + "," + instMain.getNinstrumentcode() + ", '"
						+ convertedObject.getSduedate() + "'," + convertedObject.getNoffsetdduedate() + ","
						+ instMain.getNtzduedate() + "," + dopendate + "," + noffsetdopendate + ","
						+ instMain.getNtzopendate() + ",'" + convertedObject.getSlastmaintenancedate() + "' ,"
						+ convertedObject.getNoffsetdlastmaintenancedate() + "," + instMain.getNtzlastmaintenancedate()
						+ "," + instMain.getNopenusercode() + ",'"
						+ stringUtilityFunction.replaceQuote(instMain.getSopenreason()) + "',"
						+ instMain.getNcloseusercode() + ",'"
						+ stringUtilityFunction.replaceQuote(instMain.getSclosereason()) + "'," + dclosedate + ","
						+ noffsetdclosedate + "," + instMain.getNtzclosedate() + "," + instMain.getNmaintenancestatus()
						+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNsitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(instMainInsert);

				instMainSeq = "update seqnoinstrumentmanagement set nsequenceno=" + seqNo
						+ " where stablename='instrumentmaintenance'";
				jdbcTemplate.execute(instMainSeq);

				instMain.setNinstrumentmaintenancecode(seqNo);

				if (instMain.getDlastmaintenancedate() != null) {
					Date date = Date.from(instMain.getDlastmaintenancedate());
					instMain.setSlastmaintenancedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
	          //added by sujatha ALPDJ21-21 
				if (duedate != null) {
					instMain.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(duedate));
				}
//				if (instMain.getDduedate() != null) {
//					Date date = Date.from(instMain.getDduedate());
//					instMain.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
//				}
//				if (instMain.getNmaintenancestatus() == Enumeration.TransactionStatus.MAINTANENCE
//						.gettransactionstatus()) {
//					if (instMain.getDopendate() != null) {
//						Date date = Date.from(instMain.getDopendate());
//						instMain.setSopendate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
//					}
//					if (instMain.getDclosedate() != null) {
//						Date date = Date.from(instMain.getDclosedate());
//						instMain.setSclosedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
//					}
//				}
				savedInstList.add(instMain);

				auditUtilityFunction.fnInsertAuditAction(savedInstList, 1, null,
						Arrays.asList("IDS_ADDINSTRUMENTMAINTENANCE"), userInfo);
			} else {
				String instopendate = "";

				if (instMain.getSopendate() != null) {

					final String dopendate = convertedObject.getSopendate() == null ? null
							: "'" + convertedObject.getSopendate() + "'";
					final String openreason = instMain.getSopenreason() == null ? "" : instMain.getSopenreason();
					instopendate = "update instrumentmaintenance set dopendate=" + dopendate + ",noffsetdopendate="
							+ convertedObject.getNoffsetdopendate() + ",sopenreason='"
							+ stringUtilityFunction.replaceQuote(openreason) + "',nmaintenancestatus="
							+ instMain.getNmaintenancestatus() + ", nopenusercode=" + instMain.getNopenusercode()
							+ ",ntzopendate=" + instMain.getNtzopendate()
							+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "' where ninstrumentmaintenancecode=" + instMain.getNinstrumentmaintenancecode();
					jdbcTemplate.execute(instopendate);

					if (instMain.getDopendate() != null) {
//						Date date = Date.from(instMain.getDopendate());
//						instMain.setSopendate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						instMain.setSopendate((dateUtilityFunction.instantDateToStringWithFormat(instMain.getDopendate(),userInfo.getSsitedate())));     	//added by sujatha ALPDJ21-21 
					}

					savedInstList.add(instMain);

					auditUtilityFunction.fnInsertAuditAction(savedInstList, 1, null, Arrays.asList("IDS_OPENDATE"),
							userInfo);
				}
				if (instMain.getSclosedate() != null) {
						//added by sujatha ALPDJ21-21 
					Date dueFromClose = Date.from(instMain.getDclosedate());
					String qryCl = "select i.nnextmaintenanceperiod, i.nnextmaintenance from instrument i where ninstrumentcode = "
							+ instMain.getNinstrumentcode();
					final Instrument objins = (Instrument) jdbcUtilityFunction.queryForObject(qryCl, Instrument.class,
							jdbcTemplate);
					Calendar calendar = Calendar.getInstance();
					dueFromClose = Date.from(instMain.getDclosedate());
					calendar.setTime(dueFromClose);
					if (objins.getNnextmaintenanceperiod() == Enumeration.Period.Days.getPeriod()) {
						calendar.add(Calendar.DAY_OF_MONTH, objins.getNnextmaintenance());
					} else if (objins.getNnextmaintenanceperiod() == Enumeration.Period.Weeks.getPeriod()) {
						calendar.add(Calendar.WEEK_OF_MONTH, objins.getNnextmaintenance());
					} else if (objins.getNnextmaintenanceperiod() == Enumeration.Period.Month.getPeriod()) {
						calendar.add(Calendar.MONTH, objins.getNnextmaintenance());
					}
					dueFromClose = calendar.getTime();
					
					if (dueFromClose != null) {
						instMain.setSduedate(dateUtilityFunction.instantDateToString(dueFromClose.toInstant())
								.replace("T", " "));
						lstDateField.clear();
						lstDatecolumn.clear();
						lstDateField.add("sduedate");
						lstDatecolumn.add("ntzduedate");
					}

					InstrumentMaintenance conv2 = objMapper.convertValue(dateUtilityFunction
							.convertInputDateToUTCByZone(instMain, lstDateField, lstDatecolumn, true, userInfo),
							new TypeReference<InstrumentMaintenance>() {
							});

					final String dclosedate = conv2.getSclosedate() == null ? null
							: "'" + conv2.getSclosedate() + "'";

					final String closereason = instMain.getSclosereason() == null ? "" : instMain.getSclosereason();

					final String sduedate = conv2.getSduedate() == null ? null
							: "'" + conv2.getSduedate() + "'";
					
					final String query = "select dopendate from instrumentmaintenance where  ninstrumentmaintenancecode="
							+ instMain.getNinstrumentmaintenancecode();
					Instant dopendateData = jdbcTemplate.queryForObject(query, Instant.class);

					if (dopendateData.isBefore(convertedObject.getDclosedate())) {

						instopendate = "update instrumentmaintenance set dclosedate=" + dclosedate
								+ ",noffsetdclosedate=" + conv2.getNoffsetdclosedate() + ",sclosereason='"
								+ stringUtilityFunction.replaceQuote(closereason) + "' ,nmaintenancestatus="
								+ instMain.getNmaintenancestatus() + ", ncloseusercode=" + instMain.getNcloseusercode()
								+ ",ntzclosedate=" + instMain.getNtzclosedate()+ ", dduedate=" + sduedate             	//added by sujatha ALPDJ21-21 
								+ ", noffsetdduedate=" + conv2.getNoffsetdduedate() + ", ntzduedate="
								+ instMain.getNtzduedate()
								+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' where ninstrumentmaintenancecode=" + instMain.getNinstrumentmaintenancecode();
						jdbcTemplate.execute(instopendate);

						if (instMain.getDclosedate() != null) {
							Date date = Date.from(instMain.getDclosedate());
							instMain.setSclosedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						}
	                    //added by sujatha ALPDJ21-21 
						if (dueFromClose != null) {
							instMain.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(dueFromClose));
						}
						savedInstList.clear();
						savedInstList.add(instMain);

						auditUtilityFunction.fnInsertAuditAction(savedInstList, 1, null, Arrays.asList("IDS_CLOSEDATE"),
								userInfo);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_OPENBEFORECLOSE",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				}

			}
			final List<InstrumentMaintenance> lstInstMain = getInstrumentMaintenance(instMain.getNinstrumentcode(), 0,
					userInfo);

			if (!lstInstMain.isEmpty()) {
				objMap.put("selectedInstrumentMaintenance", lstInstMain.get(lstInstMain.size() - 1));
				objMap.put("InstrumentMaintenance", lstInstMain);
				objMap.putAll(getInstrumentMaintenanceFile(
						lstInstMain.get(lstInstMain.size() - 1).getNinstrumentmaintenancecode(), userInfo));
			} else {
				objMap.put("selectedInstrumentMaintenance", lstInstMain);
				objMap.put("InstrumentMaintenance", lstInstMain);
			}
			return new ResponseEntity<>(objMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	/**
	 * This method is used to fetch the active instrumentmaintenance objects for the
	 * specified instrumentmaintenance name and site.
	 * 
	 * @param ninstrumentmaintenancecode [int] code of the of the
	 *                                   instrumentmaintenance
	 * @param userInfo                   [UserInfo] objects of the UserInfo
	 * @return list of active unit code(s) based on the specified unit name and site
	 * @throws Exception
	 */

	@Override
	public InstrumentMaintenance getActiveInstrumentMaintenanceById(final int ninstrumentmaintenancecode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select im.ninstrumentmaintenancecode," + "im.dlastmaintenancedate,im.dduedate,"
				+ " i.sinstrumentid,im.ninstrumentcode,im.nmaintenancestatus ,im.nstatus,tz1.stimezoneid as stzopendate,"
				+ " tz2.stimezoneid as stzclosedate,tz3.stimezoneid as stzlastmaintenancedate,tz4.stimezoneid as stzduedate,"
				+ " tz1.ntimezonecode,tz2.ntimezonecode,tz3.ntimezonecode,tz4.ntimezonecode,im.ntzduedate,im.ntzlastmaintenancedate, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, "
				+ " COALESCE(TO_CHAR(im.dlastmaintenancedate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as slastmaintenancedate," + " COALESCE(TO_CHAR(im.dduedate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sduedate"
				+ " from instrument i,instrumentmaintenance im ,transactionstatus ts,timezone tz1,timezone tz2,timezone tz3,timezone tz4  "
				+ " where im.ninstrumentmaintenancecode=" + ninstrumentmaintenancecode
				+ " and im.ntzopendate=tz1.ntimezonecode and im.ntzclosedate=tz2.ntimezonecode and im.ntzlastmaintenancedate=tz3.ntimezonecode and im.ntzduedate=tz4.ntimezonecode and "
				+ " im.ninstrumentcode=i.ninstrumentcode and im.nmaintenancestatus=ts.ntranscode " + " and im.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and im.nsitecode = "
				+ userInfo.getNtranssitecode() + "" + " and i.nsitecode = " + userInfo.getNmastersitecode() + "";

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<InstrumentMaintenance> lstInstMainGet = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new InstrumentMaintenance()),
						Arrays.asList("slastmaintenancedate", "sduedate"),
						Arrays.asList("ntzlastmaintenancedate", "ntzduedate"), userInfo, false, null, false),
				new TypeReference<List<InstrumentMaintenance>>() {
				});
		if (lstInstMainGet.isEmpty()) {
			InstrumentMaintenance InstrumentMaintenance = null;
			return InstrumentMaintenance;
		} else {
			return (InstrumentMaintenance) lstInstMainGet.get(0);

		}
	}

	/**
	 * This method is used to fetch the active unit objects for the specified unit
	 * name and site.
	 * 
	 * @param sunitname       [String] name of the unit
	 * @param nmasterSiteCode [int] site code of the unit
	 * @return list of active unit code(s) based on the specified unit name and site
	 * @throws Exception
	 */
	public InstrumentMaintenance getInstrumentMaintenanceByDate(final int nmaintenancestatus,
			final String maintenancedate, final String duedate, final int ninstrumentcode) throws Exception {

		final String query = "select * from instrumentmaintenance " + "where dlastmaintenancedate='" + maintenancedate
				+ "' and dduedate='" + duedate + "' and nmaintenancestatus=" + nmaintenancestatus
				+ " and ninstrumentcode=" + ninstrumentcode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		return (InstrumentMaintenance) jdbcUtilityFunction.queryForObject(query, InstrumentMaintenance.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry in instrumentmaintenance table. Need to
	 * validate that the instrumentmaintenance object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of unit name
	 * for the specified site before saving into database.
	 * 
	 * @param instMain [InstrumentMaintenance] object holding details to be updated
	 *                 in instrumentmaintenance table
	 * @return response entity object holding response status and data of updated
	 *         unit object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateInstrumentMaintenance(InstrumentMaintenance instMain, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final List<Object> beforeSavedInstList = new ArrayList<>();
		final List<Object> savedInstList = new ArrayList<>();
		List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		if (instMain.getDlastmaintenancedate() != null) {
			instMain.setSlastmaintenancedate(dateUtilityFunction.instantDateToString(instMain.getDlastmaintenancedate())
					.replace("T", " ").replace("Z", ""));
			lstDateField.add("slastmaintenancedate");
			lstDatecolumn.add("ntzlastmaintenancedate");

		}
		if (instMain.getDduedate() != null) {
			instMain.setSduedate(
					dateUtilityFunction.instantDateToString(instMain.getDduedate()).replace("T", " ").replace("Z", ""));
			lstDateField.add("sduedate");
			lstDatecolumn.add("ntzduedate");

		}

		final InstrumentMaintenance convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(instMain, lstDateField, lstDatecolumn, true, userInfo),
				new TypeReference<InstrumentMaintenance>() {
				});

		final InstrumentMaintenance instMainDetails = (InstrumentMaintenance) getActiveInstrumentMaintenanceById(
				instMain.getNinstrumentmaintenancecode(), userInfo);
		if (instMain.getDlastmaintenancedate() != null) {
			Date date = Date.from(instMainDetails.getDlastmaintenancedate());
			instMainDetails.setSlastmaintenancedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		if (instMain.getDduedate() != null) {
			Date date = Date.from(instMainDetails.getDduedate());
			instMainDetails.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
		}
		if (instMainDetails == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ninstrumentmaintenancecode from instrumentmaintenance where dlastmaintenancedate = '"
					+ instMain.getSlastmaintenancedate() + "' and nmaintenancestatus="
					+ instMain.getNmaintenancestatus() + " and ninstrumentmaintenancecode<> "
					+ instMain.getNinstrumentmaintenancecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<InstrumentMaintenance> instMainList = (List<InstrumentMaintenance>) jdbcTemplate
					.query(queryString, new InstrumentMaintenance());
			if (instMainList.isEmpty()) {
				final String query = "update instrumentmaintenance set " + "dlastmaintenancedate='"
						+ convertedObject.getSlastmaintenancedate() + "',noffsetdlastmaintenancedate="
						+ convertedObject.getNoffsetdlastmaintenancedate() + ",dduedate='"
						+ convertedObject.getSduedate() + "',noffsetdduedate=" + convertedObject.getNoffsetdduedate()
						+ ",nmaintenancestatus=" + instMain.getNmaintenancestatus() + "," + "ntzduedate="
						+ instMain.getNtzduedate() + "," + "ntzlastmaintenancedate="
						+ instMain.getNtzlastmaintenancedate() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentmaintenancecode="
						+ instMain.getNinstrumentmaintenancecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(query);

				final List<InstrumentMaintenance> lstInstrumentMaintenance = getInstrumentMaintenance(
						instMain.getNinstrumentcode(), instMain.getNinstrumentmaintenancecode(), userInfo);

				if (lstInstrumentMaintenance.size() > 0) {
					objMap.put("selectedInstrumentMaintenance",
							lstInstrumentMaintenance.get(lstInstrumentMaintenance.size() - 1));
					if (lstInstrumentMaintenance.get(0).getDlastmaintenancedate() != null) {
						Date date = Date.from(lstInstrumentMaintenance.get(0).getDlastmaintenancedate());
						instMain.setSlastmaintenancedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					if (lstInstrumentMaintenance.get(0).getDduedate() != null) {
						Date date = Date.from(lstInstrumentMaintenance.get(0).getDduedate());
						instMain.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
				}
				savedInstList.add(instMain);
				beforeSavedInstList.add(instMainDetails);

				auditUtilityFunction.fnInsertAuditAction(savedInstList, 2, beforeSavedInstList,
						Arrays.asList("IDS_EDITINSTRUMENTMAINTENANCE"), userInfo);

				return new ResponseEntity<>(objMap, HttpStatus.OK);
			}

			else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("ALREADYEXIST", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to retrieve list of all active
	 * InstrumentMaintenanceOpenCloseDateStatus for the specified site. need to
	 * check whether instrument disposed or not
	 * 
	 * @param userInfo    [UserInfo] nmasterSiteCode [int] primary key of site
	 *                    object for which the list is to be fetched
	 * @param objInstMain [InstrumentMaintenance] object of the
	 *                    InstrumentMaintenance
	 * @return response entity object holding response status and list of all active
	 *         units
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteInstrumentMaintenance(InstrumentMaintenance objInstMain, UserInfo userInfo)
			throws Exception {
		final List<Object> deletedInstList = new ArrayList<>();

		Map<String, Object> outMap = new HashMap<String, Object>();

		final InstrumentMaintenance instMainDetails = (InstrumentMaintenance) getActiveInstrumentMaintenanceById(
				objInstMain.getNinstrumentmaintenancecode(), userInfo);
		int checkMainIsPresent = 0;
		checkMainIsPresent = jdbcTemplate.queryForObject(
				" select count(*) from instrumentmaintenance where dopendate is not null and dclosedate is not null and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentmaintenancecode=" + objInstMain.getNinstrumentmaintenancecode() + "",
				Integer.class);

		if (checkMainIsPresent != 0) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYCLOSED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else {
			if (instMainDetails == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				int statusCheck = 0;

				statusCheck = jdbcTemplate
						.queryForObject("select ninstrumentstatus from instrument where ninstrumentcode="
								+ objInstMain.getNinstrumentcode(), Integer.class);
				if (statusCheck != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {

					boolean validRecord = false;
					validatorDel = projectDAOSupport.validateDeleteRecord(
							Integer.toString(objInstMain.getNinstrumentmaintenancecode()), userInfo);
					if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
						validRecord = true;
					} else {
						validRecord = false;
					}
					if (validRecord) {
						String updateQuery = "";

						updateQuery = "update instrumentmaintenance set nstatus = "
								+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' where ninstrumentmaintenancecode = " + objInstMain.getNinstrumentmaintenancecode()
								+ ";";

						String qry = "select * from instrumentfile where ninstrumentlogcode = "
								+ objInstMain.getNinstrumentmaintenancecode() + " and ninstrumentlogtypecode = "
								+ Enumeration.InstrumentLogType.INSTRUMENT_MAINTENANCE.getinstrumentlogtype()
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " ;";
						final List<InstrumentFile> oldInstrumentFile = (List<InstrumentFile>) jdbcTemplate.query(qry,
								new InstrumentFile());

						updateQuery = updateQuery + "update instrumentfile set nstatus = "
								+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstrumentlogcode = "
								+ objInstMain.getNinstrumentmaintenancecode() + " and ninstrumentlogtypecode= "
								+ Enumeration.InstrumentLogType.INSTRUMENT_MAINTENANCE.getinstrumentlogtype()
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " ;";

						jdbcTemplate.execute(updateQuery);

						final List<InstrumentMaintenance> lstInstMain = getInstrumentMaintenance(
								objInstMain.getNinstrumentcode(), 0, userInfo);
						if (lstInstMain != null && lstInstMain.size() > 0) {
							outMap.put("selectedInstrumentMaintenance", lstInstMain.get(lstInstMain.size() - 1));
							outMap.putAll(getInstrumentMaintenanceFile(
									lstInstMain.get(lstInstMain.size() - 1).getNinstrumentmaintenancecode(), userInfo));

						}
						outMap.put("InstrumentMaintenance", lstInstMain);
						if (instMainDetails.getDlastmaintenancedate() != null) {
							Date date = Date.from(instMainDetails.getDlastmaintenancedate());
							instMainDetails.setSlastmaintenancedate(
									new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						}
						if (instMainDetails.getDduedate() != null) {
							Date date = Date.from(instMainDetails.getDduedate());
							instMainDetails.setSduedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						}
						if (instMainDetails.getDopendate() != null) {
							Date date = Date.from(instMainDetails.getDopendate());
							instMainDetails.setSopendate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						}
						if (instMainDetails.getDclosedate() != null) {
							Date date = Date.from(instMainDetails.getDclosedate());
							instMainDetails.setSclosedate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
						}

						final List<String> multilingualIDList = new ArrayList<>();

						deletedInstList.add(oldInstrumentFile);
						multilingualIDList.add("IDS_INSTRUMENTMAINTENANCEFILE/LINK");
						final List<InstrumentMaintenance> lst = new ArrayList<>();
						lst.add(instMainDetails);
						deletedInstList.add(lst);
						multilingualIDList.add("IDS_DELETEINSTRUMENTMAINTENANCE");

						auditUtilityFunction.fnInsertListAuditAction(deletedInstList, 1, null, multilingualIDList,
								userInfo);

						return new ResponseEntity<Object>(outMap, HttpStatus.OK);

					} else {
						return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
	}

	/**
	 * This method is used to retrieve list of all active
	 * InstrumentMaintenanceOpenCloseDateStatus for the specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @param inputMap [Map] list used to check the whether instrument status
	 *                 disposed or not
	 * @return response entity object holding response status and list of all active
	 *         units
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getInstrumentMaintenanceOpenCloseDateStatus(UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception {
		Map<String, Object> returnobj = new HashMap<>();
		int statusCheck = 0;
		if (inputMap.get("ninstrumentcode") != null) {
			statusCheck = jdbcTemplate.queryForObject(
					"select ninstrumentstatus from instrument where ninstrumentcode=" + inputMap.get("ninstrumentcode"),
					Integer.class);
		}
		if (statusCheck != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {
			final String query = "select  * from instrumentmaintenance  where ninstrumentmaintenancecode= "
					+ (Integer) inputMap.get("ninstrumentmaintenancecode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "";

			List<InstrumentMaintenance> checkMainIsPresent = (List<InstrumentMaintenance>) jdbcTemplate.query(query,
					new InstrumentMaintenance());
			if (checkMainIsPresent.size() != 0) {
				final String strQuery = " select ntranscode," + " coalesce(jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "',"
						+ " jsondata->'stransdisplaystatus'->>'en-US') as stransstatus "
						+ "  from transactionstatus where " + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntranscode in ("
						+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.UNDERMAINTANENCE.gettransactionstatus() + ");";
				returnobj.put("MaintenanceStatus", jdbcTemplate.query(strQuery, new TransactionStatus()));
				return new ResponseEntity<>(returnobj, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to retrieve list of all active InstrumentMaintenancefile
	 * for the specified site.
	 * 
	 * @param userInfo           [UserInfo] nmasterSiteCode [int] primary key of
	 *                           site object for which the list is to be fetched
	 * @param ninstrumentlogcode [int] code which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         units
	 * @throws Exception that are thrown from this DAO layer
	 */

	public Map<String, Object> getInstrumentMaintenanceFile(final int ninstrumentlogcode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final String queryformat = "COALESCE(TO_CHAR(dcreateddate,'" + objUserInfo.getSpgdatetimeformat() + "'),'-') ";

		final String query = "select insf.ninstrumentfilecode, insf.ninstrumentlogcode, insf.ninstrumentlogtypecode, "
				+ " insf.nattachmenttypecode, insf.sfilename, insf.sfiledesc, insf.ssystemfilename, insf.nfilesize, insf.nsitecode,"
				+ " insf.nstatus, case when insf.nlinkcode = -1 then cast(insf.nfilesize as text) else '-' end sfilesize,"
				+ " case when insf.nlinkcode = -1 then " + queryformat + " else '-' end  screateddate, "
				+ " case when insf.nlinkcode = -1 then '-' else lm.jsondata->>'slinkname' end slinkname, insf.noffsetdcreateddate "
				+ " from instrumentfile insf, linkmaster lm where insf.ninstrumentlogcode=" + ninstrumentlogcode
				+ " and insf.ninstrumentlogtypecode=3 " + " and insf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and insf.nlinkcode=lm.nlinkcode and "
				+ " insf.nstatus=lm.nstatus";

		outputMap.put("MaintenanceFileData",
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new InstrumentFile()),
						Arrays.asList("screateddate"), Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false,
						null, false));

		return outputMap;

	}

	/**
	 * This method is used to add a new entry to instrumentfile table. Need to check
	 * whether instrument is available or not
	 * 
	 * @param objUnit [Unit] object holding details to be added in unit table
	 * @return inserted unit object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createInstrumentMaintenanceFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {

		final String sQueryLock = " lock  table instrumentfile "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final ObjectMapper objMapper = new ObjectMapper();
		final List<InstrumentFile> lstReqInstFile = objMapper.readValue(request.getParameter("instrumentFile"),
				new TypeReference<List<InstrumentFile>>() {
				});

		final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS);
		final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
		final String screatedDate = dateUtilityFunction.instantDateToString(instantDate);

		lstReqInstFile.forEach(objtf -> {
			objtf.setDcreateddate(instantDate);
			if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					|| objtf.getNattachmenttypecode() == Enumeration.AttachmentType.LINK.gettype()) {
				objtf.setDcreateddate(instantDate);
				objtf.setNoffsetdcreateddate(noffset);
				objtf.setScreateddate(screatedDate.replace("T", " "));
			}

		});

		int seqNo = 0;
		if (lstReqInstFile != null && lstReqInstFile.size() > 0) {
			final InstrumentMaintenance objInstMain = checkInstrumentMaintenanceIsPresent(
					lstReqInstFile.get(0).getNinstrumentlogcode());

			if (objInstMain != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqInstFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {

					String instMainSeq = "select nsequenceno from seqnoinstrumentmanagement where stablename='instrumentfile'";
					SeqNoInstrumentManagement objseq = (SeqNoInstrumentManagement) jdbcUtilityFunction
							.queryForObject(instMainSeq, SeqNoInstrumentManagement.class, jdbcTemplate);
					seqNo = objseq.getNsequenceno();
					seqNo = seqNo + 1;

					String instMain = "";
					instMain = "insert into instrumentfile(ninstrumentfilecode,ninstrumentlogcode,ninstrumentlogtypecode,nlinkcode,"
							+ "nattachmenttypecode,sfilename,sfiledesc,ssystemfilename,"
							+ "dcreateddate,ntzcreateddate,noffsetdcreateddate,nfilesize,dmodifieddate,nsitecode,nstatus)"
							+ " values(" + seqNo + "," + lstReqInstFile.get(0).getNinstrumentlogcode() + ","
							+ lstReqInstFile.get(0).getNinstrumentlogtypecode() + ","
							+ lstReqInstFile.get(0).getNlinkcode() + ","
							+ lstReqInstFile.get(0).getNattachmenttypecode() + ",'"
							+ stringUtilityFunction.replaceQuote(lstReqInstFile.get(0).getSfilename()) + "','"
							+ stringUtilityFunction.replaceQuote(lstReqInstFile.get(0).getSfiledesc()) + "','"
							+ stringUtilityFunction.replaceQuote(lstReqInstFile.get(0).getSsystemfilename()) + "','"
							+ lstReqInstFile.get(0).getDcreateddate() + "'," + objUserInfo.getNtimezonecode() + ","
							+ lstReqInstFile.get(0).getNoffsetdcreateddate() + ", "
							+ lstReqInstFile.get(0).getNfilesize() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
							+ objUserInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(instMain);

					instMain = "update seqnoinstrumentmanagement set nsequenceno=" + seqNo
							+ " where stablename='instrumentfile'";
					jdbcTemplate.execute(instMain);

					lstReqInstFile.get(0).setNinstrumentfilecode(seqNo);

					final List<Object> listObject = new ArrayList<Object>();
					listObject.add(lstReqInstFile);

					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, Arrays.asList(
							lstReqInstFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_ADDINSTRUMENTMAINTENANCEFILE"
									: "IDS_ADDINSTRUMENTMAINTENANCELINK"),
							objUserInfo);

				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return new ResponseEntity<Object>(
					getInstrumentMaintenanceFile(lstReqInstFile.get(0).getNinstrumentlogcode(), objUserInfo),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to update entry in Instrumentfile table. Need to check
	 * that there should be only one default unit for a site
	 * 
	 * @param objInstrumentFile [InstrumentFile] object holding details to be
	 *                          updated in unit table
	 * @return response entity object holding response status and data of updated
	 *         InstrumentFile object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateInstrumentMaintenanceFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final List<InstrumentFile> lstInstrumentFile = objMapper.readValue(request.getParameter("instrumentFile"),
				new TypeReference<List<InstrumentFile>>() {
				});
		if (lstInstrumentFile != null && lstInstrumentFile.size() > 0) {
			final InstrumentFile objInstrumentFile = lstInstrumentFile.get(0);

			final InstrumentMaintenance objInstMain = checkInstrumentMaintenanceIsPresent(
					objInstrumentFile.getNinstrumentlogcode());

			if (objInstMain != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objInstrumentFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select * from instrumentfile where ninstrumentfilecode = "
							+ objInstrumentFile.getNinstrumentfilecode() + " and ninstrumentlogtypecode="
							+ objInstrumentFile.getNinstrumentlogtypecode() + "  and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final InstrumentFile objTF = (InstrumentFile) jdbcUtilityFunction.queryForObject(sQuery,
							InstrumentFile.class, jdbcTemplate);

					if (objTF != null) {
						String ssystemfilename = "";
						if (objInstrumentFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							ssystemfilename = objInstrumentFile.getSsystemfilename();
						}

						final String sUpdateQuery = "update instrumentfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objInstrumentFile.getSfilename()) + "',"
								+ " sfiledesc=N'" + stringUtilityFunction.replaceQuote(objInstrumentFile.getSfiledesc())
								+ "', ssystemfilename= N'" + stringUtilityFunction.replaceQuote(ssystemfilename)
								+ "', nattachmenttypecode = " + objInstrumentFile.getNattachmenttypecode()
								+ ", nfilesize =" + objInstrumentFile.getNfilesize() + ", nlinkcode ="
								+ objInstrumentFile.getNlinkcode() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ninstrumentfilecode = "
								+ objInstrumentFile.getNinstrumentfilecode();

						jdbcTemplate.execute(sUpdateQuery);

						final List<Object> lstOldObject = new ArrayList<Object>();
						objTF.setNinstrumentcode(lstInstrumentFile.get(0).getNinstrumentcode());
						lstOldObject.add(objTF);
						auditUtilityFunction.fnInsertAuditAction(lstInstrumentFile, 2, lstOldObject,
								Arrays.asList(lstInstrumentFile.get(0)
										.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
												? "IDS_EDITINSTRUMENTMAINTENANCEFILE"
												: "IDS_EDITINSTRUMENTMAINTENANCELINK"),
								objUserInfo);
						return new ResponseEntity<Object>(
								getInstrumentMaintenanceFile(objInstrumentFile.getNinstrumentlogcode(), objUserInfo),
								HttpStatus.OK);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method id used to delete an entry in Instrumentfile table Need to check
	 * the record is already deleted or not
	 * 
	 * @param objInstrumentFile [InstrumentFile] an Object holds the record to be
	 *                          deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InstrumentFile object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteInstrumentMaintenanceFile(InstrumentFile objInstrumentFile,
			final UserInfo objUserInfo) throws Exception {
		final InstrumentMaintenance objInstMain = checkInstrumentMaintenanceIsPresent(
				objInstrumentFile.getNinstrumentlogcode());
		if (objInstMain != null) {

			boolean validRecord = false;
			validatorDel = projectDAOSupport
					.validateDeleteRecord(Integer.toString(objInstrumentFile.getNinstrumentlogcode()), objUserInfo);
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}
			if (validRecord) {
				final String sQuery = "select * from instrumentfile where ninstrumentfilecode = "
						+ objInstrumentFile.getNinstrumentfilecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final InstrumentFile objIF = (InstrumentFile) jdbcUtilityFunction.queryForObject(sQuery,
						InstrumentFile.class, jdbcTemplate);
				if (objIF != null) {

					final String sUpdateQuery = "update instrumentfile set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ninstrumentfilecode = "
							+ objInstrumentFile.getNinstrumentfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<Object> lstObject = new ArrayList<>();
					lstObject.add(objInstrumentFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null,
							Arrays.asList(objInstrumentFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
									.gettype() ? "IDS_DELETEINSTRUMENTMAINTENANCEFILE"
											: "IDS_DELETEINSTRUMENTMAINTENANCELINK"),
							objUserInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				return new ResponseEntity<Object>(
						getInstrumentMaintenanceFile(objInstrumentFile.getNinstrumentlogcode(), objUserInfo),
						HttpStatus.OK);

			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to retrieve list of all active viewAttachedInstrumentFile
	 * for the specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public Map<String, Object> viewAttachedInstrumentFile(InstrumentFile objInstrumentFile, final UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		final List<String> multilingualIDList = new ArrayList<>();
		if (objInstrumentFile != null) {
			String sQuery = "select * from instrumentfile where ninstrumentfilecode = "
					+ objInstrumentFile.getNinstrumentfilecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final InstrumentFile objIF = (InstrumentFile) jdbcUtilityFunction.queryForObject(sQuery,
					InstrumentFile.class, jdbcTemplate);
			if (objIF != null) {
				if (objIF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(objIF.getSsystemfilename(), -1, objUserInfo, "", "");// Folder
																													// Name
																													// -
																													// master
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ objIF.getNlinkcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,
							jdbcTemplate);
					map.put("AttachLink", objlinkmaster.getSlinkname() + objIF.getSfilename());
					objInstrumentFile.setScreateddate(null);
				}
				if (objInstrumentFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					if (objInstrumentFile
							.getNinstrumentlogtypecode() == Enumeration.InstrumentLogType.INSTRUMENT_VALIDATION
									.getinstrumentlogtype()) {
						multilingualIDList.add("IDS_VIEWINSTRUMENTVALIDATIONFILE");
					} else if (objInstrumentFile
							.getNinstrumentlogtypecode() == Enumeration.InstrumentLogType.INSTRUMENT_CALIBRATION
									.getinstrumentlogtype()) {
						multilingualIDList.add("IDS_VIEWINSTRUMENTCALIBRATIONFILE");
					} else {
						multilingualIDList.add("IDS_VIEWINSTRUMENTMAINTENANCEFILE");
					}
				} else {
					if (objInstrumentFile
							.getNinstrumentlogtypecode() == Enumeration.InstrumentLogType.INSTRUMENT_VALIDATION
									.getinstrumentlogtype()) {
						multilingualIDList.add("IDS_VIEWINSTRUMENTVALIDATIONLINK");
					} else if (objInstrumentFile
							.getNinstrumentlogtypecode() == Enumeration.InstrumentLogType.INSTRUMENT_CALIBRATION
									.getinstrumentlogtype()) {
						multilingualIDList.add("IDS_VIEWINSTRUMENTCALIBRATIONLINK");
					} else {
						multilingualIDList.add("IDS_VIEWINSTRUMENTMAINTENANCELINK");
					}
				}
				final List<Object> lstObject = new ArrayList<>();

				lstObject.add(objInstrumentFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);

			}
		}
		return map;
	}

	/**
	 * This method is used to retrieve list of all active
	 * InstrumentMaintenanceValidation for the specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InstrumentMaintenanceValidation
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override

	public ResponseEntity<Object> getInstrumentMaintenanceValidation(UserInfo userInfo, final int nFlag,
			final int ninstrumentcode, final int ninstrumentmaintenancecode) throws Exception {
		Map<String, Object> returnobj = new HashMap<>();
		String checkInstMainVal = "";
		String validation = "";
		int statusCheck = 0;

		statusCheck = jdbcTemplate.queryForObject(
				"select ninstrumentstatus from instrument where ninstrumentcode=" + ninstrumentcode, Integer.class);
		if (statusCheck != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {
			if (nFlag == 1) {

				int validation1, validation2, validation3 = 0;
				int nValue = 0;
				validation1 = jdbcTemplate.queryForObject(
						" select count(*) from instrumentmaintenance where ninstrumentcode=" + ninstrumentcode
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode=" + userInfo.getNtranssitecode() + ";",
						Integer.class);

				validation2 = jdbcTemplate
						.queryForObject(" select count(*) from instrumentmaintenance where ninstrumentcode="
								+ ninstrumentcode + " and dopendate is null and dclosedate is null and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
								+ userInfo.getNtranssitecode() + ";", Integer.class);

				validation3 = jdbcTemplate.queryForObject(
						" select count(*) from instrumentmaintenance where dopendate is not null and dclosedate is null and ninstrumentcode="
								+ ninstrumentcode + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
								+ userInfo.getNtranssitecode() + ";",
						Integer.class);

				if (validation1 == 0) {
					nValue = 1;
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}
				if (validation1 > 0 && nValue != 1) {
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}
				if (validation2 != 0) {
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_SELECTOPENDATE";
					returnobj.put("Status", validation);
				}
				if (validation3 != 0) {
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_SELECTCLOSEDATE";
					returnobj.put("Status", validation);
				}
			} else if (nFlag == 2) {

				checkInstMainVal = " select * from instrumentmaintenance where dopendate is null and dclosedate is null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentmaintenancecode=" + ninstrumentmaintenancecode + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";

				InstrumentMaintenance objInstValData1 = (InstrumentMaintenance) jdbcUtilityFunction
						.queryForObject(checkInstMainVal, InstrumentMaintenance.class, jdbcTemplate);

				checkInstMainVal = " select * from instrumentmaintenance where  dclosedate is not null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentmaintenancecode=" + ninstrumentmaintenancecode + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";

				InstrumentMaintenance objInstValData2 = (InstrumentMaintenance) jdbcUtilityFunction
						.queryForObject(checkInstMainVal, InstrumentMaintenance.class, jdbcTemplate);

				checkInstMainVal = " select * from instrumentmaintenance where dopendate is not null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentmaintenancecode=" + ninstrumentmaintenancecode + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";

				InstrumentMaintenance objInstValData3 = (InstrumentMaintenance) jdbcUtilityFunction
						.queryForObject(checkInstMainVal, InstrumentMaintenance.class, jdbcTemplate);
				if (objInstValData1 != null) {

					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}
				if (objInstValData2 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_OPENCLOSEDCANNOTEDIT";
					returnobj.put("Status", validation);

				}
				if (objInstValData3 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_OPENCLOSEDCANNOTEDIT";
					returnobj.put("Status", validation);

				}

			} else if (nFlag == 3) {

				checkInstMainVal = " select * from instrumentmaintenance where dopendate is null and dclosedate is null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentmaintenancecode=" + ninstrumentmaintenancecode + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";

				InstrumentMaintenance objInstMainData1 = (InstrumentMaintenance) jdbcUtilityFunction
						.queryForObject(checkInstMainVal, InstrumentMaintenance.class, jdbcTemplate);
				checkInstMainVal = " select * from instrumentmaintenance where dopendate is not null and dclosedate is not null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentmaintenancecode=" + ninstrumentmaintenancecode + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";

				InstrumentMaintenance objInstMainData2 = (InstrumentMaintenance) jdbcUtilityFunction
						.queryForObject(checkInstMainVal, InstrumentMaintenance.class, jdbcTemplate);

				checkInstMainVal = " select * from instrumentmaintenance where dopendate is not null and dclosedate is null and ninstrumentcode="
						+ ninstrumentcode + "  and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentmaintenancecode=" + ninstrumentmaintenancecode + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";

				InstrumentMaintenance objInstMainData3 = (InstrumentMaintenance) jdbcUtilityFunction
						.queryForObject(checkInstMainVal, InstrumentMaintenance.class, jdbcTemplate);
				if (objInstMainData1 != null) {
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}
				if (objInstMainData2 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_ALREADYOPENED";
					returnobj.put("Status", validation);
				}
				if (objInstMainData3 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_ALREADYOPENED";
					returnobj.put("Status", validation);
				}
			} else if (nFlag == 4) {
				checkInstMainVal = " select * from instrumentmaintenance where dopendate is not null and dclosedate is null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentmaintenancecode=" + ninstrumentmaintenancecode + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";

				InstrumentMaintenance objInstMainData1 = (InstrumentMaintenance) jdbcUtilityFunction
						.queryForObject(checkInstMainVal, InstrumentMaintenance.class, jdbcTemplate);

				checkInstMainVal = " select * from instrumentmaintenance where dopendate is not null and dclosedate is not null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentmaintenancecode=" + ninstrumentmaintenancecode + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";

				InstrumentMaintenance objInstMainData2 = (InstrumentMaintenance) jdbcUtilityFunction
						.queryForObject(checkInstMainVal, InstrumentMaintenance.class, jdbcTemplate);

				checkInstMainVal = " select * from instrumentmaintenance where dopendate is null and ninstrumentcode="
						+ ninstrumentcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ninstrumentmaintenancecode=" + ninstrumentmaintenancecode + " and nsitecode="
						+ userInfo.getNtranssitecode() + "";

				InstrumentMaintenance objInstMainData3 = (InstrumentMaintenance) jdbcUtilityFunction
						.queryForObject(checkInstMainVal, InstrumentMaintenance.class, jdbcTemplate);

				if (objInstMainData1 != null) {
					returnobj.put("OpenCloseStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

				}
				if (objInstMainData2 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_ALREADYCLOSED";
					returnobj.put("Status", validation);

				}
				if (objInstMainData3 != null) {
					returnobj.put("Status", Enumeration.ReturnStatus.FAILED.getreturnstatus());
					validation = "IDS_SELECTOPENDATEBEFORECLOSE";
					returnobj.put("Status", validation);

				}
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return new ResponseEntity<>(returnobj, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active CalibrationRequired for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         CalibrationRequired
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getCalibrationRequired(final Integer ninstrumentCatCode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<>();
		Integer ncalibrationreq = null;
		final String strQuery = "select ncalibrationreq from instrumentcategory where ninstrumentcatcode="
				+ ninstrumentCatCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "";
		ncalibrationreq = jdbcTemplate.queryForObject(strQuery, Integer.class);
		objMap.put("ncalibrationrequired", ncalibrationreq);
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve list of all active InstrumentName for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InstrumentName
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getInstrumentName(final UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<>();
		final String strQuery = "select sinstrumentname,ninstrumentnamecode from instrumentname where  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + "";
		final List<InstrumentName> instrumentName = (List<InstrumentName>) jdbcTemplate.query(strQuery,
				new InstrumentName());
		objMap.put("InstrumentName", instrumentName);
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve list of all active Instrumentlocation for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Instrumentlocation
	 * @throws Exception that are thrown from this DAO layer
	 */

	public ResponseEntity<Object> getInstrumentLocation(final UserInfo userInfo) throws Exception {

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final String query = "select * from instrumentlocation where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and ninstrumentlocationcode>0";
		final List<InstrumentLocation> lstInstrumentLocation = (List<InstrumentLocation>) jdbcTemplate.query(query,
				new InstrumentLocation());
		objMap.put("InstrumentLocation", lstInstrumentLocation);
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve list of all active InstrumenStatus for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InstrumenStatus
	 * @throws Exception that are thrown from this DAO layer
	 */

	public Integer getInstrumenStatus(final int ninstrumentcode, final UserInfo objUserInfo) {

		final int statusCheck = jdbcTemplate.queryForObject(
				"select ninstrumentstatus from instrument where ninstrumentcode=" + ninstrumentcode, Integer.class);

		return statusCheck;
	}

	public String validateInstrumenttransction(final int ninstrumentcode, final int nregionalsitecode,
			final UserInfo userInfo) throws Exception {
		final String returnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		String validationQuery = "select ncalibrationstatus from instrumentcalibration where "
				+ " ninstrumentcalibrationcode = any (select max(ninstrumentcalibrationcode) ninstrumentcalibrationcode from instrumentcalibration where ninstrumentcode = "
				+ ninstrumentcode + "" + " and nsitecode = " + nregionalsitecode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
				+ " and dclosedate is not null and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final InstrumentCalibration objInstrumentCalibration = (InstrumentCalibration) jdbcUtilityFunction
				.queryForObject(validationQuery, InstrumentCalibration.class, jdbcTemplate);

		if (objInstrumentCalibration != null) {
			if (objInstrumentCalibration.getNcalibrationstatus() == Enumeration.TransactionStatus.UNDERCALIBIRATION
					.gettransactionstatus()) {

				return commonFunction.getMultilingualMessage("IDS_INSTUNDERCALIBRATION",
						userInfo.getSlanguagefilename());

			}
		} else {
			return commonFunction.getMultilingualMessage("IDS_CLOSETHECALIBRATION", userInfo.getSlanguagefilename());

		}

		validationQuery = "select nmaintenancestatus from instrumentmaintenance where "
				+ " ninstrumentmaintenancecode = any(select max(ninstrumentmaintenancecode) ninstrumentmaintenancecode from instrumentmaintenance where ninstrumentcode = "
				+ ninstrumentcode + " " + " and nsitecode = " + nregionalsitecode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
				+ " and dclosedate is not null and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final InstrumentMaintenance objInstrumentMaintenance = (InstrumentMaintenance) jdbcUtilityFunction
				.queryForObject(validationQuery, InstrumentMaintenance.class, jdbcTemplate);

		if (objInstrumentMaintenance != null) {
			if (objInstrumentMaintenance.getNmaintenancestatus() == Enumeration.TransactionStatus.UNDERMAINTANENCE
					.gettransactionstatus()) {
				return commonFunction.getMultilingualMessage("IDS_INSTUNDERMAINTANENCE",
						userInfo.getSlanguagefilename());
			}
		} else {
			return commonFunction.getMultilingualMessage("IDS_CLOSETHEMAINTENANCE", userInfo.getSlanguagefilename());
		}

		return returnString;
	}

//	ALPD-3030
	// ALPD-5330 - Gowtham R - Default Section not loaded initially
	// (s.nsitecode=userinfo) - 08-02-2025
	@Override
	public ResponseEntity<Object> getSiteBasedSection(final int nsitecode, final UserInfo userinfo) throws Exception {

		final String strQuery = "select s.* from section s, labsection ls where s.nsectioncode=ls.nsectioncode "
				+ " and ls.nsitecode = " + nsitecode + " and s.nsitecode = " + userinfo.getNmastersitecode()
				+ " and ls.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by s.nsectioncode ";

		return new ResponseEntity<>((List<Section>) jdbcTemplate.query(strQuery, new Section()), HttpStatus.OK);
	}

	// Added by sonia on 30th Sept 2024 for Jira idL:ALPD-4940
	public ResponseEntity<Object> getInstrumentBySchedulerDetail(final int instrumentCode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();
		boolean showValidation = false;
		final String strQuery = " select count(1) from schedulersampledetail where ninstrumentcode =" + instrumentCode
				+ " " + " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nactivestatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ntransactionstatus =" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " ";
		final int count = jdbcTemplate.queryForObject(strQuery, Integer.class);
		if (count > 0) {
			showValidation = true;
		}
		objMap.put("InstrumentScheduled", showValidation);
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	// Added by sonia on 30th Sept 2024 for Jira idL:ALPD-4940
	private List<InstrumentCalibration> getActiveInstrumentCalibrationByInstrument(final int ninstrumentCode,
			final UserInfo userInfo) throws Exception {

		final String strQuery = " select * from instrumentcalibration where  ninstrumentcode=" + ninstrumentCode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<InstrumentCalibration> lstInstrumentCalibration = (List<InstrumentCalibration>) jdbcTemplate
				.query(strQuery, new InstrumentCalibration());

		return lstInstrumentCalibration;
	}

	// Added by sonia on 30th Sept 2024 for Jira idL:ALPD-4940
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateAutoCalibrationInstrument(final int instrumentCode, final int autoCalibration,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();
		final Map<String, Object> responseMap = new HashMap<String, Object>();
		String updateInstrumentQuery = "";
		String deleteICQuery = "";
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final List<Object> deletedInstCal = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		final Instrument objInstrumentCheck = (Instrument) getActiveInstrumentById(instrumentCode, objUserInfo);
		if (objInstrumentCheck != null) {
			if (autoCalibration == Enumeration.TransactionStatus.NO.gettransactionstatus()) { // Manual to Automatic

				final Instrument objOldInst = (Instrument) getActiveInstrumentById(instrumentCode, objUserInfo);
				final List<InstrumentCalibration> instCalDetails = getActiveInstrumentCalibrationByInstrument(
						instrumentCode, objUserInfo);

				updateInstrumentQuery = " update instrument set nautocalibration = "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "" + " where ninstrumentcode = "
						+ instrumentCode + " and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(updateInstrumentQuery);

				deleteICQuery = " update instrumentcalibration set nstatus= "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ""
						+ " where ninstrumentcode = " + instrumentCode + " ";
				jdbcTemplate.execute(deleteICQuery);
				final Instrument objNewInst = (Instrument) getActiveInstrumentById(instrumentCode, objUserInfo);

				listBeforeUpdate.add(objOldInst);
				listAfterUpdate.add(objNewInst);
				multilingualIDList.add("IDS_ENABLEDISABLEAUTOCALIBRATION");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						objUserInfo);

				deletedInstCal.add(instCalDetails);
				multilingualIDList.add("IDS_DELETEINSTRUMENTCALIBRATION");
				auditUtilityFunction.fnInsertListAuditAction(deletedInstCal, 1, null, multilingualIDList, objUserInfo);

			} else if (autoCalibration == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				final Instrument objOldInst = (Instrument) getActiveInstrumentById(instrumentCode, objUserInfo);

				updateInstrumentQuery = "update instrument set nautocalibration = "
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "" + " where ninstrumentcode = "
						+ instrumentCode + " and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(updateInstrumentQuery);

				final Instrument objNewInst = (Instrument) getActiveInstrumentById(instrumentCode, objUserInfo);

				listBeforeUpdate.add(objOldInst);
				listAfterUpdate.add(objNewInst);
				multilingualIDList.add("IDS_ENABLEDISABLEAUTOCALIBRATION");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						objUserInfo);

				objMap.putAll(
						(Map<String, Object>) getInstrumentBySchedulerDetail(instrumentCode, objUserInfo).getBody());

				if (objMap.containsKey("InstrumentScheduled")) {
					boolean showFlag = (boolean) objMap.get("InstrumentScheduled");
					if (showFlag) {
						String updateQuery = "update schedulersampledetail set nactivestatus = "
								+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ""
								+ " where ninstrumentcode = " + instrumentCode + " and nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
						jdbcTemplate.execute(updateQuery);
					}
				}
			}
			responseMap.putAll((Map<String, Object>) getInstrument(instrumentCode, objUserInfo).getBody());
			return new ResponseEntity<Object>(responseMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}

	}

	// ALPD-5330 - Gowtham R - Default Section not loaded initially - 08-02-2025
	public ResponseEntity<Object> getSection(final UserInfo userInfo) throws Exception {
		String strQuery = "select s.* from section s, labsection ls, site st where s.nsectioncode=ls.nsectioncode"
				+ " and ls.nsitecode = st.nsitecode and st.nsitecode = (select nsitecode from site where ndefaultstatus= "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmastersitecode ="
				+ userInfo.getNmastersitecode() + ") and ls.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode = "
				+ userInfo.getNmastersitecode() + " and st.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.ndefaultstatus= "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " group by s.nsectioncode";
		return new ResponseEntity<>((List<Section>) jdbcTemplate.query(strQuery, new Section()), HttpStatus.OK);
	}

	// ALPD-5332 Added by Abdul Material Scheduler
	// Recursive function to search for the object by name and return its label
	private static String findLabel(Object json, String targetName) {
		if (json instanceof JSONArray) {
			JSONArray array = (JSONArray) json;
			for (int i = 0; i < array.length(); i++) {
				String result = findLabel(array.get(i), targetName);
				if (result != null) {
					return result;
				}
			}
		} else if (json instanceof JSONObject) {
			JSONObject obj = (JSONObject) json;

			// Check if the object has the target name
			if (obj.has("name") && obj.getString("name").equals(targetName) && obj.has("label")) {
				return obj.getString("label");
			}

			// Recursively search in children arrays
			if (obj.has("children")) {
				return findLabel(obj.get("children"), targetName);
			}
			if (obj.has("child")) {
				return findLabel(obj.get("child"), targetName);
			}
		}
		return null;
	}

}
