//工作流组件组
function Group(){
   this.id=null;
   this.name=null;
   this.count=0;
   this.nodes=[];
   this.lines=[];
   this.selectedObj=[];
   this.selectedLineFrom=[];
   this.selectedLineTo=[];
   this.mouseX= -1;
   this.mouseY= -1;
   this.mouseEndX= -1;
   this.mouseEndY= -1;
   this.action=null;
   this.lineFlag=null;
   this.multiSelect=false;
   this.ctrlKey=false;
   this.nodeMirror=null;
   this.lineMirror=null;
   this.bottomHeight=10;
   this.rightWidth=10;
   this.init=function(){//组件组初始化
      var obj=document.getElementById('group');
      obj.setAttribute('bindClass',this);
      obj.onmousedown=GroupEvent.mouseDown;
      obj.onmousemove=GroupEvent.mouseMove;
      obj.onmouseup=GroupEvent.mouseUp;
      obj.onkeydown=GroupEvent.keyDown;
      obj.onkeyup=GroupEvent.keyUp;
      obj.ondblclick=GroupEvent.dblClick;
      this.lineMirror=new Line();
      this.lineMirror.textFlag=false;
      this.lineMirror.mirrorFlag=true;
      this.lineMirror.init();
      this.lineMirror.setDisplay('none');
      this.lineMirror.strokeObj.dashStyle='dashdot';
      this.lineMirror.obj.strokecolor='#000000';
      this.nodeMirror=new Node();
      this.nodeMirror.strokeFlag=true;
      this.nodeMirror.shadowFlag=false;
      this.nodeMirror.textFlag=false;
      this.nodeMirror.mirrorFlag=true;
      this.nodeMirror.init();
      this.nodeMirror.setDisplay('none');
      this.nodeMirror.obj.strokecolor='black';
      this.nodeMirror.obj.style.zIndex='100';
      this.nodeMirror.obj.filled=false;
      this.nodeMirror.strokeObj.dashstyle='dot';
   };
   this.getObjectNum=function(){
      this.count++;return this.count;
   };
   this.point=function(flag){
      if(flag=='down'){
         this.mouseX=GroupEvent.getMouseX();
         this.mouseY=GroupEvent.getMouseY();
      }else if(flag=='up'){
         this.mouseEndX=GroupEvent.getMouseX();
         this.mouseEndY=GroupEvent.getMouseY();
      }
   };
   this.getEventNode=function(flag){
      var res=null;
      var nodeNum=this.nodes.length;
      var node=null;
      var x;var y;
      if(flag=='down'){
         x=this.mouseX;y=this.mouseY;
      }else if(flag=='up'){
         x=this.mouseEndX;y=this.mouseEndY;
      }
      for(var i=(nodeNum-1);i>=0;i--){
         node=this.nodes[i];
         if(node.pointInObj(x,y)){
            res=node;break;
         }
      }
      return res;
   };
   this.getEventLine=function(){
      var res=null;var lineNum=this.lines.length;
      var line=null;var x=this.mouseX;
      var y=this.mouseY;var isStroke= -1;
      for(var i=(lineNum-1);i>=0;i--){
         line=this.lines[i];
         if(line.pointInObj(x,y)){
            if(res==null||line.obj.style.zIndex=='22'){
               res=null;res=GroupEvent.insertObjInArr(res,line);
               isStroke=line.pointInStroke(x,y);
               if(isStroke==0){
                  this.selectedLineTo=[];
                  this.selectedLineFrom=[];
                  this.selectedLineTo=GroupEvent.insertObjInArr(this.selectedLineTo,line);
               }else if(isStroke==1){
                  this.selectedLineTo=[];this.selectedLineFrom=[];
                  this.selectedLineFrom=GroupEvent.insertObjInArr(this.selectedLineFrom,line);
               }
            }
         }
     }
     return res;
   };
   this.moveSelectedObj=function(){
      var x=GroupEvent.getMouseX();
      var y=GroupEvent.getMouseY();
      var num=this.selectedObj.length;
      var lineNum=this.lines.length;
      var line=null;
      for(var i=0;i<num;i++){
         this.selectedObj[i].move(x,y,this.mouseX,this.mouseY);
         for(var j=0;j<lineNum;j++){
            line=this.lines[j];
            if((line.fromObj==this.selectedObj[i])||(line.toObj==this.selectedObj[i])){
               line.relink();
            }
         }
      }
   };
   this.moveSelectedObjEnd=function(){
      var num=this.selectedObj.length;
      for(var i=0;i<num;i++){
         this.selectedObj[i].moveEnd();
      }
   };
   this.moveLine=function(){
      var x=GroupEvent.getMouseX();
      var y=GroupEvent.getMouseY();
      var num=this.selectedLineTo.length;
      for(var i=0;i<num;i++){
         this.selectedLineTo[i].setTo(x,y,null);
      }
      num=this.selectedLineFrom.length;
      for(var i=0;i<num;i++){
         this.selectedLineFrom[i].setFrom(x,y,null);
      }
   };
   this.moveLineEnd=function(selNode){
      var num=this.selectedLineTo.length;
      if(selNode!=null){
         for(var i=0;i<num;i++){
            if(this.checkPointLine(this.selectedLineTo[i].fromObj,selNode)){
               this.selectedLineTo[i].setTo(this.mouseEndX,this.mouseEndY,selNode);
               this.selectedLineTo[i].relink();
            }else{
               this.selectedLineTo[i].relink();
            }
         }
      }else{
         for(var i=0;i<num;i++){
            this.selectedLineTo[i].relink();
         }
      }
      this.selectedLineTo=[];
      num=this.selectedLineFrom.length;
      if(selNode!=null){
         for(var i=0;i<num;i++){
            if(this.checkPointLine(selNode,this.selectedLineFrom[i].toObj)){
               this.selectedLineFrom[i].setFrom(this.mouseEndX,this.mouseEndY,selNode);
               this.selectedLineFrom[i].relink();
            }else{
               this.selectedLineFrom[i].relink();
            }
         }
      }else{
         for(var i=0;i<num;i++){
            this.selectedLineFrom[i].relink();
         }
      }
      this.selectedLineFrom=[];
   };
   this.drawLineEnd=function(selNode){
      if(selNode!=null){
         this.drawMirrorLineTo(selNode);
         if(this.checkPointLine(this.lineMirror.fromObj,this.lineMirror.toObj)){
            var line=new PolyLine();line.init();
            line.setShape(this.lineFlag);
            line.link(this.lineMirror);
            this.clearSelected();
         }
      }
      this.lineMirror.setDisplay('none');
   };
   this.drawMirrorLineFrom=function(selObj){
      this.lineMirror.setFrom(this.mouseX,this.mouseY,selObj);
      this.lineMirror.setTo(this.mouseX,this.mouseY,selObj);
      this.lineMirror.setDisplay('');
   };
   this.drawMirrorLineTo=function(selObj){
      var x=GroupEvent.getMouseX();
      var y=GroupEvent.getMouseY();
      this.lineMirror.setTo(x,y,selObj);
   };
   this.drawMirrorNodeStart=function(){
      this.multiSelect=false;
      this.nodeMirror.setLeft(GroupEvent.getX(this.mouseX));
      this.nodeMirror.setTop(GroupEvent.getY(this.mouseY));
      this.nodeMirror.setHeight(0);this.nodeMirror.setWidth(0);
   };
   this.drawMirrorNode=function(){
      var x=GroupEvent.getMouseX();
      var y=GroupEvent.getMouseY();
      this.nodeMirror.setWidth(Math.abs(GroupEvent.getX(x)-GroupEvent.getX(this.mouseX)));
      this.nodeMirror.setHeight(Math.abs(GroupEvent.getY(y)-GroupEvent.getY(this.mouseY)));
      if(GroupEvent.getX(x)<GroupEvent.getX(this.mouseX)){
         this.nodeMirror.setLeft(GroupEvent.getX(x));
      }
      if(GroupEvent.getY(y)<GroupEvent.getY(this.mouseY)){
         this.nodeMirror.setTop(GroupEvent.getY(y));
      }
      this.nodeMirror.setDisplay('');
   };
   this.drawMirrorNodeEnd=function(){
      this.nodeMirror.setDisplay('none');
      if(this.embodyObj())this.multiSelect=true;
   };
   this.embodyObj=function(){
      var res=false;var x1=this.nodeMirror.left;
      var x2=this.nodeMirror.left+this.nodeMirror.width;
      var y1=this.nodeMirror.top;
      var y2=this.nodeMirror.top+this.nodeMirror.height;
      var nodeNum=this.nodes.length;
      var lineNum=this.lines.length;
      var node=null;
      var line=null;
      for(var i=0;i<nodeNum;i++){
         node=this.nodes[i];
         if((x1<=node.left)&&(x2>=(node.left+node.width))&&(y1<=node.top)&&(y2>=(node.top+node.height))){
            node.setSelected();
            this.selectedObj=GroupEvent.insertObjInArr(this.selectedObj,node);
            node.x=0;node.y=0;
            node.mouseX=node.left;
            node.mouseY=node.top;
            res=true;
         }
      }
      for(var i=0;i<lineNum;i++){
         line=this.lines[i];
         if((x1<=Math.min(line.fromX,line.toX))&&(x2>=Math.max(line.fromX,line.toX))&&(y1<=Math.min(line.fromY,line.toY))&&(y2>=Math.max(line.fromY,line.toY))){
            line.setSelected();
            this.selectedObj=GroupEvent.insertObjInArr(this.selectedObj,line);
            res=true;
         }
      }
      return res;
   };
   this.checkPointLine=function(fromObj,toObj){
      var res=true;
      if(fromObj==toObj){
         res=false;
      }
      var len=this.lines.length;
      var line=null;
      for(var i=0;i<len;i++){
         line=this.lines[i];
         if((line.fromObj==fromObj)&&(line.toObj==toObj)){
            res=false;alert("两点之间已经存在连接！");
            break;
         }
      }
      return res;
   };
   this.clearSelected=function(){
      var num=this.selectedObj.length;
      for(var i=0;i<num;i++){
         this.selectedObj[i].clearSelected();
      }
      this.selectedObj=[];
   };
   this.selectAll=function(){//流程各组件全选方法
      this.selectedObj=[];
      var num=this.nodes.length;
      var obj=null;
      for(var i=0;i<num;i++){
         obj=this.nodes[i];obj.setSelected();
         this.selectedObj=GroupEvent.insertObjInArr(this.selectedObj,obj);
         obj.x=0;obj.y=0;
         obj.mouseX=obj.left;
         obj.mouseY=obj.top;
      }
      num=this.lines.length;
      obj=null;
      for(var i=0;i<num;i++){
         obj=this.lines[i];
         obj.setSelected();
         this.selectedObj=GroupEvent.insertObjInArr(this.selectedObj,obj);
      }
   };
   this.removeSelected=function(){//删除选中的流程组件
      var num=this.nodes.length;
      var obj=null;var arr=new Array();
      var count=0;
      for(var i=0;i<num;i++){
         obj=this.nodes[i];
         if(obj.selected){
            obj.remove();
         }else{
            arr[count]=obj;
            count++;
         }
      }
      this.nodes=arr;num=this.lines.length;
      obj=null;arr=new Array();
      var count=0;
      for(var i=0;i<num;i++){
         obj=this.lines[i];
         if(obj.selected){
            obj.remove();
         }else{
            arr[count]=obj;count++;
         }
      }
      this.lines=arr;
      this.selectedObj=[];
   };
   this.setGroupArea=function(){
      var obj=document.getElementById('group');
      var maxWidth= -1;var maxHeight= -1;
      var num=this.nodes.length;
      var node=null;
      for(var i=0;i<num;i++){
         node=this.nodes[i];
         if(maxWidth<(node.left+node.width)){
            maxWidth=node.left+node.width;
         }
         if(maxHeight<(node.top+node.height)){
            maxHeight=node.top+node.height;
         }
      }
      if(maxHeight>document.body.clientHeight){
         obj.style.height=(maxHeight+this.bottomHeight)+'px';
         $(document.body).css({
        	   "overflow-x":"auto",
        	   "overflow-y":"auto"
        	 });
      }else{
         obj.style.height='100%';
      }
      if(maxWidth>document.body.clientWidth){
         obj.style.width=(maxWidth+this.rightWidth)+'px';
         $(document.body).css({
        	   "overflow-x":"auto",
        	   "overflow-y":"auto"
        	 });
      }else{
         obj.style.width='100%';
      }
   };
   this.toJson=function(){
      var jNodes=[];
      var nodeNum=this.nodes.length;
      for(var i=0;i<nodeNum;i++){
         GroupEvent.insertObjInArr(jNodes,this.nodes[i].toJson());
      }
      var jLines=[];
      var lineNum=this.lines.length;
      for(var i=0;i<lineNum;i++){
         GroupEvent.insertObjInArr(jLines,this.lines[i].toJson());
      }
      var json={id:this.id,name:this.name,count:this.count,nodes:jNodes,lines:jLines};
      return JSON.encode(json);
   };
   this.jsonTo=function(json){
      this.id=json.id;this.name=json.name;
      this.count=json.count;var jNodes=json.nodes;
      var nodeNum=jNodes.length;var node=null;
      for(var i=0;i<nodeNum;i++){
         switch(jNodes[i].shape){
            case 'img':{
                  node=new NodeImg();
                  break;
               }
               case 'oval':{
                  node=new NodeOval();
                  break;
               }case 'shape':{
                  if (jNodes[i].type=='Branch'){
                     node = new TriangleStart();
                  }else if(jNodes[i].type='Shrink'){
                     node = new TriangleEnd();
                  }
                  break;
               }
               default:
                  node=new Node();
                  break;
         }
         node.jsonTo(jNodes[i]);
         node.init();
      }
      var jLines=json.lines;
      var lineNum=jLines.length;
      var line=null;
      for(var i=0;i<lineNum;i++){
         switch(jLines[i].shape){
            default:line=new PolyLine();break;
         }
         line.jsonTo(jLines[i]);
         line.init();
         line.relink();
      }
   };
   this.setProp=function(selObj,flag){
      
   };
   this.eventStart=function(){
      if(this.ctrlKey){this.multiSelect=true;}
      var selNode=this.getEventNode('down');
      var selLine=this.getEventLine();
      if(!this.multiSelect){this.clearSelected();}
      if(selNode!=null){
         if (this.action == 'dblclick'){
            selNode.dblClick();
            this.clearSelected();
         }else{
            this.setProp(selNode,'n');
            if(!GroupEvent.isInArr(this.selectedObj,selNode)){
               if(!this.ctrlKey){
                  this.multiSelect=false;
                  this.clearSelected();
               }
               selNode.setSelected();
               this.selectedObj=GroupEvent.insertObjInArr(this.selectedObj,selNode);
            }else{
               if(this.ctrlKey){
                  selNode.clearSelected();
                  this.selectedObj=GroupEvent.removeObjInArr(this.selectedObj,selNode);
               }
            }
            if(this.lineFlag==null){
               this.action='nodedown';
            }else if(this.lineFlag!=null){
               this.action='drawline';this.drawMirrorLineFrom(selNode);
            }
         }
      }else if(selLine!=null){
         this.setProp(selLine[selLine.length-1],'l');
         for(var i=0;i<selLine.length;i++){
            if(!GroupEvent.isInArr(this.selectedObj,selLine[i])){
               if(!this.ctrlKey){this.multiSelect=false;}
               selLine[i].setSelected();
               this.selectedObj=GroupEvent.insertObjInArr(this.selectedObj,selLine[i]);
            }else{
               if(this.ctrlKey){
                  selLine[i].clearSelected();
                  this.selectedObj=GroupEvent.removeObjInArr(this.selectedObj,selLine[i]);
               }
            }
         }
         if(this.selectedLineTo.length>0){
            this.action='moveline';
            for(var i=0;i<this.selectedLineTo.length;i++){
               this.selectedLineTo[i].setMoveSelected();
            }
         }
         if(this.selectedLineFrom.length>0){
            this.action='moveline';
            for(var i=0;i<this.selectedLineFrom.length;i++){
               this.selectedLineFrom[i].setMoveSelected();
            }
         }
      }else{
         this.setProp(null,'help');
         this.action='blankdown';
         if(!this.ctrlKey){
            this.clearSelected();
            this.drawMirrorNodeStart();
         }
      }
   }
};

