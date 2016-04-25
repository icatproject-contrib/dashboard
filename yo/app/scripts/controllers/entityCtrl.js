(function (){
	  'use strict';
angular.module('dashboardApp').controller('EntityCtrl', EntityCtrl);

EntityCtrl.$inject= ['$scope','googleChartApiPromise', 'entityService','$filter','$q'];	


function EntityCtrl($scope,googleChartApiPromise, entityService, $filter,$q){		
		
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

	        vm.initPage = function(){
	        	var getInstrumetNamesPromise = entityService.getInstrumetNames();
	        	var getEntityNamesPromise = entityService.getEntityNames();

	        	//Joinned promise to make sure all the data has been returned.

	        	var optionsPromise = $q.all([getInstrumetNamesPromise,getEntityNamesPromise]);

	        	optionsPromise.then(function(responseData){	  
	        		     	
	        		vm.instrumentNames = responseData[0];
	        		vm.selectedInstrument = vm.instrumentNames[0].name;

	        		vm.entityNames = responseData[1];
	        		vm.selectedEntity = vm.entityNames[0].name;
	        		

	        		//Only want to update the page once the instrument has been returned
	        		vm.updatePage();
	        	});       	

	        }    		

    		vm.updatePage = function(){	


    		 	vm.updateDfCount(vm.selectedInstrument);
    		 	vm.updateDfVolume(vm.selectedInstrument);
    		 	vm.updateEntityCount(vm.selectedEntity);


        	}

        	vm.updateDfVolume = function(instrument){
        		

        		//Have to set the time to midnight otherwise will use current time.
	     		var startDate = moment(vm.startDate).subtract(1,'seconds');			

				startDate.set('hour','00');
				startDate.set('minute','00');
				startDate.set('second','00');
							
				var endDate = Date.parse(vm.endDate);

				
				

				var instrumentDatafileVolume = entityService.getInstrumentFileVolume(startDate,endDate,instrument);				

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
						title:"Datafile Volume "+instrument,
						type:'line',
						zoom:true,
						xLabel:"Insertion Date",
						yLabel:"Volume of Datafiles "+byteFormat,
						selectOp:vm.instrumentNames,
						optionTitle:"Instrument"
						
				    } 

				});

        	}
        	vm.updateDfCount = function(instrument){
        		//Have to set the time to midnight otherwise will use current time.
	     		var startDate = moment(vm.startDate).subtract(1,'seconds');
	     		
	     			

				startDate.set('hour','00');
				startDate.set('minute','00');
				startDate.set('second','00');
							
				var endDate = Date.parse(vm.endDate);

    			var instrumentDatafileCount = entityService.getInstrumentFileCount(startDate,endDate, instrument);

    			instrumentDatafileCount.then(function(responseData){

				
					var formattedData = formatData(responseData);	


					vm.dataFileCount = {
						data:{
							x:"x",				 	 
				       	    columns : formattedData,
				       		types:{
				       			Number:'line',
				       		}
				       	},
				    	description : "This line graph shows the number of datafiles created for that instrument on a specific day. Please not this is only correct if you follow one investigation instrument per investigation",
						title:"Datafile Count "+instrument,
						zoom:true,
						xLabel:"Insertion Date",
						yLabel:"Number of Datafiles",
						selectOp:vm.instrumentNames,
						optionTitle:"Instrument"
						
				    } 

					

				});

    		}

    		vm.updateEntityCount = function(entity){

    			//Have to set the time to midnight otherwise will use current time.
	     		var startDate = moment(vm.startDate).subtract(1,'seconds');	
	     			

				startDate.set('hour','00');
				startDate.set('minute','00');
				startDate.set('second','00');
							
				var endDate = Date.parse(vm.endDate);

    			var entityCount = entityService.getEntityCount(startDate,endDate, entity);

    			entityCount.then(function(responseData){

    				var formattedData = formatData(responseData);	


					vm.countEntity = {
						data:{
							x:"x",				 	 
				       	    columns : formattedData,
				       		types:{
				       			Number:'line',
				       		}
				       	},
				    	description : "This Bar graph shows the number of "+entity+" created on each corresponding day.",
						title:entity + " Count",
						zoom:true,
						xLabel:"Insertion Date",
						yLabel:"Number of "+entity,
						selectOp:vm.entityNames,
						optionTitle:"Entity"
						
				    }

				  
    			});


    		}

    		//Formats data where the values are date and number
    		function formatData(data){


		    	var dates  = _.map(data, function(data){
					return data.date;
				});

				var numbers = _.map(data, function(data){
					return data.number;
				});

				dates.unshift("x");		
						
				numbers.unshift('Number');	 


				return [dates,numbers];

    		}    		

    		
    		
        }
	

})();