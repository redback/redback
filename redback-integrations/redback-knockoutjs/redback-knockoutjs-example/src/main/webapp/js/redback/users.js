require(["order!jquery","order!jquery.i18n.properties-1.0.9"],
function($) {

  displayUsersGrid=function() {
    ko.applyBindings(new usersViewModel());
  }

  usersViewModel=function() {
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
      this.translations.push({ sourceLanguage: "", sourceText: "", targetLanguage: "" } );
    }
  }


});


