package com.agaramtech.qualis.compentencemanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Data
@Table(name = "trainingparticipants")
public class TrainingParticipants extends CustomizedResultsetRowMapper<TrainingParticipants>
		implements Serializable, RowMapper<TrainingParticipants> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nparticipantcode")
	private int nparticipantcode;
	
	@Column(name = "ntrainingcode")
	private int ntrainingcode;
	
	@Column(name = "nusercode")
	private int nusercode;
	
	@Column(name = "ncertifiedstatus")
	private short ncertifiedstatus;
	
	@Column(name = "ncompetencystatus")
	private short ncompetencystatus;
	
	@Column(name = "ntransactionstatus")
	private short ntransactionstatus;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String sfullname;
	@Transient
	private transient String stransdisplaystatuscertified;
	@Transient
	private transient String stransdisplaystatuscompotent;
	@Transient
	private transient String strainername;
	@Transient
	private transient String strainingtopic;
	@Transient
	private transient String strainingvenue;
	@Transient
	private transient short nattachmenttypecode;
	@Transient
	private transient String strainingdatetime;
	@Transient
	private transient String strainingtime;
	@Transient
	private transient String strainingstatus;
	@Transient
	private transient String susername;
	@Transient
	private transient String stechniquename;
	@Transient
	private transient int ntrainingcategorycode;
	@Transient
	private transient int ntechniquecode;
	@Transient
	private transient String participantname;
	@Transient
	private transient String strainingcategoryname;
	@Transient
	private transient String smodifieddate;
	@Transient
	private transient int noffsetdtrainingdatetime;

	@Override
	public TrainingParticipants mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TrainingParticipants trainingParticipants = new TrainingParticipants();
		trainingParticipants.setNparticipantcode(getInteger(arg0, "nparticipantcode", arg1));
		trainingParticipants.setNtrainingcode(getInteger(arg0, "ntrainingcode", arg1));
		trainingParticipants.setNusercode(getInteger(arg0, "nusercode", arg1));
		trainingParticipants.setNcertifiedstatus(getShort(arg0, "ncertifiedstatus", arg1));
		trainingParticipants.setNcompetencystatus(getShort(arg0, "ncompetencystatus", arg1));
		trainingParticipants.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		trainingParticipants.setSfullname(getString(arg0, "sfullname", arg1));
		trainingParticipants.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		trainingParticipants.setNsitecode(getShort(arg0, "nsitecode", arg1));
		trainingParticipants.setNstatus(getShort(arg0, "nstatus", arg1));
		trainingParticipants.setStransdisplaystatuscertified(getString(arg0, "stransdisplaystatuscertified", arg1));
		trainingParticipants.setStransdisplaystatuscompotent(getString(arg0, "stransdisplaystatuscompotent", arg1));
		trainingParticipants.setStrainername(getString(arg0, "strainername", arg1));
		trainingParticipants.setStrainingtopic(getString(arg0, "strainingtopic", arg1));
		trainingParticipants.setStrainingvenue(getString(arg0, "strainingvenue", arg1));
		trainingParticipants.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		trainingParticipants.setStrainingdatetime(getString(arg0, "strainingdatetime", arg1));
		trainingParticipants.setStrainingtime(getString(arg0, "strainingtime", arg1));
		trainingParticipants.setStrainingstatus(getString(arg0, "strainingstatus", arg1));
		trainingParticipants.setSusername(getString(arg0, "susername", arg1));
		trainingParticipants.setStechniquename(getString(arg0, "stechniquename", arg1));
		trainingParticipants.setNtrainingcategorycode(getInteger(arg0, "ntrainingcategorycode", arg1));
		trainingParticipants.setNtechniquecode(getInteger(arg0, "ntechniquecode", arg1));
		trainingParticipants.setParticipantname(getString(arg0, "participantname", arg1));
		trainingParticipants.setStrainingcategoryname(getString(arg0, "strainingcategoryname", arg1));
		trainingParticipants.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		trainingParticipants.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		trainingParticipants.setNoffsetdtrainingdatetime(getInteger(arg0, "noffsetdtrainingdatetime", arg1));
		return trainingParticipants;
	}
}
