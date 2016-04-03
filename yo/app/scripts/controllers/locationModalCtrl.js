angular.module('dashboardApp').controller('LocationModalCtrl', function($scope,$uibModalInstance , logId, userService, googleChartApiPromise){

  		var vm = this;

  		vm.logId = logId;

   		userService.getIcatLogLocation(logId).then(function(responseData){
        
        googleChartApiPromise.then(function(){
          var dataTable = new google.visualization.DataTable();

          dataTable.addColumn('number', 'Lat');                                
          dataTable.addColumn('number', 'Long');          
          dataTable.addColumn('string', 'City');          
          dataTable.addColumn('string', 'Country');
          dataTable.addColumn('string', 'isp');
          

          var dataArray = _.map(responseData, function(responseData){
            return [responseData.latitude, responseData.longitude,responseData.city, responseData.countryCode, responseData.isp];
          
          });

          dataTable.addRows(dataArray);

          var localChart = {};
            localChart.type = "GeoChart";
            localChart.data = dataTable;
            localChart.options ={                          
              colorAxis: {colors: ['grey', '#e31b23']},
              legend:'none'
            };  
           
            
            vm.localChart = localChart;
          
        });
        
      });

     



  vm.close = function (){
  	 $uibModalInstance.dismiss('close');
  }



  });