var ACCEPTED_DATATYPES = new Array("INTEGER", "STRING", "VARCHAR(20)", "VARCHAR(10)", "CHAR(6)", "CHAR(5)", "CHAR(100)", "DOUBLE", "DATE", "FLOAT", "TIME", "BOOLEAN");
editView = false;
totalNumberOfClasses = 0;

function initEditor() {

	var layoutProps = {
		treeStyle : go.TreeLayout.StyleLastParents,
		arrangement : go.TreeLayout.ArrangementHorizontal,
		// properties for most of the tree:
		angle : 90,
		layerSpacing : 35,
		layerStyle : go.TreeLayout.LayerSiblings,
		// properties for the "last parents":
		alternateAngle : 90,
		alternateLayerSpacing : 35,
		alternateAlignment : go.TreeLayout.AlignmentBus,
		alternateNodeSpacing : 20
	}
	
	var prop = {
		contentAlignment : go.Spot.Center,
		// make sure users can only create trees
		validCycle : go.Diagram.CycleDestinationTree,
		// users can select only one part at a time
		maxSelectionCount : 1,
		// support editing the properties of the selected person in HTML
		"TextEdited" : onTextEdited,
		// enable undo & redo
		"undoManager.isEnabled" : true,
		allowCopy: false,
		allowDelete: false,
		layout : go.GraphObject.make(go.TreeLayout, layoutProps)
	};
	
	myDiagram = go.GraphObject.make(go.Diagram, "myDiagram", prop);

	function addChild(e, obj) {
		var clicked = obj.part;
		if (clicked !== null) {
			var thisClass = clicked.data;
			myDiagram.startTransaction("add class");
			var nextkey = Math.max((myDiagram.model.nodeDataArray.length + 1), totalNumberOfClasses);
			totalNumberOfClasses = nextkey +1;
			var newClass = {
				key : nextkey.toString(),
				className : "class" + nextkey,
				instanceCount : "10",
				fields : [],
				parent : thisClass.key,
				type : "{concrete}"
			};
			myDiagram.model.addNodeData(newClass);
			myDiagram.commitTransaction("add class");
		}
	}

	function removeClass(e, obj) {
		var clicked = obj.part;
		if (clicked !== null) {
			var thisClass = clicked.data;
			if(thisClass.parent !== undefined) {
				myDiagram.startTransaction("remove class");
				var iterator = clicked.findNodesOutOf();
				while(iterator.next()) {
					myDiagram.model.setDataProperty(iterator.value.data, "parent", thisClass.parent);
				}
				myDiagram.model.removeNodeData(thisClass);
				myDiagram.commitTransaction("remove class");
			} else {
				console.log("Cannot remove the root!");
			}
		}
	}	

	function removeAttribute(e, obj) {
		var row = obj.row;
		if (row !== null) {
			var thisClass = obj.part.data;
			myDiagram.startTransaction("remove attribute");
			myDiagram.model.removeArrayItem(thisClass.fields, row);
			myDiagram.commitTransaction("remove attribute");
		}
	}

	function addAttribute(e, obj) {
		var clicked = obj.part;
		if (clicked !== null) {
			var thisClass = clicked.data;
			myDiagram.startTransaction("add attribute");
			var newField = {
				attName : "newAttribute",
				attDistCount : "10",
				attType : "INTEGER"
			};
			myDiagram.model.insertArrayItem(thisClass.fields, -1, newField);
			myDiagram.commitTransaction("add attribute");
		}
	}

	function justNumbers(textblock, oldstr, newstr) {
		var result = /^\d+$/.test(newstr);
		return result;
	}
	
	function numbersError(textblock, oldstr, newstr) {
		alert("Please enter only Numbers!");
	}
	
	// This function provides a common style for most of the TextBlocks.
	// Some of these values may be overridden in a particular TextBlock.
	function textStyle() {
		return {
			font : "9pt  Segoe UI,sans-serif",
			stroke : "white"
		};
	}

	function makeContextMenuButton() {
		  var button =
		    go.GraphObject.make("Button",
		      { stretch: go.GraphObject.Horizontal });
		  // leave a two-pixel margin
		  var border = button.findObject("ButtonBorder");
		  if (border instanceof go.Shape) {
		    border.figure = "Rectangle";
		    border.spot1 = new go.Spot(0, 0, 2, 2);
		    border.spot2 = new go.Spot(1, 1, -2, -2);
		  }

		  return button;
	}
	
	// attribute type selection menu template
 	var attTypeMenu = go.GraphObject.make(go.Adornment, "Spot",
		go.GraphObject.make(go.Placeholder),
		go.GraphObject.make(go.Panel, "Vertical",
		{ alignment: go.Spot.Bottom, alignmentFocus: go.Spot.Top },
			go.GraphObject.make("ContextMenuButton",
				go.GraphObject.make(go.TextBlock, "STRING"),
				{ 
					click: function(e, obj) {
			            var node = obj.part.adornedPart;
			            obj.part.adornedObject.text = "STRING";
			            node.removeAdornment("ContextMenuOver");
		          	}
				}
		    ),
		    go.GraphObject.make("ContextMenuButton",
				go.GraphObject.make(go.TextBlock, "INTEGER"),
				{ 
					click: function(e, obj) {
			            var node = obj.part.adornedPart;
			            obj.part.adornedObject.text = "INTEGER";
			            node.removeAdornment("ContextMenuOver");
		          	}
				}
		    ),
		    go.GraphObject.make("ContextMenuButton",
				go.GraphObject.make(go.TextBlock, "DOUBLE"),
				{ 
					click: function(e, obj) {
			            var node = obj.part.adornedPart;
			            obj.part.adornedObject.text = "DOUBLE";
			            node.removeAdornment("ContextMenuOver");
		          	}
				}
		    ),
		    go.GraphObject.make("ContextMenuButton",
				go.GraphObject.make(go.TextBlock, "TIME"),
				{ 
					click: function(e, obj) {
			            var node = obj.part.adornedPart;
			            obj.part.adornedObject.text = "TIME";
			            node.removeAdornment("ContextMenuOver");
		          	}
				}
		    ),
		    go.GraphObject.make("ContextMenuButton",
				go.GraphObject.make(go.TextBlock, "DATE"),
				{ 
					click: function(e, obj) {
			            var node = obj.part.adornedPart;
			            obj.part.adornedObject.text = "DATE";
			            node.removeAdornment("ContextMenuOver");
		          	}
				}
		    ),
		    go.GraphObject.make("ContextMenuButton",
				go.GraphObject.make(go.TextBlock, "BOOLEAN"),
				{ 
					click: function(e, obj) {
			            var node = obj.part.adornedPart;
			            obj.part.adornedObject.text = "BOOLEAN";
			            node.removeAdornment("ContextMenuOver");
		          	}
				}
		    ),
		    go.GraphObject.make("ContextMenuButton",
				go.GraphObject.make(go.TextBlock, "FLOAT"),
				{ 
					click: function(e, obj) {
			            var node = obj.part.adornedPart;
			            obj.part.adornedObject.text = "FLOAT";
			            node.removeAdornment("ContextMenuOver");
		          	}
				}
		    )
	    )
	);

 	// attribute template for editing
	var fieldTemplateEdit = go.GraphObject.make(go.Panel, "TableRow", // this Panel is a row in the
	// containing Table
	{
		background : "transparent"
	}, go.GraphObject.make("Button", 
		{click: removeAttribute,
			margin : new go.Margin(5, 1, 10, 1)},
			go.GraphObject.make(go.Shape,
				{ name: "ButtonIcon",
				figure: "MinusLine",
				desiredSize: new go.Size(6, 6) })),
	go.GraphObject.make(go.TextBlock, {
		margin : new go.Margin(5, 1, 10, 10),
		column : 1,
		editable: true, 
		isMultiline: false,
		font : "bold 18px sans-serif",
		fromLinkable : false,
		toLinkable : false
		//textValidation: validAttName
	}, new go.Binding("text", "attName").makeTwoWay()), go.GraphObject.make(go.TextBlock, ":", {
		margin : new go.Margin(5, 1, 10, 10),
		column : 2,
		editable: false, 
		isMultiline: false,
		font : "18px sans-serif",
		fromLinkable : false,
		toLinkable : false
	}), go.GraphObject.make(go.TextBlock, {
		margin : new go.Margin(5, 1, 10, 10),
		column : 3,
		editable: false, 
		isMultiline: false,
		font : "18px sans-serif",
		fromLinkable : false,
		toLinkable : false,
		click: function(e, obj) {
        	attTypeMenu.adornedObject = obj;
        	attTypeMenu.mouseLeave = function(ev, cm) {
          		obj.part.removeAdornment("ContextMenuOver");
        	};
        	obj.part.addAdornment("ContextMenuOver", attTypeMenu);
      	}
	}, new go.Binding("text", "attType").makeTwoWay()), go.GraphObject.make(go.TextBlock, ":", {
		margin : new go.Margin(5, 1, 10, 10),
		column : 4,
		editable: false, 
		isMultiline: false,
		font : "18px sans-serif",
		fromLinkable : false,
		toLinkable : false
	}), go.GraphObject.make(go.TextBlock, {
		margin : new go.Margin(5, 1, 10, 10),
		column : 5,
		editable: true, 
		isMultiline: false,
		font : "18px sans-serif",
		fromLinkable : false,
		toLinkable : false,
		textValidation: justNumbers,
		errorFunction: numbersError
	}, new go.Binding("text", "attDistCount").makeTwoWay()));

	// attribute template for read-only
	var fieldTemplateNonEdit = go.GraphObject.make(go.Panel, "TableRow", // this Panel is a row in the
	// containing Table
	{
		background : "transparent"
	},
	go.GraphObject.make(go.TextBlock, {
		margin : new go.Margin(5, 1, 10, 10),
		column : 1,
		editable: false, 
		isMultiline: false,
		font : "bold 18px sans-serif",
		fromLinkable : false,
		toLinkable : false
	}, new go.Binding("text", "attName").makeTwoWay()), go.GraphObject.make(go.TextBlock, ":", {
		margin : new go.Margin(5, 1, 10, 10),
		column : 2,
		editable: false, 
		isMultiline: false,
		font : "18px sans-serif",
		fromLinkable : false,
		toLinkable : false
	}), go.GraphObject.make(go.TextBlock, {
		margin : new go.Margin(5, 1, 10, 10),
		column : 3,
		editable: false, 
		isMultiline: false,
		font : "18px sans-serif",
		fromLinkable : false,
		toLinkable : false
	}, new go.Binding("text", "attType").makeTwoWay()), go.GraphObject.make(go.TextBlock, ":", {
		margin : new go.Margin(5, 1, 10, 10),
		column : 4,
		editable: false, 
		isMultiline: false,
		font : "18px sans-serif",
		fromLinkable : false,
		toLinkable : false
	}), go.GraphObject.make(go.TextBlock, {
		margin : new go.Margin(5, 1, 10, 10),
		column : 5,
		editable: false, 
		isMultiline: false,
		font : "18px sans-serif",
		fromLinkable : false,
		toLinkable : false 
	}, new go.Binding("text", "attDistCount").makeTwoWay()));

	// class template for editing
	editableNodeTemplate = go.GraphObject.make(go.Node, "Vertical", { defaultStretch: go.GraphObject.Horizontal, selectionAdorned: false},
	new go.Binding("text", "name"),
	new go.Binding("layerName", "isSelected", function(sel) {
		return sel ? "Foreground" : "";
	}).ofObject(),
	go.GraphObject.make(go.Panel, "Auto", {
		stretch: go.GraphObject.Fill
	},
		go.GraphObject.make(go.Shape, "Rectangle", { fill: "#FFFFFF" }),
		go.GraphObject.make(go.Panel, "Vertical", 
		go.GraphObject.make(go.Panel, "Horizontal", 
			go.GraphObject.make(go.TextBlock, {
				row : 0,
				column : 0,
				margin : new go.Margin(10, 10, 10, 10),
				stroke : "black",
				editable: true, 
				isMultiline: false,
				textAlign : "center",
				minSize: new go.Size(10, 16),
				font : "bold 20pt sans-serif"
			}, new go.Binding("text", "className").makeTwoWay()), 
			go.GraphObject.make(go.TextBlock, ":", {
				margin : new go.Margin(10, 1, 10, 1),
				row: 0,
				column : 1,
				editable: false, 
				isMultiline: false,
				font : "18px sans-serif",
				stroke : "black",
				fromLinkable : false,
				toLinkable : false
			}), 
			go.GraphObject.make(go.TextBlock , {
				row : 0,
				column : 2,
				margin : new go.Margin(10, 10, 10, 10),
				stroke : "black",
				editable: true, 
				isMultiline: false,
				textAlign : "center",
				minSize: new go.Size(10, 16),
				font : "bold 20pt sans-serif",
				textValidation: justNumbers 
			}, new go.Binding("text", "instanceCount").makeTwoWay())),
			go.GraphObject.make(go.TextBlock, "{concrete}", {
				margin : new go.Margin(0, 10, 10, 10),
				stroke : "black",
				click: function(e, obj) {
					console.log(obj);
		        	obj.text = obj.text.includes("abstract") ? "{concrete}" : "{abstract}";
		        	if (obj.text == "{abstract}") {
		        		myDiagram.model.setDataProperty(obj.part.data, "instanceCount", 0);
		        	}
		      	},
				isMultiline: false,
				textAlign : "center",
				minSize: new go.Size(10, 16),
				font : "bold 12pt sans-serif"
			}, new go.Binding("text", "type").makeTwoWay())),
		go.GraphObject.make("Button", 
		{
			click: removeClass,
			alignment: go.Spot.TopRight
		},
			go.GraphObject.make(go.Shape,
			{ 
				name: "ButtonIcon",
				figure: "ThinX",
				desiredSize: new go.Size(6, 6) 
			})
		)
	),
	go.GraphObject.make(go.Panel, "Auto", 
		go.GraphObject.make(go.Shape, 
		{
			fill : "#FFFFFF"
		}), 
		go.GraphObject.make(go.Panel, "Vertical", 
		{
			stretch: go.GraphObject.Vertical
		}, 
			go.GraphObject.make(go.Panel, "Table", 
			{
				margin : new go.Margin(6, 10, 0, 3),
				defaultAlignment : go.Spot.Left,
				itemTemplate : fieldTemplateEdit
			}, new go.Binding("itemArray", "fields")),
			go.GraphObject.make(go.Panel, "Horizontal",
			{
				margin : new go.Margin(0, 0, 5, 3),
				stretch: go.GraphObject.Fill
			},  
				go.GraphObject.make(go.TextBlock , 
				{
					stroke : "blue",
					editable: false, 
					isMultiline: false,
					textAlign : "left",
					minSize: new go.Size(10, 16),
					font : "bold 14pt sans-serif",
					text: "+ new attribute",
					isUnderline: true,
					click: addAttribute
				})
    		)
    	)
    ), 
    // class attribute ending
	go.GraphObject.make(go.Panel, "Vertical", 
		go.GraphObject.make("Button", 
		{
			click: addChild
		},
			go.GraphObject.make(go.Shape, 
			{ 
				name: "ButtonIcon",
				figure: "ThinCross",
				desiredSize: new go.Size(10, 10) 
			})
		)
	)
); // end Node

	// class template for read-only
nonEditableNodeTemplate = go.GraphObject.make(go.Node, "Vertical", { defaultStretch: go.GraphObject.Horizontal}, 
	new go.Binding("text", "name"),
	new go.Binding("layerName", "isSelected", function(sel) {
		return sel ? "Foreground" : "";
	}).ofObject(),
	go.GraphObject.make(go.Panel, "Auto", {
		stretch: go.GraphObject.Fill
	},
		go.GraphObject.make(go.Shape, "Rectangle", { fill: "#FFFFFF" }),
		go.GraphObject.make(go.Panel, "Vertical", 
		go.GraphObject.make(go.Panel, "Horizontal", 
			go.GraphObject.make(go.TextBlock, {
				row : 0,
				column : 0,
				margin : new go.Margin(10, 10, 10, 10),
				stroke : "black",
				editable: false, 
				isMultiline: false,
				textAlign : "center",
				minSize: new go.Size(10, 16),
				font : "bold 20pt sans-serif"
			}, new go.Binding("text", "className").makeTwoWay()), 
			go.GraphObject.make(go.TextBlock, ":", {
				margin : new go.Margin(10, 1, 10, 1),
				row: 0,
				column : 1,
				editable: false, 
				isMultiline: false,
				font : "18px sans-serif",
				stroke : "black",
				fromLinkable : false,
				toLinkable : false
			}), 
			go.GraphObject.make(go.TextBlock , {
				row : 0,
				column : 2,
				margin : new go.Margin(10, 10, 10, 10),
				stroke : "black",
				editable: false, 
				isMultiline: false,
				textAlign : "center",
				minSize: new go.Size(10, 16),
				font : "bold 20pt sans-serif"
			}, new go.Binding("text", "instanceCount").makeTwoWay())
		),
		go.GraphObject.make(go.TextBlock, "{concrete}", {
			margin : new go.Margin(0, 10, 10, 10),
			stroke : "black",
			isMultiline: false,
			textAlign : "center",
			minSize: new go.Size(10, 16),
			font : "bold 12pt sans-serif"
		}, new go.Binding("text", "type").makeTwoWay()))
	),
	//class header ending
	go.GraphObject.make(go.Panel, "Auto", 
		go.GraphObject.make(go.Shape, 
		{
			fill : "#FFFFFF"
		}), 
		go.GraphObject.make(go.Panel, "Vertical", 
		{
			stretch: go.GraphObject.Vertical
		}, 
			go.GraphObject.make(go.Panel, "Table", 
			{
				margin : new go.Margin(6, 10, 0, 3),
				defaultAlignment : go.Spot.Left,
				itemTemplate : fieldTemplateNonEdit
			}, new go.Binding("itemArray", "fields"))
    	)
    )
    // class attribute ending
); //end Node

myDiagram.nodeTemplate = nonEditableNodeTemplate;

	// define the Link template
	myDiagram.linkTemplate = go.GraphObject.make(go.Link, go.Link.Orthogonal, {
		corner : 5,
		relinkableFrom : true,
		relinkableTo : true
	}, go.GraphObject.make(go.Shape, {
		strokeWidth : 3,
		stroke : "#000000"
	})); // the link
	// shape
	// read in the JSON-format data from the "classScheme" element
	load();
	//saveToJSON();
}

// This is called when the user has finished inline text-editing
function onTextEdited(e) {
	var tb = e.subject;
	if (tb === null || !tb.name) {
		return;
	}
	var data = tb.part.data;
	var model = myDiagram.model;
}

function validAttName (textblock, oldstr, newstr) {
    return newstr.toUpperCase() !== 'ID' && /^[a-z]+$/i.test(newstr);
};

// Show the diagram's model in JSON format
function saveToJSON() {
	classModel = myDiagram.model.toJSON();
	sessionStorage.setItem('model', classModel);
}

function load() {
	try {
		myDiagram.model = go.Model.fromJson(classModel);
	} catch(e) {
		alert("Reading curently only from JSON!");
	}
}
function changeView() {
	saveToJSON();
	editView = !editView;
	if(editView)
		myDiagram.nodeTemplate = editableNodeTemplate;
	else
		myDiagram.nodeTemplate = nonEditableNodeTemplate;
	load();
}

function relayout() {
	myDiagram.layout.doLayout(myDiagram);
}