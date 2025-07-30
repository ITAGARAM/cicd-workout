package com.agaramtech.qualis.audittrail.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds methods that is used to perform CRUD Operation on
 * "AuditTrail" table by implementing methods from its interface.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 15-04- 2025
 */
public interface AuditTrailDAO {

	public ResponseEntity<Object> getAuditTrail(final UserInfo userInfo, final String currentUIDate) throws Exception;

	public ResponseEntity<Object> getAuditTrailDetails(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getAuditTrailByDate(final UserInfo userInfo, final String fromDate,
			final String toDate, final int moduleCode, final int formCode, final int userCode, final int userRole,
			final int viewTypeCode, final String dauditDate) throws Exception;

	public ResponseEntity<Object> getFilterAuditTrailRecords(final UserInfo UserInfo, final String FromDate,
			final String ToDate, final int ModuleCode, final int FormCode, final int UserCode, final int UserRole,
			final int ViewTypeCode) throws Exception;

	public ResponseEntity<Object> getFormNameByModule(final int ModuleCode) throws Exception;

	public ResponseEntity<Object> getexportdata(final Map<String, Object> objmap) throws Exception;

}