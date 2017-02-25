package com.example.zpiao1.excited.logic;

import android.util.SparseArray;

public class Subject<T> {

  private T mCurrentValue;
  private SparseArray<Observer<T>> mObservers;

  public Subject() {
    mObservers = new SparseArray<>();
  }

  public Subject(T defaultValue) {
    mCurrentValue = defaultValue;
    mObservers = new SparseArray<>();
  }

  public void subscribe(int id, Observer<T> observer) {
    mObservers.put(id, observer);
    update(mCurrentValue);
  }

  public void unsubscribe(int id) {
    mObservers.remove(id);
  }

  public void update(T value) {
    mCurrentValue = value;
    for (int i = 0; i < mObservers.size(); ++i) {
      mObservers.valueAt(i).onNext(value);
    }
  }

  public void updateError(Throwable throwable) {
    for (int i = 0; i < mObservers.size(); ++i) {
      mObservers.valueAt(i).onError(throwable);
    }
  }
}
