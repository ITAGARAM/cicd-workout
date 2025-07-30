package com.agaramtech.qualis.quotation.service.oem;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.quotation.model.OEM;


public interface OEMDAO {

	public ResponseEntity<Object> getOEM(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createOEM(OEM oem, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getOEM(final int nmasterSiteCode) throws Exception;

	public ResponseEntity<Object> getActiveOEMById(int noemcode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateOEM(OEM quotationoem, UserInfo userInfo)  throws Exception;

	public ResponseEntity<Object> deleteOEM(OEM quotationoem, UserInfo userInfo) throws Exception;
	
}
