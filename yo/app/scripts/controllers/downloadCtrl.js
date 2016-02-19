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
		
			var startDate = Date.parse(self.startDate);
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

				age.unshift("x");		
							
				number.unshift('number of Files');
				
				

				
				
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
							
				numbers.unshift('Count');	    		
					
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

		    	//Bandwidth metrics

		    	var bandwidthMetrics = getBandwidthMetrics(responseData);
		    	

		    	self.lowestBandwidth = bandwidthMetrics[1]+"MB/S";
		    	self.heighestBandwidth = bandwidthMetrics[0]+"MB/S";

		    	

		    });

			
	    	downloadSize.then(function(responseData){
	    		var data = responseData;

	    		var dates  = _.map(data, function(data){
					return data.date;
				});

				var numbers = _.map(data, function(data){
					return Math.round(data.number/1000000);
				});

				//Download number metrics

		    	self.getTotalnumber = function(){
		    		var total = 0;    		    			    		
		    		
		    		for(var i=1;i<numbers.length;i++){	    			
		    			total +=numbers[i];
		    		}

		    		return total +" Megabytes";
		    	}
	    	

		    	self.getBusiestDaynumber = function(){
		    		var data = [dates,numbers];

		    		var busiestIndex = 0;
		    		var largestDay = 0;	    		
		    		
		    		for(var i=1;i<data[1].length;i++){	    			
		    			if(data[1][i] > largestDay){
		    				busiestIndex = i;
		    				largestDay = data[1][i];
		    			}
		    		}

		    		return data[0][busiestIndex] +" with "+largestDay +" Megabytes";

		    	}	    	

				dates.unshift("x");		
							
				numbers.unshift('Count');				

	    		self.downloadSize = [dates,numbers];
	    	});

			

		};	
		self.globalLocationDescription = "This map displays the number of downloads that have occured within each country during the requested period.";
		self.localLocationDescription = "This map displays the number of downloads that have occured within the selected area. The circles center position is based on the longitude and latitude of the download.";
		
		
		
		
 
	}

	/*Calculates the highest and lowest bandiwdth from the provided data.
	* 
	*/
	function getBandwidthMetrics(data) {     		

    		var highest = 0;
    		var lowest = 9999999;
    		
	    	data.forEach( function(download){
	    		if(download.bandwidth >highest){
	    			highest = download.bandwidth;
	    		}
	    		if(download.bandwidth < lowest && download.bandwidth !=='0.0'){
	    			lowest = download.bandwidth;
	    		}
	    	});

	    	//Don't want 9999999 returned as not correct.
	    	if(lowest === 9999999){
	    		lowest=0;
	    	}

	    	var result = [lowest,highest];

	    	return result;
	}

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