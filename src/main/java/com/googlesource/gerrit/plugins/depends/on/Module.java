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

import com.google.gerrit.extensions.annotations.Exports;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.extensions.validators.CommentValidator;
import com.google.gerrit.server.DynamicOptions.DynamicBean;
import com.google.gerrit.server.events.EventListener;
import com.google.gerrit.server.query.change.ChangeQueryBuilder.ChangeOperatorFactory;
import com.google.gerrit.server.restapi.change.GetChange;
import com.google.gerrit.server.restapi.change.QueryChanges;
import com.google.gerrit.sshd.commands.Query;
import com.google.inject.AbstractModule;

public class Module extends AbstractModule {
  @Override
  protected void configure() {
    bind(ChangeOperatorFactory.class)
        .annotatedWith(Exports.named(InDependsOnOperator.FIELD))
        .to(InDependsOnOperator.class);
    DynamicSet.bind(binder(), EventListener.class).to(CoreListener.class);
    bind(DynamicBean.class)
        .annotatedWith(Exports.named(GetChange.class))
        .to(ChangeMessageStore.class);
    bind(DynamicBean.class).annotatedWith(Exports.named(Query.class)).to(ChangeMessageStore.class);
    bind(DynamicBean.class)
        .annotatedWith(Exports.named(QueryChanges.class))
        .to(ChangeMessageStore.class);
    bind(CommentValidator.class)
        .annotatedWith(Exports.named(DependsOnCommentValidator.class.getSimpleName()))
        .to(DependsOnCommentValidator.class);
  }
}
