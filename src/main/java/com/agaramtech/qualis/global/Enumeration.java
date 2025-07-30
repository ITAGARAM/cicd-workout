package com.agaramtech.qualis.global;

public class Enumeration {
	public static final String DELIM = ".";

	public enum TransactionStatus {
		NA(-1),
		DELETED(-1),
		NON_EMPTY(0),
		ALL(0),
		ACTIVE(1),
		MASTER(1),
		CHECKLIST_TEMPLATE_VIEW(1),
		SEQUENCEONE(1),
		INHOUSE(1),
		STANDARDTYPE(1),
		OUTSIDE(2),// for material Qty transaction
		DEACTIVE(2),
		TRANSACTION(2),
		VOLUMETRICTYPE(2),
		CHECKLIST_TEST_GROUP_TEMPLATE_VIEW(2),
		YES(3),
		CHECKLIST_RESULTENTRY_VIEW(3),
		MATERIALINVENTORYTYPE(3),
		NO(4),
		DAYPERIOD(4),
		IQCSTANDARDMATERIALTYPE(4),
		LOCK(5),
		MONTHPERIOD(5),
		UNLOCK(6),
		YEARPERIOD(6),
		RETIRED(7),
		DRAFT(8),
		MANUAL(9),
		SYSTEM(10),
		START(11),
		STARTED(11),
		RESTART(12),
		STOP(13),
		RECEIVED(14),
		GOODS_IN(15),
		GOODS_RECEIVED(16),
		PREREGISTER(17),
		REGISTERED(18),
		RECIEVED_IN_SECTION(19),
		ALLOTTED(20),
		RE_SCHEDULED(21),
		ACCEPTED(22),
		INITIATED(23),
		RECALC(24),
		COMPLETED(25),
		RECOM_RECALC(26),
		RECOM_RETEST(27),
		CHECKED(28),
		REVIEWED(29),
		VERIFIED(30),
		APPROVED(31),
		RETEST(32),
		RELEASED(33),
		REJECTED(34),
		CANCELED(35),
		PARTIALLY(36),
		QUARENTINE(37),
		AUTHORISED(38),
		HOLD(39),
		RESERVED(40),
		SCHEDULED(41),
		INVITED(42),
		ISSUE(43),
		AUTO(44),
		PASS(45),
		FAIL(46),
		WITHDRAWN(47),
		CORRECTION(48),
		CERTIFIED(49),
		EXPIRED(50),
		INSERTED(51),
		UPDATED(52),
		VALIDATION(54),
		UNDERVALIDATION(55),
		CALIBIRATION(56),
		UNDERCALIBIRATION(57),
		MAINTANENCE(58),
		UNDERMAINTANENCE(59),
		CONDUCTED(60),
		ATTENDED(61),
		ACCREDIT(62),
		NOTACCREDIT(63),
		RETURN(64),
		WORKFLOW(65),
		SEND_REPORT(66),
		CERTIFICATECORRECTION(67),
		OVERDUE(68),
		REMAINDER(69),
		SENT(70),
		NULLIFIED(71),
		AUTO_ALLOT(72),
		AUTO_COMPLETE(73),
		AUTO_APPROVAL(74),
		PARTIAL_APPROVAL(75),
		SECTIONWISE_APPROVAL(76),
		ESIGN(77),
		JOB_ALLOCATION(78),
		BLACKLIST(79),
		DISCARD(80),
		OBSOLETE(81),
		CLOSED(81),
		GENERATED(82),
		PREPARED(83),
		SENDTOSTORE(84),
		CENTRALTOLOCAL(85),
		LOCALTOCENTRAL(86),
		BIDIRECTIONAL(87),
		SUCCESS(88),
		FAILED (89),
		INTERMEDIATE (90),
		DISPOSED(91),
		USED(91),
		OFFSITE(92), //Send to Store
		SENTSUCCESS(93),
		SENTFAILED(94),
		RECEIVESUCCESS(95),
		RECEIVEFAILED(96),
		RETRIEVED(97),
		PRELIMINARYRELEASE(98),
		EMPTY(101),
		NOTYETPROCESSED(112),
		PROCESSED(113),
		LOGIN(170);
		

		private final int transactionstatus;

		private TransactionStatus(int transactionstatus) {
			this.transactionstatus = transactionstatus;
		}

		public int gettransactionstatus() {
			return transactionstatus;
		}
	}

	public enum QualisMenu {
		MASTER(1), TRANSACTION(2), REPORTS(3);

		private final int qualismenu;

		public int getQualismenu() {
			return qualismenu;
		}

		private QualisMenu(int qualismenu) {
			this.qualismenu = qualismenu;
		}

	}

	public enum ModuleCode {
		USERMANAGEMENT(3),
		SAMPLEREGISTRATION(10),
		RESULTENTRY(11),
		APPROVAL(12),
		BATCH(16),
		JOBALLOCATION(28),
		STABILITY(73);

		private final int moduleCode;

		private ModuleCode(int moduleCode) {
			this.moduleCode = moduleCode;
		}

		public int getModuleCode() {
			return moduleCode;
		}
	}

