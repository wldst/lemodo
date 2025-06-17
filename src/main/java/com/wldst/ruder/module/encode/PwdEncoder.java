package com.wldst.ruder.module.encode;

import org.springframework.dao.DataAccessException;

public class PwdEncoder implements IPwdEncoder {
    private static SystemWideSaltSource salt = new SystemWideSaltSource("1#2$3%4(5)6@7!poeeww$3%4(5)djjkkldss");

    private PasswordEncoder encoder = new Md5PasswordEncoder();

    @Override
    public String encodePassword(String rawPass) throws DataAccessException, EncodingException {
	return this.encoder.encodePassword(rawPass, salt);
    }

    @Override
    public String decodePassword(String encPass) {
	try {
	    return this.encoder.decodePassword(encPass, salt);
	} catch (DataAccessException | EncodingException e) {
	    e.printStackTrace();
	}
	return encPass;
    }

    @Override
    public boolean isPasswordValid(String encPass, String rawPass) throws DataAccessException, EncodingException {
	return this.encoder.isPasswordValid(encPass, rawPass, salt);
    }

    public static void main(String[] args) {
	try {
	    System.out.println("p:" + PwdPasswordEncoder.getBspEncoder().encodePassword("superadmin"));
	} catch (DataAccessException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (EncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public PasswordEncoder getEncoder() {
	return this.encoder;
    }

    public void setEncoder(PasswordEncoder encoder) {
	this.encoder = encoder;
    }
}
