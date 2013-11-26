# jOTP

jOTP is a lightweight web application for generating and validating one-time passwords as a secondary factor of authentication.
Passwords may be received via email or text message.

## Usage

Applications can generate and validate one-time password tokens with jOTP via a small set of RESTful web services.  The following
briefly demonstrates the public API:

### Monitor Function

**Description:** The monitor function simply lets the caller know whether or not the application is available.  The string, 'OK'
is returned if this is the case.

**HTTP Verb:** GET

**URL:** (CONTEXT ROOT)/otp/monitor

**Parameters:** None

### Email Function

**Description:** The email function generates a one-time password token and sends it to the requested email address.

**HTTP Verb:** POST

**URL:** (CONTEXT ROOT)/otp/email

**Parameters:**

*address* - Email address to send the token to.

### Text Function

**Description:** The text function generates a one-time password token and attempts to send it to the requested phone
number (cellular providers must be configured in `config.properties`).

**HTTP Verb:** POST

**URL:** (CONTEXT ROOT)/otp/text

**Parameters:**

*number* - Phone number to send the token to.

### Validate Function

**Description:** The validate function determines whether or not a user/token pair are valid.  If the pair is valid,
the 'tokenValid' property of the JSON response will be set to 'true', and the token will be automatically invalidated.

**HTTP Verb:** POST

**URL:** (CONTEXT ROOT)/otp/validate

**Parameters:**

*uid* - User ID.  This can be either an email address or phone number, depending upon which was used to generate the
token initially.

*token* - The string value of the token sent to the user.

## Configuration

jOTP configuration and log files are stored in a directory called, '.jotp' within the (application server) user's
home directory.  When jOTP starts up, it will try to read in a file called, 'config.properties' stored within
this directory.

### Example `config.properties`

	# Minimum and maximum lengths of generated one-time password
	# tokens.  Because these values are equal below, tokens will
	# always be ten characters in length.
	MinOtpLength=10
	MaxOtpLength=10

	# This is the lifetime (in seconds) that one-time use password
	# tokens have before they automatically expire.
	TokenLifetime=60

	# SMTP settings.  This should be configured to use a (largely)
	# unmonitored email inbox.
	SmtpHost=smtp.myhost.com
	SmtpPort=587
	SmtpUsername=example_smtp_user
	SmtpPassword=example_smtp_password

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
	TextProviderHosts=vtext.com,txt.att.net,messaging.sprintpcs.com,tmomail.net

### Other Configuration Guidelines

* jOTP should, in theory, work on any Java web container implementing the Servlet 3.0
	specification.  Testing has only taken place on Tomcat 7, however.
* Be sure to enable SSL on any production instances of jOTP.
