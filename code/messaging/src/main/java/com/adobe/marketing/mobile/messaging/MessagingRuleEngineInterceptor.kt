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

import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRule
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEngine

class MessagingRuleEngineInterceptor : LaunchRulesEngine.RuleReevaluationInterceptor {
    override fun onReevaluationTriggered(
        event: Event?,
        revaluableRules: List<LaunchRule?>?,
        callback: LaunchRulesEngine.CompletionCallback?
    ) {
        refreshMessagesThenProcessEvent(callback)
    }

    private fun refreshMessagesThenProcessEvent(
        callback: LaunchRulesEngine.CompletionCallback?
    ) {
        val eventData: MutableMap<String?, Any?> = HashMap()
        eventData["refreshmessages"] = true
        val refreshMessageEvent =
            Event.Builder(
                "Refresh in-app messages",
                EventType.MESSAGING,
                EventSource.REQUEST_CONTENT
            )
                .setEventData(eventData)
                .build()

        val updateCallback = AdobeCallback<Boolean> {
            callback?.onComplete()
        }

        MessagingExtension.addCompletionHandler(
            CompletionHandler(
                refreshMessageEvent.uniqueIdentifier,
                updateCallback
            )
        )

        MobileCore.dispatchEvent(refreshMessageEvent)
    }
}
