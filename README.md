# OWASP jOTP

[![Build Status](https://travis-ci.org/upcrob/jOTP.png)](https://travis-ci.org/upcrob/jOTP)
[![Download](https://api.bintray.com/packages/upcrob/generic/jOTP/images/download.svg)](https://bintray.com/upcrob/generic/jOTP/_latestVersion)

[OWASP jOTP](https://www.owasp.org/index.php/OWASP_JOTP_Project) is a microservice for generating and validating one-time passwords as a secondary factor of authentication.

A common use case for jOTP is as follows:

1. Client applications displays a login page requesting the user enter his/her username and password.
2. If the credentials check passes, the user's email is looked up and a message containing the token is sent.
3. The application then requests that the OTP token that was sent be entered in a text box.  Once entered, it is sent to jOTP.
4. jOTP validates the token.  If the token was valid, the application finishes authenticating the user.  If the token was not valid, the user is redirected to the login page.

## Usage

### Monitor Function

**Description:** The monitor function simply lets the caller know whether or not the application is available.  It will return an HTTP 200 status and "OK" in the response body if jOTP is available.

**HTTP Verb:** GET

**URL:** `/`

**Parameters:** None

### Generate OTP Token

**Description:** POSTing to the `/sessions` endpoint tells jOTP to generate an OTP token and send it to the user.  If successful, jOTP responds with a 201 and the session endpoint to validate against in the `Location` header.

**HTTP Verb:** POST

**URL:** `/sessions`

**Request Body Format:**

	{
		"email": "[email address to send token to]",
		"subject": "[email subject]",
		"message": "[email message]",
		"ttl": "[number of minutes before token expires]"
	}

jOTP replaces `$token` in the message with the generated token and `$session` with the generated session.

### Validate Token

**Description:** POSTing the OTP token to the matching session endpoint validates the token.  jOTP responds with a 200 HTTP status if the token is valid.

**HTTP Verb:** POST

**URL:** `/sessions/[session id]`

**Request Body:** [plaintext OTP token]

## Configuration

The following 2 JVM properties are used by the application.  These can be customized by setting the `JAVA_OPTS` environment variable.:

* **propertyfiles** This should be a (comma-delimited) list of property files that jOTP should read when it starts up.  This defaults to a file called `jotp.properties` located in the current working directory.
* **logfile** This should be the absolute path of the file where jOTP logs should be written.  Log files will be rolled over hourly in the same directory.  This defaults to a file called `jotp.log` located in the current working directory.

The following properties may be set in any of the property files specified in the `properyfiles` JVM property:

**log.file** Path to log file.  Files will be rolled over with the date in the same directory.

**http.port** HTTP port the web server should listen on.

**base.url** The base URL for the service (e.g. http://localhost:8080).  This is used to construct the `Location` header.

**smtp.host** SMTP server.

**smtp.port** SMTP server port.

**smtp.from** Email address to send emails from.

**smtp.tls** If jOTP should connect to the SMTP server over TLS.

**smtp.username** SMTP username.  Optional.

**smtp.password** SMTP password.  Optional.

**repository.type** Type of repository to use (`redis` or `jdbc`).  If not set, jOTP defaults to a local in-memory repository that is useful for local development but not recommended for production environments.

### Redis Properties

**redis.host** Redis server address.

**redis.port** Redis server port.

**redis.creds** Redis server credentials.  Optional.

### JDBC Properties

**jdbc.url** URL for connecting to DBMS.  Note that jOTP does not include any JDBC drivers so these will need to be added to your runtime classpath.

**jdbc.table** Name of the table jOTP should use to store tokens in.  It should follow the following schema:

	session    VARCHAR(255) PRIMARY KEY
	token      VARCHAR(255) PRIMARY KEY
	expiretime TIMESTAMP
