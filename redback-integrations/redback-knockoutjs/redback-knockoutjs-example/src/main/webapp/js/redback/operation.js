require(["order!jquery","order!jquery.i18n.properties-1.0.9"],
function($) {


  operation=function(name) {
    this.name=name;
  }

  /**
   * @param data Operation response from redback rest api
   */
  mapOperation=function(data) {
    return new operation(data.name,null);
  }


});