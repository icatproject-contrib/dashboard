(function (){
	  'use strict';
angular.module('dashboardApp').controller('EntityCtrl', EntityCtrl);

EntityCtrl.$inject= ['$scope','googleChartApiPromise', 'entityService'];	


function EntityCtrl($scope,googleChartApiPromise, entityService){		
		
    		var vm=this;	
    		

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


    		

    		 vm.updatePage = function(){	

    		 	//Have to set the time to midnight otherwise will use current time.
	     		var startDate = moment(vm.startDate);			

				startDate.set('hour','00');
				startDate.set('minute','00');
				startDate.set('second','00');
							
				var endDate = Date.parse(vm.endDate);


				var instrumentDatafileCount = entityService.getInstrumentDatafileCount(startDate,endDate, "WISH");


				instrumentMeta.then(function(responseData){
					console.log(responseData)

				});


        	}

    		
    		}

	

})();