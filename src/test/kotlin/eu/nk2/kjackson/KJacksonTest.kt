package eu.nk2.kjackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.lang.Exception
import java.lang.IllegalStateException


class KJacksonTest {

    @Test
    fun testBasicJson() {
        val mapper = ObjectMapper()

        Assertions.assertEquals(
            mapper.writeValueAsString(jsonObject("a" to 1, "b" to 2)),
            "{\"a\":1,\"b\":2}"
        )
    }

    @Test
    fun testNestedJson() {
        val mapper = ObjectMapper()

        Assertions.assertEquals(
            mapper.writeValueAsString(jsonObject(
                "a" to jsonObject(
                    "a" to 1,
                    "b" to 2
                ),
                "b" to 3,
                "c" to jsonArray(4, 5, 6)
            )),
            "{\"a\":{\"a\":1,\"b\":2},\"b\":3,\"c\":[4,5,6]}"
        )
    }

    data class SerializableStub(
        val a: Int,
        val b: List<Int>,
        val c: SerializableStub?
    ) {
        companion object {
            val serializer = jsonSerializer<SerializableStub> { src, context -> jsonObject(
                "a" to src.a,
                "b" to context(src.b),
                "c" to context(src.c)
            ) }

            val deserializer = jsonDeserializer { src, context -> SerializableStub(
                src["a"].int,
                context(src["b"]),
                context(src["c"])
            ) }
        }
    }

    @Test
    fun testBasicSerializer() {
        val mapper = ObjectMapper()
            .registerModule(
                SimpleModule()
                    .addSerializer(SerializableStub::class.java, SerializableStub.serializer)
            )

        Assertions.assertEquals(
            mapper.writeValueAsString(SerializableStub(1, listOf(2, 3, 4), SerializableStub(2, listOf(3, 4, 5), null))),
            "{\"a\":1,\"b\":[2,3,4],\"c\":{\"a\":2,\"b\":[3,4,5],\"c\":null}}"
        )
    }

    @Test
    fun testBasicDeserializer() {
        val mapper = ObjectMapper()
            .registerModule(
                SimpleModule()
                    .addDeserializer(SerializableStub::class.java, SerializableStub.deserializer)
            )

        Assertions.assertEquals(
            mapper.readValue<SerializableStub>("{\"a\":1,\"b\":[2,3,4],\"c\":{\"a\":2,\"b\":[3,4,5],\"c\":null}}"),
            SerializableStub(1, listOf(2, 3, 4), SerializableStub(2, listOf(3, 4, 5), null))
        )
    }

    data class TypedSerializableStub<T>(
        val a: Int,
        val b: List<T>,
        val c: Any?
    ) {
        companion object {
            val serializer = jsonSerializer<TypedSerializableStub<*>> { src, context -> jsonObject(
                "a" to src.a,
                "b" to context(src.b),
                "c" to context(src.c)
            ) }

            val deserializer = jsonDeserializer { src, context -> TypedSerializableStub<Any>(
                src["a"].int,
                context(src["b"]),
                context(src["c"])
            ) }
        }
    }

    @Test
    fun testTypedSerialization() {
        val mapper = ObjectMapper()
            .activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                    .allowIfBaseType(Any::class.java)
                    .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
            )
            .registerModule(
                SimpleModule()
                    .addSerializer(TypedSerializableStub::class.java, TypedSerializableStub.serializer)
                    .addDeserializer(TypedSerializableStub::class.java, TypedSerializableStub.deserializer)
            )

        val json = mapper.writeValueAsString(TypedSerializableStub(1, listOf("a", "b", "c"), hashMapOf("a" to 5f)))
        val value = mapper.readValue<TypedSerializableStub<String>>(json)

        Assertions.assertEquals(
            value,
            TypedSerializableStub(1, listOf("a", "b", "c"), hashMapOf("a" to 5f))
        )
    }
}