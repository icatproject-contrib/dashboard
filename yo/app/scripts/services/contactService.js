(function() {
	'use strict';

	angular.module('dashboardApp').factory('contactService', contactService);

        contactService.$inject = ['$http', '$sessionStorage', '$rootScope'];

        function contactService ($http, $sessionStorage, $rootScope){

                var baseURL = $rootScope.baseURL+'contact';



                var services = {

                        getContactMessage: function(){
                                return $http.get(baseURL+"/message?sessionID="+ $sessionStorage.sessionData.sessionID)
                                        .then(function(response){
                                                return response.data;
                                        });
                        },
                }
            return services;
        }

})();