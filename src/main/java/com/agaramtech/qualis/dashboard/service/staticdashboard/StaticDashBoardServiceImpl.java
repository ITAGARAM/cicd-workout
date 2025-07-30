package com.agaramtech.qualis.dashboard.service.staticdashboard;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class StaticDashBoardServiceImpl implements StaticDashBoardService {

	private final StaticDashBoardDAO staticDashBoardDAO;

	public StaticDashBoardServiceImpl(StaticDashBoardDAO staticDashBoardDAO) {
		super();
		this.staticDashBoardDAO = staticDashBoardDAO;
	}

	@Override
	public ResponseEntity<Map<String,Object>> getStaticDashBoard(UserInfo userInfo, int nstaticDashBoardCode,
			int nstaticDashBoardMasterCode, Map<String, Object> sparamValue) throws Exception {
		return staticDashBoardDAO.getStaticDashBoard(userInfo, nstaticDashBoardCode, nstaticDashBoardMasterCode,
				sparamValue);
	}

	@Override
	public ResponseEntity<Object> getSelectionStaticDashBoard(UserInfo userInfo, int nstaticDashBoardCode,
			Map<String, Object> sparamValue) throws Exception {
		return staticDashBoardDAO.getSelectionStaticDashBoard(userInfo, nstaticDashBoardCode, sparamValue);
	}

	@Override
	public ResponseEntity<Object> getListStaticDashBoard(UserInfo userInfo) throws Exception {
		return staticDashBoardDAO.getListStaticDashBoard(userInfo);
	}
}
