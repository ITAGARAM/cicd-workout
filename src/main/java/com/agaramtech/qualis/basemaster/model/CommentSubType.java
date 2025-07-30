package com.agaramtech.qualis.basemaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

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
@Table(name = "commentsubtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CommentSubType extends CustomizedResultsetRowMapper<CommentSubType> implements Serializable, RowMapper<CommentSubType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncommentsubtypecode")
	private short ncommentsubtypecode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@ColumnDefault("3")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = 3;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = -1;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String scommentsubtype;
	
	@Transient
	private transient String spredefinedenable;

	@Override
	public CommentSubType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final CommentSubType commentSubType = new CommentSubType();
		commentSubType.setNcommentsubtypecode(getShort(arg0, "ncommentsubtypecode", arg1));
		commentSubType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		commentSubType.setScommentsubtype(getString(arg0, "scommentsubtype", arg1));
		commentSubType.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		commentSubType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		commentSubType.setNstatus(getShort(arg0, "nstatus", arg1));
		commentSubType.setSpredefinedenable(getString(arg0, "spredefinedenable", arg1));
		commentSubType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return commentSubType;
	}
}
