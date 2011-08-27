## 0.2 (??)

* SOAPClient - changes to response and exception classes
 * responses now include both the HTTPRequest`response.httpRequest` and HTTPResponse `response.httpResponse`
 * `response.http` was changed to `response.httpRequest`
 * `response.Envelope` was changed to `response.envelope`
 * exceptions now also include the same `httpRequest` and `httpResponse` objects.

* RESTClient - changes to the response and exception classes
 * `response.XML -> response.xml`, `response.JSON -> response.json`, `response.TEXT -> response.text`
 * exceptions now also include the request/response if available.

## 0.1 (2011-03-27)

* Initial release

