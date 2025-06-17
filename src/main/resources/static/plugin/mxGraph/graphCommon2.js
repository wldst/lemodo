// Program starts here. Creates a sample graph in the
// DOM node with the specified ID. This function is invoked
// from the onLoad event handler of the document (see below).
function zoomInOut(graph){
	// Adds mouse wheel handling for zoom
	mxEvent.addMouseWheelListener(function(evt, up)
	{
		if (up)
		{
			graph.zoomIn();
		}
		else
		{
			graph.zoomOut();
		}

		mxEvent.consume(evt);
	});
}

function addToolbarItem(graph, toolbar, prototype, image)
{
	// Function that is executed when the image is dropped on
	// the graph. The cell argument points to the cell under
	// the mousepointer if there is one.
	var funct = function(graph, evt, cell, x, y)
	{
		graph.stopEditing(false);

		var vertex = graph.getModel().cloneCell(prototype);
		vertex.geometry.x = x;
		vertex.geometry.y = y;
			
		graph.addCell(vertex);
		graph.setSelectionCell(vertex);
	}
	
	// Creates the image which is used as the drag icon (preview)
	var img = toolbar.addMode(null, image, function(evt, cell)
	{
		var pt = this.graph.getPointForEvent(evt);
		funct(graph, evt, cell, pt.x, pt.y);
	});
	
	// Disables dragging if element is disabled. This is a workaround
	// for wrong event order in IE. Following is a dummy listener that
	// is invoked as the last listener in IE.
	mxEvent.addListener(img, 'mousedown', function(evt)
	{
		// do nothing
	});
	
	// This listener is always called first before any other listener
	// in all browsers.
	mxEvent.addListener(img, 'mousedown', function(evt)
	{
		if (img.enabled == false)
		{
			mxEvent.consume(evt);
		}
	});
				
	mxUtils.makeDraggable(img, graph, funct);
	
	return img;
}


function unReddo(graph){
	// Undo/redo
	var undoManager = new mxUndoManager();
	var listener = function(sender, evt)
	{
		undoManager.undoableEditHappened(evt.getProperty('edit'));
	};
	graph.getModel().addListener(mxEvent.UNDO, listener);
	graph.getView().addListener(mxEvent.UNDO, listener);
	
	document.body.appendChild(mxUtils.button('Undo', function()
	{
		undoManager.undo();
	}));
	
	document.body.appendChild(mxUtils.button('Redo', function()
	{
		undoManager.redo();
	}));
}

function domainDefine(att,graph){
	var url="/cd/graph/define/";
	if(att==null){
		openLayer(url,"新节点定义","define");
	}else{
		openLayer(url,att.name,att.label);
	}
}
function domainInfo(att,graph){
	var url="/cd/graph/info/";
	if(att==null){
		url="/cd/graph/define/";
		openLayer(url,"新节点定义","define");
	}else{
		openLayer(url,att.name,att.label);
	}
}
function objectManage(att,graph){
	var url="/cd/manage/";
	if(att.label){
		openLayer(url,att.name,att.label);
	}else{
		openLayer(url,att.name,att.id);
	}
	
}

function objectData(att){
	var instanceAtt={};
	instanceAtt['id']=att.id;
	if(att.value){
		instanceAtt['name']=att.value;
	} else if(att.name){
	instanceAtt['name']=att.name;
	}
	
	 
	openInstance(instanceAtt);
}

function objectManage2(att,graph){
	var url="/cd/manage/";
	if(att.label){
		openLayer2(url,att.name,att.label);
	}else{
		openLayer2(url,att.name,att.id);
	}	
}
function interfaceManage(atti){
	var url="/cd/interface/";
	openLayer(url,atti.name,atti.label);
}
function structInfo(att,graph){
	var genurl="/cd/graph/struct/";
	openLayer(genurl,att.name,att.id)
}

