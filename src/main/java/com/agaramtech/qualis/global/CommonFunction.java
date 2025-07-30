package com.agaramtech.qualis.global;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class holds general functions that can be used globally throughout the
 * application.
 * 
 */
//@Lazy
//@Transactional(rollbackFor = Exception.class)
@Component
//@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CommonFunction {

	/**
	 * This method is used to retrieve multilingual text value from the appropriate
	 * message properties file based on the specified language for the specified key
	 * string
	 * 
	 * @param multilingualKey [String] key name for which language specific text has
	 *                        to be fetched
	 * @param sfileName       [String] name of resource file from where multilingual
	 *                        text has to be fetched
	 * @return string message from multilingual properties
	 * @throws Exception
	 * 
	 *                   Added by ATE153 - 27/06/2020
	 */
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonFunction.class);
	
	private final DateTimeUtilityFunction dateUtilityFunction ;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param DateTimeUtilityFunction dateUtilityFunction
	 */
	public CommonFunction(DateTimeUtilityFunction dateUtilityFunction) {
		super();
		this.dateUtilityFunction = dateUtilityFunction;
	}
	
	
	public ResourceBundle getResourceBundle(final String slanguageFileName, final boolean bneedFrontSeparator) throws Exception{
		
		String pathString = Enumeration.Path.PROPERTIES_FILE.getPath() + slanguageFileName + ".properties";
					
		if (bneedFrontSeparator)
		{
			pathString  = "/" + pathString;
		}
	//	LOGGER.info("pathString = " + pathString);
		
		final InputStream stream = getClass().getClassLoader().getResourceAsStream(pathString);
	//	LOGGER.info("Stream = " + stream);
		
		//below will not work in local -> will output null ->
//		InputStream stream1 = getClass().getClassLoader().getResourceAsStream(pathString);
//		LOGGER.info("Stream1 = " + stream1);
		
//		ResourceBundle resourceBundle = new PropertyResourceBundle(
//				new InputStreamReader(getClass().getClassLoader()
//								.getResourceAsStream("/"+Enumeration.Path.PROPERTIES_FILE.getPath() 
//								+ userInfo.getSlanguagefilename() + ".properties"),
//						StandardCharsets.UTF_8));
		
		final ResourceBundle resourceBundle = new PropertyResourceBundle(new InputStreamReader(stream,"UTF-8"));
	//	LOGGER.info("ResourceBundle = " + resourceBundle);
		
		return resourceBundle;
	}
	
	
	public String getMultilingualMessage(final String multilingualKey, final String slanguagefileName)
			throws Exception {
		
	
		final ResourceBundle resourcebundle = getResourceBundle(slanguagefileName, false);
		
//		LOGGER.info(multilingualKey!=null ? multilingualKey:"empty key");
//		LOGGER.info("Response:"+ resourcebundle !=null 
//							?  resourcebundle.containsKey(multilingualKey) ? resourcebundle.getString(multilingualKey) :"false"
//							:"There is no key in properties file");
		final String mulltilingualData = (multilingualKey != null && resourcebundle.containsKey(multilingualKey))
				? resourcebundle.getString(multilingualKey)
				: multilingualKey;
		return mulltilingualData;
	}

	/**
	 * This method is used to retrieve multilingual text for the specified fields in
	 * the list of objects from the resource file.
	 * 
	 * @param List             [Object] It should contain multilingual field(s).
	 * @param columnNames      [String] It should be comma separated.
	 * @param languageFileName [String] name of resource file from where
	 *                         multilingual text has to be fetched
	 * @return List<?> List of items returned as multilingual text.
	 * @throws Exception
	 * 
	 *                   Added by ATE141 - 25/09/2020
	 */
	public List<?> getMultilingualMessageList(final List<?> list, final List<String> columnList,
			final String languageFileName) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
