package com.agaramtech.qualis.configuration.service.reportsettings;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.configuration.model.ReportSettings;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'reportsettings' table
 * 
 * @author ATE241
 */
public interface ReportSettingsDAO {

	/**
	 * This interface declaration is used to get the over all reportsettings.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of reportsettings and also
	 *         have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getReportSettings(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active reportsettings object
	 * based on the specified nreportsettingsCode.
	 * 
	 * @param nreportsettingscode [int] primary key of reportsettings object
	 * @return response entity object holding response status and data of
	 *         reportsettings object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveReportSettingsById(final int nreportsettingscode,final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to reportsettings
	 * table.
	 * 
	 * @param objReportSettings [ReportSettings] object holding details to be added
	 *                          in reportsettings table
	 * @return response entity object holding response status and data of added
	 *         reportsettings object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createReportSettings(final ReportSettings objReportSettings,final  UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in reportsettings table.
	 * 
	 * @param objReportSettings [ReportSettings] object holding details to be
	 *                          updated in reportsettings table
	 * @return response entity object holding response status and data of updated
	 *         reportsettings object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateReportSettings(final ReportSettings objReportSettings, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in reportsettings table.
	 * 
	 * @param objReportSettings [ReportSettings] object holding detail to be deleted
	 *                          in reportsettings table
	 * @return response entity object holding response status and data of deleted
	 *         reportsettings object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteReportSettings(final ReportSettings objReportSettings, final UserInfo userInfo)
			throws Exception;

}
