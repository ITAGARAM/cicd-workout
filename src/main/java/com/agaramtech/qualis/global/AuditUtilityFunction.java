package com.agaramtech.qualis.global;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.map.EntryAdded;
import org.javers.core.diff.changetype.map.EntryValueChange;
import org.javers.core.diff.changetype.map.MapChange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.audittrail.model.AuditAction;
import com.agaramtech.qualis.audittrail.model.AuditComments;
import com.agaramtech.qualis.audittrail.model.DynamicAuditTable;
import com.agaramtech.qualis.configuration.model.Language;
import com.agaramtech.qualis.configuration.model.GenericLabel;
import com.agaramtech.qualis.configuration.model.Settings;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Repository
public class AuditUtilityFunction {


	private static final Logger LOGGER = LoggerFactory.getLogger(AuditUtilityFunction.class);

	protected final StringUtilityFunction stringUtilityFunction;
	protected final DateTimeUtilityFunction dateTimeUtilityFunction;
	protected final ClassUtilityFunction classUtilityFunction;
	protected final CommonFunction commonFunction;
	protected final JdbcTemplate jdbcTemplate;
	protected ValidatorDel valiDatorDel;
	protected final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;


	public int auditActionValidation(UserInfo userInfo) throws Exception {
		final String query = "select ssettingvalue from settings where nsettingcode = "+Enumeration.Settings.SILIENTAUDITENABLE.getNsettingcode()+""
							+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		int nisauditebale = 0;
	
		final Settings settings = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class, jdbcTemplate);
		nisauditebale = Integer.parseInt(settings.getSsettingvalue());
		
