package com.agaramtech.qualis.configuration.service.settings;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'settings' table
 * 
 * @author ATE241
 */
public interface SettingsService {
	/**
	 * This interface declaration is used to get the over all settings
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of settings and also have the
	 *         HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSettings(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active setting object based on
	 * the specified nsettingCode.
	 * 
	 * @param nsettingCode [int] primary key of settings object
	 * @return response entity object holding response status and data of settings
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveSettingsById(final int nsettingCode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to settings table.
	 * 
	 * @param objSettings [Settings] object holding details to be added in settings
	 *                    table
	 * @return response entity object holding response status and data of added
	 *         settings object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSettings(Settings objSettings, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in settings table.
	 * 
	 * @param objSettings [Settings] object holding details to be updated in
	 *                    settings table
	 * @return response entity object holding response status and data of updated
	 *         settings object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSettings(Settings objSettings, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in settings table.
	 * 
	 * @param objSettings [Settings] object holding detail to be deleted in settings
	 *                    table
	 * @return response entity object holding response status and data of deleted
	 *         settings object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSettings(Settings objSettings, UserInfo userInfo) throws Exception;

}
