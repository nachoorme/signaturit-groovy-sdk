Signaturit Groovy SDK
===================

This package is a wrapper for Signaturit Api. If you didn't read the documentation yet, maybe it's time to take a look [here](http://docs.signaturit.com/).

You can compile, test and build jar using [gradle](http://gradle.org/gradle-download/).

```
cd signaturit-groovy-sdk
gradle jar
```

Build and install artifact
-------------

You can install signaturit jar in your local maven repository (or modify build.gradle to add your maven remote you use) 

```
cd signaturit-groovy-sdk
gradle install
```

If you are using your local maven repository remember to add `mavenLocal()` or `<localRepository>/path/to/local/repo</localRepository>` in your project in order to dependencies will be resolved successfully.

Integration test
-------------

Signaturit groovy client uses [Spock](https://code.google.com/p/spock/) as testing and specification framework. If you want to run all api integration tests, you will need to do the following customization tasks in SignaturitClientSpec class before running them:

Add a valid OAuth token:

```
static final VALID_TOKEN = "put_a_valid_token_here_before_run_your_tests"
```

Add a valid RECIPIENT information:
```
static final RECIPIENTS = [[fullname:"PUT_YOUR_NAME_HERE", email:"nacho.orme@gmail.com"]]
```

Once these customizations have been made, you can run all library tests:

```
gradle test
```

and see results in the following directory:
```
build/reports/tests/
```


Configuration
-------------

Add following dependency in your JVM project:

```
compile "com.signaturit:signaturit-groovy-sdk:1.0"
```

You will need to import only one class to start using Signaturit:

```
import com.signaturit.api.groovy_sdk.SignaturitClient;
```

Then you can authenticate yourself using your AuthToken

```
SignaturitClient signaturitClient = new SignaturitClient("TOKEN");
```

Remember, the default calls are made to our Sandbox server. If you want to do in production, just set the flag when you do the call.

```
SignaturitClient client = new SignaturitClient("TOKEN", true);
```

Examples
--------

## Signature request


### Get all signature requests

Retrieve all data from your signature requests using different filters.

##### All signatures

```
response = client.get_signatures();
```

##### Getting the last 50 signatures

```
response = client.get_signatures(50,0) 
```

##### Getting the following last 50 signatures

```
response = client.get_signatures(50,0)
```

##### Getting only the finished signatures 

```
response = client.get_signatures (100, 0, [status:3]}
```

##### Getting the finished signatures created since July 20th of 2014

```
response = client.get_signatures (100, 0, [status:3,since:'2014-7-20'])
```

##### Getting signatures with custom field "crm_id"

```
response = client.get_signatures(100,0,[data:[crm_id:2445]]) 
```

### Count signature requests

Count your signature requests.

```
response = client.count_signatures()
```

### Get signature request

Get a single signature request.

```
response = client.get_signature('SIGNATURE_ID')
```

### Get signature documents

Get all documents from a signature request.

```
response = client.get_signature_documents('SIGNATURE_ID')
```

### Get signature document

Get a single document from a signature request.

```
response = client.get_signature_document('SIGNATURE_ID','DOCUMENT_ID')
```

### Signature request

Create a new signature request. Check all [params](http://docs.signaturit.com/api/#sign_create_sign).

```
recipients =  [[fullname:"Bob Soap", email:"bobsoap@signatur.it"]]
params = [subject: 'Receipt number 250', body: 'Please, can you sign this document?']
file_path = '/documents/contracts/125932_important.pdf'
response = client.create_signature (file_path, recipients, params)
```

You can send templates with the fields filled

```
recipients =  [[fullname:"Bob Soap", email:"bobsoap@signatur.it"]]
params = [subject: 'Receipt number 250', body: 'Please, can you sign this document?', templates:['TEMPLATE_ID'], :ata => [WIDGET_ID: 'DEFAULT_VALUE']]

response = client.create_signature(null,RECIPIENTS,params)
```

You can add custom info in your requests

```
recipients =  [[fullname:"Bob Soap", email:"bobsoap@signatur.it"]]
params = [subject: 'Receipt number 250', body: 'Please, can you sign this document?', data: [crm_id:2445]]
file_path = '/documents/contracts/125932_important.pdf'
response = client.create_signature (file_path, recipients, params)
```

### Cancel signature request

Cancel a signature request.

```
response = client.cancel_signature('SIGNATURE_ID')
```

### Send reminder

Send a reminder for signature request job.

```
response = client.send_signature_reminder('SIGNATURE_ID', 'DOCUMENT_ID')
```

### Get audit trail

Get the audit trail of a signature request document and save it in the submitted path.

```
response = client.download_audit_trail ('ID', 'DOCUMENT_ID', '/path/doc.pdf')
```

### Get signed document

Get the signed document of a signature request document and save it in the submitted path.

```
response = client.download_signed_document('ID', 'DOCUMENT_ID', '/path/doc.pdf')
```

## Account

### Get account

Retrieve the information of your account.

```
response = client.get_account()
```

## Branding

### Get brandings

Get all account brandings.

```
response = client.get_brandings()
```

### Get branding

Get a single branding.

```
response = client.get_branding('BRANDING_ID')
```

### Create branding

Create a new branding. You can check all branding params [here](http://docs.signaturit.com/api/#set_branding).`

```
params = [
    corporate_layout_color: '#FFBF00',
    corporate_text_color: '#2A1B0A',
    application_texts: [ :sign_button: 'Sign!' ]
]
response = client.create_branding(params)
```

### Update branding

Update a single branding.

```
params = [ application_texts: [send_button: 'Send!' ]]
response = client.update_branding ('BRANDING_ID', params)
```

### Update branding logo

Change the branding logo.

```
file_path = '/path/new_logo.png'
response = client.update_branding_logo('BRANDING_ID', file_path)
```

### Update branding template

Change a template. Learn more about the templates [here](http://docs.signaturit.com/api/#put_template_branding).

```
file_path = '/path/new_template.html'
response = client.update_branding_email('BRANDING_ID', 'sign_request', file_path)
```

## Template

### Get all templates

Retrieve all data from your templates.

```
response = client.get_templates()
```

## Email


### Get email

Get a single email

```
client.get_email ('EMAIL_ID')
```

### Get email certificates

Get a single email certificates

```
client.get_email_certificates('EMAIL_ID')
```

### Get email certificate

Get a single email certificate

```
client.get_email_certificate('EMAIL_ID', 'CERTIFICATE_ID')
```

### Create email

Create a new certified email.

```
file_path  = '/path/document.pdf'
recipients = [[fullname : 'Mr John', email : 'john.doe@signaturit.com']]
response  = client.create_email (file_path, recipients, 'groovy subject', 'groovy body', [])
```

### Get original file

Get the original document of an email request and save it in the submitted path.

```
response = client.download_email_original_file ('EMAIL_ID','CERTIFICATE_ID','/path/doc.pdf')
```

### Get audit trail document

Get the audit trail document of an email request and save it in the submitted path.

```
response = client.download_email_audit_trail ('EMAIL_ID','CERTIFICATE_ID','/path/doc.pdf')
```


Debugging and troubleshooting
------------------------------

You can turn low level traces on using following logging options to the groovy command:

```
    -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog 
    -Dorg.apache.commons.logging.simplelog.showdatetime=true 
    -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG 
```