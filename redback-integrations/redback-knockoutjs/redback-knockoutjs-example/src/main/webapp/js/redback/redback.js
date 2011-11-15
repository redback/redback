require(["redback/user"],
function($) {

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
  ko.applyBindings(new userViewModel());
});