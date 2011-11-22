require(["order!jquery", "order!bootstrap-modal", "order!bootstrap-dropdown", "order!jquery.tmpl", "order!head.0.96",
        "order!jquery.json-2.3.min","order!jquery.validate","jquery.i18n.properties-1.0.9","order!redback/redback", "order!knockout-1.3.0beta.debug",
        "jquery.cookie.1.0.0","order!require.domReady.1.0.0","order!redback/redback-tmpl",
        "order!main-tmpl"],
function(domReady) {
  domReady(function () {

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

    /**
     * return a user see user.js if user logged otherwise null
     */
    userLogged=function() {
      // call /restServices/redbackServices/loginService/isLogged to know
      // if a session exists and check the cookie
      var userLogged = true;
      $.ajax("/restServices/redbackServices/loginService/isLogged", {
        type: "GET",
        async: false,
        success: function(data) {
          userLogged = JSON.parse(data);
        }
      });
      if (userLogged == false)
      {
        return null;
      }
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
      $("#login-link").show();
      $("#logout-link").hide();
    }

    $('#menu').html($("#main-menu"));

    $.ajax("/restServices/redbackServices/userService/isAdminUserExists", {
      type: "GET",
      dataType: 'json',
      success: function(data) {
        var adminExists = JSON.parse(data);
        if (adminExists == false) {
          $("#create-admin-link").show();
        }
      }
    });

    var user = userLogged();
    if (!user) {
      $("#login-link").show();
    } else {
      $("#logout-link").show();
    }





  });

});
