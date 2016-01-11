(function(){
	'use strict';

	angular.module('dashboardApp')
			.factory('HttpInterceptor', HttpInterceptor);

	HttpInterceptor.$inject = ['$sessionStorage','$injector','$q','inform'];


	function HttpInterceptor($sessionStorage,$injector,$q,inform){
		return {

			responseError: function(rejection){
				var state;
				var userName;

				if(rejection.status === 403){
					state = $injector.get('$state');

					if($sessionStorage.sessionData.sessionID !== 'undefined'){
						$sessionStorage.sessionData = {};						
						state.go('login');
					}

					
				}

				inform.add('Session Expired. Please login.',{
					'ttl':0,
					'type':'danger'
				});

				return $q.reject(rejection);

			}
		}
	}


})();