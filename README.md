Java Class Generator for XBMC's JSON-RPC
========================================

Since introspect gives all the information needed, this is an approach to
compile types and methods automatically into Java classes that can be used to
access the API.

Notes
-----

This is my second try, this time in Java. Used library is Jackson for the JSON
parsing.

The Eclipse project is of Android nature because of the templates that are
compiled and copied when rendering the classes. Run the project as normal
Java application, no Android is actually needed to execute.

Status
------

Work in progress. Generated code compiles and the type structure should be
fine. Also the methods render fine now and a first test with actually
using the generated lib with XBMC was successful.

TODO is mainly multi-type related stuff which is tricky in Java.

Usage
-----

Set up an empty Android Eclipse (library) project. Then open up `Introspect.java`
and update the path. Running it will create the entire library into the project. 

Library Example
---------------

So the generated Java library should be able to do everything the API provides.
A complicated example would be fetching all albums where the genre equals `Jazz`
OR genre contains `Rock`:

```java
// init connection manager
final ConnectionManager cm = new ConnectionManager(getApplicationContext(), new HostConfig("192.168.0.100"));

final List<AlbumFilter> filters = new ArrayList<AlbumFilter>();
// genre == "Jazz"
filters.add(new AlbumFilter(new AlbumFilterRule(
	OperatorFilters.IS, 
	new FilterRule.Value("Jazz"), 
	AlbumFilterRule.Field.GENRE)
));
// genre contains "Rock"
filters.add(new AlbumFilter(new AlbumFilterRule(
	OperatorFilters.CONTAINS, 
	new FilterRule.Value("Rock"), 
	AlbumFilterRule.Field.GENRE)
));

// create api call object
final AudioLibrary.GetAlbums call = new AudioLibrary.GetAlbums(null, null,
	new AlbumFilter(new AlbumFilter.Or(filters)),
	AudioModel.AlbumFields.TITLE, AudioModel.AlbumFields.YEAR);

// do the call 
cm.call(call, new ApiCallback<AudioModel.AlbumDetail>() {
	public void onResponse(AbstractCall<AudioModel.AlbumDetail> apiCall) {
		for (AlbumDetail album : apiCall.getResults()) {
			Log.d(TAG, "Got album: " + album.title + " (" + album.year + ")");
		}
	}
	public void onError(int code, String message, String hint) {
		Log.d(TAG, "Error " + code + ": " + message);
	}
}); 
```

For now, all parameters have to be provided when constructing the API call 
objects, but that can be extended later with constructors that only take
required parameters.

