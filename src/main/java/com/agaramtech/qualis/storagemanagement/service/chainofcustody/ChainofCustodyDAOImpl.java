package com.agaramtech.qualis.storagemanagement.service.chainofcustody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.ChainCustody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ChainofCustodyDAOImpl implements ChainofCustodyDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChainofCustodyDAOImpl.class);

	private final DateTimeUtilityFunction dateUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;

	@Override
	public ResponseEntity<Object> getChainofCustody(String fromDate, String toDate, final String currentUIDate,
			final UserInfo userInfo) throws Exception {
		LOGGER.info("getChainofCustody()");
		final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			outputMap.put("FromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			outputMap.put("FromDate", fromDateUI);
			outputMap.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}

		final String strQuery = "select c.sitemno,c.sremarks,c.noffsetdtransactiondate,to_char(c.dtransactiondate,'"
				+ userInfo.getSdatetimeformat() + "') as stransactiondate, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, "
				+ "coalesce(qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',qf.jsondata->'sdisplayname'->>'en-US') as sformname, "
				+ "(u.sfirstname||' '||u.slastname) as username  " + "from  "
				+ "chaincustody c join transactionstatus ts on c.ntransactionstatus = ts.ntranscode  "
				+ "join users u on c.nusercode = u.nusercode " + "join qualisforms qf on  c.nformcode = qf.nformcode "
				+ "and  c.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nsitecode="
				+ userInfo.getNtranssitecode() + " and  dtransactiondate between '" + fromDate + "' and '"
				+ toDate + "' order by c.nchaincustodycode";

		final List<ChainCustody> lstChainofCustody = jdbcTemplate.query(strQuery, new ChainCustody());
		final List<ChainCustody> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(lstChainofCustody, Arrays.asList("stransactiondate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<ChainCustody>>() {
				});
		outputMap.put("ChainofCustody", lstUTCConvertedDate);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
}