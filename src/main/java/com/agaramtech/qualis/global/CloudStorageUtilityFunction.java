package com.agaramtech.qualis.global;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.AWSStorageConfig;
import com.agaramtech.qualis.configuration.model.FTPSubFolder;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RequiredArgsConstructor
@Component
public class CloudStorageUtilityFunction {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloudStorageUtilityFunction.class);

	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final JdbcTemplate jdbcTemplate;

	public Map<String, Object> getAWSClientBucket(final UserInfo userInfo) throws Exception {

		final Map<String, Object> objMap = new HashMap<>();
		final AWSStorageConfig objAWSStorageConfig = getAWSStorageCredential(userInfo);

		final StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials
				.create(objAWSStorageConfig.getSaccesskeyid(), objAWSStorageConfig.getSsecretpasskey()));
		final S3Client s3 = S3Client.builder().region((Region) Region.of(objAWSStorageConfig.getSregion()))
				.credentialsProvider(credentialsProvider).build();

		objMap.put("bucketName", objAWSStorageConfig.getSbucketname());
		objMap.put("s3", s3);

		return objMap;
	}

	public AWSStorageConfig getAWSStorageCredential(final UserInfo userInfo) throws Exception {

		final String strAWSCredentials = "select * from awsstorageconfig where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus();
		return (AWSStorageConfig) jdbcUtilityFunction.queryForObject(strAWSCredentials, AWSStorageConfig.class,
				jdbcTemplate);

	}

	public Settings getSetting(final UserInfo userInfo) throws Exception {
		final String strSettings = "select nsettingcode, ssettingname, ssettingvalue from settings where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsettingcode="
				+ Enumeration.Settings.NEEDS3STORAGE.getNsettingcode();
		return (Settings) jdbcUtilityFunction.queryForObject(strSettings, Settings.class, jdbcTemplate);
	}

	public String getFileAbsolutePath() throws Exception {
		final String homePathQuery = "select ssettingvalue from settings where nsettingcode ="
				+ Enumeration.Settings.DEPLOYMENTSERVER_HOMEPATH.getNsettingcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final String homePath = (String) jdbcUtilityFunction.queryForObject(homePathQuery, String.class, jdbcTemplate);

		return homePath;
	}

	public Map<String, Object> fileViewAWSStorage(final String systemFileName, final String scustomPath,
			final String subFolder, final UserInfo userInfo) throws Exception {

		Map<String, Object> mapObj = new HashMap<>();
		Path downloadPath = null;
		String downloadsPath1 = Enumeration.FTP.FILE_PATH_TO_UPLOAD.getFTP();
		Path downloadsDir = null;
		if (!scustomPath.isEmpty()) {

			final File folderCheck = new File(scustomPath);
			if (!folderCheck.exists()) {

				final boolean created = folderCheck.mkdirs();
				if (created) {
					LOGGER.info("Folder created: " + folderCheck.getAbsolutePath());
				} else {
					LOGGER.info("Failed to create folder.");
				}

			}
			downloadsDir = Paths.get(scustomPath);
			downloadPath = downloadsDir.resolve(systemFileName);
			downloadsPath1 = downloadsDir.resolve(systemFileName).toString();

		} else {

			final String homePath = getFileAbsolutePath();
			final String downloadsPath = System.getenv(homePath) + "\\" + Enumeration.FTP.DOWNLOAD_PATH.getFTP();
			downloadsDir = Paths.get(downloadsPath);
			final File downloadsFolder = new File(downloadsPath);

			if (!downloadsFolder.exists()) {

				final boolean created = downloadsFolder.mkdirs();
				if (created) {
					System.out.println("Folder created: " + downloadsFolder.getAbsolutePath());
				} else {
					System.err.println("Failed to create folder.");
				}

			}
			downloadPath = downloadsDir.resolve(systemFileName);
			downloadsPath1 = downloadsPath1 + "/" + systemFileName;

		}

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		final String subFolderPath = (!subFolder.isEmpty()) ? (subFolder + "/") : "";

		if (Files.exists(downloadPath)) {
			Files.delete(downloadPath);
		}

		try {
			final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName)
					.key(subFolderPath + systemFileName).build();

			s3.getObject(getObjectRequest, downloadPath);
			mapObj.put("AttachFile", systemFileName);
			mapObj.put("FilePath", downloadsPath1);
			mapObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		} catch (Exception e) {
			LOGGER.info("Failed to download");
			mapObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.FAILED.getreturnstatus());
		}

		return mapObj;

	}

	public void multiFileDownloadAWSStorage(final List<String> systemFileNames, final String scustomPath,
			final String subFolder, final UserInfo userInfo) throws Exception {

		Path downloadsDir = null;
		if (!scustomPath.isEmpty()) {

			final File folderCheck = new File(scustomPath);
			if (!folderCheck.exists()) {

				final boolean created = folderCheck.mkdirs();
				if (created) {
					LOGGER.info("Folder created: " + folderCheck.getAbsolutePath());
				} else {
					LOGGER.info("Failed to create folder.");
				}

			}
			downloadsDir = Paths.get(scustomPath);

		} else {

			final String homePath = getFileAbsolutePath();
			final String downloadsPath = System.getenv(homePath) + "\\" + Enumeration.FTP.DOWNLOAD_PATH.getFTP();
			downloadsDir = Paths.get(downloadsPath);

			final File downloadsFolder = new File(downloadsPath);

			if (!downloadsFolder.exists()) {

				final boolean created = downloadsFolder.mkdirs();
				if (created) {
					LOGGER.info("Folder created: " + downloadsFolder.getAbsolutePath());
				} else {
					LOGGER.info("Failed to create folder.");
				}
			}
		}

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		final String subFolderPath = (!subFolder.isEmpty()) ? (subFolder + "/") : "";

		String strSystemFilesCreated = "";
		try {
			for (String systemFileName : systemFileNames) {

				final Path downloadFilePath = downloadsDir.resolve(systemFileName);

				if (Files.exists(downloadFilePath)) {
					Files.delete(downloadFilePath);
				}

				final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName)
						.key(subFolderPath + systemFileName).build();

				s3.getObject(getObjectRequest, downloadFilePath);
				strSystemFilesCreated += systemFileName + ",";
			}

			if (strSystemFilesCreated != "") {
				LOGGER.info(
						"Files created : " + strSystemFilesCreated.substring(0, (strSystemFilesCreated.length() - 1)));
			}
		} catch (Exception e) {
			LOGGER.error("Error occured : " + e.getMessage());
		}
	}

	public String deleteFileAWSStorage(final String fileName, final String subFolder, final int ncontrolcode,
			final UserInfo userInfo) throws Exception {

		final String folderName = (subFolder != "") ? (subFolder + "/") : "";

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName)
				.key(folderName + fileName).build();
		s3.deleteObject(deleteObjectRequest);

		final String strRtn = "File deleted successfully: " + folderName + fileName;
		LOGGER.info(strRtn);

		return strRtn;
	}

	public String deleteS3File(final List<String> lstFiles, final String sChangeDirectory, final int ncontrolcode,
			final UserInfo userInfo) throws Exception {

		String changedirectory = "";

		if (sChangeDirectory.isEmpty() || sChangeDirectory == "") {
			final String subfolderquery = "select ssubfoldername,ncontrolcode from ftpsubfolder where nformcode="
					+ userInfo.getNformcode() + "  and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			final List<FTPSubFolder> lstForm = jdbcTemplate.query(subfolderquery, new FTPSubFolder());
			if (!lstForm.isEmpty()) {
				changedirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
						.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
			}
		} else {
			changedirectory = sChangeDirectory;
		}

		final String folderName = (changedirectory != "") ? (changedirectory + "/") : "";

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		String strFileName = "";

		for (String fileName : lstFiles) {
			final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName)
					.key(folderName + fileName).build();
			s3.deleteObject(deleteObjectRequest);
			strFileName += fileName + ",";
		}

		String strRtn = "File deleted successfully: " + strFileName.substring(0, strFileName.length() - 1);
		LOGGER.info(strRtn);
		strRtn = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		return strRtn;
	}

	public String getFileS3Upload(final MultipartHttpServletRequest request,
			final Map<String, Object> credentialDetails, final UserInfo userInfo) throws Exception {
		String rtnStatus = "";
		try {

			final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
			final S3Client s3 = (S3Client) mapObjGet.get("s3");
			final String bucketName = (String) mapObjGet.get("bucketName");

			final int filecount = Integer.parseInt(request.getParameter("filecount"));

			for (int i = 0; i < filecount; i++) {
				final MultipartFile multiPartFile = request.getFile("uploadedFile" + i);

				if (multiPartFile == null || multiPartFile.isEmpty()) {
					continue;
				}

				final String fileUploadDirectory = (credentialDetails.containsKey("folder")
						&& (credentialDetails.get("folder") != "")) ? (credentialDetails.get("folder") + "/") : "";
				final String uniqueFileName = fileUploadDirectory + request.getParameter("uniquefilename" + i);

				final PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName)
						.key(uniqueFileName).contentType(multiPartFile.getContentType())
						.contentLength(multiPartFile.getSize()).build();

				s3.putObject(putObjectRequest,
						RequestBody.fromInputStream(multiPartFile.getInputStream(), multiPartFile.getSize()));
				rtnStatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			}

		} catch (Exception e) {
			LOGGER.error("Connection Failed: " + e);
			rtnStatus = Enumeration.ReturnStatus.FAILED.getreturnstatus();
		}
		return rtnStatus;
	}

	public Map<String, Object> fileUploadAndDownloadInS3(final String pdfPath, final String outFileName,
			final String customFileName, final String changeWorkingDirectory, final String fileDownloadURL,
			final int ncontrolcode, final UserInfo userInfo) throws Exception {

		final Map<String, Object> mapObj = new HashMap<>();

		final String ssystemFileName = customFileName.isEmpty() ? outFileName : customFileName;
		final Path localFilePath = Paths.get(pdfPath + ssystemFileName);

		final String filePathToUpload = (changeWorkingDirectory.isEmpty() || changeWorkingDirectory == "")
				? ssystemFileName
				: (changeWorkingDirectory + "/" + ssystemFileName);

		final String homePath = getFileAbsolutePath();
		final String downloadsPath = System.getenv(homePath) + "\\" + Enumeration.FTP.DOWNLOAD_PATH.getFTP();
		final Path downloadsDir = Paths.get(downloadsPath);

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		try {
			final PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName)
					.key(filePathToUpload).build();

			s3.putObject(putObjectRequest, RequestBody.fromFile(localFilePath));

			LOGGER.info("File uploaded from : " + filePathToUpload);
			LOGGER.info("File uploaded to S3: " + localFilePath);

			final Path localFilePathToDownload = downloadsDir.resolve(ssystemFileName);

			final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName)
					.key(filePathToUpload).build();

			if (Files.exists(localFilePathToDownload)) {
				Files.delete(localFilePathToDownload);
			}

			s3.getObject(getObjectRequest, ResponseTransformer.toFile(localFilePathToDownload));
			final String downloadsPath1 = Enumeration.FTP.FILE_PATH_TO_UPLOAD.getFTP() + "/" + ssystemFileName;
			LOGGER.info("File downloaded from s3 to: " + localFilePathToDownload);

			mapObj.put("filepath", downloadsPath1);
			mapObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		} catch (Exception e) {
			LOGGER.error("Failed : " + e.getMessage());
			mapObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.FAILED.getreturnstatus());
		}
		return mapObj;
	}

	public String multiControlS3FileUpload(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		final List<ControlMaster> controlCodeList = mapper.readValue(request.getParameter("controlcodelist"),
				new TypeReference<List<ControlMaster>>() {
				});

		String changeDirectory = "";

		Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		S3Client s3 = (S3Client) mapObjGet.get("s3");
		String bucketName = (String) mapObjGet.get("bucketName");
		String uniqueFileName = "";
		String rtnStatus = "";

		try {
			for (int j = 0; j < controlCodeList.size(); j++) {
				final int filecount = Integer
						.valueOf(request.getParameter(controlCodeList.get(j).getScontrolname() + "_filecount"));
				changeDirectory = controlCodeList.get(j).getSsubfoldername();

				for (int i = 0; i < filecount; i++) {
					MultipartFile objmultipart = request
							.getFile(controlCodeList.get(j).getScontrolname() + "_uploadedFile" + i);

					uniqueFileName = request
							.getParameter(controlCodeList.get(j).getScontrolname() + "_uniquefilename" + i);

					String fileUploadDir = changeDirectory + "/" + uniqueFileName;

					PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(fileUploadDir)
							.contentType(objmultipart.getContentType()).contentLength(objmultipart.getSize()).build();

					s3.putObject(putObjectRequest,
							RequestBody.fromInputStream(objmultipart.getInputStream(), objmultipart.getSize()));
				}
			}

			rtnStatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		} catch (Exception e) {
			LOGGER.info("File upload failed");
			rtnStatus = Enumeration.ReturnStatus.FAILED.getreturnstatus();
		}

		return rtnStatus;
	}

	public Map<String, Object> multiPathMultiFileDownloadUsingS3(final Map<String, Object> fileMap,
			final List<ControlMaster> controlCodeList, final UserInfo objUserInfo, final String sCustomPath)
			throws Exception {

		final Map<String, Object> mapObjGet = getAWSClientBucket(objUserInfo);
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		Map<String, Object> mapRtnObj = new HashMap<>();

		for (int j = 0; j < controlCodeList.size(); j++) {

			String changeWorkingDirectory = controlCodeList.get(j).getSsubfoldername();

			if (fileMap.get(controlCodeList.get(j).getScontrolname() + "_fileName") != null) {
				String filePath = (String) fileMap.get(controlCodeList.get(j).getScontrolname() + "_path");

				String sCustomName = (String) fileMap.get(controlCodeList.get(j).getScontrolname() + "_customName");
				final String ssystemfilename = (String) fileMap
						.get(controlCodeList.get(j).getScontrolname() + "_fileName");

				String sCompressfilename = ((sCustomName.isEmpty() || sCustomName == "") ? ssystemfilename
						: sCustomName);

				String finalFilePath = (sCustomPath.isEmpty() || sCustomPath == "") ? filePath : sCustomPath;

				final File folderCheck = new File(finalFilePath);

				if (!folderCheck.exists()) {
					final boolean created = folderCheck.mkdirs();
					if (created) {
						LOGGER.info("Folder created: " + folderCheck.getAbsolutePath());
					} else {
						LOGGER.info("Failed to create folder.");
					}
				}

				Path downloadsDir = Paths.get(finalFilePath);
				Path downloadPath = downloadsDir.resolve(sCompressfilename);

				if (Files.exists(downloadPath)) {
					Files.delete(downloadPath);
				}

				final String subFolderPath = (!changeWorkingDirectory.isEmpty()) ? (changeWorkingDirectory + "/") : "";

				try {

					final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName)
							.key(subFolderPath + sCompressfilename).build();

					s3.getObject(getObjectRequest, downloadPath);

					mapRtnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					mapRtnObj.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()), "true");
				} catch (Exception e) {
					LOGGER.info("Failed to download");
					mapRtnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.FAILED.getreturnstatus());
					mapRtnObj.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()), "false");
				}

				mapRtnObj.put(controlCodeList.get(j).getScontrolname() + "_AttachFile", sCompressfilename);
				mapRtnObj.put(controlCodeList.get(j).getScontrolname() + "_FileName", sCompressfilename);
				mapRtnObj.put(controlCodeList.get(j).getScontrolname() + "_FilePath", filePath + sCompressfilename);
			}

		}
		return mapRtnObj;
	}

	public String multiPathDeleteS3File(final Map<String, Object> fileMap, final List<ControlMaster> controlCodeList,
			UserInfo userInfo) throws Exception {
		String returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		String changedirectory = "";

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		for (int j = 0; j < controlCodeList.size(); j++) {
			changedirectory = controlCodeList.get(j).getSsubfoldername() + "/";

			if (fileMap.get(controlCodeList.get(j).getScontrolname() + "_fileName") != null) {
				final String sfileName = (String) fileMap.get(controlCodeList.get(j).getScontrolname() + "_fileName");

				try {
					final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName)
							.key(changedirectory + sfileName).build();
					s3.deleteObject(deleteObjectRequest);

					returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				} catch (Exception e) {
					LOGGER.info("File Deletion Error: " + e.getMessage());
				}

			}
		}
		return returnStr;
	}
}
