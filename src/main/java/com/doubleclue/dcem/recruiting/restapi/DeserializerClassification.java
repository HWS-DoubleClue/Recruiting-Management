package com.doubleclue.dcem.recruiting.restapi;

import java.io.IOException;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.recruiting.logic.ClassificationLogic;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class DeserializerClassification extends JsonDeserializer<Object> {
	
	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
		int id = jsonParser.getValueAsInt();
		if (id < 1) {
			return null;
		}
		ClassificationLogic classificationLogic = CdiUtils.getReference(ClassificationLogic.class);
		return classificationLogic.getById(id);
	}

}
