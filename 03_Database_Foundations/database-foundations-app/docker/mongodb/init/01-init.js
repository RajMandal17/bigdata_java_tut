// MongoDB initialization script for Big Data Foundations
// This script creates collections, indexes, and sample data

// Switch to bigdata_events database
db = db.getSiblingDB('bigdata_events');

// Create events collection with validation schema
db.createCollection("events", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["eventId", "userId", "eventType", "timestamp"],
         properties: {
            eventId: {
               bsonType: "string",
               description: "must be a string and is required"
            },
            userId: {
               bsonType: "string",
               description: "must be a string and is required"
            },
            eventType: {
               bsonType: "string",
               enum: ["purchase", "view", "click", "search", "login", "logout", "signup", "cart_add", "cart_remove", "checkout"],
               description: "must be a string from predefined list and is required"
            },
            timestamp: {
               bsonType: "date",
               description: "must be a date and is required"
            },
            data: {
               bsonType: "object",
               description: "event specific data"
            },
            metadata: {
               bsonType: "object",
               description: "additional metadata"
            },
            tags: {
               bsonType: "array",
               items: {
                  bsonType: "string"
               },
               description: "array of tags"
            },
            processed: {
               bsonType: "bool",
               description: "processing status"
            }
         }
      }
   }
});

// Create user_sessions collection
db.createCollection("user_sessions", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["sessionId", "userId", "startTime"],
         properties: {
            sessionId: {
               bsonType: "string",
               description: "must be a string and is required"
            },
            userId: {
               bsonType: "string",
               description: "must be a string and is required"
            },
            startTime: {
               bsonType: "date",
               description: "must be a date and is required"
            },
            endTime: {
               bsonType: "date",
               description: "session end time"
            },
            duration: {
               bsonType: "int",
               description: "session duration in seconds"
            },
            device: {
               bsonType: "object",
               description: "device information"
            },
            location: {
               bsonType: "object",
               description: "geographic location"
            },
            active: {
               bsonType: "bool",
               description: "session active status"
            }
         }
      }
   }
});

// Create indexes for events collection
db.events.createIndex({ "eventId": 1 }, { unique: true });
db.events.createIndex({ "userId": 1, "timestamp": -1 });
db.events.createIndex({ "eventType": 1, "processed": 1, "timestamp": -1 });
db.events.createIndex({ "data.category": 1, "timestamp": -1 });
db.events.createIndex({ "data.productId": 1 });
db.events.createIndex({ "metadata.source": 1 });
db.events.createIndex({ "tags": 1 });

// Create compound indexes for analytics
db.events.createIndex({ 
    "eventType": 1, 
    "data.category": 1, 
    "timestamp": -1 
});

// Create text index for search
db.events.createIndex({ 
    "data.productName": "text", 
    "data.description": "text",
    "data.searchQuery": "text"
});

// Create geospatial index
db.events.createIndex({ "metadata.location": "2dsphere" });

// Create TTL index for auto-deletion (90 days)
db.events.createIndex(
    { "timestamp": 1 }, 
    { expireAfterSeconds: 7776000 }
);

// Create indexes for user_sessions collection
db.user_sessions.createIndex({ "sessionId": 1 }, { unique: true });
db.user_sessions.createIndex({ "userId": 1, "startTime": -1 });
db.user_sessions.createIndex({ "device.type": 1 });
db.user_sessions.createIndex({ "location": "2dsphere" });
db.user_sessions.createIndex({ "active": 1 });

