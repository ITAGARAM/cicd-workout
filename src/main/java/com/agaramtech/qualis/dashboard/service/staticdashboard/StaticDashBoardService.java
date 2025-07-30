package com.agaramtech.qualis.dashboard.service.staticdashboard;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface StaticDashBoardService {

	public ResponseEntity<Map<String,Object>> getStaticDashBoard(UserInfo userInfo, final int nstaticDashBoardCode,
			final int nstaticDashBoardMasterCode, Map<String, Object> sparamValue) throws Exception;

	public ResponseEntity<Object> getSelectionStaticDashBoard(UserInfo userInfo, final int nstaticDashBoardCode,
			Map<String, Object> sparamValue) throws Exception;

	public ResponseEntity<Object> getListStaticDashBoard(UserInfo userInfo) throws Exception;

}
