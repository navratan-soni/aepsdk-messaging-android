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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adobe.marketing.mobile.messaging.MessagingConstants;
import com.adobe.marketing.mobile.services.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parsed Live Update envelope from an AJO push payload. Exposed via {@link
 * MessagingPushPayload#getLiveUpdate()}.
 */
public final class LiveUpdateEnvelope {

    private static final String SELF_TAG = "LiveUpdateEnvelope";

    // Envelope keys (live inside the JSON string at adb_live_update_data)
    private static final String KEY_ID = "live_update_id";
    private static final String KEY_TEMPLATE_TYPE = "live_update_template_type";
    private static final String KEY_EVENT = "live_update_event";
    private static final String KEY_DISMISS_AFTER = "live_update_dismiss_after";
    private static final String KEY_CRITICAL_TEXT = "live_update_critical_text";
    private static final String KEY_CONTENT_STATE = "live_update_content_state";

    // Defaults
    private static final String DEFAULT_TEMPLATE_TYPE = LiveUpdateTemplateType.STANDARD;
    private static final String DEFAULT_EVENT = LiveUpdateEvent.UPDATE;

    private final String id;
    private final String templateType;
    private final String event;
    private final Long dismissAfter;
    private final String criticalText;
    private final JSONObject contentState;

    private LiveUpdateEnvelope(
            final String id,
            final String templateType,
            final String event,
            final Long dismissAfter,
            final String criticalText,
            final JSONObject contentState) {
        this.id = id;
        this.templateType = templateType;
        this.event = event;
        this.dismissAfter = dismissAfter;
        this.criticalText = criticalText;
        this.contentState = contentState;
    }

    /**
     * Parses an envelope from the JSON string at {@link
     * MessagingConstants.Push.PayloadKeys#LIVE_UPDATE_DATA}. Returns {@code null} when the
     * input is null, empty, unparseable, or missing the required {@code live_update_id}.
     */
    static @Nullable LiveUpdateEnvelope parse(@Nullable final String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            final JSONObject obj = new JSONObject(json);
            final String id = obj.optString(KEY_ID, null);
            if (id == null || id.isEmpty()) {
                Log.debug(
                        MessagingConstants.LOG_TAG,
                        SELF_TAG,
                        "adb_live_update_data missing required field '" + KEY_ID + "'");
                return null;
            }
            final String templateType = obj.optString(KEY_TEMPLATE_TYPE, DEFAULT_TEMPLATE_TYPE);
            final String event = obj.optString(KEY_EVENT, DEFAULT_EVENT);
            final Long dismissAfter =
                    obj.has(KEY_DISMISS_AFTER) ? obj.optLong(KEY_DISMISS_AFTER) : null;
            final String criticalText = obj.optString(KEY_CRITICAL_TEXT, null);
            final JSONObject contentState = obj.optJSONObject(KEY_CONTENT_STATE);
            return new LiveUpdateEnvelope(
                    id, templateType, event, dismissAfter, criticalText, contentState);
        } catch (final JSONException e) {
            Log.debug(
                    MessagingConstants.LOG_TAG,
                    SELF_TAG,
                    "Unable to parse adb_live_update_data: " + e.getLocalizedMessage());
            return null;
        }
    }

    /** Stable id. Mirrors iOS Live Activity ID. Used as {@code id.hashCode()} for notification id. */
    public @NonNull String getId() {
        return id;
    }

    /**
     * Which Android Style class to render. See {@link LiveUpdateTemplateType} for canonical
     * values. Defaults to {@link LiveUpdateTemplateType#STANDARD} when absent.
     */
    public @NonNull String getTemplateType() {
        return templateType;
    }

    /**
     * Lifecycle event. See {@link LiveUpdateEvent} for canonical values. Defaults to
     * {@link LiveUpdateEvent#UPDATE} when absent.
     */
    public @NonNull String getEvent() {
        return event;
    }

    /**
     * Relative dismissal delay in seconds, applied after the notification is posted. Only
     * meaningful on {@link LiveUpdateEvent#END}. Drives {@code Builder.setTimeoutAfter(...)}.
     */
    public @Nullable Long getDismissAfter() {
        return dismissAfter;
    }

    /** Short text shown in the status bar chip. Drives {@code Builder.setShortCriticalText(...)}. */
    public @Nullable String getCriticalText() {
        return criticalText;
    }

    /**
     * App-defined dynamic state object. The SDK does not interpret this — it is the app's
     * {@code LiveUpdateStyleProvider}'s job to parse and render from it.
     */
    public @Nullable JSONObject getContentState() {
        return contentState;
    }
}
