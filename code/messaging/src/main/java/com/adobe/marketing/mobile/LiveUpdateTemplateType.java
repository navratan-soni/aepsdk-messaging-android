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
 * Canonical {@code live_update_template_type} values. The SDK does not validate against
 * this set — the app's {@code LiveUpdateStyleProvider} maps the string to a concrete
 * {@code NotificationCompat.Style} subclass. These constants exist so server templates
 * and app code agree on the canonical spellings.
 */
public final class LiveUpdateTemplateType {

    /** {@code NotificationCompat.ProgressStyle} — API 36+. */
    public static final String PROGRESS = "progress";

    /** No special style; renders as a plain ongoing notification. Default fallback. */
    public static final String STANDARD = "standard";

    /** {@code NotificationCompat.CallStyle} — API 31+, promotion-eligible from API 36+. */
    public static final String CALL = "call";

    /** {@code NotificationCompat.MetricStyle} — API 37+. */
    public static final String METRIC = "metric";

    /** {@code NotificationCompat.BigTextStyle}. */
    public static final String BIG_TEXT = "big_text";

    private LiveUpdateTemplateType() {}
}
