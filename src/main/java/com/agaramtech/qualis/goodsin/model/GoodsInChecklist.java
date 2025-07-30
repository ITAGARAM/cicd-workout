package com.agaramtech.qualis.goodsin.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "goodsinchecklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GoodsInChecklist extends CustomizedResultsetRowMapper<GoodsInChecklist> implements Serializable,RowMapper<GoodsInChecklist>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ngoodsincode",nullable =false)
	private int ngoodsincode;
	@Column(name="nchecklistversioncode",nullable =false)
	private int nchecklistversioncode;	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;	
	@ColumnDefault("-1")
	@Column(name="nsitecode",nullable =false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name="nstatus",nullable =false)
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private  transient int nchecklistqbcode;

	@Override
	public GoodsInChecklist mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final GoodsInChecklist objGoodsInChecklist =new GoodsInChecklist();
		objGoodsInChecklist.setNgoodsincode(getInteger(arg0,"ngoodsincode",arg1));
		objGoodsInChecklist.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objGoodsInChecklist.setJsondata(unescapeString(getJsonObject(arg0, "jsondata",arg1)));
		objGoodsInChecklist.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objGoodsInChecklist.setNstatus(getShort(arg0,"nstatus",arg1));
		objGoodsInChecklist.setNchecklistqbcode(getInteger(arg0,"nchecklistqbcode",arg1));
		return objGoodsInChecklist;
	}
	
	

}
