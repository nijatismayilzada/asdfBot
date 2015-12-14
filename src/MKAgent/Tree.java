package MKAgent;

public class Tree {
  private Node root;

  public Tree() {
    this.root = new Node();
  }

  public Tree(Node root) {
    this();
    this.setRoot(root);
  }

  public Node getRoot() {
    return this.root;
  }

  public void setRoot(Node root) {
    this.root = root;
  }
}
