/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bhuwan.doit.business.reminders.boundary;

import com.airhacks.rulz.jaxrsclient.JAXRSClientProvider;
import static com.airhacks.rulz.jaxrsclient.JAXRSClientProvider.buildWithURI;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author bhuwan
 */
public class TodosResourceTest {

//    private Client client;
//    private WebTarget tut;
//
//    @Before
//    public void setup() {
//        this.client = ClientBuilder.newClient();
//        tut = target under test    
//        this.tut = this.client.target("http://localhost:8080/doit/api/todos");
//    }
    // instead we can use the following
    @Rule
    public JAXRSClientProvider provider = buildWithURI("http://localhost:8080/doit/api/todos");

    @Test
    public void crud() {

        // Create a json entity for ToDo
        JsonObjectBuilder toDoBuilder = Json.createObjectBuilder();
        JsonObject todoToBeCreate = toDoBuilder.add("caption", "Implement").add("description", "todo description").add("priority", 50).build();

        // create
        Response postResponse = this.provider.target().request().post(Entity.json(todoToBeCreate));
        assertThat(postResponse.getStatus(), is(201));
        String location = postResponse.getHeaderString("location");
        System.out.println("Location url: " + location);

        // find
        JsonObject dedicatedTodo = this.provider.client().target(location).request(MediaType.APPLICATION_JSON).get(JsonObject.class);
        assertTrue(dedicatedTodo.getString("caption").contains("Implement"));

        // update
        JsonObjectBuilder updateBuilder = Json.createObjectBuilder();
        JsonObject todoToBeUpdated = updateBuilder.add("caption", "Implemented").build();

        Response updateResponse = this.provider.client().target(location).request(MediaType.APPLICATION_JSON).put(Entity.json(todoToBeUpdated));
        assertThat(updateResponse.getStatus(), is(200));

        // update again for optimistic locking verification
        // update
        updateBuilder = Json.createObjectBuilder();
        todoToBeUpdated = updateBuilder.add("caption", "Implemented").add("priority", 44).build();

        updateResponse = this.provider.client().target(location).request(MediaType.APPLICATION_JSON).put(Entity.json(todoToBeUpdated));
        assertThat(updateResponse.getStatus(), is(409));
        String cause = updateResponse.getHeaderString("cause");
        assertNotNull(cause);
        System.out.println("Conflict cause: " + cause);

        //verify update
        JsonObject updated = this.provider.client().target(location).request(MediaType.APPLICATION_JSON).get(JsonObject.class);
        assertTrue(updated.getString("caption").contains("Implemented"));

        // update status
        JsonObjectBuilder statusUpdateBuilder = Json.createObjectBuilder();
        JsonObject statusUpdate = statusUpdateBuilder.add("done", true).build();

        this.provider.client().target(location).path("status").request(MediaType.APPLICATION_JSON).put(Entity.json(statusUpdate));

        //verify status
        JsonObject getUpdatedStatus = this.provider.client().target(location).request(MediaType.APPLICATION_JSON).get(JsonObject.class);
        assertThat(getUpdatedStatus.getBoolean("done"), is(true));

        // update non existing status
        JsonObjectBuilder nonExistingObject = Json.createObjectBuilder();
        JsonObject nonExistingStatusUpdate = nonExistingObject.add("done", true).build();

        Response response = this.provider.target().path("-9").path("status").request(MediaType.APPLICATION_JSON).put(Entity.json(nonExistingStatusUpdate));
        assertThat(response.getStatus(), is(400));
        assertFalse(response.getHeaderString("reason").isEmpty());

        // update mal formed status
        JsonObjectBuilder malFormedStatus = Json.createObjectBuilder();
        JsonObject malFormedStatusUpdate = malFormedStatus.add("mal formed", true).build();

        response = this.provider.target(location).path("status").request(MediaType.APPLICATION_JSON).put(Entity.json(malFormedStatusUpdate));
        assertThat(response.getStatus(), is(400));
        assertFalse(response.getHeaderString("reason").isEmpty());

        //find
        response = this.provider.target().request(MediaType.APPLICATION_JSON).get();
        assertThat(response.getStatus(), is(200));
        JsonArray allTodos = response.readEntity(JsonArray.class);
        System.out.println("payload: " + allTodos);
        assertFalse(allTodos.isEmpty());

        JsonObject todo = allTodos.getJsonObject(0);
        assertTrue(todo.getString("caption").startsWith("Impl"));

        // Delete
        Response delResp = this.provider.target().path("42").request(MediaType.APPLICATION_JSON).delete();
        assertThat(delResp.getStatus(), is(204));
    }

    @Test
    public void createInvalidTodo() {
        // create ToDo without caption
        // Create a json entity for ToDo
        JsonObjectBuilder toDoBuilder = Json.createObjectBuilder();
        JsonObject todoToBeCreate = toDoBuilder.add("description", "todo description").add("priority", 50).build();

        // create
        Response postResponse = this.provider.target().request().post(Entity.json(todoToBeCreate));
        assertThat(postResponse.getStatus(), is(400));
        postResponse.getHeaders().entrySet().forEach(System.out::println);
    }

    @Test
    public void createValidTodo() {
        // create ToDo without caption
        // Create a json entity for ToDo
        JsonObjectBuilder toDoBuilder = Json.createObjectBuilder();
        JsonObject todoToBeCreate = toDoBuilder.add("caption", "ab").add("description", "todo description").add("priority", 50).build();

        // create
        Response postResponse = this.provider.target().request().post(Entity.json(todoToBeCreate));
        assertThat(postResponse.getStatus(), is(201));
    }
    
    @Test
    public void createToDoWithPriorityGt10AndWithoutDesc() {
        // create ToDo with priority >10 and without description 
        // Create a json entity for ToDo
        JsonObjectBuilder toDoBuilder = Json.createObjectBuilder();
        JsonObject todoToBeCreate = toDoBuilder.add("caption", "ab").add("priority", 11).build();

        // create
        Response postResponse = this.provider.target().request().post(Entity.json(todoToBeCreate));
        assertThat(postResponse.getStatus(), is(400));
    }
}
