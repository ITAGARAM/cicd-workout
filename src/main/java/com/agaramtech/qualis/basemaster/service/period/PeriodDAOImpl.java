package com.agaramtech.qualis.basemaster.service.period;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.Period;
import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class PeriodDAOImpl  implements PeriodDAO{



	private static final Logger LOGGER = LoggerFactory.getLogger(UnitDAOImpl.class);
	
	private final JdbcTemplate jdbcTemplate;

	/**
	 * This method is used to get Period.
	 * 
	 * @param ncontrolCode
	 * @param 
	 * @return response entity
	 */
	@Override
	public ResponseEntity<Object> getPeriodByControl(final Integer ncontrolCode,final UserInfo userInfo) throws Exception {
	
		final String strQuery = " select pc.ndefaultstatus,p.nperiodcode," + " coalesce(p.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "', p.jsondata->'speriodname'->>'en-US') as speriodname"
				+ " from period p,periodconfig pc where p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and p.nperiodcode=pc.nperiodcode and pc.nformcode=" + userInfo.getNformcode()
				+ " and pc.ncontrolcode=" + ncontrolCode 
				+ " and pc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and nsitecode= "+ userInfo.getNmastersitecode();
		
		LOGGER.info(strQuery);

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Period()), HttpStatus.OK);

	}
}