//工作流组件组事件集合
var GroupEvent={
   dblClick:function(){//双击事件
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass'); 
      Love.action = 'dblclick'; 
      Love.eventStart();
      Love.action = null;
      return false;
   },
   mouseDown:function(){//鼠标按下事件
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass');
      document.selection.empty();Love.point('down');
      Love.eventStart();
      return false;
   },
   mouseMove:function(){//鼠标移动事件
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass');
      if(Love.action!=null){
         switch(Love.action){
            case "nodedown":Love.moveSelectedObj();break;
            case "drawline":Love.drawMirrorLineTo(null);break;
            case "moveline":Love.moveLine(null);break;
            case "blankdown":Love.drawMirrorNode();break;
            default:
         }
      }
      return false;
   },
   mouseUp:function(){//鼠标弹起事件
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass');
      if(Love.action!=null){
         Love.point('up');var selNode=Love.getEventNode('up');
         switch(Love.action){
            case "nodedown":Love.moveSelectedObjEnd();break;
            case "drawline":Love.drawLineEnd(selNode);break;
            case "moveline":Love.moveLineEnd(selNode);break;
            case "blankdown":Love.drawMirrorNodeEnd();break;
            default:
         }
      }
      Love.setGroupArea();
      Love.action=null;
      return false;
   },
   keyDown:function(){//按键按下事件
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass');
      if(window.event.ctrlKey)Love.ctrlKey=true;
      switch(window.event.keyCode){
         case 46:Love.removeSelected();break;
         case 65:
            if(window.event.ctrlKey){
               Love.selectAll();
               Love.multiSelect=true;
               window.event.returnValue=false;
            }
            break;
         default:
      }
   },
   keyUp:function(){//按键放开事件
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass');
      Love.ctrlKey=false;
   },
   getX:function(x){//获取x坐标
      return(x+document.body.scrollLeft);
   },
   getY:function(y){//获取y坐标
      return(y+document.body.scrollTop);
   },
   getMouseX:function(){//获取鼠标事件发生处x坐标
      return window.event.clientX
   },
   getMouseY:function(){//获取鼠标事件发生处y坐标
      return window.event.clientY
   },
   insertObjInArr:function(arr,s){
      if(arr==null)arr=[];arr[arr.length]=s;return arr;
   },
   removeObjInArr:function(arr,s){
      var tArr=null;var count=0;
      if(arr!=null){
         tArr=[];var num=arr.length;
         for(var i=0;i<num;i++){
            if(arr[i]!=s){tArr[count]=arr[i];count++;}
         }
      }
      return tArr;
   },
   isInArr:function(arr,s){
      var res=false;
      if(arr!=null){
         var num=arr.length;
         for(var i=0;i<num;i++){
            if(arr[i]==s){res=true;break;}
         }
      }
      return res;
   }
}

