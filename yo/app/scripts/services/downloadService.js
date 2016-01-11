(function(){
	'use strict';


	angular.module('dashboardApp')
			.factory('DownloadService', DownloadService);

			DownloadService.$inject = ['$http', '$sessionStorage','$q'];

			var baseURL = 'http://localhost:8080/Dashboard/rest/download/';


			function DownloadService ($http, $sessionStorage, $q){

				var deferred = $q.defer();

				var services = {				

					getRoutes : function(){
						
						return $http.get(baseURL+"route?sessionID=" + $sessionStorage.sessionData.sessionID)
							.then(function(response){

								deferred.resolve(response.data);

								return deferred.promise;
							}, function(response){

								deferred.reject(response);

								return deferred.promise;
							
							});
		
					}
			
			}
			return services;

		}

})();