package edu.virginia.lib.findingaid.rules;

import java.util.regex.Pattern;

public interface ElementPattern {
    
    public String getName();
    
    public Pattern getPattern();
    
    public boolean matchesMultiple();
}
