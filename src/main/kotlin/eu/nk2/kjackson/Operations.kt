import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import java.math.BigDecimal
import java.math.BigInteger

private fun <T : Any> JsonNode?._nullOr(getNotNull: JsonNode.() -> T) : T?
    = if (this == null || isNull) null else getNotNull()

val JsonNode.string: String get() = (this as? TextNode)?.textValue() ?: error("$this is not String")
val JsonNode?.nullString: String? get() = _nullOr { string }

val JsonNode.bool: Boolean get() = (this as? BooleanNode)?.booleanValue() ?: error("$this is not Boolean")
val JsonNode?.nullBool: Boolean? get() = _nullOr { bool }

val JsonNode.byte: Byte get() = (this as? ShortNode)?.shortValue()?.toByte() ?: error("$this is not Byte")
val JsonNode?.nullByte: Byte? get() = _nullOr { byte }

val JsonNode.char: Char get() = (this as? TextNode)?.textValue()?.first() ?: error("$this is not Char")
val JsonNode?.nullChar: Char? get() = _nullOr { char }

val JsonNode.short: Short get() = (this as? ShortNode)?.shortValue() ?: error("$this is not Short")
val JsonNode?.nullShort: Short? get() = _nullOr { short }

val JsonNode.int: Int get() = (this as? IntNode)?.intValue() ?: error("$this is not Int")
val JsonNode?.nullInt: Int? get() = _nullOr { int }

val JsonNode.long: Long get() = (this as? LongNode)?.longValue() ?: error("$this is not Long")
val JsonNode?.nullLong: Long? get() = _nullOr { long }

val JsonNode.float: Float get() = (this as? FloatNode)?.floatValue()
    ?: (this as? DoubleNode)?.floatValue()
    ?: error("$this is not Float")
val JsonNode?.nullFloat: Float? get() = _nullOr { float }

val JsonNode.double: Double get() = (this as? DoubleNode)?.doubleValue() ?: error("$this is not Double")
val JsonNode?.nullDouble: Double? get() = _nullOr { double }

val JsonNode.bigInteger: BigInteger get() = (this as? BigIntegerNode)?.bigIntegerValue() ?: error("$this is not BigInteger")
val JsonNode?.nullBigInteger: BigInteger? get() = _nullOr { bigInteger }

val JsonNode.bigDecimal: BigDecimal get() = (this as? DecimalNode)?.decimalValue() ?: error("$this is not BigDecimal")
val JsonNode?.nullBigDecimal: BigDecimal? get() = _nullOr { bigDecimal }

val JsonNode.array: ArrayNode get() = (this as? ArrayNode) ?: error("$this is not Array")
val JsonNode?.nullArray: ArrayNode? get() = _nullOr { array }

val JsonNode.obj: ObjectNode get() = (this as? ObjectNode) ?: error("$this is not Object")
val JsonNode?.nullObj: ObjectNode? get() = _nullOr { obj }

val jsonNull: NullNode = NullNode.instance

fun JsonNode.getOrNull(key: String): JsonNode? = obj.get(key)
fun JsonNode.getOrNull(index: Int): JsonNode? = array.get(index)

fun ObjectNode.toMap() = this.fieldNames().asSequence()
    .map { it to this.get(it) }
    .toMap()

operator fun ObjectNode.contains(key: String): Boolean = has(key)
fun ObjectNode.isNotEmpty(): Boolean = !this.isEmpty
fun ObjectNode.keys(): Collection<String> = fieldNames().asSequence()
    .toList()
fun ObjectNode.forEach(operation: (String, JsonNode) -> Unit): Unit = toMap().forEach { operation(it.key, it.value) }

fun ObjectNode.addProperty(property: String, value: JsonNode): JsonNode = set<JsonNode>(property, value)
fun ObjectNode.addPropertyIfNotNull(property: String, value: String?) = value?.let { addProperty(property, value.toJson()) }
fun ObjectNode.addPropertyIfNotNull(property: String, value: Char?) = value?.let { addProperty(property, value.toJson()) }
fun ObjectNode.addPropertyIfNotNull(property: String, value: Boolean?) = value?.let { addProperty(property, value.toJson()) }
fun ObjectNode.addPropertyIfNotNull(property: String, value: Number?) = value?.let { addProperty(property, value.toJson()) }
fun ObjectNode.addPropertyIfNotNull(property: String, value: JsonNode?) = value?.let { addProperty(property, value) }

operator fun ArrayNode.contains(value: Any): Boolean = this.contains(value.toJsonNode())

operator fun JsonNode.set(key: String, value: Any?): JsonNode = obj.set<JsonNode>(key, value.toJsonNode())
operator fun JsonNode.set(key: Int, value: Any?): JsonNode = array.set(key, value.toJsonNode())

fun ObjectNode.put(pair: Pair<String, Any?>) = set<JsonNode>(pair.first, pair.second.toJsonNode())
fun ObjectNode.put(entry: Map.Entry<String, Any?>) = set<JsonNode>(entry.key, entry.value.toJsonNode())

fun ObjectNode.putAll(vararg pairs: Pair<String, Any?>) = pairs.forEach { put(it) }
fun ObjectNode.putAll(vararg entries: Map.Entry<String, Any?>) = entries.forEach { put(it) }
fun ObjectNode.putAll(map: Map<String, Any?>) = map.entries.forEach { put(it) }
fun ObjectNode.putAll(pairs: Sequence<Pair<String, Any?>>) = pairs.forEach { put(it) }
fun ObjectNode.putAll(pairs: Iterable<Pair<String, Any?>>) = pairs.forEach { put(it) }
fun ObjectNode.putAllEntries(entries: Sequence<Map.Entry<String, Any?>>) = entries.forEach { put(it) }
fun ObjectNode.putAllEntries(entries: Iterable<Map.Entry<String, Any?>>) = entries.forEach { put(it) }

