package com.wldst.ruder.module.encode;

public abstract class BaseDigestPasswordEncoder extends BasePasswordEncoder {
    private boolean encodeHashAsBase64 = false;

    public void setEncodeHashAsBase64(boolean encodeHashAsBase64) {
	this.encodeHashAsBase64 = encodeHashAsBase64;
    }

    public boolean getEncodeHashAsBase64() {
	return this.encodeHashAsBase64;
    }
}
