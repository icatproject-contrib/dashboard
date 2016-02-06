(function(){
	'use strict';


	angular.module('dashboardApp').factory('DownloadService', DownloadService);

			DownloadService.$inject = ['$http', '$sessionStorage','$q'];

			function DownloadService ($http, $sessionStorage, $q){

				var baseURL = 'https://localhost:8181/dashboard/api/v1/download/';

					

				var services = {						

				    getDownloadRoute : function(startDate,endDate){
					
					
						return $http.get(baseURL+"route?sessionID=" + $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate)
							.then(function(response){

								return response.data;
							
							});
	
					},

					getDownloadFrequency : function(startDate,endDate){

						return $http.get(baseURL+"frequency?sessionID=" +$sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate)
							.then(function(response){

								return response.data;
							
							});
					},

					getDownloadBandwidth : function(startDate, endDate){

						return $http.get(baseURL+"bandwidth?sessionID=" +$sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate)
							.then(function(response){

								return response.data;
							
							});

					},

					getDownloadSize : function(startDate, endDate){

						return $http.get(baseURL+"size?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate)
							.then(function(response){

								return response.data;
							});

					},	
					getGlobalDownloadLocation : function(startDate, endDate){

						return $http.get(baseURL+"location/global?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate)
							.then(function(response){

								return response.data;
							});

					},
				    
			}

			return services;
		}

})();