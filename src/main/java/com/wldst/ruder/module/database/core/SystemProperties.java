package com.wldst.ruder.module.database.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SystemProperties
{
  private Properties pro;
  private static SystemProperties sp = new SystemProperties();

  public String getProperties(String paramString)
  {
    return this.pro.getProperty(paramString);
  }

  private SystemProperties()
  {
    InputStream localInputStream = super.getClass().getClassLoader().getResourceAsStream("system.properties");
    this.pro = new Properties();
    try
    {
      this.pro.load(localInputStream);
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }

  public static synchronized SystemProperties getInstance()
  {
    return sp;
  }
}