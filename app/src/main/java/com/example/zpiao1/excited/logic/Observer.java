package com.example.zpiao1.excited.logic;


public interface Observer<T> {

  void onNext(T value);

  void onError(Throwable throwable);

}
