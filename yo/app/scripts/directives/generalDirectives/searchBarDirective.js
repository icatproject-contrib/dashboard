(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('searchBar', function(){
    	return {
			restrict: 'EA',			
			scope: {
				csvData: '=',
                dateOptions:'&',				

			},

			templateUrl : 'views/searchBarTemplate.html',
		    controller: "SearchBarController as searchBarCtrl",
            

		    };
	});

    app.controller('SearchBarController', function($scope, $element, $attrs){
        var vm = this;

        vm.format =  'yyyy-MM-dd';

        vm.endDate = new Date();
    
        vm.startDate = new Date(new Date().setDate(new Date().getDate()-10));       

        //Date selection for the overall page
        vm.isStartDateOpen = false;
        vm.isEndDateOpen = false;       

            vm.openStartDate = function(){
            vm.isStartDateOpen = true;
            vm.isEndDateOpen = false;
        };

        vm.openEndDate = function(){
            
            vm.isStartDateOpen = false;
            vm.isEndDateOpen = true;
        };


        vm.updateDates = function(){
           
            $scope.dateOptions({startDate:vm.startDate,endDate:vm.endDate});
        }

        var dropdownElement = $($element).find('.datetime-picker');		       
    
        var CSV = 'ttttttt';

        var fileName = "test"
        //Initialize file format you want csv or xls
        var uri = 'data:text/csv;charset=utf-8,' + escape(CSV);
        
        // Now the little tricky part.
        // you can use either>> window.open(uri);
        // but this will not work in some browsers
        // or you will not get the correct file extension    
        
        //this trick will generate a temp <a /> tag
        var link = document.createElement("a");    
        link.href = uri;
        
        //set the visibility hidden so it will not effect on your web-layout
        
        link.download = fileName + ".csv";
        
        var divElement = $element.find('.csvAnchor');

        divElement.append(link); 


        vm.downloadCsv = function(){
                link.click()
                console.log("Download")
            }   

        $scope.$watch('csvData', function(data){
            if($scope.csvData){
                console.log(data)
              }
          });

    });   
               

        

       


})();