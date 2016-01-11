angular.module('dashboardApp').directive('donutChart',function(){	 	

		function link(scope,element,attr){		
			
			
			scope.$watch(function() {
				
        		return scope.data;
   			 },
	    		function() {	    			
	    			if(scope.data !== undefined){						

						var chart  = c3.generate({
							bindto: 'donut-chart',
							data:{
								columns : scope.data,							
								type: 'donut'
								},
							donut:{
								label : {
									format: function(value,ratio,id){
										return value;
									},

								}
							},
							tooltip:{
									position: function(data,width,height,element){
										return {top:-180,left:300}
									}
								},
							color:{
								pattern: ['#CF000F','#7f8c8d','#2b2b2b'],
							},							
						});	
					}		
	            })
		};			

		return {
			restrict: 'EA',			
			scope: {
				data: '='		

			},
			link : link
		};
});