//连接线（直线）
function Line(){
   this.id='line_';this.name='New Line';
   this.number= -1;this.type='line';
   this.shape='line';this.selected=false;
   this.fromX= -1;this.fromY= -1;
   this.toX= -1;this.toY= -1;
   this.textFlag=false;this.mirrorFlag=false;
   this.obj=null;this.strokeObj=null;
   this.textObj=null;this.fromObj=null;
   this.toObj=null;
   this.init=function(){//连接线初始化方法
      var groupObj=document.getElementById('group');
      var obj=document.createElement('v:line');
      groupObj.appendChild(obj);
      obj.title=this.name;obj.from='0,0';
      obj.to='0,0';obj.strokecolor='blue';
      obj.strokeweight='1';obj.filled='false';
      obj.style.position='absolute';
      obj.style.zIndex='2';
      obj.style.cursor='hand';this.obj=obj;
      var strokeObj=document.createElement('v:stroke');
      obj.appendChild(strokeObj);
      strokeObj.endArrow="Classic";
      this.strokeObj=strokeObj;
      if(this.textFlag){
         var textObj=document.createElement('v:textbox');
         textObj.inset='10pt,1pt,5pt,5pt';
         textObj.style.textAlign='center';
         textObj.style.verticalAlign='bottom';
         textObj.style.color='blue';
         textObj.style.fontSize='9pt';
         textObj.innerHTML=this.name;
         obj.appendChild(textObj);
         this.textObj=textObj;
      }
      if(!this.mirrorFlag){
         var Love=groupObj.getAttribute('bindClass');
         this.number=Love.getObjectNum();
         this.id=this.id+this.number;
         obj.id=this.id;Love.lines[Love.lines.length]=this;
      }
   };
   this.setFrom=function(x,y,obj){
      this.fromX=GroupEvent.getX(x);
      this.fromY=GroupEvent.getY(y);
      if(obj)this.fromObj=obj;
      this.obj.from=this.fromX+','+this.fromY;
   };
   this.setTo=function(x,y,obj){
      this.toX=GroupEvent.getX(x);
      this.toY=GroupEvent.getY(y);
      if(obj)this.toObj=obj;
      this.obj.to=this.toX+','+this.toY;
   };
   this.setDisplay=function(flag){
      this.obj.style.display=flag;
   };
   this.link=function(lineMirror){
      this.fromObj=lineMirror.fromObj;
      this.toObj=lineMirror.toObj;this.relink();
      this.fromObj.clearSelected();
   };
   this.relink=function(){
      var fromDots=this.fromObj.getDots();
      var toDots=this.toObj.getDots();
      var fromDotNum=fromDots.length;
      var toDotNum=toDots.length;
      var lineLen= -1;
      var fromDot;var toDot;
      for(var i=0;i<fromDotNum;i++){
         for(var j=0;j<toDotNum;j++){
            if(lineLen<0){
               lineLen=this.getLineLength(fromDots[i].x,fromDots[i].y,toDots[j].x,toDots[j].y);
               fromDot=fromDots[i];
               toDot=toDots[j];
            }else if(lineLen>this.getLineLength(fromDots[i].x,fromDots[i].y,toDots[j].x,toDots[j].y)){
               lineLen=this.getLineLength(fromDots[i].x,fromDots[i].y,toDots[j].x,toDots[j].y);
               fromDot=fromDots[i];toDot=toDots[j];
            }
         }
      }
      this.fromX=fromDot.x;
      this.fromY=fromDot.y;
      this.toX=toDot.x;
      this.toY=toDot.y;
      this.obj.from=this.fromX+','+this.fromY;
      this.obj.to=this.toX+','+this.toY;
   };
   this.getLineLength=function(x1,y1,x2,y2){
      return Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
   };
   this.pointInObj=function(x,y){
      var res=false;x=GroupEvent.getX(x);
      y=GroupEvent.getY(y);
      var x1=this.fromX;
      var x2=this.toX;
      var y1=this.fromY;
      var y2=this.toY;
      var x21=x2-x1;
      var y21=y2-y1;
      if(x21==0){
         res=(Math.abs(x-x1)<5)&&(Math.min(y1,y2)<=y)&&(Math.max(y1,y2)>=y);
      }else if(y21==0){
         res=(Math.abs(y-y1)<5)&&(Math.min(x1,x2)<=x)&&(Math.max(x1,x2)>=x);
      }else{
         res=(Math.min(y1,y2)<=y)&&(Math.max(y1,y2)>=y)&&(Math.min(x1,x2)<=x)&&(Math.max(x1,x2)>=x)&&((Math.abs(Math.floor((x21/y21)*(y-y1)+x1-x))<5)||(Math.abs(Math.floor((y21/x21)*(x-x1)+y1-y))<5));
      }
      return res;
   };
   this.pointInStroke=function(x,y){
      var res= -1;x=GroupEvent.getX(x);y=GroupEvent.getY(y);
      var x1=this.fromX;var x2=this.toX;
      var y1=this.fromY;var y2=this.toY;
      if((Math.abs(x2-x)<6)&&(Math.abs(y2-y)<6))res=0;if((Math.abs(x1-x)<6)&&(Math.abs(y1-y)<6))res=1;
      return res;
   };
   this.setSelected=function(){
      this.obj.strokecolor='green';
      if(this.textObj)this.textObj.style.color='green';
      this.selected=true;this.obj.style.zIndex='22';
   };
   this.move=function(){};
   this.moveEnd=function(){};
   this.setMoveSelected=function(){
      this.obj.strokecolor='red';
      if(this.textObj)this.textObj.style.color='green';
      this.selected=true;this.obj.style.zIndex='22';
   };
   this.clearSelected=function(){
      this.obj.strokecolor='blue';
      if(this.textObj)this.textObj.style.color='blue';
      this.selected=false;this.obj.style.zIndex='2';
   };
   this.remove=function(){
      var group=document.getElementById('group');
      group.removeChild(this.obj);
   }
}

