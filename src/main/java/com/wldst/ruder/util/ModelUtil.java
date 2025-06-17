package com.wldst.ruder.util;

import com.wldst.ruder.LemodoApplication;
import org.springframework.ui.Model;

import java.util.Map;

public class ModelUtil {

    public static void setKeyValue(Model model, Map<String, Object> po) {
        model.addAllAttributes(po);
    }


}
