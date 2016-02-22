(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('bandwidthScatter', function(){
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
					
					$scope.zoom = dataObject.zoom;
					$scope.description = dataObject.description;
					$scope.title = dataObject.title;
					
					chart = c3.generate({
						 bindto:divElement[0],
							

						 data: {

						 	 x:"x",				 	 
			       			 columns : dataObject.data,
			       			 type:'spline'
					    },
					    transition: {duration: 0},
					    legend: {
					    	show:false
					    },
					    axis: {
					        x: {

					            type: 'timeseries',
					            tick: {
					                format: '%Y-%m-%d'
					            },
					            label:{ text: 'Dates',
					            		position: 'outer-center',
					            	},	
					        },
					        y: {
					        	min:0,
					        	label: {
					        			text: 'MB/S',
					        			position: 'outer-center',
					        		},	
					        	padding:{bottom:0},
					        	
					        }
					    },
					    tooltip:{
								
								format: {
								    
								    value: function (value, ratio, id) {
								        var format = d3.format('s');
								        return format(value);
								    }

								},
								grouped: false
							},
						color:{
							pattern: ['#CF000F','#7f8c8d','#2b2b2b'],
						},
						zoom: {
							enabled:true
						}

						});						
					}
				});


					}
					
				};

			});


	
})();

