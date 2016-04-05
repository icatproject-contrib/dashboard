(function() {
    'use strict';

    var app = angular.module('dashboardApp');


    app.directive('successGauge', function(){
    	return {
			restrict: 'EA',			
			scope: {
				data: '=',			

			},

			templateUrl : 'views/headLineTemplate.html',

			link:function($scope,$element){				
	
				var chart;
				
				var divElement = $element.find('.graphAnchor');


				$scope.$watch('data', function(dataObject){
					if(dataObject){						
						
						$scope.description =  dataObject.description;						
						$scope.title = dataObject.title;
						$scope.inProgress=dataObject.inProgress;
						$scope.failed = dataObject.failed;
						$scope.successful = dataObject.successful;

						chart  = c3.generate({
							bindto:divElement[0],
							data:{
								columns : [dataObject.data],							
								type: 'gauge'
							},
							color: {
						        pattern:['#2b2b2b','#CF000F'], // the three color levels for the percentage values.
						        threshold: {
						//            unit: 'value', // percentage is default
						//            max: 200, // 100 is default
						            values: [30, 90]
						        }
						    },
														
						});
					}
				});				
			}
		}
	});
})();






