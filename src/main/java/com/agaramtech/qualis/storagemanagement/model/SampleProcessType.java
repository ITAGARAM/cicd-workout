package com.agaramtech.qualis.storagemanagement.model;

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

@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = false)  
@Entity
@Table(name = "sampleprocesstype")
@Data
public class SampleProcessType extends CustomizedResultsetRowMapper<SampleProcessType> implements Serializable, RowMapper<SampleProcessType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsampleprocesstypecode")
	private int nsampleprocesstypecode;

	@Column(name = "nprojecttypecode")
	private int nprojecttypecode;

	@Column(name = "nsamplecollectiontypecode")
	private int nsamplecollectiontypecode;

	@Column(name = "ncollectiontubetypecode")
	private int ncollectiontubetypecode;

	@Column(name = "nprocesstypecode")
	private int nprocesstypecode;

	@Column(name = "nprocesstime")
	private int nprocesstime;

	@Column(name = "nprocessperiodtime")
	private int nprocessperiodtime;

	@Column(name = "ngracetime")
	private int ngracetime;

	@Column(name = "ngraceperiodtime")
	private int ngraceperiodtime;

	@Column(name = "nexecutionorder")
	private int nexecutionorder;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@Column(name = "dmodifieddate", nullable= false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sprojecttypename;
	@Transient
	private transient int nprojectcode;
	@Transient
	private transient int nproductsamplecode;
	@Transient
	private transient String sproductname;
	@Transient
	private transient int ncollectiontubecode;
	@Transient
	private transient String stubename;
	@Transient
	private transient int nprocesscode;
	@Transient
	private transient String sprocesstypename;
	@Transient
	private transient String ngracetimeresult;
	@Transient
	private transient String nprocesstimeresult;
	@Transient
	private transient int nperiodcode;

	@Override
	public SampleProcessType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SampleProcessType objSampleProcessType = new SampleProcessType();

		objSampleProcessType.setNsampleprocesstypecode(getInteger(arg0, "nsampleprocesstypecode", arg1));
		objSampleProcessType.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objSampleProcessType.setNsamplecollectiontypecode(getInteger(arg0, "nsamplecollectiontypecode", arg1));
		objSampleProcessType.setNcollectiontubetypecode(getInteger(arg0, "ncollectiontubetypecode", arg1));
		objSampleProcessType.setNprocesstypecode(getInteger(arg0, "nprocesstypecode", arg1));
		objSampleProcessType.setNprocesstime(getInteger(arg0, "nprocesstime", arg1));
		objSampleProcessType.setNprocessperiodtime(getInteger(arg0, "nprocessperiodtime", arg1));
		objSampleProcessType.setNgracetime(getInteger(arg0, "ngracetime", arg1));
		objSampleProcessType.setNgraceperiodtime(getInteger(arg0, "ngraceperiodtime", arg1));
		objSampleProcessType.setNexecutionorder(getInteger(arg0, "nexecutionorder", arg1));
		objSampleProcessType.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objSampleProcessType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleProcessType.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleProcessType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleProcessType.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		objSampleProcessType.setSprocesstypename(getString(arg0, "sprocesstypename", arg1));
		objSampleProcessType.setSproductname(getString(arg0, "sproductname", arg1));
		objSampleProcessType.setStubename(getString(arg0, "stubename", arg1));
		objSampleProcessType.setNprojectcode(getInteger(arg0, "nprojectcode", arg1));
		objSampleProcessType.setNproductsamplecode(getInteger(arg0, "nproductsamplecode", arg1));
		objSampleProcessType.setSproductname(getString(arg0, "sproductname", arg1));
		objSampleProcessType.setNcollectiontubecode(getInteger(arg0, "ncollectiontubecode", arg1));
		objSampleProcessType.setNprocesscode(getInteger(arg0, "nprocesscode", arg1));
		objSampleProcessType.setNgracetimeresult(getString(arg0, "ngracetimeresult", arg1));
		objSampleProcessType.setNprocesstimeresult(getString(arg0, "nprocesstimeresult", arg1));
		objSampleProcessType.setNperiodcode(getInteger(arg0, "nperiodcode", arg1));
		return objSampleProcessType;
	}
}
