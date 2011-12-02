require(["text!redback/templates/user-create.html", "text!redback/templates/user-grids.html", "text!redback/templates/login.html"
          , "text!redback/templates/register-form.html","text!redback/templates/password-change-form.html"],
    function(usercreate, usergrids, login,register,passwordchange) {


      $.tmpl( login, $.i18n.map ).appendTo("#html-fragments");
      $.tmpl( register, $.i18n.map ).appendTo("#html-fragments");
      $.tmpl( passwordchange, $.i18n.map ).appendTo("#html-fragments");
      // template loading
      $("#html-fragments").append(usercreate);
      $("#html-fragments").append(usergrids);

    }
);