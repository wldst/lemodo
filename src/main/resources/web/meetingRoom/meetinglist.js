//需要进行判断的，设置为公共变量

//入口
$(function(){
	init();
});
//数据初始化
function init(){
	ajaxRequest( basePath + '/cruder/meetingInfo/search',{}, 'POST', '', resultFunc);
}
//回调函数
function resultFunc(result){
	if(result.length ==0){
		var html = '<a class="weui-cell weui-cell_access" href="javascript:;"> '+
			'<div class="weui-cell__bd"> '+
		    '<p style="color: red;">还没有重要会议申请</p>'+
			'</div></a>';
		$("#meetinglist").html(html);
	}else {
		var content = "";
		for(var i =0 ;i<result.length;i++){
			var a_html_start = ' <a class="weui-cell weui-cell_access" href="javascript:showDetails('+result[i].ID+')"> <div class="weui-cell__bd"> <p>';
			var a_html_end = '</p></div>  <div class="weui-cell__ft"> </div></a>';
			var a_content = "";
			a_content ='【'+ result[i].MDATE+'】'+result[i].USERDEPT+'-'+result[i].MUSERNAME;
			content=content+a_html_start+a_content+a_html_end;
		}
		$("#meetinglist").html(content);
	}
}
//展示会议明细
function showDetails(id){
	 window.location= basePath + "/meetingRoom/toimportmeetdetails?id="+id; 
}