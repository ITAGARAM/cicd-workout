package com.agaramtech.qualis.eln.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ELNServiceImpl implements ELNService {

	private final ELNDAO elnDAO;

	@Override
	public ResponseEntity<Object> getLimsSDMSRecords(final String nsitecode) throws Exception {
		return elnDAO.getLimsSDMSRecords(nsitecode);
	}

	@Transactional
	@Override
	public String updateLimsSDMSInprogress(final String ntransactionresultcode) throws Exception {
		return elnDAO.updateLimsSDMSInprogress(ntransactionresultcode);
	}

	@Transactional
	@Override
	public String updateLimsSDMSComplete(final Map<String, Object> inputMap) throws Exception {
		return elnDAO.updateLimsSDMSComplete(inputMap);
	}

	@Override
	public ResponseEntity<Object> getLimsTests() throws Exception {
		return elnDAO.getLimsTests();
	}

	@Override
	public ResponseEntity<Object> getLimsTestParameters() throws Exception {
		return elnDAO.getLimsTestParameters();
	}

	@Override
	public ResponseEntity<Object> getLimsInstrument(final int nsitecode) throws Exception {
		return elnDAO.getLimsInstrument(nsitecode);
	}

	public ResponseEntity<Object> getLimsMaterial(final int nsitecode) throws Exception {
		return elnDAO.getLimsMaterial(nsitecode);
	}
}
