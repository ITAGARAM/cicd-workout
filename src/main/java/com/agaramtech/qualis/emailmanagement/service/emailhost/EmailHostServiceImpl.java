package com.agaramtech.qualis.emailmanagement.service.emailhost;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.emailmanagement.model.EmailHost;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class EmailHostServiceImpl implements EmailHostService {

	private final EmailHostDAO emailhostDAO;
	private final CommonFunction commonFunction;

	@Override
	public ResponseEntity<Object> getEmailHost(int nmasterSiteCode) throws Exception {
		return emailhostDAO.getEmailHost(nmasterSiteCode);
	}

	@Override
	public ResponseEntity<Object> getActiveEmailHostById(final int nemailhostcode, final UserInfo userInfo)
			throws Exception {
		final EmailHost emailhost = (EmailHost) emailhostDAO.getActiveEmailHostById(nemailhostcode);
		if (emailhost == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(emailhost, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createEmailHost(EmailHost emailmanagementEmailHost, UserInfo userInfo)
			throws Exception {
		return emailhostDAO.createEmailHost(emailmanagementEmailHost, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateEmailHost(EmailHost emailmanagementEmailHost, UserInfo userInfo)
			throws Exception {
		return emailhostDAO.updateEmailHost(emailmanagementEmailHost, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteEmailHost(EmailHost emailmanagementEmailHost, final UserInfo userInfo)
			throws Exception {
		return emailhostDAO.deleteEmailHost(emailmanagementEmailHost, userInfo);
	}
}
