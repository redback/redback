/*
 * Copyright 2011 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
$(function() {
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
        if (username == 'admin') {
          this.createAdmin();
        } else {
          this.createUser();
        }
      };
      this.createUser = function() {
        var currentUser = this;
        $.ajax("/restServices/redbackServices/userService/createUser", {
            data: "{\"user\": " +  ko.toJSON(this)+"}",
            contentType: 'application/json',
            type: "POST",
            dataType: 'json',
            success: function(result) {
              var created = JSON.parse(result);
              // FIXME use a message div and i18n
              if (created == true) {
                displaySuccessMessage("user created:"+currentUser.username());
                window.redbackModel.usersViewModel.users.push(currentUser);
                clearForm("#main-content #user-create");
                $("#main-content #user-create").hide();
                return this;
              } else {
                displayErrorMessage("user cannot created");
              }
            },
            error: function(result) {
              var obj = jQuery.parseJSON(result.responseText);
              displayRedbackError(obj);
            }
          });
      };

      this.createAdmin = function() {
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
              if (created == true) {
                displaySuccessMessage("admin user created");
                return this;
              } else {
                displayErrorMessage("admin user not created");
              }
            },
            error: function(result) {
              var obj = jQuery.parseJSON(result.responseText);
              displayRedbackError(obj);
            }
          });
      };

      this.deleteUser=function() {
        screenChange();
        // FIXME i18n
        var currentUser = this;
        openDialogConfirm(function(){
          $.ajax("/restServices/redbackServices/userService/deleteUser/"+currentUser.username(), {
                type: "GET",
                dataType: 'json',
                success: function(data) {
                    // FIXME i18n and use a messages div
                  window.redbackModel.usersViewModel.users.remove(currentUser);
                  displaySuccessMessage("user " + currentUser.username() + " deleted");
                },
                error: function(result) {
                 var obj = jQuery.parseJSON(result.responseText);
                 displayRedbackError(obj);
                },
                complete: function() {
                  closeDialogConfirm();
                }
              }
            );
          }
          ,"Ok", $.i18n.prop("cancel"), $.i18n.prop("user.delete.message") + ": " + currentUser.username());

      };

      this.update=function(){
        var currentUser = this;
        $.ajax("/restServices/redbackServices/userService/updateUser", {
            data: "{\"user\": " +  ko.toJSON(this)+"}",
            contentType: 'application/json',
            type: "POST",
            dataType: 'json',
            success: function(result) {
              var created = JSON.parse(result);
              // FIXME use a message div and i18n
              if (created == true) {
                displaySuccessMessage("user updated:"+currentUser.username());
                clearForm("#main-content #user-create");
                $("#main-content #user-create").hide();
                return this;
              } else {
                displayErrorMessage("user cannot be updated");
              }
            },
            error: function(result) {
              var obj = jQuery.parseJSON(result.responseText);
              displayRedbackError(obj);
            }
          });
      }

      this.save=function(){
        if (window.redbackModel.createUser==true){
          return this.createUser();
        } else {
          return this.update();
        }
      }

      this.i18n = $.i18n.prop;
  }


  adminUserViewModel=function() {
    this.user = new user("admin");
  }

  adminCreateBox=function() {
    jQuery("#main-content").attr("data-bind",'template: {name:"redback/user-edit-tmpl",data: user}');
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
    screenChange();
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
    $("#modal-login").delegate("#modal-login-ok", "click keydown keypress", function(e) {
      e.preventDefault();
      login();
    });
    $("#modal-login").focus();
  }

  login=function(){
    screenChange();
    var valid = $("#user-login-form").valid();
    if (!valid) {
        return;
    }
    $("#modal-login-ok").attr("disabled","disabled");

    //#modal-login-footer
    $('#modal-login-footer').append(smallSpinnerImg());

    var url = '/restServices/redbackServices/loginService/logIn?userName='+$("#user-login-form-username").val();
    url += "&password="+$("#user-login-form-password").val();

    $.ajax({
      url: url,
      success: function(result){
        var logged = false;
        if (result == null) {
          logged = false;
        } else {
          if (result.user) {
            logged = true;
          }
        }
        if (logged == true) {
          var user = mapUser(result.user);
          $.log("user.passwordChangeRequired:"+user.passwordChangeRequired());
          if (user.passwordChangeRequired()==true){
            changePasswordBox(true,false,user);
            return;
          }
          // not really needed as an exception is returned but "ceintures et bretelles" as we said in French :-)
          if (user.locked()==true){
            $.log("user locked");
            displayErrorMessage($.i18n.prop("accout.locked"));
            return
          }          
          // FIXME check validated
          reccordLoginCookie(user);
          $("#login-link").hide();
          $("#logout-link").show();
          $("#register-link").hide();
          $("#change-password-link").show();
          decorateMenuWithKarma(user);
          return;
        }
        $("#modal-login-err-message").html($.i18n.prop("incorrect.username.password"));
        $("#modal-login-err-message").show();
      },
      error: function(result) {
       var obj = jQuery.parseJSON(result.responseText);
       displayRedbackError(obj);
      },
      complete: function(){
        clearForm("#user-login-form");
        $("#modal-login-ok").removeAttr("disabled");
        $("#login-spinner").remove();
        window.modalLoginWindow.modal('hide');
      }
    });

  }

  /**
   *
   * @param previousPassword display and validate previous password text field
   * @param registration are we in registration mode ?
   */
  changePasswordBox=function(previousPassword,registration,user){
    screenChange();
    if (previousPassword==true){
      $("#password-change-form-current-password-div").show();
      $("#password-change-form-current-password").addClass("required");
    }else{
      $("#password-change-form-current-password-div").hide();
      $("#password-change-form-current-password").removeClass("required");
    }
    if (window.modalChangePasswordBox == null) {
      window.modalChangePasswordBox = $("#modal-password-change").modal({backdrop:'static',show:false});
      window.modalChangePasswordBox.bind('hidden', function () {
        $("#modal-password-change-err-message").hide();
      })
      $("#modal-password-change").delegate("#modal-change-password-ok", "click keydown keypress", function(e) {
        e.preventDefault();
        changePassword(previousPassword,registration,user);
      });
    }
    window.modalChangePasswordBox.modal('show');
    $("#password-change-form").validate({
      rules: {
        passwordChangeFormNewPasswordConfirm : {
          equalTo: "#passwordChangeFormNewPassword"
        }
      },
      showErrors: function(validator, errorMap, errorList) {
        customShowError(validator,errorMap,errorMap);
      }
    });


    $("#modal-password-change").focus();
  }

  editUserDetailsBox=function(){
    screenChange();
    $("#modal-user-edit-err-message").hide();
    $("#modal-user-edit-err-message").html("");
    if (window.modalEditUserBox == null) {
      window.modalEditUserBox = $("#modal-user-edit").modal({backdrop:'static',show:false});
      window.modalEditUserBox.bind('hidden', function () {
        $("#modal-user-edit-err-message").hide();
      })
      $("#modal-user-edit #modal-user-edit-ok").on( "click keydown keypress", function(e) {
        e.preventDefault();
        var valid = $("#user-edit-form").valid();
        if (!valid) {
            return;
        }
        var user = {
          username:currentUser.username,
          fullName:$("#modal-user-edit #fullname").val(),
          email:$("#modal-user-edit #email").val(),
          previousPassword:$("#modal-user-edit #userEditFormCurrentPassword").val(),
          password:$("#modal-user-edit #userEditFormNewPassword").val(),
          confirmPassword:$("#modal-user-edit #userEditFormNewPasswordConfirm").val()
        };
        editUserDetails(user);
      });
    }
    var currentUser = getUserFromLoginCookie();
    $("#modal-user-edit #username").html(currentUser.username);
    $("#modal-user-edit #fullname").val(currentUser.fullName);
    $("#modal-user-edit #email").val(currentUser.email);
    window.modalEditUserBox.modal('show');
    $("#user-edit-form").validate({
      rules: {
        userEditFormNewPasswordConfirm : {
          equalTo: "#userEditFormNewPassword"
        }
      },
      showErrors: function(validator, errorMap, errorList) {
        customShowError(validator,errorMap,errorMap);
      }
    });


    $("#modal-user-edit").focus();
  }

  editUserDetails=function(user){
    $("#modal-user-edit-err-message").html("");
    $.ajax("/restServices/redbackServices/userService/updateMe", {
        data: "{\"user\": " +  ko.toJSON(user)+"}",
        contentType: 'application/json',
        type: "POST",
        dataType: 'json',
        success: function(result) {
          var created = JSON.parse(result);
          // FIXME use a message div and i18n
          if (created == true) {
            displaySuccessMessage("details updated.");
            window.modalEditUserBox.modal('hide');
            reccordLoginCookie(user);
            clearForm("#user-edit-form");
            return this;
          } else {
            displayErrorMessage("details cannot be updated","modal-user-edit-err-message");
          }
        },
        error: function(result) {
          var obj = jQuery.parseJSON(result.responseText);
          $("#modal-user-edit-err-message").show();
          displayRedbackError(obj,"modal-user-edit-err-message");
        }
      });

  }


  /**
   *
   * @param previousPassword display and validate previous password text field
   * @param registration are we in registration mode ?
   */
  changePassword=function(previousPassword,registration,user){
    var valid = $("#password-change-form").valid();
    if (!valid) {
        return;
    }
    $('#modal-password-change-footer').append(smallSpinnerImg());

    if (registration==true) {
      var url = '/restServices/redbackServices/passwordService/changePasswordWithKey?';
      url += "password="+$("#passwordChangeFormNewPassword").val();
      url += "&passwordConfirmation="+$("#passwordChangeFormNewPasswordConfirm").val();
      url += "&key="+window.redbackModel.key;
    } else {
      var url = '/restServices/redbackServices/passwordService/changePassword?';
      url += "password="+$("#passwordChangeFormNewPassword").val();
      url += "&passwordConfirmation="+$("#passwordChangeFormNewPasswordConfirm").val();
      url += "&previousPassword="+$("#password-change-form-current-password").val();
      url += "&userName="+user.username();
    }

    $.ajax({
      url: url,
      success: function(result){
        var ok = JSON.parse(result);
        if (ok == true) {
          window.modalChangePasswordBox.modal('hide');
          displaySuccessMessage($.i18n.prop('change.password.success.section.title'));
        } else {
          displayErrorMessage("issue appended");
        }
        // menu etc....

      },
      complete: function(){
        $("#login-spinner").remove();
        window.modalChangePasswordBox.modal('hide');
      },
      error: function(result) {
       var obj = jQuery.parseJSON(result.responseText);
       displayRedbackError(obj);
      }
    });

    //$.urlParam('validateMe')
    // for success i18n key change.password.success.section.title
  }

  /**
   * @param data User response from redback rest api
   */
  mapUser=function(data) {
    return new user(data.username, data.password, null,data.fullName,data.email,data.permanent,data.validated,data.timestampAccountCreation,data.timestampLastLogin,data.timestampLastPasswordChange,data.locked,data.passwordChangeRequired,self);
  }


});


