package edu.virginia.lib.findingaid.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultBlockMatch implements BlockMatch {

    private List<ElementMatch> elementMatches;

    public DefaultBlockMatch(List<ElementMatch> matches) {
        this.elementMatches = matches;

    }

    @Override
    public List<ElementMatch> getElementMatch(String id) {
        List<ElementMatch> matches = new ArrayList<ElementMatch>();
        for (ElementMatch m : elementMatches) {
            if (m.getId().equals(id)) {
                matches.add(m);
            }
        }
        return matches;
    }

    @Override
    public List<ElementMatch> getMatches() {
        return elementMatches;
    }

    @Override
    public Iterator<ElementMatch> iterator() {
        return this.elementMatches.iterator();
    }
}
