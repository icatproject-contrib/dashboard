(function(){

	'use strict';

	angular.module('dashboardApp').controller('DownloadCtrl', DownloadCtrl);

	DownloadCtrl.$inject= ['DownloadService','$scope','googleChartApiPromise'];

	

	function DownloadCtrl(DownloadService, $scope, googleChartApiPromise){	

		var self=this;

		var globalIdentifiers = ['Country', 'number of Downloads'];
		
		self.format =  'yyyy-MM-dd';

		self.endDate = new Date();
		
		self.startDate = new Date(new Date().setDate(new Date().getDate()-10));		

		self.isStartDateOpen = false;
        self.isEndDateOpen = false;

		self.openStartDate = function(){
            this.isStartDateOpen = true;
            this.isEndDateOpen = false;
        };

        self.openEndDate = function(){
            this.isStartDateOpen = false;
            this.isEndDateOpen = true;
        };	

        

        var downloadMethodTypes = DownloadService.getDownloadMethodTypes();

        	downloadMethodTypes.then(function(responseData){

        		self.downloadMethodTypes = responseData;

        	});

		self.updatePage = function(){	

			//Have to set the time to midnight otherwise will use current time.
     		var startDate = moment(self.startDate);			

			startDate.set('hour','00');
			startDate.set('minute','00');
			startDate.set('second','00');

						
			var endDate = Date.parse(self.endDate);
			var userName = self.userName;
			var method = self.selectedMethod;	

		
			
			var downloadMethod = DownloadService.getDownloadMethod(startDate,endDate, userName);
			var downloadCount = DownloadService.getDownloadFrequency(startDate,endDate, userName, method);
			var downloadBandwidth = DownloadService.getDownloadBandwidth(startDate,endDate, userName, method);
			var downloadSize = DownloadService.getDownloadSize(startDate,endDate, userName, method);			
			var globalDownloadLocation = DownloadService.getGlobalDownloadLocation(startDate,endDate, userName, method);
			var localDownloadLocation = DownloadService.getLocalDownloadLocation(startDate,endDate, userName, method);
			var downloadEntityAge = DownloadService.getDownloadEntityAge(startDate,endDate,userName,method);

			downloadEntityAge.then(function(responseData){					

				
				var age  = _.map(responseData, function(responseData){
					return responseData.age;
				});

				var number = _.map(responseData, function(responseData){
					return responseData.number;
				});

				age.unshift("Days old");		
							
				number.unshift('Number of Files');				
				
				self.downloadEntityAge = [age,number];
				
				

			});




			localDownloadLocation.then(function(responseData){
				
				googleChartApiPromise.then(function(){
					var dataTable = new google.visualization.DataTable();

					dataTable.addColumn('number', 'Lat');                                
 					dataTable.addColumn('number', 'Long'); 					
 					dataTable.addColumn('string', 'City');
    				dataTable.addColumn('number', 'number');
    			

 					var dataArray = _.map(responseData, function(responseData){
						return [responseData.latitude, responseData.longitude,responseData.city,responseData.number];
					
					});

					dataTable.addRows(dataArray);

					var localChart = {};
				    localChart.type = "GeoChart";
				    localChart.data = dataTable;
				    localChart.options ={
				    	region:'GB',				    	
				    	colorAxis: {colors: ['grey', '#e31b23']}
				    };				   
				    self.localChart = localChart;
					
				});

				
			});


			globalDownloadLocation.then(function(responseData){ 			   				  		
				

			    responseData = _.map(responseData, function(responseData){
					return [responseData.countryCode, responseData.number];
				});
	    		
	    		responseData.unshift(globalIdentifiers);

	    		var worldChart = {};
			    worldChart.type = "GeoChart";
			    worldChart.data = responseData;
			    worldChart.options = {
			    	colorAxis: {colors: ['grey', '#e31b23']}
			    };
			   
			    self.worldChart = worldChart;
			    
			});
			

			downloadMethod.then(function(responseData){	
				  	var data = responseData;			  			  	
				    self.downloadMethod = _.map(data, function(data){
						return [data.method, data.number];
					});
		    });

		    downloadCount.then(function(responseData){

		    	var data = responseData;
		    	

		    	var dates  = _.map(data, function(data){
					return data.date;
				});

				var numbers = _.map(data, function(data){
					return data.number;
				});

				

			    //Calculate metrics and assign to to controller variables
			    self.getCountTotal = function(){		    		
		    		var total = 0;	    		
		    		
		    		for(var i=1;i<numbers.length;i++){	    			
		    			total +=numbers[i];
		    		}

		    		return total;
	    		}

	    		self.getCountBusiestDay = function(){
	    		var data = [dates,numbers];

	    		var busiestIndex = 0;
	    		var largestDay = 0;	    		
	    		
	    		for(var i=1;i<data[1].length;i++){	    			
	    			if(data[1][i] > largestDay){
	    				busiestIndex = i;
	    				largestDay = data[1][i];
	    			}
	    		}

	    		return data[0][busiestIndex] +" with "+largestDay;
	    		}

	    		dates.unshift("x");		
							
				numbers.unshift('Number of Downloads');	    		
					
			    self.downloadCount = [dates,numbers];

					
		    });

		    downloadBandwidth.then(function(responseData){
		    	
		    	self.rawBandwidthData = responseData;

		    	var bandwidthData = [];

		    	var dateArray = getDateArray(startDate,endDate)
		    	var formattedDateArray = getFormattedDateArray(startDate,endDate)

		    	formattedDateArray.unshift("x")	    	
		    	 
		    	bandwidthData.push(formattedDateArray) 

		    	
		    	responseData.forEach( function(download){
		    		var downloadArray = [];		    		

		    		var start = moment(download.startDate, "YYYY-MM-DD HH:mm:ss");
		    		var end = moment(download.endDate , "YYYY-MM-DD HH:mm:ss");

	    			for(var i = 0; i< dateArray.length; i++){
	    				var current = dateArray[i];

	    				if((current.isAfter(start,'day') || current.isSame(start,'day'))&&(current.isBefore(end,'day') || current.isSame(end,'day'))){
	    					
	    					downloadArray.push(download.bandwidth)
	    				}
	    				else{
	    					downloadArray.push("null")
	    				}	    			    
	    			}

	    			downloadArray.unshift(download.id)
	    			   
	    			bandwidthData.push(downloadArray)
		    			
					});
		    	
		    	self.downloadBandwidth = bandwidthData;  	
		    	
		    	
		    	self.highestBandwidth = Math.max.apply(Math,responseData.map(function(data){return data.bandwidth;}))+"MB/S";
		    	self.lowestBandwidth  = Math.min.apply(Math,responseData.map(function(data){return data.bandwidth;}))+"MB/S";

		    	

		    });

			
	    	downloadSize.then(function(responseData){

	    		var data = responseData;

	    		var dates  = _.map(data, function(data){
					return data.date;
				});

				var byteArray = _.map(data, function(data){
					
					return Math.round(data.number);
				});

				//Need to format the bytes into human readable format
				var formattedData = byteArrayToSize(byteArray)

				

				//Download number metrics

		    	self.getTotalnumber = function(){
		    		var total = 0.0;    		    			    		
		    		

		    		for(var i=0;i<byteArray.length;i++){
		    			if(total!==null){	    			
		    				total +=byteArray[i];
		    			}
		    		}

		    		return bytesToSize(total);
		    	}
	    	
		    	//Busiest day metric
		    	self.getBusiestDaynumber = function(){

		    		var data = [dates,byteArray];

		    		var busiestIndex = 0;
		    		var largestDay = 0;	    		
		    		
		    		for(var i=1;i<data[1].length;i++){	    			
		    			if(data[1][i] > largestDay){
		    				busiestIndex = i;
		    				largestDay = data[1][i];
		    			}
		    		}

		    		return data[0][busiestIndex] +" with "+bytesToSize(largestDay);

		    	}	    	

		    	//Adding on to the beginning of the array to allow c3 to read the data
				dates.unshift("x");		
							
				var dataForGraph = formattedData[0];
				dataForGraph.unshift(formattedData[1]);
		

	    		self.downloadSize = { 	
	    			data:[dates,dataForGraph],
	    			byteFormat:formattedData[1]

	    		};
	    	});

			

		};	
		self.globalLocationDescription = "This map displays the number of downloads that have occured within each country during the requested period.";
		self.localLocationDescription = "This map displays the number of downloads that have occured within the selected area. The circles center position is based on the longitude and latitude of the download.";
		
		
		
		
 
	}


	function bytesToSize(bytes) {
   		var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
   		if (bytes == 0) return '0 Byte';

   		var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));

   		return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[i];
};


	/*
	 * Function to convert an array of bytes into an array human readable form bytes. 
	 *
	 */
	 
	function byteArrayToSize(byteArray) {
   		var sizes = ['Bytes', 'Kilobytes', 'Megabytes', 'Gigabytes', 'Terabytes'];

   		

   		var max = 0;
   		var formattedArray = [];
   		
   		//Find the highest bytes value to use as a marker to convert the rest of the bytes into. Quicker to use a for loop then Math.max.
   		for(var i=0;i<byteArray.length;i++){
   			var bytes = byteArray[i];
   			if(bytes>max){
   				max = bytes;
   			}
   		}
   		
   		//This then selects what type of byte it should be e.g. byte or KB. byteFormat refers to the position in the sizes array.
   		var byteFormat = parseInt(Math.floor(Math.log(max) / Math.log(1024)));


   		//With the known type of byte we now need to format each one and add it to the array.
   		for(var i=0;i<byteArray.length;i++){
   			var bytes = byteArray[i];
   			   			
   			if(parseInt(bytes) === 0){
   				//Null is placed so that c3 ignores those values
   				formattedArray.push("null");
   			}
   			else{
   				//Convert it to the correct byte value and then round to 2 decimal places.
   				formattedArray.push((Math.round(bytes / Math.pow(1024, byteFormat)*100)/100));
   			}
   		}

   		   		   
   		return [formattedArray,sizes[byteFormat]];
};

	
	function getDateArray(startDate,stopDate) {
		var dateArray = [];
	    var currentDate = moment(startDate);
	    while (currentDate.isBefore(stopDate) || currentDate.isSame(stopDate)) {
	        dateArray.push( moment(currentDate) )
	        currentDate = moment(currentDate).add(1, 'days');
	    }
	    return dateArray;
	}

	function getFormattedDateArray(startDate,stopDate){
		var dateArray = [];
	    var currentDate = moment(startDate);
	    while (currentDate.isBefore(stopDate) || currentDate.isSame(stopDate)) {
	        dateArray.push( moment(currentDate).format("YYYY-MM-DD") )
	        currentDate = moment(currentDate).add(1, 'days');
	    }
	    return dateArray;

	}

	function mapJsonDataToArry(data){		
		    		
		var dates  = _.map(data, function(data){
			return data.date;
		});

		var numbers = _.map(data, function(data){
			return data.number;
		});

		dates.unshift("x");		
					
		numbers.unshift('Count');

		var result = [dates,numbers];

		return result;

	}






})();