<html>
<head>
  <link rel="stylesheet" href="css/bootstrap.1.4.0.css"/>

  <script type="text/javascript" src="js/jquery-1.7.min.js"></script>
  <script type="text/javascript" src="js/jquery.tmpl.js"></script>
  <script type="text/javascript" src="js/knockout-1.3.0beta.debug.js"></script>
  <script type="text/javascript" src="js/knockout.simpleGrid.js"></script>
  <script type="text/javascript" src="js/redback/user.js"></script>

</head>

<body style="padding-top: 40px;">

<div id="menu"></div>

<div class="container">

<h2>Users list</h2>

<table class="zebra-striped" data-bind='simpleGrid: gridViewModel' id="usersTable"></table>

<button data-bind='click: addUser'>
    Add User
</button>

<button data-bind='click: sortByName'>
    Sort by name
</button>

<button data-bind='click: function() { gridViewModel.currentPageIndex(0) }'>
    Jump to first page
</button>


<h2>User creation</h2>


</div>
</body>

<script type="text/javascript">
$(function() {
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


});
</script>

</html>
