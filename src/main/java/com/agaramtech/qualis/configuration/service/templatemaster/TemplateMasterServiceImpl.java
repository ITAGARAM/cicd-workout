package com.agaramtech.qualis.configuration.service.templatemaster;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class TemplateMasterServiceImpl implements TemplateMasterService {

	private TemplateMasterDAO templateMasterDAO;

	public TemplateMasterServiceImpl(TemplateMasterDAO templateMasterDAO) {
		this.templateMasterDAO = templateMasterDAO;
	}

	public ResponseEntity<Object> getProductCategoryTemplate(final UserInfo userInfo) throws Exception {
		return templateMasterDAO.getProductCategoryTemplate(userInfo);
	}

	public ResponseEntity<Object> getSampleTypeProductCatrgory(final int nsampletypecode, final UserInfo userInfo)
			throws Exception {
		return templateMasterDAO.getSampleTypeProductCatrgory(nsampletypecode, userInfo);
	}

	public ResponseEntity<Object> getTemplateMasterVersion(final int ntreeversiontempcode, final int ncategorycode,
			final int nformcode, final int nsampletypecode, final UserInfo userInfo) throws Exception {
		return templateMasterDAO.getTemplateMasterVersion(ntreeversiontempcode, ncategorycode, nformcode,
				nsampletypecode, userInfo);
	}

	public ResponseEntity<Object> getTemplateMasterTree(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception {
		return templateMasterDAO.getTemplateMasterTree(ntreeversiontempcode, userInfo);
	}

	public ResponseEntity<Object> getTemplateVersionById(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception {
		return templateMasterDAO.getTemplateVersionById(ntreeversiontempcode, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> createTemplatemaster(Map<String, Object> inputMap) throws Exception {
		inputMap.putAll(templateMasterDAO.fnSequencenumberTemplateMasterUpdate(inputMap));
		return templateMasterDAO.createTemplatemaster(inputMap);
	}

	@Transactional
	public ResponseEntity<Object> updateEditTemplatemasterSubmit(Map<String, Object> inputMap) throws Exception {
		boolean bflag = false;
		final String strStatus = templateMasterDAO.fnCheckLabelName(inputMap);
		if (strStatus.equals("Failure")) {
			bflag = true;
		}
		inputMap.putAll(templateMasterDAO.fnSequencenumberTemplateMasterUpdate(inputMap));
		return templateMasterDAO.updateEditTemplatemasterSubmit(inputMap, bflag);
	}

	@Transactional
	public ResponseEntity<Object> deleteTemplateMaster(final int ntreeversiontempcode, final int nformcode,
			final int ncategorycode, final int nsampletypecode, final UserInfo userInfo, String ntreecontrolcode)
			throws Exception {
		return templateMasterDAO.deleteTemplateMaster(ntreeversiontempcode, nformcode, ncategorycode, nsampletypecode,
				userInfo, ntreecontrolcode);
	}

	@Transactional
	public ResponseEntity<Object> approveTemplateMasterVersion(final int ntreeversiontempcode, final int nformcode,
			final int ncategorycode, final int ntemplatecode, final UserInfo userInfo) throws Exception {
		return templateMasterDAO.approveTemplateMasterVersion(ntreeversiontempcode, nformcode, ncategorycode,
				ntemplatecode, userInfo);
	}

	public ResponseEntity<Object> getSampleTypeComboChange(final int nsampletypecode, final UserInfo userInfo)
			throws Exception {
		return templateMasterDAO.getSampleTypeComboChange(nsampletypecode, userInfo);
	}

}
