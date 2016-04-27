(function(){

	'use strict';

	angular.module('dashboardApp').controller('DownloadCtrl', DownloadCtrl);

	DownloadCtrl.$inject= ['downloadService','$scope','googleChartApiPromise','$q','$filter','$uibModal','uiGridService','$timeout','$rootScope'];	

	function DownloadCtrl(downloadService, $scope, googleChartApiPromise, $q,$filter, $uibModal, uiGridService,$rootScope){	

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
		    {area:"Southern Europe", geoCode:"039"},
		    {area:"Eastern Europe", geoCode:"151"}
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
        	{field: 'id', displayName: 'Entities', width:80, type:'button', cellTemplate:'<div class="button-holder" align=center><button class="btn btn-default btn-large text-center" ng-click="grid.appScope.loadPopUp(row.entity.id)"><span class="fa fa-archive" aria-hidden="true"></span></button></div>' },
        	{field: 'fullName', displayName: 'Full Name', type:'string'},
        	{field: 'name', displayName: 'Name', type:'string'},
        	{field: 'bandwidth', type:"bytes",displayName: 'Bandwidth',width:160,},
        	{field: 'downloadSize', type:"bytes", width:130,displayName: 'Size'},
        	{field: 'method',  displayName: 'Method',width:80},
        	{field: 'downloadStart',  displayName: 'Start', type:'date'},
        	{field: 'downloadEnd', displayName:'End',type:'date'},
    		{field: 'status', displayName:'Status',width:80, type:'string'},	
    		
    	];

    	function gridDataCall(queryConstraint,initialLimit,maxLimit,canceller){

    		return downloadService.getDownloads(queryConstraint,initialLimit,maxLimit,canceller);

  		}

    	vm.gridOptions = uiGridService.setupGrid(vm.gridOptions,$scope,"download", gridDataCall);  



    	//Unfortunately have to use $scope to allow the isolate scope access with Angular Grid UI.
    	$scope.loadPopUp = function(downloadId){
       		

       		var modalInstance = $uibModal.open({
       			templateUrl :'views/downloadModal.html',
       			controller:'DownloadModalCtrl',
       			controllerAs:'modalCtrl',
       			size:'lg',
       			resolve :{
       				
       				downloadId : function(){
       					return downloadId;
       				}

       			}

       		});
       	}
    	 	

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

			//Create the promises for the user download data.
			var userFrequencyPromise = downloadService.getUsersDownloadFrequency(startDate,endDate, method);
			var userVolumePromise = downloadService.getUsersDownloadVolume(startDate,endDate, method);

			//Combine the user promises together.
			var userDownloadData = $q.all([userVolumePromise,userFrequencyPromise]);

			userDownloadData.then(function(responseData){

				var volumeRaw = _.map(responseData[0], function(data){
						return data.volume;
				});

				var userName = _.map(responseData[1], function(data){
						return data.fullName;

				});

				var frequency = _.map(responseData[1], function(data){
						return [data.fullName,data.count];
				});

				var largestVolume = Math.max.apply(Math,volumeRaw);

				var filteredData = $filter('bytes')(volumeRaw,largestVolume);	
				
				var formattedVolume = filteredData[0];
				var byteFormat = filteredData[1]; 			

				//Combine the data into one array for the c3.js donut.
				var volume =  formattedVolume.map(function(value,index){
				    return [userName[index], formattedVolume[index]];
				});

				vm.users = {
					datasets:["number","volume"],
					number : {
						"data":frequency,
						"title":"Number of downloads per user"
					},
					volume : {
						"data":volume,
						"title":"Volume per user ("+byteFormat+")"
					},					
					description :  "This donut chart displays the number and volume of downloads per user.",
				    title :"User Downloads"
				};

			});	    

			//Create the promises for the download method data.
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
						return data.volume;
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
				

				var failedTemp;
				var inProgressTemp;
				var finishedTemp;

				//Gather the specific data e.g. amount of failed, inProgress etc...
				responseData.forEach(function(stat){
									

					if(stat.status==="failed"){
						failedTemp=stat.number;
					}
					else if(stat.status==="finished"){
						finishedTemp = stat.number;
					}
					else if(stat.status==="inProgress"){
						inProgressTemp = stat.number;
					}

				});

				var failed = failedTemp === undefined ? 0 : failedTemp;
				var inProgress  = inProgressTemp === undefined ? 0 : inProgressTemp;
				var finished = finishedTemp === undefined ? 0 : finishedTemp;			

				var successRate = ['Success percentage',$filter('number')((finished/(failed+finished))*100,1)];
				
				vm.statusNumber = {
					data:successRate,
					description: "This gauge chart displays the percentage of successful downloads to failed downloads.",
					title:"Download Success Rate",
					inProgress:inProgress,
					failed:failed,
					successful:finished
				}
			});			

			downloadService.getDownloadEntityAge(startDate,endDate,userName,method).then(function(responseData){

				var age  = _.map(responseData, function(responseData){
					return responseData.age;
				});

				var numbers = _.map(responseData, function(responseData){
					return responseData.number;
				});

				age.unshift("x");		
							
				numbers.unshift('Number');				
				
				vm.entityAge ={
					data:{
						x:"x",				 	 
			       	    columns : [age,numbers],
			       		types:{
			       			Number:'scatter',
			       		}
			       	},
					description : "This scatter graph displays the number of datafiles that have been downloaded grouped by their age. The age of the datafile is calculated from its creation date subtracted by the date of the download. The age is displayed in days.",
				    title : "Download File Age",
				    zoom : true,
				    xLabel:"Age of files",
				    yLabel:"Number of files"
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
				    	colorAxis: {colors: ["#2980B9","#4B77BE","#3498DB"]},
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
				    	colorAxis: {colors: ["#2980B9","#4B77BE","#3498DB"]}
				    };				   	

				    vm.worldChart = worldChart;
				});
			    
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
	    		var busiestIndex = 0;
	    		var largestDay = 0;	       		
	    		
	    		for(var i=0;i<numbers.length;i++){
	    			var current = numbers[i];	    			
	    			total +=current;

	    			if(current>largestDay){
	    				busiestIndex = i;
	    				largestDay = current;
	    			}
	    		}   		
	    		
	    		var busiestDay = largestDay===0 ? "No Data":dates[busiestIndex] +" with "+largestDay;
	    		var countTotal = total=== 0 ? "No Data":total;    				
	    				

	    		dates.unshift("x");		
							
				numbers.unshift('Number');	    		
					
			    vm.count ={
			    	data:{
						x:"x",				 	 
			       	    columns : [dates,numbers],
			       		types:{
			       			Number:'line',
			       		}
			       	},	
			    	description : "This line graph displays the number of downloads that occured on the requested days.",
					title:"Download Count",
					zoom:true,
					type:"line",
					xLabel:"Dates",
					yLabel:"Number of Downloads",
					total: countTotal,
					busiestDay: busiestDay
			    } 

			    console.log(vm.count)

					
		    });	

		    

		    downloadService.getDownloadISPBandwidth(startDate,endDate, userName, method).then(function(responseData){		     		
		     		
		     		var average = _.map(responseData, function(data){
		     			
						return data.average;
					});

					var min = _.map(responseData, function(data){
		     			
						return data.min;
					});

					var max = _.map(responseData, function(data){
		     			
						return data.max;
					});


					var ispArray = _.map(responseData, function(data){
		     			
						return data.isp;
					});
					

					var formattedData = [average,min,max];
					

					if (typeof formattedData !== "undefined") {
						//Get the largest value in the result to set the correct bytes format.
						var largestValue = Math.max.apply(Math,formattedData[2]);

						for(var i =0;i<formattedData.length;i++){
							formattedData[i] = $filter('bytes')(formattedData[i],largestValue)[0];
						}

						formattedData[0].unshift('average');
						formattedData[1].unshift('min');
						formattedData[2].unshift('max');

					}else{
						formattedData=[['average',0],['min',0],['max',0]];
					}			
					
					vm.ispBandwidth = {
						data: {							 	 				 	 
				       			 columns : formattedData,
				       			 type:'bar',
				       			 labels:true,
				       			 groups:[['average','min','max']]
						    },
						categories:ispArray,
						zoom :false,
				    	description : "This bar graph displays the bandwidth of downloads per ISP during the requested period.",
				    	title : "ISP Download Bandwidth MB/S",
				    	xLabel:"ISP",
				    	yLabel: "Bandwidth MB/S"
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

				dataForGraph.unshift("Volume");			

				var total = 0;	    		
	    		
	    		for(var i=0;i<byteArray.length;i++){	
	    		    var temp = byteArray[i];
	    		    if(temp!=="null"){
	    		    	total +=temp;
	    		    }    			
	    			
	    		}	    	
	    			    			

	    		var busiestIndex = 0;
	    		var largestDay = 0;	    		
	    		
	    		for(var i=1;i<dataForGraph.length;i++){	
	    		   			
	    			if(dataForGraph[i] > largestDay && dataForGraph[i]!=="null"){
	    				busiestIndex = i;
	    				largestDay = dataForGraph[i];
	    				
	    			}
	    		} 
				 
				var byteFormat = formattedData[1];
				
	    		vm.volume = { 	
	    			data:{
						x:"x",				 	 
			       	    columns : [dates,dataForGraph],
			       		types:{
			       			Volume:'bar',
			       		}
			       	},	    			
	    			byteFormat:byteFormat,
	    			total:total === 0 ? "No Data":$filter('bytes')(total),
	    			busiestDay:largestDay ===0 ?"No Data":"Busiest Day "+ dates[busiestIndex]+" with "+largestDay+" "+byteFormat,
	    			zoom :false,
				    description : "This bar graph displays the volume of data that was downloaded during the requested period.",
				    title : "Download Volume",
				    xLabel:"Dates",
					yLabel:"Volume of Downloads "+byteFormat,

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