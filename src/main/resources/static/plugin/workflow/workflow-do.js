var wflist = [
              {title:'月度需求申请', mark:'CPMS_DEMAND_PLAN', url:'../../pmis-cmp/demandPlanForm/toDemandPlanInfoMain.so'},
              {title:'外协人员进入', mark:'CPMS_RECOMMEND_APPLY', url:'../../pmis-cmp/flowRecommendApplyForm/toFlowRecommendApplyMain.so'},
              {title:'外协人员退出', mark:'CPMS_QUIT_APPLY', url:'../../pmis-cmp/cpmsQuitApplyForm/toCpmsQuitApplyMain.so'},
              {title:'月度结算申请', mark:'CPMS_MONTH_CLEARING', url:'../../pmis-cmp/clearingForm/toMonthClearingMain.so'},
              {title:'外协人员询价', mark:'CPMS_RECRUIT_INFO', url:'../../pmis-cmp/recruitEnquiryForm/toRecruitEnquiryMain.so'}
              ];

function createFrame(url) {
	return '<iframe name="mainFrame" scrolling="auto" frameborder="0"  src="' + url + '" style="width:100%;height:100%;"></iframe>';
}

function getWFurl(mark) {
	if (wflist && wflist.length > 0) {
		for (var i = 0 ; i < wflist.length ; i++) {
			if (mark == wflist[i].mark) {
				return wflist[i].url;
			}
		}
	}
	return null;
}

function showWFundoPanel(tabsID, contextPath, title, mark){
	var tabObj = $('#'+tabsID);
	var existsTab = tabObj.tabs('exists', title);
	if (existsTab) {
		tabObj.tabs('select', title);
	} else {
		var url = getWFurl(mark);
		if (url) {
			tabObj.tabs('add',{  
			    title:title,  
			    content: createFrame(contextPath + url),  
			    closable:true  
			});  
		} else {
			alert('未配置流程待办界面');
		}
	}
}