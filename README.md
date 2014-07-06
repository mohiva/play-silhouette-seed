Silhouette Slick Seed Template
==============================

This is a fork of the official Silhouette Seed project. If you want to have a
first look at Silhouette, I suggest you have a look at the
[official project](https://github.com/mohiva/play-silhouette-seed).

The Silhouette Slick Seed project is an Activator template which shows how
[Silhouette](https://github.com/mohiva/play-silhouette) can be implemented in a
Play Framework application. It uses the
[play-slick](https://github.com/playframework/play-slick) library for database
access.

## Example

Currently, there is no live example of this template.

## Features

* Sign Up
* Sign In (Credentials)
* Social Auth (Facebook, Google+, Twitter)
* Dependency Injection with Guice
* Publishing Events
* Avatar service
* play-slick database access

## Documentation

### Slick

This template defaults to an in memory data storage via hash maps. To enable
database acces (via [Slick]()), you need to edit the play configuration.

To enable use of Slick DAOs, use this:

    silhouette.seed.db.useSlick=true

Additionally, you need to configure the database layer. Example for MySQL:

    db.default.driver=com.mysql.jdbc.Driver
    db.default.url="jdbc:mysql://localhost/play_silhouette_slick_seed"
    db.default.user=your_db_user
    db.default.password="your_db_password"

### Common

Have a look at the official
[Silhouette wiki](https://github.com/mohiva/play-silhouette/wiki) for more
information. If you need help with the integration of Silhouette into your
project, don't hesitate and ask questions in the
[mailing list](https://groups.google.com/forum/#!forum/play-silhouette) or on
[Stack Overflow](http://stackoverflow.com/questions/tagged/playframework).

## Activator

This project template is also
[hosted at typesafe](https://typesafe.com/activator/template/play-silhouette-seed-slick).

# License

The code is licensed under
[Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0).
