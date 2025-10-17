package fi.kissakala;

import java.util.*;

/**
 * Simple generic tree
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public record Tree<T>(Node<T> root) {
	public static class Node<T> {
		private final T value;
		private final List<Node<T>> children = new ArrayList<>();

		private Tree<T> tree;
		private Node<T> parent;

		public Node(final T value) {
			this.value = value;
		}

		public Node(final T value, final Node<T> parent) {
			this.value = value;
			this.parent = parent;
			this.tree = parent.getTree();
		}

		/**
		 * Add child
		 * @param child Child to add
		 * @return The added child
		 */
		public Node<T> addChild(final Node<T> child) {
			child.setTree(this.getTree());
			child.setParent(this);
			this.children.add(child);
			return child;
		}

		/**
		 * Create and add child with the given {@param childValue}
		 * @return The added child
		 */
		public Node<T> addChild(final T childValue) {
			return this.addChild(new Node<>(childValue));
		}

		/**
		 * Remove child
		 * @param child Child to remove
		 */
		public void removeChild(final Node<T> child) {
			child.setTree(null);
			child.setParent(null);
			this.children.remove(child);
		}

		public T getValue() {
			return value;
		}

		public Tree<T> getTree() {
			return tree;
		}
		private void setTree(Tree<T> tree) {
			this.tree = tree;
		}

		public Node<T> getParent() {
			return parent;
		}
		private void setParent(final Node<T> parent) {
			this.parent = parent;
		}

		public List<Node<T>> getChildren() {
			return Collections.unmodifiableList(children);
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}

	public Tree(final Node<T> root) {
		this.root = root;
		this.root.setTree(this);
		fixTree();
	}

	public static <T> Tree<T> withRoot(final T rootValue) {
		return new Tree<>(new Node<>(rootValue));
	}
	public static <T> Tree<T> withRoot(final Node<T> root) {
		return new Tree<>(root);
	}

	/**
	 * Find all nodes with value {@param value}
	 * @return List of all nodes with value {@param value}
	 */
	public List<Node<T>> findNodes(final T value) {
		final List<Node<T>> result = new ArrayList<>();

		final Deque<Node<T>> stack = new ArrayDeque<>();
		stack.push(root);

		while (!stack.isEmpty()) {
			final Node<T> n = stack.pop();
			if (Objects.equals(n.getValue(), value)) {
				result.add(n);
			}
			for (final Node<T> c : n.getChildren()) {
				stack.push(c);
			}
		}

		return result;
	}

	/**
	 * Get the distance in number of edges between two node references.
	 * @return >=0 or -1 if either nodes belong to different trees
	 * @throws NullPointerException If either node is null
	 */
	public int distance(final Node<T> a, final Node<T> b) {
		if (a == null || b == null) {
			throw new NullPointerException("Nodes cannot be null");
		}
		if (a == b) {
			return 0;
		}

		// Validate same tree by checking root identity
		if (a.getTree() != b.getTree()) {
			return -1;
		}

		final Map<Node<T>, Integer> anc = new IdentityHashMap<>();
		Node<T> cur = a;

		// Map ancestors of a to distance from a
		int dist = 0;
		while (cur != null) {
			anc.put(cur, dist);
			cur = cur.getParent();
			dist++;
		}

		// Walk up b's ancestors until we find an ancestor of a (LCA)
		cur = b;
		dist = 0;
		while (cur != null) {
			final Integer da = anc.get(cur);
			if (da != null) {
				return da + dist; // steps from a to LCA + steps from b to LCA
			}
			cur = cur.getParent();
			dist++;
		}
		return -1; // should not happen if same tree, but safe fallback
	}

	/**
	 * Return the shortest path (list of nodes) from {@param start} to {@param end}.
	 * @return A list containing a path from {@param start} to {@param end}, including both
	 * @throws NullPointerException If either node is null
	 * @throws IllegalArgumentException If the nodes are NOT in the same tree
	 */
	public List<Node<T>> getPath(final Node<T> start, final Node<T> end) {
		if (start == null || end == null) {
			throw new NullPointerException("Nodes cannot be null");
		}
		if (start == end) {
			return List.of(start);
		}

		// verify same tree
		if (start.getTree() != end.getTree()) {
			throw new IllegalArgumentException("Nodes are NOT in the same tree");
		}

		// mark all ancestors of a (by identity)
		final IdentityHashMap<Node<T>, Boolean> anc = new IdentityHashMap<>();
		Node<T> cur = start;
		while (cur != null) {
			anc.put(cur, Boolean.TRUE);
			cur = cur.getParent();
		}

		// find the lowest common ancestor by walking up from b
		Node<T> lca = null;
		cur = end;
		while (cur != null) {
			if (anc.containsKey(cur)) {
				lca = cur;
				break;
			}
			cur = cur.getParent();
		}
		if (lca == null) {
			throw new RuntimeException("Should not happen");
		}

		// build path: a -> ... -> lca
		final List<Node<T>> path = new ArrayList<>();
		cur = start;
		while (cur != lca) {
			path.add(cur);
			cur = cur.getParent();
		}
		path.add(lca);

		// build tail from lca -> ... -> b by collecting b->...->lca then reversing (exclude lca)
		final List<Node<T>> tail = new ArrayList<>();
		cur = end;
		while (cur != lca) {
			tail.add(cur);
			cur = cur.getParent();
		}
		Collections.reverse(tail);
		path.addAll(tail);

		return path;
	}

	public void fixTree() {
		final Deque<Node<T>> stack = new ArrayDeque<>();
		stack.push(root);

		while (!stack.isEmpty()) {
			final Node<T> node = stack.pop();
			node.setTree(this);
			for (final Node<T> child : node.getChildren()) {
				stack.push(child);
			}
		}
	}

	public static void test() {
		final Tree<String> tree = Tree.withRoot("root");
		final Node<String> root = tree.root();

		// tree with duplicate values
		final Tree.Node<String> b1 = root.addChild("B");
		final Tree.Node<String> b2 = root.addChild("B");
		final Tree.Node<String> c  = b1.addChild("C");
		final Tree.Node<String> d  = b2.addChild("D");
		final Tree.Node<String> c2 = b2.addChild("C");

		// findNodes should find duplicates
		Utils.expect(tree.findNodes("B").size(), 2);
		Utils.expect(tree.findNodes("C").size(), 2);

		// parent relationships
		Utils.expect(b1.getParent(), root);
		Utils.expect(b2.getParent(), root);
		Utils.expect(c.getParent(), b1);

		// children counts
		Utils.expect(root.getChildren().size(), 2);
		Utils.expect(b2.getChildren().size(), 2);

		// distance
		Utils.expect(tree.distance(b1, b2), 2);
		Utils.expect(tree.distance(c, c2), 4);
		Utils.expect(tree.distance(c, d), 4);
		Utils.expect(tree.distance(root, d), 2);
		Utils.expect(tree.distance(root, root), 0);

		// nodes from different trees => -1
		final Tree<String> otherTree = Tree.withRoot("other");
		final Node<String> otherRoot = otherTree.root();
		final Node<String> otherChild = otherRoot.addChild("X");

		Utils.expect(tree.distance(root, otherChild), -1);
		Utils.expect(otherTree.distance(otherChild, b1), -1);

		// getPath
		final List<Tree.Node<String>> path = tree.getPath(c, c2);
		Utils.expect(path.size(), 5);
		Utils.expect(path.get(0), c);
		Utils.expect(path.get(1), b1);
		Utils.expect(path.get(2), root);
		Utils.expect(path.get(3), b2);
		Utils.expect(path.get(4), c2);

		// same-node path
		Utils.expect(tree.getPath(root, root).size(), 1);
	}
}