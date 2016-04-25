(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('timeLineGraph', function(){
    	return {
			restrict: 'EA',			
			scope: {
				data: '=',
				selectOptions:'&',


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
						
						$scope.description = graphObject.description;
						$scope.title = graphObject.title;
						$scope.zoom = graphObject.zoom;
						$scope.selectOp = graphObject.selectOp;
						$scope.selectTitle = graphObject.optionTitle;
																	

					
						chart = c3.generate({
							bindto:divElement[0],

							data: graphObject.data,
				       							    
						    legend: {
						    	show:false
						    },
						    axis: {
						        x: {
						        	padding:{
						        		left:0,
						        		
						        	},
						            type: 'timeseries',
						            tick: {
						                format: '%Y-%m-%d'
						            },
						            label: {
						        			text: graphObject.xLabel,
						        			position: 'outer-center',
						        		}, 
						        },
						        y: {
						        	padding:{
						        		bottom:0,
						        	},
						        	label: {
						        			text: graphObject.yLabel,
						        			position: 'outer-center',
						        		},
						        }
						    },
						    tooltip:{
									
								},
							color:{
								pattern: $rootScope.graphColours,
							},	
							zoom:{
								enabled:'true'
							}
						});
				}
			});
		}

	}

	});

})();




