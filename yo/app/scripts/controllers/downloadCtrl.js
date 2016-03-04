(function(){

	'use strict';

	angular.module('dashboardApp').controller('DownloadCtrl', DownloadCtrl);

	DownloadCtrl.$inject= ['downloadService','$scope','googleChartApiPromise','$q','$filter','$uibModal'];	

	function DownloadCtrl(downloadService, $scope, googleChartApiPromise, $q,$filter, $uibModal){	

		var vm=this;

		var globalIdentifiers = ['Country', 'number of Downloads'];
		
		vm.format =  'yyyy-MM-dd';

		vm.endDate = new Date();
		
		vm.startDate = new Date(new Date().setDate(new Date().getDate()-10));		

		vm.isStartDateOpen = false;
        vm.isEndDateOpen = false;

		vm.openStartDate = function(){
            this.isStartDateOpen = true;
            this.isEndDateOpen = false;
        };

        vm.openEndDate = function(){
            this.isStartDateOpen = false;
            this.isEndDateOpen = true;
        };	

        vm.europeOptions = [
        	{area:"Northern Europe", geoCode:"154"},
			{area:"Western Europe", geoCode:"155"},
		    {area:"Southern Europe", geoCode:"039"}
        ];
        vm.africaOptions = [
        	{area:"All of Africa", geoCode:"002"},
			{area:"Central Africa", geoCode:"017"},
			{area:"Northern Africa", geoCode:"015"}, 
			{area:"Southern Africa", geoCode:"018"}
        ];
        vm.americasOptions = [
        	{area:"South America", geoCode:"005"},
			{area:"Central America", geoCode:"013"},
			{area:"North America", geoCode:"021"},
			{area:"USA", geoCode:"US"}
        ];
        vm.asiaOptions = [
        	{area:"Eastern Asia", geoCode:"030"},
			{area:"Southern Asia", geoCode:"034"},
			{area:"Asia/Pacific region", geoCode:"035"},
			{area:"Oceania", geoCode:"009"},
			{area:"Middle East", geoCode:"145"},
			{area:"Central Asia", geoCode:"143"}, 
			{area:"Northern Asia", geoCode:"151"}
        ];

        vm.gridOptions = {};
        vm.gridOptions.columnDefs = [
        	{field: 'id', displayName: 'ID', width:80, cellTemplate:'<button class="btn primary" ng-click="grid.appScope.loadPopUp(row.entity.id)">{{row.entity.id}}</button>' },
        	{field: 'fullName', displayName: 'Full Name'},
        	{field: 'name', displayName: 'Name'},
        	{field: 'bandiwdth', type:"number",displayName: 'Bandwidth',width:160,cellTemplate:'<div class="ui-grid-cell-contents">{{row.entity.bandwidth|bytes}}</div>'},
        	{field: 'size', type:"number", width:100,displayName: 'Size',cellTemplate:'<div class="ui-grid-cell-contents">{{row.entity.size|bytes}}</div>'},
        	{field: 'method',  displayName: 'Method',width:80},
        	{field: 'start',  displayName: 'Start',cellFilter:'date:"medium"'},
        	{field: 'end', displayName:'End',cellFilter:'date:"medium"'},
    		{field: 'status', displayName:'Status',width:80},	
    		
    	];



    	//Unfortunately have to use $scope to allow the isolate scope access with Angular Grid UI.
    	$scope.loadPopUp = function(downloadId){
       		

       		var modalInstance = $uibModal.open({
       			templateUrl :'views/downloadModal.html',
       			controller:'DownloadModalCtrl',
       			resolve :{
       				data :function(){
       					return downloadService.getDownloadEntities(downloadId);
       				}
       			}

       		});
       	}

    	vm.gridOptions.enableFiltering = true;  	

        var downloadMethodTypes = downloadService.getDownloadMethodTypes();

        	downloadMethodTypes.then(function(responseData){
        		
        		responseData.push({method:"All"});
        		vm.downloadMethodTypes = responseData;

        });


        

		vm.updatePage = function(){	

			//Have to set the time to midnight otherwise will use current time.
     		var startDate = moment(vm.startDate);			

			startDate.set('hour','00');
			startDate.set('minute','00');
			startDate.set('second','00');
						
			var endDate = Date.parse(vm.endDate);
			var userName = vm.userName;
			var method = vm.selectedMethod === "All"?"":vm.selectedMethod;				

			//Create the promises for the data.
			var methodNumberPromise = downloadService.getDownloadMethodNumber(startDate,endDate, userName);			
			var methodVolumePromise = downloadService.getDownloadMethodVolume(startDate,endDate, userName);

			//Combine the promises for multiple datasets per graph
			var downloadMethod = $q.all([methodNumberPromise,methodVolumePromise]);

			downloadMethod.then(function(responseData){
				//Extract the data from the response.
				var number = _.map(responseData[0], function(data){
						return [data.method, data.number];
				});

				var volumeMethods = _.map(responseData[1], function(data){
						return data.method;
				});

				var volumeRaw = _.map(responseData[1], function(data){
						return [data.volume];
				});

				//Conver the data to human readable format.
				var formattedVolume = $filter('bytes')(volumeRaw);
				var formattedVolumeData = formattedVolume[0];

				var byteFormat = formattedVolume[1]; 

				//Combine the data into one array for the c3.js donut.
				var volume =  formattedVolumeData.map(function(value,index){
				    return [volumeMethods[index], formattedVolumeData[index]];
				});
				
				vm.method = {
					datasets:["number","volume"],
					number : {
						"data":number,
						"title":"Number of downloads"
					},
					volume : {
						"data":volume,
						"title":"Volume ("+byteFormat+")"
					},					
					description :  "This donut chart displays the number and volume of downloads by download mechanism.",
				    title :"Download Methods"
				};

			});			
			
			downloadService.getDownloadStatusNumber(startDate, endDate, userName,method).then(function(responseData){
				console.log(responseData);

			});			

			downloadService.getDownloadEntityAge(startDate,endDate,userName,method).then(function(responseData){

				var age  = _.map(responseData, function(responseData){
					return responseData.age;
				});

				var number = _.map(responseData, function(responseData){
					return responseData.number;
				});

				age.unshift("Days old");		
							
				number.unshift('Number of Files');				
				
				vm.entityAge ={
					data:[age,number],
					description : "This scatter graph displays the number of datafiles that have been downloaded grouped by their age. The age of the datafile is calculated from its creation date subtracted by the date of the download. The age is displayed in days.",
				    title : "Download File Age",
				    zoom : true
				} 				
				
			});

			downloadService.getLocalDownloadLocation(startDate,endDate, userName, method).then(function(responseData){
				
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
				    	region:'155',				    	
				    	colorAxis: {colors: ['grey', '#e31b23']},
				    	legend:'none'
				    };	
				   
				    vm.changeRegion = function(){
				    				    	
				    	localChart.options.region = vm.selectedRegion.geoCode;
				    }
				    vm.localChart = localChart;
					
				});
				
			});

			downloadService.getGlobalDownloadLocation(startDate,endDate, userName, method).then(function(responseData){ 			   				  		
				
				googleChartApiPromise.then(function(){
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

				    vm.worldChart = worldChart;
				});
			    
			});

			downloadService.getDownloads(startDate,endDate, userName, method).then(function(responseData){							
					
				vm.gridOptions.data = responseData;				
				
			});			

		    downloadService.getDownloadFrequency(startDate,endDate, userName, method).then(function(responseData){

		    	var data = responseData;		    	

		    	var dates  = _.map(data, function(data){
					return data.date;
				});

				var numbers = _.map(data, function(data){
					return data.number;
				});
				
				var data = [dates,numbers];

			    //Calculate metrics: Total amount of downloads and the busiest Day.
			 		    		
	    		var total = 0;	    		
	    		
	    		for(var i=1;i<numbers.length;i++){	    			
	    			total +=numbers[i];
	    		}   		
	    		

	    		var busiestIndex = 0;
	    		var largestDay = 0;	    		
	    		
	    		for(var i=1;i<data[1].length;i++){	    			
	    			if(data[1][i] > largestDay){
	    				busiestIndex = i;
	    				largestDay = data[1][i];
	    			}
	    		}   		

	    		dates.unshift("x");		
							
				numbers.unshift('Number of Downloads');	    		
					
			    vm.count ={
			    	data:data,
			    	description : "This line graph displays the number of downloads that occured on the requested days.",
					title:"Download Count",
					zoom:true,
					total: total=== 0 ? "No Downloads":total,
					busiestDay:largestDay===0 ? "No Downloads":data[0][busiestIndex] +" with "+largestDay
			    } 

					
		    });

		    downloadService.getDownloadBandwidth(startDate,endDate, userName, method).then(function(responseData){
		    	
		    	vm.rawBandwidthData = responseData;

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

		    	var highest = Math.max.apply(Math,responseData.map(function(data){return data.bandwidth;}));
		    	var lowest =  Math.min.apply(Math,responseData.map(function(data){return data.bandwidth;}));
		    	
		    	vm.bandwidth ={
		    		data:bandwidthData,
		    		highest:highest === "-InfinityMB/S" ? "No Downloads": highest,
		    		lowest:lowest === "InfinityMB/S" ? "No Downloads": lowest,
		    		zoom :true,
				    description : "This scatter graph displays the bandwidth of downloads in Megabytes. This is calculated by the download amount (Megabytes) over the time it took to complete.",
				    title : "Download Bandwidth"
		    	}		    	

		    });

		    downloadService.getDownloadISPBandwidth(startDate,endDate, userName, method).then(function(responseData){		     		
		     		
		     		var arrayData = _.map(responseData, function(data){
		     			
						return [[data.average],[data.min],[data.max]];
					});

					var ispArray = _.map(responseData, function(data){
		     			
						return data.isp;
					});


					//Get the largest value in the result to set the correct bytes format.
					var largestValue = Math.max.apply(Math,arrayData[0][2]);
					
					var formattedData = arrayData[0]; 

					for(var i =0;i<formattedData.length;i++){
						formattedData[i] = $filter('bytes')(formattedData[i],largestValue);
					}				
					
					if (typeof formattedData !== "undefined") {
						formattedData[0].unshift('average');
						formattedData[1].unshift('min');
						formattedData[2].unshift('max');
					}else{
						formattedData=[['average',0],['min',0],['max',0]];
					}
					

					vm.ispBandwidth = {
						data:formattedData,
						ispList:ispArray,
						zoom :false,
				    	description : "This bar graph displays the bandwidth of downloads per ISP during the requested period.",
				    	title : "ISP Download Bandwidth MB/S"
					};					
		     });
			
	    	downloadService.getDownloadVolume(startDate,endDate, userName, method).then(function(responseData){


	    		var data = responseData;

	    		var dates  = _.map(data, function(data){
					return data.date;
				});

				var byteArray = _.map(data, function(data){
					
					return Math.round(data.number);
				});

				//Need to format the bytes into human readable format
				var formattedData = $filter('bytes')(byteArray);

			   	//Adding on to the beginning of the array to allow c3 to read the data
				dates.unshift("x");		
							
				var dataForGraph = formattedData[0];
				dataForGraph.unshift(formattedData[1]);

				var total = 0;	    		
	    		
	    		for(var i=1;i<dataForGraph.length;i++){	
	    		    var temp = dataForGraph[i];
	    		    if(temp!=="null"){
	    		    	total +=temp;
	    		    }    			
	    			
	    		}	    		

	    		var busiestIndex = 0;
	    		var largestDay = 0;	    		
	    		
	    		for(var i=1;i<dataForGraph.length;i++){	    			
	    			if(dataForGraph[i] > largestDay){
	    				busiestIndex = i;
	    				largestDay = dataForGraph[i];
	    			}
	    		} 
				 
				var byteFormat = formattedData[1];

	    		vm.volume = { 	
	    			data:[dates,dataForGraph],
	    			byteFormat:byteFormat,
	    			total:total === 0 ? "No Downloads": total+' '+byteFormat,
	    			busiestDay:largestDay ===0 ?"No Downloads":"Busiest Day "+ dates[busiestIndex]+" with "+largestDay+" "+byteFormat,
	    			zoom :false,
				    description : "This bar graph displays the volume of data that was downloaded during the requested period.",
				    title : "Download Volume"

	    		};
	    	});			

		};

		vm.globalLocationDescription = "This map displays the number of downloads that have occured within each country during the requested period.";
		vm.localLocationDescription = "This map displays the number of downloads that have occured within the selected area. The circles center position is based on the longitude and latitude of the download.";
			
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


})();