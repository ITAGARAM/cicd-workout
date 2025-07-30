package com.agaramtech.qualis.compentencemanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "trainingreschedule")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReScheduleLog extends CustomizedResultsetRowMapper<ReScheduleLog> implements Serializable, RowMapper<ReScheduleLog> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntrainingrescedulecode")
	private int ntrainingrescedulecode;
	
	@Column(name = "ntrainingcode", nullable = false)
	private int ntrainingcode;
	
	@Column(name = "drescheduledate ")
	private Instant drescheduledate;
	
	@ColumnDefault("-1")
	@Column(name = "ntzrescheduledate", nullable = false)
	private short ntzrescheduledate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dscheduledate")
	private Instant dscheduledate;
	
	@ColumnDefault("-1")
	@Column(name = "ntzscheduledate", nullable = false)
	private short ntzscheduledate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dcreateddate")
	private Instant dcreateddate;
	
	@Column(name = "ntzcreateddate", nullable = false)
	private short ntzcreateddate;
	
	@Column(name = "scomments", length = 255)
	private String scomments = "";
	
	@Column(name = "noffsetdrescheduledate")
	private int noffsetdrescheduledate;
	
	@Column(name = "noffsetdscheduledate")
	private int noffsetdscheduledate;
	
	@Column(name = "noffsetdcreateddate")
	private int noffsetdcreateddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private String srescheduledate;
	
	@Transient
	private String sscheduledate;
	
	@Transient
	private String screateddate;
	
	@Transient
	private String strainername;

	@Override
	public ReScheduleLog mapRow(ResultSet arg0, int arg1) throws SQLException {
		ReScheduleLog rescheduleLog = new ReScheduleLog();
		rescheduleLog.setNtrainingrescedulecode(getInteger(arg0, "ntrainingrescedulecode", arg1));
		rescheduleLog.setNtrainingcode(getInteger(arg0, "ntrainingcode", arg1));
		rescheduleLog.setDrescheduledate(getInstant(arg0, "drescheduledate", arg1));
		rescheduleLog.setNtzscheduledate(getShort(arg0, "ntzrescheduledate", arg1));
		rescheduleLog.setDscheduledate(getInstant(arg0, "dscheduledate", arg1));
		rescheduleLog.setNtzscheduledate(getShort(arg0, "ntzscheduledate", arg1));
		rescheduleLog.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		rescheduleLog.setNtzscheduledate(getShort(arg0, "ntzcreateddate", arg1));
		rescheduleLog.setScomments(getString(arg0, "scomments", arg1));
		rescheduleLog.setNstatus(getShort(arg0, "nstatus", arg1));
		rescheduleLog.setSrescheduledate(getString(arg0, "srescheduledate", arg1));
		rescheduleLog.setSscheduledate(getString(arg0, "sscheduledate", arg1));
		rescheduleLog.setScreateddate(getString(arg0, "screateddate", arg1));
		rescheduleLog.setStrainername(getString(arg0, "strainername", arg1));
		rescheduleLog.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		rescheduleLog.setNoffsetdrescheduledate(getInteger(arg0, "noffsetdrescheduledate", arg1));
		rescheduleLog.setNoffsetdscheduledate(getInteger(arg0, "noffsetdscheduledate", arg1));
		return rescheduleLog;
	}
}
