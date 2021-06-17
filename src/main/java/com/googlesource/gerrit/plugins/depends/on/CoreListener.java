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

import com.google.gerrit.entities.Change;
import com.google.gerrit.entities.Change.Id;
import com.google.gerrit.server.data.ChangeAttribute;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.EventListener;
import com.google.gerrit.server.events.PatchSetCreatedEvent;
import com.google.gerrit.server.notedb.ChangeNotes;
import com.google.gerrit.server.project.InvalidChangeOperationException;
import com.google.gerrit.server.project.NoSuchChangeException;
import com.google.inject.Inject;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreListener implements EventListener {
  private static final Logger log = LoggerFactory.getLogger(CoreListener.class);

  protected final Propagator propagator;
  protected final ChangeNotes.Factory changeNotesFactory;

  @Inject
  public CoreListener(Propagator propagator, ChangeNotes.Factory changeNotesFactory) {
    this.propagator = propagator;
    this.changeNotesFactory = changeNotesFactory;
  }

  @Override
  public void onEvent(Event event) {
    if (event instanceof PatchSetCreatedEvent) {
      PatchSetCreatedEvent patchSetCreatedEvent = (PatchSetCreatedEvent) event;
      ChangeAttribute change = patchSetCreatedEvent.change.get();
      if (change.cherryPickOfChange != null && patchSetCreatedEvent.patchSet.get().number == 1) {
        try {
          Optional<Id> sourceId = Change.Id.tryParse(change.cherryPickOfChange.toString());
          Optional<Id> destId = Change.Id.tryParse(Integer.toString(change.number));
          if (sourceId.isPresent() && destId.isPresent()) {
            Change sourceChange =
                changeNotesFactory.createChecked(sourceId.get()).getChange();
            Change destChange =
                changeNotesFactory.createChecked(destId.get()).getChange();
            propagator.propagateFromSourceToDestination(sourceChange, destChange);
          }
        } catch (InvalidChangeOperationException | NoSuchChangeException e) {
          log.error("Unable to propagate dependencies", e);
        }
      }
    }
  }
}
