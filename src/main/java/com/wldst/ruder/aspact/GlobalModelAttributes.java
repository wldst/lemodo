package com.wldst.ruder.aspact;

import com.wldst.ruder.LemodoApplication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("MODULE_NAME", LemodoApplication.MODULE_NAME);
    }
}
