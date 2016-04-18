(function(){
	'use strict';

	/* 
	* A service that deals with calling the the dashboards entity RESTful calls.
	* Each call is dealt in the exact same manner with a .then promise which 
	* returns the data to the method that accesses this service.
	*
	*/
	angular.module('dashboardApp').factory('entityService', entityService);

			entityService.$inject = ['$http', '$sessionStorage','$q','$rootScope'];

			function entityService ($http, $sessionStorage, $q,$rootScope){

				var baseURL = $rootScope.baseURL;

					

				var services = {

					getInstrumentMeta: function(startDate,endDate, instrument){
						return $http.get(baseURL+"/icat/instrumentMeta?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&instrument="+instrument)
							.then(function(response){

								return response.data; 
							});
					},

				}

					
				    
				

			return services;
		}

})();