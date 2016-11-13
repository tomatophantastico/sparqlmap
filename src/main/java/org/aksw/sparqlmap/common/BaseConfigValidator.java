package org.aksw.sparqlmap.common;

import java.io.File;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class BaseConfigValidator implements Validator{

  @Override
  public boolean supports(Class<?> clazz) {
   
    return clazz == BaseConfig.class;
  }

  @Override
  public void validate(Object target, Errors errors) {
    BaseConfig conf = (BaseConfig) target;
    
    
    validateDataSource(conf, errors);
    
    
    
  }
  
  
  
  private void validateDataSource(BaseConfig conf, Errors errors ){
    switch(conf.getDsType()){
    case ACCESS:
      
      if(!new File(conf.getDsLocation()).exists()){
        errors.rejectValue("dsLocation", "The file does not exist.");
      }
      break;
    
    }
    
    
    
  }
  

}
