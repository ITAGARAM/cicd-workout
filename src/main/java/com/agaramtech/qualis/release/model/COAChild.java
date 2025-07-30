
package com.agaramtech.qualis.release.model;

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

@Entity
@Table(name = "coachild")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class COAChild extends CustomizedResultsetRowMapper<COAChild> implements Serializable, RowMapper<COAChild> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncoachildcode")
	private int ncoachildcode;
	
	@ColumnDefault("-1")
	@Column(name = "ncoaparentcode", nullable = false)
	private int ncoaparentcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "npreregno", nullable = false)
	private int npreregno = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nusercode", nullable = false)
	private int nusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient int ncoareporthistorycode;

	@Override
	public COAChild mapRow(ResultSet arg0, int arg1) throws SQLException {

		COAChild objCOAChild = new COAChild();

		objCOAChild.setNcoachildcode(getInteger(arg0, "ncoachildcode", arg1));
		objCOAChild.setNcoaparentcode(getInteger(arg0, "ncoaparentcode", arg1));
		objCOAChild.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objCOAChild.setNtransactionsamplecode(getInteger(arg0, "ntransactionsamplecode", arg1));
		objCOAChild.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objCOAChild.setNusercode(getInteger(arg0, "nusercode", arg1));
		objCOAChild.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objCOAChild.setNstatus(getShort(arg0, "nstatus", arg1));
		objCOAChild.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objCOAChild.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objCOAChild.setNcoareporthistorycode(getInteger(arg0, "ncoareporthistorycode", arg1));

		return objCOAChild;
	}

}
