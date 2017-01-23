(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('headLine', function(){
    	return {
            restrict: 'EA',
            scope: {
                data: '=',
            },
            templateUrl: 'views/headLineTemplate.html',
            link: function ($scope, $element, $rootScope) {



                $scope.$watch('data', function (dataObject) {
                    if (dataObject) {
                        $scope.headLineData = dataObject;

                        $scope.dataLength = dataObject.length;
                    }




                });

            }

        }

    });

})();