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

/**
 * This class is used to map the fields of 'mappedtemplatefieldpropsmaterial' table of the Database.
 */


@Entity
@Table(name = "mappedtemplatefieldpropsmaterial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MappedTemplateFieldPropsMaterial extends CustomizedResultsetRowMapper<MappedTemplateFieldPropsMaterial>
implements Serializable, RowMapper<MappedTemplateFieldPropsMaterial>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nmappedtemplatefieldpropmaterialcode")
	private int nmappedtemplatefieldpropmaterialcode;
	@Column(name = "nmaterialconfigcode")
	private short nmaterialconfigcode;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private List<?> jsondata;
	
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Override
	public MappedTemplateFieldPropsMaterial mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final MappedTemplateFieldPropsMaterial objMappedTemplateFieldPropsMaterial = new MappedTemplateFieldPropsMaterial();

		objMappedTemplateFieldPropsMaterial.setNmappedtemplatefieldpropmaterialcode(getInteger(arg0, "nmaterialinventtranscode", arg1));
		objMappedTemplateFieldPropsMaterial.setNmaterialconfigcode(getShort(arg0, "ninventorytranscode", arg1));
		objMappedTemplateFieldPropsMaterial.setJsondata(getJsonObjecttoList(arg0, "jsondata", arg1));
		objMappedTemplateFieldPropsMaterial.setNstatus(getShort(arg0, "nstatus", arg1));
		objMappedTemplateFieldPropsMaterial.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objMappedTemplateFieldPropsMaterial.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objMappedTemplateFieldPropsMaterial;
	}

}
