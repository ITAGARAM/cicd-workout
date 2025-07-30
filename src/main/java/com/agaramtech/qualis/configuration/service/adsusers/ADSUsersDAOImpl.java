package com.agaramtech.qualis.configuration.service.adsusers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.configuration.model.ADSSettings;
import com.agaramtech.qualis.configuration.model.ADSUsers;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ADSUsersDAOImpl implements ADSUsersDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ADSUsersDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final CommonFunction commonFunction;
	private final ProjectDAOSupport projectDAOSupport;

	@Override
	public ResponseEntity<Object> getADSUsers(UserInfo userInfo) throws Exception {
		final String strADSUsers = "select ads.* from adsusers ads where " + " ads.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " order by 1 desc";
		return new ResponseEntity<Object>(jdbcTemplate.queryForList(strADSUsers), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> syncADSUsers(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		String str = "";
		ObjectMapper objmapper = new ObjectMapper();
		Map<String, Object> returnString = new HashMap<String, Object>();
		final ADSUsers adsUsers = objmapper.convertValue(inputMap.get("adsusers"), ADSUsers.class);
		final String sloginid = adsUsers.getSuserid();
		final String spassword = adsUsers.getSpassword();

		String strQuery = "select sldaplink,sdomainname from adssettings where nldapstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		try {
			List<ADSSettings> lstTblADSSettings = (List<ADSSettings>) jdbcTemplate.query(strQuery, new ADSSettings());
			if (!lstTblADSSettings.isEmpty()) {
				for (int index = 0; index < lstTblADSSettings.size(); index++) {
					ADSSettings objdefault = lstTblADSSettings.get(index);
					objdefault.setSloginid(sloginid);
					objdefault.setSpassword(spassword);
					returnString = importAdsUsers(objdefault, userInfo);
					if (returnString.containsKey("rtn")
							&& (Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(returnString.get("rtn"))) {
						break;
					}
				}
				if (returnString.containsKey("rtnAlert") && returnString.get("rtnAlert") != null
						&& !(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(returnString.get("rtnAlert"))) {
					str = (String) returnString.get("rtnAlert");
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(str, userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else if (returnString.containsKey("rtnValue") && returnString.get("rtnValue") != null
						&& !returnString.get("rtnValue").equals("")) {
					str = (String) returnString.get("rtnValue");
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(str, userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					return getADSUsers(userInfo);
				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADSSERVERUNAVAILABLE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

			}
		} catch (Exception e) {
			projectDAOSupport.createException(e, userInfo);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ADSNOTSYNC", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public Map<String, Object> importAdsUsers(ADSSettings objAdsConnectConfig, UserInfo userInfo) throws Exception {
		String str = "";
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> adsUserObj = new HashMap<String, Object>();
		List<Map<String, Object>> lstADSUsers = new ArrayList<Map<String, Object>>();
		if (objAdsConnectConfig != null) {
			Hashtable<String,String> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.PROVIDER_URL, objAdsConnectConfig.getSldaplink());
			env.put(Context.SECURITY_PRINCIPAL,
					objAdsConnectConfig.getSloginid() + "@" + objAdsConnectConfig.getSdomainname());
			env.put(Context.SECURITY_CREDENTIALS, objAdsConnectConfig.getSpassword());
			env.put(Context.REFERRAL, "follow");
			try {
				DirContext ctx = new InitialDirContext(env);
				SearchControls searchCtls = new SearchControls();
				searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

				final String sQuery = "select sattributename,slimscolumn from adsattributes where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode() + " order by 1";
				List<Map<String, Object>> sattributeList = jdbcTemplate.queryForList(sQuery);

				Map<String, Object> columnMap = sattributeList.stream().collect(Collectors
						.toMap(item -> String.valueOf(item.get("slimscolumn")), item -> item.get("sattributename")));

				List<String> valuesList = new ArrayList<>(
						columnMap.values().stream().map(Object::toString).collect(Collectors.toList()));
				String resultString = valuesList.stream().map(value -> "(" + value + "=*)")
						.collect(Collectors.joining());
				String[] attrIDs = valuesList.toArray(new String[0]);
				searchCtls.setReturningAttributes(attrIDs);
				NamingEnumeration<SearchResult> answer = ctx.search("", "(&(objectClass=user)" + resultString + ")", searchCtls);
				// NamingEnumeration answer = ctx.search("", "(objectClass=*)", searchCtls);

				while (answer.hasMoreElements()) {
					SearchResult sr = (SearchResult) answer.next();
					Attributes attrs = sr.getAttributes();
					LOGGER.info(attrs.toString());
					Map<String, Object> objAttrs = new HashMap<>();
					if (!attrs.toString().equals("No attributes")) {
						for (Map.Entry<String, Object> entry : columnMap.entrySet()) {
							String key = entry.getKey();
							String valueKey = String.valueOf(entry.getValue());
							String value = attrs.get(valueKey) != null ? attrs.get(valueKey).get().toString() : "";
							objAttrs.put(key, value);
						}
						lstADSUsers.add(objAttrs);
					}
				}

				if (!lstADSUsers.isEmpty()) {
					adsUserObj = createADSUsers(lstADSUsers, objAdsConnectConfig, userInfo);
				} else {
					LOGGER.error("alert throws when user attributes are not available");
					str = "IDS_USERSNOTAVAILABLE";
				}
				LOGGER.info("connected");
				ctx.close();
				returnMap.put("rtnAlert", adsUserObj.get("rtn"));
				returnMap.put("rtnValue", str);
				returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				return returnMap;
			} catch (AuthenticationNotSupportedException ex) {
				str = "IDS_AUTHENTICATIONNOTSUPPORTEDBYSERVER";
				LOGGER.error("The authentication is not supported by the server");
			} catch (AuthenticationException ex) {
				String errorcodes[] = { "52e", "525", "530", "531", "532", "533", "701", "773" };
				for (int i = 0; i < errorcodes.length; i++) {
					String strser = errorcodes[i];
					if (ex.getLocalizedMessage().toString().contains(strser)) {
						switch (errorcodes[i]) {
						case "52e":
							str = "IDS_WRONGCREDENTIAL";
							break;
						case "525":
							str = "IDS_USERNOTFOUND";
							break;
						case "530":
							str = "IDS_NOTPERMITTEDCONTACTADMIN";
							break;
						case "531":
							str = "IDS_NOTPERMITTED";
							break;
						case "532":
							str = "IDS_PASSWORDEXPIRED";
							break;
						case "533":
							str = "IDS_ACCOUNTDISABLED";
							break;
						case "701":
							str = "IDS_ACCOUNTEXPIRED";
							break;
						case "773":
							str = "IDS_USERMUSTRESETPASSWORD";
							break;
						default:
							str = "IDS_UNKNOWNERROR";
							break;
						}
						break;
					}
				}
			} catch (NamingException ex) {
				LOGGER.error("Unknown Host...check Server name");
				LOGGER.error(ex.getMessage());
				str = "IDS_UNKNOWNHOSTCHECKSERVERNAME";
			} catch (Exception ex) {
				LOGGER.error("error when trying to create the context");
				LOGGER.error(ex.getMessage());
				str = "IDS_ERRORWHENCREATECONTEXT";
			}
		}
		returnMap.put("rtnValue", str);
		return returnMap;
	}

	private Map<String, Object> createADSUsers(List<Map<String, Object>> lstADSUsers, ADSSettings objAdsConnectConfig,
			UserInfo userInfo) throws Exception {
		String strQuery = "";
		int nsequenceno = 0;
		String dataStatus = "";
		Map<String, Object> outputMap = new HashMap<>();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		final String date = dateUtilityFunction.getCurrentDateTime(userInfo).toString();

		List<Map<String, Object>> filteredList = lstADSUsers.stream().filter(value -> {
			Object necnoValue = value.get("necno");
			LOGGER.info("" + necnoValue);
			if (necnoValue == null || necnoValue.toString().isEmpty() || necnoValue.toString().equals("\"\"")) {
				return false;
			}
			try {
				int intValue = Integer.parseInt(necnoValue.toString().trim());
				LOGGER.info("" + intValue);

				return intValue >= Integer.MIN_VALUE && intValue <= Integer.MAX_VALUE;
			} catch (NumberFormatException e) {
				return false;
			}
		})
				.collect(Collectors.toList());

		String necnoString = filteredList.stream().map(x -> String.valueOf(x.get("necno")))
				.collect(Collectors.joining(","));
		final String sQuery = "select * from adsusers where necno in (" + necnoString + ") " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		List<Map<String, Object>> adsUsersList = jdbcTemplate.queryForList(sQuery);
		List<Map<String, Object>> updateList = filteredList
				.stream().filter(
						a -> adsUsersList.stream()
								.noneMatch(b -> (b.get("sloginid").toString().trim())
										.equalsIgnoreCase(a.get("sloginid").toString().trim())))
				.collect(Collectors.toList());
		List<Map<String, Object>> insertList = filteredList.stream()
				.filter(a -> adsUsersList.stream()
						.noneMatch(b -> String.valueOf(b.get("necno")).equals(String.valueOf(a.get("necno")))))
				.collect(Collectors.toList());
		if (!insertList.isEmpty()) {
			final String sequencequery = "select nsequenceno from seqnoconfigurationmaster where stablename ='adsusers'"
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			
			for (int i = 0; i < insertList.size(); i++) {
				nsequenceno++;
				String sloginid = insertList.get(i).get("sloginid").toString();
				String sloginidstring = sloginid.length() >= 20 ? sloginid.substring(0, 20) : sloginid;
				strQuery = strQuery + " (" + nsequenceno + ",'" + sloginidstring + "'," + insertList.get(i).get("necno")
						+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " "
						+ userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
			}
			strQuery = "insert into adsusers(nadsusercode,sloginid,necno,dmodifieddate,nsitecode,nstatus)values"
					+ strQuery.substring(0, strQuery.length() - 1) + ";";

			strQuery = strQuery + "update Seqnoconfigurationmaster set nsequenceno=" + nsequenceno
					+ " where stablename='adsusers'";
			jdbcTemplate.execute(strQuery);
			returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		}
		if (!updateList.isEmpty()) {

			strQuery = IntStream.range(0, updateList.size()).mapToObj(i -> {
				String sloginid = updateList.get(i).get("sloginid").toString();
				String sloginidstring = sloginid.length() >= 20 ? sloginid.substring(0, 20) : sloginid;
				return "update adsusers set sloginid=N'" + sloginidstring + "' ,dmodifieddate='" + date + "' where"
						+ "  necno=" + updateList.get(i).get("necno") + ";";
			}).collect(Collectors.joining(";"));
			jdbcTemplate.execute(strQuery);
		}

		if (insertList.size() == 0 && updateList.size() == 0) {
			returnMap.put("rtn", "IDS_NODATATOSYNC");
		}

		if (insertList.size() == 0 && updateList.size() == 0) {
			dataStatus = "IDS_NODATASYNC";
		} else {
			dataStatus = "IDS_SYNCMODIFIED";
		}

		outputMap.put("stablename", "adsusers");
		outputMap.put("sprimarykeyvalue", -1);
		outputMap.putAll((Map<String, Object>) projectDAOSupport.auditInsert(userInfo, dataStatus));
		auditUtilityFunction.insertAuditAction(userInfo, outputMap.get("sauditaction").toString(),
				outputMap.get("scomments").toString(), outputMap);
		return returnMap;
	}
}
