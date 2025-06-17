package com.wldst.ruder.module.encode;

import java.io.Serializable;

import org.springframework.dao.DataAccessException;

public interface IPwdEncoder extends Serializable {
    String encodePassword(String paramString) throws DataAccessException, EncodingException;
    
    String decodePassword(String paramString);
    
    boolean isPasswordValid(String paramString1, String paramString2) throws DataAccessException, EncodingException;
  }
