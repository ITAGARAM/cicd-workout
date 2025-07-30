package com.agaramtech.qualis.stability.model;

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
@Table(name = "protocolhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProtocolHistory extends CustomizedResultsetRowMapper<ProtocolHistory> implements Serializable,RowMapper<ProtocolHistory>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="nprotocolhistorycode",nullable =false)private int nprotocolhistorycode;
	@Column(name="nprotocolversioncode",nullable =false)private int nprotocolversioncode;
	@Column(name="nprotocolcode",nullable =false)private int nprotocolcode;
	@Column(name="ntransactionstatus",nullable=false)private short ntransactionstatus;
	@Column(name="nuserrolecode",nullable=false)private int nuserrolecode;
	@Column(name="nusercode",nullable=false)private int nusercode;
	@Column(name="ndeputyusercode",nullable=false)private int ndeputyusercode;
	@Column(name="ndeputyuserrolecode",nullable=false)private int ndeputyuserrolecode;
	@Column(name = "dmodifieddate", nullable = false)private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode",nullable=false)private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus",nullable=false)private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
		
	@Transient
	private transient String stransdisplaystatus;
	
	public ProtocolHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		ProtocolHistory objProtocolHistory =new ProtocolHistory();
		
		objProtocolHistory.setNprotocolhistorycode(getInteger(arg0,"nprotocolhistorycode",arg1));
		objProtocolHistory.setNprotocolversioncode(getInteger(arg0,"nprotocolversioncode",arg1));
		objProtocolHistory.setNprotocolcode(getInteger(arg0,"nprotocolcode",arg1));	
		objProtocolHistory.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objProtocolHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objProtocolHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objProtocolHistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objProtocolHistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objProtocolHistory.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objProtocolHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objProtocolHistory.setNstatus(getShort(arg0,"nstatus",arg1));		
		objProtocolHistory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));	

		return objProtocolHistory;
	}

}
