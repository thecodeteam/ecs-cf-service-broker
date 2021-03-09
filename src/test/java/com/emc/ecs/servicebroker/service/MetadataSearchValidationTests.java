package com.emc.ecs.servicebroker.service;

import com.emc.ecs.management.sdk.model.SearchMetadata;
import com.emc.ecs.servicebroker.model.SearchMetadataDataType;
import com.emc.ecs.servicebroker.model.SystemMetadataName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;

import java.util.*;

import static com.emc.ecs.servicebroker.model.Constants.*;
import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class MetadataSearchValidationTests {

    public static final String WRONG_NAME = "some wrong name";
    public static final String SOME_USER_METADATA_NAME = "some_name";

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

        List<SearchMetadata> list = (List<SearchMetadata>) params.get(SEARCH_METADATA);
        Assert.assertEquals(1, list.size());
        SearchMetadata meta = list.get(0);

        assertEquals(SEARCH_METADATA_TYPE_SYSTEM, meta.getType());
        assertEquals(SystemMetadataName.LastModified.name(), meta.getName());
        assertEquals(SearchMetadataDataType.DateTime.name(), meta.getDatatype());
    }

    @Test
    public void validationAddsPrefixForUserKey() {
        Map<String, String> m = new HashMap<>();
        m.put(SEARCH_METADATA_TYPE, SEARCH_METADATA_TYPE_USER);
        m.put(SEARCH_METADATA_NAME, "abcd");
        m.put(SEARCH_METADATA_DATATYPE, SearchMetadataDataType.Decimal.name());

        Map<String, Object> params = EcsService.validateAndPrepareSearchMetadata(parameters(m));

        List<SearchMetadata> list = (List<SearchMetadata>) params.get(SEARCH_METADATA);
        Assert.assertEquals(1, list.size());
        SearchMetadata meta = list.get(0);

        assertEquals(SEARCH_METADATA_TYPE_USER, meta.getType());
        assertEquals(SEARCH_METADATA_USER_PREFIX + "abcd", meta.getName());
        assertEquals(SearchMetadataDataType.Decimal.name(), meta.getDatatype());
    }

    @Test
    public void nullListsOfMetadataAreEqual() {
        assertTrue("Null metadata search lists are equal", EcsService.isEqualSearchMetadataList(null, null));
        assertTrue("Empty metadata search lists are equal", EcsService.isEqualSearchMetadataList(new ArrayList<>(), new ArrayList<>()));
    }

    @Test
    public void nullAndNonemptyListsAreNotEqual() {
        List<SearchMetadata> list = Arrays.asList(
                new SearchMetadata(SEARCH_METADATA_TYPE_SYSTEM, SystemMetadataName.ContentEncoding.name(), SearchMetadataDataType.String.name())
        );
        assertFalse("Null and non empty metadata lists are not equal", EcsService.isEqualSearchMetadataList(null, list));
        assertFalse("Non empty list and null are not equal", EcsService.isEqualSearchMetadataList(list, null));

        assertFalse("Null amd empty metadata search lists are not equal", EcsService.isEqualSearchMetadataList(null, new ArrayList<>()));
        assertFalse("Empty and null metadata search lists are not equal", EcsService.isEqualSearchMetadataList(new ArrayList<>(), null));
    }

    @Test
    public void differentListsOfMetadataAreNotEqual() {
        List<SearchMetadata> list1 = Arrays.asList(
                new SearchMetadata(SEARCH_METADATA_TYPE_SYSTEM, SystemMetadataName.CreateTime.name(), SearchMetadataDataType.DateTime.name()),
                new SearchMetadata(SEARCH_METADATA_TYPE_USER, SOME_USER_METADATA_NAME, SearchMetadataDataType.Integer.name())
        );
        List<SearchMetadata> list2 = Arrays.asList(
                new SearchMetadata(SEARCH_METADATA_TYPE_SYSTEM, SystemMetadataName.ContentEncoding.name(), SearchMetadataDataType.String.name())
        );
        assertFalse("Lists with different entries are not equal", EcsService.isEqualSearchMetadataList(list1, list2));
    }

    @Test
    public void sameMetadataListsAreEqual() {
        List<SearchMetadata> list1 = Arrays.asList(
                new SearchMetadata(SEARCH_METADATA_TYPE_SYSTEM, SystemMetadataName.ContentType.name(), SearchMetadataDataType.String.name()),
                new SearchMetadata(SEARCH_METADATA_TYPE_USER, SOME_USER_METADATA_NAME, SearchMetadataDataType.Decimal.name())
        );

        List<SearchMetadata> list2 = Arrays.asList(
                new SearchMetadata(SEARCH_METADATA_TYPE_SYSTEM, SystemMetadataName.ContentType.name(), SearchMetadataDataType.String.name()),
                new SearchMetadata(SEARCH_METADATA_TYPE_USER, SOME_USER_METADATA_NAME, SearchMetadataDataType.Decimal.name())
        );

        assertTrue("Lists with entries with same metadata entries are equal", EcsService.isEqualSearchMetadataList(list1, list2));
    }

    private static Map<String, Object> parameters(Map<String, String> metadataParams) {
        List<Map<String, String>> metadataList = new ArrayList<>();
        metadataList.add(metadataParams);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SEARCH_METADATA, metadataList);

        return parameters;
    }
}