	public enum QualisForms {
		USERS(3),
		GOODSIN (7),
		MATERIALCATEGORY(23),
		PRODUCTCATEGORY(24),
		INSTRUMENTCATEGORY(27),
		TESTMASTER(41),
		SAMPLEREGISTRATION(43),
		APPROVALCONFIGURATION(55),
		RESULTENTRY(56),
		APPROVAL(61),
		TESTGROUP(62),
		REPORTCONFIG(77),
		CLOCKHISTORY(94),
		CLOCKMONITORING(94),
		MYJOB(107),
		BARCODE(108),
		JOBALLOCATION(110),
		TESTWISEMYJOB(142),
		RELEASE(143),
		SAMPLESTORAGEMASTER(169),
		PROJECT(172),
		WORKLIST(173),
		BATCHCREATION(174),
		SCHEDULERCONFIGURATION(244),
		PROTOCOL(245),
		STABILITY(246);

		private final int qualisforms;

		private QualisForms(int qualisforms) {
			this.qualisforms = qualisforms;
		}

		public int getqualisforms() {
			return qualisforms;
		}
	}

	public enum LoginType {
		INTERNAL(1), ADS(2);

		private final int nlogintype;

		private LoginType(int nlogintype) {
			this.nlogintype = nlogintype;
		}

		public int getnlogintype() {
			return nlogintype;
		}
	}

	//	public enum DashBoardStatus {
	//		STATUS_DELETE(0), STATUS_ACTIVE(1), STATUS_USER_LEVELS(1), STATUS_USERS(2), DATE_CONTROLCODE(5),
	//		TISSUEVERIFY(16);
	//
	//		DashBoardStatus(int dashboardstatus) {
	//			this.dashboardstatus = dashboardstatus;
	//		}
	//
	//		private final int dashboardstatus;
	//
	//		public int getdashboardstatus() {
	//			return dashboardstatus;
	//		}
	//	}

	public enum ReturnStatus {
		SUCCESS("Success"), ALREADYEXISTS("IDS_ALREADYEXISTS"), ALREADYDELETED("IDS_ALREADYDELETED"),CLINICALSPECALREADYEXISTS("IDS_CLICICALSPECALREADYEXISTS"),
		ALREADYOPENED("IDS_ALREADYCLOSED"), ALREADYCLOSED("IDS_ALREADYCLOSED"), RETURNSTRING("rtn"),
		RETURNVALUE("value"), ALREADYAPPROVED("IDS_ALREADYAPPROVED"), ALREADYRETIRED("IDS_ALREADYRETIRED"),
		ALREADYCERTIFIED("IDS_ALREADYCERTIFIED"), NOTESTGROUP("IDS_NOTESTGROUP"),
		ALREADYRELEASED("IDS_ALREADYRELEASED"), SAMPLEREGSTATUS("SampleRegStatus"),
		ALREADYCOMPLETED("IDS_ALREADYCOMPLETED"), RECORD_UPDATE("IDS_RECORD_UPDATE"),
		RECORD_DELETE("IDS_RECORD_DELETE"), RECORD_COMPLETE("IDS_RECORD_COMPLETE"), APPROVED("IDS_APPROVED"),
		RECORD_UPDATE_TP_ATTENDED("IDS_RECORD_UPDATE_TP_ATTENDED"), BARCODEEXISTS("IDS_BARCODEEXISTS"),
		TOOMANYUSERS("IDS_TOOMANYUSERS"), ORDERCODENOTGOT("IDS_ORDERCODENOTGOT"), SAMPLERECEIVED("IDS_SAMPLERECEIVED"),
		SAMPLEALIQUOTED("IDS_SAMPLEALIQUOTED"), SAMPLERESERVED("IDS_SAMPLERESERVED"), FORANALYSIS("IDS_FORANALYSIS"),
		SAMPLEINSTORE("IDS_SAMPLEINSTORE"), DISCARD("IDS_SAMPLEDISCARD"), RETURN("IDS_SAMPLERETURN"),
		LICENSEEXEEDED("IDS_LICENSEEXEEDED"), CHECKLICENSECONFIG("IDS_CHECKLICENSECONFIG"),
		LICENSEMISMATCH("IDS_LICENSEMISMATCHED"), SAMEUSERANDROLELOGIN("IDS_SAMEUSERANDROLELOGIN"), FAILED("Failed"),
		DEFAULTCANNOTDELETE("IDS_DELETEDEFAULT"), // altered by L.Subashini on 14/08/2020
		DEFAULTROLECANNOTBEDELETED("IDS_DELETEDEFAULTROLE"), // added by L.Subashini on 16/11/2020
		DEFAULTUSERSITECANNOTDELETED("IDS_DEFAULTUSERSITECANNOTDELETED"), PLATEALREADYADDED("IDS_PLATEALREADYADDED"),
		QUANTITYGREATER("IDS_QUANTITYGREATER"), ENTERVALIDINVENTORYID("IDS_ENTERVALIDINVENTORYID"),
		DUPLICATEBARCODE("IDS_DUPLICATEBARCODE"), DRAFTRETIRED("IDS_DRAFTRETIRED"),
		ALREADYDEFAULT("IDS_ALREADYDEFAULT"), ADDQUESTIONTOAPPROVE("IDS_ADDQUESTIONTOAPPROVE"),
		NOTPOSSIBLETODELETEAPPROVE("IDS_NOTPOSSIBLETODELETEAPPROVE"), SELECTDRAFTVERSION("IDS_SELECTDRAFTVERSION"),
		ALREADYCANCELED("IDS_ALREADYCANCELED"), SELECTREGISTEREDSAMPLE("IDS_SELECTREGISTEREDSAMPLE"),
		INVALIDUSER("IDS_INVALIDUSER"), DEFAULTUSERROLEMUSTBETHERE("IDS_DEFAULTUSERROLEMUSTBETHERE"),
		INACTIVEDEFAULTROLE("IDS_INACTIVEDEFAULTROLE"), APPROVETHEPROJECT("IDS_APPROVETHEPROJECT"),
		OLDPASSWORDMISSMATCH("IDS_OLDPASSWORDMISSMATCH"),
		// added by sathish on 15/10/2018 - MIT470
		IDS_DUPNOALLOW("IDS_DUPNOALLOW"), IDS_PLATEALREADYFILL("IDS_PLATEALREADYFILL"),
		SELECTPLATETYPEFORMAT("IDS_SELECTPLATETYPEFORMAT"), ALREADYREGISTERED("IDS_ALREADYREGISTERED"),
		ALREADYSTARTTED("IDS_ALREADYSTARTTED"), SELECTSTARTTED("IDS_SELECTSTARTTED"),
		NODEFAULTRESULT("IDS_NODEFAULTRESULT"), ADDRESULTTOCOMPLETE("IDS_ADDRESULTTOCOMPLETE"),
		SELECTREGISTERSAMPLE("IDS_SELECTREGISTERSAMPLE"), SELECTSAMEINSTRUMENTCATTEST("IDS_SELECTSAMEINSCATTEST"),
		ALREADYTESTALLOTED("IDS_ALREADYTESTALLOTED"), ALREADYTESTSTARTTED("IDS_ALREADYTESTSTARTED"),
		ALLOTEDACCEPTEDTESTCANRESCHEDULE("IDS_ALLOTEDACCEPTEDTESTCANRESCHEDULE"),
		ALREADYREJECTED("IDS_ALREADYREJECTED"), SAMPLEINPROGRESS("IDS_SAMPLEINPROGRESS"), TABLOCK(" IN access exclusive mode"),
		SAMPLECODENOTAVAILABLE("IDS_SAMPLECODENOTAVAILABLE"), SAMPLEEXISTS("IDS_SAMPLEEXISTS"), NOSAMPLE("NOSAMPLE"),
		LOCATIONDELETED("IDS_LOCATIONDELETED"), CALIBRATED("AutoCalibrated"), UNDERCALIBRATED("AutoUnderCalibration"),
		MAINREPORTDELETEVAL("IDS_MAINREPORTDELETEVAL"), REPORTALREADYCONFIG("IDS_REPORTALREADYCONFIG"),
		REPORTNOTCONFIGURED("Selected Report is not Configured"), FILEALREADYEXISTS("File Already Exists"),
		NOTVALIDIN("Not Valid in"), SHOULDNOTEMPTY("Should Not Empty on row"), DEFAULTCANNOTCHANGED("IDS_EDITDEFAULT"), // added
		EXCLUSIVEMODE(" IN exclusive mode"),NODATAAVAILABLEINLIMS("No Data Available in LIMS"),STATUS("Status"),
		COMPLETED("Completed"),// by
		SELECTDRAFTRECORD("IDS_SELECTDRAFTRECORD"),SELECTLESSTHANCURRENTDATE("Please select Less than current date"),																					// L.Subashini
		RELEASEDSAMPLESNOTAVAILABLEFORPURGE("Released samples not available for purge"),
		NODATAFOUND("No Data Found"),																												// on
		// 14/08/2020
		UPLOAD_EXCEL("Upload Excel File Only.."), NORELATEDDATA("No Related Data Found"), ROWLOCK("WITH (ROWLOCK)"),
		STR_ENCRYPT_SECRET("AGARAM_SDMS_SCRT"),STANDARDMATERIALNAME("Standard Name")
		,VOLUMETRICMATERIALNAME("Volumetric Name"),MATERIALNAME("Material Name"),
		ALREADYRECEIVED("IDS_ALREADYRECEIVED"),ALREADYATTENDED("IDS_ALREADYATTENDED"),ALREADYCOMPETENT("IDS_ALREADYCOMPETENT"),
		ALREADYINVITED("IDS_ALREADYINVITED"),
		TESTALREADYDELETED("IDS_TESTALREADYDELETED"),

