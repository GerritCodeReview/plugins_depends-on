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
import com.google.gerrit.exceptions.StorageException;
import com.google.gerrit.index.query.PostFilterPredicate;
import com.google.gerrit.index.query.Predicate;
import com.google.gerrit.index.query.QueryParseException;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gerrit.server.query.change.ChangeQueryBuilder;
import com.google.gerrit.server.query.change.ChangeQueryBuilder.ChangeOperatorFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InDependsOnOperator implements ChangeOperatorFactory {
  private static final Logger log = LoggerFactory.getLogger(InDependsOnOperator.class);

  public class InDependsOnPredicate extends PostFilterPredicate<ChangeData> {
    protected final Set<Change.Id> dependentChanges;

    public InDependsOnPredicate(String value) {
      super(InDependsOnOperator.FIELD, value);
      dependentChanges =
          changeMessageStore.load(Change.Id.tryParse(value).get()).stream()
              .map(d -> d.id())
              .collect(Collectors.toSet());
    }

    @Override
    public int getCost() {
      return 1;
    }

    @Override
    public boolean match(ChangeData changeData) throws StorageException {
      return dependentChanges.contains(changeData.getId());
    }
  }

  public static final String FIELD = "in";
  protected final ChangeMessageStore changeMessageStore;

  @Inject
  public InDependsOnOperator(ChangeMessageStore changeMessageStore) {
    this.changeMessageStore = changeMessageStore;
  }

  @Override
  public Predicate<ChangeData> create(ChangeQueryBuilder builder, String value)
      throws QueryParseException {
    try {
      return new InDependsOnPredicate(value);
    } catch (NumberFormatException ex) {
      throw new QueryParseException("Error in operator " + FIELD + ":" + value, ex);
    } catch (StorageException ex) {
      String message = "Error in operator " + FIELD + ":" + value;
      log.error(message, ex);
      throw new QueryParseException(message, ex);
    }
  }
}
