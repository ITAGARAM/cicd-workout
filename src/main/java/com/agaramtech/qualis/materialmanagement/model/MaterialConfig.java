package com.agaramtech.qualis.materialmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

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
@Table(name = "materialconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MaterialConfig  extends CustomizedResultsetRowMapper<MaterialConfig> implements Serializable,RowMapper<MaterialConfig> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nmaterialconfigcode")
	private short nmaterialconfigcode;
	
	@Column(name = "nformcode")
	private short nformcode;
	
	@Column(name = "nmaterialtypecode")
	private short nmaterialtypecode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private List<?> jsondata;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Override
	public MaterialConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final MaterialConfig objMaterialConfig = new MaterialConfig();
		
		objMaterialConfig.setNmaterialconfigcode(getShort(arg0,"nmaterialconfigcode",arg1));
		objMaterialConfig.setNformcode(getShort(arg0,"nformcode",arg1));
		objMaterialConfig.setNmaterialtypecode(getShort(arg0,"nmaterialtypecode",arg1));
		objMaterialConfig.setJsondata(getJsonObjecttoList(arg0,"jsondata",arg1));
		objMaterialConfig.setNstatus(getShort(arg0,"nstatus",arg1));
		objMaterialConfig.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objMaterialConfig.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objMaterialConfig;
	}

}
