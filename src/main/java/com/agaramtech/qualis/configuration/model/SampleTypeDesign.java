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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sampletypedesign")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleTypeDesign extends CustomizedResultsetRowMapper<SampleTypeDesign> implements Serializable, RowMapper<SampleTypeDesign> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsampletypedesigncode")	
	private short nsampletypedesigncode;
	
	@Column(name = "nsampletypecode", nullable = false)
	private short nsampletypecode;
	
	@Column(name = "nexistinglinkcode", nullable = false)
	private short nexistinglinkcode;
	
	@Column(name = "sdisplaylabelname", length = 50)
	private String sdisplayname;
	
	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)	
	private short nsorter = 0;
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();


	@Override
	public SampleTypeDesign mapRow(ResultSet arg0, int arg1) throws SQLException {
		SampleTypeDesign objSampleTypeDesign = new SampleTypeDesign();
		objSampleTypeDesign.setNsampletypedesigncode(getShort(arg0,"nsampletypedesigncode",arg1));
		objSampleTypeDesign.setNsampletypecode(getShort(arg0,"nsampletypecode",arg1));
		objSampleTypeDesign.setNexistinglinkcode(getShort(arg0,"nexistinglinkcode",arg1));
		objSampleTypeDesign.setSdisplayname(StringEscapeUtils.unescapeJava(getString(arg0,"sdisplayname",arg1)));
		objSampleTypeDesign.setNsorter(getShort(arg0,"nsorter",arg1));
		objSampleTypeDesign.setNstatus(getShort(arg0,"nstatus",arg1));
		objSampleTypeDesign.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objSampleTypeDesign.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		return objSampleTypeDesign;
	}

}
