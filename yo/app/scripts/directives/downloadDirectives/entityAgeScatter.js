angular.module('dashboardApp').directive('entityAgeScatter', function(){


	function link(scope,element,attr){		

		scope.$watch('data', function(){

			if(scope.data !== undefined){	
				
			
				var chart = c3.generate({
					 bindto:element[0],
						

					 data: {

					 	 x:"x",				 	 
		       			 columns : scope.data,
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
				        			text: 'number of Files',
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

	return {
			restrict: 'EA',			
			scope: {
				data: '='		

			},
			link : link
		};
});