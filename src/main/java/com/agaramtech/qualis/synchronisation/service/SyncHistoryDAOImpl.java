package com.agaramtech.qualis.synchronisation.service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.synchronisation.model.SyncBatch;
import com.agaramtech.qualis.synchronisation.model.SyncHistory;
import com.agaramtech.qualis.synchronisation.model.Synchronization;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Repository
public class SyncHistoryDAOImpl implements SyncHistoryDAO {

	private static final Log LOGGER = LogFactory.getLog(SyncHistoryDAOImpl.class);

	private final CommonFunction commonFunction;

	private final DateTimeUtilityFunction dateUtilityFunction;

	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;

	private final JdbcTemplate jdbcTemplate;

	public SyncHistoryDAOImpl(CommonFunction commonFunction, DateTimeUtilityFunction dateUtilityFunction,
			JdbcTemplateUtilityFunction jdbcUtilityFunction, JdbcTemplate jdbcTemplate) {
		super();
		this.commonFunction = commonFunction;
		this.dateUtilityFunction = dateUtilityFunction;
		this.jdbcUtilityFunction = jdbcUtilityFunction;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public ResponseEntity<Object> getSyncHistory(Integer nsyncBatchCode, final UserInfo userInfo) throws Exception {

		// LOGGER.info("getSyncHistory");
		final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		SyncBatch selectedSyncBatchHistory = null;
		String batchTransferId = "";
		LOGGER.info("getSyncHistory:" + nsyncBatchCode);
		if (nsyncBatchCode == null) {
			final String strQuery = "select sbh.*, s.ssitename sdestinationsitename,"
					+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US') as sbatchtransferstatus,"
					+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts2.jsondata->'stransdisplaystatus'->>'en-US') as sbatchfinalstatus,"
					+ " coalesce(ts3.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts3.jsondata->'stransdisplaystatus'->>'en-US') as stransertype,"
					+ " coalesce(ts4.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts4.jsondata->'stransdisplaystatus'->>'en-US') as ssynctype,"
					+ " COALESCE(TO_CHAR(sbh.dbatchstartdatetime,'" + userInfo.getSpgsitedatetime()
					+ "'),'') as sbatchstartdatetime," + " COALESCE(TO_CHAR(sbh.dbatchenddatetime,'"
					+ userInfo.getSpgsitedatetime() + "'),'') as sbatchenddatetime,"
					//
					+ " coalesce(sbh.jerrormsg->>'LIMS_Comment', '') as serrormsg"
					+ " from syncbatch sbh, synchronization sc," + " transactionstatus ts1,"
					+ " transactionstatus ts2, transactionstatus ts3,transactionstatus ts4, site s"
					+ " where sbh.ndestinationsitecode=s.nsitecode"
					+ " and sbh.nsynchronizationcode = sc.nsynchronizationcode"
					+ " and sbh.nbatchtransferstatus = ts1.ntranscode" + " and sbh.nbatchfinalstatus=ts2.ntranscode"
					+ " and sbh.ntransfertype=ts3.ntranscode" + " and sc.nsynctype=ts4.ntranscode"
					+ " and sbh.nstatus=" + +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sc.nstatus=" + +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts1.nstatus=ts2.nstatus and ts2.nstatus=ts3.nstatus and ts3.nstatus=ts4.nstatus"
					+ " and ts4.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sbh.nsitecode=" + userInfo.getNsitecode() + " order by sbh.nsyncbatchcode desc";
			LOGGER.info("sync histo get:" + strQuery);

			List<SyncBatch> syncBatchHistoryList = jdbcTemplate.query(strQuery, new SyncBatch());

			if (syncBatchHistoryList.isEmpty()) {
				outputMap.put("SyncBatchHistory", syncBatchHistoryList);
				outputMap.put("SelectedSyncBatchHistory", null);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {
				objMapper.registerModule(new JavaTimeModule());
				syncBatchHistoryList = objMapper.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new SyncBatch()),
								Arrays.asList("sbatchstartdatetime", "sbatchenddatetime"),
								Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
						new TypeReference<List<SyncBatch>>() {
						});

				outputMap.put("SyncBatchHistory", syncBatchHistoryList);
				selectedSyncBatchHistory = syncBatchHistoryList.get(0);
				nsyncBatchCode = selectedSyncBatchHistory.getNsyncbatchcode();

			}
		} else {
			selectedSyncBatchHistory = getSyncBatchById(nsyncBatchCode, userInfo);

		}
		if (selectedSyncBatchHistory == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedSyncBatchHistory", selectedSyncBatchHistory);

			batchTransferId = selectedSyncBatchHistory.getSbatchtransferid();

			final int nsyncCode = selectedSyncBatchHistory.getNsynchronizationcode();

			String tableName = "syncsendrecord";

			if (selectedSyncBatchHistory.getNtransfertype() == Enumeration.TransactionStatus.CENTRALTOLOCAL
					.gettransactionstatus()) {
				if (userInfo.getNissyncserver() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					tableName = "syncsendrecord";
				} else if (userInfo.getNisstandaloneserver() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {
					tableName = "syncreceivedrecord";
				}
			} else if (selectedSyncBatchHistory.getNtransfertype() == Enumeration.TransactionStatus.LOCALTOCENTRAL
					.gettransactionstatus()) {
				if (userInfo.getNissyncserver() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					tableName = "syncreceivedrecord";
				} else if (userInfo.getNisstandaloneserver() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {
					tableName = "syncsendrecord";
				}
			}

			if (tableName == "syncsendrecord") {
				final String syncGetQuery = "select s.*," + " COALESCE(TO_CHAR(s.dlastsyncdatetime,'"
						+ userInfo.getSpgsitedatetime() + "'),'') as slastsyncdatetime,"
						+ " COALESCE(TO_CHAR(s.dsyncdatetime,'" + userInfo.getSpgsitedatetime()
						+ "'),'') as ssyncdatetime" + " from synchronization s" + " where s.nsynchronizationcode = "
						+ nsyncCode + " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				LOGGER.info("syncGetQuery:" + syncGetQuery);

				objMapper.registerModule(new JavaTimeModule());
				final List<Synchronization> syncList = objMapper.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(
								jdbcTemplate.query(syncGetQuery, new Synchronization()),
								Arrays.asList("slastsyncdatetime", "ssyncdatetime"),
								Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
						new TypeReference<List<Synchronization>>() {
						});

				outputMap.put("Synchronization", syncList.get(0));
			} else {
				outputMap.put("Synchronization", null);
			}

			final String strQuery = "select sh.nsynchistorycode,sh.sbatchtransferid,sh.stransferid,"
					+ " tb.stablename," + " coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransferstatus,"
					+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US') as stransfertype,"
					+ " sh.dtransactiondatetime," + " COALESCE(TO_CHAR(sh.dtransactiondatetime,'"
					+ userInfo.getSpgsitedatetime() + "'),'') as stransactiondatetime," + " sh.nsitecode,sh.nstatus,"
					+ " coalesce(sh.jerrormsg->>'LIMS_Comment', '') as serrormsg" + " from synchistory sh," + tableName
					+ " tb," + " transactionstatus ts,transactionstatus ts1"
					+ " where sh.ntransferstatus = ts.ntranscode" + " and  sh.ntransfertype = ts1.ntranscode"
					+ " and sh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sh.stransferid = tb.stransferid" + " and sh.sbatchtransferid = tb.sbatchtransferid"
					+ " and sh.sbatchtransferid='" + batchTransferId + "'";

			// List<SyncHistory> syncHistoryList = (List<SyncHistory>)
			// jdbcTemplate.query(strQuery, new SyncHistory());
			LOGGER.info("sync histo get1:" + strQuery);

			List<SyncHistory> syncHistoryList = objMapper.convertValue(
					dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new SyncHistory()),
							Arrays.asList("stransactiondatetime"), Arrays.asList(userInfo.getStimezoneid()), userInfo,
							false, null, false),
					new TypeReference<List<SyncHistory>>() {
					});

			outputMap.put("SyncHistory", syncHistoryList);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	public SyncBatch getSyncBatchById(final int nsyncBatchCode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select sbh.*, s.ssitename sdestinationsitename,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US') as sbatchtransferstatus,"
				+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts2.jsondata->'stransdisplaystatus'->>'en-US') as sbatchfinalstatus,"
				+ " coalesce(ts3.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts3.jsondata->'stransdisplaystatus'->>'en-US') as stransertype,"
				+ " coalesce(ts4.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts4.jsondata->'stransdisplaystatus'->>'en-US') as ssynctype,"
				+ " COALESCE(TO_CHAR(sbh.dbatchstartdatetime,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sbatchstartdatetime," + " COALESCE(TO_CHAR(sbh.dbatchenddatetime,'"
				+ userInfo.getSpgsitedatetime() + "'),'') as sbatchenddatetime,"
				+ " coalesce(sbh.jerrormsg->>'LIMS_Comment', '') as serrormsg"
				+ " from syncbatch sbh,synchronization sc, transactionstatus ts1,"
				+ " transactionstatus ts2, transactionstatus ts3,transactionstatus ts4, site s"
				+ " where sbh.ndestinationsitecode=s.nsitecode"
				+ " and sc.nsynchronizationcode = sbh.nsynchronizationcode"
				+ " and sbh.nbatchtransferstatus = ts1.ntranscode" + " and sbh.nbatchfinalstatus=ts2.ntranscode"
				+ " and sbh.ntransfertype=ts3.ntranscode" + " and sc.nsynctype=ts4.ntranscode" + " and sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and sbh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts1.nstatus=ts2.nstatus and ts2.nstatus=ts3.nstatus and ts3.nstatus=ts4.nstatus"
				+ " and ts4.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sbh.nsyncbatchcode ="
				+ nsyncBatchCode;
		LOGGER.info("getSyncBatchById:" + strQuery);
		return (SyncBatch) jdbcUtilityFunction.queryForObject(strQuery, SyncBatch.class, jdbcTemplate);
	}

}
