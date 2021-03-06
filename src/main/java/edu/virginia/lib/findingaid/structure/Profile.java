package edu.virginia.lib.findingaid.structure;

import edu.virginia.lib.findingaid.rules.Rule;

import java.util.List;

public abstract class Profile {

    public static final String UNASSIGNED_TABLE = "UNASSIGNED_TABLE";
    public static final String UNASSIGNED_ROW = "UNASSIGNED_ROW";
    public static final String UNASSIGNED = "UNASSIGNED";

    private NodeType table;

    private NodeType row;

    private NodeType unassigned;

    public Profile() {
        row = new SystemNodeType(this, "Row", "", UNASSIGNED_ROW, false);
        table = new SystemNodeType(this, "Table", "", UNASSIGNED_TABLE, false);
        unassigned = new SystemNodeType(this, "Unassigned", "", UNASSIGNED, true);
    }

    public abstract String getProfileName();

    public abstract String getProfileDescription();

    public abstract NodeType getRootNodeType();

    public abstract List<NodeType> getAssignedNodeTypes();

    public abstract List<Rule> getRules();

    public abstract List<ValidationResult> getValidationStatus(Document d);

    public abstract String transformDocument(Element el);

    public NodeType getNodeType(String id) {
        if (unassigned.getId().equals(id)) {
            return unassigned;
        }

        if (table.getId().equals(id)) {
            return table;
        }

        if (row.getId().equals(id)) {
            return row;
        }

        // check for special types
        for (NodeType type : getAssignedNodeTypes()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Undefined node type \"" + id + "\"!");
    }

    public NodeType getUnassignedType() {
        return this.unassigned;
    }

    public NodeType getTableType() {
        return this.table;
    }

    public NodeType getRowType() {
        return this.row;
    }
}
