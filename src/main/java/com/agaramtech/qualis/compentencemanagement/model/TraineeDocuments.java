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

@Data
@Entity
@Table(name = "traineedocuments")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TraineeDocuments extends CustomizedResultsetRowMapper<TraineeDocuments>
		implements Serializable, RowMapper<TraineeDocuments> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ntraineedoccode")
	private int ntraineedoccode;
	@Column(name = "nparticipantcode")
	private int nparticipantcode;
	@Column(name = "nusercode")
	private int nusercode;
	@Column(name = "sfilename", columnDefinition = "nvarchar", length = 100)
	private String sfilename = "";
	@Column(name = "sfiledesc", columnDefinition = "nvarchar", length = 255)
	private String sfiledesc = "";
	@Column(name = "ssystemfilename", columnDefinition = "nvarchar", length = 100)
	private String ssystemfilename = "";
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
	private int nfilesize;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String screateddate;
	@Transient
	private transient String sfilesize;
	@Transient
	private transient String slinkname;
	@Transient
	private transient int ntrainingcode;

	

	@Override
	public TraineeDocuments mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TraineeDocuments objTraineeDocuments = new TraineeDocuments();
		objTraineeDocuments.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		objTraineeDocuments.setNstatus(getShort(arg0, "nstatus", arg1));
		objTraineeDocuments.setSfiledesc(StringEscapeUtils.unescapeJava(getString(arg0, "sfiledesc", arg1)));
		objTraineeDocuments.setNparticipantcode(getInteger(arg0, "nparticipantcode", arg1));
		objTraineeDocuments.setNusercode(getInteger(arg0, "nusercode", arg1));
		objTraineeDocuments.setNtraineedoccode(getInteger(arg0, "ntraineedoccode", arg1));
		objTraineeDocuments.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objTraineeDocuments.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objTraineeDocuments.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objTraineeDocuments.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		objTraineeDocuments.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objTraineeDocuments.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objTraineeDocuments.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objTraineeDocuments.setScreateddate(getString(arg0, "screateddate", arg1));
		objTraineeDocuments.setSfilesize(getString(arg0, "sfilesize", arg1));
		objTraineeDocuments.setSlinkname(getString(arg0, "slinkname", arg1));
		objTraineeDocuments.setNtrainingcode(getInteger(arg0, "ntrainingcode", arg1));

		return objTraineeDocuments;
	}
}
