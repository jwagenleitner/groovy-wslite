# groovy-wslite

*Note*

    This is a work in progress.

Library for Groovy that aims to provide no-frills SOAP webservice client (and eventually REST)
for interacting with SOAP and REST based webservices.

## Example

    import wslite.soap.*

    def client = new SOAPClient(serviceURL:'http://www.holidaywebservice.com/Holidays/US/Dates/USHolidayDates.asmx')
    def resp = client.send(SOAPAction:'http://www.27seconds.com/Holidays/US/Dates/GetMartinLutherKingDay') {
        body {
            GetMartinLutherKingDay('xmlns':'http://www.27seconds.com/Holidays/US/Dates/') {
                year(2011)
            }
        }
    }
    "2011-01-15T00:00:00" == resp.Envelope.Body.GetMartinLutherKingDayResponse.GetMartinLutherKingDayResult.text()
    200 == resp.status
    "OK" == resp.statusMessage
    "ASP.NET" == resp.headers['X-Powered-By']

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
