package com.wldst.ruder.module.database.jdbc;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.RowProcessor;

public class DBRowProcessor extends BasicRowProcessor
  implements RowProcessor
{
  public DBRowProcessor(BeanProcessor paramBeanProcessor)
  {
    super(paramBeanProcessor);
  }
}