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

import com.google.gerrit.common.Nullable;
import com.google.gerrit.entities.Change;
import com.google.gerrit.exceptions.StorageException;
import com.google.gerrit.extensions.common.PluginDefinedInfo;
import com.google.gerrit.server.DynamicOptions;
import com.google.gerrit.server.change.ChangePluginDefinedInfoFactory;
import com.google.gerrit.server.query.change.ChangeData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependsOnAttributeFactory implements ChangePluginDefinedInfoFactory {
  private static final Logger log = LoggerFactory.getLogger(DependsOnAttributeFactory.class);
  protected static final String FAILED_TO_LOAD_DEPENDS_ON = "Failed to load Depends-on";

  protected final ChangeMessageStore changeMessageStore;

  @Inject
  DependsOnAttributeFactory(ChangeMessageStore changeMessageStore) {
    this.changeMessageStore = changeMessageStore;
  }

  @Override
  public Map<Change.Id, PluginDefinedInfo> createPluginDefinedInfos(
      Collection<ChangeData> cds, DynamicOptions.BeanProvider beanProvider, String plugin) {
    if (!((Module.MyQueryOptions) beanProvider.getDynamicBean(plugin)).all) {
      return Collections.emptyMap();
    }
    Map<Change.Id, PluginDefinedInfo> dependsOnAttributesByChange = new HashMap<>();
    for (ChangeData changeData : cds) {
      try {
        List<DependsOn> dependsOns = changeMessageStore.loadWithOrder(changeData.getId());
        if (dependsOns.size() > 0) {
          DependsOnPluginAttributes dependsOnPluginAttributes = new DependsOnPluginAttributes();
          dependsOnPluginAttributes.addDependsOns(dependsOns);
          dependsOnAttributesByChange.put(changeData.getId(), dependsOnPluginAttributes);
        }
      } catch (StorageException e) {
        log.error(FAILED_TO_LOAD_DEPENDS_ON, e);
        PluginDefinedInfo pluginDefinedInfo = new PluginDefinedInfo();
        pluginDefinedInfo.message = FAILED_TO_LOAD_DEPENDS_ON;
        dependsOnAttributesByChange.put(changeData.getId(), pluginDefinedInfo);
      }
    }
    return dependsOnAttributesByChange;
  }

  protected static class DependsOnPluginAttributes extends PluginDefinedInfo {
    public List<DependsOnAttribute> dependsOns = new ArrayList<>();

    public void addDependsOns(List<DependsOn> dependsOns) {
      for (DependsOn dependsOn : dependsOns) {
        this.dependsOns.add(new DependsOnAttribute(dependsOn));
      }
    }
  }

  protected static class DependsOnAttribute {
    @Nullable protected Integer changeNumber;
    @Nullable protected String unresolved;

    public DependsOnAttribute(DependsOn dependsOn) {
      if (dependsOn.isResolved()) {
        changeNumber = dependsOn.id().get();
      } else {
        unresolved = dependsOn.key().get();
      }
    }
  }
}
