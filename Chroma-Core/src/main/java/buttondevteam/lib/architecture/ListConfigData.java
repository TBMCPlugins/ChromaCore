package buttondevteam.lib.architecture;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ListConfigData<T> extends ConfigData<ListConfigData.List<T>> {
	@SuppressWarnings("unchecked")
	ListConfigData(IHaveConfig config, String path, List<T> def) {
		super(config, path, def, new ArrayList<>(def), list -> {
			var l = new List<>((ArrayList<T>) list);
			l.listConfig = def.listConfig;
			return l;
		}, ArrayList::new);
		def.listConfig = this; //Can't make the List class non-static or pass this in the super() constructor
	}

	public static class List<T> extends ArrayList<T> {
		private ListConfigData<T> listConfig;

		public List(@NotNull Collection<? extends T> c) {
			super(c);
		}

		public List() {
		}

		private void update() {
			listConfig.set(this); //Update the config model and start save task if needed
		}

		@Override
		public T set(int index, T element) {
			T ret = super.set(index, element);
			update();
			return ret;
		}

		@Override
		public boolean add(T t) {
			val ret = super.add(t);
			update();
			return ret;
		}

		@Override
		public void add(int index, T element) {
			super.add(index, element);
			update();
		}

		@Override
		public T remove(int index) {
			T ret = super.remove(index);
			update();
			return ret;
		}

		@Override
		public boolean remove(Object o) {
			val ret = super.remove(o);
			update();
			return ret;
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			val ret = super.addAll(c);
			update();
			return ret;
		}

		@Override
		public boolean addAll(int index, Collection<? extends T> c) {
			val ret = super.addAll(index, c);
			update();
			return ret;
		}

		@Override
		protected void removeRange(int fromIndex, int toIndex) {
			super.removeRange(fromIndex, toIndex);
			update();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			val ret = super.removeAll(c);
			update();
			return ret;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			val ret = super.retainAll(c);
			update();
			return ret;
		}

		@Override
		public boolean removeIf(Predicate<? super T> filter) {
			val ret = super.removeIf(filter);
			update();
			return ret;
		}

		@Override
		public void replaceAll(UnaryOperator<T> operator) {
			super.replaceAll(operator);
			update();
		}

		@Override
		public void sort(Comparator<? super T> c) {
			super.sort(c);
			update();
		}
	}
}
