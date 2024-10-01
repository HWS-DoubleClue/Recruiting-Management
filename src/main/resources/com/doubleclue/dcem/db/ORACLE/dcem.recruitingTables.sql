
create table recruiting_candidate (
candidate_id number(10,0) generated as identity,
dc_address varchar2(255 char),
citizenship varchar2(255 char),
city varchar2(255 char),
dc_country varchar2(255 char),
country_state varchar2(255 char),
currency varchar2(15 char),
date_of_birth date,
email varchar2(255 char) not null,
experience varchar2(255 char),
first_name varchar2(255 char) not null,
gender number(10,0) not null,
job_title varchar2(255 char),
last_modified_on timestamp not null,
period_of_notice varchar2(255 char),
phone varchar2(255 char),
photo blob,
professional_qualification varchar2(255 char),
ranking float not null,
referral varchar2(255 char),
registered_on date not null,
remarks long,
dc_restricted number(1,0),
salary_request number(10,0),
school_graduation varchar2(255 char),
starting_on date,
surname varchar2(255 char) not null,
title varchar2(255 char),
zip_code varchar2(15 char),
classification number(10,0),
modified_by number(10,0),
recommended_by number(10,0),
recruiter_id number(10,0),
candidate_status number(10,0) not null,
primary key (candidate_id)
);

create table recruiting_candidate_status (
dc_id number(10,0) generated as identity,
deprecated number(1,0) not null,
dc_rank number(10,0) not null,
dc_status varchar2(128 char) not null,
primary key (dc_id)
);

create table recruiting_career_level (
dc_id number(10,0) generated as identity,
career_Level varchar2(255 char) not null,
primary key (dc_id)
);

create table recruiting_classification (
dc_id number(10,0) generated as identity,
dc_name varchar2(255 char) not null,
primary key (dc_id)
);

create table recruiting_feedback (
feedback_id number(10,0) generated as identity,
posted_on timestamp not null,
dc_rating number(10,0) not null,
dc_text long not null,
candidate_id number(10,0) not null,
candidate_status number(10,0) not null,
dc_user number(10,0) not null,
primary key (feedback_id)
);

create table recruiting_history (
dc_id number(10,0) generated as identity,
dc_information long not null,
operation number(10,0) not null,
object_reference number(10,0) not null,
dc_timestamp timestamp not null,
dc_type number(10,0) not null,
modified_by number(10,0) not null,
primary key (dc_id)
);

create table recruiting_recruiter (
dc_id number(10,0) generated as identity,
dc_address varchar2(255 char),
city varchar2(127 char),
company_name varchar2(255 char) not null,
company_url varchar2(255 char) not null,
contact_person varchar2(255 char) not null,
contract_information long,
dc_country varchar2(255 char) not null,
countryState varchar2(127 char),
email varchar2(255 char) not null,
last_modified_on timestamp not null,
phone varchar2(63 char) not null,
zip_code varchar2(15 char),
modified_by number(10,0),
primary key (dc_id)
);

create table recruiting_ref_branchlocation_vacancy (
vac_id number(10,0) not null,
dc_id number(10,0) not null,
primary key (vac_id, dc_id)
);

create table recruiting_ref_candidate_file (
candidate_id number(10,0) not null,
file_id number(10,0) not null
);

create table recruiting_ref_careerlevel_vacancy (
vac_id number(10,0) not null,
dc_id number(10,0) not null,
primary key (vac_id, dc_id)
);

create table recruiting_ref_recruiter_file (
recruiter_id number(10,0) not null,
file_id number(10,0) not null
);

create table recruiting_ref_skills_candidates (
candidate_id number(10,0) not null,
skills_level_id number(10,0) not null,
primary key (candidate_id, skills_level_id)
);

create table recruiting_ref_skills_vacancy (
vac_id number(10,0) not null,
skills_level_id number(10,0) not null,
primary key (vac_id, skills_level_id)
);

