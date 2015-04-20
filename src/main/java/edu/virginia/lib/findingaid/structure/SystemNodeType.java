package edu.virginia.lib.findingaid.structure;

import java.util.List;

public class SystemNodeType implements NodeType {

    private Profile profile;

    private String label;

    private String description;

    private String id;

    private boolean isTextNode;

    public SystemNodeType(Profile profile, String displayLabel, String description, String id, boolean isTextNode) {
        this.profile = profile;
        this.label = displayLabel;
        this.description = description;
        this.id = id;
        this.isTextNode = isTextNode;
    }

    @Override
    public Profile getProfile() {
        return profile;
    }

    @Override
    public String getDisplayLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isTextNode() {
        return isTextNode;
    }

    @Override
    public boolean canBeChildOf(NodeType type) {
        return true;
    }

    @Override
    public boolean canHaveChild(NodeType type) {
        return true;
    }

    @Override
    public List<NodeType> possibleChildren() {
        return profile.getAssignedNodeTypes();
    }

    public boolean equals(NodeType other) {
        return other.getClass().equals(this.getClass()) && other.getId().equals(this.id);
    }

    public int hashCode() {
        return this.getClass().hashCode() + this.id.hashCode();
    }
}
