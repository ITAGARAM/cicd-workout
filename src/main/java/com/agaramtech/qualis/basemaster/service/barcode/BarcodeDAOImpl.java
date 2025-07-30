package com.agaramtech.qualis.basemaster.service.barcode;

import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.Barcode;
import com.agaramtech.qualis.basemaster.model.Printer;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.SQLQueryCreator;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "materialcategory" table by
 * implementing methods from its interface.
 **/
@AllArgsConstructor
@Repository
public class BarcodeDAOImpl implements BarcodeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BarcodeDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final SQLQueryCreator sqlQueryCreator;

	/**
	 * This method is used to retrieve list of all active barcode for the specified
	 * site.
	 * 
	 * @param userInfo object for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         barcode * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBarcode(UserInfo userInfo) throws Exception {
		final String strQuery = "select b.*,sq.ssqlqueryname,cm.scontrolname,coalesce(cm.jsondata->'scontrolids'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " cm.jsondata->'scontrolids'->>'en-US') as  scontrolids from barcode b,sqlquery sq,controlmaster cm where b.nbarcode > 0 "
				+ " and b.nquerycode=sq.nsqlquerycode and cm.ncontrolcode=b.ncontrolcode and sq.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +""
				+ " and b.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and cm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nsitecode="+ userInfo.getNmastersitecode()+"";
		LOGGER.info("getBarcode -->");
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new Barcode()), HttpStatus.OK);
	}

	/**
	 * This method is used to delete entry in barcode table. Need to validate that
	 * the specified barcode object is active and is not associated with any of its
	 * child tables before updating its nstatus to -1.
	 * 
	 * @param barcode [Barcode] object holding detail to be deleted in barcode table
	 * @return response entity object holding response status and data of deleted
	 *         barcode object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteBarcode(Barcode barcode, UserInfo userInfo) throws Exception {
		final Barcode barcodeByID = (Barcode) getActiveBarcodeById(barcode.getNbarcode(),
				userInfo.getNmastersitecode());
		if (barcodeByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_MATERIALCATEGORY' as Msg from materialcategory where nbarcode= "
					+ barcode.getNbarcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(barcode.getNbarcode()),
						userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedMaterialCategoryList = new ArrayList<>();
				final String updateQueryString = "update barcode set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nbarcode="
						+ barcode.getNbarcode();
				jdbcTemplate.execute(updateQueryString);
				barcode.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedMaterialCategoryList.add(barcode);
				multilingualIDList.add("IDS_DELETEBARCODE");
				auditUtilityFunction.fnInsertAuditAction(savedMaterialCategoryList, 1, null, multilingualIDList,
						userInfo);
				return getBarcode(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to retrieve active barcode object based on the specified
	 * nbarcode.
	 * 
	 * @param nbarcode [int] primary key of barcode
	 * @return response entity object holding response status and data of barcode
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Barcode getActiveBarcodeById(final int nbarcode, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select b.* ,sq.ssqlqueryname,sq.nsqlquerycode,cm.scontrolname from barcode b ,sqlquery sq,controlmaster cm where b.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and b.nquerycode=sq.nsqlquerycode and cm.ncontrolcode=b.ncontrolcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and sq.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nbarcode = " + nbarcode
				+ " and b.nsitecode=" + nmasterSiteCode + "";
		return (Barcode) jdbcUtilityFunction.queryForObject(strQuery, Barcode.class, jdbcTemplate);
	}

	/**
	 * This interface declaration is used to add a new entry to barcode table.
	 * 
	 * @param request     object holding details to be added in barcode table
	 * @param objUserInfo [object] nmastersitecode primary key of site object from
	 *                    objUserInfo is to be fetched
	 * @return response entity object holding response status and data of added
	 *         barcode object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> createBarcode(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		final String sQuery = " lock  table barcode " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final ObjectMapper objMapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedBarcodeList = new ArrayList<>();
		final Barcode lstBarcode = objMapper.readValue(request.getParameter("barcodeFile"), Barcode.class);
		if (lstBarcode != null) {
			final Barcode objBarcode = getBarcodeByName(lstBarcode.getSbarcodename(), lstBarcode.getNsitecode());
			if (objBarcode == null) {
				if (lstBarcode.getSfilename().isEmpty()) {
					int nSeqNo = jdbcTemplate.queryForObject(
							"select nsequenceno from SeqNoBasemaster where stablename='barcode'", Integer.class);
					nSeqNo++;
					String barcodeInsert = "insert into barcode(nbarcode, nquerycode, ncontrolcode, sbarcodename, sdescription, sfilename, ssystemfilename, dmodifieddate, nsitecode, nstatus)"
							+ " values(" + nSeqNo + "," + lstBarcode.getNquerycode() + ","
							+ lstBarcode.getNcontrolcode() + ",N'" + " a.nsitecode = "
							+ objUserInfo.getNmastersitecode()
							+ stringUtilityFunction.replaceQuote(lstBarcode.getSbarcodename()) + "',N'"
							+ stringUtilityFunction.replaceQuote(lstBarcode.getSdescription()) + "',N'"
							+ stringUtilityFunction.replaceQuote(lstBarcode.getSfilename()) + "',N'"
							+ stringUtilityFunction.replaceQuote(lstBarcode.getSsystemfilename()) + "','"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ objUserInfo.getNmastersitecode() + ",1)";
					jdbcTemplate.execute(barcodeInsert);
					jdbcTemplate.execute(
							"update SeqNoBasemaster set nsequenceno = " + nSeqNo + " where stablename='barcode'");
					savedBarcodeList.add(lstBarcode);
					multilingualIDList.add("IDS_ADDBARCODE");
					auditUtilityFunction.fnInsertAuditAction(savedBarcodeList, 1, null, multilingualIDList,
							objUserInfo);
				} else {
					String sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
						int nSeqNo = jdbcTemplate.queryForObject(
								"select nsequenceno from SeqNoBasemaster where stablename='barcode'", Integer.class);
						nSeqNo++;
						String barcodeInsert = "insert into barcode(nbarcode, nquerycode, ncontrolcode, sbarcodename, sdescription, sfilename, ssystemfilename, dmodifieddate, nsitecode, nstatus)"
								+ " values(" + nSeqNo + "," + lstBarcode.getNquerycode() + ","
								+ lstBarcode.getNcontrolcode() + ",N'"
								+ stringUtilityFunction.replaceQuote(lstBarcode.getSbarcodename()) + "',N'"
								+ stringUtilityFunction.replaceQuote(lstBarcode.getSdescription()) + "',N'"
								+ stringUtilityFunction.replaceQuote(lstBarcode.getSfilename()) + "',N'"
								+ stringUtilityFunction.replaceQuote(lstBarcode.getSsystemfilename()) + "','"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
								+ objUserInfo.getNmastersitecode() + ",1)";
						jdbcTemplate.execute(barcodeInsert);
						jdbcTemplate.execute(
								"update SeqNoBasemaster set nsequenceno = " + nSeqNo + " where stablename='barcode'");
						savedBarcodeList.add(lstBarcode);
						multilingualIDList.add("IDS_ADDBARCODE");
						auditUtilityFunction.fnInsertAuditAction(savedBarcodeList, 1, null, multilingualIDList,
								objUserInfo);
					} else {

						return new ResponseEntity<>(commonFunction.getMultilingualMessage(sReturnString,
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				}

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
		return getBarcode(objUserInfo);
	}

	private String getFileFTPUpload(MultipartHttpServletRequest request, int i, UserInfo objUserInfo) {

		return null;
	}

	/**
	 * This interface declaration is used to add a new entry to barcode table.
	 * 
	 * @param sbarcodename    has been passed to fetch the data of respective
	 *                        barcodename
	 * @param nmasterSiteCode has been passed to fetch the data of respective
	 *                        nmasterSiteCode
	 * @return response entity object holding response status and data of added
	 *         barcode object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public Barcode getBarcodeByName(final String sbarcodename, final int nmasterSiteCode) throws Exception {
		String strQuery = "select nbarcode from barcode where sbarcodename = N'"
				+ stringUtilityFunction.replaceQuote(sbarcodename) + "'" + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and  nsitecode ="
				+ nmasterSiteCode;
		Barcode objBarcode = (Barcode) jdbcUtilityFunction.queryForObject(strQuery, Barcode.class, jdbcTemplate);
		return objBarcode;
	}

	/**
	 * This interface declaration is used to retrieve list of all active sqlquery
	 * and controlname for the specified site through its DAO layer
	 * 
	 * @param userInfo [object] nmastersitecode primary key of site object from
	 *                 objUserInfo is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         sqlquery
	 * @throws Exception that are thrown in the DAO layer
	 * 
	 */

	@Override
	public ResponseEntity<Object> getSqlQuery(UserInfo userInfo) throws Exception {
		String strQuery = " select sq.nquerytypecode,sq.ssqlquery,sq.sscreenrecordquery,sq.sscreenheader,sq.ncharttypecode,sq.svaluemember,"
				+ "sq.sdisplaymember,sq.nsitecode,sq.nstatus,sq.ssqlqueryname,sq.nsqlquerycode   from sqlquery sq,querytype qt where "
				+ " sq.nsqlquerycode >0 and sq.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " " + " and qt.nquerytypecode=sq.nquerytypecode and qt.nquerytypecode= "
				+ Enumeration.QueryType.BARCODE.getQuerytype() + "" + " and qt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " sq.nsitecode ="
				+ userInfo.getNmastersitecode() + ";";

		strQuery = strQuery + " select coalesce(cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " cm.jsondata->'scontrolids'->>'en-US') as  scontrolids,cm.ncontrolcode from sitecontrolmaster sc,controlmaster cm "
				+ " where sc.nisbarcodecontrol=" + Enumeration.TransactionStatus.YES.gettransactionstatus() + ""
				+ " and cm.ncontrolcode=sc.ncontrolcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and sc.nsitecode ="
				+ userInfo.getNmastersitecode();

		// Added by ATE234 -> jana need to discuss
		final String qry = sqlQueryCreator.multiPlainQueryFormation(strQuery, SQLQuery.class, ControlMaster.class);
		List<?> multiList = null;
		if (!qry.isBlank() && !qry.isEmpty() && qry != null) {
			multiList = projectDAOSupport.getMultipleEntitiesResultSetInList(strQuery, jdbcTemplate,
					SQLQuery.class, ControlMaster.class);
		}
		final Map<String, Object> mapList = new HashMap<String, Object>();
		if (multiList != null && multiList.size() > 0) {
			mapList.put("SQLQuey", multiList.get(0));
			mapList.put("ControlType", multiList.get(1));
		} else {
			mapList.put("SQLQuey", new ArrayList<>());
			mapList.put("ControlType", new ArrayList<>());
		}
		return new ResponseEntity<Object>(mapList, HttpStatus.OK);
	}

	/**
	 * This method is used to update entry in barcode table. Need to validate that
	 * the barcode name to be updated is active before updating details in database.
	 * Need to check for duplicate entry of sbarcodename for the specified site
	 * before saving into database.
	 * 
	 * @param request object holding details to be updated in barcode table
	 * @return response entity object holding response status and data of updated
	 *         barcode object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateBarcode(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final Barcode lstBarcode = objMapper.readValue(request.getParameter("barcodeFile"), Barcode.class);
		final Barcode objBarcode = (Barcode) getActiveBarcodeById(lstBarcode.getNbarcode(),
				userInfo.getNmastersitecode());
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		if (objBarcode == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final String strQuery = "select b.*  from barcode b where b.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nbarcode = "
				+ lstBarcode.getNbarcode() + "";

		final String queryString = "select nbarcode from barcode where sbarcodename = N'"
				+ stringUtilityFunction.replaceQuote(lstBarcode.getSbarcodename()) + "'" + " and nbarcode <> "
				+ lstBarcode.getNbarcode() + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + "";

		final Barcode existingBarcode = (Barcode) jdbcUtilityFunction.queryForObject(queryString, Barcode.class,
				jdbcTemplate);
		if (existingBarcode == null) {
			if (lstBarcode.getSfilename().isEmpty()) {
				final String updateQueryString = "update barcode set sbarcodename=N'"
						+ stringUtilityFunction.replaceQuote(lstBarcode.getSbarcodename()) + "'" + ",nquerycode = "
						+ lstBarcode.getNquerycode() + ",ncontrolcode = " + lstBarcode.getNcontrolcode()
						+ ",sdescription =N'" + stringUtilityFunction.replaceQuote(lstBarcode.getSdescription())
						+ "',sfilename = N'" + stringUtilityFunction.replaceQuote(objBarcode.getSfilename()) + "'"
						+ ",ssystemfilename = N'" + stringUtilityFunction.replaceQuote(objBarcode.getSsystemfilename())
						+ "',nsitecode = " + lstBarcode.getNsitecode() + ", dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nbarcode="
						+ lstBarcode.getNbarcode();
				jdbcTemplate.execute(updateQueryString);
				final Barcode objBarCode = (Barcode) jdbcUtilityFunction.queryForObject(strQuery, Barcode.class,
						jdbcTemplate);
				listAfterUpdate.add(objBarCode);
				listBeforeUpdate.add(objBarcode);
				multilingualIDList.add("IDS_EDITBARCODE");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
			} else {
				String sReturnString = getFileFTPUpload(request, -1, userInfo);
				if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
					final String updateQueryString = "update barcode set sbarcodename=N'"
							+ stringUtilityFunction.replaceQuote(lstBarcode.getSbarcodename()) + "'" + ",nquerycode = "
							+ lstBarcode.getNquerycode() + ",ncontrolcode = " + lstBarcode.getNcontrolcode()
							+ ",sdescription =N'" + stringUtilityFunction.replaceQuote(lstBarcode.getSdescription())
							+ "',sfilename = N'" + stringUtilityFunction.replaceQuote(lstBarcode.getSfilename()) + "'"
							+ ",ssystemfilename = N'"
							+ stringUtilityFunction.replaceQuote(lstBarcode.getSsystemfilename()) + "',nsitecode = "
							+ lstBarcode.getNsitecode() + " where nbarcode=" + lstBarcode.getNbarcode();
					jdbcTemplate.execute(updateQueryString);
					listAfterUpdate.add(lstBarcode);
					listBeforeUpdate.add(objBarcode);
					multilingualIDList.add("IDS_EDITBARCODE");
					auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
							userInfo);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		return getBarcode(userInfo);
	}

	@Override
	public ResponseEntity<Object> viewAttachedDownloadFile(Barcode objTestFile, UserInfo objUserInfo) throws Exception {
		final Barcode barcodeByID = (Barcode) getActiveBarcodeById(objTestFile.getNbarcode(),
				objUserInfo.getNmastersitecode());// .getBody();
		if (barcodeByID != null) {
			if (barcodeByID.getSfilename().isEmpty()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.FAILED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				Map<String, Object> map = new HashMap<>();
				map = FileViewUsingFtp(objTestFile.getSsystemfilename(), -1, objUserInfo, "", "");
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList.add("IDS_VIEWBARCODEFILE");
				lstObject.add(objTestFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				return new ResponseEntity<Object>(map, HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	private Map<String, Object> FileViewUsingFtp(String ssystemfilename, int i, UserInfo objUserInfo, String string,
			String string2) {
		return null;
	}

	@Override
	public ResponseEntity<Object> getPrinter() {
		List<Printer> lstprinter = new ArrayList<>();
		Printer p1 = new Printer();
		PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
		PrintService[] services = PrinterJob.lookupPrintServices();
		String serviceName = "";
		if (defaultService != null) {
			serviceName = defaultService.getName();
			p1.setSprintername(serviceName);
			lstprinter.add(p1);
		}
		// NIBSCRT-2110
		for (PrintService printer : services) {
			if (serviceName != null && !serviceName.equals(printer.getName())) {
				Printer p = new Printer();
				p.setSprintername(printer.getName());
				lstprinter.add(p);
			}
		}
		return new ResponseEntity<>(lstprinter, HttpStatus.OK);
	}

	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> PrintBarcode(Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		String sprintername = (String) inputMap.get("sprintername");
		String squery = "";
		int nbarcode = 0;
		List<String> sBarCodeQueries = null;
		FileReader fr = null;
		BufferedWriter bufferedWriter = null;
		BufferedReader br = null;
		String str = "select * from barcode where ncontrolcode =" + inputMap.get("ncontrolcode") + " and sbarcodename='"
				+ inputMap.get("sbarcodename") + "' and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ objUserInfo.getNmastersitecode() + "";
		List<Barcode> lst = (List<Barcode>) jdbcTemplate.query(str, new Barcode());

		List<String> lsttransactionsamplecode = Arrays
				.asList(inputMap.get("ntransactionsamplecode").toString().split(","));
		List<String> lsttransactionpreregcode = Arrays.asList(inputMap.get("npreregno").toString().split(","));
		int allSubSampleCount = lsttransactionsamplecode.size();
		int allSampleCount = lsttransactionpreregcode.size();

		final String sFindSubSampleQuery = "select count(nsamplehistorycode) from registrationsamplehistory"
				+ " where nsamplehistorycode = any (select max(nsamplehistorycode) from registrationsamplehistory where ntransactionsamplecode"
				+ " in (" + inputMap.get("ntransactionsamplecode").toString() + ")  and nsitecode="
				+ objUserInfo.getNtranssitecode() + " group by ntransactionsamplecode)"
				+ " and ntransactionstatus not in (" + Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
				+ ", " + Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")" + "  and nsitecode="
				+ objUserInfo.getNtranssitecode() + ";";

		int availableSampleCount = jdbcTemplate.queryForObject(sFindSubSampleQuery, Integer.class);

		if (allSubSampleCount == availableSampleCount) {
			final String sFindSampleQuery = "select count(nreghistorycode) from registrationhistory"
					+ " where nreghistorycode = any (select max(nreghistorycode) from registrationhistory where npreregno"
					+ " in (" + inputMap.get("npreregno").toString() + ")  and nsitecode="
					+ objUserInfo.getNtranssitecode() + " group by npreregno)" + " and ntransactionstatus not in ("
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")" + "  and nsitecode="
					+ objUserInfo.getNtranssitecode() + ";";

			int availablePreregCount = jdbcTemplate.queryForObject(sFindSampleQuery, Integer.class);

			if (allSampleCount == availablePreregCount) {
				if (!lst.isEmpty()) {

					nbarcode = lst.get(0).getNbarcode();
					FileInputStream psStream = null;
					final String getBarcodePath = " select ssettingvalue from settings" + " where nsettingcode = 9 "
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					String FilePath = jdbcTemplate.queryForObject(getBarcodePath, String.class);

					sBarCodeQueries = new ArrayList<>();
					squery = jdbcTemplate.queryForObject("Select ssqlquery from SqlQuery where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsqlquerycode = "
							+ lst.get(0).getNquerycode() + "", String.class);
					String tempQry = "";
					String[] query = squery.split(" ");
					tempQry = tempQry + squery;
					if (squery.contains("<@")) {
						int aa = squery.indexOf("<@");
						int bb = (squery.indexOf("@>") + 2);
						int b = (squery.indexOf("@>"));
						tempQry = tempQry.replace(squery.substring(aa, (bb)),
								inputMap.get(squery.substring(aa + 2, b)) + "");
					}
					String PrnFile = FilePath + lst.get(0).getSsystemfilename();
					File fileSharedFolder = new File(PrnFile);

					if (!fileSharedFolder.exists()) {
						LOGGER.info("PRN File Not Found in Path->" + PrnFile);
						LOGGER.info("Downloading from FTP");
						final UserInfo barcodeUserInfo = new UserInfo(objUserInfo);
						barcodeUserInfo.setNformcode((short) Enumeration.FormCode.BARCODE.getFormCode());
						Map<String, Object> objmap = FileViewUsingFtp(lst.get(0).getSsystemfilename(), -1,
								barcodeUserInfo, FilePath, "");
						if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == objmap.get("rtn")) {

							LOGGER.info("File Downloaded from FTP");
							fileSharedFolder = new File(PrnFile);
						} else {
							LOGGER.info("Error in downloading file from FTP");
							return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PRNFILEDOESNOTEXIST",
									objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					}
					LOGGER.info("PRN File Found in Path->" + PrnFile);
					final String homePath = getFileAbsolutePath();
					String FileNamePath1 = System.getenv(homePath) + Enumeration.FTP.UPLOAD_PATH.getFTP()
							+ UUID.randomUUID().toString().trim() + ".prn";

					Path path = Paths.get(FileNamePath1);
					LOGGER.info("New PRN File Created in Path->" + FileNamePath1);
					bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
					String line;
					StringBuffer stringBuffer = new StringBuffer();
					List<Map<String, Object>> lstquery = jdbcTemplate.queryForList(tempQry);
					if (!lstquery.isEmpty()) {
						for (int i = 0; i < lstquery.size(); i++) {
							fr = new FileReader(PrnFile);
							br = new BufferedReader(fr);
							Map<String, Object> map = lstquery.get(i);
							while ((line = br.readLine()) != null) {
								if (line.contains("$")) {
									int start = line.indexOf("$");
									int end = line.lastIndexOf("$");
									String valueSubstitutedToFilter1 = line.substring(start + 1, end);
									String keyToReplace = "\\$" + valueSubstitutedToFilter1 + "\\$";
									line = line.replaceAll(keyToReplace, map.get(valueSubstitutedToFilter1).toString());
									LOGGER.info("Value Replaced " + keyToReplace + "->"
											+ map.get(valueSubstitutedToFilter1).toString());
									bufferedWriter.write(line);
									((BufferedWriter) bufferedWriter).newLine();
									bufferedWriter.flush();
								} else {
									bufferedWriter.write(line);
									((BufferedWriter) bufferedWriter).newLine();
									bufferedWriter.flush();
								}
							}
							if (br.readLine() == null) {
								br.close();
							}
							psStream = new FileInputStream(new File(FileNamePath1));
							String Printerpath = "";
							Printer objbarcode = null;
							DocFlavor psInFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
							DocFlavor psInFormat1 = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
							Doc myDoc = new SimpleDoc(psStream, psInFormat, null);
							PrintServiceAttributeSet aset = new HashPrintServiceAttributeSet();
							aset.add(new PrinterName(Printerpath, null));//
							PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
							for (PrintService printer : services) {
								if (printer.getName().equalsIgnoreCase(sprintername)) {
									DocPrintJob job = printer.createPrintJob();
									System.out.println("print");
									job.print(myDoc, null);
									String word = "";
									LOGGER.info("Printer Name =>" + printer.getName());
									LOGGER.info("Barcode Printed Successfully");
								}
							}
							Map<String, Object> outputMap = new HashMap<String, Object>();
							outputMap.put("sprimarykeyvalue", -1);
							insertAuditAction(objUserInfo, "IDS_PRINTBARCODE", commonFunction.getMultilingualMessage(
									"IDS_PRINTBARCODE", objUserInfo.getSlanguagefilename()), outputMap);
						}
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUCCESS",
								objUserInfo.getSlanguagefilename()), HttpStatus.OK);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								"IDS_SELECTVALIDRECORDTOPRINT", objUserInfo.getSlanguagefilename()), HttpStatus.OK);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOBARCODECONFIGURATION",
							objUserInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREGISTEREDSAMPLEONLY",
						objUserInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREGISTEREDSUBSAMPLEONLY",
					objUserInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);

		}
	}

	private String getFileAbsolutePath() {

		return null;
	}

	private void insertAuditAction(UserInfo objUserInfo, String string, String multilingualMessage,
			Map<String, Object> outputMap) {

	}

	@Override
	public ResponseEntity<Object> getControlBasedBarcode(int ncontrolcode) throws Exception {
		final String strQuery = "select nbarcode,nquerycode,ncontrolcode,sbarcodename,sfilename,ssystemfilename "
				+ " from barcode  where  nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ncontrolcode=" + ncontrolcode;
		final List<Barcode> lstControlBasedBarcode = (List<Barcode>) jdbcTemplate.query(strQuery, new Barcode());
		return new ResponseEntity<>(lstControlBasedBarcode, HttpStatus.OK);
	}

}
