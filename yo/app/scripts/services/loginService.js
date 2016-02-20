(function(){

	'use strict';

	/**
	* The login service passes the authenticator, username and password 
	*/
angular.module('dashboardApp')	
	   .factory('LoginService', LoginService);

	   LoginService.$inject = ['$http'];

	   function LoginService($http){

	   		var baseURL = 'https://localhost:8181/dashboard/api/v1/';
	   		
	   		var service = {

	   			   		
	   			//Retrieves a list of authenticators the ICAT is using.
		   		getAuthenticators:function(){
		   			return $http.get(baseURL+'icat/authenticators'). 
		   			then(function(response){

		   				return response.data;
		   			});

		   		},
	   		
	   			//Logins into the dashboard
		   		login: function(authenticator,username,password){
		   			 return $http.post(baseURL+'session/login', {'authenticator' : authenticator, "username": username ,
							   "password" : password }).
		   			 then(function(response){
		   			 	return response.data;
		   			 });
		   			
		   		}
		   		

		   	};

	   		return service;
    }
 
    
 
})();