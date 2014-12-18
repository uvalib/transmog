package edu.virginia.lib.findingaid.structure;

import java.util.List;

public interface NodeType {
    
    public Schema getSchema();
    
    public String getDisplayLabel();
    
    public String getDescription();
    
    public String getId();

    /**
     * Currently means a node that can have text content.  The text is always considered before child nodes.  
     * TODO: change the way stuff works so that only one node type can have text and it can't have children.
     */
    public boolean isTextNode();
    
    public boolean canBeChildOf(NodeType type);
    
    public boolean canHaveChild(NodeType type);
    
    public List<NodeType> possibleChildren();

}
