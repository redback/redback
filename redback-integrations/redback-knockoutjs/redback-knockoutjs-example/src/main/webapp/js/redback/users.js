require(["order!jquery","order!jquery.i18n.properties-1.0.9","order!redback/knockout.usersGrid" ],
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
    this.gridViewModel = new ko.usersGrid.viewModel({
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
      }
    );

    this.sortByName = function() {
      this.users.sort(function(a, b) {
          return a.username < b.username ? -1 : 1;
      });
    };


  }



  displayUsersGrid=function() {
    $("#main-content").attr("data-bind","");
    $("#main-content").html($("#usersGrid").html());
    if (window.redbackModel.usersViewModel == null ) {
      window.redbackModel.usersViewModel = new usersViewModel();
    }
    window.redbackModel.usersViewModel.loadUsers();
    ko.applyBindings(window.redbackModel.usersViewModel);
  }

});