		// Added for License
		LICENSELIMITEXCEED("IDS_LICENSELIMITEXCEED"),CHECKPASSWORDPOLICYROLE("IDS_CHECKPASSWORDPOLICYROLE"),
		THISUSERALREADYLOGGEDIN("IDS_THISUSERALREADYLOGGEDIN"),THISUSERROLEALREADYLOGGEDIN("IDS_THISUSERROLEALREADYLOGGEDIN"),
		INVALIDLICENSECOUNT("IDS_INVALIDLICENSECOUNT"),
		
		//Added for BGSI
		NOTVALIDSUBJECTID("IDS_NOTVALIDSUBJECTID");

		private final String returnStatus;

		private ReturnStatus(String returnStatus) {
			this.returnStatus = returnStatus;
		}

		public String getreturnstatus() {
			return returnStatus;

		}

	}

	public enum RestoreFilterType {
		PURGEDATE(1), RELEASENO(2),ARNO(3);

		private final int nrestorefiltertype;

		private RestoreFilterType(int nrestorefiltertype) {
			this.nrestorefiltertype = nrestorefiltertype;
		}

		public int getnrestorefiltertype() {
			return nrestorefiltertype;
		}
	}
	public enum ModuleType {
		LIMS(1), STABILITY(2);

		private final int moduletype;

		private ModuleType(int moduletype) {
			this.moduletype = moduletype;
		}

		public int getModuletype() {
			return moduletype;
		}
	}


	public enum ParameterType {
		NUMERIC(1), PREDEFINED(2), CHARACTER(3), ATTACHEMENT(4);

		private final int parametertype;

