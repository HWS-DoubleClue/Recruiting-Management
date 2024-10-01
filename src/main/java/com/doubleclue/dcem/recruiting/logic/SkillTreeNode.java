package com.doubleclue.dcem.recruiting.logic;

import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;

public class SkillTreeNode {

	public SkillTreeNode(boolean include, SkillsEntity skillsEntity,  SkillsLevel skillsLevel) {
		super();
		this.include = include;
		this.skillsEntity = skillsEntity;
		this.skillsLevel = skillsLevel;
	}

	public SkillTreeNode(SkillsLevelEntity skillsLevelEntity) {
		this.include = true;
		this.skillsEntity = skillsLevelEntity.getSkill();
		this.skillsLevel = skillsLevelEntity.getLevel();
	}

	boolean include;
	SkillsEntity skillsEntity;
	SkillsLevel skillsLevel;
	
	public boolean isInclude() {
		return include;
	}

	public void setInclude(boolean include) {
		this.include = include;
	}
	
	public SkillsLevel getSkillsLevel() {
		return skillsLevel;
	}

	public void setSkillsLevel(SkillsLevel skillsLevel) {
		this.skillsLevel = skillsLevel;
	}

	public String getName() {
		return skillsEntity.getName();
	}
	
	public Integer getId() {
		return skillsEntity.getId();
	}

	public SkillsEntity getSkillsEntity() {
		return skillsEntity;
	}

	public void setSkillsEntity(SkillsEntity skillsEntity) {
		this.skillsEntity = skillsEntity;
	}
	
	public String getIncludeIcon() {
		if (include == true) {
			return "fa fa-thumbs-up";
		} else {
			return "fa fa-xmark";
		}
	}

	@Override
	public String toString() {
		return "SkillTreeNode [include=" + include + ", skillsEntity=" + skillsEntity + ", skillsLevel=" + skillsLevel + "]";
	}

}
