package datautils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Iterator;

public class NosqlDataWriter {
    private String username;
    private String password;
    private String controllerURL;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setControllerURL(String controllerURL) {
        this.controllerURL = controllerURL;
    }

    public boolean setSchema(JsonNode schema){
        try {
            for (Iterator<String> it = schema.fieldNames(); it.hasNext(); ) {
                String i = it.next();
                addCollection(i, schema.get(i));
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public void addCollection(String colName,JsonNode schema) throws JsonProcessingException {
        WebClient.create().put().uri(controllerURL+"/write/schema/"+colName)
                .headers(headers -> headers.setBasicAuth(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new JsonMapper().writeValueAsString(schema)))
                .retrieve().bodyToMono(String.class).block();
    }
    public void addDocument(String colName,JsonNode document) throws JsonProcessingException {
        String ret=WebClient.create().put().uri(controllerURL+"/write/"+colName)
                .headers(headers -> headers.setBasicAuth(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new JsonMapper().writeValueAsString(document)))
                .retrieve().bodyToMono(String.class).block();
    }
    public void deleteDocument(String colName,String doc_ID){
        WebClient.create().delete().uri(controllerURL+"/write/"+colName+"/"+doc_ID)
                .headers(headers -> headers.setBasicAuth(username, password))
                .retrieve().bodyToMono(String.class).block();
    }
}
