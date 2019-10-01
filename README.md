# appengine-ktor-telegram

Simple webhook for Telegram, use Ktor (a web framework in Kotlin). Ready for GAE (Google App Engine)

# Google Appengine Standard

Sample project for [Ktor](http://ktor.io) running under [Google App Engine](https://cloud.google.com/appengine/)
standard infrastructure. 

## Prerequisites

* [Java SDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or later
* [Apache Maven](https://maven.apache.org)
* [Google Cloud SDK](https://cloud.google.com/sdk/docs/)

## Running

Run this project under local dev mode with:

```
gradle appengineRun
```

And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  

## Deploying

Use Google Cloud SDK to create application similarly to 
[Google App Engine for Java Quickstart](https://cloud.google.com/appengine/docs/standard/java/quickstart):

Install all the Google Cloud components and login into your account:

```
gcloud init
gcloud components install app-engine-java
gcloud components update  
gcloud auth application-default login
```

Create project and application:

```
gcloud projects create <unique-project-id> --set-as-default
gcloud app create
```                                

Then deploy your application with:

```
gradle appengineDeploy
```

You can checkout deployed version of this sample application at
https://cvdicv.appspot.com


## Converting to Google App Engine

You'll need to remove the `deployment` block from `application.conf`, otherwise when running on Google App Engine the `Servlet` will not get it's environment configured correctly causing the Google Cloud API's to fail.

# MIT License

Copyright (c) 2019 Omar Miatello
 
> Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
>  
> The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
> 
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.