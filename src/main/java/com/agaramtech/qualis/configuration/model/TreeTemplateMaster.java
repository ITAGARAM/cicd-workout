package com.agaramtech.qualis.configuration.model;

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
@Table(name = "treetemplatemaster")
@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TreeTemplateMaster extends  CustomizedResultsetRowMapper<TreeTemplateMaster>  implements Serializable, RowMapper<TreeTemplateMaster> {
	@Id
	@Column(name = "ntemplatecode")
	private int ntemplatecode;
	
	@Column(name = "ncategorycode", nullable = false)
	private int ncategorycode;
	
	@Column(name = "nformcode", nullable = false)
	private short nformcode;
	
	@Column(name = "nrootcode", nullable = false)
	private short nrootcode;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String sversiondescription;
	@Transient
	private String stablename;
	@Transient
	private int ntreeversiontempcode;

	@Override
	public TreeTemplateMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		TreeTemplateMaster objTreeTemplateMaster = new TreeTemplateMaster();
		objTreeTemplateMaster.setNrootcode(getShort(arg0,"nrootcode",arg1));
		objTreeTemplateMaster.setNtemplatecode(getInteger(arg0,"ntemplatecode",arg1));
		objTreeTemplateMaster.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objTreeTemplateMaster.setNformcode(getShort(arg0,"nformcode",arg1));
		objTreeTemplateMaster.setNstatus(getShort(arg0,"nstatus",arg1));
		objTreeTemplateMaster.setNcategorycode(getInteger(arg0,"ncategorycode",arg1));
		objTreeTemplateMaster.setSversiondescription(StringEscapeUtils.unescapeJava(getString(arg0,"sversiondescription",arg1)));
		objTreeTemplateMaster.setNtreeversiontempcode(getShort(arg0,"ntreeversiontempcode",arg1));
		objTreeTemplateMaster.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTreeTemplateMaster.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objTreeTemplateMaster;
	}

}