(function(){

	'use strict';
angular.module('dashboardApp')	
	   .factory('LoginService', LoginService);

	   LoginService.$inject = ['$http'];

	   function LoginService($http){
	   		var service = {};

	   		service.Login = Login;	   		
	   		
	   		return service;

	   		function Login(authenticator,username,password){
	   			 return $http.post('https://localhost:8181/dashboard/api/v1/session/login', {'authenticator' : 'uows', "username": username ,
						   "password" : password })
	   			
	   		}

	   		
    }
 
    
 
})();