package io.cogswell.sdk.subscription;

import org.json.JSONObject;

import io.cogswell.sdk.exceptions.CogsBuilderException;

/**
 * Created by jedwards on 5/3/16.
 *
 * This class is a request used in order to establish a WebSocket via the
 * Cogs GET /push route.
 *
 * Use the builder() method in order to acquire a builder instance.
 */
public class CogsSubscriptionRequest {
    private String accessKey;
    private String clientSalt;
    private String clientSecret;

    private CogsSubscription subscription;

    private CogsSubscriptionRequest() {}

    public String getAccessKey() {
        return accessKey;
    }

    public String getClientSalt() {
        return clientSalt;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getNamespace() {
        return subscription == null ? null : subscription.getNamespace();
    }

    public JSONObject getTopicAttributes() {
        return subscription == null ? null : subscription.getTopicAttributes();
    }

    public CogsSubscription getSubscription() {
       return subscription;
    }

    public CogsSubscriptionRequestBuilder builder() {
        return new CogsSubscriptionRequestBuilder();
    }

    public class CogsSubscriptionRequestBuilder {
        CogsSubscriptionRequest request;

        private CogsSubscriptionRequestBuilder() {
            request = new CogsSubscriptionRequest();
        }

        /**
         * This is a required field.
         *
         * @param accessKey the identity of the api key which should be used to authenticate
         *                  this request. The client salt/secret pair must be associated with
         *                  this api key.
         *
         * @return this {@link CogsSubscriptionRequestBuilder builder}
         */
        public CogsSubscriptionRequestBuilder withAccessKey(String accessKey) {
            request.accessKey = accessKey;
            return this;
        }

        /**
         * This is a required field.
         *
         * @param clientSalt the client salt used to verify client authenticity
         *
         * @return this {@link CogsSubscriptionRequestBuilder builder}
         */
        public CogsSubscriptionRequestBuilder withClientSalt(String clientSalt) {
            request.clientSalt = clientSalt;
            return this;
        }

        /**
         * This is a required field.
         *
         * @param clientSecret the client secret used to sign the request
         *
         * @return this {@link CogsSubscriptionRequestBuilder builder}
         */
        public CogsSubscriptionRequestBuilder withClientSecret(String clientSecret) {
            request.clientSecret = clientSecret;
            return this;
        }

        /**
         * This is a required field.
         *
         * @param namespace the namespace for this subscription
         *
         * @return this {@link CogsSubscriptionRequestBuilder builder}
         */
        public CogsSubscriptionRequestBuilder withNamespace(String namespace) {
            CogsSubscription oldSubscription = request.subscription;
            request.subscription = new CogsSubscription(namespace, oldSubscription == null ? null : oldSubscription.getTopicAttributes());
            return this;
        }

        /**
         *
         * @param topicAttributes the attributes identifying the topic within the specified namespace
         *
         * @return this {@link CogsSubscriptionRequestBuilder builder}
         */
        public CogsSubscriptionRequestBuilder withTopicAttributes(JSONObject topicAttributes) {
            CogsSubscription oldSubscription = request.subscription;
            request.subscription = new CogsSubscription(oldSubscription == null ? null : oldSubscription.getNamespace(), topicAttributes);
            return this;
        }

        /**
         * Build the {@link CogsSubscriptionRequest request}, validating that required
         * attributes have been populated.
         *
         * @return the validated {@link CogsSubscriptionRequest request}
         *
         * @throws CogsBuilderException if any required fields are missing or values are invalid
         */
        public CogsSubscriptionRequest build() throws CogsBuilderException {
            if (request.accessKey == null)
                throw new CogsBuilderException("Access key must not be null.");
            else if (request.accessKey.length() < 1)
                throw new CogsBuilderException("Access key must not be empty.");

            if (request.clientSalt == null)
                throw new CogsBuilderException("Client salt must not be null.");
            else if (request.clientSalt.length() < 1)
                throw new CogsBuilderException("Client salt must not be empty.");

            if (request.clientSecret == null)
                throw new CogsBuilderException("Client secret must not be null.");
            else if (request.clientSecret.length() < 1)
                throw new CogsBuilderException("Client secret must not be empty.");

            if (request.subscription == null) {
                throw new CogsBuilderException("Namespace and attributes must not be null.");
            } else {
                if (request.subscription.getNamespace() == null)
                    throw new CogsBuilderException("Namespace must not be null.");
                else if (request.subscription.getNamespace().length() < 1)
                    throw new CogsBuilderException("Namespace must not be empty.");

                if (request.subscription.getTopicAttributes() == null)
                    throw new CogsBuilderException("Topic attributes must not be null.");
                if (request.subscription.getTopicAttributes().length() < 1)
                    throw new CogsBuilderException("Topic attributes must not be empty");
            }

            return request;
        }
    }
}
