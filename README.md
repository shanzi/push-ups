# push-ups

A simple app to help create calendar events for your push-ups exercise plan according 
to [handredpushups.com](http://www.hundredpushups.com). It helps your generate exercise schedule after
inputing how many push-ups you can execude now and do not need register. It also generate a ics subscription url 
so that you can subscribe the plan with Google Calendar or other services.

A live version hosted at [hundred-push-ups.herokuapp.com](http://hundred-push-ups.herokuapp.com). Feel free to
use it to generate a throw-away push-ups exercise plan.

## About This Project and Clojure
I write this project for learning Clojure, a lisp dialet built on the base of JVM. Clojure is an interesting programing
language, you can know more about it [here](http://clojure.com/).

I'd like to write more about the learning progress, the modules I uses and what I think about this language here.

I use [korma](http://sqlkorma.com) as the ORM for relational database. It is quite elegance and easy to use. Thanks 
to Clojure/Lisp's powerful strengh to build DSL, you can write SQL-like expression in Clojure forms with korma. I did
meet a little problem when trying to deploy this project to Heroku, but finally solved it after update korma to the 
latest version and another module named [clj-bonecp-url](https://github.com/myfreeweb/clj-bonecp-url) which can help 
parse the database url given by heroku into structure accepted by korma.

The http server built on [ring](https://github.com/ring-clojure/ring). As far as I know, this is the most common used 
http server of clojure. It is quite low level, not difficult but is better used with some router modules. In my case
I chose [compojure](https://github.com/weavejester/compojure).

As I said above, Clojure is suite for DSL, so let's see the html templating system. I use
[hiccup](https://github.com/weavejester/hiccup) to generate html. It is a bit like `haml` but you native clojure 
structures. It does not provide so many powerful features compared with templating systems of rails or django,
but is enough for me to write a little project.

After finished this project, I think I can write something simple with clojure now, but clojure (as a lisp dialet) 
Is not so easy to master and understand at a high level. To fit your mind into its pattern may take some time. I think
I will still write my simple project with script language like python, But I won't give up Clojure as it has many 
good features and maybe the most promising lisp dialet.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License
see `LICENSE`
