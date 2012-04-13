package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hama.bsp.BSPMessage;

public final class CollectionMessage<T extends Writable> extends BSPMessage {

  private String tag;
  private Collection<T> collection;
  private Class<T> itemClass;

  public CollectionMessage() {
  }

  public CollectionMessage(String tag, Collection<T> collection,
      Class<T> itemClass) {
    super();
    this.tag = tag;
    this.collection = collection;
    this.itemClass = itemClass;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readFields(DataInput in) throws IOException {
    int size = in.readInt();
    String clazz = in.readUTF();
    try {
      collection = (Collection<T>) ReflectionUtils.newInstance(
          Class.forName(clazz), null);

      String itemClassName = in.readUTF();
      itemClass = (Class<T>) Class.forName(itemClassName);
      for (int i = 0; i < size; i++) {
        Writable newInstance = (Writable) ReflectionUtils.newInstance(
            itemClass, null);
        newInstance.readFields(in);
        collection.add((T) newInstance);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    tag = in.readUTF();
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(collection.size());
    out.writeUTF(collection.getClass().getName());
    out.writeUTF(itemClass.getName());
    for (T t : collection) {
      t.write(out);
    }
    out.writeUTF(tag);
  }

  @Override
  public Collection<T> getData() {
    return collection;
  }

  @Override
  public String getTag() {
    return tag;
  }

}
