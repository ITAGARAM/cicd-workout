package com.agaramtech.qualis.synchronisation.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public class SynchronizationServiceImpl implements SynchronizationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationServiceImpl.class);

	private final SynchronizationDAO synchronizationDAO;

	public SynchronizationServiceImpl(SynchronizationDAO synchronizationDAO) {
		super();
		this.synchronizationDAO = synchronizationDAO;
	}

	@Override
	public ResponseEntity<Object> sendRecords() throws Exception {

		return synchronizationDAO.sendRecords();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> receiveRecords(Map<String, Object> inputMap) throws Exception {

		LOGGER.info("Receive Record service1 inputMap:" + inputMap);
		Map<String, Object> outputMap = (Map<String, Object>) synchronizationDAO.receiveRecords(inputMap).getBody();

		// LOGGER.info(" Receive Record service2 outputMap:"+outputMap);
		//
		//
		// //final Map<String, Object> metaData = (Map<String, Object>)
		// inputMap.get("MetaData");
		//
		//
		// Map<String, Object> metaData = null;
		//
		// if (inputMap.containsKey("MetaData"))
		// {
		// metaData = (Map<String, Object>) inputMap.get("MetaData");
		// }
		// else
		// {
		// metaData = (Map<String, Object>) inputMap;
		// }
		//
		// final Map<String, Object> limsMsg = (Map<String, Object>)
		// metaData.get("LIMS_Message");
		// final Map<String, Object> outputMetaData = (Map<String, Object>)
		// outputMap.get("MetaData");
		//
		// if(metaData.get("MessageType").equals("Data"))
		// {
		// LOGGER.info(" Receive Record service4 outputMap data:");
		// if ((int) limsMsg.get("BatchCount") == (int) limsMsg.get("RemainCount"))
		// {
		// LOGGER.info(" Receive Record service5 outputMap equal:");
		// if(outputMetaData.get("Status") != null &&
		// ("Success").equals(outputMetaData.get("Status")))
		// {
		// LOGGER.info(" Receive Record service6 outputMap data success:");
		// outputMap = (Map<String,
		// Object>)synchronizationDAO.executeReceivedRecord(inputMap).getBody();
		// }
		// else {
		// LOGGER.info(" Receive Record service7 outputMap data is not success:");
		//
		// }
		// }
		// else {
		// LOGGER.info(" Receive Record service8 BatchCount and RemainCount is not
		// match:");
		//
		// }
		// }
		LOGGER.info(" Receive Record service6 outputMap9 :" + outputMap.get("MetaData"));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> syncRecords(UserInfo userInfo, int nsyncType) throws Exception {

		return synchronizationDAO.syncRecords(userInfo, nsyncType);
	}

}
