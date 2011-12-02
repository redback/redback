require(["text!templates/menu.html","text!templates/topbar.html"],
    function(menu,topbar) {

      // template loading
      $.tmpl( menu, $.i18n.map ).appendTo("#html-fragments");
      $.tmpl( topbar, $.i18n.map ).appendTo("#html-fragments");

    }
);