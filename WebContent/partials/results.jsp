<div class="container-fluid" style="padding: 10px">
	<div id="databases" class="col-xs-1" style="padding:0"></div>
	<div id="resultData" class="col-xs-11 table-responsive"></div>
	<div id="resultGraph" class="col-xs-offset-2 col-xs-10 table-responsive">
		<table style="margin: auto;">
			<tr id="graphContainer"></tr>
		</table>
	</div>
</div>

<script type="text/javascript">

	var template;
	var results = [];
	
	function drawResultData(resultTemplate) {
		template = resultTemplate;
		results = [];
		
		renderList();
		
		renderTable();
		renderCharts();

		setTimeout(fetchResults, 500);
	}

	function renderList() {

		var html = ""
		$("input:checkbox[name=availableDbs]:checked").each(function() {
			html += '<div class="checkbox">'
			html += '<label><input type="checkbox" name="resultSelectedDBs" value="'+ $(this).val()+ '" checked>'+ $(this).val() + '</label>'
			html += '</div>'

		});
		$("#databases").empty().html(html)

		$("input:checkbox[name=resultSelectedDBs]").each(function() {
			$(this).change(function() {
				renderTable();
				renderCharts();
			});
		});

	}

	function renderTable() {

		var selectedDbs = [];
		$("input:checkbox[name=resultSelectedDBs]:checked").each(function() {
			selectedDbs.push($(this).val());
		});
		
		var html = '<table id="resultTable" class="table table-bordered" style="text-align:center;">'
		html += '<thead>'

		// draw first header line (test cases)
		html += '<tr>'
		html += '<td></td>' // strategy column
		for ( var i in template.testCases) {
			if (template.testCases[i] == 'MemoryConsumption') {
				html += '<td colspan="' + (selectedDbs.length * template.strategies.length) + '" style="border-left:3px solid black;"><strong>' + template.testCases[i] + '</strong> (in kB)</td>'
			} else {
				html += '<td colspan="' + (selectedDbs.length * template.strategies.length) + '" style="border-left:3px solid black;"><strong>' + template.testCases[i] + '</strong> (in ms)</td>'
			}
		}
		html += '</tr>'

		// draw second header line (databases)
		if (selectedDbs.length > 1) {
			html += '<tr>'
			html += '<td></td>' // strategy column
			for ( var i in template.testCases) {
				for ( var j in selectedDbs) {
					html += '<td colspan="'+template.strategies.length+'" style="border-left:3px solid black;">' + selectedDbs[j] + '</td>'
				}
			}
			html += '</tr>'
		}

		// draw third header line (strategies)
		html += '<tr>'
		html += '<td></td>' // strategy column
		for ( var i in template.testCases) {
			for ( var j in selectedDbs) {
				var borderFormat = "border-left: 3px solid black;";
				for ( var k in template.strategies) {
					html += '<td width="80px" style="'+borderFormat+'">' + template.strategies[k] + '</td>'
					borderFormat = "";
				}
			}
		}
		html += '</tr>'
		html += '</thead>'

		// draw table body
		html += '<tbody id="resultTableBody">';
		
		for(var i in template.classes) {
			html += '<tr class="measurements '+template.classes[i]+'">'
			html += '<td><strong>' + template.classes[i] + '</strong></td>';
			for (var j in template.testCases) {
				for (var k in selectedDbs) {
					
					var borderFormat = "border-left: 3px solid black;";
					for (var l in template.strategies) {
						html += '<td orm-type="value" orm-state="pending" orm-class="'+template.classes[i]+'" orm-testname="'+template.testCases[j]+'" orm-db="'+selectedDbs[k]+'" orm-strategy="'+template.strategies[l]+'" width="100px" style="'+borderFormat+'"><img src="img/buffer-loading.gif" alt="loading"></td>'
						var borderFormat = "";						
					}
					
				}
			}
			html += "</tr>"
		}
		
		// draw avg value row
		html += '<tr class="avg" style="border-top: 3px solid black;">'
		html += '<td><strong>AGG</strong></td>'
		for (var j in template.testCases) {
			for (var k in selectedDbs) {
		
				var borderFormat = "border-left: 3px solid black;";
				for (var l in template.strategies) {
					html += '<td orm-type="avg" orm-state="pending" orm-testname="'+template.testCases[j]+'" orm-db="'+selectedDbs[k]+'" orm-strategy="'+template.strategies[l]+'" width="100px" style="'+borderFormat+'"><img src="img/buffer-loading.gif" alt="loading"></td>'
					var borderFormat = "";						
				}
				
			}
		}
		html += '</tr>'
		
		html += '</tbody>';
		html += '</table>';

		$("#resultData").html(html)
		
		populateTableData();
		
	}
	
	function renderCharts() {
		
		var selectedDbs = [];
		$("input:checkbox[name=resultSelectedDBs]:checked").each(function() {
			selectedDbs.push($(this).val());
		});
		
		$("#graphContainer").empty();
		
		for(var i in selectedDbs) {
			
			$('#graphContainer').append('<td><div id="graph-'+selectedDbs[i]+'" class="inline" style="min-width: 550px; max-height: 500px;"></div></td>');
			
			$('#graph-'+selectedDbs[i]).highcharts({

		        chart: {
		            polar: true,
		            type: 'line'
		        },

		        title: {
		            text: selectedDbs[i]
		        },

		        pane: {
		            size: '80%'
		        },

		        xAxis: {
		            categories: [],
		            tickmarkPlacement: 'on',
		            lineWidth: 0
		        },

		        yAxis: {
		            gridLineInterpolation: 'polygon',
		            lineWidth: 0,
		            min: 0,
		            max: 1
		        },

		        tooltip: {
		            shared: true,
		            pointFormat: '<span style="color:{series.color}">{series.name}: <b>{point.y:,.2f}</b><br/>'
		        },

		        legend: {
		            align: 'center',
		            layout: 'horizontal'
		        },
		        
		        credits: {
		        	enabled: false
		        },

		        series: []

		    });
		}
		
		populateChartData();
	}
	
	function fetchResults() {
		
		$.get( "TestExecution", function(response) {
			
			// if server send new results
			if(response.results.length > 0) {
				
				// add server results to local results
				for (var i in response.results) {
					results.push(response.results[i]);
				}
				
				populateTableData();
				populateChartData();
			}
						
			// if server is not finished, poll again in 2000ms
			if (response.status > 0) {
				setTimeout(fetchResults, 2000);
			} else {
				$("#test_button").button('reset');
				$('#model_button').prop('disabled', false);
			}
			
		}).fail(function(request, status, error) {
			showErrorMessage(error)
		});
	}
	
	function populateTableData() {
		
		// get all cells with measurement values
		$('td[orm-type="value"][orm-state="pending"]').each(function() {
			
			var className = $(this).attr('orm-class');
			var strategy = $(this).attr('orm-strategy');
			var db = $(this).attr('orm-db');
			var testname = $(this).attr('orm-testname');

			for (var i in results) {
				if(results[i].testCase == testname && results[i].database == db) {
					
					if(results[i][strategy][className]) {
					
						var value = results[i][strategy][className].avgValue;
						if(Math.round(value) !== value) {
							$(this).text(value.toFixed(2));
					    } else {
							$(this).text(value);
						}
						$(this).attr('orm-state', 'ready');
						
						if(results[i][strategy][className].fastest) {
							$(this).css('background-color', 'lightgreen');
						}
						
					} else {
						$(this).text('n/a');
						$(this).attr('orm-state', 'ready');
					}
				}
			}
						
		});
		
		// get all cells for average values per strategy
		$('td[orm-type="avg"][orm-state="pending"]').each(function() {

			var strategy = $(this).attr('orm-strategy');
			var db = $(this).attr('orm-db');
			var testname = $(this).attr('orm-testname');

			for (var i in results) {
				if(results[i].testCase == testname && results[i].database == db) {
					
					var value = results[i][strategy+"_AGG"];
					if(Math.round(value) !== value) {
						$(this).text(value.toFixed(2));
				    } else {
						$(this).text(value);
					}
					
					$(this).attr('orm-state', 'ready');
					
					if(results[i].fastestStrategyOnAvg == strategy) {
						$(this).css('background-color', 'lightgreen');
					}
				}
			}
						
		});
		
	}
	
	function populateChartData() {
		
		var selectedDbs = [];
		
		$("input:checkbox[name=resultSelectedDBs]:checked").each(function() {
			selectedDbs.push($(this).val());
		});
		
		if(results.length > 3) {
			
			for(var i in selectedDbs) {
				
				var categories = [];
				var st_values = [];
				var tpc_values = [];
				var tpcc_values = [];
								
				for(var j in results) {
					if (results[j].database == selectedDbs[i]) {
						
						var array = [results[j].ST_AGG, results[j].TPC_AGG, results[j].TPCC_AGG]
						var min = Math.min.apply( Math, array )
						
						categories.push(results[j].testCase)
						st_values.push((min / results[j].ST_AGG));
						tpc_values.push((min / results[j].TPC_AGG));
						tpcc_values.push((min /results[j].TPCC_AGG));
						
					}					
				}	

				var st_series = {
					name: "ST",
					color: '#5cb85c',
					lineWidth: 1,
					data: st_values,
					pointPlacement: 'on'
				}
				
				var tpc_series = {
					name: "TPC",
					color: '#d9534f',
					lineWidth: 1,
					data: tpc_values,
					pointPlacement: 'on'
				}
				
				var tpcc_series = {
					name: "TPCC",
					color: '#f0ad4e',
					lineWidth: 1,
					data: tpcc_values,
					pointPlacement: 'on'
				}
				
				var chart = $('#graph-'+selectedDbs[i]).highcharts();
				
				while(chart.series.length > 0) {
				    chart.series[0].remove(true);
				}
				
				chart.xAxis[0].setCategories(categories);
				chart.addSeries(st_series);
				chart.addSeries(tpc_series);
				chart.addSeries(tpcc_series);
			}
		}
	}
		
</script>