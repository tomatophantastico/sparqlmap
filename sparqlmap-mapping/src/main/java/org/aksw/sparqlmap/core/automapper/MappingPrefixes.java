package org.aksw.sparqlmap.core.automapper;

import org.aksw.sparqlmap.core.r2rml.R2RML;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

@Getter
@Wither
@AllArgsConstructor
public class MappingPrefixes {

    
    private String mappingPrefix;
    private String instancePrefix;
    private String vocabularyPrefix;
    private String primaryKeySeparator;
    private String rowidtemplate;

    public MappingPrefixes(String prefix){
        super();
        this.mappingPrefix = prefix + "mapping/";
        this.instancePrefix = prefix + "instance/";
        this.vocabularyPrefix = prefix + "vocabulary/";
        this.primaryKeySeparator = ";";
        this.rowidtemplate = null;
    }


    public PrefixMapping asPrefixMap(){
        PrefixMapping pmap = new PrefixMappingImpl();

        pmap.setNsPrefix("rr", R2RML.R2RML_STRING);
        if(vocabularyPrefix.equals(instancePrefix)&& vocabularyPrefix.equalsIgnoreCase(mappingPrefix)) {
          pmap.setNsPrefix("mapping", mappingPrefix);
        }else {
          pmap.setNsPrefix("vocab", vocabularyPrefix);
          pmap.setNsPrefix("mapping", mappingPrefix);
          pmap.setNsPrefix("inst", instancePrefix);
        }
       
        return pmap;

    }

}
