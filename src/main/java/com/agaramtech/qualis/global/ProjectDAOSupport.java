
package com.agaramtech.qualis.global;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.beanutils.BeanUtils;
import org.javers.common.collections.Arrays;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.agaramtech.qualis.configuration.model.DeleteValidation;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.configuration.model.FilterDetail;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.configuration.model.GenericLabel;
import com.agaramtech.qualis.configuration.model.Language;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.configuration.model.UserFormControlProperties;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.global.Enumeration.EntityMapper;
import com.agaramtech.qualis.login.model.UserMFA;
import com.agaramtech.qualis.registration.model.SeqNoArnoGenerator;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ProjectDAOSupport // extends AuditUtilityFunction
{

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDAOSupport.class);

	private static final DateTimeFormatter FORMATTER1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	private final StringUtilityFunction stringUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	// private final ClassUtilityFunction classUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	// private final AuditUtilityFunction auditUtilityFunction;
	private final ClassUtilityFunction classUtilityFunction;
	private final SQLQueryCreator sqlQueryCreator;
	private final JwtUtilityFunction jwtUtilityFunction;

	// private final static String OS = System.getProperty("os.name").toLowerCase();
	// public final static boolean IS_WINDOWS = (OS.indexOf("win") >= 0);
	// public final static boolean IS_UNIX = (OS.indexOf("nix") >= 0 ||
	// OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);

	// added by Sudharshanan for rollback issue instead of batch execution
	public void executeBulkDatainSingleInsert(String[] executionStringArray) throws Exception {

		for (int i = 0; i < executionStringArray.length; i++) {
			String eachStringFromLoop = executionStringArray[i];
			jdbcTemplate.batchUpdate(eachStringFromLoop);
		}

	}

	public Map<String, Object> getMutlipleEntityResultsUsingPlainSql(String sql, JdbcTemplate jdbcTemplate,
			Object... entityTypes) {
		Object[] entities = entityTypes;
		return jdbcTemplate.execute(new CallableStatementCreator() {
			@Override
			public CallableStatement createCallableStatement(Connection con) throws SQLException {
				return con.prepareCall(sql);
			}
		}, new CallableStatementCallback<Map<String, Object>>() {
			@Override
			public Map<String, Object> doInCallableStatement(CallableStatement callableStatement)
					throws SQLException, DataAccessException {
				Map<String, Object> rowMap = new TreeMap<String, Object>();
				ResultSet collectResult = null;
				try {
					Class<?>[] cArgs = new Class[1];
					cArgs[0] = CustomizedResultsetRowMapper.class;
					boolean resultsAvailable = callableStatement.execute();
					int j = 0;
					while (resultsAvailable) {
						collectResult = callableStatement.getResultSet();
						Class<?> entityClass = null;
						String key = null;
						boolean flag = false;
						Method lMethod = null;
						List<Object> listOfEntity = null;
						if (entities[j] instanceof Class) {
							entityClass = (Class.forName(((Class<?>) entities[j]).getName()));
							flag = true;
						} else {
							key = (String) entities[j];
							flag = false;
						}
						CustomizedResultsetRowMapper<Object> customMapper = new CustomizedResultsetRowMapper<Object>();
						if (flag) {
							listOfEntity = new ArrayList<Object>();
							lMethod = entityClass.getDeclaredMethod(EntityMapper.MAPROW.getmappername(), cArgs);
							customMapper.init(collectResult);
							while (customMapper.resultSet.next()) {
								listOfEntity.add(lMethod.invoke(
										classUtilityFunction.getNewInstanceOfAnClass(entityClass.getName()),
										customMapper));
							}
							rowMap.put(entityClass.getSimpleName(), listOfEntity);
						} else {
							rowMap.put(key, customMapper.convertList(collectResult));
						}
						resultsAvailable = callableStatement.getMoreResults();
						customMapper.availableColumns = null;
						j++;
					}
				} catch (Exception e) {
					System.out.println("Exception e:" + e.getLocalizedMessage());
				}
				return rowMap;
			}
		});
	}

	public Map<String, List<?>> getMutlipleEntityResultsInListUsingPlainSql(String sql, JdbcTemplate jdbcTemplate,
			Class<?>... entityClass) {
		List<?> entityClasses = Arrays.asList(entityClass);
		return jdbcTemplate.execute(new CallableStatementCreator() {
			@Override
			public CallableStatement createCallableStatement(Connection con) throws SQLException {
				return con.prepareCall(sql);
			}
		}, new CallableStatementCallback<Map<String, List<?>>>() {
			@Override
			public Map<String, List<?>> doInCallableStatement(CallableStatement callableStatement)
					throws SQLException, DataAccessException {
				Map<String, List<?>> rowMap = new TreeMap<String, List<?>>();
				ResultSet collectResult = null;
				try {					
					boolean resultsAvailable = callableStatement.execute();
					int j = 0;
					while (resultsAvailable) {
						collectResult = callableStatement.getResultSet();
						Class<?> entityClass = null;
						Method lMethod = null;
						List<Object> listOfEntity = null;
						entityClass = (Class.forName(((Class<?>) entityClasses.get(j)).getName()));
						CustomizedResultsetRowMapper<Object> customMapper = new CustomizedResultsetRowMapper<Object>();
						listOfEntity = new ArrayList<Object>();
						lMethod = entityClass.getDeclaredMethod(EntityMapper.MAPROW.getmappername(), ResultSet.class,int.class);
						customMapper.init(collectResult);
						while (customMapper.resultSet.next()) {
							listOfEntity.add(lMethod.invoke(
									classUtilityFunction.getNewInstanceOfAnClass(entityClass.getName()), customMapper));
						}
						rowMap.put(entityClass.getSimpleName(), listOfEntity);
						resultsAvailable = callableStatement.getMoreResults();
						customMapper.availableColumns = null;
						j++;
					}
				} catch (Exception e) {
					System.out.println("Exception e:" + e.getLocalizedMessage());
				}
				return rowMap;
			}
		});
	}

	public List<?> getMultipleEntitiesResultSetInList(String sql, JdbcTemplate jdbcTemplate, Class<?>... entities) {
		List<?> lstOfEntities = Arrays.asList(entities);
		return jdbcTemplate.execute(new CallableStatementCreator() {
			@Override
			public CallableStatement createCallableStatement(Connection con) throws SQLException {
				return con.prepareCall(sql);
			}
		}, new CallableStatementCallback<List<?>>() {
			@Override
			public List<?> doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
				List<Object> rowMap = new ArrayList<>();
				ResultSet collectResult = null;
				try {
					// Class<?>[] cArgs = new Class[1];
					// cArgs[0] = CustomizedResultsetRowMapper.class;
					boolean resultsAvailable = cs.execute();
					int j = 0;
					while (resultsAvailable) {
						collectResult = cs.getResultSet();
						Class<?> entityClass = null;
						Method lMethod = null;
						entityClass = (Class.forName(((Class<?>) lstOfEntities.get(j)).getName()));
						List<Object> listOfEntity = new ArrayList<Object>();
						lMethod = entityClass.getDeclaredMethod(EntityMapper.MAPROW.getmappername(), ResultSet.class,
								int.class);
						CustomizedResultsetRowMapper<Object> genericEntityObject = new CustomizedResultsetRowMapper<Object>();
						genericEntityObject.init(collectResult);
						int rowCount = 0;
						while (genericEntityObject.resultSet.next()) {
							listOfEntity.add(
									lMethod.invoke(classUtilityFunction.getNewInstanceOfAnClass(entityClass.getName()),
											genericEntityObject.resultSet, rowCount));
							rowCount++;
						}
						rowMap.add(listOfEntity);
						resultsAvailable = cs.getMoreResults();
						genericEntityObject.availableColumns = null;
						j++;
					}
				} catch (Exception e) {
					LOGGER.error("getMultipleEntitiesResultSetInList()--> " + e.getLocalizedMessage());
					LOGGER.error("executePlainProcedure List args e.getMessage()--> " + e.getMessage());
					LOGGER.error("executePlainProcedure List args e.getCause()--> " + e.getCause());
					throw new SQLException(e.getLocalizedMessage());
				} finally {
				}
				return rowMap;
			}
		});
	}

	public List<GenericLabel> getGenericLabel() throws Exception {
		final String sQuery = "select ngenericlabelcode, sgenericlabel, sidsfieldname, jsondata, "
				+ " nsitecode, nstatus from genericlabel where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<GenericLabel> genericLabelList = (List<GenericLabel>) jdbcTemplate.query(sQuery, new GenericLabel());
		return genericLabelList;
	}

	public String getSpecificGenericLabel(List<GenericLabel> genericLabelList, UserInfo userInfo, String labelName)
			throws Exception {
		final Map<String, GenericLabel> genericLabelMap = genericLabelList.stream()
				.collect(Collectors.toMap(GenericLabel::getSgenericlabel, genericLabel -> genericLabel));
		final GenericLabel genericLabel = (GenericLabel) genericLabelMap.get(labelName);
		final Map<String, Object> jsonLabel = genericLabel.getJsondata();
		final Map<String, Object> displayName = (Map<String, Object>) jsonLabel.get("sdisplayname");
		final String multilingualData = (String) displayName.get(userInfo.getSlanguagetypecode());
		return multilingualData;
	}

	/**
	 * This method is used to validate the master data in respective transaction
	 * tables It returns the ValidateDel object which contains returnstatus and
	 * returnmessage
	 *
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public ValidatorDel getTransactionInfo(String query, UserInfo objUserInfo) throws Exception {
		ValidatorDel objReturnMsg = new ValidatorDel();

		LOGGER.info("getTransactionInfo");
		String strLanguage = "select nlanguagecode, slanguagename, sfilename, slanguagetypecode, sreportingtoolfilename,"
				+ " ndefaultstatus, nsitecode," + " nstatus from Language where slanguagetypecode='"
				+ objUserInfo.getSlanguagetypecode() + "'";
		Language objLanguage = (Language) jdbcUtilityFunction.queryForObject(strLanguage, Language.class, jdbcTemplate);
		final ResourceBundle resourcebundle = commonFunction.getResourceBundle(objLanguage.getSfilename(), false);
		List<GenericLabel> genericLabelList = getGenericLabel();
		Map<String, GenericLabel> genericLabelMap = genericLabelList.stream()
				.collect(Collectors.toMap(GenericLabel::getSidsfieldname, genericLabel -> genericLabel));
		String returnStatus = genericLabelMap.containsKey("IDS_THISISUSEDIN")
				? (String) ((Map<String, Object>) (((genericLabelMap.get("IDS_THISISUSEDIN")).getJsondata())
						.get("sdisplayname"))).get(objUserInfo.getSlanguagetypecode()) + " "
				: resourcebundle.containsKey("IDS_THISISUSEDIN") ? resourcebundle.getString("IDS_THISISUSEDIN") + " "
						: "This Record is used in ";
		List<Map<String, Object>> transIn = jdbcTemplate.queryForList("select distinct * from (" + query + " ) a");
		if (transIn != null && transIn.size() > 0) {
			String transInfo = "";
			for (int index = 0; index < transIn.size(); index++) {
				transInfo = genericLabelMap.containsKey(transIn.get(index).get("Msg").toString())
						? transInfo + (String) ((Map<String, Object>) (((genericLabelMap
								.get(transIn.get(index).get("Msg").toString())).getJsondata()).get("sdisplayname")))
								.get(objUserInfo.getSlanguagetypecode()) + " / "
						: resourcebundle.containsKey(transIn.get(index).get("Msg").toString())
								? transInfo + resourcebundle.getString(transIn.get(index).get("Msg").toString()) + " / "
								: transInfo + transIn.get(index).get("Msg").toString() + " / ";
			}
			transInfo = transInfo.substring(0, transInfo.lastIndexOf("/"));
			objReturnMsg.setNreturnstatus(Enumeration.Deletevalidator.ERORR.getReturnvalue());
			objReturnMsg.setSreturnmessage(returnStatus + " " + transInfo);
		} else {
			objReturnMsg.setNreturnstatus(Enumeration.Deletevalidator.SUCCESS.getReturnvalue());
			objReturnMsg.setSreturnmessage("Success");
		}
		return objReturnMsg;
	}

	/**
	 * 
	 * @param tableName
	 * @param primaryKeyValue
	 * @param staticTableMap
	 * @param jsonTableMap
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	public ValidatorDel validateDeleteRecord(final String primaryKeyValue, final UserInfo userInfo) throws Exception {

		String query = "select * from deletevalidation where nformcode=" + userInfo.getNformcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<DeleteValidation> deleteValidationList = (List<DeleteValidation>) jdbcTemplate.query(query,
				new DeleteValidation());

		valiDatorDel.setSreturnmessage("Success");
		valiDatorDel.setNreturnstatus(Enumeration.Deletevalidator.SUCCESS.getReturnvalue());

		if (deleteValidationList.isEmpty()) {
			return valiDatorDel;
		} else {
			final List<String> queryList = new ArrayList<String>();
			/**
			 * Delete validation for static tables
			 */
			for (DeleteValidation validation : deleteValidationList) {
				/**
				 * Sample Basic Query Format
				 *
				 * SELECT r.npreregno, d.key, d.value FROM registration r JOIN
				 * jsonb_each(r.jsond ata) d ON true where d.value->>'pkey' ='nunitcode' and
				 * d.value->>'nquerybuildertablecode'='120' and d.value->>'value'='1';
				 */

				query = " SELECT (select jsondata ->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
						+ "' from qualisforms where nformcode= " + validation.getNtransformcode() + ")"

						+ " Msg FROM " + validation.getStranstablename() + " r " + " JOIN jsonb_each(r."
						+ validation.getSjsonfieldname() + ") d ON true where " + " d.value->>'pkey' ='"
						+ validation.getSmasterprimarykeyname() + "'" + " and d.value->>'nquerybuildertablecode'= "
						+ " (select nquerybuildertablecode::varchar(10) from querybuildertables " + " where nformcode="
						+ userInfo.getNformcode() + " and stablename='" + validation.getSmastertablename() + "') "
						+ " and d.value->>'value'='" + primaryKeyValue + "'"

						+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				queryList.add(query);
			}
			query = String.join(" Union All ", queryList);

			if (query != null && query.trim().length() != 0) {
				valiDatorDel = getTransactionInfo(query, userInfo);
			}
			return valiDatorDel;
		}
	}

	/**
	 * This method is used to create or get the version
	 *
	 * @param nformmCode    [int] form code of the version to be created
	 * @param lastLevelCode [int]
	 * @param sprimaryKey   [String]
	 * @param className     [String]
	 * @param siteCode      [int]
	 * @return an Object contains version no and version description i.e version
	 *         with specified format
	 * @throws Exception
	 */

	public Object fnGetVersion(int nformmCode, int lastLevelCode, String sprimaryKey, Class<?> className, int siteCode,
			UserInfo userInfo) throws Exception {

		Object rtnObject = null;

		final String strchk = nformmCode + "_" + lastLevelCode + "_" + siteCode;
		final String validateQuery = "select * from " + className.getAnnotation(Table.class).name()
				+ " where stablename ='" + strchk + "'";
		final Object obj = jdbcUtilityFunction.queryForObject(validateQuery, className, jdbcTemplate);
		if (obj != null) {
			final int nseq = Integer.parseInt(BeanUtils.getProperty(obj, "nsequenceno"));
			final int nsequpdate = nseq + 1;
			final String updateQuery = "update " + className.getAnnotation(Table.class).name() + " set nsequenceno="
					+ nsequpdate + " where stablename='" + strchk + "'";
			jdbcTemplate.execute(updateQuery);

//			final BeanGenerator onj = new BeanGenerator();
//			onj.addProperty("versionno", Integer.class);
//			onj.addProperty("versiondes", String.class);
//
//			final Object objnew = onj.create();
//			BeanUtils.setProperty(objnew, "versionno", nsequpdate);
//			BeanUtils.setProperty(objnew, "versiondes", nsequpdate);

			Map<String, Object> objnew = new HashMap<>();
			objnew.put("versionno", nsequpdate);
			objnew.put("versiondes", nsequpdate);
			rtnObject = objnew;
		} else {
			final String insertQuery = "insert into " + className.getAnnotation(Table.class).name()
					+ " (stablename,nsequenceno,dmodifieddate,nsitecode,nstatus) values( '" + strchk + "',"
					+ Enumeration.Sequenceintialvalue.ONE.getSequence() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + siteCode + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertQuery);

//			final BeanGenerator beanGenerator = new BeanGenerator();
//			beanGenerator.addProperty("versionno", Integer.class);
//			beanGenerator.addProperty("versiondes", String.class);

//			final Object newObject = beanGenerator.create();
//			System.out.println("Object:" + newObject);

			Map<String, Object> newObject = new HashMap<>();
			newObject.put("versionno", Enumeration.Sequenceintialvalue.ONE.getSequence());
			newObject.put("versiondes", Enumeration.Sequenceintialvalue.ONE.getSequence());

//			BeanUtils.setProperty(newObject, "versionno", Enumeration.Sequenceintialvalue.ONE.getSequence());
//			BeanUtils.setProperty(newObject, "versiondes", Enumeration.Sequenceintialvalue.ONE.getSequence());
			rtnObject = newObject;
		}
		return rtnObject;
	}

	// ALPD-4513--Vignesh R(29-07-2024)--This method used for delete validation in
	// one-to-many relationship tables

	public ValidatorDel validateOneToManyDeletion(final Map<String, Object> objOneToManyValidation,
			final UserInfo userInfo) throws Exception {

		String query = "select * from deletevalidation where nformcode=" + userInfo.getNformcode()
				+ " and smastertablename='" + objOneToManyValidation.get("stablename") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<DeleteValidation> deleteValidationList = (List<DeleteValidation>) jdbcTemplate.query(query,
				new DeleteValidation());

		ValidatorDel objDeleteValidation = new ValidatorDel() {
			@Override
			public void setSreturnmessage(String sreturnmessage) {
				sreturnmessage = "Sucess";
			}

			@Override
			public void setNreturnstatus(int nreturnstatus) {
				// TODO Auto-generated method stub
				nreturnstatus = Enumeration.Deletevalidator.SUCCESS.getReturnvalue();
			}
		};

		if (deleteValidationList.isEmpty()) {
			return objDeleteValidation;
		} else {
			final List<String> queryList = new ArrayList<String>();
			/**
			 * Delete validation for static tables
			 */
			for (DeleteValidation validation : deleteValidationList) {
				/**
				 * Sample Basic Query Format
				 *
				 * SELECT r.npreregno, d.key, d.value FROM registration r JOIN
				 * jsonb_each(r.jsond ata) d ON true where d.value->>'pkey' ='nunitcode' and
				 * d.value->>'nquerybuildertablecode'='120' and d.value->>'value'='1';
				 */

				query = " SELECT (select jsondata ->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
						+ "' from qualisforms where nformcode= " + validation.getNtransformcode() + ")" + " Msg FROM "
						+ validation.getStranstablename() + " r " + " JOIN jsonb_each(r."
						+ validation.getSjsonfieldname() + ") d ON true where " + " d.value->>'pkey' ='"
						+ validation.getSmasterprimarykeyname() + "'" + " and d.value->>'nquerybuildertablecode'= "
						+ " (select nquerybuildertablecode::varchar(10) from querybuildertables " + " where nformcode="
						+ userInfo.getNformcode() + " and stablename='" + validation.getSmastertablename() + "') "
						+ " and d.value->>'value'='" + objOneToManyValidation.get("primaryKeyValue") + "'"
						+ " and r.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				queryList.add(query);
			}
			query = String.join(" Union All ", queryList);

			if (query != null && query.trim().length() != 0) {
				objDeleteValidation = getTransactionInfo(query, userInfo);
			}
			return objDeleteValidation;
		}
	}

	// Added by L.Subashini on 12/11/2020
	public Map<String, Object> getDateFromControlProperties(final UserInfo userInfo, final String currentUIDate,
			final String dateType, final String controlName) throws Exception {


		//L.Subashini 
		//Commenting mastersitecode in where clause of userformcontrolproperties as it is a backend table
		final String controlQuery = "select ufcp.*, p.jsondata->'ndata' as ndata , cm.scontrolname "
									+ " from controlmaster cm, userformcontrolproperties ufcp, period p "
									+ " where cm.ncontrolcode = ufcp.ncontrolcode"
									+ " and p.nperiodcode = ufcp.nperiodcode "
									+ " and p.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and cm.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ufcp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ufcp.nformcode = " + userInfo.getNformcode()
									//+ " and ufcp.nsitecode = " + userInfo.getNmastersitecode()
									+ " and cm.scontrolname='" + controlName + "'";

		final UserFormControlProperties controlProperties = (UserFormControlProperties) jdbcUtilityFunction
				.queryForObject(controlQuery, UserFormControlProperties.class, jdbcTemplate);

		Instant instantDate = null;
		final Map<String, Object> returnObject = new HashMap<String, Object>();

		switch (dateType) {
			case "datetime":
				instantDate = dateUtilityFunction.convertStringDateToUTC(currentUIDate, userInfo, false);
				final Instant startDate = instantDate
						.plus((controlProperties.getNdata() * controlProperties.getNwindowperiod()), ChronoUnit.MINUTES)
						.truncatedTo(ChronoUnit.DAYS);
	
				final Instant endDate = instantDate.truncatedTo(ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS)
						.plus(59, ChronoUnit.MINUTES).plus(59, ChronoUnit.SECONDS);
	
				final String fromDate = dateUtilityFunction.instantDateToString(startDate);
				returnObject.put("FromDate", fromDate);
				returnObject.put("ToDate", dateUtilityFunction.instantDateToString(endDate));
	
				final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final LocalDateTime ldt = LocalDateTime.parse(fromDate, formatter1);
				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
	
				String formattedString = "";
				if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final ZonedDateTime zonedDateTime = ZonedDateTime.of(ldt, ZoneId.of(userInfo.getStimezoneid()));
					formattedString = zonedDateTime.format(formatter);
				} else {
					formattedString = formatter.format(ldt);
				}
				final LocalDateTime ldt1 = LocalDateTime.parse(currentUIDate,
						DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
				returnObject.put("FromDateWOUTC", formattedString);
				returnObject.put("ToDateWOUTC",
						formatter.format(ldt1.toLocalDate().atTime(23, 59, 59).truncatedTo(ChronoUnit.SECONDS)));
	
				break;
			case "expirydate":
	
				Instant instDate = dateUtilityFunction.getCurrentDateTime(userInfo);
				final Instant validityStartDate = instDate.truncatedTo(ChronoUnit.SECONDS);
				final Instant expiryDate = instDate
						.plus((controlProperties.getNdata() * controlProperties.getNwindowperiod()), ChronoUnit.MINUTES)
						.truncatedTo(ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS).plus(59, ChronoUnit.MINUTES)
						.plus(59, ChronoUnit.SECONDS);
				// .truncatedTo(ChronoUnit.SECONDS);
	
				final String validityExpiryDate = dateUtilityFunction.instantDateToString(expiryDate);
				returnObject.put("ExpiryDate", validityExpiryDate);
				returnObject.put("ValidityStartDate", dateUtilityFunction.instantDateToString(validityStartDate));
	
				returnObject.put("ValidityExpiryWOUTC", dateUtilityFunction.convertUTCToSiteTime(expiryDate, userInfo));
				returnObject.put("ValidityStartWOUTC",
						dateUtilityFunction.convertUTCToSiteTime(validityStartDate, userInfo));
	
				break;
			// case "expirydate":
			//
			// final Instant validityStartDate =
			// instantDate.truncatedTo(ChronoUnit.SECONDS);
			// final Instant expiryDate = instantDate.plus((controlProperties.getNdata() *
			// controlProperties.getNwindowperiod()),
			// ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS);
			//
			// final String validityExpiryDate =
			// commonFunction.instantDateToString(expiryDate);
			// returnObject.put("ExpiryDate", validityExpiryDate );
			// returnObject.put("ValidityStartDate",
			// commonFunction.instantDateToString(validityStartDate));
			//
			// final DateTimeFormatter formatter3 =
			// DateTimeFormatter.ofPattern(userInfo.getSsitedatetime());
			//
			// final LocalDateTime ldt3 = LocalDateTime.parse(currentUIDate,
			// DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
			// returnObject.put("ValidityStartWOUTC",
			// formatter3.format(ldt3.truncatedTo(ChronoUnit.SECONDS)));
			//
			// String formattedString1 = "";
			// if(userInfo.getIsutcenabled()==Enumeration.TransactionStatus.YES.gettransactionstatus())
			// {
			// final LocalDateTime ldt4 =
			// expiryDate.atZone(ZoneId.of(userInfo.getStimezoneid())).toLocalDateTime();
			// formattedString1 = formatter3.format(ldt4.truncatedTo(ChronoUnit.SECONDS));
			// }
			// else {
			// //final LocalDateTime ldate =
			// LocalDateTime.parse(commonFunction.instantDateToString(expiryDate),
			// formatter3);
			// // formattedString1 =formatter3.format(ldate);
			// final LocalDateTime ldate = LocalDateTime.parse(validityExpiryDate,
			// DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
			// formattedString1 =formatter3.format(ldate);
			// }
			// returnObject.put("ValidityExpiryWOUTC", formattedString1);
			//// final LocalDateTime ldt4 =
			// expiryDate.atZone(ZoneId.of(userInfo.getStimezoneid())).toLocalDateTime();
			//// returnObject.put("ValidityExpiryWOUTC",
			// formatter3.format(ldt4.truncatedTo(ChronoUnit.SECONDS)));
			// break;
			//
			default:
	
				Instant instDateValue = dateUtilityFunction.getCurrentDateTime(userInfo);
				final Instant expiryDateValue = instDateValue
						.plus((controlProperties.getNdata() * controlProperties.getNwindowperiod()), ChronoUnit.MINUTES)
						.truncatedTo(ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS).plus(59, ChronoUnit.MINUTES)
						.plus(59, ChronoUnit.SECONDS);
				returnObject.put(controlProperties.getScontrolname(),
						dateUtilityFunction.instantDateToString(expiryDateValue));
		}

		return returnObject;
	}

	/**
	 * This method is used to decrypt the cipher text password to a plain text
	 * password .
	 * 
	 * @author Sudharshanan
	 * @param stablename      a String value, table name where the encrypted
	 *                        password need to be fetched.
	 * @param sprimaryField   a String value,primary key field name of the table
	 *                        where the encrypted password need to be fetched.
	 * @param nprimaryValue   a String value,primary key value of the record where
	 *                        the encrypted password need to be fetched.
	 * @param spasswordcolumn a String value,column name of the table where the
	 *                        encrypted password need to be fetched.
	 * @return the plain text password as a String.
	 * @throws Exception
	 */
	public String decryptPassword(String stablename, String sprimaryField, int nprimaryValue, String spasswordcolumn)
			throws Exception {

		final String getPassQry = "select " + sprimaryField + "," + spasswordcolumn + " as spassword from " + stablename
				+ " where " + sprimaryField + "=" + nprimaryValue + ";";
		List<Map<String, Object>> lstResult = jdbcTemplate.queryForList(getPassQry);
		String sPassword = "";
		if (!lstResult.isEmpty()) {
			Map<String, Object> objMap = lstResult.get(0);
			sPassword = (String) objMap.get("spassword");
		}
		if (sPassword != null) {
			byte[] messageInBytes = decode(sPassword);
			Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
			GCMParameterSpec spec = new GCMParameterSpec(128, getIV("agaraminitvector"));
			decryptionCipher.init(Cipher.DECRYPT_MODE, getKey("smilsilauqsymmetrickey"), spec);
			byte[] decryptedBytes = decryptionCipher.doFinal(messageInBytes);
			return new String(decryptedBytes);
		} else {
			return null;
		}
	}

	/**
	 * This method is used to encrypt the plain text password to a encrypted
	 * password and update the encrypted password to the corresponding column of the
	 * table.
	 * 
	 * @param stablename      a String value, table name where the encrypted
	 *                        password need to be updated.
	 * @param sprimaryField   a String value,primary key field name of the table
	 *                        where the encrypted password need to be updated.
	 * @param nprimaryValue   a String value,primary key value of the record where
	 *                        the encrypted password need to be updated.
	 * @param sPassword       a String value,plain text password.
	 * @param spasswordColumn a String value,column name of the table where the
	 *                        encrypted password need to be updated.
	 * @throws Exception
	 */
	public void encryptPassword(String stablename, String sprimaryField, int nprimaryValue, String sPassword,
			String spasswordColumn) throws Exception {

		byte[] sPasswordBytes = sPassword.getBytes();
		Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec spec = new GCMParameterSpec(128, getIV("agaraminitvector"));
		encryptionCipher.init(Cipher.ENCRYPT_MODE, getKey("smilsilauqsymmetrickey"), spec);
		byte[] encryptedBytes = encryptionCipher.doFinal(sPasswordBytes);
		String encryptedPassword = encode(encryptedBytes);
		final String updateQry = "update " + stablename + " set " + spasswordColumn + " = N'" + encryptedPassword
				+ "'  WHERE " + sprimaryField + " = " + nprimaryValue + ";";
		jdbcTemplate.execute(updateQry);

	}

	/**
	 * This method is used to convert the byte array to the string.
	 * 
	 * @param data a byte array
	 * @return a String value
	 */
	private String encode(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	/**
	 * This method is used to convert a string to the byte array.
	 * 
	 * @param data a string value
	 * @return a byte array
	 */
	private byte[] decode(String data) {
		return Base64.getMimeDecoder().decode(data);
	}

	/**
	 * This method is used to get the byte array for Initialization vector string.
	 * 
	 * @param IV a String to store a Initialization vector
	 * @return a byte array of Initialization vector String
	 */
	public byte[] getIV(String IV) {
		return decode(IV);
	}

	/**
	 * This method is used to create a new Secret key spec for the cipher.
	 * 
	 * @param secretKey
	 * @return SecretKeySpec
	 */
	public SecretKeySpec getKey(String secretKey) {
		return new SecretKeySpec(decode(secretKey), "AES");
	}

	public Map<String, Object> getTemplateDesign(int ndesigntemplatemappingcode, int nformcode) throws Exception {
		String str = "";
		if (nformcode == 40 || nformcode == 138) {
			str = "select jsondata->'" + nformcode + "' as jsondata from mappedtemplatefieldpropsmaterial"
					+ " where nmaterialconfigcode=" + ndesigntemplatemappingcode + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		} else if (nformcode == -1 || nformcode == Enumeration.QualisForms.GOODSIN.getqualisforms()
				|| nformcode == Enumeration.QualisForms.PROTOCOL.getqualisforms()) {
			// dynamicmaster
			str = "select jsondata from mappedtemplatefieldprops" + " where ndesigntemplatemappingcode="
					+ ndesigntemplatemappingcode + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		} else {

			str = "select jsondata->'" + nformcode + "' as jsondata from mappedtemplatefieldprops"
					+ " where ndesigntemplatemappingcode=" + ndesigntemplatemappingcode + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		try {
			return jdbcTemplate.queryForMap(str);
		} catch (Exception e) {
			return null;
		}

	}

	public List<Map<String, Object>> getSiteLocalTimeFromUTCForDynamicTemplate(final String sb, final UserInfo userinfo,
			final boolean isdbdateformat, int ndesigntemplatemappingcode, String selectionKeyName) throws Exception {
		List<Map<String, Object>> finalList = new ArrayList<>();
		if ((sb != null) && sb.length() > 0) {
			ObjectMapper Objmapper = new ObjectMapper();
			Objmapper.registerModule(new JavaTimeModule());
			List<Map<String, Object>> lst = Objmapper.readValue(sb.toString(),
					new TypeReference<List<Map<String, Object>>>() {
					});
			Map<String, Object> datefields = getTemplateDesign(ndesigntemplatemappingcode, userinfo.getNformcode());
			if (datefields == null) {
				return lst;
			}
			datefields = new JSONObject(((PGobject) datefields.get("jsondata")).getValue()).toMap();
			List<Map<Object, Object>> dateFields = new ArrayList<Map<Object, Object>>();
			if (datefields.containsKey(selectionKeyName + "datefields")) {
				dateFields = Objmapper.convertValue(datefields.get(selectionKeyName + "datefields"),
						new TypeReference<List<Map<Object, Object>>>() {
						});
			}

			if (!dateFields.isEmpty()) {
				DateTimeFormatter destFormat = DateTimeFormatter.ofPattern(userinfo.getSsitedatetime());
				SimpleDateFormat sourceFormat = new SimpleDateFormat(userinfo.getSsitedatetime().replace("'T'", " "));
				if (isdbdateformat) {
					sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				}

				for (int i = 0; i < lst.size(); i++) {
					Map<String, Object> data = lst.get(i);
					for (int j = 0; j < dateFields.size(); j++) {

						final String dateFieldName = (String) dateFields.get(j).get("2");
						boolean dateOnly = (boolean) dateFields.get(j).get("dateonly");
						if (!data.containsKey(dateFieldName)) {
							if (data.containsKey("jsondata")) {
								Map<String, Object> jsonData = (Map<String, Object>) data.get("jsondata");
								if (jsonData.containsKey(dateFieldName)) {
									if ((String) jsonData.get(dateFieldName) != null
											&& !jsonData.get(dateFieldName).equals("")
											&& !jsonData.get(dateFieldName).equals("-")) {
										if (userinfo.getIsutcenabled() == Enumeration.TransactionStatus.YES
												.gettransactionstatus()) {
											if (jsonData.containsKey("ntz" + dateFieldName)) {
												final LocalDateTime ldt = sourceFormat
														.parse((String) jsonData.get(dateFieldName)).toInstant()
														.atOffset(ZoneOffset.ofTotalSeconds(
																(int) jsonData.get("noffset" + dateFieldName)))
														.toLocalDateTime();
												jsonData.put(
														dateFieldName, dateOnly
																? DateTimeFormatter.ofPattern(userinfo.getSsitedate())
																		.format(ldt)
																: destFormat.format(ldt));
											} else if (!jsonData.containsKey("noffset" + dateFieldName)) {

											} else {
												if (!jsonData.containsKey("ntz" + dateFieldName)) {
													dateOnly = true;
												}

												final LocalDateTime ldt = sourceFormat
														.parse((String) jsonData.get(dateFieldName)).toInstant()
														.atOffset(ZoneOffset.ofTotalSeconds(
																(int) jsonData.get("noffset" + dateFieldName)))
														.toLocalDateTime();
												jsonData.put(
														dateFieldName, dateOnly
																? DateTimeFormatter.ofPattern(userinfo.getSsitedate())
																		.format(ldt)
																: destFormat.format(ldt));

											}
										} else {
											if (!jsonData.containsKey("ntz" + dateFieldName)) {
												dateOnly = true;
											}
											LocalDateTime ldt = sourceFormat.parse((String) jsonData.get(dateFieldName))
													.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
											jsonData.put(dateFieldName, dateOnly
													? DateTimeFormatter.ofPattern(userinfo.getSsitedate()).format(ldt)
													: destFormat.format(ldt));

										}
									} else {
										jsonData.put(dateFieldName, "-");
									}
									data.put("jsondata", jsonData);
								}
							}

						} else {
							if ((String) data.get(dateFieldName) != null && !data.get(dateFieldName).equals("")
									&& !data.get(dateFieldName).equals("-")) {
								if (userinfo.getIsutcenabled() == Enumeration.TransactionStatus.YES
										.gettransactionstatus()) {
									if (data.containsKey("tz" + dateFieldName)) {
										final LocalDateTime ldt = sourceFormat.parse((String) data.get(dateFieldName))
												.toInstant()
												.atOffset(ZoneOffset
														.ofTotalSeconds((int) data.get("noffset" + dateFieldName)))
												.toLocalDateTime();
										data.put(dateFieldName, dateOnly
												? DateTimeFormatter.ofPattern(userinfo.getSsitedate()).format(ldt)
												: destFormat.format(ldt));
									} else if (!data.containsKey("noffset" + dateFieldName)) {

									} else {
										final LocalDateTime ldt = sourceFormat.parse((String) data.get(dateFieldName))
												.toInstant()
												.atOffset(ZoneOffset
														.ofTotalSeconds((int) data.get("noffset" + dateFieldName)))
												.toLocalDateTime();
										data.put(dateFieldName, dateOnly
												? DateTimeFormatter.ofPattern(userinfo.getSsitedate()).format(ldt)
												: destFormat.format(ldt));
									}
								} else {
									LocalDateTime ldt = sourceFormat.parse((String) data.get(dateFieldName)).toInstant()
											.atZone(ZoneId.systemDefault()).toLocalDateTime();
									data.put(dateFieldName,
											dateOnly ? DateTimeFormatter.ofPattern(userinfo.getSsitedate()).format(ldt)
													: destFormat.format(ldt));
								}
							} else {
								data.put(dateFieldName, "-");
							}

						}

					}
					finalList.add(i, data);
				}

			} else {
				finalList.addAll(lst);
			}

		}
		return finalList;
	}

	public String getfnFormat(int sequenceno, String sFormat) throws Exception {
		if (sFormat != null) {
			while (sFormat.contains("{")) {
				int start = sFormat.indexOf('{');
				int end = sFormat.indexOf('}');

				String subString = sFormat.substring(start + 1, end);
				if (subString.equals("yy") || subString.equals("YY")) {
					SimpleDateFormat sdf = new SimpleDateFormat("yy", Locale.getDefault());
					sdf.toPattern();
					Date date = new Date();
					String replaceString = sdf.format(date);
					sFormat = sFormat.replace('{' + subString + '}', replaceString);
				} else if (subString.equals("yyyy") || subString.equals("YYYY")) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
					sdf.toPattern();
					Date date = new Date();
					String replaceString = sdf.format(date);
					sFormat = sFormat.replace('{' + subString + '}', replaceString);
				} else if (subString.equals("mm") || subString.equals("MM")) {
					SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.getDefault());
					sdf.toPattern();
					Date date = new Date();
					String replaceString = sdf.format(date);
					sFormat = sFormat.replace('{' + subString + '}', replaceString);
				} else if (subString.equals("mon") || subString.equals("MON")) {
					SimpleDateFormat sdf = new SimpleDateFormat("MON", Locale.getDefault());
					sdf.toPattern();
					Date date = new Date();
					String replaceString = sdf.format(date);
					sFormat = sFormat.replace('{' + subString + '}', replaceString);
				} else if (subString.equals("dd") || subString.equals("DD")) {
					SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
					sdf.toPattern();
					Date date = new Date();
					String replaceString = sdf.format(date);
					sFormat = sFormat.replace('{' + subString + '}', replaceString);
				} else if (subString.matches("9+")) {
					String seqPadding = "%0" + subString.length() + "d";
					String replaceString = String.format(seqPadding, sequenceno);
					sFormat = sFormat.replace('{' + subString + '}', replaceString);
				}

				else {
					sFormat = sFormat.replace('{' + subString + '}', subString);
				}
			}
		}
		return sFormat;
	}

	public String getSeqfnFormat(final String mainTableName, final String secondaryTableName, final int nregtypecode,
			final int nregsubtypecode, final UserInfo userInfo) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		int sequnenceno = 0;
		Instant resetDate = null;
		int nseqno = 0;

		final String strQuery = " select * from " + secondaryTableName + " where stablename='" + mainTableName + "'"
				+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
				+ userInfo.getNmastersitecode() + " ";
		final List<Map<String, Object>> objSeq = jdbcTemplate.queryForList(strQuery);

		sequnenceno = (int) objSeq.get(0).get("nsequenceno") + 1;

		if (objSeq.get(0).get("dseqresetdate") != null) {
			String dseqresetdate = (String) objSeq.get(0).get("dseqresetdate").toString();
			Timestamp ts = Timestamp.valueOf(dseqresetdate);
			resetDate = ts.toInstant();
		}
		final String seqFormatQuery = " select * from seqformatgenerator where stablename ='" + mainTableName + "'"
				+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and nsitecode =" + userInfo.getNmastersitecode() + " ";

		final SeqFormatGenerator objseq1 = (SeqFormatGenerator) jdbcUtilityFunction.queryForObject(seqFormatQuery,
				SeqFormatGenerator.class, jdbcTemplate);
		String seqFormat = objseq1.getSformattype();

		if (nregtypecode != 0 && nregsubtypecode != 0) {

			// Worklist, batch, release
			final String strSeqnoFormatQuery = "select rsc.nperiodcode,rsc.jsondata,rsc.jsondata->>'dseqresetdate' dseqresetdate,"
					+ " rsc.jsondata->>'ssampleformat' ssampleformat, sag.nsequenceno,sag.nseqnoarnogencode,rsc.nregsubtypeversioncode"
					+ " from  approvalconfig ap, seqnoarnogenerator sag,regsubtypeconfigversion rsc "
					+ " where  sag.nseqnoarnogencode = rsc.nseqnoarnogencode " + " and sag.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ap.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregtypecode = "
					+ nregtypecode + " and nregsubtypecode =" + (int) nregsubtypecode
					+ " and rsc.napprovalconfigcode=ap.napprovalconfigcode and rsc.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
			final SeqNoArnoGenerator seqnoFormat = jdbcTemplate.queryForObject(strSeqnoFormatQuery,
					new SeqNoArnoGenerator());
			final String siteFormat = seqnoFormat.getSsampleformat();

			final int siteSubStringStart = siteFormat.indexOf('{');
			final int siteSubStringEnd = siteFormat.indexOf('}');
			final String siteSubString = siteFormat.substring(siteSubStringStart + 1, siteSubStringEnd);

			if (siteSubString.equalsIgnoreCase("XXXXX")) {
				String siteCodeString = userInfo.getSsitecode();
				seqFormat = siteCodeString + "/" + seqFormat;
			}

		} else {
			nseqno = sequnenceno;
		}

		map = checkSequenceFormatNo(sequnenceno, objseq1.getNperiodcode(), secondaryTableName, resetDate, userInfo);
		nseqno = (int) map.get("sequenceno");

		if (seqFormat != null) {
			while (seqFormat.contains("{")) {

				int start = seqFormat.indexOf('{');
				int end = seqFormat.indexOf('}');
				final String subString = seqFormat.substring(start + 1, end);

				if (subString.equals("yy") || subString.equals("YY")) {
					final SimpleDateFormat sdf = new SimpleDateFormat("yy", Locale.getDefault());
					sdf.toPattern();
					final Date date = new Date();
					final String replaceString = sdf.format(date);
					seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
				} else if (subString.equals("yyyy") || subString.equals("YYYY")) {
					final SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
					sdf.toPattern();
					final Date date = new Date();
					final String replaceString = sdf.format(date);
					seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
				} else if (subString.equals("mm") || subString.equals("MM")) {
					final SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.getDefault());
					sdf.toPattern();
					final Date date = new Date();
					final String replaceString = sdf.format(date);
					seqFormat = seqFormat.replace('{' + subString + '}', replaceString);

					// int Y = 0;
				} else if (subString.equals("mmm") || subString.equals("MMM")) {
					final SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.getDefault());
					sdf.toPattern();
					final Date date = new Date();
					final String replaceString = sdf.format(date);

					seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					// int Y = 0;
				} else if (subString.equals("mon") || subString.equals("MON")) {
					final SimpleDateFormat sdf = new SimpleDateFormat("MON", Locale.getDefault());
					sdf.toPattern();
					final Date date = new Date();
					final String replaceString = sdf.format(date);
					seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
				} else if (subString.equals("dd") || subString.equals("DD")) {
					final SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
					sdf.toPattern();
					final Date date = new Date();
					final String replaceString = sdf.format(date);
					seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
				} else if (subString.matches("9+")) {
					final String seqPadding = "%0" + subString.length() + "d";
					final String replaceString = String.format(seqPadding, nseqno);
					seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
				} else {
					seqFormat = seqFormat.replace('{' + subString + '}', subString);
				}
			}
		}
		// nseqno++;
		final String seqnoUpdateQuery = "update " + secondaryTableName + " set nsequenceno=" + nseqno + ""
				+ " where stablename='" + mainTableName + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and  nsitecode="
				+ userInfo.getNmastersitecode() + "";
		jdbcTemplate.execute(seqnoUpdateQuery);

		return seqFormat;

	}

	private Map<String, Object> checkSequenceFormatNo(int nSequenceno, final int nperiodCode, final String tableName,
			final Instant dresetDate, final UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();
		Instant dcurrentDate = dateUtilityFunction.getUTCDateTime();
		if (dresetDate != null) {
			boolean check = false;
			int cal = 0;
			String dateFormat = "";

			if (nperiodCode == Enumeration.Period.Years.getPeriod()) {
				dateFormat = "yyyy";

			} else if (nperiodCode == Enumeration.Period.Month.getPeriod()) {
				dateFormat = "yyyy-MM";

			} else if (nperiodCode == Enumeration.Period.Weeks.getPeriod()) {
				dateFormat = "yyyy-MM-dd";
				check = true;
				cal = Calendar.WEEK_OF_YEAR;

			} else if (nperiodCode == Enumeration.Period.Days.getPeriod()) {
				dateFormat = "yyyy-MM-dd";
				check = true;
				cal = Calendar.DAY_OF_YEAR;
			}

			final LocalDateTime currentDateTime = LocalDateTime.ofInstant(dcurrentDate.truncatedTo(ChronoUnit.SECONDS),
					ZoneOffset.UTC);
			String currentDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(currentDateTime);

			final LocalDateTime resetDateTime = LocalDateTime.ofInstant(dresetDate.truncatedTo(ChronoUnit.SECONDS),
					ZoneOffset.UTC);
			String resetDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(resetDateTime);

			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
			sdf.toPattern();

			String resetFormatDate = "";
			String currentFormatDate = "";
			if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				LocalDateTime ldt = sdf.parse(currentDate).toInstant().atZone(ZoneId.of(userInfo.getStimezoneid()))
						.toLocalDateTime();
				currentFormatDate = DateTimeFormatter.ofPattern(dateFormat).format(ldt);

				LocalDateTime ldt1 = sdf.parse(resetDate).toInstant().atZone(ZoneId.of(userInfo.getStimezoneid()))
						.toLocalDateTime();
				resetFormatDate = DateTimeFormatter.ofPattern(dateFormat).format(ldt1);
			} else {

				LocalDateTime ldt = sdf.parse(currentDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				currentFormatDate = DateTimeFormatter.ofPattern(dateFormat).format(ldt);

				LocalDateTime ldt1 = sdf.parse(resetDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				resetFormatDate = DateTimeFormatter.ofPattern(dateFormat).format(ldt1);
			}

			boolean bYearreset = false;
			if (check) {
				Date date2 = sdf.parse(currentFormatDate);
				Calendar c = Calendar.getInstance();
				c.setTime(date2);
				Date date3 = sdf.parse(resetFormatDate);
				Calendar c1 = Calendar.getInstance();
				c1.setTime(date3);
				int weekandDay = c.get(cal);
				int weekandDay1 = c1.get(cal);
				if (weekandDay != weekandDay1) {
					bYearreset = true;
				}
			} else if (!(currentFormatDate.equals(resetFormatDate))) {
				bYearreset = true;
			}
			if (bYearreset) {
				String seqnoUpdateQuery = "update " + tableName + " set nsequenceno=1 ,dseqresetdate='" + dcurrentDate
						+ "' where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
				jdbcTemplate.execute(seqnoUpdateQuery);
				nSequenceno = 1;
			}

		} else {
			if (nperiodCode != Enumeration.Period.NA.getPeriod()) { // Added by sonia on 04th Mar 2025 for jira
																	// id:ALPD-5504
				String seqnoUpdateQuery = "update " + tableName + " set dseqresetdate='" + dcurrentDate
						+ "' where  nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
				jdbcTemplate.execute(seqnoUpdateQuery);

			}

		}
		objMap.put("sequenceno", nSequenceno);
		return objMap;
	}

	// ALPD-4870,ALPD-4755,ALPD-4913,ALPD-4878,ALPD-4999-To insert data when filter
	// submit,done by Dhanushya RI
	public Map<String, Object> createFilterSubmit(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> map = new HashMap<String, Object>();

		JSONObject jsonObject = new JSONObject();
		JSONObject jsonTempObject = new JSONObject();
		String uiFromDate = "";
		String uiToDate = "";
		// To clear temporary data from filterdetail
		// table(fromdate,todate,filterstatus,approvalconfig and templatedesign) on
		// logout
		if (inputMap.containsKey("clearTempFilter") && (boolean) inputMap.get("clearTempFilter") == true) {
			final String sessionQry = "select nsessiondetailscode from sessiondetails where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nuserrolecode=" + userInfo.getNuserrole() + " "
					+ " and nusercode=" + userInfo.getNusercode();

			final List<Integer> sessionData = jdbcTemplate.queryForList(sessionQry, Integer.class);

			if (sessionData.isEmpty()) {
				final String deleteQry = "update filterdetail set jsontempdata='" + jsonTempObject.toString()
						+ "' where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " " + "  and nusercode="
						+ userInfo.getNusercode() + " and nuserrolecode=" + userInfo.getNuserrole();
				jdbcTemplate.execute(deleteQry);
			}
			return null;
		} else {

			if (userInfo.getNformcode() == Enumeration.FormCode.SAMPLEREGISTRATION.getFormCode()) {
				uiFromDate = (String) inputMap.get("FromDate");
				uiToDate = (String) inputMap.get("ToDate");
				jsonTempObject.put("FromDate", uiFromDate);
				jsonTempObject.put("ToDate", uiToDate);
			} else if (userInfo.getNformcode() == Enumeration.FormCode.RESULTENTRY.getFormCode()
					|| userInfo.getNformcode() == Enumeration.FormCode.BATCHCREATION.getFormCode()) {
				uiFromDate = (String) inputMap.get("fromdate");
				uiToDate = (String) inputMap.get("todate");
				jsonTempObject.put("fromDate", uiFromDate);
				jsonTempObject.put("toDate", uiToDate);
				jsonTempObject.put("napprovalversioncode", inputMap.get("napprovalversioncode"));
				jsonTempObject.put("napprovalconfigcode", inputMap.get("napprovalconfigcode"));
			} else if (userInfo.getNformcode() == Enumeration.FormCode.RELEASE.getFormCode()) {
				uiFromDate = (String) inputMap.get("dfrom");
				uiToDate = (String) inputMap.get("dto");
				jsonTempObject.put("FromDate", uiFromDate);
				jsonTempObject.put("ToDate", uiToDate);
			} else if (userInfo.getNformcode() == Enumeration.FormCode.JOBALLOCATION.getFormCode()) {
				uiFromDate = (String) inputMap.get("fromdate");
				uiToDate = (String) inputMap.get("todate");
				jsonTempObject.put("fromdate", uiFromDate);
				jsonTempObject.put("todate", uiToDate);
				jsonTempObject.put("sectionValue", inputMap.get("sectionValue"));
				jsonTempObject.put("nsectioncode", inputMap.get("nsectioncode"));
				jsonTempObject.put("testValue", inputMap.get("testValue"));
				jsonTempObject.put("ntestcode", inputMap.get("ntestcode"));
			} else if (userInfo.getNformcode() == Enumeration.FormCode.WORKLIST.getFormCode()
					|| userInfo.getNformcode() == Enumeration.FormCode.TESTWISEMYJOBS.getFormCode()) {
				uiFromDate = (String) inputMap.get("fromdate");
				uiToDate = (String) inputMap.get("todate");
				jsonTempObject.put("FromDate", uiFromDate);
				jsonTempObject.put("ToDate", uiToDate);

			} else if (userInfo.getNformcode() == Enumeration.FormCode.APPROVAL.getFormCode()) {
				uiFromDate = (String) inputMap.get("dfrom");
				uiToDate = (String) inputMap.get("dto");
				jsonTempObject.put("FromDate", uiFromDate);
				jsonTempObject.put("ToDate", uiToDate);

			}
			jsonObject.put("sampleTypeValue", inputMap.get("sampleTypeValue"));
			jsonObject.put("regTypeValue", inputMap.get("regTypeValue"));
			jsonObject.put("regSubTypeValue", inputMap.get("regSubTypeValue"));

			jsonTempObject.put("filterStatusValue", inputMap.get("filterStatusValue"));
			jsonTempObject.put("approvalConfigValue", inputMap.get("approvalConfigValue"));
			if (userInfo.getNformcode() != Enumeration.FormCode.BATCHCREATION.getFormCode()) {
				jsonTempObject.put("designTemplateMappingValue", inputMap.get("designTemplateMappingValue"));
			}

			if (inputMap.containsKey("needExtraKeys") && (boolean) inputMap.get("needExtraKeys") == true) {
				if (userInfo.getNformcode() == Enumeration.FormCode.RELEASE.getFormCode()) {
					jsonTempObject.put("reportTypeValue", inputMap.get("reportTypeValue"));
				} else if (userInfo.getNformcode() == Enumeration.FormCode.RESULTENTRY.getFormCode()) {
					jsonTempObject.put("testValue", inputMap.get("testValue"));
					jsonTempObject.put("ntestcode", inputMap.get("ntestcode"));
					jsonTempObject.put("worklistValue", inputMap.get("worklistValue"));
					jsonTempObject.put("batchValue", inputMap.get("batchValue"));
					jsonTempObject.put("nworklistcode", inputMap.get("nworklistcode"));
					jsonTempObject.put("nbatchcode", inputMap.get("nbatchcode"));
				} else if (userInfo.getNformcode() == Enumeration.FormCode.JOBALLOCATION.getFormCode()) {
					jsonTempObject.put("sectionValue", inputMap.get("sectionValue"));
					jsonTempObject.put("nsectioncode", inputMap.get("nsectioncode"));
					jsonTempObject.put("testValue", inputMap.get("testValue"));
					jsonTempObject.put("ntestcode", inputMap.get("ntestcode"));
					jsonTempObject.put("napproveconfversioncode", inputMap.get("napprovalversioncode"));

				} else if (userInfo.getNformcode() == Enumeration.FormCode.TESTWISEMYJOBS.getFormCode()) {
					jsonTempObject.put("napprovalversioncode", inputMap.get("napprovalversioncode"));
					jsonTempObject.put("nsectioncode", inputMap.get("nsectioncode"));
					jsonTempObject.put("ntestcode", inputMap.get("ntestcode"));

				} else if (userInfo.getNformcode() == Enumeration.FormCode.APPROVAL.getFormCode()) {
					jsonTempObject.put("napprovalversioncode", inputMap.get("napprovalversioncode"));
					jsonTempObject.put("nsectioncode", inputMap.get("nsectioncode"));
					jsonTempObject.put("ntestcode", inputMap.get("ntestcode"));
					jsonTempObject.put("nfiltertransactionstatus", inputMap.get("ntransactionstatus"));

				}

			}

			final String strQuery = "select nfilterdetailcode from filterdetail where nformcode="
					+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " "
					+ " and nuserrolecode=" + userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<Integer> filterDetailCode = jdbcTemplate.queryForList(strQuery, Integer.class);

			if (filterDetailCode.isEmpty()) {

				final String sQuery = "select nsequenceno from seqnoregistration where stablename = 'filterdetail';";
				int nSeqNo = (int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class, jdbcTemplate);
				nSeqNo++;

//				ALPD-6044	Added nregtypecode and nregsubtypecode in filterdetail table by Vishakh (03-07-2025)
				final String insertQuery = "insert into filterdetail(nfilterdetailcode, nformcode, nusercode, nuserrolecode, jsondata, jsontempdata, dmodifieddate, nsitecode, nstatus, nregtypecode, nregsubtypecode)"
						+ " values(" + nSeqNo + "," + userInfo.getNformcode() + "," + userInfo.getNusercode() + ","
						+ userInfo.getNuserrole() + "," + " '"
						+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "','"
						+ stringUtilityFunction.replaceQuote(jsonTempObject.toString()) + "','"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ userInfo.getNtranssitecode()+ ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
						+ inputMap.get("nregtypecode")+ ", "+ inputMap.get("nregsubtypecode")+ ")";
				jdbcTemplate.execute(insertQuery);

				final String updatequery = "update seqnoregistration set nsequenceno = " + nSeqNo
						+ " where stablename = 'filterdetail';";
				jdbcTemplate.execute(updatequery);
			} else {
//				ALPD-6044	Added nregtypecode and nregsubtypecode in filterdetail table update by Vishakh (03-07-2025)
				final String updateQry = "update filterdetail set jsondata='"
						+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "', " + " jsontempdata='"
						+ stringUtilityFunction.replaceQuote(jsonTempObject.toString()) + "', nregtypecode="
						+ inputMap.get("nregtypecode")+ ", nregsubtypecode="+ inputMap.get("nregsubtypecode") 
						+ ", dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " 
						+ " where nformcode="+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() 
						+ " "+ " and nuserrolecode=" + userInfo.getNuserrole() + " " + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(updateQry);
			}
			return map;
		}

	}

	// ALPD-4870,ALPD-4755,ALPD-4913,ALPD-4878,ALPD-4999 When click the filtername
	// that value updated to filterdetail table,done by Dhanushya RI
	public Map<String, Object> updateFilterDetail(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select * from filtername where nformcode=" + userInfo.getNformcode()
				+ " and nusercode=" + userInfo.getNusercode() + " " + " and nuserrolecode=" + userInfo.getNuserrole()
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nfilternamecode="
				+ inputMap.get("nfilternamecode");
		final List<FilterName> lstFilterName = jdbcTemplate.query(strQuery, new FilterName());

		if (!lstFilterName.isEmpty()) {

			final JSONObject jsonData = new JSONObject(lstFilterName.get(0).getJsondata());
			final JSONObject jsonTempdata = new JSONObject(lstFilterName.get(0).getJsontempdata());

//			ALPD-6044	Added nregtypecode and nregsubtypecode in filterdetail table update by Vishakh (03-07-2025)
			final String updateQry = "update filterdetail set jsondata='"
					+ stringUtilityFunction.replaceQuote(jsonData.toString()) + "', " + " jsontempdata='"
					+ stringUtilityFunction.replaceQuote(jsonTempdata.toString()) + "', nregtypecode="
					+ inputMap.get("nregtypecode")+ ", nregsubtypecode="+ inputMap.get("nregsubtypecode") + " where nformcode="
					+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " and nuserrolecode="
					+ userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			jdbcTemplate.execute(updateQry);
		}
		return null;
	}

	// ALPD-4870,ALPD-4755,ALPD-4913,ALPD-4878,ALPD-4999 insert filtername and the
	// details in filtername table,done by Dhanushya RI
	public Map<String, Object> createFilterData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		JSONObject jsonObject = new JSONObject();
		JSONObject jsonTempObject = new JSONObject();
		String uiFromDate = "";
		String uiToDate = "";

		if (userInfo.getNformcode() == Enumeration.FormCode.SAMPLEREGISTRATION.getFormCode()) {
			uiFromDate = (String) inputMap.get("FromDate");
			uiToDate = (String) inputMap.get("ToDate");
		} else if (userInfo.getNformcode() == Enumeration.FormCode.RESULTENTRY.getFormCode()
				|| userInfo.getNformcode() == Enumeration.FormCode.JOBALLOCATION.getFormCode()) {
			uiFromDate = (String) inputMap.get("fromdate");
			uiToDate = (String) inputMap.get("todate");
		} else if (userInfo.getNformcode() == Enumeration.FormCode.RELEASE.getFormCode()) {
			uiFromDate = (String) inputMap.get("dfrom");
			uiToDate = (String) inputMap.get("dto");
		} else if (userInfo.getNformcode() == Enumeration.FormCode.BATCHCREATION.getFormCode()) {
			uiFromDate = (String) inputMap.get("fromdate");
			uiToDate = (String) inputMap.get("todate");
			jsonTempObject.put("napprovalversioncode", inputMap.get("napprovalversioncode"));
			jsonTempObject.put("napprovalconfigcode", inputMap.get("napprovalconfigcode"));

		} else if (userInfo.getNformcode() == Enumeration.FormCode.TESTWISEMYJOBS.getFormCode()) {
			uiFromDate = (String) inputMap.get("dfrom");
			uiToDate = (String) inputMap.get("dto");
			jsonTempObject.put("FromDate", uiFromDate);
			jsonTempObject.put("ToDate", uiToDate);

		} else if (userInfo.getNformcode() == Enumeration.FormCode.WORKLIST.getFormCode()) {
			uiFromDate = (String) inputMap.get("dfrom");
			uiToDate = (String) inputMap.get("dto");
			jsonTempObject.put("FromDate", uiFromDate);
			jsonTempObject.put("ToDate", uiToDate);
			jsonTempObject.put("ntransactionstatusfilter", inputMap.get("ntransactionstatusfilter"));

		} else if (userInfo.getNformcode() == Enumeration.FormCode.APPROVAL.getFormCode()) {

			uiFromDate = (String) inputMap.get("dfrom");
			uiToDate = (String) inputMap.get("dto");
			jsonTempObject.put("FromDate", uiFromDate);
			jsonTempObject.put("ToDate", uiToDate);
			jsonTempObject.put("napprovalconfigcode", inputMap.get("napprovalconfigcode"));

		}

		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		String fromDate = LocalDateTime.parse(uiFromDate, dbPattern).format(uiPattern);
		String toDate = LocalDateTime.parse(uiToDate, dbPattern).format(uiPattern);

		fromDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(uiFromDate, userInfo, true));
		toDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(uiToDate, userInfo, true));

		jsonObject.put("sampleTypeValue", inputMap.get("sampleTypeValue"));
		jsonObject.put("regTypeValue", inputMap.get("regTypeValue"));
		jsonObject.put("regSubTypeValue", inputMap.get("regSubTypeValue"));
		jsonObject.put("nsampletypecode", inputMap.get("nsampletypecode"));
		jsonObject.put("nregtypecode", inputMap.get("nregtypecode"));
		jsonObject.put("nregsubtypecode", inputMap.get("nregsubtypecode"));

		if (userInfo.getNformcode() == Enumeration.FormCode.SAMPLEREGISTRATION.getFormCode()
				|| userInfo.getNformcode() == Enumeration.FormCode.RELEASE.getFormCode()) {
			jsonTempObject.put("FromDate", fromDate);
			jsonTempObject.put("ToDate", toDate);
		} else if (userInfo.getNformcode() == Enumeration.FormCode.RESULTENTRY.getFormCode()
				|| userInfo.getNformcode() == Enumeration.FormCode.BATCHCREATION.getFormCode()) {
			jsonTempObject.put("fromDate", fromDate);
			jsonTempObject.put("toDate", toDate);
		} else if (userInfo.getNformcode() == Enumeration.FormCode.JOBALLOCATION.getFormCode()) {
			jsonTempObject.put("fromdate", fromDate);
			jsonTempObject.put("todate", toDate);
		} else if (userInfo.getNformcode() == Enumeration.FormCode.APPROVAL.getFormCode()) {
			jsonTempObject.put("fromdate", fromDate);
			jsonTempObject.put("todate", toDate);

		} else if (userInfo.getNformcode() == Enumeration.FormCode.WORKLIST.getFormCode()) {
			jsonTempObject.put("fromdate", fromDate);
			jsonTempObject.put("todate", toDate);

		} else if (userInfo.getNformcode() == Enumeration.FormCode.TESTWISEMYJOBS.getFormCode()) {
			jsonTempObject.put("fromdate", fromDate);
			jsonTempObject.put("todate", toDate);

		}

		jsonTempObject.put("filterStatusValue", inputMap.get("filterStatusValue"));
		jsonTempObject.put("approvalConfigValue", inputMap.get("approvalConfigValue"));

		if (userInfo.getNformcode() != Enumeration.FormCode.BATCHCREATION.getFormCode()) {
			jsonTempObject.put("designTemplateMappingValue", inputMap.get("designTemplateMappingValue"));
		}

		jsonTempObject.put("DbFromDate", fromDate);
		jsonTempObject.put("DbToDate", toDate);
		jsonTempObject.put("ntranscode", inputMap.get("ntranscode"));
		jsonTempObject.put("napproveconfversioncode", inputMap.get("napproveconfversioncode"));
		jsonTempObject.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));

		if (inputMap.containsKey("needExtraKeys") && (boolean) inputMap.get("needExtraKeys") == true) {
			if (userInfo.getNformcode() == Enumeration.FormCode.RELEASE.getFormCode()) {
				jsonTempObject.put("ncoareporttypecode", inputMap.get("ncoareporttypecode"));
				jsonTempObject.put("reportTypeValue", inputMap.get("reportTypeValue"));
				jsonTempObject.put("napprovalconfigcode", inputMap.get("napprovalconfigcode"));
				jsonTempObject.put("napprovalversioncode", inputMap.get("napprovalversioncode"));
				jsonTempObject.put("nneedtemplatebasedflow", inputMap.get("nneedtemplatebasedflow"));
				jsonTempObject.put("isneedsection", inputMap.get("isneedsection"));

			} else if (userInfo.getNformcode() == Enumeration.FormCode.RESULTENTRY.getFormCode()) {
				jsonTempObject.put("testValue", inputMap.get("testValue"));
				jsonTempObject.put("ntestcode", inputMap.get("ntestcode"));
				jsonTempObject.put("worklistValue", inputMap.get("worklistValue"));
				jsonTempObject.put("batchValue", inputMap.get("batchValue"));
				jsonTempObject.put("nworklistcode", inputMap.get("nworklistcode"));
				jsonTempObject.put("nbatchcode", inputMap.get("nbatchcode"));
			} else if (userInfo.getNformcode() == Enumeration.FormCode.JOBALLOCATION.getFormCode()) {
				jsonTempObject.put("sectionValue", inputMap.get("sectionValue"));
				jsonTempObject.put("nsectioncode", inputMap.get("nsectioncode"));
				jsonTempObject.put("testValue", inputMap.get("testValue"));
				jsonTempObject.put("ntestcode", inputMap.get("ntestcode"));
				jsonTempObject.put("napproveconfversioncode", inputMap.get("napprovalversioncode"));

			} else if (userInfo.getNformcode() == Enumeration.FormCode.APPROVAL.getFormCode()) {
				jsonTempObject.put("nsectioncode", inputMap.get("nsectioncode"));
				jsonTempObject.put("ntestcode", inputMap.get("ntestcode"));
				jsonTempObject.put("nfiltertransactionstatus", inputMap.get("nfiltertransactionstatus"));

			}
		}

		final String sFilterNameQry = "select nfilternamecode from filtername where nformcode="
				+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + "" + " and nuserrolecode="
				+ userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode() + " " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 asc ";

		final List<FilterName> listfilter = jdbcTemplate.query(sFilterNameQry, new FilterName());

		final String sSettings = "select ssettingvalue from settings where nsettingcode=72 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(sSettings, Settings.class,
				jdbcTemplate);

		String sQuery = "select nsequenceno from seqnoregistration where stablename = 'filtername';";
		int nSeqNo = (int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class, jdbcTemplate);
		nSeqNo++;

