package edu.virginia.lib.findingaid.rules;

public interface Rule {

    public String getDescription();
    
    public BlockMatcher getWhenClause();
    
    public FragmentAction getAction();

}
