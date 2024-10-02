# Recruiting Management Module

Recruiting-Management is an other module for DoubleClue Enterprise Management (DCEM) Framework. https://github.com/HWS-DoubleClue/IAM-Password-Manager

It manages:
- Vacancies
- Candidates
- Recruiters  

This module has dependency on an other DCEM Module [Skills](https://github.com/HWS-DoubleClue/Skills-Management)

The modules use all features of the DCEM framework, such as security, Multi-Factor Autentication (MFA), Access-Rights and many other features. 

** Give it a try at: https://doubleclue.online/dcem/createTenant/index.xhtml **
	
## Features

### Vacancies
Human-resources department, teamleads and head of departments may create and manage company vacancies. 
A vacancy has a lot of information, such as:
 - Title and Description (in different Langaugess)
 - Department (can be imported from AD or Azure AD)
 - Status (free configurable statuses)
 - Skills required
 - Skills nice to have
 - Watchers
 - others items such as, budgeted salary, decision makrers, location, start date, hours/week etc..

### Candidates
Candidates may be added manually by HR or through REST-API from a Web-Portal.
Candidates can apply to one or more Vacancies.
A candidate may have a lot if information, such as:
 - personal details including photo
 - status
 - skills
 - availability
 - request salar
 - several uploaded documents
 - others
 
#### Candidate Feedback
Teamleads and wathers can give feedback ratings to candidates. 

### Adaptive Access Rights 

HR personel may be give granular access rights to all entities. 
Teamleads or head of departments will may create new vacancies for their department respectivily, but are not allowed to publish or confirm the vacancies if not explicitily have access to do.
Also Teamleads and head of departments may always view and give feedback to candidates which applies for their vacanciey. 


### Recruiters

If you engage recruiters (headhunters) to find candidates for your vacancies, this module will also mange your recruites. If an applicant has been forwarded by a recruiter, you can select this recruiter in their profile.

### E-Mail Notifications
To track updates to a vacancy or candidate, users can be added as watchers and will then receive an e-mail notification each time a vacancy or candidate is updated. Head of departments are watchers by default.

## Try it out

### In the Cloud

Create a tenant in the DoubleClue Cloud and start using Recruiting-Management in a couple of minutes.
 
Go to: DoubleClue Cloud https://doubleclue.online/dcem/createTenant/index.xhtml

### On Premisis

Building and installing DoubleClue-Enterprise-Management with plugin-modules requires very good skills in JavaEE, Eclipse, maven, yajsw and other frameworks. 
For this reason we recommand you to contact support@doubleclue.com for further information and training.