//菜单条
function Menu(){
   this.id='menu';this.left=10;this.top=3;
   this.height=30;this.width=300;
   this.selected=false;this.obj=null;
   this.menuObj=null;this.x= -1;
   this.y= -1;
   this.img=new Array('start.gif','end.gif','node.gif','page-next.gif','page-last.gif','forward.gif','drop-yes.gif','delete.gif','grid.gif','save.gif');
   this.text=new Array('开始环节','结束环节','环节','分支','收缩','路径（直线）','路径（折线）','删除','网格','保存模版');
   this.action=new Array('MenuAction.start()','MenuAction.end()','MenuAction.nodeRect()','MenuAction.branch()','MenuAction.shrink()','if(MenuAction.line()) MenuAction.changeStyle(this)','if(MenuAction.polyline()) MenuAction.changeStyle(this)','MenuAction.remove()','MenuAction.grid()','MenuAction.save()');
   this.init=function(){//初始化菜单条
      var toolObj=document.getElementById('tool');
      var obj=document.createElement('div');
      toolObj.appendChild(obj);
      toolObj.appendChild(this.createMenu());
      obj.id='movebar';
      obj.setAttribute('bindClass',this);
      this.obj=obj;
      obj.onmousedown=MenuEvent.mouseDown;
      obj.onmousemove=MenuEvent.mouseMove;
      obj.onmouseup=MenuEvent.mouseUp;
      obj.style.position='absolute';
      obj.style.left=this.left;
      obj.style.top=this.top;
      var td=document.createElement('div');
      td.innerHTML='&nbsp;&nbsp;';
      obj.appendChild(td);
   };
   this.createMenu=function(){//创建菜单条
      var obj=document.createElement('div');
      this.menuObj=obj;obj.id=this.id;
      obj.style.position='absolute';
      obj.style.height=this.height;
      obj.style.left=this.left+8;
      obj.style.top=this.top;
      var tobj=document.createElement('table');
      obj.appendChild(tobj);
      var tb=document.createElement('tbody');
      tobj.appendChild(tb);
      var tr=document.createElement('tr');
      tb.appendChild(tr);var td=null;
      for(var i=0;i<this.img.length;i++){
         td=document.createElement('td');
         tr.appendChild(td);
         td.innerHTML='<img src="../../pmis-common/images/wfdesign/'+this.img[i]+'"><span onmousemove="MenuAction.over(this)" onmouseout="MenuAction.out(this)" onclick="'+this.action[i]+'">'+this.text[i]+'</span><img src="../../pmis-common/images/wfdesign/grid-blue-split.gif">';
      }
      return obj;
   };
   this.down=function(){
      var x=GroupEvent.getMouseX();
      var y=GroupEvent.getMouseY();
      x=GroupEvent.getX(x);
      y=GroupEvent.getY(y);
      this.x=x-this.obj.offsetLeft;
      this.y=y-this.obj.offsetTop;
      this.selected=true;
   };
   this.move=function(){//菜单条移动
      if(this.selected){
         var x=GroupEvent.getMouseX();
         var y=GroupEvent.getMouseY();
         this.left=GroupEvent.getX(x)-this.x;
         this.top=GroupEvent.getY(y)-this.y;
         this.obj.style.left=this.left+'px';
         this.obj.style.top=this.top+'px';
         this.menuObj.style.left=(this.left+8)+'px';
         this.menuObj.style.top=this.top+'px';
      }
   };
   this.up=function(){
      this.selected=false;
      if(this.left<10){
         this.left=10;
         this.obj.style.left=this.left+'px';
         this.menuObj.style.left=(this.left+8)+'px';
      }
      if(this.top<3){
         this.top=3;
         this.obj.style.top=this.top+'px';
         this.menuObj.style.top=this.top+'px';
      }
   }
};

//菜单对应事件集合
var MenuEvent={
   mouseDown:function(){
      var menu=document.getElementById('movebar');
      var menuClass=menu.getAttribute('bindClass');
      menu.setCapture();
      menuClass.down();
   },
   mouseMove:function(){
      var menu=document.getElementById('movebar');
      var menuClass=menu.getAttribute('bindClass');
      menuClass.move();return false;
   },
   mouseUp:function(){
      var menu=document.getElementById('movebar');
      var menuClass=menu.getAttribute('bindClass');
      menuClass.up();menu.releaseCapture();
      document.getElementById('group').focus();
   }
};

//具体菜单功能项实现
var MenuAction={   
   open:function(){},
   save:function(){//对应保存菜单项
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass');
      clipboardData.setData("Text",Love.toJson());
   },
   start:function(){//对应流程开始节点生成菜单项
      var n=new NodeOval();n.init();
   },
   end:function(){//对应流程结束节点生成菜单项
      var n=new NodeOval();
      n.setType('end');n.init();
   },
   nodeRect:function(){
      var n=new Node();n.init();
   },
   branch:function(){
      var n=new TriangleStart();n.init();
   },
   shrink:function(){
      var n = new TriangleEnd();n.init();
   },
   line:function(){
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass');
      if(Love.lineFlag==null){
         Love.lineFlag='line';
      }else if(Love.lineFlag=='line'){
         Love.lineFlag=null;
      }else{
         return false;
      }
      return true;
   },
   polyline:function(){
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass');
      if(Love.lineFlag==null){
         Love.lineFlag='polyline';
      }else if(Love.lineFlag=='polyline'){
         Love.lineFlag=null;
      }else{
         return false;
      }
      return true;
   },
   remove:function(){
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass');
      Love.removeSelected();
   },
   grid:function(){
      var obj=document.body.style.backgroundImage;
      if(obj=='')
         document.body.style.backgroundImage='url(../../pmis-common/images/wfdesign/bg.jpg)';
      else 
         document.body.style.backgroundImage='';
   },
   save:function(){
	   saveTemplateWF();
   },
   changeStyle:function(obj){
      if(obj.style.color=='')
         obj.style.color='#ffffff';
      else obj.style.color='';
   },
   over:function(obj){
      obj.style.backgroundColor='#A8D0F9';
   },
   out:function(obj){
      obj.style.backgroundColor='';
   }
}

//分支节点
function TriangleStart(){
   this.wfTaskName='';
   this.wfTaskInnerID='';
   this.descript='';
   this.id='branch_';
   this.name='New Branch';
   this.number=-1;
   this.type='Branch';
   this.shape='shape';
   this.property=null;
   this.left=288;
   this.top=134;
   this.width=20;
   this.height=20;
   this.mouseX= -1;
   this.mouseY= -1;
   this.strokeFlag=false;
   this.shadowFlag=true;
   this.textFlag=true;
   this.mirrorFlag=false;
   this.obj=null;
   this.shadowObj=null;
   this.textObj=null;
   this.strokeObj=null;
   this.init=function(){
      var groupObj=document.getElementById('group');
      var obj=document.createElement('v:shape');
      
      groupObj.appendChild(obj);
      obj.type = "#branchNode";
      obj.title=this.name;
      obj.style.position='absolute';
      obj.style.left=this.left;
      obj.style.top=this.top;
      obj.style.width=this.width;
      obj.style.height=this.height;
      obj.style.cursor='hand';
      obj.style.zIndex='1';
      obj.strokecolor='blue';
      obj.fillcolor='blue';
      obj.coordsize="2 2";
      obj.path="m 0,0 l 0,0,2,1,0,2 x e";
      obj.strokeweight='1';
      this.obj=obj;
      if(this.shadowFlag){
         var shadowObj=document.createElement('v:shadow');
         shadowObj.on='T';
         shadowObj.type='single';
         shadowObj.color='#b3b3b3';
         shadowObj.offset='3px,3px';
         obj.appendChild(shadowObj);
         this.shadowObj=shadowObj;
      }
      if(this.strokeFlag){
         var strokeObj=document.createElement('v:stroke');
         obj.appendChild(strokeObj);
         this.strokeObj=strokeObj;
      }
      
      if(!this.mirrorFlag){
         var Love=groupObj.getAttribute('bindClass');
         if(this.number<0){
            this.number=Love.getObjectNum();
            this.id=this.id+this.number;
            obj.id=this.id;
            this.name=this.id;
            this.obj.title=this.id;
         }
         Love.nodes[Love.nodes.length]=this;
      }
   };
   this.setDisplay=function(flag){
      this.obj.style.display=flag;
   };
   this.dblClick=function(){
      popup_task_page(this);
   };
   this.pointInObj=function(x,y){
      var res=false;x=GroupEvent.getX(x)
      ;y=GroupEvent.getY(y);
      var x1=this.left;
      var x2=x1+this.width;
      var y1=this.top;
      var y2=y1+this.height;
      if((x>=x1)&&(x<=x2)&&(y>=y1)&&(y<=y2)){
         this.mouseX=x;this.mouseY=y;
         this.x=x-this.obj.offsetLeft;
         this.y=y-this.obj.offsetTop;
         res=true;
      }
      return res;
   };
   this.move=function(x,y,mouseX,mouseY){
      this.left=GroupEvent.getX(x)-this.x-GroupEvent.getX(mouseX)+this.mouseX;
      this.top=GroupEvent.getY(y)-this.y-GroupEvent.getY(mouseY)+this.mouseY;
      this.obj.style.left=this.left+'px';
      this.obj.style.top=this.top+'px';
   };
   this.moveEnd=function(){
      this.x=0;this.y=0;this.mouseX=this.left;
      this.mouseY=this.top
   };
   this.setSelected=function(){
      this.shadowObj.color='green';
      this.obj.strokecolor='green';
      this.selected=true;
   };
   this.clearSelected=function(){
      this.shadowObj.color='#b3b3b3';
      this.obj.strokecolor='blue';
      this.selected=false;
   };
   this.remove=function(){
      var group=document.getElementById('group');
      group.removeChild(this.obj);
   };
   this.setLeft=function(n){
      this.left=n;this.obj.style.left=n;
   };
   this.setTop=function(n){
      this.top=n;this.obj.style.top=n;
   };
   this.setWidth=function(n){
      this.width=n;this.obj.style.width=n;
   };
   this.setHeight=function(n){
      this.height=n;this.obj.style.height=n;
   };
   this.getDots=function(){
      var l=this.left;var t=this.top;
      var w=this.width;var h=this.height;
      var dots=new Array();
      var dot;dots[dots.length]={x:l,y:t+h/2};
      dots[dots.length]={x:l+w,y:t+h/2};
      dots[dots.length]={x:l+w/2,y:t};
      dots[dots.length]={x:l+w/2,y:t+h};
      dots[dots.length]={x:l,y:t};
      dots[dots.length]={x:l+w,y:t};
      dots[dots.length]={x:l,y:t+h};
      dots[dots.length]={x:l+w,y:t+h};
      return dots;
   };   
   this.toJson=function(){
      var json={wfTaskName:this.wfTaskName,wfTaskInnerID:this.wfTaskInnerID,descript:this.descript,id:this.id,name:this.name,type:this.type,shape:this.shape,number:this.number,left:this.left,top:this.top,width:this.width,height:this.height,property:this.property};
      return json;
   };
   this.jsonTo=function(json){
      this.wfTaskName=json.wfTaskName;
      this.wfTaskInnerID=json.wfTaskInnerID;
      this.descript=json.descript;
      this.id=json.id;
      this.name=json.name;
      this.type=json.type;
      this.shape=json.shape;
      this.number=json.number;
      this.left=json.left;
      this.top=json.top;
      this.width=json.width;
      this.height=json.height;
      this.property=json.property;
   }
}

