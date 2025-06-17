/**
 * 全局公共变量
 */
var id = "";
var participantsLeader = "";
var permission =""
var timeSize=9;
$(function() {
	initPage();
});


/**
 * 初始化页面
 */
function initPage() {
	
	$.ajax({
	    url: basePath+'/meetingRoom/queryCurUserRole',
			type : 'POST',
			async : false,
			data : {},
			dataType : "json",
			success : function(result) {
				permission=result.permission;
				if(permission=="admin"){
					$("#addmanager").html('<button type="button" class="btn btn-primary"  id="manager" onclick="addmanager()">添加管理员</button>')
				}
			}		    	
		});		
	
	
	$(".list_div").remove();
	var timeData={};
	//timeData.put('9','上午9:00-10:00');
	timeData['10']='上午10:00-11:00';
	timeData['11']='上午11:00-12:00';
	timeData['12']='中午12:00-13:00';
	timeData['13']='下午13:00-14:00';
	timeData['14']='下午14:00-15:00';
	timeData['15']='下午15:00-16:00';
	timeData['16']='下午16:00-17:00';
	timeData['17']='下午17:00-17:30';
	var dateArray = dates.split(",");
	$.ajax({
	    url: basePath+'/cruder/MeetingRoom/listAll',
			type : 'POST',
			async : false,
			data : {},
			dataType : "json",
			success : function(result) {
				var allHtml = "";
				$.each(result.data, function(i,item){
					var roomId = item.id;
					var roomCode = item.code;
					var roomName = item.name;
					var roomCapacity = item.capacity;
					var orderno = item.ordernum;
					var roomNameCode=roomCode;
					if(roomCode!=roomName){
						roomNameCode=roomCode+roomName;
					}

					var itemHtml = "";
					var width1 = $(window).width() * 1.8;
					itemHtml += '<div class="list_div tablecss">';
					itemHtml += '   <table  border="0" cellspacing="0" cellpadding="0" style="table-layout: fixed;width:'+width1+'px">';
					itemHtml += '     <tr>';
					itemHtml += '        <td style="width: 15%" rowspan="'+timeSize+'" class="ld color'+ (i+1>6?i-6:i+1)+'">'+ roomName+'('+ roomCapacity+ '人)'+ '</td>';

					
					itemHtml += '        <td style="width: 15%" class="ld_td">上午9:00-10:00</td>';
					for(var j9 = 0; j9 < days; j9++){   
						var amId = roomId + "_" + dateArray[j9] + "_9"; 
						itemHtml += '    <td style="width: 10%" class="rc" id="'+ amId +'"';
						if(permission){
							itemHtml += '  onclick="openRCWindow(this)"';  
						}
						itemHtml += ' ></td>';
					}
					itemHtml += '  	  </tr>';
					for(var t=10;t<=17 ;t++ ) {
						itemHtml +=oneHour(roomId,dateArray,t,timeData[t]);
					}
					itemHtml += ' 	 </table>';
					itemHtml += ' </div>';
					allHtml += itemHtml;
				});
				
				$("#hbody").append(allHtml);
				
				var width = $(window).width() * 1.8;
				$(".tablecss").css("width", width);

				$.ajax({
			    url: basePath+'/meetingRoom/queryMeetingInfos',
					type : 'POST',
					async : true,
					data : {},
					dataType : "json",
					success : function(result) {
						var tempContent;
						var sHour =0;
						var eHour =0;
						var eMin =0;
						
						$.each(result, function(i,item){
							var id = item.ID;
							var roomId = item.ROOM_ID;
							var mdate =item.MDATE.substring(0,10);
							var startTime =item.MDATE.substring(10);
							sHour = parseInt(startTime.split(":")[0]);
							
							var endTime ="";
							if(item.ENDTIME&&item.ENDTIME.length>10){
								endTime =item.ENDTIME.substring(10);
								eHour = parseInt(endTime.split(":")[0]);
								eMin = parseInt(endTime.split(":")[1]);
							}
							if(sHour<9){
								sHour=9;
							}
							if(eHour>18){
								eHour=18;
							}
							var apm = item.APM;
							if(apm<9){
								apm=9;
							}
							var content = item.content;
							if (content==null){
								content="";
							}
							if(content.length>20){
								//content=content.substring(0,20)+"...";
								if(content.length<40){
									content=content.substring(0,20)+'<br>'+content.substring(20);
								}
								if(content.length>40){
									content=content.substring(0,20)+'<br>'+content.substring(20,40)+"...";
								}
							}
							
							var orderno = item.ordernum;
							var tdID =  roomId + "_" + mdate + "_" + apm;
							var participantsLeader = item.participantsLeader;
							var rowspan=1;
							debugger;
							if(eHour&&sHour&&eHour>sHour){
								if(apm==sHour){
										if(eHour>sHour){
											rowspan=eHour-sHour;
											if (eMin>0){
												rowspan+=1;
											}
										}
										if(rowspan>timeSize){
											rowspan=timeSize;
										}
									$("#" + tdID).html(item.USERDEPT+"_"+item.MUSERNAME+" 预定："+startTime+"-"+endTime+"<BR/>"+content);
									$("#" + tdID).attr("participantsLeader",participantsLeader);
									$("#" + tdID).attr("meetingid",id);
									$("#" + tdID).prop("rowspan",rowspan);
									if(content != ""){
										$("#" + tdID).addClass("color" + orderno);
									}else{
										$("#" + tdID).removeClass("color" + orderno );
									}
								}else if(apm>sHour&&apm<=eHour){
									$("#" + tdID).remove();
								}
							}else{
								$("#" + tdID).html(item.dept+"_"+item.name+" 预定："+mdate+"<BR/>"+content);
								$("#" + tdID).attr("participantsLeader",participantsLeader);
								$("#" + tdID).attr("meetingid",id);
								if(content != ""){
									$("#" + tdID).addClass("color" + orderno);
								}else{
									$("#" + tdID).removeClass("color" + orderno );
								}
							}
							
						});
					}		    	
				});
			}		    	
		});
	

}

