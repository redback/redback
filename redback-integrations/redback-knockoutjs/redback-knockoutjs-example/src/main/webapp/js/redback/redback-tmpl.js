require(["text!redback/templates/user-create.tmpl", "text!redback/templates/user-grids.tmpl", "text!redback/templates/login.tmpl"],
    function(usercreate, usergrids, login) {


      // template loading
      /*
      $("<div>").load("js/redback/templates/user-create.tmpl", function() {
        $("#html-fragments").append($(this).html());
      });
      $("<div>").load("js/redback/templates/user-grids.tmpl", function() {
        $("#html-fragments").append($(this).html());
      });
      $("<div>").load("js/redback/templates/login.tmpl", function() {
        $("#html-fragments").append($(this).html());
      });*/
      $("#html-fragments").append(usercreate);
      $("#html-fragments").append(usergrids);
      $("#html-fragments").append(login);

    }
);