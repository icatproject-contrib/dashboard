(function (){

	angular.module('dashboardApp')
		   .controller('IndexController', IndexController);

	IndexController.$inject = ['$sessionStorage','$rootScope','$location',];

	function IndexController($sessionStorage,$rootScope,$location){

		
	
		if(!getLoginStatus()){				
			$location.path('/login')
		}
		
		
		var ic = this;

		ic.isLoggedIn = function(){
			
			return getLoginStatus();
		}

		$rootScope.baseURL = '/dashboard/rest/';

		$rootScope.graphColours = ["#4DFA90","#FABE4D","#FF5468"];

	

		$rootScope.logOperations = [ 
		{ value: 'search', label: 'Search' }, 
		{ value: 'login', label: 'Login' }, 
		{ value: 'logout', label: 'Logout'}, 
		{ value: 'get', label: 'Get' }, 
		{ value: 'refresh', label: 'Refresh' },
		{ value: 'createMany', label: 'Create Many' },
		{ value: 'deleteMany', label: 'Delete Many' },
		{ value: 'update', label: 'Update' },
		{ value: 'delete', label: 'Refresh' },
		{ value: 'searchText', label: 'Search Text' },
		{ value: 'create', label: 'Create' } ]


		$rootScope.entityOptions = [
			{value:"Application" ,label:"Application"}, 
			{value: "DataCollection" ,label:"DataCollection"}, 
			{value:"DataCollectionDatafile",label:"DataCollectionDatafile"}, 
			{value:"DataCollectionDataset",label:"DataCollectionDataset"}, 
			{value:"DataCollectionParameter", label:"DataCollectionParameter"}, 
			{value: "Datafile", label:"Datafile"},
			{value:"DatafileFormat", label:"DatafileFormat"},
			{value:"DatafileParameter",label: "DatafileParameter"}, 
			{value:"Dataset",label:"Dataset"}, 
			{value:"DatasetParameter", label:"DatasetParameter"}, 
			{value:"DatasetType",label:"DatasetType"}, 
			{value:"Facility",label:"Facility"}, 
			{value:"FacilityCycle",label:"FacilityCycle"}, 
			{value:"Grouping", label:"Grouping"}, 
			{value:"Instrument", label:"Instrument"},
			{value:"InstrumentScientist", label:"InstrumentScientist"}, 
			{value:"Investigation", label:"Investigation"}, 
			{value:"InvestigationGroup",label:"InvestigationGroup"}, 
			{value:"InvestigationInstrument", label:"InvestigationInstrument"}, 
			{value:"InvestigationParameter", label: "InvestigationParameter"}, 
			{value:"InvestigationType", label:"InvestigationType"}, 
			{value:"InvestigationUser",label:"InvestigationUser"}, 
			{value:"Job",label:"Job"}, 
			{value:"Keyword",label:"Keyword"}, 
			{value:"ParameterType",label:"ParameterType"}, 
			{value:"PermissibleStringValue", label:"PermissibleStringValue"}, 
			{value:"PublicStep",label:"PublicStep"}, 
			{value:"Publication",label:"Publication"}, 
			{value:"RelatedDatafile",label:"RelatedDatafile"}, 
			{value:"Rule",label:"Rule"}, 
			{value:"Sample",label:"Sample"}, 
			{value:"SampleParameter", label:"SampleParameter"}, 
			{value:"SampleType",label:"SampleType"}, 
			{value:"Shift", label:"Shift"}, 
			{value:"Study",label:"Study"}, 
			{value:"StudyInvestigation", label:"StudyInvestigation"}, 
			{value:"User",label:"User"},
			{value:"UserGroup",label:"UserGroup"}

		]

		function getLoginStatus(){

			return !(_.isEmpty($sessionStorage.sessionData));
		}

		
	}		   


})();