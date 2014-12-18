package edu.virginia.lib.findingaid.service;

import edu.virginia.lib.findingaid.structure.Schema;
import edu.virginia.lib.findingaid.structure.XmlSerializedSchema;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SchemaStore {

    private List<Schema> schemaList;

    public SchemaStore() throws IOException, JAXBException {
        schemaList = new ArrayList<Schema>();
        schemaList.add(XmlSerializedSchema.loadSchema(getClass().getClassLoader().getResourceAsStream("uvaead-schema.xml")));
        schemaList.add(XmlSerializedSchema.loadSchema(getClass().getClassLoader().getResourceAsStream("modern-library-tei.xml")));
    }

    public Schema getDefaultSchema() {
        return schemaList.get(0);
    }

    public List<Schema> getSchemaList() {
        return schemaList;
    }

    public Schema getSchema(String name) {
        for (Schema s : schemaList) {
            if (s.getSchemaName().equals(name)) {
                return s;
            }
        }
        return null;
    }


    private static SchemaStore SCHEMA_STORE = null;

    public static SchemaStore getSchemaStore() {
        try {
            if (SCHEMA_STORE == null) {
                SCHEMA_STORE = new SchemaStore();
            }
            return SCHEMA_STORE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
