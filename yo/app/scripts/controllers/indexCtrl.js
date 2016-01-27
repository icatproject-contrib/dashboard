(function (){

	angular.module('dashboardApp')
		   .controller('IndexController', IndexController);

	IndexController.$inject = ['$sessionStorage','$scope'];

	function IndexController($sessionStorage,$scope){

		

		var ic = this;

		ic.isLoggedIn = function(){
			return !(_.isEmpty($sessionStorage.sessionData));
		}

		//Date range manipulation so data is defaulted to 10 days ago.

		var currentDate = moment();
		var initialDate = moment().subtract(10,'days');

		$scope.myDateRange = { startDate: initialDate, endDate: currentDate };

	}		   


})();