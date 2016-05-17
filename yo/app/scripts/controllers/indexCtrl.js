(function (){

	angular.module('dashboardApp')
		   .controller('IndexController', IndexController);

	IndexController.$inject = ['$sessionStorage','$rootScope'];

	function IndexController($sessionStorage,$rootScope){

		
		
		var ic = this;

		ic.isLoggedIn = function(){
			return !(_.isEmpty($sessionStorage.sessionData));
		}

		$rootScope.baseURL = 'https://localhost:8181/dashboard/';
		$rootScope.graphColours = ["#2980B9","#4B77BE","#3498DB"];

		
	}		   


})();