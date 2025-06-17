/**
 * 
 */
package com.wldst.ruder.module.parse.handle;

import java.util.Map;

/**
 * @author wldst
 *
 */
public interface SentenceHandler {
    
    Object parse(String msg,Map<String,Object> context);

}
