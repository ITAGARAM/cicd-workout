package com.agaramtech.qualis.global;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.AWSStorageConfig;
import com.agaramtech.qualis.configuration.model.FTPConfig;
import com.agaramtech.qualis.configuration.model.FTPSubFolder;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.regions.Region;

@RequiredArgsConstructor
@Component
public class FTPUtilityFunction {

	private static final Logger LOGGER = LoggerFactory.getLogger(FTPUtilityFunction.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final PasswordUtilityFunction passwordUtilityFunction;
	private final CloudStorageUtilityFunction cloudStorageUtilityFunction;

	private final static String OS = System.getProperty("os.name").toLowerCase();
	public final static boolean IS_WINDOWS = (OS.indexOf("win") >= 0);
	public final static boolean IS_UNIX = (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);

	public String getFileAbsolutePath() throws Exception {
		final String homePathQuery = "select ssettingvalue from settings where nsettingcode ="
				+ Enumeration.Settings.DEPLOYMENTSERVER_HOMEPATH.getNsettingcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final String homePath = (String) jdbcUtilityFunction.queryForObject(homePathQuery, String.class, jdbcTemplate);

		return homePath;
	}

	public String getFTPFileWritingPath(String sCustomPath, UserInfo userInfo) throws Exception {
		String path = null;
		// ALPD-4436
		// To get path value from system's environment variables instead of absolutepath
		final String homePath = getFileAbsolutePath();
		LOGGER.info("homePath :--------------> " + homePath);
		if (IS_UNIX) {
			LOGGER.info("System OS:--------------> " + System.getProperty("os.name"));

			// commented by Rajesh 20-JUN-2025
			// LOGGER.info("Container Environment Variable :-------->
			// "+System.getenv("TOMCAT_HOME")!=null ? System.getenv("TOMCAT_HOME"):"NA");
//			if (System.getenv("JBOSS_HOME") != null && System.getenv("JBOSS_HOME").length() > 0) {
//				LOGGER.info("homePath (1) :--------------> "+System.getenv("JBOSS_HOME"));
//				LOGGER.info("System has JBOSS CONTAINER:--------------> "+System.getenv("JBOSS_HOME"));
//				path = sCustomPath.isEmpty()
//						? System.getenv(homePath).substring(0, System.getenv(homePath).indexOf("bin"))
//								+ Enumeration.FTP.JBOSS_EAP_7_2_0_UPLOAD_PATH.getFTP()
//								+ (userInfo == null ? "" : userInfo.getSloginid()) + File.separatorChar
//						: sCustomPath;
//			}			
//			else if(System.getenv("TOMCAT_HOME") != null && System.getenv("TOMCAT_HOME").length() > 0) {
//				LOGGER.info("homePath (2) :--------------> "+System.getenv("TOMCAT_HOME"));
//				LOGGER.info("System has TOMCAT CONTAINER:--------------> "+System.getenv("TOMCAT_HOME"));
//				path = sCustomPath.isEmpty()
//						? 
//								//System.getenv(homePath).substring(0, System.getenv(homePath).indexOf("bin"))
//								System.getenv(homePath)
//								+ Enumeration.FTP.UBUNTU_TOMCAT_UPLOAD_PATH.getFTP()
//								+ (userInfo == null ? "" : userInfo.getSloginid()) + File.separatorChar
//						: sCustomPath;
//			}

			// Added by Rajesh 20-JUN-2025
			LOGGER.info("homePath (a) :--------------> " + System.getenv("TOMCAT_HOME"));
			if (System.getenv("TOMCAT_HOME") != null && System.getenv("TOMCAT_HOME").length() > 0) {
				LOGGER.info("homePath (2) :--------------> " + System.getenv("TOMCAT_HOME"));
				LOGGER.info("System has TOMCAT CONTAINER:--------------> " + System.getenv("TOMCAT_HOME"));
				path = sCustomPath.isEmpty() ?
				// System.getenv(homePath).substring(0, System.getenv(homePath).indexOf("bin"))
						System.getenv(homePath) + Enumeration.FTP.UBUNTU_TOMCAT_UPLOAD_PATH.getFTP()
								+ (userInfo == null ? "" : userInfo.getSloginid()) + File.separatorChar
						: sCustomPath;
			}

		} else if (IS_WINDOWS) {
//			path = sCustomPath.isEmpty() ? 
//					new File("").getAbsolutePath()+Enumeration.FTP.UPLOAD_PATH.getFTP()+(userInfo==null?"":userInfo.getSloginid())+File.separatorChar : sCustomPath;
			path = sCustomPath.isEmpty()
					? System.getenv(homePath) + Enumeration.FTP.UPLOAD_PATH.getFTP()
							+ (userInfo == null ? "" : userInfo.getSloginid()) + File.separatorChar
					: sCustomPath;
		}
		LOGGER.info("Path :---------->" + path);
		return path;
	}

	public Map<String, Object> FileViewUsingFtp(String ssystemfilename, int ncontrolcode, final UserInfo objUserInfo,
			final String sCustomPath, String sCustomName) throws Exception {
		Map<String, Object> map = new HashMap<>();

		final List<FTPSubFolder> lstForm = jdbcTemplate.query(
				" select ssubfoldername,ncontrolcode from ftpsubfolder " + " where nformcode="
						+ objUserInfo.getNformcode() + " and nsitecode=" + objUserInfo.getNmastersitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
				new FTPSubFolder());

		Settings objSettingsS3 = cloudStorageUtilityFunction.getSetting(objUserInfo);

		if (Integer.valueOf(objSettingsS3.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			String changeWorkingDirectory = "";
			if (!lstForm.isEmpty()) {
				changeWorkingDirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
						.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
			}

			Map<String, Object> mapRtnObj = cloudStorageUtilityFunction.fileViewAWSStorage(ssystemfilename, "",
					changeWorkingDirectory, objUserInfo);
			return mapRtnObj;
		} else {
			sCustomName = sCustomName.isEmpty() ? ""
					: sCustomName.concat(
							ssystemfilename.substring(ssystemfilename.lastIndexOf("."), ssystemfilename.length()));
			final String ftpQuery = "select * from ftpconfig where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode = "
					+ objUserInfo.getNmastersitecode();
			final FTPConfig objFTPConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(ftpQuery, FTPConfig.class,
					jdbcTemplate);

			if (objFTPConfig != null) {
				LOGGER.info("ftp config avaialable --- > " + objFTPConfig.getShost());
//				final List<FTPSubFolder> lstForm = jdbcTemplate.query(
//						" select ssubfoldername,ncontrolcode from ftpsubfolder "
//						+ " where nformcode="+ objUserInfo.getNformcode() 
//						+ " and nsitecode=" + objUserInfo.getNmastersitecode()
//						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//						+ "",
//						new FTPSubFolder());

				String changeWorkingDirectory = "";
//				String ftpSubFolderPath = "";
				if (!lstForm.isEmpty()) {
					// Commented because it should not take the directory without controlcode
//					if (lstForm.size() == 1) {
//						changeWorkingDirectory = lstForm.get(0).getSsubfoldername();
//					} else {
					changeWorkingDirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
							.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
					// changedirectory=(String) lstString.get(0);
//					}
//					ftpSubFolderPath= changeWorkingDirectory+"\\\\";
				}

				LOGGER.info("changeWorking Directory --- > " + changeWorkingDirectory);
				final String sQuery = "select ssettingvalue from settings where nsettingcode = "
						+ Enumeration.Settings.FILE_VIEW_SOURCE.getNsettingcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(sQuery, Settings.class,
						jdbcTemplate);

				final String sReportQuery = "select ssettingvalue from settings where nsettingcode = "
						+ Enumeration.Settings.ATTACHMENT_DOWNLOAD_URL.getNsettingcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Settings objReportsettings = (Settings) jdbcUtilityFunction.queryForObject(sReportQuery,
						Settings.class, jdbcTemplate);
				// if ssettingstring=3 then file download in sharefolder by creating user
				// loginid as folder name
				// else if ssettingstring=4 then file download from ftp.
				if (objSettings.getSsettingvalue()
						.equals(Enumeration.TransactionStatus.YES.gettransactionstatus() + "")) {

					if (objFTPConfig.getNftptypecode() == 1) {
						if (objFTPConfig.getNsslrequired() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) { // SSL
							try {
								final String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig",
										"nftpno", objFTPConfig.getNftpno(), "spassword");
								final FTPSClient ftp = new FTPSClient("SSL");
								ftp.setAuthValue("SSL");
								ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
								ftp.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
								ftp.setBufferSize(1000);

								final boolean ftpFile = ftp.login(objFTPConfig.getSusername(), RtnPasswordEncrypt);
								ftp.enterLocalPassiveMode();
								ftp.setFileType(FTP.BINARY_FILE_TYPE);
								if (ftpFile) {

									final String absolutePath1 = getFTPFileWritingPath(sCustomPath, objUserInfo);
									final File file1 = new File(absolutePath1);
									try {
										if (file1.exists() == false) {
											file1.mkdir();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}

									String sAttachedFile = "";
									String uniquefilename = sCustomName.isEmpty() ? ssystemfilename : sCustomName;
									String path = "";

									boolean nflagFtp = false;
									if (sCustomPath.isEmpty()) {
										path = absolutePath1 + uniquefilename;
										DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss", Locale.getDefault());
										Date today = Calendar.getInstance().getTime();
										String reportDate = df.format(today);
										File file = new File(path);
										if (file.exists()) {
											sAttachedFile = objUserInfo.getSloginid() + "/" + uniquefilename;
											nflagFtp = true;
										} else {
											if (changeWorkingDirectory != "" && changeWorkingDirectory != ","
													&& !changeWorkingDirectory.equals("")
													&& !changeWorkingDirectory.equals(",")
													&& changeWorkingDirectory.length() > 1) {
												LOGGER.info("Change Working Directory CWD........."
														+ changeWorkingDirectory);
												ftp.changeWorkingDirectory(changeWorkingDirectory);
												LOGGER.info("Printing Change Working Directory CWD........."
														+ ftp.printWorkingDirectory());
											}
											nflagFtp = Arrays.asList(ftp.listFiles()).stream()
													.anyMatch(item -> item.getName().equals(ssystemfilename));
											if (nflagFtp) {
												FileOutputStream regFileWrite = new FileOutputStream(file);
												boolean download = ftp.retrieveFile(ssystemfilename, regFileWrite);
												if (download) {
													LOGGER.info("File downloaded successfully !");
												} else {
													LOGGER.info("Error in downloading file !");
												}
												regFileWrite.flush();
												regFileWrite.close();
												sAttachedFile = objUserInfo.getSloginid() + "/" + uniquefilename;
											} else {
												map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
														commonFunction.getMultilingualMessage(
																"IDS_FILENOTFOUNDINFTPPATH",
																objUserInfo.getSlanguagefilename()));
											}
										}
									} else {
										path = sCustomPath + (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
										File file = new File(path);
										if (file.exists()) {
											sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
											nflagFtp = true;
										} else {
											if (changeWorkingDirectory != "" && changeWorkingDirectory != ","
													&& !changeWorkingDirectory.equals("")
													&& !changeWorkingDirectory.equals(",")
													&& changeWorkingDirectory.length() > 1) {
												LOGGER.info("Change Working Directory CWD........."
														+ changeWorkingDirectory);
												ftp.changeWorkingDirectory(changeWorkingDirectory);
												LOGGER.info("Printing Change Working Directory CWD........."
														+ ftp.printWorkingDirectory());
											}
											nflagFtp = Arrays.asList(ftp.listFiles()).stream()
													.anyMatch(item -> item.getName().equals(ssystemfilename));
											if (nflagFtp) {
												FileOutputStream regFileWrite = new FileOutputStream(file);
												boolean download = ftp.retrieveFile(ssystemfilename, regFileWrite);
												if (download) {
													LOGGER.info("File downloaded successfully !");
												} else {
													LOGGER.info("Error in downloading file !");
												}
												regFileWrite.flush();
												regFileWrite.close();
												sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
											} else {
												map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
														commonFunction.getMultilingualMessage(
																"IDS_FILENOTFOUNDINFTPPATH",
																objUserInfo.getSlanguagefilename()));
											}
										}
									}
									if (nflagFtp == true) {
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
										map.put("AttachFile", sAttachedFile);
										map.put("FileName", uniquefilename);
										map.put("FilePath", objReportsettings.getSsettingvalue() + sAttachedFile);
									}
									ftp.logout();
								} else {
									map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											"IDS_CHECKFTPCONNECTION");
								}
							} catch (Exception e) {
								LOGGER.info("Wrong FTP Credentials --- > " + e.getMessage());
								LOGGER.info("Wrong FTP Credentials --- > " + e.getCause());
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										"IDS_CHECKFTPCONNECTION");
							}
						} else { // Non SSL
							try {
								String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig",
										"nftpno", objFTPConfig.getNftpno(), "spassword");
								FTPClient ftp = new FTPClient();
								ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
								ftp.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
								ftp.setBufferSize(1000);
								boolean ftpFile = ftp.login(objFTPConfig.getSusername(), RtnPasswordEncrypt);
								ftp.enterLocalPassiveMode();
								ftp.setFileType(FTP.BINARY_FILE_TYPE);
								if (ftpFile) {
									LOGGER.info("Obtaining Obsolute Path --- > ");
									String absolutePath1 = getFTPFileWritingPath(sCustomPath, objUserInfo);
									LOGGER.info("Obtaining Obsolute Path --- > " + absolutePath1);
									File file1 = new File(absolutePath1);
									try {
										if (file1.exists() == false) {
											file1.mkdir();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									String sAttachedFile = "";
									String sCompressfilename = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
									String path = "";
									boolean nflagFtp = false;
									if (sCustomPath.isEmpty()) {
										path = absolutePath1 + sCompressfilename;
										DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss", Locale.getDefault());
										Date today = Calendar.getInstance().getTime();
										File file = new File(path);
										String reportDate = df.format(today);
										if (file.exists()) {
											sAttachedFile = objUserInfo.getSloginid() + "/" + sCompressfilename;
											nflagFtp = true;
										} else {
											if (changeWorkingDirectory != "" && changeWorkingDirectory != ","
													&& !changeWorkingDirectory.equals("")
													&& !changeWorkingDirectory.equals(",")
													&& changeWorkingDirectory.length() > 1) {
												LOGGER.info("Change Working Directory CWD........."
														+ changeWorkingDirectory);
												ftp.changeWorkingDirectory(changeWorkingDirectory);
												LOGGER.info("Printing Change Working Directory CWD........."
														+ ftp.printWorkingDirectory());
											}
											nflagFtp = Arrays.asList(ftp.listFiles()).stream()
													.anyMatch(item -> item.getName().equals(ssystemfilename));
											if (nflagFtp) {
												FileOutputStream regFileWrite = new FileOutputStream(file);
												boolean download = ftp.retrieveFile(ssystemfilename, regFileWrite);
												if (download) {
													LOGGER.info("File downloaded successfully !");
												} else {
													LOGGER.info("Error in downloading file !");
												}
												regFileWrite.flush();
												regFileWrite.close();
												sAttachedFile = objUserInfo.getSloginid() + "/" + sCompressfilename;
											} else {
												map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
														commonFunction.getMultilingualMessage(
																"IDS_FILENOTFOUNDINFTPPATH",
																objUserInfo.getSlanguagefilename()));
											}
										}
//										if (changeWorkingDirectory != "") {
//											ftp.changeWorkingDirectory("/" + changeWorkingDirectory);
//										}
									} else {
										path = sCustomPath + (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
										File file = new File(path);
										if (file.exists()) {
											sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
											nflagFtp = true;
										} else {
											if (changeWorkingDirectory != "" && changeWorkingDirectory != ","
													&& !changeWorkingDirectory.equals("")
													&& !changeWorkingDirectory.equals(",")
													&& changeWorkingDirectory.length() > 1) {
												LOGGER.info("Change Working Directory CWD........."
														+ changeWorkingDirectory);
												ftp.changeWorkingDirectory(changeWorkingDirectory);
												LOGGER.info("Printing Change Working Directory CWD........."
														+ ftp.printWorkingDirectory());
											}
											nflagFtp = Arrays.asList(ftp.listFiles()).stream()
													.anyMatch(item -> item.getName().equals(ssystemfilename));
											if (nflagFtp) {
												FileOutputStream regFileWrite = new FileOutputStream(file);
												boolean download = ftp.retrieveFile(ssystemfilename, regFileWrite);
												if (download) {
													LOGGER.info("File downloaded successfully !");
												} else {
													LOGGER.info("Error in downloading file !");
												}
												regFileWrite.flush();
												regFileWrite.close();
												sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
											} else {
												map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
														commonFunction.getMultilingualMessage(
																"IDS_FILENOTFOUNDINFTPPATH",
																objUserInfo.getSlanguagefilename()));
											}
										}
									}
									if (nflagFtp == true) {
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
//										ftp.logout();
										map.put("AttachFile", sAttachedFile);
										map.put("FileName", sCompressfilename);
										map.put("FilePath", objReportsettings.getSsettingvalue() + sAttachedFile);
									}
									ftp.logout();
								} else {
									map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											"IDS_CHECKFTPCONNECTION");
								}
							} catch (Exception e) {
								LOGGER.info("Wrong FTP Credentials --- > " + e.getMessage());
								LOGGER.info("Wrong FTP Credentials --- > " + e.getCause());
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										"IDS_CHECKFTPCONNECTION");
							}
						}
					} else if (objFTPConfig.getNftptypecode() == 2) {
						String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
								objFTPConfig.getNftpno(), "spassword");
						Properties prop = new Properties();
						prop.put("StrictHostKeyChecking", "no");
						JSch jsch = new JSch();
						Session jschSession = jsch.getSession(objFTPConfig.getSusername(), objFTPConfig.getShost(),
								objFTPConfig.getNportno());
						jschSession.setPassword(RtnPasswordEncrypt);
						jschSession.setConfig(prop);
						jschSession.connect();
						ChannelSftp channelftp = (ChannelSftp) jschSession.openChannel("sftp");
						channelftp.connect();
						boolean value = jschSession.isConnected();
						if (value) {
							// ALPD-4436
							// To get path value from system's environment variables instead of absolutepath
							final String homePath = getFileAbsolutePath();
							String absolutePath1 = sCustomPath.isEmpty() ? System.getenv(homePath)// new
																									// File("").getAbsolutePath()
									+ Enumeration.FTP.UPLOAD_PATH.getFTP() + objUserInfo.getSloginid() + "\\"
									: sCustomPath;
							File file1 = new File(absolutePath1);
							try {
								if (file1.exists() == false) {
									file1.mkdir();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							String sAttachedFile = "";
							String sCompressfilename = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
							String path = "";
							boolean nflagSftp = false;
							if (sCustomPath.isEmpty()) {
								path = absolutePath1 + sCompressfilename;
								DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss", Locale.getDefault());
								Date today = Calendar.getInstance().getTime();
								File file = new File(path);
								String reportDate = df.format(today);
								if (file.exists()) {
									sAttachedFile = objUserInfo.getSloginid() + "/" + sCompressfilename;
									nflagSftp = true;
								} else {
									if (changeWorkingDirectory != "") {
										channelftp.cd("/" + changeWorkingDirectory);
									}
									List<LsEntry> listFilesInSftpPath = channelftp.ls(".");
									nflagSftp = listFilesInSftpPath.stream().map(LsEntry::getFilename)
											.anyMatch(name -> name.equals(ssystemfilename));
									if (nflagSftp) {
										try {
											channelftp.get(ssystemfilename, path);
											LOGGER.info("File downloaded successfully !");
										} catch (Exception e) {
											LOGGER.info("Error in downloading file !");
										}
										sAttachedFile = objUserInfo.getSloginid() + "/" + sCompressfilename;
									} else {
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												commonFunction.getMultilingualMessage("IDS_FILENOTFOUNDINSFTPPATH",
														objUserInfo.getSlanguagefilename()));
									}
								}
							} else {
								path = sCustomPath + (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
								File file = new File(path);
								if (file.exists()) {
									sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
									nflagSftp = true;
								} else {
									if (changeWorkingDirectory != "") {
										channelftp.cd(changeWorkingDirectory);
									}
									List<LsEntry> listFilesInSftpPath = channelftp.ls(".");
									nflagSftp = listFilesInSftpPath.stream().map(LsEntry::getFilename)
											.anyMatch(name -> name.equals(ssystemfilename));
									if (nflagSftp) {
										try {
											channelftp.get(ssystemfilename, path);
											LOGGER.info("File downloaded successfully !");
										} catch (Exception e) {
											LOGGER.info("Error in downloading file !");
										}
										sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
									} else {
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												commonFunction.getMultilingualMessage("IDS_FILENOTFOUNDINSFTPPATH",
														objUserInfo.getSlanguagefilename()));
									}
								}
							}
							if (nflagSftp == true) {
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
								map.put("AttachFile", sAttachedFile);
								map.put("FileName", sCompressfilename);
								map.put("FilePath", objReportsettings.getSsettingvalue() + sAttachedFile);
							}
							channelftp.disconnect();
							jschSession.disconnect();
						}
					}

				} else if (objSettings.getSsettingvalue()
						.equals(Enumeration.TransactionStatus.NO.gettransactionstatus() + "")) {
					map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					String viewfilepath = "ftp:\\" + objFTPConfig.getShost() + "\\" + ssystemfilename;
					map.put("AttachFtp", viewfilepath);
				}
			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_CHECKFTPCONNECTION");
			}
			return map;
		}
	}

	public Map<String, Object> multiPathMultiFileDownloadUsingFtp(final Map<String, Object> fileMap,
			final List<ControlMaster> controlCodeList, final UserInfo objUserInfo, final String sCustomPath)
			throws Exception {
		Map<String, Object> map = new HashMap<>();

		Settings objSettingsS3 = cloudStorageUtilityFunction.getSetting(objUserInfo);

		if (Integer.valueOf(objSettingsS3.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			map = cloudStorageUtilityFunction.multiPathMultiFileDownloadUsingS3(fileMap, controlCodeList, objUserInfo,
					sCustomPath);
		} else {
			final String ftpQuery = "select * from ftpconfig where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode = "
					+ objUserInfo.getNmastersitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final FTPConfig objFTPConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(ftpQuery, FTPConfig.class,
					jdbcTemplate);

			if (objFTPConfig != null) {

				String changeWorkingDirectory = "";

				final String sQuery = "select ssettingvalue from settings where nsettingcode = "
						+ Enumeration.Settings.FILE_VIEW_SOURCE.getNsettingcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Settings objsettings = (Settings) jdbcUtilityFunction.queryForObject(sQuery, Settings.class,
						jdbcTemplate);

				final String sReportQuery = "select ssettingvalue from settings where nsettingcode = "
						+ Enumeration.Settings.FILE_VIEW_SOURCE.getNsettingcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final Settings objReportsettings = (Settings) jdbcUtilityFunction.queryForObject(sReportQuery,
						Settings.class, jdbcTemplate);

				if (objsettings.getSsettingvalue()
						.equals(Enumeration.TransactionStatus.YES.gettransactionstatus() + "")) {

					if (objFTPConfig.getNftptypecode() == 1) {

						if (objFTPConfig.getNsslrequired() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) { // SSL
							try {
								String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig",
										"nftpno", objFTPConfig.getNftpno(), "spassword");
								FTPSClient ftp = new FTPSClient("SSL");
								ftp.setAuthValue("SSL");
								ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
								ftp.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
								ftp.setBufferSize(1000);

								boolean ftpFile = ftp.login(objFTPConfig.getSusername(), RtnPasswordEncrypt);
								ftp.enterLocalPassiveMode();
								ftp.setFileType(FTP.BINARY_FILE_TYPE);

								if (ftpFile) {
									List<String> lstSystemFileName = new ArrayList<>();
									for (int j = 0; j < controlCodeList.size(); j++) {
										changeWorkingDirectory = controlCodeList.get(j).getSsubfoldername();

										if (!changeWorkingDirectory.isEmpty()) {
											ftp.changeWorkingDirectory("/" + changeWorkingDirectory);
										} else {
											ftp.changeToParentDirectory();
										}

										if (fileMap
												.get(controlCodeList.get(j).getScontrolname() + "_fileName") != null) {
											boolean nflagFtp = false;
											String filePath = (String) fileMap
													.get(controlCodeList.get(j).getScontrolname() + "_path");

											File file1 = new File(filePath);
											try {
												if (file1.exists() == false) {
													file1.mkdir();
												}
											} catch (Exception e) {
												e.printStackTrace();
											}

											String sCustomName = (String) fileMap
													.get(controlCodeList.get(j).getScontrolname() + "_customName");
											final String ssystemfilename = (String) fileMap
													.get(controlCodeList.get(j).getScontrolname() + "_fileName");
											sCustomName = sCustomName.isEmpty() ? ""
													: sCustomName.concat(
															ssystemfilename.substring(ssystemfilename.lastIndexOf("."),
																	ssystemfilename.length()));
											final String sActualFileName = fileMap.containsKey(
													controlCodeList.get(j).getScontrolname() + "_actualFileName")
															? (String) fileMap
																	.get(controlCodeList.get(j).getScontrolname()
																			+ "_actualFileName")
															: "";

											String sAttachedFile = "";
											String uniquefilename = sCustomName.isEmpty() ? ssystemfilename
													: sCustomName;
											String path = "";
											String ftpPhysicalPath = objFTPConfig.getSphysicalpath().toString()
													+ controlCodeList.get(j).getSsubfoldername() + "\\\\"
													+ ssystemfilename;
											File fileFTP = new File(ftpPhysicalPath);
											if (sCustomPath.isEmpty()) {
												// path = absolutePath1 + uniquefilename;
												path = filePath + uniquefilename;
												final DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss",
														Locale.getDefault());
												final Date today = Calendar.getInstance().getTime();
												final String reportDate = df.format(today);

												File file = new File(path);
												if (file.exists() == true) {
													sAttachedFile = uniquefilename;
													nflagFtp = true;
													map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()),
															"true");
												} else {
													nflagFtp = Arrays.asList(ftp.listFiles()).stream()
															.anyMatch(item -> item.getName().equals(ssystemfilename));
													if (nflagFtp) {
														final FileOutputStream regFileWrite = new FileOutputStream(
																file);
														boolean download = ftp.retrieveFile(ssystemfilename,
																regFileWrite);
														if (download) {
															LOGGER.info("File downloaded successfully !");
															map.put(String.valueOf(
																	controlCodeList.get(j).getNcontrolcode()), "true");
														} else {
															LOGGER.info("Error in downloading file !");
															map.put(String.valueOf(
																	controlCodeList.get(j).getNcontrolcode()), "false");
														}
														regFileWrite.flush();
														regFileWrite.close();
														ftp.logout();
														sAttachedFile = uniquefilename;
													} else {
														lstSystemFileName.add(sActualFileName != "" ? sActualFileName
																: ssystemfilename);
														map.put(String.valueOf(
																controlCodeList.get(j).getNcontrolcode()), "false");
													}
												}

											} else {
												path = sCustomPath
														+ (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
												final File file = new File(path);
												if (file.exists()) {
													sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename
															: sCustomName);
													nflagFtp = true;
													map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()),
															"true");
												} else {
													nflagFtp = Arrays.asList(ftp.listFiles()).stream()
															.anyMatch(item -> item.getName().equals(ssystemfilename));
													if (nflagFtp) {
														final FileOutputStream regFileWrite = new FileOutputStream(
																file);
														boolean download = ftp.retrieveFile(ssystemfilename,
																regFileWrite);
														if (download) {
															LOGGER.info("File downloaded successfully !");
															map.put(String.valueOf(
																	controlCodeList.get(j).getNcontrolcode()), "true");
														} else {
															LOGGER.info("Error in downloading file !");
															map.put(String.valueOf(
																	controlCodeList.get(j).getNcontrolcode()), "false");
														}
														regFileWrite.flush();
														regFileWrite.close();
														sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename
																: sCustomName);
													} else {
														lstSystemFileName.add(sActualFileName != "" ? sActualFileName
																: ssystemfilename);
														map.put(String.valueOf(
																controlCodeList.get(j).getNcontrolcode()), "false");
													}
												}
											}
											if (nflagFtp == true) {
												map.put(controlCodeList.get(j).getScontrolname() + "_AttachFile",
														sAttachedFile);
												map.put(controlCodeList.get(j).getScontrolname() + "_FileName",
														uniquefilename);
												map.put(controlCodeList.get(j).getScontrolname() + "_FilePath",
														// objReportsettings.getSsettingvalue() + sAttachedFile);
														filePath + sAttachedFile);
											}
										}
									}
									if (!lstSystemFileName.isEmpty()) {
										String strAlert = lstSystemFileName.stream().map(x -> String.valueOf(x))
												.collect(Collectors.joining(","));
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												strAlert + " "
														+ commonFunction.getMultilingualMessage(
																"IDS_FILENOTFOUNDINFTPPATH",
																objUserInfo.getSlanguagefilename()));
									} else {
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
									}
									ftp.logout();
								} else {
									map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											commonFunction.getMultilingualMessage("IDS_CHECKFTPCONNECTION",
													objUserInfo.getSlanguagefilename()));
									// "IDS_CHECKFTPCONNECTION");
								}
							} catch (Exception e) {
								LOGGER.info("Wrong FTP Credentials --- > " + e.getMessage());
								LOGGER.info("Wrong FTP Credentials --- > " + e.getCause());
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), // "IDS_CHECKFTPCONNECTION");
										commonFunction.getMultilingualMessage("IDS_CHECKFTPCONNECTION",
												objUserInfo.getSlanguagefilename()));
							}
						} else { // Non SSL
							try {

								String passWord = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
										objFTPConfig.getNftpno(), "spassword");

								final FTPClient ftp = new FTPClient();
								ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
								ftp.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
								ftp.setBufferSize(1000);

								boolean ftpFile = ftp.login(objFTPConfig.getSusername(), passWord);
								ftp.enterLocalPassiveMode();
								ftp.setFileType(FTP.BINARY_FILE_TYPE);

								if (ftpFile) {
									List<String> lstSystemFileName = new ArrayList<>();

									for (int j = 0; j < controlCodeList.size(); j++) {
										changeWorkingDirectory = controlCodeList.get(j).getSsubfoldername();

										if (!changeWorkingDirectory.isEmpty()) {
											ftp.changeWorkingDirectory("/" + changeWorkingDirectory);
										} else {
											ftp.changeToParentDirectory();
										}

										if (fileMap
												.get(controlCodeList.get(j).getScontrolname() + "_fileName") != null) {

											boolean nflagFtp = false;

											String filePath = (String) fileMap
													.get(controlCodeList.get(j).getScontrolname() + "_path");

											File file1 = new File(filePath);
											try {
												if (file1.exists() == false) {
													file1.mkdir();
												}
											} catch (Exception e) {
												e.printStackTrace();
											}

											String sCustomName = (String) fileMap
													.get(controlCodeList.get(j).getScontrolname() + "_customName");
											final String ssystemfilename = (String) fileMap
													.get(controlCodeList.get(j).getScontrolname() + "_fileName");
											sCustomName = sCustomName.isEmpty() ? ""
													: sCustomName.concat(
															ssystemfilename.substring(ssystemfilename.lastIndexOf("."),
																	ssystemfilename.length()));
											final String sActualFileName = fileMap.containsKey(
													controlCodeList.get(j).getScontrolname() + "_actualFileName")
															? (String) fileMap
																	.get(controlCodeList.get(j).getScontrolname()
																			+ "_actualFileName")
															: "";

											String sAttachedFile = "";
											String sCompressfilename = (sCustomName.isEmpty() ? ssystemfilename
													: sCustomName);
											String path = "";
											String ftpPhysicalPath = objFTPConfig.getSphysicalpath().toString()
													+ controlCodeList.get(j).getSsubfoldername() + "\\\\"
													+ ssystemfilename;
											File fileFTP = new File(ftpPhysicalPath);
											if (sCustomPath.isEmpty()) {
												path = filePath + sCompressfilename;
												DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss",
														Locale.getDefault());
												Date today = Calendar.getInstance().getTime();
												File file = new File(path);
												String reportDate = df.format(today);

												if (file.exists()) {
													sAttachedFile = sCompressfilename;
													nflagFtp = true;
													map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()),
															"true");
												} else {
													nflagFtp = Arrays.asList(ftp.listFiles()).stream()
															.anyMatch(item -> item.getName().equals(ssystemfilename));
													if (nflagFtp) {
														final FileOutputStream regFileWrite = new FileOutputStream(
																file);
														boolean download = ftp.retrieveFile(ssystemfilename,
																regFileWrite);
														if (download) {
															LOGGER.info("File downloaded successfully !");
															map.put(String.valueOf(
																	controlCodeList.get(j).getNcontrolcode()), "true");
														} else {
															LOGGER.info("Error in downloading file !");
															map.put(String.valueOf(
																	controlCodeList.get(j).getNcontrolcode()), "false");
														}
														regFileWrite.flush();
														regFileWrite.close();
														sAttachedFile = sCompressfilename;
													} else {
														lstSystemFileName.add(sActualFileName != "" ? sActualFileName
																: ssystemfilename);
														map.put(String.valueOf(
																controlCodeList.get(j).getNcontrolcode()), "false");
													}
												}
											} else {
												path = sCustomPath
														+ (sCustomName.isEmpty() ? ssystemfilename : sCustomName);

												final File file = new File(path);
												if (file.exists()) {
													sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename
															: sCustomName);
													nflagFtp = true;
													map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()),
															"true");
												} else {
													nflagFtp = Arrays.asList(ftp.listFiles()).stream()
															.anyMatch(item -> item.getName().equals(ssystemfilename));
													if (nflagFtp) {
														final FileOutputStream regFileWrite = new FileOutputStream(
																file);
														boolean download = ftp.retrieveFile(ssystemfilename,
																regFileWrite);
														if (download) {
															LOGGER.info("File downloaded successfully !");
															map.put(String.valueOf(
																	controlCodeList.get(j).getNcontrolcode()), "true");
														} else {
															LOGGER.info("Error in downloading file !");
															map.put(String.valueOf(
																	controlCodeList.get(j).getNcontrolcode()), "false");
														}
														regFileWrite.flush();
														regFileWrite.close();
														sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename
																: sCustomName);
													} else {
														lstSystemFileName.add(sActualFileName != "" ? sActualFileName
																: ssystemfilename);
														map.put(String.valueOf(
																controlCodeList.get(j).getNcontrolcode()), "false");
													}
												}
											}

											if (nflagFtp == true) {
												map.put(controlCodeList.get(j).getScontrolname() + "_AttachFile",
														sAttachedFile);
												map.put(controlCodeList.get(j).getScontrolname() + "_FileName",
														sCompressfilename);
												map.put(controlCodeList.get(j).getScontrolname() + "_FilePath",
														// objReportsettings.getSsettingvalue() + sAttachedFile);
														filePath + sAttachedFile);
											}

										}
									}
									if (!lstSystemFileName.isEmpty()) {
										String strAlert = lstSystemFileName.stream().map(x -> String.valueOf(x))
												.collect(Collectors.joining(","));
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												strAlert + " "
														+ commonFunction.getMultilingualMessage(
																"IDS_FILENOTFOUNDINFTPPATH",
																objUserInfo.getSlanguagefilename()));
									} else {
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
									}
									ftp.logout();
								} else {

									map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), // "IDS_CHECKFTPCONNECTION");
											commonFunction.getMultilingualMessage("IDS_CHECKFTPCONNECTION",
													objUserInfo.getSlanguagefilename()));
								}
							} catch (Exception e) {

								LOGGER.info("Wrong FTP Credentials --- > " + e.getMessage());
								LOGGER.info("Wrong FTP Credentials --- > " + e.getCause());
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), // "IDS_CHECKFTPCONNECTION");
										commonFunction.getMultilingualMessage("IDS_CHECKFTPCONNECTION",
												objUserInfo.getSlanguagefilename()));
							}
						}

					} else if (objFTPConfig.getNftptypecode() == 2) {

						String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
								objFTPConfig.getNftpno(), "spassword");
						Properties prop = new Properties();
						prop.put("StrictHostKeyChecking", "no");
						JSch jsch = new JSch();
						Session jschSession = jsch.getSession(objFTPConfig.getSusername(), objFTPConfig.getShost(),
								objFTPConfig.getNportno());
						jschSession.setPassword(RtnPasswordEncrypt);
						jschSession.setConfig(prop);
						jschSession.connect();
						ChannelSftp channelftp = (ChannelSftp) jschSession.openChannel("sftp");
						channelftp.connect();
						boolean value = jschSession.isConnected();
						if (value) {
							List<String> lstSystemFileName = new ArrayList<>();
							for (int j = 0; j < controlCodeList.size(); j++) {
								changeWorkingDirectory = controlCodeList.get(j).getSsubfoldername();

								if (!changeWorkingDirectory.isEmpty()) {
									channelftp.cd("/" + changeWorkingDirectory);
								}

								if (fileMap.get(controlCodeList.get(j).getScontrolname() + "_fileName") != null) {
									boolean nflagSftp = false;
									String filePath = (String) fileMap
											.get(controlCodeList.get(j).getScontrolname() + "_path");

									File file1 = new File(filePath);
									try {
										if (file1.exists() == false) {
											file1.mkdir();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}

									String sCustomName = (String) fileMap
											.get(controlCodeList.get(j).getScontrolname() + "_customName");
									final String ssystemfilename = (String) fileMap
											.get(controlCodeList.get(j).getScontrolname() + "_fileName");
									sCustomName = sCustomName.isEmpty() ? ""
											: sCustomName.concat(ssystemfilename.substring(
													ssystemfilename.lastIndexOf("."), ssystemfilename.length()));
									final String sActualFileName = fileMap
											.containsKey(controlCodeList.get(j).getScontrolname() + "_actualFileName")
													? (String) fileMap.get(controlCodeList.get(j).getScontrolname()
															+ "_actualFileName")
													: "";

									String sAttachedFile = "";
									String sCompressfilename = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
									String path = "";
									String ftpPhysicalPath = objFTPConfig.getSphysicalpath().toString()
											+ controlCodeList.get(j).getSsubfoldername() + "\\\\" + ssystemfilename;
									File fileSFTP = new File(ftpPhysicalPath);
									if (sCustomPath.isEmpty()) {
										path = filePath + sCompressfilename;
										DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss", Locale.getDefault());
										Date today = Calendar.getInstance().getTime();
										File file = new File(path);
										String reportDate = df.format(today);

										if (file.exists()) {
											sAttachedFile = objUserInfo.getSloginid() + "/" + sCompressfilename;
											nflagSftp = true;
											map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()), "true");
										} else {
											List<LsEntry> listFilesInSftpPath = channelftp.ls(".");
											nflagSftp = listFilesInSftpPath.stream().map(LsEntry::getFilename)
													.anyMatch(name -> name.equals(ssystemfilename));
											if (nflagSftp) {
												try {
													channelftp.get(ssystemfilename, path);
													LOGGER.info("File downloaded successfully !");
													map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()),
															"true");
												} catch (Exception e) {
													LOGGER.info("Error in downloading file !");
													map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()),
															"false");
												}
												sAttachedFile = objUserInfo.getSloginid() + "/" + sCompressfilename;
											} else {
												lstSystemFileName
														.add(sActualFileName != "" ? sActualFileName : ssystemfilename);
												map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()),
														"false");
											}
										}
									} else {
										path = sCustomPath + (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
										File file = new File(path);
										if (file.exists()) {
											sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
											nflagSftp = true;
											map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()), "true");
										} else {
											List<LsEntry> listFilesInSftpPath = channelftp.ls(".");
											nflagSftp = listFilesInSftpPath.stream().map(LsEntry::getFilename)
													.anyMatch(name -> name.equals(ssystemfilename));
											if (nflagSftp) {
												try {
													channelftp.get(ssystemfilename, path);
													LOGGER.info("File downloaded successfully !");
													map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()),
															"true");
												} catch (Exception e) {
													LOGGER.info("Error in downloading file !");
													map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()),
															"false");
												}
												sAttachedFile = (sCustomName.isEmpty() ? ssystemfilename : sCustomName);
											} else {
												lstSystemFileName
														.add(sActualFileName != "" ? sActualFileName : ssystemfilename);
												map.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()),
														"false");
											}
										}
									}

									if (nflagSftp == true) {
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
										map.put(controlCodeList.get(j).getScontrolname() + "_AttachFile",
												sAttachedFile);
										map.put(controlCodeList.get(j).getScontrolname() + "_FileName",
												sCompressfilename);
										map.put(controlCodeList.get(j).getScontrolname() + "_FilePath",
												// objReportsettings.getSsettingvalue() + sAttachedFile);
												filePath + sAttachedFile);
									}

								}
								// channelftp.cd("..");
							}
							if (!lstSystemFileName.isEmpty()) {
								String strAlert = lstSystemFileName.stream().map(x -> String.valueOf(x))
										.collect(Collectors.joining(","));
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										strAlert + " " + commonFunction.getMultilingualMessage(
												"IDS_FILENOTFOUNDINSFTPPATH", objUserInfo.getSlanguagefilename()));
							} else {
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
							}

						}
						jschSession.disconnect();
						channelftp.disconnect();

					}

				}
			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), // "IDS_CHECKFTPCONNECTION");
						commonFunction.getMultilingualMessage("IDS_CHECKFTPCONNECTION",
								objUserInfo.getSlanguagefilename()));
			}
		}

		return map;
	}

	public String multiPathDeleteFTPFile(final Map<String, Object> fileMap, final List<ControlMaster> controlCodeList,
			UserInfo userInfo) throws Exception {

		String returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		Settings objSettingsS3 = cloudStorageUtilityFunction.getSetting(userInfo);

		if (Integer.valueOf(objSettingsS3.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			returnStr = cloudStorageUtilityFunction.multiPathDeleteS3File(fileMap, controlCodeList, userInfo);
		} else {
			final String ftpQuery = "select * from ftpconfig where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final FTPConfig objFTPConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(ftpQuery, FTPConfig.class,
					jdbcTemplate);

			if (objFTPConfig != null) {
				String changedirectory = "";

				final String ftpPassWord = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
						objFTPConfig.getNftpno(), "spassword");
				if (objFTPConfig.getNsslrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) { // SSL
					try {
						FTPSClient ftpClient = new FTPSClient("SSL");
						ftpClient.setAuthValue("SSL");
						ftpClient.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
						boolean strue = ftpClient.login(objFTPConfig.getSusername(), ftpPassWord);

						if (strue) {
							for (int j = 0; j < controlCodeList.size(); j++) {

								changedirectory = controlCodeList.get(j).getSsubfoldername();
								ftpClient.changeWorkingDirectory("/" + changedirectory);
								changedirectory = changedirectory + "\\\\";

								if (fileMap.get(controlCodeList.get(j).getScontrolname() + "_fileName") != null) {

									final String sfileName = (String) fileMap
											.get(controlCodeList.get(j).getScontrolname() + "_fileName");
									ftpClient.deleteFile(sfileName);

									ftpClient.changeToParentDirectory();
								}
							}
						}
						ftpClient.logout();
						ftpClient.disconnect();
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
						returnStr = "IDS_CHECKFTPCONNECTION";
					}
				} else { // NON-SSL
					try {
						FTPClient ftpClient = new FTPClient();
						ftpClient.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
						boolean strue = ftpClient.login(objFTPConfig.getSusername(), ftpPassWord);
						if (strue) {
							for (int j = 0; j < controlCodeList.size(); j++) {

								changedirectory = controlCodeList.get(j).getSsubfoldername();
								ftpClient.changeWorkingDirectory("/" + changedirectory);
								changedirectory = changedirectory + "\\\\";

								if (fileMap.get(controlCodeList.get(j).getScontrolname() + "_fileName") != null) {

									final String sfileName = (String) fileMap
											.get(controlCodeList.get(j).getScontrolname() + "_fileName");
									ftpClient.deleteFile(sfileName);

									ftpClient.changeToParentDirectory();
								}
							}
						}
						ftpClient.logout();
						ftpClient.disconnect();
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
						returnStr = "IDS_CHECKFTPCONNECTION";
					}
				}
			} else {
				returnStr = "IDS_FTPNOTAVAILABLE";
			}
		}

		return returnStr;
	}

	public String deleteFTPFile(List<String> deleteList, String sChangeDirectory, UserInfo userInfo) throws Exception {
		String returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		Settings objSettingsS3 = cloudStorageUtilityFunction.getSetting(userInfo);
		if (Integer.valueOf(objSettingsS3.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			returnStr = cloudStorageUtilityFunction.deleteS3File(deleteList, sChangeDirectory, -1, userInfo);
		} else {
			final String ftpQuery = "select * from ftpconfig where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final FTPConfig objFTPConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(ftpQuery, FTPConfig.class,
					jdbcTemplate);
			if (objFTPConfig != null) {
				String ftpPassWord = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
						objFTPConfig.getNftpno(), "spassword");
				if (objFTPConfig.getNsslrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) { // SSL
					try {
						FTPSClient ftpClient = new FTPSClient("SSL");
						ftpClient.setAuthValue("SSL");
						ftpClient.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
						boolean strue = ftpClient.login(objFTPConfig.getSusername(), ftpPassWord);
						ftpClient.changeWorkingDirectory("/" + sChangeDirectory);
						if (strue) {
							for (String sfileName : deleteList) {
								ftpClient.deleteFile(sfileName);
							}
						}
						ftpClient.logout();
						ftpClient.disconnect();
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
						returnStr = "IDS_CHECKFTPCONNECTION";
					}
				} else { // NON-SSL
					try {
						FTPClient ftpClient = new FTPClient();
						ftpClient.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
						boolean strue = ftpClient.login(objFTPConfig.getSusername(), ftpPassWord);
						ftpClient.changeWorkingDirectory("/" + sChangeDirectory);
						if (strue) {
							for (String sfileName : deleteList) {
								ftpClient.deleteFile(sfileName);
							}
						}
						ftpClient.logout();
						ftpClient.disconnect();
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
						returnStr = "IDS_CHECKFTPCONNECTION";
					}
				}
			} else {
				returnStr = "IDS_FTPNOTAVAILABLE";
			}
		}
		return returnStr;
	}

	// FTP File Upload
	public String multiControlFTPFileUpload(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {
		String returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		Settings objSettingsS3 = cloudStorageUtilityFunction.getSetting(userInfo);
		if (Integer.valueOf(objSettingsS3.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			returnStr = cloudStorageUtilityFunction.multiControlS3FileUpload(request, userInfo);
		} else {
			final String ftpQuery = "select * from ftpconfig where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final FTPConfig objFTPConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(ftpQuery, FTPConfig.class,
					jdbcTemplate);

			if (objFTPConfig != null) {

				String uniquefilename = "";
				String localfile_checksumString = "";
				String ftpfile_checksumString = "";
				String changedirectory = "";

				final ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				final List<ControlMaster> controlCodeList = mapper.readValue(request.getParameter("controlcodelist"),
						new TypeReference<List<ControlMaster>>() {
						});
				String passWord = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
						objFTPConfig.getNftpno(), "spassword");
				FileOutputStream regFileWrite = null;// NIBSCRT-1910
				if (objFTPConfig.getNftptypecode() == 1) {

					if (objFTPConfig.getNsslrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						// SSL
						try {
							FTPSClient ftpClient = new FTPSClient("SSL");
							ftpClient.setAuthValue("SSL");
							ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
							ftpClient.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
							ftpClient.setBufferSize(1000);

							boolean strue = ftpClient.login(objFTPConfig.getSusername(), passWord);

							int reply = ftpClient.getReplyCode();
							if (!FTPReply.isPositiveCompletion(reply)) {
								returnStr = "IDS_CHECKFTPCONNECTION";
							}
							ftpClient.enterLocalPassiveMode();
							ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

							for (int j = 0; j < controlCodeList.size(); j++) {

								final int filecount = Integer.valueOf(
										request.getParameter(controlCodeList.get(j).getScontrolname() + "_filecount"));
								changedirectory = controlCodeList.get(j).getSsubfoldername();

								if (!changedirectory.isEmpty()) {
									final int ncwdcode = ftpClient.cwd(changedirectory);
									if (ncwdcode == 550) {
										boolean isDirectoryCreated = ftpClient.makeDirectory("/" + changedirectory);
										if (isDirectoryCreated) {
											System.out.println("Directory created successfully.");
										} else {
											System.out.println("Failed to create directory. See server's reply.");
											returnStr = "IDS_FAILEDTOCREATEDIRECTORY";
										}
										System.out.println("Directory Doesn't Exists");
									} else if (ncwdcode == 250) {
										System.out.println("Directory Exists");
									} else {
										System.out.println("Unknown Status");
									}
									ftpClient.changeWorkingDirectory("/" + changedirectory);
									changedirectory = changedirectory + "\\\\";
								} else {
									ftpClient.changeToParentDirectory();
								}

								if (strue) {
									for (int i = 0; i < filecount; i++) {
										MultipartFile objmultipart = request.getFile(
												controlCodeList.get(j).getScontrolname() + "_uploadedFile" + i);
										InputStream objinputstream = objmultipart.getInputStream();
										ByteArrayOutputStream baos = new ByteArrayOutputStream();
										byte[] buffer = new byte[1024];
										int len;
										while ((len = objinputstream.read(buffer)) > -1) {
											baos.write(buffer, 0, len);
										}
										baos.flush();
										InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
										InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
										uniquefilename = request.getParameter(
												controlCodeList.get(j).getScontrolname() + "_uniquefilename" + i);
										// boolean done = ftpClient.storeUniqueFile(uniquefilename, is1);
										ftpClient.storeFile(uniquefilename, is1);
										int needchecksum = objFTPConfig.getNchecksumrequired();
										if (needchecksum == 3) {

											MessageDigest md5Digest = null;
											try {
												md5Digest = MessageDigest.getInstance("MD5");
											} catch (NoSuchAlgorithmException e) {
												LOGGER.info(
														"=========================Invalid algorithm========================");
											}
											localfile_checksumString = getfileChecksum2(md5Digest, is2);
											String javahost = java.net.InetAddress.getLocalHost().toString();
											javahost = javahost.substring(javahost.indexOf("/") + 1);
											LOGGER.info("=========================javahost========================"
													+ javahost);
											String ftphost = ftpClient.getPassiveHost();
											LOGGER.info("=========================ftphost========================"
													+ ftphost);
											if (javahost.equalsIgnoreCase(ftphost)
													&& !objFTPConfig.getSphysicalpath().equals("")
													&& objFTPConfig.getSphysicalpath() != null
													&& !Paths.get(objFTPConfig.getSphysicalpath())
															.equals(Paths.get(System.getProperty("java.io.tmpdir")))) {

												File ftpfile = new File(objFTPConfig.getSphysicalpath()
														+ changedirectory + uniquefilename);
												LOGGER.info("======================ftpfile=================="
														+ objFTPConfig.getSphysicalpath() + changedirectory
														+ uniquefilename);
												ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
											} else {
												File ftpfile = new File(
														System.getProperty("java.io.tmpdir") + "/" + uniquefilename);
												regFileWrite = new FileOutputStream(ftpfile);
												ftpClient.retrieveFile(uniquefilename, regFileWrite);
												ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
											}
											LOGGER.info(
													"=========================localfile_checksumString========================"
															+ localfile_checksumString);
											LOGGER.info(
													"=========================ftpfile_checksumString=========================="
															+ ftpfile_checksumString);

											if (ftpfile_checksumString.equals(localfile_checksumString)) {

												LOGGER.info(
														"=====================UPLOADED SUCCESSFULLY====================="
																+ uniquefilename);
											} else {
												uniquefilename = Enumeration.ReturnStatus.FAILED.getreturnstatus();
												LOGGER.info("=====================FILE CORRUPTED====================="
														+ uniquefilename);
											}

										}
										objinputstream.close();
										is1.close();
										is2.close();
//												ftpClient.logout();
//												ftpClient.disconnect();
									}
								} else {
									returnStr = "IDS_CHECKFTPCONNECTION";
								}
								ftpClient.changeToParentDirectory();

							}
							ftpClient.logout();
							ftpClient.disconnect();
						} catch (Exception e) {
							e.getMessage();
							e.getCause();
							returnStr = "IDS_CHECKFTPCONNECTION";
						} finally {
							if (regFileWrite != null)// NIBSCRT-1910
								regFileWrite.close();
						}
					} else {// NON-SSL
						try {
							FTPClient ftpClient = new FTPClient();
							ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
							ftpClient.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
							ftpClient.setBufferSize(1000);
							boolean strue = ftpClient.login(objFTPConfig.getSusername(), passWord);
							boolean answer = ftpClient.sendNoOp();
							if (answer) {
								int reply = ftpClient.getReplyCode();
								if (!FTPReply.isPositiveCompletion(reply)) {
									returnStr = "IDS_CHECKFTPCONNECTION";
								}
								ftpClient.enterLocalPassiveMode();
								ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

								for (int j = 0; j < controlCodeList.size(); j++) {
									final int filecount = Integer.valueOf(request
											.getParameter(controlCodeList.get(j).getScontrolname() + "_filecount"));
									changedirectory = controlCodeList.get(j).getSsubfoldername();

									if (!changedirectory.isEmpty()) {
										final int ncwdcode = ftpClient.cwd(changedirectory);
										if (ncwdcode == 550) {
											boolean isDirectoryCreated = ftpClient.makeDirectory("/" + changedirectory);
											if (isDirectoryCreated) {
												System.out.println("Directory created successfully.");
											} else {
												System.out.println("Failed to create directory. See server's reply.");
												returnStr = "IDS_FAILEDTOCREATEDIRECTORY";
											}
											System.out.println("Directory Doesn't Exists");
										} else if (ncwdcode == 250) {
											System.out.println("Directory Exists");
										} else {
											System.out.println("Unknown Status");
										}
										ftpClient.changeWorkingDirectory("/" + changedirectory);
										changedirectory = changedirectory + "\\\\";
									} else {
										ftpClient.changeToParentDirectory();
									}
									if (strue) {
										for (int i = 0; i < filecount; i++) {
											MultipartFile objmultipart = request.getFile(
													controlCodeList.get(j).getScontrolname() + "_uploadedFile" + i);
											InputStream objinputstream = objmultipart.getInputStream();
											ByteArrayOutputStream baos = new ByteArrayOutputStream();
											byte[] buffer = new byte[1024];
											int len;
											while ((len = objinputstream.read(buffer)) > -1) {
												baos.write(buffer, 0, len);
											}
											baos.flush();
											InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
											InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
											uniquefilename = request.getParameter(
													controlCodeList.get(j).getScontrolname() + "_uniquefilename" + i);
											// ftpClient.storeUniqueFile(uniquefilename, is1);
											ftpClient.storeFile(uniquefilename, is1);
											if (objFTPConfig.getNchecksumrequired() == 3) {

												MessageDigest md5Digest = null;
												try {
													md5Digest = MessageDigest.getInstance("MD5");
												} catch (NoSuchAlgorithmException e) {
													LOGGER.info(
															"=========================Invalid algorithm========================");
												}
												localfile_checksumString = getfileChecksum2(md5Digest, is2);
												String javahost = java.net.InetAddress.getLocalHost().toString();
												javahost = javahost.substring(javahost.indexOf("/") + 1);
												LOGGER.info("=========================javahost========================"
														+ javahost);
												String ftphost = ftpClient.getPassiveHost();
												LOGGER.info("=========================ftphost========================"
														+ ftphost);
												if (javahost.equalsIgnoreCase(ftphost)
														&& !objFTPConfig.getSphysicalpath().equals("")
														&& objFTPConfig.getSphysicalpath() != null
														&& !Paths.get(objFTPConfig.getSphysicalpath()).equals(
																Paths.get(System.getProperty("java.io.tmpdir")))) {

													File ftpfile = new File(objFTPConfig.getSphysicalpath()
															+ changedirectory + uniquefilename);
													LOGGER.info("======================ftpfile=================="
															+ objFTPConfig.getSphysicalpath() + changedirectory
															+ uniquefilename);
													ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
												} else {
													File ftpfile = new File(System.getProperty("java.io.tmpdir") + "/"
															+ uniquefilename);
													regFileWrite = new FileOutputStream(ftpfile);
													ftpClient.retrieveFile(uniquefilename, regFileWrite);
													ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
												}
												LOGGER.info(
														"=========================localfile_checksumString========================"
																+ localfile_checksumString);
												LOGGER.info(
														"=========================ftpfile_checksumString=========================="
																+ ftpfile_checksumString);

												if (ftpfile_checksumString.equals(localfile_checksumString)) {

													LOGGER.info(
															"=====================UPLOADED SUCCESSFULLY====================="
																	+ uniquefilename);
												} else {
													uniquefilename = Enumeration.ReturnStatus.FAILED.getreturnstatus();
													LOGGER.info(
															"=====================FILE CORRUPTED====================="
																	+ uniquefilename);
												}

											}
											objinputstream.close();
											is1.close();
											is2.close();
										}
									} else {
										returnStr = "IDS_CHECKFTPCONNECTION";
									}

									ftpClient.changeToParentDirectory();

								}
								ftpClient.logout();
								ftpClient.disconnect();
							} else {
								returnStr = "IDS_SERVERTIMEOUT";
							}
						} catch (Exception e) {
							e.getMessage();
							e.getCause();
							returnStr = "IDS_CHECKFTPCONNECTION";
						} finally {
							if (regFileWrite != null)// NIBSCRT-1910
								regFileWrite.close();
						}
					}

				} else if (objFTPConfig.getNftptypecode() == 2) {

					Properties prop = new Properties();
					prop.put("StrictHostKeyChecking", "no");
					JSch jsch = new JSch();
					Session jschSession = jsch.getSession(objFTPConfig.getSusername(), objFTPConfig.getShost(),
							objFTPConfig.getNportno());
					jschSession.setPassword(passWord);
					jschSession.setConfig(prop);
					jschSession.connect();
					ChannelSftp channelftp = (ChannelSftp) jschSession.openChannel("sftp");
					channelftp.connect();
					boolean strue = jschSession.isConnected();

					for (int j = 0; j < controlCodeList.size(); j++) {
						final int filecount = Integer
								.valueOf(request.getParameter(controlCodeList.get(j).getScontrolname() + "_filecount"));
						changedirectory = controlCodeList.get(j).getSsubfoldername();

						if (!changedirectory.isEmpty()) {
							SftpATTRS attrs;
							try {
								attrs = channelftp.stat(changedirectory);
							} catch (Exception e) {
								channelftp.mkdir("/" + changedirectory);
							}
							channelftp.cd("/" + changedirectory);
							changedirectory = changedirectory + "\\\\";
						}
						if (strue) {
							for (int i = 0; i < filecount; i++) {
								MultipartFile objmultipart = request
										.getFile(controlCodeList.get(j).getScontrolname() + "_uploadedFile" + i);
								InputStream objinputstream = objmultipart.getInputStream();
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								byte[] buffer = new byte[1024];
								int len;
								while ((len = objinputstream.read(buffer)) > -1) {
									baos.write(buffer, 0, len);
								}
								baos.flush();
								InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
								InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
								uniquefilename = request
										.getParameter(controlCodeList.get(j).getScontrolname() + "_uniquefilename" + i);
								// ftpClient.storeUniqueFile(uniquefilename, is1);
								channelftp.put(is1, uniquefilename);
								if (objFTPConfig.getNchecksumrequired() == 3) {
									MessageDigest md5Digest = null;
									try {
										md5Digest = MessageDigest.getInstance("MD5");
									} catch (NoSuchAlgorithmException e) {
										LOGGER.info(
												"=========================Invalid algorithm========================");
									}
									localfile_checksumString = getfileChecksum2(md5Digest, is2);
									String javahost = java.net.InetAddress.getLocalHost().toString();
									javahost = javahost.substring(javahost.indexOf("/") + 1);
									LOGGER.info("=========================javahost========================" + javahost);
									String ftphost = jschSession.getHost();
									LOGGER.info("=========================ftphost========================" + ftphost);
									if (javahost.equalsIgnoreCase(ftphost)
											&& !objFTPConfig.getSphysicalpath().equals("")
											&& objFTPConfig.getSphysicalpath() != null
											&& !Paths.get(objFTPConfig.getSphysicalpath())
													.equals(Paths.get(System.getProperty("java.io.tmpdir")))) {

										File ftpfile = new File(
												objFTPConfig.getSphysicalpath() + changedirectory + uniquefilename);
										LOGGER.info("======================ftpfile=================="
												+ objFTPConfig.getSphysicalpath() + changedirectory + uniquefilename);
										ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
									} else {
										File ftpfile = new File(
												System.getProperty("java.io.tmpdir") + "/" + uniquefilename);
										regFileWrite = new FileOutputStream(ftpfile);
										channelftp.get(uniquefilename, regFileWrite);
										ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
									}
									LOGGER.info(
											"=========================localfile_checksumString========================"
													+ localfile_checksumString);
									LOGGER.info(
											"=========================ftpfile_checksumString=========================="
													+ ftpfile_checksumString);

									if (ftpfile_checksumString.equals(localfile_checksumString)) {

										LOGGER.info("=====================UPLOADED SUCCESSFULLY====================="
												+ uniquefilename);
									} else {
										uniquefilename = Enumeration.ReturnStatus.FAILED.getreturnstatus();
										LOGGER.info("=====================FILE CORRUPTED====================="
												+ uniquefilename);
									}

								}
								objinputstream.close();
								is1.close();
								is2.close();
							}
						} else {
							returnStr = "IDS_CHECKFTPCONNECTION";
						}
					}
					channelftp.disconnect();
					jschSession.disconnect();
				}
			} else {
				returnStr = "IDS_FTPNOTAVAILABLE";
			}
		}
		return returnStr;
	}

	// FTP File Upload
	public String getFileFTPUpload(MultipartHttpServletRequest request, int ncontrolcode, final UserInfo userInfo)
			throws Exception {
		String returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		Settings objSettingsS3 = cloudStorageUtilityFunction.getSetting(userInfo);

		final String subfolderquery = "select ssubfoldername,ncontrolcode from ftpsubfolder where nformcode="
				+ userInfo.getNformcode() + "  and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final List<FTPSubFolder> lstForm = jdbcTemplate.query(subfolderquery, new FTPSubFolder());

		if (Integer.valueOf(objSettingsS3.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {

			Map<String, Object> credentialDetails = new HashMap<>();

			String changedirectory = "";
			if (!lstForm.isEmpty()) {
				changedirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
						.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
			}
			credentialDetails.put("folder", changedirectory);
			String rtnStatus = cloudStorageUtilityFunction.getFileS3Upload(request, credentialDetails, userInfo);
			returnStr = rtnStatus;
		} else {
			String ftpQuery = "select * from ftpconfig where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final FTPConfig objFTPConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(ftpQuery, FTPConfig.class,
					jdbcTemplate);

			if (objFTPConfig != null) {

				String uniquefilename = "";
				String localfile_checksumString = "";
				String ftpfile_checksumString = "";
				String changedirectory = "";
				if (!lstForm.isEmpty()) {
					changedirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
							.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
				}
				final int filecount = Integer.parseInt(request.getParameter("filecount"));
				String passWord = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
						objFTPConfig.getNftpno(), "spassword");
				FileOutputStream regFileWrite = null;
				if (objFTPConfig.getNftptypecode() == 1) {

					if (objFTPConfig.getNsslrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {// SSL
						try {
							FTPSClient ftpClient = new FTPSClient("SSL");
							ftpClient.setAuthValue("SSL");
							ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
							ftpClient.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
							ftpClient.setBufferSize(1000);
							boolean strue = ftpClient.login(objFTPConfig.getSusername(), passWord);
							int reply = ftpClient.getReplyCode();
							if (!FTPReply.isPositiveCompletion(reply)) {
								returnStr = "IDS_CHECKFTPCONNECTION";
							}
							ftpClient.enterLocalPassiveMode();
							ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
							if (changedirectory != "") {
								final int ncwdcode = ftpClient.cwd(changedirectory);
								if (ncwdcode == 550) {
									boolean isDirectoryCreated = ftpClient.makeDirectory("/" + changedirectory);
									if (isDirectoryCreated) {
										System.out.println("Directory created successfully.");
									} else {
										System.out.println("Failed to create directory. See server's reply.");
										returnStr = "IDS_FAILEDTOCREATEDIRECTORY";
									}
									System.out.println("Directory Doesn't Exists");
								} else if (ncwdcode == 250) {
									System.out.println("Directory Exists");
								} else {
									System.out.println("Unknown Status");
								}
								ftpClient.changeWorkingDirectory("/" + changedirectory);
								changedirectory = changedirectory + "\\\\";
							}

							if (strue) {
								for (int i = 0; i < filecount; i++) {
									MultipartFile objmultipart = request.getFile("uploadedFile" + i);
									InputStream objinputstream = objmultipart.getInputStream();
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									byte[] buffer = new byte[1024];
									int len;
									while ((len = objinputstream.read(buffer)) > -1) {
										baos.write(buffer, 0, len);
									}
									baos.flush();
									InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
									InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
									uniquefilename = request.getParameter("uniquefilename" + i);
									ftpClient.storeFile(uniquefilename, is1);
									int needchecksum = objFTPConfig.getNchecksumrequired();
									if (needchecksum == 3) {

										MessageDigest md5Digest = null;
										try {
											md5Digest = MessageDigest.getInstance("MD5");
										} catch (NoSuchAlgorithmException e) {
											LOGGER.info(
													"=========================Invalid algorithm========================");
										}
										localfile_checksumString = getfileChecksum2(md5Digest, is2);
										String javahost = java.net.InetAddress.getLocalHost().toString();
										javahost = javahost.substring(javahost.indexOf("/") + 1);
										LOGGER.info(
												"=========================javahost========================" + javahost);
										String ftphost = ftpClient.getPassiveHost();
										LOGGER.info(
												"=========================ftphost========================" + ftphost);
										if (javahost.equalsIgnoreCase(ftphost)
												&& !objFTPConfig.getSphysicalpath().equals("")
												&& objFTPConfig.getSphysicalpath() != null
												&& !Paths.get(objFTPConfig.getSphysicalpath())
														.equals(Paths.get(System.getProperty("java.io.tmpdir")))) {

											File ftpfile = new File(
													objFTPConfig.getSphysicalpath() + changedirectory + uniquefilename);
											LOGGER.info("======================ftpfile=================="
													+ objFTPConfig.getSphysicalpath() + changedirectory
													+ uniquefilename);
											ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
										} else {
											File ftpfile = new File(
													System.getProperty("java.io.tmpdir") + "/" + uniquefilename);
											regFileWrite = new FileOutputStream(ftpfile);
											ftpClient.retrieveFile(uniquefilename, regFileWrite);
											ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
										}
										LOGGER.info(
												"=========================localfile_checksumString========================"
														+ localfile_checksumString);
										LOGGER.info(
												"=========================ftpfile_checksumString=========================="
														+ ftpfile_checksumString);

										if (ftpfile_checksumString.equals(localfile_checksumString)) {

											LOGGER.info(
													"=====================UPLOADED SUCCESSFULLY====================="
															+ uniquefilename);
										} else {
											uniquefilename = Enumeration.ReturnStatus.FAILED.getreturnstatus();
											LOGGER.info("=====================FILE CORRUPTED====================="
													+ uniquefilename);
										}

									}
									objinputstream.close();
									is1.close();
									is2.close();
									ftpClient.logout();
									ftpClient.disconnect();
								}
							} else {
								returnStr = "IDS_CHECKFTPCONNECTION";
							}
						} catch (Exception e) {
							e.getMessage();
							e.getCause();
							returnStr = "IDS_CHECKFTPCONNECTION";
						} finally {
							if (regFileWrite != null)
								regFileWrite.close();// NIBSCRT-1910
						}
					} else {// NON-SSL
						try {
							FTPClient ftpClient = new FTPClient();
							ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
							ftpClient.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
							ftpClient.setBufferSize(1000);
							boolean strue = ftpClient.login(objFTPConfig.getSusername(), passWord);
							boolean answer = ftpClient.sendNoOp();
							if (answer) {
								int reply = ftpClient.getReplyCode();
								if (!FTPReply.isPositiveCompletion(reply)) {
									returnStr = "IDS_CHECKFTPCONNECTION";
								}
								ftpClient.enterLocalPassiveMode();
								ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
								if (changedirectory != "" && !changedirectory.equals("")
										&& changedirectory.length() > 0) {
									final int ncwdcode = ftpClient.cwd(changedirectory);
									if (ncwdcode == 550) {
										// ALPD-4204 - fix for jboss
										boolean isDirectoryCreated = ftpClient.makeDirectory(changedirectory);
										if (isDirectoryCreated) {
											System.out.println("Directory created successfully.");
										} else {
											System.out.println("Failed to create directory. See server's reply.");
											returnStr = "IDS_FAILEDTOCREATEDIRECTORY";
										}
										System.out.println("Directory Doesn't Exists");
									} else if (ncwdcode == 250) {
										System.out.println("Directory Exists");
									} else {
										System.out.println("Unknown Status");
									}
									// ALPD-4204 - fix for jboss
									ftpClient.changeWorkingDirectory(changedirectory);
									changedirectory = changedirectory + File.separatorChar;
								}
								if (strue) {
									for (int i = 0; i < filecount; i++) {
										MultipartFile objmultipart = request.getFile("uploadedFile" + i);
										InputStream objinputstream = objmultipart.getInputStream();
										ByteArrayOutputStream baos = new ByteArrayOutputStream();
										byte[] buffer = new byte[1024];
										int len;
										while ((len = objinputstream.read(buffer)) > -1) {
											baos.write(buffer, 0, len);
										}
										baos.flush();
										InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
										InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
										uniquefilename = request.getParameter("uniquefilename" + i);
//										ftpClient.storeUniqueFile(uniquefilename, is1);
										ftpClient.storeFile(uniquefilename, is1);
										if (objFTPConfig.getNchecksumrequired() == 3) {

											MessageDigest md5Digest = null;
											try {
												md5Digest = MessageDigest.getInstance("MD5");
											} catch (NoSuchAlgorithmException e) {
												LOGGER.info(
														"=========================Invalid algorithm========================");
											}
											localfile_checksumString = getfileChecksum2(md5Digest, is2);
											String javahost = java.net.InetAddress.getLocalHost().toString();
											javahost = javahost.substring(javahost.indexOf("/") + 1);
											LOGGER.info("=========================javahost========================"
													+ javahost);
											String ftphost = ftpClient.getPassiveHost();
											LOGGER.info("=========================ftphost========================"
													+ ftphost);
											if (javahost.equalsIgnoreCase(ftphost)
													&& !objFTPConfig.getSphysicalpath().equals("")
													&& objFTPConfig.getSphysicalpath() != null
													&& !Paths.get(objFTPConfig.getSphysicalpath())
															.equals(Paths.get(System.getProperty("java.io.tmpdir")))) {

												File ftpfile = new File(objFTPConfig.getSphysicalpath()
														+ changedirectory + uniquefilename);
												LOGGER.info("======================ftpfile=================="
														+ objFTPConfig.getSphysicalpath() + changedirectory
														+ uniquefilename);
												ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
											} else {
												File ftpfile = new File(
														System.getProperty("java.io.tmpdir") + "/" + uniquefilename);
												regFileWrite = new FileOutputStream(ftpfile);
												ftpClient.retrieveFile(uniquefilename, regFileWrite);
												ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
											}
											LOGGER.info(
													"=========================localfile_checksumString========================"
															+ localfile_checksumString);
											LOGGER.info(
													"=========================ftpfile_checksumString=========================="
															+ ftpfile_checksumString);

											if (ftpfile_checksumString.equals(localfile_checksumString)) {

												LOGGER.info(
														"=====================UPLOADED SUCCESSFULLY====================="
																+ uniquefilename);
											} else {
												uniquefilename = Enumeration.ReturnStatus.FAILED.getreturnstatus();
												LOGGER.info("=====================FILE CORRUPTED====================="
														+ uniquefilename);
											}

										}
										objinputstream.close();
										is1.close();
										is2.close();
									}
									ftpClient.logout();
									ftpClient.disconnect();
								} else {
									returnStr = "IDS_CHECKFTPCONNECTION";
								}
							} else {
								returnStr = "IDS_SERVERTIMEOUT";
							}
						} catch (Exception e) {
							e.getMessage();
							e.getCause();
							returnStr = "IDS_CHECKFTPCONNECTION";
						} finally {
							if (regFileWrite != null)
								regFileWrite.close();// NIBSCRT-1910
						}
					}

				} else if (objFTPConfig.getNftptypecode() == 2) {

					Properties prop = new Properties();
					prop.put("StrictHostKeyChecking", "no");
					JSch jsch = new JSch();
					Session jschSession = jsch.getSession(objFTPConfig.getSusername(), objFTPConfig.getShost(),
							objFTPConfig.getNportno());
					jschSession.setPassword(passWord);
					jschSession.setConfig(prop);
					jschSession.connect();
					ChannelSftp channelftp = (ChannelSftp) jschSession.openChannel("sftp");
					channelftp.connect();
					boolean strue = jschSession.isConnected();

					if (changedirectory != "") {

						SftpATTRS attrs;
						try {
							attrs = channelftp.stat(changedirectory);
						} catch (Exception e) {
							channelftp.mkdir("/" + changedirectory);
						}
						channelftp.cd("/" + changedirectory);
						changedirectory = changedirectory + "\\\\";

					}
					if (strue) {
						for (int i = 0; i < filecount; i++) {
							MultipartFile objmultipart = request.getFile("uploadedFile" + i);
							InputStream objinputstream = objmultipart.getInputStream();
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							byte[] buffer = new byte[1024];
							int len;
							while ((len = objinputstream.read(buffer)) > -1) {
								baos.write(buffer, 0, len);
							}
							baos.flush();
							InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
							InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
							uniquefilename = request.getParameter("uniquefilename" + i);
							channelftp.put(is1, uniquefilename);
							if (objFTPConfig.getNchecksumrequired() == 3) {

								MessageDigest md5Digest = null;
								try {
									md5Digest = MessageDigest.getInstance("MD5");
								} catch (NoSuchAlgorithmException e) {
									LOGGER.info("=========================Invalid algorithm========================");
								}
								localfile_checksumString = getfileChecksum2(md5Digest, is2);
								String javahost = java.net.InetAddress.getLocalHost().toString();
								javahost = javahost.substring(javahost.indexOf("/") + 1);
								LOGGER.info("=========================javahost========================" + javahost);
								String sessionhost = jschSession.getHost();
								// String ftphost = ftpClient.getPassiveHost();
								LOGGER.info("=========================ftphost========================" + sessionhost);
								// javahost.equalsIgnoreCase(ftphost) &&
								if (javahost.equalsIgnoreCase(sessionhost)
										&& !objFTPConfig.getSphysicalpath().equals("")
										&& objFTPConfig.getSphysicalpath() != null
										&& !Paths.get(objFTPConfig.getSphysicalpath())
												.equals(Paths.get(System.getProperty("java.io.tmpdir")))) {

									File ftpfile = new File(
											objFTPConfig.getSphysicalpath() + changedirectory + uniquefilename);
									LOGGER.info("======================ftpfile=================="
											+ objFTPConfig.getSphysicalpath() + changedirectory + uniquefilename);
									ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
								} else {
									File ftpfile = new File(
											System.getProperty("java.io.tmpdir") + "/" + uniquefilename);
									regFileWrite = new FileOutputStream(ftpfile);
									channelftp.get(uniquefilename, regFileWrite);
									ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
								}
								LOGGER.info("=========================localfile_checksumString========================"
										+ localfile_checksumString);
								LOGGER.info("=========================ftpfile_checksumString=========================="
										+ ftpfile_checksumString);

								if (ftpfile_checksumString.equals(localfile_checksumString)) {

									LOGGER.info("=====================UPLOADED SUCCESSFULLY====================="
											+ uniquefilename);
								} else {
									uniquefilename = Enumeration.ReturnStatus.FAILED.getreturnstatus();
									LOGGER.info("=====================FILE CORRUPTED====================="
											+ uniquefilename);
								}

							}
							objinputstream.close();
							is1.close();
							is2.close();
						}
						channelftp.disconnect();
						jschSession.disconnect();
					} else {
						returnStr = "IDS_CHECKFTPCONNECTION";
					}

				}

			} else {
				returnStr = "IDS_FTPNOTAVAILABLE";
			}
		}
		return returnStr;
	}

	public String getfileChecksum(MessageDigest md5Digest, File ftpfile) throws FileNotFoundException, IOException {
		try (DigestInputStream dis = new DigestInputStream(new FileInputStream(ftpfile), md5Digest)) {
			while (dis.read() != -1)
				;
			md5Digest = dis.getMessageDigest();
		}
		StringBuilder checksum = new StringBuilder();
		for (byte b : md5Digest.digest()) {
			checksum.append(String.format("%02x", b));
		}
		return checksum.toString();
	}

	public String getfileChecksum2(MessageDigest md5Digest, InputStream objinputstream) throws IOException {
		try (DigestInputStream dis = new DigestInputStream(objinputstream, md5Digest)) {
			while (dis.read() != -1)
				;
			md5Digest = dis.getMessageDigest();
		}
		StringBuilder checksum = new StringBuilder();
		for (byte b : md5Digest.digest()) {
			checksum.append(String.format("%02x", b));
		}
		return checksum.toString();
	}

	public Map<String, Object> multiFileDownloadFromFTP(List<String> ssystemfilenames, int ncontrolcode,
			final UserInfo objUserInfo, final String sCustomPath, final List<String> customFileName) throws Exception {

		Map<String, Object> map = new HashMap<>();

		final List<FTPSubFolder> lstForm = (List<FTPSubFolder>) jdbcTemplate.query(
				" select ssubfoldername,ncontrolcode from ftpsubfolder where nformcode=" + objUserInfo.getNformcode()
						+ " and nsitecode=" + objUserInfo.getNmastersitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
				new FTPSubFolder());

		Settings objSettingsS3 = cloudStorageUtilityFunction.getSetting(objUserInfo);

		if (Integer.valueOf(objSettingsS3.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {

			String changeWorkingDirectory = "";
			if (!lstForm.isEmpty()) {
				changeWorkingDirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
						.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
			}
			cloudStorageUtilityFunction.multiFileDownloadAWSStorage(ssystemfilenames, sCustomPath,
					changeWorkingDirectory, objUserInfo);
		} else {
			final String ftpQuery = "select * from ftpconfig where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode = "
					+ objUserInfo.getNmastersitecode();
			final FTPConfig objFTPConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(ftpQuery, FTPConfig.class,
					jdbcTemplate);

			if (objFTPConfig != null) {

				String changeWorkingDirectory = "";

				if (!lstForm.isEmpty()) {
					// Commented because it should not take the directory without controlcode
					// if (lstForm.size() == 1) {
					// changeWorkingDirectory = lstForm.get(0).getSsubfoldername();
					// } else {
					changeWorkingDirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
							.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
					// changedirectory=(String) lstString.get(0);
					// }
				}

				final String sQuery = "select ssettingvalue from settings where nsettingcode = "
						+ Enumeration.Settings.FILE_VIEW_SOURCE.getNsettingcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Settings objsettings = (Settings) jdbcUtilityFunction.queryForObject(sQuery, Settings.class,
						jdbcTemplate);
				// if ssettingstring=3 then file download in sharefolder by creating user
				// loginid as folder name
				// else if ssettingstring=4 then file download from ftp.
				if (objsettings.getSsettingvalue()
						.equals(Enumeration.TransactionStatus.YES.gettransactionstatus() + "")) {
					if (objFTPConfig.getNsslrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) { // SSL
						try {
							String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
									objFTPConfig.getNftpno(), "spassword");
							FTPSClient ftp = new FTPSClient("SSL");
							ftp.setAuthValue("SSL");
							ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
							ftp.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
							ftp.setBufferSize(1000);
							boolean ftpFile = ftp.login(objFTPConfig.getSusername(), RtnPasswordEncrypt);
							ftp.enterLocalPassiveMode();
							ftp.setFileType(FTP.BINARY_FILE_TYPE);
							if (ftpFile) {
								String absolutePath1 = getFTPFileWritingPath(sCustomPath, objUserInfo);
								File file1 = new File(absolutePath1);
								try {
									if (file1.exists() == false) {
										file1.mkdir();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								String sAttachedFile = "";
								String uniquefilename = "";
								String path = "";
								String sCompressfilename = "";
								if (sCustomPath.isEmpty()) {
									DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss", Locale.getDefault());
									Date today = Calendar.getInstance().getTime();
									String reportDate = df.format(today);
									for (String ssystemfilename : ssystemfilenames) {
										sCompressfilename = ssystemfilename;
										path = absolutePath1 + sCompressfilename;
										File file = new File(path);
										if (file.exists()) {
											sCompressfilename = "";
											sCompressfilename = reportDate.substring(0, 14) + "-" + ssystemfilename;
											path = absolutePath1 + sCompressfilename;
											file = new File(path);
										}
										FileOutputStream regFileWrite = new FileOutputStream(file);
										boolean download = ftp.retrieveFile(ssystemfilename, regFileWrite);
										if (download) {
											System.out.println(ssystemfilename + "File downloaded successfully !");
										} else {
											System.out.println(ssystemfilename + "Error in downloading file !");
											LOGGER.info(ssystemfilename + "Error in downloading file !");
										}
										regFileWrite.flush();
										regFileWrite.close();
										sAttachedFile = objUserInfo.getSloginid() + "/" + sCompressfilename;
									}
									ftp.logout();
									map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
									sAttachedFile = objUserInfo.getSloginid() + "/" + uniquefilename;
								} else {

									if (changeWorkingDirectory != "" && changeWorkingDirectory != ","
											&& !changeWorkingDirectory.equals("") && !changeWorkingDirectory.equals(",")
											&& changeWorkingDirectory.length() > 1) {
										LOGGER.info("Change Working Directory CWD........." + changeWorkingDirectory);
										ftp.changeWorkingDirectory(changeWorkingDirectory);
										LOGGER.info("Printing Change Working Directory CWD........."
												+ ftp.printWorkingDirectory());
									}
									for (int i = 0; i < ssystemfilenames.size(); i++) {
										String ssystemfilename = ssystemfilenames.get(i);
										String downloadedFileName = customFileName.get(i) != null
												&& customFileName.get(i).isEmpty() ? ssystemfilenames.get(i)
														: customFileName.get(i);
										path = sCustomPath + downloadedFileName;
										File file = new File(path);
										FileOutputStream regFileWrite = new FileOutputStream(file);
										boolean download = ftp.retrieveFile(ssystemfilename, regFileWrite);
										if (download) {
											System.out.println(ssystemfilename + "File downloaded successfully !");
										} else {
											System.out.println(ssystemfilename + "Error in downloading file !");
											LOGGER.info(ssystemfilename + "Error in downloading file !");
										}
										regFileWrite.flush();
										regFileWrite.close();
										sAttachedFile = ssystemfilename;
									}
								}
								ftp.logout();
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
								map.put("AttachFile", sAttachedFile);
								map.put("FileName", uniquefilename);
								map.put("FilePath", path);
							} else {
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										"IDS_CHECKFTPCONNECTION");
							}
						} catch (Exception e) {
							LOGGER.info("Wrong FTP Credentials --- > " + e.getMessage());
							LOGGER.info("Wrong FTP Credentials --- > " + e.getCause());
							map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_CHECKFTPCONNECTION");
						}
					} else { // Non SSL
						try {
							String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
									objFTPConfig.getNftpno(), "spassword");
							FTPClient ftp = new FTPClient();
							ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
							ftp.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
							ftp.setBufferSize(1000);
							boolean ftpFile = ftp.login(objFTPConfig.getSusername(), RtnPasswordEncrypt);
							ftp.enterLocalPassiveMode();
							ftp.setFileType(FTP.BINARY_FILE_TYPE);
							if (ftpFile) {
								String absolutePath1 = getFTPFileWritingPath(sCustomPath, objUserInfo);
								File file1 = new File(absolutePath1);
								try {
									if (file1.exists() == false) {
										file1.mkdir();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								String sAttachedFile = "";

								String path = "";
								String sCompressfilename = "";
								if (sCustomPath.isEmpty()) {

									DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss", Locale.getDefault());
									Date today = Calendar.getInstance().getTime();
									String reportDate = df.format(today);
									if (changeWorkingDirectory != "" && changeWorkingDirectory != ","
											&& !changeWorkingDirectory.equals("") && !changeWorkingDirectory.equals(",")
											&& changeWorkingDirectory.length() > 1) {
										LOGGER.info("Change Working Directory CWD........." + changeWorkingDirectory);
										ftp.changeWorkingDirectory(changeWorkingDirectory);
										LOGGER.info("Printing Change Working Directory CWD........."
												+ ftp.printWorkingDirectory());
									}
									for (String ssystemfilename : ssystemfilenames) {
										sCompressfilename = ssystemfilename;
										path = absolutePath1 + sCompressfilename;
										File file = new File(path);
										if (file.exists()) {
											sCompressfilename = "";
											sCompressfilename = reportDate.substring(0, 14) + "-" + ssystemfilename;
											path = absolutePath1 + sCompressfilename;
											file = new File(path);
										}
										FileOutputStream regFileWrite = new FileOutputStream(file);
										boolean download = ftp.retrieveFile(ssystemfilename, regFileWrite);
										if (download) {
											System.out.println(ssystemfilename + "File downloaded successfully !");
										} else {
											System.out.println(ssystemfilename + "Error in downloading file !");
											LOGGER.info(ssystemfilename + "Error in downloading file !");
										}
										regFileWrite.flush();
										regFileWrite.close();
										sAttachedFile = objUserInfo.getSloginid() + "/" + sCompressfilename;
									}

								} else {

									if (changeWorkingDirectory != "" && changeWorkingDirectory != ","
											&& !changeWorkingDirectory.equals("") && !changeWorkingDirectory.equals(",")
											&& changeWorkingDirectory.length() > 1) {
										LOGGER.info("Change Working Directory CWD........." + changeWorkingDirectory);
										ftp.changeWorkingDirectory(changeWorkingDirectory);
										LOGGER.info("Printing Change Working Directory CWD........."
												+ ftp.printWorkingDirectory());
									}
									for (int i = 0; i < ssystemfilenames.size(); i++) {
										String ssystemfilename = ssystemfilenames.get(i);
										String downloadedFileName = customFileName.get(i) != null
												&& customFileName.get(i).isEmpty() ? ssystemfilenames.get(i)
														: customFileName.get(i);
										path = sCustomPath + downloadedFileName;
										File file = new File(path);
										FileOutputStream regFileWrite = new FileOutputStream(file);
										boolean download = ftp.retrieveFile(ssystemfilename, regFileWrite);
										if (download) {
											System.out.println(ssystemfilename + "File downloaded successfully !");
										} else {
											System.out.println(ssystemfilename + "Error in downloading file !");
											LOGGER.info(ssystemfilename + "Error in downloading file !");
										}
										regFileWrite.flush();
										regFileWrite.close();
										sAttachedFile = ssystemfilename;
									}

								}
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
								ftp.logout();
								map.put("AttachFile", sAttachedFile);
								map.put("FileName", sCompressfilename);
								map.put("FilePath", path);
							} else {
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										"IDS_CHECKFTPCONNECTION");
							}
						} catch (Exception e) {
							LOGGER.info("Wrong FTP Credentials --- > " + e.getMessage());
							LOGGER.info("Wrong FTP Credentials --- > " + e.getCause());
							map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_CHECKFTPCONNECTION");
						}
					}
				}
				// else
				// if(objsettings.getSsettingvalue().equals(Enumeration.TransactionStatus.NO.gettransactionstatus()+"")){
				// map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				// Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				// String viewfilepath="ftp:\\"+objFTPConfig.getShost()+"\\"+ssystemfilename;
				// map.put("AttachFtp",viewfilepath);
				// }
			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_CHECKFTPCONNECTION");
			}
		}

		return map;
	}

	@SuppressWarnings("unused")
	public Map<String, Object> FileUploadaAndDownloadinFtp(String pdfPath, String outFileName, String customFileName,
			int ncontrolcode, final UserInfo objUserInfo) throws Exception {

		Settings objSettingsS3 = cloudStorageUtilityFunction.getSetting(objUserInfo);

		final List<FTPSubFolder> lstForm = (List<FTPSubFolder>) jdbcTemplate.query(
				" select ssubfoldername,ncontrolcode from ftpsubfolder" + " where nformcode="
						+ objUserInfo.getNformcode() + " and nsitecode=" + objUserInfo.getNmastersitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				new FTPSubFolder());

		String sQueryReport = "select ssettingvalue from reportsettings " + " where nreportsettingcode = "
				+ Enumeration.ReportSettings.REPORT_DOWNLOAD_URL.getNreportsettingcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final String fileDownloadURL = jdbcTemplate.queryForObject(sQueryReport, String.class);

		if (Integer.valueOf(objSettingsS3.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			Map<String, Object> map = new HashMap<>();
			String changeWorkingDirectory = "";
			if (!lstForm.isEmpty()) {
				changeWorkingDirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
						.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
			}
			map = cloudStorageUtilityFunction.fileUploadAndDownloadInS3(pdfPath, outFileName, customFileName,
					changeWorkingDirectory, fileDownloadURL, ncontrolcode, objUserInfo);
			return map;
		} else {
			String ftpfile_checksumString = "";
			String localfile_checksumString = "";
			String file = pdfPath + outFileName;
			customFileName = customFileName.isEmpty() ? customFileName
					: customFileName.contains(".pdf") ? customFileName : customFileName + ".pdf";
			String downloadFile = customFileName.length() > 0 ? pdfPath + customFileName : pdfPath + outFileName;
			File files = new File(file);
			Map<String, Object> map = new HashMap<>();

			final String ftpQuery = "select * from ftpconfig" + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode = "
					+ objUserInfo.getNmastersitecode();

			final FTPConfig objFTPConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(ftpQuery, FTPConfig.class,
					jdbcTemplate);
			if (objFTPConfig != null) {
//				final List<FTPSubFolder> lstForm = (List<FTPSubFolder>) jdbcTemplate.query(
//								" select ssubfoldername,ncontrolcode from ftpsubfolder" 
//								+ " where nformcode="	+ objUserInfo.getNformcode() 
//								+ " and nsitecode=" + objUserInfo.getNmastersitecode()
//								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//								new FTPSubFolder());
				String changeWorkingDirectory = "";
				if (!lstForm.isEmpty()) {
					// Commented because it should not take the directory without controlcode
					// if (lstForm.size() == 1) {
					// changeWorkingDirectory = lstForm.get(0).getSsubfoldername();
					// } else {
					changeWorkingDirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
							.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
					// }
				}

				String sQuery = "select ssettingvalue from settings where nsettingcode =  "
						+ Enumeration.Settings.FILE_VIEW_SOURCE.getNsettingcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final int ftpView = jdbcTemplate.queryForObject(sQuery, Integer.class);

				if (ftpView == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

//					sQuery = "select ssettingvalue from reportsettings " 
//							+ " where nreportsettingcode = "+ Enumeration.ReportSettings.REPORT_DOWNLOAD_URL.getNreportsettingcode() 
//							+ " and nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//					final String fileDownloadURL = jdbcTemplate.queryForObject(sQuery, String.class);

					FileOutputStream regFileOut = null;// NIBSCRT-1910
					InputStream regFileWrite = null;// NIBSCRT-1910

					if (objFTPConfig.getNsslrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) { // SSL
						try {
							String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
									objFTPConfig.getNftpno(), "spassword");
							FTPSClient ftp = new FTPSClient("SSL");
							ftp.setAuthValue("SSL");
							ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
							ftp.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
							ftp.setBufferSize(1000);
							boolean ftpFile = ftp.login(objFTPConfig.getSusername(), RtnPasswordEncrypt);
							ftp.enterLocalPassiveMode();
							ftp.setFileType(FTP.BINARY_FILE_TYPE);
							if (ftpFile) {

								if (changeWorkingDirectory != "") {
									final int ncwdcode = ftp.cwd(changeWorkingDirectory);
									if (ncwdcode == 550) {
										LOGGER.info("Directory Doesn't Exists");
										// boolean isDirectoryCreated = ftp.makeDirectory("/" + changeWorkingDirectory);
										boolean isDirectoryCreated = ftp.makeDirectory(changeWorkingDirectory);
										if (isDirectoryCreated) {
											LOGGER.info("Directory created successfully.");
										} else {
											LOGGER.info("Failed to create directory. See server's reply.");
										}
									} else if (ncwdcode == 250) {
										LOGGER.info("Directory Exists");
									} else {
										LOGGER.info("Unknown Status");
									}
									ftp.changeWorkingDirectory(changeWorkingDirectory);
									changeWorkingDirectory = changeWorkingDirectory + File.separatorChar;
								}
								regFileWrite = new FileInputStream(file);
								// boolean download = ftp.storeUniqueFile(outFileName, regFileWrite);
								boolean upload = ftp.storeFile(outFileName, regFileWrite);
								int needchecksum = objFTPConfig.getNchecksumrequired();
								if (needchecksum == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

									MessageDigest md5Digest = null;
									try {
										md5Digest = MessageDigest.getInstance("MD5");
									} catch (NoSuchAlgorithmException e) {
										LOGGER.info(
												"=========================Invalid algorithm========================");
									}
									localfile_checksumString = getfileChecksum(md5Digest, files);
									String javahost = java.net.InetAddress.getLocalHost().toString();
									javahost = javahost.substring(javahost.indexOf("/") + 1);
									LOGGER.info("=========================javahost========================" + javahost);
									String ftphost = ftp.getPassiveHost();
									LOGGER.info("=========================ftphost========================" + ftphost);
									if (javahost.equalsIgnoreCase(ftphost)
											&& !objFTPConfig.getSphysicalpath().equals("")
											&& objFTPConfig.getSphysicalpath() != null
											&& !Paths.get(objFTPConfig.getSphysicalpath())
													.equals(Paths.get(System.getProperty("java.io.tmpdir")))) {

										File ftpfile = new File(
												objFTPConfig.getSphysicalpath() + changeWorkingDirectory + outFileName);
										LOGGER.info("======================ftpfile=================="
												+ objFTPConfig.getSphysicalpath() + changeWorkingDirectory
												+ outFileName);
										ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
									} else {
										File ftpfile = new File(
												System.getProperty("java.io.tmpdir") + "/" + outFileName);
										regFileOut = new FileOutputStream(ftpfile);
										ftp.retrieveFile(outFileName, regFileOut);
										ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
									}
									LOGGER.info(
											"=========================localfile_checksumString========================"
													+ localfile_checksumString);
									LOGGER.info(
											"=========================ftpfile_checksumString=========================="
													+ ftpfile_checksumString);

									if (ftpfile_checksumString.equals(localfile_checksumString)) {

										LOGGER.info("=====================UPLOADED SUCCESSFULLY====================="
												+ outFileName);
									} else {
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												"IDS_FILECORRUPTED");
										LOGGER.info("=====================FILE CORRUPTED====================="
												+ outFileName);
										return map;
									}

								}
								File downloadedReport = new File(downloadFile);
								FileOutputStream regFileRead = new FileOutputStream(downloadedReport);
								boolean download = ftp.retrieveFile(outFileName, regFileRead);
								if (upload && download) {
									LOGGER.info("File downloaded successfully !");
								} else {
									LOGGER.info("Error in downloading file !");
								}
								regFileRead.flush();
								regFileRead.close();
								// map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								// Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
								map.put("filepath",
										fileDownloadURL + (customFileName.isEmpty() ? outFileName : customFileName));

								regFileWrite.close();
								ftp.logout();
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
							} else {
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										"IDS_CHECKFTPCONNECTION");
							}
						} catch (Exception e) {
							LOGGER.info("Wrong FTP Credentials --- > " + e.getMessage());
							LOGGER.info("Wrong FTP Credentials --- > " + e.getCause());
							map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_CHECKFTPCONNECTION");
						} finally {
							// NIBSCRT-1910
							if (regFileOut != null)
								regFileOut.close();
							if (regFileWrite != null)
								regFileWrite.close();
						}
					} else { // Non SSL
						try {
							String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
									objFTPConfig.getNftpno(), "spassword");
							FTPClient ftp = new FTPClient();
							ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
							ftp.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
							ftp.setBufferSize(1000);
							boolean ftpFile = ftp.login(objFTPConfig.getSusername(), RtnPasswordEncrypt);
							ftp.enterLocalPassiveMode();
							ftp.setFileType(FTP.BINARY_FILE_TYPE);
							if (ftpFile) {
								if (changeWorkingDirectory != "") {
									final int ncwdcode = ftp.cwd(changeWorkingDirectory);
									if (ncwdcode == 550) {
										LOGGER.info("Directory Doesn't Exists");
										boolean isDirectoryCreated = ftp.makeDirectory(changeWorkingDirectory);
										if (isDirectoryCreated) {
											LOGGER.info("Directory created successfully.");
										} else {
											LOGGER.info("Failed to create directory. See server's reply.");
										}
									} else if (ncwdcode == 250) {
										LOGGER.info("Directory Exists");
									} else {
										LOGGER.info("Unknown Status");
									}
									ftp.changeWorkingDirectory(changeWorkingDirectory);
									changeWorkingDirectory = changeWorkingDirectory + File.separatorChar;
								}
								regFileWrite = new FileInputStream(files);
								boolean upload = ftp.storeFile(outFileName, regFileWrite);
								int needchecksum = objFTPConfig.getNchecksumrequired();
								if (needchecksum == 3) {

									MessageDigest md5Digest = null;
									try {
										md5Digest = MessageDigest.getInstance("MD5");
									} catch (NoSuchAlgorithmException e) {
										LOGGER.info(
												"=========================Invalid algorithm========================");
									}
									localfile_checksumString = getfileChecksum(md5Digest, files);
									String javahost = java.net.InetAddress.getLocalHost().toString();
									javahost = javahost.substring(javahost.indexOf("/") + 1);
									LOGGER.info("=========================javahost========================" + javahost);
									String ftphost = ftp.getPassiveHost();
									LOGGER.info("=========================ftphost========================" + ftphost);
									if (javahost.equalsIgnoreCase(ftphost)
											&& !objFTPConfig.getSphysicalpath().equals("")
											&& objFTPConfig.getSphysicalpath() != null
											&& !Paths.get(objFTPConfig.getSphysicalpath())
													.equals(Paths.get(System.getProperty("java.io.tmpdir")))) {

										File ftpfile = new File(
												objFTPConfig.getSphysicalpath() + changeWorkingDirectory + outFileName);
										LOGGER.info("======================ftpfile=================="
												+ objFTPConfig.getSphysicalpath() + changeWorkingDirectory
												+ outFileName);
										ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
									} else {
										File ftpfile = new File(
												System.getProperty("java.io.tmpdir") + "/" + outFileName);
										regFileOut = new FileOutputStream(ftpfile);
										ftp.retrieveFile(outFileName, regFileOut);
										ftpfile_checksumString = getfileChecksum(md5Digest, ftpfile);
									}
									LOGGER.info(
											"=========================localfile_checksumString========================"
													+ localfile_checksumString);
									LOGGER.info(
											"=========================ftpfile_checksumString=========================="
													+ ftpfile_checksumString);

									if (ftpfile_checksumString.equals(localfile_checksumString)) {

										LOGGER.info("=====================UPLOADED SUCCESSFULLY====================="
												+ outFileName);
									} else {
										map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
												"IDS_FILECORRUPTED");
										LOGGER.info("=====================FILE CORRUPTED====================="
												+ outFileName);
										return map;
									}

								}
								File downloadedReport = new File(downloadFile);
								FileOutputStream regFileRead = new FileOutputStream(downloadedReport);
								boolean download = ftp.retrieveFile(outFileName, regFileRead);

								regFileRead.flush();
								regFileRead.close();
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
								map.put("filepath",
										fileDownloadURL + (customFileName.isEmpty() ? outFileName : (customFileName)));

								regFileWrite.close();

								if (download) {
									LOGGER.info("File downloaded successfully !");
									LOGGER.info("Working Directory==>" + ftp.printWorkingDirectory());
								} else {
									LOGGER.info("Working Directory==>" + ftp.printWorkingDirectory());
									LOGGER.info("Error in downloading file !");
								}
								ftp.logout();
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
							} else {
								map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										"IDS_CHECKFTPCONNECTION");
							}
						} catch (Exception e) {
							LOGGER.info("Wrong FTP Credentials --- > " + e.getMessage());
							LOGGER.info("Wrong FTP Credentials --- > " + e.getCause());
							map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_CHECKFTPCONNECTION");
						} finally {
							// NIBSCRT-1910
							if (regFileOut != null)
								regFileOut.close();
							if (regFileWrite != null)
								regFileWrite.close();
						}
					}
				} else if (ftpView == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					String viewfilepath = "ftp:\\" + objFTPConfig.getShost() + "\\" + outFileName;
					map.put("AttachFtp", viewfilepath);
				}
			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_CHECKFTPCONNECTION");
			}
			// it is used to delete uploaded pdf file with unique name.
			if (customFileName.length() > 0 && !customFileName.equals(outFileName)) {
				files.delete();
			}
			LOGGER.info("FileUploadaAndDownloadinFtp.map --- > " + map);
			return map;
		}

	}

	public void deleteFileStorage(String systemFileName, int ncontrolcode, UserInfo userInfo) throws Exception {
		final String subfolderquery = "select ssubfoldername,ncontrolcode from ftpsubfolder where nformcode="
				+ userInfo.getNformcode() + "  and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final List<FTPSubFolder> lstForm = jdbcTemplate.query(subfolderquery, new FTPSubFolder());

		Settings objSettingsS3 = cloudStorageUtilityFunction.getSetting(userInfo);
		if (Integer.valueOf(objSettingsS3.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			String changedirectory = "";
			if (!lstForm.isEmpty()) {
				changedirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
						.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
			}
			cloudStorageUtilityFunction.deleteFileAWSStorage(systemFileName, changedirectory, -1, userInfo);
		}
	}

}
