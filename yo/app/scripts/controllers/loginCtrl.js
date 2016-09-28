(function (){
	  'use strict';
angular.module('dashboardApp').controller('LoginCtrl', LoginCtrl);

	LoginCtrl.$inject = ['LoginService','$sessionStorage','$state','$rootScope'];

	function LoginCtrl( LoginService, $sessionStorage, $state, $rootScope){
            var lc = this;


            var authenticators = LoginService.getAuthenticators();

            authenticators.then(function(responseData){
                lc.authenticators = responseData;
                lc.authenticator = responseData[0].mnemonic;  

            });

            lc.display = function() {
                if (lc.authenticators && lc.authenticators.length > 1) {
                    return true;
                } else {
                    return false;
                }
            };

            lc.login = function(){
                if (lc.authenticator) {
                   LoginService.login(lc.authenticator, lc.username, lc.password).
                        then(function(responseData){

                                $sessionStorage.sessionData = {

                                sessionID : responseData['sessionID'],
                                username : lc.username
                                        };

                                $state.go('downloads');
                    }); 
                }
            };
        };    

})();
