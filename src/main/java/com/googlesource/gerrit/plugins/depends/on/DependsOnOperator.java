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
import com.google.gerrit.server.change.ChangeFinder;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gerrit.server.query.change.ChangeQueryBuilder;
import com.google.gerrit.server.query.change.ChangeQueryBuilder.ChangeOperatorFactory;
import com.google.inject.Inject;

public class DependsOnOperator implements ChangeOperatorFactory {
  public static final String FIELD = "has";

  public class DependsOnPredicate extends PostFilterPredicate<ChangeData> {
    private Predicate<ChangeData> subQuery;

    public DependsOnPredicate(Predicate<ChangeData> subQuery) {
      super(FIELD, subQuery.toString());
      this.subQuery = subQuery;
    }

    @Override
    public boolean match(ChangeData change) {
      return changeMessageStore.load(change.getId()).stream()
          .filter(DependsOn::isResolved)
          .flatMap(dependsOn -> changeFinder.findOne(dependsOn.id()).stream())
          .anyMatch(
              note -> subQuery.asMatchable().match(changeDataFactory.create(note.getChange())));
    }

    @Override
    public int getCost() {
      return 2;
    }
  }

  protected final ChangeMessageStore changeMessageStore;
  protected final ChangeFinder changeFinder;
  protected final ChangeData.Factory changeDataFactory;

  @Inject
  public DependsOnOperator(
      ChangeMessageStore changeMessageStore,
      ChangeFinder changeFinder,
      ChangeData.Factory changeDataFactory) {
    this.changeMessageStore = changeMessageStore;
    this.changeFinder = changeFinder;
    this.changeDataFactory = changeDataFactory;
  }

  @Override
  public Predicate<ChangeData> create(ChangeQueryBuilder builder, String value)
      throws QueryParseException {
    return new DependsOnPredicate(builder.parse(value));
  }
}
