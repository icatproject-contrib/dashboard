angular.module('dashboardApp').directive('localChart', function(){


	function link(scope,element,attr){		

		
				
			    google.charts.setOnLoadCallback(drawMarkersMap);

			    function drawMarkersMap() {
			      var data = google.visualization.arrayToDataTable([
			        ['City',   'Amount', ],
			        ['London',   200],
			        ['Appleton', 10],
			        
			      ]);

			      var options = {
			        region: 'GB',
			        displayMode: 'markers',
			        colorAxis: {colors: ['red', 'grey']}
			      };

			      var chart = new google.visualization.GeoChart(element[0]);
			      chart.draw(data, options);
			      };
		
	}


	return {
			restrict: 'EA',			
			scope: {
				data: '='		

			},
			link : link
		};
});