package com.agaramtech.qualis.quotation.service.oem;

//import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.quotation.model.OEM;
//import com.agaramtech.qualis.global.CommonFunction;
//import com.agaramtech.qualis.global.Enumeration;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class OEMServiceImpl implements OEMService {

	private final OEMDAO OEMDAO;
	//private final CommonFunction commonFunction;
	
	public OEMServiceImpl(OEMDAO OEMDAO) {
		this.OEMDAO = OEMDAO;
		//this.commonFunction = commonFunction;
	}
		

	@Override
	public ResponseEntity<Object> getOEM(UserInfo userInfo) throws Exception {
		
		return OEMDAO.getOEM(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createOEM(OEM oem, UserInfo userInfo)
			throws Exception {
		return OEMDAO.createOEM(oem, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveOEMById(final int noemcode, final UserInfo userInfo)
			throws Exception {
		return OEMDAO.getActiveOEMById(noemcode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateOEM(OEM quotationoem, UserInfo userInfo)
			throws Exception {
		return OEMDAO.updateOEM(quotationoem, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteOEM(OEM quotationoem, UserInfo userInfo) throws Exception {
		
		return OEMDAO.deleteOEM(quotationoem, userInfo);
	}
	
}
