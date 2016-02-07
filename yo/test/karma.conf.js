// Karma configuration
// http://karma-runner.github.io/0.12/config/configuration-file.html
// Generated on 2015-12-11 using
// generator-karma 1.0.1

module.exports = function(config) {
  'use strict';

  config.set({
    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // base path, that will be used to resolve files and exclude
    basePath: '../',

    // testing framework to use (jasmine/mocha/qunit/...)
    // as well as any additional frameworks (requirejs/chai/sinon/...)
    frameworks: [
      "jasmine"
    ],

    // list of files / patterns to load in the browser
    files: [
      // bower:js
      'bower_components/jquery/dist/jquery.js',
      'bower_components/angular/angular.js',
      'bower_components/bootstrap/dist/js/bootstrap.js',
      'bower_components/angular-animate/angular-animate.js',
      'bower_components/angular-cookies/angular-cookies.js',
      'bower_components/angular-resource/angular-resource.js',
      'bower_components/angular-route/angular-route.js',
      'bower_components/angular-sanitize/angular-sanitize.js',
      'bower_components/ngstorage/ngStorage.js',
      'bower_components/angular-ui-router/release/angular-ui-router.js',
      'bower_components/d3/d3.js',
      'bower_components/c3/c3.js',
      'bower_components/lodash/lodash.js',
      'bower_components/angular-inform/dist/angular-inform.js',
      'bower_components/angular-loading-bar/build/loading-bar.js',
      'bower_components/moment/moment.js',
      'bower_components/momentjs/moment.js',
      'bower_components/bootstrap-daterangepicker/daterangepicker.js',
      'bower_components/ng-bs-daterangepicker/src/ng-bs-daterangepicker.js',
      'bower_components/jasmine-core/lib/jasmine-core/jasmine.js',
      'bower_components/angular-mocks/angular-mocks.js',
      // endbower
      "bower_components/angular-ui-router/release/angular-ui-router.js",
      "app/scripts/**/*.js",
      "test/mock/**/*.js",
      "test/spec/**/*.js"
    ],

    // list of files / patterns to exclude
    exclude: [
    ],

    // web server port
    port: 9876,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: [
      "Chrome",
	  "Firefox",
	  "IE",
	  
    ],

    // Which plugins to enable
    plugins: [
      "karma-phantomjs-launcher",
      "karma-jasmine",
	  "karma-chrome-launcher",
	  "karma-ie-launcher",
	  "karma-firefox-launcher",
    ],

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false,

    colors: true,

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,

    // Uncomment the following lines if you are using grunt's server to run the tests
    // proxies: {
    //   '/': 'http://localhost:9000/'
    // },
    // URL root prevent conflicts with the site root
    // urlRoot: '_karma_'
  });
};
