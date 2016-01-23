angular.module('dashboardApp').directive('timeSeries', function(){


	function link(scope,element,attr){
		$(element).css("position","static")

		scope.$watch('data', function(){

			if(scope.data !== undefined){	
			
				var chart = c3.generate({
					 bindto:element[0],
					 data: {	
					 	 x:"x",				 	 
		       			 columns : [
		       			 	scope.data[0],
		       			 	scope.data[1]
		       			 ],
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