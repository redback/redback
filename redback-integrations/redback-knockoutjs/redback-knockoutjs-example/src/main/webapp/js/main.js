require(["jquery", "bootstrap-modal", "jquery.tmpl", "knockout-1.3.0beta.debug", "head.0.96", "redback/redback",
        "jquery.json-2.3.min","jquery.validate","jquery.i18n.properties-1.0.9", "knockout.simpleGrid",
        "jquery.cookie.1.0.0"],
function($) {

  /**
   * return a user see user.js if user logged otherwise null
   */
  userLogged=function() {
    return jQuery.parseJSON($.cookie('redback_login'));
  }

  /**
   * reccord a cookie for session with the logged user
   * @param user see user.js
   */
  reccordLoginCookie=function(user) {
    $.cookie('redback_login', ko.toJSON(user));
  }

  deleteLoginCookie=function(){
    $.cookie('redback_login', null);
  }

  logout=function(){
    deleteLoginCookie();
  }

  $.get('menu.html', function(data) {
    $('#menu').html(data);
  })
  .success(function() {
      $.ajax("/restServices/redbackServices/userService/isAdminUserExists", {
          type: "GET",
          dataType: 'json',
          success: function(data) {
            var adminExists = JSON.parse(data);
            if (adminExists == false) {
              $("#create-admin-link").show();
            }
          }

        })
        var user = userLogged();
        if (!user) {
          $("#login-link").show();
        } else {
          $("#logout-link").show();
        }
  });

  // load default
  loadAndParseFile("/restServices/redbackServices/utilServices/getBundleResources", {cache:false, mode: 'map',encoding:'utf-8'});
  // load browser locale
  var browserLang = $.i18n.browserLang();
  loadAndParseFile("/restServices/redbackServices/utilServices/getBundleResources?locale="+browserLang, {cache:false, mode: 'map',encoding:'utf-8'});

  customShowError=function(validator, errorMap, errorList) {
    $( "div.clearfix" ).removeClass( "error" );
    $( "span.help-inline" ).remove();
    for ( var i = 0; errorList[i]; i++ ) {
      var error = errorList[i];
      var field = $("#"+error.element.id);
      field.parents( "div.clearfix" ).addClass( "error" );
      field.parent().append( "<span class=\"help-inline\">" + error.message + "</span>" )
    }
  }



});
