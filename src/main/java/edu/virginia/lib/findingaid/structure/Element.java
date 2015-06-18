package edu.virginia.lib.findingaid.structure;

import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Element implements Serializable {

    private static final long serialVersionUID = 1L;

    String id;
    Element parent;
    NodeType type;
    List<Element> children;
    List<Fragment> fragments;

    public Element(NodeType type) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.parent = null;
        this.children = new ArrayList<Element>();
        this.fragments = new ArrayList<Fragment>();
    }

    public Element(NodeType type, String content) {
        this(type, Collections.singletonList(new Fragment(content)));
    }

    public Element(NodeType type, List<Fragment> f) {
        this(type);
        if (!type.isTextNode()) {
            throw new IllegalArgumentException("Elements of type " + type.getId() + " may not contain text directly!");
        }
        this.fragments = new ArrayList<Fragment>(f);
    }

    /**
     * Splits this element into multiple elements (of the same type) on the first
     * instance of the given text String in the content of this element.
     */
    public void splitOnText(String text) {
        final String contentAsString = getContentAsString();
        if (!contentAsString.contains(text)) {
            return;
        }
        final Element before = new Element(getType());
        final Element after = new Element(getType());
        final int start = contentAsString.indexOf(text);
        final int end = start + text.length();
        int cursor = 0;
        final List<Fragment> fragmentQueue = new ArrayList<Fragment>(fragments);
        for (int i = 0; i < fragmentQueue.size(); i ++) {
            Fragment f = fragmentQueue.get(i);
            final int flength = f.getText().length();
            if (cursor < start) {
                if (cursor + flength <= start) {
                    // whole fragment is before
                    before.addFragment(f);
                    fragments.remove(f);
                    cursor += flength;
                } else {
                    // fragment must be split
                    final Fragment newFrag = new Fragment(f.getText().substring(0, start - cursor), f.styles);
                    before.addFragment(newFrag);
                    f.setText(f.getText().substring(start - cursor));
                    i --; // look at this fragment again...
                    cursor += newFrag.getText().length();
                }
            } else if (cursor >= end) {
                // whole fragment is after
                after.addFragment(f);
                fragments.remove(f);
                cursor += flength;
            } else if (cursor + flength <= end) {
                // whole fragment is within
                cursor += flength;
            } else {
                // fragment must be split
                after.addFragment(new Fragment(f.getText().substring(end - cursor), f.styles));
                f.setText(f.getText().substring(0, end - cursor));
                cursor += flength;
            }
        }
        if (before.getContentAsString().length() > 0) {
            parent.addChild(before, this.getIndexWithinParent());
        }
        if (after.getContentAsString().length() > 0) {
            parent.addChild(after, this.getIndexWithinParent() + 1);
        }

    }

    public Profile getProfile() {
        return this.type.getProfile();
    }

    public String getContentAsString() {
        StringBuffer sb = new StringBuffer();
        for (Fragment f : fragments) {
            sb.append(f.content);
        }
        return sb.toString();
    }

    public List<Fragment> getContent() {
        return this.fragments;
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
     * Assigns the type for an unassigned element.
     * @throws java.lang.IllegalStateException if this type isn't currently UNASSIGNED
     * @throws java.lang.IllegalArgumentException if you attempt to assign a type when the current
     *         type is anything but "unassigned" or if you attempt to assign it an illegal type
     *         this includes a type that may not contain content when content is already set,
     *         a type that may not be the child of this Element's parent.
     */
    public void assign(NodeType type) {
        if (!this.isUnassigned()) {
            throw new IllegalStateException();
        }
        if (this.isUnassigned()) {
            for (Element child : children) {
                if (!child.isUnassigned()) {
                    throw new IllegalStateException("Unable to assign type \"" + type + " because child already has assignment (" + child.getType().getId() + ")!");
                }
            }
        }
        if (!type.isTextNode() && !this.fragments.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.type = type;
    }

    public void unassign() {
        this.type = this.getProfile().getUnassignedType();
    }

    public void setContent(List<Fragment> fragments) {
        if (!this.type.isTextNode()) {
            throw new IllegalStateException();
        }
        this.fragments = fragments;
    }

    public void assignTable(String rowTypeString, List<String> colTypeStrings) {
        if (!isUnassignedTable()) {
            throw new IllegalStateException();
        }
        if (this.getChildren().get(0).getChildren().size() != colTypeStrings.size()) {
            throw new IllegalArgumentException("There are " + this.getChildren().get(0).getChildren().size() + " rows but only " + colTypeStrings.size() + " type assignments were provided!");
        }
        for (String type : colTypeStrings) {
            if (getProfile().getNodeType(type) == null) {
                throw new IllegalArgumentException("Type " + type + " not found in profile!");
            }
        }
        int insertionPoint = getIndexWithinParent();
        for (int i = 0; i < this.children.size(); i ++) {
            Element row = this.children.get(i);
            row.type = getProfile().getNodeType(rowTypeString);
            row.moveElement(parent, insertionPoint ++);
            for (int j = 0; j < row.children.size(); j ++) {
                Element cell = row.children.get(j);
                cell.type = cell.getProfile().getNodeType(colTypeStrings.get(j));
            }
        }
        this.removeFromParent();
    }

    public List<List<String>> getTableData() {
        if (!type.equals(getProfile().getTableType())) {
            throw new IllegalStateException();
        }
        final List<List<String>> table = new ArrayList<List<String>>(this.children.size());
        for (Element c : children) {
            final List<String> row = new ArrayList<String>();
            for (Element cell : c.children) {
                row.add(cell.getContentAsString());
            }
            table.add(row);
        }
        return table;
    }

    public void replaceTableData(List<List<String>> data) {
        if (!type.equals(getProfile().getTableType())) {
            throw new IllegalStateException();
        }

        this.children.clear();
        for (List<String> r : data) {
            Element row = new Element(getProfile().getRowType());
            for (String v : r) {
                row.addChild(new Element(getProfile().getUnassignedType(), v));
            }
            this.children.add(row);
        }
    }

    public void assignPath(Path path) {
        final Profile s = getProfile();
        if (path.depth() == 1) {
            assign(s.getNodeType(path.getPathElement(0)));
        } else {
            Element newParent = getParent().locateOrCreatePath(path.getParentPath(), -1);
            moveElement(newParent, newParent.getChildren().size());
            assign(s.getNodeType(path.getLastPathElement()));
        }
    }

    /**
     * Assigns this element to the given type and bumps the content down to a new
     * UNASSIGNED child.
     * @throws java.lang.IllegalStateException if this type isn't current UNASSIGNED
     * @throws java.lang.IllegalArgumentException if you attempt to
     * @returns the inserted element
     */
    public void bumpContent(NodeType type) {
        if (this.isUnassigned()) {
            Element inserted = new Element(this.type, this.fragments);
            this.type = type;
            this.children.add(inserted);
            this.fragments = new ArrayList<Fragment>();
            inserted.parent = this;
        } else if (this.isUnassignedTable()) {
            Element inserted = new Element(this.type);
            inserted.fragments = fragments;
            inserted.children = this.children;
            this.children = new ArrayList<Element>();
            this.fragments = new ArrayList<Fragment>();
            this.type = type;
            this.children.add(inserted);
            inserted.parent = this;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void moveElement(final Element newParent, int index) {
        if (!isUnassigned() && !isUnassignedTable()) {
            if (!newParent.getType().canHaveChild(type)) {
                throw new IllegalStateException("You may not place a \"" + type.getDisplayLabel()
                        + "\" within a \"" + newParent.getType().getDisplayLabel() + "\"!");
            }
        }
        final Element oldParent = this.parent;
        if (oldParent == newParent && index > oldParent.children.indexOf(this)) {
            index --;
        }
        oldParent.children.remove(this);
        newParent.children.add(index, this);
        this.parent = newParent;
    }

    public void addChild(Element newEl) {
        addChild(newEl, -1);
    }

    public void addChild(int index) {
        final Element inserted = new Element(getProfile().getUnassignedType(), "");
        addChild(inserted, index);
    }

    public void addChild(Element newEl, int index) {
        this.children.add(index == -1 ? children.size() : index, newEl);
        newEl.parent = this;
    }

    public void addFragment(Fragment f) {
        if (!this.type.isTextNode()) {
            throw new IllegalStateException();
        }
        this.fragments.add(f);
    }

    public Fragment getFragment(String id) {
        for (Fragment f : fragments) {
            if (f.getId().equals(id)) {
                return f;
            }
        }
        return null;
    }

    public Element locateOrCreatePath(Path path, int index) {
        final Profile s = getProfile();
        NodeType type = s.getNodeType(path.getPathElement(0));
        Element child = getFirstChildOfType(type);
        if (child == null) {
            child = new Element(type);
            addChild(child, index);
        }
        final Path nextPath = path.relativeToFirst();
        if (nextPath != null) {
            return child.locateOrCreatePath(nextPath, -1);
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
        emitRawXMLEvents(w);
        w.close();
    }

    public Element getLastChild() {
        return children.isEmpty() ? null : children.get(children.size() - 1);
    }

    public String printTreeXHTML() {
        return printTreeXHTML(null);
    }

    public String printTreeXHTML(final String message) {
        final NodeType table = getProfile().getTableType();
        final NodeType row = getProfile().getRowType();
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
        } else if (getProfile().getRootNodeType().equals(type)) {
            response.append("<div class=\"" + type.getId() + " ROOT ASSIGNED\" id=\"" + id + "\">");
        } else if (isUnassigned()) {
            response.append("<div class=\"" + type.getId() + "\" id=\"" + id + "\">");
        } else {
            response.append("<div class=\"" + type.getId() + " ASSIGNED\" id=\"" + id + "\">");
        }

        if (getProfile().getRootNodeType().equals(type)) {
            response.append("<div class=\"document-note\">Encoded using the <span id=\"profile-name\">" + getProfile().getProfileName() + "</span></div>");
            if (message != null) {
                response.append("<div class=\"alert alert-info alert-dismissible\" role=\"alert\">\n" +
                        "  <button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\n" +
                        message + "</div>");
            }
        }

        for (Fragment f : fragments) {
            response.append("<span id=\"" + f.getId() + "\" class=\"" + f.getStylesAsSpaceDelimitedString() + "\">" + (f.getText() == null ? "" : f.getText()) + "</span>");
        }

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

    private void emitRawXMLEvents(XMLEventWriter w) throws XMLStreamException {
        XMLEventFactory f = XMLEventFactory.newInstance();
        w.add(f.createStartDocument());
        emitElement(w);
        w.add(f.createEndDocument());
    }

    void emitElement(XMLEventWriter w) throws XMLStreamException {
        w.add(xml().createStartElement("", "", type.getId()));
        w.add(xml().createAttribute("id", this.id));
        for (Fragment f : fragments) {
            w.add(xml().createStartElement("", "", "span"));
            w.add(xml().createAttribute("id", f.getId()));
            w.add(xml().createAttribute("style", f.getStylesAsSpaceDelimitedString()));
            w.add(xml().createCharacters(f.getText()));
            w.add(xml().createEndElement("", "", "span"));
            w.add(xml().createCharacters("\n"));
        }
        for (Element child : children) {
            child.emitElement(w);
        }
        w.add(xml().createEndElement("", "", type.getId()));
        w.add(xml().createCharacters("\n"));
    }

    static Element parseElement(final StartElement startTag, XMLEventReader r, Profile p) throws XMLStreamException {
        final String typeId = startTag.asStartElement().getName().getLocalPart();
        final String id = startTag.getAttributeByName(new QName("", "id")).getValue();
        if (p.getNodeType(typeId) == null) {
            throw new RuntimeException("Unrecognized type \"" + typeId + "\" in profile \"" + p.getProfileName() + "\"...");
        }
        Element element = new Element(p.getNodeType(typeId));
        element.id = id;
        while (r.hasNext()) {
            final XMLEvent next = r.nextEvent();
            if (next.isStartElement()) {
                final StartElement s = next.asStartElement();
                if (s.getName().getLocalPart().equals("span")) {
                    final String[] styles = s.getAttributeByName(new QName("", "style")).getValue().split(" ");
                    final String fragId = s.getAttributeByName(new QName("", "id")).getValue();
                    final String value = Document.getText(r);
                    element.fragments.add(new Fragment(fragId, styles, value));
                } else {
                    element.addChild(parseElement(s, r, p));
                }
            } else if (next.isEndElement()) {
                return element;
            }
        }
        throw new RuntimeException("Malformed XML: element " + startTag + " never ended!");
    }

    public boolean isUnassigned() {
        return this.type.equals(getProfile().getUnassignedType());
    }

    public boolean isUnassignedTable() {
        return this.type.equals(getProfile().getTableType());
    }

    public boolean equals(Element other) {
        return this.printTreeXHTML().equals(other.printTreeXHTML());
    }

    public int hashCode() {
        return this.printTreeXHTML().hashCode();

    }
}
