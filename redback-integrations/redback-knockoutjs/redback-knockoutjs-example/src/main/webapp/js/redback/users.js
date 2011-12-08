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
require(["order!jquery","order!knockout.simpleGrid" ],
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
      screenChange();
      window.redbackModel.createUser=true;
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
      $("#createUserForm #user-create").delegate("#user-create-form-save-button", "click keydown", function(e) {
        e.preventDefault();
        var valid = $("#user-create").valid();
        if (!valid) {
            return;
        }
        user.createUser();
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
    window.redbackModel.createUser=false;
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
    $("#main-content #user-create").delegate("#user-create-form-cancel-button", "click keydown", function(e) {
      e.preventDefault();
      $('#main-content #user-create').remove();
    });
    $("#main-content #user-create").validate({
      rules: {
        confirmPassword: {
          equalTo: "#password"
        }
      },
      showErrors: function(validator, errorMap, errorList) {
        customShowError(validator,errorMap,errorMap);
      }
    });
    $("#main-content #user-create").delegate("#user-create-form-save-button", "click keydown", function(e) {
      e.preventDefault();
      var valid = $("#user-create").valid();
      if (!valid) {
          return;
      }
      user.update();
    });
  }





});


