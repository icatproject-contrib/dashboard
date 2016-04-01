(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('dateTimePicker', function(){
    	return {
			restrict: 'EA',			
			scope: {
				value: '=ngModel',					

			},

			templateUrl : 'views/dateTimeTemplate.html',
		    controller: "DatetimePickerController as datetimePickerController",
		            scope: {
		                value: '=ngModel'
		            }

		        };
		    });

		    app.controller('DatetimePickerController', function($scope, $element, $attrs){
		        var that = this;
		        var dropdownElement = $($element).find('.datetime-picker');		       
		        this.datetime = new Date();
		       

		        $scope.$watch('value', function(){
		            if($scope.value){
		                
		             

		                that.datetime = new Date(Date.parse($scope.value));
		            } else {
		                that.datetime = new Date();
		            }

		        }, true);

		        this.clear = function(){
		            $scope.value = "";
		            this.close();
		        };

		        this.submit = function(){               

		            console.log("work")
		            $scope.value = this.datetime;
		            
		            this.close();
		        };

		        this.close = function(){
		            $(dropdownElement).css('display', 'none');
		        };
		    });


		})();