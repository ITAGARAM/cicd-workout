package com.agaramtech.qualis.compentencemanagement.service.TrainingCertificate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.agaramtech.qualis.compentencemanagement.model.Technique;
import com.agaramtech.qualis.compentencemanagement.model.TrainingCategory;
import com.agaramtech.qualis.compentencemanagement.model.TrainingCertification;
import com.agaramtech.qualis.compentencemanagement.model.TrainingParticipants;
import com.agaramtech.qualis.compentencemanagement.service.trainingcategory.TrainingCategoryDAO;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TrainingCertificateDAOImpl implements TrainingCertificateDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrainingCertificateDAOImpl.class);

	final TrainingCategoryDAO TraningCategoryDAO;

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTrainingCertificate(Integer ntrainingcode, String fromDate, String toDate,
			final UserInfo userInfo, String currentUIDate) throws Exception {
		TrainingCertification selectedTrainingCertificate = null;
		final ObjectMapper objMapper = new ObjectMapper();
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

		final String strQuery = "select  tc.ntrainingcategorycode,tc.ntechniquecode,tc.ntrainingcode,to_char(tc.dtrainingdatetime, '"
								+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as strainingdatetime,"
								+ " to_char(tc.dtrainingexpirydate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
								+ "') as strainingexpirydate," + " coalesce(tss.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " tss.jsondata->'stransdisplaystatus'->>'en-US') as strainingexpiryneed,tc.ntrainingexpiryvalue,"
								+ " case when tc.ntrainingexpiryperiod = -1 then '-' else coalesce(p1.jsondata->'speriodname'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " p1.jsondata->'speriodname'->>'en-US') END as speriodname , " + "to_char(tc.dregistereddate, '"
								+ userInfo.getSpgsitedatetime().replace("'T'", " ")
								+ "') as sregistereddate,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,tc.ntransactionstatus ,"
								+ " tc.strainingtopic,tec.stechniquename,trc.strainingcategoryname,tc.strainername,tc.strainingvenue,"
								+ " case when tc.scomments = '' then '-' else tc.scomments end,tc.noffsetdtrainingdatetime,tc.noffsetdcompleteddate,tc.noffsetdconducteddate,tc.noffsetdregistereddate "
								+ " from TrainingCertification tc,TrainingCategory trc,Technique tec,transactionstatus ts,period p1,transactionstatus tss  "
								+ " where tc.nsitecode=" + userInfo.getNmastersitecode()
								+ " and p1.nperiodcode=tc.ntrainingexpiryperiod and tc.ntrainingcategorycode = trc.ntrainingcategorycode and tc.ntechniquecode = tec.ntechniquecode and tc.ntransactionstatus=ts.ntranscode"
								+ " and tc.ntrainingexpiryneed=tss.ntranscode and tc.nstatus=trc.nstatus and trc.nstatus=tec.nstatus and tec.nstatus=ts.nstatus"
								+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tss.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dregistereddate " + " between '"
								+ fromDate + "' " + " and  '" + toDate + "' order by tc.ntrainingcode ";
		LOGGER.info("getTrainingCategory-->" + strQuery);

//		TrainingCertification traininglst = (TrainingCertification) findBySingleObjectPlainSql(strQuery, TrainingCertification.class);
		final List<TrainingCertification> traininglst = (List<TrainingCertification>) jdbcTemplate.query(strQuery,
				new TrainingCertification());

		final List<TrainingCertification> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(traininglst, Arrays.asList("strainingdatetime"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<TrainingCertification>>() {
				});

		if (lstUTCConvertedDate == null) {
			outputMap.put("SelectedTrainingCertificate", null);
			outputMap.put("TrainingParticipants", lstUTCConvertedDate);
			outputMap.put("TrainingCertificate", lstUTCConvertedDate);
//			outputMap.put("TrainingCertificate", lstUTCConvertedDate);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
//			final ObjectMapper mapper = new ObjectMapper();
			final List<String> columnList = new ArrayList<>();
			columnList.add("sdisplaystatus");

			final List<TrainingCertification> modifiedtrainingList = objMapper.convertValue(commonFunction
					.getMultilingualMessageList(lstUTCConvertedDate, columnList, userInfo.getSlanguagefilename()),
					new TypeReference<List<TrainingCertification>>() {
					});

			outputMap.put("TrainingCertificate", modifiedtrainingList);
			if (!lstUTCConvertedDate.isEmpty()) {
				if (ntrainingcode == null) {
					selectedTrainingCertificate = ((TrainingCertification) modifiedtrainingList
							.get(modifiedtrainingList.size() - 1));
					ntrainingcode = selectedTrainingCertificate.getNtrainingcode();
				} else {
					TrainingCertification lstTrainingCertificate = ((TrainingCertification) getActiveTrainingCertificateById(
							ntrainingcode, userInfo).getBody());
					if (lstTrainingCertificate != null && lstTrainingCertificate.getScomments().isEmpty()) {
						lstTrainingCertificate.setScomments("-");
					}
					selectedTrainingCertificate = lstTrainingCertificate;
				}
			}
		}
		if (selectedTrainingCertificate == null && !traininglst.isEmpty()) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedTrainingCertificate", selectedTrainingCertificate);

			outputMap.putAll((Map<String, Object>) getTrainingParticipants(ntrainingcode, userInfo).getBody());

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@SuppressWarnings("unused")
	private TrainingCertification getTrainingCertificateByName(final String strainingtopic, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select strainingtopic  from trainingcertification where strainingtopic = N'"
				+ stringUtilityFunction.replaceQuote(strainingtopic) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;// users.getnmastersitecode();

		return (TrainingCertification) jdbcUtilityFunction.queryForObject(strQuery, TrainingCertification.class,
				jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> createTrainingCertificate(TrainingCertification trainingcertification,
			UserInfo userInfo, String fromDate, String toDate, String currentUIDate) throws Exception {

		final String sQueryLock = " lock  table locktrainingcertification "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTrainingCertificationList = new ArrayList<>();

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		trainingcertification.setStrainingdatetime(dateUtilityFunction
				.instantDateToString(trainingcertification.getDtrainingdatetime()).replace("T", " ").replace("Z", ""));

		final TrainingCertification convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(trainingcertification,
						Arrays.asList("strainingdatetime"), Arrays.asList("ntztrainingdate"), true, userInfo),
				new TypeReference<TrainingCertification>() {
				});


		final String sGetSeqNoQuery = "select nsequenceno from seqnocompetencemanagement where stablename = 'trainingcertification';";
		int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);
		trainingcertification.setNtrainingcode(nSeqNo++);

		final String sInsertQuery = "insert into trainingcertification (ntrainingcode, ntechniquecode, "
									+ " ntrainingcategorycode, ntztrainingdate,ntransactionstatus,strainername,strainingtopic,strainingvenue"
									+ ",strainingmonth,scomments,dcompleteddate,dconducteddate,dtrainingdatetime,dregistereddate,"
									+ "noffsetdcompleteddate,noffsetdconducteddate,noffsetdtrainingdatetime,noffsetdregistereddate,"
									+ "ntzcompleteddate,ntzconducteddate,ntzregistereddate,dmodifieddate,nsitecode, nstatus,ntrainingexpiryneed,ntrainingexpiryvalue,ntrainingexpiryperiod) "
									+ "values (" + nSeqNo + "," + trainingcertification.getNtechniquecode() + ","
									+ trainingcertification.getNtrainingcategorycode() + "," + trainingcertification.getNtztrainingdate()
									+ "," + trainingcertification.getNtransactionstatus() + ",N'"
									+ stringUtilityFunction.replaceQuote(trainingcertification.getStrainername()) + "', N'"
									+ stringUtilityFunction.replaceQuote(trainingcertification.getStrainingtopic()) + "',  N'"
									+ stringUtilityFunction.replaceQuote(trainingcertification.getStrainingvenue()) + "', "
									+ "NULL,case when N'" + stringUtilityFunction.replaceQuote(trainingcertification.getScomments())
									+ "'='null' then '' else N'" + stringUtilityFunction.replaceQuote(trainingcertification.getScomments())
									+ "' end,NULL,NULL,'" + convertedObject.getStrainingdatetime() + "','"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',0,0,"
									+ convertedObject.getNoffsetdtrainingdatetime() + ","
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
									+ userInfo.getNtimezonecode() + "," + userInfo.getNtimezonecode() + "," + userInfo.getNtimezonecode()
									+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",4,0,-1" + ");";
		jdbcTemplate.execute(sInsertQuery);

		final String sUpdateSeqNoQuery = "update seqnocompetencemanagement set nsequenceno = " + nSeqNo
				+ " where stablename = 'trainingcertification';";
		jdbcTemplate.execute(sUpdateSeqNoQuery);

		trainingcertification.setNtrainingcode(nSeqNo);
		savedTrainingCertificationList.add(trainingcertification);

		multilingualIDList.add("IDS_ADDTRAININGCERTIFICATE");

		auditUtilityFunction.fnInsertAuditAction(savedTrainingCertificationList, 1, null, multilingualIDList, userInfo);

		// status code:200
		// return new ResponseEntity<>(supplier, HttpStatus.OK);
		return getTrainingCertificate(null, fromDate, toDate, userInfo, null);

	}

	@Override
	public ResponseEntity<Object> getActiveTrainingCertificateById(final int ntrainingcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select  a.ntechniquecode,a.ntrainingcode,to_char(a.dtrainingdatetime, '"
								+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as strainingdatetime,"
								+ " to_char(a.dtrainingexpirydate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
								+ "') as strainingexpirydate," + " coalesce(tss.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " tss.jsondata->'stransdisplaystatus'->>'en-US') as strainingexpiryneed,a.ntrainingexpiryvalue,"
								+ "  case when a.ntrainingexpiryperiod = -1 then '-' else coalesce(p.jsondata->'speriodname'->>'"
								+ userInfo.getSlanguagetypecode() + "'," + " p.jsondata->'speriodname'->>'en-US') end as speriodname , "
								+ "to_char(a.dregistereddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
								+ "') as sregistereddate,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as  sdisplaystatus,a.ntransactionstatus ,"
								+ " a.strainingtopic,tq.stechniquename,tc.strainingcategoryname,a.strainername,a.strainingvenue,"
								+ " a.scomments,tz.stimezoneid, tq.stechniquename, tc.ntrainingcategorycode, tc.strainingcategoryname, a.noffsetdtrainingdatetime, a.ntztrainingdate "
								+ " from trainingcertification a, transactionstatus ts,transactionstatus tss,trainingcategory tc,Technique tq,period p, "
								+ " timezone tz where a.ntransactionstatus = ts.ntranscode and p.nperiodcode = a.ntrainingexpiryperiod and a.ntrainingexpiryneed = tss.ntranscode and ntrainingcode > 0 "
								+ " and tc.nsitecode = a.nsitecode and tc.nstatus = a.nstatus and a.ntztrainingdate=tz.ntimezonecode "
								+ " and tc.ntrainingcategorycode = a.ntrainingcategorycode and a.ntechniquecode = tq.ntechniquecode "
								+ " and a.nstatus = tq.nstatus and tz.nstatus = ts.nstatus " + " and ts.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode="
								+ userInfo.getNmastersitecode() + " and a.ntrainingcode = " + ntrainingcode;

		final ObjectMapper objMapper = new ObjectMapper();

		final List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(
				(List<TrainingCertification>) jdbcTemplate.query(strQuery, new TrainingCertification()),
				Arrays.asList("strainingdatetime"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null,
				false);

		final List<TrainingCertification> objTrainingCertification = objMapper.convertValue(lstUTCConvertedDate,
				new TypeReference<List<TrainingCertification>>() {
				});
		return new ResponseEntity<>(
				(TrainingCertification) (objTrainingCertification.size() > 0 ? objTrainingCertification.get(0) : null),
				HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> conductTrainingCertificate(final TrainingCertification trainingcertificate,
			String fromDate, String toDate, final UserInfo userInfo) throws Exception {

		final TrainingCertification trainingcertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
				trainingcertificate.getNtrainingcode(), userInfo).getBody());

		if (trainingcertificateByID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if (((TrainingCertification) trainingcertificateByID)
					.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED.gettransactionstatus()) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYCONDUCTED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTrainingCertificateList = new ArrayList<>();

				final String select = "select nparticipantcode from  trainingparticipants where " + "  ntrainingcode="
										+ trainingcertificate.getNtrainingcode() + " and nstatus= "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus != "
										+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus();

				final List<TrainingParticipants> lst01 = (List<TrainingParticipants>) jdbcTemplate.query(select,
						new TrainingParticipants());

				if (lst01 != null && !lst01.isEmpty()) {

					final String trainingParticipant = "select nparticipantcode from  trainingparticipants "
													+ " where ntrainingcode=" + trainingcertificate.getNtrainingcode()
													+ " and ntransactionstatus <> "
													+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " and nstatus= "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
													+ userInfo.getNmastersitecode();

					List<TrainingParticipants> particpantsList = (List<TrainingParticipants>) jdbcTemplate
							.query(trainingParticipant, new TrainingParticipants());

					final String selectString = "select nparticipantcode from  trainingparticipants"
													+ " where  ntransactionstatus = "
													+ Enumeration.TransactionStatus.INVITED.gettransactionstatus() + " and nstatus= "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
													+ userInfo.getNmastersitecode() + " and ntrainingcode="
													+ trainingcertificate.getNtrainingcode();

					final List<TrainingParticipants> invitedParticpantsList = (List<TrainingParticipants>) jdbcTemplate
							.query(selectString, new TrainingParticipants());

					if (particpantsList.size() == invitedParticpantsList.size())
					{
						short ntrainingvalue = 0;
						short ntrainingperiod = -1;
						if (trainingcertificate.getNtrainingexpiryneed() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							ntrainingvalue = trainingcertificate.getNtrainingexpiryvalue();
							ntrainingperiod = trainingcertificate.getNtrainingexpiryperiod();
						}
						final String updateQueryString = "update trainingcertification set ntransactionstatus = "
														+ Enumeration.TransactionStatus.CONDUCTED.gettransactionstatus() + ", dconducteddate= '"
														+ dateUtilityFunction.getCurrentDateTime(userInfo).truncatedTo(ChronoUnit.DAYS)
														+ "',noffsetdconducteddate="
														+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
														+ ",ntzconducteddate=" + userInfo.getNtimezonecode() + ", dmodifieddate ='"
														+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ",ntrainingexpiryneed = "
														+ trainingcertificate.getNtrainingexpiryneed() + ",noffsetdtrainingexpirydate="
														+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
														+ ",ntztrainingexpirydate=" + userInfo.getNtimezonecode() + ", ntrainingexpiryvalue ="
														+ ntrainingvalue + ",ntrainingexpiryperiod = " + ntrainingperiod
														+ " where ntrainingcode=" + trainingcertificate.getNtrainingcode();

						jdbcTemplate.execute(updateQueryString);
						// Instant dsconductDate=getCurrentDateTime(userInfo);
						String expirystring = "";
						if (trainingcertificate.getNtrainingexpiryneed() == 3) {
							expirystring = "update trainingcertification set dtrainingexpirydate = case when "
												+ trainingcertificate.getNtrainingexpiryperiod()
												+ " = 4 then dconducteddate + make_interval(days => "
												+ trainingcertificate.getNtrainingexpiryvalue() + ")" + " when "
												+ trainingcertificate.getNtrainingexpiryperiod()
												+ " = 3 then dconducteddate + make_interval(weeks => "
												+ trainingcertificate.getNtrainingexpiryvalue() + " )" + " when "
												+ trainingcertificate.getNtrainingexpiryperiod()
												+ " = 5 then dconducteddate + make_interval(months => "
												+ trainingcertificate.getNtrainingexpiryvalue() + " ) " + "	when "
												+ trainingcertificate.getNtrainingexpiryperiod()
												+ " = 6 then dconducteddate + make_interval(years => "
												+ trainingcertificate.getNtrainingexpiryvalue() + " ) end "
												+ " where ntrainingcode=" + trainingcertificate.getNtrainingcode();
						} else {
							expirystring = "update trainingcertification set dtrainingexpirydate = null "
									+ " where ntrainingcode=" + trainingcertificate.getNtrainingcode();
						}
						jdbcTemplate.execute(expirystring);

						trainingcertificate.setNtransactionstatus(
								(short) Enumeration.TransactionStatus.CONDUCTED.gettransactionstatus());

						savedTrainingCertificateList.add(trainingcertificate);
						multilingualIDList.add("IDS_CONDUCTEDTRAINING");

						auditUtilityFunction.fnInsertAuditAction(savedTrainingCertificateList, 2,
								Arrays.asList(trainingcertificateByID), multilingualIDList, userInfo);

						// status code:200
						return getTrainingCertificate(trainingcertificate.getNtrainingcode(), fromDate, toDate,
								userInfo, null);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOPARTICIPANTSINVITED",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDPARTICIPANTS",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}

		}

	}

	@Override
	public ResponseEntity<Object> validateConductAndTrainingDate(final TrainingCertification trainingcertificate,
			String fromDate, String toDate, final UserInfo userInfo) throws Exception {

		Instant dCurrentDate = dateUtilityFunction.getCurrentDateTime(userInfo).truncatedTo(ChronoUnit.DAYS);
		TrainingCertification trainingcertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
				trainingcertificate.getNtrainingcode(), userInfo).getBody());
		if (trainingcertificateByID != null
				&& trainingcertificateByID.getNtransactionstatus() != Enumeration.TransactionStatus.COMPLETED
						.gettransactionstatus()
				&& trainingcertificateByID.getNtransactionstatus() != Enumeration.TransactionStatus.CONDUCTED
						.gettransactionstatus()
				&& trainingcertificateByID.getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED
						.gettransactionstatus()) {

			Instant dTrainigDate = Instant.parse(trainingcertificate.getStemptrainingdatetime())
					.truncatedTo(ChronoUnit.DAYS);

			if (!dTrainigDate.isBefore(dCurrentDate) && dTrainigDate.isAfter(dCurrentDate)) // ||
																							// dTrainigDate.equals(dCurrentDate)
			{
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TRAININGCONDUCTDATE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String select = "select nparticipantcode from  trainingparticipants where " + "  ntrainingcode="
					+ trainingcertificate.getNtrainingcode() + " and nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus != "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final List<TrainingParticipants> lst01 = (List<TrainingParticipants>) jdbcTemplate.query(select,
					new TrainingParticipants());

			if (lst01 != null && !lst01.isEmpty()) {

				final String selectStringtrain = "select nparticipantcode from  trainingparticipants "
						+ " where ntrainingcode=" + trainingcertificate.getNtrainingcode()
						+ " and ntransactionstatus <> " + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
						+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode();

				final List<TrainingParticipants> particpantsList = (List<TrainingParticipants>) jdbcTemplate
						.query(selectStringtrain, new TrainingParticipants());

				final String selectString = "select nparticipantcode from  trainingparticipants"
						+ " where  ntransactionstatus = " + Enumeration.TransactionStatus.INVITED.gettransactionstatus()
						+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntrainingcode=" + trainingcertificate.getNtrainingcode();

				final List<TrainingParticipants> invitedParticpantsList = (List<TrainingParticipants>) jdbcTemplate
						.query(selectString, new TrainingParticipants());

				if (particpantsList.size() == invitedParticpantsList.size())

				{

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOPARTICIPANTSINVITED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ADDPARTICIPANTS", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

			return getTrainingCertificate(trainingcertificate.getNtrainingcode(), fromDate, toDate, userInfo, null);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					trainingcertificateByID == null ? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
							: trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
									.gettransactionstatus()
											? "IDS_TRAININGALREADYCOMPLETED"
											: trainingcertificateByID
													.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
															.gettransactionstatus() ? "IDS_TRAININGALREADYCONDUCTED"
																	: "IDS_TRAININGALREADYCANCELED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public TrainingParticipants getActiveTrainingParticipantsById(final int nparticipantcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select u.sfirstname || ' ' || u.slastname fullname, tc.strainingtopic,tc.strainername,tp.nparticipantcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,tp.ntransactionstatus  from trainingparticipants tp, trainingcertification tc,"
								+ " transactionstatus ts , users u where tp.ntransactionstatus=ts.ntranscode and tc.ntrainingcode=tp.ntrainingcode and tp.nusercode=u.nusercode"
								+ " and tp.nstatus = tc.nstatus and tc.nstatus=ts.nstatus and ts.nstatus=u.nstatus and u.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nparticipantcode = "
								+ nparticipantcode + " and tp.nsitecode=" + userInfo.getNmastersitecode();

		return (TrainingParticipants) jdbcUtilityFunction.queryForObject(strQuery, TrainingParticipants.class,
				jdbcTemplate);
	}

	public ResponseEntity<Object> getTrainingParticipants(final Integer ntrainingcode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strQuery = "select  u.sfirstname || ' ' || u.slastname AS sfullname,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,tp.ncompetencystatus,tp.ncertifiedstatus,tp.ntransactionstatus,tp.ntrainingcode,tp.nusercode,"
								+ " tp.nparticipantcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,to_char(tp.dmodifieddate,'"
								+ userInfo.getSpgsitedatetime().replace("'T'", " ")
								+ "') as smodifieddate from trainingparticipants tp,users u,transactionstatus ts "
								+ " where u.nusercode=tp.nusercode and ts.ntranscode=tp.ntransactionstatus "
								+ " and tp.nstatus=u.nstatus and u.nstatus=ts.nstatus and ts.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntrainingcode = " + ntrainingcode
								 + " and tp.nsitecode=" + userInfo.getNmastersitecode()
								 + " order by tp.nparticipantcode";

		final List<TrainingParticipants> mahParticipantsList = (List<TrainingParticipants>) jdbcTemplate.query(strQuery,
				new TrainingParticipants());

		final ObjectMapper mapper = new ObjectMapper();

		final List<String> columnList = new ArrayList<>();
		columnList.add("sdisplaystatus");

		final List<TrainingParticipants> modifiedmahParticipantsList = mapper.convertValue(commonFunction
				.getMultilingualMessageList(mahParticipantsList, columnList, userInfo.getSlanguagefilename()),
				new TypeReference<List<TrainingParticipants>>() {
				});

		outputMap.put("TrainingParticipants", modifiedmahParticipantsList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createTrainingParticipants(List<TrainingParticipants> trainingparticipants,
			final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();

		int ntrainingcode = trainingparticipants.get(0).getNtrainingcode();

		TrainingCertification trainingcertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
				ntrainingcode, userInfo).getBody());
		if (trainingcertificateByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRAININGDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if ((((TrainingCertification) trainingcertificateByID)
					.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED.gettransactionstatus())
					|| (((TrainingCertification) trainingcertificateByID)
							.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
									.gettransactionstatus())) {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
								.gettransactionstatus() ? "IDS_TRAININGALREADYCONDUCTED"
										: "IDS_TRAININGALREADYCANCELED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {

				final String sQueryLock = " lock  table trainingparticipants "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(sQueryLock);

				final String sGetSeqNoQuery = "select nsequenceno from seqnocompetencemanagement where stablename = 'trainingparticipants';";
				int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);

				String addstring = "";
				String sInsertQuery = " insert into trainingparticipants (nparticipantcode,ntrainingcode,nusercode,ncertifiedstatus,ncompetencystatus,ntransactionstatus,dmodifieddate,nsitecode,nstatus) values ";

				for (int i = 0; i < trainingparticipants.size(); i++) {

					String addstring1 = " ";
					nSeqNo++;
					ntrainingcode = trainingparticipants.get(i).getNtrainingcode();
					final int nusercode = trainingparticipants.get(i).getNusercode();
					if (i < trainingparticipants.size() - 1) {
						addstring1 = ",";
					}

					addstring = "( " + nSeqNo + "," + ntrainingcode + "," + nusercode + ",4,4,"
							+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1,"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;

					trainingparticipants.get(i).setNparticipantcode(nSeqNo);
					sInsertQuery = sInsertQuery + addstring;
				}
				jdbcTemplate.execute(sInsertQuery);

				final String sUpdateSeqNoQuery = "update seqnocompetencemanagement set nsequenceno =" + nSeqNo
						+ " where stablename = 'trainingparticipants';";
				jdbcTemplate.execute(sUpdateSeqNoQuery);
				final String sAuditQuery = "select tp.*,u.nusercode,u.sfirstname ||' '|| u.slastname as susername "
											+ " from trainingparticipants tp,users u " + " where ntrainingcode=" + ntrainingcode
											+ " and u.nusercode=tp.nusercode " + " and tp.nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and u.nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nsitecode="
											+ userInfo.getNmastersitecode();
				// jdbcTemplate.execute(sAuditQuery);
				List<TrainingParticipants> invitedParticpantsList = (List<TrainingParticipants>) jdbcTemplate
						.query(sAuditQuery, new TrainingParticipants());

				multilingualIDList.add("IDS_ADDPARTICIPANTS");
				List<Object> insertParticipants = new ArrayList<>();
				insertParticipants.add(invitedParticpantsList);
				auditUtilityFunction.fnInsertListAuditAction(insertParticipants, 1, null, multilingualIDList, userInfo);

				return getTrainingParticipants(ntrainingcode, userInfo);
			}
		}
	}

	public ResponseEntity<Object> getSectionUsers(final int nsectioncode, final int ntrainingcode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strQuery = "select distinct u.nusercode,u.sfirstname || ' ' || u.slastname as sfullname from labsection lb, sectionusers su, users u  where "
								+ "lb.nlabsectioncode=su.nlabsectioncode and su.nusercode=u.nusercode  and lb.nstatus=1  and su.nstatus=1 "
								+ " and  su.nusercode not in(select nusercode from trainingparticipants where ntrainingcode="
								+ ntrainingcode + " and ntransactionstatus<>"
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")  and lb.nsectioncode="
								+ nsectioncode;

		final List<TrainingParticipants> mahSectionusersList = (List<TrainingParticipants>) jdbcTemplate.query(strQuery,
				new TrainingParticipants());

		outputMap.put("SectionUsers", mahSectionusersList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

//	@Override
//	public ResponseEntity<Object> inviteTrainingParticipants(TrainingParticipants trainingparticipants,
//			UserInfo userInfo) throws Exception {
//
//		final TrainingCertification trainingcertificateByID = getActiveTrainingCertificateById(
//				trainingparticipants.getNtrainingcode(), userInfo);
//
//		if (trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED.gettransactionstatus()) {
//
//			// status code:417
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage("IDS_TRAININGCANCELED", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
//		} else {
//
//			final TrainingParticipants trainingparticipantsByID = getActiveTrainingParticipantsById(
//					trainingparticipants.getNparticipantcode(), userInfo);
//
//			if (trainingparticipantsByID.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
//					.gettransactionstatus()) {
//
//				// status code:417
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYCANCELED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//			} else {
//
//				if (trainingparticipantsByID.getNtransactionstatus() == Enumeration.TransactionStatus.INVITED
//						.gettransactionstatus()) {
//
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYINVITED",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//				} else {
//
//					final List<String> multilingualIDList = new ArrayList<>();
//					final List<Object> savedtrainingparticipantsList = new ArrayList<>();
//
//					final String updateQueryString = "update trainingparticipants set ntransactionstatus = "
//							+ Enumeration.TransactionStatus.INVITED.gettransactionstatus() + " where nparticipantcode = "
//							+ trainingparticipants.getNparticipantcode();
//
//					jdbcTemplate.execute(updateQueryString);
//
//					trainingparticipants
//							.setNtransactionstatus((short) Enumeration.TransactionStatus.INVITED.gettransactionstatus());
//
//					savedtrainingparticipantsList.add(trainingparticipants);
//					multilingualIDList.add("IDS_INVITEPARTICIPANTS");
//
//					fnInsertAuditAction(savedtrainingparticipantsList, 2, Arrays.asList(trainingparticipantsByID),
//							multilingualIDList, userInfo);
//
//					// status code:200
//					return getTrainingParticipants(trainingparticipants.getNtrainingcode(), userInfo);
//				}
//			}
//		}
//
//	}

	@Override
	public ResponseEntity<Object> cancelTrainingCertificate(final TrainingCertification trainingcertification,
			String fromDate, String toDate, final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTrainingCertificationList = new ArrayList<>();

		TrainingCertification trainingcertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
				trainingcertification.getNtrainingcode(), userInfo).getBody());
//		if(trainingcertificateByID != null && trainingcertificateByID.getNtransactionstatus() != Enumeration.TransactionStatus.COMPLETED.gettransactionstatus())
//		{
		if ((trainingcertificateByID == null)
				|| (trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
						.gettransactionstatus())
				|| (trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
						.gettransactionstatus())
				|| (trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
						.gettransactionstatus())) {

			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					trainingcertificateByID == null ? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
							: trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
									.gettransactionstatus()
											? "IDS_TRAININGALREADYCOMPLETED"
											: trainingcertificateByID
													.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
															.gettransactionstatus() ? "IDS_TRAININGALREADYCANCELED"
																	: "IDS_TRAININGALREADYCONDUCTED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {

			final String updateQuery = "update trainingcertification set ntransactionstatus = "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dmodifieddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntrainingcode ="
					+ trainingcertification.getNtrainingcode() + " and nsitecode =" + userInfo.getNmastersitecode()
					+ ";";
			jdbcTemplate.execute(updateQuery);

			final TrainingParticipants trainingparticipantsByID = (TrainingParticipants) getActiveTrainingParticipantsById(
					trainingcertification.getNtrainingcode(), userInfo);

			if (trainingparticipantsByID != null) {

				final String updateQuery1 = "update trainingcertification set ntransactionstatus = "
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntrainingcode ="
						+ trainingcertification.getNtrainingcode() + " and nsitecode =" + userInfo.getNmastersitecode()
						+ ";";
				jdbcTemplate.execute(updateQuery1);
			}

			trainingcertification
					.setNtransactionstatus((short) Enumeration.TransactionStatus.CANCELED.gettransactionstatus());

			savedTrainingCertificationList.add(trainingcertificateByID);

			multilingualIDList.add("IDS_CANCELTRAINING");

			auditUtilityFunction.fnInsertAuditAction(savedTrainingCertificationList, 1, null, multilingualIDList,
					userInfo);

			// status code:200
			// return new ResponseEntity<>(supplier, HttpStatus.OK);
			return getTrainingCertificate(trainingcertification.getNtrainingcode(), fromDate, toDate, userInfo, null);
		}
	}

//	@Override
//	public ResponseEntity<Object> cancelTrainingParticipants(TrainingParticipants trainingparticipants,
//			UserInfo userInfo) throws Exception {
//
//		final List<String> multilingualIDList = new ArrayList<>();
//		final List<Object> savedTrainingParticipantsList = new ArrayList<>();
//
//		final TrainingParticipants trainingparticipantsByID = (TrainingParticipants) getActiveTrainingParticipantsById(
//				trainingparticipants.getNparticipantcode(), userInfo);
//
//		if (trainingparticipantsByID.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
//				.gettransactionstatus()) {
//
//			// status code:417
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYCANCELED.getreturnstatus(),
//					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//		} else {
//
//			final String updateQuery = "update trainingparticipants set ntransactionstatus = "
//					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " where nparticipantcode ="
//					+ trainingparticipants.getNparticipantcode() + ";";
//			jdbcTemplate.execute(updateQuery);
//
//			trainingparticipants.setNtransactionstatus((short) Enumeration.TransactionStatus.CANCELED.gettransactionstatus());
//
//			savedTrainingParticipantsList.add(trainingparticipants);
//
//			multilingualIDList.add("IDS_CANCELTRAINING");
//
//			fnInsertAuditAction(savedTrainingParticipantsList, 1, null, multilingualIDList, userInfo);
//			
//			return getTrainingParticipants(trainingparticipants.getNtrainingcode(), userInfo);
//
//		}
//		
//	}

	@Override
	public ResponseEntity<Object> updateTrainingCertificate(TrainingCertification trainingcertificate, String fromDate,
			String toDate, final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTrainingCertificationList = new ArrayList<>();
		final List<Object> oldTrainingCertificationList = new ArrayList<>();

		final ResponseEntity<Object> trainingcertificateByID = (ResponseEntity<Object>) getActiveTrainingCertificateById(
				trainingcertificate.getNtrainingcode(), userInfo);

		final TrainingCertification trainingCertificationID = (TrainingCertification) trainingcertificateByID.getBody();
		oldTrainingCertificationList.add(trainingCertificationID);

		TrainingCategory traningCategory = TraningCategoryDAO
				.getActiveTrainingCategoryById(trainingcertificate.getNtrainingcategorycode(), userInfo);

		if (traningCategory == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TRANINGCATEGORYINCETIFICATE",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final String countQuery = "select ntechniquecode from technique where ntechniquecode="
					+ trainingcertificate.getNtechniquecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
					+ userInfo.getNmastersitecode();
			Technique count = (Technique) jdbcUtilityFunction.queryForObject(countQuery, Technique.class, jdbcTemplate);
			if (count == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TECHNIQUEINCETIFICATE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				if (trainingCertificationID == null || trainingCertificationID
						.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(trainingCertificationID == null
									? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
									: "IDS_TRAININGALREADYCOMPLETED", userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					final String select = "select nparticipantcode from  trainingparticipants where  ntrainingcode="
							+ trainingcertificate.getNtrainingcode() + " and ntransactionstatus = "
							+ Enumeration.TransactionStatus.INVITED.gettransactionstatus() + " and nsitecode ="
							+ userInfo.getNmastersitecode();

					final List<TrainingCertification> lst01 = (List<TrainingCertification>) jdbcTemplate.query(select,
							new TrainingCertification());
					if (trainingCertificationID.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
							.gettransactionstatus()) {

						// status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TRAININGALREADYCANCELED",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else if (trainingCertificationID
							.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
									.gettransactionstatus()) {

						// status code:417
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_TRAININGALREADYCONDUCTED",
										userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					} else if (lst01.size() > 0) {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_INVITEDTRAININGCANNOTEDIT",
										userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					} else {
						final ObjectMapper objMapper = new ObjectMapper();
						objMapper.registerModule(new JavaTimeModule());
						trainingcertificate.setStrainingdatetime(
								dateUtilityFunction.instantDateToString(trainingcertificate.getDtrainingdatetime())
										.replace("T", " ").replace("Z", ""));

						final TrainingCertification convertedObject = objMapper.convertValue(
								dateUtilityFunction.convertInputDateToUTCByZone(trainingcertificate,
										Arrays.asList("strainingdatetime"), Arrays.asList("ntztrainingdate"), true,
										userInfo), // , false),
								new TypeReference<TrainingCertification>() {
								});

						final String updateQuery = "update trainingcertification set" + " ntrainingcategorycode="
													+ trainingcertificate.getNtrainingcategorycode() + " ," + " strainingtopic = '"
													+ stringUtilityFunction.replaceQuote(trainingcertificate.getStrainingtopic())
													+ "',strainername = '"
													+ stringUtilityFunction.replaceQuote(trainingcertificate.getStrainername())
													+ "',strainingvenue = '"
													+ stringUtilityFunction.replaceQuote(trainingcertificate.getStrainingvenue())
													+ "',dtrainingdatetime = '" + convertedObject.getStrainingdatetime()
													+ "',noffsetdtrainingdatetime=" + convertedObject.getNoffsetdtrainingdatetime()
													+ ",scomments = '"
													+ stringUtilityFunction.replaceQuote(trainingcertificate.getScomments())
													+ "',ntransactionstatus=" + trainingcertificate.getNtransactionstatus()
													+ ", dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo)
													+ "', ntechniquecode =" + trainingcertificate.getNtechniquecode()
													+ " where ntrainingcode =" + trainingcertificate.getNtrainingcode() + ";";
						final String getSeqno = "select nsequenceno+1 from seqnocompetencemanagement where stablename ='trainingreschedule';";
						final int seqNo = jdbcTemplate.queryForObject(getSeqno, Integer.class);
						final String insertReschedule = "insert into trainingreschedule (ntrainingrescedulecode,ntrainingcode,ntzrescheduledate,ntzscheduledate,dscheduledate,"
											+ "drescheduledate,dcreateddate,noffsetdscheduledate,noffsetdrescheduledate,noffsetdcreateddate,ntzcreateddate,scomments,dmodifieddate,nsitecode,nstatus) "
											+ "select " + seqNo + " ntrainingrescedulecode,ntrainingcode,"
											+ trainingcertificate.getNtztrainingdate() + ",ntztrainingdate,"
											+ " dtrainingdatetime,'" + convertedObject.getStrainingdatetime() + "','"
											+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ")
													.replace("Z", "")
											+ "', " + "noffsetdtrainingdatetime," + convertedObject.getNoffsetdtrainingdatetime()
											+ "," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
											+ userInfo.getNtimezonecode() + "," + "'"
											+ stringUtilityFunction.replaceQuote(trainingcertificate.getScomments())
											+ "' scomments,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
											+ userInfo.getNmastersitecode() + ", nstatus " + "from trainingcertification "
											+ "where ntrainingcode = " + trainingcertificate.getNtrainingcode() + ";";
						final String updateSeqQuery = " update seqnocompetencemanagement set nsequenceno = nsequenceno+1 where stablename ='trainingreschedule';";
						jdbcTemplate.execute(insertReschedule + updateQuery + updateSeqQuery);

						final ResponseEntity<Object> trainingCertificateByID = (ResponseEntity<Object>) getActiveTrainingCertificateById(
								trainingcertificate.getNtrainingcode(), userInfo);

						final TrainingCertification trainingcertificationID = (TrainingCertification) trainingCertificateByID
								.getBody();
						oldTrainingCertificationList.add(trainingCertificationID);

						savedTrainingCertificationList.add(trainingcertificationID);

						multilingualIDList.add("IDS_EDITTRAININGCERTIFICATE");

						auditUtilityFunction.fnInsertAuditAction(savedTrainingCertificationList, 2,
								oldTrainingCertificationList, multilingualIDList, userInfo);

						return getTrainingCertificate(trainingcertificate.getNtrainingcode(), fromDate, toDate,
								userInfo, null);
					}
				}
			}
		}
	}

	public ResponseEntity<Object> getCancelParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final int ntrainingcode = (int) inputMap.get("ntrainingcode");
		TrainingCertification trainingcertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
				ntrainingcode, userInfo).getBody());

//		if(trainingcertificateByID != null && trainingcertificateByID.getNtransactionstatus() !=  Enumeration.TransactionStatus.COMPLETED.gettransactionstatus())
//		{
		if (trainingcertificateByID == null
				|| trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
						.gettransactionstatus()
				|| trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
						.gettransactionstatus()
				|| trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
						.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					trainingcertificateByID == null ? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
							: trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
									.gettransactionstatus()
											? "IDS_TRAININGALREADYCOMPLETED"
											: trainingcertificateByID
													.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
															.gettransactionstatus() ? "IDS_TRAININGALREADYCONDUCTED"
																	: "IDS_TRAININGALREADYCANCELED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

		} else {
			String strQuery = "select tp.*,concat(u.sfirstname,' ',u.slastname) as sfullname from trainingparticipants tp,users u "
					+ " where tp.ntrainingcode=" + inputMap.get("ntrainingcode")
					+ " and tp.nusercode=u.nusercode and tp.ntransactionstatus not in ("
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")" + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nsitecode ="
					+ userInfo.getNmastersitecode();

			final List<TrainingParticipants> cancelParticipantsList = (List<TrainingParticipants>) jdbcTemplate
					.query(strQuery, new TrainingParticipants());

			outputMap.put("CancelParticipants", cancelParticipantsList);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
//		}
//		else
//		{
//			//status code:417 // Commented because I added the below condition in the starting of this method.
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage(trainingcertificateByID == null ? 
//					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus() : "IDS_TRAININGALREADYCOMPLETED",
//					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//		}

	}

	public ResponseEntity<Object> getInviteParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final int ntrainingcode = (int) inputMap.get("ntrainingcode");
		TrainingCertification trainingcertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
				ntrainingcode, userInfo).getBody());

//		if(trainingcertificateByID != null && trainingcertificateByID.getNtransactionstatus() != Enumeration.TransactionStatus.COMPLETED.gettransactionstatus())
//		{
		if (trainingcertificateByID == null
				|| trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
						.gettransactionstatus()
				|| trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
						.gettransactionstatus()
				|| trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
						.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					trainingcertificateByID == null ? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
							: trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
									.gettransactionstatus()
											? "IDS_TRAININGALREADYCOMPLETED"
											: trainingcertificateByID
													.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
															.gettransactionstatus() ? "IDS_TRAININGALREADYCONDUCTED"
																	: "IDS_TRAININGALREADYCANCELED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

		} else {
			final String strQuery = "select tp.*,concat(u.sfirstname,' ',u.slastname) as sfullname from trainingparticipants tp,users u "
					+ " where tp.ntrainingcode=" + inputMap.get("ntrainingcode")
					+ " and tp.nusercode=u.nusercode and tp.ntransactionstatus="
					+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nsitecode ="
					+ userInfo.getNmastersitecode();

			final List<TrainingParticipants> invitedParticipantsList = (List<TrainingParticipants>) jdbcTemplate
					.query(strQuery, new TrainingParticipants());

			outputMap.put("InvitedParticipants", invitedParticipantsList);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
//		}
//		else
//		{
//			//status code:417 // Commented because I added the below condition in the starting of this method.
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage(trainingcertificateByID == null ?
//					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus() : "IDS_TRAININGALREADYCOMPLETED",
//					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//		}
	}

	@Override
	public ResponseEntity<Object> inviteTrainingParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();

//		TrainingCertification trainingcertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
//				ntrainingcode, userInfo).getBody());
//		
		final List<TrainingParticipants> lstTrainingParticipants = objmapper
				.convertValue(inputMap.get("trainingparticipants"), new TypeReference<List<TrainingParticipants>>() {
				});

		TrainingCertification trainingcertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
				lstTrainingParticipants.get(0).getNtrainingcode(), userInfo).getBody());

		if ((((TrainingCertification) trainingcertificateByID)
				.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED.gettransactionstatus())
				|| (((TrainingCertification) trainingcertificateByID)
						.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED.gettransactionstatus())) {

			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
							.gettransactionstatus() ? "IDS_TRAININGALREADYCONDUCTED" : "IDS_TRAININGALREADYCANCELED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {

			final String sUserCode = lstTrainingParticipants.stream()
					.map(trainingparticipants -> (String.valueOf(trainingparticipants.getNusercode())))
					.collect(Collectors.joining(","));

			final String strQuery = "select nparticipantcode from trainingparticipants " + " where ntrainingcode="
									+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in (" + sUserCode
									+ ") and ntransactionstatus=" + Enumeration.TransactionStatus.INVITED.gettransactionstatus()
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nsitecode =" + userInfo.getNmastersitecode();

			final List<TrainingParticipants> lstvalidate = (List<TrainingParticipants>) jdbcTemplate.query(strQuery,
					new TrainingParticipants());
			if (lstvalidate.size() == 0) {
				String sUpdateFinal = "";
				String sUpdateQuery = "update trainingparticipants set ntransactionstatus = "
						+ Enumeration.TransactionStatus.INVITED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntrainingcode="
						+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in ( ";
				for (int i = 0; i < lstTrainingParticipants.size(); i++) {

					int nusercode = lstTrainingParticipants.get(i).getNusercode();
					String addString = "";
					if (i < lstTrainingParticipants.size() - 1) {
						addString = ",";
					}
					sUpdateQuery = sUpdateQuery + nusercode + addString;

				}
				sUpdateFinal = sUpdateQuery + ") and ntransactionstatus <> "
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "";
				jdbcTemplate.execute(sUpdateFinal);
				final String sAuditQuery = "select * from trainingparticipants " + " where ntrainingcode="
						+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in (" + sUserCode + ") "
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode =" + userInfo.getNmastersitecode();
				final List<TrainingParticipants> lstAudit = (List<TrainingParticipants>) jdbcTemplate.query(sAuditQuery,
						new TrainingParticipants());
				multilingualIDList.add("IDS_INVITEPARTICIPANTS");
				List<Object> invitedParticipants = new ArrayList<>();
				invitedParticipants.add(lstAudit);
				auditUtilityFunction.fnInsertListAuditAction(invitedParticipants, 1, null, multilingualIDList,
						userInfo);
				return getTrainingParticipants(lstTrainingParticipants.get(0).getNtrainingcode(), userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYINVITED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}

	}

	@Override
	public ResponseEntity<Object> cancelTrainingParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();

		final List<TrainingParticipants> lstTrainingParticipants = objmapper
				.convertValue(inputMap.get("trainingparticipants"), new TypeReference<List<TrainingParticipants>>() {
				});

		final TrainingCertification trainingcertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
				lstTrainingParticipants.get(0).getNtrainingcode(), userInfo).getBody());

		if ((((TrainingCertification) trainingcertificateByID)
				.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED.gettransactionstatus())
				|| (((TrainingCertification) trainingcertificateByID)
						.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED.gettransactionstatus())) {
			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					trainingcertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
							.gettransactionstatus() ? "IDS_TRAININGALREADYCONDUCTED" : "IDS_TRAININGALREADYCANCELED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final String sUserCode = lstTrainingParticipants.stream()
					.map(trainingparticipants -> (String.valueOf(trainingparticipants.getNusercode())))
					.collect(Collectors.joining(","));
			final String sParticipantCode = lstTrainingParticipants.stream()
					.map(trainingparticipants -> (String.valueOf(trainingparticipants.getNparticipantcode())))
					.collect(Collectors.joining(","));

			final String strQuery = "select nparticipantcode from trainingparticipants " + " where ntrainingcode="
					+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in (" + sUserCode
					+ ") and nparticipantcode in (" + sParticipantCode + ") and ntransactionstatus="
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<TrainingParticipants> lstvalidate = (List<TrainingParticipants>) jdbcTemplate.query(strQuery,
					new TrainingParticipants());
			if (lstvalidate.size() == 0) {
				String sUpdateQuery = "update trainingparticipants set ntransactionstatus = "
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntrainingcode="
						+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nusercode in ( " +
//					for (int i = 0; i < lstTrainingParticipants.size(); i++) {
//			
//						 int nusercode = lstTrainingParticipants.get(i).getNusercode();
//						 int nparticipantcode=lstTrainingParticipants.get(i).getNparticipantcode();
//						 String addString="";
//						 if(i<lstTrainingParticipants.size()-1)
//							{
//							 addString = ",";
//							}
//						 sUpdateQuery =sUpdateQuery + nusercode + addString;
//						 sParticipantcode=nparticipantcode+addString;
//			
//					}
						sUserCode + ") and nparticipantcode in (" + sParticipantCode + ") and ntransactionstatus <> "
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "";

				jdbcTemplate.execute(sUpdateQuery);

				final String sAuditQuery = "select * from trainingparticipants " + " where ntrainingcode="
						+ lstTrainingParticipants.get(0).getNtrainingcode() + " and nparticipantcode="
						+ lstTrainingParticipants.get(0).getNparticipantcode() + " and nusercode in (" + sUserCode
						+ ") " + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
				final List<TrainingParticipants> lstAudit = (List<TrainingParticipants>) jdbcTemplate.query(sAuditQuery,
						new TrainingParticipants());
				multilingualIDList.add("IDS_CANCELPARTICIPANTS");
				List<Object> cancelParticipants = new ArrayList<>();
				cancelParticipants.add(lstAudit);
				auditUtilityFunction.fnInsertListAuditAction(cancelParticipants, 1, null, multilingualIDList, userInfo);
				return getTrainingParticipants(lstTrainingParticipants.get(0).getNtrainingcode(), userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYCANCELED.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getTechnique(final UserInfo userInfo) {
		final String strQuery = "select t.*" + "  from Technique t" + " where t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and t.ntechniquecode>0  and t.nsitecode=" + userInfo.getNmastersitecode()
				+ " order by t.ntechniquecode";

		return new ResponseEntity<Object>((List<Technique>) jdbcTemplate.query(strQuery, new Technique()),
				HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> rescheduleTrainingCertificate(final TrainingCertification trainingcertificate,
			String fromDate, String toDate, final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTrainingCertificationList = new ArrayList<>();
		final List<Object> oldTrainingCertificationList = new ArrayList<>();
		final String select = "select nparticipantcode from  trainingparticipants where  ntrainingcode="
								+ trainingcertificate.getNtrainingcode() + " and ntransactionstatus = "
								+ Enumeration.TransactionStatus.INVITED.gettransactionstatus() + " and nsitecode ="
								+ userInfo.getNmastersitecode();

		final List<TrainingCertification> lst01 = (List<TrainingCertification>) jdbcTemplate.query(select,
				new TrainingCertification());
		TrainingCertification trainingcertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
				trainingcertificate.getNtrainingcode(), userInfo).getBody());
		oldTrainingCertificationList.add(trainingcertificateByID);

		if (trainingcertificateByID == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (((TrainingCertification) trainingcertificateByID)
					.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED.gettransactionstatus()) {

				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TRAININGALREADYCANCELED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else if (((TrainingCertification) trainingcertificateByID)
					.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED.gettransactionstatus()) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_CONDUCTEDTRAININGCANNOTRESCHEDULE",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else if (!(lst01.size() > 0)) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVITEPARTICIPANTS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				final ObjectMapper objMapper = new ObjectMapper();
				objMapper.registerModule(new JavaTimeModule());
				trainingcertificate.setStrainingdatetime(
						dateUtilityFunction.instantDateToString(trainingcertificate.getDtrainingdatetime())
								.replace("T", " ").replace("Z", ""));

				final TrainingCertification convertedObject = objMapper.convertValue(
						dateUtilityFunction.convertInputDateToUTCByZone(trainingcertificate,
								Arrays.asList("strainingdatetime"), Arrays.asList("ntztrainingdate"), true, userInfo), // ,
																														// false),
						new TypeReference<TrainingCertification>() {
						});

				final String updateQuery = "update trainingcertification set" + " strainingtopic = '"
											+ stringUtilityFunction.replaceQuote(trainingcertificate.getStrainingtopic())
											+ "',strainername = '"
											+ stringUtilityFunction.replaceQuote(trainingcertificate.getStrainername())
											+ "',strainingvenue = '"
											+ stringUtilityFunction.replaceQuote(trainingcertificate.getStrainingvenue())
											+ "',dtrainingdatetime = '" + convertedObject.getStrainingdatetime()
											+ "',noffsetdtrainingdatetime=" + convertedObject.getNoffsetdtrainingdatetime()
											+ ",scomments = '" + stringUtilityFunction.replaceQuote(trainingcertificate.getScomments())
											+ "',ntransactionstatus=" + trainingcertificate.getNtransactionstatus() + ", dmodifieddate ='"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntrainingcode ="
											+ trainingcertificate.getNtrainingcode() + ";";
				final String getSeqno = "select nsequenceno+1 from seqnocompetencemanagement where stablename ='trainingreschedule';";
				final int seqNo = jdbcTemplate.queryForObject(getSeqno, Integer.class);
				final String insertReschedule = "insert into trainingreschedule (ntrainingrescedulecode,ntrainingcode,ntzrescheduledate,ntzscheduledate,dscheduledate,"
												+ "drescheduledate,dcreateddate,noffsetdscheduledate,noffsetdrescheduledate,noffsetdcreateddate,ntzcreateddate,scomments,dmodifieddate,nsitecode,nstatus) "
												+ "select " + seqNo + " ntrainingrescedulecode,ntrainingcode,"
												+ trainingcertificate.getNtztrainingdate() + ",ntztrainingdate," + " dtrainingdatetime,'"
												+ convertedObject.getStrainingdatetime() + "','"
												+ dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "")
												+ "', " + "noffsetdtrainingdatetime," + convertedObject.getNoffsetdtrainingdatetime() + ","
												+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
												+ userInfo.getNtimezonecode() + "," + "'"
												+ stringUtilityFunction.replaceQuote(trainingcertificate.getScomments()) + "' scomments,'"
												+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
												+ ",nstatus " + "from trainingcertification " + "where ntrainingcode = "
												+ trainingcertificate.getNtrainingcode() + ";";
				final String updateSeqQuery = " update seqnocompetencemanagement set nsequenceno = nsequenceno+1 where stablename ='trainingreschedule';";
				jdbcTemplate.execute(insertReschedule + updateQuery + updateSeqQuery);

				TrainingCertification trainingCertificateByID = ((TrainingCertification) getActiveTrainingCertificateById(
						trainingcertificate.getNtrainingcode(), userInfo).getBody());

				savedTrainingCertificationList.add(trainingCertificateByID);

				multilingualIDList.add("IDS_RESCHEDULETRAINING");

				auditUtilityFunction.fnInsertAuditAction(savedTrainingCertificationList, 2,
						oldTrainingCertificationList, multilingualIDList, userInfo);

				return getTrainingCertificate(trainingcertificate.getNtrainingcode(), fromDate, toDate, userInfo, null);

			}

		}
	}

	@Override
	public ResponseEntity<Object> deleteTrainingCertificate(final TrainingCertification objTCertify, String fromDate,
			String toDate, final UserInfo userInfo) throws Exception {
		if (objTCertify.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TRAININGALREADYCANCELED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		TrainingCertification lstTrainingCertificate = ((TrainingCertification) getActiveTrainingCertificateById(
				objTCertify.getNtrainingcode(), userInfo).getBody());

		if (lstTrainingCertificate == null
				|| lstTrainingCertificate.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
						.gettransactionstatus()
				|| lstTrainingCertificate.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
						.gettransactionstatus()
				|| lstTrainingCertificate.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
						.gettransactionstatus()) {
			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					lstTrainingCertificate == null ? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
							: lstTrainingCertificate.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
									.gettransactionstatus()
											? "IDS_TRAININGALREADYCOMPLETED"
											: lstTrainingCertificate
													.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
															.gettransactionstatus() ? "IDS_TRAININGALREADYCONDUCTED"
																	: "IDS_TRAININGALREADYCANCELED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select 'IDS_TRAININGCERTIFICATE' as Msg from trainingparticipants where ntrainingcode= "
					+ objTCertify.getNtrainingcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus!="
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objTCertify.getNtrainingcode()),
						userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedTCertifyList = new ArrayList<>();
				final String updateQueryString = "update trainingcertification set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ntrainingcode="
						+ objTCertify.getNtrainingcode();

				jdbcTemplate.execute(updateQueryString);
				objTCertify.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedTCertifyList.add(objTCertify);
				multilingualIDList.add("IDS_DELETETRAININGCERTIFICATE");
				auditUtilityFunction.fnInsertAuditAction(deletedTCertifyList, 1, null, multilingualIDList, userInfo);

				return getTrainingCertificate(null, fromDate, toDate, userInfo, null);
			} else {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTDELETEASPARTICIPANTADDED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

}
