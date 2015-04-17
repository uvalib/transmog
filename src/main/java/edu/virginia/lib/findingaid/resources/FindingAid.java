package edu.virginia.lib.findingaid.resources;

import edu.virginia.lib.findingaid.service.DocumentConverter;
import edu.virginia.lib.findingaid.service.DocumentStore;
import edu.virginia.lib.findingaid.service.SchemaStore;
import edu.virginia.lib.findingaid.structure.Element;
import edu.virginia.lib.findingaid.structure.Fragment;
import edu.virginia.lib.findingaid.structure.NodeType;
import edu.virginia.lib.findingaid.structure.Schema;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Path("findingaids")
public class FindingAid {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/schema")
    public JsonObject getStructureRules() {
        final JsonObjectBuilder all = Json.createObjectBuilder();
        for (Schema s : SchemaStore.getSchemaStore().getSchemaList()) {
            final JsonObjectBuilder o = Json.createObjectBuilder();
            for (NodeType type : s.getAssignedNodeTypes()) {
                final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                for (NodeType otherType : type.possibleChildren()) {
                    if (!otherType.equals(otherType.getSchema().getUnassignedType())) {
                        arrayBuilder.add(otherType.getId());
                    }
                }
                o.add(type.getId(), Json.createObjectBuilder().add("possibleChildren", arrayBuilder.build()).add("canContainText", type.isTextNode()).add("label", type.getDisplayLabel() == null || type.getDisplayLabel().trim().equals("") ? type.getId() : type.getDisplayLabel()));
            }
            all.add(s.getSchemaName(), o.build());
        }
        return all.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray listFindingAids(@PathParam("id") final String findingAidId) {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (String id : DocumentStore.getDocumentStore().listDocumentIds()) {
            arrayBuilder.add(id);
        }
        return arrayBuilder.build();
    }

    @POST
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}")
    public Response addDocument(@PathParam("id") final String findingAidId, @QueryParam("schemaId") final String schemaName, InputStream documentStream) throws IOException {
        final String id = (findingAidId == null || findingAidId.equals("") ? UUID.randomUUID().toString() : findingAidId);
        final Element doc = new DocumentConverter().convertWordDoc(documentStream, getRequestedSchemaOrDefault(schemaName));
        DocumentStore.getDocumentStore().addDocument(doc, id);
        System.out.println("Added document... " + id);
        return Response.status(Response.Status.OK).contentLocation(UriBuilder.fromResource(FindingAid.class).path(id).build()).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id: [^/]*}")
    public Response getDocAsHTML(@PathParam("id") final String findingAidId) {
        Element doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        if (doc == null) {
            return Response.status(404).build();
        }
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(doc.printTreeXHTML().toString()).build();
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/ead")
    public Response getDocAsEADXML(@PathParam("id") final String findingAidId) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        Element doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        if (doc == null) {
            return Response.status(404).build();
        }
        return Response.ok().type(MediaType.TEXT_XML_TYPE).entity(doc.getSchema().transformDocument(doc).toString()).build();
    }

    @PUT
    @Path("/{id: [^/]*}/{partId: [^/]*}")
    public Response assignType(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @QueryParam("type") final String type) throws IOException {
        final Element doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.findById(partId);
        NodeType nodeType = element.getType().getSchema().getNodeType(type);
        if (nodeType.equals(nodeType.getSchema().getUnassignedType())) {
            element.unassign();
        } else {
            element.assign(nodeType);
        }
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @PUT
    @Path("/{id: [^/]*}/{partId: [^/]*}/{fragmentId: [^/]*}")
    public Response updateFragmentText(InputStream value, @PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @PathParam("fragmentId") final String fragmentId, @QueryParam("type") final String fragmentType) throws IOException {
        final Element doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.findById(partId);
        final Fragment fragment = element.getFragment(fragmentId);
        if (fragmentType != null) {
            fragment.setType(fragmentType);
        }
        fragment.setText(readContent(value));
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @PUT
    @Path("/{id: [^/]*}/{partId: [^/]*}/table")
    public Response processTable(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @QueryParam("rowType") final String rowType, @QueryParam("colTypes") final List<String> colTypes) {
        final Element doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.findById(partId);
        element.assignTable(rowType, colTypes);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @POST
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/{partId: [^/]*}")
    public Response insertParent(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @QueryParam("type") final String type) throws IOException {
        final Element doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.findById(partId);
        element.bumpContent(element.getType().getSchema().getNodeType(type));
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @POST
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/{partId: [^/]*}/new")
    public Response insertNewChild(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @QueryParam("index") final int index) throws IOException {
        final Element doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.findById(partId);
        element.addChild(index);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @DELETE
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/{partId: [^/]*}")
    public Response delete(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId) throws IOException {
        final Element doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.findById(partId);
        final Element parent = element.getParent();
        parent.removeChild(element);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(parent.printTreeXHTML().toString()).build();
    }

    @MOVE
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/{partId: [^/]*}")
    /**
     * Moves the element at the given path to be a child of the element whose id
     * is included as the "newParent" query parameter.
     * @returns the XHTML tree for the updated newParent element
     */
    public Response moveComponent(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @QueryParam("newParent") final String newParentId, @QueryParam("index") final int index) throws IOException {
        final Element doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.findById(partId);
        final Element newParent = doc.findById(newParentId);
        element.moveElement(newParent, index);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(newParent.printTreeXHTML().toString()).build();
    }

    private String readContent(InputStream text) throws IOException {
        final String result = IOUtils.toString(text);
        if (result.trim().length() == 0) {
            return null;
        }
        return result;
    }

    private Schema getRequestedSchemaOrDefault(String name) {
        if (name == null) {
            return SchemaStore.getSchemaStore().getDefaultSchema();
        } else {
            Schema s = SchemaStore.getSchemaStore().getSchema(name);
            if (s == null) {
                throw new RuntimeException("Unknown schema \"" + name + "\"!");
            }
            return s;
        }

    }

}
