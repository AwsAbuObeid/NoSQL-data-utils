package datautils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;


public class SchemaCreator {
    public ObjectNode createSchema(String basePackage) throws JsonProcessingException, ClassNotFoundException {

        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        ObjectNode schema=mapper.createObjectNode();
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(JsonDocument.class));

        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)){
            Class<?> C = Class.forName(bd.getBeanClassName());
            JsonNode s= mapper.readTree(
                    mapper.writeValueAsString(generator.generateSchema(C)));
            schema.set(C.getSimpleName(),s);
        }
        return schema;

    }
}
