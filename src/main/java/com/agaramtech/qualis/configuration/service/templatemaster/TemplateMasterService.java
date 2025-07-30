package com.agaramtech.qualis.configuration.service.templatemaster;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface TemplateMasterService {

	public ResponseEntity<Object> getProductCategoryTemplate(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSampleTypeProductCatrgory(final int nsampletypecode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTemplateMasterVersion(final int ntreeversiontempcode, final int ncategorycode,
			final int nformcode, final int nsampletypecode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTemplateMasterTree(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTemplateVersionById(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createTemplatemaster(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateEditTemplatemasterSubmit(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> deleteTemplateMaster(final int ntreeversiontempcode, final int nformcode,
			final int ncategorycode, final int nsampletypecode, final UserInfo userInfo, String ntreecontrolcode)
			throws Exception;

	public ResponseEntity<Object> approveTemplateMasterVersion(final int ntreeversiontempcode, final int nformcode,
			final int ncategorycode, final int ntemplatecode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSampleTypeComboChange(final int nsampletypecode, final UserInfo userInfo)
			throws Exception;

}
