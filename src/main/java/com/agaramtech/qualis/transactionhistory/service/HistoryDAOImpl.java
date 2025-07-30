package com.agaramtech.qualis.transactionhistory.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class HistoryDAOImpl implements HistoryDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HistoryDAOImpl.class);	
	
	private final JdbcTemplate jdbcTemplate;
	
	@Override
	public ResponseEntity<Map<String, Object>> getTestHistory(String ntransactiontestcode, UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		String strQuery = "select rth.ntesthistorycode, rt.ntransactiontestcode, case when ran.sarno='-' then cast(ran.npreregno as character varying) else ran.sarno end as sarno, "
				+ " case when rsan.ssamplearno='-' then cast(rsan.ntransactionsamplecode as character varying) else rsan.ssamplearno end as ssamplearno,"
				+ " rt.jsondata->>'stestsynonym' as stestsynonym, "
				+ " coalesce(qf.jsondata->'sdisplayname'->>'"+ userInfo.getSlanguagetypecode()+ "', qf.jsondata->'sdisplayname'->>'en-US') as sscreenname,"
				+ " concat(u.sfirstname || ' ' || u.slastname) as susername, ur.suserrolename, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()+"', ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, s.ssitename,"
				+ " TO_CHAR(rth.dtransactiondate, '"+ userInfo.getSpgsitedatetime()+"') as stransactiondate "
				+ " from registrationtesthistory rth, registrationarno ran, registrationsamplearno rsan, registrationtest rt, qualisforms qf, users u, userrole ur, transactionstatus ts, site s"
				+ " where rth.ntransactiontestcode in ("+ ntransactiontestcode+ ")"
				+ " and rth.npreregno=ran.npreregno and rth.ntransactionsamplecode=rsan.ntransactionsamplecode and"
				+ " rth.ntransactiontestcode=rt.ntransactiontestcode and rth.nformcode=qf.nformcode and rth.nusercode=u.nusercode"
				+ " and rth.nuserrolecode=ur.nuserrolecode and rth.ntransactionstatus=ts.ntranscode"
				+ " and rth.nsitecode=ran.nsitecode and rth.nsitecode=rsan.nsitecode and rth.nsitecode=rt.nsitecode and rth.nsitecode=s.nsitecode"
				+ " and rth.nsitecode="+ userInfo.getNtranssitecode()
				+ " and rth.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ran.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qf.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ur.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by ntesthistorycode desc ";

		returnMap.put("RegistrationTestHistory", jdbcTemplate.query(strQuery,new RegistrationTestHistory()));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	
//	@Override
//	public ResponseEntity<Object> getSampleHistory(String npreregno, UserInfo userInfo) throws Exception {
//
//		return null;
//	}
//		
//	@Override
//	public ResponseEntity<Object> getSubSampleHistory(String ntransactionsamplecode, UserInfo userInfo) throws Exception {
//
//		return null;
//	}

}
