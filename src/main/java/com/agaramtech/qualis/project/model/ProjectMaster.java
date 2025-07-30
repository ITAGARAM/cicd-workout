package com.agaramtech.qualis.project.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "projectmaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectMaster extends CustomizedResultsetRowMapper<ProjectMaster> implements Serializable,RowMapper<ProjectMaster> {
	
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@Column(name = "nprojectmastercode")
	private int nprojectmastercode;


	@Column(name = "nprojecttypecode")
	private int nprojecttypecode;

	@Column(name = "sprojecttitle", length = 255, nullable = false)
	private String sprojecttitle;


	@Column(name = "sprojectcode", length = 100, nullable = false)
	private String sprojectcode;

	@Column(name = "sprojectdescription", length = 255, nullable = false)
	private String sprojectdescription;


	@Column(name = "nuserrolecode")
	private int nuserrolecode;

	@Column(name = "nusercode")
	private int nusercode;

	@Column(name = "nclientcatcode")
	private int nclientcatcode;

	@Column(name = "nclientcode")
	private int nclientcode;

	@Column(name = "srfwid", length = 100, nullable = false)
	private String srfwid;

	@Column(name = "drfwdate")
	private Instant drfwdate;

	@Column(name = "dprojectstartdate")
	private Instant dprojectstartdate;


	@Column(name = "dexpectcompletiondate")
	private Instant dexpectcompletiondate;

	@Column(name = "nprojectduration")
	private short nprojectduration;

	@Column(name = "nperiodcode")
	private short nperiodcode;

	@Column(name="dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;

	@Column(name = "ssystemfilename", length = 100)
	private String ssystemfilename="";

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name="noffsetdrfwdate") 
	private int noffsetdrfwdate;

	@Column(name="ntzrfwdate" )  
	private short ntzrfwdate;

	@Column(name="noffsetdprojectstartdate") 
	private int noffsetdprojectstartdate;

	@Column(name="ntzprojectstartdate" )  
	private short ntzprojectstartdate;

	@Column(name="noffsetdexpectcompletiondate") 
	private int noffsetdexpectcompletiondate;

	@Column(name="ntzexpectcompletiondate" )  
	private short ntzexpectcompletiondate;

	@Column(name = "sprojectname", length = 100, nullable = false)
	private String sprojectname;

	@Transient
	private transient String susername;
	@Transient
	private transient String steammembername;
	@Transient
	private transient String speriodname;
	@Transient
	private transient String srfwdate;
	@Transient
	private transient String sprojectstartdate;
	@Transient
	private transient String sprojecttypename;
	@Transient
	private transient String sversionstatus;
	@Transient
	private transient String suserrolename;
	@Transient
	private transient String sprojectduration;
	@Transient
	private transient int npreregno;
	@Transient
	private transient int ncost;
	@Transient
	private transient String sarno;
	@Transient
	private transient String stestsynonym;
	@Transient
	private transient int ntransactiontestcode;
	@Transient
	private transient String ssamplearno;
	@Transient
	private transient String sregtypename;
	@Transient
	private transient String sregsubtypename;
	@Transient
	private transient int nquotationcode;
	@Transient
	private transient String squotationno;
	@Transient
	private transient String sexpectcompletiondate;
	@Transient
	private transient String sprojectcompletiondate;
	@Transient
	private transient String sprojectretiredate;
	@Transient
	private transient String sprojectclosuredate;
	@Transient
	private transient String sclientcatname;
	@Transient
	private transient String sclientname;
	@Transient
	private transient String stablename;
	@Transient
	private  transient int nsequenceno;
	@Transient
	private transient String sapprovedate;
	@Transient
	private  transient Instant dtransactiondate;
	@Transient
	private transient int ntransdatetimezonecode;
	@Transient
	private transient int noffsetdtransactiondate;
	@Transient
	private transient int noffsetprojectclosuredate;
	@Transient
	private transient short ntzprojectclosuredate;
	@Transient
	private transient int noffsetdprojectcompletiondate;
	@Transient
	private transient short ntzprojectcompletiondate;
	@Transient
	private transient int noffsetprojectretiredate;
	@Transient
	private transient short ntzprojectretiredate;
	@Transient
	private transient short nautoprojectcode;
	@Transient
	private transient String sclosureremarks;
	@Transient
	private transient String sretiredremarks;
	@Transient
	private transient String scompletionremarks;
	@Transient
	private transient int needjsontemplate;
	@Transient
	private transient String stzprojectcompletiondate;
	@Transient
	private transient String stzprojectretiredate;
	@Transient
	private transient String stzprojectclosuredate;
	@Transient
	private transient short ntransactionstatus;

	@Transient
	private transient String sloginid;
	@Transient
	private transient int nprojectmembercode;
	@Transient
	private transient String slinkname;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String sattachmenttype;
	@Transient
	private transient String screateddate;
	@Transient
	private transient String sfilesize;
	@Transient
	private transient String sproductname;

		
