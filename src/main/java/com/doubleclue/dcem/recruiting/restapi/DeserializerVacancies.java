package com.doubleclue.dcem.recruiting.restapi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.logic.VacancyLogic;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


public class DeserializerVacancies extends JsonDeserializer<SortedSet<VacancyEntity>> {

	@Override
	public SortedSet<VacancyEntity> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
		ArrayNode node = jsonParser.readValueAsTree();
		if (node == null || node.isEmpty() == true) {
			return null;
		}
		VacancyLogic vacancyLogic = CdiUtils.getReference(VacancyLogic.class);
		Iterator<JsonNode> iterator = node.elements();
		SortedSet<VacancyEntity> list = new TreeSet<VacancyEntity>();
		while (iterator.hasNext()) {
			try {
				list.add(vacancyLogic.getVacancyById(iterator.next().asInt()));
			} catch (Exception e) {
				continue;
			}
		}
		return list;
	}

}
