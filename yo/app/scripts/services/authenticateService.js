(function() {
    'use strict';

    angular
        .module('dashboardApp')
        .service('Authenticate', Authenticate );

    Authenticate.$inject = ['$q', '$sessionStorage'];

    function Authenticate($q, $sessionStorage){
        this.authenticate = function(){
            var isAuthenticated = false;

            if (! _.isEmpty($sessionStorage.sessionData)){
                isAuthenticated = true;
            }

            //Authentication logic here
            if(isAuthenticated){
                //If authenticated, return anything you want, probably a user object
                return true;
            } else {
                //Else send a rejection
                return $q.reject({isAuthenticated : false});
            }
        };
    }
})();
