package com.agaramtech.qualis.basemaster.service.reason;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.basemaster.model.Reason;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ReasonServiceImpl implements ReasonService {

	private final ReasonDAO reasonDAO;
	private final CommonFunction commonFunction;

	public ReasonServiceImpl(ReasonDAO reasonDAO, CommonFunction commonFunction) {
		super();
		this.reasonDAO = reasonDAO;
		this.commonFunction = commonFunction;
	}

	@Override
	public ResponseEntity<Object> getReason(UserInfo userInfo) throws Exception {
		return reasonDAO.getReason(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createReason(Reason reason, UserInfo userInfo) throws Exception {
		return reasonDAO.createReason(reason, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateReason(Reason reason, UserInfo userInfo) throws Exception {
		return reasonDAO.updateReason(reason, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveReasonById(int nreasoncode, UserInfo userInfo) throws Exception {
		final Reason reason = (Reason) reasonDAO.getActiveReasonById(nreasoncode, userInfo);
		if (reason == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(reason, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteReason(Reason reason, UserInfo userInfo) throws Exception {
		return reasonDAO.deleteReason(reason, userInfo);
	}
}
