package com.agaramtech.qualis.emailmanagement.service.emailstatus;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface EmailStatusService {

	ResponseEntity<Object> getEmailStatus(UserInfo userInfo) throws Exception;

	ResponseEntity<Object> reSentMail(Integer nrunningno, Integer ncontrolcode, UserInfo userInfo) throws Exception;

}
