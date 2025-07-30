package com.agaramtech.qualis.configuration.service.apiservice;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;



@AllArgsConstructor
@Repository
public class APIServiceDAOImpl implements APIServiceDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(APIServiceDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;





	@Override
	public ResponseEntity<Object> getAPIService(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> objMap = new HashMap<String, Object>();
		final String setting="select * from settings where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsettingcode="+44;
		List<?> lstSetting = jdbcTemplate.queryForList(setting);
		if(!lstSetting.isEmpty()) {
			final Map<String,Object> set=(Map<String, Object>) lstSetting.get(0);
			final String s=(String) set.get("ssettingvalue");
			int index=s.indexOf(":", s.indexOf(":") + 1);
			final String runningPort=s.substring(index+1,index+5);
			String s1=serverNumber();
			if(runningPort.equals(s1)) {
				final String query = "select *,'"+s+"'||sapiservice sapiservicename from apiservice where napiservicecode>0 " + "and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				objMap.put("IPAddress", serverNumber());
				List<?> lstData = jdbcTemplate.queryForList(query);
				objMap.put("lstApiService", lstData);
				if (!lstData.isEmpty()) {
					Map<String, Object> objData = (Map<String, Object>) lstData.get(0);
					int napiservicecode = (int) objData.get("napiservicecode");
					Map<String, Object> objSelectedApi = new HashMap<String, Object>();
					objSelectedApi.put("label", objData.get("sapiservicename"));
					objSelectedApi.put("value", objData.get("napiservicecode"));
					objSelectedApi.put("item", objData);
					objMap.put("SelectedApi", objSelectedApi);
					Boolean iscustomquery = false;
					String queryForm = "";
					if (napiservicecode == 1 || napiservicecode == 2 || napiservicecode == 24) {
						if (napiservicecode == 1) {
							queryForm = "select q.*, " + " coalesce(jsondata->'sdisplayname'->>'"
									+ userInfo.getSlanguagetypecode() + "',"
									+ " jsondata->'sdisplayname'->>'en-US') as sdisplayname "
									+ " from qualisforms q,designtemplatemapping qc where q.nformcode=qc.nformcode and ntransactionstatus="
									+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and qc.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and q.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nformcode>0";
						} else if (napiservicecode == 2) {
							queryForm = "select q.*, " + " coalesce(jsondata->'sdisplayname'->>'"
									+ userInfo.getSlanguagetypecode() + "',"
									+ " jsondata->'sdisplayname'->>'en-US') as sdisplayname "
									+ " from qualisforms q,designtemplatemapping qc where q.nformcode!=qc.nformcode and ntransactionstatus="
									+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and qc.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and q.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nformcode>0";
						}else if(napiservicecode == 24) {
							queryForm = "select sq.nsqlquerycode, qt.squerytypename, sq.ssqlqueryname, sq.ssqlquery from querytype qt, sqlquery sq where "
									+ "qt.nquerytypecode = sq.nquerytypecode and sq.nsqlquerycode > 0 and sq.nquerytypecode = "+ 7+" and qt.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and sq.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +"";
							iscustomquery = true;
						}
						if(iscustomquery = false) {

							List<?> lstqueryForm = jdbcTemplate.queryForList(queryForm);
							objMap.put("lstqueryForm", lstqueryForm);
							if (!lstqueryForm.isEmpty()) {
								Map<String, Object> objDataForm = (Map<String, Object>) lstqueryForm.get(0);
								Map<String, Object> objSelectedApiForm = new HashMap<String, Object>();
								objSelectedApiForm.put("label", objDataForm.get("sdisplayname"));
								objSelectedApiForm.put("value", objDataForm.get("nformcode"));
								objSelectedApiForm.put("item", objDataForm);
								objMap.put("SelectedForm", objSelectedApiForm);
								final String querybilderColumns = "select a.stablename as stable,c.value->>'columnname' as scolumn,"
										+ "a.jsondata->'tablename'->>'" + userInfo.getSlanguagetypecode()
										+ "' as stabledisplayname," + " c.value->'displayname'->>'"
										+ userInfo.getSlanguagetypecode() + "' scolumndisplayname,"
										+ " cast( c.value->>'isjsoncolumn' as boolean) isjsoncolumn,"
										+ " c.value->>'parametername' parametername,"
										+ " cast( c.value->>'ismultilingual' as boolean) ismultilingual,"
										+ " c.value->>'jsoncolumnname' jsoncolumnname , c.value->>'columndatatype' columndatatype "
										+ " from querybuildertables a,"
										+ " querybuildertablecolumns b,querytabledetails d,jsonb_array_elements(b.jsqlquerycolumns) c  "
										+ " where  a.nquerybuildertablecode = d.nquerybuildertablecode and d.nformcode ="
										+ objDataForm.get("nformcode")
										+ " and a.nquerybuildertablecode = b.nquerybuildertablecode and  c.value->>'tablename'::text=b.stablename "
										+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " order by a.nquerybuildertablecode";
								List<Map<String, Object>> lstquerybilderColumns = jdbcTemplate
										.queryForList(querybilderColumns);
								objMap.put("lstquerybilderColumns", lstquerybilderColumns);
							} else {
								objMap.put("SelectedForm", null);
								objMap.put("lstquerybilderColumns", lstqueryForm);
							}
						}if(iscustomquery = true) {
							List<?> lstquery = jdbcTemplate.queryForList(queryForm);
							objMap.put("lstSQLQuery", lstquery);
							if (!lstquery.isEmpty()) {
								Map<String, Object> objSQL = (Map<String, Object>) lstquery.get(0);

								Map<String, Object> objSelectedsql = new HashMap<String, Object>();
								objSelectedsql.put("label", objSQL.get("ssqlqueryname"));
								objSelectedsql.put("value", objSQL.get("nsqlquerycode"));
								objSelectedsql.put("item", objSQL);
								objMap.put("SelectedSQLQuery", objSelectedsql);
							}
						}
					}

				} else {
					objMap.put("SelectedApi", null);
					objMap.put("lstqueryForm", lstData);
					objMap.put("lstquerybilderColumns", lstData);
				}
			}else {
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_PORTNUMBERISMISMATCHING", userInfo.getSlanguagefilename()), HttpStatus.OK);
			}
		}
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDynamicMaster(Map<String, Object> inputMap, UserInfo userInfo) throws Exception, Exception {

		//Map<String, Object> objMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		int nformcode = (int) inputMap.get("nformcode");
		String viewName = "select stablename from querybuildertables where nformcode=" + nformcode;
		String tableName = jdbcTemplate.queryForObject(viewName, String.class);
		String condiotionalQuery = (String) inputMap.get("whereCondition");
		condiotionalQuery = condiotionalQuery != null && !condiotionalQuery.trim().isEmpty()
				? " and " + condiotionalQuery
						: "";
		final String masterData = "select * from " + tableName + " where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + condiotionalQuery;
		List<Map<String, Object>> lstMasterRecord = jdbcTemplate.queryForList(masterData);

		final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "' and data_type = 'timestamp without time zone'";
		String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);

		List<String> lstDateField = null;
		if (jsonDateField != null) {
			lstDateField = new ArrayList<String>(Arrays.asList(jsonDateField.split(",")));
			if (lstDateField != null && !lstDateField.isEmpty()) {

				lstMasterRecord = objMapper.convertValue(
						getConverDate(lstMasterRecord, lstDateField),
						new TypeReference<List<Map<String,Object>>>() {
						});
			}
		}

		return new ResponseEntity<Object>(lstMasterRecord, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getQualisFormFields(Map<String, Object> inputMap, UserInfo userInfo) {

		int nformcode = (int) inputMap.get("nformcode");
		final String querybilderColumns = "select a.stablename as stable,c.value->>'columnname' as scolumn,"
				+ "a.jsondata->'tablename'->>'" + userInfo.getSlanguagetypecode() + "' as stabledisplayname,"
				+ " c.value->'displayname'->>'" + userInfo.getSlanguagetypecode() + "' scolumndisplayname,"
				+ " cast( c.value->>'isjsoncolumn' as boolean) isjsoncolumn,"
				+ " c.value->>'parametername' parametername,"
				+ " cast( c.value->>'ismultilingual' as boolean) ismultilingual,"
				+ " c.value->>'jsoncolumnname' jsoncolumnname , c.value->>'columndatatype' columndatatype "
				+ " from querybuildertables a,"
				+ " querybuildertablecolumns b,querytabledetails d,jsonb_array_elements(b.jsqlquerycolumns) c  "
				+ " where  a.nquerybuildertablecode = d.nquerybuildertablecode and d.nformcode =" + nformcode
				+ " and a.nquerybuildertablecode = b.nquerybuildertablecode and  c.value->>'tablename'::text=b.stablename "
				+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by a.nquerybuildertablecode";
		List<Map<String, Object>> lstquerybilderColumns = jdbcTemplate.queryForList(querybilderColumns);
		return new ResponseEntity<Object>(lstquerybilderColumns, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getQualisForms(Map<String, Object> inputMap, UserInfo userInfo) {

		Map<String, Object> objMap = new HashMap<String, Object>();
		int napiservicecode = (int) inputMap.get("napiservicecode");
		String queryForm = "";
		if (napiservicecode ==1 || napiservicecode == 2 ) {
			if (napiservicecode == 1) {
				queryForm = "select q.*, " + " coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + " jsondata->'sdisplayname'->>'en-US') as sdisplayname "
				+ " from qualisforms q,designtemplatemapping qc where q.nformcode=qc.nformcode and ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and qc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nformcode>0";
			} else if (napiservicecode == 2) {
				queryForm = "select q.*, " + " coalesce(q.jsondata->'sdisplayname'->>'"
						+ userInfo.getSlanguagetypecode() + "',"
						+ " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname "
						+ " from qualisforms q,querybuildertables qc where q.nformcode  not in (select nformcode from designtemplatemapping  "
						+ "	where ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and nstatus=1   and nformcode>0) and q.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "and qc.nstatus = " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and q.nformcode>0 and qc.nformcode=q.nformcode and qc.nismastertable="
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() +" Group by q.nformcode";
			}
			List<?> lstqueryForm = jdbcTemplate.queryForList(queryForm);
			objMap.put("lstqueryForm", lstqueryForm);
			if (!lstqueryForm.isEmpty()) {
				Map<String, Object> objDataForm = (Map<String, Object>) lstqueryForm.get(0);
				Map<String, Object> objSelectedApiForm = new HashMap<String, Object>();
				objSelectedApiForm.put("label", objDataForm.get("sdisplayname"));
				objSelectedApiForm.put("value", objDataForm.get("nformcode"));
				objSelectedApiForm.put("item", objDataForm);
				objMap.put("SelectedForm", objSelectedApiForm);
				final String querybilderColumns = "select a.stablename as stable,c.value->>'columnname' as scolumn,"
						+ "a.jsondata->'tablename'->>'" + userInfo.getSlanguagetypecode() + "' as stabledisplayname,"
						+ " c.value->'displayname'->>'" + userInfo.getSlanguagetypecode() + "' scolumndisplayname,"
						+ " cast( c.value->>'isjsoncolumn' as boolean) isjsoncolumn,"
						+ " c.value->>'parametername' parametername,"
						+ " cast( c.value->>'ismultilingual' as boolean) ismultilingual,"
						+ " c.value->>'jsoncolumnname' jsoncolumnname , c.value->>'columndatatype' columndatatype "
						+ " from querybuildertables a,"
						+ " querybuildertablecolumns b,querytabledetails d,jsonb_array_elements(b.jsqlquerycolumns) c  "
						+ " where  a.nquerybuildertablecode = d.nquerybuildertablecode and d.nformcode ="
						+ objDataForm.get("nformcode")
						+ " and a.nquerybuildertablecode = b.nquerybuildertablecode and  c.value->>'tablename'::text=b.stablename "
						+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by a.nquerybuildertablecode";
				List<Map<String, Object>> lstquerybilderColumns = jdbcTemplate.queryForList(querybilderColumns);
				objMap.put("lstquerybilderColumns", lstquerybilderColumns);
			} else {
				objMap.put("SelectedForm", null);
			}
		}
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> getStaticMaster(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> objMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		int nformcode = (int) inputMap.get("nformcode");
		String viewName = "select stablename from querybuildertables where nformcode=" + nformcode;
		String tableName = jdbcTemplate.queryForObject(viewName, String.class);
		String condiotionalQuery = (String) inputMap.get("whereCondition");
		condiotionalQuery = condiotionalQuery != null && !condiotionalQuery.trim().isEmpty()
				? " and " + condiotionalQuery
						: "";
		final String masterData = "select * from " + tableName + " where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + condiotionalQuery;
		List<Map<String, Object>> lstMasterRecord = jdbcTemplate.queryForList(masterData);
		final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "' and data_type = 'timestamp without time zone'";
		String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);

		List<String> lstDateField = null;
		if (jsonDateField != null) {
			lstDateField = new ArrayList<String>(Arrays.asList(jsonDateField.split(",")));
			if (lstDateField != null && !lstDateField.isEmpty()) {

				lstMasterRecord = objMapper.convertValue(
						getConverDate(lstMasterRecord, lstDateField),
						new TypeReference<List<Map<String,Object>>>() {
						});
			}
		}
		return new ResponseEntity<Object>(lstMasterRecord, HttpStatus.OK);
	}


	public List<Map<String,Object>> getConverDate(List<Map<String,Object>> lst,List<String> dateFields) throws Exception {


		List<Map<String, Object>> finalList = new ArrayList<>();
		ObjectMapper Objmapper = new ObjectMapper();
		Objmapper.registerModule(new JavaTimeModule());

		if (dateFields.isEmpty()) {
			return lst;
		}
		if (!dateFields.isEmpty()) {
			//DateTimeFormatter destFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

			for (int i = 0; i < lst.size(); i++) {
				Map<String, Object> data = lst.get(i);
				for (int j = 0; j < dateFields.size(); j++) {
					final String dateFieldName = dateFields.get(j);
					if (data.containsKey(dateFieldName))  {
						if ((Timestamp) data.get(dateFieldName) != null  ) {

							Timestamp date=(Timestamp) data.get(dateFieldName);
							String dateString=sourceFormat.format(date);
							LocalDateTime ldt = sourceFormat.parse(dateString).toInstant()
									.atZone(ZoneId.systemDefault()).toLocalDateTime();
							data.put(dateFieldName,
									DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(ldt)
									);
						}
					} else {
						data.put(dateFieldName, "-");
					}
				}
				finalList.add(i, data);
			}

		} else {
			finalList.addAll(lst);
		}
		return finalList;
	}


	public String serverNumber() throws MalformedObjectNameException, UnknownHostException {

		MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
		Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
				Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
		String port=null;
		if(!objectNames.isEmpty()) {
			port = objectNames.iterator().next().getKeyProperty("port");

		}


		return port;
		// }
	}



	public String currentIp() {
		InetAddress ip;
		String ipName = "";
		try {
			ip = InetAddress.getLocalHost();
			ipName = ip.getHostAddress();
			System.out.println("Current IP address : " + ip.getHostAddress());

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ipName;
	}
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getCustomQuery (Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();
		final List<String> paramList = new ArrayList<String>();
		final Map<String, String> valueMap = new HashMap<>();
		String query = "select sq.nsqlquerycode, qt.squerytypename, sq.ssqlqueryname, sq.ssqlquery from querytype qt, sqlquery sq where "
				+" qt.nquerytypecode = sq.nquerytypecode and sq.nsqlquerycode > 0 and sq.nquerytypecode = "+ 7+" and qt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and sq.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +"";
		List<?> lstquery = jdbcTemplate.queryForList(query);
		objMap.put("lstSQLQuery", lstquery);
		if (!lstquery.isEmpty()) {
			Map<String, Object> objData =  (Map<String, Object>) lstquery.get(0);

			Map<String, Object> objSelectedsql = new HashMap<String, Object>();
			objSelectedsql.put("label", objData.get("ssqlqueryname"));
			objSelectedsql.put("value", objData.get("nsqlquerycode"));
			objSelectedsql.put("item", objData);
			objSelectedsql.put("query", objData.get("ssqlquery"));

			objMap.put("SelectedSQLQuery", objSelectedsql);
			String querys = (String) objSelectedsql.get("query");
			querys.trim();
			valueMap.put(" \"Country\".\"scountryname\" ","India");
			valueMap.put(" \"Country\".\"scountryshortname\" ","Ind");

			//Set<String> QueryParam = new LinkedHashSet();
			String sOldKey = "";
			String sData = "";
			while(querys.contains("<@")|| querys.contains("<#")) {
				int startindex = 0;
				int endindex = 0;
				StringBuilder sbuilder = new StringBuilder();
				sbuilder.append(query);
				if(querys.contains("<@")) {
					startindex = querys.indexOf("<@");
					endindex = querys.indexOf("@>");
				}else if(querys.contains("<#")) {
					startindex = querys.indexOf("<#");
					endindex = querys.indexOf("#>");
				}
				String sParam = querys.substring(startindex +2 ,endindex);
				if(!sParam.equals(sOldKey)) {
					if(valueMap.containsKey(sParam)) {
						sData = "'" +String.valueOf(valueMap.get(sParam)) + "'";
						sbuilder.replace(startindex, endindex+2, sData);
						querys = sbuilder.toString();

					}
					int dot = sParam.indexOf(".");
					String parameters = sParam.substring(dot +1);
					parameters = parameters.replace("\"", "'");
					paramList.add(parameters);
				}
				//				else {
				//					sOldKey = sParam;
				//				}
			}
			querys = querys.toString();


			final String temptable = "select * into tempquery from ("+ querys +") as tempcolumns ";
			jdbcTemplate.execute(temptable);
			//			String strquery = "select * from tempquery ";
			//			List<?> lstData = jdbcTemplate.queryForList(strquery);

			final String qrycolumns = "select column_name as scolumndisplayname, data_type as columndatatype from "
					+"information_schema.columns where table_schema = 'public' AND table_name = 'tempquery';";
			List<Map<String,Object>> lstcolumns = jdbcTemplate.queryForList(qrycolumns);
			objMap.put("Columns", lstcolumns);
			objMap.put("Parameters", paramList);
			final String temptabledrop = "Drop table if exists tempquery;";
			jdbcTemplate.execute(temptabledrop);

		}
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);

	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getCustomQueryName(Map<String, Object> inputMap, UserInfo userInfo) {
		Map<String, Object> objMap = new HashMap<String, Object>();

		int nsqlquerycode = (int) inputMap.get("nsqlquerycode");
		final String query = "select sq.nsqlquerycode, qt.squerytypename, sq.ssqlqueryname, sq.ssqlquery from querytype qt, sqlquery sq where "
				+" qt.nquerytypecode = sq.nquerytypecode and sq.nsqlquerycode ="+nsqlquerycode+" and qt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and sq.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +"";
		List<?> lstquery = jdbcTemplate.queryForList(query);
		objMap.put("lstSQLQuery", lstquery);
		if (!lstquery.isEmpty()) {
			Map<String, Object> objData =  (Map<String, Object>) lstquery.get(0);

			Map<String, Object> objSelectedsql = new HashMap<String, Object>();
			objSelectedsql.put("label", objData.get("ssqlqueryname"));
			objSelectedsql.put("value", objData.get("nsqlquerycode"));
			objSelectedsql.put("item", objData);
			objSelectedsql.put("query", objData.get("ssqlquery"));

			objMap.put("SelectedSQLQuery", objSelectedsql);
			final String querys = (String) objSelectedsql.get("query");
			final String temptable = "select * into tempquery from ("+ querys +") as tempcolumns ";
			jdbcTemplate.execute(temptable);
			//String strquery = "select * from tempquery ";
			//List<?> lstData = jdbcTemplate.queryForList(strquery);
			final String qrycolumns = "select column_name as scolumndisplayname, data_type as columndatatype from "
					+"information_schema.columns where table_schema = 'public' AND table_name = 'tempquery';";
			List<Map<String,Object>> lstcolumns = jdbcTemplate.queryForList(qrycolumns);
			objMap.put("Columns", lstcolumns);
			final String temptabledrop = "Drop table if exists tempquery;";
			jdbcTemplate.execute(temptabledrop);

		}
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getSQLQueryData(Map<String, Object> inputMap,String query, UserInfo userInfo) throws Exception{
		List<Object> lstquery = new ArrayList<Object>();
		query.trim();
		query.toLowerCase();
		String conquery = "";
		try {
			LOGGER.info(query);
			String filter = (String) inputMap.get("whereCondition");

			conquery =  "select * from (" + query + ") as qry where "+ filter +" ";

			List<Map<String, Object>> lsData = jdbcTemplate.queryForList(conquery);
			lsData = jdbcUtilityFunction.replaceMultiLingualByRecord(lsData, userInfo);
			lstquery.add("Success");
			lstquery.add(lsData);
			return new ResponseEntity<>(lstquery, HttpStatus.OK);
		}catch(Exception e) {
			List<Map<String, Object>> lsDat = new ArrayList<>();

			StringBuilder sb = new StringBuilder();
			sb.append("Msg 208, Level 16, State 1, Line 1").append("\n");
			sb.append("Invalid object name" + " " + "'" + conquery + "'");

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Messages", sb.toString());
			lsDat.add(map);

			lstquery.add("Warning");
			lstquery.add(lsDat);
			return new ResponseEntity<>(lstquery, HttpStatus.OK);
		}

	}


}
