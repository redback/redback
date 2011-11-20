require(["text!templates/menu.html"],
    function(menu) {

      // template loading
      $.tmpl( menu, $.i18n.map ).appendTo("#html-fragments");

    }
);