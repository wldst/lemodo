package com.wldst.ruder.module.goods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wldst.ruder.LemodoApplication;

/**
 * 市场：搜索分类市场
 * @author wldst
 *
 */
@RestController
@RequestMapping("${server.context}/market")
public class MarketControler {
    final static Logger logger = LoggerFactory.getLogger(MarketControler.class);

}
