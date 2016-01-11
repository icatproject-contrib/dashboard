(function(){

	'use strict';

	angular.module('dashboardApp').controller('DownloadCtrl', DownloadCtrl);

	DownloadCtrl.$inject= ['DownloadService','$scope'];

	function DownloadCtrl(DownloadService, $scope){		
		
		var r = DownloadService.getRoutes();

		 r.then(function(responseData){	
			  	var data = responseData;			  			  	
			    $scope.routes = _.map(data, function(data){
					return [data.method, data.amount];
				});
		
		       

	  });

		
	}

})();