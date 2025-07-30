package com.agaramtech.qualis.scheduler.model;

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

/**
 * This class is used to map the fields of 'schedulemaster' table of the Database.
 */

@Entity 
@Table(name="schedulemaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ScheduleMaster extends CustomizedResultsetRowMapper<ScheduleMaster> implements Serializable,RowMapper<ScheduleMaster> {

	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "nschedulecode")
	private int nschedulecode;
	
	@Column(name = "sschedulename", length=100, nullable=false)
	private String sschedulename;
	
	@Column(name = "sscheduletype", length=1, nullable=false)
	private String sscheduletype;
	
	@Column(name = "sremarks", length=255)
	private String sremarks;
	
	@Column(name = "dstartdate", nullable=false)
	private Instant dstartdate;
	
	@Column(name = "dstarttime", nullable=false)
	private Instant dstarttime;
	
	@Column(name = "noccurencenooftimes", nullable=false)
	private int noccurencenooftimes;
	
	@Column(name = "soccurencehourwiseinterval", length=5, nullable=false)
	private String soccurencehourwiseinterval;
	
	@Column(name = "noccurencedaywiseinterval", nullable=false)
	private int noccurencedaywiseinterval;
	
	@Column(name = "denddate", nullable=false)
	private Instant denddate;
	
	@Column(name = "dendtime", nullable=false)
	private Instant dendtime;
	
	@Column(name = "ntzstartdatetimezone", nullable=false)
	private short ntzstartdatetimezone = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "ntzenddatetimezone", nullable=false)
	private short ntzenddatetimezone = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "ntzstarttimetimezone", nullable=false)
	private short ntzstarttimetimezone = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "ntzendtimetimezone", nullable=false)
	private short ntzendtimetimezone = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nmonthyweek", nullable=false)
	private int nmonthyweek;
	
	@ColumnDefault("0")
	@Column(name = "noffsetdstartdate", nullable=false)
	private int noffsetdstartdate =(int)Enumeration.TransactionStatus.ALL.gettransactionstatus();
	
	@ColumnDefault("0")
	@Column(name = "noffsetdenddate", nullable=false)
	private int noffsetdenddate = (int)Enumeration.TransactionStatus.ALL.gettransactionstatus();
	
	@ColumnDefault("0")
	@Column(name = "noffsetdstarttime", nullable=false)
	private int noffsetdstarttime = (int)Enumeration.TransactionStatus.ALL.gettransactionstatus();
	
	@ColumnDefault("0")
	@Column(name = "noffsetdendtime", nullable=false)
	private int noffsetdendtime = (int)Enumeration.TransactionStatus.ALL.gettransactionstatus();
	
	@ColumnDefault("8")
	@Column(name = "ntransactionstatus", nullable=false)
	private int ntransactionstatus = (int)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sstartdate;
	@Transient
	private transient String stzstartdate;
	@Transient
	private transient String senddate;
	@Transient
	private transient String stzenddate;
	@Transient
	private transient String sstarttime;
	@Transient
	private transient String stzstarttime;
	@Transient
	private transient String sendtime;
	@Transient
	private transient String stzendtime;
	@Transient
	private transient String stransstatus;
	@Transient
	private transient String stempscheduleType;
	
	@Transient
	private transient int nexactday;
	@Transient
	private transient int nmonthlyoccurrencetype;
	@Transient
	private transient int njan;
	@Transient
	private transient int nfeb;
	@Transient
	private transient int nmar;
	@Transient
	private transient int napr;
	@Transient
	private transient int nmay;
	@Transient
	private transient int njun;
	@Transient
	private transient int njul;
	@Transient
	private transient int naug;
	@Transient
	private transient int nsep;
	@Transient
	private transient int noct;
	@Transient
	private transient int nnov;
	@Transient
	private transient int ndec;
	@Transient
	private transient int nsunday;
	@Transient
	private transient int nmonday;
	@Transient
	private transient int ntuesday;
	@Transient
	private transient int nwednesday;
	@Transient
	private transient int nthursday;
	@Transient
	private transient int nfriday;
	@Transient
	private transient int nsaturday;
	
	

	@Override
	public ScheduleMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ScheduleMaster objSch = new ScheduleMaster();
		
		objSch.setNschedulecode(getInteger(arg0,"nschedulecode",arg1));
		objSch.setSschedulename(StringEscapeUtils.unescapeJava(getString(arg0,"sschedulename",arg1)));
		objSch.setSscheduletype(StringEscapeUtils.unescapeJava(getString(arg0,"sscheduletype",arg1)));
		objSch.setSremarks(StringEscapeUtils.unescapeJava(getString(arg0,"sremarks",arg1)));
		objSch.setDstartdate(getInstant(arg0,"dstartdate",arg1));
		objSch.setDstarttime(getInstant(arg0,"dstarttime",arg1));
		objSch.setDenddate(getInstant(arg0,"denddate",arg1));
		objSch.setDendtime(getInstant(arg0,"dendtime",arg1));
		objSch.setNoccurencenooftimes(getInteger(arg0,"noccurencenooftimes",arg1));
		objSch.setSoccurencehourwiseinterval(StringEscapeUtils.unescapeJava(getString(arg0,"soccurencehourwiseinterval",arg1)));
		objSch.setNoccurencedaywiseinterval(getInteger(arg0,"noccurencedaywiseinterval",arg1));
		objSch.setNmonthyweek(getInteger(arg0,"nmonthyweek",arg1));
		objSch.setNexactday(getInteger(arg0,"nexactday",arg1));
		objSch.setNmonthlyoccurrencetype(getInteger(arg0,"nmonthlyoccurrencetype",arg1));
		objSch.setNjan(getInteger(arg0,"njan",arg1));
		objSch.setNfeb(getInteger(arg0,"nfeb",arg1));
		objSch.setNmar(getInteger(arg0,"nmar",arg1));
		objSch.setNapr(getInteger(arg0,"napr",arg1));
		objSch.setNmay(getInteger(arg0,"nmay",arg1));
		objSch.setNjun(getInteger(arg0,"njun",arg1));
		objSch.setNjul(getInteger(arg0,"njul",arg1));
		objSch.setNaug(getInteger(arg0,"naug",arg1));
		objSch.setNsep(getInteger(arg0,"nsep",arg1));
		objSch.setNoct(getInteger(arg0,"noct",arg1));
		objSch.setNnov(getInteger(arg0,"nnov",arg1));
		objSch.setNdec(getInteger(arg0,"ndec",arg1));
		objSch.setNsunday(getInteger(arg0,"nsunday",arg1));
		objSch.setNmonday(getInteger(arg0,"nmonday",arg1));
		objSch.setNtuesday(getInteger(arg0,"ntuesday",arg1));
		objSch.setNwednesday(getInteger(arg0,"nwednesday",arg1));
		objSch.setNthursday(getInteger(arg0,"nthursday",arg1));
		objSch.setNfriday(getInteger(arg0,"nfriday",arg1));
		objSch.setNsaturday(getInteger(arg0,"nsaturday",arg1));
		objSch.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSch.setNstatus(getShort(arg0,"nstatus",arg1));
		
		objSch.setSstartdate(getString(arg0,"sstartdate",arg1));
		objSch.setStzstartdate(getString(arg0,"stzstartdate",arg1));
		objSch.setSstarttime(getString(arg0,"sstarttime",arg1));
		objSch.setStzstarttime(getString(arg0,"stzstarttime",arg1));
		objSch.setSenddate(getString(arg0,"senddate",arg1));
		objSch.setStzenddate(getString(arg0,"stzenddate",arg1));
		objSch.setSendtime(getString(arg0,"sendtime",arg1));
		objSch.setStzendtime(getString(arg0,"stzendtime",arg1));
		objSch.setNoffsetdstartdate(getInteger(arg0,"noffsetdstartdate",arg1));
		objSch.setNoffsetdstarttime(getInteger(arg0,"noffsetdstarttime",arg1));
		objSch.setNoffsetdenddate(getInteger(arg0,"noffsetdenddate",arg1));
		objSch.setNoffsetdendtime(getInteger(arg0,"noffsetdendtime",arg1));
		
		objSch.setNtzstartdatetimezone(getShort(arg0,"ntzstartdatetimezone",arg1));
		objSch.setNtzenddatetimezone(getShort(arg0,"ntzenddatetimezone",arg1));
		objSch.setNtzstarttimetimezone(getShort(arg0,"ntzstarttimetimezone",arg1));
		objSch.setNtzendtimetimezone(getShort(arg0,"ntzendtimetimezone",arg1));
		
		objSch.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objSch.setStransstatus(getString(arg0,"stransstatus",arg1));
		objSch.setStempscheduleType(getString(arg0,"stempscheduleType",arg1));
		objSch.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		
		return objSch;
	}

}
