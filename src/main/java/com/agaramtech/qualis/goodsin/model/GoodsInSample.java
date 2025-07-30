package com.agaramtech.qualis.goodsin.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "goodsinsample")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GoodsInSample extends CustomizedResultsetRowMapper<GoodsInSample> implements Serializable,RowMapper<GoodsInSample>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ngoodsinsamplecode",nullable=false)
	private int ngoodsinsamplecode;
	
	@Column(name="ngoodsincode",nullable=false)
	private int ngoodsincode;
	
	@Column(name="ndesigntemplatemappingcode",nullable=false)
	private int ndesigntemplatemappingcode;
	
	@Column(name="sexternalsampleid",nullable=false,length =100)
	private String sexternalsampleid;	
	
	@Column(name="nquantity",nullable=false)
	private int nquantity;
	
	@Column(name="nunitcode",nullable=false)
	private int nunitcode;
	
	@Column(name="scomments",nullable=false,length=255)
	private String scomments;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")private Map<String, Object> jsondata;
	
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")private Map<String, Object> jsonuidata;
	
	@ColumnDefault("-1")
	@Column(name="nsitecode",nullable=false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name="nstatus",nullable=false)
	private short nstatus  =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	

	@Override
	public GoodsInSample mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final GoodsInSample objGoodsInSample =new GoodsInSample();
		objGoodsInSample.setNgoodsinsamplecode(getInteger(arg0,"ngoodsinsamplecode",arg1));
		objGoodsInSample.setNgoodsincode(getInteger(arg0,"ngoodsincode",arg1));
		objGoodsInSample.setNdesigntemplatemappingcode(getInteger(arg0,"ndesigntemplatemappingcode",arg1));
		objGoodsInSample.setSexternalsampleid(StringEscapeUtils.unescapeJava(getString(arg0,"sexternalsampleid",arg1)));
		objGoodsInSample.setNquantity(getInteger(arg0,"nquantity",arg1));
		objGoodsInSample.setNunitcode(getInteger(arg0,"nunitcode",arg1));
		objGoodsInSample.setScomments(StringEscapeUtils.unescapeJava(getString(arg0,"scomments",arg1)));
		objGoodsInSample.setJsondata(unescapeString(getJsonObject(arg0, "jsondata",arg1)));
		objGoodsInSample.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata",arg1)));
		objGoodsInSample.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objGoodsInSample.setNstatus(getShort(arg0,"nstatus",arg1));
		
		return objGoodsInSample;
	}
	
	
}
