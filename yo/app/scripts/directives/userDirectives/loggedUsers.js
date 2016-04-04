(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('loggedUsers', function(){
        return {
            restrict: 'EA',         
            scope: {
                data: '=',                  

            },
            templateUrl: 'views/loggedUsers.html',
            

            link:function($scope,$element){
                
                
                

                $scope.$watch('data', function(dataObject){
                    if(dataObject){
                        console.log(dataObject)
                        $scope.data = dataObject;        
                        
                                           
                    }
                });


                    }
                    
                };

            });


    
})();

