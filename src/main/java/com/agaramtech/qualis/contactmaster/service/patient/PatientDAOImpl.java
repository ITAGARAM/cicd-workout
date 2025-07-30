package com.agaramtech.qualis.contactmaster.service.patient;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.Gender;
import com.agaramtech.qualis.configuration.model.Language;
import com.agaramtech.qualis.contactmaster.model.Country;
import com.agaramtech.qualis.contactmaster.model.Patient;
import com.agaramtech.qualis.contactmaster.model.PatientCaseType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.Enumeration.TransactionStatus;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.registration.model.PatientHistory;
import com.agaramtech.qualis.release.model.COAReportHistory;
import com.agaramtech.qualis.submitter.model.City;
import com.agaramtech.qualis.submitter.model.District;
import com.agaramtech.qualis.submitter.model.Region;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class PatientDAOImpl implements PatientDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PatientDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	public ResponseEntity<Object> getPatient(String spatientid, final UserInfo userInfo, String currentUIDate,
			int needdate) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String fromDate = "";
		String toDate = "";

		// String fromDate = (String) inputMap.get("fromDate");
		// String toDate = (String) inputMap.get("toDate");
		// final String currentUIDate = (String)inputMap.get("currentdate");

		Patient selectedPatient = null;

		if (spatientid == null) {
			if (needdate == -1) {
				final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
						currentUIDate, "datetime", "FromDate");
				fromDate = (String) mapObject.get("FromDate");
				toDate = (String) mapObject.get("ToDate");
				outputMap.put("FromDate", (String) mapObject.get("FromDateWOUTC"));
				outputMap.put("ToDate", (String) mapObject.get("ToDateWOUTC"));
			}
			String date = "";
			if (needdate != -1) {
				date = " ";
			} else {
				date = " and (pm.dmodifieddate between '" + fromDate + "' and '" + toDate + "')";

			}
//			final String query = "select pm.spatientid,pm.sfirstname||' '||pm.slastname as spatientname,"
//					+ " pm.sfathername,pm.sage,pm.ddob,coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
//					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus ,"
//					+ " to_char(pm.ddob,'"+userInfo.getSsitedate()+"') as sdob,"
//				//	+ " pm.saddress,pm.sdistrict,pm.spostalcode,pm.sphoneno,pm.smobileno,pm.semail,"
//					+ " pm.spostalcode,pm.sphoneno,pm.smobileno,pm.semail,"
//					+ " pm.srefid,pm.spassportno,pm.sexternalid,pm.ndistrictcodetemp,pm.ncitycodetemp,pm.nregioncode,pm.nregioncodetemp,"
//					+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts1.jsondata->'stransdisplaystatus'->>'en-US') as scurrentaddress,"
//					+ " coalesce(g.jsondata->'sgendername'->>'"+userInfo.getSlanguagetypecode()+"',g.jsondata->'sgendername'->>'en-US') as sgendername,"
//				    + " c.scityname,cu.scountryname ,r.sregionname,r1.sregionname as sregionnametemp,"
//					+ "d.sdistrictname,d1.sdistrictname as sdistrictnametemp ,c1.scityname as scitynametemp,pm.sstreet,pm.sstreettemp,pm.shouseno,pm.shousenotemp,"
//					+ "pm.sflatno,pm.sflatnotemp,pm.ncitycode,pm.nregioncode,pm.ndistrictcode  from patientmaster pm,gender g, city c ,city c1,country cu ,region r1, region r,district d1,district d,"
//					+ "transactionstatus ts,transactionstatus ts1,registration reg,registrationhistory regh"
//					+ " where pm.ngendercode=g.ngendercode and cu.ncountrycode=pm.ncountrycode"
//					+ " and c.ncitycode=pm.ncitycode and r.nregioncode=pm.nregioncode and r1.nregioncode=pm.nregioncodetemp and "
//					+ " d.ndistrictcode=pm.ndistrictcode and d1.ndistrictcode=pm.ndistrictcodetemp and "
//					+ "  c1.ncitycode=pm.ncitycodetemp and ts.ntranscode=pm.nneedmigrant  "
//					+ "and ts1.ntranscode=pm.nneedcurrentaddress and  pm.nsitecode="+userInfo.getNmastersitecode()
//					+ " and reg.npreregno=regh.npreregno and  regh.ntransactionstatus="+Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
//					+ " and  reg.jsonuidata->>'Patient Id'= pm.spatientid and  (regh.dtransactiondate between '"+fromDate+"' and '"+toDate+"')"
//					+ " and pm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and g.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and d.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and cu.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and reg.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//					+ " and regh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//		           //	+ " order by pm.dcreateddate desc";

			final String query = "select pm.spatientid,pm.sfirstname||' '||pm.slastname as spatientname,pm.sfirstname,pm.slastname,"
					+ " pm.sfathername,pm.sage,pm.ddob,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus ," + " to_char(pm.ddob,'"
					+ userInfo.getSsitedate() + "') as sdob," + " pm.spostalcode,pm.sphoneno,pm.smobileno,pm.semail,"
					+ " pm.srefid,pm.spassportno,pm.sexternalid,pm.ndistrictcodetemp,pm.ncitycodetemp,pm.nregioncode,pm.nregioncodetemp,"
					+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US') as scurrentaddress,"
					+ " coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
					+ "',g.jsondata->'sgendername'->>'en-US') as sgendername,"
					+ " pm.scityname,cu.scountryname ,r.sregionname,r1.sregionname as sregionnametemp,"
					+ "d.sdistrictname,d1.sdistrictname as sdistrictnametemp ,pm.scitynametemp,pm.sstreet,pm.sstreettemp,pm.shouseno,pm.shousenotemp,"
					+ "pm.sflatno,pm.sflatnotemp,pm.ncitycode,pm.nregioncode,pm.ndistrictcode,pm.spostalcodetemp,pm.ngendercode,pm.ncountrycode,"
					+ "to_char(pm.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
					+ "') as smodifieddate  from patientmaster pm,gender g, city c ,city c1,country cu ,region r1, region r,district d1,district d,"
					+ "transactionstatus ts,transactionstatus ts1 "
					+ " where pm.ngendercode=g.ngendercode and cu.ncountrycode=pm.ncountrycode"
					+ " and c.ncitycode=pm.ncitycode and r.nregioncode=pm.nregioncode and r1.nregioncode=pm.nregioncodetemp and "
					+ " d.ndistrictcode=pm.ndistrictcode and d1.ndistrictcode=pm.ndistrictcodetemp and "
					+ "  c1.ncitycode=pm.ncitycodetemp and ts.ntranscode=pm.nneedmigrant  "
					+ "and ts1.ntranscode=pm.nneedcurrentaddress and  pm.nsitecode=" + userInfo.getNmastersitecode()
					+ " " + date + "  and pm.nsitecode= " + userInfo.getNmastersitecode() + " " + "and g.nsitecode="
					+ userInfo.getNmastersitecode() + "  and  c.nsitecode=" + userInfo.getNmastersitecode() + " "
					+ "and c1.nsitecode =" + userInfo.getNmastersitecode() + "  and  cu.nsitecode="
					+ userInfo.getNmastersitecode() + " " + "and r1.nsitecode=" + userInfo.getNmastersitecode()
					+ "  and r.nsitecode=" + userInfo.getNmastersitecode() + " " + "and  d1.nsitecode ="
					+ userInfo.getNmastersitecode() + " and  d.nsitecode =" + userInfo.getNmastersitecode() + " "
					+ "and pm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and g.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and c.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and d.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and cu.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by pm.dmodifieddate desc ";

			LOGGER.info("Get Query -->" + query);
//			final List<?> patientList = objGeneral.getSiteLocalTimeFromUTC(
//					jdbcTemplate.query(query, new Patient()), Arrays.asList("ddob"), null, userInfo, true,
//					null,false);
//			ObjectMapper objMapper = new ObjectMapper();
//			outputMap.put("PatientList", objMapper.convertValue(patientList, new TypeReference<List<Patient>>() {
//			}));

			final List<Patient> patientList = jdbcTemplate.query(query, new Patient());

			String caseType = "select jsondata, npatientcasetypecode, coalesce(jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',jsondata->'sdisplayname'->>'en-US') as spatientcasetypename,jsondata->'sdisplayname'->>'en-US' as displayname "
					+ "from patientcasetype where nsitecode =" + userInfo.getNmastersitecode() + " " + "and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<PatientCaseType> patientCaseType = jdbcTemplate.query(caseType, new PatientCaseType());
			outputMap.put("PatientCaseType", patientCaseType);
			outputMap.put("SelectedPatientCaseType", patientCaseType.get(0));

