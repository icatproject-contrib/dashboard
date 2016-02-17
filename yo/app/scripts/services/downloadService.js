(function(){
	'use strict';


	angular.module('dashboardApp').factory('DownloadService', DownloadService);

			DownloadService.$inject = ['$http', '$sessionStorage','$q'];

			function DownloadService ($http, $sessionStorage, $q){

				var baseURL = 'https://localhost:8181/dashboard/api/v1/download/';

					

				var services = {

					getDownloadMethodTypes :function(){
						return $http.get(baseURL+"method/types?sessionID="+ $sessionStorage.sessionData.sessionID)
							.then(function(response){

								return response.data; 
							});

					},						

				    getDownloadMethod : function(startDate,endDate, userName){
					
						
						return $http.get(baseURL+"method?sessionID=" + $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName)
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

						return $http.get(baseURL+"bandwidth?sessionID=" +$sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName+"&method="+method)
							.then(function(response){

								return response.data;
							
							});

					},

					getDownloadSize : function(startDate, endDate, userName, method){

						return $http.get(baseURL+"size?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&userName="+userName+"&method="+method)
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