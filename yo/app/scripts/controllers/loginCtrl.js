(function (){
	  'use strict';
angular.module('dashboardApp').controller('LoginCtrl', LoginCtrl);

	LoginCtrl.$inject = ['LoginService','$sessionStorage','$state','$rootScope'];

	function LoginCtrl( LoginService, $sessionStorage, $state, $rootScope){
		var lc = this;


		var authenticators = LoginService.getAuthenticators();

                authenticators.then(function(responseData){
                    lc.authenticators = responseData;

                });

		lc.login = function(){

			LoginService.login(lc.authenticator, lc.username, lc.password).
				then(function(responseData){
								
					$sessionStorage.sessionData = {

					sessionID : responseData['sessionID'],
					username : lc.username
						};
										
					$state.go('downloads');
			});		


            };
        };    

})();
