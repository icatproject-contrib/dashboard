angular.module('dashboardApp').directive('lineTimeSeries', function(){


	function link(scope,element,attr){		

		scope.$watch('data', function(){

			if(scope.data !== undefined){	
			
				var chart = c3.generate({
					 bindto:element[0],

					data: {

					 	 x:"x",				 	 
		       			 columns : scope.data,
		       			 
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