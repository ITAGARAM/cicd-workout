package com.agaramtech.qualis.scheduler.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
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

//ALPD-4941 Created SchedulerTestDetail pojo for Scheduler configuration screen
@Entity
@Table(name = "schedulertestdetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchedulerTestDetail extends CustomizedResultsetRowMapper<SchedulerTestDetail>
implements Serializable, RowMapper<SchedulerTestDetail> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nschedulertestcode")
	private int nschedulertestcode;

	@Column(name = "nschedulersubsamplecode")
	private int nschedulersubsamplecode;

	@Column(name = "nschedulersamplecode")
	private int nschedulersamplecode;

	@Column(name = "ntestgrouptestcode")
	private int ntestgrouptestcode;

	@Column(name = "ntestcode")
	private int ntestcode;

	@Column(name = "nsectioncode")
	private int nsectioncode;

	@Column(name = "nmethodcode")
	private int nmethodcode;

	@Column(name = "ninstrumentcatcode")
	private int ninstrumentcatcode;

	@Column(name = "nchecklistversioncode")
	private int nchecklistversioncode;

	@Column(name = "ntestrepeatno")
	private short ntestrepeatno;

	@Column(name = "ntestretestno", nullable = false)
	private short ntestretestno;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dtransactiondate")
	private Instant dtransactiondate;

	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;

	@Column(name = "ntransdatetimezonecode")
	private short ntransdatetimezonecode;

	@Column(name = "nsitecode")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient short nrepeatcountno;
	@Transient
	private transient int nparenttestgrouptestcode;
	@Transient
	private transient int ntestgrouprulesenginecode;
	@Transient
	private transient String stestsynonym;
	@Transient
	private transient int ntransactionstatus;
	@Transient
	private transient String ssamplename;
	@Transient
	private transient String scomponentname;
	@Transient
	private transient String ssectionname;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String ssampleqty;
	@Transient
	private transient Date djobaccepteddate;
	@Transient
	private transient Date dallotteddate;
	@Transient
	private transient Date dcompleteddate;
	@Transient
	private transient Date dteststarteddate;
	@Transient
	private transient int njoballocationcode;
	@Transient
	private transient int ntestgrouptestparametercode;
	@Transient
	private transient int napprovalversioncode;
	@Transient
	private transient String ssourcename;
	@Transient
	private transient String smethodname;
	@Transient
	private transient String ssamplearno;
	@Transient
	private transient String sarno;
	@Transient
	private transient String sinstrumentcatname;
	@Transient
	private transient String nretestrepeat;
	@Transient
	private transient String sactiondisplaystatus;
	@Transient
	private transient String scolorhexcode;
	@Transient
	private transient String smanuflotno;
	@Transient
	private transient String stestname;
	@Transient
	private transient int nchecklistcode;
	@Transient
	private transient float ncost;
	@Transient
	private transient int ntesthistorycode;
	@Transient
	private transient String samplearno;
	@Transient
	private transient int nspecsampletypecode;
	@Transient
	private transient int ncomponentcode;
	@Transient
	private transient int nallottedspeccode;
	@Transient
	private transient String sparametersynonym;
	@Transient
	private transient String sresult;
	@Transient
	private transient String sgradename;
	@Transient
	private transient String repretest;
	@Transient
	private transient int limsprimarycode;
	@Transient
	private transient String sbatcharno;
	@Transient
	private transient String sampleID;
	@Transient
	private transient int nprojectmastercode;
	@Transient
	private transient String sremarks;
	@Transient
	private transient String ssampleid;
	@Transient
	private transient String sshipmenttracking;
	@Transient
	private transient Date doutsourcedate;
	@Transient
	private transient int nexternalordertestcode;

	@Override
	public SchedulerTestDetail mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SchedulerTestDetail objSchedulerTestDetail = new SchedulerTestDetail();
		objSchedulerTestDetail.setNschedulertestcode(getInteger(arg0, "nschedulertestcode", arg1));
		objSchedulerTestDetail.setNschedulersubsamplecode(getInteger(arg0, "nschedulersubsamplecode", arg1));
		objSchedulerTestDetail.setNschedulersamplecode(getInteger(arg0, "nschedulersamplecode", arg1));
		objSchedulerTestDetail.setNtestgrouptestcode(getInteger(arg0, "ntestgrouptestcode", arg1));
		objSchedulerTestDetail.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		objSchedulerTestDetail.setNsectioncode(getInteger(arg0, "nsectioncode", arg1));
		objSchedulerTestDetail.setNmethodcode(getInteger(arg0, "nmethodcode", arg1));
		objSchedulerTestDetail.setNinstrumentcatcode(getInteger(arg0, "ninstrumentcatcode", arg1));
		objSchedulerTestDetail.setNchecklistversioncode(getInteger(arg0, "nchecklistversioncode", arg1));
		objSchedulerTestDetail.setNtestrepeatno(getShort(arg0, "ntestrepeatno", arg1));
		objSchedulerTestDetail.setNtestretestno(getShort(arg0, "ntestretestno", arg1));
		objSchedulerTestDetail.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objSchedulerTestDetail.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objSchedulerTestDetail.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objSchedulerTestDetail.setNtransdatetimezonecode(getShort(arg0, "ntransdatetimezonecode", arg1));
		objSchedulerTestDetail.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSchedulerTestDetail.setNstatus(getShort(arg0, "nstatus", arg1));
		objSchedulerTestDetail.setNrepeatcountno(getShort(arg0, "nrepeatcountno", arg1));
		objSchedulerTestDetail.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objSchedulerTestDetail.setSsectionname(getString(arg0, "ssectionname", arg1));
		objSchedulerTestDetail.setSmethodname(getString(arg0, "smethodname", arg1));

		return objSchedulerTestDetail;
	}

}
