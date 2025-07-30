package com.agaramtech.qualis.testmanagement.service.method;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.agaramtech.qualis.testmanagement.model.Method;
import com.agaramtech.qualis.testmanagement.model.MethodValidity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "method" table by
 * implementing methods from its interface.
 * 
 * @author ATE180
 * @version 9.0.0.1
 * @since 1- July- 2020
 */

@AllArgsConstructor
@Repository
public class MethodDAOImpl implements MethodDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active method for the specified
	 * site.
	 * 
	 * @param userInfo [UserInfo] object holding details of logged in user.
	 * @return response entity object holding response status and list of all active
	 *         methods
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getMethod(Integer nmethodcode, final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();

		Method selectedMethod = new Method();
		String strQuery = "";
		if (nmethodcode == null) {

			strQuery = "select m.nmethodcode, m.nmethodcatcode, m.smethodname, m.sdescription, m.ndefaultstatus, m.nneedvalidity, m.dmodifieddate, m.nsitecode, m.nstatus, mc.smethodcatname,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from method m, methodcategory mc,"
					+ " transactionstatus ts where m.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and mc.nmethodcatcode = m.nmethodcatcode "
					+ " and ts.ntranscode=m.ndefaultstatus and nmethodcode > 0  and m.nsitecode = "
					+ userInfo.getNmastersitecode() + " and mc.nsitecode = " + userInfo.getNmastersitecode()
					+ " order by nmethodcode asc";
			LOGGER.info(strQuery);
			List<Method> methodList = jdbcTemplate.query(strQuery, new Method());

			if (methodList.isEmpty()) {

				outputMap.put("Method", methodList);
				outputMap.put("SelectedMethod", null);
				outputMap.put("MethodValidity", null);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {

				outputMap.put("Method", methodList);
				selectedMethod = methodList.get(methodList.size() - 1);
				nmethodcode = selectedMethod.getNmethodcode();

				strQuery = "select mv.nmethodvaliditycode,mv.nmethodcode,m.smethodname,mv.ntransactionstatus,mv.dvaliditystartdate,mv.dvalidityenddate, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "',"
						+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,COALESCE(TO_CHAR(mv.dvaliditystartdate,'"
						+ userInfo.getSpgsitedatetime() + "'),'') as svaliditystartdate ,"
						+ " COALESCE(TO_CHAR(mv.dvalidityenddate,'" + userInfo.getSpgsitedatetime()
						+ "'),'') as svalidityenddate from method m, methodvalidity mv,"
						+ " transactionstatus ts where mv.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and mv.nmethodcode = m.nmethodcode "
						+ " and ts.ntranscode=mv.ntransactionstatus and mv.nmethodvaliditycode > 0 and mv.nmethodcode = "
						+ nmethodcode + "  and mv.nsitecode = " + userInfo.getNmastersitecode()
						+ " order by nmethodvaliditycode asc";

				objMapper.registerModule(new JavaTimeModule());
				final List<MethodValidity> lstMthdValid = objMapper.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new MethodValidity()),
								Arrays.asList("svaliditystartdate", "svalidityenddate"), null, userInfo, false, null,
								false),
						new TypeReference<List<MethodValidity>>() {
						});
//			Map<String, Object> objMap = new LinkedHashMap<>();
//			objMap.put("MethodValidity", lstMthdValid);
				outputMap.put("MethodValidity", lstMthdValid);

			}

		} else {

			selectedMethod = getActiveMethodById(nmethodcode, userInfo);

			strQuery = "select m.nmethodcode, m.nmethodcatcode, m.smethodname, m.sdescription, m.ndefaultstatus, m.nneedvalidity, m.dmodifieddate, m.nsitecode, m.nstatus, mc.smethodcatname,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from method m, methodcategory mc,"
					+ " transactionstatus ts where m.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and mc.nmethodcatcode = m.nmethodcatcode "
					+ " and ts.ntranscode=m.ndefaultstatus and nmethodcode > 0  and m.nsitecode = "
					+ userInfo.getNmastersitecode() + " and mc.nsitecode = " + userInfo.getNmastersitecode()
					+ " order by nmethodcode asc";

			List<Method> methodLst = jdbcTemplate.query(strQuery, new Method());

			outputMap.put("Method", methodLst);

			strQuery = "select mv.nmethodvaliditycode,mv.nmethodcode,m.smethodname,mv.ntransactionstatus,mv.dvaliditystartdate,mv.dvalidityenddate, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,COALESCE(TO_CHAR(mv.dvaliditystartdate,'"
					+ userInfo.getSpgsitedatetime() + "'),'') as svaliditystartdate ,"
					+ " COALESCE(TO_CHAR(mv.dvalidityenddate,'" + userInfo.getSpgsitedatetime()
					+ "'),'') as svalidityenddate from method m, methodvalidity mv,"
					+ " transactionstatus ts where mv.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and mv.nmethodcode = m.nmethodcode "
					+ " and ts.ntranscode=mv.ntransactionstatus and mv.nmethodvaliditycode > 0 "
					+ " and mv.nmethodcode = " + nmethodcode + "  and mv.nsitecode = " + userInfo.getNmastersitecode()
					+ " and m.nsitecode = " + userInfo.getNmastersitecode() + "  order by nmethodvaliditycode asc";

			objMapper.registerModule(new JavaTimeModule());
			final List<MethodValidity> lstMthdValid = objMapper.convertValue(
					dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new MethodValidity()),
							Arrays.asList("svaliditystartdate", "svalidityenddate"), null, userInfo, false, null,
							false),
					new TypeReference<List<MethodValidity>>() {
					});

			outputMap.put("MethodValidity", lstMthdValid);

		}
		if (selectedMethod == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedMethod", selectedMethod);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}


	public ResponseEntity<Object> getMethodValidity(final Integer nmethodcode, final UserInfo userInfo) throws Exception {
		final 	ObjectMapper objMapper = new ObjectMapper();

		
		final String strQuery = "select mv.nmethodvaliditycode,mv.nmethodcode,m.smethodname,mv.ntransactionstatus,mv.dvaliditystartdate,mv.dvalidityenddate, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,COALESCE(TO_CHAR(mv.dvaliditystartdate,'"
				+ userInfo.getSpgsitedatetime() + "'),'') as svaliditystartdate ,"
				+ " COALESCE(TO_CHAR(mv.dvalidityenddate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as svalidityenddate from method m, methodvalidity mv,"
				+ " transactionstatus ts where mv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mv.nmethodcode = m.nmethodcode "
				+ " and ts.ntranscode=mv.ntransactionstatus and mv.nmethodvaliditycode > 0 and mv.nmethodcode = "
				+ nmethodcode + "  and mv.nsitecode = " + userInfo.getNmastersitecode()
				+ " and m.nsitecode = " + userInfo.getNmastersitecode()+"  order by nmethodvaliditycode asc";

		objMapper.registerModule(new JavaTimeModule());
		
		final List<MethodValidity> lstMthdValid = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new MethodValidity()),
						Arrays.asList("svaliditystartdate", "svalidityenddate"), null, userInfo, false, null, false),
				new TypeReference<List<MethodValidity>>() {
				});
		Map<String, Object> objMap = new LinkedHashMap<>();
		
		objMap.put("MethodValidity", lstMthdValid);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to method table. Need to check for
	 * duplicate entry of method name for the specified site before saving into
	 * database.
	 * 
	 * @param method [Method] object holding details to be added in method table
	 * @return response entity object holding response status and data of added
	 *         method object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createMethod(Method method, MethodValidity methodvalidity, UserInfo userInfo)
			throws Exception {


		final String sQueryLock = " lock  table lockmethod " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		
		jdbcTemplate.execute(sQueryLock);

		final Method methodByName = getMethodByName(method.getSmethodname(), method.getNmethodcatcode(),
				method.getNsitecode());
		MethodValidity convertedObject = new MethodValidity();
		List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();
		List<String> lstTZcolumn = new ArrayList<String>();
		final ObjectMapper objMapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<String> multilingualIDMehtodList = new ArrayList<>();
		final List<Object> savedMethodList = new ArrayList<>();
		final List<String> multilingualIDValList = new ArrayList<>();
		final List<Object> savedMethodValList = new ArrayList<>();
		
		if (methodByName==null) {

			if (methodvalidity.getSvaliditystartdate() != null) {

				objMapper.registerModule(new JavaTimeModule());

				if (methodvalidity.getDvaliditystartdate() != null) {
					
					methodvalidity.setSvaliditystartdate(methodvalidity.getSvaliditystartdate().replace("T", " "));
					lstDateField.add("svaliditystartdate");
					lstDatecolumn.add(methodvalidity.getStzvaliditystartdatetimezone());
					lstTZcolumn.add("ntzvaliditystartdatetimezone");
					
					if (userInfo.getNtimezonecode() > 0) {
						methodvalidity
								.setNoffsetdvaliditystartdate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(
										methodvalidity.getSvaliditystartdate().replace("T", " "),
										userInfo.getStimezoneid()));
					}
				}

				if (methodvalidity.getDvalidityenddate() != null) {
				
					methodvalidity.setSvalidityenddate(methodvalidity.getSvalidityenddate().replace("T", " "));
					lstDateField.add("svalidityenddate");
					lstDatecolumn.add(methodvalidity.getStzvalidityenddatetimezone());
					lstTZcolumn.add("ntzvalidityenddatetimezone");
					
					if (userInfo.getNtimezonecode() > 0) {
						methodvalidity.setNoffsetdvalidityenddate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(
								methodvalidity.getSvalidityenddate().replace("T", " "), userInfo.getStimezoneid()));
					}
				}
				
				
				

		       
		       
				convertedObject = objMapper.convertValue(
						dateUtilityFunction.convertInputDateToUTCByZone(methodvalidity,lstDateField, lstTZcolumn, true, userInfo),
						new TypeReference<MethodValidity>() {
						});

				if (convertedObject.getDvaliditystartdate().isAfter(convertedObject.getDvalidityenddate())) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_STARTENDDATEVALIDATE",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}

			if (method.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final Method defaultMethod = getMethodByDefaultStatus(userInfo.getNmastersitecode(),
						method.getNmethodcatcode());

				if (defaultMethod != null) {

					

					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(defaultMethod);

					defaultMethod.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update method set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmethodcode ="
							+ defaultMethod.getNmethodcode();
					
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultMethod);

					multilingualIDList.add("IDS_EDITMETHOD");

					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					

				}

			}
			final String sequencequery = "select nsequenceno from seqnotestmanagement where stablename ='method' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;
			
			final String insertquery = "insert  into method (nmethodcode,nmethodcatcode,smethodname,sdescription,dmodifieddate,ndefaultstatus,nsitecode,nstatus,nneedvalidity) "
					+ "values(" + nsequenceno + "," + method.getNmethodcatcode() + ",N'"
					+ stringUtilityFunction.replaceQuote(method.getSmethodname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(method.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + method.getNdefaultstatus() + ","
					+ userInfo.getNmastersitecode() + "," + " "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "," + method.getNneedvalidity()
					+ ")";

			jdbcTemplate.execute(insertquery);

			final String updatequery = "update seqnotestmanagement set nsequenceno=" + nsequenceno
					+ " where stablename ='method' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			
			jdbcTemplate.execute(updatequery);

			method.setNmethodcode(nsequenceno);

			savedMethodList.add(method);
			multilingualIDMehtodList.add("IDS_ADDMETHOD");
			auditUtilityFunction.fnInsertAuditAction(savedMethodList, 1, null, multilingualIDMehtodList, userInfo);
			if (methodvalidity.getSvaliditystartdate() != null) {

				final String schSeq = "select nsequenceno from seqnotestmanagement where stablename='methodvalidity' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				int seqNo = jdbcTemplate.queryForObject(schSeq, Integer.class);
				seqNo = seqNo + 1;

				String methodInsert="";
				   methodInsert = "insert into methodvalidity(nmethodvaliditycode,nmethodcode,dvaliditystartdate,dvalidityenddate,"
						+ " noffsetdvaliditystartdate,ntzvaliditystartdatetimezone,noffsetdvalidityenddate,ntzvalidityenddatetimezone,dmodifieddate,ntransactionstatus,nsitecode,nstatus) "
						+ " values(" + seqNo + ",'" + nsequenceno + "','" + convertedObject.getSvaliditystartdate()
						+ "','" + convertedObject.getSvalidityenddate() + "',"
						+ methodvalidity.getNoffsetdvaliditystartdate() + ","
						+ methodvalidity.getNtzvaliditystartdatetimezone() + "" + ","
						+ methodvalidity.getNoffsetdvalidityenddate() + ","
						+ methodvalidity.getNtzvalidityenddatetimezone() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
						+ methodvalidity.getNsitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

				jdbcTemplate.execute(methodInsert);

				methodInsert = "update seqnotestmanagement set nsequenceno=" + seqNo
						+ " where stablename='methodvalidity' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				jdbcTemplate.execute(methodInsert);

				methodvalidity.setNmethodvaliditycode(seqNo);

				final String strnew = "select COALESCE(TO_CHAR(mv.dvaliditystartdate,'" + userInfo.getSsitedate()
						+ "'),'') as svaliditystartdate, " + "COALESCE(TO_CHAR(mv.dvalidityenddate,'"
						+ userInfo.getSsitedate()
						+ "'),'') as svalidityenddate from methodvalidity mv where mv.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
						+ " mv.nmethodvaliditycode=" + seqNo + "";

				List<MethodValidity> MethodValiditynewList = jdbcTemplate.query(strnew, new MethodValidity());
				
				methodvalidity.setSvaliditystartdate(MethodValiditynewList.get(0).getSvaliditystartdate());
				methodvalidity.setSvalidityenddate(MethodValiditynewList.get(0).getSvalidityenddate());


				savedMethodValList.add(methodvalidity);
				multilingualIDValList.add("IDS_ADDMETHODVALIDITY");
				auditUtilityFunction.fnInsertAuditAction(savedMethodValList, 1, null, multilingualIDValList, userInfo);
			}

			
			return getMethod(null, userInfo);
			
		} else {
			
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> createMethodValidity(MethodValidity methodvalidity, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final String sQueryLock = " lock  table lockmethod " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final List<Object> savedMethodValidList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();


		List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();
		List<String> lstTZcolumn = new ArrayList<String>();
		objMapper.registerModule(new JavaTimeModule());

		Method selectedMethod = new Method();
		selectedMethod = getActiveMethodById(methodvalidity.getNmethodcode(), userInfo);

		if (selectedMethod == null) {

			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);

		} else {

			if (methodvalidity.getDvaliditystartdate() != null) {
				
				methodvalidity.setSvaliditystartdate(methodvalidity.getSvaliditystartdate().replace("T", " "));
				lstDateField.add("svaliditystartdate");
				lstDatecolumn.add(methodvalidity.getStzvaliditystartdatetimezone());
				lstTZcolumn.add("ntzvaliditystartdatetimezone");
				if (userInfo.getNtimezonecode() > 0) {
					methodvalidity.setNoffsetdvaliditystartdate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(
							methodvalidity.getSvaliditystartdate().replace("T", " "), userInfo.getStimezoneid()));
				}
			}

			if (methodvalidity.getDvalidityenddate() != null) {
				
				methodvalidity.setSvalidityenddate(methodvalidity.getSvalidityenddate().replace("T", " "));
				lstDateField.add("svalidityenddate");
				lstDatecolumn.add(methodvalidity.getStzvalidityenddatetimezone());
				lstTZcolumn.add("ntzvalidityenddatetimezone");
				if (userInfo.getNtimezonecode() > 0) {
					methodvalidity.setNoffsetdvalidityenddate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(
							methodvalidity.getSvalidityenddate().replace("T", " "), userInfo.getStimezoneid()));
				}
			}

			final MethodValidity convertedObject = objMapper.convertValue(dateUtilityFunction
					.convertInputDateToUTCByZone(methodvalidity, lstDateField, lstTZcolumn, true, userInfo),
					new TypeReference<MethodValidity>() {
					});

			if (convertedObject.getDvalidityenddate().isBefore(convertedObject.getDvaliditystartdate()) != false) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_STARTENDDATEVALIDATE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String schSeq = "select nsequenceno from seqnotestmanagement where stablename='methodvalidity' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			
			int seqNo = jdbcTemplate.queryForObject(schSeq, Integer.class);

			seqNo = seqNo + 1;

			String methodInsert = "";

			methodInsert = "insert into methodvalidity(nmethodvaliditycode,nmethodcode,dvaliditystartdate,dvalidityenddate,"
					+ " noffsetdvaliditystartdate,ntzvaliditystartdatetimezone,noffsetdvalidityenddate,ntzvalidityenddatetimezone,dmodifieddate,ntransactionstatus,nsitecode,nstatus) "
					+ " values(" + seqNo + ",'" + methodvalidity.getNmethodcode() + "','"
					+ convertedObject.getSvaliditystartdate() + "','" + convertedObject.getSvalidityenddate() + "',"
					+ methodvalidity.getNoffsetdvaliditystartdate() + ","
					+ methodvalidity.getNtzvaliditystartdatetimezone() + "" + ","
					+ methodvalidity.getNoffsetdvalidityenddate() + "," + methodvalidity.getNtzvalidityenddatetimezone()
					+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + methodvalidity.getNsitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(methodInsert);

			methodInsert = "update seqnotestmanagement set nsequenceno=" + seqNo + " where stablename='methodvalidity' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			jdbcTemplate.execute(methodInsert);

			methodvalidity.setNmethodvaliditycode(seqNo);

			final String str = "select COALESCE(TO_CHAR(mv.dvaliditystartdate,'" + userInfo.getSsitedate()
					+ "'),'') as svaliditystartdate , " + " COALESCE(TO_CHAR(mv.dvalidityenddate,'"
					+ userInfo.getSsitedate() + "'),'') as svalidityenddate from methodvalidity mv where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " nmethodvaliditycode="
					+ seqNo + "";

			List<MethodValidity> MethodValidityList = jdbcTemplate.query(str, new MethodValidity());
			methodvalidity.setSvaliditystartdate(MethodValidityList.get(0).getSvaliditystartdate());
			methodvalidity.setSvalidityenddate(MethodValidityList.get(0).getSvalidityenddate());

			savedMethodValidList.add(methodvalidity);
			multilingualIDList.add("IDS_ADDMETHODVALIDITY");

			auditUtilityFunction.fnInsertAuditAction(savedMethodValidList, 1, null, multilingualIDList, userInfo);
			return new ResponseEntity<>(getMethod(methodvalidity.getNmethodcode(), userInfo).getBody(), HttpStatus.OK);
		}
	}

	/**
	 * This method is used to fetch the active Method objects for the specified
	 * Method name and site.
	 * 
	 * @param methodname      [String] Method name for which the records are to be
	 *                        fetched
	 * @param nmasterSiteCode [int] primary key of site object
	 * @return list of active method based on the specified method name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Method getMethodByName(final String methodname, final int nmethodCatCode, final int nmasterSiteCode)
			throws Exception {
		
		final String strQuery = "select nmethodcode from method where smethodname = N'"
				+ stringUtilityFunction.replaceQuote(methodname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode =" + nmasterSiteCode;

		return (Method) jdbcUtilityFunction.queryForObject(strQuery, Method.class, jdbcTemplate);
	}

	/**
	 * This method is used to get a default Method object with respect to the site
	 * code
	 * 
	 * @param nsiteCode   [int] Site code
	 * @param nmethodcode [int] primary key of method table nmethodcode
	 * @return a Method Object
	 * @throws Exception that are from DAO layer
	 */
	private Method getMethodByDefaultStatus(final int nmasterSiteCode, final int nmethodCatCode) throws Exception {
		
		final String strQuery = "select * from method m" + " where m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nmethodcatcode =" + nmethodCatCode
				+ " and m.nsitecode = " + nmasterSiteCode;
		
		return (Method) jdbcUtilityFunction.queryForObject(strQuery, Method.class,jdbcTemplate);
	}

	/**
	 * This method is used to update entry in Method table. Need to validate that
	 * the method object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of Method name for the specified
	 * site before saving into database.
	 * 
	 * @param Method [Method] object holding details to be updated in method table
	 * @return response entity object holding response status and data of updated
	 *         Method object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> updateMethod(Method method, MethodValidity methodvalidity, UserInfo userInfo)
			throws Exception {

		final Method objMethod = getActiveMethodById(method.getNmethodcode(), userInfo);
		MethodValidity convertedObject = new MethodValidity();
		List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();
		List<String> lstTZcolumn = new ArrayList<String>();
		final ObjectMapper objMapper = new ObjectMapper();

		if (objMethod == null) {
			
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			
			final String queryString = "select nmethodcode from method where smethodname = '"
					+ stringUtilityFunction.replaceQuote(method.getSmethodname()) + "' and nmethodcode <> "
					+ method.getNmethodcode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final Method objMethodDubilcate = (Method) jdbcUtilityFunction.queryForObject(queryString, Method.class,jdbcTemplate);

			if (objMethodDubilcate==null) { 

				if (methodvalidity.getSvaliditystartdate() != null) {

					objMapper.registerModule(new JavaTimeModule());

					if (methodvalidity.getDvaliditystartdate() != null) {
					
						methodvalidity.setSvaliditystartdate(methodvalidity.getSvaliditystartdate().replace("T", " "));
						lstDateField.add("svaliditystartdate");
						lstDatecolumn.add(methodvalidity.getStzvaliditystartdatetimezone());
						lstTZcolumn.add("ntzvaliditystartdatetimezone");
						if (userInfo.getNtimezonecode() > 0) {
							methodvalidity
									.setNoffsetdvaliditystartdate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(
											methodvalidity.getSvaliditystartdate(), userInfo.getStimezoneid()));
						}
					}

					if (methodvalidity.getDvalidityenddate() != null) {
						
						methodvalidity.setSvalidityenddate(methodvalidity.getSvalidityenddate().replace("T", " "));
						lstDateField.add("svalidityenddate");
						lstDatecolumn.add(methodvalidity.getStzvalidityenddatetimezone());
						lstTZcolumn.add("ntzvalidityenddatetimezone");
						if (userInfo.getNtimezonecode() > 0) {
							methodvalidity
									.setNoffsetdvalidityenddate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(
											methodvalidity.getSvalidityenddate(), userInfo.getStimezoneid()));
						}
					}

					convertedObject = objMapper.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(methodvalidity,
							lstDateField, lstTZcolumn, true, userInfo),
							new TypeReference<MethodValidity>() {
							});

					if (convertedObject.getDvaliditystartdate().isAfter(convertedObject.getDvalidityenddate())) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_STARTENDDATEVALIDATE",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				}

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();

				if (method.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

					final Method defaultMethod = getMethodByDefaultStatus(userInfo.getNmastersitecode(),
							method.getNmethodcatcode());

					if (defaultMethod != null && defaultMethod.getNmethodcode() != method.getNmethodcode()) {

						
						listBeforeUpdate.add(defaultMethod);

						defaultMethod.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update method set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmethodcode="
								+ defaultMethod.getNmethodcode();
						
						jdbcTemplate.execute(updateQueryString);
						
						multilingualIDList.add("IDS_EDITMETHOD");
						listAfterUpdate.add(defaultMethod);
					}

				}

				final String updateQueryString = "update method set smethodname=N'"
						+ stringUtilityFunction.replaceQuote(method.getSmethodname()) + "', nmethodcatcode = "
						+ method.getNmethodcatcode() + ", sdescription =N'"
						+ stringUtilityFunction.replaceQuote(method.getSdescription()) + "', ndefaultstatus = "
						+ method.getNdefaultstatus() + ", nneedvalidity = " + method.getNneedvalidity()
						+ ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmethodcode="
						+ method.getNmethodcode();

				jdbcTemplate.execute(updateQueryString);

				multilingualIDList.add("IDS_EDITMETHOD");
				listAfterUpdate.add(method);
				listBeforeUpdate.add(objMethod);

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				
				if (objMethod.getNneedvalidity() != method.getNneedvalidity()) {
					
					if (method.getNneedvalidity() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						
						if (methodvalidity.getSvaliditystartdate() != null) {
							
							final List<String> multilingualIDValList = new ArrayList<>();
							final List<Object> savedMethodValList = new ArrayList<>();
							
							final String schSeq = "select nsequenceno from seqnotestmanagement where stablename='methodvalidity' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							int seqNo = jdbcTemplate.queryForObject(schSeq, Integer.class);
							seqNo = seqNo + 1;

							String methodInsert = "";

							methodInsert = "insert into methodvalidity(nmethodvaliditycode,nmethodcode,dvaliditystartdate,dvalidityenddate,"
									+ " noffsetdvaliditystartdate,ntzvaliditystartdatetimezone,noffsetdvalidityenddate,ntzvalidityenddatetimezone,dmodifieddate,ntransactionstatus,nsitecode,nstatus) "
									+ " values(" + seqNo + ",'" + method.getNmethodcode() + "','"
									+ convertedObject.getSvaliditystartdate() + "','"
									+ convertedObject.getSvalidityenddate() + "',"
									+ methodvalidity.getNoffsetdvaliditystartdate() + ","
									+ methodvalidity.getNtzvaliditystartdatetimezone() + "" + ","
									+ methodvalidity.getNoffsetdvalidityenddate() + ","
									+ methodvalidity.getNtzvalidityenddatetimezone() + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
									+ methodvalidity.getNsitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

							jdbcTemplate.execute(methodInsert);

							methodInsert = "update seqnotestmanagement set nsequenceno=" + seqNo
									+ " where stablename='methodvalidity' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							jdbcTemplate.execute(methodInsert);

							methodvalidity.setNmethodcode(method.getNmethodcode());
							methodvalidity.setSvaliditystartdate(methodvalidity.getSvaliditystartdate().replace("T", " "));
							methodvalidity.setSvalidityenddate(methodvalidity.getSvalidityenddate().replace("T", " "));

							savedMethodValList.add(methodvalidity);
							multilingualIDValList.add("IDS_ADDMETHODVALIDITY");
							
							auditUtilityFunction.fnInsertAuditAction(savedMethodValList, 1, null, multilingualIDValList,userInfo);
						}
					}
				}
			
				return getMethod(method.getNmethodcode(), userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateMethodValidity(MethodValidity methodvalidity, UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();


		Map<String, Object> objValidity = ((Map<String, Object>) getActiveMethodValidityById(
				methodvalidity.getNmethodvaliditycode(), userInfo).getBody());
		
		final MethodValidity objMethodvalidity = ((List<MethodValidity>) objValidity.get("MethodValidity")).get(0);

		String strOld = "select m.smethodname,mv.nmethodvaliditycode,mv.ntransactionstatus,COALESCE(TO_CHAR(mv.dvaliditystartdate,'"
				+ userInfo.getSsitedate()
				+ "'),'') as svaliditystartdate ,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, "
				+ "COALESCE(TO_CHAR(mv.dvalidityenddate,'" + userInfo.getSsitedate()
				+ "'),'') as svalidityenddate from methodvalidity mv,method m,transactionstatus ts where mv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and m.nmethodcode=mv.nmethodcode and ts.ntranscode=mv.ntransactionstatus and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "mv.nmethodvaliditycode="
				+ methodvalidity.getNmethodvaliditycode() + "";

		List<MethodValidity> MethodValidityOldList = jdbcTemplate.query(strOld, new MethodValidity());
		objMethodvalidity.setSvaliditystartdate(MethodValidityOldList.get(0).getSvaliditystartdate());
		objMethodvalidity.setSvalidityenddate(MethodValidityOldList.get(0).getSvalidityenddate());
		objMethodvalidity.setSmethodname(MethodValidityOldList.get(0).getSmethodname());
		objMethodvalidity.setNmethodvaliditycode(MethodValidityOldList.get(0).getNmethodvaliditycode());
		objMethodvalidity.setNtransactionstatus(MethodValidityOldList.get(0).getNtransactionstatus());

		List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();
		List<String> lstTZcolumn = new ArrayList<String>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		if (methodvalidity.getDvaliditystartdate() != null) {

			methodvalidity.setSvaliditystartdate(methodvalidity.getSvaliditystartdate().replace("T", " "));
			lstDateField.add("svaliditystartdate");
			lstDatecolumn.add(methodvalidity.getStzvaliditystartdatetimezone());
			lstTZcolumn.add("ntzvaliditystartdatetimezone");
			if (userInfo.getNtimezonecode() > 0) {
				methodvalidity.setNoffsetdvaliditystartdate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(
						methodvalidity.getSvaliditystartdate(), userInfo.getStimezoneid()));
			}
		}

		if (methodvalidity.getDvalidityenddate() != null) {

			methodvalidity.setSvalidityenddate(methodvalidity.getSvalidityenddate().replace("T", " "));
			lstDateField.add("svalidityenddate");
			lstDatecolumn.add(methodvalidity.getStzvalidityenddatetimezone());
			lstTZcolumn.add("ntzvalidityenddatetimezone");
			if (userInfo.getNtimezonecode() > 0) {
				methodvalidity.setNoffsetdvalidityenddate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(
						methodvalidity.getSvalidityenddate(), userInfo.getStimezoneid()));
			}
		}


		final MethodValidity convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(methodvalidity, lstDateField, lstTZcolumn, true, userInfo),
				new TypeReference<MethodValidity>() {
				});

		if (convertedObject.getDvalidityenddate().isBefore(convertedObject.getDvaliditystartdate()) != false) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_STARTENDDATEVALIDATE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final String updateQueryString = "update methodvalidity set dvaliditystartdate='"
				+ stringUtilityFunction.replaceQuote(convertedObject.getSvaliditystartdate())
				+ "', dvalidityenddate = '" + stringUtilityFunction.replaceQuote(convertedObject.getSvalidityenddate())
				+ "', noffsetdvaliditystartdate =" + methodvalidity.getNoffsetdvaliditystartdate()
				+ ",ntzvaliditystartdatetimezone=" + methodvalidity.getNtzvaliditystartdatetimezone() + ","
				+ " noffsetdvalidityenddate =" + methodvalidity.getNoffsetdvalidityenddate()
				+ ",ntzvalidityenddatetimezone=" + methodvalidity.getNtzvalidityenddatetimezone() + ""
				+ ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',nmethodcode="
				+ methodvalidity.getNmethodcode() + " where nmethodvaliditycode="
				+ methodvalidity.getNmethodvaliditycode();

		jdbcTemplate.execute(updateQueryString);

		multilingualIDList.add("IDS_EDITMETHODVALIDITY");

		String strnew = "select m.smethodname,mv.nmethodvaliditycode,mv.ntransactionstatus,COALESCE(TO_CHAR(mv.dvaliditystartdate,'"
				+ userInfo.getSsitedate()
				+ "'),'') as svaliditystartdate ,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, "
				+ " COALESCE(TO_CHAR(mv.dvalidityenddate,'" + userInfo.getSsitedate()
				+ "'),'') as svalidityenddate from methodvalidity mv,method m,transactionstatus ts where mv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and m.nmethodcode=mv.nmethodcode and ts.ntranscode=mv.ntransactionstatus and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  mv.nmethodvaliditycode="
				+ methodvalidity.getNmethodvaliditycode() + " and m.nsitecode="+userInfo.getNmastersitecode()+" and mv.nsitecode="+userInfo.getNmastersitecode()+"";

		List<MethodValidity> MethodValiditynewList = jdbcTemplate.query(strnew, new MethodValidity());
		methodvalidity.setSvaliditystartdate(MethodValiditynewList.get(0).getSvaliditystartdate());
		methodvalidity.setSvalidityenddate(MethodValiditynewList.get(0).getSvalidityenddate());
		methodvalidity.setSmethodname(MethodValiditynewList.get(0).getSmethodname());
		methodvalidity.setNmethodvaliditycode(MethodValiditynewList.get(0).getNmethodvaliditycode());
		methodvalidity.setNtransactionstatus(MethodValiditynewList.get(0).getNtransactionstatus());
		methodvalidity.setNtransactionstatus(MethodValiditynewList.get(0).getNtransactionstatus());
		methodvalidity.setSdisplaystatus(MethodValiditynewList.get(0).getSdisplaystatus());



		listAfterUpdate.add(methodvalidity);
		listBeforeUpdate.add(objMethodvalidity);

		auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);

		
		return new ResponseEntity<>(getMethod(methodvalidity.getNmethodcode(), userInfo).getBody(), HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve active method object based on the specified
	 * nmethodcode.
	 * 
	 * @param nmethodcode [int] primary key of method object
	 * @return response entity object holding response status and data of method
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Method getActiveMethodById(int nmethodcode, final UserInfo userInfo) throws Exception {
	
		final String strQuery = "select m.*,mc.smethodcatname,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus"
				+ " from method m,transactionstatus ts ,methodcategory mc " + " where m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.ntranscode = m.ndefaultstatus and m.nmethodcatcode = mc.nmethodcatcode "
				+ " and m.nmethodcode = " + nmethodcode +" and m.nsitecode="+userInfo.getNmastersitecode()+" and mc.nsitecode="+userInfo.getNmastersitecode()+"";

		return (Method) jdbcUtilityFunction.queryForObject(strQuery, Method.class, jdbcTemplate);

	}

	@Override
	public ResponseEntity<Object> getActiveMethodValidityById(int nmethodvaliditycode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select mv.nmethodvaliditycode,mv.nmethodcode,mv.ntransactionstatus,mv.dvaliditystartdate,mv.dvalidityenddate, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,COALESCE(TO_CHAR(mv.dvaliditystartdate,'"
				+ userInfo.getSpgsitedatetime() + "'),'') as svaliditystartdate ,"
				+ " COALESCE(TO_CHAR(mv.dvalidityenddate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as svalidityenddate from methodvalidity mv,transactionstatus ts " + " where mv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.ntranscode = mv.ntransactionstatus" + " and mv.nmethodvaliditycode = " + nmethodvaliditycode+" and mv.nsitecode="+userInfo.getNmastersitecode()+"";

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<MethodValidity> lstMthdValid = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new MethodValidity()),
						Arrays.asList("svaliditystartdate", "svalidityenddate"), null, userInfo, false, null, false),
				new TypeReference<List<MethodValidity>>() {
				});
		Map<String, Object> objMap = new LinkedHashMap<>();
		if (!lstMthdValid.isEmpty()) {
			objMap.put("MethodValidity", lstMthdValid);
			return new ResponseEntity<>(objMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	
	}

	public ResponseEntity<Object> getActiveMethodValidityByMethodId(int nmethodcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select mv.nmethodvaliditycode,mv.nmethodcode,mv.ntransactionstatus,mv.dvaliditystartdate,mv.dvalidityenddate, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,COALESCE(TO_CHAR(mv.dvaliditystartdate,'"
				+ userInfo.getSpgsitedatetime() + "'),'') as svaliditystartdate ,"
				+ " COALESCE(TO_CHAR(mv.dvalidityenddate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as svalidityenddate from methodvalidity mv,transactionstatus ts  where mv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.ntranscode = mv.ntransactionstatus and mv.nsitecode="+userInfo.getNmastersitecode()+" and mv.nmethodcode = " + nmethodcode;

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<MethodValidity> lstMthdValid = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new MethodValidity()),
						Arrays.asList("svaliditystartdate", "svalidityenddate"), null, userInfo, false, null, false),
				new TypeReference<List<MethodValidity>>() {
				});
		Map<String, Object> objMap = new LinkedHashMap<>();
		objMap.put("MethodValidity", lstMthdValid);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	

	}

	public MethodValidity getActiveMethodValidityByValidityId(int nmethodvaliditycode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select mv.nmethodvaliditycode,mv.nmethodcode,mv.ntransactionstatus,mv.dvaliditystartdate,mv.dvalidityenddate, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,COALESCE(TO_CHAR(mv.dvaliditystartdate,'"
				+ userInfo.getSpgsitedatetime() + "'),'') as svaliditystartdate ,"
				+ " COALESCE(TO_CHAR(mv.dvalidityenddate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as svalidityenddate from methodvalidity mv,transactionstatus ts where mv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.ntranscode = mv.ntransactionstatus  and mv.nmethodvaliditycode = " + nmethodvaliditycode +" and mv.nsitecode="+userInfo.getNmastersitecode()+"";

		return (MethodValidity) jdbcUtilityFunction.queryForObject(strQuery, MethodValidity.class, jdbcTemplate);

	}

	public MethodValidity getActiveApproveMethodValidityById(int nmethodvaliditycode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select mv.nmethodvaliditycode, mv.nmethodcode, mv.dvaliditystartdate, mv.dvalidityenddate, mv.noffsetdvaliditystartdate, mv.ntzvaliditystartdatetimezone, mv.noffsetdvalidityenddate, mv.ntzvalidityenddatetimezone, mv.ntransactionstatus, mv.dmodifieddate, mv.nsitecode, mv.nstatus from methodvalidity mv  where mv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and mv.nmethodvaliditycode = "
				+ nmethodvaliditycode;

		return (MethodValidity) jdbcUtilityFunction.queryForObject(strQuery, MethodValidity.class, jdbcTemplate);

	}

	/**
	 * This method is used to delete entry in method table. Need to validate that
	 * the specified method object is active and is not associated with any of its
	 * child tables before updating its nstatus to -1.
	 * 
	 * @param method [method] object holding detail to be deleted in method table
	 * @return response entity object holding response status and data of deleted
	 *         method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteMethod(Method method, UserInfo userInfo) throws Exception {
		final Method methodbyID = (Method) getActiveMethodById(method.getNmethodcode(), userInfo);

		if (methodbyID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select * from methodvalidity where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nmethodcode = "
					+ method.getNmethodcode();

			final List<MethodValidity> methodValList = (List<MethodValidity>) jdbcTemplate.query(queryString,
					new MethodValidity());

			if (methodValList.isEmpty()) {
				// deleteValidation
				final String query = "select 'IDS_TESTMASTER' as Msg from testmethod where nmethodcode= "
						+ methodbyID.getNmethodcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

				boolean validRecord = false;
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
					// ALPD-4513--Vignesh R(05-09-2024)
					Map<String, Object> objOneToManyValidation = new HashMap<String, Object>();
					objOneToManyValidation.put("primaryKeyValue", Integer.toString(methodbyID.getNmethodcode()));
					objOneToManyValidation.put("stablename", "method");
					validatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);

					if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS
							.getReturnvalue()) {
						validRecord = true;
					} else {
						validRecord = false;
					}
				}

				if (validRecord) {

					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> deletedMethodList = new ArrayList<>();
					final List<String> multilingualIDValList = new ArrayList<>();
					final List<Object> deletedMethodValList = new ArrayList<>();
					
					final String updateQueryString = "update method set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmethodcode="
							+ method.getNmethodcode();

					jdbcTemplate.execute(updateQueryString);

					method.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

					deletedMethodList.add(method);
					multilingualIDList.add("IDS_DELETEMETHOD");
					auditUtilityFunction.fnInsertAuditAction(deletedMethodList, 1, null, multilingualIDList, userInfo);

					
					final Map<String, Object> objValidity = ((Map<String, Object>) getActiveMethodValidityByMethodId(
							method.getNmethodcode(), userInfo).getBody());

					final List<MethodValidity> list = (List<MethodValidity>) objValidity.get("MethodValidity");
					if (list.size() > 0) {


						final String updateString = "update methodvalidity set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmethodcode="
								+ method.getNmethodcode();

						jdbcTemplate.execute(updateString);

					
						deletedMethodValList.add(list);
						multilingualIDValList.add("IDS_DELETEMETHODVALIDITY");

						auditUtilityFunction.fnInsertListAuditAction(deletedMethodValList, 1, null,
								multilingualIDValList, userInfo);
					}
					return getMethod(null, userInfo);
				} else {
					return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteMethodValidity(MethodValidity methodvalidity, UserInfo userInfo)
			throws Exception {

		final MethodValidity methodbyID = (MethodValidity) getActiveMethodValidityByValidityId(
				methodvalidity.getNmethodvaliditycode(), userInfo);

		if (methodbyID == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select 'IDS_TESTMASTER' as Msg from testmethod tm,methodvalidity mv where mv.nmethodvaliditycode= "
					+ methodbyID.getNmethodvaliditycode() + " and mv.nmethodcode = tm.nmethodcode and tm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);

			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedMethodList = new ArrayList<>();
				final String updateQueryString = "update methodvalidity set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmethodvaliditycode="
						+ methodvalidity.getNmethodvaliditycode();

				jdbcTemplate.execute(updateQueryString);

				methodvalidity.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				methodvalidity.setSvaliditystartdate(methodvalidity.getSvaliditystartdate().replace("T", " "));
				methodvalidity.setSvalidityenddate(methodvalidity.getSvalidityenddate().replace("T", " "));

				deletedMethodList.add(methodvalidity);
				multilingualIDList.add("IDS_DELETEMETHODVALIDITY");

				auditUtilityFunction.fnInsertAuditAction(deletedMethodList, 1, null, multilingualIDList, userInfo);

			
				return new ResponseEntity<>(getMethod(methodvalidity.getNmethodcode(), userInfo).getBody(),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> approveMethodValidity(MethodValidity methodvalidity, UserInfo userInfo)
			throws Exception {

		final MethodValidity methodactive = (MethodValidity) getActiveMethodValidityByValidityId(
				methodvalidity.getNmethodvaliditycode(), userInfo);
		if (methodactive == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		Instant dCurrentDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		Instant dEndDate = Instant.parse(methodvalidity.getStempvalidityenddate());

		if (dCurrentDate.isAfter(dEndDate) || dCurrentDate.equals(dEndDate)) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_CURRENTENDDATEAPPROVE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final MethodValidity methodbyID = (MethodValidity) getActiveApproveMethodValidityById(
				methodvalidity.getNmethodvaliditycode(), userInfo);

		if (methodbyID != null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYAPPROVED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

		 String strQuery = "select mv.* from methodvalidity mv " + " where mv.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mv.ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and mv.nmethodcode = "
					+ methodvalidity.getNmethodcode() + " and mv.nmethodvaliditycode != "
					+ methodvalidity.getNmethodvaliditycode();

			MethodValidity mValidity = (MethodValidity) jdbcUtilityFunction.queryForObject(strQuery,
					MethodValidity.class, jdbcTemplate);

			if (mValidity != null) {
				final List<String> multilingualIDSList = new ArrayList<>();
				final List<Object> retireMethodList = new ArrayList<>();

				strQuery = "update methodvalidity set ntransactionstatus = "
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmethodcode = "
						+ methodvalidity.getNmethodcode() + " and nmethodvaliditycode="
						+ mValidity.getNmethodvaliditycode();
				jdbcTemplate.execute(strQuery);

				mValidity.setNtransactionstatus((short) Enumeration.TransactionStatus.RETIRED.gettransactionstatus());

				Date date = Date.from(mValidity.getDvaliditystartdate());
				mValidity.setSvaliditystartdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));

				Date date1 = Date.from(mValidity.getDvalidityenddate());
				mValidity.setSvalidityenddate(new SimpleDateFormat(userInfo.getSsitedate()).format(date1));

				retireMethodList.add(mValidity);
				multilingualIDSList.add("IDS_RETIREDMETHODVALIDITY");
				auditUtilityFunction.fnInsertAuditAction(retireMethodList, 1, null, multilingualIDSList, userInfo);

			}

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> approveMethodList = new ArrayList<>();
			final String updateQueryString = "update methodvalidity set ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ",dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmethodvaliditycode="
					+ methodvalidity.getNmethodvaliditycode();

			jdbcTemplate.execute(updateQueryString);

			methodvalidity.setNtransactionstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());

			methodvalidity.setSvaliditystartdate(methodvalidity.getSvaliditystartdate().replace("T", " "));
			methodvalidity.setSvalidityenddate(methodvalidity.getSvalidityenddate().replace("T", " "));

			approveMethodList.add(methodvalidity);
			multilingualIDList.add("IDS_APPROVEMETHODVALIDITY");

			auditUtilityFunction.fnInsertAuditAction(approveMethodList, 1, null, multilingualIDList, userInfo);

			return new ResponseEntity<>(getMethod(methodvalidity.getNmethodcode(), userInfo).getBody(), HttpStatus.OK);
		}

	}

}
