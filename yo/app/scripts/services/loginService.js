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
	   			 return $http.post('http://localhost:8080/Dashboard/session/login', {'authenticator' : 'uows', "username": username ,
						   "password" : password })
	   			
	   		}

	   		
    }
 
    
 
})();