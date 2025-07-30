package com.agaramtech.qualis.goodsin.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "goodsinhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GoodsInHistory extends CustomizedResultsetRowMapper<GoodsInHistory> implements Serializable,RowMapper<GoodsInHistory>{

	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ngoodsinhistorycode",nullable =false)
	private int ngoodsinhistorycode;
	@Column(name="ngoodsincode",nullable =false)
	private int ngoodsincode;
	@Column(name="nusercode",nullable =false)
	private int nusercode;
	@Column(name="nuserrolecode",nullable =false)
	private int nuserrolecode;
	@Column(name="ndeputyusercode",nullable =false)
	private int ndeputyusercode;
	@Column(name="ndeputyuserrolecode",nullable =false)
	private int ndeputyuserrolecode;
	@ColumnDefault("8")
	@Column(name="ntransactionstatus",nullable =false)
	private short ntransactionstatus =(short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	@Column(name = "dtransactiondate")
	private Instant dtransactiondate;	
	@Column(name="ntransdatetimezonecode",nullable =false)
	private int ntransdatetimezonecode;
	@Column(name="noffsetdtransactiondate",nullable =false)
	private int noffsetdtransactiondate;
	@Column(name="scomments",nullable =false,length=255)
	private String scomments="";	
	@ColumnDefault("-1")
	@Column(name="nsitecode",nullable =false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name="nstatus",nullable =false)
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public GoodsInHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final GoodsInHistory objGoodsInHistory =new GoodsInHistory();
		objGoodsInHistory.setNgoodsinhistorycode(getInteger(arg0,"ngoodsinhistorycode",arg1));
		objGoodsInHistory.setNgoodsincode(getInteger(arg0,"ngoodsincode",arg1));
		objGoodsInHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objGoodsInHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objGoodsInHistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objGoodsInHistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objGoodsInHistory.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objGoodsInHistory.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
		objGoodsInHistory.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objGoodsInHistory.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objGoodsInHistory.setScomments(StringEscapeUtils.unescapeJava(getString(arg0,"scomments",arg1)));
		objGoodsInHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objGoodsInHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		
		return objGoodsInHistory;
	}
	
	
}
