# Recruiting

Recruiting is a webtool to track recruiting processes and manage vacancies, applicants as well as recruiters your company is working with. HR Managers and Department Heads can register and update open vacancies, define the skills needed for the open position and record administrative information like starting date and budget. Once applications for a vacancy have come in, they are gathered in a central location. Recruiting provides you with all the tools necessary to track the recruiting process of the applicants, rate and quickly compare the candidates for each position.
Recruiting runs as a Plugin Module in the DoubleClue Enterprise Management (DCEM). You can find the DCEM repository at https://github.com/HWS-DoubleClue/IAM-Password-Manager. It further is dependent on the DoubleClue Skills Module: https://github.com/HWS-DoubleClue/Skills-Management 
For further details, have a look at the Recruiting Manual.

## Features

### Vacancies
Add free vacancies in your company. In a detailed form you can gather and easily update all administrative information regarding the position like the budgeted salary, starting date and decision makers. You can further add a skill profile to define skills required for this position as well as nice-to-have skills which would be beneficial for a candidate to have but are not mandatory. The selection of different vacancy statuses allows to keep track in which step of the job advertisement and recruiting process currently is. It also offers a comprehensive view of all candidates which have applied to a position including the rating they have received so far.

### Candidates
Register your applicants as candidates and match them with different vacancies. You can gather all information about the applicant, including their skills, availability, and salary, in one central location and even upload documents like CVs and cover letters. For your convenience, all those information can be exported as a PDF. You can further define various statuses for candidates, to 

### Candidate Feedback
Recuriting offers a rating feature, in which decision makers involved in the hiring process can share their opinions on a candidate. It also offers a comprehensive view in which all candidates who have applied for a position and their ratings are shown together.

### Recruiters
If you cooperate with headhunters and recruiters to fill your open positions, you can add those in their separate section of Recruiting. After registering a recruiter, you can add additional information on your cooperation and even upload documents. If an applicant has been forwarded by a recruiter, you can select this recruiter in their profile.

### Automatic E-Mail Notifications
To track updates to a vacancy or candidate, users can be added as watchers and will then receive an e-mail notification each time a vacancy or candidate is updated. Department heads are watchers by default.

## [Try it out for free](https://doubleclue.online/dcem/createTenant/index.xhtml)
On Premises or in the Cloud
You can install the solution in a DCEM on premises or as a "Software As A Service" in our DoubleClue cloud.
Build
You can download the latest release version of the Skills module from GitHub.
If you prefer to build a snapshot version yourself, follow these steps:
•	Check out DCEM at https://github.com/HWS-DoubleClue/IAM-Password-Manager
•	Check out the Skills module
•	Check out the Recruiting module
•	Build DCEM in an IDE of your choice
•	Execute Maven with clean package
•	The output is a jar file in the target folder

### Install on Premises
The installation of the Skills-Module is quick and easy. Simply copy the SkillsModule.jar to the subfolder "plugins" in your DCEM folder and restart DCEM. You would also need to acquire a license key from the Doubleclue support.
•	Contact: support@doubleclue.com
For more information, check our detailed instruction at https://github.com/HWS-DoubleClue/IAM-Password-Manager/blob/master/Documents/InstallPluginModule.odt or have a look at the the [DoubleClue Manual](https://doubleclue.com/wp-content/uploads/DCEM_Manual_EN.pdf) Chapter 20: DoubleClue Plugin Modules.
