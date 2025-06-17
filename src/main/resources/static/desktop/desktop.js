/*
 * Windows WebOS
 * WEB 桌面风格，基于Layui-打造的Windows WebOS桌面风格，实现了右键、开始菜单、返回桌面等一些列功能。窗口全部由 layer 完成
 * http://test.90zs.net/window
 * Copyright 2016-2017, SMALL,1531982850
 * The 90zs.net
 * http://www.90zs.net/
 * Released on: 12, 2016
*/
!
function() {
	layui.config({
        dir: '/static/layui/',version: false ,debug: false,
		base: '/static/layui/lay/modules/'})
	.use(['form','layer','jquery','laytpl','notice','laydate'], function(){
    var n = layui.form,
    $ = layui.jquery,
    layer = layui.layer,
    laytpl = layui.laytpl,
	notice = layui.notice,
    laydate = layui.laydate;
    $("#loading").hide().remove();
    function showChats(){
    	var condition={};
    	condition['page']=1;
    	condition['limit']=10;
    	condition['readed']='false';
    	getActionData(condition,'message','readed','已读消息');
    }
     function showNotes(){
    	var condition={};
    	condition['page']=1;
    	condition['limit']=10;
		condition['status']='doing';
    	queryData(condition,MODULE_NAME+'/cruder/Todo/query');
		//getMessage(condition,'/cd/cruder/Todo/query');
    }

    var webSocketUrl = 'wss://'+window.location.host+'/message/';//websocketi连接地址
    var socketMsg;//websocket连接
    //创建websocket连接
    function createWebSocketConnect() {
        if (!socketMsg||socketMsg.readyState != socketMsg.OPEN) {//避免重复连接
            var socketMsg;//websocket连接
            socketMsg = new WebSocket(webSocketUrl + userId);
            socketMsg.onopen = function () {
                console.log("websocket已连接");
                socketMsg.send(userId+":上线了");
            };
            socketMsg.onmessage = function (e) {
                //服务端发送的消息
                //showMessages(e.data);
                var data={};
                if(e.data.indexOf(":")>0){
					let xx = e.data.split(":");
					data['name']=xx[0];
					 data['content']=xx[1];
				}else{
					 data['name']='消息';
					 data['content']=e.data;
				}
                showMsg(data);
               // showActionMessage(data[i],"message","readed","消息");
            };
            socketMsg.onclose = function () {
                socketMsg.send(userId + "已经退出系统");
            }
        }
    }
    
    function showNotice(){
    	var condition={};
    	condition['page']=1;
    	condition['limit']=10;
		condition['readed']='false';
    	getActionData(condition,'Notice','readed','已读');
    }
    function showCareer(){
		if(p){
			p.hidemenu();
		}
		
	    var a={"title":"智能职业生涯规划","url":MODULE_NAME+"/scene/CareerScheduleDegsin"}
	        windowOpen(a)
	}
	
	function showHomePage(){
		if(p){
			p.hidemenu();
		}
		
	    var a={"title":"主页","url":MODULE_NAME+"/scene/homePage"}
	        windowOpen(a)
	}
	
    function showJieLong(){
		var condition={};
    	condition['page']=1;
    	condition['limit']=10;
		condition['writed']='false';
    	getLongData(condition,'jielong','writed','数据接龙');
	}
function getLongData(po,tlabel,action,cname){
	var actionUrl = MODULE_NAME+'/collect/'+tlabel+'/'+action;
	var data=null;
	 $.ajax({
	      type: "post",
	      url: actionUrl,
	        dataType : "json",
	        contentType : "application/json;charset=UTF-8",
	        data: JSON.stringify(po),
	        success: function (d) {
	           data=d.data;
	           for(var i in data){
	        	   showJieLongForm(data[i],action,cname);
	           }
	      	},
	 		error:function (d) {
		    }
	    });
	 return data;
}

function getActionData(po,tlabel,action,cname){
	var actionUrl = MODULE_NAME+'/more/'+tlabel+'/get/'+action;
	var data=null;
	 $.ajax({
	      type: "post",
	      url: actionUrl,
	        dataType : "json",
	        contentType : "application/json;charset=UTF-8",
	        data: JSON.stringify(po),
	        success: function (d) {
	           data=d.data;
	           for(var i in data){
	        	   showActionMessage(data[i],tlabel,action,cname);
	           }
	      	},
	 		error:function (d) {
		    }
	    });
	 return data;
}
    
function getMessage(po,genurl){
	var data=null;
	 $.ajax({
	      type: "post",
	      url: genurl,
	        dataType : "json",
	        contentType : "application/json;charset=UTF-8",
	        data: JSON.stringify(po),
	        success: function (d) {
	           data=d.data;
	           for(var i in data){
	        	   showBRMessages(data[i].content,data[i].name);
	           }
	      	},
	 		error:function (d) {
		    }
	    });
	 return data;
}
	function showMessages(content,title){
    	    let options = {
    	        closeButton:true,//显示关闭按钮
    	        debug:false,//启用debug
    	        positionClass:"toast-top-center",//弹出的位置,
    	        showDuration:"3000",//显示的时间
    	        hideDuration:"1000",//消失的时间
    	        timeOut:"0",//停留的时间,0则不自动关闭
    	        extendedTimeOut:"1000",//控制时间
    	        showEasing:"swing",//显示时的动画缓冲方式
    	        hideEasing:"linear",//消失时的动画缓冲方式
    	        iconClass: 'layui-icon layui-icon-praise', // 自定义图标，有内置，如不需要则传空 支持layui内置图标/自定义iconfont类名,需要完整加上 layui-icon/icon iconfont
    	        onclick: null, // 点击关闭回调
    	    }
    	    notice.info(content,title,options);
    	}
	function showBRMessages(content,title){
			let options = {
	            closeButton:true,//显示关闭按钮
	            debug:false,//启用debug
	            positionClass:"toast-bottom-left",//弹出的位置,
	            showDuration:"3000",//显示的时间
	            hideDuration:"1000",//消失的时间
	            timeOut:"2000",//停留的时间
	            extendedTimeOut:"1000",//控制时间
	            showEasing:"swing",//显示时的动画缓冲方式
	            hideEasing:"linear",//消失时的动画缓冲方式
	            iconClass: 'toast-info', // 自定义图标，有内置，如不需要则传空 支持layui内置图标/自定义iconfont类名
	            onclick: null, // 点击关闭回调
	        };
    	   notice.success(content,title,options);
    	}
//    	var condition={};
//    	condition['page']=1;
//    	condition['limit']=10;
//    	queryData(condition,'/cd/cruder/Message/query');
    
   // change('../images/desktop_windows7.jpg');

    var p = {
        setting: function(a) {
            p.hidemenu();
             var a={"title":"设置数据","url":MODULE_NAME+"/manage/Settings"}
	        windowOpen(a)
        },
        theme: function(a) {
            p.hidemenu();
			showBRMessages("施工现场，请绕道慢行！","提示");
        },
        metaDataManage: function(){
	        p.hidemenu();
	        var a={"name":"元数据管理","title":"元数据管理","url":MODULE_NAME+"/vue/po"}
	        windowOpen(a)
        },
		mapp: function() {
            window.location.href="/lemodo/antv/index.html";
        },
		mobileQrCode: function() {
			p.hidemenu();
			var genurl = MODULE_NAME+"/qrcode/mobile";
		 $.ajax({
		      type: "post",
		      url: genurl,
		      dataType : "json",
		      contentType : "application/json;charset=UTF-8", 
		      success: function (d) {
	            var url= MODULE_NAME+"/file/show/"+d.data;
				var imgContent="<img src='"+url+"' />";
	            var width='300px';
		   	    var height='430px';
				  layer.open({
				      type: 1,
				      anim: 0,
				      shade: 0,
		              maxmin: true,
				      title: "移动端二维码",
				      area: [width, height],
				      btn:['关闭'],
				      yes:function(index,layero)
				      {
				          //index为当前层索引
				        layer.close(index)
				      },
				      cancel:function(){//右上角关闭毁回调
				     	 close()
				      },
				      zIndex: layer.zIndex //重点1
				      ,success: function(layero, index){
				        layer.setTop(layero); //重点2
				      },
				      content: imgContent
				  });
		      	},
		 		error:function (d) {
			        layer.alert(d.msg, {icon: 5})
			    }
		    });
        },
        messageCenter: function(){
	 		p.hidemenu();
	        var a={"title":"消息中心","url":MODULE_NAME+"/ws/square"}
	        windowOpen(a)	       
        },
        wumaManage: function(){
	 		p.hidemenu();
	        var a={"title":"定义数据","url":MODULE_NAME+"/vo/wumaManage"}
	        windowOpen(a)	       
        },
		webManage: function(){
	       window.location.href=MODULE_NAME+"/admin2";
        },
        addApp: function(){
			p.hidemenu();
			var desktop=$(".swiper-slide-active div").attr("data-menuid");
	        var a={"title":"桌面"+desktop+"应用选择","url":MODULE_NAME+"/manage/App/app/Desktop/"+desktop}
	        windowOpen(a)
        },        
        delDesktop:function(){
			p.hidemenu();
			var desktop=$(".swiper-slide-active div").attr("data-menuid");
	        var a={"title":"桌面"+desktop+"应用选择","url":MODULE_NAME+"/manage/App/app/Desktop/"+desktop}
	        windowOpen(a)
        },
        
        users: function(a) {
            p.hidemenu();
            showBRMessages("施工现场，请绕道慢行！","提示");
        },
        loginout: function(a) {
            p.hidemenu();
            window.top.location.href=MODULE_NAME+"/adminctrl/loginout";
        },
        technicalsupport: function(a) {
            p.hidemenu();
            layer.alert("加QQ啊（442441824），", {
                icon: 1,
                title: "技术支持"
            })
        },
        lockscreen: function(b) {
            layer.open({
                type: 1,
                title: false,
                closeBtn: false,
                area: '300px;',
                shade: .8,
                id: 'LAY_layuipro',
                resize: false,
                btn: ['解锁'],
                btnAlign: 'c',
                moveType: 1,
                content: '<div style="padding: 50px; line-height: 22px; background-color: #393D49; color: #fff; font-weight: 300;">好了，封印解除</div>',
                success: function(a) {}
            });
            p.hidemenu()
        },
        
        myInfo: function(b) {
	        window.location.href="/lemodo/blog/index.html";
        },
        closeall: function(c) {
            var d = $(".taskbar-app").length;
            p.hidemenu();
            if (d < 1) return;
            layer.alert("确定关闭所有窗口？", {
                icon: 0,
                btn: ['确定', '取消'],
                zIndex: parseInt(layer.zIndex + 1),
                yes: function(a, b) {
                    $(document).find(".taskbar-app").remove();
                    layer.closeAll('iframe');
                    layer.close(a)
                },
                end: function() {}
            })
        },
        showdesktop: function(a) {
            p.hidemenu();
            $(document).find(".layui-layer .layui-layer-min").click();
            $(document).find(".taskbar-app").removeClass("taskbar-app-on")
        },
        hidemenu: function(a) {
            $(".desktop-menu").hide()
        },
        hidopeningemenu: function() {
            $(".opening-menu").removeClass("opening-menu-on")
        },
        openingmenu: function(a) {
            $("#opening-menu").toggleClass("opening-menu-on").off('mousedown', p.stope).on('mousedown', p.stope);
            $(document).off('mousedown', p.hidopeningemenu).on('mousedown', p.hidopeningemenu);
            $(window).off('resize', p.hidopeningemenu).on('resize', p.hidopeningemenu);
            a.off('mousedown', p.stope).on('mousedown', p.stope)
        },
        hide: function() {
            layer.closeAll('tips')
        },
        pattern: function(a) {
            var b = new Date();
            var o = {
                "M+": b.getMonth() + 1,
                "d+": b.getDate(),
                "h+": b.getHours() % 12 == 0 ? 12 : b.getHours() % 12,
                "H+": b.getHours(),
                "m+": b.getMinutes(),
                "s+": b.getSeconds(),
                "q+": Math.floor((b.getMonth() + 3) / 3),
                "S": b.getMilliseconds()
            };
            var c = {
                "0": "日",
                "1": "一",
                "2": "二",
                "3": "三",
                "4": "四",
                "5": "五",
                "6": "六"
            };
            if (/(y+)/.test(a)) {
                a = a.replace(RegExp.$1, (b.getFullYear() + "").substr(4 - RegExp.$1.length))
            }
            if (/(E+)/.test(a)) {
                a = a.replace(RegExp.$1, ((RegExp.$1.length > 1) ? (RegExp.$1.length > 2 ? "星期": "周") : "") + c[b.getDay() + ""])
            }
            for (var k in o) {
                if (new RegExp("(" + k + ")").test(a)) {
                    a = a.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)))
                }
            }
            return a
        },
        refreshtime: function() {
            $(".taskbar-time").attr("title", p.pattern('yyyy年MM月dd日 EEE'));
            $("#laydate-hs").text(p.pattern('HH:mm'));
            $("#laydate-ymd").text(p.pattern('yyyy/MM/dd'))
        },
        appopen: function(g) {
            var h = true,
            data = g.data();
            $(document).find(".taskbar-app").each(function(a, b) {
                if ($(b).attr("title") == data.title) {
                    g.removeClass("disabled");
                    $(b).click();
                    h = false;
                    return
                }
            });
            if (!h) return;
            var i = $(".taskbar-app").length + 1,
            maxcount = parseInt((layui.jquery(".desktop-taskbar").width() - 160) / 110);
            if (i > maxcount) {
                layer.alert("请先关闭一些窗口！", {
                    title: "官人休息下？",
                    icon: 2,
                    zIndex: layer.zIndex + 1
                });
                return
            }
            var j = data.width ? data.width: $(".desktop-container").width() * 0.8,
            height = data.height ? data.height: $(".desktop-container").height() * 0.9;
            var k = '';
//            $(window).resize(function () {
//                FrameWH();
//            });
            var l = layer.open({
                type: 2,
                title: [data.title, 'background-color:#dcd;color:#555'],
                shadeClose: true,
                shade: false,
                maxmin: true,
                area: [j + 'px', height + 'px'],
                content: data.url,
                zIndex: layer.zIndex,
                skin: 'desktop-win-app',
                moveType: 1, // 开启拖拽模式
                moveOut: true, // 允许拖出屏幕外
                success:  function(layero, index){
                    g.removeClass("disabled");
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
                	FrameWH3(a)
                //	let ifa = $(a).find('iframe');
                //	ifa.css('height','1000px;');
                //	let ifb = ifa.find('iframe');
                //	if(ifb){
                //		ifb.css('height','980px;');
                //	}
                },
                refreshWind:function(a){
					a=a.data("id");
					url=$("#layui-layer-iframe"+a).attr("src");
					layer.iframeSrc(a,url)
				},
                restore: function(a, b) {
                	$(a).find('iframe').css('height',height*0.88 + 'px');
                	$(a).find('iframe').css('width', j*0.78 + 'px');
                	
                	//$(a).find('iframe').find('iframe').css('height','90%');
                	//$(a).find('.layui-layer-content').find('iframe').css('width','98%');
                	
                	//$(a).find('iframe').find('iframe').css('width','90%');
                	//$(a).find('.layui-layer-content').css('width','98%');
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
                    g.removeClass("disabled")
                }
            });
            k = "taskbar-layui-layer" + l;
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
        },
        stope: function(e) {
            e = e || window.event;
            e.stopPropagation ? e.stopPropagation() : e.cancelBubble = true
        },
        arrange: function(c) {
            c = $(".swiper-slide-active").index();
            c = c == '' || c == undefined ? 0 : c;
            var d = $(".desktopContainer:eq(" + c + ")");
            var e = $(".desktopContainer");
            var f = {
                x: 0,
                y: 0,
                bottom: 65,
                width: 96,
                height: 96,
                parent: {
                    height: 0,
                    width: 0
                },
                padding: {
                    top: 10,
                    left: 10,
                    right: 0,
                    bottom: 10
                }
            };
            f.parent.height = e.height() - 40;
            f.parent.width = e.width();
            d.find(".desktop-app").each(function(a, b) {
                $(b).css("top", f.y + "px");
                $(b).css("left", f.x + "px");
                f.height = $(b).height();
                f.width = $(b).width();
                f.y = f.y + f.height + f.padding.bottom + f.padding.bottom;
                if (f.y >= f.parent.height - f.bottom) {
                    f.y = 0;
                    f.x = f.x + f.width + f.padding.left
                }
            })
        },        
        init: function() {
            var c = ['<div class="desktop-app"  data-id="{{d.apps[app].appid}} " data-title="{{d.apps[app].name}}" '+
            	'data-url="{{d.apps[app].url}}" '+
            	'data-icon="{{d.apps[app].icon}}" '+
            	'data-iconbg="{{d.apps[app].iconbg}}" '+
            	'data-isicon="{{d.apps[app].isicon}}" '+
            	'data-height="{{d.apps[app].height}}" '+
            	'data-width="{{d.apps[app].width}}" '+
            	'data-fid="{{app}}">', 
            	'<i class="layui-icon" style="background-color:{{d.apps[app].iconbg}}">{{d.apps[app].icon}}</i>',
            	'<span class="desktop-title layui-elip">{{d.apps[app].name}}</span>', '</div>'].join(""),
            desktopTmp = ['{{# layui.each(d.menu, function(index, menuitem){ if(menuitem.name=="opening")return false;}}',
            	'<div class="swiper-slide">',
	            	'<div class="desktopContainer"  data-menuid="{{menuitem.menuid}}" data-name="{{menuitem.name}}" data-BG="{{menuitem.BG}}" >',
	            		'{{# layui.each(menuitem.app, function(index, app){}}', c, '{{# });}}',
	            	'</div>',
            	'</div>', '{{# }); }} '].join(""),
            desktopOpeningTmp = ['{{# layui.each(d.menu, function(index, menuitem){ if(menuitem.name!="opening")return false;}}',
            	'{{# layui.each(menuitem.app, function(index, app){}}', c, '{{# });}}',
            	'{{# });}}'].join("");
            laytpl(desktopTmp).render(desktpData, function(a) {
                $(".swiper-wrapper").html(a)
            });
            laytpl(desktopOpeningTmp).render(desktpData, function(a) {
                $(".opening-menu-app-list").html(a)
            });
            $(".desktop-container").css("height", $(window).height() - 30);
            showNotes();
            showNotice();
            showMessages("欢迎"+myName+"回家","欢迎");
            showChats();
            showJieLong();
            showHomePage()
            createWebSocketConnect();
            //显示接龙            
            var d = new Swiper('.swiper-container', {
                pagination: '.swiper-pagination',
                simulateTouch: false,
                slidesPerView: 1,
                paginationClickable: true,
                spaceBetween: 30,
                keyboardControl: true,
                mousewheelControl: true,
                onSlideChangeEnd: function(a) {
                    p.arrange(a.realIndex)
                }
            });
            $(window).resize(function(a) {
                $(".desktop-container").css("height", $(window).height() - 40);
                $(".desktopContainer").css("height", $(".desktop-container").height());
                p.arrange()
            });
            $(".desktopContainer").sortable({
                revert: true
            });
            $(".desktopContainer").sortable({
                connectToSortable: ".desktopContainer",
                stop: function(a, b) {
                    p.arrange()
                }
            }).disableSelection();
            p.arrange();
            p.refreshtime();
            setInterval(p.refreshtime, 1000);
            $(document).contextmenu(function() {
                return false
            });
            $(".desktopContainer").on("contextmenu",
            function(a) {
                var x = a.clientX,
                y = a.clientY,
                desktopmenu = $(".desktop-menu");
                var b = document.body.clientWidth,
                height = document.body.clientHeight;
                x = (x + desktopmenu.width()) >= b ? b - desktopmenu.width() - 15 : x;
                y = (y + desktopmenu.height()) >= height - 40 ? height - desktopmenu.height() - 15 : y;
                desktopmenu.css({
                    "top": y,
                    "left": x
                }).show()
            });
            $(".desktop-app").on("dblclick",
            function() {
                var a = $(this);
                if (a.hasClass("disabled")) return false;
                a.addClass("disabled");
                p.appopen(a)
            })
        }
    };
 //更新P的数据。   
     function myDesktopRightMenu(){
    	var condition={};
    	condition['page']=1;
    	condition['limit']=10;
		var genUrl=MODULE_NAME+'/auth/query/RightMenu'
    	$.ajax({
	      type: "post",
	      url: genUrl,
	        dataType : "json",
	        contentType : "application/json;charset=UTF-8",
	        data: JSON.stringify(condition),
	        success: function (d) {
	           data=d.data;
	           for(var i in data){
				   var ri=data[i];
				   var ss = $('#'+ri.code+'').val();
	        	  if(!ss){
					$('<li><a href="javascript:;" id="'+ri.code+'"  class="small-click" data-type="'+ri.code+'">'+ri.name+'</a></li>  ').insertAfter($('#myRight'));
				  }
	        	  p[ri.code]=ri.JavaScript.trim()
	           }
	      	},
	 		error:function (d) {
		    }
	    });
    }
    myDesktopRightMenu()
    p.init();

$(".taskbar-time").on('click',function() {
	if(!onlyPhoneUser){
        var a={"title":"日程","url":MODULE_NAME+"/layui/calendar"}
        windowOpen(a)
		// openCalendarWindow();
	}                
                
  })
    $('body').on('click', '.small-click',
	    function() {
	        var a = $(this),
	        type = a.data('type');
	        
	        if(p[type]  instanceof Function){
				p[type] ? p[type].call(this, a) : ''
			}else{
	 			p.hidemenu();
	 			 if(p[type].trim().startsWith("{")){
				    var a=(new Function('return '+p[type]))()
	                windowOpen(a)
				 }else{
					 eval(p[type].trim());
				 }
			}
	    })
});
} ();