//收缩节点
function TriangleEnd(){
   this.wfTaskName='';
   this.wfTaskInnerID='';
   this.descript='';
   this.id='shrink_';
   this.name='New Shrink';
   this.number=-1;
   this.type='Shrink';
   this.shape='shape';
   this.property=null;
   this.left=288;
   this.top=134;
   this.width=20;
   this.height=20;
   this.mouseX= -1;
   this.mouseY= -1;
   this.strokeFlag=false;
   this.shadowFlag=true;
   this.textFlag=true;
   this.mirrorFlag=false;
   this.obj=null;
   this.shadowObj=null;
   this.textObj=null;
   this.strokeObj=null;
   this.init=function(){
      var groupObj=document.getElementById('group');
      var obj=document.createElement('v:shape');
      
      groupObj.appendChild(obj);
      obj.type = "#branchNode";
      obj.title=this.name;
      obj.style.position='absolute';
      obj.style.left=this.left;
      obj.style.top=this.top;
      obj.style.width=this.width;
      obj.style.height=this.height;
      obj.style.cursor='hand';
      obj.style.zIndex='1';
      obj.strokecolor='blue';
      obj.fillcolor='blue';
      obj.coordsize="2 2";
      obj.path="m 0,1 l 0,1,2,2,2,0 x e";
      obj.strokeweight='1';
      this.obj=obj;
      if(this.shadowFlag){
         var shadowObj=document.createElement('v:shadow');
         shadowObj.on='T';
         shadowObj.type='single';
         shadowObj.color='#b3b3b3';
         shadowObj.offset='3px,3px';
         obj.appendChild(shadowObj);
         this.shadowObj=shadowObj;
      }
      if(this.strokeFlag){
         var strokeObj=document.createElement('v:stroke');
         obj.appendChild(strokeObj);
         this.strokeObj=strokeObj;
      }
      
      if(!this.mirrorFlag){
         var Love=groupObj.getAttribute('bindClass');
         if(this.number<0){
            this.number=Love.getObjectNum();
            this.id=this.id+this.number;
            obj.id=this.id;
            this.name=this.id;
            this.obj.title=this.id;
         }
         Love.nodes[Love.nodes.length]=this;
      }
   };
   this.setDisplay=function(flag){
      this.obj.style.display=flag;
   };
   this.dblClick=function(){
      popup_task_page(this);
   };
   this.pointInObj=function(x,y){
      var res=false;x=GroupEvent.getX(x)
      ;y=GroupEvent.getY(y);
      var x1=this.left;
      var x2=x1+this.width;
      var y1=this.top;
      var y2=y1+this.height;
      if((x>=x1)&&(x<=x2)&&(y>=y1)&&(y<=y2)){
         this.mouseX=x;this.mouseY=y;
         this.x=x-this.obj.offsetLeft;
         this.y=y-this.obj.offsetTop;
         res=true;
      }
      return res;
   };
   this.move=function(x,y,mouseX,mouseY){
      this.left=GroupEvent.getX(x)-this.x-GroupEvent.getX(mouseX)+this.mouseX;
      this.top=GroupEvent.getY(y)-this.y-GroupEvent.getY(mouseY)+this.mouseY;
      this.obj.style.left=this.left+'px';
      this.obj.style.top=this.top+'px';
   };
   this.moveEnd=function(){
      this.x=0;this.y=0;this.mouseX=this.left;
      this.mouseY=this.top
   };
   this.setSelected=function(){
      this.shadowObj.color='green';
      this.obj.strokecolor='green';
      this.selected=true;
   };
   this.clearSelected=function(){
      this.shadowObj.color='#b3b3b3';
      this.obj.strokecolor='blue';
      this.selected=false;
   };
   this.remove=function(){
      var group=document.getElementById('group');
      group.removeChild(this.obj);
   };
   this.setLeft=function(n){
      this.left=n;this.obj.style.left=n;
   };
   this.setTop=function(n){
      this.top=n;this.obj.style.top=n;
   };
   this.setWidth=function(n){
      this.width=n;this.obj.style.width=n;
   };
   this.setHeight=function(n){
      this.height=n;this.obj.style.height=n;
   };
   this.getDots=function(){
      var l=this.left;var t=this.top;
      var w=this.width;var h=this.height;
      var dots=new Array();
      var dot;dots[dots.length]={x:l,y:t+h/2};
      dots[dots.length]={x:l+w,y:t+h/2};
      dots[dots.length]={x:l+w/2,y:t};
      dots[dots.length]={x:l+w/2,y:t+h};
      dots[dots.length]={x:l,y:t};
      dots[dots.length]={x:l+w,y:t};
      dots[dots.length]={x:l,y:t+h};
      dots[dots.length]={x:l+w,y:t+h};
      return dots;
   };   
   this.toJson=function(){
      var json={wfTaskName:this.wfTaskName,wfTaskInnerID:this.wfTaskInnerID,descript:this.descript,id:this.id,name:this.name,type:this.type,shape:this.shape,number:this.number,left:this.left,top:this.top,width:this.width,height:this.height,property:this.property};
      return json;
   };
   this.jsonTo=function(json){
      this.wfTaskName=json.wfTaskName;
      this.wfTaskInnerID=json.wfTaskInnerID;
      this.descript=json.descript;
      this.id=json.id;
      this.name=json.name;
      this.type=json.type;
      this.shape=json.shape;
      this.number=json.number;
      this.left=json.left;
      this.top=json.top;
      this.width=json.width;
      this.height=json.height;
      this.property=json.property;
   }
}

