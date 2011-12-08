jQuery.log = (function(message) {
  if (typeof window.console != 'undefined' && typeof window.console.log != 'undefined') {
    console.log(message);
  } else {
    // do nothing no console
  }
});