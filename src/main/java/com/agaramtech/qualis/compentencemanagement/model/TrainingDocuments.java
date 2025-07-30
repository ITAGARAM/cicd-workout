package com.agaramtech.qualis.compentencemanagement.model;

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
@Table(name = "trainingdocuments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TrainingDocuments extends CustomizedResultsetRowMapper<TrainingDocuments>
		implements Serializable, RowMapper<TrainingDocuments> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntrainingdoccode")
	private int ntrainingdoccode;

	@Column(name = "ntrainingcode")
	private int ntrainingcode;

	@Column(name = "sfilename", columnDefinition = "nvarchar", length = 100)
	private String sfilename="";

	@Column(name = "sfiledesc", columnDefinition = "nvarchar", length = 255)
	private String sfiledesc="";

	@Column(name = "ssystemfilename", columnDefinition = "nvarchar", length = 100)
	private String ssystemfilename="";

	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;

	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode;

	@Column(name = "dcreateddate")
	private Instant dcreateddate;

	@Column(name = "noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate;

	@Column(name = "ntzcreateddate", nullable = false)
	private short ntzcreateddate;

	@Column(name = "nfilesize", nullable = false)
	@Transient
	private int nfilesize;
	
	
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String screateddate;
	@Transient
	private transient String sfilesize;
	@Transient
	private transient String slinkname;


	@Override
	public TrainingDocuments mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TrainingDocuments trainingDocuments = new TrainingDocuments();
		trainingDocuments.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		trainingDocuments.setNtrainingdoccode(getInteger(arg0, "ntrainingdoccode", arg1));
		trainingDocuments.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		trainingDocuments.setSfiledesc(StringEscapeUtils.unescapeJava(getString(arg0, "sfiledesc", arg1)));
		trainingDocuments.setNstatus(getShort(arg0, "nstatus", arg1));
		trainingDocuments.setNtrainingcode(getInteger(arg0, "ntrainingcode", arg1));
		trainingDocuments.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		trainingDocuments.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		trainingDocuments.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		trainingDocuments.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		trainingDocuments.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		trainingDocuments.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		trainingDocuments.setScreateddate(getString(arg0, "screateddate", arg1));
		trainingDocuments.setSfilesize(getString(arg0, "sfilesize", arg1));
		trainingDocuments.setSlinkname(getString(arg0, "slinkname", arg1));
		return trainingDocuments;
	}
}
