
XBMC JSON-RPC library for Android
=================================

This is a library that is automatically generated from JSON-RPC's
[introspect](http://wiki.xbmc.org/index.php?title=JSON-RPC_API/v3#JSONRPC.Introspect). It takes care of marshaling and
unmarshaling the JSON data into typed Java class objects and vice versa. It also wraps a Java object model around
JSON-RPC's method calls.

Additionally, all results are [parcelable](http://developer.android.com/reference/android/os/Parcelable.html) so they
can easily passed from one Android [activity](http://developer.android.com/reference/android/app/Activity.html) to
another.

The library also contains an implementation of a network stack that can be used if desired.

How it works
------------
XBMC's JSON-RPC API offers an introspect call that describes the entire API using a JSON schema. This schema is parsed
and coverted into classes that represent the API for a given version of XBMC. You can find the current schema
[here](src/main/json/introspect.json).

Building
--------
The generated files don't sit in the repo but get generated on every build. In Android Studio or Intellij IDEA you
should be able to simply import the Gradle project and it will generate the code at every build. You can also manually
build it by running ``gradlew build`` in the project folder.

Status
------
Haven't used it exensively yet but the API works fine in the current sandbox of the next official Android remote app.
Might change the network stack with something more mature such as [AndroidAsync](https://github.com/koush/AndroidAsync).

Example
-------
The generated Java library should be able to do everything the API provides, even the complex requests. Such an example
would be fetching all albums where the genre equals `Jazz` OR genre contains `Rock`:

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
final AudioLibrary.GetAlbums call = new AudioLibrary.GetAlbums(
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

