package com.chainsync.common.model;

import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author luyuanheng
 */
public class RequestHeaderConstants {

  public static final String REQUEST_ID = "x-request-id";
  public static final String TRACE_ID = "x-b3-traceid";
  public static final String SPAN_ID = "x-b3-spanid";
  public static final String PARENT_SPAN_ID = "x-b3-parentspanid";
  public static final String DATADOG_TRACE_ID = "x-datadog-trace-id";
  public static final String DATADOG_PARENT_ID = "x-datadog-parent-id";
  public static final String AMZN_TRACE_ID = "x-amzn-trace-id";

  public static final Metadata.Key<String> x_request_id =
      Metadata.Key.of(RequestHeaderConstants.REQUEST_ID, Metadata.ASCII_STRING_MARSHALLER);
  public static final Metadata.Key<String> x_b3_traceid =
      Metadata.Key.of(RequestHeaderConstants.TRACE_ID, Metadata.ASCII_STRING_MARSHALLER);
  public static final Metadata.Key<String> x_b3_spanid =
      Metadata.Key.of(RequestHeaderConstants.SPAN_ID, Metadata.ASCII_STRING_MARSHALLER);
  public static final Metadata.Key<String> x_b3_parentspanid =
      Metadata.Key.of(RequestHeaderConstants.PARENT_SPAN_ID, Metadata.ASCII_STRING_MARSHALLER);
  public static final Metadata.Key<String> x_datadog_traceid =
      Metadata.Key.of(RequestHeaderConstants.DATADOG_TRACE_ID, Metadata.ASCII_STRING_MARSHALLER);
  public static final Metadata.Key<String> x_datadog_parentid =
      Metadata.Key.of(RequestHeaderConstants.DATADOG_PARENT_ID, Metadata.ASCII_STRING_MARSHALLER);
  public static final Metadata.Key<String> x_amzn_traceid =
      Metadata.Key.of(RequestHeaderConstants.AMZN_TRACE_ID, Metadata.ASCII_STRING_MARSHALLER);

  public static final Context.Key<String> context_x_request_id =
      Context.key(RequestHeaderConstants.REQUEST_ID);
  public static final Context.Key<String> context_x_b3_traceid =
      Context.key(RequestHeaderConstants.TRACE_ID);
  public static final Context.Key<String> context_x_b3_spanid =
      Context.key(RequestHeaderConstants.SPAN_ID);
  public static final Context.Key<String> context_x_b3_parentspanid =
      Context.key(RequestHeaderConstants.PARENT_SPAN_ID);
  public static final Context.Key<String> context_x_datadog_traceid =
      Context.key(RequestHeaderConstants.DATADOG_TRACE_ID);
  public static final Context.Key<String> context_x_datadog_parentid =
      Context.key(RequestHeaderConstants.DATADOG_PARENT_ID);
  public static final Context.Key<String> context_x_amzn_traceid =
      Context.key(RequestHeaderConstants.AMZN_TRACE_ID);

  public static final Map<String, Context.Key<String>> contextKeyMap;
  public static final List<Key<String>> tracingKeys;
  public static final List<Context.Key<String>> contextKeys;

  static {
    contextKeyMap = new HashMap<>();
    contextKeyMap.put(REQUEST_ID, context_x_request_id);
    contextKeyMap.put(TRACE_ID, context_x_b3_traceid);
    contextKeyMap.put(SPAN_ID, context_x_b3_spanid);
    contextKeyMap.put(PARENT_SPAN_ID, context_x_b3_parentspanid);
    contextKeyMap.put(DATADOG_TRACE_ID, context_x_datadog_traceid);
    contextKeyMap.put(DATADOG_PARENT_ID, context_x_datadog_parentid);
    contextKeyMap.put(AMZN_TRACE_ID, context_x_amzn_traceid);

    tracingKeys = new ArrayList<>();
    tracingKeys.add(x_request_id);
    tracingKeys.add(x_b3_traceid);
    tracingKeys.add(x_b3_spanid);
    tracingKeys.add(x_b3_parentspanid);
    tracingKeys.add(x_datadog_traceid);
    tracingKeys.add(x_datadog_parentid);
    tracingKeys.add(x_amzn_traceid);

    contextKeys = new ArrayList<>();
    contextKeys.add(context_x_request_id);
    contextKeys.add(context_x_b3_traceid);
    contextKeys.add(context_x_b3_spanid);
    contextKeys.add(context_x_b3_parentspanid);
    contextKeys.add(context_x_datadog_traceid);
    contextKeys.add(context_x_datadog_parentid);
    contextKeys.add(context_x_amzn_traceid);
  }
}
