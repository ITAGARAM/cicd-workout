package com.agaramtech.qualis.emailmanagement.service.emailalerttransaction;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface EmailAlertTransactionService {
	ResponseEntity<Object> getEmailAlertTransaction(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo) throws Exception;
}
