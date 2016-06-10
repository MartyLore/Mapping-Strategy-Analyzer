<div id ="queryTemplateModal" class="modal fade" tabindex="-1" role="dialog">
  	<div class="modal-dialog modal-lg">
    	<div class="modal-content">
    	
      		<div class="modal-header">
        		<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        		<h4 id="queryTemplateTitle" class="modal-title">Query Templates</h4>
      		</div>
      		
     			<div class="modal-body">
				<div id="queryTemplateContainer"></div>
     		 </div>
     		 
      		<div class="modal-footer">
        		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      		</div>
      		
    	</div>
    </div>
</div>

<script type="text/javascript">
	function fetchQueryTemplates(testCase) {
		
		$("#queryTemplateTitle").empty().html('Template for ' + testCase);
		
		$.post( "QueryTemplates", { classModel: classModel, testCase: testCase }, function(response) {
			
			var html = '<ul class="nav nav-tabs nav-justified">';
			var activeClass = 'active';
			for (var db in response) {
				html += '<li role="presentation" class="'+activeClass+'"><a href="#'+db+'" aria-controls="'+db+'" role="tab" data-toggle="pill">'+db+'</a></li>'
				activeClass = "";
			}
			html += "</ul>"
			
			$("#queryTemplateContainer").empty().html(html);
			
			html = '<div class="tab-content">'
			activeClass = 'active';
			for (var db in response) {
				html += '<div role="tabpanel" class="tab-pane '+activeClass+'" id="'+db+'">';
				activeClass = "";
				for (var className in response[db]) {
					html += '<ul>'
					html += "<li><h5>"+className+"</h5></li>";
					html += "<table class='table'>"
					for (var strategy in response[db][className]) {
						html += '<tr><td><strong>'+strategy+'</strong><pre><code class="sql">'+response[db][className][strategy]+'</code></pre></td></tr>';
					}
					html += '</table></ul>'
				}
				html += '</div>'
			}
			html += "</div>"
	
			$("#queryTemplateContainer").append(html);
			
			$('pre code').each(function(i, block) {
			    hljs.highlightBlock(block);
			});
			
			$("#queryTemplateModal").modal();
								
		}).fail(function(request, status, error) {
			showErrorMessage(error)
		});
	}
</script>