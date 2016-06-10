<div id ="dataGenModal" class="modal fade" tabindex="-1" role="dialog">
  	<div class="modal-dialog">
    	<div class="modal-content">
    	
      		<div class="modal-header">
        		<h4 class="modal-title">Data Generation Progress</h4>
      		</div>
      		
     			<div class="modal-body">
				<div id="dataGenStates"></div>
     		 </div>
     		 
      		<div class="modal-footer">
        		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      		</div>
      		
    	</div>
    </div>
</div>

<script type="text/javascript">

	function checkDataGenStatus() {
		
		$.get( "ModelConsumer", function(response) {
			
			var allTerminated = true;
			
			var html = '<table class="table">'
			for (var i in response) {
				
				var status = "FINISHED";
				var result = response[i].resultMessage;
				if(response[i].statusCode == 1) {
					status = "RUNNING"
					allTerminated = false;
					var result = '<img src="img/buffer-loading.gif" alt="loading">'
				}
				
				html += '<tr><td>' + response[i].workerName + '</td><td style="width:100px">' + status + '</td><td style="width:100px">' + result + '</td></tr>'
			}
			html += '</table>'
			
			$("#dataGenStates").empty().html(html);
			
			if(!allTerminated) {
				setTimeout(checkDataGenStatus, 2000);
			} else {
				$("#model_button").button('reset');
				$('#test_button').prop('disabled', false);
			}
			
		}).fail(function(request, status, error) {
			showErrorMessage(error)
		});
		
	}

</script>