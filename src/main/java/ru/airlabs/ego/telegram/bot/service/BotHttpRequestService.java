package ru.airlabs.ego.telegram.bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.airlabs.ego.telegram.bot.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Вспомогательный класс для выполнения http-запросов для Telegram-бота
 *
 * @author Aleksey Gorbachev
 */
@Component
public class BotHttpRequestService {

    @Value("${rest.domain}")
    private String domain;

    /**
     * Авторизация пользователя в REST API
     *
     * @param telegramId идентификатор Telegram
     * @param password   пароль
     * @return при успешной авторизации роль пользователя, иначе null
     */
    public UserRole loginUser(long telegramId, String password) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(domain + "/j_spring_security_check?j_username=" + telegramId + "&j_password=" + password);
            CloseableHttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            org.apache.http.HttpEntity entity = response.getEntity();
            if (entity != null && statusCode == 200) {
                String responseBody = IOUtils.toString(entity.getContent(), "UTF-8");
                if (responseBody.contains("OWNER")) {
                    return UserRole.OWNER;
                } else {
                    return UserRole.MANAGER;
                }
            }
            return null;
        }
    }

    /**
     * Получить список вакансий для приглашений
     *
     * @param telegramId идентификатор Telegram
     * @param password   пароль
     * @return список вакансий
     */
    public List<Vacancy> getVacancyList(long telegramId, String password) throws IOException, URISyntaxException {
        final String baseUrl = domain + "/tg/vacancy/list";
        URI uri = new URI(baseUrl);
        HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders(String.valueOf(telegramId), password));
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        if (response.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrayNode = mapper.readTree(response.getBody());
            if (arrayNode.isArray()) {
                List<Vacancy> vacancies = new ArrayList<>();
                int number = 0;
                for (JsonNode vacancyNode : arrayNode) {
                    Vacancy vacancy = new Vacancy();
                    vacancy.setId(vacancyNode.path("id").asLong());
                    vacancy.setName(vacancyNode.path("name").asText());
                    vacancy.setNumber(++number);
                    vacancies.add(vacancy);
                }
                return vacancies;
            }
        }
        return null;
    }

    /**
     * Получить список пользователей для настроек
     *
     * @param telegramId идентификатор Telegram
     * @param password   пароль
     * @return список пользователей
     */
    public List<User> getUsersList(long telegramId, String password) throws IOException, URISyntaxException {
        final String baseUrl = domain + "/tg/users/list";
        URI uri = new URI(baseUrl);
        HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders(String.valueOf(telegramId), password));
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        if (response.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrayNode = mapper.readTree(response.getBody());
            if (arrayNode.isArray()) {
                List<User> users = new ArrayList<>();
                for (JsonNode userNode : arrayNode) {
                    User user = new User();
                    user.setSocialUserId(userNode.path("socialUserId").asText());
                    user.setOwnerId(userNode.path("ownerId").asLong());
                    user.setName(userNode.path("name").asText());
                    user.setPassword(userNode.path("password").asText());
                    users.add(user);
                }
                return users;
            }
        }
        return null;
    }

    /**
     * Добавление нового пользователя
     *
     * @param telegramId идентификатор Telegram
     * @param password   пароль для авторизации
     * @param userName   имя пользователя
     * @return новый пользователь
     */
    public User addNewUser(long telegramId, String password, String userName) throws IOException, URISyntaxException {
        final String baseUrl = domain + "/tg/users/add?name=" + URLEncoder.encode(userName, "UTF-8");
        URI uri = new URI(baseUrl);
        HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders(String.valueOf(telegramId), password));
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        if (response.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode userNode = mapper.readTree(response.getBody());
            String socialUserId = userNode.path("socialUserId").asText();
            if (isNotBlank(socialUserId)) {
                String userPassword = userNode.path("password").asText();
                User user = new User();
                user.setSocialUserId(socialUserId);
                user.setPassword(userPassword);
                user.setName(userName);
                return user;
            }
        }
        return null;
    }

    /**
     * Удаление пользователя
     *
     * @param telegramId   идентификатор Telegram
     * @param password     пароль
     * @param socialUserId идентификатор Telegram удаляемого пользователя
     * @return результат удаления (true\false)
     */
    public boolean deleteUser(long telegramId, String password, String socialUserId) throws IOException, URISyntaxException {
        final String baseUrl = domain + "/tg/users/delete?socialUserId=" + URLEncoder.encode(socialUserId, "UTF-8");
        URI uri = new URI(baseUrl);
        HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders(String.valueOf(telegramId), password));
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        if (response.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());
            if (node.has("success")) {
                return node.path("success").asBoolean();
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Отправка приглашения пользователю
     *
     * @param telegramId идентификатор Telegram
     * @param password   пароль
     * @param vacancyId  идентификатор вакансии
     * @param invite     приглашение
     * @return результат приглашения (true\false)
     */
    public boolean inviteUser(long telegramId,
                              String password,
                              Long vacancyId,
                              Invite invite) throws IOException, URISyntaxException {
        final String baseUrl = domain + "/tg/vacancy/createInvitationText/" + vacancyId;
        final String params;
        if (invite.isEmailInvite() && invite.isAudioInvite()) {
            params = "?email=true&audio=true";
        } else if (invite.isEmailInvite()) {
            params = "?email=true";
        } else if (invite.isAudioInvite()) {
            params = "?audio=true";
        } else {
            params = "";
        }
        URI uri = new URI(baseUrl + params);
        HttpEntity<?> entity = new HttpEntity<Object>(invite, createAuthHeaders(String.valueOf(telegramId), password));
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        if (response.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());
            return node.path("success").asBoolean();
        }
        return false;
    }

    /**
     * Регистрация нового пользователя
     *
     * @param registration данные регистрации
     * @return результат регистрациии
     */
    public RegistrationResult registerUser(Registration registration) throws IOException, URISyntaxException {
        RegistrationResult result = new RegistrationResult();
        final String baseUrl = domain + "/tg/registration";
        URI uri = new URI(baseUrl);
        HttpEntity<?> entity = new HttpEntity<Object>(registration);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        if (response.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());
            if (node.path("success").asBoolean()) {
                result.success = true;
            } else {    // в случае ошибки регистрации получаем сообщение об ошибке
                result.success = false;
                result.message = node.path("message").asText();
            }
        }
        return result;
    }

    /**
     * Получить настройки для пользователя (личные данные + данные компании)
     *
     * @param telegramId идентификатор Telegram
     * @param password   пароль
     * @return настройки для пользователя (личные данные + данные компании)
     */
    public AccountSettings getAccountSettings(long telegramId, String password) throws IOException, URISyntaxException {
        final String baseUrl = domain + "/tg/settings";
        URI uri = new URI(baseUrl);
        HttpEntity<String> entity = new HttpEntity<>(createAuthHeaders(String.valueOf(telegramId), password));
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        if (response.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());
            String managerName = node.path("managerName").asText();
            if (isNotBlank(managerName)) {
                AccountSettings settings = new AccountSettings();
                settings.setManagerName(managerName);
                settings.setManagerEmail(getNodeValueAsString(node, "managerEmail"));
                settings.setPhone(getNodeValueAsString(node, "phone"));
                settings.setSite(getNodeValueAsString(node, "site"));
                settings.setAddress(getNodeValueAsString(node, "address"));
                settings.setCompanyName(getNodeValueAsString(node, "companyName"));
                settings.setCompanyDescription(getNodeValueAsString(node, "companyDescription"));
                String organizationId = node.path("organizationId").asText();
                settings.setOrganizationId(isNotBlank(organizationId) && !organizationId.equals("null") ?
                        node.path("organizationId").asLong() :
                        null);
                return settings;
            }
        }
        return null;
    }

    /**
     * Сохранение настроек для пользователей
     *
     * @param settings   настройки пользователя и компании
     * @param telegramId идентификатор Telegram
     * @param password   пароль
     * @return результат сохранения
     */
    public boolean saveAccountSettings(AccountSettings settings,
                                       long telegramId,
                                       String password) throws IOException, URISyntaxException {
        final String baseUrl = domain + "/tg/settings";
        URI uri = new URI(baseUrl);
        HttpEntity<AccountSettings> entity = new HttpEntity<>(settings, createAuthHeaders(String.valueOf(telegramId), password));
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        if (response.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());
            return node.path("success").asBoolean();
        }
        return false;
    }

    /**
     * Создать заголовок авторизации
     *
     * @param telegramId идентификатор Telegram
     * @param password   пароль
     * @return заголовок авторизации
     */
    private HttpHeaders createAuthHeaders(String telegramId, String password) {
        String auth = telegramId + ":" + password;
        String encodedAuth = new String(Base64.encodeBase64(auth.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + encodedAuth);
        return headers;
    }

    /**
     * Получить значение поля из xml-ноды
     *
     * @param node         xml-нода
     * @param propertyName название свойства
     * @return название поля
     */
    private String getNodeValueAsString(JsonNode node, String propertyName) {
        if (node != null && node.has(propertyName)) {
            String value = node.path(propertyName).asText();
            return isNotBlank(value) && !value.equals("null") ? value : null;
        }
        return null;
    }

    private RestTemplate getRestTemplate(long telegramId, String password) {
        return new RestTemplate(getClientHttpRequestFactory(telegramId, password));
    }

    private HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory(long telegramId, String password) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient(telegramId, password));
        return clientHttpRequestFactory;
    }

    private HttpClient httpClient(long telegramId, String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(String.valueOf(telegramId), password));

        HttpClient client = HttpClientBuilder
                .create()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
        return client;
    }

}
