package com.agaramtech.qualis.barcode.model;

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
 * This class is used to map the fields of 'Visit Number' table of the Database.
 * 
 * @author ATE235
 * @version 10.0.0.1
 * @since 28- June- 2023
 */
@Entity
@Table(name = "visitnumber")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VisitNumber extends CustomizedResultsetRowMapper<VisitNumber>
		implements Serializable, RowMapper<VisitNumber> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nvisitnumbercode")
	private int nvisitnumbercode=-1;

	@Column(name = "svisitnumber", length = 100, nullable = false)
	private String svisitnumber;

	@Column(name = "ncodelength")
	@ColumnDefault("1")
	private int ncodelength=-1;

	@Column(name = "scode", length = 2, nullable = false)
	private String scode;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nprojecttypecode")
	private int nprojecttypecode=-1;
    
	@Transient
	private transient String sprojecttypename="";
	@Transient
	private transient String smodifieddate="";
	@Transient
	private transient Boolean isvisitnumber;
	@Transient
	private transient Boolean iscode;

	@Override
	public VisitNumber mapRow(ResultSet arg0, int arg1) throws SQLException {
		VisitNumber objvisitNumber = new VisitNumber();
		objvisitNumber.setNvisitnumbercode(getInteger(arg0, "nvisitnumbercode", arg1));
		objvisitNumber.setSvisitnumber(StringEscapeUtils.unescapeJava(getString(arg0, "svisitnumber", arg1)));
		objvisitNumber.setNcodelength(getInteger(arg0, "ncodelength", arg1));
		objvisitNumber.setScode(StringEscapeUtils.unescapeJava(getString(arg0, "scode", arg1)));
		objvisitNumber.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objvisitNumber.setNstatus(getShort(arg0, "nstatus", arg1));
		objvisitNumber.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objvisitNumber.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objvisitNumber.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		objvisitNumber.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objvisitNumber.setIsvisitnumber(getBoolean(arg0, "isvisitnumber", arg1));
		objvisitNumber.setIscode(getBoolean(arg0, "iscode", arg1));
		return objvisitNumber;
	}

}
