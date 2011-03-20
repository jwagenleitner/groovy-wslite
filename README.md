# groovy-wslite

Library for Groovy that aims to provide no-frills SOAP and REST webservice clients.

No magic is involved, this library assumes you know exactly what messages you want to send to your services and want full control over the request.  No streams are used and all request/responses are buffered in memory for convenience.

## SOAP

### Example

    import wslite.soap.*

    def soapClient = new SOAPClient(serviceURL: "http://www.webservicex.net/WeatherForecast.asmx")
    def response = soapClient.send(connectTimeout:5000, readTimeout:10000) {
        version SOAPVersion.V1_2
        body {
            GetWeatherByZipCode(xmlns:"http://www.webservicex.net") {
                ZipCode("93657")
            }
        }
    }

    assert "SANGER" == response.GetWeatherByZipCodeResponse.GetWeatherByZipCodeResult.PlaceName.text()
    assert 200 == resp.http.statusCode
    assert "OK" == resp.http.statusMessage
    assert "ASP.NET" == resp.http.headers["X-Powered-By"]

## REST

### Example

    import wslite.rest.*

    def client = new RESTClient("http://www.fresnostatenews.com/feed/")
    def response = client.get()

    assert 200 == response.statusCode
    assert "text/xml" == response.contentType
    assert "UTF-8" == response.charset
    assert "text/xml; charset=UTF-8" == response.headers."Content-Type"
    assert response instanceof XmlResponse
    assert "FresnoStateNews.com" == response.XML.channel.title.text()

### Methods

*RESTClient* supports the following methods:

* get
* delete
* post
* put

### Parameters

The methods can all take a map as a parameter (though not required) that give you control over the request.

    def client = new RESTClient("http://www.fresnostatenews.com/")
    def response = client.get( path: "/feed",
                               accept: ContentType.XML,
                               query:[format:"xml", type:"rss2.0"]
                               headers:["X-Foo":"bar"],
                               connectTimeout: 5000,
                               readTimeout: 10000,
                               followRedirects: false,
                               useCaches: false,
                               trustAllSSLCerts: false )

### Sending Content

In addition to a Map, the `post/put` methods take an additional parameter of a Closure.

    def client = new RESTClient("http://some.service.net/")
    def response = client.post(path: "/comments") {
        type ContentType.XML
        xml {
            Comment {
                Text("This is my comment.")
            }
        }
    }

When sending content you can also send byte[], text, url encoded parameters, and xml.

    def repsonse = client.post() {
        type "application/vnd.lock-in-proprietary-format"  // String or ContentType
        charset "US-ASCII"

        // one of the following
        bytes new File("payload.txt").bytes
        text "hello world"
        urlenc username: "homer", password: "simpson", timezone: "EST"
        xml { root() }
    }

### Client Defaults

When interacting with a service that requires a particular Accept header or when sending content of the same type/charset, you can set those as defaults so they will be sent for every request (if they are not already specified in the request):

    client.defaultAcceptHeader = "text/xml"
    client.defaultContentTypeHeader = "application/json"
    client.defaultCharset = "UTF-8"

### HTTP Authorization

Currently only *Basic Auth* is supported.

    import wslite.http.auth.*
    import wslite.rest.*

    def client = new RESTClient("http://some.service.net")
    client.authorization = new HTTPBasicAuthorization("homer", "simpson")

### Response

The response has the following properties:

* url
* statusCode // 200
* statusMessage // "Ok"
* contentType // "text/xml" (parameters are not included such as charset)
* charset // UTF-8 (charset parameter parsed from the returned Content-Type header)
* contentEncoding // from headers
* contentLength // from headers
* date // from headers
* expiration // from headers
* lastModified // from headers
* headers // Map (case insensitive) of all headers
* data // byte[] of any content returned from the server

### Content Type Handling

In addition to the above response properties, there are handlers for text and xml responses.

For all text based responses (content type starts with "text/") there will be a *TEXT* (i.e., `response.TEXT`) property available for the response.

For xml based responses, an *XML* (i.e., `response.XML`) property is available that is of type *GPathResult*.

## Dependencies

* [Groovy 1.7.x](http://groovy.codehaus.org)

## Building

groovy-wslite uses Gradle for building. Gradle handles the dependencies
for you so all you need to do is install gradle and then build the
code.

**Build instruction**

1. Download and install [Gradle 0.9.2](http://www.gradle.org/downloads.html)
2. Fetch the latest code: `git clone git://github.com/jwagenleitner/groovy-wslite.git`
3. (Optional) Run the tests using `gradle test`
4. Go to the project directory and run: `gradle jar`

You will find the built jar in `./build/libs`.
