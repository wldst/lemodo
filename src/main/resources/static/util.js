/*var timeout = setTimeout(function() {  
	    window.top.location = '[(${MODULE_NAME})]/adminctrl/loginout';
	}, 1800000);*/
if(!String.prototype.startWith){
	String.prototype.startWith=function(str){    
  var reg=new RegExp("^"+str);    
  return reg.test(this);       
	} 
}

if(!String.prototype.endWith){
	String.prototype.endWith=function(str){    
	  var reg=new RegExp(str+"$");
	  return reg.test(this);
	}
}
let byteConvert;
if(!byteConvert){
	byteConvert = function(bytes) {
		if (isNaN(bytes)) {
			return '';
		}
		let symbols = ['bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
		let exp = Math.floor(Math.log(bytes)/Math.log(2));
		if (exp < 1) {
			exp = 0;
		}
		let i = Math.floor(exp / 10);
		bytes = bytes / Math.pow(2, 10 * i);

		if (bytes.toString().length > bytes.toFixed(2).toString().length) {
			bytes = bytes.toFixed(2);
		}
		return bytes + ' ' + symbols[i];
	};
}


var k=1024
function divK(data){
	var mb = parseInt(data)*k;
	return byteConvert(mb);
}

function FrameWH() {
    var h = $(window).height() -100;
    $("iframe").css("height",h+"px");
}

function FrameWH2() {
    var h = $(window).height()*0.9;
    var w = $(window).width()*0.95;
    $("iframe").css("height",h+"px");
    $("iframe").css("width",w+"px");
}

function FrameWH3(a) {
    var h = $(a).height()*0.98;
    var w = $(a).width()*0.98;
    $(a).find("iframe").css("height",h+"px");
     $(a).find("iframe").css("width",w+"px");
}

function tableInitDone(res, curr, count){
  if(dropDown){
	  if(dropDown.suite){
		  dropDown.suite();
	  }
  }
	$("table").css("width", "100%"); 
  fixRightTool(res, curr, count);
  
}
function fixRightTool(res, curr, count) {
	let maxWidth = 0;
	let fixedRight = $(".layui-table-fixed-r");
	var br=fixedRight
	.children(".layui-table-body")
	.children("table")
	.children("tbody")
	.children("tr");
	
	var hr=fixedRight
	.children(".layui-table-header")
	.children("table")
	.children("thead")
	.children("tr");
	//移除thead中原先的宽度样式
	hr.each(function () {
		$(this).children("th").children("div").removeClass();
		$(this).children("th").children("div").addClass("layui-table-cell");
	});
	
	//移除tbody中原先的宽度样式，并计算出最后所需宽度
	br.each(function () {
			$(this).children("td").children("div").removeClass();
			$(this).children("td").children("div").addClass("layui-table-cell");
			maxWidth = $(this).width();
		});

	//修改thead中该列各单元格的宽度
	hr.each(function () {
			$(this).children("th").children("div").width(maxWidth);
		});
	//修改tbody中该列各单元格的宽度
	br.each(function () {
		$(this).children("td").children("div").width(maxWidth);
	});

	//由于layui的table组件中 浮动并不是单个单元格真浮动，而是实际上是新加了一个浮动对象覆盖在原先的单元格上，所以如果不写如下代码，会造成被覆盖的那一层单元格没有被完全覆盖的bug
	hr.each(function () {
			$(this).children("th:last").children("div").width(maxWidth);
		});
	br.each(function () {
			$(this).children("td:last").children("div").width(maxWidth);
		});
		multRow(res, curr, count);
}


function multRow(res, curr, count){
            $(".layui-table-main tr").each(function (index, val) {
                $($(".layui-table-fixed-l .layui-table-body tbody tr")[index]).height($(val).height());
                $($(".layui-table-fixed-r .layui-table-body tbody tr")[index]).height($(val).height());
            })
}

//查询数据
function queryData(po,genurl){
	var data=null;
	 $.ajax({
	      type: "post",
	      url: genurl,
	        dataType : "json",
	        contentType : "application/json;charset=UTF-8",
	        data: JSON.stringify(po),
	        success: function (d) {
	           data=d.data;
	           var label=genurl.split('/')[3];
	            
	           for(var i in data){
				  data[i].label= label;
	        	  showNoteList(data[i]);
	           }
	      	},
	 		error:function (d) {
		    }
	    });
	 return data;
}
function showNoteList(data){
	layer.open({
	    type: 2,
	    title: data.name,
	    area: '250px',
	    skin: 'layui-layer-notepaper',
	    offset: 'rt',
	    anim: 6,
	    shade: false,
	    content: MODULE_NAME+'/desktop/notePaper/'+data.label+'/'+data.id,
	    //content: '<textarea class="layui-textarea notepaper">'+data.content+'</textarea><button id="">关闭</button><button id="">详情</button>',
	    success: function(a, b) {
	        $(a).find(".notepaper").on("change",
	        function() {
	            console.log($(this).val())
	        })
	    }
	});
}

function showMessage(data){
	layer.open({
	    type: 1,
	    title: data.name,
	    area: '250px',
	    skin: 'layui-layer-notepaper',
	    offset: 'rt',
	    anim: 6,
	    shade: false,
	    content: '<textarea class="layui-textarea notepaper">'+data.content+'</textarea>',
	    success: function(a, b) {
	        $(a).find(".notepaper").on("change",
	        function() {
	            console.log($(this).val())
	        })
	    }
	});
}
function showMsg(data){
	layer.open({
	    type: 1,
	    title: data.name,
	    area: '250px',
	    offset: 'rt',
	    anim: 6,
	    shade: false,
	    content: data.content,
	    success: function(a, b) {
	        $(a).find(".notepaper").on("change",
	        function() {
	            console.log($(this).val())
	        })
	    }
	});
}

function openCalendarWindow(){
		var width= '1400px';
		  layer.open({
		      type: 2,
		      anim: 0,
		      shade: 0,
		      title: "日程",
		      maxmin: true,
		      area: [1400+'px', 815+'px'],
		      btn:['关闭'],
		      yes:function(index,layero)
		      {
		          //index为当前层索引
		        layer.close(index)
		      },
		      cancel:function(){//右上角关闭毁回调
		      },
		      zIndex: layer.zIndex
		      ,success: function(layero, index){
		        layer.setTop(layero); 
		      },
		      //"/cd/layui/calendar/demo"
		      content: MODULE_NAME+"/layui/calendar"
		  });
	}
	
	function openUrl(data){
		var width= '90%';
		  layer.open({
		      type: 2,
		      anim: 1,
		      shade: 0,
		      title: data.apptitle+'->'+data.title,
		      maxmin: true,
		      area: [width, '95%'],
		      btn:['关闭'],
		      yes:function(index,layero)
		      {
		          //index为当前层索引
		        layer.close(index)
		      },
		      cancel:function(){//右上角关闭毁回调
		      close()
				 var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
		  		parent.layer.close(index); //再执行关闭
		      },
		      zIndex: layer.zIndex
		      ,success: function(layero, index){
		        layer.setTop(layero); 
		      },
		      content: data.url
		  });
	}
	
	
  
function openApp(appPath) {  
  var shell = new ActiveXObject("WScript.Shell");  
  var app = shell.Exec(appPath);  
}
var currentOpenWindow=null;
function openManage(name,url){
	var a={"title":name,"url":url}
	currentOpenWindow=name;
	var wp = window.top;
	while(wp.location.href.indexOf("/desktop")<0){
	wp=wp.top;	
	}
	wp.windowOpen(a)
	
	if(url.indexOf("/scene/")>0){
		var index = parent.layer.getFrameIndex(window.name); //获取当前窗口的索引
	parent.layer.close(index); //关闭当前窗口
		$(wp.document).find(".taskbar-app").each(function(a, b) {
        if ($(b).attr("title") != name) {
            $(b).remove();
            return
        }
    });
	}
}
function windowOpen(data){
    var h = true;
    if(currentOpenWindow&&currentOpenWindow==data.title){
			h = false;
            return
		}
    $(document).find(".taskbar-app").each(function(a, b) {
		
        if ($(b).attr("title") == data.title&&
        data.title.indexOf("上一")<0&&
        data.title.indexOf("下一")<0
        &&data.title.indexOf("返回主页")<0) {
            $(b).click();
            currentOpenWindow=data.title;
            h = false;
            return
        }
    });
    currentOpenWindow=data.title;
    if(!h) return;
    var i = $(".taskbar-app").length + 1;
    var maxcount = parseInt((layui.jquery(".desktop-taskbar").width() - 160) / 110);
    if (maxcount>5 &&i > maxcount) {
        layer.alert("请先关闭一些窗口！", {
            title: "官人休息下？",
            icon: 2,
            zIndex: layer.zIndex + 1
        });
        return
    }
    var w = $(".desktop-container").width() * 0.8,
    height =  $(".desktop-container").height() * 0.9;
    if(data.width){
		w=data.width
	}
	if(data.height){
		height=data.height
	}
    var k =  "taskbar-layui-layer" + i;
    var l = layer.open({
        type: 2,
        title: [data.title, 'background-color:#485664;color:#fff'],
        shadeClose: true,
        shade: false,
        maxmin: true,
        area: [w + 'px', height + 'px'],
        content: data.url,
        zIndex: layer.zIndex,
        skin: 'desktop-win-app',
		success:  function(layero, index){
			$("#" + k).removeClass("disabled");
			layer.setTop(layero)
			// 鼠标放上去时禁止移位
			layero.find('.layui-layer-title').css('cursor', 'move');
			// 拖拽事件
			layero.draggable({
				// containment: 'window', // 限制在窗口内拖动
				scroll: false, // 禁止滚动条
				start: function(){
					layero.css('z-index', 9999); // 提高层级
				},
				drag: function(event){
					var offset = $(this).offset();
					if (offset.left < 0) {
						$(this).css('left', 0);
					} else if (offset.top < 0) {
						$(this).css('top', 0);
					} else if (offset.left + $(this).outerWidth() > $(window).width()) {
						$(this).css('left', $(window).width() - 50);
					} else if (offset.top + $(this).outerHeight() > $(window).height()) {
						$(this).css('top', $(window).height() - 50);
					}
				},
				stop: function(){
					layero.css('z-index', 1800); // 恢复层级
				}
			});
		},

        min: function(c, d) {
            $(c).hide();
            $("#" + k).removeClass("taskbar-app-on");
            var e = [];
            $(document).find(".layui-layer-iframe:visible").each(function(a, b) {
                e.push($(b).css("z-index"))
            });
            if (e.length < 1) return false;
            var f = e.sort().pop();
            $(document).find(".layui-layer-iframe:visible").each(function(a, b) {
                if ($(b).css("z-index") == f) {
                    $("#taskbar-" + $(b).attr("id")).addClass("taskbar-app-on");
                    return false
                }
            });
            return false
        },
        full: function(a, b) {
        },
        restore: function(a, b) {
        },
        moveEnd: function() {
            $("#" + k).addClass("taskbar-app-on").siblings().removeClass("taskbar-app-on")
        },
        cancel: function(c) {
            var d = layui.data('desktop-app')['desktop-app-' + c];
            layui.each(d,
            function(a, b) {
                layer.close(b)
            });
            layui.data('desktop-app', {
                key: 'desktop-app-' + c,
                remove: true
            });
            $("#" + k).remove()
        },
        end: function() {
        }
    });
    
    var m = "";
    if (data.isicon) {
        m = ['<div class="layui-inline layui-elip taskbar-app taskbar-app-on" title="' + data.title + '" id="' + k + '"><i class="layui-icon" style=" background-color:' + data.iconbg + '">' + data.icon + '</i><span class="desktop-title layui-elip">' + data.title + '</span></div>'].join("")
    } else {
        m = ['<div class="layui-inline layui-elip taskbar-app taskbar-app-on" title="' + data.title + '" id="' + k + '"><span class="desktop-title layui-elip">' + data.title + '</span></div>'].join("")
    }
    if ($("#" + k).is(":visible")) return;
    $(".desktop-taskbar-app-list").append(m);
    $("#" + k).on("click",
    function() {
        var a = $(this);
        if (a.hasClass("taskbar-app-on")) {
            $("#layui-layer" + l).find(".layui-layer-setwin .layui-layer-min").click()
        } else {
            a.addClass("taskbar-app-on").siblings().removeClass("taskbar-app-on");
            $("#layui-layer" + l).show();
            layer.zIndex = parseInt(layer.zIndex + 1);
            layer.style(l, {
                zIndex: layer.zIndex
            })
        }
    }).siblings().removeClass("taskbar-app-on")
}

function clearHtml(str) {
 str = str.replace(/&/g, '&amp;');
 str = str.replace(/</g, '&lt;');
 str = str.replace(/>/g, '&gt;');
 return str;
}

function jsonEncode(jsonData){
   for(var i in jsonData){
        jsonData[i]=JSON.parse(clearHtml(JSON.stringify(jsonData[i])))
   }
   return jsonData;
}

function parse3TableData(res){ //res 即为原始返回的数据
  //  res.data=jsonEncode(res.data);
  if(res.code=="-1"){
    return {
	      'code': res.status?0:1, //解析接口状态
	      'msg': res.msg, //解析提示文本
	      'count': 0, //解析数据长度
	      'data': [] //解析数据列表
	};
  }else{
    var page = res.result.page;
	return {
	      'code': res.code?0:1, //解析接口状态
	      'msg': res.msg, //解析提示文本
	      'count': page.totalCount, //解析数据长度
	      'data': page.result //解析数据列表
	};
	}
}

function parseTableData(res){ //res 即为原始返回的数据
 //   res.data=jsonEncode(res.data);
	return {
	      'code': res.status?0:1, //解析接口状态
	      'msg': res.msg, //解析提示文本
	      'count': res.page==null? 0:res.page.total, //解析数据长度
	      'data': res.data //解析数据列表
	};
}

function parseTableAndData(res){ //res 即为原始返回的数据
 //   res.data=jsonEncode(res.data);
	return {
	      'code': res.status?0:1, //解析接口状态
	      'msg': res.msg, //解析提示文本
	      'count': res.page==null? 0:res.page.total, //解析数据长度
	      'data': res.data.retData //解析数据列表
	};
}

function downLoad(id) {
        var url =MODULE_NAME+"/file/download/"+id;
var exportForm = $('<form action="'+url+'" method="post">\
        <input type="hidden" name="ids" value="'+id+'"/>\
        </form>');
       $(document.body).append(exportForm);
       exportForm.submit();
       exportForm.remove();
}


function downloadTmplate(id){
	if(!id){
		return '';
	}
	
	/*return '<a class="layui-btn layui-btn-xs" href="javascript:;" onclick="downLoad('
			    + id + ')">附件下载</a>';*/
	var fileName=null;
	$.ajax({
       type: "GET",
       url: MODULE_NAME+'/cruder/File/get/'+id,
       dataType : "json",
       async: false,
       contentType : "application/json;charset=UTF-8",
       success: function (d) {
		   
		             if(d.data){
							var data=d.data;
							fileName=data.name;
					  }
					if(d.name){
						fileName=d.name;
						}
					
              },
       error:function (d) {}
      });
           return '<a class="layui-btn layui-btn-xs" href="javascript:;" onclick="downLoad('
			    + id + ')">' + fileName + '</a>';
}

window.changeBg=function change(url){
   // var src = ' ../resource/images/top-img.png';
	/**var url0=$('.desktop-bg2').css('background-image');
	url0=url0.replace('url(\"','');
	url0=url0.replace('\")','');
	bgList[desktopi]=url0; */
	var styles={"background-image":"url(" + url + ")","background-size":"100% 100%"}
	$("body").css(styles);
}


function showChatMessage(data){	
	var form,layer;
        	layui.config({
				dir: '/static/layui/',
		base: '/static/layui/lay/modules/'
	}).use(['form','layer','layedit'], function(){
        		  form = layui.form
        		  ,layer = layui.layer;
            layer.open({
                type: 2,
                anim: 0,
                shade: 0,
                title: data.fromId+" ："+data.name,
                maxmin: true,
                area: ['400px', '310px'],
                btn:['关闭'],
                yes:function(index,layero)
                {
					data.readed=true;
					updateOne('message',data);
                	var body = layer.getChildFrame('body', index);
                    //index为当前层索引
                  layer.close(index)
                },
                cancel:function(){//右上角关闭毁回调
                    if(data.toId ==""||data.toGroup){
						var param={};
						param.endId=data.id;
						param.fromId=data.fromId;
						param.group=data.toGroup;
						param.label='readed';
						param.name="已读消息";
						var body = layer.getChildFrame('body', index); 
						param.dataId=body.find('#Id').val();
						addRel('message',param);	                    
					}else{
						 data.readed=true;
                	     updateOne('message',data);
					}
                	
                	 close()
				 	var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
		  			parent.layer.close(index); //再执行关闭
                },
                zIndex: layer.zIndex //重点1
                ,success: function(layero, index){
                  	layer.setTop(layero); //重点2
                  	var body = layer.getChildFrame('body', index);
                  	var winName=layero.find('iframe')[0]['name'];
  	           	 	var iframeWin = window[winName]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
  	           	 // console.log(body.html()) //得到iframe页的body内容
	  	           	 if(iframeWin){
	  	           		 iframeWin.initForm(data)
	  	           	 }
                },
                content: MODULE_NAME+'/layui/message/form'
            });
            });
}


function showActionMessage(data,targetLabel,action,cname){	
	var form,layer;
	var title = data.name
	if(data.fromId){
		title=title+"("+data.fromId+")";
	}
        	layui.config({
				dir: '/static/layui/',
                base: '/static/layui/lay/modules/'
            }).use(['form','layer','layedit'], function(){
        		  form = layui.form
        		  ,layer = layui.layer;
            layer.open({
                type: 2,
                anim: 0,
                shade: 0,
                title: title,
                maxmin: true,
                area: ['852px', '676px'],
                btn:['关闭'],
                yes:function(index,layero)
                {
					data.readed=true;
					 var param={};
						param.endId=data.id;
						param.group=data.toGroup;
						param.label=action;
						param.name=cname;
						var body = layer.getChildFrame('body', index); 
				param.dataId=body.find('#Id').val();
						addRel(targetLabel,param);
                    //index为当前层索引
                  layer.close(index)
                },
                cancel:function(){//右上角关闭毁回调
                    if(data.toId ==""||data.toGroup||data.id){
						var param={};
						param.endId=data.id;
						param.group=data.toGroup;
						param.label=action;
						param.name=cname;
						var body = layer.getChildFrame('body', index); 
				param.dataId=body.find('#Id').val();
						addRel(targetLabel,param);	                    
					}else{
						 data.readed=true;
                	     updateOne(targetLabel,data);
					}
                	 close()
				 	var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
		  			parent.layer.close(index); //再执行关闭
                },
                zIndex: layer.zIndex //重点1
                ,success: function(layero, index){
                  layer.setTop(layero); //重点2
                  var body = layer.getChildFrame('body', index);
                  var winName=layero.find('iframe')[0]['name'];
  	           	 var iframeWin = window[winName]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
  	           	 // console.log(body.html()) //得到iframe页的body内容
  	           	 if(iframeWin){
  	           		 iframeWin.initForm(data)
  	           	 }
                },
                content: MODULE_NAME+'/form/'+targetLabel+'/readOnly'
            });
            });
}

function showJieLongForm(data,action,cname){	
	var layer;
	var title = data.jlName
	
	layui.config({
		dir: '/static/layui/',
		base: '/static/layui/lay/modules/'
	}).use(['form','layer','layedit'], function(){
		  form = layui.form
		  ,layer = layui.layer;
		  let metaId = null;
		  if(data.formId){
				metaId=data.formId
			}
			if(data.voId){
				metaId=data.voId
			}
	var urlJieLong = MODULE_NAME+'/collect/'+metaId+'/jlform'
    layer.open({
        type: 2,
        anim: 0,
        shade: 0,
        title: title,
        maxmin: true,
        area: ['1000px', '600px'],
        btn:['关闭'],
        yes:function(index,layero)
        {
            //index为当前层索引
          layer.close(index)
        },
        cancel:function(){//右上角关闭毁回调
        	 close()
		 	var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
  			parent.layer.close(index); //再执行关闭
        },
        zIndex: layer.zIndex //重点1
        ,success: function(layero, index){
         layer.setTop(layero); //重点2
         var body = layer.getChildFrame('body', index); 
         var winName=layero.find('iframe')[0]['name'];
       	 var iframeWin = window[winName]; 
       	 //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
       	 // console.log(body.html()) //得到iframe页的body内容
       	 if(iframeWin){
			body.find('#jlId').val(data.jlId);
			body.find('#jlName').val(title);
			body.find('#jlAction').val(action);
       		iframeWin.initForm(data)
       	 }
        },
        content: urlJieLong
    });
    });
}


function updateOne(upLabel,data){	
	var genurl = MODULE_NAME+"/cruder/"+upLabel+"/save";
    $.ajax({
       type: "post",
       url: genurl,
   	dataType : "json",
   	contentType : "application/json;charset=UTF-8",      
   	data: JSON.stringify(data),
   	success: function (d) {
		   if(!d.status){
			   layer.alert(d.msg, {icon: 5})
		   }else{
			   layer.alert(d.msg, {icon: 6})
		   }
     },
    error:function (d) {
           layer.alert(d.msg, {icon: 5})
    }
   }); 
}

function postDo(posUrl,data){
    $.ajax({
       type: "post",
       url: posUrl,
   	dataType : "json",
   	contentType : "application/json;charset=UTF-8",      
   	data: JSON.stringify(data),
   	success: function (d) {
            // layer.alert(d.msg, {icon: 6})
        	},
    error:function (d) {
           layer.alert(d.msg, {icon: 5})
    }
   }); 
}

function addRel(upLabel,data){	
	var genurl = MODULE_NAME+"/cruder/"+upLabel+"/addMyRel";
    $.ajax({
       type: "post",
       url: genurl,
   	dataType : "json",
   	contentType : "application/json;charset=UTF-8",      
   	data: JSON.stringify(data),
   	success: function (d) {
            // layer.alert(d.msg, {icon: 6})
        	},
    error:function (d) {
           layer.alert(d.msg, {icon: 5})
    }
   }); 
}