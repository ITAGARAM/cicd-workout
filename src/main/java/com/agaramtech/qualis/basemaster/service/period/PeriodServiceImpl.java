package com.agaramtech.qualis.basemaster.service.period;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class PeriodServiceImpl implements PeriodService{

	
	private final PeriodDAO periodDAO;
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param unitDAO UnitDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public PeriodServiceImpl(PeriodDAO periodDAO) {
		this.periodDAO = periodDAO;
	}
	
	public ResponseEntity<Object> getPeriodByControl(final Integer ncontrolCode, final UserInfo userInfo) throws Exception
	{
		return periodDAO.getPeriodByControl(ncontrolCode, userInfo);
	}
}
