angular.module('dashboardApp').directive('scatterTimeSeries', function(){


	function link(scope,element,attr){		

		scope.$watch('data', function(){

			if(scope.data !== undefined){	
			console.log(scope.data);
				var chart = c3.generate({
					 bindto:element[0],
						

					 data: {

					 	 x:"x",				 	 
		       			 columns : scope.data,
		       			 type:'spline'
				    },
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
				chart.zoom();
				chart.zoom();
				console.log("ZOOOM")
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