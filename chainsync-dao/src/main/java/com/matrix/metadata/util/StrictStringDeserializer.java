package com.matrix.metadata.util;

/**
 * @author yangjian
 * @date 2022/2/11
 */
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import java.io.IOException;

public class StrictStringDeserializer extends StringDeserializer {
  @Override
  public String deserialize(final JsonParser p, final DeserializationContext ctxt)
      throws IOException {
    final JsonToken token = p.currentToken();
    if (token.isBoolean()
        || token.isNumeric()
        || !"VALUE_STRING".equalsIgnoreCase(token.toString())) {
      ctxt.reportInputMismatch(String.class, "%s is not a `String` value!", token.toString());
      return null;
    }
    return super.deserialize(p, ctxt);
  }
}
