package com.agaramtech.qualis.integration.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.eln.model.SDMSLabSheetDetails;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class IntegrationDAOImpl implements IntegrationDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationDAOImpl.class);
	
	private final JdbcTemplate jdbcTemplate;
	private final TransactionDAOSupport transactionDAOSupport;

	

	public void createIntegrationRecord(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		transactionDAOSupport.createIntegrationRecord(inputMap,userInfo);

	}

	public void integrationCallService(final Map<String, Object> inputMap, final short interfacetypecode,
			final String preregno, final boolean needSubSample, final UserInfo userInfo) throws Exception {
		transactionDAOSupport.integrationCallService(inputMap,interfacetypecode,preregno,needSubSample,userInfo);

	}

	@Override
	public void interfacerRequestCall(final String url, final List<Map<String, Object>> interfacerMap,final String preregno, final UserInfo userInfo) throws Exception {
		transactionDAOSupport.interfacerRequestCall(url, interfacerMap, preregno, userInfo);
	}

	
	public ResponseEntity<Object> updateInterfacerResult(final Map<String, Object> inputMap) throws Exception {

		Map<String, Object> rtnObj = new HashMap<String, Object>();
		JSONObject jsonObject = new JSONObject(inputMap);

		String key = jsonObject.keys().next();
		// Get the nested JSONObject
		JSONObject nestedObject = jsonObject.getJSONObject(key);

		// Get OrderID
		String orderID = nestedObject.optString("OrderID", "OrderID not found");

		// Get SingleFields
		JSONArray singleFieldsArray = nestedObject.optJSONObject("ParsedData").optJSONArray("SingleFields");

		final String integrationQuery = "select tp.sparametersynonym,tgt.nsorter,tgp.nsorter,sm.ntestrepeatcount,sm.ntransactionsamplecode,sm.ntransactiontestcode,"
									 + "sd.ntransactionresultcode,sm.ntestcode,sm.npreregno,sm.suuid "
									 + "from sdmslabsheetmaster sm,sdmslabsheetdetails sd,testgrouptestparameter tgp, testgrouptest tgt,testparameter tp "
									 + "wheresd.suuid = sm.suuid "
									 + "and sd.ntransactiontestcode = sm.ntransactiontestcode "
									 + "and sd.ntestgrouptestparametercode = tgp.ntestgrouptestparametercode "
									 + "and tgp.ntestparametercode = tp.ntestparametercode "
									 + "and tgp.ntestgrouptestcode = tgt.ntestgrouptestcode "
									 + "and sm.ntestgrouptestcode = tgt.ntestgrouptestcode "
									 + "and tgt.ntestcode = tp.ntestcode "
									 + "and sd.suuid = '" + orderID + "' " 
									 + "and sd.sresult is null and coalesce(sd.sresult, '') = '' "
									 + "and sm.nstatus = sd.nstatus "
									 + "and sd.nstatus = tgp.nstatus and tgp.nstatus = tgt.nstatus "
									 + "and sd.nstatus = tp.nstatus "
									 + "and tgp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									 + "and sd.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									 + "order by tgt.nsorter,tgp.nsorter,sm.ntestrepeatcount,sm.ntransactiontestcode;";

		List<SDMSLabSheetDetails> lstLimsparam = jdbcTemplate.query(integrationQuery, new SDMSLabSheetDetails());
		StringBuilder resultQuery = new StringBuilder();
		Set<Integer> testCheck = new HashSet<>();
		StringJoiner joinTest = new StringJoiner(",");
		final int arrayLength = singleFieldsArray.length();
		final int minLength = Math.min(lstLimsparam.size(), arrayLength);

		try {
			if (!lstLimsparam.isEmpty() && singleFieldsArray != null) {
				for (int i = 0; i < minLength; i++) {
					JSONObject field = singleFieldsArray.getJSONObject(i);
					String sresultValue = field.optString("sFieldValue", "No value").trim();
					resultQuery.append("Update sdmslabsheetdetails set sresult='" + sresultValue
							+ "', sllinterstatus='C' where ntransactionresultcode = "
							+ lstLimsparam.get(i).getNtransactionresultcode() + " and nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");
					if (!testCheck.contains(lstLimsparam.get(i).getNtransactiontestcode())) {
						testCheck.add(lstLimsparam.get(i).getNtransactiontestcode());
						joinTest.add(String.valueOf(lstLimsparam.get(i).getNtransactiontestcode()));
					}
				}

				resultQuery.append("Update sdmslabsheetmaster set sllinterstatus = 'C' where ntransactiontestcode in ("
						+ joinTest.toString() + ") and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");

				jdbcTemplate.execute(resultQuery.toString());
			}
		} catch (Exception e) {
			rtnObj.put("RtnString", e.getMessage());
			LOGGER.info("Catch Message IntegrationDAOImpl-->" + e.getMessage());
			return new ResponseEntity<>(rtnObj, HttpStatus.EXPECTATION_FAILED);
		}

		rtnObj.put("RtnString", "SUCCESS");
		return new ResponseEntity<>(rtnObj, HttpStatus.OK);
	}

}
