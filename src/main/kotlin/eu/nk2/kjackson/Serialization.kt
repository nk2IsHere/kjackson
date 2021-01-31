package eu.nk2.kjackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*

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
}

inline fun <T> jsonDeserializer(crossinline deserializer: (src: JsonNode, context: DeserializationContext) -> T) = object : JsonDeserializer<T>() {
    override fun deserialize(parser: JsonParser?, ctx: com.fasterxml.jackson.databind.DeserializationContext?): T =
        deserializer(parser!!.readValueAsTree(), DeserializationContext(parser))
}
