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

import com.google.gerrit.entities.Change;
import com.google.gerrit.index.query.Predicate;
import com.google.gerrit.index.query.QueryParseException;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gerrit.server.query.change.ChangeIndexPredicate;
import com.google.gerrit.server.query.change.ChangePredicates;
import com.google.gerrit.server.query.change.ChangeQueryBuilder;
import com.google.gerrit.server.query.change.ChangeQueryBuilder.ChangeOperatorFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InDependsOnOperator implements ChangeOperatorFactory {
  private static final Logger log = LoggerFactory.getLogger(InDependsOnOperator.class);

  public static final String FIELD = "in";

  protected final ChangeMessageStore changeMessageStore;

  @Inject
  public InDependsOnOperator(ChangeMessageStore changeMessageStore) {
    this.changeMessageStore = changeMessageStore;
  }

  @Override
  public Predicate<ChangeData> create(ChangeQueryBuilder builder, String value)
      throws QueryParseException {
    Optional<Change.Id> changeId = Change.Id.tryParse(value);
    if (changeId.isPresent()) {
      List<Predicate<ChangeData>> predicates =
          changeMessageStore.load(changeId.get()).stream()
              .filter(DependsOn::isResolved)
              .map(d -> ChangePredicates.idStr(d.id()))
              .collect(Collectors.toList());
      if (!predicates.isEmpty()) {
        return Predicate.or(predicates);
      }
    }
    return ChangeIndexPredicate.none();
  }
}
