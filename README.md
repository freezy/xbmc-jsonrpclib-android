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

Work in progress. It compiles and the type structure should be fine now.

Usage
-----

Set up an empty Android Eclipse (library) project. Then open up `Introspect.java`
and update the path. Running it will create the entire library into the project. 
