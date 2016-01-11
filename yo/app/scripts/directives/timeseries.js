angular.module('dashboardApp').directive('timeSeries', function(){


	function link(scope,element,attr){

		var chart = c3.generate({
			 data: {
			 	 bindto:'#bandwidth',
       			 x: 'x',		       
		        columns: [
		            ['x', '2013-01-01', '2013-01-02', '2013-01-03', '2013-01-04', '2013-01-05', '2013-01-06'],		          
		            ['Bandwidth(Bytes)', 30, 200, 100, 400, 150, 250],
		            
		        ]
		    },
		    axis: {
		        x: {
		            type: 'timeseries',
		            tick: {
		                format: '%Y-%m-%d'
		            }
		        }
		    },
		    tooltip:{
					position: function(data,width,height,element){
						return {top:-300,left:500}
					}
				},
			color:{
				pattern: ['#CF000F','#7f8c8d','#2b2b2b'],
			},	
		});

	};

	return {
			restrict: 'EA',			
			scope: {
				data: '='		

			},
			link : link
		};
});