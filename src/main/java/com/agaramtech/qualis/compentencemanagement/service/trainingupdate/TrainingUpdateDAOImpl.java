package com.agaramtech.qualis.compentencemanagement.service.trainingupdate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

//import com.agaramtech.qualis.checklist.model.ChecklistVersionQB;
import com.agaramtech.qualis.compentencemanagement.model.SeqNoCompetenceManagement;
import com.agaramtech.qualis.compentencemanagement.model.Technique;
import com.agaramtech.qualis.compentencemanagement.model.TraineeDocuments;
import com.agaramtech.qualis.compentencemanagement.model.TrainingCertification;
import com.agaramtech.qualis.compentencemanagement.model.TrainingDocuments;
import com.agaramtech.qualis.compentencemanagement.model.TrainingParticipants;
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
//import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TrainingUpdateDAOImpl implements TrainingUpdateDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrainingUpdateDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
//	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction fTPUtilityFunction;

	@Override
	public ResponseEntity<Object> getTrainingUpdate(String fromDate, String toDate, Integer ntechniquecode,
			final UserInfo userInfo, final String currentUIDate, Technique selectedTechinque) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");

			outputMap.put("FromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);

			outputMap.put("FromDate", fromDateUI);
			outputMap.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		List<Technique> lstTechnique = new ArrayList<Technique>();
		lstTechnique = getTechniqueForTrainingUpdate(userInfo);

		if (ntechniquecode == null) {
			if (lstTechnique.isEmpty()) {
				outputMap.put("selectedTechinque", null);
				outputMap.put("TechniqueList", lstTechnique);
				outputMap.put("SelectedTrainingUpdate", new HashMap<>());
			} else {
				ntechniquecode = (int) lstTechnique.get(0).getNtechniquecode();
				outputMap.put("selectedTechinque", lstTechnique.get(0));
			}
		} else {

			outputMap.put("selectedTechinque", selectedTechinque);

		}
		outputMap.put("filterTechnique", lstTechnique);

		final String queryString = " SELECT tc.ntrainingcode,tc.strainingvenue, " + " to_char(tc.dtrainingdatetime,'"
										+ userInfo.getSpgsitedatetime()
										+ "') as strainingdatetime,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
										+ userInfo.getSlanguagetypecode() + "',"
										+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,to_char(tc.dregistereddate,'"
										+ userInfo.getSsitedate() + "')"
										+ " as sregistereddate,tc.scomments, tq.stechniquename as stechniquename,"
										+ " tc.ntrainingcode,tc.dtrainingdatetime,"
										+ " tc.strainername,tc.strainingvenue,tc.strainingtopic, tc.ntransactionstatus,"
										+ " tt.strainingcategoryname,tc.ntrainingcategorycode, tc.noffsetdtrainingdatetime "
										+ " from  TrainingCertification tc,transactionstatus ts,technique tq,trainingcategory tt  "
										+ " where  tc.ntrainingcategorycode = tt.ntrainingcategorycode "
										+ " and tc.ntechniquecode = tq.ntechniquecode    " + " and tc.ntechniquecode = " + ntechniquecode
										+ " and tc.ntransactionstatus=ts.ntranscode    and tc.nstatus=1 and tc.ntransactionstatus=ts.ntranscode"
										+ " and tq.ntechniquecode = tc.ntechniquecode  and tt.ntrainingcategorycode=tc.ntrainingcategorycode "
										+ " and tc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
										+ " and tq.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tc.nsitecode = " + userInfo.getNmastersitecode()
										+""
										+ " and tc.ntransactionstatus in(" + Enumeration.TransactionStatus.CONDUCTED.gettransactionstatus()
										+ "," + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ") "
										+ " and tc.dconducteddate  between '" + fromDate + "' and '" + toDate + "' order by tc.ntrainingcode";

		LOGGER.info("getTrainingUpdate :---------->" + queryString);
		List<TrainingCertification> TechniqueList = (List<TrainingCertification>) jdbcTemplate.query(queryString,
				new TrainingCertification());
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<TrainingCertification> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(TechniqueList, Arrays.asList("strainingdatetime"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<TrainingCertification>>() {
				});
		outputMap.put("TechniqueList", TechniqueList);
		if (TechniqueList.isEmpty()) {
			map.put("TrainingDocument", Collections.emptyList());
			outputMap.put("SelectedTrainingUpdate", new HashMap<>());
		} else {
			outputMap.put("SelectedTrainingUpdate", lstUTCConvertedDate.get(TechniqueList.size() - 1));
			final TrainingCertification selectedTraining = lstUTCConvertedDate.get(TechniqueList.size() - 1);
			final int ntrainingcode = selectedTraining.getNtrainingcode();
			outputMap.put("SelectedTrainingUpdate", selectedTraining);
			outputMap.putAll(getTrainingDocument(ntrainingcode, 0, userInfo));
			outputMap.putAll(getTrainingParticipants(ntrainingcode, userInfo));
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public TrainingCertification getTrainingUpdateId(int ntrainingcode) throws Exception {
		final String strQuery = "select * from TrainingCertification where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntrainingcode=" + ntrainingcode;
		return (TrainingCertification) jdbcUtilityFunction.queryForObject(strQuery, TrainingCertification.class,
				jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getTrainingUpdateById(int ntrainingcode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		TrainingCertification lstTrainingUpdate = getTrainingUpdateId(ntrainingcode);
		if (lstTrainingUpdate != null) {
			//ALPD-5902 query spacing below error fixed
			//java.lang.ClassCastException: class org.springframework.jdbc.BadSqlGrammarException 
			//cannot be cast to class com.agaramtech.qualis.compentencemanagement.model.TrainingCertification
			//(org.springframework.jdbc.BadSqlGrammarException and com.agaramtech.qualis.compentencemanagement.model.TrainingCertification are in unnamed module of loader org.apache.catalina.loader.ParallelWebappClassLoader @4612b856
			String sQuery = " SELECT tc.ntrainingcode,tc.strainingvenue, tq.stechniquename as stechniquename,"
					// ALPD-967
					+ " tq.ntechniquecode," + " to_char(  tc.dtrainingdatetime,'" + userInfo.getSpgsitedatetime()
					+ "' ) as strainingdatetime,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, to_char(  tc.dregistereddate, '"
					+ userInfo.getSsitedate() + "' ) "
					+ " as sregistereddate,tc.strainername,tc.strainingtopic,tc.ntransactionstatus, tc.noffsetdtrainingdatetime "
					+ " from TrainingCertification tc,transactionstatus ts,technique tq "
					+ " where tc.ntransactionstatus=ts.ntranscode and tq.ntechniquecode = tc.ntechniquecode "
					+ " and tc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tq.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and tc.nsitecode = " + userInfo.getNmastersitecode() +""
					+ " and tc.ntransactionstatus in(" + Enumeration.TransactionStatus.CONDUCTED.gettransactionstatus()
					+ "," + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ") "
					+ " and tc.ntrainingcode = " + ntrainingcode + " order by tc.ntrainingcode";

			TrainingCertification lstTrainingUpdate1 = (TrainingCertification) jdbcUtilityFunction
					.queryForObject(sQuery, TrainingCertification.class, jdbcTemplate);
			
			map.put("selectedTechinque", lstTrainingUpdate1);

			List<TrainingCertification> trainingUpdate = new ArrayList<TrainingCertification>();
			trainingUpdate.add(lstTrainingUpdate1);
			
			ObjectMapper objMapper = new ObjectMapper();
			final List<TrainingCertification> lstUTCConvertedDate = objMapper.convertValue(
					dateUtilityFunction.getSiteLocalTimeFromUTC(trainingUpdate, Arrays.asList("strainingdatetime"),
							Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
					new TypeReference<List<TrainingCertification>>() {
					});
			map.put("SelectedTrainingUpdate", lstUTCConvertedDate.get(0));
			if (lstTrainingUpdate1 != null) {
				int versionCode = ((TrainingCertification) lstTrainingUpdate1).getNtrainingcode();
				map.putAll(((Map<String, Object>) getTrainingDocument(versionCode, 0, userInfo)));
				map.putAll((Map<String, Object>) getTrainingParticipants(versionCode, userInfo));
			}
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getTraningUpdateByTechnique(String fromDate, String toDate, Integer ntechniquecode,
			final UserInfo userInfo, final String currentUIDate, Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> returnMap = new LinkedHashMap<String, Object>();

		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");

			returnMap.put("FromDate", mapObject.get("FromDateWOUTC"));
			returnMap.put("ToDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);

			returnMap.put("FromDate", fromDateUI);
			returnMap.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}

		if (returnMap != null) {

			final String queryString = " SELECT tc.strainingvenue, " + " to_char(tc.dtrainingdatetime,'"
										+ userInfo.getSsitedatetime()
										+ "') as strainingdatetime,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
										+ userInfo.getSlanguagetypecode() + "',"
										+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,to_char(tc.dregistereddate,'"
										+ userInfo.getSsitedate() + "')" + " as sregistereddate,tc.scomments,"
										+ " tq.stechniquename as stechniquename," + " tc.ntrainingcode,tc.dtrainingdatetime,"
										+ " tc.strainername,tc.strainingvenue,tc.strainingtopic, tc.ntransactionstatus,"
										+ " tt.strainingcategoryname,tc.ntrainingcategorycode,tc.ntechniquecode, tc.noffsetdtrainingdatetime from "
										+ " TrainingCertification tc,transactionstatus ts,technique tq,trainingcategory tt  "
										+ " where  tc.ntrainingcategorycode = tt.ntrainingcategorycode "
										+ " and tc.ntechniquecode = tq.ntechniquecode  and tc.ntransactionstatus=ts.ntranscode and tc.nstatus=1 and tc.ntransactionstatus=ts.ntranscode"
										+ " and tq.ntechniquecode = tc.ntechniquecode  and tt.ntrainingcategorycode=tc.ntrainingcategorycode "
										+ " and tc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
										+ " and tq.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and tc.nsitecode = " + userInfo.getNmastersitecode() 
										+ " and tc.ntechniquecode= " + ntechniquecode + " and tc.ntransactionstatus in("
										+ Enumeration.TransactionStatus.CONDUCTED.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ") "
										+ " and tc.dconducteddate between'" + fromDate + "' and '" + toDate + "' order by tc.ntrainingcode";

			List<TrainingCertification> TechniqueList = (List<TrainingCertification>) jdbcTemplate.query(queryString,
					new TrainingCertification());
			final ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());

			final List<TrainingCertification> lstUTCConvertedDate = objMapper.convertValue(
					dateUtilityFunction.getSiteLocalTimeFromUTC(TechniqueList, Arrays.asList("strainingdatetime"),
							Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
					new TypeReference<List<TrainingCertification>>() {
					});
			returnMap.put("TechniqueList", lstUTCConvertedDate);
			if (TechniqueList.isEmpty()) {
				map.put("TrainingDocument", Collections.emptyList());
				returnMap.put("SelectedTrainingUpdate", new HashMap<>());
			} else {
				if (inputMap.containsKey("ntrainingcode")) {
					returnMap.put("SelectedTrainingUpdate",
							lstUTCConvertedDate.stream()
									.filter(x -> x.getNtrainingcode() == ((int) inputMap.get("ntrainingcode")))
									.collect(Collectors.toList()).get(0));
					int ntrainingcode = (int) inputMap.get("ntrainingcode");
					returnMap.putAll(getTrainingDocument(ntrainingcode, 0, userInfo));
					returnMap.putAll(getTrainingParticipants(ntrainingcode, userInfo));
				} else {
					returnMap.put("SelectedTrainingUpdate", lstUTCConvertedDate.get(TechniqueList.size() - 1));
					final TrainingCertification selectedTraining = lstUTCConvertedDate.get(TechniqueList.size() - 1);
					final int ntrainingcode = selectedTraining.getNtrainingcode();
					// returnMap.put("SelectedTrainingUpdate", selectedTraining);
					returnMap.putAll(getTrainingDocument(ntrainingcode, 0, userInfo));
					returnMap.putAll(getTrainingParticipants(ntrainingcode, userInfo));
				}
			}
		}

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getTechniqueBytechnique(int ntechniquecode) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		final String query = "select * from technique where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +  " and ntechniquecode=" + ntechniquecode;
		final Technique lstTechnique = (Technique) jdbcTemplate.queryForObject(query, new Technique());
		returnMap.put("TechniqueBytechnique", lstTechnique);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	private List<Technique> getTechniqueForTrainingUpdate(final UserInfo userInfo) throws Exception {
		final String sQuery = "select tq.* from technique tq where tq.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tq.ntechniquecode <> -1  and tq.nsitecode = " + userInfo.getNmastersitecode()
				+ " order by tq.ntechniquecode";
		return (List<Technique>) jdbcTemplate.query(sQuery, new Technique());
	}

	public Map<String, Object> getTrainingDocument(final int ntrainingcode, final int ntrainingdoccode,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String queryformat = "COALESCE(TO_CHAR(dcreateddate,'" + objUserInfo.getSpgdatetimeformat() + "'),'-') ";

		String sQuery = " select td.ntrainingdoccode, td.ntrainingcode,td.sfilename,td.sfiledesc,td.ssystemfilename,td.nstatus,"
						+ " td.nattachmenttypecode, case when td.nlinkcode = -1 then cast(td.nfilesize as text) else '-' end sfilesize,"
						+ " case when td.nlinkcode = -1 then " + queryformat + " else '-' end  screateddate,"
						+ "case when td.nlinkcode = -1 then '-' else lm.jsondata->>'slinkname' end slinkname , lm.nlinkcode, td.noffsetdcreateddate "
						+ " from trainingdocuments td, linkmaster lm  where td.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and td.ntrainingcode = "
						+ ntrainingcode + " and lm.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and td.nlinkcode=lm.nlinkcode and td.nstatus=lm.nstatus";
		if (ntrainingdoccode > 0) {
			sQuery = sQuery + " and td.ntrainingdoccode = " + ntrainingdoccode;
		}
		List<TrainingDocuments> TrainingDocuments = (List<TrainingDocuments>) jdbcTemplate.query(sQuery,
				new TrainingDocuments());

		outputMap.put("TrainingDocument",
				dateUtilityFunction.getSiteLocalTimeFromUTC(TrainingDocuments, Arrays.asList("screateddate"),
						Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null, false));

		return (outputMap);
	}

	public Map<String, Object> getTrainingParticipants(final int ntrainingcode, final UserInfo objUserInfo)
			throws Exception {
		
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String yes = commonFunction.getMultilingualMessage("IDS_YES", objUserInfo.getSlanguagefilename());
		final String no = commonFunction.getMultilingualMessage("IDS_NO", objUserInfo.getSlanguagefilename());
		
		final String query = " select  u.sfirstname  || ' ' || u.slastname AS sfullname,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
							+ objUserInfo.getSlanguagetypecode() + "',"
							+ " ts.jsondata->'stransdisplaystatus'->>'en-US')  as sattendancestatus,tp.ncompetencystatus, "
							+ "	 case when tp.ncertifiedstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ "  then  '" + yes + "' else '" + no + "'  end as stransdisplaystatuscertified , "
							+ "	 case when tp.ncompetencystatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ "  then  '" + yes + "' else '" + no + "'  end as stransdisplaystatuscompotent ,"
							+ "	 tp.ncertifiedstatus,tp.ntransactionstatus,tp.ntrainingcode,tp.nusercode,"
							+ "	 tp.nparticipantcode from trainingparticipants tp,users u,transactionstatus ts "
							+ "	 where u.nusercode=tp.nusercode and ts.ntranscode=tp.ntransactionstatus  and tp.nstatus=1 and "
							+ "	 ntrainingcode = " + ntrainingcode + " "
							// + " and tp.ntransactionstatus not in ("
							// + Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
							+ " order by tp.nparticipantcode";

		final List<TrainingCertification> ParticipantsList = (List<TrainingCertification>) jdbcTemplate.query(query,
				new TrainingCertification());
		outputMap.put("Participants", ParticipantsList);
		outputMap.put("selectedParticipants", ParticipantsList.get(ParticipantsList.size() - 1));
		
		int nparticipantcode = ((int) ParticipantsList.get(ParticipantsList.size() - 1).getNparticipantcode());
		outputMap.putAll(getTraineeDocuments(nparticipantcode, 0, objUserInfo));
		
		return (outputMap);
	}

	public Map<String, Object> getTraineeDocuments(final int nparticipantcode, final int ntraineedoccode,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String queryformat = "COALESCE(TO_CHAR(dcreateddate,'" + objUserInfo.getSpgdatetimeformat() + "'),'-') ";

		String query = " select td.ntraineedoccode, td.nparticipantcode, td.sfilename, td.sfiledesc, td.ssystemfilename, "
						+ "td.nattachmenttypecode, td.nstatus, case when td.nlinkcode = -1 then cast(td.nfilesize as text) else '-' end sfilesize,"
						+ " case when td.nlinkcode = -1 then " + queryformat + " else '-' end  screateddate, "
						+ "case when td.nlinkcode = -1 then '-' else lm.jsondata->>'slinkname' end slinkname, lm.nlinkcode, td.noffsetdcreateddate "
						+ " from traineedocuments td, linkmaster lm  where td.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and td.nparticipantcode = "
						+ nparticipantcode + " and td.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and td.nlinkcode=lm.nlinkcode and " + " td.nstatus=lm.nstatus " ;
		if (ntraineedoccode > 0) {
			query = query + " and td.ntrainingdoccode = " + ntraineedoccode;
		}
		List<TraineeDocuments> TraineeDocuments = (List<TraineeDocuments>) jdbcTemplate.query(query,
				new TraineeDocuments());
//		final List<TraineeDocuments> lstUTCConvertedDate = objMapper.convertValue(
//				objGeneral.getSiteLocalTimeFromUTC(TraineeDocuments, Arrays.asList("screateddate"),
//						Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null, false),
//				new TypeReference<List<TraineeDocuments>>() {
//				});

		outputMap.put("TraineeDocuments",
				dateUtilityFunction.getSiteLocalTimeFromUTC(TraineeDocuments, Arrays.asList("screateddate"),
						Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null, false));

		// outputMap.put("TraineeDocuments", lstUTCConvertedDate);
		return (outputMap);
	}

	@Override
	public ResponseEntity<Object> createTrainingDoc(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		final String sQuery = " lock  table trainingdocuments "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final ObjectMapper objMapper = new ObjectMapper();
		final List<TrainingDocuments> lstReqTrainingDoc = objMapper.readValue(request.getParameter("trainingdoc"),
				new TypeReference<List<TrainingDocuments>>() {
				});

		final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS);
		final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
		final String screatedDate = dateUtilityFunction.instantDateToString(instantDate);

		lstReqTrainingDoc.forEach(objtf -> {
			objtf.setDcreateddate(instantDate);
			if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					|| objtf.getNattachmenttypecode() == Enumeration.AttachmentType.LINK.gettype()) {
				objtf.setDcreateddate(instantDate);
				objtf.setNoffsetdcreateddate(noffset);
				objtf.setScreateddate(screatedDate.replace("T", " "));
			}

		});

		int seqNo = 0;
		if (lstReqTrainingDoc != null && lstReqTrainingDoc.size() > 0) {
			final TrainingCertification objTrainingDoc = checkTrainingIsPresent(
					lstReqTrainingDoc.get(0).getNtrainingcode());

			if (objTrainingDoc != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqTrainingDoc.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = fTPUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
				}
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
				final String sGetSeqNoQuery = "select nsequenceno from seqnocompetencemanagement where stablename = 'trainingdocuments';";

				SeqNoCompetenceManagement objseq = (SeqNoCompetenceManagement) jdbcTemplate
						.queryForObject(sGetSeqNoQuery, new SeqNoCompetenceManagement());
				seqNo = objseq.getNsequenceno();
				seqNo = seqNo + 1;

				final String sInsertQuery = "insert into trainingdocuments (ntrainingdoccode, ntrainingcode, sfilename, sfiledesc, "
											+ "ssystemfilename,nattachmenttypecode, nlinkcode, dcreateddate, ntzcreateddate, noffsetdcreateddate, nfilesize,"
											+ " dmodifieddate,nsitecode, nstatus) values (" + seqNo + ", "
											+ (lstReqTrainingDoc).get(0).getNtrainingcode() + ", N'"
											+ stringUtilityFunction.replaceQuote(lstReqTrainingDoc.get(0).getSfilename()) + "',N'"
											+ stringUtilityFunction.replaceQuote(lstReqTrainingDoc.get(0).getSfiledesc()) + "',N'"
											+ stringUtilityFunction.replaceQuote(lstReqTrainingDoc.get(0).getSsystemfilename()) + "',"
											+ lstReqTrainingDoc.get(0).getNattachmenttypecode() + ", "
											+ lstReqTrainingDoc.get(0).getNlinkcode() + ", '" + lstReqTrainingDoc.get(0).getDcreateddate()
											+ "', " + objUserInfo.getNtimezonecode() + ", "
											+ lstReqTrainingDoc.get(0).getNoffsetdcreateddate() + ", "
											+ lstReqTrainingDoc.get(0).getNfilesize() + ", '"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', " + objUserInfo.getNmastersitecode()
											+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				jdbcTemplate.execute(sInsertQuery);

				final String sUpdateSeqNoQuery = "update seqnocompetencemanagement set nsequenceno = " + seqNo
						+ " where stablename = 'trainingdocuments';";
				jdbcTemplate.execute(sUpdateSeqNoQuery);

				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_ADDTRAININGDOCUMENT");

				final List<Object> listObject = new ArrayList<Object>();
				lstReqTrainingDoc.get(0).setNtrainingdoccode(seqNo);
				listObject.add(lstReqTrainingDoc);

				auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FILEALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return new ResponseEntity<Object>(
					getTrainingDocument(lstReqTrainingDoc.get(0).getNtrainingcode(), 0, objUserInfo), HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public TrainingCertification checkTrainingIsPresent(final int ntrainingcode) throws Exception {
		final String strQuery = "select ntrainingcode from trainingcertification where ntrainingcode = " + ntrainingcode
				+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		TrainingCertification objTrainingDoc = (TrainingCertification) jdbcUtilityFunction.queryForObject(strQuery,
				TrainingCertification.class, jdbcTemplate);
		return objTrainingDoc;
	}

	@Override
	public ResponseEntity<Object> deleteTrainingDoc(final TrainingDocuments objTrainingDoc, final UserInfo objUserInfo)
			throws Exception {
		final TrainingCertification objTrainingDoc1 = checkTrainingIsPresent(objTrainingDoc.getNtrainingcode());
		if (objTrainingDoc1 != null) {
			if (objTrainingDoc1 != null) {
				final String sQuery = "select * from trainingdocuments where ntrainingdoccode = "
						+ objTrainingDoc.getNtrainingdoccode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TrainingDocuments objTF = (TrainingDocuments) jdbcUtilityFunction.queryForObject(sQuery,
						TrainingDocuments.class, jdbcTemplate);
				if (objTF != null) {

					final String sUpdateQuery = "update trainingdocuments set nstatus = "
												+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
												+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntrainingdoccode = "
												+ objTrainingDoc.getNtrainingdoccode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList.add("IDS_DELETETRAININGDOCUMENT");
					lstObject.add(objTrainingDoc);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return new ResponseEntity<Object>(getTrainingDocument(objTrainingDoc.getNtrainingcode(), 0, objUserInfo),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> createTrainieeDoc(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		final String sQuery = " lock  table traineedocuments "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final ObjectMapper objMapper = new ObjectMapper();

		final List<TraineeDocuments> lstReqTrainieeDoc = objMapper.readValue(request.getParameter("trainieedoc"),
				new TypeReference<List<TraineeDocuments>>() {
				});

		final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS);
		final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
		final String screatedDate = dateUtilityFunction.instantDateToString(instantDate);

		lstReqTrainieeDoc.forEach(objtf -> {
			objtf.setDcreateddate(instantDate);
			if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					|| objtf.getNattachmenttypecode() == Enumeration.AttachmentType.LINK.gettype()) {
				objtf.setDcreateddate(instantDate);
				objtf.setNoffsetdcreateddate(noffset);
				objtf.setScreateddate(screatedDate.replace("T", " "));
			}

		});

		int seqNo = 0;
		if (lstReqTrainieeDoc != null && lstReqTrainieeDoc.size() > 0) {
			final TrainingCertification objTrainieeDoc = checkTrainieeIsPresent(
					lstReqTrainieeDoc.get(0).getNparticipantcode());

			if (objTrainieeDoc != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqTrainieeDoc.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = fTPUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
				}

				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
				final String sGetSeqNoQuery = "select nsequenceno from seqnocompetencemanagement where stablename = 'traineedocuments';";

				SeqNoCompetenceManagement objseq = (SeqNoCompetenceManagement) jdbcTemplate
						.queryForObject(sGetSeqNoQuery, new SeqNoCompetenceManagement());
				seqNo = objseq.getNsequenceno();
				seqNo = seqNo + 1;

				final String sInsertQuery = "insert into traineedocuments (ntraineedoccode,nparticipantcode,nusercode, "
											+ " sfilename,sfiledesc,ssystemfilename,nattachmenttypecode,nlinkcode,dcreateddate,"
											+ " ntzcreateddate,noffsetdcreateddate,nfilesize,dmodifieddate,nsitecode,nstatus) "
											+ " values (" + seqNo + ", " + (lstReqTrainieeDoc).get(0).getNparticipantcode() + ","
											+ (lstReqTrainieeDoc).get(0).getNusercode() + ", N'"
											+ stringUtilityFunction.replaceQuote(lstReqTrainieeDoc.get(0).getSfilename()) + "',N'"
											+ stringUtilityFunction.replaceQuote(lstReqTrainieeDoc.get(0).getSfiledesc()) + "',N'"
											+ stringUtilityFunction.replaceQuote(lstReqTrainieeDoc.get(0).getSsystemfilename()) + "',"
											+ lstReqTrainieeDoc.get(0).getNattachmenttypecode() + ", "
											+ lstReqTrainieeDoc.get(0).getNlinkcode() + ", '" + lstReqTrainieeDoc.get(0).getDcreateddate()
											+ "', " + objUserInfo.getNtimezonecode() + ", "
											+ lstReqTrainieeDoc.get(0).getNoffsetdcreateddate() + ", "
											+ lstReqTrainieeDoc.get(0).getNfilesize() + ", '"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', " + objUserInfo.getNmastersitecode()
											+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				jdbcTemplate.execute(sInsertQuery);

				final String sUpdateSeqNoQuery = "update seqnocompetencemanagement set nsequenceno = " + seqNo
						+ " where stablename = 'traineedocuments';";
				jdbcTemplate.execute(sUpdateSeqNoQuery);

				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_ADDTRAINEEDOCUMENT");
				// multilingualIDList.add(lstReqTrainieeDoc.get(0).getNattachmenttypecode() ==
				// Enumeration.AttachmentType.FTP.gettype()?"IDS_ADDTRAINEEDOCUMENT":
				// "IDS_ADDTRAINEEDOCUMENTLINK");

				final List<Object> listObject = new ArrayList<Object>();
				lstReqTrainieeDoc.get(0).setNtraineedoccode(seqNo);
				listObject.add(lstReqTrainieeDoc);
				auditUtilityFunction.fnInsertAuditAction(lstReqTrainieeDoc, 1, null, multilingualIDList, objUserInfo);
				// fnInsertListAuditAction(listObject, 1, null, multilingualIDList,
				// objUserInfo);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FILEALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return new ResponseEntity<Object>(
					getTraineeDocuments(lstReqTrainieeDoc.get(0).getNparticipantcode(), 0, objUserInfo), HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public TrainingCertification checkTrainieeIsPresent(final int nparticipantcode) throws Exception {
		final String strQuery = "select nparticipantcode from trainingparticipants where nparticipantcode = "
				+ nparticipantcode + " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final TrainingCertification objTrainieeDoc = (TrainingCertification) jdbcUtilityFunction.queryForObject(strQuery,
				TrainingCertification.class, jdbcTemplate);
		return objTrainieeDoc;
	}

	@Override
	public ResponseEntity<Object> deleteTrainieeDoc(final TraineeDocuments objTrainieeDoc, final UserInfo objUserInfo)
			throws Exception {
		final TrainingCertification objTrainieeDoc1 = checkTrainieeIsPresent(objTrainieeDoc.getNparticipantcode());
		if (objTrainieeDoc1 != null) {
			if (objTrainieeDoc1 != null) {
				final String sQuery = "select * from traineedocuments where ntraineedoccode = "
						+ objTrainieeDoc.getNtraineedoccode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TraineeDocuments objTF = (TraineeDocuments) jdbcUtilityFunction.queryForObject(sQuery,
						TraineeDocuments.class, jdbcTemplate);
				if (objTF != null) {
//					if(objTrainingDoc.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
////						deleteFTPFile(Arrays.asList(objTF.getSsystemfilename()), "master", objUserInfo);
//					} else {
//						objTestFile.setScreateddate(null);
//					}

//					if(objTF.getNdefaultstatus()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
//						final String sDeleteQuery = "select * from trainingdocuments where "
//						+ " ntrainingdoccode !="+objTrainingDoc.getNtrainingdoccode()+""
//						+ " and ntrainingcode="+objTrainingDoc.getNtrainingcode()+""
//						+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//						List<TrainingDocuments> lstTrainingDoc = (List<TrainingDocuments>) findBySinglePlainSql(sDeleteQuery, TrainingDocuments.class);
//						String sDefaultQuery="";
//			        if(lstTrainingDoc.isEmpty()) {
//			        	 sDefaultQuery = " update trainingdocuments set "
//								 	+ " nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
//									+ " where ntrainingdoccode = "+objTrainingDoc.getNtrainingdoccode();					
//			        }else {
//						 sDefaultQuery = "update trainingdocuments set "
//						 	+ " ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
//							+" where ntrainingdoccode = "+lstTrainingDoc.get(lstTrainingDoc.size()-1).getNtrainingdoccode();
//			           }
//			        jdbcTemplate.execute(sDefaultQuery);
//					}				
					final String sUpdateQuery = "update traineedocuments set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'  where ntraineedoccode = "
							+ objTrainieeDoc.getNtraineedoccode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList.add("IDS_DELETETRAINEEFILE");
					lstObject.add(objTrainieeDoc);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return new ResponseEntity<Object>(getTraineeDocuments(objTrainieeDoc.getNparticipantcode(), 0, objUserInfo),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public ResponseEntity<Object> attendTrainingParticipants(TrainingParticipants objParticipants, UserInfo userInfo)
//			throws Exception {
//		Integer ntrainingcode = (Integer) objParticipants.getNtrainingcode();
//		Integer nparticipantcode = (Integer) objParticipants.getNparticipantcode();
//
//		if (objParticipants != null) {
//			if (objParticipants.getNtransactionstatus() != 40 && objParticipants.getNtransactionstatus() == 69) {
//				return new ResponseEntity<>(
//						commonFunction.getMultilingualMessage("IDS_ALREADYATTENDED", userInfo.getSlanguagefilename()),
//						HttpStatus.EXPECTATION_FAILED);
//			} else if (objParticipants.getNtransactionstatus() == 40 && objParticipants.getNtransactionstatus() == 69) {
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTINVITEDPARTICIPANTS",
//						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//			} else {
//				final String sUpdateQuery = "Update TrainingParticipants set ntransactionstatus = "
//						+ Enumeration.TransactionStatus.ATTENDED.gettransactionstatus()
//						+ "  where nparticipantcode in (" + nparticipantcode + ");";
//				jdbcTemplate.execute(sUpdateQuery);
//			}
//			String statusval = "select * from TrainingParticipants where nparticipantcode in (" + nparticipantcode
//					+ ")";
//			TrainingCertification objcertification = (TrainingCertification) jdbcTemplate.queryForObject(statusval,new TrainingCertification());
//		}
//		Map<String, Object> returnObj = new HashMap<>();
//		returnObj.putAll(getTrainingParticipants(ntrainingcode, userInfo));
//		returnObj.putAll(getParticipantsAccordion(nparticipantcode, userInfo));
//		return new ResponseEntity<Object>(returnObj, HttpStatus.OK);
//
//	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public ResponseEntity<Object> certifyTrainingParticipants(TrainingParticipants objParticipants, UserInfo userInfo)
//			throws Exception {
//		Integer ntrainingcode = (Integer) objParticipants.getNtrainingcode();
//		Integer nparticipantcode = (Integer) objParticipants.getNparticipantcode();
//
//		if (objParticipants != null) {
//			if (objParticipants.getNtransactionstatus() == 40 && objParticipants.getNtransactionstatus() == 69) {
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTATTENDEDPARTICIPANTS",
//						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//			} else if (objParticipants.getNtransactionstatus() != 69) {
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTATTENDEDPARTICIPANTS",
//						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//			} else if (objParticipants.getNcertifiedstatus() != 4) {
//				return new ResponseEntity<>(
//						commonFunction.getMultilingualMessage("IDS_ALREADYCERTIFIED", userInfo.getSlanguagefilename()),
//						HttpStatus.EXPECTATION_FAILED);
//			} else {
//				final String sUpdateQuery = "Update TrainingParticipants set ncertifiedstatus = "
//						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "  where nparticipantcode in ("
//						+ nparticipantcode + ");";
//				getJdbcTemplate().execute(sUpdateQuery);
//			}
//			String statusval = "select * from TrainingParticipants where nparticipantcode in (" + nparticipantcode
//					+ ")";
//			TrainingCertification objcertification = (TrainingCertification) getJdbcTemplate().queryForObject(statusval,new TrainingCertification());
//		}
//		Map<String, Object> returnObj = new HashMap<>();
//		returnObj.putAll(getTrainingParticipants(ntrainingcode, userInfo));
//		returnObj.putAll( getParticipantsAccordion(nparticipantcode, userInfo));
//		return new ResponseEntity<Object>(returnObj, HttpStatus.OK);
//	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public ResponseEntity<Object> competentTrainingParticipants(TrainingParticipants objParticipants, UserInfo userInfo)
//			throws Exception {
//		Integer ntrainingcode = (Integer) objParticipants.getNtrainingcode();
//		Integer nparticipantcode = (Integer) objParticipants.getNparticipantcode();
//
//		if (objParticipants != null) {
//			if (objParticipants.getNcertifiedstatus() != 3 || objParticipants.getNtransactionstatus() != 69) {
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTATTENDEDANDCERTIFIEDDATA",
//						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//			}
////		else if( objParticipants.getNtransactionstatus() != 69){
////			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYCERTIFIED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
////		}
//			else if (objParticipants.getNcompetencystatus() == 3) {
//				return new ResponseEntity<>(
//						commonFunction.getMultilingualMessage("IDS_ALREADYCOMPETENT", userInfo.getSlanguagefilename()),
//						HttpStatus.EXPECTATION_FAILED);
//			} else {
//				final String sUpdateQuery = "Update TrainingParticipants set ncompetencystatus = "
//						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "  where nparticipantcode in ("
//						+ nparticipantcode + ");";
//				getJdbcTemplate().execute(sUpdateQuery);
//			}
//			String statusval = "select * from TrainingParticipants where nparticipantcode in (" + nparticipantcode
//					+ ")";
//			TrainingCertification objcertification = (TrainingCertification) getJdbcTemplate().queryForObject(statusval,new TrainingCertification());
//		}
//		Map<String, Object> returnObj = new HashMap<>();
//		returnObj.putAll(getTrainingParticipants(ntrainingcode, userInfo));
//		returnObj.putAll(getParticipantsAccordion(nparticipantcode, userInfo));
//		return new ResponseEntity<Object>(returnObj, HttpStatus.OK);
//
//	}

	@Override
	public ResponseEntity<Object> updateTrainingUpdate(Map<String, Object> obj, UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final TrainingCertification objCompleteTraining = objMapper.convertValue(obj.get("trainingupdate"),
				TrainingCertification.class);
		final String statusval = "select ntransactionstatus from trainingcertification where ntrainingcode="
				+ objCompleteTraining.getNtrainingcode() +  " and nsitecode = " + userInfo.getNmastersitecode()+"";
		int ntranstatus = jdbcTemplate.queryForObject(statusval, Integer.class);
		String strtp = "select * from trainingparticipants where ntrainingcode in ("
				+ objCompleteTraining.getNtrainingcode() + ") and ntransactionstatus <> "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus();
		List<TrainingParticipants> lstTrainingParticipants = (List<TrainingParticipants>) jdbcTemplate.query(strtp,
				new TrainingParticipants());

		int sparticipantstatus = 0;
		int ncertifiedstatus = 0;

		for (TrainingParticipants objparticipants : lstTrainingParticipants) {
			int ntransactionstatus = objparticipants.getNtransactionstatus();
			sparticipantstatus = ntransactionstatus;
			ncertifiedstatus = objparticipants.getNcertifiedstatus();

		}
		int ntranstatus1 = sparticipantstatus;
		if (ntranstatus == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYTRAININGCOMPLETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else if (ntranstatus1 == Enumeration.TransactionStatus.INVITED.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PARTICIPANTSNOTATTENDED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else if (ncertifiedstatus == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PARTICIPANTSNOTCERTIFIED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			/*
			 * String strqry = "update TrainingCertification set  dmodifieddate ='" +
			 * getCurrentDateTime(userInfo) + "',ntransactionstatus = '" +
			 * Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() +
			 * "' where nstatus = " +
			 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
			 * " and ntrainingcode = " + objCompleteTraining.getNtrainingcode();
			 */

			String strqry = "update TrainingCertification set dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',ntransactionstatus = '"
					+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + "',dcompleteddate= '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo).truncatedTo(ChronoUnit.DAYS) + "',"
					+ "noffsetdcompleteddate='"
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "',ntzcompleteddate="
					+ userInfo.getNtimezonecode() + " " + "where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntrainingcode = "
					+ objCompleteTraining.getNtrainingcode();

			jdbcTemplate.execute(strqry);
		}
		String fromDate = "";
		String toDate = "";
		fromDate = (String) obj.get("fromDate");
		toDate = (String) obj.get("toDate");

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("ntrainingcode", objCompleteTraining.getNtrainingcode());
		List<Object> completedLst = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		objCompleteTraining
				.setNtransactionstatus((short) Enumeration.TransactionStatus.COMPLETED.gettransactionstatus());
		completedLst.add(objCompleteTraining);
		multilingualIDList.add("IDS_COMPLETEDTRAINING");
		auditUtilityFunction.fnInsertAuditAction(completedLst, 1, null, multilingualIDList, userInfo);
		return getTraningUpdateByTechnique(fromDate, toDate, objCompleteTraining.getNtechniquecode(), userInfo, null,
				inputMap);

	}

	@Override
	public Map<String, Object> getParticipantsAccordion(int nparticipantcode, UserInfo userInfo) throws Exception {

		Map<String, Object> rtnMap = new HashMap<String, Object>();
		String yes = commonFunction.getMultilingualMessage("IDS_YES", userInfo.getSlanguagefilename());
		String no = commonFunction.getMultilingualMessage("IDS_NO", userInfo.getSlanguagefilename());
		final String StringQuery = "select  u.sfirstname  || ' ' || u.slastname AS sfullname,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
									+ userInfo.getSlanguagetypecode() + "',"
									+ " ts.jsondata->'stransdisplaystatus'->>'en-US')  as sattendancestatus,tp.ncompetencystatus, "
									+ " case when tp.ncertifiedstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
									+ "  then  '" + yes + "' else '" + no + "'  end as stransdisplaystatuscertified , "
									+ "  case when tp.ncompetencystatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
									+ "  then  '" + yes + "' else '" + no + "'  end as stransdisplaystatuscompotent ,"
									+ "	tp.ncertifiedstatus,tp.ntransactionstatus,tp.ntrainingcode,tp.nusercode,"
									+ "	tp.nparticipantcode from trainingparticipants tp,users u,transactionstatus ts "
									+ "	where u.nusercode=tp.nusercode and ts.ntranscode=tp.ntransactionstatus  and tp.nstatus=1 and"
									+ "	nparticipantcode = " + nparticipantcode;
		rtnMap.put("selectedParticipants", jdbcTemplate.queryForObject(StringQuery, new TrainingParticipants()));
		rtnMap.putAll(getTraineeDocuments(nparticipantcode, 0, userInfo));
		return (rtnMap);

	}

	public ResponseEntity<Object> getInvitedParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String statusval = "select ntransactionstatus from trainingcertification where ntrainingcode="
				+ inputMap.get("ntrainingcode") + "";
		int ntranstatus = jdbcTemplate.queryForObject(statusval, Integer.class);

		if (ntranstatus == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRAININGCOMPLETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String strQuery = "select tp.*,concat(u.sfirstname,' ',u.slastname) as sfullname from trainingparticipants tp,users u "
									+ " where tp.ntrainingcode=" + inputMap.get("ntrainingcode")
									+ " and tp.nusercode=u.nusercode and tp.ntransactionstatus="
									+ Enumeration.TransactionStatus.INVITED.gettransactionstatus() + " and u.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and tp.nsitecode = " + userInfo.getNmastersitecode();

			final List<TrainingParticipants> lstTrainingParticipants = (List<TrainingParticipants>) jdbcTemplate
					.query(strQuery, new TrainingParticipants());

			outputMap.put("InvitedParticipant", lstTrainingParticipants);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> attendTrainingParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnObj = new HashMap<>();
		final ObjectMapper objmapper = new ObjectMapper();

		final List<TrainingParticipants> lstTrainingParticipants = objmapper
				.convertValue(inputMap.get("trainingupdate"), new TypeReference<List<TrainingParticipants>>() {
				});
		final String sUserCode = lstTrainingParticipants.stream()
				.map(trainingparticipants -> (String.valueOf(trainingparticipants.getNusercode())))
				.collect(Collectors.joining(","));

		final String sParticipantCode = lstTrainingParticipants.stream()
				.map(trainingparticipants -> (String.valueOf(trainingparticipants.getNparticipantcode())))
				.collect(Collectors.joining(","));

		final String strQuery = "select nparticipantcode from trainingparticipants " + " where ntrainingcode="
									+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in (" + sUserCode
									+ ") and ntransactionstatus=" + Enumeration.TransactionStatus.ATTENDED.gettransactionstatus()
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<TrainingParticipants> lstvalidate = (List<TrainingParticipants>) jdbcTemplate.query(strQuery,
				new TrainingParticipants());
		if (lstvalidate.size() == 0) {
//			String sUpdateFinal = "";
//			String sParticipantcode="";
			final String sUpdateQuery = "update trainingparticipants set dmodifieddate ='"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = "
										+ Enumeration.TransactionStatus.ATTENDED.gettransactionstatus() + " where  ntrainingcode="
										+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in ( " + sUserCode
										+ ") and  nparticipantcode in (" + sParticipantCode + ")";
//			for (int i = 0; i < lstTrainingParticipants.size(); i++) {
//
//				int nusercode = lstTrainingParticipants.get(i).getNusercode();
//				int nparticipantcode=lstTrainingParticipants.get(i).getNparticipantcode();
//				String addString = "";
//				if (i < lstTrainingParticipants.size() - 1) {
//					addString = ",";
//				}
//				sUpdateQuery = sUpdateQuery + nusercode + addString;
//				sParticipantcode=nparticipantcode+addString;
//
//			}
//			sUpdateFinal = sUpdateQuery + ") and nparticipantcode=("+sParticipantcode+")";

			jdbcTemplate.execute(sUpdateQuery);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYATTENDED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
		returnObj.putAll(getTrainingParticipants(lstTrainingParticipants.get(0).getNtrainingcode(), userInfo));
		returnObj.putAll(getParticipantsAccordion(lstTrainingParticipants.get(0).getNparticipantcode(), userInfo));
	
		final List<String> multilingualIDList = new ArrayList<>();
		List<Object> attendParticipant = new ArrayList<>();
		attendParticipant.add(lstTrainingParticipants);
		multilingualIDList.add("IDS_ATTENDPARTICIPANTS");
		auditUtilityFunction.fnInsertListAuditAction(attendParticipant, 1, null, multilingualIDList, userInfo);
		return new ResponseEntity<Object>(returnObj, HttpStatus.OK);

	}

	public ResponseEntity<Object> getAttendedParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String statusval = "select ntransactionstatus from trainingcertification where ntrainingcode="
				+ inputMap.get("ntrainingcode") + "";
		int ntranstatus = jdbcTemplate.queryForObject(statusval, Integer.class);

		if (ntranstatus == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRAININGCOMPLETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String strQuery = "select tp.*,concat(u.sfirstname,' ',u.slastname) as sfullname from trainingparticipants tp,users u "
							+ " where tp.ntrainingcode=" + inputMap.get("ntrainingcode")
							+ " and tp.nusercode=u.nusercode and tp.ntransactionstatus="
							+ Enumeration.TransactionStatus.ATTENDED.gettransactionstatus() + " and tp.ncertifiedstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " and u.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and tp.nsitecode = " + userInfo.getNmastersitecode();

			final List<TrainingParticipants> lstTrainingParticipants = (List<TrainingParticipants>) jdbcTemplate
					.query(strQuery, new TrainingParticipants());

			outputMap.put("AttendedParticipant", lstTrainingParticipants);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> certifyTrainingParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnObj = new HashMap<>();
		final ObjectMapper objmapper = new ObjectMapper();

		final List<TrainingParticipants> lstTrainingParticipants = objmapper
				.convertValue(inputMap.get("trainingupdate"), new TypeReference<List<TrainingParticipants>>() {
				});
		final String sUserCode = lstTrainingParticipants.stream()
				.map(trainingparticipants -> (String.valueOf(trainingparticipants.getNusercode())))
				.collect(Collectors.joining(","));

		final String strQuery = "select nparticipantcode from trainingparticipants " + " where ntrainingcode="
				+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in (" + sUserCode
				+ ") and ncertifiedstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode = " + userInfo.getNmastersitecode();

		final List<TrainingParticipants> lstvalidate = (List<TrainingParticipants>) jdbcTemplate.query(strQuery,
				new TrainingParticipants());
		if (lstvalidate.size() == 0) {
			String sUpdateFinal = "";
			String sUpdateQuery = "update trainingparticipants set dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,ncertifiedstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " where ntrainingcode="
					+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in ( ";
			for (int i = 0; i < lstTrainingParticipants.size(); i++) {

				int nusercode = lstTrainingParticipants.get(i).getNusercode();
				String addString = "";
				if (i < lstTrainingParticipants.size() - 1) {
					addString = ",";
				}
				sUpdateQuery = sUpdateQuery + nusercode + addString;

			}
			sUpdateFinal = sUpdateQuery + ")";

			jdbcTemplate.execute(sUpdateFinal);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYCERTIFIED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
		returnObj.putAll(getTrainingParticipants(lstTrainingParticipants.get(0).getNtrainingcode(), userInfo));
		returnObj.putAll(getParticipantsAccordion(lstTrainingParticipants.get(0).getNparticipantcode(), userInfo));
		final List<String> multilingualIDList = new ArrayList<>();
		List<Object> certifyParticipant = new ArrayList<>();
		certifyParticipant.add(lstTrainingParticipants);
		multilingualIDList.add("IDS_CERTIFYPARTICIPANTS");
		auditUtilityFunction.fnInsertListAuditAction(certifyParticipant, 1, null, multilingualIDList, userInfo);
		return new ResponseEntity<Object>(returnObj, HttpStatus.OK);

	}

	public ResponseEntity<Object> getCertifiedParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String statusval = "select ntransactionstatus from trainingcertification where ntrainingcode="
				+ inputMap.get("ntrainingcode") + "";
		int ntranstatus = jdbcTemplate.queryForObject(statusval, Integer.class);

		if (ntranstatus == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRAININGCOMPLETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String strQuery = "select tp.*,concat(u.sfirstname,' ',u.slastname) as sfullname from trainingparticipants tp,users u "
							+ " where tp.ntrainingcode=" + inputMap.get("ntrainingcode")
							+ " and tp.nusercode=u.nusercode and tp.ncompetencystatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " and tp.ntransactionstatus="
							+ Enumeration.TransactionStatus.ATTENDED.gettransactionstatus() + " and tp.ncertifiedstatus="
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and u.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and tp.nsitecode = " + userInfo.getNmastersitecode();

			final List<TrainingParticipants> lstTrainingParticipants = (List<TrainingParticipants>) jdbcTemplate
					.query(strQuery, new TrainingParticipants());

			outputMap.put("CompetentParticipant", lstTrainingParticipants);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> competentTrainingParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnObj = new HashMap<>();
		final ObjectMapper objmapper = new ObjectMapper();

		final List<TrainingParticipants> lstTrainingParticipants = objmapper
				.convertValue(inputMap.get("trainingupdate"), new TypeReference<List<TrainingParticipants>>() {
				});
		final String sUserCode = lstTrainingParticipants.stream()
				.map(trainingparticipants -> (String.valueOf(trainingparticipants.getNusercode())))
				.collect(Collectors.joining(","));

		final String strQuery = "select nparticipantcode from trainingparticipants " + " where ntrainingcode="
				+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in (" + sUserCode
				+ ") and ncompetencystatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<TrainingParticipants> lstvalidate = (List<TrainingParticipants>) jdbcTemplate.query(strQuery,
				new TrainingParticipants());
		if (lstvalidate.size() == 0) {
			String sUpdateFinal = "";
			String sUpdateQuery = "update trainingparticipants set dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ncompetencystatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " where ntrainingcode="
					+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in ( ";
			for (int i = 0; i < lstTrainingParticipants.size(); i++) {

				int nusercode = lstTrainingParticipants.get(i).getNusercode();
				String addString = "";
				if (i < lstTrainingParticipants.size() - 1) {
					addString = ",";
				}
				sUpdateQuery = sUpdateQuery + nusercode + addString;

			}
			sUpdateFinal = sUpdateQuery + ")";

			jdbcTemplate.execute(sUpdateFinal);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYCOMPETENT.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
		returnObj.putAll(getTrainingParticipants(lstTrainingParticipants.get(0).getNtrainingcode(), userInfo));
		returnObj.putAll(getParticipantsAccordion(lstTrainingParticipants.get(0).getNparticipantcode(), userInfo));
		final List<String> multilingualIDList = new ArrayList<>();
		List<Object> competentParticipant = new ArrayList<>();
		competentParticipant.add(lstTrainingParticipants);
		multilingualIDList.add("IDS_COMPETENTPARTICIPANTS");
		auditUtilityFunction.fnInsertListAuditAction(competentParticipant, 1, null, multilingualIDList, userInfo);
		return new ResponseEntity<Object>(returnObj, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> editTrainingFile(final TrainingDocuments objTrainingDocumentsFile,
			final UserInfo objUserInfo) throws Exception {
		final String sEditQuery = "select tf.ntrainingdoccode, tf.ntrainingcode, tf.nlinkcode, "
									+ "tf.nattachmenttypecode, tf.sfilename, tf.sfiledesc, tf.nfilesize,"
									+ " tf.ssystemfilename,  lm.jsondata->>'slinkname' as slinkname"
									+ " from trainingdocuments tf, linkmaster lm where lm.nlinkcode = tf.nlinkcode" + " and tf.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.ntrainingdoccode = "
									+ objTrainingDocumentsFile.getNtrainingdoccode();
		final TrainingDocuments objTF = (TrainingDocuments) jdbcUtilityFunction.queryForObject(sEditQuery,
				TrainingDocuments.class, jdbcTemplate);
		if (objTF != null) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public TrainingCertification checKTrainingIsPresent(final int ntrainingcode) throws Exception {
		final String strQuery = "select ntrainingcode from TrainingCertification where ntrainingcode = " + ntrainingcode
				+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final TrainingCertification objTest = (TrainingCertification) jdbcUtilityFunction.queryForObject(strQuery,
				TrainingCertification.class, jdbcTemplate);
		return objTest;
	}

	public TrainingCertification checKTraineeIsPresent(final int nparticipantcode) throws Exception {
		final String strQuery = "select tc.ntrainingcode from TrainingCertification tc,trainingparticipants tp"
								+ "  where tc.ntrainingcode=tp.ntrainingcode and tp.nparticipantcode = " + nparticipantcode
								+ " and  tc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and  tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final TrainingCertification objTest = (TrainingCertification) jdbcUtilityFunction.queryForObject(strQuery,
				TrainingCertification.class, jdbcTemplate);
		return objTest;
	}

	@Override
	public ResponseEntity<Object> viewAttachedTrainingFile(TrainingDocuments objTrainingDocuments, UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		final TrainingCertification objTrainingCertification = checKTrainingIsPresent(
				objTrainingDocuments.getNtrainingcode());
		if (objTrainingCertification != null) {
			String sQuery = "select * from trainingdocuments where ntrainingdoccode = "
					+ objTrainingDocuments.getNtrainingdoccode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final TrainingDocuments objTF = (TrainingDocuments) jdbcUtilityFunction.queryForObject(sQuery,
					TrainingDocuments.class, jdbcTemplate);
			if (objTF != null) {
				if (objTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = fTPUtilityFunction.FileViewUsingFtp(objTF.getSsystemfilename(), -1, objUserInfo, "", "");// Folder
																													// Name
																													// -
																													// master
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ objTF.getNlinkcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,
							jdbcTemplate);
					map.put("AttachLink", objlinkmaster.getSlinkname() + objTF.getSfilename());
					objTrainingDocuments.setScreateddate(null);
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList
						.add(objTrainingDocuments.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWTRAININGDOCUMENTFILE"
								: "IDS_VIEWTRAININGDOCUMENTLINK");
				lstObject.add(objTrainingDocuments);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
			}
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> editTraineeFile(TraineeDocuments objTraineeFile, UserInfo userInfo) throws Exception {
		final String sEditQuery = "select tf.ntraineedoccode, tf.nparticipantcode, tf.nlinkcode, "
				+ "tf.nattachmenttypecode, tf.sfilename, tf.sfiledesc, tf.nfilesize,"
				+ " tf.ssystemfilename,  lm.jsondata->>'slinkname' as slinkname"
				+ " from traineedocuments tf, linkmaster lm where lm.nlinkcode = tf.nlinkcode" + " and tf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.ntraineedoccode = "
				+ objTraineeFile.getNtraineedoccode();
		final TraineeDocuments objTF = (TraineeDocuments) jdbcUtilityFunction.queryForObject(sEditQuery,
				TraineeDocuments.class, jdbcTemplate);
		if (objTF != null) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> viewAttachedTraineeFile(TraineeDocuments objTraineeFile, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		final TrainingCertification objTrainingCertification = checKTraineeIsPresent(
				objTraineeFile.getNparticipantcode());
		if (objTrainingCertification != null) {
			String sQuery = "select * from traineedocuments where ntraineedoccode = "
					+ objTraineeFile.getNtraineedoccode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final TrainingDocuments objTF = (TrainingDocuments) jdbcUtilityFunction.queryForObject(sQuery,
					TrainingDocuments.class, jdbcTemplate);
			if (objTF != null) {
				if (objTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = fTPUtilityFunction.FileViewUsingFtp(objTF.getSsystemfilename(), -1, userInfo, "", "");// Folder
																												// Name
																												// -
																												// master
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ objTF.getNlinkcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,
							jdbcTemplate);
					map.put("AttachLink", objlinkmaster.getSlinkname() + objTF.getSfilename());
					objTraineeFile.setScreateddate(null);
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList
						.add(objTraineeFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWTRAINEEDOCUMENTFILE"
								: "IDS_VIEWTRAINEEDOCUMENTLINK");
				lstObject.add(objTraineeFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
			}
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateTrainingDoc(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<TrainingDocuments> lstTrainingDocumentsFile = objMapper
				.readValue(request.getParameter("trainingdoc"), new TypeReference<List<TrainingDocuments>>() {
				});
		if (lstTrainingDocumentsFile != null && lstTrainingDocumentsFile.size() > 0) {
			final TrainingDocuments objTrainingDocumentsFile = lstTrainingDocumentsFile.get(0);
			final TrainingCertification objTrainingDocuments = checKTrainingIsPresent(
					objTrainingDocumentsFile.getNtrainingcode());

			if (objTrainingDocuments != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objTrainingDocumentsFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = fTPUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}

				if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
					final String sQuery = "select * from trainingdocuments where ntrainingdoccode = "
							+ objTrainingDocumentsFile.getNtrainingdoccode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final TrainingDocuments objTF = (TrainingDocuments) jdbcUtilityFunction.queryForObject(sQuery,
							TrainingDocuments.class, jdbcTemplate);

//					final String sCheckDefaultQuery = "select * from trainingdocuments where ntrainingcode = "
//							+ objTrainingDocumentsFile.getNtrainingcode() + " and ntrainingdoccode!="
//							+ objTrainingDocumentsFile.getNtrainingdoccode() + " and nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//					final List<TrainingDocuments> lstDefTrainingDocumentsFiles = (List<TrainingDocuments>) jdbcTemplate
//							.query(sCheckDefaultQuery, new TrainingDocuments());

					if (objTF != null) {
						String ssystemfilename = "";
						if (objTrainingDocumentsFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
								.gettype()) {
							ssystemfilename = objTrainingDocumentsFile.getSsystemfilename();
						}

						final String sUpdateQuery = "update trainingdocuments set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objTrainingDocumentsFile.getSfilename()) + "',"
								+ " sfiledesc=N'"
								+ stringUtilityFunction.replaceQuote(objTrainingDocumentsFile.getSfiledesc())
								+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
								+ objTrainingDocumentsFile.getNattachmenttypecode() + ", nlinkcode="
								+ objTrainingDocumentsFile.getNlinkcode() + "," + " nfilesize = "
								+ objTrainingDocumentsFile.getNfilesize() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
								+ "  where ntrainingdoccode = " + objTrainingDocumentsFile.getNtrainingdoccode();
						objTrainingDocumentsFile.setDcreateddate(objTF.getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);

						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList
								.add(objTrainingDocumentsFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
										.gettype() ? "IDS_EDITTRAININGDOCUMENTFILE" : "IDS_EDITTRAININGDOCUMENTLINK");
						lstOldObject.add(objTF);

						auditUtilityFunction.fnInsertAuditAction(lstTrainingDocumentsFile, 2, lstOldObject,
								multilingualIDList, objUserInfo);
						return new ResponseEntity<Object>(
								getTrainingDocument(objTrainingDocumentsFile.getNtrainingcode(), 0, objUserInfo),
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
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TRANINGALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateTraineeDoc(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<TraineeDocuments> lstTraineeDocumentsFile = objMapper.readValue(request.getParameter("trainieedoc"),
				new TypeReference<List<TraineeDocuments>>() {
				});
		if (lstTraineeDocumentsFile != null && lstTraineeDocumentsFile.size() > 0) {
			final TraineeDocuments objTraineeDocumentsFile = lstTraineeDocumentsFile.get(0);
			final TrainingCertification objTrainingDocuments = checKTrainingIsPresent(
					objTraineeDocumentsFile.getNtrainingcode());

			if (objTrainingDocuments != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objTraineeDocumentsFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = fTPUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}

				if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
					final String sQuery = "select * from traineedocuments where ntraineedoccode = "
							+ objTraineeDocumentsFile.getNtraineedoccode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final TraineeDocuments objTF = (TraineeDocuments) jdbcUtilityFunction.queryForObject(sQuery,
							TraineeDocuments.class, jdbcTemplate);

//					final String sCheckDefaultQuery = "select * from trainingdocuments where ntrainingcode = "
//							+ objTrainingDocumentsFile.getNtrainingcode() + " and ntrainingdoccode!="
//							+ objTrainingDocumentsFile.getNtrainingdoccode() + " and nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//					final List<TrainingDocuments> lstDefTrainingDocumentsFiles = (List<TrainingDocuments>) jdbcTemplate
//							.query(sCheckDefaultQuery, new TrainingDocuments());

					if (objTF != null) {
						String ssystemfilename = "";
						if (objTraineeDocumentsFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
								.gettype()) {
							ssystemfilename = objTraineeDocumentsFile.getSsystemfilename();
						}

						final String sUpdateQuery = "update traineedocuments set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objTraineeDocumentsFile.getSfilename()) + "',"
								+ " sfiledesc=N'"
								+ stringUtilityFunction.replaceQuote(objTraineeDocumentsFile.getSfiledesc())
								+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
								+ objTraineeDocumentsFile.getNattachmenttypecode() + ", nlinkcode="
								+ objTraineeDocumentsFile.getNlinkcode() + "," + " nfilesize = "
								+ objTraineeDocumentsFile.getNfilesize() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
								+ "  where ntraineedoccode = " + objTraineeDocumentsFile.getNtraineedoccode();
						objTraineeDocumentsFile.setDcreateddate(objTF.getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);

						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList
								.add(objTraineeDocumentsFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
										.gettype() ? "IDS_EDITTRAINEEDOCUMENTFILE" : "IDS_EDITTRAINEEDOCUMENTLINK");
						lstOldObject.add(objTF);

						auditUtilityFunction.fnInsertAuditAction(lstTraineeDocumentsFile, 2, lstOldObject,
								multilingualIDList, objUserInfo);
						return new ResponseEntity<Object>(getTraineeDocuments(
								lstTraineeDocumentsFile.get(0).getNparticipantcode(), 0, objUserInfo), HttpStatus.OK);
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
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TRANINGALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}
}
