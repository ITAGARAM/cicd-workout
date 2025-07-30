package com.agaramtech.qualis.global;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Id;

//@Lazy
@Component
public class JdbcTemplateUtilityFunction {

	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplateUtilityFunction.class);

	private final ClassUtilityFunction classUtilityFunction;
	private final DateTimeUtilityFunction dateUtility_Function;
	private final CommonFunction common_Function;
	private static final char COLUMN_PLACEHOLDER = '?';
	private static final String DATE_FORMAT = "yyyy-mm-dd HH:mm:ss";
	private static final String INSTANT_DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss'Z'";

	/**
	 * @param classUtilityFunction
	 * @param dateUtility_Function
	 * @param common_Function
	 */
	public JdbcTemplateUtilityFunction(ClassUtilityFunction classUtilityFunction,
			DateTimeUtilityFunction dateUtility_Function, CommonFunction common_Function) {
		this.classUtilityFunction = classUtilityFunction;
		this.dateUtility_Function = dateUtility_Function;
		this.common_Function = common_Function;
	}

	public Object saveAnObject(Object object, Class<?> sequenceTableName, JdbcTemplate jdbcTemplate,
			String sequenceNoColumnName) throws Exception {
		StringBuilder queryBuilder = null;
		StringBuilder queryParameters = null;
		Field[] tableColumns = object.getClass().getDeclaredFields();
		List<String> tableFields = null;
		List<Character> tableColumnPlaceHolder = null;
		try {
			if (tableColumns.length > 0) {
				tableFields = new ArrayList<>();
				tableColumnPlaceHolder = new ArrayList<>();
				queryBuilder = new StringBuilder();
				queryParameters = new StringBuilder();
				for (Field f : tableColumns) {
					f.setAccessible(true);
					if (!Modifier.isTransient(f.getModifiers())) {
						tableFields.add(f.getName().toLowerCase());
						tableColumnPlaceHolder.add(COLUMN_PLACEHOLDER);
						switch (f.getType().getSimpleName()) {
						case "int":
							queryParameters.append(f.getInt(object));
							break;
						case "short":
							queryParameters.append(f.getShort(object));
							break;
						case "long":
							queryParameters.append(f.getLong(object));
							break;
						case "String":
							queryParameters.append((String) f.get(object));
							break;
						case "byte":
							queryParameters.append(f.getByte(object));
							break;
						case "byte[]":
							Object byteObject = f.get(object);
							Byte[] castedObject = (Byte[]) byteObject;
							byte[] bytes = new byte[castedObject.length];
							int b = 0;
							for (Byte byt : castedObject)
								bytes[b++] = byt.byteValue();
							queryParameters.append(bytes);
							break;
						case "float":
							queryParameters.append(f.getFloat(object));
							break;
						case "double":
							queryParameters.append(f.getDouble(object));
							break;
						case "Date":
							Object dateObject = f.get(object);
							Date date = null;
							String dateInString = dateObject.toString();
							date = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(dateInString);
							queryParameters.append(date);
							break;
						case "char":
							queryParameters.append(f.getChar(object));
							break;
						case "Instant":
							Object instantdDateObject = f.get(object);
							Date instantDate = null;
							String instantDateInString = instantdDateObject.toString();
							instantDate = new SimpleDateFormat(INSTANT_DATE_FORMAT).parse(instantDateInString);
							queryParameters.append(instantDate);
							break;
						default:
							break;
						}
					}
				}
				queryBuilder.append("INSERT INTO ").append(classUtilityFunction.getEntityTableName(object.getClass()))
						.append(" ")
						.append("(" + Arrays.toString(tableFields.toArray()).substring(
								Arrays.toString(tableFields.toArray()).indexOf('['),
								Arrays.toString(tableFields.toArray()).indexOf(']')) + ")")
						.append(" VALUES(" + Arrays.toString(tableColumnPlaceHolder.toArray()).substring(
								Arrays.toString(tableColumnPlaceHolder.toArray()).indexOf('['),
								Arrays.toString(tableColumnPlaceHolder.toArray()).indexOf(']')) + ")");
				int nRowsUpdated = jdbcTemplate.update(queryBuilder.toString(), queryParameters);
				if (nRowsUpdated > 0) {
					String query = "select " + sequenceNoColumnName + " from "
							+ classUtilityFunction.getEntityTableName(sequenceTableName.getClass()) + " where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					int nCurrentSequenceNo = jdbcTemplate.queryForObject(query, Integer.class);
					jdbcTemplate
							.execute("update " + classUtilityFunction.getEntityTableName(sequenceTableName.getClass())
									+ " set " + sequenceNoColumnName + "=" + nCurrentSequenceNo + nRowsUpdated);
				}
			}
		} catch (Exception e) {
			throw (e);
		}
		return object;
	}

	public List<?> saveBatchOfObjects(List<?> objects, Class<?> sequenceTableName, JdbcTemplate jdbcTemplate,
			String sequenceNoColumnName) throws Exception {
		StringBuilder queryBuilder = null;
		Field[] tableColumns = objects.getFirst().getClass().getDeclaredFields();
		List<String> tableFields = null;
		List<Character> tableColumnPlaceHolder = null;
		List<Field> actualFields = null;
		int nCurrentSequenceNo = 0;
		int nUpdatedSequenceNo = 0;
		try {
			if (tableColumns.length > 0) {
				tableFields = new ArrayList<>();
				tableColumnPlaceHolder = new ArrayList<>();
				queryBuilder = new StringBuilder();
				actualFields = new ArrayList<>();
				for (Field f : tableColumns) {
					f.setAccessible(true);
					System.out.println(f);
					if (!Modifier.isTransient(f.getModifiers()) && !Modifier.isStatic(f.getModifiers())) {
						tableFields.add(f.getName().toLowerCase());
						tableColumnPlaceHolder.add(COLUMN_PLACEHOLDER);
						actualFields.add(f);
					}
				}
				queryBuilder.append("INSERT INTO ")
						.append(classUtilityFunction.getEntityTableName(objects.getFirst().getClass())).append(" ")
						.append("("
								+ Arrays.toString(tableFields.toArray())
										.substring(Arrays.toString(tableFields.toArray()).indexOf('[') + 1,
												Arrays.toString(tableFields.toArray()).indexOf(']'))
								+ ")")
						.append(" VALUES(" + Arrays.toString(tableColumnPlaceHolder.toArray()).substring(
								Arrays.toString(tableColumnPlaceHolder.toArray()).indexOf('[') + 1,
								Arrays.toString(tableColumnPlaceHolder.toArray()).indexOf(']')) + ")");
				Field[] f = Arrays.copyOf(actualFields.toArray(), actualFields.size(), Field[].class);

				String query = "select " + sequenceNoColumnName + " from "
						+ classUtilityFunction.getEntityTableName(sequenceTableName) + " where stablename = '"
						+ classUtilityFunction.getEntityTableName(objects.getFirst().getClass()) + "' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				nCurrentSequenceNo = jdbcTemplate.queryForObject(query, Integer.class);
				List<?> objectsWithSequenceNo = setSequenceNoForAnEntityObject(objects, nCurrentSequenceNo);

				int[] nAffectedRows = jdbcTemplate.batchUpdate(queryBuilder.toString(),
						new BatchPreparedStatementSetter() {
							@Override
							public void setValues(PreparedStatement ps, int i) throws SQLException {
								Object persistObject = objectsWithSequenceNo.get(i);
								for (int j = 0; j < f.length; j++) {
									try {
										f[j].setAccessible(true);
										switch (f[j].getType().getSimpleName()) {
										case "int":
											ps.setObject(j + 1, f[j].getInt(persistObject));
											break;
										case "short":
											ps.setObject(j + 1, f[j].getShort(persistObject));
											break;
										case "long":
											ps.setObject(j + 1, f[j].getLong(persistObject));
											break;
										case "String":
											Object stringObject = f[j].get(persistObject);
											String value = (String) stringObject;
											ps.setObject(j + 1, value);
											break;
										case "byte":
											ps.setObject(j + 1, f[j].getByte(persistObject));
											break;
										case "byte[]":
											Object byteObject = f[j].get(persistObject);
											Byte[] castedObject = (Byte[]) byteObject;
											byte[] bytes = new byte[castedObject.length];
											int b = 0;
											for (Byte byt : castedObject)
												bytes[b++] = byt.byteValue();
											ps.setObject(j + 1, bytes);
											break;
										case "float":
											ps.setObject(j + 1, f[j].getInt(persistObject));
											break;
										case "double":
											ps.setObject(j + 1, f[j].getDouble(persistObject));
											break;
										case "Date":
											Object dateObject = f[j].get(persistObject);
											Date date = null;
											String dateInString = dateObject.toString();
											date = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
													.parse(dateInString);
											ps.setObject(j + 1, date);
											break;
										case "char":
											ps.setObject(j + 1, f[j].getChar(persistObject));
											break;
										case "Instant":
											// Object instantdDateObject = f[j].get(persistObject);
											// //Instant instantDate = null;
											// Date instantDate = null;
											// String instantDateInString = instantdDateObject.toString();
											// instantDate = new SimpleDateFormat(INSTANT_DATE_FORMAT)
											// .parse(instantDateInString);
											// //instantDate =
											// LocalDateTime.now().toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
											// ps.setObject(j + 1, (Timestamp)instantdDateObject);//(java.sql.Timestamp)

											Object instantdDateObject = f[j].get(persistObject);
											String instantDateInString = instantdDateObject.toString();
											Date instantDate = new SimpleDateFormat(INSTANT_DATE_FORMAT)
													.parse(instantDateInString);
											Timestamp timestamp = new Timestamp(instantDate.getTime());
											ps.setObject(j + 1, timestamp);
											break;
										default:
											break;
										}
									} catch (IllegalAccessException | ParseException e) {
										throw new SQLException(
												"Failed to access field value in saveBatchOfOjects() method", e);
									} catch (IllegalArgumentException | NullPointerException e) {
										throw new IllegalArgumentException(
												"Failed to modify the List of Objects in saveBatchOfOjects() method",
												e);
									}
								}
							}

							@Override
							public int getBatchSize() {
								return objects.size();
							}
						});
				nUpdatedSequenceNo = nCurrentSequenceNo + nAffectedRows.length;
				jdbcTemplate.execute("update " + classUtilityFunction.getEntityTableName(sequenceTableName) + " set "
						+ sequenceNoColumnName + "="+nUpdatedSequenceNo+" where stablename ='"
								+ classUtilityFunction.getEntityTableName(objects.getFirst().getClass()) + "' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() );
			}
		} catch (Exception e) {
			throw (e);
		}
		return new ArrayList<>(objects);
	}

	public Object queryForObject(String query, Class<?> entityClass, JdbcTemplate jdbcTemplate) throws Exception {
		try {
			if (classUtilityFunction.isWrapperClass(entityClass) || entityClass.getName().equals("java.lang.String")) {
				return jdbcTemplate.queryForObject(query, entityClass);
			} else {
				return jdbcTemplate.queryForObject(query,
						(RowMapper<?>) classUtilityFunction.getNewInstanceOfAnClass(entityClass.getName()));
			}
		} catch (EmptyResultDataAccessException e) {
			//LOGGER.error("Exception:" + e);
			return null;
		} catch (Exception e) {
			//LOGGER.error("Exception:" + e);
			return e;
		}
	}

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
				lsData = oMapper.convertValue(
						dateUtility_Function.convertDateTimeToString(lsData, dateColumn, userInfo),
						new TypeReference<List<Map<String, Object>>>() {
						});
				lsData = oMapper.convertValue(dateUtility_Function.getSiteLocalTimeFromUTC(lsData, dateColumn, null,
						userInfo, true, multilingualColumn, false), new TypeReference<List<Map<String, Object>>>() {
						});
			} else if (!dateColumn.isEmpty() && multilingualColumn.isEmpty()) {
				lsData = oMapper.convertValue(dateUtility_Function.getSiteLocalTimeFromUTC(lsData, dateColumn, null,
						userInfo, false, null, false), new TypeReference<List<Map<String, Object>>>() {
						});
			} else if (dateColumn.isEmpty() && !multilingualColumn.isEmpty()) {
				lsData = oMapper.convertValue(common_Function.getMultilingualMessageList(lsData, multilingualColumn,
						userInfo.getSlanguagefilename()), new TypeReference<List<Map<String, Object>>>() {
						});
			}
		}
		return lsData;
	}

	public synchronized List<Object> setSequenceNoForAnEntityObject(List<?> object, int nCurrentsequenceNo)
			throws Exception {
		List<Object> modifiedObject = null;
		try {
			if (object != null && object.size() > 0) {
				modifiedObject = new ArrayList<Object>();
				++nCurrentsequenceNo;
				for (Object o : object) {
					for (Field field : o.getClass().getDeclaredFields()) {
						Field fieldValue = null;
						if (field.isAnnotationPresent(Id.class)) {
							fieldValue = field;
							fieldValue.setAccessible(true);
							fieldValue.setInt(o, nCurrentsequenceNo);
							modifiedObject.add(o);
						}
					}
					nCurrentsequenceNo++;
				}
			}
		} catch (Exception e) {
			throw (e);
		}
		return modifiedObject;
	}
}
