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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gerrit.entities.BranchNameKey;
import com.google.gerrit.entities.Change;
import com.google.gerrit.entities.ChangeMessage;
import com.google.gerrit.entities.PatchSet;
import com.google.gerrit.exceptions.StorageException;
import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.server.ChangeMessagesUtil;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.cache.PerThreadCache;
import com.google.gerrit.server.change.ChangeResource;
import com.google.gerrit.server.change.RevisionResource;
import com.google.gerrit.server.notedb.ChangeNotes;
import com.google.gerrit.server.project.InvalidChangeOperationException;
import com.google.gerrit.server.project.NoSuchChangeException;
import com.google.gerrit.server.restapi.change.PostReview;
import com.google.gerrit.server.update.RetryHelper;
import com.google.gerrit.server.update.UpdateException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlesource.gerrit.plugins.depends.on.extensions.DependencyResolver;
import com.googlesource.gerrit.plugins.depends.on.formats.Comment;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeMessageStore implements DependencyResolver {
  private static final Logger log = LoggerFactory.getLogger(ChangeMessageStore.class);

  public static class DependsOnByChangeCache extends HashMap {}

  protected static final PerThreadCache.Key<DependsOnByChangeCache> DEPENDS_ON_BY_CHANGE_CACHE_KEY =
      PerThreadCache.Key.create(DependsOnByChangeCache.class);

  public interface Factory {
    ChangeMessageStore create();
  }

  protected final Provider<PostReview> reviewProvider;
  protected final ChangeResource.Factory changeResourceFactory;
  protected final CurrentUser currentUser;
  protected final Resolver resolver;
  protected final ChangeNotes.Factory changeNotesFactory;
  protected final ChangeMessagesUtil cmUtil;
  protected final RetryHelper retryHelper;

  @Inject
  public ChangeMessageStore(
      Provider<PostReview> reviewProvider,
      ChangeResource.Factory changeResourceFactory,
      CurrentUser currentUser,
      Resolver resolver,
      ChangeNotes.Factory changeNotesFactory,
      ChangeMessagesUtil cmUtil,
      RetryHelper retryHelper) {
    this.reviewProvider = reviewProvider;
    this.changeResourceFactory = changeResourceFactory;
    this.currentUser = currentUser;
    this.resolver = resolver;
    this.changeNotesFactory = changeNotesFactory;
    this.cmUtil = cmUtil;
    this.retryHelper = retryHelper;
  }

  /**
   * Load the current DependsOn from the DB for a specific change. "Current" is defined as the last
   * Depends-on defined. Older Depends-ons are assumed to be overriden by the last one. If the last
   * Depends-on is blank, it deletes any previous dependencies.
   *
   * <p>return empty set means no dependencies found.
   */
  public Set<DependsOn> load(Change.Id cid) throws StorageException {
    return loadWithOrder(cid).stream().collect(Collectors.toSet());
  }

  public Set<DependsOn> load(ChangeNotes changeNotes) throws StorageException {
    return loadWithOrder(changeNotes).stream().collect(Collectors.toSet());
  }

  public List<DependsOn> loadWithOrder(Change.Id cid) throws StorageException {
    ChangeNotes changeNotes;
    try {
      changeNotes = changeNotesFactory.createCheckedUsingIndexLookup(cid);
    } catch (NoSuchChangeException e) {
      return Collections.emptyList();
    }
    return loadWithOrder(changeNotes);
  }

  public List<DependsOn> loadWithOrder(ChangeNotes changeNotes) throws StorageException {
    PerThreadCache perThreadCache = PerThreadCache.get();
    if (perThreadCache != null) {
      Map<Change.Id, List<DependsOn>> dependsOnByChangeCache =
          perThreadCache.get(DEPENDS_ON_BY_CHANGE_CACHE_KEY, DependsOnByChangeCache::new);
      return dependsOnByChangeCache.computeIfAbsent(
          changeNotes.getChangeId(), (id) -> loadWithOrderFromNoteDb(changeNotes));
    }
    return loadWithOrderFromNoteDb(changeNotes);
  }

  private List<DependsOn> loadWithOrderFromNoteDb(ChangeNotes changeNotes) throws StorageException {
    for (ChangeMessage message : Lists.reverse(cmUtil.byChange(changeNotes))) {
      Optional<List<DependsOn>> deps = Comment.from(message.getMessage());
      if (deps.isPresent()) {
        return deps.get();
      }
    }
    return Collections.emptyList();
  }

  /** If needed, create a comment on the change with a DependsOn for the dependencies. */
  @Override
  public boolean resolveDependencies(ChangeNotes changeNotes, Set<Set<BranchNameKey>> deliverables)
      throws InvalidChangeOperationException, StorageException {
    Set<DependsOn> deps = load(changeNotes);
    if (Resolver.isResolved(deps)) {
      return false;
    }
    Set<DependsOn> resolved = resolver.resolve(deps, deliverables);
    if (resolved.equals(deps)) {
      return false; // Nothing resolved this pass
    }
    // ToDo: add info about the resolved depends-on (deliverable, branch, and ChangeId?)
    store(changeNotes, resolved, "Auto-updating resolved Depends-on");
    return true;
  }

  /** If needed, create a comment on the change with a DependsOn for the dependencies. */
  @Deprecated
  @Override
  public boolean resolveDependencies(PatchSet.Id patchSetId, Set<Set<BranchNameKey>> deliverables)
      throws InvalidChangeOperationException, StorageException {
    return resolveDependencies(
        changeNotesFactory.createCheckedUsingIndexLookup(patchSetId.changeId()), deliverables);
  }

  @Override
  public boolean hasUnresolvedDependsOn(Change.Id changeId) {
    return !Resolver.isResolved(load(changeId));
  }

  /** Create a comment on the change with a DependsOn for the deps. */
  public void store(ChangeNotes changeNotes, Set<DependsOn> deps, String message)
      throws InvalidChangeOperationException, StorageException {
    StringBuilder comment = new StringBuilder();
    if (message != null) {
      comment.append(message + "\n\n");
    }
    comment.append(Comment.getMessages(deps));
    ReviewInput review = new ReviewInput();
    review.message = Strings.emptyToNull(comment.toString());
    ChangeResource changeResource = changeResourceFactory.create(changeNotes, currentUser);
    PatchSet patchSet = changeNotes.getCurrentPatchSet();
    try {
      retryHelper
          .changeUpdate(
              "changeUpdate",
              updateFactory ->
                  reviewProvider
                      .get()
                      .apply(new RevisionResource(changeResource, patchSet), review))
          .call();
    } catch (RestApiException | UpdateException e) {
      log.error("Unable to post auto-copied review comment", e);
    }
  }
}
