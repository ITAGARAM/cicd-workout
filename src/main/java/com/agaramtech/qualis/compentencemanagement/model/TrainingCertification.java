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
@Table(name = "trainingcertification")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TrainingCertification extends CustomizedResultsetRowMapper<TrainingCertification>
		implements Serializable, RowMapper<TrainingCertification> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntrainingcode")
	private int ntrainingcode;
	@Column(name = "ntrainingcategorycode")
	private int ntrainingcategorycode;
	@Column(name = "strainingtopic", columnDefinition = "nvarchar", length = 100)
	private String strainingtopic = "";
	@Column(name = "ntechniquecode")
	private int ntechniquecode;
	@Column(name = "strainername", columnDefinition = "nvarchar", length = 100)
	private String strainername = "";
	@Column(name = "dtrainingdatetime")
	private Instant dtrainingdatetime;
	@Column(name = "strainingmonth", columnDefinition = "nvarchar", length = 10)
	private String strainingmonth = "";
	@Column(name = "strainingvenue", columnDefinition = "nvarchar", length = 100)
	private String strainingvenue = "";
	@Column(name = "scomments", columnDefinition = "nvarchar", length = 255)
	private String scomments = "";
	@Column(name = "dcompleteddate")
	private Instant dcompleteddate;
	@Column(name = "dconducteddate")
	private Instant dconducteddate;
	@Column(name = "dregistereddate")
	private Instant dregistereddate;
	@Column(name = "noffsetdtrainingdatetime")
	private int noffsetdtrainingdatetime;
	@Column(name = "noffsetdcompleteddate")
	private int noffsetdcompleteddate;
	@Column(name = "noffsetdconducteddate")
	private int noffsetdconducteddate;
	@Column(name = "noffsetdregistereddate")
	private int noffsetdregistereddate;
	@Column(name = "ntransactionstatus")
	private short ntransactionstatus;
	@Column(name = "ntztrainingdate")
	private short ntztrainingdate;
	@Column(name = "ntzcompleteddate")
	private short ntzcompleteddate;
	@Column(name = "ntzconducteddate")
	private short ntzconducteddate;
	@Column(name = "ntzregistereddate")
	private short ntzregistereddate;
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "ntrainingexpiryneed", nullable = false)
	private short ntrainingexpiryneed;
	@Column(name = "ntrainingexpiryvalue", nullable = false)
	private short ntrainingexpiryvalue;
	@Column(name = "ntrainingexpiryperiod", nullable = false)
	private short ntrainingexpiryperiod;
	@Column(name = "dtrainingexpirydate")
	private Instant dtrainingexpirydate;
	@Column(name = "noffsetdtrainingexpirydate")
	private int noffsetdtrainingexpirydate;
	@Column(name = "ntztrainingexpirydate")
	private short ntztrainingexpirydate;

	
	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Transient
	private transient int ncertifiedstatus;
	@Transient
	private transient String strainingdatetime;
	@Transient
	private transient String stztrainingdate;
	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient int ncompetencystatus;
	@Transient
	private transient String sfullname;
	@Transient
	private transient String stechniquename;
	@Transient
	private transient String strainingcategoryname;
	@Transient
	private transient String strainingdate;
	@Transient
	private transient String sregistereddate;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String strainingtime;
	@Transient
	private transient String sattendancestatus;
	@Transient
	private transient String stransdisplaystatuscertified;
	@Transient
	private transient String stransdisplaystatuscompotent;
	@Transient
	private transient int nparticipantcode;
	@Transient
	private transient int ntrainingdoccode;
	@Transient
	private transient String stzcompleteddate;
	@Transient
	private transient String stzconducteddate;
	@Transient
	private transient String stzregistereddate;
	@Transient
	private transient String stimezoneid;
	@Transient
	private transient String smodifieddate;
	@Transient
	private transient String speriodname;
	@Transient
	private transient String stemptrainingdatetime;
	@Transient
	private transient String strainingexpiryneed;
	@Transient
	private transient String strainingexpirydate;

	@Override
	public TrainingCertification mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TrainingCertification objTrainingCertification = new TrainingCertification();
		objTrainingCertification.setStrainingvenue(StringEscapeUtils.unescapeJava(getString(arg0, "strainingvenue", arg1)));
		objTrainingCertification.setDconducteddate(getInstant(arg0, "dconducteddate", arg1));
		objTrainingCertification.setNtechniquecode(getInteger(arg0, "ntechniquecode", arg1));
		objTrainingCertification.setDregistereddate(getInstant(arg0, "dregistereddate", arg1));
		objTrainingCertification.setStrainingtopic(StringEscapeUtils.unescapeJava(getString(arg0, "strainingtopic", arg1)));
		objTrainingCertification.setStechniquename(getString(arg0, "stechniquename", arg1));
		objTrainingCertification.setDtrainingdatetime(getInstant(arg0, "dtrainingdatetime", arg1));
		objTrainingCertification.setNtrainingcategorycode(getInteger(arg0, "ntrainingcategorycode", arg1));
		objTrainingCertification.setSregistereddate(getString(arg0, "sregistereddate", arg1));
		objTrainingCertification.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objTrainingCertification.setNtrainingcode(getInteger(arg0, "ntrainingcode", arg1));
		objTrainingCertification.setStrainingcategoryname(getString(arg0, "strainingcategoryname", arg1));
		objTrainingCertification.setNstatus(getShort(arg0, "nstatus", arg1));
		objTrainingCertification.setStrainingdate(getString(arg0, "strainingdate", arg1));
		objTrainingCertification.setScomments(StringEscapeUtils.unescapeJava(getString(arg0, "scomments", arg1)));
		objTrainingCertification.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTrainingCertification.setStrainingmonth(StringEscapeUtils.unescapeJava(getString(arg0, "strainingmonth", arg1)));
		objTrainingCertification.setStrainername(StringEscapeUtils.unescapeJava(getString(arg0, "strainername", arg1)));
		objTrainingCertification.setDcompleteddate(getInstant(arg0, "dcompleteddate", arg1));
		objTrainingCertification.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objTrainingCertification.setStrainingtime(getString(arg0, "strainingtime", arg1));
		objTrainingCertification.setNtztrainingdate(getShort(arg0, "ntztrainingdate", arg1));
		objTrainingCertification.setNparticipantcode(getInteger(arg0, "nparticipantcode", arg1));
		objTrainingCertification.setSfullname(getString(arg0, "sfullname", arg1));
		objTrainingCertification.setSattendancestatus(getString(arg0, "sattendancestatus", arg1));
		objTrainingCertification.setStransdisplaystatuscertified(getString(arg0, "stransdisplaystatuscertified", arg1));
		objTrainingCertification.setStransdisplaystatuscompotent(getString(arg0, "stransdisplaystatuscompotent", arg1));
		objTrainingCertification.setNtrainingdoccode(getInteger(arg0, "ntrainingdoccode", arg1));
		objTrainingCertification.setNcertifiedstatus(getInteger(arg0, "ncertifiedstatus", arg1));
		objTrainingCertification.setNcompetencystatus(getInteger(arg0, "ncompetencystatus", arg1));
		objTrainingCertification.setStrainingdatetime(getString(arg0, "strainingdatetime", arg1));
		objTrainingCertification.setStztrainingdate(getString(arg0, "stztrainingdate", arg1));
		objTrainingCertification.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objTrainingCertification.setNoffsetdtrainingdatetime(getInteger(arg0, "noffsetdtrainingdatetime", arg1));
		objTrainingCertification.setNoffsetdregistereddate(getInteger(arg0, "noffsetdregistereddate", arg1));
		objTrainingCertification.setNoffsetdconducteddate(getInteger(arg0, "noffsetdconducteddate", arg1));
		objTrainingCertification.setNoffsetdcompleteddate(getInteger(arg0, "noffsetdcompleteddate", arg1));
		objTrainingCertification.setStzcompleteddate(getString(arg0, "stzcompleteddate", arg1));
		objTrainingCertification.setStzconducteddate(getString(arg0, "stzconducteddate", arg1));
		objTrainingCertification.setStzregistereddate(getString(arg0, "stzregistereddate", arg1));
		objTrainingCertification.setNtzcompleteddate(getShort(arg0, "ntzcompleteddate", arg1));
		objTrainingCertification.setNtzconducteddate(getShort(arg0, "ntzconducteddate", arg1));
		objTrainingCertification.setNtzregistereddate(getShort(arg0, "ntzregistereddate", arg1));
		objTrainingCertification.setStimezoneid(getString(arg0, "stimezoneid", arg1));
		objTrainingCertification.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTrainingCertification.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objTrainingCertification.setNtrainingexpiryneed(getShort(arg0, "ntrainingexpiryneed", arg1));
		objTrainingCertification.setNtrainingexpiryvalue(getShort(arg0, "ntrainingexpiryvalue", arg1));
		objTrainingCertification.setNtrainingexpiryperiod(getShort(arg0, "ntrainingexpiryperiod", arg1));
		objTrainingCertification.setDtrainingexpirydate(getInstant(arg0, "dtrainingexpirydate", arg1));
		objTrainingCertification.setNtztrainingexpirydate(getShort(arg0, "ntztrainingexpirydate", arg1));
		objTrainingCertification.setNoffsetdtrainingexpirydate(getInteger(arg0, "noffsetdtrainingexpirydate", arg1));
		objTrainingCertification.setSperiodname(getString(arg0, "speriodname", arg1));
		objTrainingCertification.setStemptrainingdatetime(getString(arg0, "stemptrainingdatetime", arg1));
		objTrainingCertification.setStrainingexpiryneed(getString(arg0, "strainingexpiryneed", arg1));
		objTrainingCertification.setStrainingexpirydate(getString(arg0, "strainingexpirydate", arg1));

		return objTrainingCertification;
	}

}
