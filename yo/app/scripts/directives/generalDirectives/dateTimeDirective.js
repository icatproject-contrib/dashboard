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
            

		    };
	});

    app.controller('DatetimePickerController', function($scope, $element, $attrs){
         var vm = this;
        var dropdownElement = $($element).find('.datetime-picker');		       
        vm.datetime = new Date();
        vm.placeholder = $attrs.placeholder;
       

        $scope.$watch('value', function(){
            if($scope.value){
                
                var segments = $scope.value.split(/[-:\s]+/);
                var year = segments[0];
                var month = segments[1] || "01";
                var day = segments[2] || "01";
                var hours = segments[3] || "00";
                var minutes = segments[4] || "00";
                var seconds = segments[5] || "00";

                year = year + '0000'.slice(year.length, 4);
                month = month + '00'.slice(month.length, 2);
                day = day + '00'.slice(day.length, 2);
                hours = hours + '00'.slice(hours.length, 2);
                minutes = minutes + '00'.slice(minutes.length, 2);
                seconds = seconds + '00'.slice(seconds.length, 2);

                if(parseInt(month) == 0) month = '01';
                if(parseInt(day) == 0) day = '01';

                var value = year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
             

                vm.datetime = new Date(Date.parse($scope.value));
            } else {
                vm.datetime = new Date();
            }

        }, true);

        vm.clear = function(){
            $scope.value = "";
            vm.close();
        };

        vm.submit = function(){               

           var year = this.datetime.getFullYear();
            var month = this.datetime.getMonth() + 1;
            if(month < 10) month = "0" + month;
            var day = this.datetime.getDate();
            if(day < 10) day = "0" + day;
            var hours = this.datetime.getHours();
            if(hours < 10) hours = "0" + hours;
            var minutes = this.datetime.getMinutes();
            if(minutes < 10) minutes = "0" + minutes;
            var seconds = this.datetime.getSeconds();
            if(seconds < 10) seconds = "0" + seconds;

            
            $scope.value = year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds; 
                        
            vm.close();
        };

        this.close = function(){
            $(dropdownElement).css('display', 'none');
        };
    });


})();