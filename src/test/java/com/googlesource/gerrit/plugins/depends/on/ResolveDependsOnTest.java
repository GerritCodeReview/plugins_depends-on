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

import com.google.gerrit.testing.InMemoryModule;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResolveDependsOnTest {
  public static final String NUM = "1234";
  public static final String NUM2 = "345";
  public static final String KEY = "Iabcdef7890abcdef7890abcdef7890abcdef7890";
  public static final String KEY2 = "I1234567890ABCDEFe7890ABC7868ABCDEF122233";

  public static DependsOn NUM_DEP;
  public static DependsOn NUM2_DEP;
  public static DependsOn KEY_DEP;
  public static DependsOn KEY2_DEP;

  @Before
  public void setUp() {
    new InMemoryModule().inject(this); // Needed to set up KeyUtil.ENCODER_IMPL
    NUM_DEP = DependsOn.create(NUM);
    NUM2_DEP = DependsOn.create(NUM2);
    KEY_DEP = DependsOn.create(KEY);
    KEY2_DEP = DependsOn.create(KEY2);
  }

  @Test
  public void testResolved2Nums() {
    Set<DependsOn> deps = new HashSet<>();
    deps.add(NUM_DEP);
    deps.add(NUM2_DEP);
    assertThat(Resolver.isResolved(deps)).isTrue();
  }

  @Test
  public void testResolved2Keys() {
    Set<DependsOn> deps = new HashSet<>();
    deps.add(KEY_DEP);
    deps.add(KEY2_DEP);
    assertThat(Resolver.isResolved(deps)).isFalse();
  }

  @Test
  public void testResolvedNumAndKey() {
    Set<DependsOn> deps = new HashSet<>();
    deps.add(NUM_DEP);
    deps.add(KEY_DEP);
    assertThat(Resolver.isResolved(deps)).isFalse();
  }
}
