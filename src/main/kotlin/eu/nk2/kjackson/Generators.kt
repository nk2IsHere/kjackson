package eu.nk2.kjackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import java.math.BigDecimal
import java.math.BigInteger

fun Number.toJson(): JsonNode = when(this) {
    is Byte -> ShortNode(this.toShort())
    is Short -> ShortNode(this)
    is Int -> IntNode(this)
    is Long -> LongNode(this)
    is Float -> FloatNode(this)
    is Double -> DoubleNode(this)
    is BigInteger -> BigIntegerNode(this)
    is BigDecimal -> DecimalNode(this)
    else -> error("$this is not supported explicitly in jackson")
}

internal fun Any?.toJsonNode(): JsonNode {
    if (this == null)
        return NullNode.instance

    return when (this) {
        is JsonNode -> this
        is String -> toJson()
        is Number -> toJson()
        is Char -> toJson()
        is Boolean -> toJson()
//        else -> error("$this is not supported explicitly in jackson")
        else -> POJONode(this)
    }
}

private fun _jsonArray(values: Iterator<Any?>): ArrayNode =
    ArrayNode(
        JsonNodeFactory.instance,
        values.asSequence()
            .map { it.toJsonNode() }
            .toList()
    )

fun jsonArray(vararg values: Any?) = _jsonArray(values.iterator())
fun jsonArray(values: Iterable<*>) = _jsonArray(values.iterator())
fun jsonArray(values: Sequence<*>) = _jsonArray(values.iterator())

fun Iterable<*>.toJsonArray() = jsonArray(this)
fun Sequence<*>.toJsonArray() = jsonArray(this)

private fun _jsonObject(values: Iterator<Pair<String, *>>): ObjectNode =
    ObjectNode(
        JsonNodeFactory.instance,
        values.asSequence()
            .map { (key, value) -> key to value.toJsonNode() }
            .toMap()
    )

fun jsonObject(vararg values: Pair<String, *>) = _jsonObject(values.iterator())
fun jsonObject(values: Iterable<Pair<String, *>>) = _jsonObject(values.iterator())
fun jsonObject(values: Sequence<Pair<String, *>>) = _jsonObject(values.iterator())

fun Iterable<Pair<String, *>>.toJsonObject() = jsonObject(this)
fun Sequence<Pair<String, *>>.toJsonObject() = jsonObject(this)

fun ObjectNode.shallowCopy(): ObjectNode =
    ObjectNode(
        JsonNodeFactory.instance,
        this.fieldNames().asSequence()
            .map { it to this.get(it) }
            .toMap()
    )

fun ArrayNode.shallowCopy(): ArrayNode =
    ArrayNode(
        JsonNodeFactory.instance,
        this.elements()
            .asSequence()
            .toList()
    )

fun JsonGenerator.value(value: Any) : JsonGenerator =
    when (value) {
        is Boolean -> value(value)
        is Double -> value(value)
        is Long -> value(value)
        is Number -> value(value)
        is String -> value(value)
        else -> error("$this cannot be written on JsonGenerator")
    }


fun Char.toJson(): JsonNode = TextNode("$this")

fun Boolean.toJson() : JsonNode = if(this) BooleanNode.TRUE else BooleanNode.FALSE

fun String.toJson() : JsonNode = TextNode(this)
