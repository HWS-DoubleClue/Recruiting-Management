package com.doubleclue.dcem.recruiting.gui;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.recruiting.subjects.HistorySubject;

@Named("historyView")
@SessionScoped
public class HistoryView extends DcemView implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Inject
	private HistorySubject historySubject;
	
	@Inject
	private HistoryDialog historyDialog;
	
	@PostConstruct
	public void init() {
		historyDialog.setParentView(this);
		subject = historySubject;
	}
	
	@Override
	public void reload() { }
}
