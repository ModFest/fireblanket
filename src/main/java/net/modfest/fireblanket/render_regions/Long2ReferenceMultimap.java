package net.modfest.fireblanket.render_regions;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import it.unimi.dsi.fastutil.objects.ReferenceLists;

public class Long2ReferenceMultimap<V> implements ListMultimap<Long, V> {

	private final Long2ReferenceMap<ReferenceList<V>> underlying = new Long2ReferenceOpenHashMap<>();
	
	@Override
	public int size() {
		int count = 0;
		for (ReferenceList<V> li : underlying.values()) {
			count += li.size();
		}
		return count;
	}

	@Override
	public boolean isEmpty() {
		return underlying.isEmpty();
	}

	@Override @Deprecated
	public boolean containsKey(Object key) {
		return underlying.containsKey(key);
	}
	
	public boolean containsKey(long key) {
		return underlying.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		for (ReferenceList<V> li : underlying.values()) {
			if (li.contains(value)) return true;
		}
		return false;
	}

	@Override @Deprecated
	public boolean containsEntry(Object key, Object value) {
		if (key instanceof Long l) return get(l).contains(value);
		return false;
	}
	
	public boolean containsEntry(long key, Object value) {
		return get(key).contains(value);
	}

	@Override @Deprecated
	public ReferenceList<V> get(Long key) {
		return get(key.longValue());
	}
	
	public ReferenceList<V> get(long key) {
		return underlying.getOrDefault(key, ReferenceLists.emptyList());
	}

	@Override @Deprecated
	public boolean put(Long key, V value) {
		return put(key.longValue(), value);
	}
	
	public boolean put(long key, V value) {
		ReferenceList<V> li = underlying.get(key);
		if (li == null) {
			li = new ReferenceArrayList<>();
			underlying.put(key, li);
		}
		return li.add(value);
	}

	@Override @Deprecated
	public boolean remove(Object key, Object value) {
		if (key instanceof Long l) return remove(l.longValue(), value);
		return false;
	}

	public boolean remove(long key, Object value) {
		ReferenceList<V> li = get(key);
		if (li.isEmpty()) return false;
		return li.remove(value);
	}

	@Override @Deprecated
	public boolean putAll(Long key, Iterable<? extends V> values) {
		return putAll(key.longValue(), values);
	}
	
	public boolean putAll(long key, Iterable<? extends V> values) {
		boolean any = false;
		for (V v : values) {
			any |= put(key, v);
		}
		return any;
	}

	@Override @Deprecated
	public boolean putAll(Multimap<? extends Long, ? extends V> multimap) {
		boolean any = false;
		for (var en : multimap.asMap().entrySet()) {
			any |= putAll(en.getKey(), en.getValue());
		}
		return any;
	}

	@Override
	public ReferenceList<V> replaceValues(Long key, Iterable<? extends V> values) {
		ReferenceList<V> newLi = new ReferenceArrayList<>();
		for (V v : values) newLi.add(v);
		ReferenceList<V> li = underlying.put(key, newLi);
		return li == null ? ReferenceLists.emptyList() : li;
	}

	@Override
	public ReferenceList<V> removeAll(Object key) {
		ReferenceList<V> li = underlying.remove(key);
		return li == null ? ReferenceLists.emptyList() : li;
	}

	@Override
	public void clear() {
		underlying.clear();
	}

	@Override
	public LongSet keySet() {
		return underlying.keySet();
	}

	@Override @Deprecated
	public LongMultiset keys() {
		LongMultiset ms = new LongMultiset();
		for (var en : underlying.long2ReferenceEntrySet()) {
			ms.add(en.getLongKey(), en.getValue().size());
		}
		return ms;
	}

	@Override
	public Collection<V> values() {
		return new AbstractCollection<V>() {
			@Override
			public Iterator<V> iterator() {
				return Iterables.concat(underlying.values()).iterator();
			}
			
			@Override
			public int size() {
				return Long2ReferenceMultimap.this.size();
			}

			@Override
			public boolean contains(Object o) {
				return Long2ReferenceMultimap.this.containsValue(o);
			}

			@Override
			public boolean remove(Object o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				Long2ReferenceMultimap.this.clear();
			}
		};
	}

	@Override @Deprecated
	public Collection<Entry<Long, V>> entries() {
		return (Collection)long2ReferenceEntries();
	}

	@Override @Deprecated
	public Map<Long, Collection<V>> asMap() {
		return (Map)underlying;
	}

	public Collection<Long2ReferenceMap.Entry<V>> long2ReferenceEntries() {
		return null;
	}

	public Long2ReferenceMap<ReferenceList<V>> asLong2ReferenceMap() {
		return underlying;
	}

}
