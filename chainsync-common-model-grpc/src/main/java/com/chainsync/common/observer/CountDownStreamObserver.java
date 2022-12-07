package com.chainsync.common.observer;

import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;

/**
 * @author richard
 */
public class CountDownStreamObserver<T> implements StreamObserver<T> {

  private T result;

  CountDownLatch countDownLatch;

  public CountDownStreamObserver(final CountDownLatch countDownLatch) {
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void onNext(final T value) {
    result = value;
  }

  @Override
  public void onError(final Throwable t) {
    countDownLatch.countDown();
  }

  @Override
  public void onCompleted() {
    countDownLatch.countDown();
  }

  @SneakyThrows
  public T getResult() {
    countDownLatch.await(1, TimeUnit.MINUTES);
    return this.result;
  }
}
