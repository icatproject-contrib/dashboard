angular.module('dashboardApp').controller('LocationModalCtrl', function($scope,$uibModalInstance , Id, data, entity, userService, googleChartApiPromise){

  		var vm = this;

  		vm.id = id;
      vm.entity=entity;

   		
        
        googleChartApiPromise.then(function(){
          var dataTable = new google.visualization.DataTable();

          dataTable.addColumn('number', 'Lat');                                
          dataTable.addColumn('number', 'Long');          
          dataTable.addColumn({type:'string', role: 'tooltip',
            'p': {'html': true}});          
          
        
          

          var dataArray = _.map(data, function(data){
            return [data.latitude, data.longitude, "City: "+data.city+"<br /> ISP:" +data.isp];
          
          });

          
          dataTable.addRows(dataArray);

          var localChart = {};
            localChart.type = "GeoChart";
            localChart.data = dataTable;
            localChart.options ={                          
              colorAxis: {colors: ['grey', '#e31b23']},
              legend:'none',
              tooltip: {isHtml:true}
            };  
           
            
            vm.localChart = localChart;
          
        });
        
    

     



  vm.close = function (){
  	 $uibModalInstance.dismiss('close');
  }



  });