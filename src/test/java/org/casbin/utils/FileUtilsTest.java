package org.casbin.utils;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

  @Test
  void testGetFileAsInputStream_whenDoesExist_returnNull() {
    InputStream actual = FileUtils.getFileAsInputStream("invalid-path");
    assertNull(actual);
  }

  @Test
  void testGetFileAsInputStream() {
    InputStream actual = FileUtils.getFileAsInputStream("src/test/resources/application.yml");
    assertNotNull(actual);
  }

  @Test
  void testGetFileAsText() {
    var actual = FileUtils.getFileAsText("src/test/resources/application.yml");
    assertNotNull(actual);
  }
}