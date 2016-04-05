(function(){
	'use strict';

	/* 
	* A service that deals with calling the the dashboards user RESTful calls.
	* Each call is dealt in the exact same manner with a .then promise which 
	* returns the data to the method that accesses this service.
	*
	*/
	angular.module('dashboardApp').factory('userService', userService);

			userService.$inject = ['$http', '$sessionStorage','$q','$rootScope'];

			function userService ($http, $sessionStorage, $q,$rootScope){

				var baseURL = $rootScope.baseURL;

					

				var services = {

					getLoggedUsers: function(){
						return $http.get(baseURL+"user/logged?sessionID="+ $sessionStorage.sessionData.sessionID)
							.then(function(response){

								return response.data; 
							});
					},

					getUserLocation: function(name){
						return $http.get(baseURL+"user/location?sessionID="+ $sessionStorage.sessionData.sessionID+"&name="+name)
							.then(function(response){

								return response.data; 
							});
					},

					getIcatLogs:function(queryConstraint, initialLimit, maxLimit, canceller){
						return $http.get(baseURL+"icat/logs?sessionID="+ $sessionStorage.sessionData.sessionID+"&queryConstraint="+encodeURIComponent(queryConstraint)+"&initalLimit="+initialLimit+"&maxLimit="+maxLimit,{timeout:canceller.promise})
							.then(function(response){
								
								return response.data; 
							});
					},

					getIcatLogLocation: function(logId){
						return $http.get(baseURL+"icat/logs/location?sessionID="+ $sessionStorage.sessionData.sessionID+"&logId="+logId)
							.then(function(response){

								return response.data; 
							});
					},

					
				    
				}

			return services;
		}

})();