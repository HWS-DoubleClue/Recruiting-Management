package com.doubleclue.dcem.recruiting.entities.enums;

public enum VacancyPriority {
	Urgent("urgentColor"), High("highColor"), Normal("normalColor"), Low("lowColor"), Lowest("lowestColor");

	private String colorClass;

	private VacancyPriority(String colorClass) {
		this.colorClass = colorClass;
	}

	public String getColorClass() {
		return colorClass;
	}
}
