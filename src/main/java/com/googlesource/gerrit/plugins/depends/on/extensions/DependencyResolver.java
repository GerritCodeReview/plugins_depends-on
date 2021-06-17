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

package com.googlesource.gerrit.plugins.depends.on.extensions;

import com.google.gerrit.entities.BranchNameKey;
import com.google.gerrit.entities.Change;
import com.google.gerrit.entities.PatchSet;
import com.google.gerrit.exceptions.StorageException;
import com.google.gerrit.server.DynamicOptions.DynamicBean;
import com.google.gerrit.server.project.InvalidChangeOperationException;
import java.util.Set;

public interface DependencyResolver extends DynamicBean {
  public boolean resolveDependencies(PatchSet.Id patchSetId, Set<Set<BranchNameKey>> deliverables)
      throws InvalidChangeOperationException, StorageException;

  public boolean hasUnresolvedDependsOn(Change.Id changeId) throws StorageException;
}
