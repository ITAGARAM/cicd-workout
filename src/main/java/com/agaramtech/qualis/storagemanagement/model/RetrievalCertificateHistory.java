package com.agaramtech.qualis.storagemanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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

@Entity
@Table(name = "retrievalcertificatehistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RetrievalCertificateHistory extends CustomizedResultsetRowMapper<RetrievalCertificateHistory>
		implements Serializable, RowMapper<RetrievalCertificateHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nretrievalcertificatehistorycode")
	private int nretrievalcertificatehistorycode;

	@Column(name = "nretrievalcertificatecode", nullable = false)
	private int nretrievalcertificatecode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;

	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "dtransactiondate", nullable = false)
	private Date dtransactiondate;

	@Column(name = "noffsetdtransactiondate")
	private short noffsetdtransactiondate;

	@Column(name = "ntransdatetimezonecode")
	private int ntransdatetimezonecode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	public RetrievalCertificateHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final RetrievalCertificateHistory objRetrievalCertificateHistory = new RetrievalCertificateHistory();
		objRetrievalCertificateHistory.setNretrievalcertificatehistorycode(getInteger(arg0, "nretrievalcertificatehistorycode", arg1));
		objRetrievalCertificateHistory.setNretrievalcertificatecode(getInteger(arg0, "nretrievalcertificatecode", arg1));
		objRetrievalCertificateHistory.setNusercode(getInteger(arg0, "nusercode", arg1));
		objRetrievalCertificateHistory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objRetrievalCertificateHistory.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objRetrievalCertificateHistory.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		objRetrievalCertificateHistory.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objRetrievalCertificateHistory.setDtransactiondate(getDate(arg0, "dtransactiondate", arg1));
		objRetrievalCertificateHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		objRetrievalCertificateHistory.setNoffsetdtransactiondate(getShort(arg0, "noffsetdtransactiondate", arg1));
		objRetrievalCertificateHistory.setNtransdatetimezonecode(getInteger(arg0, "ntransdatetimezonecode", arg1));
		objRetrievalCertificateHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objRetrievalCertificateHistory;
	}

}
