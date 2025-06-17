package com.wldst.ruder.module.encode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PwdPasswordEncoder {
    private static IPwdEncoder bspEncoder;

    public static IPwdEncoder getBspEncoder() {

	if (bspEncoder == null) {
	    bspEncoder = new PwdEncoder();
	}

	// throw new BspEncodingException("bspEncoder为空，请检查配置是否正确。");
	return bspEncoder;
    }
}
