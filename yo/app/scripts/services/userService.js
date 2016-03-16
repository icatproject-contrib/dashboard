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

				var baseURL = $rootScope.baseURL+'/download';

					

				var services = {

					getLoggedUsers: function(downloadId){
						return $http.get(baseURL+"/users/logged?sessionID="+ $sessionStorage.sessionData.sessionID)
							.then(function(response){

								return response.data; 
							});
					}
					
				    
				}

			return services;
		}

})();