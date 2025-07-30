package com.agaramtech.qualis.global;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.ObjectMapper;


public class CustomizedResultsetRowMapper<T extends Object> {
	
    private final String prefix = "";
	protected Set<String> availableColumns;
	protected ResultSet resultSet;

	public void init(ResultSet resultSet) throws SQLException {
		this.resultSet = resultSet;
		availableColumns = new HashSet<String>();
		ResultSetMetaData meta = resultSet.getMetaData();
		for (int i = 1, n = meta.getColumnCount() + 1; i < n; i++){
			availableColumns.add(meta.getColumnName(i).toLowerCase());
		}
	}

	public boolean column(ResultSet resultSet,String sName,int rowcount) throws SQLException {
			if(availableColumns==null) {
			availableColumns = new HashSet<String>();
			ResultSetMetaData meta = resultSet.getMetaData();
			for (int i = 1, n = meta.getColumnCount() + 1; i < n; i++){
				availableColumns.add(meta.getColumnName(i).toLowerCase());
			}
			}
		return (availableColumns == null?false:availableColumns.contains(sName.toLowerCase()));
	}

	public long getLong(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
			return resultSet.getLong(prefix + sName);
		else
			return 0l;
	}

	public String getString(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
			return resultSet.getString(prefix + sName);
		else
			return null;
	}

	public int getInteger(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
			return resultSet.getInt(prefix + sName);
		else
			return 0;
	}

	public double getDouble(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
			return resultSet.getDouble(prefix + sName);
		else
			return 0.0d;
	}

	public float getFloat(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
			return resultSet.getFloat(prefix + sName);
		else
			return 0.0f;
	}

	public boolean getBoolean(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
			return resultSet.getBoolean(prefix + sName);
		else
			return false;
	}

	public short getShort(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
			return resultSet.getShort(prefix + sName);
		else
			return 0;
	}

	public byte getByte(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
			return resultSet.getByte(prefix + sName);
		else
			return 0;
	}
	public Date getDate(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
		return resultSet.getTimestamp(prefix + sName);
		else
			return null;
	}
	public Instant getInstant (ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount)) {
			return resultSet.getTimestamp(sName) !=null?resultSet.getTimestamp(sName).toInstant():null;
		}
		else
			return null;
	}
	
	public Map<String,Object> getJsonObject (ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount)) {
			return  resultSet.getObject(prefix + sName) != null ? 
						new JSONObject(((PGobject) resultSet.getObject(prefix + sName)).getValue()).toMap() : null;
		}
		else
			return null;
	}
	
	public List<?> getJsonObjecttoList (ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount)) {
			if(resultSet.getObject(prefix + sName) != null) {
				JSONArray jsonArray = new JSONArray(((PGobject) resultSet.getObject(prefix + sName)).getValue()); 
				return  jsonArray.toList();
			}else {
				return null;
			}
		}
		else
			return null;
	}	
	
	
	
	public List<Object> getJSONArray (ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount)) {
			if(resultSet.getObject(prefix + sName) != null) {
				return  new JSONArray(((PGobject) resultSet.getObject(prefix + sName)).getValue()).toList();
			}else
				return null;
		}
		else
			return null;
	}

	public byte[] getByteArray(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
			return resultSet.getBytes(prefix + sName);
		else
			return null;
	}
	public char getCharacter(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount)){
			char[] cbuf = new char[1];
			try {
				resultSet.getCharacterStream(prefix + sName).read(cbuf);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return cbuf[0];
		}
		else{
			return '\u0000';
		}
	}

	public List<?> convertList(ResultSet resultSet) throws SQLException {    
		List list = new ArrayList<>();    
		ResultSetMetaData md = resultSet.getMetaData();    
		int columnCount = md.getColumnCount();     
		while (resultSet.next()) { 
			Map<String,Object> rowData = new TreeMap<>();    
			//Fill up 
			for (int i = 1; i <= columnCount; i++) {    
				rowData.put(md.getColumnName(i), resultSet.getObject(i));    
			}    
			list.add(rowData);    
		}    
		return list;    
	}    
	
	public Map<String,Object> unescapeString(Map<String,Object> obj)  {    
		ObjectMapper mapper = new ObjectMapper();
		JSONObject obj1=new JSONObject(obj);
		String value= StringEscapeUtils.unescapeJava(obj1.toString());
		Map<String, Object> objMap=null;
		try {
			 objMap = mapper.readValue(value, Map.class);
			return objMap;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return objMap;
	} 
	
	
	public long getBigInteger(ResultSet resultSet,String sName,int rowcount) throws SQLException {
		if (column(resultSet,prefix + sName,rowcount))
			return resultSet.getInt(prefix + sName);
		else
			return 0;
	}
}
