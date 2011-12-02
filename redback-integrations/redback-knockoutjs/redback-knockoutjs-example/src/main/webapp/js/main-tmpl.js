require(["text!templates/menu.html","text!templates/topbar.html","text!templates/message.html"],
    function(menu,topbar,message) {

      // template loading
      $.tmpl( menu, $.i18n.map ).appendTo("#html-fragments");
      $.tmpl( topbar, $.i18n.map ).appendTo("#html-fragments");
      $("#html-fragments").append(message);

    }
);