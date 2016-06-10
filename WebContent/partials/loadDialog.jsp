<div id ="loadModelModal" class="modal fade" tabindex="-1" role="dialog">
  	<div class="modal-dialog">
    	<div class="modal-content">
    	
      		<div class="modal-header">
        		<h4 class="modal-title">Load Model</h4>
      		</div>
      		
     		<div class="modal-body">
				<table class="table">
					<thead>
						<tr>
							<td>Filename</td>
							<td></td>
							<td></td>
						</tr>
					</thead>
					<tbody id="availableModels" ></tbody>
				</table>
     		 </div>
     		 
      		<div class="modal-footer">
        		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      		</div>
      		
    	</div>
    </div>
</div>

<div id ="saveModelModal" class="modal fade" tabindex="-1" role="dialog">
  	<div class="modal-dialog">
    	<div class="modal-content">
    	
      		<div class="modal-header">
        		<h4 class="modal-title">Save Model</h4>
      		</div>
      		
      		<div class="modal-body">
				<form class="form-horizontal">
				  	<div class="form-group">
				    	<label class="col-sm-2 control-label">Name</label>
				    	<div class="col-sm-10">
				      		<input type="text" class="form-control" id="modelName" placeholder="Identifier for this model">
				    	</div>
				  	</div>
				</form>
     		 </div>
     		 
      		<div class="modal-footer">
        		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      			<button type="button" class="btn btn-primary" onclick="saveModel()">Save</button>
      		</div>
      		
    	</div>
    </div>
</div>


<script type="text/javascript">

	function saveModel() {
		
		var modelName = $("#modelName").val();
		var model = myDiagram.model.toJSON();
		
		if(modelName.length > 0) {
			$.post("Model", {classModel : model, fileName: modelName}, function(response) {
				$("#saveModelModal").modal('hide');
			}).fail(function(request, status, error) {
				showErrorMessage(error)
			});
		} else {
			alert("Model identifier can not be empty");
		}
		
	}

	function loadModel(filename) {
		
		$.get( "Model", {action: "load", fileName: filename}, function(response) {
			classModel = response;
			sessionStorage.setItem('model', classModel);
			load();
			$("#modelName").val(filename);
			$("#loadModelModal").modal('hide');
		}).fail(function(request, status, error) {
			showErrorMessage(error)
		});
		
	}
	
	function deleteModel(filename) {
		
		$.get( "Model", {action: "delete", fileName: filename}, function(response) {
			$("#loadModelModal").modal('hide');
		}).fail(function(request, status, error) {
			showErrorMessage(error)
		});
		
	}

</script>