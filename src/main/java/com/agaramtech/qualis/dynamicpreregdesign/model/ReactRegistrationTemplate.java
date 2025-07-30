package com.agaramtech.qualis.dynamicpreregdesign.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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

@Entity
@Table(name = "reactregistrationtemplate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class ReactRegistrationTemplate extends CustomizedResultsetRowMapper<ReactRegistrationTemplate>
		implements Serializable, RowMapper<ReactRegistrationTemplate> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nreactregtemplatecode")
	private int nreactregtemplatecode;
	
	@Column(name = "nsampletypecode")
	private short nsampletypecode;
	
	@Column(name = "ntransactionstatus")
	private short ntransactionstatus;
	
	@Column(name = "sregtemplatename")
	private String sregtemplatename;

	@Column(name = "ndefaulttemplatecode")
	private short ndefaulttemplatecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private List<Object> jsondata;
	
	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nsubsampletypecode")
	private short nsubsampletypecode;
	
	@Column(name = "stemplatetypesname")
	private String stemplatetypesname;

	@Transient
	private transient String jsonString;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient int ndesigntemplatemappingcode;
	@Transient
	private transient Map<String, Object> screendesign;
	@Transient
	private transient List<Object> slideoutdesign;
	@Transient
	private transient String sdefaulttemplatename;
	@Transient
	private transient String jsontext;
	@Transient
	private transient String ssampletypename;

	@Override
	public ReactRegistrationTemplate mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ReactRegistrationTemplate objReactRegistrationTemplate = new ReactRegistrationTemplate();
		
		objReactRegistrationTemplate.setNreactregtemplatecode(getInteger(arg0, "nreactregtemplatecode", arg1));
		objReactRegistrationTemplate.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		objReactRegistrationTemplate.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objReactRegistrationTemplate.setSregtemplatename(StringEscapeUtils.unescapeJava(getString(arg0, "sregtemplatename", arg1)));
		objReactRegistrationTemplate.setJsondata(getJSONArray(arg0, "jsondata", arg1));
		objReactRegistrationTemplate.setJsonString(getString(arg0, "jsonString", arg1));
		objReactRegistrationTemplate.setNstatus(getShort(arg0, "nstatus", arg1));
		objReactRegistrationTemplate.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objReactRegistrationTemplate
				.setNdesigntemplatemappingcode(getInteger(arg0, "ndesigntemplatemappingcode", arg1));
		objReactRegistrationTemplate.setScreendesign(getJsonObject(arg0, "screendesign", arg1));
		objReactRegistrationTemplate.setSlideoutdesign(getJSONArray(arg0, "slideoutdesign", arg1));
		objReactRegistrationTemplate.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objReactRegistrationTemplate.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objReactRegistrationTemplate.setSdefaulttemplatename(getString(arg0, "sdefaulttemplatename", arg1));
		objReactRegistrationTemplate.setNdefaulttemplatecode(getShort(arg0, "ndefaulttemplatecode", arg1));
		objReactRegistrationTemplate.setNsubsampletypecode(getShort(arg0, "nsubsampletypecode", arg1));
		objReactRegistrationTemplate.setStemplatetypesname(StringEscapeUtils.unescapeJava(getString(arg0, "stemplatetypesname", arg1)));
		objReactRegistrationTemplate.setJsontext(getString(arg0, "jsontext", arg1));
		objReactRegistrationTemplate.setSsampletypename(getString(arg0, "ssampletypename", arg1));

		return objReactRegistrationTemplate;
	}

	
}
