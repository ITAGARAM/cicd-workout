package com.agaramtech.qualis.global;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This pojo class is used to hold details related the loggedin user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
	private int nusercode;
	private int nuserrole;
	private int ndeputyusercode;
	private int ndeputyuserrole;
	private short nmodulecode;
	private short nformcode;
	private int nreasoncode;
	private String slanguagetypecode;
	private String sreportlanguagecode;
	private String sConnectionString;	
	private short nsitecode;
	private short ntranssitecode;
	private short nmastersitecode;
	private String sreason;
	private String susername;
	private String suserrolename;
	private String slanguagefilename;
	private int nusersitecode;
	private String sloginid;
	private String sdeptname;
	private int ndeptcode;
	private String sdatetimeformat;
	private int ntimezonecode;
	private String stimezoneid;
	private String ssitedatetime;
	private String ssitereportdatetime;
	private String ssitereportdate;
	private String ssitedate;
	private short nlogintypecode;
	private String sfirstname;
	private String slastname;
	private String ssessionid;
	private String shostip;
	private String spassword;
	private String sgmtoffset;
	private String sdeputyid;
	private String ssitename;
	private String sformname;
	private String smodulename;
	private int istimezoneshow;
	private String sreportingtoolfilename;
	private String spredefinedreason;
	private int isutcenabled;
	private int nsiteadditionalinfo;
	private int nissyncserver;
	private int nisstandaloneserver;
	private String slanguagename;
	private String ssitecode;
	private String sdeputyusername;
	private String sdeputyuserrolename;
	private List<Object> activelanguagelist;
	private String spgdatetimeformat;
	private String spgsitedatetime;
	private String spgsitereportdatetime;	
	// Added by gowtham on 23 june, ALPD-6035 - JWT
	private int nexpirytime;
	
	public UserInfo(UserInfo userInfo) {
		super();
		this.nusercode = userInfo.nusercode;
		this.nuserrole = userInfo.nuserrole;
		this.ndeputyusercode = userInfo.ndeputyusercode;
		this.ndeputyuserrole = userInfo.ndeputyuserrole;
		this.nmodulecode = userInfo.nmodulecode;
		this.nformcode = userInfo.nformcode;
		this.nreasoncode = userInfo.nreasoncode;
		this.slanguagetypecode = userInfo.slanguagetypecode;
		this.nsitecode = userInfo.nsitecode;
		this.ntranssitecode = userInfo.ntranssitecode;
		this.nmastersitecode = userInfo.nmastersitecode;
		this.sreason = userInfo.sreason;
		this.susername = userInfo.susername;
		this.suserrolename = userInfo.suserrolename;
		this.slanguagefilename = userInfo.slanguagefilename;
		this.nusersitecode = userInfo.nusersitecode;
		this.sloginid = userInfo.sloginid;
		this.sdeptname = userInfo.sdeptname;
		this.ndeptcode = userInfo.ndeptcode;
		this.sdatetimeformat = userInfo.sdatetimeformat;
		this.ntimezonecode = userInfo.ntimezonecode;
		this.stimezoneid = userInfo.stimezoneid;
		this.ssitedatetime = userInfo.ssitedatetime;
		this.ssitereportdatetime = userInfo.ssitereportdatetime;
		this.ssitereportdate = userInfo.ssitereportdate;
		this.ssitedate = userInfo.ssitedate;
		this.nlogintypecode = userInfo.nlogintypecode;
		this.sfirstname = userInfo.sfirstname;
		this.slastname = userInfo.slastname;
		this.ssessionid = userInfo.ssessionid;
		this.shostip = userInfo.shostip;
		this.spassword = userInfo.spassword;
		this.sgmtoffset = userInfo.sgmtoffset;
		this.sdeputyid = userInfo.sdeputyid;
		this.sformname = userInfo.sformname;
		this.smodulename = userInfo.smodulename;
		this.sdeputyusername = userInfo.sdeputyusername;
		this.sdeputyuserrolename = userInfo.sdeputyuserrolename;
		this.istimezoneshow = userInfo.istimezoneshow;
		this.spgdatetimeformat = userInfo.spgdatetimeformat;
		this.spgsitedatetime = userInfo.spgsitedatetime;
		this.spgsitereportdatetime = userInfo.spgsitereportdatetime;
		this.sreportingtoolfilename = userInfo.sreportingtoolfilename;
		this.spredefinedreason = userInfo.spredefinedreason;
		this.isutcenabled=userInfo.isutcenabled;
		this.nsiteadditionalinfo=userInfo.nsiteadditionalinfo;
		this.nissyncserver=userInfo.nissyncserver;
		this.nisstandaloneserver=userInfo.nisstandaloneserver;
		this.slanguagename=userInfo.slanguagename;
		this.ssitecode=userInfo.getSsitecode();
		this.activelanguagelist=userInfo.activelanguagelist;
		this.nexpirytime=userInfo.nexpirytime;
	}
}
