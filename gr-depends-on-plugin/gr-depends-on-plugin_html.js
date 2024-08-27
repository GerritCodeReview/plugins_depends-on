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
  <style include="gr-change-metadata-shared-styles">
    a {
      color: var(--link-color);
    }
    section .title,
    section .value {
      padding-top: var(--spacing-s);
    }
    .depends_on_info {
      display: inline;
      padding-right: 5px;
    }
    .value iron-icon {
      color: inherit;
      --iron-icon-height: 18px;
      --iron-icon-width: 18px;
    }
    .title {
      color: var(--deemphasized-text-color);
      display: table-cell;
      vertical-align: top;
      max-width: 20em;
      min-width: 7em;
      padding-left: var(--metadata-horizontal-padding);
    }
    .value {
      display: table-cell;
      vertical-align: top;
      white-space: normal;
      max-width: 325px;
    }
    .error {
      color: #FFA62F;
    }
    .elipses {
      color: #0654ac;
      cursor: pointer;
      text-decoration: underline;
    }
  </style>
  <section>
    <span class="title">
      <gr-tooltip-content
        has-tooltip
        title="List of changes that the current change depends on."
      >
        Depends-on-changes
      </gr-tooltip-content>
    </span>
    <span class="value">
      <span hidden$="[[!_isPending]]">loading...</span>
      <span hidden$="[[_isPending]]">
        <span class="error" hidden$="[[!_hasError]]">Error loading</span>
        <span hidden$="[[_hasError]]">
          <template id="dependsOns" is="dom-repeat" indexAs="index" as="dependsOn"
              items="[[_dependsOns]]">
            <template is="dom-if" if="[[_canShow(_isExpanded, index)]]">
              <span class="depends_on_info">
                <a href=/#/q/change:[[dependsOn.name]],n,z>[[dependsOn.name]]</a>
              </span>
            </template>
          </template>
          <span class="elipses" hidden$="[[!_canShowElipses(_isExpanded, _dependsOns)]]"
              on-click="_expand">
            ...([[_getRemainingDependsOnsCount(_dependsOns)]])
          </span>
          <gr-depends-on-edit _change-number="[[_changeNumber]]" _revision="[[_revision]]"
              _value="[[_getEditText(_dependsOns)]]"
              _original-value="[[_getEditText(_dependsOns)]]" _plugin="[[plugin]]"/>
        </span>
      </span>
    </span>
  </section>
`;
