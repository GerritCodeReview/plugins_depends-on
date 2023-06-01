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

import com.google.common.collect.ImmutableList;
import com.google.gerrit.extensions.validators.CommentForValidation;
import com.google.gerrit.extensions.validators.CommentValidationContext;
import com.google.gerrit.extensions.validators.CommentValidationFailure;
import com.google.gerrit.extensions.validators.CommentValidator;
import com.googlesource.gerrit.plugins.depends.on.formats.Comment;

public class DependsOnCommentValidator implements CommentValidator {

  @Override
  public ImmutableList<CommentValidationFailure> validateComments(
      CommentValidationContext ctx, ImmutableList<CommentForValidation> comments) {
    ImmutableList.Builder<CommentValidationFailure> failures = ImmutableList.builder();
    for (CommentForValidation c : comments) {
      if (!CommentForValidation.CommentType.CHANGE_MESSAGE.equals(c.getType())
          && Comment.hasDependsOn(c.getText())) {
        failures.add(
            c.failValidation(
                "Depends-on tags as a patchset level comment are not"
                    + " supported. See depends-on plugin documentation."));
      }
    }
    return failures.build();
  }
}
