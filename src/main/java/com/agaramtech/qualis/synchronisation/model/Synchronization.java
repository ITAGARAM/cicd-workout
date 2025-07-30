package com.agaramtech.qualis.synchronisation.model;

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
@Table(name = "synchronization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class Synchronization extends CustomizedResultsetRowMapper<Synchronization>
implements Serializable, RowMapper<Synchronization> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsynchronizationcode")
	private int nsynchronizationcode;

	@ColumnDefault("-1")
	@Column(name = "ndestinationsitecode", nullable = false)
	private short ndestinationsitecode;

	@ColumnDefault("-1")
	@Column(name = "sdestinationsitecode", nullable = false)
	private String sdestinationsitecode;

	@ColumnDefault("-1")
	@Column(name = "ssourcesitecode", nullable = false)
	private String ssourcesitecode;

	@Column(name = "dlastsyncdatetime")
	private Instant dlastsyncdatetime;

	@Column(name = "dsyncdatetime")
	private Instant dsyncdatetime;

	@Column(name = "nsynctype")
	private short nsynctype;

	@Column(name = "nsyncstatus")
	private short nsyncstatus;

	@Column(name = "nusercode")
	private int nusercode;

	@Column(name = "nuserrole")
	private int nuserrole;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "ssyncid")
	private String ssyncid = "";

	@Column(name = "ndatewisesync", nullable = false)
	private short ndatewisesync = 3;

	@Column(name = "ncoareporthistorycode", nullable = false)
	private int ncoareporthistorycode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String ssyncdatetime;
	@Transient
	private transient String slastsyncdatetime;

	@Override
	public Synchronization mapRow(ResultSet arg0, int arg1) throws SQLException {

		final Synchronization synchronization = new Synchronization();

		synchronization.setNsynchronizationcode(getInteger(arg0, "nsynchronizationcode", arg1));
		synchronization.setDsyncdatetime(getInstant(arg0, "dsyncdatetime", arg1));
		synchronization.setNsynctype(getShort(arg0, "nsynctype", arg1));
		synchronization.setNsyncstatus(getShort(arg0, "nsyncstatus", arg1));
		synchronization.setNusercode(getInteger(arg0, "nusercode", arg1));
		synchronization.setNuserrole(getInteger(arg0, "nuserrole", arg1));
		synchronization.setNdestinationsitecode(getShort(arg0, "ndestinationsitecode", arg1));
		synchronization.setNsitecode(getShort(arg0, "nsitecode", arg1));
		synchronization.setNstatus(getShort(arg0, "nstatus", arg1));
		synchronization.setSsyncdatetime(getString(arg0, "ssyncdatetime", arg1));
		synchronization.setSlastsyncdatetime(getString(arg0, "slastsyncdatetime", arg1));
		synchronization.setSdestinationsitecode(getString(arg0, "sdestinationsitecode", arg1));
		synchronization.setSsourcesitecode(getString(arg0, "ssourcesitecode", arg1));
		synchronization.setSsyncid(getString(arg0, "ssyncid", arg1));
		synchronization.setNdatewisesync(getShort(arg0, "ndatewisesync", arg1));
		synchronization.setNcoareporthistorycode(getInteger(arg0, "ncoareporthistorycode", arg1));

		return synchronization;
	}

}
