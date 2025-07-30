package com.agaramtech.qualis.configuration.service.adssettings;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.ADSSettings;
import com.agaramtech.qualis.global.UserInfo;

public interface ADSSettingsDAO {

	public ResponseEntity<Object> getADSSettings(UserInfo userInfo) throws Exception;

	public ADSSettings getActiveADSSettingsById(int adsSettingsCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createADSSettings(ADSSettings adsSettings, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateADSSettings(ADSSettings adsSettings, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteADSSettings(ADSSettings adsSettings, UserInfo userInfo) throws Exception;

}
