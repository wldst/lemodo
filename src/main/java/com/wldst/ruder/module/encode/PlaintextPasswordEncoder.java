package com.wldst.ruder.module.encode;

import org.springframework.dao.DataAccessException;

public class PlaintextPasswordEncoder extends BasePasswordEncoder {
    private boolean ignorePasswordCase = false;

    public void setIgnorePasswordCase(boolean ignorePasswordCase) {
	this.ignorePasswordCase = ignorePasswordCase;
    }

    public boolean isIgnorePasswordCase() {
	return this.ignorePasswordCase;
    }

    @Override
    public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
	String pass1 = (new StringBuilder(String.valueOf(encPass))).toString();
	String pass2;
	try {
	    pass2 = mergePasswordAndSalt(rawPass, salt, false);
	    if (!this.ignorePasswordCase)
		return pass1.equals(pass2);
	    return pass1.equalsIgnoreCase(pass2);
	} catch (EncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return false;

    }

    @Override
    public String encodePassword(String rawPass, Object salt) {
	try {
	    return mergePasswordAndSalt(rawPass, salt, true);
	} catch (EncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return rawPass;
    }

    public String[] obtainPasswordAndSalt(String password) {
	try {
	    return demergePasswordAndSalt(password);
	} catch (EncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public String decodePassword(String rawPass, Object salt) throws DataAccessException, EncodingException {
	throw new EncodingException("ddd");
    }
}
