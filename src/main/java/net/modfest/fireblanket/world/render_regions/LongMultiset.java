package net.modfest.fireblanket.world.render_regions;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Multiset;

import it.unimi.dsi.fastutil.longs.AbstractLongIterator;
import it.unimi.dsi.fastutil.longs.Long2IntArrayMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class LongMultiset implements Multiset<Long> {

	public interface Entry extends Multiset.Entry<Long> {
		
		@Override @Deprecated
		default Long getElement() {
			return getLongElement();
		}
		
		long getLongElement();
		
	}
	
	private final Long2IntMap underlying = new Long2IntOpenHashMap();

	@Override
	public boolean isEmpty() {
		return underlying.isEmpty();
	}

	@Override
	public Object[] toArray() {
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return null;
	}

	@Override @Deprecated
	public boolean addAll(Collection<? extends Long> c) {
		for (Long l : c) {
			add(l);
		}
		return true;
	}

	@Override
	public void clear() {
		underlying.clear();
	}

	@Override
	public int size() {
		int count = 0;
		for (var en : underlying.long2IntEntrySet()) {
			count += en.getIntValue();
		}
		return count;
	}

	@Override @Deprecated
	public int count(Object element) {
		if (element instanceof Long l) return count(l.longValue());
		return 0;
	}

	public int count(long element) {
		return underlying.get(element);
	}

	@Override @Deprecated
	public int add(Long element, int occurrences) {
		return add(element.longValue(), occurrences);
	}

	@Override @Deprecated
	public boolean add(Long element) {
		return add(element.longValue());
	}
	
	public int add(long element, int occurences) {
		if (occurences < 0) throw new IllegalArgumentException("occurences cannot be negative ("+occurences+")");
		int old = underlying.get(element);
		if (occurences == 0) return old;
		int count = old+occurences;
		if (count < 0) {
			throw new IllegalArgumentException("overflow (adding "+occurences+" to "+old+")");
		} else if (count == 0) {
			underlying.remove(element);
		} else {
			underlying.put(element, count);
		}
		return old;
	}
	
	public boolean add(long element) {
		add(element, 1);
		return true;
	}

	@Override @Deprecated
	public int remove(Object element, int occurrences) {
		if (element instanceof Long l) return remove(l.longValue(), occurrences);
		return 0;
	}

	@Override @Deprecated
	public boolean remove(Object element) {
		if (element instanceof Long l) return remove(l.longValue());
		return false;
	}
	
	public boolean remove(long element) {
		int old = underlying.get(element);
		if (old == 0) return false;
		int count = old-1;
		if (count == 0) {
			underlying.remove(element);
		} else {
			underlying.put(element, count);
		}
		return true;
	}
	
	public int remove(long element, int occurences) {
		if (occurences < 0) throw new IllegalArgumentException("occurences cannot be negative ("+occurences+")");
		int old = underlying.get(element);
		if (occurences == 0) return old;
		int count = old-occurences;
		if (count <= 0) {
			underlying.remove(element);
		} else {
			underlying.put(element, count);
		}
		return old;
	}

	@Override @Deprecated
	public int setCount(Long element, int count) {
		return setCount(element.longValue(), count);
	}

	@Override @Deprecated
	public boolean setCount(Long element, int oldCount, int newCount) {
		return setCount(element.longValue(), oldCount, newCount);
	}
	
	public int setCount(long element, int count) {
		return underlying.put(element, count);
	}

	public boolean setCount(long element, int oldCount, int newCount) {
		if (underlying.get(element) == oldCount) {
			underlying.put(element, newCount);
			return true;
		}
		return false;
	}

	@Override
	public LongSet elementSet() {
		return underlying.keySet();
	}

	@Override @Deprecated
	public Set<Multiset.Entry<Long>> entrySet() {
		return (Set)longEntrySet();
	}
	
	public ObjectSet<Entry> longEntrySet() {
		return new AbstractObjectSet<LongMultiset.Entry>() {

			@Override
			public int size() {
				return LongMultiset.this.size();
			}

			@Override
			public boolean add(Entry e) {
				LongMultiset.this.add(e.getLongElement(), e.getCount());
				return true;
			}

			@Override
			public boolean addAll(Collection<? extends Entry> c) {
				for (Entry e : c) {
					add(e);
				}
				return true;
			}

			@Override
			public ObjectIterator<Entry> iterator() {
				return new AbstractObjectIterator<LongMultiset.Entry>() {
					private final ObjectIterator<Long2IntMap.Entry> entries = underlying.long2IntEntrySet().iterator();

					@Override
					public boolean hasNext() {
						return entries.hasNext();
					}

					@Override
					public Entry next() {
						Long2IntMap.Entry en = entries.next();
						return new Entry() {
							
							@Override
							public int getCount() {
								return en.getIntValue();
							}
							
							@Override
							public long getLongElement() {
								return en.getLongKey();
							}
						};
					}
				};
			}
		};
	}

	@Override
	public LongIterator iterator() {
		return new AbstractLongIterator() {
			private final ObjectIterator<Long2IntMap.Entry> entries = underlying.long2IntEntrySet().iterator();
			
			private long element;
			private int count;
			
			@Override
			public boolean hasNext() {
				return count > 0 || entries.hasNext();
			}
			
			@Override
			public long nextLong() {
				if (count <= 0) {
					Long2IntMap.Entry en = entries.next();
					element = en.getLongKey();
					count = en.getIntValue();
				}
				count--;
				return element;
			}
		};
	}

	@Override @Deprecated
	public boolean contains(Object element) {
		if (element instanceof Long l) return underlying.containsKey(l);
		return false;
	}
	
	public boolean contains(long element) {
		return underlying.containsKey(element);
	}

	@Override @Deprecated
	public boolean containsAll(Collection<?> elements) {
		for (Object o : elements) {
			if (!contains(o)) return false;
		}
		return true;
	}

	@Override @Deprecated
	public boolean removeAll(Collection<?> c) {
		boolean b = false;
		for (Object o : c) {
			b |= remove(o);
		}
		return b;
	}

	@Override @Deprecated
	public boolean retainAll(Collection<?> c) {
		Long2IntMap keep = new Long2IntArrayMap();
		for (Object o : c) {
			if (o instanceof Long l) keep.put(l.longValue(), count(l.longValue()));
		}
		if (keep.size() == underlying.size()) return false;
		underlying.clear();
		underlying.putAll(keep);
		return true;
	}

	public boolean containsAll(LongCollection elements) {
		for (long l : elements) {
			if (!contains(l)) return false;
		}
		return true;
	}

	public boolean removeAll(LongCollection c) {
		boolean b = false;
		for (long l : c) {
			b |= remove(l);
		}
		return b;
	}

	public boolean retainAll(LongCollection c) {
		Long2IntMap keep = new Long2IntArrayMap();
		for (long l : c) {
			keep.put(l, count(l));
		}
		if (keep.size() == underlying.size()) return false;
		underlying.clear();
		underlying.putAll(keep);
		return true;
	}
	
}
