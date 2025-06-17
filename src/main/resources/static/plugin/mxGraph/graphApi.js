var graph1;
// Program starts here. Creates a sample graph in the
// DOM node with the specified ID. This function is invoked
// from the onLoad event handler of the document (see below).
function main()
{
	// Checks if browser is supported
	if (!mxClient.isBrowserSupported())
	{
		// Displays an error message if the browser is
		// not supported.
		mxUtils.error('Browser is not supported!', 200, false);
	}
	else
	{
		// Defines an icon for creating new connections in the connection handler.
		// This will automatically disable the highlighting of the source vertex.
		mxConnectionHandler.prototype.connectImage = new mxImage(editorPath+'images/connector.gif', 16, 16);
		
		// Creates the div for the toolbar
		var tbContainer = document.createElement('div');
		tbContainer.style.position = 'absolute';
		tbContainer.style.overflow = 'hidden';
		tbContainer.style.padding = '2px';
		tbContainer.style.left = '0px';
		tbContainer.style.top = '0px';
		tbContainer.style.width = '40px';
		tbContainer.style.bottom = '0px';
		
		document.body.appendChild(tbContainer);
	
		// Creates new toolbar without event processing
		var toolbar = new mxToolbar(tbContainer);
		toolbar.enabled = false
		
		// Creates the div for the graph
		var container = document.createElement('div');
		container.style.position = 'absolute';
		container.style.overflow = 'hidden';
		container.style.left = '40px';
		container.style.top = '0px';
		container.style.right = '0px';
		container.style.bottom = '0px';
		container.style.background = 'url("'+editorPath+'images/grid.gif")';

		document.body.appendChild(container);
		
		// Workaround for Internet Explorer ignoring certain styles
		if (mxClient.IS_QUIRKS)
		{
			document.body.style.overflow = 'hidden';
			new mxDivResizer(tbContainer);
			new mxDivResizer(container);
		}

		// Creates the model and the graph inside the container
		// using the fastest rendering available on the browser
		var model = new mxGraphModel();
		var graph = new mxGraph(container, model);
		hoverStyle(graph);
		hoverIcon(graph);
		zoomInOut(graph);
//		unReddo(graph)
		// Enables new connections in the graph
		graph.setConnectable(true);
		graph.connectionHandler.createTarget = true;
		graph.setMultigraph(false);
		
		// Disables basic selection and cell handling
//		graph.setEnabled(false);

		// Changes the default style for vertices "in-place"
		// to use the custom shape.
		var style = graph.getStylesheet().getDefaultVertexStyle();
//		style[mxConstants.STYLE_SHAPE] = 'box';
		
		// Adds a spacing for the label that matches the
		// extrusion size
		style[mxConstants.STYLE_SPACING_TOP] = BoxShape.prototype.extrude;
		style[mxConstants.STYLE_SPACING_RIGHT] = BoxShape.prototype.extrude;
		
		// Adds a gradient and shadow to improve the user experience
		style[mxConstants.STYLE_GRADIENTCOLOR] = '#FFFFFF';
		style[mxConstants.STYLE_SHADOW] = true;
		
		graph.createHandler = function(state)
		{
			if (state != null &&
				this.model.isVertex(state.cell))
			{
				return new mxVertexToolHandler(state);
			}

			return mxGraph.prototype.createHandler.apply(this, arguments);
		};
		

		// Stops editing on enter or escape keypress
		var keyHandler = new mxKeyHandler(graph);
		var rubberband = new mxRubberband(graph);
		
		menuContext(rubberband,graph);
		var addVertex = function(icon, w, h, style)
		{
			var vertex = new mxCell(null, new mxGeometry(0, 0, w, h), style);
			vertex.setVertex(true);
		
			var img = addToolbarItem(graph, toolbar, vertex, icon);
			img.enabled = true;
			
			graph.getSelectionModel().addListener(mxEvent.CHANGE, function()
			{
				var tmp = graph.isSelectionEmpty();
				mxUtils.setOpacity(img, (tmp) ? 100 : 20);
				img.enabled = tmp;
			});
		};
		
		addVertex(editorPath+'images/rectangle.gif', 100, 40, 'shape=box');
		addVertex(editorPath+'images/rounded.gif', 100, 40, 'shape=rounded');
		addVertex(editorPath+'images/ellipse.gif', 40, 40, 'shape=ellipse');
		addVertex(editorPath+'images/rhombus.gif', 40, 40, 'shape=rhombus');
		addVertex(editorPath+'images/triangle.gif', 40, 40, 'shape=triangle');
		addVertex(editorPath+'images/cylinder.gif', 40, 40, 'shape=cylinder');
		addVertex(editorPath+'images/actor.gif', 30, 40, 'shape=actor');
		
		queryDomain(graph);
		//Overrides method to provide a cell label in the display
		graph.convertValueToString = function(cell)
		{
			if (mxUtils.isNode(cell.value))
			{
				if (cell.value.nodeName.toLowerCase() == 'po')
				{
					var firstName = cell.getAttribute('name', '');
					return firstName;
				}
				else if (cell.value.nodeName.toLowerCase() == 'knows')
				{
					return cell.value.nodeName + ' (Since '
							+  cell.getAttribute('since', '') + ')';
				}

			}

			return '';
		};
	}
	
	// Adds an option to view the XML of the graph
//	document.body.appendChild(mxUtils.button('View XML', function()
//	{
//		var encoder = new mxCodec();
//		var node = encoder.encode(graph.getModel());
//		mxUtils.popup(mxUtils.getXml(node), true);
//	}));
}

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

function queryDomain(graph){
	var domains ={};
	var genurl = MODULE_NAME+"/metadata/query";
		 
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
        	var dataArray = ret.data
			for(var index in dataArray){
				var data = dataArray[index];
				var di = doc.createElement('po');
				for(var key in data){
					var dd = data[key];
					di.setAttribute(key, dd);
				}
				domains[index]=di;
			}
        	// Adds cells to the model in a single step
			graph.getModel().beginUpdate();
			try
			{
				var x1=0,y1=0,x2=0,y2=0;
				for(var di in  domains){
					x1=x1+40;
					var v1 = graph.insertVertex(parent, null, domains[di], x1, x1, 80, 30);
					
				}
				//var e1 = graph.insertEdge(parent, null, relation, v1, v2);
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