//		ResourceBundle resourcebundle = new PropertyResourceBundle(new InputStreamReader(
//				getClass().getClassLoader().getResourceAsStream(Enumeration.Path.PROPERTIES_FILE.getPath() + languageFileName + ".properties"),
//				"UTF-8"));
		
		final ResourceBundle resourcebundle = getResourceBundle(languageFileName, false);
		
		final List<Map<String, Object>> replaceMap = objMapper.convertValue(list,
				new TypeReference<List<Map<String, Object>>>() {
				});

		replaceMap.stream().map(item -> {
			try {
				for (int i = 0; i < columnList.size(); i++) {
					// System.out.println("item.get(columnList.get(i).trim()):"+ columnList.get(i) +
					// " :"+ item.get(columnList.get(i).trim()));
					item.replace(columnList.get(i).trim(),
							resourcebundle.containsKey((String) item.get(columnList.get(i).trim()))
									? resourcebundle.getString((String) item.get(columnList.get(i).trim()))
									: (String) item.get(columnList.get(i).trim()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return item;
		}).collect(Collectors.toList());
		return replaceMap;
	}

	public List<?> getMultilingualMessageMultiList(List<?> list, List<String> columnList, String languageFileName,
			int nFlag) throws Exception {
		List<Map<String, Object>> replaceMap = new ArrayList<>();
		List<Object> replaceMap1 = new ArrayList<>();

		ObjectMapper objMapper = new ObjectMapper();
//		ResourceBundle resourcebundle = new PropertyResourceBundle(new InputStreamReader(
//				getClass().getClassLoader().getResourceAsStream(Enumeration.Path.PROPERTIES_FILE.getPath() + languageFileName + ".properties"),
//				"UTF-8"));

		final ResourceBundle resourcebundle = getResourceBundle(languageFileName, false);
				
		switch (nFlag) {
		case 1:
			for (int k = 0; k < list.size(); k++) {
				replaceMap = objMapper.convertValue(list.get(k), new TypeReference<List<Map<String, Object>>>() {
				});
				if (replaceMap.size() > 0) {
					for (String column : columnList) {
						if (replaceMap.get(0).containsKey(column)) {
							try {
								replaceMap.stream().map(objmap -> {
									objmap.replace(column,
											objmap.get(column) != null
													&& resourcebundle.containsKey((String) objmap.get(column))
															? resourcebundle.getString((String) objmap.get(column))
															: (String) objmap.get(column));

									return objmap;
								}).collect(Collectors.toList());
								replaceMap1.add(replaceMap);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					replaceMap1.add(replaceMap);
				}
			}

			break;

		case 2:
			for (int k = 0; k < list.size(); k++) {
				replaceMap = objMapper.convertValue(list.get(k), new TypeReference<List<Map<String, Object>>>() {
				});
				final int s = k;
				if (replaceMap.size() > 0) {
					if (s < columnList.size() && columnList.get(k) != "" && columnList.get(k) != null) {
						if (!columnList.get(k).contains(",")) {
							if (replaceMap.get(0).containsKey(columnList.get(k))) {
								try {
									replaceMap.stream().map(objmap -> {
										objmap.replace(columnList.get(s),
												objmap.get(columnList.get(s)) != null && resourcebundle
														.containsKey((String) objmap.get(columnList.get(s)))
																? resourcebundle.getString(
																		(String) objmap.get(columnList.get(s)))
																: (String) objmap.get(columnList.get(s)));

										return objmap;
									}).collect(Collectors.toList());
									replaceMap1.add(replaceMap);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						} else {
							List<String> lst = Arrays.asList(columnList.get(s).split(","));

							for (int i = 0; i < lst.size(); i++) {
								final int j = i;
								try {
									replaceMap.stream().map(objmap -> {
										objmap.replace(lst.get(j), objmap.get(lst.get(j)) != null
												&& resourcebundle.containsKey((String) objmap.get(lst.get(j)))
														? resourcebundle.getString((String) objmap.get(lst.get(j)))
														: (String) objmap.get(lst.get(j)));

										return objmap;
									}).collect(Collectors.toList());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							replaceMap1.add(replaceMap);
						}
					} else {
						replaceMap1.add(replaceMap);
					}
				} else {
					replaceMap1.add(replaceMap);
				}
			}
			break;

		default:
			break;
		}
		return replaceMap1;
	}


//
//	/**
//	 * 
//	 * @author ATE140
//	 *
//	 */
//	public String dateToStringBySite(final Date Date, UserInfo userinfo) throws Exception {
//		String sourceFormat = userinfo.getSsitedate();
//		final LocalDateTime ldt = LocalDateTime.ofInstant(Date.toInstant(), ZoneId.systemDefault());
//		final String formatted = DateTimeFormatter.ofPattern(sourceFormat).format(ldt);
//		return formatted;
//	}

	/**
	 * @param multilingualKeyList
	 * @param slanguagefileName
	 * @return
	 * @throws Exception
	 */
	public List<String> getMultilingualMultipleMessage(final List<String> multilingualKeyList,
			final String slanguageFileName) throws Exception {
		List<String> listOfMultilingual = new ArrayList<String>();
//		try {
//				ResourceBundle resourcebundle = new PropertyResourceBundle(new InputStreamReader(
//				getClass().getClassLoader().getResourceAsStream(Enumeration.Path.PROPERTIES_FILE.getPath() + slanguagefileName + ".properties"),
//				"UTF-8"));
				
				final ResourceBundle resourcebundle = getResourceBundle(slanguageFileName, false);
				//	LOGGER.info (resourcebundle != null ? resourcebundle.containsKey(multilingualKeyList.getFirst()):"ResourceBundle is null");
				//List<String> listOfMultilingual = new ArrayList<String>();
				if (multilingualKeyList != null && multilingualKeyList.size() > 0) {
					//LOGGER.debug(multilingualKeyList.getLast());
					//int i=0;
					for (String multiLingualKey : multilingualKeyList) {
						//LOGGER.debug(multiLingualKey);
						listOfMultilingual.add((multiLingualKey != null && resourcebundle.containsKey(multiLingualKey))
								? resourcebundle.getString(multiLingualKey)
								: multiLingualKey);				
						//LOGGER.debug(listOfMultilingual.get(i));
						//i++;
					}
				}
//		}
//		catch(Exception e) {
//			LOGGER.error(e.getMessage());
//		}
		return listOfMultilingual;
	}
//
//	public Instant getCurrentDateTime(final UserInfo userInfo) throws Exception {
//		Instant date = null;
//		if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
//			// date =
//			// LocalDateTime.now().atZone(ZoneId.of(userInfo.getStimezoneid())).toInstant()
//			// .truncatedTo(ChronoUnit.SECONDS);
//			date = Instant.now();
//		} else {
//			date = LocalDateTime.now().toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
//		}
//		return date;
//	}
	
	//This method was used in DynamicPreRegDesignDAOImpl
	public Map<String, Object> validateDynamicDateContraints(JSONObject jsonObj,
			final List<Map<String, Object>> dateContraint, final UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		boolean isValid = true;
		String conditionString = "";
		String returnString = "";
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		if (dateContraint != null) {
			for (Map<String, Object> objMap : dateContraint) {
				int constraint = (int) objMap.get("constraint");
				String sparentDate = jsonObj.has(objMap.get("parentdate").toString()) 
							? (String) jsonObj.get(objMap.get("parentdate").toString()) :"";
				String schildDate;
				if (objMap.get("childdate").toString().equals("utccurrentdate")) {
					schildDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("Z", "");
				} else {
					schildDate = (String) jsonObj.get(objMap.get("childdate").toString());
				}
				if (sparentDate != null && sparentDate != "" && schildDate != null && schildDate != "") {
					sparentDate = sparentDate.replace(" ", "T") + "Z";
					schildDate = schildDate.replace(" ", "T") + "Z";
					final Instant parentDate = Instant.parse(sparentDate);
					final Instant childDate = Instant.parse(schildDate);
					int value;
					if (constraint == Enumeration.Condition.LESSTHAN.getCondition()) {
						value = parentDate.compareTo(childDate);
						if (value >= 0) {
							isValid = false;
							conditionString = getMultilingualMessage("IDS_LESSTHAN", userInfo.getSlanguagefilename());
						}
					} else if (constraint == Enumeration.Condition.LESSTHANOREQUALS.getCondition()) {
						value = parentDate.compareTo(childDate);
						if (value > 0) {
							isValid = false;
							conditionString = getMultilingualMessage("IDS_LESSTHANOREQUALS",
									userInfo.getSlanguagefilename());
						}
					} else if (constraint == Enumeration.Condition.GREATERTHAN.getCondition()) {
						value = childDate.compareTo(parentDate);
						if (value >= 0) {
							isValid = false;
							conditionString = getMultilingualMessage("IDS_GREATERTHAN",
									userInfo.getSlanguagefilename());
						}
					} else if (constraint == Enumeration.Condition.GREATERTHANEQUALS.getCondition()) {
						value = childDate.compareTo(parentDate);
						if (value > 0) {
							conditionString = getMultilingualMessage("IDS_GREATERTHANOREQUALS",
									userInfo.getSlanguagefilename());
							isValid = false;
						}
					}
					if (!isValid) {
						String childDateLabel = (String) objMap.get("childdate");
						if (childDateLabel.equals("utccurrentdate")) {
							childDateLabel = getMultilingualMessage("IDS_CURRENTDATE", userInfo.getSlanguagefilename());
						}

						returnString = objMap.get("parentdate") + " " + conditionString + " " + childDateLabel;
						// objMap.get("childdate");
						returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), returnString);
						break;
					}
				} else {
					returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}
			}
		}
		return returnMap;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> replaceMultiLingualByRecord(List<Map<String, Object>> lsData, UserInfo userInfo)
			throws Exception {
		if (!lsData.isEmpty()) {
			ObjectMapper oMapper = new ObjectMapper();
			Map<String, Object> mapcolumnfileds = oMapper.convertValue(lsData.get(0), Map.class);
			List<String> multilingualColumn = new ArrayList<String>();
			List<String> dateColumn = new ArrayList<String>();
			for (Map.Entry<String, Object> entry : mapcolumnfileds.entrySet()) {
				if (entry.getValue() != null) {
					if (entry.getValue().getClass().getSimpleName().equalsIgnoreCase("String")
							&& entry.getValue().toString().length() > 4
							&& entry.getValue().toString().substring(0, 4).equals("IDS_")) {
						multilingualColumn.add(entry.getKey());
					}

					if (entry.getValue().getClass().getSimpleName().equalsIgnoreCase("DateTime")
							|| entry.getValue().getClass().getSimpleName().equalsIgnoreCase("Date")
							|| entry.getValue().getClass().getSimpleName().equalsIgnoreCase("TimeStamp")) {
						dateColumn.add(entry.getKey());
					}
				}
			}
			if (!dateColumn.isEmpty() && !multilingualColumn.isEmpty()) {
				lsData = oMapper.convertValue(dateUtilityFunction.convertDateTimeToString(lsData, dateColumn, userInfo),
						new TypeReference<List<Map<String, Object>>>() {
						});
				lsData = oMapper.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(lsData, dateColumn, null, userInfo, true, multilingualColumn, false),
						new TypeReference<List<Map<String, Object>>>() {
						});
			} else if (!dateColumn.isEmpty() && multilingualColumn.isEmpty()) {
				lsData = oMapper.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(lsData, dateColumn, null, userInfo, false, null, false),
						new TypeReference<List<Map<String, Object>>>() {
						});
			} else if (dateColumn.isEmpty() && !multilingualColumn.isEmpty()) {
				lsData = oMapper.convertValue(getMultilingualMessageList(lsData, multilingualColumn,
						userInfo.getSlanguagefilename()), new TypeReference<List<Map<String, Object>>>() {
						});
			}
		}
		return lsData;

	}

}
