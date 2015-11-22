package com.signaturit.api.groovy_sdk


import static groovyx.net.http.Method.*
import groovy.json.JsonOutput
import groovyx.net.http.ContentType
import groovyx.net.http.EncoderRegistry
import groovyx.net.http.HTTPBuilder

import javax.activation.MimetypesFileTypeMap

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.http.Consts
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.InputStreamBody
import org.apache.http.entity.mime.content.StringBody

class SignaturitClient {
    
    static final String PRODUCTION_URL = "https://api.signaturit.com"
    static final String SANDBOX_URL =  "http://api.sandbox.signaturit.com"
    static final String LOCAL_URL = "http://127.0.0.1:9000"
    static final Boolean DEBUG_USING_LOCAL_SERVER = false
    
    private def base
    private def token
    private def http 
    
    private Log log = LogFactory.getLog(SignaturitClient.class);
    
    public SignaturitClient(token, production=false){
        base = production? PRODUCTION_URL : SANDBOX_URL
       
        if (DEBUG_USING_LOCAL_SERVER){
            base = LOCAL_URL
        }
        
        this.token = token
        http = new HTTPBuilder(base)
        http.headers.Accept = "application/json;charset=UTF-8"
        http.headers.'User-Agent' = "signaturit-ruby-sdk 0.0.4"
        http.headers.Authorization = "Bearer ${token}" 
        http.handler.failure = { resp, reader ->
            def result = reader
            log.error("${resp.statusLine} ${result}");            
            return result
        }
        http.handler.success = { resp, reader ->
            def result = reader
            log.debug("Success: ${result}");                                  
            return result         
          }
    }
    
    def get_account(){        
        def result = http.get(path:"/v2/account.json",contentType:ContentType.JSON)
                  
        return result
    }
    
    def get_signature(signature_id){
        def result = http.get(path:"/v2/signs/${signature_id}.json",contentType:ContentType.JSON)        
        
        return result
    }
    
    def get_signatures(limit = 100, offset = 0, conditions = []){
        def params = extract_query_params(conditions)
        params['limit']  = limit
        params['offset'] = offset
        
        def result = http.get(path:"/v2/signs.json",query:params,contentType:ContentType.JSON)
        
        return result
        
    }
    
    def count_signatures(conditions = []){
        def params = extract_query_params conditions
        
        def result = http.get(path:"/v2/signs/count.json",query:params,contentType:ContentType.JSON)
        
        return result
        
    }
    
    def get_signature_document(signature_id, document_id){
        def result = http.get(path:"/v2/signs/${signature_id}/documents/${document_id}.json",contentType:ContentType.JSON)
        
        return result        
    }
    
    def get_signature_documents(signature_id){
        
        def result = http.get(path:"/v2/signs/${signature_id}/documents.json",contentType:ContentType.JSON)
        
        return result        
    }
    
    
    def download_audit_trail(signature_id, document_id, path){                
        http.get(path:"/v2/signs/${signature_id}/documents/${document_id}/download/signed",contentType:ContentType.BINARY){resp, reader ->                        
             if (resp.statusLine.statusCode == 200 && resp.headers.'Content-Type'.equalsIgnoreCase("application/pdf")){
                 def file = new File(path).withOutputStream{ out ->
                     out << reader;
                 }
             }else{
                String message = new String (reader.bytes,Consts.UTF_8)
                throw new Exception(message)   
             }
        }                
        return null
    }
    
    def download_signed_document(signature_id, document_id, path){        
       
        http.get(path:"/v2/signs/${signature_id}/documents/${document_id}/download/doc_proof",contentType:ContentType.BINARY){resp, reader ->                        
             if (resp.statusLine.statusCode == 200 && resp.headers.'Content-Type'.equalsIgnoreCase("application/pdf")){
                 def file = new File(path).withOutputStream{ out ->
                     out << reader;
                 }
             }else{
                String message = new String (reader.bytes,Consts.UTF_8)
                throw new Exception(message)   
             }
        }
        return null
    }
    
