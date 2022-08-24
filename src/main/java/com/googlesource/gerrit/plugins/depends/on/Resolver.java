// Copyright (C) 2021 The Android Open Source Project
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

import com.google.gerrit.entities.BranchNameKey;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gerrit.server.query.change.InternalChangeQuery;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Resolver {
  private final Provider<InternalChangeQuery> queryProvider;

  @Inject
  public Resolver(Provider<InternalChangeQuery> queryProvider) {
    this.queryProvider = queryProvider;
  }

  /** Are all the deps resolved to a specific change? */
  protected static boolean isResolved(Set<DependsOn> deps) {
    for (DependsOn dep : deps) {
      if (dep.id() == null) {
        return false;
      }
    }
    return true;
  }

  protected Set<DependsOn> resolve(Set<DependsOn> deps, Set<Set<BranchNameKey>> deliverables) {
    Set<DependsOn> current = new HashSet<>();
    for (DependsOn dep : deps) {
      if (dep.id() == null) {
        List<ChangeData> changes = queryProvider.get().byKey(dep.key());
        for (Set<BranchNameKey> deliverable : deliverables) {
          Set<DependsOn> resolved = resolve(changes, deliverable);
          if (resolved.isEmpty()) {
            current.add(dep);
          } else {
            current.addAll(resolved);
          }
        }
      } else {
        current.add(dep);
      }
    }
    return current;
  }

  protected Set<DependsOn> resolve(List<ChangeData> changes, Set<BranchNameKey> deliverable) {
    Set<DependsOn> found = new HashSet<>();
    for (ChangeData change : changes) {
      // Although we expect at most one change for each deliverable, it is possible that someone
      // re-used a change id across different projects, therefore consider all changes in the
      // deliverable.
      if (deliverable.contains(change.change().getDest())) {
        found.add(DependsOn.create(String.valueOf(change.getId())));
      }
    }
    return found;
  }
}