create table recruiting_ref_types_classification (
dc_id number(10,0) not null,
dc_types number(10,0) not null,
primary key (dc_id, dc_types)
);

create table recruiting_ref_vacancy_candidate (
vac_id number(10,0) not null,
candidate_id number(10,0) not null,
primary key (candidate_id, vac_id)
);

create table recruiting_ref_watchers_candidate (
candidate_id number(10,0) not null,
user_id number(10,0) not null
);

create table recruiting_ref_watchers_vacancy (
vac_id number(10,0) not null,
user_id number(10,0) not null
);

create table recruiting_vacancy (
vac_id number(10,0) generated as identity,
application_deadline date,
dc_archived number(1,0) not null,
currency varchar2(15 char),
description long not null,
locale number(10,0) not null,
title varchar2(511 char) not null,
hours_per_week number(10,0),
last_modified_on timestamp not null,
onboarding_remote_percent number(10,0),
vac_priority number(10,0) not null,
probation_time number(10,0),
remarks long,
remote_percent number(10,0),
replacement number(1,0) not null,
salary_max number(10,0),
salary_target number(10,0),
starting_from date not null,
total_candidates number(10,0),
vac_type number(10,0) not null,
classification number(10,0),
clerk number(10,0),
decision_maker number(10,0),
department_id number(19,0),
modified_by number(10,0),
vacancy_status number(10,0) not null,
primary key (vac_id)
);

create table recruiting_vacancy_language (
dc_id number(10,0) generated as identity,
description long not null,
locale number(10,0) not null,
title varchar2(511 char) not null,
vacancy_id number(10,0) not null,
primary key (dc_id)
);

create table recruiting_vacancy_status (
dc_id number(10,0) generated as identity,
deprecated number(1,0) not null,
locked_for_head_ofs number(1,0) not null,
published number(1,0) not null,
dc_rank number(10,0) not null,
dc_status varchar2(128 char) not null,
primary key (dc_id)
);

alter table recruiting_candidate_status
add constraint UK_CANDIDATE_STATUS unique (dc_status);

alter table recruiting_career_level
add constraint UK_CAREER_LEVEL unique (career_Level);

alter table recruiting_classification
add constraint UK_CLASS_NAME unique (dc_name);

alter table recruiting_feedback
add constraint UK_RECRUITING_FEEDBACK unique (dc_user, candidate_id);

alter table recruiting_recruiter
add constraint UK_RECRUITER_NAME unique (company_name);

alter table recruiting_vacancy_status
add constraint UK_VACANCY_STATUS unique (dc_status);

alter table recruiting_candidate
add constraint FK_CLASS_CANDIDATE
foreign key (classification)
references recruiting_classification;

alter table recruiting_candidate
add constraint FK_MODIFIED_CANDIDATE
foreign key (modified_by)
references core_user;

alter table recruiting_candidate
add constraint FK_REC_CANDIDATE
foreign key (recommended_by)
references core_user;

alter table recruiting_candidate
add constraint FK_RECRUITER_CANDIDATE
foreign key (recruiter_id)
references recruiting_recruiter;

alter table recruiting_candidate
add constraint FK_CLASS_CANDIDATE_STATUS
foreign key (candidate_status)
references recruiting_candidate_status;

alter table recruiting_feedback
add constraint FK_CANDIDATE_FEEDBACK
foreign key (candidate_id)
references recruiting_candidate;

alter table recruiting_feedback
add constraint FK_FEEDBACK_CANDIDATE_STATUS
foreign key (candidate_status)
references recruiting_candidate_status;

alter table recruiting_feedback
add constraint FK_USER_FEEDBACK
foreign key (dc_user)
references core_user;

alter table recruiting_history
add constraint FK_MODIFIED_HISTORY
foreign key (modified_by)
references core_user;

alter table recruiting_recruiter
add constraint FK_MODIFIED_RECRUITER
foreign key (modified_by)
references core_user;

