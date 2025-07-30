package com.agaramtech.qualis.configuration.service.approvalstatusconfig;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.configuration.model.ApprovalStatusConfig;
import com.agaramtech.qualis.global.UserInfo;

public interface ApprovalStatusConfigDAO {

	public ResponseEntity<Object> getApprovalStatusConfig(final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> getTransactionForms(final int nregtypecode, final int nregsubtypecode,
			final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> getStatusFunction(final int nformcode, final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> getTransactionStatus(final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> getRegTypeBySampleType(final int nsampletypecode, final UserInfo userinfo)
			throws Exception;

	public ResponseEntity<Object> deleteApprovalStatusConfig(final ApprovalStatusConfig objApprovalStatus,
			final int nsampletypecode, final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> createApprovalStatusConfig(final List<ApprovalStatusConfig> inputMap,
			final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> closeFilterService(final int nsampletypecode, final int nformcode,
			final int nregsubtypecode, final int nregtypecode, final int napprovalsubtypecode, final UserInfo userinfo)
			throws Exception;

	public ResponseEntity<Object> getRegSubTypeByRegtype(final int nregtypecode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFilterSubmit(final int nsampletypecode, final int napprovalsubtypecode,
			final int nregtypecode, final int nregsubtypecode, final int nformCode, final UserInfo userinfo)
			throws Exception;

}
