package com.agaramtech.qualis.basemaster.service.reason;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.Reason;
import com.agaramtech.qualis.global.UserInfo;

public interface ReasonService {

	public ResponseEntity<Object> getReason(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createReason(Reason reason, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateReason(Reason reason, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveReasonById(int nreasoncode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteReason(Reason reason, UserInfo userInfo) throws Exception;

}