//普通任务节点
function Node(){
   this.wfTaskName='';
   this.wfTaskInnerID='';
   this.descript='';   
   this.id='node_';
   this.name='New Node';
   this.number= -1;
   this.type='node';
   this.shape='rect';
   this.property=null;
   this.selected=false;
   this.left=100;
   this.top=80;
   this.width=100;
   this.height=40;
   this.mouseX= -1;
   this.mouseY= -1;
   this.x= -1;
   this.y= -1;
   this.strokeFlag=false;
   this.shadowFlag=true;
   this.textFlag=true;
   this.mirrorFlag=false;
   this.obj=null;
   this.shadowObj=null;
   this.textObj=null;
   this.strokeObj=null;
   this.init=function(){
      var groupObj=document.getElementById('group');
      var obj=document.createElement('v:rect');
      groupObj.appendChild(obj);
      obj.title=this.name;
      obj.style.position='absolute';
      obj.style.left=this.left;
      obj.style.top=this.top;
      obj.style.width=this.width;
      obj.style.height=this.height;
      obj.style.cursor='hand';
      obj.style.zIndex='1';
      obj.strokecolor='blue';
      obj.fillcolor='yellow';
      obj.strokeweight='1';
      this.obj=obj;
      if(this.shadowFlag){
         var shadowObj=document.createElement('v:shadow');
         shadowObj.on='T';
         shadowObj.type='single';
         shadowObj.color='#b3b3b3';
         shadowObj.offset='5px,5px';
         obj.appendChild(shadowObj);
         this.shadowObj=shadowObj;
      }
      if(this.strokeFlag){
         var strokeObj=document.createElement('v:stroke');
         obj.appendChild(strokeObj);
         this.strokeObj=strokeObj;
      }
      if(this.textFlag){
         var textObj=document.createElement('v:textbox');
         textObj.inset='2pt,5pt,2pt,5pt';
         textObj.style.textAlign='center';
         textObj.style.color='blue';
         textObj.style.fontSize='9pt';
         textObj.innerHTML=this.name;
         obj.appendChild(textObj);
         this.textObj=textObj;
      }
      if(!this.mirrorFlag){
         var Love=groupObj.getAttribute('bindClass');
         if(this.number<0){
            this.number=Love.getObjectNum();
            this.id=this.id+this.number;
            obj.id=this.id;
            this.name=this.id;
            this.obj.title=this.id;
            this.textObj.innerHTML=this.id;
         }
         Love.nodes[Love.nodes.length]=this;
      }
   };
   this.setDisplay=function(flag){
      this.obj.style.display=flag;
   };
   this.dblClick=function(){
      popup_task_page(this);
   };
   this.pointInObj=function(x,y){
      var res=false;x=GroupEvent.getX(x)
      ;y=GroupEvent.getY(y);
      var x1=this.left;
      var x2=x1+this.width;
      var y1=this.top;
      var y2=y1+this.height;
      if((x>=x1)&&(x<=x2)&&(y>=y1)&&(y<=y2)){
         this.mouseX=x;this.mouseY=y;
         this.x=x-this.obj.offsetLeft;
         this.y=y-this.obj.offsetTop;
         res=true;
      }
      return res;
   };
   this.move=function(x,y,mouseX,mouseY){
      this.left=GroupEvent.getX(x)-this.x-GroupEvent.getX(mouseX)+this.mouseX;
      this.top=GroupEvent.getY(y)-this.y-GroupEvent.getY(mouseY)+this.mouseY;
      this.obj.style.left=this.left+'px';
      this.obj.style.top=this.top+'px';
   };
   this.moveEnd=function(){
      this.x=0;this.y=0;this.mouseX=this.left;
      this.mouseY=this.top
   };
   this.setSelected=function(){
      this.shadowObj.color='green';
      this.obj.strokecolor='green';
      this.textObj.style.color='green';
      this.selected=true;
   };
   this.clearSelected=function(){
      this.shadowObj.color='#b3b3b3';
      this.obj.strokecolor='blue';
      this.textObj.style.color='blue';
      this.selected=false;
   };
   this.remove=function(){
      var group=document.getElementById('group');
      group.removeChild(this.obj);
   };
   this.setLeft=function(n){
      this.left=n;this.obj.style.left=n;
   };
   this.setTop=function(n){
      this.top=n;this.obj.style.top=n;
   };
   this.setWidth=function(n){
      this.width=n;this.obj.style.width=n;
   };
   this.setHeight=function(n){
      this.height=n;this.obj.style.height=n;
   };
   this.getDots=function(){
      var l=this.left;var t=this.top;
      var w=this.width;var h=this.height;
      var dots=new Array();
      var dot;dots[dots.length]={x:l,y:t+h/2};
      dots[dots.length]={x:l+w,y:t+h/2};
      dots[dots.length]={x:l+w/2,y:t};
      dots[dots.length]={x:l+w/2,y:t+h};
      dots[dots.length]={x:l,y:t};
      dots[dots.length]={x:l+w,y:t};
      dots[dots.length]={x:l,y:t+h};
      dots[dots.length]={x:l+w,y:t+h};
      return dots;
   };
   this.toJson=function(){
      var json={wfTaskName:this.wfTaskName,wfTaskInnerID:this.wfTaskInnerID,descript:this.descript,id:this.id,name:this.name,type:this.type,shape:this.shape,number:this.number,left:this.left,top:this.top,width:this.width,height:this.height,property:this.property};
      return json;
   };
   this.jsonTo=function(json){
      this.wfTaskName=json.wfTaskName;
      this.wfTaskInnerID=json.wfTaskInnerID;
      this.descript=json.descript;
      this.id=json.id;this.name=json.name;
      this.type=json.type;
      this.shape=json.shape;
      this.number=json.number;
      this.left=json.left;
      this.top=json.top;
      this.width=json.width;
      this.height=json.height;
      this.property=json.property;
   }
}

//json数据格式解析和生成等
JSON=new function(){
   this.decode=function(){
      var filter,result,self,tmp;
      if($$("toString")){
         switch(arguments.length){
            case 2:self=arguments[0];filter=arguments[1];break;
            case 1:
               if($[typeof arguments[0]](arguments[0])===Function){
                  self=this;filter=arguments[0];
               }else 
                  self=arguments[0];
               break;
            default:self=this;break;
         };
         if(rc.test(self)){
            try{
               result=e("(".concat(self,")"));
               if(filter&&result!==null&&(tmp=$[typeof result](result))&&(tmp===Array||tmp===Object)){
                  for(self in result)
                     result[self]=v(self,result)?filter(self,result[self]):result[self];
               }
            }catch(z){}
         }else{
            throw new JSONError("bad data");
         }
      };
      return result;
   };
   this.encode=function(){
      var self=arguments.length?arguments[0]:this,result,tmp;
      if(self===null)
         result="null";
      else if(self!==undefined&&(tmp=$[typeof self](self))){
         switch(tmp){
            case Array:
               result=[];
               for(var i=0,j=0,k=self.length;j<k;j++){
                  if(self[j]!==undefined&&(tmp=JSON.encode(self[j])))
                     result[i++]=tmp;
               };
               result="[".concat(result.join(","),"]");
               break;
            case Boolean:result=String(self);break;
            case Date:
               result='"'.concat(self.getFullYear(),'-',d(self.getMonth()+1),'-',d(self.getDate()),'T',d(self.getHours()),':',d(self.getMinutes()),':',d(self.getSeconds()),'"');
               break;
            case Function:break;
            case Number:result=isFinite(self)?String(self):"null";break;
            case String:result='"'.concat(self.replace(rs,s).replace(ru,u),'"');break;
            default:
               var i=0,key;
               result=[];
               for(key in self){
                  if(self[key]!==undefined&&(tmp=JSON.encode(self[key])))
                     result[i++]='"'.concat(key.replace(rs,s).replace(ru,u),'":',tmp);
               };
               result="{".concat(result.join(","),"}");
               break;
         }
      };
      return result;
   };
   this.toDate=function(){
      var self=arguments.length?arguments[0]:this,result;
      if(rd.test(self)){
         result=new Date;
         result.setHours(i(self,11,2));
         result.setMinutes(i(self,14,2));
         result.setSeconds(i(self,17,2));
         result.setMonth(i(self,5,2)-1);
         result.setDate(i(self,8,2));
         result.setFullYear(i(self,0,4));
      }else if(rt.test(self))
         result=new Date(self*1000);
      return result;
   };
   var c={"\b":"b","\t":"t","\n":"n","\f":"f","\r":"r",'"':'"',"\\":"\\","/":"/"},
       d=function(n){return n<10?"0".concat(n):n},
       e=function(c,f,e){
         e=eval;delete eval;
         if(typeof eval==="undefined")eval=e;
         f=eval(""+c);
         eval=e;
         return f
         },
         i=function(e,p,l){
            return 1*e.substr(p,l)
         },
         p=["","000","00","0",""],
         rc=null,
         rd=/^[0-9]{4}\-[0-9]{2}\-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$/,
         rs=/(\x5c|\x2F|\x22|[\x0c-\x0d]|[\x08-\x0a])/g,
         rt=/^([0-9]+|[0-9]+[,\.][0-9]{1,3})$/,ru=/([\x00-\x07]|\x0b|[\x0e-\x1f])/g,
         s=function(i,d){return "\\".concat(c[d])},
         u=function(i,d){
            var n=d.charCodeAt(0).toString(16);
            return "\\u".concat(p[n.length],n)
         },
         v=function(k,v){
            return $[typeof result](result)!==Function&&(v.hasOwnProperty?v.hasOwnProperty(k):v.constructor.prototype[k]!==v[k])
         },
         $={"boolean":function(){
                        return Boolean
                      },
                      "function":function(){
                        return Function
                      },
                      "number":function(){
                        return Number
                      },
                      "object":function(o){
                        return o instanceof o.constructor?o.constructor:null
                      },"string":function(){return String},
                        "undefined":function(){return null}
                      },
         $$=function(m){
            function $(c,t){
               t=c[m];delete c[m];
               try{e(c)}catch(z){c[m]=t;return 1}
               };
               return $(Array)&&$(Object)
            };
            try{
               rc=new RegExp('^("(\\\\.|[^"\\\\\\n\\r])*?"|[,:{}\\[\\]0-9.\\-+Eaeflnr-u \\n\\r\\t])+?$')
            }catch(z){
               rc=/^(true|false|null|\[.*\]|\{.*\}|".*"|\d+|\d+\.\d+)$/
            }
};

