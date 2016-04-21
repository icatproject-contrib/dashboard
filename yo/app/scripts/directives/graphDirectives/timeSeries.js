(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('timeLineGraph', function(){
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
				
				
				$scope.$watch('data', function(graphObject){
					if(graphObject){	

						$scope.description = graphObject.description;
						$scope.title = graphObject.title;
						$scope.zoom = graphObject.zoom;	
						
					
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
								pattern: ['#CF000F','#7f8c8d','#2b2b2b'],
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




