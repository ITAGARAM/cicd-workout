package com.agaramtech.qualis.configuration.model;

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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name="existinglinktable")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExistingLinkTable  extends  CustomizedResultsetRowMapper<ExistingLinkTable> implements Serializable, RowMapper<ExistingLinkTable> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="nexistingcode")
	private short nexistingcode;
	
	@Column(name = "sdisplaymember" , length = 100, nullable=false)
	private String  sdisplaymember;
	
	@Column(name = "sexistingtablename" , length = 100, nullable=false)
	private String  sexistingtablename;
	
	@Column(name = "splainquery" , length = 2500, nullable=false)
	private String  splainquery;
	
	@Column(name = "svaluemember" , length = 100, nullable=false)
	private String  svaluemember;
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String sdisplaylabelname;
	@Transient
	private List<Map<String, Object>> lstExistingLinkTable;
	@Transient
	private short nsorter;
	@Transient
	private short nchildsamptypesedigncode;
	@Transient
	private short nsampletypedesigncode;
	@Transient
	private String sparentkeyname;

	@Override
	public ExistingLinkTable mapRow(ResultSet arg0, int arg1) throws SQLException {
		ExistingLinkTable objExistingLinkTable = new ExistingLinkTable();
		objExistingLinkTable.setNexistingcode(getShort(arg0,"nexistingcode",arg1));
		objExistingLinkTable.setSdisplaymember(StringEscapeUtils.unescapeJava(getString(arg0,"sdisplaymember",arg1)));
		objExistingLinkTable.setSexistingtablename(StringEscapeUtils.unescapeJava(getString(arg0,"sexistingtablename",arg1)));
		objExistingLinkTable.setSvaluemember(StringEscapeUtils.unescapeJava(getString(arg0,"svaluemember",arg1)));
		objExistingLinkTable.setSplainquery(StringEscapeUtils.unescapeJava(getString(arg0,"splainquery",arg1)));
		objExistingLinkTable.setNstatus(getShort(arg0,"nstatus",arg1));
		objExistingLinkTable.setSdisplaylabelname(StringEscapeUtils.unescapeJava(getString(arg0,"sdisplaylabelname",arg1)));
		objExistingLinkTable.setNsorter(getShort(arg0,"nsorter",arg1));
		objExistingLinkTable.setNchildsamptypesedigncode(getShort(arg0,"nchildsamptypesedigncode",arg1));
		objExistingLinkTable.setNsampletypedesigncode(getShort(arg0,"nsampletypedesigncode",arg1));
		objExistingLinkTable.setSparentkeyname(StringEscapeUtils.unescapeJava(getString(arg0,"sparentkeyname",arg1)));
		objExistingLinkTable.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objExistingLinkTable.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		return objExistingLinkTable;
	}

}
