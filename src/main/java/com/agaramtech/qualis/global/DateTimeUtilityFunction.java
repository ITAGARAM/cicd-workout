package com.agaramtech.qualis.global;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.agaramtech.qualis.basemaster.model.TimeZone;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class DateTimeUtilityFunction {
	
	private final JdbcTemplate jdbc_Template;	

	/**
	 * @param jdbc_Template
	 */
	public DateTimeUtilityFunction(JdbcTemplate jdbc_Template) {
		this.jdbc_Template = jdbc_Template;
	}

	//private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	public Instant getCurrentDateTime(final UserInfo userInfo) throws Exception {
		Instant date = null;
		if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			date = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		} else {
			date = LocalDateTime.now().toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
		}
		return date;
	}

	public Instant getUTCDateTime() throws ParseException {
		return Instant.now().truncatedTo(ChronoUnit.SECONDS);
	}

//	public void getParsedDateTimeWithoutTimeZone(String dateTime) {
//		final DateTimeFormatter customFormat = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
//		LocalDateTime ldt = LocalDateTime.parse(dateTime, customFormat);
//	}

	public List<?> getSiteLocalTimeFromUTC(final List<?> lsts, final List<String> lstsearchField,
			final List<String> lstselectedtimeZone, final UserInfo userinfo, boolean MultilingualNeed,
			List<String> lstMultilingualField, final boolean isdbdateformat)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