    def create_signature(filepath=[], recipients=[], params = []){
        params["recipients"]=[]
        
        [recipients].flatten().eachWithIndex{recipient, index->
            params["recipients"][index] = recipient
        }
        if (filepath && filepath.length()>0){
            params["files"] = [filepath].flatten().collect{ new File(it) }
        }
        
        if (params["templates"]){
            params["templates"] = [params["templates"]].flatten()
        }
                 
        http.encoderRegistry = new EncoderRegistry( charset: 'utf-8' )
        def result = http.request(POST){ multipartRequest ->
            uri.path="/v2/signs.json"
            
            requestContentType: "multipart/form-data;charset=UTF-8" 
            multipartRequest.entity = buildMultipartFromParams(params)
        }
        return result        
    }
    
    def cancel_signature(signature_id){
        def result = http.request( PATCH ) {
            uri.path = "/v2/signs/${signature_id}/cancel.json"
            requestContentType = ContentType.JSON            
        }
        return result        
    }
    
    def send_signature_reminder(signature_id,document_id){
        def result = http.post(path:"/v2/signs/${signature_id}/documents/${document_id}/reminder.json",requestContentType:ContentType.JSON)
        
        return result        
    }
    
    
    def get_branding(branding_id){
        def result = http.get(path:"/v2/brandings/${branding_id}.json",contentType:ContentType.JSON)
        
        return result        
    }
    
    
    def get_brandings(){
        def result = http.get(path:"/v2/brandings.json",contentType:ContentType.JSON)
        
        return result        
    }
    
    
    def create_branding(params){
        def data = JsonOutput.toJson(params)
        def result = http.post(path:"/v2/brandings.json",body:data,requestContentType:ContentType.JSON)
        
        return result        
    }
    
    
    def update_branding(branding_id, params){
        def data = JsonOutput.toJson(params)
        
        def result = http.request( PATCH ) {
            uri.path = "/v2/brandings/${branding_id}.json"
            requestContentType = ContentType.JSON
            body =  data
        }
        return result    
    }
    
    
    def update_branding_logo(branding_id, filepath){
        def file = new File(filepath)
        def result = http.request( PUT ) {
            uri.path = "/v2/brandings/${branding_id}/logo.json"
            requestContentType = ContentType.BINARY
            body =  file.bytes
        }
        
        return result
    }
    
    
    def update_branding_email(branding_id, template, filepath){
        def file = new File(filepath)
        http.headers.Accept = "*/*"
        def result = http.request( PUT ) {
            uri.path = "/v2/brandings/${branding_id}/emails/${template}.json"
            requestContentType = ContentType.HTML
            body =  file.text            
        }
        http.headers.Accept = "application/json;charset=UTF-8"
       return result
    }
    
    def get_templates(limit = 100, offset = 0){
        def params = [:]
        params['limit']  = limit
        params['offset'] = offset
        
        def result = http.get(path:"/v2/templates.json",query:params,contentType:ContentType.JSON)
        
        return result        
    }
    
    def count_emails(conditions = []){
        def params = extract_query_params(conditions)
        def result = http.get(path:"/v3/emails/count.json",query:params,contentType:ContentType.JSON)
        
        return result        
    }
    
    def get_email(email_id){
        def result = http.get(path:"/v3/emails/${email_id}.json",contentType:ContentType.JSON)
                
        return result        
    }
    
    def get_email_certificates(email_id){
        def result = http.get(path:"/v3/emails/${email_id}/certificates.json",contentType:ContentType.JSON)
        
        return result       
    }
    
    def get_email_certificate(email_id, certificate_id){
        def result = http.get(path:"/v3/emails/${email_id}/certificates/${certificate_id}.json",contentType:ContentType.JSON)
        
        return result        
    }
    
