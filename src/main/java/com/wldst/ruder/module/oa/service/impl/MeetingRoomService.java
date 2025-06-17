package com.wldst.ruder.module.oa.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.module.oa.service.IMeetingRoomService;
import com.wldst.ruder.util.CalendarUtil;
import com.wldst.ruder.util.MapTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.wldst.ruder.module.workflow.constant.BpmDo.idString;
import static com.wldst.ruder.util.MapTool.*;
import static com.wldst.ruder.util.MapTool.dateLongValue;

@Service("meetingRoomService")
public class MeetingRoomService implements IMeetingRoomService{
    private static final Logger logger=LoggerFactory.getLogger(MeetingRoomService.class);
    @Autowired
    private CrudNeo4jService repoService;
    @Autowired
    private ObjectService objectService;

    public String getTableHeader(int days){
        StringBuilder sb=new StringBuilder();

        Date date=new Date();
        for(int i=0; i<days; i++){
            Date d=CalendarUtil.getDateAfter(date, i);
            String itemDate=CalendarUtil.convertDateToString(d);
            sb.append("  <td class=\"title\">"+CalendarUtil.getWeek(itemDate));
            sb.append(" <br />"+itemDate.substring(5)+"</td>");
        }
        return sb.toString();
    }


    @Override
    public List queryMeetingInfos(){
        Date curDate=Calendar.getInstance().getTime();
        String startDateStr=CalendarUtil.getCurrentDate();
        Date endDate=CalendarUtil.getDateAfter(curDate, Integer.valueOf(repoService.getSettingBy("calendar_days")));
        String endDateStr=CalendarUtil.convertDateToString(endDate);
//        Map<String, Object> vo= newMap();
//        vo.put("poId", "MeetingInfo");

        // 查询自定义字段数据
//        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");
//        for(Map<String, Object> fi:fieldInfoList){
//            if(fi!=null&&!fi.isEmpty()&&MapTool.on(fi,"longCompare")){
//
//            }
//        }
        String cypher="match(m:MeetingInfo) where m.longTime>=$startTime and m.longTime<=$endTime return m";

        Map<String, Object> data=new HashMap<>();
        data.put("startTime", startDateStr);
        data.put("endTime", endDateStr);

       return repoService.query(cypher,data);

    }

    @Override
    public Map<String, Object> getMeetManager(String userId){
        return repoService.getAttMapBy("userId", userId, "MeetManager");
    }


    @Override
    public int addManager(String userId, String username, String useraccount, String dept){
        Map<String, Object> data=new HashMap<>();
        data.put("userId", userId);
        data.put("userName", username);
        data.put("userAccount", useraccount);
        data.put("dept", dept);

        List<Map<String, Object>> meetManager=repoService.queryBy(data, "MeetManager");
        repoService.save(data, "MeetManager");
        if(id(data)!=null){
            return 200;
        }
        return 999;
    }

