
var act_bgc = "#94C4DE"; //活动单元格背景色
var act_fc = "#FFFFFF";//活动单元格的文字颜色
/**
 * 弹出窗口调用方法
 *
 * rootApp : 应用的根节点，一般取自jsp中定义的系统变量context
 * titleStr：弹出窗口显示的标题
 * popupUrl：弹出窗口中应该实现的画面的uri,全路径
 * winWidth：弹出窗口显示宽度
 * winHeight：弹出窗口显示高度
 * scrollbar：弹出窗口中是否显示滚动条
 */
function popup_window(rootApp, titleStr, popupUrl, winWidth, winHeight, scrollbar) {
	var today = new Date();
	var feacture = "dialogWidth:" + winWidth + "px; dialogHeight:" + winHeight + "px;";
	feacture = feacture + "directories:no; localtion:no; menubar:no; status:no;";
	feacture = feacture + "toolbar:no; scroll:no; resizeable:no; help:no";
	var popupWebUri = rootApp + "/includes/popup-frame.jsp?title=" + titleStr;	
	popupWebUri = popupWebUri + "&scrollbar=" + scrollbar;
	//popup_flag_session用于判断session是否过期，避免出现点击弹出窗口session过期显示完整的情况
	popupWebUri = popupWebUri + "&popup_flag_session=true";
	if (popupUrl.indexOf("?") > -1) {
		popupUrl = popupUrl + "&differUrl=" + today.getTime();
	} else {
		popupUrl = popupUrl + "?differUrl=" + today.getTime();
	}
	var returnVal = window.showModalDialog(popupWebUri, popupUrl, feacture);
	return returnVal;
}
function popup_window_resize(rootApp, titleStr, popupUrl, winWidth, winHeight, scrollbar) {
   //alert("popup_window_resize");
	var today = new Date();
	var feacture = "width=" + winWidth + "px,height=" + winHeight + "px,";
	feacture = feacture + "directories=no,localtion=no,menubar=no,status=no,";
	feacture = feacture + "toolbar=no,scrollbars=no,resizable=yes";
	var popupWebUri = rootApp + "/includes/labor-popup-frame.jsp?title=" + titleStr;
	popupWebUri = popupWebUri + "&scrollbar=" + scrollbar;
	if (popupUrl.indexOf("?") > -1) {
		popupUrl = popupUrl + "&differUrl=" + today.getTime();
	} else {
		popupUrl = popupUrl + "?differUrl=" + today.getTime();
	}
	var returnVal = window.open(popupUrl, titleStr, feacture);
	return returnVal;
}

//初始化页面中div的显示
function initShow() {
	sa = window.document.getElementById("search_div");
	if (sa == undefined) {
		return false;
	}
	searchAreaHeight = sa.clientHeight;
	sa.style.display = "none";
	var height = window.document.body.clientHeight;
	for (var i = 0; i < window.document.body.childNodes.length; i++) {
		height = height - window.document.body.childNodes[i].clientHeight;
	}
	window.document.getElementById("display_div").style.height = 1 + height;
}


//tr鼠标移入事件,主要用于list画面
function mouseOver(obj) {
	try {
		var the_obj = event.srcElement;
		if (the_obj == null) {
			return;
		}
		var i = 0;
		if (the_obj.tagName.toLowerCase() != "table") {
			var the_td = getElement(the_obj, "td");
			if (the_td == null) {
				return;
			}
			var the_tr = the_td.parentElement;
			if (the_tr == null) {
				return;
			}
			var the_table = obj;
			if (the_tr.rowIndex != 0) {
				for (i = 0; i < the_tr.cells.length; i++) {
					with (the_tr.cells[i]) {
						runtimeStyle.backgroundColor = act_bgc;
						runtimeStyle.color = act_fc;
					}
				}
			} else {
				for (i = 1; i < the_table.rows.length; i++) {
					with (the_table.rows[i].cells(the_td.cellIndex)) {
						runtimeStyle.backgroundColor = act_bgc;
						runtimeStyle.color = act_fc;
					}
				}
			}
		}
	}
	catch (e) {
	}
}

