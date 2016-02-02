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

				if(rejection.status === 401){
					state = $injector.get('$state');

					if($sessionStorage.sessionData.sessionID !== 'undefined'){
						$sessionStorage.sessionData = {};						
						state.go('login');
					}

					inform.add('Session has Expired. Please re-login.',{
					'ttl':0,
					'type':'danger'
					});
					
				}

				if(rejection.status === 400){

					inform.add('Bad request '+ reject);
				}


				if(rejection.status === 500){
					inform.add('Internal Error: '+rejection.data.message,{
					'ttl':0,
					'type':'danger'
					});
				}

				

				return $q.reject(rejection);

			}
		}
	}


})();