package eu.nk2.kjackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.core.type.WritableTypeId
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer


class SerializationContext(
    private val generator: JsonGenerator
) {
    operator fun invoke(value: Any?): JsonNode {
        return (generator.codec as ObjectMapper).valueToTree(value)
    }
}

class DeserializationContext(
    val parser: JsonParser
) {
    inline operator fun <reified T> invoke(node: JsonNode): T {
        return (parser.codec as ObjectMapper).treeToValue<T>(node, T::class.java)
    }

    operator fun <T> invoke(node: JsonNode, clazz: Class<T>): T {
        return (parser.codec as ObjectMapper).treeToValue<T>(node, clazz)
    }
}

inline fun <T> jsonSerializer(crossinline serializer: (src: T, context: SerializationContext) -> JsonNode) = object: JsonSerializer<T>() {
    override fun serialize(value: T, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeTree(serializer(value, SerializationContext(generator)))
    }

    override fun serializeWithType(value: T, generator: JsonGenerator, provider: SerializerProvider, typeSerializer: TypeSerializer) {
        val typeId: WritableTypeId = typeSerializer.typeId(value, JsonToken.START_OBJECT)
        typeSerializer.writeTypePrefix(generator, typeId)
        serialize(value, generator, provider)
        typeSerializer.writeTypeSuffix(generator, typeId)
    }
}

inline fun <T> jsonDeserializer(crossinline deserializer: (src: JsonNode, context: DeserializationContext) -> T) = object : JsonDeserializer<T>() {
    override fun deserialize(parser: JsonParser, ctx: com.fasterxml.jackson.databind.DeserializationContext): T =
        deserializer(parser.readValueAsTree(), DeserializationContext(parser))
}
