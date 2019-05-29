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

					getInstrumentFileCount: function(startDate,endDate, instrumentName){
						return $http.get(baseURL+"icat/"+instrumentName+"/datafile/number?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate)
							.then(function(response){
								return response.data; 
							});
					},
					getInstrumentFileVolume: function(startDate,endDate, instrumentName){
						return $http.get(baseURL+"icat/"+instrumentName+"/datafile/volume?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate)
							.then(function(response){
								return response.data; 
							});
					},
					getInstrumetNames: function(){
						return $http.get(baseURL+"icat/instrument/names")
							.then(function(response){
								return response.data;
							})
					},
					getEntityNames:function(){
						return $http.get(baseURL+"icat/entity/name")
							.then(function(response){
								return response.data;
							})
					},
					getEntityCount:function(startDate,endDate,entity){
						return $http.get(baseURL+"icat/"+entity+"/number?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate)
							.then(function(response){
								return response.data; 
							});
					},
					getInvestigationDatafileVolume:function(startDate,endDate){
						return $http.get(baseURL+"icat/investigation/datafile/volume?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&limit=20")
							.then(function(response){
								return response.data; 
							});
					},
					getInvestigationDatafileCount:function(startDate,endDate){
						return $http.get(baseURL+"icat/investigation/datafile/number?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate+"&limit=20")
							.then(function(response){
								return response.data; 
							});
					},
					getDatafileVolume:function(startDate,endDate){
						return $http.get(baseURL+"icat/datafile/volume?sessionID="+ $sessionStorage.sessionData.sessionID+"&startDate="+startDate+"&endDate="+endDate)
							.then(function(response){
								return response.data; 
							});
					},
                                        getEntityPeriod : function() {
                                                return $http.get(baseURL+"icat/period")
                                                        .then(function(response) {
                                                            
                                                                return response.data;
                                                        
                                                        });
                                        },


				}

					
				    
				

			return services;
		}

})();