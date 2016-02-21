(function() {
    'use strict';

    var app = angular.module('dashboardApp');


    //Directive to display headline data about download volume data.

    app.directive('countHeadLine', function(){
    	return {
			restrict: 'EA',			
			scope: {
				data: '=',					

			},

			templateUrl : 'views/headLineTemplate.html',

			link:function($scope){

							

				$scope.$watch('data', function(data){
					if(data){
						
						var displayData = [];

					

						var rawData = data[1];
						var dates = data[0];

						var largestIndex = 0;
						var largestVolume = 0;

						var total= 0;

						for(var i = 1;i<rawData.length;i++){
							var temp = rawData[i];
							
							total+=temp;
							if(temp>largestVolume){
								largestCount = temp;
								largestIndex = i;
							}
							
						}

						var totalDownloads = {
							title:"Total Downloads ",	
							result: total			
						}

						var largestCount ={
							title:"Busiest Day",
							result: dates[largestIndex] + ' with '+largestCount+ ' '+rawData[0]
						}

						//Push tot he array					

						displayData.push(totalDownloads,largestCount);

						$scope.formattedData = displayData;
									
					}
				});


			}
					
		};

	});


	
})();

