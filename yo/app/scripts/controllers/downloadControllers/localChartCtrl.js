(function(){

	'use strict';

	angular.module('dashboardApp').controller('LocalChartCtrl', LocalChartCtrl);

	LocalChartCtrl.$inject= ['$scope','DownloadService'];

	

	function LocalChartCtrl($scope, DownloadService){	

		
		var identifiers = ['City', 'Amount of Downloads'];

		$scope.$watch('myDateRange',function(newDate){

			var localChart = {};
			localChart.type = "GeoChart";
			    

			var data =[
			        ['City',   'Amount', ],
			        ['London',   2],
			        ['Appleton', 1],
			        
			      ];

			localChart.options = {
			        region: 'GB',
			        displayMode: 'markers',
			        colorAxis: {colors: ['red', 'grey']}
			};

				    

			$scope.localChart = localChart;
			$scope.localChart.data = data;
			

			});
	
		

	    

	}






})();