(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('entityAgeScatter', function(){
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
					chart.zoom([500, 1000]);
				}

				

				$scope.$watch('data', function(dataObject){
					if(dataObject){

						$scope.description = dataObject.description;
						$scope.title = dataObject.title;
						$scope.zoom = dataObject.zoom;

						chart = c3.generate({
							
							bindto:divElement[0],
								
							data: {

							 	 x:"Days old",				 	 
				       			 columns : dataObject.data,
				       			 type:'scatter'
						    },
						   
						    legend: {
						    	show:false
						    },
						    axis: {
						        x: {

						            tick:{
						            	fit:false
						            },
						            label:{ text: 'Age of files (Days)',
						            		position: 'outer-center',
						            	},	
						        },
						        y: {
						        	min:0,
						        	label: {
						        			text: 'Number of Files',
						        			position: 'outer-center',
						        		},	
						        	padding:{bottom:0},
						        	
						        }
						    },
						    tooltip:{
									
									format: {
									    title: function (d) { return d+' days old '; },
									    value: function (value, ratio, id) {
									        var format = d3.format('s');
									        return format(value);
									    }

									}
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