package com.wldst.ruder.module.encode;


import org.springframework.dao.DataAccessException;

public interface PasswordEncoder {
    boolean isPasswordValid(String paramString1, String paramString2, Object paramObject)
	    throws DataAccessException, EncodingException;
  
    String encodePassword(String paramString, Object paramObject) throws DataAccessException, EncodingException;
  
    String decodePassword(String paramString, Object paramObject) throws DataAccessException, EncodingException;
}
