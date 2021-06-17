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

import com.google.auto.value.AutoValue;
import com.google.gerrit.common.Nullable;
import com.google.gerrit.entities.Change;
import com.google.gerrit.entities.Change.Id;
import java.util.Optional;

/**
 * Represents a non-git change dependency.
 *
 * <p>If populated with a Change.Id, then this is an explicit dependency. Populating it with only a
 * Change.Key is documenting a dependency that may point to 0 or more changes and thus relies on
 * custom intelligence, or human knowledge to figure out which project and branch this Change.Key is
 * referring to.
 */
@AutoValue
public abstract class DependsOn {

  @Nullable
  public abstract Change.Id id();

  @Nullable
  public abstract Change.Key key();

  public static DependsOn create(String change) {
    Optional<Id> id = null;
    Change.Key key = null;
    id = Change.Id.tryParse(change);
    if (!id.isPresent()) {
      return create(Change.Key.parse(change));
    }
    return create(id.get(), key);
  }

  public static DependsOn create(Change.Key key) {
    return create(null, key);
  }

  public static DependsOn create(Change.Id id, Change.Key key) {
    return new AutoValue_DependsOn(id, key);
  }

  public boolean isResolved() {
    return id() != null;
  }
}