//		    outputMap.putAll((Map<String, Object>) getGender(userInfo).getBody());
//			outputMap.putAll((Map<String, Object>) getCity(userInfo).getBody());
//			outputMap.putAll((Map<String, Object>) getCountry(userInfo).getBody());
//			outputMap.putAll((Map<String, Object>) getRegion(userInfo).getBody());

			if (patientList.isEmpty()) {
				outputMap.put("PatientList", patientList);
				outputMap.put("SelectedPatient", null);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				// selectedPatient = (Patient) patientList.get(patientList.size() - 1);
				selectedPatient = (Patient) patientList.get(0);
				spatientid = selectedPatient.getSpatientid();
				outputMap.put("PatientList", patientList);
			}
		} else {
			selectedPatient = getActivePatientById(spatientid, userInfo);
		}

		if (selectedPatient == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedPatient", selectedPatient);

			// outputMap.put("PatientHistory",getPatientHistory(selectedPatient.getNpatientcode(),
			// userInfo).getBody());

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	public Patient getActivePatientById(final String spatientid, final UserInfo userInfo) throws Exception {

		final String query = "select pm.spatientid,pm.sfirstname,c.ncitycode,cu.ncountrycode,g.ngendercode,"
				+ " pm.slastname,pm.sfirstname||' '||pm.slastname as spatientname,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "	ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus ,"
				+ " case when pm.sfathername='null' then '' else pm.sfathername end ,pm.sage,pm.ddob,"
				+ " to_char(pm.ddob,'" + userInfo.getSsitedate() + "') as sdob,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US') as scurrentaddress,"
				+ " case when pm.spostalcode='null' then '' else pm.spostalcode end ,case when pm.spostalcodetemp='null' then '' else pm.spostalcodetemp end  ,case when pm.sphoneno='null' then '' else pm.sphoneno end ,"
				+ " case when pm.smobileno='null' then '' else pm.smobileno end ,case when pm.semail='null' then '' else pm.semail end,"
				+ " case when pm.srefid='null' then '' else pm.srefid end  ,case when pm.spassportno='null' then '' else pm.spassportno end "
				+ ",case when pm.sexternalid='null' then '' else pm.sexternalid end ,"
				+ " coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
				+ "',g.jsondata->'sgendername'->>'en-US') as sgendername,"
				+ " pm.scityname,cu.scountryname,pm.sstreet,pm.sstreettemp,pm.shouseno,pm.shousenotemp,pm.sflatno,pm.sflatnotemp,"
				+ "pm.nneedcurrentaddress,pm.nneedmigrant, r1.sregionname as sregionnametemp,r.sregionname as sregionname,pm.sexternalid ,pm.scitynametemp "
				+ ",pm.nregioncode,pm.nregioncodetemp,pm.ndistrictcode,pm.ndistrictcodetemp ,d.sdistrictname,d1.sdistrictname as sdistrictnametemp ,pm.ncitycodetemp  "
				+ "from transactionstatus ts,transactionstatus ts1, patientmaster pm,gender g, city c ,country cu,region r,district d,city c1,district d1,region r1"
				+ " where pm.ngendercode=g.ngendercode and cu.ncountrycode=pm.ncountrycode and ts.ntranscode=pm.nneedmigrant"
				+ " and c.ncitycode=pm.ncitycode and  c1.ncitycode=pm.ncitycodetemp  and r.nregioncode=pm.nregioncode and r1.nregioncode=pm.nregioncodetemp  and  ts1.ntranscode=pm.nneedcurrentaddress"
				+ "  and d.ndistrictcode=pm.ndistrictcode   and d1.ndistrictcode=pm.ndistrictcodetemp "
				+ "and pm.nsitecode= " + userInfo.getNmastersitecode() + " " + "and g.nsitecode="
				+ userInfo.getNmastersitecode() + "  and  c.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ "and c1.nsitecode =" + userInfo.getNmastersitecode() + "  and  cu.nsitecode="
				+ userInfo.getNmastersitecode() + " " + "and r1.nsitecode=" + userInfo.getNmastersitecode()
				+ "  and r.nsitecode=" + userInfo.getNmastersitecode() + " " + "and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and g.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and cu.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and pm.spatientid='" + spatientid
				+ "'";

		return (Patient) jdbcUtilityFunction.queryForObject(query, Patient.class, jdbcTemplate);

	}

	public ResponseEntity<Object> getGender(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select ngendercode,coalesce(g.jsondata->'sgendername'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + " g.jsondata->'sgendername'->>'en-US') as sgendername "
				+ " from gender g where g.nsitecode=" + userInfo.getNmastersitecode() + " " + "and g.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and g.ngendercode > 0 order by ngendercode desc";
		outputMap.put("genderList", jdbcTemplate.query(strQuery, new Gender()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

//	public ResponseEntity<Object> getDiagnosticCase(final UserInfo userInfo) throws Exception {
//		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
//		final String strQuery = "select ndiagnosticcasecode,coalesce(g.jsondata->'sdiagnosticcasename'->>'"
//				+ userInfo.getSlanguagetypecode() + "'," + " g.jsondata->'sdiagnosticcasename'->>'en-US') as sdiagnosticcasename "
//				+ " from diagnosticcase g where " + " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and g.ndiagnosticcasecode > 0 order by ndiagnosticcasecode desc";
//		outputMap.put("diagnosticCaseList", jdbcTemplate.query(strQuery, new Gender()));
//		return new ResponseEntity<>(outputMap, HttpStatus.OK);
//	}

//	@Override
//	public ResponseEntity<Object> getCity(UserInfo userInfo) throws Exception {
//		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
//		final String strQuery =" select * from city where nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//								+ " and ncitycode > 0 and nsitecode= "+userInfo.getNmastersitecode()+"";
//		outputMap.put("cityList",jdbcTemplate.query(strQuery, new City()));
//		return new ResponseEntity<>(outputMap, HttpStatus.OK);
//	}

	@Override
	public ResponseEntity<Object> getCountry(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select c.ncountrycode,c.scountryname,c.scountryshortname,c.stwocharcountry,c.sthreecharcountry,c.nsitecode,c.nstatus"
				+ " from Country c where c.nsitecode=" + userInfo.getNmastersitecode() + " and c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.ncountrycode > 0 and c.nsitecode= " + userInfo.getNmastersitecode();
		outputMap.put("countryList", jdbcTemplate.query(strQuery, new Country()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select r.nregioncode,r.sregionname,r.sregioncode,r.nsitecode,r.nstatus"
				+ " from region r where r.nsitecode=" + userInfo.getNmastersitecode() + " and r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nregioncode > 0 and "
				+ " r.nsitecode= " + userInfo.getNmastersitecode();
		outputMap.put("regionList", jdbcTemplate.query(strQuery, new Region()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> createPatient(Patient patient, int needdate, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = savePatient(patient, userInfo);
		String response = (String) returnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(response)) {
			ObjectMapper objMapper = new ObjectMapper();
			Patient newPatient = objMapper.convertValue(returnMap.get("Patient"), Patient.class);

			return getPatient(needdate == 2 ? newPatient.getSpatientid() : null, userInfo, patient.getCurrentdate(),
					needdate);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	public Map<String, Object> savePatient(Patient patient, final UserInfo userInfo) throws Exception {

		final String sQuerys = " lock  table patientmaster " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuerys);

		final List<Object> patientDetails = new ArrayList<>();
		Map<String, Object> returnMap = new HashMap<String, Object>();
//		if (patient.getDdob() != null) {
//			patient.setSdob(commonFunction.instantDateToString(patient.getDdob()).replace("T", " "));
//		}
		final String Patientname = patient.getSfirstname() + " " + patient.getSlastname();
		// final String fathername = patient.getSfathername();
		// final String ddob =
		// patient.getSdob().toString().substring(0,patient.getSdob().length()-9 );
		final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
		String ddob = sdFormat.format(patient.getDdob());
		patient.setSdob(new SimpleDateFormat(userInfo.getSsitedate()).format(patient.getDdob()));
		// patient.setDdob(sdFormat.parse(doj));
		// patient.setDdob(new
		// SimpleDateFormat(userInfo.getSsitedate()).format(patient.getDdob()));
		final int gender = patient.getNgendercode();

		final String concat = Patientname + ddob + gender;
		patient.setSpatientid(UUID.nameUUIDFromBytes(concat.getBytes("UTF8")).toString());
		final String sQuery = "select spatientid from patientmaster where sfirstname = '"
				+ stringUtilityFunction.replaceQuote(patient.getSfirstname()) + "' and slastname='"
				+ stringUtilityFunction.replaceQuote(patient.getSlastname()) + "'" + "  and ddob= '" + ddob + "'"
				+ "  and ngendercode=" + patient.getNgendercode() + " and nsitecode =" + userInfo.getNmastersitecode()
				+ " " + "  and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<Patient> duplicatePatient = jdbcTemplate.query(sQuery, new Patient());
		if (duplicatePatient.isEmpty()) {
			// String response =
			// (String)objMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
			// if (response.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {

			final String sDelQuery = "select spatientid from patientmaster where sfirstname = '"
					+ stringUtilityFunction.replaceQuote(patient.getSfirstname()) + "' and slastname='"
					+ stringUtilityFunction.replaceQuote(patient.getSlastname()) + "'" + "  and ddob= '" + ddob + "'"
					+ "  and ngendercode=" + patient.getNgendercode() + "  and spatientid = '" + patient.getSpatientid()
					+ "' and nsitecode =" + userInfo.getNmastersitecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus();

			// final List<Patient> deletedPatient=jdbcTemplate.queryForObject(sDelQuery,new
			// Patient());
			Patient deletedPatient = (Patient) jdbcUtilityFunction.queryForObject(sDelQuery, Patient.class,
					jdbcTemplate);

			if (deletedPatient == null) {

				// String patientSeq = "";
				// patient = (Patient) insertObject(patient, SeqNoContactMaster.class,
				// "nsequenceno");
//				patientSeq = "select nsequenceno from seqnocontactmaster where stablename='patientmaster'";
//				int seqNo = jdbcTemplate.queryForObject(patientSeq, Integer.class);
//				seqNo = seqNo + 1;
				String patientInsert = "";

//			patientInsert = "insert into patientmaster(spatientid,sfirstname,slastname,"
//					+ " sfathername,ngendercode,ncitycode,ncountrycode,ddob,sage,saddress,"
//					+ " sdistrict,spostalcode,sphoneno,smobileno,semail,srefid,spassportno,sexternalid,"
//					+ " dcreateddate,ntzcreateddate,noffsetdcreateddate,"
//					+ " dmodifieddate,ntzmodifieddate,noffsetdmodifieddate,nsitecode,nstatus)"
//					+ " values(N'"+stringUtilityFunction.replaceQuote(patient.getSpatientid()) + "',N'"+stringUtilityFunction.replaceQuote(patient.getSfirstname())+"',"
//					+ " N'"+stringUtilityFunction.replaceQuote(patient.getSlastname())+"',"
//					+ " case when '"+patient.getSfathername()+"'=null then null else N'"+stringUtilityFunction.replaceQuote(patient.getSfathername()) + "'end,"+patient.getNgendercode()+","
//					+ " " +patient.getNcitycode()+","+patient.getNcountrycode()+ ",'"+ddob+"',"
//					+ " N'"+stringUtilityFunction.replaceQuote(patient.getSage())+"',"
//					+ " N'"+stringUtilityFunction.replaceQuote(patient.getSaddress())+"',"
//					+ " case when '"+patient.getSdistrict()+"'=null then null else N'"+stringUtilityFunction.replaceQuote(patient.getSdistrict())+"'end ,"
//					+ " case when '"+patient.getSpostalcode()+"'=null then null else N'"+stringUtilityFunction.replaceQuote(patient.getSpostalcode())+"'end,"
//					+ " case when '"+patient.getSphoneno()+"'=null then null else N'"+stringUtilityFunction.replaceQuote(patient.getSphoneno())+"'end,"
//					+ " case when '"+patient.getSmobileno()+"'=null then null else N'"+stringUtilityFunction.replaceQuote(patient.getSmobileno())+"'end,"
//					+ " case when '"+patient.getSemail()+"'=null then null else N'"+stringUtilityFunction.replaceQuote(patient.getSemail())+"'end ,"
//					+ " case when '"+patient.getSrefid()+"'=null then null else N'"+stringUtilityFunction.replaceQuote(patient.getSrefid())+"'end,"
//					+ " case when '"+patient.getSpassportno()+"'=null then null else N'"+stringUtilityFunction.replaceQuote(patient.getSpassportno())+"'end,"
//					+ " case when '"+patient.getSexternalid()+"'=null then null else N'"+stringUtilityFunction.replaceQuote(patient.getSexternalid())+"'end,"
//					+ " '" + getCurrentDateTime(userInfo)+"',"+ userInfo.getNtimezonecode()+","+ getCurrentDateTimeOffset(userInfo.getStimezoneid())+","
//					+ " '" + getCurrentDateTime(userInfo)+"',"+ userInfo.getNtimezonecode()+","
//					+ "  "+ getCurrentDateTimeOffset(userInfo.getStimezoneid())+","
//					+ "  "+userInfo.getNmastersitecode()+","+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
//			jdbcTemplate.execute(patientInsert);

				patientInsert = "insert into patientmaster(spatientid, sfirstname, slastname, ddob, "
						+ "sage, ngendercode, sfathername, nneedmigrant,"
						+ " ncountrycode, nregioncode, ndistrictcode, ncitycode, spostalcode,"
						+ " sstreet, shouseno, sflatno, nneedcurrentaddress, nregioncodetemp, ndistrictcodetemp,"
						+ " ncitycodetemp, spostalcodetemp, sstreettemp, shousenotemp, sflatnotemp, sphoneno, "
						+ "smobileno, semail, srefid, spassportno, sexternalid, dmodifieddate, nsitecode, nstatus,scityname,scitynametemp)"
						+ " values(N'" + stringUtilityFunction.replaceQuote(patient.getSpatientid()) + "',N'"
						+ stringUtilityFunction.replaceQuote(patient.getSfirstname()) + "'," + " N'"
						+ stringUtilityFunction.replaceQuote(patient.getSlastname()) + "','" + ddob + "',N'"
						+ stringUtilityFunction.replaceQuote(patient.getSage()) + "'," + patient.getNgendercode() + ","
						+ " case when '" + stringUtilityFunction.replaceQuote(patient.getSfathername())
						+ "'='null' then null else N'" + stringUtilityFunction.replaceQuote(patient.getSfathername())
						+ "'end," + patient.getNneedmigrant() + "," + patient.getNcountrycode() + ","
						+ patient.getNregioncode() + "," + patient.getNdistrictcode() + ", -1 ," + " case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSpostalcode()) + "'='null' then null else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSpostalcode()) + "'end," + " case when '"
						+ patient.getSstreet() + "'='null' then '' else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSstreet()) + "' end, case when '"
						+ patient.getShouseno() + "'='null' then '' else N'"
						+ stringUtilityFunction.replaceQuote(patient.getShouseno()) + "' end, case when '"
						+ patient.getSflatno() + "'='null' then '' else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSflatno()) + "' end,"
						+ patient.getNneedcurrentaddress() + "," + patient.getNregioncodetemp() + ","
						+ patient.getNdistrictcodetemp() + "," + " -1 ," + " case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSpostalcodetemp())
						+ "'='null' then null else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSpostalcodetemp()) + "'end, case when '"
						+ patient.getSstreettemp() + "'='null' then '' else " + "N'"
						+ stringUtilityFunction.replaceQuote(patient.getSstreettemp()) + "' end, case when '"
						+ patient.getShousenotemp() + "'='null' then '' else N'"
						+ stringUtilityFunction.replaceQuote(patient.getShousenotemp()) + "' end, case when '"
						+ patient.getSflatnotemp() + "'='null' then '' else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSflatnotemp()) + "' end," + " case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSphoneno()) + "'='null' then null else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSphoneno()) + "'end," + " case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSmobileno()) + "'='null' then null else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSmobileno()) + "'end," + " case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSemail()) + "'='null' then null else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSemail()) + "'end ," + " case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSrefid()) + "'='null' then null else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSrefid()) + "'end," + " case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSpassportno()) + "'='null' then null else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSpassportno()) + "'end," + " case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSexternalid()) + "'='null' then null else N'"
						+ stringUtilityFunction.replaceQuote(patient.getSexternalid()) + "'end," + " '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",N'"
						+ stringUtilityFunction.replaceQuote(patient.getScityname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(patient.getScitynametemp()) + "')";
				jdbcTemplate.execute(patientInsert);

//				patientSeq = "update seqnocontactmaster set nsequenceno=" + seqNo + " where stablename='patientmaster'";
//				jdbcTemplate.execute(patientSeq);

				returnMap.put("Patient", patient);
//				returnMap.put("patientcode", seqNo);
				returnMap.put("spatientid", patient.getSpatientid());
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				// patient.setSdob(ddob);
				patientDetails.add(patient);
			} else {
				String query = "update patientmaster set " + "sfirstname='" + patient.getSfirstname() + "',slastname= "
						+ "'" + patient.getSlastname() + "',sfathername='" + patient.getSfathername() + "'," + " ddob='"
						+ ddob + "',sage='" + patient.getSage() + "'," + " ngendercode=" + patient.getNgendercode()
						+ ",ncitycode= -1 ,ncitycodetemp= -1 ," + " ncountrycode=" + patient.getNcountrycode()
						+ " ,spostalcode='" + patient.getSpostalcode() + "',spostalcodetemp='"
						+ patient.getSpostalcodetemp() + "'," + "sstreet='" + patient.getSstreet() + "',sstreettemp='"
						+ patient.getSstreettemp() + "',shouseno='" + patient.getShouseno() + "',shousenotemp='"
						+ patient.getShousenotemp() + "'," + "sflatno='" + patient.getSflatno() + "',sflatnotemp='"
						+ patient.getSflatnotemp() + "'," + " sphoneno='" + patient.getSphoneno() + "',smobileno='"
						+ patient.getSmobileno() + "'," + " semail='" + patient.getSemail() + "',spassportno='"
						+ patient.getSpassportno() + "'," + " srefid='" + patient.getSrefid() + "',sexternalid='"
						+ patient.getSexternalid() + "',ndistrictcode=" + patient.getNdistrictcode()
						+ ",ndistrictcodetemp=" + patient.getNdistrictcodetemp() + "," + "nregioncode="
						+ patient.getNregioncode() + ",nregioncodetemp=" + patient.getNregioncodetemp()
						+ ",nneedmigrant=" + patient.getNneedmigrant() + ",nneedcurrentaddress="
						+ patient.getNneedcurrentaddress() + ", nsitecode=" + userInfo.getNmastersitecode() + ","
						+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",scityname='"
						+ patient.getScityname() + "',scitynametemp='" + patient.getScitynametemp() + "'"
						+ " where spatientid='" + patient.getSpatientid() + "'";
				jdbcTemplate.execute(query);
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			}

			auditUtilityFunction.fnInsertAuditAction(patientDetails, 1, null, Arrays.asList("IDS_ADDPATIENT"),
					userInfo);

		} else {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus());
			returnMap.put("spatientid", duplicatePatient.get(0).getSpatientid());
			// returnMap.put("Patient",duplicatePatient );
			// returnMap.put("patientcode",duplicatePatient.getNpatientcode());
		}
		return returnMap;

	}

	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> updatePatient(Patient patient, UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final List<Object> beforeSavedPatientList = new ArrayList<>();
		final List<Object> savedPatientList = new ArrayList<>();
//		if (patient.getDdob() != null) {
//			patient.setSdob(commonFunction.instantDateToString(patient.getDdob()).replace("T", " "));
//		}

		final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
		String sdob = sdFormat.format(patient.getDdob());
		patient.setSdob(new SimpleDateFormat(userInfo.getSsitedate()).format(patient.getDdob()));

		final Patient patientDetails = (Patient) getActivePatientById(patient.getSpatientid(), userInfo);

		if (patientDetails == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
//			Map<String,Object> returnMap = duplicatePatient(patient, userInfo,ddob);
//			String response = (String)returnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()); 
//			if (response.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {

			final String sQuery = "select spatientid from patientmaster where sfirstname = '"
					+ stringUtilityFunction.replaceQuote(patient.getSfirstname()) + "' and slastname='"
					+ stringUtilityFunction.replaceQuote(patient.getSlastname()) + "'"
					// + "' and sfathername='" + patient.getSfathername()+"'"
					+ "  and ddob= '" + sdob + "'" + "  and ngendercode=" + patient.getNgendercode() + " and nsitecode="
					+ userInfo.getNmastersitecode() + "  " + "and spatientid <> '" + patient.getSpatientid() + "'"
					+ "  and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<Patient> patientList = jdbcTemplate.query(sQuery, new Patient());

			if (patientList.isEmpty()) {
				/*
				 * String
				 * snationalid=patient.getSnationalid()==null?"":patient.getSnationalid();
				 */
				String concat = "";
				String spatientid = "";
				// String patientid = "";

//				if (!patient.getSfirstname().toLowerCase().equals(patientDetails.getSfirstname().toLowerCase())
//						|| !patient.getSlastname().toLowerCase().equals(patientDetails.getSlastname().toLowerCase())
//						|| !patient.getSgendername().equals(patientDetails.getSgendername())
//						|| !patient.getSdob().toLowerCase().equals(patientDetails.getSdob().toLowerCase())) {
//					concat = patient.getSfirstname() + patient.getSlastname() + patient.getSgendername()
//							+ patient.getSdob();
//					patientid = UUID.nameUUIDFromBytes(concat.getBytes()).toString();
//					spatientid = ",spatientid = '" + patientid + "'";
//				}
				String query = "update patientmaster set " + "sfirstname='"
						+ stringUtilityFunction.replaceQuote(patient.getSfirstname()) + "',slastname= " + "'"
						+ stringUtilityFunction.replaceQuote(patient.getSlastname()) + "',sfathername= case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSfathername()) + "'='null' then null else '"
						+ stringUtilityFunction.replaceQuote(patient.getSfathername()) + "' end," + " ddob='" + sdob
						+ "',sage='" + patient.getSage() + "'," + " ngendercode=" + patient.getNgendercode()
						+ ",ncitycode= -1 ,ncitycodetemp= -1 ," + " ncountrycode=" + patient.getNcountrycode()
						+ " ,spostalcode= case when '" + stringUtilityFunction.replaceQuote(patient.getSpostalcode())
						+ "'='null' then null else '" + stringUtilityFunction.replaceQuote(patient.getSpostalcode())
						+ "' end,spostalcodetemp= case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSpostalcodetemp()) + "'='null' then null else '"
						+ stringUtilityFunction.replaceQuote(patient.getSpostalcodetemp()) + "' end," + "sstreet='"
						+ stringUtilityFunction.replaceQuote(patient.getSstreet()) + "',sstreettemp='"
						+ stringUtilityFunction.replaceQuote(patient.getSstreettemp()) + "',shouseno='"
						+ stringUtilityFunction.replaceQuote(patient.getShouseno()) + "',shousenotemp='"
						+ stringUtilityFunction.replaceQuote(patient.getShousenotemp()) + "'," + "sflatno='"
						+ stringUtilityFunction.replaceQuote(patient.getSflatno()) + "',sflatnotemp='"
						+ stringUtilityFunction.replaceQuote(patient.getSflatnotemp()) + "'," + " sphoneno= case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSphoneno()) + "'='null' then null else '"
						+ stringUtilityFunction.replaceQuote(patient.getSphoneno()) + "' end,smobileno= case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSmobileno()) + "'='null' then null else '"
						+ stringUtilityFunction.replaceQuote(patient.getSmobileno()) + "' end," + " semail= case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSemail()) + "'='null' then null else '"
						+ stringUtilityFunction.replaceQuote(patient.getSemail()) + "' end,spassportno= case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSpassportno()) + "'='null' then null else '"
						+ stringUtilityFunction.replaceQuote(patient.getSpassportno()) + "' end,"
						+ " srefid= case when '" + stringUtilityFunction.replaceQuote(patient.getSrefid())
						+ "'='null' then null else '" + stringUtilityFunction.replaceQuote(patient.getSrefid())
						+ "' end,sexternalid= case when '"
						+ stringUtilityFunction.replaceQuote(patient.getSexternalid()) + "'='null' then null else '"
						+ stringUtilityFunction.replaceQuote(patient.getSrefid()) + "' end,ndistrictcode="
						+ patient.getNdistrictcode() + ",ndistrictcodetemp=" + patient.getNdistrictcodetemp() + ","
						+ "nregioncode=" + patient.getNregioncode() + ",nregioncodetemp=" + patient.getNregioncodetemp()
						+ ",nneedmigrant=" + patient.getNneedmigrant() + ",nneedcurrentaddress="
						+ patient.getNneedcurrentaddress() + ", nsitecode=" + userInfo.getNmastersitecode() + ","
						+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',scityname='"
						+ stringUtilityFunction.replaceQuote(patient.getScityname()) + "',scitynametemp='"
						+ stringUtilityFunction.replaceQuote(patient.getScitynametemp()) + "'"
						// + " " + spatientid
						+ "  where spatientid='" + patient.getSpatientid() + "' and nstatus=" + " "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(query);
//				final String ddob = patient.getSdob().toString().substring(0,patient.getSdob().length()-9 );
//				patient.setSdob(ddob);
				savedPatientList.add(patient);
				beforeSavedPatientList.add(patientDetails);

				auditUtilityFunction.fnInsertAuditAction(savedPatientList, 2, beforeSavedPatientList,
						Arrays.asList("IDS_EDITPATIENT"), userInfo);
				// return getPatient(patient.getSpatientid(), userInfo,null);

				// return getPatient(spatientid.equals("") ? patient.getSpatientid() :
				// patientid, userInfo, null,-1);
				return getPatient(patient.getSpatientid(), userInfo, null, -1);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("ALREADYEXIST", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	public ResponseEntity<Object> deletePatient(Patient patient, UserInfo userInfo) throws Exception {
		final List<Object> deletedPatientList = new ArrayList<>();
		final Patient patientById = getActivePatientById(patient.getSpatientid(), userInfo);

		if (patientById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_PATIENTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select 'IDS_SAMPLEREGISTRATION' as Msg from externalorder " + "where nsitecode="
					+ userInfo.getNmastersitecode() + "  and spatientid='" + patient.getSpatientid() + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {

				String delAuditQuery = "select pm.spatientid,pm.sfirstname,pm.nneedcurrentaddress,pm.nneedmigrant,c.ncitycode,cu.ncountrycode,g.ngendercode,"
						+ " pm.slastname,pm.sfirstname||' '||pm.slastname as spatientname,"
						+ " case when pm.sfathername = null then '-' else pm.sfathername end," + " pm.sage,pm.ddob,"
						+ " to_char(pm.ddob,'" + userInfo.getSsitedate() + "') as sdob,"
						+ " case when pm.spostalcode = 'null' then '-' else pm.spostalcode end,"
						+ " case when pm.sphoneno = 'null' then '-' else pm.sphoneno end,"
						+ " case when pm.smobileno = 'null' then '-' else pm.smobileno end,"
						+ " case when pm.semail = 'null' then '-' else pm.semail end,"
						+ " case when pm.srefid = 'null' then '-' else pm.srefid end,"
						+ " case when pm.spassportno = 'null' then '-' else pm.spassportno end,"
						+ " case when pm.sexternalid = 'null' then '-' else pm.sexternalid end,"
						+ " coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
						+ "',g.jsondata->'sgendername'->>'en-US') as sgendername," + " pm.scityname,cu.scountryname"
						+ " from patientmaster pm,gender g, city c ,country cu"
						+ " where pm.ngendercode=g.ngendercode and cu.ncountrycode=pm.ncountrycode"
						+ " and c.ncitycode=pm.ncitycode " + " and pm.nsitecode=" + userInfo.getNmastersitecode()
						+ " and g.nsitecode=" + userInfo.getNmastersitecode() + " and  c.nsitecode="
						+ userInfo.getNmastersitecode() + " and cu.nsitecode =" + userInfo.getNmastersitecode() + " "
						+ "and pm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and g.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and c.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and cu.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and pm.spatientid='" + patient.getSpatientid() + "'";

				Patient deletedAuditPatient = (Patient) jdbcUtilityFunction.queryForObject(delAuditQuery, Patient.class,
						jdbcTemplate);

				final String deleteQuery = "update patientmaster set  dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where spatientid = " + " '"
						+ patient.getSpatientid() + "';";

				jdbcTemplate.execute(deleteQuery);

				deletedPatientList.add(deletedAuditPatient);
				auditUtilityFunction.fnInsertAuditAction(deletedPatientList, 1, null,
						Arrays.asList("IDS_DELETEPATIENT"), userInfo);

				return getPatient(null, userInfo, patient.getCurrentdate(), -1);

			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}

	}

////	@SuppressWarnings("unchecked")
////	public ResponseEntity<Object> getPatientHistory(final int npatientCode, final UserInfo userInfo) throws Exception{
////		
////		final String getQuery = "select r.npreregno,rar.sarno,tgs.sspecname,p.sproductname,"
////								+ " exam.scontainertype as exam,pm.spatientid,rr.dcollectiondate,rr.dreceiveddate, "
////								+ " format(rr.dcollectiondate, '"+ userInfo.getSdatetimeformat() + "') as scollectiondate, "
////								+ " format(rr.dreceiveddate, '"+ userInfo.getSdatetimeformat() + "') as sreceiveddate, "
////								+ " sub.smanufname as submitter,doc.scontactname as doctor,ts.stransdisplaystatus,pm.npatientcode,"
////						        + " isnull(convert(nvarchar(30),( "
////						        + "     select max(t.dtransactiondate) from ( "
////						        + "         select max(rh2.dtransactiondate) dtransactiondate "
////						        + "         from registrationhistory rh2 "
////						        + "        where rh2.npreregno = r.npreregno "
////						        + "         and rh2.ntransactionstatus = " + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
////						        + "     ) t) "
////						        + "  ,120),'-') dregdate "
////								+ " from registration r,registrationarno rar,registrationroutine rr,registrationhistory rh,"
////							    + " patientmaster pm,testgroupspecification tgs,product p,containertype exam, "
////								+ " manufacturer sub,manufacturercontactinfo doc,transactionstatus ts "
////								+ " where r.npreregno = rh.npreregno "
////								+ " and rh.nreghistorycode = any ( "
////								+ "    select max(nreghistorycode) from registrationhistory where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////								+ "    group by npreregno "
////								+ " ) "
////								+ " and r.npreregno = rar.npreregno "
////								+ " and rr.npreregno = r.npreregno "
////								+ " and rh.npreregno = r.npreregno "
////								+ " and pm.npatientcode = rr.npriority "
////								+ " and tgs.nallottedspeccode = r.nallottedspeccode "
////								+ " and p.nproductcode = r.nproductcode "
////								+ " and exam.ncontainertypecode = rr.ncontainertypecode "
////								+ " and sub.nmanufcode = rr.nclientcode "
////								+ " and doc.nmanufcontactcode = rr.nperiodconfigcode "
////								+ " and ts.ntranscode = rh.ntransactionstatus "
////								+ " and r.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////								+ " and rr.nstatus = "  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////								+ " and rar.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////								+ " and pm.nstatus ="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////								+ " and exam.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////								+ " and sub.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////								+ " and doc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////								+ " and p.nstatus ="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////								+ " and tgs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////								+ " and pm.npatientcode =" + npatientCode;
////		
////		List<Map<String,Object>> list = jdbcTemplate.queryForList(getQuery);
////		
////		list = (List<Map<String, Object>>) objGeneral.getSiteLocalTimeFromUTC(list, Arrays.asList("scollectiondate", "sreceiveddate"), null, userInfo, true, Arrays.asList("stransdisplaystatus"), 
////				false);
////		
////		return new ResponseEntity<>(list, HttpStatus.OK);
////	}
//
//
//
//	@Override
//	public ResponseEntity<Object> patientReportGenerate(Map<String, Object> inputMap, UserInfo userInfo)
//			throws Exception {
//
//
//		ObjectMapper mapper = new ObjectMapper();
//		final Patient patient = mapper.convertValue(inputMap.get("patient"),
//				Patient.class);
//
//		if (patient != null) {
//			final String getPatientDetatils = "select spatientid,sfirstname+' '+slastname spatientname from patientmaster "
//					+ " where npatientcode = "+patient.getNpatientcode()
//					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//			Patient patientDetailsObj=(Patient) jdbcQueryForObject(getPatientDetatils, Patient.class);
//			if(patientDetailsObj!=null) {
//				
//				Map<String, Object> returnMap = new HashMap<>();
//				String sfileName = "";
//				String sJRXMLname = "";
//				int qType = 1;
//				int ncontrolCode = -1;
//				String sfilesharedfolder = "";
//				String fileuploadpath = "";
//				String subReportPath = "";
//				String imagePath = "";
//				String pdfPath = "";
//				final String getFileuploadpath = "select ssettingvalue from reportsettings where nreportsettingcode in ("
//						+ Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + ","
//						+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode() + ")";
//				List<String> reportPaths = jdbcTemplate.queryForList(getFileuploadpath, String.class);
//				fileuploadpath = reportPaths.get(0);
//				subReportPath = reportPaths.get(0);
//				imagePath = reportPaths.get(0);
//				pdfPath = reportPaths.get(1);
//	
//				if (inputMap.containsKey("ncontrolcode")) {
//					ncontrolCode = (int) inputMap.get("ncontrolcode");
//				}
//	
//				sJRXMLname = "PatientHistoryReport.jrxml";
//				sfileName = "PatientHistoryReport_" + patient.getNpatientcode();
//				;
//				inputMap.put("ncontrolcode", (int) inputMap.get("ncontrolcode"));
//	
//				UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
//				userInfoWithReportFormCode.setNformcode((short)Enumeration.FormCode.REPORTCONFIG.getFormCode());
//				Map<String, Object> dynamicReport = getDynamicReports(inputMap, userInfoWithReportFormCode);
//				String folderName = "";
//				if (((String) dynamicReport.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))
//						.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
//	
//					sJRXMLname = (String) dynamicReport.get("JRXMLname");
//					folderName = (String) dynamicReport.get("folderName");
//					fileuploadpath = fileuploadpath + folderName;
//				}
//	
//				sfilesharedfolder = fileuploadpath + sJRXMLname;
//				File JRXMLFile = new File(sfilesharedfolder);
//				if (sJRXMLname != null && !sJRXMLname.equals("")) {
//	
//					Map<String, Object> jasperParameter = new HashMap<>();
//					jasperParameter.put("ssubreportpath", subReportPath + folderName);
//					jasperParameter.put("simagepath", imagePath + folderName);
//					jasperParameter.put("npatientcode", patient.getNpatientcode());
//	
//					returnMap.putAll(compileAndPrintReport(jasperParameter, JRXMLFile, qType, pdfPath, sfileName, userInfo,
//							"", ncontrolCode, false));
//					String uploadStatus = (String) returnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
//					if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {
//						returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
//					//	String auditAction = "IDS_PATIENTHISREPORT";
//						
//						String comments = commonFunction.getMultilingualMessage("IDS_PATIENTID",
//								userInfo.getSlanguagefilename()) + ": " +patientDetailsObj.getSpatientid()+ "; ";
//						
//						comments += commonFunction.getMultilingualMessage("IDS_PATIENTNAME",
//								userInfo.getSlanguagefilename()) + ": " +patientDetailsObj.getSpatientname().isEmpty()+ "; ";
//						
//						comments = comments
//								+ commonFunction.getMultilingualMessage("IDS_FILENAME", userInfo.getSlanguagefilename())
//								+ ": " + returnMap.get("outputFilename") + "; ";
//	
//						Map<String, Object> outputMap = new HashMap<>();
//						outputMap.put("stablename", "patientmaster");
//						outputMap.put("sprimarykeyvalue", inputMap.get("npatientcode"));
//	
////						insertAuditAction(userInfo, auditAction, comments, outputMap);
//					}
//				} else {
//	
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOREPORTFOUNDFORSPEC",
//							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//	
//				}
//				return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
//			}else {
//				return new ResponseEntity<>(
//						commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",
//								userInfo.getSlanguagefilename()),
//						HttpStatus.EXPECTATION_FAILED);
//			}
//	
//		} else {
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage("IDS_REPORTCANNOTGENERATEFORCOMPWITHOUTTEST",
//							userInfo.getSlanguagefilename()),
//					HttpStatus.EXPECTATION_FAILED);
//		}
//	
//	}
//
	public ResponseEntity<Object> filterByPatient(String filterByPatientQueryBuilder, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String scollate = "collate \"default\"";

		if (filterByPatientQueryBuilder.contains("LIKE")) {

			while (filterByPatientQueryBuilder.contains("LIKE")) {
				String sb = filterByPatientQueryBuilder;
				String sQuery = filterByPatientQueryBuilder;
				int colanindex = sb.indexOf("LIKE '");
				String str1 = sQuery.substring(0, colanindex + 6);
				sQuery = sQuery.substring(colanindex + 6);
				StringBuilder sb3 = new StringBuilder(str1);
				StringBuilder sb4 = new StringBuilder(sQuery);
				sb3.replace(colanindex, colanindex + 4, "ilike");
//				System.out.println(sQuery);
				int indexofsv = sQuery.indexOf("%'");

				sb4.replace(indexofsv, indexofsv + 2, "%'" + scollate + " ");
				filterByPatientQueryBuilder = sb3.toString() + sb4.toString();
			}

//			  filterByPatient= filterByPatientQueryBuilder.replace("LIKE","ilike");
//			  scollate="collate \"default\"";
//			  if(filterByPatientQueryBuilder.contains("AND")) {
//			  String[] splits = filterByPatientQueryBuilder.split("AND");
//			  scollate="collate \"default\"";
//			  }
//			  if(filterByPatientQueryBuilder.contains("OR")) {
//				  String[] splits = filterByPatientQueryBuilder.split("OR");
//				  scollate="collate \"default\"";  
//			  }

		}
//		}else {
//			sPatientFilter = "regh.dtransactiondate between '"+fromdate+"' and '"+todate+"'";
//		}

		// AT-E241 the below logic is to handle singlequote (\\') and its consequence.
		// Example=> conditionString: firstname='\\''
		// start
		// AT-E241 for handling backsplash (\\\\) Example=> conditionString:
		// firstname='\\\\' since the later logic replaces \\' to ''
		if (filterByPatientQueryBuilder.contains("\\\\")) {
			filterByPatientQueryBuilder = filterByPatientQueryBuilder.replace("\\\\", "#LiMsBaCkSlAsH#");
		}
		if (filterByPatientQueryBuilder.contains("\\'")) {
			filterByPatientQueryBuilder = filterByPatientQueryBuilder.replace("\\'", "''");
		}
		if (filterByPatientQueryBuilder.contains("#LiMsBaCkSlAsH#")) {
			filterByPatientQueryBuilder = filterByPatientQueryBuilder.replace("#LiMsBaCkSlAsH#", "\\\\");
		}
		// end

		final String strQuery = "select patientmaster.*, CONCAT( patientmaster.sfirstname , ' ' ,patientmaster.slastname) as spatientname, TO_CHAR(patientmaster.ddob,'"
				+ userInfo.getSsitedate() + "') as sdob, " + " coalesce(g.jsondata->'sgendername'->>'"
				+ userInfo.getSlanguagetypecode() + "',g.jsondata->'sgendername'->>'en-US') as sgendername,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus ,coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts1.jsondata->'stransdisplaystatus'->>'en-US') as scurrentstatus ,"
				+ " patientmaster.scityname,cu.scountryname,r.sregionname,d.sdistrictname ,r1.sregionname as sregionnametemp ,d1.sdistrictname as sdistrictnametemp,patientmaster.scitynametemp from transactionstatus ts,transactionstatus ts1 ,patientmaster patientmaster, "
				+ " gender g , city c,country cu,region r,district d,region r1,district d1,city c1  where patientmaster.ngendercode=g.ngendercode "
				+ " and cu.ncountrycode= patientmaster.ncountrycode and ts.ntranscode=patientmaster.nneedmigrant and ts1.ntranscode=patientmaster.nneedcurrentaddress "
				+ " and patientmaster.ncitycode=c.ncitycode and r.nregioncode =patientmaster.nregioncode"
				+ " and d.ndistrictcode =patientmaster.ndistrictcode and  patientmaster.ncitycode=c1.ncitycode "
				+ "and r1.nregioncode =patientmaster.nregioncode "
				+ "and d1.ndistrictcode =patientmaster.ndistrictcode and patientmaster.nsitecode= "
				+ userInfo.getNmastersitecode() + " " + "and g.nsitecode=" + userInfo.getNmastersitecode()
				+ "  and  c.nsitecode=" + userInfo.getNmastersitecode() + " " + "and c1.nsitecode ="
				+ userInfo.getNmastersitecode() + "  and  cu.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ "and r1.nsitecode=" + userInfo.getNmastersitecode() + "  and r.nsitecode="
				+ userInfo.getNmastersitecode() + " " + "and  d1.nsitecode =" + userInfo.getNmastersitecode()
				+ " and  d.nsitecode =" + userInfo.getNmastersitecode() + " " + " and patientmaster.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cu.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and g.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + filterByPatientQueryBuilder
				+ " order by patientmaster.dmodifieddate desc ";
		final List<Patient> patientList = jdbcTemplate.query(strQuery, new Patient());

		String caseType = "select jsondata, npatientcasetypecode, coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',jsondata->'sdisplayname'->>'en-US') as spatientcasetypename "
				+ "from patientcasetype where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ TransactionStatus.ACTIVE.gettransactionstatus();
		final List<PatientCaseType> patientCaseType = jdbcTemplate.query(caseType, new PatientCaseType());
		outputMap.put("SelectedPatientCaseType", patientCaseType.get(0));

		if (patientList.isEmpty()) {
			outputMap.put("PatientList", patientList);
			// outputMap.put("filterFromdate", fromDate);
			// outputMap.put("filterToDay", toDate);
			outputMap.put("SelectedPatient", null);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			// Patient selectedPatient = patientList.get(patientList.size() - 1);
			Patient selectedPatient = patientList.get(0);
			outputMap.put("SelectedPatient", selectedPatient);
			outputMap.put("PatientList", patientList);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		}

	}

	public ResponseEntity<Object> getDistrict(final int nregioncode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String query = "select  * from district where nsitecode=" + userInfo.getNmastersitecode() + " "
				+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregioncode ="
				+ nregioncode;
		outputMap.put("districtList", jdbcTemplate.query(query, new District()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getCity(final int ndistrictcode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String query = "select  * from city  where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndistrictcode =" + ndistrictcode;
		outputMap.put("cityList", jdbcTemplate.query(query, new City()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getFilterByDate(String fromdate, String todate, final UserInfo userInfo,
			String casetype) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		String fromDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromdate, userInfo, true));
		String toDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(todate, userInfo, true));
		String strQuery = "";
		if (casetype.equals("Follow up")) {
//			strQuery = "select  reg.npreregno,patientmaster.*, CONCAT( patientmaster.sfirstname , ' ' ,patientmaster.slastname) as spatientname, TO_CHAR(patientmaster.ddob,'"
//					+ userInfo.getSsitedate() + "') as sdob, " + " coalesce(g.jsondata->'sgendername'->>'"
//					+ userInfo.getSlanguagetypecode() + "',g.jsondata->'sgendername'->>'en-US') as sgendername,"
//					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
//					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus ,coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
//					+ userInfo.getSlanguagetypecode() + "',"
//					+ "ts1.jsondata->'stransdisplaystatus'->>'en-US') as scurrentstatus ,patientmaster.spostalcode,patientmaster.spostalcodetemp,"
//					+ " patientmaster.scityname,cu.scountryname,r.sregionname,d.sdistrictname ,r1.sregionname as sregionnametemp ,d1.sdistrictname as sdistrictnametemp,patientmaster.scitynametemp,"
//					+ "to_char(patientmaster.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate from transactionstatus ts,transactionstatus ts1 ,patientmaster patientmaster, "
//					+ " gender g , city c,country cu,region r,district d,region r1,district d1,city c1 ,registration reg,registrationhistory regh where patientmaster.ngendercode=g.ngendercode "
//					+ " and cu.ncountrycode= patientmaster.ncountrycode and ts.ntranscode=patientmaster.nneedmigrant and ts1.ntranscode=patientmaster.nneedcurrentaddress "
//					+ " and patientmaster.ncitycode=c.ncitycode and r.nregioncode =patientmaster.nregioncode"
//					+ " and d.ndistrictcode =patientmaster.ndistrictcode and  patientmaster.ncitycode=c1.ncitycode and r1.nregioncode =patientmaster.nregioncode and d1.ndistrictcode =patientmaster.ndistrictcode  "
//					+ " and patientmaster.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and c.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and cu.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and reg.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and regh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and reg.npreregno=regh.npreregno and  regh.ntransactionstatus="
//					+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
//					+ " and  reg.jsonuidata->>'spatientid'= patientmaster.spatientid and (regh.dtransactiondate between '"
//					+ fromDate + "' and '" + toDate + "')" + " and reg.npreregno= any"
//					+ "(select max( reg.npreregno ) from registration reg group by reg.jsonuidata->>'spatientid' ) order by patientmaster.dmodifieddate desc ";

			// ALPD-3805 to avoid duplicate patient data for patient case type its follow up
			strQuery = "select " + "  max(reg.npreregno) as npreregno, "
					+ "  max(patientmaster.spatientid) as spatientid,"
					+ "  max(patientmaster.sfirstname) as sfirstname," + "  max(patientmaster.slastname) as slastname, "
					+ "  max(patientmaster.ddob) as ddob," + "  max(patientmaster.sage) as sage,"
					+ "  max(patientmaster.ngendercode) as ngendercode,"
					+ "  max(patientmaster.sfathername) as sfathername,"
					+ "  max(patientmaster.nneedmigrant) as nneedmigrant,"
					+ "  max(patientmaster.ncountrycode) as ncountrycode,"
					+ "  max(patientmaster.nregioncode) as nregioncode,"
					+ "  max(patientmaster.ndistrictcode) as ndistrictcode,"
					+ "  max(patientmaster.ncitycode) as ncitycode," + "  max(patientmaster.scityname) as scityname,"
					+ "  max(patientmaster.spostalcode) as spostalcode, " + "  max(patientmaster.sstreet) as sstreet,"
					+ "  max(patientmaster.shouseno) as shouseno," + "  max(patientmaster.sflatno) as sflatno,"
					+ "  max(patientmaster.nneedcurrentaddress) as nneedcurrentaddress,"
					+ "  max(patientmaster.nregioncodetemp) as nregioncodetemp,"
					+ "  max(patientmaster.ndistrictcodetemp) as ndistrictcodetemp,"
					+ "  max(patientmaster.ncitycodetemp) as ncitycodetemp,"
					+ "  max(patientmaster.scitynametemp) as scitynametemp,"
					+ "  max(patientmaster.spostalcodetemp) as spostalcodetemp,"
					+ "  max(patientmaster.sstreettemp) as sstreettemp,"
					+ "  max(patientmaster.shousenotemp) as shousenotemp,"
					+ "  max(patientmaster.sflatnotemp) as sflatnotemp," + "  max(patientmaster.sphoneno) as sphoneno,"
					+ "  max(patientmaster.smobileno) as smobileno," + "  max(patientmaster.semail) as semail,"
					+ "  max(patientmaster.srefid) as srefid," + "  max(patientmaster.spassportno) as spassportno,"
					+ "  max(patientmaster.sexternalid) as sexternalid,"
					+ "  max(patientmaster.dmodifieddate) as dmodifieddate,"
					+ "  max(patientmaster.nsitecode) as nsitecode," + "  max(patientmaster.nstatus) as nstatus,"
					+ "  max(CONCAT(patientmaster.sfirstname, ' ',patientmaster.slastname)) as spatientname, "
					+ "  max(TO_CHAR(patientmaster.ddob, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
					+ "')) as sdob, " + "  max(coalesce(g.jsondata -> 'sgendername' ->> '"
					+ userInfo.getSlanguagetypecode() + "',g.jsondata -> 'sgendername' ->> 'en-US' )) as sgendername, "
					+ "  max(coalesce(ts.jsondata -> 'stransdisplaystatus' ->> '" + userInfo.getSlanguagetypecode()
					+ "', ts.jsondata -> 'stransdisplaystatus' ->> 'en-US'))  as sdisplaystatus, "
					+ "  max(coalesce(ts1.jsondata -> 'stransdisplaystatus' ->> '" + userInfo.getSlanguagetypecode()
					+ "',ts1.jsondata -> 'stransdisplaystatus' ->> 'en-US'))  as scurrentstatus, "
					+ "  max(cu.scountryname) as scountryname, " + "  max( r.sregionname) as sregionname, "
					+ "  max( d.sdistrictname) as sdistrictname, " + "  max(r1.sregionname)  as sregionnametemp, "
					+ "  max(d1.sdistrictname)  as sdistrictnametemp, " + "  max(to_char(patientmaster.dmodifieddate, '"
					+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "')) as smodifieddate " + "  from "
					+ "  transactionstatus ts, transactionstatus ts1, patientmaster patientmaster, gender g, "
					+ "  city c, country cu, region r, district d, region r1, district d1, city c1, "
					+ "  registration reg, registrationhistory regh " + "  where "
					+ "  patientmaster.ngendercode = g.ngendercode "
					+ "  and cu.ncountrycode = patientmaster.ncountrycode "
					+ "  and ts.ntranscode = patientmaster.nneedmigrant "
					+ "  and ts1.ntranscode = patientmaster.nneedcurrentaddress "
					+ "  and patientmaster.ncitycode = c.ncitycode "
					+ "  and r.nregioncode = patientmaster.nregioncode "
					+ "  and d.ndistrictcode = patientmaster.ndistrictcode "
					+ "  and patientmaster.ncitycode = c1.ncitycode "
					+ "  and r1.nregioncode = patientmaster.nregioncode "
					+ "  and d1.ndistrictcode = patientmaster.ndistrictcode " + "  and patientmaster.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  and c.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  and cu.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  and g.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  and reg.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  and regh.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "  and reg.npreregno = regh.npreregno " + "  and regh.ntransactionstatus = "
					+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
					+ "  and reg.jsonuidata ->> 'spatientid' = patientmaster.spatientid "
					+ "  and (regh.dtransactiondate between  '" + fromDate + "' and '" + toDate + "' ) "
					+ "  and reg.npreregno = any(select max(reg.npreregno) from registration reg group by reg.jsonuidata ->> 'spatientid' )"
					+ "  group by  reg.jsonuidata ->> 'spatientid',patientmaster.dmodifieddate"
					+ "  order by   patientmaster.dmodifieddate desc";

		} else {
			strQuery = "select pm.spatientid,pm.sfirstname||' '||pm.slastname as spatientname,"
					+ " pm.sfathername,pm.sage,pm.ddob,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus ," + " to_char(pm.ddob,'"
					+ userInfo.getSsitedate() + "') as sdob," + " pm.spostalcode,pm.sphoneno,pm.smobileno,pm.semail,"
					+ " pm.srefid,pm.spassportno,pm.sexternalid,pm.ndistrictcodetemp,pm.ncitycodetemp,pm.nregioncode,pm.nregioncodetemp,"
					+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US') as scurrentaddress,"
					+ " coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
					+ "',g.jsondata->'sgendername'->>'en-US') as sgendername,"
					+ " pm.scityname,cu.scountryname ,r.sregionname,r1.sregionname as sregionnametemp,"
					+ "d.sdistrictname,d1.sdistrictname as sdistrictnametemp ,pm.scitynametemp,pm.sstreet,pm.sstreettemp,pm.shouseno,pm.shousenotemp,"
					+ "pm.sflatno,pm.sflatnotemp,pm.ncitycode,pm.nregioncode,pm.ndistrictcode,pm.spostalcodetemp,"
					+ "to_char(pm.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
					+ "') as smodifieddate  from patientmaster pm,gender g, city c ,city c1,country cu ,region r1, region r,district d1,district d,"
					+ "transactionstatus ts,transactionstatus ts1 "
					+ " where pm.ngendercode=g.ngendercode and cu.ncountrycode=pm.ncountrycode"
					+ " and c.ncitycode=pm.ncitycode and r.nregioncode=pm.nregioncode and r1.nregioncode=pm.nregioncodetemp and "
					+ " d.ndistrictcode=pm.ndistrictcode and d1.ndistrictcode=pm.ndistrictcodetemp and "
					+ "  c1.ncitycode=pm.ncitycodetemp and ts.ntranscode=pm.nneedmigrant  "
					+ "and ts1.ntranscode=pm.nneedcurrentaddress and  pm.nsitecode=" + userInfo.getNmastersitecode()
					+ " and (pm.dmodifieddate between '" + fromDate + "' and '" + toDate + "')" + ""
					+ "and pm.nsitecode= " + userInfo.getNmastersitecode() + " and g.nsitecode="
					+ userInfo.getNmastersitecode() + "  " + "and  c.nsitecode=" + userInfo.getNmastersitecode()
					+ " and c1.nsitecode =" + userInfo.getNmastersitecode() + "  and  cu.nsitecode="
					+ userInfo.getNmastersitecode() + " and r1.nsitecode=" + userInfo.getNmastersitecode()
					+ "  and r.nsitecode=" + userInfo.getNmastersitecode() + " " + "and  d1.nsitecode ="
					+ userInfo.getNmastersitecode() + " and  d.nsitecode =" + userInfo.getNmastersitecode()
					+ " and pm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and g.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and c.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and d.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and cu.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by pm.dmodifieddate desc ";

		}

		final List<Patient> patientList = jdbcTemplate.query(strQuery, new Patient());

		String retFromDate = LocalDateTime.parse(fromdate, dbPattern).format(uiPattern);
		String retToDate = LocalDateTime.parse(todate, dbPattern).format(uiPattern);

		if (patientList.isEmpty()) {
			outputMap.put("PatientList", patientList);
			outputMap.put("SelectedPatient", null);
			outputMap.put("filterFromdate", retFromDate);
			outputMap.put("filterToDay", retToDate);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			Patient selectedPatient = patientList.get(0);
			outputMap.put("SelectedPatient", selectedPatient);
			outputMap.put("PatientList", patientList);
			outputMap.put("filterFromdate", retFromDate);
			outputMap.put("filterToDay", retToDate);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		}
	}

	@Override
	public ResponseEntity<Object> createFilterQuery(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {
		// Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		// final ObjectMapper objmapper = new ObjectMapper();
		int nsequenceno;
		String patientName = inputMap.get("patient").toString();
		if (inputMap.get("nfilterstatus").equals(4)) {
			final String query = "select count(*) from patientfilter where nfilterstatus= "
					+ inputMap.get("nfilterstatus") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int ncount = (int) jdbcUtilityFunction.queryForObject(query, Integer.class, jdbcTemplate);
			if (ncount >= 5) {
				final String querys = "update patientfilter set nstatus = -1 where nfilterstatus=4 ";
//        		  		+ "npatientfiltercode=("
//        		  		+ "select npatientfiltercode from patientfilter where   nstatus=1 and nfilterstatus=4  order by dmodifieddate limit 1)";
				jdbcTemplate.execute(querys);
//        		  final String strquery = "select count(*) from patientfilter where nfilterstatus= "+ inputMap.get("filterstatus")+ " and nstatus="
//   					   + Enumeration.TransactionStatus.DELETED.gettransactionstatus();
//           	      ncount =jdbcTemplate.queryForObject(strquery,Integer.class);
				ncount = 0;
			}
			patientName = patientName + ++ncount;

		}
		String strcheck = "";
		strcheck = " select npatientfiltercode  from patientfilter   where spatientfiltername=lower('"
				+ stringUtilityFunction.replaceQuote(patientName) + "')  and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Patient ObjMatNamecheck = (Patient) jdbcUtilityFunction.queryForObject(strcheck, Patient.class,
				jdbcTemplate);
		if (ObjMatNamecheck == null) {
			String sequencenoquery = "select nsequenceno from seqnocontactmaster where stablename ='patientfilter'";
			nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;

			JSONObject language = new JSONObject();
			String query = "Select slanguagetypecode from language where nstatus=1";
			List<Language> languageList;
			languageList = jdbcTemplate.query(query, new Language());
			for (int i = 0; i < languageList.size(); i++) {
				language.put(languageList.get(i).getSlanguagetypecode(), patientName);
			}

			JSONObject templateData = new JSONObject();

			templateData.put("patientfilter", inputMap.get("patientfilter"));
			templateData.put("spatientfiltername", language);

			String insmat = " INSERT INTO patientfilter	( npatientfiltercode,spatientfiltername,jsondata,nfilterstatus,sfilterstatus,dmodifieddate,nsitecode, nstatus)"
					+ " VALUES (" + nsequenceno + " ,N'" + stringUtilityFunction.replaceQuote(patientName) + "' ,'"
					+ stringUtilityFunction.replaceQuote(templateData.toString()) + "',"
					+ (int) inputMap.get("nfilterstatus") + ",N'"
					+ stringUtilityFunction.replaceQuote(inputMap.get("sfilterstatus").toString()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + objUserInfo.getNmastersitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			jdbcTemplate.execute(insmat);
			String updatequery = "update seqnocontactmaster set nsequenceno =" + nsequenceno
					+ " where stablename='patientfilter'";
			jdbcTemplate.execute(updatequery);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS", objUserInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		return getFilterQuery(nsequenceno, objUserInfo);
	}

	public ResponseEntity<Object> getFilterQueryList(final int nfilterstatus, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String query = "select  npatientfiltercode, jsondata->'spatientfiltername'->>'"
				+ objUserInfo.getSlanguagetypecode() + "' spatientfiltername,nfilterstatus,sfilterstatus ,"
				+ "jsondata->'patientfilter'->>'tree' as tree,jsondata->'patientfilter'->'config' as config,jsondata->'patientfilter'->>'filterquery' as filterquery from patientfilter "
				+ "where nsitecode=" + objUserInfo.getNmastersitecode() + " and nfilterstatus=" + nfilterstatus
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by dmodifieddate";
		final List<Patient> patientfilterType = jdbcTemplate.query(query, new Patient());
		if (patientfilterType.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NORECENTQUERYS", objUserInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			outputMap.put("patientFilterType", patientfilterType);
			outputMap.put("SelectedPatientFilterType", patientfilterType.get(0));
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getFilterQuery(int npatientfiltercode, UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String query = "select npatientfiltercode,spatientfiltername,jsondata->'patientfilter'->>'tree' as tree,jsondata->'patientfilter'->'config' as config,jsondata->'patientfilter'->>'filterquery' as filterquery  from "
				+ " patientfilter where nsitecode=" + objUserInfo.getNmastersitecode() + "  and npatientfiltercode="
				+ npatientfiltercode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<Patient> patientfilterType = jdbcTemplate.query(query, new Patient());
		outputMap.put("patientFilterType", patientfilterType);
		outputMap.put("SelectedPatientFilterType", patientfilterType.get(0));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

//	public ResponseEntity<Object> getPatientHistory(String spatientid, UserInfo objUserInfo) throws Exception {
//		final Map<String, Object> outputMap = new HashMap<>();
//		ObjectMapper objMapper = new ObjectMapper();
//		String query = "select cp.sreportno,regh.noffsetdtransactiondate,reg.npreregno, ra.sarno,rsa.ssamplearno,(select to_char(dtransactiondate,'"
//				+ objUserInfo.getSdatetimeformat() + "')  from registrationhistory where npreregno=reg.npreregno and ntransactionstatus = "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+") as stransactiondate "
//				+ " from registrationarno ra,  registration reg,registrationhistory regh,registrationsamplearno rsa,registrationsamplehistory rsh,coachild cc,coaparent cp "
//				+ " where reg.npreregno=regh.npreregno  " + " and reg.jsonuidata->>'spatientid'= '" + spatientid + "'"
//				+ " and reg.npreregno=ra.npreregno " + " and reg.npreregno=rsa.npreregno and cc.npreregno=reg.npreregno and cc.npreregno=rsa.npreregno "
//			    + " and cc.ntransactionsamplecode=rsh.ntransactionsamplecode and cc.ncoachildcode = any(select max(rh.ncoachildcode) from coachild rh where rh.nstatus=1 group by rh.npreregno )"
//				+ " and rsh.npreregno=reg.npreregno " + " and  cc.ncoaparentcode=cp.ncoaparentcode and rsh.ntransactionstatus ="
//				+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " and regh.ntransactionstatus ="
//				+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " and ra.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and reg.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and regh.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsa.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsh.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and regh.nreghistorycode = any"
//				+ "(select max(rh.nreghistorycode) from registrationhistory rh where rh.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by rh.npreregno )";
//		List<?> patientfilterType = jdbcTemplate.queryForList(query);
//		// outputMap.put("patientHistory", patientfilterType);
//
//		List<Patient> lstUTCConvertedDate = objMapper.convertValue(
//				objGeneral.getSiteLocalTimeFromUTC(patientfilterType, Arrays.asList("stransactiondate"),
//						Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null, false),
//				new TypeReference<List<Patient>>() {
//				});
//		outputMap.put("patientHistory", lstUTCConvertedDate);
//		if (patientfilterType.size() != 0) {
//			String ntransactiontestcode = "(select String_agg(rt.ntransactiontestcode::text,',') from registrationarno ra,registration reg,registrationhistory regh,registrationsamplearno rsa,"
//					+ "registrationsamplehistory rsh,registrationtest rt where reg.npreregno=regh.npreregno and reg.jsonuidata->>'spatientid'= '"
//					+ spatientid + "'"
//					+ " and reg.npreregno=ra.npreregno and reg.npreregno=rsa.npreregno and rt.npreregno=reg.npreregno "
//					+ " and rsh.ntransactionstatus =" + Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
//					+ " and regh.ntransactionstatus =" + Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
//					+ " and ra.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and reg.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and regh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and rsa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and rsh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and regh.nreghistorycode = any"
//					+ "(select max(rh.nreghistorycode) from registrationhistory rh where rh.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by rh.npreregno ))";
//			final String querys = "select * from fn_registrationparameterget (" + ntransactiontestcode + ",'"
//					+ objUserInfo.getSsitedatetime() + "' ," + objUserInfo.getNtranssitecode() + "::integer,'" + objUserInfo.getSlanguagetypecode() + "'," + "'"
//					+ commonFunction.getMultilingualMessage("IDS_REGNO", objUserInfo.getSlanguagefilename()) + "')";
//			List<?> lstSp = jdbcTemplate.queryForList(querys);
//			outputMap.put("patientHistorydetails", lstSp);
//		} else {
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage("IDS_HISTORYDETAILSNOTAVAILABE", objUserInfo.getSlanguagefilename()),
//					HttpStatus.EXPECTATION_FAILED);
//
//		}
////		 logger.info(querys);
////		// Map<String, Object> returnMap = new HashMap<>();
////			ObjectMapper objmapper = new ObjectMapper();
////			List<Map<String,Object>> testList = new ArrayList<Map<String,Object>>();
////			String testListString = jdbcTemplate.queryForObject(querys, String.class);
////			testList = objmapper.readValue(testListString,new TypeReference<List<Map<String, Object>>>() {	});
////			Map<Object, List<Map<String, Object>>> subSampleMap=testList.stream().collect(Collectors.groupingBy(item->item.get("ntransactionsamplecode")));
////			outputMap.put("ReleaseTest", subSampleMap);
//
//		// }
////		final String str="select a.*,b.*  from (select * from fn_registrationparameterget (( select String_agg(rt.ntransactiontestcode::text,',') from registrationarno ra, "
////				+ "registration reg,registrationhistory regh,registrationsamplearno rsa,registrationtest rt where reg.npreregno=regh.npreregno "
////				+ "and reg.jsonuidata->>'Patient Id'= '"+spatientid+"' and reg.npreregno=ra.npreregno  and reg.npreregno=rsa.npreregno and reg.npreregno=rt.npreregno"
////				+ " and ra.nstatus=1 and reg.nstatus=1 and regh.nstatus=1 and rsa.nstatus=1 and rt.nstatus=1 and "
////				+ "regh.nreghistorycode = any(select max(rh.nreghistorycode) from registrationhistory rh where rh.nstatus=1 group by rh.npreregno )),'"
////				+  objUserInfo.getSsitedatetime() + "',"
////				+"'" + objUserInfo.getSlanguagetypecode() + "',"
////				+ "'"+commonFunction.getMultilingualMessage("IDS_REGNO", objUserInfo.getSlanguagefilename())+"'))a,"
////				+ "(select rt.ntransactiontestcode,reg.npreregno,to_char(regh.dtransactiondate,'"+ objUserInfo.getSdatetimeformat() + "') as stransactiondate,"
////				+ " ra.sarno as Arno,rsa.ssamplearno as Sampleorno, regh.ntransactionstatus,regh.dtransactiondate,"
////				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+ objUserInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus"
////				+ " from registrationarno ra,  registration reg,registrationhistory regh,registrationsamplearno rsa,registrationtest rt,"
////				+ " transactionstatus ts where reg.npreregno=regh.npreregno  "
////				+ " and reg.jsonuidata->>'Patient Id'= '"+spatientid+"'"
////				+ " and reg.npreregno=ra.npreregno "
////				+ " and regh.ntransactionstatus = ts.ntranscode"
////				+ " and reg.npreregno=rsa.npreregno"
////				+ " and reg.npreregno=rt.npreregno "
////				+ " and ra.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////				+ " and reg.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////				+ " and regh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////				+ " and rsa.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////				+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////				+ " and regh.nreghistorycode = any"
////				+ "(select max(rh.nreghistorycode) from registrationhistory rh where rh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by rh.npreregno) )b"
////				+ " where a.ntransactiontestcode =b.ntransactiontestcode ";
////		List<?> lstSsp = jdbcTemplate.queryForList(str);
////		outputMap.put("patientHistorydetails", lstSsp);
////		String query ="select rt.ntransactiontestcode,reg.npreregno, ra.sarno,rsa.ssamplearno, regh.ntransactionstatus,regh.dtransactiondate,"
////				+ "ap.jsondata->>'stestsynonym' as testname,ap.jsondata->>'sparametersynonym' as parametername, ap.jsondata->>'sresult' as result,g.sgradename"
////				+" from registrationarno ra,  registration reg,registrationhistory regh,registrationsamplearno rsa,"
////				+ "registrationtest rt,registrationsamplehistory rsh,approvalparameter ap,grade g"
////				+" where reg.npreregno=regh.npreregno  "
////				+" and reg.jsonuidata->>'Patient Id'= '"+spatientid+"'"
////				+" and reg.npreregno=ra.npreregno " 
////				+" and reg.npreregno=rsa.npreregno"
////				+" and reg.npreregno=rt.npreregno "
////				+ " and regh.ntransactionstatus ="+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
////				+ " and ap.ntransactiontestcode=rt.ntransactiontestcode and ap.ngradecode=g.ngradecode "
////				+ " and rsh.ntransactionstatus ="+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
////				+ " and ra.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////				+ " and reg.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////				+ " and regh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////				+ " and rsa.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////				+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
////				+" and regh.nreghistorycode = any"
////				+"(select max(rh.nreghistorycode) from registrationhistory rh where rh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by rh.npreregno )";
////		final List<Map<String, Object>> patientfilterType=jdbcTemplate.queryForList(query);
////		outputMap.put("patientHistorydetails", patientfilterType);
//		return new ResponseEntity<>(outputMap, HttpStatus.OK);
//	}
	public ResponseEntity<Object> getPatientHistory(String spatientid, UserInfo objUserInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		String strPatientHistoryParent = "select ph.npreregno, ra.sarno, rsa.ssamplearno, cp.sreportno,"
//				+ " ph.ntransactiontestcode,"
				+ " ph.spatientid, max(to_char(ph.dtransactiondate,'"
				+ objUserInfo.getSpgsitedatetime().replace("'T'", " ") + "')) stransactiondate,"
				+ " ph.ncoaparentcode from patienthistory ph, registrationarno ra, registrationsamplearno rsa, coaparent cp where "
				+ " ph.npreregno=ra.npreregno and rsa.ntransactionsamplecode=ph.ntransactionsamplecode and "
				+ " cp.ncoaparentcode=ph.ncoaparentcode and ph.spatientid='" + spatientid + "' and ph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ra.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by  ph.ncoaparentcode, ph.npreregno,ra.sarno,rsa.ssamplearno, cp.sreportno, ph.spatientid ";
//				+ ", to_char(ph.dtransactiondate, '"
//				+ objUserInfo.getSpgsitedatetime().replace("'T'", " ")+"')";
		List<Map<String, Object>> lstPatientHistoryParent = jdbcTemplate.queryForList(strPatientHistoryParent);

		String strPatientHistoryChild = "select  ph.nversionno,ph.ncoaparentcode, cp.sreportno, ph.npatienthistorycode,arr->>'sarno' sarno, arr->>'ssamplearno' ssamplearno,"
				+ " arr->>'sresult' sresult, arr->>'sfinal' sfinal, arr jsondata, ph.spatientid, arr->>'stestsynonym' stestsynonym,"
				+ " arr->>'sparametersynonym' sparametersynonym, ph.npreregno, ph.ncoaparentcode, ph.ntransactiontestcode,"
				+ " ph.ntransactionsamplecode, ph.npatienthistorycode, ph.ncoachildcode, arr->>'sgradename' sgradename from"
				+ " patienthistory ph, coaparent cp, jsonb_array_elements(ph.jsondata) arr where ph.spatientid='"
				+ spatientid + "' and cp.ncoaparentcode=ph.ncoaparentcode and ph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ph.dtransactiondate in (select max(" + "dtransactiondate) from patienthistory where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ntransactiontestcode);";
//		List<Map<String,Object>> lstPatientHistoryChild = jdbcTemplate.queryForList(strPatientHistoryChild);
		List<PatientHistory> lstPatientHistoryChildValues = jdbcTemplate.query(strPatientHistoryChild,
				new PatientHistory());

		Map<Integer, List<PatientHistory>> lstPatientHistoryChild = lstPatientHistoryChildValues.stream()
				.collect(Collectors.groupingBy(PatientHistory::getNcoaparentcode));

		List<PatientHistory> lstUTCConvertedDateParent = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(lstPatientHistoryParent, Arrays.asList("stransactiondate"),
						Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null, false),
				new TypeReference<List<PatientHistory>>() {
				});

		outputMap.put("patientHistory", lstUTCConvertedDateParent);
		outputMap.put("patientHistorydetails", lstPatientHistoryChild);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getPatientReportHistory(String spatientid, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final String strPatientReports = "select max(cp.sreportno) sreportno , TO_CHAR(max(cph.dgenerateddate) ,'"
				+ userInfo.getSpgdatetimeformat()
				+ " ') sreleasedate , max(cph.ssystemfilename) as ssystemfilename,max(ra.sarno) as sarno from patienthistory ph,coareporthistory cph ,coaparent cp ,registrationarno ra "
				+ " where ph.ncoaparentcode=cph.ncoaparentcode  and cp.ncoaparentcode=cph.ncoaparentcode and ra.npreregno=ph.npreregno and  "
				+ " cph.nversionno=(select max(nversionno) from coareporthistory cc where cc.ncoaparentcode=cp.ncoaparentcode and cc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by cc.ncoaparentcode )"
				+ " and ph.spatientid='" + spatientid + "' and ph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and cph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ra.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ph.ncoaparentcode ";
		List<Map<String, Object>> lstReleaseAttachment = jdbcTemplate.queryForList(strPatientReports);

		final List<COAReportHistory> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(lstReleaseAttachment, Arrays.asList("sreleasedate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<COAReportHistory>>() {
				});
		returnMap.put("PatientReports", lstUTCConvertedDate);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> viewPatientReportHistory(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		final List<Object> savedTestList = new ArrayList<>();
		final int ncontrolCode = (int) inputMap.get("ncontrolCode");
		if (inputMap.containsKey("ssystemfilename")) {
			map = ftpUtilityFunction.FileViewUsingFtp(inputMap.get("ssystemfilename").toString(), ncontrolCode,
					userInfo, "", "");
			COAReportHistory reportHistory = new COAReportHistory();
			reportHistory.setSreportno(inputMap.get("sreportno").toString());
			savedTestList.add(reportHistory);
			;
			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, Arrays.asList("IDS_VIEWPATIENTREPORT"),
					userInfo);

		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updatePatientID(UserInfo userInfo) throws Exception {

		String query = " lock  table patientmaster " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(query);

		query = " create table patientmastercopy as select * from patientmaster";

		jdbcTemplate.execute(query);

		query = " create table patienthistorycopy as select * from patienthistory";

		jdbcTemplate.execute(query);

		query = "CREATE TABLE IF NOT EXISTS public.patientmastertemp " + "( "
				+ "    spatientid character varying(50) COLLATE public.collate_ci NOT NULL,"
				+ "  spatientidold character varying(50) COLLATE public.collate_ci NOT NULL,"
				+ "    sfirstname character varying(100) COLLATE public.collate_ci NOT NULL,"
				+ "    slastname character varying(100) COLLATE public.collate_ci NOT NULL,"
				+ "    ddob timestamp without time zone NOT NULL,"
				+ "    sage character varying(50) COLLATE public.collate_ci NOT NULL,"
				+ "    ngendercode smallint NOT NULL,"
				+ "    sfathername character varying(100) COLLATE public.collate_ci,"
				+ "    nneedmigrant smallint NOT NULL DEFAULT 4," + "    ncountrycode integer NOT NULL,"
				+ "    nregioncode integer NOT NULL," + "    ndistrictcode integer NOT NULL,"
				+ "    ncitycode integer NOT NULL,"
				+ "    scityname character varying(100) COLLATE pg_catalog.\"default\","
				+ "    spostalcode character varying(20) COLLATE public.collate_ci,"
				+ "    sstreet character varying(100) COLLATE public.collate_ci,"
				+ "    shouseno character varying(20) COLLATE public.collate_ci,"
				+ "    sflatno character varying(20) COLLATE public.collate_ci,"
				+ "    nneedcurrentaddress smallint NOT NULL DEFAULT 4," + "    nregioncodetemp integer,"
				+ "    ndistrictcodetemp integer," + "    ncitycodetemp integer,"
				+ "    scitynametemp character varying(100) COLLATE pg_catalog.\"default\","
				+ "    spostalcodetemp character varying(20) COLLATE public.collate_ci,"
				+ "    sstreettemp character varying(100) COLLATE public.collate_ci,"
				+ "    shousenotemp character varying(20) COLLATE public.collate_ci,"
				+ "    sflatnotemp character varying(20) COLLATE public.collate_ci,"
				+ "    sphoneno character varying(20) COLLATE public.collate_ci,"
				+ "    smobileno character varying(20) COLLATE public.collate_ci,"
				+ "    semail character varying(100) COLLATE public.collate_ci,"
				+ "    srefid character varying(255) COLLATE public.collate_ci,"
				+ "    spassportno character varying(100) COLLATE public.collate_ci,"
				+ "    sexternalid character varying(255) COLLATE public.collate_ci,"
				+ "    dmodifieddate timestamp without time zone,"
				+ "    nsitecode smallint NOT NULL DEFAULT '-1'::integer," + "    nstatus smallint NOT NULL DEFAULT 1,"
				+ "    CONSTRAINT patientmastertemp_pkey PRIMARY KEY (spatientid)" + ")" + "TABLESPACE pg_default;"
				+ "ALTER TABLE IF EXISTS public.patientmastertemp " + "    OWNER to postgres ";

		jdbcTemplate.execute(query);

		query = "select *,to_char(ddob,'" + userInfo.getSsitedate() + "') as sdob from patientmaster";

		List<Patient> lstPatientMaster = jdbcTemplate.query(query, new Patient());

		for (Patient patient : lstPatientMaster) {

			final String Patientname = patient.getSfirstname() + " " + patient.getSlastname();

			final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
			String ddob = sdFormat.format(patient.getDdob());
			// patient.setSdob(new
			// SimpleDateFormat(userInfo.getSsitedate()).format(patient.getDdob()));

			final int gender = patient.getNgendercode();

			final String concat = Patientname + ddob + gender;
			String oldpatientId = patient.getSpatientid();

			patient.setSpatientid(UUID.nameUUIDFromBytes(concat.getBytes("UTF8")).toString());

			query = "select * from patientmastertemp where spatientid='" + patient.getSpatientid() + "'";
			Patient p = (Patient) jdbcUtilityFunction.queryForObject(query, Patient.class, jdbcTemplate);

			if (p == null) {

				query = " insert into patientmastertemp  " + " select N'"
						+ stringUtilityFunction.replaceQuote(patient.getSpatientid())
						+ "' as spatientid,pm.spatientid as spatientidold,sfirstname,slastname,ddob,sage,ngendercode,"
						+ " sfathername,nneedmigrant,ncountrycode,nregioncode,ndistrictcode, ncitycode ,scityname,spostalcode,sstreet,shouseno,sflatno,nneedcurrentaddress,nregioncodetemp,ndistrictcodetemp,ncitycodetemp ,"
						+ " scitynametemp,spostalcodetemp, sstreettemp , shousenotemp, sflatnotemp,sphoneno ,smobileno,semail ,srefid,spassportno,sexternalid,dmodifieddate,nsitecode,nstatus "
						+ " from patientmaster pm where pm.spatientid='" + oldpatientId + "'";

//			query = " insert into patientmastertemp " + "( "
//					+ "    spatientid,spatientidold,sfirstname,slastname,ddob,sage,"
//					+ "    ngendercode,sfathername,nneedmigrant,ncountrycode,nregioncode,ndistrictcode,ncitycode,scityname,"
//					+ "    spostalcode,sstreet,shouseno,sflatno,nneedcurrentaddress,nregioncodetemp,"
//					+ "    ndistrictcodetemp,ncitycodetemp,scitynametemp,spostalcodetemp,sstreettemp,shousenotemp,"
//					+ "    sflatnotemp,sphoneno,smobileno,semail,srefid,spassportno,sexternalid,dmodifieddate,nsitecode,nstatus,"
//					+ ") " + " values(N'" + stringUtilityFunction.replaceQuote(patient.getSpatientid()) + "',N'" + oldpatientId + "'"
//					+ stringUtilityFunction.replaceQuote(patient.getSfirstname()) + "'," + " N'" + stringUtilityFunction.replaceQuote(patient.getSlastname())
//					+ "','" + ddob + "',N'" + stringUtilityFunction.replaceQuote(patient.getSage()) + "'," + patient.getNgendercode() + ","
//					+ " case when '" + stringUtilityFunction.replaceQuote(patient.getSfathername()) + "'='null' then null else N'"
//					+ stringUtilityFunction.replaceQuote(patient.getSfathername()) + "'end," + patient.getNneedmigrant() + ","
//					+ patient.getNcountrycode() + "," + patient.getNregioncode() + "," + patient.getNdistrictcode()
//					+ ", -1 ," + " case when '" + stringUtilityFunction.replaceQuote(patient.getSpostalcode()) + "'='null' then null else N'"
//					+ stringUtilityFunction.replaceQuote(patient.getSpostalcode()) + "'end," + " case when '" + patient.getSstreet()
//					+ "'='null' then '' else N'" + stringUtilityFunction.replaceQuote(patient.getSstreet()) + "' end, case when '"
//					+ patient.getShouseno() + "'='null' then '' else N'" + stringUtilityFunction.replaceQuote(patient.getShouseno())
//					+ "' end, case when '" + patient.getSflatno() + "'='null' then '' else N'"
//					+ stringUtilityFunction.replaceQuote(patient.getSflatno()) + "' end," + patient.getNneedcurrentaddress() + ","
//					+ patient.getNregioncodetemp() + "," + patient.getNdistrictcodetemp() + "," + " -1 ,"
//					+ " case when '" + stringUtilityFunction.replaceQuote(patient.getSpostalcodetemp()) + "'='null' then null else N'"
//					+ stringUtilityFunction.replaceQuote(patient.getSpostalcodetemp()) + "'end, case when '" + patient.getSstreettemp()
//					+ "'='null' then '' else " + "N'" + stringUtilityFunction.replaceQuote(patient.getSstreettemp()) + "' end, case when '"
//					+ patient.getShousenotemp() + "'='null' then '' else N'" + stringUtilityFunction.replaceQuote(patient.getShousenotemp())
//					+ "' end, case when '" + patient.getSflatnotemp() + "'='null' then '' else N'"
//					+ stringUtilityFunction.replaceQuote(patient.getSflatnotemp()) + "' end," + " case when '"
//					+ stringUtilityFunction.replaceQuote(patient.getSphoneno()) + "'='null' then null else N'"
//					+ stringUtilityFunction.replaceQuote(patient.getSphoneno()) + "'end," + " case when '"
//					+ stringUtilityFunction.replaceQuote(patient.getSmobileno()) + "'='null' then null else N'"
//					+ stringUtilityFunction.replaceQuote(patient.getSmobileno()) + "'end," + " case when '"
//					+ stringUtilityFunction.replaceQuote(patient.getSemail()) + "'='null' then null else N'"
//					+ stringUtilityFunction.replaceQuote(patient.getSemail()) + "'end ," + " case when '" + stringUtilityFunction.replaceQuote(patient.getSrefid())
//					+ "'='null' then null else N'" + stringUtilityFunction.replaceQuote(patient.getSrefid()) + "'end," + " case when '"
//					+ stringUtilityFunction.replaceQuote(patient.getSpassportno()) + "'='null' then null else N'"
//					+ stringUtilityFunction.replaceQuote(patient.getSpassportno()) + "'end," + " case when '"
//					+ stringUtilityFunction.replaceQuote(patient.getSexternalid()) + "'='null' then null else N'"
//					+ stringUtilityFunction.replaceQuote(patient.getSexternalid()) + "'end," + " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
//					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ",N'" + stringUtilityFunction.replaceQuote(patient.getScityname()) + "',N'" + stringUtilityFunction.replaceQuote(patient.getScitynametemp())
//					+ "')";

				jdbcTemplate.execute(query);

				query = " update patienthistory set spatientid='" + patient.getSpatientid()
						+ "',jsondata=jsondata||json_build_object('spatientid','" + patient.getSpatientid()
						+ "')::jsonb where spatientid='" + oldpatientId + "'";
				jdbcTemplate.execute(query);
			}
		}
		query = "delete from patientmaster";

		jdbcTemplate.execute(query);

		query = "insert into patientmaster select spatientid,sfirstname,slastname,ddob,sage,"
				+ "    ngendercode,sfathername,nneedmigrant,ncountrycode,nregioncode,ndistrictcode,ncitycode,scityname,"
				+ "    spostalcode,sstreet,shouseno,sflatno,nneedcurrentaddress,nregioncodetemp,"
				+ "    ndistrictcodetemp,ncitycodetemp,scitynametemp,spostalcodetemp,sstreettemp,shousenotemp,"
				+ "    sflatnotemp,sphoneno,smobileno,semail,srefid,spassportno,sexternalid,dmodifieddate,nsitecode,nstatus from patientmastertemp";
		jdbcTemplate.execute(query);

		return null;
	}

}