function treeInfo(att,graph){
	var genurl="/cd/graph/tree/";
	if(att.id.nodeValue){
		openLayer(genurl,att.name,att.id)
	}else{
		openLayer2(genurl,att.name,att.id)
	}
}


function getStructData(genurl,graph){
	var domains ={};
		 
	var formData={};//po.data;
		
	// Gets the default parent for inserting new cells. This
	// is normally the first child of the root (ie. layer 0).
	var parent = graph.getDefaultParent();
	// Note that these XML nodes will be enclosing the
	// mxCell nodes for the model cells in the output
	var doc = mxUtils.createXmlDocument();
	
	$.ajax({
	    type: "post",
	    url: genurl,
        dataType : "json",
        contentType : "application/json;charset=UTF-8",
        data: JSON.stringify(formData),
        success: function (ret) {
        	var startNode = ret.data.startNode;
        	var relations = ret.data.relations;
        	var relName = ret.data.relName;
        	// Gets the default parent for inserting new cells. This
			// is normally the first child of the root (ie. layer 0).
			var parent = graph.getDefaultParent();
							
			// Adds the root vertex of the tree
			graph.getModel().beginUpdate();
			try
			{
				var w = graph.container.offsetWidth;
				var swidth=60;
				if(startNode['name'].length>5){
					swidth=startNode['name'].length/5*60;
				}
				var doc = mxUtils.createXmlDocument();
				var node = doc.createElement(startNode['name']);
				attCopy(node,startNode);
				var root = graph.insertVertex(parent, startNode['id'], node, w/4 - 30, 20, swidth, 40);
				
				for(var reli in relations){
					var relationData = relations[reli];
					
					var relNode = graph.insertVertex(parent, reli, relName[reli], 0, 0, 60, 40);
					graph.insertEdge(parent, null, '', root, relNode);
					
//					var di = doc.createElement(reli);
					for(var key in relationData){
						var endData = relationData[key].endNode;
						var width=60;
						if(endData['name'].length>5){
							width=endData['name'].length/5*60;
						}
						var eNode = doc.createElement(endData['name']);
						attCopy(eNode,endData);
						
						var endi = graph.insertVertex(parent, endData['id'], eNode, 0, 0, width, 40);
						attCopy(endi,endData);
						graph.insertEdge(parent, null, '', relNode, endi);
					}
				}
			}
			finally
			{
				// Updates the display
				graph.getModel().endUpdate();
			}
	    },
	 	error:function (d) {
		  layer.alert(d.msg, {icon: 5})
		}
	});	
}

function attCopy(di,endData){
	var name = endData['name'];
//	if(name.length>10){
//		name= name.substring(0,3)+"..."+name.substring(name.length-5);
//	}
//	var di = doc.createElement(name);
	for(var key in endData){
		var ddv = endData[key];
		di.setAttribute(key, ddv);
	}
	return di;
}

function handleSubTree(doc,graph,parent,startNode,startData,relName){
	for(var reli in startData.relations){//插入关系
		var reliEndList = startData.relations[reli];
		
		var reilnode = graph.insertVertex(parent, reli, relName[reli], 0, 0, 60, 40);
		graph.insertEdge(parent, null, '', startNode, reilnode);
		
		for(var key in reliEndList){
			var endData = reliEndList[key].endNode;
			var width=60;
			if(endData['name'].length>5){
				width=endData['name'].length/5*60;
			}
			
			var di = doc.createElement(endData['name']);
			for(var key in endData){
				var ddv = endData[key];
				di.setAttribute(key, ddv);
			}
			var endNode = graph.insertVertex(parent, null, di, 0, 0, width, 40);
			
			for(var key in endData){
				if(key!=="relations"){
					var ddx = endData[key];
					endNode.setAttribute(key, ddx);
				}
			}
			
			graph.insertEdge(parent, null, '', reilnode, endNode);
			handleSubTree(doc,graph,parent,endNode,endData,relName);
		}
	}
}

