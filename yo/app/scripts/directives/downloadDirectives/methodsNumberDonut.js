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

				
				$scope.description =  "This donut chart displays the number of downloads by download mechanism";
				$scope.title = "Download Methods";

				$scope.$watch('data', function(data){
					if(data){						
					
						chart  = c3.generate({
							bindto:divElement[0],
							data:{
								columns : data,							
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
		}

	}

	});

})();






