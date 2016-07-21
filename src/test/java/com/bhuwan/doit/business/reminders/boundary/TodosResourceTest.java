/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bhuwan.doit.business.reminders.boundary;

import com.airhacks.rulz.jaxrsclient.JAXRSClientProvider;
import static com.airhacks.rulz.jaxrsclient.JAXRSClientProvider.buildWithURI;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
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
        Response response = this.provider.target().request(MediaType.APPLICATION_JSON).get();
        assertThat(response.getStatus(), is(200));
        JsonArray allTodos = response.readEntity(JsonArray.class);
        System.out.println("payload: " + allTodos);
        assertFalse(allTodos.isEmpty());

        JsonObject todo = allTodos.getJsonObject(0);
        assertTrue(todo.getString("caption").startsWith("Impl"));

        // Get with id
        JsonObject todo42 = this.provider.target().path("42").request(MediaType.APPLICATION_JSON).get(JsonObject.class);
        assertTrue(todo42.getString("caption").contains("42"));

        // Delete
        Response delResp = this.provider.target().path("42").request(MediaType.APPLICATION_JSON).delete();
        assertThat(delResp.getStatus(), is(204));
    }
}