//		 List<TimeZone> lstTimeZone = new ArrayList<TimeZone>();
//		if (lstselectedtimeZone != null) {
//			lstTimeZone = (List<TimeZone>) jdbc_Template.query("select * from timezone where nstatus ="
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), new TimeZone());
//		}
		final DateTimeFormatter destFormat = DateTimeFormatter.ofPattern(userinfo.getSdatetimeformat());
		//SimpleDateFormat destFormat1 = new SimpleDateFormat(userinfo.getSsitedatetime());
		SimpleDateFormat sourceFormat = new SimpleDateFormat(userinfo.getSdatetimeformat().replace("'T'", " "));

		if (isdbdateformat) {
			sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		List<Map<String, Object>> finalList = new ArrayList<>();
		ResourceBundle resourcebundle = null;
		if (MultilingualNeed) {
			resourcebundle = new PropertyResourceBundle(
					new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
							Enumeration.Path.PROPERTIES_FILE.getPath() + userinfo.getSlanguagefilename() + ".properties"), "UTF-8"));
			//resourcebundle = commonFunction.getResourceBundle(userinfo.getSlanguagefilename(), false);
			
		}
		List<Map<String, Object>> lst = objMapper.convertValue(lsts, new TypeReference<List<Map<String, Object>>>() {
		});

		for (int i = 0; i < lst.size(); i++) {
			Map<String, Object> map = lst.get(i);
			for (int j = 0; j < lstsearchField.size(); j++) {
				if (map.get(lstsearchField.get(j)) != null
						&& map.get(lstsearchField.get(j)).getClass().getName() == "java.lang.Long") {
					map.put(lstsearchField.get(j), sourceFormat.format(map.get(lstsearchField.get(j))));
				}
				if ((String) map.get(lstsearchField.get(j)) != null && !map.get(lstsearchField.get(j)).equals("")
						&& !map.get(lstsearchField.get(j)).equals("-")) {

					if (lstselectedtimeZone != null) {
						if (userinfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							if (j < lstselectedtimeZone.size()) {
								final LocalDateTime ldt = sourceFormat.parse((String) map.get(lstsearchField.get(j)))
										.toInstant()
										.atOffset(ZoneOffset.ofTotalSeconds(
												(int) map.get("noffsetd" + lstsearchField.get(j).substring(1))))
										.toLocalDateTime();
								map.put(lstsearchField.get(j), destFormat.format(ldt));
							} else {
								final LocalDateTime ldt = sourceFormat.parse((String) map.get(lstsearchField.get(j)))
										.toInstant()
										.atOffset(ZoneOffset.ofTotalSeconds(
												(int) map.get("noffsetd" + lstsearchField.get(j).substring(1))))
										.toLocalDateTime();
								map.put(lstsearchField.get(j), destFormat.format(ldt));
							}
						} else {
							LocalDateTime ldt = sourceFormat.parse((String) map.get(lstsearchField.get(j))).toInstant()
									.atZone(ZoneId.systemDefault()).toLocalDateTime();
							map.put(lstsearchField.get(j), destFormat.format(ldt));
						}

					} else {
						if (userinfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							LocalDateTime ldt = sourceFormat.parse((String) map.get(lstsearchField.get(j))).toInstant()
									.atZone(ZoneId.of(userinfo.getStimezoneid())).toLocalDateTime();
							map.put(lstsearchField.get(j), destFormat.format(ldt));
						} else {
							LocalDateTime ldt = sourceFormat.parse((String) map.get(lstsearchField.get(j))).toInstant()
									.atZone(ZoneId.systemDefault()).toLocalDateTime();
							map.put(lstsearchField.get(j), destFormat.format(ldt));
						}
					}
				} else {
					map.put(lstsearchField.get(j), "-");
				}
				if (MultilingualNeed) {
					for (int l = 0; l < lstMultilingualField.size(); l++) {
						map.replace(lstMultilingualField.get(l).trim(),
								resourcebundle.containsKey((String) map.get(lstMultilingualField.get(l).trim()))
										? resourcebundle.getString((String) map.get(lstMultilingualField.get(l).trim()))
										: (String) map.get(lstMultilingualField.get(l).trim()));
					}
				}
			}
			finalList.add(i, map);
		}
		return finalList;
	}
	
	public List<Map<String, Object>> convertDateTimeToString(List<Map<String, Object>> lsData, List<String> dateColumn,
			UserInfo userInfo) {
		final String sourceFormat = "yyyy-MM-dd'T'HH:mm:ss";
		for (int i = 0; i < lsData.size(); i++) {
			for (String column : dateColumn) {
				Timestamp t = (Timestamp) lsData.get(i).get(column);
				Instant instantDate = t.toInstant();
				final LocalDateTime datetime = LocalDateTime.ofInstant(instantDate.truncatedTo(ChronoUnit.SECONDS),
						ZoneOffset.UTC);
				final String formatted = DateTimeFormatter.ofPattern(sourceFormat).format(datetime).replace("T", " ");
				lsData.get(i).put(column, formatted);
			}
		}
		return lsData;
	}
	
	/**
	 * Convenience method to add a specified number of minutes to a Date object
	 * 
	 * @param holdMinutes The number of minutes to add
	 * @param date        The time that will have minutes added to it
	 * @return A date object with the specified number of minutes added to it
	 */
	public Date addMinutesToDate(Date date, int holdMinutes) {
		final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

		long curTimeInMs = date.getTime();
		Date afterAddingMins = new Date(curTimeInMs + (holdMinutes * ONE_MINUTE_IN_MILLIS));
		return afterAddingMins;
	}

	public Date getStartOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public Date getEndOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	public String getDBFormatStringDate(final Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormat.format(date);
		return strDate;
	}

	public long getDifferenceDays(Date d1, Date d2) {
		long diff = d2.getTime() - d1.getTime();
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		// return ChronoUnit.DAYS.between(d1.toInstant(), d2.toInstant());
	}

	/**
	 * 
	 * @author ATE153
	 *
	 */
	public String instantDateToString(final Instant instantDate) throws Exception {

		String sourceFormat = "yyyy-MM-dd'T'HH:mm:ss";
		final LocalDateTime datetime = LocalDateTime.ofInstant(instantDate.truncatedTo(ChronoUnit.SECONDS),
				ZoneOffset.UTC);
		final String formatted = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);
		return formatted;
	}

	public String instantDateToStringWithFormat(final Instant instantDate, String sourceFormat) throws Exception {

		//String sourceFormat = sourceFormat1;
		final LocalDateTime datetime = LocalDateTime.ofInstant(instantDate.truncatedTo(ChronoUnit.SECONDS),
				ZoneOffset.UTC);
		final String formatted = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);
		return formatted;
	}

