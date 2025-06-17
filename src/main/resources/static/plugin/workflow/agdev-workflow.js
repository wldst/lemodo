

function wfdecision_popup_perform(contextName,bizDataID,currEmpID,taskComeDatetime,bizTabName,templateMark){
   var popupUri = contextName + "wfPerformForm/init.so";  
   popupUri = popupUri + "?bizDataID=" + bizDataID;
   popupUri = popupUri + "&currEmpID=" + currEmpID;
   popupUri = popupUri + "&taskComeDatetime=" + taskComeDatetime;
   popupUri = popupUri + "&bizTabName=" + bizTabName;
    popupUri = popupUri + "&templateMark=" + templateMark;
      popupUri = popupUri + "&time=" + new Date();
   var executeFlag = popup_window({url:popupUri,width:550,height:450,data:taskObj,model:true});
   return executeFlag;
}

function wfhistory_popup(contextName,bizDataID,currEmpID,taskComeDatetime,bizTabName){
   var popupUri = contextName + "agdev/workflow/wfhistory-display.do?method=init";  
   popupUri = popupUri + "&bizDataID=" + bizDataID;
   popupUri = popupUri + "&currEmpID=" + currEmpID;
   popupUri = popupUri + "&taskComeDatetime=" + taskComeDatetime;
   popupUri = popupUri + "&bizTabName=" + bizTabName;
      popupUri = popupUri + "&time=" + new Date();

   var executeFlag = popup_window({url:popupUri,width:550,height:300,data:taskObj,model:true});
   return executeFlag;
}

function wfexecute_popup(contextName,bizDataID,currEmpID,taskComeDatetime,bizTabName,triggerScript){
   var popupUri = contextName + "agdev/workflow/wfhistory-display.do?method=initExecute";  
   popupUri = popupUri + "&bizDataID=" + bizDataID;
   popupUri = popupUri + "&currEmpID=" + currEmpID;
   popupUri = popupUri + "&taskComeDatetime=" + taskComeDatetime;
   popupUri = popupUri + "&bizTabName=" + bizTabName;
      popupUri = popupUri + "&time=" + new Date();

   var executeFlag = popup_window(contextName,"流程操作",popupUri,650,400,false); 
   if (executeFlag){
      eval(triggerScript);
   }
   return executeFlag;
}

function wfhistory_popup_pic(contextName,bizDataID,currEmpID,taskComeDatetime,bizTabName,tempWfMark){
   var popupUri = contextName + "agdev/workflow/wfhistory-display.do?method=initPic";  
   popupUri = popupUri + "&bizDataID=" + bizDataID;
   popupUri = popupUri + "&currEmpID=" + currEmpID;
   popupUri = popupUri + "&taskComeDatetime=" + taskComeDatetime;
   popupUri = popupUri + "&bizTabName=" + bizTabName;
   popupUri = popupUri + "&templateMark=" + tempWfMark;
      popupUri = popupUri + "&time=" + new Date();
      
      popup_window({url:popupUri,id:"wfhistory_popup_pic_id",title:'打回',width:550,height:300,model:true,data:obj,clsself:true});
   //return executeFlag;
}

function wfdecision_popup_turnback(contextName,bizDataID,currEmpID,taskComeDatetime,bizTabName,templateMark){
   var popupUri = "/pmis/pmis-common/wfTurnbackForm/init.so";  
   popupUri = popupUri + "?bizDataID=" + bizDataID;
   popupUri = popupUri + "&currEmpID=" + currEmpID;
   popupUri = popupUri + "&taskComeDatetime=" + taskComeDatetime;
   popupUri = popupUri + "&bizTabName=" + bizTabName;
    popupUri = popupUri + "&templateMark=" + templateMark;
   popupUri = popupUri + "&time=" + new Date();
   
   popup_window({url:popupUri,id:"wfdecision_popup_turnback_id",title:'打回',width:550,height:300,model:true,clsself:true});
}

function wfdecision_popup_agree(contextName,bizDataID,currEmpID,taskComeDatetime,bizTabName,templateMark){
   var popupUri = "/pmis/pmis-common/wfAgreeForm/init.so";  
   popupUri = popupUri + "?bizDataID=" + bizDataID;
   popupUri = popupUri + "&currEmpID=" + currEmpID;
   popupUri = popupUri + "&taskComeDatetime=" + taskComeDatetime;
   popupUri = popupUri + "&bizTabName=" + bizTabName;
   popupUri = popupUri + "&templateMark=" + templateMark;
   popupUri = popupUri + "&time=" + new Date();
   
   popup_window({url:popupUri,id:"wfdecision_popup_agree_id",title:'同意',width:550,height:300,model:true,clsself:true});

}

function wfdecision_popup_disagree(contextName,bizDataID,currEmpID,taskComeDatetime,bizTabName,templateMark){
   var popupUri = "/pmis/pmis-common/wfDisAgreeForm/init.so";  
   popupUri = popupUri + "?bizDataID=" + bizDataID;
   popupUri = popupUri + "&currEmpID=" + currEmpID;
   popupUri = popupUri + "&taskComeDatetime=" + taskComeDatetime;
   popupUri = popupUri + "&bizTabName=" + bizTabName;
    popupUri = popupUri + "&templateMark=" + templateMark;
   popupUri = popupUri + "&time=" + new Date();
   
   popup_window({url:popupUri,id:"wfdecision_popup_disagree_id",title:'不同意',width:550,height:300,model:true,clsself:true});
}

function wfdecision_popup_reloop(contextName,bizDataID,currEmpID,taskComeDatetime,bizTabName,templateMark){
   var popupUri = "/pmis/pmis-common/wfTurnbackForm/init.so";  
   popupUri = popupUri + "?bizDataID=" + bizDataID;
   popupUri = popupUri + "&currEmpID=" + currEmpID;
   popupUri = popupUri + "&taskComeDatetime=" + taskComeDatetime;
   popupUri = popupUri + "&bizTabName=" + bizTabName;
    popupUri = popupUri + "&templateMark=" + templateMark;
      popupUri = popupUri + "&time=" + new Date();

      popup_window({url:popupUri,id:"wfdecision_popup_reloop_id",title:'跳转',width:550,height:300,model:true,clsself:true});
}

function wfdecision_popup_callback(contextName,bizDataID,currEmpID,taskComeDatetime,bizTabName,templateMark){
   if (confirm("是否收回当前任务？")){
          var params=$("#wfHistoryListForm").serialize();
	        $.ajax({
		        url: '${contextPath}/wfCallbackForm/callback.so', 
	        	type: 'POST', 
	        	data: params, 
	        	dataType: 'json', 
	        	success: function(result){
					alert(result.message);
	        	}
	        });
       }

	  
}