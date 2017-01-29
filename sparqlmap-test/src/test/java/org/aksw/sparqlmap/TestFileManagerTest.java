package org.aksw.sparqlmap;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestFileManagerTest {

  @Test
  public void test() {
    
    assertTrue(!new TestFileManager().getR2rmltest().get("hsqldb").isEmpty());
  }

}
