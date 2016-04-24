(function (){
	  'use strict';
angular.module('dashboardApp').controller('EntityCtrl', EntityCtrl);

EntityCtrl.$inject= ['$scope','googleChartApiPromise', 'entityService','$filter'];	


function EntityCtrl($scope,googleChartApiPromise, entityService, $filter){		
		
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

	        entityService.getInstrumetNames().then(function(responseData){	        	
	        	vm.instrumentNames = responseData;
	        	vm.selectedInstrument = responseData[0].name;
	        });

    		

    		 vm.updatePage = function(){	

    		 	//Have to set the time to midnight otherwise will use current time.
	     		var startDate = moment(vm.startDate).subtract(1,'seconds');			

				startDate.set('hour','00');
				startDate.set('minute','00');
				startDate.set('second','00');
							
				var endDate = Date.parse(vm.endDate);

				var instrument = vm.selectedInstrument;

				vm.updateDfCount();
				

				var instrumentDatafileVolume = entityService.getInstrumentFileVolume(startDate,endDate,instrument);

				//var entityCount = entityService.getEntityCount(startDate,endDate,entity);


				

				instrumentDatafileVolume.then(function(responseData){

					var data = responseData;		   

					var dates  = _.map(data, function(data){
						return data.date;
					});

					var byteArray = _.map(data, function(data){
					
						return Math.round(data.number);
					});

					//Need to format the bytes into human readable format
					var formattedData = $filter('bytes')(byteArray);

					var dataForGraph = formattedData[0];

					var byteFormat = formattedData[1];

					dates.unshift("x");		
							
					dataForGraph.unshift('Volume');	 

					var formattedData = [dates,dataForGraph];


					vm.dataFileVolume = {
						data:{
							x:"x",				 	 
				       	    columns : formattedData,
				       		types:{
				       			Volume:'bar',
				       		}
				       	},	
				    	description : "This line graph shows the volume of datafiles created for that instrument on a specific day. Please not this is only correct if you follow one investigation instrument per investigation",
						title:"Datafile Volume "+vm.selectedInstrument,
						type:'line',
						zoom:true,
						xLabel:"Insertion Date",
						yLabel:"Volume of Datafiles "+byteFormat,
						
				    } 

				});


        	}
        	vm.updateDfCount = function(instrument){
        		//Have to set the time to midnight otherwise will use current time.
	     		var startDate = moment(vm.startDate).subtract(1,'seconds');
	     		console.log(instrument)			

				startDate.set('hour','00');
				startDate.set('minute','00');
				startDate.set('second','00');
							
				var endDate = Date.parse(vm.endDate);

    			var instrumentDatafileCount = entityService.getInstrumentFileCount(startDate,endDate, instrument);

    			instrumentDatafileCount.then(function(responseData){

					var data = responseData;		    	

			    	var dates  = _.map(data, function(data){
						return data.date;
					});

					var numbers = _.map(data, function(data){
						return data.number;
					});

					dates.unshift("x");		
							
					numbers.unshift('Number');	 

					var formattedData = [dates,numbers];


					vm.dataFileCount = {
						data:{
							x:"x",				 	 
				       	    columns : formattedData,
				       		types:{
				       			Number:'line',
				       		}
				       	},
				    	description : "This line graph shows the number of datafiles created for that instrument on a specific day. Please not this is only correct if you follow one investigation instrument per investigation",
						title:"Datafile Count "+vm.selectedInstrument,
						zoom:true,
						xLabel:"Insertion Date",
						yLabel:"Number of Datafiles",
						selectOp:vm.instrumentNames,
						
				    } 

					

				});

    		}    		

    		
    		
        }
	

})();