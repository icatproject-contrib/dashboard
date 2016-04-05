(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('downloadSizeBar', function(){
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
						
						chart = c3.generate({
							bindto:divElement[0],

							data: {

							 	 x:"x",				 	 
				       			 columns : dataObject.data,
				       			 type:'bar',
				       			 labels:true,
						    },
						    legend: {
						    	show:false,
						    },
						    axis: {
						        x: {
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
						        	label: {
						        			text: dataObject.byteFormat,
						        			position: 'outer-center',
						        		},				        
						    },
						},
						    tooltip:{
									
								},
							color:{
								pattern: ['#2b2b2b'],
							},	
						});					
							}
						});


							}
							
						};

				});


	
})();



