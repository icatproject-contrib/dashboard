angular.module('dashboardApp').directive('barTimeSeries', function(){


	function link(scope,element,attr){		

		scope.$watch('data', function(){

			if(scope.data !== undefined){	
			
				var chart = c3.generate({
					 bindto:element[0],

					data: {

					 	 x:"x",				 	 
		       			 columns : scope.data,
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
				        			text: 'MegaBytes',
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

	return {
			restrict: 'EA',			
			scope: {
				data: '='		

			},
			link : link
		};
});