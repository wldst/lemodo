var focusId=null;
setInterval("myFunction()", 1);
function myFunction() {
    var x = document.getElementById("demo");
    if (document.hasFocus()) {
    	var axId = document.activeElement.id;
		if(axId!="luyin"&&axId!="mainFrameVoice"&&axId!="wumaManage"&&axId!="metaDataMange"){
		 focusId=document.activeElement.id;
		//x.innerHTML = "语音输入目标"+"id="+document.activeElement.id;
		}
    } else {
      //  x.innerHTML = "语音输入"+"id="+document.activeElement.id;
    }
}
var frame = document.getElementById("mainFrameVoice");
var textId=null;
function getiframeMsg(event){
	if(event){
		 const res = event.data;
		    console.log(event)
		    $("#"+res.inputId).val(res.msg);
	}
}
// vue向iframe传递信息
function opt(optCmd){
    const iframeWindow = frame.contentWindow;
    iframeWindow.postMessage({ 
      cmd:optCmd,
      inputId: focusId
    },'*')
  }

window.addEventListener('message',getiframeMsg())


function openIframe() {
 var frame = document.getElementById("mainFrameVoice");
 frame.src="https://localhost:9443/lemodo/voice/index.html?inputId="+focusId;
  $("#mainFrameVoice").show();
}

window.addEventListener('message', function(e) {
    $("#"+e.data.inputId).val(e.data.msg);
})

$('table').on("contextmenu",function(e){
	openIframe()
			return false;
});
$('table').on("dblclick",function(e){
	$(".desktop-menu").hide();
	return false;
});
$("table").on("contextmenu",
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