    @Override
    public Map<String, String> saveImportMeeting(Map<String, Object> vomap){
        Map<String, Object> dd=new HashMap<>();
        String ishelp=string(vomap, "ishelp");
        String username=string(vomap, "username");
        String meetingdate=string(vomap, "meetingdate");
        String participantsLeader=string(vomap, "participantsLeader");
        String enddate=string(vomap, "enddate");
        String meetingType=string(vomap, "type");
        String meetingtitle=string(vomap, "meetingtitle");
        String roomId=string(vomap, "roomId");

        dd.put("isHelp", ishelp);
        dd.put("meetingDate", meetingdate);
        dd.put("participantsLeader", participantsLeader);
        String leader="";
        if(participantsLeader.contains("领导班子")){
            leader=string(vomap, "leader");
            if(leader!=null){
                dd.put("participantsLeader", leader);
            }else{
                Map<String, String> map=new HashMap<String, String>();
                map.put("error", "必填领导");
                return map;
            }
        }
        dd.put("startTime", meetingdate);
        dd.put("endTime", enddate);

        dd.put("type", meetingType);
        dd.put("title", meetingtitle);
        dd.put("roomId", roomId);
        String userid=string(vomap, "userid");
        dd.put("userId", userid);
        dd.put("dept", string(vomap, "deptname"));
        dd.put("userName", string(vomap, "username"));
        dd.put("meetingnum", string(vomap, "meetingnum"));
        String apm=string(vomap, "apm");
        dd.put("apm", Integer.valueOf(apm)+"");
        dd.put("wuzi", string(vomap, "wuzi"));
        if(dd.get("wuzi")==null){
            dd.put("wuzi", null);
        }
        if(dd.get("type")==null){
            dd.put("type", null);
        }


        String[] split=enddate.substring(11).split(":");
        String endHour=split[0];
        Integer endMinute=Integer.valueOf(split[1]);
        // meetingDate
        String startHour=meetingdate.substring(11).split(":")[0];
        if(Integer.valueOf(startHour)!=Integer.valueOf(apm)){
            dd.put("apm", startHour);
        }
        // 碰撞检测，已经存在的数据，不可以进行新增。
        Map<String, Object> keyMap=new HashMap<>();
        keyMap.put("meetingDate", meetingdate);
        keyMap.put("roomId", roomId);
        keyMap.put("apm", startHour);
        Map<String, Object> meetingBooked=null;
        List<Map<String, Object>> meetingInfo=repoService.listDataByLabel("MeetingInfo", keyMap);
        if(!meetingInfo.isEmpty()){
            meetingBooked=meetingInfo.get(0);
        }
        String cnt=null;

        if(Integer.valueOf(endHour)>Integer.valueOf(apm)){

            for(int i=Integer.valueOf(startHour); i<=Integer.valueOf(endHour); i++){
                keyMap.put("apm", i+"");
                meetingInfo=repoService.listDataByLabel("MeetingInfo", keyMap);
                if(meetingInfo!=null&&!meetingInfo.isEmpty()){
                    Map<String, String> map=new HashMap<String, String>();
                    map.put("failed", meetingdate.substring(0, 10)+" "+i+"点，已被"+meetingBooked.get("MUSERNAME")
                            +"("+meetingBooked.get("USERDEPT")+")"+"预定,会议内容:"+meetingBooked.get("MCONTENT"));
                    return map;
                }
            }
            String rootMeetId=null;
            for(int i=Integer.valueOf(startHour); i<=Integer.valueOf(endHour); i++){
                if(endMinute<=0&&i==Integer.valueOf(endHour)){
                    continue;
                }
                dd.put("apm", i+"");

                repoService.save(dd, "MeetingInfo");
                cnt=idString(dd);
                if(i==Integer.valueOf(startHour)){
                    rootMeetId=cnt;
                }
                if(rootMeetId!=null){
                    dd.put("startId", rootMeetId);
                }
            }
        }else{
            if(meetingBooked!=null){
                Map<String, String> map=new HashMap<String, String>();
                map.put("failed", meetingdate.substring(0, 10)+" "+apm+"点，已被"+meetingBooked.get("MUSERNAME")
                        +"("+meetingBooked.get("dept")+")"+"预定,会议内容:"+meetingBooked.get("MCONTENT"));
                return map;
            }
            repoService.save(dd, "MeetingInfo");
            cnt=idString(dd);
        }


        // 如果更新成功就发消息给办公室的成员
        if(cnt!=null){
            String help="";
            if(("true").equals(ishelp)){
                help="需要";
            }else{
                help="不需要";
            }
            Map<String, Object> list=repoService.getNodeMapById(roomId);
            String roomname=(String) list.get("name");
            Map<String, String> mapWay=new HashMap<>();
            mapWay.put("01", "本地");
            mapWay.put("02", "I国网");
            mapWay.put("03", "亿联");

            String context=username+"申请"+meetingdate+"到"+enddate+"(会议形式："+mapWay.get(meetingType)+"-"
                    +participantsLeader
                    +"),在"
                    +roomname+"开《"+meetingtitle+"》，领导班子："+leader+",请注意查看。";
            // 发送一条微信给用户
            String users=repoService.getSettingBy("meeting_message_send_user");
            if(users!=null){
                String[] users_arr=users.split(",");
                logger.info(context+users);

                // String todoId = GlobalConfig.getProperty("meeting_id");
                // if (GlobalConfig.getProperty("dev_model").startsWith("igw")) {
                String todoId=repoService.getSettingBy("MsgSendAppTodoId");
                // }

                if("领导班子".equals(participantsLeader)){
                    for(int i=0; i<users_arr.length; i++){
//					msgSendService.sendMsg(users_arr[i], context, todoId);
                    }
                }
            }

            String screenUser=repoService.getSettingBy("meeting_screen_user");
            if(screenUser!=null){
                logger.info("screenUser=============:"+screenUser);
                if("02".equals(meetingType)||"03".equals(meetingType)){
                    logger.info(screenUser+"==========:"+meetingType+"==========:"+context);
                    String[] users_arrx=screenUser.split(",");
                    for(String ui : users_arrx){
//			    msgSendService.sendMsg(ui, context, todoId);
                    }
                }
            }

        }
        Map<String, String> map=new HashMap<String, String>();
        map.put("success", "success");
        return map;
    }

    @Override
    public List<Map<String, Object>> getImportMeetingList(){
        Map<String, Object> keyMap=new HashMap<>();
        keyMap.put("ishelp", "1");
        return repoService.queryBy(keyMap, "MeetingInfo");
    }


}
