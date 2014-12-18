package edu.virginia.lib.findingaid.rules;

import edu.virginia.lib.findingaid.structure.Element;

import java.util.List;

public interface BlockMatcher {

    public List<BlockMatch> findMatches(Element el);
}
