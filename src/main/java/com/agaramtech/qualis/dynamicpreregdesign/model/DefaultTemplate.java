package com.agaramtech.qualis.dynamicpreregdesign.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

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
@Data
@Table(name = "defaulttemplate")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DefaultTemplate  extends CustomizedResultsetRowMapper<DefaultTemplate> implements Serializable,RowMapper<DefaultTemplate> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndefaulttemplatecode")
	private short ndefaulttemplatecode;

	@Column(name = "nsampletypecode")
	private short nsampletypecode;

	@Lob
	@Column(name = "jtemplatename", columnDefinition = "jsonb")
	private Map<String, Object> jtemplatename;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private List<Object> jsondata;

	@Column(name = "ndefaultstatus")
	private short ndefaultstatus;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	@Column(name = "nsubsampletypecode")
	private short nsubsampletypecode;

	@Transient
	private transient String jsonString;
	@Transient
	private transient String sdefaulttemplatename;
	@Transient
	private transient Map<String, Object> screendesign;
	@Transient
	private transient List<Object> slideoutdesign;

	@Override
	public DefaultTemplate mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final DefaultTemplate objReactRegistrationTemplate = new DefaultTemplate();
		
		objReactRegistrationTemplate.setNdefaulttemplatecode(getShort(arg0, "ndefaulttemplatecode", arg1));
		objReactRegistrationTemplate.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		objReactRegistrationTemplate.setJtemplatename(unescapeString(getJsonObject(arg0, "jtemplatename", arg1)));
		objReactRegistrationTemplate.setSdefaulttemplatename(getString(arg0, "sdefaulttemplatename", arg1));
		objReactRegistrationTemplate.setJsondata(getJSONArray(arg0, "jsondata", arg1));
		objReactRegistrationTemplate.setNstatus(getShort(arg0, "nstatus", arg1));
		objReactRegistrationTemplate.setScreendesign(getJsonObject(arg0, "screendesign", arg1));
		objReactRegistrationTemplate.setSlideoutdesign(getJSONArray(arg0, "slideoutdesign", arg1));
		objReactRegistrationTemplate.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objReactRegistrationTemplate.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objReactRegistrationTemplate.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objReactRegistrationTemplate.setNsubsampletypecode(getShort(arg0, "nsubsampletypecode", arg1));
		return objReactRegistrationTemplate;
	}

}