function getTreeData(genurl,graph){
	var domains ={};
		 
	var formData={};//po.data;
		
	// Gets the default parent for inserting new cells. This
	// is normally the first child of the root (ie. layer 0).
	var parent = graph.getDefaultParent();
	// Note that these XML nodes will be enclosing the
	// mxCell nodes for the model cells in the output
	var doc = mxUtils.createXmlDocument();
	
	$.ajax({
	    type: "post",
	    url: genurl,
        dataType : "json",
        contentType : "application/json;charset=UTF-8",
        data: JSON.stringify(formData),
        success: function (ret) {
        	var startNode = ret.data.startNode;
        	var relations = ret.data.relations;
        	var relName = ret.data.relName;
        	// Gets the default parent for inserting new cells. This
			// is normally the first child of the root (ie. layer 0).
			var parent = graph.getDefaultParent();
							
			// Adds the root vertex of the tree
			graph.getModel().beginUpdate();
			try
			{
				var w = graph.container.offsetWidth;
				var swidth=60;
				if(startNode['name'].length>5){
					swidth=startNode['name'].length/5*60;
				}
//				var di = doc.createElement(startNode['name']);
//				var root = graph.insertVertex(parent, startNode['id'], startNode['name'], w/4 - 30, 20, swidth, 40);
//				
				var doc = mxUtils.createXmlDocument();
				var node = doc.createElement(startNode['name']);
				attCopy(node,startNode);
				var root = graph.insertVertex(parent, startNode['id'], node, w/4 - 30, 20, swidth, 40);
				
				//				console.log(relations);
				for(var reli in relations){
					var relationData = relations[reli];
					var relNode = graph.insertVertex(parent, reli, relName[reli], 0, 0, 60, 40);
					graph.insertEdge(parent, null, '', root, relNode);
					
//					var di = doc.createElement(reli);
					for(var key in relationData){
						var endData = relationData[key].endNode;
						if(!endData){
							continue;
						}
						var width=60;
						if(endData['name'].length>5){
							width=endData['name'].length/5*60;
						}
						var di = doc.createElement(endData['name']);
						for(var key in endData){
							if(key!=="relations"){
								var ddv = endData[key];
								di.setAttribute(key, ddv);
							}
						}
						var endNode = graph.insertVertex(parent, null, di, 0, 0, width, 40);
						
						graph.insertEdge(parent, null, '', relNode, endNode);
						handleSubTree(doc,graph,parent,endNode,endData,relName);
					}
				}
			}
			finally
			{
				// Updates the display
				graph.getModel().endUpdate();
			}
	    },
	 	error:function (d) {
		  layer.alert(d.msg, {icon: 5})
		}
	    });	
  }

function icon(it,x,y,state,operate,imgAddress,fun){
	var graph = state.view.graph;
	var img = mxUtils.createImage(editorImagePath+imgAddress);
	img.setAttribute('title', operate);
	img.style.position = 'absolute';
	img.style.cursor = 'pointer';
	img.style.width = '16px';
	img.style.height = '16px';
	img.style.left = (state.x + state.width+x) + 'px';
	img.style.top = (state.y+state.height+y) + 'px';
	
	mxEvent.addGestureListeners(img,
		mxUtils.bind(it, function(evt)
		{
		var att={}
			var value = state.cell.value;
			if(value!=null&&value!=undefined){
				 att.id= state.cell.id;
				 att.name=value;
				 if(value.indexOf('\n')<0||value.indexOf("]")<0){
				     att.label='module'
				 }else{
					 var label= value.split('\n')[1].substring(1,value.indexOf("]")-1)
					 att.label=label.substring(0,label.length-1)
				 }
				 
				
				 
				if(att){
					fun(att,graph);
				}else{
					fun(state.cell,graph);
				}
				
			}else{
				fun(state.cell,graph);
			}
			mxEvent.consume(evt);
			it.destroy();
		})
	);
	
	state.view.graph.container.appendChild(img);
	it.images.push(img);
}