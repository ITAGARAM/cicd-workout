package com.agaramtech.qualis.worklistpreparation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
@Table(name = "worklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Worklist extends CustomizedResultsetRowMapper<Worklist> implements Serializable,RowMapper<Worklist>{
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nworklistcode")
	private int nworklistcode;
	
	@Column(name = "nsampletypecode")
	private int nsampletypecode;
	
	@Column(name = "nregtypecode")
	private int nregtypecode;
	
	@Column(name = "nregsubtypecode")
	private int nregsubtypecode;
	
	@Column(name = "ntestcode")
	private int ntestcode;
	
	@Column(name = "nsectioncode")
	private int nsectioncode;
	
	@Column(name = "napprovalversioncode")
	private int napprovalversioncode;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sworklistno;
	@Transient
	private transient String stestname;
	@Transient
	private transient String stestsynonym;
	@Transient
	private transient String ssectionname;
	@Transient
	private transient int ntransactionstatus;	
	@Transient
	private transient String stransdisplaystatus;
	@Override
	public Worklist mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Worklist objWorklist = new Worklist();
		objWorklist.setNworklistcode(getInteger(arg0, "nworklistcode", arg1));
		objWorklist.setNsampletypecode(getInteger(arg0, "nsampletypecode", arg1));
		objWorklist.setNregtypecode(getInteger(arg0, "nregtypecode", arg1));
		objWorklist.setNregsubtypecode(getInteger(arg0, "nregsubtypecode", arg1));
		objWorklist.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		objWorklist.setNsectioncode(getInteger(arg0, "nsectioncode", arg1));
		objWorklist.setNapprovalversioncode(getInteger(arg0, "napprovalversioncode", arg1));
		objWorklist.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objWorklist.setNstatus(getShort(arg0, "nstatus", arg1));
		objWorklist.setSworklistno(getString(arg0,"sworklistno",arg1));
		objWorklist.setStestname(getString(arg0,"stestname",arg1));
		objWorklist.setStestsynonym(getString(arg0,"stestsynonym",arg1));
		objWorklist.setSsectionname(getString(arg0,"ssectionname",arg1));
		objWorklist.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objWorklist.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		return objWorklist;
	}
	
}
