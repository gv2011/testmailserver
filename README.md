# Testmailserver

## Simple mail server for testing purposes using Subethasmtp

The purpose of this project is to provide a very simple SMTP mail server to test and debug
applications that send email.

The server just logs the mail that is submitted to individual files, it does not send
mails.

The implementation is a thin wrapper (basically a main method) around the [Subethasmtp](https://github.com/voodoodyne/subethasmtp) library.


## Usage:

`sudo java -jar target/testmailserver.jar [port]`

Where `port` is optional, its default is 25 (if using a port > 1000, sudo can be omitted).

Received messages are logged to logs/msg. Java 8 is required.

Stop the server with SIGINT (Ctrl-C).


## Customization:

[Logback](http://logback.qos.ch/) is used for logging. You can modify the way messages are
written by configuring logback differently.


## Build:

`mvn clean install`

The build checks the license headers and signs the maven artifacts using GPG. There is an integration test that is not
platform independant (basically it needs `java` beeing Java 8 and the `kill` command). Modify the POM accordingly, if you have problems with that.


## Deployment to Maven Central:

Not yet supported.


## Update license:

The license headers are checked during the build. If that fails,
they can be updated by: 

`mvn license:format`


## License

The MIT License (MIT)

Copyright (c) 2015 Zalando SE

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
    