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

    		 	var investigationDatafileCountPromise = entityService.getInvestigationDatafileCount(getStartDate(),getEndDate());

    		 	var investigationDatafileVolumePromise= entityService.getInvestigationDatafileVolume(getStartDate(),getEndDate());

    		 	var investigationDatafilePromise = $q.all([investigationDatafileCountPromise,investigationDatafileVolumePromise]);

    		 	investigationDatafilePromise.then(function(responseData){    					
			
					var number = _.map(responseData[0], function(data){
							return [data.investigationId, data.value];
					});

					var volumeMethods = _.map(responseData[1], function(data){
							return data.investigationId;
					});

					var volumeRaw = _.map(responseData[1], function(data){
							return data.value;
					});

					var largestVolume = Math.max.apply(Math,volumeRaw);

					//Conver the data to human readable format.
					var formattedVolume = $filter('bytes')(volumeRaw,largestVolume);
					var formattedVolumeData = formattedVolume[0];
					

					var byteFormat = formattedVolume[1]; 

					//Combine the data into one array for the c3.js donut.
					var volume =  formattedVolumeData.map(function(value,index){
					    return [volumeMethods[index], formattedVolumeData[index]];
					});

					
					
					vm.investigationDatafile = {
						datasets:["number","volume"],
						number : {
							"data":number,
							"title":"Number of datafiles per investigation."
						},
						volume : {
							"data":volume,
							"title":"Volume of datafiles ("+byteFormat+") per investigation."
						},					
						description :  "This donut chart displays the number and volume of datafiles for the top 10 investigations.",
					    title :"Investigation datafile information"
					};

				});		    		 	


        	}

        	vm.updateDfVolume = function(instrument){
        		
				

				var instrumentDatafileVolume = entityService.getInstrumentFileVolume(getStartDate(),getEndDate(),instrument);				

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
        		

    			var instrumentDatafileCount = entityService.getInstrumentFileCount(getStartDate(),getEndDate(), instrument);

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

    			
    			var entityCount = entityService.getEntityCount(getStartDate(),getEndDate(), entity);

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

    		//Gets the start date with its formatted values
    		function getStartDate(){

    			var startDate = moment(vm.startDate).subtract(1,'seconds');   			

				startDate.set('hour','00');
				startDate.set('minute','00');
				startDate.set('second','00');

				return startDate;

    		}  	

    		//Gets the end date with its formatted values
    		function getEndDate(){
    			       
    			return Date.parse(vm.endDate);
    			
    	    }	

    		
    		
        }
	

})();