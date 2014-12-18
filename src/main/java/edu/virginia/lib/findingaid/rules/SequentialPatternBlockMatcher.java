package edu.virginia.lib.findingaid.rules;

import edu.virginia.lib.findingaid.structure.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SequentialPatternBlockMatcher implements BlockMatcher {

    private List<ElementPattern> patterns;

    public SequentialPatternBlockMatcher(List<ElementPattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public List<BlockMatch> findMatches(Element el) {
        List<BlockMatch> blockMatches = new ArrayList<BlockMatch>();
        for (int ei = 0; ei < el.getChildren().size(); ei ++) {
            List<ElementMatch> matches = reluctantMatch(el.getChildren(), ei, patterns);
            if (matches != null) {
                ei += matches.size();
                blockMatches.add(new DefaultBlockMatch(matches));
            }
        }
        return blockMatches;
    }

    private List<ElementMatch> reluctantMatch(List<Element> elements, int index, List<ElementPattern> patterns) {
        List<ElementMatch> match = new ArrayList<ElementMatch>();
        PatternCursor c = new PatternCursor(patterns);
        for (int i = index; i < elements.size(); i ++) {
            Element el = elements.get(i);
            ElementMatch m = c.addsToMatch(el);
            if (m == null) {
                break;
            } else {
                match.add(m);
            }
        }
        if (c.fullMatch) {
            return match;
        }
        return null;
    }

    private class PatternCursor {

        List<ElementPattern> patterns;

        int cursor;

        ElementPattern optional;
        ElementPattern required;

        boolean fullMatch;

        public PatternCursor(List<ElementPattern> patterns) {
            this.patterns = patterns;
            cursor = 1;
            optional = null;
            required = patterns.get(0);
        }

        ElementMatch addsToMatch(Element el) {
            if (required != null && matchesPattern(el, required.getPattern())) {
                ElementMatch m = new ElementMatch(el, required.getName());
                advancePatternCursor();
                return m;
            } else if (optional != null && matchesPattern(el, optional.getPattern())) {
                fullMatch = true;
                return new ElementMatch(el, optional.getName());
            } else {
                fullMatch = true;
                return null;
            }
        }

        private void advancePatternCursor() {
            if (cursor < patterns.size()) {
                if (required.matchesMultiple()) {
                    optional = required;
                }
                required = patterns.get(cursor++);
            } else if (required.matchesMultiple()) {
                optional = required;
                required = null;
            } else {
                // no more patterns
            }
        }

        private boolean matchesPattern(Element el, Pattern p) {
            if (!el.isUnassigned()) {
                return false;
            } else {
                final boolean matches = p.matcher(el.getContent()).matches();
                return matches;
            }
        }

    }

}
