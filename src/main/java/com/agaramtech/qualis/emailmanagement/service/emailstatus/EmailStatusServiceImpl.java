package com.agaramtech.qualis.emailmanagement.service.emailstatus;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class EmailStatusServiceImpl implements EmailStatusService {

	private final EmailStatusDAO emailstatusDAO;

	public EmailStatusServiceImpl(EmailStatusDAO emailstatusDAO) {
		super();
		this.emailstatusDAO = emailstatusDAO;
	}

	@Override
	public ResponseEntity<Object> getEmailStatus(final UserInfo userInfo) throws Exception {
		return emailstatusDAO.getEmailStatus(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> reSentMail(Integer nrunningno, Integer ncontrolcode, UserInfo userInfo)
			throws Exception {
		return emailstatusDAO.reSentMail(nrunningno, ncontrolcode, userInfo);
	}
}
