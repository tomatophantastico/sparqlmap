package org.aksw.sparqlmap.core;

import java.util.List;
import java.util.Map;

import org.aksw.sparqlmap.core.mapper.finder.QueryBinding;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;
import org.jooq.lambda.tuple.Tuple2;

import com.google.common.collect.Maps;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UpdateRequestBinding {
	
	private final UpdateRequest original;
	private Map<Update, QueryBinding> updateBindings = Maps.newIdentityHashMap();

}