		private ParameterType(int parametertype) {
			this.parametertype = parametertype;
		}

		public int getparametertype() {
			return parametertype;
		}

	}

	// modified by SYED 20-NOV-2023
	public enum Grade {
		NA(-1), PASS(1), OOT(2),
		OOS(3),
		FIO(4),
		DISREGARD(5),H_LOQ(6),H_LOD(7),L_LOQ(8),L_LOD(9),
		H_OOT(10),
		H_OOS(11),;

		private final int Grade;

		private Grade(int Grade) {
			this.Grade = Grade;
		}

		public int getGrade() {
			return Grade;
		}

	}

	public enum PasswordValidate {
		PASS(-1), ID_MISMATCH(1),
		PWD_MISMATCH(2), RETIRED(3),
		LOCK(4), DEACTIVE(5),
		NEW_USER(6), RESET_USER(7),
		CHECKPASSWORDPOLICYROLE(10),
		LOGINLIMITEXCEED(11), CHECKLICENSECONFIG(12),
		SAMEUSERLOGIN(16),
		SAMEUSERANDROLELOGIN(14), LICENSEMISMATCH(15), USER_LOCK(5), EXPIRED(50);

		private final int paswordvalidate;

		private PasswordValidate(int paswordvalidate) {
			this.paswordvalidate = paswordvalidate;
		}

		public int getPaswordvalidate() {
			return paswordvalidate;
		}
	}

	public enum ApprovalSubType {
		STUDYPLANAPPROVAL(1), TESTRESULTAPPROVAL(2), BATCHAPPROVAL(3),
		PRODUCTAPPROVAL(4), PRODUCTMAHAPPROVAL(5),PROTOCOLAPPROVAL(6);

		private final int nsubtype;

		private ApprovalSubType(int nsubtype) {
			this.nsubtype = nsubtype;
		}

		public int getnsubtype() {
			return nsubtype;
		}
	}

	public enum FTP {
		UPLOAD_PATH("\\webapps\\ROOT\\SharedFolder\\FileUpload\\"),
		USERSIGN_PATH("\\webapps\\ROOT\\SharedFolder\\UserSign\\"),
		USERPROFILE_PATH("\\webapps\\ROOT\\SharedFolder\\UserProfile\\"),
		JBOSS_EAP_7_2_0_UPLOAD_PATH("welcome-content/SharedFolder/FileUpload/"),
		JBOSS_EAP_7_2_0_USERSIGN_PATH("welcome-content/SharedFolder/UserSign/"),
		JBOSS_EAP_7_2_0_USERPROFILE_PATH("welcome-content/SharedFolder/UserProfile/"),
		UBUNTU_TOMCAT_UPLOAD_PATH("webapps/ROOT/SharedFolder/FileUpload/"),
		UBUNTU_TOMCAT_USERSIGN_PATH("webapps/ROOT/SharedFolder/UserSign/"),
		UBUNTU_TOMCAT_USERPROFILE_PATH("webapps/ROOT/SharedFolder/UserSign/"),
		DOWNLOAD_PATH("\\webapps\\ROOT\\SharedFolder\\Downloads\\"),
		FILE_PATH_TO_UPLOAD("/SharedFolder/Downloads/"),
		REPORTVIEW_FILE_PATH("/SharedFolder/ReportView/");

		public static final String DELIM = ".";

		private final String FTP;

		private FTP(String FTP) {
			this.FTP = FTP;
		}

		public String getFTP() {
			return FTP;
		}
	}

	public enum DesignComponent {
		TEXTBOX(1), TEXTAREA(2), COMBOBOX(3), DATEFEILD(4), NUMBER(5), CHECKBOX(6),USERINFO(8);

		private final int type;

		private DesignComponent(int type) {
			this.type = type;
		}

		public int gettype() {
			return type;
		}

	}

	//	// added by barath on 22-nov -2014
	//	public enum LinkType {
	//		CUSTOM(1), EXISTING(2), NONLIST(-1);
	//
	//		private LinkType(int type) {
	//			this.type = type;
	//		}
	//
	//		private final int type;
	//
	//		public int getType() {
	//			return type;
	//		}
	//
	//	}

	// added by barath on 10-Dec -2014
	public enum SampleType {
		SUBSAMPLE(-1),
		PRODUCT(1),
		INSTRUMENT(2),
		MATERIAL(3), //STORAGECATEGORY(4),
		MASTERS(4),
		CLINICALSPEC(5),
		PROJECTSAMPLETYPE(6),
		GOODSIN(7),
		STABILITY(9),
		PROTOCOL(8);

		private SampleType(int type) {
			this.type = type;
		}

		private final int type;

		public int getType() {
			return type;
		}

	}

	public enum Sequenceintialvalue {

		ZERO(0), ONE(1);

		private Sequenceintialvalue(int sequence) {
			this.sequence = sequence;
		}

		private int sequence;

		public int getSequence() {
			return sequence;
		}

		public void setSequence(int sequence) {
			this.sequence = sequence;
		}

	}

	public enum Deletevalidator {

		SUCCESS(0), ERORR(1), NOVALIDATION(-1);

		private int returnvalue;

		private Deletevalidator(int returnvalue) {
			this.returnvalue = returnvalue;
		}

		public int getReturnvalue() {
			return returnvalue;
		}

		public void setReturnvalue(int returnvalue) {
			this.returnvalue = returnvalue;
		}

	}


	//Vignesh for Report and Pdf Images on 16-03-2016
	public enum FTPImage {
		UPLOAD_PATH("\\webapps\\ROOT\\SharedFolder\\Report Images\\");
		public static final String DELIM = ".";

