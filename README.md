
# groovy-wslite

Library for Groovy that provides no-frills SOAP and REST webservice clients.

This library assumes you know exactly what messages you want to send to your services and want full control over the request.  No streams are used and all request/responses are buffered in memory for convenience.

## SOAP

### Example

    import wslite.soap.*

    def soapClient = new SOAPClient("http://www.webservicex.net/WeatherForecast.asmx")
    def response = soapClient.send {
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

### Usage

    def soapClient = new SOAPClient("http://www.webservicex.net/WeatherForecast.asmx")
    def response = soapClient.send(SOAPAction: "GetWeatherByZipCode",
                                   connectTimeout:5000,
                                   readTimeout:10000,
                                   useCaches:false,
                                   followRedirects:false,
                                   trustAllSSLCerts:true) {
        version SOAPVersion.V1_2        // SOAPVersion.V1_1 is default
        soapNamespacePrefix "soap-env"  // "SOAP" is default
        encoding "ISO-8859-1"           // "UTF-8" is default encoding for xml
        envelopeAttributes "xmlns:hr":"http://example.org/hr"
        header(mustUnderstand:false) {
            auth {
                apiToken("1234567890")
            }
        }
        body {
            GetWeatherByZipCode(xmlns:"http://www.webservicex.net") {
                ZipCode("93657")
            }
        }
    }

The `header` and `body` closures are passed to a MarkupBuilder in order to create the SOAP message.  
You can also pass a raw string to the send method if you want absolute control over the resulting message.

    soapClient.send(
        """<?xml version='1.0' encoding='UTF-8'?>
           <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
               <SOAP:Body>
                   <GetFoo>bar</GetFoo>
               </SOAP:Body>
           </SOAP:Envelope>"""
    )

The default when sending a raw string is SOAP v1.1, you can override this by specifying a SOAPVersion.

    soapClient.send(SOAPVersion.V1_2,
                    """<?xml version='1.0' encoding='UTF-8'?>
                       <SOAP:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                           <SOAP:Body>
                               <GetFoo>bar</GetFoo>
                            </SOAP:Body>
                        </SOAP:Envelope>""")

You can also specify connection settings.

    soapClient.send(SOAPVersion.V1_2,
                    connectTimeout:7000,
                    readTimeout:9000,
                    """<?xml version='1.0' encoding='UTF-8'?>
                       <SOAP:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                           <SOAP:Body>
                               <GetFoo>bar</GetFoo>
                           </SOAP:Body>
                       </SOAP:Envelope>""")

### Response

The response is automatically parsed by XmlSlurper and provides several convenient methods for accessing the SOAP response.

`response.Envelope`

To get straight to the Header or Body element...

`response.header` or `response.body`

You can access the first child element of the Body by name `response.GetWeatherByZipCodeResponse`

For a response with a SOAP Fault `response.hasFault()` and `response.fault`.

If you just want the text of the response use `response.text`.

You can also access the underlying HTTPResponse `response.http`.

### SOAP Faults

If the server responds with a SOAP Fault a `SOAPFaultException` will be thrown.  The `SOAPFaultException` provides access to the `faultcode/faultstring/faultactor/details` properties and also includes the parsed SOAPResponse via a `response` property.

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

When sending content you can also send byte[], text, url encoded parameters, xml and json.

    def repsonse = client.post() {
        type "application/vnd.lock-in-proprietary-format"  // String or ContentType
        charset "US-ASCII"

        // one of the following
        bytes new File("payload.txt").bytes
        text "hello world"
        urlenc username: "homer", password: "simpson", timezone: "EST"
        xml { root() }
        json id:"525", department:"Finance"
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

In addition to the above response properties, there are handlers for text, xml and json responses.

For all text based responses (content type starts with "text/") there will be a *TEXT* (i.e., `response.TEXT`) property available for the response.

For xml based responses, an *XML* (i.e., `response.XML`) property is available that is of type *GPathResult*.

For json based responses, a *JSON* (i.e., `response.JSON`) property is available that is of type *JSONObject* or *JSONArray*.

## Using with Grails

The SOAP/RESTClients can easily be configured and used in your Grails application.

* Copy the groovy-wslite-*.jar into the `lib/` directory

* Configure the clients in `grails-app/conf/spring/resources.groovy`

For example:

    clientBasicAuth(wslite.http.auth.HTTPBasicAuthorization) {
        username = "Aladdin"
        password = "open sesame"
    }

    httpClient(wslite.http.HTTPClient) {
        connectTimeout = 5000
        readTimeout = 10000
        useCaches = false
        followRedirects = false
        trustAllSSLCerts = false
        // authorization = ref('clientBasicAuth')
    }

    soapClient(wslite.soap.SOAPClient) {
        serviceURL = "http://example.org/soap"
        httpClient = ref('httpClient')
        // authorization = ref('clientBasicAuth')
    }

    restClient(wslite.rest.RESTClient) {
        url = "http://example.org/services"
        httpClient = ref('httpClient')
        authorization = ref('clientBasicAuth')
    }

* In your controller/service/taglib/etc. you can access the configured client(s) as you would any Grails service.

For example:

    package org.example

    class MyService {

        def restClient
        def soapClient

        def someServiceMethod() {
            def response = restClient.get()
            ....
        }

        def someOtherServiceMethod() {
            def response soapClient.send { ... }
        }
    }

## Dependencies

* [Groovy 1.7.x](http://groovy.codehaus.org)

## Building

groovy-wslite uses Gradle for building. Gradle handles the dependencies
for you so all you need to do is install Gradle and then build the
code.

**Build Instructions**

1. Download and install [Gradle](http://www.gradle.org/downloads.html)
2. Fetch the latest code: `git clone git://github.com/jwagenleitner/groovy-wslite.git`
3. (Optional) Run the tests using `gradle test`
4. Go to the project directory and run: `gradle jar`

You will find the built jar in `./build/libs`.
