package com.agaramtech.qualis.quotation.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seqnoquotationmanagement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqnoQuotationManagement extends CustomizedResultsetRowMapper implements Serializable,RowMapper<SeqnoQuotationManagement> {
    	private static final long serialVersionUID = 1L;

		@Id
		@Column(name = "stablename", length = 50, nullable = false)
		private String stablename;

		@Column(name = "nsequenceno", nullable = false)
		private int nsequenceno;

		@Column(name = "nstatus", nullable = false)
		@ColumnDefault("1")
		private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		@Override
		public SeqnoQuotationManagement mapRow(ResultSet arg0, int arg1) throws SQLException {
			// TODO Auto-generated method stub
			SeqnoQuotationManagement objSeqnoQuotationManagement = new SeqnoQuotationManagement();
			objSeqnoQuotationManagement.setStablename(getString(arg0,"stablename",arg1));
			objSeqnoQuotationManagement.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
			objSeqnoQuotationManagement.setNstatus(getShort(arg0,"nstatus",arg1));
			return objSeqnoQuotationManagement;
		}

	}


