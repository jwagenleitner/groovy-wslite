# groovy-wslite

Library for Groovy that provides no-frills SOAP and REST webservice clients.

This library assumes you know exactly what messages you want to send to your services and want full control over the request.  No streams are used and all request/responses are buffered in memory for convenience.

**Note**

Please consult the [Changelog] (https://github.com/jwagenleitner/groovy-wslite/blob/master/CHANGELOG.md) for any breaking changes.

## SOAP

### Example

    @GrabResolver(name='groovy-wslite', root='https://oss.sonatype.org/content/groups/public', m2Compatible=true)
    @Grab(group='com.github.groovy-wslite', module='groovy-wslite', version='0.2-SNAPSHOT', changing=true)
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
    assert 200 == response.httpResponse.statusCode
    assert "OK" == response.httpResponse.statusMessage
    assert "ASP.NET" == response.httpResponse.headers["X-Powered-By"]

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

If you have a string with XML content you want to include in you can use `mkp`.

    def response = soapClient.send {
        body {
            GetWeatherByZipCode(xmlns:"http://www.webservicex.net") {
                mkp.yieldUnescaped "<ZipCode>93657</ZipCode>"
            }
        }
    }

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

`response.envelope`

To get straight to the Header or Body element...

`response.header` or `response.body`

You can access the first child element of the Body by name `response.GetWeatherByZipCodeResponse`

For a response with a SOAP Fault `response.hasFault()` and `response.fault`.

If you just want the text of the response use `response.text`.

You can also access the underlying HTTPRequest `response.httpRequest` and HTTPResponse `response.httpResponse` objects.

### SOAP Faults

If the server responds with a SOAP Fault a `SOAPFaultException` will be thrown.  The `SOAPFaultException` provides access to the `faultcode/faultstring/faultactor/details` properties and also includes the parsed SOAPResponse via a `response` property.  You can also access the underlying HTTPRequest `response.httpRequest` and HTTPResponse `response.httpResponse` objects.

## REST

### Example

    @Grab(group='com.github.groovy-wslite', module='groovy-wslite', version='0.1')
    import wslite.rest.*

    def client = new RESTClient("http://www.fresnostatenews.com/feed/")
    def response = client.get()

    assert 200 == response.statusCode
    assert "text/xml" == response.contentType
    assert "UTF-8" == response.charset
    assert "text/xml; charset=UTF-8" == response.headers."Content-Type"
    assert "FresnoStateNews.com" == response.xml.channel.title.text()

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

    def response = client.post() {
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

#### Basic Auth

    import wslite.http.auth.*
    import wslite.rest.*

    def client = new RESTClient("http://some.service.net")
    client.authorization = new HTTPBasicAuthorization("homer", "simpson")

#### SSL Keystore Support

##### Example using REST:

    import wslite.http.auth.*
    import wslite.rest.*

    def client = new RESTClient("http://some.service.net")
    client.authorization = new SSLKeystoreAuthentication("mykeystore.jks", "mypassword")
   
##### Example using SOAP:

    import wslite.http.auth.*
    import wslite.soap.*

    def httpClient = new HTTPClient()
    client.authorization = new SSLKeystoreAuthentication("mykeystore.jks", "mypassword")
    def soapClient = new SOAPClient("https://www.example.com/ExampleService?WSDL", httpClient)

### Response

The response has the following properties:

* `url`
* `statusCode`        // 200
* `statusMessage`     // "Ok"
* `contentType`       // "text/xml" (parameters are not included such as charset)
* `charset`           // UTF-8 (charset parameter parsed from the returned Content-Type header)
* `contentEncoding`   // from headers
* `contentLength`     // from headers
* `date`              // from headers
* `expiration`        // from headers
* `lastModified`      // from headers
* `headers`           // Map (case insensitive) of all headers
* `data`              // byte[] of any content returned from the server

The response also includes the original *HTTPReqeust* (ex. `response.request`).

### Content Type Handling

In addition to the above response properties, there are handlers for text, xml and json responses.

For all text based responses (content type starts with "text/") there will be a *text* (i.e., `response.text`) property available for the response.

For xml based responses, an *xml* (i.e., `response.xml`) property is available that is of type *GPathResult*.

For json based responses, a *json* (i.e., `response.json`) property is available that is of type *JSONObject* or *JSONArray*.

## Using groovy-wslite in your project

__groovy-wslite__ is available in Maven Central.

### Maven

#### Releases

    <dependency>
        <groupId>com.github.groovy-wslite</groupId>
        <artifactId>groovy-wslite</artifactId>
        <version>0.1</version>
    </dependency>

#### Snapshots

    <repositories>
        <repository>
            <id>groovy-wslite</id>
            <url>https://oss.sonatype.org/content/groups/public</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.github.groovy-wslite</groupId>
            <artifactId>groovy-wslite</artifactId>
            <version>0.2-SNAPSHOT</version>
        </dependency>
    </dependencies>

### Groovy Grape

#### Releases

    @Grab(group='com.github.groovy-wslite', module='groovy-wslite', version='0.1')

#### Snapshots

    @GrabResolver(name='groovy-wslite', root='https://oss.sonatype.org/content/groups/public', m2Compatible=true)
    @Grab(group='com.github.groovy-wslite', module='groovy-wslite', version='0.2-SNAPSHOT', changing=true)

## Using with Grails

The SOAP/RESTClients can easily be configured and used in your Grails application.

* Add the dependency to `grails-app/conf/BuildConfig.groovy`.

*Note: You must enable the mavenCentral() repository.*

    grails.project.dependency.resolution = {
        ....
        ....
        repositories {
            ....
            ....
            mavenCentral()
            // uncomment below in order to use snapshots
            //mavenRepo "https://oss.sonatype.org/content/groups/public"
        }
        dependencies {
            runtime 'com.github.groovy-wslite:groovy-wslite:0.1'
        }
    }

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

* [Groovy 1.7.6] (http://groovy.codehaus.org) or higher

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
