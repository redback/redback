  function user(username, password, confirmPassword,fullName,email,permanent,validated,timestampAccountCreation,timestampLastLogin,timestampLastPasswordChange,locked,passwordChangeRequired,ownerViewModel) {
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
          //alert("valid:"+valid);
          if (!valid) {
              return;
          }

          $.ajax("/restServices/redbackServices/userService/createAdminUser", {
              data: "{\"user\": " +  ko.toJSON(this)+"}",
              contentType: 'application/json',
              type: "POST",
              dataType: 'json',
              success: function(result) {
                alert(ko.toJSON(result))
              },
              error: function(result) {
                alert(result.text);
              }
          });
      };
  }
  /*
  function validateUserCreateForm() {
      validitySetup();
      $("#user-create").validity(function() {
        $("fullname").require();
        $("#password").require();
        $("#confirmPassword").require();
        $("input:password").equal("Passwords do not match.");
        $("#email").require();
        $("#email").match("email");
      });
  }*/

  function userViewModel() {
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

  function adminUserViewModel() {
    this.user = new user("admin");

  }

  function adminCreateBox() {

      jQuery("#main-content").attr("data-bind",'template: {name:"user-create-tmpl",data: user}');
      var viewModel = new adminUserViewModel();
      ko.applyBindings(viewModel);
      //validateUserCreateForm();
      $("#user-create").validate({
        rules: {
          fullname: "required",
          password: "required",
          confirmPassword: {
            equalTo: "#password"
          },
          email: "email"
        },
        showErrors: function(validator, errorMap, errorList) {
          customShowError(validator,errorMap,errorMap);
        }
      });

      /*

      jQuery("#main-content").load("user-create.html", function() {
          jQuery.tmpl(jQuery("#user-create-tmpl"), null).appendTo( "#main-content");
          $("#username").val("admin");
          $("#username").attr("readonly") == true;
          $("#username").attr("disabled", true);
      */


  }

  function customShowError(validator, errorMap, errorList) {
      $( "div.clearfix" ).removeClass( "error" );
      $( "span.help-inline" ).remove();
      for ( var i = 0; errorList[i]; i++ ) {
        var error = errorList[i];
        var field = $("#"+error.element.id);
        field.parents( "div.clearfix" ).addClass( "error" );
        field.parent().append( "<span class=\"help-inline\">" + error.message + "</span>" )
      }
  }

