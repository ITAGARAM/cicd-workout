package com.agaramtech.qualis.emailmanagement.service.emailconfig;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.emailmanagement.model.EmailConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailUserConfig;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class) 
@Service 
@RequiredArgsConstructor 
public class EmailConfigServiceImpl implements EmailConfigService {
	
	private final EmailConfigDAO emailconfigDAO;

	@Override
	public ResponseEntity<Object> getEmailConfig(Integer nemailconfigcode, UserInfo userInfo) throws Exception {
		return emailconfigDAO.getEmailConfig(nemailconfigcode, userInfo);
	}

	public ResponseEntity<Object> getEmailConfigDetails(int nformcode, UserInfo userInfo) throws Exception {
		return emailconfigDAO.getEmailConfigDetails(nformcode, userInfo);
	}

	public ResponseEntity<Object> getEmailConfigControl(int nformcode, UserInfo userInfo) throws Exception {
		return emailconfigDAO.getEmailConfigControl(nformcode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createEmailConfig(EmailConfig emailconfig, String nusercode, UserInfo userInfo)
			throws Exception {
		return emailconfigDAO.createEmailConfig(emailconfig, nusercode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveEmailConfigById(int nemailconfigcode, UserInfo userInfo) throws Exception {
		return emailconfigDAO.getActiveEmailConfigById(nemailconfigcode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateEmailConfig(EmailConfig emailconfig, UserInfo userInfo) throws Exception {
		return emailconfigDAO.updateEmailConfig(emailconfig, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteEmailConfig(EmailConfig emailconfig, UserInfo userInfo) throws Exception {
		return emailconfigDAO.deleteEmailConfig(emailconfig, userInfo);
	}

	@Override
	public ResponseEntity<Object> getUserRoleEmail(int nuserrolecode, UserInfo userInfo) throws Exception {
		return emailconfigDAO.getUserRoleEmail(nuserrolecode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getUserEmailConfig(int nemailconfigcode, UserInfo userInfo) throws Exception {
		return emailconfigDAO.getUserEmailConfig(nemailconfigcode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createUsers(Integer nemailconfigcode, String nusercode, UserInfo userInfo)
			throws Exception {
		return emailconfigDAO.createUsers(nemailconfigcode, nusercode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteUsers(EmailUserConfig emailuserconfig, UserInfo userInfo) throws Exception {
		return emailconfigDAO.deleteUsers(emailuserconfig, userInfo);
	}
}
