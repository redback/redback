require(["jquery", "jquery.tmpl", "knockout-1.3.0beta.debug", "knockout.simpleGrid", "redback/user", "head.0.96",
        "bootstrap-modal","jquery.json-2.3.min","jquery.validate"],
function($) {
    $.get('menu.html', function(data) {
      $('#menu').html(data);
    })
    .success(function() {
        $.ajax("/restServices/redbackServices/userService/isAdminUserExists", {
            type: "GET",
            dataType: 'json',
            success: function(data) {
              var adminExists = new Boolean(data);
              if (adminExists) {
                $("#create-admin-link").show();
              } else {
                alert("admin exists.");
              }
            }
          })
    });

    ko.applyBindings(new userViewModel());


});

// "jQuery.validity","setup-validity"