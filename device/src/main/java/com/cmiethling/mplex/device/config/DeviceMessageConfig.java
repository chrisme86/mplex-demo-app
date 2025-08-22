package com.cmiethling.mplex.device.config;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cmiethling.mplex.device.message.MessageParameters;
import com.cmiethling.mplex.device.message.MessageParametersImpl;
import com.cmiethling.mplex.device.message.ResultError;
import com.cmiethling.mplex.device.message.Subsystem;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class DeviceMessageConfig {

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        // enable pretty print
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // add CustomPrettyPrinter for correct formatting
        mapper.setDefaultPrettyPrinter(new CustomPrettyPrinter());

        final SimpleModule module = new SimpleModule();
        module.addSerializer(Subsystem.class, new SubsystemSerializer());
        module.addDeserializer(Subsystem.class, new SubsystemDeserializer());
        module.addSerializer(MessageParameters.class, new MessageParametersSerializer());
        module.addDeserializer(MessageParameters.class, new MessageParametersDeserializer());
        // for ResultMessage only
        module.addSerializer(ResultError.class, new ResultErrorSerializer());
        module.addDeserializer(ResultError.class, new ResultErrorDeserializer());

        mapper.registerModule(module);

        return mapper;
    }

    //  CustomPrettyPrinter for default (IntelliJ) formatting of json files
    private static class CustomPrettyPrinter extends DefaultPrettyPrinter {
        public CustomPrettyPrinter() {
            this._objectFieldValueSeparatorWithSpaces = ": ";
            // without whitespace for empty "parameters": {}
            this._objectEmptySeparator = "";
        }

        @Override
        public void writeEndObject(final JsonGenerator jg, final int nrOfValues) throws IOException {
            super.writeEndObject(jg, nrOfValues);
            // Only add newline at the root level
            if (jg.getOutputContext().getParent().getCurrentName() == null)
                jg.writeRaw(System.lineSeparator());
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new CustomPrettyPrinter();
        }
    }

    // ################# Subsystem ######################
    public static class SubsystemSerializer extends JsonSerializer<Subsystem> {
        @Override
        public void serialize(final Subsystem value, final JsonGenerator jgen, final SerializerProvider serializers)
                throws IOException {
            jgen.writeString(value.id());
        }
    }

    public static class SubsystemDeserializer extends JsonDeserializer<Subsystem> {
        @Override
        public Subsystem deserialize(final JsonParser jsonParser, final DeserializationContext ctxt)
                throws IOException {
            return Subsystem.fromJson(jsonParser.getText());
        }
    }

    // ################# MessageParameters ######################
    public static class MessageParametersSerializer extends JsonObjectSerializer<MessageParameters> {

        @Override
        protected void serializeObject(final MessageParameters params, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException {
            final var keys = params.keySet();
            for (final var key : keys) {
                if (params.get(key).isPresent()) {
                    final var value = params.get(key).get();
                    switch (value) {
                    case final String val -> jgen.writeStringField(key, val);
                    case final Integer val -> jgen.writeNumberField(key, val);
                    case final Double val -> jgen.writeNumberField(key, val);
                    case final Boolean val -> jgen.writeBooleanField(key, val);
                    case final MessageParameters val -> {
                        jgen.writeObjectFieldStart(key);
                        serializeObject(val, jgen, provider); // recursive
                        jgen.writeEndObject();
                    }
                    default -> throw new IllegalArgumentException(value.getClass().getName());
                    }
                }
            }
        }
    }

    public static class MessageParametersDeserializer extends JsonObjectDeserializer<MessageParameters> {

        private static MessageParameters fillParamsWithFields(final MessageParameters params,
                final Iterator<Map.Entry<String, JsonNode>> fields) {
            while (fields.hasNext()) {
                final var field = fields.next();
                final var value = field.getValue();
                final var type = value.getNodeType();
                switch (type) {
                case STRING -> params.putString(field.getKey(), value.textValue());
                case BOOLEAN -> params.putBoolean(field.getKey(), value.asBoolean());
                case NUMBER -> {
                    if (value.isInt())
                        params.putInt(field.getKey(), value.intValue());
                    else if (value.isDouble())
                        params.putDouble(field.getKey(), value.doubleValue());
                    else
                        throw new IllegalArgumentException("invalid number type: not int nor double: " + value);
                }
                case OBJECT -> {
                    final var params2 = params.addNested(field.getKey());
                    fillParamsWithFields(params2, value.fields()); // recursive
                }
                default -> throw new IllegalStateException("invalid JsonType: " + type);
                }
            }
            return params;
        }

        @Override
        protected MessageParameters deserializeObject(final JsonParser jsonParser, final DeserializationContext context,
                final ObjectCodec codec, final JsonNode tree) {
            return fillParamsWithFields(new MessageParametersImpl(), tree.fields());
        }
    }

    // ################# ResultError from ResultMessage ######################
    public static class ResultErrorSerializer extends JsonSerializer<ResultError> {
        @Override
        public void serialize(final ResultError value, final JsonGenerator jgen, final SerializerProvider serializers)
                throws IOException {
            jgen.writeString(value.code());
        }
    }

    public static class ResultErrorDeserializer extends JsonDeserializer<ResultError> {
        @Override
        public ResultError deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            return ResultError.ofCode(p.getText());
        }
    }
}
