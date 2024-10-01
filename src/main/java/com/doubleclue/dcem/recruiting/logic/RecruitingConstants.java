package com.doubleclue.dcem.recruiting.logic;

import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;

public class RecruitingConstants {
	public static final String ENTITY_CANDIDATE = "Candidate";
	public static final String ENTITY_VACANCY = "Vacancy";

	public static final String VACANCY_ID_KEY = "vacancyKey";
	public static final String VACANCY_DISPLAY_TEMPLATE = RecruitingModule.MODULE_ID + ".vacancy.display";
	public static final String CANDIDATE_DISPLAY_TEMPLATE = RecruitingModule.MODULE_ID + ".candidate.display";
	public static final String VACANCY_WATCHER_TEMPLATE = RecruitingModule.MODULE_ID + ".vacancy.watcher";
	public static final String EMAIL_VACANCY_MODIFIED_SUBJECT_BUNDLE_KEY = RecruitingModule.MODULE_ID + ".vacancy.subjectWatcher";
	public static final String CANDIDATE_WATCHER_TEMPLATE = RecruitingModule.MODULE_ID + ".candidate.watcher";
	public static final String EMAIL_CANDIDATE_MODIFIED_SUBJECT_BUNDLE_KEY = RecruitingModule.MODULE_ID + ".candidate.subjectWatcher";

	public static final String ACTION_GIVE_FEEDBACK = "giveFeedback";
	public static final String ACTION_DELETE_FEEDBACK = "deleteFeedback";
	public static final String ACTION_SHOW_HISTORY = "showHistory";
	public static final String ACTION_SHOW_CANDIDATES = "showCandidates";
	public static final String ACTION_ARCHIVE_VACANCY = "archiveVacancy";
	public static final String ACTION_ACTIVATE_VACANCY = "activateVacancy";

	public final static String GRAPH_ATTRIBUTE_FILES = "files";
	public final static String GRAPH_ATTRIBUTE_FEEDBACK = "feedback";
	public final static String GRAPH_ATTRIBUTE_VACANCIES = "vacancies";
	public final static String GRAPH_ATTRIBUTE_WATCHERS = "watchers";
	public final static String GRAPH_ATTRIBUTE_CANDIDATES = "candidates";
	public final static String GRAPH_ATTRIBUTE_LANGUAGES = "languages";
	public final static String GRAPH_ATTRIBUTE_SKILLS = "skills";
	public final static String GRAPH_ATTRIBUTE_CAREER_LEVELS = "careerLevels";
	public final static String GRAPH_ATTRIBUTE_BRANCH_LOCATIONS = "branchLocations";

	public final static String URL_CANDIDATE = "/modules/recruiting/candidateDialog.xhtml";
	public final static String URL_CANDIDATE_SHOW = "/modules/recruiting/displayPdfCandidateDialog.xhtml";
	public final static String URL_HISTORY = "/modules/recruiting/historyDialog.xhtml";
	public final static String URL_CANDIDATE_GIVE_FEEDBACK = "/modules/recruiting/giveFeedbackDialog.xhtml";

	public final static String URL_CLASSIFICATION = "/modules/recruiting/classificationDialog.xhtml";
	public final static String URL_RECRUITER = "/modules/recruiting/recruiterDialog.xhtml";
	public final static String URL_VACANCY = "/modules/recruiting/vacanciesDialog.xhtml";
	public final static String URL_VACANCY_SHOW_CANDIDATES = "/modules/recruiting/showAppliedCandidatesDialog.xhtml";
	public static final String URL_VACANCY_SHOW = "/modules/recruiting/showVacancyDialog.xhtml";
	public static final String URL_CANDIDATE_STATUS = "/modules/recruiting/CandidateStatusDialog.xhtml";
	public static final String URL_VACANCY_STATUS = "/modules/recruiting/VacancyStatusDialog.xhtml";

	public static final String CAREERLEVEL_TEXTRESOURCE = "recruiting.careerLevel.";
	public static final String CANDIDATE_STATUS_TEXTRESOURCE = "recruiting.candidateStatus.";
	public static final String VACANCY_STATUS_TEXTRESOURCE = "recruiting.vacancyStatus.";
	public static final String UPLOAD_CANDIDATE_FILENAME_PREFIX = "recruiting.vacancy.";
	public static final String DELETED_HISTORY = "DELETED";

	public static final DcemMediaType[] ALLOWED_FILE_TYPES = new DcemMediaType[] { DcemMediaType.PDF, DcemMediaType.PNG, DcemMediaType.JPEG };
	public static final int MAXIMUM_FILE_NUMBER = 8;
	public static final String GENERIC_IMAGE_MEDIATYPE = "image/*";
	public static final String RECRUITING_SERVICE_OPERATOR = "RecruitingServiceOperator";

	public static final String[] CANDIDATE_STANDARD_STATUS = { "Application", "Invite First Interview", "First Interview", "Invite Second Interview",
			"Second Interview", "Contract", "Contract confirmed", "Rejected", "Application Withdraw", "Pending", "No Interest" };
	public static final String[] DEFAULT_VACANCY_STATUS = new String[] { "Request", "Budget Request", "In Progress", "Cancelled", "Rejected", "Closed" };
	public static final String PUBLISHED = "Published";
	public static final String[] DEFAULT_VACANCY_STATUS_NOT_FOR_HEAD_OFS = new String[] { "Confirmed", PUBLISHED };
}