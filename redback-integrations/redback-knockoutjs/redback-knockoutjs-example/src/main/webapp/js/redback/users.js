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
      $("#main-content #user-edit").remove();
      $('#main-content #user-create').show();
      ko.renderTemplate("redback/user-edit-tmpl", new user(), null, $("#createUserForm").get(0),"replaceChildren");
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
    screenChange();
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
    screenChange();
    $("#main-content #user-edit").remove();
    $("#main-content").append("<div id='user-edit'></div>");
    $("#main-content #user-edit").attr("data-bind",'template: {name:"redback/user-edit-tmpl",data: user}');
    $("#main-content #user-create").remove();
    $("#main-content #user-edit").show();

    var viewModel = new userViewModel(user);

    ko.applyBindings(viewModel,$("#main-content #user-edit").get(0));
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





});


