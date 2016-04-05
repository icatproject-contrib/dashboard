(function (){
	'use strict';

	angular.module('dashboardApp')
			.controller('LogoutController', LogoutController);

	LogoutController.$inject = ['$state','$sessionStorage'];

	function LogoutController($state, $sessionStorage){

		$sessionStorage.sessionData = {};
		$state.go('login');

	}			

})();