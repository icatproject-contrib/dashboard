angular.module('dashboardApp').directive('worldChart', function(){


	function link(scope,element,attr){		

		scope.$watch('data', function(){

			if(scope.data !== undefined){

				  
			      google.charts.setOnLoadCallback(drawRegionsMap);

			      function drawRegionsMap() {
			      	var identifiers = ['Country', 'Amount of Downloads'];
			      	var downloadLocations = scope.data;		      	
			      	downloadLocations.unshift(identifiers);	      	

			     
			        var data = google.visualization.arrayToDataTable(downloadLocations);

			        var options = {};

			        var chart = new google.visualization.GeoChart(element[0]);

			        chart.draw(data, options);
			      }

			}
				  
						  
			});
	}


	return {
			restrict: 'EA',			
			scope: {
				data: '='		

			},
			link : link
		};
});