package com.wldst.ruder.module.oa;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.OAuthRequired;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.exception.AuthException;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.oa.service.IMeetingRoomService;
import com.wldst.ruder.util.CalendarUtil;
import com.wldst.ruder.util.MapTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wldst.ruder.util.MapTool.*;

/**
 * 会议室预定控制类
 *
 * @author wendy
 * @date 2015-09-15
 */
@Controller
@RequestMapping("${server.context}/meetingRoom")
public class MeetingRoomController{

    @Autowired
    private IMeetingRoomService meetingRoomService;

    @Autowired
    private CrudNeo4jService repoService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private UserAdminService adminService;

    /**
     * 跳转到领导会议室表界面手机-
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/toMeetingRoomPage")
    @OAuthRequired
    public String toMeetingRoomPage(HttpServletRequest request, HttpServletResponse response, Model model){
        String settingBy=repoService.getSettingBy("calendar_days");
        int days=Integer.parseInt(settingBy);
        String dates="";
        Date date=new Date();
        for(int i=0; i<days; i++){
            Date d=CalendarUtil.getDateAfter(date, i);
            String itemDate=CalendarUtil.convertDateToString(d);
            dates+=itemDate;
            if(i<days-1){
                dates+=",";
            }
        }
        String tableHeader=meetingRoomService.getTableHeader(days);
        model.addAttribute("tableHeader", tableHeader);
        model.addAttribute("dates", dates);
        model.addAttribute("days", days);
        model.addAttribute("userId", request.getParameter("userId"));
        return "meetingRoom/main";
    }

    /**
     * 跳转到领导会议室表界面手机-
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/toMeetingRoomPageScreen")
    // @OAuthRequired
    public String toMeetingRoomPageScreen(HttpServletRequest request, HttpServletResponse response){
        return "meetingRoom/screen";
    }

    /**
     * 查询领导会议室信息
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryAllMeetingRoom")
    @ResponseBody
    public List queryAllMeetingRoom(HttpServletRequest request, HttpServletResponse response){
        return repoService.listAllByLabel("MeetingRoom");
    }

    /**
     * 查询会议室表用户
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryMeetingInfos")
    @ResponseBody
    public List queryMeetingInfos(HttpServletRequest request, HttpServletResponse response){
        return meetingRoomService.queryMeetingInfos();
    }

    /**
     * 查询会议室表用户
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryCurUserRole")
    @ResponseBody
    public Map<String, Object> queryCurUserRole(HttpServletRequest request, HttpServletResponse response){
        String userId=adminService.getCurrentAccount();

        HttpSession session=request.getSession();
        // 获取当前用户登录名
        session.setAttribute("UserId", userId);
        Map<String, Object> map=new HashMap<>();
        Map<String, Object> param=new HashMap<>();
        param.put("userId", userId);

        List<Map<String, Object>> meetManager1=repoService.queryBy(param, "MeetManager");
        if(meetManager1!=null&&!meetManager1.isEmpty()){
            Map<String, Object> meetManager=(Map<String, Object>) meetManager1.get(0);
            if(meetManager!=null&&!meetManager.isEmpty()){
                map.putAll(meetManager);
                map.put("permission", meetManager.get("roleCode"));
            }
        }
        return map;
    }

    /**
     * 保存会议室表信息
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/save", method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> save(@RequestBody Map<String, Object> vo,
                                    HttpServletRequest request,
                                    HttpServletResponse response){


        String userId=adminService.getCurrentAccount();
        Map<String, Object> md = null;
        try {
            md = adminService.checkAuth("MeetingInfo", "AddOperate", "添加");
        }catch(DefineException e){
            throw new RuntimeException(e);
        }catch(AuthException e){
            throw new RuntimeException(e);
        }

        Map<String, Object> datac=compat(vo, md);
        String labelPo=label(md);

        datac.put("meetingDate", string(vo, "mdate"));
        Map<String, Object> param = newMap();
        param.put("poId", labelPo);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(param, "Field");
        for(Map<String, Object> fi:fieldInfoList){
            if(fi!=null&&!fi.isEmpty()&&MapTool.on(fi,"isLongCompare")){
                datac.put("longTime",dateLongValue(datac,string(fi,"field")));
            }
        }
        Object ret=repoService.save(datac, "MeetingInfo");

        Map<String, Object> map=new HashMap<String, Object>();
        map.put("success", datac);
        if(id(datac)!=null){
            map.put("hints", "保存成功！");
        }else{
            map.put("hints", "保存失败！");
        }
        return map;
    }

    /**
     * 跳转到会议室管理员新增界面
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/toManagerAdd")
    // @OAuthRequired
    public String toManagerAdd(HttpServletRequest request, HttpServletResponse response, Model model){
        String userId=adminService.getCurrentAccount();
        model.addAttribute("userId", userId);
        return "meetingRoom/addManager";
    }

    /**
     * 跳转到重要会议新增界面
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/toimportmeet")
    // @OAuthRequired
    public String toImportMeet(HttpServletRequest request, HttpServletResponse response
            , Model model){
        HttpSession session=request.getSession();
        // 获取当前用户登录名
        String userId=adminService.getCurrentAccount();
        Map<String, Object> meetManager=repoService.getAttMapBy("userId", userId, "MeetManager");
        if(meetManager==null){
            return "error";
        }
        Map<String, Object> deptMap=new HashMap<>();
        deptMap.put("name", meetManager.get("deptName"));
        model.addAttribute("userdept", deptMap);

        Map<String, Object> user=new HashMap<>();
        user.put("name", meetManager.get("userName"));
        user.put("userId", userId);
        model.addAttribute("user", user);
        model.addAttribute("roomId", request.getParameter("roomId"));
        model.addAttribute("apm", request.getParameter("apm"));
        model.addAttribute("date", request.getParameter("date"));
        return "meetingRoom/importmeet";
    }

    /**
     * 重要会议保存
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/saveImport",method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, String> saveImport(HttpServletRequest request, HttpServletResponse response,@RequestBody JSONObject vo){

        Map<String, Object> md = null;
        try {
            md = adminService.checkAuth("MeetingInfo", "AddOperate", "添加");
        }catch(DefineException e){
            throw new RuntimeException(e);
        }catch(AuthException e){
            throw new RuntimeException(e);
        }

        Map<String, Object> datac=compat(vo, md);
        String labelPo=label(md);

        datac.put("meetingDate", string(vo, "mdate"));
        Map<String, Object> param = newMap();
        param.put("poId", labelPo);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(param, "Field");
        for(Map<String, Object> fi:fieldInfoList){
            if(fi!=null&&!fi.isEmpty()&&MapTool.on(fi,"isLongCompare")){
                datac.put("longTime",dateLongValue(datac,string(fi,"field")));
            }
        }

        Map<String, String> remap=this.meetingRoomService.saveImportMeeting(vo);
        return remap;
    }

    /**
     * 保存部门会议室管理员
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/addManager")
    @ResponseBody
    public Map<String, Object> addManager(HttpServletRequest request, HttpServletResponse response){
        String username=request.getParameter("username");
        String useraccount=request.getParameter("useraccount");
        String dept=request.getParameter("dept");
        String userId=request.getParameter("userId");
        int result=meetingRoomService.addManager(userId, username, useraccount, dept);
        Map<String, Object> map=new HashMap<String, Object>();
        map.put("success", result);
        if(result==200){
            map.put("hints", "保存成功！");
        }
        if(result==404){
            map.put("hints", "保存失败！管理员已存在");
        }
        if(result==999){
            map.put("hints", "保存失败！请联系管理员");
        }
        return map;
    }

    /**
     * 跳转到重要会议列表
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/toImportMeetingList")
    // @OAuthRequired
    public String toImportMeetingList(HttpServletRequest request, HttpServletResponse response){
        return "meetingRoom/meetinglist";
    }

    /**
     * 查询重要会议列表
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importmeetinglist")
    @ResponseBody
    public List<Map<String, Object>> getImportMeetingList(HttpServletRequest request, HttpServletResponse response){
        List<Map<String, Object>> list=this.meetingRoomService.getImportMeetingList();
        return list;
    }

    /**
     * 跳转到重要会议列表详情页面
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/toimportmeetdetails")
    // @OAuthRequired
    public ModelAndView toImportmeetDetails(HttpServletRequest request, HttpServletResponse response,
                                            ModelAndView modelAndView){
        String id=request.getParameter("id");
        Map<String, Object> detailsmap=repoService.getNodeMapById(id);
        modelAndView.addObject("detailsmap", detailsmap);
        modelAndView.setViewName("meetingRoom/meetingdetails");
        return modelAndView;
    }

    /**
     * 删除重要会议列表
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/removeImportmeetById")
    @ResponseBody
    public Map<String, Object> removeImportmeetById(HttpServletRequest request, HttpServletResponse response){
        String id=request.getParameter("id");
        Map<String, Object> map=repoService.getNodeMapById(id);
        repoService.removeById(Long.valueOf(id));
        return map;
    }
}
