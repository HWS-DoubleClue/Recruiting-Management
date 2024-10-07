# Recruiting Management Module

Recruiting Management is a module for the DoubleClue Enterprise Management (DCEM) Framework https://github.com/HWS-DoubleClue/IAM-Password-Manager. 

It manages:
- Vacancies
- Candidates
- Recruiters  

The module is dependent on another DCEM module: [Skills](https://github.com/HWS-DoubleClue/Skills-Management)

The module benefits from all features of the DCEM framework, such as security, multi-factor autentication (MFA), access rights and many others.

**Give it a try at: https://doubleclue.online/dcem/createTenant/index.xhtml**
	
## Features

### Vacancies
Human resources department, teamleads and heads of departments may create and manage company vacancies. 
Information added to a vacancy includes:
 - Title and Description (in different Langaugess)
 - Department (can be imported from AD or Azure AD)
 - Status (free configurable statuses)
 - Skills required
 - Skills nice to have
 - Watchers
 - Others items such as, budgeted salary, decision makrers, location, start date, hours/week etc..

### Candidates
Candidates may be added manually by HR or through REST-API from a web portal.
Candidates can apply to one or more vacancies.
Information added to a candidate includes:
 - Personal details including photo
 - Status
 - Skills
 - Availability
 - Requested salary
 - Several uploaded documents
 - Others
 
#### Candidate Feedback
Teamleads and watchers can give feedback ratings to candidates. 

### Adaptive Access Rights 

HR personel can give granular access rights to all entities. 
Teamleads or heads of departments may create new vacancies for their department, but are not allowed to publish or confirm the vacancies if not explicitily given access to do so.
Also, teamleads and head of departments may always view and give feedback to candidates which applies for vacancies of their department. 


### Recruiters
If you cooperate with recruiters (headhunters) to find candidates for your vacancies, this module will also manage your recruiters. If an applicant has been forwarded by a recruiter, you can select this recruiter in their profile.

### E-Mail Notifications
To track updates to a vacancy or a candidate, users can be added as watchers and will then receive an e-mail notification each time a vacancy or a candidate is updated. Head of departments are watchers by default.

## Try it out

### In the Cloud

Create a tenant in the DoubleClue Cloud and start using Recruiting Management in a couple of minutes.
 
Go to: DoubleClue Cloud https://doubleclue.online/dcem/createTenant/index.xhtml

### On Premisis

Building and installing DoubleClue Enterprise Management with plugin modules requires adavanced experience with JavaEE, Eclipse, maven, yajsw and other frameworks. 
For this reason, we recommand to contact support@doubleclue.com for further information and training.

For further information check the Recruiting Manual at: https://doubleclue.com/files/DC_Recruiting_Manual_en.pdf

