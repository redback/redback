require(["order!jquery","order!jquery.i18n.properties-1.0.9"],
function($) {

  registerBox=function(){
    if (window.modalRegisterWindow==null) {
      window.modalRegisterWindow = $("#modal-register").modal({backdrop:'static',show:false});
      window.modalRegisterWindow.bind('hidden', function () {
        $("#modal-register-err-message").hide();
      })
    }
    window.modalRegisterWindow.modal('show');
    $("#user-register-form").validate({
      showErrors: function(validator, errorMap, errorList) {
        customShowError(validator,errorMap,errorMap);
      }
    });
    $("#modal-register").delegate("#modal-register-ok", "click keydown keypress", function(e) {
      e.preventDefault();
      register();
    });
    $("#modal-register").focus();
  }

  register=function(){

  }

  // TODO move to a util.js
  $.urlParam = function(name){
      var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
      if (results) {
        return results[1] || 0;
      }
      return null;
  }


  // handle url with registration link
  $(document).ready(function() {
    var paramFoo = $.urlParam('foo');
    //alert(paramFoo);
  });

});