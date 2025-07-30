package com.agaramtech.qualis.audittrail.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'audittrail' table
 * through its DAO layer.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 21-04- 2025
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class AuditTrailServiceImpl implements AuditTrailService {

	private final AuditTrailDAO auditTrailDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param AuditTrailDAO auditTrailDAO to hold the request
	 */
	public AuditTrailServiceImpl(AuditTrailDAO auditTrailDAO) {
		super();
		this.auditTrailDAO = auditTrailDAO;
	}

	public ResponseEntity<Object> getAuditTrail(final UserInfo userInfo, final String currentUIDate) throws Exception {
		return auditTrailDAO.getAuditTrail(userInfo, currentUIDate);
	}

	public ResponseEntity<Object> getAuditTrailDetails(final Map<String, Object> inputMap) throws Exception {
		return auditTrailDAO.getAuditTrailDetails(inputMap);
	}

	public ResponseEntity<Object> getAuditTrailByDate(final UserInfo userInfo, final String fromDate,
			final String toDate, final int moduleCode, final int formCode, final int userCode, final int userRole,
			final int viewTypeCode, final String dauditDate) throws Exception {
		return auditTrailDAO.getAuditTrailByDate(userInfo, fromDate, toDate, moduleCode, formCode, userCode, userRole,
				viewTypeCode, dauditDate);
	}

	public ResponseEntity<Object> getFilterAuditTrailRecords(final UserInfo UserInfo, final String FromDate,
			final String ToDate, final int ModuleCode, final int FormCode, final int UserCode, final int UserRole,
			final int ViewTypeCode) throws Exception {
		return auditTrailDAO.getFilterAuditTrailRecords(UserInfo, FromDate, ToDate, ModuleCode, FormCode, UserCode,
				UserRole, ViewTypeCode);
	}

	public ResponseEntity<Object> getFormNameByModule(final int ModuleCode) throws Exception {
		return auditTrailDAO.getFormNameByModule(ModuleCode);
	}

	public ResponseEntity<Object> getexportdata(final Map<String, Object> objmap) throws Exception {
		return auditTrailDAO.getexportdata(objmap);
	}
}