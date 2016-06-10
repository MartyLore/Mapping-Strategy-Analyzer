<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
	<head>
		<title>ORMapping Performance Testing</title>
		<jsp:include page="partials/header.jsp" />
	</head>

	<body>

		<jsp:include page="partials/menu.jsp" />
	
		<div class="jumbotron">
			<div class="container">
				<h1>ORMapper</h1>
				<p>Here you can test performance of different Object-Relational Mappings on various databases by various metrics.</p>
				<p>
					<a class="btn btn-primary btn-lg" href="learnMore.html" role="button">Learn more &raquo;</a>
				</p>
			</div>
		</div>
	
		<div class="container">
			<div class="row">
				<div class="col-md-4">
					<h2>Testing</h2>
					<p>Get straight into the topic by creating a class schema and run tests on it!</p>
					<p>
						<a class="btn btn-default" href="editor.jsp" role="button">Get started &raquo;</a>
					</p>
				</div>
			</div>
		</div>
	
		<jsp:include page="partials/footer.jsp" />

	</body>
</html>