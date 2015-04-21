package edu.virginia.lib.findingaid.rules;

import java.util.regex.Pattern;

public interface ElementPattern {

    public String getName();

    public Pattern getPattern();

    /**
     * Will always return a value of "any" or "first" to indicate whether this pattern applies
     * anywhere in the elements or only when matching the first element.
     */
    public String getPosition();

    public boolean matchesMultiple();
}
