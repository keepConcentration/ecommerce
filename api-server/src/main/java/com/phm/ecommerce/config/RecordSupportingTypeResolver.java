package com.phm.ecommerce.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

public class RecordSupportingTypeResolver extends DefaultTypeResolverBuilder {

  public RecordSupportingTypeResolver(DefaultTyping t, PolymorphicTypeValidator ptv) {
    super(t, ptv);
  }

  @Override
  public boolean useForType(JavaType t) {
    boolean isRecord = t.getRawClass().isRecord();
    boolean superResult = super.useForType(t);

    if (isRecord) {
      return true;
    }
    return superResult;
  }
}
