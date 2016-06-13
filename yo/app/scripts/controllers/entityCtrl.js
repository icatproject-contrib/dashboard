(function (){
	  'use strict';
angular.module('dashboardApp').controller('EntityCtrl', EntityCtrl);

EntityCtrl.$inject= ['$scope','entityService','$filter','$q','$element'];	


function EntityCtrl($scope,entityService, $filter,$q,$element){				
		
    		var vm=this;   	
    			
    		vm.dataCsv = [];
    			
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
	      	
	      		//Call the methods without providing a instrument or entity as to not overwrite a users selection.
	      		vm.updateInsDfCount();
    		 	vm.updateInsDfVolume();
    		 	vm.updateEntityCount();
    		 	vm.updateInvData();
    		 	vm.updateDfVolume();
	        	

	        }  	

	        //Will call all promises to update the data
    		vm.updatePage = function(){  

    			var instrument = vm.selectedInstrument;
    			var entity = vm.selectedEntity;
    		  

    		 	var insCountPromise = vm.updateInsDfCount(instrument,true);
    		 	var insVolumePromise = vm.updateInsDfVolume(instrument,true);
    		 	var entityCountPromise = vm.updateEntityCount(entity,true);
    		 	var invDataPromise = vm.updateInvData(true);
    		 	var dfVolumePromise = vm.updateDfVolume(true);

    		 	var groupPromise = $q.all([insCountPromise,insVolumePromise,entityCountPromise,invDataPromise,dfVolumePromise])
    		 	
    		 	groupPromise.then(function(){
    		 		
    		 		updateCSV();
    		 		
    		 	});	  		 	

	     
        	}

        	

        	//Updates the vm.dataCSV file with new values.
    		function updateCSV(){

    			vm.dataCsv = [

    		 			{
    		 				type:"insDfCount",
    		 				title:vm.insDataFileCount.title,
    		 				data:vm.insDataFileCount.rawData
    		 			},
    		 			{
    		 				type:"insDfVolume",
    		 				title:vm.insDataFileVolume.title,
    		 				data:vm.insDataFileVolume.rawData

    		 			},
    		 			{
    		 				type:"entityCount",
    		 				title:vm.countEntity.title,
    		 				data:vm.countEntity.rawData
    		 			},
    		 			{
    		 				type:"invDfCount",
    		 				title:vm.investigationDatafile.number.title,
    		 				data:vm.investigationDatafile.number.rawData
    		 			},
    		 			{
    		 				type:"invDfVolume",
    		 				title:vm.investigationDatafile.volume.title,
    		 				data:vm.investigationDatafile.volume.rawData
    		 			},    		 			
    		 			{
    		 				type:"dfVolume",
    		 				title:vm.dataFileVolume.title,
    		 				data:vm.dataFileVolume.rawData
    		 			}   		 			

    		 		]    		
    		}

        	
    		//Updates the datafile volume graph. Initialupload to prevent calling uploadCSV before the data has been uploaded.
        	vm.updateDfVolume = function(initialUpload){
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

					var total = getArrayTotal(dataForGraph.slice(1,dataForGraph.length));

					var volumeTotal = total === 0 ? "No Data":total;
					var volumeAverage = total === 0 ? "No Data":Math.round(total/(dates.length-1));


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
						rawData:responseData,
						headLine:[	
						{	
							title:"Total Datafile Volume ("+byteFormat+")",
							data:volumeTotal							
						},	
						{
							title:"Avergae Daily Datafile Volume ("+byteFormat+")",
							data:volumeAverage 
						}					

						]						
						
				    } 

				    if(!initialUpload){
				    	 updateCSV();
				    }
				
				   	
				});

        	}

        	//Updates the investigation volume and file count graph. Initialupload to see if it should update the CSV or not.
        	vm.updateInvData = function(initialUpload){

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
							"title":"Number of datafiles per investigation.",
							"rawData":responseData[0]
						},
						volume : {
							"data":volume,
							"title":"Volume of datafiles ("+byteFormat+") per investigation.",
							"rawData":responseData[1]
						},					
						description :  "This donut chart displays the number and volume of datafiles for the top 10 investigations.",
					    title :"Investigation datafile information"
					};

			
					if(!initialUpload){
				    	 updateCSV();
				    }	

				});		    

        	}
        	//updates the instrument datafile volume graph. Instrument: To pull data up for, initialUpload: if to updateCsv or not.
        	vm.updateInsDfVolume = function(instrument, initialUpload){    

        		if(instrument){
        			vm.insDfVolumeIns = instrument;
        		}  
        		else{
        			instrument = vm.insDfVolumeIns;
        		} 	
        			
				
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

					var total = getArrayTotal(dataForGraph.slice(1,dataForGraph.length));

					var volumeTotal = total;
					var volumeAverage = total === 0 ? 0:Math.round(total/(dates.length-1));

					byteFormat = byteFormat === undefined? "":byteFormat


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
						optionTitle:"Instrument",
						rawData:responseData,
						headLine:[	
						{	
							title:"Datafile Total Volume for "+instrument +"  ("+byteFormat+")" ,
							data:volumeTotal,							
						},	
						{
							title:"Daily Average Datafile volume for "+instrument +"  ("+byteFormat+")",
							data:volumeAverage
						}					

						]	
						
				    } 

				    if(!initialUpload){
				    	 updateCSV();
				    }	


				});		

        	}

        	//Updates instrument datafile count. Instrument: Data to look up for, initialUpload: to update the csv or not.
        	vm.updateInsDfCount = function(instrument,initialUpload){

        		if(instrument){
        			vm.insDfCountIns = instrument;
        		}  
        		else{
        			instrument = vm.insDfCountIns;
        		} 	
        		

    			return	entityService.getInstrumentFileCount(getStartDate(),getEndDate(), instrument).then(function(responseData){

    			
				
					var formattedData = formatData(responseData);
					

				    var totalAndAverage = getTotalAndAverage(formattedData);	 
				    console.log(totalAndAverage)


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
						optionTitle:"Instrument",
						rawData:responseData,
						headLine:[	
						{	
							title:"Datafile Total for "+instrument,
							data:totalAndAverage[0],							
						},	
						{
							title:"Daily Average Datafile creation for "+instrument,
							data:totalAndAverage[1]
						}					

						]
						
				    } 

					if(!initialUpload){
				    	 updateCSV();
				    }	
	

				});

    		}

    		//Updates the entity count graph. entity: to get data for, initialUpload: to update the csv or not.
    		vm.updateEntityCount = function(entity,initialUpload){

    			if(entity){
        			vm.entityCountEntity = entity;
        		}  
        		else{
        			entity = vm.entityCountEntity;
        		} 	

    			
    			return entityService.getEntityCount(getStartDate(),getEndDate(), entity).then(function(responseData){

    			

    				var formattedData = formatData(responseData);	

    				var totalAndAverage = getTotalAndAverage(formattedData);


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
						optionTitle:"Entity",
						rawData:responseData,
						headLine:[	
						{	
							title:"Total Number of "+entity,
							data:totalAndAverage[0],							
						},	
						{
							title:"Average Daily Creation of "+entity,
							data:totalAndAverage[1]
						}					

						]	
						
				    }

				    if(!initialUpload){
				    	 updateCSV();
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

    	    //Adds up all the items in an array
    	    function getArrayTotal(array){
    	    	var total = 0;

    	    	for(var i=0;i<array.length;i++){	
	    		    var temp = array[i];
	    		    if(temp!=="null"){
	    		    	total +=temp;
	    		    }	    			
	    		}

	    		return Math.round(total);
    	    }

    	    function getTotalAndAverage(array){

    	    	var numbers = array[1];

				var total = getArrayTotal(numbers.slice(1,numbers.length));
				
				var average = total === 0 ? 0:Math.round(total/(numbers.length-1));

				return [total,average]

    	    }

    		
    		
        }
	

})();