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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

/**
 * Receives AJO push payloads that carry a {@link LiveUpdateEnvelope}, along with the
 * already-populated {@link NotificationCompat.Builder} Messaging would otherwise post directly.
 *
 * <p>Implemented by the Live Updates SDK (in {@code com.adobe.marketing.mobile.messaging.liveupdate})
 * and registered with Messaging via {@link Messaging#setLiveUpdateHandler(LiveUpdateHandler)}.
 *
 * <p>The implementation is expected to apply Live-Update-specific bits to the builder
 * (e.g. {@code setStyle}, {@code setRequestPromotedOngoing(true)}, {@code setOngoing(true)},
 * {@code setShortCriticalText}, {@code setTimeoutAfter}), set the notification id from
 * {@link LiveUpdateEnvelope#getId()}, and post via
 * {@link androidx.core.app.NotificationManagerCompat}.
 */
public interface LiveUpdateHandler {

    /**
     * Handles a Live Update push. Invoked on the FCM background thread.
     *
     * @param context the application {@link Context}
     * @param builder the {@link NotificationCompat.Builder} pre-populated by
     *     {@code MessagingPushBuilder} (title, body, small icon, click/delete intents, channel,
     *     action buttons, sound, visibility)
     * @param payload the parsed {@link MessagingPushPayload}; the Live Update envelope is at
     *     {@link MessagingPushPayload#getLiveUpdate()}
     * @return {@code true} when this handler posted the notification (or otherwise completed
     *     handling); {@code false} to ask Messaging to render the push via its default path
     *     instead. Returning {@code false} is the right choice when the handler cannot derive a
     *     valid Live Update (missing id, app-side style provider returned null, etc.).
     */
    boolean handleLiveUpdatePush(
            @NonNull Context context,
            @NonNull NotificationCompat.Builder builder,
            @NonNull MessagingPushPayload payload);
}
