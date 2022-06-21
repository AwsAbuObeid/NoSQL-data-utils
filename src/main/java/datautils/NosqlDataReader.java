package datautils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

public class NosqlDataReader {

    private String username;
    private String password;
    private String controllerURL;
    private String currentReadServerURL;
    private String currentReadServerKEY;
    private String sessionIdCookie;

    ObjectMapper mapper=new ObjectMapper();

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setControllerURL(String controllerURL) {
        this.controllerURL = controllerURL;
    }

    public boolean connect(){
        WebClient webClient= WebClient.builder().build();
        try {
            List<ResponseCookie> cookies=new ArrayList<>();
            String ret = webClient.get()
                    .uri(controllerURL+"/read")
                    .headers(headers -> headers.setBasicAuth(username, password))
                    .retrieve().bodyToMono(String.class).block();
            JsonNode values = mapper.readTree(ret);

            currentReadServerKEY=values.get("key").asText();
            currentReadServerURL=values.get("read_server_link").asText();
            WebClient.create().get().uri(currentReadServerURL+"/authenticate")
                    .header("x-api-key",currentReadServerKEY)
                    .exchange().doOnSuccess(clientResponse ->cookies
                    .add(clientResponse.cookies().getFirst("JSESSIONID")))
                    .flatMap(clientResponse -> clientResponse.bodyToMono(String.class)).block();
            sessionIdCookie = cookies.get(0).getValue();
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public ArrayNode searchQuery(String colName,ObjectNode index){
        while(true)
            try {
                String ret =doPostQuery(currentReadServerURL+"/searchQuery/"+colName,index);
                return (ArrayNode) mapper.readTree(ret).get("content");
            } catch (WebClientException | JsonProcessingException e) {
                if(e instanceof WebClientResponseException.Forbidden) {
                    connect();
                    continue;
                }
                return mapper.createArrayNode();
            }
    }

    public ArrayNode indexQuery(String colName, ObjectNode index){
        while(true)
        try {
            String ret =doPostQuery(currentReadServerURL+"/indexQuery/"+colName,index);
            return (ArrayNode) mapper.readTree(ret).get("content");
        } catch (WebClientException | JsonProcessingException e) {
            if(e instanceof WebClientResponseException.Forbidden) {
                connect();
                continue;
            }
           return mapper.createArrayNode();
        }
    }
    public ArrayNode idQuery(String colName,String id){
        while(true)
            try {
                String ret =doGetQuery(currentReadServerURL+"/idQuery/"+colName+"/"+id);
                return (ArrayNode) mapper.readTree(ret).get("content");
            } catch (WebClientException | JsonProcessingException e) {
                if(e instanceof WebClientResponseException.Forbidden) {
                    connect();
                    continue;
                }return mapper.createArrayNode();
            }
    }
    public ArrayNode getCollection(String colName){
        while(true)
            try {
                String ret =doGetQuery(currentReadServerURL+"/query/"+colName);
                return (ArrayNode) mapper.readTree(ret).get("content");
            } catch (WebClientException | JsonProcessingException e) {
                if(e instanceof WebClientResponseException.Forbidden) {
                    connect();
                    continue;
                }return mapper.createArrayNode();
            }
    }

    private String doGetQuery(String url){
        return WebClient.create().get().uri(url)
                .cookie("JSESSIONID",sessionIdCookie)
                .retrieve().bodyToMono(String.class).block();
    }
    private String doPostQuery(String url,ObjectNode content){
        return WebClient.create().post().uri(url)
                .cookie("JSESSIONID",sessionIdCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(content))
                .retrieve().bodyToMono(String.class).block();
    }


}