function oneHour(roomId,dateArray,hour,name){
	var itemHtml = '   <tr>     <td class="ld_td">'+name+'</td>';
	for(var j = 0; j < days; j++){   
		var amId = roomId + "_" + dateArray[j] + "_"+hour; 
		itemHtml += '    <td class="rc" id="'+ amId +'"'; 
		if(permission){
			itemHtml += '  onclick="openRCWindow(this)"';  
		}
		itemHtml += ' ></td>';
	}
	itemHtml += '  	  </tr>';
	return itemHtml
}

/**
 * 打开会议室预定窗口
 * 
 * @param obj
 */
function openRCWindow(obj) {
	id = obj.id;
	var content = $("#" + id).html();
	var participantsLeader = $("#" + id).attr("participantsLeader");
	if(participantsLeader==''||participantsLeader==undefined){
		participantsLeader="";
	}
	var meetingid = $("#" + id).attr("meetingid");//会议ID
	if(content==''||content==undefined){
		if(permission=="admin"){
			$.confirm({
				  title: '会议室预定注意事项',
				  text: '领导班子、经营团队是否参会?',
				  onOK: function () {
					  var idArray = id.split("_");
					  window.location= basePath + "/meetingRoom/toimportmeet?roomId="+idArray[0]+"&date="+idArray[1]+"&apm="+idArray[2];
				  },
				  onCancel: function () {
						$('#myModal').modal();
						$("#RCCOMMENT").val(content);
				  }
				})
		}else if(permission=="manager"&&content==""){
			$.confirm({
				  title: '会议室预定注意事项',
				  text: '领导班子、经营团队是否参会?',
				  onOK: function () {
					  var idArray = id.split("_");
					  window.location= basePath + "/meetingRoom/toimportmeet?roomid="+idArray[0]+"&date="+idArray[1]+"&apm="+idArray[2]; 
				  },
				  onCancel: function () {
						$('#myModal').modal();
						$("#RCCOMMENT").val(content);
				  }
				})
		}
	}else{
		if(permission=="admin"){
			$.confirm({
				  title: "重要会议",
				  text: "已经申请"+participantsLeader+"会议,是否关闭?",
				  onOK: function () {
						$.ajax({
						    url: basePath + '/meetingRoom/removeImportmeetById',
								type : 'POST',
								async : false,
								data : {"id" : meetingid},
								dataType : "json",
								success : function(result) {
									$.toast("操作成功", function() {
										 window.location= basePath + "/meetingRoom/toMeetingRoomPage"; 
									});
								}
						});
				  }
				})
		}else {
			$.alert("已经申请"+participantsLeader+"会议");
		}
	}
}
/**
 * 保存方法
 */
function save() {
	var content = $("#RCCOMMENT").val();
	//var tdID =  roomId + "_" + mdate + "_" + apm;
	var idArray = id.split("_");
	var roomId = idArray[0];
	var mdate = idArray[1];
	var mdate = idArray[1];
	var apm = idArray[2];
//	var orderno = idArray[3];
	var params ={"roomId" : roomId, "mdate" : mdate, "apm" : apm, "content" : content,"curUserId":curUserId};
	$.ajax({
	    url: basePath+'/meetingRoom/save',
			type : 'POST',
			async : true,
			dataType : "json",
			data: JSON.stringify(params),
			contentType : "application/json;charset=UTF-8",
			beforeSend: function () {
		    	$(".btn").attr("disabled","disabled");
		    },
			success : function(result) {
				if(result.success){
					$("#" + id).html(content);
					if(content == null){
				//		$("#" + id).removeClass("color" + orderno );
					}else{
				//		$("#" + id).addClass("color" + orderno );
					}
					initPage();
				}
				$('#myModal').modal("hide");
			},
			complete: function(e, xhr, settings) {
				$(".btn").removeAttr("disabled");
		  	}
	});
	
}
/**
 * 关闭方法
 */
function closeModal() {
	$('#myModal').modal("hide");
}

function addmanager(){
	window.location= basePath+"/meetingRoom/toManagerAdd?id="+10000*Math.random();
}