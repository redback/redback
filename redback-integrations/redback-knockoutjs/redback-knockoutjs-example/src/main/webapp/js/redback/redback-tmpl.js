require(["text!redback/templates/user-create.html", "text!redback/templates/user-grids.html", "text!redback/templates/login.html"
          , "text!redback/templates/register-form.html"],
    function(usercreate, usergrids, login,register) {


      $.tmpl( login, $.i18n.map ).appendTo("#html-fragments");
      $.tmpl( register, $.i18n.map ).appendTo("#html-fragments");
      // template loading
      $("#html-fragments").append(usercreate);
      //$.tmpl( usercreate, $.i18n.map ).appendTo("#html-fragments");

      //$.tmpl( usergrids, $.i18n.map ).appendTo("#html-fragments");
      $("#html-fragments").append(usergrids);

    }
);