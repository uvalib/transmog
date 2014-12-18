package edu.virginia.lib.findingaid.structure;

public class FindingAidBuilder {

    private Element root;

    private Element cursor;

    private Schema s;

    public FindingAidBuilder(Schema schema) {
        this.s = schema;
        root = new Element(schema.getRootNodeType());
        cursor = root;
    }

    public Schema getSchema() {
        return this.s;
    }

    public boolean canAddChildOfType(NodeType type) {
        return type.canBeChildOf(cursor.getType());
    }

    /**
     * Adds a child to the cursor and possibly updates the cursor.
     * If the added child is a leaf type, the cursor is unchanged,
     * otherwise the cursor now points to the new node.
     */
    public void addChild(NodeType type, String content, boolean setCursorToChild) {
        Element child = new Element(type, content);
        if (!canAddChildOfType(child.getType())) {
            throw new IllegalArgumentException("Can't add " + type + " to " + cursor.getType() + "!");
        }
        cursor.children.add(child);
        child.parent = cursor;

        if (setCursorToChild) {
            cursor = child;
        }
    }

    public Element getDocument() {
        return this.root;
    }

}
