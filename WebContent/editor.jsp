<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="de.hpi.ormapping.servlets.AvailableDatabases "%>
<%@ page import="de.hpi.ormapping.servlets.AvailableTests "%>
<%@ page import="de.hpi.ormapping.tests.cases.TestCaseTemplate "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>

<title>ORMapping Performance Testing</title>
<jsp:include page="partials/header.jsp" />

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.1.0/styles/default.min.css">
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.1.0/highlight.min.js"></script>
<script src="https://code.highcharts.com/highcharts.js"></script>
<script src="https://code.highcharts.com/highcharts-more.js"></script>
<script src="js/editor.js"></script>
<script src="js/go.js"></script>

</head>

<body>

	<jsp:include page="partials/menu.jsp" />

	<div class="container-fluid" style="padding: 20px 0px;">

		<div class="row" style="padding: 0px 10px;">
		
			<div class="col-lg-9 col-md-12">
				<div class="panel panel-primary">
					<div class="panel-heading">Model Editor</div>
					<div class="panel-body">
						<jsp:include page="partials/hierarchyEditor.jsp" />
					</div>
					<div class="panel-footer">
						<button type="button" class="btn btn-primary" onclick="triggerSaveModelDialog();">Save Model</button>
						<button type="button" class="btn btn-primary" onclick="triggerLoadModelDialog();">Load Model</button>
						<button id="model_button" type="button" class="btn btn-primary pull-right" data-loading-text="Model generation ..." onclick="sendModelToServer();">Create Model</button>
					</div>
				</div>
			</div>

			<div class="col-lg-3 col-md-12">
			
				<div class="panel panel-primary">
					<div class="panel-heading">Available Databases</div>
				
					<ul class="list-group">
						
						<%
							StringBuilder builder = new StringBuilder();
							for (String databaseName : AvailableDatabases.fetchDatabaseResources()) {
								builder.append("<li class='list-group-item'>").append(databaseName);
								builder.append("<div class='material-switch pull-right'>");
								builder.append(String.format("<input id='adb_%1$s' name='availableDbs' type='checkbox' value='%1$s' />", databaseName));
								builder.append(String.format("<label for='adb_%1$s' class='label-success'></label>", databaseName));
								builder.append("</div>");
								builder.append("</li>");
							}
						%>
						<%=builder.toString()%>

					</ul>
					
				</div>
			
				<div class="panel panel-primary">
					<div class="panel-heading">Available Tests</div>
					
					<ul class="list-group">
						
						<%
							builder = new StringBuilder();
							for (TestCaseTemplate test : AvailableTests.fetchAvailableTests()) {
								builder.append("<li class='list-group-item'>");
								builder.append(String.format("<a href='#' onclick='fetchQueryTemplates(\"%1$s\");return false;'>%2$s</a>", test.getName(), test.getClass().getSimpleName()));
								builder.append("<div class='material-switch pull-right'>");
								builder.append(String.format("<input id='at_%1$s' name='availableTests' type='checkbox' value='%1$s' />", test.getName()));
								builder.append(String.format("<label for='at_%1$s' class='label-success'></label>", test.getClass().getSimpleName()));
								builder.append("</div>");
								builder.append("</li>");
							}
						%>
						<%=builder.toString()%>

					</ul>
			
					<div class="panel-footer">
						<button id="test_button" type="button" class="btn btn-primary" data-loading-text="Tests running ..." onclick="startTests();">Start Tests</button>
					</div>
				</div>
			</div>

		</div>

		<div class="row" style="padding: 0px 10px;">
		
			<div class="col-lg-12">
				<div class="panel panel-primary">
					<div class="panel-heading">Results</div>
					<div class="panel-body" style="padding:0">
						<jsp:include page="partials/results.jsp" />
					</div>
				</div>
			</div>
			
		</div>

	</div>


	<jsp:include page="partials/queryTemplateModal.jsp" />

	<jsp:include page="partials/dataGenProgress.jsp" />

	<jsp:include page="partials/loadDialog.jsp" />

	<jsp:include page="partials/footer.jsp" />

	<script type="text/javascript">
	
		var classModel;
		
		$(document).ready(function() {
			
			$("input:checkbox").each(function() {
				if(sessionStorage.getItem(this.id)) {
					this.checked = sessionStorage.getItem(this.id);
				}
			});
			
			$("input:checkbox").change(function() {
				if(this.checked) {
					sessionStorage.setItem(this.id, this.checked);
				} else {
					sessionStorage.removeItem(this.id);
				}
			});
			
			if (sessionStorage.model) {
				classModel = sessionStorage.model;
			} else {
				classModel = '{ "class": "go.TreeModel", "nodeDataArray": [ {"key":"1", "className":"class1", "instanceCount":"10000", "fields":[], "type":"{concrete}"} ] }';
			}

			initEditor();
		})

		function sendModelToServer() {
			
			var selectedDBs = [];

			$("input:checkbox[name=availableDbs]:checked").each(function() {
				selectedDBs.push($(this).val());
			});

			if (selectedDBs.length == 0) {
				alert("You have to select a database");
			} else {
				$("#model_button").button('loading');
				$('#test_button').prop('disabled', true);

				$.post("ModelConsumer", {classModel : classModel,dbs : JSON.stringify(selectedDBs)}, function(response) {
					checkDataGenStatus()
					$("#dataGenModal").modal();
				}).fail(function(request, status, error) {
					$("#model_button").button('reset');
					$('#test_button').prop('disabled', false);
					showErrorMessage(error)
				});
			}

		}

		function startTests() {
			
			var selectedDBs = [];
			$("input:checkbox[name=availableDbs]:checked").each(function() {
				selectedDBs.push($(this).val());
			});

			var selectedTests = [];
			$("input:checkbox[name=availableTests]:checked").each(function() {
				selectedTests.push($(this).val());
			});

			if (selectedDBs.length == 0 || selectedTests.length == 0) {
				alert("You have to select a database and a test case");
			} else {
				$("#test_button").button('loading');
				$('#model_button').prop('disabled', true);
				$.post("TestExecution", {classModel : classModel,dbs : JSON.stringify(selectedDBs),tests : JSON.stringify(selectedTests)}, function(response) {
					drawResultData(response)
				}).fail(function(request, status, error) {
					$("#test_button").button('reset');
					$('#model_button').prop('disabled', false);
					showErrorMessage(error)
				});
			}
		}

		function triggerSaveModelDialog() {
			var model = myDiagram.model.toJSON();
			$("#saveModelModal").modal();
		}
		
		function triggerLoadModelDialog() {
			$.get("Model", {}, function(response) {
				
				var html = '';
				for(var i in response) {
					html += '<tr>';
					html += '<td>'+response[i]+'</td>';
					html += '<td style="width: 100px"><button class="btn btn-primary btn-sm" style="width: 90px;" onclick="loadModel(\''+response[i]+'\')">Laden</button></td>';
					html += '<td style="width: 100px"><button class="btn btn-primary btn-sm" style="width: 90px;" onclick="deleteModel(\''+response[i]+'\')">Löschen</button></td>';
					html += '</tr>';				
				}
				
				$("#availableModels").empty().html(html);
				$("#loadModelModal").modal();
				
			}).fail(function(request, status, error) {
				showErrorMessage(error)
			});
		}
		
	</script>

</body>
</html>