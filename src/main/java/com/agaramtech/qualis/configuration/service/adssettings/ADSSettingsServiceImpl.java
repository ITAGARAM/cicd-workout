package com.agaramtech.qualis.configuration.service.adssettings;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.configuration.model.ADSSettings;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ADSSettingsServiceImpl implements ADSSettingsService {

	private final ADSSettingsDAO adsSettingsDAO;
	private final CommonFunction commonFunction;

	public ADSSettingsServiceImpl(ADSSettingsDAO adsSettingsDAO, CommonFunction commonFunction) {
		this.adsSettingsDAO = adsSettingsDAO;
		this.commonFunction = commonFunction;
	}

	@Override
	public ResponseEntity<Object> getADSSettings(UserInfo userInfo) throws Exception {
		return adsSettingsDAO.getADSSettings(userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveADSSettingsById(int adsSettingsCode, UserInfo userInfo) throws Exception {
		final ADSSettings adsSettings = adsSettingsDAO.getActiveADSSettingsById(adsSettingsCode, userInfo);
		if (adsSettings == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(adsSettings, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createADSSettings(ADSSettings adsSettings, UserInfo userInfo) throws Exception {
		return adsSettingsDAO.createADSSettings(adsSettings, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateADSSettings(ADSSettings adsSettings, UserInfo userInfo) throws Exception {
		return adsSettingsDAO.updateADSSettings(adsSettings, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteADSSettings(ADSSettings adsSettings, UserInfo userInfo) throws Exception {
		return adsSettingsDAO.deleteADSSettings(adsSettings, userInfo);
	}
}
