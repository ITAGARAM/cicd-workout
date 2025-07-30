package com.agaramtech.qualis.credential.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

//import com.agaramtech.lims.dao.support.AgaramResultSetMapper;
//import com.agaramtech.lims.dao.support.AgaramRowMapper;
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
@Table(name = "sitequalisforms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SiteQualisForms extends CustomizedResultsetRowMapper<SiteQualisForms> implements Serializable,RowMapper<SiteQualisForms> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsiteformscode")
	private short nsiteformscode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "nsorter", nullable = false)
	@ColumnDefault("0")
	private short nsorter;

	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String sdisplayname;
	@Transient
	private String label;
	@Transient
	private String value;
	@Transient
	private int nuserrolescreencode;
	@Transient
	private int nuserrolecode;

	public String getSdisplayname() {
		return sdisplayname;
	}

	public void setSdisplayname(String sdisplayname) {
		this.sdisplayname = sdisplayname;
	}

	public String getLabel() {
		return label;
	}

	public Instant getDmodifieddate() {
		return dmodifieddate;
	}

	public void setDmodifieddate(Instant dmodifieddate) {
		this.dmodifieddate = dmodifieddate;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public short getNsorter() {
		return nsorter;
	}

	public void setNsorter(short nsorter) {
		this.nsorter = nsorter;
	}

	public int getNuserrolescreencode() {
		return nuserrolescreencode;
	}

	public void setNuserrolescreencode(int nuserrolescreencode) {
		this.nuserrolescreencode = nuserrolescreencode;
	}

	public int getNuserrolecode() {
		return nuserrolecode;
	}

	public void setNuserrolecode(int nuserrolecode) {
		this.nuserrolecode = nuserrolecode;
	}

	public short getNsitecode() {
		return nsitecode;
	}

	public void setNsitecode(short nsitecode) {
		this.nsitecode = nsitecode;
	}

	public short getNformcode() {
		return nformcode;
	}

	public void setNformcode(short nformcode) {
		this.nformcode = nformcode;
	}

	public short getNstatus() {
		return nstatus;
	}

	public short getNsiteformscode() {
		return nsiteformscode;
	}

	public void setNsiteformscode(short nsiteformscode) {
		this.nsiteformscode = nsiteformscode;
	}

	public void setNstatus(short nstatus) {
		this.nstatus = nstatus;
	}

	@Override
	public SiteQualisForms mapRow(ResultSet arg0, int arg1) throws SQLException {
		SiteQualisForms objSiteQualisForms = new SiteQualisForms();
		objSiteQualisForms.setNsiteformscode(getShort(arg0,"nsiteformscode",arg1));
		objSiteQualisForms.setNformcode(getShort(arg0,"nformcode",arg1));
		objSiteQualisForms.setNuserrolescreencode(getInteger(arg0,"nuserrolescreencode",arg1));
		objSiteQualisForms.setNstatus(getShort(arg0,"nstatus",arg1));
		objSiteQualisForms.setNsorter(getShort(arg0,"nsorter",arg1));
		objSiteQualisForms.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSiteQualisForms.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objSiteQualisForms.setSdisplayname(StringEscapeUtils.unescapeJava(getString(arg0,"sdisplayname",arg1)));
		objSiteQualisForms.setLabel(StringEscapeUtils.unescapeJava(getString(arg0,"label",arg1)));
		objSiteQualisForms.setValue(StringEscapeUtils.unescapeJava(getString(arg0,"value",arg1)));
		objSiteQualisForms.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objSiteQualisForms;
	}

}
