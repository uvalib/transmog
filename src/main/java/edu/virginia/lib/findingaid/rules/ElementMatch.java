package edu.virginia.lib.findingaid.rules;

import edu.virginia.lib.findingaid.structure.Element;

public class ElementMatch {

    private Element el;
    
    private String id;
    
    public ElementMatch(Element el, String id) {
        this.el = el;
        this.id = id;
    }
    
    public Element getElement() {
        return el;
    }
    
    public String getId() {
        return id;
    }

}
