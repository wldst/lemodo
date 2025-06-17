function menuContext(rubberband,graph){
	// Disables built-in context menu
	mxEvent.disableContextMenu(document.body);
	
	// Changes some default colors
	mxConstants.HANDLE_FILLCOLOR = '#99ccff';
	mxConstants.HANDLE_STROKECOLOR = '#0088cf';
	mxConstants.VERTEX_SELECTION_COLOR = '#00a8ff';
	
	
	rubberband.isForceRubberbandEvent = function(me)
	{
		return mxRubberband.prototype.isForceRubberbandEvent.apply(this, arguments) || mxEvent.isPopupTrigger(me.getEvent()); 
	}
					
	// Defines a new popup menu for region selection in the rubberband handler
	rubberband.popupMenu = new mxPopupMenu(function(menu, cell, evt)
	{
		var rect = new mxRectangle(rubberband.x, rubberband.y, rubberband.width, rubberband.height);
		
		menu.addItem('Show this', null, function()
	    {
			rubberband.popupMenu.hideMenu();
			var bounds = graph.getGraphBounds();
			mxUtils.show(graph, null, bounds.x - rubberband.x, bounds.y - rubberband.y, rubberband.width, rubberband.height);
	    });
	});

	var rubberbandMouseDown = rubberband.mouseDown;
	rubberband.mouseDown = function(sender, me)
	{
		this.popupMenu.hideMenu();
		rubberbandMouseDown.apply(this, arguments);
	};

	var rubberbandMouseUp = rubberband.mouseUp;
	rubberband.mouseUp = function(sender, me)
	{
		if (this.div != null && mxEvent.isPopupTrigger(me.getEvent()))
		{
			if (!graph.popupMenuHandler.isMenuShowing())
			{
				var origin = mxUtils.getScrollOrigin();
				this.popupMenu.popup(me.getX() + origin.x + 1, me.getY() + origin.y + 1, null, me.getEvent());
				this.reset();
			}
		}
		else
		{
			rubberbandMouseUp.apply(this, arguments);
		}
	};
}
		