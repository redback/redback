require(["order!jquery","order!jquery.i18n.properties-1.0.9","order!redback/redback"],
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
    var valid = $("#user-register-form").valid();
    if (!valid) {
        return;
    }
    clearUserMessages();
    $("#modal-register-ok").attr("disabled","disabled");

    $('#modal-register-footer').append(smallSpinnerImg());

    var user = {};
    user.username = $("#user-register-form-username").val();
    user.fullName = $("#user-register-form-fullname").val();
    user.email = $("#user-register-form-email").val();
    jQuery.ajax({
      url:  '/restServices/redbackServices/userService/registerUser',
      data:  '{"user":'+JSON.stringify(user)+'}',
      type: 'POST',
      contentType: "application/json",
      success: function(result){
        var registered = false;
        if (result == "-1") {
          registered = false;
        } else {
          registered = true;
        }

        if (registered == true) {
          window.modalRegisterWindow.modal('hide');
          $("#register-link").hide();
          // FIXME i18n
          displaySuccessMessage("registered your key has been sent");
        }
      },
      complete: function(){
        $("#modal-register-ok").removeAttr("disabled");
        $("#login-spinner").remove();
      },
      error: function(result) {
        var obj = jQuery.parseJSON(result.responseText);
        displayRedbackError(obj);
        window.modalRegisterWindow.modal('hide');
      }
    })

  }

  // TODO move to a util.js
  $.urlParam = function(name){
      var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
      if (results) {
        return results[1] || 0;
      }
      return null;
  }
  /**
   * validate a registration key and go to change password key
   * @param key
   */
  validateKey=function(key) {
    // spinner display
    $.ajax({
      url: '/restServices/redbackServices/userService/validateKey/'+key,
      type: 'GET',
       success: function(result){
         window.redbackModel.key=key;
         changePasswordBox(false);
       },
       complete: function(){
         // hide spinner
       },
       error: function(result) {
         var obj = jQuery.parseJSON(result.responseText);
         displayRedbackError(obj);
       }
    })
  }


  // handle url with registration link
  $(document).ready(function() {
    var validateMeId = $.urlParam('validateMe');
    if (validateMeId) {
      validateKey(validateMeId);
    }
  });

});