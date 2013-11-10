(function(){
    var fields = document.querySelectorAll(".timezone-field");
    var timezoneOffset = - (new Date()).getTimezoneOffset();
    for(var i=0;i<fields.length;i++){
        fields[i].value = timezoneOffset;
    }
})();
