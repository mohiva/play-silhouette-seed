Silhouette Seed Template
=====================================

The Silhouette Seed project is an example application which shows how [Silhouette](https://github.com/mohiva/play-silhouette) can be implemented in a Play Framework application. It's a starting point which can be extended to fit your needs.

## Example

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

(The "Build App" phase will take a few minutes)

Or you can find a running example of this template under the following URL: https://play-silhouette-seed.herokuapp.com/

## Features

* Sign Up
* Sign In (Credentials)
* Social Auth (Facebook, Google+, VK, Twitter, Xing, Yahoo)
* Dependency Injection with Guice
* Publishing Events
* Avatar service
* Remember me functionality
* Password reset/change functionality
* Account activation functionality
* Email sending and auth token cleanup
* [Security headers](https://www.playframework.com/documentation/latest/SecurityHeaders)
* [CSRF Protection](https://www.playframework.com/documentation/latest/ScalaCsrf)

## Documentation

Consult the [Silhouette documentation](http://silhouette.mohiva.com/docs) for more information. If you need help with the integration of Silhouette into your project, don't hesitate and ask questions in our [mailing list](https://groups.google.com/forum/#!forum/play-silhouette) or on [Stack Overflow](http://stackoverflow.com/questions/tagged/playframework).

## Social Authentication Providers

If you are testing social authentication, you'll want to set up a Heroku test application as it's publicly available and free for developers.  Play integration is [straightforward](https://devcenter.heroku.com/articles/play-support).

### Twitter

Create an application in [Twitter Developer Console](https://developer.twitter.com/en/apps).

The key and secret that you should use for authentication is under "Keys and Tokens" tab in the "Consumer API keys" section.

### Github

Create an application in [Github Developer Console](https://github.com/settings/applications/new).

### Google

You will need to [set up an application](https://developers.google.com/identity/protocols/OpenIDConnect#appsetup) in Google API Console and enable the People API, then [set up authentication](https://developers.google.com/identity/protocols/OpenIDConnect#authenticatingtheuser). 

There is a good guide at [oauth.com](https://www.oauth.com/oauth2-servers/signing-in-with-google/create-an-application/).

# License

The code is licensed under [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0).