alter table recruiting_ref_branchlocation_vacancy
add constraint FK_VACANCY_BRANCHLOCATION
foreign key (dc_id)
references core_branch_location;

alter table recruiting_ref_branchlocation_vacancy
add constraint FK_BRANCHLOCATION_VACANCY
foreign key (vac_id)
references recruiting_vacancy;

alter table recruiting_ref_candidate_file
add constraint FK_CANDIDATE_FILES
foreign key (file_id)
references as_cloudsafe;

alter table recruiting_ref_candidate_file
add constraint FK_FILES_CANDIDATE
foreign key (candidate_id)
references recruiting_candidate;

alter table recruiting_ref_careerlevel_vacancy
add constraint FK_VACANCY_CAREERLEVEL
foreign key (dc_id)
references recruiting_career_level;

alter table recruiting_ref_careerlevel_vacancy
add constraint FK_CAREERLEVEL_VACANCY
foreign key (vac_id)
references recruiting_vacancy;

alter table recruiting_ref_recruiter_file
add constraint FK_RECRUITER_FILE
foreign key (file_id)
references as_cloudsafe;

alter table recruiting_ref_recruiter_file
add constraint FK_FILE_RECRUITER
foreign key (recruiter_id)
references recruiting_recruiter;

alter table recruiting_ref_skills_candidates
add constraint FK_CANDIDATE_SKILLS
foreign key (skills_level_id)
references skills_skills_level;

alter table recruiting_ref_skills_candidates
add constraint FK_SKILLS_CANDIDATE
foreign key (candidate_id)
references recruiting_candidate;

alter table recruiting_ref_skills_vacancy
add constraint FK_VACANCY_SKILLS
foreign key (skills_level_id)
references skills_skills_level;

alter table recruiting_ref_skills_vacancy
add constraint FK_SKILLS_VACANCY
foreign key (vac_id)
references recruiting_vacancy;

alter table recruiting_ref_types_classification
add constraint FK1qegq868nbiqx07x1vmebvlhj
foreign key (dc_id)
references recruiting_classification;

alter table recruiting_ref_vacancy_candidate
add constraint FK_VACANCY_CANDIDATE
foreign key (candidate_id)
references recruiting_candidate;

alter table recruiting_ref_vacancy_candidate
add constraint FK_CANDIDATE_VACANCY
foreign key (vac_id)
references recruiting_vacancy;

alter table recruiting_ref_watchers_candidate
add constraint FK_CANDIDATE_WATCHERS
foreign key (user_id)
references core_user;

alter table recruiting_ref_watchers_candidate
add constraint FK_WATCHERS_CANDIDATE
foreign key (candidate_id)
references recruiting_candidate;

alter table recruiting_ref_watchers_vacancy
add constraint FK_VACANCY_WATCHERS
foreign key (user_id)
references core_user;

alter table recruiting_ref_watchers_vacancy
add constraint FK_WATCHERS_VACANCY
foreign key (vac_id)
references recruiting_vacancy;

alter table recruiting_vacancy
add constraint FK_CLASS_VACANCY
foreign key (classification)
references recruiting_classification;

alter table recruiting_vacancy
add constraint FK_CLERK_VACANCY
foreign key (clerk)
references core_user;

alter table recruiting_vacancy
add constraint FK_DECISIONMAKER_VACANCY
foreign key (decision_maker)
references core_user;

alter table recruiting_vacancy
add constraint FK_DEPT_VACANCY
foreign key (department_id)
references core_department;

alter table recruiting_vacancy
add constraint FK_MODIFIED_VACANCY
foreign key (modified_by)
references core_user;

alter table recruiting_vacancy
add constraint FK_VACANCY_STATUS
foreign key (vacancy_status)
references recruiting_vacancy_status;

alter table recruiting_vacancy_language
add constraint FK_VACANCY_LANGUAGE
foreign key (vacancy_id)
references recruiting_vacancy;
