package org.swrlapi.factory;

import checkers.nullness.quals.NonNull;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.swrlapi.exceptions.SWRLAPIInternalException;
import org.swrlapi.bridge.resolvers.OWLObjectPropertyExpressionResolver;

import java.util.HashMap;
import java.util.Map;

class DefaultOWLObjectPropertyExpressionResolver implements OWLObjectPropertyExpressionResolver
{
  @NonNull private final Map<String, OWLObjectPropertyExpression> id2OWLPropertyExpression;
  @NonNull private final Map<OWLObjectPropertyExpression, String> owlPropertyExpression2ID;

  public DefaultOWLObjectPropertyExpressionResolver()
  {
    this.id2OWLPropertyExpression = new HashMap<>();
    this.owlPropertyExpression2ID = new HashMap<>();
  }

  @Override public void reset()
  {
    this.id2OWLPropertyExpression.clear();
    this.owlPropertyExpression2ID.clear();
  }

  @Override public void recordOWLObjectPropertyExpression(@NonNull String propertyExpressionID,
      @NonNull OWLObjectPropertyExpression propertyExpression)
  {
    this.id2OWLPropertyExpression.put(propertyExpressionID, propertyExpression);
    this.owlPropertyExpression2ID.put(propertyExpression, propertyExpressionID);
  }

  @Override public boolean recordsOWLObjectPropertyExpression(@NonNull OWLObjectPropertyExpression propertyExpression)
  {
    return this.owlPropertyExpression2ID.containsKey(propertyExpression);
  }

  @Override @NonNull public String resolveOWLObjectPropertyExpression(
      @NonNull OWLObjectPropertyExpression propertyExpression)
  {
    if (this.owlPropertyExpression2ID.containsKey(propertyExpression))
      return this.owlPropertyExpression2ID.get(propertyExpression);
    else
      throw new IllegalArgumentException("no ID found for object property expression " + propertyExpression);
  }

  @Override @NonNull public OWLObjectPropertyExpression resolveOWLObjectPropertyExpression(
      @NonNull String propertyExpressionID)
  {
    if (this.id2OWLPropertyExpression.containsKey(propertyExpressionID))
      return this.id2OWLPropertyExpression.get(propertyExpressionID);
    else
      throw new SWRLAPIInternalException("no OWL object property expression found with ID " + propertyExpressionID);
  }
}