//开始和结束节点
function NodeOval(){
   this.wfTaskName='';
   this.wfTaskInnerID='';
   this.descript='';
   
   this.id='control_';  
   this.name='New Node';   
   this.number= -1;
   this.type='start';
   this.shape='oval';
   this.property=null;
   this.selected=false;
   this.left=100;this.top=80;
   this.width=20;this.height=20;
   this.mouseX= -1;this.mouseY= -1;
   this.x= -1;this.y= -1;
   this.strokeFlag=false;
   this.shadowFlag=true;
   this.textFlag=false;
   this.mirrorFlag=false;
   this.obj=null;
   this.shadowObj=null;
   this.textObj=null;
   this.strokeObj=null;
   this.init=function(){
      var groupObj=document.getElementById('group');
      var obj=document.createElement('v:oval');
      groupObj.appendChild(obj);
      obj.title=this.name;
      obj.style.position='absolute';
      obj.style.left=this.left;
      obj.style.top=this.top;
      obj.style.width=this.width;
      obj.style.height=this.height;
      obj.style.cursor='hand';
      obj.style.zIndex='1';
      if(this.type=='start'){
         obj.fillcolor='#33CC00';
         obj.strokecolor='#33CC00';
      }else{
         obj.fillcolor='red';obj.strokecolor='red';
      }
      obj.strokeweight='1';
      this.obj=obj;
      if(this.shadowFlag){
         var shadowObj=document.createElement('v:shadow');
         shadowObj.on=false;
         shadowObj.type='single';
         shadowObj.color='#b3b3b3';
         shadowObj.offset='3px,3px';
         obj.appendChild(shadowObj);
         this.shadowObj=shadowObj;
      }
      if(this.strokeFlag){
         var strokeObj=document.createElement('v:stroke');
         obj.appendChild(strokeObj);this.strokeObj=strokeObj;
      }
      if(this.textFlag){
         var textObj=document.createElement('v:textbox');
         textObj.inset='2pt,5pt,2pt,5pt';
         textObj.style.textAlign='center';
         textObj.style.color='blue';
         textObj.style.fontSize='9pt';
         textObj.innerHTML=this.name;
         obj.appendChild(textObj);
         this.textObj=textObj;
      }
      if(!this.mirrorFlag){
         var Love=groupObj.getAttribute('bindClass');
         if(this.number<0){
            this.number=Love.getObjectNum();
            this.id=this.id+this.number;
            obj.id=this.id;this.name=this.id;
            this.obj.title=this.id;
            if(this.textObj)this.textObj.innerHTML=this.id;
         }
         Love.nodes[Love.nodes.length]=this;
      }
   };
   this.setType=function(type){
      if(type)this.type=type;
   };
   this.setDisplay=function(flag){
      this.obj.style.display=flag;
   };
   this.dblClick=function(){
      popup_task_page(this);
   };
   this.pointInObj=function(x,y){
      var res=false;x=GroupEvent.getX(x);
      y=GroupEvent.getY(y);
      var x1=this.left;
      var x2=x1+this.width;var y1=this.top;
      var y2=y1+this.height;
      var centerX=x1+this.width/2;
      var centerY=y1+this.height/2;
      var radius=this.width/2;
      var d=(x-centerX)*(x-centerX)+(y-centerY)*(y-centerY);
      if((radius*radius)>d){
         this.mouseX=x;this.mouseY=y;
         this.x=x-this.obj.offsetLeft;
         this.y=y-this.obj.offsetTop;
         res=true;
      }
      return res;
   };
   this.move=function(x,y,mouseX,mouseY){
      this.obj.style.left=GroupEvent.getX(x)-this.x-GroupEvent.getX(mouseX)+this.mouseX;
      this.obj.style.top=GroupEvent.getY(y)-this.y-GroupEvent.getY(mouseY)+this.mouseY;
      this.left=parseInt(this.obj.style.left);
      this.top=parseInt(this.obj.style.top);
   };
   this.moveEnd=function(){
      this.x=0;this.y=0;
      this.mouseX=this.left;
      this.mouseY=this.top
   };
   this.setSelected=function(){
      this.shadowObj.on='T';
      if(this.textObj)this.textObj.style.color='green';
      this.selected=true;
   };
   this.clearSelected=function(){
      this.shadowObj.on=false;
      if(this.textObj)this.textObj.style.color='blue';
      this.selected=false;
   };
   this.remove=function(){
      var group=document.getElementById('group');
      group.removeChild(this.obj);
   };
   this.setLeft=function(n){
      this.left=n;this.obj.style.left=n;
   };
   this.setTop=function(n){
      this.top=n;this.obj.style.top=n;
   };
   this.setWidth=function(n){
      this.width=n;this.obj.style.width=n;
   };
   this.setHeight=function(n){
      this.height=n;this.obj.style.height=n;
   };
   this.getDots=function(){
      var l=this.left;var t=this.top;
      var w=this.width;var h=this.height;
      var dots=new Array();
      var dot;
      dots[dots.length]={x:l,y:t+h/2};
      dots[dots.length]={x:l+w,y:t+h/2};
      dots[dots.length]={x:l+w/2,y:t};
      dots[dots.length]={x:l+w/2,y:t+h};
      return dots;
   };   
   this.toJson=function(){
      var json={wfTaskName:this.wfTaskName,wfTaskInnerID:this.wfTaskInnerID,descript:this.descript,id:this.id,name:this.name,type:this.type,shape:this.shape,number:this.number,left:this.left,top:this.top,width:this.width,height:this.height,property:this.property};
      return json;
   };
   this.jsonTo=function(json){
      this.wfTaskName=json.wfTaskName;
      this.wfTaskInnerID=json.wfTaskInnerID;
      this.descript=json.descript;
      this.id=json.id;this.name=json.name;
      this.type=json.type;this.shape=json.shape;
      this.number=json.number;
      this.left=json.left;
      this.top=json.top;
      this.width=json.width;
      this.height=json.height;
      this.property=json.property;
   }
}

