
/**
 * @ngdoc overview
 * @name dashboardApp
 * @description
 * # dashboardApp
 *
 * Main module of the application.
 */
angular
.module('dashboardApp', [
    'ui.bootstrap',
    'ui.router',
    'ngStorage',
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'inform', 
    'angular-loading-bar',    
    'googlechart',
    'ui.grid',
    'ui.grid.infiniteScroll',
    'ui.grid.selection',   
    'ui.grid.resizeColumns',   
    'ui.grid.autoResize', 
    'bytes',
    

    

  ])
  .config(['$httpProvider', function($httpProvider){
    $httpProvider.interceptors.push('HttpInterceptor');
  }])
  .config(['cfpLoadingBarProvider', function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.includeSpinner = false;
  }])
  .config(function ($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.otherwise('/login');
    $stateProvider
      .state('login', {
        url: '/login',
        resolve :{
          authenticate :['Authenticate', function(Authenticate){
            return ! Authenticate.authenticate();
          }]
        },
       
        templateUrl:'views/login.html',
        controller: 'LoginCtrl as login'            

      })
      .state('logout',{
        url:'/logout',
        controller: 'LogoutController'
      })
      .state('users',{
        url:'/users',
        resolve :{
          authenticate :['Authenticate', function(Authenticate){
            return Authenticate.authenticate();
          }]
        },
        templateUrl:'views/users.html',
        controller: 'UserCtrl as user'  
      })
      .state('entities',{
        url:'/entities',
        resolve :{
          authenticate :['Authenticate', function(Authenticate){
            return Authenticate.authenticate();
          }]
        },
        templateUrl:'views/entities.html',
        controller: 'EntityCtrl as entity'  
      })
      .state('downloads',{
          url:'/downloads',          
          resolve :{
          authenticate :['Authenticate', function(Authenticate){
            return Authenticate.authenticate();
          }]
          },
          templateUrl: 'views/downloads.html',
          controller: 'DownloadCtrl as download'
      })
      
  });

