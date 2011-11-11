
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
        alert("remove");
        ownerViewModel.translations.destroy(this);
      }
  }

  function userViewModel() {
    this.users = ko.observableArray([]);
    var self = this;
    $.ajax("/restServices/redbackServices/userService/getUsers", {

        type: "GET",
        dataType: 'json',
        success: function(data) {
            var mappedUsers = $.map(data.translation, function(item) {
                return new user(username, password, confirmPassword,fullName,email,permanent,isValidated,timestampAccountCreation,timestampLastLogin,timestampLastPasswordChange,isLocked,passwordChangeRequired,self);
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


  function adminCreateBox() {
      alert("box");
  }