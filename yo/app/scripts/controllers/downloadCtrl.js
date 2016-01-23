(function(){

	'use strict';

	angular.module('dashboardApp').controller('DownloadCtrl', DownloadCtrl);

	DownloadCtrl.$inject= ['DownloadService','$scope'];

	function DownloadCtrl(DownloadService, $scope){	

		

		$scope.$watch('myDateRange',function(newDate){

			var startDate = $scope.myDateRange.startDate;
			var endDate = $scope.myDateRange.endDate;

			var downloadRoutes = DownloadService.getRoutes(startDate,endDate);
			var downloadCount = DownloadService.getDownloadFrequency(startDate,endDate);

			downloadRoutes.then(function(responseData){	
				  	var data = responseData;			  			  	
				    $scope.downloadRoutes = _.map(data, function(data){
						return [data.method, data.amount];
					});
		    });

		    downloadCount.then(function(responseData){
		    		var data = responseData;
		    		
		    		var dates  = _.map(data, function(data){
						return data.date;
					});

					var amounts = _.map(data, function(data){
						return data.amount;
					});

					dates.unshift("x");							
					amounts.unshift('Count');
					
					$scope.downloadCount = [dates,amounts]
					
		    });

	    
	    	
	    });

		
	}

})();