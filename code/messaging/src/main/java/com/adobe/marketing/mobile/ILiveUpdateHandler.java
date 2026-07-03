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
import com.google.firebase.messaging.RemoteMessage;

/**
 * Receives AJO Live Update FCM pushes. Implemented by the Live Updates SDK (see
 * {@code com.adobe.marketing.mobile.messaging.liveupdate.LiveUpdateHandlerImpl}) and
 * registered with Messaging via {@link Messaging#setLiveUpdateHandler(ILiveUpdateHandler)}.
 *
 * <p>The implementation is fully responsible for parsing the payload, constructing the
 * {@link androidx.core.app.NotificationCompat.Builder}, applying any Live Update–specific
 * decoration (style, ongoing/promoted flags, critical text, timeout), and posting the
 * resulting notification via {@link androidx.core.app.NotificationManagerCompat}.
 *
 * <p>Messaging dispatches but does not pre-build the notification — when a Live Update is
 * detected and a handler is registered, the raw {@link RemoteMessage} is handed off as-is.
 * If no handler is registered, the push is dropped with a warning log.
 */
public interface ILiveUpdateHandler {

    /**
     * Handles a Live Update push. Invoked on the FCM background thread.
     *
     * @param context the application {@link Context}
     * @param message the raw {@link RemoteMessage} received from Firebase, containing the
     *     {@code adb_liveupdate_data} envelope in its data map
     */
    void handleLiveUpdatePush(@NonNull Context context, @NonNull RemoteMessage message);
}
