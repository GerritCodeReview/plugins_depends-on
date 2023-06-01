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

package com.googlesource.gerrit.plugins.depends.on.formats;

import com.googlesource.gerrit.plugins.depends.on.DependsOn;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
 * Extract DependsOn(s) from comment string using Depends-on pattern.
 * Print DependsOn(s) in its preferred form for comments.
 */
public class Comment {
  protected static final Pattern DEPENDS_ON_PATTERN = Pattern.compile("^Depends-on:(.*)$");

  /** return empty Optional instance means no dependencies found */
  public static Optional<Set<DependsOn>> from(String comment) {
    for (String line : comment.split("\n", -1)) {
      Matcher match = DEPENDS_ON_PATTERN.matcher(line);
      if (match.find()) {
        // Sample: "Depends-on: 1234, 4444"
        String changes = match.group(1); // -> "1234, 4444"
        changes = changes.replace(",", " "); // -> "1234 4444"
        return Optional.of(
            Arrays.stream(changes.split("\\s+", -1)) // -> ["1234", "4444"]
                .filter(c -> !c.isEmpty())
                .map(c -> DependsOn.create(c))
                .collect(Collectors.toSet()));
      }
    }
    return Optional.empty();
  }

  public static boolean hasDependsOn(String comment) {
    for (String line : comment.split("\n", -1)) {
      if (DEPENDS_ON_PATTERN.matcher(line).find()) {
        return true;
      }
    }
    return false;
  }

  public static StringBuilder getMessages(Set<DependsOn> dependsons) {
    StringBuilder dependencies = new StringBuilder("Depends-on:");
    for (DependsOn dep : dependsons) {
      dependencies.append(" " + getMessage(dep));
    }
    return dependencies;
  }

  /**
   * Print a DependsOn in its preferred form for comments. The preferred form is generally the most
   * precise form currently supported based on the data available in the DependsOn.
   */
  public static String getMessage(DependsOn dependency) {
    if (dependency.isResolved()) {
      return String.valueOf(dependency.id().get());
    }
    return dependency.key().get();
  }
}
