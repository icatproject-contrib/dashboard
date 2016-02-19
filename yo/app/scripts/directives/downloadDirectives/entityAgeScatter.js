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
					chart.unzoom();
				}

				$scope.description = "This scatter graph displays the number of datafiles that have been downloaded grouped by their age. The age of the datafile is calculated from its creation date subtracted by the date of the download. The age is displayed in days.";
				$scope.title = "Download File Age";
				$scope.zoom = true;

				$scope.$watch('data', function(data){
					if(data){

						chart = c3.generate({
							
							bindto:divElement[0],
								
							data: {

							 	 x:"x",				 	 
				       			 columns : data,
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