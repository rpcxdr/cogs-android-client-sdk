[ ![Download](https://api.bintray.com/packages/cogswell-io/maven/cogs-android-client-sdk/images/download.svg)](https://bintray.com/cogswell-io/maven/cogs-android-client-sdk/_latestVersion)

## Description
The Android SDK for the Cogs real-time message brokering system.

## Requirements
* Android 4.0.4+
* Android Studio 1.5.0
* Requires GCM enabled application. You can read more about GCM [here](https://developers.google.com/cloud-messaging/)

## Installation
### Manual
* Follow the Android Studio installation instructions appropriate for your platform. http://developer.android.com/sdk/index.html (If you are on Ubuntu 15.04 or later, the only dependencies you need are lib32stdc++6 and lib32z1. Attempting to install some of the others will result in errors)
* Once you have Android Studio installed, you will need to add the ANDROID_HOME environment variable to you profile, giving it the full path to your Android Studio intallation.
* Now you can run either your locally installed gradle or the Gradle Wrapper script (gradlew on Linux and OS X; gradlew.bat on Windows) in order to assemble the SDK: `./gradlew install`
* The .aar file is now installed in your local cache, and can be used for either the example app or your own Android app.

## Usage

You can read the complete documentation [here](https://cogswell.io/docs/android/client-sdk/api/)

## [Code Samples](#code-samples)
You will see the name Gambit throughout our code samples. This was the code name used for Cogs prior to release.

### Preparation for using the Android Client SDK
```java
import io.cogswell.sdk.GambitSDKService;

// Hex encoded access-key from one of your api keys in the Web UI.
String accessKey;

// Hex encoded client salt/secret pair acquired from /client_secret endpoint and
// associated with above access-key.
String clientSalt;
String clientSecret;

// Create and setup the Cogs SDK service
GambitSDKService cogsService = GambitSDKService.getInstance();
```

### POST /event
This API route is used to send an event to Cogs.
```java
// This should contain the current time in ISO-8601 format.
String timestamp;

// The name of the namespace for which the event is destined.
String namespace

// This will be sent along with messages so that you can identify the event which
// "triggered" the message delivery.
String eventName;

// The optional ID of the campaign to which this event is responsing. This can
// either be omitted or set to -1 for no campaign.
Integer campaignId;

// The attributes whose names and types should match the namespace schema.
LinkedHashMap<String, Object> attributes;

GambitRequestEvent.Builder builder = new GambitRequestEvent.Builder(
  accessKey, clientSalt, clientSecret
).setEventName(eventName)
  .setNamespace(namespace)
  .setAttributes(attributes)
  .setCampaignId(campaignId)
  .setForwardAsMessage(true);

Future<io.cogswell.sdk.GambitResponse> future = null;
try {
  future = GambitSDKService.getInstance().sendGambitEvent(builder);
} catch (Exception e) {
  // Handle Exception
}

GambitResponseEvent response;
try {
  response = (GambitResponseEvent) future.get();
  message = response.getMessage();
} catch (InterruptedException | ExecutionException ex) {
  // Handle Exception
}
```

### GET /register_push
This API route is used to register an application for Cogs push notifications.
```java
// An executor service
ExecutorService executor;

// The attributes whose names and types should match the namespace schema.
LinkedHashMap<String, Object> attributes;

// The device UDID
String UDID;

// The push notification environment
// "dev" or "production"
String environment;

// GCM registration token
// See https://developers.google.com/cloud-messaging/registration
String UDID

// Android ApplicationId
String platform_app_id

GambitRequestPush.Builder builder = new GambitRequestPush.Builder(
  accessKey, clientSalt, clientSecret
).setNamespace(namespaceName)
  .setAttributes(attributes)
  .setUDID(UDID)
  .setEnviornment(environment)
  .setPlatform("android")
  .setPlatformAppID(platform_app_id)
  .setMethodName(GambitRequestPush.register);

Future<io.cogswell.sdk.GambitResponse> future = null;
try {
  future = executor.submit(builder.build());
} catch (Exception e) {
  // Handle Exception
}

GambitResponsePush response;
try {
  response = (GambitResponsePush) future.get();
} catch (InterruptedException | ExecutionException ex) {
  // Handle Exception
}
```

### DELETE /unregister_push
This API route is used to unregister an application from Cogs push notifications.
```java
// An executor service
ExecutorService executor;

// The attributes whose names and types should match the namespace schema.
LinkedHashMap<String, Object> attributes;

// The device UDID
String UDID;

// The push notification environment
// "dev" or "production"
String environment;

// GCM registration token
// See https://developers.google.com/cloud-messaging/registration
String UDID

// Android ApplicationId
String platform_app_id

GambitRequestPush.Builder builder = new GambitRequestPush.Builder(
  accessKey, clientSalt, clientSecret
).setNamespace(namespaceName)
  .setAttributes(attributes)
  .setUDID(UDID)
  .setEnviornment(environment)
  .setPlatform("android")
  .setPlatformAppID(platform_app_id)
  .setMethodName(GambitRequestPush.unregister);

Future<io.cogswell.sdk.GambitResponse> future = null;
try {
  future = executor.submit(builder.build());
} catch (Exception e) {
  // Handle Exception
}

GambitResponsePush response;
try {
  response = (GambitResponsePush) future.get();
} catch (InterruptedException | ExecutionException ex) {
  // Handle Exception
}
```

### GET /message/{token}
This API route is used to fetch message content for a Cogs push notification.
```java
// An executor service
ExecutorService executor;

// The attributes whose names and types should match the namespace schema.
LinkedHashMap<String, Object> attributes;

// The message id token returned by the notification
String token;

GambitRequestMessage.Builder builder = new GambitRequestMessage.Builder(
  accessKey, clientSalt, clientSecret
).setNamespace(namespaceName)
  .setAttributes(attributes)
  .setUDID(token);

Future<io.cogswell.sdk.GambitResponse> future = null;
try {
  future = executor.submit(builder.build());
} catch (Exception e) {
  // Handle Exception
}

GambitResponseMessage response;
try {
  response = (GambitResponseMessage) future.get();
} catch (InterruptedException | ExecutionException ex) {
  // Handle Exception
}
```

## Publishing

In order to publish to jcenter, you will need to create a new local.properties file in the root of the project repository, and populate the `user` and `apiKey` fields with the jcenter login credentials.

## License

Copyright 2016 Aviata Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
