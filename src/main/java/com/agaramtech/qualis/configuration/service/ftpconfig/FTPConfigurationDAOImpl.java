package com.agaramtech.qualis.configuration.service.ftpconfig;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.FTPConfig;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.PasswordUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class FTPConfigurationDAOImpl implements FTPConfigurationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(FTPConfigurationDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	//private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	//private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final PasswordUtilityFunction passwordUtilityFunction;

	/**
	 * This method is used to get List of FTP Configured from ftpconfig table
	 * 
	 * @param userInfo [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getFTPConfig(UserInfo userInfo) throws Exception {
	
		final String yes = commonFunction.getMultilingualMessage("IDS_YES", userInfo.getSlanguagefilename());
		final String no = commonFunction.getMultilingualMessage("IDS_NO", userInfo.getSlanguagefilename());
	
		final String strQuery = "select fc.nftpno,fc.susername, fc.shost,"
								+ " fc.nportno,fc.nstatus,"
								+ " replace(fc.sphysicalpath collate \"default\",'\\','\') as sphysicalpath,fc.nsitecode,fc.nsslrequired,fc.nchecksumrequired,fc.ndefaultstatus,"
								+ " case when fc.nsslrequired=3 then '" + yes + "' else '" + no + "' end as ssslrequired,"
								+ " case when fc.nchecksumrequired=3 then '" + yes + "' else '" + no + "' end as schecksumrequired,"
								+ " case when fc.ndefaultstatus=3 then '" + yes + "' else '" + no + "' end as sdefaultstatus,fc.nftptypecode,ft.jsondata->'sftptypename'->>'"+userInfo.getSlanguagetypecode()+"' as sftptypename"
								+ " from ftpconfig fc,transactionstatus ts,"
								+ " ftptype ft  where fc.ndefaultstatus=ts.ntranscode"
								+ " and ft.nftptypecode= fc.nftptypecode "
								+ " and fc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and fc.nsitecode="+ userInfo.getNmastersitecode();
		return new ResponseEntity<>((List<FTPConfig>) jdbcTemplate.query(strQuery, new FTPConfig()), HttpStatus.OK);
	}

	/**
	 * This method is used to get a FTP Configured from ftpconfig table using
	 * primary key
	 * 
	 * @param ftpNo [int] primary key of ftpconfig table
	 * @return ResponseEntity which have a FTP configured and HTTP Status
	 * @throws Exception
	 */
	@Override
	public FTPConfig getActiveFTPConfigById(int ftpNo,UserInfo userInfo) throws Exception {
		
		final String strQuery = "select fc.nftpno,fc.susername, fc.shost,"
								+ " fc.nportno,fc.nstatus, "
								+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdefaultstatus,"//fc.nregionsitecode,"
								+ " fc.sphysicalpath,fc.nsslrequired,fc.nchecksumrequired,fc.nsitecode,fc.ndefaultstatus,ft.nftptypecode,ft.jsondata->'sftptypename'->>'"+userInfo.getSlanguagetypecode()+"' sftptypename"
								+ " from ftpconfig fc,transactionstatus ts,"//site s,"
								+ " ftptype ft where fc.ndefaultstatus=ts.ntranscode"
								+ " and ft.nftptypecode=fc.nftptypecode "
								+ " and fc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and fc.nftpno=" + ftpNo + ";";
		final FTPConfig ftpConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(strQuery, FTPConfig.class, jdbcTemplate);
		
		if(ftpConfig != null){
			
			final String physicalPath = ftpConfig.getSphysicalpath();
			
			String modifiedPhysicalPath = physicalPath.replace("\\\\", "\\");
			modifiedPhysicalPath = modifiedPhysicalPath.substring(0, modifiedPhysicalPath.length() - 1);
			ftpConfig.setSphysicalpath(modifiedPhysicalPath);
			
			final String plainPassword = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno", ftpConfig.getNftpno(), "spassword");
			ftpConfig.setSpassword(plainPassword);
		}
		return ftpConfig;
	}

	/**
	 * This method is used to get the FTP by host name and port name with respect to
	 * site
	 * 
	 * @param ftpConfig [FTPConfig] object to be fetched
	 * @return a FTPConfig object which is fetched
	 * @throws Exception
	 */
	private FTPConfig getFTPByHost(FTPConfig ftpConfig) throws Exception {
		final String strQuery = "select nftpno from ftpconfig where shost = N'" 
							+ stringUtilityFunction.replaceQuote(ftpConfig.getShost()) + "' "
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ ftpConfig.getNsitecode() + " and nportno=" + ftpConfig.getNportno() + ";";
		return 	(FTPConfig) jdbcUtilityFunction.queryForObject(strQuery, FTPConfig.class, jdbcTemplate);
		
	}

	/**
	 * This method is used to get the FTP by default status with respect to site
	 * 
	 * @param ftpConfig [FTPConfig] object to be fetched
	 * @return a FTPConfig object which is fetched
	 * @throws Exception
	 */
	private FTPConfig getFTPConfigByStatus(int nmasterSiteCode) throws Exception {
		final String strQuery = "Select * from FTPconfig where nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode=" + nmasterSiteCode
								+ " and ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus();
		return (FTPConfig) jdbcUtilityFunction.queryForObject(strQuery, FTPConfig.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry in ftpconfig table Need to test the
	 * FTP connection Before insert the record Need to check Duplicate Record with
	 * respect to host name and port No. with respect to site Need to set it as
	 * Default if none of the FTP is default before with respect to site Need to set
	 * other record as not default if it is a Default FTP
	 * 
	 * @param ftpConfig [FTPConfig] object to be inserted
	 * @param userInfo  [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> createFTPConfig(FTPConfig ftpConfig, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table ftpconfig "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
	    jdbcTemplate.execute(sQuery);
	   
		FTPConfig lstFTPByHost = getFTPByHost(ftpConfig);
		boolean pingTest = false;
		
		if (lstFTPByHost == null) {
			pingTest = testFTPConnection(ftpConfig, false);
			if (pingTest) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedFTPList = new ArrayList<>();
				
				if 
				(ftpConfig.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final FTPConfig defaultFTPConfig = getFTPConfigByStatus(ftpConfig.getNsitecode());
					if (defaultFTPConfig != null && defaultFTPConfig.getNftpno() != ftpConfig.getNftpno()) {

						final List<Object> oldDefaultFTP = new ArrayList<>();
						final List<Object> modifiedDefaultFTP = new ArrayList<>();
						final FTPConfig newFTP = SerializationUtils.clone(defaultFTPConfig);

						defaultFTPConfig
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						
						final String updateQueryString = "update ftpconfig set ndefaultstatus = "
														+ Enumeration.TransactionStatus.NO.gettransactionstatus()+", dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nftpno="
														+ defaultFTPConfig.getNftpno();
						jdbcTemplate.execute(updateQueryString);
						
						modifiedDefaultFTP.add(defaultFTPConfig);
						oldDefaultFTP.add(newFTP);
						
						multilingualIDList.add("IDS_EDITFTPCONFIG");
						auditUtilityFunction.fnInsertAuditAction(modifiedDefaultFTP, 2, oldDefaultFTP, multilingualIDList, userInfo);
						
						multilingualIDList.clear();
					}

				} else {
					final FTPConfig defaultFTPConfig = getFTPConfigByStatus(ftpConfig.getNsitecode());
					if (defaultFTPConfig == null) {
						ftpConfig.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
					}
				}

				String physicalPath = ftpConfig.getSphysicalpath();
				String check = physicalPath.substring(physicalPath.length() - 1, physicalPath.length());
				if (check.equals("\\")) {
					physicalPath = physicalPath.substring(0, physicalPath.length() - 1);
				}

				String modifiedPhysicalPath = physicalPath != null && physicalPath.length() > 0
						? physicalPath.replace(File.separator, "\\\\")
						: "";
				modifiedPhysicalPath = modifiedPhysicalPath.concat("\\\\");
				ftpConfig.setSphysicalpath(modifiedPhysicalPath);
				
				String sequencequery ="select nsequenceno from SeqNoConfigurationMaster where stablename ='ftpconfig'";
				int nsequenceno =jdbcTemplate.queryForObject(sequencequery, Integer.class);
				nsequenceno++;

				final String insertquery = " insert into ftpconfig (nftpno,nftptypecode,"//nregionsitecode,"
						   + " shost,spassword,susername,nportno,sphysicalpath,nsslrequired,nchecksumrequired,ndefaultstatus,nsitecode,nstatus,dmodifieddate) "
						   + " values("+nsequenceno+","+ftpConfig.getNftptypecode()
						   +",N'"+stringUtilityFunction.replaceQuote(ftpConfig.getShost())+"',N'"+ftpConfig.getSpassword()+"',N'"+ stringUtilityFunction.replaceQuote(ftpConfig.getSusername())+"',"+ftpConfig.getNportno()+","
						   + " N'"+stringUtilityFunction.replaceQuote(ftpConfig.getSphysicalpath())+"',"+ftpConfig.getNsslrequired()+","+ftpConfig.getNchecksumrequired()+","+ftpConfig.getNdefaultstatus()+","
						   + " "+userInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"')";
			
				jdbcTemplate.execute(insertquery);
				
				final String updatequery ="update SeqNoConfigurationMaster set nsequenceno="+nsequenceno+" where stablename ='ftpconfig'";
				jdbcTemplate.execute(updatequery);
				
				ftpConfig.setNftpno(nsequenceno);
				passwordUtilityFunction.encryptPassword("ftpconfig", "nftpno", ftpConfig.getNftpno(), ftpConfig.getSpassword(), "spassword");
				
				ftpConfig.setNftpno(nsequenceno);
				savedFTPList.add(ftpConfig);
				multilingualIDList.add("IDS_ADDFTPCONFIG");
				
				auditUtilityFunction.fnInsertAuditAction(savedFTPList, 1, null, multilingualIDList, userInfo);
				
				return getFTPConfig(userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_WRONGCREDENTIALS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update an entry in ftpconfig table Need to test the
	 * FTP connection Before update the record Need to check Duplicate Record with
	 * respect to host name and port No. with respect to site Need to set it as
	 * Default if none of the FTP is default before with respect to site Need to set
	 * other record as not default if it is a Default FTP
	 * 
	 * @param ftpConfig [FTPConfig] object to be updated
	 * @param userInfo  [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> updateFTPConfig(FTPConfig ftpConfig, UserInfo userInfo) throws Exception {

		final FTPConfig objFTPConfig = getActiveFTPConfigById(ftpConfig.getNftpno(),userInfo);

		if (objFTPConfig == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final FTPConfig objFTP = getFTPByHost(ftpConfig);

			if (objFTP == null || objFTP.getNftpno() == ftpConfig.getNftpno()) {
				boolean pingTest = testFTPConnection(ftpConfig, false);
				if (pingTest) {
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> savedFTPList = new ArrayList<>();
					final List<Object> editedFTPList = new ArrayList<>();
					if (ftpConfig.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						final FTPConfig defaultFTPConfig = getFTPConfigByStatus(ftpConfig.getNsitecode());
						if (defaultFTPConfig != null && defaultFTPConfig.getNftpno() != ftpConfig.getNftpno()) {
							final FTPConfig newFTP = SerializationUtils.clone(defaultFTPConfig);

							defaultFTPConfig
									.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							final String updateQueryString = " update ftpconfig set ndefaultstatus="
									+ Enumeration.TransactionStatus.NO.gettransactionstatus()+", dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nftpno="
									+ defaultFTPConfig.getNftpno();
							jdbcTemplate.execute(updateQueryString);
							savedFTPList.add(defaultFTPConfig);
							editedFTPList.add(newFTP);
						}
					} else {
						final FTPConfig defaultFTPConfig = getFTPConfigByStatus(ftpConfig.getNsitecode());
						if (defaultFTPConfig != null && defaultFTPConfig.getNftpno() == ftpConfig.getNftpno()) {
							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_ATLEASTONEFTPMUSTBEDEFAULT",
											userInfo.getSlanguagefilename()),
									HttpStatus.EXPECTATION_FAILED);
						}
					}

					String physicalPath = ftpConfig.getSphysicalpath();

					String check = physicalPath.substring(physicalPath.length() - 1, physicalPath.length());
					if (check.equals("\\")) {
						physicalPath = physicalPath.substring(0, physicalPath.length() - 1);
					}
					physicalPath = physicalPath.concat("\\");
					
					String modifiedPhysicalPath = physicalPath.replace(File.separator, "\\\\");
					ftpConfig.setSphysicalpath(modifiedPhysicalPath);
					
					final String updateQueryString = "update ftpconfig set sphysicalpath=N'"
													+ stringUtilityFunction.replaceQuote(ftpConfig.getSphysicalpath()) + "',shost=N'"
													+ stringUtilityFunction.replaceQuote(ftpConfig.getShost()) + "',spassword=N'"
													+ stringUtilityFunction.replaceQuote(ftpConfig.getSpassword()) + "',nportno=" + ftpConfig.getNportno()
													+ ",ndefaultstatus=" + ftpConfig.getNdefaultstatus() + ",nsslrequired="
													+ ftpConfig.getNsslrequired() + ",nchecksumrequired=" + ftpConfig.getNchecksumrequired()
													+ ",susername=N'" + stringUtilityFunction.replaceQuote(ftpConfig.getSusername()) + "',nsitecode="
													+ ftpConfig.getNsitecode() + ",nftptypecode="+ftpConfig.getNftptypecode()+",dmodifieddate='"
													+ dateUtilityFunction.getCurrentDateTime(userInfo)+"' where nftpno=" + ftpConfig.getNftpno();

					jdbcTemplate.execute(updateQueryString);
					
					passwordUtilityFunction.encryptPassword("ftpconfig", "nftpno", ftpConfig.getNftpno(), ftpConfig.getSpassword(),
							"spassword");
					savedFTPList.add(ftpConfig);
					editedFTPList.add(objFTPConfig);
					multilingualIDList.add("IDS_EDITFTPCONFIG");
					
					auditUtilityFunction.fnInsertAuditAction(savedFTPList, 2, editedFTPList, multilingualIDList, userInfo);

					// status code:200
					return getFTPConfig(userInfo);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_WRONGCREDENTIALS",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to delete an entry in ftpconfig table Need to check For
	 * Default record
	 * 
	 * @param ftpConfig [FTPConfig] object to be deleted
	 * @param userInfo  [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status ,If
	 *         it is a default record then return default cannot be deleted
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> deleteFTPConfig(FTPConfig ftpConfig, UserInfo userInfo) throws Exception {
	
		final FTPConfig objFTPConfig = getActiveFTPConfigById(ftpConfig.getNftpno(),userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedFTPConfig = new ArrayList<>();
	
		if (objFTPConfig == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			
			final FTPConfig defaultFTPConfig = getFTPConfigByStatus(ftpConfig.getNsitecode());
			if (defaultFTPConfig != null && defaultFTPConfig.getNftpno() == ftpConfig.getNftpno()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(),
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			
			final String deleteQuery = "update FTPconfig set nstatus="
										+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nftpno="
										+ ftpConfig.getNftpno();
			ftpConfig.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			jdbcTemplate.execute(deleteQuery);
			
			deletedFTPConfig.add(objFTPConfig);
			multilingualIDList.add("IDS_DELETEFTPCONFIG");
			
			auditUtilityFunction.fnInsertAuditAction(deletedFTPConfig, 1, null, multilingualIDList, userInfo);
			
			return getFTPConfig(userInfo);
		}

	}

	/**
	 * This method is used to set a FTP as default in ftpconfig table * Need to set
	 * other record as not default if it is a Default FTP
	 * 
	 * @param ftpConfig [FTPConfig] object to be set as default
	 * @param userInfo  [UserInfo] holds value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> setDefaultFTPConfig(FTPConfig ftpConfig, UserInfo userInfo) throws Exception {
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> newDefaultFTPConfig = new ArrayList<>();
		final List<Object> oldDefaultFTPConfig = new ArrayList<>();
		final FTPConfig objFTPConfig = getActiveFTPConfigById(ftpConfig.getNftpno(),userInfo);

		if (objFTPConfig == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// if yes need to set other default FTP as not a default FTP
			if (ftpConfig.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final FTPConfig defaultFTPConfig = getFTPConfigByStatus(ftpConfig.getNsitecode());
				
				if (defaultFTPConfig != null && defaultFTPConfig.getNftpno() != ftpConfig.getNftpno()) {
					final FTPConfig newFTP = SerializationUtils.clone(defaultFTPConfig);

					defaultFTPConfig.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					
					final String updateQueryString = " update ftpconfig set ndefaultstatus="
													+ Enumeration.TransactionStatus.NO.gettransactionstatus()+", dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nftpno="
													+ defaultFTPConfig.getNftpno();
					jdbcTemplate.execute(updateQueryString);
					newDefaultFTPConfig.add(defaultFTPConfig);
					oldDefaultFTPConfig.add(newFTP);
				}
			}
			
			final String updateQuery = "update FTPconfig set ndefaultstatus=" + ftpConfig.getNdefaultstatus()
										+", dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nftpno=" + ftpConfig.getNftpno();
			jdbcTemplate.execute(updateQuery);
			
			newDefaultFTPConfig.add(ftpConfig);
			oldDefaultFTPConfig.add(objFTPConfig);
			multilingualIDList.add("IDS_SETDEFAULTFTP");
			
			//fnInsertAuditAction(newDefaultFTPConfig, 2, oldDefaultFTPConfig, multilingualIDList, userInfo);
			
			return getFTPConfig(userInfo);
		}

	}

	/**
	 * This method is used to test connection of FTP through credentials given by
	 * users
	 * 
	 * @param objFTPconfig        [FTPConfig] object to be test
	 * @param isPasswordEncrypted [boolean] to check whether the password is
	 *                            encrypted form (true) or it is in plain text form
	 *                            (false)
	 * @return a boolean true is test connection is success otherwise false
	 * @throws Exception
	 */
	public boolean testFTPConnection(FTPConfig objFTPconfig, boolean isPasswordEncrypted) throws Exception {
		boolean value = false;
		String passWord = "";
		try {
			if (!isPasswordEncrypted)// if password is available and plain in object
			{
				if (objFTPconfig == null) {
					return value;
				} else {
					passWord = objFTPconfig.getSpassword().trim();
				}
			} else {// if password is not available or the password is encrypted form in object
				final String strQuery = "select * from FTPconfig where nftpno = " + objFTPconfig.getNftpno()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				objFTPconfig = (FTPConfig) jdbcUtilityFunction.queryForObject(strQuery, FTPConfig.class, jdbcTemplate);
				
				if (objFTPconfig != null) {
					passWord = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno", objFTPconfig.getNftpno(), "spassword");
				} else {
					return value;
				}
			}
			
			if(objFTPconfig.getNftptypecode()==1) {
				if (objFTPconfig.getNsslrequired() == 3)// ssl
				{
					final FTPSClient ftpsClient = new FTPSClient("SSL");
					ftpsClient.setAuthValue("SSL");
					ftpsClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
					ftpsClient.connect(objFTPconfig.getShost(), objFTPconfig.getNportno());
					ftpsClient.setBufferSize(1000);
					ftpsClient.login(objFTPconfig.getSusername(), passWord);
					
					boolean answer = ftpsClient.sendNoOp(); // if connection success return true, if fail return false
					value = answer; // set the connection status here to show alert in screen
				} else if (objFTPconfig.getNsslrequired() == 4)// non ssl
				{
					final FTPClient ftpClient = new FTPClient();
					ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
					ftpClient.connect(objFTPconfig.getShost(), objFTPconfig.getNportno());
					ftpClient.setBufferSize(1000);
					ftpClient.login(objFTPconfig.getSusername(), passWord);
					
					boolean answer = ftpClient.sendNoOp(); // if connection success return true, if fail return false
					value = answer; // set the connection status here to show alert in screen
				}
			}else if(objFTPconfig.getNftptypecode()==2) {
				
				final Properties prop=new Properties();
			    prop.put("StrictHostKeyChecking", "no");
			  
			    final JSch jsch = new JSch();

			   
			    final Session jschSession = jsch.getSession(objFTPconfig.getSusername(), objFTPconfig.getShost(),objFTPconfig.getNportno());
			    jschSession.setPassword(passWord);
			    jschSession.setConfig(prop);
			    jschSession.connect();

			    final ChannelSftp channelftp=(ChannelSftp) jschSession.openChannel("sftp");
			    channelftp.connect();
			    value=jschSession.isConnected();
			 
			    channelftp.exit();
			    jschSession.disconnect();
				
			}
			return value;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return false;
		}
	}

	/**
	 * This method is used to get certificate details for create symmetric key
	 * 
	 * @throws Exception
	 */
	public void getCertificateDetail() throws Exception {

		final String query = "select certificate_id as nsitecode from sys.certificates";
		final List<Site> lstsite = (List<Site>) jdbcTemplate.query(query, new Site());
		
		if (lstsite != null && lstsite.isEmpty()) {
			String query1 = "create master key encryption by password = 'silaUQ@12';create certificate AG_ADMIN with subject = 'Password For Agaram';create symmetric key AGARAMTECH with algorithm = aes_192 encryption by certificate AG_ADMIN;";
			jdbcTemplate.execute(query1);
		}
	}

	/**
	 * This method is used to Encrypt the password
	 * 
	 * @param stablename      [String] name of the table where the password column
	 *                        is present
	 * @param suniqeField     [String] primary key field name of the table
	 * @param nuniqueValue    [String] primary key value of the record
	 * @param sPassword       [String] a plain password string
	 * @param spasswordColumn [String] password column name
	 * @throws Exception
	 */
	public void fnPasswordEncrypt(String stablename, String suniqeField, int nuniqueValue, String sPassword,
			String spasswordColumn) {
		
		final String queryEncrypt = "open symmetric key AGARAMTECH decryption by certificate AG_ADMIN;" + "update "
									+ stablename + " set " + spasswordColumn + " = encryptbykey(key_guid('AGARAMTECH'), '" + sPassword
									+ "', 3, '1') " + " WHERE " + suniqeField + " = " + nuniqueValue + ";" + "close all symmetric keys;";
		jdbcTemplate.execute(queryEncrypt);
	}
	
	@Override
	public ResponseEntity<Object> getFTPType(UserInfo userInfo) throws Exception {
		
		final String strQuery = "select ft.nftptypecode,ft.jsondata->'sftptypename'->>'"+userInfo.getSlanguagetypecode()
								+"' as sftptypename,ft.ndefaultstatus"
								+ " from ftptype ft where ft.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<>((List<FTPConfig>) jdbcTemplate.query(strQuery, new FTPConfig()), HttpStatus.OK);
	}

}
