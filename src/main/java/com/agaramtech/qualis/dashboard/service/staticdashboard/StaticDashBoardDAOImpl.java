package com.agaramtech.qualis.dashboard.service.staticdashboard;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.dashboard.model.StaticDashBoardDetails;
import com.agaramtech.qualis.dashboard.model.StaticDashBoardMaster;
import com.agaramtech.qualis.dashboard.model.StaticDashBoardParameter;
import com.agaramtech.qualis.dashboard.model.StaticDashBoardType;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StaticDashBoardDAOImpl implements StaticDashBoardDAO {

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;

	private static final Logger LOGGER = LoggerFactory.getLogger(StaticDashBoardDAOImpl.class);

	@Override
	public ResponseEntity<Object> getListStaticDashBoard(UserInfo userInfo) throws Exception {

		LOGGER.info("getListStaticDashBoard service invoked");
		final Map<String, Object> objMap = new LinkedHashMap<>();
		final Map<String, Object> objMapFinal = new LinkedHashMap<>();
		final List<String> lscolunms = new ArrayList<>();
		final List<String> lscolunmsdetails = new ArrayList<>();
		String strQuery = " select nstaticdashboardmastercode, sdescription, sstaticdashboardmastername, nsitecode, nstatus from staticdashboardmaster where  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nstaticdashboardmastercode ";
		List<StaticDashBoardMaster> lstStaticDashBoardMaster = (List<StaticDashBoardMaster>) jdbcTemplate
				.query(strQuery, new StaticDashBoardMaster());
		if (!lstStaticDashBoardMaster.isEmpty()) {
			final ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			lscolunms.add("sstaticdashboardmastername");
			lstStaticDashBoardMaster = mapper.convertValue(commonFunction
					.getMultilingualMessageList(lstStaticDashBoardMaster, lscolunms, userInfo.getSlanguagefilename()),
					new TypeReference<List<StaticDashBoardMaster>>() {
					});
			objMap.put("staticDashBoardMaster", lstStaticDashBoardMaster);
			objMap.put("selectedStaticDashBoardMaster", lstStaticDashBoardMaster.get(0));
			String validateStr = " and nisshowprimaryserver = "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " ";
			if (userInfo.getNissyncserver() == Enumeration.TransactionStatus.YES.gettransactionstatus()
					|| userInfo.getNusercode() == -1) {
				validateStr = " and nisshowprimaryserver = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ "";
			}
			for (int i = 0; i < lstStaticDashBoardMaster.size(); i++) {
				strQuery = " select nstaticdashboardcode,nstaticdashboardmastercode,sdescription,sstaticdashboardname from staticdashboarddetails where nstaticdashboardmastercode = "
						+ lstStaticDashBoardMaster.get(i).getNstaticdashboardmastercode() + " " + validateStr
						+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nstaticdashboardcode ";
				List<StaticDashBoardDetails> lstStaticDashBoardDetails = (List<StaticDashBoardDetails>) jdbcTemplate
						.query(strQuery, new StaticDashBoardDetails());
				if (!lstStaticDashBoardDetails.isEmpty()) {
					lscolunmsdetails.add("sstaticdashboardname");
					lstStaticDashBoardDetails = mapper.convertValue(
							commonFunction.getMultilingualMessageList(lstStaticDashBoardDetails, lscolunmsdetails,
									userInfo.getSlanguagefilename()),
							new TypeReference<List<StaticDashBoardDetails>>() {
							});
				}
				lstStaticDashBoardMaster.get(i).setStaticDBDetailsList(lstStaticDashBoardDetails);
				if (i == 0) {
					if (!lstStaticDashBoardDetails.isEmpty()) {
						objMapFinal.putAll((Map<String, Object>) getStaticDashBoard(userInfo,
								lstStaticDashBoardDetails.get(0).getNstaticdashboardcode(), 0, null).getBody());
						final StaticDashBoardDetails staticDashBoardDetails = lstStaticDashBoardDetails.get(0);
						objMap.put("selectedDashBoardDetail", staticDashBoardDetails);
					}
				}
			}

		}

		objMapFinal.putAll(objMap);
		return new ResponseEntity<Object>(objMapFinal, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Map<String,Object>> getStaticDashBoard(UserInfo userInfo, int nstaticDashBoardCode,
			int nstaticDashBoardMasterCode, Map<String, Object> sparamValue) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> objMapReturn = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstData = new ArrayList<Map<String, Object>>();
		int chartTypeCode = 0;
		String strQuery = " select * from staticdashboardtype where nstaticdashboardcode =  " + nstaticDashBoardCode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<StaticDashBoardType> lstStaticDashBoardType = (List<StaticDashBoardType>) jdbcTemplate.query(strQuery,
				new StaticDashBoardType());
		if (lstStaticDashBoardType != null && lstStaticDashBoardType.size() > 0) {
			for (int i = 0; i < lstStaticDashBoardType.size(); i++) {
				String sQuery = "";
				if (lstStaticDashBoardType.get(i).getNcharttypecode() == Enumeration.ChartType.DONUT.getChartType()
						|| lstStaticDashBoardType.get(i).getNcharttypecode() == Enumeration.ChartType.PIE
								.getChartType()) {
					sQuery = lstStaticDashBoardType.get(i).getSquery();
					List<StaticDashBoardParameter> lstStaticDBParams = getStaticDashBoardParameter(
							lstStaticDashBoardType.get(i).getNstaticdashboardtypecode());
					String sOldKey = "";
					int firstIndex = 0;
					int lastIndex = 0;
					String sFieldKey = "";
					String sTempData = "";
					chartTypeCode = lstStaticDashBoardType.get(i).getNcharttypecode();
					if (lstStaticDBParams != null && lstStaticDBParams.size() > 0) {
						objMapReturn.put("filterParams", lstStaticDBParams);
						while (sQuery.contains("P$")) {
							firstIndex = sQuery.indexOf("P$");
							lastIndex = sQuery.indexOf("$P");
							sFieldKey = sQuery.substring(firstIndex + 2, lastIndex);
							if (!sFieldKey.equals(sOldKey)) {
								final String sTempFieldKey = sFieldKey;
								List<StaticDashBoardParameter> lstStaticDBParams1 = lstStaticDBParams.stream()
										.filter(param -> param.getSfieldname().equals(sTempFieldKey))
										.collect(Collectors.toList());
								String dateValue = "";
								if (lstStaticDBParams1.isEmpty() || lstStaticDBParams1.size() == 0) {
									Map<String, Object> mapUserInfo = mapper.convertValue(userInfo, Map.class);
									if (mapUserInfo.containsKey(sTempFieldKey)) {
										if (mapUserInfo.get(sTempFieldKey).getClass().getName() == "java.lang.Short") {
											sTempData = String.valueOf(mapUserInfo.get(sTempFieldKey));
											objMapReturn.put(sTempFieldKey, sTempData);
										}
									} else {
										sTempData = "null";
									}
								} else {
									if (lstStaticDBParams1.get(0)
											.getNdesigncomponentcode() == Enumeration.DesignComponent.DATEFEILD
													.gettype()) {
										String formattedString = "";
										final DateTimeFormatter formatter = DateTimeFormatter
												.ofPattern(userInfo.getSdatetimeformat());
										final DateTimeFormatter formatter1 = DateTimeFormatter
												.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
										Instant instantDate = null;
										if (sparamValue != null) {
											if (sparamValue.containsKey(sTempFieldKey)) {
												instantDate = dateUtilityFunction.convertStringDateToUTC(
														(String) sparamValue.get(sFieldKey), userInfo, true);
												dateValue = dateUtilityFunction.instantDateToString(instantDate);
												final Instant instantDate1 = instantDate.truncatedTo(ChronoUnit.DAYS);
												if (sTempFieldKey.equalsIgnoreCase("fromdate")) {
													instantDate = instantDate1.truncatedTo(ChronoUnit.DAYS);
												} else if (sTempFieldKey.equalsIgnoreCase("todate")) {
													instantDate = instantDate1.plus(23, ChronoUnit.HOURS)
															.plus(59, ChronoUnit.MINUTES).plus(59, ChronoUnit.SECONDS);
												}
											}

										} else {
											instantDate = dateUtilityFunction.getUTCDateTime();
											final Instant instantDate1 = instantDate.truncatedTo(ChronoUnit.DAYS);
											if (lstStaticDBParams1.get(0).getNdays() >= 0) {
												if (sTempFieldKey.equalsIgnoreCase("fromdate")) {
													instantDate = instantDate1
															.plus(lstStaticDBParams1.get(0).getNdays(), ChronoUnit.DAYS)
															.truncatedTo(ChronoUnit.DAYS);
												} else if (sTempFieldKey.equalsIgnoreCase("todate")) {
													instantDate = instantDate1
															.minus(Math.abs(lstStaticDBParams1.get(0).getNdays()),
																	ChronoUnit.DAYS)
															.plus(23, ChronoUnit.HOURS).plus(59, ChronoUnit.MINUTES)
															.plus(59, ChronoUnit.SECONDS);
												} else {
													instantDate1.plus(lstStaticDBParams1.get(0).getNdays(),
															ChronoUnit.DAYS);
												}
											} else {
												if (sTempFieldKey.equalsIgnoreCase("fromdate")) {
													instantDate = instantDate1
															.minus(Math.abs(lstStaticDBParams1.get(0).getNdays()),
																	ChronoUnit.DAYS)
															.truncatedTo(ChronoUnit.DAYS);
												} else if (sTempFieldKey.equalsIgnoreCase("todate")) {
													instantDate = instantDate1
															.minus(Math.abs(lstStaticDBParams1.get(0).getNdays()),
																	ChronoUnit.DAYS)
															.plus(23, ChronoUnit.HOURS).plus(59, ChronoUnit.MINUTES)
															.plus(59, ChronoUnit.SECONDS);
												} else {
													instantDate = instantDate1.minus(
															Math.abs(lstStaticDBParams1.get(0).getNdays()),
															ChronoUnit.DAYS);
												}
											}
										}

										dateValue = dateUtilityFunction.instantDateToString(instantDate);
										// for UTC or Local send to frot end
										final LocalDateTime ldt = LocalDateTime.parse(dateValue, formatter1);

										if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES
												.gettransactionstatus()) {
											final ZonedDateTime zonedDateTime = ZonedDateTime.of(ldt,
													ZoneId.of(userInfo.getStimezoneid()));
											formattedString = zonedDateTime.format(formatter);
										} else {
											formattedString = formatter1.format(ldt);
										}
										objMapReturn.put(lstStaticDBParams1.get(0).getSfieldname(), formattedString);
										sTempData = formattedString;
									} else {
										Map<String, Object> mapUserInfo = mapper.convertValue(userInfo, Map.class);
										if (mapUserInfo.containsKey(sTempFieldKey)) {
											if (mapUserInfo.get(sTempFieldKey).getClass()
													.getName() == "java.lang.Short") {
												sTempData = String.valueOf(mapUserInfo.get(sTempFieldKey));
												objMapReturn.put(lstStaticDBParams1.get(0).getSfieldname(), sTempData);
											}
										} else {
											sTempData = "null";
										}
									}

								}
								StringBuffer sb = new StringBuffer(sQuery);
								sb.replace(firstIndex, lastIndex + 2, sTempData);
								sQuery = sb.toString();
								sOldKey = sFieldKey;
							}

						}
					} else {
						StringBuffer sb = new StringBuffer();
						sb.delete(0, sb.length());
						String sSQLQuery = sQuery;
						sb.append(sSQLQuery);
						while (sSQLQuery.contains("P$")) {
							int nStart = sSQLQuery.indexOf("P$");
							int nEnd = sSQLQuery.indexOf("$P");
							sFieldKey = sQuery.substring(nStart + 2, nEnd);
							Map<String, Object> mapUserInfo = mapper.convertValue(userInfo, Map.class);
							if (mapUserInfo.containsKey(sFieldKey)) {
								if ((sFieldKey).equals("slanguagetypecode")) {
									sTempData = String.valueOf(mapUserInfo.get(sFieldKey));
									sb.replace(nStart, nEnd + 2, sTempData);
								}
							}
							sSQLQuery = sb.toString();
						}
						sQuery = sSQLQuery;
					}

					LOGGER.info(sQuery);
					lstData.addAll(jdbcTemplate.queryForList(sQuery));

					List<Map<String, Object>> lstPropert = (List<Map<String, Object>>) getStaticChartPropTransaction(
							lstStaticDashBoardType.get(i).getNstaticdashboardtypecode());
					if (lstPropert != null && lstPropert.size() > 0) {
						String fieldCategory = "";
						String fieldValue = "";
						List<Map<String, Object>> lstPieChart = new ArrayList<Map<String, Object>>();
						for (int j = 0; j < lstPropert.size(); j++) {
							if (((String) lstPropert.get(j).get("schartpropertyname")).equals("field")) {

								fieldCategory = (String) lstPropert.get(j).get("schartpropvalue");
							} else if (((String) lstPropert.get(j).get("schartpropertyname")).equals("nameField")) {

								fieldValue = (String) lstPropert.get(j).get("schartpropvalue");
							}
						}
						if (lstData != null && lstData.size() > 0) {
							for (int j = 0; j < lstData.size(); j++) {
								Map<String, Object> objPieChartMap = new LinkedHashMap<String, Object>();
								objPieChartMap.put("categoryField", commonFunction.getMultilingualMessage(
										(String) lstData.get(j).get(fieldCategory), userInfo.getSlanguagefilename()));
								objPieChartMap.put("valueField", lstData.get(j).get(fieldValue));
								objPieChartMap.put("rowItem", lstData.get(j));
								lstPieChart.add(objPieChartMap);
							}
							objMapReturn.put("pieChartData", lstPieChart);
						}
					}
					break;
				} else {

				}
			}
			if (lstData != null && lstData.size() > 0) {
				objMapReturn.putAll((Map<String, Object>) getSelectionStaticDashBoard(userInfo, nstaticDashBoardCode,
						lstData.get(0)).getBody());
			}
		}
		objMapReturn.put("chartTypeCode", chartTypeCode);
		return new ResponseEntity<Map<String,Object>>(objMapReturn, HttpStatus.OK);
	}

	public List<Map<String,Object>> getStaticChartPropTransaction(int nstaticDashBoardCode) throws Exception {
		final String sChartProps = " select cp.nchartpropertycode,cp.schartpropertyname,sct.schartpropvalue, sct.nseries from ChartProperty cp,staticchartproptransaction sct, staticdashboardtype sdt where  "
				+ " cp.nchartpropertycode=sct.nchartpropertycode and sct.nstaticdashboardtypecode = sdt.nstaticdashboardtypecode and "
				+ " sdt.nstaticdashboardtypecode = " + nstaticDashBoardCode + " and sct.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sdt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by cp.nchartpropertycode,sct.nseries ";
		List<Map<String, Object>> lstPropert = jdbcTemplate.queryForList(sChartProps);
		return lstPropert;
	}

	public List<StaticDashBoardParameter> getStaticDashBoardParameter(int nstaticDashBoardTypeCode) throws Exception {
		final String strQuery = " select * from staticdashboardparameter where nstaticdashboardtypecode =  "
				+ nstaticDashBoardTypeCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nstaticdashboardparametercode";
		List<StaticDashBoardParameter> lstStaticDashBoardParameter = (List<StaticDashBoardParameter>) jdbcTemplate
				.query(strQuery, new StaticDashBoardParameter());
		return lstStaticDashBoardParameter;
	}

	@Override
	public ResponseEntity<Object> getSelectionStaticDashBoard(UserInfo userInfo, int nstaticDashBoardCode,
			Map<String, Object> sparamValue) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final ObjectMapper mapper = new ObjectMapper();
		String strQuery = " select * from staticdashboardtype where nstaticdashboardcode =  " + nstaticDashBoardCode
				+ " and ncharttypecode != " + Enumeration.ChartType.DONUT.getChartType() + " and ncharttypecode != "
				+ Enumeration.ChartType.PIE.getChartType() + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<StaticDashBoardType> lstStaticDashBoardType = (List<StaticDashBoardType>) jdbcTemplate.query(strQuery,
				new StaticDashBoardType());
		if (lstStaticDashBoardType != null && lstStaticDashBoardType.size() > 0) {
			for (int i = 0; i < lstStaticDashBoardType.size(); i++) {
				String sQuery = "";
				sQuery = lstStaticDashBoardType.get(i).getSquery();
				List<StaticDashBoardParameter> lstStaticDBParams = getStaticDashBoardParameter(
						lstStaticDashBoardType.get(i).getNstaticdashboardtypecode());
				String sOldKey = "";
				int firstIndex = 0;
				int lastIndex = 0;
				String sFieldKey = "";
				String sTempData = "";
				if (lstStaticDBParams != null && lstStaticDBParams.size() > 0) {
					while (sQuery.contains("P$")) {
						firstIndex = sQuery.indexOf("P$");
						lastIndex = sQuery.indexOf("$P");
						sFieldKey = sQuery.substring(firstIndex + 2, lastIndex);
						if (!sFieldKey.equals(sOldKey)) {
							final String sTempFieldKey = sFieldKey;
							List<StaticDashBoardParameter> lstStaticDBParams1 = lstStaticDBParams.stream()
									.filter(param -> param.getSfieldname().equals(sTempFieldKey))
									.collect(Collectors.toList());
							if (lstStaticDBParams1.isEmpty() || lstStaticDBParams1.size() == 0) {
								Map<String, Object> mapUserInfo = mapper.convertValue(userInfo, Map.class);
								if (mapUserInfo.containsKey(sTempFieldKey)) {
									if (mapUserInfo.get(sTempFieldKey).getClass().getName() == "java.lang.Short") {
										sTempData = String.valueOf(mapUserInfo.get(sTempFieldKey));
									}
								} else {
									sTempData = "null";
								}
							} else {
								String dateValue = "";
								if (sparamValue != null) {
									if (sparamValue.containsKey(sTempFieldKey)) {
										if (sparamValue
												.get(lstStaticDBParams1.get(0).getSfieldname()) instanceof Integer) {
											int value = (int) sparamValue
													.get(lstStaticDBParams1.get(0).getSfieldname());
											sTempData = Integer.toString(value);
										} else {
											sTempData = (String) sparamValue
													.get(lstStaticDBParams1.get(0).getSfieldname());
										}
									} else {
										if (lstStaticDBParams1.get(0)
												.getNdesigncomponentcode() == Enumeration.DesignComponent.DATEFEILD
														.gettype()) {
											Instant instantDate = dateUtilityFunction.getUTCDateTime();
											if (lstStaticDBParams1.get(0).getNdays() >= 0) {
												instantDate = instantDate.plus(lstStaticDBParams1.get(0).getNdays(),
														ChronoUnit.DAYS);
											} else {
												instantDate = instantDate.minus(
														Math.abs(lstStaticDBParams1.get(0).getNdays()),
														ChronoUnit.DAYS);
											}
											dateValue = dateUtilityFunction.instantDateToString(instantDate);
											sTempData = dateValue;
										}
									}
								} else {
									Instant instantDate = dateUtilityFunction.getUTCDateTime();
									if (lstStaticDBParams1.get(0).getNdays() >= 0) {
										instantDate = instantDate.plus(lstStaticDBParams1.get(0).getNdays(),
												ChronoUnit.DAYS);
									} else {
										instantDate = instantDate.minus(Math.abs(lstStaticDBParams1.get(0).getNdays()),
												ChronoUnit.DAYS);
									}
									dateValue = dateUtilityFunction.instantDateToString(instantDate);
									sTempData = dateValue;
								}
							}
							StringBuffer sb = new StringBuffer(sQuery);
							sb.replace(firstIndex, lastIndex + 2, sTempData);
							sQuery = sb.toString();
							sOldKey = sFieldKey;
						}
					}
				} else {
					StringBuffer sb = new StringBuffer();
					sb.delete(0, sb.length());
					String sSQLQuery = sQuery;
					sb.append(sSQLQuery);
					while (sSQLQuery.contains("P$")) {
						int nStart = sSQLQuery.indexOf("P$");
						int nEnd = sSQLQuery.indexOf("$P");

						sFieldKey = sQuery.substring(nStart + 2, nEnd);
						Map<String, Object> mapUserInfo = mapper.convertValue(userInfo, Map.class);
						if (mapUserInfo.containsKey(sFieldKey)) {
							if ((sFieldKey).equals("slanguagetypecode")) {
								sTempData = String.valueOf(mapUserInfo.get(sFieldKey));
								sb.replace(nStart, nEnd + 2, sTempData);
							}
						}
						sSQLQuery = sb.toString();
						sQuery = sSQLQuery;
					}
					sQuery = sSQLQuery;
				}
				LOGGER.info(sQuery);
				List<Map<String, Object>> lstData = jdbcTemplate.queryForList(sQuery);
				if (lstStaticDashBoardType.get(i).getNcharttypecode() == Enumeration.ChartType.GRID.getChartType()) {
					objMap.put("gridData", lstData);
					objMap.put("selectedCategoryField", lstStaticDBParams.get(0).getSdisplayname());
				} else {
					List<Map<String, Object>> lstPropert = (List<Map<String, Object>>) getStaticChartPropTransaction(
							lstStaticDashBoardType.get(i).getNstaticdashboardtypecode());
					if (lstData != null && lstData.size() > 0) {
						String xPropField = "";
						String yPropField = "";
						String colorPropField = "";
						String categoryField = "categoryField";
						String sizeField = "sizeField";
						List<Map<String, Object>> lstCategoryField = new ArrayList<Map<String, Object>>();
						List<Map<String, Object>> lstSizeField = new ArrayList<Map<String, Object>>();
						if (lstStaticDashBoardType.get(i).getNcharttypecode() == Enumeration.ChartType.BUBBLE
								.getChartType()) {
							xPropField = "xFieldBubble";
							yPropField = "yFieldBubble";
							colorPropField = "colorFill";
							lstCategoryField = lstPropert.stream()
									.filter(yField -> ((String) yField.get("schartpropertyname")).equals(categoryField))
									.collect(Collectors.toList());

							lstSizeField = lstPropert.stream()
									.filter(yField -> ((String) yField.get("schartpropertyname")).equals(sizeField))
									.collect(Collectors.toList());
						} else {
							xPropField = "xField";
							yPropField = "yField";
							colorPropField = "areaFill";
						}
						final String xPropFieldfinal = xPropField;
						final String yPropFieldfinal = yPropField;
						final String colorPropFieldfinal = colorPropField;

						String xFieldName = lstPropert.stream()
								.map(xField -> ((String) xField.get("schartpropertyname")).equals(xPropFieldfinal)
										? (String) xField.get("schartpropvalue")
										: "")
								.collect(Collectors.toList()).get(0);

						List<Object> category = lstData.stream().map(data -> data.get(xFieldName))
								.collect(Collectors.toList());

						List<Map<String, Object>> ySeries = lstPropert.stream()
								.filter(yField -> ((String) yField.get("schartpropertyname")).equals(yPropFieldfinal))
								.collect(Collectors.toList());

						List<Map<String, Object>> areaFill = lstPropert.stream().filter(
								yField -> ((String) yField.get("schartpropertyname")).equals(colorPropFieldfinal))
								.collect(Collectors.toList());

						List<Map<String, Object>> lstChartSeries = new ArrayList<Map<String, Object>>();
						List<Map<String, Object>> lstChartSeriesBubble = new ArrayList<Map<String, Object>>();

						if (lstStaticDashBoardType.get(i).getNcharttypecode() != Enumeration.ChartType.BUBBLE
								.getChartType()) {
							for (int j = 0; j < ySeries.size(); j++) {
								final Map<String, Object> objMapSeries = new HashMap<String, Object>();

								String yFieldName = (String) ySeries.get(j).get("schartpropvalue");

								objMapSeries.put("yField", commonFunction.getMultilingualMessage(yFieldName,
										userInfo.getSlanguagefilename()));
								objMapSeries.put("colors", areaFill.get(j).get("schartpropvalue"));
								objMapSeries.put("Series", lstData.stream().map(data -> data.get(yFieldName))
										.collect(Collectors.toList()));

								lstChartSeries.add(objMapSeries);
							}
							objMap.put("xSeries", category);
							objMap.put("ySeries", lstChartSeries);
						} else {

							for (int k = 0; k < ySeries.size(); k++) {
								final Map<String, Object> objMapSeries = new HashMap<String, Object>();

								String yFieldName = (String) ySeries.get(k).get("schartpropvalue");
								String sizeFieldNameForLegend = commonFunction.getMultilingualMessage(
										(String) lstSizeField.get(k).get("schartpropvalue"),
										userInfo.getSlanguagefilename());
								String sizeFieldName = (String) lstSizeField.get(k).get("schartpropvalue");
								String categoryFieldName = (String) lstCategoryField.get(k).get("schartpropvalue");

								objMapSeries.put("colorFill", areaFill.get(k).get("schartpropvalue"));

								objMapSeries.put("ySeriesBubble", yFieldName);
								objMapSeries.put("sizeSeriesBubble", sizeFieldName);
								objMapSeries.put("sizeSeriesBubbleForLegend", sizeFieldNameForLegend);
								objMapSeries.put("categorySeriesBubble", categoryFieldName);
								objMapSeries.put("xFieldBubble", xFieldName);

								lstChartSeriesBubble.add(objMapSeries);
							}
							objMap.put("bubbleSeries", lstChartSeriesBubble);
						}
						objMap.put("selectionChartType", lstStaticDashBoardType.get(i).getNcharttypecode());
						objMap.put("chartData", lstData);
					}
				}
			}
		}
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}
}
