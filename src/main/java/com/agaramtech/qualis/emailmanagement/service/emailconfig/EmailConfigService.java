package com.agaramtech.qualis.emailmanagement.service.emailconfig;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.emailmanagement.model.EmailConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailUserConfig;
import com.agaramtech.qualis.global.UserInfo;

public interface EmailConfigService {

	ResponseEntity<Object> getEmailConfig(Integer nemailconfigcode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEmailConfigDetails(int nformcode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEmailConfigControl(int nformcode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createEmailConfig(EmailConfig emailconfig, String nusercode, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getActiveEmailConfigById(int nemailconfigcode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> updateEmailConfig(EmailConfig emailconfig, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteEmailConfig(EmailConfig emailconfig, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getUserRoleEmail(int nuserrolecode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getUserEmailConfig(int nemailconfigcode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createUsers(Integer nemailconfigcode, String nusercode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteUsers(EmailUserConfig emailuserconfig, UserInfo userInfo) throws Exception;
}