		private final String FTPImage;

		private FTPImage(String FTPImage) {
			this.FTPImage = FTPImage;
		}

		public String getFTPImage() {
			return FTPImage;
		}
	}

	public enum AttachmentType {
		FTP(1), LINK(2), FILETABLE(3);

		private final int type;

		private AttachmentType(int type) {
			this.type = type;
		}

		public int gettype() {
			return type;
		}
	}

	//Added By RAJESH KUMAR.K
	public enum FTPReplycodes {

		REQUIRED_PASSWORD(331), SERVER_RESPONSE_OK(200), SERVER_USERLOGGED_RESPONSE(230), NEED_VALID_CREDENTIALS(332),
		CANT_OPEN_CONNECTION(425), SERVER_TIMEOUT(10060), SWITCH_CONNECTION_PORT(10061), TOO_MANY_USER_ACTIVE(10068),
		NOT_LOGGED_IN(530), INVALID_CREDENTIALS(430), HOST_UNAVAILABLE(434);

		private final int ReplyCode;

		private FTPReplycodes(int ReplyCode) {
			this.ReplyCode = ReplyCode;
		}

		public int getReplyCode() {
			return ReplyCode;
		}

	}

	public enum FTP_DIR {
		REPORT("JRXMLREPORT");

		private final String ftp_directory;

		public String getFtp_directory() {
			return ftp_directory;
		}

		private FTP_DIR(String ftp_directory) {
			this.ftp_directory = ftp_directory;
		}

	}

	public enum QueryType // Added for Querybuilder
	{
		DASHBOARD(1), ALERTS(2), BARCODE(3), FILTER(5), OTHERS(-1);

		private final int Querytype;

		private QueryType(int qtype) {
			this.Querytype = qtype;
		}

		public int getQuerytype() {
			return Querytype;
		}

	}


	public enum FormCode {
		USERS(3),
		MATERIALCATEGORY(23),
		PRODUCTCATEGORY(24),
		INSTRUMENTCATEGORY(27),
		TESTMASTER(41),
		SAMPLEREGISTRATION(43),
		RESULTENTRY(56),
		APPROVAL(61),
		TESTGROUP(62),
		REPORTCONFIG(77),
		CLOCKMONITORING(94),
		MYJOB(107),
		BARCODE(108),
		JOBALLOCATION(110),
		TESTWISEMYJOBS(142),
		RELEASE(143),
		PROJECT(172),
		BATCHCREATION(174),
		WORKLIST(173),
		PROTOCOL(245);

		private final int formCode;

		private FormCode(int formCode) {
			this.formCode = formCode;
		}

		public int getFormCode() {
			return formCode;
		}
	}


	public enum UserRoleLevel {
		LEVEL1(1);

		private final int level;

		private UserRoleLevel(int level) {
			this.level = level;
		}

		public int getUserRoleLevel() {
			return level;
		}

	}

	//Added by Subashini on 01-Dec-2020
	public enum ReportType {
		COA(1), MIS(2), BATCH(3), SAMPLE(4), CONTROLBASED(5),COAPREVIEW(6),COAPRELIMINARY(7);

		private final int reporttype;

		public int getReporttype() {
			return reporttype;
		}

		private ReportType(int reporttype) {
			this.reporttype = reporttype;
		}

	}

	public enum COAReportType {
		SAMPLEWISE(1), TESTWISE(2), CLIENTWISE(3), SAMPLECERTIFICATE(12), SAMPLECERTIFICATEPRIVIEW(4), BATCH(5),
		BATCHPREVIEW(6), SECTIONWISE(8),PROJECTWISE(7), BATCHSTUDY(13),PATIENTWISE(10),SECTIONWISEMULTIPLESAMPLE(9);

		private final int coaReportType;

		public int getcoaReportType() {
			return coaReportType;
		}

		private COAReportType(int coaReportType) {
			this.coaReportType = coaReportType;
		}

	}

	public enum ChartType {
		GRID(-2), AREA(1), BAR(2), BUBBLE(3), COLUMN(5), DONUT(6), PIE(8);

		private final int chartType;

		private ChartType(int chartType) {
			this.chartType = chartType;
		}

		public int getChartType() {
			return chartType;
		}
	}

	//	public enum DynamicRecordDetailType {
	//		SAMPLE(1), SUBSAMPLE(2), TEST(3);
	//
	//		private final int recorddetailtype;
	//
	//		public int getRecorddetailtype() {
	//			return recorddetailtype;
	//		}
	//
	//		private DynamicRecordDetailType(int recorddetailtype) {
	//			this.recorddetailtype = recorddetailtype;
	//		}
	//
	//	}

	public enum InterFaceType {

		ELN(1),
		INTERFACER(2),
		SDMS(3);

		private final int interFaceType;

		public int getInterFaceType() {
			return interFaceType;
		}

		private InterFaceType(int interFaceType) {
			this.interFaceType = interFaceType;
		}
	}

	/* added by sudharshanan for report settings */
	public enum ReportSettings {

		REPORT_PATH(1), REPORT_PDF_PATH(4), REPORT_DOWNLOAD_URL(5),SMTLP(12), REPORTINGTOOL_URL(13),
		REPORTLINKPATH(15),MRT_REPORT_JSON_PATH(16)
		;

		private final int nreportsettingcode;

		public int getNreportsettingcode() {
			return nreportsettingcode;
		}

