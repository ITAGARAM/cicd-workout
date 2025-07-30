package com.agaramtech.qualis.configuration.service.ftpconfig;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.FTPConfig;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This Interface is used to perform CRUD operations on ftpconfig and to make default FTP which is configured
 * @author ATE169
 * @version 9.0.0.1
 * @since 05- AUG -2020
 */
public interface FTPConfigurationService {

	/**
	 * This method is used to get List of FTP Configured from ftpconfig table
	 * @param userInfo [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	public ResponseEntity<Object> getFTPConfig(UserInfo userInfo) throws Exception;
	/**
	 * This method is used to get a FTP Configured from ftpconfig table using primary key
	 * @param ftpNo [int] primary key of ftpconfig table
	 * @param userInfo [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have a FTP configured and HTTP Status
	 * @throws Exception
	 */
	public ResponseEntity<Object> getActiveFTPConfigById(int ftpNo,UserInfo userInfo) throws Exception;
	/**
	 * This method is used to add a new entry in ftpconfig table 
	 * @param ftpConfig [FTPConfig] object to be inserted
	 * @param userInfo [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	public ResponseEntity<Object> createFTPConfig(FTPConfig ftpConfig,UserInfo userInfo) throws Exception;
	
	/**
	 * This method is used to update an  entry in ftpconfig table 
	 * @param ftpConfig [FTPConfig] object to be updated
	 * @param userInfo [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	public ResponseEntity<Object> updateFTPConfig(FTPConfig ftpConfig,UserInfo userInfo) throws Exception;
	
	/**
	 * This method is used to delete an entry in ftpconfig table 
	 * @param ftpConfig [FTPConfig] object to be deleted
	 * @param userInfo [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteFTPConfig(FTPConfig ftpConfig,UserInfo userInfo) throws Exception;
	
	/**
	 * 
	 * This method is used to set a FTP as default in ftpconfig table 
	 * @param ftpConfig [FTPConfig] object to be set as default
	 * @param userInfo [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	public ResponseEntity<Object> setDefaultFTPConfig(FTPConfig ftpConfig,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getFTPType(UserInfo userInfo) throws Exception;
}
