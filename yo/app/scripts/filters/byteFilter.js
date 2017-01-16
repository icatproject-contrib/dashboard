 (function() {
    'use strict';

    /**
     * This is a filter to prettyfy bytes. It takes two arguments. One is the value
     * to prettyfy and a potential largestValue which is used to find out what
     * format of bytes should be used e.g. KB, MB etc
     */
    angular.module('bytes', []).filter('bytes', ['$filter', function( $filter) {

            var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];


            return function() {               

                var args = arguments;

                var result;

                if(args.length==1){
                    var value = args[0];
                    
                    if(value===0){
                        return 0;
                    }
                    if(value instanceof Array){
                        result = byteArrayToSize(value);
                        
                    }
                    else{
                        var log = getLog(value);
                        
                        var formattedValue = convertBytes(value,log);

                        result = formattedValue+' '+sizes[log];                      
                    }               
                } 
                else{
                    var value = args[0];
                    var maxValue = args[1];

                    result = byteArrayToSizeWithValue(value, maxValue);
                }
                

                return result;                
            };

            //Gets the human readable format array value for sizes.
            function getLog(value){

                return parseInt(Math.floor(Math.log(value) / Math.log(1024)));

            }
            //Converts the bytes into human readable format. bytes is the value to change and 
            // log is the power to convert by.
            function convertBytes(bytes, log){
                    
                var convertedValue = (bytes / Math.pow(1024, log));
                
                var byteValue = convertedValue;

                convertedValue = convertedValue.toString();
                
                                
                // This is a very small number which has been expressed in scientific notation
                // (i.e. it uses e^-x) since we cut off the latter half of the string to get
                // rid of unneeeded decimal numbers, we would ignore the e^-x part unless we deal with it here.
                if(convertedValue.includes("e")) {
                    byteValue = toFixed(byteValue);
                    convertedValue = byteValue.toString();
                }
                
                //Only want to substring values that have decimal places.
                if(convertedValue.indexOf(".") > -1){
                    convertedValue = convertedValue.slice(0, (convertedValue.indexOf("."))+3);
                }

                
                return parseFloat(convertedValue);
            }
            
            // This function will take a number in scientific notation and convert it to standard 
            // notation. 
            function toFixed(x) {
                if (Math.abs(x) < 1.0) {
                    // Used for negative powers
                    var e = parseInt(x.toString().split('e-')[1]);
                    if (e) {
                        x *= Math.pow(10,e-1);
                        x = '0.' + (new Array(e)).join('0') + x.toString().substring(2);
                    }
                } else {
                    // Used for positive powers
                    var e = parseInt(x.toString().split('+')[1]);
                    if (e > 20) {
                        e -= 20;
                        x /= Math.pow(10,e);
                        x += (new Array(e+1)).join('0');
                    }
                }
                return x;
            }

            //Converts an array of bytes into the correct size with the provided max value.            
            function byteArrayToSizeWithValue(byteArray, maxValue){

                var formattedArray = [];

                //This then selects what type of byte it should be e.g. byte or KB. byteFormat refers to the position in the sizes array.
                var byteFormat = getLog(maxValue);
                
                //With the known type of byte we now need to format each one and add it to the array.
                for(var i=0;i<byteArray.length;i++){
                    var bytes = byteArray[i];
                                
                    if(parseInt(bytes) === 0){
                        //Null is placed so that c3 ignores those values
                        formattedArray.push("null");
                    }
                    else{
                        //Convert it to the correct byte value and then round to 2 decimal places.
                        formattedArray.push(convertBytes(bytes,byteFormat));
                    }
                }

                return [formattedArray,sizes[byteFormat]];
            }

            //Converts an array of bytes into the correct size.
            function byteArrayToSize(byteArray) {
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
                var byteFormat = getLog(max);


                //With the known type of byte we now need to format each one and add it to the array.
                for(var i=0;i<byteArray.length;i++){
                    var bytes = byteArray[i];
                                
                    if(parseInt(bytes) === 0){
                        //Null is placed so that c3 ignores those values
                        formattedArray.push("null");
                    }
                    else{
                        //Convert it to the correct byte value and then round to 2 decimal places.
                        formattedArray.push(convertBytes(bytes,byteFormat));
                    }
                }

                return [formattedArray,sizes[byteFormat]];
            }

        }]);
})();