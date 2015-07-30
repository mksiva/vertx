/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iot.i4.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ski8cob
 */
public class I4Server extends AbstractVerticle {

    private MongoClient mongo;

    @Override
    public void start() throws Exception {
        // Create a mongo client using all defaults (connect to localhost and default port) using the database name "demo".
        JsonObject config = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "i4");
        mongo = MongoClient.createShared(Vertx.vertx(), config);

        // the load function just populates some data on the storage
        loadData(mongo);

        Router router = Router.router(Vertx.vertx());
        router.route().handler(BodyHandler.create());

        //1.findAll payloads
        router.get("/api/payloads").handler(ctx ->{
            mongo.find("payloads", new JsonObject(), lookup ->{
                // error handling
                if (lookup.failed()) {
                    ctx.fail(500);
                    return;
                }
                
                // now convert the list to a JsonArray because it will be easier to encode the final object as the response.
                final JsonArray json = new JsonArray();
                for (JsonObject o : lookup.result()) {
                    json.add(o);
                }
                ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                ctx.response().end(json.encode());
            });
        });
        
        //1. findAll
        router.get("/api/users").handler(ctx -> {
            mongo.find("users", new JsonObject(), lookup -> {
                // error handling
                if (lookup.failed()) {
                    ctx.fail(500);
                    return;
                }
                // now convert the list to a JsonArray because it will be easier to encode the final object as the response.
                final JsonArray json = new JsonArray();
                for (JsonObject o : lookup.result()) {
                    json.add(o);
                }
                ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                ctx.response().end(json.encode());
            });
        });
         
        //2.findOne
        router.get("/api/users/:id").handler(ctx -> {
        mongo.findOne("users", new JsonObject().put("_id", ctx.request().getParam("id")), null, lookup -> {
        // error handling
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }

        JsonObject user = lookup.result();

        if (user == null) {
          ctx.fail(404);
        } else {
          ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
          ctx.response().end(user.encode());
        }
      });
    });
        
        router.post("/api/users").handler(ctx -> {
      JsonObject newUser = ctx.getBodyAsJson();

      mongo.findOne("users", new JsonObject().put("username", newUser.getString("username")), null, lookup -> {
        // error handling
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }

        JsonObject user = lookup.result();

        if (user != null) {
          // already exists
          ctx.fail(500);
        } else {
          mongo.insert("users", newUser, insert -> {
            // error handling
            if (insert.failed()) {
              ctx.fail(500);
              return;
            }

            // add the generated id to the user object
            newUser.put("_id", insert.result());

            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(newUser.encode());
          });
        }
      });
    });
        
        router.put("/api/users/:id").handler(ctx -> {
      mongo.findOne("users", new JsonObject().put("_id", ctx.request().getParam("id")), null, lookup -> {
        // error handling
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }

        JsonObject user = lookup.result();

        if (user == null) {
          // does not exist
          ctx.fail(404);
        } else {

          // update the user properties
          JsonObject update = ctx.getBodyAsJson();

          user.put("username", update.getString("username"));
          user.put("firstName", update.getString("firstName"));
          user.put("lastName", update.getString("lastName"));
          user.put("address", update.getString("address"));

          mongo.replace("users", new JsonObject().put("_id", ctx.request().getParam("id")), user, replace -> {
            // error handling
            if (replace.failed()) {
              ctx.fail(500);
              return;
            }

            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(user.encode());
          });
        }
      });
    });
        
        router.delete("/api/users/:id").handler(ctx -> {
      mongo.findOne("users", new JsonObject().put("_id", ctx.request().getParam("id")), null, lookup -> {
        // error handling
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }

        JsonObject user = lookup.result();

        if (user == null) {
          // does not exist
          ctx.fail(404);
        } else {

          mongo.remove("users", new JsonObject().put("_id", ctx.request().getParam("id")), remove -> {
            // error handling
            if (remove.failed()) {
              ctx.fail(500);
              return;
            }

            ctx.response().setStatusCode(204);
            ctx.response().end();
          });
        }
      });
    });
        // Create a router endpoint for the static content.
        router.route().handler(StaticHandler.create());
        Vertx.vertx().createHttpServer().requestHandler(router::accept).listen(8080);
        //Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World From I4Server!")).listen(8080);
        System.out.println("Server started & listening to [8080]");
    }

    public static void main(String[] args) throws Exception {
        new I4Server().start();
    }

    private void loadData(MongoClient db) {
        /*
        db.dropCollection("users", drop -> {
            if (drop.failed()) {
                throw new RuntimeException(drop.cause());
            }
            List<JsonObject> users = new LinkedList<>();
            users.add(new JsonObject()
                    .put("username", "mks")
                    .put("firstName", "Siva")
                    .put("lastName", "Kalidasan")
                    .put("address", "India"));
            users.add(new JsonObject()
                    .put("username", "timfox")
                    .put("firstName", "Tim")
                    .put("lastName", "Fox")
                    .put("address", "The Moon"));
            for (JsonObject user : users) {
                db.insert("users", user, res -> {
                    System.out.println("inserted " + user.encode());
                });
            }
        });*/
        
        List<JsonObject> users = new LinkedList<>();
            users.add(new JsonObject()
                    .put("username", "mks")
                    .put("firstName", "Siva")
                    .put("lastName", "Kalidasan")
                    .put("address", "India"));
            users.add(new JsonObject()
                    .put("username", "timfox")
                    .put("firstName", "Tim")
                    .put("lastName", "Fox")
                    .put("address", "The Moon"));
            for (JsonObject user : users) {
//                db.insert("users", user, res -> {
//                    System.out.println("inserted " + user.encode());
//                });
            }
        
        //insert some payload
        List<JsonObject> payloads = new LinkedList<>();
        for (int i = 1; i <= 5000; i++) {
            payloads.add(new JsonObject()
                    .put("deviceId", "DEV0" + i)
                    .put("deviceName", "Arduino" + i)
                    .put("hubId", "CBE0" + i)
                    .put("status", "On")
                    .put("receivedOn",new Date().toString()));
        }          
            
            for (JsonObject payload : payloads) {
                db.insert("payloads", payload, res -> {
                    //System.out.println("inserted " + payload.encode());
                });
            }
            System.out.println("inserted");
    }
}
