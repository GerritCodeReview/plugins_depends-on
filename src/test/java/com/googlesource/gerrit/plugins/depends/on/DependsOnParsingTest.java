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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.gerrit.testing.InMemoryModule;
import com.googlesource.gerrit.plugins.depends.on.formats.Comment;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DependsOnParsingTest {
  public static final String NUM = "1234";
  public static final String NUM2 = "345";
  public static final String KEY = "Iabcdef7890abcdef7890abcdef7890abcdef7890";
  public static final String KEY2 = "I1234567890ABCDEFe7890ABC7868ABCDEF122233";

  public static DependsOn NUM_DEP;
  public static DependsOn KEY_DEP;

  @Before
  public void setUp() {
    new InMemoryModule().inject(this); // Needed to setup KeyUtil.ENCODER_IMPL
    NUM_DEP = DependsOn.create(NUM);
    KEY_DEP = DependsOn.create(KEY);
  }

  @Test
  public void testCommentMessageChangeNum() {
    assertThat(Comment.getMessage(NUM_DEP)).isEqualTo(NUM);
  }

  @Test
  public void testCommentMessageChangeKey() {
    assertThat(Comment.getMessage(KEY_DEP)).isEqualTo(KEY);
  }

  @Test
  public void testParseNum() {
    DependsOn dep = DependsOn.create(NUM);
    assertThat("" + dep.id().get()).isEqualTo(NUM);
  }

  @Test
  public void testParseKey() {
    DependsOn dep = DependsOn.create(KEY);
    assertThat(dep.key().get()).isEqualTo(KEY);
  }

  @Test
  public void testParseNoneComment() {
    String comment = "My Very Educated Mother Just Served Us Nothing!";
    assertThat(Comment.from(comment)).isEmpty();
  }

  @Test
  public void testParseEmptyComment() {
    String comment = "Depends-on:";
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    assertThat(deps.get()).hasSize(0);
  }

  @Test
  public void testParseOneNumComment() {
    String comment = "Depends-on:" + NUM;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    for (DependsOn dep : deps.get()) {
      assertThat("" + dep.id().get()).isEqualTo(NUM);
      return;
    }
    assertWithMessage("not expected to reach").fail();
  }

  @Test
  public void testParseOneKeyComment() {
    String comment = "Depends-on:" + KEY;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    for (DependsOn dep : deps.get()) {
      assertThat(dep.key().get()).isEqualTo(KEY);
      return;
    }
    assertWithMessage("not expected to reach").fail();
  }

  @Test
  public void testParseTwoNumsComment() {
    String comment = "Depends-on:" + NUM + " " + NUM2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    assertThat(deps.get()).hasSize(2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertThat("" + dep.id().get()).isAnyOf(NUM, NUM2);
      found++;
    }
    assertThat(found).isEqualTo(2);
  }

  @Test
  public void testParseTwoKeyComments() {
    String comment = "Depends-on:" + KEY + " " + KEY2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    assertThat(deps.get()).hasSize(2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertThat(dep.key().get()).isAnyOf(KEY, KEY2);
      found++;
    }
    assertThat(found).isEqualTo(2);
  }

  @Test
  public void testParseNumAndKeyComment() {
    String comment = "Depends-on:" + NUM + " " + KEY;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    assertThat(deps.get()).hasSize(2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      try {
        assertThat("" + dep.id().get()).isEqualTo(NUM);
      } catch (Exception e) {
        assertThat(dep.key().get()).isEqualTo(KEY);
      }
      found++;
    }
    assertThat(found).isEqualTo(2);
  }

  @Test
  public void testParseTwoNumsCommaComment() {
    String comment = "Depends-on:" + NUM + "," + NUM2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    assertThat(deps.get()).hasSize(2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertThat("" + dep.id().get()).isAnyOf(NUM, NUM2);
      found++;
    }
    assertThat(found).isEqualTo(2);
  }

  @Test
  public void testParseTwoNumsCommaSpaceComment() {
    String comment = "Depends-on:" + NUM + ", " + NUM2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    assertThat(deps.get()).hasSize(2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertThat("" + dep.id().get()).isAnyOf(NUM, NUM2);
      found++;
    }
    assertThat(found).isEqualTo(2);
  }

  @Test
  public void testParseTwoNumsWhiteComment() {
    String comment = "Depends-on:" + NUM + ", \t" + NUM2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    assertThat(deps.get()).hasSize(2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertThat("" + dep.id().get()).isAnyOf(NUM, NUM2);
      found++;
    }
    assertThat(found).isEqualTo(2);
  }

  @Test
  public void testParseTwoNumsNewLineWhiteComment() {
    // Should stop processing at newline
    String comment = "Depends-on:" + NUM + ", \t\n" + NUM2;
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    assertThat(deps.get()).hasSize(1);
    for (DependsOn dep : deps.get()) {
      assertThat("" + dep.id().get()).isEqualTo(NUM);
    }
  }

  @Test
  public void testParseEmbeddedComment() {
    String comment = "Patch Set 2:\n\nDepends-on:" + NUM + " " + NUM2 + "\nHey\n";
    Optional<List<DependsOn>> deps = Comment.from(comment);
    assertThat(deps).isPresent();
    assertThat(deps.get()).hasSize(2);
    int found = 0;
    for (DependsOn dep : deps.get()) {
      assertThat("" + dep.id().get()).isAnyOf(NUM, NUM2);
      found++;
    }
    assertThat(found).isEqualTo(2);
  }
}