		private ReportSettings(int nreportsettingcode) {
			this.nreportsettingcode = nreportsettingcode;
		}

	}

	//	ALPD-4393 17/06/2024 Abdul Gaffoor.A To validate ads password of login User and to get ads user details and update it
	public enum ADSAttributes {
		EC_NO(1), LOGIN_ID(2), EMAIL_ID(3);

		private final int nadsattributecode;

		public int getNadsattributecode() {
			return nadsattributecode;
		}

		private ADSAttributes(int nadsattributecode) {
			this.nadsattributecode= nadsattributecode;
		}
	}

	//added by sudharshanan.P on 29-03-2022
	public enum Condition {
		EQUALS(1),
		NOTEQUALS(2),
		CONTAINS(3),
		NOTCONTAINS(4),
		STARTSWITH(5),
		ENDSWITH(6),
		INCLUDES(7),
		LESSTHAN(8),
		LESSTHANOREQUALS(9),
		GREATERTHAN(10),
		GREATERTHANEQUALS(11);
		private final int condition;

		public int getCondition() {
			return condition;
		}
		private Condition(int condition) {
			this.condition = condition;
		}

	}


	//added by Subashini.L on 20/04/2022
	public enum DesignProperties {
		LABEL(1), VALUE(2), LISTITEM(3), SINGLEITEMDATA(4),
		GRIDITEM(5), GRIDEXPANDABLEITEM(6), LISTMOREITEM(7), LISTMAINFIELD(8),
		COLOUR(9);

		private final int type;

		private DesignProperties(int type) {
			this.type = type;
		}

		public int gettype() {
			return type;
		}

	}

	//added by Subashini.L on 26/04/2022
	public enum ApprovalStatusFunction {
		SAMPLEREGISTRATIONFILTER(9);

		private final int nstatustype;

		private ApprovalStatusFunction(int nstatustype) {
			this.nstatustype = nstatustype;
		}

		public int getNstatustype() {
			return nstatustype;
		}
	}

	public enum CommentSubType {
		TEST_COMMENTS(1), SAMPLE_COMMENTS(2), PREDEFINED_TEST_COMMENTS(3), DEVIATION_COMMENTS(5), REPORTS_COMMENTS(4), GENERAL_COMMENTS(6);

		private final int ncommentsubtype;

		private CommentSubType(int ncommentsubtype) {
			this.ncommentsubtype = ncommentsubtype;
		}

		public int getncommentsubtype() {
			return ncommentsubtype;
		}
	}

	public enum TestMaster {
		TrainingNeed(3);
		private final int teststatus;

		private TestMaster(int teststatus) {
			this.teststatus = teststatus;
		}

		public int getteststatus() {
			return teststatus;
		}
	}

	public enum InstrumentLogType {
		INSTRUMENT_VALIDATION(1), INSTRUMENT_CALIBRATION(2), INSTRUMENT_MAINTENANCE(3);
		private final int instrumentlogtype;

		private InstrumentLogType(int instrumentlogtype) {
			this.instrumentlogtype = instrumentlogtype;
		}
		public int getinstrumentlogtype() {
			return instrumentlogtype;
		}
	}

	public enum DatabaseDateFormat {
		FORMAT_1("yyyy-MM-dd HH24:mi:ss"),
		FORMAT_2("yyyy-MM-dd HH:mm:ss");

		private final String dateFormat;

		private DatabaseDateFormat(String dateFormat) {
			this.dateFormat = dateFormat;
		}
		public String getDateFormat() {
			return dateFormat;
		}
	}

	public enum MaterialType {
		VOLUMETRIC_TYPE(2), STANDARD_TYPE(1);

		private final int materialType;

		private MaterialType(int materialType) {
			this.materialType = materialType;
		}

		public int getMaterialType() {
			return materialType;
		}
	}

	public enum Period{
		Weeks(3),
		Days(4),
		Month(5),
		Years(6),
		Hours(2),
		Never(8),
		Minutes(1),
		NA(-1);	 //Added by sonia on 04th Mar 2025 for  jira id:ALPD-5504

		private final int period;

		private Period (int period) {
			this.period =period;
		}
		public int getPeriod () {
			return period;
		}
	}

	public enum ConfigFilter {
		BATCH("IDS_BATCH"), WORKLIST("IDS_WORKLIST"),DEFAULT("DEFAULT");
		private final String ConfigFilter;

		public String getConfigFilter() {
			return ConfigFilter;
		}

		private ConfigFilter(String ConfigFilter) {
			this.ConfigFilter = ConfigFilter;
		}

	}

	public enum OrderType {
		INTERNAL(1),
		EXTERNAL(2),
		NA(-1);
		private final int OrderType;

		public int getOrderType() {
			return OrderType;
		}

		private OrderType(int OrderType) {
			this.OrderType = OrderType;
		}

	}

	public enum ExternalOrderType {
		PORTAL(1),
		PREVENTTB(2),
		OPENMRS(3),
		LIMS(4),
		MANUAL(-1);
		private final int ExternalOrderType;

		public int getExternalOrderType() {
			return ExternalOrderType;
		}

		private ExternalOrderType(int ExternalOrderType) {
			this.ExternalOrderType = ExternalOrderType;
		}

	}

	//	Added for License
	public enum LicenseType {

		CONCURRENTNOSESSION(1),
		CONCURRENTUSERBASEDSESSION(2),
		CONCURRENTUSERROLEBASEDSESSION(3),
		NAMEDLICENSE(4);

		private final int LicenseType;

		public int getLicenseType() {
			return LicenseType;
		}

