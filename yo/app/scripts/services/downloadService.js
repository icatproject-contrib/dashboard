(function(){
	'use strict';


	angular.module('dashboardApp').factory('DownloadService', DownloadService);

			DownloadService.$inject = ['$http', '$sessionStorage','$q'];

			function DownloadService ($http, $sessionStorage, $q){

				var baseURL = 'http://localhost:8080/Dashboard/download/';

					

				var services = {						

				    getRoutes : function(startDate,endDate){
					
					
						return $http.get(baseURL+"route?sessionID=" + $sessionStorage.sessionData.sessionID+"&startDate="+startDate.unix()+"&endDate="+endDate.unix())
							.then(function(response){

								return response.data;
							
							});
	
					},

					getDownloadFrequency : function(startDate,endDate){

						return $http.get(baseURL+"frequency?sessionID=" +$sessionStorage.sessionData.sessionID+"&startDate="+startDate.unix()+"&endDate="+endDate.unix())
							.then(function(response){

								return response.data;
							
							});
					},
				    
			}

			return services;
		}

})();