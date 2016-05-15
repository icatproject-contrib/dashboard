(function (){
	  'use strict';
angular.module('dashboardApp').controller('EntityCtrl', EntityCtrl);

EntityCtrl.$inject= ['$scope','entityService','$filter','$q','$element'];	


function EntityCtrl($scope,entityService, $filter,$q,$element){				
		
    		var vm=this;   	
    			
    		vm.userOption = false;	
    		
    		//Initialise the page with the values it requires for the menus
	        vm.initPage = function(){
	        	//Set default dates
	        	vm.endDate = new Date();
    
        		vm.startDate = new Date(new Date().setDate(new Date().getDate()-10)); 

	        	var getInstrumetNamesPromise = entityService.getInstrumetNames();
	        	var getEntityNamesPromise = entityService.getEntityNames();

	        	//Joinned promise to make sure all the data has been returned.

	        	var optionsPromise = $q.all([getInstrumetNamesPromise,getEntityNamesPromise]);

	        	optionsPromise.then(function(responseData){	  
	        		     	
	        		vm.instrumentNames = responseData[0];
	        		vm.entityNames = responseData[1];
	        		console.log(vm.entityNames)

	        		if(vm.instrumentNames.length == 0){
	        			vm.selectedInstrument = "No Data";
	        		}else{
	        			vm.selectedInstrument = vm.instrumentNames[0].name;
	        		}

	        		if(vm.entityNames.length==0){
	        			vm.selectedEntity = "No Data";
	        		}
	        		else{
	        			vm.selectedEntity = vm.entityNames[0].name;
	        		}

	        		
	        		
	        		
	        		
	        		

	        		//Only want to update the page once the instrument has been returned
	        		vm.updatePage();
	        	});       	

	        }  

	        //Updates the date and the page
	        vm.updateDates = function(startDate,endDate){
	        	vm.startDate = startDate;
	        	vm.endDate = endDate;
	      
	        	vm.updatePage();

	        }  	

	        //Will call all promises to update the data
    		vm.updatePage = function(){  

    			var instrument = vm.selectedInstrument;
    			var entity = vm.selectedEntity;
    		  

    		 	var insCountPromise = vm.updateInsDfCount(instrument);
    		 	var insVolumePromise = vm.updateInsDfVolume(instrument);
    		 	var entityCountPromise = vm.updateEntityCount(entity);
    		 	var invDataPromise = vm.updateInvData();
    		 	var dfVolumePromise = vm.updateDfVolume();

    		 	var groupPromise = $q.all([insCountPromise,insVolumePromise,entityCountPromise,invDataPromise,dfVolumePromise])

    		 	groupPromise.then(function(responseData){
    		 		vm.dataCsv = [

    		 			["Datafile count for "+instrument,responseData[0]],
    		 			["Datafile volume for "+instrument,responseData[1]],
    		 			["Entity count for "+entity,responseData[2]],
    		 			["Investigation Datafile count",responseData[3][0]],
    		 			["Investigation Datafile volume",responseData[3][1]],
    		 			["Datafile Volume",responseData[4]]

    		 		]

    		 		
    		 	});	


	     
        	}

        	vm.updateDfVolume = function(){
        		return entityService.getDatafileVolume(getStartDate(),getEndDate()).then(function(responseData){

				
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
				    	description : "This bar graph shows the volume of datafiles created between the specified dates",
						title:"Datafile Volume",					
						zoom:true,
						xLabel:"Insertion Date",
						yLabel:"Volume of Datafiles "+byteFormat,
						
						
				    } 

				 return responseData;

				});

        	}

        	vm.updateInvData = function(){

        		var investigationDatafileCountPromise = entityService.getInvestigationDatafileCount(getStartDate(),getEndDate());

    		 	var investigationDatafileVolumePromise= entityService.getInvestigationDatafileVolume(getStartDate(),getEndDate());

    		 	var investigationDatafilePromise = $q.all([investigationDatafileVolumePromise,investigationDatafileCountPromise]);

    		 	return investigationDatafilePromise.then(function(responseData){  

    		 					
					var volumeRaw = _.map(responseData[0], function(data){
						return data.value;
					});

					var investigationId = _.map(responseData[1], function(data){
							return data.investigationId;

					});

					var frequency = _.map(responseData[1], function(data){
							return [data.investigationId,data.value];
					});

					var largestVolume = Math.max.apply(Math,volumeRaw);

					var filteredData = $filter('bytes')(volumeRaw,largestVolume);	
					
					var formattedVolume = filteredData[0];
					var byteFormat = filteredData[1]; 			

					//Combine the data into one array for the c3.js donut.
					var volume =  formattedVolume.map(function(value,index){
					    return [investigationId[index], formattedVolume[index]];
					});

					
					vm.investigationDatafile = {
						datasets:["number","volume"],
						number : {
							"data":frequency,
							"title":"Number of datafiles per investigation."
						},
						volume : {
							"data":volume,
							"title":"Volume of datafiles ("+byteFormat+") per investigation."
						},					
						description :  "This donut chart displays the number and volume of datafiles for the top 10 investigations.",
					    title :"Investigation datafile information"
					};

					return responseData;
			

				});		    

        	}
        	
        	vm.updateInsDfVolume = function(instrument){       		
				
				return entityService.getInstrumentFileVolume(getStartDate(),getEndDate(),instrument).then(function(responseData){

				
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


					vm.insDataFileVolume = {
						data:{
							x:"x",				 	 
				       	    columns : formattedData,
				       		types:{
				       			Volume:'bar',
				       		}
				       	},	
				    	description : "This bar graph shows the volume of datafiles created for that instrument on a specific day. Please not this is only correct if you follow one investigation instrument per investigation",
						title:"Datafile Volume "+instrument,
						type:'line',
						zoom:true,
						xLabel:"Insertion Date",
						yLabel:"Volume of Datafiles "+byteFormat,
						selectOp:vm.instrumentNames,
						optionTitle:"Instrument"
						
				    } 

				 return responseData;

				});



		

        	}
        	vm.updateInsDfCount = function(instrument){
        		

    		return	entityService.getInstrumentFileCount(getStartDate(),getEndDate(), instrument).then(function(responseData){

    			
				
					var formattedData = formatData(responseData);	


					vm.insDataFileCount = {
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

					return responseData

				});

    		}

    		vm.updateEntityCount = function(entity){

    			
    			return entityService.getEntityCount(getStartDate(),getEndDate(), entity).then(function(responseData){

    			

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

				  return responseData
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