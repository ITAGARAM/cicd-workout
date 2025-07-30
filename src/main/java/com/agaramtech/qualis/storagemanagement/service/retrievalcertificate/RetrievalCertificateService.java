package com.agaramtech.qualis.storagemanagement.service.retrievalcertificate;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.RetrievalCertificate;

public interface RetrievalCertificateService {
	public ResponseEntity<Object> getRetrievalCertificate(final Integer ntrainingcode, final String fromDate,
			final String toDate, final UserInfo userInfo, final String currentUIDate) throws Exception;

	public ResponseEntity<Object> getProjectUsers(int nprojectmastercode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getProject(int nprojectTypeCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSiteAddress(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createRetrievalCertificate(RetrievalCertificate retrievalcertificate,
			UserInfo userInfo, final String fromDate, final String toDate, final String currentUIDate) throws Exception;

	public ResponseEntity<Object> getActiveRetrievalCertificateById(final int nretrievalcertificatecode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateRetrievalCertificate(RetrievalCertificate retrievalcertificate, String fromDate,
			String toDate, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteRetrievalCertificate(RetrievalCertificate retrievalcertificate,
			final String fromDate, final String toDate, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> completeRetrievalCertificate(RetrievalCertificate retrievalcertificate,
			final String fromDate, final String toDate, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> checkRetrievalCertificate(RetrievalCertificate retrievalcertificate,
			final String fromDate, final String toDate, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> approveRetrievalCertificate(RetrievalCertificate retrievalcertificate,
			final String fromDate, final String toDate, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> retrievalreportcertificate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

}
