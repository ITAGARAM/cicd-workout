package com.agaramtech.qualis.basemaster.service.calendarproperties;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class CalenderPropertiesServiceImpl implements CalenderPropertiesService {

	private final CalenderPropertiesDAO calenderPropertiesDAO;

	public CalenderPropertiesServiceImpl(CalenderPropertiesDAO calenderPropertiesDAO) {
		super();
		this.calenderPropertiesDAO = calenderPropertiesDAO;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available CalenderPropertiess with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of CalenderProperties records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCalenderProperties(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return calenderPropertiesDAO.getCalenderProperties(inputMap, userInfo);
	}

	/**
	 * This method is used to update entry in CalenderProperties table through its
	 * DAO layer.
	 * 
	 * @param CalenderProperties [Manufacturer] object holding details to be updated
	 *                           in CalenderProperties table
	 * @return response entity object holding response status and data of updated
	 *         CalenderProperties object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateCalenderProperties(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return calenderPropertiesDAO.updateCalenderProperties(inputMap, userInfo);
	}

}
