function showProp(d){
			var data= {};
	        if(d.id!=undefined){
	        	data=propMap['"'+d.id+'"'];
	        }else{
	        	data=propMap['"'+d.name+'"'];
	        }
			
			var attHTML="";
			for(var di in data){
				attHTML+=di+":"+data[di]+"<br/>";
			}
			return attHTML;
}


function updateStyle(state, hover)
{
	if (hover)
	{
		state.style[mxConstants.STYLE_FILLCOLOR] = '#99bb33';
	}
	
	// Sets rounded style for both cases since the rounded style
	// is not set in the default style and is therefore inherited
	// once it is set, whereas the above overrides the default value
	state.style[mxConstants.STYLE_ROUNDED] = (hover) ? '1' : '0';
	state.style[mxConstants.STYLE_STROKEWIDTH] = (hover) ? '4' : '1';
	state.style[mxConstants.STYLE_FONTSTYLE] = (hover) ? mxConstants.FONT_BOLD : '0';
};
function hoverStyle(graph){
	// Changes fill color to red on mouseover
	graph.addMouseListener(
	{
	    currentState: null,
	    previousStyle: null,
	    mouseDown: function(sender, me)
	    {
	        if (this.currentState != null)
	        {
	        	this.dragLeave(me.getEvent(), this.currentState);
	        	this.currentState = null;
	        }
	    },
	    mouseMove: function(sender, me)
	    {
	        if (this.currentState != null && me.getState() == this.currentState)
	        {
	            return;
	        }

	        var tmp = graph.view.getState(me.getCell());

	        // Ignores everything but vertices
	        if (graph.isMouseDown || (tmp != null && !
	            graph.getModel().isVertex(tmp.cell)))
	        {
	        	tmp = null;
	        }

	        if (tmp != this.currentState)
	        {
	            if (this.currentState != null)
	            {
	                this.dragLeave(me.getEvent(), this.currentState);
	            }

	            this.currentState = tmp;

	            if (this.currentState != null)
	            {
	                this.dragEnter(me.getEvent(), this.currentState);
	            }
	        }
	    },
	    mouseUp: function(sender, me) { },
	    dragEnter: function(evt, state)
	    {
	        if (state != null)
	        {
	        	this.previousStyle = state.style;
	        	state.style = mxUtils.clone(state.style);
	        	updateStyle(state, true);
//	        	var att = state.cell.value.attributes;
//				domainInfo(att.name,att.label);
	        	state.shape.apply(state);
	        	state.shape.redraw();
	        	
	        	if (state.text != null)
	        	{
	        		state.text.apply(state);
	        		state.text.redraw();
	        	}
	        }
	    },
	    dragLeave: function(evt, state)
	    {
	        if (state != null)
	        {
	        	state.style = this.previousStyle;
	        	updateStyle(state, false);
	        	if(state.shape!=undefined){
	        	state.shape.apply(state);
	        	state.shape.redraw();
	        	}
	        	
	        	if (state.text != null)
	        	{
	        		state.text.apply(state);
	        		state.text.redraw();
	        	}
	        }
	    }
	});
}

var iconTolerance = 20;
//Defines a new class for all icons
function mxIconSet(state)
{
	this.images = [];
	var it=this;
	// objectManage
	icon(it,0,-48,state,'管理数据','/plugin/mxGraph/editors/images/console.gif',objectManage);
	icon(it,0,-32,state,'档案','/plugin/mxGraph/editors/images/workplace.png',objectData);
	icon(it,0,-16,state,'树','/plugin/mxGraph/editors/images/tree.gif',treeInfo);
	icon(it,0,0,state,'结构','/images/console.gif',structInfo);
	// Icon2
	
	// Delete
	var img = mxUtils.createImage(editorPath+'images/delete.gif');
	img.setAttribute('title', 'Delete');
	img.style.position = 'absolute';
	img.style.cursor = 'pointer';
	img.style.width = '16px';
	img.style.height = '16px';
	img.style.left = (state.x + state.width) + 'px';
	img.style.top = (state.y - 16) + 'px';
	
	mxEvent.addGestureListeners(img,
		mxUtils.bind(this, function(evt)
		{
			// Disables dragging the image
			mxEvent.consume(evt);
		})
	);
	
	var graph = state.view.graph;
	mxEvent.addListener(img, 'click',
		mxUtils.bind(this, function(evt)
		{
			graph.removeCells([state.cell]);
			mxEvent.consume(evt);
			this.destroy();
		})
	);
	
	state.view.graph.container.appendChild(img);
	this.images.push(img);
};

mxIconSet.prototype.destroy = function()
{
	if (this.images != null)
	{
		for (var i = 0; i < this.images.length; i++)
		{
			var img = this.images[i];
			img.parentNode.removeChild(img);
		}
	}
	
	this.images = null;
};

function hoverIcon(graph){
	//Shows icons if the mouse is over a cell
	graph.addMouseListener(
	{
	    currentState: null,
	    currentIconSet: null,
	    mouseDown: function(sender, me)
	    {
	    	// Hides icons on mouse down
	    	if (this.currentState != null)
	    	{
	      		this.dragLeave(me.getEvent(), this.currentState);
	      		this.currentState = null;
	    	}
	    },
	    mouseMove: function(sender, me)
	    {
	    	if (this.currentState != null && (me.getState() == this.currentState ||
	    		me.getState() == null))
	    	{
	    		var tol = iconTolerance;
	    		var tmp = new mxRectangle(me.getGraphX() - tol,
	    			me.getGraphY() - tol, 2 * tol, 2 * tol);

	    		if (mxUtils.intersects(tmp, this.currentState))
	    		{
	    			return;
	    		}
	    	}
	    	
			var tmp = graph.view.getState(me.getCell());
	    	
	    	// Ignores everything but vertices
			if (graph.isMouseDown || (tmp != null && !graph.getModel().isVertex(tmp.cell)))
			{
				tmp = null;
			}

	      	if (tmp != this.currentState)
	      	{
	        	if (this.currentState != null)
	        	{
	          		this.dragLeave(me.getEvent(), this.currentState);
	        	}
	        
	    		this.currentState = tmp;
	        
	        	if (this.currentState != null)
	        	{
	          		this.dragEnter(me.getEvent(), this.currentState);
	        	}
	      	}
	    },
	    mouseUp: function(sender, me) { },
	    dragEnter: function(evt, state)
	    {
	    	if (this.currentIconSet == null)
	    	{
				this.currentIconSet = new mxIconSet(state);
	    	}
	    },
	    dragLeave: function(evt, state)
	    {
	    	if (this.currentIconSet != null)
	    	{
				this.currentIconSet.destroy();
				this.currentIconSet = null;
	    	}
	    }
	});
}


