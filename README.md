# groovy-wslite

Library for Groovy that provides no-frills SOAP and REST webservice clients.

This library assumes you know exactly what messages you want to send to your services and want full control over the
request.  No streams are used and all request/responses are buffered in memory for convenience.

**Note**

Please consult the [Changelog] (https://github.com/jwagenleitner/groovy-wslite/blob/master/CHANGELOG.md) for any
breaking changes.

## SOAP

### Example

``` groovy
@Grab(group='com.github.groovy-wslite', module='groovy-wslite', version='0.8.2')
import wslite.soap.*

def client = new SOAPClient('http://www.holidaywebservice.com/Holidays/US/Dates/USHolidayDates.asmx')
def response = client.send(SOAPAction:'http://www.27seconds.com/Holidays/US/Dates/GetMothersDay') {
    body {
        GetMothersDay('xmlns':'http://www.27seconds.com/Holidays/US/Dates/') {
            year(2011)
        }
    }
}

assert "2011-05-08T00:00:00" == response.GetMothersDayResponse.GetMothersDayResult.text()
assert 200 == response.httpResponse.statusCode
assert "ASP.NET" == response.httpResponse.headers['X-Powered-By']
```

### Usage

``` groovy
def client = new SOAPClient("http://...")
def response = client.send(SOAPAction: "SomeAction",
                           connectTimeout:5000,
                           readTimeout:10000,
                           useCaches:false,
                           followRedirects:false,
                           sslTrustAllCerts:true) {
    version SOAPVersion.V1_2        // SOAPVersion.V1_1 is default
    soapNamespacePrefix "SOAP"      // "soap-env" is default
    encoding "ISO-8859-1"           // "UTF-8" is default encoding for xml
    envelopeAttributes "xmlns:hr":"http://example.org/hr"
    header(mustUnderstand:false) {
        auth {
            apiToken("1234567890")
        }
    }
    body {
        GetWeatherByZipCode(xmlns:"http://example.weather.org") {
            ZipCode("93657")
        }
    }
}
```

The `header` and `body` closures are passed to a MarkupBuilder in order to create the SOAP message.

If you have a string with XML content you want to include in you can use `mkp`.

``` groovy
def response = client.send {
    body {
        GetWeatherByZipCode(xmlns:"http://example.weather.org") {
            mkp.yieldUnescaped "<ZipCode>93657</ZipCode>"
        }
    }
}
```

You can also pass a raw string to the send method if you want absolute control over the resulting message.

``` groovy
client.send(
    """<?xml version='1.0' encoding='UTF-8'?>
       <soap-env:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
           <soap-env:Body>
               <GetFoo>bar</GetFoo>
           </soap-env:Body>
       </soap-env:Envelope>"""
)
```

The SOAP version will be auto-detected using the namespace URI of the Envelope element, you can
override this by specifying a SOAPVersion.

``` groovy
client.send(SOAPVersion.V1_2,
            """<?xml version='1.0' encoding='UTF-8'?>
               <soap-env:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                   <soap-env:Body>
                       <GetFoo>bar</GetFoo>
                    </soap-env:Body>
                </soap-env:Envelope>""")
```

You can also specify connection settings.

``` groovy
client.send(SOAPVersion.V1_2,
            connectTimeout:7000,
            readTimeout:9000,
            """<?xml version='1.0' encoding='UTF-8'?>
               <soap-env:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                   <soap-env:Body>
                       <GetFoo>bar</GetFoo>
                   </soap-env:Body>
               </soap-env:Envelope>""")
```

### SSL

#### Using a custom SSL trust store

In addition to setting a global trust store and trust store password using the `javax.net.ssl.trustStore` and
`javax.net.ssl.trustStorePassword` System properties, you can set a custom trust store on a client.

``` groovy
import wslite.soap.*

def client = new SOAPClient("https://www.example.com/ExampleService")
client.httpClient.sslTrustStoreFile = "~/mykeystore.jks"
client.httpClient.sslTrustStorePassword = "secret"

def response = client.send() {
    ....
}
```

You can also specify a custom trust store on a per request basis, this will override any custom trust store that may be
set on the client.

``` groovy
def client = new SOAPClient("https://www.example.com/ExampleService")
def response = client.send(sslTrustStoreFile:"~/mykeystore.jks", sslTrustStorePassword:"secret") {
    ....
}
```

Note: sslTrustStorePassword is optional.

#### Trusting all SSL certs

When in development mode and dealing with lots of servers with self-signed certs it can be helpful to bypass a custom
trust store and trust all certs automatically.

``` groovy
import wslite.soap.*

def client = new SOAPClient("https://www.example.com/ExampleService")
client.httpClient.sslTrustAllCerts = true

def response = client.send() {
    ....
}
```

You can also specify a the same parameter on a per request basis.

``` groovy
def client = new SOAPClient("https://www.example.com/ExampleService")
def response = client.send(sslTrustAllCerts:true) {
    ....
}
```

Note: sslTrustAllCerts overrides any custom trust store settings that may have already be set on the client or
the request.

### Response

The response is automatically parsed by XmlSlurper and provides several convenient properties for accessing the SOAP
message.

`response.envelope`

To get straight to the Header or Body element...

`response.header` or `response.body`

You can access the first child element of the Body by name `response.GetWeatherByZipCodeResponse`

If you just want the text of the response use `response.text`.

You can also access the underlying HTTPRequest `response.httpRequest` and HTTPResponse `response.httpResponse` objects.

### SOAP Faults

If the server responds with a SOAP Fault a `SOAPFaultException` will be thrown.  The `SOAPFaultException` wraps a
`SOAPResponse` that contains the Fault.

``` groovy
import wslite.soap.*

def client = new SOAPClient("http://www.webservicex.net/WeatherForecast2.asmx")
try {
    def response = client.send {
        ....
    }
} catch (SOAPFaultException sfe) {
    println sfe.message // faultcode/faultstring for 1.1 or Code/Reason for 1.2
    println sfe.text    // prints SOAP Envelope
    println sfe.httpResponse.statusCode
    println sfe.fault.detail.text() // sfe.fault is a GPathResult of Envelope/Body/Fault
} catch (SOAPClientException sce) {
    // This indicates an error with underlying HTTP Client (i.e., 404 Not Found)
}
```

### Proxy

If behind proxy, you can set it in the request.

``` groovy
def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('proxy.example.com', 8080))

def client = new SOAPClient("https://www.example.com/ExampleService")
def response = client.send(proxy:proxy) {
    ....
}
```

If the proxy requires authentication...

``` groovy
Authenticator.setDefault(new Authenticator() {
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("username","password".toCharArray())
    }
})
```

You can also set the proxy on the SOAP client itself or via the standard java.net "http.proxyHost" and "http.proxyPort" system properties (or their "https.*" counterparts). To configure the client with a proxy, use code like this:

``` groovy
def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('proxy.example.com', 8080))

def client = new SOAPClient("https://www.example.com/ExampleService")
client.httpClient.proxy = proxy
....
```

In decreasing precedence, groovy-wslite picks the proxy settings from:

1. The request's proxy
2. The client's proxy
3. The java.net system properties
4. No proxy

## REST

### Example

``` groovy
@Grab(group='com.github.groovy-wslite', module='groovy-wslite', version='0.8.2')
import wslite.rest.*

def client = new RESTClient("http://api.twitter.com/1/")
def response = client.get(path:'/users/show.json', query:[screen_name:'jwagenleitner', include_entities:true])

assert 200 == response.statusCode
assert "John Wagenleitner" == response.json.name
```

### Methods

*RESTClient* supports the following methods:

* get
* delete
* post
* put

### Parameters

The methods can all take a map as a parameter (though not required) that give you control over the request.

``` groovy
def client = new RESTClient("http://api.twitter.com/1/")
def response = client.get( path:'/users/show.json',
                           accept: ContentType.JSON,
                           query:[screen_name:'jwagenleitner', include_entities:true],
                           headers:["X-Foo":"bar"],
                           connectTimeout: 5000,
                           readTimeout: 10000,
                           followRedirects: false,
                           useCaches: false,
                           sslTrustAllCerts: true )
```

### Sending Content

In addition to a Map, the `post/put` methods take an additional parameter of a Closure.

``` groovy
def client = new RESTClient("http://some.service.net/")
def response = client.post(path: "/comments") {
    type ContentType.XML
    xml {
        Comment {
            Text("This is my comment.")
        }
    }
}
```

When sending content you can also send byte[], text, url encoded parameters, xml and json.

``` groovy
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
```

You can also do multipart requests for uploading files and such. You don't need to specify content type as this will be multipart/form-data
and will have a content boundary assigned to it.

```groovy
def response = client.post() {

    // call once for each body-part
    multipart 'username', 'antony'.bytes
    multipart 'files[myfile.png]', myFile.bytes
}
```

### Client Defaults

When interacting with a service that requires a particular Accept header or when sending content of
the same type/charset, you can set those as defaults so they will be sent for every request
(if they are not already specified in the request):

``` groovy
client.defaultAcceptHeader = "text/xml"
client.defaultContentTypeHeader = "application/json"
client.defaultCharset = "UTF-8"
```

### HTTP Authorization

Currently only *Basic Auth* is supported.

#### Basic Auth

``` groovy
import wslite.http.auth.*
import wslite.rest.*

def client = new RESTClient("http://some.service.net")
client.authorization = new HTTPBasicAuthorization("homer", "simpson")
```

### SSL

#### Using a custom SSL trust store

In addition to setting a global trust store and trust store password using the `javax.net.ssl.trustStore` and
`javax.net.ssl.trustStorePassword` System properties, you can set a custom trust store on a client.

``` groovy
import wslite.rest.*

def client = new RESTClient("http://some.service.net")
client.httpClient.sslTrustStoreFile = "~/mykeystore.jks"
client.httpClient.sslTrustStorePassword = "myKeystorePassword"

def response = client.get()
```

You can also specify a custom trust store on a per request basis, this will override any custom
trust store that may be set on the client.

``` groovy
def client = new RESTClient("http://some.service.net")
client.get(sslTrustStoreFile:"~/mykeystore.jks", sslTrustStorePassword:"secret")
```

Note: sslTrustStorePassword is optional.

#### Trusting all SSL certs

When in development mode and dealing with lots of servers with self-signed certs it can be helpful
to bypass a custom trust store and trust all certs automatically.

``` groovy
import wslite.rest.*

def client = new RESTClient("http://some.service.net")
client.httpClient.sslTrustAllCerts = true

def response = client.get()
```

You can also specify a the same parameter on a per request basis.

``` groovy
def client = new RESTClient("http://some.service.net")
def response = client.get(sslTrustAllCerts:true)
```

Note: sslTrustAllCerts overrides any custom trust store settings that may have already be set
on the client or the request.

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

## Proxies

If you want to send requests via a proxy, you can configure one in several ways. You can do it at the level of the request:

``` groovy
// SOAPClient
def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('proxy.example.com', 8080))

def client = new SOAPClient("https://www.example.com/ExampleService")
def response = client.send(proxy:proxy) {
    ....
}

// RESTClient
def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('proxy.example.com', 8080))

def client = new RESTClient("http://api.twitter.com/1/")
def response = client.get(path:'/users/show.json', proxy:proxy, query:[screen_name:'jwagenleitner', include_entities:true])
```

You can also set the proxy on the SOAP client or REST client itself:

``` groovy
def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('proxy.example.com', 8080))

def client = new SOAPClient("https://www.example.com/ExampleService")
client.httpClient.proxy = proxy
....
```

Finally, you can use the standard java.net "http.proxyHost" and "http.proxyPort" system properties (or their "https.*" counterparts). 

In decreasing precedence, groovy-wslite picks the proxy settings from:

1. The request's proxy
2. The client's proxy
3. The java.net system properties
4. No proxy

If the proxy requires authentication, then you will need to set an `Authenticator`:

``` groovy
Authenticator.setDefault(new Authenticator() {
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("username","password".toCharArray())
    }
})
```

## Using groovy-wslite in your project

* [Download JAR Files] (/jwagenleitner/groovy-wslite/archives/master)

__groovy-wslite__ is available in Maven Central.

### Maven

#### Releases

    <dependency>
        <groupId>com.github.groovy-wslite</groupId>
        <artifactId>groovy-wslite</artifactId>
        <version>0.8.2</version>
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
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

### Groovy Grape

#### Releases

    @Grab(group='com.github.groovy-wslite', module='groovy-wslite', version='0.8.2')

#### Snapshots

    @GrabResolver(name='groovy-wslite', root='https://oss.sonatype.org/content/groups/public', m2Compatible=true)
    @Grab(group='com.github.groovy-wslite', module='groovy-wslite', version='1.0.0-SNAPSHOT', changing=true)

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
            runtime 'com.github.groovy-wslite:groovy-wslite:0.8.2'
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
        sslTrustAllCerts = true
        // authorization = ref('clientBasicAuth')
        // proxy = myproxy
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

``` groovy
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
```

## Versioning

This project uses [Semantic Versioning] (http://semver.org/).

## Dependencies

* [Groovy 1.7.6] (http://groovy.codehaus.org) or higher

## Building

groovy-wslite uses [Gradle](http://www.gradle.org/downloads.html) for the build process.

**Build Instructions**

1. Fetch the latest code: `git clone git://github.com/jwagenleitner/groovy-wslite.git`
2. (Optional) Run the unit tests using `gradlew test`
3. (Optional) Run the integration tests using `gradlew integrationTest`
4. (Optional) Run the code quality checks `gradlew codenarcMain codenarcTest`
5. Go to the project directory and run: `gradlew jar`

You will find the built jar in `./build/libs`.
