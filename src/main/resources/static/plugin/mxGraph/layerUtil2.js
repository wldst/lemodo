var form,layer;
layui.use(['form','layer'], function(){
		  form = layui.form
		  ,layer = layui.layer;
});

function parentShow(url,title,atti){
	parent.layui.index.openTabsPage(url+atti, title);
}
function openLayer(url,attName,atti){
		layer.open({
	          type: 2,
	          anim: 0,
	          shade: 0,
	          maxmin:true,
	          title: attName,
	          area: ['70%', '80%'],
	          btn:['关闭'],
	          full: function(a, b) {
              	$(a).find('.layui-layer-content').css('height','100%');
              	$(a).find('iframe').css('height','100%');
              },
              restore: function(a, b) {
              	$(a).find('iframe').css('height','90%');
              	$(a).find('.layui-layer-content').css('height','90%');
              },
	          yes:function(index,layero)
	          {
				//index为当前层索引
	              layer.close(index)
	          },
	          cancel:function(){
	        	  //右上角关闭毁回调
	         	var index = parent.layer.getFrameIndex(attName); //先得到当前iframe层的索引
	      		parent.layer.close(index); //再执行关闭
	          },
	          zIndex: layer.zIndex //重点1
	          ,success: function(layero){
	            layer.setTop(layero); //重点2
	          },
	          content: url+atti
	      });
}


function openLayer2(url,attName,atti){
	layer.open({
          type: 2,
          anim: 0,
          shade: 0,
          maxmin:true,
          title: attName,
          area: ['70%', '80%'],
          btn:['关闭'],
          full: function(a, b) {
          	$(a).find('.layui-layer-content').css('height','100%');
          	$(a).find('iframe').css('height','100%');
          	
          },
          restore: function(a, b) {
          	$(a).find('iframe').css('height','90%');
          	$(a).find('iframe').find('iframe').css('height','90%');
          },
          yes:function(index,layero)
          {
			//index为当前层索引
              layer.close(index)
          },
          cancel:function(){//右上角关闭毁回调
         	var index = parent.layer.getFrameIndex(attName); //先得到当前iframe层的索引
      		parent.layer.close(index); //再执行关闭
          },
          zIndex: layer.zIndex //重点1
          ,success: function(layero){
            layer.setTop(layero); //重点2
          },
          content: url+atti
      });
}

function openInstance(data){
	var width='100%';
	  layer.open({
	      type: 2,
	      anim: 0,
	      shade: 0,
	      title: data.name,
	      maxmin: true,
	      area: [width, '100%'],
	      btn:['关闭'],
	      full: function(a, b) {
          	$(a).find('.layui-layer-content').css('height','100%');
          	$(a).find('iframe').css('height','100%');
          },
          restore: function(a, b) {
          	$(a).find('iframe').css('height','90%');
          	$(a).find('.layui-layer-content').css('height','90%');
          },
	      yes:function(index,layero)
	      {
	          //index为当前层索引
	        layer.close(index)
	      },
	      cancel:function(){//右上角关闭毁回调
	     	var index = parent.layer.getFrameIndex(data.name); //先得到当前iframe层的索引
	  		parent.layer.close(index); //再执行关闭
	      },
	      zIndex: layer.zIndex //重点1
	      ,success: function(layero, index){
	        layer.setTop(layero); //重点2
	   	    var body = layer.getChildFrame('body', index);
	        var winname = layero.find('iframe')[0]['name']
	   	    var iframeWin = window[winname]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
	   	    iframeWin.initForm(data)
	      },
	      content: "/cd/layui/"+data.id+"/documentRel"
	  });
}