// Insert sample events data
var sampleEvents = [
    {
        "eventId": "evt_001",
        "userId": "CUST001",
        "eventType": "login",
        "timestamp": new Date("2024-07-20T08:00:00Z"),
        "data": {
            "source": "mobile_app",
            "version": "2.1.0"
        },
        "metadata": {
            "userAgent": "BigDataApp/2.1.0 (iOS 17.0)",
            "ipAddress": "192.168.1.100",
            "location": {
                "type": "Point",
                "coordinates": [-73.856077, 40.848447]
            }
        },
        "tags": ["mobile", "ios"],
        "processed": false
    },
    {
        "eventId": "evt_002",
        "userId": "CUST001",
        "eventType": "view",
        "timestamp": new Date("2024-07-20T08:05:00Z"),
        "data": {
            "productId": "PROD_001",
            "productName": "Gaming Console",
            "category": "electronics",
            "price": 599.99,
            "pageUrl": "/products/gaming-console"
        },
        "metadata": {
            "source": "mobile_app",
            "sessionId": "sess_001",
            "referrer": "homepage"
        },
        "tags": ["electronics", "gaming"],
        "processed": false
    },
    {
        "eventId": "evt_003",
        "userId": "CUST001",
        "eventType": "cart_add",
        "timestamp": new Date("2024-07-20T08:10:00Z"),
        "data": {
            "productId": "PROD_001",
            "productName": "Gaming Console",
            "category": "electronics",
            "price": 599.99,
            "quantity": 1
        },
        "metadata": {
            "source": "mobile_app",
            "sessionId": "sess_001"
        },
        "tags": ["electronics", "gaming", "cart"],
        "processed": false
    },
    {
        "eventId": "evt_004",
        "userId": "CUST001",
        "eventType": "purchase",
        "timestamp": new Date("2024-07-20T08:15:00Z"),
        "data": {
            "transactionId": "TXN_001",
            "productId": "PROD_001",
            "productName": "Gaming Console",
            "category": "electronics",
            "price": 599.99,
            "quantity": 1,
            "totalAmount": 599.99,
            "paymentMethod": "credit_card"
        },
        "metadata": {
            "source": "mobile_app",
            "sessionId": "sess_001",
            "location": {
                "type": "Point",
                "coordinates": [-73.856077, 40.848447]
            }
        },
        "tags": ["electronics", "gaming", "purchase", "high_value"],
        "processed": false
    },
    {
        "eventId": "evt_005",
        "userId": "CUST002",
        "eventType": "search",
        "timestamp": new Date("2024-07-20T09:00:00Z"),
        "data": {
            "searchQuery": "wireless headphones",
            "category": "electronics",
            "resultsCount": 25,
            "filters": {
                "priceRange": "50-200",
                "brand": "any",
                "rating": "4+"
            }
        },
        "metadata": {
            "source": "web_app",
            "sessionId": "sess_002",
            "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
        },
        "tags": ["search", "electronics"],
        "processed": false
    },
    {
        "eventId": "evt_006",
        "userId": "CUST002",
        "eventType": "click",
        "timestamp": new Date("2024-07-20T09:05:00Z"),
        "data": {
            "elementType": "product_card",
            "productId": "PROD_002",
            "productName": "Wireless Headphones",
            "category": "electronics",
            "position": 3,
            "listType": "search_results"
        },
        "metadata": {
            "source": "web_app",
            "sessionId": "sess_002",
            "pageUrl": "/search?q=wireless+headphones"
        },
        "tags": ["click", "electronics", "search_result"],
        "processed": false
    },
    {
        "eventId": "evt_007",
        "userId": "CUST003",
        "eventType": "signup",
        "timestamp": new Date("2024-07-20T10:00:00Z"),
        "data": {
            "registrationMethod": "email",
            "source": "organic",
            "referralCode": null
        },
        "metadata": {
            "source": "web_app",
            "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
            "location": {
                "type": "Point",
                "coordinates": [-122.4194, 37.7749]
            }
        },
        "tags": ["signup", "new_user"],
        "processed": false
    },
    {
        "eventId": "evt_008",
        "userId": "CUST004",
        "eventType": "view",
        "timestamp": new Date("2024-07-20T11:00:00Z"),
        "data": {
            "productId": "PROD_003",
            "productName": "Fitness Tracker",
            "category": "health",
            "price": 199.99,
            "pageUrl": "/products/fitness-tracker"
        },
        "metadata": {
            "source": "mobile_app",
            "sessionId": "sess_003"
        },
        "tags": ["health", "fitness"],
        "processed": false
    }
];

// Insert the sample events
db.events.insertMany(sampleEvents);

// Insert sample user sessions
var sampleSessions = [
    {
        "sessionId": "sess_001",
        "userId": "CUST001",
        "startTime": new Date("2024-07-20T08:00:00Z"),
        "endTime": new Date("2024-07-20T08:30:00Z"),
        "duration": 1800,
        "device": {
            "type": "mobile",
            "os": "iOS",
            "version": "17.0",
            "model": "iPhone 14"
        },
        "location": {
            "type": "Point",
            "coordinates": [-73.856077, 40.848447]
        },
        "active": false
    },
    {
        "sessionId": "sess_002",
        "userId": "CUST002",
        "startTime": new Date("2024-07-20T09:00:00Z"),
        "endTime": null,
        "duration": null,
        "device": {
            "type": "desktop",
            "os": "Windows",
            "version": "11",
            "browser": "Chrome"
        },
        "location": {
            "type": "Point",
            "coordinates": [-74.0060, 40.7128]
        },
        "active": true
    },
    {
        "sessionId": "sess_003",
        "userId": "CUST004",
        "startTime": new Date("2024-07-20T11:00:00Z"),
        "endTime": new Date("2024-07-20T11:15:00Z"),
        "duration": 900,
        "device": {
            "type": "mobile",
            "os": "Android",
            "version": "14",
            "model": "Samsung Galaxy S23"
        },
        "location": {
            "type": "Point",
            "coordinates": [-122.4194, 37.7749]
        },
        "active": false
    }
];

// Insert the sample sessions
db.user_sessions.insertMany(sampleSessions);

// Create aggregation view for event analytics
db.createView("event_analytics", "events", [
    {
        $group: {
            _id: {
                date: { $dateToString: { format: "%Y-%m-%d", date: "$timestamp" } },
                eventType: "$eventType"
            },
            count: { $sum: 1 },
            uniqueUsers: { $addToSet: "$userId" }
        }
    },
    {
        $addFields: {
            uniqueUserCount: { $size: "$uniqueUsers" }
        }
    },
    {
        $project: {
            uniqueUsers: 0
        }
    },
    {
        $sort: {
            "_id.date": -1,
            "_id.eventType": 1
        }
    }
]);

// Print collection statistics
print("=== MongoDB Collections Created ===");
print("Events collection: " + db.events.countDocuments() + " documents");
print("User sessions collection: " + db.user_sessions.countDocuments() + " documents");
print("Indexes created on events: " + db.events.getIndexes().length);
print("Indexes created on user_sessions: " + db.user_sessions.getIndexes().length);

// Print sample data
print("\n=== Sample Events ===");
db.events.find().limit(3).forEach(printjson);

print("\n=== Sample Sessions ===");
db.user_sessions.find().limit(2).forEach(printjson);
