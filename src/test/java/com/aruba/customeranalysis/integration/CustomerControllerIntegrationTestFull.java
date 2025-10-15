package com.aruba.customeranalysis.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.aruba.customeranalysis.config.security.JwtUtil;
import com.aruba.customeranalysis.domain.CustomerServiceRepositoryInterface;
import com.aruba.customeranalysis.domain.dto.CustomerExpiredServiceDTO;
import com.aruba.customeranalysis.domain.dto.ServiceSummaryDTO;
import com.aruba.customeranalysis.domain.event.CustomerEvent;
import com.aruba.customeranalysis.infrastructure.persistence.model.security.RoleEntity;
import com.aruba.customeranalysis.infrastructure.persistence.model.security.UserEntity;
import com.aruba.customeranalysis.infrastructure.persistence.security.RoleRepository;
import com.aruba.customeranalysis.infrastructure.persistence.security.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Testcontainers
class CustomerControllerIntegrationTestFull {

    // ---------- CONTAINERS ----------
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withDatabaseName("customeranalysisdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    // ---------- DYNAMIC PROPERTIES ----------
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.customer-topic", () -> "alerts.customer_expired");
    }

    // ---------- AUTOWIRED COMPONENTS ----------
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerServiceRepositoryInterface customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private ObjectMapper objectMapper;
    private String adminToken;
    private String operatorToken;
    private Consumer<String, Object> consumer;
    
    private String csvContent;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        RoleEntity adminRole = new RoleEntity(null, "ROLE_ADMIN");
        RoleEntity operatorRole = new RoleEntity(null, "ROLE_OPERATOR");
        roleRepository.saveAll(List.of(adminRole, operatorRole));

        UserEntity adminUserEntity = new UserEntity(null, "admin", "password", Set.of(adminRole));
        UserEntity operatorUserEntity = new UserEntity(null, "operator", "password", Set.of(operatorRole));
        userRepository.saveAll(List.of(adminUserEntity, operatorUserEntity));

        UserDetails adminUser = User.withUsername("admin").password("password").roles("ADMIN").build();
        adminToken = jwtUtil.generateToken(adminUser);

        UserDetails operatorUser = User.withUsername("operator").password("password").roles("OPERATOR").build();
        operatorToken = jwtUtil.generateToken(operatorUser);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                kafka.getBootstrapServers(),
                UUID.randomUUID().toString()
        );

        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CustomerEvent.class.getName());
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", JsonDeserializer.class);

        DefaultKafkaConsumerFactory<String, Object> factory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        consumer = factory.createConsumer();
        consumer.subscribe(List.of("alerts.customer_expired"));
        
        csvContent = """
                customer_id,service_type,activation_date,expiration_date,amount,status
                CUST001,HOSTING,2024-01-01,2025-12-31,99.90,ACTIVE
                CUST002,PEC,2020-03-15,2026-03-15,35.50,ACTIVE
                CUST001,PEC,2020-01-01,2023-12-31,99.90,EXPIRED
                CUST001,SPID,2021-01-01,2022-12-31,99.90,EXPIRED
                CUST001,FATTURAZIONE,2019-01-01,2023-12-31,99.90,EXPIRED
                CUST001,HOSTING,2021-01-01,2022-12-31,99.90,EXPIRED
                CUST001,SPID,2020-01-01,2022-12-31,99.90,EXPIRED
                CUST001,PEC,2022-01-01,2023-12-31,99.90,EXPIRED
                """;

    }

    @AfterEach
    void tearDown() {
    	
        if (consumer != null) {
            consumer.close();
        }
        
        userRepository.deleteAll();
        roleRepository.deleteAll();
        
    }

    @Test
    void uploadCsv_withValidToken_shouldReturnOkWithMessage() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "customers-service.csv",
            "text/csv",
            csvContent.getBytes()
        );

    	mockMvc.perform(multipart("/api/customerservice/upload").file(file)
    			.header("Authorization", "Bearer " + adminToken))    			
	        .andExpect(status().isOk())
	        .andExpect(content().string(containsString("CSV successfully uploaded and elaborated.")));
    	
    	List<ServiceSummaryDTO> activeServices = customerRepository.findActiveServicesByType();
        assertThat(activeServices).isNotEmpty();
        
        List<CustomerExpiredServiceDTO> expiredServices = customerRepository.findCustomersWithExpiredServices();
        assertThat(expiredServices).isNotEmpty();
    	       
    }

    @Test
    void getSummaryReport_withValidToken_shouldReturnExcelReport() throws Exception {
    	
    	MockMultipartFile file = new MockMultipartFile(
                "file",
                "customers-service.csv",
                "text/csv",
                csvContent.getBytes()
            );

       mockMvc.perform(multipart("/api/customerservice/upload").file(file)
    			.header("Authorization", "Bearer " + adminToken))    			
	        .andExpect(status().isOk());

    	MvcResult mvcResult = mockMvc.perform(get("/api/customerservice/summary")
                 .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk()).andReturn();
        
        MockHttpServletResponse response = mvcResult.getResponse();
        
        assertThat(response.getContentType())
            .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        
        byte[] fileContent = response.getContentAsByteArray();
        
        try (XSSFWorkbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(fileContent))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(4);
        }
        
        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        assertThat(records.count()).isGreaterThan(0);
        
    }

    
}
