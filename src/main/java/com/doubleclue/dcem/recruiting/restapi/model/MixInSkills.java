package com.doubleclue.dcem.recruiting.restapi.model;

import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class MixInSkills {
	
	@JsonIgnore
	private boolean obtainable = true;

	@JsonIgnore
	private ApprovalStatus approvalStatus;
}
