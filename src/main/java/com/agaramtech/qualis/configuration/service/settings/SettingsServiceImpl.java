package com.agaramtech.qualis.configuration.service.settings;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.UserInfo;
/**
 * This class holds methods to perform CRUD operation on 'settings' table
 * through its DAO layer.
 * 
 * @author ATE241
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SettingsServiceImpl implements SettingsService {

	private final SettingsDAO settingsDAO;	
	
	public SettingsServiceImpl(SettingsDAO settingsDAO) {
		super();
		this.settingsDAO = settingsDAO;
	}

	/**
	 * This method is used to retrieve list of all active settings.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         settings
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getSettings(UserInfo userInfo) throws Exception {
		return settingsDAO.getSettings(userInfo);
	}

	/**
	 * This method is used to retrieve active setting object based on the specified
	 * nsettingCode.
	 * 
	 * @param nsettingCode [int] primary key of settings object
	 * @return response entity object holding response status and data of settings
	 *         object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSettingsById(int nsettingCode, UserInfo userInfo) throws Exception {
		return settingsDAO.getActiveSettingsById(nsettingCode, userInfo);
	}

	/**
	 * This method is used to add a new entry to settings table. On successive
	 * insert get the new inserted record.
	 * 
	 * @param objSettings [Settings] object holding details to be added in settings
	 *                    table
	 * @return inserted settings object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSettings(Settings objSettings, UserInfo userInfo) throws Exception {
		return settingsDAO.createSettings(objSettings, userInfo);
	}

	/**
	 * This method is used to update entry in settings table.
	 * 
	 * @param objSettings [Settings] object holding details to be updated in
	 *                    settings table
	 * @return response entity object holding response status and data of updated
	 *         settings object
	 * @throws Exception that are thrown from this Service layer
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> updateSettings(Settings objSettings, UserInfo userInfo) throws Exception {
		return settingsDAO.updateSettings(objSettings, userInfo);
	}

	/**
	 * This method id used to delete an entry in Settings table
	 * 
	 * @param objSettings [Settings] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and a settings
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> deleteSettings(Settings objSettings, UserInfo userInfo) throws Exception {
		return settingsDAO.deleteSettings(objSettings, userInfo);
	}
}
