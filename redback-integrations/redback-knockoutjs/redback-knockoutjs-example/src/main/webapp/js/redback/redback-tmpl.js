require(["text!redback/templates/user-create.html", "text!redback/templates/user-grids.html", "text!redback/templates/login.html"],
    function(usercreate, usergrids, login) {


      // template loading
      //$("#html-fragments").append(usercreate);
      $.tmpl( login, $.i18n.map ).appendTo("#html-fragments");
      $.tmpl( usercreate, $.i18n.map ).appendTo("#html-fragments");

      //$.tmpl( usergrids, $.i18n.map ).appendTo("#html-fragments");
      $("#html-fragments").append(usergrids);

    }
);