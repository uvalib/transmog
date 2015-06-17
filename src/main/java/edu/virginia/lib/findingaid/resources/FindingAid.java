package edu.virginia.lib.findingaid.resources;

import edu.virginia.lib.findingaid.service.DocumentConverter;
import edu.virginia.lib.findingaid.service.DocumentStore;
import edu.virginia.lib.findingaid.service.ProfileStore;
import edu.virginia.lib.findingaid.structure.Document;
import edu.virginia.lib.findingaid.structure.Element;
import edu.virginia.lib.findingaid.structure.Fragment;
import edu.virginia.lib.findingaid.structure.NodeType;
import edu.virginia.lib.findingaid.structure.Profile;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("findingaids")
public class FindingAid {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile")
    public JsonObject getStructureRules() {
        final JsonObjectBuilder all = Json.createObjectBuilder();
        for (Profile s : ProfileStore.getProfileStore().getProfileList()) {
            final JsonObjectBuilder o = Json.createObjectBuilder();
            for (NodeType type : s.getAssignedNodeTypes()) {
                final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                for (NodeType otherType : type.possibleChildren()) {
                    if (!otherType.equals(otherType.getProfile().getUnassignedType())) {
                        arrayBuilder.add(otherType.getId());
                    }
                }
                o.add(type.getId(), Json.createObjectBuilder().add("possibleChildren", arrayBuilder.build()).add("canContainText", type.isTextNode()).add("label", type.getDisplayLabel() == null || type.getDisplayLabel().trim().equals("") ? type.getId() : type.getDisplayLabel()));
            }
            all.add(s.getProfileName(), o.build());
        }
        return all.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile/{id: [^/]*}")
    public JsonObject getSingleProfile(@PathParam("id") final String profileId) {
        Profile s = ProfileStore.getProfileStore().getProfile(profileId);
        final JsonObjectBuilder o = Json.createObjectBuilder();
        for (NodeType type : s.getAssignedNodeTypes()) {
            final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (NodeType otherType : type.possibleChildren()) {
                if (!otherType.equals(otherType.getProfile().getUnassignedType())) {
                    arrayBuilder.add(otherType.getId());
                }
            }
            o.add(type.getId(), Json.createObjectBuilder().add("possibleChildren", arrayBuilder.build()).add("canContainText", type.isTextNode()).add("label", type.getDisplayLabel() == null || type.getDisplayLabel().trim().equals("") ? type.getId() : type.getDisplayLabel()));
        }
        return o.build();
    }

    @POST
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}")
    public Response addDocument(@PathParam("id") final String findingAidId, @QueryParam("profileId") final String profileName, InputStream documentStream) throws IOException {
        final Document doc = new DocumentConverter().convertWordDoc(documentStream, getRequestedProfileOrDefault(profileName), findingAidId);
        DocumentStore.getDocumentStore().addDocument(doc);
        System.out.println("Added document... " + doc.getId());
        return Response.status(Response.Status.OK).contentLocation(UriBuilder.fromResource(FindingAid.class).path(doc.getId()).build()).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id: [^/]*}")
    public Response getDocAsHTML(@PathParam("id") final String findingAidId) {
        Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        if (doc == null) {
            return Response.status(404).build();
        }
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(doc.getRootElement().printTreeXHTML().toString()).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id: [^/]*}/edit")
    public Response getDocumentPage(@PathParam("id") final String findingAidId) throws IOException {
        Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        if (doc == null) {
            return Response.status(404).build();
        }
        final String pageHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head lang=\"en\">\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Transmog</title>\n" +
                "\n" +
                "    <script src=\"../../../js/jquery-2.1.3.js\"></script>\n" +
                "    <script src=\"../../../js/jquery-ui.js\"></script>\n" +
                "    <script src=\"../../../js/bootstrap.js\"></script>\n" +
                "     <link rel=\"stylesheet\" href=\"../../../css/jquery-ui.css\">\n" +
                "     <link rel=\"stylesheet\" href=\"../../../css/jquery-ui.structure.css\">\n" +
                "     <link rel=\"stylesheet\" href=\"../../../css/jquery-ui.theme.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"../../../css/bootstrap.min.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"../../../css/main.css\">\n" +
                "    <script src=\"../../../js/document-support.js\"></script>\n" +
                "      <script type=\"text/javascript\">\n" +
                "          $(document).ready(function(){\n" +
                "              loadDocumentById('" + findingAidId + "')\n" +
                "          });          \n" +
                "      </script>    \n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <div class=\"container\">\n" +
                readContent(getClass().getClassLoader().getResourceAsStream("document-notes.html")) +
                "      <div class=\"row\" id=\"workspace\">\n" +
                "      </div>\n" +
                "    </div>\n" +
                "</body>";
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(pageHtml).build();
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/xml")
    public Response getDocAsEADXML(@PathParam("id") final String findingAidId) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        if (doc == null) {
            return Response.status(404).build();
        }
        return Response.ok().type(MediaType.TEXT_XML_TYPE).entity(doc.getProfile().transformDocument(doc.getRootElement()).toString()).build();
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/raw")
    public Response getDocAsRawXML(@PathParam("id") final String findingAidId) throws ParserConfigurationException, IOException, SAXException, TransformerException, XMLStreamException {
        Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        if (doc == null) {
            return Response.status(404).build();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.getRootElement().writeOutXML(baos);
        baos.close();
        return Response.ok().type(MediaType.TEXT_XML_TYPE).entity(new String(baos.toByteArray(), "UTF-8")).build();
    }

    @PUT
    @Path("/{id: [^/]*}/{partId: [^/]*}")
    public Response assignType(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @QueryParam("type") final String type) throws IOException {
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.getRootElement().findById(partId);
        NodeType nodeType = element.getType().getProfile().getNodeType(type);
        if (nodeType.equals(nodeType.getProfile().getUnassignedType())) {
            element.unassign();
        } else {
            element.assign(nodeType);
        }
        DocumentStore.getDocumentStore().saveDocument(doc);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @PUT
    @Path("/{id: [^/]*}/{partId: [^/]*}/{fragmentId: [^/]*}")
    public Response updateFragmentText(InputStream value, @PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @PathParam("fragmentId") final String fragmentId, @QueryParam("type") final String styles) throws IOException {
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.getRootElement().findById(partId);
        final Fragment fragment = element.getFragment(fragmentId);
        if (styles != null) {
            fragment.setStyles(styles.split(" "));
        }
        fragment.setText(readContent(value));
        DocumentStore.getDocumentStore().saveDocument(doc);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @POST
    @Path("/{id: [^/]*}/{partId: [^/]*}/table")
    public Response processTable(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @QueryParam("rowType") final String rowType, @QueryParam("colTypes") final List<String> colTypes) {
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.getRootElement().findById(partId);
        element.assignTable(rowType, colTypes);
        DocumentStore.getDocumentStore().saveDocument(doc);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @POST
    @Path("/{id: [^/]*}/undo")
    public Response undoLastChange(@PathParam("id") final String findingAidId) {
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        Document updated = DocumentStore.getDocumentStore().undoLastChange(doc);
        if (updated == null) {
            return Response.status(404).build();
        } else {
            return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(updated.getRootElement().printTreeXHTML().toString()).build();
        }
    }

    @GET
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Path("/{id: [^/]*}/{partId: [^/]*}/table.xlsx")
    public StreamingOutput processTable(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId) {
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.getRootElement().findById(partId);

        final Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("exported table");
        final List<List<String>> table = element.getTableData();
        for (short rowNum = 0; rowNum < table.size(); rowNum ++) {
            Row row = sheet.createRow(rowNum);
            for (int colNum = 0; colNum < table.get(rowNum).size(); colNum++) {
                Cell cell = row.createCell(colNum);
                cell.setCellValue(table.get(rowNum).get(colNum));
            }
        }

        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                wb.write(outputStream);
            }
        };
    }

    @PUT
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/{partId: [^/]*}/table.xlsx")
    public Response processTable(InputStream spreadsheet, @PathParam("id") final String findingAidId, @PathParam("partId") final String partId) throws IOException {
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.getRootElement().findById(partId);

        final Workbook wb = new XSSFWorkbook(spreadsheet);
        Sheet sheet = wb.getSheetAt(0);
        final List<List<String>> table = new ArrayList<List<String>>();
        for (Row r : sheet) {
            List<String> row = new ArrayList<String>();
            table.add(row);
            for (Cell c : r) {
                if (c != null && (c.getCellType() == Cell.CELL_TYPE_STRING)) {
                    row.add(c.getStringCellValue());
                } else if (c != null && c.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    row.add(new DataFormatter().formatCellValue(c));
                } else {
                    // hope this works...
                    row.add(c.getStringCellValue());
                }
            }
        }
        element.replaceTableData(table);
        DocumentStore.getDocumentStore().saveDocument(doc);
        return Response.ok().type(MediaType.TEXT_XML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @POST
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/{partId: [^/]*}")
    public Response insertParent(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @QueryParam("type") final String type) throws IOException {
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.getRootElement().findById(partId);
        element.bumpContent(element.getType().getProfile().getNodeType(type));
        DocumentStore.getDocumentStore().saveDocument(doc);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @POST
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/{partId: [^/]*}/new")
    public Response insertNewChild(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @QueryParam("index") final int index) throws IOException {
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.getRootElement().findById(partId);
        element.addChild(index);
        DocumentStore.getDocumentStore().saveDocument(doc);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.printTreeXHTML().toString()).build();
    }

    @POST
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/{partId: [^/]*}/split")
    public Response splitText(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId, @QueryParam("text") final String text) throws IOException {
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.getRootElement().findById(partId);
        element.splitOnText(text);
        DocumentStore.getDocumentStore().saveDocument(doc);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(element.getParent().printTreeXHTML().toString()).build();
    }

    @DELETE
    @Produces(MediaType.TEXT_XML)
    @Path("/{id: [^/]*}/{partId: [^/]*}")
    public Response delete(@PathParam("id") final String findingAidId, @PathParam("partId") final String partId) throws IOException {
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.getRootElement().findById(partId);
        final Element parent = element.getParent();
        parent.removeChild(element);
        DocumentStore.getDocumentStore().saveDocument(doc);
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
        final Document doc = DocumentStore.getDocumentStore().getDocument(findingAidId);
        final Element element = doc.getRootElement().findById(partId);
        final Element newParent = doc.getRootElement().findById(newParentId);
        element.moveElement(newParent, index);
        DocumentStore.getDocumentStore().saveDocument(doc);
        return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(newParent.printTreeXHTML().toString()).build();
    }

    private String readContent(InputStream text) throws IOException {
        final String result = IOUtils.toString(text);
        if (result.trim().length() == 0) {
            return null;
        }
        return result;
    }

    private Profile getRequestedProfileOrDefault(String name) {
        if (name == null) {
            return ProfileStore.getProfileStore().getDefaultProfile();
        } else {
            Profile s = ProfileStore.getProfileStore().getProfile(name);
            if (s == null) {
                throw new RuntimeException("Unknown profile \"" + name + "\"!");
            }
            return s;
        }

    }

}
