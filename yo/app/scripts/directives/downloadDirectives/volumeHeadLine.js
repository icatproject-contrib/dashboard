(function() {
    'use strict';

    var app = angular.module('dashboardApp');


    //Directive to display headline data about download volume data.

    app.directive('volumeHeadLine', function(){
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

						var rawData = data.data[1];
						var dates = data.data[0];

						//Calculate the total volume of downloads. Will also find the highest so do not have to repeat.

						var total = 0;
						var largestIndex = 0;
						var largestVolume = 0;
						
						for(var i = 1;i<rawData.length;i++){
							var temp = rawData[i];

							if(temp!=="null"){
								total+=temp;
								if(temp>largestVolume){
									largestVolume = temp;
									largestIndex = i;
								}
							}
						}

						var roundedTotal = Math.round(total*100)/100

						var totalVolume = {
							title:"Total Volume",	
							result:	roundedTotal+' '+rawData[0]			
						}

						var largestVolume = {
							title:"Busiest Day",
							result: dates[largestIndex] + ' with '+largestVolume+ ' '+rawData[0]
						}

						//Push tot he array					

						displayData.push(totalVolume,largestVolume);

						$scope.formattedData = displayData;
									
					}
				});


			}
					
		};

	});


	
})();

