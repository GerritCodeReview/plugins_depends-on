// Copyright (C) 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.depends.on;

import com.google.gerrit.testing.InMemoryModule;
import com.googlesource.gerrit.plugins.depends.on.formats.Comment;
import java.util.List;
import java.util.Optional;
import junit.framework.TestCase;
import org.junit.Test;

public class DependsOnParsingTest extends TestCase {
  public static final String NUM = "1234";
  public static final String NUM2 = "345";
  public static final String KEY = "Iabcdef7890abcdef7890abcdef7890abcdef7890";
  public static final String KEY2 = "I1234567890ABCDEFe7890ABC7868ABCDEF122233";

  public static DependsOn NUM_DEP;
  public static DependsOn KEY_DEP;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    new InMemoryModule().inject(this); // Needed to setup KeyUtil.ENCODER_IMPL
    NUM_DEP = DependsOn.create(NUM);
    KEY_DEP = DependsOn.create(KEY);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testCommentMessageChangeNum() {
    assertTrue(NUM.equals(Comment.getMessage(NUM_DEP)));
  }

  @Test
  public void testCommentMessageChangeKey() {
    assertTrue(KEY.equals(Comment.getMessage(KEY_DEP)));
  }

  @Test
  public void testParseNum() {
    DependsOn dep = DependsOn.create(NUM);
    assertTrue(NUM.equals("" + dep.id().get()));
  }

  @Test
  public void testParseKey() {
    DependsOn dep = DependsOn.create(KEY);
    assertTrue(KEY.equals(dep.key().get()));
  }

  @Test
  public void testParseNoneComment() {
    String comment = "My Very Educated Mother Just Served Us Nothing!";
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertFalse(deps.isPresent());
  }

  @Test
  public void testParseEmptyComment() {
    String comment = "Depends-on:";
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertTrue(deps.get().size() == 0);
  }

  @Test
  public void testParseOneNumComment() {
    String comment = "Depends-on:" + NUM;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    for (DependsOn dep : deps.get()) {
      assertTrue(NUM.equals("" + dep.id().get()));
      return;
    }
    assertTrue(false);
  }

  @Test
  public void testParseOneKeyComment() {
    String comment = "Depends-on:" + KEY;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    for (DependsOn dep : deps.get()) {
      assertTrue(KEY.equals("" + dep.key().get()));
      return;
    }
    assertTrue(false);
  }

  @Test
  public void testParseTwoNumsComment() {
    String comment = "Depends-on:" + NUM + " " + NUM2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertTrue(deps.get().size() == 2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertTrue(NUM.equals("" + dep.id().get()) || NUM2.equals("" + dep.id().get()));
      found++;
    }
    assertTrue(found == 2);
  }

  @Test
  public void testParseTwoKeyComments() {
    String comment = "Depends-on:" + KEY + " " + KEY2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertTrue(deps.get().size() == 2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertTrue(KEY.equals("" + dep.key().get()) || KEY2.equals("" + dep.key().get()));
      found++;
    }
    assertTrue(found == 2);
  }

  @Test
  public void testParseNumAndKeyComment() {
    String comment = "Depends-on:" + NUM + " " + KEY;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertTrue(deps.get().size() == 2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      try {
        assertTrue(NUM.equals("" + dep.id().get()));
      } catch (Exception e) {
        assertTrue(KEY.equals("" + dep.key().get()));
      }
      found++;
    }
    assertTrue(found == 2);
  }

  public void testParseTwoNumsCommaComment() {
    String comment = "Depends-on:" + NUM + "," + NUM2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertTrue(deps.get().size() == 2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertTrue(NUM.equals("" + dep.id().get()) || NUM2.equals("" + dep.id().get()));
      found++;
    }
    assertTrue(found == 2);
  }

  public void testParseTwoNumsCommaSpaceComment() {
    String comment = "Depends-on:" + NUM + ", " + NUM2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertTrue(deps.get().size() == 2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertTrue(NUM.equals("" + dep.id().get()) || NUM2.equals("" + dep.id().get()));
      found++;
    }
    assertTrue(found == 2);
  }

  public void testParseTwoNumsWhiteComment() {
    String comment = "Depends-on:" + NUM + ", \t" + NUM2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertTrue(deps.get().size() == 2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertTrue(NUM.equals("" + dep.id().get()) || NUM2.equals("" + dep.id().get()));
      found++;
    }
    assertTrue(found == 2);
  }

  public void testParseTwoNumsNewLineWhiteComment() {
    // Should stop processing at newline
    String comment = "Depends-on:" + NUM + ", \t\n" + NUM2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertTrue(deps.get().size() == 1);
    for (DependsOn dep : deps.get()) {
      assertTrue(NUM.equals("" + dep.id().get()));
    }
  }

  public void testParseEmbeddedComment() {
    String comment = "Patch Set 2:\n\nDepends-on:" + NUM + " " + NUM2 + "\nHey\n";
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertTrue(deps.get().size() == 2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertTrue(NUM.equals("" + dep.id().get()) || NUM2.equals("" + dep.id().get()));
      found++;
    }
    assertTrue(found == 2);
  }
}
