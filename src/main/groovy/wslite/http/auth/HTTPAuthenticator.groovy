package wslite.http.auth

import wslite.http.HTTPClientException

interface HTTPAuthenticator {
    boolean authenticate() throws HTTPClientException
}