operator fun ObjectNode.plus(pair: Pair<String, Any?>) = shallowCopy().apply { put(pair) }
operator fun ObjectNode.plus(entry: Map.Entry<String, Any?>) = shallowCopy().apply { put(entry) }
operator fun ObjectNode.plus(pairs: Array<Pair<String, Any?>>) = shallowCopy().apply { putAll(*pairs) }
operator fun ObjectNode.plus(entries: Array<Map.Entry<String, Any?>>) = shallowCopy().apply { putAll(*entries) }
operator fun ObjectNode.plus(map: Map<String, Any?>) = shallowCopy().apply { putAll(map) }
operator fun ObjectNode.plus(obj: ObjectNode) = shallowCopy().apply { setAll<JsonNode>(obj) }

operator fun ObjectNode.plusAssign(pair: Pair<String, Any?>) { put(pair) }
operator fun ObjectNode.plusAssign(entry: Map.Entry<String, Any?>) { put(entry) }
operator fun ObjectNode.plusAssign(pairs: Array<Pair<String, Any?>>) = putAll(*pairs)
operator fun ObjectNode.plusAssign(entries: Array<Map.Entry<String, Any?>>) = putAll(*entries)
operator fun ObjectNode.plusAssign(map: Map<String, Any?>) = putAll(map)
operator fun ObjectNode.plusAssign(obj: ObjectNode) { setAll<JsonNode>(obj) }

fun ObjectNode.removeAll(vararg keys: String) = keys.forEach { remove(it) }
fun ObjectNode.removeAll(keys: Iterable<String>) = keys.forEach { remove(it) }
fun ObjectNode.removeAll(keys: Sequence<String>) = keys.forEach { remove(it) }
fun ObjectNode.removeAllJsonKeys(vararg keys: JsonNode) = keys.forEach { remove(it.string) }
fun ObjectNode.removeAllJsonKeys(keys: Iterable<JsonNode>) = keys.forEach { remove(it.string) }
fun ObjectNode.removeAllJsonKeys(keys: Sequence<JsonNode>) = keys.forEach { remove(it.string) }

operator fun ObjectNode.minus(key: String) = shallowCopy().apply { remove(key) }
operator fun ObjectNode.minus(keys: Array<String>) = shallowCopy().apply { removeAll(*keys) }
operator fun ObjectNode.minus(keys: Iterable<String>) = shallowCopy().apply { removeAll(keys) }
operator fun ObjectNode.minus(keys: Sequence<String>) = shallowCopy().apply { removeAll(keys) }

operator fun ObjectNode.minusAssign(key: String) { remove(key) }
operator fun ObjectNode.minusAssign(keys: Array<String>) = removeAll(*keys)
operator fun ObjectNode.minusAssign(keys: Iterable<String>) = removeAll(keys)
operator fun ObjectNode.minusAssign(keys: Sequence<String>) = removeAll(keys)

fun ArrayNode.add(value: Any?) = add(value.toJsonNode())

fun ArrayNode.addAll(vararg values: Any?) = values.forEach { add(it) }
fun ArrayNode.addAll(values: Iterable<Any?>) = values.forEach { add(it) }
fun ArrayNode.addAll(values: Sequence<Any?>) = values.forEach { add(it) }

operator fun ArrayNode.plus(value: Any?) = shallowCopy().apply { add(value) }
operator fun ArrayNode.plus(values: Array<Any?>) = shallowCopy().apply { addAll(values) }
operator fun ArrayNode.plus(values: Iterable<Any?>) = shallowCopy().apply { addAll(values) }
operator fun ArrayNode.plus(values: Sequence<Any?>) = shallowCopy().apply { addAll(values) }

operator fun ArrayNode.plusAssign(value: Any?) { add(value) }
operator fun ArrayNode.plusAssign(values: Array<Any?>) = addAll(values)
operator fun ArrayNode.plusAssign(values: Iterable<Any?>) = addAll(values)
operator fun ArrayNode.plusAssign(values: Sequence<Any?>) = addAll(values)

fun ArrayNode.remove(value: JsonNode) { remove(indexOf(value)) }

fun ArrayNode.removeAll(vararg values: JsonNode) = values.forEach { remove(it) }
fun ArrayNode.removeAll(values: Iterable<JsonNode>) = values.forEach { remove(it) }
fun ArrayNode.removeAll(values: Sequence<JsonNode>) = values.forEach { remove(it) }

operator fun ArrayNode.minus(value: JsonNode) = shallowCopy().apply { remove(value) }
operator fun ArrayNode.minus(values: Array<JsonNode>) = shallowCopy().apply { removeAll(*values) }
operator fun ArrayNode.minus(values: Iterable<JsonNode>) = shallowCopy().apply { removeAll(values) }
operator fun ArrayNode.minus(values: Sequence<JsonNode>) = shallowCopy().apply { removeAll(values) }

operator fun ArrayNode.minusAssign(value: JsonNode) { remove(value) }
operator fun ArrayNode.minusAssign(values: Array<JsonNode>) = removeAll(*values)
operator fun ArrayNode.minusAssign(values: Iterable<JsonNode>) = removeAll(values)
operator fun ArrayNode.minusAssign(values: Sequence<JsonNode>) = removeAll(values)

fun ArrayNode.removeAllIndexes(vararg indexes: Int) = indexes.forEach { remove(it) }
fun ArrayNode.removeAllIndexes(indexes: Iterable<Int>) = indexes.forEach { remove(it) }
fun ArrayNode.removeAllIndexes(indexes: Sequence<Int>) = indexes.forEach { remove(it) }
