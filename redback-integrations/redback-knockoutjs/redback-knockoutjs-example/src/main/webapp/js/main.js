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

    // load default
    loadAndParseFile("/restServices/redbackServices/utilServices/getBundleResources", {cache:false, mode: 'map',encoding:'utf-8'});
    // load browser locale
    var browserLang = $.i18n.browserLang();
    loadAndParseFile("/restServices/redbackServices/utilServices/getBundleResources?locale="+browserLang, {cache:false, mode: 'map',encoding:'utf-8'});

    //alert($.i18n.prop("cannot.remove.user.role","foo","bar"));




});
