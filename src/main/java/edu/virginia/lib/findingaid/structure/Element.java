package edu.virginia.lib.findingaid.structure;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Element implements Serializable {

    private static final long serialVersionUID = 1L;

    String id;
    Element parent;
    NodeType type;
    String content;
    List<Element> children;

    public Element(NodeType type) {
        this(type, null);
    }

    public Element(NodeType type, String content) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.content = content;
        this.parent = null;
        this.children = new ArrayList<Element>();
    }

    public Schema getSchema() {
        return this.type.getSchema();
    }

    public String getContent() {
        return this.content;
    }

    public List<Element> getChildren() {
        return this.children;
    }

    public int getIndexWithinParent() {
        if (parent == null) {
            return 0;
        } else {
            return parent.children.indexOf(this);
        }
    }

    public Element findById(String idToFind) {
        if (id.equals(idToFind)) {
            return this;
        } else if (children != null) {
            for (Element child : children) {
                final Element found = child.findById(idToFind);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Assigns the type for an unassigned element or unassign the type of an assigned element.
     * @throws java.lang.IllegalStateException if this type isn't current UNASSIGNED
     * @throws java.lang.IllegalArgumentException if you attempt to assign a type when the current
     *         type is anything but "unassigned" or if you attempt to assign it an illegal type
     *         this includes a type that may not contain content when content is already set,
     *         a type that may not be the child of this Element's parent.
     */
    public void assign(NodeType type, String content) {
        if (!type.equals(getSchema().getUnassignedType()) && !this.isUnassigned()) {
            throw new IllegalStateException();
        }
        if (this.isUnassigned()) {
            for (Element child : children) {
                if (!child.isUnassigned()) {
                    throw new IllegalStateException("Unable to assign type \"" + type + " because child already has assignment!");
                }
            }
        }
        if (!type.isTextNode()) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        if (content != null) {
            this.content = content;
        }
    }

    public void assignTable(String rowTypeString, List<String> colTypeStrings) {
        if (!isUnassignedTable()) {
            throw new IllegalStateException();
        }
        if (this.getChildren().get(0).getChildren().size() != colTypeStrings.size()) {
            throw new IllegalArgumentException("There are " + this.getChildren().get(0).getChildren().size() + " rows but only " + colTypeStrings.size() + " type assignments were provided!");
        }
        for (String type : colTypeStrings) {
            if (getSchema().getNodeType(type) == null) {
                throw new IllegalArgumentException("Type " + type + " not found in schema!");
            }
        }
        this.type = getSchema().getNodeType(rowTypeString);
        for (int i = 0; i < this.children.size(); i ++) {
            Element row = this.children.get(i);
            row.type = getSchema().getNodeType(rowTypeString);
            for (int j = 0; j < row.children.size(); j ++) {
                Element cell = row.children.get(j);
                cell.type = cell.getSchema().getNodeType(colTypeStrings.get(j));
            }
        }
    }

    public void assignPath(String path) {
        final Schema s = getSchema();
        if (!path.contains("/")) {
            this.assign(s.getNodeType(path), this.content);
        } else {
            Element newParent = locateOrCreatePath(path.substring(0, path.lastIndexOf('/')), getChildren().size());
            moveElement(newParent, newParent.getChildren().size(), content);
            assign(s.getNodeType(path.substring(path.lastIndexOf("/") + 1)), content);
        }
    }

    /**
     * Assigns this element to the given type and bumps the content down to a new
     * UNASSIGNED child.
     * @throws java.lang.IllegalStateException if this type isn't current UNASSIGNED
     * @throws java.lang.IllegalArgumentException if you attempt to
     * @returns the inserted element
     */
    public void bumpContent(NodeType type, String content) {
        if (this.isUnassigned()) {
            Element inserted = new Element(this.type, this.content);
            this.type = type;
            this.children.add(inserted);
            inserted.parent = this;
        } else if (this.isUnassignedTable()) {
            Element inserted = new Element(this.type);
            if (content != null) {
                inserted.content = content;
            }
            inserted.children = this.children;
            this.children = new ArrayList<Element>();
            this.type = type;
            this.children.add(inserted);
            inserted.parent = this;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void moveElement(final Element newParent, int index, String content) {
        if (!isUnassigned() && !isUnassignedTable()) {
            throw new IllegalStateException();
        }
        final Element oldParent = this.parent;
        if (oldParent == newParent && index > oldParent.children.indexOf(this)) {
            index --;
        }
        oldParent.children.remove(this);
        newParent.children.add(index, this);
        this.parent = newParent;
        if (content != null) {
            this.content = content;
        }
    }

    public void addChild(Element newEl) {
        addChild(newEl, -1);
    }

    public void addChild(int index) {
        final Element inserted = new Element(getSchema().getUnassignedType(), "");
        addChild(inserted, index);
    }

    public void addChild(Element newEl, int index) {
        this.children.add(index == -1 ? children.size() : index, newEl);
        newEl.parent = this;
    }

    public Element locateOrCreatePath(String path, int index) {
        final Schema s = getSchema();
        String[] pathTypes = path.split("/");
        NodeType type = s.getNodeType(pathTypes[0]);
        Element child = getFirstChildOfType(type);
        if (child == null) {
            child = new Element(type);
            addChild(child, index);
        }
        if (pathTypes.length > 1) {
            return child.locateOrCreatePath(path.substring(path.indexOf('/') + 1), -1);
        } else {
            return child;
        }
    }

    private Element getFirstChildOfType(NodeType t) {
        for (Element child : this.children) {
            if (child.type.equals(t)) {
                return child;
            }
        }
        return null;
    }

    public void removeChild(Element child) {
        this.children.remove(child);
    }

    public void removeFromParent() {
        this.parent.children.remove(this);
        this.parent = null;
    }

    public Element getParent() {
        return parent;
    }

    public NodeType getType() {
        return type;
    }

    public void writeOutXML(OutputStream os) throws XMLStreamException {
        XMLOutputFactory f = XMLOutputFactory.newFactory();
        XMLEventWriter w = f.createXMLEventWriter(os);
        emitEADXMLEvents(w);
        w.close();
    }

    public JsonObject asJsonObject() {
        JsonObjectBuilder b = Json.createObjectBuilder().add("id", id).add("type", type.getId());
        if (content != null) b.add("value", content);
        JsonArrayBuilder a = Json.createArrayBuilder();
        for (Element child : children) {
            a.add(child.asJsonObject());
        }
        b.add("children", a);
        return b.build();
    }

    public Element getLastChild() {
        return children.isEmpty() ? null : children.get(children.size() - 1);
    }

    public String printTreeXHTML() {
        final NodeType table = getSchema().getTableType();
        final NodeType row = getSchema().getRowType();
        final boolean isTable = type.equals(table);
        final boolean isRow = type.equals(row);
        final boolean isCell = getParent() != null && getParent().type.equals(row);
        StringBuffer response = new StringBuffer();
        if (isTable) {
            response.append("<div id=\"" + id + "\" class=\"UNASSIGNED_TABLE\"><table><tbody>");
        } else if (isRow) {
            response.append("<tr id=\"" + id + "\">");
        } else if (isCell) {
            response.append("<td id=\"" + id + "\">");
        } else if (getSchema().getRootNodeType().equals(type)) {
            response.append("<div class=\"" + type.getId() + " ROOT ASSIGNED\" id=\"" + id + "\">");
        } else if (isUnassigned()) {
            response.append("<div class=\"" + type.getId() + "\" id=\"" + id + "\">");
        } else {
            response.append("<div class=\"" + type.getId() + " ASSIGNED\" id=\"" + id + "\">");
        }

        if (getSchema().getRootNodeType().equals(type)) {
            response.append("<div class=\"document-note\">Encoded using the <span id=\"profile-name\">" + getSchema().getSchemaName() + "</span></div>");
        }

        response.append(content != null ? content : "");

        if (children != null) {
            for (Element child : children) {
                response.append(child.printTreeXHTML());
            }
        }

        if (isTable) {
            response.append("</tbody></table></div>");
        } else if (isRow) {
            response.append("</tr>");
        } else if (isCell) {
            response.append("</td>");
        } else {
            response.append("</div>");
        }
        return response.toString();
    }

    public String toString() {
        return getType().toString();
    }

    private XMLEventFactory xml() {
        return XMLEventFactory.newInstance();
    }

    private void emitEADXMLEvents(XMLEventWriter w) throws XMLStreamException {
        XMLEventFactory f = XMLEventFactory.newInstance();
        w.add(f.createStartDocument());
        emitElement(w);
        w.add(f.createEndDocument());
    }

    private void emitElement(XMLEventWriter w) throws XMLStreamException {
        w.add(xml().createStartElement("", "", type.getId()));
        if (content != null) {
            w.add(xml().createCharacters(content));
        }
        for (Element child : children) {
            child.emitElement(w);
        }
        w.add(xml().createEndElement("", "", type.getId()));
    }

    public boolean isUnassigned() {
        return this.type.equals(getSchema().getUnassignedType());
    }

    public boolean isUnassignedTable() {
        return this.type.equals(getSchema().getTableType());
    }
}