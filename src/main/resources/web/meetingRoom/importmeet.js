/**
 * 重要会议js
 */
//需要进行判断的，设置为公共变量
var participantsLeader ="";
var meetingdate ;
var enddate ;
var meetingtitle ;
var wuzi;
var leader;
var ishelp;
//入口
$(function(){
	$("#wuzi").select({
        title: "选择会议物资",
        multi: true,
        min: 1,
        items: ["会议视频系统", "电话会议系统", "会议笔记本", "摄影摄像", "矿泉水", "茶水","翻页笔","纸","笔","座牌"],
        onChange: function(d) {
          console.log(this, d);
        },
        onClose: function() {
          console.log("close");
        },
        onOpen: function() {
          console.log("open");
        },
      });
	var da = $("#date").val()+" "+$("#apm").val()+":00";
	 $("#meetingdate").datetimePicker({value:da });
	 $("#endDate").datetimePicker({value:da});
	 var MAX = 99, MIN = 2;
	 $('.weui-count__decrease').click(function (e) {
	        var $input = $(e.currentTarget).parent().find('.weui-count__number');
	        var number = parseInt($input.val() || "0") - 1
	        if (number < MIN) number = MIN;
	        $input.val(number)
	      })
	 $('.weui-count__increase').click(function (e) {
	        var $input = $(e.currentTarget).parent().find('.weui-count__number');
	        var number = parseInt($input.val() || "0") + 1
	        if (number > MAX) number = MAX;
	        $input.val(number)
	 })
	 $("#isdisplay").css("display","none");
	 $("#leaderDisplay").css("display","none");
	 
});


function leaderChange(obj){
	 var flag = $("#"+obj.id).is(":checked");
	 if(flag){
		 $("#leaderDisplay").css("display","block");
	 }else {
		 $("#leaderDisplay").css("display","none");
	 }
}
function changehelp(obj){
	 var flag = $("#"+obj.id).is(":checked");
	 if(flag){
		 $("#isdisplay").css("display","block");
	 }else {
		 $("#isdisplay").css("display","none");
	 }
}
/**
 * 保存会议内容
 * @returns
 */
function savemeetinginfo(){
	$("input:checkbox[name='participantsLeader']:checked").each(function(i, n) {
		
		if(i == 0){
			participantsLeader = $(this).val();
		}else{
			participantsLeader = participantsLeader+","+$(this).val();
		}
	});
	console.info(participantsLeader);
	 
	ishelp = $("#ishelp").is(":checked");
	var username = $("#username").val();
	var userId = $("#userId").val();
	var deptname = $("#deptname").val();
	 meetingdate =$("#meetingdate").val();
	 enddate =$("#endDate").val();
	 meetingtitle = $("#meetingtitle").val();
	 
	var meetType  = $("#type").val();
	var meetingnum = $("#meetingnum").val();
	var roomId = $("#roomId").val();
	var leader = $("#leader").val();
	
	var apm = $("#apm").val();
	
	var date = $("#date").val();
	if(meetingdate.indexOf(date)<0){
		$.toptip('会议预定时间只能是在所选时间段：'+date, 'error');
		return false;
	}
	 wuzi = $("#wuzi").val();
	 if(!check()){
		 return;
	 }
	 var params ={"apm":apm,"roomId":roomId,"participantsLeader":participantsLeader,"ishelp":ishelp,
			 "username":username,"userId":userId,"deptname":deptname,
			 "type":meetType,"meetingdate":meetingdate,"enddate":enddate,
			 "meetingtitle":meetingtitle,"meetingnum":meetingnum,"wuzi":wuzi
			 };
	 if(leader){
		 params ={"apm":apm,"roomId":roomId,"participantsLeader":participantsLeader,"ishelp":ishelp,
				 "username":username,"userId":userId,"deptname":deptname,
				 "type":meetType,"meetingdate":meetingdate,"enddate":enddate,
				 "meetingtitle":meetingtitle,"meetingnum":meetingnum,
				 "wuzi":wuzi,"leader":leader
				 };
	 }
	 ajaxRequest(basePath + '/meetingRoom/saveImport',params, 'POST', '', resultFunc);
}
function resultFunc(result){
	if(result['failed']){
		$.toast(result['failed']);
	}else{
		$.toast("申请成功", function() {
			 window.location= basePath + "/meetingRoom/toMeetingRoomPage"; 
		});
	}
	
}
function check(){
	if(participantsLeader == null || participantsLeader == ""){
		$.toptip('必须选择参会领导', 'error');
		return false;
	}
	if (meetingdate == null || meetingdate == "") {
		$.toptip('必须选择会议时间', 'error');
		return false;
	}
	if (enddate == null || enddate == "") {
		$.toptip('必须选择会议结束时间', 'error');
		return false;
	}
	var d = new Date(Date.parse(meetingdate.replace(/-/g, "/")));
	var d1 = new Date(Date.parse(enddate.replace(/-/g, "/")));
	var curDate = new Date();
	if (d <= curDate) {
		$.toptip('会议日期只能大于当前日期', 'error');
		return false;
	}
	if (d1 <= d) {
		$.toptip('结束时间只能大于会议日期', 'error');
		return false;
	}
	if (meetingtitle == null || meetingtitle == "") {
		$.toptip('必须填写会议名称', 'error');
		return false;
	}
	if(ishelp){//需要会议指导则需进行如下判断
		if(wuzi == null || wuzi == ""){
			$.toptip('必须选择物资', 'error');
			return false;
		}
	}
	return true;
}
