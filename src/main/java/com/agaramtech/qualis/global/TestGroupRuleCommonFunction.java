package com.agaramtech.qualis.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class TestGroupRuleCommonFunction {
	
	
	protected final JdbcTemplate jdbcTemplate;

	public Map<String, Object> getTestGroupRulesEngine(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String jsonstr = "";
		List<Map<String, Object>> lstrulesEngine = new ArrayList<>();
		ObjectMapper objmapper = new ObjectMapper();
		
		try {
			
			jsonstr = jdbcTemplate.queryForObject("select json_agg(x.*) from "
					+ "(select  t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "' as stransdisplaystatus,tre.* from testgrouprulesengine tre,transactionstatus t "
					+ " where tre.ntransactionstatus=t.ntranscode " + "  and tre.ntestgrouptestcode= "
					+ inputMap.get("ntestgrouptestcode") + "  and tre.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nruleexecorder asc)x", String.class);

		} catch (Exception e) {
			jsonstr = null;
		}
		if (jsonstr != null) {
			lstrulesEngine = objmapper.readValue(jsonstr.toString(), new TypeReference<List<Map<String, Object>>>() {
			});
		}
		
		outputMap.put("RulesEngine", lstrulesEngine);
		if (lstrulesEngine.size() > 0) {
			outputMap.put("SelectedRulesEngine", lstrulesEngine.get(0));
		} else {
			outputMap.put("SelectedRulesEngine", new HashMap<>());
		}
		
		return outputMap;
	}
	
}
