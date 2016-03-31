(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('dateTimePicker', function(){
    	return {
			restrict: 'A',			
			scope: {
				value: '=ngModel',					

			},

			templateUrl : 'views/dateTimeTemplate.html',

			link:function($scope,$element){
				
				$scope.dateTime;
					
			
				$scope.format =  'yyyy-MM-dd';

        		$scope.isDateOpen = false;
		      

		        $scope.openDate = function(){
		            $scope.isDateOpen = true;
		            
		        };		      

		       	$scope.update = function(){	
			        $scope.value = $scope.dateTime;		  

				    

				 }
				       	
		        


			}
			
		};

	});	
})();