//		ALPD-6044	Added nregtypecode and nregsubtypecode in filtername table by Vishakh (03-07-2025)
		final String insertQuery = "insert into filtername(nfilternamecode, nformcode, nusercode, nuserrolecode, sfiltername, jsondata, jsontempdata, dmodifieddate, nsitecode,nstatus, nregtypecode, nregsubtypecode)"
				+ " values(" + nSeqNo + "," + userInfo.getNformcode() + "," + userInfo.getNusercode() + ","
				+ userInfo.getNuserrole() + ",N'"
				+ stringUtilityFunction.replaceQuote(inputMap.get("sfiltername").toString()) + "'," + " '"
				+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "','"
				+ stringUtilityFunction.replaceQuote(jsonTempObject.toString()) + "','"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ", "+ inputMap.get("nregtypecode")
				+ ", "+ inputMap.get("nregsubtypecode")+ ")";
		jdbcTemplate.execute(insertQuery);

		final String updateSeqQuery = "update seqnoregistration set nsequenceno = " + nSeqNo
				+ " where stablename = 'filtername';";

		jdbcTemplate.execute(updateSeqQuery);

		final int settingValue = objSettings != null ? Integer.parseInt(objSettings.getSsettingvalue()) : 5;

		// To delete the oldest record if the filtername table reached more than 5
		// records
		if (!listfilter.isEmpty() && listfilter.size() >= settingValue) {

			final int filterCode = !listfilter.isEmpty() ? listfilter.get(0).getNfilternamecode() : 0;

			final String updateQry = "update filtername set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " " + " where nfilternamecode="
					+ filterCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updateQry);
		}

		return null;

	}

	// To get the primarykey of the previously saved filter by filtername
	public List<FilterName> getFilterListByName(final String sFilterName, final UserInfo userInfo) throws Exception {
		final String strQuery = "select nfilternamecode from filtername where sfiltername = N'"
				+ stringUtilityFunction.replaceQuote(sFilterName) + "'" + " and nformcode=" + userInfo.getNformcode()
				+ " and nusercode=" + userInfo.getNusercode() + "" + " and nuserrolecode=" + userInfo.getNuserrole()
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (List<FilterName>) jdbcTemplate.query(strQuery, new FilterName());
	}

	public List<TreeDataFormat> generateTreeFormatData(List<TreeTemplateManipulation> listTree) {
		List<TreeDataFormat> treeList = new ArrayList<TreeDataFormat>();
		for (TreeTemplateManipulation objTree : listTree) {
			TreeDataFormat treeDataFormat = new TreeDataFormat();
			treeDataFormat.setItem(objTree);
			treeDataFormat.setKey(String.valueOf(objTree.getNtemplatemanipulationcode()));
			treeDataFormat.setPrimaryKey(objTree.getNtemplatemanipulationcode());
			treeDataFormat.setParentKey(objTree.getNparentnode());
			treeDataFormat.setLabel(objTree.getSleveldescription());
			treeList.add(treeDataFormat);
		}
		return treeList;
	}

	public Map<String, Object> getHierarchicalList(final List<TreeTemplateManipulation> listTree, boolean sEditAction,
			String sNode, int primarykey) {
		Map<String, Object> outputMap = new TreeMap<String, Object>();
		List<TreeDataFormat> list = null;
		String activeKey = "";
		Map<Integer, String> openNodes = new HashMap<Integer, String>();
		openNodes.put(0, "root");
		if (!listTree.isEmpty()) {
			List<TreeDataFormat> originalList = generateTreeFormatData(listTree);
			List<TreeDataFormat> copyList = new ArrayList<TreeDataFormat>();
			originalList.forEach(o -> {
				copyList.add(new TreeDataFormat(o));
			});

			// Generate Open Nodes
			for (TreeDataFormat treeDataFormat : copyList) {
				copyList.stream().forEach(x -> {
					if (treeDataFormat.getPrimaryKey() == x.getParentKey()) {
						x.setKey(treeDataFormat.getKey() + "/" + x.getKey());
					}
				});
			}

			copyList.stream().forEach(m -> {
				openNodes.put(m.getPrimaryKey(), "root/" + m.getKey());
			});

			// Generate Tree Data
//			Map<Integer, TreeDataFormat> nodes = originalList.stream()
//					.collect(Collectors.toMap(treeFormat -> treeFormat.getPrimaryKey(), treeFormat -> treeFormat));
			//ALPD-5873 Added by Abdul for Default Spec
            Map<Integer, TreeDataFormat> nodes = originalList.stream()
				    .collect(Collectors.toMap(
				        TreeDataFormat::getPrimaryKey,
				        treeFormat -> treeFormat,
				        (existing, replacement) -> existing, // merge function
				        LinkedHashMap::new // supplier for ordered map
				    ));
			originalList.stream().filter(o -> o.getParentKey() > -1)
					.sorted(Comparator.comparing(TreeDataFormat::getPrimaryKey).reversed()).forEach(h -> {
						if (nodes.get(h.getParentKey()) != null) {
							if (nodes.get(h.getParentKey()).getNodes() == null) {
								nodes.get(h.getParentKey()).setNodes(new ArrayList<TreeDataFormat>());
							}
							nodes.get(h.getParentKey()).getNodes().add(nodes.get(h.getPrimaryKey()));
							nodes.remove(h.getPrimaryKey());
						}
					});

			list = new ArrayList<TreeDataFormat>(nodes.values());
			if (sEditAction) {
				outputMap.put("primarykey", primarykey);
				activeKey = sNode;
			} else {
				List<TreeDataFormat> listTreeDataFormat = convertToFlatList(list);
				//ALPD-5873 Added by Abdul for Default Spec
                TreeDataFormat root = listTreeDataFormat.get(0);
				int key;
				if (root.getNodes() == null || root.getNodes().isEmpty()) {
				    key = root.getPrimaryKey(); // No children, return root
				} else {
				    key = findLastLeafPrimaryKey(root); // Has children, find last leaf
				}
				outputMap.put("primarykey", key);
				activeKey = openNodes.get(key);
			}
		} else {
			outputMap.put("primarykey", 0);
			activeKey = "root";
		}

		TreeDataFormat treeDataFormat = new TreeDataFormat();
		treeDataFormat.setKey("root");
		treeDataFormat.setLabel("root");
		treeDataFormat.setItem(null);
		treeDataFormat.setNodes(list);
		List<TreeDataFormat> lstTreeDataFormat = new ArrayList<TreeDataFormat>();
		lstTreeDataFormat.add(treeDataFormat);

		outputMap.put("AgaramTree", lstTreeDataFormat);
		outputMap.put("OpenNodes", new ArrayList<String>(openNodes.values()));
		outputMap.put("FocusKey", activeKey);
		outputMap.put("ActiveKey", activeKey);
		return outputMap;
	}

	private static List<TreeDataFormat> convertToFlatList(List<TreeDataFormat> listTreeDataFormat) {
		return listTreeDataFormat.stream().flatMap(i -> {
			if (Objects.nonNull(i.getNodes())) {
				return Stream.concat(Stream.of(i), convertToFlatList(i.getNodes()).stream());
			}
			return Stream.of(i);

		}).collect(Collectors.toList());
	}
	//ALPD-5873 Added by Abdul for Default Spec
	private static Integer findLastLeafPrimaryKey(TreeDataFormat node) {
	    if (node.getNodes() == null || node.getNodes().isEmpty()) {
	        return node.getPrimaryKey();  // Leaf node
	    }

	    // Get the last child node (rightmost)
	    TreeDataFormat lastChild = node.getNodes().get(node.getNodes().size() - 1);
	    return findLastLeafPrimaryKey(lastChild);  // Recurse
	}
	public Map<String, Object> validateUniqueConstraint(final List<Map<String, Object>> masterUniqueValidation,
			final Map<String, Object> registration, final UserInfo userInfo, final String task, Class<?> tableName,
			final String columnName, boolean isMaster) throws Exception {

		Map<String, Object> jsonUIData = (Map<String, Object>) registration.get("jsonuidata");

		final String tablename = tableName.getSimpleName().toLowerCase();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> returnObj = new HashMap<String, Object>();
		final StringBuffer query = new StringBuffer();
		String conditionalString = "";
		final List<String> listMultiLingual = new LinkedList<>();
		final List<Class<?>> listClass = new LinkedList<>();
		if (isMaster) {
			conditionalString = " nformcode =" + userInfo.getNformcode() + " and ";
		} else if (tablename.equals("registration")) {
			conditionalString = " nregtypecode =" + registration.get("nregtypecode") + " and nregsubtypecode="
					+ registration.get("nregsubtypecode") + " and ";
		}
		String data = "";
		for (Map<String, Object> constraintMap : masterUniqueValidation) {
			final StringBuffer buffer = new StringBuffer();
			Set<String> lstKey = constraintMap.keySet();
			String multiLingualName = "";

			for (String key : lstKey) {
				final Map<String, Object> objMap = (Map<String, Object>) constraintMap.get(key);
				final Map<String, Object> mapMultilingual = (Map<String, Object>) objMap.get("1");
				if (multiLingualName.isEmpty()) {
					multiLingualName = (String) mapMultilingual.get(userInfo.getSlanguagetypecode());
				} else {
					multiLingualName = multiLingualName + "," + mapMultilingual.get(userInfo.getSlanguagetypecode());
				}
				if (jsonUIData.get(key) instanceof Integer) {
					data = String.valueOf(jsonUIData.get(key));
				} else {
					data = jsonUIData.get(key).toString();
				}

				buffer.append(" and  jsonuidata->> '" + key + "' ILIKE '" + data + "'");
			}
			listMultiLingual.add(multiLingualName);
			if (task.equalsIgnoreCase("update")) {
				buffer.append(" and " + columnName + " <>" + registration.get(columnName));
			}

			query.append(
					"select " + columnName + " from " + tablename + " dm where " + conditionalString + "  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + buffer.toString() + ";");
			listClass.add(tableName);
		}

		if (query.toString().length() > 1) {

			// List<?> lstData = findByMultiTablesPlainSqlList1(query.toString(), 0);

			List<?> lstData = getMultipleEntitiesResultSetInList(query.toString(), jdbcTemplate, tableName);

			// Need to work out for above query migration
			// List<?> lstData = new ArrayList<>();

			int i = 0;
			for (Map<String, Object> constraintMap : masterUniqueValidation) {
				List<?> lstData1 = (List<?>) lstData.get(i);
				if (!lstData1.isEmpty()) {
					returnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							listMultiLingual.get(i) + " " + commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS",
									userInfo.getSlanguagefilename()));
					return returnObj;
				}
				i++;
			}

		}
		returnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		return returnObj;
	}

	public Map<Integer, String> getfnInfFormat(List<String> list, int sequenceno, String sFormat, Instant lastresetdate,
			int nseqnoarno, UserInfo userInfo, Map<String, Object> jsondata, int nregsubtypeversioncode,
			int nperiodcode, int updatesequenceno) throws Exception {
		Map<Integer, String> returnMap = new TreeMap<Integer, String>();
		Map<String, Object> map = new HashMap<>();
		int nseqno;
		JSONObject jsonobj = new JSONObject(jsondata);
		if (nperiodcode != Enumeration.Period.Never.getPeriod()) {
			map = checkSequenceNo(list, sequenceno, lastresetdate, nseqnoarno, userInfo, jsonobj,
					nregsubtypeversioncode, nperiodcode, updatesequenceno);
			nseqno = (int) map.get("sequenceno");
		} else {
			nseqno = sequenceno;
			String str = "";
			if (jsonobj.has("nneedsitewisearno") && jsonobj.getBoolean("nneedsitewisearno")) {
				str = "update seqnositearnogenerator set nsequenceno=" + (list.size() + updatesequenceno)
						+ " where nseqnoarnogencode=" + nseqnoarno + "and nsitecode=" + userInfo.getNsitecode()
						+ "and nregsubtypeversioncode=" + nregsubtypeversioncode + ";";
				jdbcTemplate.execute(str);
			} else {
				str = "update seqnoarnogenerator set nsequenceno=" + (list.size() + updatesequenceno)
						+ " where nseqnoarnogencode=" + nseqnoarno;
			}

			jdbcTemplate.execute(str);
		}

		// init(timestamp);
		for (int i = 0; i < list.size(); i++) {
			String seqFormat = sFormat;
			if (seqFormat != null) {
				while (seqFormat.contains("{")) {
					int start = seqFormat.indexOf('{');
					int end = seqFormat.indexOf('}');

					String subString = seqFormat.substring(start + 1, end);
					if (subString.equalsIgnoreCase("XXXXX")) {
						String replaceString = userInfo.getSsitecode();
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("yy") || subString.equals("YY")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yy", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("yyyy") || subString.equals("YYYY")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("mm") || subString.equals("MM")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);

						int Y = 0;
					} else if (subString.equals("mmm") || subString.equals("MMM")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);

						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
						int Y = 0;
					} else if (subString.equals("mon") || subString.equals("MON")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MON", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("dd") || subString.equals("DD")) {
						SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.matches("9+")) {
						String seqPadding = "%0" + subString.length() + "d";
						String replaceString = String.format(seqPadding, nseqno);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					}

					else {
						seqFormat = seqFormat.replace('{' + subString + '}', subString);
					}
				}
			}
			nseqno++;
			returnMap.put(Integer.parseInt(list.get(i)), seqFormat);
		}
		return returnMap;
	}

	@SuppressWarnings("unused")
	private Map<String, Object> checkSequenceNo(List<String> list, int sequenceno, Instant lastresetdate,
			int nseqnoarnogencode, UserInfo userinfo, JSONObject jsonobj, int nregsubtypeversioncode, int nperiodcode,
			int updatesequenceno) throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();
		Instant date = dateUtilityFunction.getUTCDateTime();
		if (lastresetdate != null) {
			boolean check = false;
			int cal = 0;
			String dateFormat = "";
			if (nperiodcode == Enumeration.Period.Years.getPeriod()) {
				dateFormat = "yyyy";
			} else if (nperiodcode == Enumeration.Period.Month.getPeriod()) {
				dateFormat = "yyyy-MM";
			} else if (nperiodcode == Enumeration.Period.Weeks.getPeriod()) {
				dateFormat = "yyyy-MM-dd";
				check = true;
				cal = Calendar.WEEK_OF_YEAR;
			} else if (nperiodcode == Enumeration.Period.Days.getPeriod()) {
				dateFormat = "yyyy-MM-dd";
				check = true;
				cal = Calendar.DAY_OF_YEAR;
			}

			final LocalDateTime datetime = LocalDateTime.ofInstant(date.truncatedTo(ChronoUnit.SECONDS),
					ZoneOffset.UTC);
			String currentdate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(datetime);

			final LocalDateTime resetDateTime = LocalDateTime.ofInstant(lastresetdate.truncatedTo(ChronoUnit.SECONDS),
					ZoneOffset.UTC);
			Date myDate1 = Date.from(lastresetdate);
			SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String resetDate = formatter1.format(myDate1);

			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
			sdf.toPattern();

			String formatted = "";
			String formatdate = "";
			if (userinfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				LocalDateTime ldt = sdf.parse(resetDate).toInstant().atZone(ZoneId.of(userinfo.getStimezoneid()))
						.toLocalDateTime();
				formatted = DateTimeFormatter.ofPattern(dateFormat).format(ldt);

				LocalDateTime ldt1 = sdf.parse(currentdate).toInstant().atZone(ZoneId.of(userinfo.getStimezoneid()))
						.toLocalDateTime();
				formatdate = DateTimeFormatter.ofPattern(dateFormat).format(ldt1);
			} else {

				LocalDateTime ldt = sdf.parse(resetDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				formatted = DateTimeFormatter.ofPattern(dateFormat).format(ldt);

				LocalDateTime ldt1 = sdf.parse(currentdate).toInstant().atZone(ZoneId.systemDefault())
						.toLocalDateTime();
				formatdate = DateTimeFormatter.ofPattern(dateFormat).format(ldt1);
			}

			boolean bYearreset = false;
			if (check) {
				Date date2 = sdf.parse(formatdate);
				Calendar c = Calendar.getInstance();
				c.setTime(date2);
				Date date3 = sdf.parse(formatted);
				Calendar c1 = Calendar.getInstance();
				c1.setTime(date3);
				int weekandDay = c.get(cal);
				int weekandDay1 = c1.get(cal);
				if (weekandDay != weekandDay1) {
					bYearreset = true;
				}
			} else if (!(formatdate.equals(formatted))) {
				bYearreset = true;
			}
			String st = "";
			if (bYearreset) {

				if (jsonobj.has("nneedsitewisearno") && jsonobj.getBoolean("nneedsitewisearno")) {
					st = "update seqnositearnogenerator set nsequenceno=" + (list.size()) + " where nseqnoarnogencode="
							+ nseqnoarnogencode + "and nsitecode=" + userinfo.getNsitecode()
							+ "and nregsubtypeversioncode=" + nregsubtypeversioncode + ";";

				} else {
					st = "update seqnoarnogenerator set nsequenceno= " + list.size() + "  where nseqnoarnogencode="
							+ nseqnoarnogencode;
				}

				jdbcTemplate.execute(st);
				sequenceno = 1;

				jsonobj.remove("dseqresetdate");
				jsonobj.put("dseqresetdate",
						dateUtilityFunction.instantDateToStringWithFormat(date, "yyyy-MM-dd HH:mm:ss"));

				st = "update regsubtypeconfigversion set jsondata='" + jsonobj.toString() + "'::jsonb "
						+ " where nregsubtypeversioncode=" + nregsubtypeversioncode;
				jdbcTemplate.execute(st);
			} else {
				String insertNewSeqNo = "";
				int seqNoSiteArnoGenCode;
				String updateSeqNo = "";
				if (jsonobj.has("nneedsitewisearno") && jsonobj.getBoolean("nneedsitewisearno")) {
					String str = "update seqnositearnogenerator set nsequenceno=" + (list.size() + updatesequenceno)
							+ " where nseqnoarnogencode=" + nseqnoarnogencode + "and nsitecode="
							+ userinfo.getNsitecode() + "and nregsubtypeversioncode=" + nregsubtypeversioncode + ";";
					jdbcTemplate.execute(str);
				}

				else {
					st = "update seqnoarnogenerator set nsequenceno=" + (list.size() + updatesequenceno)
							+ " where nseqnoarnogencode=" + nseqnoarnogencode;
					jdbcTemplate.execute(st);
				}
			}
		} else {
			String str = "";
			if (jsonobj.has("nneedsitewisearno") && jsonobj.getBoolean("nneedsitewisearno")) {
				String str1 = "update seqnositearnogenerator set nsequenceno=" + (list.size() + updatesequenceno)
						+ " where nseqnoarnogencode=" + nseqnoarnogencode + "and nsitecode=" + userinfo.getNsitecode()
						+ "and nregsubtypeversioncode=" + nregsubtypeversioncode + ";";
				jdbcTemplate.execute(str1);
			} else {
				str = "update seqnoarnogenerator set nsequenceno=" + (list.size() + updatesequenceno)
						+ " where nseqnoarnogencode=" + nseqnoarnogencode + ";";
			}
			jsonobj.put("dseqresetdate",
					dateUtilityFunction.instantDateToStringWithFormat(date, "yyyy-MM-dd HH:mm:ss"));
			str = str + "update regsubtypeconfigversion set jsondata='" + jsonobj.toString() + "'::jsonb "
					+ " where nregsubtypeversioncode=" + nregsubtypeversioncode + ";";
			jdbcTemplate.execute(str);
		}
		objMap.put("sequenceno", sequenceno);
		return objMap;
	}

	public String fnReplaceParameter(String sgetQuery, final Map<String, Object> inputMap) throws Exception {

		Map<String, Object> mapUser = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		UserInfo userinfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		mapUser = mapper.convertValue(userinfo, Map.class);
		String sOldKey = "";
		int firstIndex = 0;
		int lastIndex = 0;
		String sFieldKey = "";
		String sTempData = "";

		if (!sgetQuery.isEmpty()) {
			outer: while (sgetQuery.contains(" ")) {
				firstIndex = sgetQuery.indexOf("P$");
				lastIndex = sgetQuery.indexOf("$P");
				if (firstIndex > 0 && lastIndex > 0) {
					sFieldKey = sgetQuery.substring(firstIndex + 2, lastIndex);
					if (!sFieldKey.equals(sOldKey)) {
						if (inputMap.containsKey(sFieldKey)) {
							if (inputMap.get(sFieldKey).getClass().getName() == "java.lang.Integer") {
								int value = (int) inputMap.get(sFieldKey);
								sTempData = Integer.toString(value);
							} else if (inputMap.get(sFieldKey).getClass().getName() == "java.lang.String") {
								sTempData = (String) inputMap.get(sFieldKey);
							}
							StringBuffer sb = new StringBuffer(sgetQuery);
							sb.replace(firstIndex, lastIndex + 2, sTempData);
							sgetQuery = sb.toString();
							continue outer;
						} else if (mapUser != null && mapUser.containsKey(sFieldKey)) {
							if (mapUser.get(sFieldKey).getClass().getName() == "java.lang.Integer") {
								int value = (int) mapUser.get(sFieldKey);
								sTempData = Integer.toString(value);
							} else if (mapUser.get(sFieldKey).getClass().getName() == "java.lang.String") {
								sTempData = (String) mapUser.get(sFieldKey);
							}
							StringBuffer sb = new StringBuffer(sgetQuery);
							sb.replace(firstIndex, lastIndex + 2, sTempData);
							sgetQuery = sb.toString();
							continue outer;
						} else {
							sOldKey = sFieldKey;
							// ALPD-5651 Added Failed string by Vishakh for temperory fix for release when
							// configured mail (06-04-2025)
							return Enumeration.ReturnStatus.FAILED.getreturnstatus();
						}
					} else {
						sTempData = "null";
						StringBuffer sb = new StringBuffer(sgetQuery);
						sb.replace(firstIndex, lastIndex + 2, sTempData);
						sgetQuery = sb.toString();
						continue outer;
					}
				}
				break outer;
			}
		}
		return sgetQuery;
	}

	public int calculateAge(String s, UserInfo userinfo) throws Exception {
		// String s = "1994/06/23";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date oldDate = (Date) formatter.parse(formatter);
		Date d = sdf.parse(s);
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DATE);
		LocalDate l1 = LocalDate.of(year, month, date);
		LocalDate now1 = LocalDate.now();
		int diff1 = Period.between(l1, now1).getYears();
		// int years=diff1.getYears();)

		return diff1;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	public Map<String, Object> validateUniqueConstraintImport(final List<Map<String, Object>> masterUniqueValidation,
			final List<Map<String, Object>> registration, final UserInfo userInfo, final String task,
			Class<?> tableName, final String columnName, boolean isMaster) throws Exception {

		Map<String, Object> returnObj = new HashMap<String, Object>();
		for (int j = 0; j < registration.size(); j++) {
			Map<String, Object> jsonUIData = (Map<String, Object>) registration.get(j).get("jsonuidata");
			final String tablename = tableName.getSimpleName().toLowerCase();
			Map<String, Object> map = new HashMap<String, Object>();
			final StringBuffer query = new StringBuffer();
			String conditionalString = "";
			final List<String> listMultiLingual = new LinkedList<>();
			final List<Class<?>> listClass = new LinkedList<>();
			if (isMaster) {
				conditionalString = " nformcode =" + userInfo.getNformcode() + " and ";
			} else if (tablename.equals("registration")) {
				conditionalString = " nregtypecode =" + registration.get(j).get("nregtypecode")
						+ " and nregsubtypecode=" + registration.get(j).get("nregsubtypecode") + " and ";
			}

			for (Map<String, Object> constraintMap : masterUniqueValidation) {
				final StringBuffer buffer = new StringBuffer();
				Set<String> lstKey = constraintMap.keySet();
				String multiLingualName = "";
				for (String key : lstKey) {
					final Map<String, Object> objMap = (Map<String, Object>) constraintMap.get(key);
					final Map<String, Object> mapMultilingual = (Map<String, Object>) objMap.get("1");
					if (multiLingualName.isEmpty()) {
						multiLingualName = (String) mapMultilingual.get(userInfo.getSlanguagetypecode());
					} else {
						multiLingualName = multiLingualName + ","
								+ mapMultilingual.get(userInfo.getSlanguagetypecode());
					}
					String data = "";
					if (jsonUIData.get(key) instanceof Integer) {
						data = String.valueOf(jsonUIData.get(key));
					} else {
						data = jsonUIData.get(key).toString();
					}
					buffer.append(" and  jsonuidata->> '" + key + "' = '" + data + "'");
				}
				listMultiLingual.add(multiLingualName);
				if (task.equalsIgnoreCase("update")) {
					buffer.append(" and " + columnName + " <>" + registration.get(j).get(columnName));
				}

				query.append("select " + columnName + " from " + tablename + " dm where " + conditionalString
						+ "  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ buffer.toString() + ";");
				listClass.add(tableName);
			}

			if (query.toString().length() > 1) {
				// List<?> lstData = findByMultiTablesPlainSqlList1(query.toString(),
				// listClass);

				List<?> lstData = getMultipleEntitiesResultSetInList(conditionalString, jdbcTemplate, tableName);
				// need to migrate above logic
				// List<?> lstData = new ArrayList<>();

				int i = 0;
				for (Map<String, Object> constraintMap : masterUniqueValidation) {
					List<?> lstData1 = (List<?>) lstData.get(i);
					if (!lstData1.isEmpty()) {
						returnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								listMultiLingual.get(i) + " " + commonFunction
										.getMultilingualMessage("IDS_ALREADYEXISTS", userInfo.getSlanguagefilename()));
						return returnObj;
					}
					i++;
				}

			}
			returnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		}
		return returnObj;
	}

	public int getApprovalConfigVersionByUserRoleTemplate(int napprovalversioncode, UserInfo userInfo)
			throws Exception {

		String str = "select count(*) from treetemplatetransactionrole ttrl " + "where ttrl.nlevelno = "
				+ "(select max(ttr.nlevelno) from treeversiontemplate tvt,treetemplatetransactionrole ttr,approvalconfigversion acv where "
				+ "tvt.ntreeversiontempcode=ttr.ntreeversiontempcode "
				+ "and tvt.ntreeversiontempcode = acv.ntreeversiontempcode "
				+ "and ttrl.ntreeversiontempcode=ttr.ntreeversiontempcode " + "and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and acv.napproveconfversioncode="
				+ napprovalversioncode + " " + "group by ttr.ntreeversiontempcode) and ttrl.nuserrolecode="
				+ userInfo.getNuserrole() + " ";
		return (int) jdbcTemplate.queryForObject(str, Integer.class);

	}

	public DesignTemplateMapping getApprovedRegistrationTemplate(short regTypeCode, short regSubTypeCode,
			int napproveConfigVersionCode) throws Exception {

		final String str = "select dm.ndesigntemplatemappingcode,dm.nreactregtemplatecode "
				+ "  from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt,approvalconfigversion acv  where  "
				+ " acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode  and  "
				+ " rt.nreactregtemplatecode=dm.nreactregtemplatecode and  dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  acv.napproveconfversioncode="
				+ napproveConfigVersionCode;

		return (DesignTemplateMapping) jdbcUtilityFunction.queryForObject(str, DesignTemplateMapping.class,
				jdbcTemplate);

	}

	public boolean patternMatches(String emailAddress) {
		String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
				+ "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
		return Pattern.compile(regexPattern).matcher(emailAddress).matches();
	}

	public boolean isDateValid(String dateString, String pattern) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			if (sdf.format(sdf.parse(dateString)).equals(dateString))
				return true;
		} catch (Exception e) {
			LOGGER.error("Exception:" + e.getMessage());
			LOGGER.error("Exception:" + e.getCause());
		}

		return false;
	}

	// ALPD-4912-To insert data when filter name input submit,done by Dhanushya RI
	public ResponseEntity<Object> createFavoriteFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();

		Map<String, Object> objMap = new HashMap<>();

		final List<FilterName> lstFilterByName = getFilterListByName(inputMap.get("sfiltername").toString(), userInfo);
		if (lstFilterByName.isEmpty()) {
			final String strValidationQuery = "select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="
					+ userInfo.getNformcode() + " and nusercode=" + userInfo.getNusercode() + " "
					+ " and nuserrolecode=" + userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " " + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and (jsondata->'nsampletypecode')::int=" + inputMap.get("nsampletypecode") + " "
					+ " and (jsondata->'nregtypecode')::int=" + inputMap.get("nregtypecode") + " "
					+ " and (jsondata->'nregsubtypecode')::int=" + inputMap.get("nregsubtypecode") + " "
					+ " and (jsontempdata->'ntranscode')::int=" + inputMap.get("ntranscode") + " "
					+ " and (jsontempdata->'napproveconfversioncode')::int=" + inputMap.get("napproveconfversioncode")
					+ " " + " and (jsontempdata->'ndesigntemplatemappingcode')::int="
					+ inputMap.get("ndesigntemplatemappingcode") + " " + " and (jsontempdata->>'DbFromDate')='"
					+ inputMap.get("FromDate") + "' " + " and (jsontempdata->>'DbToDate')='" + inputMap.get("ToDate")
					+ "' ";

			final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);

			final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null
					? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
					})
					: new ArrayList<Map<String, Object>>();
			if (lstValidationFilter.isEmpty()) {

				createFilterData(inputMap, userInfo);
				final List<FilterName> lstFilterName = getFavoriteFilterName(userInfo);
				objMap.put("FilterName", lstFilterName);
				return new ResponseEntity<Object>(objMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FILTERALREADYPRESENT",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
	}

	public Map<String, Object> getFavoriteFilterDetail(final UserInfo userInfo) throws Exception {
		// ALPD-4913-To get previously save filter details when initial get,done by
		// Dhanushya RI

		final String strQuery = // "select json_agg(jsondata || jsontempdata) as jsondata "
				" select jsondata, jsontempdata " + " from filterdetail where nformcode=" + userInfo.getNformcode()
						+ " and nusercode=" + userInfo.getNusercode() + " " + " and nuserrolecode="
						+ userInfo.getNuserrole() + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final FilterDetail filterDetail = (FilterDetail) jdbcUtilityFunction.queryForObject(strQuery,
				FilterDetail.class, jdbcTemplate);

		final Map<String, Object> returnMap = new HashMap<String, Object>();

		if (filterDetail == null) {
			return returnMap;
		} else {
			final Map<String, Object> jsonData = filterDetail.getJsondata();
			final Map<String, Object> jsonTempData = filterDetail.getJsontempdata();

			returnMap.putAll(jsonData);
			returnMap.putAll(jsonTempData);

			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

			if (returnMap.containsKey("FromDate")) {
				final Instant instantFromDate = dateUtilityFunction
						.convertStringDateToUTC(returnMap.get("FromDate").toString(), userInfo, true);

				final String fromDate = dateUtilityFunction.instantDateToString(instantFromDate);
				returnMap.put("FromDate", fromDate);

				final LocalDateTime ldt = LocalDateTime.parse(fromDate, FORMATTER1);
				String formattedFromString = "";

				if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final ZonedDateTime zonedDateFromTime = ZonedDateTime.of(ldt, ZoneId.of(userInfo.getStimezoneid()));
					formattedFromString = zonedDateFromTime.format(formatter);
				} else {
					formattedFromString = formatter.format(ldt);

				}
				returnMap.put("FromDateWOUTC", formattedFromString);
			}

			if (returnMap.containsKey("ToDate")) {
				final Instant instantToDate = dateUtilityFunction
						.convertStringDateToUTC(returnMap.get("ToDate").toString(), userInfo, true);
				final String toDate = dateUtilityFunction.instantDateToString(instantToDate);
				returnMap.put("ToDate", toDate);

				final LocalDateTime ldt1 = LocalDateTime.parse(toDate, FORMATTER1);
				String formattedToString = "";

				if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final ZonedDateTime zonedDateToTime = ZonedDateTime.of(ldt1, ZoneId.of(userInfo.getStimezoneid()));
					formattedToString = zonedDateToTime.format(formatter);

				} else {
					formattedToString = formatter.format(ldt1);
				}
				returnMap.put("ToDateWOUTC", formattedToString);
			}
			return returnMap;
		}
	}

	// ALPD-4912-To show the previously saved filter name,done by Dhanushya RI
	public List<FilterName> getFavoriteFilterName(final UserInfo userInfo) throws Exception {

		final String sFilterQry = "select * from filtername where nformcode= " + userInfo.getNformcode()
				+ " and nusercode= " + userInfo.getNusercode() + " and nuserrolecode=" + userInfo.getNuserrole()
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc;";

		return jdbcTemplate.query(sFilterQry, new FilterName());
	}

	public ResponseEntity<Map<String, Object>> getApproveConfigVersionBasedTemplate(final short nregtypecode,
			final short nregsubtypecode, final int napprovalconfigversioncode) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,CONCAT(rt.sregtemplatename,'(',cast(dm.nversionno as character varying),')') sregtemplatename from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt,approvalconfigversion acv  where  acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode  and  "
				+ " rt.nreactregtemplatecode=dm.nreactregtemplatecode and  dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  acv.napproveconfversioncode="
				+ napprovalconfigversioncode;

		final List<ReactRegistrationTemplate> lstReactRegistrationTemplate = jdbcTemplate.query(str,
				new ReactRegistrationTemplate());

		returnMap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
		returnMap.put("realDesignTemplateMappingList", lstReactRegistrationTemplate);
		returnMap.put("realDesignTemplateMapping", lstReactRegistrationTemplate.get(0));
		returnMap.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
		returnMap.put("ndesigntemplatemappingcode",
				lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());

		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	private Map<String, Object> checkSequenceListFormatNo(int nSequenceno, int listCount, final int nperiodCode,
			final String tableName, final Instant dresetDate, final UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();
		Instant dcurrentDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		if (dresetDate != null) {
			// String periodQuery="select jsondata->'speriodname'->>'en-US' as speriodname
			// from period where nperiodcode ="+nperiodCode+" and
			// nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
			// periodQuery = getJdbcTemplate().queryForObject(periodQuery, String.class);
			boolean check = false;
			int cal = 0;
			String dateFormat = "";

			if (nperiodCode == Enumeration.Period.Years.getPeriod()) {
				dateFormat = "YYYY";

			} else if (nperiodCode == Enumeration.Period.Month.getPeriod()) {
				dateFormat = "YYYY-M";

			} else if (nperiodCode == Enumeration.Period.Weeks.getPeriod()) {
				dateFormat = "yyyyMMdd";
				check = true;
				cal = Calendar.WEEK_OF_YEAR;

			} else if (nperiodCode == Enumeration.Period.Days.getPeriod()) {
				dateFormat = "yyyyMMdd";
				check = true;
				cal = Calendar.DAY_OF_YEAR;
			}

			final LocalDateTime currentDateTime = LocalDateTime.ofInstant(dcurrentDate.truncatedTo(ChronoUnit.SECONDS),
					ZoneOffset.UTC);
			String currentDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(currentDateTime);

			final LocalDateTime resetDateTime = LocalDateTime.ofInstant(dresetDate.truncatedTo(ChronoUnit.SECONDS),
					ZoneOffset.UTC);
			String resetDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(resetDateTime);

			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
			sdf.toPattern();

			String resetFormatDate = "";
			String currentFormatDate = "";
			if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				LocalDateTime ldt = sdf.parse(currentDate).toInstant().atZone(ZoneId.of(userInfo.getStimezoneid()))
						.toLocalDateTime();
				currentFormatDate = DateTimeFormatter.ofPattern(dateFormat).format(ldt);

				LocalDateTime ldt1 = sdf.parse(resetDate).toInstant().atZone(ZoneId.of(userInfo.getStimezoneid()))
						.toLocalDateTime();
				resetFormatDate = DateTimeFormatter.ofPattern(dateFormat).format(ldt1);
			} else {

				LocalDateTime ldt = sdf.parse(currentDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				currentFormatDate = DateTimeFormatter.ofPattern(dateFormat).format(ldt);

				LocalDateTime ldt1 = sdf.parse(resetDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				resetFormatDate = DateTimeFormatter.ofPattern(dateFormat).format(ldt1);
			}

			boolean bYearreset = false;
			if (check) {
				Date date2 = sdf.parse(currentFormatDate);
				Calendar c = Calendar.getInstance();
				c.setTime(date2);
				Date date3 = sdf.parse(resetFormatDate);
				Calendar c1 = Calendar.getInstance();
				c1.setTime(date3);
				int weekandDay = c.get(cal);
				int weekandDay1 = c1.get(cal);
				if (weekandDay != weekandDay1) {
					bYearreset = true;
				}
			} else if (!(currentFormatDate.equals(resetFormatDate))) {
				bYearreset = true;
			}
			if (bYearreset) {
				String seqnoUpdateQuery = "update " + tableName + " set nsequenceno=1 ,dseqresetdate='" + dcurrentDate
						+ "' where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
				jdbcTemplate.execute(seqnoUpdateQuery);
				nSequenceno = 1;
				objMap.put("sequenceno", nSequenceno);

			}
			// nSequenceno++;
			nSequenceno = nSequenceno + listCount;
			objMap.put("sequenceno", nSequenceno);

		} else {
			String seqnoUpdateQuery = "update " + tableName + " set dseqresetdate='" + dcurrentDate
					+ "' where  nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			jdbcTemplate.execute(seqnoUpdateQuery);
			nSequenceno = 1;
			objMap.put("sequenceno", nSequenceno);
		}

		return objMap;
	}

	public Map<Integer, String> getSeqfnFormatForMultipleRunningNo(List<String> list, final String mainTableName,
			final String secondaryTableName, final int nregtypecode, final int nregsubtypecode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<Integer, String> returnMap = new TreeMap<Integer, String>();
		int sequnenceno = 0;
		Instant resetDate = null;

		String seqformatsubmitterquery = " select * from " + secondaryTableName + " " + " where stablename='"
				+ mainTableName + "' " + " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "" + " and nsitecode =" + userInfo.getNmastersitecode() + " ";
		List<Map<String, Object>> objseq = jdbcTemplate.queryForList(seqformatsubmitterquery);
		// String stestcount = getJdbcTemplate().queryForObject(seqformatsubmitterquery,
		// String.class);
		sequnenceno = (int) objseq.get(0).get("nsequenceno");
		if (objseq.get(0).get("dseqresetdate") != null) {
			String dseqresetdate = (String) objseq.get(0).get("dseqresetdate").toString();
			Timestamp ts = Timestamp.valueOf(dseqresetdate);
			resetDate = ts.toInstant();
		}
		String seqformatquery = " select * from seqformatgenerator where stablename ='" + mainTableName
				+ "' and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and nsitecode =" + userInfo.getNmastersitecode() + " ";
		SeqFormatGenerator objseq1 = (SeqFormatGenerator) jdbcUtilityFunction.queryForObject(seqformatquery,
				SeqFormatGenerator.class, jdbcTemplate);

		map = checkSequenceListFormatNo(sequnenceno, list.size(), objseq1.getNperiodcode(), secondaryTableName,
				resetDate, userInfo);

		int nseqno = (int) map.get("sequenceno");

		final String strSeqnoFormatQuery = "select rsc.nperiodcode,rsc.jsondata,rsc.jsondata->>'dseqresetdate' dseqresetdate,"
				+ " rsc.jsondata->>'ssampleformat' ssampleformat, sag.nsequenceno,sag.nseqnoarnogencode,rsc.nregsubtypeversioncode"
				+ " from  approvalconfig ap, seqnoarnogenerator sag,regsubtypeconfigversion rsc "
				+ " where  sag.nseqnoarnogencode = rsc.nseqnoarnogencode " + " and sag.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ap.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregtypecode = " + nregtypecode
				+ " and nregsubtypecode =" + (int) nregsubtypecode
				+ " and rsc.napprovalconfigcode=ap.napprovalconfigcode and rsc.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
		SeqNoArnoGenerator seqnoFormat = (SeqNoArnoGenerator) jdbcUtilityFunction.queryForObject(strSeqnoFormatQuery,
				SeqNoArnoGenerator.class, jdbcTemplate);

		String siteFormat = seqnoFormat.getSsampleformat();

		String sFormat = objseq1.getSformattype();

		int siteSubStringStart = siteFormat.indexOf('{');
		int siteSubStringEnd = siteFormat.indexOf('}');
		String siteSubString = siteFormat.substring(siteSubStringStart + 1, siteSubStringEnd);

		if (siteSubString.equalsIgnoreCase("XXXXX")) {
			String siteCodeString = userInfo.getSsitecode();
			sFormat = siteCodeString + "/" + sFormat;
		}

		for (int i = 0; i < list.size(); i++) {

			sequnenceno++;
			String seqFormat = sFormat;

			if (seqFormat != null) {
				while (seqFormat.contains("{")) {
					int start = seqFormat.indexOf('{');
					int end = seqFormat.indexOf('}');

					String subString = seqFormat.substring(start + 1, end);
					if (subString.equals("yy") || subString.equals("YY")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yy", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("yyyy") || subString.equals("YYYY")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("mm") || subString.equals("MM")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);

						// int Y = 0;
					} else if (subString.equals("mmm") || subString.equals("MMM")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);

						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
						// int Y = 0;
					} else if (subString.equals("mon") || subString.equals("MON")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MON", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("dd") || subString.equals("DD")) {
						SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.matches("9+")) {
						String seqPadding = "%0" + subString.length() + "d";
						String replaceString = String.format(seqPadding, sequnenceno);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					}

					else {
						seqFormat = seqFormat.replace('{' + subString + '}', subString);
					}
				}
			}
			returnMap.put(Integer.parseInt(list.get(i)), seqFormat);
		}
		// nseqno++;
		String seqnoUpdateQuery = "update " + secondaryTableName + " set nsequenceno=" + nseqno + ""
				+ " where stablename='" + mainTableName + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and  nsitecode="
				+ userInfo.getNmastersitecode() + "";
		jdbcTemplate.execute(seqnoUpdateQuery);

		return returnMap;

	}

	public ResponseEntity<Map<String, Object>> getReactRegistrationTemplateList(short nregtypecode,
			short nregsubtypecode, boolean nneedtemplatebasedflow, UserInfo userInfo) throws Exception {

		String stranstatus = nneedtemplatebasedflow
				? Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
				: String.valueOf(Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
		Map<String, Object> returnMap = new HashMap<String, Object>();

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,rt.sregtemplatename from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt where nregtypecode=" + nregtypecode + " and nregsubtypecode="
				+ nregsubtypecode + " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and " + " dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nsitecode = "
				+ userInfo.getNmastersitecode() + " and dm.nsitecode = " + userInfo.getNmastersitecode()
				+ " and dm.ntransactionstatus in" + " (" + stranstatus + ") order by dm.ntransactionstatus desc";

		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = jdbcTemplate.query(str,
				new ReactRegistrationTemplate());

		returnMap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
		returnMap.put("realDesignTemplateMappingList", lstReactRegistrationTemplate);
		returnMap.put("DesignTemplateMappingValue", lstReactRegistrationTemplate.get(0));
		returnMap.put("ndesigntemplatemappingcode",
				lstReactRegistrationTemplate.get(0).getNdesigntemplatemappingcode());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	public Map<String, Object> auditInsert(UserInfo userInfo, String dataStatus) throws Exception {
		String sauditaction = "";
		Map<String, Object> outputMap = new HashMap<String, Object>();
		if (userInfo.getNusercode() != -1) {
			sauditaction = "IDS_MANUALSYNC";

		} else {
			sauditaction = "IDS_AUTOSYNC";

		}

		outputMap.put("isSync", true);
		outputMap.put("dataStatus", dataStatus);
		outputMap.put("sauditaction", sauditaction);

		ResourceBundle resourcebundle = new PropertyResourceBundle(new InputStreamReader(getClass().getClassLoader()
				.getResourceAsStream("/com/properties/" + userInfo.getSlanguagefilename() + ".properties"), "UTF-8"));

		String scomments = resourcebundle.containsKey("IDS_SYNCATTEMPT")
				? resourcebundle.getString("IDS_SYNCATTEMPT") + ": " + resourcebundle.getString(dataStatus) + ";"
				: "Sync Attempted : " + " No Data Modified" + "; ";
		outputMap.put("scomments", scomments);
		return outputMap;
	}

	public ResponseEntity<Object> createException(Exception e, UserInfo userinfo) {

		Map<String, Object> map = new HashMap<String, Object>();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		final String date = dtf.format(now);

		String stackTrace = getStackTrace(e);

		map.put("stacktrace", stackTrace);
		map.put("message", e.toString());
		map.put("sitename", userinfo.getSsitename());
		map.put("username", userinfo.getSusername());
		map.put("userrolename", userinfo.getSuserrolename());
		map.put("formname", userinfo.getSformname());
		map.put("modulename", "Configuration");
		map.put("exceptiondate", date);

		JSONObject jsonStackTrace = new JSONObject(map);

		jdbcTemplate.execute("Insert into jsonexceptionlogs(jsondata, dmodifieddate, nsitecode, nstatus )" + " values('"
				+ stringUtilityFunction.replaceQuote(jsonStackTrace.toString()) + "'::jsonb, '" + date + "',"
				+ " -1, 1 )");

		return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
	}

	public static String getStackTrace(final Throwable throwable) {

		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);

		throwable.printStackTrace(pw);
		String printStringTrace = sw.getBuffer().toString();

		return printStringTrace;
	}
	
//	public List<QualisForms> getERDiagram(int nusercode, int nuserrolecode, int nmasterSiteCode,
//			String slanguageTypeCode) throws Exception {
//		
//		final String queryString =  "select qf.nformcode, qf.sclassname, "
//								+ " coalesce(qf.jsondata->'sdisplayname'->>'" + slanguageTypeCode + "',sformname) as sdisplayname," 
//								+ " coalesce(qf.jsondata->>'erdiagram','') as serdiagram," 
//								+ " sformname ,"
//								+ " qf.nmodulecode, qf.nmenucode, qf.nsorter, qf.surl from "
//								+ " usersrolescreen urs, qualisforms qf, sitequalisforms sqf"
//								+ " where qf.nformcode = urs.nformcode and qf.nformcode = sqf.nformcode" 
//								+ " and sqf.nsitecode = " + nmasterSiteCode 
//								+ " and qf.nformcode not in (select"
//								+ " nformcode from usersrolescreenhide where nuserrolecode=" + nuserrolecode
//								+ " and nusercode=" + nusercode
//								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//								+ " and needrights=" + Enumeration.TransactionStatus.YES.gettransactionstatus() + ")" 
//								+ " and qf.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//								+ " and urs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//								+ " and urs.nuserrolecode="+ nuserrolecode 
//								+ " and sqf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//								+ " and coalesce(jsondata->>'erdiagram', '') =''"
//								+ " order by qf.nmenucode,qf.nmodulecode,qf.nsorter";
//		final List<QualisForms> qualisFormList = (List<QualisForms>) jdbcTemplate.query(queryString, new QualisForms());
//		return qualisFormList;
//	}
	
	
		public String getUserEmail(final UserInfo userInfo) throws Exception {
			
			return (String) jdbcUtilityFunction.queryForObject(" select semail from users where nusercode="
					+ userInfo.getNusercode() +" "
					+ "and nsitecode=" +userInfo.getNmastersitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), 
					String.class, jdbcTemplate);
		}
		
		// ALPD-6042--Added by vignesh(21-07-2025)--for Prepare message to Email.
		public Message prepareMessage(jakarta.mail.Session session, String senderMail, String sreceipientname,
				String subject, String body, String filePath) throws AddressException, MessagingException, IOException {
			MimeMessage mimemessage = new MimeMessage(session);
			mimemessage.setFrom(new InternetAddress(senderMail));
			mimemessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sreceipientname));
			mimemessage.setSubject(subject);
	
			BodyPart messageBodyPart1 = new MimeBodyPart();
			messageBodyPart1.setContent(body, "text/html");
	
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart1);
	
			if (filePath != null && !filePath.isEmpty()) {
				MimeBodyPart messageBodyPart2 = new MimeBodyPart();
				messageBodyPart2.attachFile(filePath);
				multipart.addBodyPart(messageBodyPart2);
			}
			mimemessage.setContent(multipart);
			return mimemessage;
		}
		
		//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
		public UserMFA getUserMFA(final int nuserCode, final int nfmaNeed,final boolean isInitalGet) throws Exception {
			
			String concatStr = "";
			
			if(!isInitalGet) {
				if (nfmaNeed == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					//user specific MFA
					concatStr = " and nactivestatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus() + " ";
				}
			}
			final String queryString = "select * from usermfa um,mfatype mt,transactionstatus ts where nusercode="
										+ nuserCode + " " + concatStr + "" + " and ts.ntranscode=um.nactivestatus "
										+ " and mt.nmfatypecode=um.nmfatypecode  and  um.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  mt.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  ts.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

			final UserMFA objUserMfa = (UserMFA) jdbcUtilityFunction
					.queryForObject(queryString, UserMFA.class, jdbcTemplate);

			return objUserMfa;

		}
		
		
		//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
		public Map<String, Object> getMFAType(final UserInfo userInfo) throws Exception {
			
			final Map<String, Object> outputMap = new HashMap<String, Object>();
			final String smfaTypeQuery = "select nmfatypecode,coalesce(jsondata->'smfatypename'->>'"
										+ userInfo.getSlanguagetypecode()
										+ "',jsondata->'smfatypename'->>'en-US') as smfatype "
										+ " from mfatype where nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
										//+ " and nsitecode=-1";
			final List<Map<String, Object>> listMFAType = jdbcTemplate.queryForList(smfaTypeQuery);

			outputMap.put("mfaType", listMFAType);
			return outputMap;
		}
		
		
		public String generateRandomNumber(final int digitCount) {
			return String.format("%"+ digitCount + "d", new SecureRandom().nextInt(999999));
		}
		
		public String idGenerator(final int length, final String idType) {
			 
			//final char[] alphaNumeric = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
			   
			char[] inputData = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();;
			if(idType == "number") {
				inputData = "0123456789".toCharArray();
			}
			final SecureRandom random = new SecureRandom();

		    char[] result = new char[length];
	        for (int i = 0; i < length; i++) {
	            result[i] = inputData[random.nextInt(inputData.length)];
	        }
		    return new String(result);
		}
		
		
		//ALPDJ21-19--Added by vignesh(21-07-2025)--for OTP send to the Email.
		public void insertOneTimePassword(final String soneTimePasswordCode, final String soneTimePassword, final UserInfo userInfo)
				throws Exception {
//			Instant date = userInfo == null
//					? LocalDateTime.now().toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS)
//					: dateUtilityFunction.getCurrentDateTime(userInfo);
			
			final String sQuery = " insert into onetimepassword(sonetimepasswordcode, sonetimepassword,dcreateddate, "
								+ "dmodifieddate, nsitecode, nstatus) VALUES "
								+ "(N'" + soneTimePasswordCode + "', '" + soneTimePassword + "',"
								//+ " N'" + date + "'::timestamp, " 
								//+ "N'" + date + "'::timestamp, "
								+ " '"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
								+ " '"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
								+ userInfo.getNmastersitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";

			jdbcTemplate.execute(sQuery);
		}
		
		public void setJWTResponseHeader(final HttpServletRequest request,final HttpServletResponse response,
				final UserInfo userInfo) throws Exception
		{
			// Added by gowtham on 23 june, ALPD-6035 - JWT
			boolean isSecure = request.getScheme().equalsIgnoreCase("https");
			String sameSite = isSecure ? "None" : "Lax";

			ResponseCookie cookie = ResponseCookie.from("jwt", jwtUtilityFunction.generateToken(userInfo.getSsessionid(), userInfo.getNexpirytime()))
			    .httpOnly(true)
			    .secure(isSecure)
			    .path("/")
			    .sameSite(sameSite)
			    .maxAge(60 * (userInfo.getNexpirytime() > 0 ? userInfo.getNexpirytime() : (60 * 24))) // seconds * minutes * hours
			    .build();
			
			response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		}
		
		
}