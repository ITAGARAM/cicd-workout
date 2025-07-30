package com.agaramtech.qualis.scheduler.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'schedulemastermonthly' table of the Database.
 */
@Entity 
@Table(name="schedulemastermonthly")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ScheduleMasterMonthly extends CustomizedResultsetRowMapper<ScheduleMasterMonthly> implements Serializable,RowMapper<ScheduleMasterMonthly> {

	private static final long serialVersionUID = 1L;
	
	@Id
    @Column(name = "nschedulecode")
	private int nschedulecode;

	@Column(name = "nexactday", nullable=false)
	private int nexactday;
	
	@Column(name = "nmonthlyoccurrencetype", nullable=false)
	private int nmonthlyoccurrencetype;
	
	@Column(name = "njan", nullable=false)
	private int njan;

	@Column(name = "nfeb", nullable=false)
	private int nfeb;
	
	@Column(name = "nmar", nullable=false)
	private int nmar;
	
	@Column(name = "napr", nullable=false)
	private int napr;
	
	@Column(name = "nmay", nullable=false)
	private int nmay;
	
	@Column(name = "njun", nullable=false)
	private int njun;
	
	@Column(name = "njul", nullable=false)
	private int njul;
	
	@Column(name = "naug", nullable=false)
	private int naug;
	
	@Column(name = "nsep", nullable=false)
	private int nsep;
	
	@Column(name = "noct", nullable=false)
	private int noct;
	
	@Column(name = "nnov", nullable=false)
	private int nnov;
	
	@Column(name = "ndec", nullable=false)
	private int ndec;
	
	@Column(name = "nmonthyweek", nullable=false)
	private int nmonthyweek;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	

	public ScheduleMasterMonthly mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ScheduleMasterMonthly objSch = new ScheduleMasterMonthly();
		
		objSch.setNschedulecode(getInteger(arg0,"nschedulecode",arg1));
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
		objSch.setNstatus(getShort(arg0,"nstatus",arg1));
		objSch.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objSch.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		return objSch;
	}
	

}
