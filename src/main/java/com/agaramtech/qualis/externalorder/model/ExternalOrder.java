package com.agaramtech.qualis.externalorder.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'externalorder' table of the Database.
 */
@Entity
@Table(name = "externalorder")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExternalOrder extends CustomizedResultsetRowMapper<ExternalOrder> implements Serializable, RowMapper<ExternalOrder> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nexternalordercode")
	private int nexternalordercode;
	
	@Column(name = "nordertypecode")
	@ColumnDefault("-1")
	private short nordertypecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nproductcatcode")
	@ColumnDefault("-1")
	private int nproductcatcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nproductcode")
	@ColumnDefault("-1")
	private int nproductcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ngendercode")
	@ColumnDefault("-1")
	private short ngendercode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ninstitutioncode")
	@ColumnDefault("-1")
	private int ninstitutioncode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ninstitutionsitecode")
	@ColumnDefault("-1")
	private int ninstitutionsitecode;
	
	@Column(name = "ndiagnosticcasecode")
	@ColumnDefault("-1")
	private short ndiagnosticcasecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nallottedspeccode")
	@ColumnDefault("-1")
	private int nallottedspeccode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ndefaultstatus", nullable = false)
	@ColumnDefault("3")
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@Column(name = "nusercode")
	private int nusercode;
	
	@Column(name = "spatientid", length = 50, columnDefinition = "nvarchar")
	private String spatientid;
	
	@Column(name = "ssubmittercode", length = 50, columnDefinition = "nvarchar")
	private String ssubmittercode;
	
	@Column(name = "sexternalorderid", length = 50, columnDefinition = "nvarchar")
	private String sexternalorderid;
	
	@Column(name = "sorderseqno", length = 50, columnDefinition = "nvarchar")
	private String sorderseqno;
	
	@Column(name = "ntransactionstatus")
	private short ntransactionstatus;
	
	@Column(name = "nexternalordertypecode")
	@ColumnDefault("-1")
	private short nexternalordertypecode;
	
	@Column(name = "nlabcode")
	@ColumnDefault("-1")
	private int nlabcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nplantcode")
	@ColumnDefault("-1")
	private int nplantcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nparentsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nparentsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	


	@Transient
	private transient String ssubmittername;
	
	@Transient
	private transient String sdiagnosticcasename;
	
	@Transient
	private transient String sinstitutiondeptname;
	
	@Transient
	private transient String sinstitutiondeptcode;

	@Transient
	private transient String sfathername;
	
	@Transient
	private transient String ddob;
	
	@Transient
	private transient String sage;
	
	@Transient
	private transient String scountryname;
	
	@Transient
	private transient String scityname;
	
	@Transient
	private transient String sdistrictname;
	
	@Transient
	private transient String spostalcode;
	
	@Transient
	private transient String sregionname;

	@Transient
	private transient String sphoneno;
	
	@Transient
	private transient String smobileno;
	
	@Transient
	private transient String semail;
	
	@Transient
	private transient String sexternalid;
	
	@Transient
	private transient String spassportno;
	
	@Transient
	private transient String shouseno;

	@Transient
	private transient String sproductcatname;
	
	@Transient
	private transient String sproductname;
	
	@Transient
	private transient String spatientname;
	
	@Transient
	private transient String sinstitutionname;
	
	@Transient
	private transient String sinstitutioncode;
	
	@Transient
	private transient String scontactname;
	
	@Transient
	private transient String sgendername;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String sdob;
	
	@Transient
	private transient int nexternalordersamplecode;
	
	@Transient
	private transient String stubename;
	
	@Transient
	private transient String scontainertype;
	
	@Transient
	private transient String sunitname;

	@Transient
	private transient String stestsynonym;
	
	@Transient
	private transient String spriorityname;
	
	@Transient
	private transient String ssampletype;
	 
	@Transient
	private transient int ncomponentcode;
	
	@Transient
	private transient String sinstitutionsitename;

	@Transient
	private transient String sfirstname;
	
	@Transient
	private transient String slastname;
	
	@Transient
	private transient String ssubmitterid;
	
	@Transient
	private transient int ninstitutioncatcode;
	 
	@Transient
	private transient String sinstitutioncatname;
	
	@Transient
	private transient String spatientfirstname;
	
	@Transient
	private transient String spatientlastname;
	
	@Transient
	private transient String spatientfathername;
	
	@Transient
	private transient String ssubmitterfirstname;
	
	@Transient
	private transient String ssubmitterlastname;
	
	@Transient
	private transient  String ssubmitteremail;

	@Transient
	private transient String spermanentadd;
	
	@Transient
	private transient String scurrentadd;
	
	@Transient
	private transient String sinsdistrictcity;
	
	@Transient
	private transient String submittertelephone;
	
	@Transient
	private transient int ndistrictcode;
	
	@Transient
	private transient String sexternalsampleid;
	// private transient Date dcollectiondate;
	
	@Transient
	private transient String dcollectiondate;
	
	@Transient
	private transient String sexternalordertypename;
	
	@Transient
	private transient String ssitecode;

	@Transient
	private transient String spatientrefid;
	
	@Transient
	private transient String externalordertypelist;

	@Transient
	private transient List<ExternalOrderSample> externalordersample;
	
	@Transient
	private transient List<ExternalOrderTest> externalordertest;
	
	@Transient
	private transient String  sordertestcomment;
	
	@Transient
	private transient Map<String,Object> extras;
	
	@Transient
	private transient int npreregno;
	 
	@Transient
	private transient String sinstitutiondepartment;
	
	@Transient
	private transient String sinstitutionregion;
	
	@Transient
	private transient String sinstitutioncountry;
	
	@Transient
	private transient String ssubmittermobileno;	
	

	@Override
	public ExternalOrder mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ExternalOrder objExternalOrder = new ExternalOrder();

		objExternalOrder.setNexternalordercode(getInteger(arg0, "nexternalordercode", arg1));
		objExternalOrder.setNordertypecode(getShort(arg0, "nordertypecode", arg1));
		objExternalOrder.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objExternalOrder.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objExternalOrder.setNgendercode(getShort(arg0, "ngendercode", arg1));
		objExternalOrder.setNinstitutioncode(getInteger(arg0, "ninstitutioncode", arg1));
		objExternalOrder.setNinstitutionsitecode(getInteger(arg0, "ninstitutionsitecode", arg1));
		objExternalOrder.setNdiagnosticcasecode(getShort(arg0, "ndiagnosticcasecode", arg1));
		objExternalOrder.setSpatientid(StringEscapeUtils.unescapeJava(getString(arg0, "spatientid", arg1)));
		objExternalOrder.setSsubmittercode(StringEscapeUtils.unescapeJava(getString(arg0, "ssubmittercode", arg1)));
		objExternalOrder.setSexternalorderid(StringEscapeUtils.unescapeJava(getString(arg0, "sexternalorderid", arg1)));
		objExternalOrder.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objExternalOrder.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objExternalOrder.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objExternalOrder.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objExternalOrder.setNparentsitecode(getShort(arg0, "nparentsitecode", arg1));
		objExternalOrder.setNstatus(getShort(arg0, "nstatus", arg1));

		objExternalOrder.setSsubmittername(getString(arg0, "ssubmittername", arg1));
		objExternalOrder.setSdiagnosticcasename(getString(arg0, "sdiagnosticcasename", arg1));

		objExternalOrder.setSinstitutiondeptcode(getString(arg0, "sinstitutiondeptcode", arg1));
		objExternalOrder.setSinstitutiondeptname(getString(arg0, "sinstitutiondeptname", arg1));
		objExternalOrder.setSinstitutionsitename(getString(arg0, "sinstitutionsitename", arg1));

		objExternalOrder.setSinstitutionname(getString(arg0, "sinstitutionname", arg1));
		objExternalOrder.setSpatientname(getString(arg0, "spatientname", arg1));
		objExternalOrder.setSfathername(getString(arg0, "sfathername", arg1));
		objExternalOrder.setDdob(getString(arg0, "ddob", arg1));
		objExternalOrder.setSage(getString(arg0, "sage", arg1));
		objExternalOrder.setScountryname(getString(arg0, "scountryname", arg1));

		objExternalOrder.setSpatientfirstname(getString(arg0, "spatientfirstname", arg1));
		objExternalOrder.setSpatientlastname(getString(arg0, "spatientlastname", arg1));
		objExternalOrder.setSpatientfathername(getString(arg0, "spatientfathername", arg1));

		objExternalOrder.setScityname(getString(arg0, "scityname", arg1));
		objExternalOrder.setSdistrictname(getString(arg0, "sdistrictname", arg1));

		objExternalOrder.setSpostalcode(getString(arg0, "spostalcode", arg1));
		objExternalOrder.setSphoneno(getString(arg0, "sphoneno", arg1));
		objExternalOrder.setSmobileno(getString(arg0, "smobileno", arg1));
		objExternalOrder.setSemail(getString(arg0, "semail", arg1));
		objExternalOrder.setSexternalid(getString(arg0, "sexternalid", arg1));
		objExternalOrder.setSregionname(getString(arg0, "sregionname", arg1));
		objExternalOrder.setShouseno(getString(arg0, "shouseno", arg1));

		objExternalOrder.setSpassportno(getString(arg0, "spassportno", arg1));
		objExternalOrder.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		objExternalOrder.setSproductname(getString(arg0, "sproductname", arg1));
		objExternalOrder.setSinstitutionname(getString(arg0, "sinstitutionname", arg1));
		objExternalOrder.setSinstitutioncode(getString(arg0, "sinstitutioncode", arg1));
		objExternalOrder.setScontactname(getString(arg0, "scontactname", arg1));
		objExternalOrder.setSgendername(getString(arg0, "sgendername", arg1));
		objExternalOrder.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objExternalOrder.setSdob(getString(arg0, "sdob", arg1));
		objExternalOrder.setNexternalordersamplecode(getInteger(arg0, "nexternalordersamplecode", arg1));
		objExternalOrder.setStubename(getString(arg0, "stubename", arg1));
		objExternalOrder.setScontainertype(getString(arg0, "scontainertype", arg1));
		objExternalOrder.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objExternalOrder.setSpriorityname(getString(arg0, "spriorityname", arg1));
		objExternalOrder.setSsampletype(getString(arg0, "ssampletype", arg1));
		objExternalOrder.setNcomponentcode(getShort(arg0, "ncomponentcode", arg1));
		objExternalOrder.setSinstitutionsitename(getString(arg0, "sinstitutionsitename", arg1));
		objExternalOrder.setStestsynonym(getString(arg0, "stestsynonym", arg1));

		objExternalOrder.setSfirstname(getString(arg0, "sfirstname", arg1));
		objExternalOrder.setSlastname(getString(arg0, "slastname", arg1));
		objExternalOrder.setSsubmitterid(getString(arg0, "ssubmitterid", arg1));
		objExternalOrder.setSinstitutiondeptname(getString(arg0, "sinstitutiondeptname", arg1));
		objExternalOrder.setSinstitutiondeptcode(getString(arg0, "sinstitutiondeptcode", arg1));
		objExternalOrder.setSsubmittername(getString(arg0, "ssubmittername", arg1));
		objExternalOrder.setSsubmitterfirstname(getString(arg0, "ssubmitterfirstname", arg1));
		objExternalOrder.setSsubmitterlastname(getString(arg0, "ssubmitterlastname", arg1));
		objExternalOrder.setSsubmitteremail(getString(arg0, "ssubmitteremail", arg1));
		objExternalOrder.setSunitname(getString(arg0, "sunitname", arg1));
	
		objExternalOrder.setNinstitutioncatcode(getInteger(arg0, "ninstitutioncatcode", arg1));
		objExternalOrder.setSinstitutioncatname(getString(arg0, "sinstitutioncatname", arg1));
		objExternalOrder.setSorderseqno(StringEscapeUtils.unescapeJava(getString(arg0, "sorderseqno", arg1)));
		objExternalOrder.setNallottedspeccode(getInteger(arg0, "nallottedspeccode", arg1));
		objExternalOrder.setNusercode(getInteger(arg0, "nusercode", arg1));

		objExternalOrder.setSpermanentadd(getString(arg0, "spermanentadd", arg1));
		objExternalOrder.setScurrentadd(getString(arg0, "scurrentadd", arg1));
		objExternalOrder.setSinsdistrictcity(getString(arg0, "sinsdistrictcity", arg1));
		objExternalOrder.setSubmittertelephone(getString(arg0, "submittertelephone", arg1));
		objExternalOrder.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objExternalOrder.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objExternalOrder.setSexternalsampleid(getString(arg0, "sexternalsampleid", arg1));
		objExternalOrder.setDcollectiondate(getString(arg0, "dcollectiondate", arg1));
		objExternalOrder.setNexternalordertypecode(getShort(arg0, "nexternalordertypecode", arg1));
		objExternalOrder.setSsitecode(getString(arg0, "sstiecode", arg1));
		objExternalOrder.setExternalordertypelist(getString(arg0, "externalordertypelist", arg1));
		objExternalOrder.setSexternalordertypename(getString(arg0, "sexternalordertypename", arg1));
		objExternalOrder.setNlabcode(getInteger(arg0, "nlabcode", arg1));
		objExternalOrder.setNplantcode(getInteger(arg0, "nplantcode", arg1));
		objExternalOrder.setSpatientrefid(getString(arg0, "spatientrefid", arg1));
		objExternalOrder.setSordertestcomment(getString(arg0, "sordertestcomment", arg1));
		objExternalOrder.setExtras(getJsonObject(arg0, "extras", arg1));
		objExternalOrder.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objExternalOrder.setSinstitutiondepartment(getString(arg0,"sinstitutiondepartment",arg1));
		objExternalOrder.setSinstitutionregion(getString(arg0,"sinstitutionregion",arg1));
		objExternalOrder.setSinstitutioncountry(getString(arg0,"sinstitutioncountry",arg1));
		objExternalOrder.setSsubmittermobileno(getString(arg0,"ssubmittermobileno",arg1));
		
		return objExternalOrder;
	}
