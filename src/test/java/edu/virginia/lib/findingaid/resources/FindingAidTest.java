package edu.virginia.lib.findingaid.resources;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Application;
import java.io.ByteArrayInputStream;

public class FindingAidTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(FindingAid.class);
    }

    @Test
    public void testGetSchema() {
        JsonObject result = Json.createReader(new ByteArrayInputStream(
                target().path("findingaids/schema").request().get(String.class).getBytes())).readObject();
        System.out.println(result);
    }

    @Test
    public void testGetUnknown() {
        Assert.assertEquals(404, target().path("findingaids/unknown").request().buildGet().invoke().getStatus());
    }


}
