<nav class="navbar navbar-inverse">
	<div class="navbar-header">
		<button type="button" class="navbar-toggle collapsed"
			data-toggle="collapse" data-target="#navbar" aria-expanded="false"
			aria-controls="navbar">
			<span class="sr-only">Toggle navigation</span> <span class="icon-bar"></span>
			<span class="icon-bar"></span> <span class="icon-bar"></span>
		</button>
		<a class="navbar-brand" href=".">ORMapping</a>
	</div>
	<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
		<ul class="nav navbar-nav">
			<li><a href="editor.jsp">Benchmark</a></li>
		</ul>
	</div>
</nav>

<div id="errorMessageContainer" class="container" style="display: none;">
	<br />
	<p style="margin-bottom: 0px;">
		<strong>Attention: </strong><span id="errorMessage"></span>
	</p>
	<div style="text-align: center;">
		<a href="#" onclick="hideErrorMessage();return false;"><i
			class="fa fa-angle-up fa-2x"></i></a>
	</div>
</div>

<script type="text/javascript">
	function showErrorMessage(message) {
		$("#errorMessageContainer").removeClass("bg-success").addClass("bg-danger");
		$("#errorMessage").text(message);
		$("#errorMessageContainer").slideDown(800);
	}

	function showSuccessMessage(message) {
		$("#errorMessageContainer").removeClass("bg-danger").addClass("bg-success");
		$("#errorMessage").text(message);
		$("#errorMessageContainer").slideDown(800, function() {
			$("#errorMessageContainer").delay(2000).slideUp(800);
		});
	}

	function hideErrorMessage() {
		$("#errorMessageContainer").slideUp(800, function() {
			console.log("hide");
		});
	}
</script>