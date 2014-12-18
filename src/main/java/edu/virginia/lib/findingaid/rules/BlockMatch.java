package edu.virginia.lib.findingaid.rules;

import java.util.List;

public interface BlockMatch extends Iterable<ElementMatch> {

    public List<ElementMatch> getElementMatch(String id);

    public List<ElementMatch> getMatches();
}
