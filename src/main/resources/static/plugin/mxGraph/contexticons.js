// Defines a subclass for mxVertexHandler that adds a set of clickable
// icons to every selected vertex.
function mxVertexToolHandler(state)
{
	mxVertexHandler.apply(this, arguments);
};

mxVertexToolHandler.prototype = new mxVertexHandler();
mxVertexToolHandler.prototype.constructor = mxVertexToolHandler;

mxVertexToolHandler.prototype.domNode = null;

mxVertexToolHandler.prototype.init = function()
{
	mxVertexHandler.prototype.init.apply(this, arguments);

	// In this example we force the use of DIVs for images in IE. This
	// handles transparency in PNG images properly in IE and fixes the
	// problem that IE routes all mouse events for a gesture via the
	// initial IMG node, which means the target vertices 
	this.domNode = document.createElement('div');
	this.domNode.style.position = 'absolute';
	this.domNode.style.whiteSpace = 'nowrap';
	
	// Workaround for event redirection via image tag in quirks and IE8
	function createImage(src)
	{
		if (mxClient.IS_IE && !mxClient.IS_SVG)
		{
			var img = document.createElement('div');
			img.style.backgroundImage = 'url(' + src + ')';
			img.style.backgroundPosition = 'center';
			img.style.backgroundRepeat = 'no-repeat';
			img.style.display = (mxClient.IS_QUIRKS) ? 'inline' : 'inline-block';
			
			return img;
		}
		else
		{
			return mxUtils.createImage(src);
		}
	};

	// Delete
	var img = createImage(editorImagePath+'/images/delete2.png');
	img.setAttribute('title', 'Delete');
	img.style.cursor = 'pointer';
	img.style.width = '16px';
	img.style.height = '16px';
	mxEvent.addGestureListeners(img,
		mxUtils.bind(this, function(evt)
		{
			// Disables dragging the image
			mxEvent.consume(evt);
		})
	);
	mxEvent.addListener(img, 'click',
		mxUtils.bind(this, function(evt)
		{
			this.graph.removeCells([this.state.cell]);
			mxEvent.consume(evt);
		})
	);
	this.domNode.appendChild(img);
	
	this.graph.container.appendChild(this.domNode);
	this.redrawTools();
};

mxVertexToolHandler.prototype.redraw = function()
{
	mxVertexHandler.prototype.redraw.apply(this);
	this.redrawTools();
};

mxVertexToolHandler.prototype.redrawTools = function()
{
	if (this.state != null && this.domNode != null)
	{
		var dy = (mxClient.IS_VML && document.compatMode == 'CSS1Compat') ? 20 : 4;
		this.domNode.style.left = (this.state.x + this.state.width - 56) + 'px';
		this.domNode.style.top = (this.state.y + this.state.height + dy) + 'px';
	}
};

mxVertexToolHandler.prototype.destroy = function(sender, me)
{
	mxVertexHandler.prototype.destroy.apply(this, arguments);

	if (this.domNode != null)
	{
		this.domNode.parentNode.removeChild(this.domNode);
		this.domNode = null;
	}
};