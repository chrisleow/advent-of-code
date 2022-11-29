/**
 * Uses an AVLTree as a map
 */
class AVLMap<K, V> private constructor(private val tree: AVLTree<Pair<K, List<Pair<K, V>>>>) : Map<K, V> {

    data class AVLMapEntry<K, V>(override val key: K, override val value: V) : Map.Entry<K, V>

    // basic accessors
    override val size = tree.size // i know this is broken, will fix with monoids!
    override operator fun get(key: K): V? = tree.find(Pair(key, emptyList()))
        ?.second
        ?.filter { (k, _) -> k == key }
        ?.firstNotNullOfOrNull { (_, v) -> v }
    override fun containsKey(key: K): Boolean = tree.find(Pair(key, emptyList()))
        ?.second
        ?.any { (k, _) -> k == key }
        ?: false
    override fun containsValue(value: V): Boolean = tree.scan()
        .any { (_, values) -> values.any { (_, v) -> v == value } }
    override fun isEmpty(): Boolean = (tree.size == 0)

    private fun <K, V> AVLTree<Pair<K, List<Pair<K, V>>>>.addToList(key: K, value: V): AVLTree<Pair<K, List<Pair<K, V>>>> {
        val (_, existingValues) = this.find(Pair(key, emptyList())) ?: Pair(key, emptyList())
        val newValues = existingValues + Pair(key, value)
        return this.add(Pair(key, newValues))
    }

    private fun <K, V> AVLTree<Pair<K, List<Pair<K, V>>>>.removeFromList(key: K): AVLTree<Pair<K, List<Pair<K, V>>>> {
        val (_, existingValues) = this.find(Pair(key, emptyList())) ?: Pair(key, emptyList())
        val newValues = existingValues.filter { (k, _) -> k == key }
        return if (newValues.isNotEmpty()) {
            this.add(Pair(key, newValues))
        } else {
            this.remove(Pair(key, emptyList()))
        }
    }

    // modifiers
    fun add(key: K, value: V) = AVLMap(tree.addToList(key, value))
    fun add(entry: Pair<K, V>) = AVLMap(tree.addToList(entry.first, entry.second))
    fun add(entry: Map.Entry<K, V>) = AVLMap(tree.addToList(entry.key, entry.value))
    fun add(map: Map<K, V>) = AVLMap(map.entries.fold(tree) { t, (k, v) -> t.addToList(k, v) })
    fun addAll(entries: Iterable<Pair<K, V>>) =  AVLMap(entries.fold(tree) { t, (k, v) -> t.addToList(k, v) })
    fun remove(key: K) = AVLMap(tree.removeFromList(key))
    fun remove(keys: Iterable<K>) = AVLMap(keys.fold(tree) { t, k -> t.removeFromList(k) })

    override val entries: Set<Map.Entry<K, V>> = object : Set<Map.Entry<K, V>> {
        override val size = tree.size
        override fun contains(element: Map.Entry<K, V>): Boolean = tree
            .find(Pair(element.key, emptyList()))
            ?.second
            ?.any { (_, v) -> v == element.value }
            ?: false
        override fun containsAll(elements: Collection<Map.Entry<K, V>>) = elements.all { this.contains(it) }
        override fun isEmpty() = (tree.size == 0)
        override fun iterator() = tree.scan()
            .flatMap { (_, ps) -> ps.map { (k, v) -> AVLMapEntry(k, v) } }
            .iterator()
    }

    override val keys: Set<K> = object : Set<K> {
        override val size = tree.size
        override fun contains(element: K) = tree.find(Pair(element, emptyList()))
            ?.second
            ?.any { (k, _) -> k == element }
            ?: false
        override fun containsAll(elements: Collection<K>) = elements.all { this.contains(it) }
        override fun isEmpty() = (tree.size == 0)
        override fun iterator() = tree.scan()
            .flatMap { (_, ps) -> ps.map { (k, _) -> k } }
            .iterator()
    }

    override val values: Collection<V> = object : AbstractCollection<V>() {
        override val size = tree.size
        override fun iterator() = tree.scan()
            .flatMap { (_, ps) -> ps.map { (_, v) -> v } }
            .iterator()
    }

    companion object {
        operator fun <K, V: Any> invoke(): AVLMap<K, V> {
            return AVLMap(AVLTree(compareBy { it.first.hashCode() }))
        }
    }
}
