# jOTP

[![Build Status](https://travis-ci.org/upcrob/jOTP.png)](https://travis-ci.org/upcrob/jOTP)

jOTP is a lightweight web application for generating and validating one-time passwords as a secondary factor of authentication.
Passwords may be received via email or text message.

## Usage

Applications can generate and validate one-time password tokens with jOTP via a small set of RESTful web services.  The following
briefly demonstrates the public API:

### Monitor Function

**Description:** The monitor function simply lets the caller know whether or not the application is available.  The string, 'OK'
is returned if this is the case.

**HTTP Verb:** GET

**URL:** (CONTEXT ROOT)/sys/monitor

**Parameters:** None

### Email Function

**Description:** The email function generates a one-time password token and sends it to the requested email address.

**HTTP Verb:** POST

**URL:** (CONTEXT ROOT)/otp/email

**Parameters:**

*client* - Name of client application.

*clientpassword* - Password of client application.

*address* - Email address to send the token to.

### Text Function

**Description:** The text function generates a one-time password token and attempts to send it to the requested phone
number (cellular providers must be configured in `config.properties`).

**HTTP Verb:** POST

**URL:** (CONTEXT ROOT)/otp/text

**Parameters:**

*client* - Name of client application.

*clientpassword* - Password of client application.

*number* - Phone number to send the token to.

### Validate Function

**Description:** The validate function determines whether or not a user/token pair are valid.  If the pair is valid,
the 'tokenValid' property of the JSON response will be set to 'true', and the token will be automatically invalidated.

**HTTP Verb:** POST

**URL:** (CONTEXT ROOT)/otp/validate

**Parameters:**

*client* - Name of client application.

*clientpassword* - Password of client application.

*uid* - User ID.  This can be either an email address or phone number, depending upon which was used to generate the
token initially.

*token* - The string value of the token sent to the user.

## Configuration

jOTP configuration and log files are stored in a directory called, '.jotp' within the (application server) user's
home directory.  When jOTP starts up, it will try to read in a YAML file called, 'config.yaml' stored within
this directory.

### Example `config.yaml`

	# SMTP settings.  This should be configured to use a (largely)
	# unmonitored email inbox.
	SmtpHost: smtp.myhost.com
	SmtpPort: 587
	SmtpUsername: example_smtp_user
	SmtpPassword: example_smtp_password

	# This optional property defines whether or not SMTP operations
	# should block HTTP responses.  If set to true, HTTP responses
	# won't be sent until emails have been verified to have been
	# sent successfully.  If set to false, SMTP operations take
	# place in a separate thread are assumed to always be successful.
	# If not set in the configuration, this property defaults to false.
	BlockingSmtp: false

	# Instructs the system to use local memory to cache OTP tokens.
	# This is the fastest option, but does not allow for multiple
	# application instances for failover.  If this property is not
	# set in the configuration, a local store is used by default.
	TokenstoreType: local

	# Instructs the system to use a JDBC datasource to cache OTP
	# tokens using the provided JDBC connection string and driver
	# (with the fully-qualified class name).  The driver JAR(s)
	# will need to be added to the web server's classpath (eg.
	# the 'lib' directory in Apache Tomcat).  Note that a table
	# called, 'tokenstore' will need to be created within the
	# database with the following columns:
	#   client     VARCHAR(255)
	#   uid        VARCHAR(255)
	#   token      VARCHAR(255)
	#   expiration BIGINT
	#
	# Note that the primary key should be set to (client, uid).
	#
	# TokenstoreType: jdbc
	# JdbcString: jdbc:derby://localhost:1527/mydatabase
	# JdbcDriver: org.apache.derby.jdbc.ClientDriver

	# Instructs the system to use a Redis server to cache OTP
	# tokens using the provided hostname, port, and password.
	# If no port is set, the default Redis port will be used.
	# If no password is set, the system will assume that no
	# authentication is necessary.
	#
	# TokenstoreType: redis
	# RedisHost: localhost
	# RedisPort: 6379
	# RedisPassword: myredispassword
	
	# This is the list of all cellular providers that jOTP will
	# try when it sends text messages.
	# Verizon=vtext.com
	# AT&T=txt.att.net
	# T-Mobile=tmomail.net
	# Sprint=messaging.sprintpcs.com
	# Boost Mobile=myboostmobile.com
	# US Cellular=email.uscc.net
	# Alltel=message.alltel.com
	# Virgin Mobile=vmobl.com
	MobileProviderHosts:
	  - vtext.com
	  - txt.att.net
	  - messaging.sprintpcs.com
	  - tmomail.net

	# List of all client (application users) of the system.  Each
	# client has its own pool of tokens.  For example, if app1
	# sends the token 'ABC' to test@example.com and app2
	# simultaneously sends 'ABC' to test@example.com, 'ABC' could
	# be validated against each of these pools successfully.
	# Note that each client has its own password (used when sending
	# requests to the system), as well settings for token length
	# and maximum lifetime (in seconds).
	Clients:
	  - Name: app1
	    Password: app1password
	    MinOtpLength: 5
		MaxOtpLength: 5
		TokenLifetime: 90
	  - Name: app2
	    Password: app2password
	    MinOtpLength: 10
		MaxOtpLength: 10
		TokenLifetime: 300

### Other Configuration Guidelines

* jOTP should, in theory, work on any Java web container implementing the Servlet 3.0
	specification.  Testing has only taken place on Tomcat 7, however.
* Be sure to enable SSL on any production instances of jOTP.
