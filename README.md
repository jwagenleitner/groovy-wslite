# groovy-wslite

*Note*

    This is a work in progress.

Library for Groovy that aims to provide no-frills SOAP webservice client (and eventually REST)
for interacting with SOAP and REST based webservices.

## Example

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