//	public String instantDateToStringwithformat(final Instant instantDate, String sourceFormat) throws Exception {
//
//		// String sourceFormat = "yyyy-MM-dd'T'HH:mm:ss";
//		final LocalDateTime datetime = LocalDateTime.ofInstant(instantDate.truncatedTo(ChronoUnit.SECONDS),
//				ZoneOffset.UTC);
//		final String formatted = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);
//		return formatted;
//	}

	public Instant convertStringDateToUTC(final String stringDate, final UserInfo userinfo,
			final boolean excludeTZFormat) throws Exception {

		String sourceFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		if (excludeTZFormat) {
			sourceFormat = "yyyy-MM-dd'T'HH:mm:ss";
		}

		if (userinfo.getNtimezonecode() > 0) {
			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(sourceFormat);
			final LocalDateTime ldt = LocalDateTime.parse(stringDate, formatter);

			final ZoneId zoneId = ZoneId.of(userinfo.getStimezoneid());
			final ZonedDateTime zdt = ldt.atZone(zoneId);
			final Instant instantDate = zdt.toInstant();

			return instantDate;
		} else {

			if (excludeTZFormat) {
				return Instant.parse(stringDate.concat("Z"));
			} else {
				return Instant.parse(stringDate);
			}
		}
	}
	
	public int getCurrentDateTimeOffset(final String stimezoneid) throws Exception {
		final ZoneId zone = ZoneId.of(stimezoneid);
		final LocalDateTime dt = LocalDateTime.now();
		final ZonedDateTime zdt = dt.atZone(zone);
		final ZoneOffset offset = zdt.getOffset();
		final int offSet = offset.getTotalSeconds();
		return offSet;
	}

	public int getCurrentDateTimeOffsetFromDate(String Date, final String stimezoneid) throws Exception {
		final ZoneId zone = ZoneId.of(stimezoneid);
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		final LocalDateTime dt = LocalDateTime.parse(Date, formatter);
		final ZonedDateTime zdt = dt.atZone(zone);
		final ZoneOffset offset = zdt.getOffset();
		final int offSet = offset.getTotalSeconds();
		return offSet;
	}
	
	public String convertUTCToSiteTime(Instant instantDate, UserInfo userInfo) throws ParseException {
		
		if (userInfo.getIsutcenabled()== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			final LocalDateTime LocalDatetime = LocalDateTime.ofInstant(instantDate.truncatedTo(ChronoUnit.SECONDS),
					ZoneId.of(userInfo.getStimezoneid()));
			final DateTimeFormatter destFormat = DateTimeFormatter.ofPattern(userInfo.getSsitedatetime());
			return destFormat.format(LocalDatetime);
		} else {
			final LocalDateTime localDateTime = LocalDateTime.ofInstant(instantDate, ZoneOffset.UTC);
			return DateTimeFormatter.ofPattern(userInfo.getSsitedatetime()).format(localDateTime);
		}
	}
	
	public Object convertInputDateToUTCByZone(final Object inputObject, final List<String> inputFieldList,
			final List<String> timeZoneList, final boolean returnAsString, final UserInfo userInfo// , final boolean
																									// includeMilliseconds
	) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
	
		final Map<String, Object> map = objMapper.convertValue(inputObject, Map.class);
		Object returnObject = new Object();

		if (userInfo.getIsutcenabled()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			final List<TimeZone> lstTimeZone = (List<TimeZone>) jdbc_Template.query("select * from timezone where nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), new TimeZone());
					
			//String instantSourceFormat = "yyyy-MM-dd'T'HH:mm:ss";
			String sourceFormat = "yyyy-MM-dd HH:mm:ss";
			
			for (int i = 0; i < inputFieldList.size(); i++) {
				int k = i;
				//final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(instantSourceFormat);
				
				final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(sourceFormat);
				//SimpleDateFormat sourceFormat1 = new SimpleDateFormat(sourceFormat);

				final TimeZone objTimeZone = lstTimeZone.stream()
						.filter(obj -> (obj.getNtimezonecode() ==(short)map.get(timeZoneList.get(k)) )).findAny()
						.orElse(null);

				if (objTimeZone != null) {					
					
					int offset = getCurrentDateTimeOffsetFromDate((String) map.get(inputFieldList.get(i)),
							objTimeZone.getStimezoneid());					

					final String dateField = "d" + inputFieldList.get(i).substring(1, inputFieldList.get(i).length());
					final String stringField = "s" + inputFieldList.get(i).substring(1, inputFieldList.get(i).length());
					
					final Instant ldt = LocalDateTime.parse((String) map.get(inputFieldList.get(i)),formatter1)
							.atOffset(ZoneOffset.ofTotalSeconds(offset))
							.toInstant();
					map.put(stringField, ldt.toString().replace("T", " ").replace("Z",""));
					map.put(dateField, ldt);
					map.put("noffset" + dateField, offset);
					
				}
			}
		}

		returnObject = objMapper.convertValue(map, inputObject.getClass());
		return returnObject;
	}
	
	public Object convertJSONInputDateToUTCByZone(JSONObject jsonObj, final List<String> inputFieldList,
			final boolean returnAsString, final UserInfo userInfo// , final boolean includeMilliseconds
			) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		if (userInfo.getIsutcenabled()== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
						
			for (int i = 0; i < inputFieldList.size(); i++) {
				
				if ( jsonObj.has(inputFieldList.get(i))  &&
						(String) jsonObj.get(inputFieldList.get(i)) != null
						&& !jsonObj.get(inputFieldList.get(i)).equals("")
						&& !jsonObj.get(inputFieldList.get(i)).equals("-")) {
					if (jsonObj.has("tz" + inputFieldList.get(i))) {
						
						final Object strData = jsonObj.get("tz"+ inputFieldList.get(i)).getClass().getName() == "java.lang.String" ? jsonObj.get("tz"+ inputFieldList.get(i))
								: jsonObj.getJSONObject("tz"+ inputFieldList.get(i)).get("label");

						jsonObj.put("noffset" + inputFieldList.get(i), getCurrentDateTimeOffsetFromDate(
								(String) jsonObj.get(inputFieldList.get(i)), (String) strData));
						

						final Instant ldt = LocalDateTime.parse((String) jsonObj.get(inputFieldList.get(i)),dbPattern)
								.atOffset(ZoneOffset.ofTotalSeconds(jsonObj.getInt("noffset" + inputFieldList.get(i))))
								.toInstant();

						jsonObj.put(inputFieldList.get(i), ldt.toString().replace("T", " ").replace("Z",""));
						
					} 
					else {
						String stringDate = (String) jsonObj.get(inputFieldList.get(i));
						if (!stringDate.equals("")&& !stringDate.equals("-")) {
							stringDate = LocalDateTime.parse(stringDate, dbPattern).format(dbPattern);
							jsonObj.put(inputFieldList.get(i), stringDate);
						}
					}
				} else {
					jsonObj.put(inputFieldList.get(i), "");
				}

			}
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			
			for (int i = 0; i < inputFieldList.size(); i++) {
				String stringDate = jsonObj.has(inputFieldList.get(i)) ? (String) jsonObj.get(inputFieldList.get(i)):"";
				if (!stringDate.equals("")&& !stringDate.equals("-")) {
					stringDate = LocalDateTime.parse(stringDate, dbPattern).format(dbPattern);
					jsonObj.put(inputFieldList.get(i), stringDate);
				}

			}
		}
		return jsonObj;
	}
	
	public String convertUtcToLocalSiteTime(Instant I, UserInfo userInfo) throws ParseException {
		if (userInfo.getIsutcenabled()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			final LocalDateTime LocalDatetime = LocalDateTime.ofInstant(I.truncatedTo(ChronoUnit.SECONDS),
					ZoneId.of(userInfo.getStimezoneid()));
			final DateTimeFormatter destFormat = DateTimeFormatter.ofPattern(userInfo.getSsitedatetime());
			return destFormat.format(LocalDatetime);
		} else {
			final LocalDateTime localDateTime = LocalDateTime.ofInstant(I, ZoneOffset.UTC);
			return DateTimeFormatter.ofPattern(userInfo.getSsitedatetime()).format(localDateTime);
		}
	}
	
}