//
//	@Override
//	public String toString() {
//		return "ExternalOrder [nexternalordercode=" + nexternalordercode + ", nordertypecode=" + nordertypecode
//				+ ", nexternalordertypecode=" + nexternalordertypecode + ", nproductcatcode=" + nproductcatcode
//				+ ", nproductcode=" + nproductcode + ", ngendercode=" + ngendercode + ", ninstitutioncode="
//				+ ninstitutioncode + ", ninstitutionsitecode=" + ninstitutionsitecode + ", ndiagnosticcasecode="
//				+ ndiagnosticcasecode + ", nallottedspeccode=" + nallottedspeccode + ", ndefaultstatus="
//				+ ndefaultstatus + ", nusercode=" + nusercode + ", spatientid=" + spatientid + ", ssubmittercode="
//				+ ssubmittercode + ", sexternalorderid=" + sexternalorderid + ", sorderseqno=" + sorderseqno
//				+ ", ntransactionstatus=" + ntransactionstatus + ", jsondata=" + jsondata + ", dmodifieddate="
//				+ dmodifieddate + ", nsitecode=" + nsitecode + ", nparentsitecode=" + nparentsitecode + ", nstatus="
//				+ nstatus + ", ssubmittername=" + ssubmittername + ", sdiagnosticcasename=" + sdiagnosticcasename
//				+ ", sinstitutiondeptname=" + sinstitutiondeptname + ", sinstitutiondeptcode=" + sinstitutiondeptcode
//				+ ", sfathername=" + sfathername + ", ddob=" + ddob + ", sage=" + sage + ", scountryname="
//				+ scountryname + ", scityname=" + scityname + ", sdistrictname=" + sdistrictname + ", spostalcode="
//				+ spostalcode + ", sregionname=" + sregionname + ", sphoneno=" + sphoneno + ", smobileno=" + smobileno
//				+ ", semail=" + semail + ", sexternalid=" + sexternalid + ", spassportno=" + spassportno + ", shouseno="
//				+ shouseno + ", sproductcatname=" + sproductcatname + ", sproductname=" + sproductname
//				+ ", spatientname=" + spatientname + ", sinstitutionname=" + sinstitutionname + ", sinstitutioncode="
//				+ sinstitutioncode + ", scontactname=" + scontactname + ", sgendername=" + sgendername
//				+ ", stransdisplaystatus=" + stransdisplaystatus + ", sdob=" + sdob + ", nexternalordersamplecode="
//				+ nexternalordersamplecode + ", stubename=" + stubename + ", scontainertype=" + scontainertype
//				+ ", sunitname=" + sunitname + ", stestsynonym=" + stestsynonym + ", spriorityname=" + spriorityname
//				+ ", ssampletype=" + ssampletype + ", ncomponentcode=" + ncomponentcode + ", sinstitutionsitename="
//				+ sinstitutionsitename + ", sfirstname=" + sfirstname + ", slastname=" + slastname + ", ssubmitterid="
//				+ ssubmitterid + ", ninstitutioncatcode=" + ninstitutioncatcode + ", sinstitutioncatname="
//				+ sinstitutioncatname + ", spatientfirstname=" + spatientfirstname + ", spatientlastname="
//				+ spatientlastname + ", spatientfathername=" + spatientfathername + ", ssubmitterfirstname="
//				+ ssubmitterfirstname + ", ssubmitterlastname=" + ssubmitterlastname + ", ssubmitteremail="
//				+ ssubmitteremail + ", spermanentadd=" + spermanentadd + ", scurrentadd=" + scurrentadd
//				+ ", sinsdistrictcity=" + sinsdistrictcity + ", submittertelephone=" + submittertelephone
//				+ ", ndistrictcode=" + ndistrictcode + ", sexternalsampleid=" + sexternalsampleid + ", dcollectiondate="
//				+ dcollectiondate + ", externalordersample=" + externalordersample + ", externalordertest="
//				+ externalordertest + ", availableColumns=" + availableColumns + ", getExternalordersample()="
//				+ getExternalordersample() + ", getExternalordertest()=" + getExternalordertest()
//				+ ", getNexternalordercode()=" + getNexternalordercode() + ", getNordertypecode()="
//				+ getNordertypecode() + ", getNexternalordertypecode=" + getNexternalordertypecode()
//				+ ", getNproductcatcode()=" + getNproductcatcode() + ", getNproductcode()=" + getNproductcode()
//				+ ", getNgendercode()=" + getNgendercode() + ", getNinstitutioncode()=" + getNinstitutioncode()
//				+ ", getNinstitutionsitecode()=" + getNinstitutionsitecode() + ", getNdiagnosticcasecode()="
//				+ getNdiagnosticcasecode() + ", getNallottedspeccode()=" + getNallottedspeccode()
//				+ ", getNdefaultstatus()=" + getNdefaultstatus() + ", getNusercode()=" + getNusercode()
//				+ ", getSpatientid()=" + getSpatientid() + ", getSsubmittercode()=" + getSsubmittercode()
//				+ ", getSexternalorderid()=" + getSexternalorderid() + ", getSorderseqno()=" + getSorderseqno()
//				+ ", getNtransactionstatus()=" + getNtransactionstatus() + ", getJsondata()=" + getJsondata()
//				+ ", getDmodifieddate()=" + getDmodifieddate() + ", getNsitecode()=" + getNsitecode()
//				+ ", getNparentsitecode()=" + getNparentsitecode() + ", getNstatus()=" + getNstatus()
//				+ ", getSsubmittername()=" + getSsubmittername() + ", getSdiagnosticcasename()="
//				+ getSdiagnosticcasename() + ", getSinstitutiondeptname()=" + getSinstitutiondeptname()
//				+ ", getSinstitutiondeptcode()=" + getSinstitutiondeptcode() + ", getSfathername()=" + getSfathername()
//				+ ", getDdob()=" + getDdob() + ", getSage()=" + getSage() + ", getScountryname()=" + getScountryname()
//				+ ", getScityname()=" + getScityname() + ", getSdistrictname()=" + getSdistrictname()
//				+ ", getSpostalcode()=" + getSpostalcode() + ", getSregionname()=" + getSregionname()
//				+ ", getSphoneno()=" + getSphoneno() + ", getSmobileno()=" + getSmobileno() + ", getSemail()="
//				+ getSemail() + ", getSexternalid()=" + getSexternalid() + ", getSpassportno()=" + getSpassportno()
//				+ ", getShouseno()=" + getShouseno() + ", getSproductcatname()=" + getSproductcatname()
//				+ ", getSproductname()=" + getSproductname() + ", getSpatientname()=" + getSpatientname()
//				+ ", getSinstitutionname()=" + getSinstitutionname() + ", getSinstitutioncode()="
//				+ getSinstitutioncode() + ", getScontactname()=" + getScontactname() + ", getSgendername()="
//				+ getSgendername() + ", getStransdisplaystatus()=" + getStransdisplaystatus() + ", getSdob()="
//				+ getSdob() + ", getNexternalordersamplecode()=" + getNexternalordersamplecode() + ", getStubename()="
//				+ getStubename() + ", getScontainertype()=" + getScontainertype() + ", getSunitname()=" + getSunitname()
//				+ ", getStestsynonym()=" + getStestsynonym() + ", getSpriorityname()=" + getSpriorityname()
//				+ ", getSsampletype()=" + getSsampletype() + ", getNcomponentcode()=" + getNcomponentcode()
//				+ ", getSinstitutionsitename()=" + getSinstitutionsitename() + ", getSfirstname()=" + getSfirstname()
//				+ ", getSlastname()=" + getSlastname() + ", getSsubmitterid()=" + getSsubmitterid()
//				+ ", getNinstitutioncatcode()=" + getNinstitutioncatcode() + ", getSinstitutioncatname()="
//				+ getSinstitutioncatname() + ", getSpatientfirstname()=" + getSpatientfirstname()
//				+ ", getSpatientlastname()=" + getSpatientlastname() + ", getSpatientfathername()="
//				+ getSpatientfathername() + ", getSsubmitterfirstname()=" + getSsubmitterfirstname()
//				+ ", getSsubmitterlastname()=" + getSsubmitterlastname() + ", getSsubmitteremail()="
//				+ getSsubmitteremail() + ", getSpermanentadd()=" + getSpermanentadd() + ", getScurrentadd()="
//				+ getScurrentadd() + ", getSinsdistrictcity()=" + getSinsdistrictcity() + ", getSubmittertelephone()="
//				+ getSubmittertelephone() + ", getNdistrictcode()=" + getNdistrictcode() + ", getSexternalsampleid()="
//				+ getSexternalsampleid() + ", getDcollectiondate()=" + getDcollectiondate() + ",getSinstitutiondepartment()=" + getSinstitutiondepartment() + ","
//				+ "getSinstitutionregion()=" + getSinstitutionregion() + ",getSinstitutioncountry()=" + getSinstitutioncountry() + ","
//				+ "getSsubmittermobileno()=" + getSsubmittermobileno() + ", getClass()="+ getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
//	}

}