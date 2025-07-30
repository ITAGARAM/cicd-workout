package com.agaramtech.qualis.goodsin.model;

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
@Table(name = "goodsin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GoodsIn extends CustomizedResultsetRowMapper<GoodsIn> implements Serializable,RowMapper<GoodsIn>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ngoodsincode",nullable =false)
	private int ngoodsincode;
	@Column(name="sgoodsinid",nullable =false,length=100)
	private String sgoodsinid;
	@Column(name="nclientcatcode",nullable=false)
	private int nclientcatcode;
	@Column(name="nclientcode",nullable=false)
	private int nclientcode;
	@Column(name="nprojecttypecode",nullable=false)
	private int nprojecttypecode;
	@Column(name="nprojectmastercode",nullable=false)
	private int nprojectmastercode;
	@Column(name="ncouriercode",nullable=false)
	private int ncouriercode;
	@Column(name="nnoofpackages",nullable=false)
	private int nnoofpackages;
	@Column(name="noutofhours",nullable=false)
	private int noutofhours;	
	@Column(name="sconsignmentno",nullable =false,length=100)
	private String sconsignmentno;
	@Column(name="ssecurityrefno",nullable =false,length=100)
	private String ssecurityrefno;
	@Column(name="scomments",nullable =false,length=255)
	private String scomments;	
	@Column(name = "dgoodsindatetime")
	private Instant dgoodsindatetime;	
	@ColumnDefault("-1")
	@Column(name = "ntzgoodsindatetime", nullable = false)
	private int ntzgoodsindatetime =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	@Column(name = "noffsetdgoodsindatetime", nullable = false)
	private int noffsetdgoodsindatetime;
	@ColumnDefault("-1")
	@Column(name = "nsitecode",nullable=false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus",nullable=false)
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String sgoodsindatetime;
	@Transient
	private transient String susername;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient int nusercode;
	@Transient
	private transient int nuserrolecode;
	@Transient
	private transient int ntransactionstatus;
	@Transient
	private transient String sclientcatname;
	@Transient
	private transient String sclientname;
	@Transient
	private transient String sprojecttypename;
	@Transient
	private transient String sprojectname;
	@Transient
	private transient String scouriername;
	@Transient
	private transient String sgoodsinid1;
	@Transient
	private transient String sviewstatus;

	
	@Override
	public GoodsIn mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final GoodsIn objGoodsIn =new GoodsIn();
		objGoodsIn.setNgoodsincode(getInteger(arg0,"ngoodsincode",arg1));
		objGoodsIn.setSgoodsinid(StringEscapeUtils.unescapeJava(getString(arg0,"sgoodsinid",arg1)));
		objGoodsIn.setNclientcatcode(getInteger(arg0,"nclientcatcode",arg1));
		objGoodsIn.setNclientcode(getInteger(arg0,"nclientcode",arg1));
		objGoodsIn.setNprojecttypecode(getInteger(arg0,"nprojecttypecode",arg1));
		objGoodsIn.setNprojectmastercode(getInteger(arg0,"nprojectmastercode",arg1));
		objGoodsIn.setNcouriercode(getInteger(arg0,"ncouriercode",arg1));
		objGoodsIn.setNnoofpackages(getInteger(arg0,"nnoofpackages",arg1));
		objGoodsIn.setNoutofhours(getInteger(arg0,"noutofhours",arg1));
		objGoodsIn.setSconsignmentno(StringEscapeUtils.unescapeJava(getString(arg0,"sconsignmentno",arg1)));
		objGoodsIn.setSsecurityrefno(StringEscapeUtils.unescapeJava(getString(arg0,"ssecurityrefno",arg1)));
		objGoodsIn.setScomments(StringEscapeUtils.unescapeJava(getString(arg0,"scomments",arg1)));
		objGoodsIn.setDgoodsindatetime(getInstant(arg0,"dgoodsindatetime",arg1));
		objGoodsIn.setNtzgoodsindatetime(getInteger(arg0,"ntzgoodsindatetime",arg1));
		objGoodsIn.setNoffsetdgoodsindatetime(getInteger(arg0,"noffsetdgoodsindatetime",arg1));
		objGoodsIn.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objGoodsIn.setNstatus(getShort(arg0,"nstatus",arg1));
		objGoodsIn.setSgoodsindatetime(getString(arg0,"sgoodsindatetime",arg1));
		objGoodsIn.setSusername(getString(arg0,"susername",arg1));
		objGoodsIn.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objGoodsIn.setNusercode(getInteger(arg0,"nusercode",arg1));
		objGoodsIn.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objGoodsIn.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objGoodsIn.setSclientcatname(getString(arg0,"sclientcatname",arg1));
		objGoodsIn.setSclientname(getString(arg0,"sclientname",arg1));
		objGoodsIn.setSprojecttypename(getString(arg0,"sprojecttypename",arg1));
		objGoodsIn.setSprojectname(getString(arg0,"sprojectname",arg1));
		objGoodsIn.setScouriername(getString(arg0,"scouriername",arg1));
		objGoodsIn.setSgoodsinid1(getString(arg0,"sgoodsinid1",arg1));
		objGoodsIn.setSviewstatus(getString(arg0,"sviewstatus",arg1));


		return objGoodsIn;
	}
	
	
	

}
