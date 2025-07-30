package com.agaramtech.qualis.dynamicpreregdesign.service.registrationtypemaster;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class RegistrationTypeMasterServiceImpl implements RegistrationTypeMasterService {

	
	private final RegistrationTypeMasterDAO registrationTypeDAO;
	private final CommonFunction commonFunction;

	public RegistrationTypeMasterServiceImpl(RegistrationTypeMasterDAO registrationTypeDAO, CommonFunction commonFunction) {
		super();
		this.registrationTypeDAO = registrationTypeDAO;
		this.commonFunction = commonFunction;
	}
	
	@Override
	public ResponseEntity<Object> getRegistrationType(final UserInfo userInfo) throws Exception {
		return registrationTypeDAO.getRegistrationType(userInfo);

	}

	@Override
	public ResponseEntity<Object> getSampleType(UserInfo userInfo) throws Exception {
		return registrationTypeDAO.getSampleType(userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationTypeBySampleType(final int sampleTypeCode, UserInfo userInfo)
			throws Exception {
		return registrationTypeDAO.getRegistrationTypeBySampleType(sampleTypeCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationTypeById(int regTypeCode, UserInfo userInfo) throws Exception {
		RegistrationType regType = registrationTypeDAO.getRegistrationTypeById(regTypeCode, userInfo);
		if (regType != null) {
			return new ResponseEntity<>(regType, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createRegistrationType(RegistrationType registrationType, UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		return registrationTypeDAO.createRegistrationType(registrationType, userInfo, inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateRegistrationType(RegistrationType registrationType, UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		return registrationTypeDAO.updateRegistrationType(registrationType, userInfo, inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteRegistrationType(RegistrationType objRegistrationType, UserInfo userInfo)
			throws Exception {
		return registrationTypeDAO.deleteRegistrationType(objRegistrationType, userInfo);
	}

}
