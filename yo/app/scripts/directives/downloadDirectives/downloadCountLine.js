(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('downloadCountLine', function(){
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
				
				
				$scope.$watch('data', function(dataObject){
					if(dataObject){	

						$scope.description = dataObject.description;
						$scope.title = dataObject.title;
						$scope.zoom = dataObject.zoom;					
					
						chart = c3.generate({
							bindto:divElement[0],

							data: {

							 	 x:"x",				 	 
				       			 columns : dataObject.data,
				       			 
						    },
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
						        			text: 'Dates',
						        			position: 'outer-center',
						        		}, 
						        },
						        y: {
						        	padding:{
						        		bottom:0,
						        	},
						        	label: {
						        			text: 'Number of Downloads',
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




