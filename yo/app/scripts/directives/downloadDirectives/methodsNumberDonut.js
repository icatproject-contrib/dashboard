(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('methodsNumberDonut', function(){
    	return {
			restrict: 'EA',			
			scope: {
				data: '=',					

			},

			templateUrl : 'views/graphTemplate.html',

			link:function($scope,$element){
				
	
				var chart;
				
				var divElement = $element.find('.graphAnchor');

				$scope.reset = function(){					
					chart.unzoom();
				}

				$scope.options = ["number","volume"];

			

						

				$scope.$watch('data', function(data){
					if(data){
						console.log(data.volume)
						$scope.description =  data.description;
						$scope.title = data.title;					
						chart  = c3.generate({
							bindto:divElement[0],
							data:{
								columns : data.number,							
								type: 'donut'
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
				
				chart.load({
					columns: $scope.data[option]
				});
			}	
		}

	}

	});

})();






