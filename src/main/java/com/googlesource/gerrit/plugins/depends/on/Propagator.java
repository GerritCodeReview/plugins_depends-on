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

import com.google.gerrit.entities.Change;
import com.google.gerrit.server.notedb.ChangeNotes;
import com.google.gerrit.server.project.InvalidChangeOperationException;
import com.google.gerrit.server.project.NoSuchChangeException;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/*
 * Propagates dependencies added on source change using "Depends-on:" tag to
 * copied change. Dependencies on copied change are added using Change-Ids.
 */
public class Propagator {
  protected final ChangeMessageStore changeMessageStore;
  protected final ChangeNotes.Factory changeNotesFactory;

  @Inject
  public Propagator(ChangeMessageStore changeMessageStore, ChangeNotes.Factory changeNotesFactory) {
    this.changeMessageStore = changeMessageStore;
    this.changeNotesFactory = changeNotesFactory;
  }

  public void propagateFromSourceToDestination(ChangeNotes srcChange, ChangeNotes destChange)
      throws InvalidChangeOperationException, NoSuchChangeException {

    Set<DependsOn> deps = changeMessageStore.load(srcChange);
    if (!deps.isEmpty()) {
      Set<DependsOn> keyDeps = new HashSet<DependsOn>(deps.size());
      for (DependsOn dep : deps) {
        keyDeps.add(DependsOn.create(loadChangeKey(dep)));
      }
      changeMessageStore.store(
          destChange,
          keyDeps,
          "Dependencies propagated from " + srcChange.getChange().currentPatchSetId());
    }
  }

  /**
   * Get the Change-Id for a DependsOn. Look it up in the DB if the DependsOn Change-Id is not known
   */
  protected Change.Key loadChangeKey(DependsOn dep) {
    Change.Key changeKey = dep.key();
    if (changeKey != null) {
      return changeKey;
    }
    Change c = changeNotesFactory.createCheckedUsingIndexLookup(dep.id()).getChange();
    if (c != null) {
      return c.getKey();
    }
    // Since the ChangeKey parser will accept any random string
    // as a Key, it will inherently carry along random strings.
    // This is thus used to carry unidentified dependencies to
    // propagated changes (the assumption is that the user needs
    // to fix it). Piggy back here on this idea for change-nums
    // that lead to unidentified changes (treat them as bad
    // strings, and throw them into the change-id to get
    // propagated).
    return Change.Key.parse(Integer.toString(dep.id().get()));
  }
}
