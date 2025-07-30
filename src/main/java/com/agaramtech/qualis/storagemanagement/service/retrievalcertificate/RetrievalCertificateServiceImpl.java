package com.agaramtech.qualis.storagemanagement.service.retrievalcertificate;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.RetrievalCertificate;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class RetrievalCertificateServiceImpl implements RetrievalCertificateService {

	private final RetrievalCertificateDAO retrievalCertificateDAO;
	private final CommonFunction commonFunction;

	@Override
	public ResponseEntity<Object> getRetrievalCertificate(final Integer ntrainingcode, final String fromDate,
			final String toDate, final UserInfo userInfo, final String currentUIDate) throws Exception {
		return retrievalCertificateDAO.getRetrievalCertificate(ntrainingcode, fromDate, toDate, userInfo,
				currentUIDate);
	}

	@Override
	public ResponseEntity<Object> getProjectUsers(int nprojectmastercode, final UserInfo userInfo) throws Exception {

		return retrievalCertificateDAO.getProjectUsers(nprojectmastercode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getProject(int nprojectTypeCode, final UserInfo userInfo) throws Exception {

		return retrievalCertificateDAO.getProject(nprojectTypeCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSiteAddress(final UserInfo userInfo) throws Exception {

		return retrievalCertificateDAO.getSiteAddress(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createRetrievalCertificate(RetrievalCertificate retrievalcertificate,
			UserInfo userInfo, final String fromDate, final String toDate, final String currentUIDate)
					throws Exception {
		return retrievalCertificateDAO.createRetrievalCertificate(retrievalcertificate, userInfo, fromDate, toDate,
				currentUIDate);
	}

	@Override
	public ResponseEntity<Object> getActiveRetrievalCertificateById(int nretrievalcertificatecode,
			final UserInfo userInfo) throws Exception {
		final RetrievalCertificate objRetrievalCertificate = (RetrievalCertificate) retrievalCertificateDAO
				.getActiveRetrievalCertificateById(nretrievalcertificatecode, userInfo).getBody();
		if (objRetrievalCertificate == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objRetrievalCertificate, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteRetrievalCertificate(RetrievalCertificate retrievalcertificate,
			final String fromDate, final String toDate, UserInfo userInfo) throws Exception {

		return retrievalCertificateDAO.deleteRetrievalCertificate(retrievalcertificate, fromDate, toDate, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateRetrievalCertificate(RetrievalCertificate retrievalcertificate, String fromDate,
			String toDate, UserInfo userInfo) throws Exception {

		return retrievalCertificateDAO.updateRetrievalCertificate(retrievalcertificate, fromDate, toDate, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> completeRetrievalCertificate(RetrievalCertificate retrievalcertificate,
			String fromDate, String toDate, UserInfo userInfo) throws Exception {

		return retrievalCertificateDAO.completeRetrievalCertificate(retrievalcertificate, fromDate, toDate, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> checkRetrievalCertificate(RetrievalCertificate retrievalcertificate, String fromDate,
			String toDate, UserInfo userInfo) throws Exception {

		return retrievalCertificateDAO.checkRetrievalCertificate(retrievalcertificate, fromDate, toDate, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveRetrievalCertificate(RetrievalCertificate retrievalcertificate,
			String fromDate, String toDate, UserInfo userInfo) throws Exception {

		return retrievalCertificateDAO.approveRetrievalCertificate(retrievalcertificate, fromDate, toDate, userInfo);
	}

	@Override
	public ResponseEntity<Object> retrievalreportcertificate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		return retrievalCertificateDAO.retrievalreportcertificate(inputMap, userInfo);
	}
}