    def create_email(files, recipients, subject, body, params = []){
        
        params["recipients"]=[]
        
        [recipients].flatten().eachWithIndex{recipient, index->
            params["recipients"][index] = recipient
        }
        if (filepath && filepath.length()>0){
            params["files"] = [filepath].flatten().collect{ new File(it) }
        }
        
        params["subject"] = subject
        params["body"] = body
        
                 
        http.encoderRegistry = new EncoderRegistry( charset: 'utf-8' )
        def result = http.request(POST){ multipartRequest ->
            uri.path="/v3/signs.json"
            
            requestContentType: "multipart/form-data;charset=UTF-8"
            multipartRequest.entity = buildMultipartFromParams(params)
        }
        return result      
    }
    
    def download_email_audit_trail(email_id, certificate_id, path){
        http.get(path:"/v3/emails/${email_id}/certificates/${certificate_id}/download/audit_trail",contentType:ContentType.BINARY){resp, reader ->
            if (resp.statusLine.statusCode == 200 && resp.headers.'Content-Type'.equalsIgnoreCase("application/pdf")){
                def file = new File(path).withOutputStream{ out ->
                    out << reader;
                }
            }else{
               String message = new String (reader.bytes,Consts.UTF_8)
               return message
            }
       }
       return null        
    }
    
    def download_email_original_file(email_id, certificate_id, path){
        http.get(path:"/v3/emails/${email_id}/certificates/${certificate_id}/download/original",contentType:ContentType.BINARY){resp, reader ->
            if (resp.statusLine.statusCode == 200 && resp.headers.'Content-Type'.equalsIgnoreCase("application/pdf")){
                def file = new File(path).withOutputStream{ out ->
                    out << reader;
                }
            }else{
               String message = new String (reader.bytes,Consts.UTF_8)
               return message
            }
        }        
        return null
    }
    
    private def extract_query_params(conditions){
        def params = [:]
        conditions.each{
            def key = it.key
            def value = it.value
            
            if (key == 'data'){
                value.each {dataItem_key, dataItem_value ->
                    params[key]=["${dataItem_key}":dataItem_value]
                }
            }else{
                if (key == 'ids'){
                    value = value.join(',')
                }
                
                params[key]=value
            }
        }
        return params
    }
    
    private def buildMultipartFromParams(params){
        org.apache.http.entity.ContentType textInUTF8 = org.apache.http.entity.ContentType.create("text/plain", Consts.UTF_8)
        MultipartEntityBuilder multipartRequestEntity = new MultipartEntityBuilder()
        
        params.files?.eachWithIndex{ file,index ->
            log.debug "name: files[${index}] -> ${file}"            
            multipartRequestEntity.addPart("files[${index}]", new InputStreamBody(file.newInputStream(), new MimetypesFileTypeMap().getContentType(file), file.name))            
        }
        
        
        params.recipients?.eachWithIndex{ r,index ->
            log.debug "email: recipients[${index}][email] -> ${r.email}"
            log.debug "fullname: recipients[${index}][fullname] -> ${r.fullname}"
            multipartRequestEntity.addPart("recipients[${index}][email]", new StringBody(r.email,textInUTF8))
            multipartRequestEntity.addPart("recipients[${index}][fullname]", new StringBody(r.fullname,textInUTF8))
        }
         
        params.remove('files')
        params.remove('recipients')
        
        log.debug params
        params.eachWithIndex{param,index ->
            log.debug param.value.getClass()
            if (param.value instanceof ArrayList){
                param.value.eachWithIndex{innerparam,innerindex ->
                    log.debug "${param.key}[${innerindex}]···${innerparam}"
                    multipartRequestEntity.addPart("${param.key}[${innerindex}]", new StringBody(innerparam?.toString(),textInUTF8))
                }
            }else if (param.value instanceof LinkedHashMap){
                param.value.eachWithIndex{innerparam,innerindex ->
                    log.debug "${param.key}[${innerparam.key}]···${innerparam.value}"
                    multipartRequestEntity.addPart("${param.key}[${innerparam.key}]", new StringBody(innerparam?.value?.toString(),textInUTF8))
                }
            }else{
                multipartRequestEntity.addPart("${param.key}", new StringBody(param?.value,textInUTF8))
                log.debug "${param.key}#${param.value}"
            }
        }
        return multipartRequestEntity.build()
    }
}
