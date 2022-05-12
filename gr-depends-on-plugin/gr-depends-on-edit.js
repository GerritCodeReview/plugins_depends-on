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

import {htmlTemplate} from './gr-depends-on-edit_html.js';

const DEPENDS_ON_PREFIX = 'Depends-on: ';

class GrDependsOnEdit extends Polymer.Element {
  static get is() {
    return 'gr-depends-on-edit';
  }

  static get template() {
    return htmlTemplate;
  }

  static get properties() {
    return {
      _changeNumber: {
        type: Number,
      },
      _revision: {
        type: String,
      },
      _value: {
        type: String,
      },
      _originalValue: {
        type: String,
      },
      _plugin: {
        type: Object,
      },
    };
  }

  connectedCallback() {
    super.connectedCallback();
  }

  _onKeyPressListener(e) {
    e.stopPropagation();
  }

  _openEditDialog() {
    this._value = this._originalValue;
    this.$.edit_dialog_overlay.open();
    this.$.edit_dialog.classList.toggle('invisible', false);
  }

  _closeEditDialog() {
    this.$.edit_dialog.classList.toggle('invisible', true);
    this.$.edit_dialog_overlay.close();
  }

  _saveDependsOn() {
    this._closeEditDialog();
    const endpoint = `/changes/${this._changeNumber}` +
        `/revisions/${this._revision}/review`;

    return this._plugin.restApi().post(endpoint,
        {message: DEPENDS_ON_PREFIX + this._value}).then(
        response => {
          location.reload();
        }
    );
  }
}

customElements.define(GrDependsOnEdit.is, GrDependsOnEdit);
