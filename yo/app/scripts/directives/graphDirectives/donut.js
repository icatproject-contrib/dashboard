(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('donut', function(){
    	return {
			restrict: 'EA',			
			scope: {
				data: '=',					

			},

			templateUrl : 'views/graphTemplate.html',

			link:function($scope,$element,$rootScope){
				
	
				var chart;
				
				var divElement = $element.find('.graphAnchor');

				$scope.reset = function(){					
					chart.unzoom();
				}					

				$scope.$watch('data', function(graphObject){
					if(graphObject){
						
						$scope.description =  graphObject.description;
						$scope.options = graphObject.datasets;
						$scope.title = graphObject.number.title;

						chart  = c3.generate({
							bindto:divElement[0],
							data:{
								columns : graphObject.number.data,							
								type: 'donut'
								},
							donut:{
								label : {
									format: function(value,ratio,id){
										return value;
									},

								}
							},
							
							color:{
								pattern: $rootScope.graphColours,
							},							
						});
				}
			});

			$scope.changeData = function(option){				
				chart.unload();

				var dataset = $scope.data[option];
				
				console.log(dataset.data)
				chart.load({

					columns: dataset.data,

					
				});
				$scope.title=  dataset.title;
			}	
		}

	}

	});

})();






