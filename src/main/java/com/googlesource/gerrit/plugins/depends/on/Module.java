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
import com.google.gerrit.entities.Change;
import com.google.gerrit.entities.PatchSet;
import com.google.gerrit.exceptions.StorageException;
import com.google.gerrit.extensions.annotations.Exports;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.extensions.validators.CommentValidator;
import com.google.gerrit.extensions.webui.JavaScriptPlugin;
import com.google.gerrit.extensions.webui.WebUiPlugin;
import com.google.gerrit.server.DynamicOptions.DynamicBean;
import com.google.gerrit.server.change.ChangePluginDefinedInfoFactory;
import com.google.gerrit.server.events.EventListener;
import com.google.gerrit.server.notedb.ChangeNotes;
import com.google.gerrit.server.project.InvalidChangeOperationException;
import com.google.gerrit.server.project.NoSuchChangeException;
import com.google.gerrit.server.query.change.ChangeQueryBuilder.ChangeHasOperandFactory;
import com.google.gerrit.server.query.change.ChangeQueryBuilder.ChangeOperatorFactory;
import com.google.gerrit.server.restapi.change.GetChange;
import com.google.gerrit.server.restapi.change.QueryChanges;
import com.google.gerrit.sshd.commands.Query;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.depends.on.extensions.DependencyResolver;
import java.util.Set;
import org.kohsuke.args4j.Option;

public class Module extends AbstractModule {
  @Override
  protected void configure() {
    bind(ChangeOperatorFactory.class)
        .annotatedWith(Exports.named(InDependsOnOperator.FIELD))
        .to(InDependsOnOperator.class);
    bind(ChangeOperatorFactory.class)
        .annotatedWith(Exports.named(DependsOnOperator.FIELD))
        .to(DependsOnOperator.class);
    bind(ChangeHasOperandFactory.class)
        .annotatedWith(Exports.named(HasDependsOnOperator.FIELD))
        .to(HasDependsOnOperator.class);
    DynamicSet.bind(binder(), EventListener.class).to(CoreListener.class);
    bind(ChangePluginDefinedInfoFactory.class)
        .annotatedWith(Exports.named("depends-ons"))
        .to(DependsOnAttributeFactory.class);
    bind(DynamicBean.class).annotatedWith(Exports.named(GetChange.class)).to(MyQueryOptions.class);
    bind(DynamicBean.class).annotatedWith(Exports.named(Query.class)).to(MyQueryOptions.class);
    bind(DynamicBean.class)
        .annotatedWith(Exports.named(QueryChanges.class))
        .to(MyQueryOptions.class);
    bind(CommentValidator.class)
        .annotatedWith(Exports.named(DependsOnCommentValidator.class.getSimpleName()))
        .to(DependsOnCommentValidator.class);
    DynamicSet.bind(binder(), WebUiPlugin.class)
        .toInstance(new JavaScriptPlugin("gr-depends-on-plugin.js"));
  }

  public static class MyQueryOptions implements DependencyResolver {
    @Option(name = "--all", usage = "Include all depends-on in the output")
    public boolean all = false;

    protected final ChangeMessageStore changeMessageStore;

    @Inject
    public MyQueryOptions(ChangeMessageStore changeMessageStore) {
      this.changeMessageStore = changeMessageStore;
    }

    @Override
    public boolean resolveDependencies(
        ChangeNotes changeNotes, Set<Set<BranchNameKey>> deliverables)
        throws InvalidChangeOperationException {
      return changeMessageStore.resolveDependencies(changeNotes, deliverables);
    }

    @Deprecated
    @Override
    public boolean resolveDependencies(PatchSet.Id patchSetId, Set<Set<BranchNameKey>> deliverables)
        throws InvalidChangeOperationException, StorageException, NoSuchChangeException {
      return changeMessageStore.resolveDependencies(patchSetId, deliverables);
    }

    @Override
    public boolean hasUnresolvedDependsOn(Change.Id changeId) throws StorageException {
      return changeMessageStore.hasUnresolvedDependsOn(changeId);
    }
  }
}