@Override
public ProjectMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
	
	ProjectMaster objProjectMaster = new ProjectMaster();
	
	objProjectMaster.setNprojectmastercode(getInteger(arg0,"nprojectmastercode",arg1));
	objProjectMaster.setNprojecttypecode(getInteger(arg0,"nprojecttypecode",arg1));
	objProjectMaster.setSprojecttitle(StringEscapeUtils.unescapeJava(getString(arg0,"sprojecttitle",arg1)));
	objProjectMaster.setSprojectcode(StringEscapeUtils.unescapeJava(getString(arg0,"sprojectcode",arg1)));
	objProjectMaster.setSprojectdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sprojectdescription",arg1)));
	objProjectMaster.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
	objProjectMaster.setDrfwdate(getInstant(arg0,"drfwdate",arg1));
	objProjectMaster.setDprojectstartdate(getInstant(arg0,"dprojectstartdate",arg1));
	objProjectMaster.setNprojectduration(getShort(arg0,"nprojectduration",arg1));
	objProjectMaster.setNperiodcode(getShort(arg0,"nperiodcode",arg1));
	objProjectMaster.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
	objProjectMaster.setNsitecode(getShort(arg0,"nsitecode",arg1));
	objProjectMaster.setNstatus(getShort(arg0,"nstatus",arg1));
	objProjectMaster.setSusername(getString(arg0,"susername",arg1));
	objProjectMaster.setSteammembername(getString(arg0,"steammembername",arg1));
	objProjectMaster.setSperiodname(getString(arg0,"speriodname",arg1));
	objProjectMaster.setSrfwdate(getString(arg0,"srfwdate",arg1));
	objProjectMaster.setSprojectstartdate(getString(arg0,"sprojectstartdate",arg1));
	objProjectMaster.setNusercode(getInteger(arg0,"nusercode",arg1));
	objProjectMaster.setSprojecttypename(getString(arg0,"sprojecttypename",arg1));
	objProjectMaster.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
	objProjectMaster.setSversionstatus(getString(arg0,"sversionstatus",arg1));
	objProjectMaster.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));
	objProjectMaster.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0,"sfilename",arg1)));
	objProjectMaster.setSclosureremarks(getString(arg0,"sclosureremarks",arg1));
	objProjectMaster.setSuserrolename(getString(arg0,"suserrolename",arg1));
	objProjectMaster.setSprojectduration(getString(arg0,"sprojectduration",arg1));
	objProjectMaster.setNoffsetdrfwdate(getInteger(arg0,"noffsetdrfwdate",arg1));
	objProjectMaster.setNoffsetdprojectstartdate(getInteger(arg0,"noffsetdprojectstartdate",arg1));
	objProjectMaster.setNtzrfwdate(getShort(arg0,"ntzrfwdate",arg1));
	objProjectMaster.setNtzprojectstartdate(getShort(arg0,"ntzprojectstartdate",arg1));

	objProjectMaster.setNpreregno(getInteger(arg0,"npreregno",arg1));
	objProjectMaster.setNcost(getInteger(arg0,"ncost",arg1));
	objProjectMaster.setSarno(getString(arg0,"sarno",arg1));
	objProjectMaster.setStestsynonym(getString(arg0,"stestsynonym",arg1));
	objProjectMaster.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
	objProjectMaster.setSsamplearno(getString(arg0,"ssamplearno",arg1));
	objProjectMaster.setSregtypename(getString(arg0,"sregtypename",arg1));
	objProjectMaster.setSregsubtypename(getString(arg0,"sregsubtypename",arg1));
	objProjectMaster.setSprojectname(StringEscapeUtils.unescapeJava(getString(arg0,"sprojectname",arg1)));
	
	objProjectMaster.setSquotationno(getString(arg0,"squotationno",arg1));
	objProjectMaster.setNquotationcode(getInteger(arg0,"nquotationcode",arg1));
	objProjectMaster.setSprojectclosuredate(getString(arg0,"sprojectclosuredate",arg1));
	objProjectMaster.setSexpectcompletiondate(getString(arg0,"sexpectcompletiondate",arg1));
	objProjectMaster.setSprojectretiredate(getString(arg0,"sprojectretiredate",arg1));
	objProjectMaster.setSprojectcompletiondate(getString(arg0,"sprojectcompletiondate",arg1));
	objProjectMaster.setDexpectcompletiondate(getInstant(arg0,"dexpectcompletiondate",arg1));
	objProjectMaster.setNoffsetdexpectcompletiondate(getInteger(arg0,"noffsetdexpectcompletiondate",arg1));
	objProjectMaster.setNoffsetdprojectcompletiondate(getInteger(arg0,"noffsetdprojectcompletiondate",arg1));
	objProjectMaster.setNoffsetprojectretiredate(getInteger(arg0,"noffsetprojectretiredate",arg1));
	objProjectMaster.setNoffsetprojectclosuredate(getInteger(arg0,"noffsetprojectclosuredate",arg1));
	objProjectMaster.setNtzexpectcompletiondate(getShort(arg0,"ntzexpectcompletiondate",arg1));
	objProjectMaster.setNtzprojectcompletiondate(getShort(arg0,"ntzprojectcompletiondate",arg1));
	objProjectMaster.setNtzprojectretiredate(getShort(arg0,"tzprojectretiredate",arg1));
	objProjectMaster.setNtzprojectclosuredate(getShort(arg0,"ntzprojectclosuredate",arg1));
	objProjectMaster.setNclientcatcode(getInteger(arg0,"nclientcatcode",arg1));
	objProjectMaster.setNclientcode(getInteger(arg0,"nclientcode",arg1));
	objProjectMaster.setSretiredremarks(getString(arg0,"sretiredremarks",arg1));
	objProjectMaster.setScompletionremarks(getString(arg0,"scompletionremarks",arg1));
	objProjectMaster.setSrfwid(StringEscapeUtils.unescapeJava(getString(arg0,"srfwid",arg1)));
	objProjectMaster.setSclientcatname(getString(arg0,"sclientcatname",arg1));
	objProjectMaster.setSclientname(getString(arg0,"sclientname",arg1));
	objProjectMaster.setStablename(getString(arg0,"stablename",arg1));
	objProjectMaster.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
	objProjectMaster.setSapprovedate(getString(arg0,"sapprovedate",arg1));
	objProjectMaster.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
	objProjectMaster.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
	objProjectMaster.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
	objProjectMaster.setNautoprojectcode(getShort(arg0,"nautoprojectcode",arg1));

	objProjectMaster.setNeedjsontemplate(getInteger(arg0,"needjsontemplate",arg1));
	objProjectMaster.setStzprojectcompletiondate(getString(arg0,"stzprojectcompletiondate",arg1));
	objProjectMaster.setStzprojectretiredate(getString(arg0,"stzprojectretiredate",arg1));
	
	
	objProjectMaster.setNprojectmembercode(getInteger(arg0,"nprojectmembercode",arg1));
	objProjectMaster.setSloginid(getString(arg0,"sloginid",arg1));
	objProjectMaster.setSproductname(getString(arg0,"sproductname",arg1));
	objProjectMaster.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));


	return objProjectMaster;
	
}


}