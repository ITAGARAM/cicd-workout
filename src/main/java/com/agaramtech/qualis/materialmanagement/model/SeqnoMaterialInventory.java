package com.agaramtech.qualis.materialmanagement.model;


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
@Table(name="seqnomaterialinventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqnoMaterialInventory extends CustomizedResultsetRowMapper<SeqnoMaterialInventory> implements Serializable,RowMapper<SeqnoMaterialInventory> {


	private static final long serialVersionUID = 1L;
	
	
	@Id
    @Column(name = "stablename")
	private String stablename;
	
    @Column(name = "nsequenceno")
    private int  nsequenceno;
    
    @ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
    
	@Override
	public SeqnoMaterialInventory mapRow(ResultSet arg0, int arg1)throws SQLException 
	{
		final SeqnoMaterialInventory objSeqnoMaterialInventory = new SeqnoMaterialInventory();
		objSeqnoMaterialInventory.setStablename(getString(arg0,"stablename",arg1));
		objSeqnoMaterialInventory.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objSeqnoMaterialInventory.setNstatus(getShort(arg0,"nstatus",arg1));

		return objSeqnoMaterialInventory;
	}
    

    
}
