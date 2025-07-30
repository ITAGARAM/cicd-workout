package com.agaramtech.qualis.credential.model;

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

/**
 * This class is used to map fields of 'qualisforms' table of database
*/
@Entity
@Table(name="qualisforms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QualisForms extends CustomizedResultsetRowMapper<QualisForms> implements Serializable, RowMapper<QualisForms> {
	
	private static final long serialVersionUID = 1L; 
	
	@Id
	@Column(name="nformcode") 
	private short nformcode;
	
	@Column(name="nmenucode", nullable=false) 
	private short nmenucode;
	
	@Column(name="nmodulecode", nullable=false) 
	private short nmodulecode;
	
	@Column(name="sformname",length = 100, nullable=false) 
	private String sformname;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Column(name="sclassname",length = 255, nullable=false) 
	private String sclassname;

	@Column(name="surl",length = 50) 
	private String surl;
	
	@ColumnDefault("0")
	@Column(name="nsorter", nullable=false) 
	private short nsorter = (short) Enumeration.TransactionStatus.ALL.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false) 
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@Transient
	private transient String smodulename;
	@Transient
	private transient String smenuname;
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient String sformdisplayname;
	@Transient
	private transient String smoduledisplayname;
	@Transient
	private transient String sdisplayname;
//	@Transient
//	private transient String serdiagram;

	
	@Override
	public QualisForms mapRow(ResultSet arg0, int arg1)
			throws SQLException {
		final QualisForms objQualisForms = new QualisForms();
		objQualisForms.setNformcode(getShort(arg0,"nformcode",arg1));
		objQualisForms.setNmenucode(getShort(arg0,"nmenucode",arg1));
		objQualisForms.setNmodulecode(getShort(arg0,"nmodulecode",arg1));
		objQualisForms.setSformname(getString(arg0, "sformname", arg1));
		objQualisForms.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objQualisForms.setSclassname(getString(arg0, "sclassname", arg1));
		objQualisForms.setSurl(getString(arg0, "surl", arg1));
		objQualisForms.setNsorter(getShort(arg0,"nsorter",arg1));
		objQualisForms.setNstatus(getShort(arg0,"nstatus",arg1));
		objQualisForms.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		objQualisForms.setSmodulename(getString(arg0,"smodulename",arg1));
		objQualisForms.setSmenuname(getString(arg0,"smenuname",arg1));
		objQualisForms.setSdefaultname(getString(arg0,"sdefaultname",arg1));
		objQualisForms.setSformdisplayname(getString(arg0,"sformdisplayname",arg1));
		objQualisForms.setSmoduledisplayname(getString(arg0,"smoduledisplayname",arg1));
		objQualisForms.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
	//	objQualisForms.setSerdiagram(getString(arg0, "serdiagram", arg1));

		return objQualisForms;
	}

}
