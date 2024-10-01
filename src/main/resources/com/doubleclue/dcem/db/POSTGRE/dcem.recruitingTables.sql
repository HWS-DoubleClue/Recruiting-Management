
create table recruiting_candidate (
candidate_id  serial not null,
dc_address varchar(255),
citizenship varchar(255),
city varchar(255),
dc_country varchar(255),
country_state varchar(255),
currency varchar(15),
date_of_birth date,
email varchar(255) not null,
experience varchar(255),
first_name varchar(255) not null,
gender int4 not null,
job_title varchar(255),
last_modified_on timestamp not null,
period_of_notice varchar(255),
phone varchar(255),
photo bytea,
professional_qualification varchar(255),
ranking float4 not null,
referral varchar(255),
registered_on date not null,
remarks varchar(1048576),
dc_restricted boolean,
salary_request int4,
school_graduation varchar(255),
starting_on date,
surname varchar(255) not null,
title varchar(255),
zip_code varchar(15),
classification int4,
modified_by int4,
recommended_by int4,
recruiter_id int4,
candidate_status int4 not null,
primary key (candidate_id)
);

create table recruiting_candidate_status (
dc_id  serial not null,
deprecated boolean not null,
dc_rank int4 not null,
dc_status varchar(128) not null,
primary key (dc_id)
);

create table recruiting_career_level (
dc_id  serial not null,
career_Level varchar(255) not null,
primary key (dc_id)
);

create table recruiting_classification (
dc_id  serial not null,
dc_name varchar(255) not null,
primary key (dc_id)
);

create table recruiting_feedback (
feedback_id  serial not null,
posted_on timestamp not null,
dc_rating int4 not null,
dc_text varchar(4096) not null,
candidate_id int4 not null,
candidate_status int4 not null,
dc_user int4 not null,
primary key (feedback_id)
);

create table recruiting_history (
dc_id  serial not null,
dc_information varchar(4096) not null,
operation int4 not null,
object_reference int4 not null,
dc_timestamp timestamp not null,
dc_type int4 not null,
modified_by int4 not null,
primary key (dc_id)
);

create table recruiting_recruiter (
dc_id  serial not null,
dc_address varchar(255),
city varchar(127),
company_name varchar(255) not null,
company_url varchar(255) not null,
contact_person varchar(255) not null,
contract_information varchar(1048576),
dc_country varchar(255) not null,
countryState varchar(127),
email varchar(255) not null,
last_modified_on timestamp not null,
phone varchar(63) not null,
zip_code varchar(15),
modified_by int4,
primary key (dc_id)
);

create table recruiting_ref_branchlocation_vacancy (
vac_id int4 not null,
dc_id int4 not null,
primary key (vac_id, dc_id)
);

create table recruiting_ref_candidate_file (
candidate_id int4 not null,
file_id int4 not null
);

create table recruiting_ref_careerlevel_vacancy (
vac_id int4 not null,
dc_id int4 not null,
primary key (vac_id, dc_id)
);

create table recruiting_ref_recruiter_file (
recruiter_id int4 not null,
file_id int4 not null
);

create table recruiting_ref_skills_candidates (
candidate_id int4 not null,
skills_level_id int4 not null,
primary key (candidate_id, skills_level_id)
);

create table recruiting_ref_skills_vacancy (
vac_id int4 not null,
skills_level_id int4 not null,
primary key (vac_id, skills_level_id)
);

create table recruiting_ref_types_classification (
dc_id int4 not null,
dc_types int4 not null,
primary key (dc_id, dc_types)
);

create table recruiting_ref_vacancy_candidate (
vac_id int4 not null,
candidate_id int4 not null,
primary key (candidate_id, vac_id)
);

create table recruiting_ref_watchers_candidate (
candidate_id int4 not null,
user_id int4 not null
);

create table recruiting_ref_watchers_vacancy (
vac_id int4 not null,
user_id int4 not null
);

create table recruiting_vacancy (
vac_id  serial not null,
application_deadline date,
dc_archived boolean not null,
currency varchar(15),
description varchar(1048576) not null,
locale int4 not null,
title varchar(511) not null,
hours_per_week int4,
last_modified_on timestamp not null,
onboarding_remote_percent int4,
vac_priority int4 not null,
probation_time int4,
remarks varchar(1048576),
remote_percent int4,
replacement boolean not null,
salary_max int4,
salary_target int4,
starting_from date not null,
total_candidates int4,
vac_type int4 not null,
classification int4,
clerk int4,
decision_maker int4,
department_id int8,
modified_by int4,
vacancy_status int4 not null,
primary key (vac_id)
);

create table recruiting_vacancy_language (
dc_id  serial not null,
description varchar(1048576) not null,
locale int4 not null,
title varchar(511) not null,
vacancy_id int4 not null,
primary key (dc_id)
);

create table recruiting_vacancy_status (
dc_id  serial not null,
deprecated boolean not null,
locked_for_head_ofs boolean not null,
published boolean not null,
dc_rank int4 not null,
dc_status varchar(128) not null,
primary key (dc_id)
);

alter table if exists recruiting_candidate_status
add constraint UK_CANDIDATE_STATUS unique (dc_status);

alter table if exists recruiting_career_level
add constraint UK_CAREER_LEVEL unique (career_Level);

alter table if exists recruiting_classification
add constraint UK_CLASS_NAME unique (dc_name);

alter table if exists recruiting_feedback
add constraint UK_RECRUITING_FEEDBACK unique (dc_user, candidate_id);

alter table if exists recruiting_recruiter
add constraint UK_RECRUITER_NAME unique (company_name);

