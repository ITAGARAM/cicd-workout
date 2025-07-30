package com.agaramtech.qualis.configuration.service.approvalstatusconfig;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.configuration.model.ApprovalStatusConfig;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ApprovalStatusConfigServiceImpl implements ApprovalStatusConfigService {

	private final ApprovalStatusConfigDAO approvalStatusConfigDAO;

	@Override
	public ResponseEntity<Object> getApprovalStatusConfig(final UserInfo userinfo) throws Exception {
		return approvalStatusConfigDAO.getApprovalStatusConfig(userinfo);
	}

	@Override
	public ResponseEntity<Object> closeFilterService(final int nsampletypecode, final int nformcode,
			final int nregsubtypecode, final int nregtypecode, final int napprovalsubtypecode, final UserInfo userinfo)
			throws Exception {
		return approvalStatusConfigDAO.closeFilterService(nsampletypecode, nformcode, nregsubtypecode, nregtypecode,
				napprovalsubtypecode, userinfo);
	}

	@Override
	public ResponseEntity<Object> getTransactionForms(final int nregtypecode, final int nregsubtypecode,
			final UserInfo userinfo) throws Exception {
		return approvalStatusConfigDAO.getTransactionForms(nregtypecode, nregsubtypecode, userinfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteApprovalStatusConfig(final ApprovalStatusConfig objApprovalStatusConfig,
			final int nsampletypecode, final UserInfo userinfo) throws Exception {
		return approvalStatusConfigDAO.deleteApprovalStatusConfig(objApprovalStatusConfig, nsampletypecode, userinfo);
	}

	@Override
	public ResponseEntity<Object> getRegTypeBySampleType(final int nsampletypecode, final UserInfo userinfo)
			throws Exception {
		return approvalStatusConfigDAO.getRegTypeBySampleType(nsampletypecode, userinfo);
	}

	@Override
	public ResponseEntity<Object> getRegSubTypeByRegtype(final int nregtypecode, final UserInfo userinfo)
			throws Exception {
		return approvalStatusConfigDAO.getRegSubTypeByRegtype(nregtypecode, userinfo);
	}

	@Override
	public ResponseEntity<Object> getFilterSubmit(final int nsampletypecode, final int napprovalsubtypecode,
			final int nregtypecode, final int nregsubtypecode, final int nformCode, final UserInfo userinfo)
			throws Exception {
		return approvalStatusConfigDAO.getFilterSubmit(nsampletypecode, napprovalsubtypecode, nregtypecode,
				nregsubtypecode, nformCode, userinfo);
	}

	@Override
	public ResponseEntity<Object> getStatusFunction(final int nformcode, final UserInfo userinfo) throws Exception {
		return approvalStatusConfigDAO.getStatusFunction(nformcode, userinfo);
	}

	@Override
	public ResponseEntity<Object> getTransactionStatus(final UserInfo userinfo) throws Exception {
		return approvalStatusConfigDAO.getTransactionStatus(userinfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createApprovalStatusConfig(final List<ApprovalStatusConfig> inputMap,
			final UserInfo userinfo) throws Exception {
		return approvalStatusConfigDAO.createApprovalStatusConfig(inputMap, userinfo);
	}

}