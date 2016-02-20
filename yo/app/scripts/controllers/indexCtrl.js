(function (){

	angular.module('dashboardApp')
		   .controller('IndexController', IndexController);

	IndexController.$inject = ['$sessionStorage','$rootScope'];

	function IndexController($sessionStorage,$rootScope){

		
		
		var ic = this;

		ic.isLoggedIn = function(){
			return !(_.isEmpty($sessionStorage.sessionData));
		}

		$rootScope.baseURL = 'api/v1/';

		
	}		   


})();