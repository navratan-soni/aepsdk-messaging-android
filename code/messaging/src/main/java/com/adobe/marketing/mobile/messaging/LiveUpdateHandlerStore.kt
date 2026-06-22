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

package com.adobe.marketing.mobile.messaging

import com.adobe.marketing.mobile.ILiveUpdateHandler

/**
 * Holds the currently-registered [ILiveUpdateHandler]. Backed by a `@Volatile` field —
 * no persistence needed; the Live Updates SDK re-registers on every app start.
 */
internal object LiveUpdateHandlerStore {

    @Volatile
    private var handler: ILiveUpdateHandler? = null

    fun setHandler(h: ILiveUpdateHandler?) {
        handler = h
    }

    fun getHandler(): ILiveUpdateHandler? = handler
}
