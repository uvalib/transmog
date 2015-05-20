package edu.virginia.lib.findingaid.structure;

import edu.virginia.lib.findingaid.service.ProfileStore;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.io.OutputStream;

public class Document {

    private Element rootEl;

    private String filename;

    private Profile profile;

    private Document() {
    }

    public Document(Element root, String filename) {
        this.rootEl = root;
        this.filename = filename;
        this.profile = root.getProfile();
    }

    public String getId() {
        return rootEl.id;
    }

    public Element getRootElement() {
        return rootEl;
    }

    public Profile getProfile() {
        return profile;
    }

    public String getOriginalFilename() {
        return filename;
    }

    public void serialize(OutputStream os) throws XMLStreamException {
        XMLEventFactory f = XMLEventFactory.newInstance();
        XMLEventWriter w = XMLOutputFactory.newFactory().createXMLEventWriter(os);
        w.add(f.createStartDocument());
        w.add(f.createStartElement("", "", "document"));
        w.add(f.createCharacters("\n"));

        // add the filename
        w.add(f.createStartElement("", "", "filename"));
        w.add(f.createCharacters(getOriginalFilename()));
        w.add(f.createEndElement("", "", "filename"));
        w.add(f.createCharacters("\n"));

        // add the profile
        w.add(f.createStartElement("", "", "profile"));
        w.add(f.createCharacters(getProfile().getProfileName()));
        w.add(f.createEndElement("", "", "profile"));
        w.add(f.createCharacters("\n"));

        // add the element tree
        w.add(f.createStartElement("", "", "root"));
        getRootElement().emitElement(w);
        w.add(f.createEndElement("", "", "root"));
        w.add(f.createCharacters("\n"));

        w.add(f.createEndElement("", "", "document"));
        w.add(f.createEndDocument());
    }

    public static Document parse(InputStream is, ProfileStore profiles) throws XMLStreamException {
        Document d = new Document();
        XMLEventFactory f = XMLEventFactory.newInstance();
        XMLEventReader r = XMLInputFactory.newFactory().createXMLEventReader(is);
        while (r.hasNext()) {
            XMLEvent e = r.nextEvent();
            if (e.isStartElement()) {
                final String localName = e.asStartElement().getName().getLocalPart();
                if (localName.equals("filename")) {
                    d.filename = getText(r);
                } else if (localName.equals("profile")) {
                    d.profile = profiles.getProfile(getText(r));
                } else if (localName.equals("root")) {
                    while (r.hasNext()) {
                        final XMLEvent n = r.nextEvent();
                        if (n.isStartElement()) {
                            d.rootEl = Element.parseElement(n.asStartElement(), r, d.profile);
                        } else if (n.isEndElement()) {
                            break;
                        }
                    }
                }
            }
        }
        return d;
    }

    public static String getText(XMLEventReader r) throws XMLStreamException {
        StringBuffer sb = new StringBuffer();
        while (r.hasNext()) {
            final XMLEvent next = r.nextEvent();
            if (next.isCharacters()) {
                sb.append(next.asCharacters().getData());
            } else if (next.isEndElement()) {
                return sb.toString();
            }
        }
        return sb.toString();
    }

    public boolean equals(Document otherDoc) {
        return (this.filename.equals(otherDoc.filename)
            && this.profile.getProfileName().equals(otherDoc.profile.getProfileName())
            && this.rootEl.equals(otherDoc.rootEl));
    }

    public int hashCode() {
        return this.filename.hashCode() + this.profile.getProfileName().hashCode() + this.rootEl.hashCode();

   }

}
