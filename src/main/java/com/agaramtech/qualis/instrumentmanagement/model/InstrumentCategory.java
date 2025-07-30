package com.agaramtech.qualis.instrumentmanagement.model;

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
 * This class is used to map the fields of 'instrumentcategory' table of the
 * Database.
 * 
 * @author ATE180
 * @version 9.0.0.1
 * @since 24-07-2020
 */

@Entity
@Table(name = "instrumentcategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstrumentCategory extends CustomizedResultsetRowMapper<InstrumentCategory> implements Serializable, RowMapper<InstrumentCategory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstrumentcatcode")
	private int ninstrumentcatcode;
	
	@ColumnDefault("1")
	@Column(name = "ninstrumenttypecode")
	private short ninstrumenttypecode =(short) Enumeration.InstrumentType.STANDARD_INSTRUMENT.getNinstrumenttypecode();

	@ColumnDefault("4")
	@Column(name = "ncalibrationreq", nullable = false)
	private short ncalibrationreq = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "ncategorybasedflow", nullable = false)
	private short ncategorybasedflow = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "sinstrumentcatname", length = 100, nullable = false)
	private String sinstrumentcatname;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nrequiredstorage", nullable = false)
	private short nrequiredstorage = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	

	@Transient
	private transient String scategorybasedflow;
	
	@Transient
	private transient String scalibrationrequired;
	
	@Transient
	private transient String sdisplaystatus;
	
	@Transient
	private transient int nproductcatcode;
	
	@Transient
	private transient String sproductcatname;
	
	@Transient
	private transient String srequiredstorage;
	
	@Transient
	private transient String sinstrumenttype;

	@Override
	public InstrumentCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final InstrumentCategory objInstrumentCategory = new InstrumentCategory();
		objInstrumentCategory.setNinstrumentcatcode(getInteger(arg0, "ninstrumentcatcode", arg1));
		objInstrumentCategory.setNcalibrationreq(getShort(arg0, "ncalibrationreq", arg1));
		objInstrumentCategory.setNcategorybasedflow(getShort(arg0, "ncategorybasedflow", arg1));
		objInstrumentCategory.setSinstrumentcatname(StringEscapeUtils.unescapeJava(getString(arg0, "sinstrumentcatname", arg1)));
		objInstrumentCategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objInstrumentCategory.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objInstrumentCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInstrumentCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		objInstrumentCategory.setScategorybasedflow(getString(arg0, "scategorybasedflow", arg1));
		objInstrumentCategory.setScalibrationrequired(getString(arg0, "scalibrationrequired", arg1));
		objInstrumentCategory.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objInstrumentCategory.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		objInstrumentCategory.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objInstrumentCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInstrumentCategory.setNrequiredstorage(getShort(arg0, "nrequiredstorage", arg1));
		objInstrumentCategory.setSrequiredstorage(getString(arg0, "srequiredstorage", arg1));
		objInstrumentCategory.setNinstrumenttypecode(getShort(arg0, "ninstrumenttypecode", arg1));
		objInstrumentCategory.setSinstrumenttype(getString(arg0, "sinstrumenttype", arg1));


		return objInstrumentCategory;
	}

}
