package net.acesinc.data.json.generator.log;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.net.www.protocol.http.AuthScheme;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkLogicLogger implements EventLogger {

    private static final Logger log = LogManager.getLogger(MarkLogicLogger.class);

    public static final String HOST_PROP_NAME = "host";
    public static final String PORT_PROP_NAME = "port";
    public static final String USER_PROP_NAME = "username";
    public static final String PASS_PROP_NAME = "password";
    public static final String AUTHTYPE_PROP_NAME = "authtype";
    public static final String METADATA_FIELDS = "metadataPaths";
    public static final String METADATA_FIELD_NAMES = "metadataNames";
    public static final String PROPERTY_FIELDS = "propertyPaths";
    public static final String PROPERTY_FIELD_NAMES = "propertyNames";
    public static final String DOCUMENT_URI_PROP_NAME = "uriPath";
    public static final String COLLECTIONS_PROP_NAME = "collections";
    public static final String FIRST_NODE = "firstNode";

    private DatabaseClient client;
    private String host;
    private int port;
    private String username;
    private String password;
    private AuthScheme auth;
    private String uriField;
    private String firstNode;

    private List<String> metadataFields;
    private List<String> metadataFieldNames;
    private List<String> propertyFields;
    private List<String> propertyFieldNames;
    private List<String> collections;

    public MarkLogicLogger(Map<String, Object> props) {
        this.host = (String) props.get(HOST_PROP_NAME);
        this.port = (Integer) props.get(PORT_PROP_NAME);
        this.username = (String) props.get(USER_PROP_NAME);
        this.password = (String) props.get(PASS_PROP_NAME);
        this.auth = AuthScheme.valueOf((String) props.get(AUTHTYPE_PROP_NAME));
        this.metadataFields = Arrays.asList(((String) props.get(METADATA_FIELDS)).split(","));
        this.propertyFields = Arrays.asList(((String) props.get(PROPERTY_FIELDS)).split(","));
        this.metadataFieldNames = Arrays.asList(((String) props.get(METADATA_FIELD_NAMES)).split(","));
        this.propertyFieldNames = Arrays.asList(((String) props.get(PROPERTY_FIELD_NAMES)).split(","));
        this.collections = Arrays.asList(((String) props.get(COLLECTIONS_PROP_NAME)).split(","));
        this.uriField = (String) props.get(DOCUMENT_URI_PROP_NAME);
        this.firstNode = (String) props.get(FIRST_NODE);

        if (!(this.auth.equals(AuthScheme.BASIC) || this.auth.equals(AuthScheme.DIGEST))) {
            throw new UnsupportedOperationException("Currently only BASIC or DIGEST supported.");
        }

//        log.debug("Creating Connection to Database...");
        if (this.auth.equals(AuthScheme.DIGEST)) {
            client = DatabaseClientFactory.newClient(host,port, new DatabaseClientFactory.DigestAuthContext(this.username, this.password));
//            log.debug("Client Created using DIGEST authentication");
        } else { // must be BASIC then
            client = DatabaseClientFactory.newClient(host,port, new DatabaseClientFactory.BasicAuthContext(this.username, this.password));
//            log.debug("Client Created using BASIC authentication");
        }

    }

    @Override
    public void logEvent(String event, Map<String, Object> producerConfig) {
        CustomEvent ce = processEvent(event);

        JSONDocumentManager dm = client.newJSONDocumentManager();

        StringHandle sh = new StringHandle(ce.getCleaned());

        sh.withFormat(Format.JSON);
        sh.withMimetype("application/json");

        DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
        Map<String,String> metadata = ce.getMetadata();
        for (String key:metadata.keySet()) {
            metadataHandle.getMetadataValues().add(key,metadata.get(key));
        }
        Map<String,String> props = ce.getProperties();
        for (String key:props.keySet()) {
            metadataHandle.getProperties().put(key,props.get(key));
        }

        metadataHandle.getCollections().addAll(collections);

        dm.write(ce.getUri(), metadataHandle, sh);
    }

    private class CustomEvent {
        private String original;
        private String cleaned;
        private String uri;
        private Map<String, String> metadata = new HashMap<String, String>();
        private Map<String, String> properties = new HashMap<String, String>();

        public CustomEvent(String event) {
            this.original = event;
        }

        public String getOriginal() { return original; }
        public String getCleaned() { return cleaned; }
        public void setCleaned(String cleaned) { this.cleaned = cleaned; }
        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
        public Map<String,String> getMetadata() { return metadata; }
        public Map<String, String> getProperties() { return properties;}
        public void addMetadata(String key, String value) { metadata.put(key, value);}
        public void addProperty(String key, String value) { properties.put(key, value);}

    }

    private CustomEvent processEvent(String event) {
        CustomEvent ce = new CustomEvent(event);
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(event);

        ce.setUri((String) JsonPath.read(document, this.uriField));

        for (int i=0; i < metadataFieldNames.size(); i++) {
//            log.debug("Attempting to add metadata for: " + metadataFieldNames.get(i));
//            log.debug("Read Result was: " + JsonPath.read(document,metadataFields.get(i)));
            ce.addMetadata(metadataFieldNames.get(i), (String) JsonPath.read(document,metadataFields.get(i)));
        }
        for (int i=0; i < propertyFieldNames.size(); i++) {
//            log.debug("Attempting to add property for: " + propertyFieldNames.get(i));
            ce.addProperty(propertyFieldNames.get(i), (String) JsonPath.read(document,propertyFields.get(i)));
        }

//        log.debug("event: " + event);
//        log.debug("event.indexOf " + firstNode + ": "  );
        if (!firstNode.equals(null)) {
            ce.setCleaned("{" + event.substring(event.indexOf(firstNode) - 1));
        } else ce.setCleaned(ce.getOriginal());

        return ce;
    }

    @Override
    public void shutdown() {
        client.release() ;
    }


}