//连接线（折线）
function PolyLine(){
   this.id='line_';
   this.name='New Line';
   this.number= -1;
   this.type='line';
   this.shape='polyline';
   this.property=null;
   this.selected=false;
   this.fromX= -1;
   this.fromY= -1;
   this.toX= -1;
   this.toY= -1;
   this.polyDot=[];
   this.textFlag=false;
   this.mirrorFlag=false;
   this.obj=null;
   this.strokeObj=null;
   this.textObj=null;
   this.fromObj=null;
   this.toObj=null;
   this.init=function(){
      var groupObj=document.getElementById('group');
      var obj=document.createElement('v:polyline');
      groupObj.appendChild(obj);
      obj.title=this.name;
      obj.points.value='0,20 50,0 100,20';
      obj.strokecolor='blue';
      obj.strokeweight='1';
      obj.filled='false';
      obj.style.position='absolute';
      obj.style.zIndex='2';
      obj.style.cursor='hand';
      this.obj=obj;
      var strokeObj=document.createElement('v:stroke');
      obj.appendChild(strokeObj);
      strokeObj.endArrow="Classic";
      this.strokeObj=strokeObj;
      if(this.textFlag){
         var textObj=document.createElement('v:textbox');
         textObj.inset='10pt,1pt,5pt,5pt';
         textObj.style.textAlign='center';
         textObj.style.verticalAlign='bottom';
         textObj.style.color='blue';
         textObj.style.fontSize='9pt';
         textObj.innerHTML=this.name;
         obj.appendChild(textObj);
         this.textObj=textObj;
      }
      if(!this.mirrorFlag){
         var Love=groupObj.getAttribute('bindClass');
         if(this.number<0){
            this.number=Love.getObjectNum();
            this.id=this.id+this.number;
            obj.id=this.id;
            this.name=this.id;
            this.obj.title=this.id;
         }
         Love.lines[Love.lines.length]=this;
      }
   };
   this.setFrom=function(x,y,obj){
      this.fromX=GroupEvent.getX(x);
      this.fromY=GroupEvent.getY(y);
      if(obj)this.fromObj=obj;
      this.polyDot=[];
      this.obj.points.value=this.getPointsValue();
   };
   this.setTo=function(x,y,obj){
      this.toX=GroupEvent.getX(x);
      this.toY=GroupEvent.getY(y);
      if(obj)this.toObj=obj;
      this.polyDot=[];
      this.obj.points.value=this.getPointsValue();
   };
   this.setDisplay=function(flag){
      this.obj.style.display=flag;
   };
   this.setShape=function(shape){
      if(shape)this.shape=shape;
   };
   this.link=function(lineMirror){
      this.fromObj=lineMirror.fromObj;
      this.toObj=lineMirror.toObj;
      this.relink();
      this.fromObj.clearSelected();
   };
   this.relink=function(){
      var fromDots=this.fromObj.getDots();
      var toDots=this.toObj.getDots();
      switch(this.shape){
         case 'polyline':this.relinkPolyline(fromDots,toDots);break;
         case 'line':this.relinkLine(fromDots,toDots);break;
         default:
      }
   };
   this.relinkLine=function(fromDots,toDots){
      var fromDotNum=fromDots.length;
      var toDotNum=toDots.length;
      var lineLen= -1;var fromDot;
      var toDot;
      for(var i=0;i<fromDotNum;i++){
         for(var j=0;j<toDotNum;j++){
            if(lineLen<0){
               lineLen=this.getLineLength(fromDots[i].x,fromDots[i].y,toDots[j].x,toDots[j].y);
               fromDot=fromDots[i];toDot=toDots[j];
            }else if(lineLen>this.getLineLength(fromDots[i].x,fromDots[i].y,toDots[j].x,toDots[j].y)){
               lineLen=this.getLineLength(fromDots[i].x,fromDots[i].y,toDots[j].x,toDots[j].y);
               fromDot=fromDots[i];
               toDot=toDots[j];
            }
         }
      }
      this.fromX=fromDot.x;
      this.fromY=fromDot.y;
      this.toX=toDot.x;
      this.toY=toDot.y;
      this.obj.points.value=this.getPointsValue();
   };
   this.relinkPolyline=function(fromDots,toDots){
      var fromDot;var toDot;
      this.polyDot=[];
      var xflag= -1;var yflag= -1;
      if((this.fromObj.left+this.fromObj.width)<toDots[2].x){
         fromDot=fromDots[1];toDot=toDots[0];
         xflag=0;
      }else if(this.fromObj.left>toDots[2].x){
         fromDot=fromDots[0];toDot=toDots[1];xflag=1;
      }else{
         fromDot=fromDots[1];toDot=toDots[1];xflag=2;
      }
      if(fromDots[0].y>toDots[3].y){
         if(xflag==2){
            fromDot=fromDots[0];toDot=toDots[0];
            this.polyDot[0]={x:Math.min(fromDot.x,toDot.x)-30,y:fromDot.y};
            this.polyDot[1]={x:Math.min(fromDot.x,toDot.x)-30,y:toDot.y};
         }else{
            toDot=toDots[3];
            this.polyDot[0]={x:toDot.x,y:fromDot.y};
         }
      }else if(fromDots[0].y<toDots[2].y){
         if(xflag==2){
            this.polyDot[0]={x:Math.max(fromDot.x,toDot.x)+30,y:fromDot.y};
            this.polyDot[1]={x:Math.max(fromDot.x,toDot.x)+30,y:toDot.y};
         }else{
            toDot=toDots[2];
            this.polyDot[0]={x:toDot.x,y:fromDot.y};
         }
      }else{
         if(xflag==0){
            fromDot=fromDots[2];toDot=toDots[2];
            this.polyDot[0]={x:fromDot.x,y:Math.min(fromDot.y,toDot.y)-10};
            this.polyDot[1]={x:toDot.x,y:Math.min(fromDot.y,toDot.y)-10};
         }else if(xflag==1){
            fromDot=fromDots[3];toDot=toDots[3];
            this.polyDot[0]={x:fromDot.x,y:Math.max(fromDot.y,toDot.y)+10};
            this.polyDot[1]={x:toDot.x,y:Math.max(fromDot.y,toDot.y)+10};
         }
      }
      this.fromX=fromDot.x;
      this.fromY=fromDot.y;
      this.toX=toDot.x;
      this.toY=toDot.y;
      this.obj.points.value=this.getPointsValue();
   };
   this.getPointsValue=function(){
      var res=this.fromX+','+this.fromY;
      for(var i=0;i<this.polyDot.length;i++){
         res+=' '+this.polyDot[i].x+','+this.polyDot[i].y;
      }
      res+=' '+this.toX+','+this.toY;
      return res;
   };
   this.getLineLength=function(x1,y1,x2,y2){
      return Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
   };
   this.pointInObj=function(x,y){
      var res=false;x=GroupEvent.getX(x);
      y=GroupEvent.getY(y);
      var x1=this.fromX;
      var y1=this.fromY;
      var x2=this.toX;
      var y2=this.toY;
      var x21=x2-x1;
      var y21=y2-y1;
      switch(this.shape){
         case 'polyline':
            for(var i=0;i<this.polyDot.length;i++){
               x2=this.polyDot[i].x;
               y2=this.polyDot[i].y;
               x21=x2-x1;y21=y2-y1;
               if(x21==0){
                  res=(Math.abs(x-x1)<5)&&(Math.min(y1,y2)<=y)&&(Math.max(y1,y2)>=y);
               }else if(y21==0){
                  res=(Math.abs(y-y1)<5)&&(Math.min(x1,x2)<=x)&&(Math.max(x1,x2)>=x);
               }
               if(res)
                  break;
               x1=x2;y1=y2;
            }
            if(!res){
               x2=this.toX;
               y2=this.toY;
               x21=x2-x1;y21=y2-y1;
               if(x21==0){
                  res=(Math.abs(x-x1)<5)&&(Math.min(y1,y2)<=y)&&(Math.max(y1,y2)>=y);
               }else if(y21==0){
                  res=(Math.abs(y-y1)<5)&&(Math.min(x1,x2)<=x)&&(Math.max(x1,x2)>=x);
               }
            }
            break;
         case 'line':
            if(x21==0){
               res=(Math.abs(x-x1)<5)&&(Math.min(y1,y2)<=y)&&(Math.max(y1,y2)>=y);
            }else if(y21==0){
               res=(Math.abs(y-y1)<5)&&(Math.min(x1,x2)<=x)&&(Math.max(x1,x2)>=x);
            }else{
               res=(Math.min(y1,y2)<=y)&&(Math.max(y1,y2)>=y)&&(Math.min(x1,x2)<=x)&&(Math.max(x1,x2)>=x)&&((Math.abs(Math.floor((x21/y21)*(y-y1)+x1-x))<5)||(Math.abs(Math.floor((y21/x21)*(x-x1)+y1-y))<5));
            }
            break;
         default:
      }
      return res;
   };
   this.pointInStroke=function(x,y){
      var res= -1;x=GroupEvent.getX(x);
      y=GroupEvent.getY(y);
      var x1=this.fromX;
      var x2=this.toX;
      var y1=this.fromY;
      var y2=this.toY;
      if((Math.abs(x2-x)<6)&&(Math.abs(y2-y)<6))res=0;
      if((Math.abs(x1-x)<6)&&(Math.abs(y1-y)<6))res=1;
      return res;
   };
   this.setSelected=function(){
      this.obj.strokecolor='green';
      if(this.textObj)this.textObj.style.color='green';
      this.selected=true;this.obj.style.zIndex='22';
   };
   this.move=function(){};
   this.moveEnd=function(){};
   this.setMoveSelected=function(){
      this.obj.strokecolor='red';
      if(this.textObj)this.textObj.style.color='green';
      this.selected=true;this.obj.style.zIndex='22';
   };
   this.clearSelected=function(){
      this.obj.strokecolor='blue';
      if(this.textObj)this.textObj.style.color='blue';
      this.selected=false;
      this.obj.style.zIndex='2';
   };
   this.remove=function(){
      var group=document.getElementById('group');
      group.removeChild(this.obj);
   };   
   this.toJson=function(){
      var json={id:this.id,name:this.name,type:this.type,shape:this.shape,number:this.number,from:this.fromObj.id,to:this.toObj.id,fromx:this.fromX,fromy:this.fromY,tox:this.toX,toy:this.toY,polydot:this.polyDot,property:this.property};
      return json;
   };
   this.jsonTo=function(json){
      this.id=json.id;this.name=json.name;
      this.type=json.type;this.shape=json.shape;
      this.number=json.number;
      this.fromX=json.fromx;this.fromY=json.fromy;
      this.toX=json.tox;this.toY=json.toy;
      this.polyDot=json.polydot;
      this.property=json.property;
      var group=document.getElementById('group');
      var Love=group.getAttribute('bindClass');
      var nodes=Love.nodes;var nodeNum=nodes.length;
      var node=null;
      for(var i=0;i<nodeNum;i++){
         node=nodes[i];
         if(node.id==json.from){
            this.fromObj=node;
         }else if(node.id==json.to){
            this.toObj=node;
         }
      }
   }
}