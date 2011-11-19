require(["order!jquery","order!redback/user","order!jquery.i18n.properties-1.0.9"],
function($) {
  user=function(username, password, confirmPassword,fullName,email,permanent,validated,timestampAccountCreation,timestampLastLogin,timestampLastPasswordChange,locked,passwordChangeRequired,ownerViewModel) {
      // Potentially Editable Field.
      this.username = ko.observable(username);
      // Editable Fields.
      this.password = ko.observable(password);
      this.confirmPassword = ko.observable(confirmPassword);
      this.fullName = ko.observable(fullName);
      this.email = ko.observable(email);
      this.permanent = ko.observable(permanent);
      this.validated = ko.observable(validated);
      // Display Only Fields.
      this.timestampAccountCreation = ko.observable(timestampAccountCreation);
      this.timestampLastLogin = ko.observable(timestampLastLogin);
      this.timestampLastPasswordChange = ko.observable(timestampLastPasswordChange);
      // admin only
      this.locked = ko.observable(locked);
      this.passwordChangeRequired = ko.observable(passwordChangeRequired);
      this.remove = function() {
        if (ownerViewModel) {
          ownerViewModel.users.destroy(this);
        }
      };
      this.create = function() {
          var valid = $("#user-create").valid();
          if (!valid) {
              return;
          }

          $.ajax("/restServices/redbackServices/userService/createAdminUser", {
              data: "{\"user\": " +  ko.toJSON(this)+"}",
              contentType: 'application/json',
              type: "POST",
              dataType: 'json',
              success: function(result) {
                var created = JSON.parse(result);
                // TODO use a message not an alert
                if (created == true) {
                  alert("admin user created");
                } else {
                  alert("admin user not created");
                }
              },
              error: function(result) {
                var obj = jQuery.parseJSON(result.responseText);
                displayRedbackError(obj);
              }
          });
      };
  }


  userViewModel=function() {
    this.users = ko.observableArray([]);
    var self = this;
    $.ajax("/restServices/redbackServices/userService/getUsers", {
        type: "GET",
        dataType: 'json',
        success: function(data) {
            var mappedUsers = $.map(data.translation, function(item) {
                return new user(username, password, null,fullName,email,permanent,isValidated,timestampAccountCreation,timestampLastLogin,timestampLastPasswordChange,isLocked,passwordChangeRequired,self);
            });
            self.users(mappedUsers);
        }
      }
    );

    this.gridViewModel = new ko.simpleGrid.viewModel({
      data: this.users,
      columns: [
        {
          headerText: "User Name",
          rowText: "username"},
        {
          headerText: "Full Name",
          rowText: "fullName"},
        {
          headerText: "Email",
          rowText: "email"}
      ],
      pageSize: 2

    });

    this.sortByName = function() {
      this.translations.sort(function(a, b) {
          return a.username < b.username ? -1 : 1;
      });
    };

    this.addUser = function() {
      ko.renderTemplate("translationsEdit", this, null, jQuery("#editTranslationDiv"), "replaceNode");
      //this.translations.push({ sourceLanguage: "", sourceText: "", targetLanguage: "" } );
    }
  }

  adminUserViewModel=function() {
    this.user = new user("admin");
  }

  adminCreateBox=function() {

      jQuery("#main-content").attr("data-bind",'template: {name:"user-create-tmpl",data: user}');
      var viewModel = new adminUserViewModel();
      ko.applyBindings(viewModel);
      $("#user-create").validate({
        rules: {
          confirmPassword: {
            equalTo: "#password"
          }
        },
        showErrors: function(validator, errorMap, errorList) {
          customShowError(validator,errorMap,errorMap);
        }
      });

  }
  loginBox=function(){
    window.console && console.debug( "loginBox");
    if (window.modalLoginWindow==null) {
      window.modalLoginWindow = $("#modal-login").modal({backdrop:'static',show:false});
      window.modalLoginWindow.bind('hidden', function () {
        $("#modal-login-err-message").hide();
      })
    }
    window.modalLoginWindow.modal('show');
    $("#user-login-form").validate({
      showErrors: function(validator, errorMap, errorList) {
        customShowError(validator,errorMap,errorMap);
      }
    });
    $("#modal-login").delegate("#modal-login-ok", "click keydown", function(e) {
      e.preventDefault();
      login();
    });
  }

  login=function(){
    var valid = $("#user-login-form").valid();
    if (!valid) {
        return;
    }
    $("#modal-login-ok").attr("disabled","disabled");

    //#modal-login-footer
    $('#modal-login-footer').append("<img id=\"login-spinner\" src='images/spinner.gif'/>")

    var url = '/restServices/redbackServices/loginService/logIn?userName='+$("#user-login-form-username").val();
    url += "&password="+$("#user-login-form-password").val();

    $.ajax({
      url: url,
      success: function(result){
        var logged = false;//JSON.parse(result);
        if (result == null) {
          logged = false;
        } else {
          if (result.user) {
            logged = true;
          }
        }
        if (logged == true) {
          var user = mapUser(result.user);
          reccordLoginCookie(user);
          // TODO check password change required locked etc....
          window.modalLoginWindow.modal('hide');
          $("#login-link").hide();
          $("#logout-link").show();
          return;
        }
        $("#modal-login-err-message").html($.i18n.prop("incorrect.username.password"));
        $("#modal-login-err-message").show();
      },
      complete: function(){
        $("#modal-login-ok").removeAttr("disabled");
        $("#login-spinner").remove();
      }
    });
    // removeAttr disabled

  }

  /**
   *
   * @param data User response from redback rest api
   */
  mapUser=function(data) {

    // {"user":{"email":"a@de.fr","fullName":"olamy","locked":false,"passwordChangeRequired":false,"permanent":false,"username":"admin","validated":false}}
    return new user(data.username, data.password, null,data.fullName,data.email,data.permanent,data.validated,data.timestampAccountCreation,data.timestampLastLogin,data.timestampLastPasswordChange,data.locked,data.passwordChangeRequired,self);
  }

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