		return nisauditebale;
		
	}


	/**
	 * 
	 * @param lstnewobject
	 * @param nFlag
	 * @param oldobj
	 * @param actionType
	 * @param objUserInfo
	 * @throws Exception
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	public void fnInsertAuditAction(List<?> lstnewobject, int nFlag, List<?> oldobj, List<String> actionType,UserInfo objUserInfo) throws Exception {
		LOGGER.info("fnInsertAuditAction");

		final int nisauditebale =  auditActionValidation(objUserInfo);

		if(nisauditebale == Enumeration.TransactionStatus.NO.gettransactionstatus() || !objUserInfo.getSreason().isEmpty())
		{
			JSONObject objJsonData = new JSONObject();
			List<Map<String, Object>> oldJsonArray = new ArrayList<>();
			List<Map<String, Object>> newJsonArray = new ArrayList<>();
			List<String> lstactionType = new ArrayList<>();
			String sPrimaryKey = "";
			JSONObject actionTypejson = new JSONObject();
			List<String> lstTablename = new ArrayList<String>();
			Map<String, Object> inputmap = new HashMap<String, Object>();
			JSONArray auditcapturefields = new JSONArray();
			JSONArray editmandatoryfields = new JSONArray();
			JSONArray multiLingualArray = new JSONArray();
			JSONObject jsonAuditnew = new JSONObject();
			JSONObject jsonAuditold = new JSONObject();

			final String strLanguage = "select nlanguagecode, slanguagename, sfilename, slanguagetypecode, "
										+ " sreportingtoolfilename, ndefaultstatus, nsitecode, nstatus "
										+ " from language where slanguagetypecode= '"
										+ objUserInfo.getSlanguagetypecode() + "' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final Language objLanguage = (Language) jdbcUtilityFunction.queryForObject(strLanguage, Language.class,
					jdbcTemplate);
			//		ResourceBundle resourcebundle = new PropertyResourceBundle(new InputStreamReader(
			//				getClass().getResourceAsStream("/"+ Enumeration.Path.PROPERTIES_FILE.getPath() + objLanguage.getSfilename() + ".properties"),
			//				StandardCharsets.UTF_8));

			final ResourceBundle resourcebundle = commonFunction.getResourceBundle(objLanguage.getSfilename(), false);

			final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();
			final Map<String, GenericLabel> genericLabelMap = genericLabelList.stream()
					.collect(Collectors.toMap(GenericLabel::getSidsfieldname, genericLabel -> genericLabel));

			List<AuditAction> lstAuditaction = new ArrayList<>();
			List<AuditComments> lstAuditComments = new ArrayList<>();
			String sourceFormat = "yyyy-MM-dd HH:mm:ss";
			final DateTimeFormatter oldPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			final DateTimeFormatter newPattern = DateTimeFormatter.ofPattern(objUserInfo.getSdatetimeformat());
			final DateTimeFormatter destFormat = DateTimeFormatter.ofPattern("HH:00 (h a)");

			for (int p = 0; p < lstnewobject.size(); p++) {
				AuditAction objAuditAction = new AuditAction();
				AuditComments objAuditComments = new AuditComments();
				String sComments1 = "";
				ObjectMapper oMapper = new ObjectMapper();
				oMapper.setSerializationInclusion(Include.NON_NULL);
				oMapper.registerModule(new JavaTimeModule());
				DateFormat dateFormat = new SimpleDateFormat(objUserInfo.getSdatetimeformat(), Locale.getDefault());
				oMapper.setDateFormat(dateFormat);
				Map<String, Object> map = oMapper.convertValue(lstnewobject.get(p), Map.class);

				// New JsonArrayAudit Call start
				List<String> lststablefeildname = new ArrayList<>();
				List<String> lststaticfeildname = new ArrayList<>();
				List<String> lstsprimarytableforeignkey = new ArrayList<>();
				JSONObject fkpair = new JSONObject();
				JSONObject fkcolumnpair = new JSONObject();
				Map<String, Object> newJsonObject = map;
				List<?> lstObject = (List<?>) lstnewobject;
				List<Map<String, Object>> lstData = oMapper.convertValue(lstnewobject,
						new TypeReference<List<Map<String, Object>>>() {
				});

				// New JsonArrayAudit Call end
				List<String> sameKeyNameCheckin1 = new ArrayList<>();
				List<String> sameKeyNameCheckOut1 = new ArrayList<>();

				String strAuditactiontable = " select * from auditrecordtable where nformcode ="
						+ objUserInfo.getNformcode() + " and stablename = N'"
						+ classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()) + "' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter asc";
				List<Map<String, Object>> lst = jdbcTemplate.queryForList(strAuditactiontable);
				String sComments = "";
				String ntransactionno = "";
				StringBuffer sb = new StringBuffer();
				StringBuffer sb1 = new StringBuffer();
				// New JsonArrayAudit Call start
				List<Map<String, Object>> lstTableParam = new ArrayList<Map<String, Object>>();
				List<String> keys = new ArrayList<>();
				List<Map<String, Object>> lst1 = lst.stream().filter((Map<String, Object> x) -> {
					return (int) x.get("nisforeigntable") == 1;
				}).collect(Collectors.toList());
				List<String> lstTablenamenew = new ArrayList<>();
				List<String> lstTablecolumnname = new ArrayList<>();
				List<String> lstrepeatingtable = new ArrayList<>();
				StringBuilder sbuilder = new StringBuilder();
				for (int th = 0; th < lst1.size(); th++) {
					String sforeigntablename = (String) lst1.get(th).get("sforeigntablename");
					String sforeignprimarykey = (String) lst1.get(th).get("sforeignprimarykey");
					String sforeignkeyalias = (String) lst1.get(th).get("sprimarytableforeignkey");
					List<String> primarykey = null;
					String primarykeyvalue = "";
					String query = "";
					boolean check = lstTablenamenew.stream().anyMatch(x -> x.equals(sforeigntablename));
					if (!check) {
						primarykeyvalue = map.get(sforeignkeyalias).toString();
						if ((int) lst1.get(th).get("nqueryneed") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							String squery = (String) lst1.get(th).get("squery");
							StringBuilder sbuilder1 = new StringBuilder();
							if (squery != null) {
								sbuilder1.append(squery);
								while (squery.contains("P$")) {
									int nStart = squery.indexOf("P$");
									int nEnd = squery.indexOf("$P");
									sbuilder1.replace(nStart, nEnd + 2, primarykeyvalue);
									squery = sbuilder1.toString();
								}
								query = squery + ";";
							}
						} else {
							query = "select * from " + sforeigntablename + " where " + sforeignprimarykey + " in ("
									+ primarykeyvalue + ");";
						}
						sbuilder.append(query);
						lstTablenamenew.add(sforeigntablename);
						lstTablecolumnname.add(sforeignkeyalias);
					} else {

						primarykeyvalue = map.get(sforeignkeyalias).toString();

						if ((int) lst1.get(th).get("nqueryneed") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							String squery = (String) lst1.get(th).get("squery");
							StringBuilder sbuilder1 = new StringBuilder();
							if (squery != null) {
								sbuilder1.append(squery);
								while (squery.contains("P$")) {
									int nStart = squery.indexOf("P$");
									int nEnd = squery.indexOf("$P");
									sbuilder1.replace(nStart, nEnd + 2, primarykeyvalue);
									squery = sbuilder1.toString();
								}
								query = squery + ";";
							}
						} else {
							query = "select * from " + sforeigntablename + " where " + sforeignprimarykey + " in ("
									+ primarykeyvalue + ");";
						}
						sbuilder.append(query);
						lstTablenamenew.add(sforeigntablename + sforeignkeyalias);
						lstTablecolumnname.add(sforeignkeyalias);
						lstrepeatingtable.add(sforeignkeyalias);
						// }
					}
				}

				/*
				 * Will be enabled with alternate method final Map<String, Object>
				 * lstForeignColumndata = findByMultiQueryPlainSql(sbuilder.toString(),
				 * lstTablenamenew.toArray());
				 */
				final Map<String, Object> lstForeignColumndata = projectDAOSupport.getMutlipleEntityResultsUsingPlainSql(sbuilder.toString(),
						jdbcTemplate, lstTablenamenew.toArray());

				List<String> checkrepeatingTable = new ArrayList<>();
				for (int n = 0; n < lst.size(); n++) {
					if ((int) lst.get(n).get("nisforeigntable") == 1) {
						String key = lst.get(n).get("sprimarytableforeignkey").toString();
						if (newJsonObject.containsKey(key)) {
							String fkstablename = (String) lst.get(n).get("sforeigntablename");
							if (lstForeignColumndata.containsKey(fkstablename)
									|| lstForeignColumndata.containsKey(fkstablename + key)) {
								String tablename = "";
								if (lstForeignColumndata.containsKey(fkstablename)
										&& !checkrepeatingTable.contains(fkstablename)) {
									tablename = fkstablename;
									checkrepeatingTable.add(tablename);
								} else if (lstForeignColumndata.containsKey(fkstablename + key)
										&& !checkrepeatingTable.contains(fkstablename + key)) {
									tablename = fkstablename + key;
									checkrepeatingTable.add(tablename);
								}
								if (tablename.equals("")) {
									tablename = fkstablename;
								}
								List<Map<String, Object>> foriegnMap = (List<Map<String, Object>>) lstForeignColumndata
										.get(tablename);
								for (int y = 0; y < foriegnMap.size(); y++) {
									if (((Map<String, Object>) foriegnMap.get(y)).containsKey("jsondata")) {
										if (foriegnMap.get(y).get(lst.get(n).get("sforeignprimarykey").toString())
												.toString()
												.equals(newJsonObject
														.get(lst.get(n).get("sprimarytableforeignkey").toString())
														.toString())) {
											JSONObject foriegnjson = new JSONObject(
													((Map<String, Object>) foriegnMap.get(y)).get("jsondata").toString());
											if (foriegnjson.has(lst.get(n).get("stablefieldname").toString())
													&& !foriegnjson.get(lst.get(n).get("stablefieldname").toString())
													.getClass().toString().equals("class java.lang.String")) {
												newJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
														((JSONObject) foriegnjson
																.get(lst.get(n).get("stablefieldname").toString()))
														.get(objUserInfo.getSlanguagetypecode()));
											} else {
												if (((Map<String, Object>) foriegnMap.get(y))
														.containsKey(lst.get(n).get("stablefieldname").toString())) {
													newJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
															((Map<String, Object>) foriegnMap.get(y))
															.get(lst.get(n).get("stablefieldname").toString()));
												} else {
													newJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
															foriegnjson.get(lst.get(n).get("stablefieldname").toString()));
												}

											}
										}
									} else {
										if (foriegnMap.size() != 1) {
											if (foriegnMap.get(y).get(lst.get(n).get("sforeignprimarykey").toString())
													.toString()
													.equals(newJsonObject
															.get(lst.get(n).get("sprimarytableforeignkey").toString())
															.toString())) {
												newJsonObject.put((String) lst.get(n).get("sstaticfieldname"), foriegnMap
														.get(y).get(lst.get(n).get("stablefieldname").toString()));
											}
										} else {
											newJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
													foriegnMap.get(y).get(lst.get(n).get("stablefieldname").toString()));
										}
									}

								}
							}
						}
						lstsprimarytableforeignkey.add((String) lst.get(n).get("sprimarytableforeignkey"));
						fkpair.put((String) lst.get(n).get("sprimarytableforeignkey"),
								(String) lst.get(n).get("sforeigntablename"));
						fkcolumnpair.put((String) lst.get(n).get("sprimarytableforeignkey"),
								(String) lst.get(n).get("stablefieldname"));
						lststablefeildname.add((String) lst.get(n).get("stablefieldname"));
						lststaticfeildname.add((String) lst.get(n).get("sstaticfieldname"));
					} else {
						newJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
								map.get(lst.get(n).get("stablefieldname")));
					}
					if (nFlag == 1) {
						JSONObject objtemp = new JSONObject();
						auditcapturefields.put((String) lst.get(n).get("sstaticfieldname"));
						sameKeyNameCheckOut1.add((String) lst.get(n).get("stablefieldname"));
						objtemp.put((String) lst.get(n).get("sstaticfieldname"), lst.get(n).get("sidsfieldname"));
						if ((int) lst.get(n).get("neditmainfield") == 1) {
							editmandatoryfields.put(lst.get(n).get("sstaticfieldname"));
						}
						multiLingualArray.put(objtemp);

					}
					sPrimaryKey = (String) lst.get(n).get("stableprimarykey");
					// ntransactionno = (int) map.get(sPrimaryKey);
					ntransactionno = map.get(sPrimaryKey.trim()).toString();
					newJsonObject.put("sPrimaryKey", ntransactionno);
				}
				// New Code End
				// New JsonArrayAudit Call end
				// for Insert and update below coding helpful to right a insert/Delete Audit in
				// AuditTrail
				switch (nFlag) {
				case 1:// Add and delete type
					// New JsonArrayAudit Call Start
					inputmap.put(
							"multilingualfields" + classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()),
							multiLingualArray.toString());
					inputmap.put(
							"editmandatoryfields" + classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()),
							editmandatoryfields.toString());
					inputmap.put(
							"auditcapturefields" + classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()),
							auditcapturefields.toString());
					multiLingualArray.clear();
					editmandatoryfields.clear();
					auditcapturefields.clear();
					// lstTablename.add(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()));
					if (!lstTablename.contains(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()))) {
						lstTablename.add(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()));
						newJsonArray.clear();
						lstactionType.clear();
					}
					if (lstTablename.contains(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()))) {
						ObjectMapper mapper = new ObjectMapper();
						newJsonArray.clear();
						if (jsonAuditnew.has(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()))) {
							newJsonArray = mapper.readValue(jsonAuditnew
									.get(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()))
									.toString(), new TypeReference<List<Map<String, Object>>>() {
							});
						}
					}
					newJsonArray.add(newJsonObject);

					if (actionTypejson.has(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()))
							&& !(actionTypejson.get(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()))
									.equals(actionType.get(p)))) {
						lstactionType.add((String) actionTypejson
								.get(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass())));
						lstactionType.add(actionType.get(p));
						actionTypejson.put(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()),
								lstactionType);
					} else {
						actionTypejson.put(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()),
								actionType.get(p));
					}
					jsonAuditnew.put(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()), newJsonArray);

					break;
				case 2: // Edit AuditAction

					Map<String, Object> oldmap = oMapper.convertValue(oldobj.get(p), Map.class);

					// New JsonArrayAudit Call start
					List<String> lststablefeildnameold = new ArrayList<>();
					List<String> lststaticfeildnameold = new ArrayList<>();
					List<String> sameKeyNameCheckin = new ArrayList<>();
					List<String> sameKeyNameCheckOut = new ArrayList<>();
					List<String> lstsprimarytableforeignkeyold = new ArrayList<>();

					int sameKeyCountin = 0;
					int sameKeyCountout = 0;
					// JSONObject oldJsonObject = new
					// JSONObject(oMapper.writeValueAsString(oldmap));
					Map<String, Object> oldJsonObject = oldmap;
					// New Optimization Code Start

					List<Map<String, Object>> lstOldData = oMapper.convertValue(oldobj,
							new TypeReference<List<Map<String, Object>>>() {
					});
					List<String> lstTablenameOld = new ArrayList<String>();
					List<String> lstTablecolumnnameOld = new ArrayList<String>();
					List<String> lstrepeatingtableOld = new ArrayList<String>();
					StringBuilder sbOld = new StringBuilder();
					for (int th = 0; th < lst1.size(); th++) {
						String sforeigntablenameOld = (String) lst1.get(th).get("sforeigntablename");
						String sforeignprimarykeyOld = (String) lst1.get(th).get("sforeignprimarykey");
						String sforeignaliasnameOld = (String) lst1.get(th).get("sprimarytableforeignkey");
						List<String> primarykeyOld = null;
						// LinkedHashSet<String> dataOld=null;
						String primarykeyvalueOld = "";
						String queryOld = "";
						boolean checkOld = lstTablenameOld.stream().anyMatch(x -> x.equals(sforeigntablenameOld));
						if (!checkOld) {
							primarykeyvalueOld = oldmap.get(sforeignaliasnameOld).toString();
							if ((int) lst1.get(th).get("nqueryneed") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								String squery = (String) lst1.get(th).get("squery");
								StringBuffer sbuilderold1 = new StringBuffer();
								if (squery != null) {
									sbuilderold1.append(squery);
									while (squery.contains("P$")) {
										int nStart = squery.indexOf("P$");
										int nEnd = squery.indexOf("$P");
										sbuilderold1.replace(nStart, nEnd + 2, primarykeyvalueOld);
										squery = sbuilderold1.toString();
									}
									queryOld = squery + ";";
								}
							} else {
								queryOld = "select * from " + sforeigntablenameOld + " where " + sforeignprimarykeyOld
										+ " in (" + primarykeyvalueOld + ");";
							}
							sbOld.append(queryOld);
							lstTablenameOld.add(sforeigntablenameOld);
							lstTablecolumnnameOld.add(sforeignaliasnameOld);
						} else {
							primarykeyvalueOld = oldmap.get(sforeignaliasnameOld).toString();
							if ((int) lst1.get(th).get("nqueryneed") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								String squery = (String) lst1.get(th).get("squery");
								StringBuffer sbuilderold1 = new StringBuffer();
								if (squery != null) {
									sbuilderold1.append(squery);
									while (squery.contains("P$")) {
										int nStart = squery.indexOf("P$");
										int nEnd = squery.indexOf("$P");
										sbuilderold1.replace(nStart, nEnd + 2, primarykeyvalueOld);
										squery = sbuilderold1.toString();
									}
									queryOld = squery + ";";
								}
							} else {
								queryOld = "select * from " + sforeigntablenameOld + " where " + sforeignprimarykeyOld
										+ " in (" + primarykeyvalueOld + ");";
							}
							sbOld.append(queryOld);
							lstTablenameOld.add(sforeigntablenameOld + sforeignaliasnameOld);
							lstTablecolumnnameOld.add(sforeignaliasnameOld);
							lstrepeatingtableOld.add(sforeignaliasnameOld);
							// }
						}
					}
					/*
					 * Will be enabled with Alternate method final Map<String, Object>
					 * lstForeignColumndataOld = findByMultiQueryPlainSql(sbOld.toString(),
					 * lstTablenameOld.toArray());
					 */
					final Map<String, Object> lstForeignColumndataOld = projectDAOSupport.getMutlipleEntityResultsUsingPlainSql(
							sbOld.toString(), jdbcTemplate, lstTablenameOld.toArray());

					List<String> checkrepeatingTable1 = new ArrayList<>();
					for (int n = 0; n < lst.size(); n++) {
						if ((int) lst.get(n).get("nisforeigntable") == 1) {
							String key = lst.get(n).get("sprimarytableforeignkey").toString();
							if (oldJsonObject.containsKey(key)) {
								String fkstablename = (String) lst.get(n).get("sforeigntablename");
								if (lstForeignColumndataOld.containsKey(fkstablename)
										|| lstForeignColumndataOld.containsKey(fkstablename + key)) {
									String tablename = "";
									if (lstForeignColumndataOld.containsKey(fkstablename)
											&& !checkrepeatingTable1.contains(fkstablename)) {
										tablename = fkstablename;
										checkrepeatingTable1.add(tablename);
									} else if (lstForeignColumndataOld.containsKey(fkstablename + key)
											&& !checkrepeatingTable1.contains(fkstablename + key)) {
										tablename = fkstablename + key;
										checkrepeatingTable1.add(tablename);
									}
									if (tablename.equals("")) {
										tablename = fkstablename;
									}
									List<Map<String, Object>> foriegnMap = (List<Map<String, Object>>) lstForeignColumndataOld
											.get(tablename);
									for (int y = 0; y < foriegnMap.size(); y++) {
										if (((Map<String, Object>) foriegnMap.get(y)).containsKey("jsondata")) {
											if (foriegnMap.get(y).get(lst.get(n).get("sforeignprimarykey").toString())
													.toString()
													.equals(oldJsonObject
															.get(lst.get(n).get("sprimarytableforeignkey").toString())
															.toString())) {
												JSONObject foriegnjson = new JSONObject(
														((Map<String, Object>) foriegnMap.get(y)).get("jsondata")
														.toString());
												// if (foriegnjson.has(lst.get(n).get("stablefieldname").toString())) {
												if (foriegnjson.has(lst.get(n).get("stablefieldname").toString())
														&& !foriegnjson.get(lst.get(n).get("stablefieldname").toString())
														.getClass().toString().equals("class java.lang.String")) {
													oldJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
															((JSONObject) foriegnjson
																	.get(lst.get(n).get("stablefieldname").toString()))
															.get(objUserInfo.getSlanguagetypecode()));
												} else {
													if (((Map<String, Object>) foriegnMap.get(y))
															.containsKey(lst.get(n).get("stablefieldname").toString())) {
														oldJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
																((Map<String, Object>) foriegnMap.get(y))
																.get(lst.get(n).get("stablefieldname").toString()));
													} else {
														oldJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
																foriegnjson
																.get(lst.get(n).get("stablefieldname").toString()));
													}
												}
												// }
											}
										} else {
											if (foriegnMap.size() != 1) {
												if (foriegnMap.get(y).get(lst.get(n).get("sforeignprimarykey").toString())
														.toString()
														.equals(oldJsonObject
																.get(lst.get(n).get("sprimarytableforeignkey").toString())
																.toString())) {
													oldJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
															foriegnMap.get(y)
															.get(lst.get(n).get("stablefieldname").toString()));
												}
											} else {
												oldJsonObject.put((String) lst.get(n).get("sstaticfieldname"), foriegnMap
														.get(y).get(lst.get(n).get("stablefieldname").toString()));
											}
										}

									}
								}
							}
							lststaticfeildnameold.add((String) lst.get(n).get("sstaticfieldname"));
							lststablefeildnameold.add((String) lst.get(n).get("stablefieldname"));
							lstsprimarytableforeignkeyold.add((String) lst.get(n).get("sprimarytableforeignkey"));

						} else {
							oldJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
									oldmap.get(lst.get(n).get("stablefieldname")));
						}
						JSONObject objtemp = new JSONObject();
						auditcapturefields.put(lst.get(n).get("sstaticfieldname"));
						sameKeyNameCheckOut.add((String) lst.get(n).get("stablefieldname"));
						objtemp.put((String) lst.get(n).get("sstaticfieldname"), lst.get(n).get("sidsfieldname"));
						if ((int) lst.get(n).get("neditmainfield") == 1) {
							String checkstr = (String) lst.get(n).get("sstaticfieldname");
							long count = editmandatoryfields.toList().stream().filter(t -> checkstr.equals(t)).count();
							if (!(count > 0)) {
								editmandatoryfields.put(lst.get(n).get("sstaticfieldname"));
							}
						}
						multiLingualArray.put(objtemp);
						sPrimaryKey = (String) lst.get(n).get("stableprimarykey");
						ntransactionno = map.get(sPrimaryKey.trim()).toString();
						newJsonObject.put("sPrimaryKey", ntransactionno);
						oldJsonObject.put("sPrimaryKey", ntransactionno);
					}

					inputmap.put(
							"multilingualfields" + classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()),
							multiLingualArray.toString());
					inputmap.put(
							"editmandatoryfields" + classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()),
							editmandatoryfields.toString());
					inputmap.put(
							"auditcapturefields" + classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()),
							auditcapturefields.toString());
					auditcapturefields.clear();
					editmandatoryfields.clear();
					multiLingualArray.clear();

					if (actionTypejson.has(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()))
							&& actionType.size() > 1) {
						lstactionType.add((String) actionTypejson
								.get(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass())));
						lstactionType.add(actionType.get(p));
						actionTypejson.put(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()),
								actionType.get(p));
					} else {
						actionTypejson.put(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()),
								actionType.get(0));
					}
					if (!lstTablename.contains(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()))) {
						lstTablename.add(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()));
						newJsonArray.clear();
						oldJsonArray.clear();
						lstactionType.clear();
					}
					oldJsonArray.add(oldJsonObject);
					newJsonArray.add(newJsonObject);

					jsonAuditnew.put(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()), newJsonArray);
					jsonAuditold.put(classUtilityFunction.getEntityTableName(lstnewobject.get(p).getClass()), oldJsonArray);

					break;
				case 3:// Add and delete type
					for (int i = 0; i < lst.size(); i++) {
						int nforeigntable = (int) lst.get(i).get("nisforeigntable");
						sPrimaryKey = (String) lst.get(i).get("stableprimarykey");
						// ntransactionno = (int) map.get(sPrimaryKey);
						String strIDS = (String) lst.get(i).get("sIDSfieldname");
						String strStatic = genericLabelMap.containsKey(strIDS)
								? ((String) ((Map<String, Object>) (((genericLabelMap.get(strIDS)).getJsondata())
										.get("sdisplayname"))).get(objUserInfo.getSlanguagetypecode())).toUpperCase()
										: resourcebundle.containsKey(strIDS) ? resourcebundle.getString(strIDS).toUpperCase()
												: strIDS;
						if (nforeigntable > 0) {
							sb.append("select " + lst.get(i).get("stablefieldname") + " from "
									+ lst.get(i).get("sforeigntable") + " where " + lst.get(i).get("sforeignprimarykey")
									+ " = " + (int) map.get(lst.get(i).get("sforeigncolumn")) + ";");
							List<Map<String, Object>> lstTable = jdbcTemplate.queryForList(sb.toString());
							sb.delete(0, sb.length());
							if ((int) lst.get(i).get("nmultilingual") == 3) {
								String str1 = (String) lstTable.get(0).get(lst.get(i).get("stablefieldname"));
								str1 = genericLabelMap.containsKey(str1)
										? (String) ((Map<String, Object>) (((genericLabelMap.get(str1)).getJsondata())
												.get("sdisplayname"))).get(objUserInfo.getSlanguagetypecode())
												: resourcebundle.containsKey(str1) ? resourcebundle.getString(str1) : str1;
								sComments = sComments + strStatic + ": " + str1 + ";";
							} else {
								sComments = sComments + strStatic + ": "
										+ lstTable.get(0).get(lst.get(i).get("stablefieldname")) + ";";
							}

						} else {
							String str = (String) lst.get(i).get("stablefieldname");
							String str1 = "";
							if (map.get(str).getClass().getSimpleName().equals("Integer")) {
								int pp = (int) map.get(str);
								str1 = str.valueOf(pp);
							} else if (map.get(str).getClass().getSimpleName().equals("Double")) {
								double pp = (double) map.get(str);
								str1 = str.valueOf(pp);
							} else if (map.get(str).getClass().getSimpleName().equals("float")) {
								float pp = (float) map.get(str);
								str1 = str.valueOf(pp);
							} else if (map.get(str).getClass().getSimpleName().equals("Date")) {
								Date pp = (Date) map.get(str);
								str1 = str.valueOf(pp);
							} else if (map.get(str).getClass().getSimpleName().equals("long")) {
								long pp = (long) map.get(str);
								str1 = str.valueOf(pp);
							} else if (map.get(str).getClass().getSimpleName().equals("Short")) {
								short pp = (short) map.get(str);
								str1 = str.valueOf(pp);
							} else {
								if ((int) lst.get(i).get("nneeddateformat") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									try {
										final LocalDateTime datetime = LocalDateTime.parse((String) map.get(str),
												oldPattern);
										str1 = datetime.format(newPattern);
									} catch (Exception e) {
										str1 = (String) map.get(str);
									}
								} else {
									str1 = (String) map.get(str);
									str1 = str1.trim();
								}
							}
							if (!str1.equals("") && str1 != null) {
								if ((int) lst.get(i).get("nmultilingual") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									str1 = genericLabelMap.containsKey(str1)
											? (String) ((Map<String, Object>) (((genericLabelMap.get(str1)).getJsondata())
													.get("sdisplayname"))).get(objUserInfo.getSlanguagetypecode())
													: resourcebundle.containsKey(str1) ? resourcebundle.getString(str1) : str1;
									sComments = sComments + strStatic + ": " + str1 + ";";
									;
								} else {
									sComments = sComments + strStatic + ": " + str1 + ";";
								}

							}
						}
					}
					if (sComments != "" && sComments != null) {
						sComments = sComments1 + sComments;
						objAuditAction.setNformcode(objUserInfo.getNformcode());
						objAuditAction.setNuserrolecode(objUserInfo.getNuserrole());
						objAuditAction.setNdeputyrolecode(objUserInfo.getNdeputyuserrole());
						objAuditAction.setNauditcode(0);
						objAuditAction.setNmodulecode(objUserInfo.getNformcode());
						if (objUserInfo.getSreason() != "" && objUserInfo.getSreason() != null) {
							objAuditAction.setSactiontype(resourcebundle.getString("IDS_MANUAL").toUpperCase());
							objAuditComments.setScomments(sComments);
						} else {
							objAuditAction.setSactiontype(resourcebundle.getString("IDS_SYSTEM").toUpperCase());
							objAuditComments.setScomments(sComments);
						}
						objAuditAction.setNreasoncode(objUserInfo.getNreasoncode());
						objAuditAction.setSauditaction(genericLabelMap.containsKey(actionType.get(0))
								? ((String) ((Map<String, Object>) (((genericLabelMap.get(actionType.get(0))).getJsondata())
										.get("sdisplayname"))).get(objUserInfo.getSlanguagetypecode())).toUpperCase()
										: resourcebundle.containsKey(actionType.get(0))
										? resourcebundle.getString(actionType.get(0)).toUpperCase()
												: (String) actionType.get(0).toUpperCase());
						objAuditAction.setNusercode(objUserInfo.getNusercode());
						objAuditAction.setSreason(stringUtilityFunction.replaceQuote(objUserInfo.getSreason()));
						if (objUserInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							objAuditAction.setDauditdate(dateTimeUtilityFunction.getUTCDateTime());
						} else {
							objAuditAction.setDauditdate(LocalDateTime.now().toInstant(ZoneOffset.UTC));
						}
						final LocalDateTime datetime = LocalDateTime
								.ofInstant(objAuditAction.getDauditdate().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC);
						final String formatted = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);

						objAuditAction.setSauditdate(LocalDateTime.parse(formatted, oldPattern).format(newPattern));
						objAuditAction.setSday(String.valueOf(datetime.getDayOfMonth()).length() == 1
								? "0" + String.valueOf(datetime.getDayOfMonth())
								: String.valueOf(datetime.getDayOfMonth()));
						objAuditAction.setSmonth(String.valueOf(datetime.getMonthValue()).length() == 1
								? "0" + String.valueOf(datetime.getMonthValue())
								: String.valueOf(datetime.getMonthValue()));
						objAuditAction.setSyear(String.valueOf(datetime.getYear()));
						objAuditAction.setShour(String.valueOf(destFormat.format(datetime)));
						objAuditAction.setNdeputyusercode(objUserInfo.getNdeputyusercode());
						objAuditAction.setNsitecode(objUserInfo.getNtranssitecode());
						objAuditAction.setStablename((String) lst.get(0).get("stablename"));
						objAuditAction.setNstatus((short) 1);
						lstAuditaction.add(objAuditAction);
						objAuditComments.setNauditcode(0);
						objAuditComments.setNstatus((short) 1);
						lstAuditComments.add(objAuditComments);
						// Data needed for Get start
						objJsonData.put("sauditdate", objAuditAction.getSauditdate());
						objJsonData.put("scomments", objAuditComments.getScomments());
						objJsonData.put("susername", objUserInfo.getSusername());
						objJsonData.put("suserrolename", objUserInfo.getSuserrolename());
						objJsonData.put("sdeputyusername", objUserInfo.getSdeputyusername());
						objJsonData.put("sdeputyuserrolename", objUserInfo.getSdeputyuserrolename());
						objJsonData.put("sactiontype", objAuditAction.getSactiontype());
						objJsonData.put("viewPeriod", objAuditAction.getShour());
						objJsonData.put("sformname", objUserInfo.getSformname());
						objJsonData.put("smodulename", objUserInfo.getSmodulename());
						objJsonData.put("sreason", stringUtilityFunction.replaceQuote(objAuditAction.getSreason()));
						// Data needed for get End
					}
					break;
				}
			}
			if (nFlag == 1) {
				fnJsonArrayAudit(jsonAuditnew, null, actionTypejson, inputmap, true, objUserInfo);
			}
			if (nFlag == 2) {
				fnJsonArrayAudit(jsonAuditold, jsonAuditnew, actionTypejson, inputmap, true, objUserInfo);

			}
		}

	}

	/**
	 * This Method will capture audit for jsonData through javers(For Dynamic
	 * Templates)
	 * 
	 * @param oldJsonmap
	 * @param newJsonmap
	 * @param actionType
	 * @param inputMap
	 * @param isDataFromOldAuditFunction
	 * @param userInfo
	 * @throws Exception
	 */
	public void fnJsonArrayAudit(JSONObject oldJsonmap, JSONObject newJsonmap, JSONObject actionType,
			Map<String, Object> inputMap, boolean isDataFromOldAuditFunction, UserInfo userInfo) throws Exception {

		LOGGER.info("fnJsonArrayAudit");

		final int nisauditebale =  auditActionValidation(userInfo);

		if(nisauditebale == Enumeration.TransactionStatus.NO.gettransactionstatus() || !userInfo.getSreason().isEmpty())
		{

			final String sQuery = " lock  table lockaudit " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
			jdbcTemplate.execute(sQuery);
			String ntransactionno = "";
			JSONArray oldJsonarray = new JSONArray();
			JSONArray newJsonarray = new JSONArray();
			List<String> lstoldJsonmap = new ArrayList<>();
			List<String> lstnewJsonmap = new ArrayList<>();
			Map<String, Object> sortedOldmap = new LinkedHashMap<>();
			JSONObject nsorterjson = new JSONObject();
			lstoldJsonmap.addAll(oldJsonmap.keySet());
			String sComments = "";
			String sPrimaryKey = null;
			String sjsonsComments = "";
			String insStr = "";
			boolean nflag = false;
			boolean operationDone = false;
		
			ObjectMapper objmapper = new ObjectMapper();
			List<String> keySet = new ArrayList<String>();
			Javers javers = JaversBuilder.javers().build();
			objmapper.registerModule(new JavaTimeModule());
			AuditAction objAuditAction = new AuditAction();
			AuditComments objAuditComments = new AuditComments();
			
			final DateTimeFormatter oldPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			final DateTimeFormatter newPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final DateTimeFormatter destFormat = DateTimeFormatter.ofPattern("HH:00 (h a)");
			String sourceFormat = "yyyy-MM-dd HH:mm:ss";
			
			final String strLanguage = "select nlanguagecode, slanguagename, sfilename, slanguagetypecode, "
								+ " sreportingtoolfilename, ndefaultstatus, nsitecode, nstatus "
								+ " from Language where slanguagetypecode= '"
								+ userInfo.getSlanguagetypecode() + "'"
								+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final Language objLanguage = (Language) jdbcUtilityFunction.queryForObject(strLanguage, Language.class,
					jdbcTemplate);
			//		ResourceBundle resourcebundle = new PropertyResourceBundle(new InputStreamReader(
			//				getClass().getResourceAsStream("/"+Enumeration.Path.PROPERTIES_FILE.getPath() + objLanguage.getSfilename() + ".properties"),
			//				StandardCharsets.UTF_8));

			final ResourceBundle resourcebundle = commonFunction.getResourceBundle(objLanguage.getSfilename(), false);

			final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();
			final Map<String, GenericLabel> genericLabelMap = genericLabelList.stream()
					.collect(Collectors.toMap(GenericLabel::getSidsfieldname, genericLabel -> genericLabel));

			JSONObject customizedFeilds = null;
			List<String> lstauditcapturefeilds = new ArrayList<String>();
			JSONArray mandatoryFieldArray = null;
			JSONArray multiLingualArray = null;
			List<String> lstrightvalueblankcheck = new ArrayList<>();
			Map<String, String> multiLingualappliedmap = new LinkedHashMap<>();
			DynamicAuditTable query1 = new DynamicAuditTable();
			
			for (int j = 0; j < lstoldJsonmap.size(); j++) {
				String stablename = lstoldJsonmap.get(j);
				try {
					if (!isDataFromOldAuditFunction)

						query1 = (DynamicAuditTable) jdbcUtilityFunction.queryForObject(
								" select stableprimarykey, jsondata  from dynamicauditrecordtable where nformcode ="
										+ userInfo.getNformcode() + " and stablename = '" + stablename
										+ "' and nregtypecode=" + inputMap.get("nregtypecode") + " and nregsubtypecode="
										+ inputMap.get("nregsubtypecode") + " and ndesigntemplatemappingcode="
										+ inputMap.get("ndesigntemplatemappingcode") + " and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
										DynamicAuditTable.class, jdbcTemplate);
					customizedFeilds = new JSONObject(query1.getJsondata());
					sPrimaryKey = (String) query1.getStableprimarykey();
				} catch (Exception e) {
					customizedFeilds = null;
				}
				if (customizedFeilds != null || isDataFromOldAuditFunction) {
					lstauditcapturefeilds = objmapper.readValue(
							isDataFromOldAuditFunction ? inputMap.get("auditcapturefields" + stablename).toString()
									: customizedFeilds.get("auditcapturefields").toString(),
									List.class);
					mandatoryFieldArray = new JSONArray(
							isDataFromOldAuditFunction ? inputMap.get("editmandatoryfields" + stablename).toString()
									: customizedFeilds.get("editmandatoryfields").toString());
					multiLingualArray = new JSONArray(
							isDataFromOldAuditFunction ? inputMap.get("multilingualfields" + stablename).toString()
									: customizedFeilds.get("multilingualfields").toString());

				}
				oldJsonarray = new JSONArray(oldJsonmap.get(stablename).toString());
				if (newJsonmap != null) {
					if (newJsonmap.has(stablename)) {
						newJsonarray = newJsonmap.has(stablename) ? new JSONArray(newJsonmap.get(stablename).toString())
								: null;
					}
				} else {
					newJsonarray = null;
				}
				for (int i = 0; i < oldJsonarray.length(); i++) {
					if (!(oldJsonarray.get(i).equals(null))) {
						JSONObject oldJsonObject = ((JSONObject) oldJsonarray.get(i));
						List<String> lstoldJsonObjectKeys = new ArrayList<String>();
						List<String> lstoldJsonaftrRemoveKeys = new ArrayList<String>();
						List<String> lstnewJsonObjectKeys = new ArrayList<String>();
						lstoldJsonObjectKeys.addAll(oldJsonObject.keySet());
						JSONObject newJsonObject = null;
						if (customizedFeilds != null) {
							if (sPrimaryKey != null) {
								ntransactionno = sPrimaryKey != null ? oldJsonObject.keySet().contains(sPrimaryKey)
										? oldJsonObject.get(sPrimaryKey).toString()
												: "-1" : "-1";
							} else {
								ntransactionno = oldJsonObject.keySet().contains("sPrimaryKey")
										? oldJsonObject.get("sPrimaryKey").toString()
												: "-1";
							}
						}
						for (int x = 0; x < lstoldJsonObjectKeys.size(); x++) {
							if (!lstauditcapturefeilds.contains(lstoldJsonObjectKeys.get(x))) {
								oldJsonObject.remove(lstoldJsonObjectKeys.get(x));
							}
						}

						if (newJsonarray != null) {
							newJsonObject = ((JSONObject) newJsonarray.get(i));
							lstnewJsonObjectKeys.addAll(newJsonObject.keySet());
							for (int x = 0; x < lstnewJsonObjectKeys.size(); x++) {
								if (!lstauditcapturefeilds.contains(lstnewJsonObjectKeys.get(x))) {
									newJsonObject.remove(lstnewJsonObjectKeys.get(x));
								}
							}
							nflag = true;
							Diff diff = javers.compare(oldJsonObject, newJsonObject);
							if (diff.hasChanges()) {
								operationDone = true;
								final JSONObject finaljson = new JSONObject(oldJsonarray.get(i).toString());
								List<MapChange> lstMapChange = diff.getChangesByType(MapChange.class);
								List<EntryValueChange> lstMapEntryChanges = lstMapChange.get(0).getEntryValueChanges();
								List<EntryValueChange> lstMapSortedEntryChanges = new ArrayList<>();
								if (lstMapChange.get(0).getEntryAddedChanges().size() > 0) {
									List<EntryAdded> lstentryadded = lstMapChange.get(0).getEntryAddedChanges();
									for (int e = 0; e < lstentryadded.size(); e++) {
										EntryValueChange entryadded = new EntryValueChange(lstentryadded.get(e).getKey(),
												"", lstentryadded.get(e).getValue());
										lstMapEntryChanges.add(entryadded);
									}
								}
								List<String> lstMapEntryChangesKeys = new ArrayList<String>();
								lstMapEntryChanges.forEach(x -> {
									lstMapEntryChangesKeys.add(x.getKey().toString());
								});

								for (int x = 0; x < mandatoryFieldArray.length(); x++) {
									String mandatoryField = mandatoryFieldArray.get(x).toString();
									if (!lstMapEntryChangesKeys.contains(mandatoryFieldArray.get(x))) {
										if (finaljson.has(mandatoryFieldArray.get(x).toString())) {
											EntryValueChange mainFeildEntries = new EntryValueChange(
													mandatoryFieldArray.get(x),
													finaljson.get(mandatoryFieldArray.get(x).toString()), "");
											lstMapEntryChanges.add(mainFeildEntries);
										}
									}

								}
								for (int x = 0; x < multiLingualArray.length(); x++) {
									for (int y = 0; y < lstMapEntryChanges.size(); y++) {
										if (((JSONObject) multiLingualArray.get(x))
												.has((String) lstMapEntryChanges.get(y).getKey())) {
											String Key = isDataFromOldAuditFunction
													? genericLabelMap.containsKey(((JSONObject) multiLingualArray.get(x))
															.get((String) lstMapEntryChanges.get(y).getKey()).toString())
															? (String) ((Map<String, Object>) (((genericLabelMap
																	.get(((JSONObject) multiLingualArray.get(x))
																			.get((String) lstMapEntryChanges.get(y)
																					.getKey())
																			.toString()))
																	.getJsondata()).get("sdisplayname")))
																	.get(userInfo.getSlanguagetypecode())
																	: resourcebundle.containsKey(
																			((JSONObject) multiLingualArray.get(x))
																			.get((String) lstMapEntryChanges.get(y)
																					.getKey())
																			.toString()) ? resourcebundle.getString(
																					((JSONObject) multiLingualArray
																							.get(x))
																					.get((String) lstMapEntryChanges
																							.get(y)
																							.getKey())
																					.toString())
																					: (String) ((JSONObject) multiLingualArray
																							.get(x))
																					.get((String) lstMapEntryChanges
																							.get(y)
																							.getKey())
																					: (String) ((JSONObject) ((JSONObject) multiLingualArray.get(x))
																							.get((String) lstMapEntryChanges.get(y).getKey()))
																					.get(userInfo.getSlanguagetypecode());
											if (lstMapEntryChangesKeys
													.contains((String) lstMapEntryChanges.get(y).getKey())) {
												lstrightvalueblankcheck.add(Key);
											}
											String leftvalue = lstMapEntryChanges.get(y).getLeftValue().toString();
											String rightValue = lstMapEntryChanges.get(y).getRightValue().toString();
											multiLingualappliedmap.put((String) lstMapEntryChanges.get(y).getKey(), Key);

											EntryValueChange mainFeildEntries = new EntryValueChange(Key, leftvalue,
													rightValue);
											lstMapEntryChanges.remove(y);
											lstMapEntryChanges.add(mainFeildEntries);

											newJsonObject.put((String) mainFeildEntries.getKey(),
													(!mainFeildEntries.getRightValue().equals("")
															? mainFeildEntries.getRightValue()
																	: mainFeildEntries.getLeftValue()));

											break;
										}
									}

								}

								lstMapSortedEntryChanges.addAll(lstMapEntryChanges);
								lstMapEntryChanges.clear();
								for (int x = 0; x < mandatoryFieldArray.length(); x++) {
									int sortindex = x;
									if (multiLingualappliedmap.containsKey(mandatoryFieldArray.get(sortindex))) {
										String mandatoryField = multiLingualappliedmap
												.get(mandatoryFieldArray.get(sortindex));
										Optional<EntryValueChange> mainFeildEntriesOptional = lstMapSortedEntryChanges
												.stream()
												.filter(t -> ((EntryValueChange) t).getKey().equals(mandatoryField))
												.findAny();
										if (mainFeildEntriesOptional.isPresent()) {
											lstMapEntryChanges.remove(mainFeildEntriesOptional.get());
											lstMapEntryChanges.add(mainFeildEntriesOptional.get());
											lstMapSortedEntryChanges.remove(mainFeildEntriesOptional.get());
										}

									}
								}
								lstMapEntryChanges.addAll(lstMapSortedEntryChanges);
								// lstmultiLingualapplied.clear();
								sjsonsComments = lstMapEntryChanges.stream()
										.map(t -> ((EntryValueChange) t).getKey() + ":"
												+ (!((EntryValueChange) t).getRightValue().equals("")
														? ((EntryValueChange) t).getRightValue()
																: lstrightvalueblankcheck.contains(((EntryValueChange) t).getKey())
																? ((EntryValueChange) t).getRightValue()
																		: ((EntryValueChange) t).getLeftValue()))
										.collect(Collectors.joining(";")) + ";";

								final int nauditmodifiedcommentscode = (int) jdbcUtilityFunction.queryForObject(
																			"select nsequenceno+1 from seqnoaudittrail where stablename = 'auditmodifiedcomments'"
																			+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
										Integer.class, jdbcTemplate);
								final String getAuditLastRecord = "select nsequenceno+1 from seqnoaudittrail where stablename = 'auditaction'"
																 + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
								final int nauditcode = (int) jdbcUtilityFunction.queryForObject(getAuditLastRecord,
										Integer.class, jdbcTemplate);
								insStr = "INSERT INTO public.auditmodifiedcomments( "
										+ "	nauditmodifiedcommentscode, nauditcode, sfieldname, soldvalue, snewvalue, nstatus) values ";
								for (int k = 0; k < lstMapEntryChanges.size(); k++) {

									insStr += "(" + k + "+" + nauditmodifiedcommentscode + "," + nauditcode + ", '"
											+ stringUtilityFunction
											.replaceQuote(lstMapEntryChanges.get(k).getKey().toString())
											+ "', '"
											+ (lstMapEntryChanges.get(k).getLeftValue().toString().equals("") ? "-"
													: stringUtilityFunction.replaceQuote(
															lstMapEntryChanges.get(k).getLeftValue().toString()))
											+ "', '"
											+ (lstMapEntryChanges.get(k).getRightValue().toString().equals("") ? "-"
													: stringUtilityFunction.replaceQuote(
															lstMapEntryChanges.get(k).getRightValue().toString()))
											+ "', " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "), ";
								}
								jdbcTemplate.execute(insStr.substring(0, (insStr.length() - 2)).toString());
								insStr = "";
								jdbcTemplate.execute("update seqnoaudittrail set nsequenceno =" + lstMapEntryChanges.size()
								+ "+" + nauditmodifiedcommentscode + " where stablename ='auditmodifiedcomments'");
							}
						} else {
							operationDone = true;

							// Removing Unwanted fields End
							if (multiLingualArray != null) {

								// Sort Order Start
								int n = 0;
								for (int p = 0; p < lstauditcapturefeilds.size(); p++) {
									for (int s = 0; s < multiLingualArray.length(); s++) {
										String nonMultilingualKey = lstauditcapturefeilds.get(p);
										if (isDataFromOldAuditFunction) {
											if (multiLingualArray.get(s).getClass().toString()
													.equals("class java.lang.String") ?

															(multiLingualArray.get(s)).equals(nonMultilingualKey)
															&& oldJsonObject.has(nonMultilingualKey)

															: ((JSONObject) multiLingualArray.get(s))
															.has(nonMultilingualKey)
															&& oldJsonObject.has(nonMultilingualKey)) {
												String idsKey = multiLingualArray.get(s).getClass().toString().equals(
														"class java.lang.String") ? (multiLingualArray.get(s)).toString()
																: ((JSONObject) multiLingualArray.get(s))
																.get(nonMultilingualKey).toString();
												String Key = genericLabelMap.containsKey(idsKey)
														? (String) ((Map<String, Object>) (((genericLabelMap.get(idsKey))
																.getJsondata()).get("sdisplayname")))
																.get(userInfo.getSlanguagetypecode())
																: resourcebundle.containsKey(idsKey)
																? resourcebundle.getString(idsKey)
																		: idsKey;
												String Value = oldJsonObject.get(nonMultilingualKey).toString();
												nsorterjson.put(Key, n);
												sortedOldmap.put(Key, Value);
												if (Value.equals("")) {
													sortedOldmap.remove(Key);
												}
												n++;
											}
										} else {
											if (((JSONObject) multiLingualArray.get(s)).has(nonMultilingualKey)
													&& oldJsonObject.has(nonMultilingualKey)) {
												String Key = new JSONObject(((JSONObject) multiLingualArray.get(s))
														.get(lstauditcapturefeilds.get(p)).toString())
														.get(userInfo.getSlanguagetypecode()).toString();
												String Value = oldJsonObject.get(nonMultilingualKey).toString();
												nsorterjson.put(Key, n);
												sortedOldmap.put(Key, Value);
												if (Value.equals("")) {
													sortedOldmap.remove(Key);
												}
												n++;
											}
										}
									}
								}
								n = 0;
								oldJsonObject.clear();
								oldJsonObject.put("data", sortedOldmap);
								JSONArray array = new JSONArray(sortedOldmap.keySet().toArray());
								oldJsonObject.put("keys", array);

								sjsonsComments = sortedOldmap.keySet().stream()
										.map(k -> !sortedOldmap.get(k).equals("") ? k + ":" + sortedOldmap.get(k) : "")
										.filter(item -> !item.trim().isEmpty()).collect(Collectors.joining(";")) + ";";

								if (sjsonsComments.equals(";")) {
									operationDone = false;
								}
								sortedOldmap.clear();
							} else {

								for (int p = 0; p < lstauditcapturefeilds.size(); p++) {
									String Key = lstauditcapturefeilds.get(p);
									String Value = oldJsonObject.get(Key).toString();
									sortedOldmap.put(Key, Value);
									if (Value.equals("")) {
										sortedOldmap.remove(Key);
									}
								}
								oldJsonObject.clear();
								oldJsonObject.put("data", sortedOldmap);
								JSONArray array = new JSONArray(sortedOldmap.keySet().toArray());
								oldJsonObject.put("keys", array);
								sjsonsComments = sortedOldmap.keySet().stream()
										.map(k -> !sortedOldmap.get(k).equals("") ? k + ":" + sortedOldmap.get(k) : "")
										.filter(item -> !item.trim().isEmpty()).collect(Collectors.joining(";")) + ";";
								if (sjsonsComments.equals(";")) {
									operationDone = false;
								}
							}
						}
						if (operationDone) {
							operationDone = false;
							objAuditAction.setNformcode(userInfo.getNformcode());
							objAuditAction.setNuserrolecode(userInfo.getNuserrole());
							objAuditAction.setNdeputyrolecode(userInfo.getNdeputyuserrole());
							objAuditAction.setStransactionno(ntransactionno);
							objAuditAction.setNauditcode(0);
							objAuditAction.setNmodulecode(userInfo.getNmodulecode());
							if (userInfo.getSreason() != "" && userInfo.getSreason() != null) {
								objAuditAction.setSactiontype(resourcebundle.getString("IDS_MANUAL").toUpperCase());
								objAuditComments.setScomments(sComments);
							} else {
								objAuditAction.setSactiontype(resourcebundle.getString("IDS_SYSTEM").toUpperCase());
								objAuditComments.setScomments(sComments);
							}
							if (actionType.get(stablename).getClass().toString().equals("class org.json.JSONArray")) {
								objAuditAction.setSauditaction(resourcebundle
										.containsKey((String) (((JSONArray) actionType.get(stablename)).get(i)))
										? resourcebundle
												.getString(
														(String) (((JSONArray) actionType.get(stablename)).get(i)))
												.toUpperCase()
												: ((String) (((JSONArray) actionType.get(stablename)).get(i)))
												.toUpperCase());
							} else {
								objAuditAction
								.setSauditaction(genericLabelMap.containsKey((String) actionType.get(stablename))
										? ((String) ((Map<String, Object>) (((genericLabelMap
												.get((String) actionType.get(stablename))).getJsondata())
												.get("sdisplayname"))).get(userInfo.getSlanguagetypecode()))
												.toUpperCase()
												: resourcebundle.containsKey((String) actionType.get(stablename))
												? resourcebundle.getString((String) actionType.get(stablename))
														.toUpperCase()
														: ((String) actionType.get(stablename)).toUpperCase());
							}
							objAuditAction.setNusercode(userInfo.getNusercode());
							objAuditAction.setSreason(userInfo.getSreason());
							objAuditAction.setNreasoncode(userInfo.getNreasoncode());

							if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								objAuditAction.setDauditdate(dateTimeUtilityFunction.getUTCDateTime());
							} else {
								objAuditAction.setDauditdate(LocalDateTime.now().toInstant(ZoneOffset.UTC));
							}
							final LocalDateTime datetime = LocalDateTime.ofInstant(
									objAuditAction.getDauditdate().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC);
							final String formatted = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);

							objAuditAction.setSauditdate(LocalDateTime.parse(formatted, oldPattern).format(newPattern));
							objAuditAction.setSday(String.valueOf(datetime.getDayOfMonth()).length() == 1
									? "0" + String.valueOf(datetime.getDayOfMonth())
									: String.valueOf(datetime.getDayOfMonth()));
							objAuditAction.setSmonth(String.valueOf(datetime.getMonthValue()).length() == 1
									? "0" + String.valueOf(datetime.getMonthValue())
									: String.valueOf(datetime.getMonthValue()));
							objAuditAction.setSyear(String.valueOf(datetime.getYear()));
							objAuditAction.setShour(String.valueOf(destFormat.format(datetime)));
							objAuditAction.setNdeputyusercode(userInfo.getNdeputyusercode());
							objAuditAction.setNsitecode(userInfo.getNtranssitecode());
							objAuditAction.setStablename(stablename);
							objAuditAction.setNstatus((short) 1);
							objAuditComments.setNauditcode(0);
							objAuditComments.setNstatus((short) 1);
							final String getAuditLastRecord = "select nsequenceno from seqnoaudittrail where stablename = 'auditaction'"
															+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							int nauditcode = (int) jdbcUtilityFunction.queryForObject(getAuditLastRecord, Integer.class,
									jdbcTemplate);
							nauditcode++;
							String auditAction = "insert into auditaction (nauditcode,nusercode,nuserrolecode,ndeputyusercode "
												+ ",ndeputyrolecode,nformcode,nmodulecode, nreasoncode "
												+ ",stablename,dauditdate,sactiontype,sauditaction, "
												+ "stransactionno,sreason,nsitecode,nstatus,sday,smonth,syear,sauditdate,shour) values ("
												+ nauditcode + "," + objAuditAction.getNusercode() + ","
												+ objAuditAction.getNuserrolecode() + "," + objAuditAction.getNdeputyusercode() + ","
												+ objAuditAction.getNdeputyrolecode() + "," + objAuditAction.getNformcode() + ","
												+ objAuditAction.getNmodulecode() + "," + objAuditAction.getNreasoncode() + ",'"
												+ objAuditAction.getStablename() + "','" + objAuditAction.getDauditdate() + "','"
												+ stringUtilityFunction.replaceQuote(objAuditAction.getSactiontype()) + "','"
												+ stringUtilityFunction.replaceQuote(objAuditAction.getSauditaction()) + "','"
												+ objAuditAction.getStransactionno() + "','"
												+ stringUtilityFunction.replaceQuote(objAuditAction.getSreason()) + "',"
												+ objAuditAction.getNsitecode() + "," + objAuditAction.getNstatus() + ",'"
												+ objAuditAction.getSday() + "','" + objAuditAction.getSmonth() + "','"
												+ objAuditAction.getSyear() + "','" + objAuditAction.getSauditdate() + "','"
												+ objAuditAction.getShour() + "');";

							objAuditAction.setScomments(objAuditComments.getScomments());
							String auditComments = "";

							// Data needed for Get start
							JSONObject objJsonData = new JSONObject();
							objJsonData.put("nauditcode", nauditcode);
							objJsonData.put("sauditdate", objAuditAction.getSauditdate());
							objJsonData.put("sauditaction", objAuditAction.getSauditaction());
							objJsonData.put("scomments", sjsonsComments);
							objJsonData.put("susername", userInfo.getSusername());
							objJsonData.put("suserrolename", userInfo.getSuserrolename());
							objJsonData.put("sdeputyusername", userInfo.getSdeputyusername());
							objJsonData.put("sdeputyuserrolename", userInfo.getSdeputyuserrolename());
							objJsonData.put("sactiontype", objAuditAction.getSactiontype());
							objJsonData.put("viewPeriod", objAuditAction.getShour());
							objJsonData.put("sformname", userInfo.getSformname());
							objJsonData.put("smodulename", userInfo.getSmodulename());
							objJsonData.put("sreason", objAuditAction.getSreason());
							objJsonData.put("spredefinedreason", userInfo.getSpredefinedreason());
							objJsonData.put("slanguagename", userInfo.getSlanguagename());
							// Data needed for get End
							if (nflag) {
								objJsonData.put("jsoncomments", newJsonObject.toString());
								auditComments = auditAction
										+ "insert into auditcomments (nauditcode,scomments,jsondata,jsoncomments,nstatus) values("
										+ nauditcode + ",'" + stringUtilityFunction.replaceQuote(sjsonsComments) + "','"
										+ stringUtilityFunction.replaceQuote(objJsonData.toString()) + "','"
										+ stringUtilityFunction.replaceQuote(newJsonObject.toString()) + "'" + ","
										+ objAuditComments.getNstatus() + ");";

							} else {
								objJsonData.put("jsoncomments", oldJsonObject.toString());
								auditComments = auditAction
										+ "insert into auditcomments (nauditcode,scomments,jsondata,jsoncomments,nstatus) values("
										+ nauditcode + ",'" + stringUtilityFunction.replaceQuote(sjsonsComments) + "','"
										+ stringUtilityFunction.replaceQuote(objJsonData.toString()) + "','"
										+ stringUtilityFunction.replaceQuote(oldJsonObject.toString()) + "'" + ","
										+ objAuditComments.getNstatus() + ");";

							}
							jdbcTemplate.execute("update seqnoaudittrail set nsequenceno =" + nauditcode
									+ " where stablename ='auditaction'");
							jdbcTemplate.execute(auditComments);
						}
					}
				}
			}
		}
	}


	/***
	 * added by perumalraj for auditaction through java on 14/05/2019
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	public void fnInsertListAuditAction(List<?> lstnewobject, int nFlag, List<?> lstoldobj, List<String> actionType,
			UserInfo objUsers) throws Exception {

		LOGGER.info("fnInsertListAuditAction");
		final int nisauditebale =  auditActionValidation(objUsers);

		if(nisauditebale == Enumeration.TransactionStatus.NO.gettransactionstatus() || !objUsers.getSreason().isEmpty())
		{


			JSONObject objJsonData = new JSONObject();
			Map<String, Object> lstForeignColumndataold = new HashMap<String, Object>();
			List<String> lstmainrepeatingtables = new ArrayList<>();
			String smaintablename = "";
			String sPrimaryKey = "";
			String ntransactionno = "";
			List<Map<String, Object>> oldJsonArray = new ArrayList<>();
			List<Map<String, Object>> newJsonArray1 = new ArrayList<>();
			List<Map<String, Object>> newJsonArray = new ArrayList<>();
			String stablename = "";
			JSONObject jsonAuditnew = new JSONObject();
			JSONObject jsonAuditold = new JSONObject();

			List<String> lstTablename = new ArrayList<>();

			List<String> lstactionType = new ArrayList<>();
			JSONObject actionTypejson = new JSONObject();

			Map<String, Object> inputmap = new HashMap<String, Object>();
			JSONArray auditcapturefields = new JSONArray();
			JSONArray editmandatoryfields = new JSONArray();
			JSONArray multiLingualArray = new JSONArray();

			final String strLanguage = "select nlanguagecode, slanguagename, sfilename, slanguagetypecode, sreportingtoolfilename, ndefaultstatus, nsitecode, nstatus from Language where slanguagetypecode='"
					+ objUsers.getSlanguagetypecode() + "'"
				    + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final Language objLanguage = (Language) jdbcUtilityFunction.queryForObject(strLanguage, Language.class,
					jdbcTemplate);
			//		ResourceBundle resourcebundle = new PropertyResourceBundle(
			//				new InputStreamReader(
			//						getClass().getClassLoader()
			//								.getResourceAsStream("/"+Enumeration.Path.PROPERTIES_FILE.getPath() + objLanguage.getSfilename() + ".properties"),
			//						StandardCharsets.UTF_8));

			final ResourceBundle resourcebundle = commonFunction.getResourceBundle(objLanguage.getSfilename(), false);

			final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();
			final Map<String, GenericLabel> genericLabelMap = genericLabelList.stream()
					.collect(Collectors.toMap(GenericLabel::getSidsfieldname, genericLabel -> genericLabel));
			
			List<AuditAction> lstAuditaction = new ArrayList<>();
			List<AuditComments> lstAuditComments = new ArrayList<>();
			ObjectMapper oMapper = new ObjectMapper();
			oMapper.setSerializationInclusion(Include.NON_NULL);
			oMapper.registerModule(new JavaTimeModule());
			String sourceFormat = "yyyy-MM-dd HH:mm:ss";
			final DateTimeFormatter oldPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			final DateTimeFormatter newPattern = DateTimeFormatter.ofPattern(objUsers.getSdatetimeformat());
			final DateTimeFormatter destFormat = DateTimeFormatter.ofPattern("HH:00 (h a)");
			for (int p = 0; p < lstnewobject.size(); p++) {
				List<?> lstObject = (List<?>) lstnewobject.get(p);
				multiLingualArray.clear();
				editmandatoryfields.clear();
				auditcapturefields.clear();
				if (!lstObject.isEmpty()) {
					if (lstmainrepeatingtables
							.contains(classUtilityFunction.getEntityTableName(lstObject.get(0).getClass()).toString())) {
						if (actionType.size() > p) {
							lstmainrepeatingtables.add(
									(classUtilityFunction.getEntityTableName(lstObject.get(0).getClass()) + p).toString());
							smaintablename = (classUtilityFunction.getEntityTableName(lstObject.get(0).getClass()) + p)
									.toString();
							actionTypejson.put(smaintablename, actionType.get(p));
						}
					} else {
						lstmainrepeatingtables
						.add(classUtilityFunction.getEntityTableName(lstObject.get(0).getClass()).toString());
						smaintablename = classUtilityFunction.getEntityTableName(lstObject.get(0).getClass()).toString();
						actionTypejson.put(smaintablename, actionType.get(p));

					}
				}

				if (!lstObject.isEmpty()) {
					// int ntransactionno = 0;
					StringBuilder sb = new StringBuilder();
					StringBuilder sb1 = new StringBuilder();

					// for Insert and update below coding helpful to right a insert/Delete Audit in
					// AuditTrail
					// New Optimized Code start
					List<Map<String, Object>> lstData = oMapper.convertValue(lstObject,
							new TypeReference<List<Map<String, Object>>>() {
					});

					String strAuditactiontable = " select * from auditrecordtable where nformcode ="
							+ objUsers.getNformcode() + " and stablename = N'"
							+ classUtilityFunction.getEntityTableName(lstObject.get(0).getClass()) + "' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter asc ";

					// List<Map<String, Object>> lstAuditrecord =
					// jdbcTemplate.queryForList(strAuditactiontable);
					List<Map<String, Object>> lst = jdbcTemplate.queryForList(strAuditactiontable);

					List<Map<String, Object>> lst1 = lst.stream().filter((Map<String, Object> x) -> {
						return (int) x.get("nisforeigntable") == 1;
					}).collect(Collectors.toList());
					List<String> lstTablenamenew = new ArrayList<>();
					List<String> lstTablecolumnname = new ArrayList<>();
					List<String> lstrepeatingtable = new ArrayList<>();
					List<String> lstTablenameold = new ArrayList<>();
					List<String> lstTablecolumnnameold = new ArrayList<>();
					List<String> lstrepeatingtableold = new ArrayList<>();
					StringBuilder sbuilder = new StringBuilder();
					StringBuilder sbuilderold = new StringBuilder();
					// For new Object start
					for (int th = 0; th < lst1.size(); th++) {
						String sforeigntablename = (String) lst1.get(th).get("sforeigntablename");
						String sforeignprimarykey = (String) lst1.get(th).get("sforeignprimarykey");
						String sforeignkeyalias = (String) lst1.get(th).get("sprimarytableforeignkey");
						List<String> primarykey = null;
						String primarykeyvalue = "";
						String query = "";
						boolean check = lstTablenamenew.stream().anyMatch(x -> x.equals(sforeigntablename));
						if (!check) {
							primarykeyvalue = lstData.stream().map(x -> String.valueOf(x.get(sforeignkeyalias))).distinct()
									.collect(Collectors.joining(","));
							// primarykeyvalue = map.get(sforeignkeyalias).toString();
							if ((int) lst1.get(th).get("nqueryneed") == 3) {
								String squery = (String) lst1.get(th).get("squery");
								StringBuilder sbuilder1 = new StringBuilder();
								if (squery != null) {
									sbuilder1.append(squery);
									while (squery.contains("P$")) {
										int nStart = squery.indexOf("P$");
										int nEnd = squery.indexOf("$P");
										sbuilder1.replace(nStart, nEnd + 2, primarykeyvalue);
										squery = sbuilder1.toString();
									}
									query = squery + ";";
								}
							} else {
								query = "select * from " + sforeigntablename + " where " + sforeignprimarykey + " in ("
										+ primarykeyvalue + ");";
							}
							sbuilder.append(query);
							lstTablenamenew.add(sforeigntablename);
							lstTablecolumnname.add(sforeignkeyalias);
						} else {
							// boolean check1 = lstTablecolumnname.stream().anyMatch(x ->
							// x.equals(sforeignkeyalias));
							// if (check1) {
							primarykeyvalue = lstData.stream().map(x -> String.valueOf(x.get(sforeignkeyalias))).distinct()
									.collect(Collectors.joining(","));
							if ((int) lst1.get(th).get("nqueryneed") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								String squery = (String) lst1.get(th).get("squery");
								StringBuilder sbuilder1 = new StringBuilder();
								if (squery != null) {
									sbuilder1.append(squery);
									while (squery.contains("P$")) {
										int nStart = squery.indexOf("P$");
										int nEnd = squery.indexOf("$P");
										sbuilder1.replace(nStart, nEnd + 2, primarykeyvalue);
										squery = sbuilder1.toString();
									}
									query = squery + ";";
								}
							} else {

								query = "select * from " + sforeigntablename + " where " + sforeignprimarykey + " in ("
										+ primarykeyvalue + ");";
							}
							sbuilder.append(query);
							lstTablenamenew.add(sforeigntablename + sforeignkeyalias);
							lstTablecolumnname.add(sforeignkeyalias);
							lstrepeatingtable.add(sforeignkeyalias);
							// }
						}
					}
					/*
					 * Will be enabled with Alternate method final Map<String, Object>
					 * lstForeignColumndata = findByMultiQueryPlainSql(sbuilder.toString(),
					 * lstTablenamenew.toArray());
					 */
					final Map<String, Object> lstForeignColumndata = projectDAOSupport.getMutlipleEntityResultsUsingPlainSql(
							sbuilder.toString(), jdbcTemplate, lstTablenamenew.toArray());

					// For new object end

					// For old object start
					if (lstoldobj != null && !lstoldobj.isEmpty()) {
						List<?> lstObjectold = (List<?>) lstoldobj.get(p);
						List<Map<String, Object>> lstDataOld = oMapper.convertValue(lstObjectold,
								new TypeReference<List<Map<String, Object>>>() {
						});
						for (int th = 0; th < lst1.size(); th++) {
							String sforeigntablename = (String) lst1.get(th).get("sforeigntablename");
							String sforeignprimarykey = (String) lst1.get(th).get("sforeignprimarykey");
							String sforeignkeyalias = (String) lst1.get(th).get("sprimarytableforeignkey");
							List<String> primarykey = null;
							String primarykeyvalue = "";
							String query = "";
							boolean check = lstTablenameold.stream().anyMatch(x -> x.equals(sforeigntablename));
							if (!check) {
								primarykeyvalue = lstDataOld.stream().map(x -> String.valueOf(x.get(sforeignkeyalias)))
										.distinct().collect(Collectors.joining(","));
								// primarykeyvalue = map.get(sforeignkeyalias).toString();
								if ((int) lst1.get(th).get("nqueryneed") == 3) {
									String squery = (String) lst1.get(th).get("squery");
									StringBuilder sbuilder1 = new StringBuilder();
									if (squery != null) {
										sbuilder1.append(squery);
										while (squery.contains("P$")) {
											int nStart = squery.indexOf("P$");
											int nEnd = squery.indexOf("$P");
											sbuilder1.replace(nStart, nEnd + 2, primarykeyvalue);
											squery = sbuilder1.toString();
										}
										query = squery + ";";
									}
								} else {
									query = "select * from " + sforeigntablename + " where " + sforeignprimarykey + " in ("
											+ primarykeyvalue + ");";
								}
								sbuilderold.append(query);
								lstTablenameold.add(sforeigntablename);
								lstTablecolumnnameold.add(sforeignkeyalias);
							} else {
								// boolean check1 = lstTablecolumnnameold.stream().anyMatch(x ->
								// x.equals(sforeignkeyalias));
								// if (check1 == false) {
								primarykeyvalue = lstDataOld.stream().map(x -> String.valueOf(x.get(sforeignkeyalias)))
										.distinct().collect(Collectors.joining(","));
								if ((int) lst1.get(th).get("nqueryneed") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									String squery = (String) lst1.get(th).get("squery");
									StringBuilder sbuilder1 = new StringBuilder();
									if (squery != null) {
										sbuilder1.append(squery);
										while (squery.contains("P$")) {
											int nStart = squery.indexOf("P$");
											int nEnd = squery.indexOf("$P");
											sbuilder1.replace(nStart, nEnd + 2, primarykeyvalue);
											squery = sbuilder1.toString();
										}
										query = squery + ";";
									}
								} else {
									query = "select * from " + sforeigntablename + " where " + sforeignprimarykey + " in ("
											+ primarykeyvalue + ");";
								}
								sbuilderold.append(query);
								lstTablenameold.add(sforeigntablename + sforeignkeyalias);
								lstTablecolumnnameold.add(sforeignkeyalias);
								lstrepeatingtableold.add(sforeignkeyalias);
								// }
							}
						}
						/*
						 * Will be enabled with Alternate method lstForeignColumndataold =
						 * findByMultiQueryPlainSql(sbuilderold.toString(), lstTablenameold.toArray());
						 */
						lstForeignColumndataold = projectDAOSupport.getMutlipleEntityResultsUsingPlainSql(sbuilderold.toString(),
								jdbcTemplate, lstTablenameold.toArray());
					}
					// For old object end

					// New Optimzed code end
					switch (nFlag) {
					case 1:
						for (int s = 0; s < lstObject.size(); s++) {

							AuditComments objAuditComments = new AuditComments();
							AuditAction objAuditAction = new AuditAction();
							String sComments = "";
							String sComments1 = "";
							strAuditactiontable = " select * from auditrecordtable where nformcode ="
									+ objUsers.getNformcode() + " and stablename = N'"
									+ classUtilityFunction.getEntityTableName(lstObject.get(s).getClass())
									+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " order by nsorter asc ";
							Map<String, Object> map = oMapper.convertValue(lstObject.get(s), Map.class);
							// New JsonArrayAudit Call start
							// JSONObject newJsonObject = new JSONObject(oMapper.writeValueAsString(map));
							Map<String, Object> newJsonObject = map;
							List<String> sameKeyNameCheckin1 = new ArrayList<>();
							List<String> sameKeyNameCheckOut1 = new ArrayList<>();
							List<Map<String, Object>> lstTableParam = new ArrayList<Map<String, Object>>();
							// lst.forEach(t->{
							List<String> checkrepeatingTable = new ArrayList<>();
							for (int n = 0; n < lst.size(); n++) {
								if ((int) lst.get(n).get("nisforeigntable") == 1) {

									String key = lst.get(n).get("sprimarytableforeignkey").toString();
									if (newJsonObject.containsKey(key)) {
										String fkstablename = (String) lst.get(n).get("sforeigntablename");
										if (lstForeignColumndata.containsKey(fkstablename)
												|| lstForeignColumndata.containsKey(fkstablename + key)) {
											String tablename = "";
											if (lstForeignColumndata.containsKey(fkstablename + key)) {
												tablename = fkstablename + key;
											} else if (lstForeignColumndata.containsKey(fkstablename)) {
												tablename = fkstablename;
											}
											// Commented by Vishakh - ALPD-1430
											//										if (lstForeignColumndata.containsKey(fkstablename)
											//												&& !checkrepeatingTable.contains(fkstablename)) {
											//											tablename = fkstablename;
											//											checkrepeatingTable.add(tablename);
											//										} else if (lstForeignColumndata.containsKey(fkstablename + key)
											//												&& !checkrepeatingTable.contains(fkstablename + key)) {
											//											tablename = fkstablename + key;
											//											checkrepeatingTable.add(tablename);
											//										}
											List<Map<String, Object>> foriegnMap = (List<Map<String, Object>>) lstForeignColumndata
													.get(tablename);
											for (int y = 0; y < foriegnMap.size(); y++) {
												if (((Map<String, Object>) foriegnMap.get(y)).containsKey("jsondata")) {
													if (foriegnMap.get(y)
															.get(lst.get(n).get("sforeignprimarykey").toString()).toString()
															.equals(newJsonObject.get(
																	lst.get(n).get("sprimarytableforeignkey").toString())
																	.toString())) {
														JSONObject foriegnjson = new JSONObject(
																((Map<String, Object>) foriegnMap.get(y)).get("jsondata")
																.toString());
														if (foriegnjson.has(lst.get(n).get("stablefieldname").toString())
																&& !foriegnjson
																.get(lst.get(n).get("stablefieldname").toString())
																.getClass().toString()
																.equals("class java.lang.String")) {
															newJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
																	((JSONObject) foriegnjson.get(
																			lst.get(n).get("stablefieldname").toString()))
																	.get(objUsers.getSlanguagetypecode()));
														} else {
															if (((Map<String, Object>) foriegnMap.get(y)).containsKey(
																	lst.get(n).get("stablefieldname").toString())) {
																newJsonObject.put(
																		(String) lst.get(n).get("sstaticfieldname"),
																		((Map<String, Object>) foriegnMap.get(y)).get(lst
																				.get(n).get("stablefieldname").toString()));
															} else {
																newJsonObject.put(
																		(String) lst.get(n).get("sstaticfieldname"),
																		foriegnjson.get(lst.get(n).get("stablefieldname")
																				.toString()));
															}

														}
													}
												} else {
													// newJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
													// foriegnMap.get(y).get(lst.get(n).get("stablefieldname").toString()));
													if (foriegnMap.size() != 1) {
														if (foriegnMap.get(y)
																.get(lst.get(n).get("sforeignprimarykey").toString())
																.toString()
																.equals(newJsonObject.get(lst.get(n)
																		.get("sprimarytableforeignkey").toString())
																		.toString())) {
															System.out.println(
																	"audit====" + lst.get(n).get("sstaticfieldname"));
															newJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
																	foriegnMap.get(y).get(
																			lst.get(n).get("stablefieldname").toString()));
														}
													} else {
														System.out
														.println("audit====" + lst.get(n).get("sstaticfieldname"));
														newJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
																foriegnMap.get(y)
																.get(lst.get(n).get("stablefieldname").toString()));
													}
												}

											}
										}
									}
								} else {
									newJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
											map.get(lst.get(n).get("stablefieldname")));
								}
								JSONObject objtemp = new JSONObject();
								auditcapturefields.put((String) lst.get(n).get("sstaticfieldname"));
								sameKeyNameCheckOut1.add((String) lst.get(n).get("stablefieldname"));
								objtemp.put((String) lst.get(n).get("sstaticfieldname"), lst.get(n).get("sidsfieldname"));
								if ((int) lst.get(n).get("neditmainfield") == 1) {
									editmandatoryfields.put(lst.get(n).get("sstaticfieldname"));
								}
								// }
								// objtemp.put((String) lst.get(n).get("stablefieldname"),
								// lst.get(n).get("sidsfieldname"));
								multiLingualArray.put(objtemp);
								sPrimaryKey = (String) lst.get(n).get("stableprimarykey");
								// ntransactionno = (int) map.get(sPrimaryKey);
								ntransactionno = map.get(sPrimaryKey.trim()).toString();
								newJsonObject.put("sPrimaryKey", ntransactionno);
							}
							inputmap.put("multilingualfields" + smaintablename, multiLingualArray.toString());
							inputmap.put("editmandatoryfields" + smaintablename, editmandatoryfields.toString());
							inputmap.put("auditcapturefields" + smaintablename, auditcapturefields.toString());

							if (!lstTablename.contains(smaintablename)) {
								lstTablename.add(smaintablename);
								newJsonArray.clear();
								lstactionType.clear();
							}
							inputmap.put("tableName", classUtilityFunction.getEntityTableName(lstObject.get(s).getClass()));
							newJsonArray.add(newJsonObject);
							jsonAuditnew.put(smaintablename, newJsonArray);

						}

						break;

					case 2:// Edit or Update------Added by sathish 30/09/2020
						List<?> oldlstObject = (List<?>) lstoldobj.get(p);
						for (int s = 0; s < lstObject.size(); s++) {

							String sComments = "";
							String sComments1 = "";
							AuditAction objAuditAction = new AuditAction();
							AuditComments objAuditComments = new AuditComments();
							strAuditactiontable = " select * from auditrecordtable where nformcode ="
									+ objUsers.getNformcode() + " and stablename = N'"
									+ classUtilityFunction.getEntityTableName(lstObject.get(s).getClass())
									+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " order by nsorter asc ";
							Map<String, Object> map = oMapper.convertValue(lstObject.get(s), Map.class);
							Map<String, Object> oldmap = oMapper.convertValue(oldlstObject.get(s), Map.class);
							// New JsonArrayAudit Call start
							List<String> sameKeyNameCheckin = new ArrayList<>();
							List<String> sameKeyNameCheckOut = new ArrayList<>();
							List<Map<String, Object>> lstTableParam1 = new ArrayList<Map<String, Object>>();
							List<Map<String, Object>> lstTableParam2 = new ArrayList<Map<String, Object>>();
							Map<String, Object> oldJsonObject = oldmap;
							Map<String, Object> newJsonObject1 = map;
							// lst.forEach(t->{
							for (int n = 0; n < lst.size(); n++) {
								if ((int) lst.get(n).get("nisforeigntable") == 1) {

									String key = lst.get(n).get("sprimarytableforeignkey").toString();
									if (newJsonObject1.containsKey(key)) {
										String fkstablename = (String) lst.get(n).get("sforeigntablename");
										if (lstForeignColumndata.containsKey(fkstablename)
												|| lstForeignColumndata.containsKey(fkstablename + key)) {
											String tablename = "";
											if (lstForeignColumndata.containsKey(fkstablename + key)) {
												tablename = fkstablename + key;
											} else if (lstForeignColumndata.containsKey(fkstablename)) {
												tablename = fkstablename;
											}
											List<Map<String, Object>> foriegnMap = (List<Map<String, Object>>) lstForeignColumndata
													.get(tablename);
											for (int y = 0; y < foriegnMap.size(); y++) {
												if (((Map<String, Object>) foriegnMap.get(y)).containsKey("jsondata")) {
													if (foriegnMap.get(y)
															.get(lst.get(n).get("sforeignprimarykey").toString()).toString()
															.equals(newJsonObject1.get(
																	lst.get(n).get("sprimarytableforeignkey").toString())
																	.toString())) {

														JSONObject foriegnjson = new JSONObject(
																((Map<String, Object>) foriegnMap.get(y)).get("jsondata")
																.toString());
														if (foriegnjson.has(lst.get(n).get("stablefieldname").toString())) {
															newJsonObject1.put((String) lst.get(n).get("sstaticfieldname"),
																	((JSONObject) foriegnjson.get(
																			lst.get(n).get("stablefieldname").toString()))
																	.get(objUsers.getSlanguagetypecode()));
														}
													}
												} else {
													if (foriegnMap.get(y)
															.get(lst.get(n).get("sforeignprimarykey").toString()).toString()
															.equals(newJsonObject1.get(
																	lst.get(n).get("sprimarytableforeignkey").toString())
																	.toString())) {
														newJsonObject1.put((String) lst.get(n).get("sstaticfieldname"),
																foriegnMap.get(y)
																.get(lst.get(n).get("stablefieldname").toString()));
													}
												}

											}
										}
									}
									if (oldJsonObject.containsKey(key)) {
										String fkstablename = (String) lst.get(n).get("sforeigntablename");
										if (lstForeignColumndataold.containsKey(fkstablename)
												|| lstForeignColumndataold.containsKey(fkstablename + key)) {
											String tablename = "";
											if (lstForeignColumndataold.containsKey(fkstablename + key)) {
												tablename = fkstablename + key;
											} else if (lstForeignColumndataold.containsKey(fkstablename)) {
												tablename = fkstablename;
											}
											List<Map<String, Object>> foriegnMap = (List<Map<String, Object>>) lstForeignColumndataold
													.get(tablename);
											for (int y = 0; y < foriegnMap.size(); y++) {
												if (((Map<String, Object>) foriegnMap.get(y)).containsKey("jsondata")) {
													if (foriegnMap.get(y)
															.get(lst.get(n).get("sforeignprimarykey").toString()).toString()
															.equals(oldJsonObject.get(
																	lst.get(n).get("sprimarytableforeignkey").toString())
																	.toString())) {
														JSONObject foriegnjson = new JSONObject(
																((Map<String, Object>) foriegnMap.get(y)).get("jsondata")
																.toString());
														if (foriegnjson.has(lst.get(n).get("stablefieldname").toString())) {
															oldJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
																	((JSONObject) foriegnjson.get(
																			lst.get(n).get("stablefieldname").toString()))
																	.get(objUsers.getSlanguagetypecode()));
														}
													}
												} else {
													if (foriegnMap.get(y)
															.get(lst.get(n).get("sforeignprimarykey").toString()).toString()
															.equals(oldJsonObject.get(
																	lst.get(n).get("sprimarytableforeignkey").toString())
																	.toString())) {
														oldJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
																foriegnMap.get(y)
																.get(lst.get(n).get("stablefieldname").toString()));
													}
												}

											}
										}
									}
									// List Call End
								} else {
									oldJsonObject.put((String) lst.get(n).get("sstaticfieldname"),
											oldmap.get(lst.get(n).get("stablefieldname")));
									newJsonObject1.put((String) lst.get(n).get("sstaticfieldname"),
											map.get(lst.get(n).get("stablefieldname")));
								}
								JSONObject objtemp = new JSONObject();
								auditcapturefields.put(lst.get(n).get("sstaticfieldname"));
								sameKeyNameCheckOut.add((String) lst.get(n).get("stablefieldname"));
								objtemp.put((String) lst.get(n).get("sstaticfieldname"), lst.get(n).get("sidsfieldname"));
								if ((int) lst.get(n).get("neditmainfield") == 1) {
									String checkstr = (String) lst.get(n).get("sstaticfieldname");
									long count = editmandatoryfields.toList().stream().filter(t -> checkstr.equals(t))
											.count();
									if (!(count > 0)) {
										editmandatoryfields.put(lst.get(n).get("sstaticfieldname"));
									}
								}
								// }
								multiLingualArray.put(objtemp);
								sPrimaryKey = (String) lst.get(n).get("stableprimarykey");
								// ntransactionno = (int) map.get(sPrimaryKey);
								ntransactionno = map.get(sPrimaryKey.trim()).toString();
								newJsonObject1.put("sPrimaryKey", ntransactionno);
								oldJsonObject.put("sPrimaryKey", ntransactionno);
							}
							// });
							inputmap.put(
									"multilingualfields"
											+ classUtilityFunction.getEntityTableName(lstObject.get(s).getClass()),
											multiLingualArray.toString());
							inputmap.put(
									"editmandatoryfields"
											+ classUtilityFunction.getEntityTableName(lstObject.get(s).getClass()),
											editmandatoryfields.toString());
							inputmap.put(
									"auditcapturefields"
											+ classUtilityFunction.getEntityTableName(lstObject.get(s).getClass()),
											auditcapturefields.toString());
							// lstTablename.add(classUtilityFunction.getEntityTableName(lstObject.get(s).getClass()));

							if (!lstTablename
									.contains(classUtilityFunction.getEntityTableName(lstObject.get(s).getClass()))) {
								lstTablename.add(classUtilityFunction.getEntityTableName(lstObject.get(s).getClass()));
								newJsonArray1.clear();
								oldJsonArray.clear();
								lstactionType.clear();
							}

							oldJsonArray.add(oldJsonObject);
							newJsonArray1.add(newJsonObject1);
							jsonAuditnew.put(classUtilityFunction.getEntityTableName(lstObject.get(s).getClass()),
									newJsonArray1);
							jsonAuditold.put(classUtilityFunction.getEntityTableName(lstObject.get(s).getClass()),
									oldJsonArray);
						}

						break;
					case 3:// delete type-----Added by sathish 30/09/2020
						for (int s = 0; s < lstObject.size(); s++) {
							Map<String, Object> map = oMapper.convertValue(lstObject.get(s), Map.class);
							String sComments = "";
							String sComments1 = "";
							AuditAction objAuditAction = new AuditAction();
							AuditComments objAuditComments = new AuditComments();
							strAuditactiontable = " select * from auditrecordtable where nformcode ="
									+ objUsers.getNformcode() + " and stablename = N'"
									+ classUtilityFunction.getEntityTableName(lstObject.get(s).getClass())
									+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							// List<Map<String, Object>> lst =
							// jdbcTemplate.queryForList(strAuditactiontable);

							for (int i = 0; i < lst.size(); i++) {
								int nforeigntable = (int) lst.get(i).get("nisforeigntable");
								sPrimaryKey = (String) lst.get(i).get("stableprimarykey");
								// ntransactionno = (int) map.get(sPrimaryKey);
								String strIDS = (String) lst.get(i).get("sIDSfieldname");
								String strStatic = genericLabelMap.containsKey(strIDS)
										? ((String) ((Map<String, Object>) (((genericLabelMap.get(strIDS)).getJsondata())
												.get("sdisplayname"))).get(objUsers.getSlanguagetypecode())).toUpperCase()
												: resourcebundle.containsKey(strIDS)
												? resourcebundle.getString(strIDS).toUpperCase()
														: strIDS;
								if (nforeigntable > 0) {
									sb.append("select " + lst.get(i).get("stablefieldname") + " from "
											+ lst.get(i).get("sforeigntable") + " where "
											+ lst.get(i).get("sforeignprimarykey") + " = "
											+ (int) map.get(lst.get(i).get("sforeigncolumn")) + ";");
									List<Map<String, Object>> lstTable = jdbcTemplate.queryForList(sb.toString());
									sb.delete(0, sb.length());
									if ((int) lst.get(i).get("nmultilingual") == 3) {
										String str1 = (String) lstTable.get(0).get(lst.get(i).get("stablefieldname"));
										str1 = genericLabelMap.containsKey(str1)
												? (String) ((Map<String, Object>) (((genericLabelMap.get(str1))
														.getJsondata()).get("sdisplayname")))
														.get(objUsers.getSlanguagetypecode())
														: resourcebundle.containsKey(str1) ? resourcebundle.getString(str1) : str1;
										sComments = sComments + strStatic + ": " + str1 + ";";
									} else {
										sComments = sComments + strStatic + ": "
												+ lstTable.get(0).get(lst.get(i).get("stablefieldname")) + ";";
									}

								} else {
									String str = (String) lst.get(i).get("stablefieldname");
									String str1 = "";
									if (map.get(str).getClass().getSimpleName().equals("Integer")) {
										int pp = (int) map.get(str);
										str1 = str.valueOf(pp);
									} else if (map.get(str).getClass().getSimpleName().equals("Double")) {
										double pp = (double) map.get(str);
										str1 = str.valueOf(pp);
									} else if (map.get(str).getClass().getSimpleName().equals("float")) {
										float pp = (float) map.get(str);
										str1 = str.valueOf(pp);
									} else if (map.get(str).getClass().getSimpleName().equals("Date")) {
										Date pp = (Date) map.get(str);
										str1 = str.valueOf(pp);
									} else if (map.get(str).getClass().getSimpleName().equals("long")) {
										long pp = (long) map.get(str);
										str1 = str.valueOf(pp);
									} else if (map.get(str).getClass().getSimpleName().equals("Short")) {
										long pp = (short) map.get(str);
										str1 = str.valueOf(pp);
									} else {

										// ---Start of string loop

										if ((int) lst.get(i).get("nneeddateformat") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
											try {
												final LocalDateTime datetime = LocalDateTime.parse((String) map.get(str),
														oldPattern);
												str1 = datetime.format(newPattern);
											} catch (Exception e) {
												str1 = (String) map.get(str);
											}
										} else {
											str1 = (String) map.get(str);
											str1 = str1.trim();
										}
										// End of string loop
									}
									if (!str1.equals("") && str1 != null) {
										if ((int) lst.get(i).get("nmultilingual") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
											str1 = genericLabelMap.containsKey(str1)
													? (String) ((Map<String, Object>) (((genericLabelMap.get(str1))
															.getJsondata()).get("sdisplayname")))
															.get(objUsers.getSlanguagetypecode())
															: resourcebundle.containsKey(str1) ? resourcebundle.getString(str1)
																	: str1;
											sComments = sComments + strStatic + ": " + str1 + ";";
											;
										} else {
											sComments = sComments + strStatic + ": " + str1 + ";";
										}

									}
								}
							}
							if (sComments != null && !sComments.isEmpty()) {
								sComments = sComments1 + sComments;
								objAuditAction.setNformcode(objUsers.getNformcode());
								objAuditAction.setNuserrolecode(objUsers.getNuserrole());
								objAuditAction.setNdeputyrolecode(objUsers.getNdeputyuserrole());
								objAuditAction.setNauditcode(0);
								objAuditAction.setNmodulecode(objUsers.getNformcode());
								if (objUsers.getSreason() != null && !objUsers.getSreason().isEmpty()) {
									objAuditAction.setSactiontype(resourcebundle.getString("IDS_MANUAL").toUpperCase());
									objAuditComments.setScomments(sComments);
								} else {
									objAuditAction.setSactiontype(resourcebundle.getString("IDS_SYSTEM").toUpperCase());
									objAuditComments.setScomments(sComments);
								}

								objAuditAction.setSauditaction(genericLabelMap.containsKey(actionType.get(0))
										? ((String) ((Map<String, Object>) (((genericLabelMap.get(actionType.get(0)))
												.getJsondata()).get("sdisplayname"))).get(objUsers.getSlanguagetypecode()))
												.toUpperCase()
												: resourcebundle.containsKey(actionType.get(0))
												? resourcebundle.getString(actionType.get(0)).toUpperCase()
														: (String) actionType.get(0));
								objAuditAction.setNreasoncode(objUsers.getNreasoncode());
								objAuditAction.setNusercode(objUsers.getNusercode());
								objAuditAction.setSreason(stringUtilityFunction.replaceQuote(objUsers.getSreason()));
								if (objUsers.getNtimezonecode() > 0) {
									objAuditAction.setDauditdate(dateTimeUtilityFunction.getUTCDateTime());

								} else {
									objAuditAction.setDauditdate(LocalDateTime.now().toInstant(ZoneOffset.UTC));
								}
								final LocalDateTime datetime = LocalDateTime.ofInstant(
										objAuditAction.getDauditdate().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC);
								final String formatted = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);

								objAuditAction.setSauditdate(LocalDateTime.parse(formatted, oldPattern).format(newPattern));
								objAuditAction.setSday(String.valueOf(datetime.getDayOfMonth()));
								objAuditAction.setSmonth(String.valueOf(datetime.getMonthValue()));
								objAuditAction.setSyear(String.valueOf(datetime.getYear()));
								objAuditAction.setShour(String.valueOf(destFormat.format(datetime)));
								objAuditAction.setNdeputyusercode(objUsers.getNdeputyusercode());
								objAuditAction.setNsitecode(objUsers.getNtranssitecode());
								objAuditAction.setStablename((String) lst.get(0).get("stablename"));
								objAuditAction.setNstatus((short) 1);
								lstAuditaction.add(objAuditAction);
								objAuditComments.setNauditcode(0);
								objAuditComments.setNstatus((short) 1);
								lstAuditComments.add(objAuditComments);
								// Data needed for Get start
								objJsonData.put("sauditdate", objAuditAction.getSauditdate());
								objJsonData.put("scomments", objAuditComments.getScomments());
								objJsonData.put("susername", objUsers.getSusername());
								objJsonData.put("suserrolename", objUsers.getSuserrolename());
								objJsonData.put("sdeputyusername", objUsers.getSdeputyusername());
								objJsonData.put("sdeputyuserrolename", objUsers.getSdeputyuserrolename());
								objJsonData.put("sactiontype", objAuditAction.getSactiontype());
								objJsonData.put("viewPeriod", objAuditAction.getShour());
								objJsonData.put("sformname", objUsers.getSformname());
								objJsonData.put("smodulename", objUsers.getSmodulename());
								objJsonData.put("sreason", stringUtilityFunction.replaceQuote(objAuditAction.getSreason()));
								// Data needed for get End
							}
						}

						break;

					default:
						break;
					}
				}
			}

			if (nFlag == 1) {
				fnJsonArrayAudit(jsonAuditnew, null, actionTypejson, inputmap, true, objUsers);
			}
			if (nFlag == 2) {
				fnJsonArrayAudit(jsonAuditold, jsonAuditnew, actionTypejson, inputmap, true, objUsers);

			}
		}
	}

	public ResponseEntity<Object> insertAuditAction(final UserInfo userInfo, final String sAuditAction,
			final String sComments, Map<String, Object> inputMap) throws Exception {
		LOGGER.info("insertAuditAction");

		final int nisauditebale =  auditActionValidation(userInfo);

		if(nisauditebale == Enumeration.TransactionStatus.NO.gettransactionstatus() || !userInfo.getSreason().isEmpty())
		{

			JSONObject objJsonData = new JSONObject();
			JSONObject jsoncomments = new JSONObject();
			Map<String, Object> sortedOldmap = new LinkedHashMap<>();
			// ALPD-1249
			final String sQuery = " lock  table lockaudit " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
			jdbcTemplate.execute(sQuery);

			String sourceFormat = "yyyy-MM-dd HH:mm:ss";
			final DateTimeFormatter oldPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			final DateTimeFormatter newPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

			final DateTimeFormatter destFormat = DateTimeFormatter.ofPattern("HH:00 (h a)");
			AuditAction objAuditAction = new AuditAction();
			AuditComments objAuditComments = new AuditComments();

			objAuditAction.setSauditaction(
					commonFunction.getMultilingualMessage(sAuditAction, userInfo.getSlanguagefilename()).toUpperCase());
			objAuditAction.setNformcode(userInfo.getNformcode());
			objAuditAction.setNuserrolecode(userInfo.getNuserrole());
			objAuditAction.setNdeputyrolecode(userInfo.getNdeputyuserrole());
			objAuditAction.setStransactionno((String) inputMap.get("sprimarykeyvalue").toString());
			objAuditAction.setNauditcode(0);
			objAuditAction.setNmodulecode(userInfo.getNmodulecode());
			objAuditAction.setSactiontype(userInfo.getSreason() == null || userInfo.getSreason().equals("")
					? commonFunction.getMultilingualMessage("IDS_SYSTEM", userInfo.getSlanguagefilename()).toUpperCase()
							: commonFunction.getMultilingualMessage("IDS_MANUAL", userInfo.getSlanguagefilename()).toUpperCase());

			objAuditAction.setNreasoncode(userInfo.getNreasoncode());
			objAuditAction.setNusercode(userInfo.getNusercode());
			objAuditAction.setSreason(stringUtilityFunction.replaceQuote(userInfo.getSreason()));

			if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				objAuditAction.setDauditdate(dateTimeUtilityFunction.getUTCDateTime());
			} else {
				objAuditAction.setDauditdate(LocalDateTime.now().toInstant(ZoneOffset.UTC));
			}
			final LocalDateTime datetime = LocalDateTime
					.ofInstant(objAuditAction.getDauditdate().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC);
			final String formatted = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);

			objAuditAction.setSauditdate(LocalDateTime.parse(formatted, oldPattern).format(newPattern));
			objAuditAction.setSday(
					String.valueOf(datetime.getDayOfMonth()).length() == 1 ? "0" + String.valueOf(datetime.getDayOfMonth())
					: String.valueOf(datetime.getDayOfMonth()));
			objAuditAction.setSmonth(
					String.valueOf(datetime.getMonthValue()).length() == 1 ? "0" + String.valueOf(datetime.getMonthValue())
					: String.valueOf(datetime.getMonthValue()));
			objAuditAction.setSyear(String.valueOf(datetime.getYear()));
			objAuditAction.setShour(String.valueOf(destFormat.format(datetime)));
			objAuditAction.setNdeputyusercode(userInfo.getNdeputyusercode());
			objAuditAction.setNsitecode(userInfo.getNtranssitecode());
			objAuditAction.setStablename(inputMap.containsKey("stablename") ? (String) inputMap.get("stablename") : "");
			objAuditAction.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
			objAuditComments.setNauditcode(0);
			objAuditComments.setScomments(sComments);
			objAuditComments.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

			final String getAuditLastRecord = "select nsequenceno from seqnoaudittrail where stablename = 'auditaction'"
											+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int lastValueFromAuditTable = (int) jdbcUtilityFunction.queryForObject(getAuditLastRecord, Integer.class,
					jdbcTemplate);
			// Data needed for Get start

			objJsonData.put("sauditdate", objAuditAction.getSauditdate());
			objJsonData.put("sauditaction", objAuditAction.getSauditaction());
			objJsonData.put("scomments", objAuditComments.getScomments());
			objJsonData.put("susername", userInfo.getSusername());
			objJsonData.put("suserrolename", userInfo.getSuserrolename());
			objJsonData.put("sdeputyusername", userInfo.getSdeputyusername());
			objJsonData.put("sdeputyuserrolename", userInfo.getSdeputyuserrolename());
			objJsonData.put("sactiontype", objAuditAction.getSactiontype());
			objJsonData.put("viewPeriod", objAuditAction.getShour());
			objJsonData.put("sformname",
					jdbcUtilityFunction.queryForObject(
							"select jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' from"
							+ "  qualisforms where nformcode=" + userInfo.getNformcode()
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									String.class, jdbcTemplate));
			objJsonData.put("smodulename",
					jdbcUtilityFunction.queryForObject(
							"select jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' from"
									+ "  qualismodule where nmodulecode=" + userInfo.getNmodulecode()
									+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									String.class, jdbcTemplate));
			objJsonData.put("sreason", stringUtilityFunction.replaceQuote(objAuditAction.getSreason()));
			objJsonData.put("spredefinedreason", userInfo.getSpredefinedreason());
			objJsonData.put("jsoncomments", objAuditComments.getScomments());
			objJsonData.put("sjsoncomments", objAuditComments.getScomments());
			objJsonData.put("slanguagename", userInfo.getSlanguagename());
			// ALPD-971
			if (inputMap.containsKey("isSync")) {

				sortedOldmap.put(commonFunction.getMultilingualMessage("IDS_SYNCAUTOMED", userInfo.getSlanguagefilename()),
						commonFunction.getMultilingualMessage(inputMap.get("dataStatus").toString(),
								userInfo.getSlanguagefilename()));

			} else {
				sortedOldmap.put(commonFunction.getMultilingualMessage("IDS_USERNAME", userInfo.getSlanguagefilename()),
						userInfo.getSfirstname() + userInfo.getSlastname());
				sortedOldmap.put(commonFunction.getMultilingualMessage("IDS_LOGINID", userInfo.getSlanguagefilename()),
						userInfo.getSloginid());
			}

			if (inputMap.containsKey("additionalFields")) {
				sortedOldmap.putAll((Map<String, Object>) inputMap.get("additionalFields"));
			}

			if (sAuditAction.equals("IDS_IDLETIMELOGIN") || sAuditAction.equals("IDS_IDLETIMELOCK")
					|| sAuditAction.equals("IDS_SESSIONEXPIRED")) {
				sortedOldmap.put("User Role", userInfo.getSuserrolename());
			}
			jsoncomments.put("data", new JSONObject(sortedOldmap));
			JSONArray array = new JSONArray(sortedOldmap.keySet().toArray());
			jsoncomments.put("keys", array);
			// Data needed for get End
			StringBuilder sb = new StringBuilder();
			lastValueFromAuditTable++;
			objJsonData.put("nauditcode", lastValueFromAuditTable);
		
			sb.append("insert into auditaction (nauditcode,nusercode,nuserrolecode,ndeputyusercode "
					+ ",ndeputyrolecode,nformcode,nmodulecode, nreasoncode "
					+ ",stablename,dauditdate,sactiontype,sauditaction, "
					+ "stransactionno,sreason,nsitecode,nstatus,sday,smonth,syear,sauditdate,shour) values("
					+ lastValueFromAuditTable + "," + objAuditAction.getNusercode() + ","
					+ objAuditAction.getNuserrolecode() + "," + objAuditAction.getNdeputyusercode() + ","
					+ objAuditAction.getNdeputyrolecode() + "," + objAuditAction.getNformcode() + ","
					+ objAuditAction.getNmodulecode() + "," + objAuditAction.getNreasoncode() + ",'"
					+ objAuditAction.getStablename() + "','" + objAuditAction.getDauditdate() + "','"
					+ objAuditAction.getSactiontype() + "','" + objAuditAction.getSauditaction() + "','"
					+ objAuditAction.getStransactionno() + "','"
					+ stringUtilityFunction.replaceQuote(objAuditAction.getSreason()) + "'," + objAuditAction.getNsitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'" + objAuditAction.getSday()
					+ "','" + objAuditAction.getSmonth() + "'" + ",'" + objAuditAction.getSyear() + "','"
					+ objAuditAction.getSauditdate() + "','" + objAuditAction.getShour() + "');");
			jdbcTemplate.batchUpdate(sb.toString());
			sb.delete(0, sb.length());
			
			sb.append(" insert into auditcomments (nauditcode ,scomments  ,jsondata,jsoncomments,nstatus) values("
					+ lastValueFromAuditTable + ",'" + stringUtilityFunction.replaceQuote(objAuditComments.getScomments())
					+ "','" + stringUtilityFunction.replaceQuote(objJsonData.toString()) + "','"
					+ stringUtilityFunction.replaceQuote(jsoncomments.toString()) + "',"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");
			jdbcTemplate.batchUpdate(sb.toString());
			sb.delete(0, sb.length());
			sb.append("update seqnoaudittrail set nsequenceno = nsequenceno +1 where stablename ='auditaction';");
			
			jdbcTemplate.batchUpdate(sb.toString());
			
			return null;
		}
		return null;
	}
}

