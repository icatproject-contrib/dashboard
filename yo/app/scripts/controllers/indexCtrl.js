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


		/* Workaround to deal with datepicker use case: 
			When a user selects the start date as the current date
			then the current time will be selected which isn't good. It needs to be
			00:00:00. May be removed if user asks for time.
		*/
		$scope.$watch('myDateRange',function(newDate){
			var start = moment(newDate.startDate);
			
			
			if(start.get('date')===moment().get('date')) {
				var newStartDate = moment();
				newStartDate.set('hour',00);
				newStartDate.set('minute',00);
				newStartDate.set('second',00);
				$scope.myDateRange.startDate = newStartDate;
		
			}
			


		});
	}		   


})();