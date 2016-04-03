(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('byteFilter', function(){
    	return {
			restrict: 'EA',			
			scope: {
				value: '=ngModel',					

			},

			templateUrl : 'views/byteFilterTemplate.html',
		    controller: "ByteFilterController as byteCtrl",
            

		    };
	});

    app.controller('ByteFilterController', function($scope, $element, $attrs){
         var vm = this;

        vm.formats = ['B', 'KB', 'MB', 'GB', 'TB'];

        $scope.appendToEl = $($element).find('#modal-body');
        console.log($scope.appendToEl)
               
        vm.selectedFormat = 'B'
        vm.isCollapsed = false;

        $scope.$watch('input', function(){

            if($scope.input){
                
                vm.parseInput($scope.input)              
            
                
            } else {
                $scope.value='';
            }

        }, true);

        
        vm.update = function(format){ 
            vm.selectedFormat = format; 
            vm.parseInput($scope.input)           
           
                        
            
        };

        vm.parseInput= function(input){
            var format = vm.selectedFormat;
           


            if(format==='B'){  
                if(input.slice(-1)=="."){
                    input = parseInt(input);
                }                  
                $scope.value=input;                
               
            }
            else if(format==='KB'){                
                $scope.value = (input * Math.pow(1024,1));               
                console.log($scope.value)

                 
            }
            else if(format==='MB'){
                $scope.value = (input * Math.pow(1024,2));

                 
            }
            else if(format==='GB'){
                $scope.value = (input * Math.pow(1024,3));
                  
                  
            }
            else if(format==='TB'){
                $scope.value = (input * Math.pow(1024,4));
                 
                 
            }
        }

       
    });


})();