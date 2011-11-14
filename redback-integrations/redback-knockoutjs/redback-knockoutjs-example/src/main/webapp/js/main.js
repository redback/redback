require(["jquery", "jquery.tmpl", "knockout-1.3.0beta.debug", "knockout.simpleGrid", "redback/user", "head.0.96",
        "bootstrap-modal","jquery.json-2.3.min","jquery.validate","jquery.i18n.properties-1.0.9"],
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

    loadAndParseFile("/restServices/redbackServices/utilServices/getBundleResources", {cache:false, mode: 'map'});

    jQuery.i18n.properties({
        name:[],
        path:'bundle/',
        mode:'map',
        //language:'pt_PT',
        callback: function() {
            // We specified mode: 'both' so translated values will be
            // available as JS vars/functions and as a map

            // Accessing a simple value through the map
            jQuery.i18n.prop('msg_hello');
            // Accessing a value with placeholders through the map
            jQuery.i18n.prop('msg_complex', 'John');

            // Accessing a simple value through a JS variable
            alert(msg_hello +' '+ msg_world);
            // Accessing a value with placeholders through a JS function
            alert(msg_complex('John'));
        }
    });



});

// "jQuery.validity","setup-validity"