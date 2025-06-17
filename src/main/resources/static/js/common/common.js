/**
 * 公共js类
 */

/**
 * 通用Ajax请求方法
 * @param url 链接地址
 * @param params 请求参数,没有参数时默认传[]
 * @param method 请求方式
 * @param tagetUrl 跳转地址, 没有跳转默认传空字符串
 * @param callBack 回掉函数，没有回掉函数默认传null
 * @returns
 */
function ajaxRequest(url, params, method, tagetUrl,callBack){
	//url校验
	if(url == '' || url == null){
		console.log("url不能为空");
		return;
	}
	//参数校验
	if(params == null){
		console.log("参数不能为空");
		return;
	}
	//请求方法校验
	if(method  == '' || method == null){
		console.log("请求类型不能为空");
		return;
	}
	$.ajax({
		url: url,
		data: JSON.stringify(params),
		type: method,
		async : false,
		dataType : "json",
		contentType : "application/json;charset=UTF-8",
		success: function(res) {
			if(tagetUrl != ''){
				window.location = tagetUrl;
			}else if(callBack != null){
				callBack(res);
			}
		},error:function(XMLHttpRequest, textStatus, errorThrown){
			if(XMLHttpRequest.status == 500){
				window.location = basePath + '/error';
			}
		}
	});
}