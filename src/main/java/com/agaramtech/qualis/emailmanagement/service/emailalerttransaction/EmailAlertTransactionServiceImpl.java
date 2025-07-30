package com.agaramtech.qualis.emailmanagement.service.emailalerttransaction;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class EmailAlertTransactionServiceImpl implements EmailAlertTransactionService {

	private final EmailAlertTransactionDAO emailalerttransactionDAO;

	@Override
	public ResponseEntity<Object> getEmailAlertTransaction(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo) throws Exception {
		return emailalerttransactionDAO.getEmailAlertTransaction(fromDate, toDate, currentUIDate, userInfo);
	}

}
