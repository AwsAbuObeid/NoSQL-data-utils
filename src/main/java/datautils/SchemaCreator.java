package datautils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;


public class SchemaCreator {
    public ObjectNode createSchema(String basePackage) throws JsonProcessingException, ClassNotFoundException, NoSuchFieldException {

        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        ObjectNode schema=mapper.createObjectNode();
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(JsonDocument.class));

        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)){
            Class<?> C = Class.forName(bd.getBeanClassName());
            ObjectNode s= (ObjectNode) mapper.readTree(
                    mapper.writeValueAsString(generator.generateSchema(C)));
            JsonDocument ann =C.getAnnotation(JsonDocument.class);
            String[] index =ann.index();
            ArrayNode indexAttr= mapper.createArrayNode();
            if (!index[0].equals(""))
                for (String value : index) {
                    C.getDeclaredField(value);
                    indexAttr.add(value);
                }
            s.set("index",indexAttr);
            schema.set(C.getSimpleName(),s);
        }
        return schema;
    }
}
