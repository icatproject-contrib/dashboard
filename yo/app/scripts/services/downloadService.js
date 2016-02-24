(function(){
	'use strict';

	/* 
	* A service that deals with calling the the dashboards download RESTful calls.
	* Each call is dealt in the exact same manner with a .then promise which 
	* returns the data to the method that accesses this service.
	*
	*/
	angular.module('dashboardApp').factory('downloadService', downloadService);

			downloadService.$inject = ['$http', '$sessionStorage','$q','$rootScope'];

			function downloadService ($http, $sessionStorage, $q,$rootScope){

				var baseURL = $rootScope.baseURL+'/download/';

					

				var services = {

					getDownloadMethodTypes :function(){
						return $http.get(baseURL+"method/types?sessionID="+ $sessionStorage.sessionData.sessionID)
							.then(function(response){

								return response.data; 
							});

					},						

				    getDownloadMethodNumber : function(startDate,endDate, userName){
					
						
						return $http.get(baseURL+"method/number?sessionID=" + $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName)
							.then(function(response){

								return response.data;
							
							});
	
					},

					getDownloadMethodVolume : function(startDate,endDate, userName){
					
						
						return $http.get(baseURL+"method/volume?sessionID=" + $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName)
							.then(function(response){

								return response.data;
							
							});
	
					},

					getDownloadFrequency : function(startDate,endDate, userName, method){

						return $http.get(baseURL+"frequency?sessionID=" +$sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName+"&method="+method)
							.then(function(response){

								return response.data;
							
							});
					},

					getDownloadBandwidth : function(startDate, endDate, userName, method){

						return $http.get(baseURL+"bandwidth/?sessionID=" +$sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName+"&method="+method)
							.then(function(response){

								return response.data;
							
							});

					},

					getDownloadISPBandwidth : function(startDate, endDate, userName, method){

						return $http.get(baseURL+"bandwidth/isp?sessionID=" +$sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName+"&method="+method)
							.then(function(response){

								return response.data;
							
							});

					},

					getDownloadVolume : function(startDate, endDate, userName, method){

						return $http.get(baseURL+"volume?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName+"&method="+method)
							.then(function(response){

								return response.data;
							});

					},	
					getLocalDownloadLocation : function(startDate, endDate, userName, method){

						return $http.get(baseURL+"location?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName+"&method="+method)
							.then(function(response){

								return response.data;
							});

					},
					getGlobalDownloadLocation : function(startDate, endDate, userName, method){

						return $http.get(baseURL+"location/global?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName+"&method="+method)
							.then(function(response){

								return response.data;
							});

					},
					getDownloadEntityAge : function(startDate, endDate, userName, method){

						return $http.get(baseURL+"entities/age?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName+"&method="+method)
							.then(function(response){

								return response.data;
							});

					},
				    
			}

			return services;
		}

})();