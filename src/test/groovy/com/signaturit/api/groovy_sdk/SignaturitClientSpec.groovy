package com.signaturit.api.groovy_sdk

/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class SignaturitClientSpec extends spock.lang.Specification {
    
    static final VALID_TOKEN = "put_a_valid_token_here_before_run_your_tests"
    static final INVALID_TOKEN = "NNNNNNNNNNTTTTTNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN"
    
    static final RECIPIENTS = [[fullname:"PUT_YOUR_NAME_HERE", email:"nacho.orme@gmail.com"]]
    
    static final SIGNATURE_ID = "ce3f8a48-8f9c-11e5-a4b9-0a0f5351f1ad"
    static final DOC_ID = "ce5b7c1d-8f9c-11e5-a4b9-0a0f5351f1ad"
    static final ERRONEOUS_ID = "0b1db805-0b1db805-0b1db805-0b1db805"
    
    
    def "get account with invalid credentials"() {
        when:
            SignaturitClient client = new SignaturitClient(INVALID_TOKEN)
            def result = client.get_account()
        then:
            result != null
            result.error == "invalid_grant"
            result.error_description != null
    }
    
    def "get account with valid credentials"(){
        when:        
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            def result = client.get_account()
        then:
            result.id != null
            result.created_at != null
    }
    
   
    def "get brandings"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            def result = client.get_brandings()
        then:
            result != null
            result.size()>0
            result[0].id != null
    }
    
    def "get branding invalid id"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            def result = client.get_branding(ERRONEOUS_ID)
        then:
            result != null
            result.status_code == 404
            result.message.contains("not found")
    }
    
    def "get branding valid id"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            def brandings = client.get_brandings()
            def result = client.get_branding(brandings[0].id)
        then:
            result.id == brandings[0].id
            result.subject_tag.contains("signaturit")
    }
    
    def "create a branding"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            def params =     [primary:"1",
                subject_tag : "signaturit_developer",
                corporate_layout_color : "#FAAC58",
                corporate_text_color : "#B43104",
                application_texts : [
                    "sign_button": "Sign"
                ],
                reminders : [1200, 4500],
                expire_time : "56000",
                callback_url : "http://testingapi.dev/thanks",
                events_url : "http://testingapi.dev/parse",
                signature_pos_x : [0.5],
                signature_pos_y : [0.3],
                terms_and_conditions_label : "Please, accept this terms.",
                terms_and_conditions_text : "I will accept all this terms for testing purposes"]
            def result = client.create_branding(params)
        then:
            result.id != null
            result.subject_tag == params.subject_tag
            result.events_url == params.events_url
            result.signature_pos_x == params.position_x
            result.signature_pos_y == params.position_y
            result.reminders == params.reminders
            result.created_at != null
    }
     
    def "update branding"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            def params =     [
                subject_tag : "signaturit_developer_updater20",
                application_texts: ['button.send': 'Sign it again!']
                ]           
            def brandings = client.get_brandings()
            def result = client.update_branding(brandings[0].id,params)
        then:
            result.id == brandings[0].id   
            result.subject_tag == params.subject_tag
    }
 
    def "update branding logo"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            
            def file = this.getClass().getResource("/signaturit-reverse.png")  
            def brandings = client.get_brandings()
            def result = client.update_branding_logo(brandings[0].id,file.path)
        then:
            result.id == brandings[0].id   
    }
   
    def "update branding template"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)            
            def file = this.getClass().getResource("/document.html")
            def brandings = client.get_brandings()
            def result = client.update_branding_email(brandings[0].id, 'sign_request', file.path)
        then:
            result.id == brandings[0].id
    }
    
    def "get templates"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)           
            def result = client.get_templates()
        then:
            result != null
            result[0] != null
            result[0].name == "#demo"
    }
    
    def "count signature requests"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
           
            def result = client.count_signatures()
        then:
            result != null
            Integer.parseInt(result.count) >0            
    }
    
    def "get signature requests"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            def result = client.get_signatures()
        then:
            result != null
            result.size() >0
    }
    
    def "get signature requests limit"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            def result = client.get_signatures(2,0,[])
        then:
            result != null
            result.size() == 2
    }
    
    def "get signature requests limit and offset"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            def firstSignatureRequest = client.get_signatures(1,0,[])
            def secondSignatureRequest = client.get_signatures(1,1,[])
        then:
            firstSignatureRequest != null
            firstSignatureRequest.size() == 1
            secondSignatureRequest != null
            secondSignatureRequest.size() == 1
            firstSignatureRequest[0].id != secondSignatureRequest[0].id
    }
    def "get signature requests custom attribute"(){
        when:
            SignaturitClient client = new SignaturitClient(VALID_TOKEN)
            def result = client.get_signatures(1,0,[data:[crm_id:2445]])            
        then:
            result != null
            result.size() == 0            
    }
    
     def "get signature with a valid signature_id"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)          
             def result = client.get_signature(SIGNATURE_ID)
         then:
             result.id == SIGNATURE_ID
            
     }
     
    
     def "get signature with an invalid signature_id"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def result = client.get_signature(ERRONEOUS_ID)
         then:
             result != null
            result.status_code == 404
            result.message.contains("not found")
     }
    
     def "get signature documents"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def result = client.get_signature_documents(SIGNATURE_ID)
         then:
             result != null
             result.id != null
             result.file.id != null
     }
     
     def "create signature request"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def file = this.getClass().getResource("/fourpages.pdf")            
             def params = [subject:"Please, sign this document", 
                           body:"Hey! Please sign this, in order to test our integration.",
                           mandatory_pages:[[3,4]],
                           mandatory_photo:[1],
                           mandatory_voice:[1]]
             def result = client.create_signature(file.path,RECIPIENTS,params)
         then:
             result != null
             result.id != null
             result.jobs.id != null
             result.body == params.body
             result.subject == params.subject
             result.jobs[0].signer_email == RECIPIENTS[0].email
             result.jobs[0].signer_name == RECIPIENTS[0].fullname
             result.mandatory_voice == params.mandatory_voice
     }
     
     def "create signature request using template"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)                 
             def params = [subject:"Please, sign this document", 
                           body:"Hey! Please sign this, in order to test our integration.",
                           templates: ["#demo"]]
             def result = client.create_signature(null,RECIPIENTS,params)
         then:
             result != null
             result.id != null
             result.jobs[0].id != null
             result.body == params.body
             result.subject == params.subject
             result.jobs[0].template.name == params.templates[0]             
     }
     
     def "create signature request using template and fields filled"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def params = [  subject:"Please, sign this document", 
                             body:"Hey! Please sign this, in order to test our integration.",
                             templates: ["#demo"],
                             data:['textfield_01':'Barcelona','textfield_02':'Signaturit']]
             def result = client.create_signature(null,RECIPIENTS,params)
         then:
             result != null
             result.id != null
             result.jobs[0].id != null
             result.body == params.body
             result.subject == params.subject
             result.jobs[0].template.name == params.templates[0]
             params.data.keySet().any{ it == result.data_info.key[0]}
     }
     
     def "create signature request using template and custom data"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def params = [  subject:"Please, sign this document", 
                             body:"Hey! Please sign this, in order to test our integration.",
                             templates: ["#demo"],
                             data:['crm_id':'2455']]
             def result = client.create_signature(null,RECIPIENTS,params)
         then:
             result != null
             result.id != null
             result.jobs[0].id != null
             result.body == params.body
             result.subject == params.subject
             result.jobs[0].template.name == params.templates[0]
             params.data.keySet().any{ it == result.data_info.key[0]}
     }
     
     def "download audit trail with invalid signature id"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def filepath = '/tmp/audittrail4.pdf'
             def result = client.download_audit_trail(SIGNATURE_ID,ERRONEOUS_ID,filepath)
         then:
             def exception = thrown(Exception)      
     }
     
     def "download audit trail with valid signature id"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def filepath = '/tmp/audittrail4.pdf'
             def result = client.download_audit_trail(SIGNATURE_ID,DOC_ID,filepath)
         then:
             result == null
             new File(filepath).exists()
     }
     
     def "download signed document with invalid signature id"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def filepath = '/tmp/download_signed_document1.pdf'
             def result = client.download_signed_document(ERRONEOUS_ID,DOC_ID,filepath)
         then:
             def exception = thrown(Exception)
     }
     
     def "download signed document with valid signature id"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def filepath = '/tmp/download_signed_document2.pdf'
             def result = client.download_signed_document(SIGNATURE_ID,DOC_ID,filepath)
         then:
             result == null
             new File(filepath).exists()
     }
     
     def "cancel signature request"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def pendingSignature = client.get_signatures(1,0,[status:'READY'])
             def pending_signature_id = pendingSignature.id[0]             
             def result = client.cancel_signature(pending_signature_id)
         then:
             result != null
             result.jobs[0].status == "CANCELED"             
     }
     
     def "cancel invalid signature request"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             
             def result = client.cancel_signature(ERRONEOUS_ID)
         then:
             result != null
             result.status_code == 404
             
     }
     def "get signature by status"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def result = client.get_signatures(1,0,[status:'READY'])
         then:
             result != null
             result.id != null
             result.jobs[0].status[0] == 'READY'
     }
     def "send reminder"(){
         when:
             SignaturitClient client = new SignaturitClient(VALID_TOKEN)
             def pendingSignature = client.get_signatures(1,0,[status:'READY'])
             def pending_signature_id = pendingSignature.id[0]
             def pending_doc_id = pendingSignature.jobs[0].id[0]             
             def result = client.send_signature_reminder(pending_signature_id,pending_doc_id)
         then:
             result != null
             result.id == pending_doc_id 
             result.sign.id == pending_signature_id      
     }
}