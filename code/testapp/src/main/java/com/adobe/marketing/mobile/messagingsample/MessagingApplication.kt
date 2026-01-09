/*
 Copyright 2021 Adobe. All rights reserved.
 This file is licensed to you under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software distributed under
 the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 OF ANY KIND, either express or implied. See the License for the specific language
 governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile.messagingsample

import android.app.Application
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.Messaging
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.Lifecycle
import com.adobe.marketing.mobile.edge.identity.AuthenticatedState
import com.adobe.marketing.mobile.edge.identity.Identity
import com.adobe.marketing.mobile.edge.identity.IdentityItem
import com.adobe.marketing.mobile.edge.identity.IdentityMap

class MessagingApplication : Application() {
    private val ENVIRONMENT_FILE_ID = "staging/1b50a869c4a2/b565757407ab/launch-b0c521d31444"

       // "3149c49c3910/4f6b2fbf2986/launch-7d78a5fd1de3-development"
    private val ASSURANCE_SESSION_ID = "exdTest://?adb_validation_sessionid=704df49b-70a3-42c6-925d-ae3a5288a26d&env=qa"
    private val STAGING_APP_ID = "staging/1b50a869c4a2/b565757407ab/launch-b0c521d31444"
       // "staging/1b50a869c4a2/cfad4f117814/launch-cd2deff4f4f6"
    private val STAGING = true

    override fun onCreate() {
        super.onCreate()

        MobileCore.setApplication(this)
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val extensions = listOf(Messaging.EXTENSION, Identity.EXTENSION, Lifecycle.EXTENSION, Edge.EXTENSION, Assurance.EXTENSION)
        MobileCore.registerExtensions(extensions) {
            // Necessary property id which has the edge configuration id needed by aep sdk
            if (STAGING) {
                MobileCore.configureWithAppID(STAGING_APP_ID)
                MobileCore.updateConfiguration(
                    hashMapOf("edge.environment" to "int") as Map<String, Any>)
            } else {
                MobileCore.configureWithAppID(ENVIRONMENT_FILE_ID)
            }
            MobileCore.lifecycleStart(null)

            val configMap = mapOf(
                "messaging.optimizePushSync" to false
            )
            MobileCore.updateConfiguration(configMap)
        }

        val emailIdentity = IdentityItem(
            "navratan_exd1@adobe.com",
        )
        val identityMap = IdentityMap()
        identityMap.addItem(emailIdentity, "Email")

        Identity.updateIdentities(identityMap)

        Assurance.startSession(ASSURANCE_SESSION_ID)
    }
}