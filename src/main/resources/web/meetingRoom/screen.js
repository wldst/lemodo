/**
 * 全局公共变量
 */
var id = "";
$(function() {
	initPage();
});
/**
 * 初始化页面
 */
function initPage() {
	var rowCount = 0;
	$(".list_div").remove();
	var dateArray = dates.split(",");
	$.ajax({
	    url: basePath + '/meetingRoom/queryAllMeetingRoom',
			type : 'POST',
			async : true,
			data : {},
			dataType : "json",
			success : function(result) {
				var allHtml = "";
				$.each(result, function(i,item){
					rowCount = rowCount + 1;
					var roomId = item.ID;
					var roomCode = item.ROOM_CODE;
					var roomName = item.ROOM_NAME;
					var roomCapacity = item.ROOM_CAPACITY;
					var orderno = item.ORDERNO;
					var itemHtml = "";
					itemHtml += '<div class="list_div tablecss">';
					itemHtml += '   <table width="100%" border="0" cellspacing="0" cellpadding="0" class="tablecss">';
					itemHtml += '     <tr>';
					itemHtml += '        <td rowspan="2" class="ld color'+ (i+1>6?i-6:i+1)+'">'+ roomCode + '</td>';
					itemHtml += '        <td rowspan="2" class="ld">'+ roomName +'<br/><br/>('+ roomCapacity+ '人)'+'</td>';
//					itemHtml += '        <td rowspan="2" class="ld">'+ roomCapacity +'</td>';

					itemHtml += '        <td class="ld_td">上午</td>';
					for(var j = 0; j < days; j++){   
						var amId = roomId + "_" + dateArray[j] + "_am"; 
						itemHtml += '    <td class="rc" id="'+ amId +'"'; 
						itemHtml += ' ></td>';
					}
					itemHtml += '     </tr>';
					itemHtml += '     <tr>';
					itemHtml += '         <td class="ld_td">下午</td>';
					for(var m = 0; m < days; m++){   
						var pmId = roomId + "_" + dateArray[m] + "_pm"; 
						itemHtml += '     <td class="rc" id="' + pmId + '"';
						itemHtml += ' ></td>';
					}
					itemHtml += '  	  </tr>';
					itemHtml += ' 	 </table>';
					itemHtml += ' </div>';
					allHtml += itemHtml;
				});
				
				$("#hbody").append(allHtml);
				
				//窗口高度
				var height = $(window).height() - 200 - rowCount * 20;
				//计算单行高度
				var rowHeight = height/(rowCount * 2);
				//CSS设置高度
				$(".rc").css("height", rowHeight);
				$(".ld_td").css("height", rowHeight);

				$.ajax({
			    url: basePath + '/meetingRoom/queryMeetingInfos',
					type : 'POST',
					async : true,
					data : {},
					dataType : "json",
					success : function(result) {
						$.each(result, function(i,item){
							var id = item.ID;
							var roomId = item.ROOM_ID;
							var mdate = item.MDATE;
							var apm = item.APM;
							var content = item.MCONTENT;
							var orderno = item.ORDERNO;
							var tdID =  roomId + "_" + mdate + "_" + apm;
							
							$("#" + tdID).html(content);
							if(content != ""){
								$("#" + tdID).addClass("color" + orderno);
							}else{
								$("#" + tdID).removeClass("color" + orderno );
							}
						});
					}		    	
				});
			}		    	
		});
	

}