		private LicenseType(int LicenseType) {
			this.LicenseType = LicenseType;
		}

	}





	//ALPD-4129 For MAterial Accounting
	public enum CalculationType {
		NA(-1),TEST(1), QUANTITY(2), SAMPLE(3);

		private final int calculationtype;

		private CalculationType(int calculationtype) {
			this.calculationtype = calculationtype;
		}

		public int getcalculationtype() {
			return calculationtype;
		}
	}

	/* added by Neeraj for  settings Table*/
	public enum Settings {

		FILE_VIEW_SOURCE(2),
		ATTACHMENT_DOWNLOAD_URL(6),
		NEED_SMTL_JSON_TEMPLATE(29),
		ALLOT_JOB_OPTIONAL(43),
		UPDATING_ANALYSER(45),
		SCHEDULER_BASED_STATUS_UPDATE_TO_PORTAL(52),
		EXPORTVIEW_URL(56),
		INTEGRATION_SERVICE(58),
		DELETE_EXCEPTION_LOG(59),
		NFCOLDREPORT_PATH(61),
		WEBSERVER_PATH(62),
		COAREPORT_GENERATION(64),
		LOGINADSSYNC(65),
		COAREPORTVIEW_URL(66),
		DEPLOYMENTSERVER_HOMEPATH(67),
		REPORT_OPEN_NEW_TAB(70),
		AUTO_CALIBRATION(73),  //Added by sonia on 27th Sept 2024 for Jira idL:ALPD-4939;
		SYNC_DATA_RETRY_COUNT(74),
		DAUGHTERALIQUOTPLAN(75),
		SILIENTAUDITENABLE(80),
		CAPTCHA(81), //Added for sonia on 16th June 2025 for jira id:ALPD-6028 (Captcha Validation)
		MFA(83),
		INSTRUMENT_BASED_STORAGE(84),
		NEEDS3STORAGE(86);
		private final int nsettingcode;

		public int getNsettingcode() {
			return nsettingcode;
		}

		private Settings(int nsettingcode) {
			this.nsettingcode = nsettingcode;
		}

	}

	public enum ExportCell {
		EDIT(3),  VISIBLEHIDE(4);

		private final int nexportCell;

		private ExportCell(int nexportCell) {
			this.nexportCell = nexportCell;
		}

		public int getExportCell() {
			return nexportCell;
		}
	}

	public enum PublicAPIEndPoints {

		GET_LOGIN_INFO("/login/getloginInfo"),
		GET_LOGIN_VALIDATION("/login/getloginvalidation"),
		INTERNAL_LOGIN("/login/internallogin"),
		ADS_LOGIN("/login/adsLogin"),
		GET_ALERTS("/alertview/getAlerts"),
		UNAUTHORIZED_ACCESS("/authorization/invalidToken"),
		LOGIN_AUDITACTION("login/insertAuditAction");

		private final String endPoints;

		/**
		 * @param endPoints
		 */
		private PublicAPIEndPoints(String endPoints) {
			this.endPoints = endPoints;
		}

		public String getEndPoints() {
			return endPoints;
		}

	}

	/* Created By: RAJESH KUMAR.K
	 * Created on : 30/09/2024
	 * Purpose : Wrapper class enum will be used in casting the object in JdbcTemplateUtitlity class
	 * */
	public enum WrapperClasses {

		BYTE("java.lang.Byte"), SHORT("java.lang.Short"), INTEGER("java.lang.Integer"), FLOAT("java.lang.Float"),
		DOUBLE("java.lang.Double"), LONG("java.lang.Long"), BOOLEAN("java.lang.Boolean"), CHARACTER("java.lang.Character");

		private WrapperClasses(String wrapperClassName) {
			this.wrapperClassName = wrapperClassName;
		}

		private final String wrapperClassName;

		public String getWrapperClassName() {
			return wrapperClassName;
		}

	}


	public enum CategoryType {
		SUPPLIERCATEGORY(1),
		MATERIALCATEGORY(2),
		NA(-1);
		private final int CategoryType;

		public int getCategoryType() {
			return CategoryType;
		}

		private CategoryType(int CategoryType) {
			this.CategoryType = CategoryType;
		}

	}

	// ALPD-5332 Added by Abdul for Scheduler Config
	public enum SchedulerConfigType {
		EXTERNAL(2), INTERNAL(1);

		private final int nschedulerconfigtype;

		private SchedulerConfigType(int nschedulerconfigtype) {
			this.nschedulerconfigtype = nschedulerconfigtype;
		}

		public int getSchedulerConfigType() {
			return nschedulerconfigtype;
		}
	}

	public enum Default {

		DEFAULTSPEC(-2);

		private final int defaultspec;

		private Default(int defaultspec) {
			this.defaultspec = defaultspec;
		}

		public int getDefault() {
			return defaultspec;
		}
	}



	public enum DynamicInitialValue {
		QUERYBUILDERVIEWS(5000),QUERYTABLEDETAILS(5000),QUERYBUILDERTABLES(5000),DYNAMICFORMS(5000), DYNAMICMODULE(1000);

		private final int initialValue;

		private DynamicInitialValue(int initialValue) {
			this.initialValue = initialValue;
		}

		public int getInitialValue() {
			return initialValue;
		}
	}


