/*
  Copyright 2026 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile;

/**
 * Canonical {@code live_update_event} values. Phase 1: only {@link #END} alters
 * post-time behaviour (auto-dismiss via {@code setTimeoutAfter}). Phase 2 will use
 * these for Edge lifecycle telemetry.
 */
public final class LiveUpdateEvent {

    public static final String START = "start";
    public static final String UPDATE = "update";
    public static final String END = "end";

    private LiveUpdateEvent() {}
}