alter table if exists recruiting_vacancy_status
add constraint UK_VACANCY_STATUS unique (dc_status);

alter table if exists recruiting_candidate
add constraint FK_CLASS_CANDIDATE
foreign key (classification)
references recruiting_classification;

alter table if exists recruiting_candidate
add constraint FK_MODIFIED_CANDIDATE
foreign key (modified_by)
references core_user;

alter table if exists recruiting_candidate
add constraint FK_REC_CANDIDATE
foreign key (recommended_by)
references core_user;

alter table if exists recruiting_candidate
add constraint FK_RECRUITER_CANDIDATE
foreign key (recruiter_id)
references recruiting_recruiter;

alter table if exists recruiting_candidate
add constraint FK_CLASS_CANDIDATE_STATUS
foreign key (candidate_status)
references recruiting_candidate_status;

alter table if exists recruiting_feedback
add constraint FK_CANDIDATE_FEEDBACK
foreign key (candidate_id)
references recruiting_candidate;

alter table if exists recruiting_feedback
add constraint FK_FEEDBACK_CANDIDATE_STATUS
foreign key (candidate_status)
references recruiting_candidate_status;

alter table if exists recruiting_feedback
add constraint FK_USER_FEEDBACK
foreign key (dc_user)
references core_user;

alter table if exists recruiting_history
add constraint FK_MODIFIED_HISTORY
foreign key (modified_by)
references core_user;

alter table if exists recruiting_recruiter
add constraint FK_MODIFIED_RECRUITER
foreign key (modified_by)
references core_user;

alter table if exists recruiting_ref_branchlocation_vacancy
add constraint FK_VACANCY_BRANCHLOCATION
foreign key (dc_id)
references core_branch_location;

alter table if exists recruiting_ref_branchlocation_vacancy
add constraint FK_BRANCHLOCATION_VACANCY
foreign key (vac_id)
references recruiting_vacancy;

alter table if exists recruiting_ref_candidate_file
add constraint FK_CANDIDATE_FILES
foreign key (file_id)
references as_cloudsafe;

alter table if exists recruiting_ref_candidate_file
add constraint FK_FILES_CANDIDATE
foreign key (candidate_id)
references recruiting_candidate;

alter table if exists recruiting_ref_careerlevel_vacancy
add constraint FK_VACANCY_CAREERLEVEL
foreign key (dc_id)
references recruiting_career_level;

alter table if exists recruiting_ref_careerlevel_vacancy
add constraint FK_CAREERLEVEL_VACANCY
foreign key (vac_id)
references recruiting_vacancy;

alter table if exists recruiting_ref_recruiter_file
add constraint FK_RECRUITER_FILE
foreign key (file_id)
references as_cloudsafe;

alter table if exists recruiting_ref_recruiter_file
add constraint FK_FILE_RECRUITER
foreign key (recruiter_id)
references recruiting_recruiter;

alter table if exists recruiting_ref_skills_candidates
add constraint FK_CANDIDATE_SKILLS
foreign key (skills_level_id)
references skills_skills_level;

alter table if exists recruiting_ref_skills_candidates
add constraint FK_SKILLS_CANDIDATE
foreign key (candidate_id)
references recruiting_candidate;

alter table if exists recruiting_ref_skills_vacancy
add constraint FK_VACANCY_SKILLS
foreign key (skills_level_id)
references skills_skills_level;

alter table if exists recruiting_ref_skills_vacancy
add constraint FK_SKILLS_VACANCY
foreign key (vac_id)
references recruiting_vacancy;

alter table if exists recruiting_ref_types_classification
add constraint FK1qegq868nbiqx07x1vmebvlhj
foreign key (dc_id)
references recruiting_classification;

alter table if exists recruiting_ref_vacancy_candidate
add constraint FK_VACANCY_CANDIDATE
foreign key (candidate_id)
references recruiting_candidate;

alter table if exists recruiting_ref_vacancy_candidate
add constraint FK_CANDIDATE_VACANCY
foreign key (vac_id)
references recruiting_vacancy;

alter table if exists recruiting_ref_watchers_candidate
add constraint FK_CANDIDATE_WATCHERS
foreign key (user_id)
references core_user;

alter table if exists recruiting_ref_watchers_candidate
add constraint FK_WATCHERS_CANDIDATE
foreign key (candidate_id)
references recruiting_candidate;

alter table if exists recruiting_ref_watchers_vacancy
add constraint FK_VACANCY_WATCHERS
foreign key (user_id)
references core_user;

alter table if exists recruiting_ref_watchers_vacancy
add constraint FK_WATCHERS_VACANCY
foreign key (vac_id)
references recruiting_vacancy;

alter table if exists recruiting_vacancy
add constraint FK_CLASS_VACANCY
foreign key (classification)
references recruiting_classification;

alter table if exists recruiting_vacancy
add constraint FK_CLERK_VACANCY
foreign key (clerk)
references core_user;

alter table if exists recruiting_vacancy
add constraint FK_DECISIONMAKER_VACANCY
foreign key (decision_maker)
references core_user;

alter table if exists recruiting_vacancy
add constraint FK_DEPT_VACANCY
foreign key (department_id)
references core_department;

alter table if exists recruiting_vacancy
add constraint FK_MODIFIED_VACANCY
foreign key (modified_by)
references core_user;

alter table if exists recruiting_vacancy
add constraint FK_VACANCY_STATUS
foreign key (vacancy_status)
references recruiting_vacancy_status;

alter table if exists recruiting_vacancy_language
add constraint FK_VACANCY_LANGUAGE
foreign key (vacancy_id)
references recruiting_vacancy;
