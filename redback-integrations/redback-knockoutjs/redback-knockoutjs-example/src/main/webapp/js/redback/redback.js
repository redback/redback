require(["order!jquery","order!redback/redback-tmpl","order!redback/user","order!jquery.i18n.properties-1.0.9"],
function($) {


  // load default
  loadAndParseFile("/restServices/redbackServices/utilServices/getBundleResources", {cache:false, mode: 'map',encoding:'utf-8'});
  // load browser locale
  var browserLang = $.i18n.browserLang();
  loadAndParseFile("/restServices/redbackServices/utilServices/getBundleResources?locale="+browserLang, {cache:false, mode: 'map',encoding:'utf-8'});

  displayRedbackError=function(obj) {
    // {"redbackRestError":{"errorMessages":{"args":1,"errorKey":"user.password.violation.numeric"}}}
    if ($.isArray(obj.redbackRestError.errorMessages)) {
      for(var i=0; i<obj.redbackRestError.errorMessages.length; i++ ) {
        alert($.i18n.prop( obj.redbackRestError.errorMessages[i].errorKey, obj.redbackRestError.errorMessages[i].args ));
      }
    } else {
      alert($.i18n.prop( obj.redbackRestError.errorMessages.errorKey, obj.redbackRestError.errorMessages.args ));
    }
  }


  // ko.applyBindings(new userViewModel());



});