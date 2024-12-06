import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TestCreateUser {
    private String token;

    @Before
    public void setUp() {
        baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    public void testRegisterUserSuccessfully() {
        User user = new User("examplr.praktikum@gmail.com", "praktikum", "praktikum");

        Response response = registerUser(user);

        validateSuccessfulRegistration(response);

        saveToken(response);
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void testRegisterUserRepeat() {
        User user = new User("examplerrr.praktikum@gmail.com", "praktikum", "praktikum");

        Response firstResponse = registerUser(user);
        validateSuccessfulRegistration(firstResponse);

        Response secondResponse = registerUser(user);
        validateDuplicateRegistration(secondResponse);

        saveToken(firstResponse);
    }

    @Test
    @DisplayName("Создание пользователя: одно из обязательных полей не заполнено")
    public void testRegisterUserWithoutParameter() {
        Collection<Object[]> testData = UserData.getTestData();

        for (Object[] data : testData) {
            String email = (String) data[0];
            String password = (String) data[1];
            String name = (String) data[2];

            User user = new User(email, password, name);

            Response response = registerUser(user);
            validateMissingParameterResponse(response);
        }
    }

    @After
    public void tearDown() {
        if (token != null) {
            deleteUser(token);
        }
    }

    @Step("Регистрация пользователя")
    private Response registerUser(User user) {
        return given()
                .header("Content-Type", "application/json")
                .body(user)
                .when()
                .post("/api/auth/register");
    }

    @Step("Проверка успешной регистрации")
    private void validateSuccessfulRegistration(Response response) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Step("Проверка регистрации с повторными данными")
    private void validateDuplicateRegistration(Response response) {
        response.then()
                .statusCode(403)
                .body("message", equalTo("User already exists"));
    }

    @Step("Проверка ответа при отсутствии параметров")
    private void validateMissingParameterResponse(Response response) {
        response.then()
                .statusCode(403)
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Сохранение токена")
    private void saveToken(Response response) {
        token = response.jsonPath().getString("accessToken");
    }

    @Step("Удаление пользователя")
    private void deleteUser(String token) {
        given()
                .header("Authorization", token)
                .when()
                .delete("/api/auth/user")
                .then()
                .statusCode(202);
    }
}
