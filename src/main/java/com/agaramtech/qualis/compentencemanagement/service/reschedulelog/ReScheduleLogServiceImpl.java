package com.agaramtech.qualis.compentencemanagement.service.reschedulelog;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class) 
@Service 
@RequiredArgsConstructor 
public class ReScheduleLogServiceImpl implements ReScheduleLogService {
	private final ReScheduleLogDAO reschedulelogDAO;

	public ResponseEntity<Object> getReScheduleLog(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo) throws Exception {
		return reschedulelogDAO.getReScheduleLog(fromDate, toDate, currentUIDate, userInfo);
	}

}