//tr鼠标事件移出,主要用于list画面
function mouseOut(obj) {
	try {
		var the_obj = event.srcElement;
		var i = 0;
		if (the_obj.tagName.toLowerCase() != "table") {
			var the_td = getElement(the_obj, "td");
			if (the_td == null) {
				return;
			}
			var the_tr = the_td.parentElement;
			if (the_tr == null) {
				return;
			}
			var the_table = obj;
			if (the_tr.rowIndex != 0) {
				for (i = 0; i < the_tr.cells.length; i++) {
					with (the_tr.cells[i]) {
						runtimeStyle.backgroundColor = "";
						runtimeStyle.color = "";
					}
				}
			} else {
				var the_table = obj;
				for (i = 0; i < the_table.rows.length; i++) {
					with (the_table.rows[i].cells(the_td.cellIndex)) {
						runtimeStyle.backgroundColor = "";
						runtimeStyle.color = "";
					}
				}
			}
		}
	}
	catch (e) {
	}
}

//得到对象
function getElement(the_ele, the_tag) {
	the_tag = the_tag.toLowerCase();
	if (the_ele.tagName.toLowerCase() == the_tag) {
		return the_ele;
	}
	while (the_ele = the_ele.offsetParent) {
		if (the_ele.tagName.toLowerCase() == the_tag) {
			return the_ele;
		}
	}
	return (null);
}

//全选或者解除全选
//formObj：需要全选或解除全选的checkbox所在form对象
//selectBoxObj：用于控制全选的checkBox输入域的对象
//checkBoxName：需要被控制全选的checkBox输入域的名称
function selectAllCheckBox(formObj, selectBoxObj, checkBoxName) {
   //alert("test");
	if (formObj == null || selectBoxObj == null) {
		return;
	}
	var checkBoxArray = formObj.getElementsByTagName("INPUT");
	if (selectBoxObj.checked) {
		for (i = 0; i < checkBoxArray.length; i++) {
			if (checkBoxArray[i].type == "checkbox" && checkBoxArray[i].name == checkBoxName) {
				checkBoxArray[i].checked = true;
			}
		}
	} else {
		for (i = 0; i < checkBoxArray.length; i++) {
			if (checkBoxArray[i].type == "checkbox" && checkBoxArray[i].name == checkBoxName) {
				checkBoxArray[i].checked = false;
			}
		}
	}
}

//得到指定表单对象中所选中的指定checkbox的数目
//formObj：需要全选或解除全选的checkbox所在form对象
//checkBoxName：需要被控制全选的checkBox输入域的名称
function getSelectedCheckBox(formObj, checkBoxObjName) {
	var checkedCount = 0;
	if (formObj == null || checkBoxObjName == "") {
		return checkedCount;
	}
	var checkBoxArray = formObj.getElementsByTagName("INPUT");
	if (checkBoxArray != null && checkBoxArray.length > 0) {
		for (i = 0; i < checkBoxArray.length; i++) {
			if (checkBoxArray[i].type == "checkbox" && checkBoxArray[i].name == checkBoxObjName && checkBoxArray[i].checked) {
				checkedCount++;
			}
		}
	}
	return checkedCount;
}

//加密;对应后台代码Base64Util
function base64encode(Str) { 
   var keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="; 
   Str = escape(Str); 
   var output = ""; 
   var chr1, chr2, chr3 = ""; 
   var enc1, enc2, enc3, enc4 = ""; 
   var i = 0; 
   do { 
      chr1 = Str.charCodeAt(i++); 
      chr2 = Str.charCodeAt(i++); 
      chr3 = Str.charCodeAt(i++); 
      enc1 = chr1 >> 2; 
      enc2 = ((chr1 & 3) << 4) | (chr2 >> 4); 
      enc3 = ((chr2 & 15) << 2) | (chr3 >> 6); 
      enc4 = chr3 & 63; 
      if (isNaN(chr2)) {enc3 = enc4 = 64;} else if (isNaN(chr3)) {enc4 = 64;} 
      output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2) + keyStr.charAt(enc3) + keyStr.charAt(enc4); 
      chr1 = chr2 = chr3 = ""; 
      enc1 = enc2 = enc3 = enc4 = ""; 
   } while (i < Str.length); 
   return output; 
} 

//获取指定元素的相对于版面的绝对位置H
function getElementTop(element){
   var actualTop = element.offsetTop;
   var current = element.offsetParent;
   
   while (current !== null){
      actualTop += current.offsetTop;
      current = current.offsetParent;
   }
   
   return actualTop;
}

//获取指定元素的相对于版面的绝对位置W
function getElementLeft(element){
   var actualLeft = element.offsetLeft;
   var current = element.offsetParent;
   
   while (current !== null){
      actualLeft += current.offsetLeft;
      current = current.offsetParent;
   }
   
   return actualLeft;
}

