require(["order!jquery","order!jquery.i18n.properties-1.0.9","order!knockout.simpleGrid" ],
function($) {

  usersViewModel=function() {
    this.users = ko.observableArray([]);
    var self = this;

    this.loadUsers = function() {
      $.ajax("/restServices/redbackServices/userService/getUsers", {
          type: "GET",
          dataType: 'json',
          success: function(data) {
              var mappedUsers = $.map(data.user, function(item) {
                  return mapUser(item);
              });
              self.users(mappedUsers);
          }
        }
      );
    };
    this.gridViewModel = new ko.simpleGrid.viewModel({
      data: this.users,
      viewModel: this,
      pageLinksId: "usersPagination",
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
      pageSize: 5
    });

    this.addUser=function() {
      ko.renderTemplate("redback/user-create-tmpl", new user(), null, jQuery("#createUserForm"), "replaceNode");
      $('#user-create').show();
      $("#user-create").delegate("#user-create-form-cancel-button", "click keydown", function(e) {
        e.preventDefault();
        $('#user-create').hide();
      });
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
    };

    this.sortByName = function() {
      this.users.sort(function(a, b) {
          return a.username < b.username ? -1 : 1;
      });
    };

  }

  displayUsersGrid=function() {
    jQuery("#main-content").attr("data-bind","");
    jQuery("#main-content").html($("#usersGrid").html());
    if (window.redbackModel.usersViewModel == null ) {
      window.redbackModel.usersViewModel = new usersViewModel();
    }
    window.redbackModel.usersViewModel.loadUsers();
    ko.applyBindings(window.redbackModel.usersViewModel,jQuery("#main-content").get(0));
  }

  userViewModel=function(user) {
      this.user=user;
  }

  this.editUserBox=function(user) {
    jQuery("#main-content").append("<div id='user-edit'></div>");
    jQuery("#main-content #user-edit").attr("data-bind",'template: {name:"redback/user-create-tmpl",data: user}');
    var viewModel = new userViewModel(user);

    ko.applyBindings(viewModel,jQuery("#main-content #user-edit").get(0));
    jQuery("#main-content #user-create").validate({
      rules: {
        confirmPassword: {
          equalTo: "#password"
        }
      },
      showErrors: function(validator, errorMap, errorList) {
        customShowError(validator,errorMap,errorMap);
      }
    });
    jQuery("#main-content #user-create").delegate("#user-create-form-cancel-button", "click keydown", function(e) {
      e.preventDefault();
      jQuery('#main-content #user-create').hide();
    });
    jQuery("#main-content #user-create").validate({
      rules: {
        confirmPassword: {
          equalTo: "#password"
        }
      },
      showErrors: function(validator, errorMap, errorList) {
        customShowError(validator,errorMap,errorMap);
      }
    });
    jQuery("#main-content #user-create").delegate("#user-create-form-register-button", "click keydown", function(e) {
      e.preventDefault();
      var valid = $("#user-create").valid();
      if (!valid) {
          return;
      }
    });
  }

  deleteUser=function(user) {
    $.ajax("/restServices/redbackServices/userService/deleteUser/"+user.username(), {
        type: "GET",
        dataType: 'json',
        success: function(data) {
            // FIXME i18n and use a messages div
          window.redbackModel.usersViewModel.users.remove(user);
          alert("user " + user.username() + " deleted");
        }
      }
    );
  }



});


