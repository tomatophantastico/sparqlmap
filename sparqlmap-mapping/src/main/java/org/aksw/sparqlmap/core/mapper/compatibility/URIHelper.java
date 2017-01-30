package org.aksw.sparqlmap.core.mapper.compatibility;

import java.util.BitSet;

public class URIHelper {

  
  public static BitSet RESERVED = new BitSet();
  
  static {
    
    RESERVED.set(';');
    RESERVED.set('/');
    RESERVED.set('?');
    RESERVED.set(':');
    RESERVED.set('@');
    RESERVED.set('&');
    RESERVED.set('=');
    RESERVED.set('+');
    RESERVED.set('$');
    RESERVED.set(',');
    RESERVED.set('[');
    RESERVED.set(']');


}
}
