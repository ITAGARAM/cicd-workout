package com.agaramtech.qualis.configuration.service.ftpconfig;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.configuration.model.FTPConfig;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This Class is used to perform CRUD operations on ftpconfig and to make
 * default FTP which is configured
 
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class FTPConfigurationServiceImpl implements FTPConfigurationService {


	private final FTPConfigurationDAO objFTPConfigurationDAO;
	private final CommonFunction commonFunction;	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param unitDAO UnitDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public FTPConfigurationServiceImpl(FTPConfigurationDAO objFTPConfigurationDAO, CommonFunction commonFunction) {
		this.objFTPConfigurationDAO = objFTPConfigurationDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to get List of FTP Configured from ftpconfig table
	 * 
	 * @param userInfo [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getFTPConfig(UserInfo userInfo) throws Exception {

		return objFTPConfigurationDAO.getFTPConfig(userInfo);
	}

	/**
	 * This method is used to get a FTP Configured from ftpconfig table using
	 * primary key
	 * 
	 * @param ftpNo    [int] primary key of ftpconfig table
	 * @param userInfo [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have a FTP configured and HTTP Status if
	 *         success. If the FTPCnfig Object is null then return Already deleted
	 *         response
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getActiveFTPConfigById(int ftpNo, UserInfo userInfo) throws Exception {
		final FTPConfig ftpConfig = objFTPConfigurationDAO.getActiveFTPConfigById(ftpNo,userInfo);
		if (ftpConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(ftpConfig, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry in ftpconfig table
	 * 
	 * @param ftpConfig [FTPConfig] object to be inserted
	 * @param userInfo  [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */

	@Override
	@Transactional
	public ResponseEntity<Object> createFTPConfig(FTPConfig ftpConfig, UserInfo userInfo) throws Exception {

		return objFTPConfigurationDAO.createFTPConfig(ftpConfig, userInfo);
	}

	/**
	 * This method is used to update an entry in ftpconfig table
	 * 
	 * @param ftpConfig [FTPConfig] object to be updated
	 * @param userInfo  [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> updateFTPConfig(FTPConfig ftpConfig, UserInfo userInfo) throws Exception {

		return objFTPConfigurationDAO.updateFTPConfig(ftpConfig, userInfo);
	}

	/**
	 * This method is used to delete an entry in ftpconfig table
	 * 
	 * @param ftpConfig [FTPConfig] object to be deleted
	 * @param userInfo  [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteFTPConfig(FTPConfig ftpConfig, UserInfo userInfo) throws Exception {
		return objFTPConfigurationDAO.deleteFTPConfig(ftpConfig, userInfo);
	}

	/**
	 * 
	 * This method is used to set a FTP as default in ftpconfig table
	 * 
	 * @param ftpConfig [FTPConfig] object to be set as default
	 * @param userInfo  [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> setDefaultFTPConfig(FTPConfig ftpConfig, UserInfo userInfo) throws Exception {
		return objFTPConfigurationDAO.setDefaultFTPConfig(ftpConfig, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFTPType(UserInfo userInfo)throws Exception  {
		// TODO Auto-generated method stub
		return objFTPConfigurationDAO.getFTPType( userInfo);
	}

}
