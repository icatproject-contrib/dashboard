(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('successGauge', function(){
    	return {
			restrict: 'EA',			
			scope: {
				data: '=',					

			},

			templateUrl : 'views/graphTemplate.html',

			link:function($scope,$element){				
	
				var chart;
				
				var divElement = $element.find('.graphAnchor');						

				$scope.$watch('data', function(dataObject){
					if(dataObject){
						
						$scope.description =  dataObject.description;
						$scope.options = dataObject.datasets;
						$scope.title = dataObject.number.title;

						chart  = c3.generate({
							bindto:divElement[0],
							data:{
								columns : data.number.data,							
								type: 'gauge'
								},
							donut:{
								label : {
									format: function(value,ratio,id){
										return value;
									},

								}
							},
							tooltip:{
									
									
								},
							color:{
								pattern: ['#CF000F','#7f8c8d','#2b2b2b'],
							},							
						});
				}
			});

			$scope.changeData = function(option){				
				chart.unload();

				var dataset = $scope.data[option];
				
				
				chart.load({
					columns: dataset.data,
					
				});
				$scope.title=  dataset.title;
			}	
		}

	}

	});

})();






