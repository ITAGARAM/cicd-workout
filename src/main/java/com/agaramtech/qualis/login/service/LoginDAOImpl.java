package com.agaramtech.qualis.login.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.ADSSettings;
import com.agaramtech.qualis.configuration.model.GenericLabel;
import com.agaramtech.qualis.configuration.model.IntegrationSettings;
import com.agaramtech.qualis.configuration.model.Language;
import com.agaramtech.qualis.configuration.model.LicenseConfiguration;
import com.agaramtech.qualis.configuration.model.LimsElnSiteMapping;
import com.agaramtech.qualis.configuration.model.MfaSettings;
import com.agaramtech.qualis.configuration.model.PasswordPolicy;
import com.agaramtech.qualis.configuration.model.ReportSettings;
import com.agaramtech.qualis.configuration.model.SDMSELNSettings;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.configuration.model.UserRolePolicy;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.LimsElnUserMapping;
import com.agaramtech.qualis.credential.model.LoginType;
import com.agaramtech.qualis.credential.model.QualisForms;
import com.agaramtech.qualis.credential.model.QualisMenu;
import com.agaramtech.qualis.credential.model.QualisModule;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.credential.model.SiteConfig;
import com.agaramtech.qualis.credential.model.UserFile;
import com.agaramtech.qualis.credential.model.UserMultiDeputy;
import com.agaramtech.qualis.credential.model.UserMultiRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.credential.model.UsersSite;
import com.agaramtech.qualis.emailmanagement.model.EmailHost;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.JwtUtilityFunction;
import com.agaramtech.qualis.global.PasswordUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.login.model.FilterOperator;
import com.agaramtech.qualis.login.model.OneTimePassword;
import com.agaramtech.qualis.login.model.SessionDetails;
import com.agaramtech.qualis.login.model.UserMFA;
import com.agaramtech.qualis.login.model.UserUiConfig;
import com.agaramtech.qualis.registration.model.TransactionValidation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Transport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class LoginDAOImpl implements LoginDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoginDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final JwtUtilityFunction jwtUtilityFunction;
	private final PasswordUtilityFunction passwordUtilityFunction;
	//private final EmailHostDAO emailHostDAO;
	
	@Value("${spring.datasource.username}")
	private String databaseUserName;
	@Value("${spring.datasource.password}")
	private String databasePassword;
	@Value("${spring.datasource.url}")
	private String databaseConnectionUrl;

	@Override
	public ResponseEntity<Object> getLoginInfo() throws Exception {
		
		final Map<String, Object> map = new HashMap<String, Object>();
		
		String sQuery = "select nlogintypecode,jsondata, ndefaultstatus from logintype where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		final List<LoginType> lstLoginType = jdbcTemplate.query(sQuery, new LoginType());
		if(lstLoginType.size()>0) {
			map.put("LoginType", lstLoginType);			
		}	

		sQuery = "select nlanguagecode, slanguagename, sfilename, slanguagetypecode, sreportingtoolfilename, ndefaultstatus, nsitecode, nstatus from language where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		final List<Language> lstLanguage = jdbcTemplate.query(sQuery, new Language());		
		
		if (lstLanguage.size() > 0) {
			map.put("Language", lstLanguage);
			
		//Added for sonia on 16th June 2025 for jira id:ALPD-6028 (Captcha Validation)
			final String settingsQuery = "select * from settings where nsettingcode ="+ Enumeration.Settings.CAPTCHA.getNsettingcode()
										  + " and nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(settingsQuery, Settings.class,jdbcTemplate);
			
		if(objSettings !=null) {
				map.put("Captcha", objSettings);
		}		
		
		} else {
			return new ResponseEntity<>("Language not available", HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getLoginValidation(final String sloginid, final Language language,final int nlogintypecode) throws Exception {
		
		Map<String, Object> objMap = new HashMap<>();
		
		if (sloginid != null && !sloginid.isEmpty()) {
			final String sUserQuery = "select nusercode,sloginid,ntransactionstatus,nlockmode,nlogintypecode from users "
					+ "where LOWER(sloginid) =LOWER('" + sloginid + "') and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final Users objUsers = (Users) jdbcUtilityFunction.queryForObject(sUserQuery, Users.class, jdbcTemplate);;
			
			if (objUsers != null) {
				if (objUsers.getNtransactionstatus() == Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_LOGINIDDEACTIVATED", language.getSfilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else if (objUsers.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
						.gettransactionstatus()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_LOGINUSERRETIRED", language.getSfilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else if (objUsers.getNlockmode() == Enumeration.TransactionStatus.LOCK.gettransactionstatus()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_LOGINIDLOCKED", language.getSfilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					final String sSiteQuery = " select s.ssitename,us.nusersitecode,s.nsitecode,us.ndefaultsite from userssite us,site s "
							+ " where us.nsitecode=s.nsitecode and us.nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  us.nusercode= "
							+ objUsers.getNusercode() + " and s.nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " order by us.ndefaultsite asc;";
					final List<UsersSite> lstSite = (List<UsersSite>) jdbcTemplate.query(sSiteQuery, new UsersSite());
					
					if (!lstSite.isEmpty()) {
						String sSubQry = "";
						if (objUsers.getNusercode() > -1) {
							sSubQry = " and ur.nuserrolecode <> -1 ";
						}

						final String sMultiRoleQry = "select umr.nusermultirolecode,umr.ndefaultrole,umr.nuserrolecode,ur.suserrolename,umr.ntransactionstatus,"
								+ "us.nusercode from usermultirole umr,userrole ur ,userssite us where umr.nuserrolecode = ur.nuserrolecode"
								+ " and us.nusersitecode=umr.nusersitecode " + "and umr.nusersitecode="
								+ lstSite.get(0).getNusersitecode() + " and umr.ntransactionstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + sSubQry
								+ " order by umr.ndefaultrole asc";
						final List<UserMultiRole> lstMultiRole = (List<UserMultiRole>) jdbcTemplate.query(sMultiRoleQry,	new UserMultiRole());
						
						final List<UserMultiRole> lstDefaultRole = lstMultiRole.stream().filter(value -> value
								.getNdefaultrole() == Enumeration.TransactionStatus.YES.gettransactionstatus())
								.collect(Collectors.toList());
						
					
						if (lstDefaultRole != null && lstDefaultRole.size() > 0) {					
							
							UserMFA objUsersAuthentication = null;
							int nmfaNeed = -1;
							int nmfaTypeCode = -1;
							
							if(objUsers.getNusercode() > 0)
							{
								//Start - Block to execute for users other than system user
								
					//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
						
								final String mfaSettingQuery = "select ssettingvalue from settings where nsettingcode ="+ Enumeration.Settings.MFA.getNsettingcode() 
																+ " and nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
								final String sMfaNeed = (String) jdbcUtilityFunction.queryForObject(mfaSettingQuery, String.class, jdbcTemplate);
					
								nmfaNeed = Integer.parseInt(sMfaNeed);
								objMap.put("mfaNeed", nmfaNeed);
						
								objUsersAuthentication = projectDAOSupport.getUserMFA((int)  objUsers.getNusercode(), nmfaNeed, false);
								
							
//						if(nmfaNeeded==Enumeration.TransactionStatus.YES.gettransactionstatus() && objUsersAuthentication==null) {
//							objMap.put("newUserAuth", true);
//							objMap.put("showEmailOTPModal", true);
//							UserInfo userInfo=new UserInfo();
//									objMap.putAll(getMFAType(userInfo));
//						}
//						else 
						if(objUsersAuthentication!=null) {
									nmfaTypeCode = objUsersAuthentication.getNmfatypecode();
						}
								objMap.put("nmfatype", nmfaTypeCode);

								//End - Block to execute for users other than system user
							}
							
							final String spassword = projectDAOSupport.decryptPassword("usermultirole", "nusermultirolecode",
									lstDefaultRole.get(0).getNusermultirolecode(), "spassword");

							if (spassword != null) {
								if (spassword.equals(Enumeration.ReturnStatus.FAILED.getreturnstatus())) {
									return new ResponseEntity<>(commonFunction
											.getMultilingualMessage("IDS_EXECUTESYMMETRICKEY", language.getSfilename()),
											HttpStatus.EXPECTATION_FAILED);
								} else {
									objMap.put("PassFlag", Enumeration.PasswordValidate.PASS.getPaswordvalidate());
								}
							} else {
								
								//Start - Block to execute for users other than system user
								if(objUsers.getNusercode() > 0)
								{
				//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
				//start
									if(objUsersAuthentication == null) 
									{
										//Logged in User is not defined with a MFA Type
										if(nmfaNeed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
											
												objMap.put("newUserAuth", true);
												
											final UserInfo userInfo = new UserInfo();
											objMap.putAll(projectDAOSupport.getMFAType(userInfo));
											
												}
										}
											else  {
										//Logged in User is defined with a MFA Type
										if(nmfaNeed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
													objMap.put("newUserAuth", false);
												}
//										nmfaTypeCode = objUsersAuthentication.getNmfatypecode();
//										objMap.put("nmfatype", nmfaTypeCode);

									}
								}
								//End - Block to execute for users other than system user	
					//end
								objMap.put("PassFlag", Enumeration.PasswordValidate.NEW_USER.getPaswordvalidate());
								final PasswordPolicy objPasswordPolicy = getPasswordPolicyMsg(
										lstDefaultRole.get(0).getNuserrolecode());
								objMap.put("PasswordPolicy", objPasswordPolicy);
							}
						} else {
							objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
									.getMultilingualMessage("IDS_DEFAULTROLEISNOTAVAILABLE", language.getSfilename()));
						}
						objMap.put("UserMultiRole", lstMultiRole);
					}
					objMap.put("Users", objUsers);
					objMap.put("Site", lstSite);
					return new ResponseEntity<>(objMap, HttpStatus.OK);
				}
			} else {
				final String settingQry="select ssettingvalue from settings where nsettingcode= "
									+ Enumeration.Settings.LOGINADSSYNC.getNsettingcode() 
									+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
				final Settings adsVal = (Settings) jdbcUtilityFunction.queryForObject(settingQry, Settings.class,jdbcTemplate);
				int adsSettingValue= -1;

				if(adsVal != null) {
			    	adsSettingValue=Integer.parseInt(adsVal.getSsettingvalue());
			    }
	
				if (Enumeration.TransactionStatus.YES.gettransactionstatus() == adsSettingValue &&  nlogintypecode == Enumeration.LoginType.ADS.getnlogintype()) {
					objMap.put("NewAdsUser", "true");
					return new ResponseEntity<>(objMap, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(Enumeration.ReturnStatus.INVALIDUSER.getreturnstatus(),
							HttpStatus.EXPECTATION_FAILED);
				}
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_LOGINIDSHOULDNOTBEEMPTY", language.getSfilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	private PasswordPolicy getPasswordPolicyMsg(int nuserrolecode) throws Exception {
		PasswordPolicy objPasswordPolicy = new PasswordPolicy();
		if (nuserrolecode != 0) {
			String sQuery = "select pp.npolicycode,pp.nminnoofnumberchar,pp.nminnooflowerchar,pp.nminnoofupperchar,pp.nminnoofspecialchar,pp.nminpasslength,"
					+ "pp.nmaxpasslength,pp.nnooffailedattempt,pp.nexpirypolicy,pp.nremainderdays,pp.scomments,pp.nminpasslength,pp.nmaxpasslength "
					+ " from passwordpolicy pp,userrolepolicy up,transactionstatus ts where  pp.npolicycode=up.npolicycode"
					+ " and up.nuserrolecode=" + nuserrolecode + " and up.ntransactionstatus=ts.ntranscode"
					+ " and up.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and up.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and pp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " order by npolicycode desc";
			objPasswordPolicy = (PasswordPolicy) jdbcUtilityFunction.queryForObject(sQuery, PasswordPolicy.class,jdbcTemplate);
		}
		return objPasswordPolicy;
	}

	@Override
	public ResponseEntity<Object> collectLoginData(Map<String, Object> objinputmap, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		final int nusercode = (int) objinputmap.get("nusercode");
		final String spassword = (String) objinputmap.get("spassword");
		final int nusermultisitecode = (int) objinputmap.get("nusermultisitecode");
		final String slanguageTypeCode = (String) objinputmap.get("slanguagetypecode");
		final int nuserrolecode = (int) objinputmap.get("nuserrolecode");
		boolean isNeedNewPswd = false;
		final Map<String, Object> objMap = new HashMap<>();
		final String strLanguage = "select nlanguagecode, slanguagename, sfilename, slanguagetypecode, sreportingtoolfilename, ndefaultstatus, nsitecode, nstatus from Language where slanguagetypecode= N'"
				+ (String) objinputmap.get("slanguagetypecode") + "';";
		final Language objLanguage = (Language) jdbcUtilityFunction.queryForObject(strLanguage, Language.class,jdbcTemplate);
		
		
		final Map<String, Object> obj = validateUserStatus(nusercode, nuserrolecode, nusermultisitecode);
		String returnStr = (String) obj.get("rtn");
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(returnStr)) {

			final Boolean obj1 = validateSite(nusercode, (int) objinputmap.get("nsitecode"));
			
			if (obj1) {
				String sQuery = "select umr.nusermultirolecode ,us.nsitecode,umr.nuserrolecode ,umr.ndefaultrole,ur.suserrolename,umr.spassword,"
						+ "umr.ntransactionstatus,us.nusercode,us.nusersitecode, umr.nnooffailedattempt"
						+ " from usermultirole umr,userrole ur ,userssite us"
						+ " where umr.nuserrolecode = ur.nuserrolecode and us.nusersitecode=umr.nusersitecode"
						+ " and umr.nusersitecode=" + nusermultisitecode + " and umr.ntransactionstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.ndefaultrole = "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and us.nusercode=" + nusercode
						+ " and umr.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final UserMultiRole objUserMultiRole = (UserMultiRole) jdbcUtilityFunction.queryForObject (sQuery, UserMultiRole.class,jdbcTemplate);
				if (objUserMultiRole != null) {
					final String suserpassword = projectDAOSupport.decryptPassword("usermultirole", "nusermultirolecode",
							objUserMultiRole.getNusermultirolecode(), "spassword");
					if (suserpassword != null && !suserpassword.isEmpty()) {

						final String siteQuery = " select case when s.nismultisite = 3 then s.nsitecode else -1 end nismultisite, s.nsitecode,s.ssitename, s.ntimezonecode, us.nusercode,tzd.sdatetimeformat,tzd.stimezoneid, tzd.sgmtoffset"
								+ " from userssite us, site s,timezone tzd where s.nsitecode = us.nsitecode and tzd.ntimezonecode = s.ntimezonecode "
								+ " and tzd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and us.nusersitecode = " + nusermultisitecode;

						final Site objSite = (Site) jdbcUtilityFunction.queryForObject(siteQuery, Site.class, jdbcTemplate);

						Instant currenDate = null;
						if (objSite.getNtimezonecode() > 0) {
							currenDate = LocalDateTime.now().atZone(ZoneId.of(objSite.getStimezoneid())).toInstant()
									.truncatedTo(ChronoUnit.SECONDS);
						} else {
							currenDate = LocalDateTime.now().toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
						}

						sQuery = "select DATE_PART('day', (select COALESCE(dpasswordvalidatedate,'" + currenDate + "') "
								+ "from usermultirole where nusersitecode = " + nusermultisitecode
								+ " and ndefaultrole = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " )- '" + currenDate + "')";
						final int Passwordexpireremaindays = (int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class,jdbcTemplate);
						
						final String passwordpolicyrolequery = "select up.npolicycode,pp.nexpirypolicyrequired,pp.nremainderdays from userrolepolicy up,usermultirole umr,passwordpolicy pp"
								+ " where umr.nuserrolecode=up.nuserrolecode and pp.npolicycode = up.npolicycode"
								+ " and up.ntransactionstatus= "
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
								+ " and umr.nusersitecode=" + nusermultisitecode + " and umr.ndefaultrole = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and umr.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and up.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pp.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final PasswordPolicy objPasswordPolicy = (PasswordPolicy) jdbcUtilityFunction.queryForObject(passwordpolicyrolequery, PasswordPolicy.class,jdbcTemplate);
						if (objPasswordPolicy != null) {
							if (objPasswordPolicy.getNexpirypolicyrequired() == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
								if (Passwordexpireremaindays <= objPasswordPolicy.getNremainderdays()
										&& Passwordexpireremaindays > 0) {
									objMap.put("PasswordAlertDay", Passwordexpireremaindays);
								} else if (Passwordexpireremaindays <= 0) {
									objMap.put("PassFlag", Enumeration.PasswordValidate.EXPIRED.getPaswordvalidate());
									isNeedNewPswd = true;
								}
							}
							if (!isNeedNewPswd) {
								Map<String, Object> mapUserInfo = collectUserInfo(objinputmap);
								if (mapUserInfo == null) {
									return new ResponseEntity<>("Language not available", HttpStatus.CONFLICT);
								} else {
									final UserInfo userInfo = (UserInfo) mapUserInfo.get("UserInfo");
									HttpSession session = request.getSession(true);
									userInfo.setSsessionid(session.getId());
//									userInfo.setSsessionid(UUID.randomUUID().toString());
									userInfo.setShostip(request.getRemoteAddr());
									userInfo.setShostip(request.getRemoteAddr());
									clearsessiondetail(userInfo);
									
									
									if (suserpassword.equals(spassword)) {
										String updateQry = "update usermultirole set nnooffailedattempt = "
												+ "(select nnooffailedattempt from passwordpolicy where npolicycode = "
												+ objPasswordPolicy.getNpolicycode() + ")"
												+ " where nusermultirolecode = (select nusermultirolecode from usermultirole "
												+ " where nusersitecode = " + nusermultisitecode
												+ " and ndefaultrole = "
												+ Enumeration.TransactionStatus.YES.gettransactionstatus()
												+ " and nstatus = "
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
										jdbcTemplate.update(updateQry);
										
										sQuery = "select nusermultirolecode, nusersitecode, nuserrolecode from usermultirole"
												+ " where nusermultirolecode = "
												+ (int) objinputmap.get("nusermultirolecode") + " and nstatus = "
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
										UserMultiRole objUserMultiRoleTemp = (UserMultiRole) jdbcUtilityFunction.queryForObject(sQuery, UserMultiRole.class, jdbcTemplate);
										mapUserInfo.put("UserInfo", userInfo);
//										String SiteAdditionalInfo = (String) ((Map<String, Object>) mapUserInfo
//												.get("Settings")).get((short) 23);

										objMap.putAll(getUserForms(nusercode, objUserMultiRoleTemp.getNuserrolecode(),
												userInfo.getNmastersitecode(), slanguageTypeCode));
										objMap.putAll(getControlRights(objUserMultiRoleTemp.getNuserrolecode(),
												nusercode, userInfo.getNmastersitecode(), slanguageTypeCode, userInfo));
										objMap.putAll(getTransactionStatus(userInfo));
										objMap.putAll(getLoggedUserProfile(userInfo));
										objMap.putAll(mapUserInfo);
										
										// Added by gowtham on 18 July, ALPDJ21-27 - JWT
										objMap.put("token",jwtUtilityFunction.generateToken(userInfo.getSsessionid(), userInfo.getNexpirytime()));
									    
										Map<String, Object> outputMap = insertSessionDetails(userInfo, 1);
										auditUtilityFunction.insertAuditAction(userInfo, "IDS_LOGIN", commonFunction.getMultilingualMessage(
												"IDS_LOGIN", userInfo.getSlanguagefilename()), outputMap);
									} else {
										int nremainingattempt = objUserMultiRole.getNnooffailedattempt() - 1;
										if (nremainingattempt >= 0) {
											String updateQry = "update usermultirole set nnooffailedattempt = "
													+ nremainingattempt
													+ " where nusermultirolecode = (select um.nusermultirolecode from usermultirole um"
													+ " where um.nusersitecode = " + nusermultisitecode
													+ " and ndefaultrole = "
													+ Enumeration.TransactionStatus.YES.gettransactionstatus()
													+ " and nstatus = "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
											jdbcTemplate.update(updateQry);
										}
										final List<String> listOfMultilingual = commonFunction
												.getMultilingualMultipleMessage(Arrays.asList("IDS_WRONGCREDENTIAL",
														"IDS_YOUARELEFTWITH", "IDS_MOREATTEMPTS", "IDS_LOGINID",
														"IDS_USERNAME", "IDS_ENTEREDINCORRECTPSWD",
														"IDS_DUETOWRONGCREDENTIAL", "IDS_LOGINIDLOCKED"),
														userInfo.getSlanguagefilename());
										String sComments = listOfMultilingual.get(3) + ": " + userInfo.getSloginid()
												+ ";" + listOfMultilingual.get(4) + ": " + userInfo.getSusername()
												+ ";";
										if (nremainingattempt == 0) {
											jdbcTemplate.update("update users set nlockmode = "
													+ Enumeration.TransactionStatus.LOCK.gettransactionstatus()
													+ " where nusercode = " + nusercode);
											returnStr = listOfMultilingual.get(7);
											sComments = sComments + " " + returnStr + " " + listOfMultilingual.get(6)
													+ ";";
										} else {
											returnStr = listOfMultilingual.get(0) + ". " + listOfMultilingual.get(1)
													+ " " + nremainingattempt + " " + listOfMultilingual.get(2) + ".";
											sComments = sComments + " " + listOfMultilingual.get(5);
										}
										Map<String, Object> inputMap = new HashMap<>();
										inputMap.put("sprimarykeyvalue", -1);
										inputMap.put("stablename", "login");
									auditUtilityFunction.insertAuditAction(userInfo, "IDS_WRONGCREDENTIAL", sComments, inputMap);
									}
								}
							}
						} else {
							returnStr = commonFunction.getMultilingualMessage("IDS_PASSWORDPOLICYNOTAVAILABLE",
									objLanguage.getSfilename());
						}
					} else {
						objMap.put("PassFlag", Enumeration.PasswordValidate.NEW_USER.getPaswordvalidate());
						isNeedNewPswd = true;
					}
					if (isNeedNewPswd) {
						Map<String, Object> mapUserInfo = collectUserInfo(objinputmap);
						final UserInfo userInfo = (UserInfo) mapUserInfo.get("UserInfo");
						objMap.put("UserInfo", userInfo);
						PasswordPolicy objPasswordPolicy = getPasswordPolicyMsg(objUserMultiRole.getNuserrolecode());
						objMap.put("PasswordPolicy", objPasswordPolicy);
					}
				} else {
					returnStr = commonFunction.getMultilingualMessage("IDS_USERMULTIROLENOTAVAILABLE",
							objLanguage.getSfilename());
				}
			} else {
				returnStr = commonFunction.getMultilingualMessage("IDS_INVALIDSITETOLOGIN", objLanguage.getSfilename());

			}

		} else {
			returnStr = commonFunction.getMultilingualMessage(returnStr, objLanguage.getSfilename());
		}
		objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), returnStr);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	private Boolean validateSite(int nusercode, int nsitecode) throws Exception {

		boolean flag = true;

		String sQuery = " select sc.* from siteconfig sc,site s,settings s1 "
				+ " where  sc.nsitecode = s.nsitecode "
				+ " and sc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and s1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and s1.ssettingvalue=s.ssitecode "
				+ " and s1.nsettingcode=30 ";
		
		SiteConfig objSiteConfig = (SiteConfig) jdbcUtilityFunction.queryForObject(sQuery, SiteConfig.class, jdbcTemplate);

		if (objSiteConfig!=null &&  objSiteConfig.getNisstandaloneserver() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			if(objSiteConfig.getNsitecode()!=nsitecode)
			{
				flag =false;
			}
			
		}
		return flag;
	}

	private Map<String, Object> insertSessionDetails(final UserInfo userInfo, final int nFlag) throws Exception {
		if (nFlag == 1) {
			// Commented by Gowtham R on 24 July 2025 - ALPDJ21-35 - Removed Username Column from sessiondetails table.
//			String sInsertQuery = "insert into sessiondetails (ssessionid, susername, shostip, nusercode, dlogindate, dlogoutdate, nuserrolecode, ntransactionstatus, nsitecode, nstatus,dmodifieddate)"
//					+ " values (N'" + userInfo.getSsessionid() + "', N'" + stringUtilityFunction.replaceQuote(userInfo.getSusername())
//					+ "', N'" + userInfo.getShostip() + "', " + userInfo.getNusercode() + "," + " N'"
//					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'::timestamp, NULL, " + userInfo.getNuserrole() + ", 170, "
//					+ userInfo.getNsitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "');";
//			jdbcTemplate.execute(sInsertQuery);
			String sInsertQuery = "insert into sessiondetails (ssessionid, shostip, nusercode, dlogindate, dlogoutdate, nuserrolecode, ntransactionstatus, nsitecode, nstatus,dmodifieddate)"
					+ " values (N'" + userInfo.getSsessionid() + "', N'" + userInfo.getShostip() + "', " + userInfo.getNusercode() + "," + " N'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'::timestamp, NULL, " + userInfo.getNuserrole() + ", 170, "
					+ userInfo.getNsitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "');";
			jdbcTemplate.execute(sInsertQuery);
		} else if (nFlag == 2) {
			String Que = "delete from sessiondetails where  ssessionid =N'" + userInfo.getSsessionid() + "' ";
			jdbcTemplate.execute(Que);
			
			//ALPD-4913-To detele the saved filter dynamic data when session delete	
			final Map<String, Object> deleteMap = new HashMap<>();
			deleteMap.put("clearTempFilter", true);
			projectDAOSupport.createFilterSubmit(deleteMap,userInfo);	
		} else {

		}
		Map<String, Object> outputMap = new HashMap<>();
		outputMap.put("stablename", "sessiondetails");
		outputMap.put("sprimarykeyvalue", -1);
		return outputMap;
	}

	public void clearsessiondetail(final UserInfo userInfo) throws Exception {
		JSONObject objJsonData = new JSONObject();
		JSONObject jsoncomments = new JSONObject();
		Map<String, Object> sortedOldmap = new LinkedHashMap<>();
		String strQuery = "";
		final String slanguagefilename = userInfo.getSlanguagefilename();
		Instant utcDate = // getCurrentDateTime(userInfo);
			dateUtilityFunction.getUTCDateTime();
		strQuery = " select * from sessiondetails where (DATE_PART('day', N'" + utcDate
				+ "'::timestamptz - dlogindate) * 24 + " + "  DATE_PART('hour', N'" + utcDate
				+ "'::timestamptz - dlogindate)) * 60 + " + "  DATE_PART('minute', N'" + utcDate
				+ "'::timestamptz - dlogindate)>20;";

		List<SessionDetails> lstSessionDetail = (List<SessionDetails>) jdbcTemplate.query(strQuery, new SessionDetails());
		
		if (!lstSessionDetail.isEmpty()) {
			String getAuditLastRecord = "select nsequenceno from seqnoaudittrail "
					// + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
					+ " where stablename = 'auditaction';";
			int lastValueFromAuditTable = (int) jdbcUtilityFunction.queryForObject(getAuditLastRecord, Integer.class,jdbcTemplate);
			StringBuilder sb = new StringBuilder();
			String sourceFormat = "yyyy-MM-dd HH:mm:ss";
			final DateTimeFormatter oldPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			final DateTimeFormatter newPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final DateTimeFormatter destFormat = DateTimeFormatter.ofPattern("HH:00 (h a)");
			final LocalDateTime datetime = LocalDateTime.ofInstant(utcDate.truncatedTo(ChronoUnit.SECONDS),
					ZoneOffset.UTC);
			final String formatted = DateTimeFormatter.ofPattern(sourceFormat).format(datetime);

			String sauditdate = LocalDateTime.parse(formatted, oldPattern).format(newPattern);
			String sday = String.valueOf(datetime.getDayOfMonth()).length() == 1
					? "0" + String.valueOf(datetime.getDayOfMonth())
					: String.valueOf(datetime.getDayOfMonth());
			String smonth = String.valueOf(datetime.getMonthValue()).length() == 1
					? "0" + String.valueOf(datetime.getMonthValue())
					: String.valueOf(datetime.getMonthValue());
			String syear = String.valueOf(datetime.getYear());
			String shour = String.valueOf(destFormat.format(datetime));

			for (int i = 0; i < lstSessionDetail.size(); i++) {
				lastValueFromAuditTable++;
				sb.append("insert into auditaction (nauditcode,nusercode,nuserrolecode,ndeputyusercode "
						+ ",ndeputyrolecode,nformcode,nmodulecode,nreasoncode "
						+ ",stablename,dauditdate,sactiontype,sauditaction, "
						+ "stransactionno,sreason,nsitecode,nstatus,sday,smonth,syear,sauditdate,shour) values("
						+ lastValueFromAuditTable + "," + lstSessionDetail.get(i).getNusercode() + ","
						+ lstSessionDetail.get(i).getNuserrolecode() + "," + lstSessionDetail.get(i).getNusercode()
						+ "," + lstSessionDetail.get(i).getNuserrolecode() + ","
						+ Enumeration.FormCode.USERS.getFormCode() + ","
						+ Enumeration.ModuleCode.USERMANAGEMENT.getModuleCode() + ", -1" + ", N'sessiondetails','"
						+ utcDate + "','"
						+ commonFunction.getMultilingualMessage("IDS_SYSTEM", slanguagefilename).toUpperCase() + "','"
						+ commonFunction.getMultilingualMessage("IDS_LOGOUT", slanguagefilename) + "'," + "'-1', NULL ,"
						+ lstSessionDetail.get(i).getNsitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + ",'" + sday + "','"
						+ smonth + "','" + syear + "','" + sauditdate + "','" + shour + "');");

				// Data needed for Get start
				objJsonData.put("nauditcode", lastValueFromAuditTable);
				objJsonData.put("sauditaction", commonFunction.getMultilingualMessage("IDS_LOGOUT", slanguagefilename));
				objJsonData.put("sauditdate", sauditdate);
				objJsonData.put("scomments",
						commonFunction.getMultilingualMessage("IDS_IMPROPERLOGOUT", slanguagefilename));
				objJsonData.put("susername", userInfo.getSusername());
				objJsonData.put("suserrolename", userInfo.getSuserrolename());
				objJsonData.put("susername", userInfo.getSdeputyusername());
				objJsonData.put("suserrolename", userInfo.getSdeputyuserrolename());
				objJsonData.put("sactiontype",
						commonFunction.getMultilingualMessage("IDS_SYSTEM", slanguagefilename).toUpperCase());
				objJsonData.put("viewPeriod", shour);
				objJsonData
						.put("sformname",
								jdbcTemplate.queryForObject(
										"select jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
												+ "' from" + "  qualisforms where nformcode=" + userInfo.getNformcode(),
										String.class));
				objJsonData.put("smodulename",
						jdbcTemplate.queryForObject(
								"select jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' from"
										+ "  qualismodule where nmodulecode=" + userInfo.getNmodulecode(),
								String.class));
				objJsonData.put("sreason", "NULL");
				objJsonData.put("jsoncomments",
						commonFunction.getMultilingualMessage("IDS_IMPROPERLOGOUT", slanguagefilename));
				objJsonData.put("slanguagename", userInfo.getSlanguagename());

				sortedOldmap.put("User Name", userInfo.getSfirstname() + userInfo.getSlastname());
				sortedOldmap.put("Login ID", userInfo.getSloginid()); // Data needed for get End
				jsoncomments.put("data", new JSONObject(sortedOldmap));
				JSONArray array = new JSONArray(sortedOldmap.keySet().toArray());
				jsoncomments.put("keys", array);

				sb.append(" insert into auditcomments (nauditcode ,scomments ,jsondata ,jsoncomments,nstatus) values("
						+ lastValueFromAuditTable + ",'"
						+ commonFunction.getMultilingualMessage("IDS_IMPROPERLOGOUT", slanguagefilename) + "','"
						+ stringUtilityFunction.replaceQuote(objJsonData.toString()) + "','" + stringUtilityFunction.replaceQuote(jsoncomments.toString()) + "' ,"
						+ +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");
			}

			sb.append("update seqnoaudittrail set nsequenceno = " + lastValueFromAuditTable
					+ " where stablename = N'auditaction';");

			// sb.append("delete from sessiondetails where datediff(minute, dlogindate, N'"
			// + utcDate + "') > 20;");
			sb.append("delete from sessiondetails where (DATE_PART('day', N'" + utcDate
					+ "'::timestamptz - dlogindate) * 24 + " + "  DATE_PART('hour', N'" + utcDate
					+ "'::timestamptz - dlogindate)) * 60 + " + "  DATE_PART('minute', N'" + utcDate
					+ "'::timestamptz - dlogindate) > 20;");
			jdbcTemplate.execute(sb.toString());
			
			//ALPD-4913-To detele the saved filter dynamic data when session delete	
			final Map<String, Object> deleteMap = new HashMap<>();
			deleteMap.put("clearTempFilter", true);
			projectDAOSupport.createFilterSubmit(deleteMap,userInfo);	
		}
	}

	@Override
	public ResponseEntity<Object> collectAdsLoginData(Map<String, Object> objinputmap) throws Exception {
		final int nusercode = (int) objinputmap.get("nusercode");
		final String slanguageTypeCode = (String) objinputmap.get("slanguagetypecode");
		String sComments = "";
		String sQuery = "select * from users where nusercode = " + nusercode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Users objUsers = (Users) jdbcUtilityFunction.queryForObject(sQuery, Users.class, jdbcTemplate);
		if (objUsers != null) {
			if (objUsers.getNtransactionstatus() == Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADSINACTIVEUSER",
						(String) objinputmap.get("slanguageTypeCode")), HttpStatus.CONFLICT);
			} else {
				final String slanguagefilename = (String) objinputmap.get("slanguagefilename");
				String str = validateADSPassword((String) objinputmap.get("sloginid"),
						(String) objinputmap.get("spassword"), slanguagefilename);
				Map<String, Object> mapUserInfo = collectUserInfo(objinputmap);
				final UserInfo objUserInfo = (UserInfo) mapUserInfo.get("UserInfo");
				final List<String> listOfMultilingual = commonFunction
						.getMultilingualMultipleMessage(
								Arrays.asList("IDS_WRONGCREDENTIAL", "IDS_YOUARELEFTWITH", "IDS_MOREATTEMPTS",
										"IDS_LOGINID", "IDS_USERNAME", "IDS_ENTEREDINCORRECTPSWD",
										"IDS_DUETOWRONGCREDENTIAL", "IDS_LOGINIDLOCKED"),
								objUserInfo.getSlanguagefilename());

				sComments = listOfMultilingual.get(3) + ": " + objUserInfo.getSloginid() + ";"
						+ listOfMultilingual.get(4) + ": " + objUserInfo.getSusername() + ";";

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(str)) {
					Map<String, Object> objMap = new HashMap<>();
					sQuery = "select nusermultirolecode, nusersitecode, nuserrolecode from usermultirole"
							+ " where nusermultirolecode = " + (int) objinputmap.get("nusermultirolecode")
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					UserMultiRole objUserMultiRole = (UserMultiRole) jdbcUtilityFunction.queryForObject(sQuery, UserMultiRole.class,jdbcTemplate);
					if (objUserMultiRole != null) {
						objinputmap.put("nuserrolecode", objUserMultiRole.getNuserrolecode());
						objMap.putAll(getUserForms(nusercode, objUserMultiRole.getNuserrolecode(),
								objUserInfo.getNmastersitecode(), slanguageTypeCode));
						objMap.putAll(getControlRights(objUserMultiRole.getNuserrolecode(), nusercode,
								objUserInfo.getNmastersitecode(), slanguageTypeCode, objUserInfo));
						objMap.putAll(getTransactionStatus(objUserInfo));
						objMap.putAll(mapUserInfo);
					}
					objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							commonFunction.getMultilingualMessage(str, slanguagefilename));
					Map<String, Object> inputMap = new HashMap<>();
					inputMap.put("sprimarykeyvalue", -1);
					inputMap.put("stablename", "adslogin");
					auditUtilityFunction.insertAuditAction(objUserInfo, "IDS_ADSLOGIN", sComments, inputMap);
					return new ResponseEntity<>(objMap, HttpStatus.OK);
				} else {
					Map<String, Object> inputMap = new HashMap<>();
					inputMap.put("sprimarykeyvalue", -1);
					inputMap.put("stablename", "adslogin");
				auditUtilityFunction.insertAuditAction(objUserInfo,commonFunction.getMultilingualMessage("IDS_ADSLOGIN ", slanguagefilename)
									+ commonFunction.getMultilingualMessage(str, slanguagefilename),
							sComments, inputMap);
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(str, slanguagefilename),
							HttpStatus.EXPECTATION_FAILED);
				}
			}

		} else {
			return new ResponseEntity<>(Enumeration.ReturnStatus.INVALIDUSER.getreturnstatus(), HttpStatus.CONFLICT);
		}
	}

	private String validateADSPassword(final String sloginid, final String spassword, final String slanguagefilename)
			throws Exception {
		String strQuery = "select sldaplink,sdomainname from adssettings where nldapstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		try {
			List<ADSSettings> lstTblADSSettings = (List<ADSSettings>) jdbcTemplate.query(strQuery,new ADSSettings());
			if (!lstTblADSSettings.isEmpty()) {
				String str = "";
				for (int index = 0; index < lstTblADSSettings.size(); index++) {
					ADSSettings objdefault = lstTblADSSettings.get(index);
					objdefault.setSloginid(sloginid);
					objdefault.setSpassword(spassword);
					str = getAdsconnection(objdefault);
					if (str.contains(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
						break;
					}
				}
				return str;
			} else {
				return commonFunction.getMultilingualMessage("IDS_ADSSERVERUNAVAILABLE", slanguagefilename);
			}
		} catch (Exception e) {
			return commonFunction.getMultilingualMessage("IDS_ADSNOTSYNC", slanguagefilename);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> collectUserInfo (Map<String, Object> objInputMap) throws Exception {
		Map<String, Object> objMap = new HashMap<>();
		final int nusermultisitecode = (int) objInputMap.get("nusermultisitecode");
		final int nuserrolecode = (int) objInputMap.get("nuserrolecode");
		final int nusermultirolecode = (int) objInputMap.get("nusermultirolecode");
		final int nlogintypecode = (int) objInputMap.get("nlogintypecode");
		List<Object> activelanguagelist = new ArrayList<>();
		
		
		String suser = databaseUserName;
		String spsw = databasePassword;
		String surl = databaseConnectionUrl;
		String sDB = surl.substring(surl.lastIndexOf("/") + 1);
		String sserver = surl.substring(surl.indexOf("//") + 2, surl.lastIndexOf(":"));
		String sport = surl.substring(surl.lastIndexOf(":") + 1, surl.lastIndexOf("/"));
		String sConnStr = "Server=" + sserver + ";Port=" + sport + ";Database=" + sDB + ";User=" + suser + ";Pwd="
				+ spsw + ";";
		if (objInputMap.get("languageList") != null) {
			activelanguagelist = ((List<Map<String, Object>>) objInputMap.get("languageList")).stream()
					.map(lang -> lang.get("value")).collect(Collectors.toList());
		} else if (objInputMap.containsKey("userinfo")
				&& ((Map<String, Object>) objInputMap.get("userinfo")).containsKey("activelanguagelist")) {
			activelanguagelist = (List<Object>) ((Map<String, Object>) objInputMap.get("userinfo"))
					.get("activelanguagelist");
		} else {
			return null;
		}
		final String sDeputyUser = "select CONCAT(max(u.sfirstname),' ',max(u.slastname)) sdeputyname, u.nusercode,max(u.sloginid) sdeputyid,"
									+ " ur.suserrolename ,ur.nuserrolecode,umd.ndeputyusersitecode"
									+ " from usermultideputy umd, userssite us, users u ,userrole ur "
									+ " where umd.nusersitecode = us.nusersitecode " + " and us.nusercode = u.nusercode"
									+ " and us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and umd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									+ " and ur.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
									+ " and u.ntransactionstatus = " + Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()
									+ " and umd.ndeputyusersitecode = " + nusermultisitecode
									+ "  and umd.nuserrolecode=ur.nuserrolecode and umd.ntransactionstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " group by u.nusercode, umd.ndeputyusersitecode,ur.suserrolename,ur.nuserrolecode;;";

		final List<UserMultiDeputy> deptyList = (List<UserMultiDeputy>) jdbcTemplate.query(sDeputyUser,
				new UserMultiDeputy());
		if (!deptyList.isEmpty()) {
			final String sDeputyRole = "select u.nusercode,max(ur.nuserrolecode) nuserrolecode, "
									+ " max(ur.suserrolename) suserrolename, max(umd.ndeputyusersitecode) ndeputyusersitecode"
									+ " from usermultideputy umd, userssite us, users u, userrole ur"
									+ " where us.nusersitecode = umd.nusersitecode and us.nusercode = u.nusercode"
									+ " and ur.nuserrolecode = umd.nuserrolecode and ur.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umd.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.ntransactionstatus = "
									+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + " and umd.ndeputyusersitecode = "
									+ nusermultisitecode + " and umd.ntransactionstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " group by umd.ndeputyusersitecode,u.nusercode;";
			final List<UserMultiDeputy> userRoleList = (List<UserMultiDeputy>) jdbcTemplate.query(sDeputyRole,
					new UserMultiDeputy());
			deptyList.forEach(deputyObj -> {
				final List<UserMultiDeputy> deptyRoleList = userRoleList.stream()
						.filter(deputyRole -> deputyRole.getNusercode() == deputyObj.getNusercode())
						.collect(Collectors.toList());
				deputyObj.setLstUserMultiDeputy(deptyRoleList);
			});
		}
		objMap.put("DeputyUser", deptyList);

		String querymultirole = "";
		if (nlogintypecode == Enumeration.LoginType.ADS.getnlogintype()) {
			querymultirole = "select umr.nusermultirolecode, umr.nuserrolecode, ur.suserrolename"
							+ " from usermultirole umr,userrole ur ,userssite us "
							+ " where umr.nuserrolecode = ur.nuserrolecode and  us.nusersitecode=umr.nusersitecode "
							+ " and umr.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and umr.nusersitecode = " + nusermultisitecode + " and umr.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nuserrolecode not in("
							+ nuserrolecode + "," + Enumeration.TransactionStatus.NA.gettransactionstatus()
							+ ") and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and us.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				} else {
			querymultirole = "select umr.nusermultirolecode, umr.nuserrolecode, ur.suserrolename"
								+ " from usermultirole umr,userrole ur ,userssite us,userrolepolicy urp"
								+ " where umr.nuserrolecode = ur.nuserrolecode"
								+ " and urp.nuserrolecode=ur.nuserrolecode and  us.nusersitecode=umr.nusersitecode "
								+ " and umr.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and umr.nusersitecode = " + nusermultisitecode + " and umr.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nuserrolecode not in("
								+ nuserrolecode + "," + Enumeration.TransactionStatus.NA.gettransactionstatus()
								+ ") and urp.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
								+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and us.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and urp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					}
		final List<UserMultiRole> userMultiRole = (List<UserMultiRole>) jdbcTemplate.query(querymultirole,
				new UserMultiRole());
		objMap.put("UserMultiRole", userMultiRole);

		String sQuery = "select umr.nusermultirolecode ,umr.nuserrolecode ,umr.ndefaultrole,umr.spassword,umr.ntransactionstatus,"
							+ "CONCAT(u.sfirstname,' ',u.slastname) susername,u.sfirstname,u.slastname, ur.suserrolename, umr.nusersitecode, u.sloginid"
							+ " from usermultirole umr, userssite us, users u, userrole ur" + " where umr.nusermultirolecode = "
							+ nusermultirolecode + " and us.nusersitecode = umr.nusersitecode and us.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ur.nuserrolecode = umr.nuserrolecode and ur.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and u.nusercode = us.nusercode and u.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.ntransactionstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final UserMultiRole objUserMultiRole = (UserMultiRole) jdbcTemplate.queryForObject(sQuery,
				new UserMultiRole());

		sQuery = " select case when s.nismultisite = 3 then s.nsitecode else -1 end nismultisite, s.nsitecode,s.ssitename, s.ntimezonecode, us.nusercode,"
					+ " tzd.sdatetimeformat, tzd.spgdatetimeformat,tzd.stimezoneid, tzd.sgmtoffset, " + " s.ssitecode"
					+ " from userssite us, site s,timezone tzd where s.nsitecode = us.nsitecode and tzd.ntimezonecode = s.ntimezonecode "
					+ " and tzd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and us.nusersitecode = " + nusermultisitecode;

		final Site objSite = (Site) jdbcTemplate.queryForObject(sQuery, new Site());

		sQuery = "select * from siteconfig where nsitecode = " + objSite.getNsitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SiteConfig objsiteconfig = (SiteConfig) jdbcUtilityFunction.queryForObject(sQuery, SiteConfig.class, jdbcTemplate);

		sQuery = "select ssettingvalue,ssettingname,nsettingcode from settings where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<Settings> listSettings = (List<Settings>) jdbcTemplate.query(sQuery, new Settings());
		
		Map<Short, String> mapOfTestComponent = listSettings.stream()
				.collect(Collectors.toMap(Settings::getNsettingcode, Settings::getSsettingvalue));

		sQuery = "select nformcode, sformname, jsondata from qualisforms where nstatus="
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus();
		List<QualisForms> hideScreenList = (List<QualisForms>) jdbcTemplate.query(sQuery, new QualisForms());
		

		sQuery = "select ssettingvalue,ssettingname,nsettingcode from sdmselnsettings where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		final List<SDMSELNSettings> listsdmselnsettings = (List<SDMSELNSettings>) jdbcTemplate.query(sQuery,	new SDMSELNSettings());
		
		Map<Integer, String> mapOfsdmselnsettings = listsdmselnsettings.stream()
				.collect(Collectors.toMap(SDMSELNSettings::getNsettingcode, SDMSELNSettings::getSsettingvalue));

		final int nmasterSiteCode = objSite.getNismultisite();

		sQuery = "select nlanguagecode, slanguagename, sfilename, slanguagetypecode, sreportingtoolfilename,sreportlanguagecode, ndefaultstatus, nsitecode, nstatus from Language where slanguagetypecode= N'"
				+ (String) objInputMap.get("slanguagetypecode") + "'";
		final Language objLanguage = (Language) jdbcUtilityFunction.queryForObject(sQuery, Language.class,jdbcTemplate);

		sQuery = "select nintegrationcode,sdescription,slinkname,smethodname,sclassurlname from integrationsettings where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nintegrationcode asc";
		final List<IntegrationSettings> listIntegrationSettings = (List<IntegrationSettings>) jdbcTemplate.query(sQuery,	new IntegrationSettings());

		sQuery = " select u.nusercode, d.sdeptname, d.ndeptcode, u.ndeptcode from users u, department d where d.ndeptcode = u.ndeptcode"
				+ " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nusercode = " + objSite.getNusercode();
		final Users objUsers = (Users) jdbcUtilityFunction.queryForObject(sQuery, Users.class,jdbcTemplate);

		sQuery = "select * from limselnusermapping where nlimsusercode= " + objSite.getNusercode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final LimsElnUserMapping objelnusers = (LimsElnUserMapping) jdbcUtilityFunction.queryForObject(sQuery, LimsElnUserMapping.class,jdbcTemplate);
		
		sQuery = "select nelnsitemappingcode,nlimssitecode,nelnsitecode,selnsitename,nsitecode from limselnsitemapping where nlimssitecode= "
				+ objSite.getNsitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final LimsElnSiteMapping objelnsite = (LimsElnSiteMapping) jdbcUtilityFunction.queryForObject(sQuery, LimsElnSiteMapping.class,jdbcTemplate);

		final String userThemeGet = "select tcm.sthemecolorname,ut.* from useruiconfig ut,themecolormaster tcm where ut.nthemecolorcode = tcm.nthemecolorcode "
				+ " and ut.nusercode = " + objSite.getNusercode() + " " + " and ut.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ut.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final String strQuery = "select nthemecolorcode,sthemecolorname,sthemecolorhexcode from themecolormaster tcm where  "
				+ " tcm.nthemecolorcode > 0 and tcm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		objMap.put("colortheme", jdbcTemplate.queryForList(strQuery));

		sQuery = "select * from reportsettings where nreportsettingcode in (15,16) order by nreportsettingcode";
		final List<ReportSettings> lstReportSettings = (List<ReportSettings>) jdbcTemplate.query(sQuery,	new ReportSettings());
		
		final Map<Short, String> mapOfReportSettings = lstReportSettings.stream()
				.collect(Collectors.toMap(ReportSettings::getNreportsettingcode, ReportSettings::getSsettingvalue));

		// To search data based on startswith,endswith etc., which is predefined in
		// database --ALPD-4167 ,work done by Dhanushya R I

		sQuery = "select nfilteroperatorcode, sfilteroperator from filteroperator where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nfilteroperatorcode>0";
		final List<FilterOperator> listFilterOperator = (List<FilterOperator>) jdbcTemplate.query(sQuery,	new FilterOperator());
		
//		Map<Short, String> mapOfFilterOperator = listFilterOperator.stream()
//				.collect(Collectors.toMap(FilterOperator::getNfilteroperatorcode, FilterOperator::getSfilteroperator));

		final UserInfo objUserInfo = new UserInfo();
		objUserInfo.setNusercode(objSite.getNusercode());
		objUserInfo.setSsitename(objSite.getSsitename());
		objUserInfo.setNuserrole(objUserMultiRole.getNuserrolecode());
		objUserInfo.setNdeputyusercode(objSite.getNusercode());
		objUserInfo.setNdeputyuserrole(objUserMultiRole.getNuserrolecode());
		objUserInfo.setSdeputyid(objUserMultiRole.getSloginid());
		objUserInfo.setNmodulecode((short) -1);
		objUserInfo.setNformcode((short) -1);
		objUserInfo.setSlanguagetypecode(objLanguage.getSlanguagetypecode());
		objUserInfo.setSreportlanguagecode(objLanguage.getSreportlanguagecode());
		objUserInfo.setSConnectionString(sConnStr);
		objUserInfo.setSlanguagefilename(objLanguage.getSfilename());
		objUserInfo.setSlanguagename(objLanguage.getSlanguagename());
		objUserInfo.setNsitecode(objSite.getNsitecode());
		objUserInfo.setNmastersitecode((short) nmasterSiteCode);
		objUserInfo.setNtranssitecode(objSite.getNsitecode());
		objUserInfo.setSreason("");
		objUserInfo.setSusername(objUserMultiRole.getSusername());
		objUserInfo.setSfirstname(objUserMultiRole.getSfirstname());
		objUserInfo.setSlastname(objUserMultiRole.getSlastname());
		objUserInfo.setSuserrolename(objUserMultiRole.getSuserrolename());
		objUserInfo.setSdeputyusername(objUserMultiRole.getSusername());
		objUserInfo.setSdeputyuserrolename(objUserMultiRole.getSuserrolename());
		objUserInfo.setNusersitecode(nusermultisitecode);
		objUserInfo.setSloginid(objUserMultiRole.getSloginid());
		objUserInfo.setSdeptname(objUsers.getSdeptname());
		objUserInfo.setNdeptcode(objUsers.getNdeptcode());
		// objUserInfo.setSdatetimeformat(objSite.getSdatetimeformat());
		objUserInfo.setSdatetimeformat(objsiteconfig.getSsitedatetime());
		objUserInfo.setSpgdatetimeformat(objSite.getSpgdatetimeformat());
		objUserInfo.setStimezoneid(objSite.getStimezoneid());
		objUserInfo.setNtimezonecode(objSite.getNtimezonecode());
		objUserInfo.setSgmtoffset(objSite.getSgmtoffset());
		objUserInfo.setIstimezoneshow(Integer.parseInt(mapOfTestComponent.get((short) 19)));
		objUserInfo.setSreportingtoolfilename(objLanguage.getSreportingtoolfilename());
		objUserInfo.setIsutcenabled(Integer.parseInt(mapOfTestComponent.get((short) 21)));
		objUserInfo.setNsiteadditionalinfo(Integer.parseInt(mapOfTestComponent.get((short) 23)));
		objUserInfo.setNisstandaloneserver(objsiteconfig.getNisstandaloneserver());
		objUserInfo.setNissyncserver(objsiteconfig.getNissyncserver());
		objUserInfo.setSsitecode(objSite.getSsitecode());
		objUserInfo.setActivelanguagelist(activelanguagelist);
		objUserInfo.setNexpirytime(Integer.parseInt(mapOfTestComponent.get((short) 82)));

		if (objsiteconfig != null) {
			objUserInfo.setSsitedate(objsiteconfig.getSsitedate());
			objUserInfo.setSsitedatetime(objsiteconfig.getSsitedatetime());
			objUserInfo.setSpgsitedatetime(objsiteconfig.getSpgdatetime());
			objUserInfo.setSsitereportdate(objsiteconfig.getSsitereportdate());
			objUserInfo.setSsitereportdatetime(objsiteconfig.getSsitereportdatetime());
			objUserInfo.setSpgsitereportdatetime(objsiteconfig.getSpgreportdatetime());
		}

		objUserInfo.setNlogintypecode((short) nlogintypecode);
		objMap.put("UserInfo", objUserInfo);
		objMap.put("Settings", mapOfTestComponent);
		objMap.put("SDMSELNSettings", mapOfsdmselnsettings);
		objMap.put("HideQualisForms", hideScreenList);
		objMap.put("selectedUserUiConfig", jdbcUtilityFunction.queryForObject(userThemeGet, UserUiConfig.class,jdbcTemplate));
		objMap.put("ReportSettings", mapOfReportSettings);
		objMap.put("IntegrationSettings", listIntegrationSettings);
		objMap.put("ELNUserInfo", objelnusers);
		objMap.put("ELNSite", objelnsite);
		objMap.put("FilterOperator", listFilterOperator);

		final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();
		final Map<String, GenericLabel> genericLabelMap = genericLabelList.stream()
				.collect(Collectors.toMap(GenericLabel::getSgenericlabel, genericLabel -> genericLabel));
		final Map<String, GenericLabel> genericLabelMapIDS = genericLabelList.stream()
				.collect(Collectors.toMap(GenericLabel::getSidsfieldname, genericLabel -> genericLabel));

		objMap.put("GenericLabel", genericLabelMap);
		objMap.put("GenericLabelIDS", genericLabelMapIDS);
		
//		final List<QualisForms> erDiagramList = projectDAOSupport.getERDiagram(nlogintypecode, nuserrolecode, 
//											nmasterSiteCode, strQuery);
//		
//		final Map<Short, QualisForms> erDiagramMap = erDiagramList.stream()
//				.collect(Collectors.toMap(QualisForms::getNformcode, qualisForms -> qualisForms));
//		
//		objMap.put("ERDiagramMap", erDiagramMap);
		// objMap.put("IntegrationSettings", listIntegrationSettings);

		return objMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getAdsconnection(ADSSettings objAdsConnectConfig) throws Exception {
		String str = "";
		if (objAdsConnectConfig != null) {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.PROVIDER_URL, objAdsConnectConfig.getSldaplink());
			env.put(Context.SECURITY_PRINCIPAL,
					objAdsConnectConfig.getSloginid() + "@" + objAdsConnectConfig.getSdomainname());
			env.put(Context.SECURITY_CREDENTIALS, objAdsConnectConfig.getSpassword());
			env.put(Context.REFERRAL, "follow");
			try {
				DirContext ctx = new InitialDirContext(env);
				LOGGER.info("connected");
				ctx.close();
				return Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			} catch (AuthenticationNotSupportedException ex) {
				str = "IDS_AUTHENTICATIONNOTSUPPORTEDBYSERVER";
				LOGGER.error("The authentication is not supported by the server");
			} catch (AuthenticationException ex) {
				String errorcodes[] = { "52e", "525", "530", "531", "532", "533", "701", "773" };
				for (int i = 0; i < errorcodes.length; i++) {
					String strser = errorcodes[i];
					if (ex.getLocalizedMessage().toString().contains(strser)) {
						switch (errorcodes[i]) {
						case "52e":
							str = "IDS_WRONGCREDENTIAL";
							break;
						case "525":
							str = "IDS_USERNOTFOUND";
							break;
						case "530":
							str = "IDS_NOTPERMITTEDCONTACTADMIN";
							break;
						case "531":
							str = "IDS_NOTPERMITTED";
							break;
						case "532":
							str = "IDS_PASSWORDEXPIRED";
							break;
						case "533":
							str = "IDS_ACCOUNTDISABLED";
							break;
						case "701":
							str = "IDS_ACCOUNTEXPIRED";
							break;
						case "773":
							str = "IDS_USERMUSTRESETPASSWORD";
							break;
						default:
							str = "IDS_UNKNOWNERROR";
							break;
						}
						break;
					}
				}
			} catch (NamingException ex) {
				LOGGER.error("Unknown Host...check Server name");
				str = "IDS_UNKNOWNHOSTCHECKSERVERNAME";
			} catch (Exception ex) {
				LOGGER.error("error when trying to create the context : " + ex.getMessage());
				str = "IDS_ERRORWHENCREATECONTEXT";
			}
		}
		return str;
	}

	private Map<String, Object> validateUserStatus(int nusercode, int nuserrolecode, int nusermultisitecode)
			throws Exception {

		final String queryString = "select * from Users U where U.nusercode=" + nusercode + " " + "and U.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		final Users objUsers = (Users) jdbcUtilityFunction.queryForObject(queryString, Users.class,jdbcTemplate);
		Map<String, Object> objMap = new HashMap<String, Object>();

		if (objUsers == null) {

			objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus());
		} else {
			if (objUsers.getNtransactionstatus() == Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()) {

				objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_LOGINIDDEACTIVATED");

			} else if (objUsers.getNlockmode() == Enumeration.TransactionStatus.LOCK.gettransactionstatus()) {

				objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_LOGINIDLOCKED");

			} else if (objUsers.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
					.gettransactionstatus()) {

				objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_ALREADYRETIRED");

			} else {

				Map<String, Object> returnmap = validateLicense(objUsers, nusercode, nuserrolecode, nusermultisitecode);
				return returnmap;

			}
		}
		return objMap;
	}

	// ALPD-4169
	public String decryptData(String encryptedList) throws Exception {

		final String SECRET_KEY = "XMzDdG4D03CKm2IxIWQw7g=="; // 128 bit key
		final String INIT_VECTOR = "encryptionIntVec"; // 16 bytes IV
		IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
		SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
		byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedList));

		return new String(original);

	}

	// This method is used for validate the license
	private Map<String, Object> validateLicense(Users objUsers, int nusercode, int nuserrolecode,
			int nusermultisitecode) throws Exception {

		boolean bTrue = false;
		String query = "";
		Map<String, Object> objMap = new HashMap<String, Object>();

		String strQuery = "select * from licenseconfiguration where nsitecode = " + objUsers.getNsitecode()
				+ " and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		LicenseConfiguration licenseconfiguration = (LicenseConfiguration) jdbcUtilityFunction.queryForObject(strQuery,LicenseConfiguration.class,jdbcTemplate);

		if (licenseconfiguration != null) {

			int nsessionusers = 0;

			try {
				
				int nexistuser = 0;
				if (nusercode == -1) {
					bTrue = true;
				} else {
					
					// ALPD-4169
					// int ncountuser = licenseconfiguration.getNnooflicense();			
					//LOGGER.info("nooflicense:"+licenseconfiguration.getSnooflicense());
					int ncountuser = Integer.parseInt(decryptData(licenseconfiguration.getSnooflicense()));
					LOGGER.info("License count:" + ncountuser);

					query = "select count (*) as ncount from sessiondetails where ntransactionstatus = "
							+ Enumeration.TransactionStatus.LOGIN.gettransactionstatus()
							+ " and nusercode <> -1 and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
					Integer sessionUserCount = (Integer) jdbcUtilityFunction.queryForObject(query, Integer.class,jdbcTemplate);
					if(sessionUserCount != null)
					{
						nsessionusers = sessionUserCount.intValue();
					}

					if ((int) licenseconfiguration.getNlicensetypecode() == Enumeration.LicenseType.CONCURRENTNOSESSION
							.getLicenseType()) {
						// Based on the nnooflicense count it will login the application.Here, the user
						// and user role doesn't consider.
						if (nsessionusers < ncountuser) {
							bTrue = true;
						} else {
							objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									Enumeration.ReturnStatus.LICENSELIMITEXCEED.getreturnstatus());
							bTrue = false;
						}
					} else if ((int) licenseconfiguration
							.getNlicensetypecode() == Enumeration.LicenseType.CONCURRENTUSERBASEDSESSION
									.getLicenseType()) {
						// User level validation - The Same user is not allowed to login again

						query = "select count(*) as nuserexist from sessiondetails where nusercode = " + nusercode
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nusercode<> -1 ";
						

						final Integer nUserExit = (Integer) jdbcUtilityFunction.queryForObject(query, Integer.class,jdbcTemplate);
						
						if(nUserExit != null)
						{
							nexistuser = nUserExit;
						}
						if (nexistuser == 0) {
							if (nsessionusers < ncountuser) {
								bTrue = true;
							} else {
								objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										Enumeration.ReturnStatus.LICENSELIMITEXCEED.getreturnstatus());
								bTrue = false;
							}
						} else {
							objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									Enumeration.ReturnStatus.THISUSERALREADYLOGGEDIN.getreturnstatus());

							bTrue = false;
						}
					} else if ((int) licenseconfiguration
							.getNlicensetypecode() == Enumeration.LicenseType.CONCURRENTUSERROLEBASEDSESSION
									.getLicenseType()) {
						// User & User Role level validation - Same user and Same user role are not
						// allowed to login again

						query = "select count(*) as nuserexist from sessiondetails where nusercode = " + nusercode
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nusercode<> -1 and nuserrolecode=" + nuserrolecode + " ";
						

						nexistuser = (int) jdbcUtilityFunction.queryForObject(query, Integer.class,jdbcTemplate);
						if (nexistuser == 0) {
							if (nsessionusers < ncountuser) {
								bTrue = true;
							} else {
								objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										Enumeration.ReturnStatus.LICENSELIMITEXCEED.getreturnstatus());
								bTrue = false;
							}
						} else {
							objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									Enumeration.ReturnStatus.SAMEUSERANDROLELOGIN.getreturnstatus());
							//
							bTrue = false;
						}
					}
				}
				if (bTrue) {

					objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				}
			} catch (Exception e) {
				LOGGER.info("License count error:" + e);
				objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.INVALIDLICENSECOUNT.getreturnstatus());
				// bTrue = false;
			}

		} else {

			objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		}
		return objMap;

	}

	@Override
	public ResponseEntity<Object> createNewPassword(Map<String, Object> objinputmap) throws Exception {
		final int nusersitecode = (int) objinputmap.get("nusersitecode");
		final String spassword = (String) objinputmap.get("spassword");
		updatePassword(nusersitecode, spassword);
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	private int updatePassword(final int nusersitecode, final String spassword) throws Exception {
		String sQuery = "select pp.nexpirypolicy from usermultirole um,userrolepolicy urp, passwordpolicy pp "
				+ " where pp.npolicycode = urp.npolicycode and um.nuserrolecode=urp.nuserrolecode "
				+ " and um.nusersitecode=" + nusersitecode + " and um.ndefaultrole="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and  pp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and urp.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and um.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and urp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		PasswordPolicy objPasswordPolicy = (PasswordPolicy) jdbcUtilityFunction.queryForObject(sQuery, PasswordPolicy.class,jdbcTemplate);

		sQuery = " update UserMultiRole set  spassword ='" + spassword + "' ," + " dpasswordvalidatedate= " + "'"
				+ dateUtilityFunction.getUTCDateTime() + "'::Timestamp +" + objPasswordPolicy.getNexpirypolicy()
				+ " * interval '1 DAY' " + " where nusersitecode = " + nusersitecode + " and ndefaultrole="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		jdbcTemplate.execute(sQuery);

		sQuery = "select nusermultirolecode,spassword from usermultirole where nusersitecode = " + nusersitecode
				+ " and ndefaultrole=" + Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		UserMultiRole objUserMultiRole = (UserMultiRole) jdbcUtilityFunction.queryForObject(sQuery, UserMultiRole.class,jdbcTemplate);
		
		projectDAOSupport.encryptPassword("usermultirole", "nusermultirolecode", objUserMultiRole.getNusermultirolecode(),
				objUserMultiRole.getSpassword(), "spassword");
		return objUserMultiRole.getNusermultirolecode();
	}

	@Override
	public ResponseEntity<Object> changePassword(Map<String, Object> objinputmap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(objinputmap.get("userInfo"), new TypeReference<UserInfo>() {
		});
		final int nusersitecode = (int) objinputmap.get("nusersitecode");
		final String sNewPassword = (String) objinputmap.get("spassword");
		final String sOldPassword = (String) objinputmap.get("sOldPassword");
		String sQuery = "select ur.suserrolename, umr.nusermultirolecode ,umr.nuserrolecode ,umr.ndefaultrole,umr.spassword,umr.ntransactionstatus"
				+ " from usermultirole umr, userrole ur where umr.nusersitecode = " + nusersitecode
				+ " and umr.nuserrolecode = ur.nuserrolecode " + " and umr.ntransactionstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.ndefaultrole = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and umr.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final UserMultiRole objUserMultiRole = (UserMultiRole) jdbcUtilityFunction.queryForObject(sQuery, UserMultiRole.class,jdbcTemplate);
		if (objUserMultiRole != null) {
			final String sOldPasswordInDb = projectDAOSupport.decryptPassword("usermultirole", "nusermultirolecode",
					objUserMultiRole.getNusermultirolecode(), "spassword");
			if (!sOldPasswordInDb.equals(sOldPassword)) {
				return new ResponseEntity<>(Enumeration.ReturnStatus.OLDPASSWORDMISSMATCH.getreturnstatus(),
						HttpStatus.CONFLICT);
			} else if (sNewPassword.equals(sOldPassword)) {
				return new ResponseEntity<>("IDS_NEWANDOLDPASSWORDSHOULDNOTSAME", HttpStatus.CONFLICT);
			} else {
				final int nusermultirolecode = updatePassword(nusersitecode, sNewPassword);

				final List<String> listOfMultiLingual = commonFunction.getMultilingualMultipleMessage(
						Arrays.asList("IDS_CHANGEPASSWORD", "IDS_LOGINID", "IDS_USERNAME", "IDS_USERROLE"),
						userInfo.getSlanguagefilename());

				final String scomments = listOfMultiLingual.get(0) + ": " + listOfMultiLingual.get(1) + ": "
						+ userInfo.getSloginid() + ";" + listOfMultiLingual.get(2) + ": " + userInfo.getSusername()
						+ ";" + listOfMultiLingual.get(3) + ": " + userInfo.getSuserrolename();

				Map<String, Object> inputMap = new HashMap<>();
				inputMap.put("sprimarykeyvalue", nusermultirolecode);
				inputMap.put("stablename", "usermultirole");
				auditUtilityFunction.insertAuditAction(userInfo, "IDS_CHANGEPASSWORD", scomments, inputMap);
				return new ResponseEntity<>(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(), HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>("IDS_USERMULTIROLENOTAVAILABLE", HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> getUserScreenRightsMenu(Map<String, Object> objinputmap) throws Exception {
		Map<String, Object> objMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		final int nuserrolecode = (int) objinputmap.get("nuserrolecode");
		UserInfo userInfo = objMapper.convertValue(objinputmap.get("userinfo"), UserInfo.class);
		objMap = getUserForms(userInfo.getNusercode(), nuserrolecode, userInfo.getNmastersitecode(),
				userInfo.getSlanguagetypecode());
		objMap.putAll(getControlRights(nuserrolecode, userInfo.getNusercode(), userInfo.getNmastersitecode(),
				userInfo.getSlanguagetypecode(), userInfo));
		objMap.putAll(collectUserInfo(objinputmap));
		UserInfo newUserInfo = (UserInfo) objMap.get("UserInfo");
		final List<String> listOfMultilingual = commonFunction.getMultilingualMultipleMessage(
				Arrays.asList("IDS_LOGINID", "IDS_USERNAME", "IDS_ROLECHANGE"), userInfo.getSlanguagefilename());
		final String scomments = listOfMultilingual.get(0) + ": " + userInfo.getSloginid() + ";"
				+ listOfMultilingual.get(1) + ": " + userInfo.getSusername() + ";" + listOfMultilingual.get(2) + ": "
				+ userInfo.getSuserrolename() + "-> " + newUserInfo.getSuserrolename() + ";";
		
		//ALPD-4913-To detele the saved filter dynamic data when session delete	
				final String updateroleqry="update sessiondetails set nuserrolecode= "+nuserrolecode
						+" where ssessionid='"+ stringUtilityFunction.replaceQuote(userInfo.getSsessionid())
						+"' and nusercode="+userInfo.getNusercode()+" and nuserrolecode="+ userInfo.getNuserrole()+";";
				jdbcTemplate.execute(updateroleqry);
				
				final Map<String, Object> deleteMap = new HashMap<>();
				deleteMap.put("clearTempFilter", true);
				projectDAOSupport.createFilterSubmit(deleteMap,userInfo);	
		        //ALPD-4913 End

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("sprimarykeyvalue", -1);
		inputMap.put("stablename", "login");
		userInfo.setNformcode((short) -1);
		auditUtilityFunction.insertAuditAction(userInfo, "IDS_CHANGEROLE", scomments, inputMap);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	private Map<String, Object> getUserForms(int nusercode, int nuserrolecode, int nmasterSiteCode,
			String slanguageTypeCode) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		List<QualisMenu> lstfinalqlsmenu = new ArrayList<QualisMenu>();
		List<QualisModule> lstfinalqlsmodule = new ArrayList<QualisModule>();
		String smenu = "select nmenucode, smenuname, coalesce(jsondata->'sdisplayname'->>'" + slanguageTypeCode
				+ "',smenuname) as sdisplayname, coalesce(jsondata->'sshortdesc'->>'" + slanguageTypeCode
				+ "',smenuname) as sshortdesc from qualismenu where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter";
		List<QualisMenu> lstqlsmenu = (List<QualisMenu>) jdbcTemplate.query(smenu, new QualisMenu());
		
		smenu = "select nmodulecode, smodulename, coalesce(jsondata->'sdisplayname'->>'" + slanguageTypeCode
				+ "',smodulename) as sdisplayname, nmenucode from qualismodule where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter";
		
		List<QualisModule> lstqlsmodule = (List<QualisModule>) jdbcTemplate.query(smenu, new QualisModule());

		smenu = "select qf.nformcode, qf.sclassname, "
				+ " coalesce(qf.jsondata->'sdisplayname'->>'" + slanguageTypeCode + "',sformname) as sdisplayname," 
			//	+ " coalesce(qf.jsondata->>'erdiagram','') as serdiagram," 
				+ " sformname ,"
				+ " qf.nmodulecode, qf.nmenucode, qf.nsorter, qf.surl from "
				+ " usersrolescreen urs, qualisforms qf, sitequalisforms sqf"
				+ " where qf.nformcode = urs.nformcode and qf.nformcode = sqf.nformcode" + " and sqf.nsitecode = "
				+ nmasterSiteCode + " and qf.nformcode not in (select"
				+ " nformcode from usersrolescreenhide where nuserrolecode=" + nuserrolecode + " and nusercode="
				+ nusercode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and needrights=3)" + " and qf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and urs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and urs.nuserrolecode="
				+ nuserrolecode + " and sqf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by qf.nmenucode,qf.nmodulecode,qf.nsorter";
		List<QualisForms> lstqlsforms = (List<QualisForms>) jdbcTemplate.query(smenu, new QualisForms());
		
		if (lstqlsforms.size() > 0) {
			lstqlsmodule.forEach(objqualismodule -> {
				List<QualisForms> filterform = lstqlsforms.stream()
						.filter((QualisForms objforms) -> objforms.getNmodulecode() == objqualismodule.getNmodulecode())
						.collect(Collectors.toList());
				if (filterform != null && filterform.size() > 0) {
					objqualismodule.setLstforms(filterform);
					lstfinalqlsmodule.add(objqualismodule);
				}
			});
			lstqlsmenu.forEach(objqualismenu -> {
				List<QualisModule> filtermodule = lstfinalqlsmodule.stream()
						.filter((QualisModule objmodule) -> objmodule.getNmenucode() == objqualismenu.getNmenucode())
						.collect(Collectors.toList());
				if (filtermodule != null && filtermodule.size() > 0) {
					objqualismenu.setLstmodule(filtermodule);
					lstfinalqlsmenu.add(objqualismenu);
				}
			});
		}
		smenu = "select nmenucode, smenuname, coalesce(jsondata->'sdisplayname'->>'" + slanguageTypeCode
				+ "',smenuname) as sdisplayname,coalesce(jsondata->'sshortdesc'->>'" + slanguageTypeCode
				+ "',smenuname) as sshortdesc  from qualismenu where nmenucode=-2 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter";
		List<QualisMenu> lstqlsmenu1 = (List<QualisMenu>) jdbcTemplate.query(smenu, new QualisMenu());
		lstqlsmenu1.addAll(lstfinalqlsmenu);

		map.put("MenuDesign", lstqlsmenu1);

		smenu = "select nhomedesigncode,shomename,smethodurl,ndefaultstatus,sdisplayname->'sdisplayname'->>'"
				+ slanguageTypeCode + "' sdisplayname from homedesign where nsitecode=" + nmasterSiteCode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1";
		List<?> lstHomeMenu = jdbcTemplate.queryForList(smenu);

		map.put("HomeDesign", lstHomeMenu);

		return map;
	}

	@Override
	public ResponseEntity<Object> getChangeRole(Map<String, Object> objinputmap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(objinputmap.get("userinfo"), UserInfo.class);
		String passwordpolicyrolequery = "select * from userrolepolicy where ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nuserrolecode="
				+ objUserInfo.getNuserrole() + "";
		List<UserRolePolicy> lstPasswordpolicyrole = (List<UserRolePolicy>) jdbcTemplate.query(passwordpolicyrolequery, new UserRolePolicy());
		if (lstPasswordpolicyrole.size() == 0) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKPASSWORDPOLICYROLE",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			Map<String, Object> objMap = new HashMap<String, Object>();
			String querymultirole = "select umr.nusermultirolecode, umr.nuserrolecode, ur.suserrolename "
					+ " from usermultirole umr,userrole ur ,userssite us,userrolepolicy urp where umr.nuserrolecode = ur.nuserrolecode"
					+ " and urp.nuserrolecode=ur.nuserrolecode and  us.nusersitecode=umr.nusersitecode "
					+ " and umr.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and umr.nusersitecode = " + objUserInfo.getNusersitecode() + " and umr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nuserrolecode not in("
					+ objUserInfo.getNuserrole() + "," + Enumeration.TransactionStatus.NA.gettransactionstatus()
					+ ") and urp.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and us.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and urp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by umr.ndefaultrole asc";
			List<UserMultiRole> lstUserMultiRole = (List<UserMultiRole>) jdbcTemplate.query(querymultirole,	new UserMultiRole());
			objMap.put("UserMultiRole", lstUserMultiRole);
			return new ResponseEntity<>(objMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getUserRoleScreenControl(Map<String, Object> objinputmap) throws Exception {
		Map<String, Object> outMap = new HashMap<>();
		final int nuserrolecode = (int) objinputmap.get("nuserrolecode");
		final int nusercode = (int) objinputmap.get("nusercode");
		final int nmasterSiteCode = (int) objinputmap.get("nmastersitecode");
		final String slanguageTypeCode = (String) objinputmap.get("slanguagetypecode");
		outMap.putAll(getUserForms(nusercode, nuserrolecode, nmasterSiteCode, slanguageTypeCode));
		final UserInfo userInfo = (UserInfo) objinputmap.get("userinfo");
		outMap.putAll(getControlRights(nuserrolecode, nusercode, nmasterSiteCode, slanguageTypeCode, userInfo));
		return new ResponseEntity<>(outMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> validateEsignCredential(final UserInfo userInfo, final String password)
			throws Exception {
		final String queryString = "select * from Users u where u.nusercode=" + userInfo.getNusercode()
				+ " and u.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Users users = (Users) jdbcUtilityFunction.queryForObject(queryString, Users.class,jdbcTemplate);
		if (users == null) {
			return new ResponseEntity<>(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (users.getNtransactionstatus() == Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()) {
				final String userString = "select * from Users u where u.nusercode=" + userInfo.getNdeputyusercode()
						+ " and u.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Users deputyUsers = (Users) jdbcUtilityFunction.queryForObject(userString, Users.class,jdbcTemplate);
				if (deputyUsers.getNtransactionstatus() == Enumeration.TransactionStatus.ACTIVE
						.gettransactionstatus()) {
					return activeUser(userInfo, deputyUsers, password);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_LOGINIDDEACTIVATED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else if (users.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_USERRETIRED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				return activeUser(userInfo, users, password);
			}
		}
	}

	private ResponseEntity<Object> activeUser(final UserInfo userInfo, final Users users, final String password)
			throws Exception {
		if (users.getNlockmode() == Enumeration.TransactionStatus.LOCK.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_LOGINIDLOCKED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String userSiteQuery = "select * from userssite where nusercode=" + users.getNusercode()
					+ " and nsitecode=" + userInfo.getNsitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final UsersSite userSite = (UsersSite) jdbcUtilityFunction.queryForObject(userSiteQuery, UsersSite.class,jdbcTemplate);
			if (userSite == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_INVALIDUSERSITE", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final String roleQuery = "select * from usermultirole where nusersitecode ="
						+ userSite.getNusersitecode() + " and ndefaultrole ="
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and ntransactionstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final UserMultiRole userMultiRole = (UserMultiRole) jdbcUtilityFunction.queryForObject(roleQuery, UserMultiRole.class,jdbcTemplate);
				if (userMultiRole == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INACTIVEUSERROLE",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				} else {
					if (userMultiRole.getSpassword() == null) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NULLPASSWORD",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else {
						final String decryptedPassword = projectDAOSupport.decryptPassword("usermultirole", "nusermultirolecode",
								userMultiRole.getNusermultirolecode(), "spassword");
						if (decryptedPassword.equals(password)) {
							if (userMultiRole.getDpasswordvalidatedate() == null) {
								return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NULLEXPIRY",
										userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
							} else {
								final String expiryNeededQuery = "select nexpirypolicyrequired from passwordpolicy pp, "
										+ " userrolepolicy urp where pp.npolicycode=urp.npolicycode and urp.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and pp.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and urp.nuserrolecode =" + userMultiRole.getNuserrolecode()
										+ " and urp.ntransactionstatus= "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

								final int expiryNeeded = (int) jdbcUtilityFunction.queryForObject(expiryNeededQuery,Integer.class,jdbcTemplate);
								
								if (expiryNeeded == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									long difference = userMultiRole.getDpasswordvalidatedate().getTime()
											- new Date().getTime();
									float daysBetween = (difference / (1000 * 60 * 60 * 24));
									if (daysBetween >= 0) {
										return new ResponseEntity<>(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(),
												HttpStatus.OK);
									} else {
										return new ResponseEntity<>(
												commonFunction.getMultilingualMessage("IDS_PASSWORDEXPIRED",
														userInfo.getSlanguagefilename()),
												HttpStatus.EXPECTATION_FAILED);
									}
								} else {
									return new ResponseEntity<>(commonFunction.getMultilingualMessage(
											Enumeration.ReturnStatus.SUCCESS.getreturnstatus(),
											userInfo.getSlanguagefilename()), HttpStatus.OK);
								}
							}

						} else {
							final List<String> listOfMultiLingual = commonFunction.getMultilingualMultipleMessage(Arrays
									.asList("IDS_ESIGNINVALIDPASSWORD", "IDS_LOGINID", "IDS_USERNAME", "IDS_USERROLE"),
									userInfo.getSlanguagefilename());

							final String scomments = listOfMultiLingual.get(1) + ": " + userInfo.getSloginid();
							
							final Map<String, Object> inputMap = new HashMap<>();
							inputMap.put("sprimarykeyvalue", userMultiRole.getNusermultirolecode());
							inputMap.put("stablename", "usermultirole");
							auditUtilityFunction.insertAuditAction(userInfo, "IDS_ESIGNINVALIDPASSWORD", scomments, inputMap);
							return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVALIDPASSWORD",
									userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					}
				}
			}
		}
	}

	private Map<String, Object> getControlRights(int nuserrolecode, int nusercode, int nsitecode,
			String slanguageTypeCode, UserInfo userInfo) throws Exception {
		final Map<String, Object> controlMap = new HashMap<String, Object>();
		final Map<String, Object> outMap = new HashMap<String, Object>();
		String sitebaseControl = "";
		//ALPD-4420 Aravindh 19/06/2024
		if(userInfo.getNsiteadditionalinfo() == Enumeration.TransactionStatus.YES.gettransactionstatus()
				&& userInfo.getNusercode() != -1 && 
				(userInfo.getNisstandaloneserver() == Enumeration.TransactionStatus.NO.gettransactionstatus() && userInfo.getNissyncserver() == Enumeration.TransactionStatus.NO.gettransactionstatus()))
		{
			//Login - not system user, Login site - Web Technology site
			final String query = "select * from settings where nsettingcode=25 and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<Settings> lstQuery = (List<Settings>) jdbcTemplate.query(query, new Settings());
			if (!lstQuery.isEmpty()) {
				final int disableMasterControls = Integer.parseInt(lstQuery.get(0).getSsettingvalue());
				if (disableMasterControls == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					sitebaseControl = " and cm.nisdistributedsite="
							+ Enumeration.TransactionStatus.YES.gettransactionstatus();
				}
			}
							

		}
		else if(userInfo.getNsiteadditionalinfo() == Enumeration.TransactionStatus.NO.gettransactionstatus())
		{
			//Do nothing
			sitebaseControl="";
		}
	
		else if (userInfo.getNsiteadditionalinfo() == Enumeration.TransactionStatus.YES.gettransactionstatus()
				&& userInfo.getNusercode() != -1) {
			
			//Login not as system user
			final String sQuery = " select sc.* from siteconfig sc,site s,settings s1 "
					+ " where  sc.nsitecode = s.nsitecode "
					+ " and sc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and s1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and s1.ssettingvalue=s.ssitecode "
					+ " and s1.nsettingcode=30 ";
			
			final SiteConfig objSiteConfig = (SiteConfig) jdbcUtilityFunction.queryForObject(sQuery, SiteConfig.class,jdbcTemplate);
			if(objSiteConfig.getNissyncserver() == Enumeration.TransactionStatus.YES.gettransactionstatus())
	          {
	        	  sitebaseControl = " and cm.nisprimarysyncsite="
							+ Enumeration.TransactionStatus.YES.gettransactionstatus();
	          }
	          else {
				if (userInfo.getNisstandaloneserver() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							
						sitebaseControl = " and cm.nisdistributedsite="
							+ Enumeration.TransactionStatus.YES.gettransactionstatus();				
				} else if (userInfo.getNissyncserver() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
				String query = "select * from settings where nsettingcode=25 and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				List<Settings> lstQuery = (List<Settings>) jdbcTemplate.query(query, new Settings());
				if (!lstQuery.isEmpty()) {
					int disableMasterControls = Integer.parseInt(lstQuery.get(0).getSsettingvalue());
					if (disableMasterControls == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						sitebaseControl = " and cm.nisdistributedsite="
								+ Enumeration.TransactionStatus.YES.gettransactionstatus();
					}
				}						
				
			} else if (userInfo.getNissyncserver() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				sitebaseControl = " and cm.nisprimarysyncsite="
						+ Enumeration.TransactionStatus.YES.gettransactionstatus();
			     }			
	        }
						
		}	
 
		String sQuery = "SELECT urc.ncontrolcode,cm.nformcode ,  cm.scontrolname," + " cm.jsondata->'scontrolids'->>'"
				+ slanguageTypeCode + "' as scontrolids," + " cm.nisesigncontrol,"
				+ " qf.nstatus, scm.nsitecontrolcode, scm.nsitecode, urc.nneedesign, scm.nisbarcodecontrol"
				+ " from userrolescreencontrol urc ,controlmaster cm ,qualisforms qf,usersrolescreen urs, sitecontrolmaster scm"
				+ " WHERE not exists ( SELECT usch.ncontrolcode"
				+ " from userrolescreencontrolhide usch  WHERE usch.nformcode = urc.nformcode and usch.ncontrolcode=urc.ncontrolcode"
				+ " and usch.nusercode = " + nusercode + " AND usch.nuserrolecode = urc.nuserrolecode"
				+ " and usch.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and usch.needrights=3)"
				+ " and not exists( SELECT ursh.nformcode FROM usersrolescreenhide ursh WHERE ursh.nformcode = urs.nformcode AND ursh.nusercode = "
				+ nusercode + " and ursh.nuserrolecode = urs.nuserrolecode  and ursh.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ursh.needrights=3 ) AND cm.ncontrolcode = urc.ncontrolcode AND urc.nformcode = cm.nformcode and urc.nneedrights="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ""
				+ " and cm.ncontrolcode = scm.ncontrolcode and scm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and scm.nsitecode = " + nsitecode
				+ " and urs.nformcode = cm.nformcode AND urs.nformcode = qf.nformcode AND urc.nuserrolecode = urs.nuserrolecode AND urc.nuserrolecode = "
				+ nuserrolecode + " and cm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND urc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + sitebaseControl;
		List<ControlMaster> lstCM = (List<ControlMaster>) jdbcTemplate.query(sQuery, new ControlMaster());

		Set<Short> lst = lstCM.stream().map(source -> source.getNformcode()).collect(Collectors.toSet());
		lst.stream().forEach(objControlMaster -> {
			List<ControlMaster> lstControlMaster = lstCM.stream()
					.filter(source -> source.getNformcode() == objControlMaster).collect(Collectors.toList());
			controlMap.put(String.valueOf(objControlMaster), lstControlMaster);
		});
		outMap.put("UserRoleControlRights", controlMap);
		return outMap;
	}

	private Map<String, Object> getTransactionStatus(final UserInfo userInfo) throws Exception {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		Map<String, Object> outMap = new HashMap<String, Object>();
		final String sStatusQuery = "select tv.ntransactionvalidationcode,tv.nformcode,tv.ncontrolcode,tv.ntransactionstatus,"
				+ "coalesce(t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',stransstatus) stransdisplaystatus "
				+ ",nregtypecode,nregsubtypecode from transactionvalidation tv,transactionstatus t where t.ntranscode=tv.ntransactionstatus and t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + ";";
		List<TransactionValidation> lstCM = (List<TransactionValidation>) jdbcTemplate.query(sStatusQuery,
				new TransactionValidation());
		Set<Short> lst = lstCM.stream().map(source -> source.getNcontrolcode()).collect(Collectors.toSet());
		lst.stream().forEach(objStatus -> {
			List<TransactionValidation> lstControlMaster = lstCM.stream()
					.filter(source -> source.getNcontrolcode() == objStatus).collect(Collectors.toList());
			statusMap.put(String.valueOf(objStatus), lstControlMaster);
		});
		outMap.put("TransactionValidation", statusMap);
		return outMap;
	}

	@Override
	public ResponseEntity<Object> getPassWordPolicy(int nuserrolecode) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.put("PasswordPolicy", getPasswordPolicyMsg(nuserrolecode));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> idleTimeAuditAction(final UserInfo userInfo, final String password,
			final boolean flag, final int nFlag,HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
//		ResourceBundle resourcebundle = new PropertyResourceBundle(new InputStreamReader(getClass().getClassLoader()
//				.getResourceAsStream(Enumeration.Path.PROPERTIES_FILE.getPath() + userInfo.getSlanguagefilename() + ".properties"), "UTF-8"));
		final ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(), false);
		
		
		String sauditaction = "";
		String scomments = resourcebundle.containsKey("IDS_USERNAME")
				? resourcebundle.getString("IDS_USERNAME") + ": " + userInfo.getSusername() + ";"
				: "User Name" + userInfo.getSusername() + "; ";
		scomments += resourcebundle.containsKey("IDS_USERROLE")
				? resourcebundle.getString("IDS_USERROLE") + ": " + userInfo.getSuserrolename() + ";"
				: "User Role" + userInfo.getSuserrolename() + "; ";
		
		scomments += resourcebundle.containsKey("IDS_LOGINID")
				? resourcebundle.getString("IDS_LOGINID") + ": " + userInfo.getSloginid()
				: "Login ID" + userInfo.getSloginid() + "";

		// get defaultuserrole
		String strQuery = "select um.nusermultirolecode,um.spassword,u.nusercode,u.sloginid,u.ntransactionstatus,u.nlockmode,um.nnooffailedattempt "
				+ " from usermultirole um,userssite us,users u "
				+ " where u.nusercode = us.nusercode and us.nusersitecode = um.nusersitecode and" + " um.ndefaultrole="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and u.nusercode= "
				+ userInfo.getNdeputyusercode() + " and um.nusersitecode = " + userInfo.getNusersitecode() + " "
				+ " and um.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		UserMultiRole objUsersData = (UserMultiRole) jdbcUtilityFunction.queryForObject(strQuery, UserMultiRole.class,jdbcTemplate);

		if (objUsersData != null) {
			/* Login User Validation */
			if (flag) {
				if (objUsersData.getNtransactionstatus() == Enumeration.TransactionStatus.DEACTIVE
						.gettransactionstatus()) {
					returnMap.put("PassFlag", commonFunction.getMultilingualMessage("IDS_LOGINIDDEACTIVATED",
							userInfo.getSlanguagefilename()));
					return new ResponseEntity<>(returnMap, HttpStatus.OK);
				}

				if (userInfo.getNlogintypecode() == Enumeration.LoginType.INTERNAL.getnlogintype()) {
					if (objUsersData.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
							.gettransactionstatus()) {
						returnMap.put("PassFlag", commonFunction.getMultilingualMessage("IDS_LOGINUSERRETIRED",
								userInfo.getSlanguagefilename()));
						return new ResponseEntity<>(returnMap, HttpStatus.OK);
					}
					if (objUsersData.getNlockmode() == Enumeration.TransactionStatus.LOCK.gettransactionstatus()) {
						returnMap.put("PassFlag", commonFunction.getMultilingualMessage("IDS_LOGINIDLOCKED",
								userInfo.getSlanguagefilename()));
						return new ResponseEntity<>(returnMap, HttpStatus.OK);
					}
				}

			}

			if (userInfo.getNlogintypecode() == Enumeration.LoginType.ADS.getnlogintype()) {
				if (flag) {
					String returnAds = validateADSPassword(userInfo.getSloginid(), password,
							userInfo.getSlanguagefilename());
					sauditaction = "IDS_IDLETIMEADSLOGIN";
					returnMap.put("PassFlag",
							commonFunction.getMultilingualMessage(returnAds, userInfo.getSlanguagefilename()));
					if (!commonFunction.getMultilingualMessage(returnAds, userInfo.getSlanguagefilename())
							.equalsIgnoreCase(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
						sauditaction = returnAds + "FORADS";
					}

				} else {
					sauditaction = "IDS_IDLETIMEADSLOGOUT";
				}
			}

			else {
				/* Internal Login password Validation */
				if (flag) {
					if (objUsersData.getSpassword() != null) {
						String RtnPasswordEncrypt2 = null;
						RtnPasswordEncrypt2 = projectDAOSupport.decryptPassword("usermultirole", "nusermultirolecode",
								objUsersData.getNusermultirolecode(), "spassword");
						if (!(RtnPasswordEncrypt2.equals(password))) {
							sauditaction = resourcebundle.containsKey("IDS_IDLETIMEPASSWORDINCORRECT")
									? resourcebundle.getString("IDS_IDLETIMEPASSWORDINCORRECT").toUpperCase()
									: "IDS_IDLETIMEPASSWORDINCORRECT";
							returnMap.put("PassFlag", commonFunction.getMultilingualMessage("IDS_PASSWORDINCORRECT",
									userInfo.getSlanguagefilename()));

							int nremainingattempt = objUsersData.getNnooffailedattempt() - 1;
							if (nremainingattempt > 0) {
								jdbcTemplate.update("update usermultirole set nnooffailedattempt = "
										+ nremainingattempt
										+ " where nusermultirolecode = (select um.nusermultirolecode from usermultirole um"
										+ " where um.nusersitecode = " + userInfo.getNusersitecode()
										+ " and ndefaultrole = "
										+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");

							}
							if (nremainingattempt == 0) {
								jdbcTemplate.update("update users set nlockmode = "
										+ Enumeration.TransactionStatus.LOCK.gettransactionstatus()
										+ " where nusercode = " + userInfo.getNusercode());
							}
						} else {
							sauditaction = "IDS_IDLETIMELOGIN";
							returnMap.put("PassFlag", commonFunction.getMultilingualMessage("IDS_SUCCESS",
									userInfo.getSlanguagefilename()));
							
							// Added by gowtham on 18 July, ALPDJ21-27 - JWT
							returnMap.put("token",jwtUtilityFunction.generateToken(userInfo.getSsessionid(), userInfo.getNexpirytime()));
						}
					}
				} else {
					sauditaction = "IDS_IDLETIMELOGOUT";
				}
			}
			Map<String, Object> inputMap = insertSessionDetails(userInfo, nFlag);
			auditUtilityFunction.insertAuditAction(userInfo, sauditaction, scomments, inputMap);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}


	@Override
	public ResponseEntity<Object> changeOwner(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objMap = new HashMap<>();
		final int nuserrolecode = (int) inputMap.get("nuserrolecode");
		final int nusercode = (int) inputMap.get("nusercode");
		final String deputyLoginId = (String) inputMap.get("sdeputyid");
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo oldUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ndeputyusercode = oldUserInfo.getNdeputyusercode();
		final int ndeputyuserrole = oldUserInfo.getNdeputyuserrole();
		final String suserrolename = (String) inputMap.get("suserrolename");
		final String sQuery = "select u.nusercode, d.sdeptname, d.ndeptcode, u.ndeptcode, u.sloginid, concat(u.sfirstname,' ',u.slastname) susername"
				+ " from users u, department d where d.ndeptcode = u.ndeptcode" + " and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nusercode = " + nusercode;
		Users objUsers = (Users) jdbcUtilityFunction.queryForObject(sQuery, Users.class,jdbcTemplate);

		// ALPD-4262-Vignesh R-(01-06-2024)(Can't able to login in session logout appear
		// while using the deputy user in specific scenario.)
		UserInfo userInfo = new UserInfo(oldUserInfo);
		System.out.println("while2 ," + userInfo);
		userInfo.setNusercode(nusercode);
		userInfo.setNuserrole(nuserrolecode);
		userInfo.setNdeputyusercode(ndeputyusercode);
		userInfo.setNdeputyuserrole(ndeputyuserrole);
		userInfo.setSdeputyid(oldUserInfo.getSdeputyid());
		userInfo.setNmodulecode((short) -1);
		userInfo.setNformcode((short) -1);
		userInfo.setNreasoncode(-1);
		userInfo.setSreason("");
		//commented by sonia on 6th Aug 2024 for JIRA ID:ALPD-4181
		//userInfo.setSusername(oldUserInfo.getSusername());
		//userInfo.setSuserrolename(oldUserInfo.getSuserrolename());
		//userInfo.setSdeputyusername(objUsers.getSusername());
		//userInfo.setSdeputyuserrolename(suserrolename);
		//Added by sonia on 6th Aug 2024 for JIRA ID:ALPD-4181
		userInfo.setSusername(objUsers.getSusername());	
		userInfo.setSuserrolename(suserrolename);		
		userInfo.setSdeputyusername(oldUserInfo.getSusername());
		userInfo.setSdeputyuserrolename(oldUserInfo.getSuserrolename());
		userInfo.setSloginid(deputyLoginId);
		userInfo.setSdeptname(objUsers.getSdeptname());
		userInfo.setNdeptcode(objUsers.getNdeptcode());
		userInfo.setNusersitecode(oldUserInfo.getNusersitecode());
		userInfo.setSsitename(oldUserInfo.getSsitename());

		objMap.put("UserInfo", userInfo);
		objMap.putAll(
				getUserForms(nusercode, nuserrolecode, userInfo.getNmastersitecode(), userInfo.getSlanguagetypecode()));
		objMap.putAll(getControlRights(nuserrolecode, nusercode, userInfo.getNmastersitecode(),
				userInfo.getSlanguagetypecode(), userInfo));
		objMap.putAll(getTransactionStatus(userInfo));
		final List<String> listOfMultiLingual = commonFunction.getMultilingualMultipleMessage(
				Arrays.asList("IDS_LOGINID", "IDS_USERNAME", "IDS_USERROLE"), userInfo.getSlanguagefilename());
		final String scomments = listOfMultiLingual.get(0) + ": " + oldUserInfo.getSloginid() + " -> "
				+ userInfo.getSloginid() + "; " + listOfMultiLingual.get(1) + ": " + oldUserInfo.getSusername() + " -> "
				+ userInfo.getSusername() + "; " + listOfMultiLingual.get(2) + ": " + oldUserInfo.getSuserrolename()
				+ " -> " + userInfo.getSuserrolename() + ";";
		inputMap.put("sprimarykeyvalue", -1);
		inputMap.put("stablename", "login");
		auditUtilityFunction.insertAuditAction(userInfo, "IDS_CHANGEOWNER", scomments, inputMap);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	private Map<String, String> getLoggedUserProfile(final UserInfo userInfo) throws Exception {
		Map<String, String> outputMap = new HashMap<String, String>();
		final String sUserFileQuery = "select uf.suserimgname, uf.suserimgftp"
				+ " from users u, userfile uf where u.nusercode = uf.nusercode and u.nusercode = "
				+ userInfo.getNusercode() + " and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final UserFile userFile = (UserFile) jdbcUtilityFunction.queryForObject(sUserFileQuery, UserFile.class,jdbcTemplate);
		
		String UserImagePath = "";
		if (userFile != null && userFile.getSuserimgftp() != null) {
			final String sReportSettingQry = "select ssettingvalue from reportsettings where nreportsettingcode = 11 and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final String sProfilePath = (String) jdbcUtilityFunction.queryForObject(sReportSettingQry, String.class,jdbcTemplate);
			if (sProfilePath.isEmpty()) {

			} else {
				final Map<String, Object> map = ftpUtilityFunction.FileViewUsingFtp(userFile.getSuserimgftp(), -1, userInfo, sProfilePath,
						"");
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
						.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					UserImagePath = (String) map.get("AttachFile");
				}
			}
		}
		outputMap.put("UserImagePath", UserImagePath);
		return outputMap;
	}

	@Override
	public ResponseEntity<Object> validateEsignADSCredential(UserInfo userInfo, String password) throws Exception {
		final String returnMsg = validateADSPassword(userInfo.getSloginid(), password, userInfo.getSlanguagefilename());
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(returnMsg)) {
			return new ResponseEntity<>(returnMsg, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(returnMsg, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> insertAuditActionTable(UserInfo userInfo, String sauditaction, String scomments,
			final int nFlag) throws Exception {
		Map<String, Object> inputMap = insertSessionDetails(userInfo, nFlag);
		auditUtilityFunction.insertAuditAction(userInfo, sauditaction, scomments, inputMap);
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> changeSite(UsersSite usersSite) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String sMultiRoleQry = "select umr.nusermultirolecode,umr.ndefaultrole,umr.nuserrolecode,ur.suserrolename,"
				+ "umr.ntransactionstatus,"
				+ "us.nusercode from usermultirole umr,userrole ur ,userssite us where umr.nuserrolecode = ur.nuserrolecode"
				+ " and us.nusersitecode=umr.nusersitecode " + "and umr.nusersitecode=" + usersSite.getNusersitecode()
				+ " and umr.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and umr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by umr.ndefaultrole asc";
		List<UserMultiRole> lstMultirole = (List<UserMultiRole>) jdbcTemplate.query(sMultiRoleQry, new UserMultiRole());
		outputMap.put("UserMultiRole", lstMultirole);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getLoginTypeValidation(String sloginid, Language language,
			final int nusermultisitecode, final int nusermultirolecode, final int nuserrolecode) throws Exception {
		Map<String, Object> objMap = new HashMap<>();
		if (sloginid != null && !sloginid.isEmpty()) {
			final String sUserQuery = "select nusercode,sloginid,ntransactionstatus,nlockmode,nlogintypecode from users "
					+ "where sloginid =N'" + sloginid + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final Users objUsers = (Users) jdbcUtilityFunction.queryForObject(sUserQuery, Users.class,jdbcTemplate);
			if (objUsers != null) {
				if (objUsers.getNtransactionstatus() == Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_LOGINIDDEACTIVATED", language.getSfilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else if (objUsers.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
						.gettransactionstatus()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_LOGINUSERRETIRED", language.getSfilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else if (objUsers.getNlockmode() == Enumeration.TransactionStatus.LOCK.gettransactionstatus()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_LOGINIDLOCKED", language.getSfilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					final String sMultiRoleQry = "select umr.nusermultirolecode,umr.ndefaultrole,umr.nuserrolecode,ur.suserrolename,umr.ntransactionstatus,"
							+ "us.nusercode from usermultirole umr,userrole ur ,userssite us where umr.nuserrolecode = ur.nuserrolecode"
							+ " and us.nusersitecode=umr.nusersitecode " + "and umr.nusersitecode=" + nusermultisitecode
							+ " and umr.ntransactionstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " order by umr.ndefaultrole asc";

					List<UserMultiRole> lstMultirole = (List<UserMultiRole>) jdbcTemplate.query(sMultiRoleQry,	new UserMultiRole());
					String spassword = projectDAOSupport.decryptPassword("usermultirole", "nusermultirolecode",
							lstMultirole.get(0).getNusermultirolecode(), "spassword");
					if (spassword != null) {
						if (spassword.equals(Enumeration.ReturnStatus.FAILED.getreturnstatus())) {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_EXECUTESYMMETRICKEY",
									language.getSfilename()), HttpStatus.EXPECTATION_FAILED);
						} else {
							objMap.put("PassFlag", Enumeration.PasswordValidate.PASS.getPaswordvalidate());
						}
					} else {
						objMap.put("PassFlag", Enumeration.PasswordValidate.NEW_USER.getPaswordvalidate());
						PasswordPolicy objPasswordPolicy = getPasswordPolicyMsg(nuserrolecode);
						objMap.put("PasswordPolicy", objPasswordPolicy);
					}
					objMap.put("Users", objUsers);
					return new ResponseEntity<>(objMap, HttpStatus.OK);
				}
			} else {
				return new ResponseEntity<>(Enumeration.ReturnStatus.INVALIDUSER.getreturnstatus(),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_LOGINIDSHOULDNOTBEEMPTY", language.getSfilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	

	// ALPD-4102
	@Override
	public ResponseEntity<Object> getAboutInfo(final UserInfo userInfo) throws Exception {

		final String sQuery = "select jsondata from licenseconfiguration where nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final LicenseConfiguration objLicenseConfiguration = (LicenseConfiguration) jdbcUtilityFunction.queryForObject(sQuery,LicenseConfiguration.class,jdbcTemplate);

		return new ResponseEntity<>(objLicenseConfiguration, HttpStatus.OK);

	}
	
	
//	ALPD-4393 17/06/2024 Abdul Gaffoor.A To validate ads password of login User and to get ads user details and update it
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> validateADSPassword(Map<String, Object> objinputmap) throws Exception {
		String loginId = (String) objinputmap.get("sloginid");
		String password = (String) objinputmap.get("spassword");
		String languageFileName = (String) objinputmap.get("slanguagefilename");

		
		if (loginId == null || loginId.isEmpty() || password == null || password.isEmpty()) {
			String missingParamMessage = commonFunction.getMultilingualMessage("IDS_MISSING_CREDENTIALS",
					languageFileName);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(missingParamMessage);
		}

		try {
			List<ADSSettings> adsSettingsList = fetchActiveADSSettings();

			if (adsSettingsList.isEmpty()) {
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
						.body(commonFunction.getMultilingualMessage("IDS_ADSSERVERUNAVAILABLE", languageFileName));
			}
			
			ResponseEntity<Object> validationResponse = null;
			for (int index = 0; index < adsSettingsList.size(); index++) {
				ADSSettings objdefault = adsSettingsList.get(index);
				objdefault.setSloginid(loginId);
				objdefault.setSpassword(password);
				validationResponse = getAdsdetails(objdefault, languageFileName);
				if (validationResponse != null && validationResponse.getStatusCode().value() == 200) {
					break;
				}
			}

			if (validationResponse.hasBody()) {
				if (validationResponse.getStatusCode().value() == 200) {
					Map<String, Object> objvalres= (Map<String, Object>) validationResponse.getBody();

					final String attQry=" select nadsattributecode,slimscolumn from adsattributes where nadsattributecode in ("
							+ Enumeration.ADSAttributes.EC_NO.getNadsattributecode() +","
							+ Enumeration.ADSAttributes.LOGIN_ID.getNadsattributecode() +","
							+ Enumeration.ADSAttributes.EMAIL_ID.getNadsattributecode() +") and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nadsattributecode ";
					
			        List<Map<String, Object>> lstAttributes = jdbcTemplate.queryForList(attQry);
			        
			        Map<Integer, String> resultMap = lstAttributes.stream()
			        	    .collect(Collectors.toMap(
			        	        attribute -> (Integer) attribute.get("nadsattributecode"),
			        	        attribute -> (String) attribute.get("slimscolumn")
			        	    ));
			        
					if(objvalres.containsKey(resultMap.get(Enumeration.ADSAttributes.EC_NO.getNadsattributecode()).toString()) && objvalres.get(resultMap.get(Enumeration.ADSAttributes.EC_NO.getNadsattributecode()).toString()) != "") {
						String mailid=null;
						if(objvalres.containsKey(resultMap.get(Enumeration.ADSAttributes.EMAIL_ID.getNadsattributecode()).toString()) && objvalres.get(resultMap.get(Enumeration.ADSAttributes.EMAIL_ID.getNadsattributecode()).toString()).toString() != "" ) {
							mailid="N'"+ stringUtilityFunction.replaceQuote(objvalres.get(resultMap.get(Enumeration.ADSAttributes.EMAIL_ID.getNadsattributecode()).toString()).toString() )+"'";
						}
						String Qry = "select nusercode from users where sempid= N'"+ stringUtilityFunction.replaceQuote(objvalres.get(resultMap.get(Enumeration.ADSAttributes.EC_NO.getNadsattributecode()).toString()).toString() )+"' and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final Users objUsers = (Users) jdbcUtilityFunction.queryForObject(Qry, Users.class,jdbcTemplate);
						if (objUsers != null) {
						        Qry= "update users set sloginid = N'"+ stringUtilityFunction.replaceQuote( objvalres.get(resultMap.get(Enumeration.ADSAttributes.LOGIN_ID.getNadsattributecode()).toString()).toString())+ "', semail = "+ mailid
						                +" where sempid = N'"+ stringUtilityFunction.replaceQuote(objvalres.get(resultMap.get(Enumeration.ADSAttributes.EC_NO.getNadsattributecode()).toString()).toString()) +"' and nstatus = " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
						        jdbcTemplate.execute(Qry);
						        Language langObj=new Language();
						        langObj.setSfilename(languageFileName);						        
						        return getLoginValidation((String) objvalres.get(resultMap.get(Enumeration.ADSAttributes.LOGIN_ID.getNadsattributecode()).toString()), langObj, 0);
						        
						}else {
							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_ECNOUNAVAILABLE", languageFileName),
									HttpStatus.EXPECTATION_FAILED);
						}
					}
					else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_ECNOISEMPTY", languageFileName),
								HttpStatus.EXPECTATION_FAILED);
					}
			
				}else {
					return validationResponse;
				}
			} else {
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
						.body(commonFunction.getMultilingualMessage("IDS_ADSNOTSYNC", languageFileName));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(commonFunction.getMultilingualMessage("IDS_ADSNOTSYNC", languageFileName));
		}
	}
//	ALPD-4393 17/06/2024 Abdul Gaffoor.A To fecth ADS Settings detail
	private List<ADSSettings> fetchActiveADSSettings() {
		String query = "SELECT sldaplink, sdomainname FROM adssettings " + "WHERE nldapstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		

//		return getJdbcTemplate().query(query, new Object[] { activeStatus, activeStatus }, (rs, rowNum) -> {
//			ADSSettings settings = new ADSSettings();
//			settings.setSldaplink(rs.getString("sldaplink"));
//			settings.setSdomainname(rs.getString("sdomainname"));
//			return settings;
//		});
		
		return jdbcTemplate.query(query, new ADSSettings());
	}

	
//	ALPD-4393 17/06/2024 Abdul Gaffoor.A To validate ads password of login User and to get ads user details 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ResponseEntity<Object> getAdsdetails(ADSSettings adsSetting, String languageFileName ) throws Exception {

		String str = "";

		Map<String, Object> objres = new HashMap<>() ;
		List<Map<String, Object>> lstADSUsers = new ArrayList<Map<String, Object>>();

		if (adsSetting != null && adsSetting.getSloginid() != null && !adsSetting.getSloginid().isEmpty() && adsSetting.getSloginid() != "") {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.PROVIDER_URL, adsSetting.getSldaplink());
			env.put(Context.SECURITY_PRINCIPAL, adsSetting.getSloginid() + "@" + adsSetting.getSdomainname());
			env.put(Context.SECURITY_CREDENTIALS, adsSetting.getSpassword());
			env.put(Context.REFERRAL, "follow");

			try {
				DirContext ctx = new InitialDirContext(env);
				System.out.println("Connected");
				SearchControls searchCtls = new SearchControls();
				searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				
				final String sQuery="select sattributename,slimscolumn from adsattributes where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by 1";
				List<Map<String, Object>> sattributeList=jdbcTemplate.queryForList(sQuery);

				Map<String, Object> columnMap = sattributeList.stream().collect(Collectors.toMap(
						item -> String.valueOf(item.get("slimscolumn")),item -> item.get("sattributename")));  
				
				List<String> valuesList = new ArrayList<>(columnMap.values().stream().map(Object::toString).collect(Collectors.toList()));
				
				String[] attrIDs = valuesList.toArray(new String[0]);
				searchCtls.setReturningAttributes(attrIDs);
				String FILTER = "(&(objectClass=user)(objectCategory=person)((sAMAccountName="
						+ adsSetting.getSloginid() + ")))";
				NamingEnumeration answer = ctx.search("", FILTER, searchCtls);

				while (answer.hasMoreElements())
				{
					SearchResult sr = (SearchResult)answer.next();
					Attributes attrs = sr.getAttributes();
					LOGGER.info(attrs.toString());
					Map<String, Object> objAttrs = new HashMap<>();
					if(!attrs.toString().equals("No attributes")) {
						for (Map.Entry<String, Object> entry : columnMap.entrySet()) {
							String key = entry.getKey();
							String valueKey = String.valueOf(entry.getValue());				        	 
							String value = attrs.get(valueKey)!=null?attrs.get(valueKey).get().toString():"";
							objAttrs.put(key, value);
						}
						lstADSUsers.add(objAttrs);
					}

				}
				ctx.close();

                objres = lstADSUsers.get(0);
				return new ResponseEntity<>(objres, HttpStatus.OK);

			} catch (AuthenticationNotSupportedException ex) {
				str = "IDS_AUTHENTICATIONNOTSUPPORTEDBYSERVER";
				System.out.println("The authentication is not supported by the server");

			} catch (AuthenticationException ex) {
				String errorcodes[] = { "52e", "525", "530", "531", "532", "533", "701", "773" };
				for (int i = 0; i < errorcodes.length; i++) {
					String strser = errorcodes[i];
					if (ex.getLocalizedMessage().toString().contains(strser)) {

						switch (errorcodes[i]) {

						case "52e":
							str = "IDS_WRONGCREDENTIAL";
							break;

						case "525":
							str = "IDS_USERNOTFOUND";
							break;

						case "530":
							str = "IDS_NOTPERMITTEDCONTACTADMIN";
							break;

						case "531":
							str = "IDS_NOTPERMITTED";
							break;

						case "532":
							str = "IDS_PASSWORDEXPIRED";
							break;

						case "533":
							str = "IDS_ACCOUNTDISABLED";
							break;

						case "701":
							str = "IDS_ACCOUNTEXPIRED";
							break;

						case "773":
							str = "IDS_USERMUSTRESETPASSWORD";
							break;

						default:
							str = "IDS_UNKNOWNERROR";
							break;
						}
						break;
					}
				}

			} catch (NamingException ex) {
				System.out.println("Unknown Host...check Server name");
				str = "IDS_UNKNOWNHOSTCHECKSERVERNAME";

			} catch (Exception ex) {
				System.out.println("error when trying to create the context");
				str = "IDS_ERRORWHENCREATECONTEXT";
			}
		}
		return new ResponseEntity<>(
				commonFunction.getMultilingualMessage(str, languageFileName),
				HttpStatus.EXPECTATION_FAILED);
	}
	
	// Added by Gowtham R ALPD-5190, for Vacuum Analysis
		@Override
		public ResponseEntity<Object> getJavaTime() throws Exception {
			Map<String, Object> map = new HashMap<String, Object>();
			final LocalDateTime today = LocalDateTime.now();
			final int hours = today.getHour();
			final int minutes = today.getMinute();
			final int seconds = today.getSecond();
			map.put("Hours", hours);
			map.put("Minutes", minutes);
			map.put("Seconds", seconds);						
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
		
		public List<MfaSettings> getMFASettings(final UserInfo userInfo) throws Exception {
			final String sQuery = "select smfasettingname,smfasettingvalue,nmfasettingcode from mfasetting where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<MfaSettings> listMFASettings = (List<MfaSettings>) jdbcTemplate.query(sQuery, new MfaSettings());
		
		    return listMFASettings;
		}
		
	//ALPDJ21-19--Added by vignesh(21-07-2025)--for OTP send to the Email.
		public ResponseEntity<Map<String, Object>> sendOTPMail(Map<String, Object> inputMap) throws Exception {

			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

			Map<String, Object> returnMap = new HashMap<>();
			returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

			final EmailHost objEmailHost = getEmailHost(userInfo);
			
			boolean showEmailOTPModal = false;
			
			if (objEmailHost == null) 
			{
				returnMap.put("rtn", "IDS_CONFIGUREEMAILHOST");
			}
			else
			{				
				String semail = "";

				if (inputMap.containsKey("emailidChange")) {
					semail = inputMap.get("sreceivermailid").toString();
				} else {
						semail = projectDAOSupport.getUserEmail(userInfo);
				}

				final Properties properties = new Properties();

				properties.put("mail.smtp.auth", "true");
				properties.put("mail.smtp.starttls.enable", "true");
					properties.put("mail.smtp.host", objEmailHost.getShostname());
					properties.put("mail.smtp.port", objEmailHost.getNportno());
	
				// TLS1.2 for SMTP Authentication
				properties.put("mail.smtp.starttls.required", "true");
				properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

				final String senderMail = objEmailHost.getSemail();

				final String password = passwordUtilityFunction.decryptPassword("emailhost", "nemailhostcode",
						objEmailHost.getNemailhostcode(), "spassword");

				jakarta.mail.Session session = jakarta.mail.Session.getInstance(properties, new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(senderMail, password);
					}
				});
				String sOneTimePassword = "";
				//final String sonetimepasswordcode = UUID.randomUUID().toString();

				final String soneTimePasswordCode = projectDAOSupport.idGenerator(10, "alphanumeric");

	//			try {
					//sOneTimePassword = generateOtp();
					sOneTimePassword = projectDAOSupport.idGenerator(6, "number");

					final String sbody = commonFunction.getMultilingualMessage("IDS_MFAREFID",
							userInfo.getSlanguagefilename()) + soneTimePasswordCode + "<br><br><br><b> "
							+ commonFunction.getMultilingualMessage("IDS_OTP", userInfo.getSlanguagefilename())
							+ " " + sOneTimePassword + "</b>";

					final String ssubject = commonFunction.getMultilingualMessage("IDS_MAILOTPSUBJECT",
							userInfo.getSlanguagefilename());

					final Message message = projectDAOSupport.prepareMessage(session, senderMail, semail, ssubject, sbody,"");
					message.setContent(sbody, "text/html");

					try
					{
						Transport.send(message);
								
						showEmailOTPModal = true;

						final List<MfaSettings> settingList = (List<MfaSettings>) getMFASettings(userInfo);

						final Map<Short, String> mapMFASettings= settingList.stream()
								.collect(Collectors.toMap(MfaSettings::getNmfasettingcode, MfaSettings::getSmfasettingvalue));			


						final String notpExpiryTime = (String) mapMFASettings.get((short) 1);

						final String nNeedEmailEdit = (String) mapMFASettings.get((short) 2);					 

						returnMap.put("notpexpiredtime",notpExpiryTime);
						returnMap.put("nNeedEmailEdit",nNeedEmailEdit);
						 
						projectDAOSupport.insertOneTimePassword(soneTimePasswordCode, sOneTimePassword, userInfo);
											
						LOGGER.info("OTP Sent Successfully");
					} catch (Exception e) {
						returnMap.put("rtn", commonFunction.getMultilingualMessage("IDS_HOSTCONNECTIONISSUE", userInfo.getSlanguagefilename()));
						LOGGER.error(e.getMessage());
		}

					//showEmailOTPModal = true;



//				} catch (Exception e) {
//					returnMap.put("rtn", "IDS_INVALIDEMAILID");
//					LOGGER.error(e.getMessage());
//				}

				// returnMap.put("sonetimepassword", sonetimepassword);
				
				returnMap.put("sreceivermailid", semail);
				returnMap.put("sonetimepasswordcode", soneTimePasswordCode);
			}
			returnMap.put("showEmailOTPModal", showEmailOTPModal);
			return new ResponseEntity<Map<String, Object>>(returnMap, HttpStatus.OK);
		}



		//ALPDJ21-19--Added by vignesh(21-07-2025)--for OTP send to the Email.
		public ResponseEntity<Object> confirmOTP(final Map<String, Object> inputMap, final HttpServletRequest request,
				final HttpServletResponse response) throws Exception {

			Map<String, Object> map = new HashMap<>();
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

			final String soneTimePassword = inputMap.get("sonetimepassword").toString();
			final String soneTimePasswordCode = inputMap.get("sonetimepasswordcode").toString();

			final int nmfaTypeCode = (int) inputMap.get("nmfatypecode");

			int nactivestatus = Enumeration.TransactionStatus.YES.gettransactionstatus();
			if (inputMap.containsKey("nactivestatus")) {
				nactivestatus = (int) inputMap.get("nactivestatus");

			}
			int nmfaNeed = -1;
			if (inputMap.containsKey("mfaNeed")) {
				nmfaNeed = (int) inputMap.get("mfaNeed");

			}
			final String dateFormat ="yyyy-MM-ddThh24:mi:ss";
			String sQuery = "select *, "
							+ " COALESCE(TO_CHAR(dcreateddate,  '"+ dateFormat + "'), '') AS screateddate "
							+ " from onetimepassword where sonetimepasswordcode='" + soneTimePasswordCode
							+ "' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ " and nsitecode=" + objUserInfo.getNmastersitecode();

			final OneTimePassword objOneTimePassword = (OneTimePassword) jdbcUtilityFunction.queryForObject(sQuery,
					OneTimePassword.class, jdbcTemplate);

			boolean isValidOtp = false;
			boolean isExpiry = false;

			if (objOneTimePassword != null) {

//				final LocalDateTime now = LocalDateTime.now();
//				
//				final LocalDateTime createdDateTime = LocalDateTime.ofInstant(objOneTimePassword.getDcreateddate(),
//						ZoneId.systemDefault());
//
//				final int nminutes = (int) ChronoUnit.MINUTES.between(createdDateTime, now);

				final Instant currentDateTime = dateUtilityFunction.getCurrentDateTime(objUserInfo);				
				
				final Duration duration = Duration.between(
						dateUtilityFunction.convertStringDateToUTC(objOneTimePassword.getScreateddate(), objUserInfo, true), 
						currentDateTime
						);

		        // Get the difference in minutes
		        long minutesDifference = duration.toMinutes();

				final String mfaSettingsQuery = "select smfasettingvalue from mfasetting where nmfasettingcode ="
												+ Enumeration.MFASettings.OTP_EXPIRY_MINUTES.getNmfasettingcode()
												+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
				final String sotpExpiryTime = (String) jdbcUtilityFunction.queryForObject(mfaSettingsQuery, String.class,
						jdbcTemplate);

				final int notpExpiryTime = Integer.parseInt(sotpExpiryTime);

				if (notpExpiryTime < minutesDifference) {
					isExpiry = true;
					sQuery = " delete from onetimepassword where sonetimepasswordcode='" + soneTimePasswordCode 
							 + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and nsitecode="+objUserInfo.getNmastersitecode()+"";
					jdbcTemplate.execute(sQuery);
				}

				if (!isExpiry) {
					if (objOneTimePassword.getSonetimepassword().equals(soneTimePassword)) {
						isValidOtp = true;
						sQuery = "delete from onetimepassword where sonetimepasswordcode='" + soneTimePasswordCode
								+ "' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								+ " and nsitecode="+objUserInfo.getNmastersitecode()+"";
						
						jdbcTemplate.execute(sQuery);
						
						if (inputMap.containsKey("nFlag")) {

							final UserMFA objUserMfa = projectDAOSupport.getUserMFA(objUserInfo.getNusercode(), nmfaNeed,false);

							String sinsertQuery = "";

							if (objUserMfa == null) {	

								if (inputMap.containsKey("emailidChange")) {
									sinsertQuery = sinsertQuery + "update users set semail='"
											+ inputMap.get("sreceivermailid") + "' where nusercode="
											+ objUserInfo.getNusercode() + ";";
								}

								sinsertQuery = sinsertQuery + " insert into usermfa("
										+ "  nusercode, nmfatypecode,nactivestatus, dmodifieddate, nsitecode, nstatus)"
										+ " VALUES ( " + objUserInfo.getNusercode() + ", " + nmfaTypeCode
										+ "," + nactivestatus + ", '"
										+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
										+ objUserInfo.getNmastersitecode() + ", "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

								jdbcTemplate.execute(sinsertQuery);

							} else {

								if (nmfaTypeCode == objUserMfa.getNmfatypecode()) {
									return new ResponseEntity<>(commonFunction.getMultilingualMessage(
											Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
											objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
								} else {
									sinsertQuery = "update usermfa set nmfatypecode="
											+ nmfaTypeCode + ",dmodifieddate='"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where nusercode="
											+ objUserInfo.getNusercode() + "";
									
									jdbcTemplate.execute(sinsertQuery);
								}

							}
						} else {
							final int nusercode = (int) inputMap.get("nusercode");
							final int nusermultisitecode = (int) inputMap.get("nusermultisitecode");
							final String slanguageTypeCode = (String) inputMap.get("slanguagetypecode");

							Map<String, Object> mapUserInfo = collectUserInfo(inputMap);

							final String passwordpolicyrolequery = "select up.npolicycode,pp.nexpirypolicyrequired,pp.nremainderdays from userrolepolicy up,usermultirole umr,passwordpolicy pp"
									+ " where umr.nuserrolecode=up.nuserrolecode and pp.npolicycode = up.npolicycode"
									+ " and up.ntransactionstatus= "
									+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
									+ " and umr.nusersitecode=" + nusermultisitecode + " and umr.ndefaultrole = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and umr.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and up.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pp.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							final PasswordPolicy objPasswordPolicy = (PasswordPolicy) jdbcUtilityFunction
									.queryForObject(passwordpolicyrolequery, PasswordPolicy.class, jdbcTemplate);

							final String updateQry = "update usermultirole set nnooffailedattempt = "
									+ "(select nnooffailedattempt from passwordpolicy where npolicycode = "
									+ objPasswordPolicy.getNpolicycode() + ")"
									+ " where nusermultirolecode = (select nusermultirolecode from usermultirole "
									+ " where nusersitecode = " + nusermultisitecode + " and ndefaultrole = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

							sQuery = "select nusermultirolecode, nusersitecode, nuserrolecode from usermultirole"
									+ " where nusermultirolecode = " + (int) inputMap.get("nusermultirolecode")
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

							final UserMultiRole objUserMultiRoleTemp = (UserMultiRole) jdbcUtilityFunction
									.queryForObject(sQuery, UserMultiRole.class, jdbcTemplate);

//							String SiteAdditionalInfo = (String) ((Map<String, Object>) mapUserInfo.get("Settings"))
//									.get((short) 23);

							final String mfaNeed = (String) ((Map<String, Object>) mapUserInfo.get("Settings"))
									.get((short) 83);

							final UserInfo userInfo = (UserInfo) mapUserInfo.get("UserInfo");

							final UserMFA objUsersAuthentication = projectDAOSupport.getUserMFA(nusercode, nmfaNeed,false);

							if (objUsersAuthentication == null
									&& nmfaNeed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								final String sinsertQuery = "INSERT INTO public.usermfa("
										+ "  nusercode, nmfatypecode,nactivestatus, dmodifieddate, nsitecode, nstatus)"
										+ " VALUES ( " + nusercode + ", " + nmfaTypeCode + ","
										+ nactivestatus + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
										+ "', "+userInfo.getNmastersitecode()+", "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
								
								jdbcTemplate.execute(sinsertQuery);
							}

							if (mfaNeed.equals("3")) {
								map.putAll(projectDAOSupport.getMFAType(userInfo));
							}
							
							HttpSession session = request.getSession(true);
							userInfo.setSsessionid(session.getId());
							//userInfo.setSsessionid(UUID.randomUUID().toString());
							
							userInfo.setShostip(request.getRemoteAddr());
							
							clearsessiondetail(userInfo);
							
							map.put("UserInfo", userInfo);

							map.putAll(getUserForms(nusercode, objUserMultiRoleTemp.getNuserrolecode(),
									userInfo.getNmastersitecode(), slanguageTypeCode));
							map.putAll(getControlRights(objUserMultiRoleTemp.getNuserrolecode(), nusercode,
									userInfo.getNmastersitecode(), slanguageTypeCode, userInfo));
							map.putAll(getTransactionStatus(userInfo));
							map.putAll(getLoggedUserProfile(userInfo));
							map.putAll(mapUserInfo);

							// Added by gowtham on 18 July, ALPDJ21-27 - JWT
							map.put("token",jwtUtilityFunction.generateToken(userInfo.getSsessionid(), userInfo.getNexpirytime()));

							//fixed merge issue - L.Subashini
							Map<String, Object> auditMap = new HashMap<>();
							auditMap.put("sprimarykeyvalue", -1);
							auditMap.put("stablename", "login");
							
							auditUtilityFunction.insertAuditAction(userInfo, "IDS_LOGIN",
									commonFunction.getMultilingualMessage("IDS_LOGIN", userInfo.getSlanguagefilename()),
									auditMap);

							jdbcTemplate.execute(updateQry);
							//map.put("showEmailOTPModal", false);
						}
					}
				}
			}
			map.put("isValidOtp", isValidOtp);
			map.put("isExpiry", isExpiry);

			return new ResponseEntity<>(map, HttpStatus.OK);

		}

//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
		public ResponseEntity<Object> resendOTP(Map<String, Object> inputMap) throws Exception {

			Map<String, Object> responseMap = new HashMap<String, Object>();

			Map<String, Object> mailSentData=new HashMap<>();

			mailSentData.putAll(sendOTPMail(inputMap).getBody()); 

			responseMap.put("showEmailOTPModal", mailSentData.get("showEmailOTPModal"));

			responseMap.put("sonetimepasswordcode", mailSentData.get("sonetimepasswordcode"));

			return new ResponseEntity<>(responseMap, HttpStatus.OK);

		}

//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
		public ResponseEntity<Object> checkAuthenticationByUser(Map<String, Object> inputMap) throws Exception {

			final int nusercode = (int) inputMap.get("nusercode");
			
			Map<String, Object> responseMap = new HashMap<String, Object>();

			final String spassword = (String) inputMap.get("spassword");
			final int nusermultisitecode = (int) inputMap.get("nusermultisitecode");
			final int nuserrolecode = (int) inputMap.get("nuserrolecode");


			boolean isNeedNewPswd = false;
			final String strLanguage = "select nlanguagecode, slanguagename, sfilename, slanguagetypecode, sreportingtoolfilename, ndefaultstatus, nsitecode, nstatus from Language where slanguagetypecode= N'"
					+ (String) inputMap.get("slanguagetypecode") + "'";
			final Language objLanguage = (Language) jdbcUtilityFunction.queryForObject(strLanguage, Language.class,
					jdbcTemplate);

			final Map<String, Object> obj = validateUserStatus(nusercode, nuserrolecode, nusermultisitecode);
			String returnStr = (String) obj.get("rtn");
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(returnStr)) {
				final Boolean obj1 = validateSite(nusercode, (int) inputMap.get("nsitecode"));
				if (obj1) {
					String sQuery = "select umr.nusermultirolecode ,us.nsitecode,umr.nuserrolecode ,umr.ndefaultrole,ur.suserrolename,umr.spassword,"
							+ "umr.ntransactionstatus,us.nusercode,us.nusersitecode, umr.nnooffailedattempt"
							+ " from usermultirole umr,userrole ur ,userssite us"
							+ " where umr.nuserrolecode = ur.nuserrolecode and us.nusersitecode=umr.nusersitecode"
							+ " and umr.nusersitecode=" + nusermultisitecode + " and umr.ntransactionstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.ndefaultrole = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and us.nusercode="
							+ nusercode + " and umr.nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final UserMultiRole objUserMultiRole = (UserMultiRole) jdbcUtilityFunction.queryForObject(sQuery,
							UserMultiRole.class, jdbcTemplate);

					if (objUserMultiRole != null) {
						final String suserpassword = projectDAOSupport.decryptPassword("usermultirole",
								"nusermultirolecode", objUserMultiRole.getNusermultirolecode(), "spassword");

						if (suserpassword != null && !suserpassword.isEmpty()) {
							final String siteQuery = " select case when s.nismultisite = 3 then s.nsitecode else -1 end nismultisite, s.nsitecode,s.ssitename, s.ntimezonecode, us.nusercode,tzd.sdatetimeformat,tzd.stimezoneid, tzd.sgmtoffset"
									+ " from userssite us, site s,timezone tzd where s.nsitecode = us.nsitecode and tzd.ntimezonecode = s.ntimezonecode "
									+ " and tzd.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and us.nusersitecode = " + nusermultisitecode;

							final Site objSite = (Site) jdbcUtilityFunction.queryForObject(siteQuery, Site.class,
									jdbcTemplate);

							Instant currenDate = null;
							if (objSite.getNtimezonecode() > 0) {
								currenDate = LocalDateTime.now().atZone(ZoneId.of(objSite.getStimezoneid())).toInstant()
										.truncatedTo(ChronoUnit.SECONDS);
							} else {
								currenDate = LocalDateTime.now().toInstant(ZoneOffset.UTC)
										.truncatedTo(ChronoUnit.SECONDS);
							}

							sQuery = "select DATE_PART('day', (select COALESCE(dpasswordvalidatedate,'" + currenDate
									+ "') " + "from usermultirole where nusersitecode = " + nusermultisitecode
									+ " and ndefaultrole = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " )- '" + currenDate + "')";
							final int Passwordexpireremaindays = (int) jdbcUtilityFunction.queryForObject(sQuery,
									Integer.class, jdbcTemplate);

							final String passwordpolicyrolequery = "select up.npolicycode,pp.nexpirypolicyrequired,pp.nremainderdays from userrolepolicy up,usermultirole umr,passwordpolicy pp"
									+ " where umr.nuserrolecode=up.nuserrolecode and pp.npolicycode = up.npolicycode"
									+ " and up.ntransactionstatus= "
									+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
									+ " and umr.nusersitecode=" + nusermultisitecode + " and umr.ndefaultrole = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and umr.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and up.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pp.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

							final PasswordPolicy objPasswordPolicy = (PasswordPolicy) jdbcUtilityFunction
									.queryForObject(passwordpolicyrolequery, PasswordPolicy.class, jdbcTemplate);

							if (objPasswordPolicy != null) {
								if (objPasswordPolicy.getNexpirypolicyrequired() == Enumeration.TransactionStatus.YES
										.gettransactionstatus()) {
									if (Passwordexpireremaindays <= objPasswordPolicy.getNremainderdays()
											&& Passwordexpireremaindays > 0) {
										responseMap.put("PasswordAlertDay", Passwordexpireremaindays);
									} else if (Passwordexpireremaindays <= 0) {
										responseMap.put("PassFlag",
												Enumeration.PasswordValidate.EXPIRED.getPaswordvalidate());
										isNeedNewPswd = true;
									}
								}
								if (!isNeedNewPswd) {
									Map<String, Object> mapUserInfo = collectUserInfo(inputMap);

									if (mapUserInfo == null) {
										return new ResponseEntity<>("Language not available", HttpStatus.CONFLICT);
									} else {
										final UserInfo objUserInfo = (UserInfo) mapUserInfo.get("UserInfo");

										if (suserpassword.equals(spassword)) {
											
											final String mfaSettingsQuery = "select ssettingvalue from settings where nsettingcode ="
													+ Enumeration.Settings.MFA.getNsettingcode()
													+ " and nstatus= "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
											final String sMfaNeed = (String) jdbcUtilityFunction
													.queryForObject(mfaSettingsQuery, String.class, jdbcTemplate);

											final int nmfaNeed = Integer.parseInt(sMfaNeed);
											final List<MfaSettings> settingList = getMFASettings(objUserInfo);

											final Map<Short, String> mapMFASettings= settingList.stream()
													.collect(Collectors.toMap(MfaSettings::getNmfasettingcode, MfaSettings::getSmfasettingvalue));			
																						  
											final String notpExpiryTime = (String) mapMFASettings.get((short) 1);											
											  
											final String nNeedEmailEdit = (String) mapMFASettings.get((short) 2);	
											
											final UserMFA objUsersAuthentication = projectDAOSupport.getUserMFA(nusercode, nmfaNeed,false);
												
											responseMap.put("notpexpiredtime",notpExpiryTime);
											responseMap.put("nNeedEmailEdit",nNeedEmailEdit);
											
											if (objUsersAuthentication != null) {
												if (objUsersAuthentication
														.getNmfatypecode() == Enumeration.AuthenticationType.EMAIL
																.getNmfatypecode()) {

//													final String sonetimepasswordcode = UUID.randomUUID().toString();
//													inputMap.put("sonetimepasswordcode", sonetimepasswordcode);
//													Map<String, Object> mailSendData = (inputMap,objUserInfo);
//
//													insertOneTimePassword(sonetimepasswordcode,
//															mailSendData.get("sonetimepassword").toString(), objUserInfo);

													responseMap.put("UserInfo", objUserInfo);
													responseMap.put("showEmailOTPModal",true);
													//responseMap.put("sonetimepasswordcode", sonetimepasswordcode);
													responseMap.put("sreceivermailid", projectDAOSupport.getUserEmail(objUserInfo));
													responseMap.put("nmfatype",objUsersAuthentication.getNmfatypecode());

												}
											} else {
												final EmailHost objEmailHost = getEmailHost(objUserInfo);
												
												if(objEmailHost == null) 
												{
													returnStr = commonFunction.getMultilingualMessage("IDS_CONFIGUREEMAILHOST", objLanguage.getSfilename());
												}
												else {
													if (nmfaNeed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
														
													responseMap.put("newUserAuth", true);
														responseMap.put("nmfatype", Enumeration.TransactionStatus.NA.gettransactionstatus());
													responseMap.put("UserInfo", objUserInfo);
														responseMap.put("sreceivermailid", projectDAOSupport.getUserEmail(objUserInfo));
														responseMap.putAll(projectDAOSupport.getMFAType(objUserInfo));
												}
											}
											}
										} else {
											int nremainingattempt = objUserMultiRole.getNnooffailedattempt() - 1;
											if (nremainingattempt >= 0) {
												String updateQry = "update usermultirole set nnooffailedattempt = "
														+ nremainingattempt
														+ " where nusermultirolecode = (select um.nusermultirolecode from usermultirole um"
														+ " where um.nusersitecode = " + nusermultisitecode
														+ " and ndefaultrole = "
														+ Enumeration.TransactionStatus.YES.gettransactionstatus()
														+ " and nstatus = "
														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
														+ ")";
												jdbcTemplate.update(updateQry);
											}
											final List<String> listOfMultilingual = commonFunction
													.getMultilingualMultipleMessage(
															Arrays.asList("IDS_WRONGCREDENTIAL", "IDS_YOUARELEFTWITH",
																	"IDS_MOREATTEMPTS", "IDS_LOGINID", "IDS_USERNAME",
																	"IDS_ENTEREDINCORRECTPSWD",
																	"IDS_DUETOWRONGCREDENTIAL", "IDS_LOGINIDLOCKED"),
															objUserInfo.getSlanguagefilename());
											String sComments = listOfMultilingual.get(3) + ": "
													+ objUserInfo.getSloginid() + ";" + listOfMultilingual.get(4) + ": "
													+ objUserInfo.getSusername() + ";";
											if (nremainingattempt == 0) {
												jdbcTemplate.update("update users set nlockmode = "
														+ Enumeration.TransactionStatus.LOCK.gettransactionstatus()
														+ " where nusercode = " + nusercode);
												returnStr = listOfMultilingual.get(7);
												sComments = sComments + " " + returnStr + " "
														+ listOfMultilingual.get(6) + ";";
											} else {
												returnStr = listOfMultilingual.get(0) + ". " + listOfMultilingual.get(1)
														+ " " + nremainingattempt + " " + listOfMultilingual.get(2)
														+ ".";
												sComments = sComments + " " + listOfMultilingual.get(5);
											}
											Map<String, Object> inputMap1 = new HashMap<>();
											inputMap1.put("sprimarykeyvalue", -1);
											inputMap1.put("stablename", "login");
											auditUtilityFunction.insertAuditAction(objUserInfo, "IDS_WRONGCREDENTIAL",
													sComments, inputMap1);
										}

									}
								}

							} else {
								returnStr = commonFunction.getMultilingualMessage("IDS_PASSWORDPOLICYNOTAVAILABLE",
										objLanguage.getSfilename());
							}

						} else {
							responseMap.put("PassFlag", Enumeration.PasswordValidate.NEW_USER.getPaswordvalidate());
							isNeedNewPswd = true;
						}
						if (isNeedNewPswd) {
							Map<String, Object> mapUserInfo = collectUserInfo(inputMap);
							final UserInfo userInfo = (UserInfo) mapUserInfo.get("UserInfo");
							responseMap.put("UserInfo", userInfo);
							PasswordPolicy objPasswordPolicy1 = getPasswordPolicyMsg(
									objUserMultiRole.getNuserrolecode());
							responseMap.put("PasswordPolicy", objPasswordPolicy1);
						}
					} else {
						returnStr = commonFunction.getMultilingualMessage("IDS_USERMULTIROLENOTAVAILABLE",
								objLanguage.getSfilename());
					}

				} else {
					returnStr = commonFunction.getMultilingualMessage("IDS_INVALIDSITETOLOGIN",
							objLanguage.getSfilename());
				}

			} else {
				returnStr = commonFunction.getMultilingualMessage(returnStr, objLanguage.getSfilename());
			}
			responseMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), returnStr);


			return new ResponseEntity<>(responseMap, HttpStatus.OK);

		}


//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
		public ResponseEntity<Object> getAuthenticationMFA(Map<String, Object> inputMap) throws Exception {

			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			int nfmaNeed = Enumeration.TransactionStatus.YES.gettransactionstatus();

			if (inputMap.containsKey("nmfaneed")) {
				nfmaNeed = (int) inputMap.get("nmfaneed");
			}
			final Map<String, Object> responseMap = new HashMap<>();
			final UserMFA objUserMfa = projectDAOSupport.getUserMFA(userInfo.getNusercode(), nfmaNeed,true);

			if (objUserMfa != null) {
				responseMap.put("newUserAuth", true);
				responseMap.put("authentication", objUserMfa);
				responseMap.put("showActiveStatus",true);
				responseMap.put("showEmailOTPModal", false);
			} else {
				responseMap.put("newUserAuth", false);
				responseMap.put("nmfatype", Enumeration.TransactionStatus.NA.gettransactionstatus());				
			}
			return new ResponseEntity<>(responseMap, HttpStatus.OK);

		}

		


//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
		public ResponseEntity<Object> updateActiveStatusMFA(Map<String, Object> inputMap) throws Exception {
			
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			int nfmaneed = Enumeration.TransactionStatus.YES.gettransactionstatus();
			int nactivestatus = Enumeration.TransactionStatus.YES.gettransactionstatus();
			Map<String, Object> responseMap = new HashMap<>();

			if (inputMap.containsKey("nmfaneed")) {
				nfmaneed = (int) inputMap.get("nmfaneed");
			}
			if (inputMap.containsKey("nactivestatus")) {
				nactivestatus = (int) inputMap.get("nactivestatus");
			}
			final UserMFA objUserMfa = projectDAOSupport.getUserMFA(userInfo.getNusercode(), nfmaneed,true);

			if (objUserMfa != null) {
				final String sQuery = "update usermfa set nactivestatus=" + nactivestatus
						+ "  where nusercode=" + userInfo.getNusercode() + " and  nsitecode="
						+ userInfo.getNmastersitecode() + " " + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(sQuery);
				responseMap.put("newUserAuth", true);
			} else {
				responseMap.put("newUserAuth", true);
				responseMap.put("nmfatype", Enumeration.TransactionStatus.NA.gettransactionstatus());
				responseMap.put("sreceivermailid", projectDAOSupport.getUserEmail(userInfo));

				responseMap.putAll(projectDAOSupport.getMFAType(userInfo));
			}

			return new ResponseEntity<>(responseMap, HttpStatus.OK);
			// return new ResponseEntity<>(responseMap, HttpStatus.OK);

		}
		
		
		
//		private String getEmailOTP(final UserInfo userInfo) throws Exception {
//			return (String) jdbcUtilityFunction.queryForObject(" select smfasettingvalue from mfasetting where nmfasettingcode="+Enumeration.MFASettings.EMAIL_OTP_TIME.getNmfasettingcode()+" "
//					+ " and nstatus=1", String.class, jdbcTemplate);
//		}
		
//		private String getEmailEditOption(final UserInfo userInfo) throws Exception {
//			return (String) jdbcUtilityFunction.queryForObject(" select smfasettingvalue from mfasetting where nmfasettingcode="+Enumeration.MFASettings.EMAIL_EDIT_OPTION.getNmfasettingcode()+" "
//					+ " and nstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" ", String.class, jdbcTemplate);
//		}
		
		
		
	
		
		private EmailHost getEmailHost(UserInfo objUserInfo) throws Exception {
			final String hostQry = "select eh.semail, eh.nemailhostcode, eh.shostname, eh.nportno from  emailhost eh where "
					+ "  eh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final EmailHost objEmailHost = (EmailHost) jdbcUtilityFunction.queryForObject(hostQry, EmailHost.class,
					jdbcTemplate);
			return objEmailHost;
}
}
