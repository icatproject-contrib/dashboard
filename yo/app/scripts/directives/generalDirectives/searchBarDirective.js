(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('searchBar', function(){
    	return {
			restrict: 'EA',			
			scope: {
				csvData: '=',
                selectValues:'&',
                userOption:'=',	


			},

			templateUrl : 'views/searchBarTemplate.html',
		    controller: "SearchBarController as searchBarCtrl",
            

		    };
	});

    app.controller('SearchBarController', function($scope, $element, $attrs){
        var vm = this;

        vm.showUserSearch = $scope.userOption

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


        vm.updateSelection = function(){
           
            $scope.selectValues({startDate:vm.startDate,endDate:vm.endDate,userName:vm.userName});
        }

        var dropdownElement = $($element).find('.datetime-picker');


        //Watches for changes in csvData and modifies the link with the new data
        $scope.$watch('csvData', function(data){
           
            if(data){
                var CSV ='';
               data.forEach(function(entry){
                    CSV += JSONToCSVConvertor(entry.data,entry.title);

               });           
                    
                
           
                 //Initialize file format you want csv or xls
                var uri = 'data:text/csv;charset=utf-8,' + escape(CSV);
                
                
                
                //this trick will generate a temp <a /> tag
                link = document.createElement("a");    
                link.href = uri;
                
                var fileName = "Dashboard"
        
                link.download = fileName + ".csv";         
                
                
                
            }
        });		       
    
       
        //Create the link and append it to the csvAnchor class
        var link;

        var divElement = $element.find('.csvAnchor');

        divElement.append(link); 


        vm.downloadCsv = function(){
                link.click()
             
        } 


        function JSONToCSVConvertor(JSONData, ReportTitle) {  
            

            //If JSONData is not an object then JSON.parse will parse the JSON string in an Object
           var arrData = typeof JSONData != 'object' ? JSON.parse(JSONData) : JSONData;


            var CSV = ''; 

            CSV+= ReportTitle + '\r\n'; 
            
            var row = "";

            //This loop will extract the label from 1st index of on array
            for (var index in arrData[0]) {
                //Now convert each value to string and comma-seprated
                row += index + ',';
            }
            row = row.slice(0, -1);
            //append Label row with line break
            CSV += row + '\r\n';
                        

            //1st loop is to extract each row
            for (var i = 0; i < arrData.length; i++) {
                var row = "";
                //2nd loop will extract each column and convert it in string comma-seprated
                for (var index in arrData[i]) {
                    row += '"' + arrData[i][index] + '",';

                }
                row.slice(0, row.length - 1);
                //add a line break after each row
                CSV += row + '\r\n';
            }

            return CSV

            
        }        

       

    });   
               

        

       


})();