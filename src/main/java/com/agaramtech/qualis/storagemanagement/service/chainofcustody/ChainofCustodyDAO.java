package com.agaramtech.qualis.storagemanagement.service.chainofcustody;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface ChainofCustodyDAO {

	public ResponseEntity<Object> getChainofCustody(String fromDate, String toDate, final String currentUIDate,
			final UserInfo userInfo) throws Exception;

}