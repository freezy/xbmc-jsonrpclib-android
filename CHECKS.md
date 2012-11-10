
XBMC JSON-RPC Wilderness
========================

Methods
-------

* `Addons.ExecuteAddon`: Multitype, with additional properties
* `Player.Repeat`: Multitype, String + referenced enum arg
* `GUI.ShowNotification`: Multitype, String + defined enum arg
* `Player.Open`: A real bitch. Two args, first one 4-way multitype (`item`), 
  second inner object (`options`) which second member `resume` is a 3-way 
  multitype.
  
 Types
 -----
 
* `Video.Details.Episode` contains field `uniqueid` which is an object that 
  exclusively has `additionalProperties` defined.
* `Global.Toggle`: Multitype where one type is an enum.  
  