require(["order!jquery","order!jquery.i18n.properties-1.0.9"],
function($) {

  this.addUser = function() {
    ko.renderTemplate("redback/user-create-tmpl", new user(), null, jQuery("#createUserForm"), "replaceNode");
    $('#user-create').show();
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

  usersViewModel=function() {
    this.users = ko.observableArray([]);
    var self = this;
    $.ajax("/restServices/redbackServices/userService/getUsers", {
        type: "GET",
        dataType: 'json',
        success: function(data) {
            //alert(data);
            var mappedUsers = $.map(data.user, function(item) {
                return new user(item.username, item.password, null,item.fullName,item.email,item.permanent,item.isValidated,item.timestampAccountCreation,item.timestampLastLogin,item.timestampLastPasswordChange,item.isLocked,item.passwordChangeRequired,self);
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
      this.users.sort(function(a, b) {
          return a.username < b.username ? -1 : 1;
      });
    };
  }

  displayUsersGrid=function() {
    $("#main-content").attr("data-bind","");
    $("#main-content").html($("#usersGrid").html());

    ko.applyBindings(new usersViewModel());//,$("#"));
  }

});


