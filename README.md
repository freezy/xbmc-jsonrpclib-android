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
fine now.

TODO is mainly multi-type related stuff which is tricky in Java, as well as
all the method parsing & rendering.

Usage
-----

Set up an empty Android Eclipse (library) project. Then open up `Introspect.java`
and update the path. Running it will create the entire library into the project. 
