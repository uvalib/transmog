package edu.virginia.lib.findingaid.service;


import edu.virginia.lib.findingaid.structure.Document;
import edu.virginia.lib.findingaid.structure.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentStore {

    private Map<String, Document> inMemoryStore;

    public DocumentStore() throws IOException {
        this.inMemoryStore = new HashMap<>();
        DocumentConverter c = new DocumentConverter();
    }


    /**
     * Stores a new document with the given id and returns that id.
     */
    public String addDocument(Document document, String id) {
        inMemoryStore.put(id, document);
        return id;
    }

    /**
     * Fetches a document by id.
     */
    public Document getDocument(String id) {
        return inMemoryStore.get(id);
    }

    public List<String> listDocumentIds() {
        ArrayList<String> ids = new ArrayList<String>(inMemoryStore.size());
        ids.addAll(inMemoryStore.keySet());
        Collections.sort(ids);
        return ids;
    }

    private static DocumentStore DOCUMENT_STORE = null;

    public static DocumentStore getDocumentStore() {
        try {
            if (DOCUMENT_STORE == null) {
                DOCUMENT_STORE = new DocumentStore();
            }
            return DOCUMENT_STORE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
