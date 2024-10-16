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

package com.adobe.marketing.mobile.messaging;

import static com.adobe.marketing.mobile.util.TestHelper.getDispatchedEventsWith;
import static com.adobe.marketing.mobile.util.TestHelper.resetTestExpectations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.adobe.marketing.mobile.Edge;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.Messaging;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.edge.identity.Identity;
import com.adobe.marketing.mobile.util.TestHelper;
import com.adobe.marketing.mobile.util.TestRetryRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MessageCachingFunctionalTests {
    static {
        BuildConfig.IS_E2E_TEST.set(false);
        BuildConfig.IS_FUNCTIONAL_TEST.set(true);
    }

    @Rule
    public RuleChain rule =
            RuleChain.outerRule(new TestHelper.SetupCoreRule())
                    .around(new TestHelper.RegisterMonitorExtensionRule());

    // A test will be retried at most 3 times
    @Rule public TestRetryRule totalTestCount = new TestRetryRule(3);

    MessagingCacheUtilities messagingCacheUtilities = new MessagingCacheUtilities();

    // --------------------------------------------------------------------------------------------
    // Setup and teardown
    // --------------------------------------------------------------------------------------------
    @Before
    public void setup() throws Exception {
        MessagingTestUtils.setEdgeIdentityPersistence(
                MessagingTestUtils.createIdentityMap("ECID", "mockECID"),
                TestHelper.getDefaultApplication());

        final CountDownLatch latch = new CountDownLatch(1);
        final List<Class<? extends Extension>> extensions =
                new ArrayList<Class<? extends Extension>>() {
                    {
                        add(Messaging.EXTENSION);
                        add(Identity.EXTENSION);
                        add(Edge.EXTENSION);
                    }
                };

        MobileCore.registerExtensions(
                extensions,
                o -> {
                    HashMap<String, Object> config =
                            new HashMap<String, Object>() {
                                {
                                    put("messaging.eventDataset", "somedatasetid");
                                    put("edge.configId", "someedgeconfigid");
                                }
                            };
                    MobileCore.updateConfiguration(config);
                    // wait for configuration to be set
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        fail(interruptedException.getMessage());
                    }
                    latch.countDown();
                });

        latch.await(2, TimeUnit.SECONDS);

        // wait for the initial edge personalization request to be made before resetting the monitor
        // extension
        List<Event> dispatchedEvents =
                getDispatchedEventsWith(
                        MessagingTestConstants.EventType.EDGE, EventSource.CONTENT_COMPLETE, 5000);
        assertEquals(1, dispatchedEvents.size());
        resetTestExpectations();

        // ensure cache is cleared before testing
        MessagingTestUtils.cleanCache();

        // write an image file from resources to the image asset cache
        MessagingTestUtils.addImageAssetToCache();
    }

    @After
    public void tearDown() {
        messagingCacheUtilities.clearCachedData();
    }

    @Test
    public void testMessageCaching_CacheThenRetrieveV1Propositions() {
        final Surface surface = new Surface();
        final Map<Surface, List<Proposition>> propositions = new HashMap<>();
        final List<Proposition> propositionList = new ArrayList<>();
        final Map<String, Object> propositionEventData =
                MessagingTestUtils.getMapFromFile("personalizationPayloadV1.json");
        propositionList.add(Proposition.fromEventData(propositionEventData));
        propositions.put(surface, propositionList);
        // add a messaging payload to the cache
        messagingCacheUtilities.cachePropositions(propositions, Collections.EMPTY_LIST);
        // wait for event and rules processing
        TestHelper.sleep(1000);
        // verify message payload was cached
        assertTrue(messagingCacheUtilities.arePropositionsCached());
        final Map<Surface, List<Proposition>> cachedPropositions =
                messagingCacheUtilities.getCachedPropositions();
        final List<Map<String, Object>> expectedPropositions = new ArrayList<>();
        expectedPropositions.add(propositionEventData);
        final String expectedPropositionString =
                MessagingTestUtils.convertPropositionsToString(
                        InternalMessagingUtils.getPropositionsFromPayloads(expectedPropositions));
        assertEquals(
                expectedPropositionString,
                MessagingTestUtils.convertPropositionsToString(cachedPropositions.get(surface)));
    }

    @Test
    public void testMessageCaching_CacheThenRetrieveV2Propositions() {
        final Surface surface = new Surface();
        final Map<Surface, List<Proposition>> propositions = new HashMap<>();
        final List<Proposition> propositionList = new ArrayList<>();
        final Map<String, Object> propositionEventData =
                MessagingTestUtils.getMapFromFile("inappPropositionV2.json");
        propositionList.add(Proposition.fromEventData(propositionEventData));
        propositions.put(surface, propositionList);
        // add a messaging payload to the cache
        messagingCacheUtilities.cachePropositions(propositions, Collections.EMPTY_LIST);
        // wait for event and rules processing
        TestHelper.sleep(1000);
        // verify message payload was cached
        assertTrue(messagingCacheUtilities.arePropositionsCached());
        final Map<Surface, List<Proposition>> cachedPropositions =
                messagingCacheUtilities.getCachedPropositions();
        final List<Map<String, Object>> expectedPropositions = new ArrayList<>();
        expectedPropositions.add(propositionEventData);
        final String expectedPropositionString =
                MessagingTestUtils.convertPropositionsToString(
                        InternalMessagingUtils.getPropositionsFromPayloads(expectedPropositions));
        assertEquals(
                expectedPropositionString,
                MessagingTestUtils.convertPropositionsToString(cachedPropositions.get(surface)));
    }
}
