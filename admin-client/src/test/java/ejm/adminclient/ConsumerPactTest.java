package ejm.adminclient;

import java.io.IOException;
import java.time.LocalDateTime;

import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactSpecVersion;
import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.fest.assertions.Assertions;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ConsumerPactTest extends ConsumerPactTestMk2 {
    private Category createCategory(Integer id, String name) {
        Category cat = new TestCategoryObject(id, LocalDateTime.parse("2002-01-01T00:00:00"), 1);
        cat.setName(name);
        cat.setVisible(Boolean.TRUE);
        cat.setHeader("header");
        cat.setImagePath("n/a");

        return cat;
    }

    @Override
    protected RequestResponsePact createPact(PactDslWithProvider builder) {
        Category top = createCategory(0, "Top");

        Category transport = createCategory(1000, "Transportation");
        transport.setParent(top);

        Category autos = createCategory(1002, "Automobiles");
        autos.setParent(transport);

        Category cars = createCategory(1009, "Cars");
        cars.setParent(autos);

        Category toyotas = createCategory(1015, "Toyota Cars");
        toyotas.setParent(cars);

        Category bikes = createCategory(1001, "Bikes");
        bikes.setParent(transport);

        Category[] categories = {top, transport, autos, cars, toyotas};

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        try {
            return builder
                    .uponReceiving("Retrieve a category")
                        .path("/admin/category/1015")
                        .method("GET")
                    .willRespondWith()
                        .status(200)
                        .body(mapper.writeValueAsString(toyotas))
                    .uponReceiving("All categories")
                        .path("/admin/category/")
                        .method("GET")
                    .willRespondWith()
                        .status(200)
                        .body(mapper.writeValueAsString(categories))
                    .uponReceiving("Create a category")
                        .path("/admin/category/")
                        .method("POST")
                    .willRespondWith()
                        .status(201)
                        .body(mapper.writeValueAsString(bikes))
                    .uponReceiving("Update a category")
                        .path("/admin/category/1015")
                        .method("PUT")
                    .willRespondWith()
                        .status(200)
                        .body(mapper.writeValueAsString(toyotas))
                    .uponReceiving("Delete a category")
                        .path("/admin/category/1015")
                        .method("DELETE")
                    .willRespondWith()
                        .status(204)
                    .toPact();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected String providerName() {
        return "admin_service_provider";
    }

    @Override
    protected String consumerName() {
        return "admin_client_consumer";
    }

    @Override
    protected PactSpecVersion getSpecificationVersion() {
        return PactSpecVersion.V3;
    }

    @Override
    protected void runTest(MockServer mockServer) throws IOException {

        AdminClient adminClient = new AdminClient(mockServer.getUrl());

        // verifying getCategory:
        Category cat = new AdminClient(mockServer.getUrl()).getCategory(1015);
        Assertions.assertThat(cat).isNotNull();
        assertThat(cat.getId()).isEqualTo(1015);
        assertThat(cat.getName()).isEqualTo("Toyota Cars");
        assertThat(cat.getHeader()).isEqualTo("header");
        assertThat(cat.getImagePath()).isEqualTo("n/a");
        assertThat(cat.isVisible()).isTrue();
        assertThat(cat.getParent()).isNotNull();
        assertThat(cat.getParent().getId()).isEqualTo(1009);


        // verifying allCategories:
        Category[] categories = adminClient.allCategories();
        assertThat(categories.length).isEqualTo(5);
        assertThat(categories[0].getId()).isEqualTo(0);
        assertThat(categories[0].getName()).isEqualTo("Top");
        assertThat(categories[0].getParent()).isNull();
        assertThat(categories[1].getId()).isEqualTo(1000);
        assertThat(categories[1].getName()).isEqualTo("Transportation");
        assertThat(categories[1].getParent().getId()).isEqualTo(0);

        // verifying addCategory:
        Category bikes = createCategory(1001, "Bikes");
        Category newCat = adminClient.addCategory(bikes);
        Assertions.assertThat(cat).isNotNull();
        assertThat(newCat.getId()).isEqualTo(1001);
        assertThat(newCat.getName()).isEqualTo("Bikes");
        assertThat(newCat.getHeader()).isEqualTo("header");
        assertThat(newCat.getImagePath()).isEqualTo("n/a");
        assertThat(newCat.isVisible()).isTrue();
        assertThat(newCat.getParent()).isNotNull();
        assertThat(newCat.getParent().getId()).isEqualTo(1000);
        assertThat(newCat.getParent().getName()).isEqualTo("Transportation");

        // verifying updateCategory:
        cat.setName("Ford Cars");
        cat.setHeader("NewHeader");
        Category updatedCat = adminClient.updateCategory(1015,cat);
        Assertions.assertThat(cat).isNotNull();
        assertThat(cat.getId()).isEqualTo(1015);
        assertThat(cat.getName()).isEqualTo("Ford Cars");
        assertThat(cat.getHeader()).isEqualTo("NewHeader");
        assertThat(cat.getImagePath()).isEqualTo("n/a");
        assertThat(cat.isVisible()).isTrue();
        assertThat(cat.getParent()).isNotNull();
        assertThat(cat.getParent().getId()).isEqualTo(1009);


        // verifying updateCategory:
        adminClient.deleteCategory(1015);
        cat = new AdminClient(mockServer.getUrl()).getCategory(1015);
        Assertions.assertThat(cat).isNotNull();
        assertThat(cat.getId()).isEqualTo(1015);
        assertThat(cat.getName()).isEqualTo("Toyota Cars");
        assertThat(cat.getHeader()).isEqualTo("header");
        assertThat(cat.getImagePath()).isEqualTo("n/a");
        assertThat(cat.isVisible()).isTrue();
        assertThat(cat.getParent()).isNotNull();
        assertThat(cat.getParent().getId()).isEqualTo(1009);

    }
}
