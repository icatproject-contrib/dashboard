(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('ispBandwidthBar', function(){
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
							 	 				 	 
				       			 columns : dataObject.data,
				       			 type:'bar',
				       			 labels:true,
				       			 groups:[['average','min','max']]
						    },
						    legend: {
						    	show:false,
						    },
						    axis: {
						    	x:{
						    		type:'category',
						    		categories:[dataObject.ispList],
						            label: {
						            	text: 'ISP',
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
								pattern: ['#2b2b2b','#CF000F','#7f8c8d'],
							},		
						});					
							}
						});


							}
							
						};

				});


	
})();



