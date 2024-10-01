package com.doubleclue.dcem.recruiting.exceptions;

import java.util.ResourceBundle;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;

public class RecruitingException extends DcemException {

	private static final long serialVersionUID = 1L;

	private RecruitingErrorCodes recruitingErrorCode;

	public String getLocalizedMessageWithMessage() {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(RecruitingModule.RESOURCE_NAME, JsfUtils.getLocale());
			String error = JsfUtils.getMessageFromBundle(bundle, RecruitingErrorCodes.class.getSimpleName() + "." + recruitingErrorCode.name(), super.getMessage());
			if (error.startsWith("???")) {
				return this.toString();
			}
			return error;
		} catch (Exception e) {
			return this.toString();
		}
	}

	public String getLocalizedMessage() {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(RecruitingModule.RESOURCE_NAME, JsfUtils.getLocale());
			String error = JsfUtils.getStringSafely(bundle, RecruitingErrorCodes.class.getSimpleName() + "." + recruitingErrorCode.name());
			if (error.startsWith("???")) {
				return this.toString();
			}
			return error;
		} catch (Exception e) {
			return this.toString();
		}
	}

	public RecruitingException(RecruitingErrorCodes recruitingErrorCode, String message, Throwable cause) {
		super(null, message, cause);
		this.recruitingErrorCode = recruitingErrorCode;
	}

	public RecruitingException(RecruitingErrorCodes recruitingErrorCode, String message) {
		super(null, message, null);
		this.recruitingErrorCode = recruitingErrorCode;
	}


	public Throwable getExceptionCause() {
		return super.getCause();
	}

	public String getMessage() {
		return super.getMessage();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(recruitingErrorCode.name() + " - ");
		if (getMessage() != null) {
			sb.append(getMessage());
		}
		if (getCause() != null) {
			sb.append(" - ");
			sb.append(getCause().toString());
		}
		return sb.toString();
	}

	public RecruitingErrorCodes getRecruitingErrorCode() {
		return recruitingErrorCode;
	}

	public void setRecruitingErrorCode(RecruitingErrorCodes recruitingErrorCode) {
		this.recruitingErrorCode = recruitingErrorCode;
	}

}
