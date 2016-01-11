(function (){
	  'use strict';
angular.module('dashboardApp').controller('LoginCtrl', LoginCtrl);

	LoginCtrl.$inject = ['LoginService','$sessionStorage','$state','$rootScope'];

	function LoginCtrl( LoginService, $sessionStorage, $state, $rootScope){
		var lc = this;

		lc.login = login;


		function login(){

			LoginService.Login(lc.authenticator, lc.username, lc.password).
			then(function(responseData){				
				$sessionStorage.sessionData = {

				sessionID : responseData.data['sessionID'],
				username : lc.username
					};
									
				$state.go('downloads');
			});		


            };
        };    

})();
