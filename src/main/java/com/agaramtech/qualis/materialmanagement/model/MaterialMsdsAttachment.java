package com.agaramtech.qualis.materialmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
@Table(name="materialmsdsattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MaterialMsdsAttachment  extends CustomizedResultsetRowMapper<MaterialMsdsAttachment> implements Serializable,RowMapper<MaterialMsdsAttachment>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nmaterialfilecode")
	private int nmaterialfilecode;

	@Column(name = "nmaterialcode", nullable = false)
	private int nmaterialcode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;

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
	

	@Override
	public MaterialMsdsAttachment mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final MaterialMsdsAttachment objTestFile = new MaterialMsdsAttachment();
		
		objTestFile.setNmaterialfilecode(getInteger(arg0,"nmaterialfilecode",arg1));
		objTestFile.setNmaterialcode(getInteger(arg0,"nmaterialcode",arg1));
		objTestFile.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objTestFile.setNstatus(getShort(arg0,"sfilename",arg1));
		objTestFile.setSlinkname(getString(arg0,"slinkname",arg1));
		objTestFile.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTestFile.setSattachmenttype(getString(arg0,"sattachmenttype",arg1));
		objTestFile.setScreateddate(getString(arg0,"screateddate",arg1));
		objTestFile.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestFile.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objTestFile;
	}

}
