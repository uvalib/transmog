package edu.virginia.lib.findingaid.rules;

import edu.virginia.lib.findingaid.structure.Element;

import java.util.regex.Matcher;

public class ElementMatch {

    private Element el;

    private ElementPattern p;

    public ElementMatch(Element el, ElementPattern p) {
        this.el = el;
        this.p = p;
    }

    public Element getElement() {
        return el;
    }

    public Element createElement(int matchingGroup) {
        if (matchingGroup == 0) {
            return el;
        } else {
            Matcher m = p.getPattern().matcher(el.getContentAsString());
            if (!m.matches()) {
                throw new IllegalStateException();
            }
            return new Element(el.getType(), m.group(matchingGroup));
        }
    }

    public String getId() {
        return p.getName();
    }

}
