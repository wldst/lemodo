package com.wldst.ruder.module.database.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

public class DBMetaListHandler extends AbstractListHandler<Set<String>>
{
  private ConvertToList convert;

  public DBMetaListHandler()
  {
    if (this.convert == null)
      this.convert = new ConvertToList();
  }

  public List<Set<String>> handle(ResultSet paramResultSet)
    throws SQLException
  {
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(handleRow(paramResultSet));
    return localArrayList;
  }

  protected Set<String> handleRow(ResultSet paramResultSet)
    throws SQLException
  {
    return this.convert.toList(paramResultSet);
  }

  static class ConvertToList extends BasicRowProcessor
  {
    public Set<String> toList(ResultSet paramResultSet)
      throws SQLException
    {
      HashSet localHashSet = new HashSet();
      ResultSetMetaData localResultSetMetaData = paramResultSet.getMetaData();
      int i = localResultSetMetaData.getColumnCount();
      for (int j = 1; j <= i; ++j)
        localHashSet.add(localResultSetMetaData.getColumnLabel(j));
      return localHashSet;
    }
  }
}