package com.agaramtech.qualis.dashboard.service.dashboardtypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.dashboard.model.ChartPropTransaction;
import com.agaramtech.qualis.dashboard.model.ChartProperty;
import com.agaramtech.qualis.dashboard.model.ChartType;
import com.agaramtech.qualis.dashboard.model.DashBoardDesignConfig;
import com.agaramtech.qualis.dashboard.model.DashBoardHomePages;
import com.agaramtech.qualis.dashboard.model.DashBoardHomePriority;
import com.agaramtech.qualis.dashboard.model.DashBoardHomeTemplate;
import com.agaramtech.qualis.dashboard.model.DashBoardHomeTypes;
import com.agaramtech.qualis.dashboard.model.DashBoardParameterMapping;
import com.agaramtech.qualis.dashboard.model.DashBoardType;
import com.agaramtech.qualis.dashboard.model.DesignComponents;
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.dashboard.model.SeqNoDashBoardManagement;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.misrights.model.DashBoardHomeRights;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class DashBoardTypeDAOImpl implements DashBoardTypeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(DashBoardTypeDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getDashBoardTypes(final UserInfo userInfo, int nDashBoardTypeCode) throws Exception {
		final Map<String, Object> objMap = new LinkedHashMap<String, Object>();

		if (nDashBoardTypeCode == 0) {
			final List<DashBoardType> lstdashBoardType = getListDahsBoardTypes(userInfo);
			objMap.put("DashBoardTypes", lstdashBoardType);
			if (lstdashBoardType.size() > 0) {
				nDashBoardTypeCode = lstdashBoardType.get(lstdashBoardType.size() - 1).getNdashboardtypecode();
				objMap.put("selectedDashBoardTypes", lstdashBoardType.get(lstdashBoardType.size() - 1));
			} else {
				objMap.put("selectedDashBoardTypes", null);
			}
		} else {
			objMap.put("selectedDashBoardTypes", getDashBoardTypeByID(nDashBoardTypeCode, userInfo));
		}

		final List<DashBoardDesignConfig> lstDesignConfig = getSelectedDashBoardDesign(userInfo, nDashBoardTypeCode);

		objMap.put("selectedDesignConfig", lstDesignConfig);

		final List<DashBoardParameterMapping> mappingList = getDashBoardParameterMapping(nDashBoardTypeCode, userInfo);
		objMap.put("DashBoardParameterMapping", mappingList);

		objMap.put("DashboardDesignDefaultList", getDefaultValueList(lstDesignConfig, mappingList, userInfo));
		LOGGER.info("getDashBoardTypes -->");
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	private List<DashBoardDesignConfig> getDefaultValueList(final List<DashBoardDesignConfig> lstDesignConfig,
			final List<DashBoardParameterMapping> mappingList, final UserInfo userInfo) throws Exception {
		final List<String> fieldList = new ArrayList<String>();
		final StringBuffer queryBuffer = new StringBuffer();

		for (DashBoardDesignConfig designConfig : lstDesignConfig) {
			if (designConfig.getSdefaultvalue() != null && designConfig.getSdefaultvalue().trim().length() != 0) {

				final String sqlquery = designConfig.getSsqlquery();

				if (sqlquery.contains("where")) {
					queryBuffer.append(sqlquery + " and " + designConfig.getSvaluemember() + "='"
							+ designConfig.getSdefaultvalue() + "';");
				} else {
					queryBuffer.append(sqlquery + " where " + designConfig.getSvaluemember() + "='"
							+ designConfig.getSdefaultvalue() + "';");
				}

				int count = StringUtils.countMatches(sqlquery, "<@");
				int count1 = StringUtils.countMatches(sqlquery, "<#");
				if (count > 0 || count1 > 0) {

					for (DashBoardParameterMapping parameterMapping : mappingList) {
						if (parameterMapping.getNchilddashboarddesigncode() == designConfig.getNdashboarddesigncode()) {
							List<String> valueList = lstDesignConfig.stream()
									.filter(p -> p.getNdashboarddesigncode() == parameterMapping
											.getNparentdashboarddesigncode())
									.map(pm -> pm.getSdefaultvalue()).collect(Collectors.toList());
							String parameterName = "<@" + parameterMapping.getSfieldname() + "@>";
							if (sqlquery.contains(parameterName)) {
								queryBuffer.replace(queryBuffer.indexOf(parameterName),
										(queryBuffer.indexOf(parameterName) + parameterName.length()),
										"'" + valueList.get(0) + "'");
							} else if (sqlquery.contains("<#" + parameterMapping.getSfieldname() + "#>")) {
								parameterName = "<#" + parameterMapping.getSfieldname() + "#>";
								queryBuffer.replace(queryBuffer.indexOf(parameterName),
										(queryBuffer.indexOf(parameterName) + parameterName.length()),
										valueList.get(0));
							}

						}
					}
				}
				fieldList.add(designConfig.getSdisplaymember());
			}
		}

		final Map<String, Object> defaultObject = (Map<String, Object>) projectDAOSupport
				.getMutlipleEntityResultsUsingPlainSql(queryBuffer.toString(), jdbcTemplate, fieldList.toArray());
		final List<DashBoardDesignConfig> returnObject = new ArrayList<DashBoardDesignConfig>();
		for (DashBoardDesignConfig designConfig : lstDesignConfig) {
			if (designConfig.getSdisplaymember() != null) {
				List<Object> list = (List) defaultObject.get(designConfig.getSdisplaymember());
				if (list != null && list.size() > 0) {
					final Map<String, Object> defaultObject1 = (Map<String, Object>) list.get(0);
					String value = (String) defaultObject1.get(designConfig.getSdisplaymember());
					if (value != null && value.startsWith("IDS_")) {
						value = commonFunction.getMultilingualMessage(value, userInfo.getSlanguagefilename());
					}
					designConfig.setSdisplaymember(value);
					returnObject.add(designConfig);
				}
			}
		}

		return returnObject;
	}

	private List<DashBoardType> getListDahsBoardTypes(final UserInfo userInfo) throws Exception {
		final String strQuery = " select a.*, a.nquerycode as nsqlquerycode, coalesce(b.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',b.jsondata->'sdisplayname'->>'en-US') as schartname, c.ssqlqueryname from dashboardtype a, charttype b, sqlquery c where a.ncharttypecode = b.ncharttypecode  "
				+ " and a.nquerycode = c.nsqlquerycode and a.nstatus = b.nstatus and a.nstatus = c.nstatus  "
				+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.nsitecode = " + userInfo.getNmastersitecode() + " order by a.ndashboardtypecode ";

		return (List<DashBoardType>) jdbcTemplate.query(strQuery, new DashBoardType());
	}

	@Override
	public DashBoardType getDashBoardTypeByID(final int ndashBoardTypeCode, final UserInfo userInfo) throws Exception {
		final String strQuery = " select a.ndashboardtypecode, a.sdashboardtypename, a.nquerycode, a.nquerycode as nsqlquerycode,a.ncharttypecode, a.nseriesno, a.nsitecode, a.nstatus, coalesce(b.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',b.jsondata->'sdisplayname'->>'en-US') as schartname, c.ssqlqueryname from dashboardtype a, charttype b, sqlquery c where a.ncharttypecode = b.ncharttypecode  "
				+ " and a.nquerycode = c.nsqlquerycode and a.nstatus = b.nstatus and a.nstatus = c.nstatus  "
				+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.ndashboardtypecode = " + ndashBoardTypeCode;

		return (DashBoardType) jdbcUtilityFunction.queryForObject(strQuery, DashBoardType.class, jdbcTemplate);
	}

	private Map<String, Object> viewDashboardDesignConfigList(final List<DashBoardDesignConfig> lstDesignConfig,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> objSendMap = new LinkedHashMap<String, Object>();
		final StringBuffer queryBuffer = new StringBuffer();
		final List<String> fieldList = new ArrayList<String>();
		final Map<String, String> dateFieldMap = new HashMap<String, String>();
		final ObjectMapper objMapper = new ObjectMapper();
		List<DashBoardParameterMapping> mappingList = new ArrayList<>();
		if (lstDesignConfig.size() > 0) {
			mappingList = getDashBoardParameterMapping(lstDesignConfig.get(0).getNdashboardtypecode(), userInfo);
		}

		for (DashBoardDesignConfig designConfig : lstDesignConfig) {
			if (designConfig.getNdesigncomponentcode() == Enumeration.DesignComponent.DATEFEILD.gettype()) {

				Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo);// objGeneral.getUTCDateTime();
				if (designConfig.getNdays() >= 0) {
					instantDate = instantDate.plus(designConfig.getNdays(), ChronoUnit.DAYS);
				} else {
					instantDate = instantDate.minus(Math.abs(designConfig.getNdays()), ChronoUnit.DAYS);
				}

				final String stringDate = dateUtilityFunction.convertUTCToSiteTime(instantDate, userInfo);
				designConfig.setDataList(Arrays.asList(stringDate));
				dateFieldMap.put(designConfig.getSfieldname(), stringDate);
			}
		}

		for (DashBoardDesignConfig designConfig : lstDesignConfig) {
			if (designConfig.getNsqlquerycode() != -1) {
				if (designConfig.getSsqlquery().contains("<@") || designConfig.getSsqlquery().contains("<#")) {

					fieldList.add(designConfig.getSdisplayname());
					String sqlQuery = designConfig.getSsqlquery();

					for (Map.Entry<String, String> dateField : dateFieldMap.entrySet()) {
						if (sqlQuery.contains("<@" + dateField.getKey() + "@>")) {
							sqlQuery = sqlQuery.replace("<@" + dateField.getKey() + "@>",
									"'" + dateField.getValue() + "'");
						} else if (sqlQuery.contains("<#" + dateField.getKey() + "#>")) {
							sqlQuery = sqlQuery.replace("<#" + dateField.getKey() + "#>",
									"'" + dateField.getValue() + "'");
						}

					}

					for (DashBoardParameterMapping parameterMapping : mappingList) {
						if (parameterMapping.getNchilddashboarddesigncode() == designConfig.getNdashboarddesigncode()) {
							List<String> valueList = lstDesignConfig.stream()
									.filter(p -> p.getNdashboarddesigncode() == parameterMapping
											.getNparentdashboarddesigncode())
									.map(pm -> pm.getSdefaultvalue()).collect(Collectors.toList());
							String parameterName = "<@" + parameterMapping.getSfieldname() + "@>";

							final StringBuffer newBuffer = new StringBuffer();
							newBuffer.append(sqlQuery + ";");
							if (sqlQuery.indexOf(parameterName) != -1 && valueList.get(0) != null) {

								newBuffer.replace(newBuffer.indexOf(parameterName),
										(newBuffer.indexOf(parameterName) + parameterName.length()),
										"'" + valueList.get(0) + "'");
							} else if (sqlQuery.indexOf("<#" + parameterMapping.getSfieldname() + "#>") != -1
									&& valueList.get(0) != null) {
								parameterName = "<#" + parameterMapping.getSfieldname() + "#>";

								newBuffer.replace(newBuffer.indexOf(parameterName),
										(newBuffer.indexOf(parameterName) + parameterName.length()),
										"'" + valueList.get(0) + "'");
							}

							if (newBuffer.toString().contains("<@") || newBuffer.toString().contains("<#")) {
								fieldList.remove(designConfig.getSdisplayname());
							} else {
								queryBuffer.append(newBuffer.toString());
							}
						}
					}

				} else {
					queryBuffer.append(designConfig.getSsqlquery() + ";");
					fieldList.add(designConfig.getSdisplayname());
				}
			}
		}

		if (!queryBuffer.toString().contains("<@") && !queryBuffer.toString().contains("<#")) {

			final Map<String, Object> queryMap = (Map<String, Object>) projectDAOSupport
					.getMutlipleEntityResultsUsingPlainSql(queryBuffer.toString(), jdbcTemplate, fieldList.toArray());

			for (DashBoardDesignConfig designConfig : lstDesignConfig) {

				if (fieldList.contains(designConfig.getSdisplayname())) {
					List<?> list = (List<?>) queryMap.get(designConfig.getSdisplayname());

					if (list != null && list.size() > 0) {
						Map<String, Object> mapcolumnfileds = objMapper.convertValue(list.get(0), Map.class);
						List<String> multilingualColumnList = new ArrayList<String>();

						for (Map.Entry<String, Object> entry : mapcolumnfileds.entrySet()) {
							if (entry.getValue() != null) {
								if (entry.getValue().getClass().getSimpleName().equalsIgnoreCase("String")
										&& entry.getValue().toString().length() > 4
										&& entry.getValue().toString().substring(0, 4).equals("IDS_")) {
									multilingualColumnList.add(entry.getKey());
								}
							}
						}

						list = objMapper.convertValue(
								commonFunction.getMultilingualMessageList(list, multilingualColumnList,
										userInfo.getSlanguagefilename()),
								new TypeReference<List<Map<String, Object>>>() {
								});
					}
					if (list != null) {
						designConfig.setDataList(list);
					}
				}

				if (designConfig.getSdisplaymember() != null) {

					List<Object> list = (List) queryMap.get(designConfig.getSdisplaymember());
					if (list != null && list.size() > 0) {
						final Map<String, Object> defaultObject1 = (Map<String, Object>) list.get(0);
						String value = (String) defaultObject1.get(designConfig.getSdisplaymember());
						if (value.startsWith("IDS_")) {
							value = commonFunction.getMultilingualMessage(value, userInfo.getSlanguagefilename());
						}
						designConfig.setSdisplaymember(value);
					}
				}
			}
		}

		objSendMap.put("viewDashBoardDesignConfigList", lstDesignConfig);

		return objSendMap;

	}

	private DashBoardType getDashBoardTypeListByName(final String dashBoardTypeName, final int nmasterSiteCode)
			throws Exception {

		final String strQuery = "select ndashboardtypecode from dashboardtype where sdashboardtypename = N'"
				+ stringUtilityFunction.replaceQuote(dashBoardTypeName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;// users.getnmastersitecode();

		return (DashBoardType) jdbcUtilityFunction.queryForObject(strQuery, DashBoardType.class, jdbcTemplate);
	}

	@Override
	public List<ChartType> getChartTypes(UserInfo userInfo) throws Exception {

		final String strQuery = "select ncharttypecode, nseries, dmodifieddate, nsitecode, nstatus, coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as schartname from charttype where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ncharttypecode > 0"
				+ " and nsitecode = " + userInfo.getNmastersitecode();

		return (List<ChartType>) jdbcTemplate.query(strQuery, new ChartType());
	}

	@Override
	public List<SQLQuery> getSqlQueriesByChart(int nChartTypeCode, UserInfo userInfo) throws Exception {

		final String strQuery = " select a.* from sqlquery a, charttype b where a.ncharttypecode = b.ncharttypecode "			
							  + " and a.ncharttypecode = " + nChartTypeCode + ""
							  + " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
							  + "and a.nsitecode = " + userInfo.getNmastersitecode() ;

		return (List<SQLQuery>) jdbcTemplate.query(strQuery, new SQLQuery());
	}

	@Override
	public ResponseEntity<Object> createDashBoardTypes(DashBoardType dashBoardType,
			List<ChartPropTransaction> chartPropTransaction, UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedDashBoardTypeList = new ArrayList<>();

		final List<Object> finalAuditList = new ArrayList<>();

		final DashBoardType dashBoardTypeListByName = getDashBoardTypeListByName(dashBoardType.getSdashboardtypename(),
				userInfo.getNmastersitecode());
		List<DashBoardType> validDashboard = new ArrayList<>();
		validDashboard.add(dashBoardType);
		if (dashBoardTypeListByName == null) {
			dashBoardType.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
//			dashBoardType = (DashBoardType) jdbcUtilityFunction.saveBatchOfObjects((List<?>) dashBoardType,
//					SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");
			validDashboard = (List<DashBoardType>) jdbcUtilityFunction.saveBatchOfObjects(validDashboard,
					SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");
			final int ndashboard = dashBoardType.getNdashboardtypecode();
			Instant modifieddate = dateUtilityFunction.getCurrentDateTime(userInfo);
			Short nsitecode = userInfo.getNmastersitecode();
			chartPropTransaction = chartPropTransaction.stream().map(obj -> {
				obj.setNdashboardtypecode(ndashboard);
				obj.setDmodifieddate(modifieddate);
				obj.setNsitecode(nsitecode);
				return obj;
			}).collect(Collectors.toList());

			chartPropTransaction = (List<ChartPropTransaction>) jdbcUtilityFunction.saveBatchOfObjects(
					chartPropTransaction, SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");

			savedDashBoardTypeList.add(dashBoardType);

			finalAuditList.add(savedDashBoardTypeList);
			finalAuditList.add(chartPropTransaction);

			multilingualIDList.add("IDS_ADDDASHBOARDTYPES");
			multilingualIDList.add("IDS_ADDCHARTPROPERTYTRANSACTION");

			auditUtilityFunction.fnInsertListAuditAction(finalAuditList, 1, null, multilingualIDList, userInfo);

			return getDashBoardTypes(userInfo, 0);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> updateDashBoardTypes(DashBoardType dashBoardType,
			List<ChartPropTransaction> chartPropTransaction, UserInfo userInfo) throws Exception {

		final List<Object> beforeSavedDashBoardTypeList = new ArrayList<>();
		final List<Object> savedDashBoardTypeList = new ArrayList<>();

		final DashBoardType dashBoardTypeByID = getDashBoardTypeByID(dashBoardType.getNdashboardtypecode(), userInfo);

		if (dashBoardTypeByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ndashboardtypecode from dashboardtype where sdashboardtypename = N'"
					+ stringUtilityFunction.replaceQuote(dashBoardType.getSdashboardtypename())
					+ "' and ndashboardtypecode <> " + dashBoardType.getNdashboardtypecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();

			final List<DashBoardType> dashBoardTypeList = (List<DashBoardType>) jdbcTemplate.query(queryString,
					new DashBoardType());

			savedDashBoardTypeList.add(dashBoardType);

			if (dashBoardTypeList.isEmpty()) {

				final String updateQueryString = "update dashboardtype set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', sdashboardtypename=N'"
						+ stringUtilityFunction.replaceQuote(dashBoardType.getSdashboardtypename()) + "', nquerycode= "
						+ dashBoardType.getNquerycode() + ", ncharttypecode= " + dashBoardType.getNcharttypecode()
						+ ", nseriesno = " + dashBoardType.getNseriesno() + " where ndashboardtypecode="
						+ dashBoardType.getNdashboardtypecode();

				jdbcTemplate.execute(updateQueryString);

				beforeSavedDashBoardTypeList.add(dashBoardTypeByID);

				final String queryStringProp = "select * from chartproptransaction where ndashboardtypecode= "
						+ dashBoardType.getNdashboardtypecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<ChartPropTransaction> deletedChartPropTransaction = (List<ChartPropTransaction>) jdbcTemplate
						.query(queryStringProp, new ChartPropTransaction());

				final String deleteQueryString = "delete from chartproptransaction where  ndashboardtypecode= "
						+ dashBoardType.getNdashboardtypecode();
				jdbcTemplate.execute(deleteQueryString);

				final int ndashboard = dashBoardType.getNdashboardtypecode();
				Instant modifieddate = dateUtilityFunction.getCurrentDateTime(userInfo);
				Short nsitecode = userInfo.getNmastersitecode();
				chartPropTransaction = chartPropTransaction.stream().map(obj -> {
					obj.setNdashboardtypecode(ndashboard);
					obj.setDmodifieddate(modifieddate);
					obj.setNsitecode(nsitecode);
					return obj;
				}).collect(Collectors.toList());

				chartPropTransaction = (List<ChartPropTransaction>) jdbcUtilityFunction.saveBatchOfObjects(
						chartPropTransaction, SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");

				return getDashBoardTypes(userInfo, dashBoardType.getNdashboardtypecode());

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}

	}

	@Override
	public ResponseEntity<Object> deleteDashBoardTypes(DashBoardType dashBoardType, UserInfo userInfo)
			throws Exception {

		final DashBoardType dashBoardTypeByID = getDashBoardTypeByID(dashBoardType.getNdashboardtypecode(), userInfo);

		if (dashBoardTypeByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String strQuery = " select a.* from dashboardtype a,  dashboardrights b where a.ndashboardtypecode = b.ndashboardtypecode  "
					+ " and b.ndashboardtypecode = " + dashBoardType.getNdashboardtypecode() + " and a.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<DashBoardType> lstdashBoardType = (List<DashBoardType>) jdbcTemplate.query(strQuery,
					new DashBoardType());
			if (lstdashBoardType != null && lstdashBoardType.size() > 0) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_DASHBOARDINUSE", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedDashBoardTypeList = new ArrayList<>();
				final List<Object> finalAuditList = new ArrayList<>();

				final String updateQueryString = "update dashboardtype set  dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ndashboardtypecode="
						+ dashBoardType.getNdashboardtypecode();

				jdbcTemplate.execute(updateQueryString);

				dashBoardType.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedDashBoardTypeList.add(dashBoardType);

				final String queryStringProp = "select * from chartproptransaction where ndashboardtypecode= "
						+ dashBoardType.getNdashboardtypecode();

				final List<ChartPropTransaction> deletedChartPropTransaction = (List<ChartPropTransaction>) jdbcTemplate
						.query(queryStringProp, new ChartPropTransaction());

				final String deleteQueryString = " delete from chartproptransaction where  ndashboardtypecode= "
						+ dashBoardType.getNdashboardtypecode();
				jdbcTemplate.execute(deleteQueryString);

				final String configQuery = "select * from dashboarddesignconfig  where ndashboardtypecode="
						+ dashBoardType.getNdashboardtypecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNmastersitecode();
				List<DashBoardDesignConfig> configListDesignConfig = (List<DashBoardDesignConfig>) jdbcTemplate
						.query(configQuery, new DashBoardDesignConfig());

				final String queryString = "select rpm.* from dashboardparametermapping rpm where rpm.ndashboardtypecode="
						+ dashBoardType.getNdashboardtypecode() + " and rpm.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<DashBoardParameterMapping> configListParameterMapping = (List<DashBoardParameterMapping>) jdbcTemplate
						.query(queryString, new DashBoardParameterMapping());

				String updateQryStr = "update dashboarddesignconfig set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ndashboardtypecode="
						+ dashBoardType.getNdashboardtypecode();

				jdbcTemplate.execute(updateQryStr);

				updateQryStr = "update dashboardparametermapping set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ndashboardtypecode="
						+ dashBoardType.getNdashboardtypecode();

				jdbcTemplate.execute(updateQryStr);

				multilingualIDList.add("IDS_DELETEDASHBOARDPARAMETERMAPPING");
				multilingualIDList.add("IDS_DELETEDASHBOARDDESIGNCONFIG");
				multilingualIDList.add("IDS_DELETECHARTPROPTRANSACTION");
				multilingualIDList.add("IDS_DELETEDASHBOARDTYPES");

				finalAuditList.add(configListParameterMapping);
				finalAuditList.add(configListDesignConfig);
				finalAuditList.add(deletedChartPropTransaction);
				finalAuditList.add(savedDashBoardTypeList);

				auditUtilityFunction.fnInsertListAuditAction(finalAuditList, 1, null, multilingualIDList, userInfo);

				return getDashBoardTypes(userInfo, 0);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getColumnsBasedOnQuery(final int nSqlQueryCode, UserInfo userInfo) throws Exception {

		final String sQuery = " select * from  sqlquery where nsqlquerycode = " + nSqlQueryCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();

		final SQLQuery objSqlQuery = (SQLQuery) jdbcUtilityFunction.queryForObject(sQuery, SQLQuery.class,
				jdbcTemplate);

		final Map<String, Object> objMapFinal = new HashMap<String, Object>();
		if (objSqlQuery != null && objSqlQuery.getSsqlquery().length() > 0) {
			StringBuffer sb = new StringBuffer();
			sb.delete(0, sb.length());

			String sData = "NULL";
			String nData = "0";
			String sSQLQuery = objSqlQuery.getSsqlquery();
			sb.append(sSQLQuery);
			String sFieldName = "";

			while (sSQLQuery.contains("<@") || sSQLQuery.contains("<#")) {

				if (sSQLQuery.contains("<@")) {
					int nStart = sSQLQuery.indexOf("<@");
					int nEnd = sSQLQuery.indexOf("@>");
					sFieldName = sSQLQuery.substring(nStart + 2, nEnd);
					sb.replace(nStart, nEnd + 2, sData);

				} else if (sSQLQuery.contains("<#")) {
					int nStart = sSQLQuery.indexOf("<#");
					int nEnd = sSQLQuery.indexOf("#>");
					if (sData == "NULL") {
						sb.replace(nStart, nEnd + 2, sData);
					} else {
						sb.replace(nStart, nEnd + 2, "'" + sData + "'");
					}
				}
				sSQLQuery = sb.toString();
			}
			List<Map<String, Object>> lstTable = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> lstTableY = new ArrayList<Map<String, Object>>();

			Connection c = jdbcTemplate.getDataSource().getConnection();
			PreparedStatement ps = c.prepareStatement(sSQLQuery);
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			final String sDefaultColor = "#5e72e4";

			for (int i = 1; i <= rsmd.getColumnCount(); i++) {

				Map<String, Object> objMap = new HashMap<String, Object>();
				objMap.put("Value", rsmd.getColumnName(i));
				objMap.put("ColumnName",
						commonFunction.getMultilingualMessage(rsmd.getColumnName(i), userInfo.getSlanguagefilename()));
				objMap.put("Color", sDefaultColor);

				lstTable.add(objMap);

				if ((rsmd.getColumnClassName(i) != "java.lang.String")
						&& (rsmd.getColumnClassName(i) != "java.sql.Timestamp")) {
					lstTableY.add(objMap);
				}
			}
			objMapFinal.put("xSeriesColumns", lstTable);
			objMapFinal.put("ySeriesColumns", lstTableY);
		}
		return new ResponseEntity<Object>(objMapFinal, HttpStatus.OK);
	}

	@Override
	public List<ChartProperty> getChartProperty(int nChartTypeCode, UserInfo userInfo) throws Exception {

		final String strQuery = "select a.* from chartproperty a, charttype b where a.ncharttypecode = b.ncharttypecode and a.ncharttypecode = "
				+ nChartTypeCode + " and b.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and b.nsitecode = " + userInfo.getNmastersitecode();

		return (List<ChartProperty>) jdbcTemplate.query(strQuery, new ChartProperty());
	}

	@Override
	public ResponseEntity<Object> createChartPropTransaction(List<ChartPropTransaction> chartPropTransaction,
			UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedDashBoardTypeList = new ArrayList<>();

		Instant modifieddate = dateUtilityFunction.getCurrentDateTime(userInfo);
		Short nsitecode = userInfo.getNmastersitecode();
		chartPropTransaction = chartPropTransaction.stream().map(obj -> {
			obj.setDmodifieddate(modifieddate);
			obj.setNsitecode(nsitecode);
			return obj;
		}).collect(Collectors.toList());

		chartPropTransaction = (List<ChartPropTransaction>) jdbcUtilityFunction.saveBatchOfObjects(chartPropTransaction,
				SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");

		savedDashBoardTypeList.add(chartPropTransaction);
		multilingualIDList.add("IDS_ADDDASHBOARDTYPES");

		auditUtilityFunction.fnInsertAuditAction(savedDashBoardTypeList, 1, null, multilingualIDList, userInfo);

		return getDashBoardTypes(userInfo, 1);

	}

	@Override
	public ResponseEntity<Object> updateChartPropTransaction(ChartPropTransaction chartPropTransaction,
			UserInfo userInfo) throws Exception {

		return null;
	}

	@Override
	public ResponseEntity<Object> deleteChartPropTransaction(ChartPropTransaction chartPropTransaction,
			UserInfo userInfo) throws Exception {

		return null;
	}

	@Override
	public ResponseEntity<Object> getChartPropTransaction(UserInfo userInfo, int ndashBoardTypeCode, int nChartTypeCode,
			final String sDashBoardProperty, final int nSqlQueryCode) throws Exception {

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();

		final String strQuery = " select ct.*,cp.schartpropertyname from ChartProperty cp,chartproptransaction ct, dashboardtype dt where  "
				+ " cp.ncharttypecode= dt.ncharttypecode  and cp.nchartpropertycode=ct.nchartpropertycode and ct.ndashboardtypecode = dt.ndashboardtypecode and "
				+ " dt.ndashboardtypecode = " + ndashBoardTypeCode + " and ct.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nstatus = "

				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.nsitecode = "
				+ userInfo.getNmastersitecode() + "    order by cp.nchartpropertycode ";

		final List<ChartPropTransaction> chartPropTransaction = (List<ChartPropTransaction>) jdbcTemplate
				.query(strQuery, new ChartPropTransaction());

		List<ChartPropTransaction> chartPropTransactionx = (List<ChartPropTransaction>) commonFunction
				.getMultilingualMessageList(
						chartPropTransaction.stream().filter(xField -> xField.getSchartpropertyname().equals("xField"))
								.collect(Collectors.toList()),
						Arrays.asList("schartpropvalue"), userInfo.getSlanguagefilename());
		final List<ChartPropTransaction> chartPropTransactiony = chartPropTransaction.stream()
				.filter(xField -> xField.getSchartpropertyname().equals("yField")).collect(Collectors.toList());
		final List<ChartPropTransaction> chartPropTransactionColor = chartPropTransaction.stream()
				.filter(xField -> xField.getSchartpropertyname().equals("areaFill")).collect(Collectors.toList());

		final List<ChartPropTransaction> chartPropTransactionxBubble = chartPropTransaction.stream()
				.filter(xField -> xField.getSchartpropertyname().equals("xFieldBubble")).collect(Collectors.toList());
		final List<ChartPropTransaction> chartPropTransactionyBubble = chartPropTransaction.stream()
				.filter(xField -> xField.getSchartpropertyname().equals("yFieldBubble")).collect(Collectors.toList());
		final List<ChartPropTransaction> chartPropTransactionSizeBubble = chartPropTransaction.stream()
				.filter(xField -> xField.getSchartpropertyname().equals("sizeField")).collect(Collectors.toList());
		final List<ChartPropTransaction> chartPropTransactionCategoryBubble = chartPropTransaction.stream()
				.filter(xField -> xField.getSchartpropertyname().equals("categoryField")).collect(Collectors.toList());
		final List<ChartPropTransaction> chartPropTransactionColorBubble = chartPropTransaction.stream()
				.filter(xField -> xField.getSchartpropertyname().equals("colorFill")).collect(Collectors.toList());

		List<Map<String, Object>> lstTable = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lstTableBubble = new ArrayList<Map<String, Object>>();

		String sPieCategory = "";
		String sPieValue = "";
		String sPieCategoryComboValueField = "";
		String sPieValueComboValueField = "";
		if (nChartTypeCode == Enumeration.ChartType.PIE.getChartType()
				|| nChartTypeCode == Enumeration.ChartType.DONUT.getChartType()) {
			for (int i = 0; i < chartPropTransaction.size(); i++) {

				if (chartPropTransaction.get(i).getNchartpropertycode() == 9
						|| chartPropTransaction.get(i).getNchartpropertycode() == 17) {
					sPieCategory = commonFunction.getMultilingualMessage(
							chartPropTransaction.get(i).getSchartpropvalue(), userInfo.getSlanguagefilename());
					sPieCategoryComboValueField = chartPropTransaction.get(i).getSchartpropvalue();
				} else if (chartPropTransaction.get(i).getNchartpropertycode() == 10
						|| chartPropTransaction.get(i).getNchartpropertycode() == 18) {
					sPieValue = commonFunction.getMultilingualMessage(chartPropTransaction.get(i).getSchartpropvalue(),
							userInfo.getSlanguagefilename());
					sPieValueComboValueField = chartPropTransaction.get(i).getSchartpropvalue();
				}
			}
		} else if (nChartTypeCode != Enumeration.ChartType.BUBBLE.getChartType()) {
			for (int i = 0; i < chartPropTransactiony.size(); i++) {
				Map<String, Object> objMapColor = new HashMap<String, Object>();

				objMapColor.put("Value", chartPropTransactiony.get(i).getSchartpropvalue());
				objMapColor.put("ColumnName", commonFunction.getMultilingualMessage(
						chartPropTransactiony.get(i).getSchartpropvalue(), userInfo.getSlanguagefilename()));
				objMapColor.put("Color", chartPropTransactionColor.get(i).getSchartpropvalue());

				lstTable.add(objMapColor);
			}
		} else {
			if (chartPropTransactionSizeBubble != null && chartPropTransactionSizeBubble.size() > 0) {
				for (int i = 0; i < chartPropTransactionSizeBubble.size(); i++) {
					Map<String, Object> objMapColor = new HashMap<String, Object>();

					objMapColor.put("Value", chartPropTransactionSizeBubble.get(i).getSchartpropvalue());
					objMapColor.put("ColumnName",
							commonFunction.getMultilingualMessage(
									chartPropTransactionSizeBubble.get(i).getSchartpropvalue(),
									userInfo.getSlanguagefilename()));
					objMapColor.put("Color", chartPropTransactionColorBubble.get(i).getSchartpropvalue());

					lstTableBubble.add(objMapColor);
				}

				objMap.put("xFieldBubble", chartPropTransactionxBubble);
				objMap.put("yFieldBubble", chartPropTransactionyBubble);
				objMap.put("sizeField", chartPropTransactionSizeBubble);
				objMap.put("categoryField", chartPropTransactionCategoryBubble);
				objMap.put("colorField", lstTableBubble);
			}
		}

		final Map<String, Object> columns = (Map<String, Object>) getColumnsBasedOnQuery(nSqlQueryCode, userInfo)
				.getBody();

		objMap.put("xField", chartPropTransactionx);
		objMap.put("yField", chartPropTransactiony);
		objMap.put("Colors", lstTable);
		objMap.put("Columns", columns);
		objMap.put("pieCategoryColumn", sPieCategory);
		objMap.put("pieValueColumn", sPieValue);
		objMap.put("pieCategoryColumnComboVal", sPieCategoryComboValueField);
		objMap.put("pieValueColumnComboVal", sPieValueComboValueField);

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	/**
	 * This method is used to get the parameter list from the sql query provided for
	 * the selected dashboard type for adding design controls to the parameters
	 */

	@Override
	public ResponseEntity<Object> getDashBoardDesign(UserInfo userInfo, int ndashBoardTypeCode, final int nSqlQueryCode)
			throws Exception {

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstTable = new ArrayList<Map<String, Object>>();

		String strQuery = " select ndesigncomponentcode, coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as sdesigncomponentname, nstatus from designcomponents where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + " order by ndesigncomponentcode ";

		final List<DesignComponents> designComponents = (List<DesignComponents>) jdbcTemplate.query(strQuery,
				new DesignComponents());

		strQuery = " select * from sqlquery where nstatus = " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		           + " and nsitecode = " + userInfo.getNmastersitecode() + " and nquerytypecode = "
				   + Enumeration.QueryType.FILTER.getQuerytype() + " ";
		final List<SQLQuery> sqlQueryForDesign = (List<SQLQuery>) jdbcTemplate.query(strQuery, new SQLQuery());

		strQuery = " select * from sqlquery where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsqlquerycode = " + nSqlQueryCode
				+ " and nsitecode = " + userInfo.getNmastersitecode();
		final SQLQuery sqlQueryForType = (SQLQuery) jdbcUtilityFunction.queryForObject(strQuery, SQLQuery.class,
				jdbcTemplate);

		String sSQLQuery = "";
		if (sqlQueryForType != null && sqlQueryForType.getSsqlquery().length() > 0) {

			StringBuffer sb = new StringBuffer();
			sb.delete(0, sb.length());

			List<String> sOldKeys = new ArrayList<String>();
			String sData = "NULL";
			sSQLQuery = sqlQueryForType.getSsqlquery().replaceAll("\\s", "");
			sb.append(sSQLQuery);

			while (sSQLQuery.contains("<@") || sSQLQuery.contains("<#")) {
				int nStart = 0;
				int nEnd = 0;
				if (sSQLQuery.contains("<@")) {
					nStart = sSQLQuery.indexOf("<@");
					nEnd = sSQLQuery.indexOf("@>");
				} else if (sSQLQuery.contains("<#")) {
					nStart = sSQLQuery.indexOf("<#");
					nEnd = sSQLQuery.indexOf("#>");
				}

				String sQuery = sSQLQuery.substring(0, nStart);
				int nEqualIndex = sQuery.lastIndexOf("=");

				String sParam = sSQLQuery.substring(nStart + 2, nEnd);
				if(!sOldKeys.contains(sParam)) {
					boolean isMandatory = false;
					if (nEqualIndex != -1) {
						if ((sQuery.substring(nEqualIndex, nStart).toUpperCase().contains("ISNULL"))
								|| (sQuery.substring(nEqualIndex, nStart).toUpperCase().contains("COALESCE"))
								|| (sQuery.substring(nEqualIndex, nStart).toUpperCase().contains("IIF"))) {

							isMandatory = true;
						}
					} else {
						isMandatory = true;
					}

					sb.replace(nStart, nEnd + 2, sData);

					sSQLQuery = sb.toString();

					Map<String, Object> objMapParams = new HashMap<String, Object>();
					objMapParams.put("sqlQueryParams", sParam);
					objMapParams.put("Mandatory", isMandatory ? Enumeration.TransactionStatus.NO.gettransactionstatus()
							: Enumeration.TransactionStatus.YES.gettransactionstatus());
					lstTable.add(objMapParams);

				} else {
					sb.replace(nStart, nEnd + 2, sData);
					sSQLQuery = sb.toString();
				}
				sOldKeys.add(sParam);

			}

		}

		objMap.put("designComponents", designComponents);
		objMap.put("sqlQueryForExistingLinkTable", sqlQueryForDesign);
		objMap.put("sqlQueryForParams", lstTable);

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createDashBoardDesignConfig(List<DashBoardDesignConfig> dashBoardDesignConfig,
			final int ndashBoardTypeCode, UserInfo userInfo) throws Exception {

		final String configQuery = "select * from dashboarddesignconfig  where ndashboardtypecode="
				+ dashBoardDesignConfig.get(0).getNdashboardtypecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();
		List<DashBoardDesignConfig> configList = (List<DashBoardDesignConfig>) jdbcTemplate.query(configQuery,
				new DashBoardDesignConfig());

		if (configList.isEmpty()) {
			if (dashBoardDesignConfig.size() > 0) {
				dashBoardDesignConfig.stream().forEach(x -> {
					try {
						x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						x.setNsitecode(userInfo.getNmastersitecode());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
			configList = (List<DashBoardDesignConfig>) jdbcUtilityFunction.saveBatchOfObjects(dashBoardDesignConfig,
					SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");
			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(dashBoardDesignConfig), 1, null,
					Arrays.asList("IDS_ADDDASHBOARDDESIGNCONFIG"), userInfo);
		} else {

			String sQry = " delete from dashboarddesignconfig where ndashboardtypecode = " + ndashBoardTypeCode;
			jdbcTemplate.execute(sQry);

			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(configList), 1, null,
					Arrays.asList("IDS_DELETEDASHBOARDDESIGNCONFIG"), userInfo);
			if (dashBoardDesignConfig.size() > 0) {
				dashBoardDesignConfig.stream().forEach(x -> {
					try {
						x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						x.setNsitecode(userInfo.getNmastersitecode());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
			dashBoardDesignConfig = (List<DashBoardDesignConfig>) jdbcUtilityFunction.saveBatchOfObjects(
					dashBoardDesignConfig, SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");

			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(dashBoardDesignConfig), 1, null,
					Arrays.asList("IDS_ADDDASHBOARDDESIGNCONFIG"), userInfo);
		}
		return getDashBoardTypes(userInfo, ndashBoardTypeCode);

	}

	/**
	 * This method is used to get selected dashboard's design configuration details
	 */

	@Override
	public List<DashBoardDesignConfig> getSelectedDashBoardDesign(UserInfo userInfo, int ndashBoardTypeCode)
			throws Exception {

		final String strQuery = " select a.*, b.ssqlqueryname, coalesce(c.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',c.jsondata->'sdisplayname'->>'en-US') as sdesigncomponentname, b.ssqlquery, b.svaluemember as svaluemember, b.sdisplaymember as sdisplaymember"
				+ " from dashboarddesignconfig a, sqlquery b, designcomponents c, dashboardtype d  where a.nsqlquerycode = b.nsqlquerycode and  "
				+ " a.ndesigncomponentcode = c.ndesigncomponentcode and a.ndashboardtypecode = d.ndashboardtypecode and a.ndashboardtypecode = "
				+ ndashBoardTypeCode + " and a.nstatus = "
				+ +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and a.nsitecode = "
				+ userInfo.getNmastersitecode() + " order by a.ndashboarddesigncode ";

		final List<DashBoardDesignConfig> lstDesignConfig = (List<DashBoardDesignConfig>) jdbcTemplate.query(strQuery,
				new DashBoardDesignConfig());

		return lstDesignConfig;
	}

	@Override
	public ResponseEntity<Object> getDashBoardView(UserInfo userInfo, int nDashBoardTypeCode) throws Exception {

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();

		final String strQuery = " select a.*, coalesce(b.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',b.jsondata->'sdisplayname'->>'en-US') as schartname from dashboardtype a, charttype b, sqlquery c, dashboardrights d where a.ncharttypecode = b.ncharttypecode  "
				+ " and a.nquerycode = c.nsqlquerycode and a.ndashboardtypecode = d.ndashboardtypecode and a.nstatus = b.nstatus and a.nstatus = c.nstatus  "
				+ " and a.nsitecode = " + userInfo.getNmastersitecode() + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nuserrolecode = "
				+ userInfo.getNuserrole() + " and a.nsitecode = " + userInfo.getNmastersitecode()
				+ " order by a.ndashboardtypecode ";

		List<DashBoardType> lstdashBoardType = (List<DashBoardType>) jdbcTemplate.query(strQuery, new DashBoardType());// getListDahsBoardTypes(userInfo,
																														// nDashBoardTypeCode);

		objMap.put("userinfo", userInfo);
		objMap.put("DashBoardView", lstdashBoardType);

		if (nDashBoardTypeCode == 0 && lstdashBoardType.size() > 0) {
			nDashBoardTypeCode = lstdashBoardType.get(lstdashBoardType.size() - 1).getNdashboardtypecode();

			final DashBoardType dashBoardType = lstdashBoardType.get(lstdashBoardType.size() - 1);
			objMap.put("selectedDashBoardTypes", dashBoardType);
			objMap.put("dashboardtypes", dashBoardType);
			objMap.putAll((Map<String, Object>) getSelectedDashBoardView(userInfo, dashBoardType).getBody());
		} else {
			objMap.put("selectedDashBoardTypes", new HashMap<String, Object>());
		}

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> checkParameteAvailableInDashBoardView(UserInfo userInfo, int nDashBoardTypeCode)
			throws Exception {

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();

		final List<DashBoardDesignConfig> lstDesignConfig = getSelectedDashBoardDesign(userInfo, nDashBoardTypeCode);

		if (lstDesignConfig != null && lstDesignConfig.size() > 0) {

			objMap.putAll(viewDashboardDesignConfigList(lstDesignConfig, userInfo));
			return new ResponseEntity<>(objMap, HttpStatus.OK);
		} else {

			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PARAMETERSNOTAVAILABLE",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	public boolean isStringInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	@Override
	public ResponseEntity<Object> getAllSelectionDashBoardView(UserInfo userInfo, int nDashBoardTypeCode)
			throws Exception {

		final DashBoardType dashBoardType = getDashBoardTypeByID(nDashBoardTypeCode, userInfo);

		return getSelectedDashBoardView(userInfo, dashBoardType);
	}

	public ResponseEntity<Object> getSelectedDashBoardView(UserInfo userInfo, final DashBoardType dashBoardType)
			throws Exception {

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		Map<String, Object> objParamMap = new LinkedHashMap<String, Object>();
		ObjectMapper objmap = new ObjectMapper();
		Map<String, Object> mapUserInfo = objmap.convertValue(userInfo, Map.class);
		objMap.put("dashboardtypes", dashBoardType);
		objMap.put("userinfo", userInfo);

		if (dashBoardType == null) {
			return new ResponseEntity<>(new HashMap<String, Object>(), HttpStatus.OK);
		} else {
			List<DashBoardDesignConfig> lstDesignConfig = getSelectedDashBoardDesign(userInfo,
					dashBoardType.getNdashboardtypecode());
			objMap.put("selectedDesignConfig", lstDesignConfig);

			if (lstDesignConfig.size() > 0) {
				for (int i = 0; i < lstDesignConfig.size(); i++) {
					if (lstDesignConfig.get(i).getNdesigncomponentcode() == Enumeration.DesignComponent.DATEFEILD
							.gettype()) {
						Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo); // objGeneral.getUTCDateTime();
						if (lstDesignConfig.get(i).getNdays() >= 0) {
							instantDate = instantDate.plus(lstDesignConfig.get(i).getNdays(), ChronoUnit.DAYS);
						} else {
							instantDate = instantDate.minus(Math.abs(lstDesignConfig.get(i).getNdays()),
									ChronoUnit.DAYS);
						}

						objParamMap.put(lstDesignConfig.get(i).getSfieldname(),
								dateUtilityFunction.instantDateToString(instantDate));
						objParamMap.put(lstDesignConfig.get(i).getSfieldname() + "_componentcode",
								lstDesignConfig.get(i).getNdesigncomponentcode());
						objParamMap.put(lstDesignConfig.get(i).getSfieldname() + "_componentname",
								lstDesignConfig.get(i).getSdesigncomponentname());
						String isDate = "Yes";

						objMap.put("dateField", isDate);
					} else {

						objParamMap.put(lstDesignConfig.get(i).getSfieldname(),
								lstDesignConfig.get(i).getNdesigncomponentcode() == Enumeration.DesignComponent.USERINFO
										.gettype() ? mapUserInfo.get(lstDesignConfig.get(i).getSfieldname())
												: lstDesignConfig.get(i).getSdefaultvalue());
						objParamMap.put(lstDesignConfig.get(i).getSfieldname() + "_componentcode",
								lstDesignConfig.get(i).getNdesigncomponentcode());
						objParamMap.put(lstDesignConfig.get(i).getSfieldname() + "_componentname",
								lstDesignConfig.get(i).getSdesigncomponentname());
					}
				}
				objMap.put("inputfielddata", objParamMap);
			} else {
				objMap.put("inputfielddata", parameterMap);
			}

			return getChartParameters(objMap);
		}

	}

	public ResponseEntity<Object> getDashboardViewChartParameters(Map<String, Object> obj) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(obj.get("userinfo"), UserInfo.class);
		objmapper.registerModule(new JavaTimeModule());
		final DashBoardType dashBoardType = objmapper.convertValue(obj.get("dashboardtypes"), DashBoardType.class);

		final List<DashBoardDesignConfig> lstDesignConfig = getSelectedDashBoardDesign(userInfo,
				dashBoardType.getNdashboardtypecode());
		obj.put("selectedDesignConfig", lstDesignConfig);
		return getChartParameters(obj);
	}

	public ResponseEntity<Object> getChartParameters(Map<String, Object> obj) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final String isDateMap = (String) obj.get("dateField");
		final Map<String, Object> parameterMap = (Map<String, Object>) obj.get("inputfielddata");

		final UserInfo userInfo = objmapper.convertValue(obj.get("userinfo"), UserInfo.class);
		objmapper.registerModule(new JavaTimeModule());
		final DashBoardType dashBoardType = objmapper.convertValue(obj.get("dashboardtypes"), DashBoardType.class);

		final String strQuery = " select * from sqlquery where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsqlquerycode = "
				+ dashBoardType.getNquerycode();
		final SQLQuery sqlQueryForType = (SQLQuery) jdbcUtilityFunction.queryForObject(strQuery, SQLQuery.class,
				jdbcTemplate);

		String sSQLQuery = "";
		if (sqlQueryForType != null && sqlQueryForType.getSsqlquery().length() > 0) {
			sSQLQuery = sqlQueryForType.getSsqlquery();

			Map<String, Object> mapUser = new HashMap<>();
			ObjectMapper mapper = new ObjectMapper();
			mapUser = mapper.convertValue(userInfo, Map.class);
			String sOldKey = "";
			int firstIndex = 0;
			int lastIndex = 0;
			int firstIndexWithSingleQuotes = -1;
			int lastIndexWithSingleQuotes = -1;
			String sFieldKeyWithSingleQuotes = "";
			String sFieldKey = "";
			String sTempData = "";
			short shortvalue = 0;

			while (sSQLQuery.contains("<@") || sSQLQuery.contains("<#")) {

				if (sSQLQuery.contains("<@")) {
					firstIndex = sSQLQuery.indexOf("<@");
					lastIndex = sSQLQuery.indexOf("@>");
				} else if (sSQLQuery.contains("<#")) {
					firstIndex = sSQLQuery.indexOf("<#");
					lastIndex = sSQLQuery.indexOf("#>");
				}

				sFieldKey = sSQLQuery.substring(firstIndex + 2, lastIndex);
				if (sSQLQuery.contains("'<@")) {

					firstIndexWithSingleQuotes = sSQLQuery.indexOf("'<@");
					lastIndexWithSingleQuotes = sSQLQuery.indexOf("@>'");
				} else if (sSQLQuery.contains("'<#")) {

					firstIndexWithSingleQuotes = sSQLQuery.indexOf("'<#");
					lastIndexWithSingleQuotes = sSQLQuery.indexOf("#>'");

				}
				if (firstIndexWithSingleQuotes != -1 && lastIndexWithSingleQuotes != -1) {
					sFieldKeyWithSingleQuotes = sSQLQuery.substring(firstIndexWithSingleQuotes + 3,
							lastIndexWithSingleQuotes);
				}
				boolean bcheckSingleQuotes = false;

				if (!sFieldKey.equals(sOldKey)) {
					if (parameterMap.containsKey(sFieldKey)) {

						if (!sFieldKey.endsWith("_componentcode") && !sFieldKey.endsWith("_componentname")) {

							if (sFieldKey.equals(sFieldKeyWithSingleQuotes)) {

								bcheckSingleQuotes = true;
							}

							if (parameterMap.get(sFieldKey.concat("_componentcode")).getClass().getSimpleName()
									.equals("Short")) {
								shortvalue = (short) parameterMap.get(sFieldKey.concat("_componentcode"));

							} else {
								Integer objval = new Integer(
										(int) parameterMap.get(sFieldKey.concat("_componentcode")));
								shortvalue = objval.shortValue();
							}

							if (shortvalue == Enumeration.DesignComponent.DATEFEILD.gettype()) {

								if (isDateMap != null) {
									if (bcheckSingleQuotes == true) {
										sTempData = "" + (String) parameterMap.get(sFieldKey) + "";
									} else {
										sTempData = "'" + (String) parameterMap.get(sFieldKey) + "'";
									}
								} else {
									String dateValue = (String) parameterMap.get(sFieldKey);
									if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES
											.gettransactionstatus()) {
										Instant instantDate = dateUtilityFunction.convertStringDateToUTC(
												(String) parameterMap.get(sFieldKey), userInfo, false);
										dateValue = dateUtilityFunction.instantDateToString(instantDate);
									} else {
										Instant date = Instant.parse(dateValue);
										dateValue = dateUtilityFunction.instantDateToString(date).replace("T", " ")
												.replace("Z", "");

									}

									if (bcheckSingleQuotes == true) {
										sTempData = "" + dateValue + "";
									} else {
										sTempData = "'" + dateValue + "'";
									}

								}

							} else {

								boolean bCheckInt = isStringInt(String.valueOf(parameterMap.get(sFieldKey)));
								if (bCheckInt == true) {
									if (parameterMap.get(sFieldKey).getClass().getName() == "java.lang.Integer"
											|| parameterMap.get(sFieldKey).getClass().getName() == "java.lang.Short") {
										sTempData = String.valueOf(parameterMap.get(sFieldKey));
									} else {
										sTempData = (String) parameterMap.get(sFieldKey);
									}

								} else {
									sTempData = (String) parameterMap.get(sFieldKey);
									if (sTempData == null) {
										sTempData = "" + sTempData + "";
									} else {
										if (bcheckSingleQuotes == true) {
											sTempData = "" + sTempData + "";
										} else {
											sTempData = "'" + sTempData + "'";
										}

									}
								}
							}
						}

						StringBuffer sb = new StringBuffer(sSQLQuery);

						sb.replace(firstIndex, lastIndex + 2, sTempData);
						sSQLQuery = sb.toString();
						LOGGER.info(sSQLQuery);

					} else if (mapUser != null && mapUser.containsKey(sFieldKey)) {
						if (mapUser.get(sFieldKey).getClass().getName() == "java.lang.Integer") {
							int value = (int) mapUser.get(sFieldKey);
							sTempData = Integer.toString(value);
						} else if (mapUser.get(sFieldKey).getClass().getName() == "java.lang.String") {
							sTempData = (String) mapUser.get(sFieldKey);
							sTempData = "'" + sTempData + "'";
						}
						StringBuffer sb = new StringBuffer(sSQLQuery);
						sb.replace(firstIndex, lastIndex + 2, sTempData);
						sSQLQuery = sb.toString();

					} else {
						sOldKey = sFieldKey;
					}
				} else {
					sTempData = "null";
					StringBuffer sb = new StringBuffer(sSQLQuery);
					sb.replace(firstIndex, lastIndex + 2, sTempData);
					sSQLQuery = sb.toString();

				}
			}
		}
		List<Map<String, Object>> lstData = jdbcTemplate.queryForList(sSQLQuery);

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final String sChartProps = " select cp.nchartpropertycode,cp.schartpropertyname,ct.schartpropvalue, ct.nseries from ChartProperty cp,chartproptransaction ct, dashboardtype dt where  "
				+ " cp.ncharttypecode= dt.ncharttypecode  and cp.nchartpropertycode=ct.nchartpropertycode and ct.ndashboardtypecode = dt.ndashboardtypecode and "
				+ " dt.ndashboardtypecode = " + dashBoardType.getNdashboardtypecode() + " and ct.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.nsitecode = "
				+ userInfo.getNmastersitecode() + "    order by cp.nchartpropertycode ";

		List<Map<String, Object>> lstPropert = jdbcTemplate.queryForList(sChartProps);
		List<Map<String, Object>> lstPieChart = new ArrayList<Map<String, Object>>();
		String fieldCategory = "";
		String fieldValue = "";

		if (dashBoardType.getNcharttypecode() == Enumeration.ChartType.PIE.getChartType()
				|| dashBoardType.getNcharttypecode() == Enumeration.ChartType.DONUT.getChartType()) {

			if (lstPropert != null && lstPropert.size() > 0) {

				for (int j = 0; j < lstPropert.size(); j++) {
					if (((String) lstPropert.get(j).get("schartpropertyname")).equals("field")) {

						fieldCategory = (String) lstPropert.get(j).get("schartpropvalue");
					} else if (((String) lstPropert.get(j).get("schartpropertyname")).equals("nameField")) {

						fieldValue = (String) lstPropert.get(j).get("schartpropvalue");
					}

				}
				if (lstData != null && lstData.size() > 0) {

					for (int i = 0; i < lstData.size(); i++) {

						Map<String, Object> objPieChartMap = new LinkedHashMap<String, Object>();
						if (lstData.get(i).get(fieldCategory).getClass().getName() == "java.lang.Integer") {
							objPieChartMap.put("categoryField", (int) lstData.get(i).get(fieldCategory));
						} else if (lstData.get(i).get(fieldCategory).getClass().getName() == "java.lang.Long") {
							objPieChartMap.put("categoryField", (Long) lstData.get(i).get(fieldCategory));
						} else {
							objPieChartMap.put("categoryField", commonFunction.getMultilingualMessage(
									(String) lstData.get(i).get(fieldCategory), userInfo.getSlanguagefilename()));
						}
						objPieChartMap.put("valueField", lstData.get(i).get(fieldValue));
						lstPieChart.add(objPieChartMap);
					}
				}
			}
		}

		String xPropField = "";
		String yPropField = "";
		String colorPropField = "";
		String categoryField = "categoryField";
		String sizeField = "sizeField";
		List<Map<String, Object>> lstCategoryField = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lstSizeField = new ArrayList<Map<String, Object>>();

		if (dashBoardType.getNcharttypecode() == Enumeration.ChartType.BUBBLE.getChartType()) {

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
		String xFieldName1 = "";

		for (int i = 0; i < lstPropert.size(); i++) {
			if (lstPropert.get(i).get("schartpropertyname").equals(xPropFieldfinal)) {
				xFieldName1 = (String) lstPropert.get(i).get("schartpropvalue");
				break;
			} else {
				xFieldName1 = "";
			}
		}

		final String xFieldName = xFieldName1;
		List<Object> category = lstData.stream().map(data -> data.get(xFieldName)).collect(Collectors.toList());

		List<Map<String, Object>> ySeries = lstPropert.stream()
				.filter(yField -> ((String) yField.get("schartpropertyname")).equals(yPropFieldfinal))
				.collect(Collectors.toList());

		List<Map<String, Object>> areaFill = lstPropert.stream()
				.filter(yField -> ((String) yField.get("schartpropertyname")).equals(colorPropFieldfinal))
				.collect(Collectors.toList());

		List<Map<String, Object>> lstChartSeries = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lstChartSeriesBubble = new ArrayList<Map<String, Object>>();

		if (dashBoardType.getNcharttypecode() != Enumeration.ChartType.BUBBLE.getChartType()
				&& dashBoardType.getNcharttypecode() != Enumeration.ChartType.PIE.getChartType()
				&& dashBoardType.getNcharttypecode() != Enumeration.ChartType.DONUT.getChartType()) {
			for (int i = 0; i < ySeries.size(); i++) {
				final Map<String, Object> objMapSeries = new HashMap<String, Object>();

				String yFieldName = (String) ySeries.get(i).get("schartpropvalue");

				objMapSeries.put("yField",
						commonFunction.getMultilingualMessage(yFieldName, userInfo.getSlanguagefilename()));
				objMapSeries.put("colors", areaFill.get(i).get("schartpropvalue"));
				objMapSeries.put("Series",
						lstData.stream().map(data -> data.get(yFieldName) == null ? 0 : data.get(yFieldName))
								.collect(Collectors.toList()));

				lstChartSeries.add(objMapSeries);
			}
		} else {

			for (int i = 0; i < ySeries.size(); i++) {
				final Map<String, Object> objMapSeries = new HashMap<String, Object>();

				String yFieldName = (String) ySeries.get(i).get("schartpropvalue");
				String sizeFieldNameForLegend = commonFunction.getMultilingualMessage(
						(String) lstSizeField.get(i).get("schartpropvalue"), userInfo.getSlanguagefilename());
				String sizeFieldName = (String) lstSizeField.get(i).get("schartpropvalue");
				String categoryFieldName = (String) lstCategoryField.get(i).get("schartpropvalue");
				objMapSeries.put("colorFill", areaFill.get(i).get("schartpropvalue"));
				objMapSeries.put("ySeriesBubble", yFieldName);
				objMapSeries.put("sizeSeriesBubble", sizeFieldName);
				objMapSeries.put("sizeSeriesBubbleForLegend", sizeFieldNameForLegend);
				objMapSeries.put("categorySeriesBubble", categoryFieldName);
				objMapSeries.put("xFieldBubble", xFieldName);

				lstChartSeriesBubble.add(objMapSeries);
			}
		}

		if (dashBoardType.getNcharttypecode() == Enumeration.ChartType.PIE.getChartType()
				|| dashBoardType.getNcharttypecode() == Enumeration.ChartType.DONUT.getChartType()
				|| dashBoardType.getNcharttypecode() == Enumeration.ChartType.BUBBLE.getChartType()) {

			if (dashBoardType.getNcharttypecode() == Enumeration.ChartType.BUBBLE.getChartType()) {

				objMap.put("bubbleSeries", lstChartSeriesBubble);
			}
		} else {

			objMap.put("xSeries", category);
			objMap.put("ySeries", lstChartSeries);
		}

		final List<DashBoardDesignConfig> lstDesignConfig = (List<DashBoardDesignConfig>) obj
				.get("selectedDesignConfig");
		objMap.put("chartData", lstData);
		objMap.put("selectedDesignConfig", lstDesignConfig);
		objMap.put("selectedDashBoardTypes", dashBoardType);
		objMap.put("pieChart", lstPieChart);

		Map<String, Object> filterParameter = viewDashboardDesignConfigList(lstDesignConfig, userInfo);
		objMap.put("viewDashBoardDesignConfigList", filterParameter.get("viewDashBoardDesignConfigList"));

		objMap.put("filterParameter", filterParameter.get("viewDashBoardDesignConfigList"));
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	public List<DashBoardParameterMapping> getDashBoardParameterMapping(final int ndashBoardTypeCode, UserInfo userInfo)
			throws Exception {

		final String queryString = "select *, rdc1.sdisplayname as sparentparametername,"
				+ " rdc2.sdisplayname as schildparametername," + " coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sisactionparent "
				+ " from dashboardparametermapping rpm, "
				+ " dashboarddesignconfig rdc1 , dashboarddesignconfig rdc2, transactionstatus ts where "
				+ " rdc1.ndashboarddesigncode = rpm.nparentdashboarddesigncode "
				+ " and rdc2.ndashboarddesigncode = rpm.nchilddashboarddesigncode " + " and rpm.nstatus = rdc1.nstatus "
				+ " and rpm.nisactionparent = ts.ntranscode" + " and rpm.ndashboardtypecode=" + ndashBoardTypeCode
				+ " and rpm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rpm.nsitecode = " + userInfo.getNmastersitecode();

		final List<DashBoardParameterMapping> mappingList = (List<DashBoardParameterMapping>) jdbcTemplate
				.query(queryString, new DashBoardParameterMapping());
		return mappingList;

	}

	@Override
	public ResponseEntity<Object> getDashBoardParameterMappingComboData(int nDashBoardTypeCode, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strQuery = "select rdc.*, sq.ssqlqueryname as ssqlqueryname, sq.ssqlquery as ssqlquery, sq.svaluemember as svaluemember from "
				+ " dashboarddesignconfig rdc, sqlquery sq where rdc.nsqlquerycode = sq.nsqlquerycode "
				+ " and rdc.nsitecode = " + userInfo.getNmastersitecode()
				+ " and rdc.nstatus = sq.nstatus and rdc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rdc.ndashboardtypecode= "
				+ nDashBoardTypeCode;

		final List<DashBoardDesignConfig> designComponentList = (List<DashBoardDesignConfig>) jdbcTemplate
				.query(strQuery, new DashBoardDesignConfig());
		outputMap.put("ParentComponenList", designComponentList);

		final List<DashBoardDesignConfig> parameterList = designComponentList.stream()
				.filter(item -> item.getSsqlquery().contains("<@") || item.getSsqlquery().contains("<#"))
				.collect(Collectors.toList());
		outputMap.put("ChildComponentList", parameterList);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createDashBoardParameterMapping(List<DashBoardParameterMapping> mappingList,
			UserInfo userInfo) throws Exception {

		int nDashBoardTypeCode = 0;

		final String queryString = "select rpm.* from dashboardparametermapping rpm where rpm.ndashboardtypecode="
				+ mappingList.get(0).getNdashboardtypecode() + " and rpm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rpm.nsitecode = "
				+ userInfo.getNmastersitecode();
		List<DashBoardParameterMapping> configList = (List<DashBoardParameterMapping>) jdbcTemplate.query(queryString,
				new DashBoardParameterMapping());

		if (configList.isEmpty()) {
			if (mappingList.size() > 0) {
				mappingList.stream().forEach(x -> {
					try {
						x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						x.setNsitecode(userInfo.getNmastersitecode());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
			configList = (List<DashBoardParameterMapping>) jdbcUtilityFunction.saveBatchOfObjects(mappingList,
					SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");
			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(mappingList), 1, null,
					Arrays.asList("IDS_ADDDASHBOARDPARAMETERMAPPING"), userInfo);

		} else {

			nDashBoardTypeCode = mappingList.get(0).getNdashboardtypecode();

			final String delQuery = "delete from dashboardparametermapping where ndashboardtypecode="
					+ mappingList.get(0).getNdashboardtypecode();
			jdbcTemplate.execute(delQuery);

			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(configList), 1, null,
					Arrays.asList("IDS_DELETEDASHBOARDPARAMETERMAPPING"), userInfo);
			if (mappingList.size() > 0) {
				mappingList.stream().forEach(x -> {
					try {
						x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						x.setNsitecode(userInfo.getNmastersitecode());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
			configList = (List<DashBoardParameterMapping>) jdbcUtilityFunction.saveBatchOfObjects(mappingList,
					SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");
			auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(mappingList), 1, null,
					Arrays.asList("IDS_ADDDASHBOARDPARAMETERMAPPING"), userInfo);

		}
		return getDashBoardTypes(userInfo, nDashBoardTypeCode);

	}

	public ResponseEntity<Object> getChildDataList(final DashBoardDesignConfig parentConfig,
			final Map<String, Object> inputFieldData, final String parentValue, final UserInfo userInfo)
			throws Exception {
		final String query = " select rpm.*, rdc.sdisplayname as schildparametername, rpm.sfieldname as sparentparametername ,"
				+ " sq.ssqlquery from dashboardparametermapping rpm, " + " dashboarddesignconfig rdc,sqlquery sq where "
				+ "	rpm.nchilddashboarddesigncode = rdc.ndashboarddesigncode "
				+ " and rdc.nsqlquerycode = sq.nsqlquerycode and rpm.nstatus = rdc.nstatus "
				+ " and rdc.nstatus = sq.nstatus and sq.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and rpm.ndashboardtypecode="
				+ parentConfig.getNdashboardtypecode() + " and rpm.nisactionparent = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and rpm.nparentdashboarddesigncode="
				+ parentConfig.getNdashboarddesigncode() + " and rdc.nsitecode = " + userInfo.getNmastersitecode();

		final List<DashBoardParameterMapping> actionList = (List<DashBoardParameterMapping>) jdbcTemplate.query(query,
				new DashBoardParameterMapping());

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final StringBuffer queryBuffer = new StringBuffer();
		final List<String> childFieldList = new ArrayList<String>();

		for (DashBoardParameterMapping parameterMapping : actionList) {

			final String sqlquery = parameterMapping.getSsqlquery();
			queryBuffer.append(sqlquery + ";");
			childFieldList.add(parameterMapping.getSchildparametername());

			int count = StringUtils.countMatches(sqlquery, "<@");
			int count1 = StringUtils.countMatches(sqlquery, "<#");
			if (count > 1 || count1 > 1) {
				for (Map.Entry<String, Object> entrySet : inputFieldData.entrySet()) {
					String key = entrySet.getKey();
					String fieldValue = "";
					if (sqlquery.contains("<@".concat(key.concat("@>")))) {
						if (!entrySet.getKey().endsWith("_componentcode")
								&& !entrySet.getKey().endsWith("_componentname")) {
							if (inputFieldData.get(entrySet.getKey().concat("_componentcode")) != null
									&& (Integer) inputFieldData.get(entrySet.getKey().concat(
											"_componentcode")) == Enumeration.DesignComponent.DATEFEILD.gettype()) {
								if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES
										.gettransactionstatus()) {
									final Instant instantDate = dateUtilityFunction.convertStringDateToUTC(
											(String) inputFieldData.get(entrySet.getKey()), userInfo, false);
									;

									final String dateValue = dateUtilityFunction.instantDateToString(instantDate);

									fieldValue = "'" + dateValue + "'";
								} else {

									fieldValue = "'" + (String) inputFieldData.get(entrySet.getKey()) + "'";
								}

							} else {

								if (entrySet.getValue() instanceof Integer) {
									fieldValue = Integer.toString((Integer) entrySet.getValue());
								} else {
									fieldValue = (String) entrySet.getValue();
								}
							}

							final String parameterName = "<@".concat(key.concat("@>"));
							if (queryBuffer.indexOf(parameterName) != -1) {
								queryBuffer.replace(queryBuffer.indexOf(parameterName),
										(queryBuffer.indexOf(parameterName) + parameterName.length()),
										"'" + fieldValue + "'");
							}

						}
					} else if (sqlquery.contains("<#".concat(key.concat("#>")))) {
						if (!entrySet.getKey().endsWith("_componentcode")
								&& !entrySet.getKey().endsWith("_componentname")) {
							if (inputFieldData.get(entrySet.getKey().concat("_componentcode")) != null
									&& (Integer) inputFieldData.get(entrySet.getKey().concat(
											"_componentcode")) == Enumeration.DesignComponent.DATEFEILD.gettype()) {

								Instant instantDate = dateUtilityFunction.convertStringDateToUTC(
										(String) inputFieldData.get(entrySet.getKey()), userInfo, false);
								;

								final String dateValue = dateUtilityFunction.instantDateToString(instantDate);
								fieldValue = "'" + dateValue + "'";
							} else {
								if (entrySet.getValue() instanceof Integer) {
									fieldValue = Integer.toString((Integer) entrySet.getValue());
								} else {
									fieldValue = (String) entrySet.getValue();
								}
							}
							final String parameterName = "<#".concat(key.concat("#>"));
							if (queryBuffer.indexOf(parameterName) != -1) {
								queryBuffer.replace(queryBuffer.indexOf(parameterName),
										(queryBuffer.indexOf(parameterName) + parameterName.length()),
										"'" + fieldValue + "'");
							}
						}
					}
				}
			} else {
				String fieldValue = parentValue;
				if (parentConfig.getNdesigncomponentcode() == Enumeration.DesignComponent.DATEFEILD.gettype()) {
					Instant instantDate = dateUtilityFunction.convertStringDateToUTC(parentValue, userInfo, false);
					fieldValue = "'" + dateUtilityFunction.instantDateToString(instantDate) + "'";
				}
				final String parameterName = "<@".concat(parameterMapping.getSparentparametername().concat("@>"));
				if (queryBuffer.indexOf(parameterName) > 0) {
					queryBuffer.replace(queryBuffer.indexOf(parameterName),
							(queryBuffer.indexOf(parameterName) + parameterName.length()), "'" + fieldValue + "'");

				} else if (queryBuffer
						.indexOf("<#".concat(parameterMapping.getSparentparametername().concat("#>"))) > 0) {
					final String parameterName1 = "<#".concat(parameterMapping.getSparentparametername().concat("#>"));
					queryBuffer.replace(queryBuffer.indexOf(parameterName1),
							(queryBuffer.indexOf(parameterName1) + parameterName1.length()), "'" + fieldValue + "'");
				}
			}

		}

		if (queryBuffer.length() > 0) {

			final Map<String, Object> defaultObject = (Map<String, Object>) projectDAOSupport
					.getMutlipleEntityResultsUsingPlainSql(queryBuffer.toString(), jdbcTemplate,
							childFieldList.toArray());

			final ObjectMapper objMapper = new ObjectMapper();

			for (String fieldName : childFieldList) {
				List<?> list = (List<?>) defaultObject.get(fieldName);

				if (list != null && list.size() > 0) {
					Map<String, Object> mapcolumnfileds = objMapper.convertValue(list.get(0), Map.class);
					List<String> multilingualColumnList = new ArrayList<String>();

					for (Map.Entry<String, Object> entry : mapcolumnfileds.entrySet()) {
						if (entry.getValue() != null) {
							if (entry.getValue().getClass().getSimpleName().equalsIgnoreCase("String")
									&& entry.getValue().toString().length() > 4
									&& entry.getValue().toString().substring(0, 4).equals("IDS_")) {
								multilingualColumnList.add(entry.getKey());
							}
						}
					}

					list = objMapper
							.convertValue(
									commonFunction.getMultilingualMessageList(list, multilingualColumnList,
											userInfo.getSlanguagefilename()),
									new TypeReference<List<Map<String, Object>>>() {
									});
				}
				outputMap.put(fieldName, list);
			}
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getHomeDashBoard1(UserInfo userInfo, final int npageCode, final Boolean bpageAction)
			throws Exception {

		final Map<String, Object> objMapReturn = new LinkedHashMap<String, Object>();
		final List<Map<String, Object>> lstMap = new ArrayList<Map<String, Object>>();

		String strQuery = " select dhp.* "
				+ "from dashboardhomerights dhr, dashboardhomepriority dhp where dhr.ndashboardhomeprioritycode = dhp.ndashboardhomeprioritycode  and dhp.nuserrolecode = "
				+ userInfo.getNuserrole() + " and dhr.nuserrolecode = dhp.nuserrolecode and dhr.nsitecode = "
				+ userInfo.getNmastersitecode() + " and  dhr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dhp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by dhp.ndashboardhomepagecode ";

		final List<DashBoardHomePriority> lstdashBoardHomePriority = (List<DashBoardHomePriority>) jdbcTemplate
				.query(strQuery, new DashBoardHomePriority());

		if (lstdashBoardHomePriority.size() > 0) {
			for (int ncount = 0; ncount < lstdashBoardHomePriority.size(); ncount++) {

				final Map<String, Object> objMap = new LinkedHashMap<String, Object>();

				strQuery = " select * from dashboardhometypes where ndashboardhomeprioritycode  = "
						+ lstdashBoardHomePriority.get(ncount).getNdashboardhomeprioritycode() + " ";
				final List<DashBoardHomeTypes> lstdashBoardHomeType = (List<DashBoardHomeTypes>) jdbcTemplate
						.query(strQuery, new DashBoardHomeTypes());

				for (int i = 0; i < lstdashBoardHomeType.size(); i++) {

					objMap.put("dashBoardType" + (i + 1),
							getAllSelectionDashBoardView(userInfo, lstdashBoardHomeType.get(i).getNdashboardtypecode())
									.getBody());
					objMap.put("ntemplatecode", lstdashBoardHomePriority.get(ncount).getNdashboardhometemplatecode());
				}

				lstMap.add(objMap);
			}
		}
		objMapReturn.put("homeDashBoard", lstMap);

		return new ResponseEntity<Object>(objMapReturn, HttpStatus.OK);
	}

	public ResponseEntity<Object> getHomeDashBoard(UserInfo userInfo, final int npageCode, final Boolean bpageAction)
			throws Exception {

		final String strQuery = "select dhp.*, dht.ndashboardtypecode, ROW_NUMBER()  "
				+ " over(partition by   dhp.ndashboardhomepagecode  "
				+ " order by dhp.ndashboardhomepagecode) as npagedashboardindex " + " from dashboardhomerights dhr  "
				+ " join dashboardhomepriority dhp   on "
				+ " dhr.ndashboardhomeprioritycode = dhp.ndashboardhomeprioritycode  " + " and dhp.nuserrolecode = "
				+ userInfo.getNuserrole() + " and dhp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and dhr.nuserrolecode = dhp.nuserrolecode " + " left outer join dashboardhometypes dht  on  "
				+ " dhp.ndashboardhomeprioritycode = dht.ndashboardhomeprioritycode   " + " and dht.nsitecode = "
				+ userInfo.getNmastersitecode() + " and dht.nstatus = dhr.nstatus and dhr.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dhr.nsitecode = "
				+ userInfo.getNmastersitecode() + " order by dhp.ndashboardhomepagecode ";

		final List<DashBoardHomePriority> homePriorityList = (List<DashBoardHomePriority>) jdbcTemplate.query(strQuery,
				new DashBoardHomePriority());

		final Set<Integer> dashBoardTypeCodeList = new TreeSet<Integer>();
		final Set<Integer> dashBoardPageCodeList = new TreeSet<Integer>();

		for (int i = 0; i < homePriorityList.size(); i++) {
			dashBoardTypeCodeList.add(homePriorityList.get(i).getNdashboardtypecode());
			dashBoardPageCodeList.add(homePriorityList.get(i).getNdashboardhomepagecode());
		}

		final Map<Integer, Object> dashBoardTypeMap = new HashMap<Integer, Object>();

		Integer arr[] = new Integer[dashBoardTypeCodeList.size()];
		arr = dashBoardTypeCodeList.toArray(arr);

		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == 0) {
				dashBoardTypeMap.put(arr[i], new HashMap<String, Object>());
			} else {
				dashBoardTypeMap.put(arr[i], getAllSelectionDashBoardView(userInfo, arr[i]).getBody());
			}
		}

		final Map<String, Object> objMapReturn = new LinkedHashMap<String, Object>();
		final List<Map<String, Object>> lstMap = new ArrayList<Map<String, Object>>();

		Integer pageArray[] = new Integer[dashBoardPageCodeList.size()];
		pageArray = dashBoardPageCodeList.toArray(pageArray);

		for (int ncount = 0; ncount < pageArray.length; ncount++) {
			final Map<String, Object> objMap = new LinkedHashMap<String, Object>();

			for (int j = 0; j < homePriorityList.size(); j++) {
				if (pageArray[ncount] == homePriorityList.get(j).getNdashboardhomepagecode()
						&& homePriorityList.get(j).getNdashboardtypecode() != 0) {
					objMap.put("dashBoardType" + (homePriorityList.get(j).getNpagedashboardindex()),
							dashBoardTypeMap.get(homePriorityList.get(j).getNdashboardtypecode()));
					objMap.put("ntemplatecode", homePriorityList.get(ncount).getNdashboardhometemplatecode());
				}
			}

			lstMap.add(objMap);
		}

		objMapReturn.put("homeDashBoard", lstMap);

		return new ResponseEntity<Object>(objMapReturn, HttpStatus.OK);
	}

	/**
	 * This method is used to get details to be loaded in combo of Add
	 * DashboardHomeConfig screen
	 */
	@Override
	public ResponseEntity<Object> getDashBoardHomePagesandTemplates(UserInfo userInfo) throws Exception {

		String strQuery = " select ndashboardhomepagecode, " + " coalesce(jsondata->'sdashboardhomepagename'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'sdashboardhomepagename'->>'en-US')  as sdashboardhomepagename  " + " and nsitecode = "
				+ userInfo.getNmastersitecode() + "from dashboardhomepages where  " + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ndashboardhomepagecode ";

		List<DashBoardHomePages> lstdashBoardHomePages = (List<DashBoardHomePages>) jdbcTemplate.query(strQuery,
				new DashBoardHomePages());

		strQuery = " select ndashboardhometemplatecode,ndashboardhomeorientation,"
				+ " coalesce(jsondata->'sdashboardhometemplatename'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'sdashboardhometemplatename'->>'en-US')  as sdashboardhometemplatename  "
				+ " from dashboardhometemplate where  " + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + " order by ndashboardhometemplatecode ";

		List<DashBoardHomeTemplate> lstdashBoardHomeTemplate = (List<DashBoardHomeTemplate>) jdbcTemplate
				.query(strQuery, new DashBoardHomeTemplate());

		List<DashBoardType> lstdashBoardType = getListDahsBoardTypes(userInfo);// , 0);

		final Map<String, Object> objMap = new HashMap<String, Object>();

		objMap.put("dashBoardHomePages", lstdashBoardHomePages);
		objMap.put("dashBoardHomeTemplate", lstdashBoardHomeTemplate);
		objMap.put("dashBoardType", lstdashBoardType);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This method is used to get list of dashboardtypes associated for the selected
	 * home dashboard config.
	 * 
	 * @param nprioritycode
	 * @return
	 * @throws Exception
	 */
	public List<DashBoardHomeTypes> getListDashBoardHomeConfig(final int nprioritycode) throws Exception {

		final String strQuery = " select b.*, c.sdashboardtypename from dashboardhomepriority a, dashboardhometypes b, dashboardtype c where a.ndashboardhomeprioritycode = b.ndashboardhomeprioritycode "
				+ "  and b.ndashboardtypecode = c.ndashboardtypecode " + "  and a.ndashboardhomeprioritycode = "
				+ nprioritycode + "  and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<DashBoardHomeTypes> mappingList = jdbcTemplate.query(strQuery, new DashBoardHomeTypes());
		return mappingList;

	}

	/**
	 * This method is used to retreive list of home dashboard config details.
	 */
	@Override
	public ResponseEntity<Object> getDashBoardHomeConfig(UserInfo userInfo) throws Exception {
		final String strQuery = "  select a.*, " + "  coalesce(b.jsondata->'sdashboardhomepagename'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "  b.jsondata->'sdashboardhomepagename'->>'en-US')  as sdashboardhomepagename , "
				+ "  coalesce(c.jsondata->'sdashboardhometemplatename'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "  c.jsondata->'sdashboardhometemplatename'->>'en-US')  as sdashboardhometemplatename , "
				+ "  e.suserrolename from dashboardhomepriority a,  dashboardhomepages b,  dashboardhometemplate c,  userrole e where  "
				+ "  a.ndashboardhomepagecode = b.ndashboardhomepagecode and a.ndashboardhometemplatecode = c.ndashboardhometemplatecode and a.nuserrolecode = e.nuserrolecode "
				+ "  and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and b.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and e.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.nsitecode = " + userInfo.getNmastersitecode();

		final List<DashBoardHomePriority> dashBoardHomePriorityForGrid = jdbcTemplate.query(strQuery,
				new DashBoardHomePriority());

		final Map<String, Object> objMap = new HashMap<String, Object>();
		objMap.put("DashBoardHomeConfig", dashBoardHomePriorityForGrid);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve detail of selected home dashboard config
	 * based on its primary key
	 * 
	 * @param nprioritycode
	 * @return
	 * @throws Exception
	 */
	public DashBoardHomePriority getSingleDashBoardHomeConfig(final int nprioritycode, UserInfo userInfo)
			throws Exception {

		final String strQuery = "  select a.*," + "  coalesce(b.jsondata->'sdashboardhomepagename'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "  b.jsondata->'sdashboardhomepagename'->>'en-US')  as sdashboardhomepagename , "
				+ "  coalesce(c.jsondata->'sdashboardhometemplatename'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "  c.jsondata->'sdashboardhometemplatename'->>'en-US')  as sdashboardhometemplatename , "
				+ "  e.suserrolename from dashboardhomepriority a,  dashboardhomepages b,  dashboardhometemplate c,  userrole e where  "
				+ "  a.ndashboardhomepagecode = b.ndashboardhomepagecode and a.ndashboardhometemplatecode = c.ndashboardhometemplatecode and a.nuserrolecode = e.nuserrolecode "
				+ "  and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and b.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and e.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and a.ndashboardhomeprioritycode = " + nprioritycode + " and a.nsitecode = "
				+ userInfo.getNmastersitecode();

		return (DashBoardHomePriority) jdbcUtilityFunction.queryForObject(strQuery, DashBoardHomePriority.class,
				jdbcTemplate);
	}

	/***
	 * This method is used to retrieve details to edit selected home dashboard
	 * config
	 */
	@Override
	public ResponseEntity<Object> getDashBoardHomeConfigByID(final int nprioritycode, UserInfo userInfo)
			throws Exception {

		final DashBoardHomePriority dashBoardHomePriority = getSingleDashBoardHomeConfig(nprioritycode, userInfo);

		final String sPage = commonFunction.getMultilingualMessage(dashBoardHomePriority.getSdashboardhomepagename(),
				userInfo.getSlanguagefilename());
		dashBoardHomePriority.setSdashboardhomepagename(sPage);

		final Map<String, Object> objMap = new HashMap<String, Object>();

		List<DashBoardHomeTypes> dashBoardTypes = getListDashBoardHomeConfig(nprioritycode);

		objMap.put("DashBoardHomeConfigByID", dashBoardHomePriority);
		objMap.put("DashBoardTypeNames", dashBoardTypes);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This method is used to create a new home dashboard config detail
	 */
	@Override
	public ResponseEntity<Object> createDashBoardHomePriority(DashBoardHomePriority dashBoardHomePriority,
			List<DashBoardHomeTypes> dashBoardHomeTypes, UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedDashBoardHomeList = new ArrayList<>();

		final List<Object> finalAuditList = new ArrayList<>();

		final String configQuery = "select * from dashboardhomepriority  where ndashboardhomepagecode="
				+ dashBoardHomePriority.getNdashboardhomepagecode() + " and nuserrolecode = "
				+ dashBoardHomePriority.getNuserrolecode() + " and nsitecode = " + userInfo.getNmastersitecode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<DashBoardHomePriority> configList = jdbcTemplate.query(configQuery, new DashBoardHomePriority());

		if (configList.isEmpty()) {
			dashBoardHomePriority.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
			dashBoardHomePriority.setNsitecode(userInfo.getNmastersitecode());
			dashBoardHomePriority = (DashBoardHomePriority) jdbcUtilityFunction.saveBatchOfObjects(
					(List<?>) dashBoardHomePriority, SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");

			final int npriorityCode = dashBoardHomePriority.getNdashboardhomeprioritycode();
			dashBoardHomeTypes = dashBoardHomeTypes.stream().map(obj -> {
				obj.setNdashboardhomeprioritycode(npriorityCode);
				return obj;
			}).collect(Collectors.toList());

			if (dashBoardHomeTypes.size() > 0) {
				dashBoardHomeTypes.stream().forEach(x -> {
					try {
						x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						x.setNsitecode(userInfo.getNmastersitecode());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
			dashBoardHomeTypes = (List<DashBoardHomeTypes>) jdbcUtilityFunction.saveBatchOfObjects(dashBoardHomeTypes,
					SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");

			savedDashBoardHomeList.add(dashBoardHomePriority);

			finalAuditList.add(savedDashBoardHomeList);
			finalAuditList.add(dashBoardHomeTypes);

			multilingualIDList.add("IDS_ADDDASHBOARDHOMECONFIG");
			multilingualIDList.add("IDS_ADDDASHBOARDHOMETYPES");

			auditUtilityFunction.fnInsertListAuditAction(finalAuditList, 1, null, multilingualIDList, userInfo);

		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		return getDashBoardHomeConfig(userInfo);
	}

	/**
	 * This method is used to update home dashboard config detail
	 */
	@Override
	public ResponseEntity<Object> updateDashBoardHomePriority(DashBoardHomePriority dashBoardHomePriority,
			List<DashBoardHomeTypes> dashBoardHomeTypes, UserInfo userInfo) throws Exception {

		final List<Object> beforeSavedDashBoardTypeList = new ArrayList<>();
		final List<Object> savedDashBoardTypeList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		final DashBoardHomePriority dashBoardHomePriorityByID = getSingleDashBoardHomeConfig(
				dashBoardHomePriority.getNdashboardhomeprioritycode(), userInfo);

		if (dashBoardHomePriorityByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String configQuery = "select * from dashboardhomepriority  where  ndashboardhomeprioritycode <> "
					+ dashBoardHomePriority.getNdashboardhomeprioritycode() + " and ndashboardhomepagecode="
					+ dashBoardHomePriority.getNdashboardhomepagecode() + " and nuserrolecode = "
					+ dashBoardHomePriority.getNuserrolecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<DashBoardHomePriority> configList = jdbcTemplate.query(configQuery, new DashBoardHomePriority());

			savedDashBoardTypeList.add(dashBoardHomePriority);

			if (configList.isEmpty()) {
				final int npriorityCode = dashBoardHomePriority.getNdashboardhomeprioritycode();

				final String updateQueryString = "update dashboardhomepriority set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ndashboardhometemplatecode="
						+ dashBoardHomePriority.getNdashboardhometemplatecode() + ", ndashboardhomepagecode= "
						+ dashBoardHomePriority.getNdashboardhomepagecode() + ", nuserrolecode= "
						+ dashBoardHomePriority.getNuserrolecode() + " where ndashboardhomeprioritycode="
						+ dashBoardHomePriority.getNdashboardhomeprioritycode();

				jdbcTemplate.execute(updateQueryString);

				beforeSavedDashBoardTypeList.add(dashBoardHomePriorityByID);

				final String queryStringProp = "select * from dashboardhometypes where ndashboardhomeprioritycode= "
						+ dashBoardHomePriority.getNdashboardhomeprioritycode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final List<DashBoardHomeTypes> deletedDashBoardHomeTypes = jdbcTemplate.query(queryStringProp,
						new DashBoardHomeTypes());

				String sQry = " delete from dashboardhometypes where ndashboardhomeprioritycode = "
						+ dashBoardHomePriority.getNdashboardhomeprioritycode();

				jdbcTemplate.execute(sQry);

				dashBoardHomeTypes = dashBoardHomeTypes.stream().map(obj -> {
					obj.setNdashboardhomeprioritycode(npriorityCode);
					return obj;
				}).collect(Collectors.toList());

				if (deletedDashBoardHomeTypes.size() > 0) {
					deletedDashBoardHomeTypes.stream().forEach(x -> {
						try {
							x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
							x.setNsitecode(userInfo.getNmastersitecode());
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
				dashBoardHomeTypes = (List<DashBoardHomeTypes>) jdbcUtilityFunction.saveBatchOfObjects(
						deletedDashBoardHomeTypes, SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");

				multilingualIDList.add("IDS_EDITDASHBOARDHOMECONFIG");

				auditUtilityFunction.fnInsertAuditAction(savedDashBoardTypeList, 2, beforeSavedDashBoardTypeList,
						multilingualIDList, userInfo);

				auditUtilityFunction.fnInsertListAuditAction(
						Arrays.asList(deletedDashBoardHomeTypes, dashBoardHomeTypes), 1, null,
						Arrays.asList("IDS_DELETEDASHBOARDHOMETYPES", "IDS_ADDDASHBOARDHOMETYPES"), userInfo);

				return getDashBoardHomeConfig(userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteDashBoardHomePriority(final DashBoardHomePriority dashBoardHomePriority,
			List<DashBoardHomeTypes> dashBoardHomeTypes, UserInfo userInfo) throws Exception {

		final DashBoardHomePriority dashBoardHomePriorityByID = getSingleDashBoardHomeConfig(
				dashBoardHomePriority.getNdashboardhomeprioritycode(), userInfo);

		if (dashBoardHomePriorityByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String rightsQuery = "select * from dashboardhomerights where " + " nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ndashboardhomeprioritycode="
					+ dashBoardHomePriority.getNdashboardhomeprioritycode();
			final DashBoardHomeRights dashBoardRights = (DashBoardHomeRights) jdbcUtilityFunction
					.queryForObject(rightsQuery, DashBoardHomeRights.class, jdbcTemplate);

			if (dashBoardRights == null) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedDashBoardTypeList = new ArrayList<>();
				final List<Object> finalAuditList = new ArrayList<>();

				String query = " update dashboardhomepriority set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where ndashboardhomeprioritycode=" + dashBoardHomePriority.getNdashboardhomeprioritycode();

				jdbcTemplate.execute(query);

				dashBoardHomePriority.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedDashBoardTypeList.add(dashBoardHomePriority);

				final String queryStringProp = "select * from dashboardhometypes where ndashboardhomeprioritycode= "
						+ dashBoardHomePriority.getNdashboardhomeprioritycode();

				final List<DashBoardHomeTypes> deletedDashBoardHomeTypes = (List<DashBoardHomeTypes>) jdbcTemplate
						.query(queryStringProp, new DashBoardHomeTypes());

				final String deleteQueryString = " Delete from DashBoardHomeTypes where  ndashboardhomeprioritycode= "
						+ dashBoardHomePriority.getNdashboardhomeprioritycode();
				jdbcTemplate.execute(deleteQueryString);

				multilingualIDList.add("IDS_DELETEDASHBOARDHOMETYPES");
				multilingualIDList.add("IDS_DELETEDASHBOARDHOMECONFIG");

				finalAuditList.add(deletedDashBoardHomeTypes);
				finalAuditList.add(savedDashBoardTypeList);

				auditUtilityFunction.fnInsertListAuditAction(finalAuditList, 1, null, multilingualIDList, userInfo);

				return getDashBoardHomeConfig(userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DASHBOARDHOMECONFIGUSEDINRIGHTS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> updateDashBoardDesignConfig(List<DashBoardDesignConfig> dashBoardDesignConfig,
			final int ndashBoardTypeCode, UserInfo userInfo) throws Exception {

		if (dashBoardDesignConfig != null && dashBoardDesignConfig.size() > 0) {

		    //Commented By Rajesh Kumar during Review 
			
			/*final String queryStringProp = "select * from dashboarddesignconfig where ndashboardtypecode= "
					+ ndashBoardTypeCode;
			final List<DashBoardDesignConfig> beforeupdateDashboarddesignconfig = jdbcTemplate.query(queryStringProp,
					new DashBoardDesignConfig());
					*/

			String updateQueryString = "";
			for (int i = 0; i < dashBoardDesignConfig.size(); i++) {

				if (dashBoardDesignConfig.get(i).getNdesigncomponentcode() != Enumeration.DesignComponent.DATEFEILD
						.gettype()) {
					updateQueryString = updateQueryString + ";update dashboarddesignconfig set  dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',sdefaultvalue=N'"
							+ dashBoardDesignConfig.get(i).getSdefaultvalue() + "' where ndashboardtypecode = "
							+ dashBoardDesignConfig.get(i).getNdashboardtypecode() + " and ndashboarddesigncode="
							+ dashBoardDesignConfig.get(i).getNdashboarddesigncode();
				}
			}

			jdbcTemplate.execute(updateQueryString);

		}
		return getDashBoardTypes(userInfo, ndashBoardTypeCode);
	}

}
