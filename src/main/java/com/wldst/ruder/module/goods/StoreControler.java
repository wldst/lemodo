package com.wldst.ruder.module.goods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wldst.ruder.LemodoApplication;
/**
 * 商店
 * @author wldst
 *
 */
@RestController
@RequestMapping("${server.context}/store")
public class StoreControler {
    final static Logger logger = LoggerFactory.getLogger(StoreControler.class);

}
