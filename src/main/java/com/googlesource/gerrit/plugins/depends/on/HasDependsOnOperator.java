// Copyright (C) 2022 The Android Open Source Project
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

import com.google.gerrit.index.query.PostFilterPredicate;
import com.google.gerrit.index.query.Predicate;
import com.google.gerrit.index.query.QueryParseException;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gerrit.server.query.change.ChangeQueryBuilder;
import com.google.gerrit.server.query.change.ChangeQueryBuilder.ChangeHasOperandFactory;
import com.google.inject.Inject;

public class HasDependsOnOperator implements ChangeHasOperandFactory {
  public static final String FIELD = "a";

  public class HasDependsOnPredicate extends PostFilterPredicate<ChangeData> {
    HasDependsOnPredicate() {
      super("has", FIELD);
    }

    @Override
    public boolean match(ChangeData change) {
      return !changeMessageStore.load(change.notes()).isEmpty();
    }

    @Override
    public int getCost() {
      return 2;
    }
  }

  protected final ChangeMessageStore changeMessageStore;

  @Inject
  public HasDependsOnOperator(ChangeMessageStore changeMessageStore) {
    this.changeMessageStore = changeMessageStore;
  }

  @Override
  public Predicate<ChangeData> create(ChangeQueryBuilder builder) throws QueryParseException {
    return new HasDependsOnPredicate();
  }
}
