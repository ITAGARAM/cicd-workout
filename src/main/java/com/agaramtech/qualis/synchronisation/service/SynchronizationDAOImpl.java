package com.agaramtech.qualis.synchronisation.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.credential.model.SiteConfig;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.synchronisation.model.SyncBatch;
import com.agaramtech.qualis.synchronisation.model.SyncSendRecord;
import com.agaramtech.qualis.synchronisation.model.SyncTableList;
import com.agaramtech.qualis.synchronisation.model.Synchronization;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SynchronizationDAOImpl implements SynchronizationDAO {

	final Log syncLogging = LogFactory.getLog(SynchronizationDAOImpl.class);

	//private static final String PATTERN_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String SOURCE_SITE_NAME = "SourceSiteName";
	private static final String DESTINATION_SITE_NAME = "DestinationSiteName";
	private static final String DESTINATION_SITE_CODE = "DestinationSiteCode";
	private static final String SOURCE_SITE_CODE = "SourceSiteCode";
	private static final String SITE_NAME = "SiteName";
	private static final String TRANSFER_ID = "TransferID";
	private static final String STATUS = "Status";
	private static final String MESSAGE_TYPE = "MessageType";
	private static final String LIMS_COMMENT = "LIMS_Comment";
	private static final String LIMS_MESSAGE = "LIMS_Message";
	private static final String META_DATA = "MetaData";
	public static final String NO_DATA_FOUND = "NoDataFound";
	public static final String RESPONSE = "Response";
	public static final String TRANSFER_TYPE = "TransferType";
	public static final String BATCH_TRANSFER_ID= "BatchTransferID";
	public static final String S_TABLE_NAME = "stablename";
	public static final String S_TRANSFER_ID = "stransferid";
	public static final String N_SORTER = "nsorter";
	public static final String ACKNOWLEDGEMENT = "Acknowledge";


	private final CommonFunction commonFunction;

	private final SyncHistoryDAO syncBatchHistoryDAO;

	private final JdbcTemplate jdbcTemplate;

	private final DateTimeUtilityFunction dateUtilityFunction;

	private final StringUtilityFunction stringUtilityFunction;

	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;


	/**
	 * This method will be called by the interfacer to send the data prepared or the response
	 * of sync process between source and destination servers.
	 * 1. Records that are available in draft/received status (at nbatchtransferstatus field in syncbatch table) will be fetched.
	 * 		If records are not available in this status, then response has to be sent to interfacer that batches are completed
	 * 		and no data or response is pending to transfer between sites.
	 * 2.
	 *
	 */
	@SuppressWarnings({ "unused" })
	@Override
	public ResponseEntity<Object> sendRecords() throws Exception {

		syncLogging.info("------------------------send Record start----------------");
		final Map<String, Object> outputMap = new HashMap<>();
		final Map<String, Object> dataOutPutMap = new HashMap<>();
		final Map<String, Object> objMetaData = new HashMap<>();
		final Map<String, Object> objLimsMsg = new HashMap<>();

		String sMaxRecord = "";
		String sTableRecords = "";
		String sinsertSync = "";
		List<Map<String, Object>> listqry = new ArrayList<>();

		//		String sQuery = "select * from syncbatch where "
		//						+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		//						+ " and nbatchtransferstatus in ( "
		//						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
		//						+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ", "
		//						+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()
		//						+ " ) order by nsyncbatchcode asc LIMIT 1";//can we change as asc, any impact for this?


		String sQuery = "select * from syncbatch where nbatchtransferstatus in ("
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ") "
				+ " union all "
				+ " select sb.* from syncbatch sb, syncsendrecord ssr  where "
				+ " sb.sbatchtransferid = ssr.sbatchtransferid and "
				+ " sb.nstatus =ssr.nstatus and ssr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sb.nbatchtransferstatus =" + Enumeration.TransactionStatus.SENT.gettransactionstatus()
				+ " and ssr.nacknowledgestatus=" + Enumeration.TransactionStatus.NO.gettransactionstatus()
				+ " order by nsyncbatchcode asc LIMIT 1";

		syncLogging.info("Send Records getQuery for draft, received status:"+ sQuery);

		final SyncBatch syncBatch = (SyncBatch)jdbcUtilityFunction.queryForObject(sQuery, SyncBatch.class, jdbcTemplate);
		syncLogging.info("Send Records 1:"+ syncBatch);

		if(syncBatch != null)
		{
			syncLogging.info("Send Records if loop 2");
			objLimsMsg.put(SOURCE_SITE_NAME, syncBatch.getSsourcesitecode());
			objLimsMsg.put(DESTINATION_SITE_NAME, syncBatch.getSdestinationsitecode());
			objLimsMsg.put(DESTINATION_SITE_CODE, syncBatch.getNdestinationsitecode());
			objLimsMsg.put(SOURCE_SITE_CODE, syncBatch.getNsitecode());
			syncLogging.info("Send Records 3:"+ objLimsMsg);

		}

		final Map<String, Object> completedBatchMetaData = new HashMap<>();
		completedBatchMetaData.put(SITE_NAME, "");
		completedBatchMetaData.put(TRANSFER_ID, "");
		completedBatchMetaData.put(STATUS, "Completed");
		completedBatchMetaData.put(MESSAGE_TYPE, NO_DATA_FOUND);
		completedBatchMetaData.put(LIMS_COMMENT, NO_DATA_FOUND);
		completedBatchMetaData.put(LIMS_MESSAGE, objLimsMsg);
		syncLogging.info("Send Records batch default response 4:"+ completedBatchMetaData);


		try {

			if (syncBatch == null) {
				//Batch Completed
				syncLogging.info("Send Records null loop 5");
				outputMap.put(META_DATA, completedBatchMetaData);
			}
			else
			{
				syncLogging.info("Send Records not null batchfinalstatus 6:"+ syncBatch.getSbatchtransferid() +","
						+ syncBatch.getNbatchfinalstatus()
						+",batchtransferstatus:"+ syncBatch.getNbatchtransferstatus());

				sQuery = "select count(*) from syncsendrecord where sbatchtransferid = '"	+ syncBatch.getSbatchtransferid() + "'";
				final Integer batchRecordCount = jdbcTemplate.queryForObject(sQuery, Integer.class);

				syncLogging.info("Send Records Batch Count 7:"+ syncBatch.getSbatchtransferid()+ "," + batchRecordCount);

				int sendType = -1;
				if(syncBatch.getNbatchfinalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus())
				{
					//Draft Batch will send data
					sendType = Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

					if(batchRecordCount != null && batchRecordCount > 0) {

						sQuery = "select count(*) from syncsendrecord "
								+ " where sbatchtransferid = '" + syncBatch.getSbatchtransferid()
								+ "' and nfetchstatus =" + Enumeration.TransactionStatus.YES.gettransactionstatus();
						final Integer fetchedRecordCount = jdbcTemplate.queryForObject(sQuery, Integer.class);

						syncLogging.info("Send Records fetched record count 8:"+ fetchedRecordCount);

						if(fetchedRecordCount != null && fetchedRecordCount == batchRecordCount) {

							syncLogging.info("Send Records fetched and batch record count equal 9");

							sQuery = "select count(*) from syncsendrecord "
									+ " where sbatchtransferid = '" + syncBatch.getSbatchtransferid()
									+ "' and nacknowledgestatus =" + Enumeration.TransactionStatus.YES.gettransactionstatus();
							final Integer acknowledgeRecordCount = jdbcTemplate.queryForObject(sQuery, Integer.class);

							syncLogging.info("Send Records 10 acknowledged RecordCount 10:"+ acknowledgeRecordCount);

							if(acknowledgeRecordCount != null && fetchedRecordCount != acknowledgeRecordCount) {

								syncLogging.info("Send Records fetched and acknowledge record count not equal 11");
								sendType = Enumeration.TransactionStatus.INTERMEDIATE.gettransactionstatus();
							}
						}
					}

				}
				else if(syncBatch.getNbatchfinalstatus() == Enumeration.TransactionStatus.RECEIVESUCCESS.gettransactionstatus()
						|| syncBatch.getNbatchfinalstatus() == Enumeration.TransactionStatus.RECEIVEFAILED.gettransactionstatus())
				{
					syncLogging.info("Send Records sendType 12:"+ sendType);

					//Received Batch will send status
					if(syncBatch.getNbatchtransferstatus() == Enumeration.TransactionStatus.RECEIVED.gettransactionstatus())
					{
						sendType = Enumeration.TransactionStatus.RECEIVED.gettransactionstatus();

					}
				}

				syncLogging.info("Send Records send type 13:"+ sendType);

				//				sQuery = " lock table locksendrecords " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
				//				jdbcTemplate.execute(sQuery);

				if (sendType == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

					syncLogging.info("Send Records draft sendType , sbatchtransferid 14:"+ syncBatch.getSbatchtransferid());
					final int retryRecord = Enumeration.TransactionStatus.NO.gettransactionstatus();

					if(batchRecordCount != null && batchRecordCount > 0) {
						final int retryCount = 0;
						outputMap.putAll(organiseSendRecord(syncBatch, objLimsMsg, objMetaData,
								dataOutPutMap, completedBatchMetaData, retryCount, batchRecordCount));
					}
					else
					{
						//added on 19-07-2024
						outputMap.put(META_DATA, completedBatchMetaData);
					}
				}
				else if (sendType == Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()) {

					syncLogging.info("Send Records received sendType , sbatchtransferid 15:"+ syncBatch.getSbatchtransferid());

					if(syncBatch.getNbatchfinalstatus() == Enumeration.TransactionStatus.RECEIVESUCCESS.gettransactionstatus())
					{
						objLimsMsg.put("BatchFinalStatus", Enumeration.TransactionStatus.SENTSUCCESS.gettransactionstatus());
					}
					else if(syncBatch.getNbatchfinalstatus() == Enumeration.TransactionStatus.RECEIVEFAILED.gettransactionstatus())
					{
						objLimsMsg.put("BatchFinalStatus", Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus());
					}

					objLimsMsg.put(BATCH_TRANSFER_ID, syncBatch.getSbatchtransferid());
					objLimsMsg.put(SOURCE_SITE_NAME, syncBatch.getSsourcesitecode());
					objLimsMsg.put(DESTINATION_SITE_NAME, syncBatch.getSdestinationsitecode());
					objLimsMsg.put(SOURCE_SITE_CODE, syncBatch.getNsitecode());
					objLimsMsg.put(DESTINATION_SITE_CODE, syncBatch.getNdestinationsitecode());

					objMetaData.put(SITE_NAME, syncBatch.getSdestinationsitecode());
					objMetaData.put(TRANSFER_ID, syncBatch.getSbatchtransferid());
					objMetaData.put(MESSAGE_TYPE, "FinalStatusUpdate");
					objMetaData.put(LIMS_COMMENT, NO_DATA_FOUND);
					objMetaData.put(LIMS_MESSAGE, objLimsMsg);

					outputMap.put(META_DATA, objMetaData);
					syncLogging.info("Send Records 16:"+ objMetaData);

					jdbcTemplate.execute("update syncbatch set nbatchtransferstatus = "
							+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
							+ ", dbatchenddatetime= Now() "
							+ "  where sbatchtransferid = '" + syncBatch.getSbatchtransferid() + "'");

					syncLogging.info("Send BatchFinalStatus data map  17:" + outputMap);
				}
				else if (sendType == Enumeration.TransactionStatus.INTERMEDIATE.gettransactionstatus()) {

					syncLogging.info("Send Records intermediate sendType, sbatchtransferid:"+ sendType +","+ syncBatch.getSbatchtransferid());
					final int retryRecord = Enumeration.TransactionStatus.YES.gettransactionstatus();

					syncLogging.info("Send Records retryRecord 18:"+ retryRecord +","+ syncBatch.getSbatchtransferid());


					if(batchRecordCount != null && batchRecordCount > 0) {

						final String retryCountQry="select ssettingvalue from settings where nsettingcode="
								+ Enumeration.Settings.SYNC_DATA_RETRY_COUNT.getNsettingcode()
								+ "  and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						Integer retryCount = jdbcTemplate.queryForObject(retryCountQry, Integer.class);

						syncLogging.info("Send Records retryCount 19a:"+ retryCount);

						if(retryCount == null)
						{
							retryCount = 3;
						}
						outputMap.putAll(organiseSendRecord(syncBatch, objLimsMsg, objMetaData,
								dataOutPutMap, completedBatchMetaData, retryCount, batchRecordCount));
					}
					else
					{
						outputMap.put(META_DATA, completedBatchMetaData);
					}
				}
				else {
					outputMap.put(META_DATA, completedBatchMetaData);
					syncLogging.info("Send Records 19:"+ completedBatchMetaData);

				}
			}
			syncLogging.info("-----------send Record end--------20-:"+outputMap);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
		catch (Exception e) {
			syncLogging.error("Excepton in send records DAO:"+e);
			syncLogging.error("Excepton in send records DAO:"+completedBatchMetaData);


			outputMap.put(META_DATA, completedBatchMetaData);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		}
	}

	/**
	 * Added by L.Subashini to handle resenting data if the acknowledge is not yet received from interfacer
	 * @param syncBatch
	 * @param objLimsMsg
	 * @param objMetaData
	 * @param dataOutPutMap
	 * @param completedBatchMetaData
	 * @param recordType
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> organiseSendRecord(SyncBatch syncBatch, Map<String, Object> objLimsMsg,
			Map<String, Object> objMetaData, Map<String, Object> dataOutPutMap,
			Map<String, Object> completedBatchMetaData, final int retryCount,
			final int recordCount) throws Exception{

		syncLogging.info("SendDraftRecord method 1:"+  syncBatch.getSbatchtransferid()
		+" retryRecord:"+retryCount +",recordCount:"+recordCount);
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final int ntransferType = syncBatch.getNtransfertype();

		try
		{
			String sQuery = "select  nsyncsendrecordcode, sbatchtransferid, nsyncbatchcode,"
					+ " json_build_object('records', jsondata) :: jsonb as jsondata,"
					+ " stablename, stransferid, nfetchstatus, sdestinationsitecode, "
					+ " ssourcesitecode, dtransactiondatetime,nsitecode,"
					+ " nstatus, nretrycount, nacknowledgestatus  "
					+ " from syncsendrecord where "
					//+ " nfetchstatus ="+ Enumeration.TransactionStatus.NO.gettransactionstatus()
					+ " nacknowledgestatus ="+ Enumeration.TransactionStatus.NO.gettransactionstatus()
					+ " and nretrycount <"+ retryCount
					+ " and  sbatchtransferid = '" + syncBatch.getSbatchtransferid()
					+ "' order by nsyncsendrecordcode asc limit 1";

			final SyncSendRecord syncSendRecord =  (SyncSendRecord)jdbcUtilityFunction.queryForObject(
					sQuery, SyncSendRecord.class, jdbcTemplate);

			syncLogging.info("SendDraftRecord method 3: "+ sQuery +"," + syncSendRecord + ",:"+syncBatch.getSbatchtransferid());

			if (syncSendRecord != null) {

				syncLogging.info("SendDraftRecord method -tablename 2: " + syncSendRecord.getStablename()+",retry count:"+syncSendRecord.getNretrycount());

				if(syncSendRecord.getNretrycount()< retryCount)
				{
					syncLogging.info("SendDraftRecord method retrycount count update loop 2a:");

					jdbcTemplate.execute("update syncsendrecord set nfetchstatus = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ ", nretrycount=(select nretrycount+1 from syncsendrecord where nsyncsendrecordcode = "
							+ syncSendRecord.getNsyncsendrecordcode()+ ") "
							+ " where nsyncsendrecordcode = " + syncSendRecord.getNsyncsendrecordcode()+";");

				}
				sQuery = "select count(1) from syncsendrecord "
						+ " where sbatchtransferid = '" + syncBatch.getSbatchtransferid()
						+ "' and nfetchstatus =" + Enumeration.TransactionStatus.YES.gettransactionstatus();
				final Integer sentRecordCount = jdbcTemplate.queryForObject(sQuery, Integer.class);

				sQuery = "select count(1) from syncsendrecord "
						+ " where sbatchtransferid = '" + syncBatch.getSbatchtransferid()
						+ "' and nacknowledgestatus ="+ Enumeration.TransactionStatus.NO.gettransactionstatus();
				final Integer acknowledgePendingCount = jdbcTemplate.queryForObject(sQuery, Integer.class);

				objLimsMsg.put("BatchCount", recordCount);// 3
				objLimsMsg.put("RemainCount", sentRecordCount);// 3
				objLimsMsg.put("AcknowledgePendingCount", acknowledgePendingCount);
				objLimsMsg.put("RetryCount", syncSendRecord.getNretrycount());
				objLimsMsg.put(BATCH_TRANSFER_ID, syncBatch.getSbatchtransferid());
				objLimsMsg.put("TransferType", ntransferType);
				objLimsMsg.put(SOURCE_SITE_NAME, syncSendRecord.getSsourcesitecode());
				objLimsMsg.put(SOURCE_SITE_CODE, syncSendRecord.getNsitecode());
				objLimsMsg.put(DESTINATION_SITE_NAME, syncSendRecord.getSdestinationsitecode());
				objLimsMsg.put(DESTINATION_SITE_CODE, syncBatch.getNdestinationsitecode());

				objMetaData.put(SITE_NAME, syncSendRecord.getSdestinationsitecode());
				objMetaData.put(TRANSFER_ID, syncSendRecord.getStransferid());
				objMetaData.put(MESSAGE_TYPE, "Data");
				objMetaData.put(LIMS_MESSAGE, objLimsMsg);


				dataOutPutMap.put(S_TABLE_NAME, syncSendRecord.getStablename());
				dataOutPutMap.put(S_TRANSFER_ID, syncSendRecord.getStransferid());
				dataOutPutMap.put(N_SORTER, syncSendRecord.getNsorter());
				dataOutPutMap.putAll(syncSendRecord.getJsondata());

				outputMap.put(META_DATA, objMetaData);
				outputMap.put("Data", dataOutPutMap);

				syncLogging.info("SendDraftRecord method  3:"+ outputMap);
				syncLogging.info("SendDraftRecord method , batch count, sent reccord count - 4 :"+ recordCount+","+sentRecordCount);

				if(retryCount != 0 && syncSendRecord.getNretrycount() == retryCount  && acknowledgePendingCount != 0)
				{
					//making batch as sent failed if the acknowledgement is not received even after 3 attempts
					syncLogging.info("SendDraftRecord method  max retry reached and not acknowledged 5:"+outputMap);

					jdbcTemplate.execute("update syncbatch set nbatchtransferstatus = "
							+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
							+ ", dbatchenddatetime= Now() "
							+ " , nbatchfinalstatus = " + Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus()
							+ "  where sbatchtransferid = '" + syncBatch.getSbatchtransferid() + "'");

					jdbcTemplate.execute("update synchronization set nsyncstatus = "
							+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
							+ ", dlastsyncdatetime=dsyncdatetime "
							+ " where nsynchronizationcode = (select nsynchronizationcode from syncbatch"
							+ " where sbatchtransferid = '" + syncBatch.getSbatchtransferid() + "'"
							+ " and nbatchtransferstatus="+	 Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
							+ " and nbatchfinalstatus = "+ Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus() + ") "
							);

					//Update coareporthistorygeneration only for coaparent based sync
					jdbcTemplate.execute("update coareporthistorygeneration set ssynccomments="
							+ " 'Sync status reverted back to Draft as process failed'"
							+ " , nsyncstatus= "
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
							+ " where ncoareporthistorycode =( "
							+ " select s.ncoareporthistorycode from  syncbatch sb, synchronization s where "
							+ " sb.nsynchronizationcode = s.nsynchronizationcode "
							+ " and ndatewisesync = " + Enumeration.TransactionStatus.NO.gettransactionstatus()
							+ " and  sb.sbatchtransferid = '" + syncBatch.getSbatchtransferid() + "')");

				}
				else
				{
					if (recordCount == sentRecordCount) {

						jdbcTemplate.execute("update syncbatch set nbatchtransferstatus = "
								+ Enumeration.TransactionStatus.SENT.gettransactionstatus()
								+ " where sbatchtransferid = '"+ syncBatch.getSbatchtransferid() + "'");

						syncLogging.info("SendDraftRecord method :update syncbatchhistory set nbatchtransferstatus = Sent 5");
					}
					syncLogging.info("SendDraftRecord method retryCount 6a:"+syncSendRecord.getStransferid()+",count:"+syncSendRecord.getNretrycount()
					+","+syncSendRecord.getNfetchstatus());

					if(syncSendRecord.getNretrycount() == -1
							&& syncSendRecord.getNfetchstatus() ==  Enumeration.TransactionStatus.NO.gettransactionstatus()){

						//For the first time when the data is sent, status of sent will be recorded in history
						syncLogging.info("SendDraftRecord method retryCount 6b 0 loop:");
						//						sQuery = " lock table locksynchronization " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
						//						jdbcTemplate.execute(sQuery);
						//
						//						sQuery = "select nsequenceno from seqnosyncmanagement where stablename = 'synchistory';";
						//
						//						int nsyncHistoryCode = jdbcTemplate.queryForObject(sQuery, Integer.class);
						//						nsyncHistoryCode++;
						//						syncLogging.info("SendDraftRecord method nsyncHistoryCode 6c:"+nsyncHistoryCode);

						sQuery = "insert into synchistory ("
								//+ "nsynchistorycode,"
								+ " nsyncbatchcode, sbatchtransferid, "
								+ " stransferid, sdestinationsitecode, ssourcesitecode,"
								+ " ntransferstatus, ntransfertype, dtransactiondatetime, nsitecode, nstatus) "
								+ " values ( "
								//+ "" + nsyncHistoryCode +","
								+ syncSendRecord.getNsyncbatchcode() + ",'"
								+ syncBatch.getSbatchtransferid() + "', '"
								+ syncSendRecord.getStransferid() + "', "
								+ "N'"+  stringUtilityFunction.replaceQuote(syncSendRecord.getSdestinationsitecode()) +"',"
								+  "N'"+  stringUtilityFunction.replaceQuote(syncSendRecord.getSsourcesitecode()) +"',"
								+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + "," + ntransferType
								+ ", NOW(), " + syncSendRecord.getNsitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ ")  ";
						jdbcTemplate.execute(sQuery);
						syncLogging.info("SendDraftRecord method synchistory for stransferid(Sent) 7= "
								+ syncSendRecord.getStransferid());

						syncLogging.info("SendDraftRecord method synchistory 8:"+ sQuery);

						//						final String updateSqQuery = "update seqnosyncmanagement set nsequenceno =" + nsyncHistoryCode + " where stablename='synchistory';";
						//
						//						jdbcTemplate.execute(updateSqQuery);
					}
				}

			} else {
				outputMap.put(META_DATA, completedBatchMetaData);
				syncLogging.info("SendDraftRecord method 9:"+ completedBatchMetaData);
			}
		}
		catch(Exception e)
		{
			syncLogging.error("exception in SendDraftRecord method 10:"+ e.getMessage());
			syncLogging.error("Excepton in SendDraftRecord method DAO 11:"+completedBatchMetaData);
			outputMap.put(META_DATA, completedBatchMetaData);
		}

		return outputMap;
	}


	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> receiveRecords(Map<String, Object> inputMap) throws Exception {

		syncLogging.info("-----------------------------Receive Record DAO Loop start---- 1 ----inputMap:");
		String sQuery = "";

		final Map<String, Object> outputMap = new HashMap<>();
		final Map<String, Object> objMetaData = new HashMap<>();
		final Map<String, Object> objLims_Msg = new HashMap<>();

		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		Map<String, Object> metaData = null;

		if (inputMap.containsKey(META_DATA))
		{
			metaData = (Map<String, Object>) inputMap.get(META_DATA);
		}
		else
		{
			metaData = inputMap;

		}

		Map<String, Object> limsMsg = (Map<String, Object>) metaData.get(LIMS_MESSAGE);
		syncLogging.info("Receive Record 1:"+metaData +"," + limsMsg);

		long hoursDifference=0;

		try {
			if (metaData != null) {

				final Map<String, Object> finalResponseObject = new HashMap<String, Object>();
				finalResponseObject.put(SITE_NAME, "");
				finalResponseObject.put(TRANSFER_ID, metaData.get(TRANSFER_ID));
				finalResponseObject.put(STATUS, "Completed");
				finalResponseObject.put(MESSAGE_TYPE, NO_DATA_FOUND);
				finalResponseObject.put(LIMS_COMMENT, NO_DATA_FOUND);

				syncLogging.info("Received Records 2:"+ finalResponseObject);

				if (metaData.get(MESSAGE_TYPE).equals("FinalStatusUpdate")) {

					// limsMsg.get("BatchFinalStatus") - Success/Failure
					//					Integer batchFinalStatus = (Integer) limsMsg.get("BatchFinalStatus");
					//					if(batchFinalStatus == Enumeration.TransactionStatus.SUCCESS.gettransactionstatus())
					//					{
					//						batchFinalStatus =Enumeration.TransactionStatus.SENTSUCCESS.gettransactionstatus();
					//					}
					//					else if(batchFinalStatus == Enumeration.TransactionStatus.FAIL.gettransactionstatus())
					//					{
					//						batchFinalStatus = Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus();
					//					}
					//					syncLogging.info("Received Records 3:"+ batchFinalStatus);
					syncLogging.info("Received Records 3:"+ limsMsg.get("BatchFinalStatus"));

					jdbcTemplate.execute("update syncbatch set nbatchtransferstatus = "
							+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
							+ ", dbatchenddatetime= Now() "
							+ " , nbatchfinalstatus = '" + limsMsg.get("BatchFinalStatus")
							+ "'  where sbatchtransferid = '" + limsMsg.get(BATCH_TRANSFER_ID) + "'");

					jdbcTemplate.execute("update synchronization set nsyncstatus = "
							+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
							+ ", dlastsyncdatetime=dsyncdatetime "
							+ " where nsynchronizationcode = (select nsynchronizationcode from syncbatch"
							+ " where sbatchtransferid = '" + limsMsg.get(BATCH_TRANSFER_ID) + "'"
							+ " and nbatchtransferstatus="+	 Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
							+ " and nbatchfinalstatus = "+ limsMsg.get("BatchFinalStatus") + ") "
							//+ " and sdestinationsitecode=N'"+  stringUtilityFunction.replaceQuote((String)limsMsg.get(SOURCE_SITE_NAME))+"'"
							);

					//finalResponseObject.put(STATUS, "Completed");


					if(Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus() == (Integer)limsMsg.get("BatchFinalStatus"))
					{
						final String errorMsgSync = "1 - Sync status reverted back to Draft as process failed - "
								+ LocalDateTime.now().toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
						syncLogging.info("Received Records 3a:"+ errorMsgSync);

						//Update coareporthistorygeneration only for coaparent based sync)
						jdbcTemplate.execute("update coareporthistorygeneration set ssynccomments='"
								+ errorMsgSync
								+ "' , nsyncstatus= "
								+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
								+ " where ncoareporthistorycode =( "
								+ " select s.ncoareporthistorycode from  syncbatch sb, synchronization s where "
								+ " sb.nsynchronizationcode = s.nsynchronizationcode "
								+ " and ndatewisesync = " + Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ " and  sb.sbatchtransferid = '" + limsMsg.get(BATCH_TRANSFER_ID) + "')");
					}

					outputMap.put(META_DATA, finalResponseObject);

					syncLogging.info("Receive FinalStatusUpdate =  " + limsMsg.get("BatchFinalStatus")+ " BatchTransferID = " + limsMsg.get(BATCH_TRANSFER_ID));
					syncLogging.info("Received Records 4:"+ outputMap);

				}
				else if (metaData.get(MESSAGE_TYPE).equals(RESPONSE)) {
					syncLogging.info("Response msg type loop 5:");

					//					sQuery = " lock table locksynchronization " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					//					jdbcTemplate.execute(sQuery);

					//					final String sequenceno = "select nsequenceno, stablename from seqnosyncmanagement where stablename ='synchistory';";
					//					final List<SeqNoSyncManagement> lstSeqNo = jdbcTemplate.query(sequenceno, new SeqNoSyncManagement());
					//
					//					final Map<String, Integer> seqMap = lstSeqNo.stream().collect(Collectors.toMap(
					//							SeqNoSyncManagement::getStablename, SeqNoSyncManagement::getNsequenceno));

					//					final String sequenceno = "select nsequenceno from seqnosyncmanagement where stablename = 'synchistory';";
					//					int nsyncHistoryCode = jdbcTemplate.queryForObject(sequenceno, Integer.class);

					//int nsyncHistoryCode = seqMap.get("synchistory") ;
					//nsyncHistoryCode++;

					String responseMsg = (String) metaData.get(STATUS);
					int responseStatus = Enumeration.TransactionStatus.RECEIVED.gettransactionstatus();

					//Commented by Subashini as error msg displayed as 'Duplicate' and to mark its sent status
					// as received
					//					if(responseMsg.equalsIgnoreCase("Duplicate")){
					//						responseStatus = Enumeration.TransactionStatus.FAIL.gettransactionstatus();
					//					}

					final Map<String, Object> msgObject = new HashMap<>();
					msgObject.put(LIMS_COMMENT, responseMsg);

					final JSONObject errorMsg = new JSONObject(msgObject);

					//adding where not exists in below insert query as the reason for the duplicate
					//response is not yet figured out on both interfacer and LIMS Side
					sQuery = "insert into synchistory ("
							//+ "nsynchistorycode,"
							+ " nsyncbatchcode, sbatchtransferid, "
							+ " stransferid, sdestinationsitecode,ssourcesitecode, "
							+ " ntransferstatus, ntransfertype, jerrormsg,"
							+ " dtransactiondatetime, nsitecode, nstatus) "
							//+ " values ("
							+ "select "
							//+ nsyncHistoryCode +","
							+ " (select nsyncbatchcode from syncbatch where sbatchtransferid='"+limsMsg.get(BATCH_TRANSFER_ID) + "'),"
							+ "'"+ limsMsg.get(BATCH_TRANSFER_ID) + "', '"
							+ metaData.get(TRANSFER_ID) + "', '"
							+ limsMsg.get(SOURCE_SITE_NAME)+ "','"
							+  limsMsg.get(DESTINATION_SITE_NAME)+ "',"
							+ responseStatus + ","
							+ (int) limsMsg.get("TransferType") + ","
							+  "'" + stringUtilityFunction.replaceQuote(errorMsg.toString())	+ "'::jsonb"
							+ ", NOW(), "
							+ limsMsg.get(DESTINATION_SITE_CODE)+ ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " where not exists (select 1 from synchistory where "
									+ " sbatchtransferid = '"+ limsMsg.get(BATCH_TRANSFER_ID) +"'"
									+ " and stransferid = '" + metaData.get(TRANSFER_ID) + "'"
									+ " and ntransferstatus = " + responseStatus
							+ ")";

					syncLogging.info("Received Records_6:"+ sQuery);

					jdbcTemplate.execute(sQuery);

					//finalResponseObject.put(STATUS, responseMsg.equalsIgnoreCase("Duplicate")?"Duplicate":"Success");

					outputMap.put(META_DATA, finalResponseObject);

					syncLogging.info("Received Records_7:"+ finalResponseObject);

					//					final String updateSqQuery = "update seqnosyncmanagement set nsequenceno =" + nsyncHistoryCode + " where stablename='synchistory';";
					//
					//					jdbcTemplate.execute(updateSqQuery);

				}
				else if (metaData.get(MESSAGE_TYPE).equals("Error")) {

					syncLogging.info("Error Loop 8:");

					//					sQuery = " lock table locksynchronization " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					//					jdbcTemplate.execute(sQuery);


					//					final String sequenceno = "select nsequenceno, stablename from seqnosyncmanagement where stablename in "
					//							+ " ('synchistory');";
					//					final List<SeqNoSyncManagement> lstSeqNo = (List<SeqNoSyncManagement>) jdbcTemplate.query(sequenceno, new SeqNoSyncManagement());
					//
					//					final Map<String, Integer> seqMap = lstSeqNo.stream().collect(Collectors.toMap(
					//							SeqNoSyncManagement::getStablename, seqNoSyncManagement -> seqNoSyncManagement.getNsequenceno()));

					//					int nsyncHistoryCode = seqMap.get("synchistory") ;

					//					final String sequenceno = "select nsequenceno from seqnosyncmanagement where stablename = 'synchistory';";
					//					int nsyncHistoryCode = jdbcTemplate.queryForObject(sequenceno, Integer.class);
					//
					//					nsyncHistoryCode++;

					//int historyCode1 = Enumeration.TransactionStatus.FAIL.gettransactionstatus();
					int historyCode = Enumeration.TransactionStatus.RECEIVED.gettransactionstatus();

					String executionStatus = "";
					if(limsMsg.get("ExecutionStatus") != null)
					{
						executionStatus = (String)limsMsg.get("ExecutionStatus");
					}

					JSONObject errorMsg =  new JSONObject(metaData);
					JSONObject historyMsg =  new JSONObject(metaData);
					syncLogging.info("Received Records_9:"+ executionStatus);

					if(executionStatus.equalsIgnoreCase("Error")) {
						final Map<String, Object> msgObject = new HashMap<String, Object>();
						msgObject.put(LIMS_COMMENT, Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

						historyMsg = new JSONObject(msgObject);
						//historyCode = Enumeration.TransactionStatus.SUCCESS.gettransactionstatus();
					}


					sQuery = "insert into synchistory ("
							//+ "nsynchistorycode,"
							+ " nsyncbatchcode, sbatchtransferid, "
							+ " stransferid, sdestinationsitecode, ssourcesitecode,"
							+ " ntransferstatus, ntransfertype, jerrormsg,"
							+ " dtransactiondatetime, nsitecode, nstatus) "
						//	+ " values ("
							+ " select "
							//+nsyncHistoryCode +","
							+ "-1,"
							+ "'"+ limsMsg.get(BATCH_TRANSFER_ID) + "', '"
							+ metaData.get(TRANSFER_ID) + "', '"
							+  limsMsg.get(SOURCE_SITE_NAME)+ "','"
							+  limsMsg.get(DESTINATION_SITE_NAME)+ "',"
							+ historyCode + ","
							+ (int) limsMsg.get("TransferType")
							+  ",'" + stringUtilityFunction.replaceQuote(historyMsg.toString())	+ "'::jsonb"
							+ ", NOW(), "
							+ limsMsg.get(DESTINATION_SITE_CODE) + ","
							//+ "(select nsitecode from site where ssitecode='" +  limsMsg.get(DESTINATION_SITE_NAME) + "'),"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " where not exists (select 1 from synchistory where "
											+ " sbatchtransferid = '"+ limsMsg.get(BATCH_TRANSFER_ID) +"'"
											+ " and stransferid = '" + metaData.get(TRANSFER_ID) + "'"
											+ " and ntransferstatus = " + historyCode
							+ ")";

					syncLogging.info("Received Records_10:"+ sQuery);

					jdbcTemplate.execute(sQuery);

					//					final String updateSqQuery = "update seqnosyncmanagement set nsequenceno =" + nsyncHistoryCode + " where stablename='synchistory';";
					//
					//					jdbcTemplate.execute(updateSqQuery);

					//					jdbcTemplate.execute("update syncbatch set nbatchtransferstatus = "
					//							+ Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus()
					//							+ ", dbatchenddatetime= Now()"
					//							+ " , nbatchfinalstatus = '" + Enumeration.TransactionStatus.FAIL.gettransactionstatus()
					//							+ "' , jerrormsg = '" + stringUtilityFunction.replaceQuote(errorMsg.toString()) + "'::jsonb where sbatchtransferid = '" + limsMsg.get(BATCH_TRANSFER_ID) + "'");


					jdbcTemplate.execute("update syncbatch set nbatchtransferstatus = "
							+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
							+ ", dbatchenddatetime= Now()"
							+ " , nbatchfinalstatus = '" + Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus()
							+ "' , jerrormsg = '" + stringUtilityFunction.replaceQuote(errorMsg.toString()) + "'::jsonb where sbatchtransferid = '" + limsMsg.get(BATCH_TRANSFER_ID) + "'");


					finalResponseObject.put(STATUS, Enumeration.ReturnStatus.FAILED.getreturnstatus());

					outputMap.put(META_DATA, finalResponseObject);

					syncLogging.info("Received Error loop 11:" + metaData.get(TRANSFER_ID)
					+ " BatchTransferID = " + limsMsg.get(BATCH_TRANSFER_ID));

					syncLogging.info("Received Records_12:"+ outputMap);


				}
				else if (metaData.get(MESSAGE_TYPE).equals("Data")) {

					final Map<String, Object> data = (Map<String, Object>) inputMap.get("Data");
					syncLogging.info("Receive Record 13:"+data);

					final String stableName = (String) data.get(S_TABLE_NAME);
					final String stransferID = (String) data.get(S_TRANSFER_ID);
					syncLogging.info("Receive Record 14:"+data.get(N_SORTER));

					final short nsorter = ((Integer) data.get(N_SORTER)).shortValue();
					final String siteJsonData = mapper//.writerWithDefaultPrettyPrinter()
							.writeValueAsString(data.get("records"));

					syncLogging.info("Received 15:" +limsMsg.get(BATCH_TRANSFER_ID) +","+ stransferID);

					final String batchfailedQry="select * from syncbatch where ndestinationsitecode="+limsMsg.get(SOURCE_SITE_CODE)+""
							+ " and nbatchfinalstatus ="+Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus()+""
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by nsyncbatchcode desc LIMIT 1 ";
					final SyncBatch failedBatch = (SyncBatch)jdbcUtilityFunction.queryForObject(batchfailedQry, SyncBatch.class, jdbcTemplate);

					syncLogging.info("Received Records_16:"+ failedBatch);

					if (siteJsonData != null) {

						final int receivedTableRecordCount = jdbcTemplate.queryForObject("select count(nsyncreceivedrecordcode)"
								+ "  from syncreceivedrecord where "
								+ " sbatchtransferid = '"+ limsMsg.get(BATCH_TRANSFER_ID) +"'"
								+ " and stransferid = '" + stransferID + "'"
								+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";",Integer.class);

						syncLogging.info("receivedTableRecordCount:"+receivedTableRecordCount
								+ ":" + "select count(nsyncreceivedrecordcode)"
								+ "  from syncreceivedrecord where "
								+ " sbatchtransferid = '"+ limsMsg.get(BATCH_TRANSFER_ID) +"'"
								+ " and stransferid = '" + stransferID + "'");

						if (receivedTableRecordCount == 0) {
							if(failedBatch==null) {

								//replaced dbatchenddatetime with dbatchstarttime as at some case batch has not ended
								//because of not successful receiving of records even after 2 hrs.
								final String batchInterQry="select sbatchtransferid,COALESCE(TO_CHAR(dbatchstartdatetime,'yyyy-MM-dd HH24:mi:ss'),'') "
										+ " as sbatchstartdatetime,nsynchronizationcode from syncbatch where ndestinationsitecode="+limsMsg.get(SOURCE_SITE_CODE)+" "
										+ " and nbatchfinalstatus ="+Enumeration.TransactionStatus.INTERMEDIATE.gettransactionstatus()+""
										+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by nsyncbatchcode asc LIMIT 1 ";

								syncLogging.info("received_Intermediate_Delete_17:"+ batchInterQry);

								final SyncBatch intermediateBatch = (SyncBatch)jdbcUtilityFunction.queryForObject(batchInterQry, SyncBatch.class, jdbcTemplate);

								final String intermediateHoursQry="select ssettingvalue from settings where nsettingcode=68 and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
								final int hours=	jdbcTemplate.queryForObject(intermediateHoursQry, Integer.class);

								if(intermediateBatch != null && intermediateBatch.getSbatchstartdatetime()!=null){

									syncLogging.info("received_Intermediate_Delete_18:"+ intermediateBatch.getSbatchtransferid());
									syncLogging.info("received_Intermediate_Delete_19:"+ intermediateBatch.getSbatchstartdatetime());
									syncLogging.info("received_Intermediate_Delete_20:"+ intermediateBatch.getSbatchenddatetime());
									syncLogging.info("received_Intermediate_Delete_21:"+ intermediateBatch.getNsynchronizationcode());

									DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
									LocalDateTime now = LocalDateTime.now();
									LocalDateTime givenDateTime = LocalDateTime.parse(intermediateBatch.getSbatchstartdatetime(), dtf);

									syncLogging.info("received_Intermediate_Delete_22:"+ givenDateTime);

									hoursDifference = ChronoUnit.HOURS.between(givenDateTime, now);
								}
								syncLogging.info("received_Intermediate_Delete_23:"+ hoursDifference);

								if(intermediateBatch !=null //&& intermediateBatch.getSbatchenddatetime()!= null
										&& Math.abs(hoursDifference) >= hours) {

									final String sdeleteQuery =
											//" update synchronization set nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()+" "
											//+ " where nsynchronizationcode="+IntermediateBatch.getNsynchronizationcode()+";"
											//											" update syncbatch set nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()+" "
											//											+ " where sbatchtransferid='"+intermediateBatch.getSbatchtransferid()+"';"
											//											+ " update synchistory set nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()+" "
											//											+ " where sbatchtransferid='"+intermediateBatch.getSbatchtransferid()+"';"
											//											+ " update syncreceivedrecord set nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()+" "
											//											+ " where sbatchtransferid='"+intermediateBatch.getSbatchtransferid()+"';";
											" update syncbatch set nbatchfinalstatus=" + Enumeration.TransactionStatus.RECEIVEFAILED.gettransactionstatus()+" "
											+ " where sbatchtransferid='"+intermediateBatch.getSbatchtransferid()+"';";
									syncLogging.info("received_Intermediate_Delete_24:"+ sdeleteQuery);

									jdbcTemplate.execute(sdeleteQuery);

									outputMap.put(META_DATA, finalResponseObject);

									syncLogging.info("received_Intermediate_Delete_25:"+ finalResponseObject);

								}
								else {

									syncLogging.info("receivedTableRecordCount 26 loop:");

									//									sQuery = " lock table locksynchronization " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
									//									jdbcTemplate.execute(sQuery);
									//
									//									sQuery = " lock table lockreceiverecords " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
									//									jdbcTemplate.execute(sQuery);
									//
									//									syncLogging.info("locked tables for insert 27:");
									//
									//									final String sequenceno = "select nsequenceno, stablename from seqnosyncmanagement where stablename in "
									//											+ " ('syncbatch','synchistory', 'syncreceivedrecord');";
									//									final List<SeqNoSyncManagement> lstSeqNo = (List<SeqNoSyncManagement>) jdbcTemplate.query(sequenceno, new SeqNoSyncManagement());
									//
									//									final Map<String, Integer> seqMap = lstSeqNo.stream().collect(Collectors.toMap(
									//											SeqNoSyncManagement::getStablename, seqNoSyncManagement -> seqNoSyncManagement.getNsequenceno()));
									//
									//									int nsyncBatchCode = seqMap.get("syncbatch") ;
									//									int nsyncHistoryCode = seqMap.get("synchistory") ;
									//									int nsyncReceivedCode = seqMap.get("syncreceivedrecord") ;
									//
									//									nsyncReceivedCode++;
									//									nsyncBatchCode++;
									//									nsyncHistoryCode++;

									//									sQuery = "insert into syncreceivedrecord (nsyncreceivedrecordcode,"
									//											+ " ssourcesitecode,sbatchtransferid,stransferid,"
									//											+ " stablename, nsorter, jsondata,dtransactiondatetime, nsitecode, nstatus) "
									//											+ " values ( " + nsyncReceivedCode + ",'"
									//											+ limsMsg.get(SOURCE_SITE_NAME)//data received from site
									//											+ "','" + limsMsg.get(BATCH_TRANSFER_ID) + "', "
									//											+ "'" + stransferID + "',"
									//											+ "'" + stableName + "',"+nsorter + ","
									//											+ "'" + stringUtilityFunction.replaceQuote(siteJsonData) + "', NOW(),"
									//											+ limsMsg.get(DESTINATION_SITE_CODE)+","
									//											//+ metaData.get(SITE_NAME) + ", "
									//											//+ "(select nsitecode from site where ssitecode='" +  metaData.get(SITE_NAME) + "'),"
									//											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")  ";

									//Commented above and added below query by L.Subashini 12-09-2024 as a fix for duplicate insert of same transfer id
									sQuery = "insert into syncreceivedrecord ("
											//+ "nsyncreceivedrecordcode,"
											+ " ssourcesitecode,sbatchtransferid,stransferid,"
											+ " stablename, nsorter, jsondata,dtransactiondatetime, nsitecode, nstatus) "
											+ " select '"
											//	+ "" + nsyncReceivedCode + ",'"
											+ limsMsg.get(SOURCE_SITE_NAME)
											+ "','" + limsMsg.get(BATCH_TRANSFER_ID) + "', "
											+ "'" + stransferID + "',"
											+ "'" + stableName + "',"+nsorter + ","
											+ "'" + stringUtilityFunction.replaceQuote(siteJsonData) + "', NOW(),"
											+ limsMsg.get(DESTINATION_SITE_CODE)+","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " where not exists (select 1 from syncreceivedrecord where "
											+ " sbatchtransferid = '"+ limsMsg.get(BATCH_TRANSFER_ID) +"'"
											+ " and stransferid = '" + stransferID + "')";


									jdbcTemplate.execute(sQuery);

									syncLogging.info("Received Data for table 28:"+ stableName+":"+metaData.get(TRANSFER_ID)+ " BatchTransferID = " + limsMsg.get(BATCH_TRANSFER_ID));


									sQuery = "select * from syncbatch where sbatchtransferid = '"+ limsMsg.get(BATCH_TRANSFER_ID) + "'"
											//+ " and ndestinationsitecode="+ metaData.get(SOURCE_SITE_NAME)
											+ " and ntransfertype="+ limsMsg.get("TransferType")
											+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

									SyncBatch syncBatchHistory = (SyncBatch) jdbcUtilityFunction.queryForObject(sQuery,	SyncBatch.class, jdbcTemplate);
									syncLogging.info("received syn histry 29:"+syncBatchHistory);
									if (syncBatchHistory == null) {


										sQuery = "insert into syncbatch("
												//+ "nsyncbatchcode,"
												+ "nsynchronizationcode, sbatchtransferid,nbatchtransferstatus,"
												+ " nbatchfinalstatus, ntransfertype, dbatchstartdatetime, "//dbatchenddatetime,"
												+ " ndestinationsitecode,sdestinationsitecode,ssourcesitecode, nsitecode,nstatus, nbatchcount)"
												+ " values ( "
												//+ "" + nsyncBatchCode +", "
												+ " -1"//+ nsynchronizationcode
												+ ",'" + limsMsg.get(BATCH_TRANSFER_ID) + "', "
												+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", "
												+ Enumeration.TransactionStatus.INTERMEDIATE.gettransactionstatus() + ","
												+ limsMsg.get("TransferType") + ", NOW(),"//NOW(), "
												+ limsMsg.get(SOURCE_SITE_CODE) + ","
												+ "'"+ limsMsg.get(SOURCE_SITE_NAME) + "',"
												+ "'"+ limsMsg.get(DESTINATION_SITE_NAME) + "',"
												+ limsMsg.get(DESTINATION_SITE_CODE) + ", "
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ "," +limsMsg.get("BatchCount")
												+ ")";
										syncLogging.info("Received Records_30:"+ syncBatchHistory);

										jdbcTemplate.execute(sQuery);
										syncLogging.info("Received Batch Inserted 31:"+limsMsg.get(BATCH_TRANSFER_ID));


									}

									sQuery = "insert into synchistory ("
											//+ "nsynchistorycode, "
											+ "nsyncbatchcode, sbatchtransferid, "
											+ " stransferid, sdestinationsitecode,ssourcesitecode, "
											+ " ntransferstatus, ntransfertype, "
											+ " dtransactiondatetime, nsitecode, nstatus) "
										//	+ " values ("
											+ " select "
											//+ nsyncHistoryCode +","
											+ "(select nsyncbatchcode from syncbatch where sbatchtransferid='"+limsMsg.get(BATCH_TRANSFER_ID) + "'),"
											+ "'"+ limsMsg.get(BATCH_TRANSFER_ID) + "', '"
											+ metaData.get(TRANSFER_ID) + "', '"
											+ limsMsg.get(SOURCE_SITE_NAME)+ "','"
											+ limsMsg.get(DESTINATION_SITE_NAME)+ "',"
											+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ","
											+ (int) limsMsg.get("TransferType") + ","
											+ " NOW(), "
											+ limsMsg.get(DESTINATION_SITE_CODE) + ", "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													+ " where not exists (select 1 from synchistory where "
													+ " sbatchtransferid = '"+ limsMsg.get(BATCH_TRANSFER_ID) +"'"
													+ " and stransferid = '" + metaData.get(TRANSFER_ID) + "'"
													+ " and ntransferstatus = " + Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()
											+ ")";

									syncLogging.info("Received Records_32:"+ sQuery);

									jdbcTemplate.execute(sQuery);

									//received records execution will be done here
									//
									//									final String updateSqQuery = "update seqnosyncmanagement set nsequenceno =" + nsyncBatchCode
									//											+ " where stablename='syncbatch';"
									//											+ "update seqnosyncmanagement set nsequenceno =" + nsyncReceivedCode
									//											+ " where stablename='syncreceivedrecord';"
									//											+ "update seqnosyncmanagement set nsequenceno =" + nsyncHistoryCode
									//											+ " where stablename='synchistory';";
									//
									//									jdbcTemplate.execute(updateSqQuery);

									objLims_Msg.put(BATCH_TRANSFER_ID, limsMsg.get(BATCH_TRANSFER_ID));
									objLims_Msg.put("TransferType", limsMsg.get("TransferType"));

									objLims_Msg.put(SOURCE_SITE_NAME, limsMsg.get(DESTINATION_SITE_NAME));
									objLims_Msg.put(DESTINATION_SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
									objLims_Msg.put(SOURCE_SITE_CODE, limsMsg.get(DESTINATION_SITE_CODE));
									objLims_Msg.put(DESTINATION_SITE_CODE, limsMsg.get(SOURCE_SITE_CODE));

									//syncLogging.info("response to site 33:"+ limsMsg.get(SOURCE_SITE_NAME));

									//syncLogging.info("Received Records_34:"+ objLims_Msg);

									objMetaData.put(SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
									objMetaData.put(TRANSFER_ID, stransferID);

									objMetaData.put(STATUS, Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
									objMetaData.put(MESSAGE_TYPE, RESPONSE);
									objMetaData.put(LIMS_COMMENT, NO_DATA_FOUND);
									objMetaData.put(LIMS_MESSAGE, objLims_Msg);

									//syncLogging.info("metadata for respnse to send 35:"+objMetaData);
									outputMap.put(META_DATA,objMetaData );

									syncLogging.info("Received Records_33:"+ outputMap);
								}


							}

						} else {

							syncLogging.info("Duplicate loop 37:");
							objLims_Msg.put("TransferType", limsMsg.get("TransferType"));
							objLims_Msg.put(BATCH_TRANSFER_ID, limsMsg.get(BATCH_TRANSFER_ID));
							objLims_Msg.put(SOURCE_SITE_NAME, limsMsg.get(DESTINATION_SITE_NAME));
							objLims_Msg.put(DESTINATION_SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
							objLims_Msg.put(SOURCE_SITE_CODE, limsMsg.get(DESTINATION_SITE_CODE));
							objLims_Msg.put(DESTINATION_SITE_CODE, limsMsg.get(SOURCE_SITE_CODE));

							objMetaData.put(SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
							objMetaData.put(TRANSFER_ID, stransferID);
							objMetaData.put(STATUS, "Duplicate");
							objMetaData.put(MESSAGE_TYPE, RESPONSE);
							objMetaData.put(LIMS_COMMENT, NO_DATA_FOUND);
							objMetaData.put(LIMS_MESSAGE, objLims_Msg);

							outputMap.put(META_DATA, objMetaData);

							syncLogging.info("Duplicate loop dao 38:" +outputMap);

							syncLogging.info("Received Records_39:"+ outputMap);

						}

					}
				}
				else if (metaData.get(MESSAGE_TYPE).equals(ACKNOWLEDGEMENT)) {
					syncLogging.info("START OF ACKNOWLEDGEMENT msg type loop 40:"+ inputMap);

					if(metaData.get(TRANSFER_ID) != null  && ((String) metaData.get(TRANSFER_ID)).trim().length() > 0)
					{
						final String acknowledgeQry = "update syncsendrecord set nacknowledgestatus = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " where stransferid = '" +metaData.get(TRANSFER_ID)+"'";
						syncLogging.info("ACKNOWLEDGEMENT update qry 41:"+acknowledgeQry);

						jdbcTemplate.execute(acknowledgeQry);
					}

					syncLogging.info("END OF ACKNOWLEDGEMENT msg type loop 42:");

					outputMap.put(META_DATA, finalResponseObject);
				}
				else{
					syncLogging.info("Received Records_43:"+ finalResponseObject);

					//	finalResponseObject.put(STATUS, "Draft");
					//outputMap.put(META_DATA, finalResponseObject);

				}
			}
			syncLogging.info("------Receive Record DAO Loop end-----------1:"+outputMap);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} catch (Exception e) {

			syncLogging.error("error 44:"+e);
			syncLogging.error("msg1 45:"+ limsMsg);

			objLims_Msg.put("TransferType", limsMsg.get("TransferType"));
			objLims_Msg.put(BATCH_TRANSFER_ID, limsMsg.get(BATCH_TRANSFER_ID));
			objLims_Msg.put(SOURCE_SITE_NAME, limsMsg.get(DESTINATION_SITE_NAME));
			objLims_Msg.put(DESTINATION_SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
			objLims_Msg.put(SOURCE_SITE_CODE, limsMsg.get(DESTINATION_SITE_CODE));
			objLims_Msg.put(DESTINATION_SITE_CODE, limsMsg.get(SOURCE_SITE_CODE));

			objMetaData.put(SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
			objMetaData.put(TRANSFER_ID, metaData.get(TRANSFER_ID));
			objMetaData.put(STATUS, "Error");
			objMetaData.put(MESSAGE_TYPE, "Error");
			objMetaData.put(LIMS_COMMENT, e.getMessage());
			objMetaData.put(LIMS_MESSAGE, objLims_Msg);

			outputMap.put(META_DATA, objMetaData);

			syncLogging.error("error output 46:"+outputMap);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	//	@SuppressWarnings("unchecked")
	//	public ResponseEntity<Object> executeReceivedRecord(final Map<String, Object> inputMap) throws Exception
	//	{
	//
	//		syncLogging.info("executeReceivedRecord");
	//		final Map<String, Object> outputMap = new HashMap<>();
	//		final Map<String, Object> objMetaData = new HashMap<String, Object>();
	//		final Map<String, Object> objLims_Msg = new HashMap<String, Object>();
	//
	//		final ObjectMapper mapper = new ObjectMapper();
	//		mapper.registerModule(new JavaTimeModule());
	//
	//		Map<String, Object> metaData = (Map<String, Object>) inputMap.get(META_DATA);
	//
	//		Map<String, Object> limsMsg = (Map<String, Object>) metaData.get(LIMS_MESSAGE);
	//
	//		try {
	//			final Map<String, Object> data = (Map<String, Object>) inputMap.get("Data");
	//
	//			final String stransferID = (String) data.get(S_TRANSFER_ID);
	//
	//			String sReturnMsg = Enumeration.ReturnStatus.FAILED.getreturnstatus();
	//
	//			syncLogging.info("Execute Received Record_1:"+ metaData);
	//			syncLogging.info("Execute Received Record_2:"+ limsMsg);
	//
	//			objLims_Msg.put(BATCH_TRANSFER_ID, limsMsg.get(BATCH_TRANSFER_ID));
	//			objLims_Msg.put("TransferType", limsMsg.get("TransferType"));
	//			objLims_Msg.put(SOURCE_SITE_NAME, limsMsg.get(DESTINATION_SITE_NAME));
	//			objLims_Msg.put(DESTINATION_SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
	//			objLims_Msg.put(SOURCE_SITE_CODE, limsMsg.get(DESTINATION_SITE_CODE));
	//			objLims_Msg.put(DESTINATION_SITE_CODE, limsMsg.get(SOURCE_SITE_CODE));
	//			//objLims_Msg.put("ExecutionStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
	//
	//			objMetaData.put(STATUS, sReturnMsg);
	//			objMetaData.put(SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
	//			objMetaData.put(TRANSFER_ID, stransferID);
	//			objMetaData.put(LIMS_COMMENT, NO_DATA_FOUND);
	//
	//			if ((int) limsMsg.get("BatchCount") == (int) limsMsg.get("RemainCount"))
	//			{
	//				syncLogging.info("BatchCount == RemainCount:"+limsMsg.get("BatchCount")
	//							+",Batch Transfer ID:"+limsMsg.get(BATCH_TRANSFER_ID));
	//
	//				int receivedRecordCount = 0;
	//				do
	//				{
	//					Thread.sleep(2000);//2 seconds
	//					final String receivedQryString = "select count(nsyncreceivedrecordcode) from syncreceivedrecord "
	//							+ " where sbatchtransferid="
	//							+ "'" + limsMsg.get(BATCH_TRANSFER_ID) + "' and nstatus="
	//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	//
	//					receivedRecordCount = jdbcTemplate.queryForObject(receivedQryString, Integer.class);
	//
	//					syncLogging.info("receivedRecordCount:"+ receivedRecordCount + " , Batch Count :"+ limsMsg.get("BatchCount")
	//								+",Batch Transfer ID:"+limsMsg.get(BATCH_TRANSFER_ID));
	//				}
	//				//11-09-2024 - Below condition changed by L. Subashini from != to < as this condition is executing repeatedly when there
	//				//duplicate entries in syncreceivedrecord table for the same transfer id.
	//				//Duplicate entry for a transfer id might be due to huge data record which is repeatedly sent
	//				//from interfacer tables.
	//				while(receivedRecordCount != 0 && receivedRecordCount < (int) limsMsg.get("BatchCount")) ;
	//
	//				int executionCount=0;
	//				int executionSuccessCount = 0;
	//
	//				do
	//				{
	//					syncLogging.info("do loop: i:"+ executionCount);
	//
	//					jdbcTemplate.execute("truncate table syncexecutionstatus");
	//					jdbcTemplate.execute("SELECT setval((SELECT pg_get_serial_sequence("
	//											 + "'syncexecutionstatus', 'nsyncexecutionstatuscode')),1)");
	//
	//					final String sQuery = "select * from fn_executesyncrecords(" + (int) limsMsg.get("TransferType")
	//									+ ",'" + limsMsg.get(BATCH_TRANSFER_ID) + "'," + limsMsg.get(DESTINATION_SITE_CODE) + ")";
	//
	//					syncLogging.info("Execute Received Record_3:"+ sQuery);
	//
	//					sReturnMsg = jdbcTemplate.queryForObject(sQuery, String.class);
	//					syncLogging.info("Execute Received Record_4:"+ sReturnMsg);
	//
	//					if (sReturnMsg.trim().length() > 0  &&
	//							sReturnMsg.equalsIgnoreCase(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
	//
	//						syncLogging.info("Execute Received Record_5 if loop:"+ sReturnMsg);
	//
	//						final String batchExecuteQry = "select count(*) from syncexecutionstatus where"
	//								+ " sbatchtransferid = '" + limsMsg.get(BATCH_TRANSFER_ID) + "'" ;
	//
	//						syncLogging.info("Execute Received Record_5a if loop:"+ batchExecuteQry);
	//
	//						executionSuccessCount = jdbcTemplate.queryForObject(batchExecuteQry, Integer.class);
	//
	//						syncLogging.info("executionSuccessCount 1:"+ executionSuccessCount);
	//					}
	//					executionCount++;
	//					syncLogging.info("executionSuccessCount: i:"+ executionCount);
	//				}
	//				while(executionSuccessCount != (int)limsMsg.get("BatchCount") && executionCount <= 2);
	//
	//
	//				jdbcTemplate.execute("truncate table syncexecutionstatus");
	//				jdbcTemplate.execute("SELECT setval((SELECT pg_get_serial_sequence("
	//										 + "'syncexecutionstatus', 'nsyncexecutionstatuscode')),1)");
	//
	//				syncLogging.info("truncate records:");
	//				int nfinalStatus = -1;
	//				if (sReturnMsg.trim().length() > 0  &&
	//						sReturnMsg.equalsIgnoreCase(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
	//
	//					syncLogging.info("Execute Received Record_6 if loop:"+ sReturnMsg+":"+executionSuccessCount
	//							+":"+ limsMsg.get("BatchCount"));
	//
	//					if (executionSuccessCount == (int)limsMsg.get("BatchCount"))
	//					{
	//						syncLogging.info("All tables executed:"+executionSuccessCount + ":"+ limsMsg.get("BatchCount"));
	//						nfinalStatus = Enumeration.TransactionStatus.RECEIVESUCCESS.gettransactionstatus();
	//
	//						objLims_Msg.put("ExecutionStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
	//
	//						objMetaData.put(STATUS, sReturnMsg);
	//						objMetaData.put(MESSAGE_TYPE, RESPONSE);
	//
	//					}
	//					else
	//					{
	//						syncLogging.info("All tables not executed:"+executionSuccessCount + ":"+ limsMsg.get("BatchCount"));
	//
	//
	//						nfinalStatus = Enumeration.TransactionStatus.RECEIVEFAILED.gettransactionstatus();
	//
	//						objLims_Msg.put("ExecutionStatus", "Error");
	//
	//						objMetaData.put(STATUS, "Error");
	//						objMetaData.put(MESSAGE_TYPE, "Error");
	//						objMetaData.put(LIMS_COMMENT, "Execution Failed");
	//					}
	//
	//
	//				} else {
	//					syncLogging.info("Execute Received Record_6 else loop:"+ sReturnMsg);
	//					nfinalStatus = Enumeration.TransactionStatus.RECEIVEFAILED.gettransactionstatus();
	//
	//					objLims_Msg.put("ExecutionStatus", "Error");
	//
	//					objMetaData.put(STATUS, "Error");
	//					objMetaData.put(MESSAGE_TYPE, "Error");
	//					objMetaData.put(LIMS_COMMENT, sReturnMsg);
	//
	//				}
	//
	//				syncLogging.info("Execute Received Record 7:"+ nfinalStatus);
	//
	//				jdbcTemplate.execute("update syncbatch set nbatchfinalstatus = "+ nfinalStatus
	//						+ ", dbatchenddatetime= Now() "
	//						+ " where sbatchtransferid = '"	+ limsMsg.get(BATCH_TRANSFER_ID) + "'");
	//
	//
	//				syncLogging.info("update syncbatchhistory set sbatchtransferid -> "
	//						+ limsMsg.get(BATCH_TRANSFER_ID));
	//
	//
	//			}
	//
	//			objMetaData.put(LIMS_MESSAGE, objLims_Msg);
	//
	//			outputMap.put(META_DATA, objMetaData);
	//
	//			syncLogging.info("Execute Received Record_8:"+ outputMap);
	//			syncLogging.info("-----------execute loop end-------------:"+outputMap);
	//
	//			return new ResponseEntity<>(outputMap, HttpStatus.OK);
	//
	//		}
	//		catch (Exception e) {
	//
	//			syncLogging.error("e2:"+ e);
	//			syncLogging.error("msg2:"+ limsMsg);
	//			objLims_Msg.put("TransferType", limsMsg.get("TransferType"));
	//			objLims_Msg.put(BATCH_TRANSFER_ID, limsMsg.get(BATCH_TRANSFER_ID));
	//			objLims_Msg.put(SOURCE_SITE_NAME, limsMsg.get(DESTINATION_SITE_NAME));
	//			objLims_Msg.put(DESTINATION_SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
	//			objLims_Msg.put(SOURCE_SITE_CODE, limsMsg.get(DESTINATION_SITE_CODE));
	//			objLims_Msg.put(DESTINATION_SITE_CODE, limsMsg.get(SOURCE_SITE_CODE));
	//			objLims_Msg.put("ExecutionStatus", "Error");
	//
	//			objMetaData.put(SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
	//			objMetaData.put(TRANSFER_ID, metaData.get(TRANSFER_ID));
	//			objMetaData.put(STATUS, "Error");
	//			objMetaData.put(MESSAGE_TYPE, "Error");
	//			objMetaData.put(LIMS_COMMENT, e.getMessage());
	//			objMetaData.put(LIMS_MESSAGE, objLims_Msg);
	//
	//			outputMap.put(META_DATA, objMetaData);
	//			syncLogging.info("execute loop catch:"+outputMap);
	//
	//			syncLogging.info("Execute Received Record_9:"+ outputMap);
	//
	//			return new ResponseEntity<>(outputMap, HttpStatus.OK);
	//		}
	//
	//
	//	}

	//	public ResponseEntity<Object> executeReceivedRecord1(final Map<String, Object> inputMap) throws Exception
	//	{
	//		syncLogging.info("executeReceivedRecord1");
	//		final Map<String, Object> outputMap = new HashMap<>();
	//		final Map<String, Object> objMetaData = new HashMap<String, Object>();
	//		final Map<String, Object> objLims_Msg = new HashMap<String, Object>();
	//
	//		final ObjectMapper mapper = new ObjectMapper();
	//		mapper.registerModule(new JavaTimeModule());
	//
	//		Map<String, Object> metaData = (Map<String, Object>) inputMap.get(META_DATA);
	//
	//		Map<String, Object> limsMsg = (Map<String, Object>) metaData.get(LIMS_MESSAGE);
	//
	//		try {
	//			final Map<String, Object> data = (Map<String, Object>) inputMap.get("Data");
	//
	//			final String stransferID = (String) data.get(S_TRANSFER_ID);
	//
	//			String sReturnMsg = Enumeration.ReturnStatus.FAILED.getreturnstatus();
	//
	//			//			if (siteJsonData != null) {
	//
	//			final String ssourceTableName = "syncreceivedrecord";
	//			final String stablePrimaryKey = "nsyncreceivedrecordcode";
	//			syncLogging.info("Execute Received Record_1:"+ metaData);
	//			syncLogging.info("Execute Received Record_2:"+ limsMsg);
	//
	//			if ((int) limsMsg.get("BatchCount") == (int) limsMsg.get("RemainCount"))
	//			{
	//				String sQuery = "select * from  synctablelist where nstatus = "
	//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	//						+ " and ntransfertype in ( " + (int) limsMsg.get("TransferType") + ","
	//						+  Enumeration.TransactionStatus.BIDIRECTIONAL.gettransactionstatus() +") "
	//						+ " order by nsorter";
	//
	//				final List<SyncTableList> syncTableList = jdbcTemplate.query(sQuery,
	//						new SyncTableList());
	//				syncLogging.info("Execute Received Record_3:"+ syncTableList);
	//
	//				syncLogging.info("BatchCount == RemainCount");
	//
	//				if (syncTableList.size() > 0) {
	//					for (int j = 0; j < syncTableList.size(); j++) {
	//
	//						sQuery = "select * from  syncreceivedrecord where "
	//								+ " stablename = '"+ syncTableList.get(j).getStablename()+ "' "
	//								+ "  and sbatchtransferid = '" + limsMsg.get(BATCH_TRANSFER_ID)
	//								+ "' and nsitecode = "  + limsMsg.get(DESTINATION_SITE_CODE)
	//								//+ "(select nsitecode from site where ssitecode='" +  metaData.get(SITE_NAME) + "')"
	//								+ " order by 1";
	//
	//						syncLogging.info("execute loop2:"+sQuery);
	//						List<Map<String, Object>> receivedRecordList = jdbcTemplate
	//								.queryForList(sQuery);
	//						syncLogging.info("execute loop 3:"+receivedRecordList);
	//
	//						syncLogging.info("Execute Received Record_4:"+ receivedRecordList);
	//
	//						if (receivedRecordList.size() > 0) {
	//
	//							syncLogging.info("execute loop 4:"+receivedRecordList.size());
	//							for (int i = 0; i < receivedRecordList.size(); i++) {
	//
	//								syncLogging.info("Execute Records for table: "+ receivedRecordList.get(i)
	//								.get(S_TABLE_NAME));
	//
	//								sQuery = "select * from fn_syncrecords('" + ssourceTableName + "','"
	//										+ stablePrimaryKey + "',"
	//										//+ (int) metaData.get(SITE_NAME)+ ",'"
	//										+ (int) limsMsg.get(DESTINATION_SITE_CODE) + ",'"
	//										+ receivedRecordList.get(i).get(S_TABLE_NAME)
	//										+ "','" + syncTableList.get(j).getSseqnotablename() + "',"
	//										+ "'" + syncTableList.get(j).getStableprimarykey() + "',"
	//										+ receivedRecordList.get(i).get(stablePrimaryKey)
	//										+"," + syncTableList.get(j).getNinttypeprimarykey()
	//										+"," + syncTableList.get(j).getNupdatedatereqd()
	//										+ ","+syncTableList.get(j).getNsitecodereqd()+")";
	//
	//								syncLogging.info("function query:"+sQuery);
	//								syncLogging.info("Execute Received Record_5:"+ sQuery);
	//
	//								List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);
	//
	//								syncLogging.info("Execute Received Record_6:"+ list);
	//
	//								if (list.size() > 0) {
	//
	//									sReturnMsg = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
	//								} else {
	//									sReturnMsg = Enumeration.ReturnStatus.FAILED.getreturnstatus();
	//									break;
	//								}
	//
	//								syncLogging.info("Execute Insert/Update Status -> " + sReturnMsg);
	//							}
	//
	//						}
	//						if (sReturnMsg == Enumeration.ReturnStatus.FAILED.getreturnstatus()) {
	//							break;
	//						}
	//					}
	//					int nfinalStatus = -1;
	//					if (sReturnMsg == Enumeration.ReturnStatus.FAILED.getreturnstatus()) {
	//						nfinalStatus = Enumeration.TransactionStatus.RECEIVEFAILED.gettransactionstatus();
	//					} else {
	//						nfinalStatus = Enumeration.TransactionStatus.RECEIVESUCCESS.gettransactionstatus();
	//					}
	//
	//					syncLogging.info("Execute Received Record_7:"+ nfinalStatus);
	//
	//					//catch block error if uncommented below line
	//					jdbcTemplate.execute("update syncbatch set nbatchfinalstatus = "+ nfinalStatus
	//							+ ", dbatchenddatetime= Now() "
	//							+ " where sbatchtransferid = '"	+ limsMsg.get(BATCH_TRANSFER_ID) + "'");
	//
	//
	//					syncLogging.info("update syncbatchhistory set sbatchtransferid -> "
	//							+ limsMsg.get(BATCH_TRANSFER_ID));
	//
	//				}
	//
	//			}
	//			objLims_Msg.put(BATCH_TRANSFER_ID, limsMsg.get(BATCH_TRANSFER_ID));
	//			objLims_Msg.put("TransferType", limsMsg.get("TransferType"));
	//			//objMetaData.put(SOURCE_SITE_NAME, limsMsg.get(SITE_NAME));
	//			objLims_Msg.put(SOURCE_SITE_NAME, limsMsg.get(DESTINATION_SITE_NAME));
	//			objLims_Msg.put(DESTINATION_SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
	//			objLims_Msg.put(SOURCE_SITE_CODE, limsMsg.get(DESTINATION_SITE_CODE));
	//			objLims_Msg.put(DESTINATION_SITE_CODE, limsMsg.get(SOURCE_SITE_CODE));
	//			objLims_Msg.put("ExecutionStatus", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
	//
	//			objMetaData.put(SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
	//			objMetaData.put(TRANSFER_ID, stransferID);
	//			objMetaData.put(STATUS, sReturnMsg);
	//
	//			objMetaData.put(MESSAGE_TYPE, RESPONSE);
	//			objMetaData.put(LIMS_COMMENT, NO_DATA_FOUND);
	//			objMetaData.put(LIMS_MESSAGE, objLims_Msg);
	//
	//			outputMap.put(META_DATA, objMetaData);
	//
	//			syncLogging.info("Execute Received Record_8:"+ outputMap);
	//
	//			syncLogging.info("execute loop end:"+outputMap);
	//
	//			return new ResponseEntity<>(outputMap, HttpStatus.OK);
	//			//}
	//		}
	//		catch (Exception e) {
	//
	//			syncLogging.error("e2:"+ e);
	//			syncLogging.error("msg2:"+ limsMsg);
	//			objLims_Msg.put("TransferType", limsMsg.get("TransferType"));
	//			objLims_Msg.put(BATCH_TRANSFER_ID, limsMsg.get(BATCH_TRANSFER_ID));
	//			objLims_Msg.put(SOURCE_SITE_NAME, limsMsg.get(DESTINATION_SITE_NAME));
	//			objLims_Msg.put(DESTINATION_SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
	//			objLims_Msg.put(SOURCE_SITE_CODE, limsMsg.get(DESTINATION_SITE_CODE));
	//			objLims_Msg.put(DESTINATION_SITE_CODE, limsMsg.get(SOURCE_SITE_CODE));
	//			objLims_Msg.put("ExecutionStatus", "Error");
	//
	//			objMetaData.put(SITE_NAME, limsMsg.get(SOURCE_SITE_NAME));
	//			objMetaData.put(TRANSFER_ID, metaData.get(TRANSFER_ID));
	//			objMetaData.put(STATUS, "Error");
	//			objMetaData.put(MESSAGE_TYPE, "Error");
	//			objMetaData.put(LIMS_COMMENT, e.getMessage());
	//			objMetaData.put(LIMS_MESSAGE, objLims_Msg);
	//
	//			outputMap.put(META_DATA, objMetaData);
	//			syncLogging.info("execute loop catch:"+outputMap);
	//
	//			syncLogging.info("Execute Received Record_9:"+ outputMap);
	//
	//			return new ResponseEntity<>(outputMap, HttpStatus.OK);
	//		}
	//
	//	}


	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> syncRecords(final UserInfo userInfo, final int nsyncType) throws Exception
	{
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String settingSiteQry = "select sc.nsitecode, sc.nissyncserver, sc.nisstandaloneserver, s.ssitecode "
				+ " from settings ss, siteconfig sc, site s "
				+ " where ss.nsettingcode = 30 "
				+ " and ss.ssettingvalue=s.ssitecode and sc.nsitecode=s.nsitecode"
				+ " and sc.nstatus=ss.nstatus and ss.nstatus=s.nstatus "
				+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		syncLogging.info("sync Records_1:"+ settingSiteQry);

		final Site site =(Site) jdbcUtilityFunction.queryForObject(settingSiteQry, Site.class, jdbcTemplate);

		syncLogging.info("sync Records_2:"+ site);

		if(nsyncType == Enumeration.TransactionStatus.AUTO.gettransactionstatus()) {
			final String settingUTCQry="select ssettingvalue from settings where nsettingcode=21 and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final int isUtcEnabled=	jdbcTemplate.queryForObject(settingUTCQry, Integer.class);
			userInfo.setIsutcenabled(isUtcEnabled);
		}
		if(site == null && nsyncType!=Enumeration.TransactionStatus.AUTO.gettransactionstatus())
		{
			outputMap.put("SyncMessage", commonFunction.getMultilingualMessage("IDS_SERVERINFOREQD", userInfo.getSlanguagefilename()));
			return new ResponseEntity<>(outputMap,	HttpStatus.OK);
		}
		else {

			final int nsiteCode = site.getNsitecode();
			final int nsyncServer = site.getNissyncserver();
			final int nstandAloneServer = site.getNisstandaloneserver();
			final String ssiteCode = site.getSsitecode();

			if(nsyncServer == Enumeration.TransactionStatus.NO.gettransactionstatus()
					&& nstandAloneServer == Enumeration.TransactionStatus.NO.gettransactionstatus())
			{
				//Sync not required for this site
				syncLogging.info("Primary Sync Server or stand alone servers not available");

				outputMap.put("SyncMessage", commonFunction.getMultilingualMessage("IDS_SYNCNOTREQUIRED", userInfo.getSlanguagefilename()));

				syncLogging.info("sync Records_3:"+ outputMap);

				return new ResponseEntity<>(outputMap,	HttpStatus.OK);
			}
			else {
				try {

					syncLogging.info("Primary Sync Server or stand alone servers available");

					String sTableRecords = "";
					String sinsertSync = "";
					final String dbFormat =  "yyyy-MM-dd HH:mm:ss";

					String sQuery = "select s.nsynchronizationcode, s.ndestinationsitecode, s.sdestinationsitecode, "
							+ " s.ssourcesitecode, s.dlastsyncdatetime, s.dsyncdatetime,"
							+ " s.nsynctype,s.nsyncstatus, s.nusercode,s.nuserrole,s.nsitecode,s.nstatus,"
							+ " to_char(s.dlastsyncdatetime :: timestamp without time zone, '" + dbFormat + "')  as slastsyncdatetime,"
							+ " to_char(s.dsyncdatetime :: timestamp without time zone, '" + dbFormat + "')  as ssyncdatetime"
							+ " from synchronization s,  syncbatch sb "
							+ " where sb.nsynchronizationcode=s.nsynchronizationcode "
							+ " and sb.nbatchfinalstatus=" +  Enumeration.TransactionStatus.SENTSUCCESS.gettransactionstatus()
							+ " and s.ndatewisesync="+Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and  s.nstatus=sb.nstatus and sb.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " order by 1 desc";

					syncLogging.info("sync Records_4:"+ sQuery);

					final List<Synchronization> synchronizationList = jdbcTemplate.query(sQuery, new Synchronization());
					syncLogging.info("sync Records_5:"+ synchronizationList);


					int ntransferType = -1;
					String concatString = "";

					if(nsyncServer == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						ntransferType = Enumeration.TransactionStatus.CENTRALTOLOCAL.gettransactionstatus();
						concatString = " and nisstandaloneserver=" + Enumeration.TransactionStatus.YES.gettransactionstatus() ;
					}
					else if(nstandAloneServer == Enumeration.TransactionStatus.YES.gettransactionstatus()){
						ntransferType = Enumeration.TransactionStatus.LOCALTOCENTRAL.gettransactionstatus();
						concatString = " and nissyncserver=" + Enumeration.TransactionStatus.YES.gettransactionstatus() ;
					}

					sQuery = "select sc.*, s.ssitecode from siteconfig sc, site s where "
							+ "sc.nsitecode=s.nsitecode and sc.nstatus=s.nstatus and"
							+ " s.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ concatString + " and s.nsitecode != -1 order by s.nsitecode";

					final List<SiteConfig> syncSiteList = jdbcTemplate.query(sQuery, new SiteConfig());
					syncLogging.info("sync Records_6:"+ syncSiteList.size());

					if(syncSiteList.isEmpty())
					{
						//Destination/Sync sites not available
						outputMap.put("SyncMessage", commonFunction.getMultilingualMessage("IDS_SYNCSITENOTAVAILABLE", userInfo.getSlanguagefilename()));

						syncLogging.info("sync Records_7:"+ outputMap);

					}
					else {
						syncLogging.info("Non empty syncSiteList :");

						sQuery = "select * from synctablelist "
								+ " where nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ntransfertype in ( " + ntransferType + ","
								+ Enumeration.TransactionStatus.BIDIRECTIONAL.gettransactionstatus() + ") "
								+ " and sreleaseqry is null "
								+ " order by nsorter";

						final List<SyncTableList> syncTableList = jdbcTemplate.query(sQuery, new SyncTableList());
						syncLogging.info("sync Records_8:"+ syncTableList.size());

						if (syncTableList.isEmpty()) {
							outputMap.put("SyncMessage", commonFunction.getMultilingualMessage("IDS_EMPTYSYNCTABLELIST", userInfo.getSlanguagefilename()));

						}
						else {

							int i = 0;
							String synRecordQuery = "";
							String batchQuery = "";
							//int sRecordLimit=0;
							//List<String> insertQueryArray = new ArrayList<>();
							//List<String> sendRecordList = new ArrayList<>();

							final Instant instant = dateUtilityFunction.getCurrentDateTime(userInfo);
							//							final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_FORMAT)
							//									.withZone(ZoneId.systemDefault());

							String currentDate = "'"+ instant.toString() + "'";
							//syncLogging.info("currentDate1 -> " + currentDate1 +":"+ currentDate);

							final int transStatusCode = Enumeration.TransactionStatus.RELEASED.gettransactionstatus();

							StringBuilder syncSendRecordSB = new StringBuilder(
									" insert into syncsendrecord(nsyncbatchcode, sbatchtransferid, stablename, nsorter, jsondata, "
											+" stransferid, nfetchstatus, sdestinationsitecode,ssourcesitecode,dtransactiondatetime,nsitecode,nstatus) values ");

							for (int j = 0; j < syncSiteList.size(); j++)
							{

								final int ndestSiteCode = syncSiteList.get(j).getNsitecode();
								final String sdestinationSiteCode = syncSiteList.get(j).getSsitecode();
								final List<Synchronization> destinationSyncList = synchronizationList.stream().filter(item->
								item.getNdestinationsitecode() == ndestSiteCode)
										.collect(Collectors.toList());

								String modifiedDateString = null;
								if(destinationSyncList.isEmpty())
								{
									//modifiedDateString = "'1970-01-01 00:00:00'";
									final String modifiedDateQry="select ssettingvalue from settings where nsettingcode=35 and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
									String modifiedDate=jdbcTemplate.queryForObject(modifiedDateQry, String.class);
									modifiedDateString= "'"+ modifiedDate + "'";
								}
								else
								{

									modifiedDateString =  " (select dlastsyncdatetime from synchronization "
											+ " where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and nsynchronizationcode=" + destinationSyncList.get(0).getNsynchronizationcode() + ")";

								}
								//syncLogging.info("modifiedDateString:"+modifiedDateString);

								if(modifiedDateString != null)
								{
									final String sbatchTransferID = UUID.randomUUID().toString().trim();
									final String sSyncId = UUID.randomUUID().toString().trim();
									boolean bcheckData = false;

									for (i = 0; i < syncTableList.size(); i++)
									{
										int limitValue = syncTableList.get(i).getNrecordcount();
										int offsetStart = 0;

										final String tableName = syncTableList.get(i).getStablename();
										syncLogging.info("Sync TableName -> " + tableName);

										List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

										do
										{

											String conditionalString = "";
											if (limitValue > 0 )
											{
												conditionalString = " LIMIT " + limitValue + " OFFSET " + offsetStart ;
											}

											if(syncTableList.get(i).getSquery() != null)
											{
												//start of Transaction records


												int trfrSite = nsiteCode;
												if(nsyncServer == Enumeration.TransactionStatus.YES.gettransactionstatus())
												{
													trfrSite = ndestSiteCode;
												}

												if(syncTableList.get(i).getNtransfertype() == Enumeration.TransactionStatus.BIDIRECTIONAL.gettransactionstatus()
														&& nsyncServer == Enumeration.TransactionStatus.YES.gettransactionstatus())
												{
													//For Patient History and outsource details to be synced from central to local
													String dateFieldName = "dmodifieddate";

													String siteCodeQry = "";
													if(!tableName.equalsIgnoreCase("patienthistory")) {
														siteCodeQry = " nsitecode=" + trfrSite + " and ";
													}

													String sUpdateFieldQuery = "";
													if(syncTableList.get(i).getJupdatefield()!= null)
													{
														String key = "";
														int value = 0;
														final JSONObject updateFieldObj = new JSONObject (syncTableList.get(i).getJupdatefield());
														final JSONArray keys = updateFieldObj.names ();

														for (int k = 0; k < keys.length (); k++) {
															String addString="";
															if(k<keys.length ()-1)
															{
																addString = ",";
															}
															key = keys.getString (k);
															value = updateFieldObj.getInt(key);
															sUpdateFieldQuery=sUpdateFieldQuery +"'"+key+"',"+value+" "+addString+ "";
														}
														sUpdateFieldQuery = " || jsonb_build_object("+sUpdateFieldQuery+")::jsonb";

													}
													String UpdateFieldQuery = sUpdateFieldQuery.isEmpty() ? " " :sUpdateFieldQuery ;

													sQuery = " select  *  from " + tableName + " where " + siteCodeQry +  dateFieldName + " between "
															+ modifiedDateString + " and " + currentDate
															+ " order by 1 asc "
															+ conditionalString ;
													//syncLogging.info("sQuery:"+sQuery);

													sTableRecords = "select  jsonb_agg(jsonobj):: json  from ("
															+ " select to_jsonb(" + tableName + ".*) "+UpdateFieldQuery+" jsonobj from " + tableName
															+ "  where  " + siteCodeQry + dateFieldName
															+ "  between " + modifiedDateString +" and " + currentDate
															+ " order by 1 asc "
															+ conditionalString
															+ ") a";

													//syncLogging.info("sync Records_10:"+ sTableRecords);

												}
												else
												{
													String dateFieldName = "dtransactiondate";
													if(syncTableList.get(i).getSdatefieldname() != null)
													{
														dateFieldName = syncTableList.get(i).getSdatefieldname();
													}
													if(syncTableList.get(i).getSquery().contains("P$sitecode$P"))
													{

														sQuery = syncTableList.get(i).getSquery()
																//.replace("P$nsitecode$P", Integer.toString(destSiteCode))
																.replace("P$sitecode$P", " rh.nsitecode= " + trfrSite + " ")
																.replace("P$dmodifieddate$P", modifiedDateString)
																.replace("P$datefield$P", dateFieldName)
																.replace("P$dcurrentdate$P", currentDate)
																.replace("P$ntransactionstatus$P", Integer.toString(transStatusCode))
																.replace("P$conditionstring$P", conditionalString);

														sTableRecords = syncTableList.get(i).getSrecordquery()
																//.replace("P$nsitecode$P", Integer.toString(destSiteCode))
																.replace("P$sitecode$P", " rh.nsitecode= " + trfrSite + " ")
																.replace("P$dmodifieddate$P", modifiedDateString)
																.replace("P$datefield$P", dateFieldName)
																.replace("P$dcurrentdate$P", currentDate)
																.replace("P$ntransactionstatus$P", Integer.toString(transStatusCode))
																.replace("P$conditionstring$P", conditionalString);
													}
													else
													{
														sQuery = syncTableList.get(i).getSquery()
																//.replace("P$nsitecode$P", Integer.toString(nsiteCode))
																.replace("P$dmodifieddate$P", modifiedDateString)
																.replace("P$datefield$P", dateFieldName)
																.replace("P$dcurrentdate$P", currentDate)
																.replace("P$ntransactionstatus$P", Integer.toString(transStatusCode))
																.replace("P$conditionstring$P", conditionalString);

														sTableRecords = syncTableList.get(i).getSrecordquery()
																//.replace("P$nsitecode$P", Integer.toString(nsiteCode))
																.replace("P$dmodifieddate$P", modifiedDateString)
																.replace("P$datefield$P", dateFieldName)
																.replace("P$dcurrentdate$P", currentDate)
																.replace("P$ntransactionstatus$P", Integer.toString(transStatusCode))
																.replace("P$conditionstring$P", conditionalString);
													}
												}

											}

											else
											{	//master screen records

												String dateFieldName = "dmodifieddate";
												if(syncTableList.get(i).getSdatefieldname() != null)
												{
													dateFieldName = syncTableList.get(i).getSdatefieldname();
												}

												sQuery = " select  *  from " + tableName + " where "
														+ dateFieldName + " between " + modifiedDateString + " and " + currentDate
														+ " order by 1 asc "
														+ conditionalString ;
												//syncLogging.info("sQuery:"+sQuery);

												sTableRecords = "select  jsonb_agg(jsonobj):: json  from ("
														+ " select to_jsonb(" + tableName + ".*) jsonobj from " + tableName
														+ "  where  " + dateFieldName + "  between "
														+ modifiedDateString +" and " + currentDate
														+ " order by 1 asc "
														+ conditionalString
														+ ") a";

											}

											syncLogging.info("sync Records_14:");

											list = jdbcTemplate.queryForList(sQuery);
											syncLogging.info("sync Records_15 listsize:"+list.size());


											if (list.size() > 0) {

												offsetStart = offsetStart+ limitValue;

												syncLogging.info(" data exists to sync query:"+offsetStart+","+tableName);

												String sendRecordQry = "("
														// + nsyncSendRecordCode
														//	+ ","
														+ " (select nsyncbatchcode from syncbatch where sbatchtransferid='"+sbatchTransferID+"'),'"+ sbatchTransferID + "',"
														+ "'" + tableName + "',"
														+ syncTableList.get(i).getNsorter()
														+ ", (" + sTableRecords + "),'"
														+ UUID.randomUUID().toString().trim() + "', "
														+ Enumeration.TransactionStatus.NO.gettransactionstatus()+ ","
														+ "'"+ sdestinationSiteCode + "',"
														+ "'"+ ssiteCode + "',"
														+  "'" + dateUtilityFunction.getCurrentDateTime(userInfo)
														+ "', "+ nsiteCode + ","
														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

												synRecordQuery =  synRecordQuery + sendRecordQry;

												//sendRecordList.add(sendRecordQry);
												bcheckData = true;
												syncLogging.info("sync Records_16:"+ tableName);

											}

										}while(list.size()> 0 && offsetStart > 0);

										if (i == syncTableList.size() - 1 && bcheckData == true) {

											batchQuery =  batchQuery + "( "
													//+ ""+ nsyncBatchCode +","
													+ " (select nsynchronizationcode from synchronization where ssyncid='"+sSyncId+"')"
													+ " , '"+ sbatchTransferID + "', "
													+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
													+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + ntransferType
													+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
													+ syncSiteList.get(j).getNsitecode() + ","
													+ "'"+ sdestinationSiteCode + "',"
													+ "'"+ ssiteCode + "',"
													+ nsiteCode + ","
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													+ "),";

											syncLogging.info("sync Records_17:");

											syncLogging.info("insert into synchistory sbatchtransferid ->  " + sbatchTransferID +":"+batchQuery);
										}

									}

									sinsertSync = sinsertSync + " ( "
											+ modifiedDateString
											+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
											+ syncSiteList.get(j).getNsitecode() + ","
											+ "'"+ sdestinationSiteCode + "',"
											+ "'"+ ssiteCode + "',"
											+ nsyncType + ", "
											+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
											+ ", " + userInfo.getNusercode()
											+ "," + userInfo.getNuserrole() + ", " + nsiteCode + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ ",'"+sSyncId+"'),";

									syncLogging.info("sync Records_18:");


								}

								// End of Site Loop

							}
							syncLogging.info("sinsertSync:"+sinsertSync.trim().length() + ":"+batchQuery.length() +":"+syncSendRecordSB.length() );

							if(sinsertSync.trim().length() > 0 && batchQuery.length() > 0 && synRecordQuery.length() > 0
									//sendRecordList.size() > 0
									)
							{

								final String insertSyncString = "insert into synchronization ("
										+ " dlastsyncdatetime, dsyncdatetime,ndestinationsitecode, "
										+ " sdestinationsitecode,ssourcesitecode, "
										+ "nsynctype,nsyncstatus,nusercode,nuserrole,nsitecode,nstatus,ssyncid) "
										+ " values " + sinsertSync.substring(0, sinsertSync.length() - 1);

								syncLogging.info("sync Records_19:"+insertSyncString);

								jdbcTemplate.execute(insertSyncString);

								if(batchQuery.length() >0)
								{
									final String query2 =  "insert into syncbatch("
											+ "nsynchronizationcode, sbatchtransferid,nbatchtransferstatus,"
											+ " nbatchfinalstatus, ntransfertype, dbatchstartdatetime, "//dbatchenddatetime,"
											+ " ndestinationsitecode, sdestinationsitecode,ssourcesitecode,nsitecode,nstatus) "
											+ " values" + batchQuery.substring(0, batchQuery.length() - 1);

									syncLogging.info("sync Records_20:"+query2);

									jdbcTemplate.execute(query2);
								}

								if(synRecordQuery.length() > 0)
								{

									//									if (sRecordLimit > 0 && sRecordLimit < 899) {
									//										insertQueryArray.add(syncSendRecordSB.toString().substring(0, syncSendRecordSB.length() - 1) + ";");
									//									}
									//									executeBulkDatainSingleInsert(insertQueryArray.toArray(new String[0]));

									final String query1 = "insert into syncsendrecord("
											+ "nsyncbatchcode, sbatchtransferid, "
											+ " stablename, nsorter, jsondata, "
											+ " stransferid, nfetchstatus, sdestinationsitecode,ssourcesitecode,"
											+ " dtransactiondatetime,nsitecode,nstatus) "
											+ " values " + synRecordQuery.substring(0, synRecordQuery.length() - 1);
									syncLogging.info("sync Records_21_syn:"+ query1);

									syncLogging.info("sync Records_21:");

									jdbcTemplate.execute(query1);
								}

								//								if(sendRecordList.size() > 0)
								//								{
								//									syncLogging.info("insert into syncsendrecord_22:");
								//
								//									String insertQry = "insert into syncsendrecord("
								//														+ "nsyncbatchcode, sbatchtransferid, "
								//														+ " stablename, nsorter, jsondata, "
								//														+ " stransferid, nfetchstatus, sdestinationsitecode,ssourcesitecode,"
								//														+ " dtransactiondatetime,nsitecode,nstatus) "
								//														+ " values ";
								//									StringBuilder auditActionSB = new StringBuilder(insertQry);
								//
								//									List<String> insertQueryList = new ArrayList<>();
								//
								//									int j = 0;
								//									for (int i1 = 0; i1 < sendRecordList.size(); i1++)
								//									{
								//										auditActionSB.append(sendRecordList.get(i1));
								//
								//										//insertQueryList.add(sendRecordList.get(i1));
								//
								//										if (j == 200) {
								//
								//											//insertQueryList.add(auditActionSB.toString().substring(0, auditActionSB.length() - 1) + ";");
								//
								//											String qry1 = auditActionSB.toString().substring(0, auditActionSB.length() - 1) + ";";
								//
								//											syncLogging.info("insert into syncsendrecord_23:");
								//
								//											jdbcTemplate.execute(qry1);
								//
								//											auditActionSB.delete(0, auditActionSB.length());
								//
								//											//syncLogging.info("insert into syncsendrecord_23:"+insertQry);
								//
								//											//insertQueryList.addFirst(insertQry);
								//											//final String qry1 =insertQueryList.toString();
								//
								//											//syncLogging.info("insert into syncsendrecord_24:"+qry1);
								//
								//											//jdbcTemplate.execute(qry1.substring(0, qry1.length() - 1) + ";");
								//
								//											//insertQueryList.clear();
								//
								//											auditActionSB = new StringBuilder(
								//													 "insert into syncsendrecord("
								//																+ "nsyncbatchcode, sbatchtransferid, "
								//																+ " stablename, nsorter, jsondata, "
								//																+ " stransferid, nfetchstatus, sdestinationsitecode,ssourcesitecode,"
								//																+ " dtransactiondatetime,nsitecode,nstatus) "
								//																+ " values ");
								//
								//											j = 0;
								//										} else {
								//											j++;
								//										}
								//									}
								//									syncLogging.info("insert into syncsendrecord_25 iteration ends:");
								//									if (j > 0 && j < 200) {
								//										//nsertQueryList.add(auditActionSB.toString().substring(0, auditActionSB.length() - 1) + ";");
								//
								////										insertQueryList.addFirst(insertQry);
								////										final String qry1 =insertQueryList.toString();
								////
								////										jdbcTemplate.execute(qry1.substring(0, qry1.length() - 1) + ";");
								////
								////										insertQueryList.clear();
								//
								//										String qry1 = auditActionSB.toString().substring(0, auditActionSB.length() - 1) + ";";
								//
								//										syncLogging.info("insert into syncsendrecord_26:");
								//										jdbcTemplate.execute(qry1);
								//
								//										auditActionSB.delete(0, auditActionSB.length());
								//
								//									}
								//
								//									//executeBulkDatainSingleInsert(nsertQueryList.toArray(new String[0]));
								//									syncLogging.info("insert into syncsendrecord_27 completed");
								//								}


							}
							else {
								outputMap.put("SyncMessage", commonFunction.getMultilingualMessage("IDS_NODATATOSYNC", userInfo.getSlanguagefilename()));

							}
							syncLogging.info("Synchronization data preparation completed at :");


							//							final String updateSqQuery = "update seqnosyncmanagement set nsequenceno =" + nsyncCode + " where stablename='synchronization';"
							//									+ " update seqnosyncmanagement set nsequenceno =" + nsyncSendRecordCode + " where stablename='syncsendrecord';"
							//									+ " update seqnosyncmanagement set nsequenceno =" + nsyncBatchCode + " where stablename='syncbatch';";
							//
							//							jdbcTemplate.execute(updateSqQuery);
						}
					}

				}
				catch(Exception e)
				{
					syncLogging.info(e.getMessage());

				}

				if(nsyncType != Enumeration.TransactionStatus.AUTO.gettransactionstatus())
				{
					outputMap.putAll((Map<String, Object>)syncBatchHistoryDAO.getSyncHistory(null, userInfo).getBody());
				}
				return new ResponseEntity<>(outputMap,	HttpStatus.OK);
			}
		}
	}


	// Three months once delete sync history related tables call scheduler daily  --ALPD-4159 ,work done by Dhanushya R I
	@Override
	public void deleteSync() throws Exception {

		String updateExcepQuery="";
		final String sSettings="select ssettingvalue from settings where nsettingcode=57 and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(sSettings, Settings.class, jdbcTemplate);

		final String sQuery="select nsyncbatchcode from synchistory where dtransactiondatetime <= (NOW() - INTERVAL '"+objSettings.getSsettingvalue()+" days') group by nsyncbatchcode ";
		final List<String> lstBatchCode=jdbcTemplate.queryForList(sQuery, String.class);

		if(!lstBatchCode.isEmpty()) {
			final String batchcode=lstBatchCode.stream().collect(Collectors.joining(","));

			final String str="select sbatchtransferid,nsynchronizationcode from syncbatch where nsyncbatchcode in ("+batchcode+" ) ";
			final List<SyncBatch> lstBatch = jdbcTemplate.query(str, new SyncBatch());

			final String bactchid=lstBatch.stream().map(x -> "'" +String.valueOf(x.getSbatchtransferid())+"'" )
					.collect(Collectors.joining(","));
			final String nsynchronizationcode=lstBatch.stream().map(x -> String.valueOf(x.getNsynchronizationcode()))
					.collect(Collectors.joining(","));

			updateExcepQuery=updateExcepQuery+" delete from synchistory where nsyncbatchcode in ("+batchcode+" );";

			updateExcepQuery=updateExcepQuery+" delete from syncsendrecord where nsyncbatchcode in ("+batchcode+" );";

			updateExcepQuery=updateExcepQuery+" delete from syncreceivedrecord where sbatchtransferid in ("+bactchid+" );";

			updateExcepQuery=updateExcepQuery+" delete from syncbatch where nsyncbatchcode in ("+batchcode+" );";

			updateExcepQuery=updateExcepQuery+" delete from synchronization where nsynchronizationcode in ("+nsynchronizationcode+" );";

			updateExcepQuery=updateExcepQuery+" delete from syncexecutionstatus where sbatchtransferid in ("+bactchid+" );";

			jdbcTemplate.execute(updateExcepQuery);
		}

	}

}
