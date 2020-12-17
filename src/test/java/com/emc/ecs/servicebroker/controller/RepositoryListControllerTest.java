package com.emc.ecs.servicebroker.controller;

import com.emc.ecs.servicebroker.config.BrokerConfig;
import com.emc.ecs.servicebroker.service.S3Service;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@PowerMockRunnerDelegate(PowerMockRunner.class)
@PrepareForTest(S3Service.class)
//@AutoConfigureMockMvc
@WebMvcTest(RepositoryListController.class)
public class RepositoryListControllerTest {

    @Autowired
    private MockMvc mockMvc;

//    @MockBean
    @Mock
    private S3Service s3;

    @Autowired
    private BrokerConfig broker;

    @Test
    public void isOk() throws Exception {
        this.mockMvc.perform(get("/v2/repository/instances?pageSize=2"));

        ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> markerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> pageSizeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(s3).listObjects(prefixCaptor.capture(), markerCaptor.capture(), pageSizeCaptor.capture());
        assertEquals(Integer.valueOf(2),pageSizeCaptor.getValue());
    }
}
