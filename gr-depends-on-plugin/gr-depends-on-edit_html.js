/**
 * @license
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export const htmlTemplate = Polymer.html`
  <style>
    .title {
      vertical-align: top;
    }
    iron-icon {
      height: 18px;
      width: 18px;
      color: var(--link-color);
    }
    .edit_button {
      height: 17px;
      width: 17px;
      min-width: 17px;
      padding: 0px;
      position: var(--layout-fit_-_position);
    }
    .text_area {
      min-height: 100px;
      max-height: 150px;
    }
    .depends_on_edit {
      display: inline;
    }
  </style>
  <div class="depends_on_edit">
    <paper-button class="edit_button" on-click="_openEditDialog">
      <iron-icon icon="gr-icons:edit"/>
    </paper-button>
    <gr-overlay id="edit_dialog_overlay" with-backdrop>
      <gr-dialog id="edit_dialog" confirm-label="Save"
          on-confirm="_saveDependsOn" on-cancel="_closeEditDialog">
        <div class="header" slot="header">Depends-on:</div>
        <div class="main" slot="main">
          <iron-autogrow-textarea on-keypress="_onKeyPressListener" class="text_area"
              autocomplete="off" focused=true bind-value="{{_value}}"/>
        </div>
      </gr-dialog>
    </gr-overlay>
  </div>
`;
