package de.jungblut.nlp.model;

import java.util.Arrays;
import java.util.Collection;

/**
 * Reference and its context. Which is a one-to-many mapping, hashcode and
 * equals are implemented on the reference.
 * 
 * @param <REF_TYPE> the reference type.
 * @param <CONTEXT_TYPE> the context type.
 */
public final class ReferencedContext<REF_TYPE, CONTEXT_TYPE> {

  private final REF_TYPE reference;
  private final Collection<CONTEXT_TYPE> context;

  @SafeVarargs
  public ReferencedContext(REF_TYPE reference, CONTEXT_TYPE... context) {
    this(reference, Arrays.asList(context));
  }

  public ReferencedContext(REF_TYPE reference, Collection<CONTEXT_TYPE> context) {
    this.reference = reference;
    this.context = context;
  }

  public REF_TYPE getReference() {
    return this.reference;
  }

  public Collection<CONTEXT_TYPE> getContext() {
    return this.context;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.reference == null) ? 0 : this.reference.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    @SuppressWarnings("rawtypes")
    ReferencedContext other = (ReferencedContext) obj;
    if (this.reference == null) {
      if (other.reference != null)
        return false;
    } else if (!this.reference.equals(other.reference))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ReferencedContext [reference=" + this.reference + ", context="
        + this.context + "]";
  }

}
