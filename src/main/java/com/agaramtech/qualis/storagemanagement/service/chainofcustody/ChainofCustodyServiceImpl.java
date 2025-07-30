package com.agaramtech.qualis.storagemanagement.service.chainofcustody;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ChainofCustodyServiceImpl implements ChainofCustodyService {

	private final ChainofCustodyDAO chainofCutodyDAO;

	@Override
	public ResponseEntity<Object> getChainofCustody(String fromDate, String toDate, final String currentUIDate,
			final UserInfo userInfo) throws Exception {
		return chainofCutodyDAO.getChainofCustody(fromDate, toDate, currentUIDate, userInfo);
	}
}