	public enum TransAction {
		SAMPLEREGISTERED("IDS_SAMPLEREGISTERED"),
		RECIEVEDBY("IDS_SAMPLERECEIVEDIN"),
		SENDTOSRORE("IDS_SAMPLEISSENDTOSTORED"),
		SAMPLEDISCARD("IDS_SAMPLEDISCARD"),
		BATCHINITIATED("IDS_SAMPLEUSEDFORBATCHRUN"),
		TESTINITIATED("IDS_SAMPLEUSEDFOR"),
		ALLIQUOTEDTO("Sample Alliquoted For "),
		RESERVEDFOR("Reserved For Store");

		private final String TransAction;

		public String getTransAction() {
			return TransAction;
		}

		private TransAction(String TransAction) {
			this.TransAction = TransAction;
		}

	}

	public enum ReportQueryFormat // Added by RAJESH KUMAR.K on 05-03-2020 FOR JASPER REPORT GENERATION AND REPORT
	// CONFIGURATION IN LIMS
	{
		PLSQL(2), SQL(1);

		private final int reportQueryFormat;

		private ReportQueryFormat(int reportQueryFormat) {
			this.reportQueryFormat = reportQueryFormat;
		}

		public int getReportQueryFormat() {
			return reportQueryFormat;
		}

	}

	public enum RegistrationType {
		INSTRUMENT(4), MATERIAL(5), BATCH(1), NONBATCH(2), PLASMAPOOL(3), ROUTINE(6);

		private final int regtype;

		private RegistrationType(int regtype) {
			this.regtype = regtype;
		}

		public int getregtype() {
			return regtype;
		}
	}

	public enum CertificateReportType {
		NA(-1), PASS(1), FAIL(2), WITHDRAWNBLOOD(3), WITHDRAWNVACCINE(4), NULLIFICATION(5), PLASMAPOOL_PASS(6),
		PLASMAPOOL_FAIL(7), PLASMAPOOL_WITHDRAWN(8);

		private final int ncertificatereporttype;

		public int getNCertificateReportType() {
			return ncertificatereporttype;
		}

		private CertificateReportType(int ncertificatereporttype) {
			this.ncertificatereporttype = ncertificatereporttype;
		}

	}


	public enum Path {
		PROPERTIES_FILE("properties/");

		private final String path;

		public String getPath() {
			return path;
		}

		private Path(String path) {
			this.path = path;
		}
	}

	public enum StringTokens{
		SEMICOLON(";"),
		OPEN_PARENTHESIS("("),
		CLOSE_PARENTHESIS(")"),
		SPACE(" "),
		COMMA(",");

		private final String token;

		public String getToken() {
			return token;
		}

		private StringTokens(String token) {
			this.token = token;
		}
	}

	public enum EntityMapper
	{
		MAPROW("mapRow"),
		TABLENAME("stablename"),
		SEQUENCENO("nsequenceno");
		private EntityMapper(String mappername) {
			this.mappername = mappername;
		}
		private final String mappername;

		public String getmappername(){
			return this.mappername;
		}
	}

	public enum ChecklistComponent {
		COMBOBOX(1), TEXTBOX(2), TEXTAREA(3), CHECKBOX(4), LABEL(5), BUTTON(6), DATEFEILD(7), RADIOBUTTON(8), SERARCHLIST(9);

		private final int nchecklistcomponent;

		private ChecklistComponent(int nchecklistcomponent) {
			this.nchecklistcomponent = nchecklistcomponent;
		}

		public int getNchecklistcomponent() {
			return nchecklistcomponent;
		}

	}	public enum CellType {
		_NONE(-1), // index 0
		NUMERIC(0), // index 0
		STRING(1), // index 1
		FORMULA(2), // index 2
		BLANK(3), // index 3
		BOOLEAN(4), // index 4
		ERROR(5); // index 5 ;

		private final int nCellType;

		private CellType(int nCellType) {
			this.nCellType = nCellType;
		}

		public int getNcellTypet() {
			return nCellType;
		}
	}
	public enum DireactionMaster {
		LEFTTORIGHT(1),  UPTODOWN(2);
		
		private final int ndirectionmastercode;


		private DireactionMaster(int ndirectionmastercode) {
			this.ndirectionmastercode = ndirectionmastercode;
		}

		public int getDireactionMaster() {
			return ndirectionmastercode;
		}
	}
	
	//ALPD-6042--Added by vignesh(21-07-2025)--for Email Authentication.
	public enum AuthenticationType {
		EMAIL(1), // index 0
		MOBILEAPP(2), // index 0
		SMS(3);
		
		private final int nmfatypecode;

		private AuthenticationType(int nmfatypecode) {
			this.nmfatypecode = nmfatypecode;
		}

		public int getNmfatypecode() {
			return nmfatypecode;
		}
	}

	//ALPD-6042--Added by vignesh(09-07-2025)--for MFASettings table
	public enum MFASettings {

		OTP_EXPIRY_MINUTES(1),
		EMAIL_EDIT_OPTION(2);
		
		private  final  int nmfasettingcode;

		public int getNmfasettingcode() {
			return nmfasettingcode;
		}

		private MFASettings(int nmfasettingcode) {
			this.nmfasettingcode = nmfasettingcode;
		}

	}

	//ALPDJ21-31-->Added by Vignesh(23-07-2025)-->Added the new dropdown Instrument Type-Standard, Storage
		public enum InstrumentType {

			NA(-1),
			STANDARD_INSTRUMENT(1),
			STORAGE_INSTRUMENT(2);
			
			private  final  int ninstrumenttypecode;

			public int getNinstrumenttypecode() {
				return ninstrumenttypecode;
}

			private InstrumentType(int ninstrumenttypecode) {
				this.ninstrumenttypecode = ninstrumenttypecode;
			}

		}

}
