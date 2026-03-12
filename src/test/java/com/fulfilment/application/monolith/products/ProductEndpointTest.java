package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  final String path = "/product";

  @Test
  public void testListProducts() {

    given()
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .body(containsString("name"));
  }

  @Test
  public void testCreateProduct() {

    String json = """
      {
        "name": "NEW_PRODUCT",
        "description": "test product",
        "stock": 20
      }
      """;

    given()
            .body(json)
            .header("Content-Type", "application/json")
            .when()
            .post(path)
            .then()
            .statusCode(201)
            .body(containsString("NEW_PRODUCT"));
  }

  @Test
  public void testGetSingleProduct() {

    String json = """
      {
        "name": "GET_PRODUCT",
        "stock": 5
      }
      """;

    int id =
            given()
                    .body(json)
                    .header("Content-Type", "application/json")
                    .when()
                    .post(path)
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

    given()
            .when()
            .get(path + "/" + id)
            .then()
            .statusCode(200)
            .body(containsString("GET_PRODUCT"));
  }

  @Test
  public void testUpdateProduct() {

    String json = """
      {
        "name": "UPDATE_PRODUCT",
        "stock": 5
      }
      """;

    int id =
            given()
                    .body(json)
                    .header("Content-Type", "application/json")
                    .when()
                    .post(path)
                    .then()
                    .extract()
                    .path("id");

    String update = """
      {
        "name": "UPDATED_PRODUCT",
        "description": "updated description",
        "stock": 99
      }
      """;

    given()
            .body(update)
            .header("Content-Type", "application/json")
            .when()
            .put(path + "/" + id)
            .then()
            .statusCode(200)
            .body(containsString("UPDATED_PRODUCT"));
  }

  @Test
  public void testDeleteProduct() {

    String json = """
      {
        "name": "DELETE_PRODUCT",
        "stock": 5
      }
      """;

    int id =
            given()
                    .body(json)
                    .header("Content-Type", "application/json")
                    .when()
                    .post(path)
                    .then()
                    .extract()
                    .path("id");

    given()
            .when()
            .delete(path + "/" + id)
            .then()
            .statusCode(204);
  }

  @Test
  public void testCreateProductWithIdFails() {

    String json = """
      {
        "id": 10,
        "name": "INVALID_PRODUCT"
      }
      """;

    given()
            .body(json)
            .header("Content-Type", "application/json")
            .when()
            .post(path)
            .then()
            .statusCode(422);
  }

  @Test
  public void testUpdateProductWithoutNameFails() {

    String json = """
      {
        "description": "missing name",
        "stock": 5
      }
      """;

    given()
            .body(json)
            .header("Content-Type", "application/json")
            .when()
            .put(path + "/1")
            .then()
            .statusCode(422);
  }

  @Test
  public void testGetNonExistingProduct() {

    given()
            .when()
            .get(path + "/9999")
            .then()
            .statusCode(404);
  }

  @Test
  public void testDeleteNonExistingProduct() {

    given()
            .when()
            .delete(path + "/9999")
            .then()
            .statusCode(404);
  }

  @Test
  public void testUpdateNonExistingProduct() {

    String json = """
      {
        "name": "DOES_NOT_EXIST",
        "stock": 10
      }
      """;

    given()
            .body(json)
            .header("Content-Type", "application/json")
            .when()
            .put(path + "/9999")
            .then()
            .statusCode(404);
  }

  @Test
  public void testListAfterCreation() {

    String json = """
      {
        "name": "LIST_TEST_PRODUCT",
        "stock": 15
      }
      """;

    given()
            .body(json)
            .header("Content-Type", "application/json")
            .when()
            .post(path)
            .then()
            .statusCode(201);

    given()
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .body(containsString("LIST_TEST_PRODUCT"));
  }

  @Test
  public void testErrorMapperResponse() {

    given()
            .when()
            .get(path + "/999999")
            .then()
            .statusCode(404)
            .body(containsString("does not exist"));
  }
}