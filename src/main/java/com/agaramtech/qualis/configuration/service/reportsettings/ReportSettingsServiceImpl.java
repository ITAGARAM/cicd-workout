package com.agaramtech.qualis.configuration.service.reportsettings;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.configuration.model.ReportSettings;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'reportsettings' table
 * through its DAO layer.
 * 
 * @author ATE241
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ReportSettingsServiceImpl implements ReportSettingsService {
	
	private final ReportSettingsDAO reportSettingsDAO;

	public ReportSettingsServiceImpl(ReportSettingsDAO reportSettingsDAO) {
		super();
		this.reportSettingsDAO = reportSettingsDAO;
	}

	/**
	 * This method is used to retrieve list of all active reportsettings.
	 * 
	 * @return response entity object holding response status and list of all active
	 *         reportsettings
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getReportSettings(final UserInfo userInfo) throws Exception {
		return reportSettingsDAO.getReportSettings(userInfo);
	}

	/**
	 * This method is used to retrieve active reportsetting object based on the
	 * specified nreportsettingCode.
	 * 
	 * @param nreportsettingCode [int] primary key of reportsettings object
	 * @return response entity object holding response status and data of
	 *         reportsettings object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveReportSettingsById(final int nreportsettingCode, final UserInfo userInfo)
			throws Exception {
		return reportSettingsDAO.getActiveReportSettingsById(nreportsettingCode, userInfo);
	}

	/**
	 * This method is used to add a new entry to reportsettings table. On successive
	 * insert get the new inserted record.
	 * 
	 * @param objReportSettings [ReportSettings] object holding details to be added
	 *                          in reportsettings table
	 * @return inserted reportsettings object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createReportSettings(final ReportSettings objReportSettings, final UserInfo userInfo)
			throws Exception {
		return reportSettingsDAO.createReportSettings(objReportSettings, userInfo);
	}

	/**
	 * This method is used to update entry in reportsettings table.
	 * 
	 * @param objReportSettings [ReportSettings] object holding details to be
	 *                          updated in reportsettings table
	 * @return response entity object holding response status and data of updated
	 *         reportsettings object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateReportSettings(final ReportSettings objReportSettings, final UserInfo userInfo)
			throws Exception {
		return reportSettingsDAO.updateReportSettings(objReportSettings, userInfo);
	}

	/**
	 * This method id used to delete an entry in reportsettings table
	 * 
	 * @param objReportSettings [ReportSettings] an Object holds the record to be
	 *                          deleted
	 * @return a response entity with corresponding HTTP status and a reportsettings
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteReportSettings(final ReportSettings objReportSettings, final UserInfo userInfo)
			throws Exception {
		return reportSettingsDAO.deleteReportSettings(objReportSettings, userInfo);
	}

}
