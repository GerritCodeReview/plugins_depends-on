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

import './gr-depends-on-edit.js';
import {htmlTemplate} from './gr-depends-on-plugin_html.js';

const ELIPSES_INDEX = 3;

class GrDependsOnPlugin extends Polymer.Element {
  static get is() {
    return 'gr-depends-on-plugin';
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
      _dependsOns: {
        type: Array,
        value() { return []; },
      },
      _hasError: {
        type: Boolean,
        value: false,
      },
      _isPending: {
        type: Boolean,
        value: true,
      },
      _isExpanded: {
        type: Boolean,
        value: false,
      },
    };
  }

  _canShow(isExpanded, index) {
    if (!isExpanded && index >= ELIPSES_INDEX) {
      return false;
    }
    return true;
  }

  _expand() {
    this._isExpanded = true;
  }

  _canShowElipses(isExpanded, dependsOns) {
    return !isExpanded && dependsOns.length > ELIPSES_INDEX;
  }

  _getRemainingDependsOnsCount(dependsOns) {
    return dependsOns.length - ELIPSES_INDEX;
  }

  _getEditText(dependsOns) {
    return dependsOns.map(d => d.name).join(' ');
  }

  connectedCallback() {
    super.connectedCallback();
    this._getDependsOns();
  }

  _getDependsOns() {
    if (!this.change) {
      return;
    }
    this._changeNumber = this.change._number;
    this._isPending = true;
    const endpoint = `/changes/?q=change:${this.change._number}` +
        `&--depends-on--all&o=CURRENT_REVISION`;

    return this.plugin.restApi().get(endpoint).then(response => {
      this._isPending = false;
      if (response && response.length === 1) {
        const change = response[0];
        this._revision=change.current_revision;
        if (change.plugins) {
          const dependsOnPluginInfo =
            change.plugins.find(pluginInfo => pluginInfo.name === 'depends-on');
          if (dependsOnPluginInfo) {
            if (dependsOnPluginInfo.depends_ons) {
              for (const dependsOn of dependsOnPluginInfo.depends_ons) {
                if (dependsOn.change_number) {
                  dependsOn.name = dependsOn.change_number;
                } else {
                  dependsOn.name = dependsOn.unresolved;
                }
              }

              this._dependsOns = dependsOnPluginInfo.depends_ons;
            } else {
              this._hasError = true;
            }
          }
        }
      } else {
        this._hasError = true;
      }
    }).catch(e => {
      this._isPending = false;
      this._hasError = true;
    });
  }

  // plugin change-metadata-items don't flex with core ones as the css layout
  // is broken in core. Until core is fixed, use below logic to set the width
  // of the title. This is fragile though and will likely break when new core
  // metadata types are added or the logic for how they are rendered changes.
  _computeTitleWidthClass() {
    if ((this.change.project.length + this.change.branch.length < 40) ||
        (!!this.change.cherry_pick_of_change &&
            !!this.change.cherry_pick_of_patch_set)) {
      return 'title-long';
    }
    return 'title-short';
  }
}

customElements.define(GrDependsOnPlugin.is, GrDependsOnPlugin);
