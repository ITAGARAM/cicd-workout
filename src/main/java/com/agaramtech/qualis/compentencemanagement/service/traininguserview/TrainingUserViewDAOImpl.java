
package com.agaramtech.qualis.compentencemanagement.service.traininguserview;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.agaramtech.qualis.compentencemanagement.model.TrainingDocuments;
import com.agaramtech.qualis.compentencemanagement.model.TrainingParticipants;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class TrainingUserViewDAOImpl implements TrainingUserViewDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrainingUserViewDAOImpl.class);
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	@Override
	public ResponseEntity<Object> getUserView(String fromDate, String toDate, String currentUIDate, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			outputMap.put("FromDate", (String) mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", (String) mapObject.get("ToDateWOUTC"));
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

		String userviewget = "select tc.ntrainingcode,tc.strainername,tc.strainingtopic,tc.strainingvenue,tp.ntransactionstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as strainingstatus,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as sattendancestatus,"
				+ " tc.strainingtime,tp.nparticipantcode," + " TO_CHAR(tc.dtrainingdatetime,'"
				+ userInfo.getSpgsitedatetime()
				+ "') as strainingdatetime, tca.strainingcategoryname, tc.noffsetdtrainingdatetime "
				+ " from trainingcertification tc , trainingparticipants tp, transactionstatus ts,transactionstatus ts1,timezone tz, trainingcategory tca where tp.ntrainingcode = tc.ntrainingcode and "
				+ " tc.ntransactionstatus=ts.ntranscode and tp.ntransactionstatus=ts1.ntranscode and "
				+ " tz.ntimezonecode=tc.ntztrainingdate and " + " tp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nusercode = "
				+ userInfo.getNusercode() + " and " + " tc.dtrainingdatetime " + " between '" + fromDate + "' and "
				+ " '" + toDate + "' and tp.ntransactionstatus != "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " and tc.ntransactionstatus != "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " and"
				+ " tca.ntrainingcategorycode = tc.ntrainingcategorycode order by tp.nparticipantcode asc ";

		List<TrainingParticipants> lstTrainingUserView = (List<TrainingParticipants>) jdbcTemplate.query(userviewget,
				new TrainingParticipants());

		ObjectMapper objMapper = new ObjectMapper();
		final List<TrainingParticipants> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(lstTrainingUserView, Arrays.asList("strainingdatetime"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<TrainingParticipants>>() {
				});

		if (!lstTrainingUserView.isEmpty()) {
			outputMap.put("TrainingUserView", lstTrainingUserView);
			outputMap.put("SelectedTrainingUserView", lstUTCConvertedDate.get(lstTrainingUserView.size() - 1));
			outputMap.putAll(getTrainingDoc(lstUTCConvertedDate.get(lstTrainingUserView.size() - 1).getNtrainingcode(),
					userInfo));
		} else {
			outputMap.put("TrainingUserView", lstTrainingUserView);
			outputMap.put("TrainingDocuments", null);
		}
		LOGGER.info("getUserView:" + userviewget);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTrainingUserViewDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String userViewSelected = "select tp.nparticipantcode,tc.ntrainingcode,tc.strainername,tc.strainingtopic,tc.strainingvenue,tc.ntransactionstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sattendancestatus,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as strainingstatus,tc.strainingtime,"
				+ " TO_CHAR(tc.dtrainingdatetime,'" + userInfo.getSpgsitedatetime()
				+ "') as strainingdatetime, tc.noffsetdtrainingdatetime, tca.strainingcategoryname "
				+ " from trainingcertification tc ,trainingparticipants tp,transactionstatus ts,transactionstatus ts1, trainingcategory tca where ts.ntranscode=tc.ntransactionstatus "
				+ " and ts1.ntranscode=tc.ntransactionstatus " + " and tp.ntrainingcode = tc.ntrainingcode "
				+ " and tp.nparticipantcode=" + inputMap.get("nparticipantcode") + " and " + " tc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nusercode = "
				+ userInfo.getNusercode() + " and tca.ntrainingcategorycode = tc.ntrainingcategorycode ";
		List<TrainingParticipants> lstTrainingUserViewSelected = (List<TrainingParticipants>) jdbcTemplate
				.query(userViewSelected, new TrainingParticipants());
		ObjectMapper objMapper = new ObjectMapper();
		final List<TrainingParticipants> lstUTCConvertedDate = objMapper
				.convertValue(dateUtilityFunction.getSiteLocalTimeFromUTC(lstTrainingUserViewSelected,
						Arrays.asList("strainingdatetime"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false,
						null, false), new TypeReference<List<TrainingParticipants>>() {
						});
		outputMap.put("SelectedTrainingUserView", lstUTCConvertedDate.get(0));
		outputMap.putAll(getTrainingDoc(lstTrainingUserViewSelected.get(0).getNtrainingcode(), userInfo));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public Map<String, Object> getTrainingDoc(int ntrainingcode, UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();
		String queryformat = "COALESCE(TO_CHAR(dcreateddate,'" + userInfo.getSpgdatetimeformat() + "'),'-') ";
		String strquery = "select td.ntrainingdoccode as nprimarycode,td.sfilename,td.sfiledesc,td.ntrainingdoccode,"
				+ " td.ssystemfilename as ssystemfilename,td.nattachmenttypecode,case when td.nlinkcode = -1 then "
				+ queryformat
				+ " else '-' end screateddate, case when td.nlinkcode = -1 then cast(td.nfilesize as text) else '-' end sfilesize,"
				+ "case when td.nlinkcode = -1 then '-' else lm.jsondata->>'slinkname' end slinkname  from trainingdocuments td, linkmaster lm  where "
				+ " td.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and td.ntrainingcode=" + ntrainingcode + " and " + " lm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and td.nlinkcode=lm.nlinkcode";
		List<TrainingDocuments> lstTrainingDocuments = (List<TrainingDocuments>) jdbcTemplate.query(strquery,
				new TrainingDocuments());
		objMap.put("TrainingDocuments", lstTrainingDocuments);
		return objMap;
	}

	@Override
	public ResponseEntity<Object> viewUserViewFile(TrainingDocuments objUserViewFile, UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		String sQuery = "select * from trainingdocuments where ntrainingdoccode = "
				+ objUserViewFile.getNtrainingdoccode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final TrainingDocuments objTF = (TrainingDocuments) jdbcUtilityFunction.queryForObject(sQuery,
				TrainingDocuments.class, jdbcTemplate);
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
				objUserViewFile.setScreateddate(null);
			}
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList.add(objUserViewFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					? "IDS_VIEWTRAININGDOCUMENTFILE"
					: "IDS_VIEWTRAININGDOCUMENTLINK");
			lstObject.add(objUserViewFile);
			auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
}
