require(["jquery","redback/user","jquery.i18n.properties-1.0.9"],
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

  // template loading
  $("<div>").load("js/redback/templates/user-create.tmpl", function() {
    $("#html-fragments").append($(this).html());
  });
  $("<div>").load("js/redback/templates/user-grids.tmpl", function() {
    $("#html-fragments").append($(this).html());
  });
  $("<div>").load("js/redback/templates/login.tmpl", function() {
    $("#html-fragments").append($(this).html());
  });
  // ko.applyBindings(new userViewModel());



});