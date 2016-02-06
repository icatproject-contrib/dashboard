(function(){

	'use strict';

	angular.module('dashboardApp').controller('WorldChartCtrl', WorldChartCtrl);

	WorldChartCtrl.$inject= ['$scope','DownloadService'];

	

	function WorldChartCtrl($scope, DownloadService){	

		
		var identifiers = ['Country', 'Amount of Downloads'];

		$scope.$watch('myDateRange',function(newDate){

			var startDate = Date.parse($scope.myDateRange.startDate);
			var endDate = Date.parse($scope.myDateRange.endDate);

			var globalDownloadLocation = DownloadService.getGlobalDownloadLocation(startDate,endDate);

			globalDownloadLocation.then(function(responseData){ 			   				  		

			    responseData = _.map(responseData, function(responseData){
					return [responseData.countryCode, responseData.amount];
				});
	    		
	    		responseData.unshift(identifiers);

	    		var worldChart = {};
			    worldChart.type = "GeoChart";
			    worldChart.options = {
			        
			    };			    

			    $scope.worldChart = worldChart;
			    $scope.worldChart.data = responseData;
			});

			});
	
		

	    

	}






})();