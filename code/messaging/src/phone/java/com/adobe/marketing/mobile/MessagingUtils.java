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

package com.adobe.marketing.mobile;

import static com.adobe.marketing.mobile.MessagingConstants.CACHE_NAME;
import static com.adobe.marketing.mobile.MessagingConstants.CACHE_SUBDIRECTORY;
import static com.adobe.marketing.mobile.MessagingConstants.LOG_TAG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class MessagingUtils {
    /* JSON - Map conversion helpers */

    /**
     * Converts provided {@link org.json.JSONObject} into {@link java.util.Map} for any number of levels, which can be used as event data
     * This method is recursive.
     * The elements for which the conversion fails will be skipped.
     *
     * @param jsonObject to be converted
     * @return {@link java.util.Map} containing the elements from the provided json, null if {@code jsonObject} is null
     */
    static Map<String, Object> toMap(final JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            return null;
        }

        Map<String, Object> jsonAsMap = new HashMap<>();
        Iterator<String> keysIterator = jsonObject.keys();

        if (keysIterator == null) return null;

        while (keysIterator.hasNext()) {
            String nextKey = keysIterator.next();
            jsonAsMap.put(nextKey, fromJson(jsonObject.get(nextKey)));
        }

        return jsonAsMap;
    }

    /**
     * Converts provided {@link org.json.JSONObject} into a {@link Map<String, Variant>} for any number of levels, which can be used as event data.
     * This method is recursive.
     * The elements for which the conversion fails will be skipped.
     *
     * @param jsonObject to be converted
     * @return {@code Map<String, Variant>} containing the elements from the provided json, null if {@code jsonObject} is null
     */
    static Map<String, Variant> toVariantMap(final JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            return null;
        }

        Map<String, Variant> jsonAsVariantMap = new HashMap<>();
        Iterator<String> keysIterator = jsonObject.keys();

        if (keysIterator == null) return null;

        while (keysIterator.hasNext()) {
            final String nextKey = keysIterator.next();
            final Object value = fromJson(jsonObject.get(nextKey));
            jsonAsVariantMap.put(nextKey, getVariantValue(value));
        }

        return jsonAsVariantMap;
    }

    static Variant getVariantValue(final Object value) {
        Variant convertedValue;
        if (value instanceof String) {
            convertedValue = StringVariant.fromString((String) value);
        } else if (value instanceof Double) {
            convertedValue = DoubleVariant.fromDouble((Double) value);
        } else if (value instanceof Integer) {
            convertedValue = IntegerVariant.fromInteger((int) value);
        } else if (value instanceof Boolean) {
            convertedValue = BooleanVariant.fromBoolean((boolean) value);
        } else if (value instanceof Long) {
            convertedValue = LongVariant.fromLong((long) value);
        } else if (value instanceof Map) {
            final HashMap<String, Variant> map = new HashMap<>();
            for(Map.Entry entry: ((Map<String, Object>) value).entrySet()) {
                map.put((String) entry.getKey(), getVariantValue(entry.getValue()));
            }
            convertedValue = Variant.fromVariantMap((Map<String, Variant>) map);
        } else if (value instanceof List) {
            final ArrayList<Variant> list = new ArrayList<>();
            for (final Object element: (ArrayList) value) {
                list.add((Variant) getVariantValue(element));
            }
            convertedValue = Variant.fromVariantList(list);
        } else {
            convertedValue = (Variant) value;
        }
        return convertedValue;
    }

    /**
     * Converts provided {@link JSONArray} into {@link List} for any number of levels which can be used as event data
     * This method is recursive.
     * The elements for which the conversion fails will be skipped.
     *
     * @param jsonArray to be converted
     * @return {@link List} containing the elements from the provided json, null if {@code jsonArray} is null
     */
    static List<Object> toList(final JSONArray jsonArray) throws JSONException {
        if (jsonArray == null) {
            return null;
        }

        List<Object> jsonArrayAsList = new ArrayList<>();
        int size = jsonArray.length();

        for (int i = 0; i < size; i++) {
            jsonArrayAsList.add(fromJson(jsonArray.get(i)));
        }

        return jsonArrayAsList;
    }

    // ========================================================================================
    // Event Validation
    // ========================================================================================

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    /**
     * @param event A Generic Identity Request Content {@link Event}.
     * @return {@code boolean} indicating if the passed in event is a generic identity request content event.
     */
    static boolean isGenericIdentityRequestEvent(final Event event) {
        if (event == null || event.getEventData() == null) {
            return false;
        }

        return EventType.GENERIC_IDENTITY.getName().equalsIgnoreCase(event.getType()) &&
                EventSource.REQUEST_CONTENT.getName().equalsIgnoreCase(event.getSource());
    }

    /**
     * @param event A Messaging Request Content {@link Event}.
     * @return {@code boolean} indicating if the passed in event is a messaging request content event.
     */
    static boolean isMessagingRequestContentEvent(final Event event) {
        if (event == null || event.getEventData() == null) {
            return false;
        }

        return MessagingConstants.EventType.MESSAGING.equalsIgnoreCase(event.getType()) &&
                EventSource.REQUEST_CONTENT.getName().equalsIgnoreCase(event.getSource());
    }

    /**
     * @param event A Messaging Request Content {@link Event}.
     * @return {@code boolean} indicating if the passed in event is a message fetch event.
     */
    static boolean isFetchMessagesEvent(final Event event) {
        if (event == null || event.getEventData() == null) {
            return false;
        }

        return MessagingConstants.EventType.MESSAGING.equalsIgnoreCase(event.getType())
                && EventSource.REQUEST_CONTENT.getName().equalsIgnoreCase(event.getSource())
                && event.getEventData().containsKey(MessagingConstants.EventDataKeys.Messaging.REFRESH_MESSAGES);
    }

    /**
     * @param event A Rules Response Content {@link Event}.
     * @return {@code boolean} indicating if the passed in event is a messaging consequence event.
     */
    static boolean isMessagingConsequenceEvent(final Event event) {
        if (event == null || event.getEventData() == null) {
            return false;
        }

        return EventType.RULES_ENGINE.getName().equalsIgnoreCase(event.getType())
                && EventSource.RESPONSE_CONTENT.getName().equalsIgnoreCase(event.getSource())
                && event.getEventData().containsKey(MessagingConstants.EventDataKeys.RulesEngine.CONSEQUENCE_TRIGGERED);
    }

    /**
     * @param event An Edge Personalization Decision {@link Event}.
     * @return {@code boolean} indicating if the passed in event is an edge personalization decision event.
     */
    static boolean isEdgePersonalizationDecisionEvent(final Event event) {
        if (event == null || event.getEventData() == null) {
            return false;
        }

        return MessagingConstants.EventType.EDGE.equalsIgnoreCase(event.getType()) &&
                MessagingConstants.EventSource.PERSONALIZATION_DECISIONS.equalsIgnoreCase(event.getSource());
    }

    // ========================================================================================
    // PlatformServices getters
    // ========================================================================================

    /**
     * @param event A Lifecycle Response Content {@link Event}.
     * @return {@code boolean} indicating if the passed in event is a lifecycle response content start event.
     */
    static boolean isLifecycleStartEvent(final Event event) {
        if (event == null || event.getEventData() == null) {
            return false;
        }

        return EventType.LIFECYCLE.getName().equalsIgnoreCase(event.getType()) &&
                EventSource.RESPONSE_CONTENT.getName().equalsIgnoreCase(event.getSource()) &&
                event.getName().equalsIgnoreCase(MessagingConstants.EventName.LIFECYCLE_START);
    }

    /**
     * Returns the {@link PlatformServices} instance.
     *
     * @return {@code PlatformServices} or null if {@code PlatformServices} are unavailable
     */
    static PlatformServices getPlatformServices() {
        final PlatformServices platformServices = MobileCore.getCore().eventHub.getPlatformServices();

        if (platformServices == null) {
            Log.debug(LOG_TAG,
                    "getPlatformServices - Platform services are not available.");
            return null;
        }

        return platformServices;
    }

    /**
     * Returns platform {@link JsonUtilityService} instance.
     *
     * @return {@code JsonUtilityService} or null if {@link PlatformServices} are unavailable
     */
    static JsonUtilityService getJsonUtilityService() {
        final PlatformServices platformServices = getPlatformServices();

        if (platformServices == null) {
            Log.debug(LOG_TAG,
                    "getJsonUtilityService -  Cannot get JsonUtility Service, Platform services are not available.");
            return null;
        }

        return platformServices.getJsonUtilityService();
    }

    /**
     * Returns the {@link UIService} instance.
     *
     * @return {@code UIService} or null if {@link PlatformServices} are unavailable
     */
    static UIService getUIService() {
        final PlatformServices platformServices = MessagingUtils.getPlatformServices();

        if (platformServices == null) {
            Log.debug(LOG_TAG,
                    "getUIService -  Cannot get UIService, Platform services are not available.");
            return null;
        }

        return platformServices.getUIService();
    }

    // ========================================================================================
    // Cache utilities
    // ========================================================================================
    private static final int STREAM_WRITE_BUFFER_SIZE = 4096;

    static boolean areMessagesCached(final CacheManager cacheManager) {
        if (cacheManager != null) {
            return cacheManager.getFileForCachedURL(CACHE_NAME, CACHE_SUBDIRECTORY, false) != null;
        }
        return false;
    }

    static void cacheRetrievedMessages(final CacheManager cacheManager, final ArrayList<Map<String, Variant>> messagePayload) {
        if (cacheManager != null) {
            // clean any existing cached files first
            cacheManager.deleteCachedDataForURL(CACHE_NAME);
            Log.debug(LOG_TAG, "Creating new cached message definitions at: %s", cacheManager.getBaseFilePath(CACHE_NAME, CACHE_SUBDIRECTORY));
            final Date date = new Date(System.currentTimeMillis());
            final File cachedMessages = cacheManager.createNewCacheFile(CACHE_NAME, CACHE_SUBDIRECTORY, date);
            try {
                final Object json = toJSON(messagePayload);
                readInputStreamIntoFile(cachedMessages, new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8)), false);
            } catch (final JSONException e) {
                Log.error(LOG_TAG, "JSONException while attempting to create JSON from ArrayList payload: (%s)", e);
            }
        }
    }

    private static Object toJSON(final Object object) throws JSONException {
        if (object instanceof HashMap) {
            JSONObject jsonObject = new JSONObject();
            final HashMap map = (HashMap) object;
            for (final Object key : map.keySet()) {
                jsonObject.put(key.toString(), toJSON(map.get(key)));
            }
            return jsonObject;
        } else if (object instanceof Iterable) {
            JSONArray jsonArray = new JSONArray();
            for (final Object value : ((Iterable) object)) {
                jsonArray.put(toJSON(value));
            }
            return jsonArray;
        } else {
            return object;
        }
    }

    /**
     * Writes the inputStream into the file.
     * <p>
     * Will append the content of the inputStream to the existing file if the boolean is set as true.
     *
     * @param cachedFile File to which the content has to be written
     * @param input      Inputstream with json content
     * @param append     true, if you wanna append the input stream to the existing file content
     * @return true if the inputstream has been successfully written into the file
     */
    private static boolean readInputStreamIntoFile(final File cachedFile, final InputStream input, final boolean append) {
        boolean result;

        if (cachedFile == null || input == null) {
            return false;
        }

        FileOutputStream output = null;

        try {
            output = new FileOutputStream(cachedFile, append);
            final byte[] data = new byte[STREAM_WRITE_BUFFER_SIZE];
            int count;

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            result = true;
        } catch (final IOException e) {
            Log.error(LOG_TAG, "IOException while attempting to write remote file (%s)",
                    e);
            return false;
        } catch (final Exception e) {
            Log.error(LOG_TAG, "Unexpected exception while attempting to write remote file (%s)",
                    e);
            return false;
        } finally {
            try {
                if (output != null) {
                    output.close();
                }

            } catch (final Exception e) {
                Log.error(LOG_TAG, "Unable to close the OutputStream (%s) ", e);
            }
        }

        return result;
    }

    static ArrayList<Map<String, Variant>> getCachedMessages(final CacheManager cacheManager) {
        final ArrayList<Map<String, Variant>> payload = new ArrayList<>();

        if (cacheManager != null) {
            final File cachedMessageFile = cacheManager.getFileForCachedURL(CACHE_NAME, CACHE_SUBDIRECTORY, false);
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(cachedMessageFile);
                final String streamContents = StringUtils.streamToString(fileInputStream);
                fileInputStream.close();
                final JSONArray cachedMessagePayload = new JSONArray(streamContents);
                // convert each JSONObject in the payload JSONArray into a VariantMap and add it to an ArrayList
                // for processing in handleOfferNotificationPayload.
                for (int i = 0; i < cachedMessagePayload.length(); i++) {
                    final Map<String, Variant> variantHashMap = toVariantMap(cachedMessagePayload.getJSONObject(i));
                    payload.add(variantHashMap);
                }
            } catch (final FileNotFoundException fileNotFoundException) {
                Log.warning(LOG_TAG, "Exception occurred when retrieving the cached message file: %s", fileNotFoundException.getMessage());
                return null;
            } catch (final IOException ioException) {
                Log.warning(LOG_TAG, "Exception occurred when converting the cached message file to a string: %s", ioException.getMessage());
                return null;
//            } catch (final VariantException variantException) {
//                Log.warning(LOG_TAG, "Exception occurred when creating a VariantMap: %s", variantException.getMessage());
//                return null;
            } catch (final JSONException jsonException) {
                Log.warning(LOG_TAG, "Exception occurred when creating the JSONArray: %s", jsonException.getMessage());
                return null;
            } finally {
                return payload;
            }
        }
        Log.error(LOG_TAG, "CacheManager is null, unable to get cached messages.");
        return null;
    }
}
