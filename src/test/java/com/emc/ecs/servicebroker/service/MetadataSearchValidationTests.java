package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.model.SearchMetadataDataType;
import com.emc.ecs.servicebroker.model.SystemMetadataName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emc.ecs.servicebroker.model.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class MetadataSearchValidationTests {

    public static final String WRONG_NAME = "some wrong name";

    @Test
    public void validationFailsOnEmptyName() {
        try {
            EcsService.validateAndPrepareSearchMetadata(parameters(new HashMap<>()));
            Assert.fail();
        } catch (ServiceBrokerInvalidParametersException e) {
            assertTrue(e.getMessage().endsWith("Invalid search metadata: name is not provided"));
        }
    }

    @Test
    public void validationFailsOnBadDatatype() {
        Map<String, String> m = new HashMap<>();
        m.put(SEARCH_METADATA_NAME, "some name");
        m.put(SEARCH_METADATA_DATATYPE, WRONG_NAME);

        try {
            EcsService.validateAndPrepareSearchMetadata(parameters(m));
            Assert.fail();
        } catch (ServiceBrokerInvalidParametersException e) {
            assertTrue(
                    "Exception should point to wrong datatype name: " + e.getMessage(),
                    e.getMessage().contains("Invalid search metadata datatype") && e.getMessage().contains(WRONG_NAME)
            );
        }
    }

    @Test
    public void validationFailsOnBadType() {
        Map<String, String> m = new HashMap<>();
        m.put(SEARCH_METADATA_NAME, "some name");
        m.put(SEARCH_METADATA_DATATYPE, SearchMetadataDataType.Integer.name());
        m.put(SEARCH_METADATA_TYPE, WRONG_NAME);

        try {
            EcsService.validateAndPrepareSearchMetadata(parameters(m));
            Assert.fail();
        } catch (ServiceBrokerInvalidParametersException e) {
            assertTrue(
                    "Exception should point to wrong metadata type name: " + e.getMessage(),
                    e.getMessage().contains("Invalid type specified for search metadata") && e.getMessage().contains(WRONG_NAME)
            );
        }
    }

    @Test
    public void validationFailsOnWrongSystemKeyName() {
        Map<String, String> m = new HashMap<>();
        m.put(SEARCH_METADATA_TYPE, SEARCH_METADATA_TYPE_SYSTEM);
        m.put(SEARCH_METADATA_NAME, WRONG_NAME);

        try {
            EcsService.validateAndPrepareSearchMetadata(parameters(m));
            Assert.fail();
        } catch (ServiceBrokerInvalidParametersException e) {
            assertTrue(e.getMessage().contains("Invalid system search metadata name"));
        }
    }

    @Test
    public void validationFailsOnWrongDatatypeForSystemKey() {
        Map<String, String> m = new HashMap<>();
        m.put(SEARCH_METADATA_NAME, SystemMetadataName.Expiration.name());
        m.put(SEARCH_METADATA_DATATYPE, SearchMetadataDataType.Integer.name());

        try {
            EcsService.validateAndPrepareSearchMetadata(parameters(m));
            Assert.fail();
        } catch (ServiceBrokerInvalidParametersException e) {
            assertTrue(e.getMessage().contains("Invalid system search metadata '" + SystemMetadataName.Expiration.name() + "' datatype"));
        }
    }

    @Test
    public void validationAddsTypeAndDatatypeForSystemKey() {
        Map<String, String> m = new HashMap<>();
        m.put(SEARCH_METADATA_NAME, SystemMetadataName.LastModified.name());

        Map<String, Object> params = EcsService.validateAndPrepareSearchMetadata(parameters(m));

        List<Map<String, String>> list = (List<Map<String, String>>) params.get(SEARCH_METADATA);
        Assert.assertEquals(1, list.size());
        Map<String, String> meta = list.get(0);

        assertEquals(SEARCH_METADATA_TYPE_SYSTEM, meta.get(SEARCH_METADATA_TYPE));
        assertEquals(SystemMetadataName.LastModified.name(), meta.get(SEARCH_METADATA_NAME));
        assertEquals(SearchMetadataDataType.DateTime.name(), meta.get(SEARCH_METADATA_DATATYPE));
    }

    @Test
    public void validationAddsPrefixForUserKey() {
        Map<String, String> m = new HashMap<>();
        m.put(SEARCH_METADATA_TYPE, SEARCH_METADATA_TYPE_USER);
        m.put(SEARCH_METADATA_NAME, "abcd");
        m.put(SEARCH_METADATA_DATATYPE, SearchMetadataDataType.Decimal.name());

        Map<String, Object> params = EcsService.validateAndPrepareSearchMetadata(parameters(m));

        List<Map<String, String>> list = (List<Map<String, String>>) params.get(SEARCH_METADATA);
        Assert.assertEquals(1, list.size());
        Map<String, String> meta = list.get(0);

        assertEquals(SEARCH_METADATA_TYPE_USER, meta.get(SEARCH_METADATA_TYPE));
        assertEquals(SEARCH_METADATA_USER_PREFIX + "abcd", meta.get(SEARCH_METADATA_NAME));
        assertEquals(SearchMetadataDataType.Decimal.name(), meta.get(SEARCH_METADATA_DATATYPE));
    }

    private static Map<String, Object> parameters(Map<String, String> metadataParams) {
        List<Map<String, String>> metadataList = new ArrayList<>();
        metadataList.add(metadataParams);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SEARCH_METADATA, metadataList);

        return parameters;
    }
}
