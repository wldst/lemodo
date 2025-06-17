package com.wldst.ruder.module.event;

import java.io.Serializable;
import java.util.Map;

public interface Message<T> extends Serializable {
    
    String getIdentifier();
 
 
    T getPayload();
 
    Class<T> getPayloadType();
 
}

